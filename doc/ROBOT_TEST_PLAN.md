# Robot Test Plan for the off-season changes

Everything on branch `offseason/common-cleanup-2026` was written and compiled without a robot.
This is the list of what to check the next time a robot is available, in the order that gets the
most coverage per minute. Tick the box, write the date and robot next to it, and note anything
that surprised you. Where a test needs a number, the expected value is given.

Last updated 2026-09-07 evening, after the first Skyline session. Each entry names the commit that made the change.

## Before you start

- [ ] Deploy the branch from Android Studio to each Control Hub you test.
- [ ] Have a tape measure, a masking-tape start line, and something to block a wheel (a hand on the
      tire is fine with the robot on a stand).
- [ ] Battery above 12.5 V for any distance measurement; low battery changes RUN_TO_POSITION timing.

## A. Every robot: it still boots and drives (R4, R5, R6)

Run on: every competition robot (GearGirls Bot 1 and Bot 2, P3 Bot 3, Skyline), plus one pushbot
and one StarterBot.

- [x] **A1. Driver Station groups.** The OpMode list shows only that team's group plus `Demo`.
      Pushbot and StarterBot OpModes are under `Demo`. No OpMode named `L01_HelloWorld` or any
      other lesson file is visible. (b72f53d, 1d9f7a7)
- [x] **A2. Config name in telemetry.** Run the `(RUN ME)` TeleOp. The telemetry line
      `robot config` shows the right name for the chassis. (c7cb5ca, 3cc8ac1)
- [x] **A3. It drives.** Forward, strafe, turn all go the direction the driver expects. The gamepad
      layout has not changed, so a driver from last season should notice nothing. (d4b2708 and later)
- [ ] **A4. Alliance handoff.** Run any auto, select BLUE in init, start it, stop it. Start the
      TeleOp. It reports BLUE, the LED shows the blue alliance color, and the Limelight is targeting
      the blue goal. Repeat with RED. (d4b2708)

## B. Encoder moves: distance fix and stoppable loop (a107f3f)

These are the two DriveUtil2026b changes from 2026-09-07. Any mecanum robot on this branch works;
Skyline is the best choice because its live autos use these moves.

- [ ] **B1. Nothing got slower.** Robot on the floor at a tape line. Run `SKYLINE: PARK FAR` (it
      drives `drive_p3(24, 0, 0, 0.5)`). Time it by eye against last season. Expected: the same
      move, no pause at the end. The move now polls every 10 ms instead of spinning; if it looks
      hesitant at the end of the move, say so.
- [x] **B2. Distance is right.** Same run. Measure the travel. Expected about 24 in, within the
      slip you saw last season. Record the number, it feeds section F.
- [x] **B3. Stalled wheel ends the step.** Robot on a stand. Start `SKYLINE: PARK FAR`. During the
      24 in move, hold one wheel. Expected: the step ends on its own after about 4 to 5 s
      (the limit is three times the ideal travel time at 0.5 power, plus 2 s), the motors stop,
      and the auto continues to its next state. Before this change it would spin forever.
- [x] **B4. Stop during a move does not restart the app.** Robot on a stand. Start the same auto,
      press Stop on the Driver Station while the wheels are moving. Expected: the OpMode stops
      within a second and the Robot Controller app does NOT restart. Note: Skyline's autos are
      iterative OpModes, so the time limit is what ends the move there, not the interrupt check.
      Both paths are covered by this test plus B3.
- [x] **B5. Forward by inches drives the right distance.** Run the team's Encoder Move Check
      TeleOp (`GG Encoder Move Check`, `P3 Encoder Move Check`, `SKYLINE: Encoder Move Check`, or
      `Encoder Move Check (StandardBot)` under Common Test). Robot on the floor at a tape line,
      press the left bumper (forward 12 in via `driveRobotDistanceForwardInches`). Expected: 12 in,
      and telemetry says "reached target". Before the fix this drove about 21 in. Right bumper is
      the backward twin, which was always right; the two should match.
- [ ] **B6. The same OpMode covers B1 to B4 without an auto.** Y drives forward 24 in with
      `drive_p3` at the speed shown (triggers change it). Hold a wheel for B3, press Stop for B4;
      the telemetry line `result` says "TIMED OUT or stopped" and how long it took.

## C. LaunchController in Common (f054394, ba10258)

Run on: P3 Bot 3 (`P3: Robot 3 TeleOp (RUN ME)`) and Skyline (`SKYLINE: Teleop (V2 RUN ME)`).
The unit tests cover the state machine; this checks the wiring to real motors and servos.

- [x] **C1. Normal shot.** Spin up, fire. The flywheel reaches speed, the feeder runs for the
      configured time, the shot counter goes up by one, the flywheel behaves as it did before
      (keep spinning or stop, per the robot's old setting).
- [ ] **C2. Repeat fire.** Fire three times in a row. Cooldown between shots feels the same as
      last season on P3. Skyline has cooldown 0, so back-to-back is immediate.
- [ ] **C3. Skyline spin-up timeout is NEW.** Skyline used to wait forever for the flywheel.
      It now feeds after 2 s even if the flywheel has not reached the target. With a fresh battery
      this should never trigger. If a shot feeds early on a weak battery, that is this timeout
      working; decide whether 2 s is the right number for Skyline.
- [ ] **C4. Stall abort (P3 only).** Fire, then block the flywheel briefly during spin-up.
      Expected: the sequence aborts instead of hanging, telemetry says why.

## D. Aiming helpers in Common (6c5e053)

Run on: GearGirls Bot 2, P3 Bot 3, Skyline, each with its `(RUN ME)` TeleOp.

- [x] **D1. Flywheel fallback before the goal is seen.** Cover the Limelight, start the TeleOp,
      read the target-velocity telemetry. Expected: GearGirls 1290, Skyline 1254, P3 its own close
      value. Last season GearGirls and Skyline showed 0 here. This was a deliberate change.
- [x] **D2. Table lookup.** Uncover the Limelight, stand the robot at a known distance from the
      goal (say 60 in). The target velocity matches the row in the team's Constants table for that
      distance, interpolated if between rows.
- [ ] **D3. Lose the goal.** Cover the Limelight again. The target velocity holds the last good
      value, it does not drop to the fallback.
- [x] **D4. Aim LED.** Point the robot left of the goal, right of it, and at it. The LED shows the
      team's three colors in the right places, with the same tolerance as last season.
- [x] **D5. Snap-to-target turns the right way.** Press the aim button with the goal off to the
      right. The robot turns right, and stops turning when centered. Repeat from the left. A sign
      error here shows up immediately as turning away from the goal.

## E. Demo-safe TeleOp defaults (d524e2d, 839f3f2)

Run on: GearGirls Bot 2 first (its default changed), then P3 Bot 3 and Skyline V2 to confirm they
already behaved this way.

- [ ] **E1. Starts in PRESET at CLOSE.** Start the TeleOp with the Limelight covered. Targeting
      mode telemetry says PRESET (or MANUAL), the launcher setpoint is the CLOSE value, and the
      flywheel is idle until the driver spins it up.
- [ ] **E2. Vision is opt-in.** On GearGirls Bot 2, D-pad left cycles the mode to AUTO. Then the
      launcher follows the Limelight distance. Cycling back returns to PRESET.
- [ ] **E3. A full demo shot with the goal not visible** works from PRESET at CLOSE range.

## F. Measurements to take while you have the robot (feeds the next DriveUtil step)

Every chassis still carries the shared default calibration. Take these numbers once per chassis
and write them in that robot's config file (`GGBot2Config`, `P3Bot3Config`, Skyline's config).
Each takes about two minutes with `drive_p3` from a tape line.

| Measurement | How | Feeds |
|---|---|---|
| Ticks per inch | `drive_p3(48, 0, 0, 0.4)`, measure actual travel. New value = 45.33 x 48 / measured | `encoderCountsPerInch` |
| Strafe slip | `drive_p3(0, 24, 0, 0.4)`, measure actual sideways travel. New scale = 1.1 x 24 / measured | `strafeScale` |
| Turning circle | `drive_p3(0, 0, 360, 0.4)`, note the actual rotation. New value = 27.5 x 360 / actual degrees. Expect the robot to turn well short of 360; the default is known to be small | `turnCircumferenceIn` |
| Right-rear correction | Drive straight at 0.5 for 8 ft with `rightRearPowerScale` set to 1.0 in the config; note the drift | Whether 1.15 belongs on this chassis at all |

The team's Encoder Move Check TeleOp has all of these on buttons: Y is forward 24 (use it twice
for 48), B is strafe right 24, D-pad up is the 360 turn (D-pad down is a 180 if space is tight;
scale the arithmetic). After each move the telemetry shows
the wheel ticks and what those ticks mean under the calibration in use, so the arithmetic above is
a tape measure and one division.

## G. Known defects that are NOT fixed yet (expect these to still misbehave)

Listed so a tester does not report them as regressions. They are R11 in the cleanup plan.

- P3 Bot 3: the TeleOp alliance switch never reaches the Limelight (`configureVisionForTeleOp`
  ignores its argument).
- All P3 autos preselect a TeleOp that is `@Disabled`, so the Driver Station will not auto-load
  the TeleOp after auto.
- `driveToTagAsync` (only reachable from a disabled GearGirls auto) marks the drive busy forever.

## H. Tag approach (wired 2026-09-07, never run on a robot)

Run on test2027bot with `Test2027: Teleop (RUN ME)` for H1 and H2 (hold the right bumper to run
the approach; release to cancel) and `Test2027: Tag Approach Test` for H3. Set
`Test2027Constants.TagTest.TAG_ID` to the tag on the wall first. Any other robot on this branch
works the same way once it has a TagApproach call in a TeleOp.

- [x] **H1. Sign check, robot on a stand, tag held in front of the camera.** Hold RB. The
      `TagApproach` telemetry lines show the three errors. Move the tag closer: forward error goes
      down. Move it to the robot's right: right error goes positive. Rotate the tag so the robot
      would have to turn left to face it squarely: yaw error goes positive. A sign that goes the
      other way is fixed by flipping the matching `TAG_*_SIGN` constant at the top of the
      TagSighting section of `common/vision/VisionUtil.java` (or setting `TAG_SQUARE_YAW_OFFSET_DEG` to
      180 if the yaw reads 180 when square). Never change TagApproach for a sign problem.
- [x] **H2. Power directions, still on the stand.** Hold RB. With the tag too far away the wheels
      spin forward; tag to the right, the wheels spin in the strafe-right pattern (front-left and
      rear-right forward); tag needing a left turn, the right side spins forward.
- [x] **H3. First live approach, tag taped to a wall, robot 3 ft away.** `maxPower` is 0.3 in
      `Test2027BotConfig`. Expected: the robot ends the standoff distance (12 in) from the wall,
      centered on the tag, square, and telemetry says DONE with the time it took. Then run it
      again and cover the camera mid-approach: the robot coasts briefly and stops, telemetry says
      GAVE UP: LOST, within about 1.5 s.
- [ ] **H4. Tune.** If it oscillates, lower the `kp*` gains in `Test2027BotConfig`; if it stalls
      short, raise `minPower` a little. Record the final numbers in the config.

## I. test2027bot bring-up (the new-team template, `teams/testteam2027/README.md`)

The README is the checklist. In short: config filled in (device names, motor directions, IMU,
pods), `Test2027: Teleop (RUN ME)` drives, `Test2027: Beginner Auto (START HERE)` runs its eight
moves from a tape mark and ends about where it started facing the same way (forward 24, left 90,
forward 12, right 12, wait, right 90, back 12), `Test2027: Encoder Move Check` measures section F,
`Test2027: Drive Square (Pinpoint)` returns to its mark within an inch with "waypoints that gave
up" reading 0 (it now uses the non-blocking `startDriveTo` / `isBusy` pair, so this also proves
the Intermediate tier), then section H. Then section K for Pedro Pathing (the `Drive Square (Pedro)` auto and the tuning that comes first). In the TeleOp, Back zeroes the position: push the robot
forward by hand and the telemetry X rises, push it left and Y rises, turn it counter-clockwise
and the heading rises; a pod direction that goes the wrong way is fixed in the config. Note anything in the README that a first-time team would have tripped on.

## J. Mechanism Bench Test on a prototype (added 2026-09-07, never run on a robot)

`common/test/MechanismBenchTest` (Disabled, group Common Test) is the first thing to run on any
new mechanism, and the check that the skeleton subsystems in `common/subsystems/` work on real
hardware. Enable it, set its device-name constants to match the Control Hub configuration (the
goBILDA StarterBot names are the defaults), deploy.

| # | Check | Expected |
|---|---|---|
| J1 | Init with only some devices configured | Telemetry lists the missing names under "missing (fine)"; no crash |
| J2 | Right trigger / left trigger | Intake motor and both intake servos spin in / out, proportional; "intake power" tracks the trigger |
| J3 | A | Claw toggles between the OPEN and CLOSED positions; telemetry shows "OPEN 0.300" / "CLOSED 0.700" |
| J4 | Bumpers | Wrist steps A / B / C; left stick X creeps it; the position shown is what to copy into Constants |
| J5 | Left stick Y on the lift | Moves with the stick; releasing the stick HOLDS (does not sag); cannot push past 0 or LIFT_MAX |
| J6 | X / B / Y | Lift goes to DOWN / MID / HIGH, "moving" shows until within tolerance, then clears |
| J7 | Press the home switch by hand while the lift is moving down | Telemetry "HOME", ticks reset to 0, the lift stops pushing into the switch |
| J8 | D-pad left | Flywheel spins to LAUNCH_VELOCITY; "READY" appears near it; press again to stop |
| J9 | D-pad right | One shot: spin-up, feeder runs FEED_TIME_SEC, LaunchController shots fired count goes up |
| J10 | Stop | Everything off |

If J5 sags on release, the motor's RUN_TO_POSITION hold is too weak for the load: raise
`.power(...)` or check the gearing. If J7 never resets, the switch is wired to a DigitalChannel
rather than a TouchSensor; configure it as a REV Touch Sensor.


## K. Pedro Pathing on test2027bot

Section K ran in full on Pedro 2.1.2 on 2026-09-07 and 08 (results table). **Pedro 3 replaced it on
2026-09-15** (`doc/PEDRO_ON_TEST2027.md` sections 6 and 7): the Driver Station `Tuning` menu and
Panels are gone, and the tuning is AutoTune, a page the robot hosts. Pedro 3 has twelve numbers with
no default, so **Pedro is off on this robot until K3 is done**; the RUN ME TeleOp, the tag approach
and the Pinpoint square do not depend on it. Prerequisite as before: section I's push test and
`Drive Square (Pinpoint)` pass.

Setup: deploy, connect a laptop or phone to the robot's WiFi, open `http://192.168.43.1:10158`.
Every procedure ends by printing Java; paste it where the row says, redeploy, continue.

| # | Check | Expected |
|---|---|---|
| K1 | AutoTune > Mecanum Tuner | it spins each motor and asks which way it went, then prints names and directions. They should match config sections 1 and 2 already; if not, the config was wrong, fix it there |
| K2 | AutoTune > Pinpoint Tuner | push forward, push left, rotate 180 counter-clockwise. It prints pod directions and offsets in INCHES. Directions go in config section 4; offsets should agree with the tape-measured +120/-120 mm (4.72 / -4.72 in) within a quarter inch. If a direction is reversed, fix section 4 and redeploy before K3 |
| K3 | AutoTune > Foresight Tuner (48 in clear forward and to the left, robot weighed already) | nine measurements: forward and strafe top speed, forward and strafe coast-down, heading braking, heading kP, forward and strafe braking at several powers, translational gains. Paste the whole printed lambda into config section 6c, redeploy. `Test2027: Teleop (RUN ME)` telemetry now says the Follower owns the Pinpoint instead of "Pedro Pathing OFF" |
| K4 | AutoTune > Tests > Line | 48 in out and back repeatedly with no overshoot |
| K5 | AutoTune > Tests > Curve, Hold, Interpolation Curve | smooth curve; holds a pose against a push; heading changes as programmed |
| K6 | `Test2027: Drive Square (Pedro)` from a tape corner | returns within 1 in (tape-measure the chassis corner; the "off the mark" line is the robot's own opinion), time written down |
| K7 | `Test2027: Drive Square (Pinpoint)` from the same corner, same session | returns within 1 in; time written down. Both run under AUTO_DRIVE_SPEED 0.6 |
| K8 | `Test2027: Teleop (RUN ME)` after K6 | drives as before; telemetry X/Y/heading still track a push (the Follower owns the Pinpoint, TeleOp still drives through moveRobot) |
| K9 | `Test2027: Teleop (Pedro drive)` (group TestTeam2027 Test), same lap twice, X toggles | both drive from the same sticks; X mid-move hands over without a lurch; with Pedro on, releasing the sticks brakes (not coasts) and a straight push stays straight |

If K2 disagrees with section I's push test on the same robot, the bridge (`common/drive/PedroBridge`)
is wrong, not the pods. If the robot drives away from the line in K4, check the config's motor
directions against K1 first; Pedro uses the same four directions as the TeleOp.


## L. AdvantageScope logging on test2027bot (added 2026-09-15, never run on a robot)

Koala-Log writes a `.wpilog` on the Control Hub for every test2027bot run (`common/LogUtil`,
`doc/LOGGING_ADVANTAGESCOPE.md`). Nothing in the robot's behaviour should change; this section
checks that, and that a file comes back readable. Needs a laptop with `adb` and AdvantageScope.

| # | Check | Expected |
|---|---|---|
| L1 | Deploy; run `Test2027: Teleop (RUN ME)` for 30 s, drive around, Stop | drives as in section I; init telemetry has no "Koala-Log could not start" line in the robot log (`adb logcat -s LogUtil`) |
| L2 | `adb pull /sdcard/Android/data/com.qualcomm.ftcrobotcontroller/files/ logs` | one new file named with the time and `Test2027__Teleop__RUN_ME_`, more than a few kB |
| L3 | Open it in AdvantageScope, Line Graph tab, drag in `Drive/X_in`, `Drive/Heading_deg`, `Drive/Velocity/LF` | traces that follow what was driven; the timeline is about 30 s long |
| L4 | 2D Field tab, add `Drive/Pose` | the robot's path is drawn. If it is drawn tiny or off the field, the tab's units are wrong for inches: note what setting fixed it in the doc |
| L5 | `Test2027: Drive Square (Pedro)` (after K3) | `Drive/Pedro/Completion` rises 0 to 1 over one path; `Drive/State` reads FOLLOWING_PATH then IDLE; `Drive/Pose` draws a 24 in square |
| L6 | Loop time with logging on vs `Logging.ENABLED = false` (Driver Station shows it) | no visible difference; Koala-Log writes from its own thread |
| L7 | Stop the OpMode by pressing Stop, then again by killing the app from the Driver Station | the first file opens complete; the second opens up to its last flush (a partial file is expected, not a crash on the next run) |

## M. Launcher on test2027bot (added 2026-09-16, never run on a robot)

The Skyline launcher (two flywheel motors, two feeder servos, the aiming LED) as the common
`Launcher` skeleton on the test2027 config, driven by `Test2027: Teleop (Launcher)`. The hub
configuration is the Skyline one, so the device names are already there. Run on the Skyline chassis
with a goal tag in view for M6.

| # | Check | Expected |
|---|---|---|
| M1 | `Test2027: Teleop (Launcher)`, init, then Start | telemetry "launcher IDLE 0 / 1400 aim NONE"; nothing spins; the LED is off (no goal in view) or shows a colour (goal in view) |
| M2 | D-pad right | wheel spins up; telemetry velocity climbs to about 1400 and says READY. Both wheels spin the same way (if one is backwards, swap its direction in `Test2027BotConfig`) |
| M3 | Hold left trigger | feeder servos run backward, both the same way; release: stop |
| M4 | Hold right trigger with a game piece loaded | one shot every 0.45 s of feeding while held; "fired" count climbs; the wheel stays spinning between shots. If the piece does not reach the wheel, raise FEED_TIME_SEC |
| M5 | A, then right trigger | wheel spins down; the trigger spins it back up and shoots (a shot starts the wheel if needed) |
| M6 | Y with the goal tag in view, walk the robot from 3 ft to 10 ft | telemetry "aim VISION" and the target velocity follows the table; LED green when lined up, orange/blue when the goal is right/left; cover the tag: "aim LAST KNOWN", velocity holds |
| M7 | `Test2027: Teleop (RUN ME)` | unchanged: the launcher exists but no control touches it; nothing spins |

## Results

| Test | Date | Robot | Pass? | Notes |
|---|---|---|---|---|
| A1 | 2026-09-07 | Skyline | pass | Driver Station groups looked fine: Skyline, Skyline Test, TestTeam2027, TestTeam2027 Test, Demo, no stray lesson OpModes. |
| A2, A3 | 2026-09-07 | Skyline | pass | `SKYLINE: Teleop (V2 RUN ME)` after the hub config was corrected and SkylineBotConfig updated (left side reversed, turn negation removed). Forward, strafes, turns as expected. |
| B2 | 2026-09-07 | Skyline chassis, test2027bot config | pass | Encoder Move Check: with 140 mm wheels in the config, two commanded 24 in moves measured 48 in. With the 96 mm default it had driven 35 in. |
| F ticks per inch | 2026-09-07 | Skyline | done | 31.05 (537.7 ticks/rev, 140 mm wheel), confirmed by the 48 in run. |
| F strafe slip | 2026-09-07 | Skyline | done | commanded 24 went 28 at x1.1; scale 0.94, refined on the floor to 0.95. |
| F turning circle | 2026-09-07 | Skyline | done | commanded 360 turned about 135 at 27.5; 73 in, refined with a commanded 90 to 79 in. |
| B3 | 2026-09-07 | Skyline | pass, slow | rear-right wheel held during a 360: the move ended on its own, but only at the 12 s time limit (three times the ideal time for a 79 in turn, plus 2). Encoder stall detection added afterward: no wheel moving for 0.5 s ends the move. Re-tested the same session: passes, the move gives up promptly. |
| B4 | 2026-09-07 | Skyline | pass | Stop pressed mid-move; nothing crashed, no app restart. |
| B5 | 2026-09-07 | Skyline | pass | left bumper, `driveForward(12)`: measured 12 in. |
| C1 | 2026-09-07 | Skyline | pass | launch on the left shoulder button works. |
| D1, D2 | 2026-09-07 | Skyline | pass | flywheel velocity follows the distance table. |
| D4, D5 | 2026-09-07 | Skyline | pass | aim LED and snap-to-target work, with the turn negation removed. |
| Pinpoint | 2026-09-07 | Skyline | unblocked | pods were not touching the field; fixed mechanically during the session, and X/Y began tracking. The push test (forward raises X, left raises Y, a spin leaves X/Y within ~100 mm) and Drive Square are next; both need the +120/-120 mm offsets deployed. |
| Pinpoint push test | 2026-09-07 | Skyline, test2027bot config | pass | `Test2027: Teleop (RUN ME)`: X/Y/heading track a push as expected with the +120/-120 mm offsets. |
| I Drive Square (Pinpoint) | 2026-09-07 | Skyline, test2027bot config | drives the square, hunts | Every corner: large overshoot, then oscillation before settling. Cause was the untuned gains (P 0.019/mm saturates 2 in out); config moved to P3 Bot3's tuned numbers (P 0.0035, tolerance 18 mm, yaw 2.5/0/0.08) afterward. |
| I Drive Square (Pinpoint), re-run | 2026-09-07 | Skyline, test2027bot config | pass | Much better with the retuned gains; the mentor closed the case. Time not written down; take it on the K8 run. |
| K7 Drive Square (Pedro), untuned | 2026-09-07 | Skyline, test2027bot config | drives the square, hunts | Run before K1 to K6, on Pedro's library defaults (81/65 in/s, zero-power accel -41/-60, mass 10.65, max power 1.0 against the Pinpoint square's 0.4). Massive overshoot and oscillation at the corners, as the untuned warning in section K says. Next: the Tuning session K1 to K6 with Panels, then re-run. |
| K2 | 2026-09-07 | Skyline, test2027bot config | done | Forward 81.1 in/s, lateral 67.8 in/s, written as `.velocities(81.1, 67.8)`. |
| K3 | 2026-09-07 | Skyline, test2027bot config | pass | Heading Tuner: twisted by hand, snapped back with the library default heading PIDF; nothing changed in the config. |
| K4 | 2026-09-07 | Skyline, test2027bot config | done | kLinear 0.0962, kQuadratic 0.00165, written as `.predictiveBraking(0.1, 0.0962, 0.00165).centripetalScaling(0)`. Robot weighed the same session: 9.15 kg, written as `.mass(9.15)`. |
| K5 | 2026-09-08 | Skyline, test2027bot config | pass | Tests > Line, 48 in out and back on the tuned numbers: no overshoot. Translational PIDF left at the library default. |
| K6 | 2026-09-08 | Skyline, test2027bot config | pass | Triangle and Circle both looked right. |
| K7, K8 first run | 2026-09-08 | Skyline, test2027bot config | not comparable | Both squares drove. Pedro at its max power 1.0 finished with 24 s left on the 30 s timer (about 6 s); the Pinpoint square at AUTO_DRIVE_SPEED 0.4 finished with 19 s left (about 11 s). Return errors not written down. Different power caps, so no verdict: AUTO_DRIVE_SPEED set to 0.6 and the Pedro square now caps its follower at the same constant; re-run both. |
| K7, K8 second run | 2026-09-08 | Skyline, test2027bot config | pass, both | Both squares under the same 0.6 cap, both ending at heading 0, both back on the tape corner by eye. From the Driver Station screens: Pinpoint square DONE with 20 s left on the 30 s timer (about 10 s), odometry x -0.4 y 0.5 in, heading -2 deg, 0 waypoints gave up, battery 12.16 V. Pedro square 5.0 s at path 98% with 24 s left (about 6 s with settling), off the mark 0.4 in, heading 0, stuck false, battery 11.81 V. Pedro about 40% faster at the same cap. Caveat: Pedro's time barely changed from its 1.0 run, so the 0.6 cap may not be what limits it; tape-measured gaps not taken. |
| K9 | 2026-09-08 | Skyline, test2027bot config | pass | RUN ME TeleOp drives as before after the Pedro square; telemetry still tracks a push. |
| K10 | 2026-09-08 | Skyline, test2027bot config | pass, one fix | Pedro drive felt fine; it coasted when the sticks were released where moveRobot brakes. Fixed afterward: the Pedro TeleOp drive now starts in brake mode and leaving any Pedro mode restores BRAKE on the motors. Not yet re-run. |
| H1 | 2026-09-07 | Skyline | pass | The Limelight robot-space pose is in camera axes (X right, Y down, Z forward) and the rotation about the vertical arrives as pitch; VisionUtil remapped and all four sign constants confirmed against the blue goal tag. Goal tags need their own pipeline (VisionUtil.selectPipelineForTag). |
| H2 | 2026-09-07 | Skyline | pass | first attempt drove backward and left (wrong axes); after the remap it drove toward the goal and centered. |
| H3 | 2026-09-07 | Skyline | pass | from about 7 ft away and 2 ft right of the goal centerline, angled 30 deg: turned to square, drove in, centered, DONE at 60.1 in / 0.3 in / 0.4 deg (standoff 60 because the goal tag sits 31 in above the camera and leaves the frame inside about 50 in). A fair bit of hunting near the target; tolerances widened to 2 in / 3 deg and min power lowered to 0.05 afterward, not yet re-run. |
