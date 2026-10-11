package org.firstinspires.ftc.robotcore.external.navigation;

/** Sim stand-in for the FTC SDK's Pose3D. */
public class Pose3D {
    private final Position position;
    private final SimAngles orientation;
    public Pose3D(Position position, YawPitchRollAngles orientation) { this.position = position; this.orientation = new SimAngles(); }
    public Position getPosition() { return position; }
    public SimAngles getOrientation() { return orientation; }
}
