// ============================================================
//  LESSON 25 — Complete Solution, Part 2: Pinpoint Auto (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@Autonomous(name = "L25 Pinpoint Auto SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L25_PinpointAuto_Solution extends OpMode {

    static final double X_POD_OFFSET_MM = 0.0;   // measure on the StarterBot
    static final double Y_POD_OFFSET_MM = 0.0;
    static final GoBildaPinpointDriver.EncoderDirection X_POD_DIR = GoBildaPinpointDriver.EncoderDirection.FORWARD;
    static final GoBildaPinpointDriver.EncoderDirection Y_POD_DIR = GoBildaPinpointDriver.EncoderDirection.FORWARD;

    static final double TARGET_X_IN        = 24.0;
    static final double TARGET_Y_IN        = 12.0;
    static final double TARGET_HEADING_DEG = 90.0;
    static final double DRIVE_POWER        = 0.3;
    static final double TURN_POWER         = 0.25;

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private GoBildaPinpointDriver pinpoint;

    enum State { DRIVE_FORWARD, STRAFE_LEFT, TURN_LEFT, DONE }
    State state = State.DRIVE_FORWARD;

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
        pinpoint.update();
        Pose2D pose = pinpoint.getPosition();
        double x       = pose.getX(DistanceUnit.INCH);
        double y       = pose.getY(DistanceUnit.INCH);
        double heading = pose.getHeading(AngleUnit.DEGREES);

        switch (state) {
            case DRIVE_FORWARD:
                drive(DRIVE_POWER, 0, 0);
                if (x >= TARGET_X_IN) {
                    drive(0, 0, 0);
                    state = State.STRAFE_LEFT;
                }
                break;

            case STRAFE_LEFT:
                drive(0, -DRIVE_POWER, 0);
                if (y >= TARGET_Y_IN) {
                    drive(0, 0, 0);
                    state = State.TURN_LEFT;
                }
                break;

            case TURN_LEFT:
                drive(0, 0, -TURN_POWER);
                if (heading >= TARGET_HEADING_DEG) {
                    drive(0, 0, 0);
                    state = State.DONE;
                }
                break;

            case DONE:
            default:
                drive(0, 0, 0);
                break;
        }

        telemetry.addData("State", state);
        telemetry.addData("Pose", "x %.1f  y %.1f  h %.1f", x, y, heading);
        telemetry.update();
    }

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
