// ============================================================
//  LESSON 14 — Complete Solution: TestMotor (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class L14_TestMotor_Solution extends L14_TestItem_Solution {

    private final double  speed;
    private final DcMotor motor;

    public L14_TestMotor_Solution(String description, double speed, DcMotor motor) {
        super(description);
        this.speed = speed;
        this.motor = motor;
    }

    @Override
    public void run(boolean on, Telemetry telemetry) {
        motor.setPower(on ? speed : 0.0);
        telemetry.addData("Encoder", motor.getCurrentPosition());
    }
}
