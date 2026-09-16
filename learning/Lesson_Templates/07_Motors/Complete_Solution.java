package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * LESSON 1: MOTOR BASICS - COMPLETE SOLUTION
 * 
 * This is the complete solution showing motor control.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 1: Motor Basics (Solution)", group = "Learning")
public class Lesson1_MotorBasics_Solution extends LinearOpMode {

    // Declare a DcMotor variable called motorTest
    private DcMotor motorTest;

    @Override
    public void runOpMode() {
        
        // Initialize the motor using hardwareMap
        // The name "motorTest" must match your hardware configuration
        motorTest = hardwareMap.get(DcMotor.class, "motorTest");

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.addData("Motor Name", motorTest.getDeviceName());
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // Set the motor to full power forward
            motorTest.setPower(1.0);
            telemetry.addData("Action", "Motor running FORWARD at full power");
            telemetry.addData("Motor Power", motorTest.getPower());
            telemetry.update();

            // Wait 2 seconds
            sleep(2000);

            // Set the motor to full power reverse
            motorTest.setPower(-1.0);
            telemetry.addData("Action", "Motor running REVERSE at full power");
            telemetry.addData("Motor Power", motorTest.getPower());
            telemetry.update();

            // Wait 2 seconds
            sleep(2000);

            // Stop the motor
            motorTest.setPower(0.0);
            telemetry.addData("Action", "Motor STOPPED");
            telemetry.addData("Motor Power", motorTest.getPower());
            telemetry.update();

            // Wait 1 second before repeating
            sleep(1000);
        }

        // Stop the motor when program ends
        motorTest.setPower(0.0);
        telemetry.addData("Status", "Program ended. Motor stopped.");
        telemetry.update();
    }
}
