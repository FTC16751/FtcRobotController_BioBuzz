package org.firstinspires.ftc.teamcode.common.hardware;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.function.IntConsumer;

/**
 * One gamepad buzz that fires once when a condition turns true, not every loop it stays true.
 * Idea from FIRST's ConceptGamepadRumble sample (BSD-3). Feedback only: it never changes what the
 * robot does. One Rumble per condition, since each remembers its own last state:
 * <pre>
 *   private final Rumble readyBuzz = new Rumble(gamepad1, 1);
 *   ...
 *   readyBuzz.update(robot.launcher.isReady());   // every loop
 * </pre>
 * The SDK truncates a running rumble when a new one starts, so keep the conditions that share a
 * gamepad from becoming true at the same moment.
 */
public class Rumble {

    private final IntConsumer blips;
    private final int count;
    private boolean last = false;

    public Rumble(Gamepad gamepad, int blipCount) {
        this(gamepad::rumbleBlips, blipCount);
    }

    /** For tests: anything that takes a blip count. */
    public Rumble(IntConsumer blips, int blipCount) {
        this.blips = blips;
        this.count = blipCount;
    }

    /** Call every loop with the condition. Buzzes on the loop it changes from false to true. */
    public void update(boolean condition) {
        if (condition && !last) blips.accept(count);
        last = condition;
    }
}
