package managers;

import objects.Admin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sindre Paulshus
 */
public class AdminManagerTest {
    private AdminManager instance;
    private Admin testAdmin;

    @Before
    public void setUp(){
        instance = new AdminManager();
        testAdmin = new Admin("admin", "password");
        instance.addAdmin(testAdmin);
        instance.login(testAdmin.getUsername(), testAdmin.getPassword());
    }

    @After
    public void tearDown(){
        instance = null;
    }

    @Test
    public void login() {
        boolean res = instance.login("admin", "password");
        boolean expRes = true;
        assertEquals(expRes, res);
    }

    @Test
    public void newPassword() {
        instance.newPassword("123", "321");
        String res = instance.getAdmin().getPassword();
        String expRes = null;
        assertEquals(expRes, res);
    }

    @Test
    public void addAdmin() {
        boolean res = instance.addAdmin(testAdmin);
        boolean expRes = false;
        assertEquals(expRes, res);
    }

    @Test
    public void getAdmin() {
        Admin res = instance.getAdmin();
        Admin expRes = new Admin(testAdmin.getUsername(), null);
        assertEquals(expRes, res);
    }
}