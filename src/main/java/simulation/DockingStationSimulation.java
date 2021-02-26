package simulation;

import db.dao.BikeDao;
import db.dao.DockingStationDao;
import logging.Logger;
import managers.DockingStationManager;
import objects.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class handles the simulation of all docking station.
 * It takes care of updating the power usage based upon the number
 * of bikes currently docked and their current battery level.
 * @author Joergen Bele Reinfjell
 * @author Odd-Erik Frantzen
 */
public class DockingStationSimulation {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final DockingStationDao DAO = DockingStationDao.getInstance();
    private final BikeDao bikeDAO = BikeDao.getInstance();

    // We simulate into the future starting at the start time.
    private long currentTimeMillis;

    // Updating on an interval of
    private long updatePeriodMillis;

    // Where the simulated currentTimeMillis is incremented by
    private long simulatedTimeIncrementMillis; // each update.

    public DockingStationSimulation(long startTimeMillis, long updatePeriodMillis, long simulatedTimeIncrementMillis) {
        this.currentTimeMillis = startTimeMillis;
        this.updatePeriodMillis = updatePeriodMillis;
        this.simulatedTimeIncrementMillis = simulatedTimeIncrementMillis;
    }

    private double calculatePowerUse(List<Bike> statuses, int maxBikes) {
        final double maxBatteryCapacity = 30; // Watt hours
        final double maxChargeRate = 0.8 * maxBatteryCapacity;      // Watts/hour
        final double maxPowerUsage = maxBikes * maxChargeRate;
        final double MAX_CHARGE_LEVEL = 100.0;

        double totalPowerUsage = 0.0;

        for (Bike status : statuses) {
            double chargeLevel = status.getChargeLevel();
            double powerUsage =  (MAX_CHARGE_LEVEL - chargeLevel) * maxChargeRate;
            totalPowerUsage += powerUsage * 1/(updatePeriodMillis/1000 * 3600);
            totalPowerUsage += maxChargeRate;
        }
        return totalPowerUsage;
    }

    private void loop() {
        while (isRunning()) {
            List<DockingStation> stations;
            try {
                //Logger.log("Updating docks!");
                stations = DAO.getAll();
                for (DockingStation s : stations) {
                    List<Integer> bikeIds = DAO.bikesInDock(s);
                    if (bikeIds == null) {
                        Logger.errf("Unable to get bikes for docking station: %s", s);
                        continue;
                    }

                    // Gets the latest bike history for each docked bike..
                    List<Bike> bikeStatuses = bikeIds.stream().map(bikeDAO::getById).filter(Objects::nonNull).collect(Collectors.toList());
                    double powerUse = calculatePowerUse(bikeStatuses, s.getMAX_BIKES());

                    DockingStation newDockingStation = new DockingStation.Builder().from(s).powerUsage(powerUse).build();

                    if (!DockingStationDao.getInstance().update(newDockingStation)) {
                        Logger.errf("Unable to update docking station: %d", newDockingStation.getDockID());
                    }
                    Logger.logf("Power usage for docking station: %d is %f distributed to %d bikes",
                            s.getDockID(), newDockingStation.getPowerUsage(), bikeStatuses.size());


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            currentTimeMillis += simulatedTimeIncrementMillis;
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
        new DockingStationSimulation(System.currentTimeMillis(),1000, 5*60*1000).start();
    }
}
