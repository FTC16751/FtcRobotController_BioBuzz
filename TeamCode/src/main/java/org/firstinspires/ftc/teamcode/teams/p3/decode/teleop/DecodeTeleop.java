package org.firstinspires.ftc.teamcode.teams.p3.decode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.CommonConstants;
import org.firstinspires.ftc.teamcode.common.SharedState;
import org.firstinspires.ftc.teamcode.common.vision.VisionAim;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConstants;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeRobot;

/**
 * Decode single-driver TeleOp: arcade drive, vision snap-to-goal, intake and launcher on gamepad 1.
 * The turret is not used (it was never competition-ready); see test/DecodeTurretCalibration.
 *
 * GAMEPAD 1
 *   Left stick          drive and strafe
 *   Right stick X       turn
 *   Right stick button  HOLD to turn to the goal tag (needs the tag in view)
 *   A                   toggle intake on / off
 *   B                   toggle intake reverse / off
 *   Right trigger       HOLD to shoot (fires repeatedly while held)
 *   Left trigger        HOLD to run the indexer backwards (unjam)
 *   Y                   launcher AUTO: velocity follows the distance table
 *   X                   flywheel off
 *   D-pad left / right  close / far preset velocity
 *   D-pad up / down     velocity +100 / -100 (held: repeats every loop, as it always did)
 *   Left / right bumper alliance BLUE / RED (RED unless the autonomous set one)
 */
@TeleOp(name = "Decode: Teleop (RUN ME)", group = "Decode")
public class DecodeTeleop extends OpMode {

    private DecodeRobot robot;

    private enum LauncherMode { AUTO_TARGETING, MANUAL_OVERRIDE }
    private LauncherMode launcherMode = LauncherMode.MANUAL_OVERRIDE;
    private double requestedVelocity = 0;
    private boolean intakeOn = false;
    private boolean intakeReverse = false;

    @Override
    public void init() {
        robot = new DecodeRobot(hardwareMap, telemetry);
        robot.vision.setTargetingAlliance(SharedState.alliance);   // set by the autonomous, RED by default
        telemetry.addData("Status", "Initialized: %s", robot.config.robotName);
    }

    @Override
    public void init_loop() {
        robot.update();
    }

    @Override
    public void loop() {
        robot.update();
        handleDriving();
        handleIntake();
        handleLauncher();
        handleAlliance();
        displayTelemetry();
    }

    private void handleDriving() {
        double turn = gamepad1.right_stick_x;
        if (gamepad1.right_stick_button && robot.vision.isTargetVisible()) {
            turn = VisionAim.turnPower(robot.vision.getTargetAngleX(),
                    DecodeConstants.Aim.SNAP_KP, DecodeConstants.Aim.SNAP_TOLERANCE_DEG);
        }
        // arcadeDrive's arguments are (strafe, drive, turn, unused, speed); a forward stick is negative Y.
        robot.drive.arcadeDrive(gamepad1.left_stick_x, -gamepad1.left_stick_y, turn, 0, DecodeConstants.Drive.TELEOP_SPEED);
    }

    private void handleIntake() {
        if (gamepad1.aWasPressed()) { intakeOn = !intakeOn; intakeReverse = false; }
        if (gamepad1.bWasPressed()) { intakeReverse = !intakeReverse; intakeOn = false; }
        if (intakeOn) robot.intake.in();
        else if (intakeReverse) robot.intake.out();
        else robot.intake.stop();
    }

    private void handleLauncher() {
        boolean shooting = gamepad1.right_trigger > 0.8;
        robot.launcher.shoot(shooting);
        if (!robot.launcher.isBusy()) {   // the shot sequence owns the indexer while a shot is in progress
            if (gamepad1.left_trigger > 0.8) robot.indexer.setPower(-DecodeConstants.Indexer.UNJAM_POWER);
            else if (!shooting)              robot.indexer.stop();
        }

        if (gamepad1.yWasPressed()) launcherMode = LauncherMode.AUTO_TARGETING;
        if (gamepad1.x || gamepad1.dpad_left || gamepad1.dpad_right || gamepad1.dpad_up || gamepad1.dpad_down) {
            launcherMode = LauncherMode.MANUAL_OVERRIDE;
        }

        if (launcherMode == LauncherMode.AUTO_TARGETING) {
            requestedVelocity = robot.launcher.aim(robot.vision);
        } else {
            if (gamepad1.dpad_left)       requestedVelocity = DecodeConstants.Launcher.CLOSE_TARGET_VELOCITY;
            else if (gamepad1.dpad_right) requestedVelocity = DecodeConstants.Launcher.FAR_TARGET_VELOCITY;
            else if (gamepad1.x)          { requestedVelocity = 0; robot.launcher.stop(); }
            else if (gamepad1.dpad_up)    requestedVelocity = Math.min(requestedVelocity + DecodeConstants.Launcher.NUDGE, DecodeConstants.Launcher.MAX_VELOCITY);
            else if (gamepad1.dpad_down)  requestedVelocity = Math.max(requestedVelocity - DecodeConstants.Launcher.NUDGE, 0);
            robot.launcher.spinUp(requestedVelocity);
        }
    }

    private void handleAlliance() {
        if (gamepad1.leftBumperWasPressed())  setAlliance(CommonConstants.Alliance.BLUE);
        if (gamepad1.rightBumperWasPressed()) setAlliance(CommonConstants.Alliance.RED);
    }

    private void setAlliance(CommonConstants.Alliance alliance) {
        SharedState.alliance = alliance;
        robot.vision.setTargetingAlliance(alliance);
    }

    private void displayTelemetry() {
        telemetry.addData("Alliance", SharedState.alliance);
        telemetry.addData("Launcher", launcherMode);
        telemetry.addData("Intake", intakeOn ? "IN" : intakeReverse ? "OUT" : "off");
        robot.addTelemetry();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
