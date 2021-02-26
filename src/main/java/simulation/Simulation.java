package simulation;

import logging.Logger;
import managers.BikeManager;
import objects.Bike;

import java.util.List;

/**
 * Main class for the simulation.
 * @author Joergen Bele Reinfjell
 * @author Odd-Erik Frantzen
 */
public class Simulation {
    public static void main(String[] args) {
        new Thread(() -> {
            Logger.logf("Starting docking stations simulation");
            new DockingStationSimulation(System.currentTimeMillis(),2000, 1000*60).start();
        }).start();

        BikeManager manager = new BikeManager();
        manager.refresh();
        List<Bike> bikes = manager.getBikes();
        bikes.removeIf(b -> !b.getActiveStatus().equals(Bike.ActiveStatus.RENTED));
        bikes.forEach(b -> {
            Logger.logf("Starting simulation for bike: %d", b.getId());
            new Thread(new BikeSimulation(b.getId(), System.currentTimeMillis(),1000, 1000)::start).start();
        });

    }
}