package simulation;

import app.map.GoogleMapUtils;
import app.map.LatLng;
import db.dao.BikeDao;
import db.dao.DockingStationDao;
import db.dao.RepairDao;
import logging.Logger;
import objects.Bike;
import objects.Bike.ActiveStatus;
import objects.DockingStation;
import objects.Repair;
import org.joda.time.DateTime;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class handles the simulation of a single bike on a thread.
 * @author Joergen Bele Reinfjell
 * @author Odd-Erik Frantzen
 */
public class BikeSimulation {
    private static final double BATTERY_DEPLETED_LEVEL = 1;
    private final int bikeId;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BikeDao DAO = BikeDao.getInstance();

    private List<DockingStation> dockingStations;

    private Bike bike = null;
    private Bike previous = null;
    private BikeTravelHandler bikeTravelHandler = null;

    private final double maxBatteryCapacity = 30; // Watt hours
    private final double MAX_CHARGE_LEVEL = 100.0;
    private final double MAX_CHARGE_RATE = 0.7 * MAX_CHARGE_LEVEL; // % per hour
    private final double MAX_BATTERY_DRAIN_RATE = 0.4 * MAX_CHARGE_LEVEL; // % per hour
    private final double DOCKING_STATION_PROXIMITY_DETECT_DISTANCE = 600; // meters. High because of poor placement of docking stations in test data.
    private final double LOW_BATTERY_FIND_DOCK_LEVEL = 40; // percent

    private final String[] descriptions = new String[]{
            "Deflated tire",     "Maintenance",                  "Battery replacement",
            "Disfigured wheel replacement", "Frame replacement", "No more space in this note book, will follow up."};

    private final double[] prices = new double[]{500,   400,   800,
            300,  3000, 10000};


    private final static Random random = new Random();

    // We simulate into the future starting at the start time.
    private long currentTimeMillis;

    // Updating on an interval of
    private long updatePeriodMillis;

    // Where the simulated currentTimeMillis is incremented by
    private long simulatedTimeIncrementMillis; // each update.

    // Delta seconds.
    private final double ds;

    // The DockingStation which the bike plans to end up at.
    private DockingStation endDockingStation = null;


    public BikeSimulation(int bikeId, long startTimeMillis, long updatePeriodMillis, long simulatedTimeIncrementMillis) {
        this.bikeId = bikeId;
        this.currentTimeMillis = startTimeMillis;
        this.updatePeriodMillis = updatePeriodMillis;
        this.simulatedTimeIncrementMillis = simulatedTimeIncrementMillis;
        this.ds =  simulatedTimeIncrementMillis/1000;

        bike = DAO.get(new Bike.Builder().id(bikeId).build());
        if (bike == null) {
            throw new IllegalStateException("Unable to get bike for bike: " + bikeId);
        } else {
            Logger.logf("bike: %s", bike.toString());
        }
    }

    /**
     * @return the closest docking station if it exists.
     */
    public static DockingStation getAvailableDockingStation(Bike b) {
        try {
            double closest = Double.POSITIVE_INFINITY;
            DockingStation closestDock = null;
            DockingStationDao dao = DockingStationDao.getInstance();
            List<DockingStation> dockingStations = dao.getAll();
            dockingStations.removeIf(d -> d.getStatus()==DockingStation.ActiveStatus.DELETED || dao.bikesInDock(d).size()>=d.getMAX_BIKES());
            for (DockingStation d : dockingStations) {
                double distance = b.getPosition().distance(d.getPosition());
                if (distance < closest){
                    closest = distance;
                    closestDock = d;
                }
            }
            return closestDock;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * @return the docking station if it exists.
     */
    private DockingStation getAvailableDockingStation() {
        try {
            List<DockingStation> dockingStations = DockingStationDao.getInstance().getAll();
            dockingStations.removeIf(d -> d.getStatus()==DockingStation.ActiveStatus.DELETED);
            for (DockingStation d : dockingStations) {
                if (d.getBikes().size() < d.getMAX_BIKES()) {
                    return d;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param ds the difference in time.
     * @return true when the bike has to be repaired.
     */
    private boolean randomChanceBroken(double ds) {
        for (int i = 0; i < ds*10; i++) {
            if (random.nextDouble() >= 0.999996) return true; //0.999996^(10*60*60*2) = 0.74976116% chance of not being broken per 2 hours
        }
        return false;
    }

    /**
     * @param ds the difference in time.
     * @return true when the bike has been fixed.
     */
    private boolean randomChanceFixed(double ds) {
        for (int i = 0; i < ds*10; i++) {
            if (random.nextDouble() >= 0.99995) return true; //0.99995^(10*60) = 0.97 = 97% chance of not being fixed per minute.
        }
        return false;
    }

    private boolean handleRepair() {

        Bike.Builder builder = new Bike.Builder().from(bike);
        if (previous != null && previous.getActiveStatus().equals(ActiveStatus.RENTED)){
            builder.totalTrips(previous.getTotalTrips()+1);
        }

        // In the case that the bike was fixed.
        if (randomChanceFixed(ds)) {
            Logger.logf("Bike: %d is now repaired!", bike.getId());

            // Find a available docking station to dock the bike.
            DockingStation available = getAvailableDockingStation();
            if (available == null) {
                Logger.logf("Bike: %d repaired bike delivery failed!", bike.getId());
                return false;
            }

            builder
                    .activeStatus(ActiveStatus.DOCKED)
                    .stationId(available.getDockID())
                    .userId(-1)
                    .position(available.getPosition());

            Repair current = RepairDao.getInstance().getCurrentRepair(bike.getId());
            if (current != null) {

                Repair.Builder repairBuilder = new Repair.Builder().from(current);
                repairBuilder
                        .returnDate(bike.getStatusTimestamp())
                        .returnDescription("Returned by simulation");
                for (int i = 0; i < descriptions.length; i++) {
                    if (descriptions[i].equals(current.getDescription())) {
                        repairBuilder.price(prices[i]);
                    }
                }

                if (!repairBuilder.build().returnIsValid()) {
                    repairBuilder.price(9000.1);
                }

                current = repairBuilder.build();
                try {
                    if (!RepairDao.getInstance().update(current)) {
                        Logger.errf("handleRepair: Failed to update (add dateReturned) repair for bike: %d", bike.getId());
                        return false;
                    } else {
                        Logger.logf("Bike: %d finished repair: added repair for bike", bike.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }else{
            builder.position(null);
        }

        builder.statusTimestamp(new Timestamp(currentTimeMillis));
        bike = builder.build();
        if (!DAO.update(bike)) {
            Logger.errf("handleRepair: Failed to update bike: %d", bikeId);
            return false;
        }

        return true;
    }

    /**
     * Returns true if the bike is to be rented or false if it should not.
     * @implNote Utilizes util.Random.
     * @param ds delta seconds since last call.
     * @return true if the bike is to be rented, or false if not.
     */
    private boolean randomChanceRented(double ds) {
        for (int i = 0; i < ds*10; i++) {
            if (random.nextDouble() >= 0.9997) return true; //0.9997^(10*60) = 0.83 = 83% chance of not being rented
        }
        return false;
    }

    /**
     * Handles the random event of a bike being rented while docked,
     * and the continuous change in charging level.
     * @return true on success, false on failure.
     */
    private boolean handleDocked() {
        Bike.Builder builder = new Bike.Builder().from(bike);

        int totalTrips = bike.getTotalTrips();
        // Update trips counter if the bike was just rented.
        if (previous != null && previous.getActiveStatus() == ActiveStatus.RENTED) {
            totalTrips++;
        }

        double chargeLevel = bike.getChargeLevel();
        if (Double.isNaN(chargeLevel)) {
            throw new IllegalStateException("ChargeLevel is NaN");
        }
        final double powerUsage;
        if (chargeLevel < MAX_CHARGE_LEVEL){
            powerUsage =  MAX_CHARGE_RATE / 3600;
            chargeLevel += powerUsage * ds;
        }else {
            powerUsage = 0; //to avoid going over and flipping negative due to floating point errors.
            chargeLevel = MAX_CHARGE_LEVEL;
        }
        chargeLevel += powerUsage/ds;

        //chargeLevel += MAX_CHARGE_LEVEL;

        if (chargeLevel >= 100) {
            chargeLevel = 100;
        } else if (chargeLevel < 0) {
            chargeLevel = 0;
        }

        /*
         * Bike can only be rented when chargeLevel is at or above 90% charge.
         * The probability of the bike getting rented is BIKE_RENTED_PROBABILITY.
         */
        if (chargeLevel >= 90 && randomChanceRented(ds)) {
            Logger.logf("Bike: %d is now rented!", bike.getId());
            final int userId = 1; // TODO: Remove user completely.

            builder
                    .activeStatus(ActiveStatus.RENTED)
                    .stationId(-1)
                    .userId(userId);
        }

        builder
                .chargingLevel(chargeLevel)
                .totalTrips(totalTrips)
                .statusTimestamp(new Timestamp(currentTimeMillis));

        builder.chargingLevel(chargeLevel);
        bike = builder.build();
        if (!DAO.update(bike)) {
            Logger.errf("Failed to add history element for bike: %d", bikeId);
            return false;
        }
        return true;
    }

    /**
     * Generates a new random repair for the given bike.
     * @param bike the bike the repair should be done to.
     * @return the new repair instance.
     */
    public Repair newRandomRepair(final Bike bike) {
        int rand = random.nextInt(descriptions.length);

        // Relies on builder automatically providing requestDate.
        Repair.Builder builder = new Repair.Builder()
                .using(bike)
                .description(descriptions[rand])
                .requestDate(new Date(currentTimeMillis));

        return builder.build();
    }

    private boolean handleRented() {
        Bike.Builder builder = new Bike.Builder().from(bike);

        // Check if the last trip was to a docking station and it was successful.
        // Uses a resolution of DOCKING_STATION_PROXIMITYetc

        if (bikeTravelHandler != null && bikeTravelHandler.isDone() && endDockingStation != null
                && bike.getPosition().distance(endDockingStation.getPosition()) < DOCKING_STATION_PROXIMITY_DETECT_DISTANCE) {
            // Dock at docking station.
            builder
                    .stationId(endDockingStation.getDockID())
                    .userId(-1)
                    .activeStatus(ActiveStatus.DOCKED)
                    .position(new LatLng(endDockingStation.getPosLat(), endDockingStation.getPosLong()));
            Logger.logf("Bike: %d ended its trip at dock: %d", bike.getId(), endDockingStation.getDockID());
            bike = builder.build();
            if (!DAO.update(bike)) return false;
            endDockingStation = null;
            return true;
        }

        // Create new traveler if none exists, or if the current one is done.
        if (bikeTravelHandler == null || bikeTravelHandler.isDone() || (bike.getChargeLevel() < LOW_BATTERY_FIND_DOCK_LEVEL && endDockingStation==null)) {
            LatLng pos = null;
            boolean asap = false;
            // Go to a docking station if the charge level is below the constant val
            if (bike.getChargeLevel() < LOW_BATTERY_FIND_DOCK_LEVEL) {
                DockingStation available = getAvailableDockingStation(builder.build());
                if (available != null) {
                    endDockingStation = available;
                    pos = new LatLng(available.getPosLat(), available.getPosLong());
                    asap = true;
                    Logger.logf("Bike: %d --> Going to to docking station: %d", bike.getId(), available.getDockID());
                }
            }
            // otherwise generate a new random position.
            if (pos == null) {
                pos = GoogleMapUtils.getRandomRouteDestination();
            }

            bikeTravelHandler = new BikeTravelHandler(
                    bike.getPosition(),
                    pos,
                    asap ? 0 : BikeTravelHandler.MAX_WAYPOINT_COUNT
            );
            bikeTravelHandler.startTravel(currentTimeMillis);
        }

        // Get the next position.
        bikeTravelHandler.updatePosition(currentTimeMillis);
        LatLng current = bikeTravelHandler.getPosition();
        if (current == null) {
            Logger.log("Unable to get current position!");
            return false;
        }
        builder.position(current);

        // Debug information.
        int pathIndex = bikeTravelHandler.getPathIndex();
        List<BikeTravelHandler.PositionTimeDistanceTuple> path = bikeTravelHandler.getPath();
        Logger.logf("Bike[%f]: [%d] %f (%f) => %s, next: %s [%d]",
                bike.getChargeLevel(), bikeId, bikeTravelHandler.getTravelledMeters(),
                bikeTravelHandler.getTravelledMetersSinceLast(), current, bikeTravelHandler.getNext(), pathIndex);

        // Update charge level.
        final double powerUsage = MAX_BATTERY_DRAIN_RATE / (3600); //*(MAX_CHARGE_LEVEL - bike.getChargeLevel());

        double chargeLevel = bike.getChargeLevel() - powerUsage * ds;

        boolean sendToRepair = false;

        // Random event of the bike getting broken.
        if (randomChanceBroken(ds)) {
            sendToRepair = true;
            Logger.logf("Bike: %d broke!", bike.getId());
            //  In the case that the battery is depleted while in use!
        }

        // Set builder parameters necessary for a bike being repaired.
        if (sendToRepair) {
            // Set the active status to REPAIR and set position, stationId and userId to null in the database.
            builder
                    .position(null)
                    .activeStatus(ActiveStatus.REPAIR)
                    .stationId(-1)
                    .userId(-1)
                    .chargingLevel(70.);
        }

        // Create updated history element.
        if (chargeLevel<0) builder.chargingLevel(0);
        bike = builder
                .chargingLevel(chargeLevel)
                .totalKilometers(bike.getTotalKilometers() + (bikeTravelHandler.getTravelledMetersSinceLast() / 1000))
                .statusTimestamp(new Timestamp(currentTimeMillis))
                .build();

        if (sendToRepair) {
            // Create a new random repair for the given bike.
            Repair repair = newRandomRepair(bike);

            // Update database.
            // FIXME - Transaction handling - Automatically update necessary fields in. Make add() or update() automatically set bike.
            try {
                if (!RepairDao.getInstance().add(repair)) {
                    Logger.errf("Failed to add repair to bike: %d", bike.getId());
                    return false;
                }
                Logger.logf("Added repair to bike: %d", bike.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
         * Update bike and history.
         */
        if (!DAO.update(bike)) {
            Logger.errf("Failed to update for bike: %d", bike.getId());
            return false;
        }
        /*
        bike = builder.statusTimestamp(new Timestamp(System.currentTimeMillis())).position(current).build();
        if (!DAO.update(bike)) {
            Logger.logf("Failed to add history element for bike: %d", bikeId);
        }
        */
        return true;
    }

    /* NOTE that it does not fetch updates on the bike from the db,
        only gets the history once. */
    private void loop() {
        Logger.logf("Starting simulation of bike: %d", bikeId);

        while (isRunning()) {
            Logger.logf("Updating: state: %s, totalKM: %f", bike.getActiveStatus(), bike.getTotalKilometers());
            switch (bike.getActiveStatus()) {
                case DOCKED: {
                    handleDocked();
                    break;
                }
                case RENTED: {
                    handleRented();
                    break;
                }
                case REPAIR: {
                    handleRepair();
                    break;
                }
                case DELETED: {
                    Logger.log("Not handling a deleted bike.");
                    break;
                }
            }

            currentTimeMillis += simulatedTimeIncrementMillis;
            previous = bike;
            try {
                Thread.sleep(updatePeriodMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        running.set(true);
        loop();
    }

    public void stop() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public static void main(String[] args) {
        // Start a new simulation on bike id 1 updating every second, simulating 5 minutes each second,
        // starting at the current time.
        new BikeSimulation(2, new DateTime(2018, 5, 1, 0,0,0).getMillis(),2000, 180*1000).start();
    }

}