/**
 * Skeleton subsystems: the five shapes every FTC mechanism we have ever built comes down to.
 * Pick by what the mechanism does, not what it is called:
 * <pre>
 *   It spins game pieces in or out         intake, feeder, indexer, conveyor    -> Roller
 *   A servo goes to a few positions         wrist, hood, stopper, diverter, flipper -> PresetServo
 *   A servo grips                           claw, gripper                       -> Claw
 *   A motor goes to a few positions         lift, slide, arm, hang, motor turret -> PresetMotor
 *   A motor holds a speed                   flywheel, shooter wheel             -> VelocityMotor
 *   A wheel plus a feeder that shoots       launcher, shooter                   -> Launcher
 * </pre>
 * Launcher is the one composite: a VelocityMotor and a Roller with the spin-up / feed / cooldown
 * sequence ({@code common.LaunchController}) and distance-table aiming built in.
 *
 * <b>How every class here behaves</b>
 * <ul>
 *   <li>Built from a device name in the team's BotConfig plus a direction; more devices are ganged
 *       with {@code add(...)} / {@code pair(...)}. Positions and speeds come from the team's
 *       Constants through {@code preset(name, value)}, {@code limits(...)}, {@code speeds(...)}.</li>
 *   <li>Nothing moves when the object is built. {@code startAt("NAME")} is the opt-in for init.
 *       (PresetMotor does zero its encoder when built, so build it with the mechanism at rest.)</li>
 *   <li>Nothing blocks: no sleep, no loops. Anything timed or sensed runs through
 *       {@code update()} which the robot class calls every loop, with {@code isBusy()} to poll.</li>
 *   <li>Preset names are plain strings, exact match. A wrong name throws at once and lists the
 *       names that exist, so a typo shows up on the first button press during bench testing.</li>
 *   <li>{@code stop()} and {@code addTelemetry(telemetry, label)} on every class.</li>
 *   <li>A second constructor takes the device object itself, so the laptop tests (and a robot
 *       class that wants to) can hand one in. See the tests for the exact contract of each.</li>
 * </ul>
 *
 * First thing to run on a new mechanism: {@code common/test/MechanismBenchTest}, which drives any
 * of these from gamepad 1 and shows the live positions so preset values can be read off.
 *
 * Adding to a robot class (see teams/testteam2027/README.md, Step 7):
 * <pre>
 *   public final Roller intake;
 *   ...
 *   intake = new Roller(hardwareMap, MyBotConfig.INTAKE, DcMotorSimple.Direction.FORWARD);
 *   ...
 *   public void update() { drive.update(); intake.update(); }
 *   public void stopAll() { drive.stop(); intake.stop(); }
 * </pre>
 */
package org.firstinspires.ftc.teamcode.common.subsystems;
