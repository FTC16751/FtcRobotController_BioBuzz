package org.firstinspires.ftc.teamcode.pedropathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.common.PedroBridge;
import org.firstinspires.ftc.teamcode.common.RobotConfig;
import org.firstinspires.ftc.teamcode.teams.testteam2027.Test2027BotConfig;

/**
 * The Pedro Pathing tuning entry point. Pedro's Tuning OpMode (and its vendor examples) call the
 * one-argument createFollower(hardwareMap) and expect these static constants objects, which the
 * Panels dashboard edits live while a tuner runs.
 *
 * Until 2026-09-07 this file held one robot's numbers typed by hand, and they described neither
 * of the robots that ran them (doc/PEDRO_ON_TEST2027.md, section 1.3). Now the statics are BUILT
 * from a RobotConfig, so the tuner always tunes the robot named in ACTIVE_CONFIG, and the finished
 * numbers are copied back into that robot's config file (its PedroPathingConfig), where every
 * robot class picks them up through DriveUtil2026b. Nothing Pedro-specific is typed here.
 *
 * To tune a different chassis: change ACTIVE_CONFIG, redeploy, run "Tuning" (Driver Station
 * group Pedro). Robot code never uses this class; it goes through common/PedroBridge.
 */
public class Constants {

    /** The robot the Tuning OpMode tunes. Change this one line to tune another chassis. */
    public static final RobotConfig ACTIVE_CONFIG = Test2027BotConfig.create();

    // Panels edits these objects live during tuning. createFollower(hardwareMap) builds the
    // Follower from these same objects, so an edit applies on the next loop.
    public static FollowerConstants followerConstants = PedroBridge.followerConstantsFor(ACTIVE_CONFIG);
    public static MecanumConstants driveConstants = PedroBridge.mecanumConstantsFor(ACTIVE_CONFIG);
    public static PinpointConstants localizerConstants = PedroBridge.pinpointConstantsFor(ACTIVE_CONFIG);
    public static PathConstraints pathConstraints = PedroBridge.requirePedro(ACTIVE_CONFIG).pathConstraints;

    /** Where the tuner and the vendor examples put the robot at start. Pedro field frame, inches, radians. */
    public static Pose startingPos = new Pose(72, 72, 0);

    /** For the Tuning OpMode and the vendor examples: a Follower on ACTIVE_CONFIG through the live-tunable statics above. */
    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();
    }

    /**
     * A Follower for any robot's config. Kept for the older OpModes that call it; new robot code
     * gets its Follower from DriveUtil2026b.getFollower(), which builds it from the same config and
     * owns the Pinpoint. Do not call this in an OpMode that also constructs a DriveUtil2026b: two
     * Followers, or a Follower plus a DriveUtil, both opening the Pinpoint is the 2025 failure.
     */
    public static Follower createFollower(HardwareMap hardwareMap, RobotConfig config) {
        return PedroBridge.createFollower(hardwareMap, config);
    }
}
