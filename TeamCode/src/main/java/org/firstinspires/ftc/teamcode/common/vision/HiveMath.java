package org.firstinspires.ftc.teamcode.common.vision;

/**
 * Is a Hive cell UP (the one to shoot into) or DOWN, from one sighting of its AprilTag cluster.
 * Pure math, no hardware; unit-tested (HiveMathTest). See doc/VISION_TWO_CAMERAS.md section 4.
 *
 * A cluster id says which alliance and which cell, never which way the seesaw is tipped. The
 * height of the tag plane above the tiles says that, and the tilt of the plane says whether the
 * Hive has settled or is mid-tip.
 *
 * The caller (the webcam adapter) supplies the camera mount and the sighting; this class never
 * reads a camera, so the sign conventions of the SDK's ftcPose are the adapter's problem.
 */
public final class HiveMath {

    private HiveMath() {}

    public enum State { UP, DOWN, MOVING, NOT_SEEN }

    // Cluster base ids, one cluster of 4 tags per Cell (AprilTagGameDatabase.getBioBuzzTagLibrary()).
    public static final int RED_SCORING   = 30;
    public static final int RED_AUDIENCE  = 34;
    public static final int BLUE_AUDIENCE = 38;
    public static final int BLUE_SCORING  = 42;
    private static final int FIRST_TAG = 30, LAST_TAG = 45, TAGS_PER_CLUSTER = 4;

    /**
     * Pivot axis height above the tiles (Competition Manual TU04, section 9.6.1). The two Cells
     * are identical and 180 deg apart, so one Cell's tag is as far above this when UP as it is
     * below it when DOWN: this is the UP/DOWN threshold, whatever the tag's mounting offset is.
     */
    public static final double PIVOT_HEIGHT_IN = 43.95;
    /** Tag plane tilt from horizontal when the Hive is at rest (bi-stable at +/-30 deg). */
    public static final double SETTLED_TILT_DEG = 30.0;
    /** How far from SETTLED_TILT_DEG still counts as at rest. Tune on a practice Hive. */
    public static final double SETTLED_TOLERANCE_DEG = 5.0;

    /** Above this the Cell is UP, at or below it DOWN. */
    public static double upThresholdIn() {
        return PIVOT_HEIGHT_IN;
    }

    /**
     * Height of the sighted tag above the tiles. cameraPitchDeg is positive for a camera tilted
     * up; rangeIn and elevationDeg are the sighting's range and elevation from the camera.
     */
    public static double tagHeightIn(double cameraHeightIn, double cameraPitchDeg,
                                     double rangeIn, double elevationDeg) {
        return cameraHeightIn + rangeIn * Math.sin(Math.toRadians(elevationDeg + cameraPitchDeg));
    }

    /** True when the plane's tilt (either sign) is within the tolerance of the at-rest tilt. */
    public static boolean isSettled(double tiltDeg) {
        return Math.abs(Math.abs(tiltDeg) - SETTLED_TILT_DEG) <= SETTLED_TOLERANCE_DEG;
    }

    /** UP / DOWN once settled, MOVING while the tilt is off its at-rest value. NOT_SEEN is the caller's to report. */
    public static State stateOf(double tagHeightIn, double tiltDeg) {
        if (!isSettled(tiltDeg)) return State.MOVING;
        return tagHeightIn > upThresholdIn() ? State.UP : State.DOWN;
    }

    /** The cluster base id (30, 34, 38 or 42) that a member tag id 30-45 belongs to, or -1 for any other id. */
    public static int clusterOf(int tagId) {
        if (tagId < FIRST_TAG || tagId > LAST_TAG) return -1;
        return FIRST_TAG + (tagId - FIRST_TAG) / TAGS_PER_CLUSTER * TAGS_PER_CLUSTER;
    }
}
