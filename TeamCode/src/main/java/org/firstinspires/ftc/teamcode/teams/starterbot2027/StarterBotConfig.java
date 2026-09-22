package org.firstinspires.ftc.teamcode.teams.starterbot2027;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.common.RobotConfig;

/**
 * WHAT a goBILDA BIOBUZZ StarterBot IS, all in this one file: every device name as typed in the
 * Control Hub configuration, which way each one spins, how the hub is mounted, and the drive
 * calibration. Two robots are built from the same kit, P3's and GearGirls': {@link #p3()} and
 * {@link #gg()}. Two differences so far, both on GG: its intake gear is mounted on the other side of
 * the robot, so its intake motor runs reversed; and its launcher needs a gentler velocity PIDF.
 *
 * Rewired or renamed something? Edit here. Want it to drive or shoot differently? That is
 * StarterBot2027Constants.
 */
public final class StarterBotConfig {

    // ---- Device names. Drive motors and IMU use our standard names, which are the defaults in
    //      RobotConfig.HardwareNames (Front_Left, Front_Right, Rear_Left, Rear_Right, imu).
    //      The intake and launcher keep goBILDA's names.
    public static final String INTAKE       = "intake";              // the roller motor
    public static final String INTAKE_LEFT  = "left_intake_servo";   // corner servos that pull elements in
    public static final String INTAKE_RIGHT = "right_intake_servo";
    public static final String LAUNCHER     = "launcher";            // flywheel motor, encoder plugged in beside it
    public static final String WINDMILL     = "windmillServo";       // the feeder servo

    // ---- Directions that are the same on both robots. The intake motor's is per robot, below.
    public static final DcMotorSimple.Direction INTAKE_LEFT_DIR  = FORWARD;
    public static final DcMotorSimple.Direction INTAKE_RIGHT_DIR = REVERSE;
    public static final DcMotorSimple.Direction LAUNCHER_DIR     = FORWARD;
    public static final DcMotorSimple.Direction WINDMILL_DIR     = REVERSE;

    /** The chassis: drive directions, IMU mounting, calibration, and which sensors exist (none yet). */
    public final RobotConfig chassis;
    /** Which way the intake roller motor spins to pull elements in. */
    public final DcMotorSimple.Direction intakeDir;
    /** The launcher's velocity PIDF (P, I, D, F). Tune it with the Starterbot: Launcher Test OpMode. */
    public final PIDFCoefficients launcherPidf;

    /** P3 shoots fine on goBILDA's shipped PIDF. */
    public static StarterBotConfig p3() {
        return new StarterBotConfig("P3 Starterbot", FORWARD, new PIDFCoefficients(40, 0, 0, 12.5));
    }

    /**
     * GG's intake gear is on the other side of the robot (found 2026-09-17), so its motor is reversed.
     * GG's launcher stopped and started on goBILDA's P = 40: the hub's velocity loop overshot every
     * update. P = 10 is smooth; F = 32767 / its measured top speed (2026-09-18). P3 does not do this,
     * so something on GG's launcher differs; check that its wheel is tight on the shaft.
     */
    public static StarterBotConfig gg() {
        return new StarterBotConfig("GG Starterbot", REVERSE, new PIDFCoefficients(10, 0, 0, 12.6));
    }

    private StarterBotConfig(String name, DcMotorSimple.Direction intakeDir, PIDFCoefficients launcherPidf) {
        this.intakeDir = intakeDir;
        this.launcherPidf = launcherPidf;
        this.chassis = new RobotConfig(
                // Drive motor directions, goBILDA's: left side reversed. Left stick forward MUST
                // drive forward; reverse any wheel that spins backward on the first drive.
                new RobotConfig.DrivetrainConfig(
                        REVERSE, FORWARD,     // left front, right front
                        REVERSE, FORWARD),    // left rear,  right rear
                // Pinpoint: none yet, so these are ignored. When one is fitted: pod offsets from the
                // robot centre in mm (X pod left positive, Y pod forward positive) and pod directions.
                new RobotConfig.OdometryConfig(0, 0,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD),
                // Control Hub mounting: which way the REV logo and the USB ports face. UNVERIFIED,
                // goBILDA's code never reads the IMU; a commanded 90 degree turn should read about 90.
                new RobotConfig.ImuConfig(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD),
                new RobotConfig.PointToPointTuning(),   // unused until there is a Pinpoint
                null)                                   // no Pedro Pathing (needs a Pinpoint first)
            .named(name)
            .withHardware(new RobotConfig.HardwareNames()
                    .pinpoint(null)      // none yet; "odo" when fitted
                    .limelight(null))    // none yet; "limelight" when fitted
            // Starting guesses for the encoder moves: goBILDA 312 rpm motor (537.7 ticks/rev) on a
            // 96 mm mecanum wheel. Measure each with the Encoder Move Check before an auto trusts them.
            .withCalibration(new RobotConfig.Calibration()
                    .rightRearPowerScale(1.0)
                    .strafeScale(1.1)
                    .turnCircumferenceIn(27.5)
                    .encoderCountsPerInch(RobotConfig.Calibration.countsPerInch(537.7, 1.0, 96)));
    }
}
