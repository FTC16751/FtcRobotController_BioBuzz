package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Flywheel;

import java.util.ArrayList;
import java.util.List;

/**
 * One or two motors held at a speed by their encoders: a flywheel, a shooter wheel, anything
 * that must spin at a known rate rather than a known power.
 * <pre>
 *   flywheel = new VelocityMotor(hardwareMap, "launcher", DcMotorSimple.Direction.FORWARD)
 *                  .add("launcher2", DcMotorSimple.Direction.REVERSE)
 *                  .pidf(300, 0, 0, 10);
 *   flywheel.spinUp(MyConstants.Launcher.CLOSE_VELOCITY);
 *   if (flywheel.isReady()) ...
 * </pre>
 * Velocity is in encoder ticks per second, the SDK's unit. A VelocityMotor is a {@link Flywheel},
 * so with a {@link Roller} as the feeder a whole launcher is
 * {@code new LaunchController(flywheel, feeder, settings, telemetry)}.
 *
 * {@link #isReady()} is for the driver's ready light; LaunchController keeps its own ready check.
 */
public class VelocityMotor implements Flywheel {

    private final List<DcMotorEx> motors = new ArrayList<>();
    private final HardwareMap hardwareMap;   // null when built from a device

    private double target = 0;
    private double readyFraction = 0.95;

    /** The normal way: first motor by name from the robot configuration. */
    public VelocityMotor(HardwareMap hardwareMap, String deviceName, DcMotorSimple.Direction direction) {
        this.hardwareMap = hardwareMap;
        add(deviceName, direction);
    }

    /** Build from a motor you already have (the robot class, or a test). */
    public VelocityMotor(DcMotorEx motor) {
        this.hardwareMap = null;
        add(motor);
    }

    // ---------------------------------------------------------------- setup (fluent)

    /** Add a second motor that spins with the first (the other side of a two-wheel flywheel). */
    public VelocityMotor add(String deviceName, DcMotorSimple.Direction direction) {
        if (hardwareMap == null) {
            throw new IllegalStateException("This VelocityMotor was built from a device; use add(DcMotorEx)");
        }
        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, deviceName);
        motor.setDirection(direction);
        return add(motor);
    }

    public VelocityMotor add(DcMotorEx motor) {
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);   // let a flywheel coast down
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motors.add(motor);
        return this;
    }

    /** Velocity PIDF for every motor. Every launcher in this repo uses about (300, 0, 0, 10). */
    public VelocityMotor pidf(double p, double i, double d, double f) {
        for (DcMotorEx m : motors) m.setVelocityPIDFCoefficients(p, i, d, f);
        return this;
    }

    /** isReady() is true at this fraction of the target. Default 0.95. */
    public VelocityMotor readyFraction(double fraction) {
        this.readyFraction = fraction;
        return this;
    }

    // ---------------------------------------------------------------- beginner commands

    /** Spin at this velocity (ticks per second) and keep it there. */
    public void spinUp(double velocity) { setVelocity(velocity); }

    /** Flywheel contract; same as spinUp. 0 stops. */
    @Override public void setVelocity(double velocity) {
        target = velocity;
        for (DcMotorEx m : motors) m.setVelocity(velocity);
    }

    /** Stop driving; the wheel coasts down. */
    public void stop() { setVelocity(0); }

    /** The slowest motor's velocity, so a two-wheel flywheel is "ready" only when both are. */
    @Override public double getVelocity() {
        double slowest = 0;
        boolean first = true;
        for (DcMotorEx m : motors) {
            double v = m.getVelocity();
            if (first || Math.abs(v) < Math.abs(slowest)) { slowest = v; first = false; }
        }
        return slowest;
    }

    public double getTargetVelocity() { return target; }

    public boolean isSpinning() { return target != 0; }

    /** True once the wheel is within the ready fraction of the target. Never true at target 0. */
    public boolean isReady() {
        if (target == 0) return false;
        return Math.abs(getVelocity()) >= Math.abs(target) * readyFraction;
    }

    // ---------------------------------------------------------------- telemetry

    public void addTelemetry(Telemetry telemetry, String label) {
        telemetry.addData(label, "%.0f / %.0f%s", getVelocity(), target, isReady() ? "  READY" : "");
    }
}
