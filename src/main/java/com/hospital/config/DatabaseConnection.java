package com.hospital.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to manage MySQL database connections via JDBC.
 * Loads connection configuration from src/main/resources/db.properties.
 */
public class DatabaseConnection {

    // Properties instance to hold database credentials
    private static final Properties properties = new Properties();
    private static boolean isConfigLoaded = false;

    // Private constructor to prevent direct instantiation
    private DatabaseConnection() {}

    /**
     * Loads database configuration properties from the classpath.
     * Looks for 'db.properties' in src/main/resources/.
     */
    private static synchronized void loadProperties() {
        if (isConfigLoaded) {
            return;
        }

        try (InputStream inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException(
                        "Configuration file 'db.properties' was not found in src/main/resources/.\n" +
                        "Please create 'src/main/resources/db.properties' with the following format:\n\n" +
                        "db.url=jdbc:mysql://localhost:3306/hospital_db\n" +
                        "db.username=your_mysql_username\n" +
                        "db.password=your_mysql_password\n"
                );
            }

            properties.load(inputStream);
            isConfigLoaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read database configuration from 'db.properties'.", e);
        }
    }

    /**
     * Establishes and returns a new JDBC Connection to the MySQL database.
     *
     * @return an open {@link Connection}
     * @throws SQLException if a database connection error occurs
     */
    public static Connection getConnection() throws SQLException {
        loadProperties();

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        if (username == null || username.trim().isEmpty()) {
            username = properties.getProperty("db.user");
        }
        String password = properties.getProperty("db.password");

        // Validate presence of required properties
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Missing required property 'db.url' in db.properties.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Missing required property 'db.username' in db.properties.");
        }
        if (password == null) {
            throw new IllegalStateException("Missing required property 'db.password' in db.properties.");
        }

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Quick test method to verify connectivity directly from this class.
     */
    public static void main(String[] args) {
        System.out.println("Testing database connection from DatabaseConnection...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Successfully connected to MySQL database!");
                System.out.println("Connected URL: " + conn.getMetaData().getURL());
            }
        } catch (IllegalStateException e) {
            System.err.println("❌ Configuration error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }
}

