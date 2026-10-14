package org.firstinspires.ftc.teamcode.common.vision;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.teamcode.common.CommonConstants;
import org.firstinspires.ftc.teamcode.common.LogUtil;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.concurrent.TimeUnit;

/**
 * A USB webcam looking for OUR alliance's Hive AprilTags (red 30-37, blue 38-45). It is the
 * webcam version of VisionUtil: any robot that has a USB camera instead of a Limelight hands one
 * of these to RobotBase as its {@link Vision}. It reads the camera name from the robot's config,
 * so nothing here is specific to one robot.
 *
 * The rule: a tag in view means the goal is UP; no tag means it is DOWN.
 *
 * In an AUTO, call update() in init_loop() so the camera has time to start and see, then ask
 * goalWasUp() once at PLAY (a tag seen at ANY time since the camera started counts, so one
 * missed frame does not flip the answer). Mid-auto, goalIsUp() is "seen right now".
 *
 * In TELEOP, use isTargetVisible(), getTargetAngleX() to aim and getDistanceToTagInches() for the
 * shooter speed. Distances are from the CAMERA, not the shooter, so tune the speed table against
 * them directly.
 *
 * Exposure and gain: the defaults below were picked on the bench (a dim garage). Retune with the
 * Starterbot: Webcam Tag Test OpMode and change them here.
 */
public class UsbVision implements Vision {

    public static final long EXPOSURE_MS = 45;
    public static final int  GAIN = 50;
    public static final float DECIMATION = 2;

    // Same job as VisionUtil's TAG_*_SIGN constants. The SDK's ftcPose is x RIGHT, y FORWARD,
    // bearing positive LEFT. Its yaw convention has NOT been checked on a robot: turn the robot
    // left in front of a tag, and if squareUpDegrees() should go negative but goes positive, flip this.
    private static final double TAG_SQUARE_SIGN = 1.0;

    private final AprilTagProcessor tags = new AprilTagProcessor.Builder()
            .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())
            .setDrawTagOutline(true)
            .build();
    private final VisionPortal portal;
    private final Telemetry telemetry;
    private boolean red = true;

    private boolean configured;
    private boolean everSeen;
    private AprilTagDetection best;       // closest of our alliance's detections, null if none
    private AprilTagDetection sighted;    // what canSee() last found

    /** webcamName is the camera's name in the Control Hub configuration. Starts looking for RED tags. */
    public UsbVision(HardwareMap hardwareMap, Telemetry telemetry, String webcamName) {
        this.telemetry = telemetry;
        tags.setDecimation(DECIMATION);
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, webcamName))
                .setCameraResolution(new Size(640, 480))
                .addProcessor(tags)
                .build();
        FtcDashboard.getInstance().startCameraStream(portal, 15);
    }

    /** Call every loop (init_loop too). Sets the exposure once the camera is up, then picks the closest tag. */
    @Override
    public void update() {
        sighted = null;
        best = null;
        if (!streaming()) return;
        if (!configured) { configured = true; setExposure(true, EXPOSURE_MS, GAIN); }

        for (AprilTagDetection d : tags.getDetections()) {
            if (d.ftcPose == null || !ours(d)) continue;
            if (best == null || d.ftcPose.range < best.ftcPose.range) best = d;
        }
        everSeen |= best != null;
    }

    // ---- The auto's question: is the goal up?

    /** Goal is up: one of our tags is in view right now. */
    public boolean goalIsUp()  { return best != null; }
    /** Goal was up: a tag has been seen at any point since the camera started or the alliance was set. Use at the start of an auto. */
    public boolean goalWasUp() { return everSeen; }

    // ---- AimTarget: for the shooter and the aim

    @Override public boolean isTargetVisible() { return best != null; }
    /** Inches from the camera to the tag; -1 when nothing is seen (like VisionUtil). */
    @Override public double getDistanceToTagInches() { return best == null ? -1 : best.ftcPose.range; }
    /** Degrees from the camera's centre line to the tag. Positive means the tag is to the RIGHT. */
    @Override public double getTargetAngleX() { return best == null ? 0 : -best.ftcPose.bearing; }

    @Override
    public int getDetectedTagId() {
        if (best == null) return -1;
        return best instanceof AprilTagClusterDetection ? clusterBase((AprilTagClusterDetection) best)
                                                        : ((AprilTagSingleDetection) best).id;
    }

    // ---- TagSighting: for TagApproach. Ask canSee() first; a cluster's base id (HiveMath.RED_SCORING
    //      and the others) means the whole cluster, otherwise a single tag's id.

    @Override
    public boolean canSee(int tagId) {
        sighted = null;
        for (AprilTagDetection d : tags.getDetections()) {
            if (d.ftcPose == null) continue;
            int id = d instanceof AprilTagClusterDetection ? clusterBase((AprilTagClusterDetection) d)
                                                           : ((AprilTagSingleDetection) d).id;
            if (id == tagId) { sighted = d; return true; }
        }
        return false;
    }

    @Override public double forwardInches() { return sighted == null ? 0 : sighted.ftcPose.y; }
    @Override public double rightInches()   { return sighted == null ? 0 : sighted.ftcPose.x; }
    @Override public double squareUpDegrees() { return sighted == null ? 0 : TAG_SQUARE_SIGN * sighted.ftcPose.yaw; }

    /**
     * Where the sighted tag is from the robot's centre as {forward, right} inches, using where the
     * camera is mounted; null when no tag is seen. For FieldAim (aim by position, fix the odometry).
     */
    public double[] tagInRobot(FieldAim.CameraMount mount) {
        if (best == null) return null;
        return FieldAim.tagInRobot(mount, best.ftcPose.x, best.ftcPose.y, best.ftcPose.z);
    }

    // ---- Vision

    @Override
    public void setTargetingAlliance(CommonConstants.Alliance alliance) {
        red = alliance == CommonConstants.Alliance.RED;
        everSeen = false;
    }

    @Override public void stop() { portal.close(); }

    @Override
    public void log(String group) {
        if (!LogUtil.isRunning()) return;
        LogUtil.log(group + "/TagVisible", isTargetVisible());
        LogUtil.log(group + "/TagId", getDetectedTagId());
        if (best == null) return;
        LogUtil.log(group + "/Distance_in", getDistanceToTagInches());
        LogUtil.log(group + "/AngleX_deg", getTargetAngleX());
    }

    @Override
    public void addTelemetry() {
        telemetry.addLine("--- USB camera ---");
        telemetry.addData("Camera", "%s  %.0f fps", portal.getCameraState(), portal.getFps());
        if (best == null) { telemetry.addData("Tag", "none"); return; }
        telemetry.addData("Tag", "%d  %.1f in  %.0f deg", getDetectedTagId(), getDistanceToTagInches(), getTargetAngleX());
    }

    // ---- Camera tuning (used by the bench test)

    public boolean streaming() { return portal.getCameraState() == VisionPortal.CameraState.STREAMING; }
    public void setDecimation(float d) { tags.setDecimation(d); }

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

    public double fps() { return portal.getFps(); }

    private boolean ours(AprilTagDetection d) {
        if (d instanceof AprilTagClusterDetection) {
            return ((AprilTagClusterDetection) d).metadata.name.toUpperCase().startsWith(red ? "RED" : "BLUE");
        }
        int id = ((AprilTagSingleDetection) d).id;
        return red ? (id >= 30 && id <= 37) : (id >= 38 && id <= 45);
    }

    /** A cluster has no id of its own; its first tag's id (30, 34, 38, 42) is its key, as in VisionUtil. */
    private static int clusterBase(AprilTagClusterDetection c) {
        switch (c.metadata.name.toUpperCase()) {
            case "RED SCORING":   return HiveMath.RED_SCORING;
            case "RED AUDIENCE":  return HiveMath.RED_AUDIENCE;
            case "BLUE AUDIENCE": return HiveMath.BLUE_AUDIENCE;
            default:              return HiveMath.BLUE_SCORING;
        }
    }
}
