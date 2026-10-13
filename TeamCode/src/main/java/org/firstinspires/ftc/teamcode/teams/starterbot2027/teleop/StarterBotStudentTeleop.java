package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.hardware.Rumble;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Constants;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Robot;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;

/**
 * STUDENT VERSION of the StarterBot TeleOp. Same controls as StarterBot2027Teleop, written so you
 * can read it top to bottom:
 *
 *   init()       runs once when you press INIT. Builds the robot.
 *   init_loop()  runs over and over until you press PLAY.
 *   loop()       runs over and over after PLAY. It just calls one method per job.
 *   stop()       runs once when you press STOP.
 *
 * To change what a button does, find the method for that job (handleDriving, handleIntake, ...)
 * and edit only that method.
 *
 * Controls (gamepad 1)
 *   Left stick      drive forward/back and strafe
 *   Right stick X   turn
 *   Left stick click  HOLD for slow mode
 *   Right trigger   intake in (push harder = faster)
 *   Left trigger    intake out
 *   Right bumper    HOLD to launch: the wheel spins up, and when it is fast enough the windmill
 *                   feeds the element. Let go and everything stops. The controller buzzes once
 *                   when the wheel is up to speed
 *   Left bumper     HOLD to run the windmill backwards (unjam). Ignored while launching
 *   Back            reset the robot's position to (0, 0)
 */
@TeleOp(name = "StarterBot: Student Teleop", group = "StarterBot2027")
public class StarterBotStudentTeleop extends OpMode {

    private StarterBot2027Robot robot;
    private Rumble readyBuzz;

    @Override
    public void init() {
        // Which robot are you driving? Use StarterBotConfig.p3() or StarterBotConfig.gg().
        StarterBotConfig config = StarterBotConfig.p3();

        robot = new StarterBot2027Robot(hardwareMap, telemetry, config);
        readyBuzz = new Rumble(gamepad1, 1);   // buzzes the controller 1 time
        telemetry.addData("Status", "Initialized: %s", robot.config.robotName);
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void loop() {
        robot.update();   // always first: reads the sensors and updates the robot

        handleDriving();
        handleLauncher();
        handleIntake();
        handleReadyBuzz();
        handleResetPosition();

        robot.addTelemetry();   // always last: shows the robot's numbers on the Driver Station
    }

    /** Sticks drive the robot. Click the left stick to go slow. */
    private void handleDriving() {
        // Pushing a stick forward gives a NEGATIVE y, so we flip it with a minus sign.
        double drive  = deadband(-gamepad1.left_stick_y);
        double strafe = deadband(gamepad1.left_stick_x);
        double turn   = deadband(gamepad1.right_stick_x);

        double speed = StarterBot2027Constants.Drive.NORMAL_SPEED;
        if (gamepad1.left_stick_button) {
            speed = StarterBot2027Constants.Drive.SLOW_SPEED;
        }

        // The 4th number (0) is not used here.
        robot.drive.arcadeDrive(strafe, drive, turn, 0, speed);
    }

    /** Right bumper launches. Left bumper unjams. Otherwise the launcher is off. */
    private void handleLauncher() {
        if (isLaunching()) {
            robot.launcher.spinUp(StarterBot2027Constants.Launcher.TARGET_VELOCITY);
            if (robot.launcher.isReady()) {
                robot.launcher.feed();       // wheel is fast enough: push an element in
            } else {
                robot.launcher.feedStop();   // still spinning up: wait
            }
        } else {
            robot.launcher.spinDown();
            if (gamepad1.left_bumper) {
                robot.launcher.feedBack();   // unjam: windmill backwards
            } else {
                robot.launcher.feedStop();
            }
        }
    }

    /** Triggers run the intake. While a launch is feeding, the intake pushes harder. */
    private void handleIntake() {
        double power = gamepad1.right_trigger - gamepad1.left_trigger;   // in minus out

        if (isLaunching() && robot.launcher.isReady()) {
            power += StarterBot2027Constants.Launcher.INTAKE_BOOST_WHILE_FEEDING;
        }
        robot.intake.setPower(power);
    }

    /** One buzz on the controller when the held launch button's wheel is up to speed. */
    private void handleReadyBuzz() {
        readyBuzz.update(isLaunching() && robot.launcher.isReady());
    }

    /** Back button: tell the robot it is at (0, 0). Needs a Pinpoint to mean anything. */
    private void handleResetPosition() {
        if (gamepad1.backWasPressed()) {
            robot.drive.resetPosition();
        }
    }

    /** True while the driver is holding the launch button. */
    private boolean isLaunching() {
        return gamepad1.right_bumper;
    }

    /** Ignore tiny stick movements (a stick never rests at exactly 0). */
    private double deadband(double value) {
        if (Math.abs(value) < StarterBot2027Constants.Drive.STICK_DEADBAND) {
            return 0.0;
        }
        return value;
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
