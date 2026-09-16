// ============================================================
//  LESSON 12 — State Machines & Autonomous
//  Book: Chapter 12
//  Hardware: programming board (touch sensor + servo + motor)
// ============================================================
//
//  This OpMode runs as Autonomous. The robot will:
//    STATE 1: Wait for the touch sensor to be pressed
//    STATE 2: Move the servo to 1.0 position
//    STATE 3: Run the motor for 2 seconds
//    STATE 4: Stop and display "Done"
//
//  Key insight: We never use a while() loop.
//  Instead, loop() checks the current state each frame.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

// @Autonomous makes this appear in the Autonomous list on the Driver Station
@Autonomous(name = "L12 State Machine")
public class L12_StateMachine extends OpMode {

    // ── HARDWARE ───────────────────────────────────────────────────────────────
    DcMotor       motor;
    Servo         servo;
    DigitalChannel touchSensor;

    // ── STATE ENUM ─────────────────────────────────────────────────────────────
    // TODO 1: Define an enum called 'State' with these values:
    //         WAIT_FOR_TOUCH, MOVE_SERVO, RUN_MOTOR, DONE
    //
    //   enum State {
    //       WAIT_FOR_TOUCH,
    //       MOVE_SERVO,
    //       RUN_MOTOR,
    //       DONE
    //   }


    // TODO 2: Declare a class-level State variable named 'currentState'
    //         Initialize it to State.WAIT_FOR_TOUCH
    //
    //   State currentState = State.WAIT_FOR_TOUCH;


    // For timing the RUN_MOTOR state
    double stateStartTime = 0.0;


    // ── INIT ───────────────────────────────────────────────────────────────────
    @Override
    public void init() {
        // TODO 3: Get hardware from hardwareMap
        //   motor       = hardwareMap.get(DcMotor.class,       "motor");
        //   servo       = hardwareMap.get(Servo.class,         "servo");
        //   touchSensor = hardwareMap.get(DigitalChannel.class,"touch_sensor");
        //   touchSensor.setMode(DigitalChannel.Mode.INPUT);


        // TODO 4: Set initial hardware positions
        //   motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //   motor.setPower(0.0);
        //   servo.setPosition(0.0);


        telemetry.addLine("L12: Press PLAY when ready. Then press touch sensor to start.");
        telemetry.update();
    }


    // ── START ──────────────────────────────────────────────────────────────────
    // Runs ONCE when the driver presses PLAY.
    // Always reset your state here so re-running works correctly.
    @Override
    public void start() {
        // TODO 5: Reset currentState to WAIT_FOR_TOUCH
        //         Reset stateStartTime to getRuntime()
        //
        //   currentState = State.WAIT_FOR_TOUCH;
        //   stateStartTime = getRuntime();

    }


    // ── LOOP ───────────────────────────────────────────────────────────────────
    @Override
    public void loop() {

        // Always show the current state so we can debug easily
        // TODO 6: telemetry.addData("State", currentState);


        // TODO 7: Write a switch statement that handles each state.
        //
        //   switch (currentState) {
        //
        //       case WAIT_FOR_TOUCH:
        //           // Wait for the touch sensor to be pressed
        //           // Remember: getState() returns FALSE when pressed (inverted!)
        //           if (!touchSensor.getState()) {
        //               currentState = State.MOVE_SERVO;
        //           }
        //           break;
        //
        //       case MOVE_SERVO:
        //           // Move servo to position 1.0
        //           // Transition immediately to RUN_MOTOR, recording the start time
        //           servo.setPosition(1.0);
        //           stateStartTime = getRuntime();
        //           currentState = State.RUN_MOTOR;
        //           break;
        //
        //       case RUN_MOTOR:
        //           // Run motor at 0.5 for 2 seconds, then stop
        //           motor.setPower(0.5);
        //           if (getRuntime() >= stateStartTime + 2.0) {
        //               motor.setPower(0.0);
        //               currentState = State.DONE;
        //           }
        //           break;
        //
        //       case DONE:
        //       default:
        //           telemetry.addLine("Autonomous complete!");
        //           motor.setPower(0.0);
        //           break;
        //   }

    }
}
