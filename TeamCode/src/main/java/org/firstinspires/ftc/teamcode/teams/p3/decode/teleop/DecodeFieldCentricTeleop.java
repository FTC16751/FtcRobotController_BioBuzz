package org.firstinspires.ftc.teamcode.teams.p3.decode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.SharedState;
import org.firstinspires.ftc.teamcode.common.vision.VisionAim;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;

/**
 * DecodeTeleop (same buttons for intake, launcher, alliance, snap-to-goal) with FIELD-centric driving
 * through Pedro: push the stick toward the far wall and the robot goes there whichever way it faces.
 *
 * The pose carries over from the autonomous (SharedState.endPose, saved by DriveUtil.stop()), so the
 * heading is already right when TeleOp starts. With no auto before it, the robot is assumed to point
 * away from the driver at (0, 0). Poses are in the auto's Pedro field frame; stick-forward is
 * heading FORWARD_DEG (0 = toward +x, found on the robot after red_audience; blue would need 180).
 * BACK = the robot is pointing away from the driver right now: fixes the heading, keeps x and y.
 * Hold left stick click for slow mode.
 *
 * Pedro 3 only has follower.manual(forward, left, ccw), which is robot-centric, so DriveUtil's
 * pedroTeleopDrive is fed a stick vector rotated by the heading. Without Pedro in the config this
 * falls back to the normal robot-centric arcade drive. Not run on the robot yet.
 */
@TeleOp(name = "Decode: Teleop Field-Centric (Pedro)", group = "Decode")
public class DecodeFieldCentricTeleop extends DecodeTeleop {

    private static final double FORWARD_DEG = 0;    // heading that stick-forward means, in the auto's field frame

    @Override
    public void init() {
        super.init();
        double[] end = SharedState.endPose;
        if (end != null) robot.drive.setPosition(end[0], end[1], end[2]);
        else             robot.drive.setPosition(0, 0, FORWARD_DEG);
        telemetry.addData("Start pose", end != null ? "from the auto" : "none saved: assuming (0, 0) facing away");
    }

    @Override
    public void start() {
        robot.drive.startPedroTeleopDrive();   // does nothing when Pedro is off
    }

    @Override
    protected void handleDriving() {
        if (gamepad1.backWasPressed()) robot.drive.setPosition(robot.drive.getX(), robot.drive.getY(), FORWARD_DEG);

        double turn = gamepad1.right_stick_x;
        if (gamepad1.right_stick_button && robot.vision.isTargetVisible()) {
            turn = VisionAim.turnPower(robot.vision.getTargetAngleX(),
                    DecodeConstants.Aim.SNAP_KP, DecodeConstants.Aim.SNAP_TOLERANCE_DEG);
        }
        double strafe = gamepad1.left_stick_x;      // field right
        double drive = -gamepad1.left_stick_y;      // field forward (away from the driver)

        if (!robot.drive.isPedroTeleopDrive()) {    // Pedro off: plain robot-centric
            robot.drive.arcadeDrive(strafe, drive, turn, 0, DecodeConstants.Drive.TELEOP_SPEED);
            telemetry.addLine("Pedro OFF: robot-centric driving");
            return;
        }

        // Rotate the field-frame stick vector into the robot frame (heading is counter-clockwise).
        double h = Math.toRadians(robot.drive.getHeadingDegrees() - FORWARD_DEG);
        double robotDrive  = drive * Math.cos(h) - strafe * Math.sin(h);
        double robotStrafe = drive * Math.sin(h) + strafe * Math.cos(h);
        // Pedro's manual power is the real wheel power, so full speed here; TELEOP_SPEED (0.25) is for arcadeDrive.
        double speed = gamepad1.left_stick_button ? DecodeConstants.Drive.TELEOP_SPEED : 1.0;
        robot.drive.pedroTeleopDrive(robotStrafe, robotDrive, turn, speed);
        telemetry.addData("Drive", "FIELD-centric, heading %.0f (BACK = zero)", robot.drive.getHeadingDegrees());
        com.pedropathing.math.Pose p = robot.drive.getFollower().pose();   // diagnostic: Pedro's own view of the pose
        telemetry.addData("Pedro pose", "x %.1f  y %.1f  heading %.1f deg", p.x(), p.y(), Math.toDegrees(p.heading()));
        telemetry.addData("Pinpoint pose", "x %.1f  y %.1f  heading %.1f deg", robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees());
    }
}
