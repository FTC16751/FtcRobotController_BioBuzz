package org.firstinspires.ftc.teamcode.teams.p3.decode;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.common.RobotConfig;

/**
 * WHAT DECODE IS, all in this one file: P3's 2025-26 DECODE robot (called "Bot 3" in the old
 * FtcRobotController_Decode repo), brought into this repo unchanged in behavior. Mecanum drive,
 * goBILDA Pinpoint, Limelight, status LED, an intake, an indexer belt, a two-motor flywheel, a
 * stopper servo and a servo turret. Every device name as typed in the Control Hub configuration,
 * and which way each one spins, is here. Speeds, velocities and waypoints are in DecodeConstants.
 *
 * Every value below is what the old code had: nothing here is a guess. The drive chassis part
 * (directions, Pinpoint, IMU, point-to-point gains) is the old P3Bot3Config, copied verbatim.
 *
 * Not carried over because the old code never ran them: the two intake CR servos
 * ("intakeServo", "intakeServo2", commented out of the intake) and the two indexer CR servos
 * ("indexerServo", "indexerServo2", only ever sent power 0). If they are still on the robot they
 * can stay in the hub configuration; nothing here asks for them.
 */
public final class DecodeConfig {

    private DecodeConfig() {}

    // ---- Mechanism device names and directions (drive motors, IMU, Pinpoint and Limelight use
    //      our standard hub names, set below).
    public static final String INTAKE   = "intake";         // motor: pulls game pieces in
    public static final String INDEXER  = "indexer";        // motor: rubber-band belt, intake to flywheel
    public static final String SHOOTER_LEFT  = "left_shooter";    // motor: flywheel, encoder plugged in
    public static final String SHOOTER_RIGHT = "right_shooter";   // motor: flywheel, encoder plugged in
    public static final String STOPPER  = "stopperServo";   // servo: holds game pieces back until a shot
    public static final String TURRET       = "turret";         // servo: goBILDA 5-turn (ServoImplEx)
    public static final String TURRET_LIMIT = "turret_limit";   // REV magnetic limit switch, active low

    public static final DcMotorSimple.Direction INTAKE_DIR  = REVERSE;
    public static final DcMotorSimple.Direction INDEXER_DIR = REVERSE;
    public static final DcMotorSimple.Direction SHOOTER_LEFT_DIR  = FORWARD;
    public static final DcMotorSimple.Direction SHOOTER_RIGHT_DIR = REVERSE;
    public static final Servo.Direction STOPPER_DIR = Servo.Direction.REVERSE;

    public static RobotConfig create() {
        return new RobotConfig(
                new RobotConfig.DrivetrainConfig(
                        REVERSE, FORWARD,     // left front, right front
                        REVERSE, FORWARD),    // left rear,  right rear
                new RobotConfig.OdometryConfig(
                        50, -152.0,           // Pinpoint pod offsets, mm (X pod, Y pod)
                        GoBildaPinpointDriver.EncoderDirection.FORWARD,
                        GoBildaPinpointDriver.EncoderDirection.REVERSED),
                new RobotConfig.ImuConfig(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP),
                new RobotConfig.PointToPointTuning()
                        .xyToleranceMm(18.0).yawToleranceRad(0.055)
                        .xyGains(0.00350, 0.000010, 0.00035).xyAccel(8.0)
                        .yawGains(2.5, 0.00005, 0.08).yawAccel(10.0),
                // Pedro Pathing 3: empty until AutoTune has run on this robot (doc/PEDRO_ON_DECODE.md).
                // Until then DriveUtil says "Pedro Pathing OFF" in telemetry and the robot drives on
                // the Pinpoint with driveTo, exactly as in the Decode season. Paste AutoTune's
                // Foresight lambda here whole (doc/PEDRO_ON_TEST2027.md, section 8 shows what it looks like).
                new RobotConfig.PedroPathingConfig())
            .named("decode")
            // Device names, exactly as in the Control Hub configuration. Drive motors, imu, pinpoint
            // and limelight use the standard names, so only the LED is named here.
            .withHardware(new RobotConfig.HardwareNames()
                    .led("light"))
            // The standard calibration. This chassis was never measured; measure with the Encoder
            // Move Check (rightRearPowerScale, strafeScale, turnCircumferenceIn) before an
            // encoder-based auto trusts it. The Pinpoint autos do not use these.
            .withCalibration(new RobotConfig.Calibration());
    }
}
