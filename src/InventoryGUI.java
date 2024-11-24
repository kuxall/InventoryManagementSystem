import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

public class InventoryGUI extends JFrame {

    private JTextField txtItemId, txtItemName, txtQuantity, txtPrice, txtImagePath;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSelectImage;
    private JTable table;
    private DefaultTableModel tableModel;
    private JFileChooser fileChooser;

    // Database connection variables
    private Connection conn;

    public InventoryGUI() {
        setTitle("Inventory Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        createMenuBar();
        connect();
        loadTable();
        pack();
        setLocationRelativeTo(null);
    }

    // Method to establish a connection to the database
    private void connect() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Database credentials
            String url = "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC";
            String username = "root"; 
            String password = "root"; 
            // Establish the connection
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the database successfully.");

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "MySQL JDBC Driver not found.", "Driver Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to the database.\n" + e.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        }
    }

    // Method to load data from the database into the table
    private void loadTable() {
        try (PreparedStatement pst = conn.prepareStatement("SELECT * FROM inventory");
             ResultSet rs = pst.executeQuery()) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                String itemId = rs.getString("item_id");
                String itemName = rs.getString("item_name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double totalPrice = quantity * price;
                String imagePath = rs.getString("image_path");

                // Format price and totalPrice to two decimal places
                String priceStr = String.format("%.2f", price);
                String totalPriceStr = String.format("%.2f", totalPrice);

                // Add the row to the table model
                tableModel.addRow(new Object[]{
                    itemId,
                    itemName,
                    quantity,
                    priceStr,
                    totalPriceStr,
                    imagePath
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load data from the database.\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean validateFields() {
        // Validate Item ID
        String itemId = txtItemId.getText().trim();
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item ID cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!itemId.matches("^[a-zA-Z0-9_-]+$")) {
            JOptionPane.showMessageDialog(this, "Item ID can only contain letters, numbers, underscores, and hyphens.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (itemId.length() > 20) {
            JOptionPane.showMessageDialog(this, "Item ID cannot exceed 20 characters.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validate Item Name
        String itemName = txtItemName.getText().trim();
        if (itemName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (itemName.length() > 100) {
            JOptionPane.showMessageDialog(this, "Item Name cannot exceed 100 characters.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validate Quantity
        String quantityStr = txtQuantity.getText().trim();
        if (quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive integer greater than zero.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid integer.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validate Price
        String priceStr = txtPrice.getText().trim();
        if (priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price cannot be empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be a positive number greater than zero.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (priceStr.contains(".")) {
                int decimalPlaces = priceStr.substring(priceStr.indexOf('.') + 1).length();
                if (decimalPlaces > 2) {
                    JOptionPane.showMessageDialog(this, "Price cannot have more than two decimal places.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }
    
    private void initComponents() {
        // Main Panels
        JPanel panelInput = new JPanel(new GridBagLayout());
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JPanel panelTable = new JPanel(new BorderLayout(10, 10));

        // Set Borders with Titles
        panelInput.setBorder(BorderFactory.createTitledBorder("Item Details"));
        panelTable.setBorder(BorderFactory.createTitledBorder("Inventory Items"));

        // GridBagConstraints for Input Fields
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels and TextFields
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelInput.add(new JLabel("Item ID:"), gbc);

        gbc.gridx = 1;
        txtItemId = new JTextField(15);
        panelInput.add(txtItemId, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelInput.add(new JLabel("Item Name:"), gbc);

        gbc.gridx = 1;
        txtItemName = new JTextField(15);
        panelInput.add(txtItemName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelInput.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        txtQuantity = new JTextField(15);
        panelInput.add(txtQuantity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelInput.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        txtPrice = new JTextField(15);
        panelInput.add(txtPrice, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelInput.add(new JLabel("Image Path:"), gbc);

        gbc.gridx = 1;
        txtImagePath = new JTextField(15);
        txtImagePath.setEditable(false);
        panelInput.add(txtImagePath, gbc);

        gbc.gridx = 2;
        btnSelectImage = new JButton("Select Image");
        btnSelectImage.addActionListener(e -> selectImage());
        panelInput.add(btnSelectImage, gbc);

        // Adjust panelInput size
        panelInput.setPreferredSize(new Dimension(300, getHeight()));

        // Buttons with Icons (optional)
        btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> addItem());
        btnAdd.setToolTipText("Add a new item to the inventory");

        btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(e -> updateItem());
        btnUpdate.setToolTipText("Update the selected item");

        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteItem());
        btnDelete.setToolTipText("Delete the selected item");

        btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> clearFields());
        btnClear.setToolTipText("Clear input fields");

        panelButtons.add(btnAdd);
        panelButtons.add(btnUpdate);
        panelButtons.add(btnDelete);
        panelButtons.add(btnClear);

        // Table with Sorting and Filtering
        tableModel = new DefaultTableModel(new Object[]{"Item ID", "Item Name", "Quantity", "Price", "Total Price", "Image Path"}, 0);
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // Enable sorting
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    txtItemId.setText(table.getValueAt(row, 0).toString());
                    txtItemName.setText(table.getValueAt(row, 1).toString());
                    txtQuantity.setText(table.getValueAt(row, 2).toString());
                    txtPrice.setText(table.getValueAt(row, 3).toString());
                    txtImagePath.setText(table.getValueAt(row, 5).toString());
                }
            }
        });


        // Add a search field
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSearch.add(new JLabel("Search:"));
        JTextField txtSearch = new JTextField(20);
        panelSearch.add(txtSearch);

        // Implementing Row Filter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                search(txtSearch.getText(), sorter);
            }

            public void removeUpdate(DocumentEvent e) {
                search(txtSearch.getText(), sorter);
            }

            public void changedUpdate(DocumentEvent e) {
                search(txtSearch.getText(), sorter);
            }
        });

        panelTable.add(panelSearch, BorderLayout.NORTH);
        panelTable.add(new JScrollPane(table), BorderLayout.CENTER);

        // Set up the main frame layout
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(10, 10));

        // Add panels to the frame
        contentPane.add(panelInput, BorderLayout.WEST);
        contentPane.add(panelButtons, BorderLayout.SOUTH);
        contentPane.add(panelTable, BorderLayout.CENTER);
    }

    private boolean validateFields() {
        if (txtItemId.getText().trim().isEmpty() ||
                txtItemName.getText().trim().isEmpty() ||
                txtQuantity.getText().trim().isEmpty() ||
                txtPrice.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Incomplete Data", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity < 0) {
                JOptionPane.showMessageDialog(this, "Quantity cannot be negative.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be an integer.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem miExport = new JMenuItem("Export to CSV");
        miExport.addActionListener(e -> exportToCSV());
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));
        menuFile.add(miExport);
        menuFile.addSeparator();
        menuFile.add(miExit);

        menuBar.add(menuFile);

        setJMenuBar(menuBar);
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = new File(fileChooser.getSelectedFile() + ".csv");
            if (file.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                // Write column names
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    pw.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        pw.print(",");
                    }
                }
                pw.println();
                // Write rows
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        pw.print(tableModel.getValueAt(i, j).toString());
                        if (j < tableModel.getColumnCount() - 1) {
                            pw.print(",");
                        }
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Data exported successfully.", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export data.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void search(String query, TableRowSorter<DefaultTableModel> sorter) {
        if (query.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void addItem() {
        if (validateFields()) {
            try (PreparedStatement pst = conn.prepareStatement("INSERT INTO inventory(item_id, item_name, quantity, price, image_path) VALUES(?,?,?,?,?)")) {
                pst.setString(1, txtItemId.getText().trim());
                pst.setString(2, txtItemName.getText().trim());
                pst.setInt(3, Integer.parseInt(txtQuantity.getText().trim()));
                pst.setDouble(4, Double.parseDouble(txtPrice.getText().trim()));
                pst.setString(5, txtImagePath.getText().trim());
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item added successfully.");
                loadTable();
                clearFields();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to add item. Item ID might already exist.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void updateItem() {
        if (validateFields()) {
            try (PreparedStatement pst = conn.prepareStatement("UPDATE inventory SET item_name=?, quantity=?, price=?, image_path=? WHERE item_id=?")) {
                pst.setString(1, txtItemName.getText().trim());
                pst.setInt(2, Integer.parseInt(txtQuantity.getText().trim()));
                pst.setDouble(3, Double.parseDouble(txtPrice.getText().trim()));
                pst.setString(4, txtImagePath.getText().trim());
                pst.setString(5, txtItemId.getText().trim());
                int affectedRows = pst.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Item updated successfully.");
                    loadTable();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Item ID not found.", "Update Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to update item.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void deleteItem() {
        if (!txtItemId.getText().trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (PreparedStatement pst = conn.prepareStatement("DELETE FROM inventory WHERE item_id=?")) {
                    pst.setString(1, txtItemId.getText().trim());
                    int affectedRows = pst.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Item deleted successfully.");
                        loadTable();
                        clearFields();
                    } else {
                        JOptionPane.showMessageDialog(this, "Item ID not found.", "Deletion Failed", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete item.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearFields() {
        txtItemId.setText("");
        txtItemName.setText("");
        txtQuantity.setText("");
        txtPrice.setText("");
        txtImagePath.setText("");
        table.clearSelection();
    }

    private void selectImage() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            txtImagePath.setText(file.getAbsolutePath());
        }
    }
}
