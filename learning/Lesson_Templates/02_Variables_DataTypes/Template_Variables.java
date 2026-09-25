// ============================================================
//  LESSON 02 — Variables & Data Types
//  Book: Chapter 2  |  No hardware needed
// ============================================================
//
//  INSTRUCTIONS: Fill in every section marked with // TODO
//  After completing, all 5 telemetry lines should appear
//  on the Driver Station when you press INIT.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L02 Variables")
public class L02_Variables extends OpMode {

    // ── CLASS MEMBERS ──────────────────────────────────────────────────────────
    // These variables are available in EVERY method of this class.
    // They keep their value between loop() calls.

    // TODO 1: Declare an int for your team number (value: 16751)
    //   int teamNumber = ???;


    // TODO 2: Declare a double for the robot's max motor power (value: 0.75)
    //   double maxMotorPower = ???;


    // TODO 3: Declare a boolean named "isRobotReady" set to false
    //   boolean isRobotReady = ???;


    // TODO 4: Declare a String for your team's name
    //   String teamName = ???;


    // This is a class-level loop counter (you'll use it in loop())
    int loopCount = 0;


    // ── INIT ───────────────────────────────────────────────────────────────────
    @Override
    public void init() {

        // TODO 5: Set isRobotReady to true (robot is now initialized)
        //   isRobotReady = ???;


        // TODO 6: Send all 4 variables to telemetry using addData()
        //   Format:  telemetry.addData("label", variableName);
        //
        //   Show:
        //     "Team Number"   → teamNumber
        //     "Max Power"     → maxMotorPower
        //     "Robot Ready"   → isRobotReady
        //     "Team Name"     → teamName




        telemetry.update();
    }


    // ── LOOP ───────────────────────────────────────────────────────────────────
    @Override
    public void loop() {

        // Increment the loop counter each time loop() runs
        loopCount++;

        // TODO 7: Send the loop count and runtime to telemetry
        //   telemetry.addData("Loop Count", loopCount);
        //   telemetry.addData("Runtime", getRuntime());




        // TODO 8: Declare a LOCAL variable named "powerPercent" of type double.
        //         Set it to maxMotorPower * 100.0  (converts 0.75 → 75.0)
        //         Then display it with label "Max Power %"
        //
        //   IMPORTANT: This variable must be declared INSIDE loop(), not at the
        //   top of the class. Local variables only exist within their method.
        //
        //   double powerPercent = ???;
        //   telemetry.addData("Max Power %", powerPercent);


    }
}
