package org.firstinspires.ftc.teamcode.common.subsystems.examples;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.LaunchController;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

/**
 * Launcher: a whole shooter. You hand it a flywheel (VelocityMotor) and a feeder (Roller) and it
 * runs the shot for you: wait for the wheel to reach speed, feed one game piece, cool down.
 *
 * Controls (gamepad 1):
 *   D-pad right / left   wheel on at the CLOSE / FAR speed
 *   D-pad up / down      nudge the speed by 50
 *   A                    wheel off
 *   Right trigger        HOLD to shoot at the speed last picked (spins up first if the wheel is
 *                        off), repeats while held
 *   Left trigger         HOLD to run the feeder backward (unjam)
 *
 * To try it: change the device names and remove @Disabled. The wheel stays OFF until a button
 * asks for it. For aiming by distance from an AprilTag (table(...) and aim(...)) see
 * teams/testteam2027/teleop/Test2027LauncherTeleop.
 */
@TeleOp(name = "Example: Launcher", group = "Examples")
@Disabled
public class LauncherExample extends OpMode {

    // As typed in the Control Hub configuration. In team code these live in your BotConfig.
    static final String FLYWHEEL = "launcher";
    static final String FEEDER   = "windmillServo";

    // Encoder ticks per second. In team code these live in your Constants.
    static final double CLOSE = 1300, FAR = 1600, NUDGE = 50;

    private Launcher launcher;

    @Override
    public void init() {
        launcher = new Launcher(
                new VelocityMotor(hardwareMap, FLYWHEEL, DcMotorSimple.Direction.FORWARD).pidf(300, 0, 0, 10),
                new Roller(hardwareMap, FEEDER, DcMotorSimple.Direction.REVERSE),
                new LaunchController.Settings().feedTimeSec(0.45),   // how long the feeder runs per shot
                telemetry);
    }

    @Override
    public void loop() {
        if (gamepad1.dpadRightWasPressed()) launcher.spinUp(CLOSE);
        if (gamepad1.dpadLeftWasPressed())  launcher.spinUp(FAR);
        if (gamepad1.dpadUpWasPressed())    launcher.nudge(NUDGE);
        if (gamepad1.dpadDownWasPressed())  launcher.nudge(-NUDGE);
        if (gamepad1.aWasPressed())         launcher.spinDown();

        // No speed picked yet (no d-pad press) means a target of 0, and a shot at 0 feeds into a
        // stopped wheel. So the trigger only counts once there is a speed.
        boolean fire = gamepad1.right_trigger > 0.5 && launcher.getTargetVelocity() > 0;
        launcher.shoot(fire);   // true = "shoot when ready"; the sequence itself runs in update()

        // The feeder by hand. These are ignored while a shot is in progress.
        if (gamepad1.left_trigger > 0.5) launcher.feedBack();
        else if (!fire)                  launcher.feedStop();

        launcher.update();   // every loop, or nothing ever shoots
        launcher.addTelemetry(telemetry, "launcher");
        // In an auto: launcher.shoot() once, then move on in the loop where launcher.shotDone() is true.
    }

    @Override
    public void stop() {
        launcher.stop();
    }
}
