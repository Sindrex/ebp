package managers;

import javafx.beans.property.*;
import javafx.scene.control.Button;
import objects.Repair;

/**
 * Subclass for TableView
 * Is mutable through property values, unlike {@link Repair}
 * Repair values can become outdated.
 * @author Odd-Erik Frantzen
 */
public class RepairTableRow extends Repair {

    /**
     * Properties for {@link javafx.scene.control.TableView} use
     */
    private IntegerProperty repairIdProperty;
    private IntegerProperty bikeIdProperty;
    private StringProperty requestDateProperty;
    private StringProperty descriptionProperty;
    private StringProperty returnDateProperty;
    private StringProperty returnDescriptionProperty;
    private DoubleProperty priceProperty;
    private ObjectProperty<Button> buttonProperty;

    public RepairTableRow(int repairId, int bikeId, String requestDate, String description) {
        super(repairId, bikeId, requestDate, description);
        startProperties();

    }

    public RepairTableRow(int repairId) {
        super(repairId);
        startProperties();
    }

    public RepairTableRow(int repairId, int bikeId, String requestDate, String description, String returnDate, double price, String returnDescription) {
        super(repairId, bikeId, requestDate, description, returnDate, price, returnDescription);
        startProperties();
    }

    /**
     * Constructs a RepairTableRow from a {@link Repair}
     * Fills in all values from the Repair
     *
     * @param r the {@link Repair} to use
     * @return a new {@link RepairTableRow} with data from the {@link Repair}
     */
    public static RepairTableRow from(Repair r) {
        return new RepairTableRow(r.getRepairId(), r.getBikeId(), r.getRequestDate(), r.getDescription(),
                r.getReturnDate(), r.getPrice(), r.getReturnDescription());
    }

    /**
     * Initializes properties.
     */
    private void startProperties() {
        repairIdProperty = new SimpleIntegerProperty(getRepairId());
        bikeIdProperty = new SimpleIntegerProperty(getBikeId());
        requestDateProperty = new SimpleStringProperty(getRequestDate());
        descriptionProperty = new SimpleStringProperty(getDescription());
        returnDateProperty = new SimpleStringProperty(getReturnDate());
        returnDescriptionProperty = new SimpleStringProperty(getReturnDescription());
        priceProperty = new SimpleDoubleProperty(getPrice());
        buttonProperty = new SimpleObjectProperty<Button>();
    }

    public IntegerProperty repairIdProperty() {
        return repairIdProperty;
    }

    public IntegerProperty bikeIdProperty() {
        return bikeIdProperty;
    }

    public StringProperty requestDateProperty() {
        return requestDateProperty;
    }

    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }

    public StringProperty returnDateProperty() {
        return returnDateProperty;
    }

    public StringProperty returnDescriptionProperty() {
        return returnDescriptionProperty;
    }

    public DoubleProperty priceProperty() {
        return priceProperty;
    }

    public ObjectProperty<Button> buttonProperty() {
        return buttonProperty;
    }

    private void setRepairIdProperty(int repairIdProperty) {
        this.repairIdProperty.set(repairIdProperty);
    }

    private void setBikeIdProperty(int bikeIdProperty) {
        this.bikeIdProperty.set(bikeIdProperty);
    }

    private void setRequestDateProperty(String requestDateProperty) {
        this.requestDateProperty.set(requestDateProperty);
    }

    private void setDescriptionProperty(String descriptionProperty) {
        this.descriptionProperty.set(descriptionProperty);
    }

    private void setReturnDateProperty(String returnDateProperty) {
        this.returnDateProperty.set(returnDateProperty);
    }

    private void setReturnDescriptionProperty(String returnDescriptionProperty) {
        this.returnDescriptionProperty.set(returnDescriptionProperty);
    }

    private void setPriceProperty(double priceProperty) {
        this.priceProperty.set(priceProperty);
    }

    /**
     * Sets all properties from a {@link Repair}
     *
     * @param r the {@link Repair} to set them from
     */
    void setFrom(Repair r) {
        setRepairIdProperty(r.getRepairId());
        setBikeIdProperty(r.getBikeId());
        setRequestDateProperty(r.getRequestDate());
        setDescriptionProperty(r.getDescription());
        setReturnDateProperty(r.getReturnDate());
        setReturnDescriptionProperty(r.getReturnDescription());
        setPriceProperty(r.getPrice());
    }
}
