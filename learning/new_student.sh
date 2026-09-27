#!/bin/bash
# Sets up (or tops up) a student's package: students/<name>/PROGRESS.md and one folder per lesson
# holding that lesson's templates, with the package line, OpMode name and group already set.
# Every OpMode starts @Disabled; the student deletes that line when starting the lesson.
#
# Never overwrites: files that already exist are left alone, so re-run it after adding a lesson.
#
#   learning/new_student.sh Jane          # run from the repo root
#   learning/new_student.sh GraceJr       # folder "gracejr", OpModes "... - GraceJr"
set -euo pipefail

display="${1:?usage: learning/new_student.sh <Name>}"
name=$(echo "$display" | tr '[:upper:]' '[:lower:]')
[[ "$name" =~ ^[a-z][a-z0-9]*$ ]] || { echo "Name must be letters and digits, starting with a letter"; exit 1; }

root="$(cd "$(dirname "$0")/.." && pwd)"
dir="$root/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/students/$name"
mkdir -p "$dir"

if [ ! -f "$dir/PROGRESS.md" ]; then
    sed -e "s/\[Replace with student's name\]/$display/" \
        -e "s/\[Replace with start date\]/$(date +%Y-%m-%d)/" \
        "$root/learning/STUDENT_PROGRESS.md" > "$dir/PROGRESS.md"
fi

for lesson in "$root"/learning/Lesson_Templates/*/; do
    base=$(basename "$lesson")                                  # 01_Hello_World
    folder="l$(echo "$base" | tr '[:upper:]' '[:lower:]')"      # l01_hello_world
    mkdir -p "$dir/$folder"
    for t in "$lesson"Template*.java; do
        [ -e "$t" ] || continue
        class=$(grep -m1 -oE 'public (abstract )?class [A-Za-z0-9_]+' "$t" | awk '{print $NF}')
        out="$dir/$folder/$class.java"
        [ -e "$out" ] && continue
        sed -E \
            -e "s/^package org\.firstinspires\.ftc\.teamcode;/package org.firstinspires.ftc.teamcode.students.$name.$folder;/" \
            -e "s/^@(TeleOp|Autonomous)\(name = \"([^\"]*)\"\)/@com.qualcomm.robotcore.eventloop.opmode.Disabled   \/\/ DELETE this line when you start the lesson\\
@\1(name = \"\2 - $display\", group = \"$display\")/" \
            "$t" > "$out"
    done
done
echo "students/$name ready"
