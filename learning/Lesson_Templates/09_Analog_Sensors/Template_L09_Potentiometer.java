// ============================================================
//  LESSON 09 — Analog Sensors (Potentiometer)
//  Book: Chapter 9  |  Hardware: REV Potentiometer
// ============================================================
//  Config: Analog Input Port 0, named "pot"
//
//  This file combines the Mechanism and OpMode into one file
//  to keep things simpler. For a real robot, they'd be separate.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "L09 Potentiometer")
public class L09_Potentiometer extends OpMode {

    // TODO 1: Declare a private AnalogInput field named 'pot'
    //
    //   private AnalogInput pot;


    @Override
    public void init() {
        // TODO 2: Get the potentiometer from the hardware map
        //
        //   pot = hardwareMap.get(AnalogInput.class, "pot");


        telemetry.addLine("L09: Rotate the potentiometer knob.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 3: Read the current voltage
        //
        //   double voltage = pot.getVoltage();
        //   telemetry.addData("Voltage (V)", voltage);


        // TODO 4: Read the maximum possible voltage
        //
        //   double maxVoltage = pot.getMaxVoltage();
        //   telemetry.addData("Max Voltage", maxVoltage);


        // TODO 5: Convert voltage to angle (0–270 degrees) using Range.scale()
        //         Range.scale(input, inMin, inMax, outMin, outMax)
        //
        //   double angle = Range.scale(voltage, 0, maxVoltage, 0, 270);
        //   telemetry.addData("Angle (degrees)", angle);


        // TODO 6: Convert angle to a 0.0–1.0 range (useful for controlling a servo)
        //         Hint: angle / 270.0
        //
        //   double normalized = angle / 270.0;
        //   telemetry.addData("Normalized (0-1)", normalized);


        // TODO 7 (CHALLENGE): Use the potentiometer to control a servo position.
        //         You'll need to add a Servo field and init it in init().
        //         Set the servo position to the normalized pot value each loop.
    }
}
