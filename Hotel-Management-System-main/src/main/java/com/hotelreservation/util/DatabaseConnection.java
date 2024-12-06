package com.hotelreservation.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Utility class for establishing a connection to the database.
 * Loads database connection details from environment variables and provides a method to get a connection to the PostgreSQL database.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    // Load environment variables
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("SUPABASE_DB_URL");
    private static final String USER = dotenv.get("SUPABASE_DB_USER");
    private static final String PASSWORD = dotenv.get("SUPABASE_DB_PASSWORD");

    /**
     * Establishes a connection to the PostgreSQL database using credentials from environment variables.
     *
     * @return a {@link Connection} object to interact with the database
     * @throws SQLException if there is an error in establishing the database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            logger.info("Connecting to database...");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL JDBC Driver not found", e);
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }
}
