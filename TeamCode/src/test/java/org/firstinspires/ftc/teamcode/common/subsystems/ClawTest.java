package org.firstinspires.ftc.teamcode.common.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Servo;

import org.junit.Test;

/** Claw is a PresetServo with OPEN and CLOSED filled in; toggle flips between them. */
public class ClawTest {

    @Test
    public void openAndCloseSendTheGivenPositions() {
        FakeServo servo = new FakeServo();
        Claw claw = new Claw(servo, 0.25, 0.70);
        claw.open();
        assertEquals(0.25, servo.position, 0.0);
        assertTrue(claw.isOpen());
        claw.close();
        assertEquals(0.70, servo.position, 0.0);
        assertTrue(claw.isClosed());
        assertFalse(claw.isOpen());
    }

    @Test
    public void toggleOpensFirstThenAlternates() {
        FakeServo servo = new FakeServo();
        Claw claw = new Claw(servo, 0.25, 0.70);
        claw.toggle();
        assertTrue(claw.isOpen());
        claw.toggle();
        assertTrue(claw.isClosed());
        claw.toggle();
        assertTrue(claw.isOpen());
    }

    @Test
    public void pairedServoMirrorsThroughItsDirection() {
        FakeServo left = new FakeServo();
        FakeServo right = new FakeServo();
        right.setDirection(Servo.Direction.REVERSE);
        Claw claw = new Claw(left, 0.25, 0.70).pair(right);   // pair still returns a Claw
        claw.close();
        assertEquals(0.70, left.position, 0.0);
        assertEquals(0.70, right.position, 0.0);
        assertTrue(claw.isClosed());
    }

    @Test
    public void everythingAPresetServoDoesStillWorks() {
        FakeServo servo = new FakeServo();
        Claw claw = new Claw(servo, 0.25, 0.70).limits(0.3, 1.0);
        claw.open();
        assertEquals("open clamped by the limit", 0.3, servo.position, 0.0);
        claw.nudge(0.1);
        assertEquals(0.4, servo.position, 1e-9);
        assertFalse(claw.isOpen());
        assertEquals("manual", claw.getPresetName());
    }
}
