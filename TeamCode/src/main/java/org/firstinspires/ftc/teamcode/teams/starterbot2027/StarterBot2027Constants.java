package org.firstinspires.ftc.teamcode.teams.starterbot2027;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * HOW the two BIOBUZZ StarterBots OPERATE: speeds and launcher numbers, shared by both robots
 * because they are the same goBILDA kit. Every number here is from goBILDA's example code unless
 * the comment says otherwise. If one robot needs its own value, copy this file into that robot's
 * name and point its TeleOp at the copy.
 */
public final class StarterBot2027Constants {

    private StarterBot2027Constants() {}

    /**
     * Intake and launcher device names and directions, exactly as goBILDA's example configures
     * them. They are the "what the robot is" kind of fact and would normally sit in a robot's
     * config file; they are here because both StarterBots are the same kit and share one robot
     * class. If one robot is rewired differently, move its names into its config file.
     */
    public static final class Devices {
        public static final String LAUNCHER = "launcher";
        public static final DcMotorSimple.Direction LAUNCHER_DIR = DcMotorSimple.Direction.FORWARD;
        public static final String WINDMILL = "windmillServo";                          // the feeder
        public static final DcMotorSimple.Direction WINDMILL_DIR = DcMotorSimple.Direction.REVERSE;
        public static final String INTAKE = "intake", INTAKE_LEFT = "left_intake_servo", INTAKE_RIGHT = "right_intake_servo";
        public static final DcMotorSimple.Direction INTAKE_DIR = DcMotorSimple.Direction.FORWARD;
        public static final DcMotorSimple.Direction INTAKE_LEFT_DIR  = DcMotorSimple.Direction.FORWARD;
        public static final DcMotorSimple.Direction INTAKE_RIGHT_DIR = DcMotorSimple.Direction.REVERSE;
    }

    /** TeleOp driving. goBILDA drives at full stick; the slow mode is ours. */
    public static final class Drive {
        public static final double NORMAL_SPEED   = 1.0;
        public static final double SLOW_SPEED     = 0.35;
        public static final double STICK_DEADBAND = 0.05;
        /** Speeds the beginner auto commands use when no speed is given (driveForward(24) etc.). */
        public static final double AUTO_DRIVE_SPEED = 0.5;
        public static final double AUTO_TURN_SPEED  = 0.3;
    }

    /**
     * The launcher: one flywheel motor held at a velocity (encoder ticks per second; goBILDA's
     * motor has 28 ticks per revolution, so 1250 is about 2680 rpm) and the windmill servo that
     * feeds it once the wheel is fast enough.
     */
    public static final class Launcher {
        public static final double TARGET_VELOCITY = 1250;
        /** Feed only above this. 1200 of 1250 is 96%, the ready fraction below. */
        public static final double MIN_VELOCITY    = 1200;
        public static final double READY_FRACTION  = MIN_VELOCITY / TARGET_VELOCITY;
        /** goBILDA's velocity PIDF for this motor. Different motor from the Skyline launcher's, so different numbers. */
        public static final double PIDF_P = 40, PIDF_I = 0, PIDF_D = 0, PIDF_F = 12.5;
        /** While the windmill feeds, the intake runs this much harder to shake loose a stuck element. */
        public static final double INTAKE_BOOST_WHILE_FEEDING = 0.5;
        /** For the timed shot() in an auto; the TeleOp feeds continuously while the bumper is held instead. */
        public static final double FEED_TIME_SEC = 0.5;
        public static final double SPIN_UP_TIMEOUT_SEC = 2.0;
    }
}
