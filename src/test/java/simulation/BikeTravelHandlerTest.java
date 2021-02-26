package simulation;

import app.map.LatLng;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Joergen Bele Reinfjell
 */
public class BikeTravelHandlerTest {

    /**
     * Requires Google API calls. May result in inconsistent results.
     */
    @Test
    public void buildTravelPath() {
        BikeTravelHandler.PositionTimeDistanceTuple tuples[] = new BikeTravelHandler.PositionTimeDistanceTuple[]{
                new BikeTravelHandler.PositionTimeDistanceTuple(new LatLng(63.431830,10.402710), 78000, 422),
                new BikeTravelHandler.PositionTimeDistanceTuple(new LatLng(63.432420, 10.403180), 90000, 69)
        };

        BikeTravelHandler handler = new BikeTravelHandler(new LatLng(63.431965,10.3942386), new LatLng(63.4324244,10.4031593));
        handler.startTravel(System.currentTimeMillis());
        List<BikeTravelHandler.PositionTimeDistanceTuple> path = handler.getPath();
        assertTrue(tuples[0].position.distance(path.get(0).position) < 10);
        assertTrue(tuples[1].position.distance(path.get(1).position) < 10);
    }
}