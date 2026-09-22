package org.firstinspires.ftc.teamcode.common.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * The drive for a SIMPLE robot: a pushbot (one left motor, one right motor) or a basic mecanum
 * chassis, with motor names and directions hardcoded here and nothing else required on the robot.
 * No RobotConfig, IMU, Pinpoint or Limelight. Call ONE of the init methods, then drive.
 *
 * Grew out of the 2025 drive utility. A robot that needs odometry, driveTo, Pedro, or per-robot
 * configuration should use {@link DriveUtil} with a RobotConfig instead.
 *
 * Pushbot:
 *   drive = new DriveUtilSimple();  drive.initTank(hardwareMap);
 *   drive.simpleDrive(-gamepad1.left_stick_y, gamepad1.right_stick_x);
 * Mecanum:
 *   drive.initMecanum(hardwareMap);
 *   drive.arcadeDrive(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, 0, 0.8);
 */
public class DriveUtilSimple {

    // Device names as typed in the Control Hub configuration. Pushbot names are the FTC samples'.
    public static final String LEFT_DRIVE  = "left_drive";
    public static final String RIGHT_DRIVE = "right_drive";
    public static final String FRONT_LEFT  = "Front_Left";
    public static final String FRONT_RIGHT = "Front_Right";
    public static final String REAR_LEFT   = "Rear_Left";
    public static final String REAR_RIGHT  = "Rear_Right";

    // Pushbot
    public DcMotor leftDrive, rightDrive;
    // Mecanum
    public DcMotor leftFrontMotor, rightFrontMotor, leftRearMotor, rightRearMotor;

    /** Pushbot: two motors, right side reversed so positive power drives forward on both. */
    public void initTank(HardwareMap hardwareMap) {
        leftDrive  = motor(hardwareMap, LEFT_DRIVE,  DcMotorSimple.Direction.FORWARD);
        rightDrive = motor(hardwareMap, RIGHT_DRIVE, DcMotorSimple.Direction.REVERSE);
    }

    /** Mecanum: four motors, left side forward, right side reversed. Same names as our other robots. */
    public void initMecanum(HardwareMap hardwareMap) {
        leftFrontMotor  = motor(hardwareMap, FRONT_LEFT,  DcMotorSimple.Direction.FORWARD);
        leftRearMotor   = motor(hardwareMap, REAR_LEFT,   DcMotorSimple.Direction.FORWARD);
        rightFrontMotor = motor(hardwareMap, FRONT_RIGHT, DcMotorSimple.Direction.REVERSE);
        rightRearMotor  = motor(hardwareMap, REAR_RIGHT,  DcMotorSimple.Direction.REVERSE);
    }

    private static DcMotor motor(HardwareMap hardwareMap, String name, DcMotorSimple.Direction dir) {
        DcMotor m = hardwareMap.get(DcMotor.class, name);
        m.setDirection(dir);
        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        return m;
    }

    /**
     * Pushbot arcade drive. forward: +1 is full speed ahead (pass -left_stick_y). rotate: +1 turns
     * right (pass right_stick_x). Powers are scaled down together if their sum would exceed 1.
     */
    public void simpleDrive(double forward, double rotate) {
        double denominator = Math.max(Math.abs(forward) + Math.abs(rotate), 1);
        leftDrive.setPower((forward + rotate) / denominator);
        rightDrive.setPower((forward - rotate) / denominator);
    }

    /**
     * Mecanum robot-centric arcade drive. Note the sign conventions this legacy API expects:
     * left_stick_y is negated internally (pass the raw gamepad value), and strafe is scaled by 1.1.
     */
    public void arcadeDrive(double left_stick_x, double left_stick_y, double right_stick_x, double right_stick_y, double DRIVE_SPEED) {
        double y = -left_stick_y * DRIVE_SPEED; // Remember, this is reversed!
        double x = left_stick_x * 1.1 * DRIVE_SPEED; // Counteract imperfect strafing
        double rx = right_stick_x * DRIVE_SPEED;

        // Denominator is the largest motor power (absolute value) or 1.
        // This ensures all the powers maintain the same ratio, but only when
        // at least one is out of the range [-1, 1].
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        setMotorPowers(frontLeftPower, backLeftPower, backRightPower, frontRightPower);
    }

    public void setMotorPowers(double lf, double lr, double rr, double rf) {
        leftFrontMotor.setPower(lf);
        leftRearMotor.setPower(lr);
        rightRearMotor.setPower(rr);
        rightFrontMotor.setPower(rf);
    }
}
