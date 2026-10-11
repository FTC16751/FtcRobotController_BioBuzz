# Running our autos in the virtual_robot simulator

[virtual_robot](https://github.com/Beta8397/virtual_robot) is a 2D FTC simulator. This folder runs our
unmodified OpModes in it, so path and sequencing logic can be checked without a robot.

```
sim/run_sim.sh
```

In the window: **Configurations -> MecDynamic Bot**, pick the OpMode (for example
`Decode: Park Red (Pedro)`), **INIT**, **START**. The robot jumps to the start pose the auto declares,
then drives. Tick **Show path** to draw its trail. Stop with the window's STOP button, then INIT again
to repeat; Ctrl+C in the terminal quits.

First run downloads a JDK (about 310 MB) and the simulator (about 70 MB) and builds them, a few minutes;
after that it starts in seconds. Everything lands in `sim/.cache` (git-ignored). Needs macOS (tested on
Intel only), `git`, `python3`, and one `./gradlew` build done so the Koala-Log jar is in `~/.gradle`.

| Command | Does |
|---|---|
| `sim/run_sim.sh` | Build what is missing, open the simulator. Your OpModes recompile on every run. |
| `sim/run_sim.sh --build-only` | Compile and stop; handy to check that an OpMode still compiles. |
| `sim/run_sim.sh --rebuild` | Throw away the cached simulator and rebuild it. |
| `SIM_JDK=/path/to/jdk17 sim/run_sim.sh` | Use a JDK 17 with JavaFX you already have (Liberica "Full"), skipping the download. |

## Adding an OpMode

Add its source path to `sim/opmodes.txt` (relative to `TeamCode/src/main/java`) and run again. Its
dependencies compile automatically. If INIT shows no telemetry, the OpMode thread died: read the terminal
where you ran `run_sim.sh`; the stack trace is there (typically a device the sim does not have, or an
SDK class missing from `sim/stubs`).

## What the simulator can and cannot tell you

Checked 2026-10-10: `DecodePedroParkAuto` ended at the park pose (9, 106.1, 180 degrees) in 5.3 s on two
runs in a row, with Pedro's pose matching the robot on screen; `DecodeRedAudienceAuto` (shoot, flower,
score table, park) ran start to finish.

It tests: path order and sequencing, state machines, start-pose and heading handling, and whether the
follower reaches and holds a pose.

It does **not** test: your real tuning (the sim robot has its own physics, so times and accuracy are not
your robot's), whether mechanisms work (see below), vision (the Limelight never sees a tag), or game
elements. Logging is off in the sim (your `LogUtil` prints "could not start" and carries on).

**Mechanisms are stand-ins.** `sim/stubs/ours/sim/OurDevices.java` registers the Decode devices under
`DecodeConfig`'s own names (intake, indexer, two shooter motors, stopper servo, turret servo and limit
switch, LED), so `DecodeRobot` builds unmodified. They accept commands and give plausible readings. The
flywheel approaches its commanded speed with a time constant of 0.5 s, which is a guess. Nothing launches
or picks up a game piece, so a shot "fires" on timing alone. That is enough to check an auto's sequencing
and timeouts (`DecodeRedAudienceAuto` runs start to finish), not to tell whether a shot would score.
A robot with different devices needs its own stand-ins added there.

## How it is set up

`run_sim.sh` fetches virtual_robot at one pinned commit, applies `patch_sim.py`, and compiles it. No
virtual_robot source is stored in our repo (it has no top-level license file). The patch makes three
changes, each an exact-text edit that stops with an error if the sim's code ever differs:

1. `@Autonomous` gains `preselectTeleOp`, which our autos use.
2. The MecDynamic robot's device names become ours: `Front_Left`, `Front_Right`, `Rear_Left`, `Rear_Right`,
   `odo` (`imu` already matches). If a robot's `RobotConfig.HardwareNames` change, change the table in
   `patch_sim.py` too.
3. Setting a pose teleports the simulated robot to it. Pedro's `follower.setPose` calls
   `GoBildaPinpointDriver.setPosition`, and the sim now moves its robot there (Pedro frame: inches from
   the bottom-left corner, heading 0 along +x; sim frame: pixels from the centre, heading 0 facing up),
   then resyncs its odometry baseline. Without the resync the jump counts as driven distance and each run
   ends somewhere different.

4. Our stand-in mechanisms are added to the hardware map (by reflection, if `OurDevices` was compiled).

`sim/stubs` also holds stand-ins for FTC SDK classes the sim lacks, compiled with your OpModes and used
only here (not by Gradle): `ServoImplEx`, `PwmControl`, the Limelight result types, `Pose3D`/`Position`,
`AppUtil`, and Koala-Log's `LogFileManager`. The last two make logging fail cleanly (`RuntimeException`),
which `LogUtil` already handles.

To move to a newer virtual_robot, change `PINNED_COMMIT` in `run_sim.sh`, run with `--rebuild`, and fix
`patch_sim.py` if its error message says an edit no longer matches.
