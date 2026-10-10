package org.firstinspires.ftc.teamcode.common.vision;

/**
 * Finds the centre of a BIOBUZZ AprilTag cluster from the tags a camera reports one at a time
 * (the Limelight has no cluster concept; the SDK's AprilTagProcessor does it itself). Pure math,
 * unit-tested (ClusterMathTest). See doc/VISION_TWO_CAMERAS.md section 4.
 *
 * A cluster is four tags in a row on one sticker, centres at -6.5, -2.75, +2.75, +6.5 in from the
 * cluster centre (Competition Manual TU04 Figure 9-15), ids base..base+3 in that order. Each
 * visible tag is at centre + offset * (the strip's direction), so two or more tags give the
 * direction (a least-squares line through them) and from it the centre. One tag cannot say which
 * way the strip runs, so it is reported as it is, uncorrected: up to 6.5 in off the centre.
 *
 * Positions are in the robot frame TagSighting uses: right and forward, inches. Only the
 * horizontal plane matters: the strip lies along the Hive's pivot axis.
 */
public final class ClusterMath {

    private ClusterMath() {}

    /** Tag centres along the strip, by member index (id - base), inches. */
    public static final double[] OFFSET_IN = {-6.5, -2.75, 2.75, 6.5};

    public static final class Result {
        /** How many member tags this came from. 1 means the centre is uncorrected. */
        public final int members;
        public final double rightIn, forwardIn;
        /** Degrees to turn LEFT (+) to run the robot's lateral axis along the strip, in -90..90. 0 with one member. */
        public final double squareUpDeg;
        Result(int members, double rightIn, double forwardIn, double squareUpDeg) {
            this.members = members; this.rightIn = rightIn; this.forwardIn = forwardIn; this.squareUpDeg = squareUpDeg;
        }
    }

    /**
     * @param memberIndex 0-3 for each visible tag (id - cluster base); anything else is ignored
     * @param rightIn     that tag's position, + to the robot's right
     * @param forwardIn   that tag's position, + ahead
     * @return the cluster, or null if no valid member was given
     */
    public static Result solve(int[] memberIndex, double[] rightIn, double[] forwardIn) {
        int n = 0;
        double[] o = new double[memberIndex.length], x = new double[o.length], z = new double[o.length];
        boolean[] seen = new boolean[OFFSET_IN.length];
        for (int i = 0; i < memberIndex.length; i++) {
            int m = memberIndex[i];
            if (m < 0 || m >= OFFSET_IN.length || seen[m]) continue;
            seen[m] = true;
            o[n] = OFFSET_IN[m]; x[n] = rightIn[i]; z[n] = forwardIn[i]; n++;
        }
        if (n == 0) return null;
        if (n == 1) return new Result(1, x[0], z[0], 0.0);

        double mo = 0, mx = 0, mz = 0;
        for (int i = 0; i < n; i++) { mo += o[i]; mx += x[i]; mz += z[i]; }
        mo /= n; mx /= n; mz /= n;
        double sxx = 0, sox = 0, soz = 0;
        for (int i = 0; i < n; i++) {
            sxx += (o[i] - mo) * (o[i] - mo);
            sox += (o[i] - mo) * (x[i] - mx);
            soz += (o[i] - mo) * (z[i] - mz);
        }
        double len = Math.hypot(sox, soz);
        if (len < 1e-9) return new Result(n, mx, mz, 0.0);   // tags on top of each other: no direction to use
        double ux = sox / len, uz = soz / len;               // unit vector from tag 0's side toward tag 3's side
        double s = Math.toDegrees(Math.atan2(uz, ux));       // strip seen from the other end runs the other way; square is the same
        if (s > 90) s -= 180; else if (s <= -90) s += 180;
        return new Result(n, mx - mo * ux, mz - mo * uz, s);
    }
}
