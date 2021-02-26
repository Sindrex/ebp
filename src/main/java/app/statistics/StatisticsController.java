package app.statistics;

import app.ViewControllerSelected;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import db.dao.TypeDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import logging.Logger;
import managers.BikeManager;
import managers.DockingStationManager;
import managers.RepairManager;
import managers.RepairTableRow;
import objects.Bike;
import objects.BikeType;
import objects.DockingStation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller class for the statistics view.
 *
 * @author Sindre Paulshus
 * @author Aleksander Johansen
 * @author Odd-Erik Frantzen
 * @author Joergen Bele Reinfjell
 */
public class StatisticsController implements ViewControllerSelected {

    @FXML LineChart<XYChart.Data, XYChart.Data> totalTripsGraph;
    @FXML LineChart<XYChart.Data, XYChart.Data> kmPerTripGraph;

    @FXML LineChart<XYChart.Data, XYChart.Data> totalRepairsGraph;
    @FXML LineChart<XYChart.Data, XYChart.Data> repairsPerBikeGraph;

    @FXML LineChart<XYChart.Data, XYChart.Data> dockPowerUsageGraph;
    @FXML LineChart<XYChart.Data, XYChart.Data> dockAveragePowerUsageGraph;

    @FXML LineChart<XYChart.Data, XYChart.Data> bikeCostGraph;
    @FXML LineChart<XYChart.Data, XYChart.Data> repairCostGraph;

    private final int graphsCount = 8;

    @FXML private PaneSwitchButton bikesTabButton;
    @FXML private PaneSwitchButton docksTabButton;
    @FXML private PaneSwitchButton repairTabButton;
    @FXML private PaneSwitchButton economyTabButton;

    @FXML private HBox bikesBox;
    @FXML private HBox repairsBox;
    @FXML private HBox dockingStationBox;
    @FXML private HBox economyBox;

    @FXML private ComboBox bikeTypes;
    private String currentType = "All";

    @FXML private ComboBox year;
    private int selectedYear = 2018;

    @FXML private ObservableList<String> typeList;
    @FXML private ObservableList<Integer> yearList;

    @FXML private AnchorPane paneHolder;
    private PaneSwitchButtonGroup paneSwitchButtonGroup = new PaneSwitchButtonGroup();

    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private BikeManager bikeManager = new BikeManager();
    private DockingStationManager dockingStationManager = new DockingStationManager();
    private TypeDao typeDao = TypeDao.getInstance();
    private RepairManager repairManager = new RepairManager();

    private List<Integer> nrTrips = new ArrayList<>();
    private List<Double> kmPerTrip = new ArrayList<>();
    private List<Double> bikePurchaseCost = new ArrayList<>();
    private List<Integer> totalRepairs = new ArrayList<>();
    private List<Double> repairsPerBike = new ArrayList<>();
    private List<Double> repairCost = new ArrayList<>();
    private List<Double> docksPowerUsage = new ArrayList<>();
    private List<Double> docksAveragePowerUsage = new ArrayList<>();

    private final int THREAD_UPDATE_PERIOD = 2;
    private final int THREAD_UPDATE_PERIOD_BIKE = 1;
    private int POINTS = 12; //eg. months to show
    private int HISTORY_COUNT = 10; //Must be more than 2 (per bike)

    private final AtomicBoolean updaterRunning = new AtomicBoolean(false);
    private final AtomicBoolean updaterRunningBike = new AtomicBoolean(false);
    private ScheduledService updateService = null;
    private ScheduledService updateServiceBike = null;

    private enum graphs{
        ALL, TOTAL_TRIPS, KM_PER_TRIP, BIKE_COST, TOTAL_REPAIRS,
        REPAIRS_PER_BIKE,REPAIR_COST, DOCK_POWER_USAGE, DOCK_AVG_POWER_USAGE
    }

    /**
     * Method that runs once every time the document is loaded.
     */
    @FXML
    public void initialize() {
        Logger.log("StatisticsController() initialize called!");
        paneSwitchButtonGroup.addView(bikesTabButton, bikesBox);
        paneSwitchButtonGroup.addView(repairTabButton, repairsBox);
        paneSwitchButtonGroup.addView(docksTabButton, dockingStationBox);
        paneSwitchButtonGroup.addView(economyTabButton, economyBox);
        paneSwitchButtonGroup.setSelectedPanelByButton(bikesTabButton);

        bikesTabButton.setOnAction(e -> updateGraphs());
        repairTabButton.setOnAction(e -> updateGraphs());
        docksTabButton.setOnAction(e -> updateGraphs());
        economyTabButton.setOnAction(e -> updateGraphs());

        //BikeType combobox and selection
        bikeTypes.setOnAction(e -> {
            selectType();
            clearGraphs(graphs.ALL);
            updateGraphs();
        });

        List<String> nameList = new ArrayList<>();
        List<BikeType> bList = TypeDao.getInstance().getAll();
        nameList.add("All");
        currentType = "All";
        for(BikeType b : bList){
            nameList.add(b.getName());
        }

        typeList = FXCollections.observableArrayList(nameList);
        //bikeTypes.setItems(FXCollections.observableArrayList(typeDao.getAll()));
        bikeTypes.setItems(typeList);
        bikeTypes.setPromptText("Bike Type");

        //Year combobox and selection
        List<Integer> boxInput = new ArrayList<>();
        boxInput.add(2018);
        boxInput.add(2017);
        yearList = FXCollections.observableArrayList(boxInput);
        year.setItems(yearList);
        year.setPromptText("Year");

        year.setOnAction(e -> {
            selectYear();
            clearGraphs(graphs.ALL);
            updateGraphs();
        });

        initializeStats();
        updateGraphs();
    }

    private void initializeStats(){
        //initialize lists
        for (int i = 0; i < POINTS; i++) {
            nrTrips.add(0);
            kmPerTrip.add(0.0);
            bikePurchaseCost.add(0.0);
            totalRepairs.add(0);
            repairsPerBike.add(0.0);
            repairCost.add(0.0);
            docksAveragePowerUsage.add(0.0);
            docksPowerUsage.add(0.0);
        }

        // Task for updating statistics-lists
        updateService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Logger.log("Statistics: update task called!");
                        update();
                        return null;
                    }
                };
            }
        };
        updateService.start();
        updateService.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD));
        updaterRunning.set(true);

        // Task for adding the stats to bike (heavy)
        updateServiceBike = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Logger.log("Statistics: update bikeStats task called!");
                        updateBikeStats();
                        return null;
                    }
                };
            }
        };
        updateServiceBike.start();
        updateServiceBike.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD_BIKE));
        updaterRunningBike.set(true);
    }

    private void updateBikeStats(){
        Logger.log("Statistics: updateBikeStats() started!");
        List<Bike> bikeList = bikeManager.getBikes(currentType);

        List<Integer> nrTripsTemp = new ArrayList<>();
        List<Double> kmPerTripTemp = new ArrayList<>();

        for (int i = 0; i < POINTS; i++) {
            nrTripsTemp.add(0);
            kmPerTripTemp.add(0.0);
        }

        for(int i = 0; i < bikeList.size()/6; i++){
            String[] dateSplit = bikeList.get(i).getDatePurchased().split("-");

            //Logger.log("Statistics: updateBikeStats() bike: " + bikeList.get(i).getId());

            for(int j = 0; j < POINTS; j++){
                //TotalTrips
                int trips = bikeManager.trips(bikeList.get(i).getId(), selectedYear, j, HISTORY_COUNT);
                nrTripsTemp.set(j, trips + nrTripsTemp.get(j));
                if(trips > 0){
                    //Logger.log("Statistics: updateBikeStats() trips: " + trips);
                }

                //KM/Trip
                double average = bikeManager.kmPerTrip(bikeList.get(i).getId(), selectedYear, j, HISTORY_COUNT);
                kmPerTripTemp.set(j, average + kmPerTripTemp.get(j));
                if(average > 0){
                    //Logger.log("Statistics: updateBikeStats() average: " + average);
                }
            }
        }

        for(int k = 0; k < kmPerTripTemp.size(); k++){
            //Logger.log("Statistics: " + kmPerTripTemp.get(k) + "/" + bikeList.size());
            kmPerTripTemp.set(k, kmPerTripTemp.get(k)/bikeList.size());
        }

        nrTrips = nrTripsTemp;
        kmPerTrip = kmPerTripTemp;
        /*
        //Logger.log("nrTripsTemp.size(): " + nrTripsTemp.size());
        //Logger.log("kmPerTripTemp.size(): " + kmPerTripTemp.size());
        for(int i = 0; i < nrTripsTemp.size(); i++){
            Logger.log("nrTripsTemp.get(i): " + nrTripsTemp.get(i));
            Logger.log("kmPerTripTemp.get(i): " + kmPerTripTemp.get(i));
        }*/

        Logger.log("Statistics: updateBikeStats() finished!");
    }

    private void update(){
        //Logger.log("Statistics: Update started!");

        List<Bike> bikeList = bikeManager.getBikes();
        List<DockingStation> dockList = dockingStationManager.getDocks();
        ObservableList<RepairTableRow> repairList = null;

        try{
            repairManager.refresh();
            repairList = repairManager.getRepairs();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        List<Double> bikePurchaseCostTemp = new ArrayList<>();
        List<Integer> totalRepairsTemp = new ArrayList<>();
        List<Double> repairsPerBikeTemp = new ArrayList<>();
        List<Double> repairCostTemp = new ArrayList<>();
        List<Double> docksPowerUsageTemp = new ArrayList<>();
        List<Double> docksAveragePowerUsageTemp = new ArrayList<>();

        //initialize lists
        for (int i = 0; i < POINTS; i++) {
            bikePurchaseCostTemp.add(0.0);
            totalRepairsTemp.add(0);
            repairsPerBikeTemp.add(0.0);
            repairCostTemp.add(0.0);
            docksAveragePowerUsageTemp.add(0.0);
            docksPowerUsageTemp.add(0.0);
        }

        //Bikes
        for(int i = 0; i < bikeList.size(); i++){
            String[] dateSplit = bikeList.get(i).getDatePurchased().split("-");
            int year = Integer.parseInt((dateSplit[0]));
            int month = Integer.parseInt(dateSplit[1]) - 1;

            if(currentType.equals("All") && year == selectedYear){
                //Bike Purchase cost
                double bikeCost = bikeList.get(i).getPrice();
                bikePurchaseCostTemp.set(month, bikeCost + bikePurchaseCostTemp.get(month));
            }
            else{
                if(bikeList.get(i).getType().equals(currentType) && year == selectedYear){
                    //Bike Purchase cost
                    double bikeCost = bikeList.get(i).getPrice();
                    bikePurchaseCostTemp.set(month, bikeCost + bikePurchaseCostTemp.get(month));
                }
            }
        }

        //Logger.log("Statistics: Update called (2)!");

        //repairs
        if(repairList != null){
            for(int i = 0; i < repairList.size(); i++){
                String[] dateRequestSplit = repairList.get(i).getRequestDate().split("-");
                String[] dateReturnSplit = repairList.get(i).getRequestDate().split("-");
                int yearReq = Integer.parseInt((dateRequestSplit[0]));
                int yearRet = Integer.parseInt((dateReturnSplit[0]));
                int monthReq = Integer.parseInt(dateRequestSplit[1]) - 1;
                int monthRet = Integer.parseInt(dateReturnSplit[1]) - 1;

                if(currentType.equals("All")){
                    if(yearReq == selectedYear){
                        //Total Repairs
                        totalRepairsTemp.set(monthReq, 1 + totalRepairsTemp.get(monthReq));
                    }
                    if(yearRet == selectedYear){
                        //RepairCost
                        double price = repairList.get(i).getPrice();
                        repairCostTemp.set(monthRet, price + repairCostTemp.get(monthRet));
                    }

                }
                else{
                    if(bikeList.get(i).getType().equals(currentType)){
                        if(yearReq == selectedYear){
                            //Total Repairs
                            totalRepairsTemp.set(monthReq, 1 + totalRepairsTemp.get(monthReq));
                        }
                        if(yearRet == selectedYear){
                            //RepairCost
                            double price = repairList.get(i).getPrice();
                            repairCostTemp.set(monthRet, price + repairCostTemp.get(monthRet));
                        }
                    }
                }
            }

            //Repairs per bike
            //TODO: Bike count at a place in time?
            if(currentType.equals("All")){
                double bikeCount = bikeList.size(); //Can be lossy since we do not know how many bikes there were at month X
                if(bikeCount > 0){
                    for(int k = 0; k < POINTS; k++) {
                        repairsPerBikeTemp.set(k, (totalRepairsTemp.get(k)/bikeCount));
                    }
                }
            }
            else{
                double bikeCountType = 0;
                for(int i = 0; i < bikeList.size(); i++){
                    if(bikeList.get(i).getType().equals(currentType)){
                        bikeCountType++;
                    }
                }
                if(bikeCountType > 0){
                    for(int k = 0; k < POINTS; k++) {
                        repairsPerBikeTemp.set(k, (totalRepairsTemp.get(k)/bikeCountType));
                    }
                }
            }
        }


        //Logger.log("Statistics: Update called (3)!");

        //Docks
        for(int i = 0; i < dockList.size(); i++){
            for(int j = 0; j < POINTS; j++){
                //Total power Usage
                double total = dockingStationManager.totalPowerUsage(dockList.get(i).getDockID(), selectedYear, j, HISTORY_COUNT);
                docksPowerUsageTemp.set(j, total + docksPowerUsageTemp.get(j));

                //Average power usage
                double average = dockingStationManager.averagePowerUsage(dockList.get(i).getDockID(), selectedYear, j, HISTORY_COUNT);
                docksAveragePowerUsageTemp.set(j, average + docksAveragePowerUsageTemp.get(j));
            }
        }

        bikePurchaseCost = bikePurchaseCostTemp;
        totalRepairs = totalRepairsTemp;
        repairsPerBike = repairsPerBikeTemp;
        repairCost = repairCostTemp;
        docksPowerUsage = docksPowerUsageTemp;
        docksAveragePowerUsage = docksAveragePowerUsageTemp;

        Logger.log("Statistics: Update stats finished!");
    }

    private void updateGraphs(){
        for(int i = 0; i < graphsCount; i++){
            if(i == graphsCount - 1){
                Logger.log("Statistics: Graphs updated!");
            }
            switch (i){
                case 0:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.TOTAL_TRIPS);
                        addBikeTripsData(selectedYear + "", nrTrips);
                    });
                    break;
                case 1:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.KM_PER_TRIP);
                        addKmPerTripData(selectedYear + "", kmPerTrip);
                    });
                    break;
                case 2:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.TOTAL_REPAIRS);
                        addTotalRepairsData(selectedYear + "", totalRepairs);
                    });
                    break;
                case 3:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.REPAIRS_PER_BIKE);
                        addRepairsPerBikeData(selectedYear + "", repairsPerBike);
                    });
                    break;
                case 4:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.DOCK_POWER_USAGE);
                        addDockPowerData(selectedYear + "", docksPowerUsage);
                    });
                    break;
                case 5:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.DOCK_AVG_POWER_USAGE);
                        addDockAveragePowerData(selectedYear + "", docksAveragePowerUsage);
                    });
                    break;
                case 6:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.REPAIR_COST);
                        addRepairCostData(selectedYear + "", repairCost);
                    });
                    break;
                case 7:
                    Platform.runLater(() -> {
                        clearGraphs(graphs.BIKE_COST);
                        addBikeCostData(selectedYear + "", bikePurchaseCost);
                    });
                    break;
            }
        }
    }

    private void clearGraphs(graphs mode){
        switch (mode){
            case ALL: //all
                totalTripsGraph.getData().clear();
                kmPerTripGraph.getData().clear();

                totalRepairsGraph.getData().clear();
                repairsPerBikeGraph.getData().clear();

                dockPowerUsageGraph.getData().clear();
                dockAveragePowerUsageGraph.getData().clear();

                repairCostGraph.getData().clear();
                bikeCostGraph.getData().clear();
                break;
            case TOTAL_TRIPS:
                totalTripsGraph.getData().clear();
                break;
            case KM_PER_TRIP:
                kmPerTripGraph.getData().clear();
                break;
            case TOTAL_REPAIRS:
                totalRepairsGraph.getData().clear();
                break;
            case REPAIRS_PER_BIKE:
                repairsPerBikeGraph.getData().clear();
                break;
            case DOCK_POWER_USAGE:
                dockPowerUsageGraph.getData().clear();
                break;
            case DOCK_AVG_POWER_USAGE:
                dockAveragePowerUsageGraph.getData().clear();
                break;
            case REPAIR_COST:
                repairCostGraph.getData().clear();
                break;
            case BIKE_COST:
                bikeCostGraph.getData().clear();
                break;
        }
    }

    private void selectType(){
        currentType = (String) bikeTypes.getSelectionModel().getSelectedItem();
    }

    private void selectYear(){
        selectedYear = (Integer) year.getSelectionModel().getSelectedItem();
    }

    private void addBikeTripsData(String name, List<Integer> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        totalTripsGraph.getData().add(series);
    }

    private void addKmPerTripData(String name, List<Double> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        kmPerTripGraph.getData().add(series);
    }

    private void addTotalRepairsData(String name, List<Integer> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
           // series.getData().add(new XYChart.Data(months[i], data.get(i)));
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(0, data.get(i)));
            series.getData().add(data2);
        }
        totalRepairsGraph.getData().add(series);
    }

    private void addRepairsPerBikeData(String name, List<Double> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        repairsPerBikeGraph.getData().add(series);
    }

    private void addDockPowerData(String name, List<Double> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        dockPowerUsageGraph.getData().add(series);
    }

    private void addDockAveragePowerData(String name, List<Double> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
         //   series.getData().add(new XYChart.Data(months[i], data.get(i)));
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        dockAveragePowerUsageGraph.getData().add(series);
    }

    private void addBikeCostData(String name, List<Double> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        bikeCostGraph.getData().add(series);
    }

    private void addRepairCostData(String name, List<Double> data){
        XYChart.Series series = new XYChart.Series();
        series.setName(name);
        for(int i = 0; i < POINTS; i++){
            XYChart.Data<Integer, Integer> data2 = new XYChart.Data(months[i], data.get(i));
            data2.setNode(new HoveredThresholdNode(
                    (i == 0)? 0 : data.get(i - 1), data.get(i)
            ));
            series.getData().add(data2);
        }
        repairCostGraph.getData().add(series);
    }

    @Override
    public void onViewSelected() {
        if (!updaterRunning.getAndSet(true)) {
            updateService = new ScheduledService<Void>() {
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        protected Void call() {
                            Logger.log("updater: Statistics: update task called!");
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

        // Task for adding the stats to bike (heavy)
        if (!updaterRunningBike.getAndSet(true)) {
            updateServiceBike = new ScheduledService<Void>() {
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        protected Void call() {
                            Logger.log("Statistics: update bikeStats task called!");
                            updateBikeStats();
                            return null;
                        }
                    };
                }
            };
            updateServiceBike.start();
            updateServiceBike.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD_BIKE));
            updaterRunningBike.set(true);
        }

        // Only run the updateService when the view is selected.
        Logger.log("Statistics view selected!");
    }

    @Override
    public void onViewUnselected() {
        updaterRunning.set(false);
        updateService.cancel();
    }


    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(double priorValue, double value) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(priorValue, value);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });
        }

        private Label createDataThresholdLabel(double priorValue, double value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
} // 700 Lines BOOOOOOOOI