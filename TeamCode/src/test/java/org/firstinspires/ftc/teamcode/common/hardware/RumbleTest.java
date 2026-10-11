package org.firstinspires.ftc.teamcode.common.hardware;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RumbleTest {

    private int buzzes = 0;
    private int lastCount = 0;
    private final Rumble rumble = new Rumble(n -> { buzzes++; lastCount = n; }, 3);

    @Test
    public void buzzesOnceOnTheRisingEdgeOnly() {
        rumble.update(false);
        rumble.update(true);
        rumble.update(true);
        rumble.update(true);
        assertEquals(1, buzzes);
        assertEquals(3, lastCount);
    }

    @Test
    public void buzzesAgainAfterTheConditionDropsAndReturns() {
        rumble.update(true);
        rumble.update(false);
        rumble.update(true);
        assertEquals(2, buzzes);
    }

    @Test
    public void aConditionTrueFromTheStartBuzzesOnce() {
        rumble.update(true);
        rumble.update(true);
        assertEquals(1, buzzes);
    }
}
