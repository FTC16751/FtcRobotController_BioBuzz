package org.firstinspires.ftc.teamcode.teams.starterbot2027.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Constants;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBot2027Robot;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;

/**
 * STUDENT AUTO for the P3 StarterBot, using the Pinpoint. It is a state machine (Lesson 12) that
 * drives to positions (Lesson 25):
 *
 *   SHOOT           fire SHOTS elements, one after another, from where the robot starts
 *   PARK            drive away from the wall to the parking spot
 *   DONE            stop
 *
 * Same plan as goBILDA's own StarterBotAuto: start against the field wall, launch everything,
 * then drive off the wall. The difference is HOW it drives: goBILDA counts wheel encoder ticks,
 * this one asks the Pinpoint where the robot is.
 *
 * Positions are in inches from where the robot sits at PLAY, which counts as (0, 0) facing 0:
 *   X goes UP when the robot drives forward, Y goes UP when it drives LEFT,
 *   heading goes UP when it turns LEFT (counter-clockwise), in degrees.
 *
 * To change the route, change the numbers just below. To add a step, add a state to the enum, a
 * case in loop(), and set `state` to it from the step before.
 *
 * Needs the Pinpoint ("odo" in the hub config and in StarterBotConfig.p3()); without it this auto
 * only shows a warning.
 *
 * Setup: put the robot on the tape, facing where you want, and DO NOT MOVE IT during INIT (the
 * Pinpoint is calibrating its gyro). Leave room in front and to the left.
 */
@Autonomous(name = "P3 StarterBot: Pinpoint Auto", group = "StarterBot2027")
public class P3StarterBotPinpointAuto extends OpMode {

    // ---- Edit these. Inches and degrees; see the note above for which way is positive.
    private static final int    SHOTS   = 4;
    // Negative X is BACKWARD, away from the wall if the robot starts with its back to it.
    private static final double PARK_X  = -12, PARK_Y = 0, PARK_HEADING = 0;

    private enum State { SHOOT, PARK, DONE }

    private StarterBot2027Robot robot;
    private State state;
    private int shotsFired;

    @Override
    public void init() {
        robot = new StarterBot2027Robot(hardwareMap, telemetry, StarterBotConfig.p3());
        if (!robot.drive.hasPinpoint()) {
            telemetry.addLine("NO PINPOINT: check the config name 'odo' and the I2C cable");
        }
        telemetry.addData("Status", "Initialized: %s", robot.config.robotName);
    }

    @Override
    public void init_loop() {
        robot.update();
        robot.addTelemetry();
    }

    @Override
    public void start() {
        robot.drive.resetPosition();   // wherever the robot is now is (0, 0), facing 0
        shotsFired = 0;
        state = State.SHOOT;
    }

    @Override
    public void loop() {
        robot.update();   // always first: reads the Pinpoint and steers the move that is running

        switch (state) {
            case SHOOT:
                robot.launcher.spinUp(StarterBot2027Constants.Launcher.TARGET_VELOCITY);
                if (robot.launcher.shotDone()) {
                    shotsFired++;
                }
                if (shotsFired >= SHOTS) {
                    robot.launcher.spinDown();
                    robot.drive.startDriveTo(PARK_X, PARK_Y, PARK_HEADING);
                    state = State.PARK;
                } else if (!robot.launcher.isBusy()) {
                    robot.launcher.shoot();   // start the next shot (wait for speed, feed, finish)
                }
                break;

            case PARK:
                // isBusy() stays true until the robot arrives (or the move gives up on its time limit).
                if (!robot.drive.isBusy()) {
                    state = State.DONE;
                }
                break;

            case DONE:
            default:
                robot.drive.stop();
                break;
        }

        telemetry.addData("State", state);
        telemetry.addData("Shots", "%d of %d", shotsFired, SHOTS);
        telemetry.addData("Pose", "x %.1f  y %.1f  heading %.0f",
                robot.drive.getX(), robot.drive.getY(), robot.drive.getHeadingDegrees());
        robot.addTelemetry();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }
}
