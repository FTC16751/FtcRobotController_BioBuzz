# Lesson 12 — State Machines & Autonomous

**Book**: Chapter 12 | **Estimated Time**: 3–4 hours | **Hardware Required**: Programming board (touch sensor + servo + motor)

---

## What You'll Learn

- Why **state machines** are the right pattern for autonomous in FTC
- How to use `enum` to name your states (instead of magic numbers)
- The `switch` statement
- Using `getRuntime()` for timed state transitions
- The `start()` lifecycle method
- Writing `@Autonomous` OpModes

---

## The Core Problem with while Loops in FTC

You might be tempted to write autonomous like this:

```java
// DON'T DO THIS
motor.setPower(0.5);
while (encoder < 1000) {}   // ← freezes everything: no telemetry, no stop button!
motor.setPower(0.0);
```

Instead, use a **state machine** — each call to `loop()` checks the current state
and advances to the next when the condition is met:

```java
enum State { DRIVE, STOP, DONE }
State currentState = State.DRIVE;

public void loop() {
    switch (currentState) {
        case DRIVE:
            motor.setPower(0.5);
            if (encoder >= 1000) { currentState = State.STOP; }
            break;
        case STOP:
            motor.setPower(0.0);
            currentState = State.DONE;
            break;
        default:
            telemetry.addLine("Autonomous complete.");
    }
}
```

The robot stays responsive: telemetry updates, the stop button works, and
the Driver Station can monitor progress via `telemetry.addData("State", currentState)`.

---

## Timed Transitions

Use `getRuntime()` instead of encoder counts when you don't have encoders:

```java
double stateStartTime = 0.0;

// In start():
stateStartTime = getRuntime();

// In loop(), inside a state:
if (getRuntime() >= stateStartTime + 2.0) {  // 2 seconds have passed
    currentState = State.NEXT_STATE;
    stateStartTime = getRuntime();             // reset for next state
}
```

---

## The `start()` Method

`start()` runs **once** when the driver presses PLAY. It's the perfect place to:
- Reset the state to the beginning
- Reset `getRuntime()` via `resetRuntime()`
- Stop streaming vision (from Lesson 16)

---

## Book Reference

- Chapter 12, sections 12.1–12.4 (pages 77–89)
- Listings 12.1: `ToggleOpMode.java` — simple state with a boolean
- Listing 12.2–12.5: `AutoState1–4.java` — full state machine evolution
- Listing 12.6: `AutoTime.java` — timed transitions

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L12_StateMachine.java` | State machine skeleton to complete |
| `Complete_Solution.java` | Full 4-state autonomous solution |
| `Exercises.txt` | Practice challenges |
