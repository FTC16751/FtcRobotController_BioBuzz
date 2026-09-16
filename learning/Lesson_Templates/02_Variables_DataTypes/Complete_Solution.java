// ============================================================
//  LESSON 02 — Complete Solution (Coach Reference)
//  Book: Chapter 2
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L02 Variables SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L02_Variables_Solution extends OpMode {

    // TODO 1
    int teamNumber = 17651;

    // TODO 2
    double maxMotorPower = 0.75;

    // TODO 3
    boolean isRobotReady = false;

    // TODO 4
    String teamName = "Playful Lobsters";

    int loopCount = 0;

    @Override
    public void init() {
        // TODO 5
        isRobotReady = true;

        // TODO 6
        telemetry.addData("Team Number", teamNumber);
        telemetry.addData("Max Power", maxMotorPower);
        telemetry.addData("Robot Ready", isRobotReady);
        telemetry.addData("Team Name", teamName);
        telemetry.update();
    }

    @Override
    public void loop() {
        loopCount++;

        // TODO 7
        telemetry.addData("Loop Count", loopCount);
        telemetry.addData("Runtime", getRuntime());

        // TODO 8 — local variable (only exists inside this method)
        double powerPercent = maxMotorPower * 100.0;
        telemetry.addData("Max Power %", powerPercent);
    }
}
