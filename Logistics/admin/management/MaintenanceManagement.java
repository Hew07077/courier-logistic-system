package admin.management;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

/**
 * Manages maintenance records with a comprehensive UI for tracking vehicle maintenance tasks.
 * Provides filtering, sorting, and CRUD operations for maintenance records.
 * Integrated with VehicleManagement to track vehicles in maintenance.
 */
public class MaintenanceManagement {
    private static final Logger LOGGER = Logger.getLogger(MaintenanceManagement.class.getName());
    
    // File paths
    private static final String MAINTENANCE_FILE = "maintenance_data.txt";
    
    // UI Components
    private JPanel mainPanel;
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel[] statLabels;
    private JPanel statsPanel;
    private DashboardUpdateListener dashboardListener;
    
    // Data
    private List<MaintenanceRecord> maintenanceRecords;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // Reference to VehicleManagement
    private VehicleManagement vehicleManagement;
    
    // Constants
    private static final Color[] STATUS_COLORS = {
        new Color(237, 108, 2),   // Scheduled - Orange
        new Color(2, 136, 209),   // In Progress - Blue
        new Color(46, 125, 50)    // Completed - Green
    };
    
    private static final Color PRIMARY = new Color(25, 118, 210);
    private static final Color SUCCESS = new Color(46, 125, 50);
    private static final Color WARNING = new Color(237, 108, 2);
    private static final Color DANGER = new Color(198, 40, 40);
    private static final Color INFO = new Color(2, 136, 209);
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    
    private static final String[] TABLE_COLUMNS = {"ID", "Vehicle", "Description", "Status", "Date", "Notes"};
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    /**
     * Listener interface for dashboard updates
     */
    public interface DashboardUpdateListener {
        void onMaintenanceDataChanged();
        void onStatusCountsUpdated(int scheduled, int inProgress, int completed);
    }
    
    /**
     * Constructs a new MaintenanceManagement instance
     */
    public MaintenanceManagement() {
        this(null, null);
    }
    
    /**
     * Constructs a new MaintenanceManagement instance with a dashboard listener
     * @param listener The dashboard update listener
     */
    public MaintenanceManagement(DashboardUpdateListener listener) {
        this(listener, null);
    }
    
    /**
     * Constructs a new MaintenanceManagement instance with VehicleManagement integration
     * @param listener The dashboard update listener
     * @param vehicleMgmt The VehicleManagement instance
     */
    public MaintenanceManagement(DashboardUpdateListener listener, VehicleManagement vehicleMgmt) {
        this.dashboardListener = listener;
        this.vehicleManagement = vehicleMgmt;
        this.maintenanceRecords = new ArrayList<>();
        loadMaintenanceRecords();
        createMainPanel();
    }
    
    /**
     * Sets the VehicleManagement instance for integration
     * @param vehicleMgmt The VehicleManagement instance
     */
    public void setVehicleManagement(VehicleManagement vehicleMgmt) {
        this.vehicleManagement = vehicleMgmt;
        syncWithVehicleManagement();
    }
    
    /**
     * Loads maintenance records from file or creates sample data
     */
    private void loadMaintenanceRecords() {
        File file = new File(MAINTENANCE_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.lines()
                    .filter(l -> !l.trim().isEmpty() && !l.startsWith("//"))
                    .forEach(this::parseMaintenanceRecord);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading maintenance records", e);
            }
        }
        
        if (maintenanceRecords.isEmpty()) {
            initializeSampleData();
        }
    }
    
    /**
     * Saves maintenance records to file
     */
    private void saveMaintenanceRecords() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAINTENANCE_FILE))) {
            writer.write("// Format: ID|VehicleID|Description|Status|Date|Notes");
            writer.newLine();
            for (MaintenanceRecord record : maintenanceRecords) {
                writer.write(String.format("%s|%s|%s|%s|%s|%s",
                    record.maintenanceId,
                    record.vehicleId,
                    record.description,
                    record.status,
                    dateFormat.format(record.scheduledDate),
                    record.notes));
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving maintenance records", e);
        }
    }
    
    /**
     * Parses a maintenance record from a line of text
     * @param line The line to parse
     */
    private void parseMaintenanceRecord(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                maintenanceRecords.add(new MaintenanceRecord(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    dateFormat.parse(parts[4].trim()),
                    parts[5].trim()
                ));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing maintenance record: " + line, e);
        }
    }
    
    /**
     * Synchronizes with VehicleManagement to track vehicles in maintenance
     */
    private void syncWithVehicleManagement() {
        if (vehicleManagement == null) return;
        
        // Get all vehicles that are in maintenance from VehicleManagement
        List<VehicleManagement.Vehicle> maintenanceVehicles = vehicleManagement.getVehiclesByStatus("Maintenance");
        Set<String> maintenanceVehicleIds = maintenanceVehicles.stream()
            .map(v -> v.id)
            .collect(Collectors.toSet());
        
        // Also get all vehicles that are active
        List<VehicleManagement.Vehicle> activeVehicles = vehicleManagement.getVehiclesByStatus("Active");
        Set<String> activeVehicleIds = activeVehicles.stream()
            .map(v -> v.id)
            .collect(Collectors.toSet());
        
        boolean changed = false;
        
        // Complete any active maintenance records for vehicles that are now active
        for (MaintenanceRecord record : maintenanceRecords) {
            if (!record.status.equals("Completed") && activeVehicleIds.contains(record.vehicleId)) {
                record.status = "Completed";
                record.notes += " | Auto-completed (vehicle is now active)";
                changed = true;
            }
        }
        
        // Create records for vehicles in maintenance that don't have active records
        for (String vehicleId : maintenanceVehicleIds) {
            boolean hasActiveRecord = maintenanceRecords.stream()
                .anyMatch(r -> r.vehicleId.equals(vehicleId) && !r.status.equals("Completed"));
            
            if (!hasActiveRecord) {
                // Find vehicle model if possible
                String model = "Unknown";
                for (VehicleManagement.Vehicle v : maintenanceVehicles) {
                    if (v.id.equals(vehicleId)) {
                        model = v.model;
                        break;
                    }
                }
                addVehicleToMaintenance(vehicleId, model, "Vehicle in maintenance (auto-synced)", false);
                changed = true;
            }
        }
        
        if (changed) {
            saveMaintenanceRecords();
            refreshTableData();
        }
    }
    
    /**
     * Called when a vehicle is sent to maintenance from VehicleManagement
     * @param vehicleId The vehicle ID
     * @param vehicleModel The vehicle model
     * @param description Maintenance description
     */
    public void addVehicleToMaintenance(String vehicleId, String vehicleModel, String description) {
        addVehicleToMaintenance(vehicleId, vehicleModel, description, true);
    }
    
    /**
     * Called when a vehicle is sent to maintenance from VehicleManagement
     * @param vehicleId The vehicle ID
     * @param vehicleModel The vehicle model
     * @param description Maintenance description
     * @param showMessage Whether to show a message dialog
     */
    private void addVehicleToMaintenance(String vehicleId, String vehicleModel, String description, boolean showMessage) {
        // Check if vehicle already has an active maintenance record
        boolean hasActiveRecord = maintenanceRecords.stream()
            .anyMatch(r -> r.vehicleId.equals(vehicleId) && 
                          !r.status.equals("Completed"));
        
        if (hasActiveRecord) {
            if (showMessage) {
                JOptionPane.showMessageDialog(mainPanel,
                    "Vehicle " + vehicleId + " already has an active maintenance record.",
                    "Duplicate Record",
                    JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        
        // Create new maintenance record
        String id = generateMaintenanceId();
        MaintenanceRecord newRecord = new MaintenanceRecord(
            id,
            vehicleId,
            description != null ? description : "Routine maintenance",
            "In Progress",  // Directly set to In Progress when sent from vehicle management
            new Date(),
            "Vehicle sent to maintenance from fleet management"
        );
        
        maintenanceRecords.add(newRecord);
        saveMaintenanceRecords();
        refreshTableData();
        
        if (dashboardListener != null) {
            dashboardListener.onMaintenanceDataChanged();
        }
        
        if (showMessage) {
            JOptionPane.showMessageDialog(mainPanel,
                "Vehicle " + vehicleId + " added to maintenance as record " + id,
                "Maintenance Record Created",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Completes all active maintenance records for a vehicle
     * @param vehicleId The vehicle ID
     */
    public void completeMaintenanceForVehicle(String vehicleId) {
        boolean updated = false;
        for (MaintenanceRecord record : maintenanceRecords) {
            if (record.vehicleId.equals(vehicleId) && !record.status.equals("Completed")) {
                record.status = "Completed";
                record.notes += " | Completed on " + dateFormat.format(new Date());
                updated = true;
            }
        }
        
        if (updated) {
            saveMaintenanceRecords();
            refreshTableData();
            
            if (dashboardListener != null) {
                dashboardListener.onMaintenanceDataChanged();
            }
        }
    }
    
    /**
     * Gets all active maintenance records (not completed)
     * @return List of active maintenance records
     */
    public List<MaintenanceRecord> getActiveMaintenanceRecords() {
        return maintenanceRecords.stream()
            .filter(r -> !r.status.equals("Completed"))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets maintenance records for a specific vehicle
     * @param vehicleId The vehicle ID
     * @return List of maintenance records for the vehicle
     */
    public List<MaintenanceRecord> getVehicleMaintenanceHistory(String vehicleId) {
        return maintenanceRecords.stream()
            .filter(r -> r.vehicleId.equals(vehicleId))
            .sorted((a, b) -> b.scheduledDate.compareTo(a.scheduledDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if a vehicle is currently in maintenance
     * @param vehicleId The vehicle ID
     * @return true if vehicle has active maintenance
     */
    public boolean isVehicleInMaintenance(String vehicleId) {
        return maintenanceRecords.stream()
            .anyMatch(r -> r.vehicleId.equals(vehicleId) && 
                          !r.status.equals("Completed"));
    }
    
    /**
     * Initializes sample data for demonstration purposes
     */
    private void initializeSampleData() {
        try {
            // Create dates
            Calendar cal = Calendar.getInstance();
            Date today = new Date();
            
            // MNT001 - For TRK001 (Active vehicle) - This should be COMPLETED
            cal.setTime(today);
            cal.add(Calendar.MONTH, -2);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT001", "TRK001", "Oil Change", "Completed", 
                cal.getTime(), "Regular maintenance (Completed)"
            ));
            
            // MNT002 - For VAN001 (In Maintenance)
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -2);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT002", "VAN001", "Brake Repair", "In Progress", 
                cal.getTime(), "Front brake pads replacement"
            ));
            
            // MNT003 - For TRK002 (In Maintenance)
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -3);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT003", "TRK002", "Tire Replacement", "In Progress", 
                cal.getTime(), "All 4 tires replaced"
            ));
            
            // MNT004 - For CAR001 (Active vehicle) - Completed
            cal.setTime(today);
            cal.add(Calendar.MONTH, -1);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT004", "CAR001", "Engine Check", "Completed", 
                cal.getTime(), "Check engine light diagnosis - Fixed"
            ));
            
            // MNT005 - For VAN002 (Active vehicle) - Completed
            cal.setTime(today);
            cal.add(Calendar.MONTH, -3);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT005", "VAN002", "Battery Replacement", "Completed", 
                cal.getTime(), "12V battery replaced"
            ));
            
            // MNT006 - For TRK003 (Active vehicle) - Completed
            cal.setTime(today);
            cal.add(Calendar.MONTH, -1);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT006", "TRK003", "Transmission Service", "Completed", 
                cal.getTime(), "Fluid change and inspection"
            ));
            
            saveMaintenanceRecords();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize sample data", e);
        }
    }
    
    /**
     * Creates the main panel with all UI components
     */
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_COLOR);
        
        // Create all components
        JPanel topContainer = new JPanel(new BorderLayout(15, 15));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createStatsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        // Update stats after UI is created
        SwingUtilities.invokeLater(() -> updateStats());
    }
    
    /**
     * Creates the header panel with title
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Maintenance Management");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Track and manage vehicle maintenance tasks");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * Creates the statistics panel showing counts by status
     */
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        String[] titles = {"Scheduled", "In Progress", "Completed", "Total"};
        String[] icons = {"📅", "🔧", "✅", "📊"};
        String[] descriptions = {
            "Pending tasks", "Ongoing work", "Finished tasks", "All records"
        };
        Color[] colors = {WARNING, INFO, SUCCESS, PRIMARY};
        
        statLabels = new JLabel[4];
        
        for (int i = 0; i < 4; i++) {
            JPanel card = createStatCard(titles[i], icons[i], descriptions[i], "0", colors[i], i);
            statsPanel.add(card);
        }
        
        return statsPanel;
    }
    
    /**
     * Creates an individual stat card
     */
    private JPanel createStatCard(String title, String icon, String description, String value, Color color, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Icon and title row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(CARD_BG);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        topRow.add(iconLabel, BorderLayout.WEST);
        topRow.add(titleLabel, BorderLayout.EAST);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(topRow);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descLabel);
        
        // Store reference to value label for updates
        statLabels[index] = valueLabel;
        
        // Make card clickable for filtering
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final String filterStatus = title;
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(250, 250, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
            public void mouseClicked(MouseEvent e) {
                if (!filterStatus.equals("Total")) {
                    statusFilter.setSelectedItem(filterStatus);
                    filterTable();
                }
            }
        });
        
        return card;
    }
    
    /**
     * Creates the center panel with filter and table
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        
        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the filter panel with search and status filters
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Search field with icon
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.setBackground(CARD_BG);
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, vehicle, description...");
        searchField.setFont(REGULAR_FONT);
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        searchField.getDocument().addDocumentListener(new DocumentListenerAdapter());
        
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        panel.add(searchPanel);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 30));
            setForeground(BORDER_COLOR);
        }});
        
        // Status filter
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setBackground(CARD_BG);
        statusPanel.add(new JLabel("Status:") {{
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(TEXT_SECONDARY);
        }});
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "Scheduled", "In Progress", "Completed"});
        styleComboBox(statusFilter);
        statusPanel.add(statusFilter);
        panel.add(statusPanel);
        
        // Clear button
        JButton clearBtn = new JButton("Clear Filters ✕");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setForeground(TEXT_SECONDARY);
        clearBtn.setBackground(CARD_BG);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            filterTable();
        });
        
        clearBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                clearBtn.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent e) {
                clearBtn.setBackground(CARD_BG);
            }
        });
        
        panel.add(clearBtn);
        
        return panel;
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(REGULAR_FONT);
        comboBox.setPreferredSize(new Dimension(120, 35));
        comboBox.setBackground(CARD_BG);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        comboBox.addActionListener(e -> filterTable());
    }
    
    /**
     * Creates the table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the maintenance table
     */
    private JTable createTable() {
        tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        maintenanceTable = new JTable(tableModel);
        maintenanceTable.setRowHeight(45);
        maintenanceTable.setFont(REGULAR_FONT);
        maintenanceTable.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 30));
        maintenanceTable.setSelectionForeground(TEXT_PRIMARY);
        maintenanceTable.setShowGrid(true);
        maintenanceTable.setGridColor(BORDER_COLOR);
        maintenanceTable.setIntercellSpacing(new Dimension(10, 5));
        
        // Table header styling
        JTableHeader header = maintenanceTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        rowSorter = new TableRowSorter<>(tableModel);
        maintenanceTable.setRowSorter(rowSorter);
        
        // Set column widths
        maintenanceTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        maintenanceTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        maintenanceTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        maintenanceTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        maintenanceTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        maintenanceTable.getColumnModel().getColumn(5).setPreferredWidth(250);
        
        // Set custom renderers
        maintenanceTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        maintenanceTable.getColumnModel().getColumn(4).setCellRenderer(new DateCellRenderer());
        
        // Double-click listener
        maintenanceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showRecordDetails();
                }
            }
        });
        
        refreshTableData();
        return maintenanceTable;
    }
    
    /**
     * Creates the button panel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        panel.setBackground(BG_COLOR);
        
        String[] btns = {"➕ Add Record", "✏️ Edit", "❌ Delete", "▶️ Start", "✅ Complete", "🚗 View Vehicle"};
        Color[] colors = {PRIMARY, new Color(255, 152, 0), DANGER, INFO, SUCCESS, new Color(156, 39, 176)};
        Runnable[] actions = {
            this::showAddDialog, 
            this::showEditDialog, 
            this::deleteRecord, 
            () -> updateStatus("In Progress"),
            () -> updateStatus("Completed"),
            this::viewVehicleDetails
        };
        
        for (int i = 0; i < btns.length; i++) {
            JButton btn = createStyledButton(btns[i], colors[i]);
            final int index = i;
            btn.addActionListener(e -> actions[index].run());
            panel.add(btn);
        }
        
        return panel;
    }
    
    /**
     * View vehicle details for the selected maintenance record
     */
    private void viewVehicleDetails() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (!validateRowSelection(selectedRow, "Select a record to view vehicle")) {
            return;
        }
        
        MaintenanceRecord record = getSelectedRecord(selectedRow);
        if (record == null) return;
        
        // Get vehicle info from VehicleManagement
        if (vehicleManagement != null) {
            // This would ideally show the vehicle details dialog
            // For now, just show a message
            JOptionPane.showMessageDialog(mainPanel,
                "Vehicle ID: " + record.vehicleId + "\n" +
                "Would open vehicle details here",
                "Vehicle Details",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            String vehicleInfo = String.format(
                "Vehicle ID: %s\n" +
                "Maintenance Record: %s\n" +
                "Current Status: %s\n" +
                "Scheduled Date: %s\n" +
                "Description: %s",
                record.vehicleId,
                record.maintenanceId,
                record.status,
                dateFormat.format(record.scheduledDate),
                record.description
            );
            
            JOptionPane.showMessageDialog(mainPanel,
                vehicleInfo,
                "Vehicle Details for " + record.vehicleId,
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Creates a styled button
     */
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
        });
        
        return btn;
    }
    
    /**
     * Custom cell renderer for status column
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                setHorizontalAlignment(CENTER);
                setText(" " + status + " ");
                
                if (status.equals("Scheduled")) {
                    setForeground(WARNING);
                    setBackground(new Color(255, 243, 224));
                } else if (status.equals("In Progress")) {
                    setForeground(INFO);
                    setBackground(new Color(227, 242, 253));
                } else if (status.equals("Completed")) {
                    setForeground(SUCCESS);
                    setBackground(new Color(232, 245, 233));
                }
                setFont(getFont().deriveFont(Font.BOLD, 12));
            }
            return this;
        }
    }
    
    /**
     * Custom cell renderer for date column
     */
    private class DateCellRenderer extends DefaultTableCellRenderer {
        private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Date) {
                Date date = (Date) value;
                String text = displayFormat.format(date);
                setHorizontalAlignment(CENTER);
                
                if (date.before(new Date()) && !isSelected) {
                    setForeground(DANGER);
                    setText("⚠️ " + text + " (Overdue)");
                    setFont(getFont().deriveFont(Font.BOLD));
                    setBackground(new Color(255, 235, 238));
                } else {
                    setForeground(TEXT_PRIMARY);
                    setText("📅 " + text);
                    setBackground(Color.WHITE);
                }
            }
            return this;
        }
    }
    
    /**
     * Document listener adapter for search field
     */
    private class DocumentListenerAdapter implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }
        
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            filterTable();
        }
    }
    
    /**
     * Shows detailed view of a selected record
     */
    private void showRecordDetails() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (!validateRowSelection(selectedRow, "Select a record to view details")) {
            return;
        }
        
        MaintenanceRecord record = getSelectedRecord(selectedRow);
        if (record == null) return;
        
        String taxStatus = record.scheduledDate.before(new Date()) && !record.status.equals("Completed") 
            ? "Overdue" : "On Schedule";
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Maintenance Record: " + record.maintenanceId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addDetailRow(detailsPanel, "Vehicle ID:", record.vehicleId, gbc, 0);
        addDetailRow(detailsPanel, "Description:", record.description, gbc, 1);
        addDetailRow(detailsPanel, "Status:", record.status, gbc, 2);
        addDetailRow(detailsPanel, "Date:", dateFormat.format(record.scheduledDate) + " (" + taxStatus + ")", gbc, 3);
        addDetailRow(detailsPanel, "Notes:", record.notes.isEmpty() ? "No notes" : record.notes, gbc, 4);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(mainPanel,
            panel,
            "Maintenance Record Details",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setForeground(TEXT_SECONDARY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComp.setForeground(TEXT_PRIMARY);
        panel.add(valueComp, gbc);
    }
    
    /**
     * Filters the table based on search text and status filter
     */
    private void filterTable() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        if (!searchField.getText().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchField.getText(), 0, 1, 2, 5));
        }
        
        if (statusFilter.getSelectedIndex() > 0) {
            filters.add(RowFilter.regexFilter(statusFilter.getSelectedItem().toString(), 3));
        }
        
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
    
    /**
     * Refreshes the table data from the records list
     */
    private void refreshTableData() {
        tableModel.setRowCount(0);
        for (MaintenanceRecord record : maintenanceRecords) {
            tableModel.addRow(new Object[]{
                record.maintenanceId,
                record.vehicleId,
                record.description,
                record.status,
                record.scheduledDate,
                record.notes
            });
        }
        updateStats();
    }
    
    /**
     * Updates the statistics labels
     */
    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            long scheduled = maintenanceRecords.stream().filter(r -> r.status.equals("Scheduled")).count();
            long inProgress = maintenanceRecords.stream().filter(r -> r.status.equals("In Progress")).count();
            long completed = maintenanceRecords.stream().filter(r -> r.status.equals("Completed")).count();
            long total = maintenanceRecords.size();
            
            if (statLabels[0] != null) statLabels[0].setText(String.valueOf(scheduled));
            if (statLabels[1] != null) statLabels[1].setText(String.valueOf(inProgress));
            if (statLabels[2] != null) statLabels[2].setText(String.valueOf(completed));
            if (statLabels[3] != null) statLabels[3].setText(String.valueOf(total));
            
            statsPanel.revalidate();
            statsPanel.repaint();
            
            if (dashboardListener != null) {
                dashboardListener.onStatusCountsUpdated((int)scheduled, (int)inProgress, (int)completed);
            }
        });
    }
    
    /**
     * Shows dialog to add a new maintenance record
     */
    private void showAddDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add Maintenance Record");
        dialog.setModal(true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(CARD_BG);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(CARD_BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        JLabel titleLabel = new JLabel("➕ Add Maintenance Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Form fields
        JTextField vehicleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField dateField = new JTextField(dateFormat.format(new Date()));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Scheduled", "In Progress"});
        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        // Style form fields
        styleFormField(vehicleField);
        styleFormField(descField);
        styleFormField(dateField);
        styleFormField(statusCombo);
        styleFormField(new JScrollPane(notesArea));
        
        // Add fields with labels
        int row = 0;
        addFormField(formPanel, "Vehicle ID:", vehicleField, gbc, row++);
        addFormField(formPanel, "Description:", descField, gbc, row++);
        addFormField(formPanel, "Date (yyyy-MM-dd):", dateField, gbc, row++);
        addFormField(formPanel, "Status:", statusCombo, gbc, row++);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(notesArea), gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveBtn = new JButton("Save Record");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(SUCCESS);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(120, 40));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            if (validateForm(vehicleField, descField, dateField)) {
                try {
                    String id = generateMaintenanceId();
                    Date date = dateFormat.parse(dateField.getText());
                    String status = (String) statusCombo.getSelectedItem();
                    
                    MaintenanceRecord newRecord = new MaintenanceRecord(
                        id,
                        vehicleField.getText().trim().toUpperCase(),
                        descField.getText().trim(),
                        status,
                        date,
                        notesArea.getText().trim()
                    );
                    
                    maintenanceRecords.add(newRecord);
                    saveMaintenanceRecords();
                    refreshTableData();
                    
                    if (dashboardListener != null) {
                        dashboardListener.onMaintenanceDataChanged();
                    }
                    
                    showSuccess("Record " + id + " added successfully!");
                    dialog.dispose();
                } catch (ParseException ex) {
                    showError("Invalid date format. Please use yyyy-MM-dd");
                }
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void styleFormField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 35));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        } else if (field instanceof JSpinner) {
            ((JSpinner) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
    }
    
    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jLabel.setForeground(TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        panel.add(field, gbc);
    }
    
    private boolean validateForm(JTextField vehicleField, JTextField descField, JTextField dateField) {
        if (vehicleField.getText().trim().isEmpty()) {
            showWarning("Please enter vehicle ID");
            return false;
        }
        if (descField.getText().trim().isEmpty()) {
            showWarning("Please enter description");
            return false;
        }
        if (dateField.getText().trim().isEmpty()) {
            showWarning("Please enter date");
            return false;
        }
        return true;
    }
    
    /**
     * Shows dialog to edit an existing maintenance record
     */
    private void showEditDialog() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (!validateRowSelection(selectedRow, "Select a record to edit")) {
            return;
        }
        
        MaintenanceRecord record = getSelectedRecord(selectedRow);
        if (record == null) return;
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Maintenance Record: " + record.maintenanceId);
        dialog.setModal(true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(CARD_BG);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(CARD_BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("✏️ Edit Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 152, 0));
        
        JLabel idLabel = new JLabel("  •  " + record.maintenanceId);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        idLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(idLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Form fields
        JTextField vehicleField = new JTextField(record.vehicleId);
        JTextField descField = new JTextField(record.description);
        JTextField dateField = new JTextField(dateFormat.format(record.scheduledDate));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Scheduled", "In Progress", "Completed"});
        statusCombo.setSelectedItem(record.status);
        JTextArea notesArea = new JTextArea(record.notes, 3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        // Style fields
        styleFormField(vehicleField);
        styleFormField(descField);
        styleFormField(dateField);
        styleFormField(statusCombo);
        styleFormField(new JScrollPane(notesArea));
        
        // Add fields
        int row = 0;
        addFormField(formPanel, "Vehicle ID:", vehicleField, gbc, row++);
        addFormField(formPanel, "Description:", descField, gbc, row++);
        addFormField(formPanel, "Date (yyyy-MM-dd):", dateField, gbc, row++);
        addFormField(formPanel, "Status:", statusCombo, gbc, row++);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(notesArea), gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(255, 152, 0));
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(130, 40));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            try {
                String oldStatus = record.status;
                record.vehicleId = vehicleField.getText().trim().toUpperCase();
                record.description = descField.getText().trim();
                record.scheduledDate = dateFormat.parse(dateField.getText());
                record.status = (String) statusCombo.getSelectedItem();
                record.notes = notesArea.getText().trim();
                
                // If status changed to Completed, notify VehicleManagement
                if (!oldStatus.equals(record.status) && record.status.equals("Completed") && vehicleManagement != null) {
                    vehicleManagement.completeMaintenanceForVehicle(record.vehicleId);
                }
                
                saveMaintenanceRecords();
                refreshTableData();
                
                if (dashboardListener != null) {
                    dashboardListener.onMaintenanceDataChanged();
                }
                
                showSuccess("Record " + record.maintenanceId + " updated successfully!");
                dialog.dispose();
            } catch (ParseException ex) {
                showError("Invalid date format. Please use yyyy-MM-dd");
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Deletes the selected record
     */
    private void deleteRecord() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (!validateRowSelection(selectedRow, "Select a record to delete")) {
            return;
        }
        
        MaintenanceRecord record = getSelectedRecord(selectedRow);
        if (record == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Are you sure you want to delete record " + record.maintenanceId + "?\nThis action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            maintenanceRecords.remove(record);
            saveMaintenanceRecords();
            refreshTableData();
            
            if (dashboardListener != null) {
                dashboardListener.onMaintenanceDataChanged();
            }
            
            showSuccess("Record deleted successfully!");
        }
    }
    
    /**
     * Updates the status of the selected record
     * @param newStatus The new status to set
     */
    private void updateStatus(String newStatus) {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (!validateRowSelection(selectedRow, "Select a record to update")) {
            return;
        }
        
        MaintenanceRecord record = getSelectedRecord(selectedRow);
        if (record == null) return;
        
        if (record.status.equals(newStatus)) {
            showWarning("Record is already " + newStatus);
            return;
        }
        
        String oldStatus = record.status;
        record.status = newStatus;
        record.notes += " | Status changed to " + newStatus + " on " + dateFormat.format(new Date());
        
        // If status changed to Completed, notify VehicleManagement
        if (oldStatus.equals("In Progress") && record.status.equals("Completed") && vehicleManagement != null) {
            vehicleManagement.completeMaintenanceForVehicle(record.vehicleId);
        }
        
        saveMaintenanceRecords();
        refreshTableData();
        
        if (dashboardListener != null) {
            dashboardListener.onMaintenanceDataChanged();
        }
        
        showSuccess("Status updated to: " + record.status);
    }
    
    /**
     * Validates that a row is selected
     * @param selectedRow The selected row index
     * @param message The message to show if no row is selected
     * @return true if a row is selected, false otherwise
     */
    private boolean validateRowSelection(int selectedRow, String message) {
        if (selectedRow == -1) {
            showWarning(message);
            return false;
        }
        return true;
    }
    
    /**
     * Gets the selected record from the table
     * @param selectedRow The selected row index
     * @return The selected MaintenanceRecord or null if invalid
     */
    private MaintenanceRecord getSelectedRecord(int selectedRow) {
        try {
            int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);
            return maintenanceRecords.get(modelRow);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, "Invalid row selection", e);
            return null;
        }
    }
    
    /**
     * Generates a new maintenance ID
     * @return A unique maintenance ID
     */
    private String generateMaintenanceId() {
        int maxId = maintenanceRecords.stream()
            .mapToInt(r -> {
                try {
                    return Integer.parseInt(r.maintenanceId.replace("MNT", ""));
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .max()
            .orElse(0);
        
        return String.format("MNT%03d", maxId + 1);
    }
    
    /**
     * Shows an error message dialog
     */
    private void showError(String msg) { 
        JOptionPane.showMessageDialog(mainPanel, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Shows a warning message dialog
     */
    private void showWarning(String msg) { 
        JOptionPane.showMessageDialog(mainPanel, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Shows a success message dialog
     */
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(mainPanel, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Gets overdue maintenance records
     * @return List of overdue records
     */
    public List<MaintenanceRecord> getOverdueRecords() {
        Date today = new Date();
        return maintenanceRecords.stream()
            .filter(r -> r.scheduledDate.before(today) && !r.status.equals("Completed"))
            .collect(Collectors.toList());
    }
    
    /**
     * Refreshes the panel data
     */
    public void refreshData() {
        refreshTableData();
    }
    
    /**
     * Gets the refreshed panel
     * @return The main panel with refreshed data
     */
    public JPanel getRefreshedPanel() {
        refreshData();
        return mainPanel;
    }
    
    /**
     * Gets the main panel
     * @return The main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Gets the count of scheduled records
     * @return Scheduled count
     */
    public int getScheduledCount() {
        return (int) maintenanceRecords.stream()
            .filter(r -> r.status.equals("Scheduled"))
            .count();
    }
    
    /**
     * Gets the count of in-progress records
     * @return In-progress count
     */
    public int getInProgressCount() {
        return (int) maintenanceRecords.stream()
            .filter(r -> r.status.equals("In Progress"))
            .count();
    }
    
    /**
     * Gets the count of completed records
     * @return Completed count
     */
    public int getCompletedCount() {
        return (int) maintenanceRecords.stream()
            .filter(r -> r.status.equals("Completed"))
            .count();
    }
    
    /**
     * Maintenance Record class representing a single maintenance task
     */
    public static class MaintenanceRecord {
        public String maintenanceId;
        public String vehicleId;
        public String description;
        public String status;
        public Date scheduledDate;
        public String notes;
        
        public MaintenanceRecord(String id, String vehicle, String desc, 
                                 String status, Date date, String notes) {
            this.maintenanceId = id;
            this.vehicleId = vehicle;
            this.description = desc;
            this.status = status;
            this.scheduledDate = date;
            this.notes = notes != null ? notes : "";
        }
        
        @Override
        public String toString() {
            return String.format("%s - %s (%s)", maintenanceId, description, status);
        }
    }
}