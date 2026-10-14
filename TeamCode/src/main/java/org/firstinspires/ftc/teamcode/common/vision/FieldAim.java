package org.firstinspires.ftc.teamcode.common.vision;

/**
 * Aim at a point on the field from where the odometry says the robot is, and fix the odometry
 * from a tag sighting. Pure math, no hardware; unit-tested (FieldAimTest). Works for any robot with
 * a Pinpoint: the OpMode passes the pose in, gets an angle or a position out.
 *
 * THE FIELD FRAME is described at {@link #HIVE_X_IN}: inches, heading degrees counter-clockwise
 * from +x. The Pinpoint is in this frame once the OpMode tells it where the robot is
 * (DriveUtil.setPosition) at the start, or a tag sighting fixes it (robotFromTag).
 *
 * Robot frame: forward and right in inches, like TagSighting.
 */
public final class FieldAim {

    private FieldAim() {}

    // ---- Where the Hive cells' AprilTags are when the cell is UP, from the official field CAD
    //      (field-cad-step.zip, v26-27.2, 2026-09-15; the Onshape model is in metres, y up). The Hives
    //      sit in the Frame at the middle of the field, 25.5 in apart: Red at x = -12.75, Blue at
    //      x = +12.75, each pivoting about an axis 42.9 in up that runs along x. FIELD FRAME used here:
    //      origin at the centre of the field, +x toward the Blue Hive, +y toward the SCORING side
    //      (CAD z points toward the audience, so y = -z). The tag plane is 49.7 in up when a cell is UP.
    //      A cell that is DOWN has its tag about 1.5 in nearer the pivot; ignored.
    //      The SDK's BIOBUZZ tag library has no field positions at all (every cluster sits at the
    //      origin), so the SDK cannot give a field pose from these tags; we do it with these numbers.
    //      (CommonConstants.Field.*_GOAL_* are last season's DECODE goals; nothing in BIOBUZZ is there.)
    public static final double HIVE_X_IN = 12.75, CELL_Y_IN = 12.9, UP_TAG_HEIGHT_IN = 49.7;

    /**
     * {x, y} in the field frame of the tag cluster whose base id is given (HiveMath.RED_AUDIENCE and
     * the other three), or null for any other id. Audience cells are on -y, Scoring cells on +y.
     */
    public static double[] cell(int clusterBase) {
        switch (clusterBase) {
            case HiveMath.RED_AUDIENCE:  return new double[]{-HIVE_X_IN, -CELL_Y_IN};
            case HiveMath.RED_SCORING:   return new double[]{-HIVE_X_IN,  CELL_Y_IN};
            case HiveMath.BLUE_AUDIENCE: return new double[]{ HIVE_X_IN, -CELL_Y_IN};
            case HiveMath.BLUE_SCORING:  return new double[]{ HIVE_X_IN,  CELL_Y_IN};
            default: return null;
        }
    }

    /** The way a cluster's tag faces on the field, degrees counter-clockwise from +x: Audience cells face -y, Scoring cells +y. */
    public static double tagFacingDeg(int clusterBase) {
        return clusterBase == HiveMath.RED_AUDIENCE || clusterBase == HiveMath.BLUE_AUDIENCE ? -90 : 90;
    }

    /**
     * The robot's heading from one tag: the tag faces tagFacingDeg, and squareUpLeftDeg is how far the
     * robot must turn LEFT to face it squarely (TagSighting.squareUpDegrees). Squared up, the camera
     * looks straight into the tag; this undoes that turn. Only as good as the tag's yaw reading.
     */
    public static double headingFromTag(double tagFacingDeg, double squareUpLeftDeg) {
        return wrap(tagFacingDeg - squareUpLeftDeg);
    }

    /** Degrees wrapped to [-180, 180). */
    public static double wrap(double deg) {
        return ((deg + 180) % 360 + 360) % 360 - 180;
    }

    /** Straight-line inches from the robot to the target. */
    public static double distanceIn(double robotX, double robotY, double targetX, double targetY) {
        return Math.hypot(targetX - robotX, targetY - robotY);
    }

    /**
     * How far the robot must turn, in degrees counter-clockwise (positive = turn LEFT), so that the
     * side of the robot at facingDeg points at the target. facingDeg is 0 for a front shooter,
     * 180 for one on the back, 90 for the left side.
     */
    public static double turnLeftDeg(double robotX, double robotY, double headingDeg,
                                     double targetX, double targetY, double facingDeg) {
        double toTarget = Math.toDegrees(Math.atan2(targetY - robotY, targetX - robotX));
        return wrap(toTarget - facingDeg - headingDeg);
    }

    /**
     * A camera on the robot. forwardIn / rightIn: where it sits from the robot's centre. facingDeg:
     * which way it looks, clockwise from the robot's front (180 = backwards). pitchUpDeg: tilted up
     * from level. The SDK's ftcPose (x right, y along the lens axis, z up in the image) is turned
     * into the robot's frame by {@link #tagInRobot}.
     */
    public static final class CameraMount {
        public final double forwardIn, rightIn, facingDeg, pitchUpDeg;
        public CameraMount(double forwardIn, double rightIn, double facingDeg, double pitchUpDeg) {
            this.forwardIn = forwardIn;
            this.rightIn = rightIn;
            this.facingDeg = facingDeg;
            this.pitchUpDeg = pitchUpDeg;
        }
    }

    /**
     * A tag's ftcPose (camera frame, inches) as {forward, right} inches from the robot's centre.
     * Tilting the camera up shortens how far ahead it sees and the tag's height is dropped, so only
     * the floor distance remains.
     */
    public static double[] tagInRobot(CameraMount cam, double poseX, double poseY, double poseZ) {
        double p = Math.toRadians(cam.pitchUpDeg), f = Math.toRadians(cam.facingDeg);
        double ahead = poseY * Math.cos(p) - poseZ * Math.sin(p);   // along the floor, the way the camera looks
        double side = poseX;                                        // to the camera's right
        double forward = cam.forwardIn + ahead * Math.cos(f) - side * Math.sin(f);
        double right   = cam.rightIn   + ahead * Math.sin(f) + side * Math.cos(f);
        return new double[]{forward, right};
    }

    /**
     * Where the robot's centre is, given where the tag is on the field, the robot's heading, and
     * the tag's {forward, right} from the robot. Position only: the heading is trusted, not fixed.
     */
    public static double[] robotFromTag(double tagX, double tagY, double headingDeg,
                                        double forwardIn, double rightIn) {
        double h = Math.toRadians(headingDeg);
        // forward is (cos h, sin h); right is (sin h, -cos h)
        double dx = forwardIn * Math.cos(h) + rightIn * Math.sin(h);
        double dy = forwardIn * Math.sin(h) - rightIn * Math.cos(h);
        return new double[]{tagX - dx, tagY - dy};
    }
}
