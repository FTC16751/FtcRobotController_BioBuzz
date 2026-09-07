package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One or more encoder motors that go to named positions: a lift, a linear slide, an arm, a hang,
 * a motor-driven turret. The motors share one target and one power (a two-motor lift is two
 * {@code add} calls).
 * <pre>
 *   lift = new PresetMotor(hardwareMap, "lift", DcMotorSimple.Direction.FORWARD)
 *              .add("lift2", DcMotorSimple.Direction.REVERSE)
 *              .preset("DOWN", 0).preset("LOW", 800).preset("HIGH", 2200)
 *              .limits(0, 2300)
 *              .homeSwitch("lift_home");
 *   lift.goTo("HIGH");                       // non-blocking; poll lift.isAtTarget()
 *   lift.manual(-gamepad2.left_stick_y);     // stick drives it; releasing the stick holds position
 * </pre>
 * Positions are encoder ticks from wherever the mechanism was when this object was built (the
 * encoder is zeroed then), so build it with the mechanism at rest. A home switch re-zeroes it
 * whenever it is pressed, through {@link #update()}.
 *
 * Moves use the motor controller's own RUN_TO_POSITION loop; nothing here blocks. Soft limits
 * clamp every target and stop manual power from pushing past either end.
 */
public class PresetMotor {

    private enum Mode { OFF, TO_POSITION, MANUAL }

    private final List<DcMotorEx> motors = new ArrayList<>();
    private final HardwareMap hardwareMap;   // null when built from a device
    private final Map<String, Integer> presets = new LinkedHashMap<>();

    private int minTicks = Integer.MIN_VALUE;
    private int maxTicks = Integer.MAX_VALUE;
    private int tolerance = 10;
    private double movePower = 1.0;
    private TouchSensor homeSwitch;
    private boolean homeWasPressed;

    private Mode mode = Mode.OFF;
    private int target = 0;
    private double manualPower = 0;
    private String presetName = "none";

    /** The normal way: first motor by name from the robot configuration. Zeroes the encoder. */
    public PresetMotor(HardwareMap hardwareMap, String deviceName, DcMotorSimple.Direction direction) {
        this.hardwareMap = hardwareMap;
        add(deviceName, direction);
    }

    /** Build from a motor you already have (the robot class, or a test). Zeroes the encoder. */
    public PresetMotor(DcMotorEx motor) {
        this.hardwareMap = null;
        add(motor);
    }

    // ---------------------------------------------------------------- setup (fluent)

    /** Add a motor that moves with the first (the other side of a two-motor lift). */
    public PresetMotor add(String deviceName, DcMotorSimple.Direction direction) {
        if (hardwareMap == null) {
            throw new IllegalStateException("This PresetMotor was built from a device; use add(DcMotorEx)");
        }
        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, deviceName);
        motor.setDirection(direction);
        return add(motor);
    }

    public PresetMotor add(DcMotorEx motor) {
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motors.add(motor);
        return this;
    }

    /** Name a position, in encoder ticks from the zero position. */
    public PresetMotor preset(String name, int ticks) {
        presets.put(name, ticks);
        return this;
    }

    /** Soft limits: no target outside this range, and manual power cannot push past either end. */
    public PresetMotor limits(int minTicks, int maxTicks) {
        this.minTicks = Math.min(minTicks, maxTicks);
        this.maxTicks = Math.max(minTicks, maxTicks);
        return this;
    }

    /** How close counts as "at target", in ticks. Default 10. */
    public PresetMotor tolerance(int ticks) {
        this.tolerance = ticks;
        return this;
    }

    /** Power used for goTo moves, 0 to 1. Default 1.0. */
    public PresetMotor power(double power) {
        this.movePower = Math.abs(power);
        return this;
    }

    /** A touch sensor at the zero end. When it is pressed the encoder is re-zeroed (see update()). */
    public PresetMotor homeSwitch(String deviceName) {
        if (hardwareMap == null) {
            throw new IllegalStateException("This PresetMotor was built from a device; use homeSwitch(TouchSensor)");
        }
        return homeSwitch(hardwareMap.get(TouchSensor.class, deviceName));
    }

    public PresetMotor homeSwitch(TouchSensor sensor) {
        this.homeSwitch = sensor;
        this.homeWasPressed = sensor.isPressed();
        return this;
    }

    // ---------------------------------------------------------------- beginner commands

    /** Start moving to a named position. Non-blocking: poll isAtTarget(). Unknown names throw. */
    public void goTo(String name) {
        Integer ticks = presets.get(name);
        if (ticks == null) {
            throw new IllegalArgumentException("No preset '" + name + "'. Known presets: " + presets.keySet());
        }
        goToTicks(ticks);
        presetName = name;
    }

    /** Start moving to a raw tick count, clamped to the limits. */
    public void goToTicks(int ticks) {
        target = clamp(ticks);
        presetName = "manual";
        for (DcMotorEx m : motors) {
            m.setTargetPositionTolerance(tolerance);
            m.setTargetPosition(target);
            m.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            m.setPower(movePower);
        }
        mode = Mode.TO_POSITION;
    }

    /** Move this many ticks from the current target (or from where it is, if not on a target). */
    public void nudge(int ticks) {
        int from = mode == Mode.TO_POSITION ? target : getPosition();
        goToTicks(from + ticks);
    }

    /**
     * Drive with a stick: power -1 to 1. Power that would push past a limit is turned off.
     * Power 0 holds the current position (so a lift does not sag when the stick is released).
     */
    public void manual(double power) {
        if (power == 0) {
            if (mode != Mode.TO_POSITION) goToTicks(getPosition());
            return;
        }
        int pos = getPosition();
        if ((power < 0 && pos <= minTicks) || (power > 0 && pos >= maxTicks)) {
            goToTicks(pos);   // at the end of travel: hold there instead of pushing
            return;
        }
        manualPower = Math.max(-1.0, Math.min(1.0, power));
        presetName = "manual";
        for (DcMotorEx m : motors) {
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            m.setPower(manualPower);
        }
        mode = Mode.MANUAL;
    }

    /** Cut power. The mechanism is free to sag; use manual(0) or goTo to hold instead. */
    public void stop() {
        for (DcMotorEx m : motors) m.setPower(0);
        mode = Mode.OFF;
    }

    /** True when a goTo has arrived (within tolerance). False while moving or in manual. */
    public boolean isAtTarget() {
        return mode == Mode.TO_POSITION && Math.abs(getPosition() - target) <= tolerance;
    }

    /** True while a goTo is still moving. */
    public boolean isBusy() {
        return mode == Mode.TO_POSITION && !isAtTarget();
    }

    /** Encoder ticks of the first motor. */
    public int getPosition() { return motors.get(0).getCurrentPosition(); }

    public int getTarget() { return target; }

    public String getPresetName() { return presetName; }

    public boolean isAt(String name) { return presetName.equals(name) && isAtTarget(); }

    /** True while the home switch is pressed (false if there is no switch). */
    public boolean isAtHome() { return homeSwitch != null && homeSwitch.isPressed(); }

    /** Make the current position the new zero. Re-issues the current move in the new frame. */
    public void zeroHere() {
        for (DcMotorEx m : motors) {
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        if (mode == Mode.TO_POSITION) {
            String keep = presetName;
            goToTicks(target);
            presetName = keep;
        } else if (mode == Mode.MANUAL) {
            manual(manualPower);
        }
    }

    /** Call every loop. When the home switch is first pressed, re-zero and stop pushing into it. */
    public void update() {
        if (homeSwitch == null) return;
        boolean pressed = homeSwitch.isPressed();
        if (pressed && !homeWasPressed) {
            zeroHere();
            if (mode == Mode.TO_POSITION && target < 0) {
                String keep = presetName;
                goToTicks(0);
                presetName = keep;
            } else if (mode == Mode.MANUAL && manualPower < 0) {
                goToTicks(0);
            }
        }
        homeWasPressed = pressed;
    }

    // ---------------------------------------------------------------- telemetry

    public void addTelemetry(Telemetry telemetry, String label) {
        telemetry.addData(label, "%s  pos %d  target %d%s%s", presetName, getPosition(), target,
                isBusy() ? "  moving" : "", isAtHome() ? "  HOME" : "");
    }

    // ---------------------------------------------------------------- internals

    private int clamp(int ticks) {
        return Math.max(minTicks, Math.min(maxTicks, ticks));
    }
}
