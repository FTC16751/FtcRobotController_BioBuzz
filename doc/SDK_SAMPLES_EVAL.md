# SDK 12 samples evaluated against the BIOBUZZ robots

**Written 2026-10-10.** Brings the 2026-09-08 survey (`OFFSEASON_CLEANUP_PLAN.md`, "Next focus: FIRST SDK
samples") up to date for SDK 12.0 and the BIOBUZZ robots (test2027bot, the P3 and GG StarterBots). About
ten samples were read closely; the hardware we do not own leans on the earlier survey. Samples are BSD-3:
lifted math needs a one-line attribution comment, and the pristine copies stay in `FtcRobotController/.../samples`.

## What changed since 09-08

- **Bulk reads are done** (`DriveUtil` constructor sets `BulkCachingMode.AUTO`).
- **SDK 12 added one thing that matters:** AprilTag results can be a cluster (`AprilTagClusterDetection`),
  which is how the Hive cell tags (ids 30-45) arrive. The four AprilTag samples show the cast;
  `ConceptAprilTagEasy` is the shortest.
- **The color-locator samples are DECODE leftovers** (`ARTIFACT_PURPLE` / `ARTIFACT_GREEN`, no BIOBUZZ
  colors). `VISION_TWO_CAMERAS.md` measured color pipelines at 7-11% precision on match footage, so not for finding balls.
- **Still not built from the 09-08 list:** webcam tag class, color/distance sensor class, `driveForwardUntil`.

## Recommended order (each quick to test, none changes a gamepad layout)

| # | Build | From sample | Why | Hardware | State |
|---|---|---|---|---|---|
| 1 | `Rumble` helper: one buzz on a condition's rising edge | `ConceptGamepadRumble` | Feedback only. Triggers exist: `Launcher.isReady()`, `shotDone`, tag acquired. | none | **built 2026-10-10** |
| 2 | IMU fallback in `DriveUtil`: `fieldCentricDrive`, `resetFieldForward` and `turnToHeading` use the Control Hub IMU when there is no Pinpoint | `RobotTeleopMecanumFieldRelativeDrive`, `RobotAutoDriveByGyro_Linear` | StarterBots have no Pinpoint: field-centric was silently robot-centric, `turnToHeading` finished at once as failed. | none | **built 2026-10-10** |
| 3 | `WebcamTags`: a second `Vision` / `TagSighting` from `AprilTagProcessor` `ftcPose` (range/bearing/yaw), with the SDK 12 cluster cast | `ConceptAprilTagEasy`, `RobotAutoDriveToAprilTagOmni` | The Hive line-up half of `VISION_TWO_CAMERAS.md`; `TagApproach` and its tests stay untouched. | any webcam | not built |
| 3a | Webcam setup: exposure/gain against motion blur, camera calibration, decimation | `ConceptAprilTagOptimizeExposure`, `UtilityCameraFrameCapture` | Tag pose from a moving robot is poor without it. | same webcam | not built |
| 4 | `PieceSensor`: presence with hysteresis + color name on a REV Color Sensor V3 (also distance) | `SensorColor`, `SensorREV2mDistance` | `Launcher` feeds on a timer (`feedTimeSec`) and never knows if a ball is there. Feeder sensor = shot confirmed; intake sensor = full, stop and rumble. | one REV V3 | not built |
| 5 | `driveForwardUntil(BooleanSupplier, maxInches)` | `RobotAutoDriveToLine_Linear` | After #4: "drive until the ball is in or the Hive is 6 in away". | needs #4 | not built |

## Other improvements the samples expose

- `calculateAutoAimTurn` (`DriveUtil`) has `kP_TURN` and friends inline; its own comment says they belong in config.
- `StarterBot2027Teleop.handleLauncher` is hand-written (mirrors goBILDA) and bypasses `LaunchController`, so no shot counting or stall abort. Intentional; leave it.
- `ConceptExploringIMUOrientation`: five minutes on each StarterBot to confirm the two orientation enums in `StarterBotConfig` before trusting #2.
- `turnLeft` / `turnRight` are still encoder turns through `turnCircumferenceIn`. Making them IMU-relative is the next step after #2 is proven on a robot.

## Skip

`ConceptBlackboard` (`SharedState` does this), `ConceptGamepadEdgeDetection` (the SDK `*WasPressed()` calls are in use),
`ConceptGamepadTouchpad` / `ConceptTelemetry` / `ConceptSounds*` (demo features), the color-locator and
predominant-color samples, OctoQuad / OTOS / HuskyLens / navX / BNO055 / AndyMark / Modern Robotics / Blinkin / LED stick
(hardware we do not own; the Prism LEDs cover status), `AprilTagMultiPortal` / `SwitchableCameras` (only for two webcams).

## What #1 and #2 changed

- `common/hardware/Rumble` + `RumbleTest` (3). Wired into `StarterBot2027Teleop`: one blip when the held launch button's wheel reaches speed.
- `common/drive/HeadingMath` + `HeadingMathTest` (5): wrap, shortest error, proportional power with floor and cap.
- `DriveUtil`: new `TURNING_IMU` state; gains `IMU_TURN_KP` 0.02, `IMU_TURN_MIN_POWER` 0.06, threshold 1 deg are the sample's start values, **untuned**; 0 is wherever `resetHeading()` was last called.
- Unit tests pass. The `TURNING_IMU` state, the sign of the turn and field-centric on the IMU are not covered by a laptop test.

## Robot checks this creates

- **L1 (new):** on a StarterBot, `turnToHeading(90)` then `turnToHeading(0)` returns to its mark within a few degrees, turning the right way. If it spins away from the target, flip the sign in `DriveUtil` `TURNING_IMU`.
- **L2 (new):** field-centric on a Pinpoint-less robot: after `resetFieldForward()`, push the stick forward, rotate the robot by hand, forward still goes the same way on the field. Needs a TeleOp that calls `fieldCentricDrive`; the StarterBot TeleOp still uses `arcadeDrive`.
- **L3 (new):** StarterBot launch: the controller blips once as the wheel reaches speed, not repeatedly while held.
