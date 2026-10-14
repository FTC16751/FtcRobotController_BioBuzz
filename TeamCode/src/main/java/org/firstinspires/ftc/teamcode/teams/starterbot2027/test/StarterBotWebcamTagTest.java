package org.firstinspires.ftc.teamcode.teams.starterbot2027.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.teams.starterbot2027.UsbVision;

/**
 * Bench test: can the goBILDA USB webcam see the BIOBUZZ Hive AprilTags, and at what range?
 * Nothing on the robot moves. Uses UsbVision, the same class the autos and teleop use.
 * Print a Hive tag cluster (3.25 in tags, ids 30-45), hold it in front
 * of the camera, and back away until the tags drop out.
 *
 * Watch the picture on the Driver Station (3 dots, Camera Stream) or FTC Dashboard
 * (http://192.168.43.1:8080). Tag outlines on it mean the detector has them.
 *
 * Tuning (gamepad 1). Blur is the usual enemy: a short exposure with more gain beats a long one.
 *   Y              exposure AUTO / MANUAL (the controls below only work in MANUAL)
 *   D-pad up/down  exposure +/- 1 ms
 *   bumper right/left  gain +/- 5
 *   D-pad right/left   decimation up / down (higher = faster and sees farther away tags less)
 *   X              look for RED / BLUE alliance tags
 *   B              clear "farthest range seen"
 *
 * When a setting works, note exposure / gain / decimation for the real vision code.
 */
@TeleOp(name = "Starterbot: Webcam Tag Test", group = "StarterBot2027")
public class StarterBotWebcamTagTest extends OpMode {

    private UsbVision vision;

    private boolean manual = true;
    private long exposureMs = UsbVision.EXPOSURE_MS;
    private int gain = UsbVision.GAIN;
    private float decimation = UsbVision.DECIMATION;
    private boolean red = true;
    private double farthest;
    private String controlsStatus = "";
    private boolean firstFrame = true;   // push the starting settings once the camera is up

    @Override
    public void init() {
        vision = new UsbVision(hardwareMap, red);
    }

    @Override
    public void init_loop() {
        telemetry.addData("camera", vision.streaming() ? "STREAMING" : "starting");
    }

    @Override
    public void loop() {
        vision.update();
        if (!vision.streaming()) { telemetry.addData("camera", "starting"); return; }

        boolean changed = firstFrame;
        firstFrame = false;
        if      (gamepad1.yWasPressed())           { manual = !manual; changed = true; }
        else if (gamepad1.dpadUpWasPressed())      { exposureMs++; changed = true; }
        else if (gamepad1.dpadDownWasPressed())    { exposureMs = Math.max(1, exposureMs - 1); changed = true; }
        else if (gamepad1.rightBumperWasPressed()) { gain += 5; changed = true; }
        else if (gamepad1.leftBumperWasPressed())  { gain = Math.max(0, gain - 5); changed = true; }
        else if (gamepad1.dpadRightWasPressed())   { decimation = Math.min(4, decimation + 1); changed = true; }
        else if (gamepad1.dpadLeftWasPressed())    { decimation = Math.max(1, decimation - 1); changed = true; }
        if (gamepad1.xWasPressed()) { red = !red; vision.setRed(red); farthest = 0; }
        if (gamepad1.bWasPressed()) farthest = 0;
        if (changed) {
            vision.setDecimation(decimation);
            controlsStatus = vision.setExposure(manual, exposureMs, gain);
        }

        if (vision.seen()) farthest = Math.max(farthest, vision.range());

        telemetry.addData("camera", "%.0f fps", vision.fps());
        telemetry.addData("exposure (Y, D-pad up/down)", manual ? exposureMs + " ms  MANUAL" : "AUTO");
        telemetry.addData("gain (bumpers)", gain);
        telemetry.addData("decimation (D-pad left/right)", "%.0f", decimation);
        telemetry.addData("camera says", controlsStatus);
        telemetry.addData("goal is up (sees a " + (red ? "RED" : "BLUE") + " tag, X)", vision.goalIsUp());
        telemetry.addData("range / bearing", "%.1f in   %.0f deg", vision.range(), vision.bearing());
        telemetry.addData("farthest range seen (B clears)", "%.1f in", farthest);
    }

    @Override
    public void stop() {
        vision.close();
    }
}
