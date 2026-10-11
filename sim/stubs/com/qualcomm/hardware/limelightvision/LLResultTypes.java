package com.qualcomm.hardware.limelightvision;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

/** Sim stand-in for the Limelight SDK types. The simulated Limelight never sees a tag. */
public class LLResultTypes {
    public static class FiducialResult {
        public int getFiducialId() { return -1; }
        public double getTargetXDegrees() { return 0; }
        public double getTargetYDegrees() { return 0; }
        public double getTargetArea() { return 0; }
        public Pose3D getRobotPoseTargetSpace() { return null; }
        public Pose3D getRobotPoseFieldSpace() { return null; }
        public Pose3D getTargetPoseRobotSpace() { return null; }
        public Pose3D getTargetPoseCameraSpace() { return null; }
    }
}
