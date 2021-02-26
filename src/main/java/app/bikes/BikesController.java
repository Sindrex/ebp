package app.bikes;

import app.AlertBox;
import app.Inputs;
import app.ViewControllerSelected;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import app.map.GoogleMapMarker;
import app.map.GoogleMapView;
import db.dao.TypeDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import logging.Logger;
import managers.BikeManager;
import objects.Bike;
import objects.BikeType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static objects.Bike.ActiveStatus.DOCKED;

 /**
 * Controller class for the bike view.
 *
 * @author Sindre Paulshus
 * @author Aleksander Johansen
 * @author Joergen Bele Reinfjell
 * @author Odd-Erik Frantzen
 */
public class BikesController implements ViewControllerSelected {
    @FXML private BikeDetailsController bikeDetailsController;

    @FXML private Button regButton;

    @FXML private TextField priceField;
    @FXML private TextField brandField;
    @FXML private TextField dockingIdField;

    @FXML private DatePicker bikeDate;

    @FXML private GoogleMapView overviewMapView;
    @FXML private HBox bikesPane;
    @FXML private HBox detailPage;
    @FXML private HBox typesPane;

    @FXML private GridPane detailGridPane1;

    @FXML private BorderPane mainPage;

    @FXML private PaneSwitchButton overviewTabButton;
    @FXML private PaneSwitchButton bikesTabButton;
    @FXML private PaneSwitchButton typeTabButton;

    @FXML private TableView<BikeTableRow> tableViewBikes;


    @FXML private ComboBox<String> comboBox;
    @FXML private Label errorBikes;

    @FXML private TableView<BikeType> tableViewTypes;
    @FXML private Button addTypeButton;
    @FXML private TextField typeNameField;
    @FXML private Label errorType;

    private Thread updateThread = null;

    private ArrayList<String> text = new ArrayList<>();
    private ObservableList<String> list;

    private static TypeDao td = TypeDao.getInstance();
    private static BikeManager bm = new BikeManager();
    private PaneSwitchButtonGroup paneSwitchButtonGroup = new PaneSwitchButtonGroup();
    private final int THREAD_UPDATE_PERIOD = 2;
    private final int detailsHistoryCount = 100;

    // XXX: Change to BikesManager? or change BikesController to BikeController?

    private final AtomicBoolean updaterRunning = new AtomicBoolean(false);
    private ScheduledService<Void> updateService = null;

    private static final String BIKE_CHARGE_EMPTY_ICON_PATH = "http://17.pages.stud.iie.ntnu.no/ebp/bike_charge_empty.png";//"../map/bike_charge_empty.png";
    private static final String BIKE_CHARGE_DEPLETING_ICON_PATH = "http://17.pages.stud.iie.ntnu.no/ebp/bike_charge_depleting.png";//"../map/bike_charge_depleting.png";
    private static final String BIKE_CHARGE_FULL_ICON_PATH = "http://17.pages.stud.iie.ntnu.no/ebp/bike_charge_full.png";//"../map/bike_charge_full.png";


    /**
     * @param b the bike to generate the info window string for.
     * @return the info window string generated from the given bike.
     */
    private String createMarkerInfoWindowString(Bike b) {
        return String.format(
                "<b>Bike %d</b><br>" +
                        "Type: %s, Make: %s<br>" +
                        "Charge level: %.0f<br>" +
                        "Total KM: %.1f<br>" +
                        "Total trips: %d<br>" +
                        "Position: %s<br>" +
                        "Last updated: %s<br>",
                b.getId(), b.getType(), b.getMake(),
                b.getChargeLevel(), b.getTotalKilometers(), b.getTotalTrips(),
                b.getPosition(), b.getStatusTimestamp());
    }

    private boolean update() {
        long startTime = System.currentTimeMillis();
        if (!bm.refresh()) {
            Logger.log("BikesController::update: bm.refresh(): failed!");
            return false;
        }

        //Update bike table
        List<Bike> bikeList = bm.getBikes();
        Platform.runLater(() -> {
            // XXX - has to happen in the JavaFX thread.
            tableViewBikes.getItems().clear();
            if (bikeList != null) {
                for (Bike b : bikeList) {
                    tableViewBikes.getItems().add(BikeTableRow.from(b, getTableCommunicator()));
                }
            }
        });

        // Update comboBox choices only when no element is selected.
        if (comboBox.getSelectionModel().getSelectedItem() == null){
            List<BikeType> typeList = td.getAll();
            Platform.runLater(() -> {
                // XXX - has to happen in the JavaFX thread.
                comboBox.getItems().clear(); // XXX - this also clears the currently selected item.
                comboBox.getItems().add(null);

                if (typeList != null) {
                    for (BikeType t : typeList) {
                        comboBox.getItems().add(t.getName());
                    }
                    //Update type-view
                    tableViewTypes.getItems().clear();

                    for (BikeType t : typeList) {
                        if(t.getActive()){
                            tableViewTypes.getItems().add(BikeTypeTableRow.from(t));
                        }
                    }
                }
            });
        }

        //Mapstuff
        if (overviewMapView.isLoaded()) {
            List<GoogleMapMarker> updatedMarkers = bikeList.stream().map(b -> {
                // Since the map can only know when a marker has changed if it is given it, all marker deletions
                // must be represented by setting the marker to invisible for now.
                boolean visible = b.getActiveStatus() == Bike.ActiveStatus.RENTED;

                GoogleMapMarker.Builder builder = new GoogleMapMarker.Builder()
                        .position(b.getPosition())
                        .id(b.getId())
                        .label(String.valueOf(b.getId()))
                        .info(createMarkerInfoWindowString(b))
                        .visible(visible);

                if (b.getChargeLevel() < 20) {
                    builder.iconPath(BIKE_CHARGE_EMPTY_ICON_PATH);
                } else if (b.getChargeLevel() < 80) {
                    builder.iconPath(BIKE_CHARGE_DEPLETING_ICON_PATH);
                } else {
                    builder.iconPath(BIKE_CHARGE_FULL_ICON_PATH);
                }
                return builder.build();
            }).collect(Collectors.toList());


            Platform.runLater(() -> {
                overviewMapView.updateAllMarkers(updatedMarkers);
                Logger.logf("Bikes: Updated %s map markers.", updatedMarkers.size());
            });
        }

        Logger.logf("Bikes: update took %d ms", System.currentTimeMillis() - startTime);

        return true;
    }

    @FXML
    /**
     * Initialize method. It runs once when the BikeView fxml documents loads.
     */
    public void initialize() {
        Logger.log("BikesController() initialize called!");
        long startTime = System.currentTimeMillis();
        bikeDetailsController.setCommunicator(getTableCommunicator());

        initializeUI();
        initializeTables();
        initializeData();
        initializeMaps();
        Logger.logf("BikesController() initialize took: %d ms", System.currentTimeMillis() - startTime);
    }

    private void initializeData() {
        // Task for updating the list of bikes and biketypes.
        updateService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Logger.log("Bikes & BikeTypes: update task called!");
                        update();
                        return null;
                    }
                };
            }
        };
        updateService.start();
        updateService.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD));
        updaterRunning.set(true);
    }

    //Sets up onAction for buttons, textfields, etc.
    private void initializeUI() {
        paneSwitchButtonGroup.addView(overviewTabButton, overviewMapView);
        paneSwitchButtonGroup.addView(bikesTabButton, bikesPane);
        paneSwitchButtonGroup.addView(typeTabButton, typesPane);
        paneSwitchButtonGroup.setSelectedPanelByButton(overviewTabButton);

        regButton.setOnAction(e -> addBike());
    }

    /**
     * Initializes the tableviews, by telling the columns which object it shall hold and what datatype.
     */
    private void initializeTables() {
        //Initialize table-cell values property for bikes.

        // BikeID column
        String[] values = {"Id", "chargeLevel", "activeStatus", "detailsButton"};
        TableColumn<BikeTableRow, Integer> idCol = (TableColumn<BikeTableRow, Integer>) tableViewBikes.getColumns().get(0);
        idCol.setCellValueFactory(new PropertyValueFactory<BikeTableRow, Integer>(values[0]));

        // Charge level column
        TableColumn<BikeTableRow, Double> chargeCol = (TableColumn<BikeTableRow, Double>) tableViewBikes.getColumns().get(1);
        chargeCol.setCellValueFactory(new PropertyValueFactory<BikeTableRow, Double>(values[1]));

        // Activity column
        TableColumn<BikeTableRow, String> activityCol = (TableColumn<BikeTableRow, String>) tableViewBikes.getColumns().get(2);
        activityCol.setCellValueFactory(new PropertyValueFactory<BikeTableRow, String>(values[2]));

        // Detail column
        TableColumn<BikeTableRow, Button> detailCol = (TableColumn<BikeTableRow, Button>) tableViewBikes.getColumns().get(3);
        detailCol.setCellValueFactory(new PropertyValueFactory<BikeTableRow, Button>(values[3]));
        
        List<Bike> bikeList = bm.getBikes();
        if(bikeList != null){
            for(Bike b : bikeList){
                tableViewBikes.getItems().add(BikeTableRow.from(b, getTableCommunicator()));
            }
        }

        //Initialize type-table-cell values property
        String[] variables = new String[]{"name", "timestamp", "bikeCount", "delete"};
        for (int i = 0; i < tableViewTypes.getColumns().size(); i++) {
            TableColumn<BikeType, String> col = (TableColumn<BikeType, String>) tableViewTypes.getColumns().get(i);
            col.setCellValueFactory(new PropertyValueFactory<BikeType, String>(variables[i]));
        }

        TableColumn<BikeType, Button> deleteCol = (TableColumn<BikeType, Button>)tableViewTypes.getColumns().get(3);
        deleteCol.setCellValueFactory(new PropertyValueFactory<>(variables[3]));
    }

    //Starts update and map threads
    private void initializeMaps() {

        /*
         * Map.
         */
        overviewMapView.setOnMapInitCallback((x) -> {
            Logger.log("overviewMapView initialized!");

            Platform.runLater(() -> overviewMapView.setMapZoom(12));

            //update();
            if (!updateService.isRunning()) {
                update();
            }
            return null;
        });

        overviewMapView.setOnClick((p) -> {
            Logger.logf("overviewMapView: clicked at: %s", p);
            return null;
        });

        overviewMapView.setOnMarkerClickCallback((id) -> {
            Logger.logf("overviewMapView: marker %d clicked", id);
            return null;
        });

        Platform.runLater(overviewMapView::load);
    }

    public void addTypeData() {
        if (checkFieldsType()) {
            BikeType type = new BikeType(typeNameField.getText());
            if (td.add(type)) {
                tableViewTypes.getItems().add(type);
                addTypeComboBox(type.getName());
                typeNameField.clear();
            } else {
               // setInfoMessageType("Error!", true);
            }
        } else {
            AlertBox.errorMessage("Type Error", "Could not add type.");
        }
    }

    /**
     * Adds a new bike by checking if all the fields are filled in, and then creates a new Bike object that
     * is added to the database.
     */
    public void addBike() {
        if (checkBikeFields() && testBikeParsing()) {
            String date = String.valueOf(bikeDate.getValue());
            double price = Double.parseDouble(priceField.getText());
            String brand = brandField.getText();
            String type = comboBox.getSelectionModel().getSelectedItem();
           // int dockingStation = Integer.parseInt(dockingStationField.getText());
            int dockingStation = Integer.parseInt(dockingIdField.getText());
            // Use the data above to store in the database.

            Bike bike = new Bike(bm.getMaxId() + 1, type, brand, price, date, DOCKED, dockingStation);
            if (bm.addBike(bike)) {
                tableViewBikes.getItems().add(BikeTableRow.from(bike, getTableCommunicator()));
                clearBikeFields();
                AlertBox.successMessage("Bike", "Bike added successfully");
              //  setInfoMessage("Bike is added!", false);
            } else {
                AlertBox.errorMessage("Bike Error", "Could not add bike");
            }
        }
        else{
            if(!checkBikeFields()){
                AlertBox.errorMessage("Missing Fields", "You must fill in all the fields.");
            }
            else{
                AlertBox.errorMessage("Integer Parse Error!", "Docking Station must be an integer!");
            }
        }
    }

    /**
     * Method for adding a new bike type to the combobox.
     * @param type The name of the bike type.
     */
    public void addTypeComboBox(String type) {
        text.add(type);
        list = FXCollections.observableArrayList(text);
        comboBox.setItems(list);
    }

    /**
     * Clears the fields in the register bike section.
     */
    private void clearBikeFields() {
        bikeDate.getEditor().clear();
        priceField.clear();
        brandField.clear();
        comboBox.getSelectionModel().clearSelection();
        dockingIdField.clear();
    }

    /**
     * Checks that all the fields in the add bikes section are filled.
     * @return true of false depending on whether or not all fields have been filled.
     */
    private boolean checkBikeFields() {
        if (bikeDate.getValue() != null && !priceField.getText().equals("") && !brandField.getText().equals("") && comboBox.getSelectionModel() != null &&
                !dockingIdField.getText().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Used to check that the fields that takes a number as an argument, really is a number and not a string.
     * @return true or false. If the price can't be parsed to a double, it returns false, else it returns true.
     */
    private boolean testBikeParsing() {
        if (!priceField.getText().equals("") && !dockingIdField.getText().equals("")) {
            try {
                double price = Double.parseDouble(priceField.getText());
                if(price < 0){
                    AlertBox.errorMessage("Bike Error", "Price must be positive");
                    return false;
                }
                //int dockingStation = Integer.parseInt(dockingStationField.getText());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        AlertBox.errorMessage("Bike Error", "Illegal arguments in fields");
        return false;
    }

    /**
     * Checks that the field is not null or empty string
     * @return true or false.
     */
    private boolean checkFieldsType() {
        if (typeNameField.getText() != null && !typeNameField.getText().trim().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Used for choosing the dock id when registering a bike. A list with available docking ids pop up and this method
     * sets the value of the dockingIdField to the id of what is chosen.
     */
   public void chooseDockingId(){
       int dockId = Inputs.activeDockingList();
       dockingIdField.setText(dockId <= 0 ? "" : String.valueOf(dockId));
   }

    /**
     * A communicator. It is used so that other classes can communicate with the BikesController.
     * Primarily used with Objects for the tableview and the BikesDetailsController so that they
     * can call upon methods in the BikesController.
     * @return Returns an interface the other classes can use for communication.
     */
    protected TableCommunicator getTableCommunicator() {
        return new TableCommunicator() {
            @Override
            public void openBikeDetails() {
                mainPage.setVisible(false);
                bikeDetailsController.hidePage(false);
            }

            public void closeBikeDetails() {
                mainPage.setVisible(true);
                bikeDetailsController.hidePage(true);
            }

            public void updateDetails(Bike bike) {
                bikeDetailsController.currentViewedBike = bike;
                bikeDetailsController.updateDetails(bike, bm.getBikeHistories(bike.getId(), detailsHistoryCount)); // TODO: 4/17/18 Fix w respect to bike history
            }

            @Override
            public void addType(String type) {
                // TODO: 4/17/18 what?
            }

            /*
            public void addType(String type) {
                addType(type);
            }*/
        };
    }

    @Override
    public void onViewSelected() {
        if (!updaterRunning.getAndSet(true)) {
            updateService = new ScheduledService<Void>() {
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        protected Void call() {
                            Logger.log("updater: Bikes & BikeTypes: update task called!");
                            update();
                            return null;
                        }
                    };
                }
            };
            updateService.start();
            updateService.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD));
            updaterRunning.set(true);
        }
        // Only run the updateService when the view is selected.
        Logger.log("Bikes view selected!");

        /*
        if (updateService.getState() == Worker.State.CANCELLED) {
            updateService.restart();
        } else if (updateService.getState() == Worker.State.READY) {
            updateService.start();
            updateService.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD));
        }
        */
    }

    @Override
    public void onViewUnselected() {
        updaterRunning.set(false);
        updateService.cancel();

        /*
        Logger.log("Bikes view unselected!");
        if (updateService.isRunning()) {
            updateService.cancel();
        }
        */
    }

    /**
     * The communicator which is given to the other classes, so that they can call upon methods in the BikesController.
     */
    public interface TableCommunicator {
        void openBikeDetails();

        void closeBikeDetails();

        void updateDetails(Bike bike);

        void addType(String type);
    }
}