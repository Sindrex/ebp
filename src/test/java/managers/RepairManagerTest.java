package managers;

import javafx.collections.ObservableList;
import objects.Repair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Observable;

import static org.junit.Assert.*;

/**
 * @author odderikf
 * JUnit test class
 * Tests whether data is successfully communicated through DAO
 * Requires data from database_test_data.sql
 * WILL add data to database.
 * Only run on test database.
 * @see RepairManager
 */
public class RepairManagerTest {
    /**
     * For expected exceptions
     */
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private RepairManager manager;

    @Before
    public void setUp() {
        manager = new RepairManager();
    }

    /**
     * Tests add functionality.
     */
    @Test
    public void addRepair() throws Exception {

        assertTrue(manager.addRepair(4, "2012-05-01", "Testrepair"));
        List<Repair> a = manager.getRepairsMatching(4, "2012-05-01", "Testrepair");
        assertEquals("2012-05-01", a.get(0).getRequestDate());
        assertEquals("Testrepair", a.get(0).getDescription());

        assertTrue(manager.addRepair(4, "2019-05-01", "test med return"));
        Repair b = manager.getRepairsMatching(4, "2019-05-01", "test med return").get(0);
        assertTrue(manager.returnRepair(b.getRepairId(), "2019-05-02", 20.3, "return fra test"));

        b = manager.getRepairsMatching(4, "2019-05-01", "test med return",
                "2019-05-02", 20.3, "return fra test").get(0);

        assertEquals("2019-05-01", b.getRequestDate());
        assertEquals("test med return", b.getDescription());
        assertEquals("2019-05-02", b.getReturnDate());
        assertEquals(20.3, b.getPrice(), 0.01);
        assertEquals("return fra test", b.getReturnDescription());

        exception.expect(InputMismatchException.class);
        manager.addRepair(Repair.INVALID_ID, "", "");
    }

    /**
     * Tests return functionality.
     */
    @Test
    public void returnRepair() throws Exception {
        manager.returnRepair(4, "2018-04-05", 40, "Repaired in test");
        manager.returnRepair(4, "2018-04-06", 50, "returned twice");
    }


    /**
     * Tests that getRepair() successfully fetches test data
     */
    /*
    @Test
    public void getRepair() throws Exception {
        Repair r = manager.getRepair(1);
        assertEquals("2018-04-04", r.getRequestDate());
        assertEquals("Broken tires", r.getDescription());
        assertEquals("2018-04-05", r.getReturnDate());
        assertEquals(5000, r.getPrice(), 0.01);
        assertEquals("Something got fixed.", r.getReturnDescription());

    }*/

    /**
     * Tests filtered getting
     */
    /*@Test
    public void getRepairsMatching() throws Exception {
        List<Repair> l = manager.getRepairsMatching(12, null, null);
        assertTrue(l.contains(manager.getRepair(6)));
        assertTrue(l.contains(manager.getRepair(7)));
        assertFalse(l.contains(manager.getRepair(2)));
    }*/

    /**
     * Tests getAll
     */
    /*@Test
    public void getRepairs() throws Exception {
        ObservableList<RepairTableRow> l = manager.getRepairs();
        assertTrue(l.contains(manager.getRepair(6)));
        assertTrue(l.contains(manager.getRepair(7)));
        assertTrue(l.contains(manager.getRepair(2)));

    }*/


}
