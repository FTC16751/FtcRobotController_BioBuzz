package org.firstinspires.ftc.teamcode.teams.demobots;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.drive.DriveUtilSimple;

/**
 * Two-wheel pushbot demo. Motors "left_drive" and "right_drive" (see DriveUtilSimple).
 *
 * Controls (gamepad 1):
 *   Left stick Y    forward/back
 *   Right stick X   turn
 *   Left bumper     hold for slow mode
 */
@TeleOp(name = "Demo: Pushbot", group = "DemoBots")
public class PushbotTeleop extends OpMode {

    static final double NORMAL_SPEED = 0.8, SLOW_SPEED = 0.4;

    private final DriveUtilSimple drive = new DriveUtilSimple();

    @Override
    public void init() {
        drive.initTank(hardwareMap);
        telemetry.addData("Status", "Initialized");
    }

    @Override
    public void loop() {
        double speed   = gamepad1.left_bumper ? SLOW_SPEED : NORMAL_SPEED;
        double forward = -gamepad1.left_stick_y * speed;
        double rotate  =  gamepad1.right_stick_x * speed;
        drive.simpleDrive(forward, rotate);
        telemetry.addData("forward / rotate", "%.2f / %.2f", forward, rotate);
    }
}
