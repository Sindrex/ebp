package objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple class that just tests all fields get filled out correctly
 *
 * @author Sindre Thomassen
 */
public class UserTest {
    private User instance;

    @Before
    public void setUp() throws Exception {
        try {
            instance = new User(-1,"bob@hotmail.com", "qwerty");
        } catch (Exception e) {

        }
    }

    @After
    public void tearDown() throws Exception { instance = null; }

    @Test
    public void getUsername() {
        String res = instance.getUsername();
        String expected = "bob@hotmail.com";
        assertEquals(res, expected);
    }

    @Test
    public void getPassword() {
        String res = instance.getPassword();
        String expected = "qwerty";
        assertEquals(res, expected);
    }

    @Test
    public void getId() {
        int res = instance.getId();
        int expected = -1;
        assertEquals(res, expected);
    }

    @Test
    public void changeEmail() {
        instance.changeEmail("bobby@outlook.com");
        String res = instance.getUsername();
        String expected = "bobby@outlook.com";
        assertEquals(res, expected);
    }

    @Test
    public void setId() {
        instance.setId(10);
        int res = instance.getId();
        int expected = 10;
        assertEquals(res, expected);
    }
}