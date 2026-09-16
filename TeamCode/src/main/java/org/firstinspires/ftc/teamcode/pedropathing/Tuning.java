package org.firstinspires.ftc.teamcode.pedropathing;

import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedropathing.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedropathing.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedropathing.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedropathing.procedures.Tests;

/**
 * AutoTune's menu. Pedro 3's tuning library scans for @Tuner methods when the Robot Controller
 * app starts and lists them on the page it hosts at http://192.168.43.1:10158 (robot WiFi, any
 * browser). Each procedure walks you through its measurements and prints the Java to paste into
 * the robot's config file; Constants.java says where each piece goes. The procedures themselves
 * are the Pedro 3 Quickstart's, copied verbatim into procedures/.
 *
 * Order for a new chassis (test plan section K): Mecanum, Pinpoint, Foresight, then Tests.
 */
public class Tuning {

    /** Spins each motor and asks which way it went: names and directions for config sections 1 and 2. */
    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    /** Push forward, push left, rotate 180: pod directions and offsets for config section 4. */
    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    /** Nine driving measurements; prints the ForesightConfig lambda for config section 6c. */
    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(Constants::localizer, Constants::drivetrain);
    }

    /** Line, Curve, Hold and interpolation tests on the numbers in ACTIVE_CONFIG. */
    @Tuner
    public static Procedure tests() {
        return new Tests(Constants::drivetrain, Constants::localizer, Constants::foresight);
    }
}
