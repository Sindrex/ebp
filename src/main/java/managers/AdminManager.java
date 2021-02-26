package managers;

import db.dao.AdminDao;
import objects.Admin;

import java.util.List;
import java.util.Random;

/**
 * Handles communication between Admins, AdminDao and AdminController.
 *
 * @author Sindre Haugland Paulshus
 * date 03.04.2018
 * @see Admin
 * @see AdminDao
 */
public class AdminManager implements ManagerInterface {
    private static Admin myAdmin = null;
    private final AdminDao dao = AdminDao.getInstance();

    public AdminManager() { }

    @Override
    public boolean refresh() {
        return true;
    }

    /**
     * Login. Sets the current Admin.
     * @param username the admin account username.
     * @param password the admin account password.
     * @return true or false depending on success.
     */
    public boolean login(String username, String password){
        if(dao.checkPassword(username, password)){
            myAdmin = dao.get(new Admin(username));
            return true;
        }
        return false;
    }

    /**
     * Change the logged in admin's password.
     * @param oldPass the logged in admin's old password
     * @param newPass the logged in admin's new password
     * @return true or false depending on success.
     */
    public boolean newPassword(String oldPass, String newPass) {
        if(myAdmin == null){
            return false;
        }
        if(dao.checkPassword(myAdmin.getUsername(), oldPass)){
            String revert = myAdmin.getPassword();
            myAdmin.changePassword(newPass);
            if(dao.update(myAdmin)){
                myAdmin = new Admin(myAdmin.getUsername()); //Dont keep the password.
                return true;
            }
            else{
                myAdmin.changePassword(revert);
                return false;
            }
        }
        return false;
    }

    /**
     * Function used for changing another admins password to a randomly generated one. Used if an admin has forgotten
     * it's password and another admin has to change it.
     * @param admin the Admin object you wish to give a new password.
     * @param newPass the password you wish to give.
     * @return true or false depending on success.
     */
    public boolean newAdminPassword(Admin admin, String newPass) {
        if(admin == null){
            return false;
        }
        admin = new Admin(admin.getUsername(), newPass);
        if(dao.update(admin)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a new admin to the DB.
     * @param newAdmin the admin account username.
     * @return true or false depending on success.
     */
    public boolean addAdmin(Admin newAdmin) {
        if(newAdmin.getPassword() != null){
            for (Admin u : dao.getAll()) {
                if (newAdmin.equals(u)) {
                    return false;
                }
            }
            if(dao.add(newAdmin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current admin.
     * @return a clone of the current Admin.
     */
    public Admin getAdmin() {
        return new Admin(myAdmin.getUsername());
    }

    /**
     * Makes a random password of letters given a Random object.
     * @param r the Random Object.
     * @return the random password as a String.
     */
    public String randPass(Random r) {
        String res = "";
        for (int i = 0; i < r.nextInt(25) + 6; i++) {
            char nextLetter = (char) (r.nextInt(26) + 97);
            res += nextLetter;
        }
        return res;
    }

    /**
     * @return all admins registered in the database.
     */
    public List<Admin> getAll() {
        return dao.getAll();
    }
}
