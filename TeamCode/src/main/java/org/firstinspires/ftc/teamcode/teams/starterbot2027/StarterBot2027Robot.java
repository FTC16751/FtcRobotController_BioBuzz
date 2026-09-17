package org.firstinspires.ftc.teamcode.teams.starterbot2027;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.DriveUtil2026b;
import org.firstinspires.ftc.teamcode.common.LaunchController;
import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.common.VisionUtil;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

/**
 * A goBILDA BIOBUZZ StarterBot: mecanum drive, an intake (one motor and two servos), and a
 * launcher (one flywheel motor and the windmill feeder servo). Both StarterBots use this one
 * class; which robot it is comes from the RobotConfig handed in (P3StarterBotConfig or
 * GGStarterBotConfig), which is the only difference between them.
 *
 * Rules that keep OpModes simple:
 *   - OpModes call robot.update() first thing in every loop() and init_loop().
 *   - OpModes call robot.stopAll() in stop().
 *   - OpModes never touch hardwareMap themselves.
 *
 * Nothing here needs a Pinpoint or a Limelight; when one is added to a robot's config, the drive's
 * odometry commands and the vision helpers switch on without changes to this class.
 */
public class StarterBot2027Robot {

    public final RobotConfig config;
    public final DriveUtil2026b drive;
    public final VisionUtil vision;       // null-safe inside: reports nothing without a Limelight
    public final Roller intake;           // the roller motor plus the two corner servos
    public final Launcher launcher;       // flywheel plus windmill feeder
    public final Telemetry telemetry;

    public StarterBot2027Robot(HardwareMap hardwareMap, Telemetry telemetry, RobotConfig config) {
        this.telemetry = telemetry;
        this.config = config;

        drive  = new DriveUtil2026b(hardwareMap, telemetry, null, config);
        vision = new VisionUtil(hardwareMap, telemetry, config.hardware.limelight);

        // The intake: one motor and two continuous servos that all pull elements in together.
        // Device names are goBILDA's, the same on both StarterBots (StarterBot2027Constants.Devices).
        intake = new Roller(hardwareMap, StarterBot2027Constants.Devices.INTAKE, StarterBot2027Constants.Devices.INTAKE_DIR)
                .add(StarterBot2027Constants.Devices.INTAKE_LEFT,  StarterBot2027Constants.Devices.INTAKE_LEFT_DIR)
                .add(StarterBot2027Constants.Devices.INTAKE_RIGHT, StarterBot2027Constants.Devices.INTAKE_RIGHT_DIR)
                .brake();

        // The launcher: goBILDA's single flywheel motor (it has its encoder) and the windmill.
        launcher = new Launcher(
                new VelocityMotor(hardwareMap, StarterBot2027Constants.Devices.LAUNCHER, StarterBot2027Constants.Devices.LAUNCHER_DIR)
                        .pidf(StarterBot2027Constants.Launcher.PIDF_P, StarterBot2027Constants.Launcher.PIDF_I,
                              StarterBot2027Constants.Launcher.PIDF_D, StarterBot2027Constants.Launcher.PIDF_F)
                        .readyFraction(StarterBot2027Constants.Launcher.READY_FRACTION),
                new Roller(hardwareMap, StarterBot2027Constants.Devices.WINDMILL, StarterBot2027Constants.Devices.WINDMILL_DIR),
                new LaunchController.Settings()
                        .feedTimeSec(StarterBot2027Constants.Launcher.FEED_TIME_SEC)
                        .readyFraction(StarterBot2027Constants.Launcher.READY_FRACTION)
                        .spinUpTimeoutSec(StarterBot2027Constants.Launcher.SPIN_UP_TIMEOUT_SEC)
                        .cooldownSec(0).stallFraction(0).keepSpinning(true),
                telemetry);
        // No distance table yet: there is no camera to measure the distance with. When a Limelight
        // is added, a .table(...) here and aim(robot.vision) in the TeleOp is all it takes.

        drive.setDefaultSpeeds(StarterBot2027Constants.Drive.AUTO_DRIVE_SPEED, StarterBot2027Constants.Drive.AUTO_TURN_SPEED);
    }

    /** Call in every loop() and init_loop(). */
    public void update() {
        vision.update();
        drive.update();
        launcher.update();
        intake.update();
    }

    public void stopAll() {
        drive.cancel();
        drive.stop();
        launcher.stop();
        intake.stop();
        vision.stop();
    }

    /** The standard telemetry footer for this robot. */
    public void addTelemetry() {
        launcher.addTelemetry(telemetry, "launcher");
        intake.addTelemetry(telemetry, "intake");
        drive.addTelemetry();
    }
}
