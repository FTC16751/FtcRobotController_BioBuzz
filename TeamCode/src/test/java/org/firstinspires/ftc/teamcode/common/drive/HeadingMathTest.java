package org.firstinspires.ftc.teamcode.common.drive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeadingMathTest {

    private static final double EPS = 1e-9;

    @Test
    public void wrapKeepsAnglesInHalfOpenRange() {
        assertEquals(180.0, HeadingMath.wrapDegrees(180), EPS);
        assertEquals(180.0, HeadingMath.wrapDegrees(-180), EPS);
        assertEquals(-170.0, HeadingMath.wrapDegrees(190), EPS);
        assertEquals(170.0, HeadingMath.wrapDegrees(-190), EPS);
        assertEquals(0.0, HeadingMath.wrapDegrees(720), EPS);
    }

    @Test
    public void errorTakesTheShortWayAcrossTheSeam() {
        assertEquals(20.0, HeadingMath.errorDegrees(-170, 170), EPS);    // 170 -> -170 is 20 CCW
        assertEquals(-20.0, HeadingMath.errorDegrees(170, -170), EPS);
        assertEquals(90.0, HeadingMath.errorDegrees(90, 0), EPS);
    }

    @Test
    public void insideThresholdIsZero() {
        assertEquals(0.0, HeadingMath.turnPower(1.0, 0.02, 0.06, 0.5, 1.0), EPS);
        assertEquals(0.0, HeadingMath.turnPower(-0.5, 0.02, 0.06, 0.5, 1.0), EPS);
    }

    @Test
    public void proportionalBetweenTheLimitsAndSignedLikeTheError() {
        assertEquals(0.4, HeadingMath.turnPower(20, 0.02, 0.06, 0.5, 1.0), EPS);
        assertEquals(-0.4, HeadingMath.turnPower(-20, 0.02, 0.06, 0.5, 1.0), EPS);
    }

    @Test
    public void clippedToMaxAndLiftedToMin() {
        assertEquals(0.5, HeadingMath.turnPower(90, 0.02, 0.06, 0.5, 1.0), EPS);
        assertEquals(-0.5, HeadingMath.turnPower(-90, 0.02, 0.06, 0.5, 1.0), EPS);
        assertEquals(0.06, HeadingMath.turnPower(2, 0.02, 0.06, 0.5, 1.0), EPS);   // 0.04 would stall
    }
}
