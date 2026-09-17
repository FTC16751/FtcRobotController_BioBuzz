# Data logging for AdvantageScope with Koala-Log

**Added 2026-09-15, not yet run on a robot** (test plan section L). Every test2027bot run writes a
`.wpilog` file on the Control Hub; pull it to a laptop and open it in
[AdvantageScope](https://docs.advantagescope.org), the FRC log viewer, to graph any value against
time or replay the robot's path on a field drawing. This is what lets a student answer "why did the
square overshoot on the third corner" from data instead of memory.

## What was added

| Where | What |
|---|---|
| `build.dependencies.gradle` | two Maven repositories: JitPack (Koala-Log) and maven.brott.dev (FTC Dashboard) |
| `TeamCode/build.gradle` | `KoalaLogger` 1.5.7, its annotation processor, and FTC Dashboard 0.6.0 |
| `common/LogUtil` | our wrapper: `start(hardwareMap)`, `log(name, value)`, `logPose`, `logBattery`, `stop()`. Contains Koala-Log's failures so a logger can never stop a robot |
| `common/DriveUtil2026b.addLog()` | pose, drive state, the four motor powers and velocities, Pedro's progress |
| `teams/testteam2027/Test2027Robot` | starts the log in the constructor, logs drive, vision and battery in `update()`, closes it in `stopAll()` |
| `Test2027Constants.Logging.ENABLED` | the off switch |

[Koala-Log](https://github.com/Koala-Log/Koala-Log) (Ori Coval, MIT) is an FTC library that writes
WPILib's `.wpilog` format. Version 1.5.7 (2026-06-05) is the newest; it names each file after the
active OpMode, and writes from its own thread so the OpMode loop only pays for a queue put. Its
wiki: [add to project](https://github.com/ori-coval/Koala-Log/wiki/1.-How-to-add-to-project),
[use in code](https://github.com/ori-coval/Koala-Log/wiki/2.-how-to-use-in-your-code),
[get the log](https://github.com/ori-coval/Koala-Log/wiki/3.-how-to-get-the-log).

Two things read from its source (JitPack POM and `KoalaLogger/build.gradle`, 2026-09-15) that the
wiki understates:

- **FTC Dashboard is required at runtime**, and Koala-Log's POM does not declare it, so it is in
  `TeamCode/build.gradle` by hand. Dashboard 0.6.0 is the newest and was built for SDK 11; nothing
  in the SDK 12 notes touches what it uses, but it has not been run here. Its page is at
  `http://192.168.43.1:8080` while the robot is on. We removed Dashboard when this repo was made
  (`SDK12_MIGRATION.md`); Koala-Log brings it back as a dependency only. `LogUtil` passes
  `post=false` on every call, so nothing is sent to the Dashboard page and nobody needs to open it.
- The wiki says to call `AutoLogManager.periodic()` from the loop. In 1.5.7 that method is
  package-private and Koala-Log's own thread calls it; there is nothing to call.

## Logging a value

```java
LogUtil.log("Launcher/FlywheelRpm", flywheel.getVelocity());     // double, boolean, long, String, enum
LogUtil.logPose("Drive/Pose", x, y, headingDegrees);              // Pose2d for the field view
motor.setPower(LogUtil.log("Intake/Power", power));               // log returns its argument
```

Names are `Group/Name`; AdvantageScope shows them as a tree. Log once per loop from the robot
class's `update()` (or a subsystem's), never from a blocking loop that does not run `update()`. A
value logged at the same number every loop costs almost nothing in the file: the format stores a
record per call, but the records are small (about 20 bytes each) and the drive's twenty values at
50 Hz come to roughly 1 MB a minute at the outside.

Koala-Log also has `@AutoLog`: annotate a class and the annotation processor generates a
`...AutoLogged` subclass that logs every public field and getter each period. We have not used it;
the explicit calls above say exactly what is logged, which is what a student debugging a subsystem
wants to see in the code. The processor is in the build if a team wants it.

## Getting the log off the robot

Koala-Log ships a Windows-only puller. On a Mac use `adb`, which Android Studio installs
(`~/Library/Android/sdk/platform-tools/adb`).

Over the robot's WiFi (laptop joined to the robot's network):

```bash
adb connect 192.168.43.1:5555
```

Then, over WiFi or a USB-C cable to the Control Hub:

```bash
adb pull /sdcard/Android/data/com.qualcomm.ftcrobotcontroller/files/ ~/Desktop/robot-logs
```

Files are named `2026-09-20_14-30-00_Test2027__Drive_Square__Pedro_.wpilog`: the time the OpMode
was initialised, then the OpMode name with everything but letters, digits, `_` and `-` replaced.
Nothing deletes them; when the folder gets large:

```bash
adb shell rm /sdcard/Android/data/com.qualcomm.ftcrobotcontroller/files/*.wpilog
```

## Reading it in AdvantageScope

Install AdvantageScope from its [releases page](https://github.com/Mechanical-Advantage/AdvantageScope/releases)
(it is not FRC-only; recent FTC fields are built in). File > Open, choose the `.wpilog`.

- **Line Graph**: drag `Drive/X_in`, `Drive/Velocity/LF` and so on onto the left or right axis.
  Drag `Drive/State` onto the discrete band at the bottom to see when a move started and ended.
- **2D Field**: drag `Drive/Pose` onto the robot slot. The pose is in inches with Pedro's frame
  (origin at a field corner, x and y across the 144 in field, heading counter-clockwise from +x).
  The tab has coordinate-system and unit settings; test plan L4 records what makes an inch pose
  draw correctly.
- **Table** shows every value at the cursor time, which is the fastest way to read the numbers at
  the moment a corner overshot.

What is logged today, once per loop:

| Name | Meaning |
|---|---|
| `Drive/X_in`, `Drive/Y_in`, `Drive/Heading_deg`, `Drive/Pose` | Pinpoint position (through Pedro when it owns the Pinpoint) |
| `Drive/State` | IDLE, DRIVING_TO_POINT_PINPOINT, ALIGNING_TO_APRILTAG, FOLLOWING_PATH, HOLDING_POINT, TELEOP_PEDRO |
| `Drive/LastMoveSucceeded` | whether the last start* move arrived or gave up |
| `Drive/Power/LF..RR`, `Drive/Velocity/LF..RR` | each drive motor's commanded power and encoder velocity (ticks/s) |
| `Drive/Pedro/Following`, `Mode`, `Completion`, `RemainingDistance_in` | Pedro's progress along the current path |
| `Vision/TagVisible`, `TagId`, `Forward_in`, `Right_in`, `SquareUp_deg` | the Limelight's tag sighting, as TagApproach sees it |
| `Robot/BatteryVolts` | main battery |

## Open questions (answer on the robot, then update this file)

1. Does FTC Dashboard 0.6.0 start cleanly on SDK 12.0 (a Dashboard failure would show in the robot
   log at boot, not in the OpMode)?
2. What 2D Field setting draws an inch-unit `Drive/Pose` on the FTC field at the right scale (L4)?
3. Is the loop time unchanged with logging on (L6)? If not, drop the velocities first; they are
   the only values that are not already in memory.
