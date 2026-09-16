# Lesson 05 — Classes, Members & Methods
**Book**: Chapter 5 | **Estimated Time**: 2–3 hours | **Hardware Required**: None

---

## What You'll Learn

- The difference between **class members** (fields) and **local variables**
- Writing your own **methods** with parameters and return values
- `private` vs. `public` — who can see what
- **Constructors** — methods that run when an object is created
- The **Mechanism + OpMode** split pattern you'll use for ALL real hardware

---

## Class Members vs. Local Variables (Review)

```java
public class MyOpMode extends OpMode {
    double motorPower = 0.0;    // CLASS MEMBER — survives between loop() calls

    public void loop() {
        double temp = 42.0;     // LOCAL VARIABLE — gone when loop() ends
        motorPower += 0.01;     // class member still visible here
    }
}
```

---

## Writing Your Own Methods

```java
// Pattern:
//   returnType  methodName(paramType paramName, ...) { ... }

double clamp(double value, double min, double max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
}

// Calling it:
double safePower = clamp(gamepad1.left_stick_y, -0.5, 0.5);
```

### Return Types
- `void` — method doesn't give anything back
- `double`, `int`, `boolean`, `String` — method returns that type
- Use `return value;` to send a value back to the caller

---

## private vs. public

```java
public class RobotStatus {
    private double motorPower;   // only THIS class can touch it directly

    public void setMotorPower(double power) {   // anyone can call this
        motorPower = clamp(power, -1.0, 1.0);  // but we validate inside
    }

    public double getMotorPower() {
        return motorPower;
    }
}
```

**Rule of thumb**: Make everything `private` by default. Only `public` what other code
needs to call. This protects your class from being used incorrectly.

---

## Constructors

A constructor has the same name as the class and no return type.
It runs automatically when you write `new ClassName(...)`.

```java
public class RobotStatus {
    private String robotName;
    private double maxSpeed;

    public RobotStatus(String name, double speed) {   // constructor
        this.robotName = name;   // 'this.' distinguishes the field from the parameter
        this.maxSpeed  = speed;
    }
}

// Using it:
RobotStatus robot = new RobotStatus("Lobster Bot", 0.75);
```

---

## The Mechanism + OpMode Pattern

From lesson 06 onwards, EVERY hardware lesson uses this split:

```
RobotHardware.java       ← the Mechanism class (handles hardware details)
    init(HardwareMap)    ← sets up hardware
    doSomething()        ← public methods OpMode can call

MyOpMode.java            ← the OpMode (knows NOTHING about hardware internals)
    RobotHardware robot = new RobotHardware();
    robot.init(hardwareMap);
    robot.doSomething();
```

This lesson builds the pure-Java foundation before adding real hardware.

---

## Book Reference
- Chapter 5, sections 5.1–5.6 (pages 29–40)
- Listings 5.1–5.5: `ClassMemberOpMode`, `ClassMethodOpMode`, `RobotLocation`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_RobotStatus.java` | The Mechanism class to complete |
| `Template_Classes.java` | The OpMode that uses it |
| `Complete_Solution_RobotStatus.java` | Solution for the mechanism |
| `Complete_Solution_Classes.java` | Solution for the opmode |
| `Exercises.txt` | Extra practice |

> **Note**: This lesson has TWO template files — one for the mechanism class
> and one for the OpMode. Both must be in your TeamCode folder.
