package org.firstinspires.ftc.teamcode.common.subsystems.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

/**
 * VelocityMotor: a motor held at a speed by its encoder (flywheel, shooter wheel). The encoder
 * cable MUST be plugged in. For a whole shooter (wheel plus feeder) see LauncherExample instead.
 *
 * Controls (gamepad 1):
 *   A                wheel on / off
 *   D-pad up/down    speed up / down by 50 (takes effect while the wheel is on)
 *
 * To try it: change FLYWHEEL to your device name and remove @Disabled.
 */
@TeleOp(name = "Example: VelocityMotor (flywheel)", group = "Examples")
@Disabled
public class VelocityMotorExample extends OpMode {

    // As typed in the Control Hub configuration. In team code this lives in your BotConfig.
    static final String FLYWHEEL = "launcher";

    private VelocityMotor flywheel;
    private double velocity = 1500;   // encoder ticks per second. In team code, a Constant.

    @Override
    public void init() {
        flywheel = new VelocityMotor(hardwareMap, FLYWHEEL, DcMotorSimple.Direction.FORWARD)
                // Second wheel WITH an encoder cable:
                // .add("launcher2", DcMotorSimple.Direction.REVERSE)
                // Second wheel with NO encoder cable (it copies the first one's power):
                // .follower("launcher2", DcMotorSimple.Direction.REVERSE)
                .pidf(300, 0, 0, 10);   // what every launcher in this repo uses; call it after add()
    }

    @Override
    public void loop() {
        if (gamepad1.dpadUpWasPressed())   velocity += 50;
        if (gamepad1.dpadDownWasPressed()) velocity -= 50;

        if (gamepad1.aWasPressed()) {
            if (flywheel.isSpinning()) flywheel.stop();   // it coasts down
            else flywheel.spinUp(velocity);
        } else if (flywheel.isSpinning()) {
            flywheel.spinUp(velocity);   // follow the d-pad changes
        }

        flywheel.update();   // every loop; only a follower() motor needs it, but it costs nothing
        flywheel.addTelemetry(telemetry, "flywheel");   // shows "actual / target" and READY
        telemetry.addData("ok to feed?", flywheel.isReady());   // within 95% of the target
    }

    @Override
    public void stop() {
        flywheel.stop();
    }
}
