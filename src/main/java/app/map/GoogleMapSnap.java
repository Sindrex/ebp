package app.map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a snapped point returned as a result from Google's
 * Snapped Point API.
 * @author Joergen Bele Reinfjell
 */
class SnappedPoint {
    static class Location {
        String latitude;
        String longitude;
    }

    private Location location;
    private String placeId;
    private int originalIndex;

    /**
     * @return the location as a LatLong.
     */
    public LatLng getLatLng() {
        return new LatLng(Double.parseDouble(location.latitude),
                Double.parseDouble(location.longitude));
    }
}

/**
 * This class represents the list of snapped points returned from Google's
 * Snapped Point API.
 * @author Joergen Bele Reinfjell
 */
class SnappedPoints {
    private List<SnappedPoint> snappedPoints;

    /**
     * Gets the list of snapped points.
     * @return list of snapped points, if any.
     */
    public List<SnappedPoint> getSnappedPoints() {
        return snappedPoints;
    }
}

/**
 * This class handles the "snapping" of locations to roads using Google's various snapping APIs.
 */
public class GoogleMapSnap {
    private final String GOOGLE_MAPS_API_KEY;

    public GoogleMapSnap(String apiKey) {
        this.GOOGLE_MAPS_API_KEY = apiKey;
    }

    /**
     * Builds the url for a Google Snapping API.
     * @param url the google api url including "?points=" or "?path=" for
     *           nearestRoads or snapToRoad respectively.
     * @param locations the list of locations.
     * @return the built url containing the locations and API key.
     */
    private String buildSnapToURL(String url, final List<LatLng> locations) {
        StringBuilder builder = new StringBuilder(url);
        //builder.append("https://roads.googleapis.com/v1/nearestRoads?points=");
        for (int i = 0; i < locations.size(); i++) {
            LatLng location = locations.get(i);
            builder.append(location.getLat());
            builder.append(",");
            builder.append(location.getLng());
            if (i + 1 < locations.size()) {
                builder.append("|");
            }
        }
        builder.append("&key=");
        builder.append(GOOGLE_MAPS_API_KEY);

        return builder.toString();
    }
    private String buildSnapToRoadsURL(final List<LatLng> locations) {
        return buildSnapToURL("https://roads.googleapis.com/v1/snapToRoads?path=", locations);
    }
    private String buildNearestRoadsURL(final List<LatLng> locations) {
        return buildSnapToURL("https://roads.googleapis.com/v1/nearestRoads?points=", locations);
    }

    /**
     * Uses Google's Snap To Roads API to snap a list of locations to the nearest road.
     * @param googleMapSnapType the snap type to use.
     * @param locations the list of locations to try to fit.
     * @return the list of snapped locations, if any were found.
     */
    public List<LatLng> snapTo(GoogleMapSnapType googleMapSnapType, final List<LatLng> locations){
        try {
            URL url = new URL(
                    googleMapSnapType == GoogleMapSnapType.SNAP_NEAREST_ROAD ?
                            buildNearestRoadsURL(locations)
                            : buildSnapToRoadsURL(locations));
            try (InputStreamReader inputStreamReader = new InputStreamReader(url.openStream())) {
                // XXX: Are nested try's needed?
                try (BufferedReader bir = new BufferedReader(inputStreamReader)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    bir.lines().forEach(stringBuilder::append);
                    String json = stringBuilder.toString();

                    System.out.println(json);

                    // XXX: Better error detection?
                    if ("{}".equals(json)) {
                        return Collections.emptyList();
                    }

                    Gson gson = new Gson();
                    Type collectionType = new TypeToken<SnappedPoints>(){}.getType();
                    SnappedPoints points = gson.fromJson(json, collectionType);
                    Stream<LatLng> p = points.getSnappedPoints().stream().map(SnappedPoint::getLatLng);
                    return p.collect(Collectors.toList());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
