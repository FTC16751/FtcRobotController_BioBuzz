package org.firstinspires.ftc.teamcode.common;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.drive.DriveUtil;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.vision.Vision;
import org.firstinspires.ftc.teamcode.common.vision.VisionUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What every robot class for the usual robot (mecanum drive, rollers, a shooter, AprilTag vision)
 * does the same way, so a new robot only writes its own subsystems.
 *
 * This owns: the AdvantageScope log (started here, closed in stopAll), the drive and the camera,
 * and the loop: update() steps vision, drive, launcher, then each tracked roller; stopAll() stops
 * them; addTelemetry() and the log cover them too. The robot class:
 * <pre>
 *   super(hardwareMap, telemetry, MyConfig.create(), MyConstants.Logging.ENABLED);
 *   intake = track("intake", new Roller(hardwareMap, MyConfig.INTAKE, MyConfig.INTAKE_DIR));
 *   launcher = new Launcher(...);                 // the inherited field
 * </pre>
 * Anything else (a servo, an LED, a turret) is a field in the subclass, stepped from
 * {@link #onUpdate()} and parked from {@link #onStop()}. Device names stay in the robot's own
 * config file; nothing here knows one.
 *
 * Log keys: drive (DriveUtil.addLog), Battery, the launcher's own, "Vision/...", and one group per
 * tracked roller named with its label capitalised ("intake" logs under "Intake/").
 */
public abstract class RobotBase {

    public final RobotConfig config;
    public final Telemetry telemetry;
    public final DriveUtil drive;
    public final Vision vision;
    public Launcher launcher;             // the subclass assigns this; null is allowed (no shooter)

    private final Map<String, Roller> rollers = new LinkedHashMap<>();   // label -> roller, in update order

    /** The usual: the Limelight named in the config. */
    protected RobotBase(HardwareMap hardwareMap, Telemetry telemetry, RobotConfig config, boolean logging) {
        this(hardwareMap, telemetry, config, logging,
                new VisionUtil(hardwareMap, telemetry, config.hardware.limelight));
    }

    /** Another camera: any Vision. */
    protected RobotBase(HardwareMap hardwareMap, Telemetry telemetry, RobotConfig config, boolean logging,
                        Vision vision) {
        this.telemetry = telemetry;
        this.config = config;
        if (logging) {
            LogUtil.start(hardwareMap);   // one .wpilog per OpMode run, named by time and OpMode
        }
        this.drive = new DriveUtil(hardwareMap, telemetry, null, config);
        this.vision = vision;
    }

    /** Have update(), stopAll(), addTelemetry() and the log cover this roller. Returns it. */
    protected Roller track(String label, Roller roller) {
        rollers.put(label, roller);
        return roller;
    }

    /** Extra per-loop work (an LED, a turret), after the standard steps and before the log. */
    protected void onUpdate() {}

    /** Park extra mechanisms (a stopper servo). Runs before the camera stops and the log closes. */
    protected void onStop() {}

    /** Call in every loop() and init_loop(). */
    public void update() {
        vision.update();     // camera first, so this loop's drive step sees this loop's tag
        drive.update();
        if (launcher != null) launcher.update();
        for (Roller r : rollers.values()) r.update();
        onUpdate();
        addLog();
    }

    public void stopAll() {
        drive.cancel();
        drive.stop();
        if (launcher != null) launcher.stop();
        for (Roller r : rollers.values()) r.stop();
        onStop();
        vision.stop();
        LogUtil.stop();      // flush and close this run's log
    }

    /** The standard telemetry footer. Override to change what a robot shows. */
    public void addTelemetry() {
        if (launcher != null) launcher.addTelemetry(telemetry, "launcher");
        for (Map.Entry<String, Roller> e : rollers.entrySet()) e.getValue().addTelemetry(telemetry, e.getKey());
        drive.addTelemetry();
        vision.addTelemetry();
    }

    /** Point the camera at this tag if it has to be told which. */
    public void lookForTag(int tagId) {
        vision.lookForTag(tagId);
    }

    /** This loop's values for the AdvantageScope log. Nothing happens when logging is off. */
    private void addLog() {
        if (!LogUtil.isRunning()) return;
        drive.addLog();
        LogUtil.logBattery();
        if (launcher != null) launcher.log();
        for (Map.Entry<String, Roller> e : rollers.entrySet()) {
            String label = e.getKey();
            e.getValue().log(Character.toUpperCase(label.charAt(0)) + label.substring(1));
        }
        vision.log("Vision");
    }
}
