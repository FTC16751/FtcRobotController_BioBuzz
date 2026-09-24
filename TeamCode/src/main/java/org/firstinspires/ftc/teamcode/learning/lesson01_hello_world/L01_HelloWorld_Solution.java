// ============================================================
//  LESSON 01 — Complete Solution (Coach Reference)
//  Book: Chapter 1
// ============================================================
//  NOTE FOR COACHES: Share this ONLY after students have
//  attempted the template on their own.
// ============================================================

package org.firstinspires.ftc.teamcode.learning.lesson01_hello_world;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L01 Hello World SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled   // hide from Driver Station
public class L01_HelloWorld_Solution extends OpMode {

    // TODO 1 + 2 — class-level variables
    int teamNumber = 17651;
    String studentName = "Your Name";

    // Optional challenge variable for loop count
    int loopCount = 0;

    @Override
    public void init() {
        // TODO 3 — telemetry greeting
        telemetry.addData("Hello", studentName);
        telemetry.addData("Team", teamNumber);
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 4 — runtime display
        telemetry.addData("Runtime (sec)", getRuntime());

        // TODO 5 challenge — loop counter
        loopCount++;
        telemetry.addData("Loop #", loopCount);
    }

    // TODO 6 challenge — start() override
    @Override
    public void start() {
        telemetry.addData("Status", "OpMode Started!");
        telemetry.update();
    }
}
