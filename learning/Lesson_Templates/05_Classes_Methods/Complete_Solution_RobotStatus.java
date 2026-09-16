// ============================================================
//  LESSON 05 — Complete Solution: RobotStatus (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

public class RobotStatus_Solution {

    private double motorPower    = 0.0;
    private double servoPosition = 0.5;
    private boolean isRunning    = false;
    private static int instanceCount = 0;

    public RobotStatus_Solution() {
        instanceCount++;
    }

    public double getMotorPower()    { return motorPower; }
    public double getServoPosition() { return servoPosition; }
    public boolean isRunning()       { return isRunning; }

    public void setMotorPower(double power) {
        motorPower = clamp(power, -1.0, 1.0);
    }

    public void setServoPosition(double position) {
        servoPosition = clamp(position, 0.0, 1.0);
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    @Override
    public String toString() {
        return "Power=" + motorPower + " Servo=" + servoPosition + " Running=" + isRunning;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }
}
