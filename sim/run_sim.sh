#!/usr/bin/env bash
# Runs our OpModes in the virtual_robot simulator. See sim/README.md.
#
#   sim/run_sim.sh              build what is missing, then open the simulator
#   sim/run_sim.sh --build-only compile everything and stop (no window)
#   sim/run_sim.sh --rebuild    throw away the cached sim build first
#
# Everything it downloads or builds goes in sim/.cache (git-ignored). Set SIM_JDK to a JDK 17 that
# includes JavaFX (Liberica "Full") to skip the JDK download.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(dirname "$HERE")"
CACHE="$HERE/.cache"
TEAMCODE="$REPO/TeamCode/src/main/java"

# Pinned so the patches in patch_sim.py match. Change both together.
SIM_URL="https://github.com/Beta8397/virtual_robot.git"
PINNED_COMMIT="168a8a32028f7b1e51a3a767329f193c18b6a911"
JDK_VERSION="17.0.20.1+1"

BUILD_ONLY=0
for arg in "$@"; do
  case "$arg" in
    --build-only) BUILD_ONLY=1 ;;
    --rebuild) rm -rf "$CACHE/sim-classes" "$CACHE/virtual_robot" ;;
    *) echo "unknown option: $arg"; exit 2 ;;
  esac
done

[ "$(uname -s)" = "Darwin" ] || { echo "Only tested on macOS. On another OS set SIM_JDK and run the steps by hand."; [ -n "${SIM_JDK:-}" ] || exit 1; }
mkdir -p "$CACHE"

# 1. A JDK 17 with JavaFX.
if [ -n "${SIM_JDK:-}" ]; then
  JDK="$SIM_JDK"
else
  case "$(uname -m)" in
    x86_64) ARCH=amd64; SHA1=ac77be96e030cb23e8f927c938553b57328cd7f9 ;;
    arm64)  ARCH=aarch64; SHA1=68317f6523574deefd6f9e3b2b22bc434e15b6ba ;;   # not tested on Apple Silicon
    *) echo "unsupported CPU $(uname -m)"; exit 1 ;;
  esac
  JDK="$CACHE/jdk17/jdk-17.0.20.1-full.jdk"
  if [ ! -x "$JDK/bin/java" ]; then
    echo "Downloading Liberica JDK $JDK_VERSION Full ($ARCH, about 310 MB) from BellSoft..."
    TAR="$CACHE/jdk.tar.gz"
    curl -fSL -o "$TAR" "https://github.com/bell-sw/Liberica/releases/download/$JDK_VERSION/bellsoft-jdk17.0.20.1+1-macos-$ARCH-full.tar.gz"
    echo "$SHA1  $TAR" | shasum -a 1 -c - || { echo "JDK checksum mismatch, not unpacking"; rm -f "$TAR"; exit 1; }
    mkdir -p "$CACHE/jdk17" && tar -xzf "$TAR" -C "$CACHE/jdk17" && rm -f "$TAR"
  fi
fi
"$JDK/bin/java" --list-modules | grep -q javafx.controls || { echo "$JDK has no JavaFX. Use a Liberica Full JDK 17."; exit 1; }

# 2. The simulator at the pinned commit, patched.
SIMSRC="$CACHE/virtual_robot"
if [ ! -d "$SIMSRC/.git" ]; then
  echo "Fetching virtual_robot at $PINNED_COMMIT..."
  git init -q "$SIMSRC" && git -C "$SIMSRC" remote add origin "$SIM_URL"
  git -C "$SIMSRC" fetch -q --depth 1 origin "$PINNED_COMMIT"
  git -C "$SIMSRC" checkout -q FETCH_HEAD
fi
python3 "$HERE/patch_sim.py" "$SIMSRC"

# 3. Compile the simulator once (and its assets).
LIBS="$(ls "$SIMSRC"/lib/*.jar | tr '\n' ':')"
KOALA="$(find "$HOME/.gradle" -name 'KoalaLogger-*-runtime.jar' 2>/dev/null | head -1)"
[ -n "$KOALA" ] || { echo "Koala-Log jar not in ~/.gradle. Run ./gradlew :TeamCode:compileDebugJavaWithJavac once, then retry."; exit 1; }
SIMOUT="$CACHE/sim-classes"
if [ ! -d "$SIMOUT" ]; then
  echo "Compiling the simulator..."
  mkdir -p "$SIMOUT"
  find "$SIMSRC/Controller/src" "$SIMSRC/TeamCode/src" -name '*.java' > "$CACHE/sim-sources.txt"
  "$JDK/bin/javac" -proc:none -nowarn -d "$SIMOUT" -cp "$LIBS" @"$CACHE/sim-sources.txt"
  for d in Controller TeamCode; do rsync -a --exclude='*.java' "$SIMSRC/$d/src/" "$SIMOUT/"; done
fi

# 4. Compile our OpModes (every run: it is quick, and it picks up your edits).
OURS="$CACHE/our-classes"
rm -rf "$OURS" && mkdir -p "$OURS"
SOURCES=()
while IFS= read -r line; do
  line="${line%%#*}"; line="$(echo "$line" | xargs)"
  [ -n "$line" ] && SOURCES+=("$TEAMCODE/$line")
done < "$HERE/opmodes.txt"
[ ${#SOURCES[@]} -gt 0 ] || { echo "sim/opmodes.txt lists no OpModes"; exit 1; }
echo "Compiling ${#SOURCES[@]} OpMode(s) from sim/opmodes.txt..."
"$JDK/bin/javac" -proc:none -nowarn -d "$OURS" -cp "$SIMOUT:$LIBS$KOALA" -sourcepath "$TEAMCODE" "${SOURCES[@]}"

[ "$BUILD_ONLY" = 1 ] && { echo "Build OK."; exit 0; }

# 5. Run. In the window: Configurations -> MecDynamic Bot, pick the OpMode, INIT, START.
exec "$JDK/bin/java" -cp "$OURS:$SIMOUT:$LIBS$KOALA" virtual_robot.controller.VirtualRobotApplication
