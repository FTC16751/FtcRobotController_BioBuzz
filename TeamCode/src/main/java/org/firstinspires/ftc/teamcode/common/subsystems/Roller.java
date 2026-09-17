package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Clock;
import org.firstinspires.ftc.teamcode.common.Feeder;

import java.util.ArrayList;
import java.util.List;

/**
 * Anything that spins game pieces in or out: an intake, a feeder, an indexer, a conveyor.
 *
 * One Roller can drive any mix of motors and continuous-rotation servos as a group; they all get
 * the same power. Build it from the device names in your BotConfig:
 * <pre>
 *   intake = new Roller(hardwareMap, "intake", DcMotorSimple.Direction.REVERSE)
 *                 .add("left_intake_servo",  DcMotorSimple.Direction.FORWARD)
 *                 .add("right_intake_servo", DcMotorSimple.Direction.REVERSE);
 * </pre>
 * Beginner commands: {@link #in()}, {@link #out()}, {@link #stop()}, {@link #setPower(double)}.
 * Timed: {@link #runFor(double, double)} runs then stops on its own, as long as the robot class
 * calls {@link #update()} every loop.
 *
 * A Roller is also a {@link Feeder}, so it can be handed straight to LaunchController as the
 * thing that pushes a game piece into the flywheel.
 */
public class Roller implements Feeder {

    private final List<DcMotorSimple> devices = new ArrayList<>();
    private final HardwareMap hardwareMap;   // null when built from devices directly
    private final Clock clock;

    private double inPower  = 1.0;
    private double outPower = -1.0;
    private double power    = 0.0;
    private double runUntil = -1;            // clock seconds when a timed run ends; <0 = not timed

    /** The normal way: first device by name from the robot configuration. */
    public Roller(HardwareMap hardwareMap, String deviceName, DcMotorSimple.Direction direction) {
        this.hardwareMap = hardwareMap;
        this.clock = Clock.SYSTEM;
        add(deviceName, direction);
    }

    /** Build from a device you already have (the robot class, or a test). */
    public Roller(DcMotorSimple device) {
        this(device, Clock.SYSTEM);
    }

    /** For tests: a clock that can be advanced by hand. */
    public Roller(DcMotorSimple device, Clock clock) {
        this.hardwareMap = null;
        this.clock = clock;
        add(device);
    }

    // ---------------------------------------------------------------- setup (fluent)

    /** Add another motor or CR servo that spins with this one. */
    public Roller add(String deviceName, DcMotorSimple.Direction direction) {
        if (hardwareMap == null) {
            throw new IllegalStateException("This Roller was built from a device; use add(DcMotorSimple)");
        }
        DcMotorSimple device = hardwareMap.get(DcMotorSimple.class, deviceName);
        device.setDirection(direction);
        return add(device);
    }

    /** Add a device you already have. */
    public Roller add(DcMotorSimple device) {
        devices.add(device);
        device.setPower(0);   // also wakes up a servo controller, as goBILDA's sample does
        return this;
    }

    /** Motors stop hard instead of coasting when set to 0 (goBILDA's StarterBot intake does this). Servos are unaffected. */
    public Roller brake() {
        for (DcMotorSimple d : devices) {
            if (d instanceof DcMotor) ((DcMotor) d).setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
        return this;
    }

    /** What in() and out() send. Defaults are full speed each way. */
    public Roller speeds(double inPower, double outPower) {
        this.inPower = inPower;
        this.outPower = outPower;
        return this;
    }

    // ---------------------------------------------------------------- beginner commands

    /** Pull game pieces in at the in speed. */
    public void in() { setPower(inPower); }

    /** Push game pieces out at the out speed. */
    public void out() { setPower(outPower); }

    /** All devices off. */
    public void stop() { setPower(0); }

    /** Direct power, -1 to 1. Positive is "in". Cancels any timed run. */
    public void setPower(double p) {
        power = Math.max(-1.0, Math.min(1.0, p));
        for (DcMotorSimple d : devices) d.setPower(power);
        runUntil = -1;
    }

    public double getPower() { return power; }

    public boolean isRunning() { return power != 0; }

    // ---------------------------------------------------------------- timed run

    /** Run at this power for this many seconds, then stop. Needs update() each loop. */
    public void runFor(double p, double seconds) {
        setPower(p);
        runUntil = clock.seconds() + seconds;
    }

    /** in() for a fixed time, then stop. */
    public void inFor(double seconds) { runFor(inPower, seconds); }

    /** out() for a fixed time, then stop. */
    public void outFor(double seconds) { runFor(outPower, seconds); }

    /** True while a timed run is in progress. */
    public boolean isBusy() { return runUntil >= 0; }

    /** Call every loop. Ends a timed run when its time is up. */
    public void update() {
        if (runUntil >= 0 && clock.seconds() >= runUntil) stop();
    }

    // ---------------------------------------------------------------- Feeder (LaunchController)

    /** Feeder contract: begin feeding. Safe to call every loop. */
    @Override public void start() { in(); }

    // stop() above is the Feeder stop.

    // ---------------------------------------------------------------- telemetry

    public void addTelemetry(Telemetry telemetry, String label) {
        telemetry.addData(label, "power %.2f%s", power, isBusy() ? " (timed)" : "");
    }
}
