package config;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Joergen Bele Reinfjell
 */
public class ConfigurationTest {

    @Test
    public void getProperties() {
        URL url = ClassLoader.getSystemClassLoader().getResource("get_properties_test.properties");
        assertNotNull(url);
        String path = url.getPath();
        assertNotNull(path);
        Properties properties = Configuration.getProperties(path);
        assertNotNull(properties);
        assertEquals("test_database", properties.getProperty("database"));
        assertEquals("test_username", properties.getProperty("username"));
        assertEquals("test_password", properties.getProperty("password"));
    }

    @Test
    public void storePropertiesInFile() throws IOException {
        File tempFile = File.createTempFile("test_output", "properties");
        assertTrue(Configuration.storePropertiesInFile(Configuration.DEFAULT_PROPERTIES, tempFile.getAbsolutePath()));
        Properties properties = Configuration.getProperties(tempFile.getAbsolutePath());
        assertEquals(Configuration.DEFAULT_PROPERTIES, properties);
        assertTrue(tempFile.delete());
    }
}