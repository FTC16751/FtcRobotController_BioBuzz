// ============================================================
//  LESSON 27 — LEDs: A Status Light the Driver Can See
//  Hardware: goBILDA RGB Indicator Light on a servo port,
//            plus the Limelight 3A for TODOs 5–7
// ============================================================
//  Config names: "led_servo" (type: Servo) and "limelight".
//  The light is controlled like a servo: each POSITION is a COLOR.
// ============================================================

package org.firstinspires.ftc.teamcode.students.gracejr.l27_leds;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@com.qualcomm.robotcore.eventloop.opmode.Disabled   // DELETE this line when you start the lesson
@TeleOp(name = "L27 Status LED - GraceJr", group = "GraceJr")
public class L27_StatusLed extends OpMode {

    // Servo positions for each color, from goBILDA's chart for this light.
    static final double OFF    = 0.0;
    static final double RED    = 0.277;
    static final double ORANGE = 0.333;
    static final double YELLOW = 0.388;
    static final double GREEN  = 0.500;
    static final double BLUE   = 0.611;
    static final double VIOLET = 0.720;
    static final double WHITE  = 1.0;

    static final double ON_TARGET_DEG = 2.0;   // |tx| smaller than this = lined up

    private Limelight3A limelight;

    // TODO 1: Declare a private Servo field named 'led'
    //   private Servo led;


    @Override
    public void init() {
        // TODO 2: Get the light from the hardware map and turn it off.
        //   led = hardwareMap.get(Servo.class, "led_servo");
        //   led.setPosition(OFF);


        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();

        telemetry.addLine("L27 ready. Buttons pick a color; hold LEFT BUMPER for Limelight mode.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 3: Pick a color from the buttons.
        //   A → GREEN,  B → RED,  X → BLUE,  Y → YELLOW
        //
        //   double color = OFF;
        //   if (gamepad1.a)      { color = GREEN;  }
        //   else if (gamepad1.b) { color = RED;    }
        //   ...


        // TODO 5 (do this AFTER TODO 4 and TODO 6 work) — Limelight mode.
        //         In a match the driver cannot read telemetry; the light tells them instead.
        //         While the left bumper is held, the Limelight chooses the color. It sits
        //         here so it replaces the buttons' color before the color is sent in TODO 4.
        //
        //   if (gamepad1.left_bumper) {
        //       color = chooseAimColor();
        //   }


        // TODO 4: Send the color to the light. Keep this at the END of loop().
        //   led.setPosition(color);
        //   telemetry.addData("LED position", color);
        //
        //   Try every color. Are any hard to tell apart in a bright room?
        //   Which two would a driver NEVER mix up across the field?


        telemetry.update();
    }

    // TODO 6: Finish this method. It returns the color for where the target is:
    //           no target                 → OFF
    //           |tx| < ON_TARGET_DEG      → GREEN   (lined up: shoot!)
    //           tx > 0 (target is right)  → YELLOW
    //           tx < 0 (target is left)   → BLUE
    private double chooseAimColor() {
        LLResult result = limelight.getLatestResult();
        //   if (result == null || !result.isValid()) {
        //       return OFF;
        //   }
        //   double tx = result.getTx();
        //   ...

        return OFF;
    }

    // TODO 7 (CHALLENGE): Blink. When lined up, flash GREEN on and off
    //         twice a second instead of staying solid.
    //         Hint: getRuntime() counts seconds. (int) (getRuntime() * 4) % 2 is
    //         0 or 1, and changes four times a second.

    @Override
    public void stop() {
        limelight.stop();
    }
}
