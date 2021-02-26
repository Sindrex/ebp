package app.map;

/**
 * This class represents a GPS coordinate.
 * @author Joergen Bele Reinfjell
 * @see GoogleMapMarker
 */
public class LatLng implements Comparable<LatLng> {
    public static final double EPSILON = 0.0005;

    private final double lat;
    private final double lng;

    public LatLng(double lat, double lng) {
        if (lat <= -90 || lat >= 90) {
            throw new IllegalArgumentException(lat + " is not a valid latitude, must be -90 <= lat <= 90");
        } else if (lng <= -180 || lng >= 180) {
            throw new IllegalArgumentException(lng + " is not a valid longitude, must be -180 <= lnt <= 180");
        }
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * Calculate the distance between two 'LatLng's in meters.
     * @param latLng the other point.
     * @return the distance between the two points.
     * @author Sindre Haugland Paulshus (modified by Joergen Bele Reinfjell)
     */
    public double distance(LatLng latLng) {
        // Common values
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(latLng.lat - lat);
        double lonDistance = Math.toRadians(latLng.lng - lng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(latLng.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double distance = R * c * 1000; // convert to meters

        return distance;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public String toString() {
        return String.format("%f,%f", lat, lng);
    }

    private static int compareDecimal(double d1, double d2) {
        final double delta = Math.abs(d1 - d2);
        if (delta < EPSILON) {
            return 0;
        } else if (d1 < d2) {
            return -1;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof LatLng)) {
            return false;
        }
        LatLng l = (LatLng) o;
        return compareTo((LatLng) o) == 0;
    }

    @Override
    public int compareTo(LatLng latLng) {
        int r1 = compareDecimal(lat, latLng.lat);
        if (r1 != 0) {
            return r1;
        }
        return compareDecimal(lng, latLng.lng);
    }
}
