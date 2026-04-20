package admin.management;

import logistics.orders.Order;
import logistics.orders.OrderStorage;
import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class OrderManagement extends AdminManagementModule {
    
    private OrderStorage storage;
    private DriverStorage driverStorage;
    
    private DriverManagement driverManagement;
    private VehicleManagement vehicleManagement;
    
    // Additional colors specific to Order Management
    private static final Color ASSIGNED_COLOR = new Color(111, 66, 193);
    private static final Color PICKUP_COLOR = new Color(0, 150, 136);
    private static final Color OUT_FOR_DELIVERY_COLOR = new Color(255, 87, 34);
    
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // ==================== CONSTRUCTORS ====================
    
    public OrderManagement() {
        this(null, null);
    }
    
    public OrderManagement(DriverManagement driverMgmt) {
        this(driverMgmt, null);
    }
    
    public OrderManagement(DriverManagement driverMgmt, VehicleManagement vehicleMgmt) {
        this.driverManagement = driverMgmt;
        this.vehicleManagement = vehicleMgmt;
        this.storage = new OrderStorage();
        this.driverStorage = new DriverStorage();
        initializeUI();
    }
    
    // ==================== PUBLIC METHODS FOR INTEGRATION ====================
    
    public void setDriverManagement(DriverManagement driverMgmt) {
        this.driverManagement = driverMgmt;
    }
    
    public void setVehicleManagement(VehicleManagement vehicleMgmt) {
        this.vehicleManagement = vehicleMgmt;
    }
    
    // ==================== ABSTRACT METHOD IMPLEMENTATIONS ====================
    
    @Override
    protected String getModuleName() {
        return "Order Management";
    }
    
    @Override
    protected String getSubtitle() {
        return "Manage orders - Assign drivers to Pending, Delayed, or Failed orders";
    }
    
    @Override
    protected void initializeUI() {
        createTable();
        createHeaderPanel();
        createStatsPanelContainer(8);
        createButtonPanel();
        createStatusBar();
        
        // Create stats panel with 8 columns
        statsPanel = createStatsPanelContainer(8);
        String[] titles = {"Total", "Pending", "Assigned", "Pickup", "In Transit", "Out Delivery", "Delayed", "Failed"};
        String[] descriptions = {"All orders", "Awaiting assign", "Driver assigned", "Picked up", "On the way", "Out for delivery", "Behind schedule", "Delivery failed"};
        Color[] colors = {PRIMARY, WARNING, ASSIGNED_COLOR, PICKUP_COLOR, INFO, OUT_FOR_DELIVERY_COLOR, DANGER, DANGER};
        Color[] bgColors = {
            new Color(255, 245, 235),
            new Color(255, 243, 224),
            new Color(243, 232, 255),
            new Color(224, 242, 241),
            new Color(227, 242, 253),
            new Color(255, 243, 224),
            new Color(255, 235, 238),
            new Color(255, 235, 238)
        };
        
        statValues = new JLabel[8];
        statCards = new JPanel[8];
        
        for (int i = 0; i < 8; i++) {
            final int index = i;
            final Color color = colors[i];
            final Color bgColor = bgColors[i];
            final String title = titles[i];
            
            JPanel card = createStatCard(title, descriptions[i], "0", color, bgColor, i);
            statCards[i] = card;
            statsPanel.add(card);
            
            if (index == 0) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { card.setBackground(HOVER_COLOR); }
                    public void mouseExited(MouseEvent e) { card.setBackground(CARD_BG); }
                    public void mouseClicked(MouseEvent e) { clearAllFilters(); }
                });
            } else {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                final String filterTitle = title;
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
                        applyStatusFilter(filterTitle, cardIndex, color);
                    }
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
        storage = new OrderStorage();
        driverStorage = new DriverStorage();
    }
    
    @Override
    protected void createTable() {
        String[] columns = {"Order ID", "Recipient", "Status", "Order Date", "Est. Delivery", 
                            "Sender", "Weight", "Driver", "Vehicle", "Amount"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        comp.setBackground(new Color(252, 252, 253));
                    } else {
                        comp.setBackground(CARD_BG);
                    }
                    
                    String status = (String) getValueAt(row, 2);
                    if ("Delayed".equals(status)) {
                        comp.setBackground(new Color(255, 235, 238));
                    } else if ("Assigned".equals(status)) {
                        comp.setBackground(new Color(243, 232, 255));
                    } else if ("Picked Up".equals(status)) {
                        comp.setBackground(new Color(224, 242, 241));
                    } else if ("Out for Delivery".equals(status)) {
                        comp.setBackground(new Color(255, 243, 224));
                    } else if ("Failed".equals(status)) {
                        comp.setBackground(new Color(255, 235, 238));
                    }
                } else {
                    comp.setBackground(SELECTION_COLOR);
                }
                return comp;
            }
        };
        
        table.setRowHeight(45);
        table.setFont(REGULAR_FONT);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(80);
        table.getColumnModel().getColumn(9).setPreferredWidth(100);
        
        // Set custom renderers
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new AmountCellRenderer());
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showEnhancedOrderDetails();
                }
            }
        });
        
        populateTableData();
    }
    
    @Override
    protected void populateTableData() {
        if (tableModel == null) return;
        
        tableModel.setRowCount(0);
        storage.forceReload();
        
        for (Order o : storage.getAllOrders()) {
            tableModel.addRow(new Object[]{
                o.id,
                o.recipientName,
                o.status,
                o.orderDate,
                o.estimatedDelivery != null ? o.estimatedDelivery : "-",
                o.customerName,
                String.format("%.2f kg", o.weight),
                o.driverId != null ? o.driverId : "-",
                o.vehicleId != null ? o.vehicleId : "-",
                o.getFormattedEstimatedCost()
            });
        }
    }
    
    @Override
    protected void updateStats() {
        SwingUtilities.invokeLater(() -> {
            if (statValues != null) {
                if (statValues[0] != null) statValues[0].setText(String.valueOf(storage.getTotalCount()));
                if (statValues[1] != null) statValues[1].setText(String.valueOf(storage.getPendingCount()));
                if (statValues[2] != null) statValues[2].setText(String.valueOf(storage.getAssignedCount()));
                if (statValues[3] != null) statValues[3].setText(String.valueOf(storage.getPickupCount()));
                if (statValues[4] != null) statValues[4].setText(String.valueOf(storage.getInTransitCount()));
                if (statValues[5] != null) statValues[5].setText(String.valueOf(storage.getOutForDeliveryCount()));
                if (statValues[6] != null) statValues[6].setText(String.valueOf(storage.getDelayedCount()));
                if (statValues[7] != null) statValues[7].setText(String.valueOf(storage.getFailedCount()));
            }
            
            if (statsPanel != null) {
                statsPanel.revalidate();
                statsPanel.repaint();
            }
        });
    }
    
    @Override
    protected String[] getTableColumns() {
        return new String[]{"Order ID", "Recipient", "Status", "Order Date", "Est. Delivery", 
                            "Sender", "Weight", "Driver", "Vehicle", "Amount"};
    }
    
    @Override
    protected int[] getColumnWidths() {
        return new int[]{100, 150, 100, 120, 100, 150, 70, 80, 80, 100};
    }
    
    @Override
    protected void applyStatusFilter(String status, int cardIndex) {
        // Override to handle Order-specific status mapping
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentStatusFilter = null;
            currentFilterIndex = -1;
            currentStatusFilter = null;
            rowSorter.setRowFilter(null);
        } else {
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            
            if (statCards != null && cardIndex >= 0 && cardIndex < statCards.length && statCards[cardIndex] != null) {
                statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            
            applyFilters();
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
            
            if (statCards != null && cardIndex >= 0 && cardIndex < statCards.length && statCards[cardIndex] != null) {
                statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
                statCards[cardIndex].setBackground(color.brighter());
            }
            
            applyFilters();
        }
    }
    
    private void applyFilters() {
        if (currentStatusFilter == null) {
            rowSorter.setRowFilter(null);
            return;
        }
        
        String filterValue;
        switch(currentStatusFilter) {
            case "Pickup": filterValue = "Picked Up"; break;
            case "Out Delivery": filterValue = "Out for Delivery"; break;
            case "In Transit": filterValue = "In Transit"; break;
            case "Pending": filterValue = "Pending"; break;
            case "Assigned": filterValue = "Assigned"; break;
            case "Delayed": filterValue = "Delayed"; break;
            case "Delivered": filterValue = "Delivered"; break;
            case "Failed": filterValue = "Failed"; break;
            default: filterValue = currentStatusFilter;
        }
        
        final String finalFilterValue = filterValue;
        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String status = (String) entry.getValue(2);
                return finalFilterValue.equals(status);
            }
        });
    }
    
    private void clearAllFilters() {
        resetCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        rowSorter.setRowFilter(null);
    }
    
    @Override
    protected void clearFilters() {
        clearAllFilters();
    }
    
    @Override
    protected JPanel createButtonPanel() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        buttonPanel.setBackground(BG_COLOR);
        
        JButton viewBtn = createStyledButton("View Details", INFO, 100, 32);
        JButton addBtn = createStyledButton("Add Order", SUCCESS, 100, 32);
        JButton deleteBtn = createStyledButton("Delete", DANGER, 100, 32);
        JButton assignBtn = createStyledButton("Assign Driver", PRIMARY, 100, 32);
        
        viewBtn.addActionListener(e -> showEnhancedOrderDetails());
        addBtn.addActionListener(e -> showCreateOrderPanel());
        deleteBtn.addActionListener(e -> deleteOrder());
        assignBtn.addActionListener(e -> assignDriver());
        
        buttonPanel.add(viewBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(assignBtn);
        
        return buttonPanel;
    }
    
    @Override
    public int getTotalCount() {
        return storage.getTotalCount();
    }
    
    // ==================== CUSTOM CELL RENDERERS ====================
    
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
                    case "Pending":
                        label.setForeground(WARNING.darker());
                        label.setBackground(new Color(255, 243, 224));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(WARNING, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Assigned":
                        label.setForeground(ASSIGNED_COLOR);
                        label.setBackground(new Color(243, 232, 255));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(ASSIGNED_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Picked Up":
                        label.setForeground(PICKUP_COLOR);
                        label.setBackground(new Color(224, 242, 241));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(PICKUP_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "In Transit":
                        label.setForeground(INFO);
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(INFO, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Out for Delivery":
                        label.setForeground(OUT_FOR_DELIVERY_COLOR);
                        label.setBackground(new Color(255, 243, 224));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(OUT_FOR_DELIVERY_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Delayed":
                        label.setForeground(DANGER.darker());
                        label.setBackground(new Color(255, 235, 238));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DANGER, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Delivered":
                        label.setForeground(SUCCESS.darker());
                        label.setBackground(new Color(232, 245, 233));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(SUCCESS, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Failed":
                        label.setForeground(DANGER.darker());
                        label.setBackground(new Color(255, 235, 238));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DANGER, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Cancelled":
                        label.setForeground(TEXT_SECONDARY);
                        label.setBackground(new Color(245, 245, 245));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(TEXT_SECONDARY, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                }
            }
            return panel;
        }
    }
    
    private class AmountCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (!isSelected && value != null) {
                setForeground(SUCCESS);
            }
            return this;
        }
    }
    
    // ==================== CREATE ORDER PANEL ====================
    
    private void showCreateOrderPanel() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                      "Create New Order", true);
        dialog.setSize(950, 850);
        dialog.setLocationRelativeTo(this);
    
        CreateOrderPanel createPanel = new CreateOrderPanel(this);
    
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createPanel, BorderLayout.CENTER);
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
    
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
    
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    // ==================== DELETE ORDER ====================
    
    private void deleteOrder() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select an order to delete");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 2);
        
        if (!"Pending".equals(status) && !"Assigned".equals(status) && !"Delayed".equals(status) && !"Failed".equals(status)) {
            showWarning("Cannot delete orders that are " + status + ".\nOnly Pending, Assigned, Delayed, or Failed orders can be deleted.");
            return;
        }
        
        if (confirmAction("Are you sure you want to delete order " + id + "?", "Confirm Delete")) {
            storage.removeOrder(id);
            try {
                sender.SenderOrderRepository.getInstance().deleteOrder(id);
            } catch (Exception e) {}
            refreshTable();
            showSuccess("Order deleted successfully");
        }
    }
    
    // ==================== ASSIGN DRIVER ====================
    
    private void assignDriver() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select an order");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        boolean canAssign = "Pending".equals(order.status) || "Assigned".equals(order.status) || 
                           "Delayed".equals(order.status) || "Failed".equals(order.status);
        
        if (!canAssign) {
            showWarning("Only Pending, Assigned, Delayed, or Failed orders can be assigned to drivers.\nCurrent status: " + order.status);
            return;
        }
        
        if (driverManagement == null) {
            showWarning("Driver Management module not available.");
            return;
        }
        
        List<Driver> availableDrivers = driverManagement.getAvailableDriversWithVehicles();
        
        if (availableDrivers.isEmpty()) {
            List<Driver> allApproved = driverManagement.getAllDrivers();
            long driversWithoutVehicles = allApproved.stream()
                .filter(d -> "APPROVED".equals(d.approvalStatus))
                .filter(d -> d.vehicleId == null || d.vehicleId.isEmpty())
                .count();
                
            if (driversWithoutVehicles > 0) {
                showWarning("Found " + driversWithoutVehicles + " approved driver(s) without vehicles.\n\n" +
                    "Please assign vehicles to drivers first in Vehicle Management > Driver Actions.");
            } else {
                showWarning("No available drivers at the moment.\n\n" +
                    "Please ensure drivers are approved, have vehicles, and are marked as 'Available'.");
            }
            return;
        }
        
        if ("Failed".equals(order.status)) {
            if (!confirmAction("This order is currently marked as FAILED.\n\nReassigning will clear the failure reason and reset the order status to Assigned.\n\nDo you want to proceed?", "Reassign Failed Order")) {
                return;
            }
        }
        
        showDriverAndVehicleSelection(order, availableDrivers);
    }
    
    private void showDriverAndVehicleSelection(Order order, List<Driver> availableDrivers) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                      "Assign Driver to Order", true);
        dialog.setSize(550, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Assign Driver to Order: " + order.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        String statusInfo = "Current Status: " + order.status;
        if ("Failed".equals(order.status)) {
            statusInfo += " (Reason: " + (order.reason != null ? order.reason : "Not specified") + ")";
        }
        JLabel recipientLabel = new JLabel("Recipient: " + order.recipientName + " | " + statusInfo + " | Amount: " + order.getFormattedEstimatedCost());
        recipientLabel.setFont(REGULAR_FONT);
        recipientLabel.setForeground("Failed".equals(order.status) ? DANGER : TEXT_SECONDARY);
        headerPanel.add(recipientLabel, BorderLayout.SOUTH);
        
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Driver Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel driverLabel = new JLabel("Select Driver:*");
        driverLabel.setFont(HEADER_FONT);
        selectionPanel.add(driverLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String[] driverOptions = new String[availableDrivers.size()];
        for (int i = 0; i < availableDrivers.size(); i++) {
            Driver d = availableDrivers.get(i);
            String vehicleInfo = d.vehicleId != null ? "Vehicle: " + d.vehicleId : "No Vehicle";
            driverOptions[i] = String.format("%s - %s | %s", d.id, d.name, vehicleInfo);
        }
        JComboBox<String> driverCombo = new JComboBox<>(driverOptions);
        driverCombo.setFont(REGULAR_FONT);
        driverCombo.setPreferredSize(new Dimension(400, 35));
        selectionPanel.add(driverCombo, gbc);
        
        // Vehicle info
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel vehicleInfoLabel = new JLabel("Assigned Vehicle:");
        vehicleInfoLabel.setFont(HEADER_FONT);
        selectionPanel.add(vehicleInfoLabel, gbc);
        
        gbc.gridx = 1;
        JLabel vehicleDisplayLabel = new JLabel("-- Will be shown when driver selected --");
        vehicleDisplayLabel.setFont(REGULAR_FONT);
        vehicleDisplayLabel.setForeground(TEXT_SECONDARY);
        selectionPanel.add(vehicleDisplayLabel, gbc);
        
        driverCombo.addActionListener(e -> {
            int idx = driverCombo.getSelectedIndex();
            if (idx >= 0 && idx < availableDrivers.size()) {
                Driver d = availableDrivers.get(idx);
                if (d.vehicleId != null && !d.vehicleId.isEmpty()) {
                    if (vehicleManagement != null) {
                        VehicleManagement.Vehicle v = vehicleManagement.getVehicleById(d.vehicleId);
                        if (v != null) {
                            vehicleDisplayLabel.setText(d.vehicleId + " - " + v.model + " (" + v.numberPlate + ")");
                        } else {
                            vehicleDisplayLabel.setText(d.vehicleId);
                        }
                    } else {
                        vehicleDisplayLabel.setText(d.vehicleId);
                    }
                    vehicleDisplayLabel.setForeground(SUCCESS);
                } else {
                    vehicleDisplayLabel.setText("No vehicle assigned to this driver");
                    vehicleDisplayLabel.setForeground(WARNING);
                }
            }
        });
        
        // Estimated Delivery
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel dateLabel = new JLabel("Est. Delivery Date:");
        dateLabel.setFont(HEADER_FONT);
        selectionPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        SpinnerDateModel dateModel = new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(REGULAR_FONT);
        dateSpinner.setPreferredSize(new Dimension(400, 35));
        selectionPanel.add(dateSpinner, gbc);
        
        // Info panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        infoPanel.setBackground(CARD_BG);
        JLabel infoIcon = new JLabel("ℹ️");
        infoIcon.setFont(REGULAR_FONT);
        infoIcon.setForeground(INFO);
        String infoText = "The driver's assigned vehicle will be used for this delivery";
        if ("Failed".equals(order.status)) {
            infoText = "This order was previously FAILED. Reassigning will reset the order status to Assigned.";
        }
        JLabel infoLabel = new JLabel(infoText);
        infoLabel.setFont(SMALL_FONT);
        infoLabel.setForeground(TEXT_MUTED);
        infoPanel.add(infoIcon);
        infoPanel.add(infoLabel);
        selectionPanel.add(infoPanel, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        String btnText = "Failed".equals(order.status) ? "Reassign Driver" : "Assign Driver";
        JButton assignBtn = new JButton(btnText);
        assignBtn.setFont(BUTTON_FONT);
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setBackground("Failed".equals(order.status) ? new Color(255, 87, 34) : SUCCESS);
        assignBtn.setBorderPainted(false);
        assignBtn.setPreferredSize(new Dimension(130, 35));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        
        assignBtn.addActionListener(e -> {
            int idx = driverCombo.getSelectedIndex();
            if (idx < 0 || idx >= availableDrivers.size()) {
                showError("Please select a driver");
                return;
            }
            Driver selectedDriver = availableDrivers.get(idx);
            Date estimatedDate = (Date) dateSpinner.getValue();
            
            if (selectedDriver.vehicleId == null || selectedDriver.vehicleId.isEmpty()) {
                if (!confirmAction("Driver " + selectedDriver.name + " does not have a vehicle assigned.\n\nDo you still want to proceed?", "No Vehicle Assigned")) {
                    return;
                }
            }
            
            assignDriverToOrder(order, selectedDriver, estimatedDate);
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(assignBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(selectionPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void assignDriverToOrder(Order order, Driver driver, Date estimatedDate) {
        String oldStatus = order.status;
        order.driverId = driver.id;
        order.vehicleId = driver.vehicleId;
        order.status = "Assigned";
        order.estimatedDelivery = dateFormat.format(estimatedDate);
        
        if ("Failed".equals(oldStatus)) {
            order.reason = null;
            order.pickupTime = null;
            order.inTransitTime = null;
            order.outForDeliveryTime = null;
            order.deliveryTime = null;
            order.actualDelivery = null;
        }
        if ("Delayed".equals(oldStatus)) {
            order.reason = null;
        }
        
        driver.assignOrder(order.id);
        storage.updateOrder(order);
        
        DriverStorage driverStorage = new DriverStorage();
        driverStorage.updateDriver(driver);
        
        if (driverManagement != null) {
            driverManagement.updateDriverStatus(driver.id, "On Delivery");
            driverManagement.refreshData();
        }
        
        if (driver.vehicleId != null && vehicleManagement != null) {
            vehicleManagement.updateVehicleStatus(driver.vehicleId, "Active");
        }
        
        try {
            sender.SenderOrderRepository.getInstance().updateOrderStatus(order.id, "Assigned");
        } catch (Exception e) {}
        
        refreshTable();
        
        String reassignNote = "Failed".equals(oldStatus) ? "\n\nNote: This order was previously FAILED and has been reset." : "";
        showSuccess("✓ Order assigned successfully!\n\nOrder ID: " + order.id + "\nPrevious Status: " + oldStatus + 
            "\nNew Status: Assigned\nDriver: " + driver.name + " (" + driver.id + ")\n" +
            "Vehicle: " + (driver.vehicleId != null ? driver.vehicleId : "Not assigned") + 
            "\nEstimated Delivery: " + dateFormat.format(estimatedDate) + reassignNote +
            "\nAmount: " + order.getFormattedEstimatedCost());
    }
    
    // ==================== ENHANCED ORDER DETAILS ====================
    
    private void showEnhancedOrderDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select an order to view");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        Driver driver = order.driverId != null ? driverStorage.findDriver(order.driverId) : null;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                      "Complete Order Details - " + order.id, true);
        dialog.setSize(900, 800);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(CARD_BG);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Order Details: " + order.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY);
        titlePanel.add(titleLabel);
        
        JPanel statusBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBadgePanel.setBackground(CARD_BG);
        
        JLabel statusBadge = new JLabel("  " + order.status + "  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBadge.setBackground(getColorForStatus(order.status));
        statusBadge.setForeground(Color.WHITE);
        statusBadgePanel.add(statusBadge);
        
        titlePanel.add(statusBadgePanel);
        
        JLabel dateLabel = new JLabel("Order Date: " + order.orderDate);
        dateLabel.setFont(REGULAR_FONT);
        dateLabel.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(CARD_BG);
        
        tabbedPane.addTab("Order Info", createOrderInfoTab(order));
        tabbedPane.addTab("Customer & Recipient", createCustomerInfoTab(order));
        tabbedPane.addTab("Package & Timeline", createPackageAndTimelineTab(order));
        tabbedPane.addTab("Payment", createPaymentInfoTab(order));
        tabbedPane.addTab("Driver & Vehicle", createDriverInfoTab(order, driver));
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        if ("Failed".equals(order.status)) {
            JButton reassignBtn = new JButton("Reassign Driver");
            reassignBtn.setFont(BUTTON_FONT);
            reassignBtn.setForeground(Color.WHITE);
            reassignBtn.setBackground(new Color(255, 87, 34));
            reassignBtn.setBorderPainted(false);
            reassignBtn.setPreferredSize(new Dimension(130, 35));
            reassignBtn.addActionListener(e -> {
                dialog.dispose();
                assignDriver();
            });
            buttonPanel.add(reassignBtn);
        }
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createOrderInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        addDetailRow(panel, "Order ID:", order.id, gbc, row++);
        addDetailRow(panel, "Status:", order.status, gbc, row++);
        addDetailRow(panel, "Order Date:", order.orderDate, gbc, row++);
        addDetailRow(panel, "Estimated Delivery:", order.estimatedDelivery != null ? order.estimatedDelivery : "-", gbc, row++);
        addDetailRow(panel, "Order Amount:", order.getFormattedEstimatedCost(), gbc, row++);
        if (order.reason != null && !order.reason.isEmpty()) {
            addDetailRow(panel, "Reason:", order.reason, gbc, row++);
        }
        return panel;
    }
    
    private JPanel createCustomerInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel senderTitle = new JLabel("SENDER INFORMATION");
        senderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        senderTitle.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(senderTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Name:", order.customerName != null ? order.customerName : "-", gbc, row++);
        addDetailRow(panel, "Phone:", order.customerPhone != null ? order.customerPhone : "-", gbc, row++);
        addDetailRow(panel, "Email:", order.customerEmail != null ? order.customerEmail : "-", gbc, row++);
        addDetailRow(panel, "Address:", order.customerAddress != null ? order.customerAddress : "-", gbc, row++);
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        JLabel recipientTitle = new JLabel("RECIPIENT INFORMATION");
        recipientTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipientTitle.setForeground(SUCCESS);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(recipientTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Name:", order.recipientName != null ? order.recipientName : "-", gbc, row++);
        addDetailRow(panel, "Phone:", order.recipientPhone != null ? order.recipientPhone : "-", gbc, row++);
        addDetailRow(panel, "Address:", order.recipientAddress != null ? order.recipientAddress : "-", gbc, row++);
        
        return panel;
    }
    
    private JPanel createPackageAndTimelineTab(Order order) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel packagePanel = new JPanel(new GridBagLayout());
        packagePanel.setBackground(CARD_BG);
        packagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ORANGE), "Package Details",
            TitledBorder.LEFT, TitledBorder.TOP, HEADER_FONT, ORANGE));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        addDetailRow(packagePanel, "Weight:", order.getFormattedWeight(), gbc, row++);
        addDetailRow(packagePanel, "Dimensions:", order.dimensions + " cm", gbc, row++);
        addDetailRow(packagePanel, "Package Type:", extractPackageType(order.notes), gbc, row++);
        addDetailRow(packagePanel, "Order Amount:", order.getFormattedEstimatedCost(), gbc, row++);
        
        panel.add(packagePanel, BorderLayout.NORTH);
        
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(CARD_BG);
        timelinePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(INFO), "Delivery Timeline",
            TitledBorder.LEFT, TitledBorder.TOP, HEADER_FONT, INFO));
        
        // Order Created - Always completed
        timelinePanel.add(createTimelineEvent("Order Created", "Order has been placed", 
            order.orderDate != null ? order.orderDate : "-", true));
        timelinePanel.add(createTimelineConnector(hasValidTime(order.pickupTime)));
        
        // Picked Up
        boolean pickupCompleted = hasValidTime(order.pickupTime);
        timelinePanel.add(createTimelineEvent("Picked Up", "Package picked up by courier", 
            pickupCompleted ? formatDateTime(order.pickupTime) : "Not yet", 
            pickupCompleted));
        timelinePanel.add(createTimelineConnector(hasValidTime(order.inTransitTime)));
        
        // In Transit
        boolean inTransitCompleted = hasValidTime(order.inTransitTime);
        timelinePanel.add(createTimelineEvent("In Transit", "Package on the way", 
            inTransitCompleted ? formatDateTime(order.inTransitTime) : "Not yet", 
            inTransitCompleted));
        timelinePanel.add(createTimelineConnector(hasValidTime(order.outForDeliveryTime)));
        
        // Out for Delivery
        boolean outForDeliveryCompleted = order.outForDeliveryTime != null && order.outForDeliveryTime != "0" && hasValidTime(order.outForDeliveryTime);
        String outForDeliveryDisplay = outForDeliveryCompleted ? formatDateTime(order.outForDeliveryTime) : "Not yet";
        
        timelinePanel.add(createTimelineEvent("Out for Delivery", "Package out for delivery", 
            outForDeliveryDisplay, outForDeliveryCompleted));
        timelinePanel.add(createTimelineConnector("Delivered".equals(order.status)));
        
        // Delivered
        boolean deliveredCompleted = order.deliveryTime != null && order.deliveryTime != "0" && "Delivered".equals(order.status);
        String deliveryDisplay = deliveredCompleted && hasValidTime(order.deliveryTime) ? 
            formatDateTime(order.deliveryTime) : "Not yet";
        timelinePanel.add(createTimelineEvent("Delivered", "Package delivered successfully", 
            deliveryDisplay, deliveredCompleted));
        
        JScrollPane scrollPane = new JScrollPane(timelinePanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private boolean hasValidTime(String timeValue) {
        return timeValue != null && !timeValue.trim().isEmpty() && !"0".equals(timeValue.trim());
    }
    
    private String formatDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "-";
        try {
            if (dateTime.contains(" ")) {
                String[] parts = dateTime.split(" ");
                if (parts.length >= 2) {
                    String timePart = parts[1];
                    if (timePart.length() >= 5) {
                        return parts[0] + " " + timePart.substring(0, 5);
                    }
                    return parts[0] + " " + timePart;
                }
            }
            return dateTime;
        } catch (Exception e) {
            return dateTime;
        }
    }
    
    private JPanel createTimelineEvent(String title, String description, String time, boolean completed) {
        JPanel eventPanel = new JPanel(new BorderLayout(10, 0));
        eventPanel.setBackground(CARD_BG);
        eventPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setBackground(CARD_BG);
        
        JLabel eventLabel = new JLabel(title);
        eventLabel.setFont(new Font("Segoe UI", completed ? Font.BOLD : Font.PLAIN, 13));
        eventLabel.setForeground(completed ? SUCCESS : TEXT_SECONDARY);
        leftPanel.add(eventLabel);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        leftPanel.add(descLabel);
        
        eventPanel.add(leftPanel, BorderLayout.WEST);
        
        String displayTime = (time != null && !time.isEmpty() && !"Not yet".equals(time)) ? time : "-";
        JLabel timeLabel = new JLabel(displayTime);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(completed ? SUCCESS : TEXT_SECONDARY);
        eventPanel.add(timeLabel, BorderLayout.EAST);
        
        return eventPanel;
    }
    
    private JPanel createTimelineConnector(boolean nextStepCompleted) {
        JPanel connector = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(nextStepCompleted ? SUCCESS : new Color(206, 212, 218));
                g2d.setStroke(new BasicStroke(2));
                int x = 15;
                g2d.drawLine(x, 0, x, getHeight());
            }
        };
        connector.setPreferredSize(new Dimension(30, 25));
        connector.setBackground(CARD_BG);
        return connector;
    }
    
    private JPanel createPaymentInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel paymentTitle = new JLabel("PAYMENT INFORMATION");
        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentTitle.setForeground(PURPLE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(paymentTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Payment Status:", order.paymentStatus != null ? order.paymentStatus : "Pending", gbc, row++);
        addDetailRow(panel, "Payment Method:", order.paymentMethod != null ? order.paymentMethod : "-", gbc, row++);
        addDetailRow(panel, "Order Amount:", order.getFormattedEstimatedCost(), gbc, row++);
        
        return panel;
    }
    
    private JPanel createDriverInfoTab(Order order, Driver driver) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel driverTitle = new JLabel("DRIVER INFORMATION");
        driverTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        driverTitle.setForeground(ORANGE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(driverTitle, gbc);
        
        gbc.gridwidth = 1;
        
        if (driver != null) {
            addDetailRow(panel, "Driver ID:", driver.id, gbc, row++);
            addDetailRow(panel, "Driver Name:", driver.name, gbc, row++);
            addDetailRow(panel, "Driver Phone:", driver.phone, gbc, row++);
            addDetailRow(panel, "Driver Status:", driver.workStatus, gbc, row++);
        } else if (order.driverId != null) {
            addDetailRow(panel, "Driver ID:", order.driverId, gbc, row++);
        } else {
            addDetailRow(panel, "Driver:", "Not assigned", gbc, row++);
        }
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        JLabel vehicleTitle = new JLabel("VEHICLE INFORMATION");
        vehicleTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vehicleTitle.setForeground(SUCCESS);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(vehicleTitle, gbc);
        
        gbc.gridwidth = 1;
        
        if (order.vehicleId != null && !order.vehicleId.isEmpty()) {
            addDetailRow(panel, "Vehicle ID:", order.vehicleId, gbc, row++);
            if (vehicleManagement != null) {
                VehicleManagement.Vehicle vehicle = vehicleManagement.getVehicleById(order.vehicleId);
                if (vehicle != null) {
                    addDetailRow(panel, "Vehicle Model:", vehicle.model, gbc, row++);
                    addDetailRow(panel, "Vehicle Type:", vehicle.type, gbc, row++);
                    addDetailRow(panel, "Plate Number:", vehicle.numberPlate, gbc, row++);
                }
            }
        } else {
            addDetailRow(panel, "Vehicle:", "Not assigned", gbc, row++);
        }
        
        return panel;
    }
    
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(HEADER_FONT);
        labelComp.setForeground(TEXT_SECONDARY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value != null ? value : "-");
        valueComp.setFont(REGULAR_FONT);
        valueComp.setForeground(TEXT_PRIMARY);
        panel.add(valueComp, gbc);
    }
    
    private String extractPackageType(String notes) {
        if (notes == null || notes.isEmpty()) return "Standard";
        if (notes.contains("Package Type:")) {
            int start = notes.indexOf("Package Type:") + 13;
            int end = notes.indexOf(";", start);
            if (end == -1) end = notes.indexOf("|", start);
            if (end == -1) end = notes.length();
            String type = notes.substring(start, end).trim();
            return type.isEmpty() ? "Standard" : type;
        }
        return "Standard";
    }
    
    private Color getColorForStatus(String status) {
        switch(status) {
            case "Pending": return WARNING;
            case "Assigned": return ASSIGNED_COLOR;
            case "Picked Up": return PICKUP_COLOR;
            case "In Transit": return INFO;
            case "Out for Delivery": return OUT_FOR_DELIVERY_COLOR;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Failed": return DANGER;
            case "Cancelled": return TEXT_SECONDARY;
            default: return PRIMARY;
        }
    }
    
    @Override
    protected void refreshTable() {
        if (tableModel != null) {
            tableModel.setRowCount(0);
            populateTableData();
        }
        updateStats();
    }
    
    // ==================== PUBLIC METHODS ====================
    
    public JPanel getMainPanel() { 
        refreshTable();
        return this; 
    }
    
    public JPanel getRefreshedPanel() { 
        refreshTable(); 
        return this; 
    }
    
    public void refreshData() { 
        refreshTable(); 
    }
    
    public int getPendingCount() { return storage.getPendingCount(); }
    public int getFailedCount() { return storage.getFailedCount(); }
    public int getAssignedCount() { return storage.getAssignedCount(); }
    public int getPickupCount() { return storage.getPickupCount(); }
    public int getInTransitCount() { return storage.getInTransitCount(); }
    public int getOutForDeliveryCount() { return storage.getOutForDeliveryCount(); }
    public int getDelayedCount() { return storage.getDelayedCount(); }
    public int getDeliveredCount() { return storage.getDeliveredCount(); }
}