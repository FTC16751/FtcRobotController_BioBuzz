package org.firstinspires.ftc.teamcode.teams.p3.decode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.RobotBase;
import org.firstinspires.ftc.teamcode.common.hardware.LedUtil;
import org.firstinspires.ftc.teamcode.common.launch.LaunchController;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.PresetServo;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;
import org.firstinspires.ftc.teamcode.common.vision.AimLed;

/**
 * Decode: the one object every OpMode for this robot creates. Mecanum drive on the Pinpoint, the
 * Limelight, an intake, a launcher (two-motor flywheel plus the indexer belt as its feeder), the
 * stopper servo, a status LED and the turret. Names and directions come from DecodeConfig, numbers
 * from DecodeConstants.
 *
 * Rules that keep OpModes simple:
 *   - OpModes call robot.update() first thing in every loop() and init_loop().
 *   - OpModes call robot.stopAll() in stop().
 *   - OpModes never touch hardwareMap themselves (the bench tests in test/ are the exception).
 *
 * This replaces the old P3_Robot3 and its P3_*Util classes. Same hardware, same numbers; the
 * launch sequence is common/launch/LaunchController through common/subsystems/Launcher.
 */
public class DecodeRobot extends RobotBase {

    public final Roller intake;
    public final Roller indexer;            // the belt; also the launcher's feeder, exposed for unjamming by hand
    public final PresetServo stopper;       // "STOP" holds game pieces back, "SHOOT" lets them through
    public final LedUtil led;               // null if the config has no LED
    public final Turret turret;
    private final AimLed aimLed;            // LED shows lined up / goal left / goal right / none

    public DecodeRobot(HardwareMap hardwareMap, Telemetry telemetry) {
        super(hardwareMap, telemetry, DecodeConfig.create(), DecodeConstants.Logging.ENABLED);

        intake = track("intake", new Roller(hardwareMap, DecodeConfig.INTAKE, DecodeConfig.INTAKE_DIR)
                .speeds(DecodeConstants.Intake.POWER, -DecodeConstants.Intake.POWER));

        indexer = new Roller(hardwareMap, DecodeConfig.INDEXER, DecodeConfig.INDEXER_DIR)
                .speeds(DecodeConstants.Indexer.POWER, -DecodeConstants.Indexer.POWER).brake();

        launcher = new Launcher(
                new VelocityMotor(hardwareMap, DecodeConfig.SHOOTER_LEFT, DecodeConfig.SHOOTER_LEFT_DIR)
                        .add(DecodeConfig.SHOOTER_RIGHT, DecodeConfig.SHOOTER_RIGHT_DIR)
                        .pidf(DecodeConstants.Launcher.PIDF_P, DecodeConstants.Launcher.PIDF_I,
                              DecodeConstants.Launcher.PIDF_D, DecodeConstants.Launcher.PIDF_F)
                        .readyFraction(DecodeConstants.Launcher.READY_FRACTION),
                indexer,
                new LaunchController.Settings()
                        .feedTimeSec(DecodeConstants.Launcher.FEED_TIME_SEC)
                        .cooldownSec(DecodeConstants.Launcher.COOLDOWN_SEC)
                        .spinUpTimeoutSec(DecodeConstants.Launcher.SPIN_UP_TIMEOUT_SEC)
                        .readyFraction(DecodeConstants.Launcher.READY_FRACTION)
                        .stallFraction(DecodeConstants.Launcher.STALL_FRACTION)
                        .keepSpinning(true),
                telemetry)
                .table(DecodeConstants.Launcher.FLYWHEEL_TABLE, DecodeConstants.Launcher.FLYWHEEL_INITIAL_FALLBACK);

        Servo stopperServo = hardwareMap.get(Servo.class, DecodeConfig.STOPPER);
        stopperServo.setDirection(DecodeConfig.STOPPER_DIR);
        stopper = new PresetServo(stopperServo)
                .preset("STOP", DecodeConstants.Stopper.STOP)
                .preset("SHOOT", DecodeConstants.Stopper.SHOOT)
                .startAt("STOP");   // safe initial state, as the old robot did

        led = config.hardware.led == null ? null : new LedUtil(hardwareMap, config.hardware.led);
        aimLed = new AimLed(led, vision, DecodeConstants.Aim.LED_TOLERANCE_DEG,
                new AimLed.Colors()
                        .goalToRight(DecodeConstants.Aim.LED_GOAL_RIGHT)
                        .goalToLeft(DecodeConstants.Aim.LED_GOAL_LEFT));

        turret = new Turret(hardwareMap.get(ServoImplEx.class, DecodeConfig.TURRET),
                            hardwareMap.get(DigitalChannel.class, DecodeConfig.TURRET_LIMIT));
    }

    @Override protected void onUpdate() {
        aimLed.update();     // does nothing without an LED
    }

    @Override protected void onStop() {
        stopper.goTo("STOP");
    }
}
