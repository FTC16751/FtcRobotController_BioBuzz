// ============================================================
//  LESSON 20 — Making Robots Drive  (FILE 2 of 2: Arcade Drive OpMode)
//  Book: Chapter 20.1.2
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L20 Arcade Drive")
public class L20_ArcadeDrive extends OpMode {

    L20_TwoMotorDrive drive = new L20_TwoMotorDrive();

    @Override
    public void init() {
        drive.init(hardwareMap);
        telemetry.addLine("L20: Arcade Drive ready. Left stick to drive.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Read the joystick
        // NOTE: stick Y is negated because up = negative on gamepads
        double forward = -gamepad1.left_stick_y;
        double turn    =  gamepad1.left_stick_x;

        // TODO 1: Compute left and right wheel powers using arcade drive formula:
        //         leftPower  = forward + turn
        //         rightPower = forward - turn
        //
        //   double leftPower  = forward + turn;
        //   double rightPower = forward - turn;


        // TODO 2: Call drive.setPowers() with the computed values
        //   drive.setPowers(leftPower, rightPower);


        // TODO 3: Display the values on telemetry
        //   telemetry.addData("Forward", forward);
        //   telemetry.addData("Turn",    turn);
        //   telemetry.addData("L Power", leftPower);
        //   telemetry.addData("R Power", rightPower);


        // TODO 4 (BONUS): Add a turbo button.
        //         When gamepad1.a is held, don't reduce the turn speed.
        //         When not held, reduce turn by 50%:
        //           turn = gamepad1.left_stick_x * (gamepad1.a ? 1.0 : 0.5);
    }
}
