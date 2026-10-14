package org.firstinspires.ftc.teamcode.teams.starterbot2027.test;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bench test: can the goBILDA USB webcam see the BIOBUZZ Hive AprilTags, and at what range?
 * Nothing on the robot moves. Print a Hive tag cluster (3.25 in tags, ids 30-45), hold it in front
 * of the camera, and back away until the tags drop out.
 *
 * Watch the picture on the Driver Station (3 dots, Camera Stream) or FTC Dashboard
 * (http://192.168.43.1:8080). Tag outlines on it mean the detector has them.
 *
 * Tuning (gamepad 1). Blur is the usual enemy: a short exposure with more gain beats a long one.
 *   Y              exposure AUTO / MANUAL (the controls below only work in MANUAL)
 *   D-pad up/down  exposure +/- 1 ms
 *   bumper right/left  gain +/- 5
 *   D-pad right/left   decimation up / down (higher = faster and sees farther away tags less)
 *   B              clear "farthest range seen"
 *
 * When a setting works, note exposure / gain / decimation for the real vision code.
 */
@TeleOp(name = "Starterbot: Webcam Tag Test", group = "StarterBot2027")
public class StarterBotWebcamTagTest extends OpMode {

    private AprilTagProcessor tags;
    private VisionPortal portal;

    private boolean manual = true;
    private long exposureMs = 10;
    private int gain = 0;
    private float decimation = 2;
    private boolean applied;           // camera controls can only be set once it is streaming
    private double farthest;
    private String controlsStatus = "";

    @Override
    public void init() {
        tags = new AprilTagProcessor.Builder()
                .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())
                .setDrawTagOutline(true)
                .build();
        tags.setDecimation(decimation);
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, StarterBotConfig.WEBCAM))
                .setCameraResolution(new Size(640, 480))
                .addProcessor(tags)
                .build();
        FtcDashboard.getInstance().startCameraStream(portal, 15);
    }

    @Override
    public void init_loop() {
        telemetry.addData("camera", portal.getCameraState());
    }

    @Override
    public void loop() {
        if (portal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            telemetry.addData("camera", portal.getCameraState());
            return;
        }

        boolean changed = true;
        if      (gamepad1.yWasPressed())           manual = !manual;
        else if (gamepad1.dpadUpWasPressed())      exposureMs++;
        else if (gamepad1.dpadDownWasPressed())    exposureMs = Math.max(1, exposureMs - 1);
        else if (gamepad1.rightBumperWasPressed()) gain += 5;
        else if (gamepad1.leftBumperWasPressed())  gain = Math.max(0, gain - 5);
        else if (gamepad1.dpadRightWasPressed())   decimation = Math.min(4, decimation + 1);
        else if (gamepad1.dpadLeftWasPressed())    decimation = Math.max(1, decimation - 1);
        else changed = !applied;
        if (gamepad1.bWasPressed()) farthest = 0;
        if (changed) applyControls();

        List<AprilTagDetection> seen = tags.getDetections();
        for (AprilTagDetection d : seen) {
            if (d.ftcPose != null) farthest = Math.max(farthest, d.ftcPose.range);
        }

        telemetry.addData("camera", "%s   %.0f fps", portal.getCameraState(), portal.getFps());
        telemetry.addData("exposure (Y, D-pad up/down)", manual ? exposureMs + " ms  MANUAL" : "AUTO");
        telemetry.addData("gain (bumpers)", gain);
        telemetry.addData("decimation (D-pad left/right)", "%.0f", decimation);
        telemetry.addData("camera says", controlsStatus);
        telemetry.addData("tags in view", seen.size());
        for (AprilTagDetection d : seen) {
            String who = d instanceof AprilTagClusterDetection
                    ? "CLUSTER " + ((AprilTagClusterDetection) d).metadata.name
                        + " " + ((AprilTagClusterDetection) d).percentClusterFound + "%"
                    : "tag " + ((AprilTagSingleDetection) d).id
                        + " margin " + (int) ((AprilTagSingleDetection) d).decisionMargin;
            telemetry.addLine(String.format("%s  range %.1f in  bearing %.0f  yaw %.0f",
                    who, d.ftcPose.range, d.ftcPose.bearing, d.ftcPose.yaw));
        }
        telemetry.addData("farthest range seen (B clears)", "%.1f in", farthest);
    }

    @Override
    public void stop() {
        portal.close();
    }

    /** Push exposure, gain and decimation to the camera and read the limits back. */
    private void applyControls() {
        applied = true;
        tags.setDecimation(decimation);

        ExposureControl exp = portal.getCameraControl(ExposureControl.class);
        GainControl g = portal.getCameraControl(GainControl.class);
        if (exp == null || g == null) { controlsStatus = "no exposure/gain control"; return; }

        if (!manual) {
            exp.setMode(ExposureControl.Mode.AperturePriority);
        } else if (exp.setMode(ExposureControl.Mode.Manual)) {
            long lo = exp.getMinExposure(TimeUnit.MILLISECONDS), hi = exp.getMaxExposure(TimeUnit.MILLISECONDS);
            exposureMs = Math.max(lo, Math.min(hi, exposureMs));
            exp.setExposure(exposureMs, TimeUnit.MILLISECONDS);
            gain = Math.max(g.getMinGain(), Math.min(g.getMaxGain(), gain));
            g.setGain(gain);
            controlsStatus = String.format("exposure %d-%d ms, gain %d-%d", lo, hi, g.getMinGain(), g.getMaxGain());
        } else {
            controlsStatus = "manual exposure not supported by this camera";
        }
    }
}
