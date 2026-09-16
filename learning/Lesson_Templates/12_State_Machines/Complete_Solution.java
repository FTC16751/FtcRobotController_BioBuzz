// ============================================================
//  LESSON 12 — Complete Solution (Coach Reference)
//  Book: Chapter 12
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name = "L12 State Machine SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L12_StateMachine_Solution extends OpMode {

    DcMotor       motor;
    Servo         servo;
    DigitalChannel touchSensor;

    enum State { WAIT_FOR_TOUCH, MOVE_SERVO, RUN_MOTOR, DONE }
    State currentState = State.WAIT_FOR_TOUCH;
    double stateStartTime = 0.0;

    @Override
    public void init() {
        motor       = hardwareMap.get(DcMotor.class,        "motor");
        servo       = hardwareMap.get(Servo.class,          "servo");
        touchSensor = hardwareMap.get(DigitalChannel.class, "touch_sensor");
        touchSensor.setMode(DigitalChannel.Mode.INPUT);

        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setPower(0.0);
        servo.setPosition(0.0);

        telemetry.addLine("L12 ready. Press PLAY, then touch the sensor.");
        telemetry.update();
    }

    @Override
    public void start() {
        currentState   = State.WAIT_FOR_TOUCH;
        stateStartTime = getRuntime();
    }

    @Override
    public void loop() {
        telemetry.addData("State", currentState);
        telemetry.addData("State time", "%.2f sec", getRuntime() - stateStartTime);

        switch (currentState) {
            case WAIT_FOR_TOUCH:
                if (!touchSensor.getState()) {  // false = pressed
                    currentState = State.MOVE_SERVO;
                    stateStartTime = getRuntime();
                }
                break;

            case MOVE_SERVO:
                servo.setPosition(1.0);
                stateStartTime = getRuntime();
                currentState   = State.RUN_MOTOR;
                break;

            case RUN_MOTOR:
                motor.setPower(0.5);
                if (getRuntime() >= stateStartTime + 2.0) {
                    motor.setPower(0.0);
                    currentState = State.DONE;
                }
                break;

            case DONE:
            default:
                telemetry.addData("Total runtime", "%.2f sec", getRuntime());
                telemetry.addLine("Autonomous complete!");
                motor.setPower(0.0);
                break;
        }
    }
}
