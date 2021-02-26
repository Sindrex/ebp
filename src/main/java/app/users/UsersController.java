package app.users;

import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import logging.Logger;
import managers.AdminManager;
import managers.UserManager;
import objects.User;

import java.util.List;
/**
 * @author Aleksander Johansen
 * @author Sindre Thomassen
 */
public class UsersController {

    public TableView<UserObject> userTable;
    public TableView<AdminObject> adminTable;

    private long startTime;
    private UserManager um = new UserManager();
    private AdminManager AM = new AdminManager();

    @FXML private TextField searchbar;
    @FXML private Label matches;

    @FXML private HBox customerView;
    @FXML private HBox adminView;

    @FXML private PaneSwitchButton customerViewTabButton;
    @FXML private PaneSwitchButton adminViewTabButton;

    private PaneSwitchButtonGroup buttonGroup;

    @FXML
    public void initialize() {
        buttonGroup = new PaneSwitchButtonGroup();
        buttonGroup.addView(customerViewTabButton, customerView, e -> {
            searchbar.setText("");
            refresh();
            return null;
        });
        buttonGroup.addView(adminViewTabButton, adminView, e -> {
            refresh();
            return null;
        });
        buttonGroup.setSelectedPanelByButton(customerViewTabButton);

        searchbar.setOnAction(e -> refresh());
        matches.setText("No search detected.");

        //Initialize table-cell values property
        String[] variables = new String[]{"id", "username", "button"};

        TableColumn<UserObject, String> col = (TableColumn<UserObject, String>) userTable.getColumns().get(0);
        col.setCellValueFactory(new PropertyValueFactory<UserObject, String>(variables[0]));

        col = (TableColumn<UserObject, String>) userTable.getColumns().get(1);
        col.setCellValueFactory(new PropertyValueFactory<UserObject, String>(variables[1]));

        TableColumn<UserObject, Button> userButtonTableColumn = (TableColumn<UserObject, Button>) userTable.getColumns().get(2);
        userButtonTableColumn.setCellValueFactory(new PropertyValueFactory<UserObject, Button>(variables[2]));

        TableColumn<AdminObject, String> adminCol = (TableColumn<AdminObject, String>) adminTable.getColumns().get(0);
        adminCol.setCellValueFactory(new PropertyValueFactory<AdminObject, String>(variables[0]));

        adminCol = (TableColumn<AdminObject, String>) adminTable.getColumns().get(1);
        adminCol.setCellValueFactory(new PropertyValueFactory<AdminObject, String>(variables[1]));

        TableColumn<AdminObject, Button> adminButtonTableColumn = (TableColumn<AdminObject, Button>) adminTable.getColumns().get(2);
        adminButtonTableColumn.setCellValueFactory(new PropertyValueFactory<AdminObject, Button>(variables[2]));

        new Thread(this::refresh).start();
    }

    /**
     * Adds a new random user to the database and prints it to the gui table. Used for simulation purposes.
     */
    public void addUser() {
        User u = um.addRandomUser();
        if (u != null) {
            Logger.logf("New user: " + u.toString() + "Added.");
        } else {
            Logger.errf("New user Failed to be added.");
        }
        refresh();
    }

    /**
     * Refreshes the tables in the gui.
     */
    public void refresh() {
        List<User> userList = um.getAll();

        Platform.runLater(() -> {
            int match = 0;
            userTable.getItems().remove(0, userTable.getItems().size());
            adminTable.getItems().remove(0, adminTable.getItems().size());
            for (User u : userList) {
                if (u.getUsername().contains(searchbar.getText()) || searchbar.getText().equals("")) {
                    userTable.getItems().add(new UserObject(u, um));
                    match++;
                }

                if (searchbar.getText().equals(""))
                    matches.setText("No search detected");
                else
                    matches.setText("Matches found: " + match);

            }
            for (int i = 1; i <= AM.getAll().size(); i++) {
                adminTable.getItems().add(new AdminObject(i, AM.getAll().get(i - 1), AM));
            }
        });
    }
}