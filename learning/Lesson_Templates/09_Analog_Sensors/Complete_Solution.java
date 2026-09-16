// ============================================================
//  LESSON 09 — Complete Solution (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "L09 Potentiometer SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L09_Potentiometer_Solution extends OpMode {

    private AnalogInput pot;

    @Override
    public void init() {
        pot = hardwareMap.get(AnalogInput.class, "pot");
        telemetry.addLine("L09 ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        double voltage    = pot.getVoltage();
        double maxVoltage = pot.getMaxVoltage();
        double angle      = Range.scale(voltage, 0, maxVoltage, 0, 270);
        double normalized = angle / 270.0;

        telemetry.addData("Voltage (V)",    voltage);
        telemetry.addData("Max Voltage",    maxVoltage);
        telemetry.addData("Angle (degrees)", angle);
        telemetry.addData("Normalized (0-1)", normalized);
    }
}
