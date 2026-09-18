package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;

/** The P3 StarterBot's TeleOp: goBILDA's controls (see StarterBot2027Teleop) on StarterBotConfig.p3(). */
@TeleOp(name = "P3 Starterbot: Teleop (RUN ME)", group = "StarterBot2027")
public class P3StarterBotTeleop extends StarterBot2027Teleop {
    @Override
    protected StarterBotConfig config() {
        return StarterBotConfig.p3();
    }
}
