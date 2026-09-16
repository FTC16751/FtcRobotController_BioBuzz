# Lesson 10 — Color & Distance Sensors

**Book**: Chapter 10 | **Estimated Time**: 2 hours | **Hardware Required**: REV Color/Range Sensor

---

## What You'll Learn

- How to use `ColorSensor` to detect RGB color values
- How the same hardware (`sensor_color_distance`) serves as **both** a color AND distance sensor
- How to use `DistanceSensor` with `DistanceUnit` to get readings in real units
- Applying sensor data to trigger robot actions

---

## Configuration

Add to `programming_board` config:
- Under **I2C Bus 1**, Port 0: type = `REV Color/Range Sensor`, name = `sensor_color_distance`

---

## Two Interfaces, One Sensor

The REV Color/Range sensor appears **twice** in the hardware map — once as each type:

```java
ColorSensor    colorSensor    = hwMap.get(ColorSensor.class,    "sensor_color_distance");
DistanceSensor distanceSensor = hwMap.get(DistanceSensor.class, "sensor_color_distance");
```

Note: both use the **exact same name** in the config file.

---

## Reading Color

```java
int red   = colorSensor.red();    // 0–255
int green = colorSensor.green();  // 0–255
int blue  = colorSensor.blue();   // 0–255
```

---

## Reading Distance

```java
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

double distCm   = distanceSensor.getDistance(DistanceUnit.CM);
double distInch = distanceSensor.getDistance(DistanceUnit.INCH);
```

Available units: `DistanceUnit.MM`, `CM`, `INCH`, `METER`

---

## Book Reference

- Chapter 10, sections 10.1–10.4 (pages 67–71)
- Listing 10.1: `ProgrammingBoard7.java`
- Listing 10.2: `DistanceColorOpMode.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L10_ColorDistance.java` | Template with TODOs |
| `Complete_Solution.java` | Full solution |
| `Exercises.txt` | Practice challenges |
