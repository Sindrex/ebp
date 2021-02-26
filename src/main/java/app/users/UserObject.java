package app.users;

import javafx.scene.control.Button;
import managers.UserManager;
import objects.User;

import java.security.SecureRandom;

import static logging.SendMail.*;

/**
 * A class for simplifying addition of users to the view table in the GUI.
 * The object contains a user and a button for resetting password.
 *
 * @author Sindre Thomassen
 * @date 16.04.2018
 * @see app.users.UsersController
 */
public class UserObject {
    private Button button = new Button("Reset Password");

    private int id;
    private String username;
    private UserManager UM;

    public UserObject(User user, UserManager UM) {
        id = user.getId();
        username = user.getUsername();
        this.UM = UM;
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
     * Changes the password of a user in the database to a randomly generated one,
     * and sends the new password with email to a test email account.
     */
    private void buttonAction() {
        String newPass = UM.randPass(new SecureRandom());
        if(UM.newPassword(new User(getId(), getUsername(), newPass))) {
            sendNewPass(getUsername(), newPass);
        } else {
            System.out.println("Password couldn't be changed. Please try again.");
        }
    }
}
