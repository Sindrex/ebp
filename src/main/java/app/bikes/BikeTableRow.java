package app.bikes;

import app.Inputs;
import javafx.scene.control.Button;
import objects.Bike;

/**
 * This class represents a row containing information about a bike in a table.
 *
 * @author Aleksander Johansen
 * @author Odd-Erik Frantzen
 * @author Sindre Paulshus
 */
public class BikeTableRow extends Bike {
    private Button detailsButton = new Button("Details");
    private BikesController.TableCommunicator communicator;

    /**
     * Private constructor. Creates a BikeTableRow object to put in the tableview.
     * @param bike Takes a Bike object and copes it's attributes.
     */
    private BikeTableRow(Bike bike) {
        super(bike.getId(), bike.getType(), bike.getMake(), bike.getPrice(), bike.getDatePurchased(),
                bike.getActiveStatus(), bike.getUserId(), bike.getStationId(), bike.getTotalTrips(),
                bike.getStatusTimestamp(), bike.getTotalKilometers(), bike.getPosition(), Inputs.roundDouble(bike.getChargeLevel(), 1));
    }

    /**
     * Static method for creating a BikeTableRow from anywhere.
     * @param b Bike object for copying attributes
     * @param communicator Interface that is used to communicate between this object and the BikesController, so that it can call upon it's methods.
     * @return Returns a BikeTableRow object.
     */
    public static BikeTableRow from(Bike b, BikesController.TableCommunicator communicator) {
        BikeTableRow t = new BikeTableRow(b);
        t.communicator = communicator;
        t.detailsButton.setOnAction(e -> {
            communicator.openBikeDetails();
            communicator.updateDetails(t);
        });
        return t;
    }

    /**
     * Used because tableviews require get methods for showing data from an object.
     * @return The details button.
     */
    public Button getDetailsButton() {
        return detailsButton;
    }
}