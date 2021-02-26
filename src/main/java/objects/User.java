package objects;

//import com.sun.xml.internal.bind.v2.model.core.ID;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sindre Thomassen
 * date 21.03.2018
 * @see db.dao.UserDao
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;

    private static final int SALT_LENGTH = 24;
    private static SecureRandom r = new SecureRandom();

    /**
     * @param id
     * @param username
     */
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    /**
     * Used only for when a user wants to change its password and on initial user creation.
     *
     * @param username
     * @param password
     */
    public User(int id, String username, String password) throws IllegalArgumentException {
        this.id = id;
        salt = newSalt();
        if (validEmail(username))
            this.username = username;
        else {
            throw new IllegalArgumentException("Invalid email. Make sure the the information is typed correctly.");
        }
        if (validPassword(password))
            this.password = password;
        else
            throw new IllegalArgumentException("Password too short. Password demands at least 6 characters.");
    }

    /**
     * @return username
     */
    public String getUsername() { return username; }

    /**
     * @return password
     */
    public String getPassword() { return password; }

    /**
     * @return id
     */
    public int getId() { return id; }

    /**
     * Changes a users email adress.
     * @param newUsername
     */
    public void changeEmail(String newUsername) {
        username = newUsername;
    }

    /**
     * Function for setting the id on creation
     * @param newId
     */
    public void setId(int newId) {
        id = newId;
    }

    /**
     * Returns a byte array to use as a salt for password hashing.
     * @return
     */
    private static String newSalt() {
        SecureRandom r = new SecureRandom();
        byte[] bytes = new byte[SALT_LENGTH];
        r.nextBytes(bytes);
        BigInteger big = new BigInteger(1, bytes);
        String a = big.toString(16);
        int paddingLength = (bytes.length * 2) - a.length();
        if(paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + a;
        } else {
            return a;
        }
    }

    /**
     * @return User stats as a string
     */
    public String toString() {
        String results = "";
        results += "ID: " + getId() + "\nEmail: " + getUsername();
        return results;
    }

    /**
     * Checks that password requierments is fulfilled.
     * @param pass
     * @return true if requierments are met, false if not.
     */
    private boolean validPassword(String pass) {
        if(pass.length() >= 6)
            return true;
        else
            return false;
    }

    /**
     * A pattern used to confirm that an email adress is valid or not.
     * @param email
     * @return true if email is valid and false if not.
     */
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Private method for email validation on user creation.
     * @param emailStr
     * @return true if the email entered on user is valid, false if not.
     */
    private static boolean validEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.matches();
    }

    /**
     * @return salt
     */
    public String getSalt() {
        return salt;
    }
}
