package org.firstinspires.ftc.teamcode.teams.p3.decode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.drive.DriveUtil;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConfig;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;

/**
 * Drive only: no intake, launcher, turret or camera, so it runs with just the drive motors, IMU and
 * Pinpoint wired. Use it for drive checks and for watching the Pinpoint pose while pushing the robot.
 *
 * Gamepad 1: left stick drive and strafe, right stick X turn, left bumper hold for slow mode,
 * Back resets the position to (0, 0) facing 0.
 */
@TeleOp(name = "Decode: Drive Only", group = "Decode")
public class DecodeDriveTeleop extends OpMode {

    private DriveUtil drive;

    @Override
    public void init() {
        drive = new DriveUtil(hardwareMap, telemetry, null, DecodeConfig.create());
    }

    @Override
    public void init_loop() {
        drive.update();
        drive.addTelemetry();
    }

    @Override
    public void loop() {
        drive.update();   // steps the Pinpoint
        if (gamepad1.backWasPressed()) drive.resetPosition();

        double speed = gamepad1.left_bumper ? DecodeConstants.Drive.TELEOP_SPEED : 1.0;
        // arcadeDrive's arguments are (strafe, drive, turn, unused, speed); a forward stick is negative Y.
        drive.arcadeDrive(gamepad1.left_stick_x, -gamepad1.left_stick_y, gamepad1.right_stick_x, 0, speed);

        telemetry.addData("speed", gamepad1.left_bumper ? "SLOW" : "full");
        drive.addTelemetry();
    }

    @Override
    public void stop() {
        drive.stop();
    }
}
