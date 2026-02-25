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
import javax.swing.RowFilter;
import javax.swing.border.*;

public class MaintenanceManagement {
    private JPanel mainPanel;
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<MaintenanceRecord> maintenanceRecords;
    
    // File for data persistence
    private static final String MAINTENANCE_FILE = "maintenance_data.txt";
    
    // UI Components
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel scheduledCountLabel;
    private JLabel inProgressCountLabel;
    private JLabel completedCountLabel;
    private JLabel totalCountLabel;
    
    // Color scheme - matching VehicleManagement
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color SECONDARY_COLOR = new Color(255, 160, 0);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color WARNING_COLOR = new Color(237, 108, 2);
    private static final Color DANGER_COLOR = new Color(198, 40, 40);
    private static final Color INFO_COLOR = new Color(2, 136, 209);
    private static final Color PURPLE_COLOR = new Color(156, 39, 176);
    private static final Color LIGHT_BG = new Color(250, 250, 250);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public MaintenanceManagement() {
        maintenanceRecords = new ArrayList<>();
        loadDataFromFile();
        createMainPanel();
    }
    
    private void loadDataFromFile() {
        maintenanceRecords.clear();
        File file = new File(MAINTENANCE_FILE);
        
        if (!file.exists()) {
            createSampleDataFile();
            loadDataFromFile();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("//")) {
                    MaintenanceRecord record = parseRecordFromLine(line);
                    if (record != null) {
                        maintenanceRecords.add(record);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error loading maintenance data: " + e.getMessage());
        }
    }
    
    private MaintenanceRecord parseRecordFromLine(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                String maintenanceId = parts[0].trim();
                String vehicleId = parts[1].trim();
                String description = parts[2].trim();
                String status = parts[3].trim();
                Date scheduledDate = dateFormat.parse(parts[4].trim());
                String notes = parts[5].trim();
                
                return new MaintenanceRecord(maintenanceId, vehicleId, description, status, scheduledDate, notes);
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
        }
        return null;
    }
    
    private void createSampleDataFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAINTENANCE_FILE))) {
            writer.write("// Format: MaintenanceID|VehicleID|Description|Status|ScheduledDate|Notes");
            writer.newLine();
            writer.write("MNT001|TRK001|Oil Change|Scheduled|2024-01-20|Regular maintenance - 5000 miles");
            writer.newLine();
            writer.write("MNT002|VAN001|Brake Repair|In Progress|2024-01-15|Front brake pads replacement");
            writer.newLine();
            writer.write("MNT003|TRK002|Tire Replacement|Completed|2024-01-10|All 6 tires replaced");
            writer.newLine();
            writer.write("MNT004|CAR001|Engine Check|Scheduled|2024-01-22|Check engine light on");
            writer.newLine();
            writer.write("MNT005|VAN002|Battery Replacement|Scheduled|2024-01-18|Battery showing low voltage");
            writer.newLine();
            writer.write("MNT006|TRK003|Transmission Service|In Progress|2024-01-12|Fluid change and inspection");
            writer.newLine();
            writer.write("MNT007|MTC001|Chain Replacement|Completed|2024-01-08|Drive chain and sprockets");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAINTENANCE_FILE))) {
            writer.write("// Format: MaintenanceID|VehicleID|Description|Status|ScheduledDate|Notes");
            writer.newLine();
            
            for (MaintenanceRecord record : maintenanceRecords) {
                String line = String.format("%s|%s|%s|%s|%s|%s",
                    record.getMaintenanceId(),
                    record.getVehicleId(),
                    record.getDescription(),
                    record.getStatus(),
                    dateFormat.format(record.getScheduledDate()),
                    record.getNotes()
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error saving maintenance data: " + e.getMessage());
        }
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(LIGHT_BG);
        
        // Top Panel with Title and Stats
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(LIGHT_BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(LIGHT_BG);
        
        JLabel titleIcon = new JLabel("🔧");
        titleIcon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("Maintenance Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Service & Repair Tracking");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleIcon);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(subtitleLabel);
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        
        // Stats Panel
        JPanel statsPanel = createStatsPanel();
        topPanel.add(statsPanel, BorderLayout.EAST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center Panel with Table and Filters
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(LIGHT_BG);
        
        // Filter Panel
        JPanel filterPanel = createFilterPanel();
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        createMaintenanceTable();
        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        
        scheduledCountLabel = createStatCard("Scheduled", String.valueOf(getScheduledCount()), WARNING_COLOR, "📅");
        inProgressCountLabel = createStatCard("In Progress", String.valueOf(getInProgressCount()), INFO_COLOR, "🔧");
        completedCountLabel = createStatCard("Completed", String.valueOf(getCompletedCount()), SUCCESS_COLOR, "✅");
        totalCountLabel = createStatCard("Total Tasks", String.valueOf(getTotalCount()), PRIMARY_COLOR, "📊");
        
        statsPanel.add(scheduledCountLabel);
        statsPanel.add(inProgressCountLabel);
        statsPanel.add(completedCountLabel);
        statsPanel.add(totalCountLabel);
        
        return statsPanel;
    }
    
    private JLabel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(5));
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
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search maintenance records...");
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
        
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        filterPanel.add(searchPanel);
        
        // Status Filter
        filterPanel.add(createFilterLabel("Status:"));
        String[] statuses = {"All Status", "Scheduled", "In Progress", "Completed"};
        statusFilter = new JComboBox<>(statuses);
        styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> filterTable());
        filterPanel.add(statusFilter);
        
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
    
    private void createMaintenanceTable() {
        String[] columns = {"ID", "Vehicle ID", "Description", "Status", "Scheduled Date", "Notes"};
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
        
        maintenanceTable = new JTable(tableModel);
        maintenanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        maintenanceTable.setRowHeight(45);
        maintenanceTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
        maintenanceTable.setSelectionForeground(TEXT_PRIMARY);
        maintenanceTable.setShowGrid(true);
        maintenanceTable.setGridColor(BORDER_COLOR);
        maintenanceTable.setIntercellSpacing(new Dimension(10, 5));
        
        // Modern table header
        JTableHeader header = maintenanceTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(Color.WHITE);
        header.setForeground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        
        // Set column widths
        maintenanceTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        maintenanceTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Vehicle ID
        maintenanceTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        maintenanceTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        maintenanceTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Scheduled Date
        maintenanceTable.getColumnModel().getColumn(5).setPreferredWidth(250); // Notes
        
        // Set up row sorter
        rowSorter = new TableRowSorter<>(tableModel);
        maintenanceTable.setRowSorter(rowSorter);
        
        // Add custom cell renderers
        maintenanceTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        maintenanceTable.getColumnModel().getColumn(4).setCellRenderer(new DateCellRenderer());
        
        // Add mouse listener for double-click
        maintenanceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = maintenanceTable.getSelectedRow();
                    if (row != -1) {
                        showMaintenanceDetails(row);
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
        
        JButton addButton = createModernButton("➕ Schedule Maintenance", PRIMARY_COLOR);
        JButton editButton = createModernButton("✏️ Edit Record", SUCCESS_COLOR);
        JButton deleteButton = createModernButton("🗑️ Delete", DANGER_COLOR);
        JButton completeButton = createModernButton("✅ Mark Completed", SUCCESS_COLOR);
        JButton progressButton = createModernButton("🔄 Start Progress", INFO_COLOR);
        JButton refreshButton = createModernButton("🔄 Refresh", TEXT_SECONDARY);
        
        addButton.addActionListener(this::showAddMaintenanceDialog);
        editButton.addActionListener(this::showEditMaintenanceDialog);
        deleteButton.addActionListener(this::deleteMaintenance);
        completeButton.addActionListener(this::markAsCompleted);
        progressButton.addActionListener(this::markAsInProgress);
        refreshButton.addActionListener(e -> refreshData());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(progressButton);
        buttonPanel.add(refreshButton);
        
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
        button.setPreferredSize(new Dimension(140, 38));
        
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
        for (MaintenanceRecord record : maintenanceRecords) {
            tableModel.addRow(new Object[]{
                record.getMaintenanceId(),
                record.getVehicleId(),
                record.getDescription(),
                record.getStatus(),
                record.getScheduledDate(),
                record.getNotes()
            });
        }
        updateStats();
    }
    
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 0, 1, 2, 5));
        }
        
        if (!"All Status".equals(status)) {
            filters.add(RowFilter.regexFilter(status, 3));
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
        filterTable();
    }
    
    private void showMaintenanceDetails(int row) {
        int modelRow = maintenanceTable.convertRowIndexToModel(row);
        MaintenanceRecord record = maintenanceRecords.get(modelRow);
        
        JDialog dialog = createModernDialog("Maintenance Details - " + record.getMaintenanceId(), 500, 450);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel("🔧");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel idLabel = new JLabel(record.getMaintenanceId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        idLabel.setForeground(PRIMARY_COLOR);
        
        JLabel vehicleLabel = new JLabel("Vehicle: " + record.getVehicleId());
        vehicleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        vehicleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(idLabel);
        titlePanel.add(vehicleLabel);
        
        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Details
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 1;
        
        addDetailRow(detailsPanel, gbc, "Description:", record.getDescription(), 0);
        addDetailRow(detailsPanel, gbc, "Status:", record.getStatus(), 1);
        addDetailRow(detailsPanel, gbc, "Scheduled Date:", dateFormat.format(record.getScheduledDate()), 2);
        addDetailRow(detailsPanel, gbc, "Notes:", record.getNotes(), 3);
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton closeButton = createModernButton("Close", TEXT_SECONDARY);
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dialog.dispose());
        
        if (!"Completed".equals(record.getStatus())) {
            JButton completeButton = createModernButton("Mark Completed", SUCCESS_COLOR);
            completeButton.setPreferredSize(new Dimension(140, 35));
            completeButton.addActionListener(e -> {
                record.setStatus("Completed");
                saveDataToFile();
                refreshData();
                dialog.dispose();
                showSuccessDialog("Maintenance marked as completed!");
            });
            buttonPanel.add(completeButton);
        }
        
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
    
    private void showAddMaintenanceDialog(ActionEvent e) {
        JDialog dialog = createModernDialog("Schedule New Maintenance", 550, 500);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        String[] labels = {"Vehicle ID:", "Description:", "Scheduled Date:", "Status:", "Notes:"};
        JTextField[] fields = new JTextField[2];
        fields[0] = new JTextField(20); // Vehicle ID
        fields[1] = new JTextField(20); // Description
        styleTextField(fields[0]);
        styleTextField(fields[1]);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Scheduled", "In Progress"});
        styleComboBox(statusCombo);
        
        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
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
                formPanel.add(fields[0], gbc);
            } else if (i == 1) {
                formPanel.add(fields[1], gbc);
            } else if (i == 2) {
                formPanel.add(dateSpinner, gbc);
            } else if (i == 3) {
                formPanel.add(statusCombo, gbc);
            } else if (i == 4) {
                JScrollPane notesScroll = new JScrollPane(notesArea);
                notesScroll.setPreferredSize(new Dimension(200, 60));
                formPanel.add(notesScroll, gbc);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createModernButton("Schedule", SUCCESS_COLOR);
        saveButton.setPreferredSize(new Dimension(120, 35));
        saveButton.addActionListener(saveEvent -> {
            try {
                String vehicleId = fields[0].getText();
                String description = fields[1].getText();
                Date scheduledDate = (Date) dateSpinner.getValue();
                String status = (String) statusCombo.getSelectedItem();
                String notes = notesArea.getText();
                
                if (vehicleId.isEmpty() || description.isEmpty()) {
                    showWarningDialog("Please fill in all required fields!");
                    return;
                }
                
                String maintenanceId = generateMaintenanceId();
                
                MaintenanceRecord newRecord = new MaintenanceRecord(
                    maintenanceId, vehicleId, description, status, scheduledDate, notes);
                maintenanceRecords.add(newRecord);
                
                saveDataToFile();
                
                showSuccessDialog("Maintenance scheduled successfully!\nID: " + maintenanceId);
                refreshData();
                dialog.dispose();
            } catch (Exception ex) {
                showErrorDialog("Error scheduling maintenance: " + ex.getMessage());
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
    
    private void showEditMaintenanceDialog(ActionEvent e) {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a maintenance record to edit.");
            return;
        }
        
        int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);
        MaintenanceRecord record = maintenanceRecords.get(modelRow);
        
        JDialog dialog = createModernDialog("Edit Maintenance Record", 550, 500);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField vehicleField = new JTextField(record.getVehicleId(), 20);
        JTextField descField = new JTextField(record.getDescription(), 20);
        styleTextField(vehicleField);
        styleTextField(descField);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(record.getScheduledDate());
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Scheduled", "In Progress", "Completed"});
        statusCombo.setSelectedItem(record.getStatus());
        styleComboBox(statusCombo);
        
        JTextArea notesArea = new JTextArea(record.getNotes(), 3, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        String[] labels = {"Maintenance ID:", "Vehicle ID:", "Description:", "Scheduled Date:", "Status:", "Notes:"};
        JComponent[] components = {
            new JLabel(record.getMaintenanceId()),
            vehicleField,
            descField,
            dateSpinner,
            statusCombo,
            new JScrollPane(notesArea)
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
            } else if (components[i] instanceof JScrollPane) {
                components[i].setPreferredSize(new Dimension(200, 60));
                formPanel.add(components[i], gbc);
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
        
        JButton saveButton = createModernButton("Save Changes", SUCCESS_COLOR);
        saveButton.setPreferredSize(new Dimension(130, 35));
        saveButton.addActionListener(saveEvent -> {
            try {
                record.setVehicleId(vehicleField.getText());
                record.setDescription(descField.getText());
                record.setScheduledDate((Date) dateSpinner.getValue());
                record.setStatus((String) statusCombo.getSelectedItem());
                record.setNotes(notesArea.getText());
                
                saveDataToFile();
                
                showSuccessDialog("Maintenance record updated successfully!");
                refreshData();
                dialog.dispose();
            } catch (Exception ex) {
                showErrorDialog("Error updating record: " + ex.getMessage());
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
    
    private void deleteMaintenance(ActionEvent e) {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a maintenance record to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Are you sure you want to delete this maintenance record?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);
            MaintenanceRecord record = maintenanceRecords.get(modelRow);
            maintenanceRecords.remove(modelRow);
            saveDataToFile();
            refreshData();
            showSuccessDialog("Maintenance record deleted successfully!");
        }
    }
    
    private void markAsCompleted(ActionEvent e) {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a maintenance record to mark as completed.");
            return;
        }
        
        int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);
        MaintenanceRecord record = maintenanceRecords.get(modelRow);
        
        if (!"Completed".equals(record.getStatus())) {
            record.setStatus("Completed");
            saveDataToFile();
            refreshData();
            showSuccessDialog("Maintenance marked as completed!");
        } else {
            showWarningDialog("This record is already completed.");
        }
    }
    
    private void markAsInProgress(ActionEvent e) {
        int selectedRow = maintenanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select a maintenance record to start.");
            return;
        }
        
        int modelRow = maintenanceTable.convertRowIndexToModel(selectedRow);
        MaintenanceRecord record = maintenanceRecords.get(modelRow);
        
        if ("Scheduled".equals(record.getStatus())) {
            record.setStatus("In Progress");
            saveDataToFile();
            refreshData();
            showSuccessDialog("Maintenance marked as In Progress!");
        } else {
            showWarningDialog("This record cannot be marked as In Progress (current status: " + record.getStatus() + ")");
        }
    }
    
    private String generateMaintenanceId() {
        int maxId = 0;
        for (MaintenanceRecord record : maintenanceRecords) {
            String id = record.getMaintenanceId();
            if (id.startsWith("MNT")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("MNT%03d", maxId + 1);
    }
    
    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
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
        JOptionPane.showMessageDialog(mainPanel, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(mainPanel, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(mainPanel, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void updateStats() {
        scheduledCountLabel.setText(String.valueOf(getScheduledCount()));
        inProgressCountLabel.setText(String.valueOf(getInProgressCount()));
        completedCountLabel.setText(String.valueOf(getCompletedCount()));
        totalCountLabel.setText(String.valueOf(getTotalCount()));
    }
    
    public void refreshData() {
        loadDataFromFile();
        refreshTableData();
        updateStats();
    }
    
    public int getScheduledCount() {
        return (int) maintenanceRecords.stream()
            .filter(m -> "Scheduled".equals(m.getStatus()))
            .count();
    }
    
    public int getInProgressCount() {
        return (int) maintenanceRecords.stream()
            .filter(m -> "In Progress".equals(m.getStatus()))
            .count();
    }
    
    public int getCompletedCount() {
        return (int) maintenanceRecords.stream()
            .filter(m -> "Completed".equals(m.getStatus()))
            .count();
    }
    
    public int getTotalCount() {
        return maintenanceRecords.size();
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
                
                String icon = "● ";
                switch (status) {
                    case "Scheduled":
                        label.setForeground(WARNING_COLOR);
                        label.setText(icon + "Scheduled");
                        break;
                    case "In Progress":
                        label.setForeground(INFO_COLOR);
                        label.setText(icon + "In Progress");
                        break;
                    case "Completed":
                        label.setForeground(SUCCESS_COLOR);
                        label.setText("✅ Completed");
                        break;
                    default:
                        label.setForeground(TEXT_PRIMARY);
                        label.setText(status);
                }
                
                label.setFont(label.getFont().deriveFont(Font.BOLD));
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
                
                // Check if scheduled date is overdue for non-completed tasks
                if (date.before(today)) {
                    int modelRow = table.convertRowIndexToModel(row);
                    MaintenanceRecord record = maintenanceRecords.get(modelRow);
                    if (!"Completed".equals(record.getStatus())) {
                        label.setForeground(DANGER_COLOR);
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                        label.setText("⚠️ " + label.getText() + " (Overdue)");
                    }
                }
            }
            
            return c;
        }
    }
    
    // Maintenance Record class
    private class MaintenanceRecord {
        private String maintenanceId;
        private String vehicleId;
        private String description;
        private String status;
        private Date scheduledDate;
        private String notes;
        
        public MaintenanceRecord(String maintenanceId, String vehicleId, String description, 
                                String status, Date scheduledDate, String notes) {
            this.maintenanceId = maintenanceId;
            this.vehicleId = vehicleId;
            this.description = description;
            this.status = status;
            this.scheduledDate = scheduledDate;
            this.notes = notes;
        }
        
        public String getMaintenanceId() { return maintenanceId; }
        public String getVehicleId() { return vehicleId; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public Date getScheduledDate() { return scheduledDate; }
        public String getNotes() { return notes; }
        
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
        public void setDescription(String description) { this.description = description; }
        public void setStatus(String status) { this.status = status; }
        public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}