import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class InventoryGUI extends JFrame {
    private String currentUserRole;
    private Connection conn;

    private JButton btnAdd, btnUpdate, btnDelete, btnExportCSV;
    private JTable table;
    private JTextField searchField;
    private JLabel lblImagePreview;
    private JTextField txtImagePath;
    private JFileChooser fileChooser;

    public InventoryGUI(Connection conn) {
        this.conn = conn;

        setTitle("Inventory Management System");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Handle window close to close the database connection
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                        System.out.println("Database connection closed.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Create buttons panel
        JPanel panel = new JPanel();
        btnAdd = new JButton("Add Item");
        btnUpdate = new JButton("Update Item");
        btnDelete = new JButton("Delete Item");
        btnExportCSV = new JButton("Export to CSV");
        searchField = new JTextField(20);
        lblImagePreview = new JLabel("Image Preview", JLabel.CENTER);
        txtImagePath = new JTextField(20);
        txtImagePath.setEditable(false);

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(btnExportCSV);

        add(panel, BorderLayout.NORTH);

        // Table for inventory items
        String[] columnNames = {"ID", "Item Name", "Category", "Quantity", "Price", "Image Path"};
        table = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        lblImagePreview.setPreferredSize(new Dimension(150, 150));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(lblImagePreview);

        // Show login dialog
        showLogin();

        // Button Actions
        btnAdd.addActionListener(e -> addItem());
        btnUpdate.addActionListener(e -> updateItem());
        btnDelete.addActionListener(e -> deleteItem());
        btnExportCSV.addActionListener(e -> exportToCSV());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchItems(); }
            public void removeUpdate(DocumentEvent e) { searchItems(); }
            public void changedUpdate(DocumentEvent e) { searchItems(); }
        });
    }

    // Login Dialog
    private void showLogin() {
        LoginManager loginManager = new LoginManager(conn);
        String role = loginManager.showLoginDialog();
        if (role != null) {
            currentUserRole = role;
            enableAdminFeatures(role.equals("admin"));
            refreshTable();
        } else {
            System.exit(0); // Exit on failed login
        }
    }

    // Enable features based on user role
    private void enableAdminFeatures(boolean isAdmin) {
        btnAdd.setEnabled(isAdmin);
        btnUpdate.setEnabled(isAdmin);
        btnDelete.setEnabled(isAdmin);
        btnExportCSV.setEnabled(isAdmin);
        table.setEnabled(isAdmin);
    }

    // Refresh table with data from database
    private void refreshTable() {
        try {
            String query = "SELECT * FROM inventory";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                table.setModel(buildTableModel(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add Item
    private void addItem() {
        if (!"admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to add items.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }


        String itemName = JOptionPane.showInputDialog(this, "Enter item name:");
        String category = JOptionPane.showInputDialog(this, "Enter item category:");
        String quantity = JOptionPane.showInputDialog(this, "Enter item quantity:");
        String price = JOptionPane.showInputDialog(this, "Enter item price:");
        String threshold = JOptionPane.showInputDialog(this, "Enter inventory threshold:");


        if (fileChooser == null) {
        	fileChooser = new JFileChooser();
        	fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
        }
        
        if (threshold == null || threshold.isEmpty()) {
            threshold = "0"; // Default to 0 if not provided
        }
        
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
        	File file = fileChooser.getSelectedFile();
        	txtImagePath.setText(file.getAbsolutePath());
        	lblImagePreview.setIcon(resizeImage(file.getAbsolutePath()));
        }
        if (itemName == null || category == null || quantity == null || price == null || itemName.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "INSERT INTO inventory (item_name, category, quantity, price, image_path, threshold) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, itemName);
                pst.setString(2, category);
                pst.setInt(3, Integer.parseInt(quantity));
                pst.setDouble(4, Double.parseDouble(price));
                pst.setString(5, txtImagePath.getText());
                pst.setInt(6, Integer.parseInt(threshold));

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item added successfully.");
                refreshTable();
                checkLowInventory();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update Item
    private void updateItem() {
        if (!"admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to update items.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "No item selected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String itemId = table.getValueAt(selectedRow, 0).toString();
        String newQuantity = JOptionPane.showInputDialog(this, "Enter new quantity:");
        String newPrice = JOptionPane.showInputDialog(this, "Enter new price:");
        String newThreshold = JOptionPane.showInputDialog(this, "Enter new threshold value:", currentthreshold);


        if (newQuantity == null || newPrice == null) {
            JOptionPane.showMessageDialog(this, "Update canceled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
       
        try {
            String query = "UPDATE inventory SET quantity = ?, price = ?, image_path = ? WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
            	if (newThreshold == null) newThreshold = "0";
             	pst.setInt(4, Integer.parseInt(newThreshold)); 
                pst.setInt(1, Integer.parseInt(newQuantity));
                pst.setDouble(2, Double.parseDouble(newPrice));
                pst.setString(3, txtImagePath.getText()); 
                pst.setInt(4, Integer.parseInt(itemId));
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item updated successfully.");
                refreshTable();
                checkLowInventory();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Delete Item
    private void deleteItem() {
        if (!"admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to delete items.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No item selected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String itemId = table.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM inventory WHERE id = ?";
                try (PreparedStatement pst = conn.prepareStatement(query)) {
                    pst.setInt(1, Integer.parseInt(itemId));
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Item deleted successfully.");
                    refreshTable();
                    checkLowInventory();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    private void checkLowInventory() {
        try {
            String query = "SELECT item_name, quantity, threshold FROM inventory WHERE quantity < threshold";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                StringBuilder alertMessage = new StringBuilder();
                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    int quantity = rs.getInt("quantity");
                    int threshold = rs.getInt("threshold");
                    alertMessage.append(String.format("Item: %s is below threshold. Quantity: %d, Threshold: %d%n", itemName, quantity, threshold));
                }
                if (alertMessage.length() > 0) {
                    JOptionPane.showMessageDialog(this, alertMessage.toString(), "Low Inventory Alert", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking inventory levels: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Export to CSV
    private void exportToCSV() {
        if (!"admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to export data.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = new File(fileChooser.getSelectedFile() + ".csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    pw.print(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        pw.print(model.getValueAt(i, j));
                        if (j < model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Data exported successfully.", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Search Items
    private void searchItems() {
        String searchText = searchField.getText();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);
        sorter.setRowFilter(searchText.isEmpty() ? null : RowFilter.regexFilter("(?i)" + searchText));
    }

    // Resize image for preview
    private ImageIcon resizeImage(String imagePath) {
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image img = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // Build Table Model from ResultSet
    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int column = 1; column <= columnCount; column++) {
                row.add(rs.getObject(column));
            }
            data.add(row);
        }

        return new DefaultTableModel(data, columnNames);
    }
}
