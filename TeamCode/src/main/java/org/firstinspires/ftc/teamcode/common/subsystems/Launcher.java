package org.firstinspires.ftc.teamcode.common.subsystems;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.AimTarget;
import org.firstinspires.ftc.teamcode.common.Clock;
import org.firstinspires.ftc.teamcode.common.FlywheelVelocityModel;
import org.firstinspires.ftc.teamcode.common.LaunchController;

/**
 * A whole shooter in one object: a flywheel ({@link VelocityMotor}), a feeder ({@link Roller}),
 * the spin-up / feed / cooldown sequence ({@code common.LaunchController}) and, if a table is
 * given, distance-to-velocity aiming ({@code common.FlywheelVelocityModel}).
 * <pre>
 *   launcher = new Launcher(
 *           new VelocityMotor(hardwareMap, LAUNCHER, REVERSE).add(LAUNCHER_2, FORWARD).pidf(300, 0, 0, 10),
 *           new Roller(hardwareMap, FEEDER_LEFT, REVERSE).add(FEEDER_RIGHT, FORWARD),
 *           new LaunchController.Settings().feedTimeSec(0.45),
 *           telemetry)
 *       .table(MyConstants.Launcher.FLYWHEEL_TABLE, MyConstants.Launcher.FLYWHEEL_INITIAL_FALLBACK);
 *
 *   // TeleOp, one line each
 *   if (gamepad1.dpadRightWasPressed()) robot.launcher.spinUp(NEAR);   // wheel on at a preset
 *   robot.launcher.aim(robot.vision);                                   // or: follow the table
 *   robot.launcher.shoot(gamepad1.right_trigger > 0.5);                 // hold to fire repeatedly
 *   // Auto
 *   robot.launcher.shoot();  ...  if (robot.launcher.shotDone()) next();
 * </pre>
 * Nothing moves when it is built. The wheel spins only after {@link #spinUp} or {@link #aim};
 * a shot only after {@link #shoot}. Nothing blocks: the sequence runs inside {@link #update()},
 * which the robot class calls every loop. The velocity unit is the flywheel's (ticks per second).
 */
public class Launcher {

    private final VelocityMotor wheel;
    private final Roller feeder;
    private final LaunchController sequence;
    private FlywheelVelocityModel model;     // null until table(...) is set

    private double velocity = 0;             // the velocity the wheel is asked for and the next shot uses
    private boolean spinning = false;        // the driver wants the wheel on
    private boolean shotRequested = false;   // set by shoot(), consumed by update()
    private boolean shotDone = false;        // true for the one loop in which a shot completed

    public Launcher(VelocityMotor wheel, Roller feeder, LaunchController.Settings settings, Telemetry telemetry) {
        this(wheel, feeder, settings, telemetry, Clock.SYSTEM);
    }

    /** For tests: a clock that can be advanced by hand. */
    public Launcher(VelocityMotor wheel, Roller feeder, LaunchController.Settings settings, Telemetry telemetry, Clock clock) {
        this.wheel = wheel;
        this.feeder = feeder;
        this.sequence = new LaunchController(wheel, feeder, settings, telemetry, clock);
    }

    // ---------------------------------------------------------------- setup (fluent)

    /** Distance-to-velocity rows ({inches, velocity}) for aim(), and the velocity to use before the goal is ever seen. */
    public Launcher table(double[][] distanceToVelocity, double initialFallback) {
        this.model = new FlywheelVelocityModel(distanceToVelocity, initialFallback);
        if (velocity == 0) velocity = initialFallback;
        return this;
    }

    // ---------------------------------------------------------------- beginner commands

    /** Wheel on at this velocity, and shoot at it until told otherwise. */
    public void spinUp(double velocity) {
        this.velocity = velocity;
        this.spinning = velocity != 0;
        wheel.spinUp(velocity);
    }

    /** Wheel off (it coasts down). The remembered velocity stays for the next spinUp(). */
    public void spinDown() {
        spinning = false;
        if (!sequence.isBusy()) wheel.stop();
    }

    /** Change the velocity by this much, for a driver's up/down buttons. */
    public void nudge(double delta) {
        spinUp(velocity + delta);
    }

    /**
     * Pick the velocity from the table for how far away the goal is (last good value if it is
     * not in view), and spin the wheel at it. Call every loop while aiming. Returns the velocity.
     */
    public double aim(AimTarget target) {
        if (model == null) throw new IllegalStateException("aim() needs a table(...); none was set on this Launcher");
        spinUp(model.update(target));
        return velocity;
    }

    /** Fire one shot at the current velocity (spin up if needed, feed, cool down). Non-blocking. */
    public void shoot() { shoot(true); }

    /** Hold true to fire repeatedly, pulse it to fire once, false does nothing. For a trigger. */
    public void shoot(boolean fire) {
        if (fire) shotRequested = true;
    }

    /** True for the one loop in which a shot finished. */
    public boolean shotDone() { return shotDone; }

    /** True from shoot() until the shot is done or aborted. */
    public boolean isBusy() { return sequence.isBusy(); }

    /** True once the wheel is at its velocity, for a driver's ready light. */
    public boolean isReady() { return wheel.isReady(); }

    public boolean isSpinning() { return spinning; }

    /** Run the feeder by hand (unjamming, a manual shot). Ignored while a shot is in progress. */
    public void feed()     { if (!sequence.isBusy()) feeder.in(); }
    public void feedBack() { if (!sequence.isBusy()) feeder.out(); }
    public void feedStop() { if (!sequence.isBusy()) feeder.stop(); }

    /** Everything off: wheel, feeder, and the sequence back to idle. */
    public void stop() {
        spinning = false;
        shotRequested = false;
        sequence.stop();
    }

    /** Call every loop. Steps the shot sequence and any timed feeder run. */
    public void update() {
        shotDone = sequence.update(shotRequested, velocity);
        shotRequested = false;
        if (!sequence.isBusy() && !spinning) wheel.stop();   // a finished shot must not leave the wheel running after spinDown()
        feeder.update();
    }

    // ---------------------------------------------------------------- readings

    public double getVelocity()        { return wheel.getVelocity(); }
    public double getTargetVelocity()  { return velocity; }
    public LaunchController.State getState() { return sequence.getState(); }
    public int getShotsFired()         { return sequence.getShotsFired(); }
    public String getAimSource()       { return model == null ? "no table" : model.getLastSource(); }

    public void addTelemetry(Telemetry telemetry, String label) {
        telemetry.addData(label, "%s  %.0f / %.0f%s  aim %s", sequence.getState(), wheel.getVelocity(), velocity,
                wheel.isReady() ? "  READY" : "", getAimSource());
        wheel.addTelemetry(telemetry, label + " wheel");
        feeder.addTelemetry(telemetry, label + " feeder");
        telemetry.addData(label + " shots", "fired %d / attempted %d / aborted %d",
                sequence.getShotsFired(), sequence.getShotsAttempted(), sequence.getShotsAborted());
        if (!sequence.getLastAbortReason().isEmpty()) {
            telemetry.addData(label + " last abort", sequence.getLastAbortReason());
        }
    }
}
