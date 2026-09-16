// ============================================================
//  LESSON 20 — Complete Solution: Arcade Drive OpMode (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L20 Arcade Drive SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L20_ArcadeDrive_Solution extends OpMode {

    L20_TwoMotorDrive_Solution drive = new L20_TwoMotorDrive_Solution();

    @Override
    public void init() {
        drive.init(hardwareMap);
        telemetry.addLine("L20 Arcade Drive ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double turn    =  gamepad1.left_stick_x * (gamepad1.a ? 1.0 : 0.5);

        double leftPower  = forward + turn;
        double rightPower = forward - turn;

        drive.setPowers(leftPower, rightPower);

        telemetry.addData("Forward",  forward);
        telemetry.addData("Turn",     turn);
        telemetry.addData("L Power",  leftPower);
        telemetry.addData("R Power",  rightPower);
        telemetry.addData("Turbo",    gamepad1.a ? "ON" : "off");
    }
}
