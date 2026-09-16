// ============================================================
//  LESSON 05 — Classes & Methods  (FILE 1 of 2)
//  The "Mechanism" class — no OpMode, no hardware yet.
//  Book: Chapter 5
// ============================================================
//
//  This class represents the status of a robot.
//  It stores data (members) and provides methods to work with it.
//
//  Save this file as: RobotStatus.java
//  (the class name MUST match the filename)
// ============================================================

package org.firstinspires.ftc.teamcode;

public class RobotStatus {

    // ── PRIVATE MEMBERS ────────────────────────────────────────────────────────
    // 'private' means only THIS class can read/write these directly.
    // Other classes must use the public getter/setter methods below.

    // TODO 1: Declare a private double named 'motorPower', initialized to 0.0


    // TODO 2: Declare a private double named 'servoPosition', initialized to 0.5


    // TODO 3: Declare a private boolean named 'isRunning', initialized to false


    // Optional: static counter to track how many RobotStatus objects exist
    private static int instanceCount = 0;


    // ── CONSTRUCTOR ────────────────────────────────────────────────────────────
    // Called automatically when someone writes: new RobotStatus()

    // TODO 4: Write a constructor that takes no parameters.
    //         Inside it, increment instanceCount by 1.
    //         (This counts how many RobotStatus objects have been made.)
    //
    //   public RobotStatus() {
    //       instanceCount++;
    //   }


    // ── GETTERS (read the private values) ─────────────────────────────────────

    // TODO 5: Write a public getter for motorPower that returns a double
    //
    //   public double getMotorPower() {
    //       return motorPower;
    //   }


    // TODO 6: Write a public getter for servoPosition (returns double)


    // TODO 7: Write a public getter for isRunning (returns boolean)


    // ── SETTERS (write the private values with validation) ────────────────────

    // TODO 8: Write a setter for motorPower.
    //         CLAMP the input so it stays between -1.0 and 1.0.
    //         Call the private clamp() method (you'll write it below).
    //
    //   public void setMotorPower(double power) {
    //       motorPower = clamp(power, -1.0, 1.0);
    //   }


    // TODO 9: Write a setter for servoPosition.
    //         Clamp it between 0.0 and 1.0.


    // TODO 10: Write a setter for isRunning (just assign — no clamping needed)


    // ── UTILITY METHODS ────────────────────────────────────────────────────────

    // TODO 11: Write a private helper method called 'clamp'.
    //          It takes (double value, double min, double max) and returns double.
    //          If value < min, return min.
    //          If value > max, return max.
    //          Otherwise, return value.
    //
    //   private double clamp(double value, double min, double max) {
    //       ...
    //   }


    // TODO 12: Write a public toString() method that returns a formatted String.
    //          This is called automatically when you do telemetry.addData("s", robotStatus).
    //
    //   @Override
    //   public String toString() {
    //       return "Power=" + motorPower + " Servo=" + servoPosition + " Running=" + isRunning;
    //   }


    // TODO 13 (STATIC): Write a public STATIC method named 'getInstanceCount'
    //                   that returns instanceCount.
    //                   Called as: RobotStatus.getInstanceCount()   (no object needed)
    //
    //   public static int getInstanceCount() {
    //       return instanceCount;
    //   }

}
