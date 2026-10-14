package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.CommonConstants;
import org.firstinspires.ftc.teamcode.common.hardware.Rumble;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.common.vision.FieldAim;
import org.firstinspires.ftc.teamcode.common.vision.HiveMath;
import org.firstinspires.ftc.teamcode.common.vision.UsbVision;
import org.firstinspires.ftc.teamcode.common.vision.VisionAim;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Constants;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Robot;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;

/**
 * The student TeleOp plus AUTO-AIM at the red goal, using the USB webcam (UsbVision). Hold X and the
 * robot turns by itself until the red goal's AprilTag is straight ahead. You still drive forward,
 * back and sideways with the left stick. The launcher runs at one fixed speed: it does not use the
 * distance yet.
 *
 * Written so you can read it top to bottom:
 *
 *   init()       runs once when you press INIT. Builds the robot.
 *   init_loop()  runs over and over until you press PLAY.
 *   loop()       runs over and over after PLAY. It just calls one method per job.
 *   stop()       runs once when you press STOP.
 *
 * To change what a button does, find the method for that job (handleDriving, handleIntake, ...)
 * and edit only that method.
 *
 * Controls (gamepad 1)
 *   Left stick      drive forward/back and strafe
 *   Right stick X   turn
 *   Left stick click  HOLD for slow mode
 *   Right trigger   intake in (push harder = faster)
 *   Left trigger    intake out
 *   Right bumper    HOLD to launch: the wheel spins up, and when it is fast enough the windmill
 *                   feeds the element. Let go and everything stops. The controller buzzes once
 *                   when the wheel is up to speed
 *   Left bumper     HOLD to run the windmill backwards (unjam). Ignored while launching
 *   X               HOLD to auto-aim at the red goal with the CAMERA (the right stick's turning is
 *                   ignored). The controller buzzes once when the goal is straight ahead
 *   Y               HOLD to auto-aim with the PINPOINT: the robot turns its back (the launcher) to the
 *                   Hive cell using only where it thinks it is. Same buzz. Works with no tag in view;
 *                   compare it with X. Needs the position to be right: see Back and A
 *   A               PRESS to fix the Pinpoint's position from the tag in view (the heading is trusted
 *                   and kept). Only while a tag is seen. The Hive swings, so treat it as a rough fix
 *   Back            PRESS to set the Pinpoint's position AND heading from the tag in view. Do this
 *                   before the first Y. The heading comes from the tag's yaw, which is unchecked: if
 *                   Y turns the wrong way after Back, that sign (TAG_SQUARE_SIGN in UsbVision) is flipped
 *
 * Field frame (from the official field CAD): origin at the field centre, +x toward the Blue Hive,
 * +y toward the Scoring side, heading counter-clockwise from +x. See FieldAim.
 */
@TeleOp(name = "P3 StarterBot: Auto-Aim Teleop", group = "StarterBot2027")
public class P3StarterBotAimTeleop extends OpMode {

    // Auto-aim tuning. Turn power = AIM_GAIN * degrees off, so 30 degrees off turns at 0.6.
    // Turns too slowly: raise AIM_GAIN. Overshoots and wiggles: lower it.
    private static final double AIM_GAIN = 0.02;
    private static final double AIM_MAX_TURN = 0.5;
    private static final double AIM_TOLERANCE_DEG = 2;   // this close counts as aimed

    // Where the camera sits on the robot: 0 in forward (NOT MEASURED, set it), 3 in right of centre,
    // looking backwards (180), tilted 45 deg up. 12 in off the ground, which the maths drops.
    private static final FieldAim.CameraMount CAMERA = new FieldAim.CameraMount(0, 3, 180, 45);
    // Which way the launcher points, clockwise from the robot's front: the back of the robot.
    private static final double LAUNCHER_FACING_DEG = 180;

    private StarterBot2027Robot robot;
    private UsbVision camera;
    private int targetCell = HiveMath.RED_AUDIENCE;   // the cluster the Pinpoint aims at: the last one the camera saw
    private Rumble readyBuzz;
    private Rumble aimedBuzz;

    @Override
    public void init() {
        // Which robot are you driving? Use StarterBotConfig.p3() or StarterBotConfig.gg().
        StarterBotConfig config = StarterBotConfig.p3();

        camera = new UsbVision(hardwareMap, telemetry, StarterBotConfig.WEBCAM);
        camera.setTargetingAlliance(CommonConstants.Alliance.RED);
        robot = new StarterBot2027Robot(hardwareMap, telemetry, config, camera);
        readyBuzz = new Rumble(gamepad1, 1);   // buzzes the controller 1 time
        aimedBuzz = new Rumble(gamepad1, 1);
        telemetry.addData("Status", "Initialized: %s", robot.config.robotName);
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void loop() {
        robot.update();   // always first: reads the sensors and updates the robot

        updateTargetCell();
        handleDriving();
        handleLauncher();
        handleIntake();
        handleReadyBuzz();
        handleAimedBuzz();
        handleResetPosition();
        handleRelocalize();

        telemetry.addData("Camera aim (hold X)", !gamepad1.x ? "off"
                : !robot.vision.isTargetVisible() ? "NO TAG IN VIEW"
                : visionAimed() ? "AIMED" : "turning");
        telemetry.addData("Pinpoint aim (hold Y)", !gamepad1.y ? "off" : pinpointAimed() ? "AIMED" : "turning");
        addPoseTelemetry();
        robot.addTelemetry();   // always last: shows the robot's numbers on the Driver Station
    }

    /** Sticks drive the robot. Click the left stick to go slow. */
    private void handleDriving() {
        // Pushing a stick forward gives a NEGATIVE y, so we flip it with a minus sign.
        double drive  = deadband(-gamepad1.left_stick_y);
        double strafe = deadband(gamepad1.left_stick_x);
        double turn   = deadband(gamepad1.right_stick_x);
        if (gamepad1.x) {
            turn = aimTurn();
        } else if (gamepad1.y) {
            turn = pinpointTurn();
        }

        double speed = StarterBot2027Constants.Drive.NORMAL_SPEED;
        if (gamepad1.left_stick_button) {
            speed = StarterBot2027Constants.Drive.SLOW_SPEED;
        }

        // The 4th number (0) is not used here.
        robot.drive.arcadeDrive(strafe, drive, turn, 0, speed);
    }

    /**
     * Turn power that points the robot at the red goal. 0 when the camera does not see the tag
     * (the robot just waits for you) or when it is already within AIM_TOLERANCE_DEG.
     */
    private double aimTurn() {
        double turn = VisionAim.turnPower(robot.vision, AIM_GAIN, AIM_TOLERANCE_DEG);
        return Math.max(-AIM_MAX_TURN, Math.min(AIM_MAX_TURN, turn));
    }

    /** True while the red goal's tag is seen and straight ahead. */
    private boolean visionAimed() {
        return robot.vision.isTargetVisible()
                && VisionAim.onTarget(robot.vision.getTargetAngleX(), AIM_TOLERANCE_DEG);
    }

    /** Degrees the robot must turn LEFT for its launcher to point at the target cell, from the Pinpoint alone. */
    private double pinpointErrorLeftDeg() {
        Pose2D p = robot.drive.getPose();
        double[] t = FieldAim.cell(targetCell);
        return FieldAim.turnLeftDeg(p.getX(DistanceUnit.INCH), p.getY(DistanceUnit.INCH),
                p.getHeading(AngleUnit.DEGREES), t[0], t[1], LAUNCHER_FACING_DEG);
    }

    /** Turn power from the Pinpoint. Positive turns right (clockwise), so a left error gives a negative power. */
    private double pinpointTurn() {
        double turn = -VisionAim.turnPower(pinpointErrorLeftDeg(), AIM_GAIN, AIM_TOLERANCE_DEG);
        return Math.max(-AIM_MAX_TURN, Math.min(AIM_MAX_TURN, turn));
    }

    private boolean pinpointAimed() {
        return VisionAim.onTarget(pinpointErrorLeftDeg(), AIM_TOLERANCE_DEG);
    }

    /** Once a cluster is seen, that is the cell the Pinpoint aims at (the camera only sees the UP cell). */
    private void updateTargetCell() {
        int id = robot.vision.getDetectedTagId();
        if (FieldAim.cell(id) != null) targetCell = id;
    }

    /**
     * Where the tag says the robot is, or null: the tag's place on the field minus where the camera
     * says the tag is from the robot, with the Pinpoint's heading.
     */
    private double[] robotFromTag() {
        double[] fromRobot = camera.tagInRobot(CAMERA);
        double[] cell = FieldAim.cell(robot.vision.getDetectedTagId());
        if (fromRobot == null || cell == null) return null;
        double heading = robot.drive.getPose().getHeading(AngleUnit.DEGREES);
        return FieldAim.robotFromTag(cell[0], cell[1], heading, fromRobot[0], fromRobot[1]);
    }

    /** Pose, distance and the Pinpoint-versus-tag comparison, for judging which aim to trust. */
    private void addPoseTelemetry() {
        Pose2D p = robot.drive.getPose();
        double x = p.getX(DistanceUnit.INCH), y = p.getY(DistanceUnit.INCH);
        double[] t = FieldAim.cell(targetCell);
        telemetry.addData("Pinpoint", "x %.1f  y %.1f  heading %.0f", x, y, p.getHeading(AngleUnit.DEGREES));
        telemetry.addData("Target cell", "%d  %.0f in away  Pinpoint turn %.1f deg left",
                targetCell, FieldAim.distanceIn(x, y, t[0], t[1]), pinpointErrorLeftDeg());
        double[] tag = robotFromTag();
        if (tag == null) {
            telemetry.addData("Tag says", "no tag");
        } else {
            telemetry.addData("Tag says", "x %.1f  y %.1f  (off by %.1f in)", tag[0], tag[1], Math.hypot(tag[0] - x, tag[1] - y));
        }
    }

    /** Right bumper launches. Left bumper unjams. Otherwise the launcher is off. */
    private void handleLauncher() {
        if (isLaunching()) {
            robot.launcher.spinUp(StarterBot2027Constants.Launcher.TARGET_VELOCITY);
            if (robot.launcher.isReady()) {
                robot.launcher.feed();       // wheel is fast enough: push an element in
            } else {
                robot.launcher.feedStop();   // still spinning up: wait
            }
        } else {
            robot.launcher.spinDown();
            if (gamepad1.left_bumper) {
                robot.launcher.feedBack();   // unjam: windmill backwards
            } else {
                robot.launcher.feedStop();
            }
        }
    }

    /** Triggers run the intake. While a launch is feeding, the intake pushes harder. */
    private void handleIntake() {
        double power = gamepad1.right_trigger - gamepad1.left_trigger;   // in minus out

        if (isLaunching() && robot.launcher.isReady()) {
            power += StarterBot2027Constants.Launcher.INTAKE_BOOST_WHILE_FEEDING;
        }
        robot.intake.setPower(power);
    }

    /** One buzz when either auto-aim reaches the goal. */
    private void handleAimedBuzz() {
        aimedBuzz.update((gamepad1.x && visionAimed()) || (gamepad1.y && pinpointAimed()));
    }

    /** One buzz on the controller when the held launch button's wheel is up to speed. */
    private void handleReadyBuzz() {
        readyBuzz.update(isLaunching() && robot.launcher.isReady());
    }

    /** Back button: position and heading from the tag in view. Does nothing when no tag is seen. */
    private void handleResetPosition() {
        if (!gamepad1.backWasPressed()) return;
        int id = robot.vision.getDetectedTagId();
        if (FieldAim.cell(id) == null || !robot.vision.canSee(id)) return;
        double heading = FieldAim.headingFromTag(FieldAim.tagFacingDeg(id), robot.vision.squareUpDegrees());
        double[] fromRobot = camera.tagInRobot(CAMERA);
        double[] cell = FieldAim.cell(id);
        double[] xy = FieldAim.robotFromTag(cell[0], cell[1], heading, fromRobot[0], fromRobot[1]);
        robot.drive.setPosition(xy[0], xy[1], heading);
    }

    /** A button: move the Pinpoint to where the tag in view says the robot is. Heading stays as the Pinpoint has it. */
    private void handleRelocalize() {
        if (!gamepad1.aWasPressed()) return;
        double[] tag = robotFromTag();
        if (tag != null) {
            robot.drive.setPosition(tag[0], tag[1], robot.drive.getPose().getHeading(AngleUnit.DEGREES));
        }
    }

    /** True while the driver is holding the launch button. */
    private boolean isLaunching() {
        return gamepad1.right_bumper;
    }

    /** Ignore tiny stick movements (a stick never rests at exactly 0). */
    private double deadband(double value) {
        if (Math.abs(value) < StarterBot2027Constants.Drive.STICK_DEADBAND) {
            return 0.0;
        }
        return value;
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
