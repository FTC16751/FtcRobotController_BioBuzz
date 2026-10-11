package ours.sim;

import com.qualcomm.robotcore.hardware.DcMotorControllerImpl;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.DigitalChannelImpl;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.configuration.MotorType;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.teams.p3.decode.DecodeConfig;

/**
 * Stand-in Decode mechanisms for the simulator, registered under DecodeConfig's own device names so
 * DecodeRobot builds unmodified. They accept commands and report sensible readings; they do not move
 * game pieces, so the sim shows our sequencing and timing, not whether a shot scores.
 *
 * The patched sim robot calls register() by reflection when it builds its hardware map.
 */
public class OurDevices {
    /** GUESS: how fast the simulated flywheel reaches a commanded speed (seconds for 63% of the way). Tune to the real wheel. */
    static final double FLYWHEEL_TIME_CONSTANT_SEC = 0.5;

    public static void register(HardwareMap map) {
        DcMotorControllerImpl controller = new DcMotorControllerImpl();
        map.put(DecodeConfig.INTAKE, new Roller(controller, 0));
        map.put(DecodeConfig.INDEXER, new Roller(controller, 1));
        map.put(DecodeConfig.SHOOTER_LEFT, new Flywheel(controller, 2));
        map.put(DecodeConfig.SHOOTER_RIGHT, new Flywheel(controller, 3));
        map.put(DecodeConfig.STOPPER, new ServoImpl());
        map.put(DecodeConfig.TURRET, new ServoImplEx());
        map.put(DecodeConfig.TURRET_LIMIT, new DigitalChannelImpl());   // reads true (not pressed; the switch is active low)
        String led = DecodeConfig.create().hardware.led;
        if (led != null) map.put(led, new ServoImpl());
    }

    /** Intake and indexer: only power is used. */
    static class Roller extends DcMotorExImpl {
        Roller(DcMotorControllerImpl controller, int port) { super(MotorType.Neverest40, controller, port); }
    }

    /** A flywheel that approaches the commanded velocity like a first-order system, reading in the units it was given. */
    static class Flywheel extends DcMotorExImpl {
        private double target = 0, current = 0;
        private long lastNanos = System.nanoTime();

        Flywheel(DcMotorControllerImpl controller, int port) { super(MotorType.Neverest40, controller, port); }

        @Override public synchronized void setVelocity(double ticksPerSecond) { advance(); target = ticksPerSecond; }
        @Override public synchronized void setVelocity(double rate, AngleUnit unit) { advance(); target = rate * ticksPerUnit(unit); }
        @Override public synchronized double getVelocity() { advance(); return current; }
        @Override public synchronized double getVelocity(AngleUnit unit) { advance(); return current / ticksPerUnit(unit); }
        @Override public synchronized void setPower(double power) { advance(); target = power * MOTOR_TYPE.MAX_TICKS_PER_SECOND; }

        private double ticksPerUnit(AngleUnit unit) { return MOTOR_TYPE.TICKS_PER_ROTATION / (unit == AngleUnit.DEGREES ? 360.0 : 2.0 * Math.PI); }

        private void advance() {
            long now = System.nanoTime();
            double dt = (now - lastNanos) / 1e9;
            lastNanos = now;
            current += (target - current) * (1 - Math.exp(-dt / FLYWHEEL_TIME_CONSTANT_SEC));
        }
    }
}
