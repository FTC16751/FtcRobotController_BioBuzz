package org.firstinspires.ftc.teamcode.common.drive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OdometryWatchdogTest {

    private static final String[] THREE = {"pinpoint", "otos", "wheels"};

    private final OdometryWatchdog.Settings settings = new OdometryWatchdog.Settings();   // 0.5 s windows
    private final OdometryWatchdog dog = new OdometryWatchdog(THREE, settings);
    private double t = 0;

    /** Per-source position along its own x axis and heading. */
    private final double[] x = new double[3], y = new double[3], h = new double[3];

    private void feed() { dog.update(t, x, y, h); }

    /** Advance one window: each source moves forward by its own distance (inches) and turns by its own degrees. */
    private void window(double d0, double d1, double d2, double t0, double t1, double t2) {
        double[] d = {d0, d1, d2}, turn = {t0, t1, t2};
        for (int i = 0; i < 3; i++) {
            x[i] += d[i];
            h[i] += turn[i];
        }
        t += settings.windowSec;
        feed();
    }

    private void straight(double a, double b, double c) { window(a, b, c, 0, 0, 0); }

    @Test
    public void agreeingSourcesAreAgreeAndNeverSuspect() {
        feed();
        for (int i = 0; i < 5; i++) straight(10, 10.5, 9.6);
        assertEquals(OdometryWatchdog.Verdict.AGREE, dog.lastVerdict());
        assertEquals(-1, dog.suspect());
        assertFalse(dog.disagreeing());
    }

    @Test
    public void aSourceThatReadsShortThreeWindowsRunningIsTheSuspect() {
        feed();
        straight(10, 6, 10);   // otos 40% short
        assertEquals(OdometryWatchdog.Verdict.OUTLIER, dog.lastVerdict());
        assertEquals(1, dog.lastOutlier());
        assertEquals(-1, dog.suspect());   // one window is not enough
        straight(10, 6, 10);
        straight(10, 6, 10);
        assertEquals(1, dog.suspect());
        assertEquals("SUSPECT: otos", dog.summary());
    }

    @Test
    public void oneGoodWindowClearsTheStreak() {
        feed();
        straight(10, 6, 10);
        straight(10, 6, 10);
        straight(10, 10, 10);
        straight(10, 6, 10);
        assertEquals(-1, dog.suspect());
    }

    @Test
    public void quietWindowsAreNotJudged() {
        feed();
        straight(0.2, 0.1, 0.0);
        assertEquals(OdometryWatchdog.Verdict.IDLE, dog.lastVerdict());
        straight(10, 6, 10);
        straight(0.1, 0.2, 0.1);   // standing still does not clear or add to the streak
        straight(10, 6, 10);
        straight(10, 6, 10);
        assertEquals(1, dog.suspect());
    }

    @Test
    public void aSourceWhoseHeadingIsOffIsBlamedForTheTurn() {
        feed();
        for (int i = 0; i < 3; i++) window(0, 0, 0, 30, 30, 15);   // wheels turned half as much
        assertEquals(2, dog.suspect());
    }

    @Test
    public void sourcesInDifferentFramesStillAgree() {
        // the OTOS reports the same motion along its own y axis, with its heading origin elsewhere
        feed();
        for (int i = 0; i < 4; i++) {
            x[0] += 10; y[1] += 10; x[2] += 10;
            h[1] = 90;
            t += settings.windowSec;
            feed();
        }
        assertEquals(OdometryWatchdog.Verdict.AGREE, dog.lastVerdict());
        assertEquals(40, dog.totalDistance(1), 1e-9);
    }

    @Test
    public void headingWrapDoesNotLookLikeATurn() {
        h[0] = h[1] = h[2] = 179;
        feed();
        for (int i = 0; i < 3; i++) window(0, 0, 0, 4, 4, 4);   // crosses +-180
        assertEquals(OdometryWatchdog.Verdict.IDLE, dog.lastVerdict());
        assertEquals(12, dog.totalTurnDegrees(1), 1e-9);
    }

    @Test
    public void twoSourcesCanOnlyDisagreeNeverBlame() {
        OdometryWatchdog two = new OdometryWatchdog(new String[] {"pinpoint", "otos"}, settings);
        double[] px = {0, 0}, py = {0, 0}, ph = {0, 0};
        two.update(0, px, py, ph);
        for (int i = 1; i <= 3; i++) {
            px[0] += 10; px[1] += 6;
            two.update(i * settings.windowSec, px, py, ph);
        }
        assertEquals(OdometryWatchdog.Verdict.DISAGREE, two.lastVerdict());
        assertTrue(two.disagreeing());
        assertEquals(-1, two.suspect());
    }

    @Test
    public void resetStartsOver() {
        feed();
        for (int i = 0; i < 3; i++) straight(10, 6, 10);
        dog.reset();
        assertEquals(-1, dog.suspect());
        assertEquals(0, dog.totalDistance(0), 0);
    }
}
