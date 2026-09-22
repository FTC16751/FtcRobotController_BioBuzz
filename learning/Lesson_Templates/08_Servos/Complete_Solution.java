package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * LESSON 08: SERVOS - COMPLETE SOLUTION
 * 
 * This is the complete solution showing servo control.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "L08 Servos (Solution)")
public class L08_Servos_Solution extends LinearOpMode {

    // Declare a Servo variable called servo
    private Servo servo;

    @Override
    public void runOpMode() {
        
        // Initialize the servo using hardwareMap
        // The name "servo" must match your hardware configuration
        servo = hardwareMap.get(Servo.class, "servo");

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.addData("Servo Name", servo.getDeviceName());
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // Move servo to position 0.0 (0 degrees)
            servo.setPosition(0.0);
            telemetry.addData("Action", "Servo moving to 0° (position 0.0)");
            telemetry.addData("Target Position", 0.0);
            telemetry.update();

            // Wait 1.5 seconds for servo to reach position
            sleep(1500);

            // Move servo to position 0.5 (90 degrees - middle)
            servo.setPosition(0.5);
            telemetry.addData("Action", "Servo moving to 90° (position 0.5)");
            telemetry.addData("Target Position", 0.5);
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // Move servo to position 1.0 (180 degrees)
            servo.setPosition(1.0);
            telemetry.addData("Action", "Servo moving to 180° (position 1.0)");
            telemetry.addData("Target Position", 1.0);
            telemetry.update();

            // Wait 1.5 seconds
            sleep(1500);

            // Read current servo position with getPosition()
            // and display it on telemetry
            double position = servo.getPosition();
            telemetry.addData("Current Position", position);
            telemetry.addData("Position (degrees)", position * 180);
            telemetry.update();
        }
    }
}
