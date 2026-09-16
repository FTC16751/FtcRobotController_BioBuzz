package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Clock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A positional servo that moves between named positions: a wrist, a hood, a stopper, a diverter,
 * a flipper, a drone-launcher trigger. (A gripper is a {@link Claw}, which is one of these with
 * OPEN and CLOSED filled in.)
 *
 * Build it from the device name in your BotConfig and the positions in your Constants:
 * <pre>
 *   wrist = new PresetServo(hardwareMap, "wrist")
 *               .preset("UP",   MyConstants.Wrist.UP)
 *               .preset("DOWN", MyConstants.Wrist.DOWN)
 *               .limits(0.1, 0.9);
 *   wrist.goTo("UP");
 * </pre>
 * A second servo on the same joint can be ganged with {@link #pair}; REVERSE makes it mirror the
 * first, which is FIRST's two-servo claw idiom.
 *
 * {@link #flick(String, double)} goes to a preset, waits, and comes back on its own, as long as
 * the robot class calls {@link #update()} every loop. That is the whole of a launch flipper.
 *
 * Nothing moves until you command it; {@link #startAt(String)} is the opt-in for init.
 */
public class PresetServo {

    private final List<Servo> servos = new ArrayList<>();
    private final HardwareMap hardwareMap;   // null when built from a device
    private final Clock clock;
    private final Map<String, Double> presets = new LinkedHashMap<>();

    private double min = 0.0;
    private double max = 1.0;
    private double position = Double.NaN;    // last commanded; NaN until the first command
    private String presetName = "none";

    private double returnPosition;           // flick: where to go back to
    private String returnName;
    private double flickUntil = -1;          // clock seconds; <0 = no flick in progress

    /** The normal way: by device name from the robot configuration. */
    public PresetServo(HardwareMap hardwareMap, String deviceName) {
        this.hardwareMap = hardwareMap;
        this.clock = Clock.SYSTEM;
        servos.add(hardwareMap.get(Servo.class, deviceName));
    }

    /** Build from a servo you already have. */
    public PresetServo(Servo servo) {
        this(servo, Clock.SYSTEM);
    }

    /** For tests: a clock that can be advanced by hand. */
    public PresetServo(Servo servo, Clock clock) {
        this.hardwareMap = null;
        this.clock = clock;
        servos.add(servo);
    }

    // ---------------------------------------------------------------- setup (fluent)

    /** Name a position. Positions are 0 to 1, whatever the servo's range is. */
    public PresetServo preset(String name, double servoPosition) {
        presets.put(name, servoPosition);
        return this;
    }

    /** Never command outside this range, whatever a preset or nudge asks for. */
    public PresetServo limits(double min, double max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        return this;
    }

    /** Gang a second servo on the same joint. REVERSE makes it mirror the first (a two-servo claw). */
    public PresetServo pair(String deviceName, Servo.Direction direction) {
        if (hardwareMap == null) {
            throw new IllegalStateException("This PresetServo was built from a device; use pair(Servo)");
        }
        Servo second = hardwareMap.get(Servo.class, deviceName);
        second.setDirection(direction);
        return pair(second);
    }

    /** Gang a servo you already have (set its direction yourself). */
    public PresetServo pair(Servo servo) {
        servos.add(servo);
        return this;
    }

    /** Move to this preset now. The explicit opt-in for moving during init. */
    public PresetServo startAt(String name) {
        goTo(name);
        return this;
    }

    // ---------------------------------------------------------------- beginner commands

    /** Go to a named position. An unknown name throws and lists the ones that exist. */
    public void goTo(String name) {
        Double p = presets.get(name);
        if (p == null) {
            throw new IllegalArgumentException("No preset '" + name + "'. Known presets: " + presets.keySet());
        }
        command(p, name);
    }

    /** Raw position, clamped to the limits. Telemetry shows it as "manual". */
    public void setPosition(double servoPosition) {
        command(servoPosition, "manual");
    }

    /** Move a little from where it is; hold a button and call this each loop to creep. */
    public void nudge(double delta) {
        double from = Double.isNaN(position) ? (min + max) / 2 : position;
        setPosition(from + delta);
    }

    /** Last commanded position, or NaN if nothing has been commanded yet. */
    public double getPosition() { return position; }

    /** Name of the preset last commanded, "manual" after setPosition/nudge, "none" before anything. */
    public String getPresetName() { return presetName; }

    public boolean isAt(String name) { return presetName.equals(name); }

    // ---------------------------------------------------------------- flick

    /** Go to a preset, hold for this many seconds, then return to where it was. Needs update(). */
    public void flick(String name, double holdSeconds) {
        returnPosition = Double.isNaN(position) ? (min + max) / 2 : position;
        returnName = presetName;
        goTo(name);
        flickUntil = clock.seconds() + holdSeconds;
    }

    /** True while a flick is out and has not returned yet. */
    public boolean isBusy() { return flickUntil >= 0; }

    /** Call every loop. Brings a flick back when its hold time is up. */
    public void update() {
        if (flickUntil >= 0 && clock.seconds() >= flickUntil) {
            flickUntil = -1;
            command(returnPosition, returnName);
        }
    }

    /** Cancel a flick in progress and stay where it is. A servo has no power to cut. */
    public void stop() { flickUntil = -1; }

    // ---------------------------------------------------------------- telemetry

    public void addTelemetry(Telemetry telemetry, String label) {
        telemetry.addData(label, "%s %.3f%s", presetName, position, isBusy() ? " (flick)" : "");
    }

    // ---------------------------------------------------------------- internals

    private void command(double servoPosition, String name) {
        position = Math.max(min, Math.min(max, servoPosition));
        presetName = name;
        for (Servo s : servos) s.setPosition(position);
        flickUntil = -1;   // any new command replaces a flick in progress
    }
}
