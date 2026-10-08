package org.firstinspires.ftc.teamcode.teams.p3.decode.auto;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.common.drive.DriveUtil;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConfig;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;

/**
 * Park red with Pedro Pathing: one straight line from the start tile to the red parking area,
 * turning from 90 to 180 degrees on the way, then holding there. Drive only (no mechanisms), so it
 * runs with just the drive motors, IMU and Pinpoint wired.
 *
 * Poses are in the Pedro Visualizer's field frame: inches, origin in the field corner, x right,
 * y up the screen, heading counter-clockwise from +x. The start is (56, 8) facing +y; put the
 * robot there. Pedro is tuned on Decode (the Foresight lambda in DecodeConfig); if that is ever removed
 * this OpMode says so and does nothing. This path has not been run on the robot yet.
 */
@Autonomous(name = "Decode: Park Red (Pedro)", group = "Decode", preselectTeleOp = "Decode: Teleop (RUN ME)")
public class DecodePedroParkAuto extends OpMode {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final Pose START = POSES.of(56, 8, 90);
    private static final Pose PARK = POSES.of(9, 106.1, 180);

    private DriveUtil drive;
    private Path toPark;
    private boolean arrived = false;
    private final ElapsedTime runTimer = new ElapsedTime();
    private double finishedAfterSec = 0;

    @Override
    public void init() {
        drive = new DriveUtil(hardwareMap, telemetry, null, DecodeConfig.create());
        if (drive.hasPedro()) {
            toPark = Paths.line(START, PARK).linear(START, PARK);   // heading turns from START's to PARK's along the way
        }
    }

    @Override
    public void init_loop() {
        drive.update();
        telemetry.addData("Status", toPark == null
                ? "Pedro is OFF on this robot (not tuned yet): this auto cannot run"
                : "Ready. Place the robot on (56, 8) facing +y");
        drive.addTelemetry();
    }

    @Override
    public void start() {
        if (toPark == null) return;
        drive.setPosition(START.x(), START.y(), Math.toDegrees(START.heading()));   // wherever we are is the start pose
        Follower f = drive.getFollower();
        ((Foresight) f.algorithm()).config.maxPathSpeed.set(DecodeConstants.Drive.PEDRO_PATH_SPEED);
        runTimer.reset();
        drive.followPath(toPark, true);   // hold the park pose after arriving
    }

    @Override
    public void loop() {
        drive.update();   // steps Pedro (and through it the Pinpoint) while the path runs
        if (toPark == null) {
            telemetry.addLine("Pedro is OFF: run AutoTune and paste the Foresight lambda into DecodeConfig");
            return;
        }
        if (!arrived && !drive.isBusy()) {
            arrived = true;
            finishedAfterSec = runTimer.seconds();
        }

        telemetry.addData("state", arrived ? "ARRIVED (holding)" : "DRIVING");
        telemetry.addData("time", "%.1f s", arrived ? finishedAfterSec : runTimer.seconds());
        telemetry.addData("park", "x %.1f  y %.1f  heading %.0f", PARK.x(), PARK.y(), Math.toDegrees(PARK.heading()));
        Follower f = drive.getFollower();
        telemetry.addData("pedro", "path %.0f%%  %.1f in to go  mode %s", f.completion() * 100, f.remainingDistance(), f.mode());
        drive.addTelemetry();
    }

    @Override
    public void stop() {
        drive.cancel();
        drive.stop();
    }
}
