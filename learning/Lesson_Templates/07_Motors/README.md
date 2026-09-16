# Lesson 1: Motor Basics

## Book Alignment
This lesson is based on the LearnJavaForFTC chapters *Our first OpMode* and *Motors*. Use the book as a reference for deeper explanations of OpMode structure, hardware mapping, and motor behavior.
**Book reference**: Read the "Our first OpMode" section and the "Motors" section for the first FTC OpMode example and the initial motor power patterns.

## Learning Objectives
By the end of this lesson, students will be able to:
- Understand the structure of an FTC OpMode
- Declare and initialize a motor in hardware
- Control a motor's direction (forward/reverse/stop)
- Understand motor power ranges (-1.0 to +1.0)
- Send commands to the robot controller

## Key Concepts

### What is a Motor?
A motor is an electrical device that converts electrical power into mechanical motion. In FTC, we use motors to move parts of the robot like wheels, arms, or claws.

### OpMode Structure
Every FTC program inherits from either `LinearOpMode` or `OpMode`. A `LinearOpMode` is simpler and runs code sequentially from top to bottom.

Key methods in LinearOpMode:
- `runOpMode()` - Main entry point, called when the PLAY button is pressed
- `waitForStart()` - Waits for driver to press the PLAY button
- `opModeIsActive()` - Checks if OpMode is still running (returns false if STOP pressed)

### Motor Power Range
Motors accept power values from -1.0 to +1.0:
- **+1.0** = Full power forward
- **+0.5** = Half power forward
- **0.0** = Stop
- **-0.5** = Half power reverse
- **-1.0** = Full power reverse

### Hardware Configuration
Before you can use a motor in code, it must be configured in the Robot Controller app on the physical hardware.

## Code Pattern: Motor Basics

### Step 1: Declare the motor
```java
private DcMotor motorTest;
```

### Step 2: Initialize in runOpMode()
```java
motorTest = hardwareMap.get(DcMotor.class, "motorTest");
```
The string "motorTest" must match the name in your hardware configuration file.

### Step 3: Control the motor
```java
motorTest.setPower(1.0);    // Full speed forward
motorTest.setPower(-1.0);   // Full speed reverse
motorTest.setPower(0.0);    // Stop
```

### Step 4: Read motor state
```java
double currentPower = motorTest.getPower();
```

## Hardware Configuration Checklist
Before testing:
- [ ] Motor is physically connected to the motor port
- [ ] Motor port name is configured in the FTC Robot Controller app
- [ ] Configuration file is saved on the robot controller
- [ ] You're using the correct name in your code (case-sensitive!)

## Summary
Motors are controlled by setting their power value. The power range from -1.0 to +1.0 determines both direction and speed. Always initialize the motor before using it!

---
**Next Lesson**: Servo Control - Another way to move things with precise positioning
