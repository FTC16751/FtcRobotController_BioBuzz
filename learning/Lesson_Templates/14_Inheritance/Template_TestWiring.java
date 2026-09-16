// ============================================================
//  LESSON 14 — Inheritance  (FILE 4 of 4: TestWiring OpMode)
//  Book: Chapter 14, Listing 14.8
// ============================================================
//  This OpMode works with ANY TestItem subclass.
//  Add/remove items from getTests() without touching this file.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import java.util.ArrayList;

@TeleOp(name = "L14 Test Wiring")
public class L14_TestWiring extends OpMode {

    // A list that can hold ANY L14_TestItem (including subclasses)
    ArrayList<L14_TestItem> tests;

    // Navigation state
    boolean wasUp   = false;
    boolean wasDown = false;
    int testNum = 0;

    // ── BUILD THE TEST LIST ───────────────────────────────────────────────────
    // TODO 1: Create an ArrayList<L14_TestItem> and add items.
    //         Create the hardware objects first, then wrap them in TestItem subclasses.
    //
    ArrayList<L14_TestItem> buildTests() {
        ArrayList<L14_TestItem> list = new ArrayList<>();

        // TODO 1a: Get motor from hardwareMap, create an L14_TestMotor, add to list
        //   DcMotor motor = hardwareMap.get(DcMotor.class, "motor");
        //   motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //   list.add(new L14_TestMotor("Motor", 0.5, motor));

        // TODO 1b: Get touch sensor, create L14_TestDigitalChannel, add to list
        //   DigitalChannel touch = hardwareMap.get(DigitalChannel.class, "touch_sensor");
        //   touch.setMode(DigitalChannel.Mode.INPUT);
        //   list.add(new L14_TestDigitalChannel("Touch Sensor", touch));

        return list;
    }

    @Override
    public void init() {
        // TODO 2: Call buildTests() and store result in 'tests'
        //   tests = buildTests();

        telemetry.addLine("L14 TestWiring ready.");
        telemetry.addLine("D-pad UP/DOWN: cycle tests. Hold A: run test.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 3: Add D-pad navigation (same pattern as book listing 14.8)
        //
        //   if (gamepad1.dpad_up && !wasUp) {
        //       testNum--;
        //       if (testNum < 0) testNum = tests.size() - 1;
        //   }
        //   wasUp = gamepad1.dpad_up;
        //
        //   if (gamepad1.dpad_down && !wasDown) {
        //       testNum++;
        //       if (testNum >= tests.size()) testNum = 0;
        //   }
        //   wasDown = gamepad1.dpad_down;


        // TODO 4: Show instructions and current test name
        //   telemetry.addLine("UP/DOWN: cycle | Hold A: run test");
        //   L14_TestItem currTest = tests.get(testNum);
        //   telemetry.addData("Test", currTest.getDescription());


        // TODO 5: Run the test when A is held, passing gamepad1.a as the 'on' value
        //   currTest.run(gamepad1.a, telemetry);
        //
        // Notice: this code doesn't care if currTest is a TestMotor or
        // TestDigitalChannel — polymorphism calls the right run() automatically!
    }
}
