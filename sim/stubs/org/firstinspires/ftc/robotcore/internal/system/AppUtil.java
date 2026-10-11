package org.firstinspires.ftc.robotcore.internal.system;

/**
 * Sim stand-in for the FTC SDK's AppUtil. Koala-Log calls it to name the log file after the active OpMode.
 * Throwing a RuntimeException here makes LogUtil.start() take its normal "Koala-Log could not start; this
 * run is not logged" path, so the OpMode runs without a log file instead of dying on a missing class.
 */
public class AppUtil {
    public static AppUtil getInstance() {
        throw new RuntimeException("logging is off in the simulator (no Android app to write a log file)");
    }
    public android.app.Activity getRootActivity() { return null; }
}
