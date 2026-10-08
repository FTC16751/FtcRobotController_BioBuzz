package org.firstinspires.ftc.teamcode.teams.p3.decode.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.common.test.EncoderMoveCheck;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConfig;

/** Encoder move checks and calibration measurements for the Decode chassis. Buttons are in common/test/EncoderMoveCheck. */
@TeleOp(name = "Decode: Encoder Move Check", group = "Decode Test")
public class DecodeEncoderMoveCheck extends EncoderMoveCheck {
    @Override
    protected RobotConfig robotConfig() {
        return DecodeConfig.create();
    }
}
