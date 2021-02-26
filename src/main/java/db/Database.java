package db;

import config.Configuration;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class functions as a database connection pool, requesting a connection
 * from the pool and then releasing it when done.
 *<br>
 * It is designed with the intention to be used as a resource in
 * "try-with-resources" blocks. Because of this it has to both be able to
 * create new connections which are aware of the connection queues so
 * that the resource itself can handle the action of putting the connection
 * back into the queue when "released".
 *
 * @author Jorgen Bele Reinfjell
 * date 05.03.2018
 */
public class Database extends MysqlDatabaseConnection {
    // The total number of availableConnections allowed.
    private final static int MAX_CONNECTIONS = 8;

    // Keep the total number of database availableConnections started for logging purposes.
    private static int startedConnections = 0;

    /*
     * Store a set of all active connections so that we can know how many active
     * connections are still in use.
     * NOTE: chose to use a Map instead of a Set because of the requirement to
      * implement Comparable for all objects in a set. This may or may not change.
     */
    private final static Map<DatabaseConnection, Boolean> activeConnections = new ConcurrentHashMap<>();

    // The queue containing available connections.
    private final static BlockingQueue<DatabaseConnection> availableConnections = new ArrayBlockingQueue<>(MAX_CONNECTIONS);

    public synchronized static boolean closeAllConnections() {
        for (DatabaseConnection con : activeConnections.keySet()) {
            try {
                con.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Closes all availableConnections in the connection queue.
     * Note that this does not close all connections, only those
     * that are not currently in use.
     * @return true on success, false on failure.
     */
    public synchronized static boolean closeAvailableConnections() {
        while (!availableConnections.isEmpty()) {
            try {
                DatabaseConnection con = availableConnections.poll();
                con.closeConnection();
                /* Remove from active queue.. */
                activeConnections.remove(con);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a database connection from the availableConnections queue.
     * @return DatabaseConnection the connection to the database,
     * or null on error.
     * @throws InterruptedException if interrupted while waiting to get connection.
     */
    public synchronized static DatabaseConnection getConnection() throws InterruptedException {
        while (true) {
            /* First check if there is any available availableConnections. If there are
               no such availableConnections, and the queue is not full, create a new
               connection to the database. */
            if (availableConnections.peek() == null && activeConnections.size() < MAX_CONNECTIONS) {
                if (!createNewConnection()) {
                    System.err.println("[****] Failed to create connection!");
                }
            }
            /* This is a blocking call, meaning that it waits until there is
               an available connection. */
            DatabaseConnection con = availableConnections.take();
            if (con.isClosed()) {
                activeConnections.remove(con);
                continue;
            }
            return con;
        }
    }

    /**
     * Creates a new connection and adds it to the queue.
     * @return true on success, false on failure.
     */
    private static boolean createNewConnection() {
        Properties properties = Configuration.getProperties();
        String database = properties.getProperty(Configuration.DATABASE_PROPERTY_ENTRY);
        String username = properties.getProperty(Configuration.USERNAME_PROPERTY_ENTRY);
        String password = properties.getProperty(Configuration.PASSWORD_PROPERTY_ENTRY);

        DatabaseConnection con = new Database();
        try {
            con.create(database, username, password);
            activeConnections.put(con, true);
            availableConnections.put(con);
        } catch (Exception e) {
            System.err.println("Exception on attempt to connect to database: " + database + " using username: "
                    + username + "using password: " + ( password == null || password.equals("") ? "YES" : "NO" ));
            e.printStackTrace();
            return false;
        }
        startedConnections++;
        return true;
    }

    /**
     * Checks if there is any active connections..
     * @return true when there is an active connection; otherwise, false.
     */
    public synchronized static boolean hasActiveConnections() {
        return !activeConnections.isEmpty();
    }

    /**
     * Checks if there is any available connections.
     * @return true when there is an available connection; otherwise, false.
     */
    public synchronized static boolean hasAvailableConnections() {
        return !availableConnections.isEmpty();
    }

    /**
     * Releases the connection back into the queue of available availableConnections if it still open.
     */
    public void releaseConnection() {
        try {
            if (this.isClosed()) {
                /*
                 * Don't add the connection back to the availableConnections
                 * queue because the database connection  is closed, and remove
                 * it from the activeConnections set.
                 */
                activeConnections.remove(this);
                return;
            }
            availableConnections.put(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the DatabaseConnection (this) to the queue of available availableConnections,
     * if the database connection is still active. It is implemented in this way
     * to be able to use try-with-resources on the returned object from
     * getConnection() to close it.
     */
    @Override
    public void close() {
        releaseConnection();
    }

    public synchronized static void printDebugInfo() {
        System.out.println("=== Database DEBUG info ===");
        System.out.printf("Started connections: %d\n", startedConnections);
        System.out.printf("Currently active connections: %d\n", activeConnections.size());
        for (DatabaseConnection db : activeConnections.keySet()) {
            System.out.println("\t" + db);
        }
        System.out.printf("Currently available connections: %d\n", availableConnections.size());
        for (DatabaseConnection db : availableConnections) {
            System.out.println("\t" + db);
        }
    }
}
