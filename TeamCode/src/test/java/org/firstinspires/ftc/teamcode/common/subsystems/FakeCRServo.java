package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ServoController;

/** A continuous-rotation servo for laptop tests. Also stands in for any DcMotorSimple. */
public class FakeCRServo implements CRServo {
    public double power = 0;
    public Direction direction = Direction.FORWARD;

    @Override public void setPower(double p) { power = p; }
    @Override public double getPower() { return power; }
    @Override public void setDirection(Direction d) { direction = d; }
    @Override public Direction getDirection() { return direction; }

    @Override public ServoController getController() { return null; }
    @Override public int getPortNumber() { return 0; }
    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "FakeCRServo"; }
    @Override public String getConnectionInfo() { return "fake"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() { }
    @Override public void close() { }
}
