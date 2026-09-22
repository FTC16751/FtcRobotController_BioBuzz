# TestTeam2027 / test2027bot: how a new team gets up and running

This folder is a complete, minimal team: one robot, one TeleOp, two autos, one bench-test OpMode.
It exists to test the shared `common/` code on a spare chassis, and to be copied by the next real
team. Nothing in `common/` was changed to make it work, which is the point of the structure.

Time for a new team, once the robot is wired: about one meeting to drive, a second to measure and
tune. The steps below are in the order to do them.

## What is in the folder

```
teams/testteam2027/
  Test2027BotConfig.java      WHAT the robot is: device names, motor directions, IMU mounting,
                              Pinpoint pods, calibration, tuning. Edit when the robot is rewired.
  Test2027Constants.java      HOW it operates: speeds, waypoints, which tag to drive to. Edit when
                              the game changes or the drivers change their minds.
  Test2027Robot.java          The one object every OpMode creates. Owns drive and vision, updates
                              them each loop. Add game subsystems here as public fields.
  teleop/Test2027Teleop.java  Drive with the sticks; hold RB to drive to the test tag.
  auto/Test2027BeginnerAuto.java      A first auto: eight lines of driveForward / turnLeft / ...
                              This is the file a new programmer copies first.
  auto/Test2027DriveSquareAuto.java   Pinpoint waypoints with startDriveTo / isBusy. The next step up.
  auto/Test2027TagApproachAuto.java   Non-blocking AprilTag approach test.
  test/Test2027EncoderMoveCheck.java  Encoder moves on buttons, for calibration measurements.
  (Every run also writes an AdvantageScope log; see doc/LOGGING_ADVANTAGESCOPE.md and the
  Logging section of the constants file to turn it off.)
  README.md                   This file.
```

Two rules from the cleanup plan that this layout follows: the config file says what the robot IS,
the constants file says how it OPERATES, and they are never merged. And `common/` never names a
specific robot; each team's three-line `EncoderMoveCheck` subclass is what that looks like.

## Step 1. Copy and rename (10 minutes)

1. Copy this folder to `teams/<yourteam>/`.
2. Replace the package line in every file: `teams.testteam2027` becomes `teams.<yourteam>`.
   Android Studio: right-click the folder, Refactor, Rename, and it rewrites the imports.
3. Rename the classes from `Test2027...` to your team's prefix. Android Studio Refactor, Rename
   on each class name updates every use.
4. Change the `group = "TestTeam2027"` in the four `@TeleOp` / `@Autonomous` annotations to your
   team name. That group is what the Driver Station shows.
5. Change `preselectTeleOp` in the two autos to your TeleOp's name.

Build from the terminal to catch a missed rename before it reaches a robot:

```bash
cd /Users/georgemitchom/StudioProjects/FTC17651/FtcRobotController_Decode && JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :TeamCode:assembleDebug
```

## Step 2. Fill in the config (20 minutes, at the robot)

Open your `...BotConfig.java`. Each numbered comment in it matches these:

1. **Device names.** On the Driver Station, open Configure Robot and the active configuration.
   Copy the four drive motor names, the IMU name, and the Pinpoint and Limelight names exactly.
   No Pinpoint? `.pinpoint(null)`. No Limelight? `.limelight(null)`. The shared code checks.
   goBILDA 2026-27 mecanum StarterBot names: `left_front_drive`, `right_front_drive`,
   `left_back_drive`, `right_back_drive`; its intake is `intake`, `left_intake_servo`,
   `right_intake_servo`.
2. **Motor directions.** Leave the defaults, deploy, run the TeleOp, push the left stick forward.
   Any wheel that turns backward gets `REVERSE` in `DrivetrainConfig`. Then try strafe and turn.
3. **IMU mounting.** Which way the REV logo faces (UP, DOWN, FORWARD, ...) and which way the USB
   ports face. Wrong values make field-centric driving and the heading telemetry drift.
4. **Pinpoint pods.** Offsets in mm from the robot's center of rotation to each pod, and the
   count direction. Check by pushing the robot forward by hand with the TeleOp in init: the
   telemetry X should rise. Push it left: Y should rise. Turn it counter-clockwise: heading rises.
   Flip the pod direction that goes the wrong way.
5. **Calibration.** Leave the defaults for now; Step 4 measures them.
6. **Tuning.** Leave the defaults for now; Step 5 tunes them.

## Step 3. Drive it (first meeting)

Deploy. On the Driver Station the team's group shows four OpModes. Run `Teleop (RUN ME)`:

- Left stick drives and strafes, right stick turns, left bumper is slow mode.
- The telemetry footer shows the robot config name, the Pinpoint position, and whether a tag is
  visible. If the config name is wrong you are running another team's robot class.
- Back resets the Pinpoint to zero.

If the robot drives, Step 2 is done. Commit.

## Step 4. Measure the calibration (second meeting, tape measure)

Run `Encoder Move Check` and follow `doc/ROBOT_TEST_PLAN.md` section F. Four numbers, four
moves, two minutes each: ticks per inch, strafe slip, turning circle, and whether the right-rear
wheel needs a power correction. Write them into `Calibration` in your config. Now `drive_p3`
and the `driveRobotDistance*` commands move the distance they are told.

## Step 5. Prove the Pinpoint and tune driveTo (second meeting, floor space)

Run `Drive Square (Pinpoint)` from a tape mark. The robot drives a 24 in square and ends facing
the way it started, so the same chassis corner should land within an inch of the tape corner;
measure it with a tape, the telemetry only reports where the robot thinks it is. If it drifts, the pod offsets or
directions in Step 2 are off. If it oscillates or crawls into each corner, the point-to-point
gains in `PointToPointTuning` need work: lower `xyGains` P if it oscillates, raise it if it
crawls, and only then touch D. The telemetry line "steps that timed out" should read 0.

## Step 6. Prove the AprilTag approach (a tag taped to a wall)

Set `TagTest.TAG_ID` in your constants to the tag on the wall. Follow `doc/ROBOT_TEST_PLAN.md`
section H, in order: the sign check on a stand (H1), the wheel directions (H2), then
`Tag Approach Test` from 3 ft away (H3). The TeleOp's right bumper does the same approach
interactively, which is the quickest way to repeat H1 while someone adjusts the four sign
constants at the top of the TagSighting section of `common/vision/VisionUtil.java`. Those constants
are shared by every robot, so once one robot has them right, every robot does.

## Step 6b. Write the first auto (the beginner vocabulary)

Copy `auto/Test2027BeginnerAuto.java`, rename it, and change the numbers. The whole vocabulary:

| Command | What it does |
|---|---|
| `driveForward(inches)`, `driveBackward(inches)` | straight, then stop |
| `strafeLeft(inches)`, `strafeRight(inches)` | slide sideways without turning |
| `turnLeft(degrees)`, `turnRight(degrees)` | spin in place |
| `waitSeconds(seconds)` | pause (a launcher spinning up, a servo finishing) |
| `stop()` | all wheels off |
| `driveToTag(robot.vision, tagId, inches)` | drive to a spot in front of an AprilTag |

Each one finishes before the next line runs, gives up after a few seconds if a wheel is stuck,
and returns true if it got there. Add a second number for a different speed:
`driveForward(24, 0.3)`. The default speeds are `Drive.AUTO_DRIVE_SPEED` and
`Drive.AUTO_TURN_SPEED` in your constants. Distances are only as accurate as Step 4's calibration.

## Step 6c. The second auto (the intermediate vocabulary)

When the robot has a Pinpoint and the square in Step 5 comes back to its mark, the autos stop
waiting and start telling: `auto/Test2027DriveSquareAuto.java` is the pattern. The robot knows
where it is, and a move is started, then polled, while other things run in the same loop.

| Command | What it does |
|---|---|
| `getX()`, `getY()`, `getHeadingDegrees()` | where the robot is: inches forward, inches left, degrees counter-clockwise |
| `setPosition(x, y, heading)`, `resetPosition()` | tell the odometry where it is (the start tile) |
| `move(forward, right, turnDegrees)` | one blocking encoder move with all three parts |
| `startDriveTo(x, y, heading)` then `isBusy()` | drive to a field position without blocking |
| `turnToHeading(degrees)` | face a field heading, non-blocking |
| `startDriveToTag(robot.vision, tagId, inches)` then `isBusy()` | the tag approach, non-blocking |
| `cancel()` | abandon whatever move is running |
| `lastMoveSucceeded()` | did the last start* move arrive, or give up |
| `resetFieldForward()` | for field-centric TeleOp: this way is forward |

Power and hold time come from the defaults in your constants; add a power argument to
`startDriveTo` for one move. Every start* move has a time limit that scales with the distance.

## Step 6d. The third auto (Pedro Pathing paths)

`auto/Test2027PedroSquareAuto.java` drives the same square as ONE Pedro Pathing path of four lines,
without stopping at the corners. It works because the config has a `PedroPathingConfig` (section 6c
of the config file) with AutoTune's Foresight numbers in it: DriveUtil then builds a Pedro
Follower for the robot, and the Follower owns the Pinpoint. Without those numbers Pedro is off,
the telemetry says so, and everything else still drives. Tune first: open `http://192.168.43.1:10158`
on the robot's WiFi and run the procedures in the order test plan section K gives, pasting what each
one prints into the config.

| Command | What it does |
|---|---|
| `hasPedro()` | true if the config has a complete PedroPathingConfig |
| `PoseFactory.degrees().of(x, y, headingDeg)` | a Pedro pose: inches; x forward, y left, counter-clockwise, same as the Pinpoint |
| `Paths.line(a, b).constant(a)` | a straight leg from pose a to pose b holding a's heading; `.tangent()` faces along the leg, `.linear(a, b)` turns evenly |
| `Paths.path(leg1, leg2, ...)` | join legs into one path |
| `followPath(path)` then `isBusy()` | follow it without blocking; the robot stops at the end |
| `followPath(path, true)` | same, then hold the last pose (for shooting) until `cancel()` or the next move |
| `cancel()` | abandon the path, or release the hold |
| `startPedroTeleopDrive()`, then `pedroTeleopDrive(strafe, drive, turn, speed)` each loop | let Pedro drive the wheels from the sticks (same arguments as `arcadeDrive`); `cancel()` goes back to `moveRobot`. Comparison only, in `teleop/Test2027PedroTeleop`, never in the RUN ME TeleOp |

Never build a second Follower or open the Pinpoint yourself in an OpMode that has a robot object;
`getFollower()` is the one to use. Two owners of one Pinpoint is what broke Pedro in 2025.


## Step 7. Add the game

- **First, on the bench:** enable `common/test/MechanismBenchTest`, edit its device-name constants
  to match the Control Hub, and drive the prototype from gamepad 1. Its telemetry shows the live
  servo positions and motor ticks; write those numbers down, they become your Constants.
- **A subsystem is one line per device** using the skeletons in `common/subsystems/` (Roller,
  PresetServo, Claw, PresetMotor, VelocityMotor; `package-info.java` there says which one you want).
  Device names go in your BotConfig, positions and speeds in your Constants. The goBILDA intake,
  for example:
  ```java
  // Test2027BotConfig.java                       what the robot IS
  public static final String INTAKE = "intake", INTAKE_LEFT = "left_intake_servo", INTAKE_RIGHT = "right_intake_servo";

  // Test2027Robot.java                           a public field, built in the constructor
  public final Roller intake;
  intake = new Roller(hardwareMap, Test2027BotConfig.INTAKE, DcMotorSimple.Direction.FORWARD)
               .add(Test2027BotConfig.INTAKE_LEFT,  DcMotorSimple.Direction.FORWARD)
               .add(Test2027BotConfig.INTAKE_RIGHT, DcMotorSimple.Direction.REVERSE);
  // and in update():  intake.update();      in stopAll():  intake.stop();

  // Test2027Teleop.java                          one line in loop()
  robot.intake.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
  ```
  A claw is `new Claw(hardwareMap, CLAW, OPEN, CLOSED)` and `if (gamepad1.aWasPressed()) robot.claw.toggle();`.
  A lift is `new PresetMotor(hardwareMap, LIFT, FORWARD).preset("HIGH", 2200).limits(0, 2300)` and
  `robot.lift.goTo("HIGH")`. Only write your own class in `teams/<yourteam>/subsystems/` when a
  mechanism does something none of the five do.
- **A launcher** is the sixth skeleton, `Launcher`: a `VelocityMotor` for the wheel and a `Roller`
  for the feeder, with the spin-up / feed / cooldown sequence and distance-table aiming built in.
  `Test2027Robot` builds one (names in the BotConfig, numbers in `Test2027Constants.Launcher`), and
  `Test2027: Teleop (Launcher)` drives it: `spinUp(CLOSE)`, `aim(robot.vision)`,
  `shoot(gamepad1.right_trigger > 0.5)`. In an auto: `shoot()` then wait for `shotDone()`.
- Waypoints: add them to your constants, one `Pose2D` each, and sequence them the way
  `Drive Square` does. That is exactly how the GearGirls and P3 autos work.
- A tag to drive to: `robot.drive.driveToTagAsync(robot.vision, id, standoffInches, holdSec)`,
  then wait on `robot.drive.isBusy()` while the launcher spins up in the same loop.


## Things that will bite

- `arcadeDrive`'s arguments are `(strafe, drive, turn, unused, speed)`. The stick's forward is
  negative, so drive is `-left_stick_y`. The TeleOp already does this; keep it.
- `@Disabled` hides an OpMode from the Driver Station; it does not delete it.
- Every blocking encoder move now has a time limit. If a wheel is held, the move ends on its own
  after a few seconds and returns false. The Pinpoint `driveTo` never blocks.
- The Driver Station only shows the groups of the OpModes in the APK. Four teams' OpModes are
  always all present; the group name is what keeps them apart.
