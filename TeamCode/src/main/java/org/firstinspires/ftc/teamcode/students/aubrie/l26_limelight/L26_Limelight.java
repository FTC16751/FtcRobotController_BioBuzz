// ============================================================
//  LESSON 26 — Limelight, Part 1: What Does the Camera See?
//  Hardware: Limelight 3A plugged into a Control Hub USB port
// ============================================================
//  Config name: "limelight". Works on the StarterBot or on the
//  programming board — the robot does not move in Part 1.
// ============================================================

package org.firstinspires.ftc.teamcode.students.aubrie.l26_limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.List;

@com.qualcomm.robotcore.eventloop.opmode.Disabled   // DELETE this line when you start the lesson
@TeleOp(name = "L26 Limelight - Aubrie", group = "Aubrie")
public class L26_Limelight extends OpMode {

    // Which pipeline to run. Pipelines are set up in the Limelight's web page (see README).
    static final int PIPELINE = 0;

    // TODO 1: Declare a private Limelight3A field named 'limelight'
    //   private Limelight3A limelight;


    @Override
    public void init() {
        // TODO 2: Get the Limelight from the hardware map. Its config name is "limelight".
        //   limelight = hardwareMap.get(Limelight3A.class, "limelight");


        // TODO 3: Pick the pipeline, then start the camera sending results.
        //         Without start(), you never get any results.
        //   limelight.pipelineSwitch(PIPELINE);
        //   limelight.start();


        telemetry.addLine("L26 ready. Press PLAY and hold something in front of the camera.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 4: Get the newest result and check it is valid before using it.
        //         A result is NOT valid when the camera sees no target.
        //
        //   LLResult result = limelight.getLatestResult();
        //   if (result != null && result.isValid()) {
        //       telemetry.addData("tx (deg)", "%.1f", result.getTx());
        //       telemetry.addData("ty (deg)", "%.1f", result.getTy());
        //       telemetry.addData("ta (%)",   "%.2f", result.getTa());
        //   } else {
        //       telemetry.addLine("No target");
        //   }
        //
        //   Now move the target around and fill in the README's table:
        //   which way does tx go when the target moves RIGHT? ty when it moves UP?
        //   What happens to ta when the target comes CLOSER?


        // TODO 5: How old is this result? Show its staleness in milliseconds.
        //         Put it INSIDE the "if" from TODO 4.
        //   telemetry.addData("Age (ms)", result.getStaleness());


        // TODO 6: With the ball detector pipeline, list EVERY ball the camera sees.
        //         getDetectorResults() returns a List; a for-each loop visits each one.
        //         Put this INSIDE the "if" from TODO 4 too.
        //
        //   List<LLResultTypes.DetectorResult> balls = result.getDetectorResults();
        //   telemetry.addData("Balls seen", balls.size());
        //   for (LLResultTypes.DetectorResult ball : balls) {
        //       telemetry.addData(ball.getClassName(), "conf %.2f  x %.1f  y %.1f",
        //               ball.getConfidence(), ball.getTargetXDegrees(), ball.getTargetYDegrees());
        //   }


        // TODO 7 (CHALLENGE): Count only the yellow_pollen balls, and show the count.
        //         Hint: inside the loop, ball.getClassName().equals("yellow_pollen")


        telemetry.update();
    }

    @Override
    public void stop() {
        // TODO 8: Stop the camera when the OpMode ends.
        //   limelight.stop();
    }
}
