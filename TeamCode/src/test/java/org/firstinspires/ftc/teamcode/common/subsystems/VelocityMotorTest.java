package org.firstinspires.ftc.teamcode.common.subsystems;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.common.Flywheel;
import org.junit.Before;
import org.junit.Test;

/** VelocityMotor commands every motor, reports the slowest one, and is ready at the fraction. */
public class VelocityMotorTest {

    private FakeDcMotorEx left;
    private FakeDcMotorEx right;
    private VelocityMotor flywheel;

    @Before
    public void setUp() {
        left = new FakeDcMotorEx();
        right = new FakeDcMotorEx();
        flywheel = new VelocityMotor(left).add(right);
    }

    @Test
    public void setupPutsEveryMotorInVelocityModeAndFloating() {
        assertEquals(DcMotor.RunMode.RUN_USING_ENCODER, left.mode);
        assertEquals(DcMotor.RunMode.RUN_USING_ENCODER, right.mode);
        assertEquals(DcMotor.ZeroPowerBehavior.FLOAT, left.zeroPowerBehavior);
        assertEquals(DcMotor.ZeroPowerBehavior.FLOAT, right.zeroPowerBehavior);
    }

    @Test
    public void aFollowerMotorCopiesPowerAndDoesNotCountTowardReady() {
        FakeDcMotorEx follower = new FakeDcMotorEx();
        VelocityMotor wheel = new VelocityMotor(left).follower(follower);
        assertEquals(DcMotor.RunMode.RUN_WITHOUT_ENCODER, follower.mode);
        wheel.spinUp(1400);
        left.measuredVelocity = 1400;
        left.power = 0.8;          // what the SDK's velocity loop happens to be sending
        wheel.update();
        assertEquals(0.8, follower.power, 0);
        assertEquals("the follower's 0 velocity is ignored", 1400, wheel.getVelocity(), 0);
        assertTrue(wheel.isReady());
        wheel.stop();
        wheel.update();
        assertEquals(0, follower.power, 0);
    }

    @Test
    public void pidfReachesEveryMotor() {
        flywheel.pidf(300, 0, 0, 10);
        assertArrayEquals(new double[] {300, 0, 0, 10}, left.velocityPidf, 0.0);
        assertArrayEquals(new double[] {300, 0, 0, 10}, right.velocityPidf, 0.0);
    }

    @Test
    public void spinUpCommandsEveryMotor() {
        flywheel.spinUp(1500);
        assertEquals(1500, left.commandedVelocity, 0.0);
        assertEquals(1500, right.commandedVelocity, 0.0);
        assertEquals(1500, flywheel.getTargetVelocity(), 0.0);
        assertTrue(flywheel.isSpinning());
    }

    @Test
    public void reportsTheSlowestMotor() {
        left.measuredVelocity = 1480;
        right.measuredVelocity = 1200;
        assertEquals(1200, flywheel.getVelocity(), 0.0);
    }

    @Test
    public void readyAtTheFractionOfTarget() {
        flywheel.spinUp(1000);
        left.measuredVelocity = 960;
        right.measuredVelocity = 940;
        assertFalse("slowest wheel below 95%", flywheel.isReady());
        right.measuredVelocity = 950;
        assertTrue(flywheel.isReady());
    }

    @Test
    public void readyFractionIsAdjustable() {
        flywheel.readyFraction(0.80).spinUp(1000);
        left.measuredVelocity = 810;
        right.measuredVelocity = 800;
        assertTrue(flywheel.isReady());
    }

    @Test
    public void neverReadyWhenNotAskedToSpin() {
        left.measuredVelocity = 500;
        right.measuredVelocity = 500;
        assertFalse(flywheel.isReady());
    }

    @Test
    public void negativeTargetsWorkTheSameWay() {
        flywheel.spinUp(-1000);
        left.measuredVelocity = -990;
        right.measuredVelocity = -980;
        assertTrue(flywheel.isReady());
    }

    @Test
    public void stopSendsZero() {
        flywheel.spinUp(1500);
        flywheel.stop();
        assertEquals(0, left.commandedVelocity, 0.0);
        assertEquals(0, right.commandedVelocity, 0.0);
        assertFalse(flywheel.isSpinning());
    }

    @Test
    public void behavesAsAFlywheel() {
        Flywheel wheel = flywheel;
        wheel.setVelocity(1200);
        left.measuredVelocity = 1200;
        right.measuredVelocity = 1190;
        assertEquals(1190, wheel.getVelocity(), 0.0);
    }
}
