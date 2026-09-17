package org.firstinspires.ftc.teamcode.teams.starterbot2027;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.common.RobotConfig;

/**
 * WHAT the GearGirls goBILDA BIOBUZZ StarterBot IS (mecanum chassis, intake, launcher, windmill feeder),
 * as goBILDA's example code configures it. Device names are goBILDA's, so a hub configured from
 * their build guide works unchanged. Edit this file when the robot is rewired, and this file only:
 * the twin StarterBot has its own copy, so the two can grow apart (one gets a Pinpoint or a
 * Limelight first) without touching each other. How the robot operates is in StarterBot2027Constants.
 *
 * Not on this robot yet (both null below): a Pinpoint and a Limelight. When one is added, fill in
 * its name and, for the Pinpoint, section 4; DriveUtil2026b and VisionUtil switch on by themselves.
 */
public final class GGStarterBotConfig {

    private GGStarterBotConfig() {}

    // 7. Game mechanisms: the intake and launcher device names are goBILDA's and identical on both
    //    StarterBots, so they live once in StarterBot2027Constants.Devices. If THIS robot is ever
    //    rewired differently, move them here and give it its own robot class.

    public static RobotConfig create() {
        return new RobotConfig(
                // 2. Motor directions, goBILDA's: left side reversed. Verify on the first drive:
                //    left stick forward MUST drive forward; reverse any wheel that spins backward.
                new RobotConfig.DrivetrainConfig(
                        DcMotorEx.Direction.REVERSE,   // left front
                        DcMotorEx.Direction.FORWARD,   // right front
                        DcMotorEx.Direction.REVERSE,   // left rear
                        DcMotorEx.Direction.FORWARD    // right rear
                ),
                // 4. Pinpoint: NONE on this robot yet (hardware.pinpoint is null below, so these are
                //    ignored). When one is fitted: pod offsets from the robot centre in mm (X pod
                //    left of centre positive, Y pod forward of centre positive) and pod directions.
                new RobotConfig.OdometryConfig(
                        0.0, 0.0,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD
                ),
                // 3. Control Hub mounting: which way the REV logo faces and which way the USB ports
                //    face. goBILDA's example never reads the IMU, so this is UNVERIFIED; it only
                //    matters for the turn commands and field-centric driving. Check with the
                //    Encoder Move Check: a commanded 90 degree turn should read about 90 in telemetry.
                new RobotConfig.ImuConfig(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                ),
                // 6a. Point-to-point PID: unused until there is a Pinpoint; the shared defaults.
                new RobotConfig.PointToPointTuning(),
                // 6c. Pedro Pathing: none (needs a Pinpoint first).
                null
        )
        .named("GG Starterbot")
        // 1. Device names, exactly as in the Control Hub configuration (goBILDA's names).
        .withHardware(new RobotConfig.HardwareNames()
                .driveMotors("left_front_drive", "right_front_drive", "left_back_drive", "right_back_drive")
                .imu("imu")
                .pinpoint(null)             // no Pinpoint yet
                .limelight(null)            // no Limelight yet
                .led(null))                 // no status LED
        // 5. Calibration for the encoder moves (driveForward and friends). Starting values for
        //    the goBILDA 312 rpm motor (537.7 ticks/rev) on a 96 mm mecanum wheel; measure each
        //    one with the Encoder Move Check before trusting an auto to them.
        .withCalibration(new RobotConfig.Calibration()
                .rightRearPowerScale(1.0)
                .strafeScale(1.1)
                .turnCircumferenceIn(27.5)
                .encoderCountsPerInch(RobotConfig.Calibration.countsPerInch(537.7, 1.0, 96)));
    }
}
