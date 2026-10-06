package org.firstinspires.ftc.teamcode.teams.p3.rex.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;
import org.firstinspires.ftc.teamcode.teams.p3.rex.RexConfig;
import org.firstinspires.ftc.teamcode.teams.p3.rex.RexConstants;

/**
 * Bench test for the shooter prototype: the flywheel and the feeder wheel and NOTHING else, so it
 * runs with only those two motors wired. Find the speeds that shoot well, then put them in
 * RexConstants.
 *
 * Buttons (gamepad 1):
 *   D-pad up / down      shooter target +50 / -50 rpm (0 to 6000)
 *   A                    shooter on at the target (changes while it spins take effect at once)
 *   B                    shooter off
 *   Right bumper (hold)  feeder forward
 *   Left bumper (hold)   feeder reverse
 *   D-pad right / left   feeder speed +5% / -5%
 *
 * The feeder runs only while a bumper is held. Telemetry shows READY once the wheel is within
 * RexConstants.Shooter.READY_FRACTION of the target: feed after that, not before.
 */
@TeleOp(name = "Rex: Shooter Test", group = "Rex")
public class RexShooterTest extends OpMode {

    private static final double STEP_RPM = RexConstants.Shooter.TEST_RPM_STEP;
    private static final double FEEDER_STEP = 0.05;

    private VelocityMotor shooter;
    private Roller feeder;
    private double targetRpm = 0;
    private double feederSpeed = RexConstants.Shooter.FEED_POWER;
    private boolean shooterOn = false;

    @Override
    public void init() {
        shooter = new VelocityMotor(hardwareMap, RexConfig.SHOOTER, RexConfig.SHOOTER_DIR)
                .pidf(RexConstants.Shooter.PIDF_P, RexConstants.Shooter.PIDF_I,
                      RexConstants.Shooter.PIDF_D, RexConstants.Shooter.PIDF_F)
                .readyFraction(RexConstants.Shooter.READY_FRACTION);
        feeder = new Roller(hardwareMap, RexConfig.FEEDER, RexConfig.FEEDER_DIR).brake();
    }

    @Override
    public void loop() {
        // shooter speed
        if (gamepad1.dpadUpWasPressed())   targetRpm = Math.min(RexConstants.Shooter.MAX_RPM, targetRpm + STEP_RPM);
        if (gamepad1.dpadDownWasPressed()) targetRpm = Math.max(0, targetRpm - STEP_RPM);
        if (gamepad1.aWasPressed()) shooterOn = true;
        if (gamepad1.bWasPressed()) shooterOn = false;
        if (shooterOn) shooter.spinUp(RexConstants.Shooter.rpmToTicksPerSec(targetRpm));
        else shooter.stop();

        // feeder speed, then run it only while a bumper is held
        if (gamepad1.dpadRightWasPressed()) feederSpeed = Math.min(1.0, feederSpeed + FEEDER_STEP);
        if (gamepad1.dpadLeftWasPressed())  feederSpeed = Math.max(FEEDER_STEP, feederSpeed - FEEDER_STEP);
        if (gamepad1.right_bumper)     feeder.setPower(feederSpeed);
        else if (gamepad1.left_bumper) feeder.setPower(-feederSpeed);
        else                           feeder.stop();

        double actualRpm = RexConstants.Shooter.ticksPerSecToRpm(shooter.getVelocity());
        telemetry.addData("shooter", "%s   target %.0f rpm   actual %.0f rpm%s",
                shooterOn ? "ON" : "off", targetRpm, actualRpm, shooter.isReady() ? "   READY" : "");
        telemetry.addData("feeder", "%.0f%%   %s", feederSpeed * 100,
                feeder.getPower() > 0 ? "FORWARD" : feeder.getPower() < 0 ? "REVERSE" : "stopped");
        telemetry.addLine("D-pad up/down: rpm +-50   A on   B off");
        telemetry.addLine("Bumpers (hold): feeder fwd / rev   D-pad left/right: feeder speed");
    }

    @Override
    public void stop() {
        shooter.stop();
        feeder.stop();
    }
}
