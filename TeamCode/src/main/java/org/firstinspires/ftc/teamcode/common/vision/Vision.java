package org.firstinspires.ftc.teamcode.common.vision;

import org.firstinspires.ftc.teamcode.common.CommonConstants;

/**
 * The camera a robot class talks to: AprilTags for aiming and for TagApproach, plus the loop calls
 * every robot makes. A robot class holds a Vision, not a VisionUtil, so changing the camera (the
 * Limelight today, a webcam running the SDK AprilTagProcessor later) means writing another class
 * that implements this, not editing every robot. The Limelight's own extras (pipelines, motif,
 * MegaTag) stay on VisionUtil for the code that needs them.
 */
public interface Vision extends AimTarget, TagSighting {

    /** Call first in every loop (and init_loop): reads the camera so everything after sees this loop's frame. */
    void update();

    /** Release the camera. Call from the robot's stop path. */
    void stop();

    /** Put this camera's state on the Driver Station. */
    void addTelemetry();

    /** Write the sighting to the AdvantageScope log under "group/" (the robot passes "Vision"). Free when LogUtil is off. */
    void log(String group);

    /** Point the camera at this tag if it has to be told which (the Limelight picks a pipeline per tag). Default: nothing to do. */
    default void lookForTag(int tagId) {}

    /** Which alliance's goal to aim at. Default: a camera that does not care ignores it. */
    default void setTargetingAlliance(CommonConstants.Alliance alliance) {}

    /** The tag the camera sees right now, or -1 for none. */
    int getDetectedTagId();
}
