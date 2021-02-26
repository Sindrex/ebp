package app.dockingstation;

import db.dao.DockingStationDao;
import javafx.scene.control.Button;
import objects.DockingStation;

/**
 * Class representing a docking station table row.
 *
 * @author Aleksander Johansen
 * @author Odd-Erik Frantzen
 */
public class DockingTableRow extends DockingStation {
    private Button detailsButton = new Button("Details");
    private DockingStationController.DockingCommunicator communicator;
    private int bikeAmount;

    private DockingTableRow(DockingStation station){
        super(station.getDockID(), station.getMAX_BIKES(), station.getPosition(), station.getPowerUsage(),
                station.getStatus(), station.getStatusTimestamp());
        // TODO: Null pointer exception.
        this.bikeAmount = DockingStationDao.getInstance().bikesInDock(station).size();
    }

    public static DockingTableRow from(DockingStation d, DockingStationController.DockingCommunicator communicator){
        DockingTableRow t = new DockingTableRow(d);
        t.communicator = communicator;
        t.detailsButton.setOnAction(e -> {
            communicator.openDetails();
            communicator.updateDetails(t);
            System.out.println("Bike amount: " + t.getBikeAmount());
        });
        return t;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }

    public int getBikeAmount() {
        return bikeAmount;
    }

    public void setBikeAmount(int bikeAmount) {
        this.bikeAmount = bikeAmount;
    }
}