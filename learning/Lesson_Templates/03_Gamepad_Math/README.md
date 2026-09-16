# Lesson 03 — Gamepad & Basic Math

**Book**: Chapter 3 | **Estimated Time**: 1–2 hours | **Hardware Required**: Gamepad only

> This lesson was previously numbered Lesson 4. It has been renumbered to match
> Chapter 3 of "Learn Java for FTC."

## Book Alignment
This lesson is based on the LearnJavaForFTC chapter *Gamepad and basic math* and *Motors and Gamepads*.
**Book reference**: Chapter 3 — Listings 3.1 (`GamepadOpMode.java`) and 3.2 (`MathOpMode.java`).
Also see Chapter 7.6 (`MotorGamepadOpMode.java`).

## Learning Objectives
By the end of this lesson, students will be able to:
- Access gamepad input from the driver station
- Read button states (pressed/released)
- Read analog stick values (smooth input)
- Read trigger values
- Use gamepad input to control motors and servos

## Key Concepts

### What is a Gamepad?
The gamepad (game controller) is how the driver interacts with the robot during TeleOp mode. There are two gamepads available:
- `gamepad1` - Main driver gamepad
- `gamepad2` - Co-driver gamepad (optional)

### Button Input (Digital)
Buttons are either pressed (true) or not pressed (false):
```java
boolean aPressed = gamepad1.a;           // A button
boolean bPressed = gamepad1.b;           // B button
boolean xPressed = gamepad1.x;           // X button
boolean yPressed = gamepad1.y;           // Y button
boolean backPressed = gamepad1.back;     // Back button
boolean startPressed = gamepad1.start;   // Start button
boolean leftBumper = gamepad1.left_bumper;  // LB
boolean rightBumper = gamepad1.right_bumper; // RB
```

### Stick Input (Analog)
Sticks give smooth values from -1.0 to +1.0:
```java
double leftStickX = gamepad1.left_stick_x;    // -1.0 to +1.0 (left to right)
double leftStickY = gamepad1.left_stick_y;    // -1.0 to +1.0 (up to down) [INVERTED!]
double rightStickX = gamepad1.right_stick_x;  // Rotate
double rightStickY = gamepad1.right_stick_y;  // Forward/backward
```

**IMPORTANT**: The Y-axis is inverted! Pushing stick forward gives NEGATIVE value.

### Trigger Input (Analog)
Triggers give values from 0.0 (not pressed) to 1.0 (fully pressed):
```java
double leftTrigger = gamepad1.left_trigger;   // 0.0 to 1.0
double rightTrigger = gamepad1.right_trigger; // 0.0 to 1.0
```

## Code Patterns

### Using Buttons to Control Motors
```java
if (gamepad1.a) {
    motor.setPower(1.0);  // Run motor when A pressed
} else {
    motor.setPower(0.0);  // Stop when released
}
```

### Using Sticks to Control Motors
```java
// Left stick controls forward/backward
double drivePower = gamepad1.left_stick_y;  // Remember: inverted!
motor.setPower(drivePower);
```

### Using Buttons to Control Motor Speed
```java
// A button runs the motor at a set speed, otherwise the motor is stopped.
if (gamepad1.a) {
    motor.setPower(0.5);
} else {
    motor.setPower(0.0);
}
```

The book example `MotorGamepadOpMode.java` uses a button to control the motor and telemetry while the OpMode updates continuously.

### Using Buttons for Servo Positions
```java
if (gamepad1.a) {
    servo.setPosition(0.0);  // Close
} else if (gamepad1.b) {
    servo.setPosition(1.0);  // Open
}
```

## Important Notes
- Gamepad values update frequently (~100 Hz)
- Always read gamepad in your main loop
- Buttons are sampled once per loop
- For continuous movement, check button state inside loop
- Remember: left_stick_y is INVERTED!

## Summary
The gamepad is your interface to control the robot. Buttons provide on/off control, sticks and triggers provide smooth analog control. Combine these inputs to create intuitive robot control.

---
**Related**: Lessons 1-3 covered Motors, Servos, and Sensors. This lesson combines all three with gamepad input!
