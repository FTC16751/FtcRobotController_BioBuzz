// ============================================================
//  LESSON 25 — Pinpoint Odometry, Part 1: Where Is the Robot?
//  Hardware: StarterBot with a goBILDA Pinpoint and two odometry pods
// ============================================================
//  Config names: "odo" (the Pinpoint, on an I2C port) and the four
//  drive motors Front_Left, Front_Right, Rear_Left, Rear_Right.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@TeleOp(name = "L25 Pinpoint Pose")
public class L25_PinpointPose extends OpMode {

    // Where the pods sit, measured from the CENTER of the robot, in millimeters.
    //   X_POD_OFFSET: how far LEFT of center the forward-rolling (X) pod is. Right = negative.
    //   Y_POD_OFFSET: how far FORWARD of center the sideways-rolling (Y) pod is. Back = negative.
    // Measure the StarterBot with a ruler and fill these in (see README).
    static final double X_POD_OFFSET_MM = 0.0;
    static final double Y_POD_OFFSET_MM = 0.0;

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;

    // TODO 1: Declare a private GoBildaPinpointDriver field named 'pinpoint'
    //   private GoBildaPinpointDriver pinpoint;


    @Override
    public void init() {
        initDrive();

        // TODO 2: Get the Pinpoint from the hardware map. Its config name is "odo".
        //   pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "odo");


        // TODO 3: Tell the Pinpoint where the pods are and what kind they are.
        //   pinpoint.setOffsets(X_POD_OFFSET_MM, Y_POD_OFFSET_MM, DistanceUnit.MM);
        //   pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);


        // TODO 4: Tell it which way each pod counts. Start with FORWARD, FORWARD.
        //         You will check these on the robot in TODO 6.
        //   pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
        //                                 GoBildaPinpointDriver.EncoderDirection.FORWARD);


        // TODO 5: Reset the position to (0, 0, 0) and calibrate the Pinpoint's gyro.
        //         The robot must be sitting STILL when this runs.
        //   pinpoint.resetPosAndIMU();


        telemetry.addLine("L25 ready. Keep the robot still until you press PLAY.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Drive with the left stick (move) and right stick (turn). Your L20 mecanum code.
        drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        // TODO 6: Ask the Pinpoint for fresh data, then read the position.
        //         update() must run EVERY loop, or the position never changes.
        //
        //   pinpoint.update();
        //   Pose2D pose = pinpoint.getPosition();
        //   telemetry.addData("X (in)", "%.1f", pose.getX(DistanceUnit.INCH));
        //   telemetry.addData("Y (in)", "%.1f", pose.getY(DistanceUnit.INCH));
        //   telemetry.addData("Heading (deg)", "%.1f", pose.getHeading(AngleUnit.DEGREES));
        //
        //   Now CHECK the directions (README, "The Three Checks"):
        //     push the robot forward  → X must go UP
        //     push the robot left     → Y must go UP
        //     turn the robot left     → Heading must go UP
        //   If X or Y goes the wrong way, change that pod to REVERSED in TODO 4.


        // TODO 7: Press A to put the robot back at (0, 0, 0) without recalibrating.
        //   if (gamepad1.a) {
        //       pinpoint.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
        //   }


        // TODO 8 (CHALLENGE): Show the Pinpoint's status ("READY" when all is well)
        //         and the raw pod counts, which help when a pod is unplugged.
        //   telemetry.addData("Status", pinpoint.getDeviceStatus());
        //   telemetry.addData("X pod ticks", pinpoint.getEncoderX());
        //   telemetry.addData("Y pod ticks", pinpoint.getEncoderY());

        telemetry.update();
    }

    // ── Drive helpers (already done — this is your Lesson 20 mecanum code) ──────────

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

    /** forward: + is forward. right: + strafes right. turn: + turns clockwise (right). */
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
