package admin.management;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Timer;

public class VehicleManagement {
    private JPanel mainPanel;
    private JTable vehiclesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Driver> drivers = new ArrayList<>();
    private Map<String, Integer> typeCounters = new HashMap<>();
    
    private MaintenanceManagement maintenanceManagement;
    
    private static final String VEHICLES_FILE = "vehicles_data.txt";
    private static final String DRIVERS_FILE = "drivers_data.txt";
    private static final String COUNTERS_FILE = "counters_data.txt";
    
    private JComboBox<String> statusFilter, typeFilter;
    private JPanel statsPanel;
    private JLabel[] statValues = new JLabel[5];
    private JPanel[] statCards = new JPanel[5];
    
    // Filter state
    private String currentFilter = null;
    private int currentFilterIndex = -1;
    
    // Modern color scheme matching MaintenanceManagement
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
    
    // Fonts - Matching MaintenanceManagement
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font BUTTON_HOVER_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 12);
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    
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
        
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createStatsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        SwingUtilities.invokeLater(() -> updateStats());
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
                statCards[i].setBackground(CARD_BG);
            }
        }
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Vehicle Management");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Manage your fleet vehicles and drivers");
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }

    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Active", "Maintenance", "Expired Tax", "Total Fleet", "Available"};
        String[] descriptions = {"Operating", "In service", "Overdue", "All vehicles", "Drivers"};
        Color[] colors = {SUCCESS, INFO, WARNING, PRIMARY, PURPLE};
        Color[] bgColors = {
            new Color(232, 245, 233),
            new Color(227, 242, 253),
            new Color(255, 243, 224),
            new Color(230, 242, 255),
            new Color(243, 232, 255)
        };
        
        statValues = new JLabel[5];
        statCards = new JPanel[5];
        
        for (int i = 0; i < 5; i++) {
            JPanel card = createStatCard(titles[i], descriptions[i], "0", colors[i], bgColors[i], i);
            statCards[i] = card;
            statsPanel.add(card);
        }
        
        return statsPanel;
    }

    private JPanel createStatCard(String title, String description, String value, 
                                  Color color, Color bgColor, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
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
        statValues[index] = valueLabel;
        
        // Make all filterable cards clickable
        if (title.equals("Active") || title.equals("Maintenance") || title.equals("Expired Tax")) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final String filterStatus = title;
            final int cardIndex = index;
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(bgColor);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(color, 1, true),
                            BorderFactory.createEmptyBorder(7, 11, 7, 11)
                        ));
                    }
                }
                
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(CARD_BG);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                        ));
                    }
                }
                
                public void mouseClicked(MouseEvent e) {
                    applyStatusFilter(filterStatus, cardIndex, color);
                }
            });
        } else if (title.equals("Available")) {
            // Available drivers card - show dialog on click
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(HOVER_COLOR);
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(CARD_BG);
                }
                public void mouseClicked(MouseEvent e) {
                    showAvailableDrivers();
                }
            });
        } else if (title.equals("Total Fleet")) {
            // Total fleet card - clear all filters
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(HOVER_COLOR);
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(CARD_BG);
                }
                public void mouseClicked(MouseEvent e) {
                    clearAllFilters();
                }
            });
        }
        
        return card;
    }

    /**
     * Apply filter by status
     */
    private void applyStatusFilter(String status, int cardIndex, Color color) {
        System.out.println("Applying filter: " + status); // Debug line
        
        // Reset all card borders
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            // Clicking the same card again - clear filter
            currentFilter = null;
            currentFilterIndex = -1;
            statusFilter.setSelectedIndex(0);
            // Apply only type filter if any
            applyFilters();
        } else {
            // Apply new filter
            currentFilter = status;
            currentFilterIndex = cardIndex;
            
            // Highlight selected card
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            // Update status filter dropdown without triggering action listener
            statusFilter.removeActionListener(statusFilter.getActionListeners()[0]);
            statusFilter.setSelectedItem(status);
            styleFilterCombo(statusFilter); // Re-add listener
            
            // Apply filters
            applyFilters();
        }
    }

    /**
     * Apply all active filters (status and type)
     */
    private void applyFilters() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        // Add status filter if active
        if (currentFilter != null) {
            String statusForFilter = currentFilter;
            if (currentFilter.equals("Expired Tax")) {
                // For expired tax, we need a special filter based on date
                filters.add(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        Date taxDate = (Date) entry.getValue(4); // Tax Expiry column index
                        return taxDate != null && taxDate.before(new Date());
                    }
                });
            } else {
                filters.add(RowFilter.regexFilter("^" + statusForFilter + "$", 6)); // Status column index
            }
        }
        
        // Add type filter if selected
        if (typeFilter.getSelectedIndex() > 0) {
            filters.add(RowFilter.regexFilter("^" + typeFilter.getSelectedItem().toString() + "$", 2)); // Type column index
        }
        
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    /**
     * Clear all filters
     */
    private void clearAllFilters() {
        resetCardBorders();
        currentFilter = null;
        currentFilterIndex = -1;
        statusFilter.setSelectedIndex(0);
        typeFilter.setSelectedIndex(0);
        rowSorter.setRowFilter(null);
        showNotification("All filters cleared", INFO);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_COLOR);
        
        panel.add(createFilterBar(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createTableHeaderCorner());
        
        // Make the scroll pane take all available space
        scrollPane.setPreferredSize(new Dimension(1000, 550));
        
        // Custom scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createTableHeaderCorner() {
        JPanel corner = new JPanel();
        corner.setBackground(new Color(245, 245, 245));
        corner.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        return corner;
    }

    private JTable createTable() {
        String[] columns = {"ID", "Vehicle", "Type", "Plate", "Tax Expiry", "Fuel", "Status", "Driver"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Date.class;
                return String.class;
            }
        };
        
        vehiclesTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                
                // Alternating row colors
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        comp.setBackground(new Color(252, 252, 253));
                    } else {
                        comp.setBackground(CARD_BG);
                    }
                } else {
                    comp.setBackground(SELECTION_COLOR);
                }
                
                return comp;
            }
        };
        
        vehiclesTable.setRowHeight(55); // Taller rows
        vehiclesTable.setFont(REGULAR_FONT);
        vehiclesTable.setSelectionBackground(SELECTION_COLOR);
        vehiclesTable.setSelectionForeground(TEXT_PRIMARY);
        vehiclesTable.setShowGrid(true);
        vehiclesTable.setGridColor(BORDER_COLOR);
        vehiclesTable.setIntercellSpacing(new Dimension(10, 5));
        vehiclesTable.setFillsViewportHeight(true);
        
        // Table header styling
        JTableHeader header = vehiclesTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        // Row sorter
        rowSorter = new TableRowSorter<>(tableModel);
        vehiclesTable.setRowSorter(rowSorter);
        
        // Set column widths
        vehiclesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        vehiclesTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        vehiclesTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        vehiclesTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        vehiclesTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        vehiclesTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        vehiclesTable.getColumnModel().getColumn(7).setPreferredWidth(200);
        
        // Set custom renderers
        vehiclesTable.getColumnModel().getColumn(4).setCellRenderer(new DateCellRenderer());
        vehiclesTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        vehiclesTable.getColumnModel().getColumn(7).setCellRenderer(new DriverCellRenderer());
        
        // Double-click listener
        vehiclesTable.addMouseListener(new MouseAdapter() {
            @Override
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
        
        // Add hover effect
        vehiclesTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = vehiclesTable.rowAtPoint(e.getPoint());
                if (row >= 0 && vehiclesTable.getSelectedRow() != row) {
                    vehiclesTable.setRowSelectionInterval(row, row);
                }
            }
        });
        
        refreshTable();
        return vehiclesTable;
    }

    private JPanel createFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Status filter
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setBackground(CARD_BG);
        
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(HEADER_FONT);
        statusLabel.setForeground(TEXT_SECONDARY);
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "Active", "Maintenance", "Inactive"});
        styleFilterCombo(statusFilter);
        
        statusPanel.add(statusLabel);
        statusPanel.add(statusFilter);
        panel.add(statusPanel);
        
        // Type filter
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        typePanel.setBackground(CARD_BG);
        
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(HEADER_FONT);
        typeLabel.setForeground(TEXT_SECONDARY);
        
        typeFilter = new JComboBox<>(new String[]{"All Types", "Truck", "Van", "Car", "Motorcycle"});
        styleFilterCombo(typeFilter);
        
        typePanel.add(typeLabel);
        typePanel.add(typeFilter);
        panel.add(typePanel);
        
        // Clear button
        JButton clearBtn = new JButton("Clear Filters");
        clearBtn.setFont(REGULAR_FONT);
        clearBtn.setForeground(TEXT_SECONDARY);
        clearBtn.setBackground(CARD_BG);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setPreferredSize(new Dimension(100, 32));
        
        clearBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                clearBtn.setBackground(HOVER_COLOR);
                clearBtn.setBorder(BorderFactory.createLineBorder(PRIMARY, 1, true));
            }
            public void mouseExited(MouseEvent e) {
                clearBtn.setBackground(CARD_BG);
                clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            }
        });
        
        clearBtn.addActionListener(e -> {
            clearAllFilters();
        });
        
        panel.add(clearBtn);
        
        return panel;
    }

    private void styleFilterCombo(JComboBox<String> comboBox) {
        comboBox.setFont(REGULAR_FONT);
        comboBox.setPreferredSize(new Dimension(130, 32));
        comboBox.setBackground(CARD_BG);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        comboBox.setFocusable(false);
        
        if (comboBox == statusFilter) {
            comboBox.addActionListener(e -> {
                int selectedIndex = statusFilter.getSelectedIndex();
                if (selectedIndex > 0) {
                    String selectedStatus = (String) statusFilter.getSelectedItem();
                    // Find matching stat card
                    for (int i = 0; i < statCards.length; i++) {
                        // Get the title label from the card (first component in the card)
                        JPanel card = (JPanel) statCards[i];
                        Component[] components = card.getComponents();
                        if (components.length > 0 && components[0] instanceof JLabel) {
                            JLabel titleLabel = (JLabel) components[0];
                            String cardTitle = titleLabel.getText();
                            if (cardTitle.equals(selectedStatus)) {
                                applyStatusFilter(selectedStatus, i, getColorForStatus(selectedStatus));
                                return;
                            }
                        }
                    }
                } else {
                    clearAllFilters();
                }
            });
        } else {
            comboBox.addActionListener(e -> {
                applyFilters();
            });
        }
    }

    private Color getColorForStatus(String status) {
        switch (status) {
            case "Active": return SUCCESS;
            case "Maintenance": return INFO;
            case "Inactive": return WARNING;
            case "Expired Tax": return WARNING;
            default: return PRIMARY;
        }
    }

    private void showAvailableDrivers() {
        String availableDrivers = drivers.stream()
            .filter(d -> "Available".equals(d.status) && !"Unassigned".equals(d.name))
            .map(d -> String.format("• %s (%s) - %s", d.name, d.license, d.phone))
            .collect(Collectors.joining("\n"));
        
        if (availableDrivers.isEmpty()) {
            showNotification("No available drivers", WARNING);
        } else {
            JOptionPane.showMessageDialog(mainPanel, 
                availableDrivers,
                "Available Drivers",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("Add", PRIMARY, PRIMARY_DARK, this::addVehicle),
            new ButtonConfig("Edit", WARNING, WARNING_DARK, this::editVehicle),
            new ButtonConfig("Delete", DANGER, DANGER_DARK, this::deleteVehicle),
            new ButtonConfig("Assign", SUCCESS, SUCCESS_DARK, this::assignDriver),
            new ButtonConfig("Unassign", DANGER, DANGER_DARK, this::unassignDriver),
            new ButtonConfig("Maintenance", INFO, INFO_DARK, this::toggleMaintenance),
            new ButtonConfig("Tax", PURPLE, new Color(89, 52, 154), this::updateTax),
            new ButtonConfig("History", new Color(108, 117, 125), new Color(73, 80, 87), this::viewMaintenanceHistory)
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

    // Status Cell Renderer
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
                
                if (status.equals("Active")) {
                    label.setForeground(SUCCESS.darker());
                    label.setBackground(new Color(232, 245, 233));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(SUCCESS, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else if (status.equals("Maintenance")) {
                    label.setForeground(INFO.darker());
                    label.setBackground(new Color(227, 242, 253));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(INFO, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else {
                    label.setForeground(WARNING.darker());
                    label.setBackground(new Color(255, 243, 224));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(WARNING, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                }
            }
            
            return panel;
        }
    }

    // Driver Cell Renderer
    private class DriverCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setFont(REGULAR_FONT);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            
            if (value != null) {
                String driver = value.toString();
                if (!driver.equals("Unassigned")) {
                    setForeground(PRIMARY);
                    setText("👤 " + driver);
                } else {
                    setForeground(TEXT_MUTED);
                    setFont(getFont().deriveFont(Font.ITALIC));
                    setText("— " + driver + " —");
                }
            }
            
            setOpaque(true);
            setBackground(isSelected ? SELECTION_COLOR : 
                         (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
            
            return this;
        }
    }

    // Date Cell Renderer
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
                    setBackground(isSelected ? SELECTION_COLOR : 
                                 (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
                }
            }
            
            setOpaque(true);
            return this;
        }
    }

    // Modern ScrollBar UI
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final int THUMB_SIZE = 6;
        
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(203, 213, 225);
            trackColor = new Color(241, 245, 249);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, THUMB_SIZE, THUMB_SIZE);
            g2.dispose();
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.dispose();
        }
    }

    // Delete vehicle method
    private void deleteVehicle() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle to delete", DANGER);
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Are you sure you want to delete vehicle " + v.id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            vehicles.remove(row);
            saveData();
            refreshTable();
            showNotification("Vehicle " + v.id + " deleted successfully", SUCCESS);
        }
    }

    private void syncWithMaintenance() {
        if (maintenanceManagement == null) return;
        
        boolean changed = false;
        for (Vehicle v : vehicles) {
            boolean hasActiveMaintenance = maintenanceManagement.isVehicleInMaintenance(v.id);
            
            if (hasActiveMaintenance && !v.status.equals("Maintenance")) {
                v.status = "Maintenance";
                changed = true;
            } else if (!hasActiveMaintenance && v.status.equals("Maintenance")) {
                v.status = "Active";
                changed = true;
            }
        }
        
        if (changed) {
            saveData();
            refreshTable();
        }
    }

    private void viewMaintenanceHistory() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle to view maintenance history", WARNING);
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        if (maintenanceManagement == null) {
            showNotification("Maintenance Management module not available", DANGER);
            return;
        }
        
        List<MaintenanceManagement.MaintenanceRecord> history = 
            maintenanceManagement.getVehicleMaintenanceHistory(v.id);
        
        if (history.isEmpty()) {
            showNotification("No maintenance records found for " + v.id, INFO);
            return;
        }
        
        showMaintenanceHistoryDialog(v, history);
    }
    
    private void showMaintenanceHistoryDialog(Vehicle v, List<MaintenanceManagement.MaintenanceRecord> history) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Maintenance History - " + v.id);
        dialog.setModal(true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        mainPanel.setBackground(CARD_BG);
        
        // Title
        JLabel titleLabel = new JLabel("Maintenance History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        
        JLabel subtitleLabel = new JLabel(v.id + " - " + v.model);
        subtitleLabel.setFont(REGULAR_FONT);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_BG);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Stats summary
        long scheduled = history.stream().filter(r -> r.status.equals("Scheduled")).count();
        long inProgress = history.stream().filter(r -> r.status.equals("In Progress")).count();
        long completed = history.stream().filter(r -> r.status.equals("Completed")).count();
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statsPanel.setBackground(CARD_BG);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        
        statsPanel.add(createStatBadge("Scheduled", String.valueOf(scheduled), WARNING));
        statsPanel.add(createStatBadge("In Progress", String.valueOf(inProgress), INFO));
        statsPanel.add(createStatBadge("Completed", String.valueOf(completed), SUCCESS));
        
        // Table
        String[] columns = {"ID", "Date", "Description", "Status", "Notes"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        for (MaintenanceManagement.MaintenanceRecord record : history) {
            historyModel.addRow(new Object[]{
                record.maintenanceId,
                displayDateFormat.format(record.scheduledDate),
                record.description,
                record.status,
                record.notes
            });
        }
        
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(45);
        historyTable.setFont(REGULAR_FONT);
        historyTable.getTableHeader().setFont(HEADER_FONT);
        historyTable.getTableHeader().setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(statsPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createStatBadge(String label, String value, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setBackground(CARD_BG);
        
        JLabel dot = new JLabel("●");
        dot.setFont(STATUS_FONT);
        dot.setForeground(color);
        
        JLabel text = new JLabel(label + ": " + value);
        text.setFont(REGULAR_FONT);
        text.setForeground(TEXT_PRIMARY);
        
        panel.add(dot);
        panel.add(text);
        
        return panel;
    }

    private void addVehicle() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Vehicle");
        dialog.setModal(true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(CARD_BG);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Truck", "Van", "Car", "Motorcycle"});
        JTextField modelField = createStyledTextField();
        JTextField plateField = createStyledTextField();
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        JSpinner dateSpinner = createStyledDateSpinner(new Date());
        
        styleFormField(typeCombo);
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(fuelCombo);
        styleFormField(dateSpinner);
        
        int row = 0;
        addFormField(formPanel, "Type:", typeCombo, gbc, row++);
        addFormField(formPanel, "Model:", modelField, gbc, row++);
        addFormField(formPanel, "Plate:", plateField, gbc, row++);
        addFormField(formPanel, "Fuel:", fuelCombo, gbc, row++);
        addFormField(formPanel, "Tax Expiry:", dateSpinner, gbc, row++);
        
        // Buttons
        JPanel buttonPanel = createDialogButtonPanel(
            dialog,
            "Add Vehicle",
            SUCCESS,
            SUCCESS_DARK,
            () -> {
                if (validateForm(modelField, plateField)) {
                    String type = (String) typeCombo.getSelectedItem();
                    String id = generateId(type);
                    
                    vehicles.add(new Vehicle(id, modelField.getText().trim(), "Active", null, type,
                        plateField.getText().trim().toUpperCase(), (Date) dateSpinner.getValue(), 
                        (String) fuelCombo.getSelectedItem()));
                    
                    saveData();
                    refreshTable();
                    showNotification("Vehicle added: " + id, SUCCESS);
                    dialog.dispose();
                }
            }
        );
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void editVehicle() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle to edit", WARNING);
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Vehicle - " + v.id);
        dialog.setModal(true);
        dialog.setSize(500, 650);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(CARD_BG);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField modelField = createStyledTextField(v.model);
        JTextField plateField = createStyledTextField(v.numberPlate);
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        fuelCombo.setSelectedItem(v.fuelType);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setSelectedItem(v.status);
        JSpinner dateSpinner = createStyledDateSpinner(v.roadTaxExpiry);
        
        styleFormField(fuelCombo);
        styleFormField(statusCombo);
        
        int rowIndex = 0;
        addFormField(formPanel, "Model:", modelField, gbc, rowIndex++);
        addFormField(formPanel, "Plate:", plateField, gbc, rowIndex++);
        addFormField(formPanel, "Fuel:", fuelCombo, gbc, rowIndex++);
        addFormField(formPanel, "Status:", statusCombo, gbc, rowIndex++);
        addFormField(formPanel, "Tax Expiry:", dateSpinner, gbc, rowIndex++);
        
        // Buttons
        JPanel buttonPanel = createDialogButtonPanel(
            dialog,
            "Save Changes",
            WARNING,
            WARNING_DARK,
            () -> {
                v.model = modelField.getText();
                v.numberPlate = plateField.getText().toUpperCase();
                v.fuelType = (String) fuelCombo.getSelectedItem();
                v.status = (String) statusCombo.getSelectedItem();
                v.roadTaxExpiry = (Date) dateSpinner.getValue();
                
                saveData();
                refreshTable();
                showNotification("Vehicle updated: " + v.id, SUCCESS);
                dialog.dispose();
            }
        );
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void assignDriver() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        if (v.status.equals("Maintenance")) {
            showNotification("Cannot assign driver to vehicle in maintenance", WARNING);
            return;
        }
        
        List<Driver> availableDrivers = drivers.stream()
            .filter(d -> !"Unassigned".equals(d.name) && d.currentVehicle == null)
            .collect(Collectors.toList());
        
        if (availableDrivers.isEmpty()) {
            showNotification("No available drivers", WARNING);
            return;
        }
        
        showAssignDriverDialog(v, availableDrivers);
    }

    private void showAssignDriverDialog(Vehicle v, List<Driver> availableDrivers) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Assign Driver");
        dialog.setModal(true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(CARD_BG);
        
        // Title
        JLabel titleLabel = new JLabel("Assign Driver to " + v.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);
        
        // Driver selection
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel selectLabel = new JLabel("Select Driver:");
        selectLabel.setFont(HEADER_FONT);
        selectLabel.setForeground(TEXT_PRIMARY);
        
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Choose a driver...");
        availableDrivers.forEach(d -> model.addElement(d.name + " (" + d.license + ")"));
        
        JComboBox<String> driverCombo = new JComboBox<>(model);
        driverCombo.setFont(REGULAR_FONT);
        driverCombo.setPreferredSize(new Dimension(300, 35));
        driverCombo.setBackground(CARD_BG);
        driverCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        gbc.gridx = 0; gbc.gridy = 0;
        selectionPanel.add(selectLabel, gbc);
        
        gbc.gridx = 1;
        selectionPanel.add(driverCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = createDialogButtonPanel(
            dialog,
            "Assign",
            SUCCESS,
            SUCCESS_DARK,
            () -> {
                int selectedIndex = driverCombo.getSelectedIndex();
                if (selectedIndex > 0) {
                    Driver selectedDriver = availableDrivers.get(selectedIndex - 1);
                    
                    if (v.driverName != null) {
                        findDriver(v.driverName).ifPresent(d -> {
                            d.currentVehicle = null;
                            d.status = "Available";
                        });
                    }
                    
                    selectedDriver.currentVehicle = v.id;
                    selectedDriver.status = "Active";
                    v.driverName = selectedDriver.name;
                    v.status = "Active";
                    
                    saveData();
                    refreshTable();
                    showNotification(selectedDriver.name + " assigned to " + v.id, SUCCESS);
                    dialog.dispose();
                } else {
                    showNotification("Please select a driver", WARNING);
                }
            }
        );
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(selectionPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void unassignDriver() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        if (v.driverName != null) {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "Are you sure you want to unassign " + v.driverName + " from " + v.id + "?",
                "Confirm Unassign",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                String driverName = v.driverName;
                findDriver(v.driverName).ifPresent(d -> {
                    d.currentVehicle = null;
                    d.status = "Available";
                });
                v.driverName = null;
                saveData();
                refreshTable();
                showNotification(driverName + " unassigned from " + v.id, INFO);
            }
        } else {
            showNotification("No driver assigned to this vehicle", WARNING);
        }
    }

    private void toggleMaintenance() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        
        if (v.status.equals("Maintenance")) {
            completeMaintenance(v);
        } else {
            startMaintenance(v);
        }
    }

    private void startMaintenance(Vehicle v) {
        if (v.driverName != null) {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                v.driverName + " is assigned to this vehicle. Moving to maintenance will unassign them.\nContinue?",
                "Start Maintenance",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                String driverName = v.driverName;
                findDriver(v.driverName).ifPresent(d -> {
                    d.currentVehicle = null;
                    d.status = "Available";
                });
                v.driverName = null;
                
                createMaintenanceRecord(v);
                v.status = "Maintenance";
                showNotification(driverName + " has been unassigned", INFO);
            } else {
                return;
            }
        } else {
            createMaintenanceRecord(v);
            v.status = "Maintenance";
        }
        
        saveData();
        refreshTable();
    }

    private void createMaintenanceRecord(Vehicle v) {
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
    }

    private void completeMaintenance(Vehicle v) {
        v.status = "Active";
        if (maintenanceManagement != null) {
            maintenanceManagement.completeMaintenanceForVehicle(v.id);
        }
        showNotification(v.id + " is now active", SUCCESS);
        saveData();
        refreshTable();
    }

    private void updateTax() {
        int row = getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        Vehicle v = vehicles.get(row);
        showUpdateTaxDialog(v);
    }

    private void showUpdateTaxDialog(Vehicle v) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Update Road Tax");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(CARD_BG);
        
        // Title
        JLabel titleLabel = new JLabel("Update Tax for " + v.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);
        
        // Current expiry info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(248, 250, 252));
        infoPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        JLabel currentLabel = new JLabel("Current Expiry:");
        currentLabel.setFont(HEADER_FONT);
        currentLabel.setForeground(TEXT_SECONDARY);
        
        boolean isExpired = v.roadTaxExpiry.before(new Date());
        JLabel dateLabel = new JLabel(displayDateFormat.format(v.roadTaxExpiry));
        dateLabel.setFont(STATUS_FONT);
        dateLabel.setForeground(isExpired ? DANGER : SUCCESS);
        
        infoPanel.add(currentLabel, BorderLayout.WEST);
        infoPanel.add(dateLabel, BorderLayout.EAST);
        
        // Date selection
        JPanel selectionPanel = new JPanel(new BorderLayout(10, 0));
        selectionPanel.setBackground(CARD_BG);
        
        JLabel newLabel = new JLabel("New Date:");
        newLabel.setFont(HEADER_FONT);
        newLabel.setForeground(TEXT_PRIMARY);
        
        JSpinner dateSpinner = createStyledDateSpinner(v.roadTaxExpiry);
        
        selectionPanel.add(newLabel, BorderLayout.WEST);
        selectionPanel.add(dateSpinner, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createDialogButtonPanel(
            dialog,
            "Update",
            INFO,
            INFO_DARK,
            () -> {
                v.roadTaxExpiry = (Date) dateSpinner.getValue();
                saveData();
                refreshTable();
                showNotification("Road tax updated for " + v.id, SUCCESS);
                dialog.dispose();
            }
        );
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(selectionPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Helper methods for dialogs
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

    private JSpinner createStyledDateSpinner(Date initialDate) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setValue(initialDate);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setPreferredSize(new Dimension(300, 35));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DateEditor) {
            JFormattedTextField ftf = ((JSpinner.DateEditor) editor).getTextField();
            ftf.setFont(REGULAR_FONT);
            ftf.setBackground(CARD_BG);
            ftf.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }
        
        return spinner;
    }

    private void styleFormField(JComponent field) {
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(300, 35));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        } else if (field instanceof JSpinner) {
            ((JSpinner) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        }
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(HEADER_FONT);
        jLabel.setForeground(TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
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
                saveBtn.setFont(BUTTON_HOVER_FONT);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(saveColor);
                saveBtn.setFont(BUTTON_FONT);
            }
        });
        
        saveBtn.addActionListener(e -> saveAction.run());
        
        panel.add(cancelBtn);
        panel.add(saveBtn);
        
        return panel;
    }

    private boolean validateForm(JTextField modelField, JTextField plateField) {
        if (modelField.getText().trim().isEmpty()) {
            showNotification("Please enter vehicle model", WARNING);
            modelField.requestFocus();
            return false;
        }
        if (plateField.getText().trim().isEmpty()) {
            showNotification("Please enter number plate", WARNING);
            plateField.requestFocus();
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

    // ========== METHODS REQUIRED BY MAINTENANCE MANAGEMENT ==========
    
    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicles.stream()
            .filter(v -> v.status.equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

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

    // ========== END OF METHODS REQUIRED BY MAINTENANCE MANAGEMENT ==========

    private Optional<Driver> findDriver(String name) {
        return drivers.stream().filter(d -> d.name.equals(name)).findFirst();
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
        });
    }

    private void showVehicleDetails(Vehicle v) {
        boolean taxExpired = v.roadTaxExpiry.before(new Date());
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Vehicle Details");
        dialog.setModal(true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(CARD_BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(CARD_BG);
        
        String icon = v.type.equals("Truck") ? "🚛" : v.type.equals("Van") ? "🚐" : v.type.equals("Car") ? "🚗" : "🏍️";
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(CARD_BG);
        
        JLabel idLabel = new JLabel(v.id);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        idLabel.setForeground(PRIMARY);
        
        JLabel modelLabel = new JLabel(v.model);
        modelLabel.setFont(REGULAR_FONT);
        modelLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(idLabel);
        titlePanel.add(modelLabel);
        
        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Details
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(new Color(248, 250, 252));
        detailsPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);
        
        addDetailRow(detailsPanel, "Type:", v.type, gbc, 0);
        addDetailRow(detailsPanel, "Plate:", v.numberPlate, gbc, 1);
        addDetailRow(detailsPanel, "Fuel:", v.fuelType, gbc, 2);
        addDetailRow(detailsPanel, "Status:", v.status, gbc, 3);
        addDetailRow(detailsPanel, "Tax Expiry:", displayDateFormat.format(v.roadTaxExpiry) + 
                     (taxExpired ? " (Overdue)" : ""), gbc, 4);
        addDetailRow(detailsPanel, "Driver:", v.driverName == null ? "Unassigned" : v.driverName, gbc, 5);
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(closeBtn);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(HEADER_FONT);
        labelComp.setForeground(TEXT_SECONDARY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(REGULAR_FONT);
        valueComp.setForeground(TEXT_PRIMARY);
        panel.add(valueComp, gbc);
    }

    private void createSampleData() {
        try {
            Calendar cal = Calendar.getInstance();
            
            Date validDate1 = new Date(System.currentTimeMillis() + 86400000L * 365);
            Date validDate2 = new Date(System.currentTimeMillis() + 86400000L * 180);
            Date expiredDate1 = new Date(System.currentTimeMillis() - 86400000L * 30);
            Date expiredDate2 = new Date(System.currentTimeMillis() - 86400000L * 60);
            Date futureDate = new Date(System.currentTimeMillis() + 86400000L * 90);
            
            vehicles.add(new Vehicle("TRK001", "Freightliner Cascadia", "Active", "John Smith", 
                "Truck", "ABC-1234", validDate1, "Diesel"));
            
            vehicles.add(new Vehicle("VAN001", "Ford Transit", "Maintenance", null, 
                "Van", "DEF-5678", expiredDate1, "Gasoline"));
            
            vehicles.add(new Vehicle("TRK002", "Peterbilt 579", "Maintenance", null, 
                "Truck", "GHI-9012", expiredDate2, "Diesel"));
            
            vehicles.add(new Vehicle("VAN002", "Mercedes Sprinter", "Active", null, 
                "Van", "JKL-3456", validDate2, "Diesel"));
            
            vehicles.add(new Vehicle("TRK003", "International LT", "Active", null, 
                "Truck", "MNO-7890", futureDate, "Diesel"));
            
            vehicles.add(new Vehicle("CAR001", "Toyota Camry", "Active", "Mike Johnson", 
                "Car", "PQR-2345", futureDate, "Gasoline"));
            
            vehicles.add(new Vehicle("MTC001", "Harley Davidson", "Inactive", null, 
                "Motorcycle", "STU-6789", expiredDate1, "Gasoline"));
            
            vehicles.add(new Vehicle("CAR002", "Honda Civic", "Active", "Sarah Wilson", 
                "Car", "VWX-3456", futureDate, "Gasoline"));
            
            vehicles.add(new Vehicle("VAN003", "RAM ProMaster", "Active", null, 
                "Van", "YZA-7890", validDate1, "Diesel"));
            
            drivers.add(new Driver("John Smith", "DL12345", "(555) 123-4567", "john.smith@email.com", 
                "Active", new Date(), "TRK001"));
            drivers.add(new Driver("Mike Johnson", "DL12346", "(555) 234-5678", "mike.j@email.com", 
                "Active", new Date(), "CAR001"));
            drivers.add(new Driver("Sarah Wilson", "DL12347", "(555) 345-6789", "sarah.w@email.com", 
                "Active", new Date(), "CAR002"));
            drivers.add(new Driver("Robert Brown", "DL12348", "(555) 456-7890", "robert.b@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("Emily Davis", "DL12349", "(555) 567-8901", "emily.d@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("David Lee", "DL12350", "(555) 678-9012", "david.l@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("Unassigned", "", "", "", "Available", new Date(), null));
            
            typeCounters.put("Truck", 3);
            typeCounters.put("Van", 3);
            typeCounters.put("Car", 2);
            typeCounters.put("Motorcycle", 1);
            
            saveData();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void showNotification(String message, Color color) {
        JWindow notification = new JWindow();
        notification.setAlwaysOnTop(true);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        messageLabel.setForeground(Color.WHITE);
        
        panel.add(messageLabel, BorderLayout.CENTER);
        
        notification.getContentPane().add(panel);
        notification.pack();
        
        Point p = mainPanel.getLocationOnScreen();
        notification.setLocation(
            p.x + mainPanel.getWidth() - notification.getWidth() - 24,
            p.y + mainPanel.getHeight() - notification.getHeight() - 24
        );
        
        notification.setVisible(true);
        
        Timer timer = new Timer(3000, e -> notification.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void showError(String msg) { 
        JOptionPane.showMessageDialog(mainPanel, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ========== PUBLIC METHODS ==========
    
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
    
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    // Data classes
    public static class Vehicle {
        public String id, model, status, driverName, type, numberPlate, fuelType;
        public Date roadTaxExpiry;
        
        public Vehicle(String id, String model, String status, String driver, String type,
                String plate, Date tax, String fuel) {
            this.id = id; 
            this.model = model; 
            this.status = status; 
            this.driverName = driver;
            this.type = type; 
            this.numberPlate = plate; 
            this.roadTaxExpiry = tax; 
            this.fuelType = fuel;
        }
    }

    private static class Driver {
        String name, license, phone, email, status, currentVehicle;
        Date joinedDate;
        
        Driver(String name, String license, String phone, String email, 
               String status, Date joined, String vehicle) {
            this.name = name; 
            this.license = license; 
            this.phone = phone; 
            this.email = email;
            this.status = status; 
            this.joinedDate = joined; 
            this.currentVehicle = vehicle;
        }
    }
}