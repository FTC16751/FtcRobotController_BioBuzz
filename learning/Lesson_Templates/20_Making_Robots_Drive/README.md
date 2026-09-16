# Lesson 20 — Making Robots Drive

**Book**: Chapter 20 | **Estimated Time**: 3–4 hours | **Hardware Required**: 2-motor or 4-motor (mecanum) robot

---

## What You'll Learn

- **Tank Drive** vs **Arcade Drive** vs **Mecanum Drive**
- The motor normalization trick (keeps relative speeds correct even when values exceed ±1)
- Mecanum wheel math (4 motors, drive in any direction)
- **Field-relative driving** using the IMU from Lesson 11

---

## Two-Motor Arcade Drive

Arcade drive maps ONE stick to both forward/backward AND turning:

```
leftWheelPower  = forward + turn
rightWheelPower = forward - turn
```

Adding turn makes the left wheel faster (turns right); subtracting makes the
right wheel faster. The normalization step keeps both values within ±1:

```java
double largest = Math.max(1.0, Math.max(Math.abs(leftPower), Math.abs(rightPower)));
leftMotor.setPower(leftPower  / largest);
rightMotor.setPower(rightPower / largest);
```

---

## Mecanum Wheel Math

Mecanum wheels let the robot move sideways. The 4-motor power formulas are:

```
leftFrontPower  = forward + right + rotate
rightFrontPower = forward - right - rotate
leftBackPower   = forward - right + rotate
rightBackPower  = forward + right - rotate
```

Where:
- `forward` = left stick Y (negated — stick up = positive)
- `right`   = left stick X (strafe)
- `rotate`  = right stick X

---

## Motor Direction

The left-side motors are mounted facing the opposite direction from right-side motors.
Set left motors to `REVERSE` so "positive power" always means "wheel rolls forward":

```java
leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
```

For mecanum: set both `frontLeftMotor` and `backLeftMotor` to REVERSE.

---

## Field-Relative Driving

In robot-relative mode, "forward" always means the front of the robot.
In field-relative mode, "forward" always means toward the far end of the field,
regardless of which way the robot is facing.

See the template for the polar coordinate rotation trick.

---

## Book Reference

- Chapter 20, sections 20.1–20.3 (pages 139–149)
- Listing 20.1: `TwoMotorDrive.java`
- Listing 20.2–20.3: `ArcadeDrive.java`, `BetterArcadeDrive.java`
- Listing 20.4: `MecanumDrive.java`
- Listing 20.5–20.6: `SimpleMecanumDriveOpMode.java`, `FieldRelativeMecanumDriveOpMode.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L20_TwoMotorDrive.java` | 2-motor mechanism class |
| `Template_L20_ArcadeDrive.java` | Arcade drive OpMode |
| `Template_L20_MecanumDrive.java` | 4-motor mecanum mechanism |
| `Template_L20_MecanumOpMode.java` | Mecanum OpMode |
| `Complete_Solution_*.java` | Solutions |
| `Exercises.txt` | Practice challenges |
