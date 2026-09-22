package org.firstinspires.ftc.teamcode.common.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.common.launch.LaunchController;
import org.firstinspires.ftc.teamcode.common.subsystems.Claw;
import org.firstinspires.ftc.teamcode.common.subsystems.PresetMotor;
import org.firstinspires.ftc.teamcode.common.subsystems.PresetServo;
import org.firstinspires.ftc.teamcode.common.subsystems.Roller;
import org.firstinspires.ftc.teamcode.common.subsystems.VelocityMotor;

import java.util.ArrayList;
import java.util.List;

/**
 * Drive a prototype mechanism from gamepad 1 before any team code exists. Every device below is
 * optional: name it in the Control Hub configuration to use it, or leave it out. The telemetry
 * shows the live servo position and motor ticks so preset values can be read off and copied into
 * the team's Constants.
 *
 * Device names are goBILDA's StarterBot names where one exists; edit the constants to match your
 * configuration.
 *
 * Buttons (gamepad 1):
 *   right / left trigger   intake in / out (proportional, goBILDA style)
 *   A                      claw open / close
 *   left / right bumper    wrist to the previous / next preset
 *   left stick X (held)    wrist creeps, to find positions
 *   left stick Y           lift manual (release to hold)
 *   X / B / Y              lift to DOWN / MID / HIGH
 *   D-pad up / down        lift nudge +50 / -50 ticks
 *   Back                   lift: make here zero
 *   D-pad left             flywheel spin / stop
 *   D-pad right            fire one shot through LaunchController (spins up, feeds, done)
 */
@TeleOp(name = "Mechanism Bench Test (Common)", group = "Common Test")
@Disabled
public class MechanismBenchTest extends OpMode {

    // ---- device names: edit to match the Control Hub configuration ----
    static final String INTAKE          = "intake";
    static final String INTAKE_SERVO_L  = "left_intake_servo";
    static final String INTAKE_SERVO_R  = "right_intake_servo";
    static final String CLAW            = "claw";
    static final String WRIST           = "wrist";
    static final String LIFT            = "lift";
    static final String LIFT_2          = "lift2";
    static final String LIFT_HOME       = "lift_home";
    static final String LAUNCHER        = "launcher";
    static final String LAUNCHER_2      = "launcher2";
    static final String FEEDER_L        = "left_feeder";
    static final String FEEDER_R        = "right_feeder";

    // ---- starting guesses; read the real values off the telemetry, then put them in Constants ----
    static final double CLAW_OPEN = 0.30, CLAW_CLOSED = 0.70;
    static final String[] WRIST_PRESETS = { "A", "B", "C" };
    static final double[] WRIST_POSITIONS = { 0.20, 0.50, 0.80 };
    static final int LIFT_DOWN = 0, LIFT_MID = 1000, LIFT_HIGH = 2000, LIFT_MAX = 2500;
    static final double LAUNCH_VELOCITY = 1500;   // ticks per second
    static final double FEED_TIME_SEC = 0.3;

    private Roller intake;
    private Claw claw;
    private PresetServo wrist;
    private PresetMotor lift;
    private VelocityMotor flywheel;
    private Roller feeder;
    private LaunchController launcher;

    private final List<String> missing = new ArrayList<>();
    private int wristIndex = 0;

    @Override
    public void init() {
        DcMotorSimple intakeMotor = tryGet(DcMotorSimple.class, INTAKE);
        if (intakeMotor != null) {
            intake = new Roller(intakeMotor);
            DcMotorSimple l = tryGet(DcMotorSimple.class, INTAKE_SERVO_L);
            DcMotorSimple r = tryGet(DcMotorSimple.class, INTAKE_SERVO_R);
            if (l != null) intake.add(l);
            if (r != null) { r.setDirection(DcMotorSimple.Direction.REVERSE); intake.add(r); }
        }

        Servo clawServo = tryGet(Servo.class, CLAW);
        if (clawServo != null) claw = new Claw(clawServo, CLAW_OPEN, CLAW_CLOSED);

        Servo wristServo = tryGet(Servo.class, WRIST);
        if (wristServo != null) {
            wrist = new PresetServo(wristServo);
            for (int i = 0; i < WRIST_PRESETS.length; i++) wrist.preset(WRIST_PRESETS[i], WRIST_POSITIONS[i]);
        }

        DcMotorEx liftMotor = tryGet(DcMotorEx.class, LIFT);
        if (liftMotor != null) {
            lift = new PresetMotor(liftMotor)
                    .preset("DOWN", LIFT_DOWN).preset("MID", LIFT_MID).preset("HIGH", LIFT_HIGH)
                    .limits(0, LIFT_MAX);
            DcMotorEx second = tryGet(DcMotorEx.class, LIFT_2);
            if (second != null) { second.setDirection(DcMotorSimple.Direction.REVERSE); lift.add(second); }
            TouchSensor home = tryGet(TouchSensor.class, LIFT_HOME);
            if (home != null) lift.homeSwitch(home);
        }

        DcMotorEx launcherMotor = tryGet(DcMotorEx.class, LAUNCHER);
        if (launcherMotor != null) {
            flywheel = new VelocityMotor(launcherMotor).pidf(300, 0, 0, 10);
            DcMotorEx second = tryGet(DcMotorEx.class, LAUNCHER_2);
            if (second != null) { second.setDirection(DcMotorSimple.Direction.REVERSE); flywheel.add(second); }
        }

        DcMotorSimple feedL = tryGet(DcMotorSimple.class, FEEDER_L);
        DcMotorSimple feedR = tryGet(DcMotorSimple.class, FEEDER_R);
        if (feedL != null || feedR != null) {
            feeder = new Roller(feedL != null ? feedL : feedR);
            if (feedL != null && feedR != null) { feedR.setDirection(DcMotorSimple.Direction.REVERSE); feeder.add(feedR); }
        }

        if (flywheel != null && feeder != null) {
            launcher = new LaunchController(flywheel, feeder,
                    new LaunchController.Settings().feedTimeSec(FEED_TIME_SEC).keepSpinning(true), telemetry);
        }

        telemetry.addData("missing (fine)", missing);
        telemetry.addLine("Press START. Positions in the telemetry are what to copy into Constants.");
    }

    @Override
    public void loop() {
        if (intake != null) {
            intake.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
        }
        if (claw != null && gamepad1.aWasPressed()) claw.toggle();

        if (wrist != null) {
            if (gamepad1.rightBumperWasPressed()) { wristIndex = (wristIndex + 1) % WRIST_PRESETS.length; wrist.goTo(WRIST_PRESETS[wristIndex]); }
            if (gamepad1.leftBumperWasPressed())  { wristIndex = (wristIndex + WRIST_PRESETS.length - 1) % WRIST_PRESETS.length; wrist.goTo(WRIST_PRESETS[wristIndex]); }
            if (Math.abs(gamepad1.left_stick_x) > 0.5) wrist.nudge(0.004 * Math.signum(gamepad1.left_stick_x));
        }

        if (lift != null) {
            if (gamepad1.xWasPressed()) lift.goTo("DOWN");
            if (gamepad1.bWasPressed()) lift.goTo("MID");
            if (gamepad1.yWasPressed()) lift.goTo("HIGH");
            if (gamepad1.dpadUpWasPressed())   lift.nudge(50);
            if (gamepad1.dpadDownWasPressed()) lift.nudge(-50);
            if (gamepad1.backWasPressed())     lift.zeroHere();
            double stick = -gamepad1.left_stick_y;
            if (Math.abs(stick) > 0.1 || lift.getPresetName().equals("manual")) lift.manual(Math.abs(stick) > 0.1 ? stick : 0);
        }

        if (flywheel != null && gamepad1.dpadLeftWasPressed()) {
            if (flywheel.isSpinning()) flywheel.stop(); else flywheel.spinUp(LAUNCH_VELOCITY);
        }
        if (launcher != null) {
            launcher.update(gamepad1.dpadRightWasPressed(), LAUNCH_VELOCITY);
        } else if (feeder != null && gamepad1.dpadRightWasPressed()) {
            feeder.inFor(FEED_TIME_SEC);
        }

        // Timed things end here.
        if (intake != null) intake.update();
        if (wrist != null) wrist.update();
        if (lift != null) lift.update();
        if (feeder != null) feeder.update();

        if (intake != null)   intake.addTelemetry(telemetry, "intake");
        if (claw != null)     claw.addTelemetry(telemetry, "claw");
        if (wrist != null)    wrist.addTelemetry(telemetry, "wrist");
        if (lift != null)     lift.addTelemetry(telemetry, "lift");
        if (flywheel != null) flywheel.addTelemetry(telemetry, "flywheel");
        if (feeder != null)   feeder.addTelemetry(telemetry, "feeder");
        if (launcher != null) launcher.addTelemetry(telemetry);
        if (!missing.isEmpty()) telemetry.addData("not configured", missing);
    }

    @Override
    public void stop() {
        if (intake != null)   intake.stop();
        if (wrist != null)    wrist.stop();
        if (lift != null)     lift.stop();
        if (flywheel != null) flywheel.stop();
        if (feeder != null)   feeder.stop();
        if (launcher != null) launcher.stop();
    }

    /** hardwareMap.tryGet, remembering what was absent for the telemetry. */
    private <T> T tryGet(Class<? extends T> type, String name) {
        T device = hardwareMap.tryGet(type, name);
        if (device == null) missing.add(name);
        return device;
    }
}
