package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * LESSON 07: MOTORS
 * 
 * This is a template program for learning motor control.
 * Students will fill in the TODOs to make the motor work.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "L07 Motors")
public class L07_Motors extends LinearOpMode {

    // TODO 1: Declare a DcMotor variable called motor
    // Hint: private DcMotor motor;
    

    @Override
    public void runOpMode() {
        
        // TODO 2: Initialize the motor using hardwareMap
        // The name "motor" must match your hardware configuration
        // Hint: motor = hardwareMap.get(DcMotor.class, "motor");
        

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // TODO 3: Set the motor to full power forward
            // Power range is -1.0 to +1.0
            // Hint: motor.setPower(1.0);
            

            // Wait 2 seconds
            sleep(2000);

            // TODO 4: Set the motor to full power reverse
            // Hint: motor.setPower(-1.0);
            

            // Wait 2 seconds
            sleep(2000);

            // TODO 5: Stop the motor
            // Hint: motor.setPower(0.0);
            

            // Wait 1 second
            sleep(1000);

            // Display the current motor power on the driver station
            telemetry.addData("Motor Power", motor.getPower());
            telemetry.update();
        }
    }
}
