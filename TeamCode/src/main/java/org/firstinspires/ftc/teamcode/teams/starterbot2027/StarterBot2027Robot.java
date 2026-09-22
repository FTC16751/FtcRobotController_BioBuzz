package org.firstinspires.ftc.teamcode.teams.starterbot2027;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.DriveUtil2026b;
import org.firstinspires.ftc.teamcode.common.LaunchController;
import org.firstinspires.ftc.teamcode.common.VisionUtil;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

/**
 * A goBILDA BIOBUZZ StarterBot: mecanum drive, an intake (one motor and two servos), and a
 * launcher (one flywheel motor and the windmill feeder servo). Both StarterBots use this one
 * class; which robot it is comes from the StarterBotConfig handed in (p3() or gg()).
 * Every device name and direction is in StarterBotConfig; every speed and launcher number is in
 * StarterBot2027Constants.
 *
 * Rules that keep OpModes simple:
 *   - OpModes call robot.update() first thing in every loop() and init_loop().
 *   - OpModes call robot.stopAll() in stop().
 *   - OpModes never touch hardwareMap themselves.
 *
 * Nothing here needs a Pinpoint or a Limelight; when one is added to the config, the drive's
 * odometry commands and the vision helpers switch on without changes to this class.
 */
public class StarterBot2027Robot {

    public final StarterBotConfig config;
    public final DriveUtil2026b drive;
    public final VisionUtil vision;       // null-safe inside: reports nothing without a Limelight
    public final Roller intake;           // the roller motor plus the two corner servos
    public final Launcher launcher;       // flywheel plus windmill feeder
    public final Telemetry telemetry;

    public StarterBot2027Robot(HardwareMap hardwareMap, Telemetry telemetry, StarterBotConfig config) {
        this.telemetry = telemetry;
        this.config = config;

        drive  = new DriveUtil2026b(hardwareMap, telemetry, null, config.chassis);
        vision = new VisionUtil(hardwareMap, telemetry, config.chassis.hardware.limelight);

        // The intake: one motor (its direction is the one thing that differs between the two
        // robots) and two continuous servos that all pull elements in together.
        intake = new Roller(hardwareMap, StarterBotConfig.INTAKE, config.intakeDir)
                .add(StarterBotConfig.INTAKE_LEFT,  StarterBotConfig.INTAKE_LEFT_DIR)
                .add(StarterBotConfig.INTAKE_RIGHT, StarterBotConfig.INTAKE_RIGHT_DIR)
                .brake();

        // The launcher: goBILDA's single flywheel motor (it has its encoder) and the windmill.
        launcher = new Launcher(
                new VelocityMotor(hardwareMap, StarterBotConfig.LAUNCHER, StarterBotConfig.LAUNCHER_DIR)
                        .pidf(config.launcherPidf.p, config.launcherPidf.i,
                              config.launcherPidf.d, config.launcherPidf.f)   // per robot: GG's is gentler
                        .readyFraction(StarterBot2027Constants.Launcher.READY_FRACTION),
                new Roller(hardwareMap, StarterBotConfig.WINDMILL, StarterBotConfig.WINDMILL_DIR),
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
