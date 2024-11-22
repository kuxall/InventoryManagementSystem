import javax.swing.*;

public class InventoryManagementSystem {
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

        // Create and display the GUI
        SwingUtilities.invokeLater(() -> {
            InventoryGUI gui = new InventoryGUI();
            gui.setVisible(true);
        });
    }
}
