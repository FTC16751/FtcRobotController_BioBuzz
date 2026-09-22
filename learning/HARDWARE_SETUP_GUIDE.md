# Hardware Setup Guide — the programming board

One small rig serves lessons L03 and L06–L14: a Control Hub with one motor, one servo, a touch
sensor, a potentiometer and a REV Color/Range sensor. Build it once, configure it once, and every
lesson's code names match. L20 and L24 need a drivable robot instead (see the bottom).

## Wiring

| Hub port | Device | Config type | Config name | Used by |
|----------|--------|-------------|-------------|---------|
| Motor 0 | any 12 V DC motor with encoder (REV HD Hex, goBILDA Yellow Jacket) | DcMotor (pick the real motor type) | `motor` | L03, L07, L12, L14, L24 |
| Servo 0 | standard 0–180° servo | Servo | `servo` | L03, L08, L12 |
| Digital 1 | REV Touch Sensor | Digital Channel | `touch_sensor` | L06, L12, L14 |
| Analog 0 | potentiometer (REV) | Analog Input | `pot` | L09 |
| I2C bus 1 | REV Color Sensor V3 / Color-Range | REV Color/Range Sensor | `sensor_color_distance` | L10 |
| built in | Control Hub IMU | already present | `imu` | L11 |

Plug the motor's encoder cable into Encoder 0 as well, or L07's encoder section and L24 will read
zero. Mount the hub flat and note which way its logo and USB ports face; L11 asks for that.

## Configuring the Control Hub (once)

On the Driver Station: ⋮ → **Configure Robot** → **New** → tap the hub, then for each row above
pick the port, set the type and type the name **exactly** (lowercase, underscores). Save as
`programming_board`, then **Activate**. L06's README walks through the first device step by
step; the other lessons say "add to your `programming_board` config".

Names are case-sensitive. A typo in the config or in the code is by far the most common reason
"my hardware doesn't respond".

## Testing the board

Deploy any lesson's `Complete_Solution` (rename its `package` line the same way students do) and
run it. Quick checks:

- L07 solution: the motor spins forward, then reverse, then stops, on repeat
- L08 solution: the servo steps through 0.0, 0.5, 1.0
- L06 solution: telemetry flips when you press the touch sensor
- L10 solution: hold a hand in front of the sensor; distance falls, colour values change
- L11 solution: turn the hub; yaw changes and wraps at ±180°

## Common problems

| Symptom | Check |
|---------|-------|
| "Unable to find a hardware device with name …" | config name vs code string; is `programming_board` the **active** config? |
| Motor spins but encoder stays 0 | encoder cable in the matching Encoder port; motor has an encoder at all |
| Servo twitches or does nothing | servo on a Servo port, not a Motor port; battery voltage |
| Touch sensor always false | Digital port **1** (REV touch uses the n+1 pin); wired with the right cable |
| Colour sensor not found | I2C bus 1, port 0; sensor type set to REV Color/Range Sensor |
| IMU reads nonsense | `RevHubOrientationOnRobot` in the code matches how the hub is mounted |
| Nothing works after config edit | did you tap **Activate** and restart the robot? |

## L20 and L24: a drivable robot

- **L20** needs a chassis. The two-motor template wants `left_motor` and `right_motor`; the
  mecanum template wants `front_left_motor`, `front_right_motor`, `back_left_motor`,
  `back_right_motor`. The demobots in `TeamCode/.../teams/demobots` are the team's own
  drive-only robots; their motor names are fixed in `common/drive/DriveUtilSimple`.
- **L24** runs on the programming board's `motor` (with encoder), but a flywheel or an arm on a
  real robot makes the PID tuning far more visible.

## Before a session

- [ ] Board powered, `programming_board` active, a solution OpMode deployed and run once
- [ ] Battery charged, gamepad paired (L03, L12 and later)
- [ ] Each student's laptop builds `TeamCode` and can deploy over USB or Wi-Fi
