# Lesson 11 — Gyro / IMU

**Book**: Chapter 11 | **Estimated Time**: 2 hours | **Hardware Required**: Control Hub (IMU is built in)

---

## What You'll Learn

- How to use the built-in **IMU** (Inertial Measurement Unit) in the Control Hub
- Setting up `RevHubOrientationOnRobot` (you must tell it how the hub is mounted)
- Reading **Yaw** (heading), Pitch, and Roll
- Using `AngleUnit.DEGREES` vs `AngleUnit.RADIANS`
- Why `AngleUnit` normalizes angles to the range −180 to +180

---

## Key Concept: Hub Orientation

The IMU needs to know which way the Control Hub logo faces and which way
the USB port faces. This accounts for different mounting positions on your robot.

```java
RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
    RevHubOrientationOnRobot.LogoFacingDirection.UP,
    RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
);
imu.initialize(new IMU.Parameters(orientation));
```

Common direction options: `UP`, `DOWN`, `FORWARD`, `BACKWARD`, `LEFT`, `RIGHT`

---

## Reading the Heading

```java
// YAW = rotation around vertical axis (which way the robot faces)
double headingDegrees = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
double headingRadians = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
```

`AngleUnit` automatically normalizes:
- DEGREES → always in range −180 to +180
- RADIANS → always in range −π to +π

---

## Configuration

No configuration file changes needed — the IMU is already named `imu` in every
Control Hub configuration by default.

---

## Book Reference

- Chapter 11, sections 11.1–11.4 (pages 73–76)
- Listing 11.1: `ProgrammingBoard8.java`
- Listing 11.2: `GyroOpMode.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L11_IMU.java` | Template with TODOs |
| `Complete_Solution.java` | Full solution |
| `Exercises.txt` | Practice challenges |

---

## Common Mistakes

- **Wrong orientation** — if heading goes the wrong direction, try `REVERSE` instead of `FORWARD` for USB
- **Not initializing** — forgetting `imu.initialize()` means all readings will be 0
- **Unit confusion** — `getYaw()` requires you specify the unit; it won't guess
