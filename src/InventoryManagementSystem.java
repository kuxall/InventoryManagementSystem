import javax.swing.*;

public class InventoryManagementSystem {
    public static void main(String[] args) {
        // Set the Nimbus Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLook and Feels()) {
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

        // Create and display the login screen
        SwingUtilities.invokeLater(() -> {
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });
    }
}
