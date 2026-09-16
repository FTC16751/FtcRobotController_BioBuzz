// ============================================================
//  LESSON 14 — Complete Solution: TestItem (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;

abstract public class L14_TestItem_Solution {

    private String description;

    protected L14_TestItem_Solution(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    abstract public void run(boolean on, Telemetry telemetry);
}
