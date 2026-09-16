// ============================================================
//  LESSON 11 — Gyro / IMU
//  Book: Chapter 11  |  Hardware: Control Hub built-in IMU
// ============================================================
//  No config changes needed — IMU is already named "imu".
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "L11 IMU Gyro")
public class L11_IMU extends OpMode {

    // TODO 1: Declare a private IMU field named 'imu'
    //   private IMU imu;


    @Override
    public void init() {
        // TODO 2: Get the IMU from the hardware map
        //   imu = hardwareMap.get(IMU.class, "imu");


        // TODO 3: Create a RevHubOrientationOnRobot describing how your hub is mounted.
        //         For a hub flat on the robot with logo UP and USB facing FORWARD:
        //
        //   RevHubOrientationOnRobot orientation = new RevHubOrientationOnRobot(
        //       RevHubOrientationOnRobot.LogoFacingDirection.UP,
        //       RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        //   );


        // TODO 4: Initialize the IMU with the orientation
        //   imu.initialize(new IMU.Parameters(orientation));


        telemetry.addLine("L11: IMU initialized. Rotate the hub to see heading change.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 5: Read the heading in DEGREES
        //         (Yaw = rotation around the vertical axis)
        //
        //   double headingDegrees = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        //   telemetry.addData("Heading (degrees)", headingDegrees);


        // TODO 6: Read the heading in RADIANS
        //
        //   double headingRadians = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        //   telemetry.addData("Heading (radians)", headingRadians);


        // TODO 7: Display a compass label based on heading:
        //         -45 to 45    → "NORTH"
        //         45 to 135    → "EAST"
        //         135 to 180 or -180 to -135  → "SOUTH"
        //         -135 to -45  → "WEST"
        //
        //   String compass;
        //   if (headingDegrees >= -45 && headingDegrees < 45)       { compass = "NORTH"; }
        //   else if (headingDegrees >= 45 && headingDegrees < 135)  { compass = "EAST";  }
        //   else if (headingDegrees >= -135 && headingDegrees < -45){ compass = "WEST";  }
        //   else                                                     { compass = "SOUTH"; }
        //   telemetry.addData("Compass", compass);


        // TODO 8 (CHALLENGE): Display pitch and roll as well.
        //   double pitch = imu.getRobotYawPitchRollAngles().getPitch(AngleUnit.DEGREES);
        //   double roll  = imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.DEGREES);
    }
}
