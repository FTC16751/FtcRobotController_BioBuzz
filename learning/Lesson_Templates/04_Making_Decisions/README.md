# Lesson 04 — Making Decisions
**Book**: Chapter 4 | **Estimated Time**: 1–2 hours | **Hardware Required**: Gamepad only

---

## What You'll Learn

- `if`, `else if`, `else` — choose different code paths
- Comparison operators (`==`, `!=`, `<`, `>`, `<=`, `>=`)
- Logical operators (`&&`, `||`, `!`) — combine conditions
- `while` loops (and why to be careful with them inside `loop()`)
- `for` loops

---

## if / else if / else

```java
if (someCondition) {
    // runs when condition is TRUE
} else if (anotherCondition) {
    // runs when first is false but this is TRUE
} else {
    // runs when ALL conditions above are false
}
```

**Always use curly braces `{}`** — even for single-line bodies. Skipping them is a
common source of hard-to-find bugs.

---

## Comparison Operators

| Operator | Meaning | Example |
|----------|---------|---------|
| `==` | equals | `loopCount == 10` |
| `!=` | not equals | `state != "DONE"` |
| `<` | less than | `power < 0.5` |
| `>` | greater than | `distance > 30` |
| `<=` | less than or equal | `position <= 100` |
| `>=` | greater than or equal | `encoder >= 500` |

> **Common mistake**: Using `=` (assignment) instead of `==` (comparison) in an if condition.

---

## Logical Operators

```java
// AND — both must be true
if (gamepad1.a && !wasAPressed) { ... }

// OR — at least one must be true
if (distance < 10 || touchPressed) { ... }

// NOT — flips true/false
if (!isFinished) { ... }
```

---

## while Loop — Use Carefully in FTC!

```java
// Fine OUTSIDE an OpMode loop()
while (angle > 180) {
    angle -= 360;
}

// DANGER — do NOT write this inside loop()!
// It blocks everything else, including gamepad updates.
while (gamepad1.a) {   // ← WRONG — freezes the robot
    motor.setPower(1.0);
}
```

In FTC, use `loop()` itself as your "while loop" — it already runs continuously.

---

## for Loop

```java
// for (starting value; keep going while; do this each time)
for (int i = 0; i < 4; i++) {
    // this block runs 4 times (i = 0, 1, 2, 3)
}
```

---

## Book Reference
- Chapter 4, sections 4.1–4.6 (pages 21–27)
- Listings 4.1–4.4

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_Decisions.java` | Starter code with TODOs |
| `Complete_Solution.java` | Full solution (coaches only!) |
| `Exercises.txt` | Extra practice challenges |
