package org.firstinspires.ftc.teamcode.teams.p3.rex;

/**
 * HOW REX OPERATES: speeds and shooter numbers. Device names and directions are in RexConfig.
 * Every number is a first guess until the shooter prototype has been tested.
 */
public final class RexConstants {

    private RexConstants() {}

    /** AdvantageScope logging (common/LogUtil). One .wpilog per run on the Control Hub while true. */
    public static final class Logging {
        public static final boolean ENABLED = true;
    }

    /** TeleOp driving and the speeds the beginner auto commands use. */
    public static final class Drive {
        public static final double NORMAL_SPEED   = 1.0;
        public static final double SLOW_SPEED     = 0.35;
        public static final double STICK_DEADBAND = 0.05;
        public static final double AUTO_DRIVE_SPEED = 0.5;
        public static final double AUTO_TURN_SPEED  = 0.3;
    }

    /** Powers for the belts and wheels that move a game piece. Sign is set by the direction in RexConfig. */
    public static final class Intake {
        public static final double INTAKE_POWER   = 1.0;
        public static final double TRANSFER_POWER = 0.8;
    }

    /**
     * The shooter. The flywheel is a 6000 rpm goBILDA Yellow Jacket (5203 series): 28 encoder ticks
     * per motor revolution, so 6000 rpm is 2800 ticks per second, which is the unit the SDK's
     * velocity control uses.
     */
    public static final class Shooter {
        public static final double TICKS_PER_REV = 28;
        public static final double MAX_RPM = 6000;
        /** One D-pad press in the shooter test moves the target this much. */
        public static final double TEST_RPM_STEP = 50;
        /** goBILDA's velocity PIDF for this motor (it runs P3's StarterBot launcher). F = 32767 / top speed is about 11.7. Retune with the shooter test. */
        public static final double PIDF_P = 40, PIDF_I = 0, PIDF_D = 0, PIDF_F = 12.5;
        /** Feed a game piece only above this fraction of the target speed. */
        public static final double READY_FRACTION = 0.96;
        public static final double FEED_POWER = 0.8;
        public static final double FEED_TIME_SEC = 0.5;
        public static final double SPIN_UP_TIMEOUT_SEC = 2.0;

        public static double rpmToTicksPerSec(double rpm) { return rpm * TICKS_PER_REV / 60.0; }
        public static double ticksPerSecToRpm(double ticksPerSec) { return ticksPerSec * 60.0 / TICKS_PER_REV; }
    }
}
