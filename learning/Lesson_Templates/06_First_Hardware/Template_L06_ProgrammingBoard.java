// ============================================================
//  LESSON 06 — First Hardware  (FILE 1 of 2: Mechanism)
//  Book: Chapter 6
// ============================================================
//
//  This is the MECHANISM class. It handles all hardware details.
//  The OpMode should never need to know about DigitalChannel directly.
//
//  Config file requirement: a Digital Channel named "touch_sensor"
//  Save as: L06_ProgrammingBoard.java
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class L06_ProgrammingBoard {

    // TODO 1: Declare a private DigitalChannel field named 'touchSensor'
    //
    //   private DigitalChannel touchSensor;


    // ── INIT ───────────────────────────────────────────────────────────────────
    // Called from the OpMode's init() method with hardwareMap passed in.
    public void init(HardwareMap hwMap) {

        // TODO 2: Get the touch sensor from the hardware map.
        //         The name in quotes MUST match your configuration file.
        //
        //   touchSensor = hwMap.get(DigitalChannel.class, "touch_sensor");


        // TODO 3: Set the channel mode to INPUT (we are reading from it).
        //
        //   touchSensor.setMode(DigitalChannel.Mode.INPUT);

    }


    // ── SENSOR METHODS ─────────────────────────────────────────────────────────

    // TODO 4: Write a public method named isTouchSensorPressed() that returns boolean.
    //
    //         IMPORTANT: touchSensor.getState() returns FALSE when PRESSED
    //         (inverted logic). Your method should return TRUE when pressed.
    //         Use the ! operator to flip the value.
    //
    //   public boolean isTouchSensorPressed() {
    //       return !touchSensor.getState();
    //   }


    // TODO 5 (BONUS): Write a companion method isTouchSensorReleased()
    //                 that returns true when the sensor is NOT pressed.
    //
    //   public boolean isTouchSensorReleased() {
    //       return touchSensor.getState();
    //   }

}
