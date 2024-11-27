import javax.swing.*;
import java.sql.*;

public class InventoryManagementSystem {
    private static Connection conn;  // Declare the connection globally

    public static void main(String[] args) {
        // Set the Nimbus Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to the default look and feel
            System.err.println("Failed to initialize Nimbus Look and Feel");
            e.printStackTrace();
        }

        // Database connection info
        String url = "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC";
        String username = "root"; 
        String password = "root";

        // Establish the connection once, do not close it until the application ends
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the database successfully.");

            // Create and display the GUI
            SwingUtilities.invokeLater(() -> {
                InventoryGUI gui = new InventoryGUI(conn);
                gui.setVisible(true);
            });
            
            // Add a shutdown hook to ensure the connection is closed when the application exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                closeDatabaseConnection();
            }));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to close the database connection when the application ends
    private static void closeDatabaseConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection.");
            e.printStackTrace();
        }
    }
}
