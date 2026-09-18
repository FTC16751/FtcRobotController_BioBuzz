package org.firstinspires.ftc.teamcode.common.subsystems.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.subsystems.Claw;

/**
 * Claw: a servo that grips. It is a PresetServo with OPEN and CLOSED already named, so everything
 * in PresetServoExample (nudge, limits, flick, extra presets) works here too.
 *
 * Controls (gamepad 1):
 *   A                       toggle open / closed
 *   Left / right bumper     open / close
 *
 * To try it: change CLAW to your device name and remove @Disabled.
 */
@TeleOp(name = "Example: Claw", group = "Examples")
@Disabled
public class ClawExample extends OpMode {

    // As typed in the Control Hub configuration. In team code this lives in your BotConfig.
    static final String CLAW = "claw";

    // Starting guesses. In team code these live in your Constants.
    static final double OPEN = 0.30, CLOSED = 0.70;

    private Claw claw;

    @Override
    public void init() {
        claw = new Claw(hardwareMap, CLAW, OPEN, CLOSED);
        // Two-servo claw, the second one mirrored:
        // claw = new Claw(hardwareMap, "left_hand", OPEN, CLOSED).pair("right_hand", Servo.Direction.REVERSE);
    }

    @Override
    public void loop() {
        if (gamepad1.aWasPressed())           claw.toggle();
        if (gamepad1.leftBumperWasPressed())  claw.open();
        if (gamepad1.rightBumperWasPressed()) claw.close();

        claw.update();
        claw.addTelemetry(telemetry, "claw");
        telemetry.addData("holding something?", claw.isClosed());
    }

    @Override
    public void stop() {
        claw.stop();
    }
}
