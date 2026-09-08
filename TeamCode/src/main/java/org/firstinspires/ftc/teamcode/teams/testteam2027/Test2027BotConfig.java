package org.firstinspires.ftc.teamcode.teams.testteam2027;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.common.TagApproach;

/**
 * WHAT THE test2027bot CHASSIS IS.
 *
 * This is the first file a new team fills in. Every value here is a physical fact about one
 * chassis: what its devices are called in the Control Hub configuration, which way the motors
 * spin, where the odometry pods sit, how the hub is mounted, and the numbers that make it drive
 * straight. Nothing in here changes when the game changes; that belongs in Test2027Constants.
 *
 * How to fill it in, in order (see README.md in this folder for the long version):
 *   1. Device names: open the Control Hub configuration on the Driver Station and copy the names
 *      exactly. Set pinpoint or limelight to null if the robot does not have one.
 *   2. Motor directions: with everything FORWARD, push the left stick forward in the TeleOp and
 *      REVERSE any wheel that spins backward.
 *   3. IMU mounting: which way the REV logo faces and which way the USB ports face.
 *   4. Pinpoint pod offsets and directions (skip if no Pinpoint).
 *   5. Calibration: start with the defaults, then measure with the Encoder Move Check OpMode.
 *   6. Tuning (point-to-point and tag approach): start with the defaults, then tune on the floor.
 */
public final class Test2027BotConfig {

    private Test2027BotConfig() {}

    public static RobotConfig create() {
        return new RobotConfig(
                // 2. Motor directions. Verified on the Skyline chassis 2026-09-07: forward, both
                //    strafes, and both turns all go the right way with the left side reversed. Which
                //    side needs reversing depends on how the motors face; test, do not assume.
                new RobotConfig.DrivetrainConfig(
                        DcMotorEx.Direction.REVERSE,   // left front
                        DcMotorEx.Direction.FORWARD,   // right front
                        DcMotorEx.Direction.REVERSE,   // left rear
                        DcMotorEx.Direction.FORWARD    // right rear
                ),
                // 4. Pinpoint: pod offsets from the robot center in mm (X pod is the forward pod,
                //    Y pod the sideways pod) and the direction each pod counts positive.
                // Pinpoint pod offsets in mm, measured 2026-09-07: the X (forward) pod sits 12 cm to the
                // LEFT of the robot's center (left is positive), the Y (sideways) pod 12 cm BEHIND it
                // (forward is positive). Wrong offsets show up as the position drifting during turns.
                new RobotConfig.OdometryConfig(
                        120.0, -120.0,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD
                ),
                // 3. Control Hub mounting.
                new RobotConfig.ImuConfig(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
                ),
                // 6a. Pinpoint point-to-point PID (driveTo). Gains are per mm of error, so P alone
                //     saturates the output at 1/P mm from the target: the first numbers here (P 0.019,
                //     full power until 2 in out) made Drive Square overshoot and hunt at every corner on
                //     the Skyline chassis, 2026-09-07. Now P3 Bot3's tuned numbers (P 0.0035, the robot
                //     starts slowing about 11 in out). Tune with Drive Square, one thing at a time:
                //     overshoots: lower P a quarter or raise D toward 0.002; stops short and creeps:
                //     raise P a quarter; heading wobbles at the corners: yaw P 2.0, yaw D 0.2.
                new RobotConfig.PointToPointTuning()
                        .xyToleranceMm(18.0).yawToleranceRad(0.055)
                        .xyGains(0.0035, 0.000003, 0.001).xyAccel(8.0)
                        .yawGains(2.5, 0.0, 0.08).yawAccel(10.0),
                // 6c. Pedro Pathing (doc/PEDRO_ON_TEST2027.md). Because this is set, DriveUtil2026b builds
                //     a Pedro Follower for this robot, and the Follower owns the Pinpoint. Tuned on the
                //     Skyline chassis 2026-09-07 with the "Tuning" OpMode (Driver Station group Pedro):
                //     velocities from the Forward and Lateral Velocity Tuners (K2), predictive braking
                //     from its tuner (K4). Heading PIDF: the library default snapped back cleanly in the
                //     Heading Tuner (K3), so it is not set here. Not yet done: .mass(kg) from a scale,
                //     and the Line / Triangle / Circle tests (K5, K6). To retune, run the same tuner
                //     and replace the number here; edits made in Panels are lost when the OpMode stops.
                new RobotConfig.PedroPathingConfig()
                        .velocities(81.1, 67.8)
                        .predictiveBraking(0.1, 0.0962, 0.00165).centripetalScaling(0)
        )
        .named("test2027bot")
        // 1. Device names, exactly as in the Control Hub configuration.
        .withHardware(new RobotConfig.HardwareNames()
                .driveMotors("Front_Left", "Front_Right", "Rear_Left", "Rear_Right")
                .imu("imu")
                .pinpoint("odo")            // null if this robot has no Pinpoint
                .limelight("limelight")     // null if this robot has no Limelight
                .led(null))                 // no status LED on the test robot
        // 5. Calibration. Defaults were tuned on one robot years ago; measure this one.
        .withCalibration(new RobotConfig.Calibration()
                .rightRearPowerScale(1.0)   // start with no correction; measure drift first
                // Measured on the Skyline chassis 2026-09-07 with the Encoder Move Check:
                // strafe: commanded 24 in went 28 in with x1.1, so 1.1 x 24/28.
                // turn: commanded 360 turned about 135 with 27.5, so 27.5 x 360/135. Refine with a
                // commanded 90 (D-pad right): new = 73 x 90 / degrees actually turned.
                .strafeScale(0.95)          // refined on the floor after the first 0.94 estimate
                .turnCircumferenceIn(79.0)  // refined with a commanded 90 after the first 73 estimate
                // 312 rpm goBILDA motor (537.7 ticks/rev), direct drive, 140 mm goBILDA mecanum
                // wheel. Different motor or wheel? Change these three numbers; the Encoder Move
                // Check then measures the real value. (Found 2026-09-07: with 96 here, a commanded
                // 24 in drove 35 in, exactly the 140/96 wheel ratio.)
                .encoderCountsPerInch(RobotConfig.Calibration.countsPerInch(537.7, 1.0, 140)))
        // 6b. Tag approach (driveToTagAsync). Gentle on purpose: the robot is about to touch something.
        .withTagApproach(new TagApproach.Settings()
                // First live run 2026-09-07: settled DONE at 60.1 in, 0.3 in off center, 0.4 deg off
                // square, after a fair bit of hunting near the target. Hunting is the minimum power
                // pushing across a tight tolerance band, so the band is wider and the nudge smaller.
                // If it still swings on the way in, lower the kp gains by about a third.
                .kpDrive(0.04).kpStrafe(0.04).kpYaw(0.015)
                .maxPower(0.3).minPower(0.05)
                .toleranceInches(2.0).toleranceDegrees(3.0)
                .lostTimeoutSec(1.5).maxTimeSec(10.0));   // 10 s: a goal approach can start 8 ft out at 0.3 power
    }
}
