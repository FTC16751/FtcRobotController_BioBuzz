// ============================================================
//  LESSON 05 — Complete Solution: OpMode (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L05 Classes SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L05_Classes_Solution extends OpMode {

    RobotStatus_Solution robot = new RobotStatus_Solution();
    boolean wasAPressed = false;

    @Override
    public void init() {
        robot.setMotorPower(0.0);
        robot.setServoPosition(0.5);
        robot.setRunning(false);

        telemetry.addData("Motor Power",   robot.getMotorPower());
        telemetry.addData("Servo Pos",     robot.getServoPosition());
        telemetry.addData("Running",       robot.isRunning());
        telemetry.addData("Instance Count", RobotStatus_Solution.getInstanceCount());
        telemetry.update();
    }

    @Override
    public void loop() {
        robot.setMotorPower(-gamepad1.left_stick_y);
        robot.setServoPosition(gamepad1.right_trigger);

        if (gamepad1.a && !wasAPressed) {
            robot.setRunning(!robot.isRunning());
        }
        wasAPressed = gamepad1.a;

        telemetry.addData("Status", robot);

        // Clamp demonstration
        robot.setMotorPower(5.0);
        telemetry.addData("Clamped Power", robot.getMotorPower()); // shows 1.0
        robot.setMotorPower(-gamepad1.left_stick_y); // restore
    }
}
