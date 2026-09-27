// ============================================================
//  LESSON 25 — Complete Solution, Part 1: Pinpoint Pose (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@TeleOp(name = "L25 Pinpoint Pose SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L25_PinpointPose_Solution extends OpMode {

    static final double X_POD_OFFSET_MM = 0.0;   // measure on the StarterBot
    static final double Y_POD_OFFSET_MM = 0.0;

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private GoBildaPinpointDriver pinpoint;

    @Override
    public void init() {
        initDrive();

        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
        pinpoint.setOffsets(X_POD_OFFSET_MM, Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
                                      GoBildaPinpointDriver.EncoderDirection.FORWARD);
        pinpoint.resetPosAndIMU();

        telemetry.addLine("L25 ready. Keep the robot still until you press PLAY.");
        telemetry.update();
    }

    @Override
    public void loop() {
        drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        pinpoint.update();
        Pose2D pose = pinpoint.getPosition();
        telemetry.addData("X (in)", "%.1f", pose.getX(DistanceUnit.INCH));
        telemetry.addData("Y (in)", "%.1f", pose.getY(DistanceUnit.INCH));
        telemetry.addData("Heading (deg)", "%.1f", pose.getHeading(AngleUnit.DEGREES));

        if (gamepad1.a) {
            pinpoint.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
        }

        telemetry.addData("Status", pinpoint.getDeviceStatus());
        telemetry.addData("X pod ticks", pinpoint.getEncoderX());
        telemetry.addData("Y pod ticks", pinpoint.getEncoderY());
        telemetry.update();
    }

    private void initDrive() {
        frontLeft  = hardwareMap.get(DcMotor.class, "Front_Left");
        frontRight = hardwareMap.get(DcMotor.class, "Front_Right");
        rearLeft   = hardwareMap.get(DcMotor.class, "Rear_Left");
        rearRight  = hardwareMap.get(DcMotor.class, "Rear_Right");
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        rearLeft.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor m : new DcMotor[]{frontLeft, frontRight, rearLeft, rearRight}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    private void drive(double forward, double right, double turn) {
        double fl = forward + right + turn;
        double fr = forward - right - turn;
        double rl = forward - right + turn;
        double rr = forward + right - turn;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                                            Math.max(Math.abs(rl), Math.abs(rr))));
        frontLeft.setPower(fl / max);
        frontRight.setPower(fr / max);
        rearLeft.setPower(rl / max);
        rearRight.setPower(rr / max);
    }
}
