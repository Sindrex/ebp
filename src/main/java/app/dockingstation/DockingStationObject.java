package app.dockingstation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Object representing a simplified docking station.
 *
 * @author Joergen Bele Reinfjell
 * @author Aleksander Johansen
 */
public class DockingStationObject {

    private int id;
    private double presentPowerUsage;
    private int numBikes;

    public DockingStationObject(int id, double presentPowerUsage, int numBikes) {
        this.id = id;
        this.presentPowerUsage = presentPowerUsage;
        this.numBikes = numBikes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public double getPresentPowerUsage() {
        return presentPowerUsage;
    }

    public void setPresentPowerUsage(double presentPowerUsage) {
        if (presentPowerUsage < 0) {
            throw new IllegalArgumentException("powerUsage must be >= 0");
        }
        this.presentPowerUsage = presentPowerUsage;
    }

    public int getNumBikes() {
        return numBikes;
    }

    public void setNumBikes(int numBikes) {
        this.numBikes = numBikes;
    }

    // TODO: Move to utility class.
    private double roundTo(double value, int places){
        if (places < 0){
            throw new IllegalArgumentException("places must be > 0.");
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Compares two DockingStationObject instances on all attributes.
     * @param o the DockingStationObject instance.
     * @return true if they are equal, else false.
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DockingStationObject)){
            return false;
        }
        DockingStationObject d = (DockingStationObject) o;
        return d.id == id && d.presentPowerUsage == presentPowerUsage && d.numBikes == numBikes;
    }
}