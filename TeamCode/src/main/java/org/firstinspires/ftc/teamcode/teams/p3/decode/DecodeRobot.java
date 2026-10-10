package org.firstinspires.ftc.teamcode.teams.p3.decode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.LogUtil;
import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.common.drive.DriveUtil;
import org.firstinspires.ftc.teamcode.common.hardware.LedUtil;
import org.firstinspires.ftc.teamcode.common.launch.LaunchController;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.PresetServo;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;
import org.firstinspires.ftc.teamcode.common.vision.AimLed;
import org.firstinspires.ftc.teamcode.common.vision.Vision;
import org.firstinspires.ftc.teamcode.common.vision.VisionUtil;

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
public class DecodeRobot {

    public final RobotConfig config;
    public final DriveUtil drive;
    public final Vision vision;
    public final Roller intake;
    public final Roller indexer;            // the belt; also the launcher's feeder, exposed for unjamming by hand
    public final Launcher launcher;         // two-motor flywheel + indexer belt + shot sequence + distance table
    public final PresetServo stopper;       // "STOP" holds game pieces back, "SHOOT" lets them through
    public final LedUtil led;               // null if the config has no LED
    public final Turret turret;
    private final AimLed aimLed;            // LED shows lined up / goal left / goal right / none
    public final Telemetry telemetry;

    public DecodeRobot(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.config = DecodeConfig.create();

        if (DecodeConstants.Logging.ENABLED) {
            LogUtil.start(hardwareMap);   // one .wpilog per OpMode run, named by time and OpMode
        }
        drive  = new DriveUtil(hardwareMap, telemetry, null, config);
        vision = new VisionUtil(hardwareMap, telemetry, config.hardware.limelight);

        intake = new Roller(hardwareMap, DecodeConfig.INTAKE, DecodeConfig.INTAKE_DIR)
                .speeds(DecodeConstants.Intake.POWER, -DecodeConstants.Intake.POWER);

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

    /** Call in every loop() and init_loop(). Steps the camera, the drive, the launcher and the LED. */
    public void update() {
        vision.update();     // camera first, so this loop's drive step sees this loop's tag
        drive.update();
        launcher.update();
        intake.update();
        aimLed.update();     // does nothing without an LED
        addLog();
    }

    public void stopAll() {
        drive.cancel();
        drive.stop();
        intake.stop();
        launcher.stop();
        stopper.goTo("STOP");
        vision.stop();
        LogUtil.stop();      // flush and close this run's log
    }

    /** This loop's values for the AdvantageScope log. Nothing happens when logging is off. */
    private void addLog() {
        if (!LogUtil.isRunning()) return;
        drive.addLog();
        LogUtil.logBattery();
        launcher.log();
        intake.log("Intake");
        vision.log("Vision");
    }

    /** The standard telemetry footer for this robot. */
    public void addTelemetry() {
        launcher.addTelemetry(telemetry, "launcher");
        intake.addTelemetry(telemetry, "intake");
        drive.addTelemetry();
        vision.addTelemetry();
    }
}
