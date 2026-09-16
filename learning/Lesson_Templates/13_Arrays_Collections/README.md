# Lesson 13 — Arrays & Collections

**Book**: Chapter 13 | **Estimated Time**: 1–2 hours | **Hardware Required**: None (optional: motors)

---

## What You'll Learn

- How to declare and use **arrays** (fixed-size lists)
- Array **indexing** starts at 0
- The **for-each** loop — the clean way to iterate an array
- `ArrayList` — a list that can grow and shrink
- Common ArrayList methods: `add()`, `get()`, `size()`, `clear()`

---

## Arrays

```java
// Declaring an array of 4 doubles
double[] motorPowers = new double[4];

// Assigning values by index
motorPowers[0] = 0.5;   // first element (index 0)
motorPowers[3] = -0.5;  // fourth element (index 3)

// Declare and initialize at once
String[] directions = {"FORWARD", "BACKWARD", "LEFT", "RIGHT"};

// Length
int count = motorPowers.length;  // 4

// For-each loop — cleaner than a regular for loop
for (double power : motorPowers) {
    telemetry.addData("Power", power);
}
```

---

## ArrayList

```java
import java.util.ArrayList;

ArrayList<String> steps = new ArrayList<>();

steps.add("Move forward");       // add to end
steps.add("Turn right");
steps.add("Stop");

steps.get(0);     // "Move forward"
steps.size();     // 3
steps.clear();    // empties the list
```

> Arrays are fixed-size. Use `ArrayList` when the number of items might change.

---

## Book Reference

- Chapter 13, sections 13.1 (pages 91–93)
- Listing 13.1: `ArrayOpMode.java`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L13_Arrays.java` | Template with TODOs |
| `Complete_Solution.java` | Full solution |
| `Exercises.txt` | Practice challenges |
