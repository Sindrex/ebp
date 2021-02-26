package app.users;

import javafx.scene.control.Button;
import managers.AdminManager;
import objects.Admin;

import java.security.SecureRandom;

import static logging.SendMail.*;


/**
 * A class for simplifying addition of admins to the view table in the GUI.
 * The object contains an admin, a id and a button for resetting password.
 *
 * @author Sindre Thomassen
 * @date 16.04.2018
 * @see app.users.UsersController
 */
public class AdminObject {
    private Button button = new Button("Reset Password");

    private int id;
    private String username;
    private AdminManager AM;

    public AdminObject(int id, Admin admin, AdminManager manager) {
        this.id = id;
        username = admin.getUsername();
        AM = manager;
        button.setOnAction(e -> buttonAction());
    }

    public Button getButton() {
        return button;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Changes the password of a admin in the database to a randomly generated one,
     * and sends the admin email with new password.
     */
    private void buttonAction() {
        String newPass = AM.randPass(new SecureRandom());
        if(AM.newAdminPassword(new Admin(this.getUsername()), newPass)) {
            sendNewPass(getUsername(), newPass, new Admin(getUsername()));
        } else {
            System.out.println("Password couldn't be changed. Please try again.");
        }
    }

}
