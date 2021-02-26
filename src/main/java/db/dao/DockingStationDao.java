package db.dao;

import app.map.LatLng;
import db.Database;
import db.DatabaseConnection;
import objects.DockingStation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This singleton class handles the interaction with the database concerning
 * the `docking_station` table.
 * <br>
 *      It reads the response from the database server and converts the results
 * to instances of the DockingStation class.
 *
 * For now it only contains methods from the DataAccessInterface interface, but
 * it is planned to implement all other needed functionality regarding the
 * `docking_station` table.
 *
 * @author Sindre Thomassen
 * date 08.03.2018
 * @see DockingStation
 * @see DataAccessInterface
 */
public class DockingStationDao implements DataAccessInterface<DockingStation> {
    private static final DockingStationDao INSTANCE = new DockingStationDao();


    private DockingStationDao() {}

    /**
     *
     * @return instance of dockingstationdao
     */
    public static DockingStationDao getInstance() { return INSTANCE; }

    /**
     *
     * @param res
     * @return dockingstation built with data from the resultset.
     */
    private static DockingStation dockingstationFromRes(ResultSet res) {
        try {
            int id = res.getInt("station_id");
            LatLng position = new LatLng(res.getDouble("position_lat"), res.getDouble("position_long"));
            int maxBikes = res.getInt("max_bikes");
            double powerUsage = res.getDouble("power_usage");
            Timestamp statusTimestamp = res.getTimestamp("status_timestamp");
            DockingStation.ActiveStatus active = DockingStation.ActiveStatus.from(res.getInt("active"));
            DockingStation myDock =  new DockingStation(id, maxBikes, position, powerUsage, active, statusTimestamp);
            return myDock;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @implNote        ordered by timestamp, newest first.
     * @param stationId the id of the station as an int.
     * @param limit     the total number of docking station status entries to retrieve.
     * @return          a newest-first sorted List of DockingStation Objects representing history entries.
     */
    public List<DockingStation> getDockingStatusHistory(int stationId, int limit){
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                    "SELECT s.station_id, active, max_bikes, position_lat, position_long, s.status_timestamp, s.power_usage\n" +
                         "FROM docking_station JOIN docking_status as s\n" +
                         "  ON docking_station.station_id = s.station_id\n" +
                             "WHERE s.station_id = ?\n" +
                            "ORDER BY s.status_timestamp DESC\n" +
                            "LIMIT ?"
                );
        ) {
            try{
                statement.setInt(1, stationId);
                statement.setInt(2, limit);
                ResultSet res = statement.executeQuery();
                List<DockingStation> stationList = new ArrayList<>();
                while (res.next()) {
                    stationList.add(dockingstationFromRes(res));
                }
                res.close();
                return stationList;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Compares the id of a reference dockingstation with all dockingstations in docks until a match is found or
     * it reaches the end of docks and returns null.
     * @param reference the reference object with the fields used for unique
     *                 identification.
     * @return a dock with the same ID, or null if no such dockingstation exists in docks
     */
    @Override
    public DockingStation get(DockingStation reference) throws Exception {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM docking_station WHERE station_id = ?");
                ) {
            statement.setInt(1, reference.getDockID());
            try (ResultSet res = statement.executeQuery()) {
                res.first();
                DockingStation dock = dockingstationFromRes(res);

                boolean ok = true;
                if(res.next()) {
                    ok = false;
                }
                res.close();
                if(ok){
                    return dock;
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param element the element to add.
     * @return true if addition was successful, and false if it failed.
     * @throws Exception
     */
    @Override
    public boolean add(DockingStation element) throws Exception {
        try (
            DatabaseConnection con = Database.getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO docking_station" +
                    " (position_long, position_lat, max_bikes, active) " +
                    "VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        ) {
            try {
                con.autocommit(false);
                statement.setDouble(1, element.getPosLong());
                statement.setDouble(2, element.getPosLat());
                statement.setInt(3, element.getMAX_BIKES());
                statement.setInt(4, element.getStatus().asInt());
                statement.executeUpdate();
                ResultSet res = statement.getGeneratedKeys();
                res.first();
                int newId = res.getInt(1);
                con.commit();
                con.autocommit(true);
                element.setId(newId);
                return true;
            } catch (Exception e) {
                con.rollback();
                con.autocommit(true);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     *
     * Updates dockingstation in the database with the same id as the reference object sent in as parameter to the function.
     * @param element the element to update.
     * @return true if the update goes well, and false if something goes wrong.
     * @throws Exception
     */
    @Override
    public boolean update(DockingStation element)  {
        // TODO: Need to implement list of bike ids update as to know which bikes is where.
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("UPDATE docking_station SET power_usage = ?, active = ?, status_timestamp=? WHERE station_id = ?");
        ) {
            try {
                statement.setDouble(1, element.getPowerUsage());
                statement.setInt(2, element.getStatus().asInt());
                statement.setTimestamp(3, Timestamp.from(Instant.now()));
                statement.setInt(4, element.getDockID());
                statement.executeUpdate();
                return true;
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param element the element to delete.
     * @return true if deletion was successful and false if it was not.
     * @throws Exception
     */
    @Override
    public boolean delete(DockingStation element) throws Exception {
        throw new IllegalStateException("delete() method is not implemented yet!");
        //return false;
        /* Should set flag, not delete
        try (
           DatabaseConnection con = Database.getConnection();
           PreparedStatement statement = con.prepareStatement("DELETE FROM docking_station WHERE station_id = ?");
        ) {
            try {
                con.autocommit(false);
                statement.setInt(1, element.getDockID());
                statement.executeUpdate();
                con.commit();
                con.autocommit(true);
                return true;
            } catch (Exception e) {
                con.rollback();
                con.autocommit(true);
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        */
    }

    /**
     *
     * @return a list off all dockingstations in the database.
     * @throws Exception
     */
    @Override
    public List<DockingStation> getAll() throws Exception {
        List<DockingStation> dockList = new ArrayList<>();
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM docking_station");
                ResultSet res = statement.executeQuery();
        ) {
            while (res.next()) {
                DockingStation dock = dockingstationFromRes(res);
                if (dock != null) {
                    dockList.add(dock);
                }
            }
            res.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return dockList;
    }

    // Unnecessary?
    @Override
    public boolean addAll(List<DockingStation> elements) throws Exception {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO docking_station VALUES (?, ?, ?, ?, ?)");
        ) {
            try {
                con.autocommit(false);
                for (DockingStation element : elements) {
                    statement.setInt(1, element.getDockID());
                    statement.setDouble(2, element.getPosLong());
                    statement.setDouble(3, element.getPosLat());
                    statement.setInt(4, element.getMAX_BIKES());
                    statement.setInt(5, element.getStatus().asInt());
                    statement.executeUpdate();
                }
                con.commit();
                con.autocommit(true);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                con.rollback();
                con.autocommit(true);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Unnecessary?
    @Override
    public boolean updateAll(List<DockingStation> elements) throws Exception {
        return false;
    }

    // Unnecessary?
    @Override
    public boolean deleteAll(List<DockingStation> elements) throws Exception {
        return false;
    }

    /**
     * @param dock the dock.
     * @return a list of all bikes currently docked at the dock.
     */
    public List<Integer> bikesInDock(DockingStation dock) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT bike_id FROM bike WHERE station_id = ?");
        ) {
            try {
                List<Integer> ids = new ArrayList<>();
                statement.setInt(1, dock.getDockID());
                ResultSet res = statement.executeQuery();
                while(res.next()) {
                    ids.add(res.getInt("bike_id"));
                }
                res.close();
                return ids;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
