# Pedro Pathing on Decode, and what it replaces in the Pinpoint auto

**Written 2026-10-06.** Companion to `PEDRO_ON_TEST2027.md` (why Pedro failed in 2025, how the Pedro 3 bridge
works) and `DRIVE_STRATEGY_REVIEW.md`. Sources: `DecodeQueueAuto`, `DriveUtil.driveTo`, `PinpointPIDLoop`,
`DecodeConstants.Waypoints`, and the Pedro 3.0.1 `core` jar (read with `javap`). Nothing here has run on the robot.

## 1. What was done

- `DecodeConfig` now passes an empty `PedroPathingConfig` (it was `null`). With it empty, `DriveUtil` tries Pedro,
  `PedroBridge` refuses the untuned config, and `DriveUtil` falls back to the plain Pinpoint and says
  "Pedro Pathing OFF". So **the robot drives exactly as it did until AutoTune's numbers are pasted.** The one
  visible change is a slower init (Pedro's localizer sleeps 500 ms, and the Pinpoint is configured twice).
- `pedropathing/Constants.ACTIVE_CONFIG` now points at `DecodeConfig.create()`, so AutoTune
  (`http://192.168.43.1:10158`) tunes Decode. **It no longer tunes Test2027** until that line is flipped back; it is
  one global, by design.
- Not done, because it cannot be: the Foresight numbers. They are measured per chassis (top speed, braking,
  gains) and Test2027's would be wrong here.

## 2. What the current auto actually does

`DecodeQueueAuto` is a queue of states (`DRIVE_TO_SPIKE_1`, `COLLECT_FROM_SPIKE_1`, `DRIVE_TO_SHOOT_CYCLE_1`, ...)
built from alliance, location, cycle count and gate. There are no paths: every move is
`drive.driveTo(currentPose, waypoint, power, holdSec)` called once per loop until it returns true. Twelve call
sites, one per drive state, plus a vision turn and a "hold" for shooting.

How `driveTo` behaves (`DriveUtil.driveTo`, `PinpointPIDLoop`, gains in `DecodeConfig`):

1. **Three independent PIDs**: x, y and heading, each clamped to +/-1, then rotated into the robot frame and
   multiplied by `power`. P gain is 0.0035 per mm, so an axis saturates at about 11 in of error.
2. **The route is not a straight line.** While both axes are saturated the robot heads diagonally at 45 degrees
   toward the target, then bends as the smaller axis unsaturates. A 60 in by 10 in move is not driven along the
   line between the points. Nothing in the code plans or checks the route.
3. **No deceleration profile.** Acceleration is limited (8/s, so about 0.125 s from stop to full) but braking is
   not, and there is no velocity feedforward. It runs at full speed until about 11 in out and relies on P and a
   very small D (0.00035) to stop. Overshoot is handled by sitting still until back inside tolerance.
4. **Arrival = inside 18 mm (0.71 in) in x AND y AND 0.055 rad (3.2 degrees) in heading, held for `holdSec`**
   (0.0 to 0.25 in this auto). Inside the box it sends zero power, so it is braking, not actively holding.
5. **The "hold while shooting" is not a hold.** `shootCycle` calls `driveTo(currentPose, currentPose, ...)`;
   the error is zero, so it just commands zero power every loop. A shove during a shot is not corrected.
6. **Align-then-collect is two stops.** Each spike is `driveTo(spikeNAlign)` (stop, rotate to +/-90) then
   `driveTo(spikeNCollect)` (a straight run at constant heading). Each move ends in tolerance, so each costs a settle.
7. **Aim is a separate controller.** `AIM_AT_TARGET` bypasses `driveTo` and calls `moveRobot(0, 0, turn)`
   with `turn = 0.027 * (tx + offset)` from the Limelight, until `|tx| < 1` degree. If the tag is not visible it skips.
8. **Safety is in the auto, not the drive:** per-state timeouts (drive 5 s, collect 5 s, shoot 4 s, aim 2.5 s),
   a 27 s global timeout that forces PARK.
9. **Frame:** the Pinpoint zeroes at the start, so every waypoint is relative to wherever the robot was placed
   (`START_*` are all (0, 0, 0)). Red and blue waypoint tables are hand-written and are not exact mirrors, because
   they were tuned separately.

## 3. What replaces each piece

Pedro's unit is a `Path` (lines and curves, with a heading rule), followed by Foresight, which brakes from a
measured model instead of a P term. `DriveUtil.followPath(path, holdEnd)` starts one; `isBusy()` says when it ends.

| Auto step today | With Pedro |
|---|---|
| `DRIVE_TO_SHOOT_PRELOAD` / `_CYCLE_n`: `driveTo` to the shooting pose, speed 0.7-0.75, hold 0.125-0.25 s | `followPath(Paths.line(here, shoot).linear(here, shoot), true)`. Speed becomes `maxPathSpeed` (sec. 4.3). Hold becomes `holdEnd`. |
| `DRIVE_TO_SPIKE_n` + `COLLECT_FROM_SPIKE_n` (two stops) | **One path**: `Paths.path(line(here, align).linear(...), line(align, collect).constant(...))`. Heading turns to +/-90 while moving, no stop at the align point; the intake is already on. Roughly half the states in the script go away. |
| `ALIGN_GATE` + `OPEN_GATE` | One two-segment path, same idea. |
| `SHOOT_*` "hold" (`driveTo(current, current)`, really a brake) | `follower.hold(pose)`: an actively held pose. A real behavior change, and an improvement if the shooting position matters. Uses `DriveUtil.holdPose` (sec. 4.1). |
| `AIM_AT_TARGET` (`moveRobot` P loop on Limelight tx) | Best: `hold(new Pose(x, y, heading - tx_radians - offset))` and let Pedro's heading controller turn, then the same hold covers the shots. Simplest: `drive.cancel()` then keep the existing `moveRobot` loop, then re-hold afterwards. Do the simple one first. |
| `PARK` | `followPath(line, true)`. |
| Start: `pinpoint.setPosition(Pose2D)` | `drive.setPosition(0, 0, 0)`. It goes through the Follower when Pedro is on; writing the Pinpoint directly would leave Pedro's pose stale. |
| Per-state timeouts | `ForesightConfig.timeoutConstraint` for the path, **and keep the `stateTimer` as a backstop that calls `drive.cancel()`**. A timed-out state that just moves on leaves the old path running until the next `followPath`, which cancels it, but a stuck hold never would. |
| 27 s global timeout | Unchanged. `cancel()` first, then PARK. |
| `DecodeConstants.Waypoints` (`Pose2D`, inches and degrees) | Reuse as they are for the first comparison: Pedro takes any frame as long as the start is `setPosition(0,0,0)`. Convert with `PoseFactory.degrees().of(x, y, headingDeg)`. |

## 4. Things that will bite

**4.1 Hold a pose (added 2026-10-07).** `Follower.hold(Pose)` exists in Pedro 3.0.1 but `DriveUtil` only steps the
Follower in its path states, so a bare `getFollower().hold(...)` would hold nothing. `DriveUtil.holdPose(x, y,
headingDeg)` and `holdPose()` (hold where the robot is) now do it: same pattern as `followPath`, state
`HOLDING_POINT`, released by `cancel()` or the next move. To turn in place, hold the same x and y with a new
heading. Compiles; not run on the robot, and not yet used by an OpMode.

**4.2 Pedro and `moveRobot` must never drive at once.** While a path or hold is active, `update()` calls
`follower.update()` every loop and Pedro writes the wheels. The aim step's `moveRobot` would fight it. Every
non-Pedro drive command in the auto (the aim turn, `stopRobot`) needs `drive.cancel()` before it.
`cancel()` restores encoder mode and BRAKE, which is what `moveRobot` expects.

**4.3 `power` and `maxPathSpeed` are not the same number.** `driveTo`'s power scales a clamped PID output.
`Foresight`'s `maxPathSpeed` caps the commanded velocity (Test2027's autos set it with
`((Foresight) follower.algorithm()).config.maxPathSpeed.set(x)`). 0.7 in one does not mean 0.7 in the other. Set it
before each `followPath`, start with the same fractions the auto uses, and compare times.

**4.4 End-of-path tolerance is Foresight's, not ours.** `translationalConstraint`, `headingConstraint`,
`velocityConstraint` and `timeoutConstraint` decide when a path counts as done; they replace 18 mm / 0.055 rad /
`holdSec`. Their defaults were not read here. Set them explicitly to match (about 0.7 in and 0.055 rad) for the
first A/B, then loosen them where a looser stop is fine (a collect run does not need to end inside 0.7 in).

**4.5 Mass changes.** Foresight is tuned on a bare robot. Decode carries up to three game pieces and the flywheel
pulls the battery down. Tune with a typical battery and check the Tests procedure with pieces loaded.

**4.6 TeleOp is untouched.** `DecodeTeleop` drives through `arcadeDrive` and `moveRobot`, and with Pedro idle the
Follower is never stepped. Pedro TeleOp is optional and separate (`Test2027PedroTeleop` is the example).

## 5. Order of work

1. **Hub check.** Deploy; the TeleOp and the Pinpoint auto behave as before; telemetry says "Pedro Pathing OFF".
2. **AutoTune** (`ROBOT_TEST_PLAN.md` section K, same steps): Mecanum Tuner (names and directions must match
   `DecodeConfig`), Pinpoint Tuner (it prints inches: 50 mm = 1.97 in, -152 mm = -5.98 in should come back close),
   Foresight Tuner (48 in clear forward and left). Paste the lambda into `DecodeConfig`'s `PedroPathingConfig`.
   Telemetry then says the Follower owns the Pinpoint.
3. **AutoTune Tests**: Line, Curve, Hold. Push the robot while it holds.
4. **Baseline.** Run today's `Decode: Auto Queue` (Pinpoint) from each start and write down the time to finish
   each cycle. That is the number Pedro has to beat. Use the existing telemetry (`State Time`, `Total Time`).
5. **Port, smallest first**, each step re-measured against the baseline:
   a. `DecodePedroAuto`: copy of the queue auto with the same script; `DRIVE_TO_*` become `followPath`, the aim and
      shot steps unchanged (with `drive.cancel()` before `moveRobot`). Falls back to the Pinpoint path when
      `!hasPedro()`.
   b. Merge align + collect into one path.
   c. Add the `DriveUtil` hold call (4.1), hold during shots, then try the Pedro aim.
   d. Only then consider field coordinates, Visualizer paths and mirroring red to blue. The current tables are not
      exact mirrors, so mirroring throws away tuned differences: do it with real start poses on the field, not before.
6. **Decide.** If Pedro is not clearly faster or more repeatable at the end of 5b, keep the Pinpoint auto: it is
   the one with a competition record. `driveTo` stays in `common/` either way.
