package org.firstinspires.ftc.teamcode.common.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.common.AimTarget;
import org.firstinspires.ftc.teamcode.common.LaunchController;
import org.junit.Before;
import org.junit.Test;

/** Launcher: wheel on request only, one non-blocking shot per shoot(), table aiming, manual feed locked out during a shot. */
public class LauncherTest {

    private FakeDcMotorEx wheelA, wheelB;
    private FakeCRServo feederL, feederR;
    private FakeClock clock;
    private Launcher launcher;

    private static final double[][] TABLE = { {30, 1200}, {60, 1300}, {90, 1400} };

    @Before
    public void setUp() {
        wheelA = new FakeDcMotorEx();
        wheelB = new FakeDcMotorEx();
        feederL = new FakeCRServo();
        feederR = new FakeCRServo();
        clock = new FakeClock();
        VelocityMotor wheel = new VelocityMotor(wheelA).add(wheelB);
        Roller feeder = new Roller(feederL, clock).add(feederR);
        launcher = new Launcher(wheel, feeder,
                new LaunchController.Settings().feedTimeSec(0.45).cooldownSec(0).stallFraction(0).spinUpTimeoutSec(2.0).readyFraction(0.95),
                null, clock)
                .table(TABLE, 1250);
    }

    private void wheelReports(double v) { wheelA.measuredVelocity = v; wheelB.measuredVelocity = v; }

    @Test
    public void nothingMovesWhenBuilt() {
        assertEquals(0, wheelA.commandedVelocity, 0);
        assertEquals(0, feederL.power, 0);
        assertFalse(launcher.isSpinning());
        assertEquals("the table's fallback is the velocity the first shot would use", 1250, launcher.getTargetVelocity(), 0);
    }

    @Test
    public void spinUpCommandsBothWheelsAndSpinDownStopsThem() {
        launcher.spinUp(1400);
        assertEquals(1400, wheelA.commandedVelocity, 0);
        assertEquals(1400, wheelB.commandedVelocity, 0);
        assertTrue(launcher.isSpinning());
        launcher.nudge(100);
        assertEquals(1500, wheelA.commandedVelocity, 0);
        launcher.spinDown();
        assertEquals(0, wheelA.commandedVelocity, 0);
        assertEquals("spinDown keeps the number for the next spinUp", 1500, launcher.getTargetVelocity(), 0);
    }

    @Test
    public void aShotSpinsUpFeedsForTheFeedTimeThenReportsDone() {
        launcher.spinUp(1400);
        launcher.shoot();
        launcher.update();                                   // IDLE -> SPIN_UP
        assertEquals(LaunchController.State.SPIN_UP, launcher.getState());
        assertEquals("no feeding before the wheel is ready", 0, feederL.power, 0);
        wheelReports(1400 * 0.96);
        launcher.update();                                   // ready -> FEEDING
        assertEquals(LaunchController.State.FEEDING, launcher.getState());
        launcher.update();
        assertEquals(1.0, feederL.power, 0);
        assertEquals(1.0, feederR.power, 0);
        assertFalse(launcher.shotDone());
        clock.advance(0.5);
        launcher.update();                                   // feed time over
        assertTrue(launcher.shotDone());
        assertEquals(0, feederL.power, 0);
        assertEquals(1, launcher.getShotsFired());
        assertEquals("keepSpinning: the wheel stays at speed for the next shot", 1400, wheelA.commandedVelocity, 0);
        launcher.update();
        assertFalse("shotDone is true for one loop only", launcher.shotDone());
    }

    @Test
    public void shootWithFalseDoesNothingAndHoldingTrueFiresAgain() {
        launcher.spinUp(1400);
        launcher.shoot(false);
        launcher.update();
        assertFalse(launcher.isBusy());
        launcher.shoot(true);
        launcher.update();
        assertTrue(launcher.isBusy());
    }

    @Test
    public void shootStartsTheWheelEvenIfItWasOff() {
        launcher.shoot();
        launcher.update();
        assertEquals("the fallback velocity", 1250, wheelA.commandedVelocity, 0);
    }

    @Test
    public void shootWithNoVelocityEverSetDoesNotFeedIntoAStoppedWheel() {
        Launcher bare = new Launcher(new VelocityMotor(wheelA), new Roller(feederL, clock),
                new LaunchController.Settings(), null, clock);   // no table(...), no spinUp()
        bare.shoot();
        for (int i = 0; i < 3; i++) bare.update();           // unguarded: IDLE -> SPIN_UP -> FEEDING -> feeder on
        assertFalse(bare.isBusy());
        assertEquals(0, feederL.power, 0);
    }

    @Test
    public void aimTakesTheVelocityFromTheTableAndRemembersIt() {
        AimTarget goalAt60 = new AimTarget() {
            @Override public boolean isTargetVisible() { return true; }
            @Override public double getDistanceToTagInches() { return 60; }
            @Override public double getTargetAngleX() { return 0; }
        };
        assertEquals(1300, launcher.aim(goalAt60), 0);
        assertEquals(1300, wheelA.commandedVelocity, 0);
        AimTarget nothing = new AimTarget() {
            @Override public boolean isTargetVisible() { return false; }
            @Override public double getDistanceToTagInches() { return 0; }
            @Override public double getTargetAngleX() { return 0; }
        };
        assertEquals("goal lost: last good value", 1300, launcher.aim(nothing), 0);
        assertEquals("LAST KNOWN", launcher.getAimSource());
    }

    @Test
    public void manualFeedWorksWhenIdleAndIsIgnoredDuringAShot() {
        launcher.feed();
        assertEquals(1.0, feederL.power, 0);
        launcher.feedBack();
        assertEquals(-1.0, feederL.power, 0);
        launcher.feedStop();
        assertEquals(0, feederL.power, 0);

        launcher.spinUp(1400);
        launcher.shoot();
        launcher.update();                                   // SPIN_UP, feeder must stay off
        launcher.feed();
        assertEquals("a shot in progress owns the feeder", 0, feederL.power, 0);
    }

    @Test
    public void stopTurnsEverythingOff() {
        launcher.spinUp(1400);
        launcher.shoot();
        launcher.update();
        launcher.stop();
        assertEquals(0, wheelA.commandedVelocity, 0);
        assertEquals(0, feederL.power, 0);
        assertFalse(launcher.isBusy());
        assertFalse(launcher.isSpinning());
    }
}
