package app.bikes;

import app.AlertBox;
import app.Inputs;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import db.dao.BikeDao;
import db.dao.TypeDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import managers.BikeManager;
import objects.Bike;
import objects.BikeType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the bike details view.
 *
 * @author Aleksander Johansen
 * @author Sindre Paulshus
 * @author Odd-Erik Frantzen
 * @author Joergen Bele Reinfjell
 */
public class BikeDetailsController {

    private BikesController.TableCommunicator communicator;

    private BikeManager bikeManager = new BikeManager();

    @FXML private GridPane titleGrid;
    @FXML private GridPane detailGridPane;
    @FXML private BorderPane mainPage;

    @FXML private PaneSwitchButton detailsTabButton;
    @FXML private PaneSwitchButton historyTabButton;

    @FXML private TextField editBrandBox;

    @FXML private BikesController controller; // <-- it is used, trust me!

    @FXML private TableView historyView;

    @FXML private HBox detailsPage;
    @FXML private HBox historyPage;

    @FXML private Label typeLabel;
    @FXML private Label brandLabel;

    @FXML private Button backButton;
    @FXML private Button editTypeButton;
    @FXML private Button deleteButton;
    @FXML private Button editBrandButton;

    @FXML private ComboBox<BikeType> editTypeCombo;

    public Bike currentViewedBike;

    private PaneSwitchButtonGroup paneSwitchButtonGroup = new PaneSwitchButtonGroup();

    private final static Duration THREAD_UPDATE_PERIOD = Duration.seconds(2);
    private ScheduledService<Void> updateService = null;

    private BikeDao bikeDao = BikeDao.getInstance();
    private final int HISTORY_LIMIT = 100;

    /**
     * Initialize method. Runs once when fxml document is loaded. Used for setting button functions and initializing tableview columns etc.
     */
    public void initialize(){
        deleteButton.setStyle("-fx-background-color: red");

        defaultData("N/A");
        paneSwitchButtonGroup.addView(detailsTabButton, detailsPage);
        paneSwitchButtonGroup.addView(historyTabButton, historyPage);
        paneSwitchButtonGroup.setSelectedPanelByButton(detailsTabButton);
        detailsTabButton.setOnAction(e -> {deleteButton.setVisible(true); titleGrid.setVisible(true);});
        historyTabButton.setOnAction(e -> {deleteButton.setVisible(false); titleGrid.setVisible(false);});
        backButton.setOnAction(e -> {
            communicator.closeBikeDetails();
            editTypeCombo.setVisible(false);
            editTypeButton.setText("Edit");
            editTypeCombo.getSelectionModel().clearSelection();
            paneSwitchButtonGroup.setSelectedPanelByButton(detailsTabButton);
        });
        deleteButton.setOnAction(e -> {
            try{
                bikeManager.deleteBike(currentViewedBike.getId());
                communicator.closeBikeDetails();
                editTypeCombo.setVisible(false);
                editTypeButton.setText("Edit");
                editTypeCombo.getSelectionModel().clearSelection();
                paneSwitchButtonGroup.setSelectedPanelByButton(detailsTabButton);
            }
            catch(Exception e1){
                e1.printStackTrace();
                AlertBox.errorMessage("Delete Error", "Could not delete bike");
            }
        });

        // Initialize columns in bikehistory.
        String[] values = {"statusTimestamp", "userId", "stationId", "chargeLevel", "totalTrips", "totalKilometers", "posLong", "posLat"};
        // Timestamp
        TableColumn<BikeHistoryRow, Timestamp> timeCol = (TableColumn<BikeHistoryRow, Timestamp>) historyView.getColumns().get(0);
        timeCol.setCellValueFactory(new PropertyValueFactory<>(values[0]));
        // UserId
        TableColumn<BikeHistoryRow, Integer> userCol = (TableColumn<BikeHistoryRow, Integer>) historyView.getColumns().get(1);
        userCol.setCellValueFactory(new PropertyValueFactory<>(values[1]));
        // StationId
        TableColumn<BikeHistoryRow, Integer> dockCol = (TableColumn<BikeHistoryRow, Integer>) historyView.getColumns().get(2);
        dockCol.setCellValueFactory(new PropertyValueFactory<>(values[2]));
        // ChargingLevel
        TableColumn<BikeHistoryRow, Double> chargeCol = (TableColumn<BikeHistoryRow, Double>) historyView.getColumns().get(3);
        chargeCol.setCellValueFactory(new PropertyValueFactory<>(values[3]));
        // TotalTrips
        TableColumn<BikeHistoryRow, Integer> totTripsCol = (TableColumn<BikeHistoryRow, Integer>) historyView.getColumns().get(4);
        totTripsCol.setCellValueFactory(new PropertyValueFactory<>(values[4]));
        // TotalKilometers
        TableColumn<BikeHistoryRow, Double> totKmCol = (TableColumn<BikeHistoryRow, Double>) historyView.getColumns().get(5);
        totKmCol.setCellValueFactory(new PropertyValueFactory<>(values[5]));
        // PosLong
        TableColumn<BikeHistoryRow, Double> posLongCol = (TableColumn<BikeHistoryRow, Double>) historyView.getColumns().get(6);
        posLongCol.setCellValueFactory(new PropertyValueFactory<>(values[6]));
        // PosLat
        TableColumn<BikeHistoryRow, Double> posLatCol = (TableColumn<BikeHistoryRow, Double>) historyView.getColumns().get(7);
        posLatCol.setCellValueFactory(new PropertyValueFactory<>(values[7]));

        /*
        // Task for updating the list of docking stations++.
        updateService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        if(currentViewedBike != null){
                            Logger.log("BikeDetails: update task called!");
                            currentViewedBike = bikeDao.get(currentViewedBike);
                            Platform.runLater(() -> {
                                updateDetails(currentViewedBike,
                                        bikeDao.getBikeStatusHistory(currentViewedBike.getId(), HISTORY_LIMIT));
                            });
                        }
                        return null;
                    }
                };
            }
        };
        updateService.start();
        updateService.setPeriod(THREAD_UPDATE_PERIOD);
        */
    }

    /**
     * Used to set the communicator, so that this class and BikesController can communicate both ways.
     * @param communicator Interface from BikesController which gives this controller access to some methods in BikesController.
     */
    public void setCommunicator(BikesController.TableCommunicator communicator){
        this.communicator = communicator;
    }

    private void defaultData(String data){
        int counter = 0;
        for(Node n : detailGridPane.getChildren()){
            if(!(n instanceof Label)){
                continue;
            }

            if(counter == 1){
                ((Label) n).setText(data);
                counter = 0;
            }
            else{
                counter++;
            }
        }
    }

    /**
     * Method for hiding bike details/history. Used for when switching between mainbike view and details.
     * @param hide if true, hide page, else page is visible.
     */
    public void hidePage(boolean hide){
       mainPage.setVisible(!hide);
    }

    /**
     * Updates the bike details by taking a bike, and sets the corresponding labels equal to that of the bike's attributes.
     * Also fills in the bikehistory tableview with the last 100 minutes of history for that bike.
     * @param bike The bike that is picked from the bikelist.
     * @param bikeHistories The bikes list of histories, so that the
     */
    public void updateDetails(Bike bike, List<Bike> bikeHistories){
        // Clear historyView
        historyView.getItems().clear();

        int counter = 0;
        ArrayList<Label> detailList = new ArrayList<>();
        for(Node n : detailGridPane.getChildren()){
            if(!(n instanceof Label)){
                continue;
            }
            if(counter == 1){
                detailList.add((Label) n);
                counter = 0;
            }
            else{
                counter = 1;
            }
        }

        currentViewedBike = bike;

        detailList.get(0).setText(String.valueOf(bike.getId()));
        detailList.get(1).setText(bike.getType());
        detailList.get(2).setText(bike.getMake());
        detailList.get(3).setText(String.valueOf(bike.getPrice()));
        detailList.get(4).setText(bike.getDatePurchased());

        detailList.get(5).setText(bike.getActiveStatus().asString());
        if(bike.getActiveStatus() == Bike.ActiveStatus.RENTED){
            detailList.get(6).setText(String.valueOf(bike.getUserId()));
        }
        else if (bike.getActiveStatus() == Bike.ActiveStatus.DOCKED){
            detailList.get(6).setText(String.valueOf(bike.getStationId()));
        }
        else{
            detailList.get(6).setText("N/A");
        }
        detailList.get(7).setText(String.valueOf(Inputs.roundDouble(bike.getChargeLevel(), 1)));
        detailList.get(8).setText(String.valueOf(Inputs.roundDouble(bike.getPosition().getLng(), 5)));
        detailList.get(9).setText(String.valueOf(Inputs.roundDouble(bike.getPosition().getLat(), 5)));
        detailList.get(10).setText(String.valueOf(bike.getTotalTrips()));
        detailList.get(11).setText(String.valueOf(Inputs.roundDouble(bike.getTotalKilometers(), 1)));

        // Putting data in the historyview

        for (Bike bikeHistory : bikeHistories) {
            historyView.getItems().add(new BikeHistoryRow(bikeHistory));
        }
        //Initialize ComboBox
        ObservableList<BikeType> types = FXCollections.observableArrayList(TypeDao.getInstance().getAll());
        for(BikeType type : types){
            editTypeCombo.getItems().setAll(types);
        }
    }

    /**
     * Method for editing the type for currently viewed bike.
     */
    @FXML
    public void editType(){
        editTypeCombo.setVisible(!editTypeCombo.isVisible());
        if (editTypeCombo.isVisible()){
            editTypeButton.setText("Save");
        } else{
            editTypeButton.setText("Edit");

            //Save here
            Bike.Builder builder = new Bike.Builder().from(currentViewedBike);
            builder.type(editTypeCombo.getSelectionModel().getSelectedItem().toString());
            boolean status = BikeDao.getInstance().update(builder.build());
            if (status){
                typeLabel.setText(editTypeCombo.getSelectionModel().getSelectedItem().toString());
                currentViewedBike = builder.build();
            }
            else{
                AlertBox.errorMessage("Edit Error", "Could not edit type");
            }
        }
    }

    /**
     * Method for editing the brand for the currently viewed bike.
     */
    public void editBrand(){
        editBrandBox.setVisible(!editBrandBox.isVisible());
        if(editBrandBox.isVisible()){
            editBrandButton.setText("Save");
            editBrandBox.setText(currentViewedBike.getMake());
        }
        else{
            editBrandButton.setText("Edit");

            if(editBrandBox.getText().trim().equals("") || editBrandBox.getText() == null){
                return;
            }
            // Save changes.
            Bike.Builder builder = new Bike.Builder().from(currentViewedBike);
            builder.make(editBrandBox.getText());
            boolean status = BikeDao.getInstance().update(builder.build());
            if(status){
                brandLabel.setText(editBrandBox.getText());
                currentViewedBike = builder.build();
                editBrandBox.clear();
            }
            else{
                AlertBox.errorMessage("Edit Error", "Could not edit brand.");
            }
        }
    }

    /**
     * Class for taking the bikehistory data and putting them in the bikehistory tableview.
     */
    public class BikeHistoryRow extends Bike{

        private double posLat = 0;
        private double posLong = 0;

        public BikeHistoryRow(Bike history){
            super(history.getId(), history.getType(), history.getMake(), history.getPrice(), history.getDatePurchased(),
                    history.getActiveStatus(), history.getUserId(), history.getStationId(), history.getTotalTrips(),
                    history.getStatusTimestamp(), history.getTotalKilometers(), history.getPosition(), history.getChargeLevel());
            posLat = history.getPosition().getLat();
            posLong = history.getPosition().getLng();
        }

        public double getPosLat() {
            return posLat;
        }

        public double getPosLong() {
            return posLong;
        }
    }
}