package com.qualcomm.robotcore.hardware;

/** Sim stand-in for the FTC SDK's ServoImplEx: a plain simulated servo that accepts a PWM range. */
public class ServoImplEx extends ServoImpl implements PwmControl {
    private PwmRange range = new PwmRange(600, 2400);
    @Override public void setPwmRange(PwmRange range) { this.range = range; }
    @Override public PwmRange getPwmRange() { return range; }
}
