// ============================================================
//  LESSON 25 — Pinpoint Odometry, Part 2: Drive by Position
//  Hardware: StarterBot with a goBILDA Pinpoint (finish Part 1 first!)
// ============================================================
//  A state machine (Lesson 12) whose states end when the robot
//  REACHES a position, instead of after a number of seconds.
//
//  Path:  start at (0, 0) facing forward
//         → drive forward until X = 24 in
//         → strafe left   until Y = 12 in
//         → turn left     until Heading = 90°
// ============================================================

package org.firstinspires.ftc.teamcode.students.moira.l25_pinpoint_odometry;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@com.qualcomm.robotcore.eventloop.opmode.Disabled   // DELETE this line when you start the lesson
@Autonomous(name = "L25 Pinpoint Auto - Moira", group = "Moira")
public class L25_PinpointAuto extends OpMode {

    // Copy these from your finished Part 1.
    static final double X_POD_OFFSET_MM = 0.0;
    static final double Y_POD_OFFSET_MM = 0.0;
    static final GoBildaPinpointDriver.EncoderDirection X_POD_DIR = GoBildaPinpointDriver.EncoderDirection.FORWARD;
    static final GoBildaPinpointDriver.EncoderDirection Y_POD_DIR = GoBildaPinpointDriver.EncoderDirection.FORWARD;

    static final double TARGET_X_IN        = 24.0;
    static final double TARGET_Y_IN        = 12.0;
    static final double TARGET_HEADING_DEG = 90.0;
    static final double DRIVE_POWER        = 0.3;   // slow: this is bang-bang, it cannot slow down by itself
    static final double TURN_POWER         = 0.25;

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private GoBildaPinpointDriver pinpoint;

    // TODO 1: Declare the states and a field holding the current one.
    //   enum State { DRIVE_FORWARD, STRAFE_LEFT, TURN_LEFT, DONE }
    //   State state = State.DRIVE_FORWARD;


    @Override
    public void init() {
        initDrive();
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
        pinpoint.setOffsets(X_POD_OFFSET_MM, Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(X_POD_DIR, Y_POD_DIR);
        pinpoint.resetPosAndIMU();

        telemetry.addLine("L25 Auto ready. Leave 3 feet of space in front and to the left.");
        telemetry.update();
    }

    @Override
    public void loop() {
        // TODO 2: Update the Pinpoint and read x, y and heading into local variables.
        //   pinpoint.update();
        //   Pose2D pose = pinpoint.getPosition();
        //   double x       = pose.getX(DistanceUnit.INCH);
        //   double y       = pose.getY(DistanceUnit.INCH);
        //   double heading = pose.getHeading(AngleUnit.DEGREES);


        // TODO 3: Write the state machine.
        //         Each state: set the drive, and when the target is reached,
        //         stop and move to the next state.
        //
        //   switch (state) {
        //       case DRIVE_FORWARD:
        //           drive(DRIVE_POWER, 0, 0);
        //           if (x >= TARGET_X_IN) {
        //               drive(0, 0, 0);
        //               state = State.STRAFE_LEFT;
        //           }
        //           break;
        //
        //       case STRAFE_LEFT:
        //           // Left is NEGATIVE "right" in drive(), and makes Y go UP.
        //           ...
        //
        //       case TURN_LEFT:
        //           // Turning left is NEGATIVE "turn" in drive(), and makes Heading go UP.
        //           ...
        //
        //       case DONE:
        //           drive(0, 0, 0);
        //           break;
        //   }


        // TODO 4: Show the state and position on telemetry.
        //   telemetry.addData("State", state);
        //   telemetry.addData("Pose", "x %.1f  y %.1f  h %.1f", x, y, heading);


        // TODO 5: Run it three times from the same spot and write down where the robot
        //         stops each time. How far PAST each target does it go? Why?
        //         (Hint: the robot keeps rolling after the power goes to zero.
        //          Lesson 24's P control is the fix; see Exercise 3.)

        telemetry.update();
    }

    // ── Drive helpers (already done — your Lesson 20 mecanum code) ─────────────────

    private void initDrive() {
        frontLeft  = hardwareMap.get(DcMotor.class, "Front_Left");
        frontRight = hardwareMap.get(DcMotor.class, "Front_Right");
        rearLeft   = hardwareMap.get(DcMotor.class, "Rear_Left");
        rearRight  = hardwareMap.get(DcMotor.class, "Rear_Right");
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        rearLeft.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor m : new DcMotor[]{frontLeft, frontRight, rearLeft, rearRight}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    /** forward: + is forward. right: + strafes right. turn: + turns clockwise (right). */
    private void drive(double forward, double right, double turn) {
        double fl = forward + right + turn;
        double fr = forward - right - turn;
        double rl = forward - right + turn;
        double rr = forward + right - turn;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                                            Math.max(Math.abs(rl), Math.abs(rr))));
        frontLeft.setPower(fl / max);
        frontRight.setPower(fr / max);
        rearLeft.setPower(rl / max);
        rearRight.setPower(rr / max);
    }
}
