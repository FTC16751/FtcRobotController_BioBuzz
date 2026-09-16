package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.common.Clock;

/**
 * A gripper: a {@link PresetServo} with OPEN and CLOSED filled in and the words a driver uses.
 * <pre>
 *   claw = new Claw(hardwareMap, "claw", MyConstants.Claw.OPEN, MyConstants.Claw.CLOSED);
 *   if (gamepad1.aWasPressed()) claw.toggle();
 * </pre>
 * Two-servo claw (FIRST's sample idiom): {@code .pair("right_hand", Servo.Direction.REVERSE)}.
 * Everything else a PresetServo can do (nudge, limits, flick, extra presets) still works here.
 *
 * This class is also the worked example of extending a skeleton: 30 lines, no new state.
 */
public class Claw extends PresetServo {

    public static final String OPEN = "OPEN";
    public static final String CLOSED = "CLOSED";

    public Claw(HardwareMap hardwareMap, String deviceName, double openPosition, double closedPosition) {
        super(hardwareMap, deviceName);
        preset(OPEN, openPosition).preset(CLOSED, closedPosition);
    }

    public Claw(Servo servo, double openPosition, double closedPosition) {
        this(servo, openPosition, closedPosition, Clock.SYSTEM);
    }

    public Claw(Servo servo, double openPosition, double closedPosition, Clock clock) {
        super(servo, clock);
        preset(OPEN, openPosition).preset(CLOSED, closedPosition);
    }

    public void open()  { goTo(OPEN); }
    public void close() { goTo(CLOSED); }

    /** Open if closed, otherwise close. Before the first command it opens. */
    public void toggle() {
        if (isOpen()) close(); else open();
    }

    public boolean isOpen()   { return isAt(OPEN); }
    public boolean isClosed() { return isAt(CLOSED); }

    // Keep the fluent setup returning a Claw so `new Claw(...).pair(...)` still reads as a Claw.
    @Override public Claw pair(String deviceName, Servo.Direction direction) { super.pair(deviceName, direction); return this; }
    @Override public Claw pair(Servo servo) { super.pair(servo); return this; }
    @Override public Claw limits(double min, double max) { super.limits(min, max); return this; }
}
