package com.hospital;

import com.hospital.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Temporary test runner to verify MySQL database connectivity.
 * Can be executed directly in IntelliJ IDEA via the green Run arrow next to main().
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Hospital Management System");
        System.out.println("  Testing MySQL Database Connection");
        System.out.println("========================================");

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("✅ Connection Successful!");
                System.out.println("Database Product : " + metaData.getDatabaseProductName());
                System.out.println("Database Version : " + metaData.getDatabaseProductVersion());
                System.out.println("Driver Name      : " + metaData.getDriverName());
                System.out.println("Driver Version   : " + metaData.getDriverVersion());
                System.out.println("Connected User   : " + metaData.getUserName());
                System.out.println("Connected URL    : " + metaData.getURL());
                System.out.println("========================================");
            }
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Configuration Error:");
            System.err.println(e.getMessage());
        } catch (SQLException e) {
            System.err.println("\n❌ Database Connection Error:");
            System.err.println("Message   : " + e.getMessage());
            System.err.println("SQL State : " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
        } catch (Exception e) {
            System.err.println("\n❌ Unexpected Error:");
            e.printStackTrace();
        }
    }
}

