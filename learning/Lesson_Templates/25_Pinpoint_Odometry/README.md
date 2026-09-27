# Lesson 25 — Pinpoint Odometry

**Book**: none (the book stops before odometry) | **Estimated Time**: 2 meetings, about 4 hours | **Hardware Required**: StarterBot with a goBILDA Pinpoint and two 4-bar odometry pods

**Do first**: L12 State Machines and L20 Making Robots Drive. Exercise 3 needs L24 PID.

---

## What You'll Learn

- What **odometry** is: tracking the robot's position by measuring how far unpowered wheels roll
- The robot's **pose**: X, Y and Heading, and which way each one counts
- Setting up a **goBILDA Pinpoint**: pod offsets, pod type, pod directions
- Why `update()` must run every loop
- Driving to a **position** in autonomous instead of for a number of seconds

---

## Key Concept: The Pose

Odometry pods are small wheels that are not driven by a motor. They are pressed against the floor
and roll whenever the robot moves, and an encoder on each one counts how far. The Pinpoint is a
small computer that reads both pods and its own gyro, and works out where the robot is.

The robot's **pose** is three numbers, all measured from where the robot was when it was reset:

| | Meaning | Goes UP when the robot... |
|---|---|---|
| **X** | forward/back distance | drives **forward** |
| **Y** | sideways distance | drives **left** |
| **Heading** | which way it faces | turns **left** (counter-clockwise) |

"Left is positive" surprises everyone once. It is the math convention, and every FTC library uses it.

```java
pinpoint.update();                           // read the pods; do this EVERY loop
Pose2D pose = pinpoint.getPosition();
double x       = pose.getX(DistanceUnit.INCH);
double y       = pose.getY(DistanceUnit.INCH);
double heading = pose.getHeading(AngleUnit.DEGREES);   // −180 to +180, like the IMU
```

---

## Setting Up the Pinpoint

The Pinpoint needs to know three things about your robot before its numbers mean anything.

**1. Where the pods are (offsets).** Measure from the center of the robot, in millimeters:

- **X pod offset**: how far **left** of center the forward-rolling pod is. Right of center is negative.
- **Y pod offset**: how far **forward** of center the sideways-rolling pod is. Behind center is negative.

Wrong offsets do not matter while driving straight. They show up when you **turn in place**: X and
Y should stay near 0, and with wrong offsets they wander off.

**2. What kind of pods.** The team uses goBILDA 4-bar pods:
`GoBildaOdometryPods.goBILDA_4_BAR_POD`.

**3. Which way each pod counts.** Start with `FORWARD, FORWARD` and fix them with the checks below.

### The Three Checks (do these every time a pod is moved or re-plugged)

Run Part 1, keep the robot still until PLAY, then push it by hand:

1. Push it **forward** 24 in (use a tape measure) → X reads about **+24**. Negative? Set the X pod to `REVERSED`.
2. Push it **left** 24 in → Y reads about **+24**. Negative? Set the Y pod to `REVERSED`.
3. Turn it **left** a quarter turn → Heading reads about **+90**, and X and Y stay within about an inch. X or Y wandering means the offsets are wrong.

Write the working offsets and directions in your `PROGRESS.md`. Part 2 needs them.

---

## Configuration

Add to the StarterBot's config: the Pinpoint on an **I2C port**, type **goBILDA Pinpoint Odometry
Computer**, named **`odo`**. The drive motors are already named `Front_Left`, `Front_Right`,
`Rear_Left`, `Rear_Right`.

---

## Part 2: Drive by Position

Lesson 12's states ended after a number of seconds. Now a state ends when the robot **gets
somewhere**:

```java
case DRIVE_FORWARD:
    drive(DRIVE_POWER, 0, 0);
    if (x >= TARGET_X_IN) {
        drive(0, 0, 0);
        state = State.STRAFE_LEFT;
    }
    break;
```

That is **bang-bang** control from Lesson 24: full speed, then stop. The robot keeps rolling after
the power goes to zero, so it always ends up a bit past the target. TODO 5 has you measure by how
much; Exercise 3 fixes it with P control.

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L25_PinpointPose.java` | Part 1 (meeting 1): set up the Pinpoint and read the pose |
| `Template_L25_PinpointAuto.java` | Part 2 (meeting 2): a state machine that drives to positions |
| `Complete_Solution_PinpointPose.java` | Part 1 solution |
| `Complete_Solution_PinpointAuto.java` | Part 2 solution |
| `Exercises.txt` | Practice challenges |

The team's full version of this lives in `common/drive/DriveUtil.java` (`driveTo`) and
`common/drive/PinpointPIDLoop.java`. goBILDA's own example is `samples/SensorGoBildaPinpoint.java`.

---

## Common Mistakes

- **Forgetting `pinpoint.update()`** — the pose never changes from 0, 0, 0
- **Moving the robot during init** — `resetPosAndIMU()` calibrates the gyro; a bump makes the heading drift all match
- **Pods not touching the floor** — X or Y stops counting; watch the raw ticks (TODO 8)
- **Strafe or turn the wrong way in Part 2** — left is *negative* `right` and *negative* `turn` in `drive()`, but it makes Y and Heading go *up*
- **Heading past 180** — heading wraps from +180 to −180, so a target of 180 can never be "reached" with `>=`. Keep turn targets under 180 for now (Exercise 4)
