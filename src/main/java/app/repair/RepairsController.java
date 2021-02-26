package app.repair;

import app.AlertBox;
import app.Inputs;
import app.ViewControllerSelected;
import app.elements.PaneSwitchButton;
import app.elements.PaneSwitchButtonGroup;
import db.dao.RepairDao;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import logging.Logger;
import managers.RepairManager;
import managers.RepairTableRow;
import objects.Repair;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controls the repair tab
 * Repair allows you to add, view and update Repairs
 *
 * @author Odd-Erik Frantzen
 * @author Sindre Paulshus
 * @author Aleksander Johansen
 */
public class RepairsController implements ViewControllerSelected {

    private static RepairManager rm = new RepairManager();
    private final int THREAD_UPDATE_PERIOD = 10;//s

    @FXML private Button backButton;
    @FXML private Button regRepairButton;
    @FXML private Button retReturnButton;
    @FXML private Button retRegisterButton;
    @FXML private Button chooseIdButton;
    @FXML private DatePicker requestField;
    @FXML private TextField reqDescriptionField;
    @FXML private TextField priceRepairField;
    @FXML private TextField bikeIdField;
    @FXML private TextField repairIdField;
    @FXML private DatePicker requestFieldEdit;
    @FXML private TextField reqDescriptionFieldEdit;
    @FXML private TextField bikeIdFieldEdit;
    @FXML private TextField retDescriptionField;
    @FXML private DatePicker returnField;
    @FXML private HBox repairsPane;
    @FXML private HBox allRepairs;
    @FXML private HBox editRepairs;
    @FXML private Label errorRepairs;
    @FXML private TableView<RepairTableRow> repairView;
    private ScheduledService updateService;

    @FXML private PaneSwitchButton addRepairTabButton;
    @FXML private PaneSwitchButton allRepairsTabButton;
    private PaneSwitchButtonGroup paneSwitchButtonGroup = new PaneSwitchButtonGroup();

    @FXML
    public void initialize() {
        initializeUI();
        initializeTables();
        updateService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Logger.log("Repair: update task called!");
                        loadRepairs();
                        return null;
                    }
                };
            }
        };
        updateService.start();
        updateService.setPeriod(Duration.seconds(THREAD_UPDATE_PERIOD));
        loadRepairs();
    }

    /**
     * Loads Repairs from manager
     */
    private void loadRepairs() {
        try {
            rm.refresh();
            repairView.setItems(rm.getRepairs());
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
    }

    //Sets up onAction for buttons, textfields, etc.

    /**
     * Sets up onaction for buttons, textfields, etc.
     * clears info message
     */
    private void initializeUI() {
        regRepairButton.setOnAction(e -> addRepair());

        backButton.setOnAction(e -> setBackButton());

        retRegisterButton.setOnAction(e -> editRepair());
        retReturnButton.setOnAction(e -> setAllRepairsPane());

        paneSwitchButtonGroup.addView(addRepairTabButton, repairsPane);
        paneSwitchButtonGroup.addView(allRepairsTabButton, allRepairs);
        paneSwitchButtonGroup.setSelectedPanelByButton(addRepairTabButton);

        addRepairTabButton.setOnAction(e -> {
            editRepairs.setVisible(false);
        });
        allRepairsTabButton.setOnAction(e -> {
            editRepairs.setVisible(false);
        });
    }

    /**
     * Checks fields and runs update via manager.
     */
    private void editRepair() {
        if (checkEditFields() && checkPriceField()) {
            int repairID = Integer.parseInt(repairIdField.getText());
            int bikeID = Integer.parseInt(bikeIdFieldEdit.getText());
            String requestDate = String.valueOf(requestFieldEdit.getValue());
            String requestDescription = reqDescriptionFieldEdit.getText();
            Repair rep;
            if (checkEditReturnFields()) {
                String returnDate = String.valueOf(returnField.getValue());
                Double price = Double.parseDouble(priceRepairField.getText());
                String description = retDescriptionField.getText();
                rep = new Repair(repairID, bikeID, requestDate, requestDescription,
                        returnDate, price, description);
            } else rep = new Repair(repairID, bikeID, requestDate, requestDescription);

            boolean success = false;
            try {
                success = rm.updateRepair(rep);
            } catch (Exception e) {
                if (e.getMessage().equals(RepairDao.INVALID_BIKE_ID_DESCRIPTION)) {
                    AlertBox.errorMessage("Existence error", "Bike ID does not exist");
                    return;
                } else {
                    Logger.log(e.getMessage());
                    success = false;
                }
            }
            if (success) {
                clearRepairFields();
                AlertBox.successMessage("Success!", "Repair updated!");
                return;
            }

            AlertBox.errorMessage("Add error", "Repair could not be added");
        } else {
            if(!checkEditFields()){
                AlertBox.errorMessage("Incorrect fields", "You must fill in all the fields correctly.");
            }
            else{
                AlertBox.errorMessage("Double Parse Error", "Price field is not a number. Try again.");
            }
        }
    }


    /**
     * Checks validity of request edit fields
     *
     * @return whether edit fields are valid
     */
    private boolean checkEditFields() {
        return requestFieldEdit.getValue() != null
                && bikeIdFieldEdit.getText().matches("\\d+")
                && repairIdField.getText().matches("\\d+")
                && !reqDescriptionFieldEdit.getText().equals("");
    }

    /**
     * Checks validity of return edit fields.
     *
     * @return whether return fields are valid. Used to determine if return data should be filled.
     */
    private boolean checkEditReturnFields() {
        return returnField.getValue() != null
                && repairIdField.getText().matches("\\d+")
                && !reqDescriptionFieldEdit.getText().equals("");
    }

    /**
     * Checks if the price field is an integer or not.
     * @return
     */
    private boolean checkPriceField(){
        try{
           Double.parseDouble(priceRepairField.getText());
            return true;
        }
        catch (NumberFormatException e){
            return false;
        }
    }

    /**
     * Gives a list of all bikeID's and let's the user pick one of these.
     */
    public void chooseId(){
        int bikeID = Inputs.allBikes();
        if(bikeID > 0){
            bikeIdField.setText(String.valueOf(bikeID));
        }
    }

    /**
     * Defines columns and value factories
     */
    private void initializeTables() {
        // Repair column
        TableColumn<RepairTableRow, Integer> repairCol = (TableColumn<RepairTableRow, Integer>) repairView.getColumns().get(0);
        repairCol.setCellValueFactory(cellData -> cellData.getValue().repairIdProperty().asObject());
        //as object because IntegerProperty implements ObservableValue<Number> and not <Integer>

        // Bike column
        TableColumn<RepairTableRow, Integer> bikeCol = (TableColumn<RepairTableRow, Integer>) repairView.getColumns().get(1);
        bikeCol.setCellValueFactory(cellData -> cellData.getValue().bikeIdProperty().asObject());

        // Request Date column
        TableColumn<RepairTableRow, String> reqCol = (TableColumn<RepairTableRow, String>) repairView.getColumns().get(2);
        reqCol.setCellValueFactory(cellData -> cellData.getValue().requestDateProperty());

        // Request Description column
        TableColumn<RepairTableRow, String> ReqDescriptionCol = (TableColumn<RepairTableRow, String>) repairView.getColumns().get(3);
        ReqDescriptionCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        // Return Date column
        TableColumn<RepairTableRow, String> retCol = (TableColumn<RepairTableRow, String>) repairView.getColumns().get(4);
        retCol.setCellValueFactory(cellData -> cellData.getValue().returnDateProperty());

        // Return Description column
        TableColumn<RepairTableRow, String> retDescriptionCol = (TableColumn<RepairTableRow, String>) repairView.getColumns().get(5);
        retDescriptionCol.setCellValueFactory(cellData -> cellData.getValue().returnDescriptionProperty());

        // Price column
        TableColumn<RepairTableRow, Double> priceCol = (TableColumn<RepairTableRow, Double>) repairView.getColumns().get(6);
        priceCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().returnIsValid())
                return cellData.getValue().priceProperty().asObject();
            else
                return null;
        });

        // Button column
        TableColumn<RepairTableRow, Button> buttonCol = (TableColumn<RepairTableRow, Button>) repairView.getColumns().get(7);
        buttonCol.setCellValueFactory(cellData -> {
            Button b = genButton(cellData.getValue());
            cellData.getValue().buttonProperty().set(b);
            return cellData.getValue().buttonProperty();
        });

    }

    /**
     * helper to generate an edit button for the {@link TableView}
     *
     * @param r the row to make a button for
     * @return the button
     */
    private Button genButton(RepairTableRow r) {
        Button b = new Button("Edit");
        b.setOnAction(e -> {
            setEditRepairs(r);
        });
        return b;
    }

    /**
     * Date formatter for {@link DatePicker}
     *
     * @param s the mysql date string
     * @return the LocalDate for {@link DatePicker}
     */
    private LocalDate date_formatted(String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(s, formatter);
    }

    /**
     * Fills fields for the actual Repair into the edit fields
     *
     * @param r the {@link RepairTableRow} to fetch data from
     */
    private void initializeEditPane(RepairTableRow r) {
        repairIdField.setText(Integer.toString(r.repairIdProperty().getValue()));
        requestFieldEdit.setValue(date_formatted(r.requestDateProperty().getValue()));
        reqDescriptionFieldEdit.setText(r.descriptionProperty().getValue());
        bikeIdFieldEdit.setText(Integer.toString(r.bikeIdProperty().getValue()));
        if (r.returnIsValid()) {
            returnField.setValue(date_formatted(r.returnDateProperty().getValue()));
            priceRepairField.setText(Double.toString(r.priceProperty().getValue()));
            retDescriptionField.setText(r.returnDescriptionProperty().getValue());
        } else {
            returnField.setValue(null);
            priceRepairField.setText("");
            retDescriptionField.setText("");
        }
    }

    /**
     * Switches to the edit page
     *
     * @param r {@link RepairTableRow} to use for initialization
     */
    private void setEditRepairs(RepairTableRow r) {
        editRepairs.setVisible(true);
        retReturnButton.setVisible(true);
        backButton.setVisible(false);
        repairsPane.setVisible(false);
        allRepairs.setVisible(false);
        retRegisterButton.setVisible(true);
        initializeEditPane(r);
    }

    /**
     * Switches to the All Repairs page
     */
    public void setAllRepairsPane() {
        allRepairs.setVisible(true);
        backButton.setVisible(false);
        repairsPane.setVisible(false);
        retReturnButton.setVisible(false);
        retRegisterButton.setVisible(false);
        editRepairs.setVisible(false);
    }

    /**
     * Switches back to the first page. (Add repair)
     */
    public void setBackButton() {
        backButton.setVisible(false);
        editRepairs.setVisible(false);
        repairsPane.setVisible(true);
        allRepairs.setVisible(false);
    }

    /**
     * Checks field and adds a repair through the {@link RepairManager}
     */
    public void addRepair() {
        if (checkRepairFields()) {
            int repairID = -1;
            int bikeID = Integer.valueOf(bikeIdField.getText());
            String requestDate = String.valueOf(requestField.getValue());
            String requestDescription = reqDescriptionField.getText();
            Repair rep = new Repair(repairID, bikeID, requestDate, requestDescription);

            boolean success = false;
            try {
                success = rm.addRepair(rep);
            } catch (Exception e) {
                if (e.getMessage() == RepairDao.INVALID_BIKE_ID_DESCRIPTION) {
                    AlertBox.errorMessage("Non-existing bike", "Bike ID does not exist");
                    return;

                } else {
                    Logger.log(e.getMessage());
                    success = false;
                }
            }
            if (success) {
                clearRepairFields();
                AlertBox.successMessage("Success", "Repair added!");
                return;
            }

            AlertBox.errorMessage("Add error", "Repair could not be added");
        } else {
            AlertBox.errorMessage("Incorrect fields", "You must fill in all the fields correctly.");
        }
    }

    /**
     * Cleans up all fields
     */
    private void clearRepairFields() {
        requestField.getEditor().clear();
        requestFieldEdit.getEditor().clear();
        reqDescriptionField.clear();
        reqDescriptionFieldEdit.clear();
        bikeIdField.clear();
        bikeIdFieldEdit.clear();
        repairIdField.clear();
        returnField.getEditor().clear();
        priceRepairField.clear();
        retDescriptionField.clear();
    }

    /**
     * Checks validity of addRepair fields
     *
     * @return whether they are valid
     */
    private boolean checkRepairFields() {
        return requestField.getValue() != null && bikeIdField.getText().matches("\\d+") && !reqDescriptionField.getText().equals("");
    }

    /**
     * Triggers when the view is selected from the main menu
     * Resumes the update thread if cancelled
     */
    @Override
    public void onViewSelected() {
        // Only restart if it was previously cancelled.
        Logger.log("Bikes view selected!");
        if (updateService.getState() == Worker.State.CANCELLED) {
            updateService.restart();
        }
    }

    /**
     * Trigers when the view is unselected
     * Cancels the update thread.
     */
    @Override
    public void onViewUnselected() {
        Logger.log("Bikes view unselected!");
        if (updateService.isRunning()) {
            updateService.cancel();
        }
    }
}