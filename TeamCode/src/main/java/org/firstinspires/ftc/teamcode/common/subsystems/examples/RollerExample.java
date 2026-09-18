package org.firstinspires.ftc.teamcode.common.subsystems.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.subsystems.Roller;

/**
 * Roller: anything that spins game pieces in or out (intake, feeder, indexer, conveyor).
 *
 * Controls (gamepad 1):
 *   Right bumper   HOLD to pull in
 *   Left bumper    HOLD to push out
 *   A              spit out for half a second, then stop on its own
 *
 * To try it: change INTAKE to your device name and remove @Disabled.
 */
@TeleOp(name = "Example: Roller (intake)", group = "Examples")
@Disabled
public class RollerExample extends OpMode {

    // As typed in the Control Hub configuration. In team code this lives in your BotConfig.
    static final String INTAKE = "intake";

    private Roller intake;

    @Override
    public void init() {
        intake = new Roller(hardwareMap, INTAKE, DcMotorSimple.Direction.FORWARD)
                // More motors or continuous-rotation servos that spin with it:
                // .add("left_intake_servo", DcMotorSimple.Direction.FORWARD)
                // .add("right_intake_servo", DcMotorSimple.Direction.REVERSE)
                .speeds(1.0, -0.6);   // what in() and out() send. Pulls the wrong way? Flip the Direction above.
    }

    @Override
    public void loop() {
        if (gamepad1.right_bumper)     intake.in();
        else if (gamepad1.left_bumper) intake.out();
        else if (!intake.isBusy())     intake.stop();   // isBusy = a timed run is going; leave it alone

        if (gamepad1.aWasPressed()) intake.outFor(0.5);

        intake.update();   // every loop, or a timed run never ends
        intake.addTelemetry(telemetry, "intake");
    }

    @Override
    public void stop() {
        intake.stop();
    }
}
