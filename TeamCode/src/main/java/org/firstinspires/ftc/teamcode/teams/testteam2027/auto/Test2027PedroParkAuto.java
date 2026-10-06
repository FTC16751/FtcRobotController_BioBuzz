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
 * Pedro Pathing from the Visualizer: one straight line from the start tile to the red parking
 * area, turning from 90 to 180 degrees on the way, then holding there.
 *
 * The poses came out of visualizer.pedropathing.com (field map BIOBUZZ 2026-2027, Java Code export,
 * 2026-10-04). Its field frame is ours: inches, origin in the field corner, x right, y up the
 * screen, heading counter-clockwise from +x. The start is (56, 8) facing +y. The visualizer put
 * the end at x = 5.6, which hangs the 17 in chassis over the wall, so it is 9 here; check it on
 * the real field and edit PARK. Nothing has run on the robot.
 *
 * To make another auto: draw it in the visualizer, export Java, paste the poses and the
 * Paths.line / Paths.curve calls into buildPath(), and add states to loop().
 */
@Autonomous(name = "Test2027: Park Red (Pedro)", group = "TestTeam2027", preselectTeleOp = "Test2027: Teleop (RUN ME)")
public class Test2027PedroParkAuto extends OpMode {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final Pose START = POSES.of(56, 8, 90);
    private static final Pose PARK = POSES.of(9, 106.1, 180);

    private enum State { DRIVING, DONE }

    private Test2027Robot robot;
    private Path toPark;
    private State state = State.DRIVING;
    private final ElapsedTime runTimer = new ElapsedTime();
    private double finishedAfterSec = 0;

    @Override
    public void init() {
        robot = new Test2027Robot(hardwareMap, telemetry);
        if (!robot.drive.hasPedro()) {
            telemetry.addLine("This robot's config has no PedroPathingConfig; this auto cannot run.");
            return;
        }
        toPark = buildPath();
        telemetry.addData("Status", "Initialized. Place the robot on (56, 8) facing +y.");
    }

    /** Straight line, heading turning from the start heading to the park heading along the way. */
    private Path buildPath() {
        return Paths.line(START, PARK).linear(START, PARK);
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void start() {
        if (toPark == null) { state = State.DONE; return; }
        robot.drive.setPosition(START.x(), START.y(), Math.toDegrees(START.heading()));  // wherever we are is the start pose
        Follower f = robot.drive.getFollower();
        ((Foresight) f.algorithm()).config.maxPathSpeed.set(Test2027Constants.Drive.AUTO_DRIVE_SPEED);
        runTimer.reset();
        robot.drive.followPath(toPark, true);    // hold the park pose after arriving
        state = State.DRIVING;
    }

    @Override
    public void loop() {
        robot.update();   // steps Pedro (and through it the Pinpoint) while the path runs

        switch (state) {
            case DRIVING:
                if (!robot.drive.isBusy()) {
                    finishedAfterSec = runTimer.seconds();
                    state = State.DONE;
                }
                break;
            case DONE:
                break;   // followPath(..., true) is still holding the park pose
        }

        telemetry.addData("state", state);
        telemetry.addData("position", "x %.1f  y %.1f  heading %.0f (park %.0f, %.0f, %.0f)",
                robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees(),
                PARK.x(), PARK.y(), Math.toDegrees(PARK.heading()));
        telemetry.addData("time", "%.1f s%s", state == State.DONE ? finishedAfterSec : runTimer.seconds(), state == State.DONE ? " (arrived)" : "");
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
