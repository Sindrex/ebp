package objects;

import db.dao.TypeDao;
import java.sql.Timestamp;

public class BikeType {

    private String name;
    private Timestamp timestamp;
    private int bikeCount;
    private TypeDao DAO = TypeDao.getInstance();
    private boolean active = true;

    public BikeType(String name, Timestamp timestamp, int bikeCount, boolean active){
        this.name = name;
        this.timestamp = timestamp;
        this.bikeCount = bikeCount;
        this.active = active;
    }

    public BikeType(String name, Timestamp timestamp, int bikeCount) { this(name, timestamp, bikeCount, true); }
    public BikeType(String name, Timestamp timestamp){ this(name, timestamp, 0); }
    public BikeType(String name){ this(name, new Timestamp(System.currentTimeMillis())); }

    /**
     * Get this type's name.
     * @return the name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Get this type's active (Active/Deleted)
     * @return the active as a boolean.
     */
    public boolean getActive() {
        return active;
    }

    /**
     * Set this type's active (Active/Deleted)
     * @param active true if you want to set active, false if deleted.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get this type's timestamp (the timestamp of which it was created)
     * @return the timestamp as an sql.Timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Get this type's bike Count.
     * Note: Not all types may have this set.
     * @return the bike count as an int
     */
    public int getBikeCount() {
        return bikeCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
