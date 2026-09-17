package org.firstinspires.ftc.teamcode.common;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.RobotLog;

import Ori.Coval.Logging.Logger.KoalaLog;

/**
 * Data logging for AdvantageScope, through Koala-Log (https://github.com/Koala-Log/Koala-Log).
 * Every OpMode run that starts this writes one .wpilog file on the Control Hub, named by the time
 * and the OpMode ("2026-09-20_14-30-00_Test2027__Drive_Square__Pedro_.wpilog"). Pull it with adb and
 * open it in AdvantageScope to graph any value against time or replay the robot's path on the field
 * (doc/LOGGING_ADVANTAGESCOPE.md).
 *
 * How a robot class uses it:
 *   - constructor: {@link #start(HardwareMap)}
 *   - every update(): {@link #log} what matters ("Drive/X", the motor powers, the tag in view ...)
 *   - stopAll(): {@link #stop()}, which flushes and closes the file
 *
 * Why a wrapper and not KoalaLog directly: a logger must never stop a robot. Koala-Log throws if it
 * cannot create the file, and it needs an FTC Dashboard instance for the "post" option; both are
 * contained here, so an OpMode without logging is exactly as safe as before. Everything Koala-Log
 * does after start() runs on its own thread, so a log call from the OpMode loop costs a queue put.
 *
 * Names: "Group/Name", the way AdvantageScope builds its tree. Values are written with post=false:
 * nothing goes to the FTC Dashboard page, so the Dashboard is a dependency, not something a driver
 * has to open. Angles are logged in degrees except inside a pose, which AdvantageScope reads in
 * radians. Distances are in inches, the frame Pedro and the Pinpoint use.
 */
public final class LogUtil {

    private static boolean running = false;
    private static VoltageSensor battery = null;

    private LogUtil() {}

    /**
     * Open a new log file for this OpMode run and start the writer thread. Safe to call when the
     * file cannot be made (then nothing is logged and the reason goes to the robot log), and safe
     * to call twice.
     */
    public static void start(HardwareMap hardwareMap) {
        if (running) return;
        try {
            KoalaLog.setup(hardwareMap);      // file named by time and the active OpMode
            KoalaLog.start();
            running = true;
        } catch (RuntimeException e) {
            RobotLog.ww("LogUtil", e, "Koala-Log could not start; this run is not logged");
            running = false;
        }
        try {
            battery = hardwareMap.voltageSensor.iterator().next();
        } catch (RuntimeException e) {
            battery = null;
        }
    }

    /** Flush and close the file. Call from the OpMode's stop path; a run without it loses the tail. */
    public static void stop() {
        if (!running) return;
        running = false;
        try {
            KoalaLog.stop();
        } catch (RuntimeException e) {
            RobotLog.ww("LogUtil", e, "Koala-Log did not stop cleanly; the log may be incomplete");
        }
    }

    public static boolean isRunning() {
        return running;
    }

    // --- Values. Each returns its argument so a call can sit inline: motor.setPower(LogUtil.log("Drive/LF", p)). ---

    public static double log(String name, double value) {
        if (running) KoalaLog.log(name, value, false);
        return value;
    }

    public static boolean log(String name, boolean value) {
        if (running) KoalaLog.log(name, value, false);
        return value;
    }

    public static long log(String name, long value) {
        if (running) KoalaLog.log(name, value, false);
        return value;
    }

    public static String log(String name, String value) {
        if (running && value != null) KoalaLog.log(name, value, false);
        return value;
    }

    public static <E extends Enum<E>> E log(String name, E value) {
        if (running && value != null) KoalaLog.log(name, value.name(), false);
        return value;
    }

    /**
     * A robot position as an AdvantageScope Pose2d, for the 2D field view: inches, heading in
     * degrees counter-clockwise (the Pinpoint and Pedro frame). Stored with the heading in radians.
     */
    public static void logPose(String name, double xInches, double yInches, double headingDegrees) {
        if (running) KoalaLog.logPose2d(name, xInches, yInches, Math.toRadians(headingDegrees), false);
    }

    /** The main battery, under "Robot/BatteryVolts". Cheap; the hub bulk read already has it. */
    public static void logBattery() {
        if (running && battery != null) KoalaLog.log("Robot/BatteryVolts", battery.getVoltage(), false);
    }
}
