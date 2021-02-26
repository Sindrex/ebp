package objects;

import app.map.LatLng;

/**
 * This class represents one row in the BikeStatus table. It is a snapshot in time of the bike.
 *
 * @author Sindre Haugland Paulshus
 * date 05.03.2018
 * @see objects.Bike
 */
@Deprecated
public class BikeHistory {
    private String timestamp;
    private int bikeId;
    private double chargeLevel;
    private double totalKM;
    private int totalTrips;
    private double posLong;
    private double posLat;
    private Bike.ActiveStatus activity;
    private int userId;
    private int stationId;

    // NaN values will be treated as NULL in SQL queries. - jbr
    public BikeHistory(String timestamp, int bikeId, double chargeLevel, double totalKM, int totalTrips,
                       double posLat, double posLong, Bike.ActiveStatus activity, int userId, int stationId) {
        /*
        if (Double.isNaN(chargeLevel) || Double.isNaN(totalKM) || Double.isNaN(posLat) || Double.isNaN(posLong)) {
            throw new IllegalArgumentException("No arguments can be NaN.");
        }
        */

        this.timestamp = timestamp;
        this.bikeId = bikeId;
        this.chargeLevel = chargeLevel;
        this.totalKM = totalKM;
        this.totalTrips = totalTrips;
        this.posLong = posLong;
        this.posLat = posLat;
        this.activity = activity;
        this.userId = userId;
        this.stationId = stationId;
    }


    /**
     * Gets the bikeHistory's timestamp.
     * @return the timestamp as a String on the format yyyy/mm/dd/hh/mm.
     */
    public String getTimestamp() {
        return timestamp;
    }
    /**
     * Gets the bikeHistory's bikeId..
     * @return the Id as a int.
     */
    public int getBikeId() {
        return bikeId;
    }

    /**
     * Gets the bikeHistory's charge level as a percentage.
     * @return the charge level as a double.
     */
    public double getChargeLevel() {
        return chargeLevel;
    }

    /**
     * Gets the bikeHistory's total kilometers (km)
     * @return the total kilometers as a double.
     */
    public double getTotalKM(){
        return totalKM;
    }

    /**
     * Gets the bikeHistory's total trips as a percentage.
     * @return the charge level as an integer.
     */
    public int getTotalTrips(){
        return totalTrips;
    }

    /**
     * Gets the bikeHistory's longitude position.
     * @return the longitude position as a double.
     */
    public double getPosLong() {
        return posLong;
    }

    /**
     * Gets the bikeHistory's latitude position.
     * @return the latitude position as a double.
     */
    public double getPosLat() {
        return posLat;
    }

    /**
     * Gets the bikeHistory's activity.
     * @return the the bikeHistory's activity.
     */
    public Bike.ActiveStatus getActivity(){
        return activity;
    }

    /**
     * Gets the bikeHistory's user Id.
     * @return the user Id as an int.
     */
    public int getUserId(){
        return  userId;
    }

    /**
     * Gets the bikeHistory's station Id.
     * @return the station Id as an int.
     */
    public int getStationId(){
        return stationId;
    }

    public String toString() {
        return String.format("BikeHistory{timestamp: %s, bikeId: %d chargeLevel: %f, total km: %f, total trips: %d, " +
                            "posLong: %f, posLat: %f, userId: %d, stationId: %d}",
                            timestamp, bikeId, chargeLevel, totalKM, totalTrips, posLong, posLat, userId, stationId);
    }

    @Override
    public boolean equals(Object o) {
        if(o == this){
            return true;
        }
        if(o == null){
            return false;
        }
        if(getClass() != o.getClass()){
            return  false;
        }
        BikeHistory hist = (BikeHistory) o;
        if(hist.getBikeId() == this.getBikeId() && hist.getTimestamp().equals(this.getTimestamp())){
            return  true;
        }
        return  false;
    }

    /**
     * Builder class for BikeHistory.
     * @author Joergen Bele Reinfjell
     */
    public static class Builder {
        private String timestamp = null;
        private int bikeId = -1;
        private double chargeLevel = 0;
        private double totalKM = 0;
        private int totalTrips = -1;
        private double posLong = 0;
        private double posLat = 0;
        private Bike.ActiveStatus activity = Bike.ActiveStatus.RENTED; // Default.
        private int userId = -1;
        private int stationId = -1;

        public BikeHistory build() {
            // XXX - checks?
            return new BikeHistory(timestamp, bikeId, chargeLevel, totalKM,
                    totalTrips, posLat, posLong, activity, userId, stationId);
        }

        public Builder position(LatLng position) {
            if (position != null) {
                this.posLat = position.getLat();
                this.posLong = position.getLng();
            }  else {
                this.posLat = Double.NaN;
                this.posLong = Double.NaN;
            }
            return this;
        }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder bikeId(int bikeId) { this.bikeId = bikeId; return this; }
        public Builder chargeLevel(double chargeLevel) { this.chargeLevel = chargeLevel; return this; }
        public Builder totalKM(double totalKM) { this.totalKM = totalKM; return this; }
        public Builder totalTrips(int totalTrips) { this.totalTrips = totalTrips; return this; }
        public Builder posLong(int posLong) { this.posLong = posLong; return this; }
        public Builder posLat(int posLat) { this.posLat = posLat; return this; }
        public Builder userId(int userId) { this.userId = userId; return this; }
        public Builder stationId(int stationId) { this.stationId = stationId; return this; }
        public Builder activeStatus(Bike.ActiveStatus status) { this.activity = status; return this; }

        /**
         * Sets attributes from an existing instance.
         */
        public Builder from(BikeHistory h) {
            timestamp = h.getTimestamp();
            bikeId =  h.getBikeId();
            chargeLevel = h.getChargeLevel();
            totalKM = h.getTotalKM();
            totalTrips = h.getTotalTrips();
            posLat = h.getPosLat();
            posLong = h.getPosLong();
            activity = h.getActivity();
            userId = h.getUserId();
            stationId = h.getStationId();
            return this;
        }
    }
}
