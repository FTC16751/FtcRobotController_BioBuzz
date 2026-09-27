// ============================================================
//  LESSON 27 — Complete Solution: Status LED (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "L27 Status LED SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L27_StatusLed_Solution extends OpMode {

    static final double OFF    = 0.0;
    static final double RED    = 0.277;
    static final double ORANGE = 0.333;
    static final double YELLOW = 0.388;
    static final double GREEN  = 0.500;
    static final double BLUE   = 0.611;
    static final double VIOLET = 0.720;
    static final double WHITE  = 1.0;

    static final double ON_TARGET_DEG = 2.0;

    private Limelight3A limelight;
    private Servo led;

    @Override
    public void init() {
        led = hardwareMap.get(Servo.class, "led_servo");
        led.setPosition(OFF);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();

        telemetry.addLine("L27 ready. Buttons pick a color; hold LEFT BUMPER for Limelight mode.");
        telemetry.update();
    }

    @Override
    public void loop() {
        double color = OFF;
        if (gamepad1.a)      { color = GREEN;  }
        else if (gamepad1.b) { color = RED;    }
        else if (gamepad1.x) { color = BLUE;   }
        else if (gamepad1.y) { color = YELLOW; }

        if (gamepad1.left_bumper) {
            color = chooseAimColor();
        }

        led.setPosition(color);
        telemetry.addData("LED position", color);
        telemetry.update();
    }

    private double chooseAimColor() {
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) {
            return OFF;
        }
        double tx = result.getTx();
        if (Math.abs(tx) < ON_TARGET_DEG) {
            boolean blinkOn = (int) (getRuntime() * 4) % 2 == 0;
            return blinkOn ? GREEN : OFF;
        }
        return tx > 0 ? YELLOW : BLUE;
    }

    @Override
    public void stop() {
        limelight.stop();
    }
}
