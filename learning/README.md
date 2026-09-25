# FTC 16751 — Student Learning Curriculum

**Based on**: *"Learn Java for FTC"* by Alan G. Smith (May 2026 edition)
**Team**: FTC 16751 | BioBuzz

---

## Where this lives

This folder sits next to `doc/` and `TeamCode/`, **outside every Gradle module**, so nothing in it
is compiled or shows up on the Driver Station. The lesson `.java` files are templates to copy.
Each student writes their code in their own package under `TeamCode` (see *Per-student package*
below); that is the only place code builds and deploys to a robot. Never move this folder under
`TeamCode/src`: the forty-odd lesson files share class names and would break the build. The book
itself goes in `book/` (see the README there); it is not committed.

## Overview

This curriculum takes a student with **zero programming experience** all the way
to writing competition-ready FTC code. Every lesson maps directly to a chapter in
*"Learn Java for FTC"* — read the book section alongside each lesson for the best
results.

**Total estimated time**: 26–38 hours across 4 phases

---

## Quick Start

1. **Coaches**: create the student's package and progress file (see *Per-student package*).
2. **Students**: start at Lesson 01. Lessons 01, 02, 04 and 05 need no hardware.
3. **Each lesson**: in Android Studio, create a new Java class in your package, paste in the
   lesson template, fix the `package` line, and follow the TODO comments in order.

---

## Curriculum Map

### Phase 1 — Java Fundamentals

> **Hardware needed**: None, except L03 (a gamepad plus the programming board's motor and servo)
> **Time**: ~6–8 hours

| Folder | Lesson | Book Chapter | Key Concepts | Time |
|--------|--------|-------------|--------------|------|
| `01_Hello_World/` | Hello World & OpMode Structure | Ch. 1 | `@TeleOp`, `init()`, `loop()`, `telemetry` | 1–2 hrs |
| `02_Variables_DataTypes/` | Variables & Data Types | Ch. 2 | `int`, `double`, `boolean`, `String`, scope | 1–2 hrs |
| `03_Gamepad_Math/` | Gamepad & Basic Math | Ch. 3 | `gamepad1`, analog/digital inputs, math operators | 1–2 hrs |
| `04_Making_Decisions/` | Making Decisions | Ch. 4 | `if/else`, logical operators, `while`, `for` | 1–2 hrs |
| `05_Classes_Methods/` | Classes, Members & Methods | Ch. 5 | class fields, methods, `private`/`public`, constructors | 2–3 hrs |

### Phase 2 — Hardware Basics

> **Hardware needed**: Programming board (Control Hub + sensors + motor + servo)
> **Time**: ~8–10 hours

| Folder | Lesson | Book Chapter | Key Concepts | Time |
|--------|--------|-------------|--------------|------|
| `06_First_Hardware/` | First Hardware — Touch Sensor | Ch. 6 | Config file, `HardwareMap`, `DigitalChannel`, Mechanism pattern | 2 hrs |
| `07_Motors/` | Motors & Encoders | Ch. 7 | `DcMotor`, `RunMode`, `setPower()`, encoders, `setDirection()` | 2–3 hrs |
| `08_Servos/` | Servos | Ch. 8 | `Servo`, `setPosition()`, `scaleRange()`, direction | 2 hrs |
| `09_Analog_Sensors/` | Analog Sensors | Ch. 9 | `AnalogInput`, `Range.scale()`, voltage to angle | 1–2 hrs |
| `10_Color_Distance_Sensors/` | Color & Distance Sensors | Ch. 10 | `ColorSensor`, `DistanceSensor`, `DistanceUnit` | 2 hrs |
| `11_Gyro_IMU/` | Gyro / IMU | Ch. 11 | `IMU`, hub orientation, `AngleUnit`, yaw/pitch/roll | 2 hrs |

### Phase 3 — Programming Patterns

> **Hardware needed**: Full programming board
> **Time**: ~6–8 hours

| Folder | Lesson | Book Chapter | Key Concepts | Time |
|--------|--------|-------------|--------------|------|
| `12_State_Machines/` | State Machines & Autonomous | Ch. 12 | `enum`, `switch`, `@Autonomous`, timed transitions, `start()` | 3–4 hrs |
| `13_Arrays_Collections/` | Arrays & Collections | Ch. 13 | arrays, for-each, `ArrayList`, `add()`, `get()`, `size()` | 1–2 hrs |
| `14_Inheritance/` | Inheritance & Polymorphism | Ch. 14 | `extends`, `super()`, `abstract`, `@Override`, TestWiring pattern | 2–3 hrs |

### Phase 4 — Advanced FTC

> **Hardware needed**: Complete robot (drive train recommended for L20)
> **Time**: ~6–8 hours

| Folder | Lesson | Book Chapter | Key Concepts | Time |
|--------|--------|-------------|--------------|------|
| `20_Making_Robots_Drive/` | Making Robots Drive | Ch. 20 | Arcade/mecanum formulas, normalization, field-relative | 3–4 hrs |
| `24_Control_Theory_PID/` | Control Theory & PID | Ch. 24 | Bang-bang, P control, PID(f), tuning, integral windup | 3–4 hrs |

---

## Book Chapter Cross-Reference

| Book Chapter | Lesson | Notes |
|-------------|--------|-------|
| Ch. 1 — Introduction | L01 | Hello World |
| Ch. 2 — Variables | L02 | Data types |
| Ch. 3 — Gamepad & Math | L03 | Analog inputs |
| Ch. 4 — Making Decisions | L04 | Control flow |
| Ch. 5 — Classes & Methods | L05 | OOP basics |
| Ch. 6 — First Hardware | L06 | Config + DigitalChannel |
| Ch. 7 — Motors | L07 | DcMotor + encoders |
| Ch. 8 — Servos | L08 | Servo control |
| Ch. 9 — Analog Sensors | L09 | Potentiometer |
| Ch. 10 — Color/Distance | L10 | Sensor fusion |
| Ch. 11 — Gyro/IMU | L11 | Heading |
| Ch. 12 — State Machines | L12 | Autonomous programming |
| Ch. 13 — Arrays | L13 | Collections |
| Ch. 14 — Inheritance | L14 | Polymorphism |
| Ch. 15 — Rumble | — | See QUICK_REFERENCE.md |
| Ch. 16 — Computer Vision | — | See FTC_PATTERNS_REFERENCE.md |
| Ch. 17 — Javadoc | — | Covered in L14 exercises |
| Ch. 20 — Making Robots Drive | L20 | Mecanum + field-relative |
| Ch. 21 — Odometry Hardware | — | Reference team's actual code |
| Ch. 24 — Control Theory/PID | L24 | Bang-bang through full PID |

---

## Lesson Folder Structure

Every lesson folder contains the same 4 files:

```
XX_LessonName/
├── README.md              ← Read this first! Concepts + examples.
├── Template_*.java        ← Your starting code — fill in the TODOs.
├── Complete_Solution.java ← Full solution. Coaches: share after student finishes.
└── Exercises.txt          ← 4–6 progressive challenges (★☆☆☆ to ★★★★)
```

Some lessons (05, 06, 14, 20) have multiple template files — the README explains the order.

---

## Per-student package

Every student gets their own package under `TeamCode`, so two students can both have an
`L01_HelloWorld` class without colliding, and their finished work stays in git next to the team
code:

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/students/
├── jane/
│   ├── PROGRESS.md        ← copied from learning/STUDENT_PROGRESS.md
│   ├── L01_HelloWorld.java
│   └── L02_Variables.java
└── alex/
    └── ...
```

**To onboard a new student** (lowercase first name, no spaces):

```bash
mkdir -p TeamCode/src/main/java/org/firstinspires/ftc/teamcode/students/jane
cp learning/STUDENT_PROGRESS.md TeamCode/src/main/java/org/firstinspires/ftc/teamcode/students/jane/PROGRESS.md
```

**Each lesson, the student**:

1. In Android Studio, right-clicks their `students/<name>` folder → New → Java Class, named
   after the template's class (the README of each lesson says which, e.g. `L01_HelloWorld`).
2. Pastes the whole template over the new file.
3. Changes the first line from `package org.firstinspires.ftc.teamcode;` to
   `package org.firstinspires.ftc.teamcode.students.<name>;` (Android Studio underlines the
   wrong one in red; Alt+Enter → *Move to package* also works).
4. Puts their name in the OpMode annotation so two students' OpModes never share a name on the
   Driver Station, which the SDK refuses: `@TeleOp(name = "L01 Hello World - Jane", group = "Jane")`.
   The `group` keeps each student's OpModes together in the list.

Solutions are pasted the same way when a coach wants one on the robot; their class and OpMode
names already end in `_Solution` / `(Solution)`, so they never clash with the student's copy.

---

## Reference Documents

| File | Purpose |
|------|---------|
| `QUICK_REFERENCE.md` | Printable cheat sheet for active coding sessions |
| `FTC_PATTERNS_REFERENCE.md` | Common code patterns with copy-paste examples |
| `HARDWARE_SETUP_GUIDE.md` | Step-by-step hardware configuration per lesson |
| `COACHING_GUIDE.md` | Session plans, differentiation strategies, troubleshooting |

---

## Notes for Coaches

- **L01, L02, L04, L05** require NO hardware. Students can work from any computer.
  Great for first sessions or when hardware is unavailable.
- The whole curriculum, templates and solutions, compiles against SDK 12.0. Templates whose
  first TODO is "declare the motor" (L07) will not build until that TODO is done; that is
  deliberate.
- See `COACHING_GUIDE.md` for full session plans and differentiation strategies.
