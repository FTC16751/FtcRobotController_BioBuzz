package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * LESSON 3: SENSOR READING - COMPLETE SOLUTION
 * 
 * This program reads and displays a distance sensor value.
 * Move your hand closer/farther to see the value change.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 3: Sensor Reading (Solution)", group = "Learning")
public class Lesson3_SensorReading_Solution extends LinearOpMode {

    // Declare a DistanceSensor variable called distanceSensor
    private DistanceSensor distanceSensor;

    @Override
    public void runOpMode() {
        
        // Initialize the distance sensor using hardwareMap
        // The name "distanceSensor" must match your hardware configuration
        distanceSensor = hardwareMap.get(DistanceSensor.class, "distanceSensor");

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.addData("Sensor Name", distanceSensor.getDeviceName());
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // Read the distance sensor value in centimeters
            double distance = distanceSensor.getDistance(DistanceUnit.CM);

            // Display the distance on telemetry
            telemetry.addData("Distance (cm)", distance);
            telemetry.addData("Distance (inches)", distance / 2.54);
            telemetry.update();

            // Small delay to avoid overwhelming the telemetry
            sleep(50);
        }
    }
}
