package db;

/**
 * This class contains various utility methods which are useful when
 * using JDBC.
 *
 * @author Joergen Bele Reinfjell
 * date 05.03.2018
 */
class DatabaseUtils {
    /**
     * Tries to sleep for the specified amount of time but ignores any
     * InterruptedException.
     * @param millis the number of milliseconds to sleep for.
     * @return true when successful, false on failure.
     */
    public static boolean trySleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            return false;
        }
        return true;
    }
}
