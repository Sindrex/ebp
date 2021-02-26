package app.bikes;

import app.AlertBox;
import db.dao.TypeDao;
import javafx.scene.control.Button;
import logging.Logger;
import objects.BikeType;

/**
 * This class represents a row containing information about a bike type in a table.
 *
 * @author Joergen Bele Reinfjell
 */
public class BikeTypeTableRow extends BikeType {
    private Button delete = new Button("Delete");
    private BikesController.TableCommunicator communicator;

    private final static TypeDao DAO = TypeDao.getInstance();

    private BikeTypeTableRow(BikeType t) {
        super(t.getName(), t.getTimestamp(), t.getBikeCount());
    }

    public static BikeTypeTableRow from(BikeType t) {
        BikeTypeTableRow t1 = new BikeTypeTableRow(t);

        t1.delete.setStyle("-fx-background-color: red");
        t1.delete.setOnAction(e -> {
            BikeType newType = new BikeType(t.getName());
            newType.setActive(false);
            if(!DAO.update(newType)){
                AlertBox.errorMessage("Error", "Could not delete type.");
                Logger.errf("Error: %s", "Could not delete type.");
            }
        });
        return t1;
    }

    public Button getDelete() {
        return delete;
    }
}