package objects;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class represents a row in the admin table. If any row is null, it may
 * not be possible to add it to the database, but it can be used as agt
 *
 * @author Joergen Bele Reinfjell
 * @author Sindre Haugland Paulshus
 * date 04.03.2018
 * date 03.04.2018
 * @see managers.AdminManager
 */
public class Admin {
    private final static int SALT_LENGTH = 24;

    private String username;
    private String password = null;
    private String salt;

    public Admin(String username) {
        this.username = username;
        salt = newSalt();
    }

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
        salt = newSalt();
    }

    /*
     * Gets the admins username.
     * @return the username as a String.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the admins password.
     * @return the password as a String.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the admins username.
     * @param username the admin account username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the admins password.
     * @param password the password of the admin account.
     */
    public void changePassword(String password) {
      this.password = password;
    }

    /**
     * Sets the admins password.
     * @return the salt as a String.
     */
    public String getSalt(){
        return salt;
    }

    /**
     * Sets the admins password.
     * @return a new salt as a String.
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

    public String toString() {
        return String.format("bike{username: `%s`, password: `%s`}", username, password);
    }

    @Override
    public boolean equals(Object object){
        if(object == this){
            return true;
        }
        if(object == null){
            return false;
        }
        if(object instanceof Admin){
            Admin admin = (Admin) object;
            return admin.getUsername().equals(this.getUsername());

        }
        return false;
    }
}
