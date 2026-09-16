// ============================================================
//  LESSON 20 — Complete Solution: Mecanum Drive (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class L20_MecanumDrive_Solution {

    private DcMotor frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor;

    public void init(HardwareMap hardwareMap) {
        frontLeftMotor  = hardwareMap.dcMotor.get("front_left_motor");
        frontRightMotor = hardwareMap.dcMotor.get("front_right_motor");
        backLeftMotor   = hardwareMap.dcMotor.get("back_left_motor");
        backRightMotor  = hardwareMap.dcMotor.get("back_right_motor");

        backLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);

        for (DcMotor m : new DcMotor[]{frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor}) {
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    private void setPowers(double fl, double fr, double bl, double br) {
        double max = 1.0;
        max = Math.max(max, Math.abs(fl));
        max = Math.max(max, Math.abs(fr));
        max = Math.max(max, Math.abs(bl));
        max = Math.max(max, Math.abs(br));
        frontLeftMotor.setPower(fl  / max);
        frontRightMotor.setPower(fr / max);
        backLeftMotor.setPower(bl   / max);
        backRightMotor.setPower(br  / max);
    }

    public void drive(double forward, double right, double rotate) {
        double fl = forward + right + rotate;
        double fr = forward - right - rotate;
        double bl = forward - right + rotate;
        double br = forward + right - rotate;
        setPowers(fl, fr, bl, br);
    }
}
