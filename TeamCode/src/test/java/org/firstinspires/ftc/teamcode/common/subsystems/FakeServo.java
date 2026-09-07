package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/** A positional Servo for laptop tests. Records the last commanded position and direction. */
public class FakeServo implements Servo {
    public double position = Double.NaN;   // NaN until something commands it
    public Direction direction = Direction.FORWARD;
    public int commandCount = 0;

    @Override public void setPosition(double p) { position = p; commandCount++; }
    @Override public double getPosition() { return position; }
    @Override public void setDirection(Direction d) { direction = d; }
    @Override public Direction getDirection() { return direction; }
    @Override public void scaleRange(double min, double max) { }

    @Override public ServoController getController() { return null; }
    @Override public int getPortNumber() { return 0; }
    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "FakeServo"; }
    @Override public String getConnectionInfo() { return "fake"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() { }
    @Override public void close() { }
}
