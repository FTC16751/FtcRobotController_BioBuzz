package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * LESSON 08: SERVOS
 * 
 * This is a template program for learning servo control.
 * Students will fill in the TODOs to make the servo work.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "L08 Servos")
public class L08_Servos extends LinearOpMode {

    // TODO 1: Declare a Servo variable called servo
    // Hint: private Servo servo;
    

    @Override
    public void runOpMode() {
        
        // TODO 2: Initialize the servo using hardwareMap
        // The name "servo" must match your hardware configuration
        // Hint: servo = hardwareMap.get(Servo.class, "servo");
        

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // TODO 3: Move servo to position 0.0 (0 degrees)
            // Hint: servo.setPosition(0.0);
            

            telemetry.addData("Action", "Servo moving to 0° (position 0.0)");
            telemetry.update();

            // Wait 1.5 seconds for servo to reach position
            sleep(1500);

            // TODO 4: Move servo to position 0.5 (90 degrees - middle)
            // Hint: servo.setPosition(0.5);
            

            telemetry.addData("Action", "Servo moving to 90° (position 0.5)");
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // TODO 5: Move servo to position 1.0 (180 degrees)
            // Hint: servo.setPosition(1.0);
            

            telemetry.addData("Action", "Servo moving to 180° (position 1.0)");
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // TODO 6: Read current servo position with getPosition()
            // and display it on telemetry
            // Hint: double position = servo.getPosition();
            //       telemetry.addData("Current Position", position);
            
        }
    }
}
