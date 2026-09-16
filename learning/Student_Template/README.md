# Student Folder Template

This folder is the **template** for each new student.

## Coach Instructions

When a new student joins, copy this entire folder into `Student_Code/`:

```bash
cp -r Student_Template Student_Code/FirstName_LastName
```

Then:
1. Open `STUDENT_PROGRESS.md` and fill in the student's name, date, and coach
2. Have the student start with Lesson 01 (`Lesson_Templates/01_Hello_World/`)
3. The student copies their completed `.java` files into the matching `L01/`, `L02/` etc. folders here

## Folder Contents

| Folder | Put what here |
|--------|--------------|
| `L01/` | Completed `L01_HelloWorld.java` |
| `L02/` | Completed `L02_Variables.java` |
| `L03/` | Completed gamepad/math OpMode |
| `L04/` | Completed decisions OpMode |
| `L05/` | Completed `RobotStatus.java` + `L05_Classes.java` |
| `L06/` | Completed `L06_ProgrammingBoard.java` + `L06_TouchSensorOpMode.java` |
| `L07/` | Motor/encoder OpMode |
| `L08/` | Servo OpMode |
| `L09/` | Potentiometer OpMode |
| `L10/` | Color/distance OpMode |
| `L11/` | IMU/gyro OpMode |
| `L12/` | State machine autonomous |
| `L13/` | Arrays OpMode |
| `L14/` | All 4 inheritance files |
| `L20/` | Drive mechanism + OpMode |
| `L24/` | PID control OpMode |

## Note

The `.java` files placed here are for archival/review — they are NOT compiled by
Android Studio directly (they're outside the `src/` folder).
Students write and test their code inside the `teamcode` package, then copy finished
versions here for record-keeping.
