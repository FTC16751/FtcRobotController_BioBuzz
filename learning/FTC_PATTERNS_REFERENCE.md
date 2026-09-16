# FTC Programming Patterns Reference Guide

A comprehensive guide of common FTC programming patterns and best practices, based on LearnJavaForFTC and FTC SDK standards.

---

## Table of Contents
1. [OpMode Structure](#opmode-structure)
2. [Hardware Initialization](#hardware-initialization)
3. [Motor Control Patterns](#motor-control-patterns)
4. [Servo Control Patterns](#servo-control-patterns)
5. [Sensor Reading Patterns](#sensor-reading-patterns)
6. [Gamepad Input Patterns](#gamepad-input-patterns)
7. [Control Flow Patterns](#control-flow-patterns)
8. [Common Utilities](#common-utilities)

---

## OpMode Structure

### LinearOpMode vs OpMode
- **LinearOpMode**: Sequential execution, simpler to understand, runs code line-by-line
- **OpMode**: State-based, more complex, runs init() then loop() repeatedly

### Basic LinearOpMode Template
```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "My Program", group = "Learning")
public class MyRobot extends LinearOpMode {
    
    private DcMotor motor;
    
    @Override
    public void runOpMode() {
        // Initialize hardware
        motor = hardwareMap.get(DcMotor.class, "motor");
        
        // Send status to driver station
        telemetry.addData("Status", "Ready");
        telemetry.update();
        
        // Wait for PLAY button
        waitForStart();
        
        // Main loop - runs until STOP pressed
        while (opModeIsActive()) {
            // Your code here
            motor.setPower(1.0);
            
            telemetry.addData("Motor Power", motor.getPower());
            telemetry.update();
        }
    }
}
```

### Key Methods
- `waitForStart()` - Blocks until PLAY button pressed
- `opModeIsActive()` - Returns false if STOP pressed
- `sleep(ms)` - Wait in milliseconds
- `telemetry.update()` - Send data to driver station

---

## Hardware Initialization

### Standard Hardware Map Names (by type)

**Motors**
```java
DcMotor motorLeft = hardwareMap.get(DcMotor.class, "motorLeft");
DcMotor motorRight = hardwareMap.get(DcMotor.class, "motorRight");
```

**Servos**
```java
Servo servoGripper = hardwareMap.get(Servo.class, "servoGripper");
Servo servoArm = hardwareMap.get(Servo.class, "servoArm");
```

**Sensors**
```java
DistanceSensor distanceSensor = hardwareMap.get(DistanceSensor.class, "distanceSensor");
ColorSensor colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
DigitalChannel touchSensor = hardwareMap.get(DigitalChannel.class, "touchSensor");
```

**Important**: Hardware configuration names must match EXACTLY (case-sensitive)!

### Safe Initialization Pattern
```java
private void initializeHardware() {
    try {
        motorLeft = hardwareMap.get(DcMotor.class, "motorLeft");
        motorRight = hardwareMap.get(DcMotor.class, "motorRight");
        telemetry.addData("Status", "Hardware initialized successfully");
    } catch (Exception e) {
        telemetry.addData("ERROR", "Failed to initialize: " + e.getMessage());
    }
    telemetry.update();
}
```

---

## Motor Control Patterns

### Basic Motor Control
```java
// Forward at full power
motor.setPower(1.0);

// Reverse at full power
motor.setPower(-1.0);

// Stop
motor.setPower(0.0);

// Read current power
double power = motor.getPower();
```

### Tank Drive (Two Motors)
```java
// Left stick controls left motor, right stick controls right motor
motorLeft.setPower(gamepad1.left_stick_y);
motorRight.setPower(gamepad1.right_stick_y);
```

**Important**: Remember left_stick_y is INVERTED! Push forward = negative value.

```java
// Fix inverted Y-axis
motorLeft.setPower(-gamepad1.left_stick_y);
motorRight.setPower(-gamepad1.right_stick_y);
```

### Arcade Drive (Two Motors, One Stick)
```java
double drive = gamepad1.left_stick_y;   // Forward/backward
double turn = gamepad1.left_stick_x;     // Left/right

motorLeft.setPower(drive + turn);
motorRight.setPower(drive - turn);
```

### Motor with Encoder (Position Control)
```java
// Read encoder position (in ticks)
int encoderPosition = motor.getCurrentPosition();

// Reset encoder
motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

// Run to position (autonomous)
motor.setTargetPosition(1000);  // Move 1000 ticks
motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
motor.setPower(0.5);

// Check if reached target
if (motor.isBusy()) {
    telemetry.addData("Status", "Moving to position");
} else {
    telemetry.addData("Status", "Position reached");
}
```

### Motor Direction Reversal
```java
// If motor spins wrong direction, reverse it
motor.setDirection(DcMotor.Direction.REVERSE);

// Set to forward (default)
motor.setDirection(DcMotor.Direction.FORWARD);
```

### Motor Brake vs Float
```java
// BRAKE: Motor stops immediately when power set to 0
motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

// FLOAT: Motor coasts when power set to 0
motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
```

---

## Servo Control Patterns

### Basic Servo Control
```java
// Position 0.0 = 0°
servo.setPosition(0.0);

// Position 0.5 = 90°
servo.setPosition(0.5);

// Position 1.0 = 180°
servo.setPosition(1.0);

// Read current position
double position = servo.getPosition();
```

### Named Servo Positions (Constants)
```java
static final double SERVO_OPEN = 1.0;
static final double SERVO_CLOSED = 0.0;
static final double SERVO_MID = 0.5;

// Usage
servo.setPosition(SERVO_OPEN);
servo.setPosition(SERVO_CLOSED);
```

### Servo Toggle Pattern
```java
private boolean servoOpen = false;

// In loop
if (gamepad1.a && !previousAPressed) {
    servoOpen = !servoOpen;
    servo.setPosition(servoOpen ? SERVO_OPEN : SERVO_CLOSED);
}
previousAPressed = gamepad1.a;
```

### Two-Servo Complementary Control
```java
// When one opens, the other closes (like a claw)
servo1.setPosition(0.2);      // Left finger
servo2.setPosition(1.0 - 0.2); // Right finger (opposite)
```

---

## Sensor Reading Patterns

### Distance Sensor
```java
// Read distance in centimeters
double distanceCm = distanceSensor.getDistance(DistanceUnit.CM);

// Read distance in inches
double distanceIn = distanceSensor.getDistance(DistanceUnit.INCH);

// Simple threshold check
if (distanceCm < 10) {
    telemetry.addData("Status", "Object very close!");
}
```

### Color Sensor
```java
// Read color values
int red = colorSensor.red();
int green = colorSensor.green();
int blue = colorSensor.blue();
int alpha = colorSensor.alpha();

// Read brightness (reflectance)
int brightness = colorSensor.alpha();

// Detect color
if (red > green && red > blue) {
    telemetry.addData("Color", "RED detected");
}
```

### Touch Sensor
```java
// Read touch sensor state
boolean isPressed = touchSensor.getState();

// Remember: state() returns true when NOT pressed
// So invert it
if (!touchSensor.getState()) {
    telemetry.addData("Status", "Button pressed!");
}
```

### Motor Encoder (as Sensor)
```java
// Get current encoder count
int ticks = motor.getCurrentPosition();

// Calculate distance traveled
double distanceInches = (ticks / TICKS_PER_INCH);
```

### Averaging Sensor Readings
```java
// Reduce sensor noise with averaging
double[] readings = new double[5];
for (int i = 0; i < 5; i++) {
    readings[i] = distanceSensor.getDistance(DistanceUnit.CM);
}

// Calculate average
double average = 0;
for (double reading : readings) {
    average += reading;
}
average /= readings.length;
```

---

## Gamepad Input Patterns

### Reading Button States
```java
// Digital buttons (pressed or not)
boolean a = gamepad1.a;
boolean b = gamepad1.b;
boolean x = gamepad1.x;
boolean y = gamepad1.y;
boolean leftBumper = gamepad1.left_bumper;
boolean rightBumper = gamepad1.right_bumper;
boolean back = gamepad1.back;
boolean start = gamepad1.start;
```

### Reading Stick Input
```java
// Analog sticks (-1.0 to +1.0)
double leftStickX = gamepad1.left_stick_x;   // Strafe left/right
double leftStickY = gamepad1.left_stick_y;   // Forward/back (INVERTED!)
double rightStickX = gamepad1.right_stick_x; // Rotate
double rightStickY = gamepad1.right_stick_y; // Vertical
```

### Reading Trigger Input
```java
// Analog triggers (0.0 to 1.0)
double leftTrigger = gamepad1.left_trigger;
double rightTrigger = gamepad1.right_trigger;

// Combined trigger control
double motorPower = rightTrigger - leftTrigger;  // Forward/back
```

### Dead Zone Implementation
```java
// Ignore small stick movements (dead zone)
double deadzone = 0.1;

double stickX = gamepad1.left_stick_x;
double stickY = gamepad1.left_stick_y;

if (Math.abs(stickX) < deadzone) stickX = 0;
if (Math.abs(stickY) < deadzone) stickY = 0;

motor.setPower(stickY);
```

### Button State Tracking (Edge Detection)
```java
// Track previous state to detect button press transitions
private boolean previousA = false;

// In main loop
if (gamepad1.a && !previousA) {
    // Button just pressed
    telemetry.addData("Event", "A button pressed!");
}
previousA = gamepad1.a;
```

### Dual Gamepad
```java
// Two drivers can control different subsystems
// Driver 1 controls motors
motorLeft.setPower(gamepad1.left_stick_y);

// Driver 2 controls arm
armServo.setPosition(gamepad2.left_stick_y / 2 + 0.5);
```

---

## Control Flow Patterns

### If/Else Decision Making
```java
// Single condition
if (distance < 10) {
    motor.setPower(0.0);  // Stop if too close
}

// If/else
if (gamepad1.a) {
    servo.setPosition(OPEN);
} else {
    servo.setPosition(CLOSED);
}

// If/else-if/else (multiple conditions)
if (distance < 5) {
    motor.setPower(0.0);
} else if (distance < 20) {
    motor.setPower(0.5);
} else {
    motor.setPower(1.0);
}
```

### While Loop (repeating during OpMode)
```java
while (opModeIsActive()) {
    // This runs repeatedly until STOP pressed
    motor.setPower(gamepad1.left_trigger);
    telemetry.addData("Power", motor.getPower());
    telemetry.update();
}
```

### For Loop (counting)
```java
// Repeat action 5 times
for (int i = 0; i < 5; i++) {
    motor.setPower(0.5);
    sleep(1000);
    motor.setPower(0.0);
    sleep(500);
}
```

### Switch Statement (multiple options)
```java
int mode = 0;  // 0=stop, 1=forward, 2=backward

switch (mode) {
    case 0:
        motor.setPower(0.0);
        break;
    case 1:
        motor.setPower(1.0);
        break;
    case 2:
        motor.setPower(-1.0);
        break;
}
```

---

## Common Utilities

### Telemetry Patterns
```java
// Display multiple lines
telemetry.addData("Status", "Running");
telemetry.addData("Motor Power", motor.getPower());
telemetry.addData("Servo Position", servo.getPosition());
telemetry.addData("Distance", distance);
telemetry.update();

// Clear telemetry
telemetry.clear();

// Format numbers
telemetry.addData("Distance", String.format("%.2f cm", distance));
telemetry.addData("Position", String.format("%d ticks", encoder));
telemetry.update();
```

### Timing Patterns
```java
// Delay
sleep(1000);  // Wait 1 second

// Time-based movement (autonomous)
ElapsedTime runtime = new ElapsedTime();
while (opModeIsActive() && runtime.seconds() < 5.0) {
    motor.setPower(1.0);  // Run for 5 seconds
}

// Check elapsed time
if (runtime.seconds() > 2.0) {
    telemetry.addData("Status", "2 seconds have passed");
}
```

### Clipping Values to Range
```java
// Ensure value is between -1.0 and 1.0
double power = Range.clip(stickValue, -1.0, 1.0);

// Ensure value is between 0.0 and 1.0
double position = Range.clip(servoValue, 0.0, 1.0);
```

### Absolute Value
```java
// Get magnitude (distance) of a value
double distance = Math.abs(motor.getPower());
```

### Rounding
```java
// Round to nearest integer
int rounded = Math.round(value);

// Round to 2 decimal places
double rounded = Math.round(value * 100.0) / 100.0;
```

---

## Best Practices

### Always Use Constants
```java
// GOOD: Easy to change, readable
static final double MOTOR_SPEED = 0.8;
motor.setPower(MOTOR_SPEED);

// BAD: Magic numbers, hard to maintain
motor.setPower(0.8);
```

### Name Hardware Descriptively
```java
// GOOD
private DcMotor leftDriveMotor;
private Servo gripperServo;

// NOT IDEAL
private DcMotor m1;
private Servo s;
```

### Add Comments for Complex Logic
```java
// Calculate arcade drive (mixing drive and turn)
double drive = gamepad1.left_stick_y;
double turn = gamepad1.left_stick_x;
motorLeft.setPower(drive + turn);
motorRight.setPower(drive - turn);
```

### Initialize Motors with Correct Properties
```java
motorLeft.setDirection(DcMotor.Direction.FORWARD);
motorRight.setDirection(DcMotor.Direction.REVERSE);  // Often reversed
motorLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
motorRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
```

---

## Troubleshooting Checklist

**Hardware doesn't respond:**
- [ ] Is hardware physically connected?
- [ ] Is name in code EXACTLY matching configuration?
- [ ] Are you initializing in runOpMode()?
- [ ] Is the robot powered on?

**Motor spins wrong direction:**
- [ ] Set `setDirection(DcMotor.Direction.REVERSE)`

**Servo jerky/not smooth:**
- [ ] Increase delay between position changes
- [ ] Check power supply (servo draws significant current)

**Gamepad input not working:**
- [ ] Is gamepad connected to Driver Station?
- [ ] Check gamepad battery
- [ ] Ensure reading inside while loop

**Telemetry not displaying:**
- [ ] Called `telemetry.update()`?
- [ ] Check log on driver station

---

## Advanced Topics (See LearnJavaForFTC for Details)

- State machines for complex robot behavior
- Autonomous path planning
- PID control for smooth motion
- Computer vision with OpenCV
- IMU/Gyro for precise turning

---

**Last Updated**: Based on LearnJavaForFTC by Alan G. Smith (2024)
