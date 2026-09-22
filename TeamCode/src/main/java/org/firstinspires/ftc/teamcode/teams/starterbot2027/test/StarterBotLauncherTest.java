package org.firstinspires.ftc.teamcode.teams.starterbot2027.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Constants;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;

/**
 * Tests the launcher motor and NOTHING else: no drive, no intake, no windmill. For finding out why
 * the wheel stops and starts in velocity mode, and for tuning its PIDF on the real robot.
 *
 * The motor is commanded ONLY when a button is pressed, never every loop, so anything it does
 * between presses is the hub's own velocity loop. It starts on goBILDA's shipped numbers (PIDF
 * 40/0/0/12.5, BRAKE), which run fine on P3 and make GG's launcher stop and start; lower P and
 * watch "swing" drop.
 *
 * Buttons (gamepad 1):
 *   A              velocity mode: hold the target speed with the PIDF below
 *   Y              full power, no encoder control: read off the top speed and the F it implies
 *   B              stop
 *   X              zero-power BRAKE / FLOAT
 *   D-pad up/down  P up / down (by 5 above 10, by 1 below)
 *   D-pad right/left  F up / down by 0.1
 *   bumper right/left I up / down by 0.1
 *
 * When it is smooth and on target, copy P, I, F into that robot's line in StarterBotConfig (p3() or gg()).
 */
@TeleOp(name = "Starterbot: Launcher Test", group = "StarterBot2027")
public class StarterBotLauncherTest extends OpMode {

    private static final double TARGET = StarterBot2027Constants.Launcher.TARGET_VELOCITY;

    private DcMotorEx launcher;
    private double p = 40, i = 0, f = 12.5;   // goBILDA's shipped PIDF
    private boolean brake = true;          // the SDK default, which is what goBILDA's code runs with
    private String mode = "stopped";
    private String pidfOnHub = "";

    // How far the speed moved in the last second. Smooth is a few tens; on/off is hundreds.
    private final ElapsedTime swingTimer = new ElapsedTime();
    private double lo, hi, swing;

    @Override
    public void init() {
        launcher = hardwareMap.get(DcMotorEx.class, StarterBotConfig.LAUNCHER);
        launcher.setDirection(StarterBotConfig.LAUNCHER_DIR);
        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        applySettings();
    }

    @Override
    public void loop() {
        // ---- tuning buttons: change a number, send it to the hub once
        boolean changed = true;
        if      (gamepad1.dpadUpWasPressed())      p += (p >= 10 ? 5 : 1);
        else if (gamepad1.dpadDownWasPressed())    p = Math.max(0, p - (p > 10 ? 5 : 1));
        else if (gamepad1.dpadRightWasPressed())   f += 0.1;
        else if (gamepad1.dpadLeftWasPressed())    f = Math.max(0, f - 0.1);
        else if (gamepad1.rightBumperWasPressed()) i += 0.1;
        else if (gamepad1.leftBumperWasPressed())  i = Math.max(0, i - 0.1);
        else if (gamepad1.xWasPressed())           brake = !brake;
        else changed = false;
        if (changed) applySettings();

        // ---- run buttons: one command per press, then hands off
        if (gamepad1.aWasPressed()) {
            launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            launcher.setVelocity(TARGET);
            mode = "VELOCITY (hub holds the target)";
        } else if (gamepad1.yWasPressed()) {
            launcher.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            launcher.setPower(1.0);
            mode = "FULL POWER (no encoder control)";
        } else if (gamepad1.bWasPressed()) {
            launcher.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            launcher.setPower(0);
            mode = "stopped";
        }

        // ---- what the wheel is doing
        double v = launcher.getVelocity();
        if (swingTimer.seconds() > 1.0) { swing = hi - lo; lo = hi = v; swingTimer.reset(); }
        else { lo = Math.min(lo, v); hi = Math.max(hi, v); }

        telemetry.addData("mode", mode);
        telemetry.addData("velocity", "%.0f / %.0f   error %.0f", v, TARGET, TARGET - v);
        telemetry.addData("swing (last 1 s)", "%.0f ticks/s", swing);
        telemetry.addData("current", "%.1f A", launcher.getCurrent(CurrentUnit.AMPS));
        telemetry.addData("P  I  F  (D-pad, bumpers)", "%.0f   %.1f   %.1f", p, i, f);
        telemetry.addData("on the hub", pidfOnHub);
        telemetry.addData("zero power (X)", brake ? "BRAKE" : "FLOAT");
        if (mode.startsWith("FULL") && v > 100) {
            telemetry.addData("F for this wheel", "32767 / %.0f = %.1f", v, 32767 / v);
        }
        telemetry.addLine("A velocity   Y full power   B stop");
    }

    @Override
    public void stop() {
        launcher.setPower(0);
    }

    /** Send P, I, F and the zero-power choice to the hub, and read the PIDF back to prove it took. */
    private void applySettings() {
        launcher.setZeroPowerBehavior(brake ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT);
        launcher.setVelocityPIDFCoefficients(p, i, 0, f);
        pidfOnHub = launcher.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER).toString();
    }
}
