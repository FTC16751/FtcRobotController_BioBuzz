# Lesson 3: Sensor Reading

## Book Alignment
This lesson is based on the LearnJavaForFTC chapters *Analog Sensors* and *Color and Distance Sensors*. Use the book to learn how sensors report values, how to interpret units, and how to use sensor data in OpModes.
**Book reference**: See the sections labeled "Analog Sensors" and "Color and Distance Sensors" for distance sensor units, conversion examples, and practical sensor usage.

## Learning Objectives
By the end of this lesson, students will be able to:
- Understand common FTC sensors and their purpose
- Initialize sensors using hardwareMap
- Read raw sensor values
- Understand sensor ranges and units
- Use sensor data for decision making (basic)

## Key Concepts

### What is a Sensor?
A sensor is a device that detects physical properties and sends data to the robot controller. Common FTC sensors include:

**Touch Sensor (Digital)**
- Returns true/false (pressed or not)
- Used for limit switches, bumpers
- Units: Boolean

**Distance Sensor (Analog)**
- Measures distance using infrared
- Returns value in centimeters
- Units: cm (typical range 2-150 cm)

**Color Sensor**
- Detects color and light intensity
- Returns RGB values or color name
- Can also read reflectance (brightness)

**Gyroscope/IMU (Inertial Measurement Unit)**
- Measures robot orientation/rotation
- Returns angles in degrees
- Useful for precise autonomous turns

**Motor Encoder**
- Measures how much motor has rotated
- Returns counts or degrees
- Built into DC motors

### Sensor Initialization Pattern
```java
// For a digital/touch sensor
DigitalChannel limitSwitch = hardwareMap.get(DigitalChannel.class, "limitSwitch");

// For a distance sensor
DistanceSensor distanceSensor = hardwareMap.get(DistanceSensor.class, "distanceSensor");

// For color sensor
ColorSensor colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
```

### Reading Sensor Values
```java
// Digital sensor - true/false
boolean isPressed = limitSwitch.getState();  // true if NOT pressed, false if pressed

// Distance sensor - value in cm
double distance = distanceSensor.getDistance(DistanceUnit.CM);

// Color sensor - intensity 0-1
int red = colorSensor.red();
int blue = colorSensor.blue();
```

## Important Notes
- Different sensors return different data types and units
- Always check sensor documentation for value ranges
- Some sensors need warmup time before accurate readings
- Read sensors frequently in your loop for responsive behavior
- Consider sensor noise - average multiple readings if needed

## Summary
Sensors provide information about the robot's environment. Each sensor type has specific methods to read data. Learning to use sensors opens up closed-loop control and autonomous programming.

---
**Related**: Lesson 1 (Motors), Lesson 2 (Servos), Lesson 4 (Gamepads)
