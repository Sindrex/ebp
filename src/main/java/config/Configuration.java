package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Class containing static methods for access to the programs properties file.
 * @author Joergen Bele Reinfjell
 */
public class Configuration {

    public final static Properties DEFAULT_PROPERTIES;
    private static String CONFIGURATION_PROPERTIES_FILE = "config.properties";
    private static Properties properties;

    public final static String DATABASE_PROPERTY_ENTRY = "database";
    public final static String USERNAME_PROPERTY_ENTRY = "username";
    public final static String PASSWORD_PROPERTY_ENTRY = "password";
    public final static String GOOGLE_API_KEY_PROPERTY_ENTRY = "googleApiKey";

    public final static String EMAIL_RECIPIENT_PROPERTY_ENTRY = "email_recipient";
    public final static String EMAIL_SENDER_PROPERTY_ENTRY = "email_sender";
    public final static String EMAIL_USERNAME_PROPERTY_ENTRY = "email_username";
    public final static String EMAIL_PASSWORD_PROPERTY_ENTRY = "email_password";

    static {
        properties = null;
        DEFAULT_PROPERTIES = new Properties();
        DEFAULT_PROPERTIES.setProperty(DATABASE_PROPERTY_ENTRY, "DATABASE");
        DEFAULT_PROPERTIES.setProperty(USERNAME_PROPERTY_ENTRY, "DB_USER");
        DEFAULT_PROPERTIES.setProperty(PASSWORD_PROPERTY_ENTRY, "DB_PASSWORD");

        DEFAULT_PROPERTIES.setProperty(GOOGLE_API_KEY_PROPERTY_ENTRY, "KEY");

        DEFAULT_PROPERTIES.setProperty(EMAIL_RECIPIENT_PROPERTY_ENTRY, "EMAIL");

        DEFAULT_PROPERTIES.setProperty(EMAIL_SENDER_PROPERTY_ENTRY, "email2@host2.domain2");
        DEFAULT_PROPERTIES.setProperty(EMAIL_USERNAME_PROPERTY_ENTRY, "USERNAME");
        DEFAULT_PROPERTIES.setProperty(EMAIL_PASSWORD_PROPERTY_ENTRY, "PASSWORD");
    }

    public static Properties getProperties() {
        if (properties == null) {
            properties = Configuration.getProperties(CONFIGURATION_PROPERTIES_FILE);
            if (properties == null) {
                properties = DEFAULT_PROPERTIES;
                storePropertiesInFile(properties, CONFIGURATION_PROPERTIES_FILE);
            }
        }
        return properties;
    }

    /**
     * Create a Properties object from file.
     * @return a Properties instance with the fields from given databaseProperties file,
     * * or null if unable to read from file.
     */
    public static synchronized Properties getProperties(String propertiesFilePath) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFilePath)) {
            properties.load(fis);
        } catch (IOException e) {
            return null;
        }
        return properties;
    }

    /**
     * Creates and stores Properties in a .databaseProperties file..
     * @param properties the databaseProperties to store in file.
     * @return true on success, false on failure.
     */
    public static synchronized boolean storePropertiesInFile(Properties properties, String propertiesFilePath) {
        try (FileOutputStream fos = new FileOutputStream(propertiesFilePath)) {
            properties.store(fos, "Auto generated: default database detail values.");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static synchronized boolean storeProperties(Properties properties) {
        return storePropertiesInFile(properties, CONFIGURATION_PROPERTIES_FILE);
    }
}
