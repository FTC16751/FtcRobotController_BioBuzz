# AprilTag relocalization on top of Pedro Pathing

**Written 2026-09-08. Research and design only; nothing built yet.** Companion to
`doc/PEDRO_ON_TEST2027.md` (which got Pedro building and owning the Pinpoint) and
`doc/DRIVE_STRATEGY_REVIEW.md`. The question asked: our Pedro setup and the Limelight both exist,
they use different coordinate frames, and Pedro ships a conversion between the FTC standard field
frame and an *inverted* one. How do we fuse them, and how do we write it so next week's game
announcement changes one line instead of ten files?

Sources: our tree at the current tip of `offseason/common-cleanup-2026`; the Pedro `core` and `ftc`
**2.1.2** artifacts in the Gradle cache (read as bytecode, so the transforms below are the real
ones, not remembered ones); FTC SDK 11.0.0 `Hardware`/`RobotCore` artifacts; the Pedro docs pages
*Example AprilTag Usage* and *Reference > Coordinates*; the FIRST field-coordinate-system page; the
Limelight MegaTag2 docs.

---

## 1. What we have today

### 1.1 The Pedro side (working, untuned, one robot)

- `com.pedropathing:ftc:2.1.2` + `telemetry:1.0.0`.
- `common/drive/PedroBridge` builds `FollowerConstants` / `MecanumConstants` / `PinpointConstants` /
  `PinpointLocalizer` / `Follower` **from a `RobotConfig`**. Six unit tests pin it.
- `common/drive/DriveUtil` builds the Follower first when `config.pedroPathing != null` and takes the
  Pinpoint from it, so exactly one object owns the device. `DriveState` makes the two motion sources
  exclusive (`FOLLOWING_PATH`, `HOLDING_POINT`, `TELEOP_PEDRO` vs `DRIVING_TO_POINT_PINPOINT`,
  `ALIGNING_TO_APRILTAG`).
- `DriveUtil.setPosition(x, y, headingDeg)` already routes through `follower.setPose(...)` when
  Pedro exists. **That is the door relocalization walks through — it is already built.**
- Only `Test2027BotConfig` has a `PedroPathingConfig`. Nothing is tuned on a robot yet (test plan K).

### 1.2 The vision side (working, robot-relative only)

- `common/vision/VisionUtil` wraps a Limelight 3A. It implements `AimTarget` and `TagSighting`.
- `common/vision/TagSighting` + `common/vision/TagApproach` are the good pattern: an interface in front of the
  camera, pure math behind it, a fake in `TagApproachTest`. But `TagSighting` is deliberately
  **robot-relative** (forward / right / square-up inches and degrees). It never mentions the field.
- `VisionUtil` already reads all three field-space products and shows them in telemetry:
  `getBotPose()` (MegaTag1), `getBotpose_MT2()` (MegaTag2), `getRobotPoseFieldSpace()` (single tag).
  Nothing in `common/` consumes them.
- The only existing relocalization is `GGRobot2.resetOdometryToVision()` (and a copy in `GGRobot`),
  which writes the single-tag field pose straight into `pinpoint.setPosition`. It is on an
  R3-on-hold robot; do not change it, but do not promote it as written either (see section 3).

### 1.3 The gap

Everything needed is present except the piece in the middle: **nothing converts a camera field pose
into the frame the follower's pose lives in, and nothing decides whether a given camera reading is
trustworthy enough to overwrite odometry with.** That middle piece is this document.

The Advanced tier plan in `OFFSEASON_CLEANUP_PLAN.md` already reserves the name:
`relocalizeFromTag(vision)`. This is the design for it.

---

## 2. The coordinate problem, exactly

### 2.1 Four frames are in play

| Frame | Origin | +X | +Y | Heading 0 | Units |
|---|---|---|---|---|---|
| **FTC standard** (FIRST spec) | field centre | right, parallel to the Red Wall | out, away from the Red Wall | along +X, CCW positive | any, consistent |
| **Inverted FTC** (`InvertedFTCCoordinates`) | field centre | the FTC frame turned 180° | " | " | inches |
| **Pedro field** | one corner of the field, 144 x 144 | right in the Panels drawing | up in the Panels drawing | along +X, CCW positive | inches, radians |
| **Limelight field space** | field centre, per the `.fmap` uploaded to the camera | whatever that map says | " | " | metres, degrees |
| **Pinpoint / `driveTo`** | wherever `setPosition` last put it | robot forward at heading 0 | robot left | CCW | inches (`Pose2D`), mm internally |

### 2.2 Pedro's two conversions, read out of the 2.1.2 bytecode

Both are "rotate by ±90° about the field centre, then translate by (+72, +72)". `Pose.rotate(a, true)`
is the ordinary CCW rotation with `heading += a`.

```
FTCCoordinates.convertToPedro:            rotate(-pi/2), then + (72, 72)
    x_pedro =  y_ftc + 72
    y_pedro = -x_ftc + 72
    h_pedro =  h_ftc - 90 deg

InvertedFTCCoordinates.convertToPedro:    rotate(+pi/2), then + (72, 72)
    x_pedro = -y_ftc + 72
    y_pedro =  x_ftc + 72
    h_pedro =  h_ftc + 90 deg
```

Consequences worth writing on the whiteboard:

- Under `FTCCoordinates`, Pedro's `(0, 0)` corner is FTC `(+72, -72)`: the corner at the Red Wall,
  on the right as you stand at the Red Wall. Under `InvertedFTCCoordinates` it is the opposite
  corner, FTC `(-72, +72)`.
- **`InvertedFTCCoordinates` is exactly the FTC standard frame rotated 180° about the field centre**
  (`x -> -x`, `y -> -y`, `h -> h + 180`). Substituting that into the FTC formulas gives the inverted
  formulas identically. So "inverted" is not a different kind of frame; it is the same frame with the
  other alliance corner treated as positive. That is why DECODE needed it: the season's AprilTag
  field map was drawn the other way round.
- `Pose.getAsCoordinateSystem(target)` is `target.convertFromPedro(source.convertToPedro(pose))`, so
  it round-trips both directions. **Never hand-write the algebra above in robot code** — call the
  library and pin it with a unit test. The algebra is here so the test has something to assert.
- `PoseConverter.pose2DToPose(Pose2D, CoordinateSystem)` does the same job starting from an SDK
  `Pose2D`, which is what the Pinpoint and most of our code speaks.

### 2.3 What actually decides which one is right

Not the game. The **field map file you uploaded to the Limelight** (and, for the SDK's own AprilTag
processor, `AprilTagGameDatabase`). The camera reports a pose in the frame of the map it was given.
So the rule is:

> Whichever `.fmap` is on the camera defines the frame. Pick the matching `CoordinateSystem`
> constant, prove it once on the field with a tape measure, and never think about it again until the
> map changes.

For DECODE that was `InvertedFTCCoordinates` (this is what the Pedro *Coordinates* page says
explicitly: "first declare a Pose in FTC coordinates (inverted for decode)"). For 2026-27 it may be
either. **This is a one-line change if, and only if, the constant lives in one place.** Section 5.1.

### 2.4 The heading MegaTag2 wants is not Pedro's heading

MegaTag2 is not a pose solver you can feed anything: it requires the robot's field yaw every frame
(`limelight.updateRobotOrientation(yawDegrees)`), and that yaw must be **in the same field frame as
the map**, i.e. FTC standard (or inverted), not Pedro's. Inverting the formulas above:

```
h_ftc = h_pedro + 90 deg     when the season frame is FTCCoordinates
h_ftc = h_pedro - 90 deg     when the season frame is InvertedFTCCoordinates
```

`teams/p3/teleop/P3_Robot3_TeleOp.java:260` does
`robot.vision.updateRobotOrientation(Math.toDegrees(follower.getPose().getHeading()))` — Pedro's
heading, raw. That is 90° wrong, which is the single most common way MegaTag2 goes bad (it silently
returns a plausible but rotated pose). That OpMode is `@Disabled`, so it has never hurt anyone, but
the same line must not appear in the Common version.

---

## 3. Defects found while reading, that this work must not inherit

| # | Where | What | Do |
|---|---|---|---|
| D1 | `teams/p3/teleop/P3_Robot3_TeleOp.java:260` | Pedro heading fed to `updateRobotOrientation`; 90° off (section 2.4) | fix when that OpMode is revived; never copy |
| D2 | `GGRobot2.java:223-237`, `GGRobot.java:192` | `resetOdometryToVision` uses the **single-tag** field pose, applies no frame conversion, and gates on nothing | leave (R3 on hold); the Common version must not follow it |
| D3 | same, line 228 | `Math.toDegrees(pose.getOrientation().getYaw())` — `YawPitchRollAngles.getYaw()` with no argument returns the raw stored value, and the Limelight driver constructs these with `AngleUnit.DEGREES` (verified in `Hardware-11.0.0`). So this multiplies degrees by 57.3 | always call `getYaw(AngleUnit.DEGREES)`; same for `getPitch`/`getRoll` |
| D4 | `common/vision/VisionUtil.java:158-165` | `hasMegaTag2FieldPose` is true whenever `getBotpose_MT2()` is non-null. The Limelight returns a **zero pose**, not null, when it has no fix | gate on `LLResult.getBotposeTagCount() > 0` |
| D5 | `common/vision/VisionUtil.java:145` | the "primary tag" is `tags.get(0)`, whichever the camera listed first | fine for `TagSighting` (which asks by id), not fine as a pose source |
| D6 | `common/drive/DriveUtil.java:376-389` | `setPosition` means "field pose, 0..144" on a Pedro robot and "wherever you say the origin is" on a Pinpoint-only robot. `resetPosition()` calls `setPosition(0,0,0)`, which on a Pedro robot **teleports the follower to the field corner** | resolve by adopting one field frame everywhere (section 5.1) before adding relocalization, which uses the same door |

D6 is the one that matters most here: relocalization is only meaningful if the odometry frame *is* a
field frame. On a Pinpoint-only robot whose auto never told the Pinpoint where the field is, there is
nothing for a camera pose to correct.

---

## 4. How AprilTag relocalization is actually done

### 4.1 The three products, and which to use

| Product | What it is | Needs | Use it for |
|---|---|---|---|
| **Target pose in robot space** (`getTargetPoseRobotSpace`) | one tag relative to the robot | camera pose in robot space, set in the LL web UI | `TagApproach` — what we already do |
| **MegaTag1** (`getBotpose`) | robot in field space, solved from the tags alone | field map | a **first** fix when you have no heading at all, e.g. at auto init; unstable from a single distant tag |
| **MegaTag2** (`getBotpose_MT2`) | robot in field space, solved with your yaw held fixed | field map **and** `updateRobotOrientation` every frame | **the one to relocalize from** — far less ambiguous, works off a single tag, immune to the tag-plane flip that wrecks MT1 |

### 4.2 The trap: never close the heading loop

MT2 takes your yaw as an input and returns it (essentially) unchanged. If you then write MT2's yaw
back into the source of that yaw, you have a loop with no new information in it — errors get locked
in, not corrected. Rule:

> **Relocalize X and Y from MegaTag2. Keep heading from the Pinpoint.** If heading itself is
> suspect, fix it with a MegaTag1 fix while stationary, or by squaring on a wall, not with MT2.

This also fits our stack: the Pinpoint's heading is our best number and Pedro's follower depends on
it continuously.

### 4.3 Gating — the part that decides whether this helps or hurts

A vision pose that is wrong once and gets written into the follower is worse than no vision at all.
Every rule below is cheap and each one has a failure it prevents:

1. `result.isValid()` and `getBotposeTagCount() > 0` — no fix, no write (D4).
2. **Staleness / latency**: `getStaleness()` (ms since the result was produced) below ~100 ms.
   `getCaptureLatency() + getTargetingLatency()` tells you how old the picture is; at 40 in/s a
   60 ms pipeline is 2.4 in of lag. Either compensate with the pose history or only relocalize slow.
3. **Distance**: `getBotposeAvgDist()` under about 8-10 ft. Angular error in the solve grows with
   range; a tag across the field is a guess.
4. **Speed**: skip while the robot is moving fast (motion blur plus latency). Pedro gives you
   `follower.getVelocity()`; a simple threshold of a few inches per second is enough. The best
   relocalization moments are: auto init, the pause at a scoring position, and any hold point.
5. **Plausibility**: reject a fix more than N inches (start with 12) from the current pose *unless*
   several consecutive frames agree on it. A big jump is either a wrong frame (section 2) or a
   misread map — it is almost never the robot actually being a foot from where it thought.
6. **Rate limit**: at most once every ~250 ms. There is no information in relocalizing at 50 Hz off
   the same tag.

### 4.4 Snap, blend, or custom localizer

The Pedro docs give the shape of all three (`Example AprilTag Usage`, "custom localizer or
relocalization"):

- **Snap** — `follower.setPose(cameraPose)` at chosen moments. Simplest, testable, and if the frame
  is wrong you find out immediately instead of slowly. **Start here.**
- **Blend** — every gated frame, move the follower a fraction of the way toward the camera pose,
  or run the camera X and Y through Pedro's own `KalmanFilter` / `LowPassFilter`
  (`com.pedropathing.control`, both present in 2.1.2) and set the filtered result. Handles a noisy
  camera without lurching. This is a *setting* on the same class, not a different class.
- **Custom fusing `Localizer`** — implement `com.pedropathing.localization.Localizer` (15 methods;
  `getPose`, `getVelocity`, `setPose`, `update`, `resetIMU`, `isNAN`, ...) wrapping the
  `PinpointLocalizer` and folding vision in inside `update()`. Then every auto gets it for free and
  nothing else in the codebase changes. It is also the version that can strand us mid-season if it
  is wrong, and it cannot be unit-tested without a HardwareMap. **Do it only after snap and blend
  have run on a field.**

---

## 5. The design for us

### 5.1 First, adopt one field frame for the whole codebase

**Pedro's frame (0..144 in, radians, corner origin) becomes THE field frame**, on Pedro robots and
Pinpoint-only robots alike. Reasons: Panels draws it, `Pose.mirror()` works in it, and it is already
what `DriveUtil.setPosition` means on a Pedro robot. Conversions happen at exactly two edges:
the Limelight coming in, and any FTC-standard number a human quotes going out.

New in `common/` (name to be argued about, `FieldFrame` is fine):

```java
public final class FieldFrame {
    /** The FTC-standard frame this season's AprilTag field map is drawn in.
     *  DECODE (2025-26) was inverted. CHANGE THIS ONE LINE when the map changes. */
    public static final CoordinateSystem SEASON = InvertedFTCCoordinates.INSTANCE;

    /** An FTC-standard field pose (inches, degrees CCW) as a Pedro field pose. */
    public static Pose fromFtc(double xIn, double yIn, double headingDeg) {
        return new Pose(xIn, yIn, Math.toRadians(headingDeg), SEASON)
                .getAsCoordinateSystem(PedroCoordinates.INSTANCE);
    }

    /** The FTC-standard yaw, in degrees, that MegaTag2 must be told (section 2.4). */
    public static double ftcYawDegrees(Pose pedroPose) {
        return Math.toDegrees(pedroPose.getAsCoordinateSystem(SEASON).getHeading());
    }
}
```

Tests (pure math, no hardware — the house pattern): the corner mappings of section 2.2 both ways,
the 180° relationship between the two FTC frames, and a round trip through
`ftcYawDegrees`/`fromFtc`. Those tests are what makes flipping `SEASON` next week a safe one-line
change: flip it, run the tests, read the new expected corners.

D6 gets fixed in the same breath: `resetPosition()` stops meaning "(0,0,0)" and becomes
"the start pose this auto declared".

### 5.2 A `FieldPoseSource` seam, exactly like `TagSighting`

`TagSighting` proved the pattern: an interface with plain numbers, `VisionUtil` implements it, a fake
implements it in the tests, and the sign conventions live in the adapter with a comment saying which
robot check confirmed them.

```java
public interface FieldPoseSource {
    /** True if this loop produced a usable MegaTag2 field fix. */
    boolean hasFieldPose();
    /** Robot pose in the FTC-standard frame of the season's field map: inches, heading degrees CCW. */
    double fieldXInches();
    double fieldYInches();
    double fieldHeadingDegrees();
    /** Quality, for gating (section 4.3). */
    int    tagCount();
    double averageTagDistanceInches();
    double stalenessSeconds();
}
```

`VisionUtil` implements it from `getBotpose_MT2()` + `getBotposeTagCount()` + `getBotposeAvgDist()` +
`getStaleness()`, fixing D3 and D4 on the way. `VisionUtil` also gains
`updateRobotOrientation(...)` callers being told, in one comment, that the argument is
`FieldFrame.ftcYawDegrees(follower.getPose())` and never a raw Pedro heading.

### 5.3 `common/TagRelocalizer` — pure math and state, no hardware

Mirrors `TagApproach`: a `Settings` with fluent setters, a `Clock`, one `update(...)` per loop, and
outputs the caller applies. It never touches the drive.

```java
public class TagRelocalizer {
    public static class Settings {
        public double maxTagDistanceInches = 108;   // 9 ft
        public double maxStalenessSec      = 0.10;
        public double maxSpeedInPerSec     = 4.0;   // relocalize when nearly still
        public double maxJumpInches        = 12.0;  // bigger needs agreement
        public int    agreeingFramesForJump= 3;
        public double minIntervalSec       = 0.25;
        public double blend                = 1.0;   // 1.0 = snap, 0.2 = ease in
        public boolean correctHeading      = false; // section 4.2: leave this off with MT2
    }
    /** @return the Pedro-frame pose to apply, or null if this loop's reading was rejected. */
    public Pose update(FieldPoseSource vision, Pose currentPose, double speedInPerSec) { ... }
    public String lastRejectReason();   // for telemetry: this is what you stare at on the field
}
```

Every gating rule in 4.3 is one `if` with one test. `lastRejectReason()` is not decoration — on a
field, "why did it not correct?" is the only question you will have.

### 5.4 The surface students see

In `DriveUtil`, Advanced tier, next to `followPath`:

```java
/** Correct the robot's field position from an AprilTag, if this loop's sighting is trustworthy.
 *  Heading is left alone (see doc/APRILTAG_RELOCALIZATION.md section 4.2). Returns true if it corrected. */
public boolean relocalizeFromTag(FieldPoseSource vision)
public void    setRelocalization(TagRelocalizer.Settings s)
public boolean isRelocalizationTrusted()      // for telemetry / a driver LED
```

Applying the pose reuses `setPosition`, so it works on Pedro robots (`follower.setPose`) and
Pinpoint-only robots (`pinpoint.setPosition`) with no branch in the caller — once 5.1 has made both
mean the same thing.

Typical auto, unchanged in shape from what we already write:

```java
robot.drive.followPath(toScorePosition, true);
...
if (!robot.drive.isBusy()) {
    robot.vision.update();
    robot.drive.relocalizeFromTag(robot.vision);   // we are holding still at the goal: best moment
    // then launch, or build the next path from the corrected pose
}
```

### 5.5 What we are deliberately not building yet

The custom fusing `Localizer` (4.4). It is the right end state and the Pedro docs point at it, but it
is invisible when it misbehaves and it cannot be unit-tested. Revisit after a season-opening event.

---

## 6. Step plan, with a check on each step

Laptop steps 1-4, robot steps 5-8. No live team OpMode changes; hard rules 1-6 hold.

1. **`common/FieldFrame` + tests.** Check: the corner assertions of 2.2 pass for both frames, and
   flipping `SEASON` flips them. ~40 lines, ~10 tests.
2. **`common/FieldPoseSource`, implemented by `VisionUtil`.** Fix D3 and D4 while in there. Check:
   `assembleDebug` passes; a fake source in a test drives the interface.
3. **`common/TagRelocalizer` + tests** covering each gate, the jump-agreement counter, the blend, and
   `lastRejectReason`. Check: ~20 tests, no hardware. (Total goes from 102 to ~130.)
4. **`DriveUtil.relocalizeFromTag`** plus the `setPosition` semantics fix (D6). Check: compiles;
   `PedroBridgeTest` still passes; no existing call site changes behaviour.
5. **Frame proof on the field.** Park the robot at a measured spot facing a known direction, with the
   goal tag visible. A new `Test2027FrameCheck` TeleOp prints: Limelight field pose (FTC frame),
   `FieldFrame.fromFtc(...)` of it, `follower.getPose()`, and the difference. Move to a second spot
   30 in away and repeat. **Pass: the two agree within ~2 in and ~3° at both spots.** If they are
   consistently rotated 90° or 180°, `SEASON` is wrong — flip it and repeat. *This single test is the
   whole coordinate question, answered.*
6. **MT2 heading proof.** With `updateRobotOrientation(FieldFrame.ftcYawDegrees(...))` wired: turn
   the robot 90° by hand, confirm the MT2 pose stays put (within a couple of inches) instead of
   swinging. Failing this means 2.4 is still wrong somewhere.
7. **Snap in an auto.** Beginner-style auto: drive a path, stop at the goal, `relocalizeFromTag`,
   drive back to the start mark. **Pass: returns within 2 in with relocalization on, and the
   correction it applied is printed.** Compare against the same auto with relocalization off — the
   difference is the value of this whole document.
8. **Blend and gating tuning.** Turn `blend` down until the correction is invisible to the drivers
   but the drift still goes away; record the numbers with the date.

Robot checks 5-8 become **section N** of `doc/ROBOT_TEST_PLAN.md`.

---

## 7. Next week, when the new game drops

The whole point of the structure above. In order:

1. Upload the new season `.fmap` to every Limelight; set each camera's pose in robot space in the
   web UI (that is where camera geometry lives, not in `RobotConfig`).
2. Determine the map's frame and set `FieldFrame.SEASON` — one line. Run the tests, then do check 5.
3. New tag ids and pipelines in `CommonConstants.Limelight` (today: 20/24 goals, 21/22/23 motif,
   pipelines 0/1/2).
4. New field landmarks in `CommonConstants.Field` — and move them to **inches in the Pedro frame**
   while you are there. The two goal poses are currently metres in Limelight field space, which is a
   third unit system for no reason.
5. Nothing in `TagRelocalizer`, `TagApproach`, `PedroBridge` or `DriveUtil` should need to
   change. If it does, this design failed and the doc should say why.

---

## 8. Open questions for the mentor

1. **Frame unification (5.1) now or later?** It fixes D6 and makes relocalization meaningful, but it
   changes what `resetPosition()` means on Pedro robots. Only test2027bot has Pedro today, so the
   blast radius is one robot — but the *concept* ("your odometry origin is the field corner, not
   where you set the robot down") is a real change to how students think. Recommendation: do it, with
   the README table updated in the same commit.
2. **Which tier does `relocalizeFromTag` belong to?** The plan says Advanced. Argument for
   Intermediate: it is one call and it makes a beginner's auto *more* reliable, which is hard rule 5's
   test. Argument for Advanced: it silently does nothing when gating rejects, and a beginner cannot
   debug that. Recommendation: Advanced, but with `isRelocalizationTrusted()` on the RUN ME telemetry
   so anyone can see it working.
3. **Do the demo robots (GearGirls, P3) get this?** They have Limelights and Pinpoints but no Pedro.
   The design works for them via `pinpoint.setPosition`, but only if their autos start by declaring a
   real field start pose, which they do not today. Recommendation: leave them; build it on
   test2027bot and let the new season's robots inherit it.
4. **Camera pose in robot space** is configured per-camera in the Limelight web UI and is not in
   version control. Should `RobotConfig` at least *record* it (forward / left / up inches, and mount
   angles) so a swapped camera can be reconfigured from the repo? Recommendation: yes, as documented
   fields nothing reads yet.
