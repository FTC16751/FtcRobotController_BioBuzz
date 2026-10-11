package com.qualcomm.robotcore.hardware;

/** Sim stand-in for the FTC SDK's PwmControl: just enough for Turret.setPwmRange. */
public interface PwmControl {
    class PwmRange {
        public final double usPulseLower, usPulseUpper, usFrame;
        public PwmRange(double usPulseLower, double usPulseUpper) { this(usPulseLower, usPulseUpper, 20000); }
        public PwmRange(double usPulseLower, double usPulseUpper, double usFrame) {
            this.usPulseLower = usPulseLower; this.usPulseUpper = usPulseUpper; this.usFrame = usFrame;
        }
    }
    void setPwmRange(PwmRange range);
    PwmRange getPwmRange();
}
