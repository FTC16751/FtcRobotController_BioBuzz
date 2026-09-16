// ============================================================
//  LESSON 20 — Making Robots Drive  (FILE 1 of 2: Mechanism)
//  Book: Chapter 20.1  |  Hardware: 2 motors
// ============================================================
//  Config: motors named "left_motor" and "right_motor"
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class L20_TwoMotorDrive {

    // TODO 1: Declare private DcMotor fields: leftMotor and rightMotor
    //   private DcMotor leftMotor;
    //   private DcMotor rightMotor;


    public void init(HardwareMap hardwareMap) {
        // TODO 2: Get both motors from hardwareMap
        //   leftMotor  = hardwareMap.get(DcMotor.class, "left_motor");
        //   rightMotor = hardwareMap.get(DcMotor.class, "right_motor");


        // TODO 3: Set both motors to RUN_USING_ENCODER mode
        //   leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //   rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        // TODO 4: REVERSE the left motor so positive power = forward on both sides.
        //         (Left motor is mounted facing opposite to the right motor.)
        //   leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);

    }


    // TODO 5: Implement setPowers(double leftPower, double rightPower)
    //
    //         IMPORTANT: Normalize before setting power!
    //         If either value is > 1.0 or < -1.0 in absolute terms,
    //         scale BOTH down proportionally so the relative ratio stays the same.
    //
    //   public void setPowers(double leftPower, double rightPower) {
    //       double largest = 1.0;
    //       largest = Math.max(largest, Math.abs(leftPower));
    //       largest = Math.max(largest, Math.abs(rightPower));
    //
    //       leftMotor.setPower(leftPower   / largest);
    //       rightMotor.setPower(rightPower / largest);
    //   }

}
