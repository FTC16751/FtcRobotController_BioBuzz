package org.firstinspires.ftc.teamcode.teams.testteam2027.auto;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Constants;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Robot;

/**
 * The same 24 in square as Drive Square (Pinpoint), driven by Pedro Pathing as ONE path chain of
 * four straight lines, then a quarter turn on the last leg. This is the ADVANCED pattern: the robot
 * follows a planned path through the corners without stopping at each one.
 *
 * Run both squares from the same tape mark and compare: does each return to the mark within an
 * inch, and how long did each take? That timing is the input to the "Pedro or driveTo" decision
 * (doc/DRIVE_STRATEGY_REVIEW.md, doc/PEDRO_ON_TEST2027.md). Tune Pedro first with the "Tuning"
 * OpMode (group Pedro); an untuned follower will still drive, just not well.
 *
 * Poses are Pedro's: inches, radians, x forward / y left / counter-clockwise, the same axes as the
 * Pinpoint. The start is put at the field centre (72, 72) so the Panels field view draws the
 * square in the middle rather than in a corner.
 */
@Autonomous(name = "Test2027: Drive Square (Pedro)", group = "TestTeam2027", preselectTeleOp = "Test2027: Teleop (RUN ME)")
public class Test2027PedroSquareAuto extends OpMode {

    private static final double SIDE_IN = 24.0;
    private static final Pose START = new Pose(72, 72, 0);

    private enum State { FOLLOWING, DONE }

    private Test2027Robot robot;
    private PathChain square;
    private State state = State.FOLLOWING;
    private final ElapsedTime runTimer = new ElapsedTime();
    private double finishedAfterSec = 0;

    @Override
    public void init() {
        robot = new Test2027Robot(hardwareMap, telemetry);
        if (!robot.drive.hasPedro()) {
            telemetry.addLine("This robot's config has no PedroPathingConfig; this auto cannot run.");
            return;
        }
        square = buildSquare(robot.drive.getFollower());
        telemetry.addData("Status", "Initialized");
    }

    /** Four legs, corners counter-clockwise, heading held at 0 until the last leg turns to 90. */
    private PathChain buildSquare(Follower follower) {
        Pose corner1 = new Pose(START.getX() + SIDE_IN, START.getY(),           0);
        Pose corner2 = new Pose(START.getX() + SIDE_IN, START.getY() + SIDE_IN, 0);
        Pose corner3 = new Pose(START.getX(),           START.getY() + SIDE_IN, 0);
        Pose finish  = new Pose(START.getX(),           START.getY(),           Math.toRadians(90));
        return follower.pathBuilder()
                .addPath(new BezierLine(START, corner1)).setConstantHeadingInterpolation(0)
                .addPath(new BezierLine(corner1, corner2)).setConstantHeadingInterpolation(0)
                .addPath(new BezierLine(corner2, corner3)).setConstantHeadingInterpolation(0)
                .addPath(new BezierLine(corner3, finish)).setLinearHeadingInterpolation(0, finish.getHeading())
                .build();
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void start() {
        if (square == null) { state = State.DONE; return; }
        robot.drive.setPosition(START.getX(), START.getY(), Math.toDegrees(START.getHeading()));  // wherever we are is the start pose
        // Same power cap as the Pinpoint square, so the two times can be compared. Pedro's own
        // default is 1.0, and its tuning was done at 1.0; below that it may brake a little early.
        robot.drive.getFollower().setMaxPower(Test2027Constants.Drive.AUTO_DRIVE_SPEED);
        runTimer.reset();
        robot.drive.followPath(square);    // no hold: stop at the end like the Pinpoint square does
        state = State.FOLLOWING;
    }

    @Override
    public void loop() {
        robot.update();   // steps Pedro (and through it the Pinpoint) while the path runs

        switch (state) {
            case FOLLOWING:
                // A launcher or intake would run here too.
                if (!robot.drive.isBusy()) {
                    finishedAfterSec = runTimer.seconds();
                    state = State.DONE;
                }
                break;
            case DONE:
                robot.drive.stop();
                break;
        }

        telemetry.addData("state", state);
        telemetry.addData("position", "x %.1f  y %.1f  heading %.0f (start 72, 72, 0)",
                robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees());
        telemetry.addData("off the mark", "%.1f in", Math.hypot(robot.drive.getX() - START.getX(), robot.drive.getY() - START.getY()));
        telemetry.addData("time", "%.1f s%s", state == State.DONE ? finishedAfterSec : runTimer.seconds(), state == State.DONE ? " (finished)" : "");
        if (robot.drive.hasPedro()) {
            Follower f = robot.drive.getFollower();
            telemetry.addData("pedro", "path %.0f%%  stuck %s", f.getPathCompletion() * 100, f.isRobotStuck());
        }
        robot.addTelemetry();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
