# FTC Learning System: Hardware Setup Guide

This guide helps coaches and team leads prepare hardware for each lesson.

## Pre-Lesson Checklist

### For ALL Lessons
- [ ] Robot Controller (Control Hub) powered on and connected
- [ ] Driver Station app on phone/tablet
- [ ] USB cable from PC to robot (for downloading code)
- [ ] Battery in gamepad(s)
- [ ] Telemetry viewing capability (phone or computer screen)

---

## Lesson 1: Motor Basics - Hardware Setup

### Required Hardware
- 1x DcMotor (any type: 12V, gearbox ratio doesn't matter for learning)
- Robot Controller configured and ready

### Hardware Configuration Steps
1. Open **FTC Robot Controller** app on the Control Hub
2. Tap **Configuration**
3. Select **Create New**
4. Choose **Configuration Type**: "Expansion Hub"
5. **Add a Motor**:
   - Click **Add Device**
   - Select **DcMotor**
   - Port: `Motor 0` (or any available motor port)
   - Name: `motorTest` (EXACTLY this, case-sensitive!)
   - **DO NOT** configure as encoder yet
6. Click **Save Configuration**
7. Name file: `Lesson1_MotorBasics`
8. Confirm it's active (green checkmark)

### Test Procedure
1. Deploy `Lesson1_MotorBasics_Solution.java` to robot
2. Select the OpMode from Driver Station
3. Press INIT, then PLAY
4. Monitor telemetry - motor power should cycle: 1.0 → -1.0 → 0.0
5. **Listen/feel** for motor spinning forward, backward, then stopping
6. **Note**: Motor will cycle through this pattern repeatedly while OpMode is active

### Common Setup Issues
| Issue | Solution |
|-------|----------|
| Motor doesn't appear in config | Check USB connection to motor port |
| Name doesn't save | Ensure exactly `motorTest` (lowercase t) |
| Motor shows but won't spin | Check battery power, motor connection |
| Multiple configs showing | Delete old ones, keep only active |

---

## Lesson 2: Servo Control - Hardware Setup

### Required Hardware
- 1x Servo (standard PWM servo, 0-180° range)
- Robot Controller with motor config from Lesson 1

### Hardware Configuration Steps
1. Open **FTC Robot Controller** app, tap **Configuration**
2. Edit existing config from Lesson 1 (or create new)
3. **Add a Servo**:
   - Click **Add Device**
   - Select **Servo**
   - Port: `Servo 0` or `Servo 1` (any available servo port)
   - Name: `servoTest` (EXACTLY this, case-sensitive!)
4. Save configuration: `Lesson2_ServoControl`

### Test Procedure
1. Deploy `Lesson2_ServoControl_Solution.java`
2. Press INIT, then PLAY
3. Telemetry shows target positions (0.0, 0.5, 1.0)
4. **Watch** servo move to three positions
5. Should move smoothly with ~0.5-1 second between positions

### Servo Calibration
If servo movement doesn't match expected angles:
1. Move servo manually to 0°, 90°, 180° positions
2. Note any offset or scaling issues
3. May need to adjust in mechanism code

---

## Lesson 3: Sensor Reading - Hardware Setup

### Required Hardware
- 1x Distance Sensor (REV 2m Distance Sensor or similar)
- Motor + Servo from previous lessons (optional, for exercises)

### Hardware Configuration Steps
1. Open **FTC Robot Controller**, edit or create config
2. **Add Distance Sensor**:
   - Click **Add Device**
   - Select **DistanceSensor**
   - Port: Any I2C port (check your hub configuration)
   - Name: `distanceSensor` (EXACTLY this, case-sensitive!)
3. Save configuration: `Lesson3_SensorReading`

### Test Procedure
1. Deploy `Lesson3_SensorReading_Solution.java`
2. Press INIT, then PLAY
3. **Move your hand** in front of sensor
4. Telemetry should show changing distance (in cm)
5. Test range: 2 cm to ~150 cm

### Sensor Testing
- Close distance (hand 5cm away): should show ~5.0 cm
- Far distance (hand 50cm away): should show ~50.0 cm
- At maximum range: may show max value (~255)

### Troubleshooting Sensors
| Symptom | Check |
|---------|-------|
| Sensor always reads 0 | Check I2C connection, address in config |
| Sensor always reads max | Nothing in range, or lens is dirty |
| Readings are very noisy | Average multiple readings, increase delays |
| Sensor won't initialize | Try restarting Control Hub app |

---

## Lesson 4: Gamepad Control - Hardware Setup

### Required Hardware
- Motor + Servo from previous lessons
- 1-2 Gamepads with charged batteries

### Hardware Configuration Steps
1. **Gamepad Configuration** (in Driver Station app, NOT Robot Controller):
   - On the Driver Station phone/tablet
   - Settings → Gamepad Controller Options
   - Verify gamepads are paired and showing
   - No special configuration needed in Robot Controller

2. **Code Configuration**:
   - Lesson 4 code uses existing motor + servo from Lessons 1-2
   - Use same hardware config from previous lessons
   - Name file: `Lesson4_GamepadControl`

### Test Procedure
1. Deploy `Lesson4_GamepadControl_Solution.java`
2. Press INIT on Driver Station
3. Check Driver Station telemetry shows gamepad inputs
4. Press PLAY
5. **Test Controls**:
   - Pull Left Trigger: Motor should spin
   - Press A Button: Servo should move to 0.0
   - Press B Button: Servo should move to 1.0
   - Telemetry shows trigger value and servo position

### Gamepad Troubleshooting
| Issue | Solution |
|-------|----------|
| Buttons not responding | Check gamepad connection in Driver Station |
| Battery low | Replace batteries in gamepad |
| Stick input jerky | Check for dead zone issues in code |
| Wrong gamepad detected | Restart Driver Station, reconnect gamepad |

---

## Multi-Lesson Robot Configuration

### Recommended Final Setup
A single robot with this configuration works for ALL lessons:

```
Hardware Items:
- 2x DcMotor (motorLeft, motorRight)
- 2x Servo (servoTest, servoArm)  
- 1x DistanceSensor
- 1-2x Gamepad

Configuration File: "CompleteLearningSetup"

Device Mapping:
Motor 0: motorLeft
Motor 1: motorRight
Servo 0: servoTest
Servo 1: servoArm
I2C Port: distanceSensor
```

This allows students to use the same robot for all 4 lessons without reconfiguration.

---

## Tips for Coaches

### Setup Strategy #1: Shared Robot
- **Pros**: One robot serves all lessons
- **Cons**: Limited to one student/group at a time
- **Best for**: Small teams or rotating practice

### Setup Strategy #2: Multiple Practice Robots
- **Pros**: Multiple students can practice simultaneously
- **Cons**: More hardware and configuration needed
- **Best for**: Larger teams, parallel instruction

### Setup Strategy #3: Hybrid Approach
- **Shared competition robot** for practice
- **Simpler practice robots** for Lessons 1-2 (just motor+servo)
- **Dedicated sensor robot** for Lesson 3
- **Full robot** for Lesson 4

---

## Hardware Recommendations

### Motors
- **REV HD Hex Motor** (most common in FTC)
- **REV Core Hex Motor** (lighter, smaller)
- Any 12V brushed DC motor works for learning

### Servos
- **REV Standard Servo** (standard 0-180°)
- **REV Continuous Rotation Servo** (not needed for these lessons)

### Sensors
- **REV 2m Distance Sensor** (recommended, I2C)
- **Modern Robotics Distance Sensor** (alternative)
- Other distance sensors with I2C support work

### Gamepads
- **Xbox 360 Controller** (Bluetooth)
- **Logitech Controller** (Bluetooth)
- Any standard Bluetooth gamepad

---

## Configuration File Management

### Best Practices
1. **Name configs descriptively**: `Lesson1_MotorBasics`, `Lesson2_ServoControl`
2. **Save multiple versions**: Don't overwrite old configs
3. **Document in comments**: Add what devices are connected
4. **Test after config changes**: Always verify devices respond

### Backing Up Configurations
1. From Control Hub settings, export configuration
2. Save to cloud or external storage
3. Can reload if configuration corrupted

---

## Pre-Lesson Coaching Preparation

### 24 Hours Before Lesson
- [ ] Test all hardware works and responds
- [ ] Verify configuration names match lesson code
- [ ] Test sample solution code compiles
- [ ] Practice deploying to robot
- [ ] Check all batteries charged
- [ ] Have backup code ready

### 1 Hour Before Lesson
- [ ] Verify robot boots up and shows configuration
- [ ] Check telemetry displays properly
- [ ] Ensure gamepads connected (if Lesson 4)
- [ ] Have printouts of lesson README
- [ ] Test one student's environment setup

---

## Quick Hardware Check
Run this before each lesson:
```
[ ] Robot Controller boots and shows configuration?
[ ] Correct configuration file active?
[ ] Can compile and deploy sample code?
[ ] Does device respond to simple command?
[ ] Is telemetry visible on Driver Station?
[ ] Is battery charged?
```

If all ✓, you're ready to start!

---

## Emergency Contact Points

**Code won't compile:**
- Check imports match your SDK version
- Verify hardware names exactly match config

**Robot won't respond:**
- Is it running OpMode after PLAY?
- Check battery power and connections
- Try restarting Control Hub

**Telemetry not showing:**
- Did you call `telemetry.update()`?
- Check telemetry tab on Driver Station

---

**Last Updated**: May 2026  
**For**: FTC Team 17651 Learning System
