package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * LESSON 4: GAMEPAD CONTROL
 * 
 * This is a template program for learning gamepad control.
 * Students will fill in the TODOs to control a motor and servo with gamepad.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "Lesson 4: Gamepad Control (Template)", group = "Learning")
public class Lesson4_GamepadControl_Template extends LinearOpMode {

    // TODO 1: Declare a DcMotor and Servo
    // Hint: private DcMotor motorTest;
    //       private Servo servoTest;
    

    @Override
    public void runOpMode() {
        
        // TODO 2: Initialize motor and servo
        // Hint: motorTest = hardwareMap.get(DcMotor.class, "motorTest");
        //       servoTest = hardwareMap.get(Servo.class, "servoTest");
        

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // TODO 3: Use the A button to run the motor at half speed
            // Hint: if (gamepad1.a) {
            //           motorTest.setPower(0.5);
            //       } else {
            //           motorTest.setPower(0.0);
            //       }
            

            // TODO 4: Use A button to move servo to 0.0 (closed)
            // Use B button to move servo to 1.0 (open)
            // Hint: if (gamepad1.a) {
            //           servoTest.setPosition(0.0);
            //       } else if (gamepad1.b) {
            //           servoTest.setPosition(1.0);
            //       }
            

            // TODO 5: Display gamepad input and device states on telemetry
            // Hint: telemetry.addData("A Button", gamepad1.a);
            //       telemetry.addData("B Button", gamepad1.b);
            //       telemetry.addData("Motor Power", motorTest.getPower());
            //       telemetry.addData("Servo Position", servoTest.getPosition());
            //       telemetry.update();
            

            // Small delay to allow gamepad updates to be processed
            sleep(50);
        }
    }
}
