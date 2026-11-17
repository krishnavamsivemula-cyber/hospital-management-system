package com.hospital;

import com.hospital.config.DatabaseConnection;
import com.hospital.ui.MainFrame;

import javax.swing.*;

/**
 * Main application entry point for the Hospital Management System.
 */
public class Main {

    public static void main(String[] args) {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing look and feel
        }

        // Initialize/verify database schema safely (creates missing tables without data
        // loss)
        try {
            DatabaseConnection.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database auto-initialization notice: " + e.getMessage());
        }

        // Launch application GUI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception ex) {
                System.err.println("Fatal error starting application: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch application:\n" + ex.getMessage() +
                                "\n\nPlease ensure MySQL is running and src/main/resources/db.properties is configured properly.",
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
