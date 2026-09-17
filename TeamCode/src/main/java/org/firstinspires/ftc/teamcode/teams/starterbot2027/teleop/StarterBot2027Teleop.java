package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Constants;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Robot;

/**
 * goBILDA's BIOBUZZ StarterBot TeleOp, the same controls, on our robot class. Not an OpMode by
 * itself: P3StarterBotTeleop and GGStarterBotTeleop are the two lines that pick a robot.
 *
 * Controls (gamepad 1), as in goBILDA's example plus a slow mode:
 *   Left stick      drive (forward/back) and strafe
 *   Right stick X   turn
 *   Left bumper     hold for slow mode (ours)
 *   Right trigger   intake in, proportional; left trigger: intake out. Both: off
 *   Right bumper    HOLD to launch: the wheel spins up; once it is fast enough the windmill feeds
 *                   and the intake runs harder to push elements through. Release: everything stops
 *   Back            reset the position to (0, 0) facing 0 (only meaningful once there is a Pinpoint)
 */
public abstract class StarterBot2027Teleop extends OpMode {

    private StarterBot2027Robot robot;

    /** Which StarterBot this is. */
    protected abstract RobotConfig config();

    @Override
    public void init() {
        robot = new StarterBot2027Robot(hardwareMap, telemetry, config());
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
        double intakePower = gamepad1.right_trigger - gamepad1.left_trigger;
        intakePower += handleLauncher();
        robot.intake.setPower(intakePower);

        if (gamepad1.backWasPressed()) {
            robot.drive.resetPosition();
        }
        robot.addTelemetry();
    }

    private void handleDriving() {
        double drive  = deadband(-gamepad1.left_stick_y);
        double strafe = deadband( gamepad1.left_stick_x);
        double turn   = deadband( gamepad1.right_stick_x);
        double speed  = gamepad1.left_bumper ? StarterBot2027Constants.Drive.SLOW_SPEED
                                             : StarterBot2027Constants.Drive.NORMAL_SPEED;
        robot.drive.arcadeDrive(strafe, drive, turn, 0, speed);
    }

    /**
     * goBILDA's launch(): wheel on while the bumper is held, feed once it is fast enough.
     * Returns the extra intake power to apply while feeding (0 otherwise).
     */
    private double handleLauncher() {
        if (!gamepad1.right_bumper) {
            robot.launcher.spinDown();
            robot.launcher.feedStop();
            return 0;
        }
        robot.launcher.spinUp(StarterBot2027Constants.Launcher.TARGET_VELOCITY);
        if (robot.launcher.isReady()) {
            robot.launcher.feed();
            return StarterBot2027Constants.Launcher.INTAKE_BOOST_WHILE_FEEDING;
        }
        robot.launcher.feedStop();
        return 0;
    }

    private static double deadband(double v) {
        return Math.abs(v) < StarterBot2027Constants.Drive.STICK_DEADBAND ? 0.0 : v;
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
