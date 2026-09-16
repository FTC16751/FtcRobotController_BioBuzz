# FTC Programming Quick Reference Card

Print this out and keep it on your desk while coding!

---

## Motor Control

```java
// Initialize
motor = hardwareMap.get(DcMotor.class, "motorName");

// Set power
motor.setPower(1.0);    // Full speed forward
motor.setPower(-1.0);   // Full speed reverse
motor.setPower(0.5);    // Half speed forward
motor.setPower(0.0);    // Stop

// Read power
double p = motor.getPower();
```

**Remember**: Power range is -1.0 to +1.0

---

## Servo Control

```java
// Initialize
servo = hardwareMap.get(Servo.class, "servoName");

// Set position
servo.setPosition(0.0);   // 0 degrees
servo.setPosition(0.5);   // 90 degrees
servo.setPosition(1.0);   // 180 degrees

// Read position
double pos = servo.getPosition();
```

**Remember**: Position range is 0.0 to 1.0 (maps to 0-180°)

---

## Sensor Reading

```java
// Distance Sensor
double dist = distanceSensor.getDistance(DistanceUnit.CM);

// Color Sensor
int red = colorSensor.red();
int green = colorSensor.green();
int blue = colorSensor.blue();

// Touch Sensor
boolean pressed = !touchSensor.getState();  // Note: inverted!
```

---

## Gamepad Input

### Buttons
```java
gamepad1.a          // A button
gamepad1.b          // B button
gamepad1.x          // X button
gamepad1.y          // Y button
gamepad1.left_bumper    // LB
gamepad1.right_bumper   // RB
```

### Sticks (Analog)
```java
gamepad1.left_stick_x      // -1.0 to +1.0 (left/right)
gamepad1.left_stick_y      // -1.0 to +1.0 (INVERTED!)
gamepad1.right_stick_x     // -1.0 to +1.0 (rotate)
gamepad1.right_stick_y     // -1.0 to +1.0
```

### Triggers (Analog)
```java
gamepad1.left_trigger      // 0.0 to 1.0
gamepad1.right_trigger     // 0.0 to 1.0
```

**⚠️ ALERT**: left_stick_y is INVERTED!  
→ Push stick FORWARD = **NEGATIVE** value  
→ Pull stick BACK = **POSITIVE** value

---

## Basic Control Flow

```java
// If statement
if (condition) {
    // Do something
}

// If-else
if (gamepad1.a) {
    servo.setPosition(0.0);
} else {
    servo.setPosition(1.0);
}

// While loop (runs repeatedly)
while (opModeIsActive()) {
    motor.setPower(gamepad1.left_trigger);
}

// For loop (counted repetition)
for (int i = 0; i < 5; i++) {
    motor.setPower(0.5);
    sleep(1000);
}
```

---

## Telemetry (Display Data)

```java
// Add data
telemetry.addData("Label", value);
telemetry.addData("Motor Power", motor.getPower());
telemetry.addData("Distance", distance + " cm");

// Send to Driver Station
telemetry.update();

// Clear old data
telemetry.clear();
```

---

## Timing

```java
// Wait (milliseconds)
sleep(1000);  // Wait 1 second

// Time elapsed
ElapsedTime timer = new ElapsedTime();
if (timer.seconds() > 5.0) {
    // 5+ seconds have passed
}
```

---

## OpMode Basics

```java
@TeleOp(name = "MyProgram", group = "Learning")
public class MyRobot extends LinearOpMode {
    
    private DcMotor motor;
    
    @Override
    public void runOpMode() {
        
        // INITIALIZE
        motor = hardwareMap.get(DcMotor.class, "motor");
        telemetry.addData("Status", "Ready");
        telemetry.update();
        
        // WAIT FOR PLAY
        waitForStart();
        
        // MAIN LOOP
        while (opModeIsActive()) {
            motor.setPower(gamepad1.left_trigger);
            telemetry.update();
        }
    }
}
```

---

## Common Patterns

### Motor Control (Trigger)
```java
motor.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
```

### Servo Control (Buttons)
```java
if (gamepad1.a) {
    servo.setPosition(0.0);
} else if (gamepad1.b) {
    servo.setPosition(1.0);
}
```

### Decision Based on Sensor
```java
if (distance < 10) {
    motor.setPower(0.0);  // Stop if too close
} else {
    motor.setPower(0.5);  // Move forward
}
```

---

## Hardware Names (Must Match Configuration!)

```
Motor:          motorLeft, motorRight, motorArm
Servo:          servoGripper, servoArm
Sensor:         distanceSensor, colorSensor, touchSensor
```

**IMPORTANT**: Names are case-sensitive!  
❌ Wrong: "motorleft", "MotorLeft"  
✅ Correct: "motorLeft"

---

## Error Messages & Fixes

| Error | Fix |
|-------|-----|
| `NullPointerException` | Hardware not initialized before use |
| `hardware not found` | Name doesn't match configuration (case-sensitive!) |
| `opModeIsActive() unknown` | Forget to extend `LinearOpMode` |
| Telemetry not showing | Forgot `telemetry.update()` |
| Motor won't stop | Check `opModeIsActive()` in while condition |

---

## Value Ranges Reference

| Device | Range | Notes |
|--------|-------|-------|
| Motor Power | -1.0 to +1.0 | Negative = reverse |
| Servo Position | 0.0 to 1.0 | Maps to 0-180° |
| Trigger | 0.0 to 1.0 | Not pressed = 0, fully = 1 |
| Stick | -1.0 to +1.0 | ⚠️ Y-axis inverted! |
| Distance (cm) | 2 to ~150 | Depends on sensor |

---

## Coding Checklist

Before running code, verify:
- [ ] Hardware initialized: `hardwareMap.get(...)`
- [ ] Hardware names match configuration (case-sensitive)
- [ ] Inside `while (opModeIsActive())` loop
- [ ] Called `telemetry.update()`
- [ ] Proper imports at top of file
- [ ] Code compiles without errors
- [ ] Robot is powered on
- [ ] Gamepad is paired (if using)

---

## Keyboard Shortcuts (VS Code)

| Shortcut | Action |
|----------|--------|
| `Ctrl+Space` | Auto-complete |
| `Ctrl+/` | Comment/uncomment |
| `F5` | Build/Deploy |
| `Ctrl+K Ctrl+0` | Fold all |
| `Ctrl+K Ctrl+J` | Unfold all |

---

## Learning Path

1. **Lesson 1**: Motor basics (forward/reverse/stop)
2. **Lesson 2**: Servo positions (0°, 90°, 180°)
3. **Lesson 3**: Read sensors (distance, color, buttons)
4. **Lesson 4**: Gamepad control (buttons, sticks, triggers)

After these, you're ready for more complex projects!

---

## Need Help?

1. Check the **Lesson README** for concepts
2. Look at **Complete_Solution.java** for examples
3. Review **FTC_PATTERNS_REFERENCE.md** for code patterns
4. Ask your coach or team lead
5. Refer to **LearnJavaForFTC** for deeper learning

---

**Print this card and keep it handy! ✏️**

Last Updated: 2026
