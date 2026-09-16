# Lesson 06 — First Hardware (Touch Sensor)

**Book**: Chapter 6 | **Estimated Time**: 2 hours | **Hardware Required**: Control Hub + REV Touch Sensor

---

## What You'll Learn

- How to create a **configuration file** on the Control Hub
- What **HardwareMap** is and why every hardware class needs it
- The **Mechanism + OpMode split** pattern (used in every hardware lesson from here on)
- How to use `DigitalChannel` and why the touch sensor logic is **inverted**

---

## Before You Write Any Code — Configuration File

The Control Hub needs to know what hardware is plugged in before your code can
talk to it. You create a "configuration file" directly on the hub.

### Steps (do this on the Driver Station or the Robot Controller app):
1. Tap the three dots (⋮) in the upper right
2. Tap **Configure Robot** → **New**
3. The hub should appear — tap **Expansion Hub 2** (or Control Hub)
4. Tap **Digital Devices**
5. On Port **1**, change type to **Digital Channel**
6. Set the name to: `touch_sensor`  ← **This must match your code exactly**
7. Done → Done → Done → **Save** → name it `programming_board` → OK
8. Tap **Activate** → restart the robot

> **Critical**: The string you use in `hwMap.get(...)` must match the name in the
> config file **exactly**, including capitalization and underscores.

---

## Key Classes

### DigitalChannel
A digital sensor that is either ON or OFF (like a button or limit switch).

```java
DigitalChannel touchSensor;

// In init():
touchSensor = hwMap.get(DigitalChannel.class, "touch_sensor");
touchSensor.setMode(DigitalChannel.Mode.INPUT);

// Reading it:
boolean state = touchSensor.getState();
// WARNING: getState() returns FALSE when pressed, TRUE when not pressed!
// (The hardware logic is inverted)
```

### Why is it Inverted?
The REV Touch Sensor uses a "pull-up resistor" circuit where unpressed = high voltage (true)
and pressed = grounded (false). We hide this weirdness inside our Mechanism class.

---

## The Mechanism + OpMode Pattern

```
L06_ProgrammingBoard.java    ← Mechanism: knows HOW to talk to hardware
    - init(HardwareMap)
    - isTouchSensorPressed()  ← hides the inverted logic

L06_TouchSensorOpMode.java   ← OpMode: knows WHAT to do, not HOW
    - board.init(hardwareMap)
    - if (board.isTouchSensorPressed()) { ... }
```

The OpMode never directly touches `DigitalChannel` — that's all hidden in the Mechanism.

---

## Book Reference

- Chapter 6, sections 6.1–6.5 (pages 41–47)
- Listings 6.1: `ProgrammingBoard1.java`
- Listing 6.2: `TouchSensorOpMode.java`
- Listing 6.3–6.4: `ProgrammingBoard2.java` (fixed inverted logic)

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L06_ProgrammingBoard.java` | Mechanism class — hardware init + methods |
| `Template_L06_TouchSensorOpMode.java` | OpMode that uses the mechanism |
| `Complete_Solution_Board.java` | Mechanism solution |
| `Complete_Solution_OpMode.java` | OpMode solution |
| `Exercises.txt` | Practice challenges |

---

## Common Mistakes

- **Wrong config name** — `"touch_sensor"` vs `"touchSensor"` — they won't match
- **Forgetting `setMode(INPUT)`** — the channel won't work without it
- **Inverted logic** — `getState()` is `false` when pressed; your `isTouchSensorPressed()` should invert it
