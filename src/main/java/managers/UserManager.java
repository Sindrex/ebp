package managers;

import db.dao.UserDao;
import logging.Logger;
import objects.User;
import static logging.SendMail.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Sindre Thomassen
 * date 21.03.2018
 * @see User
 * @see UserDao
 */
public class UserManager implements ManagerInterface {
    private static List<User> users = new ArrayList<>();
    private final static UserDao UD = UserDao.getInstance();

    private final static String[] providers = {
            "@hotmail.com", "@gmail.com", "@yahoo.com", "@live.no", "@ntnu.no",
            "@bing.com", "@outlook.com", "@mail.com", "@myspace.mail", "@stud.ntnu.no"
    };

    private final static List<String> FIRST_NAMES_LIST = Arrays.asList(
            "Aaron", "Abbas", "Abdirahman", "Abdul", "Abdullah", "Abdullahi",
            "Abdulrahman", "Abel", "Adam", "Adrian", "Ahmad", "Ahmed", "Aiden",
            "Ailo", "Akram", "Aksel", "Alan", "Albert", "Albin", "Aleksander",
            "Alex", "Alexander", "Alf", "Alfred", "Ali", "Alvar", "Alvin",
            "Amadeus", "Amandus", "Amar", "Amin", "Amir", "Amund", "Anas",
            "Anders", "Andre", "Andreas", "Anthony", "Anton", "Antoni", "Antonio",
            "Aras", "Are", "Arian", "Arijus", "Arin", "Arman", "Armin", "Arn",
            "Arnas", "Arne", "Aron", "Aronas", "Arthur", "Artur", "Arvid", "Arvin", "Aryan"
    );

    private final static List<String> LAST_NAMES_LIST = Arrays.asList(
            "Hansen", "Johansen", "Olsen", "Larsen", "Andersen", "Pedersen",
            "Nilsen", "Kristiansen", "Jensen", "Karlsen", "Johnsen", "Pettersen",
            "Eriksen", "Berg", "Haugen", "Hagen", "Johannessen", "Andreassen",
            "Jacobsen", "Dahl", "Jørgensen", "Halvorsen", "Henriksen", "Lund",
            "Sørensen", "Jakobsen", "Moen", "Gundersen", "Iversen", "Strand",
            "Svendsen", "Solberg", "Martinsen", "Knutsen", "Eide", "Paulsen",
            "Bakken", "Kristoffersen", "Mathisen", "Lie", "Amundsen"
    );


    public UserManager() {
        try {
            users = UD.getAll();
            //List<User> help = UD.getAll();
            /*for (User u : help) {
                users.add(u);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return a copy of the list of all users registered.
     */
    public List<User> getAll() {
        refresh();
        List<User> help = new ArrayList<>();
        for (User u : users) {
            help.add(new User(u.getId(), u.getUsername()));
        }
        return help;
    }

    /**
     * Updates a user if anything is changed. Password, email
     *
     * @param element
     * @return true if update was successfull, and false if something went wrong.
     */
    public boolean updateUser(User element) {
        refresh();
        try {
            UD.update(element);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Changes a users password if a user with the same id as reference object exists.
     *
     * @param element
     * @return true if password was changed successfully, and false if something went wrong.
     */
    public boolean newPassword(User element) {
        refresh();
        for (User u : users) {
            if (u.getId() == element.getId()) {
                if (UD.updatePassword(element)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Changes the email of a user if a user with the same id as the reference object exists.
     *
     * @param element
     * @return kugyug
     */
    public boolean newEmail(User element) {
        refresh();
        try {
            String oldMail;
            for (User u : users) {
                if (u.getId() == element.getId()) {
                    oldMail = u.getUsername();
                    u.changeEmail(element.getUsername());
                    if (UD.update(element)) {
                        return true;
                    } else {
                        u.changeEmail(oldMail);
                        return false;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Adds a new user to database if it does not allready exist.
     *
     * @param newUser
     * @return
     */
    public boolean addUser(User newUser) {
        refresh();
        for (User u : users) {
            if (newUser.getUsername().equals(u.getUsername())) {
                return false;
            }
        }
        int highest = 0;
        for (User u : users) {
            if (u.getId() > highest)
                highest = u.getId();
        }
        newUser.setId(highest + 1);

        if (UD.add(newUser)) {
            users.add(new User(newUser.getId(), newUser.getUsername()));
            return true;
        }
        return false;
    }

    /**
     * Returns a user with the specified ID if it exists. Otherwise, returns null.
     *
     * @param id
     * @return user with specified id or null if it does not exist.
     */
    public User getUser(int id) {
        refresh();
        for (User u : users) {
            if (u.getId() == id) {
                return new User(u.getId(), u.getUsername());
            }
        }
        return null;
    }

    /**
     * Updates all objects in userlist with new information from the database.
     */
    @Override
    public boolean refresh() {
        users = UD.getAll();
        return true;
    }

    /**
     * Function for simulation purposes. Only used for adding random users to the database in connection to simulation.
     * Uses two private help functions randUsername and randPass which returns a random username and password.
     *
     * @return returns a user if it is successfully added to the database.
     */
    public User addRandomUser() {
        Random r = new Random();
        try {
            String username = randUsername(r);
            String password = randPass(r);
            User u = new User(-1,username, password);
            if (addUser(u)) {
                //newUserMail(username, password);
                return u;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Fail");
            return null;
        }
    }

    /**
     * Generates a new random username by combining random first names, last names and emails providers.
     * @param r the random object to use as generator.
     * @return the random username.
     */
    private String randUsername(Random r) {
        return
                FIRST_NAMES_LIST.get(r.nextInt(FIRST_NAMES_LIST.size())).toLowerCase() +
                "." +
                LAST_NAMES_LIST.get(r.nextInt(LAST_NAMES_LIST.size())).toLowerCase() +
                providers[r.nextInt(providers.length)];
   }

    public String randPass(Random r) {
        String res = "";
        for (int i = 0; i < r.nextInt(25) + 6; i++) {
            char nextLetter = (char) (r.nextInt(26) + 97);
            res += nextLetter;
        }
        return res;
    }

    public boolean login(String username, String password) {
        if (UD.validatePassword(username, password)) {
            System.out.println("Login successful.");
            return true;
        } else {
            Logger.errf("\nAccess denied.\nWrong username or password.");
            return false;
        }
    }
}