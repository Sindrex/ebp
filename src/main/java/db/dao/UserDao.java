package db.dao;

import db.Database;
import db.DatabaseConnection;
import objects.User;

//import javax.jws.soap.SOAPBinding;
//import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

//import static objects.User.getSalt;

/**
 * This singleton class handles the interaction with the database concerning
 * the `user` table.
 * <br>
 *      It reads the response from the database server and converts the results
 * to instances of the User class.
 *
 * @author Sindre Thomassen
 * date 21.03.2018
 * @see User
 * @see DataAccessInterface
 */
public class UserDao implements DataAccessInterface<User> {
    private static final UserDao INSTANCE = new UserDao();

    private UserDao() {}

    /**
     * @return Instance of UserDao
     */
    public static UserDao getInstance() { return INSTANCE; }

    /**
     * @param res
     * @return user made from a resultset from the database.
     */
    private User userFromRes(ResultSet res) {
        try {
            int id = res.getInt("user_id");
            String email = res.getString("email");
            return new User(id, email);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param reference the reference object with the fields used for unique
     *                 identification.
     * @return User object made from a row from the database with the same user id as the reference object.
     */
    @Override
    public User get(User reference) {
        try (
            DatabaseConnection con = Database.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "SELECT * FROM user WHERE user_id = ?"
            );
        ) {
            statement.setInt(1, reference.getId());
            ResultSet res = statement.executeQuery();
            User myUser = null;
            if(res.next()) {
                myUser = userFromRes(res);
            }
            res.close();
            return myUser;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Adds a new user to the database. Throws an exception if the user is already registered.
     * @param element the element to add.
     * @return true if new user was successfully added to the database, false if something went wrong.
     * @throws Exception
     */
    @Override
    public boolean add(User element) {
        try (
            DatabaseConnection con = Database.getConnection();
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO user (user_id, email, password_hash, salt) VALUES (?, ?, password(?), ?)"
            );
        ) {
            try {
                statement.setInt(1, element.getId());
                statement.setString(2, element.getUsername());
                statement.setString(3, element.getPassword() + element.getSalt());
                statement.setString(4, element.getSalt());
                statement.executeUpdate();
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Updates a user email in the database with the same id as the element used as parameter.
     *
     * @param element the element to update.
     * @return true if update was successful, false if something went wrong, for example if the user does not exist.
     * @throws Exception
     */
    @Override
    public boolean update(User element) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE user SET email = ? WHERE user_id = ?"
                );
        ) {
            try {
                con.autocommit(false);
                statement.setString(1,element.getUsername());
                statement.setInt(2, element.getId());
                statement.executeUpdate();
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                con.autocommit(true);
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Replaces the current password with a new password.
     * @param element
     * @return
     */
    public boolean updatePassword(User element) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE user SET password_hash = password(?), salt = ? WHERE email = ?"
                )
        ) {
            try {
                statement.setString(1,element.getPassword() + element.getSalt());
                statement.setString(2, element.getSalt());
                statement.setString(3, element.getUsername());
                statement.executeUpdate();
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean delete(User element) { return false; }

    /**
     * @return returns a list of all users objects made from users currently saved in the database
     * @throws Exception
     */
    @Override
    public List<User> getAll() {
        try (
            DatabaseConnection con = Database.getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM user");
        ) {
            List<User> help = new ArrayList<>();
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                User user = userFromRes(res);
                if(user != null) {
                    help.add(user);
                }
            }
            res.close();
            return help;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean addAll(List<User> elements) throws Exception { return false; }

    @Override
    public boolean updateAll(List<User> elements) throws Exception { return false; }

    @Override
    public boolean deleteAll(List<User> elements) throws Exception { return false; }

    /**
     * Sends a password and username combo to the database to see if it exists. Used for logging in.
     * @param username
     * @param password
     * @return true if the input is a valid username password combo, false if not.
     */
    public boolean validatePassword(String username, String password) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "SELECT * from user WHERE email = ? AND password_hash = password(concat(?, salt))");
        ){
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet res = statement.executeQuery();
            boolean ok = false;
            if(res.next())
                ok = true;
            else
                ok = false;
            res.close();
            return ok;
        } catch (Exception e) {
            return false;
        }
    }
}
