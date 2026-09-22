package org.firstinspires.ftc.teamcode.common.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.common.launch.Feeder;
import org.junit.Before;
import org.junit.Test;

/**
 * Roller drives every device in its group with one power, and a timed run ends by itself when
 * update() sees the clock pass the end time. A Roller is a Feeder, so LaunchController can use it.
 */
public class RollerTest {

    @Test
    public void brakeSetsMotorsOnlyAndLeavesServosAlone() {
        FakeDcMotorEx motor = new FakeDcMotorEx();
        FakeCRServo servo = new FakeCRServo();
        new Roller(motor).add(servo).brake();
        assertEquals(com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE, motor.zeroPowerBehavior);
        assertEquals("a servo has no zero-power behaviour; nothing to check but no crash", 0, servo.power, 0);
    }

    private FakeCRServo motor;
    private FakeCRServo servo;
    private FakeClock clock;
    private Roller roller;

    @Before
    public void setUp() {
        motor = new FakeCRServo();
        servo = new FakeCRServo();
        clock = new FakeClock();
        roller = new Roller(motor, clock).add(servo);
    }

    @Test
    public void inOutStopDriveEveryDevice() {
        roller.in();
        assertEquals(1.0, motor.power, 0.0);
        assertEquals(1.0, servo.power, 0.0);
        assertTrue(roller.isRunning());

        roller.out();
        assertEquals(-1.0, motor.power, 0.0);
        assertEquals(-1.0, servo.power, 0.0);

        roller.stop();
        assertEquals(0.0, motor.power, 0.0);
        assertEquals(0.0, servo.power, 0.0);
        assertFalse(roller.isRunning());
    }

    @Test
    public void speedsChangeWhatInAndOutSend() {
        roller.speeds(0.6, -0.3);
        roller.in();
        assertEquals(0.6, motor.power, 0.0);
        roller.out();
        assertEquals(-0.3, servo.power, 0.0);
    }

    @Test
    public void setPowerIsClampedToPlusMinusOne() {
        roller.setPower(2.5);
        assertEquals(1.0, motor.power, 0.0);
        roller.setPower(-7);
        assertEquals(-1.0, motor.power, 0.0);
        assertEquals(-1.0, roller.getPower(), 0.0);
    }

    @Test
    public void addingADeviceStartsItStopped() {
        FakeCRServo third = new FakeCRServo();
        third.power = 0.4;
        roller.add(third);
        assertEquals(0.0, third.power, 0.0);
    }

    @Test
    public void timedRunStopsWhenTheClockPasses() {
        roller.runFor(0.8, 0.5);
        assertTrue(roller.isBusy());
        assertEquals(0.8, motor.power, 0.0);

        clock.advance(0.3);
        roller.update();
        assertTrue("still inside the window", roller.isBusy());
        assertEquals(0.8, motor.power, 0.0);

        clock.advance(0.2);
        roller.update();
        assertFalse(roller.isBusy());
        assertEquals(0.0, motor.power, 0.0);
        assertEquals(0.0, servo.power, 0.0);
    }

    @Test
    public void inForAndOutForUseTheConfiguredSpeeds() {
        roller.speeds(0.5, -0.5);
        roller.inFor(1.0);
        assertEquals(0.5, motor.power, 0.0);
        roller.outFor(1.0);
        assertEquals(-0.5, motor.power, 0.0);
        assertTrue(roller.isBusy());
    }

    @Test
    public void aDirectCommandCancelsATimedRun() {
        roller.runFor(1.0, 5.0);
        roller.stop();
        assertFalse(roller.isBusy());
        clock.advance(10);
        roller.update();               // nothing left to end
        assertEquals(0.0, motor.power, 0.0);
    }

    @Test
    public void updateWithNothingTimedDoesNothing() {
        roller.in();
        clock.advance(100);
        roller.update();
        assertEquals(1.0, motor.power, 0.0);
    }

    @Test
    public void behavesAsAFeeder() {
        Feeder feeder = roller;
        feeder.start();
        feeder.start();                // called every loop during the feed window; must be harmless
        assertEquals(1.0, motor.power, 0.0);
        feeder.stop();
        assertEquals(0.0, motor.power, 0.0);
    }
}
