package org.firstinspires.ftc.teamcode.common;

import com.pedropathing.control.PredictiveBrakingCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.ftc.localization.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Builds Pedro Pathing's constants objects and Follower FROM a RobotConfig, so a robot's device
 * names, motor directions, Pinpoint pod offsets and Pedro tuning all live in the one config file
 * for that chassis. Nothing Pedro-specific is typed twice. (doc/PEDRO_ON_TEST2027.md)
 *
 * The one convention worth knowing is the pod offsets. goBILDA's Pinpoint takes
 * setOffsets(xOffset, yOffset): xOffset is how far the X (forward) pod sits to the LEFT of the
 * robot's centre, yOffset how far the Y (sideways) pod sits FORWARD of it. RobotConfig.OdometryConfig
 * stores exactly those two numbers in mm, and DriveUtil2026b passes them to setOffsets in that
 * order. Pedro's PinpointLocalizer passes its forwardPodY as xOffset and its strafePodX as
 * yOffset, so forwardPodY gets our X offset and strafePodX gets our Y offset. (The pre-2026-09-07
 * bridge had these swapped.) PedroBridgeTest pins this.
 */
public final class PedroBridge {

    private PedroBridge() {}

    /** The Pedro section of a config, or an IllegalArgumentException naming the robot if it has none. */
    public static RobotConfig.PedroPathingConfig requirePedro(RobotConfig config) {
        if (config.pedroPathing == null) {
            throw new IllegalArgumentException("RobotConfig '" + config.robotName
                    + "' has no PedroPathingConfig; add one to that robot's config file to use Pedro Pathing.");
        }
        return config.pedroPathing;
    }

    public static FollowerConstants followerConstantsFor(RobotConfig config) {
        RobotConfig.PedroPathingConfig p = requirePedro(config);
        FollowerConstants fc = new FollowerConstants()
                .mass(p.followerMass)
                .forwardZeroPowerAcceleration(p.forwardZeroPowerAccel)
                .lateralZeroPowerAcceleration(p.lateralZeroPowerAccel)
                .translationalPIDFCoefficients(p.translationalPIDF)
                .headingPIDFCoefficients(p.headingPIDF)
                .centripetalScaling(p.centripetalScaling);
        if (p.predictiveBraking != null) {
            // Setting the coefficients is what switches Pedro from the PIDF drive to predictive braking.
            fc.predictiveBrakingCoefficients(new PredictiveBrakingCoefficients(
                    p.predictiveBraking[0], p.predictiveBraking[1], p.predictiveBraking[2]));
        }
        return fc;
    }

    public static MecanumConstants mecanumConstantsFor(RobotConfig config) {
        RobotConfig.PedroPathingConfig p = requirePedro(config);
        return new MecanumConstants()
                .maxPower(1.0)
                .leftFrontMotorName(config.hardware.leftFront)
                .leftRearMotorName(config.hardware.leftRear)
                .rightFrontMotorName(config.hardware.rightFront)
                .rightRearMotorName(config.hardware.rightRear)
                .leftFrontMotorDirection(config.drivetrain.leftFrontDirection)
                .leftRearMotorDirection(config.drivetrain.leftRearDirection)
                .rightFrontMotorDirection(config.drivetrain.rightFrontDirection)
                .rightRearMotorDirection(config.drivetrain.rightRearDirection)
                .xVelocity(p.driveMaxVelo)
                .yVelocity(p.strafeMaxVelo);
    }

    public static PinpointConstants pinpointConstantsFor(RobotConfig config) {
        if (config.hardware.pinpoint == null) {
            throw new IllegalArgumentException("RobotConfig '" + config.robotName + "' has no Pinpoint; Pedro needs one.");
        }
        return new PinpointConstants()
                .forwardPodY(config.odometry.pinpointOffsetX_mm)   // see the class comment: X offset -> forwardPodY
                .strafePodX(config.odometry.pinpointOffsetY_mm)    //                        Y offset -> strafePodX
                .distanceUnit(DistanceUnit.MM)
                .hardwareMapName(config.hardware.pinpoint)
                .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
                .forwardEncoderDirection(config.odometry.pinpointXPodDirection)
                .strafeEncoderDirection(config.odometry.pinpointYPodDirection);
    }

    /** Opens and configures the Pinpoint for Pedro. Whoever holds this owns the device; see DriveUtil2026b. */
    public static PinpointLocalizer createPinpointLocalizer(HardwareMap hardwareMap, RobotConfig config) {
        return new PinpointLocalizer(hardwareMap, pinpointConstantsFor(config));
    }

    /** A Follower on an already-built localizer, so the caller can keep the Pinpoint it opened. */
    public static Follower createFollower(HardwareMap hardwareMap, RobotConfig config, PinpointLocalizer localizer) {
        return new FollowerBuilder(followerConstantsFor(config), hardwareMap)
                .pathConstraints(requirePedro(config).pathConstraints)
                .mecanumDrivetrain(mecanumConstantsFor(config))
                .setLocalizer(localizer)
                .build();
    }

    /** A Follower that opens its own Pinpoint. Only for OpModes that do NOT also build a DriveUtil2026b. */
    public static Follower createFollower(HardwareMap hardwareMap, RobotConfig config) {
        return createFollower(hardwareMap, config, createPinpointLocalizer(hardwareMap, config));
    }
}
