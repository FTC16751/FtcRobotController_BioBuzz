package org.firstinspires.ftc.teamcode.teams.testteam2027;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.common.LedUtil;

/**
 * HOW test2027bot OPERATES: speeds, timings, waypoints, and the tag it drives to. These change
 * when the game changes or the drivers change their minds. What the robot physically IS lives in
 * Test2027BotConfig.
 *
 * Keep every number an OpMode needs in here, named, so the OpModes read as plain sentences and a
 * student can retune the robot without hunting through them.
 */
public final class Test2027Constants {

    private Test2027Constants() {}

    /** TeleOp driving. */
    public static final class Drive {
        public static final double NORMAL_SPEED   = 0.6;
        public static final double SLOW_SPEED     = 0.25;
        public static final double STICK_DEADBAND = 0.05;
        /** Speeds the beginner auto commands use when no speed is given (driveForward(24) etc.). */
        // 0.6 since 2026-09-08 for the Pedro-or-driveTo comparison (test plan K7/K8): both squares run
        // under this same cap. The first comparison ran Pedro at 1.0 and driveTo at 0.4 and was not
        // fair. Also the beginner commands' default speed. Was 0.4.
        public static final double AUTO_DRIVE_SPEED = 0.6;
        public static final double AUTO_TURN_SPEED  = 0.3;
    }

    /** The AprilTag approach test. Change TAG_ID to whatever tag is taped to the wall today. */
    public static final class TagTest {
        public static final int    TAG_ID            = 20;   // blue goal. 24 = red goal, 21/22/23 = motif. The pipeline follows this.
        /**
         * Where to stop, measured along the camera's view. For the goal tags this must stay where the
         * camera can still see the tag: on the Skyline chassis the tag sits 31 in above the camera and
         * drops out of view inside about 50 in. 60 keeps it visible. A tag at camera height (a motif
         * tag, or a printed tag taped to a wall) can use 12.
         */
        public static final double STANDOFF_INCHES   = 60.0;
        public static final double HOLD_SECONDS      = 0.5;
    }

    /**
     * AdvantageScope logging (common/LogUtil, doc/LOGGING_ADVANTAGESCOPE.md). One .wpilog per run on
     * the Control Hub while true. Turn off if the hub's storage fills or a run must be as lean as
     * possible; the files are small (well under 1 MB a minute) but nothing deletes them for you.
     */
    public static final class Logging {
        public static final boolean ENABLED = true;
    }

    /**
     * The launcher (common/subsystems/Launcher): the Skyline launcher's numbers, since that chassis is
     * the testbed. Velocities are flywheel ticks per second.
     */
    public static final class Launcher {
        /** Velocity PIDF for the flywheel motors; every launcher in this repo uses about this. */
        public static final double PIDF_P = 300, PIDF_I = 0, PIDF_D = 0, PIDF_F = 10;
        /** The driver's presets. CLOSE is where the TeleOp starts (hard rule 0: demo-safe). */
        public static final double CLOSE_VELOCITY = 1400;
        public static final double FAR_VELOCITY   = 1680;
        /** D-pad up/down change the velocity by this much. */
        public static final double NUDGE = 100;
        /** A shot feeds this long. Skyline's auto used 0.45 s; its TeleOp 2.5 s. Chosen 2026-09-16. */
        public static final double FEED_TIME_SEC = 0.45;
        /** The wheel counts as ready at this fraction of the target (1350 of 1400 on Skyline). */
        public static final double READY_FRACTION = 0.96;
        /** Give up on a shot if the wheel is not ready after this long. */
        public static final double SPIN_UP_TIMEOUT_SEC = 2.0;
        /** Distance to the goal (inches) -> flywheel velocity, for aim(). From Skyline, DECODE goal. */
        public static final double[][] FLYWHEEL_TABLE = {
                { 30.0, 1200.0*1.045},
                { 40.0, 1200.0*1.045},
                { 50.0, 1230.0*1.045},
                { 60.0, 1260.0*1.05},
                { 70.0, 1285.0*1.05},
                { 80.0, 1340.0*1.045},
                { 90.0, 1420.0*1.04},
                {100.0, 1460.0*1.04},
                {110.0, 1480.0*1.04},
                {120.0, 1560.0*1.04},
                {130.0, 1640.0*1.04},
                {140.0, 1720.0*1.04},
                {150.0, 1760.0},
        };
        /** Velocity to shoot at before the goal has ever been seen (the close-range table value). */
        public static final double FLYWHEEL_INITIAL_FALLBACK = 1200.0*1.045;
    }

    /** The aiming LED (common/AimLed): green when lined up on the goal tag, else which way to turn. */
    public static final class Aim {
        public static final double LED_TOLERANCE_DEG = 2.0;
        public static final double LED_GOAL_RIGHT = LedUtil.Color.ORANGE;
        public static final double LED_GOAL_LEFT  = LedUtil.Color.BLUE;
    }

    /** Autonomous driving. Power for waypoint moves is Drive.AUTO_DRIVE_SPEED. */
    public static final class Auto {
        /** How long startDriveTo must sit inside tolerance before it counts as arrived. */
        public static final double HOLD_SEC = 0.25;
    }

    /**
     * Waypoints for the drive-a-square auto, relative to where the robot starts (0, 0, heading 0).
     * Pinpoint frame: X forward from the start, Y to the left, heading counter-clockwise positive.
     */
    public static final class Waypoints {
        public static final double SIDE_IN = 24.0;
        public static final Pose2D START    = pose(0,       0,       0);
        public static final Pose2D CORNER_1 = pose(SIDE_IN, 0,       0);
        public static final Pose2D CORNER_2 = pose(SIDE_IN, SIDE_IN, 0);
        public static final Pose2D CORNER_3 = pose(0,       SIDE_IN, 0);
        public static final Pose2D FINISH   = pose(0,       0,       0);    // back at start, same heading, so a chassis corner lands on the tape corner (was a quarter turn until 2026-09-08)

        private static Pose2D pose(double xIn, double yIn, double headingDeg) {
            return new Pose2D(DistanceUnit.INCH, xIn, yIn, AngleUnit.DEGREES, headingDeg);
        }
    }
}
