package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * LESSON 2: SERVO CONTROL
 * 
 * This is a template program for learning servo control.
 * Students will fill in the TODOs to make the servo work.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 2: Servo Control (Template)", group = "Learning")
public class Lesson2_ServoControl_Template extends LinearOpMode {

    // TODO 1: Declare a Servo variable called servoTest
    // Hint: private Servo servoTest;
    

    @Override
    public void runOpMode() {
        
        // TODO 2: Initialize the servo using hardwareMap
        // The name "servoTest" must match your hardware configuration
        // Hint: servoTest = hardwareMap.get(Servo.class, "servoTest");
        

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // TODO 3: Move servo to position 0.0 (0 degrees)
            // Hint: servoTest.setPosition(0.0);
            

            telemetry.addData("Action", "Servo moving to 0° (position 0.0)");
            telemetry.update();

            // Wait 1.5 seconds for servo to reach position
            sleep(1500);

            // TODO 4: Move servo to position 0.5 (90 degrees - middle)
            // Hint: servoTest.setPosition(0.5);
            

            telemetry.addData("Action", "Servo moving to 90° (position 0.5)");
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // TODO 5: Move servo to position 1.0 (180 degrees)
            // Hint: servoTest.setPosition(1.0);
            

            telemetry.addData("Action", "Servo moving to 180° (position 1.0)");
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // TODO 6: Read current servo position with getPosition()
            // and display it on telemetry
            // Hint: double position = servoTest.getPosition();
            //       telemetry.addData("Current Position", position);
            
        }
    }
}
