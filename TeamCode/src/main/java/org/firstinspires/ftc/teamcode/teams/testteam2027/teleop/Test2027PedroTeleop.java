package org.firstinspires.ftc.teamcode.teams.testteam2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Constants;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Robot;

/**
 * The RUN ME TeleOp's sticks with a Pedro Pathing drive toggle, for the "Pedro or moveRobot"
 * comparison (doc/PEDRO_ON_TEST2027.md section 5, question 3; test plan K10). It is a SEPARATE
 * OpMode on purpose: Test2027Teleop (RUN ME) stays on moveRobot so the drivers' feel never changes
 * with the config. Nothing here is needed for a game; delete it once the comparison is decided.
 *
 * Controls (gamepad 1):
 *   Left stick      drive (forward/back) and strafe
 *   Right stick X   turn
 *   Left bumper     hold for slow mode
 *   X               toggle between Pedro driving the wheels and moveRobot driving them
 *   Back            reset the position to (0, 0) facing 0
 *
 * What to compare, driving the same lap both ways: does the robot drift sideways in a straight
 * push, does it overshoot a turn, does a diagonal stay a diagonal. Pedro's drive uses the tuned
 * velocities and centripetal correction (Tuning first, test plan K2 to K4); untuned it still drives.
 *
 * Stick signs are handled inside DriveUtil2026b: both drive commands take the same
 * (strafe, drive, turn, speed) with strafe right-positive and turn clockwise-positive.
 */
@TeleOp(name = "Test2027: Teleop (Pedro drive)", group = "TestTeam2027 Test")
public class Test2027PedroTeleop extends OpMode {

    private Test2027Robot robot;

    @Override
    public void init() {
        robot = new Test2027Robot(hardwareMap, telemetry);
        if (!robot.drive.hasPedro()) {
            telemetry.addLine("This robot's config has no PedroPathingConfig; X does nothing, moveRobot drives.");
        }
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

        if (gamepad1.xWasPressed()) {
            if (robot.drive.isPedroTeleopDrive()) {
                robot.drive.cancel();                 // back to moveRobot
            } else {
                robot.drive.startPedroTeleopDrive();  // Pedro drives from here on
            }
        }
        if (gamepad1.backWasPressed()) {
            robot.drive.resetPosition();
        }

        double drive  = deadband(-gamepad1.left_stick_y);
        double strafe = deadband( gamepad1.left_stick_x);
        double turn   = deadband( gamepad1.right_stick_x);
        double speed  = gamepad1.left_bumper ? Test2027Constants.Drive.SLOW_SPEED
                                             : Test2027Constants.Drive.NORMAL_SPEED;
        if (robot.drive.isPedroTeleopDrive()) {
            robot.drive.pedroTeleopDrive(strafe, drive, turn, speed);
        } else {
            robot.drive.arcadeDrive(strafe, drive, turn, 0, speed);
        }

        telemetry.addData("drive", robot.drive.isPedroTeleopDrive() ? "PEDRO (X for moveRobot)" : "moveRobot (X for Pedro)");
        robot.addTelemetry();
    }

    private static double deadband(double v) {
        return Math.abs(v) < Test2027Constants.Drive.STICK_DEADBAND ? 0.0 : v;
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
