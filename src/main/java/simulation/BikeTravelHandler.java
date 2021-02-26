package simulation;

import app.map.GoogleMapUtils;
import app.map.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.model.*;
import logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This class handles the simulation of a bikes travel route,
 * taking the bike from point A to point B in some amount of time..
 * @author Joergen Bele Reinfjell
 * @author Odd-Erik Frantzen
 */
public class BikeTravelHandler {
    static class PositionTimeDistanceTuple {
        public long distanceMeters;
        public LatLng position;
        public long timeMillis;


        PositionTimeDistanceTuple(LatLng position, long timeMillis, long distanceMeters) {
            this.position = position;
            this.timeMillis = timeMillis;
            this.distanceMeters = distanceMeters;
        }

        @Override
        public String toString() {
            return String.format("{distaceMeters: %s, position: %s, timeMillis: %d}",
                    distanceMeters, position, timeMillis);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof PositionTimeDistanceTuple)) {
                return false;
            }
            PositionTimeDistanceTuple p = (PositionTimeDistanceTuple) o;
            boolean ret =  (p.timeMillis == timeMillis) && (p.distanceMeters == distanceMeters) && (p.position.equals(position));
            return ret;
        }
    }

    // Start and end of the travel.
    private final LatLng start;
    private final LatLng end;
    private List<LatLng> waypoints;

    private LatLng position;

    // The list of all travel routes.
    private DirectionsRoute[] route = null;
    private List<PositionTimeDistanceTuple> path = new ArrayList<>();
    private int pathIndex = 0;

    private long travelStartTime = -1;

    private double travelledMeters = 0;
    private double travelledMetersSinceLast = 0;
    private int countWaypoints;

    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean done = new AtomicBoolean(false);

    private static final long MILLISECONDS_TIME_FACTOR = 1000; // 1000 for 1 second.
    public static final int MAX_WAYPOINT_COUNT = 20;
    public static final int MAX_TRIES = 5;

    public BikeTravelHandler(LatLng start, LatLng end) {
        this(start, end, 0);
    }

    public BikeTravelHandler(LatLng start, LatLng end, int countWaypoints) {
        this.start = start;
        this.position = null;
        if (countWaypoints > MAX_WAYPOINT_COUNT) {
            throw new IllegalArgumentException("Waypoint count: " + countWaypoints + " higher than max: " + MAX_WAYPOINT_COUNT);
        }
        this.countWaypoints = countWaypoints;
        this.waypoints = new ArrayList<>();
        this.end = end;
    }


    /**
     * Starts the bikes travel. This means setting the travel start time
     * and must be called before all other calls.
     */
    public boolean startTravel(long startTimeMillis) {
        if (started.get()) {
            throw new IllegalStateException("Travel already started!");
        }
        travelStartTime = startTimeMillis;
        started.set(true);

        for (int i = 0; i < countWaypoints; i++) {
            int tries = MAX_TRIES;
            LatLng p;

            do {
                p = GoogleMapUtils.getRandomRouteDestination();
            } while (p == null && --tries >= 0);

            if (p == null) throw new IllegalStateException("Cannot fetch random route destinations");
            waypoints.add(p);
        }

/*
        com.google.maps.model.LatLng[] googleWaypoints =
                (com.google.maps.model.LatLng[]) waypoints.stream().map(
                        p -> new com.google.maps.model.LatLng(p.getLat(), p.getLng())).toArray();
*/

        com.google.maps.model.LatLng[] googleWaypoints = new com.google.maps.model.LatLng[waypoints.size()];
        for (int i = 0; i < waypoints.size(); i++) {
            googleWaypoints[i] = new com.google.maps.model.LatLng(waypoints.get(i).getLat(), waypoints.get(i).getLng());
        }

        DirectionsApiRequest req = DirectionsApi.newRequest(GoogleMapUtils.GEO_API_CONTEXT)
                .waypoints(googleWaypoints)
                .units(Unit.METRIC)
                .mode(TravelMode.BICYCLING)
                .origin(start.toString())
                .destination(end.toString());

        try {
            DirectionsResult result = req.await();
            route = result.routes;
            path = buildTravelPath(route);
            pathIndex = 0;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Builds a list of travel points (path points) and time pairs for the given route.
     * @param route the travel route.
     * @return the list of pairs.
     */
    public List<PositionTimeDistanceTuple> buildTravelPath(DirectionsRoute[] route) {
        List<PositionTimeDistanceTuple> tuples = new ArrayList<>();
        long timeElapsed = 0;

        for (DirectionsRoute r : route) {
            for (DirectionsLeg l : r.legs) {
                for (DirectionsStep s : l.steps) {
                    List<LatLng> latLngs = s.polyline.decodePath()
                            .stream().map(gll -> new LatLng(gll.lat, gll.lng)).collect(Collectors.toList());
                    if (latLngs.size() < 1) {
                        Logger.log("Encountered empty polyline!");
                        timeElapsed += s.duration.inSeconds*MILLISECONDS_TIME_FACTOR;
                        continue;
                    }

                    for (int i = 1; i < latLngs.size(); i++) {
                        // The shortest distance between the two path points (in meters).
                        double distance = latLngs.get(i-1).distance(latLngs.get(i));
                        // An rough estimate on the time needed.
                        long pointsMillis = (long) (distance/s.distance.inMeters * s.duration.inSeconds*MILLISECONDS_TIME_FACTOR);
                        timeElapsed += pointsMillis;

                        //LatLng stepEnd = latLngs.get(latLngs.size()-1);
                        //PositionTimeDistanceTuple tuple = new PositionTimeDistanceTuple(stepEnd, timeElapsed, (long) distance);
                        PositionTimeDistanceTuple tuple = new PositionTimeDistanceTuple(latLngs.get(i), timeElapsed, (long) distance);
                        //Logger.logf("Adding tuple: %s", tuple);
                        tuples.add(tuple);
                    }
                }
            }
        }
        return tuples;
    }


    /**
     * Updates the position to the estimated position using the current
     * directions step's path and the time elapsed since the start.
     */
    public void updatePosition(long currentTimeMillis) {
        if (pathIndex >= path.size()) {
            done.set(true);
            if (path.size() == 0)
                position = start;
            return;

        }

        PositionTimeDistanceTuple tuple = path.get(pathIndex);

        // TODO: use the vector between the previous and the next path
        // element to "extrapolate" a position.
        PositionTimeDistanceTuple tuple1 = tuple;
        while (currentTimeMillis - travelStartTime >= tuple1.timeMillis) {
            // Moved to the next path element.
            pathIndex++;
            this.position = tuple.position;
            if (pathIndex >= path.size()) {
                done.set(true);
                break;
            }
            tuple1 = path.get(pathIndex);
            travelledMetersSinceLast = tuple.distanceMeters;
            travelledMeters += tuple.distanceMeters;
        }

        if (this.position == null) {
            this.position = tuple.position;
        }
        //System.out.println("complete updatePosition() call took: " + (eTimeMillis - sTimeMillis) + " ms");
    }

    /**
     * @return the next position on the path.
     */
    public LatLng getNext() {
        if (pathIndex + 1 < path.size()) {
            return path.get(pathIndex+1).position;
        }
        return null;
    }

    public boolean isDone() {
        return done.get();
    }

    public LatLng getPosition() {
        return position;
    }

    public double getTravelledMeters() {
        return travelledMeters;
    }

    public double getTravelledMetersSinceLast() {
        return travelledMetersSinceLast;
    }


    public boolean hasStarted() {
        return started.get();
    }

    /**
     * @apiNote is only used in JUnit test.
     * @return an unmodifiable list containing the paths positions.
     */
    public List<PositionTimeDistanceTuple> getPath() {
        return Collections.unmodifiableList(path);
    }

    public int getPathIndex() {
        return pathIndex;
    }
}
