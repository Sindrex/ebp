package objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

/**
 * @author Sindre Haugland Paulshus
 * date 12.03.2018
 */
@Deprecated
public class BikeHistoryTest {
    private static BikeHistory instance;

    @Before
    public void setUp() throws Exception{
        instance = new BikeHistory("2018/03/12 11:00", 1, 100, 0, 0, 40, 70,
                Bike.ActiveStatus.DELETED, 0, 0);
    }

    @After
    public void tearDown()throws Exception {
        instance = null;
    }

    @Test
    public void getTimestamp() {
        String res = instance.getTimestamp();
        String expRes = "2018/03/12 11:00";
        assertEquals(expRes, res);
    }

    @Test
    public void getChargeLevel() {
        double res = instance.getChargeLevel();
        double expRes = 100;
        double delta = 0.001;
        assertEquals(expRes, res, delta);
    }

    @Test
    public void getPosLong() {
        double res = instance.getPosLong();
        double expRes = 70;
        double delta = 0.001;
        assertEquals(expRes, res, delta);
    }

    @Test
    public void getPosLat() {
        double res = instance.getPosLat();
        double expRes = 40;
        double delta = 0.001;
        assertEquals(expRes, res, delta);
    }

    @Test
    public void getUserId() {
        int res = instance.getUserId();
        int expRes = 0;
        assertEquals(expRes, res);
    }

    @Test
    public void getStationId() {
        int res = instance.getStationId();
        int expRes = 0;
        assertEquals(expRes, res);
    }
}