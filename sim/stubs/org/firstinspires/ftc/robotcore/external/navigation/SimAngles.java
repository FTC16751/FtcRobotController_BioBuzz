package org.firstinspires.ftc.robotcore.external.navigation;

/** Sim stand-in: the real SDK's YawPitchRollAngles also has no-argument getters (degrees); the sim's does not. */
public class SimAngles extends YawPitchRollAngles {
    public SimAngles() { super(AngleUnit.DEGREES, 0, 0, 0, 0); }
    public double getYaw() { return getYaw(AngleUnit.DEGREES); }
    public double getPitch() { return getPitch(AngleUnit.DEGREES); }
    public double getRoll() { return getRoll(AngleUnit.DEGREES); }
}
