package db;

import config.Configuration;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Jorgen Bele Reinfjell
 * date 07.03.2018
 *
 * Needs test/resources marked as test source
 */
public class DatabaseUtilsTest {
    @Test
    public void trySleep() {
        long sleepLength = 100;
        long start = System.currentTimeMillis();
        boolean success =  (DatabaseUtils.trySleep(sleepLength));
        long end = System.currentTimeMillis();
        if (success) {
            assertTrue(end - start >= sleepLength);
        } else {
            assertTrue(end - start < sleepLength);
        }
    }
}