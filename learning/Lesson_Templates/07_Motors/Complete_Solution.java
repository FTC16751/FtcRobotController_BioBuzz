package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * LESSON 07: MOTORS - COMPLETE SOLUTION
 * 
 * This is the complete solution showing motor control.
 * 
 * @author FTC Learning Team
 */
@TeleOp(name = "L07 Motors (Solution)")
public class L07_Motors_Solution extends LinearOpMode {

    // Declare a DcMotor variable called motor
    private DcMotor motor;

    @Override
    public void runOpMode() {
        
        // Initialize the motor using hardwareMap
        // The name "motor" must match your hardware configuration
        motor = hardwareMap.get(DcMotor.class, "motor");

        // Display initialization message
        telemetry.addData("Status", "Initialized. Press Play to start.");
        telemetry.addData("Motor Name", motor.getDeviceName());
        telemetry.update();

        // Wait for the play button to be pressed
        waitForStart();

        // Main loop - runs until STOP is pressed
        while (opModeIsActive()) {

            // Set the motor to full power forward
            motor.setPower(1.0);
            telemetry.addData("Action", "Motor running FORWARD at full power");
            telemetry.addData("Motor Power", motor.getPower());
            telemetry.update();

            // Wait 2 seconds
            sleep(2000);

            // Set the motor to full power reverse
            motor.setPower(-1.0);
            telemetry.addData("Action", "Motor running REVERSE at full power");
            telemetry.addData("Motor Power", motor.getPower());
            telemetry.update();

            // Wait 2 seconds
            sleep(2000);

            // Stop the motor
            motor.setPower(0.0);
            telemetry.addData("Action", "Motor STOPPED");
            telemetry.addData("Motor Power", motor.getPower());
            telemetry.update();

            // Wait 1 second before repeating
            sleep(1000);
        }

        // Stop the motor when program ends
        motor.setPower(0.0);
        telemetry.addData("Status", "Program ended. Motor stopped.");
        telemetry.update();
    }
}
