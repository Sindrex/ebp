package app.deleted;

import app.ViewControllerSelected;
import objects.BikeType;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import app.Inputs;
import db.dao.TypeDao;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import logging.Logger;
import managers.BikeManager;
import managers.DockingStationManager;
import objects.Bike;
import objects.DockingStation;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for deleted view.
 *
 * @author Sindre Paulshus
 * @author Joergen Bele Reinfjell
 * @author Aleksander Johansen
 */
public class DeletedController implements ViewControllerSelected {

    @FXML private HBox bikesPane;
    @FXML private HBox typesPane;
    @FXML private HBox docksPane;

    @FXML private GridPane detailGridPane1;

    @FXML private BorderPane mainPage;

    @FXML private PaneSwitchButton bikesTabButton;
    @FXML private PaneSwitchButton docksTabButton;
    @FXML private PaneSwitchButton typesTabButton;

    @FXML private TableView tableViewBikes;
    @FXML private TableView tableViewDocks;
    @FXML private TableView tableViewTypes;

    private ObservableList<String> list;

    private static TypeDao typeDao = TypeDao.getInstance();
    private static BikeManager bikeManager = new BikeManager();
    private static DockingStationManager dockingStationManager = new DockingStationManager();

    private PaneSwitchButtonGroup paneSwitchButtonGroup = new PaneSwitchButtonGroup();
    private final int THREAD_UPDATE_PERIOD = 2;

    // XXX: Change to BikesManager? or change BikesController to BikeController?

    private ScheduledService updateService = null;

    private boolean update() {
        if (!bikeManager.refresh()) {
            Logger.log("DeletedController::update: bikeManager.refresh(): failed!");
            return false;
        }
        if(!dockingStationManager.refresh()){
            Logger.log("DeletedController::update: dockingStationManager.refresh(): failed!");
            return false;
        }

        //Update bike table
        List<Bike> bikeList = bikeManager.getDeletedBikes();
        tableViewBikes.getItems().clear();

        if(bikeList != null){
            for(Bike b : bikeList){
                tableViewBikes.getItems().add(DeletedBikeRow.from(b, getTableCommunicator()));
            }
        }

        //Update dock table
        List<DockingStation> dockList = dockingStationManager.getDeletedDocks();
        tableViewDocks.getItems().clear();

        if(dockList != null){
            for(DockingStation d : dockList){
                tableViewDocks.getItems().add(DeletedDockRow.from(d, getTableCommunicator()));
            }
        }

        //Update type table
        List<BikeType> typeList = typeDao.getAllDeleted();
        tableViewTypes.getItems().clear();

        if(typeList != null){
            List<BikeType> deletedList = new ArrayList<>();
            for(BikeType t : typeList){
                if(!t.getActive()){
                    deletedList.add(t);
                }
            }
            if(deletedList != null){
                for(BikeType t : deletedList){
                    tableViewTypes.getItems().add(DeletedTypeRow.from(t, getTableCommunicator()));
                }
            }
        }
        return true;
    }

    @FXML
    public void initialize(){
        initializeUI();

        initializeTables();

        startThreads();
    }

    //Sets up onAction for buttons, textfields, etc.
    private void initializeUI(){
        paneSwitchButtonGroup.addView(bikesTabButton, bikesPane);
        paneSwitchButtonGroup.addView(docksTabButton, docksPane);
        paneSwitchButtonGroup.addView(typesTabButton, typesPane);

        paneSwitchButtonGroup.setSelectedPanelByButton(bikesTabButton);

        bikesTabButton.setOnAction(e -> setBikesPane());
        docksTabButton.setOnAction(e -> setDocksPane());
        typesTabButton.setOnAction(e -> setTypesPane());
    }

    //Sets up bikes and type-tables
    private void initializeTables(){

        //Initialize table-cell values property for bikes.
        String[] values = new String[]{"Id", "make", "type", "datePurchased", "totalTrips", "totalKM", "restoreButton"};

        // BikeID column
        TableColumn<DeletedBikeRow, Integer> idCol = (TableColumn<DeletedBikeRow, Integer>)tableViewBikes.getColumns().get(0);
        idCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, Integer>(values[0]));

        // Make column
        TableColumn<DeletedBikeRow, Double> chargeCol = (TableColumn<DeletedBikeRow, Double>)tableViewBikes.getColumns().get(1);
        chargeCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, Double>(values[1]));

        // Type column
        TableColumn<DeletedBikeRow, String> activityCol = (TableColumn<DeletedBikeRow, String>)tableViewBikes.getColumns().get(2);
        activityCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, String>(values[2]));

        // Date Purchased column
        TableColumn<DeletedBikeRow, String> dateCol = (TableColumn<DeletedBikeRow, String>)tableViewBikes.getColumns().get(3);
        dateCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, String>(values[3]));

        // Detail column
        TableColumn<DeletedBikeRow, Integer> totalTripsCol = (TableColumn<DeletedBikeRow, Integer>)tableViewBikes.getColumns().get(4);
        totalTripsCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, Integer>(values[4]));

        TableColumn<DeletedBikeRow, Double> totalKMCol = (TableColumn<DeletedBikeRow, Double>)tableViewBikes.getColumns().get(5);
        totalKMCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, Double>(values[5]));

        // Restore column
        TableColumn<DeletedBikeRow, Button> restoreCol = (TableColumn<DeletedBikeRow, Button>)tableViewBikes.getColumns().get(6);
        restoreCol.setCellValueFactory(new PropertyValueFactory<DeletedBikeRow, Button>(values[6]));


        // Initializing table-cell values properties for docks.
        String[] variables2 = new String[]{"dockID", "maxBikes", "posLong","posLat", "restoreButton"};

        // DockID column
        TableColumn<DockingStation, Integer> dockIDCol = (TableColumn<DockingStation, Integer>)tableViewDocks.getColumns().get(0);
        dockIDCol.setCellValueFactory(new PropertyValueFactory<DockingStation, Integer>(variables2[0]));

        // details column
        TableColumn<DockingStation, Integer> capacityCol = (TableColumn<DockingStation, Integer>)tableViewDocks.getColumns().get(1);
        capacityCol.setCellValueFactory(new PropertyValueFactory<DockingStation, Integer>(variables2[1]));

        // posLong column
        TableColumn<DockingStation, Double> posLongCol = (TableColumn<DockingStation, Double>)tableViewDocks.getColumns().get(2);
        posLongCol.setCellValueFactory(new PropertyValueFactory<DockingStation, Double>(variables2[2]));

        // posLat column
        TableColumn<DockingStation, Double> posLatCol = (TableColumn<DockingStation, Double>)tableViewDocks.getColumns().get(3);
        posLatCol.setCellValueFactory(new PropertyValueFactory<DockingStation, Double>(variables2[3]));

        // restore column
        TableColumn<DockingStation, Button> restoreDockCol = (TableColumn<DockingStation, Button>)tableViewDocks.getColumns().get(4);
        restoreDockCol.setCellValueFactory(new PropertyValueFactory<DockingStation, Button>(variables2[4]));

        List<Bike> bikeList = bikeManager.getDeletedBikes();
        if(bikeList != null){
            for(Bike b : bikeList){
                tableViewBikes.getItems().add(DeletedBikeRow.from(b, getTableCommunicator()));
            }
        }

        //Initialize type-table-cell values property
        String[] variables = new String[]{"name","timestamp", "restoreButton"};
        for(int i = 0; i < tableViewTypes.getColumns().size(); i++){
            TableColumn<DeletedTypeRow,String> col = (TableColumn<DeletedTypeRow,String>)tableViewTypes.getColumns().get(i);
            col.setCellValueFactory(new PropertyValueFactory<DeletedTypeRow,String>(variables[i]));
        }
    }

    //Starts update and map threads
    private void startThreads(){
        // Task for updating the list of bikes and biketypes.
        updateService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Logger.log("Deleted: update task called!");
                        update();
                        return null;
                    }
                };
            }
        };
        updateService.start();
        updateService.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD));
    }

    // Used for toggling the bikes list pane.
    public void setBikesPane(){
        typesPane.setVisible(false);
        bikesPane.setVisible(true);
        docksPane.setVisible(false);
    }

    // Used for toggling the dock list pane.
    public void setDocksPane(){
        typesPane.setVisible(false);
        bikesPane.setVisible(false);
        docksPane.setVisible(true);
    }

    // Used for toggling the types list pane.
    public void setTypesPane(){
        typesPane.setVisible(true);
        bikesPane.setVisible(false);
        docksPane.setVisible(false);
    }

    @Override
    public void onViewSelected() {
        // Only restart if it was previously cancelled.
        Logger.log("Deleted view selected!");
        if (updateService.getState() == Worker.State.CANCELLED) {
            updateService.restart();
        }
    }

    @Override
    public void onViewUnselected() {
        Logger.log("Bikes view unselected!");
        if (updateService.isRunning()) {
            updateService.cancel();
        }
    }

    private TableCommunicator getTableCommunicator(){
        return new TableCommunicator() {
            @Override
            public void restoreBike(int id) {
                try{

                    // TODO: Popup choose stationID
                    bikeManager.refresh();
                    int stationId = Inputs.activeDockingList();
                    if(id == 0){
                        return;
                    }
                    if(bikeManager.restoreBike(id, stationId)){
                        //Feedback OK
                        Logger.log("RestoreBike OK: DeletedController, TableCommunicator, restoreBike(): @see BikeManager");
                    }
                    else{
                        //Feedback NOT OK
                        Logger.log("RestoreBike Failed: DeletedController, TableCommunicator, restoreBike(): @see BikeManager");
                    }
                }catch (Exception e){
                    Logger.log("Exception: DeletedController, TableCommunicator, restoreBike()");
                }
            }

            public void restoreDock(int id){
                if(dockingStationManager.restoreDock(id)){
                    //Feedback OK
                    Logger.log("RestoreBike Failed: DeletedController, TableCommunicator, restoreBike(): @see BikeManager");
                }
                else{
                    //Feedback NOT OK
                    Logger.log("Restore Failed: DeletedController, TableCommunicator, restoreBike(): @see BikeManager");
                }
            }

            public void restoreType(String name){
                BikeType bt = new BikeType(name);
                bt.setActive(true);
                if(typeDao.has(bt)){
                    if(typeDao.update(bt)){
                        //FeedBack OK
                    }
                    else{
                        //Feedback NOT OK
                        Logger.log("Restore Type failed: DeletedController, TableCommunicator, restoreType(): could not update DB");
                    }
                }
                else{
                    Logger.log("Restore Type failed: DeletedController, TableCommunicator, restoreType() type does not exist");
                }
            }
        };
    }

    public interface TableCommunicator{
        void restoreBike(int id);
        void restoreDock(int id);
        void restoreType(String name);
    }
}