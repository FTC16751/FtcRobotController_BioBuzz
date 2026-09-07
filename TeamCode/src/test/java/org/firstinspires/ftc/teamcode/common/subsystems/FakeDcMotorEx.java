package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * A DcMotorEx for laptop tests. Records what the subsystem commanded; the test sets what the
 * "encoder" reads. isBusy() mimics RUN_TO_POSITION: true until the position is within tolerance.
 */
public class FakeDcMotorEx implements DcMotorEx {
    public double power = 0;
    public Direction direction = Direction.FORWARD;
    public RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    public ZeroPowerBehavior zeroPowerBehavior = ZeroPowerBehavior.UNKNOWN;
    public int targetPosition = 0;
    public int tolerance = 5;
    public double commandedVelocity = 0;
    public double[] velocityPidf = null;
    public int resetCount = 0;

    // What the test wants the hardware to report.
    public int currentPosition = 0;
    public double measuredVelocity = 0;

    @Override public void setPower(double p) { power = p; }
    @Override public double getPower() { return power; }
    @Override public void setDirection(Direction d) { direction = d; }
    @Override public Direction getDirection() { return direction; }

    @Override public void setMode(RunMode m) {
        mode = m;
        if (m == RunMode.STOP_AND_RESET_ENCODER) { currentPosition = 0; resetCount++; }
    }
    @Override public RunMode getMode() { return mode; }
    @Override public void setTargetPosition(int t) { targetPosition = t; }
    @Override public int getTargetPosition() { return targetPosition; }
    @Override public boolean isBusy() {
        return mode == RunMode.RUN_TO_POSITION && Math.abs(currentPosition - targetPosition) > tolerance;
    }
    @Override public int getCurrentPosition() { return currentPosition; }
    @Override public void setZeroPowerBehavior(ZeroPowerBehavior z) { zeroPowerBehavior = z; }
    @Override public ZeroPowerBehavior getZeroPowerBehavior() { return zeroPowerBehavior; }
    @Override public void setPowerFloat() { zeroPowerBehavior = ZeroPowerBehavior.FLOAT; power = 0; }
    @Override public boolean getPowerFloat() { return zeroPowerBehavior == ZeroPowerBehavior.FLOAT && power == 0; }
    @Override public void setTargetPositionTolerance(int t) { tolerance = t; }
    @Override public int getTargetPositionTolerance() { return tolerance; }

    @Override public void setVelocity(double v) { commandedVelocity = v; }
    @Override public void setVelocity(double v, AngleUnit unit) { commandedVelocity = v; }
    @Override public double getVelocity() { return measuredVelocity; }
    @Override public double getVelocity(AngleUnit unit) { return measuredVelocity; }
    @Override public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {
        velocityPidf = new double[] { p, i, d, f };
    }
    @Override public void setPositionPIDFCoefficients(double p) { }
    @Override public void setPIDCoefficients(RunMode m, PIDCoefficients c) { }
    @Override public void setPIDFCoefficients(RunMode m, PIDFCoefficients c) { }
    @Override public PIDCoefficients getPIDCoefficients(RunMode m) { return null; }
    @Override public PIDFCoefficients getPIDFCoefficients(RunMode m) { return null; }

    // Everything below is required by the interface and unused by the skeletons.
    @Override public void setMotorEnable() { }
    @Override public void setMotorDisable() { }
    @Override public boolean isMotorEnabled() { return true; }
    @Override public double getCurrent(CurrentUnit unit) { return 0; }
    @Override public double getCurrentAlert(CurrentUnit unit) { return 0; }
    @Override public void setCurrentAlert(double current, CurrentUnit unit) { }
    @Override public boolean isOverCurrent() { return false; }
    @Override public MotorConfigurationType getMotorType() { return null; }
    @Override public void setMotorType(MotorConfigurationType t) { }
    @Override public DcMotorController getController() { return null; }
    @Override public int getPortNumber() { return 0; }
    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "FakeDcMotorEx"; }
    @Override public String getConnectionInfo() { return "fake"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() { }
    @Override public void close() { }
}
