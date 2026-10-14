package org.firstinspires.ftc.teamcode.common.vision;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FieldAimTest {

    @Test
    public void frontShooterTurnsLeftWhenTargetIsLeft() {
        // robot at the origin facing +x, target straight left (+y): turn 90 left
        assertEquals(90, FieldAim.turnLeftDeg(0, 0, 0, 0, 50, 0), 1e-9);
        assertEquals(-90, FieldAim.turnLeftDeg(0, 0, 0, 0, -50, 0), 1e-9);
        assertEquals(0, FieldAim.turnLeftDeg(0, 0, 0, 50, 0, 0), 1e-9);
    }

    @Test
    public void rearShooterWantsTheBackToFaceTheTarget() {
        // target dead ahead: a rear shooter must turn all the way round (either way, wrapped)
        assertEquals(-180, FieldAim.turnLeftDeg(0, 0, 0, 50, 0, 180), 1e-9);
        // already facing away from the target: nothing to do
        assertEquals(0, FieldAim.turnLeftDeg(0, 0, 180, 50, 0, 180), 1e-9);
        // heading 170 with the target behind-ish: 10 to go the short way
        assertEquals(10, FieldAim.turnLeftDeg(0, 0, 170, 50, 0, 180) , 1e-9);
    }

    @Test
    public void wrapTakesTheShortWay() {
        assertEquals(-170, FieldAim.wrap(190), 1e-9);
        assertEquals(170, FieldAim.wrap(-190), 1e-9);
        assertEquals(-180, FieldAim.wrap(180), 1e-9);
    }

    @Test
    public void rearCameraSeesBehindTheRobot() {
        // level rear camera 3 in right of centre, tag 40 in ahead of the lens and 5 in to the lens's right
        FieldAim.CameraMount rear = new FieldAim.CameraMount(0, 3, 180, 0);
        double[] t = FieldAim.tagInRobot(rear, 5, 40, 0);
        assertEquals(-40, t[0], 1e-9);          // 40 in BEHIND the robot
        assertEquals(3 - 5, t[1], 1e-9);        // the lens's right is the robot's left
    }

    @Test
    public void tiltedCameraKeepsOnlyTheFloorDistance() {
        // 45 deg up: a tag 100 in along the lens axis and level with the lens is 70.7 in away on the floor
        FieldAim.CameraMount cam = new FieldAim.CameraMount(0, 0, 0, 45);
        assertEquals(100 * Math.cos(Math.toRadians(45)), FieldAim.tagInRobot(cam, 0, 100, 0)[0], 1e-9);
        // a tag 20 in above the lens's axis point (image-up) is closer on the floor than the axis says
        assertEquals(100 * Math.cos(Math.toRadians(45)) - 20 * Math.sin(Math.toRadians(45)),
                FieldAim.tagInRobot(cam, 0, 100, 20)[0], 1e-9);
    }

    @Test
    public void robotFromTagRoundTrips() {
        // robot at (10, -20) heading 30; tag 40 forward, 5 right of it: place the tag, then recover the robot
        double h = Math.toRadians(30);
        double tagX = 10 + 40 * Math.cos(h) + 5 * Math.sin(h);
        double tagY = -20 + 40 * Math.sin(h) - 5 * Math.cos(h);
        double[] r = FieldAim.robotFromTag(tagX, tagY, 30, 40, 5);
        assertEquals(10, r[0], 1e-9);
        assertEquals(-20, r[1], 1e-9);
    }

    @Test
    public void headingZeroMeansRightIsMinusY() {
        // facing +x, a tag 10 to the right is at y = -10
        double[] r = FieldAim.robotFromTag(0, -10, 0, 0, 10);
        assertEquals(0, r[0], 1e-9);
        assertEquals(0, r[1], 1e-9);
    }

    @Test
    public void hiveCellsAreMirroredAboutTheMiddle() {
        double[] ra = FieldAim.cell(HiveMath.RED_AUDIENCE), bs = FieldAim.cell(HiveMath.BLUE_SCORING);
        assertEquals(-ra[0], bs[0], 1e-9);
        assertEquals(-ra[1], bs[1], 1e-9);
        assertEquals(null, FieldAim.cell(99));
    }

    @Test
    public void headingFromATagSeenSquareOn() {
        // robot's back to an Audience tag (facing -y), already square: it points -y
        assertEquals(-90, FieldAim.headingFromTag(FieldAim.tagFacingDeg(HiveMath.RED_AUDIENCE), 0), 1e-9);
        // needs 10 more degrees left to be square, so it is 10 degrees right of that
        assertEquals(-100, FieldAim.headingFromTag(-90, 10), 1e-9);
        assertEquals(90, FieldAim.tagFacingDeg(HiveMath.BLUE_SCORING), 0);
    }
}
