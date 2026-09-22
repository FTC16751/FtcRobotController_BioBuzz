package org.firstinspires.ftc.teamcode.teams.demobots;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.drive.DriveUtilSimple;

/**
 * Basic mecanum demo. Motors Front_Left / Front_Right / Rear_Left / Rear_Right (see DriveUtilSimple).
 * No IMU, robot-centric only.
 *
 * Controls (gamepad 1):
 *   Left stick      forward/back and strafe
 *   Right stick X   turn
 *   Left bumper     hold for slow mode
 */
@TeleOp(name = "Demo: Mecanum", group = "DemoBots")
public class MecanumTeleop extends OpMode {

    static final double NORMAL_SPEED = 0.8, SLOW_SPEED = 0.4;

    private final DriveUtilSimple drive = new DriveUtilSimple();

    @Override
    public void init() {
        drive.initMecanum(hardwareMap);
        telemetry.addData("Status", "Initialized");
    }

    @Override
    public void loop() {
        double speed = gamepad1.left_bumper ? SLOW_SPEED : NORMAL_SPEED;
        // arcadeDrive negates left_stick_y itself; pass the raw stick values.
        drive.arcadeDrive(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, 0, speed);
        telemetry.addData("speed", "%.1f", speed);
    }
}
