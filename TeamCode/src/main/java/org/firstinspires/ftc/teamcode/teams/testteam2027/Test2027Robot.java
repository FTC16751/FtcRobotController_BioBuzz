package org.firstinspires.ftc.teamcode.teams.testteam2027;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.vision.AimLed;
import org.firstinspires.ftc.teamcode.common.drive.DriveUtil;
import org.firstinspires.ftc.teamcode.common.launch.LaunchController;
import org.firstinspires.ftc.teamcode.common.hardware.LedUtil;
import org.firstinspires.ftc.teamcode.common.LogUtil;
import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.common.vision.VisionUtil;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

/**
 * test2027bot: the one object every OpMode for this robot creates. It owns the drive, the camera
 * and the launcher and knows how to update them each loop. Subsystems for a real game (intake,
 * arm) get added here as public fields, built from device names in Test2027BotConfig; the
 * launcher (added 2026-09-16) is the worked example.
 *
 * Rules that keep OpModes simple:
 *   - OpModes call robot.update() first thing in every loop() and init_loop().
 *   - OpModes call robot.stopAll() in stop().
 *   - OpModes never touch hardwareMap themselves.
 *
 * Every run also writes an AdvantageScope log (common/LogUtil, doc/LOGGING_ADVANTAGESCOPE.md) while
 * Test2027Constants.Logging.ENABLED is true: the drive's pose, state and motors, the tag in view and
 * the battery, once per loop. stopAll() closes the file; a run that never reaches stop() keeps what
 * was flushed.
 */
public class Test2027Robot {

    public final RobotConfig config;
    public final DriveUtil drive;
    public final VisionUtil vision;       // null-safe inside: reports nothing if the Limelight is absent
    public final Launcher launcher;       // flywheel + feeder + shot sequence + distance table
    public final LedUtil led;             // null if the config has no LED
    private final AimLed aimLed;          // LED shows lined up / turn left / turn right / no goal
    public final Telemetry telemetry;

    public Test2027Robot(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        this.config = Test2027BotConfig.create();

        if (Test2027Constants.Logging.ENABLED) {
            LogUtil.start(hardwareMap);   // one .wpilog per OpMode run, named by time and OpMode
        }
        drive  = new DriveUtil(hardwareMap, telemetry, null, config);
        vision = new VisionUtil(hardwareMap, telemetry, config.hardware.limelight);

        // The launcher: two flywheel motors held at a velocity, two feeder servos, and the shot
        // sequence. Names and directions from the config, numbers from the constants.
        launcher = new Launcher(
                new VelocityMotor(hardwareMap, Test2027BotConfig.LAUNCHER, Test2027BotConfig.LAUNCHER_DIR)
                        // launcher2 has no encoder cable (found 2026-09-16: it read 0), so it follows the
                        // first motor's power instead of running its own velocity loop. If an encoder
                        // is ever connected, change follower(...) to add(...).
                        .follower(Test2027BotConfig.LAUNCHER_2, Test2027BotConfig.LAUNCHER_2_DIR)
                        .pidf(Test2027Constants.Launcher.PIDF_P, Test2027Constants.Launcher.PIDF_I,
                              Test2027Constants.Launcher.PIDF_D, Test2027Constants.Launcher.PIDF_F)
                        .readyFraction(Test2027Constants.Launcher.READY_FRACTION),
                new Roller(hardwareMap, Test2027BotConfig.FEEDER_LEFT, Test2027BotConfig.FEEDER_LEFT_DIR)
                        .add(Test2027BotConfig.FEEDER_RIGHT, Test2027BotConfig.FEEDER_RIGHT_DIR),
                new LaunchController.Settings()
                        .feedTimeSec(Test2027Constants.Launcher.FEED_TIME_SEC)
                        .readyFraction(Test2027Constants.Launcher.READY_FRACTION)
                        .spinUpTimeoutSec(Test2027Constants.Launcher.SPIN_UP_TIMEOUT_SEC)
                        .cooldownSec(0).stallFraction(0).keepSpinning(true),   // as Skyline shot
                telemetry)
                .table(Test2027Constants.Launcher.FLYWHEEL_TABLE, Test2027Constants.Launcher.FLYWHEEL_INITIAL_FALLBACK);

        led = config.hardware.led == null ? null : new LedUtil(hardwareMap, config.hardware.led);
        aimLed = new AimLed(led, vision, Test2027Constants.Aim.LED_TOLERANCE_DEG,
                new AimLed.Colors()
                        .goalToRight(Test2027Constants.Aim.LED_GOAL_RIGHT)
                        .goalToLeft(Test2027Constants.Aim.LED_GOAL_LEFT));

        // How the robot operates comes from Constants; the beginner commands read these defaults.
        drive.setDefaultSpeeds(Test2027Constants.Drive.AUTO_DRIVE_SPEED, Test2027Constants.Drive.AUTO_TURN_SPEED);
        drive.setDefaultHoldTime(Test2027Constants.Auto.HOLD_SEC);
    }

    /** Point the camera's pipeline at this tag (goal tags and motif tags live on different pipelines). */
    public void lookForTag(int tagId) {
        vision.selectPipelineForTag(tagId);
    }

    /** Call in every loop() and init_loop(). Steps the Pinpoint, any async drive, the camera, the launcher and the LED. */
    public void update() {
        vision.update();     // camera first, so this loop's drive step sees this loop's tag
        drive.update();
        launcher.update();
        aimLed.update();     // does nothing without an LED
        addLog();
    }

    public void stopAll() {
        drive.cancel();
        drive.stop();
        launcher.stop();
        vision.stop();
        LogUtil.stop();      // flush and close this run's log
    }

    /** This loop's values for the AdvantageScope log. Nothing happens when logging is off. */
    private void addLog() {
        if (!LogUtil.isRunning()) return;
        drive.addLog();
        LogUtil.logBattery();
        LogUtil.log("Launcher/Velocity", launcher.getVelocity());
        LogUtil.log("Launcher/Target", launcher.getTargetVelocity());
        LogUtil.log("Launcher/State", launcher.getState().toString());
        boolean visible = vision.isTargetVisible();
        LogUtil.log("Vision/TagVisible", visible);
        LogUtil.log("Vision/TagId", visible ? vision.getDetectedTagId() : -1);
        if (visible && vision.canSee(vision.getDetectedTagId())) {
            LogUtil.log("Vision/Forward_in", vision.forwardInches());
            LogUtil.log("Vision/Right_in", vision.rightInches());
            LogUtil.log("Vision/SquareUp_deg", vision.squareUpDegrees());
        }
    }

    /** The standard telemetry footer for this robot. */
    public void addTelemetry() {
        launcher.addTelemetry(telemetry, "launcher");
        drive.addTelemetry();
        drive.getTagApproach().addTelemetry(telemetry);
        if (vision.isTargetVisible()) {
            int seen = vision.getDetectedTagId();
            if (vision.canSee(seen)) {
                telemetry.addData("sighting", "id %d  fwd %.1f in  right %.1f in  square %.1f deg",
                        seen, vision.forwardInches(), vision.rightInches(), vision.squareUpDegrees());
                telemetry.addData("raw", vision.sightedTagRaw());
            } else {
                telemetry.addData("sighting", "id %d seen, but the Limelight gave no robot-space pose (pipeline 3D setting)", seen);
            }
        } else {
            telemetry.addData("sighting", "no tag in view");
        }
    }
}
