package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.TouchSensor;

/** A touch sensor for laptop tests; the test flips {@link #pressed}. */
public class FakeTouchSensor implements TouchSensor {
    public boolean pressed = false;

    @Override public boolean isPressed() { return pressed; }
    @Override public double getValue() { return pressed ? 1.0 : 0.0; }

    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "FakeTouchSensor"; }
    @Override public String getConnectionInfo() { return "fake"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() { }
    @Override public void close() { }
}
