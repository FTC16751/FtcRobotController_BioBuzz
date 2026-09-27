// ============================================================
//  LESSON 26 — Limelight, Part 2: Turn to Face the Target
//  Hardware: StarterBot with the Limelight 3A mounted on it
// ============================================================
//  Drive normally. HOLD the left bumper and the robot turns by
//  itself until the target is straight ahead (tx = 0).
//  This is Lesson 24's P control, with tx as the error.
// ============================================================

package org.firstinspires.ftc.teamcode.students.moira.l26_limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

@com.qualcomm.robotcore.eventloop.opmode.Disabled   // DELETE this line when you start the lesson
@TeleOp(name = "L26 Aim At Target - Moira", group = "Moira")
public class L26_AimAtTarget extends OpMode {

    static final int    PIPELINE      = 0;
    static final double kP            = 0.02;  // turn power per degree of tx. Tune this (TODO 5).
    static final double MAX_TURN      = 0.4;   // never turn faster than this while aiming
    static final double TOLERANCE_DEG = 1.5;   // "close enough" to straight ahead

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

        // TODO 1: When the left bumper is held AND the camera sees a target,
        //         replace the driver's turn with an automatic one.
        //
        //   if (gamepad1.left_bumper && seesTarget) {
        //       double tx = result.getTx();
        //       ... TODO 2 and 3 go here ...
        //   }


        // TODO 2: P control. The error is tx: how many degrees the target is off center.
        //         Target to the RIGHT (tx positive) → turn RIGHT (turn positive).
        //   turn = kP * tx;


        // TODO 3: Limit the turn so the robot never spins fast, and stop turning
        //         once it is close enough (otherwise it wiggles back and forth).
        //   turn = Range.clip(turn, -MAX_TURN, MAX_TURN);
        //   if (Math.abs(tx) < TOLERANCE_DEG) {
        //       turn = 0;
        //   }


        drive(forward, right, turn);

        // TODO 4: Show what is happening.
        //   telemetry.addData("Sees target", seesTarget);
        //   if (seesTarget) telemetry.addData("tx (deg)", "%.1f", result.getTx());
        //   telemetry.addData("Turn power", "%.2f", turn);


        // TODO 5: Tune kP on the robot. Put a target 4 feet away, 30° to one side,
        //         and hold the bumper. Write down what happens with each kP:
        //           0.005 → ?     0.02 → ?     0.05 → ?
        //         Too small: slow, stops short. Too big: swings past and wobbles.
        //         Keep the biggest kP that does NOT wobble.

        telemetry.update();
    }

    @Override
    public void stop() {
        limelight.stop();
    }

    // ── Drive helpers (already done — your Lesson 20 mecanum code) ─────────────────

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
