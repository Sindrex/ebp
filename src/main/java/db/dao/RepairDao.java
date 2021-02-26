package db.dao;

import db.Database;
import db.DatabaseConnection;
import objects.Repair;

import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

/**
 * @author odderikf
 * @see db.dao.DataAccessInterface
 * Data Access Object for the repair and repair_return tables
 */
public class RepairDao implements DataAccessInterface<Repair> {

    /**
     * Descriptions for InputMismatchExceptions
     */
    public static final String INVALID_DATE_DESCRIPTION = "Invalid date format";
    public static final String INVALID_BIKE_ID_DESCRIPTION = "Bike not registered";
    public static final String INVALID_NULL_DESCRIPTION = "Value was null illegally";
    /**
     * Strings for SQL statements
     */
    private static final String UPDATE_ALL_STATEMENT =
            "UPDATE repair SET\n" +
            "      request_date = ?,\n" +
            "      description = ?,\n" +
            "      bike_id = ?\n" +
            "WHERE repair_id = ?;\n";

    private static final String UPDATE_RETURN_ALL_STATEMENT =
           "INSERT INTO repair_return (repair_id, return_date, price, description)\n" +
           "VALUES (?,?,?,?)\n" +
           "ON DUPLICATE KEY UPDATE repair_id = ?, return_date = ?, price = ?, description = ?;";

    private static final String ADD_ALL_STATEMENT = "INSERT INTO repair (repair_id, request_date, description, bike_id) VALUES (?, ?, ?, ?)";

    private static final String ADD_STATEMENT = "INSERT INTO repair (repair_id, request_date, description, bike_id) VALUES (?, ?, ?, ?);";
    private static final String UPDATE_STATEMENT = "UPDATE repair SET request_date=?, description=?, bike_id=? WHERE repair_id=?";
    private static final String UPDATE_RETURN_STATEMENT = "UPDATE repair_return SET return_date = ?, description=?, price=? WHERE repair_id=?;";
    private static final String INSERT_RETURN_STATEMENT = "INSERT INTO repair_return VALUES (?, ?, ?, ?)";
    private static final String GET_ALL_STATEMENT = "SELECT repair.repair_id, bike_id, request_date,  repair.description, return_date, price, return2.description\n" +
            "FROM repair LEFT JOIN repair_return return2 ON repair.repair_id = return2.repair_id";
    private static final String GET_RETURN_STATEMENT = GET_ALL_STATEMENT + " WHERE repair.repair_id = ?;";
    private static final String GET_CURRENT_REPAIR_STATEMENT = GET_ALL_STATEMENT + " WHERE repair.bike_id = ? AND price IS NULL";

    private static final RepairDao instance;
    /**
     * Values for SQL-errors
     */
    private static final int INVALID_DATE_SQL_ERROR = 1292;
    private static final int INVALID_NULL_SQL_ERROR = 1048;
    private static final int FOREIGN_KEY_MISSING_SQL_ERROR = 1452;

    private RepairDao() {
    }

    static {
        instance = new RepairDao();
    }


    public static RepairDao getInstance() {
        return instance;
    }

    /**
     * @param reference the reference object with the fields used for unique
     *                  identification.
     * @return the identified object, default null
     * @throws InterruptedException, SQLException
     */
    @Override
    public Repair get(Repair reference) throws Exception {
        if (reference.getRepairId() > 0) {
            try (
                    DatabaseConnection con = Database.getConnection();
                    PreparedStatement p = con.prepareStatement(GET_RETURN_STATEMENT)
            ) {
                p.setInt(1, reference.getRepairId());
                return repairFromRes(p);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private Repair repairFromRes(PreparedStatement p) throws SQLException {
        ResultSet r = p.executeQuery();
        if (r.next()) {
            int id = r.getInt(1);
            int bikeId = r.getInt(2);
            String requestDate = r.getDate(3).toString();
            String description = r.getString(4);
            String returnDate = r.getString(5);
            double price = r.getDouble(6);
            if (r.wasNull()) {
                price = Double.NaN;
            }
            String returnDescription = r.getString(7);
            Repair myRepair = new Repair(id, bikeId, requestDate, description, returnDate, price, returnDescription);
            r.close();
            return myRepair;
        } else {
            return null;
        }
    }

    /**
     * Adds one element to the database,
     *
     * @param element the element to add. ID will be generated, so please set to Repair.INVALID_ID for clarity.
     * @return whether an item was successfully added
     * @throws InterruptedException   from Database.getConnection();
     * @throws Exception              from con.close();
     * @throws InputMismatchException for erroneous input, e.g. bike_id not in bike table, or date or description null.
     */
    @Override
    public boolean add(Repair element) throws Exception {
        try (
                DatabaseConnection con = Database.getConnection()
        ) {
            return add(element, con);
        }
    }

    /**
     * Helper method to save duplicate code
     *
     * @param element the element to add
     * @param con     the connection to run it through, for transaction purposes
     * @return whether it succeeded or not
     * @throws InputMismatchException for erroneous input
     */
    private boolean add(Repair element, DatabaseConnection con) {
        try (
                PreparedStatement p = con.prepareStatement(ADD_STATEMENT)
        ) {
            if (element.getBikeId() < 0) {
                throw new InputMismatchException(INVALID_BIKE_ID_DESCRIPTION);
            }

            if (element.getRepairId() > 0) {
                p.setInt(1, element.getRepairId());
            } else {
                p.setNull(1, Types.INTEGER);
            }
            p.setString(2, element.getRequestDate());
            p.setString(3, element.getDescription());
            p.setInt(4, element.getBikeId());
            int affected = p.executeUpdate();
            return affected == 1;

        } catch (SQLException e) {
            if (e.getErrorCode() == INVALID_DATE_SQL_ERROR) {
                throw new InputMismatchException(INVALID_DATE_DESCRIPTION);
            } else if (e.getErrorCode() == FOREIGN_KEY_MISSING_SQL_ERROR) {
                throw new InputMismatchException(INVALID_BIKE_ID_DESCRIPTION);
            }
            e.printStackTrace();
            return false;
        }
    }


    /* code for add that return keys, not needed as of right now, and also unfinished / requires missing code
    public boolean addReturnKeys(Repair element) throws Exception {
        try(
            DatabaseConnection con = Database.getConnection();
            PreparedStatement p = con.prepareStatement(ADD_STATEMENT, Statement.RETURN_GENERATED_KEYS);
        ){
            p.setNull(1, Types.INTEGER);
            p.setString(2, element.getRequestDate());
            p.setString(3, element.getDescription());
            p.setInt(4, element.getBikeId());
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()){
                r.next();
                int id = r.getInt(1);
            }
        }
        return false;
    }
    */

    /**
     * Updates the database's record for an element.
     *
     * @param element the element to update.
     * @return true if update of both tables succeeded,
     * or if one table succeeded and the other had no valid data to update.
     * @throws InterruptedException   from Database.getConnection
     * @throws Exception              from autoclosable
     * @throws InputMismatchException for erroneous input (illegal null field)
     */
    @Override
    public boolean update(Repair element) throws Exception {
        if (element.getRepairId() > 0) {
            try (
                    DatabaseConnection con = Database.getConnection()
            ) {
                return update(element, con);
            }
        } else {
            return false;
        }

    }


    /**
     * Gets the repair object for the current repair element (the last one which was not returned).
     * @param bikeId the bike id.
     * @return the Repair instance.
     */
    public Repair getCurrentRepair(int bikeId) {
        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement prep = con.prepareStatement(GET_CURRENT_REPAIR_STATEMENT);
        ) {
            prep.setInt(1, bikeId);
            return repairFromRes(prep);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to save duplicate code
     * Updates repair
     * If all repair_return fields are valid, try to update repair_return.
     * If that update fails, try insert
     *
     * @param element the element to add
     * @param con     the connection to use, for transaction purposes
     * @return true if update of both tables succeeded,
     * or if one table succeeded and the other had no valid data to update.
     */
    private boolean update(Repair element, DatabaseConnection con) {
        try (
                PreparedStatement p = con.prepareStatement(UPDATE_STATEMENT)
        ) {
            con.autocommit(false);
            p.setInt(4, element.getRepairId());
            p.setString(1, element.getRequestDate());
            p.setString(2, element.getDescription());
            p.setInt(3, element.getBikeId());
            int affected = p.executeUpdate();
            if (affected == 1) {
                if (element.getReturnDate() != null && element.getReturnDescription() != null && element.getPrice() > 0) {
                    try (
                            PreparedStatement p2 = con.prepareStatement(UPDATE_RETURN_STATEMENT)
                    ) {
                        p2.setInt(4, element.getRepairId());
                        p2.setString(1, element.getReturnDate());
                        p2.setString(2, element.getReturnDescription());
                        p2.setDouble(3, element.getPrice());
                        int affected_2 = p2.executeUpdate();
                        if (affected_2 == 0) {
                            try (
                                    PreparedStatement p3 = con.prepareStatement(INSERT_RETURN_STATEMENT)
                            ) {
                                p3.setInt(1, element.getRepairId());
                                p3.setString(2, element.getReturnDate());
                                p3.setDouble(3, element.getPrice());
                                p3.setString(4, element.getReturnDescription());
                                affected_2 = p3.executeUpdate();
                            }
                        }
                        if (affected_2 == 1) {
                            con.prepareStatement("COMMIT;").execute();
                            return true;
                        } else {
                            con.rollback();
                            return false;
                        }
                    }
                } else {
                    con.prepareStatement("COMMIT;").execute();
                    return true;
                }
            } else {
                con.rollback();
                return false;
            }
        } catch (SQLException e) {
            con.rollback();
            if (e.getErrorCode() == INVALID_NULL_SQL_ERROR) {
                throw new InputMismatchException(INVALID_NULL_DESCRIPTION);
            }
            e.printStackTrace();
            return false;
        } finally {
            con.autocommit(true);
        }
    }

    /**
     * @param element the element to delete.
     * @return false, because repairs cannot be deleted
     */
    @Override
    public boolean delete(Repair element) {
        return false;
    }


    private List<Repair> getAllHelper(PreparedStatement p) throws Exception {

        List<Repair> result = new ArrayList<>();
        ResultSet resultSet = p.executeQuery();
        while (resultSet.next()) {
            Repair r = new Repair(
                    resultSet.getInt(1),
                    resultSet.getInt(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getString(5),
                    resultSet.getDouble(6),
                    resultSet.getString(7)
            );
            result.add(r);
        }
        resultSet.close();
        return result;
    }

    /**
     * @return List of all repairs fetched from database
     * @throws SQLException         from database
     * @throws InterruptedException from getConnection
     * @throws Exception            from autoclosable
     */
    @Override
    public List<Repair> getAll() throws Exception {

        try (
                DatabaseConnection con = Database.getConnection();
                PreparedStatement p = con.prepareStatement(GET_ALL_STATEMENT)
        ) {
            return getAllHelper(p);
        }
    }

    public List<Repair> getAllMatching(Repair reference) throws Exception {

        boolean validId = (reference.getBikeId() != Repair.INVALID_ID);
        boolean validReqDate = reference.getRequestDate() != Repair.INVALID_STRING
                && !reference.getRequestDate().equals(Repair.INVALID_STRING);

        boolean validDescription = reference.getDescription() != Repair.INVALID_STRING
                && !reference.getDescription().equals(Repair.INVALID_STRING);

        boolean validReturnDate = reference.getReturnDate() != Repair.INVALID_STRING
                && !reference.getReturnDate().equals(Repair.INVALID_STRING);

        boolean validPrice = reference.getPrice() >= 0 && !Double.isNaN(reference.getPrice())
                && reference.getPrice() != Repair.INVALID_PRICE;

        boolean validReturnDescription = (reference.getReturnDescription() != null
                && !reference.getReturnDescription().equals(Repair.INVALID_STRING));

        boolean[] valids = {validId, validReqDate, validDescription, validReturnDate, validPrice, validReturnDescription};
        String[] conditions = {"bike_id", "request_date", "repair.description",
                "return_date", "price", "repair_return.description"};

        if (!(validId || validReqDate || validDescription || validReturnDate || validPrice || validReturnDescription)) {
            return getAll();
        } else {
            StringBuilder b = new StringBuilder(GET_ALL_STATEMENT);
            b.append(" WHERE ");
            boolean previous = false;
            for (int i = 0; i < valids.length; i++) {
                if (valids[i]) {
                    if (previous) b.append(" AND ");
                    b.append(conditions[i]);
                    b.append(" = ?");
                    previous = true;
                }
            }
            try (
                    DatabaseConnection con = Database.getConnection();
                    PreparedStatement p = con.prepareStatement(b.toString())
            ) {

                int i = 1;
                if (validId) p.setInt(i++, reference.getBikeId());
                if (validReqDate) p.setString(i++, reference.getRequestDate());
                if (validDescription) p.setString(i++, reference.getDescription());
                if (validReturnDate) p.setString(i++, reference.getReturnDate());
                if (validPrice) p.setDouble(i++, reference.getPrice());
                if (validReturnDescription) p.setString(i, reference.getReturnDescription());

                return getAllHelper(p);
            }
        }
    }

    /*

    /**
     * @param elements list of elements to be added.
     * @return whether all elements were added successfully. Will rollback if any fail
     * @throws InterruptedException from getConnection
     * @throws Exception            from autoclosable
     */
    @Override
    public boolean addAll(List<Repair> elements) throws Exception {
        try (
                DatabaseConnection con = Database.getConnection()
        ) {
            try (PreparedStatement p = con.prepareStatement(ADD_ALL_STATEMENT)){
                con.autocommit(false);
                for (Repair r : elements) {
                    if (!r.isValid()) return false;
                    p.setInt(1, r.getRepairId());
                    p.setString(2, r.getRequestDate());
                    p.setString(3, r.getDescription());
                    p.setInt(4, r.getBikeId());
                    p.addBatch();
                }
                p.executeBatch();
                con.commit();
                return true;

            }catch (SQLException e){
                con.rollback();
                e.printStackTrace();
                return false;

            }finally {
                con.autocommit(true);
            }
        }
    }

    /**
     * Updates all Repairs in list. Rollbacks if any updates fail
     *
     * @param elements list of elements to be updated.
     * @return whether all elements were updated successfully.
     * @throws InterruptedException from getConnection()
     * @throws Exception            from autoclosable
     */
    @Override
    public boolean updateAll(List<Repair> elements) throws Exception {
        try (DatabaseConnection con = Database.getConnection() ) {
            try(PreparedStatement rep = con.prepareStatement(UPDATE_ALL_STATEMENT);
                PreparedStatement ret = con.prepareStatement(UPDATE_RETURN_ALL_STATEMENT)){
                boolean hasRet = false;
                for (Repair r : elements) {

                    if (!r.isValid()) return false;
                    rep.setInt(4, r.getRepairId());
                    rep.setString(1, r.getRequestDate());
                    if (r.getDescription() != null && !r.getDescription().equals(Repair.INVALID_STRING)){
                        rep.setString(2, r.getDescription());
                    }else rep.setNull(2, Types.VARCHAR);
                    rep.setInt(3, r.getBikeId());
                    rep.addBatch();

                    if (r.returnIsValid()){
                        hasRet = true;
                        ret.setInt(1, r.getRepairId());
                        ret.setInt(5, r.getRepairId());
                        ret.setString(2, r.getReturnDate());
                        ret.setString(6, r.getReturnDate());
                        ret.setDouble(3, r.getPrice());
                        ret.setDouble(7, r.getPrice());
                        ret.setString(4, r.getDescription());
                        ret.setString(8, r.getDescription());
                        ret.addBatch();
                    }
                }
                con.autocommit(false);
                rep.executeBatch();
                if (hasRet) ret.executeBatch();

                con.commit();
                return true;

            }catch (SQLException e){
                e.printStackTrace();
                con.rollback();
                return false;
            }finally {
                con.autocommit(true);
            }
        }
    }

    /**
     * @param elements list of elements to be deleted.
     * @return false, as repairs cannot be deleted
     */
    @Override
    public boolean deleteAll(List<Repair> elements) {
        return false;
    }
}
