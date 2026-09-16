// ============================================================
//  LESSON 14 — Complete Solution: TestWiring OpMode (Coach Reference)
// ============================================================

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import java.util.ArrayList;

@TeleOp(name = "L14 Test Wiring SOLUTION")
@com.qualcomm.robotcore.eventloop.opmode.Disabled
public class L14_TestWiring_Solution extends OpMode {

    ArrayList<L14_TestItem_Solution> tests;
    boolean wasUp = false, wasDown = false;
    int testNum = 0;

    ArrayList<L14_TestItem_Solution> buildTests() {
        ArrayList<L14_TestItem_Solution> list = new ArrayList<>();

        DcMotor motor = hardwareMap.get(DcMotor.class, "motor");
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        list.add(new L14_TestMotor_Solution("Motor", 0.5, motor));

        DigitalChannel touch = hardwareMap.get(DigitalChannel.class, "touch_sensor");
        touch.setMode(DigitalChannel.Mode.INPUT);
        list.add(new L14_TestDigitalChannel_Solution("Touch Sensor", touch));

        return list;
    }

    @Override
    public void init() {
        tests = buildTests();
        telemetry.addLine("L14 TestWiring ready.");
        telemetry.addLine("D-pad UP/DOWN: cycle. Hold A: run test.");
        telemetry.update();
    }

    @Override
    public void loop() {
        if (gamepad1.dpad_up && !wasUp) {
            testNum--;
            if (testNum < 0) testNum = tests.size() - 1;
        }
        wasUp = gamepad1.dpad_up;

        if (gamepad1.dpad_down && !wasDown) {
            testNum++;
            if (testNum >= tests.size()) testNum = 0;
        }
        wasDown = gamepad1.dpad_down;

        telemetry.addLine("UP/DOWN: cycle | Hold A: run test");
        L14_TestItem_Solution currTest = tests.get(testNum);
        telemetry.addData("Test", currTest.getDescription());
        currTest.run(gamepad1.a, telemetry);
    }
}
