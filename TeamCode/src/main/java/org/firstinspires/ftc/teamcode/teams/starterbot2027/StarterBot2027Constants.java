package org.firstinspires.ftc.teamcode.teams.starterbot2027;

/**
 * HOW the two BIOBUZZ StarterBots OPERATE: speeds and launcher numbers, shared by both robots
 * because they are the same goBILDA kit. Every number here is from goBILDA's example code unless
 * the comment says otherwise. Device names and directions are in StarterBotConfig.
 */
public final class StarterBot2027Constants {

    private StarterBot2027Constants() {}

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
     * motor has 28 ticks per revolution, so 1250 is about 2680 rpm) and the windmill (servo or motor) that
     * feeds it once the wheel is fast enough.
     */
    public static final class Launcher {
        public static final double TARGET_VELOCITY = 1250;
        /** Feed only above this. 1200 of 1250 is 96%, the ready fraction below. */
        public static final double MIN_VELOCITY    = 1200;
        public static final double READY_FRACTION  = MIN_VELOCITY / TARGET_VELOCITY;
        // The velocity PIDF is per robot (GG needs a gentler P than P3): StarterBotConfig.launcherPidf.
        /** While the windmill feeds, the intake runs this much harder to shake loose a stuck element. */
        public static final double INTAKE_BOOST_WHILE_FEEDING = 0.5;
        /** For the timed shot() in an auto; the TeleOp feeds continuously while the bumper is held instead. */
        public static final double FEED_TIME_SEC = 0.5;
        public static final double SPIN_UP_TIMEOUT_SEC = 2.0;
    }
}
