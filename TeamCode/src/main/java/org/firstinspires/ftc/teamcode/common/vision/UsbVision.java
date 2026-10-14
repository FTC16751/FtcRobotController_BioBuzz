package org.firstinspires.ftc.teamcode.common.vision;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.concurrent.TimeUnit;

/**
 * The USB webcam looking for OUR alliance's Hive tags (red 30-37, blue 38-45).
 *
 * The rule: tag in view means the goal is UP; no tag means it is DOWN.
 *
 * In an AUTO, call update() in init_loop() so the camera has time to start and see, then ask
 * goalWasUp() once at PLAY (a tag seen at ANY time since the camera started counts, so one
 * missed frame does not flip the answer). Mid-auto, goalIsUp() is "seen right now".
 *
 * In TELEOP, call update() every loop, then use seen(), bearing() to aim and range() for the
 * shooter speed. All three are in inches / degrees, measured from the CAMERA, not the shooter.
 *   bearing: positive means the tag is to the LEFT of where the camera points (turn left).
 *
 * Exposure and gain: the defaults below were picked on the bench (garage, dim). Retune with the
 * Starterbot: Webcam Tag Test OpMode and change them here.
 */
public class UsbVision {

    public static final long EXPOSURE_MS = 25;
    public static final int  GAIN = 50;
    public static final float DECIMATION = 2;

    private final AprilTagProcessor tags = new AprilTagProcessor.Builder()
            .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())
            .setDrawTagOutline(true)
            .build();
    private final VisionPortal portal;
    private boolean red;

    private boolean configured;
    private boolean everSeen;
    private AprilTagDetection best;       // closest of our alliance's detections, null if none

    /** webcamName is the camera's name in the Control Hub configuration. */
    public UsbVision(HardwareMap hardwareMap, String webcamName, boolean redAlliance) {
        red = redAlliance;
        tags.setDecimation(DECIMATION);
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, webcamName))
                .setCameraResolution(new Size(640, 480))
                .addProcessor(tags)
                .build();
        FtcDashboard.getInstance().startCameraStream(portal, 15);
    }

    /** Call every loop (init_loop too). Sets the exposure once the camera is up, then picks the closest tag. */
    public void update() {
        if (!streaming()) { best = null; return; }
        if (!configured) { configured = true; setExposure(true, EXPOSURE_MS, GAIN); }

        best = null;
        for (AprilTagDetection d : tags.getDetections()) {
            if (d.ftcPose == null || !ours(d)) continue;
            if (best == null || d.ftcPose.range < best.ftcPose.range) best = d;
        }
        everSeen |= best != null;
    }

    /** One of our tags is in view right now. */
    public boolean seen()       { return best != null; }
    /** Goal is up: a tag is in view right now. */
    public boolean goalIsUp()   { return seen(); }
    /** Goal was up: a tag has been seen at any point since the camera started. Use at the start of an auto. */
    public boolean goalWasUp()  { return everSeen; }

    /** Inches from the camera to the tag; 0 when nothing is seen. */
    public double range()   { return best == null ? 0 : best.ftcPose.range; }
    /** Degrees to turn LEFT to face the tag (negative = turn right); 0 when nothing is seen. */
    public double bearing() { return best == null ? 0 : best.ftcPose.bearing; }

    public boolean streaming()  { return portal.getCameraState() == VisionPortal.CameraState.STREAMING; }
    public double fps()         { return portal.getFps(); }
    public void setDecimation(float d) { tags.setDecimation(d); }
    public void setRed(boolean redAlliance) { red = redAlliance; everSeen = false; }
    public void close()         { portal.close(); }

    /**
     * Manual or automatic exposure, and gain. Returns what the camera allows, or why it refused.
     * Only works once streaming(); update() calls it for you with the defaults.
     */
    public String setExposure(boolean manual, long exposureMs, int gain) {
        ExposureControl exp = portal.getCameraControl(ExposureControl.class);
        GainControl g = portal.getCameraControl(GainControl.class);
        if (exp == null || g == null) return "no exposure/gain control";
        if (!manual) { exp.setMode(ExposureControl.Mode.AperturePriority); return "auto exposure"; }
        if (!exp.setMode(ExposureControl.Mode.Manual)) return "manual exposure not supported by this camera";
        long lo = exp.getMinExposure(TimeUnit.MILLISECONDS), hi = exp.getMaxExposure(TimeUnit.MILLISECONDS);
        exp.setExposure(Math.max(lo, Math.min(hi, exposureMs)), TimeUnit.MILLISECONDS);
        g.setGain(Math.max(g.getMinGain(), Math.min(g.getMaxGain(), gain)));
        return String.format("exposure %d-%d ms, gain %d-%d", lo, hi, g.getMinGain(), g.getMaxGain());
    }

    private boolean ours(AprilTagDetection d) {
        if (d instanceof AprilTagClusterDetection) {
            return ((AprilTagClusterDetection) d).metadata.name.toUpperCase().startsWith(red ? "RED" : "BLUE");
        }
        int id = ((AprilTagSingleDetection) d).id;
        return red ? (id >= 30 && id <= 37) : (id >= 38 && id <= 45);
    }
}
