package org.firstinspires.ftc.teamcode.common.subsystems;

import org.firstinspires.ftc.teamcode.common.Clock;

/** A clock the test advances by hand. */
public class FakeClock implements Clock {
    public double now = 0;

    @Override public double seconds() { return now; }

    public void advance(double seconds) { now += seconds; }
}
