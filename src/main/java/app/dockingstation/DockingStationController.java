package app.dockingstation;

import app.AlertBox;
import app.ViewControllerSelected;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import app.map.GoogleMapMarker;
import app.map.GoogleMapView;
import app.map.LatLng;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import logging.Logger;
import managers.DockingStationManager;
import objects.DockingStation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller class for the docking stations view.
 *
 * @author Joergen Bele Reinfjell
 * @author Aleksander Johansen
 * @author Sindre Paulshus
 * @author Odd-Erik Frantzen
 */
public class DockingStationController implements ViewControllerSelected {
    @FXML private DockingDetailsController dockingDetailsController;

    @FXML private TextField latitudeTextField;
    @FXML private TextField longitudeTextField;
    @FXML private Spinner<Integer> capacitySpinner;
    @FXML private Button addStationButton;

    @FXML private GoogleMapView addDockMapView;
    @FXML private GoogleMapView overviewMapView;
    //private GoogleMap overviewMap;

    @FXML private VBox statusPanel;
    @FXML private HBox addPanel;
    @FXML private HBox overViewMapPanel;

    @FXML private Insets test;
    @FXML private TableView<DockingTableRow> tableView;
    @FXML private PaneSwitchButton addButton;
    @FXML private PaneSwitchButton overViewMapButton;
    @FXML private PaneSwitchButton statusButton;

    @FXML private BorderPane mainPage;

    private final DockingStationManager manager = new DockingStationManager();
    private final PaneSwitchButtonGroup panelSwitchButtonGroup = new PaneSwitchButtonGroup();
    private Map<Integer, DockingStationObject> dockingStationObjectMap = new HashMap<>();

    private final static Duration UPDATE_DURATION = Duration.seconds(2);
    private ScheduledService<Void> updateService = null;

    private final static int MAX_CAPACITY = Integer.MAX_VALUE;
    private final static int MIN_CAPACITY = 1;
    private final static int DEFAULT_CAPACITY = 10;

    private final static int ADD_DOCKING_STATION_MARKER_ID = 0;
    private final static LatLng DEFAULT_ADD_MARKER_POSITION = new LatLng(63.430891973, 10.39479413);
    private static final String DOCKING_STATION_MARKER_ICON_PATH = "http://17.pages.stud.iie.ntnu.no/ebp/docking_station_icon.png";//"../map/docking_station_icon.png";

    /**
     * @param d the docking station to generate the info window string for.
     * @return the info window string generated from the given docking station.
     */
    private String createMarkerInfoWindowString(DockingStation d) {
        return String.format(
                // TODO POWER USAGE UNIT
            "<b>Docking station %d</b><br>" +
                "Power usage: %f (W)<br>" +
                "Docked bikes: %d<br>" +
                "Bike capacity: %d<br>" +
                "Position: %s<br>" +
                    "Last update: %s<br>",
                d.getDockID(), d.getPowerUsage(), d.getNumBikes(),
                d.getMAX_BIKES(), d.getPosition(), d.getStatusTimestamp());
    }

    /**
     * @param position the position for the add docking station marker.
     * @return the add docking station marker at the given position.
     */
    private GoogleMapMarker createAddMarker(LatLng position) {
        return new GoogleMapMarker.Builder()
                .position(position)
                .id(ADD_DOCKING_STATION_MARKER_ID)
                .iconPath(DOCKING_STATION_MARKER_ICON_PATH)
                .draggable(true)
                .info("Drag this marker to select the docking station's position.")
                .build();
    }

    /**
     * Updates the add docking station view, setting the position
     * text fields to the correct values, and moving/adding the marker.
     * @param newPosition the new position.
     */
    private void updateAddDockView(LatLng newPosition) {
        GoogleMapMarker marker = createAddMarker(newPosition);

        Platform.runLater(() -> {
            // Only want this one marker.
            addDockMapView.removeAllMarkers();
            addDockMapView.addMarker(marker);

            latitudeTextField.textProperty().set(String.valueOf(marker.getPosition().getLat()));
            longitudeTextField.textProperty().set(String.valueOf(marker.getPosition().getLng()));
        });
    }

    /**
     * Updates the docking station view.
     * @return true on success, false on failure.
     */
    private boolean update() {
        if (!manager.refresh()) {
            Logger.log("DockingStationController::update: manager.refresh(): failed");
            return false;
        }

        final List<DockingStation> dockingStationList = manager.getDocks();
        final ObservableList<DockingTableRow> dockingTableRows = FXCollections.observableArrayList();
        for (DockingStation e : dockingStationList) {
            dockingTableRows.add(DockingTableRow.from(e, getDockingCommunicator()));
        }
        tableView.setItems(dockingTableRows);
        if (overviewMapView.isLoaded()) {
            final List<GoogleMapMarker> updatedMarkers = dockingStationList.stream().map(d ->
                    // TODO: Add unit to power usage!
                    new GoogleMapMarker.Builder()
                            .position(d.getPosition())
                            .id(d.getDockID())
                            .iconPath(DOCKING_STATION_MARKER_ICON_PATH)
                            .label(String.valueOf(d.getDockID()))
                            .info(createMarkerInfoWindowString(d))
                            .build())
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                overviewMapView.updateAllMarkers(updatedMarkers);
                Logger.logf("Updated %s map markers.", updatedMarkers.size());
            });
        }

        final List<DockingStationObject> objectList = dockingStationList
                .stream()
                .map(d -> new DockingStationObject(d.getDockID(), 11, 10))
                //.map(d -> new DockingStationObject(d.getDockID(), d.getPresentPowerUsage(), d.getNumBikes()))
                .collect(Collectors.toList());

        Logger.logf("Updating DockingStationObject list: %d items.", objectList.size());

        objectList.forEach(o -> {
            Platform.runLater(() -> {
                DockingStationObject obj = dockingStationObjectMap.get(o.getId());
                if (obj != null) {
                    if (!o.equals(obj)) {
                        obj.setNumBikes(o.getNumBikes());
                        obj.setPresentPowerUsage(o.getPresentPowerUsage());
                        dockingStationObjectMap.replace(obj.getId(), obj);
                        Logger.logf("Updating docking station object in list: %d.", obj.getId());
                    }
                } else {
                    dockingStationObjectMap.put(o.getId(), o);
                    Logger.logf("Adding new docking station object to list: %d.", o.getId());
                }
            });
        });
        Logger.log("Done Updated docking stations!");

        return true;
    }

    public void initialize(){
        dockingDetailsController.setDockingCommunicator(getDockingCommunicator());

        /*
         * Overview map.
         */
        overviewMapView.setOnMapInitCallback((x) -> {
            Logger.log("DockingStation: loaded overview map!");
            //update();
            Logger.log("DockingStation: initialized overview map!");
            return null;
        });

        overviewMapView.setOnClick((p) -> {
            System.out.println("Map clicked at: " + p);
            //update();
            return null;
        });

        overviewMapView.setOnMarkerClickCallback((i) -> {
            System.out.println("Marker: " + i + " clicked!");
            return null;
        });
        Platform.runLater(overviewMapView::load);

        /*
         * Add dock map.
         */
        addDockMapView.setOnMapInitCallback((x) -> {
            updateAddDockView(DEFAULT_ADD_MARKER_POSITION);
            Platform.runLater(() -> addDockMapView.showMarkerInfoWindow(ADD_DOCKING_STATION_MARKER_ID));
            return null;
        });

        addDockMapView.setOnClick((LatLng position) -> {
            Logger.logf("addDockMapView clicked at: %s", position);
            updateAddDockView(position);
            return null;
        });

        addDockMapView.setOnMarkerClickCallback((id) -> {
            System.out.println("Marker: " + id + " clicked!");
            return null;
        });

        // Update text fields based upon marker position.
        addDockMapView.setOnMarkerDraggedCallback((id) -> {
            Platform.runLater(() -> {
                GoogleMapMarker m = addDockMapView.getMarkerById(id);
                if (m == null) {
                    Logger.errf("Unable to find marker by id: %d", id);
                    return;
                }
                addDockMapView.updateFromMap(m);
                m = addDockMapView.getMarkerById(id);
                if (m == null) {
                    Logger.errf("Unable to find marker by id: %d", id);
                    return;
                }

                // Update text fields.
                latitudeTextField.textProperty().set(String.valueOf(m.getPosition().getLat()));
                longitudeTextField.textProperty().set(String.valueOf(m.getPosition().getLng()));
            });
            return null;
        });
        Platform.runLater(addDockMapView::load);

        // Task for updating the list of docking stations++.
        updateService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Logger.log("DockingStation: update task called!");
                        update();
                        return null;
                    }
                };
            }
        };

        /*
         * Text fields.
         */
        // Move the marker to the position specified by the text fields.
        EventHandler<ActionEvent> positionTextFieldsChange = e -> {

            Platform.runLater(() -> {
                LatLng pos = new LatLng(Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText()));
                updateAddDockView(pos);
                //GoogleMapMarker marker = addDockMapView.getMarkerById(ADD_DOCKING_STATION_MARKER_ID);
                //addDockMapView.moveMarker(marker, pos);
                addDockMapView.panTo(pos);
            });
        };
        latitudeTextField.setOnAction(positionTextFieldsChange);
        longitudeTextField.setOnAction(positionTextFieldsChange);

        /*
         * Add button.
         */
        addStationButton.setOnAction(e -> {
            DockingStation dock = new DockingStation.Builder()
                    .maxBikes(capacitySpinner.getValue())
                    .position(new LatLng(Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText())))
                    .build();

            if (!manager.addDock(dock)) {
                Logger.errf("Failed to add dock!");
                AlertBox.errorMessage("Failure", "Failed to add new docking station!");
                return;
            }
            Logger.log("Added docking station!");
            AlertBox.successMessage("Success", "Added new docking station!");
        });

        /*
         * Button group.
         */
        panelSwitchButtonGroup.addView(addButton, addPanel);
        panelSwitchButtonGroup.addView(overViewMapButton, overViewMapPanel, button -> {
            Logger.log("overviewMapPanel setup called!");
            return null;
        }, button -> {
            System.out.println("overviewMapPanel destructor called!");
            return null;
        });
        panelSwitchButtonGroup.addView(statusButton, statusPanel);
        //panelSwitchButtonGroup.setSelectedPanelByButton(addButton);
        panelSwitchButtonGroup.setSelectedPanelByButton(overViewMapButton);

        String[] values = new String[]{"dockID", "powerUsage", "bikeAmount", "MAX_BIKES", "detailsButton"};
        TableColumn<DockingTableRow, Integer> idCol = (TableColumn<DockingTableRow, Integer>)tableView.getColumns().get(0);
        idCol.setCellValueFactory(new PropertyValueFactory<DockingTableRow, Integer>(values[0]));

        // Power usage column
        TableColumn<DockingTableRow, Double> presentPowerUsageCol = (TableColumn<DockingTableRow, Double>)tableView.getColumns().get(1);
        presentPowerUsageCol.setCellValueFactory(new PropertyValueFactory<DockingTableRow, Double>(values[1]));

        // Nr of bikes column
        TableColumn<DockingTableRow, String> numBikesCol = (TableColumn<DockingTableRow, String>)tableView.getColumns().get(2);
        numBikesCol.setCellValueFactory(new PropertyValueFactory<DockingTableRow, String>(values[2]));

        // Nr of bikes column
        TableColumn<DockingTableRow, Integer> maxBikesCol = (TableColumn<DockingTableRow, Integer>)tableView.getColumns().get(3);
        maxBikesCol.setCellValueFactory(new PropertyValueFactory<DockingTableRow, Integer>(values[3]));

        // Details column
        TableColumn<DockingTableRow, Button> detailCol = (TableColumn<DockingTableRow, Button>)tableView.getColumns().get(4);
        detailCol.setCellValueFactory(new PropertyValueFactory<DockingTableRow, Button>(values[4]));

        // Adds the docking stations to the tableview.

        update();
    }

    @Override
    public void onViewSelected() {
        // Only run the updateService when the view is selected.
        if (updateService.getState() == Worker.State.CANCELLED) {
            Logger.log("DockingStationController: restarting update service");
            updateService.restart();
        } else if (updateService.getState() == Worker.State.READY) {
            Logger.log("DockingStationController: updating service");
            updateService.start();
            updateService.setPeriod(UPDATE_DURATION);
        }
    }

    @Override
    public void onViewUnselected() {
        if (updateService.isRunning()) {
            updateService.cancel();
        }
    }

    /**
     * Method for giving other classes an interface, so that they can call upon methods in this class.
     * @return interface that has some methods.
     */
    private DockingCommunicator getDockingCommunicator(){
        return new DockingCommunicator() {
            @Override
            public void openDetails(){
                mainPage.setVisible(false);
                dockingDetailsController.hidePage(false);
            }

            @Deprecated
            public void updateDetails(DockingStation dock){
                dockingDetailsController.updateDetails(dock);
            }

            public void closeDetails() {
                mainPage.setVisible(true);
                dockingDetailsController.hidePage(true);
            }

            public boolean deleteDock(DockingStation dock){
                if(manager.deleteDock(dock.getDockID())){
                    AlertBox.successMessage("Worked", "Deletion complete.");
                    update();
                    return true;

                }
                else{
                    AlertBox.errorMessage("Error", "Could not delete docking station");
                    update();
                    return false;
                }

            }
        };
    }

    /**
     * Communicator used for communicating between this class and DockingDetailsController.
     */
    public interface DockingCommunicator{
        void openDetails();
        void closeDetails();
        void updateDetails(DockingStation dock);
        boolean deleteDock(DockingStation dock);
    }
}