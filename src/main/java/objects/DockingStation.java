package objects;

import app.map.LatLng;
import db.dao.DockingStationDao;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sindre Thomassen
 * date 08.03.2018
 * @see DockingStationDao
 */
public class DockingStation {
    public static final double DEFAULT_POWER_USAGE = 0.0;
    private static final double powerBikeTakes = 25;
    private final int MAX_BIKES;
    private final LatLng position;
    private int dockID;
    /*
     * currentPowerUsage was changed to presentPowerUsage on account of current having a double meaning
     * of which one is usually used in a context with power, and we want it to mean the other one.
     */
    private double powerUsage;
    private List<Integer> bikes;
    private ActiveStatus status;
    private Timestamp statusTimestamp;
    /**
     *
     * @param dockID
     * @param posLong
     * @param posLat
     */
    @Deprecated
    public DockingStation(int dockID, int maxBikes, double posLong, double posLat, double powerUsage, ActiveStatus status, Timestamp statusTimestamp) {
        this.MAX_BIKES = maxBikes;
        this.status = status;
        this.dockID = dockID;
        this.position = new LatLng(posLat, posLong);
        this.powerUsage = powerUsage;
        this.bikes = new ArrayList<>();
        this.statusTimestamp = statusTimestamp;
    }

    public DockingStation(int dockID, int maxBikes, LatLng position, double powerUsage, ActiveStatus status, Timestamp statusTimestamp, List<Integer> bikes){
        this.MAX_BIKES = maxBikes;
        this.status = status;
        this.dockID = dockID;
        this.position = position;
        this.powerUsage = powerUsage;
        this.bikes = new ArrayList<>();
        this.statusTimestamp = statusTimestamp;
        this.bikes = bikes;

    }

    public DockingStation(int dockID, int maxBikes, LatLng position, double powerUsage, ActiveStatus status, Timestamp statusTimestamp) {
        this.MAX_BIKES = maxBikes;
        this.status = status;
        this.dockID = dockID;
        this.position = position;
        this.powerUsage = powerUsage;
        this.bikes = new ArrayList<>();
        this.statusTimestamp = statusTimestamp;

    }

    public DockingStation(int id){
        this.dockID = id;
        this.MAX_BIKES = 0;
        this.position = null;
    }

    @Deprecated
    public DockingStation(int dockID, int maxBikes, double posLong, double posLat, double powerUsage,
                          ActiveStatus status, List<Integer> newBikes, List<DockingStationHistory> history, Timestamp statusTimestamp) {
        this(dockID, maxBikes, posLong, posLat, powerUsage, status, statusTimestamp);
        this.bikes = newBikes;
    }

    @Deprecated
    public DockingStation(int dockID, int maxBikes, LatLng position, double powerUsage,
                          ActiveStatus status, List<Integer> newBikes, List<DockingStationHistory> history, Timestamp statusTimestamp) {

        this(dockID, maxBikes, position, powerUsage, status, statusTimestamp);
        this.bikes = newBikes;
    }

    public Timestamp getStatusTimestamp() {
        return statusTimestamp;
    }

    @Deprecated
    public List<DockingStationHistory> getHistory() {
        return null;
    }

    @Deprecated
    public void addStory(DockingStationHistory story) {

    }

    /**
     * Updates current powerusage according to how many bikes are at the dockingstation.
     */
    public void updatePowerUsage() { return; } // To be implemented.

    /**
     *
     * @return dockID
     */
    public int getDockID() { return dockID; }

    /**
     *
     * @return posLat
     */
    public double getPosLong() { return position.getLng(); }

    /**
     *
     * @return posLat
     */
    public double getPosLat() { return position.getLat(); }

    /**
     * @return the position as a LatLng.
     */
    public LatLng getPosition() {
        return position;
    }

    /**
     *
     * @return a list of bikes currently docked at the particular dockingstation
     */
    public List<Integer> getBikes() {
        List<Integer> help = new ArrayList<>();
        for(int i : bikes) {
            help.add(i);
        }
        return help;
    }

    /**
     *
     * @return presentPowerUsage (kW)
     */
    /*
    public double getPresentPowerUsage() {
        this.presentPowerUsage = bikes.size() * powerBikeTakes; //  kW/h
        return presentPowerUsage;
    }
    */

    /**
     *
     * //@param newBike
     * //@return true if newBike is added to bikes, false if newBike is already in bikes or station is full
     */
    /*public boolean addBike(Bike newBike) {
        if(bikes.size() == MAX_BIKES)
            return false;
        for(int id : bikes) {
            if(id == newBike.getId())
                return false;
        }
        bikes.add(newBike.getId());
        return true;
    }*/

    public void setBikes(List<Integer> newBikes) {
        bikes = newBikes;
    }

    /**
     * Get the number of bikes currently docked.
     * @return the number of bikes currently docked.
     */
    public int getNumBikes() {
        return bikes.size();
    }

    /**
     *
     * @param status
     * @return returns true if status was changed, but false if new status is the same as current status
     */
    public void changeStatus(ActiveStatus status) { // Needs to be added to the class diagram.
        this.status = status;
    }

    /**
     *
     * Sets the id of a dock station to new id.
     * @param newId
     */
    public void setId(int newId) {
        dockID = newId;
    }

    /**
     *
     * @return maximum number of bikes that can fit in the docking station.
     */
    public int getMAX_BIKES() {
        return MAX_BIKES;
    }

    /**
     *
     * @return active status. Active if the station is in use, and deleted if the station is deleted.
     */
    public ActiveStatus getStatus() {
        return status;
    }

    public double getPowerUsage() {
        return powerUsage;
    }

    /**
     * Enum class containging the various DockingStation states.
     */
    public enum ActiveStatus {
        ACTIVE("ACTIVE"), DELETED("DELETED");

        private final String stringEnum;
        ActiveStatus(String stringEnum) { this.stringEnum = stringEnum; }

        public static ActiveStatus from(int s) {
            for (ActiveStatus elem : ActiveStatus.values()) {
                if(elem.asInt() == s) {
                    return elem;
                }
            }
            return null;
        }

        public String asString() { return stringEnum; }

        public int asInt() {
            if(asString().equals("DELETED")) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     * Builder for DockingStation.
     */
    public static class Builder {
        private int dockID = -1;
        private double powerUsage = DEFAULT_POWER_USAGE;
        private int MAX_BIKES = Integer.MAX_VALUE;
        private LatLng position;
        private ActiveStatus status = ActiveStatus.ACTIVE;
        private List<Integer> bikes = new ArrayList<>();
        private Timestamp statusTimestamp;

        public DockingStation build() {
            return new DockingStation(dockID, MAX_BIKES, position, powerUsage, status, statusTimestamp, bikes);
        }

        public Builder from(DockingStation d) {
            dockID = d.dockID;
            powerUsage = d.powerUsage;
            MAX_BIKES = d.MAX_BIKES;
            position = d.position;
            status = d.status;
            bikes = d.bikes;
            statusTimestamp = d.statusTimestamp;
            return this;
        }

        public Builder dockID(int dockID) {
            this.dockID = dockID;
            return this;
        }

        public Builder powerUsage(double powerUsage) {
            this.powerUsage = powerUsage;
            return this;
        }

        public Builder maxBikes(int maxBikes) {
            this.MAX_BIKES = maxBikes;
            return this;
        }

        public Builder position(LatLng position) {
            this.position = position;
            return this;
        }

        public Builder status(ActiveStatus status) {
            this.status = status;
            return this;
        }

        public Builder bikes(List<Integer> bikes) {
            this.bikes = bikes;
            return this;
        }

        public Builder statusTimestamp(Timestamp statusTimestamp){
            this.statusTimestamp = statusTimestamp;
            return this;
        }

    }
}
