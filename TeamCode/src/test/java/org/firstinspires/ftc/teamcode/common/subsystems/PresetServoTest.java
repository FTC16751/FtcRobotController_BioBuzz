package org.firstinspires.ftc.teamcode.common.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.qualcomm.robotcore.hardware.Servo;

import org.junit.Before;
import org.junit.Test;

/**
 * PresetServo moves to named positions, clamps everything to its limits, gangs a paired servo,
 * and a flick goes out and comes back on its own through update().
 */
public class PresetServoTest {

    private FakeServo servo;
    private FakeClock clock;
    private PresetServo wrist;

    @Before
    public void setUp() {
        servo = new FakeServo();
        clock = new FakeClock();
        wrist = new PresetServo(servo, clock).preset("UP", 0.8).preset("DOWN", 0.2);
    }

    @Test
    public void nothingMovesUntilCommanded() {
        assertTrue(Double.isNaN(servo.position));
        assertEquals("none", wrist.getPresetName());
        assertEquals(0, servo.commandCount);
    }

    @Test
    public void goToSendsThePresetAndRemembersItsName() {
        wrist.goTo("UP");
        assertEquals(0.8, servo.position, 0.0);
        assertEquals("UP", wrist.getPresetName());
        assertTrue(wrist.isAt("UP"));
        assertFalse(wrist.isAt("DOWN"));
    }

    @Test
    public void unknownPresetThrowsAndListsTheKnownOnes() {
        try {
            wrist.goTo("SIDEWAYS");
            fail("expected an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("SIDEWAYS"));
            assertTrue(e.getMessage(), e.getMessage().contains("UP"));
            assertTrue(e.getMessage(), e.getMessage().contains("DOWN"));
        }
        assertTrue("servo untouched", Double.isNaN(servo.position));
    }

    @Test
    public void startAtIsTheOptInForMovingDuringInit() {
        wrist.startAt("DOWN");
        assertEquals(0.2, servo.position, 0.0);
    }

    @Test
    public void limitsClampPresetsAndManualPositions() {
        wrist.limits(0.3, 0.7);
        wrist.goTo("UP");
        assertEquals(0.7, servo.position, 0.0);
        wrist.setPosition(-1);
        assertEquals(0.3, servo.position, 0.0);
        assertEquals("manual", wrist.getPresetName());
    }

    @Test
    public void limitsAcceptEitherOrder() {
        wrist.limits(0.9, 0.1);
        wrist.setPosition(2.0);
        assertEquals(0.9, servo.position, 0.0);
    }

    @Test
    public void nudgeMovesFromTheLastCommand() {
        wrist.goTo("DOWN");
        wrist.nudge(0.05);
        assertEquals(0.25, servo.position, 1e-9);
        wrist.nudge(-0.1);
        assertEquals(0.15, servo.position, 1e-9);
        assertEquals("manual", wrist.getPresetName());
    }

    @Test
    public void nudgeBeforeAnyCommandStartsFromTheMiddle() {
        wrist.limits(0.2, 0.6);
        wrist.nudge(0.1);
        assertEquals(0.5, servo.position, 1e-9);
    }

    @Test
    public void pairedServoGetsTheSameCommand() {
        FakeServo right = new FakeServo();
        right.setDirection(Servo.Direction.REVERSE);
        wrist.pair(right);
        wrist.goTo("UP");
        assertEquals(0.8, servo.position, 0.0);
        assertEquals(0.8, right.position, 0.0);
        assertEquals(Servo.Direction.REVERSE, right.direction);   // the SDK mirrors it physically
    }

    @Test
    public void flickGoesOutHoldsAndComesBack() {
        wrist.goTo("DOWN");
        wrist.flick("UP", 0.4);
        assertEquals(0.8, servo.position, 0.0);
        assertTrue(wrist.isBusy());

        clock.advance(0.3);
        wrist.update();
        assertTrue(wrist.isBusy());
        assertEquals(0.8, servo.position, 0.0);

        clock.advance(0.1);
        wrist.update();
        assertFalse(wrist.isBusy());
        assertEquals(0.2, servo.position, 0.0);
        assertEquals("DOWN", wrist.getPresetName());
    }

    @Test
    public void flickBeforeAnyCommandReturnsToTheMiddle() {
        wrist.flick("UP", 1.0);
        clock.advance(1.0);
        wrist.update();
        assertEquals(0.5, servo.position, 1e-9);
    }

    @Test
    public void aNewCommandReplacesAFlick() {
        wrist.goTo("DOWN");
        wrist.flick("UP", 1.0);
        wrist.goTo("DOWN");
        assertFalse(wrist.isBusy());
        clock.advance(5);
        wrist.update();
        assertEquals(0.2, servo.position, 0.0);
        assertEquals("DOWN, UP (flick), DOWN and nothing after", 3, servo.commandCount);
    }

    @Test
    public void stopCancelsAFlickAndStaysPut() {
        wrist.goTo("DOWN");
        wrist.flick("UP", 1.0);
        wrist.stop();
        assertFalse(wrist.isBusy());
        clock.advance(5);
        wrist.update();
        assertEquals("stays where the flick left it", 0.8, servo.position, 0.0);
    }
}
