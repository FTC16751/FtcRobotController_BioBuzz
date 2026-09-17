package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.P3StarterBotConfig;

/** The P3 StarterBot's TeleOp: goBILDA's controls (see StarterBot2027Teleop) on P3StarterBotConfig. */
@TeleOp(name = "P3 Starterbot: Teleop (RUN ME)", group = "StarterBot2027")
public class P3StarterBotTeleop extends StarterBot2027Teleop {
    @Override
    protected RobotConfig config() {
        return P3StarterBotConfig.create();
    }
}
