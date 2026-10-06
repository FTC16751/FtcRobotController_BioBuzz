# P3: BIOBUZZ's first team for 2027

One folder per robot, because P3 builds at least two and may run both at once. Each robot has its
own package, config, constants and robot class, so they never share a class by accident. Anything
the robots truly share lives in `common/`.

```
teams/p3/
  rex/                  P3's first 2027 robot (named for the Toy Story dinosaur)
    RexConfig.java      WHAT Rex is: EVERY device name and direction, IMU, Pinpoint, Pedro, calibration
    RexConstants.java   HOW Rex operates: drive speeds, intake powers, shooter numbers
    RexRobot.java       the one object every OpMode creates: drive, vision, intake, transfer, launcher
    teleop/             (empty) the driver TeleOp goes here
    auto/               (empty) Pedro autos go here
    test/RexShooterTest "Rex: Shooter Test", the bench test for the shooter prototype
```

A second robot is a sibling folder, `teams/p3/<name>/`, copied from `rex/` and renamed.

## Rex

Mecanum drive, goBILDA Pinpoint, Limelight, Pedro Pathing. Mechanisms, all motors: intake, transfer
(belt rollers), feeder wheel, and one flywheel (a 6000 rpm goBILDA Yellow Jacket; a second goes in
later, see the comment in `RexConfig`).

Device names in the Control Hub configuration: `Front_Left`, `Front_Right`, `Rear_Left`,
`Rear_Right`, `imu`, `odo`, `limelight`, `intake`, `transfer`, `feeder`, `shooter`.

**Everything marked UNVERIFIED in `RexConfig` is a guess.** Motor directions, Pinpoint pod offsets
(zero now), IMU mounting and the calibration numbers all need measuring on the real robot.

### Bring-up order

1. **Shooter test first.** It needs only the `shooter` and `feeder` motors. `Rex: Shooter Test`: D-pad
   up/down changes the target 50 rpm at a time, A starts the wheel, B stops it, bumpers run the
   feeder forward/reverse while held, D-pad left/right set the feeder speed. Flip `SHOOTER_DIR` or
   `FEEDER_DIR` if either goes the wrong way. Retune `PIDF_*` in `RexConstants` if the speed swings.
2. **Drive.** Wire the drive motors and Hub IMU; copy `Test2027EncoderMoveCheck` for Rex and measure
   `encoderCountsPerInch`, `strafeScale`, `turnCircumferenceIn`.
3. **Pinpoint.** Measure the pod offsets into `OdometryConfig`.
4. **Pedro.** Run AutoTune (`http://192.168.43.1:10158`), paste the Foresight lambda into
   `RexConfig`'s `PedroPathingConfig`. Until then the robot reports "Pedro Pathing OFF" and still
   drives.
5. **Limelight**, then a TeleOp and the autos.
