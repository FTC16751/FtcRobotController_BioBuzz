package Ori.Coval.Logging.Logger;

/**
 * Sim stand-in for Koala-Log's file manager (it shadows the library's class, because sim/.cache's classes
 * come first on the classpath). The real one asks Android for external storage, which the sim does not have.
 * Throwing a RuntimeException sends LogUtil.start() down its normal "could not start; this run is not
 * logged" path.
 */
public class LogFileManager {
    static java.io.FileOutputStream getOutputStream() { return null; }
    static void setup(android.content.Context context, String name) {
        throw new RuntimeException("logging is off in the simulator (no Android storage to write a log file)");
    }
}
