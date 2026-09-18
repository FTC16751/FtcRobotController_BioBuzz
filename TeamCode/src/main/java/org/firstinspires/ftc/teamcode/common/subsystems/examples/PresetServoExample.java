package org.firstinspires.ftc.teamcode.common.subsystems.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.subsystems.PresetServo;

/**
 * PresetServo: a servo that goes to a few named positions (wrist, hood, stopper, flipper).
 *
 * Controls (gamepad 1):
 *   Y / A            wrist UP / DOWN
 *   X                flick: go to KICK, wait 0.3 s, come back on its own
 *   D-pad up/down    HOLD to creep; read the position off the telemetry to find your preset values
 *
 * To try it: change WRIST to your device name and remove @Disabled.
 */
@TeleOp(name = "Example: PresetServo (wrist)", group = "Examples")
@Disabled
public class PresetServoExample extends OpMode {

    // As typed in the Control Hub configuration. In team code this lives in your BotConfig.
    static final String WRIST = "wrist";

    // Preset names, as constants so a typo (UPP) is a compile error. A raw "UP" works too; a wrong
    // one throws on the button press and lists the real names.
    static final String UP = "UP", DOWN = "DOWN", KICK = "KICK";

    // Starting guesses. In team code these live in your Constants.
    static final double UP_POS = 0.80, DOWN_POS = 0.20, KICK_POS = 0.95;

    private PresetServo wrist;

    @Override
    public void init() {
        wrist = new PresetServo(hardwareMap, WRIST)
                .preset(UP, UP_POS)
                .preset(DOWN, DOWN_POS)
                .preset(KICK, KICK_POS)
                .limits(0.10, 0.95);   // never commanded outside this, whatever a preset or nudge asks
        // Nothing has moved yet. To move during init, end the chain with .startAt(DOWN)
    }

    @Override
    public void loop() {
        if (gamepad1.yWasPressed()) wrist.goTo(UP);
        if (gamepad1.aWasPressed()) wrist.goTo(DOWN);
        if (gamepad1.xWasPressed()) wrist.flick(KICK, 0.3);

        if (gamepad1.dpad_up)   wrist.nudge(0.004);
        if (gamepad1.dpad_down) wrist.nudge(-0.004);

        wrist.update();   // every loop, or a flick never comes back
        wrist.addTelemetry(telemetry, "wrist");
    }

    @Override
    public void stop() {
        wrist.stop();
    }
}
