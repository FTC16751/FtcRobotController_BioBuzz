// ============================================================
//  LESSON 06 — Complete Solution: Mechanism (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class L06_ProgrammingBoard_Solution {

    private DigitalChannel touchSensor;

    public void init(HardwareMap hwMap) {
        touchSensor = hwMap.get(DigitalChannel.class, "touch_sensor");
        touchSensor.setMode(DigitalChannel.Mode.INPUT);
    }

    public boolean isTouchSensorPressed() {
        return !touchSensor.getState();  // inverted: false = pressed
    }

    public boolean isTouchSensorReleased() {
        return touchSensor.getState();
    }
}
