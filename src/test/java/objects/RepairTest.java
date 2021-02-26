package objects;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple class that just tests all fields get filled out correctly
 *
 * @author Odd-Erik Frantzen
 */
public class RepairTest {
    private static Repair minimal;
    private static Repair submitted;
    private static Repair done;

    /**
     * Setup method
     * Sets up 3 repairs, one empty, one filled in, and one filled in with return data
     *
     * @throws Exception
     */
    @Before
    public void setUp() {
        minimal = new Repair(1);
        submitted = new Repair(2, 1, "2018/03/12/11/00", "BikeTableRow repair");
        done = new Repair(3, 2, "2018/03/12/11/00", "done repair",
                "2018/03/14/11/00", 200.3, "Repair is done");
    }

    @Test
    public void getRepairId() {
        assertEquals(1, minimal.getRepairId());
        assertEquals(2, submitted.getRepairId());
        assertEquals(3, done.getRepairId());
    }

    @Test
    public void getBikeId() {
        assertEquals(Repair.INVALID_ID, minimal.getBikeId());
        assertEquals(1, submitted.getBikeId());
        assertEquals(2, done.getBikeId());
    }

    @Test
    public void getDescription() {
        assertEquals(null, minimal.getDescription());
        assertEquals("BikeTableRow repair", submitted.getDescription());
        assertEquals("done repair", done.getDescription());
    }

    @Test
    public void getPrice() {
        assertEquals(Double.NaN, minimal.getPrice(), 0.01);
        assertEquals(Double.NaN, submitted.getPrice(), 0.01);
        assertEquals(200.3, done.getPrice(), 0.01);
    }

    @Test
    public void getRequestDate() {
        assertEquals(null, minimal.getRequestDate());
        assertEquals("2018/03/12/11/00", submitted.getRequestDate());
        assertEquals("2018/03/12/11/00", done.getRequestDate());
    }

    @Test
    public void getReturnDate() {
        assertEquals(null, minimal.getReturnDate());
        assertEquals(null, submitted.getReturnDate());
        assertEquals("2018/03/14/11/00", done.getReturnDate());
    }

    @Test
    public void getReturnDescription() {
        assertEquals(null, minimal.getReturnDescription());
        assertEquals(null, submitted.getReturnDescription());
        assertEquals("Repair is done", done.getReturnDescription());
    }

    @Test
    public void compareTo() {
        assertTrue(done.equals(new Repair(done.getRepairId())));
        assertEquals(0, minimal.compareTo(new Repair(1)));
        assertTrue(minimal.compareTo(done) < 0);
    }

    @Test
    public void equalsRepair() {
        assertTrue(done.equals(done));
        assertTrue(done.equals(new Repair(done.getRepairId())));
        assertFalse(done.equals(minimal));
        assertTrue(done.compareTo(minimal) > 0);
    }

    @Test
    public void equalsObject() {
        assertTrue(done.equals((Object) done));
        assertTrue(done.equals((Object) new Repair(done.getRepairId())));
        assertFalse(done.equals(new Object()));
        assertFalse(done.equals(new ExampleObject()));

    }

    class ExampleObject {
        public int repairId = 3;

        private ExampleObject() {
        }

        public int getRepairId() {
            return 3;
        }
    }

}