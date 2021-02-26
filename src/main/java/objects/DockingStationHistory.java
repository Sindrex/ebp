package objects;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Timestamp;

@Deprecated
public class DockingStationHistory {
    private String timeStamp;
    private int id;
    private double presentPowerUsage;

    public DockingStationHistory(String timestamp, int id, double power) {
        this.id = id;
        //timeStamp = new Timestamp(System.currentTimeMillis()).toString();
        this.timeStamp = timestamp;
        presentPowerUsage = power;
    }

    public int getId() {
        return id;
    }

    public double getPresentPowerUsage() {
        return presentPowerUsage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public SimpleStringProperty getTimeStampProperty(){
        return new SimpleStringProperty(timeStamp);
    }

    @Override
    public String toString() {
        return String.format("{timStamp: %s, id: %d, presentPowerUsage: %f}", timeStamp, id, presentPowerUsage);
    }

    public static void main(String[] args) {
        DockingStationHistory a = new DockingStationHistory(new Timestamp(System.currentTimeMillis()).toString(),34, 432.12);
        System.out.println(a.getTimeStamp());
    }
}
