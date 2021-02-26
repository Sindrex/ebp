package app.map;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import config.Configuration;
import logging.Logger;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This class contains various utility functions for simplified use of some Google's Map APIs.
 * @author Joergen Bele Reinfjell
 * @see GoogleMapSnap
 * @see GoogleMapView
 */
public class GoogleMapUtils {
    public final static String GOOGLE_MAPS_API_KEY = Configuration.getProperties().getProperty(Configuration.GOOGLE_API_KEY_PROPERTY_ENTRY);
    public final static GeoApiContext GEO_API_CONTEXT = new GeoApiContext.Builder().apiKey(GOOGLE_MAPS_API_KEY).build();

    //public final static GeoApiContext GEO_API_CONTEXT = new GeoApiContext().setApiKey(GOOGLE_MAPS_API_KEY);


    public final static LatLng NIDAROS_CATHEDRAL = new LatLng(63.4278489, 10.3938181);
    public final static LatLng NORTH_EAST = new LatLng(63.4393877,10.5078055);
    public final static LatLng SOUTH_WEST = new LatLng(63.3248793,10.2984027);


    // The maximum to cache which is also the maximum number of positions the API supports per request.
    private static final int MAX_CACHED_POSITIONS = 100;

    // Cache the random positions to limit the need for API requests.
    private final static ArrayBlockingQueue<LatLng> positionsCache = new ArrayBlockingQueue<>(MAX_CACHED_POSITIONS);

    private final static int MAX_FAILURES = 5;

    /**
     * Fetches MAX_CACHED_POSITIONS new random positions within the given bounds, if the current cache is empty.
     * @param northEast the north east boundary.
     * @param southWest the south west boundary.
     * @return true on success, false on failure.
     */
    private static synchronized boolean fetchRandomBoundedPositions(LatLng southWest, LatLng northEast) {
        if (!positionsCache.isEmpty()) {
            return false;
        }

        while (true) {
            List<LatLng> positions = getRandomValidPositions(MAX_CACHED_POSITIONS);
            if (!positions.isEmpty()) {
                positionsCache.addAll(positions.subList(0, Math.min(MAX_CACHED_POSITIONS, positions.size())));
                //positionsCache.addAll(positions);
                return true;
            } else {
                Logger.log("GoogleMapUtils: getRandomValidPositions() returned empty collection. Sleeping and retrying..");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static DirectionsRoute[] fetchRandomTravelRoute(LatLng origin) {
        return fetchRandomTravelRoute(GEO_API_CONTEXT, origin);
    }

    public static LatLng getRandomRouteDestination() {
        fetchRandomBoundedPositions(SOUTH_WEST, NORTH_EAST);
        try {
            return positionsCache.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Fetches a random travel route using google maps' directions API.
     * @return the next position.
     */
    public static DirectionsRoute[] fetchRandomTravelRoute(GeoApiContext context, LatLng origin) {
        try {
            fetchRandomBoundedPositions(SOUTH_WEST, NORTH_EAST);
            LatLng destination = positionsCache.take();
            DirectionsApiRequest req = DirectionsApi.newRequest(context)
                    .units(Unit.METRIC)
                    .mode(TravelMode.BICYCLING)
                    .origin(origin.toString())
                    .destination(destination.toString());

            try {
                DirectionsResult result = req.await();
                return result.routes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a random but valid road position to the map.
     * Only here for testing.
     */
    private static List<LatLng> getRandomValidPositions(int maxPositions) {
        if (maxPositions <= 0 || maxPositions > 100) {
            throw new IllegalArgumentException("maxPositions must > 0 and <= 100");
        }

        Random random = new Random();
        int failures = 0;
        while (failures < MAX_FAILURES) {
            /*
             * Send 100 random positions in one go to make the probability to
             * get at least one correct high enough so that we do not have to
             * repeatedly spam Google's servers.
             */
            List<LatLng> randLatLongs = new ArrayList<>();
            for (int i = 0; i < maxPositions; i++) {
                LatLng randLatLong = new LatLng(SOUTH_WEST.getLat() + (NORTH_EAST.getLat() - SOUTH_WEST.getLat()) * random.nextDouble(),
                        SOUTH_WEST.getLng() + (NORTH_EAST.getLng() - SOUTH_WEST.getLng()) * random.nextDouble());
                randLatLongs.add(randLatLong);
            }

            GoogleMapSnap googleMapSnap = new GoogleMapSnap(GOOGLE_MAPS_API_KEY);
            List<LatLng> snapped = googleMapSnap.snapTo(GoogleMapSnapType.SNAP_NEAREST_ROAD, randLatLongs);
            if (snapped.size() >= 1) {
                return snapped;
            }

            Logger.log("GoogleMapUtils: Failed to generate random valid position, looping..");
            failures++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
