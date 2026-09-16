// ============================================================
//  LESSON 10 — Complete Solution (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "L10 Color Distance SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L10_ColorDistance_Solution extends OpMode {

    private ColorSensor    colorSensor;
    private DistanceSensor distanceSensor;

    @Override
    public void init() {
        colorSensor    = hardwareMap.get(ColorSensor.class,    "sensor_color_distance");
        distanceSensor = hardwareMap.get(DistanceSensor.class, "sensor_color_distance");
        telemetry.addLine("L10 ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        int red   = colorSensor.red();
        int green = colorSensor.green();
        int blue  = colorSensor.blue();

        telemetry.addData("Red",   red);
        telemetry.addData("Green", green);
        telemetry.addData("Blue",  blue);

        if (red > green && red > blue) {
            telemetry.addData("Dominant Color", "RED");
        } else if (green > red && green > blue) {
            telemetry.addData("Dominant Color", "GREEN");
        } else {
            telemetry.addData("Dominant Color", "BLUE");
        }

        telemetry.addLine("---");

        double distCm   = distanceSensor.getDistance(DistanceUnit.CM);
        double distInch = distanceSensor.getDistance(DistanceUnit.INCH);
        telemetry.addData("Distance (cm)", distCm);
        telemetry.addData("Distance (in)", distInch);

        if (distCm < 10.0) {
            telemetry.addLine("WARNING: Object too close!");
        }
    }
}
