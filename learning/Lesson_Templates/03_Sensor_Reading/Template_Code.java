package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * LESSON 3: SENSOR READING - DISTANCE SENSOR
 * 
 * This is a template program for learning sensor reading.
 * Students will fill in the TODOs to read a distance sensor.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 3: Sensor Reading (Template)", group = "Learning")
public class Lesson3_SensorReading_Template extends LinearOpMode {

    // TODO 1: Declare a DistanceSensor variable called distanceSensor
    // Hint: private DistanceSensor distanceSensor;
    

    @Override
    public void runOpMode() {
        
        // TODO 2: Initialize the distance sensor using hardwareMap
        // The name "distanceSensor" must match your hardware configuration
        // Hint: distanceSensor = hardwareMap.get(DistanceSensor.class, "distanceSensor");
        

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // TODO 3: Read the distance sensor value in centimeters
            // Hint: double distance = distanceSensor.getDistance(DistanceUnit.CM);
            

            // TODO 4: Display the distance on telemetry
            // Hint: telemetry.addData("Distance (cm)", distance);
            //       telemetry.update();
            

            // Small delay to avoid overwhelming the telemetry
            sleep(50);
        }
    }
}
