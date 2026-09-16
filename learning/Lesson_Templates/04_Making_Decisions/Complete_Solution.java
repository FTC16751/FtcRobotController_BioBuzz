// ============================================================
//  LESSON 04 — Complete Solution (Coach Reference)
//  Book: Chapter 4
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L04 Decisions SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L04_Decisions_Solution extends OpMode {

    boolean wasAPressed = false;
    int aPressCount = 0;

    @Override
    public void init() {
        telemetry.addLine("L04 ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        double stickY = -gamepad1.left_stick_y;

        // TODO 1
        telemetry.addData("Stick Y", stickY);

        // TODO 2
        if (stickY > 0.5) {
            telemetry.addData("Direction", "FORWARD FAST");
        } else if (stickY > 0.1) {
            telemetry.addData("Direction", "forward slow");
        } else if (stickY < -0.5) {
            telemetry.addData("Direction", "BACKWARD FAST");
        } else if (stickY < -0.1) {
            telemetry.addData("Direction", "backward slow");
        } else {
            telemetry.addData("Direction", "stopped");
        }

        telemetry.addLine("---");

        // TODO 3 — deadzone
        double stickX = gamepad1.left_stick_x;
        if (Math.abs(stickX) < 0.1) {
            stickX = 0.0;
        }
        telemetry.addData("Stick X (filtered)", stickX);

        telemetry.addLine("---");

        // TODO 4 — edge detection
        if (gamepad1.a && !wasAPressed) {
            aPressCount++;
        }
        wasAPressed = gamepad1.a;
        telemetry.addData("A presses", aPressCount);

        telemetry.addLine("---");

        // TODO 5 — speed mode
        String speedMode;
        if (gamepad1.a) {
            speedMode = "TURBO (100%)";
        } else if (gamepad1.b) {
            speedMode = "SLOW (25%)";
        } else {
            speedMode = "NORMAL (50%)";
        }
        telemetry.addData("Speed Mode", speedMode);
    }
}
