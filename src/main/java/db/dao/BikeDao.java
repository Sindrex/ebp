package db.dao;

import app.map.LatLng;
import db.Database;
import db.DatabaseConnection;
import objects.Bike;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles SQL and communicates with the database.
 *
 * @author Joergen Bele Reinfjell
 * @author Sindre Haugland Paulshus
 * date 05.03.2018
 * @see objects.Bike;
 */
public class BikeDao implements DataAccessInterface<Bike> {
    private static final BikeDao INSTANCE = new BikeDao();

    private BikeDao() { }

    public static BikeDao getInstance() {
        return INSTANCE;
    }

    /**
     * Private method to make a bike from resultset using the BikeBuilder class.
     * @param res the bike's resultSet.
     * @return the Bike.
     */
    private static Bike bikeFromRes(ResultSet res) {
        try {
            Bike.Builder bikeBuilder = new Bike.Builder()
                    .id(res.getInt("bike_id"))
                    .type(res.getString("type"))
                    .make(res.getString("make"))
                    .price(res.getDouble("price"))
                    .datePurchased(res.getString("date_purchased"))
                    .activeStatus(Bike.ActiveStatus.from(res.getString("active_status")))
                    .userId(res.getInt("user_id"))
                    .stationId(res.getInt("station_id"))
                    .statusTimestamp(res.getTimestamp("status_timestamp"))
                    .totalKilometers(res.getDouble("total_km"))
                    .position(new LatLng(res.getDouble("position_lat"), res.getDouble("position_long")))
                    .totalTrips(res.getInt("total_trips"))
                    .chargingLevel(res.getDouble("charging_level"));

            return bikeBuilder.build();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Private method builder for Bike-status-objects given a ResultSet.
     * @param res the bike's resultSet.
     * @return the Bike.
     */
    private static Bike bikeStatusFromRes(ResultSet res) {
        try {
            Bike.Builder bikeBuilder = new Bike.Builder()
                    .id(res.getInt("bike_id"))
                    .activeStatus(Bike.ActiveStatus.from(res.getString("active_status")))
                    .userId(res.getInt("user_id"))
                    .stationId(res.getInt("station_id"))
                    .statusTimestamp(res.getTimestamp("status_timestamp"))
                    .totalKilometers(res.getDouble("total_km"))
                    .position(new LatLng(res.getDouble("position_lat"), res.getDouble("position_long")))
                    .totalTrips(res.getInt("total_trips"))
                    .chargingLevel(res.getDouble("charging_level"));

            return bikeBuilder.build();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a bike from the DB given the Bike object.
     * @param reference the bike-object
     * @return the given Bike (from the DB).
     */
    @Override
    public Bike get(Bike reference) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM bike where bike_id = ?")
        ) {
            statement.setInt(1, reference.getId());
            try (ResultSet res = statement.executeQuery()) {
                res.first();
                Bike myBike = bikeFromRes(res);
                res.close();
                return myBike;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a bike from the database by id.
     * @param id the bike id.
     * @return the bike with the corresponding id.
     */
    public Bike getById(int id) {
        return get(new Bike.Builder().id(id).build());
    }

    /**
     * Add a bike to the DB given the Bike object.
     * @param element the bike-object
     * @return true or false depending on success.
     */
    @Override
    public boolean add(Bike element) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO bike (type, make, price, date_purchased, active_status, user_id, station_id," +
                                "status_timestamp, charging_level, total_trips, total_km, position_lat, position_long" +
                                ") VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?," +
                                "?, ?, ?, ?, ?, ?)");
        ) {
            try {
                statement.setString(1, element.getType());
                statement.setString(2, element.getMake());
                statement.setDouble(3, element.getPrice());
                statement.setString(4, element.getDatePurchased());
                statement.setString(5, element.getActiveStatus().asString());

                if (element.getUserId() > 1){
                    statement.setInt(6, element.getUserId());
                }
                else {
                    statement.setNull(6, Types.INTEGER);
                }

                statement.setInt(7, element.getStationId());
                statement.setTimestamp(8, element.getStatusTimestamp());
                statement.setDouble(9, 100);
                statement.setInt(10, 0);
                statement.setInt(11, 0);
                if (element.getPosition() == null) {
                    statement.setNull(12, Types.DOUBLE);
                    statement.setNull(13, Types.DOUBLE);
                } else {
                    statement.setDouble(12, element.getPosition().getLat());
                    statement.setDouble(13, element.getPosition().getLng());
                }

                statement.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update a bike in the DB given the Bike object.
     * @param element the bike-object
     * @return true or false depending on success.
     */
    @Override
    public boolean update(Bike element) {
        String query =
                "UPDATE bike SET" + //(bike_id, type, make, price, date_purchased, active, station_id," +
                        "\n" +
                        "type = ?, make = ?, price = ?, date_purchased = ?,\n" +
                        "active_status = ?, user_id = ?, station_id = ?,\n" +
                        "status_timestamp = ?, charging_level = ?, total_km = ?, total_trips = ?,\n" +
                        "position_long = ?, position_lat = ?\n" +
                "WHERE bike_id = ?";
        //String test = "UPDATE bike SET user_id = ?, station_id = ?, active = ? where bike_id = ?";
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement s = con.prepareStatement(query);
        ) {
            try {
                //If you want to change active status you have to change both user_id and station_id at the same time!
                s.setString(1, element.getType());
                s.setString(2, element.getMake());
                s.setDouble(3, element.getPrice());
                s.setString(4, element.getDatePurchased());
                s.setString(5, element.getActiveStatus().asString());

                if (element.getUserId() < 1) {
                    s.setNull(6, Types.INTEGER);
                } else {
                    s.setInt(6, element.getUserId());
                }

                if (element.getStationId() < 1) {
                    s.setNull(7, Types.INTEGER);
                } else {
                    s.setInt(7, element.getStationId());
                }

                s.setTimestamp(8, element.getStatusTimestamp());
                s.setDouble(9, element.getChargeLevel());
                s.setDouble(10, element.getTotalKilometers());
                s.setInt(11, element.getTotalTrips());

                if (element.getPosition() != null && element.getPosition().getLat()!=0 && element.getPosition().getLng()!=0) {
                    s.setDouble(12, element.getPosition().getLng());
                    s.setDouble(13, element.getPosition().getLat());
                } else {
                    s.setNull(12, Types.DOUBLE);
                    s.setNull(13, Types.DOUBLE);
                }

                s.setInt(14, element.getId());

                s.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets all bikes
     * @return a list of Bike-objects.
     */
    @Override
    public List<Bike> getAll() {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM bike");
                ResultSet res = statement.executeQuery()
        ) {
            List<Bike> bikeList = new ArrayList<>();
            while (res.next()) {
                Bike bike = bikeFromRes(res);
                bikeList.add(bike);
            }
            res.close();
            return bikeList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a bike's status history given the id and a limit.
     * @param bikeId the id of the bike as an int.
     * @param limit  the total number of bike status entries to retrieve.
     * @return       a newest-first sorted List of Bike Objects
     * @implNote     ordered by timestamp, newest first.
     */
    public List<Bike> getBikeStatusHistory(int bikeId, int limit) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "SELECT bike.bike_id, type, make, price, date_purchased, s.active_status, s.user_id, s.station_id, " +
                                "s.status_timestamp, s.charging_level, s.total_km, s.total_trips, s.position_long, s.position_lat\n" +
                                "FROM bike\n" +
                                "JOIN bike_status AS s ON bike.bike_id=s.bike_id\n" +
                                "where s.bike_id = ?\n" +
                                "ORDER BY s.status_timestamp DESC\n" +
                                "LIMIT ?");
        ) {
            try {
                statement.setInt(1, bikeId);
                statement.setInt(2, limit);
                ResultSet res = statement.executeQuery();
                List<Bike> historyList = new ArrayList<>();
                while (res.next()) {
                    historyList.add(bikeFromRes(res));
                }
                res.close();
                return historyList;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //Unused methods:

    @Override
    public boolean delete(Bike element) {
        return false;
    }

    @Override
    public boolean addAll(List<Bike> elements) {
        return false;
    }

    @Override
    public boolean updateAll(List<Bike> elements) {
        return false;
    }

    @Override
    public boolean deleteAll(List<Bike> elements) {
        return false;
    }
}