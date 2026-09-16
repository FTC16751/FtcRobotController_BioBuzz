# Lesson 14 — Inheritance & Polymorphism

**Book**: Chapter 14 | **Estimated Time**: 2–3 hours | **Hardware Required**: Any hardware available

---

## What You'll Learn

- How `extends` lets a class inherit all of another class's behavior
- `super()` — calling the parent constructor
- `@Override` — replacing a parent method with your own version
- `abstract` classes and methods — a contract that child classes must fulfill
- The real-world FTC use case: a `TestWiring` OpMode powered by polymorphism

---

## The Basics

```java
// Parent class
public class Animal {
    public void speak() {
        telemetry.addLine("...");
    }
}

// Child class — inherits everything from Animal
public class Dog extends Animal {
    @Override
    public void speak() {
        telemetry.addLine("Woof!");   // replaces parent's speak()
    }
}
```

A `Dog` IS-A `Animal`, so you can use it anywhere an Animal is expected.

---

## abstract — A Contract

```java
abstract public class TestItem {
    private String description;

    protected TestItem(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    // No body — subclasses MUST implement this
    abstract public void run(boolean on, Telemetry telemetry);
}
```

- `abstract class` — cannot be instantiated directly
- `abstract method` — no body; child classes must provide one
- `protected` — visible to this class and its children (but not unrelated classes)

---

## The TestWiring Pattern (Book's Real Example)

The book builds a hardware tester using this pattern:
1. Abstract `TestItem` base class with `run(boolean on, Telemetry telemetry)`
2. Concrete subclasses: `TestMotor`, `TestAnalogInput`, etc.
3. An `ArrayList<TestItem>` that holds any mix of them
4. A single `TestWiring` OpMode that works for ALL hardware types

This is **polymorphism** — `currTest.run(...)` calls the right method for
whichever subclass `currTest` happens to be.

---

## Book Reference

- Chapter 14, sections 14.1–14.3 (pages 95–104)
- Listings 14.4–14.8: `TestItem`, `TestMotor`, `TestAnalogInput`, `ProgrammingBoard9`, `TestWiring`

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_TestItem.java` | Abstract base class to complete |
| `Template_TestMotor.java` | Concrete motor test subclass |
| `Template_TestDigitalChannel.java` | Concrete touch sensor test subclass |
| `Template_TestWiring.java` | OpMode that uses all test items |
| `Complete_Solution_*.java` | Solutions for each file |
| `Exercises.txt` | Practice challenges |

> This lesson has **4 template files**. Complete them in order.
