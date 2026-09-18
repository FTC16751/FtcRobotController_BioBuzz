package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.teams.starterbot2027.StarterBotConfig;

/** The GearGirls StarterBot's TeleOp: goBILDA's controls (see StarterBot2027Teleop) on StarterBotConfig.gg(). */
@TeleOp(name = "GG Starterbot: Teleop (RUN ME)", group = "StarterBot2027")
public class GGStarterBotTeleop extends StarterBot2027Teleop {
    @Override
    protected StarterBotConfig config() {
        return StarterBotConfig.gg();
    }
}
