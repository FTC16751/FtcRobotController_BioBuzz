// ============================================================
//  LESSON 06 — Complete Solution: OpMode (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "L06 Touch Sensor SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L06_TouchSensorOpMode_Solution extends OpMode {

    L06_ProgrammingBoard_Solution board = new L06_ProgrammingBoard_Solution();
    boolean wasTouchPressed = false;
    int pressCount = 0;
    double pressStartTime = 0.0;

    @Override
    public void init() {
        board.init(hardwareMap);
        telemetry.addLine("L06 ready.");
        telemetry.update();
    }

    @Override
    public void loop() {
        boolean isPressed = board.isTouchSensorPressed();

        telemetry.addData("Touch Pressed", isPressed);

        if (isPressed) {
            telemetry.addData("Status", "PRESSED");
        } else {
            telemetry.addData("Status", "not pressed");
        }

        if (isPressed && !wasTouchPressed) {
            pressCount++;
            pressStartTime = getRuntime();
        }
        wasTouchPressed = isPressed;
        telemetry.addData("Press count", pressCount);

        if (isPressed) {
            telemetry.addData("Hold time (sec)", getRuntime() - pressStartTime);
        }
    }
}
