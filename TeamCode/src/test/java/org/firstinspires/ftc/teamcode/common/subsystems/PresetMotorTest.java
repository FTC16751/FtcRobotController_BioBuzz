package org.firstinspires.ftc.teamcode.common.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.junit.Before;
import org.junit.Test;

/**
 * PresetMotor drives every ganged motor to one target through RUN_TO_POSITION, clamps to its
 * limits, holds on manual(0), and re-zeroes on the home switch without pushing into it.
 */
public class PresetMotorTest {

    private FakeDcMotorEx a;
    private FakeDcMotorEx b;
    private FakeTouchSensor home;
    private PresetMotor lift;

    @Before
    public void setUp() {
        a = new FakeDcMotorEx();
        b = new FakeDcMotorEx();
        home = new FakeTouchSensor();
        lift = new PresetMotor(a).add(b)
                .preset("DOWN", 0).preset("LOW", 800).preset("HIGH", 2200)
                .limits(0, 2300)
                .tolerance(15)
                .power(0.8)
                .homeSwitch(home);
    }

    private void bothAt(int ticks) { a.currentPosition = ticks; b.currentPosition = ticks; }

    @Test
    public void setupZeroesTheEncodersAndBrakes() {
        assertEquals(1, a.resetCount);
        assertEquals(1, b.resetCount);
        assertEquals(DcMotor.RunMode.RUN_USING_ENCODER, a.mode);
        assertEquals(DcMotor.ZeroPowerBehavior.BRAKE, b.zeroPowerBehavior);
        assertEquals(0.0, a.power, 0.0);
        assertEquals("none", lift.getPresetName());
    }

    @Test
    public void goToCommandsEveryMotor() {
        lift.goTo("HIGH");
        for (FakeDcMotorEx m : new FakeDcMotorEx[] {a, b}) {
            assertEquals(2200, m.targetPosition);
            assertEquals(DcMotor.RunMode.RUN_TO_POSITION, m.mode);
            assertEquals(0.8, m.power, 0.0);
            assertEquals(15, m.tolerance);
        }
        assertEquals("HIGH", lift.getPresetName());
        assertEquals(2200, lift.getTarget());
    }

    @Test
    public void unknownPresetThrowsAndListsTheKnownOnes() {
        try {
            lift.goTo("MIDDLE");
            fail("expected an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("MIDDLE"));
            assertTrue(e.getMessage(), e.getMessage().contains("HIGH"));
        }
        assertEquals("nothing commanded", 0.0, a.power, 0.0);
    }

    @Test
    public void busyUntilWithinToleranceThenAtTarget() {
        lift.goTo("LOW");
        bothAt(300);
        assertTrue(lift.isBusy());
        assertFalse(lift.isAtTarget());
        bothAt(790);
        assertFalse(lift.isBusy());
        assertTrue(lift.isAtTarget());
        assertTrue(lift.isAt("LOW"));
    }

    @Test
    public void notAtTargetInManualOrBeforeAnyMove() {
        assertFalse(lift.isAtTarget());
        lift.manual(0.5);
        bothAt(0);
        assertFalse(lift.isAtTarget());
        assertFalse(lift.isBusy());
    }

    @Test
    public void goToTicksIsClampedToTheLimits() {
        lift.goToTicks(5000);
        assertEquals(2300, a.targetPosition);
        lift.goToTicks(-40);
        assertEquals(0, b.targetPosition);
        assertEquals("manual", lift.getPresetName());
    }

    @Test
    public void nudgeMovesFromTheTargetWhenOnOne() {
        lift.goTo("LOW");
        bothAt(420);                 // still on the way
        lift.nudge(50);
        assertEquals(850, a.targetPosition);
    }

    @Test
    public void nudgeMovesFromThePositionWhenNotOnATarget() {
        bothAt(600);
        lift.nudge(-100);
        assertEquals(500, a.targetPosition);
        assertEquals(DcMotor.RunMode.RUN_TO_POSITION, a.mode);
    }

    @Test
    public void manualDrivesWithPowerInEncoderMode() {
        bothAt(1000);
        lift.manual(0.6);
        assertEquals(DcMotor.RunMode.RUN_USING_ENCODER, a.mode);
        assertEquals(0.6, a.power, 0.0);
        assertEquals(0.6, b.power, 0.0);
        assertEquals("manual", lift.getPresetName());
    }

    @Test
    public void manualIsClampedToPlusMinusOne() {
        bothAt(1000);
        lift.manual(3);
        assertEquals(1.0, a.power, 0.0);
    }

    @Test
    public void manualCannotPushPastALimit() {
        bothAt(2300);
        lift.manual(0.9);
        assertEquals("holds at the top instead", DcMotor.RunMode.RUN_TO_POSITION, a.mode);
        assertEquals(2300, a.targetPosition);

        bothAt(0);
        lift.manual(-0.9);
        assertEquals(0, a.targetPosition);
        assertEquals(DcMotor.RunMode.RUN_TO_POSITION, a.mode);

        bothAt(1000);
        lift.manual(-0.9);
        assertEquals("free to move in between", -0.9, a.power, 0.0);
    }

    @Test
    public void releasingTheStickHoldsPosition() {
        bothAt(1200);
        lift.manual(0.5);
        bothAt(1250);
        lift.manual(0);
        assertEquals(1250, a.targetPosition);
        assertEquals(DcMotor.RunMode.RUN_TO_POSITION, a.mode);
        assertEquals(0.8, a.power, 0.0);
    }

    @Test
    public void manualZeroWhileOnAPresetKeepsThePreset() {
        lift.goTo("HIGH");
        bothAt(2200);
        lift.manual(0);
        assertEquals(2200, a.targetPosition);
        assertEquals("HIGH", lift.getPresetName());
    }

    @Test
    public void stopCutsPower() {
        lift.goTo("HIGH");
        lift.stop();
        assertEquals(0.0, a.power, 0.0);
        assertEquals(0.0, b.power, 0.0);
        assertFalse(lift.isBusy());
    }

    @Test
    public void zeroHereResetsAndReissuesTheMove() {
        lift.goTo("HIGH");
        bothAt(500);
        lift.zeroHere();
        assertEquals(2, a.resetCount);
        assertEquals(0, a.currentPosition);
        assertEquals(2200, a.targetPosition);
        assertEquals(DcMotor.RunMode.RUN_TO_POSITION, a.mode);
        assertEquals("HIGH", lift.getPresetName());
    }

    @Test
    public void homeSwitchRisingEdgeReZeroesOnce() {
        bothAt(-30);                      // drifted below the zero it was built with
        lift.manual(-0.5);
        home.pressed = true;
        lift.update();
        assertEquals(2, a.resetCount);
        assertTrue(lift.isAtHome());
        lift.update();                    // still pressed: no second reset
        assertEquals(2, a.resetCount);
        home.pressed = false;
        lift.update();
        home.pressed = true;
        lift.update();                    // pressed again: a new rising edge
        assertEquals(3, a.resetCount);
    }

    @Test
    public void homeSwitchStopsManualPowerFromPushingIntoIt() {
        bothAt(200);
        lift.manual(-0.7);
        assertEquals(-0.7, a.power, 0.0);
        home.pressed = true;
        lift.update();
        assertEquals("holds at the new zero", 0, a.targetPosition);
        assertEquals(DcMotor.RunMode.RUN_TO_POSITION, a.mode);
    }

    @Test
    public void homeSwitchClampsATargetBelowZero() {
        lift.limits(-500, 2300);          // a team that let it nudge below zero
        lift.goToTicks(-200);
        home.pressed = true;
        lift.update();
        assertEquals(0, a.targetPosition);
    }

    @Test
    public void homeSwitchLeavesAnUpwardMoveAlone() {
        lift.goTo("LOW");
        home.pressed = true;
        lift.update();
        assertEquals(800, a.targetPosition);
        assertEquals("LOW", lift.getPresetName());
    }

    @Test
    public void noHomeSwitchMeansUpdateIsHarmless() {
        PresetMotor arm = new PresetMotor(new FakeDcMotorEx());
        arm.update();
        assertFalse(arm.isAtHome());
    }
}
