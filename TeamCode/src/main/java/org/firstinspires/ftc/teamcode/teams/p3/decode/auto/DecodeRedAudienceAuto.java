package org.firstinspires.ftc.teamcode.teams.p3.decode.auto;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeRobot;

/**
 * red_audience: the BIOBUZZ red auto for a robot that starts on the AUDIENCE side of the field.
 * A teaching version: one start, plain states, no menu except which plan to run.
 *
 * The plan is picked in init with the D-pad left / right:
 *   PARK_ONLY               just drive to the parking spot
 *   SHOOT_AND_PARK          shoot the preloads from the start, then park
 *   SHOOT_FLOWER_SHOOT_PARK shoot the preloads, drive to our flower and eat it, drive to the
 *                           score-table side, shoot, then park
 *
 * HOW IT WORKS (read this first). The robot is always in exactly one STATE. A state does two things:
 *   1. enter(state)   runs ONCE when the robot moves into the state: start a path, turn something on.
 *   2. loop() switch  runs EVERY loop while it is in the state: is it finished yet? If so, go on.
 * after(state) is the one place that says which state comes next, so the whole route reads top to
 * bottom there. Paths are Pedro Pathing paths, one method each (toFlower, toScoreTable, park).
 *
 * The poses are from the Pedro Visualizer's field map for BIOBUZZ: inches, x across the field,
 * y up the field, heading in degrees counter-clockwise from +x. The robot is placed on START
 * (56, 8) facing +y. Everything marked GUESS below was not measured and has never run.
 *
 * WHICH WAY THE ROBOT FACES: heading = the front of the robot. The intake is at the front and the
 * shooter also fires out the front (photo marked FRONT, 2026-10-07). So at START (heading 90) the
 * shot goes up the field, at TABLE_SHOOT (heading -49) it points down toward the middle of the
 * field, and at FLOWER (heading 180) the intake faces the wall. toScoreTable's reverseTangent leg
 * backs away from the flower with the intake end trailing.
 */
@Autonomous(name = "Decode: red_audience", group = "Decode", preselectTeleOp = "Decode: Teleop (RUN ME)")
public class DecodeRedAudienceAuto extends OpMode {

    // ---- Poses (from the Visualizer)
    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final Pose START         = POSES.of(56, 8, 90);
    private static final Pose FLOWER        = POSES.of(11.4203, 47.0096, 180);       // our flower, on the red wall
    private static final Pose TABLE_MIDWAY  = POSES.of(33.9279, 116.8967, -107.8515);
    private static final Pose TABLE_SHOOT   = POSES.of(37.9018, 112.2953, -49.1853); // shoot from here
    private static final Pose PARK          = POSES.of(9.7462, 94.6078, 0);

    // ---- Mechanism numbers. ALL GUESSES: measure them and edit here.
    private static final int    PRELOAD_SHOTS = 4;     // how many pieces we start with
    private static final int    TABLE_SHOTS   = 4;     // how many we shoot after the flower
    private static final double EAT_FLOWER_SEC = 2.0;  // how long the intake runs at the flower
    private static final double START_SHOT_VELOCITY = DecodeConstants.Launcher.FAR_TARGET_VELOCITY;   // far from the hive
    private static final double TABLE_SHOT_VELOCITY = DecodeConstants.Launcher.CLOSE_TARGET_VELOCITY;
    private static final double PATH_TIMEOUT_SEC  = 8.0;  // give up on a path after this long and carry on
    private static final double SHOOT_TIMEOUT_SEC = 6.0;  // give up on a shot after this long and carry on

    private enum Plan { PARK_ONLY, SHOOT_AND_PARK, SHOOT_FLOWER_SHOOT_PARK }
    private enum State { SHOOT_PRELOADS, TO_FLOWER, EAT_FLOWER, TO_SCORE_TABLE, SHOOT_AT_TABLE, PARK, DONE }

    private DecodeRobot robot;
    private Plan plan = Plan.SHOOT_FLOWER_SHOOT_PARK;
    private State state;
    private int shotsFired = 0;
    private final ElapsedTime stateTimer = new ElapsedTime();

    // ---- The paths. Each is a Pedro Path the robot follows with robot.drive.followPath.

    /** Start to the flower: a straight line, heading turning from 90 to 180 on the way. */
    private Path toFlower() {
        return Paths.line(START, FLOWER).linear(START, FLOWER);
    }

    /** Flower to the score-table side: backwards to the midway point, then forwards to the shooting spot. */
    private Path toScoreTable() {
        return Paths.path(
                Paths.line(FLOWER, TABLE_MIDWAY).reverseTangent(),
                Paths.line(TABLE_MIDWAY, TABLE_SHOOT).tangent());
    }

    /** To the parking spot from wherever the robot is now (so every plan can end with it). */
    private Path park() {
        Pose here = POSES.of(robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees());
        return Paths.line(here, PARK).linear(here, PARK);
    }

    // ---- The order of the states
    private State after(State finished) {
        switch (finished) {
            case SHOOT_PRELOADS: return plan == Plan.SHOOT_FLOWER_SHOOT_PARK ? State.TO_FLOWER : State.PARK;
            case TO_FLOWER:      return State.EAT_FLOWER;
            case EAT_FLOWER:     return State.TO_SCORE_TABLE;
            case TO_SCORE_TABLE: return State.SHOOT_AT_TABLE;
            case SHOOT_AT_TABLE: return State.PARK;
            default:             return State.DONE;   // PARK was the last one
        }
    }

    // ---- What happens ONCE when a state begins
    private void enter(State next) {
        state = next;
        stateTimer.reset();
        boolean shooting = state == State.SHOOT_PRELOADS || state == State.SHOOT_AT_TABLE;
        robot.stopper.goTo(shooting ? "SHOOT" : "STOP");   // the stopper is open only while shooting

        switch (state) {
            case SHOOT_PRELOADS:
                shotsFired = 0;
                robot.launcher.spinUp(START_SHOT_VELOCITY);
                robot.drive.holdPose();                      // stay put while shooting
                break;
            case TO_FLOWER:
                robot.intake.in();
                robot.drive.followPath(toFlower(), true);    // true = keep holding the end pose
                break;
            case EAT_FLOWER:
                break;                                       // the intake is already on and the robot is holding at the flower
            case TO_SCORE_TABLE:
                robot.launcher.spinUp(TABLE_SHOT_VELOCITY);  // spin up on the way
                robot.drive.followPath(toScoreTable(), true);
                break;
            case SHOOT_AT_TABLE:
                shotsFired = 0;
                robot.intake.stop();
                break;
            case PARK:
                robot.launcher.spinDown();
                robot.drive.followPath(park(), false);       // false = stop at the end
                break;
            case DONE:
                robot.stopAll();
                requestOpModeStop();
                break;
        }
    }

    @Override
    public void init() {
        robot = new DecodeRobot(hardwareMap, telemetry);
    }

    @Override
    public void init_loop() {
        robot.update();
        Plan[] plans = Plan.values();
        if (gamepad1.dpadRightWasPressed()) plan = plans[(plan.ordinal() + 1) % plans.length];
        if (gamepad1.dpadLeftWasPressed())  plan = plans[(plan.ordinal() + plans.length - 1) % plans.length];

        telemetry.addData("Plan", "%s  (D-pad left / right)", plan);
        telemetry.addLine("Put the robot on (56, 8) facing the far wall (+y)");
        if (!robot.drive.hasPedro()) telemetry.addLine("!!! Pedro is OFF on this robot: this auto cannot run");
        robot.drive.addTelemetry();
    }

    @Override
    public void start() {
        if (!robot.drive.hasPedro()) {
            telemetry.log().add("Pedro is OFF on this robot; this auto cannot run");
            enter(State.DONE);
            return;
        }
        robot.drive.setPosition(START.x(), START.y(), Math.toDegrees(START.heading()));   // wherever we are is START
        ((Foresight) robot.drive.getFollower().algorithm()).config.maxPathSpeed.set(DecodeConstants.Drive.PEDRO_PATH_SPEED);
        enter(plan == Plan.PARK_ONLY ? State.PARK : State.SHOOT_PRELOADS);
    }

    // ---- What happens EVERY loop: is the current state finished?
    @Override
    public void loop() {
        robot.update();

        boolean finished = false;
        switch (state) {
            case SHOOT_PRELOADS:
            case SHOOT_AT_TABLE:
                int wanted = state == State.SHOOT_PRELOADS ? PRELOAD_SHOTS : TABLE_SHOTS;
                if (robot.launcher.shotDone()) shotsFired++;   // count a finished shot first...
                if (shotsFired >= wanted) finished = true;
                else robot.launcher.shoot();                   // ...then ask for the next one
                break;
            case EAT_FLOWER:
                finished = stateTimer.seconds() >= EAT_FLOWER_SEC;
                break;
            case TO_FLOWER:
            case TO_SCORE_TABLE:
            case PARK:
                finished = !robot.drive.isBusy();              // the path is done
                break;
            case DONE:
                return;
        }

        boolean shootingState = state == State.SHOOT_PRELOADS || state == State.SHOOT_AT_TABLE;
        boolean timedOut = state != State.EAT_FLOWER
                && stateTimer.seconds() > (shootingState ? SHOOT_TIMEOUT_SEC : PATH_TIMEOUT_SEC);
        if (timedOut && !finished) telemetry.log().add("TIMEOUT: %s", state);

        if (finished || timedOut) enter(after(state));

        telemetry.addData("Plan", plan);
        telemetry.addData("State", "%s  (%.1f s)", state, stateTimer.seconds());
        telemetry.addData("Shots", shotsFired);
        robot.drive.addTelemetry();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopAll();
    }
}
