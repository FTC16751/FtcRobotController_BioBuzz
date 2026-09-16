package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * LESSON 2: SERVO CONTROL - COMPLETE SOLUTION
 * 
 * This is the complete solution showing servo control.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 2: Servo Control (Solution)", group = "Learning")
public class Lesson2_ServoControl_Solution extends LinearOpMode {

    // Declare a Servo variable called servoTest
    private Servo servoTest;

    @Override
    public void runOpMode() {
        
        // Initialize the servo using hardwareMap
        // The name "servoTest" must match your hardware configuration
        servoTest = hardwareMap.get(Servo.class, "servoTest");

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.addData("Servo Name", servoTest.getDeviceName());
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // Move servo to position 0.0 (0 degrees)
            servoTest.setPosition(0.0);
            telemetry.addData("Action", "Servo moving to 0° (position 0.0)");
            telemetry.addData("Target Position", 0.0);
            telemetry.update();

            // Wait 1.5 seconds for servo to reach position
            sleep(1500);

            // Move servo to position 0.5 (90 degrees - middle)
            servoTest.setPosition(0.5);
            telemetry.addData("Action", "Servo moving to 90° (position 0.5)");
            telemetry.addData("Target Position", 0.5);
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // Move servo to position 1.0 (180 degrees)
            servoTest.setPosition(1.0);
            telemetry.addData("Action", "Servo moving to 180° (position 1.0)");
            telemetry.addData("Target Position", 1.0);
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // Read current servo position with getPosition()
            // and display it on telemetry
            double position = servoTest.getPosition();
            telemetry.addData("Current Position", position);
            telemetry.addData("Position (degrees)", position * 180);
            telemetry.update();
        }
    }
}
