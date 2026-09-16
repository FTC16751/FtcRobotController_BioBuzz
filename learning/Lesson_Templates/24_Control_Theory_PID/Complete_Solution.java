// ============================================================
//  LESSON 24 — Complete Solution (Coach Reference)
//  Book: Chapter 24
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "L24 PID SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L24_PIDControl_Solution extends OpMode {

    DcMotor     motor;
    ElapsedTime timer = new ElapsedTime();

    static final int    FULLY_IN     = 0;
    static final int    FULLY_OUT    = 1100;
    static final double kP           = 0.001;
    static final double kI           = 0.0001;
    static final double kD           = 0.0005;
    static final double kF           = 0.0;
    static final int    DEADBAND     = 20;
    static final double MAX_INTEGRAL = 0.3;

    int    desiredPosition = 0;
    double lastError       = 0;
    double sumErrors       = 0;
    int    lastDesired     = 0;

    @Override
    public void init() {
        motor = hardwareMap.get(DcMotor.class, "motor");
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        timer.reset();
        telemetry.addLine("L24 PID ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        if (gamepad1.dpad_up) {
            desiredPosition = Math.min(desiredPosition + 100, FULLY_OUT);
        } else if (gamepad1.dpad_down) {
            desiredPosition = Math.max(desiredPosition - 100, FULLY_IN);
        }

        int    actualPosition = motor.getCurrentPosition();
        int    error          = desiredPosition - actualPosition;
        double deltaTime      = timer.seconds();
        double derivative     = (error - lastError) / deltaTime;
        sumErrors            += error * deltaTime;

        if (desiredPosition != lastDesired) {
            sumErrors   = 0;
            lastDesired = desiredPosition;
        } else if (Math.abs(sumErrors) > MAX_INTEGRAL) {
            sumErrors = Math.signum(sumErrors) * MAX_INTEGRAL;
        }

        double power = (kP * error) + (kI * sumErrors) + (kD * derivative) + kF;
        power = Math.max(-1.0, Math.min(1.0, power));
        motor.setPower(power);

        lastError = error;
        timer.reset();

        telemetry.addData("Desired",    desiredPosition);
        telemetry.addData("Actual",     actualPosition);
        telemetry.addData("Error",      error);
        telemetry.addData("Power",      power);
        telemetry.addData("Sum Errors", sumErrors);
        telemetry.addData("Derivative", derivative);
    }
}
