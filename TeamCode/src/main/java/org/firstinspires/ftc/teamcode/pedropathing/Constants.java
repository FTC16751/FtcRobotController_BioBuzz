package org.firstinspires.ftc.teamcode.pedropathing;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.common.PedroBridge;
import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027BotConfig;

/**
 * Which robot AutoTune tunes. Pedro 3's tuning procedures (Tuning.java, procedures/) ask for a
 * localizer, a drivetrain and an algorithm built from a HardwareMap; these come from the robot's
 * RobotConfig through common/PedroBridge, so the tuner always tunes the robot named here and
 * nothing Pedro-specific is typed in this package.
 *
 * To tune a different chassis: change ACTIVE_CONFIG, redeploy, open http://192.168.43.1:10158.
 * When AutoTune prints a ForesightConfig at the end, paste its lambda into that robot's
 * PedroPathingConfig (Test2027BotConfig section 6c); the Pinpoint and drivetrain procedures print
 * names, directions and offsets that belong in sections 1, 2 and 4 of the same file (offsets in
 * inches there, mm in ours: multiply by 25.4). Robot code never uses this class; it goes through
 * DriveUtil2026b.
 */
public class Constants {

    /** The robot AutoTune tunes. Change this one line to tune another chassis. */
    public static final RobotConfig ACTIVE_CONFIG = Test2027BotConfig.create();

    public static PinpointLocalizer localizer(HardwareMap hardwareMap) {
        return PedroBridge.createPinpointLocalizer(hardwareMap, ACTIVE_CONFIG);
    }

    public static Mecanum drivetrain(HardwareMap hardwareMap) {
        return PedroBridge.createDrivetrain(hardwareMap, ACTIVE_CONFIG);
    }

    public static Foresight foresight() {
        return PedroBridge.createForesight(ACTIVE_CONFIG);
    }

    /**
     * A Follower on ACTIVE_CONFIG that opens its own Pinpoint. Only for OpModes that do NOT also
     * build a DriveUtil2026b: two owners of one Pinpoint is the 2025 failure. Robot code gets its
     * Follower from DriveUtil2026b.getFollower().
     */
    public static Follower createFollower(HardwareMap hardwareMap) {
        return PedroBridge.createFollower(hardwareMap, ACTIVE_CONFIG);
    }
}
