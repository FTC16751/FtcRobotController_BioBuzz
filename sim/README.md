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
dependencies compile automatically. Expect it to fail if it needs hardware the sim does not have; see below.

## What the simulator can and cannot tell you

Checked 2026-10-10 with `DecodePedroParkAuto`: it ended at the park pose (9, 106.1, 180 degrees) in
5.3 s on two runs in a row, and Pedro's reported pose matched where the robot sat on screen.

It tests: path order and sequencing, state machines, start-pose and heading handling, and whether the
follower reaches and holds a pose.

It does **not** test: your real tuning (the sim robot has its own physics, so times and accuracy are not
your robot's), mechanisms (a flywheel is just numbers, there is no launching), vision (no Limelight or
cameras), or game elements. Only drive-and-Pinpoint autos work today. The sim robot has no `intake`,
`indexer`, `left_shooter` and so on, so autos that touch those fail at INIT with "No ... named ... is found".

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

To move to a newer virtual_robot, change `PINNED_COMMIT` in `run_sim.sh`, run with `--rebuild`, and fix
`patch_sim.py` if its error message says an edit no longer matches.
