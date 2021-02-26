package db.dao;

import objects.BikeType;
import db.Database;
import db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles SQL and communicates with the database.
 *
 * @author Sindre Haugland Paulshus
 * date 19.03.2018
 * @see objects.Bike;
 */
public class TypeDao implements DataAccessInterface<BikeType>{
    private static final TypeDao INSTANCE = new TypeDao();

    private TypeDao() {}

    public static TypeDao getInstance() {
        return INSTANCE;
    }

    /**
     * Add a bikeType to the DB.
     * @param element the BikeType you want to add.
     * @return true or false depending on success.
     */
    @Override
    public boolean add(BikeType element) {
        String query = "INSERT INTO bike_type(active, name) VALUES (?, ?)";
        if(executeUpdate(query, element)){
            return true;
        }
        return false;
    }

    /**
     * Update a type in the DB given the type name.
     * @param element the BikeType object you want to update.
     * @return true or false depending on success.
     */
    @Override
    public boolean update(BikeType element) {
        String query = "UPDATE bike_type SET active = ? WHERE name = ?";
        if(executeUpdate(query, element)){
            return true;
        }
        return false;
    }

    /**
     * Gets all deleted types.
     * @return a list of BikeType-objects.
     */
    public List<BikeType> getAllDeleted() {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "SELECT *, (SELECT COUNT(bike_id) FROM bike WHERE type = bike_type.name AND active='DELETED') AS bike_count FROM bike_type;"
                );
                ResultSet res = statement.executeQuery()
        ) {
            List<BikeType> typeList = new ArrayList<>();
            while (res.next()) {
                boolean active = "ACTIVE".equals(res.getString("active"));
                BikeType t = new BikeType(
                        res.getString("name"),
                        res.getTimestamp("timestamp"),
                        res.getInt("bike_count"),
                        active
                );
                typeList.add(t);
            }
            res.close();
            return typeList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets all active types.
     * @return a list of BikeType-objects.
     */
    @Override
    public List<BikeType> getAll() {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "SELECT *, (SELECT COUNT(bike_id) FROM bike WHERE type = bike_type.name AND active='ACTIVE') AS bike_count FROM bike_type;"
                );
                ResultSet res = statement.executeQuery()
        ) {
            List<BikeType> typeList = new ArrayList<>();
            while (res.next()) {
                boolean active = "ACTIVE".equals(res.getString("active"));
                BikeType t = new BikeType(
                        res.getString("name"),
                        res.getTimestamp("timestamp"),
                        res.getInt("bike_count"),
                        active
                );
                typeList.add(t);
            }
            res.close();
            return typeList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a given type is in the DB.
     * @param reference the BikeType to check for.
     * @return true or false depending on success.
     */
    public boolean has(BikeType reference){
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM bike_type WHERE name = ?");
        ) {
            try{
                statement.setString(1, reference.getName());
                ResultSet res = statement.executeQuery();
                boolean ok = res.next() && reference.getName().equals(res.getString("name"));
                res.close();
                return ok;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Private method that executes an update given the query and the element
     * @param query the query to execute.
     * @param element the element to execute the query on.
     * @return true or false depending on success.
     */
    private boolean executeUpdate(String query, BikeType element){
        try (
            DatabaseConnection con = Database.getConnection();
            PreparedStatement statement = con.prepareStatement(query)
        ) {
            if(element.getActive()){
                statement.setString(1, "ACTIVE");
            }
            else{
                statement.setString(1, "DELETED");
            }

            statement.setString(2, element.getName());

            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Unused methods:

    @Override
    public BikeType get(BikeType reference) {
        return null;
    }

    @Override
    public boolean delete(BikeType element) {
        return false;
    }

    @Override
    public boolean addAll(List<BikeType> elements) {
        return false;
    }

    @Override
    public boolean updateAll(List<BikeType> elements) {
        return false;
    }

    @Override
    public boolean deleteAll(List<BikeType> elements) {
        return false;
    }
}
