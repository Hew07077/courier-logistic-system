package admin.management;

import logistics.orders.Order;
import logistics.orders.OrderStorage;
import logistics.driver.Driver;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Timer;

public class OrderManagement {
    private JPanel mainPanel;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JPanel statsPanel;
    private JLabel[] statValues;
    private JPanel[] statCards;
    
    private OrderStorage storage;
    
    // References to other modules
    private DriverManagement driverManagement;
    private VehicleManagement vehicleManagement;
    
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
    
    // Filter state
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;
    
    public OrderManagement() {
        this(null, null);
    }
    
    public OrderManagement(DriverManagement driverMgmt) {
        this(driverMgmt, null);
    }
    
    public OrderManagement(DriverManagement driverMgmt, VehicleManagement vehicleMgmt) {
        this.driverManagement = driverMgmt;
        this.vehicleManagement = vehicleMgmt;
        storage = new OrderStorage();
        createMainPanel();
        refreshTable();
    }
    
    public void setDriverManagement(DriverManagement driverMgmt) {
        this.driverManagement = driverMgmt;
    }
    
    public void setVehicleManagement(VehicleManagement vehicleMgmt) {
        this.vehicleManagement = vehicleMgmt;
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_COLOR);
        
        // Top container with header and stats
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
        
        JLabel title = new JLabel("Order Management");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Manage and track all customer orders");
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        // Refresh button in header
        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(REGULAR_FONT);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(PRIMARY);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(100, 35));
        refreshBtn.addActionListener(e -> refreshTable());
        
        refreshBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                refreshBtn.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(MouseEvent e) {
                refreshBtn.setBackground(PRIMARY);
            }
        });
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(BG_COLOR);
        rightPanel.add(refreshBtn);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Total", "Pending", "In Transit", "Delayed", "Delivered"};
        String[] descriptions = {"All orders", "Awaiting assignment", "On the way", "Behind schedule", "Completed"};
        Color[] colors = {PRIMARY, WARNING, INFO, DANGER, SUCCESS};
        Color[] bgColors = {
            new Color(255, 245, 235),
            new Color(255, 243, 224),
            new Color(227, 242, 253),
            new Color(255, 235, 238),
            new Color(232, 245, 233)
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
        
        // Make filterable cards clickable (all except Total)
        if (index > 0) {
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
        } else {
            // Total card - clear all filters
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
        // Reset all card borders
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            // Clicking the same card again - clear filter
            currentStatusFilter = null;
            currentFilterIndex = -1;
            applyFilters();
        } else {
            // Apply new filter
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            
            // Highlight selected card
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            // Apply filters
            applyFilters();
        }
    }
    
    private void clearAllFilters() {
        resetCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        rowSorter.setRowFilter(null);
    }
    
    private void applyFilters() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        // Add status filter
        if (currentStatusFilter != null) {
            filters.add(RowFilter.regexFilter("^" + currentStatusFilter + "$", 2)); // Status column index
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
        scrollPane.setPreferredSize(new Dimension(1000, 450));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createTable() {
        String[] columns = {"Order ID", "Recipient", "Status", "Order Date", "Est. Delivery", 
                            "Sender", "Weight", "Driver", "Vehicle"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { 
                return false; 
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        ordersTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        comp.setBackground(new Color(252, 252, 253));
                    } else {
                        comp.setBackground(CARD_BG);
                    }
                    
                    // Highlight delayed orders
                    String status = (String) getValueAt(row, 2);
                    if ("Delayed".equals(status)) {
                        comp.setBackground(new Color(255, 235, 238));
                    }
                } else {
                    comp.setBackground(SELECTION_COLOR);
                }
                
                return comp;
            }
        };
        
        ordersTable.setRowHeight(45);
        ordersTable.setFont(REGULAR_FONT);
        ordersTable.setSelectionBackground(SELECTION_COLOR);
        ordersTable.setSelectionForeground(TEXT_PRIMARY);
        ordersTable.setShowGrid(true);
        ordersTable.setGridColor(BORDER_COLOR);
        ordersTable.setIntercellSpacing(new Dimension(10, 5));
        ordersTable.setFillsViewportHeight(true);
        ordersTable.setAutoCreateRowSorter(true);
        
        // Table header styling
        JTableHeader header = ordersTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        // Row sorter
        rowSorter = new TableRowSorter<>(tableModel);
        ordersTable.setRowSorter(rowSorter);
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(8).setPreferredWidth(80);
        
        // Set custom renderers
        ordersTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        
        // Double-click listener
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ordersTable.getSelectedRow() != -1) {
                    showOrderDetails();
                }
            }
        });
        
        refreshTable();
        
        return ordersTable;
    }
    
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JLabel label = new JLabel();
        
        public StatusCellRenderer() {
            panel.setOpaque(true);
            label.setFont(REGULAR_FONT);
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
                    case "In Transit":
                        label.setForeground(INFO.darker());
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(INFO, 1, true),
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
                }
            }
            
            return panel;
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("View Details", INFO, INFO_DARK, this::showOrderDetails),
            new ButtonConfig("Add Order", SUCCESS, SUCCESS_DARK, this::addOrder),
            new ButtonConfig("Edit", WARNING, WARNING_DARK, this::editOrder),
            new ButtonConfig("Delete", DANGER, DANGER_DARK, this::deleteOrder),
            new ButtonConfig("Assign Driver", PRIMARY, PRIMARY_DARK, this::assignDriver),
            new ButtonConfig("Mark Delivered", new Color(46, 204, 113), new Color(39, 174, 96), this::markAsDelivered)
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
        btn.setPreferredSize(new Dimension(100, 32));
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
    
    private void showOrderDetails() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order to view", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Order Details - " + order.id, true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(CARD_BG);
        
        // Title
        JLabel titleLabel = new JLabel("Order Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        
        JLabel idLabel = new JLabel(order.id);
        idLabel.setFont(REGULAR_FONT);
        idLabel.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_BG);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(idLabel, BorderLayout.SOUTH);
        
        // Details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(CARD_BG);
        
        // Status badge
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(CARD_BG);
        JLabel statusLabel = new JLabel(order.status);
        statusLabel.setFont(REGULAR_FONT);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        Color statusColor = getColorForStatus(order.status);
        statusLabel.setBackground(statusColor);
        statusLabel.setForeground(Color.WHITE);
        statusPanel.add(new JLabel("Status: "));
        statusPanel.add(statusLabel);
        detailsPanel.add(statusPanel);
        detailsPanel.add(Box.createVerticalStrut(15));
        
        // Sender Information
        detailsPanel.add(createDetailSection("Sender Information",
            new String[]{
                "Name: " + order.customerName,
                "Phone: " + order.customerPhone,
                "Email: " + order.customerEmail,
                "Address: " + order.customerAddress
            }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Recipient Information
        detailsPanel.add(createDetailSection("Recipient Information",
            new String[]{
                "Name: " + order.recipientName,
                "Phone: " + order.recipientPhone,
                "Address: " + order.recipientAddress
            }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Package Information
        detailsPanel.add(createDetailSection("Package Information",
            new String[]{
                "Weight: " + String.format("%.2f kg", order.weight),
                "Dimensions: " + order.dimensions + " cm"
            }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Delivery Information
        List<String> deliveryInfo = new ArrayList<>();
        deliveryInfo.add("Order Date: " + order.orderDate);
        if (order.estimatedDelivery != null) {
            deliveryInfo.add("Estimated Delivery: " + order.estimatedDelivery);
        }
        if (order.driverId != null && !order.driverId.isEmpty()) {
            deliveryInfo.add("Driver ID: " + order.driverId);
        }
        if (order.vehicleId != null && !order.vehicleId.isEmpty()) {
            deliveryInfo.add("Vehicle ID: " + order.vehicleId);
        }
        if (order.reason != null && !order.reason.isEmpty()) {
            deliveryInfo.add("Delay Reason: " + order.reason);
        }
        if (order.notes != null && !order.notes.isEmpty()) {
            deliveryInfo.add("Notes: " + order.notes);
        }
        
        detailsPanel.add(createDetailSection("Delivery Information", 
            deliveryInfo.toArray(new String[0])));
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
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
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
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
    
    private Color getColorForStatus(String status) {
        switch (status) {
            case "Pending": return WARNING;
            case "In Transit": return INFO;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            default: return PRIMARY;
        }
    }
    
    // ==================== ADD ORDER METHODS ====================
    
    private void addOrder() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Create New Order", true);
        dialog.setSize(650, 750);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        // Title
        JLabel titleLabel = new JLabel("Create New Order");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create a container for all form panels
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(CARD_BG);
        
        // Create panels and store references to components
        Map<String, JComponent> componentMap = new HashMap<>();
        
        // Sender Information Panel
        JPanel senderPanel = createInfoPanel("Sender Information", new String[]{
            "Full Name:", "Phone:", "Email:", "Address:"
        }, componentMap, "sender");
        formContainer.add(senderPanel);
        formContainer.add(Box.createVerticalStrut(15));
        
        // Recipient Information Panel
        JPanel recipientPanel = createInfoPanel("Recipient Information", new String[]{
            "Full Name:", "Phone:", "Address:"
        }, componentMap, "recipient");
        formContainer.add(recipientPanel);
        formContainer.add(Box.createVerticalStrut(15));
        
        // Package Information Panel
        JPanel packagePanel = createInfoPanel("Package Information", new String[]{
            "Weight (kg):", "Dimensions (LxWxH cm):"
        }, componentMap, "package");
        formContainer.add(packagePanel);
        
        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Create Order");
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
            if (validateAndSaveOrder(dialog, componentMap)) {
                dialog.dispose();
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JPanel createInfoPanel(String title, String[] labels, Map<String, JComponent> componentMap, String prefix) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY, 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            HEADER_FONT,
            PRIMARY
        ));
        
        JPanel gridPanel = new JPanel(new GridLayout(labels.length, 2, 10, 8));
        gridPanel.setBackground(CARD_BG);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (int i = 0; i < labels.length; i++) {
            String labelText = labels[i];
            JLabel label = new JLabel(labelText);
            label.setFont(HEADER_FONT);
            label.setForeground(TEXT_PRIMARY);
            
            if (labelText.contains("Address")) {
                JTextArea textArea = new JTextArea(3, 20);
                textArea.setFont(REGULAR_FONT);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
                JScrollPane scroll = new JScrollPane(textArea);
                scroll.setPreferredSize(new Dimension(250, 60));
                gridPanel.add(label);
                gridPanel.add(scroll);
                
                // Store in component map
                String key = prefix + "_address";
                componentMap.put(key, scroll);
            } else {
                JTextField textField = new JTextField(20);
                textField.setFont(REGULAR_FONT);
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
                gridPanel.add(label);
                gridPanel.add(textField);
                
                // Store in component map
                String fieldName = labelText.replace("*", "").replace(" ", "_").toLowerCase();
                if (fieldName.contains(":")) {
                    fieldName = fieldName.substring(0, fieldName.indexOf(":"));
                }
                String key = prefix + "_" + fieldName;
                componentMap.put(key, textField);
            }
        }
        
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private boolean validateAndSaveOrder(JDialog dialog, Map<String, JComponent> componentMap) {
        try {
            // Get all fields from component map
            JTextField senderNameField = (JTextField) componentMap.get("sender_full_name");
            JTextField senderPhoneField = (JTextField) componentMap.get("sender_phone");
            JTextField senderEmailField = (JTextField) componentMap.get("sender_email");
            JScrollPane senderAddressScroll = (JScrollPane) componentMap.get("sender_address");
            JTextArea senderAddressArea = (JTextArea) senderAddressScroll.getViewport().getView();
            
            JTextField recipientNameField = (JTextField) componentMap.get("recipient_full_name");
            JTextField recipientPhoneField = (JTextField) componentMap.get("recipient_phone");
            JScrollPane recipientAddressScroll = (JScrollPane) componentMap.get("recipient_address");
            JTextArea recipientAddressArea = (JTextArea) recipientAddressScroll.getViewport().getView();
            
            JTextField weightField = (JTextField) componentMap.get("package_weight_(kg)");
            JTextField dimensionsField = (JTextField) componentMap.get("package_dimensions_(lxwxh_cm)");
            
            // Check if any required fields are null
            if (senderNameField == null || senderPhoneField == null || senderEmailField == null || 
                senderAddressArea == null || recipientNameField == null || recipientPhoneField == null || 
                recipientAddressArea == null || weightField == null || dimensionsField == null) {
                
                JOptionPane.showMessageDialog(dialog, 
                    "Error initializing form fields. Please try again.", 
                    "System Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Get values
            String senderName = senderNameField.getText().trim();
            String senderPhone = senderPhoneField.getText().trim();
            String senderEmail = senderEmailField.getText().trim();
            String senderAddress = senderAddressArea.getText().trim();
            
            String recipientName = recipientNameField.getText().trim();
            String recipientPhone = recipientPhoneField.getText().trim();
            String recipientAddress = recipientAddressArea.getText().trim();
            
            String weightText = weightField.getText().trim();
            String dimensions = dimensionsField.getText().trim();
            
            // Validate required fields
            StringBuilder missingFields = new StringBuilder();
            if (senderName.isEmpty()) missingFields.append("- Sender Name\n");
            if (senderPhone.isEmpty()) missingFields.append("- Sender Phone\n");
            if (senderEmail.isEmpty()) missingFields.append("- Sender Email\n");
            if (senderAddress.isEmpty()) missingFields.append("- Sender Address\n");
            if (recipientName.isEmpty()) missingFields.append("- Recipient Name\n");
            if (recipientPhone.isEmpty()) missingFields.append("- Recipient Phone\n");
            if (recipientAddress.isEmpty()) missingFields.append("- Recipient Address\n");
            if (weightText.isEmpty()) missingFields.append("- Weight\n");
            if (dimensions.isEmpty()) missingFields.append("- Dimensions\n");
            
            if (missingFields.length() > 0) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please fill in all required fields:\n" + missingFields.toString(), 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Validate email format (basic)
            if (!senderEmail.contains("@") || !senderEmail.contains(".")) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a valid email address", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Validate phone number (basic - just check if it contains only digits and allowed characters)
            String phonePattern = "^[0-9\\-\\+\\s\\(\\)]+$";
            if (!senderPhone.matches(phonePattern)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Sender phone number contains invalid characters", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!recipientPhone.matches(phonePattern)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Recipient phone number contains invalid characters", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Validate weight
            double weight;
            try {
                weight = Double.parseDouble(weightText);
                if (weight <= 0) throw new NumberFormatException();
                if (weight > 1000) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Weight exceeds maximum limit (1000 kg)", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(dialog, 
                    "Weight must be a positive number", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Validate dimensions format (basic)
            if (!dimensions.matches("^[0-9xX\\s]+$")) {
                JOptionPane.showMessageDialog(dialog, 
                    "Dimensions should be in format: LxWxH (e.g., 30x20x15)", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Create order
            String orderId = storage.generateNewId();
            Order order = new Order(
                orderId,
                senderName,
                senderPhone,
                senderEmail,
                senderAddress,
                recipientName,
                recipientPhone,
                recipientAddress,
                weight,
                dimensions
            );
            
            storage.addOrder(order);
            refreshTable();
            
            JOptionPane.showMessageDialog(dialog, 
                "Order created successfully!\nOrder ID: " + orderId, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, 
                "Error creating order: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void editOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Edit Order - " + order.id, true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Edit Order");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        String[] fields = {"Status:", "Driver ID:", "Vehicle ID:", "Notes:", "Delay Reason:"};
        JComponent[] components = {
            createStatusCombo(order.status),
            new JTextField(order.driverId != null ? order.driverId : ""),
            new JTextField(order.vehicleId != null ? order.vehicleId : ""),
            createTextArea(order.notes),
            new JTextField(order.reason != null ? order.reason : "")
        };
        
        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            formPanel.add(new JLabel(fields[i]), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            if (components[i] instanceof JScrollPane) {
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weighty = 1.0;
                formPanel.add(components[i], gbc);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 0;
            } else {
                formPanel.add(components[i], gbc);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
            order.status = (String) ((JComboBox<?>) components[0]).getSelectedItem();
            order.driverId = ((JTextField) components[1]).getText().trim();
            if (order.driverId.isEmpty()) order.driverId = null;
            
            order.vehicleId = ((JTextField) components[2]).getText().trim();
            if (order.vehicleId.isEmpty()) order.vehicleId = null;
            
            order.notes = ((JTextArea) ((JScrollPane) components[3]).getViewport().getView()).getText().trim();
            if (order.notes.isEmpty()) order.notes = null;
            
            order.reason = ((JTextField) components[4]).getText().trim();
            if (order.reason.isEmpty()) order.reason = null;
            
            storage.updateOrder(order);
            refreshTable();
            JOptionPane.showMessageDialog(dialog, "Order updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
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
    
    private JComboBox<String> createStatusCombo(String currentStatus) {
        JComboBox<String> combo = new JComboBox<>(new String[]{"Pending", "In Transit", "Delayed", "Delivered"});
        combo.setSelectedItem(currentStatus);
        combo.setFont(REGULAR_FONT);
        combo.setPreferredSize(new Dimension(200, 30));
        return combo;
    }
    
    private JScrollPane createTextArea(String text) {
        JTextArea textArea = new JTextArea(text != null ? text : "", 3, 20);
        textArea.setFont(REGULAR_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(250, 60));
        return scroll;
    }
    
    private void deleteOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Are you sure you want to delete order " + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            storage.removeOrder(id);
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "Order deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Assign driver and vehicle to order with dropdown lists
     */
    private void assignDriver() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        if (!"Pending".equals(order.status) && !"Delayed".equals(order.status)) {
            JOptionPane.showMessageDialog(mainPanel, "Only pending or delayed orders can be assigned", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if DriverManagement is available
        if (driverManagement == null) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Driver Management module not available.", 
                "Module Unavailable", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get list of available drivers
        List<Driver> availableDrivers = driverManagement.getAvailableDrivers();
        
        if (availableDrivers.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel,
                "No available drivers at the moment.\nPlease try again later or check driver status.",
                "No Available Drivers",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show driver and vehicle selection dialog
        showDriverAndVehicleSelection(order, availableDrivers);
    }

    /**
     * Shows a dialog with driver and vehicle selection dropdowns
     */
    private void showDriverAndVehicleSelection(Order order, List<Driver> availableDrivers) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Assign Driver and Vehicle", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Assign Resources to Order: " + order.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY);
        
        JLabel recipientLabel = new JLabel("Recipient: " + order.recipientName);
        recipientLabel.setFont(REGULAR_FONT);
        recipientLabel.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(recipientLabel, BorderLayout.SOUTH);
        
        // Selection panel
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Driver selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel driverLabel = new JLabel("Select Driver:");
        driverLabel.setFont(HEADER_FONT);
        selectionPanel.add(driverLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        // Create driver dropdown with detailed information
        String[] driverOptions = new String[availableDrivers.size()];
        for (int i = 0; i < availableDrivers.size(); i++) {
            Driver d = availableDrivers.get(i);
            String vehicleInfo = d.vehicleId != null ? d.vehicleId : "No Vehicle Assigned";
            String ratingInfo = String.format("%.1f⭐", d.rating);
            driverOptions[i] = String.format("%s - %s | Current Vehicle: %s | Rating: %s | Deliveries: %d", 
                d.id, d.name, vehicleInfo, ratingInfo, d.totalDeliveries);
        }
        
        JComboBox<String> driverCombo = new JComboBox<>(driverOptions);
        driverCombo.setFont(REGULAR_FONT);
        driverCombo.setPreferredSize(new Dimension(380, 35));
        driverCombo.setBackground(Color.WHITE);
        driverCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        selectionPanel.add(driverCombo, gbc);
        
        // Vehicle selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel vehicleLabel = new JLabel("Select Vehicle:");
        vehicleLabel.setFont(HEADER_FONT);
        selectionPanel.add(vehicleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        // Get vehicles from VehicleManagement if available, otherwise use default list
        String[] vehicleOptions;
        
        // Try to get vehicles from VehicleManagement
        if (vehicleManagement != null && vehicleManagement.getAllVehicles() != null) {
            List<VehicleManagement.Vehicle> vehicles = vehicleManagement.getAllVehicles();
            vehicleOptions = new String[vehicles.size()];
            for (int i = 0; i < vehicles.size(); i++) {
                VehicleManagement.Vehicle v = vehicles.get(i);
                String status = v.status != null ? v.status : "Unknown";
                String driverInfo = v.driverName != null ? " | Driver: " + v.driverName : " | No Driver";
                vehicleOptions[i] = String.format("%s - %s (%s) | %s%s", 
                    v.id, v.model, v.type, v.fuelType, driverInfo);
            }
        } else {
            // Default vehicle options if VehicleManagement is not available
            vehicleOptions = new String[]{
                "TRK001 - Freightliner Cascadia (Diesel) | No Driver",
                "TRK002 - Peterbilt 579 (Diesel) | No Driver", 
                "VAN001 - Ford Transit (Gasoline) | No Driver",
                "VAN002 - Mercedes Sprinter (Diesel) | No Driver",
                "VAN003 - RAM ProMaster (Diesel) | No Driver",
                "CAR001 - Toyota Camry (Gasoline) | No Driver",
                "CAR002 - Honda Civic (Gasoline) | No Driver",
                "MTC001 - Harley Davidson (Gasoline) | No Driver"
            };
        }
        
        JComboBox<String> vehicleCombo = new JComboBox<>(vehicleOptions);
        vehicleCombo.setFont(REGULAR_FONT);
        vehicleCombo.setPreferredSize(new Dimension(380, 35));
        vehicleCombo.setBackground(Color.WHITE);
        vehicleCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        selectionPanel.add(vehicleCombo, gbc);
        
        // Estimated delivery date
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel dateLabel = new JLabel("Est. Delivery Date:");
        dateLabel.setFont(HEADER_FONT);
        selectionPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        // Date spinner for estimated delivery
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3); // Default to 3 days from now
        SpinnerDateModel dateModel = new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(REGULAR_FONT);
        dateSpinner.setPreferredSize(new Dimension(380, 35));
        dateSpinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        selectionPanel.add(dateSpinner, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton assignBtn = new JButton("Assign");
        assignBtn.setFont(BUTTON_FONT);
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setBackground(SUCCESS);
        assignBtn.setBorderPainted(false);
        assignBtn.setPreferredSize(new Dimension(100, 35));
        assignBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        assignBtn.addActionListener(e -> {
            int selectedDriverIndex = driverCombo.getSelectedIndex();
            if (selectedDriverIndex >= 0) {
                Driver selectedDriver = availableDrivers.get(selectedDriverIndex);
                String selectedVehicle = (String) vehicleCombo.getSelectedItem();
                String vehicleId = selectedVehicle.split(" - ")[0]; // Extract vehicle ID
                Date estimatedDate = (Date) dateSpinner.getValue();
                
                assignDriverAndVehicleToOrder(order, selectedDriver.id, vehicleId, estimatedDate);
                dialog.dispose();
            }
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

    /**
     * Assign driver and vehicle to order
     */
    private void assignDriverAndVehicleToOrder(Order order, String driverId, String vehicleId, Date estimatedDate) {
        order.driverId = driverId;
        order.vehicleId = vehicleId;
        order.status = "In Transit";
        
        // Update estimated delivery
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        order.estimatedDelivery = sdf.format(estimatedDate);
        
        storage.updateOrder(order);
        refreshTable();
        
        // Update driver status if DriverManagement is available
        if (driverManagement != null) {
            driverManagement.updateDriverStatus(driverId, "On Delivery");
        }
        
        JOptionPane.showMessageDialog(mainPanel, 
            "Driver " + driverId + " and vehicle " + vehicleId + " assigned to " + order.id + "\n" +
            "Estimated Delivery: " + sdf.format(estimatedDate), 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void markAsDelivered() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        if ("Delivered".equals(order.status)) {
            JOptionPane.showMessageDialog(mainPanel, "Order is already delivered", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Mark order " + order.id + " as delivered?",
            "Confirm Delivery",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            order.status = "Delivered";
            
            // Update driver status if DriverManagement is available
            if (driverManagement != null && order.driverId != null) {
                driverManagement.updateDriverStatus(order.driverId, "Available");
            }
            
            storage.updateOrder(order);
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "Order marked as delivered", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
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
                o.vehicleId != null ? o.vehicleId : "-"
            });
        }
        
        // Update statistics
        updateStats();
    }
    
    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            int total = storage.getTotalCount();
            int pending = storage.getPendingCount();
            int transit = storage.getInTransitCount();
            int delayed = storage.getDelayedCount();
            int delivered = storage.getDeliveredCount();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(total));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(pending));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(transit));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(delayed));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(delivered));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        });
    }
    
    // Public methods for AdminDashboard
    public JPanel getMainPanel() { 
        refreshTable(); // Ensure data is loaded when panel is requested
        return mainPanel; 
    }
    
    public JPanel getRefreshedPanel() { 
        refreshTable(); 
        return mainPanel; 
    }
    
    public void refreshData() { 
        refreshTable(); 
    }
    
    public int getPendingCount() { 
        return storage.getPendingCount(); 
    }
    
    public int getTotalCount() { 
        return storage.getTotalCount(); 
    }
    
    public int getInTransitCount() {
        return storage.getInTransitCount();
    }
    
    public int getDelayedCount() {
        return storage.getDelayedCount();
    }
    
    public int getDeliveredCount() {
        return storage.getDeliveredCount();
    }
    
    // ==================== INTEGRATION METHODS ====================
    
    /**
     * Assign driver to order (called by DriverManagement)
     */
    public boolean assignDriverToOrder(String orderId, String driverId) {
        Order order = storage.findOrder(orderId);
        if (order != null && ("Pending".equals(order.status) || "Delayed".equals(order.status))) {
            order.driverId = driverId;
            order.status = "In Transit";
            
            // Update estimated delivery
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 3);
            order.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            
            storage.updateOrder(order);
            refreshTable();
            return true;
        }
        return false;
    }
    
    /**
     * Get orders by driver (for driver schedule)
     */
    public List<Order> getOrdersByDriver(String driverId) {
        return storage.getAllOrders().stream()
            .filter(o -> driverId.equals(o.driverId))
            .collect(Collectors.toList());
    }
    
    /**
     * Get pending orders (for driver assignment)
     */
    public List<Order> getPendingOrders() {
        return storage.getAllOrders().stream()
            .filter(o -> "Pending".equals(o.status))
            .collect(Collectors.toList());
    }
    
    /**
     * Update order status (called by other modules)
     */
    public void updateOrderStatus(String orderId, String newStatus) {
        Order order = storage.findOrder(orderId);
        if (order != null) {
            order.status = newStatus;
            storage.updateOrder(order);
            refreshTable();
        }
    }
}