package objects;

import app.map.LatLng;
import org.junit.*;

import java.sql.Timestamp;
import java.util.IntSummaryStatistics;

import static objects.DockingStation.ActiveStatus.ACTIVE;
import static org.junit.Assert.*;

public class DockingStationTest {
    private DockingStation instance;

    @Before
    public void setUp() {
        instance = new DockingStation(1, 10, new LatLng(11.7, 10.5),
                0, ACTIVE, new Timestamp(0));
    }

    @After
    public void tearDown() {
        instance = null;
    }

    @Test
    public void getDockID() {
        int expectedResult = 1;
        int result = instance.getDockID();
        assertEquals(expectedResult, result);
    }

    @Test
    public void getPosLong() {
        double expectedResults = 10.5;
        double result = instance.getPosLong();
        assertEquals(expectedResults, result, 0.000001);
    }

    @Test
    public void getPosLat() {
        double expectedREsults = 11.7;
        double result = instance.getPosLat();
        assertEquals(expectedREsults, result, 0.000001);
    }

    @Test
    public void getPosition() {
        //String expectedResults = "Longditude: 10.5, Latitude: 11.7";
        //String result = instance.getPosition();
        LatLng expectedResults = new LatLng(11.7, 10.5);
        LatLng result = instance.getPosition();
        assertEquals(expectedResults, result); // 5 meters
    }

    /*@BikeTableRow
    public void addBike() {
        boolean expectedResults = true;
        boolean result = instance.addBike(new BikeBuilder().id(1).datePurchased());
    }*/

    @Test
    public void getBikes() {
        int expectedResults = 0;
        int result = instance.getBikes().size();
        assertEquals(expectedResults, result);
    }

    @Test
    public void changeStatus() {
        DockingStation.ActiveStatus expectedResults = DockingStation.ActiveStatus.DELETED;
        instance.changeStatus(DockingStation.ActiveStatus.DELETED);
        DockingStation.ActiveStatus result = instance.getStatus();
        assertEquals(expectedResults, result);
    }
}