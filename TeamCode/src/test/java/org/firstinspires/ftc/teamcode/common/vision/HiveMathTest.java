package org.firstinspires.ftc.teamcode.common.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HiveMathTest {

    @Test
    public void heightFromRangeAndElevation() {
        // camera 10 in up, pitched 20 up, tag 48 in away seen 15 deg above the camera axis: sin(35)
        assertEquals(10 + 48 * Math.sin(Math.toRadians(35)), HiveMath.tagHeightIn(10, 20, 48, 15), 1e-9);
        // level camera, tag straight ahead on the axis: same height as the camera
        assertEquals(10.0, HiveMath.tagHeightIn(10, 0, 60, 0), 1e-9);
        // a tag below the camera axis reads lower
        assertTrue(HiveMath.tagHeightIn(10, 0, 60, -10) < 10.0);
    }

    @Test
    public void upAndDownAreSplitAtThePivotHeight() {
        double pivot = HiveMath.PIVOT_HEIGHT_IN;
        assertEquals(43.95, pivot, 0.0);
        assertEquals(pivot, HiveMath.upThresholdIn(), 0.0);
        assertEquals(HiveMath.State.DOWN, HiveMath.stateOf(pivot - 10, 30));
        assertEquals(HiveMath.State.UP, HiveMath.stateOf(pivot + 10, 30));
        assertEquals(HiveMath.State.DOWN, HiveMath.stateOf(pivot, 30));        // exactly on it is DOWN
        assertEquals(HiveMath.State.UP, HiveMath.stateOf(pivot + 0.1, 30));
    }

    @Test
    public void tiltOfEitherSignCountsAsSettled() {
        assertTrue(HiveMath.isSettled(30));
        assertTrue(HiveMath.isSettled(-30));    // which sign is "rest" is a practice-Hive check; the gate takes both
        assertTrue(HiveMath.isSettled(30 + HiveMath.SETTLED_TOLERANCE_DEG));
        assertFalse(HiveMath.isSettled(30 + HiveMath.SETTLED_TOLERANCE_DEG + 0.1));
    }

    @Test
    public void aTippingHiveIsMovingWhateverTheHeight() {
        assertEquals(HiveMath.State.MOVING, HiveMath.stateOf(HiveMath.PIVOT_HEIGHT_IN + 10, 0));    // passing through flat
        assertEquals(HiveMath.State.MOVING, HiveMath.stateOf(HiveMath.PIVOT_HEIGHT_IN - 10, 15));
        assertEquals(HiveMath.State.MOVING, HiveMath.stateOf(HiveMath.PIVOT_HEIGHT_IN - 10, 60));
    }

    @Test
    public void memberTagsMapToTheirClusterBaseId() {
        assertEquals(HiveMath.RED_SCORING, HiveMath.clusterOf(30));
        assertEquals(HiveMath.RED_SCORING, HiveMath.clusterOf(33));
        assertEquals(HiveMath.RED_AUDIENCE, HiveMath.clusterOf(34));
        assertEquals(HiveMath.BLUE_AUDIENCE, HiveMath.clusterOf(41));
        assertEquals(HiveMath.BLUE_SCORING, HiveMath.clusterOf(42));
        assertEquals(HiveMath.BLUE_SCORING, HiveMath.clusterOf(45));
        assertEquals(-1, HiveMath.clusterOf(29));   // DECODE tags and anything else
        assertEquals(-1, HiveMath.clusterOf(46));
        assertEquals(-1, HiveMath.clusterOf(24));
    }
}
