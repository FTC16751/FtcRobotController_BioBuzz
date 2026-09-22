# Lesson 08 — Servos
**Book**: Chapter 8 | **Estimated Time**: 2 hours | **Hardware Required**: Programming board servo (`servo`)

| File | Purpose |
|------|---------|
| `Template_L08_Servos.java` | Your starting point — class `L08_Servos` |
| `Complete_Solution.java` | Full working solution |
| `Exercises.txt` | Extra practice challenges |

## Book Alignment
This lesson is based on the LearnJavaForFTC chapter *Servos*. Use the book as a reference for servo configuration, position values, and examples of simple servo mechanisms.
**Book reference**: Follow the book's servo hardware configuration guidance from section 7.1 and the example servo control patterns.

## Learning Objectives
By the end of this lesson, students will be able to:
- Understand the difference between motors and servos
- Initialize and control a servo
- Position a servo to specific angles
- Use servo position ranges (0.0 to 1.0)
- Read current servo position

## Key Concepts

### Motor vs. Servo
**Motors** spin continuously when powered:
- Power values: -1.0 to +1.0
- Rotate indefinitely in chosen direction
- Good for wheels, flywheels, conveyor belts

**Servos** move to specific positions and hold:
- Position values: 0.0 to 1.0
- Can move to exact angle and hold
- Good for claws, intake arms, camera mounts
- Typical range is 0° to 180°

### Servo Position Range
When you set a servo position:
- **0.0** = 0° (minimum position)
- **0.5** = 90° (middle position)
- **1.0** = 180° (maximum position)
- Any value between 0.0 and 1.0 is valid

### Hardware Configuration
Servos are configured similarly to motors but appear as `Servo` in the hardware configuration.

## Code Pattern: Servo Basics

### Step 1: Declare the servo
```java
private Servo servo;
```

### Step 2: Initialize in runOpMode()
```java
servo = hardwareMap.get(Servo.class, "servo");
```
The string "servo" must match the name in your hardware configuration file.

### Step 3: Set servo position
```java
servo.setPosition(0.0);    // Move to 0°
servo.setPosition(0.5);    // Move to 90°
servo.setPosition(1.0);    // Move to 180°
```

### Step 4: Read current servo position
```java
double currentPosition = servo.getPosition();
```

## Important Notes
- Servo moves immediately to the target position (no ramp-up)
- Servo holds position even after code stops sending commands
- Servo draws significant power - avoid many servos at once
- Servo position 0.0 to 1.0 maps to physical 0° to 180°

## Summary
Servos provide precise angular positioning, unlike motors which rotate continuously. Use servo position values from 0.0 to 1.0 to control movement to specific angles.

---
**Related**: L07 covered motors; L03 used a gamepad to drive both a motor and a servo
