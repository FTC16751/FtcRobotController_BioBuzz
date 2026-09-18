package org.firstinspires.ftc.teamcode.common.subsystems.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.subsystems.PresetMotor;

/**
 * PresetMotor: an encoder motor that goes to a few named positions (lift, slide, arm, hang).
 *
 * Positions are encoder ticks from wherever the mechanism was when init ran, so press INIT with
 * the lift all the way DOWN.
 *
 * Controls (gamepad 1):
 *   X / B / Y        lift to DOWN / MID / HIGH
 *   Left stick Y     drive it by hand; let go and it holds where it is
 *   D-pad up/down    nudge 50 ticks
 *   Back             make here the new zero
 *
 * To try it: change LIFT to your device name and remove @Disabled. Start with power(0.4) and
 * small presets, then read the real tick values off the telemetry.
 */
@TeleOp(name = "Example: PresetMotor (lift)", group = "Examples")
@Disabled
public class PresetMotorExample extends OpMode {

    // As typed in the Control Hub configuration. In team code this lives in your BotConfig.
    static final String LIFT = "lift";

    // Preset names, as constants so a typo (HIHG) is a compile error. A raw "HIGH" works too; a
    // wrong one throws on the button press and lists the real names.
    static final String DOWN = "DOWN", MID = "MID", HIGH = "HIGH";

    // Starting guesses, in encoder ticks. In team code these live in your Constants.
    static final int DOWN_TICKS = 0, MID_TICKS = 1000, HIGH_TICKS = 2000, MAX_TICKS = 2300;

    private PresetMotor lift;

    @Override
    public void init() {
        lift = new PresetMotor(hardwareMap, LIFT, DcMotorSimple.Direction.FORWARD)
                // Second motor on the other side of the lift:
                // .add("lift2", DcMotorSimple.Direction.REVERSE)
                // Touch sensor at the bottom that re-zeroes the encoder whenever it is pressed:
                // .homeSwitch("lift_home")
                .preset(DOWN, DOWN_TICKS)
                .preset(MID, MID_TICKS)
                .preset(HIGH, HIGH_TICKS)
                .limits(0, MAX_TICKS)   // no target outside this, and the stick cannot push past either end
                .power(0.4);      // gentle while testing; default is full power
    }

    @Override
    public void loop() {
        // goTo returns at once; the motor controller does the move while the loop keeps running.
        if (gamepad1.xWasPressed()) lift.goTo(DOWN);
        if (gamepad1.bWasPressed()) lift.goTo(MID);
        if (gamepad1.yWasPressed()) lift.goTo(HIGH);

        if (gamepad1.dpadUpWasPressed())   lift.nudge(50);
        if (gamepad1.dpadDownWasPressed()) lift.nudge(-50);
        if (gamepad1.backWasPressed())     lift.zeroHere();

        double stick = -gamepad1.left_stick_y;   // up on the stick is negative, so flip it
        if (Math.abs(stick) > 0.1)                      lift.manual(stick);
        else if (lift.getPresetName().equals("manual")) lift.manual(0);   // stick released: hold here

        lift.update();   // every loop, or the home switch is never checked
        lift.addTelemetry(telemetry, "lift");
        // In an auto: lift.goTo(HIGH) in one step, then move on once lift.isAtTarget() is true.
        telemetry.addData("arrived?", lift.isAtTarget());
    }

    @Override
    public void stop() {
        lift.stop();
    }
}
