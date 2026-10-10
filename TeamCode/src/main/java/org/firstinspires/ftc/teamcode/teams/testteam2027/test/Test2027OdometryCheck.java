package org.firstinspires.ftc.teamcode.teams.testteam2027.test;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.common.LogUtil;
import org.firstinspires.ftc.teamcode.common.drive.EncoderOdometry;
import org.firstinspires.ftc.teamcode.common.drive.OdometryWatchdog;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027BotConfig;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Constants;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027Robot;

/**
 * Drive the robot (or push it by hand) and watch three estimates of where it is: the Pinpoint, the
 * SparkFun OTOS, and the wheel encoders + Control Hub IMU. OdometryWatchdog compares how far and
 * how much each one moved every half second and names the odd one out. Nothing here changes how
 * the robot drives; it is a bench check.
 *
 * Before the first run (Test2027BotConfig step 8): add the OTOS to the Control Hub configuration
 * as the "SparkFun OTOS" device type on an I2C port of its own (not the Pinpoint's),
 * mount it 10 mm (+-1) above the tiles and flat, and run the Pedro OTOS tuner to get the scalars
 * and offset. Keep the robot still while the OTOS calibrates its gyro (about 0.6 s in init).
 *
 * Things to try, in order:
 *   1. Sit still. All three should stay near zero; the OTOS flags should be clear.
 *   2. Push it straight about 4 ft. Compare the TOTAL distances at the end: the Pinpoint should
 *      read what the tape measure does; the OTOS within a few percent; the wheels a little off.
 *   3. Spin it in place for ten turns by hand. Total turn should be about 3600 degrees on each.
 *      An OTOS that disagrees only while turning has a wrong offset.
 *   4. Lift one Pinpoint pod off the floor, or drive across something slippery, and watch the
 *      suspect name. Strafing makes the wheels the odd one out; that is expected.
 *
 * Gamepad 1: sticks drive (like the TeleOp), left bumper slow.
 *   A  zero all three and the watchdog          B  recalibrate the OTOS gyro (robot still!)
 */
@TeleOp(name = "Test2027: Odometry Check", group = "TestTeam2027")
public class Test2027OdometryCheck extends OpMode {

    private Test2027Robot robot;
    private SparkFunOTOS otos;
    private EncoderOdometry wheels;
    private OdometryWatchdog watchdog;
    private String otosSetupResult = "";

    @Override
    public void init() {
        robot = new Test2027Robot(hardwareMap, telemetry);
        otos = hardwareMap.tryGet(SparkFunOTOS.class, Test2027BotConfig.OTOS);
        if (otos == null) {
            otosSetupResult = "NO OTOS named \"" + Test2027BotConfig.OTOS + "\" in the configuration";
            telemetry.addLine(otosSetupResult);
            return;
        }
        configureOtos();
        double strafeScale = robot.config.calibration.strafeScale;
        wheels = new EncoderOdometry(robot.drive.leftFrontMotor, robot.drive.rightFrontMotor,
                robot.drive.leftRearMotor, robot.drive.rightRearMotor, robot.drive.imu,
                robot.config.calibration.encoderCountsPerInch, 1.0 / strafeScale);
        watchdog = new OdometryWatchdog(new String[] {"pinpoint", "otos", "wheels"}, new OdometryWatchdog.Settings());
    }

    private void configureOtos() {
        otos.setLinearUnit(DistanceUnit.INCH);
        otos.setAngularUnit(AngleUnit.DEGREES);
        otos.setOffset(new SparkFunOTOS.Pose2D(Test2027BotConfig.OTOS_OFFSET_X_IN,
                Test2027BotConfig.OTOS_OFFSET_Y_IN, Test2027BotConfig.OTOS_OFFSET_HEADING_DEG));
        boolean linear = otos.setLinearScalar(Test2027BotConfig.OTOS_LINEAR_SCALAR);
        boolean angular = otos.setAngularScalar(Test2027BotConfig.OTOS_ANGULAR_SCALAR);
        boolean calibrated = otos.calibrateImu();   // blocks about 0.6 s; the robot must be still and flat
        otos.resetTracking();
        otos.setPosition(new SparkFunOTOS.Pose2D(0, 0, 0));
        otosSetupResult = (linear && angular && calibrated) ? "OTOS configured" : "OTOS configuration FAILED (scalars "
                + linear + "/" + angular + ", gyro " + calibrated + ")";
    }

    @Override
    public void init_loop() {
        robot.update();
        telemetry.addLine(otosSetupResult);
        if (otos != null) addOtosStatus();
    }

    @Override
    public void start() {
        zeroAll();
    }

    @Override
    public void loop() {
        robot.update();
        double speed = gamepad1.left_bumper ? Test2027Constants.Drive.SLOW_SPEED : Test2027Constants.Drive.NORMAL_SPEED;
        robot.drive.arcadeDrive(gamepad1.left_stick_x, -gamepad1.left_stick_y, gamepad1.right_stick_x, 0, speed);

        if (otos == null) {
            telemetry.addLine(otosSetupResult);
            return;
        }
        if (gamepad1.aWasPressed()) zeroAll();
        if (gamepad1.bWasPressed()) { otos.calibrateImu(); zeroAll(); }

        wheels.update();
        SparkFunOTOS.Pose2D o = otos.getPosition();
        double[] x = {robot.drive.getX(), o.x, wheels.getPose().getX(DistanceUnit.INCH)};
        double[] y = {robot.drive.getY(), o.y, wheels.getPose().getY(DistanceUnit.INCH)};
        double[] h = {robot.drive.getHeadingDegrees(), o.h, wheels.getPose().getHeading(AngleUnit.DEGREES)};
        watchdog.update(getRuntime(), x, y, h);

        for (int i = 0; i < 3; i++) {
            LogUtil.logPose("Odometry/" + watchdog.name(i), x[i], y[i], h[i]);
        }
        LogUtil.log("Odometry/Suspect", watchdog.suspect() >= 0 ? watchdog.name(watchdog.suspect()) : "none");

        telemetry.addData("Watchdog", watchdog.summary());
        for (int i = 0; i < 3; i++) {
            telemetry.addData(watchdog.name(i), "x %.1f y %.1f h %.1f | total %.1f in, %.0f deg",
                    x[i], y[i], h[i], watchdog.totalDistance(i), watchdog.totalTurnDegrees(i));
        }
        addOtosStatus();
        robot.addTelemetry();
    }

    private void addOtosStatus() {
        SparkFunOTOS.Status st = otos.getStatus();
        telemetry.addData("OTOS flags", "%s%s%s%s",
                st.warnOpticalTracking ? "OPTICAL-TRACKING " : "", st.warnTiltAngle ? "TILT " : "",
                st.errorLsm ? "LSM-ERROR " : "", st.errorPaa ? "PAA-ERROR " : "");
    }

    private void zeroAll() {
        robot.drive.resetPosition();
        otos.resetTracking();
        otos.setPosition(new SparkFunOTOS.Pose2D(0, 0, 0));
        wheels.resetToOriginAtCurrentHeading();
        watchdog.reset();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
