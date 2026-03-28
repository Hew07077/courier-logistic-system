package admin.management;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
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
    private List<DriverReport> driverReports = new ArrayList<>();
    
    private MaintenanceManagement maintenanceManagement;
    private DriverManagement driverManagement;
    private OrderManagement orderManagement;
    
    private static final String VEHICLES_FILE = "vehicles_data.txt";
    private static final String DRIVERS_FILE = "drivers_data.txt";
    private static final String COUNTERS_FILE = "counters_data.txt";
    private static final String REPORTS_FILE = "driver_reports.txt";
    
    private JPanel statsPanel;
    private JLabel[] statValues = new JLabel[6];
    private JPanel[] statCards = new JPanel[6];
    
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;
    
    // Modern color scheme
    private static final Color PRIMARY = new Color(255, 140, 0);
    private static final Color PRIMARY_DARK = new Color(235, 120, 0);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color SUCCESS_DARK = new Color(30, 126, 52);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color WARNING_DARK = new Color(204, 154, 6);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color DANGER_DARK = new Color(176, 42, 55);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color INFO_DARK = new Color(17, 122, 139);
    private static final Color PURPLE = new Color(111, 66, 193);
    private static final Color PURPLE_DARK = new Color(88, 53, 154);
    private static final Color BG_COLOR = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color TEXT_MUTED = new Color(134, 142, 150);
    private static final Color HOVER_COLOR = new Color(255, 245, 235);
    private static final Color SELECTION_COLOR = new Color(255, 245, 235);
    private static final Color ACTIVE_FILTER_BORDER = PRIMARY;
    
    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 12);
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    
    public interface DriverProfileListener {
        void onDriverProfileClicked(String driverName);
    }
    private DriverProfileListener driverProfileListener;

    public VehicleManagement() {
        this(null, null, null);
    }
    
    public VehicleManagement(MaintenanceManagement maintenanceMgmt) {
        this(maintenanceMgmt, null, null);
    }
    
    public VehicleManagement(MaintenanceManagement maintenanceMgmt, 
                            DriverManagement driverMgmt,
                            OrderManagement orderMgmt) {
        this.maintenanceManagement = maintenanceMgmt;
        this.driverManagement = driverMgmt;
        this.orderManagement = orderMgmt;
        loadData();
        loadDriverReports();
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
    
    public void setDriverManagement(DriverManagement driverMgmt) {
        this.driverManagement = driverMgmt;
    }
    
    public void setOrderManagement(OrderManagement orderMgmt) {
        this.orderManagement = orderMgmt;
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

    private void loadDriverReports() {
        driverReports.clear();
        File file = new File(REPORTS_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().filter(l -> !l.trim().isEmpty() && !l.startsWith("//"))
                  .forEach(this::parseDriverReport);
        } catch (IOException e) {
            showError("Error loading driver reports");
        }
    }

    private void saveDriverReports() {
        saveToFile(REPORTS_FILE, driverReports, r -> String.format("%s|%s|%s|%s|%s|%s|%s|%s",
            r.reportId, r.vehicleId, r.driverName, r.reportDate.getTime(), r.description,
            r.severity, r.status, r.adminNotes == null ? "" : r.adminNotes));
    }

    private void parseDriverReport(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length >= 7) {
                DriverReport report = new DriverReport(
                    p[0].trim(), p[1].trim(), p[2].trim(),
                    new Date(Long.parseLong(p[3].trim())), p[4].trim(),
                    p[5].trim(), p[6].trim(),
                    p.length > 7 ? p[7].trim() : ""
                );
                driverReports.add(report);
            }
        } catch (Exception e) {
            System.err.println("Error parsing driver report: " + line);
        }
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
                    new Date(Long.parseLong(p[6].trim())), p[7].trim()));
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
                    p[4].trim(), new Date(Long.parseLong(p[5].trim())), 
                    p[6].trim().isEmpty() ? null : p[6].trim()));
            }
        } catch (Exception e) { 
            System.err.println("Error parsing driver: " + line); 
        }
    }

    private void saveData() {
        saveToFile(VEHICLES_FILE, vehicles, v -> String.format("%s|%s|%s|%s|%s|%s|%s|%s",
            v.id, v.model, v.status, v.driverName == null ? "" : v.driverName,
            v.type, v.numberPlate, v.roadTaxExpiry.getTime(), v.fuelType));
        
        saveToFile(DRIVERS_FILE, drivers, d -> String.format("%s|%s|%s|%s|%s|%s|%s",
            d.name, d.license, d.phone, d.email, d.status,
            d.joinedDate.getTime(), d.currentVehicle == null ? "" : d.currentVehicle));
        
        saveToFile(COUNTERS_FILE, typeCounters.entrySet(), 
            e -> e.getKey() + "=" + e.getValue());
        
        saveDriverReports();
    }

    private <T> void saveToFile(String filename, Collection<T> data, DataFormatter<T> formatter) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
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

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Vehicle Management");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Manage fleet vehicles, drivers, and maintenance reports");
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        // REMOVED: Refresh button
        
        return panel;
    }

    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 6, 15, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Total Fleet", "Active", "Maintenance", "Tax Expired", "Avail. Drivers", "Driver Reports"};
        String[] descriptions = {"All vehicles", "Currently operating", "In service", "Overdue renewal", "Ready to assign", "Total reports"};
        Color[] colors = {PRIMARY, SUCCESS, INFO, WARNING, PURPLE, new Color(255, 87, 34)};
        Color[] bgColors = {
            new Color(255, 245, 235),
            new Color(232, 245, 233),
            new Color(227, 242, 253),
            new Color(255, 243, 224),
            new Color(243, 232, 255),
            new Color(255, 243, 224)
        };
        
        statValues = new JLabel[6];
        statCards = new JPanel[6];
        
        for (int i = 0; i < 6; i++) {
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
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STATS_FONT);
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(SMALL_FONT);
        descLabel.setForeground(TEXT_MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);
        
        statValues[index] = valueLabel;
        
        if (index == 1 || index == 2 || index == 3) {
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
                    if (index == 3) {
                        applyTaxExpiredFilter(cardIndex, color);
                    } else {
                        applyStatusFilter(filterStatus, cardIndex, color);
                    }
                }
            });
        } else if (index == 4) {
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
        } else if (index == 5) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(HOVER_COLOR);
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(CARD_BG);
                }
                public void mouseClicked(MouseEvent e) {
                    showDriverReportsDialog();
                }
            });
        } else if (index == 0) {
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

    private void applyStatusFilter(String status, int cardIndex, Color color) {
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentStatusFilter = null;
            currentFilterIndex = -1;
            rowSorter.setRowFilter(null);
        } else {
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            rowSorter.setRowFilter(RowFilter.regexFilter("^" + status + "$", 6));
        }
    }

    private void applyTaxExpiredFilter(int cardIndex, Color color) {
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentStatusFilter = null;
            currentFilterIndex = -1;
            rowSorter.setRowFilter(null);
        } else {
            currentStatusFilter = "Tax Expired";
            currentFilterIndex = cardIndex;
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    Date taxDate = (Date) entry.getValue(4);
                    return taxDate != null && taxDate.before(new Date());
                }
            });
        }
    }

    private void clearAllFilters() {
        resetCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        rowSorter.setRowFilter(null);
    }

    private void showAvailableDrivers() {
        List<Driver> available = drivers.stream()
            .filter(d -> "Available".equals(d.status) && !"Unassigned".equals(d.name))
            .collect(Collectors.toList());
        
        if (available.isEmpty()) {
            showNotification("No available drivers", WARNING);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Available Drivers", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel title = new JLabel("Available Drivers");
        title.setFont(HEADER_FONT);
        title.setForeground(PURPLE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Driver d : available) {
            listModel.addElement(String.format("👤 %s - %s | %s", d.name, d.license, d.phone));
        }
        
        JList<String> driverList = new JList<>(listModel);
        driverList.setFont(REGULAR_FONT);
        driverList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JScrollPane scrollPane = new JScrollPane(driverList);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(closeBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_COLOR);
        
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
        scrollPane.setPreferredSize(new Dimension(1000, 450));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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
                
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        comp.setBackground(new Color(252, 252, 253));
                    } else {
                        comp.setBackground(CARD_BG);
                    }
                    
                    int modelRow = convertRowIndexToModel(row);
                    if (modelRow >= 0 && modelRow < vehicles.size()) {
                        Vehicle v = vehicles.get(modelRow);
                        boolean hasPendingReports = driverReports.stream()
                            .anyMatch(r -> r.vehicleId.equals(v.id) && "Pending".equals(r.status));
                        
                        if (hasPendingReports) {
                            comp.setBackground(new Color(255, 245, 235));
                        }
                    }
                } else {
                    comp.setBackground(SELECTION_COLOR);
                }
                
                return comp;
            }
        };
        
        vehiclesTable.setRowHeight(45);
        vehiclesTable.setFont(REGULAR_FONT);
        vehiclesTable.setSelectionBackground(SELECTION_COLOR);
        vehiclesTable.setSelectionForeground(TEXT_PRIMARY);
        vehiclesTable.setShowGrid(true);
        vehiclesTable.setGridColor(BORDER_COLOR);
        vehiclesTable.setIntercellSpacing(new Dimension(10, 5));
        vehiclesTable.setFillsViewportHeight(true);
        vehiclesTable.setAutoCreateRowSorter(true);
        
        JTableHeader header = vehiclesTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        rowSorter = new TableRowSorter<>(tableModel);
        vehiclesTable.setRowSorter(rowSorter);
        
        vehiclesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        vehiclesTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        vehiclesTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        vehiclesTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        vehiclesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        vehiclesTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        vehiclesTable.getColumnModel().getColumn(7).setPreferredWidth(180);
        
        vehiclesTable.getColumnModel().getColumn(4).setCellRenderer(new DateCellRenderer());
        vehiclesTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        vehiclesTable.getColumnModel().getColumn(7).setCellRenderer(new DriverCellRenderer());
        
        vehiclesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && vehiclesTable.getSelectedRow() != -1) {
                    int row = vehiclesTable.convertRowIndexToModel(vehiclesTable.getSelectedRow());
                    if (row >= 0 && row < vehicles.size()) {
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
            }
        });
        
        refreshTable();
        return vehiclesTable;
    }

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
            
            panel.setBackground(isSelected ? SELECTION_COLOR : 
                               (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
            
            if (value != null) {
                String status = value.toString();
                label.setText(status);
                label.setOpaque(true);
                
                switch (status) {
                    case "Active":
                        label.setForeground(SUCCESS.darker());
                        label.setBackground(new Color(232, 245, 233));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(SUCCESS, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Maintenance":
                        label.setForeground(INFO.darker());
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(INFO, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    default:
                        label.setForeground(WARNING.darker());
                        label.setBackground(new Color(255, 243, 224));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(WARNING, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                }
            }
            
            return panel;
        }
    }

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
                    setText("⚠️ " + text + " (Overdue)");
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (recordDate.equals(today) && !isSelected) {
                    setForeground(WARNING);
                    setText("🔔 " + text + " (Today)");
                } else {
                    setForeground(TEXT_PRIMARY);
                    setText("📅 " + text);
                }
            }
            
            setOpaque(true);
            setBackground(isSelected ? SELECTION_COLOR : 
                         (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
            
            return this;
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("Add Vehicle", SUCCESS, SUCCESS_DARK, this::addVehicle),
            new ButtonConfig("Edit", WARNING, WARNING_DARK, this::editVehicle),
            new ButtonConfig("Delete", DANGER, DANGER_DARK, this::deleteVehicle),
            new ButtonConfig("Driver", PURPLE, PURPLE_DARK, this::showDriverActions),
            new ButtonConfig("View Reports", PRIMARY, PRIMARY_DARK, this::showDriverReportsDialog),
            new ButtonConfig("Maintenance", INFO, INFO_DARK, this::toggleMaintenance),
            new ButtonConfig("Tax", new Color(108, 117, 125), new Color(73, 80, 87), this::updateTax)
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
        btn.setPreferredSize(new Dimension(110, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(config.hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(config.bgColor);
            }
        });
        
        btn.addActionListener(e -> config.action.run());
        
        return btn;
    }

    private void showDriverActions() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle first", WARNING);
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        if (v.driverName != null) {
            JLabel currentDriver = new JLabel("  Current: " + v.driverName);
            currentDriver.setFont(SMALL_FONT);
            currentDriver.setForeground(TEXT_SECONDARY);
            currentDriver.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));
            menu.add(currentDriver);
            menu.addSeparator();
        }
        
        JMenuItem assignItem = new JMenuItem("Assign Driver");
        assignItem.setFont(REGULAR_FONT);
        assignItem.setForeground(SUCCESS);
        assignItem.addActionListener(e -> assignDriver());
        menu.add(assignItem);
        
        if (v.driverName != null) {
            JMenuItem unassignItem = new JMenuItem("Unassign Driver");
            unassignItem.setFont(REGULAR_FONT);
            unassignItem.setForeground(DANGER);
            unassignItem.addActionListener(e -> unassignDriver());
            menu.add(unassignItem);
        }
        
        JMenuItem viewReportsItem = new JMenuItem("View Driver Reports");
        viewReportsItem.setFont(REGULAR_FONT);
        viewReportsItem.setForeground(PRIMARY);
        viewReportsItem.addActionListener(e -> showVehicleReports(v));
        menu.add(viewReportsItem);
        
        menu.show(vehiclesTable, vehiclesTable.getWidth() - 200, 0);
    }

    private void addVehicle() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Add New Vehicle", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Add New Vehicle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Truck", "Van", "Car", "Motorcycle"});
        JTextField modelField = new JTextField();
        JTextField plateField = new JTextField();
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Gasoline", "Electric", "Hybrid"});
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(new Date());
        
        styleFormField(typeCombo);
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(fuelCombo);
        styleFormField(dateSpinner);
        
        int row = 0;
        addFormRow(formPanel, "Type:", typeCombo, gbc, row++);
        addFormRow(formPanel, "Model:", modelField, gbc, row++);
        addFormRow(formPanel, "Plate:", plateField, gbc, row++);
        addFormRow(formPanel, "Fuel:", fuelCombo, gbc, row++);
        addFormRow(formPanel, "Tax Expiry:", dateSpinner, gbc, row++);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Add Vehicle");
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(SUCCESS);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(120, 35));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(SUCCESS_DARK);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(SUCCESS);
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        saveBtn.addActionListener(e -> {
            if (modelField.getText().trim().isEmpty() || plateField.getText().trim().isEmpty()) {
                showNotification("Model and plate are required", WARNING);
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            String id = generateId(type);
            
            vehicles.add(new Vehicle(id, modelField.getText().trim(), "Active", null, type,
                plateField.getText().trim().toUpperCase(), (Date) dateSpinner.getValue(), 
                (String) fuelCombo.getSelectedItem()));
            
            saveData();
            refreshTable();
            showNotification("✓ Vehicle added: " + id, SUCCESS);
            dialog.dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void editVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING);
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Edit Vehicle - " + v.id, true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Edit Vehicle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField modelField = new JTextField(v.model);
        JTextField plateField = new JTextField(v.numberPlate);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setSelectedItem(v.status);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(v.roadTaxExpiry);
        
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(statusCombo);
        styleFormField(dateSpinner);
        
        int rowIdx = 0;
        addFormRow(formPanel, "Model:", modelField, gbc, rowIdx++);
        addFormRow(formPanel, "Plate:", plateField, gbc, rowIdx++);
        addFormRow(formPanel, "Status:", statusCombo, gbc, rowIdx++);
        addFormRow(formPanel, "Tax Expiry:", dateSpinner, gbc, rowIdx++);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(WARNING);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(120, 35));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(WARNING_DARK);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(WARNING);
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        saveBtn.addActionListener(e -> {
            v.model = modelField.getText();
            v.numberPlate = plateField.getText().toUpperCase();
            v.status = (String) statusCombo.getSelectedItem();
            v.roadTaxExpiry = (Date) dateSpinner.getValue();
            
            saveData();
            refreshTable();
            showNotification("✓ Vehicle updated", SUCCESS);
            dialog.dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", DANGER);
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        boolean hasReports = driverReports.stream()
            .anyMatch(r -> r.vehicleId.equals(v.id) && "Pending".equals(r.status));
        
        if (hasReports) {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "This vehicle has pending reports. Delete anyway?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Delete " + v.id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            vehicles.remove(modelRow);
            saveData();
            refreshTable();
            showNotification("✓ Vehicle deleted", SUCCESS);
        }
    }

    private void assignDriver() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        if (v.status.equals("Maintenance")) {
            showNotification("Cannot assign to vehicle in maintenance", WARNING);
            return;
        }
        
        List<Driver> availableDrivers = drivers.stream()
            .filter(d -> "Available".equals(d.status) && !"Unassigned".equals(d.name))
            .collect(Collectors.toList());
        
        if (availableDrivers.isEmpty()) {
            showNotification("No available drivers", WARNING);
            return;
        }
        
        String[] driverNames = availableDrivers.stream()
            .map(d -> d.name + " (" + d.license + ")")
            .toArray(String[]::new);
        
        String selected = (String) JOptionPane.showInputDialog(mainPanel,
            "Select a driver for " + v.id + ":",
            "Assign Driver",
            JOptionPane.QUESTION_MESSAGE,
            null,
            driverNames,
            driverNames[0]);
        
        if (selected != null) {
            int index = Arrays.asList(driverNames).indexOf(selected);
            Driver selectedDriver = availableDrivers.get(index);
            
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
            showNotification("✓ " + selectedDriver.name + " assigned", SUCCESS);
            
            // Notify DriverManagement if available
            if (driverManagement != null) {
                driverManagement.refreshData();
            }
        }
    }

    private void unassignDriver() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        if (v.driverName == null) {
            showNotification("No driver assigned", WARNING);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Unassign " + v.driverName + " from " + v.id + "?",
            "Confirm Unassign",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            findDriver(v.driverName).ifPresent(d -> {
                d.currentVehicle = null;
                d.status = "Available";
            });
            v.driverName = null;
            saveData();
            refreshTable();
            showNotification("✓ Driver unassigned", INFO);
            
            // Notify DriverManagement if available
            if (driverManagement != null) {
                driverManagement.refreshData();
            }
        }
    }

    private void toggleMaintenance() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        if (v.status.equals("Maintenance")) {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "Complete maintenance for " + v.id + "?",
                "Complete Maintenance",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                v.status = "Active";
                if (maintenanceManagement != null) {
                    maintenanceManagement.completeMaintenanceForVehicle(v.id);
                }
                showNotification("✓ Maintenance completed", SUCCESS);
            }
        } else {
            String message = "Start maintenance for " + v.id + "?";
            if (v.driverName != null) {
                message = v.driverName + " will be unassigned.\n\nContinue?";
            }
            
            int confirm = JOptionPane.showConfirmDialog(mainPanel, message,
                "Start Maintenance",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (v.driverName != null) {
                    findDriver(v.driverName).ifPresent(d -> {
                        d.currentVehicle = null;
                        d.status = "Available";
                    });
                    v.driverName = null;
                }
                
                if (maintenanceManagement != null) {
                    String desc = JOptionPane.showInputDialog(mainPanel, 
                        "Maintenance description:", "Routine maintenance");
                    maintenanceManagement.addVehicleToMaintenance(v.id, v.model, desc);
                }
                
                v.status = "Maintenance";
                showNotification("✓ Maintenance started", INFO);
            }
        }
        
        saveData();
        refreshTable();
    }

    private void updateTax() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JLabel currentLabel = new JLabel("Current: " + displayDateFormat.format(v.roadTaxExpiry));
        currentLabel.setFont(REGULAR_FONT);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setValue(v.roadTaxExpiry);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(currentLabel, gbc);
        gbc.gridy = 1;
        panel.add(dateSpinner, gbc);
        
        int result = JOptionPane.showConfirmDialog(mainPanel, panel,
            "Update Road Tax", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            v.roadTaxExpiry = (Date) dateSpinner.getValue();
            saveData();
            refreshTable();
            showNotification("✓ Tax updated", SUCCESS);
        }
    }

    /**
     * Show dialog with all driver reports - ADMIN CAN ONLY VIEW, NOT CREATE
     */
    private void showDriverReportsDialog() {
        if (driverReports.isEmpty()) {
            showNotification("No driver reports available", INFO);
            return;
        }
        
        // Separate pending and other reports
        List<DriverReport> pendingReports = driverReports.stream()
            .filter(r -> "Pending".equals(r.status))
            .collect(Collectors.toList());
        
        List<DriverReport> otherReports = driverReports.stream()
            .filter(r -> !"Pending".equals(r.status))
            .collect(Collectors.toList());
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Driver Reports Management", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("Driver Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY);
        
        JLabel subtitleLabel = new JLabel("View and manage driver-submitted reports");
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create tabbed pane for pending and history
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(CARD_BG);
        
        // Pending Reports Tab
        if (!pendingReports.isEmpty()) {
            JPanel pendingPanel = createReportsListPanel(pendingReports, true);
            tabbedPane.addTab("Pending (" + pendingReports.size() + ")", pendingPanel);
        }
        
        // All Reports/History Tab
        if (!otherReports.isEmpty() || pendingReports.isEmpty()) {
            JPanel allPanel = createReportsListPanel(driverReports, false);
            tabbedPane.addTab("All Reports (" + driverReports.size() + ")", allPanel);
        }
        
        // Button Panel - REMOVED refresh button, only close button remains
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Create a panel with a list of reports and action buttons
     */
    private JPanel createReportsListPanel(List<DriverReport> reports, boolean showPendingActions) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create list model
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (DriverReport r : reports) {
            String severityIcon = r.severity.equals("Critical") ? "🔴" :
                                 r.severity.equals("High") ? "🟠" :
                                 r.severity.equals("Medium") ? "🟡" : "🔵";
            
            String statusIcon = r.status.equals("Pending") ? "⏳" :
                               r.status.equals("Scheduled") ? "📅" :
                               r.status.equals("Resolved") ? "✅" : "⏰";
            
            String reportLine = String.format("%s %s | %s | %s | %s: %s", 
                severityIcon, statusIcon,
                r.reportId, 
                r.vehicleId, 
                r.driverName,
                truncateText(r.description, 50));
            
            listModel.addElement(reportLine);
        }
        
        JList<String> reportsList = new JList<>(listModel);
        reportsList.setFont(REGULAR_FONT);
        reportsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        reportsList.setFixedCellHeight(40);
        
        // Add tooltip for full text
        reportsList.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int index = reportsList.locationToIndex(e.getPoint());
                if (index > -1) {
                    reportsList.setToolTipText(reports.get(index).description);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(reportsList);
        scrollPane.setPreferredSize(new Dimension(650, 350));
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(CARD_BG);
        
        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(BUTTON_FONT);
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBackground(INFO);
        viewBtn.setBorderPainted(false);
        viewBtn.setPreferredSize(new Dimension(120, 35));
        viewBtn.addActionListener(e -> {
            int index = reportsList.getSelectedIndex();
            if (index >= 0) {
                showReportDetails(reports.get(index));
            } else {
                showNotification("Please select a report", WARNING);
            }
        });
        
        actionPanel.add(viewBtn);
        
        // Add pending-specific actions
        if (showPendingActions) {
            JButton scheduleBtn = new JButton("Schedule Maintenance");
            scheduleBtn.setFont(BUTTON_FONT);
            scheduleBtn.setForeground(Color.WHITE);
            scheduleBtn.setBackground(SUCCESS);
            scheduleBtn.setBorderPainted(false);
            scheduleBtn.setPreferredSize(new Dimension(160, 35));
            scheduleBtn.addActionListener(e -> {
                int index = reportsList.getSelectedIndex();
                if (index >= 0) {
                    showScheduleMaintenanceDialog(reports.get(index));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            JButton postponeBtn = new JButton("Postpone");
            postponeBtn.setFont(BUTTON_FONT);
            postponeBtn.setForeground(Color.WHITE);
            postponeBtn.setBackground(WARNING);
            postponeBtn.setBorderPainted(false);
            postponeBtn.setPreferredSize(new Dimension(100, 35));
            postponeBtn.addActionListener(e -> {
                int index = reportsList.getSelectedIndex();
                if (index >= 0) {
                    postponeReport(reports.get(index));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(BUTTON_FONT);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBackground(DANGER);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setPreferredSize(new Dimension(100, 35));
            deleteBtn.addActionListener(e -> {
                int index = reportsList.getSelectedIndex();
                if (index >= 0) {
                    deleteReport(reports.get(index));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            actionPanel.add(scheduleBtn);
            actionPanel.add(postponeBtn);
            actionPanel.add(deleteBtn);
        } else {
            // For history reports, add option to delete
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(BUTTON_FONT);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBackground(DANGER);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setPreferredSize(new Dimension(100, 35));
            deleteBtn.addActionListener(e -> {
                int index = reportsList.getSelectedIndex();
                if (index >= 0) {
                    deleteReport(reports.get(index));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            actionPanel.add(deleteBtn);
        }
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private void showVehicleReports(Vehicle vehicle) {
        List<DriverReport> vehicleReports = driverReports.stream()
            .filter(r -> r.vehicleId.equals(vehicle.id))
            .collect(Collectors.toList());
        
        if (vehicleReports.isEmpty()) {
            showNotification("No reports for " + vehicle.id, INFO);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Reports - " + vehicle.id, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel title = new JLabel("Driver Reports for " + vehicle.id);
        title.setFont(HEADER_FONT);
        title.setForeground(PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (DriverReport r : vehicleReports) {
            String severityIcon = r.severity.equals("Critical") ? "🔴" :
                                 r.severity.equals("High") ? "🟠" :
                                 r.severity.equals("Medium") ? "🟡" : "🔵";
            
            String statusIcon = r.status.equals("Pending") ? "⏳" :
                               r.status.equals("Scheduled") ? "📅" :
                               r.status.equals("Resolved") ? "✅" : "⏰";
            
            listModel.addElement(String.format("%s %s | %s | %s: %s", 
                severityIcon, statusIcon, r.reportId, r.driverName, 
                truncateText(r.description, 40)));
        }
        
        JList<String> reportsList = new JList<>(listModel);
        reportsList.setFont(REGULAR_FONT);
        reportsList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JScrollPane scrollPane = new JScrollPane(reportsList);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(CARD_BG);
        
        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(BUTTON_FONT);
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBackground(INFO);
        viewBtn.setBorderPainted(false);
        viewBtn.setPreferredSize(new Dimension(120, 35));
        viewBtn.addActionListener(e -> {
            int index = reportsList.getSelectedIndex();
            if (index >= 0) {
                showReportDetails(vehicleReports.get(index));
            }
        });
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(TEXT_SECONDARY);
        closeBtn.setBackground(CARD_BG);
        closeBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(viewBtn);
        buttonPanel.add(closeBtn);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showReportDetails(DriverReport report) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Report Details - " + report.reportId, true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BG);
        
        // Header with severity color
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String severityColor;
        switch (report.severity) {
            case "Critical": severityColor = "🔴"; break;
            case "High": severityColor = "🟠"; break;
            case "Medium": severityColor = "🟡"; break;
            default: severityColor = "🔵";
        }
        
        JLabel severityLabel = new JLabel(severityColor + " " + report.severity + " Severity");
        severityLabel.setFont(HEADER_FONT);
        severityLabel.setForeground(getSeverityColor(report.severity));
        
        JLabel statusLabel = new JLabel("Status: " + report.status);
        statusLabel.setFont(REGULAR_FONT);
        statusLabel.setForeground(getStatusColor(report.status));
        
        headerPanel.add(severityLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Details
        contentPanel.add(createDetailRow("Report ID:", report.reportId));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Vehicle:", report.vehicleId));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Driver:", report.driverName));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Date:", displayDateFormat.format(report.reportDate) + " at " + 
                         new SimpleDateFormat("HH:mm").format(report.reportDate)));
        contentPanel.add(Box.createVerticalStrut(15));
        
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(HEADER_FONT);
        descLabel.setForeground(TEXT_PRIMARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        
        JTextArea descArea = new JTextArea(report.description);
        descArea.setFont(REGULAR_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 248, 248));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        contentPanel.add(descArea);
        
        if (report.adminNotes != null && !report.adminNotes.isEmpty()) {
            contentPanel.add(Box.createVerticalStrut(8));
            JLabel notesLabel = new JLabel("Admin Notes:");
            notesLabel.setFont(HEADER_FONT);
            notesLabel.setForeground(TEXT_PRIMARY);
            notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(notesLabel);
            contentPanel.add(Box.createVerticalStrut(4));
            
            JTextArea notesArea = new JTextArea(report.adminNotes);
            notesArea.setFont(REGULAR_FONT);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            notesArea.setEditable(false);
            notesArea.setBackground(new Color(248, 248, 248));
            notesArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            contentPanel.add(notesArea);
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        if ("Pending".equals(report.status)) {
            JButton scheduleBtn = new JButton("Schedule Maintenance");
            scheduleBtn.setFont(BUTTON_FONT);
            scheduleBtn.setForeground(Color.WHITE);
            scheduleBtn.setBackground(SUCCESS);
            scheduleBtn.setBorderPainted(false);
            scheduleBtn.setPreferredSize(new Dimension(160, 35));
            scheduleBtn.addActionListener(e -> {
                dialog.dispose();
                showScheduleMaintenanceDialog(report);
            });
            
            JButton postponeBtn = new JButton("Postpone");
            postponeBtn.setFont(BUTTON_FONT);
            postponeBtn.setForeground(Color.WHITE);
            postponeBtn.setBackground(WARNING);
            postponeBtn.setBorderPainted(false);
            postponeBtn.setPreferredSize(new Dimension(100, 35));
            postponeBtn.addActionListener(e -> {
                dialog.dispose();
                postponeReport(report);
            });
            
            buttonPanel.add(scheduleBtn);
            buttonPanel.add(postponeBtn);
        }
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private Color getSeverityColor(String severity) {
        switch (severity) {
            case "Critical": return DANGER;
            case "High": return new Color(255, 87, 34);
            case "Medium": return WARNING;
            default: return INFO;
        }
    }

    private Color getStatusColor(String status) {
        switch (status) {
            case "Pending": return WARNING;
            case "Scheduled": return INFO;
            case "Resolved": return SUCCESS;
            default: return TEXT_SECONDARY;
        }
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(CARD_BG);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(HEADER_FONT);
        labelComp.setForeground(TEXT_SECONDARY);
        labelComp.setPreferredSize(new Dimension(80, 25));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(REGULAR_FONT);
        valueComp.setForeground(TEXT_PRIMARY);
        
        panel.add(labelComp);
        panel.add(valueComp);
        
        return panel;
    }

    /**
     * Show dialog to schedule maintenance from a driver report
     */
    private void showScheduleMaintenanceDialog(DriverReport report) {
        String[] options = {"Immediate", "Schedule for Later", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainPanel,
            "Schedule maintenance for " + report.vehicleId + "?\n\n" +
            "Report: " + report.description,
            "Schedule Maintenance",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) { // Immediate
            Vehicle vehicle = findVehicle(report.vehicleId).orElse(null);
            if (vehicle != null) {
                // Check if vehicle is already in maintenance
                if ("Maintenance".equals(vehicle.status)) {
                    int overwrite = JOptionPane.showConfirmDialog(mainPanel,
                        "Vehicle is already in maintenance. Create a new maintenance record anyway?",
                        "Vehicle in Maintenance",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (overwrite != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                // Unassign driver if exists
                if (vehicle.driverName != null && !"Unassigned".equals(vehicle.driverName)) {
                    findDriver(vehicle.driverName).ifPresent(d -> {
                        d.currentVehicle = null;
                        d.status = "Available";
                    });
                    vehicle.driverName = null;
                }
                
                // Update vehicle status
                vehicle.status = "Maintenance";
                
                // Create maintenance record in MaintenanceManagement
                if (maintenanceManagement != null) {
                    // Format the maintenance description with report details
                    String maintenanceDesc = String.format("[DRIVER REPORT #%s - %s] %s (Severity: %s)", 
                        report.reportId, 
                        report.driverName, 
                        report.description,
                        report.severity);
                    
                    // Add to maintenance management
                    maintenanceManagement.addVehicleToMaintenance(
                        vehicle.id, 
                        vehicle.model, 
                        maintenanceDesc
                    );
                    
                    // Update report status
                    report.status = "Scheduled";
                    report.adminNotes = "Immediate maintenance scheduled on " + 
                        displayDateFormat.format(new Date());
                    saveDriverReports();
                    
                    // Force refresh maintenance view if possible
                    maintenanceManagement.refreshData();
                    
                    showNotification("✓ Maintenance created for " + report.vehicleId, SUCCESS);
                } else {
                    // Just update report if no maintenance management
                    report.status = "Scheduled";
                    report.adminNotes = "Marked for maintenance (no maintenance module)";
                    saveDriverReports();
                    showNotification("✓ Report marked for maintenance", INFO);
                }
                
                saveData();
                refreshTable();
            }
        } else if (choice == 1) { // Schedule for Later
            // Create a date picker dialog
            JPanel datePanel = new JPanel(new GridBagLayout());
            datePanel.setBackground(CARD_BG);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            JLabel instructionLabel = new JLabel("Select scheduled date:");
            instructionLabel.setFont(REGULAR_FONT);
            
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            // Default to tomorrow
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            dateSpinner.setValue(tomorrow.getTime());
            dateSpinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = 2;
            datePanel.add(instructionLabel, gbc);
            
            gbc.gridy = 1;
            datePanel.add(dateSpinner, gbc);
            
            int result = JOptionPane.showConfirmDialog(mainPanel, datePanel,
                "Schedule Maintenance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                Date scheduledDate = (Date) dateSpinner.getValue();
                
                // Update the report
                report.status = "Scheduled";
                report.adminNotes = "Scheduled for " + displayDateFormat.format(scheduledDate);
                saveDriverReports();
                
                showNotification("✓ Report scheduled for " + displayDateFormat.format(scheduledDate), INFO);
            }
        }
    }

    private void postponeReport(DriverReport report) {
        String[] options = {"24 Hours", "48 Hours", "1 Week", "Custom"};
        int choice = JOptionPane.showOptionDialog(mainPanel,
            "Postpone for how long?",
            "Postpone Report",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        Calendar cal = Calendar.getInstance();
        
        switch (choice) {
            case 0: cal.add(Calendar.DAY_OF_MONTH, 1); break;
            case 1: cal.add(Calendar.DAY_OF_MONTH, 2); break;
            case 2: cal.add(Calendar.DAY_OF_MONTH, 7); break;
            case 3:
                String dateStr = JOptionPane.showInputDialog(mainPanel, "New date (yyyy-MM-dd):");
                if (dateStr == null) return;
                try {
                    cal.setTime(dateFormat.parse(dateStr));
                } catch (Exception ex) {
                    showNotification("Invalid date", DANGER);
                    return;
                }
                break;
            default: return;
        }
        
        report.status = "Postponed";
        report.adminNotes = "Postponed until " + displayDateFormat.format(cal.getTime());
        saveDriverReports();
        updateStats();
        
        showNotification("Report postponed until " + displayDateFormat.format(cal.getTime()), INFO);
    }

    /**
     * Delete a driver report
     */
    private void deleteReport(DriverReport report) {
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Delete report #" + report.reportId + "?\n\n" +
            "Vehicle: " + report.vehicleId + "\n" +
            "Driver: " + report.driverName + "\n" +
            "Description: " + truncateText(report.description, 50),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            driverReports.remove(report);
            saveDriverReports();
            updateStats();
            refreshTable();
            showNotification("✓ Report deleted", SUCCESS);
        }
    }

    private void addFormRow(JPanel panel, String label, JComponent field, 
                            GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(HEADER_FONT);
        jLabel.setForeground(TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void styleFormField(JComponent field) {
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(250, 35));
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

    private String generateId(String type) {
        String prefix = type.equals("Truck") ? "TRK" : 
                       type.equals("Van") ? "VAN" : 
                       type.equals("Car") ? "CAR" : "MTC";
        int count = typeCounters.getOrDefault(type, 0) + 1;
        typeCounters.put(type, count);
        return prefix + String.format("%03d", count);
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
                // Check if there are any pending maintenance records
                // If not, set to Active
                v.status = "Active";
                changed = true;
            }
        }
        
        if (changed) {
            saveData();
            refreshTable();
        }
    }

    private Optional<Vehicle> findVehicle(String id) {
        return vehicles.stream().filter(v -> v.id.equals(id)).findFirst();
    }

    private Optional<Driver> findDriver(String name) {
        return drivers.stream().filter(d -> d.name.equals(name)).findFirst();
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
            if (statValues[0] != null) statValues[0].setText(String.valueOf(getTotalCount()));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(getActiveCount()));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(getMaintenanceCount()));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(getExpiredRoadTaxCount()));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(getAvailableDriversCount()));
            if (statValues[5] != null) statValues[5].setText(String.valueOf(getTotalReportsCount())); // Changed to total reports
        });
    }

    private void showVehicleDetails(Vehicle v) {
        boolean taxExpired = v.roadTaxExpiry.before(new Date());
        long reportCount = driverReports.stream()
            .filter(r -> r.vehicleId.equals(v.id) && "Pending".equals(r.status))
            .count();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Vehicle Details", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BG);
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setBackground(CARD_BG);
        
        String icon = v.type.equals("Truck") ? "🚛" : 
                     v.type.equals("Van") ? "🚐" : 
                     v.type.equals("Car") ? "🚗" : "🏍️";
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(PRIMARY);
        
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
        
        headerPanel.add(iconLabel);
        headerPanel.add(titlePanel);
        
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        contentPanel.add(createDetailRow("Type:", v.type));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Plate:", v.numberPlate));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Fuel:", v.fuelType));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Status:", v.status));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Tax Expiry:", displayDateFormat.format(v.roadTaxExpiry) + 
                     (taxExpired ? " (Overdue)" : "")));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Driver:", v.driverName == null ? "Unassigned" : v.driverName));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Reports:", reportCount + " pending"));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(closeBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void createSampleData() {
        try {
            Date now = new Date();
            Date validDate1 = new Date(now.getTime() + 86400000L * 365);
            Date validDate2 = new Date(now.getTime() + 86400000L * 180);
            Date expiredDate1 = new Date(now.getTime() - 86400000L * 30);
            Date expiredDate2 = new Date(now.getTime() - 86400000L * 60);
            Date futureDate = new Date(now.getTime() + 86400000L * 90);
            
            vehicles.add(new Vehicle("TRK001", "Freightliner Cascadia", "Active", "John Smith", 
                "Truck", "ABC-1234", validDate1, "Diesel"));
            vehicles.add(new Vehicle("VAN001", "Ford Transit", "Maintenance", null, 
                "Van", "DEF-5678", expiredDate1, "Gasoline"));
            vehicles.add(new Vehicle("TRK002", "Peterbilt 579", "Active", null, 
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
                "Active", new Date(now.getTime() - 86400000L * 30), "TRK001"));
            drivers.add(new Driver("Mike Johnson", "DL12346", "(555) 234-5678", "mike.j@email.com", 
                "Active", new Date(now.getTime() - 86400000L * 15), "CAR001"));
            drivers.add(new Driver("Sarah Wilson", "DL12347", "(555) 345-6789", "sarah.w@email.com", 
                "Active", new Date(now.getTime() - 86400000L * 10), "CAR002"));
            drivers.add(new Driver("Robert Brown", "DL12348", "(555) 456-7890", "robert.b@email.com", 
                "Available", new Date(now.getTime() - 86400000L * 5), null));
            drivers.add(new Driver("Emily Davis", "DL12349", "(555) 567-8901", "emily.d@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("David Lee", "DL12350", "(555) 678-9012", "david.l@email.com", 
                "Available", new Date(), null));
            drivers.add(new Driver("Unassigned", "", "", "", "Available", new Date(), null));
            
            typeCounters.put("Truck", 3);
            typeCounters.put("Van", 3);
            typeCounters.put("Car", 2);
            typeCounters.put("Motorcycle", 1);
            
            driverReports.add(new DriverReport(
                "RPT001", "VAN001", "Robert Brown", new Date(now.getTime() - 86400000L * 2),
                "Engine making strange noise when accelerating. The sound gets louder at higher RPMs.",
                "High", "Pending", ""
            ));
            driverReports.add(new DriverReport(
                "RPT002", "TRK002", "Emily Davis", new Date(now.getTime() - 86400000L * 1),
                "Brakes feel soft and spongy. Needs immediate inspection before next trip.",
                "Critical", "Pending", ""
            ));
            driverReports.add(new DriverReport(
                "RPT003", "CAR001", "Mike Johnson", new Date(now.getTime() - 86400000L * 3),
                "Check engine light is on. Vehicle seems to be running rough at idle.",
                "Medium", "Scheduled", "Scheduled for next week"
            ));
            driverReports.add(new DriverReport(
                "RPT004", "VAN002", "David Lee", new Date(now.getTime() - 86400000L * 5),
                "Air conditioning not working properly. Blowing warm air only.",
                "Low", "Resolved", "Fixed on 2024-01-15"
            ));
            
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
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        
        JLabel label = new JLabel(message);
        label.setFont(REGULAR_FONT);
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        
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

    // ==================== PUBLIC METHODS ====================
    
    public JPanel getMainPanel() { 
        return mainPanel; 
    }
    
    public JPanel getRefreshedPanel() {
        // REMOVED: refreshData() call - just return the panel without refreshing
        return mainPanel;
    }
    
    public void refreshData() {
        // This method is kept for internal use only
        vehicles.clear();
        drivers.clear();
        typeCounters.clear();
        driverReports.clear();
        loadData();
        loadDriverReports();
        if (maintenanceManagement != null) syncWithMaintenance();
        refreshTable();
    }
    
    public int getTotalCount() {
        return vehicles.size();
    }
    
    public int getActiveCount() {
        return (int) vehicles.stream().filter(v -> "Active".equals(v.status)).count();
    }
    
    public int getMaintenanceCount() {
        return (int) vehicles.stream().filter(v -> "Maintenance".equals(v.status)).count();
    }
    
    public int getInactiveCount() {
        return (int) vehicles.stream().filter(v -> "Inactive".equals(v.status)).count();
    }
    
    public int getExpiredRoadTaxCount() {
        return (int) vehicles.stream().filter(v -> v.roadTaxExpiry.before(new Date())).count();
    }
    
    public int getAvailableDriversCount() {
        return (int) drivers.stream().filter(d -> "Available".equals(d.status) && !"Unassigned".equals(d.name)).count();
    }
    
    public int getTotalReportsCount() {
        return driverReports.size(); // Returns total number of reports (all statuses)
    }
    
    public int getPendingReportsCount() {
        return (int) driverReports.stream().filter(r -> "Pending".equals(r.status)).count();
    }
    
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    // ==================== INTEGRATION METHODS ====================
    
    /**
     * Get vehicle by ID for other modules
     */
    public Vehicle getVehicleById(String id) {
        return findVehicle(id).orElse(null);
    }
    
    /**
     * Get driver by name for other modules
     */
    public Driver getDriverByName(String name) {
        return findDriver(name).orElse(null);
    }
    
    /**
     * Update vehicle status (called by MaintenanceManagement)
     */
    public void updateVehicleStatus(String vehicleId, String newStatus) {
        findVehicle(vehicleId).ifPresent(v -> {
            v.status = newStatus;
            saveData();
            refreshTable();
        });
    }
    
    /**
     * Assign driver to vehicle (called by DriverManagement)
     */
    public boolean assignDriverToVehicle(String driverName, String vehicleId) {
        Optional<Vehicle> vehicleOpt = findVehicle(vehicleId);
        Optional<Driver> driverOpt = findDriver(driverName);
        
        if (vehicleOpt.isPresent() && driverOpt.isPresent()) {
            Vehicle v = vehicleOpt.get();
            Driver d = driverOpt.get();
            
            // Unassign current driver if any
            if (v.driverName != null) {
                findDriver(v.driverName).ifPresent(oldDriver -> {
                    oldDriver.currentVehicle = null;
                    oldDriver.status = "Available";
                });
            }
            
            // Unassign from old vehicle if driver had one
            if (d.currentVehicle != null) {
                findVehicle(d.currentVehicle).ifPresent(oldVehicle -> {
                    oldVehicle.driverName = null;
                });
            }
            
            // Assign new
            v.driverName = driverName;
            d.currentVehicle = vehicleId;
            d.status = "Active";
            v.status = "Active";
            
            saveData();
            refreshTable();
            return true;
        }
        return false;
    }
    
    /**
     * Check if a vehicle is currently in maintenance
     */
    public boolean isVehicleInMaintenance(String vehicleId) {
        return findVehicle(vehicleId)
            .map(v -> "Maintenance".equals(v.status))
            .orElse(false);
    }
    
    /**
     * Get vehicles by their status
     */
    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicles.stream()
            .filter(v -> v.status.equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }
    
    /**
     * Complete maintenance for a specific vehicle
     */
    public void completeMaintenanceForVehicle(String vehicleId) {
        findVehicle(vehicleId).ifPresent(v -> {
            if (v.status.equals("Maintenance")) {
                v.status = "Active";
                saveData();
                refreshTable();
            }
        });
    }

    // ==================== DATA CLASSES ====================

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

    private static class DriverReport {
        String reportId, vehicleId, driverName, description, severity, status, adminNotes;
        Date reportDate;
        
        DriverReport(String reportId, String vehicleId, String driverName, Date reportDate,
                    String description, String severity, String status, String adminNotes) {
            this.reportId = reportId;
            this.vehicleId = vehicleId;
            this.driverName = driverName;
            this.reportDate = reportDate;
            this.description = description;
            this.severity = severity;
            this.status = status;
            this.adminNotes = adminNotes;
        }
    }
}