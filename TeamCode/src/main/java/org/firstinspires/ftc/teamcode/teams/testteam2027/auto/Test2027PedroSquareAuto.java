package org.firstinspires.ftc.teamcode.teams.testteam2027.auto;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Constants;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Robot;

/**
 * The same 24 in square as Drive Square (Pinpoint), driven by Pedro Pathing as ONE path of four
 * straight lines, heading held at 0 throughout. This is the ADVANCED pattern: the robot follows a
 * planned path through the corners without stopping at each one. It ends in the starting
 * orientation so a chassis corner lands back on the tape corner and the error can be measured
 * with a tape, not guessed.
 *
 * Run both squares from the same tape mark and compare: does each return to the mark within an
 * inch, and how long did each take? That timing is the input to the "Pedro or driveTo" decision
 * (doc/DRIVE_STRATEGY_REVIEW.md, doc/PEDRO_ON_TEST2027.md). Tune Pedro first with AutoTune
 * (test plan section K); an untuned follower will still drive, just not well.
 *
 * Poses come from a PoseFactory in DEGREES: inches, x forward / y left / counter-clockwise, the
 * same axes as the Pinpoint. The start is put at the field centre (72, 72) so a field view draws
 * the square in the middle rather than in a corner.
 */
@Autonomous(name = "Test2027: Drive Square (Pedro)", group = "TestTeam2027", preselectTeleOp = "Test2027: Teleop (RUN ME)")
public class Test2027PedroSquareAuto extends OpMode {

    private static final double SIDE_IN = 24.0;
    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final Pose START = POSES.of(72, 72, 0);

    private enum State { FOLLOWING, DONE }

    private Test2027Robot robot;
    private Path square;
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
        square = buildSquare();
        telemetry.addData("Status", "Initialized");
    }

    /** Four legs, corners counter-clockwise, heading held at 0 the whole way. */
    private Path buildSquare() {
        Pose corner1 = POSES.of(START.x() + SIDE_IN, START.y(),           0);
        Pose corner2 = POSES.of(START.x() + SIDE_IN, START.y() + SIDE_IN, 0);
        Pose corner3 = POSES.of(START.x(),           START.y() + SIDE_IN, 0);
        return Paths.path(
                Paths.line(START,   corner1).constant(START),
                Paths.line(corner1, corner2).constant(START),
                Paths.line(corner2, corner3).constant(START),
                Paths.line(corner3, START  ).constant(START));
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void start() {
        if (square == null) { state = State.DONE; return; }
        robot.drive.setPosition(START.x(), START.y(), Math.toDegrees(START.heading()));  // wherever we are is the start pose
        // Same power cap as the Pinpoint square, so the two times can be compared. Foresight's own
        // default is full power, and AutoTune measures at full power; below that it may brake early.
        Follower f = robot.drive.getFollower();
        ((Foresight) f.algorithm()).config.maxPathSpeed.set(Test2027Constants.Drive.AUTO_DRIVE_SPEED);
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
        telemetry.addData("off the mark", "%.1f in", Math.hypot(robot.drive.getX() - START.x(), robot.drive.getY() - START.y()));
        telemetry.addData("time", "%.1f s%s", state == State.DONE ? finishedAfterSec : runTimer.seconds(), state == State.DONE ? " (finished)" : "");
        if (robot.drive.hasPedro()) {
            Follower f = robot.drive.getFollower();
            telemetry.addData("pedro", "path %.0f%%  %.1f in to go  mode %s", f.completion() * 100, f.remainingDistance(), f.mode());
        }
        robot.addTelemetry();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
