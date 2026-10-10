package org.firstinspires.ftc.teamcode.teams.p3.decode.auto;

import org.firstinspires.ftc.teamcode.common.vision.VisionAim;

import com.pedropathing.algorithm.Foresight;
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
import org.firstinspires.ftc.teamcode.common.CommonConstants;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeRobot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * DecodeQueueAuto with Pedro Pathing doing the driving (doc/PEDRO_ON_DECODE.md, section 3). The
 * script, the waypoints and the shot logic are the same; what changed:
 *   - every drive state is a Pedro path from wherever the robot is (followRoute), not driveTo;
 *   - align + collect on a spike mark is ONE path (no stop at the align point), and so is
 *     align + open gate, so the COLLECT_FROM_SPIKE_n and OPEN_GATE states are gone;
 *   - the robot HOLDS its shooting pose (DriveUtil.holdPose) while it aims and shoots, where the
 *     Pinpoint auto only braked;
 *   - the vision turn still uses moveRobot, after cancelling Pedro's hold.
 * Each path is capped at the same speed fraction the Pinpoint auto used for that move (the lower of
 * the two for a two-part path). Pedro's maxPathSpeed is not the same quantity as driveTo's power, so
 * expect to retune them. Waypoints are unchanged and still relative to the start pose: start() makes
 * the start (0, 0, 0) through DriveUtil.setPosition, so Pedro needs no field coordinates, but the robot
 * must be placed exactly as for the Pinpoint auto. It needs
 * Pedro on (tuned) and does nothing without it. Not run on the robot yet; compare its times with
 * "Decode: Auto Queue" from the same start.
 */
@Autonomous(name="Decode: Auto Queue (Pedro)", group = "Decode", preselectTeleOp = "Decode: Teleop (RUN ME)")
public class DecodePedroQueueAuto extends OpMode {

    // --- Subsystems ---
    private DecodeRobot robot;

    // --- OpMode State and Configuration ---
    private CommonConstants.Alliance alliance = CommonConstants.Alliance.RED;
    private CommonConstants.Location location = CommonConstants.Location.CLOSE;

    // --- Master State Machine ---
    private enum AutonomousState { PRE_START, RUNNING_PATH, COMPLETE }
    private AutonomousState autonomousState = AutonomousState.PRE_START;

    // === 1. A SINGLE, UNIFIED STATE MACHINE ===
    // These enums describe the ACTION the robot is taking, not the entire path.
    private enum PathState {
        START,
        DRIVE_TO_SHOOT_PRELOAD,
        AIM_AT_TARGET,
        SHOOT_PRELOAD,
        DRIVE_TO_SPIKE_1,
        DRIVE_TO_SPIKE_2,
        DRIVE_TO_SPIKE_3,
        DRIVE_TO_SHOOT_CYCLE_1,
        SHOOT_CYCLE_1,
        DRIVE_TO_SHOOT_CYCLE_2,
        SHOOT_CYCLE_2,
        PARK,
        INIT_WAIT, WAIT_FOR_TIMER, ALIGN_GATE, DRIVE_TO_SHOOT_CYCLE_3, SHOOT_CYCLE_3, IDLE // A final state for when a path is done
    }
    private PathState currentState = PathState.START;

    // === 2. A "SCRIPT" for each path ===
    // A Queue is a "First-In, First-Out" list, perfect for a sequence of steps.
    private final Queue<PathState> pathScript = new LinkedList<>();
    private int selectedCycles = 3; // Default to 3 cycles
    private boolean enableGate = false; // Default gate enabled

    // === 3. Waypoint and Velocity Variables ===
    // These will be loaded in start() based on the selected path.
    private Pose2D shootingPosition;
    private Pose2D spike1Align;
    private Pose2D spike1Collect;
    private Pose2D spike2Align;
    private Pose2D spike2Collect;
    private Pose2D spike3Align;
    private Pose2D spike3Collect;
    private Pose2D parkPosition;
    private Pose2D alignOpenGate;
    private Pose2D openGate;
    private double shootingVelocity;
    
    // Path-specific parameters
    private double driveToShootSpeed = 0.5;
    private double driveToShootHoldTime;
    private double shootingDriveSpeed;
    private double shootingHoldTime;
    private double spike1AlignSpeed = 0.5;
    private double spike1CollectSpeed= 0.75;
    private double spike2AlignSpeed = 0.5;
    private double spike2CollectSpeed= 0.75;
    private double spike3AlignSpeed = 0.5;
    private double spike3CollectSpeed= 0.75;
    private double openGateAlignSpeed=0.75;
    private double openGateSpeed= 0.5;
    private boolean useFeederOnSpikeMarkCollection;

    // --- Action-Specific Variables ---
    private int shotsFired = 0;
    private static final double TX_ALIGN_KP = 0.027; // Proportional gain for turning
    private static final double TX_ALIGN_TOLERANCE_DEG = 1.0;
    // This adds a small correction to the aim based on the alliance.
    // Based on your request, we add a -2.5 degree offset for the RED alliance.
    private static final double RED_AIM_OFFSET_DEG = 2.5;
    private static final double BLUE_AIM_OFFSET_DEG = 0.0;
    private final ElapsedTime waitTimer = new ElapsedTime();
    private double waitDuration = 0.0;
    private static final double AUTONOMOUS_TIMEOUT_SECONDS = 27.0; // Leave 3 sec buffer before 30s
    private static final double STATE_TIMEOUT_DRIVE = 5.0;
    private static final double STATE_TIMEOUT_COLLECT = 5.0;
    private static final double STATE_TIMEOUT_SHOOT = 4.0;
    private static final double STATE_TIMEOUT_AIM = 2.5;
    private final ElapsedTime autonomousTimer = new ElapsedTime();
    private final ElapsedTime stateTimer = new ElapsedTime();

    //================================================================================
    // INITIALIZATION
    //================================================================================

    @Override
    public void init() {
        robot = new DecodeRobot(hardwareMap, telemetry);
        telemetry.addData(">", "Robot Initialized. Ready for selections.");
    }

    @Override
    public void init_loop() {
        robot.update();


        // Alliance and Location selection
        if (gamepad1.x) { alliance = CommonConstants.Alliance.BLUE; }
        else if (gamepad1.b) { alliance = CommonConstants.Alliance.RED; }

        if (gamepad1.y) { location = CommonConstants.Location.CLOSE; }
        else if (gamepad1.a) { location = CommonConstants.Location.FAR; }

       // Cycle selection
        if (gamepad1.dpadLeftWasPressed() && selectedCycles > 0) { selectedCycles--; }
        else if (gamepad1.dpadRightWasPressed() && selectedCycles < 4) { selectedCycles++; }

        // NEW: Gate toggle
        if (gamepad1.leftBumperWasPressed()) { enableGate = false; }
        else if (gamepad1.rightBumperWasPressed()) { enableGate = true; }

        if (gamepad1.dpadUpWasPressed()) { waitDuration += 1; }
        else if (gamepad1.dpadDownWasPressed()) { waitDuration -= 1; }


        telemetry.addLine("--- Autonomous Configuration ---");
        telemetry.addData("Alliance", "%s (X=Blue, B=Red)", alliance);
        telemetry.addData("Location", "%s (Y=Close, A=Far)", location);
        telemetry.addData("Cycles", "%d (DPad Left/Right)", selectedCycles);
        telemetry.addData("Gate", "%s (LB=Off, RB=On)", enableGate ? "ENABLED" : "DISABLED");
        telemetry.addData("Wait Duration", "%.1f sec (DPad Up/Down)", waitDuration);
        telemetry.addLine();
        telemetry.addLine();
        if (!robot.drive.hasPedro()) telemetry.addLine("!!! Pedro is OFF on this robot: this auto cannot run");
        robot.drive.addTelemetry();   // Pedro status and the Pinpoint pose
    }


    @Override
    public void start() {
        if (!robot.drive.hasPedro()) {
            telemetry.log().add("Pedro is OFF on this robot; this auto cannot run");
            autonomousState = AutonomousState.COMPLETE;
            return;
        }
        autonomousTimer.reset();
        autonomousState = AutonomousState.RUNNING_PATH;

        // --- 4. BUILD THE SCRIPT AND LOAD THE WAYPOINTS ---
        // This block now defines the entire "plan" for the chosen path.

        if (alliance == CommonConstants.Alliance.RED) {
            if (location == CommonConstants.Location.CLOSE) {
                // Set the waypoints for this path
                shootingPosition = DecodeConstants.Waypoints.RED_CLOSE_SHOOTING_POSITION;

                spike1Align = DecodeConstants.Waypoints.RED_CLOSE_SPIKEMARK1_ALIGN;
                spike1Collect = DecodeConstants.Waypoints.RED_CLOSE_SPIKEMARK1_COLLECT;

                spike2Align = DecodeConstants.Waypoints.RED_CLOSE_SPIKEMARK2_ALIGN;
                spike2Collect = DecodeConstants.Waypoints.RED_CLOSE_SPIKEMARK2_COLLECT;

                alignOpenGate = DecodeConstants.Waypoints.RED_ALIGN_GATE;
                openGate = DecodeConstants.Waypoints.RED_OPEN_GATE;

                parkPosition = DecodeConstants.Waypoints.RED_CLOSE_PARK;

                spike3Align = DecodeConstants.Waypoints.RED_CLOSE_SPIKEMARK3_ALIGN;
                spike3Collect = DecodeConstants.Waypoints.RED_CLOSE_SPIKEMARK3_COLLECT;

                shootingVelocity = 1150;
                
                // Set path-specific parameters
                driveToShootSpeed = 0.70;
                driveToShootHoldTime = 0.125;
                shootingDriveSpeed = 0.5;
                shootingHoldTime = 0.0;
                spike1AlignSpeed = 0.75;
                spike1CollectSpeed = 0.70;
                spike2AlignSpeed = 0.8;
                spike2CollectSpeed = 0.75;
                spike3AlignSpeed = 0.8;
                spike3CollectSpeed = 0.80;
                useFeederOnSpikeMarkCollection = true;

                // Build the script for Red Close
                buildRedCloseScript();

            } else { // RED FAR
                shootingPosition = DecodeConstants.Waypoints.RED_FAR_SHOOTING_POSITION;

                spike1Align = DecodeConstants.Waypoints.RED_FAR_SPIKEMARK1_ALIGN;      // NEW
                spike1Collect = DecodeConstants.Waypoints.RED_FAR_SPIKEMARK1_COLLECT;  // NEW

                spike2Align = DecodeConstants.Waypoints.RED_FAR_SPIKEMARK2_ALIGN;
                spike2Collect = DecodeConstants.Waypoints.RED_FAR_SPIKEMARK2_COLLECT;

                spike3Align = DecodeConstants.Waypoints.RED_FAR_SPIKEMARK3_ALIGN;
                spike3Collect = DecodeConstants.Waypoints.RED_FAR_SPIKEMARK3_COLLECT;

                parkPosition = DecodeConstants.Waypoints.RED_FAR_PARK_POSITION;

                shootingVelocity = 1425;

                // Set path-specific parameters
                driveToShootSpeed = 0.7;
                driveToShootHoldTime = 0.25;
                shootingDriveSpeed = 0.5;
                shootingHoldTime = 0.0;
                spike1AlignSpeed = 0.7;      // NEW
                spike1CollectSpeed = 0.7;    // NEW
                spike2AlignSpeed = 0.7;
                spike2CollectSpeed = 0.67;
                spike3AlignSpeed = 0.7;
                spike3CollectSpeed = 0.7;
                useFeederOnSpikeMarkCollection = true;

                robot.stopper.goTo("STOP");

                // Build the script for Red Far
                buildRedFarScript();
            }
        } else { // BLUE
            if (location == CommonConstants.Location.CLOSE) {
                shootingPosition = DecodeConstants.Waypoints.BLUE_CLOSE_SHOOTING_POSITION;

                spike1Align = DecodeConstants.Waypoints.BLUE_CLOSE_SPIKEMARK1_ALIGN;
                spike1Collect = DecodeConstants.Waypoints.BLUE_CLOSE_SPIKEMARK1_COLLECT;

                spike2Align = DecodeConstants.Waypoints.BLUE_CLOSE_SPIKEMARK2_ALIGN;
                spike2Collect = DecodeConstants.Waypoints.BLUE_CLOSE_SPIKEMARK2_COLLECT;

                spike3Align = DecodeConstants.Waypoints.BLUE_CLOSE_SPIKEMARK3_ALIGN;
                spike3Collect = DecodeConstants.Waypoints.BLUE_CLOSE_SPIKEMARK3_COLLECT;

                alignOpenGate = DecodeConstants.Waypoints.BLUE_ALIGN_GATE;
                openGate = DecodeConstants.Waypoints.BLUE_OPEN_GATE;

                parkPosition = DecodeConstants.Waypoints.BLUE_CLOSE_PARK;

                shootingVelocity = 1150;

                // Set path-specific parameters
                driveToShootSpeed = 0.75;
                driveToShootHoldTime = 0.20;
                shootingDriveSpeed = 0.6;
                shootingHoldTime = 0.0;
                spike1AlignSpeed = 0.75;
                spike1CollectSpeed = 0.75;
                spike2AlignSpeed = 0.75;
                spike2CollectSpeed = 0.75;
                spike3AlignSpeed = 0.80;
                spike3CollectSpeed = 0.75;
                useFeederOnSpikeMarkCollection = true;

                // Build script for Blue Close
                buildBlueCloseScript();

            } else { // BLUE FAR
                shootingPosition = DecodeConstants.Waypoints.BLUE_FAR_SHOOTING_POSITION;

                spike1Align = DecodeConstants.Waypoints.BLUE_FAR_SPIKEMARK1_ALIGN;
                spike1Collect = DecodeConstants.Waypoints.BLUE_FAR_SPIKEMARK1_COLLECT;

                spike2Align = DecodeConstants.Waypoints.BLUE_FAR_SPIKEMARK2_ALIGN;
                spike2Collect = DecodeConstants.Waypoints.BLUE_FAR_SPIKEMARK2_COLLECT;

                spike3Align = DecodeConstants.Waypoints.BLUE_FAR_SPIKEMARK3_ALIGN;
                spike3Collect = DecodeConstants.Waypoints.BLUE_FAR_SPIKEMARK3_COLLECT;

                parkPosition = DecodeConstants.Waypoints.BLUE_FAR_PARK_POSITION;

                shootingVelocity = 1420;

                // Set path-specific parameters
                driveToShootSpeed = 0.75;
                driveToShootHoldTime = 0.20;
                shootingDriveSpeed = 0.6;
                shootingHoldTime = 0.0;
                spike1AlignSpeed = 0.75;      // NEW
                spike1CollectSpeed = 0.75;    // NEW
                spike2AlignSpeed = 0.75;
                spike2CollectSpeed = 0.75;
                spike3AlignSpeed = 0.75;
                spike3CollectSpeed = 0.75;
                useFeederOnSpikeMarkCollection = true;

                // Build the most complex script for Blue Far
                buildBlueFarScript();
            }
        }

        // Set the robot's starting position (through DriveUtil, so Pedro's pose and the Pinpoint agree)
        Pose2D startPose;
        if (location == CommonConstants.Location.CLOSE) {
            startPose = (alliance == CommonConstants.Alliance.RED) ?
                    DecodeConstants.Waypoints.START_RED_CLOSE : DecodeConstants.Waypoints.START_BLUE_CLOSE;
        } else { // FAR
            startPose = (alliance == CommonConstants.Alliance.RED) ?
                    DecodeConstants.Waypoints.RED_FAR_START_POSITION : DecodeConstants.Waypoints.START_BLUE_FAR;
        }
        robot.drive.setPosition(startPose.getX(DistanceUnit.INCH), startPose.getY(DistanceUnit.INCH),
                startPose.getHeading(AngleUnit.DEGREES));
        robot.vision.setTargetingAlliance(alliance);
        telemetry.log().add("Vision pipeline set for: " + alliance);
        // Get the first state from the script
        currentState = getNextState();

        // Transition to the main execution state
        autonomousState = AutonomousState.RUNNING_PATH;
    }

    private void buildRedCloseScript() {
        // Optional wait
        if (waitDuration > 0) {
            pathScript.add(PathState.INIT_WAIT);
            pathScript.add(PathState.WAIT_FOR_TIMER);
        }

        // CYCLE 1: Preload
        if (selectedCycles >= 1) {
            pathScript.add(PathState.DRIVE_TO_SHOOT_PRELOAD);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_PRELOAD);
        }

        // CYCLE 2: Spike 1
        if (selectedCycles >= 2) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_1);

            // Gate opening (only if enabled and we're doing cycle 2+)
            if (enableGate) {
                pathScript.add(PathState.ALIGN_GATE);
            }

            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_1);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_1);
        }

        // CYCLE 3: Spike 2
        if (selectedCycles >= 3) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_2);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_2);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_2);
        }

        // CYCLE 4: Spike 3
        if (selectedCycles >= 4) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_3);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_3);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_3);
        }

        // Always park at the end
        pathScript.add(PathState.PARK);
    }

    private void buildRedFarScript() {
        if (waitDuration > 0) {
            pathScript.add(PathState.INIT_WAIT);
            pathScript.add(PathState.WAIT_FOR_TIMER);
        }

        // CYCLE 1: Preload
        if (selectedCycles >= 1) {
            pathScript.add(PathState.DRIVE_TO_SHOOT_PRELOAD);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_PRELOAD);
        }

        // CYCLE 2: Spike 3
        if (selectedCycles >= 2) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_3);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_1);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_1);
        }

        // CYCLE 3: Spike 2
        if (selectedCycles >= 3) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_2);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_2);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_2);
        }

        // CYCLE 4: Spike 1 (NEW!)
        if (selectedCycles >= 4) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_1);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_3);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_3);
        }

        pathScript.add(PathState.PARK);
    }

    private void buildBlueCloseScript() {
        if (waitDuration > 0) {
            pathScript.add(PathState.INIT_WAIT);
            pathScript.add(PathState.WAIT_FOR_TIMER);
        }

        // CYCLE 1: Preload
        if (selectedCycles >= 1) {
            pathScript.add(PathState.DRIVE_TO_SHOOT_PRELOAD);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_PRELOAD);
        }

        // CYCLE 2: Spike 1
        if (selectedCycles >= 2) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_1);

            // Gate for Blue Close comes after first cycle
            if (enableGate) {
                pathScript.add(PathState.ALIGN_GATE);
            }

            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_1);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_1);
        }

        // CYCLE 3: Spike 2
        if (selectedCycles >= 3) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_2);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_2);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_2);
        }

        // CYCLE 4: Spike 3 (NEW!)
        if (selectedCycles >= 4) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_3);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_3);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_3);
        }

        pathScript.add(PathState.PARK);
    }

    private void buildBlueFarScript() {
        if (waitDuration > 0) {
            pathScript.add(PathState.INIT_WAIT);
            pathScript.add(PathState.WAIT_FOR_TIMER);
        }

        // CYCLE 1: Preload
        if (selectedCycles >= 1) {
            pathScript.add(PathState.DRIVE_TO_SHOOT_PRELOAD);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_PRELOAD);
        }

        // CYCLE 2: Spike 3
        if (selectedCycles >= 2) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_3);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_1);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_1);
        }

        // CYCLE 3: Spike 2
        if (selectedCycles >= 3) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_2);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_2);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_2);
        }

        // CYCLE 4: Spike 1 (NEW!)
        if (selectedCycles >= 4) {
            pathScript.add(PathState.DRIVE_TO_SPIKE_1);
            pathScript.add(PathState.DRIVE_TO_SHOOT_CYCLE_3);
            pathScript.add(PathState.AIM_AT_TARGET);
            pathScript.add(PathState.SHOOT_CYCLE_3);
        }

        pathScript.add(PathState.PARK);
    }

    //================================================================================
    // MAIN LOOP
    //================================================================================

    @Override
    public void loop() {
        robot.update();
        switch (autonomousState) {
            case RUNNING_PATH:
                // --- 5. RUN THE SINGLE, UNIFIED STATE MACHINE ---
                runPath();
                break;
            case COMPLETE:
                robot.stopAll();
                requestOpModeStop();
                break;
        }

//        telemetry.addData("Current Path", alliance + " " + location);
//        telemetry.addData("Current State", currentState);
        // Enhanced telemetry with warnings
        telemetry.addData("Path", "%s %s (Cycle %d)", alliance, location, selectedCycles);
        telemetry.addData("State", currentState);
        telemetry.addData("State Time", "%.1f s", stateTimer.seconds());
        telemetry.addData("Total Time", "%.1f / %.0f s",
                autonomousTimer.seconds(), AUTONOMOUS_TIMEOUT_SECONDS);

        // Visual warning if running out of time
        if (autonomousTimer.seconds() > AUTONOMOUS_TIMEOUT_SECONDS - 5.0) {
            telemetry.addLine("⚠️ WARNING: LOW TIME!");
        }
        telemetry.addData("imu heading: ", robot.drive.heading);
        telemetry.addData("robot location X: ", robot.drive.getOdoPosition().getX(DistanceUnit.INCH));
        telemetry.addData("robot location: Y ", robot.drive.getOdoPosition().getY(DistanceUnit.INCH));
        telemetry.addData("robot location: HEADING", robot.drive.getOdoPosition().getHeading(AngleUnit.DEGREES));
        telemetry.addData("shooter velocity: ", robot.launcher.getVelocity());
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) {
            robot.stopAll();
        }
    }

    /**
     * A helper method to get the next state from the script queue.
     * If the queue is empty, it returns the IDLE state.
     */
    private PathState getNextState() {
        stateTimer.reset();
        pathStarted = false;
        if (!pathScript.isEmpty()) {
            return pathScript.poll(); // .poll() retrieves and removes the head of the queue
        }
        return PathState.IDLE;
    }

    //================================================================================
    // PEDRO HELPERS
    //================================================================================

    private static final PoseFactory POSES = PoseFactory.degrees();
    /** A leg shorter than this is skipped: Pedro cannot build a zero-length line. */
    private static final double MIN_LEG_IN = 0.5;
    /** Set when the current state has started its path (or its aim); getNextState() clears it. */
    private boolean pathStarted = false;

    private static Pose toPedro(Pose2D p) {
        return POSES.of(p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH), p.getHeading(AngleUnit.DEGREES));
    }

    /** One path from the robot's current pose through these stops, heading turning along each leg. Null if already there. */
    private Path buildRoute(Pose2D... stops) {
        Pose from = POSES.of(robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees());
        List<Path> legs = new ArrayList<>();
        for (Pose2D stop : stops) {
            Pose to = toPedro(stop);
            if (Math.hypot(to.x() - from.x(), to.y() - from.y()) >= MIN_LEG_IN) {
                legs.add(Paths.line(from, to).linear(from, to));
                from = to;
            }
        }
        return legs.isEmpty() ? null : Paths.path(legs.toArray(new Path[0]));
    }

    /**
     * Drive through these stops with Pedro, one path per state. Call every loop from the state;
     * returns true when the state is done (arrived, already there, or timed out).
     * @param speed    path speed cap, a fraction of top speed
     * @param holdEnd  keep holding the last pose after arriving (until the next move or cancel)
     */
    private boolean followRoute(double speed, boolean holdEnd, double timeoutSec, Pose2D... stops) {
        if (!pathStarted) {
            pathStarted = true;
            Path route = buildRoute(stops);
            if (route == null) return true;
            ((Foresight) robot.drive.getFollower().algorithm()).config.maxPathSpeed.set(speed);
            robot.drive.followPath(route, holdEnd);
            return false;
        }
        if (!robot.drive.isBusy()) return true;
        if (stateTimer.seconds() > timeoutSec) {
            telemetry.log().add("TIMEOUT: %s at %.1f sec", currentState, stateTimer.seconds());
            robot.drive.cancel();
            return true;
        }
        return false;
    }

    /** End of AIM_AT_TARGET: stop turning and hold this pose for the shots. */
    private void endAim() {
        robot.drive.stopRobot();
        robot.drive.holdPose();
        currentState = getNextState();
    }

    /**
     * A reusable helper method for the shooting cycle. The robot is already holding its pose
     * (endAim), so this only runs the flywheel, the stopper and the shot sequence.
     * @param totalShots The number of artifacts to shoot in this cycle.
     * @return true when all shots have been fired, false otherwise.
     */
    private boolean shootCycle(int totalShots) {
        robot.launcher.spinUp(shootingVelocity);
        robot.stopper.goTo("SHOOT");

        // Count a finished shot before asking for the next, so the last shot is not followed by one more.
        if (robot.launcher.shotDone()) {
            shotsFired++;
        }
        if (shotsFired < totalShots) {
            robot.launcher.shoot();
            return false; // Still shooting
        }
        return true; // Finished shooting all shots for this cycle
    }

    //================================================================================
    // THE UNIFIED PATH METHOD
    //================================================================================

    private void runPath() {
        if (autonomousTimer.seconds() > AUTONOMOUS_TIMEOUT_SECONDS) {
            if (currentState != PathState.PARK && currentState != PathState.IDLE) {
                telemetry.log().add("!!! GLOBAL TIMEOUT at %.1f sec - Forcing PARK !!!", autonomousTimer.seconds());
                pathScript.clear();
                pathScript.add(PathState.PARK);
                currentState = getNextState();
                return; // Exit immediately to start parking
            }
        }

        switch (currentState) {
            case START:
                currentState = getNextState();
                break;

            case DRIVE_TO_SHOOT_PRELOAD:
                robot.intake.in();
                robot.launcher.spinUp(shootingVelocity);
                if (followRoute(driveToShootSpeed, true, STATE_TIMEOUT_DRIVE, shootingPosition)) {
                    shotsFired = 0;
                    currentState = getNextState();
                }
                break;

            case AIM_AT_TARGET:
                if (!pathStarted) {
                    pathStarted = true;
                    robot.drive.cancel();   // let go of Pedro's hold: the vision turn drives the wheels itself
                }
                if (stateTimer.seconds() > STATE_TIMEOUT_AIM) {
                    telemetry.log().add("TIMEOUT: AIM_AT_TARGET at %.1f sec", stateTimer.seconds());
                    endAim();
                    break;
                }
                if (!robot.vision.isTargetVisible()) {
                    // We can't see the tag. For safety, stop turning and shoot from where we are.
                    endAim();
                    break;
                }
                double txError = robot.vision.getTargetAngleX();
                if (alliance == CommonConstants.Alliance.RED && location == CommonConstants.Location.FAR) {
                    txError += RED_AIM_OFFSET_DEG;
                } else if (alliance == CommonConstants.Alliance.BLUE && location == CommonConstants.Location.FAR) {
                    txError += BLUE_AIM_OFFSET_DEG;
                }
                if (VisionAim.onTarget(txError, TX_ALIGN_TOLERANCE_DEG)) {
                    endAim();
                } else {
                    robot.drive.moveRobot(0, 0, VisionAim.turnPower(txError, TX_ALIGN_KP, TX_ALIGN_TOLERANCE_DEG));
                }
                telemetry.addData("Aiming", "Running... Error: %.1f", robot.vision.getTargetAngleX());
                break;

            case SHOOT_PRELOAD:
                if (shootCycle(1)) {
                    currentState = getNextState();
                }
                break;

            // Align and collect are one path: to the align point, then straight on to the collect point.
            case DRIVE_TO_SPIKE_1:
                robot.stopper.goTo("STOP");
                if (followRoute(Math.min(spike1AlignSpeed, spike1CollectSpeed), false,
                        STATE_TIMEOUT_DRIVE + STATE_TIMEOUT_COLLECT, spike1Align, spike1Collect)) {
                    currentState = getNextState();
                }
                break;

            case DRIVE_TO_SPIKE_2:
                robot.stopper.goTo("STOP");
                if (followRoute(Math.min(spike2AlignSpeed, spike2CollectSpeed), false,
                        STATE_TIMEOUT_DRIVE + STATE_TIMEOUT_COLLECT, spike2Align, spike2Collect)) {
                    currentState = getNextState();
                }
                break;

            case DRIVE_TO_SPIKE_3:
                robot.stopper.goTo("STOP");
                if (followRoute(Math.min(spike3AlignSpeed, spike3CollectSpeed), false,
                        STATE_TIMEOUT_DRIVE + STATE_TIMEOUT_COLLECT, spike3Align, spike3Collect)) {
                    currentState = getNextState();
                }
                break;

            // Align the gate and open it in one path.
            case ALIGN_GATE:
                if (followRoute(Math.min(openGateAlignSpeed, openGateSpeed), false,
                        STATE_TIMEOUT_DRIVE * 2, alignOpenGate, openGate)) {
                    currentState = getNextState();
                }
                break;

            case DRIVE_TO_SHOOT_CYCLE_1:
            case DRIVE_TO_SHOOT_CYCLE_2:
            case DRIVE_TO_SHOOT_CYCLE_3:
                if (followRoute(driveToShootSpeed, true, STATE_TIMEOUT_DRIVE, shootingPosition)) {
                    shotsFired = 0;
                    currentState = getNextState();
                }
                break;

            case SHOOT_CYCLE_1:
            case SHOOT_CYCLE_2:
            case SHOOT_CYCLE_3:
                if (shootCycle(1)) {
                    currentState = getNextState();
                }
                else if (stateTimer.seconds() > STATE_TIMEOUT_SHOOT) {
                    telemetry.log().add("TIMEOUT: SHOOTING at %.1f sec (fired %d)",
                            stateTimer.seconds(), shotsFired);
                    currentState = getNextState();
                }
                break;

            case PARK:
                robot.stopper.goTo("STOP");
                if (followRoute(0.9, false, STATE_TIMEOUT_DRIVE, parkPosition)) {
                    currentState = getNextState();
                }
                break;

            case INIT_WAIT:
                waitTimer.reset();
                currentState = getNextState();
                break;

            case WAIT_FOR_TIMER:
                if (waitTimer.seconds() >= waitDuration) {
                    currentState = getNextState();
                }
                break;

            case IDLE:
                // The script is empty, so the entire path is done.
                autonomousState = AutonomousState.COMPLETE;
                break;
        }
    }
}
