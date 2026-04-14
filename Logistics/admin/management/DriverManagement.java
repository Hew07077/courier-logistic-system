package admin.management;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class DriverManagement {
    private JPanel mainPanel;
    private JTable driversTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JPanel statsPanel;
    private JLabel[] statValues;
    private JPanel[] statCards;
    
    private DriverStorage storage;
    
    private VehicleManagement vehicleManagement;
    private OrderManagement orderManagement;
    
    private String currentApprovalFilter = null;
    private String currentWorkFilter = null;
    private int currentFilterIndex = -1;
    
    private static final String PHOTO_DIR = "driver_photos/";
    private String currentPhotoPath = null;      // IC photo path
    private String licensePhotoPath = null;      // License photo path
    
    // Modern color scheme
    private static final Color PRIMARY = new Color(46, 125, 50);
    private static final Color PRIMARY_DARK = new Color(27, 94, 32);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color BG_COLOR = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color TEXT_MUTED = new Color(134, 142, 150);
    private static final Color HOVER_COLOR = new Color(245, 247, 250);
    private static final Color SELECTION_COLOR = new Color(232, 245, 233);
    private static final Color ACTIVE_FILTER_BORDER = PRIMARY;
    private static final Color APPROVED_COLOR = new Color(40, 167, 69);
    private static final Color PENDING_COLOR = new Color(255, 193, 7);
    private static final Color REJECTED_COLOR = new Color(220, 53, 69);
    
    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    
    public DriverManagement() {
        this(null, null);
    }
    
    public DriverManagement(VehicleManagement vehicleMgmt, OrderManagement orderMgmt) {
        this.vehicleManagement = vehicleMgmt;
        this.orderManagement = orderMgmt;
        storage = new DriverStorage();
        createMainPanel();
        refreshTable();
        System.out.println("DriverManagement initialized with " + storage.getTotalCount() + " drivers");
    }
    
    // ========== LICENSE TYPE DISPLAY HELPER ==========
    
    private String getLicenseDisplayString(String licenseType) {
        if (licenseType == null) return "Not specified";
        String upper = licenseType.toUpperCase();
        switch(upper) {
            case "B": return "B (Motorcycle)";
            case "B1": return "B1 (Motorcycle)";
            case "B2": return "B2 (Motorcycle)";
            case "D": return "D (Car/Van)";
            case "DA": return "DA (Car/Van)";
            case "E": return "E (Truck/Van)";
            case "E1": return "E1 (Truck/Van)";
            case "E2": return "E2 (Truck/Van)";
            default: return licenseType;
        }
    }
    
    // ========== PUBLIC METHODS FOR ORDER MANAGEMENT ==========
    
    public List<Driver> getAllDrivers() {
        return storage.getAllDrivers();
    }
    
    public List<Driver> getAvailableDrivers() {
        List<Driver> allDrivers = storage.getAllDrivers();
        List<Driver> available = new ArrayList<>();
        
        for (Driver d : allDrivers) {
            if ("APPROVED".equals(d.approvalStatus) && "Available".equals(d.workStatus)) {
                available.add(d);
            }
        }
        return available;
    }
    
    public Driver getDriverById(String id) {
        return storage.findDriver(id);
    }
    
    public void updateDriverStatus(String driverId, String newStatus) {
        Driver driver = storage.findDriver(driverId);
        if (driver != null) {
            driver.workStatus = newStatus;
            storage.updateDriver(driver);
            refreshTable();
        }
    }
    
    public boolean assignVehicleToDriver(String driverId, String vehicleId) {
        Driver driver = storage.findDriver(driverId);
        if (driver != null && "APPROVED".equals(driver.approvalStatus)) {
            driver.vehicleId = vehicleId;
            storage.updateDriver(driver);
            refreshTable();
            return true;
        }
        return false;
    }
    
    public List<Driver> getAvailableDriversWithVehicles() {
        List<Driver> allDrivers = storage.getAllDrivers();
        List<Driver> available = new ArrayList<>();
        
        for (Driver d : allDrivers) {
            if ("APPROVED".equals(d.approvalStatus) 
                && "Available".equals(d.workStatus)
                && d.vehicleId != null && !d.vehicleId.isEmpty()) {
                available.add(d);
            }
        }
        return available;
    }
    
    public void ensureAtLeastOneAvailableDriver() {
        List<Driver> drivers = storage.getAllDrivers();
        boolean hasAvailable = false;
        
        for (Driver d : drivers) {
            if ("APPROVED".equals(d.approvalStatus) && "Available".equals(d.workStatus)) {
                hasAvailable = true;
                break;
            }
        }
        
        if (!hasAvailable && !drivers.isEmpty()) {
            Driver firstDriver = drivers.get(0);
            firstDriver.approvalStatus = "APPROVED";
            firstDriver.workStatus = "Available";
            storage.updateDriver(firstDriver);
            refreshTable();
        }
    }
    
    // ========== UI CREATION METHODS ==========
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_COLOR);
        
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createStatsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Driver Management");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Manage drivers, approvals, assignments, and performance");
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
        statsPanel = new JPanel(new GridLayout(1, 7, 12, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {
            "Total Drivers", "Approved", "Pending", "Rejected",
            "Available", "On Delivery", "Off Duty"
        };
        String[] descriptions = {
            "All drivers", "Active drivers", "Awaiting approval", "Rejected applications",
            "Ready for work", "Currently delivering", "Not working"
        };
        Color[] colors = {
            PRIMARY, SUCCESS, WARNING, DANGER,
            SUCCESS, INFO, TEXT_SECONDARY
        };
        Color[] bgColors = {
            new Color(232, 245, 233),
            new Color(232, 245, 233),
            new Color(255, 243, 224),
            new Color(255, 235, 238),
            new Color(232, 245, 233),
            new Color(227, 242, 253),
            new Color(245, 245, 245)
        };
        
        statValues = new JLabel[7];
        statCards = new JPanel[7];
        
        for (int i = 0; i < 7; i++) {
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
        
        if (index >= 1 && index <= 3) {
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
                    applyApprovalFilter(filterStatus.toUpperCase(), cardIndex, color);
                }
            });
        }
        
        if (index >= 4 && index <= 6) {
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
                    applyWorkFilter(filterStatus, cardIndex, color);
                }
            });
        }
        
        if (index == 0) {
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
    
    private void applyApprovalFilter(String status, int cardIndex, Color color) {
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentApprovalFilter = null;
            currentWorkFilter = null;
            currentFilterIndex = -1;
            applyFilters();
        } else {
            currentApprovalFilter = status;
            currentWorkFilter = null;
            currentFilterIndex = cardIndex;
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            applyFilters();
        }
    }
    
    private void applyWorkFilter(String status, int cardIndex, Color color) {
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentApprovalFilter = null;
            currentWorkFilter = null;
            currentFilterIndex = -1;
            applyFilters();
        } else {
            currentApprovalFilter = null;
            currentWorkFilter = status;
            currentFilterIndex = cardIndex;
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            applyFilters();
        }
    }
    
    private void clearAllFilters() {
        resetCardBorders();
        currentApprovalFilter = null;
        currentWorkFilter = null;
        currentFilterIndex = -1;
        rowSorter.setRowFilter(null);
    }
    
    private void applyFilters() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        if (currentApprovalFilter != null) {
            filters.add(RowFilter.regexFilter("^" + currentApprovalFilter + "$", 8));
        }
        
        if (currentWorkFilter != null) {
            String status = currentWorkFilter;
            if (status.equals("Available")) {
                filters.add(RowFilter.regexFilter("^Available$", 3));
            } else if (status.equals("On Delivery")) {
                filters.add(RowFilter.regexFilter("^On Delivery$", 3));
            } else if (status.equals("Off Duty")) {
                filters.add(RowFilter.regexFilter("^(Off Duty|On Leave)$", 3));
            }
        }
        
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setPreferredSize(new Dimension(1100, 400));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createTable() {
        // Removed License No and License Expiry columns, added License Type
        String[] columns = {"ID", "Name", "Phone", "Work Status", "License Type", 
                            "Vehicle ID", "Deliveries", "Join Date", "Approval Status", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        driversTable = new JTable(tableModel);
        driversTable.setRowHeight(40);
        driversTable.setFont(REGULAR_FONT);
        driversTable.setSelectionBackground(SELECTION_COLOR);
        driversTable.setSelectionForeground(TEXT_PRIMARY);
        driversTable.setShowGrid(true);
        driversTable.setGridColor(BORDER_COLOR);
        driversTable.setIntercellSpacing(new Dimension(8, 3));
        driversTable.setFillsViewportHeight(true);
        
        JTableHeader header = driversTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        rowSorter = new TableRowSorter<>(tableModel);
        driversTable.setRowSorter(rowSorter);
        
        driversTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        driversTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        driversTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        driversTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        driversTable.getColumnModel().getColumn(6).setPreferredWidth(70);
        driversTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(9).setPreferredWidth(150);
        
        driversTable.getColumnModel().getColumn(3).setCellRenderer(new WorkStatusCellRenderer());
        driversTable.getColumnModel().getColumn(4).setCellRenderer(new LicenseTypeCellRenderer());
        driversTable.getColumnModel().getColumn(8).setCellRenderer(new ApprovalStatusCellRenderer());
        
        driversTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && driversTable.getSelectedRow() != -1) {
                    showDriverDetails();
                }
            }
        });
        
        refreshTable();
        
        return driversTable;
    }
    
    private class WorkStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected) {
                String status = value != null ? value.toString() : "";
                
                switch (status) {
                    case "Available":
                        label.setForeground(SUCCESS);
                        label.setText("● " + status);
                        break;
                    case "On Delivery":
                        label.setForeground(INFO);
                        label.setText("🚚 " + status);
                        break;
                    case "Off Duty":
                        label.setForeground(TEXT_SECONDARY);
                        label.setText("○ " + status);
                        break;
                    case "On Leave":
                        label.setForeground(WARNING);
                        label.setText("✗ " + status);
                        break;
                }
            }
            
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return label;
        }
    }
    
    private class LicenseTypeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected && value != null) {
                String license = value.toString();
                if (license.contains("Motorcycle")) {
                    label.setForeground(new Color(156, 39, 176));
                } else if (license.contains("Car/Van")) {
                    label.setForeground(new Color(0, 123, 255));
                } else if (license.contains("Truck/Van")) {
                    label.setForeground(new Color(255, 87, 34));
                }
                label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            }
            
            return label;
        }
    }
    
    private class ApprovalStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected && value != null) {
                String status = value.toString();
                
                switch (status) {
                    case "APPROVED":
                        label.setForeground(APPROVED_COLOR);
                        label.setText("✓ APPROVED");
                        break;
                    case "PENDING":
                        label.setForeground(PENDING_COLOR);
                        label.setText("⏳ PENDING");
                        break;
                    case "REJECTED":
                        label.setForeground(REJECTED_COLOR);
                        label.setText("✗ REJECTED");
                        break;
                }
            }
            
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return label;
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        JButton addBtn = createButton("Add Driver", SUCCESS, new Color(30, 126, 52));
        addBtn.addActionListener(e -> addDriver());
        panel.add(addBtn);
        
        JButton viewBtn = createButton("View Details", INFO, new Color(17, 122, 139));
        viewBtn.addActionListener(e -> showDriverDetails());
        panel.add(viewBtn);
        
        JButton deleteBtn = createButton("Delete", DANGER, new Color(176, 42, 55));
        deleteBtn.addActionListener(e -> deleteDriver());
        panel.add(deleteBtn);
        
        JButton scheduleBtn = createButton("Schedule", new Color(111, 66, 193), new Color(81, 45, 168));
        scheduleBtn.addActionListener(e -> scheduleDriver());
        panel.add(scheduleBtn);
        
        panel.add(Box.createHorizontalStrut(20));
        
        JLabel approvalLabel = new JLabel("Approval Actions:");
        approvalLabel.setFont(HEADER_FONT);
        approvalLabel.setForeground(TEXT_PRIMARY);
        panel.add(approvalLabel);
        
        JButton approveBtn = createButton("Approve", SUCCESS, new Color(30, 126, 52));
        approveBtn.addActionListener(e -> approveDriver());
        panel.add(approveBtn);
        
        JButton rejectBtn = createButton("Reject", DANGER, new Color(176, 42, 55));
        rejectBtn.addActionListener(e -> rejectDriver());
        panel.add(rejectBtn);
        
        return panel;
    }
    
    private JButton createButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        
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
    
    // ========== APPROVAL METHODS ==========
    
    private void approveDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to approve", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentApproval = (String) tableModel.getValueAt(modelRow, 8);
        
        if ("APPROVED".equals(currentApproval)) {
            JOptionPane.showMessageDialog(mainPanel, "Driver is already approved!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Approve driver " + name + " (" + id + ")?\n\nThey will be able to login and start working.",
            "Confirm Approval",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            storage.approveDriver(id);
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, 
                "Driver " + name + " approved successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void rejectDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to reject", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentApproval = (String) tableModel.getValueAt(modelRow, 8);
        
        if ("REJECTED".equals(currentApproval)) {
            JOptionPane.showMessageDialog(mainPanel, "Driver is already rejected!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String reason = JOptionPane.showInputDialog(mainPanel, 
            "Enter rejection reason for " + name + ":", 
            "Reject Driver", JOptionPane.PLAIN_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "Reject driver " + name + "?\nReason: " + reason,
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                storage.rejectDriver(id, reason);
                refreshTable();
                JOptionPane.showMessageDialog(mainPanel, 
                    "Driver " + name + " rejected.\nReason: " + reason,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (reason != null) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a rejection reason.", 
                "Input Required", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // ========== SHOW DRIVER DETAILS ==========
    
    private void showDriverDetails() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to view", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Driver driver = storage.findDriver(id);
        if (driver == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Driver Details - " + driver.name, true);
        dialog.setSize(650, 580);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(CARD_BG);
        
        JPanel photoPanel = new JPanel();
        photoPanel.setBackground(CARD_BG);
        photoPanel.setPreferredSize(new Dimension(100, 100));
        photoPanel.setBorder(BorderFactory.createLineBorder(getApprovalColor(driver.approvalStatus), 2));
        
        JLabel detailPhotoLabel = new JLabel();
        detailPhotoLabel.setPreferredSize(new Dimension(96, 96));
        
        if (driver.photoPath != null) {
            detailPhotoLabel.setIcon(loadDriverPhoto(driver.photoPath, 96, 96));
        } else {
            detailPhotoLabel.setIcon(createDefaultPhoto(96, 96, driver.name));
        }
        
        photoPanel.add(detailPhotoLabel);
        
        JPanel titlePanel = new JPanel(new GridLayout(3, 1));
        titlePanel.setBackground(CARD_BG);
        
        JLabel nameLabel = new JLabel(driver.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(PRIMARY);
        
        JLabel idLabel = new JLabel(driver.id + " • " + driver.workStatus);
        idLabel.setFont(REGULAR_FONT);
        idLabel.setForeground(TEXT_SECONDARY);
        
        JLabel approvalLabel = new JLabel("Approval: " + driver.approvalStatus);
        approvalLabel.setFont(HEADER_FONT);
        approvalLabel.setForeground(getApprovalColor(driver.approvalStatus));
        
        titlePanel.add(nameLabel);
        titlePanel.add(idLabel);
        titlePanel.add(approvalLabel);
        
        headerPanel.add(photoPanel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(CARD_BG);
        
        // Contact Information Section
        detailsPanel.add(createDetailSection("Contact Information", new String[]{
            "Phone: " + driver.phone,
            "Email: " + (driver.email != null ? driver.email : "-"),
            "Emergency Contact: " + (driver.emergencyContact != null ? 
                driver.emergencyContact + " (" + driver.emergencyPhone + ")" : "-"),
            "Address: " + (driver.address != null ? driver.address : "-")
        }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // License Information Section - Only License Type and License Photo
        detailsPanel.add(createLicenseInfoSection(driver));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Assignment Information Section
        detailsPanel.add(createDetailSection("Assignment Information", new String[]{
            "Vehicle: " + (driver.vehicleId != null ? driver.vehicleId : "Not Assigned"),
            "Join Date: " + driver.joinDate,
            "Total Deliveries: " + driver.totalDeliveries
        }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Approval Information Section
        detailsPanel.add(createDetailSection("Approval Information", new String[]{
            "Approval Status: " + driver.approvalStatus,
            "Remarks: " + (driver.remarks != null ? driver.remarks : "No remarks")
        }));
        
        if (driver.notes != null && !driver.notes.isEmpty()) {
            detailsPanel.add(Box.createVerticalStrut(10));
            detailsPanel.add(createDetailSection("Notes", new String[]{driver.notes}));
        }
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        
        if ("PENDING".equals(driver.approvalStatus)) {
            JButton approveBtn = new JButton("Approve");
            approveBtn.setFont(BUTTON_FONT);
            approveBtn.setForeground(Color.WHITE);
            approveBtn.setBackground(SUCCESS);
            approveBtn.setBorderPainted(false);
            approveBtn.setPreferredSize(new Dimension(100, 32));
            approveBtn.addActionListener(e -> {
                storage.approveDriver(driver.id);
                dialog.dispose();
                refreshTable();
                JOptionPane.showMessageDialog(mainPanel, 
                    "Driver " + driver.name + " approved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            });
            buttonPanel.add(approveBtn);
            
            JButton rejectBtn = new JButton("Reject");
            rejectBtn.setFont(BUTTON_FONT);
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setBackground(DANGER);
            rejectBtn.setBorderPainted(false);
            rejectBtn.setPreferredSize(new Dimension(100, 32));
            rejectBtn.addActionListener(e -> {
                String reason = JOptionPane.showInputDialog(dialog, "Rejection reason:");
                if (reason != null) {
                    storage.rejectDriver(driver.id, reason);
                    dialog.dispose();
                    refreshTable();
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Driver " + driver.name + " rejected.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            buttonPanel.add(rejectBtn);
        }
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JPanel createLicenseInfoSection(Driver driver) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("License Information");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        // License Type row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel typeLabel = new JLabel("License Type:");
        typeLabel.setFont(REGULAR_FONT);
        typeLabel.setForeground(TEXT_PRIMARY);
        contentPanel.add(typeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String licenseType = (driver.licenseType != null && !driver.licenseType.isEmpty()) ? driver.licenseType : "-";
        String licenseDisplay = getLicenseDisplayString(licenseType);
        JLabel typeValueLabel = new JLabel(licenseDisplay);
        typeValueLabel.setFont(REGULAR_FONT);
        typeValueLabel.setForeground(TEXT_PRIMARY);
        contentPanel.add(typeValueLabel, gbc);
        
        // License Photo row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel photoLabel = new JLabel("License Photo:");
        photoLabel.setFont(REGULAR_FONT);
        photoLabel.setForeground(TEXT_PRIMARY);
        contentPanel.add(photoLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        String licensePhotoPath = extractLicensePhotoPath(driver.notes);
        
        JPanel licensePhotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        licensePhotoPanel.setBackground(CARD_BG);
        licensePhotoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel licensePhotoIconLabel = new JLabel();
        licensePhotoIconLabel.setPreferredSize(new Dimension(100, 100));
        licensePhotoIconLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        licensePhotoIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (licensePhotoPath != null && !licensePhotoPath.isEmpty()) {
            ImageIcon icon = loadDriverPhoto(licensePhotoPath, 100, 100);
            if (icon != null) {
                licensePhotoIconLabel.setIcon(icon);
            } else {
                licensePhotoIconLabel.setText("No Image");
                licensePhotoIconLabel.setFont(SMALL_FONT);
            }
        } else {
            licensePhotoIconLabel.setText("No License Photo");
            licensePhotoIconLabel.setFont(SMALL_FONT);
        }
        
        JButton viewPhotoBtn = new JButton("View Full Size");
        viewPhotoBtn.setFont(SMALL_FONT);
        viewPhotoBtn.setBackground(INFO);
        viewPhotoBtn.setForeground(Color.WHITE);
        viewPhotoBtn.setBorderPainted(false);
        viewPhotoBtn.setFocusPainted(false);
        viewPhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewPhotoBtn.setPreferredSize(new Dimension(100, 28));
        viewPhotoBtn.addActionListener(e -> {
            if (licensePhotoPath != null && !licensePhotoPath.isEmpty()) {
                showFullSizeImage(licensePhotoPath, "License Photo");
            } else {
                JOptionPane.showMessageDialog(panel, "No license photo available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        licensePhotoPanel.add(licensePhotoIconLabel);
        licensePhotoPanel.add(Box.createHorizontalStrut(10));
        licensePhotoPanel.add(viewPhotoBtn);
        
        contentPanel.add(licensePhotoPanel, gbc);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private String extractLicensePhotoPath(String notes) {
        if (notes == null || notes.isEmpty()) {
            return null;
        }
        
        if (notes.contains("License Photo:")) {
            try {
                int start = notes.indexOf("License Photo:") + 14;
                int end = notes.indexOf(";", start);
                if (end == -1) end = notes.indexOf("\n", start);
                if (end == -1) end = notes.indexOf("|", start);
                if (end == -1) end = notes.length();
                String path = notes.substring(start, end).trim();
                if (!path.isEmpty()) {
                    return path;
                }
            } catch (Exception e) {}
        }
        
        return null;
    }
    
    private void showFullSizeImage(String imagePath, String title) {
        if (imagePath == null || imagePath.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "No image available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                JOptionPane.showMessageDialog(mainPanel, "Image file not found: " + imagePath, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                JOptionPane.showMessageDialog(mainPanel, "Unable to load image.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int maxWidth = screenSize.width - 100;
            int maxHeight = screenSize.height - 100;
            
            int imgWidth = originalImage.getWidth();
            int imgHeight = originalImage.getHeight();
            
            double scale = Math.min(1.0, Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight));
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            
            Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            
            JDialog imageDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), title, true);
            imageDialog.setLayout(new BorderLayout());
            
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JScrollPane scrollPane = new JScrollPane(imageLabel);
            scrollPane.getViewport().setBackground(Color.BLACK);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton closeBtn = new JButton("Close");
            closeBtn.setFont(REGULAR_FONT);
            closeBtn.setBackground(PRIMARY);
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setBorderPainted(false);
            closeBtn.setFocusPainted(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(e -> imageDialog.dispose());
            buttonPanel.add(closeBtn);
            
            imageDialog.add(scrollPane, BorderLayout.CENTER);
            imageDialog.add(buttonPanel, BorderLayout.SOUTH);
            imageDialog.setSize(scaledWidth + 50, scaledHeight + 80);
            imageDialog.setLocationRelativeTo(mainPanel);
            imageDialog.setVisible(true);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error loading image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private Color getApprovalColor(String status) {
        if ("APPROVED".equals(status)) return APPROVED_COLOR;
        if ("PENDING".equals(status)) return PENDING_COLOR;
        if ("REJECTED".equals(status)) return REJECTED_COLOR;
        return TEXT_SECONDARY;
    }
    
    private JPanel createDetailSection(String title, String[] lines) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        JPanel linesPanel = new JPanel(new GridLayout(lines.length, 1, 0, 3));
        linesPanel.setBackground(CARD_BG);
        linesPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        for (String line : lines) {
            JLabel lineLabel = new JLabel(line);
            lineLabel.setFont(REGULAR_FONT);
            lineLabel.setForeground(TEXT_PRIMARY);
            linesPanel.add(lineLabel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(linesPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== ADD DRIVER ==========
    
    private void addDriver() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Apply as Courier", true);
        dialog.setSize(500, 580);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("APPLY AS COURIER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField icField = new JTextField(15);
        
        // License Type Combo - Updated with all types
        JComboBox<String> licenseTypeCombo = new JComboBox<>(new String[]{
            "B", "B1", "B2", "D", "DA", "E", "E1", "E2"
        });
        licenseTypeCombo.setFont(REGULAR_FONT);
        licenseTypeCombo.setBackground(CARD_BG);
        licenseTypeCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // IC Upload
        JLabel icFileNameLabel = new JLabel("No file");
        icFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        JButton uploadICBtn = new JButton("Upload");
        uploadICBtn.setFont(SMALL_FONT);
        uploadICBtn.setBackground(new Color(100, 100, 100));
        uploadICBtn.setForeground(Color.WHITE);
        uploadICBtn.setFocusPainted(false);
        uploadICBtn.setBorderPainted(false);
        uploadICBtn.setPreferredSize(new Dimension(90, 30));
        uploadICBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                currentPhotoPath = fc.getSelectedFile().getAbsolutePath();
                icFileNameLabel.setText(fc.getSelectedFile().getName());
            }
        });
        
        // License Upload
        JLabel licenseFileNameLabel = new JLabel("No file");
        licenseFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        JButton uploadLicenseBtn = new JButton("Upload");
        uploadLicenseBtn.setFont(SMALL_FONT);
        uploadLicenseBtn.setBackground(new Color(100, 100, 100));
        uploadLicenseBtn.setForeground(Color.WHITE);
        uploadLicenseBtn.setFocusPainted(false);
        uploadLicenseBtn.setBorderPainted(false);
        uploadLicenseBtn.setPreferredSize(new Dimension(90, 30));
        uploadLicenseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                licensePhotoPath = fc.getSelectedFile().getAbsolutePath();
                licenseFileNameLabel.setText(fc.getSelectedFile().getName());
            }
        });
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        nameField.setPreferredSize(new Dimension(220, 30));
        formPanel.add(nameField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        emailField.setPreferredSize(new Dimension(220, 30));
        formPanel.add(emailField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Phone:*"), gbc);
        gbc.gridx = 1;
        phoneField.setPreferredSize(new Dimension(220, 30));
        formPanel.add(phoneField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("IC Number:*"), gbc);
        gbc.gridx = 1;
        icField.setPreferredSize(new Dimension(220, 30));
        formPanel.add(icField, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Upload IC:*"), gbc);
        gbc.gridx = 1;
        JPanel icPanel = new JPanel(new BorderLayout(5, 0));
        icPanel.setBackground(CARD_BG);
        icPanel.add(uploadICBtn, BorderLayout.WEST);
        icPanel.add(icFileNameLabel, BorderLayout.CENTER);
        formPanel.add(icPanel, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("License Type:*"), gbc);
        gbc.gridx = 1;
        licenseTypeCombo.setPreferredSize(new Dimension(220, 30));
        formPanel.add(licenseTypeCombo, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Upload License:*"), gbc);
        gbc.gridx = 1;
        JPanel licensePanel = new JPanel(new BorderLayout(5, 0));
        licensePanel.setBackground(CARD_BG);
        licensePanel.add(uploadLicenseBtn, BorderLayout.WEST);
        licensePanel.add(licenseFileNameLabel, BorderLayout.CENTER);
        formPanel.add(licensePanel, gbc);
        row++;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton submitBtn = new JButton("SUBMIT APPLICATION");
        submitBtn.setFont(BUTTON_FONT);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(PRIMARY);
        submitBtn.setBorderPainted(false);
        submitBtn.setPreferredSize(new Dimension(180, 45));
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(TEXT_SECONDARY);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setPreferredSize(new Dimension(180, 45));
        cancelBtn.addActionListener(e -> {
            currentPhotoPath = null;
            licensePhotoPath = null;
            dialog.dispose();
        });
        
        submitBtn.addActionListener(e -> {
            if (validateCourierForm(nameField, emailField, phoneField, icField, licenseTypeCombo)) {
                saveNewCourierDriver(dialog, nameField, emailField, phoneField, icField,
                                    licenseTypeCombo, licenseFileNameLabel.getText());
            }
        });
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean validateCourierForm(JTextField nameField, JTextField emailField,
                                         JTextField phoneField, JTextField icField,
                                         JComboBox<String> licenseTypeCombo) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String ic = icField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        if (ic.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            icField.requestFocus();
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(null, "Invalid email format! Example: name@domain.com", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        
        String cleanPhone = phone.replaceAll("-", "").replaceAll("\\s+", "");
        if (!cleanPhone.matches("^[0-9]{10,11}$")) {
            JOptionPane.showMessageDialog(null, "Invalid phone number! Only digits allowed (10-11 digits)", "Validation Error", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        if (!ic.matches("^[0-9]{12}$")) {
            JOptionPane.showMessageDialog(null, "Invalid IC number! Only digits allowed (12 digits)", "Validation Error", JOptionPane.WARNING_MESSAGE);
            icField.requestFocus();
            return false;
        }
        
        if (storage.isIcNumberExists(ic)) {
            JOptionPane.showMessageDialog(null, "This IC number is already registered!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            icField.requestFocus();
            return false;
        }
        
        if (storage.isEmailExists(email)) {
            JOptionPane.showMessageDialog(null, "This email is already registered!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        
        if (storage.isPhoneExists(phone)) {
            JOptionPane.showMessageDialog(null, "This phone number is already registered!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        if (currentPhotoPath == null || licensePhotoPath == null) {
            JOptionPane.showMessageDialog(null, "Please upload both IC and License photos!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void saveNewCourierDriver(JDialog dialog, JTextField nameField, JTextField emailField,
                                       JTextField phoneField, JTextField icField,
                                       JComboBox<String> licenseTypeCombo, String licenseFileName) {
        try {
            String driverId = storage.generateNewId();
            String password = "courier" + (int)(Math.random() * 9000 + 1000);
            String licenseExpiry = "2025-12-31";
            String licenseType = (String) licenseTypeCombo.getSelectedItem();
            
            Driver driver = new Driver(
                driverId,
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                "",  // license number - removed
                licenseExpiry
            );
            
            driver.icNumber = icField.getText().trim();
            driver.licenseType = licenseType;
            driver.photoPath = currentPhotoPath;
            driver.passwordHash = hashPassword(password);
            driver.approvalStatus = "PENDING";
            driver.workStatus = "Off Duty";
            
            driver.notes = "License Photo: " + licensePhotoPath;
            
            storage.addDriver(driver);
            refreshTable();
            
            String msg = String.format(
                "Application Submitted!\n\nUser ID: %s\nPassword: %s\nLicense Type: %s\n\nPlease save these credentials.\n\n" +
                "Your application is pending admin approval.",
                driverId, password, getLicenseDisplayString(licenseType));
            
            JOptionPane.showMessageDialog(dialog, msg, "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
            
            currentPhotoPath = null;
            licensePhotoPath = null;
            dialog.dispose();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog,
                "Error adding driver: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }
    
    private void deleteDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to delete", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String approval = (String) tableModel.getValueAt(modelRow, 8);
        
        String warning = "Are you sure you want to delete driver " + name + " (" + id + ")?";
        if ("APPROVED".equals(approval)) {
            warning = "WARNING: This driver is APPROVED and may have active deliveries.\n\n" + warning;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            warning,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Driver driver = storage.findDriver(id);
            if (driver != null && driver.photoPath != null) {
                File photoFile = new File(driver.photoPath);
                if (photoFile.exists()) {
                    photoFile.delete();
                }
            }
            
            if (vehicleManagement != null && driver != null && driver.vehicleId != null) {
                vehicleManagement.assignDriverToVehicle(null, driver.vehicleId);
            }
            
            storage.removeDriver(id);
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "Driver deleted successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void scheduleDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to schedule", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String approval = (String) tableModel.getValueAt(modelRow, 8);
        
        if (!"APPROVED".equals(approval)) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Only approved drivers can be scheduled.", 
                "Driver Not Approved", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String[] options = {"Morning Shift (6AM-2PM)", "Afternoon Shift (2PM-10PM)", 
                           "Night Shift (10PM-6AM)", "Day Off", "On Leave"};
        
        String shift = (String) JOptionPane.showInputDialog(mainPanel,
            "Select schedule for " + name + ":",
            "Driver Schedule",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (shift != null) {
            Driver driver = storage.findDriver(id);
            if (driver != null) {
                if (shift.contains("Day Off") || shift.contains("On Leave")) {
                    driver.workStatus = shift.contains("Day Off") ? "Off Duty" : "On Leave";
                } else {
                    driver.workStatus = "Available";
                }
                storage.updateDriver(driver);
                refreshTable();
            }
            
            JOptionPane.showMessageDialog(mainPanel,
                name + " scheduled for: " + shift,
                "Schedule Updated",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Driver> allDrivers = storage.getAllDrivers();
        
        for (Driver d : allDrivers) {
            tableModel.addRow(new Object[]{
                d.id,
                d.name,
                d.phone,
                d.workStatus,
                getLicenseDisplayString(d.licenseType),
                d.vehicleId != null ? d.vehicleId : "-",
                d.totalDeliveries,
                d.joinDate,
                d.approvalStatus != null ? d.approvalStatus : "PENDING",
                d.remarks != null ? d.remarks : "-"
            });
        }
        
        updateStats();
    }
    
    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            int total = storage.getTotalCount();
            int approved = storage.getApprovedCount();
            int pending = storage.getPendingCount();
            int rejected = storage.getRejectedCount();
            int available = storage.getAvailableCount();
            int onDelivery = storage.getOnDeliveryCount();
            int offDuty = storage.getOffDutyCount();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(total));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(approved));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(pending));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(rejected));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(available));
            if (statValues[5] != null) statValues[5].setText(String.valueOf(onDelivery));
            if (statValues[6] != null) statValues[6].setText(String.valueOf(offDuty));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        });
    }
    
    // ========== PHOTO METHODS ==========
    
    private String savePhoto(File sourceFile, String driverId) {
        if (sourceFile == null) return null;
        
        File directory = new File(PHOTO_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String fileName = sourceFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        String newFileName = driverId + extension;
        File destFile = new File(PHOTO_DIR + newFileName);
        
        try {
            java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return PHOTO_DIR + newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private ImageIcon loadDriverPhoto(String photoPath, int width, int height) {
        if (photoPath == null || photoPath.isEmpty()) {
            return createDefaultPhoto(width, height, null);
        }
        
        try {
            File photoFile = new File(photoPath);
            if (!photoFile.exists()) {
                return createDefaultPhoto(width, height, null);
            }
            
            BufferedImage img = ImageIO.read(photoFile);
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (IOException e) {
            e.printStackTrace();
            return createDefaultPhoto(width, height, null);
        }
    }
    
    private ImageIcon createDefaultPhoto(int width, int height, String initial) {
        BufferedImage defaultImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultImg.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(PRIMARY);
        g2d.fillOval(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, height / 2));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = initial != null && !initial.isEmpty() ? initial.substring(0, 1).toUpperCase() : "?";
        int x = (width - fm.stringWidth(text)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return new ImageIcon(defaultImg);
    }
    
    // ========== PUBLIC METHODS FOR DASHBOARD ==========
    
    public JPanel getMainPanel() { 
        refreshTable();
        return mainPanel; 
    }
    
    public JPanel getRefreshedPanel() { 
        refreshTable(); 
        return mainPanel; 
    }
    
    public void refreshData() { 
        storage = new DriverStorage();
        refreshTable(); 
    }
    
    public int getTotalCount() { return storage.getTotalCount(); }
    public int getApprovedCount() { return storage.getApprovedCount(); }
    public int getPendingCount() { return storage.getPendingCount(); }
    public int getRejectedCount() { return storage.getRejectedCount(); }
    public int getAvailableCount() { return storage.getAvailableCount(); }
    public int getOnDeliveryCount() { return storage.getOnDeliveryCount(); }
    public int getOnDutyCount() { return storage.getAvailableCount() + storage.getOnDeliveryCount(); }
    public int getOffDutyCount() { return storage.getOffDutyCount(); }
    
    public void setVehicleManagement(VehicleManagement vehicleMgmt) {
        this.vehicleManagement = vehicleMgmt;
    }
    
    public void setOrderManagement(OrderManagement orderMgmt) {
        this.orderManagement = orderMgmt;
    }
}