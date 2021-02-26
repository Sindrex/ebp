package db.dao;

import db.Database;
import db.DatabaseConnection;
import objects.Repair;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author odderikf
 * JUNit test class
 * @see db.dao.RepairDao
 * Should result in no change to database, given less than 10k entries to repair table
 * Please run on test databases only
 * Requires data from testdata.sql
 */
public class RepairDaoTest {
    /**
     * For expected errors
     */
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private RepairDao dao;

    @Before
    public void setUp() {
        dao = RepairDao.getInstance();
    }

    /**
     * Deletes test entries in repair to save space.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        deleteRepairs();
    }

    /**
     * Tests whether dao.get successfully pulls data from mysql
     *
     * @throws Exception
     */
    @Test
    public void get() throws Exception {
        Repair t = dao.get(new Repair(1));
        assertEquals(1, t.getRepairId());
        assertEquals("2018-04-04", t.getRequestDate());
        assertEquals("Broken tires", t.getDescription());
        assertEquals(5, t.getBikeId());
        assertEquals("2018-04-05", t.getReturnDate());
        assertEquals("Something got fixed.", t.getReturnDescription());

    }

    /**
     * Tests asynchronous access to the database
     * by having one process wait for the row count to rise,
     * while the other writes additional rows.
     * @throws InterruptedException because of threading
     */
    @Test
    public void getInstance() throws InterruptedException{
        getInstanceWaiter waiter = new getInstanceWaiter();
        Thread waiterThread = new Thread(waiter);
        waiterThread.start();
        while (!waiter.isWaiting()){
            Thread.sleep(100);
        }
        getInstanceAdder adder = new getInstanceAdder();
        Thread adderThread = new Thread(adder);
        adderThread.start();
        adderThread.join();
        waiterThread.join();
        assertEquals(adder.getCount(), waiter.getCount());
    }

    private class getInstanceWaiter implements Runnable{
        private boolean waiting = false;
        private int count = 0;

        boolean isWaiting(){
            return waiting;
        }

        int getCount(){
            return count;
        }

        @Override
        public void run() {
            RepairDao r = RepairDao.getInstance();
            assertEquals(r, dao);
            try{
                int old_total = r.getAll().size();
                waiting = true;
                while (count <= old_total){
                    count = r.getAll().size();
                }
                waiting = false;
                count = r.getAll().size();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**

     */
    private class getInstanceAdder implements Runnable{
        private int count;
        int getCount(){
            return count;
        }
        @Override
        public void run() {
            try {
                addAll();
                count = dao.getAll().size();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Tests that data is successfully added and retrieved
     * Tests that return data is not added during the add step
     * dao.get
     * dao.add
     *
     * @throws Exception
     */
    @Test
    public void add() throws Exception {
        deleteRepairs();
        assertTrue(dao.add(new Repair(10000, 7, "2018-04-06", "RepairDaoTest")));
        Repair r = dao.get(new Repair(10000));
        assertEquals(7, r.getBikeId());
        assertEquals("2018-04-06", r.getRequestDate());
        assertEquals("RepairDaoTest", r.getDescription());

        try {
            assertTrue(dao.add(new Repair(Repair.INVALID_ID, 7, "", "")));
            fail();
        } catch (InputMismatchException e) {
            assertEquals(RepairDao.INVALID_DATE_DESCRIPTION, e.getMessage());
        }

        try {
            dao.add(new Repair(1, Repair.INVALID_ID, "2018-04-05", ""));
            fail();
        } catch (InputMismatchException e) {
            assertEquals(e.getMessage(), RepairDao.INVALID_BIKE_ID_DESCRIPTION);
        }
        exception.expect(InputMismatchException.class);
        dao.add(new Repair(Repair.INVALID_ID));

        assertTrue(dao.add(new Repair(10001, 7, "2018-04-07", "updatetest",
                "2018-04-08", 60.2, "returntext should not appear")));
        r = dao.get(new Repair(10001));
        assertEquals(7, r.getBikeId());
        assertEquals("2018-04-07", r.getRequestDate());
        assertEquals("updatetest", r.getDescription());
        assertEquals(null, r.getReturnDescription());
        assertEquals(null, r.getReturnDate());
        assertTrue(Double.isNaN(r.getPrice()));
    }

    /**
     * Tests whether update successfully adds return data
     *
     * @throws Exception
     */
    @Test
    public void update() throws Exception {
        deleteRepairs();
        assertTrue(dao.add(new Repair(10000, 7, "2018-04-06", "RepairDaoTest")));
        Repair r = dao.get(new Repair(10000));
        assertTrue(dao.update(new Repair(r.getRepairId(), r.getBikeId(), r.getRequestDate(), r.getDescription(),
                "2018-04-07", 60.2, "Returned repairdaotest")));
        r = dao.get(r);
        assertEquals(7, r.getBikeId());
        assertEquals("2018-04-06", r.getRequestDate());
        assertEquals("RepairDaoTest", r.getDescription());
        assertEquals("Returned repairdaotest", r.getReturnDescription());
        assertEquals("2018-04-07", r.getReturnDate());
        assertEquals(60.2, r.getPrice(), 0);

        try {
            assertFalse(dao.update(new Repair(5)));
            fail();
        } catch (InputMismatchException e) {
            assertEquals(RepairDao.INVALID_NULL_DESCRIPTION, e.getMessage());
        }
    }

    @Test
    public void updateAll() throws Exception{
        deleteRepairs();
        assertTrue(dao.add(new Repair(10000, 7, "2018-04-06", "RepairDaoTest")));
        assertTrue(dao.add(new Repair(10001, 7, "2018-04-07", "RepairDaoTest2")));
        List<Repair> elements = new ArrayList<>();
        Repair r = dao.get(new Repair(10000));
        Repair r2 = dao.get(new Repair(10001));
        elements.add(new Repair(r.getRepairId(), r.getBikeId(), r.getRequestDate(), "RepairDaoTestUpdated"));
        elements.add(new Repair(r2.getRepairId(), r2.getBikeId(), r2.getRequestDate(), "RepairDaoTestUpdated2",
                "2018-04-08", 4.2, "Returned and updated"));
        assertTrue(dao.updateAll(elements));

        Repair new_r = dao.get(r);
        r2 = dao.get(r2);

        assertEquals(r.getRepairId(), new_r.getRepairId());
        assertEquals(r.getRequestDate(), new_r.getRequestDate());
        assertNotEquals(r.getDescription(), new_r.getDescription());
        assertEquals("RepairDaoTestUpdated", new_r.getDescription());
        assertEquals(r.returnIsValid(), new_r.returnIsValid());

        assertEquals(4.2, r2.getPrice(), 0.01);
    }

    /**
     * Tests that delete returns false
     */
    @Test
    public void delete() {
        assertFalse(dao.delete(new Repair(Repair.INVALID_ID)));
    }

    /**
     * Tests whether a list of Repairs can be added to the database and be fetched and found using getAll
     * dao.addAll
     * dao.getAll
     *
     * @throws Exception
     */
    @Test
    public void addAll() throws Exception {
        deleteRepairs();
        List<Repair> rs = new ArrayList<>();
        String basestring = "2040-01-01";  // Start date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(basestring));
        for (int i = 10000; i < 10500; i++) {
            c.add(Calendar.DATE, 1);
            Repair r = new Repair(i, 7, sdf.format(c.getTime()), "repair " + i);
            rs.add(r);
        }
        assertTrue(dao.addAll(rs));

        c = Calendar.getInstance();
        c.setTime(sdf.parse(basestring));
        List<Repair> repairs = dao.getAll();
        assertTrue(repairs != null && repairs.size() > 0);

        for (int i = 10000; i < 10500; i++) {
            c.add(Calendar.DATE, 1);
            Repair r = new Repair(i);
            r = repairs.get(repairs.indexOf(r));
            assertEquals(i, r.getRepairId());
            assertEquals(7, r.getBikeId());
            assertEquals(sdf.format(c.getTime()), r.getRequestDate());
            assertEquals("repair " + i, r.getDescription());
        }
        assertNotNull(repairs);
    }


    /**
     * Cleanup method so multiple tests can use the same range of repair_id
     *
     * @throws Exception
     */
    private void deleteRepairs() throws Exception {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement p = con.prepareStatement("DELETE FROM repair_return WHERE repair_id > 9999");
                PreparedStatement p2 = con.prepareStatement("DELETE FROM repair WHERE repair_id > 9999;")
        ) {
            p.execute();
            p2.execute();
        }
    }

    @Test
    public void getRepairsMatching() throws Exception {
        List<Repair> l = dao.getAllMatching(new Repair(-1, 12, null, null));
        assertTrue(l.contains(dao.get(new Repair(6))));
        assertTrue(l.contains(dao.get(new Repair(7))));
        assertFalse(l.contains(dao.get(new Repair(2))));
    }
}
