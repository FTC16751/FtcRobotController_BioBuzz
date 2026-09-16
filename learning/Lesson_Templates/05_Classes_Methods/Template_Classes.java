// ============================================================
//  LESSON 05 — Classes & Methods  (FILE 2 of 2)
//  The OpMode that uses RobotStatus
//  Book: Chapter 5
// ============================================================
//
//  PREREQUISITE: Complete Template_RobotStatus.java first!
//  Both files must be in your TeamCode folder.
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L05 Classes")
public class L05_Classes extends OpMode {

    // ── CLASS MEMBER: Create a RobotStatus object ──────────────────────────────
    // 'new RobotStatus()' calls the constructor you wrote.

    // TODO 1: Create a RobotStatus object named 'robot'
    //
    //   RobotStatus robot = new RobotStatus();


    // ── INIT ───────────────────────────────────────────────────────────────────
    @Override
    public void init() {
        // TODO 2: Use the setter methods to set initial values:
        //         motorPower   = 0.0
        //         servoPosition = 0.5
        //         isRunning     = false
        //
        //   robot.setMotorPower(0.0);
        //   ...


        // TODO 3: Display all three values using the getters:
        //   telemetry.addData("Motor Power", robot.getMotorPower());
        //   ...


        // TODO 4: Display how many RobotStatus objects exist (should be 1):
        //   telemetry.addData("Instance Count", RobotStatus.getInstanceCount());


        telemetry.update();
    }


    // ── LOOP ───────────────────────────────────────────────────────────────────
    @Override
    public void loop() {

        // TODO 5: Use the left stick Y to set motor power (negate Y so up = positive).
        //         The setter already clamps it, so just pass the raw value.
        //
        //   robot.setMotorPower(-gamepad1.left_stick_y);


        // TODO 6: Use the right trigger to set servo position (triggers go 0.0 → 1.0)
        //
        //   robot.setServoPosition(gamepad1.right_trigger);


        // TODO 7: Toggle isRunning when button A is pressed (you'll need edge detection
        //         from Lesson 04 — a wasAPressed boolean)
        //
        //   if (gamepad1.a && !wasAPressed) {
        //       robot.setRunning(!robot.isRunning());
        //   }
        //   wasAPressed = gamepad1.a;


        // TODO 8: Display the full robot status using toString() (display the object itself)
        //
        //   telemetry.addData("Status", robot);    // toString() is called automatically


        // TODO 9 (CHALLENGE): Try to set motorPower to 5.0 using the setter.
        //         Check what getMotorPower() actually returns — the clamp should limit it to 1.0.
        //   robot.setMotorPower(5.0);
        //   telemetry.addData("Clamped Power", robot.getMotorPower());  // should show 1.0

    }

    // You may need this for TODO 7:
    boolean wasAPressed = false;
}
