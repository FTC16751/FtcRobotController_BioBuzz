// ============================================================
//  LESSON 24 — Control Theory & PID
//  Book: Chapter 24  |  Hardware: Motor with encoder
// ============================================================
//  Work through the steps in order.
//  Each TODO step upgrades the controller from the previous one.
//
//  Config: a motor named "motor" with encoder plugged in.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "L24 PID Control")
public class L24_PIDControl extends OpMode {

    DcMotor     motor;
    ElapsedTime timer = new ElapsedTime();

    // ── TARGET POSITION ────────────────────────────────────────────────────────
    // D-pad up/down moves the target in 100-tick increments
    static final int FULLY_IN  = 0;
    static final int FULLY_OUT = 1100;   // TUNE for your slide/mechanism
    int desiredPosition = 0;

    // ── TUNING CONSTANTS ───────────────────────────────────────────────────────
    // Start all at 0. Tune kP first, then kI, then kD.
    static final double kP          = 0.001;   // TODO: tune this
    static final double kI          = 0.0;     // TODO: tune this
    static final double kD          = 0.0;     // TODO: tune this
    static final double kF          = 0.0;     // TODO: add if needed (vertical slides)
    static final int    DEADBAND    = 20;      // encoder ticks to treat as "close enough"
    static final double MAX_INTEGRAL = 0.3;    // prevents integral windup

    // PID state (need to persist between loop() calls)
    double lastError    = 0;
    double sumErrors    = 0;
    int    lastDesired  = 0;


    // ── INIT ───────────────────────────────────────────────────────────────────
    @Override
    public void init() {
        motor = hardwareMap.get(DcMotor.class, "motor");
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        timer.reset();

        telemetry.addLine("L24: D-pad UP/DOWN to change target. Watch the motor respond.");
        telemetry.update();
    }


    // ── LOOP ───────────────────────────────────────────────────────────────────
    @Override
    public void loop() {

        // ── TARGET SELECTION ──────────────────────────────────────────────────
        if (gamepad1.dpad_up) {
            desiredPosition = Math.min(desiredPosition + 100, FULLY_OUT);
        } else if (gamepad1.dpad_down) {
            desiredPosition = Math.max(desiredPosition - 100, FULLY_IN);
        }

        int actualPosition = motor.getCurrentPosition();
        int error          = desiredPosition - actualPosition;

        telemetry.addData("Desired", desiredPosition);
        telemetry.addData("Actual",  actualPosition);
        telemetry.addData("Error",   error);


        // ══════════════════════════════════════════════════════════════════════
        //  STEP 1 — BANG-BANG (uncomment this block to test bang-bang first)
        // ══════════════════════════════════════════════════════════════════════
        //
        // TODO STEP 1: Implement bang-bang with deadband.
        //              When |error| < DEADBAND → stop
        //              When error > 0  → setPower(0.5)
        //              When error < 0  → setPower(-0.5)
        //
        // double power;
        // if (Math.abs(error) < DEADBAND) {
        //     power = 0.0;
        // } else if (error > 0) {
        //     power = 0.5;
        // } else {
        //     power = -0.5;
        // }
        // motor.setPower(power);
        // telemetry.addData("Mode",  "Bang-Bang");
        // telemetry.addData("Power", power);


        // ══════════════════════════════════════════════════════════════════════
        //  STEP 2 — PROPORTIONAL (comment out Step 1, uncomment this)
        // ══════════════════════════════════════════════════════════════════════
        //
        // TODO STEP 2: Proportional control.
        //              power = error * kP
        //              Clamp power to [-1.0, 1.0].
        //
        // double power = error * kP;
        // power = Math.max(-1.0, Math.min(1.0, power));
        // motor.setPower(power);
        // telemetry.addData("Mode",  "P-Only");
        // telemetry.addData("Power", power);


        // ══════════════════════════════════════════════════════════════════════
        //  STEP 3 — FULL PID (comment out Step 2, implement this)
        // ══════════════════════════════════════════════════════════════════════
        //
        // TODO STEP 3: Full PID control.
        //              derivative = (error - lastError) / timer.seconds()
        //              sumErrors += error * timer.seconds()
        //              Reset sumErrors when desiredPosition changes.
        //              Clamp sumErrors to ±MAX_INTEGRAL.
        //              power = kP*error + kI*sumErrors + kD*derivative + kF
        //
        // double deltaTime   = timer.seconds();
        // double derivative  = (error - lastError) / deltaTime;
        // sumErrors         += error * deltaTime;
        //
        // if (desiredPosition != lastDesired) {
        //     sumErrors   = 0;
        //     lastDesired = desiredPosition;
        // } else {
        //     if (Math.abs(sumErrors) > MAX_INTEGRAL) {
        //         sumErrors = Math.signum(sumErrors) * MAX_INTEGRAL;
        //     }
        // }
        //
        // double power = (kP * error) + (kI * sumErrors) + (kD * derivative) + kF;
        // power = Math.max(-1.0, Math.min(1.0, power));
        // motor.setPower(power);
        // telemetry.addData("Mode",        "PID");
        // telemetry.addData("Power",       power);
        // telemetry.addData("Sum Errors",  sumErrors);
        // telemetry.addData("Derivative",  derivative);
        //
        // lastError = error;
        // timer.reset();
    }
}
