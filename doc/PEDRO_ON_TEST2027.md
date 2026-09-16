# Getting Pedro Pathing working on the Test2027 bot

**Written 2026-09-07.** Companion to `DRIVE_STRATEGY_REVIEW.md`, which recommended a real comparison
between the hand-rolled `driveTo` and Pedro Pathing. This document says why Pedro did not work last
season (from the code and git history, not memory), what the current Pedro release expects, and the
exact steps to get it running on the Test2027 chassis. Sources: our tree at commit 0bbc947, the Pedro
2.0.1 sources in the Gradle cache, the Pedro docs at pedropathing.com/docs/pathing, the Pedro GitHub
releases and Quickstart repo as of today.

## 1. Why it did not work last season

Six causes, each verifiable in the tree. Any one of them would have made Pedro look broken; we had all six.

**1.1 Two objects owned the same Pinpoint.** Both Coach tests (`teams/geargirls/test/PedroPathTeleopCoachTest*.java`)
build `GGRobot`, whose `DriveUtil2026b` opens the I2C device `odo`, applies GearGirls' pod offsets and
directions, and calls `resetPosAndIMU()`. Then they build a Pedro `Follower` whose `PinpointLocalizer`
opens the same `odo`, applies **different** offsets (38, -168 mm) and directions, and both objects call
`update()` on it every loop from different code paths. Whichever configured it last won, and the
gamepad START button did `resetPosAndIMU()` on the shared device without telling the Follower, so
Pedro's pose jumped. The `DriveOnly` variant added a third localizer (`EncoderOdometry` on the drive
motor encoders that Pedro was driving). The commented-out Pedro block in the `DriveUtil2026b`
constructor (`:149-160`) creates the Follower **after** `initOdo()`, so it would have baked the same
conflict into every robot.

**1.2 The library version had localizer bugs.** We are on `com.pedropathing:ftc:2.0.1` (Sep 9, 2025),
which also drags in `core:1.1.0-SNAPSHOT`. Release notes since then: 2.0.3 "resolved heading
direction issues in PinpointLocalizer", 2.0.4 "addressed PinpointLocalizer angular velocity
extraction" (2.0.1's `update()` puts the *heading* into the velocity's heading slot; 2.1.x uses
`getHeadingVelocity`), 2.0.6 "fixed coordinate system conversion". Current is **2.1.2** (May 2026).
Our tests ran during the window when the Pinpoint localizer's heading handling was known-bad.

**1.3 The constants never described the robot under test.** `pedropathing/Constants.java` was edited
by three people for two different robots: Olivia's commits (Nov 4 and 9, 2025) set INCH units,
`forwardPodY(-5)`/`strafePodX(0.5)` then `(-6, 0)`, both pods REVERSED, placeholder PIDFs of
`(.1, 0, 0, 0)`, and at one point two duplicate `.forwardPodY(0).strafePodX(0)` calls that zeroed the
offsets. The GearGirls commits (Nov 11 and 30) changed it to MM, `(38, -168)`, forward REVERSED /
strafe FORWARD, and real PIDFs and velocities. Meanwhile `GGBot2Config` says GearGirls' Pinpoint is at
`(0, -203)` both FORWARD. The static motor directions (LF/LR FORWARD, RF/RR REVERSE) are the mirror of
Test2027's (LF/LR REVERSE, RF/RR FORWARD). Every Pedro OpMode in the tree uses this static block.

**1.4 The RobotConfig bridge swaps the pod offsets.** `Constants.createFollower(hardwareMap, config)`
does `.forwardPodY(config.odometry.pinpointOffsetY_mm)` and `.strafePodX(config.odometry.pinpointOffsetX_mm)`.
Pedro's `PinpointLocalizer` passes `forwardPodY` straight into goBILDA's `setOffsets` as the **first**
argument, which is the X (forward) pod's sideways offset, left positive; `strafePodX` becomes the
second argument, the Y (strafe) pod's forward offset. `DriveUtil2026b.configurePinpoint()` calls
`setOffsets(pinpointOffsetX_mm, pinpointOffsetY_mm)`. So the bridge must be
`.forwardPodY(pinpointOffsetX_mm).strafePodX(pinpointOffsetY_mm)`; today it is the reverse. For
Test2027 (`OdometryConfig(120, -120)`: forward pod 120 mm left, strafe pod 120 mm behind) the bridge
would tell Pedro the forward pod is 120 mm right and the strafe pod 120 mm ahead. The only caller of
the bridge, the disabled `P3_Robot3_TeleOp`, would have had the same problem.

**1.5 The coordinate frames were misread.** Pedro's localizer frame is X forward, Y left, heading
counter-clockwise, the same as the Pinpoint at heading zero. The Coach test telemetry labelled Pedro's
X as "Right/Left" and its Y as "Forward/Back", which is wrong, and then converted to `FTCCoordinates`
(a 90 degree rotation plus a 72 in shift), which made the two pose readouts disagree by construction.
Separately, the example autos used Pedro's absolute field frame (start pose (28.5, 128, 180 deg))
while the robot was placed "wherever" with no `setStartingPose` to match, so the first path started
from a pose the robot was not at.

**1.6 The test OpModes had their own bugs.** `AutoPedroPathExample` jumps from state 0 to the terminal
state 7 (scores the preload and stops). `P3PedroPathAuto` re-follows `pathToScore` on arrival and
loops `MOVE_TO_PARK` forever with no terminal state. The Coach tests pass un-negated sticks and use the
`robotCentric` flag backwards relative to their own labels. The `Drivetrain` wrapper ignores its
`isAutoOrienting` argument and initialises `position` from `Constants.startingPos` (9, 9) instead of
the pose it was given. `Tuning` was `@Disabled`, so the tuner suite that would have caught 1.3 was
never on the Driver Station. Nothing in `teams/testteam2027/` references Pedro at all, and
`Test2027BotConfig.pedroPathing` is `null`, so the bridge throws on that robot today.

## 2. What Pedro 2.1.2 expects (from the docs and Quickstart, September 2026)

- **Dependencies** (Quickstart `build.dependencies.gradle`): `com.pedropathing:ftc:2.1.2`,
  `com.pedropathing:telemetry:1.0.0`, `com.bylazar:fullpanels:1.0.12`, repositories `mavenCentral()`
  and `https://mymaven.bylazar.com/releases`. Pedro publishes to Maven Central since 2.0.2, so
  `maven.pedropathing.com` is no longer needed. `compileSdk 34` (we have it). Quickstart is on FTC SDK
  11.1.0; we are on 11.0.0. The 2026-27 season SDK arrives this month and Pedro will follow it.
- **Constants shape**: unchanged builder API. `FollowerConstants` (mass in kg, zero-power
  accelerations, translational/heading/drive PIDFs with optional secondaries, `centripetalScaling`,
  and new `predictiveBrakingCoefficients(kP, kLinear, kQuadratic)` which enables predictive braking
  when set). `MecanumConstants` (names, directions, `xVelocity`, `yVelocity`). `PinpointConstants`
  (`forwardPodY`, `strafePodX`, `distanceUnit`, `hardwareMapName`, `encoderResolution` or
  `customEncoderResolution`, pod directions, optional `yawScalar`). `PathConstraints` still has the
  4-argument form `(tValue, timeout, brakingStrength, brakingStart)` we use; the 8-argument form adds
  velocity/translational/heading end constraints and the Bezier search limit.
- **Tuning order** (docs "Tuning"): set constants; **Localization Test** (push forward, x increases;
  push left, y increases; fix a pod direction if not); **Velocity Tuners** (48 in forward and 48 in
  left at full power, output goes to `xVelocity`/`yVelocity`); **Heading Tuner** (turn the robot by
  hand, it corrects back; adjust P in Panels); then **one** drive algorithm: **Predictive braking**
  (run `PredictiveBrakingTuner` for kLinear and kQuadratic, hand-tune kP in 0.05 to 0.3 on the Line
  test, set `centripetalScaling(0)`, tValue constraint 0.95 to 0.97; about 15 percent faster, only kP
  is manual) or **PIDF** (zero-power acceleration tuners, translational and drive PIDF tuners,
  centripetal tuner). Finish with the **Line, Triangle, Circle** tests. 2.1 also added an automatic
  **Offsets Tuner** under Localization. Tuning is done live in Panels at `192.168.43.1:8001`, then the
  numbers are copied into `Constants`.
- **Frame**: 144 x 144 in, origin at the bottom-left corner as drawn in Panels, X to the right, Y up,
  heading 0 along +X, counter-clockwise positive. `Pose.mirror()` exists for red/blue. `FTCCoordinates`
  and `InvertedFTCCoordinates` (added 2.0.2 for the DECODE field) convert to and from the FTC
  standard frame; `PoseConverter.pose2DToPose` converts an SDK `Pose2D`.
- **TeleOp**: `startTeleopDrive()` (or `(true)` for brake mode) in `start()`, then
  `setTeleOpDrive(-left_y, -left_x, -right_x, robotCentric)` each loop after `follower.update()`.
  Switch back with `startTeleopDrive()` when `!follower.isBusy()`.
- **Auto**: build every `Path`/`PathChain` in `init`, `setStartingPose`, `followPath(chain, holdEnd)`,
  advance on `!follower.isBusy()`, `follower.update()` first in every loop. `holdEnd = true` keeps the
  robot at the end pose while a subsystem works; `automaticHoldEnd` defaults true in 2.1.

## 3. The plan for Test2027

Ordered; each step has a check. Steps 1 to 4 are laptop work, 5 onward need the robot. Java 8,
no change to any live team OpMode, hard rule 6 honoured (the commented DriveUtil blocks stay until
step 4 replaces them with the working design).

**Step 0. Prerequisites on the robot (already owed in `ROBOT_TEST_PLAN.md` section I).** Pinpoint push
test with the +120/-120 mm offsets: forward raises X, left raises Y, a spin leaves X/Y within about
100 mm. Confirm the Control Hub configuration names `Front_Left`, `Front_Right`, `Rear_Left`,
`Rear_Right`, `odo`, `limelight`. Pedro cannot be tuned on a Pinpoint that is not yet proven.

**Step 1. Upgrade the library.** `build.dependencies.gradle`: `ftc:2.0.1` to `2.1.2`, `telemetry:0.0.6`
to `1.0.0`, `fullpanels:1.0.6` to `1.0.12`; drop the `maven.pedropathing.com` repository (Maven
Central). Replace `pedropathing/Tuning.java` with the 2.1.2 Quickstart copy (ours is the June 2025
2.0 version and lacks the predictive braking and offsets tuners), keep it in our package, remove
`@Disabled` while tuning. Compile; fix Panels import changes in `P3_Robot3_TeleOp` if any. If Pedro
2.1.2 needs SDK 11.1.0, bump the eight `org.firstinspires.ftc:*:11.0.0` lines together. Check:
`:TeamCode:assembleDebug` passes and the 102 unit tests still run.

**Step 2. Fix the bridge and make it the only path.** In `pedropathing/Constants.java`:
`.forwardPodY(config.odometry.pinpointOffsetX_mm).strafePodX(config.odometry.pinpointOffsetY_mm)`
(the swap in 1.4); keep MM and `goBILDA_4_BAR_POD`. Make the static `createFollower(hardwareMap)` that
`Tuning` and every example call delegate to the bridge with one `ACTIVE_CONFIG` (Test2027BotConfig
for now), and have the static `followerConstants`/`driveConstants`/`localizerConstants` be filled
**from** that config in a static initialiser so Panels live-tuning still edits the objects the
Follower was built from. Result: the tuner tunes the robot in the RobotConfig, never a stale static
block. Check: a unit test builds the `PinpointConstants` from `Test2027BotConfig` and asserts
`forwardPodY == 120`, `strafePodX == -120`, name `odo`, both FORWARD.

**Step 3. Give Test2027 a `PedroPathingConfig`.** Replace the `null` in `Test2027BotConfig` with
starting values: mass measured on a scale (kg), the FollowerConstants defaults for everything else
(the tuner will overwrite them), `xVelocity`/`yVelocity` left at defaults until step 6,
`PathConstraints(0.99, 100, 1, 1)`. Add named setters or a builder to `RobotConfig.PedroPathingConfig`
(it is a ten-positional-argument constructor today, the same trap `PointToPointTuning` already fixed)
and drop the two fields nothing reads (`trackWidth`, `lateralMultiplier`). Add
`predictiveBraking(kP, kLinear, kQuadratic)` and `centripetalScaling` so the config can express the
2.1 drive algorithm.

**Step 4. One owner of the Pinpoint.** The design the commented blocks were reaching for, done in the
right order: when `config.pedroPathing != null`, `DriveUtil2026b` builds the Follower **first** and
takes the Pinpoint from it (`((PinpointLocalizer) follower.getLocalizer()).getPinpoint()`), skipping
`initOdo()`/`configurePinpoint()`/`resetPosAndIMU()`. Pedro configures the device once; `driveTo`,
`getX()`, telemetry and the tag approach read the same object. `update()` calls `follower.update()`
(which does `pinpoint.update()`) instead of `pinpoint.update()`. The two motion sources are made
exclusive by `DriveState`: a new `FOLLOWING_PATH` state during which the Pinpoint PID never writes
motors, and `startDriveTo`/`driveToTagAsync` call `follower.breakFollowing()` first. `resetPosition()`
goes through `follower.setPose(...)` so Pedro and the Pinpoint agree. The RUN ME TeleOp stays on
`moveRobot` (hard rule: gamepad feel does not change); Pedro's `setTeleOpDrive` is wired only in the
separate comparison OpMode (section 5, question 3). Check: `DriveUtil2026b` has no unit test (it needs
a HardwareMap), so the state-machine exclusivity is checked on the robot: `Test2027Teleop` telemetry
X/Y/heading still track the push test after the change (K9), and K10 for the toggle.

**Step 5. Localization Test.** Run `Tuning` > Localization > Localization Test with Panels open. Forward
raises x, left raises y, counter-clockwise raises heading, a spin in place leaves x/y still. A wrong
pod direction is fixed in `Test2027BotConfig.odometry` (not in Constants) and the config re-deployed.
This is the same check as Step 0 seen through Pedro; if they disagree, the bridge is still wrong.

**Step 6. Velocity and heading.** Forward Velocity Tuner and Lateral Velocity Tuner (need 48 in clear
each way; take the printed velocity into `driveMaxVelo`/`strafeMaxVelo`). Heading Tuner: turn the robot
by hand, raise P until it corrects briskly without oscillation, copy `headingPIDF` back to the config.

**Step 7. Drive algorithm: predictive braking first.** Run `PredictiveBrakingTuner`, copy kLinear and
kQuadratic, set kP 0.1, `centripetalScaling(0)`, tValue 0.97. Run the Line test; raise kP toward 0.3
until it jitters, back off. If the stops are too harsh for the students' taste, fall back to the PIDF
path (zero-power acceleration tuners, translational and drive PIDF tuners, centripetal) later; the
config can hold both. Then Triangle and Circle. Record every number in `Test2027BotConfig` with the
date, and the procedure in `ROBOT_TEST_PLAN.md` section I.

**Step 8. The first Pedro auto and the comparison.** `teams/testteam2027/auto/Test2027PedroSquareAuto`:
the same 24 in square as `Test2027DriveSquareAuto`, as one `PathChain` of four `BezierLine`s with
constant heading, `setStartingPose(new Pose(72, 72, 0))` (field centre so Panels draws it), advance
on `!isBusy()`, telemetry of pose and elapsed time. Then the timed three-waypoint route from the
review doc, once with `startDriveTo` and once with a `PathChain`, both from the same start mark.
Pass: Pedro returns to the mark within 1 in, and the time difference is written down. That number is
the input to the executor decision.

**Step 9. Hygiene while we are in there.** `@Disabled` on `ExampleTeleOp` and `ExampleTeleOp_george`
(both enabled today, both require a `limelight`, both pass un-negated sticks); fix or delete
`Drivetrain.java`'s ignored argument and wrong initial pose; fix the two example-auto state bugs or
mark them as samples. The Coach tests and `P3PedroPathAuto` stay `@Disabled` for R3.

### What was built on the laptop, 2026-09-07 (steps 1 to 4 and 9; nothing has run on a robot yet)

- `build.dependencies.gradle`: Pedro `ftc` 2.0.1 to **2.1.2**, `telemetry` 0.0.6 to 1.0.0, `fullpanels` 1.0.6
  to 1.0.12; the `maven.pedropathing.com` repository removed (Maven Central). FTC SDK left at 11.0.0.
- `pedropathing/Tuning.java` replaced by the 2.1.2 Quickstart copy (1,792 lines; adds the Offsets
  Tuner and the Predictive Braking Tuner), package fixed, Driver Station group `Pedro`, **not
  `@Disabled`** so it shows up for the tuning session.
- New `common/PedroBridge`: builds `FollowerConstants`, `MecanumConstants`, `PinpointConstants`, a
  `PinpointLocalizer` and a `Follower` from a `RobotConfig`. The pod-offset swap from section 1.4 is
  fixed here (`forwardPodY` gets `pinpointOffsetX_mm`, `strafePodX` gets `pinpointOffsetY_mm`) and
  pinned by `PedroBridgeTest` (six tests). `common/` no longer imports the `pedropathing` package.
- `pedropathing/Constants.java` rewritten: `ACTIVE_CONFIG = Test2027BotConfig.create()`, the static
  constants Panels edits are built from it, `createFollower(hardwareMap)` (what Tuning calls) uses
  them, `createFollower(hardwareMap, config)` delegates to the bridge. No hand-typed numbers remain.
- `RobotConfig.PedroPathingConfig`: no-arg constructor plus named setters (`mass`, `zeroPowerAccel`,
  `translationalPIDF`, `headingPIDF`, `velocities`, `pathConstraints`, `predictiveBraking`,
  `centripetalScaling`), defaults equal to Pedro's library defaults; the ten-argument constructor is
  deprecated. `Test2027BotConfig` now carries a `PedroPathingConfig` (untuned). `GGBot2Config`,
  `P3Bot3Config`, `P3Bot1Config` have theirs set to **null** with the old numbers kept in a comment:
  a non-null block now makes DriveUtil2026b build a Follower, and those robots must not change in
  demo season. Consequence: the disabled `P3_Robot3_TeleOp`, which builds its own Follower from
  `P3Bot3Config`, would throw at init if re-enabled until Bot 3 gets a real `PedroPathingConfig`.
- `common/DriveUtil2026b`: when the config has Pedro, the constructor builds the localizer and the
  Follower first and borrows the Pinpoint from the localizer (one owner, step 4). New
  `followPath(chain[, holdEnd])`, `hasPedro()`, `getFollower()`, states `FOLLOWING_PATH` and
  `HOLDING_POINT`; `update()` steps the Follower only while one of those is active (after a path
  ends, every further `follower.update()` re-zeroes the motors, so it must not run under TeleOp),
  `pinpoint.update()` otherwise; `cancel()` breaks a path or releases a hold; `isBusy()` is false
  while holding; `setPosition` goes through `follower.setPose`; motors run `RUN_WITHOUT_ENCODER`
  during a path (Pedro's tuning assumes plain power) and go back to `RUN_USING_ENCODER` after. The
  commented Pedro blocks and the `Constants` import are gone; TeleOp driving is unchanged.
- `teams/testteam2027/auto/Test2027PedroSquareAuto`: the 24 in square as one four-leg `PathChain`
  from (72, 72, 0), telemetry shows distance off the mark, elapsed time, path completion, stuck flag.
  README step 6d documents the vocabulary. `ROBOT_TEST_PLAN.md` section K is the robot checklist.
- `ExampleTeleOp` and `ExampleTeleOp_george` are `@Disabled`. The Coach tests, `P3PedroPathAuto`,
  `AutoPedroPathExample` and `Drivetrain.java` are untouched (R3 scope).
- Committed as a35e8cb the same evening. Added after the commit: `Test2027PedroTeleop`, the Pedro
  drive toggle from section 5 question 3, and the `TELEOP_PEDRO` state in DriveUtil2026b behind it.


## 4. Frame conventions to write down once

| | Pinpoint / `driveTo` (today) | Pedro localizer | Pedro field |
|---|---|---|---|
| Origin | where the robot was placed | `setStartingPose` | bottom-left corner in Panels, 144 x 144 in |
| X | forward at heading 0 | forward at heading 0 | to the right |
| Y | left | left | up |
| Heading | CCW, radians (SDK `Pose2D`) | CCW, radians (`Pose`) | 0 along +X, CCW |
| Units | inches in `Pose2D`, mm inside `driveTo` | inches | inches |
| Mirror | by hand | `Pose.mirror()` | `Pose.mirror()` |

At heading 0 the first two columns are the same axes, so a Pinpoint `Pose2D` becomes a Pedro `Pose`
with `PoseConverter.pose2DToPose(pose2d, PedroCoordinates.INSTANCE)` plus the start offset, nothing
else. The Coach tests' "X (Right/Left)" labels were the misunderstanding, not the library.

## 5. Open questions for the mentor

Asked 2026-09-07 with the laptop work; answered the same evening after the commit (a35e8cb). The
mentor can overturn any of these; question 4 is the one only the mentor can settle.

1. **Bump to FTC SDK 11.1.0 with the Pedro upgrade, or wait for the 2026-27 SDK this month and do
   both at once?** Wait, then bump once. As of 2026-09-07 the newest SDK is v11.2.1 (2026-07-31), a
   tooling-only fix on v11.2 (2026-07-15, the 2025-26 offseason release); no 2026-27 kickoff SDK is
   published yet (last year's v11.0 came out on 2025-09-06). The Pedro upgrade does not need it:
   2.1.2 compiles and `PedroBridgeTest` passes on 11.0.0. What 11.1 would give us: Pinpoint v2
   support in the goBILDA driver, gamepad triggers as booleans with edge detection, Limelight
   pipeline upload. What 11.2 costs: Gradle 9.1 and AGP 8.13.2, so Android Studio Narwhal 3
   Feature Drop or later on every laptop (this one runs 2025.3, which is newer; the students'
   laptops need checking). Plan: stay on 11.0.0 until the 2026-27 SDK appears, then one bump
   straight to it on a branch, Gradle move included, and re-run `PedroBridgeTest` plus test plan K.
   The Pedro commit and the SDK commit stay separate so either can be reverted alone.
2. **Predictive braking first or PIDF first?** Predictive braking, as planned. It is two numbers
   from an automatic tuner (Tuning > Automatic > Predictive Braking Tuner, K4) against three
   hand-tuned PIDF sets in the Manual folder. Touch the PIDFs only if the Line test (K5) still
   overshoots with predictive braking on. The order in section 3 and in test plan K is unchanged.
3. **A Pedro drive toggle for Test2027's TeleOp?** Built, as a second OpMode:
   `teams/testteam2027/teleop/Test2027PedroTeleop` (`Test2027: Teleop (Pedro drive)`, Driver
   Station group TestTeam2027 Test). Same sticks as RUN ME; X toggles between Pedro's
   `setTeleOpDrive` (robot-centric) and `moveRobot`. DriveUtil2026b carries it as one more state,
   `TELEOP_PEDRO`, behind `startPedroTeleopDrive()`, `pedroTeleopDrive(strafe, drive, turn, speed)`
   and `isPedroTeleopDrive()`; `cancel()` hands the wheels back to `moveRobot`, `isBusy()` is
   false in that state, and `update()` steps the Follower there exactly as it does for a path.
   `Test2027Teleop` (RUN ME) is untouched. Robot check: test plan K10. Delete the OpMode once
   the comparison is decided.
4. **Which chassis is Test2027 physically: the Skyline frame measured 2026-09-07, or a separate
   robot?** Not answerable from the laptop. Every 2026-09-07 row in the test plan's results table
   reads "Skyline chassis, test2027bot config", so this plan assumes Test2027 is the Skyline frame
   under the test2027bot hub configuration and the +120/-120 mm offsets stand. If it is a separate
   robot, measure its pods and put the numbers in `Test2027BotConfig`'s `OdometryConfig` before
   K1; nothing else in this plan changes.


## 6. Pedro 3.0.0 (released 2026-09-10), read on 2026-09-11

What changed, from the release notes, the new Quickstart and the 3.0.0 jars on Maven Central:

- **Artifacts.** `com.pedropathing:revhub:3.0.0` (replaces `ftc`) plus `com.pedropathing:tuning:1.0.0`
  for AutoTune; `core` comes with it. Panels and the `telemetry` library are no longer part of tuning.
  The Quickstart builds on FTC SDK 11.2.1.
- **Rewrite, not an upgrade.** Everything `common/PedroBridge` is written against is gone:
  `FollowerBuilder`, `FollowerConstants`, `MecanumConstants`, `PinpointConstants`, `PathChain`,
  `BezierLine`, `setConstantHeadingInterpolation`, `startTeleopDrive` / `setTeleOpDrive`. New shapes:
  `new Follower(localizer, drivetrain, new Foresight(config))` with `PinpointConfig`, `MecanumConfig`
  and `ForesightConfig` filled by lambdas; `follower.follow(path)`, `hold(pose)`, `manual(forward,
  strafe, turn)` for TeleOp, `stop()`, `mode()`, `withLogger(...)` for a per-update `FollowerLog`.
- **Paths API.** `Paths.line(a, b).constant(0)`, `Paths.curve(a, control, b).tangent()`,
  `Paths.through(poses...)`, `Paths.path(p1, p2, ...)` to chain. The square becomes four one-line
  paths in one `Paths.path(...)`.
- **PoseFactory.** `PoseFactory.degrees().of(x, y, headingDeg)`; `factory.mirrorY(72)` returns a
  factory whose poses are mirrored, so one waypoint table serves both alliances. This is the
  library-side answer to `DRIVE_STRATEGY_REVIEW.md` section 5.2 (one table, mirrored, resolved once).
- **Foresight** replaces predictive braking (36% faster Line test on the authors' robot). Its config
  is a different model (per-axis linear and quadratic brake matrices, coast and brake controllers,
  natural decelerations, piecewise translational gains), so our K4 numbers do not carry over; the
  K2 velocities do (`maxAchievableForwardVelocity` / `maxAchievableStrafeVelocity`).
- **AutoTune** is a web page the robot hosts at `192.168.43.1:10158`, driven from a browser: motor
  directions by spinning each motor and asking, Pinpoint pod directions and offsets by push forward,
  push left, rotate 180, then nine Foresight procedures. Each step prints the Java to paste into
  Constants. It replaces the Driver Station d-pad menu and the Panels page, and the "Forward Tuner"
  trap in K1 does not exist in it.

Plan: keep the 2.1.2 result (section K, the comparison) as it stands. Migrate on one branch together
with the 2026-27 SDK bump, after 3.0.x has had a few weeks of patches (2.0.1 to 2.0.4 taught that
lesson), and before the first real auto is written so students learn one API. Laptop work is a
rewrite of `PedroBridge` and its test, the Pedro parts of DriveUtil2026b, the square auto, the
TeleOp toggle, `pedropathing/Constants` and `Tuning` (the Quickstart's `procedures/` folder copied
verbatim); robot work is one AutoTune session, about an hour, then K5 to K8 again.

