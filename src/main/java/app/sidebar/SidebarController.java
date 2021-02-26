package app.sidebar;

import app.ControllerCommunication;
import app.LoginDetails;
import app.bikes.BikesController;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logging.Logger;
import managers.AdminManager;
import objects.Admin;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the sidebar view.
 * Communicates with the MainController to switch between views.
 *
 * @author Joergen Bele Reinfjell
 * @author Sindre Paulshus
 */
public class SidebarController implements Initializable {
    //public Button usersButton;
    //public Button bikesButton;
    public GridPane sidebarGridPane;

    //public Button dockingStationsButton;

    @FXML private final PaneSwitchButtonGroup buttonGroup = new PaneSwitchButtonGroup();

    @FXML private PaneSwitchButton dockingStationsButton;
    @FXML private PaneSwitchButton usersButton;
    @FXML private PaneSwitchButton bikesButton;
    @FXML private PaneSwitchButton deletedButton;
    @FXML private PaneSwitchButton repairsButton;
    @FXML private PaneSwitchButton statisticsButton;
    @FXML private PaneSwitchButton accountButton;

    @FXML private PaneSwitchButton logoutButton;

    private BikesController bikesController;

    public void setBikesController(BikesController ctrl){
        bikesController = ctrl;
    }

    private ControllerCommunication controllerCommunicator = path -> Logger.log("SidebarController: controllerCommunicator not initialized!");

    public void setControllerCommunicator(ControllerCommunication controllerCommunicator) {
        this.controllerCommunicator = controllerCommunicator;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.log("SidebarController: Initializing sidebar.");

        buttonGroup.addView(dockingStationsButton, null, b -> {
            Logger.log("SidebarController: Docking Station panel loading");
            controllerCommunicator.changeParent("/app/dockingstation/DockingStationView.fxml");
            return null;
        }, b -> {
            //Logger.log("Docking Station panel unloading");
            //controllerCommunicator.unload("/app/dockingstation/DockingStationView.fxml");
            return null;
        });

        buttonGroup.addView(bikesButton, null, b -> {
            Logger.log("SidebarController: Bikes panel loading");
            controllerCommunicator.changeParent("/app/bikes/BikesView.fxml");
            return null;
        });

        buttonGroup.addView(repairsButton, null, b -> {
            Logger.log(("SidebarController: Repair panel loading"));
            controllerCommunicator.changeParent("/app/repair/RepairsView.fxml");
            return null;
        });

        buttonGroup.addView(usersButton, null, b -> {
            Logger.log("SidebarController: Users panel loading");
            controllerCommunicator.changeParent("/app/users/UsersView.fxml");
            return null;
        });

        buttonGroup.addView(deletedButton, null, b -> {
            Logger.log("SidebarController: Deleted panel loading");
            controllerCommunicator.changeParent("/app/deleted/DeletedView.fxml");
            return null;
        });

        buttonGroup.addView(statisticsButton, null, b -> {
            Logger.log("SidebarController: Statistics panel loading");
            controllerCommunicator.changeParent("/app/statistics/StatisticsView.fxml");
            return null;
        });

        buttonGroup.addView(accountButton, null, b -> {
            Logger.log("SidebarController: Account panel loading");
            controllerCommunicator.changeParent("/app/account/AccountView.fxml");
            return null;
        });

        buttonGroup.addView(logoutButton, null, b -> {
            Logger.log("SidebarController: Logging out");
            Logger.err("Log out functionality is not implemented");
            try{
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Stage stage = new Stage();
                Parent parent = FXMLLoader.load(getClass().getResource("/app/login/LoginView.fxml"));
                stage.setResizable(true);
                stage.setTitle("Admin Application");
                stage.setScene(new Scene(parent, screenSize.getWidth() * 0.9, screenSize.getHeight() * 0.9));
                stage.show();

                LoginDetails.currentAdmin = new Admin("N/A");
                LoginDetails.adminManager = new AdminManager();

                Stage thisStage = (Stage) logoutButton.getScene().getWindow();
                thisStage.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        });
    }
}
