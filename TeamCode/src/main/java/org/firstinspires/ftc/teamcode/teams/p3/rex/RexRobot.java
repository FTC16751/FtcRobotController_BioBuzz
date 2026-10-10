package org.firstinspires.ftc.teamcode.teams.p3.rex;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.RobotBase;
import org.firstinspires.ftc.teamcode.common.launch.LaunchController;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

/**
 * Rex: the one object every OpMode for this robot creates. Mecanum drive (with the Pinpoint and
 * Pedro Pathing when tuned), the Limelight, an intake, a transfer, and a launcher (flywheel plus
 * feeder wheel). Names and directions come from RexConfig, numbers from RexConstants.
 *
 * Rules that keep OpModes simple:
 *   - OpModes call robot.update() first thing in every loop() and init_loop().
 *   - OpModes call robot.stopAll() in stop().
 *   - OpModes never touch hardwareMap themselves (the bench tests in test/ are the exception:
 *     they build only the one mechanism they test, so the rest need not be wired).
 */
public class RexRobot extends RobotBase {

    public final Roller intake;
    public final Roller transfer;

    public RexRobot(HardwareMap hardwareMap, Telemetry telemetry) {
        super(hardwareMap, telemetry, RexConfig.create(), false);   // no log for Rex yet; true turns it on

        intake   = track("intake", new Roller(hardwareMap, RexConfig.INTAKE, RexConfig.INTAKE_DIR)
                .speeds(RexConstants.Intake.INTAKE_POWER, -RexConstants.Intake.INTAKE_POWER).brake());
        transfer = track("transfer", new Roller(hardwareMap, RexConfig.TRANSFER, RexConfig.TRANSFER_DIR)
                .speeds(RexConstants.Intake.TRANSFER_POWER, -RexConstants.Intake.TRANSFER_POWER).brake());

        launcher = new Launcher(
                new VelocityMotor(hardwareMap, RexConfig.SHOOTER, RexConfig.SHOOTER_DIR)
                        .pidf(RexConstants.Shooter.PIDF_P, RexConstants.Shooter.PIDF_I,
                              RexConstants.Shooter.PIDF_D, RexConstants.Shooter.PIDF_F)
                        .readyFraction(RexConstants.Shooter.READY_FRACTION),
                new Roller(hardwareMap, RexConfig.FEEDER, RexConfig.FEEDER_DIR)
                        .speeds(RexConstants.Shooter.FEED_POWER, -RexConstants.Shooter.FEED_POWER).brake(),
                new LaunchController.Settings()
                        .feedTimeSec(RexConstants.Shooter.FEED_TIME_SEC)
                        .readyFraction(RexConstants.Shooter.READY_FRACTION)
                        .spinUpTimeoutSec(RexConstants.Shooter.SPIN_UP_TIMEOUT_SEC)
                        .cooldownSec(0).stallFraction(0).keepSpinning(true),
                telemetry);
        // No distance table yet: the shooter's speeds come out of the shooter test first.

        drive.setDefaultSpeeds(RexConstants.Drive.AUTO_DRIVE_SPEED, RexConstants.Drive.AUTO_TURN_SPEED);
    }
}
