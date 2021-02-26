package objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

/**
 * @author Sindre Haugland Paulshus
 * @author Odd-Erik Frantzen
 * date 14.03.2018
 */
public class BikeTest {
    private static  Bike instance;
    @Deprecated
    private BikeHistory testHistory;

    @Before
    @Deprecated
    public void setUp() throws Exception {
        testHistory =  new BikeHistory(new Timestamp(System.currentTimeMillis()).toString(), 1, 100, 0, 0,
                40, 70, Bike.ActiveStatus.DOCKED, 0, 1);

        instance = new Bike(1, "Tandem", "Diamond", 1000,
                    "2018-03-14", Bike.ActiveStatus.DOCKED, 0, 1);
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    @Test
    public void getID() {
        int res = instance.getId();
        int expRes = 1;
        assertEquals(expRes, res);
    }

    @Test
    public void getType() {
        String res = instance.getType();
        String expRes = "Tandem";
        assertEquals(expRes, res);
    }

    @Test
    public void getMake() {
        String res = instance.getMake();
        String expRes = "Diamond";
        assertEquals(expRes, res);
    }

    @Test
    public void getPrice() {
        double res = instance.getPrice();
        double expRes = 1000;
        double delta = 0.001;
        assertEquals(expRes, res, delta);
    }

    @Test
    public void getDatePurchased() {
        String res = instance.getDatePurchased();
        String expRes = "2018-03-14";
        assertEquals(expRes, res);
    }

    @Test
    public void getActiveStatus() {
        Bike.ActiveStatus res = instance.getActiveStatus();
        Bike.ActiveStatus expRes = Bike.ActiveStatus.DOCKED;
        assertEquals(expRes, res);
    }

    @Test
    public void setActiveStatus() {
        instance.setActiveStatus(Bike.ActiveStatus.RENTED);
        Bike.ActiveStatus res = instance.getActiveStatus();
        Bike.ActiveStatus expRes = Bike.ActiveStatus.RENTED;
        assertEquals(expRes, res);
    }

    @Test
    public void getUserId() {
        int res = instance.getUserId();
        int expRes = 0;
        assertEquals(expRes, res);
    }

    @Test
    public void setUserId() {
        instance.setUserId(2);
        int res = instance.getUserId();
        int expRes = 2;
        assertEquals(expRes, res);
    }

    @Test
    public void getStationId() {
        int res = instance.getStationId();
        int expRes = 1;
        assertEquals(expRes, res);
    }

    @Test
    public void setStationId() {
        instance.setStationId(0);
        int res = instance.getStationId();
        int expRes = 0;
        assertEquals(expRes, res);
    }

  /*  @Test
    public void getBikeHistories() {
        instance.update(testHistory);

        List<BikeHistory> res = instance.getBikeHistories();
        List<BikeHistory> expRes = new ArrayList<>();
        expRes.add(testHistory);
        assertEquals(expRes, res);
    }*/

 /*  @Test
    public void setBikeHistories() {
        BikeHistory test = new BikeHistory("123", 2, 2,0, 0, 3, 4,
                Bike.ActiveStatus.DELETED, 0, 0);
        List<BikeHistory> testList = new ArrayList<>();
        testList.add(test);
        instance.setBikeHistories(testList);

        List<BikeHistory> res = instance.getBikeHistories();
        List<BikeHistory> expRes = testList;
        expRes.add(testHistory);
        assertEquals(expRes, res);
    }*/

  /*  @Test
    public void addBikeHistory() {
        instance.addBikeHistory(testHistory);

        BikeHistory res = instance.getRecent();
        BikeHistory expRes = testHistory;
        assertEquals(expRes, res);
    }*/
}