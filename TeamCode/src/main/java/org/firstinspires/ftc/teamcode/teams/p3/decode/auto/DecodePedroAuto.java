package org.firstinspires.ftc.teamcode.teams.p3.decode.auto;

import static org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants.Waypoints.*;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.common.CommonConstants.Alliance;
import org.firstinspires.ftc.teamcode.common.CommonConstants.Location;
import org.firstinspires.ftc.teamcode.common.vision.VisionAim;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeRobot;

import java.util.ArrayList;
import java.util.List;

/**
 * The Decode queue auto written the way Pedro Pathing 3 wants an auto written, for comparison with
 * DecodeQueueAuto (Pinpoint PID) and DecodePedroQueueAuto (that script, ported state for state).
 * Same driver menu, wait, selectable cycles, gate, per-step timeouts and 27 s forced park.
 *
 * What is different, and why (pedropathing.com/docs/pathing, guide "Autonomous Usage",
 * "Modifying Constants", "Follow States"; reference "End Constraints"):
 *
 *  1. The script is DATA. A step list is built once in start() from the menu choices, instead of
 *     an enum, a queue, four script builders and a 130-line waypoint switch. The four start /
 *     alliance layouts are a small table (layoutFor). Adding a cycle or a layout is one line.
 *  2. Paths are PLANNED, not built from "where the robot is now". Every route starts at the
 *     previous planned stop, like a Visualizer path; Pedro corrects from the closest point on the
 *     path. The park step is the exception: it starts from the robot's real pose, because it is
 *     also the recovery after the 27 s timeout.
 *  3. Speed is a per-path MODIFIER: path.with(config.maxPathSpeed.at(x)). Nothing mutates the
 *     shared config, so one path's speed cannot leak into the next.
 *  4. Align + collect (and align + open gate) are ONE path of two legs; there is no stop between.
 *  5. The robot HOLDS its pose from the moment it reaches the shooting position through the
 *     aim and the shot (holdEnd, then DriveUtil.holdPose). AIM does not drive the wheels itself:
 *     it re-targets the hold with the heading turned by the Limelight's tx, so Pedro's tuned
 *     heading controller does the turning and moveRobot never fights it. This replaces the 0.027
 *     P loop; tune AIM_* below, not a gain.
 *  6. End of a path is Pedro's own (its end constraints: 0.1 in, 0.4 degrees, then a 100 ms
 *     timeout), not 18 mm / 0.055 rad / a hold time. Our step timeouts stay as the backstop.
 *
 * The docs build this with Ivy (sequential(follow(...), ...), Scheduler); Ivy is not in our
 * dependencies, so Step and runSteps() are the same idea in about 40 lines. If Ivy is added, each
 * Step maps onto a Command and runSteps() onto Scheduler.execute().
 *
 * Waypoints are the DecodeConstants.Waypoints tables, still relative to the start pose: start()
 * makes the start (0, 0, 0) through DriveUtil.setPosition, so Pedro needs no field coordinates,
 * but the robot must be placed exactly as for the Pinpoint auto. Needs Pedro on. Not run on the
 * robot yet.
 */
@Autonomous(name = "Decode: Auto (Pedro rewrite)", group = "Decode", preselectTeleOp = "Decode: Teleop (RUN ME)")
public class DecodePedroAuto extends OpMode {

    // ---- Timeouts and aiming (same numbers as the other two autos where they exist)
    private static final double AUTONOMOUS_TIMEOUT_SEC = 27.0;   // leave a 3 s buffer before 30
    private static final double TIMEOUT_DRIVE_SEC   = 5.0;
    private static final double TIMEOUT_COLLECT_SEC = 5.0;       // a spike route is align + collect, so it gets both
    private static final double TIMEOUT_AIM_SEC     = 2.5;
    private static final double TIMEOUT_SHOOT_SEC   = 4.0;
    private static final double AIM_TOLERANCE_DEG   = 1.0;
    private static final double RED_FAR_AIM_OFFSET_DEG = 2.5;     // the far red shot is aimed this far off the tag
    private static final double AIM_SETTLED_DEG     = 1.5;        // the last aim command counts as reached inside this
    private static final double AIM_MIN_BETWEEN_SEC = 0.25;       // let the Limelight see the settled robot before correcting
    private static final int    AIM_MAX_CORRECTIONS = 3;
    private static final double PARK_SPEED          = 0.9;
    private static final double MIN_LEG_IN          = 0.5;        // Pedro cannot build a zero-length line

    private static final PoseFactory POSES = PoseFactory.degrees();

    private DecodeRobot robot;

    // ---- Driver menu
    private Alliance alliance = Alliance.RED;
    private Location location = Location.CLOSE;
    private int selectedCycles = 3;        // 0 = just park, 1 = preload, 2-4 = plus that many spike marks minus one
    private boolean enableGate = false;
    private double waitDuration = 0.0;

    // ---- Run state
    private Layout layout;
    private final List<Step> steps = new ArrayList<>();
    private int index = 0;
    private boolean complete = false;
    private Pose cursor;                  // planned end of the last step added, the start of the next
    private final ElapsedTime autonomousTimer = new ElapsedTime();
    private final ElapsedTime stepTimer = new ElapsedTime();

    // ============================================================================================
    // LAYOUTS: everything that differs between the four starts, as data
    // ============================================================================================

    private static final class Spike {
        final Pose2D align, collect;
        final double alignSpeed, collectSpeed;
        Spike(Pose2D align, Pose2D collect, double alignSpeed, double collectSpeed) {
            this.align = align; this.collect = collect; this.alignSpeed = alignSpeed; this.collectSpeed = collectSpeed;
        }
    }

    private static final class Layout {
        Pose2D start, shoot, park;
        Pose2D gateAlign, gateOpen;       // null when this start has no gate step
        Spike[] spikes;                   // in the order this start drives them
        double shotVelocity, toShootSpeed;
    }

    private static Layout layoutFor(Alliance alliance, Location location) {
        boolean red = alliance == Alliance.RED;
        Layout l = new Layout();
        if (location == Location.CLOSE) {
            l.start = red ? START_RED_CLOSE : START_BLUE_CLOSE;
            l.shoot = red ? RED_CLOSE_SHOOTING_POSITION : BLUE_CLOSE_SHOOTING_POSITION;
            l.park = red ? RED_CLOSE_PARK : BLUE_CLOSE_PARK;
            l.gateAlign = red ? RED_ALIGN_GATE : BLUE_ALIGN_GATE;
            l.gateOpen = red ? RED_OPEN_GATE : BLUE_OPEN_GATE;
            l.shotVelocity = 1150;
            l.toShootSpeed = red ? 0.70 : 0.75;
            l.spikes = red
                    ? new Spike[] {
                        new Spike(RED_CLOSE_SPIKEMARK1_ALIGN, RED_CLOSE_SPIKEMARK1_COLLECT, 0.75, 0.70),
                        new Spike(RED_CLOSE_SPIKEMARK2_ALIGN, RED_CLOSE_SPIKEMARK2_COLLECT, 0.80, 0.75),
                        new Spike(RED_CLOSE_SPIKEMARK3_ALIGN, RED_CLOSE_SPIKEMARK3_COLLECT, 0.80, 0.80)}
                    : new Spike[] {
                        new Spike(BLUE_CLOSE_SPIKEMARK1_ALIGN, BLUE_CLOSE_SPIKEMARK1_COLLECT, 0.75, 0.75),
                        new Spike(BLUE_CLOSE_SPIKEMARK2_ALIGN, BLUE_CLOSE_SPIKEMARK2_COLLECT, 0.75, 0.75),
                        new Spike(BLUE_CLOSE_SPIKEMARK3_ALIGN, BLUE_CLOSE_SPIKEMARK3_COLLECT, 0.80, 0.75)};
        } else {
            l.start = red ? RED_FAR_START_POSITION : START_BLUE_FAR;
            l.shoot = red ? RED_FAR_SHOOTING_POSITION : BLUE_FAR_SHOOTING_POSITION;
            l.park = red ? RED_FAR_PARK_POSITION : BLUE_FAR_PARK_POSITION;
            l.shotVelocity = red ? 1425 : 1420;
            l.toShootSpeed = red ? 0.70 : 0.75;
            l.spikes = red   // far starts drive spike 3, then 2, then 1
                    ? new Spike[] {
                        new Spike(RED_FAR_SPIKEMARK3_ALIGN, RED_FAR_SPIKEMARK3_COLLECT, 0.70, 0.70),
                        new Spike(RED_FAR_SPIKEMARK2_ALIGN, RED_FAR_SPIKEMARK2_COLLECT, 0.70, 0.67),
                        new Spike(RED_FAR_SPIKEMARK1_ALIGN, RED_FAR_SPIKEMARK1_COLLECT, 0.70, 0.70)}
                    : new Spike[] {
                        new Spike(BLUE_FAR_SPIKEMARK3_ALIGN, BLUE_FAR_SPIKEMARK3_COLLECT, 0.75, 0.75),
                        new Spike(BLUE_FAR_SPIKEMARK2_ALIGN, BLUE_FAR_SPIKEMARK2_COLLECT, 0.75, 0.75),
                        new Spike(BLUE_FAR_SPIKEMARK1_ALIGN, BLUE_FAR_SPIKEMARK1_COLLECT, 0.75, 0.75)};
        }
        return l;
    }

    // ============================================================================================
    // STEPS
    // ============================================================================================

    /** One thing the auto does. start() once, update() every loop until it returns true, end() after. */
    private abstract class Step {
        final String name;
        final double timeoutSec;
        Step(String name, double timeoutSec) { this.name = name; this.timeoutSec = timeoutSec; }
        void start() {}
        abstract boolean update();
        /** What to do when timeoutSec passes; the step is abandoned after this. */
        void onTimeout() {}
        void end() {}
    }

    /** Do something once (start the intake and flywheel). */
    private final class Do extends Step {
        private final Runnable action;
        Do(String name, Runnable action) { super(name, 1.0); this.action = action; }
        @Override void start() { action.run(); }
        @Override boolean update() { return true; }
    }

    private final class Wait extends Step {
        private final double seconds;
        private final ElapsedTime timer = new ElapsedTime();
        Wait(double seconds) { super("wait", Double.MAX_VALUE); this.seconds = seconds; }
        @Override void start() { timer.reset(); }
        @Override boolean update() { return timer.seconds() >= seconds; }
    }

    /** Follow a planned path. A hold route keeps holding its last pose until the next step moves. */
    private final class Route extends Step {
        private final Path path;
        private final boolean hold;
        Route(String name, double timeoutSec, Path path, boolean hold) {
            super(name, timeoutSec); this.path = path; this.hold = hold;
        }
        @Override void start() { robot.drive.followPath(path, hold); }
        @Override boolean update() { return !robot.drive.isBusy(); }
        @Override void onTimeout() { robot.drive.cancel(); }
    }

    /** Drive to the park pose from wherever the robot actually is (also the recovery step). */
    private final class Park extends Step {
        Park() { super("park", TIMEOUT_DRIVE_SEC); }
        private boolean started = false;
        @Override void start() {
            Path path = route(POSES.of(robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees()),
                    PARK_SPEED, layout.park);
            started = path != null;
            if (started) robot.drive.followPath(path, false);
        }
        @Override boolean update() { return !started || !robot.drive.isBusy(); }
        @Override void onTimeout() { robot.drive.cancel(); }
    }

    /**
     * Turn to the goal while holding position. Each correction re-targets the hold with the heading
     * turned by the Limelight's tx (tx > 0 is the goal to the right, so heading decreases), waits
     * for Pedro to get there, and looks again. Stops on target, with no tag in view, or after
     * AIM_MAX_CORRECTIONS (it shoots from where it is, as the other autos do).
     */
    private final class Aim extends Step {
        private int corrections;
        private double commandedHeading;
        private final ElapsedTime sinceCommand = new ElapsedTime();
        Aim() { super("aim", TIMEOUT_AIM_SEC); }
        @Override void start() {
            corrections = 0;
            commandedHeading = Double.NaN;
            robot.drive.holdPose();   // hold exactly where we stopped, whether or not the route got here
        }
        @Override boolean update() {
            if (!robot.vision.isTargetVisible()) return true;
            double tx = robot.vision.getTargetAngleX() + aimOffsetDeg();
            if (VisionAim.onTarget(tx, AIM_TOLERANCE_DEG) || corrections >= AIM_MAX_CORRECTIONS) return true;
            double heading = robot.drive.getHeadingDegrees();
            boolean reached = Double.isNaN(commandedHeading)
                    || (Math.abs(Math.IEEEremainder(heading - commandedHeading, 360.0)) < AIM_SETTLED_DEG
                        && sinceCommand.seconds() > AIM_MIN_BETWEEN_SEC);
            if (reached) {
                commandedHeading = heading - tx;
                corrections++;
                sinceCommand.reset();
                robot.drive.holdPose(robot.drive.getX(), robot.drive.getY(), commandedHeading);
            }
            return false;
        }
        // onTimeout: nothing. The robot is still holding; shoot from here.
    }

    /** One shot. The robot is already holding its pose. Closes the stopper when done. */
    private final class Shoot extends Step {
        private int fired;
        Shoot() { super("shoot", TIMEOUT_SHOOT_SEC); }
        @Override void start() { fired = 0; }
        @Override boolean update() {
            robot.launcher.spinUp(layout.shotVelocity);
            robot.stopper.goTo("SHOOT");
            if (robot.launcher.shotDone()) fired++;   // count a finished shot before asking for the next
            if (fired >= 1) return true;
            robot.launcher.shoot();
            return false;
        }
        @Override void end() { robot.stopper.goTo("STOP"); }
    }

    private double aimOffsetDeg() {
        return alliance == Alliance.RED && location == Location.FAR ? RED_FAR_AIM_OFFSET_DEG : 0.0;
    }

    // ============================================================================================
    // BUILDING THE SCRIPT
    // ============================================================================================

    private static Pose pedro(Pose2D p) {
        return POSES.of(p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH), p.getHeading(AngleUnit.DEGREES));
    }

    /** A path from `from` through these stops, heading turning along each leg, capped at `speed`. Null if there is nothing to drive. */
    private Path route(Pose from, double speed, Pose2D... stops) {
        List<Path> legs = new ArrayList<>();
        Pose a = from;
        for (Pose2D stop : stops) {
            Pose b = pedro(stop);
            if (Math.hypot(b.x() - a.x(), b.y() - a.y()) >= MIN_LEG_IN) {
                legs.add(Paths.line(a, b).linear(a, b));
                a = b;
            }
        }
        if (legs.isEmpty()) return null;
        ForesightConfig cfg = ((Foresight) robot.drive.getFollower().algorithm()).config;
        return Paths.path(legs.toArray(new Path[0])).with(cfg.maxPathSpeed.at(speed));   // this path only
    }

    private void addRoute(String name, double timeoutSec, boolean hold, double speed, Pose2D... stops) {
        Path path = route(cursor, speed, stops);
        if (path == null) return;
        steps.add(new Route(name, timeoutSec, path, hold));
        cursor = pedro(stops[stops.length - 1]);
    }

    private void addShot(String routeName) {
        addRoute(routeName, TIMEOUT_DRIVE_SEC, true, layout.toShootSpeed, layout.shoot);   // holdEnd: hold while aiming
        steps.add(new Aim());
        steps.add(new Shoot());
    }

    /** selectedCycles: 0 park only, 1 preload, 2..4 preload plus one spike mark per extra cycle. */
    private void buildScript() {
        steps.clear();
        cursor = pedro(layout.start);

        if (waitDuration > 0) steps.add(new Wait(waitDuration));

        if (selectedCycles >= 1) {
            steps.add(new Do("spin up", () -> {
                robot.intake.in();
                robot.launcher.spinUp(layout.shotVelocity);
            }));
            addShot("to shoot (preload)");
        }
        for (int i = 0; i < Math.min(selectedCycles - 1, layout.spikes.length); i++) {
            Spike s = layout.spikes[i];
            addRoute("spike " + (i + 1), TIMEOUT_DRIVE_SEC + TIMEOUT_COLLECT_SEC, false,
                    Math.min(s.alignSpeed, s.collectSpeed), s.align, s.collect);
            if (i == 0 && enableGate && layout.gateAlign != null) {
                addRoute("gate", 2 * TIMEOUT_DRIVE_SEC, false, 0.5, layout.gateAlign, layout.gateOpen);
            }
            addShot("to shoot (cycle " + (i + 1) + ")");
        }
        steps.add(new Park());   // always last: the global timeout jumps straight here
    }

    // ============================================================================================
    // OPMODE
    // ============================================================================================

    @Override
    public void init() {
        robot = new DecodeRobot(hardwareMap, telemetry);
        telemetry.addData(">", "Robot Initialized. Ready for selections.");
    }

    @Override
    public void init_loop() {
        robot.update();

        if (gamepad1.x) { alliance = Alliance.BLUE; }
        else if (gamepad1.b) { alliance = Alliance.RED; }

        if (gamepad1.y) { location = Location.CLOSE; }
        else if (gamepad1.a) { location = Location.FAR; }

        if (gamepad1.dpadLeftWasPressed() && selectedCycles > 0) { selectedCycles--; }
        else if (gamepad1.dpadRightWasPressed() && selectedCycles < 4) { selectedCycles++; }

        if (gamepad1.leftBumperWasPressed()) { enableGate = false; }
        else if (gamepad1.rightBumperWasPressed()) { enableGate = true; }

        if (gamepad1.dpadUpWasPressed()) { waitDuration += 1; }
        else if (gamepad1.dpadDownWasPressed()) { waitDuration = Math.max(0, waitDuration - 1); }

        telemetry.addLine("--- Autonomous Configuration ---");
        telemetry.addData("Alliance", "%s (X=Blue, B=Red)", alliance);
        telemetry.addData("Location", "%s (Y=Close, A=Far)", location);
        telemetry.addData("Cycles", "%d (DPad Left/Right)", selectedCycles);
        telemetry.addData("Gate", "%s (LB=Off, RB=On)", enableGate ? "ENABLED" : "DISABLED");
        telemetry.addData("Wait Duration", "%.1f sec (DPad Up/Down)", waitDuration);
        telemetry.addLine();
        if (!robot.drive.hasPedro()) telemetry.addLine("!!! Pedro is OFF on this robot: this auto cannot run");
        robot.drive.addTelemetry();
    }

    @Override
    public void start() {
        autonomousTimer.reset();
        if (!robot.drive.hasPedro()) {
            telemetry.log().add("Pedro is OFF on this robot; this auto cannot run");
            complete = true;
            return;
        }
        layout = layoutFor(alliance, location);
        robot.drive.setPosition(layout.start.getX(DistanceUnit.INCH), layout.start.getY(DistanceUnit.INCH),
                layout.start.getHeading(AngleUnit.DEGREES));
        robot.vision.setTargetingAlliance(alliance);
        buildScript();
        index = 0;
        stepTimer.reset();
        steps.get(0).start();
    }

    @Override
    public void loop() {
        robot.update();
        if (complete) {
            robot.stopAll();
            requestOpModeStop();
            return;
        }
        runSteps();

        Step current = index < steps.size() ? steps.get(index) : null;
        telemetry.addData("Path", "%s %s (cycles %d)", alliance, location, selectedCycles);
        telemetry.addData("Step", "%d/%d %s", Math.min(index + 1, steps.size()), steps.size(), current == null ? "done" : current.name);
        telemetry.addData("Step time", "%.1f s", stepTimer.seconds());
        telemetry.addData("Total time", "%.1f / %.0f s", autonomousTimer.seconds(), AUTONOMOUS_TIMEOUT_SEC);
        if (autonomousTimer.seconds() > AUTONOMOUS_TIMEOUT_SEC - 5.0) telemetry.addLine("WARNING: LOW TIME!");
        telemetry.addData("shooter velocity", robot.launcher.getVelocity());
        robot.drive.addTelemetry();
        if (robot.drive.hasPedro()) {
            telemetry.addData("pedro", "mode %s  %.1f in to go", robot.drive.getFollower().mode(), robot.drive.getFollower().remainingDistance());
        }
    }

    /** The whole control flow: global timeout, run the current step, enforce its timeout, advance. */
    private void runSteps() {
        int parkIndex = steps.size() - 1;
        if (autonomousTimer.seconds() > AUTONOMOUS_TIMEOUT_SEC && index < parkIndex) {
            telemetry.log().add("!!! GLOBAL TIMEOUT at %.1f sec - Forcing PARK !!!", autonomousTimer.seconds());
            steps.get(index).end();
            robot.drive.cancel();
            enter(parkIndex);
        }
        if (index >= steps.size()) {
            complete = true;
            return;
        }
        Step step = steps.get(index);
        if (step.update()) {
            step.end();
            enter(index + 1);
        } else if (stepTimer.seconds() > step.timeoutSec) {
            telemetry.log().add("TIMEOUT: %s at %.1f sec", step.name, stepTimer.seconds());
            step.onTimeout();
            step.end();
            enter(index + 1);
        }
    }

    private void enter(int next) {
        index = next;
        stepTimer.reset();
        if (index < steps.size()) steps.get(index).start();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopAll();
    }
}
