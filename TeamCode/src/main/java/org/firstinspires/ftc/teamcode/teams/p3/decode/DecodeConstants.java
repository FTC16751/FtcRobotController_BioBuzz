package org.firstinspires.ftc.teamcode.teams.p3.decode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.common.hardware.LedUtil;

/**
 * HOW DECODE OPERATES: speeds, velocities, servo positions and autonomous waypoints. Device names
 * and directions are in DecodeConfig. All numbers are the competition values from the Decode
 * season; velocities are flywheel ticks per second.
 */
public final class DecodeConstants {

    private DecodeConstants() {}

    /** AdvantageScope logging (common/LogUtil). One .wpilog per run on the Control Hub while true. */
    public static final class Logging {
        public static final boolean ENABLED = true;
    }

    public static final class Intake {
        public static final double POWER = 1.0;
    }

    /** The rubber-band belt that feeds the flywheel. */
    public static final class Indexer {
        public static final double POWER = 1.0;          // a shot (LaunchController feeds at this)
        public static final double UNJAM_POWER = 0.25;   // left trigger runs it backwards at this
    }

    /** The stopper servo's two positions (after STOPPER_DIR is applied). */
    public static final class Stopper {
        public static final double STOP  = 0.93;
        public static final double SHOOT = 1.0;
    }

    public static final class Launcher {
        /** The preset velocities the driver picks (D-pad). */
        public static final double CLOSE_TARGET_VELOCITY = 1200;
        public static final double FAR_TARGET_VELOCITY   = 1450;
        /** D-pad up/down change the velocity by this much, capped at MAX_VELOCITY. */
        public static final double NUDGE = 100;
        public static final double MAX_VELOCITY = 2200;

        /** Velocity PIDF for both flywheel motors. */
        public static final double PIDF_P = 300, PIDF_I = 0, PIDF_D = 0, PIDF_F = 16;

        /** Shot sequence (LaunchController): feed when at 97% of target, abort below 80% or after 2 s of spin-up. */
        public static final double READY_FRACTION = 0.97;
        public static final double STALL_FRACTION = 0.80;
        public static final double FEED_TIME_SEC = 1.5;
        public static final double COOLDOWN_SEC = 0.05;
        public static final double SPIN_UP_TIMEOUT_SEC = 2.0;

        /** Distance to the goal (inches) -> flywheel velocity (ticks/sec), used by Launcher.aim(). */
        public static final double[][] FLYWHEEL_TABLE = {
                { 30.0,  950.0*1.10},
                { 40.0,  960.0*1.10},
                { 50.0, 1080.0*1.10},
                { 60.0, 1120.0*1.10},
                { 70.0, 1080.0*1.10},
                { 80.0, 1120.0*1.10},
                { 90.0, 1220.0*1.10},
                {100.0, 1300.0*1.10},
                {110.0, 1340.0*1.10},
                {120.0, 1380.0*1.10},
                {130.0, 1420.0*1.10},
                {140.0, 1460.0*1.10},
                {150.0, 1500.0*1.10},
        };
        /** Velocity to shoot at before the goal has ever been seen (a safe mid-range value). */
        public static final double FLYWHEEL_INITIAL_FALLBACK = 1000.0;
    }

    /** Aim feedback on the status LED (AimLed). */
    public static final class Aim {
        public static final double LED_TOLERANCE_DEG = 4.0;
        public static final double LED_GOAL_RIGHT = LedUtil.Color.YELLOW;
        public static final double LED_GOAL_LEFT  = LedUtil.Color.BLUE;
        /** TeleOp snap-to-goal (right stick button): proportional turn gain and the tolerance. */
        public static final double SNAP_KP = 0.02;
        public static final double SNAP_TOLERANCE_DEG = 1.0;
    }

    public static final class Drive {
        /** arcadeDrive speed in TeleOp. 0.25 is what the Decode TeleOp ran. */
        public static final double TELEOP_SPEED = 0.25;
    }

    /** Autonomous waypoints on the Pinpoint's frame, which resets to (0, 0) at the start. */
    public static final class Waypoints {

        /* Red Alliance Poses */
        // ---- CLOSE TO GOAL START ----//
        public static final Pose2D START_RED_CLOSE = new Pose2D(DistanceUnit.INCH,0, 0, AngleUnit.DEGREES,0);
        public static final Pose2D RED_CLOSE_SHOOTING_POSITION = new Pose2D(DistanceUnit.INCH,-26, 36, AngleUnit.DEGREES,-40);
        public static final Pose2D RED_CLOSE_PARK = new Pose2D(DistanceUnit.INCH,0, 36, AngleUnit.DEGREES,0);


        // ---- FAR FROM GOAL START ----//
        public static final Pose2D RED_FAR_START_POSITION = new Pose2D(DistanceUnit.INCH,0, 0, AngleUnit.DEGREES,0);
        public static final Pose2D RED_FAR_SHOOTING_POSITION = new Pose2D(DistanceUnit.INCH,8.5, 6, AngleUnit.DEGREES,-30);
        public static final Pose2D RED_FAR_PARK_POSITION = new Pose2D(DistanceUnit.INCH,26, 0, AngleUnit.DEGREES,0);


        // * RED SPIKEMARK POSITIONS */
        public static final Pose2D RED_CLOSE_SPIKEMARK1_ALIGN = new Pose2D(DistanceUnit.INCH,-30, 26, AngleUnit.DEGREES,-90);

        public static final Pose2D RED_CLOSE_SPIKEMARK1_COLLECT = new Pose2D(DistanceUnit.INCH,-30,2 , AngleUnit.DEGREES,-90);
        public static final Pose2D RED_CLOSE_SPIKEMARK2_ALIGN = new Pose2D(DistanceUnit.INCH,-54, 26, AngleUnit.DEGREES,-90);
        public static final Pose2D RED_CLOSE_SPIKEMARK2_COLLECT = new Pose2D(DistanceUnit.INCH,-54, -3, AngleUnit.DEGREES,-90);
        public static final Pose2D RED_CLOSE_SPIKEMARK3_ALIGN = new Pose2D(DistanceUnit.INCH,-78, 26, AngleUnit.DEGREES,-90);
        public static final Pose2D RED_CLOSE_SPIKEMARK3_COLLECT = new Pose2D(DistanceUnit.INCH,-78, -3, AngleUnit.DEGREES,-90);


        /** RED FAR **/
        public static final Pose2D RED_FAR_SPIKEMARK3_ALIGN = new Pose2D(DistanceUnit.INCH, 28, -12, AngleUnit.DEGREES, -90);

        public static final Pose2D RED_FAR_SPIKEMARK3_COLLECT = new Pose2D(DistanceUnit.INCH, 28, -45, AngleUnit.DEGREES, -90);
        public static final Pose2D RED_FAR_SPIKEMARK2_ALIGN = new Pose2D(DistanceUnit.INCH, 52, -12, AngleUnit.DEGREES, -90);
        public static final Pose2D RED_FAR_SPIKEMARK2_COLLECT = new Pose2D(DistanceUnit.INCH, 52, -45, AngleUnit.DEGREES, -90);
        public static final Pose2D RED_FAR_SPIKEMARK1_ALIGN = new Pose2D(DistanceUnit.INCH, 76, -12, AngleUnit.DEGREES, -90);
        public static final Pose2D RED_FAR_SPIKEMARK1_COLLECT = new Pose2D(DistanceUnit.INCH, 76, -38, AngleUnit.DEGREES, -90);


        // ALIGN AND OPEN GATE RED //
        public static final Pose2D RED_ALIGN_GATE = new Pose2D(DistanceUnit.INCH,42, 2, AngleUnit.DEGREES,-90);
        public static final Pose2D RED_OPEN_GATE = new Pose2D(DistanceUnit.INCH,42, -2, AngleUnit.DEGREES,-90);


        // * Blue Alliance Poses */
        // ---- CLOSE TO GOAL START ----//
        public static final Pose2D START_BLUE_CLOSE = new Pose2D(DistanceUnit.INCH,0, 0, AngleUnit.DEGREES,0);
        public static final Pose2D BLUE_CLOSE_SHOOTING_POSITION = new Pose2D(DistanceUnit.INCH, -26, -35, AngleUnit.DEGREES, 45);
        public static final Pose2D BLUE_CLOSE_PARK = new Pose2D(DistanceUnit.INCH, 0, -36, AngleUnit.DEGREES, 0);



        // ---- FAR FROM GOAL START ----//
        public static final Pose2D START_BLUE_FAR = new Pose2D(DistanceUnit.INCH,0, 0, AngleUnit.DEGREES,0);
        public static final Pose2D BLUE_FAR_SHOOTING_POSITION = new Pose2D(DistanceUnit.INCH, 8.5, -5, AngleUnit.DEGREES, 22);
        public static final Pose2D BLUE_FAR_PARK_POSITION = new Pose2D(DistanceUnit.INCH, 26, 0, AngleUnit.DEGREES, 0);


        // -- BLUE SPIKE MARK LOCATIONS -- //
        //NEAR SIDE
        public static final Pose2D BLUE_CLOSE_SPIKEMARK1_ALIGN = new Pose2D(DistanceUnit.INCH, -30, -25, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_CLOSE_SPIKEMARK1_COLLECT = new Pose2D(DistanceUnit.INCH, -30, 0, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_CLOSE_SPIKEMARK2_ALIGN = new Pose2D(DistanceUnit.INCH, -54, -25, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_CLOSE_SPIKEMARK2_COLLECT = new Pose2D(DistanceUnit.INCH, -54, 5, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_CLOSE_SPIKEMARK3_ALIGN = new Pose2D(DistanceUnit.INCH, -78, -25, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_CLOSE_SPIKEMARK3_COLLECT = new Pose2D(DistanceUnit.INCH, -78, 5, AngleUnit.DEGREES, 90);

        //FAR SIDE:
        public static final Pose2D BLUE_FAR_SPIKEMARK3_ALIGN = new Pose2D(DistanceUnit.INCH, 28, 14, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_FAR_SPIKEMARK3_COLLECT = new Pose2D(DistanceUnit.INCH, 28, 47, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_FAR_SPIKEMARK2_ALIGN = new Pose2D(DistanceUnit.INCH, 53, 14, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_FAR_SPIKEMARK2_COLLECT = new Pose2D(DistanceUnit.INCH, 53, 45, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_FAR_SPIKEMARK1_ALIGN = new Pose2D(DistanceUnit.INCH, 75, 14, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_FAR_SPIKEMARK1_COLLECT = new Pose2D(DistanceUnit.INCH, 75, 40, AngleUnit.DEGREES, 90);



        // ALIGN AND OPEN GATE BLUE //
        public static final Pose2D BLUE_ALIGN_GATE = new Pose2D(DistanceUnit.INCH, -42, -4, AngleUnit.DEGREES, 90);
        public static final Pose2D BLUE_OPEN_GATE = new Pose2D(DistanceUnit.INCH, -42, 0, AngleUnit.DEGREES, 90);

        // ---- FAR FROM GOAL START ----//
        // blue far shoot
        // x = -8.5 y = 3.7295  heading = 18.3
        //blue align to spikemark 3
        // x = -12.0 y = 10.1 heading = 90
        //blue sm3 collect
        // x = -12.1 y = -25 heading = 90
    }
}
