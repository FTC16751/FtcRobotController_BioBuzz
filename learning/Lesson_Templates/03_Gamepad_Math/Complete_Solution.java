package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * LESSON 4: GAMEPAD CONTROL - COMPLETE SOLUTION
 * 
 * This program demonstrates gamepad control of motors and servos.
 * 
 * Controls:
 *   A Button: Motor at half power and servo to position 0.0 (closed)
 *   B Button: Servo to position 1.0 (open)
 *
 * This follows the book's MotorGamepadOpMode example where a button controls motor power and telemetry output.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 4: Gamepad Control (Solution)", group = "Learning")
public class Lesson4_GamepadControl_Solution extends LinearOpMode {

    // Declare a DcMotor and Servo
    private DcMotor motorTest;
    private Servo servoTest;

    @Override
    public void runOpMode() {
        
        // Initialize motor and servo
        motorTest = hardwareMap.get(DcMotor.class, "motorTest");
        servoTest = hardwareMap.get(Servo.class, "servoTest");

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.addData("Controls", "");
        telemetry.addData("  A Button: Motor half speed and servo closed", "");
        telemetry.addData("  B Button: Servo open", "");
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // Use the A button to run the motor at half speed, otherwise stop
            if (gamepad1.a) {
                motorTest.setPower(0.5);
            } else {
                motorTest.setPower(0.0);
            }

            // Use A button to move servo to 0.0 (closed)
            // Use B button to move servo to 1.0 (open)
            if (gamepad1.a) {
                servoTest.setPosition(0.0);
            } else if (gamepad1.b) {
                servoTest.setPosition(1.0);
            }

            // Display gamepad input and device states on telemetry
            telemetry.addData("A Button", gamepad1.a);
            telemetry.addData("B Button", gamepad1.b);
            telemetry.addData("Motor Power", motorTest.getPower());
            telemetry.addData("Servo Position", servoTest.getPosition());
            telemetry.update();

            // Small delay to allow gamepad updates to be processed
            sleep(50);
        }

        // Stop motor when program ends
        motorTest.setPower(0.0);
    }
}
