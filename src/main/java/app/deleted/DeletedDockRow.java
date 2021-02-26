package app.deleted;

import app.map.LatLng;
import javafx.scene.control.Button;
import objects.DockingStation;
import java.sql.Timestamp;

/**
 * This class represents a deleted docking station row in a table.
 *
 * @author Sindre Paulshus
 * @author Odd-Erik Frantzen
 */
public class DeletedDockRow extends DockingStation{
    private final Button restoreButton = new Button("Restore");
    private DeletedController.TableCommunicator communicator;

    private DeletedDockRow(int dockID, int maxBikes, LatLng position, double powerUsage, ActiveStatus status,
                           Timestamp statusTimestamp, DeletedController.TableCommunicator comun){
        super(dockID,maxBikes,position,powerUsage,status, statusTimestamp);
        communicator = comun;

    }

    public static DeletedDockRow from(DockingStation d, DeletedController.TableCommunicator communicator){
        DeletedDockRow dbr = new DeletedDockRow(d.getDockID(), d.getMAX_BIKES(), d.getPosition(), d.getPowerUsage(),
                d.getStatus(), d.getStatusTimestamp(), communicator);
        dbr.restoreButton.setOnAction(e -> communicator.restoreDock(dbr.getDockID()));
        dbr.restoreButton.setStyle("-fx-background-color: green");
        return dbr;
    }

    public Button getRestoreButton() {
        return restoreButton;
    }
}
