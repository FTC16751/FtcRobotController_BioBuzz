// ============================================================
//  LESSON 14 — Inheritance  (FILE 2 of 4: TestMotor subclass)
//  Book: Chapter 14, Listing 14.5
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.Telemetry;

// TODO 1: Extend L14_TestItem
//   public class L14_TestMotor extends L14_TestItem {
public class L14_TestMotor {

    private double speed;
    private DcMotor motor;

    // TODO 2: Write a constructor:
    //         L14_TestMotor(String description, double speed, DcMotor motor)
    //         Call super(description) to pass the description to the parent.
    //
    //   public L14_TestMotor(String description, double speed, DcMotor motor) {
    //       super(description);
    //       this.speed = speed;
    //       this.motor = motor;
    //   }


    // TODO 3: @Override the run() method from TestItem.
    //         When 'on' is true: set motor to 'speed'
    //         When 'on' is false: set motor to 0.0
    //         Always display the encoder position.
    //
    //   @Override
    //   public void run(boolean on, Telemetry telemetry) {
    //       if (on) {
    //           motor.setPower(speed);
    //       } else {
    //           motor.setPower(0.0);
    //       }
    //       telemetry.addData("Encoder", motor.getCurrentPosition());
    //   }

}
