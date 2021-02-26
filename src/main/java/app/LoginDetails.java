package app;

import managers.AdminManager;
import objects.Admin;

/**
 * Static variables used throughout the program. TODO
 *
 * @author Sindre Paulshus
 */
public class LoginDetails {
    public static Admin currentAdmin = new Admin("N/A");
    public static AdminManager adminManager = new AdminManager();
}
