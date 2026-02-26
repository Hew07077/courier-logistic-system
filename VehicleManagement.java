package logistics.login.admin.management;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.swing.RowFilter;
import javax.swing.border.*;

public class VehicleManagement {
    private JPanel mainPanel;
    private JTable vehiclesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Vehicle> vehicles;
    private List<Driver> drivers;
    private Map<String, Integer> typeCounters;
    
    // File paths
    private static final String VEHICLES_FILE = "vehicles_data.txt";
    private static final String DRIVERS_FILE = "drivers_data.txt";
    private static final String COUNTERS_FILE = "counters_data.txt";
    
    // UI Components
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> typeFilter;
    private JLabel activeCountLabel;
    private JLabel maintenanceCountLabel;
    private JLabel expiredRoadTaxLabel;
    private JLabel totalCountLabel;
    private JLabel availableDriversLabel;
    
    // Color scheme - Modern Professional
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color WARNING_COLOR = new Color(237, 108, 2);
    private static final Color DANGER_COLOR = new Color(198, 40, 40);
    private static final Color INFO_COLOR = new Color(2, 136, 209);
    private static final Color LIGHT_BG = new Color(250, 250, 250);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public interface DriverProfileListener {
        void onDriverProfileClicked(String driverName);
    }
    
    private DriverProfileListener driverProfileListener;
    
    public VehicleManagement() {
        vehicles = new ArrayList<>();
        drivers = new ArrayList<>();
        typeCounters = new HashMap<>();
        loadDataFromFiles();
        createMainPanel();
    }
    
    public void setDriverProfileListener(DriverProfileListener listener) {
        this.driverProfileListener = listener;
    }
    
    private void loadDataFromFiles() {
        loadVehiclesFromFile();
        loadDriversFromFile();
        loadCountersFromFile();
    }
    
    private void loadVehiclesFromFile() {
        vehicles.clear();
        File file = new File(VEHICLES_FILE);
        
        if (!file.exists()) {
            createSampleDataFile();
            loadVehiclesFromFile();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("//")) {
                    Vehicle vehicle = parseVehicleFromLine(line);
                    if (vehicle != null) {
                        vehicles.add(vehicle);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error loading vehicles: " + e.getMessage());
        }
    }
    
    private Vehicle parseVehicleFromLine(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 8) {
                String vehicleId = parts[0].trim();
                String model = parts[1].trim();
                String status = parts[2].trim();
                String driverName = parts[3].trim().isEmpty() ? null : parts[3].trim();
                String type = parts[4].trim();
                String numberPlate = parts[5].trim();
                Date roadTaxExpiry = dateFormat.parse(parts[6].trim());
                String fuelType = parts[7].trim();
                
                return new Vehicle(vehicleId, model, status, driverName, type, 
                                 numberPlate, roadTaxExpiry, fuelType);
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
        }
        return null;
    }
    
    private void loadDriversFromFile() {
        drivers.clear();
        File file = new File(DRIVERS_FILE);
        
        if (!file.exists()) {
            createDriversFile();
            loadDriversFromFile();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("//")) {
                    Driver driver = parseDriverFromLine(line);
                    if (driver != null) {
                        drivers.add(driver);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Driver parseDriverFromLine(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 7) {
                String name = parts[0].trim();
                String licenseNumber = parts[1].trim();
                String phone = parts[2].trim();
                String email = parts[3].trim();
                String status = parts[4].trim();
                Date joinedDate = dateFormat.parse(parts[5].trim());
                String currentVehicle = parts[6].trim().isEmpty() ? null : parts[6].trim();
                
                return new Driver(name, licenseNumber, phone, email, status, joinedDate, currentVehicle);
            }
        } catch (Exception e) {
            System.err.println("Error parsing driver line: " + line);
        }
        return null;
    }
    
    private void loadCountersFromFile() {
        typeCounters.clear();
        File file = new File(COUNTERS_FILE);
        
        if (!file.exists()) {
            createCountersFile();
            loadCountersFromFile();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("//")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        typeCounters.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createSampleDataFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE))) {
            writer.write("// Format: ID|Model|Status|Driver|Type|NumberPlate|RoadTaxExpiry|FuelType");
            writer.newLine();
            writer.write("TRK001|Freightliner Cascadia|Active|John Smith|Truck|ABC 1234|2024-12-31|Diesel");
            writer.newLine();
            writer.write("VAN001|Ford Transit|Active|Mike Johnson|Van|DEF 5678|2024-10-15|Gasoline");
            writer.newLine();
            writer.write("TRK002|Peterbilt 579|Maintenance||Truck|GHI 9012|2023-05-30|Diesel");
            writer.newLine();
            writer.write("VAN002|Mercedes Sprinter|Active|Sarah Wilson|Van|JKL 3456|2024-08-20|Diesel");
            writer.newLine();
            writer.write("TRK003|International LT|Inactive||Truck|MNO 7890|2023-11-10|Diesel");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createDriversFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DRIVERS_FILE))) {
            writer.write("// Format: Name|LicenseNumber|Phone|Email|Status|JoinedDate|CurrentVehicle");
            writer.newLine();
            writer.write("John Smith|DL12345|555-0101|john.smith@email.com|Active|2023-01-15|TRK001");
            writer.newLine();
            writer.write("Mike Johnson|DL12346|555-0102|mike.j@email.com|Active|2023-02-20|VAN001");
            writer.newLine();
            writer.write("Sarah Wilson|DL12347|555-0103|sarah.w@email.com|Active|2023-03-10|VAN002");
            writer.newLine();
            writer.write("Robert Brown|DL12348|555-0104|robert.b@email.com|Available|2023-04-05|");
            writer.newLine();
            writer.write("Emily Davis|DL12349|555-0105|emily.d@email.com|Available|2023-05-12|");
            writer.newLine();
            writer.write("Unassigned||||Available||");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createCountersFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNTERS_FILE))) {
            writer.write("// Type counters for generating new IDs");
            writer.newLine();
            writer.write("Truck=3");
            writer.newLine();
            writer.write("Van=3");
            writer.newLine();
            writer.write("Car=0");
            writer.newLine();
            writer.write("Motorcycle=0");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveVehiclesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE))) {
            writer.write("// Format: ID|Model|Status|Driver|Type|NumberPlate|RoadTaxExpiry|FuelType");
            writer.newLine();
            
            for (Vehicle vehicle : vehicles) {
                String driverName = vehicle.getDriverName() != null ? vehicle.getDriverName() : "";
                String line = String.format("%s|%s|%s|%s|%s|%s|%s|%s",
                    vehicle.getVehicleId(),
                    vehicle.getModel(),
                    vehicle.getStatus(),
                    driverName,
                    vehicle.getType(),
                    vehicle.getNumberPlate(),
                    dateFormat.format(vehicle.getRoadTaxExpiry()),
                    vehicle.getFuelType()
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error saving vehicles: " + e.getMessage());
        }
    }
    
    private void saveDriversToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DRIVERS_FILE))) {
            writer.write("// Format: Name|LicenseNumber|Phone|Email|Status|JoinedDate|CurrentVehicle");
            writer.newLine();
            
            for (Driver driver : drivers) {
                String currentVehicle = driver.getCurrentVehicle() != null ? driver.getCurrentVehicle() : "";
                String line = String.format("%s|%s|%s|%s|%s|%s|%s",
                    driver.getName(),
                    driver.getLicenseNumber(),
                    driver.getPhone(),
                    driver.getEmail(),
                    driver.getStatus(),
                    dateFormat.format(driver.getJoinedDate()),
                    currentVehicle
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveCountersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNTERS_FILE))) {
            writer.write("// Type counters for generating new IDs");
            writer.newLine();
            
            for (Map.Entry<String, Integer> entry : typeCounters.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String generateVehicleId(String type) {
        String prefix;
        switch (type) {
            case "Truck": prefix = "TRK"; break;
            case "Van": prefix = "VAN"; break;
            case "Car": prefix = "CAR"; break;
            case "Motorcycle": prefix = "MTC"; break;
            default: prefix = "VEH";
        }
        
        int counter = typeCounters.getOrDefault(type, 0) + 1;
        typeCounters.put(type, counter);
        saveCountersToFile();
        return String.format("%s%03d", prefix, counter);
    }
    
    private List<String> getAvailableDriverNames() {
        List<String> names = new ArrayList<>();
        names.add("Unassigned");
        for (Driver driver : drivers) {
            if (!driver.getName().equals("Unassigned")) {
                names.add(driver.getName());
            }
        }
        return names;
    }
    
    private Driver findDriverByName(String name) {
        for (Driver driver : drivers) {
            if (driver.getName().equals(name)) {
                return driver;
            }
        }
        return null;
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(LIGHT_BG);
        
        // Top Panel with Title and Stats
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(LIGHT_BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(LIGHT_BG);
        
        JLabel titleLabel = new JLabel("Fleet Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Vehicle & Driver Management");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(subtitleLabel);
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        
        // Stats Panel - Modern Cards
        JPanel statsPanel = createStatsPanel();
        topPanel.add(statsPanel, BorderLayout.EAST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center Panel with Table and Filters
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(LIGHT_BG);
        
        // Filter Panel
        JPanel filterPanel = createFilterPanel();
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Table with modern styling
        createVehicleTable();
        JScrollPane scrollPane = new JScrollPane(vehiclesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        
        activeCountLabel = createStatCard("Active Vehicles", String.valueOf(getActiveCount()), SUCCESS_COLOR);
        maintenanceCountLabel = createStatCard("In Maintenance", String.valueOf(getMaintenanceCount()), WARNING_COLOR);
        expiredRoadTaxLabel = createStatCard("Expired Tax", String.valueOf(getExpiredRoadTaxCount()), DANGER_COLOR);
        totalCountLabel = createStatCard("Total Vehicles", String.valueOf(getTotalCount()), PRIMARY_COLOR);
        availableDriversLabel = createStatCard("Available Drivers", String.valueOf(getAvailableDriversCount()), INFO_COLOR);
        
        statsPanel.add(activeCountLabel);
        statsPanel.add(maintenanceCountLabel);
        statsPanel.add(expiredRoadTaxLabel);
        statsPanel.add(totalCountLabel);
        statsPanel.add(availableDriversLabel);
        
        return statsPanel;
    }
    
    private JLabel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        
        JLabel wrapper = new JLabel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(card, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Search Field
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.setBackground(Color.WHITE);
        
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search vehicles...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        searchPanel.add(searchField);
        filterPanel.add(searchPanel);
        
        // Status Filter
        filterPanel.add(createFilterLabel("Status:"));
        String[] statuses = {"All Status", "Active", "Maintenance", "Inactive"};
        statusFilter = new JComboBox<>(statuses);
        styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> filterTable());
        filterPanel.add(statusFilter);
        
        // Type Filter
        filterPanel.add(createFilterLabel("Type:"));
        String[] types = {"All Types", "Truck", "Van", "Car", "Motorcycle"};
        typeFilter = new JComboBox<>(types);
        styleComboBox(typeFilter);
        typeFilter.addActionListener(e -> {
            String selectedType = (String) typeFilter.getSelectedItem();
            filterByType(selectedType);
        });
        filterPanel.add(typeFilter);
        
        // Clear Filters Button
        JButton clearFilters = new JButton("Clear Filters");
        clearFilters.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearFilters.setForeground(TEXT_SECONDARY);
        clearFilters.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearFilters.setBackground(Color.WHITE);
        clearFilters.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearFilters.addActionListener(e -> clearFilters());
        filterPanel.add(clearFilters);
        
        return filterPanel;
    }
    
    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        comboBox.setPreferredSize(new Dimension(120, 35));
    }
    
    private void createVehicleTable() {
        String[] columns = {"ID", "Model", "Type", "Plate", "Road Tax", "Fuel", "Status", "Driver"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Date.class;
                return String.class;
            }
        };
        
        vehiclesTable = new JTable(tableModel);
        vehiclesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vehiclesTable.setRowHeight(50);
        vehiclesTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
        vehiclesTable.setSelectionForeground(TEXT_PRIMARY);
        vehiclesTable.setShowGrid(true);
        vehiclesTable.setGridColor(BORDER_COLOR);
        vehiclesTable.setIntercellSpacing(new Dimension(10, 5));
        
        // Modern table header
        JTableHeader header = vehiclesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(Color.WHITE);
        header.setForeground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        
        // Set column widths
        vehiclesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        vehiclesTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        vehiclesTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        vehiclesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        vehiclesTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        
        // Set up row sorter
        rowSorter = new TableRowSorter<>(tableModel);
        vehiclesTable.setRowSorter(rowSorter);
        
        // Add custom cell renderers
        vehiclesTable.getColumnModel().getColumn(4).setCellRenderer(new DateCellRenderer());
        vehiclesTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        vehiclesTable.getColumnModel().getColumn(7).setCellRenderer(new DriverCellRenderer());
        
        // Add mouse listener for double-click actions
        vehiclesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = vehiclesTable.getSelectedRow();
                    int col = vehiclesTable.getSelectedColumn();
                    
                    if (row != -1) {
                        if (col == 7) { // Driver column
                            int modelRow = vehiclesTable.convertRowIndexToModel(row);
                            Vehicle vehicle = vehicles.get(modelRow);
                            String driverName = vehicle.getDriverName();
                            
                            if (driverName != null && !driverName.equals("Unassigned")) {
                                if (driverProfileListener != null) {
                                    driverProfileListener.onDriverProfileClicked(driverName);
                                } else {
                                    JOptionPane.showMessageDialog(mainPanel, 
                                        "Driver: " + driverName,
                                        "Driver Information",
                                        JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        } else {
                            showVehicleDetails(row);
                        }
                    }
                }
            }
        });
        
        refreshTableData();
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(LIGHT_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton addButton = createModernButton("Add Vehicle", PRIMARY_COLOR);
        JButton editButton = createModernButton("Edit Vehicle", new Color(255, 152, 0));
        JButton assignButton = createModernButton("Assign Driver", SUCCESS_COLOR);
        JButton unassignButton = createModernButton("Unassign", DANGER_COLOR);
        JButton maintenanceButton = createModernButton("Maintenance", WARNING_COLOR);
        JButton roadTaxButton = createModernButton("Update Tax", INFO_COLOR);
        
        addButton.addActionListener(this::showAddVehicleDialog);
        editButton.addActionListener(this::showEditVehicleDialog);
        assignButton.addActionListener(this::showAssignDriverDialog);
        unassignButton.addActionListener(this::unassignDriver);
        maintenanceButton.addActionListener(this::toggleMaintenanceStatus);
        roadTaxButton.addActionListener(this::showUpdateRoadTaxDialog);
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(unassignButton);
        buttonPanel.add(maintenanceButton);
        buttonPanel.add(roadTaxButton);
        
        return buttonPanel;
    }
    
    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 38));
        
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void refreshTableData() {
        tableModel.setRowCount(0);
        for (Vehicle vehicle : vehicles) {
            tableModel.addRow(new Object[]{
                vehicle.getVehicleId(),
                vehicle.getModel(),
                vehicle.getType(),
                vehicle.getNumberPlate(),
                vehicle.getRoadTaxExpiry(),
                vehicle.getFuelType(),
                vehicle.getStatus(),
                vehicle.getDriverName() != null ? vehicle.getDriverName() : "Unassigned"
            });
        }
        updateStats();
    }
    
    private void filterByType(String type) {
        if ("All Types".equals(type)) {
            filterTable();
        } else {
            List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
            filters.add(RowFilter.regexFilter(type, 2));
            
            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + searchText, 0, 1, 2, 3, 7));
            }
            
            String status = (String) statusFilter.getSelectedItem();
            if (!"All Status".equals(status)) {
                filters.add(RowFilter.regexFilter(status, 6));
            }
            
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }
    
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        String type = (String) typeFilter.getSelectedItem();
        
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 0, 1, 2, 3, 7));
        }
        
        if (!"All Status".equals(status)) {
            filters.add(RowFilter.regexFilter(status, 6));
        }
        
        if (!"All Types".equals(type)) {
            filters.add(RowFilter.regexFilter(type, 2));
        }
        
        if (!filters.isEmpty()) {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            rowSorter.setRowFilter(null);
        }
    }
    
    private void clearFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        typeFilter.setSelectedIndex(0);
        filterTable();
    }
    
    private void showDriverProfile(String driverName) {
        if (driverProfileListener != null) {
            driverProfileListener.onDriverProfileClicked(driverName);
        } else {
            JOptionPane.showMessageDialog(mainPanel, 
                "Driver: " + driverName,
                "Driver Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showVehicleDetails(int row) {
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        Vehicle vehicle = vehicles.get(modelRow);
        showVehicleDetails(vehicle);
    }
    
    private void showVehicleDetails(Vehicle vehicle) {
        String expiryStatus = vehicle.isRoadTaxExpired() ? "Expired" : "Valid";
        Color expiryColor = vehicle.isRoadTaxExpired() ? DANGER_COLOR : SUCCESS_COLOR;
        
        JDialog dialog = createModernDialog("Vehicle Details - " + vehicle.getVehicleId(), 500, 500);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel idLabel = new JLabel(vehicle.getVehicleId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        idLabel.setForeground(PRIMARY_COLOR);
        
        JLabel modelLabel = new JLabel(vehicle.getModel());
        modelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        modelLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(idLabel);
        titlePanel.add(modelLabel);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 1;
        
        addDetailRow(detailsPanel, gbc, "Type:", vehicle.getType(), 0);
        addDetailRow(detailsPanel, gbc, "Number Plate:", vehicle.getNumberPlate(), 1);
        addDetailRow(detailsPanel, gbc, "Fuel Type:", vehicle.getFuelType(), 2);
        
        // Road Tax with color
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel taxLabel = new JLabel("Road Tax:");
        taxLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        taxLabel.setForeground(TEXT_SECONDARY);
        detailsPanel.add(taxLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel taxValue = new JLabel(dateFormat.format(vehicle.getRoadTaxExpiry()) + " (" + expiryStatus + ")");
        taxValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taxValue.setForeground(expiryColor);
        detailsPanel.add(taxValue, gbc);
        
        // Status with color
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(TEXT_SECONDARY);
        detailsPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1;
        JLabel statusValue = new JLabel(vehicle.getStatus());
        statusValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusValue.setForeground(getStatusColor(vehicle.getStatus()));
        detailsPanel.add(statusValue, gbc);
        
        // Driver info
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel driverLabel = new JLabel("Driver:");
        driverLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        driverLabel.setForeground(TEXT_SECONDARY);
        detailsPanel.add(driverLabel, gbc);
        
        gbc.gridx = 1;
        String driverName = vehicle.getDriverName() != null ? vehicle.getDriverName() : "Unassigned";
        JLabel driverValue = new JLabel(driverName);
        driverValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        driverValue.setForeground(vehicle.getDriverName() != null ? PRIMARY_COLOR : TEXT_SECONDARY);
        detailsPanel.add(driverValue, gbc);
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        if (vehicle.getDriverName() != null && !vehicle.getDriverName().equals("Unassigned")) {
            JButton viewDriverButton = createModernButton("View Driver", PRIMARY_COLOR);
            viewDriverButton.setPreferredSize(new Dimension(120, 35));
            viewDriverButton.addActionListener(e -> {
                dialog.dispose();
                showDriverProfile(vehicle.getDriverName());
            });
            buttonPanel.add(viewDriverButton);
        }
        
        JButton closeButton = createModernButton("Close", TEXT_SECONDARY);
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComp.setForeground(TEXT_SECONDARY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueComp.setForeground(TEXT_PRIMARY);
        panel.add(valueComp, gbc);
    }
    
    private Color getStatusColor(String status) {
        switch (status) {
            case "Active": return SUCCESS_COLOR;
            case "Available": return INFO_COLOR;
            case "Inactive": return TEXT_SECONDARY;
            default: return TEXT_PRIMARY;
        }
    }
    
    private JDialog createModernDialog(String title, int width, int height) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(Color.WHITE);
        return dialog;
    }
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(mainPanel,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(mainPanel,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(mainPanel,
            message,
            "Warning",
            JOptionPane.WARNING_MESSAGE);
    }
    
    private void showAssignDriverDialog(ActionEvent e) {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a vehicle to assign a driver.");
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(selectedRow);
        Vehicle vehicle = vehicles.get(modelRow);
        showAssignDriverDialog(vehicle);
    }
    
    private void showAssignDriverDialog(Vehicle vehicle) {
        JDialog dialog = createModernDialog("Assign Driver to Vehicle", 500, 400);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Vehicle info header
        JPanel vehicleHeader = new JPanel(new BorderLayout(10, 0));
        vehicleHeader.setBackground(Color.WHITE);
        vehicleHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel vehicleInfo = new JPanel();
        vehicleInfo.setLayout(new BoxLayout(vehicleInfo, BoxLayout.Y_AXIS));
        vehicleInfo.setBackground(Color.WHITE);
        
        JLabel vehicleIdLabel = new JLabel(vehicle.getVehicleId() + " - " + vehicle.getModel());
        vehicleIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel vehiclePlateLabel = new JLabel(vehicle.getNumberPlate());
        vehiclePlateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vehiclePlateLabel.setForeground(TEXT_SECONDARY);
        
        vehicleInfo.add(vehicleIdLabel);
        vehicleInfo.add(vehiclePlateLabel);
        
        vehicleHeader.add(vehicleInfo, BorderLayout.CENTER);
        
        mainPanel.add(vehicleHeader, BorderLayout.NORTH);
        
        // Driver selection panel
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel selectLabel = new JLabel("Select Driver:");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectionPanel.add(selectLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        List<String> driverNames = getAvailableDriverNames();
        JComboBox<String> driverCombo = new JComboBox<>(driverNames.toArray(new String[0]));
        driverCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        driverCombo.setPreferredSize(new Dimension(200, 35));
        selectionPanel.add(driverCombo, gbc);
        
        // Simple driver info area
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        
        JTextArea driverInfoArea = new JTextArea();
        driverInfoArea.setEditable(false);
        driverInfoArea.setBackground(new Color(250, 250, 250));
        driverInfoArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        driverInfoArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Update driver info when selection changes
        driverCombo.addActionListener(comboEvent -> {
            String selectedDriver = (String) driverCombo.getSelectedItem();
            if (selectedDriver != null && !selectedDriver.equals("Unassigned")) {
                Driver driver = findDriverByName(selectedDriver);
                if (driver != null) {
                    String info = String.format(
                        "License: %s\nPhone: %s\nStatus: %s",
                        driver.getLicenseNumber(),
                        driver.getPhone(),
                        driver.getStatus()
                    );
                    driverInfoArea.setText(info);
                }
            } else {
                driverInfoArea.setText("No driver selected");
            }
        });
        
        // Trigger initial display
        if (driverCombo.getItemCount() > 0) {
            driverCombo.setSelectedIndex(0);
        }
        
        JScrollPane scrollPane = new JScrollPane(driverInfoArea);
        scrollPane.setPreferredSize(new Dimension(400, 80));
        selectionPanel.add(scrollPane, gbc);
        
        mainPanel.add(selectionPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton assignButton = createModernButton("Assign", SUCCESS_COLOR);
        assignButton.setPreferredSize(new Dimension(100, 35));
        assignButton.addActionListener(assignEvent -> {
            String selectedDriver = (String) driverCombo.getSelectedItem();
            if (selectedDriver != null && !selectedDriver.equals("Unassigned")) {
                // Update vehicle
                String oldDriver = vehicle.getDriverName();
                vehicle.setDriverName(selectedDriver);
                vehicle.setStatus("Active");
                
                // Update driver's current vehicle
                if (oldDriver != null) {
                    Driver oldDriverObj = findDriverByName(oldDriver);
                    if (oldDriverObj != null) {
                        oldDriverObj.setCurrentVehicle(null);
                        oldDriverObj.setStatus("Available");
                    }
                }
                
                Driver newDriverObj = findDriverByName(selectedDriver);
                if (newDriverObj != null) {
                    newDriverObj.setCurrentVehicle(vehicle.getVehicleId());
                    newDriverObj.setStatus("Active");
                }
                
                saveVehiclesToFile();
                saveDriversToFile();
                
                showSuccessDialog("Driver assigned successfully!\nVehicle is now active.");
                refreshTableData();
                dialog.dispose();
            } else {
                showWarningDialog("Please select a valid driver.");
            }
        });
        
        JButton cancelButton = createModernButton("Cancel", TEXT_SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(cancelEvent -> dialog.dispose());
        
        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showUpdateRoadTaxDialog(ActionEvent e) {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a vehicle to update road tax.");
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(selectedRow);
        Vehicle vehicle = vehicles.get(modelRow);
        showUpdateRoadTaxDialog(vehicle);
    }
    
    private void showUpdateRoadTaxDialog(Vehicle vehicle) {
        JDialog dialog = createModernDialog("Update Road Tax", 400, 250);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Vehicle info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(250, 250, 250));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel vehicleLabel = new JLabel(vehicle.getVehicleId() + " - " + vehicle.getNumberPlate());
        vehicleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoPanel.add(vehicleLabel, BorderLayout.CENTER);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Date selection
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel currentLabel = new JLabel("Current Expiry:");
        currentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        datePanel.add(currentLabel, gbc);
        
        gbc.gridx = 1;
        JLabel currentExpiryLabel = new JLabel(dateFormat.format(vehicle.getRoadTaxExpiry()));
        currentExpiryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currentExpiryLabel.setForeground(vehicle.isRoadTaxExpired() ? DANGER_COLOR : SUCCESS_COLOR);
        datePanel.add(currentExpiryLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel newLabel = new JLabel("New Expiry Date:");
        newLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        datePanel.add(newLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(vehicle.getRoadTaxExpiry());
        dateSpinner.setPreferredSize(new Dimension(150, 30));
        datePanel.add(dateSpinner, gbc);
        
        mainPanel.add(datePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton updateButton = createModernButton("Update", INFO_COLOR);
        updateButton.setPreferredSize(new Dimension(100, 35));
        updateButton.addActionListener(updateEvent -> {
            Date newDate = (Date) dateSpinner.getValue();
            vehicle.setRoadTaxExpiry(newDate);
            saveVehiclesToFile();
            showSuccessDialog("Road tax expiry updated successfully!");
            refreshTableData();
            dialog.dispose();
        });
        
        JButton cancelButton = createModernButton("Cancel", TEXT_SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(cancelEvent -> dialog.dispose());
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void unassignDriver(ActionEvent e) {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a vehicle to unassign driver.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Are you sure you want to unassign the driver from this vehicle?",
            "Confirm Unassign",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = vehiclesTable.convertRowIndexToModel(selectedRow);
            Vehicle vehicle = vehicles.get(modelRow);
            String oldDriver = vehicle.getDriverName();
            
            if (oldDriver != null) {
                Driver driver = findDriverByName(oldDriver);
                if (driver != null) {
                    driver.setCurrentVehicle(null);
                    driver.setStatus("Available");
                }
            }
            
            vehicle.setDriverName(null);
            
            saveVehiclesToFile();
            saveDriversToFile();
            
            showSuccessDialog("Driver " + oldDriver + " has been unassigned from vehicle " + vehicle.getVehicleId());
            refreshTableData();
        }
    }
    
    private void toggleMaintenanceStatus(ActionEvent e) {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a vehicle to toggle maintenance status.");
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(selectedRow);
        Vehicle vehicle = vehicles.get(modelRow);
        
        if ("Maintenance".equals(vehicle.getStatus())) {
            vehicle.setStatus("Active");
            showSuccessDialog("Vehicle " + vehicle.getVehicleId() + " is now active.");
        } else {
            vehicle.setStatus("Maintenance");
            
            String oldDriver = vehicle.getDriverName();
            if (oldDriver != null) {
                Driver driver = findDriverByName(oldDriver);
                if (driver != null) {
                    driver.setCurrentVehicle(null);
                    driver.setStatus("Available");
                }
            }
            vehicle.setDriverName(null);
            
            showWarningDialog("Vehicle " + vehicle.getVehicleId() + " is now in maintenance.\nDriver has been unassigned.");
        }
        
        saveVehiclesToFile();
        saveDriversToFile();
        refreshTableData();
    }
    
    private void showAddVehicleDialog(ActionEvent e) {
        JDialog dialog = createModernDialog("Add New Vehicle", 550, 500);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        String[] labels = {"Vehicle Type:", "Model:", "Number Plate:", "Road Tax Expiry:", "Fuel Type:", "Status:", "Driver:"};
        JTextField[] fields = new JTextField[2];
        fields[0] = new JTextField(20);
        fields[1] = new JTextField(20);
        styleTextField(fields[0]);
        styleTextField(fields[1]);
        
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Truck", "Van", "Car", "Motorcycle"});
        styleComboBox(typeCombo);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        styleComboBox(fuelCombo);
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        styleComboBox(statusCombo);
        
        List<String> driverNames = getAvailableDriverNames();
        JComboBox<String> driverCombo = new JComboBox<>(driverNames.toArray(new String[0]));
        styleComboBox(driverCombo);
        
        int fieldIndex = 0;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(TEXT_PRIMARY);
            formPanel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            if (i == 0) {
                formPanel.add(typeCombo, gbc);
            } else if (i == 1) {
                formPanel.add(fields[fieldIndex++], gbc);
            } else if (i == 2) {
                formPanel.add(fields[fieldIndex++], gbc);
            } else if (i == 3) {
                formPanel.add(dateSpinner, gbc);
            } else if (i == 4) {
                formPanel.add(fuelCombo, gbc);
            } else if (i == 5) {
                formPanel.add(statusCombo, gbc);
            } else if (i == 6) {
                formPanel.add(driverCombo, gbc);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createModernButton("Save", SUCCESS_COLOR);
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.addActionListener(saveEvent -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                String model = fields[0].getText();
                String numberPlate = fields[1].getText();
                Date roadTaxExpiry = (Date) dateSpinner.getValue();
                String fuelType = (String) fuelCombo.getSelectedItem();
                String status = (String) statusCombo.getSelectedItem();
                String driver = (String) driverCombo.getSelectedItem();
                
                if (model.isEmpty() || numberPlate.isEmpty()) {
                    showWarningDialog("Please fill in all required fields!");
                    return;
                }
                
                String vehicleId = generateVehicleId(type);
                
                Vehicle newVehicle = new Vehicle(vehicleId, model, status, 
                    driver != null && !driver.equals("Unassigned") ? driver : null, 
                    type, numberPlate, roadTaxExpiry, fuelType);
                vehicles.add(newVehicle);
                
                if (driver != null && !driver.equals("Unassigned")) {
                    Driver selectedDriver = findDriverByName(driver);
                    if (selectedDriver != null) {
                        selectedDriver.setCurrentVehicle(vehicleId);
                        selectedDriver.setStatus("Active");
                    }
                }
                
                saveVehiclesToFile();
                saveDriversToFile();
                
                showSuccessDialog("Vehicle added successfully!\nVehicle ID: " + vehicleId);
                refreshTableData();
                dialog.dispose();
            } catch (Exception ex) {
                showErrorDialog("Error adding vehicle: " + ex.getMessage());
            }
        });
        
        JButton cancelButton = createModernButton("Cancel", TEXT_SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(cancelEvent -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }
    
    private void showEditVehicleDialog(ActionEvent e) {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a vehicle to edit.");
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(selectedRow);
        Vehicle vehicle = vehicles.get(modelRow);
        showEditVehicleDialog(vehicle);
    }
    
    private void showEditVehicleDialog(Vehicle vehicle) {
        JDialog dialog = createModernDialog("Edit Vehicle - " + vehicle.getVehicleId(), 550, 450);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField modelField = new JTextField(vehicle.getModel(), 20);
        JTextField plateField = new JTextField(vehicle.getNumberPlate(), 20);
        styleTextField(modelField);
        styleTextField(plateField);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(vehicle.getRoadTaxExpiry());
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        fuelCombo.setSelectedItem(vehicle.getFuelType());
        styleComboBox(fuelCombo);
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setSelectedItem(vehicle.getStatus());
        styleComboBox(statusCombo);
        
        String[] labels = {"Vehicle ID:", "Type:", "Model:", "Number Plate:", "Road Tax Expiry:", "Fuel Type:", "Status:"};
        JComponent[] components = {
            new JLabel(vehicle.getVehicleId()),
            new JLabel(vehicle.getType()),
            modelField,
            plateField,
            dateSpinner,
            fuelCombo,
            statusCombo
        };
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(TEXT_PRIMARY);
            formPanel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            if (components[i] instanceof JLabel) {
                JLabel valueLabel = (JLabel) components[i];
                valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                formPanel.add(valueLabel, gbc);
            } else {
                formPanel.add(components[i], gbc);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createModernButton("Save", SUCCESS_COLOR);
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.addActionListener(saveEvent -> {
            try {
                String oldStatus = vehicle.getStatus();
                String newStatus = (String) statusCombo.getSelectedItem();
                
                vehicle.setModel(modelField.getText());
                vehicle.setNumberPlate(plateField.getText());
                vehicle.setRoadTaxExpiry((Date) dateSpinner.getValue());
                vehicle.setFuelType((String) fuelCombo.getSelectedItem());
                vehicle.setStatus(newStatus);
                
                // Handle status change to Maintenance
                if (!oldStatus.equals(newStatus) && "Maintenance".equals(newStatus)) {
                    String driverName = vehicle.getDriverName();
                    if (driverName != null) {
                        Driver driver = findDriverByName(driverName);
                        if (driver != null) {
                            driver.setCurrentVehicle(null);
                            driver.setStatus("Available");
                        }
                        vehicle.setDriverName(null);
                    }
                }
                
                saveVehiclesToFile();
                saveDriversToFile();
                
                showSuccessDialog("Vehicle updated successfully!");
                refreshTableData();
                dialog.dispose();
            } catch (Exception ex) {
                showErrorDialog("Error updating vehicle: " + ex.getMessage());
            }
        });
        
        JButton cancelButton = createModernButton("Cancel", TEXT_SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(cancelEvent -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void updateStats() {
        activeCountLabel.setText(String.valueOf(getActiveCount()));
        maintenanceCountLabel.setText(String.valueOf(getMaintenanceCount()));
        expiredRoadTaxLabel.setText(String.valueOf(getExpiredRoadTaxCount()));
        totalCountLabel.setText(String.valueOf(getTotalCount()));
        availableDriversLabel.setText(String.valueOf(getAvailableDriversCount()));
    }
    
    public void refreshData() {
        loadDataFromFiles();
        refreshTableData();
        updateStats();
    }
    
    public int getActiveCount() {
        return (int) vehicles.stream()
            .filter(v -> "Active".equals(v.getStatus()))
            .count();
    }
    
    public int getMaintenanceCount() {
        return (int) vehicles.stream()
            .filter(v -> "Maintenance".equals(v.getStatus()))
            .count();
    }
    
    public int getExpiredRoadTaxCount() {
        return (int) vehicles.stream()
            .filter(Vehicle::isRoadTaxExpired)
            .count();
    }
    
    public int getTotalCount() {
        return vehicles.size();
    }
    
    public int getAvailableDriversCount() {
        return (int) drivers.stream()
            .filter(d -> "Available".equals(d.getStatus()) || "Active".equals(d.getStatus()))
            .count();
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JPanel getRefreshedPanel() {
        refreshData();
        return mainPanel;
    }
    
    // Custom cell renderer for status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && c instanceof JLabel) {
                JLabel label = (JLabel) c;
                String status = value.toString();
                
                switch (status) {
                    case "Active":
                        label.setForeground(SUCCESS_COLOR);
                        break;
                    case "Maintenance":
                        label.setForeground(WARNING_COLOR);
                        break;
                    case "Inactive":
                        label.setForeground(TEXT_SECONDARY);
                        break;
                    default:
                        label.setForeground(TEXT_PRIMARY);
                }
                
                label.setText(status);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            
            return c;
        }
    }
    
    // Custom cell renderer for driver column (clickable)
    private class DriverCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && c instanceof JLabel) {
                JLabel label = (JLabel) c;
                String driverName = value.toString();
                
                if (!driverName.equals("Unassigned")) {
                    label.setText(driverName);
                    label.setForeground(PRIMARY_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setToolTipText("Double-click to view driver details");
                    label.setText("<html><u>" + label.getText() + "</u></html>");
                } else {
                    label.setText(driverName);
                    label.setForeground(TEXT_SECONDARY);
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                }
            }
            
            return c;
        }
    }
    
    // Custom cell renderer for date column
    private class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Date && c instanceof JLabel) {
                JLabel label = (JLabel) c;
                Date date = (Date) value;
                Date today = new Date();
                
                label.setText(dateFormat.format(date));
                
                if (date.before(today)) {
                    label.setForeground(DANGER_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setText(label.getText() + " (Expired)");
                } else {
                    long diff = date.getTime() - today.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    
                    if (days <= 30) {
                        label.setForeground(WARNING_COLOR);
                        label.setText(label.getText() + " (Soon)");
                    } else {
                        label.setForeground(TEXT_PRIMARY);
                    }
                }
            }
            
            return c;
        }
    }
    
    private Vehicle findVehicleById(String vehicleId) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId().equals(vehicleId)) {
                return vehicle;
            }
        }
        return null;
    }
    
    // Driver class
    private class Driver {
        private String name;
        private String licenseNumber;
        private String phone;
        private String email;
        private String status;
        private Date joinedDate;
        private String currentVehicle;
        
        public Driver(String name, String licenseNumber, String phone, String email, 
                     String status, Date joinedDate, String currentVehicle) {
            this.name = name;
            this.licenseNumber = licenseNumber;
            this.phone = phone;
            this.email = email;
            this.status = status;
            this.joinedDate = joinedDate;
            this.currentVehicle = currentVehicle;
        }
        
        public String getName() { return name; }
        public String getLicenseNumber() { return licenseNumber; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public Date getJoinedDate() { return joinedDate; }
        public String getCurrentVehicle() { return currentVehicle; }
        
        public void setStatus(String status) { this.status = status; }
        public void setCurrentVehicle(String vehicle) { this.currentVehicle = vehicle; }
    }
    
    // Vehicle class
    private class Vehicle {
        private String vehicleId;
        private String model;
        private String status;
        private String driverName;
        private String type;
        private String numberPlate;
        private Date roadTaxExpiry;
        private String fuelType;
        
        public Vehicle(String vehicleId, String model, String status, String driverName, 
                      String type, String numberPlate, Date roadTaxExpiry, String fuelType) {
            this.vehicleId = vehicleId;
            this.model = model;
            this.status = status;
            this.driverName = driverName;
            this.type = type;
            this.numberPlate = numberPlate;
            this.roadTaxExpiry = roadTaxExpiry;
            this.fuelType = fuelType;
        }
        
        public String getVehicleId() { return vehicleId; }
        public String getModel() { return model; }
        public String getStatus() { return status; }
        public String getDriverName() { return driverName; }
        public String getType() { return type; }
        public String getNumberPlate() { return numberPlate; }
        public Date getRoadTaxExpiry() { return roadTaxExpiry; }
        public String getFuelType() { return fuelType; }
        
        public void setStatus(String status) { this.status = status; }
        public void setDriverName(String driverName) { this.driverName = driverName; }
        public void setModel(String model) { this.model = model; }
        public void setNumberPlate(String numberPlate) { this.numberPlate = numberPlate; }
        public void setRoadTaxExpiry(Date roadTaxExpiry) { this.roadTaxExpiry = roadTaxExpiry; }
        public void setFuelType(String fuelType) { this.fuelType = fuelType; }
        
        public boolean isRoadTaxExpired() {
            return roadTaxExpiry != null && roadTaxExpiry.before(new Date());
        }
    }
}