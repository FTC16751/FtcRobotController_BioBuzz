// ============================================================
//  LESSON 20 — Making Robots Drive  (Mecanum Mechanism)
//  Book: Chapter 20.2  |  Hardware: 4 mecanum motors
// ============================================================
//  Config: "front_left_motor", "front_right_motor",
//          "back_left_motor",  "back_right_motor"
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class L20_MecanumDrive {

    private DcMotor frontLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backLeftMotor;
    private DcMotor backRightMotor;

    public void init(HardwareMap hardwareMap) {
        frontLeftMotor  = hardwareMap.dcMotor.get("front_left_motor");
        frontRightMotor = hardwareMap.dcMotor.get("front_right_motor");
        backLeftMotor   = hardwareMap.dcMotor.get("back_left_motor");
        backRightMotor  = hardwareMap.dcMotor.get("back_right_motor");

        // Left-side motors are reversed (mounted facing opposite direction)
        backLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);

        frontLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // Normalize helper — keeps all powers in [-1, 1] while preserving ratios
    private void setPowers(double fl, double fr, double bl, double br) {
        double maxSpeed = 1.0;
        maxSpeed = Math.max(maxSpeed, Math.abs(fl));
        maxSpeed = Math.max(maxSpeed, Math.abs(fr));
        maxSpeed = Math.max(maxSpeed, Math.abs(bl));
        maxSpeed = Math.max(maxSpeed, Math.abs(br));

        frontLeftMotor.setPower(fl  / maxSpeed);
        frontRightMotor.setPower(fr / maxSpeed);
        backLeftMotor.setPower(bl   / maxSpeed);
        backRightMotor.setPower(br  / maxSpeed);
    }

    // TODO: Implement the drive() method.
    //
    //       The 4 mecanum motor power formulas are:
    //         frontLeft  = forward + right + rotate
    //         frontRight = forward - right - rotate
    //         backLeft   = forward - right + rotate
    //         backRight  = forward + right - rotate
    //
    //   public void drive(double forward, double right, double rotate) {
    //       double frontLeftPower  = forward + right + rotate;
    //       double frontRightPower = forward - right - rotate;
    //       double backLeftPower   = forward - right + rotate;
    //       double backRightPower  = forward + right - rotate;
    //       setPowers(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
    //   }
    public void drive(double forward, double right, double rotate) {
        // TODO: fill in the formulas above
    }
}
