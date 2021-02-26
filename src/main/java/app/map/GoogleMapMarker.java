package app.map;

/**
 * This immutable class represents an interface to a Google Map Marker.
 * @author Joergen Bele Reinfjell
 * @see GoogleMapView
 */
public class GoogleMapMarker implements Comparable {
    private final LatLng position;

    /*
     * Used to uniquely identify a marker, as two markers can
     * have the same positions but still be different.
     * XXX: Is this really a good way of doing things?
     */
    private final int id;

    private final String label;
    private final String iconPath;
    private final boolean draggable;
    private final boolean visible;

    // The (optional) info window text string. Formatted using HTML.
    private final String info;

    public GoogleMapMarker(LatLng position) {
        this.position = position;
        this.id = hashCode();
        this.label = null;
        this.iconPath = null;
        this.draggable = false;
        this.visible = true;
        this.info = null;
    }

    public GoogleMapMarker(LatLng position, int id, String label, String iconPath, boolean draggable) {
        if (position == null) {
            throw new IllegalArgumentException("position must be non-null!");
        }
        this.position = position;
        this.id = id;
        this.label = label;
        this.iconPath = iconPath;
        this.draggable = draggable;
        this.visible = true;
        this.info = null;
    }

    public GoogleMapMarker(LatLng position, int id, String label, String iconPath, boolean draggable, boolean visible) {
        if (position == null) {
            throw new IllegalArgumentException("position must be non-null");
        }
        this.position = position;
        this.id = id;
        this.label = label;
        this.iconPath = iconPath;
        this.draggable = draggable;
        this.visible = visible;
        this.info = null;
    }

    public GoogleMapMarker(LatLng position, int id, String label, String iconPath, boolean draggable, boolean visible, String info) {
        if (position == null) {
            throw new IllegalArgumentException("position must be non-null");
        }
        this.position = position;
        this.id = id;
        this.label = label;
        this.iconPath = iconPath;
        this.draggable = draggable;
        this.visible = visible;
        this.info = info;
    }

    public double getLat() {
        return position.getLat();
    }

    public double getLng() {
        return position.getLng();
    }

    public int getId() {
        return id;
    }

    public LatLng getPosition() {
        return position;
    }

    /**
     * Creates a new marker from the current one with a new position.
     * @param newPos the new icon position.
     * @return the new marker.
     */
    public GoogleMapMarker moveTo(LatLng newPos) {
        return new GoogleMapMarker(newPos, id, label, iconPath, draggable);
    }

    /**
     * Creates a new marker from the current one using a new icon path.
     * @param newIconPath the new icon path.
     * @return the new marker.
     */
    public GoogleMapMarker changeIconPath(String newIconPath) {
        return new GoogleMapMarker(position, id, label, newIconPath, draggable);
    }

    /**
     * Compares two objects. If the object is an instance of the
     * GoogleMapMarker class/subclass it will first check the id,
     * then the visible flag, then the info text, and at last
     * the position.
     *
     * @param o the object to compare with.
     * @return < 0 if smaller, > 0 if greater and 0 if the same.
     */
    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return 0;
        }

        if (o instanceof GoogleMapMarker) {
            GoogleMapMarker m = (GoogleMapMarker) o;
            if (getId() < m.getId()) {
                return -1;
            } else if (getId() > m.getId()) {
                return 1;
            }

            if (isVisible() && !m.isVisible()) {
                return 1;
            } else if (!isVisible() && m.isVisible()) {
                return -1;
            }

            if (info != null && m.getInfo() != null) {
                int ret = info.compareTo(m.getInfo());
                if (ret != 0) {
                    return ret;
                }
            }

            if (getLat() != m.getLat()) {
                return getLat() < m.getLat() ? -1 : 1;
            } else if (getLng() != m.getLng()) {
                return getLng() < m.getLng() ? -1 : 1;
            }
        }
        return 0;
    }

    /**
     * Compares two Google Map markers.
     * @param m the marker to compare to.
     * @return true if equal, else false.
     */
    public boolean equals(GoogleMapMarker m) {
        return m != null && compareTo(m) == 0;
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("{ id: ").append(id)
                .append(", lat: ").append(getLat())
                .append(" lng: ").append(getLng()).append("}").toString();
    }

    public String getLabel() {
        return label;
    }

    public String getIconPath() {
        return iconPath;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getInfo() {
        return info;
    }

    /**
     * Builder class for GoogleMapMarker.
     * @see GoogleMapMarker
     * @author Joergen Bele Reinfjell
     */
    public static class Builder {
        private LatLng position;
        private int id = hashCode();
        private String label = null;
        private String iconPath = null;
        private boolean draggable = false;
        private boolean visible = true;
        private String info = null;

        public GoogleMapMarker build() {
            return new GoogleMapMarker(
                    position, id, label, iconPath, draggable, visible, info
            );
        }

        public Builder from(GoogleMapMarker m) {
            position = m.position;
            id = m.id;
            label = m.label;
            iconPath = m.iconPath;
            draggable = m.draggable;
            visible = m.visible;
            info = m.info;
            return this;
        }

        public Builder position(LatLng position) { this.position = position; return this; }
        public Builder id(int id) { this.id = id; return this; }
        public Builder label(String label) { this.label = label; return this; }
        public Builder iconPath(String iconPath) { this.iconPath = iconPath; return this; }
        public Builder draggable(boolean draggable) { this.draggable = draggable; return this; }
        public Builder visible(boolean visible) { this.visible = visible; return this; }
        public Builder info(String info) { this.info = info; return this; }
    }
}
