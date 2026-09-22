package org.firstinspires.ftc.teamcode.common;

/**
 * Source of time in seconds for anything in Common that waits: launch.LaunchController, vision.TagApproach,
 * and the timed actions in the skeleton subsystems (Roller.runFor, PresetServo.flick).
 *
 * The robot uses {@link #SYSTEM}. Unit tests supply a fake clock they advance by hand, so feed
 * windows, hold times and timeouts can be tested without sleeping.
 */
public interface Clock {
    double seconds();

    /** Real time, for use on the robot. */
    Clock SYSTEM = new Clock() {
        @Override public double seconds() { return System.nanoTime() / 1e9; }
    };
}
