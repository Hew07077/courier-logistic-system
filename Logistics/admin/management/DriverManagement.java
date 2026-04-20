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
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class DriverManagement extends AdminManagementModule {
    
    private DriverStorage storage;
    
    private VehicleManagement vehicleManagement;
    private OrderManagement orderManagement;
    
    private String currentApprovalFilter = null;
    private String currentWorkFilter = null;
    
    private String currentPhotoPath = null;      // IC photo path
    private String licensePhotoPath = null;      // License photo path
    
    // Additional colors specific to Driver Management
    private static final Color APPROVED_COLOR = new Color(40, 167, 69);
    private static final Color PENDING_COLOR = new Color(255, 193, 7);
    private static final Color REJECTED_COLOR = new Color(220, 53, 69);
    private static final Color ON_DELIVERY_COLOR = new Color(23, 162, 184);
    private static final Color OFF_DUTY_COLOR = new Color(108, 117, 125);
    
    // ==================== CONSTRUCTORS ====================
    
    public DriverManagement() {
        this(null, null);
    }
    
    public DriverManagement(VehicleManagement vehicleMgmt, OrderManagement orderMgmt) {
        this.vehicleManagement = vehicleMgmt;
        this.orderManagement = orderMgmt;
        this.storage = new DriverStorage();
        initializeUI();
        System.out.println("DriverManagement initialized with " + storage.getTotalCount() + " drivers");
    }
    
    // ==================== PUBLIC METHODS FOR INTEGRATION ====================
    
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
    
    // ==================== ABSTRACT METHOD IMPLEMENTATIONS ====================
    
    @Override
    protected String getModuleName() {
        return "Driver Management";
    }
    
    @Override
    protected String getSubtitle() {
        return "Manage drivers, approvals, assignments, and performance";
    }
    
    @Override
    protected void initializeUI() {
        createTable();
        createHeaderPanel();
        createStatsPanelContainer(7);
        createButtonPanel();
        createStatusBar();
        
        // Create stats panel with 7 columns
        statsPanel = createStatsPanelContainer(7);
        String[] titles = {"Total Drivers", "Approved", "Pending", "Rejected", "Available", "On Delivery", "Off Duty"};
        String[] descriptions = {"All drivers", "Active drivers", "Awaiting approval", "Rejected applications", "Ready for work", "Currently delivering", "Not working"};
        Color[] colors = {PRIMARY, SUCCESS, WARNING, DANGER, SUCCESS, INFO, TEXT_SECONDARY};
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
            final int index = i;
            final Color color = colors[i];
            final Color bgColor = bgColors[i];
            final String title = titles[i];
            
            JPanel card = createStatCard(title, descriptions[i], "0", color, bgColor, i);
            statCards[i] = card;
            statsPanel.add(card);
            
            if (i >= 1 && i <= 3) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (currentFilterIndex != index) {
                            card.setBackground(bgColor);
                            card.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(color, 1, true),
                                BorderFactory.createEmptyBorder(7, 11, 7, 11)
                            ));
                        }
                    }
                    public void mouseExited(MouseEvent e) {
                        if (currentFilterIndex != index) {
                            card.setBackground(CARD_BG);
                            card.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(BORDER_COLOR, 1, true),
                                BorderFactory.createEmptyBorder(8, 12, 8, 12)
                            ));
                        }
                    }
                    public void mouseClicked(MouseEvent e) {
                        applyApprovalFilter(title.toUpperCase(), index, color);
                    }
                });
            } else if (i >= 4 && i <= 6) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (currentFilterIndex != index) {
                            card.setBackground(bgColor);
                            card.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(color, 1, true),
                                BorderFactory.createEmptyBorder(7, 11, 7, 11)
                            ));
                        }
                    }
                    public void mouseExited(MouseEvent e) {
                        if (currentFilterIndex != index) {
                            card.setBackground(CARD_BG);
                            card.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(BORDER_COLOR, 1, true),
                                BorderFactory.createEmptyBorder(8, 12, 8, 12)
                            ));
                        }
                    }
                    public void mouseClicked(MouseEvent e) {
                        applyWorkFilter(title, index, color);
                    }
                });
            } else if (i == 0) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { card.setBackground(HOVER_COLOR); }
                    public void mouseExited(MouseEvent e) { card.setBackground(CARD_BG); }
                    public void mouseClicked(MouseEvent e) { clearAllFilters(); }
                });
            }
        }
        
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.CENTER);
        
        add(topContainer, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateStats();
    }
    
    @Override
    protected void loadData() {
        storage = new DriverStorage();
    }
    
    @Override
    protected void createTable() {
        String[] columns = {"ID", "Name", "Phone", "Work Status", "License Type", 
                            "Vehicle ID", "Deliveries", "Join Date", "Approval Status", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(REGULAR_FONT);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(8, 3));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        // Initialize rowSorter
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);
        table.getColumnModel().getColumn(9).setPreferredWidth(150);
        
        // Set custom renderers
        table.getColumnModel().getColumn(3).setCellRenderer(new WorkStatusCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new LicenseTypeCellRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new ApprovalStatusCellRenderer());
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showDriverDetails();
                }
            }
        });
        
        // Populate table data after creation
        populateTableData();
    }
    
    @Override
    protected void populateTableData() {
        if (tableModel == null) return;
        
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
    }
    
    @Override
    protected void updateStats() {
        SwingUtilities.invokeLater(() -> {
            if (statValues != null) {
                if (statValues[0] != null) statValues[0].setText(String.valueOf(storage.getTotalCount()));
                if (statValues[1] != null) statValues[1].setText(String.valueOf(storage.getApprovedCount()));
                if (statValues[2] != null) statValues[2].setText(String.valueOf(storage.getPendingCount()));
                if (statValues[3] != null) statValues[3].setText(String.valueOf(storage.getRejectedCount()));
                if (statValues[4] != null) statValues[4].setText(String.valueOf(storage.getAvailableCount()));
                if (statValues[5] != null) statValues[5].setText(String.valueOf(storage.getOnDeliveryCount()));
                if (statValues[6] != null) statValues[6].setText(String.valueOf(storage.getOffDutyCount()));
            }
            
            if (statsPanel != null) {
                statsPanel.revalidate();
                statsPanel.repaint();
            }
        });
    }
    
    @Override
    protected String[] getTableColumns() {
        return new String[]{"ID", "Name", "Phone", "Work Status", "License Type", 
                            "Vehicle ID", "Deliveries", "Join Date", "Approval Status", "Remarks"};
    }
    
    @Override
    protected int[] getColumnWidths() {
        return new int[]{70, 150, 100, 100, 120, 80, 70, 100, 100, 150};
    }
    
    @Override
    protected void applyStatusFilter(String status, int cardIndex) {
        try {
            if (rowSorter == null) {
                rowSorter = new TableRowSorter<>(tableModel);
                table.setRowSorter(rowSorter);
            }
            
            resetCardBorders();
            
            if (currentFilterIndex == cardIndex) {
                currentApprovalFilter = null;
                currentWorkFilter = null;
                currentStatusFilter = null;
                currentFilterIndex = -1;
                rowSorter.setRowFilter(null);
                // Notification removed
            } else {
                currentApprovalFilter = status;
                currentWorkFilter = null;
                currentStatusFilter = status;
                currentFilterIndex = cardIndex;
                
                if (statCards != null && cardIndex >= 0 && cardIndex < statCards.length && statCards[cardIndex] != null) {
                    statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                    ));
                    statCards[cardIndex].setBackground(getColorForApprovalStatus(status).brighter());
                }
                
                applyFilters();
                // Notification removed
            }
        } catch (Exception e) {
            System.err.println("Error in applyStatusFilter: " + e.getMessage());
            // Notification removed
        }
    }
    
    private void applyApprovalFilter(String status, int cardIndex, Color color) {
        try {
            if (rowSorter == null) {
                rowSorter = new TableRowSorter<>(tableModel);
                table.setRowSorter(rowSorter);
            }
            
            resetCardBorders();
            
            if (currentFilterIndex == cardIndex) {
                currentApprovalFilter = null;
                currentWorkFilter = null;
                currentStatusFilter = null;
                currentFilterIndex = -1;
                rowSorter.setRowFilter(null);
                // Notification removed
            } else {
                currentApprovalFilter = status;
                currentWorkFilter = null;
                currentStatusFilter = status;
                currentFilterIndex = cardIndex;
                
                if (statCards != null && cardIndex >= 0 && cardIndex < statCards.length && statCards[cardIndex] != null) {
                    statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                    ));
                    statCards[cardIndex].setBackground(color.brighter());
                }
                
                applyFilters();
                // Notification removed
            }
        } catch (Exception e) {
            System.err.println("Error in applyApprovalFilter: " + e.getMessage());
            // Notification removed
        }
    }
    
    private void applyWorkFilter(String status, int cardIndex, Color color) {
        try {
            if (rowSorter == null) {
                rowSorter = new TableRowSorter<>(tableModel);
                table.setRowSorter(rowSorter);
            }
            
            resetCardBorders();
            
            if (currentFilterIndex == cardIndex) {
                currentApprovalFilter = null;
                currentWorkFilter = null;
                currentStatusFilter = null;
                currentFilterIndex = -1;
                rowSorter.setRowFilter(null);
                // Notification removed
            } else {
                currentApprovalFilter = null;
                currentWorkFilter = status;
                currentStatusFilter = status;
                currentFilterIndex = cardIndex;
                
                if (statCards != null && cardIndex >= 0 && cardIndex < statCards.length && statCards[cardIndex] != null) {
                    statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                    ));
                    statCards[cardIndex].setBackground(color.brighter());
                }
                
                applyFilters();
                // Notification removed
            }
        } catch (Exception e) {
            System.err.println("Error in applyWorkFilter: " + e.getMessage());
            // Notification removed
        }
    }
    
    private void applyFilters() {
        if (rowSorter == null) return;
        
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
    
    private Color getColorForApprovalStatus(String status) {
        switch(status) {
            case "APPROVED": return SUCCESS;
            case "PENDING": return WARNING;
            case "REJECTED": return DANGER;
            default: return PRIMARY;
        }
    }
    
    @Override
    protected void clearFilters() {
        try {
            if (rowSorter == null) {
                rowSorter = new TableRowSorter<>(tableModel);
                table.setRowSorter(rowSorter);
            }
            resetCardBorders();
            currentApprovalFilter = null;
            currentWorkFilter = null;
            currentStatusFilter = null;
            currentFilterIndex = -1;
            rowSorter.setRowFilter(null);
            // Notification removed
        } catch (Exception e) {
            System.err.println("Error clearing filters: " + e.getMessage());
            // Notification removed
        }
    }
    
    private void clearAllFilters() {
        clearFilters();
    }
    
    @Override
    protected JPanel createButtonPanel() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        buttonPanel.setBackground(BG_COLOR);
        
        JButton addBtn = createStyledButton("Add Driver", SUCCESS, 100, 32);
        addBtn.addActionListener(e -> addDriver());
        buttonPanel.add(addBtn);
        
        JButton viewBtn = createStyledButton("View Details", INFO, 100, 32);
        viewBtn.addActionListener(e -> showDriverDetails());
        buttonPanel.add(viewBtn);
        
        JButton deleteBtn = createStyledButton("Delete", DANGER, 100, 32);
        deleteBtn.addActionListener(e -> deleteDriver());
        buttonPanel.add(deleteBtn);
        
        JButton scheduleBtn = createStyledButton("Schedule", PURPLE, 100, 32);
        scheduleBtn.addActionListener(e -> scheduleDriver());
        buttonPanel.add(scheduleBtn);
        
        buttonPanel.add(Box.createHorizontalStrut(20));
        
        JLabel approvalLabel = new JLabel("Approval Actions:");
        approvalLabel.setFont(HEADER_FONT);
        approvalLabel.setForeground(TEXT_PRIMARY);
        buttonPanel.add(approvalLabel);
        
        JButton approveBtn = createStyledButton("Approve", SUCCESS, 100, 32);
        approveBtn.addActionListener(e -> approveDriver());
        buttonPanel.add(approveBtn);
        
        JButton rejectBtn = createStyledButton("Reject", DANGER, 100, 32);
        rejectBtn.addActionListener(e -> rejectDriver());
        buttonPanel.add(rejectBtn);
        
        return buttonPanel;
    }
    
    @Override
    public int getTotalCount() {
        return storage.getTotalCount();
    }
    
    // ==================== LICENSE HELPER METHODS ====================
    
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
    
    // ==================== CUSTOM RENDERERS ====================
    
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
                        label.setText("" + status);
                        break;
                    case "On Delivery":
                        label.setForeground(ON_DELIVERY_COLOR);
                        label.setText("" + status);
                        break;
                    case "Off Duty":
                        label.setForeground(OFF_DUTY_COLOR);
                        label.setText("" + status);
                        break;
                    case "On Leave":
                        label.setForeground(WARNING);
                        label.setText("" + status);
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
                        label.setText("APPROVED");
                        break;
                    case "PENDING":
                        label.setForeground(PENDING_COLOR);
                        label.setText("PENDING");
                        break;
                    case "REJECTED":
                        label.setForeground(REJECTED_COLOR);
                        label.setText("REJECTED");
                        break;
                }
            }
            
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return label;
        }
    }
    
    // ==================== APPROVAL METHODS ====================
    
    private void approveDriver() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a driver to approve");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentApproval = (String) tableModel.getValueAt(modelRow, 8);
        
        if ("APPROVED".equals(currentApproval)) {
            showInfo("Driver is already approved!");
            return;
        }
        
        if (confirmAction("Approve driver " + name + " (" + id + ")?\n\nThey will be able to login and start working.", "Confirm Approval")) {
            storage.approveDriver(id);
            refreshTable();
            showSuccess("Driver " + name + " approved successfully!");
        }
    }
    
    private void rejectDriver() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a driver to reject");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentApproval = (String) tableModel.getValueAt(modelRow, 8);
        
        if ("REJECTED".equals(currentApproval)) {
            showInfo("Driver is already rejected!");
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, 
            "Enter rejection reason for " + name + ":", 
            "Reject Driver", JOptionPane.PLAIN_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            if (confirmAction("Reject driver " + name + "?\nReason: " + reason, "Confirm Rejection")) {
                storage.rejectDriver(id, reason);
                refreshTable();
                showSuccess("Driver " + name + " rejected.\nReason: " + reason);
            }
        } else if (reason != null) {
            showWarning("Please enter a rejection reason.");
        }
    }
    
    // ==================== SHOW DRIVER DETAILS ====================
    
    private void showDriverDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a driver to view");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Driver driver = storage.findDriver(id);
        if (driver == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                      "Driver Details - " + driver.name, true);
        dialog.setSize(650, 580);
        dialog.setLocationRelativeTo(this);
        
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
        }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // License Information Section
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
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        
        if ("PENDING".equals(driver.approvalStatus)) {
            JButton approveBtn = createStyledButton("Approve", SUCCESS, 100, 32);
            approveBtn.addActionListener(e -> {
                storage.approveDriver(driver.id);
                dialog.dispose();
                refreshTable();
                showSuccess("Driver " + driver.name + " approved successfully!");
            });
            buttonPanel.add(approveBtn);
            
            JButton rejectBtn = createStyledButton("Reject", DANGER, 100, 32);
            rejectBtn.addActionListener(e -> {
                String reason = JOptionPane.showInputDialog(dialog, "Rejection reason:");
                if (reason != null && !reason.trim().isEmpty()) {
                    storage.rejectDriver(driver.id, reason);
                    dialog.dispose();
                    refreshTable();
                    showSuccess("Driver " + driver.name + " rejected.");
                }
            });
            buttonPanel.add(rejectBtn);
        }
        
        JButton closeBtn = createStyledButton("Close", PRIMARY, 85, 32);
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
        
        JButton viewPhotoBtn = createStyledButton("View Full Size", INFO, 100, 28);
        viewPhotoBtn.addActionListener(e -> {
            if (licensePhotoPath != null && !licensePhotoPath.isEmpty()) {
                showFullSizeImage(licensePhotoPath, "License Photo");
            } else {
                showInfo("No license photo available.");
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
            showError("No image available.");
            return;
        }
        
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                showError("Image file not found: " + imagePath);
                return;
            }
            
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                showError("Unable to load image.");
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
            
            JDialog imageDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
            imageDialog.setLayout(new BorderLayout());
            
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JScrollPane scrollPane = new JScrollPane(imageLabel);
            scrollPane.getViewport().setBackground(Color.BLACK);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton closeBtn = createStyledButton("Close", PRIMARY, 100, 35);
            closeBtn.addActionListener(e -> imageDialog.dispose());
            buttonPanel.add(closeBtn);
            
            imageDialog.add(scrollPane, BorderLayout.CENTER);
            imageDialog.add(buttonPanel, BorderLayout.SOUTH);
            imageDialog.setSize(scaledWidth + 50, scaledHeight + 80);
            imageDialog.setLocationRelativeTo(this);
            imageDialog.setVisible(true);
            
        } catch (IOException e) {
            showError("Error loading image: " + e.getMessage());
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
    
    // ==================== ADD DRIVER ====================
    
    private void addDriver() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                      "Apply as Courier", true);
        dialog.setSize(500, 580);
        dialog.setLocationRelativeTo(this);
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
        JButton uploadICBtn = createStyledButton("Upload", new Color(100, 100, 100), 90, 30);
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
        JButton uploadLicenseBtn = createStyledButton("Upload", new Color(100, 100, 100), 90, 30);
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
        formPanel.add(new JLabel("Upload Photo:*"), gbc);
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
        
        JButton submitBtn = createStyledButton("SUBMIT APPLICATION", PRIMARY, 180, 45);
        JButton cancelBtn = createStyledButton("CANCEL", TEXT_SECONDARY, 180, 45);
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
            showWarning("Please fill in all fields!");
            nameField.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            showWarning("Please fill in all fields!");
            emailField.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            showWarning("Please fill in all fields!");
            phoneField.requestFocus();
            return false;
        }
        if (ic.isEmpty()) {
            showWarning("Please fill in all fields!");
            icField.requestFocus();
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showWarning("Invalid email format! Example: name@domain.com");
            emailField.requestFocus();
            return false;
        }
        
        String cleanPhone = phone.replaceAll("-", "").replaceAll("\\s+", "");
        if (!cleanPhone.matches("^[0-9]{10,11}$")) {
            showWarning("Invalid phone number! Only digits allowed (10-11 digits)");
            phoneField.requestFocus();
            return false;
        }
        
        if (!ic.matches("^[0-9]{12}$")) {
            showWarning("Invalid IC number! Only digits allowed (12 digits)");
            icField.requestFocus();
            return false;
        }
        
        if (storage.isIcNumberExists(ic)) {
            showWarning("This IC number is already registered!");
            icField.requestFocus();
            return false;
        }
        
        if (storage.isEmailExists(email)) {
            showWarning("This email is already registered!");
            emailField.requestFocus();
            return false;
        }
        
        if (storage.isPhoneExists(phone)) {
            showWarning("This phone number is already registered!");
            phoneField.requestFocus();
            return false;
        }
        
        if (currentPhotoPath == null || licensePhotoPath == null) {
            showWarning("Please upload both IC and License photos!");
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
            showError("Error adding driver: " + e.getMessage());
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
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a driver to delete");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String approval = (String) tableModel.getValueAt(modelRow, 8);
        
        String warning = "Are you sure you want to delete driver " + name + " (" + id + ")?";
        if ("APPROVED".equals(approval)) {
            warning = "WARNING: This driver is APPROVED and may have active deliveries.\n\n" + warning;
        }
        
        if (confirmAction(warning, "Confirm Delete")) {
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
            showSuccess("Driver deleted successfully");
        }
    }
    
    private void scheduleDriver() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a driver to schedule");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String approval = (String) tableModel.getValueAt(modelRow, 8);
        
        if (!"APPROVED".equals(approval)) {
            showWarning("Only approved drivers can be scheduled.");
            return;
        }
        
        String[] options = {"Morning Shift (6AM-2PM)", "Afternoon Shift (2PM-10PM)", 
                           "Night Shift (10PM-6AM)", "Day Off", "On Leave"};
        
        String shift = (String) JOptionPane.showInputDialog(this,
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
            
            showSuccess(name + " scheduled for: " + shift);
        }
    }
    
    // ==================== REFRESH TABLE METHOD ====================
    
    @Override
    protected void refreshTable() {
        if (tableModel != null) {
            tableModel.setRowCount(0);
            populateTableData();
        }
        updateStats();
    }
    
    // ==================== IMAGE LOADING METHODS ====================
    
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
    
    // ==================== PUBLIC METHODS FOR DASHBOARD ====================
    
    @Override
    public JPanel getMainPanel() { 
        refreshTable();
        return this; 
    }
    
    @Override
    public JPanel getRefreshedPanel() { 
        refreshTable(); 
        return this; 
    }
    
    @Override
    public void refreshData() { 
        storage = new DriverStorage();
        refreshTable(); 
    }
    
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