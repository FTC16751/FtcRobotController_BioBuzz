package org.firstinspires.ftc.teamcode.common.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ClusterMathTest {

    private static final double TOL = 1e-6;

    /** Tags a camera would report for a cluster centred at (cx, cz) whose strip runs along unit (ux, uz); only the listed members. */
    private static ClusterMath.Result seen(double cx, double cz, double ux, double uz, int... members) {
        double[] r = new double[members.length], f = new double[members.length];
        for (int i = 0; i < members.length; i++) {
            r[i] = cx + ClusterMath.OFFSET_IN[members[i]] * ux;
            f[i] = cz + ClusterMath.OFFSET_IN[members[i]] * uz;
        }
        return ClusterMath.solve(members, r, f);
    }

    @Test
    public void allFourSquareOnGiveTheCentre() {
        ClusterMath.Result c = seen(3.0, 48.0, 1, 0, 0, 1, 2, 3);
        assertEquals(4, c.members);
        assertEquals(3.0, c.rightIn, TOL);
        assertEquals(48.0, c.forwardIn, TOL);
        assertEquals(0.0, c.squareUpDeg, TOL);
    }

    @Test
    public void anyTwoOrThreeMembersFindTheSameCentre() {
        int[][] subsets = {{0, 1}, {2, 3}, {1, 2}, {0, 3}, {0, 1, 2}, {1, 2, 3}, {0, 2, 3}};
        for (int[] s : subsets) {
            ClusterMath.Result c = seen(-4.0, 40.0, 1, 0, s);
            assertEquals(-4.0, c.rightIn, TOL);
            assertEquals(40.0, c.forwardIn, TOL);
        }
    }

    @Test
    public void aStripAngledToTheRobotGivesCentreAndSquareUp() {
        // strip runs 25 deg toward the far side: from tag 0 to tag 3 it goes right and forward
        double a = Math.toRadians(25);
        ClusterMath.Result c = seen(2.0, 50.0, Math.cos(a), Math.sin(a), 0, 1, 2, 3);
        assertEquals(2.0, c.rightIn, TOL);
        assertEquals(50.0, c.forwardIn, TOL);
        assertEquals(25.0, c.squareUpDeg, TOL);
        ClusterMath.Result two = seen(2.0, 50.0, Math.cos(a), Math.sin(a), 1, 2);
        assertEquals(2.0, two.rightIn, TOL);
        assertEquals(50.0, two.forwardIn, TOL);
    }

    @Test
    public void seenFromTheOtherEndTheCentreIsTheSameAndSquareUpStaysUnder90() {
        // robot on the far side of the Hive: tag 0 appears on its left, so the strip runs toward -right
        ClusterMath.Result c = seen(1.0, 45.0, -1, 0, 0, 1, 2, 3);
        assertEquals(1.0, c.rightIn, TOL);
        assertEquals(45.0, c.forwardIn, TOL);
        assertEquals(0.0, c.squareUpDeg, TOL);
        double a = Math.toRadians(200);   // same strip, 20 deg off, seen from the other end
        ClusterMath.Result d = seen(1.0, 45.0, Math.cos(a), Math.sin(a), 0, 1, 2, 3);
        assertEquals(1.0, d.rightIn, TOL);
        assertEquals(20.0, d.squareUpDeg, TOL);
    }

    @Test
    public void oneTagIsReportedAsIsAndFlaggedUncorrected() {
        ClusterMath.Result c = ClusterMath.solve(new int[]{3}, new double[]{9.5}, new double[]{48.0});
        assertEquals(1, c.members);
        assertEquals(9.5, c.rightIn, TOL);
        assertEquals(48.0, c.forwardIn, TOL);
    }

    @Test
    public void noValidMemberGivesNull() {
        assertNull(ClusterMath.solve(new int[0], new double[0], new double[0]));
        assertNull(ClusterMath.solve(new int[]{4, -1}, new double[]{0, 0}, new double[]{0, 0}));
    }

    @Test
    public void aRepeatedMemberCountsOnce() {
        ClusterMath.Result c = ClusterMath.solve(new int[]{1, 1}, new double[]{0, 5}, new double[]{40, 40});
        assertEquals(1, c.members);
        assertEquals(0.0, c.rightIn, TOL);
    }

    @Test
    public void smallNoiseMovesTheCentreByAboutTheNoise() {
        ClusterMath.Result c = ClusterMath.solve(new int[]{0, 1, 2, 3},
                new double[]{-6.5 + 0.3, -2.75 - 0.3, 2.75 + 0.2, 6.5 - 0.2}, new double[]{48, 48.4, 47.7, 48});
        assertEquals(0.0, c.rightIn, 0.5);
        assertEquals(48.0, c.forwardIn, 0.5);
    }
}
