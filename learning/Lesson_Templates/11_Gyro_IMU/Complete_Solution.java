// ============================================================
//  LESSON 11 — Complete Solution (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "L11 IMU SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L11_IMU_Solution extends OpMode {

    private IMU imu;

    @Override
    public void init() {
        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.UP,
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        );
        imu.initialize(new IMU.Parameters(orientation));

        telemetry.addLine("L11 ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        double headingDegrees = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double headingRadians = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        telemetry.addData("Heading (degrees)", headingDegrees);
        telemetry.addData("Heading (radians)", headingRadians);

        String compass;
        if (headingDegrees >= -45 && headingDegrees < 45)        { compass = "NORTH"; }
        else if (headingDegrees >= 45 && headingDegrees < 135)   { compass = "EAST";  }
        else if (headingDegrees >= -135 && headingDegrees < -45) { compass = "WEST";  }
        else                                                      { compass = "SOUTH"; }
        telemetry.addData("Compass", compass);

        double pitch = imu.getRobotYawPitchRollAngles().getPitch(AngleUnit.DEGREES);
        double roll  = imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.DEGREES);
        telemetry.addData("Pitch", pitch);
        telemetry.addData("Roll",  roll);
    }
}
