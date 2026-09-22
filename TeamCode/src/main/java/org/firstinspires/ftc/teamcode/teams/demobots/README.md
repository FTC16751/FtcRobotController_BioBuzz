# DemoBots: the simplest robots we drive

Two TeleOps for demo and outreach chassis that have nothing but drive motors. No IMU, no odometry,
no camera, no config or constants files. Everything they need is `common/drive/DriveUtilSimple`.

```
teams/demobots/
  PushbotTeleop.java   Two motors. Left stick Y drives, right stick X turns.
  MecanumTeleop.java   Four motors, robot-centric. Left stick drives and strafes, right stick X turns.
  README.md            This file.
```

Both show up on the Driver Station under the group **DemoBots**. Hold the left bumper for slow mode.
The two speeds are constants at the top of each file.

## Wiring

Motor names are fixed in `DriveUtilSimple` and must match the Control Hub configuration exactly:

| Robot   | Names                                                  | Directions                     |
|---------|--------------------------------------------------------|--------------------------------|
| Pushbot | `left_drive`, `right_drive`                            | right reversed                 |
| Mecanum | `Front_Left`, `Front_Right`, `Rear_Left`, `Rear_Right` | right side reversed            |

If a wheel spins the wrong way, flip that motor's direction in `DriveUtilSimple`, not here.

## When to outgrow this

A robot that gains a mechanism, needs an IMU, or needs autonomous should copy `teams/testteam2027/`
instead and follow its README. That layout uses `DriveUtil` with a RobotConfig; this one stays a
two-file drive-only demo on purpose.
