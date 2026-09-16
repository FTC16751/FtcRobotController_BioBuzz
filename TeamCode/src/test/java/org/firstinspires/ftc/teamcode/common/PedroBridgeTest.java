package org.firstinspires.ftc.teamcode.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.common.test.StandardBotConfig;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027BotConfig;
import org.junit.Test;

/**
 * The RobotConfig to Pedro Pathing 3 bridge. The thing that matters most is the pod-offset mapping:
 * the X pod's leftward offset must reach the Pinpoint as xOffset and the Y pod's forward offset as
 * yOffset, in mm. The 2025 bridge had them swapped, which is one reason Pedro never localized right
 * (doc/PEDRO_ON_TEST2027.md); Pedro 3 passes them straight through, and this pins that.
 */
public class PedroBridgeTest {

    private final RobotConfig test2027 = Test2027BotConfig.create();

    @Test
    public void pinpointOffsetsLandOnTheRightPedroFields() {
        PinpointConfig pc = PedroBridge.pinpointConfigFor(test2027);
        assertEquals(test2027.odometry.pinpointOffsetX_mm, pc.xPodOffset.get(), 1e-9);
        assertEquals(test2027.odometry.pinpointOffsetY_mm, pc.yPodOffset.get(), 1e-9);
        // and in plain numbers, as measured on the Skyline chassis 2026-09-07
        assertEquals(120.0, pc.xPodOffset.get(), 1e-9);
        assertEquals(-120.0, pc.yPodOffset.get(), 1e-9);
        assertEquals(DistanceUnit.MM, pc.offsetUnits.get());
        assertEquals("poses are in inches everywhere else in our code", DistanceUnit.INCH, pc.globalDistanceUnit.get());
    }

    @Test
    public void pinpointNameAndPodDirectionsComeFromTheConfig() {
        PinpointConfig pc = PedroBridge.pinpointConfigFor(test2027);
        assertEquals("odo", pc.name.get());
        assertEquals(test2027.odometry.pinpointXPodDirection, pc.xPodDirection.get());
        assertEquals(test2027.odometry.pinpointYPodDirection, pc.yPodDirection.get());
    }

    @Test
    public void motorNamesAndDirectionsComeFromTheConfig() {
        MecanumConfig mc = PedroBridge.mecanumConfigFor(test2027);
        assertEquals(test2027.hardware.leftFront,  mc.frontLeftName.get());
        assertEquals(test2027.hardware.leftRear,   mc.backLeftName.get());
        assertEquals(test2027.hardware.rightFront, mc.frontRightName.get());
        assertEquals(test2027.hardware.rightRear,  mc.backRightName.get());
        assertEquals(test2027.drivetrain.leftFrontDirection,  mc.frontLeftDirection.get());
        assertEquals(test2027.drivetrain.leftRearDirection,   mc.backLeftDirection.get());
        assertEquals(test2027.drivetrain.rightFrontDirection, mc.frontRightDirection.get());
        assertEquals(test2027.drivetrain.rightRearDirection,  mc.backRightDirection.get());
        assertTrue("the Pedro TeleOp drive brakes on centred sticks (K10, 2026-09-08)", mc.manualBrakeMode.get());
    }

    @Test
    public void aCompleteForesightLambdaCarriesTheTuning() {
        RobotConfig cfg = Test2027BotConfig.create();
        cfg.pedroPathing.foresight(c -> {
            // Shaped exactly like what AutoTune prints (numbers made up for the test).
            c.forwardTranslational.set(Controller.piecewise(Controller.proportional(0.05)).put(2.5, Controller.proportional(0.1)));
            c.strafeTranslational.set(Controller.piecewise(Controller.proportional(0.05)).put(2.5, Controller.proportional(0.1)));
            c.coast.set(Controller.proportionalFeedforward(0.02));
            c.brake.set(Controller.proportionalFeedforward(0.03));
            c.headingFeedback.set(Controller.proportional(1.0));
            c.headingBrakeCoefficients.set(Vector2D.cartesian(0.1, 0.01));
            c.linearBrakeCoefficients.set(Matrix.diag(0.09, 0.08));
            c.quadraticBrakeCoefficients.set(Matrix.diag(0.002, 0.001));
            c.maxAchievableForwardVelocity.set(81.1);
            c.maxAchievableStrafeVelocity.set(67.8);
            c.naturalForwardDeceleration.set(40.0);
            c.naturalStrafeDeceleration.set(60.0);
        });
        ForesightConfig fc = PedroBridge.foresightConfigFor(cfg);
        assertEquals(81.1, fc.maxAchievableForwardVelocity.get(), 1e-9);
        assertEquals(67.8, fc.maxAchievableStrafeVelocity.get(), 1e-9);
        assertEquals(40.0, fc.naturalForwardDeceleration.get(), 1e-9);
        assertTrue(PedroBridge.createForesight(cfg) != null);
    }

    @Test
    public void anIncompleteForesightLambdaIsRefusedByName() {
        // Test2027's block carries only the top speeds until AutoTune runs (2026-09-15).
        try {
            PedroBridge.foresightConfigFor(test2027);
            fail("expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(test2027.robotName));
            assertTrue(e.getMessage(), e.getMessage().contains("AutoTune"));
        }
        assertTrue("the Pinpoint and mecanum halves still build for AutoTune",
                PedroBridge.pinpointConfigFor(test2027) != null && PedroBridge.mecanumConfigFor(test2027) != null);
    }

    @Test
    public void aRobotWithoutPedroIsRefusedByName() {
        RobotConfig standard = StandardBotConfig.create();   // no PedroPathingConfig
        try {
            PedroBridge.foresightConfigFor(standard);
            fail("expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(standard.robotName));
        }
    }
}
