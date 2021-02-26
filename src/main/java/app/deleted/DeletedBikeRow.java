package app.deleted;

import javafx.scene.control.Button;
import objects.Bike;

/**
 * @author Sindre Paulshus
 * @author Odd-Erik Frantzen
 */
public class DeletedBikeRow extends Bike {

    private Button restoreButton = new Button("Restore");
    public DeletedController.TableCommunicator communicator;
    private int totalTrips = 0;
    private double totalKM = 0;

    private DeletedBikeRow(Bike b, DeletedController.TableCommunicator comun){
        super(b.getId(), b.getType(), b.getMake(), b.getPrice(), b.getDatePurchased(),
                b.getActiveStatus(), b.getStationId());
        communicator = comun;
        restoreButton = new Button("Restore");
        totalTrips = b.getTotalTrips();
        totalKM = b.getTotalKilometers();
    }

    public static DeletedBikeRow from(Bike b, DeletedController.TableCommunicator communicator){
        DeletedBikeRow dbr = new DeletedBikeRow(b, communicator);
        dbr.restoreButton.setOnAction(e -> communicator.restoreBike(dbr.getId()));
        dbr.restoreButton.setStyle("-fx-background-color: green");
        return dbr;
    }

    public Button getRestoreButton() {
        return restoreButton;
    }

    @Override
    public int getTotalTrips() {
        return totalTrips;
    }

    public double getTotalKM() {
        return totalKM;
    }
}
