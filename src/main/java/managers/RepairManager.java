package managers;

import db.dao.RepairDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import logging.Logger;
import objects.Repair;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author odderikf
 * Manages Repair objects and access to the Database.
 */
@SuppressWarnings("JavaDoc")
public class RepairManager {
    private final RepairDao dao = RepairDao.getInstance();
    private ObservableList<RepairTableRow> repairs;

    public RepairManager(){
        repairs = FXCollections.observableArrayList();
    }

    /**
     * Adds a Repair listing to the database
     *
     * @param bikeId      ID of the bike to be repaired
     * @param requestDate when the request was given. Preferably today, but old logs can be added
     * @param description why the repair is needed
     * @return whether the repair was successful
     * @throws java.util.InputMismatchException
     * @throws InterruptedException
     * @throws Exception
     * @see db.dao.RepairDao
     */
    public boolean addRepair(int bikeId, String requestDate, String description) throws Exception {
        return dao.add(new Repair(Repair.INVALID_ID, bikeId, requestDate, description)) && refresh();
    }
    public boolean addRepair(Repair repair) throws Exception{
        return dao.add(repair) && refresh();
    }
    /**
     * Adds return info to an existing repair
     *
     * @param repairId          which repair was finished
     * @param returnDate        When the repair was finished
     * @param price             The price given by the repair service
     * @param returnDescription info about the repair
     * @return whether data was successfully added
     * @throws Exception
     */
    public boolean returnRepair(int repairId, String returnDate, double price, String returnDescription) throws Exception {
        Repair repair = getRepair(repairId);
        if (repair != null) {
            Repair newRepair = new Repair(repairId, repair.getBikeId(),
                    repair.getRequestDate(), repair.getDescription(),
                    returnDate, price, returnDescription);
            try {
                dao.update(newRepair);
                refresh();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else
            return false;

    }

    public boolean updateRepair(Repair r) throws Exception{
        if (dao.update(r)){
            if (repairs.contains(r)){
                repairs.get(repairs.indexOf(r)).setFrom(r);
            }else {
                repairs.add(RepairTableRow.from(r));
            }
            return true;
        }
        return  dao.update(r) && refresh();
    }

    /**
     * Gets a repair with a specific ID
     *
     * @param id the ID of the wanted repair
     * @return the repair object
     * @throws Exception
     */
    public RepairTableRow getRepair(int id) throws Exception {
        if (repairs.contains(new Repair(id))){
            return repairs.get(repairs.indexOf(new Repair(id)));
        }else return null;
    }

    /**
     * Fetches repairs matching a list of fields. Any invalid values are not used for filtering
     * e.g. if you want all repairs for bike 5, set bikeId to 5 and all other values to invalid.
     *
     * @param bikeId      invalid: Repair.INVALID_ID
     * @param requestDate invalid: Repair.INVALID_STRING
     * @param description invalid: Repair.INVALID_STRING
     * @return the list of repairs matching the values. might be empty.
     * @throws Exception
     */
    public List<Repair> getRepairsMatching(int bikeId, String requestDate, String description) throws Exception {
        return getRepairsMatching(bikeId, requestDate, description, null, Double.NaN, null);
    }

    /**
     * Fetches repairs matching a list of fields. Any invalid values are not used for filtering
     * e.g. if you want all repairs for bike 5, set bikeId to 5 and all other values to invalid.
     *
     * @param bikeId            invalid: Repair.INVALID_ID
     * @param requestDate       invalid: Repair.INVALID_STRING
     * @param description       invalid: Repair.INVALID_STRING
     * @param returnDate        invalid: Repair.INVALID_STRING
     * @param price             invalid: Repair.INVALID_PRICE
     * @param returnDescription invalid: Repair.INVALID_STRING
     * @return the list of repairs matching the values. might be empty.
     * @throws Exception
     */
    public List<Repair> getRepairsMatching(int bikeId, String requestDate, String description, String returnDate, double price, String returnDescription) throws Exception {
        ArrayList<Repair> returnList = new ArrayList<>();
        for (Repair e :
                repairs) {
            boolean matching =
                    //if valid, check if matches. Double.isNaN is necessary because NaN != NaN
                    (bikeId <= 0 || bikeId == e.getBikeId())
                            && (requestDate == Repair.INVALID_STRING || requestDate.equals( Repair.INVALID_STRING )|| requestDate.equals(e.getRequestDate()))
                            && (description == Repair.INVALID_STRING || description.equals(Repair.INVALID_STRING) ||description.equals(e.getDescription()))
                            && (returnDate == Repair.INVALID_STRING || returnDate.equals(Repair.INVALID_STRING) || returnDate.equals(e.getReturnDate()))
                            && ((price < 0 || Double.isNaN(price)) || price == Repair.INVALID_PRICE|| Math.abs(price - e.getPrice()) < 0.01)
                            && (returnDescription == null || returnDescription.equals(Repair.INVALID_STRING) || returnDescription.equals(e.getReturnDescription()));
            if (matching){
                returnList.add(RepairTableRow.from(e));
            }
        }
        return returnList;
    }

    /**
     * Get a full list of all repairs, loaded fresh from the database.
     *
     * @return the full list of all repairs
     * @throws Exception
     */
    public ObservableList<RepairTableRow> getRepairs() throws Exception {
        return repairs;
    }

    public boolean refresh() {
        try {
            List<Repair> loaded = dao.getAll();
            for (Repair r: loaded) {
                if (repairs.contains(r)){
                    repairs.get(repairs.indexOf(r)).setFrom(r);
                }else{
                    repairs.add(RepairTableRow.from(r));
                }
                repairs.retainAll(loaded);
            }
            return true;
        }catch (Exception e){
            Logger.log(e.getMessage());
            return false;
        }
    }
}
