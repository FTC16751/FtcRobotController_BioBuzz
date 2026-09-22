# StarterBot2027: the two goBILDA BIOBUZZ StarterBots (P3 Starterbot, GG Starterbot)

Two goBILDA mecanum StarterBots, identical except on GG the intake gear sits on the other side (so
its motor is reversed) and the launcher needs a gentler velocity PIDF, wired into the shared `common/` code the way `teams/testteam2027` is, with
goBILDA's own TeleOp behaviour. Every device name is in ONE file, `StarterBotConfig`; if a motor
misbehaves, that is the only place to look for what it is called and which way it turns. Drive motors, IMU, Pinpoint and
Limelight use our standard hub names (`Front_Left`, `Front_Right`, `Rear_Left`, `Rear_Right`, `imu`,
`odo`, `limelight`), the same as every other robot we have; only the intake and launcher keep
goBILDA's names. Configure the hub accordingly, not from goBILDA's guide. They exist to drive
at demos and to be the test robots for adding a Pinpoint, a Limelight and Pedro Pathing one step at
a time. Neither has any of that today.

```
teams/starterbot2027/
  StarterBotConfig.java          WHAT the robots are: EVERY device name and direction, IMU mounting,
                                 calibration, and (later) the Pinpoint and Limelight. p3() and gg()
                                 for the two robots; GG differs in its reversed intake and its launcher PIDF.
  StarterBot2027Constants.java   HOW they operate: speeds and launcher velocities.
  StarterBot2027Robot.java       The one object every OpMode creates: drive, intake (Roller),
                                 launcher (Launcher). Takes the config that says which robot.
  teleop/StarterBot2027Teleop.java   goBILDA's controls on our robot class (not an OpMode itself)
  teleop/P3StarterBotTeleop.java     "P3 Starterbot: Teleop (RUN ME)"   } the OpModes: one line
  teleop/GGStarterBotTeleop.java     "GG Starterbot: Teleop (RUN ME)"   } each, picking p3() or gg()
```
goBILDA's own example with our motor names is `samples/BioBuzzStarterbotTeleopMecanum.java`
("Mec BioBuzz StarterBot Teleop"), kept as the reference while the launcher wiring is sorted out.

## Where goBILDA's example went

| goBILDA's `BioBuzzStarterbotTeleopMecanum` | Here |
|---|---|
| four drive motors (`left_front_drive` ...), left side reversed, brake mode, max-normalised mixing | `DriveUtil2026b` with `StarterBotConfig`'s `DrivetrainConfig`, motors named `Front_Left` etc. like every robot of ours; same mixing in `MecanumMixer` |
| `intake` motor + `left_intake_servo` + `right_intake_servo` (right reversed), on the triggers | one `Roller` with two `add` calls and `.brake()`; `robot.intake.setPower(right_trigger - left_trigger)` |
| `launcher` in velocity mode, PIDF 40/0/0/12.5, target 1250, minimum 1200 | a `VelocityMotor` inside `Launcher`; velocities in `StarterBot2027Constants.Launcher`, the PIDF per robot in `StarterBotConfig` (P3 keeps goBILDA's, GG uses 10/0/0/12.6) |
| `windmillServo` (reversed) feeds while the bumper is held and the wheel is above the minimum | the `Roller` inside `Launcher`; `handleLauncher()` in the TeleOp does exactly goBILDA's `launch()` |
| intake gets +0.5 power while feeding | `INTAKE_BOOST_WHILE_FEEDING`, returned from `handleLauncher()` |

Nothing in `common/` changed for this team except `Roller.brake()`.

## Adding the sensors, in the order that pays off

1. **Encoder Move Check first.** Copy `teams/testteam2027/test/Test2027EncoderMoveCheck` with this
   robot class and measure `encoderCountsPerInch`, `strafeScale` and `turnCircumferenceIn`; the
   values in the config are starting guesses. Also confirms the IMU mounting (a commanded 90 reads 90).
2. **Pinpoint.** Fit it, fill in `.pinpoint("odo")` and the `OdometryConfig` in `StarterBotConfig` (if only one robot gets it, give that factory its own values). The push
   test and `Drive Square (Pinpoint)` from the test plan (section I) then apply; copy
   `Test2027DriveSquareAuto` for it.
3. **Limelight.** `.limelight("limelight")` in `StarterBotConfig`; the tag approach (test plan H) and a
   distance table on the launcher (`.table(...)` in the robot class, `aim(robot.vision)` in the
   TeleOp, as `Test2027LauncherTeleop` does) come with it.
4. **Pedro Pathing.** A `PedroPathingConfig` in `StarterBotConfig`, then AutoTune (test plan K).

The Test2027 README has the long version of each step.
