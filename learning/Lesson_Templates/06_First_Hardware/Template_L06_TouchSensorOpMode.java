// ============================================================
//  LESSON 06 — First Hardware  (FILE 2 of 2: OpMode)
//  Book: Chapter 6
// ============================================================
//
//  PREREQUISITE: Complete Template_L06_ProgrammingBoard.java first!
//
//  Notice: this OpMode knows NOTHING about DigitalChannel.
//  All hardware details are hidden inside L06_ProgrammingBoard.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L06 Touch Sensor")
public class L06_TouchSensorOpMode extends OpMode {

    // TODO 1: Create an L06_ProgrammingBoard object named 'board'
    //
    //   L06_ProgrammingBoard board = new L06_ProgrammingBoard();


    // For press counting (edge detection)
    boolean wasTouchPressed = false;
    int pressCount = 0;
    double pressStartTime = 0.0;


    // ── INIT ───────────────────────────────────────────────────────────────────
    @Override
    public void init() {

        // TODO 2: Initialize the board by calling board.init(hardwareMap)
        //         hardwareMap is provided automatically by the OpMode base class.
        //
        //   board.init(hardwareMap);


        telemetry.addLine("L06: Touch sensor ready. Press and release the sensor.");
        telemetry.update();
    }


    // ── LOOP ───────────────────────────────────────────────────────────────────
    @Override
    public void loop() {

        // TODO 3: Read the sensor state using board.isTouchSensorPressed()
        //         Store the result in a boolean variable named 'isPressed'
        //
        //   boolean isPressed = board.isTouchSensorPressed();


        // TODO 4: Display the raw boolean state:
        //   telemetry.addData("Touch Pressed", isPressed);


        // TODO 5: Display a human-readable string instead of true/false.
        //         Use an if/else to show "PRESSED" or "not pressed"
        //
        //   if (isPressed) {
        //       telemetry.addData("Status", "PRESSED");
        //   } else {
        //       telemetry.addData("Status", "not pressed");
        //   }


        // TODO 6: Count the number of times the sensor is pressed.
        //         Use edge detection: only count when it transitions from not-pressed → pressed.
        //
        //   if (isPressed && !wasTouchPressed) {
        //       pressCount++;
        //   }
        //   wasTouchPressed = isPressed;
        //   telemetry.addData("Press count", pressCount);


        // TODO 7 (CHALLENGE): Display how long the sensor has been held down.
        //                     When the sensor first becomes pressed, record the time.
        //                     While it stays pressed, show the elapsed hold time.
        //
        //   if (isPressed && !wasTouchPressed) {
        //       pressStartTime = getRuntime();
        //   }
        //   if (isPressed) {
        //       telemetry.addData("Hold time (sec)", getRuntime() - pressStartTime);
        //   }

    }
}
