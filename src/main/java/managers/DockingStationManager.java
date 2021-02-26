package managers;

import db.dao.DockingStationDao;
import logging.Logger;
import objects.DockingStation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The DockingStationManager class is for information administration in the database, and use that information
 * to make DockingStation objects from that information, that is later used in the GUI where the information in each
 * object is represented in a textual format.
 *
 * The controller class uses the DockingStationDao class to gather information from the database,
 * and than uses the DockingStation object class to convert this information into Java objects
 * which is later interpreted and printed out in the admin application.
 *
 * @author Sindre Thomassen
 * @author Joergen Bele Reinfjell
 * 14.03.2018
 * @see DockingStation
 * @see DockingStationDao
 */
// TODO: 4/9/18 get story
public class DockingStationManager implements ManagerInterface {
    private static List<DockingStation> dockingStations = new ArrayList<>();
    private static List<DockingStation> deletedDockingStations = new ArrayList<>();
    private static final DockingStationDao DSD = DockingStationDao.getInstance();

    public DockingStationManager() {
        refresh();
    }

    /**
     * Updates the cached list of docking stations from the database.
     *
     * @return true on success, false on failure.
     */
    public boolean refresh() {
        try {
            List<DockingStation> help = DSD.getAll();
            dockingStations = new ArrayList<>();
            deletedDockingStations = new ArrayList<>();
            for (DockingStation dock : help) {
                if (dock.getStatus() == DockingStation.ActiveStatus.ACTIVE) {
                    addBikesToDock(dock);
                    dockingStations.add(dock);
                } else if (dock.getStatus() == DockingStation.ActiveStatus.DELETED) {
                    deletedDockingStations.add(dock);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the database and list of docking stations to contain the correct
     * information regarding the docking stations power usage, and bike ids of the bikes currently docked.
     *
     * @return true if everything worked fine, false if something went wrong.
     */
    /*
    public boolean update() {
        Logger.log("DockingStationManager: updating!");
        try{
            for(DockingStation dock : dockingStations) {
                addBikesToDock(dock);
                DSD.update(dock);
            }
            for(DockingStation dock : deletedDockingStations) {
                DSD.update(dock);
            }

            Logger.log("DockingStationManager: update done!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/

    /**
     * Assuming the docking station in question is active, changes the status from active to deleted. Proceed from there
     * to delete the station from the station list, and add it to the deleted list before updating the active status
     * in the database. If the docking station in question does not exist or is already deleted, throws exception.
     * @param id
     * @return
     */
    public boolean deleteDock(int id) {
        Logger.log("DockingStationManager: deleting dock!");

        try {
            for (DockingStation dock : dockingStations) {
                if (dock.getDockID() == id) {
                    dock.changeStatus(DockingStation.ActiveStatus.DELETED);
                    dock.setBikes(new ArrayList<>());
                    deletedDockingStations.add(dock);
                    dockingStations.remove(dock);
                    DSD.update(dock);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a new docking station to the database, before making an docking station object and adds it to the
     * object list assuming the dock in question does not already exist, in which case a exception is thrown.
     *
     * @param newDock
     * @return true if everything went fine, false if something went wrong.
     */
    public boolean addDock(DockingStation newDock) {
        Logger.log("DockingStationManager: adding dock!");

        try {
            for (DockingStation dock : dockingStations) {
                if (dock.getDockID() == newDock.getDockID()) {
                    return false;
                }
            }
            for (DockingStation dock : deletedDockingStations) {
                if (dock.getDockID() == newDock.getDockID()) {
                    return false;
                }
            }
            if (DSD.add(newDock)) {
                if (newDock.getStatus() == DockingStation.ActiveStatus.ACTIVE) {
                    dockingStations.add(newDock);
                } else {
                    deletedDockingStations.add(newDock);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Assuming the docking station in question exists and is in fact deleted, changes the status to active before
     * removing it from the deleted list and adds it to the station list. Afterwards the docking station status
     * is updated in the database. If the dock does not exist or is not deleted, a exception is thrown.
     *
     * @param id
     * @return true if everything went fine, false if something went wrong.
     */
    public boolean restoreDock(int id) {
        Logger.logf("DockingStationManager: restoring dock: %d", id);
        try {
            for (DockingStation dock : deletedDockingStations) {
                if (dock.getDockID() == id) {
                    dock.changeStatus(DockingStation.ActiveStatus.ACTIVE);
                    dockingStations.add(dock);
                    deletedDockingStations.remove(dock);
                    DSD.update(dock);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the dock identified by the given id.
     *
     * @param id
     * @return a docking station with the corresponding id, assuming this exists. If the id in question does not exist,
     * nothing is returned.
     */
    public DockingStation getDock(int id) {
        for (DockingStation dock : dockingStations) {
            if (dock.getDockID() == id) {
                new DockingStation.Builder().from(dock);
            }
        }
        return null;
    }

    /**
     * @return an unmodifiable list of all docks not marked as deleted.
     */
    public List<DockingStation> getDocks() {
        return Collections.unmodifiableList(dockingStations);
        /*
        List<DockingStation> newList = new ArrayList<>();
        for(DockingStation dock : dockingStations) {
            newList.add(new DockingStation(
                    dock.getDockID(), dock.getMAX_BIKES(), dock.getPosLong(), dock.getPosLat(), dock.getStatus(), dock.getBikes()
            ));
        }
        return newList;
        */
    }

    /**
     * Returns a list of all deleted docks.
     *
     * @return returns a list of currently deleted docking station.
     */
    public List<DockingStation> getDeletedDocks() {
        List<DockingStation> newList = new ArrayList<>();
        for (DockingStation dock : deletedDockingStations) {
            newList.add(new DockingStation.Builder().from(dock).build());
        }
        return newList;
    }

    /**
     * Finds all bikes currently docked in every active docking station by looking through docked bikes in the database,
     * and adds a list of bike ids to every docking station currently inhabiting bikes.
     *
     * @param dock the DockingStation instance.
     * @return true if everything went fine, false if something went wrong.
     */
    public boolean addBikesToDock(DockingStation dock) {
        try {
            dock.setBikes(DSD.bikesInDock(dock));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public double totalPowerUsage(int id, int year, int month, int limit) {
        List<DockingStation> dockHistories = DSD.getDockingStatusHistory(id, limit);
        double powerUsage = 0;
        if (dockHistories == null || dockHistories.size() <= 0) {
            return 0;
        }
        for (DockingStation b : dockHistories) {
            String[] split = b.getStatusTimestamp().toString().split("-");
            if (month == Integer.parseInt(split[1]) && year == Integer.parseInt(split[0])) {
                powerUsage += b.getPowerUsage();
            }
        }
        return powerUsage;
    }

    public double averagePowerUsage(int id, int year, int month, int limit) {
        List<DockingStation> dockHistories = DSD.getDockingStatusHistory(id, limit);
        int count = 0;
        double powerUsage = 0;
        if (dockHistories == null || dockHistories.size() <= 0) {
            return 0;
        }
        for (DockingStation b : dockHistories) {
            String[] split = b.getStatusTimestamp().toString().split("-");
            if (month == Integer.parseInt(split[1]) && year == Integer.parseInt(split[0])) {
                count++;
                powerUsage += b.getPowerUsage();
            }
        }
        if (count > 0) {
            return powerUsage / count;
        }
        return 0;
    }
}