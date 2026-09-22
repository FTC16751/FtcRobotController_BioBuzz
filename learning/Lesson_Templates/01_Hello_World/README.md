# Lesson 01 — Hello World & OpMode Structure
**Book**: Chapter 1 | **Estimated Time**: 1–2 hours | **Hardware Required**: None

---

## What You'll Learn

This is your very first FTC program. You'll learn:
- What an **OpMode** is (a program the robot can run)
- The **lifecycle methods** — the 5 special methods every OpMode can have
- How to send messages to the **Driver Station** using telemetry
- The difference between `@TeleOp` and `@Autonomous`

---

## Key Concepts

### What is an OpMode?
An OpMode is a Java class that FTC's SDK recognizes as a program for your robot.
It shows up on the Driver Station phone/tablet so drivers can select and run it.

### The 5 Lifecycle Methods

| Method | When it runs | Required? |
|--------|-------------|-----------|
| `init()` | Once when INIT is pressed | **YES** |
| `init_loop()` | Repeatedly between INIT and PLAY | no |
| `start()` | Once when PLAY is pressed | no |
| `loop()` | Repeatedly while OpMode is running | **YES** |
| `stop()` | Once when STOP is pressed | no |

> **Rule of thumb**: Put setup code in `init()`. Put repeated robot logic in `loop()`.
> `loop()` runs about 50 times per second!

### Annotations
```java
@TeleOp()          // Shows up in the TeleOp list on Driver Station
@Autonomous()      // Shows up in the Autonomous list
@Disabled          // Compiles but does NOT show up on Driver Station (useful for WIP)
```

### Telemetry — Sending Data to the Driver Station
```java
telemetry.addData("Label", value);   // Shows "Label : value" on screen
telemetry.addLine("any text");       // Shows a plain text line
```
Telemetry updates automatically in `loop()`. In `init()` you may need to call
`telemetry.update()` to force a refresh.

### getRuntime()
`getRuntime()` returns a `double` — the number of seconds since the OpMode started.
It resets when you press INIT.

---

## Book Reference
- Chapter 1, sections 1.2–1.7 (pages 2–10)
- Listing 1.1: `HelloWorld.java`
- Listing 1.2: `HelloWorldCommented.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_HelloWorld.java` | Your starting point — fill in the TODOs |
| `Complete_Solution.java` | Full working solution (coaches only until you're done!) |
| `Exercises.txt` | Extra practice challenges |

---

## Common Mistakes

- **Forgetting `@TeleOp()`** — your OpMode won't appear on the Driver Station
- **Putting a `while` loop inside `loop()`** — this freezes the robot
- **Missing semicolons** — every statement ends with `;`
- **Wrong capitalization** — Java is case-sensitive: `telemetry` ≠ `Telemetry`

---

## Before You Start

1. Open Android Studio
2. Navigate to your package: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/students/<yourname>/`
3. Right-click → New → Java Class
4. Name it `L01_HelloWorld` (must match the class name inside the file exactly)
5. Copy the template code into your new file
6. Fix the first line so it reads `package org.firstinspires.ftc.teamcode.students.<yourname>;`
7. Put your name in the annotation: `@TeleOp(name = "L01 Hello World - <yourname>", group = "<yourname>")`
