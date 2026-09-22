# BIOBUZZ vision: balls on the Limelight, hive tags on a webcam

**Written 2026-09-16. Research and design only; nothing built yet.** Companion to
`doc/APRILTAG_RELOCALIZATION.md` (parked this season, see section 1) and `doc/PEDRO_ON_TEST2027.md`.

The question asked: can the Limelight 3A run the Hive-Vision ball detector
(`github.com/sidhuharjas/Hive-Vision`, `yolo/weights/best_limelight3a_ssd_mobilenetv2_300x300.tflite`)
well enough for an auto to drive to a cluster of Pollen or Nectar and collect it? And the mentor's
proposal for how the cameras split the work:

- **Limelight 3A, pointed down at the field: ball detection only**, using its own processor.
- **A plain USB webcam: AprilTags on the Hive only**, to line the robot up and to tell whether the
  cell in front of it is UP (ready to shoot into) or DOWN. **No relocalization from tags this
  season**: every BIOBUZZ tag is on a Hive cell, and the Hive is a seesaw.

Sources: the Hive-Vision repo at its 2026-09-16 tip (README, `yolo/README.md`,
`shipping/SHIP_README.md`, `yolo/UPDATE_03.md`, the model file itself); Limelight docs (3A quick start,
software change log, downloads page); the FTC SDK 12.0.0 `Hardware` and `Vision` artifacts in the
Gradle cache, read as bytecode; the BIOBUZZ Event Field Setup Guide V1.0 (2026-09-12); Competition
Manual V1 sections 9.6 and 9.9 as quoted by team 36533's public `HiveGeometry.java` (re-check against
the manual before trusting a number to the inch); our tree at `launcher` tip.

---

## 1. The decision, and why the split is the right one

Two cameras, one job each. That removes the biggest problem found in the feasibility check: the
Limelight runs **one pipeline at a time**, so a single Limelight hunting balls is blind to tags, and
a single camera cannot point both down at the floor and up at a cell 44 in off the tiles.

Why balls go on the Limelight and not the other way round:

- The Hive-Vision repo also ships two Control Hub colour pipelines (HSV and Lab, for a webcam through
  `VisionPortal`). Their own report measures them at **7-11% precision** on match footage: the field
  is full of yellow, red and blue patches. Only the neural model finds *shape*. So balls need the
  neural model, and the neural model needs the Limelight.
- The Hive tags need only modest range (the robot is lining up to shoot from a few feet) and the
  SDK's own `AprilTagProcessor` already knows the BIOBUZZ tag layout (section 4). A webcam is enough.

What this parks: `VisionUtil`'s MegaTag and field-space code, and the whole relocalization design in
`doc/APRILTAG_RELOCALIZATION.md`. The SDK 12 BIOBUZZ tag library contains **only the four Hive cell
clusters**, ids 30-45; there are no fixed wall or frame tags this season. A tag on a seesaw is a
localization source only when you can prove the seesaw is at rest, which is exactly the gating that
doc describes and exactly what we are choosing not to build now. Its `FieldFrame` step is still the
right first move if that changes.

What stays untouched: `VisionUtil`, `TagApproach`, `DriveUtil`, and every Limelight-only robot
(GearGirls, P3). This is additive.

---

## 2. What the Hive-Vision model is

| Fact | Detail |
|---|---|
| Format | uint8 300x300 SSD-MobileNetV2, `TFLite_Detection_PostProcess` outputs (num, scores, class ids, boxes), **max 10 boxes per frame**. Tensor names confirmed inside the file. |
| Origin | Limelight's own online trainer, "Limelight CPU" target. Format risk is low. |
| Classes | `yellow_pollen` 0, `red_nectar` 1, `blue_nectar` 2 (`labels.txt`). Nectar is alliance-specific, which is a gift: filter to Pollen plus our colour. |
| Training data | about 7,000 images, mostly synthetic renders of the ball CAD plus frames from the kickoff video. Kickoff was 2026-09-12, so real footage is scarce. |
| Published accuracy | **none for this model.** The repo's precision/recall numbers are for its colour pipelines. For the SSD it reports confidences of 0.87-0.92 on one clip and recommends running at **0.35-0.45 confidence** (0.25 gives visible false positives). |
| Maturity | every commit is from 2026-09-15/16. The shipping checklist still lists "upload to a 3A and confirm it loads" as open, while Update 3 says a team ran it and the `ssd_in_action.mp4` clip is that team's footage. Expect fortnightly model updates (their stated plan). |
| Licence | code MIT. The AGPL note applies to the YOLO weights, which we do not use. |
| Alternatives | Limelight's downloads page has no BIOBUZZ detector. |

Verdict from the check: the artifact is right for the hardware and the SDK side exists. **Nobody has
measured it.** Section 8 measures it.

SDK 12.0.0 side (verified in `Hardware-12.0.0.aar`): `LLResult.getDetectorResults()` returns
`LLResultTypes.DetectorResult` with `getClassName`, `getClassId`, `getConfidence`, `getTargetXDegrees`,
`getTargetYDegrees`, `getTargetXPixels`/`YPixels`, `getTargetArea`, `getTargetCorners`. Everything a
floor-target adapter needs is there.

---

## 3. The Limelight side: pointing down at balls

### 3.1 Why the current mount cannot do it

`Test2027Constants.TagTest` records that on the Skyline chassis the goal tag sits 31 in above the
camera and leaves the frame inside about 50 in. With the 3A's 42 deg vertical field of view that puts
the camera pitched **about 11 deg up**, and the bottom of its frame only about 10 deg below level: the
floor enters the picture roughly 5.5 times the camera height ahead of the robot. A ball inside 4 ft is
invisible, which is where an intake approach needs it most. Hence the separate, down-pitched camera.

### 3.2 Where to point it

Vertical field of view 42 deg (half angle 21 deg). For camera height `h` and downward pitch `p`:
nearest floor in frame = `h / tan(p + 21)`, farthest = `h / tan(p - 21)`. Once `p` is past 21 deg the
whole frame is floor, which also keeps the far wall, the audience and other robots' panels out of
the picture (fewer false positives).

| height | pitch 25 deg | pitch 30 deg | pitch 35 deg |
|---|---|---|---|
| 8 in | 8 in to beyond range | 6.5 in to 51 in | 5 in to 32 in |
| 12 in | 12 in to beyond range | 10 in to 76 in | 8 in to 48 in |
| 16 in | 15 in to beyond range | 13 in to 101 in | 11 in to 64 in |

Recommendation: **about 30 deg down, as high as the robot allows, ideally 10-14 in**, with the
near edge of the frame just in front of the intake. The near edge matters: a ball leaves the bottom
of the picture 6-10 in ahead, so the last foot into the intake is driven blind (section 6.3).

### 3.3 How far it can see a ball

The detector sees 300 px across 54.5 deg, about 5.5 px per degree. Pixels across a ball is roughly
`882 / d` for Pollen (2.8 in) and `1134 / d` for Nectar (3.6 in), `d` in inches.

| distance | Pollen | Nectar |
|---|---|---|
| 2 ft | 37 px | 47 px |
| 4 ft | 18 px | 24 px |
| 6 ft | 12 px | 16 px |
| 8 ft | 9 px | 12 px |

SSD-MobileNet at this input size gets unreliable under about 15-20 px. Plan on **a single ball out
to 4-5 ft and a four-ball line somewhat farther.** This is not a whole-field scan from the start tile;
it is the last two tiles of an approach. That fits the 30 deg / 12 in mount above almost exactly.

### 3.4 Rate, latency, limits

- CPU inference: Limelight's change log quotes **about 10 fps** for CPU detectors on the Limelight 3
  class and publishes no 3A figure. AprilTags on the 3A run 20 fps. At 30 in/s a 100 ms frame is 3 in
  of travel; fine for a 0.3-power approach like `TagApproach`, which was tuned at 20 fps.
- **10 detections per frame maximum** (the model's post-process op). A pile of 15 reports 10.
- **Limelight OS 2026.0**, the newest image for the 3A (there is no 2026.1 for it), added a
  **Cluster Strength** setting on neural detector pipelines that merges nearby detections into one
  grouped target. Built for "drive to the pile". Also on the camera: a class filter and a confidence
  slider. Check the OS version in the web UI first (section 7).
- The pipeline switch to a neural model loads the model; on the two-camera robot the Limelight
  never leaves the detector pipeline, so this only costs at init.

---

## 4. The webcam side: Hive tags

### 4.1 What the manual says (V1, sections 9.6 and 9.9, via team 36533's quoted constants)

| Fact | Value |
|---|---|
| Tag | **3.25 in**, family 36h11, an **AprilTag Cluster of 4** on the **bottom face of each Cell, facing the tiles** |
| Ids | red Scoring-side cell 30-33, red Audience-side cell 34-37, blue Audience-side cell 38-41, blue Scoring-side cell 42-45. Verified in SDK 12's `AprilTagGameDatabase.getBioBuzzTagLibrary()`, cluster names RED SCORING / RED AUDIENCE / BLUE AUDIENCE / BLUE SCORING. |
| Hive | a seesaw at field centre, two Cells per Hive, **bi-stable at +/-30 deg**, pivot **43.95 in** above the tiles, each Cell 18.84 in from the pivot, the two Hives 25.5 in apart |
| Tag plane height | low Cell **25.5 in**, high Cell **about 44.3 in** (25.5 + 2 x 18.84 x sin 30) |
| Match start | red Hive's Audience cell up, blue Hive's Scoring cell up, 3 Nectar in each up cell (Field Setup Guide 11.1) |

So a cluster id says **which alliance and which cell**. It never says which way the seesaw is
tipped: the same cluster is seen high or low. The height of the tag plane above the tiles says that,
and the tilt of the plane (about 30 deg from horizontal when at rest, sweeping through 60 deg during a
tip) says whether the Hive is settled or moving.

### 4.2 SDK support (verified in `Vision-12.0.0.aar`)

- `AprilTagProcessor.Builder().setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())` teaches the
  solver the four-tag clusters. Detections then include `AprilTagClusterDetection` objects (with
  `percentClusterFound`) alongside the member tags; the cluster pose has a 13 in baseline and is the
  one to trust. Nobody in this repo has used `VisionPortal` yet; the SDK samples
  `ConceptAprilTagEasy` and `RobotAutoDriveToAprilTagOmni` are the reference, as the cleanup plan
  already noted (item 5, `WebcamTagSighting`).
- `ftcPose` gives `range`, `bearing`, `elevation`, `x`/`y`/`z`, `yaw`/`pitch`/`roll`. That is all
  `TagSighting` and `AimTarget` need, plus the height calculation below.

### 4.3 UP or DOWN, from one cluster

With the webcam mounted `h` inches up and pitched `p` degrees above level:

    tagHeightAboveTiles = h + range * sin(elevation + p)

Threshold at **35 in**, the midpoint of 25.5 and 44.3. Add a settled gate: the tag plane's tilt must
be within a few degrees of +/-30 deg (which sign is the H1-style stand check, once; do not reason it
out). Result per cell: `UP`, `DOWN`, `MOVING`, `NOT_SEEN`. The cell to shoot into is our alliance's
cell that reads `UP`. One cluster in view is enough; the other cell of the same Hive is by definition
in the other state.

### 4.4 How far a webcam reads a 3.25 in tag

Pixels across the tag at 640x480 is about `2167 / d` for a 55 deg webcam (Logitech C270 class) and
`1703 / d` for a 70 deg one (C920 class). 36h11 wants roughly 25 px to decode.

| distance | 55 deg webcam | 70 deg webcam |
|---|---|---|
| 3 ft | 60 px | 47 px |
| 4 ft | 45 px | 35 px |
| 6 ft | 30 px | 24 px |
| 8 ft | 23 px | 18 px |

So **reliable cluster reads inside about 6 ft** at 640x480 with no decimation; decimation 2 (the
usual Control Hub speed setting) halves that. Enough for lining up to shoot, which is the only job.
The tag is 25-44 in up and faces the tiles, so the webcam wants to be pitched **up about 20 deg**:
from 4 ft away a 44 in tag over a 10 in camera is 35 deg up, from 8 ft it is 20 deg.

---

## 5. What auto actually needs

Every ball at match start is at a fixed spot (Field Setup Guide V1.0, section 11):

| Where | What |
|---|---|
| 4 Flowers on the perimeter walls | 4 Pollen each, stacked in a ring |
| 2 Gardens, corner tiles A1 (red) and F6 (blue) | 4 Pollen each in a line against the tape and both walls |
| 2 up Hive cells | 3 Nectar each |
| alliance trays / preloads | 4 Pollen and 5 Nectar per alliance, not on the floor |

So the drive to a Garden corner is a **Pedro path to a known point**; vision is not needed to find
it. Vision pays for the **last 3-4 ft into the intake** (the balls will not be exactly where the
setup crew left them, and the robot will not be exactly where odometry says) and for **anything that
scatters mid-auto** (a tipped Hive, an emptied Flower, the other robot). Both are the reactive
approach, which is exactly what the down-pitched Limelight sees.

A first auto, 30 s:

1. Pedro path from the start tile to the shooting spot by our Hive. Webcam finds our cell's
   cluster; `driveToTagAsync` lines up; `UP` check; shoot the preloads.
2. Pedro path toward our Garden corner. At about 4 ft the Limelight sees the four-Pollen line;
   ball approach with the intake running; drive through the last foot blind.
3. Pedro path back to the shooting spot; line up; `UP` check; shoot.

---

## 6. Design in this codebase

### 6.1 The seam already exists

`TagApproach` asks a `TagSighting` four things: `canSee(id)`, `forwardInches()`, `rightInches()`,
`squareUpDegrees()`. It never mentions a camera. `DriveUtil.driveToTagAsync` takes any
`TagSighting`. `Launcher.aim`, `AimLed` and `VisionAim` take any `AimTarget`. So both new cameras
are **adapters behind interfaces that exist**, and nothing in the drive or launcher changes.

### 6.2 New classes in `common/`

| Class | What it is |
|---|---|
| `LimelightBalls implements TagSighting` | owns the Limelight 3A on the detector pipeline. `update()` each loop reads `getDetectorResults()`, keeps the detections above the confidence floor for the wanted classes. `canSee(classId)` picks the best ball of that class (largest area = nearest, or the largest cluster); `forwardInches` / `rightInches` come from `FloorTargetMath`; `squareUpDegrees` = minus the ball's horizontal angle (turn to face it). Telemetry lists every detection with class, confidence, angles, area. |
| `FloorTargetMath` | pure: camera height and pitch plus the detector's `tx`/`ty` to forward and right inches for a target on the floor (ball centre 1.4-1.8 in up, second order). Unit tested like `EncoderMoveMath`. |
| `WebcamTags implements AimTarget, TagSighting` | owns a `VisionPortal` + `AprilTagProcessor` with the BIOBUZZ library. `canSee(clusterId)` finds that cluster (member tags ignored; `percentClusterFound` gate); `forward`/`right`/`squareUp` from `ftcPose` (SDK frame: X right, Y forward, bearing positive left; signs are the H1 stand check again). `AimTarget` from the same pose: `range` and `bearing`. Plus `hive(Cell) -> UP / DOWN / MOVING / NOT_SEEN`. |
| `HiveMath` | pure: `tagHeightAboveTiles`, the 35 in threshold, the settled-tilt gate, the four cluster ids and their alliance/side. Unit tested. |

Naming is open (section 9). `TagSighting` being implemented by a ball detector reads oddly;
renaming it `TargetSighting` is a mechanical refactor with 23 tests behind it and can wait.

### 6.3 Ball approach, concretely

`robot.drive.driveToTagAsync(robot.balls, POLLEN, standoffInches, hold)` with the existing gains
(`config.tagApproach`, 0.04 / 0.04 / 0.015, max 0.3). Two ball-specific points:

- **Standoff = the near edge of the frame**, 8-12 in, not 0. The ball vanishes off the bottom of the
  picture before the intake reaches it. When the approach reports `DONE`, a beginner
  `driveForward(12)` with the intake on finishes the job. (`TagApproach`'s LOST coast, 1.5 s on the
  last powers, does roughly the same thing by accident; do not rely on an accident.)
- **Which ball.** Start with "nearest of the wanted class" (largest area). Add grouping only when the
  field shows it is needed: either the camera's Cluster Strength setting (zero code) or a small
  angular-proximity grouping in `LimelightBalls` (testable). The Garden line is four balls a tile
  wide; nearest-first drives into one end of it, which is fine for an intake.

### 6.4 Config and constants

- `RobotConfig.HardwareNames`: add `webcam` (null if none; `"Webcam 1"` on test2027bot).
- `RobotConfig.CameraMount` (new, one per camera): forward / left / up inches from robot centre and
  pitch degrees. `doc/APRILTAG_RELOCALIZATION.md` open question 4 asked for exactly this record;
  now something reads it (`FloorTargetMath`, `HiveMath`). The Limelight web UI's camera pose is not
  needed for the detector pipeline.
- `CommonConstants.Limelight`: `DETECTOR_PIPELINE = 3` and the class ids `POLLEN = 0`,
  `RED_NECTAR = 1`, `BLUE_NECTAR = 2`. Pipelines 0-2 stay as they are for the Limelight-only robots.
- `CommonConstants.Hive`: the four cluster base ids and heights from section 4, in one place with
  the manual section cited beside each number.
- `Test2027Constants`: confidence floor (start 0.40), standoff, which classes to collect
  (Pollen plus our alliance's Nectar), the shooting standoff from the cell.

### 6.5 test2027bot wiring

`Test2027Robot` grows `balls` (`LimelightBalls`) and `tags` (`WebcamTags`); on this robot `vision`
(`VisionUtil`) is not built. `Launcher.aim(robot.tags)`, `AimLed` on `robot.tags`, the TeleOp RB
approach on `robot.tags`. `update()` steps both cameras first, as it does `vision` today. Log
channels: `Vision/Ball/Class`, `Confidence`, `Forward_in`, `Right_in`, `Count`; `Vision/Hive/Cell`,
`State`, `Height_in`, `Tilt_deg`. Two new OpModes: `Test2027BallApproachAuto` (copy of the tag
approach auto with `balls` in place of `vision`) and `Test2027HiveCheck` (a bench TeleOp that prints
every cluster in view with id, range, height, tilt and the UP/DOWN verdict).

Hard rule 0 holds: TeleOp launcher starts in PRESET; the webcam only feeds AIM when the driver opts in.

---

## 7. Hardware checklist

1. **USB.** The Control Hub has one USB host port. Limelight 3A plus a webcam means a **USB hub**,
   and the 3A draws up to 4 W. Plan on a powered hub or measure the unpowered one under load; a
   brownout mid-match looks like "the camera stopped seeing", not like a power fault (check V8).
2. **Limelight mount:** about 30 deg down, 10-14 in up, near edge of the frame just ahead of the
   intake (section 3.2). Record height and pitch in `CameraMount`.
3. **Webcam mount:** forward-facing on the launcher's aim axis so `bearing` is the aim error (as the
   Limelight is today for `AimLed`), pitched about 20 deg up. Any UVC webcam; the SDK sample uses
   6 ms exposure / gain 250 against motion blur.
4. **Limelight OS 2026.0** for Cluster Strength; upload the model and `labels.txt` to pipeline 3,
   confidence 0.40, class filter Pollen plus our Nectar. Start with a short exposure and low gain
   (one team's published notes suggest 2.5 ms / gain 24); tune on our field.
5. **A practice Hive**, or at least a printed cluster on a board at 25.5 in and 44.3 in at 30 deg
   tilt, for section 8's V5-V7.

---

## 8. Bench and field checks (become a section of `doc/ROBOT_TEST_PLAN.md` when built)

No code for V1-V3 and V5; they answer the unknowns before anything is written.

- **V1 Model loads.** Pipeline 3 on the 3A, live feed, a Pollen on the floor. Read off: loads or
  not, fps shown in the web UI, time to switch to the pipeline.
- **V2 Range at the chosen pitch.** Pollen and Nectar, tape measure: the nearest distance still in
  frame and the farthest still detected at confidence 0.40. Pass: near <= 12 in, far >= 48 in.
- **V3 False positives.** Our field, robots and tape in view, 60 s of snapshots at 0.35 / 0.40 /
  0.45. Pass: no non-ball detection above the chosen floor lasts more than 2 frames.
- **V4 Ball approach.** One Pollen 3 ft ahead: `DONE` at the standoff, then drive-through collects
  it. Then the four-Pollen Garden line in a corner. Cover the camera mid-approach: LOST, stops.
- **V5 Cluster read.** Practice Hive, both states: every member id and the cluster id reported,
  `percentClusterFound`, range against a tape measure at 3 / 4 / 6 ft. Height calculation within
  3 in of 25.5 / 44.3. Which sign of `pitch` means "settled" (write it into `HiveMath`).
- **V6 UP / DOWN.** Both states read correctly from 3-6 ft; `MOVING` during a hand tip; `NOT_SEEN`
  with the lens covered.
- **V7 Line up and shoot.** `driveToTagAsync(robot.tags, ourCell, standoff)` ends square in front
  of the up cell; the launcher table distance from `robot.tags` agrees with the tape within 3 in.
- **V8 USB under load.** Full match length, drivetrain at full power, both cameras stay connected
  (`isConnected()` for the Limelight, portal state for the webcam) and the hub does not reset.
- **V9 The auto of section 5**, timed. Pass: preloads and at least the Garden line scored inside 30 s.

---

## 9. Open questions for the mentor

1. **Which robot first?** test2027bot on the Skyline chassis has the Limelight; the webcam, hub and
   two mounts are new hardware. The alternative is to wait for the competition chassis and build
   this straight onto it.
2. **Camera placement versus the launcher.** Does the webcam ride on the launcher's aim axis so the
   existing `AimLed` / `Launcher.aim` behaviour carries over unchanged? Recommendation: yes.
3. **Which balls in auto.** Pollen only, or Pollen plus our alliance's Nectar? The class filter is
   one constant either way. Recommendation: Pollen only until the Hive scoring math says otherwise.
4. **Rename `TagSighting` to `TargetSighting`** now that a ball detector implements it?
   Recommendation: after V4 passes, in its own commit.
5. **Nearest ball or biggest cluster** as the default target (section 6.3)? Recommendation:
   nearest, and let V4 on the Garden line decide whether grouping is needed.
6. **Relocalization stays parked** this season (section 1). If a Team Update adds a fixed tag, the
   `FieldFrame` step of the relocalization doc is the first thing to build.
