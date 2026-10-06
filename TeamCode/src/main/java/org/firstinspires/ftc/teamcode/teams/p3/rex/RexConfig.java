package org.firstinspires.ftc.teamcode.teams.p3.rex;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.RobotConfig;

/**
 * WHAT REX IS, all in this one file: P3's first 2027 robot. Mecanum drive, goBILDA Pinpoint,
 * Limelight, Pedro Pathing, and four mechanism motors: intake, transfer (belt rollers), feeder
 * wheel and a flywheel. Every device name as typed in the Control Hub configuration, and which
 * way each one spins, is here. If a motor misbehaves, this is the only place to look for what it
 * is called and which way it turns. Speeds and shooter numbers are in RexConstants.
 *
 * EVERYTHING MARKED UNVERIFIED IS A STARTING GUESS: the robot has not been wired yet. Check each
 * against the real robot, in the order in README.md, and delete the word when it is confirmed.
 */
public final class RexConfig {

    private RexConfig() {}

    // ---- Mechanism device names and directions. Drive motors, IMU, Pinpoint and Limelight use our
    //      standard hub names (set below), the same as every other robot we have.
    //      Directions are UNVERIFIED: the sign convention is "FORWARD pulls a game piece toward
    //      the shooter". Run the shooter test, then flip any that go the wrong way.
    public static final String INTAKE   = "intake";     // motor: pulls game pieces in
    public static final String TRANSFER = "transfer";   // motor: belt rollers from the intake to the feeder
    public static final String FEEDER   = "feeder";     // motor: the wheel that pushes a piece into the flywheel
    public static final String SHOOTER   = "shooter";   // motor: flywheel, 6000 rpm goBILDA Yellow Jacket, encoder plugged in
    // public static final String SHOOTER_2 = "shooter2";   // the second flywheel motor, when it is added:
    //                                                      // in RexRobot, change VelocityMotor to .add(SHOOTER_2, dir)

    public static final DcMotorSimple.Direction INTAKE_DIR   = FORWARD;   // UNVERIFIED
    public static final DcMotorSimple.Direction TRANSFER_DIR = FORWARD;   // UNVERIFIED
    public static final DcMotorSimple.Direction FEEDER_DIR   = FORWARD;   // UNVERIFIED
    public static final DcMotorSimple.Direction SHOOTER_DIR  = FORWARD;   // UNVERIFIED: the ball should leave the wheel, not get pulled in

    public static RobotConfig create() {
        return new RobotConfig(
                // Drive motor directions, goBILDA's: left side reversed. UNVERIFIED: left stick forward
                // MUST drive forward; reverse any wheel that spins backward on the first drive.
                new RobotConfig.DrivetrainConfig(
                        REVERSE, FORWARD,     // left front, right front
                        REVERSE, FORWARD),    // left rear,  right rear
                // Pinpoint: pod offsets from the robot centre in mm (X pod: left is positive; Y pod:
                // forward is positive) and each pod's counting direction. UNVERIFIED, all zero until the
                // pods are measured. Wrong offsets show up as the position drifting during turns.
                new RobotConfig.OdometryConfig(0, 0,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD,
                        GoBildaPinpointDriver.EncoderDirection.FORWARD),
                // Control Hub mounting: which way the REV logo and the USB ports face. UNVERIFIED;
                // a commanded 90 degree turn should read about 90.
                new RobotConfig.ImuConfig(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD),
                new RobotConfig.PointToPointTuning(),
                // Pedro Pathing 3: empty until AutoTune has run on this robot. Until then DriveUtil
                // says "Pedro Pathing OFF" in telemetry and the robot drives on the Pinpoint as usual.
                // Paste AutoTune's Foresight lambda here whole (doc/PEDRO_ON_TEST2027.md, section 8
                // shows what it looks like and how the numbers get used).
                new RobotConfig.PedroPathingConfig())
            .named("rex")
            // Device names, exactly as in the Control Hub configuration.
            .withHardware(new RobotConfig.HardwareNames()
                    .driveMotors("Front_Left", "Front_Right", "Rear_Left", "Rear_Right")
                    .imu("imu")
                    .pinpoint("odo")
                    .limelight("limelight"))
            // Starting guesses for the encoder moves: goBILDA 312 rpm motor (537.7 ticks/rev) on a
            // 96 mm mecanum wheel. UNVERIFIED: measure each with the Encoder Move Check (copy
            // teams/testteam2027/test/Test2027EncoderMoveCheck) before an auto trusts them.
            .withCalibration(new RobotConfig.Calibration()
                    .rightRearPowerScale(1.0)
                    .strafeScale(1.1)
                    .turnCircumferenceIn(27.5)
                    .encoderCountsPerInch(RobotConfig.Calibration.countsPerInch(537.7, 1.0, 96)));
    }
}
