package org.firstinspires.ftc.teamcode.teams.starterbot2027.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.teams.starterbot2027.GGStarterBotConfig;

/** The GearGirls StarterBot's TeleOp: goBILDA's controls (see StarterBot2027Teleop) on GGStarterBotConfig. */
@TeleOp(name = "GG Starterbot: Teleop (RUN ME)", group = "StarterBot2027")
public class GGStarterBotTeleop extends StarterBot2027Teleop {
    @Override
    protected RobotConfig config() {
        return GGStarterBotConfig.create();
    }
}
