package db.dao;

import db.Database;
import db.DatabaseConnection;
import managers.AdminManager;
import objects.Admin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This singleton class handles the interaction with the database concerning
 * the `admin` table.
 * <br>
 * It reads the response from the database server and converts the results
 * to instances of the Admin class.
 *
 * For now it only contains methods from the DataAccessInterface interface, but
 * it is planned to implement all other needed functionality regarding the
 * `admin` table.
 *
 * @author Joergen Bele Reinfjell
 * date 05.03.2018
 * @author Sindre Thomassen
 * @author Sindre Paulshus
 * @see Admin
 * @see DataAccessInterface
 * @see managers.AdminManager
 */
public class AdminDao implements DataAccessInterface<Admin> {
    private static final AdminDao INSTANCE = new AdminDao();

    private AdminDao() {}

    public static AdminDao getInstance() {
        return INSTANCE;
    }

    private static Admin adminFromRes(ResultSet res) {
        try {
            String username = res.getString("username");
            return new Admin(username);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get an admin object by a unique 'reference' object.
     * @param reference the reference admin instance with the username field set.
     * @return the unique admin object or null.
     */
    @Override
    public Admin get(Admin reference) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM admin WHERE username = ?")
        ) {
            statement.setString(1, reference.getUsername());
            try (ResultSet res = statement.executeQuery()) {
                Admin admin;
                if(res.next()){
                    admin = adminFromRes(res);
                }
                else{
                    res.close();
                    return null;
                }

                /* Return null if the reference was not unique. */
                if (res.next()) {
                    res.close();
                    return null;
                }
                if(admin != null){
                    res.close();
                    return admin;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return  null;
    }

    /**
     * Add an Admin to the DB
     * @param element the admin instance with the username and passwords fields set.
     * @return the unique admin object or null.
     */
    @Override
    public boolean add(Admin element) {
        if(element.getPassword() == null || get(element) != null){
            return false;
        }
        try (
            DatabaseConnection con = Database.getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO admin VALUES(?,password(?),?)");
        ) {
            statement.setString(1, element.getUsername());
            statement.setString(2, element.getPassword() + element.getSalt());
            statement.setString(3, element.getSalt());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update an admin in the DB
     * @param element the admin instance with the username and passwords fields set.
     * @return the unique admin object or null.
     */
    @Override
    public boolean update(Admin element) {
        if(element.getPassword() == null){
            return false;
        }
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("UPDATE admin SET password_hash = password(?), salt = ? where username = ?");
        ) {
            statement.setString(1, element.getPassword() + element.getSalt());
            statement.setString(2, element.getSalt());
            statement.setString(3, element.getUsername());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets a list of all admin objects.
     * @return the list of admin objects.
     */
    @Override
    public List<Admin> getAll() {
        List<Admin> adminList = new ArrayList<>();
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM admin");
                ResultSet res = statement.executeQuery()
        ) {
            while (res.next()) {
                Admin admin = adminFromRes(res);
                if (admin != null) {
                    adminList.add(admin);
                }
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return adminList;
    }

    /**
     * Sends a password and username combo to the database to see if it exists. Used for logging in.
     * @param username
     * @param password
     * @return true if the input is a valid username and password combo, false if not.
     */
    public boolean checkPassword(String username, String password){
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM admin WHERE username = ? AND " +
                                                                    "password_hash = password(concat(?, salt))");
        ) {
            statement.setString(1, username);
            statement.setString(2, password);

            try(ResultSet res = statement.executeQuery()){
                if(res.next()){
                    return true;
                }
                res.close();
            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Unused methods:

    @Override
    public boolean delete(Admin element) {
        return false;
    }

    @Override
    public boolean addAll(List<Admin> elements) {
        return false;
    }

    @Override
    public boolean updateAll(List<Admin> elements) {
        return false;
    }

    @Override
    public boolean deleteAll(List<Admin> elements) {
        return false;
    }

}
