// ============================================================
//  LESSON 26 — Complete Solution, Part 2: Aim At Target (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "L26 Aim At Target SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L26_AimAtTarget_Solution extends OpMode {

    static final int    PIPELINE      = 0;
    static final double kP            = 0.02;
    static final double MAX_TURN      = 0.4;
    static final double TOLERANCE_DEG = 1.5;

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private Limelight3A limelight;

    @Override
    public void init() {
        initDrive();
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(PIPELINE);
        limelight.start();

        telemetry.addLine("L26 Aim ready. Hold LEFT BUMPER to aim.");
        telemetry.update();
    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double right   =  gamepad1.left_stick_x;
        double turn    =  gamepad1.right_stick_x;

        LLResult result = limelight.getLatestResult();
        boolean seesTarget = result != null && result.isValid();

        if (gamepad1.left_bumper && seesTarget) {
            double tx = result.getTx();
            turn = Range.clip(kP * tx, -MAX_TURN, MAX_TURN);
            if (Math.abs(tx) < TOLERANCE_DEG) {
                turn = 0;
            }
        }

        drive(forward, right, turn);

        telemetry.addData("Sees target", seesTarget);
        if (seesTarget) telemetry.addData("tx (deg)", "%.1f", result.getTx());
        telemetry.addData("Turn power", "%.2f", turn);
        telemetry.update();
    }

    @Override
    public void stop() {
        limelight.stop();
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
