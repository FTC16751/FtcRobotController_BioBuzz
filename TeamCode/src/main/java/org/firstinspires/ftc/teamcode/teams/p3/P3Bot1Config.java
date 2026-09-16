package org.firstinspires.ftc.teamcode.teams.p3;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.common.RobotConfig;

/**
 * WHAT THE P3 BOT 1 CHASSIS IS.
 *
 * Edit this file when the robot is rewired, a device is renamed in the Control Hub config, the
 * Pinpoint is moved, or the hub is remounted. For speeds, feed times, launcher presets and
 * waypoints, edit P3RobotConstants instead.
 *
 * Values here are exactly what RobotConfig.createDefaultP3Config() held before 2026-09-06.
 * Used by P3_Robot_Bot1.
 */
public final class P3Bot1Config {

    private P3Bot1Config() {}

    public static RobotConfig create() {
        return new RobotConfig(
                new RobotConfig.DrivetrainConfig(
                        DcMotorEx.Direction.REVERSE,   // left front
                        DcMotorEx.Direction.FORWARD,   // right front
                        DcMotorEx.Direction.REVERSE,   // left rear
                        DcMotorEx.Direction.FORWARD    // right rear
                ),
                new RobotConfig.OdometryConfig(
                        -38.0, 165.0,                  // Pinpoint pod offsets, mm (X pod, Y pod)
                        GoBildaPinpointDriver.EncoderDirection.REVERSED,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD
                ),
                new RobotConfig.ImuConfig(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                ),
                new RobotConfig.PointToPointTuning()
                        .xyToleranceMm(17.0).yawToleranceRad(0.055)
                        .xyGains(0.002, 0.000002, 0.00003).xyAccel(10.0)
                        .yawGains(5.0, 0.0, 0.03).yawAccel(10.0),
                // Pedro Pathing: null means this robot does not use it, so DriveUtil2026b does not build a
                // Follower for it (2026-09-07). The block that was here was a copy of Bot 3's estimates;
                // run the Tuning OpMode on this chassis before adding a PedroPathingConfig.
                null
        )
        .named("P3 Bot 1")
        .withHardware(new RobotConfig.HardwareNames()
                // drive motors, imu, pinpoint and limelight use the default names
                .led("light"))
        // Calibration defaults are the values that were shared by every robot before 2026-09-06.
        // Measure this chassis and set its own: rightRearPowerScale, strafeScale, turnCircumferenceIn.
        .withCalibration(new RobotConfig.Calibration());
    }
}
