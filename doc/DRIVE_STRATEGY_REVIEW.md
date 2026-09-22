# Drive strategy review: point-to-point PID plus waypoints, and Pedro Pathing

**Written 2026-09-07.** Answers the "Next focus, drive side" brief in `OFFSEASON_CLEANUP_PLAN.md`:
was the `driveTo` + waypoint-table strategy GearGirls and P3 built every auto on last season a
reusable pattern, or one that only worked because two teams tuned around it? It also weighs the
hand-rolled solution against Pedro Pathing (hard rule 6: revisit this season). No code was changed
for this review. Line numbers are from the tree at commit 0bbc947 on `offseason/common-cleanup-2026`.

Files read in full: `teams/geargirls/auto/bot2/GGAutonomous_Score9_v7.java` (432 lines),
`teams/p3/auto/bot3/P3Autonomous_QueueBot3.java` (815), `GGRobotConstants.Waypoints`,
`P3RobotConstants.Bot3_Waypoints`, `GGRobot2`, `P3_Robot3`, `common/DriveUtil` (driveTo,
calculatePID, inBounds, startDriveTo, update), `common/PinpointPIDLoop`, `RobotConfig.PointToPointTuning`,
the four chassis configs, `pedropathing/*`, `teams/p3/auto/P3PedroPathAuto`, the two Coach Pedro
tests, and the existing unit tests. The older autos (bot1, bot2/old, earlyideas) were skimmed for
how the pattern evolved.

**Short version.** The strategy worked: both robots ran scored autos at qualifiers. It worked at the
cost of every waypoint number absorbing both odometry drift and controller error, gains detuned per
chassis rather than the loop fixed, red and blue tables diverging, P3 cutting its shot count for
time on qualifier day, and a two-file, dozen-edit ritual to add one stop. **The pattern is reusable;
the implementation is not.** Keep the sequencing idea, the station-keeping-while-shooting idea, and
`driveTo` as a terminal regulator. Replace the start-relative frame, the field-based step payload,
the missing feedforward, and the per-alliance copy-paste. Decide on Pedro after one timed
comparison on the Skyline chassis, not now.

---

## 1. What the strategy actually is

### The controller

`driveTo(current, target, power, hold)` at `DriveUtil.java:927` is a **stop-and-go point
regulator**, not a path follower.

- Three independent PIDs (X in mm, Y in mm, heading in rad) in the Pinpoint field frame. Only the
  *output* is rotated into the robot frame via `MecanumMixer.fieldToRobot` (`:955`). No feedforward,
  no velocity profile, no line-following between waypoints, no lookahead.
- `power` is a **multiplier on the PID output**, not a cap (`:956`). Halving power halves the whole
  loop's authority, including its ability to break static friction. That is the mechanism behind the
  detuned gains and widened tolerance boxes in section 4.
- X and Y share one gain set, but the chassis strafes about 1.4x slower than it drives (Pedro's own
  measurement in `pedropathing/Constants.java`: xVelocity 86.71 vs yVelocity 60.75 in/s). Diagonal
  moves therefore curve.
- "Done" means inside a **box** (not a radius) on X and Y and inside yaw tolerance (`inBounds`,
  `:1014-1025`), then held for `holdTime` continuously on one shared `GBholdTimer`. With `holdTime = 0`,
  which 10 of 11 GearGirls calls and 10 of 12 P3 calls pass, a single in-box Pinpoint sample
  advances the state machine.
- **The robot comes to a full stop at every waypoint by construction:** `moveRobot(0, 0, 0)` in the
  in-bounds branch (`:933`), and `cancel()` calling `stopRobot()` at the start of every `startDriveTo`
  (`:406`, `:432-439`). There is no pass-through.
- Quirks the autos were tuned against, now pinned by `PinpointPIDLoopTest`: the first call after a
  reset returns 0; an axis inside tolerance resets its PID (`calculatePID`, `:967-970`), so an axis
  hovering on the tolerance edge alternates 0, 0, real output and throws away its integral each time.
  `PinpointPIDLoop.java:11-15` says so: "Quirks that are deliberate, and tested, because the autos
  were tuned against them."
- Every live auto calls `driveTo` directly each loop and never resets the PID between waypoints, and
  gets no timeout or `lastMoveSucceeded`. Only the new `startDriveTo` path (`:392-419`) does that.
  Nothing tests the `driveTo` composition itself (tolerance box, hold semantics, rotation as
  composed, the `update()` state machine); only the extracted pure math is tested.

### The frame

Every waypoint in both teams' tables is relative to **wherever the robot was placed**. All eight
START constants are (0, 0, heading). Not field-absolute. Consequences: red and blue cannot be
derived by mirroring (they were hand-tuned separately and drifted apart), waypoints cannot be shared
between robots or seasons, and an AprilTag pose, which is field-absolute, cannot be used to
relocalize mid-run without a frame conversion nobody wrote.

### Relocalization

Neither team ever corrected odometry from a tag inside an auto. `GGRobot2.resetOdometryToVision`
(`GGRobot2.java:223-237`) has five callers, all TeleOp driver buttons or the Coach Pedro tests. P3
has it only commented out in TeleOp (`P3_Teleop.java:327`). P3's `AIM_AT_TARGET` corrects heading
only, at the shot, and never writes back to the Pinpoint. Every 30-second run was pure dead
reckoning from a hand-placed start tile.

---

## 2. Answers to the brief's five questions

### Q1. Distinct waypoints per team, and how many are the same spot under different names

| Team | Named constants | Used by live auto | Distinct values | Distinct after red/blue mirror | Exact mirror pairs |
|---|---|---|---|---|---|
| GearGirls, `GGRobotConstants.Waypoints:102-183` | 47 (23 red, 24 blue) | 20 | 46 (both FAR starts identical) | about 30 | the whole FAR block (8 pairs) plus FAR_PARK and START_CLOSE; the CLOSE block is not mirrored at all |
| P3, `P3RobotConstants.Bot3_Waypoints:257-341` | 40 (20 red, 20 blue) | about 36 | 36 (four identical starts, two identical FAR parks) | 29 | one (`CLOSE_PARK`); every other pair is a 1-2 in or 5-8 deg hand nudge |

Notes:
- GearGirls' 27 unused constants are the BALL1/BALL2 intermediate stops that v5 made unreachable
  and v7 deleted from the state machine but not from the table. Red has `RED_CLOSE_DRIVE_AWAY` with
  no blue counterpart; blue has `BALL3a` and `END` intermediates that red has commented out.
- GearGirls CLOSE is not a mirror: `RED_CLOSE_PARK` (10, 15.6, -45) vs `BLUE_CLOSE_PARK` (-41, -21, 45)
  are 51 in apart in x. `RED_CLOSE_DRIVE_TO_SCORE` (-20, 27) vs `BLUE_CLOSE_DRIVE_TO_SCORE` (-32, -34).
- P3's gate pair has the **x sign flipped** (`RED_ALIGN_GATE` x = +42, `BLUE_ALIGN_GATE` x = -42)
  where every other red/blue pair keeps x. Either a field asymmetry or a sign bug never hit, because
  only the CLOSE scripts enqueue the gate.
- P3's file holds three waypoint classes. `Waypoints` (line 127) is headed "ROBOT NUMBER 1 (DO NOT
  EDIT)", `Bot2_Waypoints` (172) "ROBOT NUMBER 2 (EDIT)", and `Bot3_Waypoints` (257), the live one,
  has no header. The file's own instructions point at the wrong class.
- `P3RobotConstants:334-340` and `:248-254` are raw Pinpoint telemetry readings pasted as comments.
  That is how waypoints were produced: push the robot, read the screen. The pasted numbers
  (`-8.5, 3.7295, 18.3`) no longer match the live constant (`8.5, -5, 22`).

### Q2. Hold times, powers, step timeouts actually used, and whether steps were skipped

| | GearGirls v7 | P3 QueueBot3 |
|---|---|---|
| `driveTo` calls | 11 | 12 |
| Pose source | `robot.drive.pinpoint.getPosition()` | `robot.drive.getOdoPosition()` |
| Powers | 0.5, 0.7, 0.75, **0.77**, 1.0 | 0.5, 0.6, **0.67**, 0.70, 0.75, 0.80, 0.90 |
| Hold times | 0.0 on 10 calls; 0.25 on PARK | 0.0 on 10 calls; 0.125 / 0.20 / 0.25 on the shoot approach depending on path; 0.25 on PARK |
| Per-step timeout | **none anywhere** | 5.0 s drive and collect, 4.0 s shoot, 2.5 s aim; **none on PARK or SHOOT_PRELOAD** |
| Global timeout | none | 27 s, clears the queue and forces PARK (`:596-604`); PARK itself excluded (`:597`) |
| On step timeout | n/a: the state freezes until FMS stop | skip to the next step, add a telemetry log line |
| Evidence of timeouts in matches | impossible; nothing logs | telemetry log only, not persisted |

The 0.77 (`v7:308`, same `scorePose` as two 0.75 calls) and the single 0.67 (`QueueBot3:227`, RED FAR
spike-2 collect) are per-meet knob turns with no comment. Two P3 speeds (`openGateAlignSpeed` 0.75,
`openGateSpeed` 0.5) are field initializers never reassigned per path.

Git tells the same story. Season-era commit messages are "pre meet commit", "pre meet checking",
"latest gear girls code", "NEW UPDATES FOR MEET3", "NEW UPDATES FOR qualifier". Every waypoint diff
is 1-5 inch nudges, red edited and blue left behind:

- `7246b32` (MEET3): both GearGirls FAR align poses moved 10 in down-field; `BLUE_CLOSE_PARK` moved
  from (-12, -3) to (-41, -21).
- `1619824` and `b2f61c9` (qualifier): `RED_CLOSE_DRIVE_TO_SCORE` moved 5 in in x and y;
  `RED_FAR_DRIVE_TO_SCORE` heading -20 to -22 with blue left at +20.
- `249371a` (P3, qualifier day, 2026-02-07) is the loudest signal. `shootCycle(3)` became
  `shootCycle(1)` in both shoot states: three launch sequences at about 1.55 s each did not fit the
  4 s shoot timeout, so the **shot count was cut rather than the timeout raised**. The same commit
  nudged every RED CLOSE collect pose by +2 in (a systematic offset patched at the waypoints, not the
  gains), moved `BLUE_CLOSE_SHOOTING_POSITION` 20 in, and **renamed BLUE_FAR spike marks 1 and 3**,
  which had been numbered backwards relative to red and only surfaced because the shared
  `buildBlueFarScript` ordering assumed they matched.

No commit message anywhere describes a tuning problem. The record is only in the diffs.

### Q3. Mid-run tag relocalization

Never, in any auto, either team (section 1).

### Q4. What went wrong, per the comments

Almost nothing was written down. Zero hits for "oscillat", "overshoot", "drift", "hack" in either
team's autos or constants. The evidence is in values, not prose:

- `GGBot2Config.java:43-46`: xy P gain 0.0035, **5.4x below** the 0.01905 default; xy tolerance
  32 mm, twice the default; yaw D 0.21 added. `P3Bot3Config.java:43-46`: xy P 0.0035, D 3x smaller,
  I 5x larger, both tolerances loosened. `P3Bot1Config:42-46` is different again. Two chassis pulled
  the same controller in different directions; this is the fingerprint of a loop with no
  feedforward that oscillated and was detuned until it stopped.
- `QueueBot3:659-663`: the lost-tag branch advances to the shot anyway, with
  `//test if we need this. cover up the april tag or turn down the lights in the room`.
- `QueueBot3:95-98`: the comment says "-2.5 degree offset for RED", the constant is `+2.5`, and it
  is added at `:648`. `BLUE_AIM_OFFSET_DEG` is 0.0 so the blue branch is a no-op.
- `QueueBot3:89` and `:198/230/268/300`: `useFeederOnSpikeMarkCollection` is set true in all four
  branches and never read. Bot 2 used it to reverse a feeder; Bot 3 has no feeder.
- `P3_Robot3.java:152`: `// Note: drive.update() is currently a no-op` is false. `DriveUtil.update()`
  calls `pinpoint.update()` and runs the async move state machine; `robot.update()` is the only
  thing refreshing the Pinpoint each loop. A student reading this would move or drop the call.
- `DriveUtil.java:966-1005`: the working notes of whoever fixed the loop are still in the
  source: `// ADD THIS: Early return if within tolerance`, `// ADDED THIS`,
  `// ADD THIS (you'll need to add this to your tuning config)`.
- `old/GGAutonomous_Score9_Bot2.java:533, 591, 640`: `// TODO: Complete remaining states` three times.
  Red Far, Blue Close and Blue Far were never finished in that version.
- `GGAutonomous_PIDTuner.java` (396 lines, in `old/`) exists, so gains were tuned empirically. The
  procedure and results were not written up.

### Q5. What a first-week programmer must touch to add one waypoint

**GearGirls: 12 edits in 2 files.** In `GGRobotConstants.Waypoints`, four constants (one per path)
via the five-positional-argument `Pose2D` constructor, knowing that y and heading flip sign between
alliances except in the CLOSE block where they do not. In `GGAutonomous_Score9_v7`: an enum constant
(`:38-55`), a field (`:62-69`), four assignments in `setPathWaypoints` (`:338-380`), a new `case` in
`runPath` (`:230-331`), and an edit to the previous case's transition. Miss one of the four
assignments and that path NPEs at runtime in a match; miss the `case` and the state machine hangs
silently (no `default`, no timeout, no log line).

**P3: about 14 edits in 2 files.** Up to eight constants in `Bot3_Waypoints`; enum constants
(`:36-53`, likely appended to the ragged last line as everyone before did); two to four fields
(`:64-88`); assignments in all four `start()` branches (`:166-305`); `pathScript.add` in all four
`build*Script` methods (`:324, 376, 419, 469`), where CLOSE and FAR use opposite spike orderings;
two `case` blocks each copying the 8-line timeout idiom and picking the right timeout constant by
hand. The gate fields are already null on both FAR paths today; only script ordering prevents the NPE.

**Both:** they must know the frame origin is the start tile (nothing says so), that tolerance is in
mm while waypoints are in inches, that hold 0 means "one loop in the box", that `driveTo` must be
called every loop and is not a blocking move, that `getNextState()` is what resets the P3 step
timer, and that a mistuned waypoint either hangs (GearGirls) or silently degrades the rest of the
run (P3). Powers and holds come from no documented guidance; the files offer 0.5 through 1.0 as
precedent with no comment on any of them.

### Two corrections to the brief in `OFFSEASON_CLEANUP_PLAN.md`

1. "All three configs still carry the StandardBot defaults, so nobody tuned per chassis" is
   **wrong**. GGBot2, P3Bot3 and P3Bot1 each carry distinct hand-tuned point-to-point gains. Only
   `SkylineBotConfig` and `Test2027BotConfig` carry the defaults. (Drivetrain *calibration*, the
   1.15 / 1.1 / 27.5 numbers, is still shared by GearGirls and P3; that part of the brief is right.)
2. P3's auto is a `Queue<PathState>` of **enum constants**, not a `Queue<Step>`. Target, power, hold
   and timeout are not on the step; they live in about 20 mutable fields assigned in four
   near-identical `start()` branches and read by a 200-line `switch`. The queue is reusable; the
   step payload is not. That distinction is the crux of section 5.
3. Smaller: GearGirls v7 does **not** pick waypoints "with a ternary at each call". v5 introduced
   `setPathWaypoints`, and v7 keeps it; the only ternaries left are the two odometry seeds in
   `start()`. The older autos and the Bot 1 autos do use ternaries per call.

---

## 3. The two state machines, critiqued

### GearGirls v7: one enum, `setPathWaypoints` once, a `switch` in `runPath`

Eleven states in one `PathState` enum (`:38-57`); `loop()` calls `robot.update()`, sets intake power,
then `runPath()`; `setPathWaypoints(alliance, location)` fills eight `Pose2D` fields once from
`start()`. Evolution: `old/Score9` and `_Bot2` had four enums and four `run*Path()` methods with
constants inlined; v4 the same with 60 `driveTo` calls; **v5 did the refactor** to one enum and
`setPathWaypoints`; v7 deleted the unreachable BALL1/BALL2 states and swapped in `ShotSequenceControllerV2`.

Good:
- One enum for all four paths, waypoints resolved once. Best auto shape in the repo, as the plan
  already says.
- The three `SHOOT_*` states call `driveTo` toward `scorePose` **and discard its return** while
  polling the shot sequence (`:241`, `:278`, `:315`). The drive PID station-keeps at 0.5 to 0.7
  power for the whole volley, so a nudge or drift is fought mid-shot. That is a deliberate and
  good interleave, and everything runs in one cooperative `robot.update()` tick.
- Launcher velocity is re-commanded at the top of `DRIVE_TO_SCORE_2/3` so the flywheel spins up
  during the drive back, not after arriving.

Bad:
- **No timeouts of any kind.** A stuck move is a frozen robot until FMS stop, and nothing records
  that it happened. This is the single biggest gap versus P3.
- `robot.intake.setIntakeMotorPower(-1)` runs unconditionally every loop from start to park
  (`:200`), while `GGRobot2.canSafelyCollectMoreBalls()`, `needsToPurgeBeforeCollecting()` and
  `prepareForCollection()` (`GGRobot2:556-602`), written for exactly this with a worked javadoc
  example, are called by nothing.
- `usePurgeMode` defaults **true** (`:33`), so the motif detection and color telemetry in
  `init_loop` are decorative unless a driver presses LB. Confirm this was intentional.
- 27 abandoned waypoint constants; hold 0 everywhere; five powers for the same drive with no note.

### P3 QueueBot3: a queue of enum states with per-step and global timeouts

`Queue<PathState> pathScript` (`:58`), filled by one of four `build*Script()` methods chosen in
`start()`, advanced by `getNextState()` (`:558-568`) which is also the only place `stateTimer` is
reset. `init_loop` selector: X/B alliance, Y/A location, dpad left/right cycles 0-4, bumpers gate
on/off, dpad up/down wait seconds.

Good:
- The queue plus `getNextState()` is the right sequencing primitive, and `selectedCycles`,
  `enableGate` and `waitDuration` make the script data-driven at init.
- Per-step skip and a global force-PARK are the right policies. The timeout log lines are the only
  in-match diagnostics either team had.
- `LaunchController` (R7) already carries the shot state machine, so the auto's shoot step is short.

Bad:
- The step payload lives in fields, so the four `start()` branches (`:166-305`) and four
  `build*Script` methods (`:324-510`) are near-copies. The only real difference is spike order
  (CLOSE 1-2-3, FAR 3-2-1) and whether the gate is enqueued.
- The 8-line timeout idiom is hand-copied nine times and forgotten twice: **`PARK` (`:791-796`) and
  `SHOOT_PRELOAD` (`:672-676`) have no timeout**, and PARK is also excluded from the global timeout.
  A park that never converges drives until FMS stop.
- Budget: a 3-cycle script is 13 steps; four 5 s drive timeouts alone burn 20 of the 27 s. Force-PARK
  is the expected outcome after two stalls, not a rare path.
- `AIM_AT_TARGET` is its own step after `DRIVE_TO_SHOOT_*`, so the robot settles to tolerance twice
  on the same target; and on "tag not visible" it advances to the shot anyway (`:663`).
- `shootCycle` "brakes" with `driveTo(here, here, ...)` (`:577`), a degenerate call that is always
  in bounds and reduces to `moveRobot(0, 0, 0)` plus a discarded hold timer.
- `alignOpenGate` / `openGate` are assigned only in the two CLOSE branches; null on FAR.
- Leftovers from the Bot 2 copy: `"fired %d/3"` while calling `shootCycle(1)`, the dead
  `useFeederOnSpikeMarkCollection`, an unused `P3_Robot` import, `// Can combine logic` twice.
- `robot.turret` is never referenced; the auto aims by turning the chassis.

### Common to both

Both teams converged on: an iterative OpMode, `robot.update()` first in `loop()`, one `driveTo` per
loop, waypoints resolved at start, launcher spun up during the approach, park last. Both duplicate
per alliance and location (GearGirls in one method, P3 in eight). Neither has a `Step` that carries
its own target and timeout. Neither logs to a file. Neither relocalizes. Neither tests anything.

Time is the scarce resource in a 30 s auto, and a stop-at-every-waypoint regulator with no
feedforward spends it twice: once decelerating to zero at each corner, once hunting inside a
widened box. The sequencing idea (a queue of steps with timeouts), the station-keeping-while-
shooting idea, and `driveTo` as a terminal regulator are all worth keeping. The start-relative
frame, the field-based payload, the missing feedforward, and the per-alliance copy-paste are what
to replace.

---

## 4. Pedro Pathing, honestly

### What is in the tree

Pedro 2.0.1 (`build.dependencies.gradle:20`), plus Road Runner 1.0.1 for the demo robots, plus the
hand-rolled loop: three motion stacks in one repo. `pedropathing/` is seven files, 2,003 lines, of
which `Tuning.java` is 1,326 lines of vendor tuner OpModes.

`pedropathing/Constants.java:21-52` is **fully configured for a real chassis**: mass 5, zero-power
acceleration -34.46 forward / -64.23 lateral, max velocity 86.71 / 60.75 in/s, translational PIDF
(0.01905, 0, 0.0035, **0.02**), heading PIDF (0.5, 0, 0.03, **0.01**), motor names and directions,
Pinpoint pods at (38, -168) mm with the forward pod reversed. Those are tuner outputs, not
placeholders; someone ran the suite once. The F terms are exactly the static-friction feedforward
`driveTo` lacks. `GGBot2Config:47-53` copies the measured numbers into a `PedroPathingConfig`;
`P3Bot3Config:47-53` holds round estimates (4.5, -30, -60, 80, 55). Nothing reads either; the only
consumer is `Constants.createFollower(hardwareMap, config)`, whose only call site is the
commented-out `DriveUtil` constructor block.

### What was tried, from git

- 2025-10-19 to 11-09: a student (Olivia) built `AutoPedroPathExample` and `P3PedroPathAuto` in
  Pedro's **absolute 144-inch field frame** (start pose (28.5, 128, 180 deg), score (60, 85, 135 deg)),
  a `BezierLine` to score, `followPath(..., true)` to hold, a `BezierLine` to park.
- 2025-11-30 to 12-09: the mentor's `PedroPathTeleopCoachTest` and `...DriveOnly`, whose telemetry
  converts Pedro's pose with `getAsCoordinateSystem(FTCCoordinates.INSTANCE)` and labels the axes
  "X (Right/Left) / Y (Forward/Back)" against the Pinpoint's X-forward / Y-left. A coordinate-frame
  reconciliation harness.
- 2025-12-12 (`a7e3a47`, "cleaned up driver hub options"): everything Pedro `@Disabled` for
  competition. The teams shipped `driveTo`. Two stock vendor TeleOp examples remain enabled under
  group "Pedro".

Nothing records *why* it failed. The circumstantial evidence: (a) coordinate-frame confusion
between Pedro's absolute frame and the start-relative frame the rest of the code assumed; (b) the
localizer constants in `pedropathing/Constants.java` (38, -168, forward pod reversed) match no live
`RobotConfig` (GG is (0, -203) both forward, P3 is (50, -152) strafe reversed), so the file is
frozen against a chassis state that changed; (c) it competed for the same six weeks as making the
robots shoot at all. The commented blocks in `DriveUtil` (constructor `:149-160`, `arcadeDrive`
`:608-617`, `fieldCentricDrive` `:619-633`, `update` `:922-924`) wire Pedro for **TeleOp
`setTeleOpDrive` only**. No `followPath` was ever sketched inside Common; the autonomous side of a
Pedro integration was never started.

### Complexity, both directions

| | Hand-rolled `driveTo` | Pedro 2.0.1 |
|---|---|---|
| What it is | 3 field-frame PIDs, full stop at each point | Bezier path follower: translational, heading, drive and centripetal vectors |
| Feedforward and profile | none; asymmetric accel limit on output power only | PIDF with F; velocity and decel model from measured zero-power accel; `PathConstraints` |
| Through waypoints | full stop, always | continuous along a `PathChain`; `holdEnd` for terminal hold |
| Per-chassis tuning | by hand, no procedure; four mutually incompatible gain sets today | eight-step tuner OpMode suite, already run once here |
| Coordinate frame | start-relative (0, 0 = where placed) | absolute field, origin at a corner, axes transposed vs the Pinpoint SDK frame |
| Beginner surface | `startDriveTo(24, 12, 90)` + `isBusy()` (hard rule 5) | `PathChain` builder, `followPath`, `Pose`, a second frame to learn |
| Tests | pure math tested; `driveTo` composition untested | vendor |
| Mid-season risk | ours to fix, we understand it | library upgrades and API churn (2.0 broke 1.x) |
| Status | about 20 autos, 3 teams, in use | configured, disabled, zero team OpModes |

### Recommendation

Do not choose globally now. Keep `driveTo` as the default for the first new-season auto: students
already know it, hard rule 5 favors it, and it will be under test on the Skyline chassis first.
Fix the structural problems in section 5 so the *pattern* is reusable regardless of executor. Then
run one honest comparison on Skyline: the same three-waypoint route (start, score pose, spike mark,
back) timed with `startPath` and with a Pedro `PathChain`, both from a tag-relocalized start. If
Pedro is more than about 20% faster and the students can read the path code, adopt it for the
**drive steps** of the Advanced tier and keep everything else (sequencer, waypoint tables,
relocalization, subsystem interleave) unchanged.

The one thing to rule out is the brief's "Pedro for paths, `driveTo` for the last foot" hybrid. It
means two frames and two localizer configurations in one auto, which is the exact confusion that
sank the first attempt. If Pedro runs the drive, Pedro's Pinpoint localizer owns the pose for that
auto, and `holdEnd` is its last foot.

Prerequisites for the Pedro revisit, whichever way it goes: the frame decision written down;
`pedropathing/Constants.java` localizer constants re-measured for the chassis it will run on (or
generated from `RobotConfig.odometry`); the tuner suite re-run on that chassis; the timed comparison.

### Result of the comparison (2026-09-08, Skyline chassis, test2027bot config)

All four prerequisites were met first: frames written down (`PEDRO_ON_TEST2027.md` section 4),
Pedro's constants generated from the config (`common/PedroBridge`), the tuner suite run (test plan
K2 to K6, all pass), and both squares run from one tape mark under the same 0.6 power cap, ending
at heading 0. The route was the 24 in square rather than the three-waypoint route above, so it
tests the drive step, not relocalization.

| | driveTo square (four `startDriveTo`) | Pedro square (one `PathChain`) |
|---|---|---|
| Time | about 10 s | about 6 s |
| Return error, odometry | 0.6 in, heading 2 deg | 0.4 in, heading 0 |
| Gave up / stuck | 0 waypoints | stuck false |
| Battery | 12.16 V | 11.81 V |

Pedro was about 40% faster at the same cap, past the 20% bar above, with the same accuracy, on a
lower battery. Two caveats: the Pedro time was the same as its earlier uncapped run, so the cap may
not be what limits it, and the return errors are the Pinpoint's own opinion, not tape. What the
comparison does not settle is the second half of the bar, whether students can read the path code;
that is the mentor's call after the first new-season auto is written both ways or the Pedro
square is walked through with a student. K9 and K10 (TeleOp unchanged, Pedro TeleOp drive feel)
were still to run.

---

## 5. What "reusable" should look like

Ordered so each item is useful alone. Items 1 to 4 are drive-side and can be built against the
Skyline chassis once the Pinpoint push test and Drive Square pass (`ROBOT_TEST_PLAN.md` section I).
Item 5 is the R9 AutoBase and stays deferred to the first new-season auto as decided; its shape is
fixed here so 1 to 4 fit it. Every item arrives with laptop tests in the R7/R8 pattern (interface in
front of hardware, fake in the test). None changes a live auto's behavior.

### 5.1 Field-absolute waypoints plus start relocalization

Promote `GGRobot2.resetOdometryToVision` into `DriveUtil` as `relocalizeFromTag(vision)`
(Advanced tier), converting the Limelight MegaTag2 field pose into the Pinpoint frame once, in one
place, using the axis facts learned 2026-09-07 and documented in `VisionUtil`. Autos call it in
`init_loop` while the robot sits on the tile looking at the goal tag, so the waypoint table is in
field inches and the start pose is measured, not assumed. This alone makes red and blue mirrorable
and lets the score pose be re-acquired each cycle. Needs `VisionUtil.updateRobotOrientation(heading)`
fed every loop (GearGirls and P3 do; Skyline does not). Test: fake field pose in, expected `Pose2D`
out, including the sign conventions.

### 5.2 One waypoint table, mirrored, resolved once

Per team, `Waypoints.forAlliance(alliance, location)` returns a small `AutoWaypoints` object
(score, park, spikeAlign[], spikeCollect[], gate) built from the BLUE table by
`mirror(pose) = (x, -y, -heading)` in the field-absolute frame, with an explicit per-pose override
map for the handful of spots that genuinely differ. Replaces the four-branch `setPathWaypoints` and
`start()` blocks and the 40 to 47 flat constants. `Pose2D` construction goes through the existing
`DriveUtil.pose(x, y, headingDeg)` so the five-positional-argument constructor leaves team
code. Test: mirror is an involution; every pose in a path lies inside the field.

### 5.3 The step carries its own payload; `startPath` in DriveUtil

A tiny `common/DriveStep` value (target, power, holdSec, timeoutSec, `passThrough`) and
`startPath(List<DriveStep>)` in the Advanced tier, stepping the existing `startDriveTo` per element
inside `update()`. `passThrough` means a wider tolerance box, no hold, and **no motor stop** before
the next target is armed. That gives corner-rounding without a path follower and is the cheap half
of what Pedro buys. `currentStep()`, `stepsTimedOut()` and `lastMoveSucceeded()` are the telemetry.
Per-step timeout defaults from distance via the existing `defaultDriveToTimeoutSec`, so a student
never writes the 8-line timeout idiom again. Tests: a fake pose source drives a 3-step path; a step
that never converges times out and the path advances; a pass-through step does not call `stopRobot`.

### 5.4 Fix the loop, not the waypoints

Add to `PointToPointTuning`: `minPower` (static-friction feedforward, the same idea
`TagApproach.Settings` already has), `strafeGainScale` (so Y does not share X's gain on a chassis
that strafes 1.4x slower), and `powerIsClamp` (make `power` a clamp on the rotated output instead
of a multiplier on the PID; default false so the old autos' tuning is untouched). Re-tune the
Skyline chassis with Drive Square and write the procedure into `ROBOT_TEST_PLAN.md` so each team
measures its own gains instead of inheriting detuned ones. Add the new terms to
`PinpointPIDLoopTest`, and add the first `driveTo` composition test (a scripted `Pose2D` sequence;
assert commanded powers, the box-and-hold done condition, and the `update()` state transitions).

### 5.5 Sequencer shape for R9 (design now, build with the first new auto)

`AutoBase`, an iterative OpMode owning a `Queue<Step>`, where `Step` is an interface
`{ start(); boolean update(); double timeoutSec(); OnTimeout policy(); }` with three implementations:

- `DriveStep`: wraps `startPath` or `startDriveTo`.
- `ActionStep`: a lambda over the robot (shoot, spin up, open gate).
- `ParallelStep`: a drive plus an action, done when the drive is done. GearGirls' station-keeping
  shoot and P3's spin-up-while-driving both become this.

Global timeout forces the PARK step; PARK has its own timeout and is never skipped. The selector
UI (X/B, Y/A, dpad cycles, bumpers) is `AutoSelector` and writes `SharedState.alliance` on start.
Teams write `buildScript(waypoints, cycles)` and nothing else. Skipped and timed-out steps are
written to a file on the Control Hub, not only to telemetry, so "did a step fail in match 3" has an
answer next season.

### Small fixes worth making even though the old autos will not run again

Students copy from these files. `P3_Robot3:152` false "no-op" comment; `QueueBot3` PARK and
SHOOT_PRELOAD timeouts; the aim-offset comment versus its sign; the gate poses null on FAR paths;
the "ADD THIS" working notes in `DriveUtil.calculatePID`. Ask before touching GearGirls'
`usePurgeMode` default.

---

## 6. Decisions for the mentor

1. **Executor.** `driveTo` stays the default for the first new-season auto; Pedro is decided after
   the timed Skyline comparison in section 4. Agree, or decide now? *2026-09-08: the comparison ran;
   Pedro cleared the speed bar (about 40% faster, same accuracy). Open: the readability half.*
2. **Frame.** Field-absolute waypoints with tag relocalization at start (5.1). This changes how
   every waypoint is written; the payoff is mirroring, sharing, and re-acquiring the score pose.
3. **On step timeout:** skip (P3's choice) or retry once? Recommendation: skip, but log to a file.
4. **PARK** gets its own timeout and is never skipped by the global timeout. Recommendation: yes.
5. **GearGirls `usePurgeMode = true` default.** Intentional for the qualifier, or an oversight?
6. **Order.** After the Pinpoint square passes: 5.1 and 5.2 on testteam2027 first (they are pure
   data and one conversion), then 5.3, then 5.4 with the Skyline re-tune, then the Pedro comparison,
   then 5.5 with the first real auto.
