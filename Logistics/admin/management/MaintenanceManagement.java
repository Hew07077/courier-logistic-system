package admin.management;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
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
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_DISPLAY_FORMAT = "MMM dd, yyyy";
    
    // UI Components
    private JPanel mainPanel;
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel[] statLabels;
    private JPanel[] statCards;
    private JPanel statsPanel;
    private DashboardUpdateListener dashboardListener;
    
    // Filter state
    private String currentFilter = null;
    
    // Data
    private List<MaintenanceRecord> maintenanceRecords;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat(DATE_DISPLAY_FORMAT);
    
    // Reference to VehicleManagement
    private VehicleManagement vehicleManagement;
    
    // Modern color scheme
    private static final Color PRIMARY = new Color(41, 98, 255);
    private static final Color PRIMARY_DARK = new Color(30, 70, 180);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color SUCCESS_DARK = new Color(30, 126, 52);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color WARNING_DARK = new Color(204, 154, 6);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color DANGER_DARK = new Color(176, 42, 55);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color INFO_DARK = new Color(17, 122, 139);
    private static final Color PURPLE = new Color(111, 66, 193);
    
    private static final Color BG_COLOR = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color TEXT_MUTED = new Color(134, 142, 150);
    private static final Color HOVER_COLOR = new Color(245, 247, 250);
    private static final Color SELECTION_COLOR = new Color(230, 242, 255);
    private static final Color ACTIVE_FILTER_BORDER = new Color(41, 98, 255);
    private static final Color CALENDAR_HEADER_BG = new Color(41, 98, 255);
    private static final Color CALENDAR_HEADER_FG = Color.WHITE;
    private static final Color CALENDAR_DAY_HOVER = new Color(230, 242, 255);
    
    // Fonts - Using standard Java font constants
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font BUTTON_HOVER_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font CALENDAR_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font CALENDAR_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    private static final String[] TABLE_COLUMNS = {"ID", "Vehicle", "Description", "Status", "Date", "Notes"};
    
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
        initializeUI();
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
     * Initializes the UI
     */
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_COLOR);
        
        // Create all components
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createStatsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        // Update stats after UI is created
        SwingUtilities.invokeLater(this::updateStats);
    }
    
    /**
     * Resets borders of all stat cards
     */
    private void resetCardBorders() {
        if (statCards != null) {
            for (int i = 0; i < statCards.length; i++) {
                statCards[i].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        }
    }
    
    /**
     * Loads maintenance records from file or creates sample data
     */
    private void loadMaintenanceRecords() {
        File file = new File(MAINTENANCE_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                maintenanceRecords = reader.lines()
                    .filter(l -> !l.trim().isEmpty() && !l.startsWith("//"))
                    .map(this::parseMaintenanceRecord)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
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
     * @return The parsed MaintenanceRecord or null if parsing fails
     */
    private MaintenanceRecord parseMaintenanceRecord(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                return new MaintenanceRecord(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    dateFormat.parse(parts[4].trim()),
                    parts[5].trim()
                );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing maintenance record: " + line, e);
        }
        return null;
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
                String model = maintenanceVehicles.stream()
                    .filter(v -> v.id.equals(vehicleId))
                    .map(v -> v.model)
                    .findFirst()
                    .orElse("Unknown");
                
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
            .anyMatch(r -> r.vehicleId.equals(vehicleId) && !r.status.equals("Completed"));
        
        if (hasActiveRecord) {
            if (showMessage) {
                showWarning("Vehicle " + vehicleId + " already has an active maintenance record.");
            }
            return;
        }
        
        // Create new maintenance record
        String id = generateMaintenanceId();
        MaintenanceRecord newRecord = new MaintenanceRecord(
            id,
            vehicleId,
            description != null ? description : "Routine maintenance",
            "In Progress",
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
            showSuccess("Vehicle " + vehicleId + " added to maintenance as record " + id);
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
            .anyMatch(r -> r.vehicleId.equals(vehicleId) && !r.status.equals("Completed"));
    }
    
    /**
     * Initializes sample data for demonstration purposes
     */
    private void initializeSampleData() {
        try {
            Calendar cal = Calendar.getInstance();
            Date today = new Date();
            
            // Add more sample records to make the table fuller
            cal.setTime(today);
            cal.add(Calendar.MONTH, -2);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT001", "TRK001", "Oil Change", "Completed", 
                cal.getTime(), "Regular maintenance completed"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -5);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT002", "VAN001", "Brake Repair", "In Progress", 
                cal.getTime(), "Front brake pads replacement in progress"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -3);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT003", "TRK002", "Tire Replacement", "In Progress", 
                cal.getTime(), "All 4 tires being replaced"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.MONTH, -1);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT004", "CAR001", "Engine Check", "Completed", 
                cal.getTime(), "Check engine light diagnosis - Fixed"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT005", "VAN002", "Battery Replacement", "Scheduled", 
                cal.getTime(), "12V battery replacement scheduled"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -7);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT006", "TRK003", "Transmission Service", "Completed", 
                cal.getTime(), "Transmission fluid change"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -10);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT007", "CAR002", "AC Repair", "In Progress", 
                cal.getTime(), "AC compressor replacement"
            ));
            
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, -15);
            maintenanceRecords.add(new MaintenanceRecord(
                "MNT008", "TRK001", "Oil Change", "Scheduled", 
                cal.getTime(), "Regular maintenance"
            ));
            
            saveMaintenanceRecords();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize sample data", e);
        }
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
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * Creates the statistics panel showing counts by status - SMALLER CARDS, NO ICONS
     */
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Scheduled", "In Progress", "Completed", "Total"};
        String[] descriptions = {"Pending", "Ongoing", "Finished", "All records"};
        Color[] colors = {WARNING, INFO, SUCCESS, PRIMARY};
        Color[] bgColors = {
            new Color(255, 243, 224),
            new Color(227, 242, 253),
            new Color(232, 245, 233),
            new Color(230, 242, 255)
        };
        
        statLabels = new JLabel[4];
        statCards = new JPanel[4];
        
        for (int i = 0; i < 4; i++) {
            JPanel card = createStatCard(titles[i], descriptions[i], "0", colors[i], bgColors[i], i);
            statCards[i] = card;
            statsPanel.add(card);
        }
        
        return statsPanel;
    }
    
    /**
     * Creates an individual stat card - SMALLER SIZE, NO ICONS, CLICKABLE
     */
    private JPanel createStatCard(String title, String description, 
                                  String value, Color color, Color bgColor, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STATS_FONT);
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(SMALL_FONT);
        descLabel.setForeground(TEXT_MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);
        
        // Store reference to value label for updates
        statLabels[index] = valueLabel;
        
        // Make all cards clickable
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final String filterStatus = title;
        final int cardIndex = index;
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!title.equals(currentFilter)) {
                    card.setBackground(bgColor);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(color, 1, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                    ));
                }
            }
            
            public void mouseExited(MouseEvent e) {
                if (!title.equals(currentFilter)) {
                    card.setBackground(CARD_BG);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                }
            }
            
            public void mouseClicked(MouseEvent e) {
                // Reset all card borders
                resetCardBorders();
                
                if (title.equals("Total")) {
                    // Show all records
                    currentFilter = null;
                    applyFilter(null);
                    // No highlight for Total card
                } else {
                    // Set current filter
                    currentFilter = filterStatus;
                    
                    // Highlight selected card
                    card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                    ));
                    
                    // Apply filter to table
                    applyFilter(filterStatus);
                }
            }
        });
        
        return card;
    }
    
    /**
     * Applies filter to the table
     */
    private void applyFilter(String status) {
        if (status == null) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return status.equals(entry.getStringValue(3)); // Status column is index 3
                }
            });
        }
    }
    
    /**
     * Creates the table panel - BIGGER TABLE
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createTableHeaderCorner());
        
        // Make the scroll pane take all available space
        scrollPane.setPreferredSize(new Dimension(900, 500));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTableHeaderCorner() {
        JPanel corner = new JPanel();
        corner.setBackground(new Color(245, 245, 245));
        corner.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        return corner;
    }
    
    /**
     * Creates the maintenance table - BIGGER ROWS
     */
    private JTable createTable() {
        tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
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
        
        maintenanceTable = new JTable(tableModel);
        maintenanceTable.setRowHeight(55); // Taller rows
        maintenanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        maintenanceTable.setSelectionBackground(SELECTION_COLOR);
        maintenanceTable.setSelectionForeground(TEXT_PRIMARY);
        maintenanceTable.setShowGrid(true);
        maintenanceTable.setGridColor(BORDER_COLOR);
        maintenanceTable.setIntercellSpacing(new Dimension(10, 5));
        maintenanceTable.setFillsViewportHeight(true);
        
        // Table header styling
        JTableHeader header = maintenanceTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        // Row sorter
        rowSorter = new TableRowSorter<>(tableModel);
        maintenanceTable.setRowSorter(rowSorter);
        
        // Set column widths
        maintenanceTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        maintenanceTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        maintenanceTable.getColumnModel().getColumn(2).setPreferredWidth(280);
        maintenanceTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        maintenanceTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        maintenanceTable.getColumnModel().getColumn(5).setPreferredWidth(400);
        
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
        
        // Add hover effect
        maintenanceTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = maintenanceTable.rowAtPoint(e.getPoint());
                if (row >= 0 && maintenanceTable.getSelectedRow() != row) {
                    maintenanceTable.setRowSelectionInterval(row, row);
                }
            }
        });
        
        refreshTableData();
        return maintenanceTable;
    }
    
    /**
     * Creates the button panel with action buttons - REMOVED VIEW BUTTON
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("Add", PRIMARY, PRIMARY_DARK, this::showAddDialog),
            new ButtonConfig("Edit", WARNING, WARNING_DARK, this::showEditDialog),
            new ButtonConfig("Delete", DANGER, DANGER_DARK, this::deleteRecord),
            new ButtonConfig("Start", INFO, INFO_DARK, () -> updateStatus("In Progress")),
            new ButtonConfig("Complete", SUCCESS, SUCCESS_DARK, () -> updateStatus("Completed"))
        };
        
        for (ButtonConfig config : buttons) {
            panel.add(createActionButton(config));
        }
        
        return panel;
    }
    
    private static class ButtonConfig {
        final String text;
        final Color bgColor;
        final Color hoverColor;
        final Runnable action;
        
        ButtonConfig(String text, Color bgColor, Color hoverColor, Runnable action) {
            this.text = text;
            this.bgColor = bgColor;
            this.hoverColor = hoverColor;
            this.action = action;
        }
    }
    
    private JButton createActionButton(ButtonConfig config) {
        JButton btn = new JButton(config.text);
        btn.setBackground(config.bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(85, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(config.hoverColor);
                btn.setFont(BUTTON_HOVER_FONT);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(config.bgColor);
                btn.setFont(BUTTON_FONT);
            }
        });
        
        btn.addActionListener(e -> config.action.run());
        
        return btn;
    }
    
    /**
     * Custom cell renderer for status column
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JLabel label = new JLabel();
        
        public StatusCellRenderer() {
            panel.setOpaque(true);
            label.setFont(STATUS_FONT);
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            panel.add(label);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            panel.setBackground(isSelected ? SELECTION_COLOR : CARD_BG);
            
            if (value != null) {
                String status = value.toString();
                label.setText(status);
                
                if (status.equals("Scheduled")) {
                    label.setForeground(WARNING.darker());
                    label.setBackground(new Color(255, 243, 224));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(WARNING, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else if (status.equals("In Progress")) {
                    label.setForeground(INFO.darker());
                    label.setBackground(new Color(227, 242, 253));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(INFO, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else if (status.equals("Completed")) {
                    label.setForeground(SUCCESS.darker());
                    label.setBackground(new Color(232, 245, 233));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(SUCCESS, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                }
            }
            
            return panel;
        }
    }
    
    /**
     * Custom cell renderer for date column
     */
    private class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(CENTER);
            setFont(REGULAR_FONT);
            
            if (value instanceof Date) {
                Date date = (Date) value;
                String text = displayDateFormat.format(date);
                
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                
                Calendar recordDate = Calendar.getInstance();
                recordDate.setTime(date);
                recordDate.set(Calendar.HOUR_OF_DAY, 0);
                recordDate.set(Calendar.MINUTE, 0);
                recordDate.set(Calendar.SECOND, 0);
                recordDate.set(Calendar.MILLISECOND, 0);
                
                if (recordDate.before(today) && !isSelected) {
                    setForeground(DANGER);
                    setText(text + " (Overdue)");
                    setFont(getFont().deriveFont(Font.BOLD));
                    setBackground(new Color(255, 235, 238));
                } else if (recordDate.equals(today) && !isSelected) {
                    setForeground(WARNING);
                    setText(text + " (Today)");
                    setBackground(new Color(255, 243, 224));
                } else {
                    setForeground(TEXT_PRIMARY);
                    setText(text);
                    setBackground(isSelected ? SELECTION_COLOR : CARD_BG);
                }
            }
            
            setOpaque(true);
            return this;
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
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        
        JLabel titleLabel = new JLabel("Maintenance Record: " + record.maintenanceId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        addDetailRow(detailsPanel, "Vehicle ID:", record.vehicleId, gbc, 0);
        addDetailRow(detailsPanel, "Description:", record.description, gbc, 1);
        addDetailRow(detailsPanel, "Status:", record.status, gbc, 2);
        addDetailRow(detailsPanel, "Date:", displayDateFormat.format(record.scheduledDate) + 
                    " (" + taxStatus + ")", gbc, 3);
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
        gbc.weightx = 0.25;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(HEADER_FONT);
        labelComp.setForeground(TEXT_SECONDARY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(REGULAR_FONT);
        valueComp.setForeground(TEXT_PRIMARY);
        panel.add(valueComp, gbc);
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
        
        // Reapply current filter if any
        if (currentFilter != null) {
            applyFilter(currentFilter);
        }
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
     * Creates a date picker panel with spinner and improved calendar button
     */
    private JPanel createDatePicker(Date initialDate) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(CARD_BG);
        
        // Create spinner for date
        SpinnerDateModel dateModel = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, DATE_FORMAT);
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(REGULAR_FONT);
        dateSpinner.setPreferredSize(new Dimension(200, 35));
        
        // Calendar button with calendar icon (using Unicode character)
        JButton calendarBtn = new JButton("\uD83D\uDCC5"); // Unicode calendar icon
        calendarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        calendarBtn.setBackground(CARD_BG);
        calendarBtn.setForeground(PRIMARY);
        calendarBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        calendarBtn.setPreferredSize(new Dimension(45, 35));
        calendarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarBtn.setToolTipText("Open Calendar");
        calendarBtn.setFocusPainted(false);
        
        calendarBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                calendarBtn.setBackground(PRIMARY);
                calendarBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                calendarBtn.setBackground(CARD_BG);
                calendarBtn.setForeground(PRIMARY);
            }
        });
        
        calendarBtn.addActionListener(e -> showImprovedCalendarPopup(calendarBtn, dateSpinner));
        
        panel.add(dateSpinner, BorderLayout.CENTER);
        panel.add(calendarBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Shows an improved popup calendar with better design - FIXED VERSION
     */
    private void showImprovedCalendarPopup(Component parent, JSpinner dateSpinner) {
        // Create a new JDialog for the calendar
        JDialog calendarDialog = new JDialog();
        calendarDialog.setTitle("Select Date");
        calendarDialog.setModal(true);
        calendarDialog.setUndecorated(false); // Changed to false to show title
        calendarDialog.setAlwaysOnTop(true);
        
        // Get current date from spinner
        Date currentDate = (Date) dateSpinner.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        
        // Main calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout(10, 10));
        calendarPanel.setBackground(CARD_BG);
        calendarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Header with month and year
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton prevMonthBtn = new JButton("◀");
        styleCalendarNavButton(prevMonthBtn);
        
        JLabel monthLabel = new JLabel(new SimpleDateFormat("MMMM yyyy").format(currentDate), SwingConstants.CENTER);
        monthLabel.setFont(CALENDAR_HEADER_FONT);
        monthLabel.setForeground(Color.WHITE);
        
        JButton nextMonthBtn = new JButton("▶");
        styleCalendarNavButton(nextMonthBtn);
        
        headerPanel.add(prevMonthBtn, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextMonthBtn, BorderLayout.EAST);
        
        // Days of week header
        JPanel daysHeaderPanel = new JPanel(new GridLayout(1, 7, 5, 5));
        daysHeaderPanel.setBackground(CARD_BG);
        daysHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dayLabel.setForeground(day.equals("Sun") || day.equals("Sat") ? DANGER : TEXT_PRIMARY);
            daysHeaderPanel.add(dayLabel);
        }
        
        // Days grid panel
        JPanel daysGridPanel = new JPanel(new GridLayout(0, 7, 5, 5)); // Changed to 0 rows to auto-fit
        daysGridPanel.setBackground(CARD_BG);
        daysGridPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        
        // Create calendar for the current month
        Calendar displayCal = (Calendar) cal.clone();
        displayCal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Store current selection for highlighting
        Calendar today = Calendar.getInstance();
        
        // Function to update days grid
        Runnable updateDaysGrid = () -> {
            daysGridPanel.removeAll();
            
            int firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            // Add empty cells for days before first day of month
            for (int i = 1; i < firstDayOfWeek; i++) {
                JLabel emptyLabel = new JLabel("");
                emptyLabel.setPreferredSize(new Dimension(35, 35));
                daysGridPanel.add(emptyLabel);
            }
            
            // Add day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                final int selectedDay = day;
                JButton dayBtn = new JButton(String.valueOf(day));
                dayBtn.setFont(CALENDAR_FONT);
                dayBtn.setBackground(CARD_BG);
                dayBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
                dayBtn.setFocusPainted(false);
                dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                dayBtn.setPreferredSize(new Dimension(35, 35));
                
                // Check if this is the selected date
                boolean isSelectedDate = day == cal.get(Calendar.DAY_OF_MONTH) && 
                                         displayCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                                         displayCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR);
                
                // Check if this is today
                boolean isToday = day == today.get(Calendar.DAY_OF_MONTH) && 
                                  displayCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                  displayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR);
                
                if (isSelectedDate) {
                    dayBtn.setBackground(PRIMARY);
                    dayBtn.setForeground(Color.WHITE);
                    dayBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_DARK, 1));
                } else if (isToday) {
                    dayBtn.setBackground(new Color(255, 243, 224));
                    dayBtn.setForeground(WARNING.darker());
                    dayBtn.setBorder(BorderFactory.createLineBorder(WARNING, 1));
                }
                
                // Add hover effect
                dayBtn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (!isSelectedDate) {
                            dayBtn.setBackground(CALENDAR_DAY_HOVER);
                        }
                    }
                    public void mouseExited(MouseEvent e) {
                        if (!isSelectedDate) {
                            if (isToday) {
                                dayBtn.setBackground(new Color(255, 243, 224));
                            } else {
                                dayBtn.setBackground(CARD_BG);
                            }
                        }
                    }
                });
                
                dayBtn.addActionListener(e -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(displayCal.get(Calendar.YEAR), displayCal.get(Calendar.MONTH), selectedDay, 
                                 cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                    dateSpinner.setValue(selected.getTime());
                    calendarDialog.dispose();
                });
                
                daysGridPanel.add(dayBtn);
            }
            
            daysGridPanel.revalidate();
            daysGridPanel.repaint();
        };
        
        // Initial update
        updateDaysGrid.run();
        
        // Navigation actions
        prevMonthBtn.addActionListener(e -> {
            displayCal.add(Calendar.MONTH, -1);
            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(displayCal.getTime()));
            updateDaysGrid.run();
            calendarDialog.pack();
        });
        
        nextMonthBtn.addActionListener(e -> {
            displayCal.add(Calendar.MONTH, 1);
            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(displayCal.getTime()));
            updateDaysGrid.run();
            calendarDialog.pack();
        });
        
        // Footer with close button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(CARD_BG);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(REGULAR_FONT);
        closeBtn.setForeground(TEXT_SECONDARY);
        closeBtn.setBackground(CARD_BG);
        closeBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(80, 30));
        closeBtn.addActionListener(e -> calendarDialog.dispose());
        
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeBtn.setBackground(HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                closeBtn.setBackground(CARD_BG);
            }
        });
        
        footerPanel.add(closeBtn);
        
        // Assemble calendar
        calendarPanel.add(headerPanel, BorderLayout.NORTH);
        calendarPanel.add(daysHeaderPanel, BorderLayout.CENTER);
        calendarPanel.add(daysGridPanel, BorderLayout.SOUTH);
        
        // Create a container for the whole dialog
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CARD_BG);
        contentPanel.add(calendarPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        calendarDialog.add(contentPanel);
        calendarDialog.pack();
        calendarDialog.setLocationRelativeTo(parent);
        calendarDialog.setVisible(true);
    }
    
    private void styleCalendarNavButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(40, 30));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY);
            }
        });
    }
    
    /**
     * Shows dialog to add a new maintenance record - WITH IMPROVED DATE PICKER
     */
    private void showAddDialog() {
        JDialog dialog = createBaseDialog("Add Maintenance Record", 600, 650);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        mainPanel.setBackground(CARD_BG);
        
        // Get vehicle IDs from VehicleManagement if available
        JComboBox<String> vehicleCombo;
        if (vehicleManagement != null) {
            // Get all vehicles
            List<VehicleManagement.Vehicle> vehicles = vehicleManagement.getAllVehicles();
            String[] vehicleIds = vehicles.stream()
                .map(v -> v.id + " - " + v.model)
                .toArray(String[]::new);
            vehicleCombo = createStyledComboBox(vehicleIds);
        } else {
            // Fallback to text field if VehicleManagement not available
            vehicleCombo = new JComboBox<>(new String[]{"TRK001", "VAN001", "CAR001", "TRK002", "VAN002"});
            vehicleCombo.setEditable(true);
            styleComboBox(vehicleCombo);
        }
        
        JTextField descField = createStyledTextField();
        
        // Improved date picker panel
        JPanel datePickerPanel = createDatePicker(new Date());
        
        JComboBox<String> statusCombo = createStyledComboBox(new String[]{"Scheduled", "In Progress", "Completed"});
        JTextArea notesArea = createStyledTextArea();
        
        // Quick date buttons
        JPanel quickDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        quickDatePanel.setBackground(CARD_BG);
        
        JButton todayBtn = new JButton("Today");
        JButton tomorrowBtn = new JButton("Tomorrow");
        JButton nextWeekBtn = new JButton("Next Week");
        
        styleQuickDateButton(todayBtn);
        styleQuickDateButton(tomorrowBtn);
        styleQuickDateButton(nextWeekBtn);
        
        Calendar cal = Calendar.getInstance();
        
        todayBtn.addActionListener(e -> {
            ((SpinnerDateModel)((JSpinner)datePickerPanel.getComponent(0)).getModel()).setValue(new Date());
        });
        
        tomorrowBtn.addActionListener(e -> {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            ((SpinnerDateModel)((JSpinner)datePickerPanel.getComponent(0)).getModel()).setValue(cal.getTime());
        });
        
        nextWeekBtn.addActionListener(e -> {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, 7);
            ((SpinnerDateModel)((JSpinner)datePickerPanel.getComponent(0)).getModel()).setValue(cal.getTime());
        });
        
        quickDatePanel.add(todayBtn);
        quickDatePanel.add(tomorrowBtn);
        quickDatePanel.add(nextWeekBtn);
        
        // Form panel
        JPanel formPanel = createFormPanel(new FormField[]{
            new FormField("Vehicle:", vehicleCombo),
            new FormField("Description:", descField),
            new FormField("Date:", datePickerPanel),
            new FormField("Quick Select:", quickDatePanel),
            new FormField("Status:", statusCombo),
            new FormField("Notes:", new JScrollPane(notesArea))
        });
        
        // Button panel
        JPanel buttonPanel = createDialogButtonPanel(
            dialog,
            "Save Record",
            SUCCESS,
            SUCCESS_DARK,
            () -> {
                if (validateForm(descField)) {
                    try {
                        String id = generateMaintenanceId();
                        Date date = (Date) ((JSpinner)datePickerPanel.getComponent(0)).getValue();
                        String status = (String) statusCombo.getSelectedItem();
                        
                        // Get vehicle ID from combo box
                        String selectedVehicle = (String) vehicleCombo.getSelectedItem();
                        String vehicleId;
                        if (selectedVehicle.contains(" - ")) {
                            vehicleId = selectedVehicle.split(" - ")[0]; // Extract just the ID
                        } else {
                            vehicleId = selectedVehicle.trim().toUpperCase();
                        }
                        
                        MaintenanceRecord newRecord = new MaintenanceRecord(
                            id,
                            vehicleId,
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
                    } catch (Exception ex) {
                        showError("Error creating record: " + ex.getMessage());
                    }
                }
            }
        );
        
        assembleDialog(mainPanel, createHeaderLabel("Add Maintenance Record"), formPanel, buttonPanel);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void styleQuickDateButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(PRIMARY);
        button.setBackground(CARD_BG);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY, 1, true));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(85, 28));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(CARD_BG);
                button.setForeground(PRIMARY);
            }
        });
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(REGULAR_FONT);
        comboBox.setPreferredSize(new Dimension(300, 35));
        comboBox.setBackground(CARD_BG);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
    }
    
    private static class FormField {
        final String label;
        final JComponent field;
        
        FormField(String label, JComponent field) {
            this.label = label;
            this.field = field;
        }
    }
    
    private JPanel createFormPanel(FormField[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        for (int i = 0; i < fields.length; i++) {
            FormField field = fields[i];
            
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.weightx = 0.2;
            
            JLabel label = new JLabel(field.label);
            label.setFont(HEADER_FONT);
            label.setForeground(TEXT_PRIMARY);
            panel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 0.8;
            
            if (field.field instanceof JScrollPane) {
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weighty = 1.0;
            } else {
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 0;
            }
            
            panel.add(field.field, gbc);
        }
        
        return panel;
    }
    
    private JTextField createStyledTextField() {
        return createStyledTextField("");
    }
    
    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(300, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(REGULAR_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return area;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(REGULAR_FONT);
        combo.setPreferredSize(new Dimension(300, 35));
        combo.setBackground(CARD_BG);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        return combo;
    }
    
    private JDialog createBaseDialog(String title, int width, int height) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(CARD_BG);
        return dialog;
    }
    
    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(PRIMARY);
        return label;
    }
    
    private JPanel createDialogButtonPanel(JDialog dialog, String saveText, 
                                           Color saveColor, Color hoverColor, Runnable saveAction) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(CARD_BG);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(REGULAR_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveBtn = new JButton(saveText);
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(saveColor);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(100, 35));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(saveColor);
            }
        });
        
        saveBtn.addActionListener(e -> saveAction.run());
        
        panel.add(cancelBtn);
        panel.add(saveBtn);
        
        return panel;
    }
    
    private void assembleDialog(JPanel mainPanel, JLabel header, JPanel form, JPanel buttons) {
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(form, BorderLayout.CENTER);
        mainPanel.add(buttons, BorderLayout.SOUTH);
    }
    
    /**
     * Shows dialog to edit an existing maintenance record - WITH IMPROVED DATE PICKER
     */
    private void showEditDialog() {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (!validateRowSelection(selectedRow, "Select a record to edit")) {
            return;
        }
        
        MaintenanceRecord record = getSelectedRecord(selectedRow);
        if (record == null) return;
        
        JDialog dialog = createBaseDialog("Edit Record: " + record.maintenanceId, 600, 650);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        mainPanel.setBackground(CARD_BG);
        
        // Form fields
        JComboBox<String> vehicleCombo;
        if (vehicleManagement != null) {
            // Get all vehicles
            List<VehicleManagement.Vehicle> vehicles = vehicleManagement.getAllVehicles();
            String[] vehicleIds = vehicles.stream()
                .map(v -> v.id + " - " + v.model)
                .toArray(String[]::new);
            vehicleCombo = createStyledComboBox(vehicleIds);
            // Find and select the current vehicle
            for (int i = 0; i < vehicleIds.length; i++) {
                if (vehicleIds[i].startsWith(record.vehicleId)) {
                    vehicleCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            vehicleCombo = new JComboBox<>(new String[]{record.vehicleId});
            vehicleCombo.setEditable(true);
            styleComboBox(vehicleCombo);
        }
        
        JTextField descField = createStyledTextField(record.description);
        
        // Improved date picker panel
        JPanel datePickerPanel = createDatePicker(record.scheduledDate);
        
        JComboBox<String> statusCombo = createStyledComboBox(new String[]{"Scheduled", "In Progress", "Completed"});
        statusCombo.setSelectedItem(record.status);
        JTextArea notesArea = createStyledTextArea();
        notesArea.setText(record.notes);
        
        // Quick date buttons
        JPanel quickDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        quickDatePanel.setBackground(CARD_BG);
        
        JButton todayBtn = new JButton("Today");
        JButton tomorrowBtn = new JButton("Tomorrow");
        JButton nextWeekBtn = new JButton("Next Week");
        
        styleQuickDateButton(todayBtn);
        styleQuickDateButton(tomorrowBtn);
        styleQuickDateButton(nextWeekBtn);
        
        Calendar cal = Calendar.getInstance();
        
        todayBtn.addActionListener(e -> {
            ((SpinnerDateModel)((JSpinner)datePickerPanel.getComponent(0)).getModel()).setValue(new Date());
        });
        
        tomorrowBtn.addActionListener(e -> {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            ((SpinnerDateModel)((JSpinner)datePickerPanel.getComponent(0)).getModel()).setValue(cal.getTime());
        });
        
        nextWeekBtn.addActionListener(e -> {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, 7);
            ((SpinnerDateModel)((JSpinner)datePickerPanel.getComponent(0)).getModel()).setValue(cal.getTime());
        });
        
        quickDatePanel.add(todayBtn);
        quickDatePanel.add(tomorrowBtn);
        quickDatePanel.add(nextWeekBtn);
        
        // Form panel
        JPanel formPanel = createFormPanel(new FormField[]{
            new FormField("Vehicle:", vehicleCombo),
            new FormField("Description:", descField),
            new FormField("Date:", datePickerPanel),
            new FormField("Quick Select:", quickDatePanel),
            new FormField("Status:", statusCombo),
            new FormField("Notes:", new JScrollPane(notesArea))
        });
        
        // Button panel
        JPanel buttonPanel = createDialogButtonPanel(
            dialog,
            "Save Changes",
            WARNING,
            WARNING_DARK,
            () -> {
                try {
                    String oldStatus = record.status;
                    
                    // Get vehicle ID from combo box
                    String selectedVehicle = (String) vehicleCombo.getSelectedItem();
                    String vehicleId;
                    if (selectedVehicle.contains(" - ")) {
                        vehicleId = selectedVehicle.split(" - ")[0]; // Extract just the ID
                    } else {
                        vehicleId = selectedVehicle.trim().toUpperCase();
                    }
                    
                    record.vehicleId = vehicleId;
                    record.description = descField.getText().trim();
                    record.scheduledDate = (Date) ((JSpinner)datePickerPanel.getComponent(0)).getValue();
                    record.status = (String) statusCombo.getSelectedItem();
                    record.notes = notesArea.getText().trim();
                    
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
                } catch (Exception ex) {
                    showError("Error updating record: " + ex.getMessage());
                }
            }
        );
        
        assembleDialog(mainPanel, createHeaderLabel("Edit Record"), formPanel, buttonPanel);
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
    
    private boolean validateForm(JTextField descField) {
        if (descField.getText().trim().isEmpty()) {
            showWarning("Please enter description");
            return false;
        }
        return true;
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