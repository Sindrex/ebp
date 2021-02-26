package objects;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * @author odderikf
 * Container for data about one repair.
 * Implements comparable and equals, compares by repairId
 */

public class Repair implements Comparable<Repair>{

    public static final int INVALID_ID = -1;
    public static final String INVALID_STRING = null;
    public static final double INVALID_PRICE = Double.NaN;

    private final int bikeId;
    private final int repairId;
    private final String requestDate;
    private final String description;
    private final String returnDate;
    private double price;
    private final String returnDescription;

    /**
     * @param repairId    unique ID of the repair service
     * @param bikeId      unique ID of the bike being repaired
     * @param requestDate date the repair was requested
     * @param description description of what repair is needed
     *                    <p>
     *                    constructor for submitted repair reports that aren't done.
     */
    public Repair(int repairId, int bikeId, String requestDate, String description) {
        this.repairId = repairId;
        this.bikeId = bikeId;
        this.requestDate = requestDate;
        this.description = description;
        this.returnDate = null;
        this.price = Double.NaN;
        this.returnDescription = null;
    }


    /**
     * Minimal constructor for loading data
     *
     * @param repairId the ID of the repair to load
     */
    public Repair(int repairId) {
        this.repairId = repairId;
        this.bikeId = INVALID_ID;
        this.requestDate = null;
        this.description = null;
        this.returnDate = null;
        this.price = Float.NaN;
        this.returnDescription = null;
    }

    /**
     * Constructor for a Repair that has already had its repair returned
     *
     * @param repairId    unique ID of the repair service
     * @param requestDate date the repair was requested
     * @param description description of what repair is needed
     * @param price       how much the repairs cost, default Float.NaN
     * @param returnDate  when the bike was returned from repairs, default null
     */
    public Repair(int repairId, int bikeId, String requestDate, String description, String returnDate, double price, String returnDescription) {
        this.repairId = repairId;
        this.bikeId = bikeId;
        this.requestDate = requestDate;
        this.description = description;
        this.returnDate = returnDate;
        this.price = price;
        this.returnDescription = returnDescription;

    }

    public int getRepairId() {
        return repairId;
    }

    public int getBikeId() {
        return bikeId;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getDescription() {
        return description;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public double getPrice() {
        return price;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public static class Builder {
        private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        private int bikeId = Repair.INVALID_ID;
        private int repairId = Repair.INVALID_ID;
        private String requestDate =  formatter.format(Date.from(Instant.now()));
        private String description = Repair.INVALID_STRING;
        private String returnDate = Repair.INVALID_STRING;
        private double price = Repair.INVALID_PRICE;
        private String returnDescription = Repair.INVALID_STRING;



        public Builder() { }

        public Repair build() {
            return new Repair(repairId, bikeId, requestDate, description, returnDate, price, returnDescription);
        }

        public Builder using(Bike bike) {
            this.bikeId = bike.getId();
            return this;
        }

        public Builder from(Repair repair) {
            this.bikeId=repair.bikeId;
            this.repairId=repair.repairId;
            this.requestDate=repair.requestDate;
            this.description=repair.getDescription();
            this.returnDate=repair.getReturnDate();
            this.price=repair.price;
            this.returnDescription=repair.getReturnDescription();
            return this;
        }

        public Builder bikeId(int bikeId){
            this.bikeId = bikeId;
            return this;
        }
        public Builder repairId(int repairId){
            this.repairId=repairId;
            return this;
        }
        public Builder requestDate(String requestDate){
            this.requestDate=requestDate;
            return this;
        }

        public Builder requestDate(Date requestDate){
            this.requestDate = formatter.format(requestDate);
            return this;
        }
        public Builder description(String description){
            this.description=description;
            return this;
        }
        public Builder returnDate(String returnDate){
            this.returnDate=returnDate;
            return this;
        }
        public Builder returnDate(Date returnDate){
            this.returnDate = formatter.format(returnDate);
            return this;
        }
        public Builder price(double price){
            this.price=price;
            return this;
        }
        public Builder returnDescription(String returnDescription){
            this.returnDescription=returnDescription;
            return this;
        }
    }


    /**
     * CompareTo override
     *
     * @param that the other Repair object
     * @return difference between the two Repair object's IDs
     */
    @Override
    public int compareTo(Repair that) {
        return this.getRepairId() - that.getRepairId();
    }

    /**
     * Equality tester
     *
     * @param that the other Repair object
     * @return true if the ID is the same
     */
    public boolean equals(Repair that) {
        return this.getRepairId() == that.getRepairId();
    }

    /**
     * Equality tester for override purposes
     *
     * @param that the object to compare to
     * @return true if the ID is the same for two Repair objects. Non-repair values of that will result in false
     */
    @Override
    public boolean equals(Object that) {
        return that instanceof Repair && equals((Repair) that);
    }

    public boolean isValid(){
        return this.getRequestDate() != null && !this.getRequestDate().equals(Repair.INVALID_STRING)
                && this.getBikeId() != Repair.INVALID_ID;
    }
    public boolean returnIsValid(){
        return isValid()
                && this.getReturnDate() != null && !this.getReturnDate().equals(Repair.INVALID_STRING)
                && this.getReturnDescription() != null && !this.getReturnDescription().equals(Repair.INVALID_STRING)
                && (!Double.isNaN(this.getPrice()) && this.getPrice() != Repair.INVALID_PRICE);
        }

    @Override
    public String toString(){
        return "Repair: { id: "+repairId+", bikeId: "+bikeId+", reqDate: "+requestDate+", reqDescr.: "+description+
                ", retDate: "+returnDate+", retDescr.: "+returnDescription+", price: "+price + " }";
    }
}
