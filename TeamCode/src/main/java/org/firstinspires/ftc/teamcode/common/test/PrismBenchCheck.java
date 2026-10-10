package org.firstinspires.ftc.teamcode.common.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.common.prismled.Color;
import org.firstinspires.ftc.teamcode.common.prismled.GoBildaPrismDriver;
import org.firstinspires.ftc.teamcode.common.prismled.GoBildaPrismDriver.Artboard;
import org.firstinspires.ftc.teamcode.common.prismled.GoBildaPrismDriver.LayerHeight;
import org.firstinspires.ftc.teamcode.common.prismled.PrismAnimations;

/**
 * First thing to run on a goBILDA Prism. Needs nothing saved on the Prism: it reads the device,
 * then draws animations directly, then can save them as artboards 0-2 for PrismLedSubsystem.
 *
 * Robot Configuration: add the Prism on an I2C port with the device type
 * "goBILDA Prism RGB LED Driver" (not a generic I2C device), named PRISM_NAME below. The Prism
 * also needs its own 6-30V power on its power input; the I2C cable does not power it.
 *
 * Read the status lines first:
 *   Device ID 3          the Prism answers on I2C. Anything else, or an error: wiring, port or
 *                        the wrong device type in the configuration.
 *   Firmware             goBILDA's own library adds a delay between commands on 1.0.5 or older.
 *                        If the lights flicker or skip commands on old firmware, update it.
 *   LEDs                 the strip length the Prism has. Dpad up/down chooses a number, X sends it.
 *
 * Gamepad 1:
 *   A / B / Y         layer 0: solid green / pulse blue-orange / rainbow
 *   X                 send the strip length chosen with dpad up/down
 *   Left bumper       clear all animations
 *   Dpad left/right   load artboard 0..3 (shows nothing if that slot was never saved)
 *   Left trigger held + A / B / Y   save what is showing as artboard 0 / 1 / 2
 */
@TeleOp(name = "TEST: Prism Bench Check", group = "Common Test")
public class PrismBenchCheck extends LinearOpMode {

    /** The device name in the Robot Configuration. */
    private static final String PRISM_NAME = "prism";

    private static final Artboard[] SLOTS = {
            Artboard.ARTBOARD_0, Artboard.ARTBOARD_1, Artboard.ARTBOARD_2, Artboard.ARTBOARD_3};

    private GoBildaPrismDriver prism;
    private String lastAction = "none yet";
    private int stripLength = 12;   // the number dpad up/down edits; X sends it
    private int slot = 0;

    @Override
    public void runOpMode() {
        prism = hardwareMap.get(GoBildaPrismDriver.class, PRISM_NAME);
        ElapsedTime statusTimer = new ElapsedTime();
        String status = readStatus();

        while (opModeInInit() || opModeIsActive()) {
            handleButtons();
            if (statusTimer.seconds() > 1.0) {   // I2C reads, not every loop
                status = readStatus();
                statusTimer.reset();
            }
            telemetry.addLine(status);
            telemetry.addData("Strip length to send (X)", stripLength);
            telemetry.addData("Artboard slot (dpad left/right)", slot);
            telemetry.addData("Last", lastAction);
            telemetry.update();
            sleep(20);
        }
    }

    private void handleButtons() {
        boolean save = gamepad1.left_trigger > 0.5;
        try {
            if (gamepad1.aWasPressed()) {
                if (save) saveTo(Artboard.ARTBOARD_0);
                else show(new PrismAnimations.Solid(Color.GREEN, 50), "solid green");
            }
            if (gamepad1.bWasPressed()) {
                if (save) saveTo(Artboard.ARTBOARD_1);
                else show(new PrismAnimations.Pulse(Color.BLUE, Color.ORANGE), "pulse blue/orange");
            }
            if (gamepad1.yWasPressed()) {
                if (save) saveTo(Artboard.ARTBOARD_2);
                else show(new PrismAnimations.Rainbow(), "rainbow");
            }
            if (gamepad1.xWasPressed()) {
                prism.setStripLength(stripLength);
                lastAction = "sent strip length " + stripLength;
            }
            if (gamepad1.leftBumperWasPressed()) {
                prism.clearAllAnimations();
                lastAction = "cleared all animations";
            }
            if (gamepad1.dpadUpWasPressed())   stripLength = Math.min(255, stripLength + 1);
            if (gamepad1.dpadDownWasPressed()) stripLength = Math.max(1, stripLength - 1);
            if (gamepad1.dpadRightWasPressed()) loadSlot((slot + 1) % SLOTS.length);
            if (gamepad1.dpadLeftWasPressed())  loadSlot((slot + SLOTS.length - 1) % SLOTS.length);
        } catch (RuntimeException e) {
            lastAction = "ERROR: " + e;   // an I2C failure shows here instead of ending the OpMode
        }
    }

    private void show(PrismAnimations.AnimationBase animation, String name) {
        prism.insertAndUpdateAnimation(LayerHeight.LAYER_0, animation);
        lastAction = "layer 0: " + name;
    }

    private void saveTo(Artboard board) {
        prism.saveCurrentAnimationsToArtboard(board);
        lastAction = "saved what is showing as artboard " + board.index;
    }

    private void loadSlot(int newSlot) {
        slot = newSlot;
        prism.loadAnimationsFromArtboard(SLOTS[slot]);
        lastAction = "loaded artboard " + slot;
    }

    private String readStatus() {
        try {
            return String.format("Device ID %d (3 = OK)  Firmware %s  Hardware %s\nLEDs %d  FPS %d  Power cycles %d",
                    prism.getDeviceID(), prism.getFirmwareVersionString(), prism.getHardwareVersionString(),
                    prism.getNumberOfLEDs(), prism.getCurrentFPS(), prism.getPowerCycleCount());
        } catch (RuntimeException e) {
            return "Cannot read the Prism: " + e + "\nCheck the port, the device type in the configuration, and its power.";
        }
    }
}
