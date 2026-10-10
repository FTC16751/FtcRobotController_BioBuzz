package org.firstinspires.ftc.teamcode.common.drive;

/**
 * Heading arithmetic for the IMU turn in DriveUtil (the robots with no Pinpoint). Pure and
 * unit-tested (HeadingMathTest). The proportional turn is the one in FIRST's
 * RobotAutoDriveByGyro_Linear sample (BSD-3): power proportional to heading error, clipped.
 * Degrees, counter-clockwise positive, like the IMU and the Pinpoint.
 */
public final class HeadingMath {

    private HeadingMath() {}

    /** Any angle to the equivalent one in (-180, 180]. */
    public static double wrapDegrees(double degrees) {
        double d = degrees % 360.0;
        if (d > 180.0)   d -= 360.0;
        if (d <= -180.0) d += 360.0;
        return d;
    }

    /** Shortest signed turn from current to target, in (-180, 180]. Positive = turn counter-clockwise. */
    public static double errorDegrees(double targetDegrees, double currentDegrees) {
        return wrapDegrees(targetDegrees - currentDegrees);
    }

    /**
     * Turn power for this heading error: 0 inside the threshold, otherwise kP * error pushed up to
     * at least minPower (to beat friction) and down to at most maxPower. Same sign as the error.
     */
    public static double turnPower(double errorDegrees, double kP, double minPower, double maxPower, double thresholdDegrees) {
        if (Math.abs(errorDegrees) <= thresholdDegrees) return 0.0;
        double magnitude = Math.min(maxPower, Math.max(minPower, Math.abs(kP * errorDegrees)));
        return Math.copySign(magnitude, errorDegrees);
    }
}
