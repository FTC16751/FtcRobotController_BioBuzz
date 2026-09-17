package org.firstinspires.ftc.teamcode.teams.testteam2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Constants;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Robot;

/**
 * The RUN ME TeleOp's driving plus the launcher, all on gamepad 1. A separate OpMode so the RUN ME
 * one stays the bare template; copy from here when a team adds its own mechanism.
 *
 * Controls (gamepad 1):
 *   Left stick      drive (forward/back) and strafe
 *   Right stick X   turn
 *   Left bumper     hold for slow mode
 *   Right trigger   HOLD to shoot: spins up if needed, feeds for FEED_TIME_SEC, repeats while held
 *   Left trigger    HOLD to run the feeder backward (unjam)
 *   D-pad right     wheel on at the CLOSE preset;  D-pad left: the FAR preset
 *   D-pad up/down   nudge the velocity by NUDGE
 *   A               wheel off
 *   Y               AIM mode: the velocity follows the goal's distance (green LED = lined up)
 *   X               back to preset mode
 *   Back            reset the position to (0, 0) facing 0
 *
 * Starts in PRESET mode at the CLOSE velocity with the wheel OFF (hard rule 0: demo-safe).
 */
@TeleOp(name = "Test2027: Teleop (Launcher)", group = "TestTeam2027")
public class Test2027LauncherTeleop extends OpMode {

    private enum LauncherMode { PRESET, AIM }

    private Test2027Robot robot;
    private LauncherMode launcherMode = LauncherMode.PRESET;
    private double presetVelocity = Test2027Constants.Launcher.CLOSE_VELOCITY;

    @Override
    public void init() {
        robot = new Test2027Robot(hardwareMap, telemetry);
        robot.lookForTag(Test2027Constants.TagTest.TAG_ID);   // the goal tag, for aiming
        telemetry.addData("Status", "Initialized: %s", robot.config.robotName);
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void loop() {
        robot.update();

        handleDriving();
        handleLauncher();
        handleFeeder();
        if (gamepad1.backWasPressed()) {
            robot.drive.resetPosition();
        }

        telemetry.addData("launcher mode", "%s%s", launcherMode,
                launcherMode == LauncherMode.PRESET ? String.format("  preset %.0f", presetVelocity) : "");
        robot.addTelemetry();
    }

    private void handleDriving() {
        double drive  = deadband(-gamepad1.left_stick_y);
        double strafe = deadband( gamepad1.left_stick_x);
        double turn   = deadband( gamepad1.right_stick_x);
        double speed  = gamepad1.left_bumper ? Test2027Constants.Drive.SLOW_SPEED
                                             : Test2027Constants.Drive.NORMAL_SPEED;
        robot.drive.arcadeDrive(strafe, drive, turn, 0, speed);
    }

    /** Presets and nudges set the wheel; AIM mode follows the goal; A turns the wheel off. */
    private void handleLauncher() {
        if (gamepad1.yWasPressed()) launcherMode = LauncherMode.AIM;
        if (gamepad1.xWasPressed()) launcherMode = LauncherMode.PRESET;

        if (gamepad1.dpadRightWasPressed()) { presetVelocity = Test2027Constants.Launcher.CLOSE_VELOCITY; launcherMode = LauncherMode.PRESET; robot.launcher.spinUp(presetVelocity); }
        if (gamepad1.dpadLeftWasPressed())  { presetVelocity = Test2027Constants.Launcher.FAR_VELOCITY;   launcherMode = LauncherMode.PRESET; robot.launcher.spinUp(presetVelocity); }
        if (gamepad1.dpadUpWasPressed())    { presetVelocity += Test2027Constants.Launcher.NUDGE; launcherMode = LauncherMode.PRESET; robot.launcher.spinUp(presetVelocity); }
        if (gamepad1.dpadDownWasPressed())  { presetVelocity -= Test2027Constants.Launcher.NUDGE; launcherMode = LauncherMode.PRESET; robot.launcher.spinUp(presetVelocity); }
        if (gamepad1.aWasPressed())         { robot.launcher.spinDown(); }

        if (launcherMode == LauncherMode.AIM) {
            robot.launcher.aim(robot.vision);   // every loop: the table picks the velocity, the wheel follows
        }

        robot.launcher.shoot(gamepad1.right_trigger > 0.5);
    }

    /** Left trigger runs the feeder backward for an unjam; the shot sequence owns it otherwise. */
    private void handleFeeder() {
        if (gamepad1.left_trigger > 0.5) {
            robot.launcher.feedBack();
        } else if (gamepad1.right_trigger <= 0.5) {
            robot.launcher.feedStop();
        }
    }

    private static double deadband(double v) {
        return Math.abs(v) < Test2027Constants.Drive.STICK_DEADBAND ? 0.0 : v;
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
