package logging;

import java.time.LocalDateTime;

/**
 * Contains static logging methods.
 * @author Joergen Bele Reinfjell
 */
public abstract class Logger {

    synchronized public static void log(String s) {
        System.out.printf("%s - %d: %s\n", LocalDateTime.now(), Thread.currentThread().getId(), s);
    }

    synchronized public static void logf(String fmt, Object... arguments) {
        System.out.printf("%s - %d: %s\n", LocalDateTime.now(), Thread.currentThread().getId(), String.format(fmt, arguments));
    }

    synchronized public static void err(String s) {
        System.err.printf("%s - %d: %s\n", LocalDateTime.now(), Thread.currentThread().getId(), s);
    }

    synchronized public static void errf(String fmt, Object... arguments) {
        System.err.printf("%s - %d: %s\n", LocalDateTime.now(), Thread.currentThread().getId(), String.format(fmt, arguments));
    }
}
