// ============================================================
//  LESSON 13 — Arrays & Collections
//  Book: Chapter 13  |  No hardware needed
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import java.util.ArrayList;

@TeleOp(name = "L13 Arrays")
public class L13_Arrays extends OpMode {

    // ── Part A: Arrays ─────────────────────────────────────────────────────────

    // TODO 1: Declare a String array named 'steps' with these 4 values:
    //         "Wait for sensor", "Move servo", "Run motor", "Done"
    //         (These are the steps from the L12 state machine!)
    //
    //   String[] steps = {"Wait for sensor", "Move servo", "Run motor", "Done"};


    // TODO 2: Declare an int to track which step we're on (start at 0)
    //   int currentStep = 0;


    // ── Part B: ArrayList ──────────────────────────────────────────────────────

    // TODO 3: Declare an ArrayList<String> named 'recentEvents'
    //   ArrayList<String> recentEvents = new ArrayList<>();


    // Timing for the step cycler
    double lastStepTime = 0.0;


    @Override
    public void init() {
        // TODO 4: Add 3 initial events to recentEvents using .add()
        //   recentEvents.add("OpMode initialized");
        //   recentEvents.add("Hardware ready");
        //   recentEvents.add("Waiting for PLAY");


        telemetry.addLine("L13: Arrays lesson ready.");
        telemetry.update();
    }

    @Override
    public void loop() {

        // ── ARRAY SECTION ──────────────────────────────────────────────────────

        // TODO 5: Display the current step name using the array and currentStep index
        //   telemetry.addData("Current Step", steps[currentStep]);
        //   telemetry.addData("Step Index", currentStep + " / " + (steps.length - 1));


        // TODO 6: Every 2 seconds, advance to the next step.
        //         When you reach the end, wrap back to 0.
        //
        //   if (getRuntime() > lastStepTime + 2.0) {
        //       currentStep = (currentStep + 1) % steps.length;
        //       lastStepTime = getRuntime();
        //   }


        // TODO 7: Use a for-each loop to display ALL steps at once,
        //         labeling them Step 1, Step 2, etc.
        //         (Hint: you'll need a counter variable alongside the for-each)
        //
        //   int i = 1;
        //   for (String step : steps) {
        //       telemetry.addData("Step " + i, step);
        //       i++;
        //   }


        telemetry.addLine("---");

        // ── ARRAYLIST SECTION ──────────────────────────────────────────────────

        // TODO 8: Display the size of recentEvents
        //   telemetry.addData("Event count", recentEvents.size());


        // TODO 9: When gamepad1.a is pressed, add a new event with the current runtime
        //   if (gamepad1.a) {
        //       recentEvents.add("Button A at " + String.format("%.1f", getRuntime()) + "s");
        //   }


        // TODO 10: Display the last event in the list (index size-1)
        //          Only do this if the list is not empty.
        //
        //   if (recentEvents.size() > 0) {
        //       telemetry.addData("Last event", recentEvents.get(recentEvents.size() - 1));
        //   }


        // TODO 11: When gamepad1.b is pressed, clear the event list
        //   if (gamepad1.b) {
        //       recentEvents.clear();
        //   }

    }
}
