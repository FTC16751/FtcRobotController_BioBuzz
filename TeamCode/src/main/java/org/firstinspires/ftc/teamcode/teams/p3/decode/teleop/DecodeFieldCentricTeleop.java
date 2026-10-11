package org.firstinspires.ftc.teamcode.teams.p3.decode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.vision.VisionAim;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;

/**
 * DecodeTeleop (same buttons for intake, launcher, alliance, snap-to-goal) with FIELD-centric driving
 * through Pedro: push the stick toward the far wall and the robot goes there whichever way it faces.
 *
 * Before the match: point the robot straight away from the driver and press BACK (sets heading to 0).
 * Press BACK again any time the heading drifts. Heading comes from the Pinpoint via Pedro.
 *
 * Pedro 3 only has follower.manual(forward, left, ccw), which is robot-centric, so DriveUtil's
 * pedroTeleopDrive is fed a stick vector rotated by the heading. Without Pedro in the config this
 * falls back to the normal robot-centric arcade drive. Not run on the robot yet.
 */
@TeleOp(name = "Decode: Teleop Field-Centric (Pedro)", group = "Decode")
public class DecodeFieldCentricTeleop extends DecodeTeleop {

    @Override
    public void start() {
        robot.drive.startPedroTeleopDrive();   // does nothing when Pedro is off
    }

    @Override
    protected void handleDriving() {
        if (gamepad1.backWasPressed()) robot.drive.resetPosition();

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
        double h = Math.toRadians(robot.drive.getHeadingDegrees());
        double robotDrive  = drive * Math.cos(h) - strafe * Math.sin(h);
        double robotStrafe = drive * Math.sin(h) + strafe * Math.cos(h);
        robot.drive.pedroTeleopDrive(robotStrafe, robotDrive, turn, DecodeConstants.Drive.TELEOP_SPEED);
        telemetry.addData("Drive", "FIELD-centric, heading %.0f (BACK = zero)", robot.drive.getHeadingDegrees());
    }
}
