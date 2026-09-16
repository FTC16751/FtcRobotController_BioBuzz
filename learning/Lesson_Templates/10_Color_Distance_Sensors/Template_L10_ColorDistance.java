// ============================================================
//  LESSON 10 — Color & Distance Sensors
//  Book: Chapter 10  |  Hardware: REV Color/Range Sensor
// ============================================================
//  Config: I2C Bus 1, Port 0, name "sensor_color_distance"
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "L10 Color Distance")
public class L10_ColorDistance extends OpMode {

    // TODO 1: Declare a private ColorSensor field named 'colorSensor'
    //   private ColorSensor colorSensor;


    // TODO 2: Declare a private DistanceSensor field named 'distanceSensor'
    //   private DistanceSensor distanceSensor;


    @Override
    public void init() {
        // TODO 3: Get the ColorSensor from hardware map (name: "sensor_color_distance")
        //   colorSensor = hardwareMap.get(ColorSensor.class, "sensor_color_distance");


        // TODO 4: Get the DistanceSensor from hardware map — SAME name as above!
        //   distanceSensor = hardwareMap.get(DistanceSensor.class, "sensor_color_distance");


        telemetry.addLine("L10: Point sensor at different colors and surfaces.");
        telemetry.update();
    }

    @Override
    public void loop() {

        // ── COLOR ──────────────────────────────────────────────────────────────

        // TODO 5: Read red, green, and blue values (each 0–255)
        //   int red   = colorSensor.red();
        //   int green = colorSensor.green();
        //   int blue  = colorSensor.blue();
        //   telemetry.addData("Red",   red);
        //   telemetry.addData("Green", green);
        //   telemetry.addData("Blue",  blue);


        // TODO 6: Determine which color is dominant and display a label.
        //         Use if/else if to compare red, green, and blue.
        //         Display "Dominant Color" → "RED", "GREEN", or "BLUE"
        //
        //   if (red > green && red > blue) {
        //       telemetry.addData("Dominant Color", "RED");
        //   } else if (...) { ... }


        telemetry.addLine("---");

        // ── DISTANCE ───────────────────────────────────────────────────────────

        // TODO 7: Read distance in centimeters and inches
        //   double distCm   = distanceSensor.getDistance(DistanceUnit.CM);
        //   double distInch = distanceSensor.getDistance(DistanceUnit.INCH);
        //   telemetry.addData("Distance (cm)", distCm);
        //   telemetry.addData("Distance (in)", distInch);


        // TODO 8: Display a warning when something is too close (< 10 cm)
        //   if (distCm < 10.0) {
        //       telemetry.addLine("WARNING: Object too close!");
        //   }

    }
}
