package logging;

import config.Configuration;
import objects.Admin;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joergen Bele Reinfjell
 */
public class SendMailTest {
    @Test
    public void newUserMailTest() {
        assertTrue(SendMail.newUserMail("fk", "the haters"));
    }

    @Test
    public void newAdminMailTest() {
        assertTrue(SendMail.newAdmin(new Admin(Configuration.getProperties().getProperty(Configuration.EMAIL_RECIPIENT_PROPERTY_ENTRY), "guyyyyyy")));
    }

    @Test
    public void sendNewPass() {
        assertTrue(SendMail.sendNewPass("d", "readful"));
    }

    @Test
    public void sendNewAdminPass() {
        Admin admin = new Admin(Configuration.getProperties().getProperty(Configuration.EMAIL_RECIPIENT_PROPERTY_ENTRY), "guyyyyyy");
        assertTrue(SendMail.sendNewPass(admin.getUsername(), "new password 2", admin));
    }
}