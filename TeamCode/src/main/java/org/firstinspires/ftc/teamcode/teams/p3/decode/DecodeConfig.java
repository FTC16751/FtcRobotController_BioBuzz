package org.firstinspires.ftc.teamcode.teams.p3.decode;

import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;

import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
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
                // Pedro Pathing 3, tuned with AutoTune on this robot (Foresight lambda pasted whole, 2026-10).
                // Re-run AutoTune and paste again if the robot's weight or wheels change
                // (doc/PEDRO_ON_DECODE.md, doc/PEDRO_ON_TEST2027.md section 8).
                new RobotConfig.PedroPathingConfig()
                        .foresight(c -> {
                            Controller primaryTranslationalForward = Controller.proportional(0.3771885791283783);
                            Controller secondaryTranslationalForward = Controller.proportional(0.13936110132348004);
                            Controller primaryTranslationalLateral = Controller.proportional(0.6069154710320314);
                            Controller secondaryTranslationalLateral = Controller.proportional(0.22423904946627538);

                            c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                            c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                            c.coast.set(Controller.proportionalFeedforward(0.01216235802064684));
                            c.brake.set(Controller.proportionalFeedforward(0.010338004317549814));

                            c.headingFeedback.set(Controller.proportional(5.826051056815965));
                            c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05217106188519486, 0.004088424046176246));

                            c.linearBrakeCoefficients.set(Matrix.diag(0.12768784975010228, 0.07107016915442102));
                            c.quadraticBrakeCoefficients.set(Matrix.diag(9.611913921042032E-4, 0.0015287313360453046));

                            c.maxAchievableForwardVelocity.set(77.19774973164238);
                            c.maxAchievableStrafeVelocity.set(63.50269229731865);
                            c.naturalForwardDeceleration.set(28.48893176747052);
                            c.naturalStrafeDeceleration.set(61.939145054980116);
                        })
        )
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
