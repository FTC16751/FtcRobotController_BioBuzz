package org.firstinspires.ftc.robotcore.external.navigation;

/** Sim stand-in for the FTC SDK's Position. */
public class Position {
    public DistanceUnit unit;
    public double x, y, z;
    public long acquisitionTime;
    public Position() { this(DistanceUnit.METER, 0, 0, 0, 0); }
    public Position(DistanceUnit unit, double x, double y, double z, long acquisitionTime) {
        this.unit = unit; this.x = x; this.y = y; this.z = z; this.acquisitionTime = acquisitionTime;
    }
}
