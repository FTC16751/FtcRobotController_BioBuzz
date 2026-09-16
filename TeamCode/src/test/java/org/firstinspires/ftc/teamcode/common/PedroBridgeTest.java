package org.firstinspires.ftc.teamcode.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.common.test.StandardBotConfig;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027BotConfig;
import org.junit.Test;

/**
 * The RobotConfig to Pedro Pathing bridge. The one thing that matters most is the pod-offset
 * mapping: Pedro's forwardPodY is passed to goBILDA's setOffsets as the X (forward) pod's
 * sideways offset, which is what OdometryConfig.pinpointOffsetX_mm holds. The 2025 bridge had the
 * two swapped, which is one reason Pedro never localized right (doc/PEDRO_ON_TEST2027.md).
 */
public class PedroBridgeTest {

    private final RobotConfig test2027 = Test2027BotConfig.create();

    @Test
    public void pinpointOffsetsLandOnTheRightPedroFields() {
        PinpointConstants pc = PedroBridge.pinpointConstantsFor(test2027);
        assertEquals(test2027.odometry.pinpointOffsetX_mm, pc.forwardPodY, 1e-9);
        assertEquals(test2027.odometry.pinpointOffsetY_mm, pc.strafePodX, 1e-9);
        // and in plain numbers, as measured on the Skyline chassis 2026-09-07
        assertEquals(120.0, pc.forwardPodY, 1e-9);
        assertEquals(-120.0, pc.strafePodX, 1e-9);
        assertEquals(DistanceUnit.MM, pc.distanceUnit);
    }

    @Test
    public void pinpointNameAndPodDirectionsComeFromTheConfig() {
        PinpointConstants pc = PedroBridge.pinpointConstantsFor(test2027);
        assertEquals("odo", pc.hardwareMapName);
        assertEquals(test2027.odometry.pinpointXPodDirection, pc.forwardEncoderDirection);
        assertEquals(test2027.odometry.pinpointYPodDirection, pc.strafeEncoderDirection);
    }

    @Test
    public void motorNamesAndDirectionsComeFromTheConfig() {
        MecanumConstants mc = PedroBridge.mecanumConstantsFor(test2027);
        assertEquals(test2027.hardware.leftFront,  mc.leftFrontMotorName);
        assertEquals(test2027.hardware.leftRear,   mc.leftRearMotorName);
        assertEquals(test2027.hardware.rightFront, mc.rightFrontMotorName);
        assertEquals(test2027.hardware.rightRear,  mc.rightRearMotorName);
        assertEquals(test2027.drivetrain.leftFrontDirection,  mc.leftFrontMotorDirection);
        assertEquals(test2027.drivetrain.leftRearDirection,   mc.leftRearMotorDirection);
        assertEquals(test2027.drivetrain.rightFrontDirection, mc.rightFrontMotorDirection);
        assertEquals(test2027.drivetrain.rightRearDirection,  mc.rightRearMotorDirection);
        assertEquals(test2027.pedroPathing.driveMaxVelo,  mc.xVelocity, 1e-9);
        assertEquals(test2027.pedroPathing.strafeMaxVelo, mc.yVelocity, 1e-9);
    }

    @Test
    public void followerConstantsCarryTheTuning() {
        FollowerConstants fc = PedroBridge.followerConstantsFor(test2027);
        assertEquals(test2027.pedroPathing.followerMass, fc.mass, 1e-9);
        assertEquals(test2027.pedroPathing.forwardZeroPowerAccel, fc.forwardZeroPowerAcceleration, 1e-9);
        assertEquals(test2027.pedroPathing.lateralZeroPowerAccel, fc.lateralZeroPowerAcceleration, 1e-9);
        assertEquals(test2027.pedroPathing.centripetalScaling, fc.centripetalScaling, 1e-9);
        // Test2027 has carried predictive braking since the 2026-09-08 tuning session (test plan K4).
        assertTrue("predictive braking once predictiveBraking(...) is set", fc.usePredictiveBraking);
    }

    @Test
    public void withoutPredictiveBrakingTheBridgeUsesThePidfDrive() {
        RobotConfig cfg = Test2027BotConfig.create();
        cfg.pedroPathing.predictiveBraking = null;    // an untuned robot
        assertFalse(PedroBridge.followerConstantsFor(cfg).usePredictiveBraking);
    }

    @Test
    public void settingPredictiveBrakingSwitchesTheDriveAlgorithm() {
        RobotConfig cfg = Test2027BotConfig.create();
        cfg.pedroPathing.predictiveBraking(0.1, 0.2, 0.003).centripetalScaling(0);
        FollowerConstants fc = PedroBridge.followerConstantsFor(cfg);
        assertTrue(fc.usePredictiveBraking);
        assertEquals(0.0, fc.centripetalScaling, 1e-9);
    }

    @Test
    public void aRobotWithoutPedroIsRefusedByName() {
        RobotConfig standard = StandardBotConfig.create();
        try {
            PedroBridge.followerConstantsFor(standard);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(standard.robotName));
        }
    }
}
