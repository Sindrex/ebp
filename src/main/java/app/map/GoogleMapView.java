/*
 * Bugs to fix:
 * - Events are not catched after a while.
 */

package app.map;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import logging.Logger;
import netscape.javascript.JSObject;

import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class contains and handles a WebView controlling a Google Maps view.
 * @author Joergen Bele Reinfjell
 */
public class GoogleMapView extends AnchorPane {
    public Callback<Integer, Void> getOnMarkerDraggedCallback() {
        return onMarkerDraggedCallback;
    }

    /**
     * Bridge class used for communication between the JavaFx application and JavaScript in the web view.
     * @author Joergen Bele Reinfjell
     */
    public class Bridge {
        private final GoogleMapView mapView;
        private final Callback<GoogleMapView, Void> mapInitCallback;
        private final Callback<LatLng, Void> clickCallback;
        private final Callback<Integer, Void> markerClickCallback;
        private final Callback<Integer, Void> markerDraggedCallback;

        public Bridge(GoogleMapView mapView, Callback<LatLng, Void> clickCallback,
                      Callback<Integer, Void> markerClickCallback, Callback<Integer, Void> markerDraggedCallback,
                      Callback<GoogleMapView, Void> mapInitCallback) {
            this.mapView = mapView;
            this.clickCallback = clickCallback;
            this.markerClickCallback = markerClickCallback;
            this.markerDraggedCallback = markerDraggedCallback;
            this.mapInitCallback = mapInitCallback;
        }

        public void onMapClick(double lat, double lng) {
            System.out.println("onMapClick(): Map clicked at: " + lat + ", " + lng);
            clickCallback.call(new LatLng(lat, lng));
        }

        public void onMapInit() {
            System.out.println("onMapInit(): MAP INITIALIZED!");
            mapInitCallback.call(mapView);
        }

        public void onMarkerClick(int id) {
            System.out.println("markerClickCallback(): marker: " + id + " clicked");
            markerClickCallback.call(id);
        }

        public void onMarkerDragged(int id) {
            System.out.println("markerDraggedCallback(): Marker " + id + "dragged");
            markerDraggedCallback.call(id);
        }


        public void log(String s) {
            Logger.logf("JavaScript: %s", s);
        }
    }

    private WebView webView = null;
    private HashMap<Integer, GoogleMapMarker> markerMap = new HashMap<>();
    private AtomicBoolean loaded = new AtomicBoolean(false);

    private Callback<LatLng, Void> onClickCallback = map -> null;
    private Callback<Integer, Void> onMarkerClickCallback = map -> null;
    private Callback<Integer, Void> onMarkerDraggedCallback = map -> null;

    private Callback<GoogleMapView, Void> userSetMapInitCallback = map -> null;
    private Callback<GoogleMapView, Void> onMapInitCallback = map -> {
        loaded.set(true);
        userSetMapInitCallback.call(map);
        return null;
    };

    private Bridge bridge = null;

    private synchronized Object execute(String scriptFmt, Object... args) {
        return execute(false, scriptFmt, args);
    }

    private synchronized Object execute(boolean force, String scriptFmt, Object... args) {
        if (!force && !isLoaded()) { throw new IllegalStateException("Map is not loaded yet!"); }
        Formatter formatter = new Formatter(Locale.US);
        return getWebEngine().executeScript(formatter.format(scriptFmt, args).toString());
    }

    public synchronized void load() {
        if (isLoaded()) {
            throw new IllegalStateException("Map is already loaded!");
        }
        webView = new WebView();

        Logger.log("GoogleMapView: Loading map: " + hashCode());
        getWebEngine().setJavaScriptEnabled(true);
        getChildren().add(webView);
        AnchorPane.setBottomAnchor(webView,0.0);
        AnchorPane.setTopAnchor(webView,0.0);
        AnchorPane.setLeftAnchor(webView,0.0);
        AnchorPane.setRightAnchor(webView,0.0);
        setVisible(true);

        // Needed for worker.
        GoogleMapView instance = this;

        bridge = new Bridge(instance, onClickCallback, onMarkerClickCallback,
                onMarkerDraggedCallback, onMapInitCallback);
        JSObject window = (JSObject) getWebEngine().executeScript("window");
        window.setMember("bridge", bridge);

        getWebEngine().load(getClass().getResource("GoogleMap.html").toExternalForm());
    }

    public synchronized boolean panTo(LatLng latLng) {
        if (!isLoaded()) { throw new IllegalStateException("Map is not loaded yet!"); }
        return (Boolean) execute("document.panTo(%f, %f);", latLng.getLat(), latLng.getLng());
    }

    /**
     * Utility method for quoting a string used when passing strings to javascript.
     * @param s the string to be quotes.
     * @return the new quoted string, or null if s is null.
     */
    private static String quoteStringOrNull(String s) {
        if (s == null) {
            return null;
        }
        return String.format("\"%s\"", s);
    }

    /**
     * Adds a marker to the map.
     * @param marker the marker to add.
     * @return true on success, false on failure.
     */
    public synchronized boolean addMarker(GoogleMapMarker marker) {
        if (!isLoaded()) { throw new IllegalStateException("Map not loaded"); }

        if (!marker.isVisible()) {
            return false;
        }

        markerMap.put(marker.getId(), marker);
        execute("document.addMarker(%d, %f, %f, %s, %s, %s, %s);",
                marker.getId(), marker.getLat(), marker.getLng(),
                quoteStringOrNull(marker.getLabel()), quoteStringOrNull(marker.getIconPath()),
                marker.isDraggable(), quoteStringOrNull(marker.getInfo()));

        Logger.logf("Adding marker: %d", marker.getId());

        return true;
    }

    /**
     * Show the info window for a given marker, if it exists.
     * @param markerId the marker's id.
     */
    public synchronized void showMarkerInfoWindow(int markerId) {
        if (!isLoaded()) { throw new IllegalStateException("Map not loaded"); }
        execute("document.showMarkerInfoWindow(%d);", markerId);
    }

    /**
     * Updates all markers in the given list on the map.
     * @param markers the list of markers to update.
     */
    public synchronized void updateAllMarkers(List<GoogleMapMarker> markers) {
        if (!isLoaded()) { throw new IllegalStateException("Map not loaded"); }
        markers.forEach((GoogleMapMarker m) -> {
            if (markerMap.containsKey(m.getId())) {
                if (m.isVisible()) {
                    updateMarker(m);
                } else {
                    removeMarker(m);
                }
            } else if (m.isVisible()) {
                addMarker(m);
            }
        });
    }

    /**
     * Updates the marker to reflect changes done upon it in
     * Google Map's JavaScript representation. Useful for updating
     * the position after a marker drag.
     * @param marker the marker to update.
     */
    public synchronized boolean updateFromMap(GoogleMapMarker marker) {
        if (!isLoaded()) { throw new IllegalStateException("Map is not loaded yet!"); }
        JSObject jsObject = (JSObject) execute("document.getMarkerPos(%d);", marker.getId());
        if (jsObject == null) {
            Logger.errf("Unable to update from map: returned null: id %d", marker.getId());
            return false;
        }
        LatLng pos = new LatLng((Double) jsObject.getMember("lat"), (Double) jsObject.getMember("lng"));
        Logger.logf("Recieved updated marker: %s => %s", marker, pos);

        GoogleMapMarker newMarker = marker.moveTo(pos);
        markerMap.put(newMarker.getId(), newMarker);

        return true;
    }

    /**
     * Changes the icon of a marker.
     * @param marker the marker to change the icon of.
     * @param iconPath the icon path.
     * @return true on success, false on failure.
     */
    public synchronized boolean changeIcon(GoogleMapMarker marker, String iconPath) {
        if (!isLoaded()) { return false; }
        boolean ret = (Boolean) execute("document.changeIconPath(%d, %s);", marker.getId(), quoteStringOrNull(iconPath));

        GoogleMapMarker newMarker = marker.changeIconPath(iconPath);
        markerMap.put(newMarker.getId(), newMarker);

        //boolean ret = (Boolean) getWebEngine().executeScript("document.moveMarker(" +marker.getId() + "," + marker.getLat() + "," + marker.getLng() + ");");
        Logger.logf("Move marker: %d: %s", newMarker.getId(), ret);
        return ret;
    }

    /**
     * Updates the marker info displayed in the markers info window.
     * @param marker the marker which the info window is connected to.
     * @param info the HTML contents of the info window.
     * @return true on success, false on failure.
     */
    public synchronized boolean updateMarkerInfo(GoogleMapMarker marker, String info) {
        if (!isLoaded()) { return false; }
        boolean ret = (Boolean) execute("document.updateMarkerInfo(%d, %s);", marker.getId(), quoteStringOrNull(info));

        GoogleMapMarker newMarker = new GoogleMapMarker.Builder().from(marker).info(info).build();
        markerMap.put(newMarker.getId(), newMarker);

        //boolean ret = (Boolean) getWebEngine().executeScript("document.moveMarker(" +marker.getId() + "," + marker.getLat() + "," + marker.getLng() + ");");
        Logger.logf("Updated marker info: %d: %s", newMarker.getId(), ret);
        return ret;
    }

    /**
     * Removes the marker from the map.
     * @param marker the marker to remove.
     * @return true on success, false on failure.
     */
    public synchronized boolean removeMarker(GoogleMapMarker marker) {
        if (!isLoaded()) { return false; }
        boolean ret = (Boolean) execute("document.removeMarker(%d);", marker.getId());
        markerMap.remove(marker.getId());
        Logger.logf("Removed marker: %d: %s", marker.getId(), ret);
        return ret;
    }

    /**
     * Updates the given marker on the map.
     * @param marker the marker to update.
     * @return true on success, false on failure.
     */
    public synchronized boolean updateMarker(GoogleMapMarker marker) {
        if (!isLoaded()) { return false; }
        execute("document.updateMarker(%d, %f, %f, %s, %s, %s, %s);",
                marker.getId(), marker.getLat(), marker.getLng(),
                quoteStringOrNull(marker.getLabel()), quoteStringOrNull(marker.getIconPath()),
                marker.isDraggable(), quoteStringOrNull(marker.getInfo()));
        //Logger.logf("Updated marker: %d: %s", marker.getId(), ret);
        return true;
    }

    /**
     * Moves the given marker to the new position.
     * @param marker the marker to move.
     * @param pos the new position.
     * @return true on success, false on failure.
     */
    public synchronized boolean moveMarker(GoogleMapMarker marker, LatLng pos) {
        if (!isLoaded()) { return false; }

        if (!markerMap.containsKey(marker.getId())) {
            GoogleMapMarker newMarker = new GoogleMapMarker.Builder().from(marker).position(pos).build();
            return addMarker(newMarker);
        }

        boolean ret = (Boolean) execute("document.moveMarker(%d, %f, %f);",
                marker.getId(), pos.getLat(), pos.getLng());

        GoogleMapMarker newMarker = marker.moveTo(pos);
        markerMap.put(newMarker.getId(), newMarker);

        //boolean ret = (Boolean) getWebEngine().executeScript("document.moveMarker(" +marker.getId() + "," + marker.getLat() + "," + marker.getLng() + ");");
        Logger.logf("Move marker: %d: %s", newMarker.getId(), ret);
        return ret;
    }

    /**
     * Removes all markers from the map.
     * @return true on success, false on failure.
     */
    public synchronized boolean removeAllMarkers() {
        if (!isLoaded()) { throw new IllegalStateException("Map not loaded"); }
        Logger.log("GoogleMapView: executing removal script!");
        execute("document.deleteMarkers();");
        //getWebEngine().executeScript("document.deleteMarkers();");
        Logger.log("GoogleMapView: script execution successful!");
        return true;
    }

    /**
     * Sets the on init callback.
     * @param onMapInitCallback the callback to be called when the map
     *                          has completed initialization.
     */
    public synchronized void setOnMapInitCallback(Callback<GoogleMapView, Void> onMapInitCallback) {
        this.userSetMapInitCallback = onMapInitCallback; // callback to user of this class.
    }

    /**
     * Sets the on click callback.
     * @param onClickCallback the callback to be called when the map
     *                        has been clicked.
     */
    public synchronized void setOnClick(Callback<LatLng, Void> onClickCallback) {
        this.onClickCallback = onClickCallback;
    }

    /**
     * Sets the on marker click callback.
     * @param onMarkerClickCallback the callback to be called when a marker
     *                        has been clicked.
     */
    public synchronized void setOnMarkerClickCallback(Callback<Integer, Void> onMarkerClickCallback) {
        this.onMarkerClickCallback = onMarkerClickCallback;
    }

    /**
     * Sets the on marker dragged callback.
     * @param onMarkerDraggedCallback the callback to be called when a marker
     *                        has been dragged.
     */
    public synchronized void setOnMarkerDraggedCallback(Callback<Integer, Void> onMarkerDraggedCallback) {
        this.onMarkerDraggedCallback = onMarkerDraggedCallback;
    }

    /**
     * Sets the map zoom level.
     * @param zoomLevel the map zoom level.
     */
    public void setMapZoom(int zoomLevel){
        if (zoomLevel < 0 || zoomLevel > 20) {
            throw new IllegalArgumentException("zoomLevel must be satisfy: 0 < zoomLevel <= 20");
        }
        if (!isLoaded()) { throw new IllegalStateException("Map not loaded"); }
        Logger.log("GoogleMapView: executing setZoom script!");
        execute("document.setZoom(%d);", zoomLevel);
        //getWebEngine().executeScript(String.format("document.map.setZoom(%d);", zoomLevel));
        Logger.log("GoogleMapView: script execution successful!");
    }

    /**
     * Wrapper function to check if the map view has completed loading.
     * @return true if the map has loaded, else false.
     */
    public boolean isLoaded() {
        return loaded.get();
    }

    /**
     * Gets the map's web view.
     * @return the web view.
     */
    public synchronized WebView getWebView() {
        return webView;
    }

    /**
     * Gets the map's web engine.
     * @return the web engine.
     */
    public synchronized WebEngine getWebEngine() {
        return webView.getEngine();
    }

    /**
     * Returns a marker found by the given id.
     * @param markerId the marker's id.
     * @return the marker if found, or null if not found.
     */
    public synchronized GoogleMapMarker getMarkerById(int markerId) {
        return markerMap.get(markerId);
    }
}
