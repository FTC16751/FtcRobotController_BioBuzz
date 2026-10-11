package com.qualcomm.hardware.limelightvision;

import com.qualcomm.robotcore.hardware.HardwareDevice;

/** Sim stand-in. VisionUtil only builds one if the sim registers a device named like the config's
 *  limelight, which it does not, so the robot reports "no target" (the same as a robot with no Limelight). */
public class Limelight3A implements HardwareDevice {
    public boolean pipelineSwitch(int index) { return true; }
    public void start() {}
    public void stop() {}
    public LLResult getLatestResult() { return null; }
    public boolean updateRobotOrientation(double yawDegrees) { return true; }
    @Override public void resetDeviceConfigurationForOpMode() {}
    @Override public void close() {}
}
