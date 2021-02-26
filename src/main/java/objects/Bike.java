package objects;

import app.map.LatLng;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Joergen Bele Reinfjell
 * @author Sindre Haugland Paulshus
 * date 05.03.2018
 * @see db.dao.BikeDao
 */
public class Bike {
    private final int Id;
    private final String type;
    private final String make;
    private final double price;
    private final String datePurchased;
    private ActiveStatus activeStatus;
    private int userId;
    private int stationId;
    private int totalTrips;
    private double chargeLevel = 0;

    private Timestamp statusTimestamp;
    private double totalKilometers;
    private LatLng position;

    public Bike(int Id, String type, String make, double price, String datePurchased,
                ActiveStatus activeStatus, int stationId) {
        this.Id = Id;
        this.type = type;
        this.make = make;
        this.price = price;
        this.datePurchased = datePurchased;
        this.activeStatus = activeStatus;
        this.stationId = stationId;
    }

    public Bike(int Id, String type, String make, double price, String datePurchased,
                ActiveStatus activeStatus, int userId, int stationId) {
        this.Id = Id;
        this.type = type;
        this.make = make;
        this.price = price;
        this.datePurchased = datePurchased;
        this.activeStatus = activeStatus;
        this.userId = userId;
        this.stationId = stationId;
    }

    public Bike(int Id, String type, String make, double price, String datePurchased,
                ActiveStatus activeStatus, int userId, int stationId, int totalTrips) {
        this.Id = Id;
        this.type = type;
        this.make = make;
        this.price = price;
        this.datePurchased = datePurchased;
        this.activeStatus = activeStatus;
        this.userId = userId;
        this.stationId = stationId;
        this.totalTrips = totalTrips;
    }

    public Bike(int Id, String type, String make, double price, String datePurchased,
                ActiveStatus activeStatus, int userId, int stationId,  int totalTrips,
                Timestamp statusTimestamp, double totalKilometers, LatLng position, double chargeLevel) {
        this.Id = Id;
        this.type = type;
        this.make = make;
        this.price = price;
        this.datePurchased = datePurchased;
        this.activeStatus = activeStatus;
        this.userId = userId;
        this.stationId = stationId;
        this.totalTrips = totalTrips;
        this.statusTimestamp = statusTimestamp;
        this.totalKilometers = totalKilometers;
        this.position = position;
        this.chargeLevel = chargeLevel;
    }

    /**
     * Gets this Bike's Id.
     * @return the Id as an int.
     */
    public int getId() {
        return Id;
    }

    /**
     * Get this Bike's type
     * @return the type as a String.
     */
    public String getType() {
        return type;
    }

    /**
     * Get this Bike's make.
     * @return the make as a String
     */
    public String getMake() {
        return make;
    }

    /**
     * Get this Bike's price.
     * @return the price as a double.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Get this Bike's date that it was purchased.
     * @return the date as a String (yy-mm-dd)
     */
    public String getDatePurchased() {
        return datePurchased;
    }

    /**
     * Get this Bike's Active Status (rented, docked, repair, deleted).
     * @return the Active Status as a Bike.ActiveStatus (enum).
     */
    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    /**
     * Set this Bike's ActiveStatus.
     */
    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }

    /**
     * Get this bike's status timestamp, the last time it's info was stored in the DB.
     * @return the timestamp as an sql.Timestamp
     */
    public Timestamp getStatusTimestamp() {
        return statusTimestamp;
    }

    /**
     * Set this bike's status timestamp.
     */
    public void setStatusTimestamp(Timestamp statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    /**
     * Get this Bike's total kilometres.
     * @return the total kilometres as a double.
     */
    public double getTotalKilometers() {
        return totalKilometers;
    }

    /**
     * Set this Bike's total kilometres.
     */
    public void setTotalKilometers(double totalKilometers) {
        this.totalKilometers = totalKilometers;
    }

    /**
     * Get this Bike's position.
     * @return the position as a LatLng.
     */
    public LatLng getPosition() {
        return position;
    }

    /**
     * Set this bike's position.
     */
    public void setPosition(LatLng position) {
        this.position = position;
    }

    /**
     * Get this Bike's user Id (the user that is currently using this bike).
     * @return the user Id as an int.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set this Bike's user Id.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets this Bike's station Id (the station the Bike currently is docked at)
     * @return the station Id as an int.
     */
    public int getStationId() {
        return stationId;
    }

    /**
     * Set this Bike's station Id.
     */
    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    /**
     * Gets this Bike's total trips.
     * @return return total trips as an int.
     */
    public int getTotalTrips() {
        return totalTrips;
    }

    /**
     * Get this Bike's date that it was purchased.
     * @return the date as a sql.Date.
     */
    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    /**
     * Increments total trips. Is incremented each time this bike is rent.
     */
    public void incrementTotalTrips() {
        totalTrips++;
    }

    /**
     * Get the bike's current charge level in %
     * @return the % as a double. Min 0, Max 100.
     */
    public double getChargeLevel() {
        return chargeLevel;
    }

    /**
     * Get this Bike's date that it was purchased.
     * @return the date as a sql.Date.
     */
    public void setChargeLevel(double chargeLevel) {
        this.chargeLevel = chargeLevel;
    }

    /**
     * Clone this Bike object by creating a new object with all the same attributes.
     * @return a clone of the Bike.
     */
    public Bike clone() {
        return new Bike(Id, type, make, price, datePurchased, activeStatus, userId, stationId, totalTrips, statusTimestamp,
                        totalKilometers, position, chargeLevel);
    }

    public String toString() {
        return String.format("bike{id: `%d`, type: `%s`, make: `%s`, price: `%f`," +
                        " datePurchased: `%s`, activeStatus: `%s`, userId: `%d`, stationId: `%d`, chargeLevel: `%f`, !missing}",
                Id, type, make, price, datePurchased, activeStatus, userId, stationId, chargeLevel);
    }

    /**
     * Checks if a Bike is this bike or not. Determines on Id.
     * @return true or false depending on if the given bike is this bike.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Bike)) {
            return false;
        }
        Bike bike = (Bike) o;
        return bike.Id == this.Id; //fixed, was UserId
    }

    /**
     * Enum class containing the various bike states.
     */
    public enum ActiveStatus {
        RENTED("RENTED"), DOCKED("DOCKED"), REPAIR("REPAIR"), DELETED("DELETED");

        private final String stringEnum;

        ActiveStatus(String stringEnum) {
            this.stringEnum = stringEnum;
        }

        public static ActiveStatus from(String s) {
            for (ActiveStatus elem : ActiveStatus.values()) {
                if (elem.asString().equals(s)) {
                    return elem;
                }
            }
            return null;
        }

        public String asString() {
            return stringEnum;
        }
    }

    /**
     * Builder class for bike.
     */
    public static class Builder {
        private int id = Integer.MIN_VALUE;
        private String type = null;
        private String make = null;
        private double price = Double.NaN;
        private String datePurchased = null;

        private Bike.ActiveStatus activeStatus = null; /* XXX: rename? */
        private int userId = Integer.MIN_VALUE;
        private int stationId = Integer.MIN_VALUE;

        private Timestamp statusTimestamp = new Timestamp(System.currentTimeMillis());
        private double totalKilometers = 0;
        private LatLng position = null;

        private double chargingLevel = 33;
        private int totalTrips = 0;


        public Builder() {
        }

        public Bike build() {
            return new Bike(id, type, make, price, datePurchased, activeStatus, userId, stationId,
                    totalTrips, statusTimestamp, totalKilometers, position, chargingLevel);
        }

        public Builder from(Bike bike) {
            id = bike.getId();
            type = bike.getType();
            make = bike.getMake();
            price = bike.getPrice();
            datePurchased = bike.getDatePurchased();
            activeStatus = bike.getActiveStatus();
            userId = bike.getUserId();
            stationId = bike.getStationId();

            statusTimestamp = bike.getStatusTimestamp();
            totalKilometers = bike.getTotalKilometers();
            position = bike.getPosition();

            totalTrips = bike.getTotalTrips();
            chargingLevel = bike.getChargeLevel();
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder make(String make) {
            this.make = make;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder datePurchased(String datePurchased) {
            this.datePurchased = datePurchased;
            return this;
        }

        public Builder activeStatus(Bike.ActiveStatus activeStatus) {
            this.activeStatus = activeStatus;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder stationId(int stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder statusTimestamp(Timestamp statusTimestamp) {
            this.statusTimestamp = statusTimestamp;
            return this;
        }

        public Builder totalKilometers(double totalKilometers) {
            this.totalKilometers = totalKilometers;
            return this;
        }

        public Builder position(LatLng position) {
            this.position = position;
            return this;
        }

        public Builder chargingLevel(double chargingLevel){
            this.chargingLevel = chargingLevel;
            return this;
        }

        public Builder totalTrips(int totalTrips){
            this.totalTrips = totalTrips;
            return this;
        }
    }
}