#!/usr/bin/env python3
"""Write sample_decode_run.wpilog: a simulated Decode run using the LogUtil names, for learning AdvantageScope.
Not a record of a real run. Usage: python3 make_sample_wpilog.py [out.wpilog]"""
import math, random, struct, sys

out = sys.argv[1] if len(sys.argv) > 1 else "sample_decode_run.wpilog"
random.seed(17651)
f = open(out, "wb")
f.write(b"WPILOG" + struct.pack("<HI", 0x0100, 0))

def rec(eid, ts, payload):
    def n(v): return max(1, (v.bit_length() + 7) // 8)
    ie, ps, ts_n = n(eid), n(len(payload)), n(ts)
    f.write(bytes([(ie - 1) | ((ps - 1) << 2) | ((ts_n - 1) << 4)]))
    f.write(eid.to_bytes(ie, "little") + len(payload).to_bytes(ps, "little") + ts.to_bytes(ts_n, "little") + payload)

def s32(s): b = s.encode(); return struct.pack("<I", len(b)) + b

ids, nxt = {}, [1]
def entry(name, typ, meta=""):
    ids[name] = nxt[0]; nxt[0] += 1
    rec(0, 0, bytes([0]) + struct.pack("<I", ids[name]) + s32(name) + s32(typ) + s32(meta))

# struct schemas AdvantageScope needs to read Pose2d
entry("/.schema/struct:Rotation2d", "structschema"); rec(ids["/.schema/struct:Rotation2d"], 0, b"double value")
entry("/.schema/struct:Translation2d", "structschema"); rec(ids["/.schema/struct:Translation2d"], 0, b"double x;double y")
entry("/.schema/struct:Pose2d", "structschema"); rec(ids["/.schema/struct:Pose2d"], 0, b"Translation2d translation;Rotation2d rotation")

def declare(name, typ): entry(name, typ)
D = "double"
for n in ["Drive/X_in","Drive/Y_in","Drive/Heading_deg","Drive/Power/LF","Drive/Power/RF","Drive/Power/LR","Drive/Power/RR",
          "Drive/Velocity/LF","Drive/Velocity/RF","Drive/Velocity/LR","Drive/Velocity/RR","Drive/Pedro/Completion",
          "Drive/Pedro/RemainingDistance_in","Launcher/Velocity","Launcher/Target","Launcher/Wheel/Velocity","Launcher/Wheel/Target",
          "Launcher/Wheel/Motor0","Launcher/Wheel/Motor1","Launcher/Feeder/Power","Launcher/Sequence/Target","Intake/Power",
          "Robot/BatteryVolts","Vision/Forward_in","Vision/Right_in","Vision/SquareUp_deg"]: declare(n, D)
for n in ["Drive/LastMoveSucceeded","Drive/Pedro/Following","Launcher/Ready","Launcher/Wheel/Ready","Launcher/Feeder/Timed","Intake/Timed","Vision/TagVisible"]: declare(n, "boolean")
declare("Launcher/Sequence/ShotsAttempted", "int64"); declare("Launcher/Sequence/ShotsFired", "int64"); declare("Launcher/Sequence/ShotsAborted", "int64")
for n in ["Drive/State","Launcher/State","Launcher/AimSource","Launcher/Sequence/State","Launcher/Sequence/LastAbort","Drive/Pedro/Mode"]: declare(n, "string")
declare("Vision/TagId", "int64")
declare("Drive/Pose", "struct:Pose2d")
ids = ids
SHOT_VEL, wheel = 1500.0, 0.0

def put(name, ts, v):
    t = ids[name]
    if isinstance(v, bool): rec(t, ts, bytes([v]))
    elif isinstance(v, int): rec(t, ts, struct.pack("<q", v))
    elif isinstance(v, str): rec(t, ts, v.encode())
    else: rec(t, ts, struct.pack("<d", v))

def path(t):  # (x, y, heading_deg, state, remaining) in Pedro inches
    wp = [(0, 8, 8, 0), (6, 8, 8, 0), (12, 60, 24, 45), (30, 60, 24, 45), (36, 24, 60, 90)]
    for (t0, x0, y0, h0), (t1, x1, y1, h1) in zip(wp, wp[1:]):
        if t0 <= t <= t1:
            u = (t - t0) / (t1 - t0); u = u * u * (3 - 2 * u)
            return x0 + (x1 - x0) * u, y0 + (y1 - y0) * u, h0 + (h1 - h0) * u, math.hypot(x1 - x0, y1 - y0) * (1 - u)
    return wp[-1][1], wp[-1][2], wp[-1][3], 0.0

shots_fired = 0; shot_start = None
shot_times = [18.0, 21.0, 24.0]
dt = 0.02
for k in range(int(40 / dt) + 1):
    t = k * dt; ts = int(t * 1e6)
    x, y, h, rem = path(t); moving = rem > 0.3 and 6 < t < 36
    state = "FOLLOWING_PATH" if moving else ("HOLDING_POINT" if t >= 6 else "IDLE")
    put("Drive/X_in", ts, x); put("Drive/Y_in", ts, y); put("Drive/Heading_deg", ts, h)
    # Pose2d in AdvantageScope's FTC frame (meters, field-center origin), as LogUtil.logPose writes it
    rec(ids["Drive/Pose"], ts, struct.pack("<ddd", (72 - y) * 0.0254, (x - 72) * 0.0254, math.radians(h + 90))); put("Drive/State", ts, state); put("Drive/LastMoveSucceeded", ts, t > 36)
    put("Drive/Pedro/Following", ts, moving); put("Drive/Pedro/Mode", ts, "AUTO")
    put("Drive/Pedro/Completion", ts, 0.0 if not moving else min(1.0, 1 - rem / 60)); put("Drive/Pedro/RemainingDistance_in", ts, rem)
    p = 0.55 if moving else 0.0
    for m, wob in zip(["LF","RF","LR","RR"], [1, 1.04, 0.97, 1.01]):
        put(f"Drive/Power/{m}", ts, p * wob); put(f"Drive/Velocity/{m}", ts, p * wob * 2200 + random.gauss(0, 15) * (p > 0))
    # launcher: spin up at t=10, three shots, stop at 28
    target = SHOT_VEL if 10 <= t < 28 else 0.0
    wheel += (target - wheel) * 0.06 + random.gauss(0, 4) * (wheel > 50)
    ready = target > 0 and wheel >= 0.97 * target
    seq, feeding = "IDLE", False
    if target and t < 10.7: seq = "SPIN_UP"
    for st in shot_times:
        if st <= t < st + 1.5: seq, feeding = "FEEDING", True
        elif st + 1.5 <= t < st + 1.55: seq = "COOLDOWN"
        elif st - 0.8 <= t < st: seq = "SPIN_UP"
    if feeding: wheel -= 9  # the ball pulls the wheel down
    shots_fired = sum(1 for st in shot_times if t >= st + 1.5)
    shots_att = sum(1 for st in shot_times if t >= st)
    put("Launcher/Velocity", ts, wheel); put("Launcher/Target", ts, target); put("Launcher/State", ts, seq)
    put("Launcher/Ready", ts, ready); put("Launcher/AimSource", ts, "table" if target else "no table")
    put("Launcher/Wheel/Velocity", ts, wheel); put("Launcher/Wheel/Target", ts, target); put("Launcher/Wheel/Ready", ts, ready)
    put("Launcher/Wheel/Motor0", ts, wheel + 6); put("Launcher/Wheel/Motor1", ts, wheel - 6)
    put("Launcher/Feeder/Power", ts, 1.0 if feeding else 0.0); put("Launcher/Feeder/Timed", ts, feeding)
    put("Launcher/Sequence/State", ts, seq); put("Launcher/Sequence/Target", ts, target)
    put("Launcher/Sequence/ShotsAttempted", ts, shots_att); put("Launcher/Sequence/ShotsFired", ts, shots_fired)
    put("Launcher/Sequence/ShotsAborted", ts, 0); put("Launcher/Sequence/LastAbort", ts, "")
    intake_on = 6 <= t < 17 or 30 <= t < 34
    put("Intake/Power", ts, 1.0 if intake_on else 0.0); put("Intake/Timed", ts, False)
    # battery sags with load
    load = 0.9 * p + (1.2 if target else 0) + (0.5 if intake_on else 0)
    put("Robot/BatteryVolts", ts, 13.1 - 0.35 * load - t * 0.006 + random.gauss(0, 0.02))
    # tag visible while the robot is parked near the goal
    vis = 12 <= t < 30
    put("Vision/TagVisible", ts, vis); put("Vision/TagId", ts, 24 if vis else -1)
    if vis:
        put("Vision/Forward_in", ts, 52 - 0.1 * (t - 12) + random.gauss(0, .3)); put("Vision/Right_in", ts, 3 + random.gauss(0, .2))
        put("Vision/SquareUp_deg", ts, 4 * math.exp(-(t - 12)) + random.gauss(0, .2))
f.close(); print("wrote", out)
