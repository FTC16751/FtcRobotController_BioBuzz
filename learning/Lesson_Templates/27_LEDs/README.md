# Lesson 27 — LEDs: A Status Light the Driver Can See

**Book**: none | **Estimated Time**: 1 meeting, about 2 hours | **Hardware Required**: goBILDA RGB Indicator Light (3118-0808-0002); the Limelight 3A for TODOs 5–7

**Do first**: L08 Servos and L26 Limelight.

---

## What You'll Learn

- Controlling the goBILDA indicator light, which pretends to be a **servo**
- Choosing colors a driver can read from across the field
- Writing a method that **returns** a value (`chooseAimColor()`)
- Turning sensor data into a signal the drive team can use during a match

---

## Key Concept: A Light That Thinks It Is a Servo

The goBILDA RGB Indicator Light plugs into a **servo port**, and the code talks to it as a
`Servo`. Instead of an angle, each position is a color:

| Color | Position |
|---|---|
| Off | 0.0 |
| Red | 0.277 |
| Orange | 0.333 |
| Yellow | 0.388 |
| Green | 0.500 |
| Blue | 0.611 |
| Violet | 0.720 |
| White | 1.0 |

Positions in between give the colors in between. `setPosition(GREEN)` is all it takes.

---

## Key Concept: Why a Light?

During a match the driver is watching the robot, not the Driver Station screen. A light on the
robot answers one question at a glance. In this lesson the question is "am I lined up to shoot?":

| What the Limelight sees | Light |
|---|---|
| nothing | off |
| target to the right | yellow |
| target to the left | blue |
| target straight ahead | green (blinking, after TODO 7) |

The team's robots already do exactly this in `common/vision/AimLed.java`. After this lesson you
can read that file and recognize every line.

---

## Configuration

Plug the light into any **servo port** and add it to the config as type **Servo**, named
**`led_servo`**. The Limelight is named **`limelight`**, as in Lesson 26.

On the programming board, TODOs 1–4 work with just the light; hold a target in front of the
Limelight for TODOs 5–7.

---

## Files in This Lesson

| File | Purpose |
|------|---------|
| `Template_L27_StatusLed.java` | Template with TODOs |
| `Complete_Solution.java` | Full solution |
| `Exercises.txt` | Practice challenges |

The team's LED class is `common/hardware/LedUtil.java`. The Prism LED strips (`common/prismled/`)
are a different, bigger kind of LED for later.

---

## Common Mistakes

- **Configured as a Continuous Rotation Servo** — it must be a plain **Servo**
- **Plugged into a motor port** — it only works on a servo port
- **Light never changes in Limelight mode** — the `if (gamepad1.left_bumper)` block is after `led.setPosition()` instead of before it
- **Colors that look alike** — orange, yellow and red blur together from far away; pick colors a driver cannot confuse
