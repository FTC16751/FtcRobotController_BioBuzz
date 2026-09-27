# Lesson 26 — Limelight 3A

**Book**: none | **Estimated Time**: 2 meetings, about 4 hours | **Hardware Required**: Limelight 3A (Part 1: any Control Hub; Part 2: mounted on the StarterBot)

**Do first**: L20 Making Robots Drive and L24 PID (Part 2 is P control).

---

## What You'll Learn

- What a **pipeline** is and how to pick one from code
- Reading a result: **tx**, **ty**, **ta**, and checking `isValid()` first
- Looping over a **List** of detections with a for-each loop
- Turning the robot to face a target with **P control**, using tx as the error

---

## Key Concept: The Limelight Does the Seeing

The Limelight is a camera with its own computer inside. It runs a **pipeline**, a set of settings
that says what to look for (a color, an AprilTag, or balls found by a neural network), and sends
the answer to the Control Hub about 100 times a second. Your code never touches a picture; it only
reads numbers.

The Limelight holds up to 10 pipelines, numbered 0–9. You set them up in its web page and pick one
from code with `limelight.pipelineSwitch(n)`.

```java
limelight = hardwareMap.get(Limelight3A.class, "limelight");
limelight.pipelineSwitch(0);
limelight.start();                              // no start(), no results

LLResult result = limelight.getLatestResult();
if (result != null && result.isValid()) {       // not valid = no target in view
    double tx = result.getTx();
}
```

## The Three Numbers

Fill in the last column during TODO 4:

| Number | Meaning | Unit | What I saw |
|---|---|---|---|
| **tx** | how far the target is left/right of the center of the picture | degrees | target moves right → tx goes ______ |
| **ty** | how far the target is above/below the center | degrees | target moves up → ty goes ______ |
| **ta** | how much of the picture the target fills | % of the image | target comes closer → ta goes ______ |

tx = 0 means the target is straight ahead. That is what Part 2 aims for.

---

## Key Concept: Lists and for-each

A pipeline can see several things at once, so `getDetectorResults()` gives back a **List**: a row
of results you can ask the size of and walk through one at a time.

```java
List<LLResultTypes.DetectorResult> balls = result.getDetectorResults();
telemetry.addData("Balls seen", balls.size());
for (LLResultTypes.DetectorResult ball : balls) {     // "for each ball in balls"
    telemetry.addData(ball.getClassName(), ball.getConfidence());
}
```

This season's ball detector knows three classes: `yellow_pollen`, `red_nectar`, `blue_nectar`.

---

## Setup (coach, before the first meeting)

1. **Config**: the Limelight appears in the robot config as its own USB device. Name it **`limelight`**.
2. **Web page**: join the robot's Wi-Fi from a laptop and open `http://172.29.0.1:5801`.
3. **Pipelines**: set up the pipeline the lesson will use and note its number in `PIPELINE`.
   - To start, any pipeline with a target you can hold works: a **color** pipeline on a ball, or an
     **AprilTag** pipeline on a printed tag. TODOs 1–5 and all of Part 2 work with it.
   - TODOs 6–7 need the **neural detector** pipeline with the Hive-Vision ball model
     (see `doc/VISION_TWO_CAMERAS.md`). If it is not loaded yet, skip them for now.
4. **Mounting (Part 2)**: the team's plan is the Limelight on the front, tilted down about 30°,
   looking at balls on the floor.

---

## Part 2: Turn to Face the Target

This is Lesson 24 again. The error is how far off target the robot is; the correction is that
error times kP:

```java
double tx = result.getTx();                          // error, in degrees
double turn = Range.clip(kP * tx, -MAX_TURN, MAX_TURN);
if (Math.abs(tx) < TOLERANCE_DEG) turn = 0;          // close enough: stop wiggling
```

A target to the right is a positive tx, and a positive `turn` turns right, so the signs already agree.

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L26_Limelight.java` | Part 1 (meeting 1): read tx/ty/ta and the list of balls |
| `Template_L26_AimAtTarget.java` | Part 2 (meeting 2): hold a bumper to turn toward the target |
| `Complete_Solution_Limelight.java` | Part 1 solution |
| `Complete_Solution_AimAtTarget.java` | Part 2 solution |
| `Exercises.txt` | Practice challenges |

The team's full version is `common/vision/VisionUtil.java`; the Limelight's own example is
`samples/SensorLimelight3A.java`.

---

## Common Mistakes

- **Forgetting `start()`** — every result is empty
- **Not checking `isValid()`** — tx reads 0 when nothing is seen, which looks like "perfectly lined up"
- **Wrong pipeline number** — the web page shows the target, your code does not; compare `PIPELINE` with the web page
- **Robot wobbles while aiming** — kP too big (TODO 5)
- **Robot stops short of lined up** — kP too small, or the tolerance too big
