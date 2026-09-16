# Lesson 09 — Analog Sensors (Potentiometer)

**Book**: Chapter 9 | **Estimated Time**: 1–2 hours | **Hardware Required**: REV Potentiometer

---

## What You'll Learn

- How **analog sensors** differ from digital ones (a range of values, not just on/off)
- How to use `AnalogInput` to read voltage from a potentiometer
- How to convert voltage to a meaningful unit (degrees) using `Range.scale()`
- Practicing the Mechanism + OpMode pattern

---

## Key Concept: Voltage → Angle

The potentiometer returns a **voltage** between 0V and its maximum voltage.
We use `Range.scale()` to convert it to degrees (0–270 for the REV pot).

```java
// Range.scale(input, inMin, inMax, outMin, outMax)
double angle = Range.scale(pot.getVoltage(), 0, pot.getMaxVoltage(), 0, 270);
```

This says: "My input goes from 0 to maxVoltage. Map that proportionally to 0–270."

---

## Configuration

Add to your `programming_board` config:
- Under **Analog Input Devices**, Port 0: type = `Analog Input`, name = `pot`

---

## Key Classes

```java
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.util.Range;

AnalogInput pot;

// In init():
pot = hwMap.get(AnalogInput.class, "pot");

// Reading:
double voltage  = pot.getVoltage();
double maxVolts = pot.getMaxVoltage();
double angle    = Range.scale(voltage, 0, maxVolts, 0, 270);
```

---

## Book Reference

- Chapter 9, sections 9.1–9.4 (pages 63–66)
- Listing 9.1: `ProgrammingBoard6.java`
- Listing 9.2: `PotOpMode.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L09_Potentiometer.java` | Mechanism + OpMode template |
| `Complete_Solution.java` | Full solution |
| `Exercises.txt` | Practice challenges |
