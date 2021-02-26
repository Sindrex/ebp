package db;

import java.sql.*;

/**
 * An abstract class for handling a MYSQL connection.
 * @author Joergen Bele Reinfjell
 */
public abstract class MysqlDatabaseConnection implements DatabaseConnection {
    private Connection connection = null;

    private static final long RETRY_TIMEOUT_MS = 100;

    MysqlDatabaseConnection() {}

    /**
     * Creates a database connection to a MYSQL server.
     * @param database the database url.
     * @param username the database username.
     * @param password the database password.
     * @throws SQLException
     */
    @Override
    public void create(String database, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(database, username, password);
    }

    /**
     * Creates a PreparedStatement on this connection.
     * @param statement the sql statement to be prepared.
     * @return the PreparedStatement.
     */
    @Override
    public PreparedStatement prepareStatement(String statement) {
        try {
            return connection.prepareStatement(statement);
        } catch (SQLException e) {
            e.printStackTrace();
           return null;
        }
    }

    /**
     * Creates a PreparedStatement using a custom flag, on this connection.
     * @param statement the sql statement to be prepared.
     * @param flag the statement flag.
     * @return the PreparedStatement.
     */
    @Override
    public PreparedStatement prepareStatement(String statement, int flag) {
        try {
            return connection.prepareStatement(statement, flag);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Enable or disable autocommit.
     * @param enable the boolean determining whether to enable or disable autocommit.
     */
    @Override
    public void autocommit(boolean enable) {
        while (true) {
            try {
                if (connection != null) {
                    connection.setAutoCommit(enable);
                }
            } catch (SQLException e) {
                DatabaseUtils.trySleep(RETRY_TIMEOUT_MS);
                continue;
            }
            break;
        }
    }

    /**
     * Commit the transaction(s) to the database.
     */
    @Override
    public void commit() {
        while (true) {
            try {
                if (connection != null) {
                    connection.commit();
                }
            } catch (SQLException e) {
                DatabaseUtils.trySleep(RETRY_TIMEOUT_MS);
                continue;
            }
            break;
        }
    }

    /**
     * Rollback the database.
     */
    @Override
    public void rollback() {
        while (true) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                DatabaseUtils.trySleep(RETRY_TIMEOUT_MS);
                continue;
            }
            break;
        }
    }

    /**
     * Close the connection to the database.
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * Checks if the database connection is closed.
     * @return a boolean value of true if it is closed,
     *      or false if it is still open.
     */
    @Override
    public boolean isClosed() {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            /* Treat the connection as closed. */
            return true;
        }
    }

    /**
     * Closes the connection to the database..
     * @throws SQLException when unable to close connection.
     */

    @Override
    public void closeConnection() throws SQLException {
        connection.close();
    }
}
