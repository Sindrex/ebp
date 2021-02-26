package objects;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author Sindre Paulshus
 */
public class AdminTest {
    private Admin instance;

    @Before
    public void setUp(){
        instance = new Admin("Bob", "123");
    }

    @After
    public void tearDown(){
        instance = null;
    }

    @Test
    public void getUsername() {
        String res = instance.getUsername();
        String expRes = "Bob";
        assertEquals(expRes, res);
    }

    @Test
    public void getPassword() {
        String res = instance.getUsername();
        String expRes = "Bob";
        assertEquals(expRes, res);
    }

    @Test
    public void setUsername() {
        String expRes = "Sim";
        instance.setUsername(expRes);
        String res = instance.getUsername();
        assertEquals(expRes, res);
    }

    @Test
    public void changePassword() {
        String expRes = "321";
        instance.changePassword(expRes);
        String res = instance.getPassword();
        assertEquals(expRes, res);
    }
}