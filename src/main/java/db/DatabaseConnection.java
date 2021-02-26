package db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for accessing the database.
 * @author Joergen Bele Reinfjell
 */
public interface DatabaseConnection extends AutoCloseable {
    PreparedStatement prepareStatement(String statement);
    PreparedStatement prepareStatement(String statement, int FLAG);

    /**
     * Creates a new database connection.
     * @param database the database uri.
     * @param username the database username.
     * @param password the database password.
     * @throws SQLException when unable to create database connection.
     */
    void create(String database, String username, String password) throws SQLException;

    /**
     * Enable or disable autocommit.
     * @param enable the boolean determining whether to enable or disable autocommit.
     */
    void autocommit(boolean enable);

    /**
     * Commits changes to database. Used in case of autocommit being turned off for transaction handling.
     */
    void commit();

    /**
     * Rollback the database to the state it was in before executing a statement.
     */
    void rollback();

    /**
     * Checks if the connection to the database is open.
     * This is used to make sure the connection is still valid before, which is
     * necessary eg. when putting it back into the connection pool.
     * @return true if the connection is closed, false if it is still open.
     */
    boolean isClosed();

    /**
     * Close the database connection.
     * Same as close(), but can be used when it has been overridden.
     * @throws SQLException when unable to close connection.
     */
    void closeConnection() throws SQLException;
}
