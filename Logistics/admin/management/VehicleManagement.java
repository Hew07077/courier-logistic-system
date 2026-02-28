package admin.management;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleManagement {
    private JPanel mainPanel;
    private JTable vehiclesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Driver> drivers = new ArrayList<>();
    private Map<String, Integer> typeCounters = new HashMap<>();
    
    // Reference to MaintenanceManagement
    private MaintenanceManagement maintenanceManagement;
    
    // File paths
    private static final String VEHICLES_FILE = "vehicles_data.txt";
    private static final String DRIVERS_FILE = "drivers_data.txt";
    private static final String COUNTERS_FILE = "counters_data.txt";
    
    // UI Components
    private JTextField searchField;
    private JComboBox<String> statusFilter, typeFilter;
    private JPanel statsPanel;
    private JLabel[] statValues = new JLabel[5];
    
    // Colors - Modern Material Design palette
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
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public interface DriverProfileListener {
        void onDriverProfileClicked(String driverName);
    }
    private DriverProfileListener driverProfileListener;

    public VehicleManagement() {
        this(null);
    }
    
    public VehicleManagement(MaintenanceManagement maintenanceMgmt) {
        this.maintenanceManagement = maintenanceMgmt;
        loadData();
        // Sync with maintenance after loading
        if (maintenanceMgmt != null) {
            syncWithMaintenance();
        }
        createUI();
    }

    public void setDriverProfileListener(DriverProfileListener listener) {
        this.driverProfileListener = listener;
    }
    
    public void setMaintenanceManagement(MaintenanceManagement maintenanceMgmt) {
        this.maintenanceManagement = maintenanceMgmt;
        // Sync with maintenance management when setting it
        if (maintenanceMgmt != null) {
            syncWithMaintenance();
        }
    }

    private void loadData() {
        loadFromFile(VEHICLES_FILE, this::parseVehicle);
        loadFromFile(DRIVERS_FILE, this::parseDriver);
        loadFromFile(COUNTERS_FILE, line -> {
            String[] parts = line.split("=");
            if (parts.length == 2) typeCounters.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
        });
        
        if (vehicles.isEmpty()) createSampleData();
    }

    private void loadFromFile(String filename, LineProcessor processor) {
        File file = new File(filename);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().filter(l -> !l.trim().isEmpty() && !l.startsWith("//"))
                  .forEach(processor::process);
        } catch (IOException e) {
            showError("Error loading " + filename);
        }
    }

    private interface LineProcessor {
        void process(String line);
    }

    private void parseVehicle(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 8) {
                vehicles.add(new Vehicle(p[0].trim(), p[1].trim(), p[2].trim(), 
                    p[3].trim().isEmpty() ? null : p[3].trim(), p[4].trim(), p[5].trim(),
                    dateFormat.parse(p[6].trim()), p[7].trim()));
            }
        } catch (Exception e) { 
            System.err.println("Error parsing vehicle: " + line); 
        }
    }

    private void parseDriver(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 7) {
                drivers.add(new Driver(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                    p[4].trim(), dateFormat.parse(p[5].trim()), 
                    p[6].trim().isEmpty() ? null : p[6].trim()));
            }
        } catch (Exception e) { 
            System.err.println("Error parsing driver: " + line); 
        }
    }

    private void saveData() {
        saveToFile(VEHICLES_FILE, vehicles, v -> String.format("%s|%s|%s|%s|%s|%s|%s|%s",
            v.id, v.model, v.status, v.driverName == null ? "" : v.driverName,
            v.type, v.numberPlate, dateFormat.format(v.roadTaxExpiry), v.fuelType));
        
        saveToFile(DRIVERS_FILE, drivers, d -> String.format("%s|%s|%s|%s|%s|%s|%s",
            d.name, d.license, d.phone, d.email, d.status,
            dateFormat.format(d.joinedDate), d.currentVehicle == null ? "" : d.currentVehicle));
        
        saveToFile(COUNTERS_FILE, typeCounters.entrySet(), 
            e -> e.getKey() + "=" + e.getValue());
    }

    private <T> void saveToFile(String filename, Collection<T> data, DataFormatter<T> formatter) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("// Format: " + filename);
            writer.newLine();
            for (T item : data) {
                writer.write(formatter.format(item));
                writer.newLine();
            }
        } catch (IOException e) { 
            showError("Error saving " + filename);
        }
    }

    private interface DataFormatter<T> {
        String format(T item);
    }

    private void createUI() {
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

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Fleet Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Manage your vehicles, drivers, and fleet operations");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }

    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 5, 20, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        String[] titles = {"Active Vehicles", "In Maintenance", "Expired Tax", "Total Vehicles", "Available Drivers"};
        String[] icons = {"🚛", "🔧", "⚠️", "📊", "👤"};
        Color[] colors = {SUCCESS, WARNING, DANGER, PRIMARY, INFO};
        String[] descriptions = {
            "Currently on road", "Under service", "Tax expired", "In fleet", "Ready to assign"
        };
        
        for (int i = 0; i < 5; i++) {
            JPanel card = createModernStatCard(titles[i], icons[i], descriptions[i], "0", colors[i]);
            statsPanel.add(card);
        }
        
        return statsPanel;
    }

    private JPanel createModernStatCard(String title, String icon, String description, String value, Color color) {
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
        if (title.startsWith("Active")) statValues[0] = valueLabel;
        else if (title.startsWith("In Maintenance")) statValues[1] = valueLabel;
        else if (title.startsWith("Expired")) statValues[2] = valueLabel;
        else if (title.startsWith("Total")) statValues[3] = valueLabel;
        else if (title.startsWith("Available")) statValues[4] = valueLabel;
        
        // Make card clickable with hover effect
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                if (title.startsWith("Active")) {
                    statusFilter.setSelectedItem("Active");
                    filter();
                } else if (title.startsWith("In Maintenance")) {
                    statusFilter.setSelectedItem("Maintenance");
                    filter();
                } else if (title.startsWith("Expired")) {
                    filterExpiredTax();
                } else if (title.startsWith("Available")) {
                    showAvailableDrivers();
                }
            }
        });
        
        return card;
    }

    private void filterExpiredTax() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        filters.add(RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, new Date(), 4));
        rowSorter.setRowFilter(RowFilter.andFilter(filters));
    }
    
    private void showAvailableDrivers() {
        String availableDrivers = drivers.stream()
            .filter(d -> "Available".equals(d.status) && !"Unassigned".equals(d.name))
            .map(d -> "• " + d.name + " (" + d.license + ")")
            .collect(Collectors.joining("\n"));
        
        JOptionPane.showMessageDialog(mainPanel, 
            availableDrivers.isEmpty() ? "No available drivers" : availableDrivers,
            "Available Drivers", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        
        panel.add(createModernFilterPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        
        return panel;
    }

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

    private JTable createTable() {
        String[] columns = {"ID", "Model", "Type", "Plate", "Tax Expiry", "Fuel", "Status", "Driver"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        vehiclesTable = new JTable(tableModel);
        vehiclesTable.setRowHeight(45);
        vehiclesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vehiclesTable.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 30));
        vehiclesTable.setSelectionForeground(TEXT_PRIMARY);
        vehiclesTable.setShowGrid(true);
        vehiclesTable.setGridColor(BORDER_COLOR);
        vehiclesTable.setIntercellSpacing(new Dimension(10, 5));
        
        // Table header styling
        JTableHeader header = vehiclesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        rowSorter = new TableRowSorter<>(tableModel);
        vehiclesTable.setRowSorter(rowSorter);
        
        // Set column widths
        vehiclesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        vehiclesTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        vehiclesTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        vehiclesTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        vehiclesTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        vehiclesTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        vehiclesTable.getColumnModel().getColumn(7).setPreferredWidth(180);
        
        // Custom renderers
        vehiclesTable.getColumnModel().getColumn(4).setCellRenderer(new ModernDateCellRenderer());
        vehiclesTable.getColumnModel().getColumn(6).setCellRenderer(new ModernStatusCellRenderer());
        vehiclesTable.getColumnModel().getColumn(7).setCellRenderer(new ModernDriverCellRenderer());
        
        vehiclesTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && vehiclesTable.getSelectedRow() != -1) {
                    int row = vehiclesTable.convertRowIndexToModel(vehiclesTable.getSelectedRow());
                    if (vehiclesTable.getSelectedColumn() == 7) {
                        String driver = vehicles.get(row).driverName;
                        if (driver != null && !driver.equals("Unassigned") && driverProfileListener != null) {
                            driverProfileListener.onDriverProfileClicked(driver);
                        }
                    } else {
                        showVehicleDetails(vehicles.get(row));
                    }
                }
            }
        });
        
        refreshTable();
        return vehiclesTable;
    }

    private JPanel createModernFilterPanel() {
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
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, model, plate, driver...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        
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
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "Active", "Maintenance", "Inactive"});
        styleComboBox(statusFilter);
        statusPanel.add(statusFilter);
        panel.add(statusPanel);
        
        // Type filter
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        typePanel.setBackground(CARD_BG);
        typePanel.add(new JLabel("Type:") {{
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(TEXT_SECONDARY);
        }});
        
        typeFilter = new JComboBox<>(new String[]{"All Types", "Truck", "Van", "Car", "Motorcycle"});
        styleComboBox(typeFilter);
        typePanel.add(typeFilter);
        panel.add(typePanel);
        
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
            typeFilter.setSelectedIndex(0);
            filter();
        });
        
        clearBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                clearBtn.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent e) {
                clearBtn.setBackground(CARD_BG);
            }
        });
        
        panel.add(Box.createHorizontalStrut(10));
        panel.add(clearBtn);
        
        return panel;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setPreferredSize(new Dimension(120, 35));
        comboBox.setBackground(CARD_BG);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        comboBox.addActionListener(e -> filter());
    }

    private void filter() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        if (!searchField.getText().isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + searchField.getText(), 0, 1, 2, 3, 7));
        
        if (statusFilter.getSelectedIndex() > 0)
            filters.add(RowFilter.regexFilter(statusFilter.getSelectedItem().toString(), 6));
        
        if (typeFilter.getSelectedIndex() > 0)
            filters.add(RowFilter.regexFilter(typeFilter.getSelectedItem().toString(), 2));
        
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        panel.setBackground(BG_COLOR);
        
        String[] btns = {"➕ Add Vehicle", "✏️ Edit Vehicle", "👤 Assign Driver", 
                         "🚫 Unassign", "🔧 Maintenance", "📅 Update Tax", "📋 View Maintenance"};
        Color[] colors = {PRIMARY, new Color(255, 152, 0), SUCCESS, DANGER, WARNING, INFO, new Color(156, 39, 176)};
        Runnable[] actions = {this::addVehicle, this::editVehicle, this::assignDriver, 
                              this::unassignDriver, this::toggleMaintenance, this::updateTax, this::viewMaintenanceHistory};
        
        for (int i = 0; i < btns.length; i++) {
            JButton btn = createModernButton(btns[i], colors[i]);
            final int index = i;
            btn.addActionListener(e -> actions[index].run());
            panel.add(btn);
        }
        
        return panel;
    }

    private JButton createModernButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 40));
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

    // Method to sync vehicle status with maintenance records
    private void syncWithMaintenance() {
        if (maintenanceManagement == null) return;
        
        boolean changed = false;
        for (Vehicle v : vehicles) {
            boolean hasActiveMaintenance = maintenanceManagement.isVehicleInMaintenance(v.id);
            
            // If vehicle has active maintenance but status is not Maintenance, fix it
            if (hasActiveMaintenance && !v.status.equals("Maintenance")) {
                v.status = "Maintenance";
                changed = true;
            }
            // If vehicle doesn't have active maintenance but status is Maintenance, fix it
            else if (!hasActiveMaintenance && v.status.equals("Maintenance")) {
                v.status = "Active";
                changed = true;
            }
        }
        
        if (changed) {
            saveData();
            refreshTable();
        }
    }

    // New method to view maintenance history for a vehicle
    private void viewMaintenanceHistory() {
        int row = getSelectedRow();
        if (row == -1) { 
            showWarning("Please select a vehicle to view maintenance history"); 
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        if (maintenanceManagement == null) {
            showError("Maintenance Management module not available");
            return;
        }
        
        List<MaintenanceManagement.MaintenanceRecord> history = 
            maintenanceManagement.getVehicleMaintenanceHistory(v.id);
        
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel,
                "No maintenance records found for vehicle " + v.id,
                "Maintenance History",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create a nice dialog to show maintenance history
        JDialog dialog = new JDialog();
        dialog.setTitle("Maintenance History - " + v.id + " (" + v.model + ")");
        dialog.setModal(true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(CARD_BG);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("📋 Maintenance History for " + v.id + " - " + v.model);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);
        
        // Create table for maintenance records
        String[] columns = {"ID", "Date", "Description", "Status", "Notes"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        for (MaintenanceManagement.MaintenanceRecord record : history) {
            historyModel.addRow(new Object[]{
                record.maintenanceId,
                dateFormat.format(record.scheduledDate),
                record.description,
                record.status,
                record.notes
            });
        }
        
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(35);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(245, 245, 245));
        
        // Status column renderer
        historyTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String status = value.toString();
                    if (status.equals("Scheduled")) {
                        setForeground(WARNING);
                    } else if (status.equals("In Progress")) {
                        setForeground(INFO);
                    } else if (status.equals("Completed")) {
                        setForeground(SUCCESS);
                    }
                    setText("● " + status);
                }
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Stats panel
        long scheduled = history.stream().filter(r -> r.status.equals("Scheduled")).count();
        long inProgress = history.stream().filter(r -> r.status.equals("In Progress")).count();
        long completed = history.stream().filter(r -> r.status.equals("Completed")).count();
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(CARD_BG);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statsPanel.add(createHistoryStatCard("Scheduled", String.valueOf(scheduled), WARNING));
        statsPanel.add(createHistoryStatCard("In Progress", String.valueOf(inProgress), INFO));
        statsPanel.add(createHistoryStatCard("Completed", String.valueOf(completed), SUCCESS));
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createHistoryStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(245, 245, 245));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    // Add Vehicle dialog
    private void addVehicle() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Vehicle");
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
        JLabel titleLabel = new JLabel("➕ Add New Vehicle");
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
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Truck", "Van", "Car", "Motorcycle"});
        JTextField modelField = new JTextField(20);
        JTextField plateField = new JTextField(20);
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(new Date());
        
        // Style form fields
        styleFormField(typeCombo);
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(fuelCombo);
        styleFormField(dateSpinner);
        
        // Add fields with labels
        int row = 0;
        addFormField(formPanel, "Vehicle Type:", typeCombo, gbc, row++);
        addFormField(formPanel, "Model:", modelField, gbc, row++);
        addFormField(formPanel, "Number Plate:", plateField, gbc, row++);
        addFormField(formPanel, "Fuel Type:", fuelCombo, gbc, row++);
        addFormField(formPanel, "Road Tax Expiry:", dateSpinner, gbc, row++);
        
        // Preview panel
        JPanel previewPanel = createPreviewPanel(typeCombo, modelField, plateField);
        
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
        
        JButton saveBtn = new JButton("Save Vehicle");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(SUCCESS);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(120, 40));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            if (validateVehicleForm(modelField, plateField)) {
                String type = (String) typeCombo.getSelectedItem();
                String id = generateId(type);
                
                vehicles.add(new Vehicle(id, modelField.getText().trim(), "Active", null, type,
                    plateField.getText().trim().toUpperCase(), (Date) dateSpinner.getValue(), 
                    (String) fuelCombo.getSelectedItem()));
                
                saveData();
                refreshTable();
                showSuccess("Vehicle " + id + " added successfully");
                dialog.dispose();
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Edit Vehicle dialog
    private void editVehicle() {
        int row = getSelectedRow();
        if (row == -1) { 
            showWarning("Please select a vehicle to edit"); 
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Vehicle: " + v.id);
        dialog.setModal(true);
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(CARD_BG);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(CARD_BG);
        
        // Header with vehicle ID
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("✏️ Edit Vehicle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 152, 0));
        
        JLabel idLabel = new JLabel("  •  " + v.id);
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
        JTextField modelField = new JTextField(v.model);
        JTextField plateField = new JTextField(v.numberPlate);
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        fuelCombo.setSelectedItem(v.fuelType);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setSelectedItem(v.status);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setValue(v.roadTaxExpiry);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        
        // Driver info (read-only)
        JTextField driverField = new JTextField(v.driverName == null ? "Unassigned" : v.driverName);
        driverField.setEditable(false);
        driverField.setBackground(new Color(245, 245, 245));
        
        // Style fields
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(fuelCombo);
        styleFormField(statusCombo);
        styleFormField(dateSpinner);
        styleFormField(driverField);
        
        // Add fields
        int rowIndex = 0;
        addFormField(formPanel, "Model:", modelField, gbc, rowIndex++);
        addFormField(formPanel, "Number Plate:", plateField, gbc, rowIndex++);
        addFormField(formPanel, "Fuel Type:", fuelCombo, gbc, rowIndex++);
        addFormField(formPanel, "Status:", statusCombo, gbc, rowIndex++);
        addFormField(formPanel, "Current Driver:", driverField, gbc, rowIndex++);
        addFormField(formPanel, "Road Tax Expiry:", dateSpinner, gbc, rowIndex++);
        
        // Status warning panel
        JPanel warningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        warningPanel.setBackground(new Color(255, 243, 205));
        warningPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7)));
        
        JLabel warningLabel = new JLabel("⚠️ Changing status to 'Maintenance' will create a maintenance record and unassign the driver");
        warningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        warningLabel.setForeground(new Color(255, 193, 7).darker());
        warningPanel.add(warningLabel);
        
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
            String oldStatus = v.status;
            
            v.model = modelField.getText();
            v.numberPlate = plateField.getText().toUpperCase();
            v.fuelType = (String) fuelCombo.getSelectedItem();
            v.status = (String) statusCombo.getSelectedItem();
            v.roadTaxExpiry = (Date) dateSpinner.getValue();
            
            // If status changed to Maintenance, create maintenance record and unassign driver
            if (!oldStatus.equals(v.status) && v.status.equals("Maintenance")) {
                // Unassign driver if any
                if (v.driverName != null) {
                    findDriver(v.driverName).ifPresent(d -> {
                        d.currentVehicle = null;
                        d.status = "Available";
                    });
                    v.driverName = null;
                }
                
                // Create maintenance record if MaintenanceManagement is available
                if (maintenanceManagement != null) {
                    String description = JOptionPane.showInputDialog(dialog,
                        "Enter maintenance description for " + v.id + ":",
                        "Maintenance Required",
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (description != null && !description.trim().isEmpty()) {
                        maintenanceManagement.addVehicleToMaintenance(v.id, v.model, description);
                    } else {
                        maintenanceManagement.addVehicleToMaintenance(v.id, v.model, "Routine maintenance");
                    }
                }
            }
            // If status changed from Maintenance to something else, complete maintenance
            else if (oldStatus.equals("Maintenance") && !v.status.equals("Maintenance") && maintenanceManagement != null) {
                maintenanceManagement.completeMaintenanceForVehicle(v.id);
            }
            
            saveData();
            refreshTable();
            showSuccess("Vehicle " + v.id + " updated");
            dialog.dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        // Assemble dialog
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(CARD_BG);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(warningPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Helper method to create preview panel
    private JPanel createPreviewPanel(JComboBox<String> typeCombo, JTextField modelField, JTextField plateField) {
        JPanel previewPanel = new JPanel();
        previewPanel.setBackground(new Color(245, 245, 245));
        previewPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Preview",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            PRIMARY
        ));
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        
        JLabel previewLabel = new JLabel(" ");
        previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        previewLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        previewPanel.add(previewLabel);
        
        // Update preview when fields change
        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            private void updatePreview() {
                String type = (String) typeCombo.getSelectedItem();
                String model = modelField.getText().trim();
                String plate = plateField.getText().trim().toUpperCase();
                String preview = String.format("<html><b>New Vehicle:</b> %s %s<br><b>Plate:</b> %s<br><b>ID will be:</b> %s</html>",
                    type, model.isEmpty() ? "?" : model,
                    plate.isEmpty() ? "?" : plate,
                    type != null ? generateIdPreview(type) : "?");
                previewLabel.setText(preview);
            }
        };
        
        typeCombo.addActionListener(e -> listener.insertUpdate(null));
        modelField.getDocument().addDocumentListener(listener);
        plateField.getDocument().addDocumentListener(listener);
        
        listener.insertUpdate(null); // Initial update
        
        return previewPanel;
    }

    private String generateIdPreview(String type) {
        String prefix = type.equals("Truck") ? "TRK" : 
                       type.equals("Van") ? "VAN" : 
                       type.equals("Car") ? "CAR" : "MTC";
        int count = typeCounters.getOrDefault(type, 0) + 1;
        return prefix + String.format("%03d", count);
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
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        jLabel.setForeground(TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        panel.add(field, gbc);
    }

    private boolean validateVehicleForm(JTextField modelField, JTextField plateField) {
        if (modelField.getText().trim().isEmpty()) {
            showWarning("Please enter vehicle model");
            return false;
        }
        if (plateField.getText().trim().isEmpty()) {
            showWarning("Please enter number plate");
            return false;
        }
        return true;
    }

    private String generateId(String type) {
        String prefix = type.equals("Truck") ? "TRK" : 
                       type.equals("Van") ? "VAN" : 
                       type.equals("Car") ? "CAR" : "MTC";
        int count = typeCounters.getOrDefault(type, 0) + 1;
        typeCounters.put(type, count);
        return prefix + String.format("%03d", count);
    }

    private void assignDriver() {
        int row = getSelectedRow();
        if (row == -1) { showWarning("Please select a vehicle"); return; }
        
        Vehicle v = vehicles.get(row);
        
        if ("Maintenance".equals(v.status)) {
            showWarning("Cannot assign driver to vehicle in maintenance");
            return;
        }
        
        List<String> available = drivers.stream()
            .filter(d -> !"Unassigned".equals(d.name) && d.currentVehicle == null)
            .map(d -> d.name).collect(Collectors.toList());
        
        if (available.isEmpty()) {
            showWarning("No available drivers");
            return;
        }
        
        available.add(0, "Select a driver");
        
        JComboBox<String> driverCombo = new JComboBox<>(available.toArray(new String[0]));
        styleComboBox(driverCombo);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Assign Driver to " + v.id);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Select Driver:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(driverCombo, gbc);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(mainPanel, panel, "Assign Driver", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String driver = (String) driverCombo.getSelectedItem();
            if (driver != null && !driver.equals("Select a driver")) {
                if (v.driverName != null) {
                    findDriver(v.driverName).ifPresent(d -> {
                        d.currentVehicle = null;
                        d.status = "Available";
                    });
                }
                
                findDriver(driver).ifPresent(d -> {
                    d.currentVehicle = v.id;
                    d.status = "Active";
                });
                
                v.driverName = driver;
                v.status = "Active";
                saveData();
                refreshTable();
                showSuccess(driver + " assigned to " + v.id);
            }
        }
    }

    private Optional<Driver> findDriver(String name) {
        return drivers.stream().filter(d -> d.name.equals(name)).findFirst();
    }

    private void unassignDriver() {
        int row = getSelectedRow();
        if (row == -1) { showWarning("Please select a vehicle"); return; }
        
        Vehicle v = vehicles.get(row);
        if (v.driverName != null) {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "Are you sure you want to unassign " + v.driverName + " from " + v.id + "?",
                "Confirm Unassign", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                String driverName = v.driverName;
                findDriver(v.driverName).ifPresent(d -> {
                    d.currentVehicle = null;
                    d.status = "Available";
                });
                v.driverName = null;
                saveData();
                refreshTable();
                showSuccess(driverName + " unassigned from " + v.id);
            }
        } else {
            showWarning("No driver assigned to this vehicle");
        }
    }

    private void toggleMaintenance() {
        int row = getSelectedRow();
        if (row == -1) { showWarning("Please select a vehicle"); return; }
        
        Vehicle v = vehicles.get(row);
        
        if (v.status.equals("Maintenance")) {
            // Vehicle is coming out of maintenance
            v.status = "Active";
            
            // Notify MaintenanceManagement that vehicle is back in service
            if (maintenanceManagement != null) {
                maintenanceManagement.completeMaintenanceForVehicle(v.id);
            }
            
            showSuccess(v.id + " is now active");
        } else {
            // Vehicle is going into maintenance
            if (v.driverName != null) {
                int confirm = JOptionPane.showConfirmDialog(mainPanel,
                    v.driverName + " is assigned to this vehicle. Moving to maintenance will unassign them.\nContinue?",
                    "Confirm Maintenance", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    String driverName = v.driverName;
                    findDriver(v.driverName).ifPresent(d -> {
                        d.currentVehicle = null;
                        d.status = "Available";
                    });
                    v.driverName = null;
                    
                    // Create maintenance record
                    if (maintenanceManagement != null) {
                        String description = JOptionPane.showInputDialog(mainPanel,
                            "Enter maintenance description for " + v.id + ":",
                            "Maintenance Required",
                            JOptionPane.QUESTION_MESSAGE);
                        
                        if (description != null && !description.trim().isEmpty()) {
                            maintenanceManagement.addVehicleToMaintenance(v.id, v.model, description);
                        } else {
                            maintenanceManagement.addVehicleToMaintenance(v.id, v.model, "Routine maintenance");
                        }
                    }
                    
                    v.status = "Maintenance";
                    showWarning(driverName + " has been unassigned");
                } else {
                    return;
                }
            } else {
                // No driver assigned, just create maintenance record
                if (maintenanceManagement != null) {
                    String description = JOptionPane.showInputDialog(mainPanel,
                        "Enter maintenance description for " + v.id + ":",
                        "Maintenance Required",
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (description != null && !description.trim().isEmpty()) {
                        maintenanceManagement.addVehicleToMaintenance(v.id, v.model, description);
                    } else {
                        maintenanceManagement.addVehicleToMaintenance(v.id, v.model, "Routine maintenance");
                    }
                }
                v.status = "Maintenance";
            }
        }
        
        saveData();
        refreshTable();
    }

    private void updateTax() {
        int row = getSelectedRow();
        if (row == -1) { showWarning("Please select a vehicle"); return; }
        
        Vehicle v = vehicles.get(row);
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Update Road Tax - " + v.id);
        dialog.setModal(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(CARD_BG);
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel infoLabel = new JLabel("Current expiry: " + dateFormat.format(v.roadTaxExpiry));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("New Expiry Date:"), gbc);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setValue(v.roadTaxExpiry);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        
        gbc.gridx = 1;
        centerPanel.add(dateSpinner, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton updateBtn = new JButton("Update");
        updateBtn.setBackground(INFO);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.addActionListener(e -> {
            v.roadTaxExpiry = (Date) dateSpinner.getValue();
            saveData();
            refreshTable();
            showSuccess("Road tax updated for " + v.id);
            dialog.dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(updateBtn);
        
        panel.add(infoLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private int getSelectedRow() {
        int row = vehiclesTable.getSelectedRow();
        return row == -1 ? -1 : vehiclesTable.convertRowIndexToModel(row);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        vehicles.forEach(v -> tableModel.addRow(new Object[]{
            v.id, v.model, v.type, v.numberPlate, v.roadTaxExpiry, 
            v.fuelType, v.status, v.driverName == null ? "Unassigned" : v.driverName
        }));
        updateStats();
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            int active = getActiveCount();
            int maintenance = getMaintenanceCount();
            int expired = getExpiredRoadTaxCount();
            int total = getTotalCount();
            int available = getAvailableDriversCount();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(active));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(maintenance));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(expired));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(total));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(available));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        });
    }

    private void showVehicleDetails(Vehicle v) {
        String taxStatus = v.roadTaxExpiry.before(new Date()) ? "⚠️ Expired" : "✅ Valid";
        
        // Get maintenance info
        String maintenanceInfo = "";
        if (maintenanceManagement != null) {
            boolean inMaintenance = maintenanceManagement.isVehicleInMaintenance(v.id);
            if (inMaintenance) {
                maintenanceInfo = "\n🔧 Currently in maintenance";
            }
            
            List<MaintenanceManagement.MaintenanceRecord> history = 
                maintenanceManagement.getVehicleMaintenanceHistory(v.id);
            if (!history.isEmpty()) {
                maintenanceInfo += "\n📋 Total maintenance records: " + history.size();
            }
        }
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel titleLabel = new JLabel("Vehicle Details: " + v.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);
        
        // Details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addDetailRow(detailsPanel, "Model:", v.model, gbc, 0);
        addDetailRow(detailsPanel, "Type:", v.type, gbc, 1);
        addDetailRow(detailsPanel, "Number Plate:", v.numberPlate, gbc, 2);
        addDetailRow(detailsPanel, "Fuel Type:", v.fuelType, gbc, 3);
        addDetailRow(detailsPanel, "Status:", v.status, gbc, 4);
        addDetailRow(detailsPanel, "Tax Expiry:", dateFormat.format(v.roadTaxExpiry) + " (" + taxStatus + ")", gbc, 5);
        addDetailRow(detailsPanel, "Driver:", v.driverName == null ? "Unassigned" : v.driverName, gbc, 6);
        
        if (!maintenanceInfo.isEmpty()) {
            JLabel maintenanceLabel = new JLabel(maintenanceInfo);
            maintenanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            maintenanceLabel.setForeground(INFO);
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.gridwidth = 2;
            detailsPanel.add(maintenanceLabel, gbc);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(mainPanel, panel, "Vehicle Details", 
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

    private void createSampleData() {
        try {
            // Create dates
            Date expiredDate1 = dateFormat.parse("2024-12-31"); // Future date - not expired
            Date expiredDate2 = dateFormat.parse("2024-10-15"); // Past date - expired
            Date expiredDate3 = dateFormat.parse("2023-05-30"); // Past date - expired
            Date expiredDate4 = dateFormat.parse("2024-08-20"); // Past date - expired
            Date expiredDate5 = dateFormat.parse("2023-11-10"); // Past date - expired
            Date futureDate = dateFormat.parse("2025-06-15"); // Future date - valid
            
            // TRK001 - Active, valid tax (Dec 2024 is still in the future as of Feb 2025? Let's fix)
            // Since we're in 2026, Dec 2024 is expired, so let's make it valid for demo
            Date validDate = dateFormat.parse("2026-12-31");
            
            vehicles.add(new Vehicle("TRK001", "Freightliner Cascadia", "Active", "John Smith", 
                "Truck", "ABC1234", validDate, "Diesel"));
            
            // VAN001 - In Maintenance, expired tax
            vehicles.add(new Vehicle("VAN001", "Ford Transit", "Maintenance", null, 
                "Van", "DEF5678", expiredDate2, "Gasoline"));
            
            // TRK002 - In Maintenance, expired tax
            vehicles.add(new Vehicle("TRK002", "Peterbilt 579", "Maintenance", null, 
                "Truck", "GHI9012", expiredDate3, "Diesel"));
            
            // VAN002 - Active, expired tax (shows warning but still active)
            vehicles.add(new Vehicle("VAN002", "Mercedes Sprinter", "Active", null, 
                "Van", "JKL3456", expiredDate4, "Diesel"));
            
            // TRK003 - Active, expired tax
            vehicles.add(new Vehicle("TRK003", "International LT", "Active", null, 
                "Truck", "MNO7890", expiredDate5, "Diesel"));
            
            // CAR001 - Active, valid tax
            vehicles.add(new Vehicle("CAR001", "Bezza", "Active", "Mike Johnson", 
                "Car", "BTE4327", futureDate, "Diesel"));
            
            drivers.add(new Driver("John Smith", "DL12345", "555-0101", "john@email.com", 
                "Active", new Date(), "TRK001"));
            drivers.add(new Driver("Mike Johnson", "DL12346", "555-0102", "mike@email.com", 
                "Active", new Date(), "CAR001"));
            drivers.add(new Driver("Robert Brown", "DL12347", "555-0103", "robert@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("Sarah Wilson", "DL12348", "555-0104", "sarah@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("Unassigned", "", "", "", "Available", new Date(), null));
            
            typeCounters.put("Truck", 3);
            typeCounters.put("Van", 2);
            typeCounters.put("Car", 1);
            
            saveData();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void showError(String msg) { 
        JOptionPane.showMessageDialog(mainPanel, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String msg) { 
        JOptionPane.showMessageDialog(mainPanel, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(mainPanel, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== PUBLIC METHODS FOR DASHBOARD ==========
    
    public JPanel getMainPanel() { 
        return mainPanel; 
    }
    
    public JPanel getRefreshedPanel() {
        refreshData();
        return mainPanel;
    }
    
    public void refreshData() {
        vehicles.clear();
        drivers.clear();
        typeCounters.clear();
        loadData();
        // Sync with maintenance after loading
        if (maintenanceManagement != null) {
            syncWithMaintenance();
        }
        refreshTable();
    }
    
    public int getActiveCount() {
        return (int) vehicles.stream().filter(v -> "Active".equals(v.status)).count();
    }
    
    public int getMaintenanceCount() {
        return (int) vehicles.stream().filter(v -> "Maintenance".equals(v.status)).count();
    }
    
    public int getExpiredRoadTaxCount() {
        return (int) vehicles.stream().filter(v -> v.roadTaxExpiry.before(new Date())).count();
    }
    
    public int getTotalCount() {
        return vehicles.size();
    }
    
    public int getAvailableDriversCount() {
        return (int) drivers.stream().filter(d -> "Available".equals(d.status) && !"Unassigned".equals(d.name)).count();
    }
    
    // New method to get vehicles by status
    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicles.stream()
            .filter(v -> v.status.equals(status))
            .collect(Collectors.toList());
    }
    
    // New method to get all vehicles
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }
    
    // New method to complete maintenance for a vehicle
    public void completeMaintenanceForVehicle(String vehicleId) {
        findVehicle(vehicleId).ifPresent(v -> {
            if (v.status.equals("Maintenance")) {
                v.status = "Active";
                saveData();
                refreshTable();
            }
        });
    }
    
    private Optional<Vehicle> findVehicle(String id) {
        return vehicles.stream().filter(v -> v.id.equals(id)).findFirst();
    }

    // Modern Cell Renderers
    private class ModernStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v != null) {
                String status = v.toString();
                setHorizontalAlignment(CENTER);
                setText(" " + status + " ");
                
                if (status.equals("Active")) {
                    setForeground(SUCCESS);
                    setBackground(new Color(232, 245, 233));
                } else if (status.equals("Maintenance")) {
                    setForeground(WARNING);
                    setBackground(new Color(255, 243, 224));
                } else {
                    setForeground(TEXT_SECONDARY);
                    setBackground(new Color(250, 250, 250));
                }
                setFont(getFont().deriveFont(Font.BOLD, 12));
            }
            return this;
        }
    }

    private class ModernDriverCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v != null) {
                String driver = v.toString();
                if (!driver.equals("Unassigned")) {
                    setForeground(PRIMARY);
                    setFont(getFont().deriveFont(Font.BOLD));
                    setText("👤 " + driver);
                    setToolTipText("Double-click to view driver profile");
                } else {
                    setForeground(TEXT_SECONDARY);
                    setText("— " + driver + " —");
                }
            }
            return this;
        }
    }

    private class ModernDateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v instanceof Date) {
                Date date = (Date) v;
                String text = dateFormat.format(date);
                setHorizontalAlignment(CENTER);
                
                if (date.before(new Date())) {
                    setForeground(DANGER);
                    setText("⚠️ " + text + " (Expired)");
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

    // Data classes
    public static class Vehicle {
        public String id, model, status, driverName, type, numberPlate, fuelType;
        public Date roadTaxExpiry;
        
        public Vehicle(String id, String model, String status, String driver, String type,
                String plate, Date tax, String fuel) {
            this.id = id; this.model = model; this.status = status; this.driverName = driver;
            this.type = type; this.numberPlate = plate; this.roadTaxExpiry = tax; this.fuelType = fuel;
        }
    }

    private static class Driver {
        String name, license, phone, email, status, currentVehicle;
        Date joinedDate;
        
        Driver(String name, String license, String phone, String email, 
               String status, Date joined, String vehicle) {
            this.name = name; this.license = license; this.phone = phone; this.email = email;
            this.status = status; this.joinedDate = joined; this.currentVehicle = vehicle;
        }
    }
}