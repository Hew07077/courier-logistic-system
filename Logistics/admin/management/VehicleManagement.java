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
    private Map<String, Integer> typeCounters = new HashMap<>();
    private List<DriverReport> driverReports = new ArrayList<>();
    
    private MaintenanceManagement maintenanceManagement;
    private DriverManagement driverManagement;
    private OrderManagement orderManagement;
    
    private static final String VEHICLES_FILE = "vehicles_data.txt";
    private static final String COUNTERS_FILE = "counters_data.txt";
    private static final String REPORTS_FILE = "driver_reports.txt";
    private static final String DRIVERS_FILE = "drivers.txt";
    
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
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 12);
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    
    // Driver data cache from drivers.txt (SINGLE SOURCE OF TRUTH)
    private Map<String, DriverData> driverCache = new HashMap<>();
    
    public interface DriverProfileListener {
        void onDriverProfileClicked(String driverId);
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
        loadDriversFromFile();
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
        loadDriversFromFile();
        refreshTable();
    }
    
    public void setOrderManagement(OrderManagement orderMgmt) {
        this.orderManagement = orderMgmt;
    }
    
    /**
     * Load driver data from drivers.txt file - SINGLE SOURCE OF TRUTH
     */
    private void loadDriversFromFile() {
        driverCache.clear();
        File file = new File(DRIVERS_FILE);
        if (!file.exists()) {
            System.out.println("Drivers file not found: " + DRIVERS_FILE);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    DriverData driver = new DriverData();
                    driver.id = parts[0].trim();
                    driver.name = parts[1].trim();
                    driver.phone = parts[2].trim();
                    driver.email = parts[3].trim();
                    driver.licenseNumber = parts[4].trim();
                    driver.licenseExpiry = parts[5].trim();
                    driver.workStatus = parts[6].trim();
                    driver.approvalStatus = parts[7].trim();
                    driver.vehicleId = parts[8].trim();
                    if (driver.vehicleId.isEmpty()) driver.vehicleId = null;
                    
                    driverCache.put(driver.id, driver);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading drivers: " + e.getMessage());
        }
    }
    
    /**
     * Update driver's vehicle assignment in drivers.txt - ONLY SOURCE
     */
    private void updateDriverVehicleAssignment(String driverId, String vehicleId) {
        DriverData driver = driverCache.get(driverId);
        if (driver == null) return;
        
        // Update in memory
        driver.vehicleId = vehicleId;
        if (vehicleId != null && !vehicleId.isEmpty()) {
            driver.workStatus = "On Delivery";
        } else {
            driver.workStatus = "Available";
        }
        
        // Write back to file
        rewriteDriversFile();
        
        // Also update the vehicle file to reflect status change
        if (vehicleId != null) {
            Optional<Vehicle> vehicleOpt = findVehicle(vehicleId);
            if (vehicleOpt.isPresent()) {
                Vehicle v = vehicleOpt.get();
                v.status = "Active";
                saveData();
            }
        } else {
            // When unassigning, find which vehicle had this driver and clear it
            for (Vehicle v : vehicles) {
                String currentDriverId = getDriverIdForVehicle(v.id);
                if (driverId.equals(currentDriverId)) {
                    v.driverName = null;
                    saveData();
                    break;
                }
            }
        }
        
        // Reload drivers to ensure cache is fresh
        loadDriversFromFile();
        
        // Refresh display
        refreshTable();
        updateStats();
    }
    
    /**
     * Rewrite the entire drivers file with updated data
     */
    private void rewriteDriversFile() {
        File file = new File(DRIVERS_FILE);
        if (!file.exists()) return;
        
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    lines.add(line);
                    firstLine = false;
                    continue;
                }
                
                if (line.trim().isEmpty()) {
                    lines.add(line);
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    String driverId = parts[0].trim();
                    DriverData driver = driverCache.get(driverId);
                    if (driver != null) {
                        StringBuilder updatedLine = new StringBuilder();
                        updatedLine.append(driver.id).append("|");
                        updatedLine.append(driver.name).append("|");
                        updatedLine.append(driver.phone).append("|");
                        updatedLine.append(driver.email).append("|");
                        updatedLine.append(driver.licenseNumber).append("|");
                        updatedLine.append(driver.licenseExpiry).append("|");
                        updatedLine.append(driver.workStatus).append("|");
                        updatedLine.append(driver.approvalStatus).append("|");
                        updatedLine.append(driver.vehicleId != null ? driver.vehicleId : "").append("|");
                        
                        if (parts.length > 9) {
                            for (int i = 9; i < parts.length; i++) {
                                updatedLine.append("|").append(parts[i]);
                            }
                        }
                        lines.add(updatedLine.toString());
                    } else {
                        lines.add(line);
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private DriverData getDriverById(String id) {
        return driverCache.get(id);
    }
    
    private DriverData getDriverByName(String name) {
        if (name == null) return null;
        for (DriverData driver : driverCache.values()) {
            if (driver.name.equals(name)) {
                return driver;
            }
        }
        return null;
    }
    
    private String getDriverDisplayInfo(String driverName) {
        if (driverName == null || driverName.equals("Unassigned")) {
            return "Unassigned";
        }
        
        DriverData driver = getDriverByName(driverName);
        if (driver == null) return driverName;
        
        if ("APPROVED".equals(driver.approvalStatus)) {
            if ("On Delivery".equals(driver.workStatus)) {
                return driver.name;
            } else if ("Available".equals(driver.workStatus)) {
                return driver.name;
            } else {
                return driver.name + " (" + driver.workStatus + ")";
            }
        } else if ("PENDING".equals(driver.approvalStatus)) {
            return driver.name + " (Pending Approval)";
        } else if ("REJECTED".equals(driver.approvalStatus)) {
            return driver.name + " (Rejected)";
        }
        return driver.name;
    }
    
    private String getDriverIdForVehicle(String vehicleId) {
        for (DriverData driver : driverCache.values()) {
            if (driver.vehicleId != null && driver.vehicleId.equals(vehicleId)) {
                return driver.id;
            }
        }
        return null;
    }

    private void loadData() {
        loadFromFile(VEHICLES_FILE, this::parseVehicle);
        loadFromFile(COUNTERS_FILE, line -> {
            String[] parts = line.split("=");
            if (parts.length == 2) typeCounters.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
        });
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
                    null,
                    p[4].trim(), p[5].trim(),
                    new Date(Long.parseLong(p[6].trim())), p[7].trim()));
            }
        } catch (Exception e) { 
            System.err.println("Error parsing vehicle: " + line); 
        }
    }

    private void saveData() {
        saveToFile(VEHICLES_FILE, vehicles, v -> String.format("%s|%s|%s|%s|%s|%s|%s|%s",
            v.id, v.model, v.status, "",
            v.type, v.numberPlate, v.roadTaxExpiry.getTime(), v.fuelType));
        
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
        loadDriversFromFile();
        
        List<DriverData> available = new ArrayList<>();
        for (DriverData d : driverCache.values()) {
            if ("APPROVED".equals(d.approvalStatus) && 
                "Available".equals(d.workStatus) && 
                (d.vehicleId == null || d.vehicleId.isEmpty())) {
                available.add(d);
            }
        }
        
        if (available.isEmpty()) {
            showNotification("No available drivers", WARNING);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Available Drivers", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel title = new JLabel("Available Drivers");
        title.setFont(HEADER_FONT);
        title.setForeground(PURPLE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (DriverData d : available) {
            listModel.addElement(String.format("%s (ID: %s) - %s | %s", 
                d.name, d.id, d.licenseNumber, d.phone));
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
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
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
                            String vehicleId = vehicles.get(row).id;
                            String driverId = getDriverIdForVehicle(vehicleId);
                            if (driverId != null && driverProfileListener != null) {
                                driverProfileListener.onDriverProfileClicked(driverId);
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
            
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < vehicles.size()) {
                String vehicleId = vehicles.get(modelRow).id;
                String driverId = getDriverIdForVehicle(vehicleId);
                
                if (driverId != null) {
                    DriverData driver = driverCache.get(driverId);
                    if (driver != null) {
                        setText(getDriverDisplayInfo(driver.name));
                    } else {
                        setText("Unassigned");
                        setForeground(TEXT_MUTED);
                        setFont(getFont().deriveFont(Font.ITALIC));
                    }
                } else {
                    setText("Unassigned");
                    setForeground(TEXT_MUTED);
                    setFont(getFont().deriveFont(Font.ITALIC));
                }
            } else {
                setText("Unassigned");
                setForeground(TEXT_MUTED);
                setFont(getFont().deriveFont(Font.ITALIC));
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
                    setText(text + " (Overdue)");
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (recordDate.equals(today) && !isSelected) {
                    setForeground(WARNING);
                    setText(text + " (Today)");
                } else {
                    setForeground(TEXT_PRIMARY);
                    setText(text);
                }
            }
            
            setOpaque(true);
            setBackground(isSelected ? SELECTION_COLOR : 
                         (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
            
            return this;
        }
    }

    // Button panel with 6 buttons (no refresh, no icons)
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panel.setBackground(BG_COLOR);
        
        // Create buttons without icons
        JButton addBtn = createStyledButton("Add", SUCCESS, SUCCESS_DARK);
        JButton editBtn = createStyledButton("Edit", WARNING, WARNING_DARK);
        JButton deleteBtn = createStyledButton("Delete", DANGER, DANGER_DARK);
        JButton driverBtn = createStyledButton("Driver", PURPLE, PURPLE_DARK);
        JButton serviceBtn = createStyledButton("Service", INFO, INFO_DARK);
        JButton reportsBtn = createStyledButton("Reports", PRIMARY, PRIMARY_DARK);
        
        // Add action listeners
        addBtn.addActionListener(e -> addVehicle());
        editBtn.addActionListener(e -> editVehicle());
        deleteBtn.addActionListener(e -> deleteVehicle());
        driverBtn.addActionListener(e -> showDriverActions());
        serviceBtn.addActionListener(e -> scheduleMaintenance());
        reportsBtn.addActionListener(e -> showDriverReportsDialog());
        
        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        panel.add(driverBtn);
        panel.add(serviceBtn);
        panel.add(reportsBtn);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(90, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
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
        String currentDriverId = getDriverIdForVehicle(v.id);
        DriverData currentDriver = currentDriverId != null ? driverCache.get(currentDriverId) : null;
        
        // Create a clean dialog for driver actions
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Driver Management - " + v.id, true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("Driver Assignment");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PURPLE);
        
        JLabel vehicleLabel = new JLabel("Vehicle: " + v.id + " (" + v.model + ")");
        vehicleLabel.setFont(REGULAR_FONT);
        vehicleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(vehicleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Current driver info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel currentLabel = new JLabel("Current Driver:");
        currentLabel.setFont(HEADER_FONT);
        
        String driverDisplay = currentDriver != null ? 
            getDriverDisplayInfo(currentDriver.name) : "Unassigned";
        JLabel driverNameLabel = new JLabel(driverDisplay);
        driverNameLabel.setFont(REGULAR_FONT);
        driverNameLabel.setForeground(currentDriver != null ? SUCCESS : TEXT_MUTED);
        
        infoPanel.add(currentLabel, BorderLayout.NORTH);
        infoPanel.add(driverNameLabel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton assignBtn = new JButton("Assign New Driver");
        assignBtn.setBackground(SUCCESS);
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setFont(BUTTON_FONT);
        assignBtn.setFocusPainted(false);
        assignBtn.setBorderPainted(false);
        assignBtn.setPreferredSize(new Dimension(150, 35));
        assignBtn.addActionListener(e -> {
            dialog.dispose();
            assignDriver(v);
        });
        
        JButton unassignBtn = new JButton("Unassign Driver");
        unassignBtn.setBackground(DANGER);
        unassignBtn.setForeground(Color.WHITE);
        unassignBtn.setFont(BUTTON_FONT);
        unassignBtn.setFocusPainted(false);
        unassignBtn.setBorderPainted(false);
        unassignBtn.setPreferredSize(new Dimension(150, 35));
        unassignBtn.setEnabled(currentDriver != null);
        unassignBtn.addActionListener(e -> {
            if (currentDriver != null) {
                dialog.dispose();
                unassignDriver(v, currentDriver.id);
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(TEXT_SECONDARY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(assignBtn);
        buttonPanel.add(unassignBtn);
        buttonPanel.add(cancelBtn);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Assign driver with dropdown menu
     * Drivers already assigned to ANY vehicle will NOT appear in the dropdown
     */
    private void assignDriver(Vehicle vehicle) {
        if (vehicle.status.equals("Maintenance")) {
            showNotification("Cannot assign to vehicle in maintenance", WARNING);
            return;
        }
        
        loadDriversFromFile();
        
        // Get current driver assigned to this vehicle (if any)
        String currentDriverId = getDriverIdForVehicle(vehicle.id);
        DriverData currentDriver = currentDriverId != null ? driverCache.get(currentDriverId) : null;
        
        // Build list of available drivers (approved, not currently assigned to ANY vehicle)
        List<DriverData> availableDrivers = new ArrayList<>();
        for (DriverData d : driverCache.values()) {
            // Only show drivers that:
            // 1. Are APPROVED
            // 2. Are "Available" status (meaning not assigned to any vehicle)
            // 3. Have no vehicle assigned OR vehicleId is null/empty
            if ("APPROVED".equals(d.approvalStatus) && 
                "Available".equals(d.workStatus) && 
                (d.vehicleId == null || d.vehicleId.isEmpty())) {
                availableDrivers.add(d);
            }
        }
        
        // If there's a current driver and they are not in the available list,
        // we need to check if they are actually assigned to this vehicle
        if (currentDriver != null && !availableDrivers.contains(currentDriver)) {
            // Current driver is assigned to this vehicle, so we need to offer unassign first
            int option = JOptionPane.showConfirmDialog(mainPanel,
                "Vehicle already has driver: " + currentDriver.name + "\n\n" +
                "Would you like to unassign this driver first?",
                "Driver Already Assigned",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                unassignDriver(vehicle, currentDriver.id);
                // After unassigning, try assigning again
                assignDriver(vehicle);
            }
            return;
        }
        
        if (availableDrivers.isEmpty()) {
            showNotification("No available drivers to assign.\n\n" +
                "Only APPROVED drivers with 'Available' status and no vehicle assignment can be selected.", WARNING);
            return;
        }
        
        // Create dropdown options
        String[] driverOptions = new String[availableDrivers.size()];
        for (int i = 0; i < availableDrivers.size(); i++) {
            DriverData d = availableDrivers.get(i);
            driverOptions[i] = String.format("%s (ID: %s) - %s | %s", 
                d.name, d.id, d.licenseNumber, d.phone);
        }
        
        // Show dropdown dialog
        JComboBox<String> driverCombo = new JComboBox<>(driverOptions);
        driverCombo.setFont(REGULAR_FONT);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Select driver to assign to " + vehicle.id + ":");
        label.setFont(HEADER_FONT);
        label.setForeground(TEXT_PRIMARY);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(driverCombo, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(mainPanel, panel,
            "Assign Driver to " + vehicle.id,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            int selectedIndex = driverCombo.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < availableDrivers.size()) {
                DriverData selectedDriver = availableDrivers.get(selectedIndex);
                
                // Double-check the driver is still available (could have been assigned in another window)
                loadDriversFromFile();
                DriverData freshDriver = driverCache.get(selectedDriver.id);
                if (freshDriver == null || 
                    !"Available".equals(freshDriver.workStatus) || 
                    (freshDriver.vehicleId != null && !freshDriver.vehicleId.isEmpty())) {
                    showNotification("Driver " + selectedDriver.name + " is no longer available!", WARNING);
                    return;
                }
                
                // Unassign any existing driver from this vehicle first
                String existingDriverId = getDriverIdForVehicle(vehicle.id);
                if (existingDriverId != null) {
                    updateDriverVehicleAssignment(existingDriverId, null);
                }
                
                // Assign new driver
                updateDriverVehicleAssignment(selectedDriver.id, vehicle.id);
                
                vehicle.status = "Active";
                saveData();
                
                refreshTable();
                showNotification(selectedDriver.name + " assigned to " + vehicle.id, SUCCESS);
            }
        }
    }

    private void unassignDriver(Vehicle vehicle, String driverId) {
        DriverData driver = driverCache.get(driverId);
        if (driver == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Unassign " + driver.name + " from " + vehicle.id + "?",
            "Confirm Unassign",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            updateDriverVehicleAssignment(driverId, null);
            
            vehicle.driverName = null;
            saveData();
            
            refreshTable();
            showNotification(driver.name + " is now available for assignment", SUCCESS);
        }
    }

    // Schedule maintenance only - cannot complete here
    private void scheduleMaintenance() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) { 
            showNotification("Please select a vehicle", WARNING); 
            return; 
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        String driverId = getDriverIdForVehicle(v.id);
        
        // Check if already in maintenance
        if (v.status.equals("Maintenance")) {
            showNotification("Vehicle is already in maintenance. To complete, go to Maintenance Management.", INFO);
            return;
        }
        
        // Create a custom dialog for maintenance description
        JDialog serviceDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Schedule Maintenance - " + v.id, true);
        serviceDialog.setSize(550, 450);
        serviceDialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("Schedule Maintenance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(INFO);
        
        JLabel vehicleLabel = new JLabel("Vehicle: " + v.id + " (" + v.model + ")");
        vehicleLabel.setFont(REGULAR_FONT);
        vehicleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(vehicleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Service description panel
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBackground(CARD_BG);
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel descLabel = new JLabel("Service Description:");
        descLabel.setFont(HEADER_FONT);
        descLabel.setForeground(TEXT_PRIMARY);
        
        JTextArea descArea = new JTextArea(5, 30);
        descArea.setFont(REGULAR_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Add placeholder text
        descArea.setText("Enter maintenance details...");
        descArea.setForeground(TEXT_MUTED);
        descArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (descArea.getText().equals("Enter maintenance details...")) {
                    descArea.setText("");
                    descArea.setForeground(TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (descArea.getText().trim().isEmpty()) {
                    descArea.setText("Enter maintenance details...");
                    descArea.setForeground(TEXT_MUTED);
                }
            }
        });
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(450, 100));
        
        descPanel.add(descLabel, BorderLayout.NORTH);
        descPanel.add(descScroll, BorderLayout.CENTER);
        
        // Quick selection buttons for common service types
        JPanel quickSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        quickSelectPanel.setBackground(CARD_BG);
        quickSelectPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel quickLabel = new JLabel("Quick Select:");
        quickLabel.setFont(SMALL_FONT);
        quickLabel.setForeground(TEXT_SECONDARY);
        
        String[] quickOptions = {"Oil Change", "Tire Rotation", "Brake Service", "Engine Check", "General Service"};
        for (String option : quickOptions) {
            JButton quickBtn = new JButton(option);
            quickBtn.setFont(SMALL_FONT);
            quickBtn.setBackground(CARD_BG);
            quickBtn.setForeground(INFO);
            quickBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            quickBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            quickBtn.addActionListener(e -> {
                descArea.setText(option);
                descArea.setForeground(TEXT_PRIMARY);
            });
            quickSelectPanel.add(quickBtn);
        }
        
        // Additional info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel infoLabel = new JLabel("Additional Information:");
        infoLabel.setFont(HEADER_FONT);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel driverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        driverPanel.setBackground(CARD_BG);
        JLabel driverIcon = new JLabel("Driver:");
        driverIcon.setFont(REGULAR_FONT);
        driverIcon.setForeground(TEXT_SECONDARY);
        
        JLabel driverStatus = new JLabel(driverId != null ? 
            "Assigned - Will be unassigned automatically" : "No driver assigned");
        driverStatus.setFont(REGULAR_FONT);
        driverStatus.setForeground(driverId != null ? WARNING : SUCCESS);
        
        driverPanel.add(driverIcon);
        driverPanel.add(driverStatus);
        
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        notePanel.setBackground(CARD_BG);
        JLabel noteIcon = new JLabel("Note:");
        noteIcon.setFont(REGULAR_FONT);
        noteIcon.setForeground(TEXT_SECONDARY);
        
        JLabel noteText = new JLabel("Maintenance completion must be done in Maintenance Management");
        noteText.setFont(SMALL_FONT);
        noteText.setForeground(TEXT_MUTED);
        
        notePanel.add(noteIcon);
        notePanel.add(noteText);
        
        infoPanel.add(infoLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(driverPanel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(notePanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton scheduleBtn = new JButton("Schedule Maintenance");
        scheduleBtn.setBackground(INFO);
        scheduleBtn.setForeground(Color.WHITE);
        scheduleBtn.setFont(BUTTON_FONT);
        scheduleBtn.setFocusPainted(false);
        scheduleBtn.setBorderPainted(false);
        scheduleBtn.setPreferredSize(new Dimension(150, 38));
        scheduleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(TEXT_SECONDARY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        scheduleBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                scheduleBtn.setBackground(INFO_DARK);
            }
            public void mouseExited(MouseEvent e) {
                scheduleBtn.setBackground(INFO);
            }
        });
        
        cancelBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelBtn.setBackground(new Color(90, 98, 104));
            }
            public void mouseExited(MouseEvent e) {
                cancelBtn.setBackground(TEXT_SECONDARY);
            }
        });
        
        scheduleBtn.addActionListener(e -> {
            String description = descArea.getText().trim();
            
            // Validate description
            if (description.isEmpty() || description.equals("Enter maintenance details...")) {
                showNotification("Please enter a service description", WARNING);
                return;
            }
            
            if (description.length() < 5) {
                showNotification("Please provide a more detailed description (minimum 5 characters)", WARNING);
                return;
            }
            
            if (description.length() > 500) {
                showNotification("Description is too long (maximum 500 characters)", WARNING);
                return;
            }
            
            // Confirm maintenance scheduling
            String confirmMessage = "Schedule maintenance for " + v.id + " (" + v.model + ")?\n\n";
            confirmMessage += "Service: " + description + "\n\n";
            if (driverId != null) {
                confirmMessage += "Driver will be automatically unassigned.\n";
            }
            confirmMessage += "\nNote: Maintenance completion must be done in Maintenance Management.";
            
            int confirm = JOptionPane.showConfirmDialog(mainPanel, confirmMessage,
                "Confirm Maintenance Schedule",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Unassign driver if exists
                if (driverId != null) {
                    updateDriverVehicleAssignment(driverId, null);
                }
                
                // Add to maintenance management
                if (maintenanceManagement != null) {
                    String maintenanceDesc = String.format("[SCHEDULED] %s", description);
                    maintenanceManagement.addVehicleToMaintenance(v.id, v.model, maintenanceDesc);
                    v.status = "Maintenance";
                    saveData();
                    refreshTable();
                    showNotification("Maintenance scheduled for " + v.id, SUCCESS);
                } else {
                    v.status = "Maintenance";
                    saveData();
                    refreshTable();
                    showNotification("Maintenance scheduled for " + v.id + " (Maintenance module not available)", INFO);
                }
                
                serviceDialog.dispose();
            }
        });
        
        cancelBtn.addActionListener(e -> serviceDialog.dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(scheduleBtn);
        
        // Assemble the dialog
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(CARD_BG);
        centerPanel.add(descPanel, BorderLayout.NORTH);
        centerPanel.add(quickSelectPanel, BorderLayout.CENTER);
        centerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        serviceDialog.add(panel);
        serviceDialog.setVisible(true);
    }

    private void addVehicle() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Add New Vehicle", true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        panel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("Add New Vehicle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Form fields - removed Gasoline option
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Truck", "Van", "Car", "Motorcycle"});
        JTextField modelField = new JTextField();
        JTextField plateField = new JTextField();
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Electric", "Hybrid"});
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(new Date());
        
        // Style fields
        styleFormField(typeCombo);
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(fuelCombo);
        styleFormField(dateSpinner);
        
        int row = 0;
        addFormRow(formPanel, "Vehicle Type:", typeCombo, gbc, row++);
        addFormRow(formPanel, "Model:", modelField, gbc, row++);
        addFormRow(formPanel, "License Plate:", plateField, gbc, row++);
        addFormRow(formPanel, "Fuel Type:", fuelCombo, gbc, row++);
        addFormRow(formPanel, "Road Tax Expiry:", dateSpinner, gbc, row++);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setBackground(CARD_BG);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Add Vehicle");
        saveBtn.setBackground(SUCCESS);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(130, 40));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(TEXT_SECONDARY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(SUCCESS_DARK);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(SUCCESS);
            }
        });
        
        cancelBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelBtn.setBackground(new Color(90, 98, 104));
            }
            public void mouseExited(MouseEvent e) {
                cancelBtn.setBackground(TEXT_SECONDARY);
            }
        });
        
        saveBtn.addActionListener(e -> {
            // Validation
            if (modelField.getText().trim().isEmpty()) {
                showNotification("Please enter vehicle model", WARNING);
                return;
            }
            
            if (plateField.getText().trim().isEmpty()) {
                showNotification("Please enter license plate number", WARNING);
                return;
            }
            
            // Check for duplicate plate number
            String plateNumber = plateField.getText().trim().toUpperCase();
            boolean duplicate = vehicles.stream().anyMatch(v -> v.numberPlate.equalsIgnoreCase(plateNumber));
            if (duplicate) {
                showNotification("License plate number already exists!", DANGER);
                return;
            }
            
            // Generate vehicle ID
            String type = (String) typeCombo.getSelectedItem();
            String id = generateId(type);
            String model = modelField.getText().trim();
            Date taxExpiry = (Date) dateSpinner.getValue();
            String fuelType = (String) fuelCombo.getSelectedItem();
            
            // Create new vehicle
            Vehicle newVehicle = new Vehicle(id, model, "Active", null, type, plateNumber, taxExpiry, fuelType);
            vehicles.add(newVehicle);
            
            // Save to file
            saveData();
            
            // Refresh table
            refreshTable();
            
            // Show success message
            showNotification("Vehicle added successfully: " + id, SUCCESS);
            
            // Close dialog
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
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
            showNotification("Please select a vehicle to edit", WARNING);
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Edit Vehicle - " + v.id, true);
        dialog.setSize(550, 550);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        panel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("Edit Vehicle");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(WARNING);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel idLabel = new JLabel("ID: " + v.id);
        idLabel.setFont(REGULAR_FONT);
        idLabel.setForeground(TEXT_SECONDARY);
        idLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Form fields - removed Gasoline option
        JTextField modelField = new JTextField(v.model);
        JTextField plateField = new JTextField(v.numberPlate);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setSelectedItem(v.status);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(v.roadTaxExpiry);
        
        // Style fields
        styleFormField(modelField);
        styleFormField(plateField);
        styleFormField(statusCombo);
        styleFormField(dateSpinner);
        
        int rowIdx = 0;
        addFormRow(formPanel, "Model:", modelField, gbc, rowIdx++);
        addFormRow(formPanel, "License Plate:", plateField, gbc, rowIdx++);
        addFormRow(formPanel, "Status:", statusCombo, gbc, rowIdx++);
        addFormRow(formPanel, "Road Tax Expiry:", dateSpinner, gbc, rowIdx++);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(WARNING);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(130, 40));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(TEXT_SECONDARY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(WARNING_DARK);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(WARNING);
            }
        });
        
        cancelBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelBtn.setBackground(new Color(90, 98, 104));
            }
            public void mouseExited(MouseEvent e) {
                cancelBtn.setBackground(TEXT_SECONDARY);
            }
        });
        
        saveBtn.addActionListener(e -> {
            // Validation
            if (modelField.getText().trim().isEmpty()) {
                showNotification("Please enter vehicle model", WARNING);
                return;
            }
            
            if (plateField.getText().trim().isEmpty()) {
                showNotification("Please enter license plate number", WARNING);
                return;
            }
            
            // Check for duplicate plate number (excluding current vehicle)
            String plateNumber = plateField.getText().trim().toUpperCase();
            boolean duplicate = vehicles.stream()
                .anyMatch(vehicle -> !vehicle.id.equals(v.id) && vehicle.numberPlate.equalsIgnoreCase(plateNumber));
            if (duplicate) {
                showNotification("License plate number already exists!", DANGER);
                return;
            }
            
            // Update vehicle
            v.model = modelField.getText().trim();
            v.numberPlate = plateNumber;
            v.status = (String) statusCombo.getSelectedItem();
            v.roadTaxExpiry = (Date) dateSpinner.getValue();
            
            // Save to file
            saveData();
            
            // Refresh table
            refreshTable();
            
            // Show success message
            showNotification("Vehicle updated successfully", SUCCESS);
            
            // Close dialog
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(CARD_BG);
        northPanel.add(titleLabel, BorderLayout.NORTH);
        northPanel.add(idLabel, BorderLayout.SOUTH);
        
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) {
            showNotification("Please select a vehicle to delete", WARNING);
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= vehicles.size()) return;
        
        Vehicle v = vehicles.get(modelRow);
        
        // Check if vehicle has pending reports
        boolean hasPendingReports = driverReports.stream()
            .anyMatch(r -> r.vehicleId.equals(v.id) && "Pending".equals(r.status));
        
        String message = "Delete " + v.id + " (" + v.model + ")?\n\n";
        if (hasPendingReports) {
            message += "This vehicle has pending driver reports!\n";
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel, message,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            hasPendingReports ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Unassign driver if any
            String driverId = getDriverIdForVehicle(v.id);
            if (driverId != null) {
                updateDriverVehicleAssignment(driverId, null);
            }
            
            // Remove vehicle
            vehicles.remove(modelRow);
            
            // Save to file
            saveData();
            
            // Refresh table
            refreshTable();
            
            // Show success message
            showNotification("Vehicle deleted successfully", SUCCESS);
        }
    }

    private void showDriverReportsDialog() {
        if (driverReports.isEmpty()) {
            showNotification("No driver reports available", INFO);
            return;
        }
        
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
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(CARD_BG);
        
        if (!pendingReports.isEmpty()) {
            JPanel pendingPanel = createReportsListPanel(pendingReports, true);
            tabbedPane.addTab("Pending (" + pendingReports.size() + ")", pendingPanel);
        }
        
        if (!otherReports.isEmpty() || pendingReports.isEmpty()) {
            JPanel allPanel = createReportsListPanel(driverReports, false);
            tabbedPane.addTab("All Reports (" + driverReports.size() + ")", allPanel);
        }
        
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

    private JPanel createReportsListPanel(List<DriverReport> reports, boolean showPendingActions) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (DriverReport r : reports) {
            String severityIcon = r.severity.equals("Critical") ? "CRITICAL" :
                                 r.severity.equals("High") ? "HIGH" :
                                 r.severity.equals("Medium") ? "MEDIUM" : "LOW";
            
            String statusIcon = r.status.equals("Pending") ? "PENDING" :
                               r.status.equals("Scheduled") ? "SCHEDULED" :
                               r.status.equals("Resolved") ? "RESOLVED" : "POSTPONED";
            
            String driverDisplay = getDriverDisplayInfo(r.driverName);
            
            String reportLine = String.format("[%s] [%s] | %s | %s | %s: %s", 
                severityIcon, statusIcon,
                r.reportId, 
                r.vehicleId, 
                driverDisplay,
                truncateText(r.description, 50));
            
            listModel.addElement(reportLine);
        }
        
        JList<String> reportsList = new JList<>(listModel);
        reportsList.setFont(REGULAR_FONT);
        reportsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        reportsList.setFixedCellHeight(40);
        
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
            String severityIcon = r.severity.equals("Critical") ? "CRITICAL" :
                                 r.severity.equals("High") ? "HIGH" :
                                 r.severity.equals("Medium") ? "MEDIUM" : "LOW";
            
            String statusIcon = r.status.equals("Pending") ? "PENDING" :
                               r.status.equals("Scheduled") ? "SCHEDULED" :
                               r.status.equals("Resolved") ? "RESOLVED" : "POSTPONED";
            
            String driverDisplay = getDriverDisplayInfo(r.driverName);
            
            listModel.addElement(String.format("[%s] [%s] | %s | %s: %s", 
                severityIcon, statusIcon, r.reportId, driverDisplay, 
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
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel severityLabel = new JLabel(report.severity + " Severity");
        severityLabel.setFont(HEADER_FONT);
        severityLabel.setForeground(getSeverityColor(report.severity));
        
        JLabel statusLabel = new JLabel("Status: " + report.status);
        statusLabel.setFont(REGULAR_FONT);
        statusLabel.setForeground(getStatusColor(report.status));
        
        headerPanel.add(severityLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        contentPanel.add(createDetailRow("Report ID:", report.reportId));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Vehicle:", report.vehicleId));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createDetailRow("Driver:", getDriverDisplayInfo(report.driverName)));
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

    private void showScheduleMaintenanceDialog(DriverReport report) {
        String[] options = {"Schedule Now", "Schedule for Later", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainPanel,
            "Schedule maintenance for " + report.vehicleId + "?\n\n" +
            "Report: " + report.description,
            "Schedule Maintenance",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) {
            Vehicle vehicle = findVehicle(report.vehicleId).orElse(null);
            if (vehicle != null) {
                if ("Maintenance".equals(vehicle.status)) {
                    JOptionPane.showMessageDialog(mainPanel,
                        "Vehicle is already in maintenance. Please complete it in Maintenance Management.",
                        "Already in Maintenance",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                String driverId = getDriverIdForVehicle(vehicle.id);
                if (driverId != null) {
                    updateDriverVehicleAssignment(driverId, null);
                }
                
                vehicle.status = "Maintenance";
                
                if (maintenanceManagement != null) {
                    String maintenanceDesc = String.format("[DRIVER REPORT #%s - %s] %s (Severity: %s)", 
                        report.reportId, 
                        getDriverDisplayInfo(report.driverName), 
                        report.description,
                        report.severity);
                    
                    maintenanceManagement.addVehicleToMaintenance(
                        vehicle.id, 
                        vehicle.model, 
                        maintenanceDesc
                    );
                    
                    report.status = "Scheduled";
                    report.adminNotes = "Scheduled for maintenance on " + 
                        displayDateFormat.format(new Date());
                    saveDriverReports();
                    
                    maintenanceManagement.refreshData();
                    
                    showNotification("Maintenance scheduled for " + report.vehicleId, SUCCESS);
                } else {
                    report.status = "Scheduled";
                    report.adminNotes = "Marked for maintenance (no maintenance module)";
                    saveDriverReports();
                    showNotification("Report marked for maintenance", INFO);
                }
                
                saveData();
                refreshTable();
            }
        } else if (choice == 1) {
            JPanel datePanel = new JPanel(new GridBagLayout());
            datePanel.setBackground(CARD_BG);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            JLabel instructionLabel = new JLabel("Select scheduled date:");
            instructionLabel.setFont(REGULAR_FONT);
            
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
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
                report.status = "Scheduled";
                report.adminNotes = "Scheduled for " + displayDateFormat.format(scheduledDate);
                saveDriverReports();
                showNotification("Report scheduled for " + displayDateFormat.format(scheduledDate), INFO);
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

    private void deleteReport(DriverReport report) {
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Delete report #" + report.reportId + "?\n\n" +
            "Vehicle: " + report.vehicleId + "\n" +
            "Driver: " + getDriverDisplayInfo(report.driverName) + "\n" +
            "Description: " + truncateText(report.description, 50),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            driverReports.remove(report);
            saveDriverReports();
            updateStats();
            refreshTable();
            showNotification("Report deleted", SUCCESS);
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
        field.setPreferredSize(new Dimension(280, 38));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            ((JComboBox<?>) field).setBackground(CARD_BG);
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
        saveData();
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

    private void refreshTable() {
        loadDriversFromFile();
        
        tableModel.setRowCount(0);
        for (Vehicle v : vehicles) {
            String driverId = getDriverIdForVehicle(v.id);
            String driverDisplay = driverId != null ? getDriverDisplayInfo(driverCache.get(driverId).name) : "Unassigned";
            
            tableModel.addRow(new Object[]{
                v.id, v.model, v.type, v.numberPlate, v.roadTaxExpiry, 
                v.fuelType, v.status, driverDisplay
            });
        }
        updateStats();
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            if (statValues[0] != null) statValues[0].setText(String.valueOf(getTotalCount()));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(getActiveCount()));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(getMaintenanceCount()));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(getExpiredRoadTaxCount()));
            if (statValues[4] != null) {
                loadDriversFromFile();
                int availableCount = 0;
                for (DriverData d : driverCache.values()) {
                    if ("Available".equals(d.workStatus) && "APPROVED".equals(d.approvalStatus) && 
                        (d.vehicleId == null || d.vehicleId.isEmpty())) {
                        availableCount++;
                    }
                }
                statValues[4].setText(String.valueOf(availableCount));
            }
            if (statValues[5] != null) statValues[5].setText(String.valueOf(getTotalReportsCount()));
        });
    }

    private void showVehicleDetails(Vehicle v) {
        boolean taxExpired = v.roadTaxExpiry.before(new Date());
        long reportCount = driverReports.stream()
            .filter(r -> r.vehicleId.equals(v.id) && "Pending".equals(r.status))
            .count();
        
        String driverId = getDriverIdForVehicle(v.id);
        String driverStatus = "";
        if (driverId != null) {
            DriverData driver = driverCache.get(driverId);
            if (driver != null) {
                driverStatus = " (" + driver.approvalStatus + " - " + driver.workStatus + ")";
            }
        }
        
        final String finalDriverId = driverId;
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Vehicle Details", true);
        dialog.setSize(450, 550);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BG);
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setBackground(CARD_BG);
        
        String icon = v.type.equals("Truck") ? "Truck" : 
                     v.type.equals("Van") ? "Van" : 
                     v.type.equals("Car") ? "Car" : "Motorcycle";
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
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
        
        String driverName = driverId != null ? driverCache.get(driverId).name : "Unassigned";
        JPanel driverPanel = createDetailRow("Driver:", driverName + driverStatus);
        if (finalDriverId != null && driverProfileListener != null) {
            driverPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            driverPanel.setToolTipText("Click to view driver details");
            driverPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    dialog.dispose();
                    driverProfileListener.onDriverProfileClicked(finalDriverId);
                }
            });
        }
        contentPanel.add(driverPanel);
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
        loadDriversFromFile();
        refreshTable();
        return mainPanel;
    }
    
    public void refreshData() {
        vehicles.clear();
        typeCounters.clear();
        driverReports.clear();
        loadData();
        loadDriverReports();
        loadDriversFromFile();
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
        loadDriversFromFile();
        int count = 0;
        for (DriverData d : driverCache.values()) {
            if ("Available".equals(d.workStatus) && "APPROVED".equals(d.approvalStatus) && 
                (d.vehicleId == null || d.vehicleId.isEmpty())) {
                count++;
            }
        }
        return count;
    }
    
    public int getTotalReportsCount() {
        return driverReports.size();
    }
    
    public int getPendingReportsCount() {
        return (int) driverReports.stream().filter(r -> "Pending".equals(r.status)).count();
    }
    
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    // ==================== INTEGRATION METHODS ====================
    
    public Vehicle getVehicleById(String id) {
        return findVehicle(id).orElse(null);
    }
    
    public void updateVehicleStatus(String vehicleId, String newStatus) {
        findVehicle(vehicleId).ifPresent(v -> {
            v.status = newStatus;
            saveData();
            refreshTable();
        });
    }
    
    public boolean assignDriverToVehicle(String driverId, String vehicleId) {
        Optional<Vehicle> vehicleOpt = findVehicle(vehicleId);
        DriverData driver = driverCache.get(driverId);
        
        if (vehicleOpt.isPresent() && driver != null) {
            Vehicle v = vehicleOpt.get();
            
            if ("Maintenance".equals(v.status)) {
                return false;
            }
            
            String currentDriverId = getDriverIdForVehicle(vehicleId);
            if (currentDriverId != null) {
                updateDriverVehicleAssignment(currentDriverId, null);
            }
            
            updateDriverVehicleAssignment(driverId, vehicleId);
            v.status = "Active";
            saveData();
            refreshTable();
            return true;
        }
        return false;
    }
    
    public boolean isVehicleInMaintenance(String vehicleId) {
        return findVehicle(vehicleId)
            .map(v -> "Maintenance".equals(v.status))
            .orElse(false);
    }
    
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
    
    public void addDriverReport(String vehicleId, String driverName, String description, String severity) {
        String reportId = "RPT" + String.format("%04d", driverReports.size() + 1);
        DriverReport report = new DriverReport(
            reportId, vehicleId, driverName, new Date(),
            description, severity, "Pending", ""
        );
        driverReports.add(report);
        saveDriverReports();
        refreshTable();
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
    
    private static class DriverData {
        String id, name, phone, email, licenseNumber, licenseExpiry, workStatus, approvalStatus, vehicleId;
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