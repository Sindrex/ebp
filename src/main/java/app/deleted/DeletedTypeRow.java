package app.deleted;

import objects.BikeType;
import javafx.scene.control.Button;

import java.sql.Timestamp;

/**
 * This class represents a deleted bike type row in a table.
 *
 * @author Sindre Paulshus
 * @author Joergen Bele Reinfjell
 */
public class DeletedTypeRow {
    private String name;
    private Timestamp timestamp;
    private Button restoreButton = new Button("Restore");

    private DeletedTypeRow(String name, Timestamp timestamp){
        this.name = name;
        this.timestamp = timestamp;
    }

    public static DeletedTypeRow from(BikeType t, DeletedController.TableCommunicator communicator){
        DeletedTypeRow dtr = new DeletedTypeRow(t.getName(), t.getTimestamp());
        dtr.restoreButton.setOnAction(e -> communicator.restoreType(dtr.getName()));
        dtr.restoreButton.setStyle("-fx-background-color: green");
        return dtr;
    }

    public String getName() {
        return name;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Button getRestoreButton() {
        return restoreButton;
    }
}
