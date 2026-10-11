#!/usr/bin/env python3
"""Patches a virtual_robot checkout so our unmodified OpModes run in it.

  python3 sim/patch_sim.py <path to virtual_robot checkout>

Three changes, each an exact-text replacement. If the checkout is not the pinned commit and the text
is not found, this stops with an error instead of guessing (see PINNED_COMMIT in run_sim.sh).

1. @Autonomous gets preselectTeleOp (our autos use it; the sim's annotation lacks it).
2. The MecDynamic robot's device names become ours (see RobotConfig.HardwareNames): Front_Left,
   Front_Right, Rear_Left, Rear_Right and odo. "imu" already matches.
3. Setting a pose on the Pinpoint teleports the simulated robot there. Pedro's follower.setPose goes
   through GoBildaPinpointDriver.setPosition, but the sim keeps its robot where it was, so Pedro and the
   sim disagree about where the robot is. Pedro's frame is inches from the bottom-left corner with
   heading 0 along +x; the sim's is pixels from the field centre with heading 0 facing up, so
   sim heading = Pedro heading - 90 degrees. After the move the Pinpoint's odometry baseline is resynced
   the way the sim's own click-to-place does it; without that the jump is counted as driven distance.
"""
import os, re, sys

MARK = "// ours: sim patch"


def edit(path, fn):
    s = open(path).read()
    if MARK in s:
        return False
    s = fn(s)
    open(path, "w").write(s)
    return True


def sub1(s, old, new):
    if s.count(old) != 1:
        sys.exit("patch_sim: expected exactly one match for:\n%s\nin the file being patched. "
                 "The sim changed; update patch_sim.py or pin the old commit." % old)
    return s.replace(old, new)


def autonomous(s):
    s = sub1(s, '    String group() default "";', '    String group() default "";\n    String preselectTeleOp() default "";   ' + MARK)
    return s


NAMES = {"back_left_motor": "Rear_Left", "front_left_motor": "Front_Left",
         "front_right_motor": "Front_Right", "back_right_motor": "Rear_Right", "pinpoint": "odo"}

HOOK = """        hardwareMap.setActive(false);

        """ + MARK + """: a pose set on the Pinpoint (Pedro frame) teleports the robot there.
        com.qualcomm.hardware.gobilda.GoBildaPinpointDriver.onSetPosition = pose -> {
            synchronized (DynamicMecanumBase.this) {
                x = (pose.getX(DistanceUnit.INCH) - VirtualField.HALF_FIELD_WIDTH_INCHES) * VirtualField.PIXELS_PER_INCH;
                y = (pose.getY(DistanceUnit.INCH) - VirtualField.HALF_FIELD_WIDTH_INCHES) * VirtualField.PIXELS_PER_INCH;
                headingRadians = pose.getHeading(AngleUnit.RADIANS) - Math.PI / 2.0;
                updateDisplay();
                if (chassisBody != null) {
                    org.dyn4j.geometry.Transform t = new org.dyn4j.geometry.Transform();
                    t.rotate(headingRadians);
                    t.translate(x / VirtualField.PIXELS_PER_METER, y / VirtualField.PIXELS_PER_METER);
                    chassisBody.setTransform(t);
                    chassisBody.setLinearVelocity(0, 0);
                    chassisBody.setAngularVelocity(0);
                    chassisBody.clearAccumulatedForce();
                    chassisBody.clearAccumulatedTorque();
                }
                // The resync positionWithMouseClick does, so the jump is not counted as driven distance.
                odo.update(
                        new Pose2D(DistanceUnit.METER, x/VirtualField.PIXELS_PER_METER, y/VirtualField.PIXELS_PER_METER, AngleUnit.RADIANS, headingRadians),
                        new Vel2D(DistanceUnit.METER, 0, 0, UnnormalizedAngleUnit.RADIANS, 0), new Vel2D(DistanceUnit.METER, 0, 0, UnnormalizedAngleUnit.RADIANS, 0));
                odo.setPosition(new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0));
                goBildaPinpointDriverInternal.internalUpdate(false, false);
                goBildaPinpointDriverInternal.resetEncoders();
            }
        };

        /*
         * Compute the motorTorqueToRobotForces"""


def mecanum_base(s):
    for old, new in NAMES.items():
        if '"%s"' % old not in s:
            sys.exit("patch_sim: DynamicMecanumBase no longer mentions \"%s\"" % old)
        s = s.replace('"%s"' % old, '"%s"' % new)
    s = sub1(s, """        hardwareMap.put("odo", new GoBildaPinpointDriverInternal());""",
             """        hardwareMap.put("odo", new GoBildaPinpointDriverInternal());
        """ + MARK + """: add our stand-in Decode mechanisms (sim/stubs/ours/sim/OurDevices.java) if they were compiled.
        try { Class.forName("ours.sim.OurDevices").getMethod("register", HardwareMap.class).invoke(null, hardwareMap); }
        catch (ClassNotFoundException e) { /* not compiled: drive-only sim */ }
        catch (ReflectiveOperationException e) { throw new RuntimeException(e); }""")
    return sub1(s, """        hardwareMap.setActive(false);

        /*
         * Compute the motorTorqueToRobotForces""", HOOK)


def pinpoint_driver(s):
    s = sub1(s, """    public synchronized void resetPosAndIMU(){
        setPosition(new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.RADIANS, 0));
    }""", """    public synchronized void resetPosAndIMU(){
        xPosition = 0; yPosition = 0; hOrientation = 0;   """ + MARK + """: a reset must not teleport the robot
        odo.setPosition(new Pose2D(DistanceUnit.MM, 0, 0, AngleUnit.RADIANS, 0));
    }""")
    s = sub1(s, """    public synchronized void setPosition(Pose2D pos){
        xPosition = (float)pos.getX(DistanceUnit.MM);""", """    /** """ + MARK + """: DynamicMecanumBase registers this so setPosition() moves the simulated robot. */
    public static volatile java.util.function.Consumer<Pose2D> onSetPosition = null;

    public synchronized void setPosition(Pose2D pos){
        java.util.function.Consumer<Pose2D> hook = onSetPosition;
        if (hook != null) hook.accept(pos);   // move the robot and resync odometry first, then record the pose
        xPosition = (float)pos.getX(DistanceUnit.MM);""")
    return s


def main():
    root = os.path.join(sys.argv[1], "Controller/src")
    files = [("com/qualcomm/robotcore/eventloop/opmode/Autonomous.java", autonomous),
             ("virtual_robot/robots/classes/DynamicMecanumBase.java", mecanum_base),
             ("com/qualcomm/hardware/gobilda/GoBildaPinpointDriver.java", pinpoint_driver)]
    for rel, fn in files:
        print(("patched " if edit(os.path.join(root, rel), fn) else "already patched ") + rel)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main()
