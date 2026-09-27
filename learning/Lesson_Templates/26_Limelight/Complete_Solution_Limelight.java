// ============================================================
//  LESSON 26 — Complete Solution, Part 1: Limelight (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.List;

@TeleOp(name = "L26 Limelight SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L26_Limelight_Solution extends OpMode {

    static final int PIPELINE = 0;

    private Limelight3A limelight;

    @Override
    public void init() {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(PIPELINE);
        limelight.start();

        telemetry.addLine("L26 ready. Press PLAY and hold something in front of the camera.");
        telemetry.update();
    }

    @Override
    public void loop() {
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            telemetry.addData("tx (deg)", "%.1f", result.getTx());
            telemetry.addData("ty (deg)", "%.1f", result.getTy());
            telemetry.addData("ta (%)",   "%.2f", result.getTa());
            telemetry.addData("Age (ms)", result.getStaleness());

            List<LLResultTypes.DetectorResult> balls = result.getDetectorResults();
            telemetry.addData("Balls seen", balls.size());
            int pollen = 0;
            for (LLResultTypes.DetectorResult ball : balls) {
                telemetry.addData(ball.getClassName(), "conf %.2f  x %.1f  y %.1f",
                        ball.getConfidence(), ball.getTargetXDegrees(), ball.getTargetYDegrees());
                if (ball.getClassName().equals("yellow_pollen")) {
                    pollen++;
                }
            }
            telemetry.addData("Pollen", pollen);
        } else {
            telemetry.addLine("No target");
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        limelight.stop();
    }
}
