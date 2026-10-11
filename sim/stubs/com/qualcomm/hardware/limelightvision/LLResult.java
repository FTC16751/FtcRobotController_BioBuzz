package com.qualcomm.hardware.limelightvision;

import java.util.Collections;
import java.util.List;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

/** Sim stand-in: always an invalid (empty) result. */
public class LLResult {
    public boolean isValid() { return false; }
    public List<LLResultTypes.FiducialResult> getFiducialResults() { return Collections.emptyList(); }
    public double getTx() { return 0; }
    public double getTy() { return 0; }
    public double getTa() { return 0; }
    public double getTxNC() { return 0; }
    public double getTyNC() { return 0; }
    public Pose3D getBotpose() { return null; }
    public Pose3D getBotpose_MT2() { return null; }
}
