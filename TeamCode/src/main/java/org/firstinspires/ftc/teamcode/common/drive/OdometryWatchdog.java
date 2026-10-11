package org.firstinspires.ftc.teamcode.common.drive;

import java.util.Arrays;

/**
 * Compares two or three position estimates of the same robot (Pinpoint, OTOS, wheel encoders +
 * IMU) and says when one of them is the odd one out. It only watches; it never changes how the
 * robot drives. Pure and unit-tested (OdometryWatchdogTest).
 *
 * <b>What it compares.</b> Every window (0.5 s by default) each source reports how far it moved
 * (the straight line between the window's start and end pose) and how much it turned. Those two
 * numbers do not depend on where a source puts its origin or which way its axes point, so the
 * sources need no frame alignment, and an OTOS offset that is wrong shows up as a distance
 * mismatch during turns. Windows where nothing moved are skipped.
 *
 * <b>Who is blamed.</b> With three sources, if two agree and one does not, the one is the outlier;
 * a source that is the outlier several windows in a row is the suspect. With two sources, a
 * mismatch cannot say who is wrong, so it is only "disagreeing". The wheel-encoder source slips
 * on mecanum strafes; expect it to be blamed first.
 *
 * Positions in inches, headings in degrees (counter-clockwise positive), any frame per source.
 */
public class OdometryWatchdog {

    public static class Settings {
        public double windowSec = 0.5;
        /** A window counts as movement if some source went this far (inches)... */
        public double minMoveInches = 2.0;
        /** ...or turned this much (degrees). Quieter windows are not judged. */
        public double minTurnDeg = 5.0;
        /** Distances agree when they differ by no more than the larger of this (inches) and distRelTol of the biggest. */
        public double distAbsTolInches = 1.0;
        public double distRelTol = 0.15;
        /** Turns agree when they differ by no more than the larger of this (degrees) and 5% of the biggest. */
        public double headingTolDeg = 3.0;
        /** Windows in a row before a source is called the suspect (or the sources "disagreeing"). */
        public int alarmWindows = 3;
    }

    public enum Verdict { IDLE, AGREE, OUTLIER, DISAGREE }

    private static final int ALL_AGREE = -1, NO_CONSENSUS = -2;

    private final String[] names;
    private final int n;
    private final Settings s;

    private boolean started = false;
    private double windowStart;
    private final double[] startX, startY, startH;
    private final double[] cumDist, cumHeading;
    private final int[] outlierStreak;
    private int disagreeStreak = 0;
    private Verdict last = Verdict.IDLE;
    private int lastOutlier = -1;

    public OdometryWatchdog(String[] names, Settings settings) {
        if (names.length < 2 || names.length > 3) throw new IllegalArgumentException("two or three sources");
        this.names = names.clone();
        this.n = names.length;
        this.s = settings;
        startX = new double[n]; startY = new double[n]; startH = new double[n];
        cumDist = new double[n]; cumHeading = new double[n];
        outlierStreak = new int[n];
    }

    /** Forget everything, e.g. when the poses are reset. The next update() starts a new window. */
    public void reset() {
        started = false;
        Arrays.fill(cumDist, 0); Arrays.fill(cumHeading, 0); Arrays.fill(outlierStreak, 0);
        disagreeStreak = 0; last = Verdict.IDLE; lastOutlier = -1;
    }

    /** Call every loop with each source's current pose, in the order of the names. */
    public void update(double nowSec, double[] x, double[] y, double[] headingDeg) {
        if (!started) {
            capture(nowSec, x, y, headingDeg);
            started = true;
            return;
        }
        if (nowSec - windowStart < s.windowSec) return;

        double[] dist = new double[n], turn = new double[n];
        double maxDist = 0, maxTurn = 0;
        for (int i = 0; i < n; i++) {
            dist[i] = Math.hypot(x[i] - startX[i], y[i] - startY[i]);
            turn[i] = HeadingMath.errorDegrees(headingDeg[i], startH[i]);
            cumDist[i] += dist[i];
            cumHeading[i] += turn[i];
            maxDist = Math.max(maxDist, dist[i]);
            maxTurn = Math.max(maxTurn, Math.abs(turn[i]));
        }
        capture(nowSec, x, y, headingDeg);

        if (maxDist < s.minMoveInches && maxTurn < s.minTurnDeg) {
            last = Verdict.IDLE;
            lastOutlier = -1;
            return;
        }
        int d = judge(dist, s.distAbsTolInches, s.distRelTol);
        int h = judge(turn, s.headingTolDeg, 0.05);
        if (d == ALL_AGREE && h == ALL_AGREE) {
            last = Verdict.AGREE; lastOutlier = -1;
            Arrays.fill(outlierStreak, 0); disagreeStreak = 0;
        } else if (d >= 0 && (h == ALL_AGREE || h == d)) {
            blame(d);
        } else if (h >= 0 && d == ALL_AGREE) {
            blame(h);
        } else {
            last = Verdict.DISAGREE; lastOutlier = -1;
            Arrays.fill(outlierStreak, 0); disagreeStreak++;
        }
    }

    private void blame(int who) {
        last = Verdict.OUTLIER; lastOutlier = who; disagreeStreak = 0;
        for (int i = 0; i < n; i++) outlierStreak[i] = (i == who) ? outlierStreak[i] + 1 : 0;
    }

    private void capture(double now, double[] x, double[] y, double[] h) {
        windowStart = now;
        for (int i = 0; i < n; i++) { startX[i] = x[i]; startY[i] = y[i]; startH[i] = h[i]; }
    }

    /** ALL_AGREE, the index of the one value that disagrees with two that agree, or NO_CONSENSUS. */
    private int judge(double[] v, double abs, double rel) {
        double max = 0;
        for (double value : v) max = Math.max(max, Math.abs(value));
        double tol = Math.max(abs, rel * max);
        if (n == 2) return Math.abs(v[0] - v[1]) <= tol ? ALL_AGREE : NO_CONSENSUS;
        boolean ab = Math.abs(v[0] - v[1]) <= tol, ac = Math.abs(v[0] - v[2]) <= tol, bc = Math.abs(v[1] - v[2]) <= tol;
        int pairs = (ab ? 1 : 0) + (ac ? 1 : 0) + (bc ? 1 : 0);
        if (pairs == 3) return ALL_AGREE;
        if (pairs == 1) return ab ? 2 : ac ? 1 : 0;
        return NO_CONSENSUS;
    }

    // ---------------------------------------------------------------- results

    public Verdict lastVerdict() { return last; }

    /** Index of the odd one out in the last judged window, or -1. */
    public int lastOutlier() { return lastOutlier; }

    /** Index of a source that has been the odd one out alarmWindows windows in a row, or -1. */
    public int suspect() {
        for (int i = 0; i < n; i++) if (outlierStreak[i] >= s.alarmWindows) return i;
        return -1;
    }

    /** True when the sources have disagreed with no one to blame alarmWindows windows in a row. */
    public boolean disagreeing() { return disagreeStreak >= s.alarmWindows; }

    public String name(int i) { return names[i]; }
    public int sources() { return n; }

    /** Total distance and total turn this source has reported since reset(), for end-of-run comparison. */
    public double totalDistance(int i) { return cumDist[i]; }
    public double totalTurnDegrees(int i) { return cumHeading[i]; }

    /** One line for the Driver Station. */
    public String summary() {
        if (suspect() >= 0) return "SUSPECT: " + names[suspect()];
        if (disagreeing()) return "DISAGREE: no one to blame";
        if (last == Verdict.OUTLIER) return "odd one out: " + names[lastOutlier];
        return last.name();
    }
}
