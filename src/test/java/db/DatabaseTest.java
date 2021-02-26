package db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joergen Bele Reinfjell
 */
public class DatabaseTest {
    @Before
    public void setUp() throws Exception {
        Database.closeAvailableConnections();
        assertFalse(Database.hasAvailableConnections());
        assertFalse(Database.hasActiveConnections());
    }

    @After
    public void tearDown() throws Exception {
        Database.closeAvailableConnections();
        assertFalse(Database.hasAvailableConnections());
        assertFalse(Database.hasActiveConnections());
    }

    @Test
    public void closeAvailableConnections() throws Exception{
        /* Gets and creates a connection, then releases back into the queue. */
        try (DatabaseConnection con = Database.getConnection()) {
            assertTrue(Database.hasActiveConnections());
        }
        assertTrue(Database.hasAvailableConnections());
        //assertFalse(Database.hasActiveConnections());
        Database.closeAvailableConnections();
        assertFalse(Database.hasAvailableConnections());
        assertFalse(Database.hasActiveConnections());
    }

    @Test
    public void getConnection() throws Exception {
        try (DatabaseConnection con = Database.getConnection()) {
            assertTrue(Database.hasActiveConnections());
        }
        assertTrue(Database.hasAvailableConnections());
    }

    @Test
    public void hasActiveConnections() throws Exception {
        /* Gets and creates a connection, then releases back into the queue. */
        try (DatabaseConnection con = Database.getConnection()) {
            assertTrue(Database.hasActiveConnections());
        }
        assertTrue(Database.hasAvailableConnections());
    }

    @Test
    public void hasAvailableConnections() throws Exception {
        /* Gets and creates a connection, then releases back into the queue. */
        try (DatabaseConnection con = Database.getConnection()) {
            //assertTrue(Database.hasActiveConnections());
        }
        assertTrue(Database.hasAvailableConnections());
    }

    @Test
    public void releaseConnection() throws Exception {
        /* Gets and creates a connection, then releases back into the queue. */
        try (DatabaseConnection con = Database.getConnection()) {
            assertTrue(Database.hasActiveConnections());
        }
        assertTrue(Database.hasAvailableConnections());
        assertTrue(Database.hasActiveConnections());
    }
}