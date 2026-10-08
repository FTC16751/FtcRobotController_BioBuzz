# Decode: P3's 2025-26 DECODE robot (the old "Bot 3")

Brought over from `FtcRobotController_Decode` (`teams/p3`, `P3_Robot3`) onto this repo's structure.
It is its own folder, a sibling of `rex/`, and shares nothing with the 2027 robots except `common/`.

```
teams/p3/decode/
  DecodeConfig.java      WHAT Decode is: every device name and direction, IMU, Pinpoint, calibration
  DecodeConstants.java   HOW Decode operates: velocities, PIDF, shot timings, servo positions, waypoints
  DecodeRobot.java       the one object every OpMode creates
  Turret.java            servo turret with magnetic-switch homing (not used by the TeleOp)
  teleop/DecodeTeleop    "Decode: Teleop (RUN ME)", single driver
  teleop/DecodeDriveTeleop "Decode: Drive Only", drive motors and Pinpoint only
  auto/DecodeQueueAuto   "Decode: Auto Queue", red/blue, close/far, 0-4 cycles, optional gate
  test/DecodeEncoderMoveCheck, test/DecodeTurretCalibration
```

Device names in the Control Hub configuration: `intake`, `indexer`, `left_shooter`, `right_shooter`,
`stopperServo`, `turret`, `turret_limit`, `light`, plus the standard `Front_Left`, `Front_Right`,
`Rear_Left`, `Rear_Right`, `imu`, `odo`, `limelight` (the `RobotConfig.HardwareNames` defaults).

## What changed from the Decode repo

Same hardware and same numbers. The old `P3_Robot3` plus `P3_IntakeUtil`, `P3_LauncherUtil`,
`P3_RubberBandIndexerUtil` became `DecodeRobot` built from `common/subsystems` (Roller, VelocityMotor,
Launcher, PresetServo), and `DriveUtil2026b` is now `common/drive/DriveUtil`.

- **Pedro Pathing is configured but untuned** (`doc/PEDRO_ON_DECODE.md`). The old config had none. Until
  AutoTune has run, DriveUtil reports "Pedro Pathing OFF" and the auto uses `DriveUtil.driveTo` on the Pinpoint.
- **Shots finish after the trigger is released.** The old TeleOp only stepped the shot sequence while
  the trigger was held; `Launcher.update()` now steps it every loop.
- **Intake and indexer zero-power behavior:** the intake used to be set to FLOAT; it now uses the SDK
  default (BRAKE). The indexer is BRAKE as before.
- **Dropped:** the four CR servos (`intakeServo`, `intakeServo2`, `indexerServo`, `indexerServo2`),
  which the old code never ran, and TeleOp's "A also runs the indexer at 25%", which the next line of
  the old loop (`indexer.stop()`) cancelled immediately.
- **Not brought over:** Bot 1 and Bot 2, the old dual-driver TeleOp (Pedro 2 and Panels based, it was
  `@Disabled`), the old TeleOps and the disabled test OpModes.
- The autonomous now sets nothing in `SharedState`, same as before: the TeleOp's alliance is RED unless
  the driver presses a bumper.

Nothing here has been run on the robot since the port. First on the robot: `Decode: Encoder Move
Check`, then the TeleOp (intake, shot, the stopper), then an auto from the close position.
