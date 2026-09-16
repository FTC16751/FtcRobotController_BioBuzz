# Moving to FTC SDK 12.0 for BIOBUZZ, and what to do with the Decode robots

**Written 2026-09-15.** Answers three questions the mentor asked: is the Pedro Quickstart on the
newest SDK, Quickstart or parent SDK plus Pedro, and how to migrate this repo. Facts checked against
the GitHub releases and repo contents on that date.

## The facts

| | |
|---|---|
| Newest FTC SDK | **v12.0, released 2026-09-12**, the 2026-27 (BIOBUZZ) season release. Needs Gradle 9.1, AGP 8.13, Android Studio Narwhal 3 Feature Drop or later |
| v12.0 breaking change | `AprilTagDetection` is now single-or-cluster and must be cast. **We have zero uses**: all our tag work goes through the Limelight, so nothing breaks. BIOBUZZ tags move, so the SDK notes say they are not usable for field localization |
| Pedro Quickstart (`Pedro-Pathing/Quickstart`) | a fork of `FIRST-Tech-Challenge/FtcRobotController` on **SDK 11.2.1**, two commits behind v12.0 as of 2026-09-13. Its whole difference from the parent is three things: `build.dependencies.gradle` (Pedro `revhub:3.0.0`, `tuning:1.0.0`, the dairy.foundation Maven repo), `compileSdk 34` in two Gradle files, and `TeamCode/.../pedro/` (Constants, Tuning, nine AutoTune procedure files, about 3,500 lines) |
| This repo | descends from the Road Runner quickstart (the `upstream` remote), which tracks the FTC SDK; last SDK merge was v11.0. Outside `TeamCode` we changed five files: Java 17 source level, `compileSdk 34`, AGP 8.9.2, the Gradle wrapper, one line in a sample |
| Our `TeamCode` | 231 Java files: `common` 46 (unit-tested), `teams` 115 (GG, P3, testteam2027, skyline), `demobots` 8, `samples` 6, `pedropathing` 7, `roadrunner` 20, `legacy` 7, plus `doc/` |

## Answers

1. **Not the newest SDK.** The Quickstart is on 11.2.1; v12.0 came out three days before this was
   written and the Quickstart had not merged it yet.
2. **Parent SDK plus Pedro installed by hand.** The Quickstart is the parent plus three small,
   copyable things, and it lags SDK releases. We also do not use its `Constants.java` pattern; our
   Pedro numbers live in each robot's config through `common/PedroBridge`. Take the parent at the
   v12.0 tag, add the two Pedro lines and the Maven repo to `build.dependencies.gradle`, and copy the
   `pedro/procedures/` folder for AutoTune when the Pedro 3 migration happens
   (`PEDRO_ON_TEST2027.md` section 6).
3. **Two repos.** Freeze this one for the Decode robots; make a new one for BIOBUZZ.

## The plan

**This repo (`FtcRobotController_Decode`) stays on SDK 11.0** and is the home of the GearGirls, P3,
Skyline and demo robot code. It compiles today; nothing in it needs to move. A Decode robot keeps
running as long as its Robot Controller and Driver Station apps stay on the same major version, so
the demo Driver Station phones stay on 11.x too. (The Decode code would also compile on 12.0
unchanged, so moving them later is a choice, not a risk.)

**New repo `FtcRobotController_BioBuzz`**, made so that history and future SDK merges both work:

1. Clone this repo, add the FTC remote, merge the v12.0 tag:
   ```
   git clone https://github.com/FTC16751/FtcRobotController_Decode.git FtcRobotController_BioBuzz
   cd FtcRobotController_BioBuzz
   git remote add ftc https://github.com/FIRST-Tech-Challenge/FtcRobotController.git
   git fetch ftc --tags
   git merge v12.0
   ```
   Expected conflicts: the five Gradle files (`build.gradle` AGP, `gradle-wrapper.properties`,
   `build.common.gradle`, `build.dependencies.gradle`, `FtcRobotController/build.gradle`). Take
   the SDK's side for AGP and the wrapper, keep our Java 17 line and `compileSdk 34`, and set the
   eight `org.firstinspires.ftc` lines to 12.0.0. Everything else merges clean because we never
   touched the Robot Controller app.
2. Delete what belongs to Decode only: `teams/geargirls`, `teams/p3`, `teams/skyline`, `demobots`,
   `legacy`, `roadrunner`, and the Road Runner and Dashboard lines in `TeamCode/build.gradle`.
   Keep `common` and its tests, `teams/testteam2027` (the template), `samples`, `pedropathing`,
   `doc`.
3. Point `origin` at the new GitHub repo and push. Set the Android Studio project name.
4. Build, run the unit tests, deploy to the test robot: RUN ME TeleOp, Tag Approach (Limelight),
   both squares. That is the whole acceptance test; expect it to pass unchanged.
5. Then, on a branch, the Pedro 3 migration from `PEDRO_ON_TEST2027.md` section 6.

Why clone-and-merge rather than copy files into a fresh SDK download: the git history of `common`
and the docs survives (blame, the cleanup commits, the Pedro comparison), and the FTC remote makes
every later SDK release a `git merge v12.1`, which the copy approach loses.

Estimated effort: one evening on the laptop for steps 1 to 4, most of it the Gradle conflicts and
the first Gradle 9 sync.
