package managers;

import db.dao.DockingStationDao;
import objects.BikeType;
import db.dao.BikeDao;
import db.dao.TypeDao;
import logging.Logger;
import objects.Bike;
import objects.DockingStation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Object that links Bikes, BikeDao and the BikesController (gui).
 *
 * @author Sindre Haugland Paulshus
 * date 12.03.2018
 * @see db.dao.BikeDao;
 */
public class BikeManager implements ManagerInterface {
    private List<Bike> bikes = Collections.emptyList();
    private List<Bike> deletedBikes = Collections.emptyList();

    private BikeDao dao;
    private TypeDao typeDao;

    private final int TIMEOUT_TRIES = 2;

    enum SearchMode {
        SEARCH_BIKES_LIST, SEARCH_DELETED_BIKES_LIST, SEARCH_ALL_BIKES_LIST
    }

    public BikeManager() {
        dao = BikeDao.getInstance();
        typeDao = TypeDao.getInstance();

        /* XXX you might not want to get the
         * entire list on creation++. */
        update();
    }

    public BikeManager(List<Bike> newBikes) {
        dao = BikeDao.getInstance();
        update();
        for (Bike b : newBikes) {
            addBike(b.clone());
        }
    }

    /**
     * Method to refresh the bike-lists.
     */
    @Override
    public boolean refresh() {
        // TODO: see update().
        update();
        return true;
    }

    /**
     * Method that gets all bikes from DB and puts in either a list of bikes
     * or list of deleted bikes.
     */
    private void update() {
        //Logger.log("BikeManager: updating!");
        List<Bike> temp = dao.getAll();
        bikes = new ArrayList<>();
        deletedBikes = new ArrayList<>();
        if (temp.size() <= 0) {
            return;
        }

        for (Bike myBike : temp) {
            if (myBike.getActiveStatus() == Bike.ActiveStatus.DELETED) {
                deletedBikes.add(myBike);
            } else {
                bikes.add(myBike);
            }
        }
    }

    /**
     * Delete a bike with a given bikeID.
     * @param bikeID the ID of the bike.
     * @return true or false depending on success.
     */
    public boolean deleteBike(int bikeID) throws Exception {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, bikeID);
        if (myBike != null) {
            Bike clone = myBike.clone();
            myBike.setUserId(0);
            myBike.setStationId(0);
            if (setBikeActiveStatus(bikeID, Bike.ActiveStatus.DELETED)) {
                bikes.remove(myBike);
                deletedBikes.add(myBike);
                return true;
            } else {
                myBike.setUserId(clone.getUserId());
                myBike.setStationId(clone.getStationId());
                return false;
            }
        }
        return false;
    }

    /**
     * Add a bike given the object.
     * @param newBike the bike-object.
     * @return true or false depending on success.
     */
    public boolean addBike(Bike newBike) {
        update();
        if(typeDao.has(new BikeType(newBike.getType()))){
            try{
                newBike.setPosition(DockingStationDao.getInstance().get(new DockingStation(newBike.getStationId())).getPosition());
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

            if(dao.add(newBike)){
                if(newBike.getActiveStatus() == Bike.ActiveStatus.DELETED){
                    deletedBikes.add(newBike);
                } else {
                    bikes.add(newBike);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Restore a bike with a given bikeID.
     * @param bikeID    the bike's id.
     * @param stationId the station's, where the restored bike will be placed, id.
     * @return true or false depending on success.
     */
    public boolean restoreBike(int bikeID, int stationId) throws Exception {
        Bike myBike = findBike(SearchMode.SEARCH_DELETED_BIKES_LIST, bikeID);
        if (myBike != null) {
            Bike clone = myBike.clone();
            myBike.setStationId(stationId);
            myBike.setPosition(DockingStationDao.getInstance().get(new DockingStation(1)).getPosition());
            if (setBikeActiveStatus(bikeID, Bike.ActiveStatus.DOCKED)) {
                bikes.remove(myBike);
                deletedBikes.add(myBike);
                return true;
            } else {
                myBike.setUserId(clone.getUserId());
                myBike.setStationId(clone.getStationId());
                Logger.log("BikeManager: restoreBike(): Could not change activeStatus");
            }
            return false;
        }
        else {
            Logger.log("BikeManager: restoreBike(): Bike does not exist");
        }
        return false;
    }

    /**
     * Private method that finds out if a given bike is in bikes-list, deletedBikes-list or in both.
     * @param mode enum of SearchMode.
     * @param bikeID the id of the bike you are searching for.
     * @return the bike as a Bike Object if it exists, null if not.
     */
    public Bike findBike(SearchMode mode, int bikeID) {
        switch (mode) {
            case SEARCH_BIKES_LIST:
                for (int i = 0; i < bikes.size(); i++) {
                    if (bikes.get(i).getId() == bikeID) {
                        return bikes.get(i);
                    }
                }
                break;
            case SEARCH_DELETED_BIKES_LIST:
                for (int i = 0; i < deletedBikes.size(); i++) {
                    if (deletedBikes.get(i).getId() == bikeID) {
                        return deletedBikes.get(i);
                    }
                }
                break;
            case SEARCH_ALL_BIKES_LIST:
                for (int i = 0; i < bikes.size(); i++) {
                    if (bikes.get(i).getId() == bikeID) {
                        return bikes.get(i);
                    }
                }
                for (int i = 0; i < deletedBikes.size(); i++) {
                    if (deletedBikes.get(i).getId() == bikeID) {
                        return deletedBikes.get(i);
                    }
                }
                break;
        }
        return null;
    }

    /**
     * Private method to change a Bike's Active Status given the bikeID and the new status. Also updates DB.
     * @param bikeID the id of the bike you would like to change.
     * @param newStatus the new status.
     * @return true or false depending on success.
     */
    private boolean setBikeActiveStatus(int bikeID, Bike.ActiveStatus newStatus) throws Exception {
        Bike myBike = findBike(SearchMode.SEARCH_ALL_BIKES_LIST, bikeID);
        if (myBike != null) {
            Bike.ActiveStatus prev = myBike.getActiveStatus();
            myBike.setActiveStatus(newStatus);
            if (updateBike(myBike)) {
                return true;
            } else {
                myBike.setActiveStatus(prev);
                throw new TimeoutException("Timed out. Database could not update.");
            }
        }
        else{
            Logger.log("BikeManager: setBikeActiveStatus(): Bike does not exist");
        }
        return false;
    }

    /**
     * Sets a given bike to repair-status.
     * @param bikeID the id of the bike you would like to repair.
     * @return true or false depending on success.
     */
    public boolean repairBike(int bikeID) throws Exception {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, bikeID);
        if (myBike != null) {
            Bike clone = myBike.clone();
            myBike.setStationId(0);
            myBike.setUserId(0);
            myBike.setPosition(null);
            if (setBikeActiveStatus(bikeID, Bike.ActiveStatus.REPAIR)) {
                return true;
            } else {
                myBike.setUserId(clone.getUserId());
                myBike.setStationId(clone.getStationId());
            }
            return false;
        }
        return false;
    }

    /**
     * Sets a given bike to docked-status.
     * @param bikeID    the bike's id.
     * @param stationId the station's id you would like to dock at.
     * @return true or false depending on success.
     */
    public boolean dockBike(int bikeID, int stationId) throws Exception {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, bikeID);
        if (myBike != null) {
            Bike clone = myBike.clone();
            myBike.setStationId(stationId);
            myBike.setUserId(0);
            myBike.setPosition(DockingStationDao.getInstance().get(new DockingStation(1)).getPosition());
            if (setBikeActiveStatus(bikeID, Bike.ActiveStatus.DOCKED)) {
                return true;
            } else {
                myBike.setUserId(clone.getUserId());
                myBike.setStationId(clone.getStationId());
            }
            return false;
        }
        return false;
    }

    /**
     * Sets a given bike to rent-status.
     * @param bikeID the bike's id.
     * @param userId the renter-user's id.
     * @return true or false depending on success.
     */
    public boolean rentBike(int bikeID, int userId) throws Exception {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, bikeID);
        if (myBike != null) {
            Bike clone = myBike.clone();
            myBike.setStationId(0);
            myBike.setUserId(userId);
            myBike.incrementTotalTrips();
            if (setBikeActiveStatus(bikeID, Bike.ActiveStatus.RENTED)) {
                return true;
            } else {
                myBike.setUserId(clone.getUserId());
                myBike.setStationId(clone.getStationId());
            }
            return false;
        }
        return false;
    }

    /**
     * Private method for updating a Bike-object in the DB.
     * @param myBike the bike-object.
     * @return true or false depending on success.
     */
    private boolean updateBike(Bike myBike) {
        int times = TIMEOUT_TRIES;
        while (!dao.update(myBike) && times > 0) {
            times--;
        }
        if (times > 0) {
            return true;
        }
        return false;
    }

    /**
     * Private method for updating a Bike-object in the DB given the bike's id.
     * @param id the id of the Bike you would like to update.
     * @return true or false depending on success.
     */
    public boolean updateBike(int id) {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, id);
        if (myBike != null) {
            if (updateBike(myBike)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Private method that calculates the distance between two gps-coordinates (Lat, Long). Uncertainty: +- 3m.
     * @param recentLat  the last position latitude.
     * @param recentLong the last position longitude.
     * @param newLat     the new position latitude.
     * @param newLong    the new position longitude.
     * @return the distance in km.
     */
    private double calculateKM(double recentLat, double recentLong, double newLat, double newLong) {
        double lat1 = recentLat;
        double lat2 = newLat;
        double lon1 = recentLong;
        double lon2 = newLong;

        // Common values
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to Kilometers

        return distance;
    }

    /**
     * Set a Bike's station ID given the bikeID.
     * @param bikeID    the bike's id.
     * @param stationId the station's id.
     * @return true or false depending on success.
     */
    public boolean setStationId(int bikeID, int stationId) {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, bikeID);
        if (myBike != null) {
            myBike.setStationId(stationId);
            myBike.setUserId(0);
            return true;
        }
        return false;
    }

    /**
     * Set a Bike's user ID given the bikeID.
     * @param bikeID the bike's id.
     * @param userId the user's id.
     * @return true or false depending on success.
     */
    public boolean setUserId(int bikeID, int userId) {
        Bike myBike = findBike(SearchMode.SEARCH_BIKES_LIST, bikeID);
        if (myBike != null) {
            myBike.setUserId(userId);
            myBike.setStationId(0);
            return true;
        }
        return false;
    }

    /**
     * Getter for bikes-list.
     * @return a copy of the list of Bike-objects
     */
    public List<Bike> getBikes() {
        //Composition: Will return a copy.
        List<Bike> temp = (List) new ArrayList<Bike>();
        for (Bike b : bikes) {
            temp.add(b.clone());
        }
        return temp;
    }

    /**
     * Getter for bikes-list given a type name
     * @param type the type name as a String
     * @return a copy of the list of Bike-objects with the given type
     */
    public List<Bike> getBikes(String type){
        List<Bike> temp = new ArrayList<>();
        for(int i = 0; i < bikes.size(); i++){
            if(type.equals("All")){
                temp.add(bikes.get(i));
            }
            else if(bikes.get(i).getType().equals(type)){
                temp.add(bikes.get(i));
            }
        }
        return temp;
    }

    /**
     * Getter for deleted bikes-list.
     * @return a copy of the list of deleted Bike-objects
     */
    public List<Bike> getDeletedBikes() {
        //Composition: Will return a copy.
        List<Bike> temp = (List) new ArrayList<Bike>();
        for (Bike b : deletedBikes) {
            temp.add(b.clone());
        }
        return temp;
    }

    /**
     * Gets a Bike in the bikes-list given the bikeID.
     * @param bikeID the bike's id.
     * @return a copy of the corresponding Bike.
     */
    public Bike getBike(int bikeID) {
        for (int i = 0; i < bikes.size(); i++) {
            Bike myBike = bikes.get(i);
            if (myBike.getId() == bikeID) {
                return myBike.clone();
            }
        }
        return null;
    }

    /**
     * Gets the BikeHistory objects for a given bikeID.
     * @param bikeID the bike's id.
     * @return a list of BikeHistory objects.
     */
    public List<Bike> getBikeHistories(int bikeID, int count) {
        Bike myBike = findBike(SearchMode.SEARCH_ALL_BIKES_LIST, bikeID);
        if (myBike != null) {
            return dao.getBikeStatusHistory(bikeID, count);
        }
        return null;
    }

    /**
     * Gets the latest BikeStatus of a bike given it's id.
     * @param bikeId the bike's id.
     * @return a Bike Object of the Bike's latest status.
     */
    public Bike getLatestBikeStatus(int bikeId) {
        Bike b = findBike(SearchMode.SEARCH_ALL_BIKES_LIST, bikeId);
        if (b != null) {
            return dao.getById(b.getId());
        }
        return null;
    }

    /**
     * Gets the maxima of BikeIDs. Add one when making a new Bike.
     * @return the maximum BikeID as an int.
     */
    public int getMaxId() {
        int maxID = 1;
        for (int i = 0; i < bikes.size(); i++) {
            if (bikes.get(i).getId() > maxID) {
                maxID = bikes.get(i).getId();
            }
        }
        for (int i = 0; i < deletedBikes.size(); i++) {

            if (deletedBikes.get(i).getId() > maxID) {
                maxID = deletedBikes.get(i).getId();
            }
        }
        return maxID;
    }

    /**
     * A method to be used with StatisticsController. Get's the total number of trips for a given bike,
     * limited by year, month and a limit on how many statuses to check.
     * @see app.statistics.StatisticsController
     * @param id the bike's id.
     * @param year the given year
     * @param month the given month
     * @param limit the limit to how many statuses the algorithm will check. The higher the better, but also slower.
     * @return the number of trips as an int.
     */
    public int trips(int id, int year, int month, int limit){
        //Logger.log("BikeManager: trips() called!");
        List<Bike> bikeHistories = BikeDao.getInstance().getBikeStatusHistory(id, limit);
        //Logger.log("BikeManager: bikeHistories[0]: " + bikeHistories.get(0).getTotalTrips());
        //Logger.log("BikeManager: bikeHistories[size -1]: " + bikeHistories.get(bikeHistories.size() - 1).getTotalTrips());
        List<Bike> thisMonth = new ArrayList<>();
        if(bikeHistories == null || bikeHistories.size() <= 0){
            return 0;
        }
        for(Bike b : bikeHistories){
            String[] split = b.getStatusTimestamp().toString().split("-");
            if(month == Integer.parseInt(split[1]) && year == Integer.parseInt(split[0])){
                thisMonth.add(b);
                //Logger.log("BikeManager: Adding a month");
            }
        }
        if(thisMonth.size() > 0){
            return thisMonth.get(0).getTotalTrips() - thisMonth.get(thisMonth.size() - 1).getTotalTrips();
        }
        return 0;
    }

    /**
     * A method to be used with StatisticsController. Get's the number of kilometres per trip for a given bike,
     * limited by year, month and a limit on how many statuses to check.
     * @see app.statistics.StatisticsController
     * @param id the bike's id.
     * @param year the given year
     * @param month the given month
     * @param limit the limit to how many statuses the algorithm will check. The higher the better, but also slower.
     * @return the number of trips as an int.
     */
    public double kmPerTrip(int id, int year, int month, int limit){
        //Logger.log("BikeManager: kmPerTrips() called!");
        //int trips = trips(id, year, month, limit);
        List<Bike> bikeHistories = BikeDao.getInstance().getBikeStatusHistory(id, limit);
        if(bikeHistories == null || bikeHistories.size() <= 0){
            return 0;
        }
        //Logger.log("BikeManager: trips: " + trips + ", bikeHistories.size(): " + trips);

        List<Bike> thisMonth = new ArrayList<>();
        for(Bike b : bikeHistories){
            String[] split = b.getStatusTimestamp().toString().split("-");
            if(month == Integer.parseInt(split[1]) && year == Integer.parseInt(split[0])){
                thisMonth.add(b);
                //Logger.log("BikeManager: Adding a month");
            }
        }
        if(thisMonth.size() > 0){
            double deltaKM = thisMonth.get(0).getTotalKilometers() - thisMonth.get(bikeHistories.size() - 1).getTotalKilometers();
            return Math.round((deltaKM*100)/100);
        }
       //Logger.log("BikeManager: get(0): " + bikeHistories.get(0).getTotalKilometers() + ", bikeHistories[size -1]: " + bikeHistories.get(bikeHistories.size() - 1).getTotalKilometers());
        //Logger.log("DeltaKM: " + deltaKM + ", trips: " + trips);
        //return Math.round((deltaKM / trips)*100)/100;
        return 0;
    }

    @Override
    public String toString() {
        String res = "** BikeController **\n*Bikes:*\n + Count: " + bikes.size() + "\n";
        for (int i = 0; i < bikes.size(); i++) {
            res += bikes.get(i).toString() + "\n";
        }
        res += "*DeletedBikes:*\n + Count: " + deletedBikes.size() + "\n";
        for (int i = 0; i < deletedBikes.size(); i++) {
            res += deletedBikes.get(i).toString() + "\n";
        }
        return res;
    }
}