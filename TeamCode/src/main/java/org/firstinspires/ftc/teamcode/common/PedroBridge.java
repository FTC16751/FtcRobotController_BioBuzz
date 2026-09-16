package org.firstinspires.ftc.teamcode.common;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Builds Pedro Pathing 3's config objects and Follower FROM a RobotConfig, so a robot's device
 * names, motor directions, Pinpoint pod offsets and Foresight tuning all live in the one config
 * file for that chassis. Nothing Pedro-specific is typed twice. (doc/PEDRO_ON_TEST2027.md)
 *
 * Pod offsets: goBILDA's Pinpoint takes setOffsets(xOffset, yOffset, unit): xOffset is how far the
 * X (forward) pod sits to the LEFT of the robot's centre, yOffset how far the Y (sideways) pod sits
 * FORWARD of it. RobotConfig.OdometryConfig stores exactly those two numbers in mm, and Pedro 3's
 * PinpointConfig passes xPodOffset / yPodOffset straight through to setOffsets in that order (read
 * from the 3.0.0 bytecode, 2026-09-15), so there is no field swap to get wrong any more. The 2025
 * bridge had one; PedroBridgeTest pins the mapping anyway.
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

    public static MecanumConfig mecanumConfigFor(RobotConfig config) {
        RobotConfig.PedroPathingConfig p = requirePedro(config);
        return new MecanumConfig(c -> {
            c.frontLeftName.set(config.hardware.leftFront);
            c.backLeftName.set(config.hardware.leftRear);
            c.frontRightName.set(config.hardware.rightFront);
            c.backRightName.set(config.hardware.rightRear);
            c.frontLeftDirection.set(config.drivetrain.leftFrontDirection);
            c.backLeftDirection.set(config.drivetrain.leftRearDirection);
            c.frontRightDirection.set(config.drivetrain.rightFrontDirection);
            c.backRightDirection.set(config.drivetrain.rightRearDirection);
            c.manualBrakeMode.set(p.brakeInManualDrive);
        });
    }

    public static PinpointConfig pinpointConfigFor(RobotConfig config) {
        if (config.hardware.pinpoint == null) {
            throw new IllegalArgumentException("RobotConfig '" + config.robotName + "' has no Pinpoint; Pedro needs one.");
        }
        return new PinpointConfig(c -> {
            c.name.set(config.hardware.pinpoint);
            c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
            c.xPodOffset.set(config.odometry.pinpointOffsetX_mm);   // X pod, left of centre (see the class comment)
            c.yPodOffset.set(config.odometry.pinpointOffsetY_mm);   // Y pod, forward of centre
            c.offsetUnits.set(DistanceUnit.MM);
            c.globalDistanceUnit.set(DistanceUnit.INCH);            // Pedro poses in inches, like the rest of our code
            c.xPodDirection.set(config.odometry.pinpointXPodDirection);
            c.yPodDirection.set(config.odometry.pinpointYPodDirection);
        });
    }

    /**
     * This robot's Foresight config, or an IllegalArgumentException naming the first number AutoTune
     * has not supplied yet. Foresight has twelve numbers with no library default; checking them here,
     * by name, beats a stack trace from inside Pedro's first update().
     */
    public static ForesightConfig foresightConfigFor(RobotConfig config) {
        ForesightConfig fc = new ForesightConfig(requirePedro(config).foresight);
        String[] names = { "maxAchievableForwardVelocity", "maxAchievableStrafeVelocity",
                "naturalForwardDeceleration", "naturalStrafeDeceleration",
                "linearBrakeCoefficients", "quadraticBrakeCoefficients", "headingBrakeCoefficients",
                "headingFeedback", "forwardTranslational", "strafeTranslational", "brake", "coast" };
        Object[] values;
        try {
            values = new Object[] {
                    fc.maxAchievableForwardVelocity.get(), fc.maxAchievableStrafeVelocity.get(),
                    fc.naturalForwardDeceleration.get(), fc.naturalStrafeDeceleration.get(),
                    fc.linearBrakeCoefficients.get(), fc.quadraticBrakeCoefficients.get(), fc.headingBrakeCoefficients.get(),
                    fc.headingFeedback.get(), fc.forwardTranslational.get(), fc.strafeTranslational.get(), fc.brake.get(), fc.coast.get() };
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("RobotConfig '" + config.robotName
                    + "': the Foresight tuning is incomplete (" + e.getMessage()
                    + "). Run AutoTune's Foresight procedure and paste its lambda into the PedroPathingConfig.", e);
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException("RobotConfig '" + config.robotName + "': Foresight's " + names[i]
                        + " is not set. Run AutoTune's Foresight procedure and paste its lambda into the PedroPathingConfig.");
            }
        }
        return fc;
    }

    /** Opens and configures the Pinpoint for Pedro. Whoever holds this owns the device; see DriveUtil2026b. */
    public static PinpointLocalizer createPinpointLocalizer(HardwareMap hardwareMap, RobotConfig config) {
        return new PinpointLocalizer(hardwareMap, pinpointConfigFor(config));
    }

    /** Pedro's mecanum drivetrain on this robot's four motors (the same DcMotorEx objects DriveUtil2026b holds). */
    public static Mecanum createDrivetrain(HardwareMap hardwareMap, RobotConfig config) {
        return new Mecanum(hardwareMap, mecanumConfigFor(config));
    }

    /** The Foresight algorithm with this robot's tuning. */
    public static Foresight createForesight(RobotConfig config) {
        return new Foresight(foresightConfigFor(config));
    }

    /** A Follower on an already-built localizer, so the caller can keep the Pinpoint it opened. */
    public static Follower createFollower(HardwareMap hardwareMap, RobotConfig config, PinpointLocalizer localizer) {
        return new Follower(localizer, createDrivetrain(hardwareMap, config), createForesight(config));
    }

    /** A Follower that opens its own Pinpoint. Only for OpModes that do NOT also build a DriveUtil2026b. */
    public static Follower createFollower(HardwareMap hardwareMap, RobotConfig config) {
        return createFollower(hardwareMap, config, createPinpointLocalizer(hardwareMap, config));
    }
}
