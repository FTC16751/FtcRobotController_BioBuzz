# Lesson 02 — Variables & Data Types
**Book**: Chapter 2 | **Estimated Time**: 1–2 hours | **Hardware Required**: None

---

## What You'll Learn

- The 8 **primitive data types** in Java (and which 3 matter most for FTC)
- How to **declare** and **initialize** variables
- What **scope** means (class-level vs. inside a method)
- How to use **String** for text
- Naming conventions: **camelCase** for variables, **PascalCase** for classes

---

## The Three Types You'll Use Most

| Type | What it holds | Example | FTC Use |
|------|--------------|---------|---------|
| `int` | Whole numbers | `int ticks = 1120;` | Encoder counts, loop counters |
| `double` | Decimal numbers | `double power = 0.75;` | Motor power, distances |
| `boolean` | `true` or `false` | `boolean isPressed = false;` | Sensor states, flags |

### The Other 5 (good to know)
```java
byte    // -128 to 127
short   // rarely used in FTC
long    // big integers (encoder counts on long runs)
float   // smaller decimals (use double instead)
char    // single character: 'A'
```

---

## Declaring Variables

```java
// Pattern:  type  name;
int teamNumber;
double motorPower;
boolean touchSensorPressed;

// Pattern:  type  name = initialValue;   (declare + assign at once)
int teamNumber = 17651;
double motorPower = 0.5;
boolean touchSensorPressed = true;
```

> **Important**: If you declare a variable without giving it a value, Java sets it to:
> - `0` for numbers
> - `false` for boolean

---

## Class Members vs. Local Variables

```java
public class MyOpMode extends OpMode {

    int loopCount = 0;          // CLASS MEMBER — lives as long as the OpMode
                                //   every method in this class can see it

    @Override
    public void loop() {
        int tempValue = 42;     // LOCAL VARIABLE — only exists inside loop()
                                //   disappears when loop() finishes
        loopCount++;            // OK: class member is still visible here
    }
}
```

**Rule**: Use class members for anything that needs to survive between calls to `loop()`.

---

## String — Text Data

`String` is special — it's actually a class (notice the capital S).

```java
String teamName = "Playful Lobsters";
String message = "Hello " + teamName;    // + joins strings together
```

---

## Naming Conventions

| Thing | Convention | Example |
|-------|-----------|---------|
| Variable | camelCase | `motorPower`, `loopCount` |
| Class | PascalCase | `MyOpMode`, `RobotStatus` |
| Constant | ALL_CAPS | `MAX_SPEED`, `TEAM_NUMBER` |

---

## Book Reference
- Chapter 2, sections 2.1–2.4 (pages 13–16)
- Listings 2.1: `PrimitiveTypes.java`
- Listing 2.2: `UseString.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_Variables.java` | Your starting point — fill in the TODOs |
| `Complete_Solution.java` | Full working solution (coaches only!) |
| `Exercises.txt` | Extra practice challenges |
