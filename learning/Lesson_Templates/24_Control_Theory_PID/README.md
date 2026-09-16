# Lesson 24 — Introduction to Control Theory & PID

**Book**: Chapter 24 | **Estimated Time**: 3–4 hours | **Hardware Required**: Motor with encoder

---

## What You'll Learn

- The difference between **open loop** and **closed loop** control
- **Bang-bang** control — the simplest closed loop
- **Proportional (P)** control — speed scales with distance from target
- Full **PID(f)** — Proportional + Integral + Derivative + Feedforward
- How to **tune** a PID: P first, then I, then D
- The **integral windup** problem and how to fix it

---

## Open Loop vs. Closed Loop

| | Open Loop | Closed Loop |
|---|---|---|
| Feedback | None | Reads sensor each loop |
| Example | "Run motor 2 seconds" | "Run motor until encoder = 1000" |
| Problem | Inaccurate (varies with load) | More complex to implement |

---

## Bang-Bang Control

```java
if (desiredPosition < actualPosition) {
    motor.setPower(0.5);   // too low — go up
} else {
    motor.setPower(-0.5);  // too high — go down
}
```

Simple, but oscillates around the target. Add a **deadband** to reduce oscillation:

```java
int error = desiredPosition - actualPosition;
if (Math.abs(error) < DEADBAND) {
    motor.setPower(0.0);
} else if (error > 0) {
    motor.setPower(0.5);
} else {
    motor.setPower(-0.5);
}
```

---

## Proportional Control

```java
double kP    = 0.001;   // tune this value
int    error = desiredPosition - actualPosition;
double power = error * kP;
motor.setPower(power);  // power scales with how far away we are
```

Go faster when far away, slower when close. No more sharp oscillation.

---

## Full PIDF Formula

```
power = (kP × error) + (kI × sumErrors) + (kD × derivative) + kF
```

Where:
- `error`      = desiredPosition − actualPosition
- `sumErrors`  = running sum of errors over time (I term)
- `derivative` = (error − lastError) / deltaTime (D term)
- `kF`         = constant to overcome gravity/friction (F term, for vertical mechanisms)

---

## Tuning Order

1. Set kP, kI, kD, kF all to **0**
2. Increase **kP** until the system gets to the target quickly (some overshoot OK)
3. Increase **kI** until steady-state error disappears
4. Increase **kD** until oscillations damp out
5. Add **kF** if the mechanism needs power just to hold position (e.g., vertical slide)

---

## Book Reference

- Chapter 24, sections 24.1–24.7 (pages 185–200)
- Listings 24.1–24.7: Full progression from open loop to built-in PIDF

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L24_PIDControl.java` | Progressive template: bang-bang → P → PID |
| `Complete_Solution.java` | Full PID solution |
| `Exercises.txt` | Tuning challenges |
