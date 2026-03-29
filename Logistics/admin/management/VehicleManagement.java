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
    
    private static final String VEHICLES_FILE = "vehicles.txt";
    private static final String COUNTERS_FILE = "counters_data.txt";
    private static final String REPORTS_FILE = "vehicleReports.txt";
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
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    // Driver data cache
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
    
    private void loadDriversFromFile() {
        driverCache.clear();
        File file = new File(DRIVERS_FILE);
        if (!file.exists()) {
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
    
    private void updateDriverVehicleAssignment(String driverId, String vehicleId) {
        DriverData driver = driverCache.get(driverId);
        if (driver == null) return;
        
        driver.vehicleId = vehicleId;
        if (vehicleId != null && !vehicleId.isEmpty()) {
            driver.workStatus = "On Delivery";
        } else {
            driver.workStatus = "Available";
        }
        
        rewriteDriversFile();
        
        if (vehicleId != null) {
            Optional<Vehicle> vehicleOpt = findVehicle(vehicleId);
            if (vehicleOpt.isPresent()) {
                Vehicle v = vehicleOpt.get();
                v.status = "Active";
                saveData();
            }
        } else {
            for (Vehicle v : vehicles) {
                String currentDriverId = getDriverIdForVehicle(v.id);
                if (driverId.equals(currentDriverId)) {
                    v.driverName = null;
                    saveData();
                    break;
                }
            }
        }
        
        loadDriversFromFile();
        refreshTable();
        updateStats();
    }
    
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
            return driver.name;
        } else if ("PENDING".equals(driver.approvalStatus)) {
            return driver.name + " (Pending)";
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
        if (!file.exists()) {
            System.out.println("Reports file not found: " + REPORTS_FILE);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Skip header line
                if (firstLine && (line.contains("REPORT_ID") || line.startsWith("==="))) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                
                // Skip comment lines
                if (line.startsWith("//")) continue;
                
                parseDriverReport(line);
            }
            System.out.println("Loaded " + driverReports.size() + " driver reports");
        } catch (IOException e) {
            System.err.println("Error loading driver reports: " + e.getMessage());
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
            } else {
                System.err.println("Invalid report line: " + line);
            }
        } catch (Exception e) {
            System.err.println("Error parsing driver report: " + e.getMessage());
        }
    }

    private void loadFromFile(String filename, LineProcessor processor) {
        File file = new File(filename);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(processor::process);
        } catch (IOException e) {
            showError("Error loading " + filename);
        }
    }

    private interface LineProcessor {
        void process(String line);
    }

    private void parseVehicle(String line) {
        try {
            if (line.startsWith("VEHICLE_ID") || line.contains("VEHICLE_ID|VEHICLE_TYPE")) {
                return;
            }
            
            if (line.trim().isEmpty()) {
                return;
            }
            
            String[] p = line.split("\\|");
            if (p.length >= 8) {
                String id = p[0].trim();
                String type = p[1].trim();
                String numberPlate = p[2].trim();
                String model = p[3].trim();
                String status = p[5].trim();
                String assignedTo = p[6].trim();
                String fuelType = p[7].trim();
                
                long roadTaxTimestamp;
                try {
                    roadTaxTimestamp = Long.parseLong(p[4].trim());
                } catch (NumberFormatException e) {
                    roadTaxTimestamp = System.currentTimeMillis();
                }
                Date roadTaxExpiry = new Date(roadTaxTimestamp);
                
                String driverName = ("Unassigned".equals(assignedTo) || assignedTo.isEmpty()) ? null : assignedTo;
                
                vehicles.add(new Vehicle(
                    id, model, status, driverName, type, 
                    numberPlate, roadTaxExpiry, fuelType
                ));
                
                typeCounters.put(type, typeCounters.getOrDefault(type, 0) + 1);
            }
        } catch (Exception e) { 
            System.err.println("Error parsing vehicle: " + line + " - " + e.getMessage()); 
        }
    }

    private void saveData() {
        saveToFile(VEHICLES_FILE, vehicles, v -> String.format("%s|%s|%s|%s|%d|%s|%s|%s",
            v.id, v.type, v.numberPlate, v.model, v.roadTaxExpiry.getTime(),
            v.status, v.driverName != null ? v.driverName : "Unassigned", v.fuelType));
        
        saveToFile(COUNTERS_FILE, typeCounters.entrySet(), 
            e -> e.getKey() + "=" + e.getValue());
        
        saveDriverReports();
    }

    private <T> void saveToFile(String filename, Collection<T> data, DataFormatter<T> formatter) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            if (filename.equals(VEHICLES_FILE)) {
                writer.write("VEHICLE_ID|VEHICLE_TYPE|LICENSE_PLATE|VEHICLE_MODEL|ROAD_TAX_EXPIRY|STATUS|ASSIGNED_TO|FUEL_TYPE");
                writer.newLine();
            }
            
            if (filename.equals(REPORTS_FILE)) {
                writer.write("REPORT_ID|VEHICLE_ID|DRIVER_NAME|REPORT_DATE|DESCRIPTION|SEVERITY|STATUS|ADMIN_NOTES");
                writer.newLine();
            }
            
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
                    String assignedTo = vehicles.get(modelRow).driverName;
                    if (assignedTo != null && !assignedTo.equals("Unassigned")) {
                        setText(assignedTo);
                        setForeground(TEXT_PRIMARY);
                    } else {
                        setText("Unassigned");
                        setForeground(TEXT_MUTED);
                        setFont(getFont().deriveFont(Font.ITALIC));
                    }
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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panel.setBackground(BG_COLOR);
        
        JButton addBtn = createStyledButton("Add", SUCCESS, SUCCESS_DARK);
        JButton editBtn = createStyledButton("Edit", WARNING, WARNING_DARK);
        JButton deleteBtn = createStyledButton("Delete", DANGER, DANGER_DARK);
        JButton driverBtn = createStyledButton("Driver", PURPLE, PURPLE_DARK);
        JButton serviceBtn = createStyledButton("Service", INFO, INFO_DARK);
        JButton reportsBtn = createStyledButton("Reports", PRIMARY, PRIMARY_DARK);
        
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
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Driver Management - " + v.id, true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
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

    private void assignDriver(Vehicle vehicle) {
        if (vehicle.status.equals("Maintenance")) {
            showNotification("Cannot assign to vehicle in maintenance", WARNING);
            return;
        }
        
        loadDriversFromFile();
        
        List<DriverData> availableDrivers = new ArrayList<>();
        for (DriverData d : driverCache.values()) {
            if ("APPROVED".equals(d.approvalStatus) && 
                "Available".equals(d.workStatus) && 
                (d.vehicleId == null || d.vehicleId.isEmpty())) {
                availableDrivers.add(d);
            }
        }
        
        if (availableDrivers.isEmpty()) {
            showNotification("No available drivers to assign.", WARNING);
            return;
        }
        
        String[] driverOptions = new String[availableDrivers.size()];
        for (int i = 0; i < availableDrivers.size(); i++) {
            DriverData d = availableDrivers.get(i);
            driverOptions[i] = String.format("%s (ID: %s) - %s | %s", 
                d.name, d.id, d.licenseNumber, d.phone);
        }
        
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
                
                String existingDriverId = getDriverIdForVehicle(vehicle.id);
                if (existingDriverId != null) {
                    updateDriverVehicleAssignment(existingDriverId, null);
                }
                
                updateDriverVehicleAssignment(selectedDriver.id, vehicle.id);
                
                vehicle.status = "Active";
                vehicle.driverName = selectedDriver.name;
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
        
        if (v.status.equals("Maintenance")) {
            showNotification("Vehicle is already in maintenance.", INFO);
            return;
        }
        
        String description = JOptionPane.showInputDialog(mainPanel,
            "Enter maintenance description for " + v.id + ":",
            "Schedule Maintenance",
            JOptionPane.QUESTION_MESSAGE);
        
        if (description == null || description.trim().isEmpty()) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Schedule maintenance for " + v.id + "?\n\n" +
            "Description: " + description + "\n\n" +
            (driverId != null ? "Driver will be unassigned.\n" : ""),
            "Confirm Maintenance",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (driverId != null) {
                updateDriverVehicleAssignment(driverId, null);
            }
            
            if (maintenanceManagement != null) {
                maintenanceManagement.addVehicleToMaintenance(v.id, v.model, description);
            }
            
            v.status = "Maintenance";
            saveData();
            refreshTable();
            showNotification("Maintenance scheduled for " + v.id, SUCCESS);
        }
    }

    private void addVehicle() {
        JTextField modelField = new JTextField();
        JTextField plateField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Truck", "Van", "Car", "Motorcycle"});
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Diesel", "Electric", "Hybrid"});
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(new Date());
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Model:"));
        panel.add(modelField);
        panel.add(new JLabel("License Plate:"));
        panel.add(plateField);
        panel.add(new JLabel("Fuel Type:"));
        panel.add(fuelCombo);
        panel.add(new JLabel("Road Tax Expiry:"));
        panel.add(dateSpinner);
        
        int result = JOptionPane.showConfirmDialog(mainPanel, panel, 
            "Add New Vehicle", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            if (modelField.getText().trim().isEmpty()) {
                showNotification("Please enter vehicle model", WARNING);
                return;
            }
            if (plateField.getText().trim().isEmpty()) {
                showNotification("Please enter license plate", WARNING);
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            String id = generateId(type);
            String model = modelField.getText().trim();
            String plate = plateField.getText().trim().toUpperCase();
            String fuel = (String) fuelCombo.getSelectedItem();
            Date taxExpiry = (Date) dateSpinner.getValue();
            
            Vehicle newVehicle = new Vehicle(id, model, "Active", null, type, plate, taxExpiry, fuel);
            vehicles.add(newVehicle);
            saveData();
            refreshTable();
            showNotification("Vehicle added: " + id, SUCCESS);
        }
    }
    
    private void editVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) {
            showNotification("Please select a vehicle", WARNING);
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        Vehicle v = vehicles.get(modelRow);
        
        JTextField modelField = new JTextField(v.model);
        JTextField plateField = new JTextField(v.numberPlate);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance"});
        statusCombo.setSelectedItem(v.status);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(v.roadTaxExpiry);
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Model:"));
        panel.add(modelField);
        panel.add(new JLabel("License Plate:"));
        panel.add(plateField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Road Tax Expiry:"));
        panel.add(dateSpinner);
        
        int result = JOptionPane.showConfirmDialog(mainPanel, panel, 
            "Edit Vehicle - " + v.id, JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            if (modelField.getText().trim().isEmpty()) {
                showNotification("Please enter vehicle model", WARNING);
                return;
            }
            if (plateField.getText().trim().isEmpty()) {
                showNotification("Please enter license plate", WARNING);
                return;
            }
            
            v.model = modelField.getText().trim();
            v.numberPlate = plateField.getText().trim().toUpperCase();
            v.status = (String) statusCombo.getSelectedItem();
            v.roadTaxExpiry = (Date) dateSpinner.getValue();
            
            saveData();
            refreshTable();
            showNotification("Vehicle updated", SUCCESS);
        }
    }
    
    private void deleteVehicle() {
        int row = vehiclesTable.getSelectedRow();
        if (row == -1) {
            showNotification("Please select a vehicle", WARNING);
            return;
        }
        
        int modelRow = vehiclesTable.convertRowIndexToModel(row);
        Vehicle v = vehicles.get(modelRow);
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Delete " + v.id + " (" + v.model + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String driverId = getDriverIdForVehicle(v.id);
            if (driverId != null) {
                updateDriverVehicleAssignment(driverId, null);
            }
            
            vehicles.remove(modelRow);
            saveData();
            refreshTable();
            showNotification("Vehicle deleted", SUCCESS);
        }
    }

    // ==================== IMPROVED DRIVER REPORTS SECTION ====================

    private void showDriverReportsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Driver Reports Management", true);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        // Header
        JLabel titleLabel = new JLabel("Driver Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY);
        
        JLabel subtitleLabel = new JLabel("View, manage, and take action on driver-submitted reports");
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create tabs for pending and all reports
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        
        // Filter reports
        List<DriverReport> pendingReports = driverReports.stream()
            .filter(r -> "Pending".equals(r.status))
            .collect(Collectors.toList());
        
        List<DriverReport> scheduledReports = driverReports.stream()
            .filter(r -> "Scheduled".equals(r.status))
            .collect(Collectors.toList());
        
        List<DriverReport> resolvedReports = driverReports.stream()
            .filter(r -> "Resolved".equals(r.status))
            .collect(Collectors.toList());
        
        List<DriverReport> postponedReports = driverReports.stream()
            .filter(r -> "Postponed".equals(r.status))
            .collect(Collectors.toList());
        
        // Create panels for each tab
        if (!pendingReports.isEmpty()) {
            tabbedPane.addTab("⚠️ Pending (" + pendingReports.size() + ")", 
                createReportTablePanel(pendingReports, true));
        }
        
        if (!scheduledReports.isEmpty()) {
            tabbedPane.addTab("📅 Scheduled (" + scheduledReports.size() + ")", 
                createReportTablePanel(scheduledReports, false));
        }
        
        if (!postponedReports.isEmpty()) {
            tabbedPane.addTab("⏰ Postponed (" + postponedReports.size() + ")", 
                createReportTablePanel(postponedReports, false));
        }
        
        if (!resolvedReports.isEmpty()) {
            tabbedPane.addTab("✅ Resolved (" + resolvedReports.size() + ")", 
                createReportTablePanel(resolvedReports, false));
        }
        
        if (driverReports.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(CARD_BG);
            JLabel emptyLabel = new JLabel("No driver reports available");
            emptyLabel.setFont(REGULAR_FONT);
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            tabbedPane.addTab("All Reports (0)", emptyPanel);
        } else if (pendingReports.isEmpty() && scheduledReports.isEmpty() && 
                   postponedReports.isEmpty() && resolvedReports.isEmpty()) {
            // Show all reports if no filtered categories
            tabbedPane.addTab("All Reports (" + driverReports.size() + ")", 
                createReportTablePanel(driverReports, false));
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(BUTTON_FONT);
        refreshBtn.setBackground(INFO);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorderPainted(false);
        refreshBtn.addActionListener(e -> {
            loadDriverReports();
            dialog.dispose();
            showDriverReportsDialog();
        });
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createReportTablePanel(List<DriverReport> reports, boolean showActions) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table model
        String[] columns = {"Report ID", "Vehicle", "Driver", "Date", "Severity", "Status", "Description"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        // Custom renderer for severity and status
        JTable reportTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String severity = (String) getValueAt(row, 4);
                    if ("Critical".equals(severity)) {
                        comp.setBackground(new Color(255, 235, 238));
                    } else if ("High".equals(severity)) {
                        comp.setBackground(new Color(255, 243, 224));
                    } else if ("Medium".equals(severity)) {
                        comp.setBackground(new Color(232, 245, 233));
                    } else {
                        comp.setBackground(row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG);
                    }
                }
                return comp;
            }
        };
        
        // Add data to table
        for (DriverReport r : reports) {
            tableModel.addRow(new Object[]{
                r.reportId,
                r.vehicleId,
                getDriverDisplayInfo(r.driverName),
                displayDateFormat.format(r.reportDate),
                r.severity,
                r.status,
                r.description.length() > 60 ? r.description.substring(0, 60) + "..." : r.description
            });
        }
        
        reportTable.setFont(REGULAR_FONT);
        reportTable.setRowHeight(35);
        reportTable.getTableHeader().setFont(HEADER_FONT);
        reportTable.getTableHeader().setBackground(new Color(245, 245, 245));
        reportTable.setSelectionBackground(SELECTION_COLOR);
        
        // Set column widths
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        reportTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        reportTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        reportTable.getColumnModel().getColumn(6).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(CARD_BG);
        
        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(BUTTON_FONT);
        viewBtn.setBackground(INFO);
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBorderPainted(false);
        viewBtn.addActionListener(e -> {
            int row = reportTable.getSelectedRow();
            if (row >= 0) {
                showReportDetailsDialog(reports.get(row));
            } else {
                showNotification("Please select a report", WARNING);
            }
        });
        
        actionPanel.add(viewBtn);
        
        if (showActions) {
            JButton scheduleBtn = new JButton("Schedule Maintenance");
            scheduleBtn.setFont(BUTTON_FONT);
            scheduleBtn.setBackground(SUCCESS);
            scheduleBtn.setForeground(Color.WHITE);
            scheduleBtn.setBorderPainted(false);
            scheduleBtn.addActionListener(e -> {
                int row = reportTable.getSelectedRow();
                if (row >= 0) {
                    scheduleMaintenanceFromReport(reports.get(row));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            JButton postponeBtn = new JButton("Postpone");
            postponeBtn.setFont(BUTTON_FONT);
            postponeBtn.setBackground(WARNING);
            postponeBtn.setForeground(Color.WHITE);
            postponeBtn.setBorderPainted(false);
            postponeBtn.addActionListener(e -> {
                int row = reportTable.getSelectedRow();
                if (row >= 0) {
                    postponeReportDialog(reports.get(row));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            JButton resolveBtn = new JButton("Mark Resolved");
            resolveBtn.setFont(BUTTON_FONT);
            resolveBtn.setBackground(PURPLE);
            resolveBtn.setForeground(Color.WHITE);
            resolveBtn.setBorderPainted(false);
            resolveBtn.addActionListener(e -> {
                int row = reportTable.getSelectedRow();
                if (row >= 0) {
                    markReportResolved(reports.get(row));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(BUTTON_FONT);
            deleteBtn.setBackground(DANGER);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorderPainted(false);
            deleteBtn.addActionListener(e -> {
                int row = reportTable.getSelectedRow();
                if (row >= 0) {
                    deleteReportDialog(reports.get(row));
                } else {
                    showNotification("Please select a report", WARNING);
                }
            });
            
            actionPanel.add(scheduleBtn);
            actionPanel.add(postponeBtn);
            actionPanel.add(resolveBtn);
            actionPanel.add(deleteBtn);
        } else {
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(BUTTON_FONT);
            deleteBtn.setBackground(DANGER);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorderPainted(false);
            deleteBtn.addActionListener(e -> {
                int row = reportTable.getSelectedRow();
                if (row >= 0) {
                    deleteReportDialog(reports.get(row));
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
    
    private void showReportDetailsDialog(DriverReport report) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
            "Report Details - " + report.reportId, true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        // Header with severity color
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(getSeverityColor(report.severity));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel severityLabel = new JLabel(report.severity.toUpperCase() + " SEVERITY REPORT");
        severityLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        severityLabel.setForeground(Color.WHITE);
        
        JLabel idLabel = new JLabel("ID: " + report.reportId);
        idLabel.setFont(REGULAR_FONT);
        idLabel.setForeground(Color.WHITE);
        
        headerPanel.add(severityLabel, BorderLayout.WEST);
        headerPanel.add(idLabel, BorderLayout.EAST);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Vehicle and driver info
        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        infoGrid.setBackground(CARD_BG);
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        infoGrid.add(createInfoLabel("Vehicle ID:"));
        infoGrid.add(new JLabel(report.vehicleId));
        infoGrid.add(createInfoLabel("Driver:"));
        infoGrid.add(new JLabel(getDriverDisplayInfo(report.driverName)));
        infoGrid.add(createInfoLabel("Report Date:"));
        infoGrid.add(new JLabel(dateTimeFormat.format(report.reportDate)));
        infoGrid.add(createInfoLabel("Status:"));
        JLabel statusLabel = new JLabel(report.status);
        statusLabel.setForeground(getStatusColor(report.status));
        statusLabel.setFont(STATUS_FONT);
        infoGrid.add(statusLabel);
        
        contentPanel.add(infoGrid);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Description
        JLabel descTitle = new JLabel("Description:");
        descTitle.setFont(HEADER_FONT);
        descTitle.setForeground(TEXT_PRIMARY);
        contentPanel.add(descTitle);
        contentPanel.add(Box.createVerticalStrut(5));
        
        JTextArea descArea = new JTextArea(report.description);
        descArea.setFont(REGULAR_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 249, 250));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        descArea.setPreferredSize(new Dimension(450, 100));
        contentPanel.add(descArea);
        
        // Admin notes
        if (report.adminNotes != null && !report.adminNotes.isEmpty()) {
            contentPanel.add(Box.createVerticalStrut(10));
            JLabel notesTitle = new JLabel("Admin Notes:");
            notesTitle.setFont(HEADER_FONT);
            notesTitle.setForeground(TEXT_PRIMARY);
            contentPanel.add(notesTitle);
            contentPanel.add(Box.createVerticalStrut(5));
            
            JTextArea notesArea = new JTextArea(report.adminNotes);
            notesArea.setFont(REGULAR_FONT);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            notesArea.setEditable(false);
            notesArea.setBackground(new Color(248, 249, 250));
            notesArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            contentPanel.add(notesArea);
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        if ("Pending".equals(report.status)) {
            JButton scheduleBtn = new JButton("Schedule Maintenance");
            scheduleBtn.setBackground(SUCCESS);
            scheduleBtn.setForeground(Color.WHITE);
            scheduleBtn.setFont(BUTTON_FONT);
            scheduleBtn.setBorderPainted(false);
            scheduleBtn.addActionListener(e -> {
                dialog.dispose();
                scheduleMaintenanceFromReport(report);
            });
            
            JButton postponeBtn = new JButton("Postpone");
            postponeBtn.setBackground(WARNING);
            postponeBtn.setForeground(Color.WHITE);
            postponeBtn.setFont(BUTTON_FONT);
            postponeBtn.setBorderPainted(false);
            postponeBtn.addActionListener(e -> {
                dialog.dispose();
                postponeReportDialog(report);
            });
            
            JButton resolveBtn = new JButton("Mark Resolved");
            resolveBtn.setBackground(PURPLE);
            resolveBtn.setForeground(Color.WHITE);
            resolveBtn.setFont(BUTTON_FONT);
            resolveBtn.setBorderPainted(false);
            resolveBtn.addActionListener(e -> {
                dialog.dispose();
                markReportResolved(report);
            });
            
            buttonPanel.add(scheduleBtn);
            buttonPanel.add(postponeBtn);
            buttonPanel.add(resolveBtn);
        }
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(PRIMARY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }
    
    private void scheduleMaintenanceFromReport(DriverReport report) {
        Vehicle vehicle = findVehicle(report.vehicleId).orElse(null);
        if (vehicle == null) {
            showNotification("Vehicle not found: " + report.vehicleId, DANGER);
            return;
        }
        
        if ("Maintenance".equals(vehicle.status)) {
            showNotification("Vehicle is already in maintenance", WARNING);
            return;
        }
        
        String maintenanceDesc = String.format("[Report: %s] %s - %s", 
            report.reportId, report.severity, report.description);
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Schedule maintenance for " + vehicle.id + " (" + vehicle.model + ")?\n\n" +
            "Based on report: " + report.description + "\n\n" +
            "The vehicle will be marked as 'Maintenance' and the driver will be unassigned.",
            "Schedule Maintenance",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Unassign driver
            String driverId = getDriverIdForVehicle(vehicle.id);
            if (driverId != null) {
                updateDriverVehicleAssignment(driverId, null);
            }
            
            // Add to maintenance
            if (maintenanceManagement != null) {
                maintenanceManagement.addVehicleToMaintenance(vehicle.id, vehicle.model, maintenanceDesc);
            }
            
            // Update vehicle status
            vehicle.status = "Maintenance";
            
            // Update report
            report.status = "Scheduled";
            report.adminNotes = "Maintenance scheduled on " + displayDateFormat.format(new Date());
            
            saveData();
            saveDriverReports();
            refreshTable();
            updateStats();
            
            showNotification("Maintenance scheduled for " + vehicle.id, SUCCESS);
        }
    }
    
    private void postponeReportDialog(DriverReport report) {
        String[] options = {"1 Day", "3 Days", "1 Week", "2 Weeks", "1 Month", "Custom"};
        int choice = JOptionPane.showOptionDialog(mainPanel,
            "Postpone report for how long?\n\n" +
            "Report: " + report.description,
            "Postpone Report",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        
        Calendar cal = Calendar.getInstance();
        String postponeDuration = "";
        
        switch (choice) {
            case 0:
                cal.add(Calendar.DAY_OF_MONTH, 1);
                postponeDuration = "1 day";
                break;
            case 1:
                cal.add(Calendar.DAY_OF_MONTH, 3);
                postponeDuration = "3 days";
                break;
            case 2:
                cal.add(Calendar.DAY_OF_MONTH, 7);
                postponeDuration = "1 week";
                break;
            case 3:
                cal.add(Calendar.DAY_OF_MONTH, 14);
                postponeDuration = "2 weeks";
                break;
            case 4:
                cal.add(Calendar.MONTH, 1);
                postponeDuration = "1 month";
                break;
            case 5:
                String dateStr = JOptionPane.showInputDialog(mainPanel, 
                    "Enter new date (yyyy-MM-dd):", 
                    displayDateFormat.format(new Date()));
                if (dateStr == null || dateStr.trim().isEmpty()) return;
                try {
                    cal.setTime(dateFormat.parse(dateStr));
                    postponeDuration = "until " + displayDateFormat.format(cal.getTime());
                } catch (Exception ex) {
                    showNotification("Invalid date format", DANGER);
                    return;
                }
                break;
            default:
                return;
        }
        
        report.status = "Postponed";
        report.adminNotes = "Postponed " + postponeDuration + " on " + displayDateFormat.format(new Date());
        saveDriverReports();
        refreshTable();
        updateStats();
        
        showNotification("Report postponed " + postponeDuration, INFO);
    }
    
    private void markReportResolved(DriverReport report) {
        String resolution = JOptionPane.showInputDialog(mainPanel,
            "Enter resolution notes for this report:",
            "Mark as Resolved",
            JOptionPane.QUESTION_MESSAGE);
        
        if (resolution == null) return;
        
        report.status = "Resolved";
        if (report.adminNotes != null && !report.adminNotes.isEmpty()) {
            report.adminNotes += "\n[RESOLVED] " + resolution + " on " + displayDateFormat.format(new Date());
        } else {
            report.adminNotes = "[RESOLVED] " + resolution + " on " + displayDateFormat.format(new Date());
        }
        
        saveDriverReports();
        refreshTable();
        updateStats();
        
        showNotification("Report marked as resolved", SUCCESS);
    }
    
    private void deleteReportDialog(DriverReport report) {
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Delete report #" + report.reportId + "?\n\n" +
            "Vehicle: " + report.vehicleId + "\n" +
            "Driver: " + getDriverDisplayInfo(report.driverName) + "\n" +
            "Description: " + report.description,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            driverReports.remove(report);
            saveDriverReports();
            refreshTable();
            updateStats();
            showNotification("Report deleted", SUCCESS);
        }
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
        
        for (Vehicle v : vehicles) {
            boolean hasActiveMaintenance = maintenanceManagement.isVehicleInMaintenance(v.id);
            
            if (hasActiveMaintenance && !v.status.equals("Maintenance")) {
                v.status = "Maintenance";
            } else if (!hasActiveMaintenance && v.status.equals("Maintenance")) {
                v.status = "Active";
            }
        }
        saveData();
        refreshTable();
    }

    private Optional<Vehicle> findVehicle(String id) {
        return vehicles.stream().filter(v -> v.id.equals(id)).findFirst();
    }

    private void refreshTable() {
        loadDriversFromFile();
        
        tableModel.setRowCount(0);
        for (Vehicle v : vehicles) {
            String driverId = getDriverIdForVehicle(v.id);
            String driverDisplay;
            if (driverId != null) {
                DriverData driver = driverCache.get(driverId);
                driverDisplay = driver != null ? getDriverDisplayInfo(driver.name) : "Unassigned";
            } else if (v.driverName != null && !v.driverName.equals("Unassigned")) {
                driverDisplay = v.driverName;
            } else {
                driverDisplay = "Unassigned";
            }
            
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
        String driverName = driverId != null && driverCache.get(driverId) != null ? 
            driverCache.get(driverId).name : "Unassigned";
        
        String message = String.format(
            "Vehicle ID: %s\n" +
            "Model: %s\n" +
            "Type: %s\n" +
            "License Plate: %s\n" +
            "Fuel Type: %s\n" +
            "Status: %s\n" +
            "Road Tax Expiry: %s%s\n" +
            "Driver: %s\n" +
            "Pending Reports: %d",
            v.id, v.model, v.type, v.numberPlate, v.fuelType, v.status,
            displayDateFormat.format(v.roadTaxExpiry), taxExpired ? " (OVERDUE!)" : "",
            driverName, reportCount
        );
        
        JOptionPane.showMessageDialog(mainPanel, message, 
            "Vehicle Details", JOptionPane.INFORMATION_MESSAGE);
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