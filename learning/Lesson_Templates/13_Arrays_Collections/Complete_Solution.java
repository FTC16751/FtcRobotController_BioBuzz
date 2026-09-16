// ============================================================
//  LESSON 13 — Complete Solution (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import java.util.ArrayList;

@TeleOp(name = "L13 Arrays SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L13_Arrays_Solution extends OpMode {

    String[] steps = {"Wait for sensor", "Move servo", "Run motor", "Done"};
    int currentStep = 0;
    ArrayList<String> recentEvents = new ArrayList<>();
    double lastStepTime = 0.0;

    @Override
    public void init() {
        recentEvents.add("OpMode initialized");
        recentEvents.add("Hardware ready");
        recentEvents.add("Waiting for PLAY");
        telemetry.addLine("L13 ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        telemetry.addData("Current Step", steps[currentStep]);
        telemetry.addData("Step Index", currentStep + " / " + (steps.length - 1));

        if (getRuntime() > lastStepTime + 2.0) {
            currentStep  = (currentStep + 1) % steps.length;
            lastStepTime = getRuntime();
        }

        int i = 1;
        for (String step : steps) {
            telemetry.addData("Step " + i, step);
            i++;
        }

        telemetry.addLine("---");
        telemetry.addData("Event count", recentEvents.size());

        if (gamepad1.a) {
            recentEvents.add("Button A at " + String.format("%.1f", getRuntime()) + "s");
        }

        if (recentEvents.size() > 0) {
            telemetry.addData("Last event", recentEvents.get(recentEvents.size() - 1));
        }

        if (gamepad1.b) {
            recentEvents.clear();
        }
    }
}
