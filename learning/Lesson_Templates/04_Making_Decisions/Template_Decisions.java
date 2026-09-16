// ============================================================
//  LESSON 04 — Making Decisions (if/else, loops)
//  Book: Chapter 4  |  Hardware: Gamepad only
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L04 Decisions")
public class L04_Decisions extends OpMode {

    // For button edge-detection (detecting first press, not held)
    boolean wasAPressed = false;
    int aPressCount = 0;

    @Override
    public void init() {
        telemetry.addLine("L04 Decisions ready. Press PLAY, then use gamepad.");
        telemetry.update();
    }

    @Override
    public void loop() {

        // ── PART 1: Joystick Zone Reporter ────────────────────────────────────
        // Read the left joystick Y axis (negative = up, positive = down)
        double stickY = -gamepad1.left_stick_y;  // negated so up = positive

        // TODO 1: Display the raw stick value
        //   telemetry.addData("Stick Y", stickY);


        // TODO 2: Use if/else if/else to describe the stick position.
        //         Print one of these labels based on the value:
        //
        //   stickY > 0.5   → "FORWARD FAST"
        //   stickY > 0.1   → "forward slow"
        //   stickY < -0.5  → "BACKWARD FAST"
        //   stickY < -0.1  → "backward slow"
        //   otherwise      → "stopped"
        //
        //   telemetry.addData("Direction", <your string here>);
        //
        //   if (stickY > 0.5) {
        //       telemetry.addData("Direction", "FORWARD FAST");
        //   } else if (...) {
        //       ...
        //   }


        telemetry.addLine("---");

        // ── PART 2: Deadzone Filter ────────────────────────────────────────────
        // Small joystick movements (drift) should be treated as zero.
        double stickX = gamepad1.left_stick_x;

        // TODO 3: If the absolute value of stickX is less than 0.1, set it to 0.0
        //         Use Math.abs(stickX) to get the absolute value.
        //
        //   if (Math.abs(stickX) < 0.1) {
        //       stickX = 0.0;
        //   }
        //
        //   telemetry.addData("Stick X (filtered)", stickX);


        telemetry.addLine("---");

        // ── PART 3: Button Toggle (Edge Detection) ────────────────────────────
        // Detect the FIRST frame when button A is pressed (not while it's held).

        // TODO 4: If gamepad1.a is pressed AND it wasn't pressed last frame,
        //         increment aPressCount.
        //
        //   if (gamepad1.a && !wasAPressed) {
        //       aPressCount++;
        //   }
        //   wasAPressed = gamepad1.a;   // remember for next frame
        //
        //   telemetry.addData("A presses", aPressCount);


        telemetry.addLine("---");

        // ── PART 4: Speed Mode Selector ───────────────────────────────────────
        // Different buttons select different speed modes.

        // TODO 5: Use if/else if to set a String called "speedMode":
        //   gamepad1.a is held  → speedMode = "TURBO (100%)"
        //   gamepad1.b is held  → speedMode = "SLOW (25%)"
        //   neither             → speedMode = "NORMAL (50%)"
        //
        //   Display: telemetry.addData("Speed Mode", speedMode);
        //
        //   String speedMode;
        //   if (gamepad1.a) { ... }
        //   ...


    }
}
