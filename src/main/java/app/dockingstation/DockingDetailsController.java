package app.dockingstation;

import app.Inputs;
import db.dao.BikeDao;
import db.dao.DockingStationDao;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import objects.Bike;
import objects.DockingStation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the docking details view.
 *
 * @author Aleksander Johansen
 * @author Sindre Paulshus
 * @author Odd-Erik Frantzen
 * @author Joergen Bele Reinfjell
 */
public class DockingDetailsController {

    @FXML private BorderPane mainPage;

    @FXML private Button backButton;
    @FXML private Button deleteButton;

    @FXML private GridPane dockDetailsGridPane;

    private DockingStationController.DockingCommunicator communicator;

    private DockingStation currentDock;

    @FXML private TableView<BikesDockedRow> bikeList;
    @FXML private TableView historyTableView;

    /**
     * Method that is run when loading the DockingDetails document. It runs only once, and is used for initializing purposes, such as setting button
     * functions and initializing the tableview columns so that data can be added.
     */
    public void initialize(){
        //restoreButton.setDisable(true);
        //restoreButton.setOnAction(e -> restoreDock());
        deleteButton.setOnAction(e -> {
            deleteDock();
        });
        backButton.setOnAction(e -> {
            communicator.closeDetails();
            bikeList.getItems().clear();
            historyTableView.getItems().clear();
        });

        //Initialize History columns
        TableColumn<DockingHistoryRow, String> timeStampCol = (TableColumn<DockingHistoryRow, String>)historyTableView.getColumns().get(0);
        timeStampCol.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));

        TableColumn<DockingHistoryRow, Double> powerUsageCol = (TableColumn<DockingHistoryRow, Double>)historyTableView.getColumns().get(1);
        powerUsageCol.setCellValueFactory(new PropertyValueFactory<>("presentPowerUsage"));

        //Initialize Bikes Docked Column
        TableColumn<BikesDockedRow, Integer> idCol = (TableColumn<BikesDockedRow, Integer>)bikeList.getColumns().get(0);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<BikesDockedRow, Double> chargeCol = (TableColumn<BikesDockedRow, Double>)bikeList.getColumns().get(1);
        chargeCol.setCellValueFactory(new PropertyValueFactory<>("chargeLevel"));
    }

    /**
     * Used to hide the docking details page.
     * @param hide hide boolean. If its false, the panel appears, else it is set to invisible.
     */
    public void hidePage(boolean hide){
        mainPage.setVisible(!hide);
    }

    /**
     * Sets the docking communicator, so that DockingDetailsController and DockingController can communicate.
     * @param communicator An interface that has some methods this class can call upon.
     */
    public void setDockingCommunicator(DockingStationController.DockingCommunicator communicator){
        this.communicator = communicator;
    }

    /**
     * Method for deleting the current viewed dock. The delete button then becomes disabled and the dock is deleted.
     */
    private void deleteDock(){
        if(communicator.deleteDock(currentDock)){
            deleteButton.setDisable(true);
        }
    }

    /**
     * Update method. used for updating the information in docking details. It takes a dock and used it's attributes to display correct information.
     * @param dock The docking object used for extracting correct data.
     */
    @Deprecated
    public void updateDetails(DockingStation dock){
        // Sets the delete button to active and restore to disabled.
        deleteButton.setDisable(false);

        ArrayList<Label> list = new ArrayList<>();
        int counter = 0;
        for(Node n : dockDetailsGridPane.getChildren()){
            if(!(n instanceof Label)){
                continue;
            }
            if(counter == 1){
                list.add((Label) n);
                counter = 0;
            }
            else{
                counter++;
            }
        }

        list.get(0).setText(String.valueOf(dock.getDockID()));
        list.get(1).setText(String.valueOf(dock.getMAX_BIKES()));
        list.get(2).setText(String.valueOf(Inputs.roundDouble(dock.getPowerUsage(), 1)));
        list.get(3).setText(String.valueOf(Inputs.roundDouble(dock.getPosLong(), 5)));
        list.get(4).setText(String.valueOf(Inputs.roundDouble(dock.getPosLat(), 5)));

        List<DockingStation> history = DockingStationDao.getInstance().getDockingStatusHistory(dock.getDockID(), 100 ); // TODO: 4/17/18 decide on limit ;
        for(int i = 0; i < history.size(); i++){
            if(history == null){
                break;
            }
            DockingHistoryRow row = new DockingHistoryRow(history.get(i));
            historyTableView.getItems().add(row);
        }
        List<Integer> bikes = DockingStationDao.getInstance().bikesInDock(dock);
        for(int i = 0; i < bikes.size(); i++){
            BikesDockedRow row = new BikesDockedRow(bikes.get(i));
            bikeList.getItems().add(row);
        }

        // Saving the current dock
        currentDock = dock;
    }

    /**
     * Class that is used for putting the data in the docking history table.
     */
    public class DockingHistoryRow {
        private Timestamp timeStamp;
        private double presentPowerUsage;

        public DockingHistoryRow(DockingStation dock){
            this.timeStamp = dock.getStatusTimestamp();
            this.presentPowerUsage = dock.getPowerUsage();
        }

        public Timestamp getTimeStamp() {
            return timeStamp;
        }

        public double getPresentPowerUsage() {
            return presentPowerUsage;
        }
    }

    /**
     * Class for putting data into the bike info tableview.
     */
    public class BikesDockedRow {
        private int id;
        private double chargeLevel;
        private Bike bike;

        public BikesDockedRow(int id){
            this.id = id;
            this.bike = BikeDao.getInstance().getById(id);
            if (this.bike == null) {
                this.chargeLevel = -1;
            } else {
                this.chargeLevel = bike.getChargeLevel();
            }
        }

        public int getId() {
            return id;
        }

        public double getChargeLevel(){
            return chargeLevel;
        }
    }
}