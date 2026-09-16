// ============================================================
//  LESSON 14 — Complete Solution: TestDigitalChannel (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class L14_TestDigitalChannel_Solution extends L14_TestItem_Solution {

    private final DigitalChannel channel;

    public L14_TestDigitalChannel_Solution(String description, DigitalChannel channel) {
        super(description);
        this.channel = channel;
    }

    @Override
    public void run(boolean on, Telemetry telemetry) {
        // 'on' is ignored for sensors — we always read
        boolean isPressed = !channel.getState();  // inverted logic
        telemetry.addData("isPressed", isPressed);
    }
}
