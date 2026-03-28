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
import java.util.stream.Collectors;

public class OrderManagement {
    private JPanel mainPanel;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JPanel statsPanel;
    private JLabel[] statValues;
    private JPanel[] statCards;
    
    private OrderStorage storage;
    private DriverStorage driverStorage;
    
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
    private static final Color PURPLE = new Color(111, 66, 193);
    private static final Color ORANGE = new Color(255, 87, 34);
    
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
    
    // Date formatters
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
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
        
        JLabel subtitle = new JLabel("Manage and track all customer orders with detailed information");
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
        scrollPane.setPreferredSize(new Dimension(1200, 450));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createTable() {
        // Expanded columns with more details
        String[] columns = {"Order ID", "Recipient", "Status", "Order Date", "Est. Delivery", 
                            "Actual Delivery", "Sender", "Weight", "Driver", "Vehicle", 
                            "Payment", "Distance", "Fuel", "On Time"};
        
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
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Order ID
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Recipient
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Order Date
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Est. Delivery
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Actual Delivery
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Sender
        ordersTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Weight
        ordersTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Driver
        ordersTable.getColumnModel().getColumn(9).setPreferredWidth(80);  // Vehicle
        ordersTable.getColumnModel().getColumn(10).setPreferredWidth(80); // Payment
        ordersTable.getColumnModel().getColumn(11).setPreferredWidth(70); // Distance
        ordersTable.getColumnModel().getColumn(12).setPreferredWidth(70); // Fuel
        ordersTable.getColumnModel().getColumn(13).setPreferredWidth(60); // On Time
        
        // Set custom renderers
        ordersTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        ordersTable.getColumnModel().getColumn(10).setCellRenderer(new PaymentStatusCellRenderer());
        ordersTable.getColumnModel().getColumn(13).setCellRenderer(new OnTimeCellRenderer());
        
        // Double-click listener
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ordersTable.getSelectedRow() != -1) {
                    showEnhancedOrderDetails();
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
    
    private class PaymentStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected && value != null) {
                String status = value.toString();
                if ("Paid".equals(status)) {
                    label.setForeground(SUCCESS);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if ("Pending".equals(status)) {
                    label.setForeground(WARNING);
                } else if ("Failed".equals(status)) {
                    label.setForeground(DANGER);
                }
            }
            
            return label;
        }
    }
    
    private class OnTimeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected && value != null) {
                String val = value.toString();
                if ("Yes".equals(val)) {
                    label.setForeground(SUCCESS);
                    label.setText("✅ Yes");
                } else if ("No".equals(val)) {
                    label.setForeground(DANGER);
                    label.setText("❌ No");
                } else {
                    label.setText("-");
                }
            }
            
            return label;
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("View Details", INFO, INFO_DARK, this::showEnhancedOrderDetails),
            new ButtonConfig("Add Order", SUCCESS, SUCCESS_DARK, this::addOrder),
            new ButtonConfig("Edit", WARNING, WARNING_DARK, this::editOrder),
            new ButtonConfig("Delete", DANGER, DANGER_DARK, this::deleteOrder),
            new ButtonConfig("Assign Driver", PRIMARY, PRIMARY_DARK, this::assignDriver),
            new ButtonConfig("Mark Delivered", new Color(46, 204, 113), new Color(39, 174, 96), this::markAsDelivered),
            new ButtonConfig("Payment", PURPLE, new Color(88, 53, 154), this::managePayment)
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
    
    /**
     * Enhanced order details view with comprehensive information
     */
    private void showEnhancedOrderDetails() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order to view", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        // Get driver details if available
        Driver driver = order.driverId != null ? driverStorage.findDriver(order.driverId) : null;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Complete Order Details - " + order.id, true);
        dialog.setSize(800, 750);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        // Header with status
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
        
        Color statusColor = getColorForStatus(order.status);
        statusBadge.setBackground(statusColor);
        statusBadge.setForeground(Color.WHITE);
        statusBadgePanel.add(statusBadge);
        
        if ("Delivered".equals(order.status)) {
            JLabel onTimeBadge = new JLabel(order.onTime ? "  ON TIME  " : "  LATE  ");
            onTimeBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            onTimeBadge.setOpaque(true);
            onTimeBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            onTimeBadge.setBackground(order.onTime ? SUCCESS : DANGER);
            onTimeBadge.setForeground(Color.WHITE);
            statusBadgePanel.add(Box.createHorizontalStrut(10));
            statusBadgePanel.add(onTimeBadge);
        }
        
        titlePanel.add(statusBadgePanel);
        
        JLabel dateLabel = new JLabel("Order Date: " + order.orderDate);
        dateLabel.setFont(REGULAR_FONT);
        dateLabel.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for organized information
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(CARD_BG);
        
        // Tab 1: Customer & Recipient Information
        JPanel customerTab = createCustomerInfoTab(order);
        tabbedPane.addTab("Customer & Recipient", customerTab);
        
        // Tab 2: Package & Delivery Details
        JPanel packageTab = createPackageInfoTab(order, driver);
        tabbedPane.addTab("Package & Delivery", packageTab);
        
        // Tab 3: Payment Information
        JPanel paymentTab = createPaymentInfoTab(order);
        tabbedPane.addTab("Payment", paymentTab);
        
        // Tab 4: Driver & Vehicle
        JPanel driverTab = createDriverInfoTab(order, driver);
        tabbedPane.addTab("Driver & Vehicle", driverTab);
        
        // Tab 5: Tracking Timeline
        JPanel timelineTab = createTimelineTab(order);
        tabbedPane.addTab("Timeline", timelineTab);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        if ("Pending".equals(order.status) || "Delayed".equals(order.status)) {
            JButton assignBtn = new JButton("Assign Driver");
            assignBtn.setFont(BUTTON_FONT);
            assignBtn.setForeground(Color.WHITE);
            assignBtn.setBackground(SUCCESS);
            assignBtn.setBorderPainted(false);
            assignBtn.setPreferredSize(new Dimension(120, 32));
            assignBtn.addActionListener(e -> {
                dialog.dispose();
                assignDriver();
            });
            buttonPanel.add(assignBtn);
        }
        
        if ("In Transit".equals(order.status)) {
            JButton deliverBtn = new JButton("Mark Delivered");
            deliverBtn.setFont(BUTTON_FONT);
            deliverBtn.setForeground(Color.WHITE);
            deliverBtn.setBackground(SUCCESS);
            deliverBtn.setBorderPainted(false);
            deliverBtn.setPreferredSize(new Dimension(120, 32));
            deliverBtn.addActionListener(e -> {
                dialog.dispose();
                markAsDelivered();
            });
            buttonPanel.add(deliverBtn);
        }
        
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
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
        
        // Sender Section
        JLabel senderTitle = new JLabel("📤 SENDER INFORMATION");
        senderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        senderTitle.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(senderTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Name:", order.customerName, gbc, row++);
        addDetailRow(panel, "Phone:", order.customerPhone, gbc, row++);
        addDetailRow(panel, "Email:", order.customerEmail, gbc, row++);
        addDetailRow(panel, "Address:", order.customerAddress, gbc, row++);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        // Recipient Section
        JLabel recipientTitle = new JLabel("📥 RECIPIENT INFORMATION");
        recipientTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipientTitle.setForeground(SUCCESS);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(recipientTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Name:", order.recipientName, gbc, row++);
        addDetailRow(panel, "Phone:", order.recipientPhone, gbc, row++);
        addDetailRow(panel, "Address:", order.recipientAddress, gbc, row++);
        
        return panel;
    }
    
    private JPanel createPackageInfoTab(Order order, Driver driver) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel packageTitle = new JLabel("📦 PACKAGE DETAILS");
        packageTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        packageTitle.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(packageTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Weight:", String.format("%.2f kg", order.weight), gbc, row++);
        addDetailRow(panel, "Dimensions:", order.dimensions + " cm", gbc, row++);
        
        // Extract package type from notes
        String packageType = extractFromNotes(order.notes, "Package Type:");
        if (packageType != null) {
            addDetailRow(panel, "Package Type:", packageType, gbc, row++);
        }
        
        String description = extractFromNotes(order.notes, "Description:");
        if (description != null) {
            addDetailRow(panel, "Description:", description, gbc, row++);
        }
        
        if (order.notes != null && !order.notes.isEmpty() && !order.notes.equals(extractFromNotes(order.notes, ""))) {
            addDetailRow(panel, "Full Notes:", order.notes, gbc, row++);
        }
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        // Delivery Information
        JLabel deliveryTitle = new JLabel("🚚 DELIVERY INFORMATION");
        deliveryTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deliveryTitle.setForeground(INFO);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(deliveryTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Order Date:", order.orderDate, gbc, row++);
        addDetailRow(panel, "Estimated Delivery:", order.estimatedDelivery != null ? order.estimatedDelivery : "-", gbc, row++);
        addDetailRow(panel, "Actual Delivery:", order.actualDelivery != null ? order.actualDelivery : "-", gbc, row++);
        addDetailRow(panel, "Pickup Time:", order.pickupTime != null ? order.pickupTime : "-", gbc, row++);
        addDetailRow(panel, "Delivery Time:", order.deliveryTime != null ? order.deliveryTime : "-", gbc, row++);
        
        if ("Delivered".equals(order.status)) {
            addDetailRow(panel, "On Time:", order.onTime ? "Yes" : "No", gbc, row++);
            addDetailRow(panel, "Distance:", String.format("%.1f km", order.distance), gbc, row++);
            addDetailRow(panel, "Fuel Used:", String.format("%.1f L", order.fuelUsed), gbc, row++);
            addDetailRow(panel, "Fuel Efficiency:", order.fuelUsed > 0 ? 
                        String.format("%.2f km/L", order.distance / order.fuelUsed) : "-", gbc, row++);
        }
        
        if (order.reason != null && !order.reason.isEmpty()) {
            addDetailRow(panel, "Delay/Cancel Reason:", order.reason, gbc, row++);
        }
        
        return panel;
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
        
        JLabel paymentTitle = new JLabel("💰 PAYMENT INFORMATION");
        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentTitle.setForeground(PURPLE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(paymentTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Payment Status:", order.paymentStatus != null ? order.paymentStatus : "Pending", gbc, row++);
        addDetailRow(panel, "Payment Method:", order.paymentMethod != null ? order.paymentMethod : "-", gbc, row++);
        addDetailRow(panel, "Transaction ID:", order.transactionId != null ? order.transactionId : "-", gbc, row++);
        addDetailRow(panel, "Payment Date:", order.paymentDate != null ? order.paymentDate : "-", gbc, row++);
        
        // Extract cost from notes
        String cost = extractCost(order.notes);
        addDetailRow(panel, "Order Cost:", cost, gbc, row++);
        
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
        
        JLabel driverTitle = new JLabel("👤 DRIVER INFORMATION");
        driverTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        driverTitle.setForeground(ORANGE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(driverTitle, gbc);
        
        gbc.gridwidth = 1;
        
        if (driver != null) {
            addDetailRow(panel, "Driver ID:", driver.id, gbc, row++);
            addDetailRow(panel, "Driver Name:", driver.name, gbc, row++);
            addDetailRow(panel, "Driver Phone:", driver.phone, gbc, row++);
            addDetailRow(panel, "Driver Email:", driver.email != null ? driver.email : "-", gbc, row++);
            addDetailRow(panel, "Driver Status:", driver.workStatus, gbc, row++);
            addDetailRow(panel, "Driver Rating:", driver.getFormattedRating(), gbc, row++);
        } else if (order.driverId != null) {
            addDetailRow(panel, "Driver ID:", order.driverId, gbc, row++);
            addDetailRow(panel, "Driver Status:", "Driver not found in database", gbc, row++);
        } else {
            addDetailRow(panel, "Driver:", "Not assigned", gbc, row++);
        }
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        JLabel vehicleTitle = new JLabel("🚗 VEHICLE INFORMATION");
        vehicleTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vehicleTitle.setForeground(SUCCESS);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(vehicleTitle, gbc);
        
        gbc.gridwidth = 1;
        
        if (order.vehicleId != null && !order.vehicleId.isEmpty()) {
            addDetailRow(panel, "Vehicle ID:", order.vehicleId, gbc, row++);
            
            // Try to get vehicle details from VehicleManagement if available
            if (vehicleManagement != null) {
                VehicleManagement.Vehicle vehicle = vehicleManagement.getVehicleById(order.vehicleId);
                if (vehicle != null) {
                    addDetailRow(panel, "Vehicle Model:", vehicle.model, gbc, row++);
                    addDetailRow(panel, "Vehicle Type:", vehicle.type, gbc, row++);
                    addDetailRow(panel, "Plate Number:", vehicle.numberPlate, gbc, row++);
                    addDetailRow(panel, "Fuel Type:", vehicle.fuelType, gbc, row++);
                    addDetailRow(panel, "Vehicle Status:", vehicle.status, gbc, row++);
                }
            }
        } else {
            addDetailRow(panel, "Vehicle:", "Not assigned", gbc, row++);
        }
        
        return panel;
    }
    
    private JPanel createTimelineTab(Order order) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel timelineTitle = new JLabel("📅 ORDER TIMELINE");
        timelineTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timelineTitle.setForeground(INFO);
        timelineTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timelineTitle);
        panel.add(Box.createVerticalStrut(15));
        
        // Order Created
        panel.add(createTimelineEvent(
            "Order Created", 
            order.customerName,
            order.orderDate,
            true
        ));
        panel.add(Box.createVerticalStrut(5));
        
        // Payment
        boolean paymentCompleted = "Paid".equals(order.paymentStatus);
        panel.add(createTimelineEvent(
            "Payment", 
            paymentCompleted ? "Payment completed" : "Payment " + (order.paymentStatus != null ? order.paymentStatus.toLowerCase() : "pending"),
            order.paymentDate != null ? order.paymentDate : (paymentCompleted ? "Date unknown" : "Not paid yet"),
            paymentCompleted
        ));
        panel.add(Box.createVerticalStrut(5));
        
        // Pickup
        boolean pickedUp = order.pickupTime != null;
        panel.add(createTimelineEvent(
            "Pickup", 
            pickedUp ? "Picked up by driver" : "Awaiting pickup",
            pickedUp ? order.pickupTime : "Not picked up",
            pickedUp
        ));
        panel.add(Box.createVerticalStrut(5));
        
        // In Transit
        boolean inTransit = "In Transit".equals(order.status) || "Delayed".equals(order.status) || "Delivered".equals(order.status);
        panel.add(createTimelineEvent(
            "In Transit", 
            inTransit ? "Package is on the way" : "Not in transit",
            order.estimatedDelivery != null ? "Est: " + order.estimatedDelivery : "-",
            inTransit
        ));
        panel.add(Box.createVerticalStrut(5));
        
        // Delivered
        boolean delivered = "Delivered".equals(order.status);
        String deliveryInfo = delivered ? 
            (order.onTime ? "Delivered on time" : "Delivered late") : 
            "Not delivered";
        panel.add(createTimelineEvent(
            "Delivered", 
            deliveryInfo,
            order.actualDelivery != null ? order.actualDelivery : "-",
            delivered
        ));
        
        return panel;
    }
    
    private JPanel createTimelineEvent(String event, String description, String time, boolean completed) {
        JPanel eventPanel = new JPanel(new BorderLayout(10, 0));
        eventPanel.setBackground(CARD_BG);
        eventPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setBackground(CARD_BG);
        
        String icon = completed ? "✅ " : "⏳ ";
        JLabel eventLabel = new JLabel(icon + event);
        eventLabel.setFont(new Font("Segoe UI", completed ? Font.BOLD : Font.PLAIN, 13));
        eventLabel.setForeground(completed ? SUCCESS : TEXT_SECONDARY);
        leftPanel.add(eventLabel);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        leftPanel.add(descLabel);
        
        eventPanel.add(leftPanel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_SECONDARY);
        eventPanel.add(timeLabel, BorderLayout.EAST);
        
        return eventPanel;
    }
    
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
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
    
    private String extractFromNotes(String notes, String prefix) {
        if (notes == null || !notes.contains(prefix)) return null;
        try {
            String[] parts = notes.split(prefix);
            if (parts.length > 1) {
                String result = parts[1];
                if (result.contains(" ")) {
                    result = result.substring(0, result.indexOf(" ")).trim();
                }
                return result;
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private String extractCost(String notes) {
        if (notes == null) return "RM --.--";
        
        if (notes.contains("Estimated Cost:")) {
            String[] parts = notes.split("Estimated Cost: ");
            if (parts.length > 1) {
                String[] costParts = parts[1].split(" ");
                return costParts[0].trim();
            }
        }
        return "RM --.--";
    }
    
    private Color getColorForStatus(String status) {
        switch (status) {
            case "Pending": return WARNING;
            case "In Transit": return INFO;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Cancelled": return TEXT_SECONDARY;
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
            "Full Name:*", "Phone:*", "Email:*", "Address:*"
        }, componentMap, "sender");
        formContainer.add(senderPanel);
        formContainer.add(Box.createVerticalStrut(15));
        
        // Recipient Information Panel
        JPanel recipientPanel = createInfoPanel("Recipient Information", new String[]{
            "Full Name:*", "Phone:*", "Address:*"
        }, componentMap, "recipient");
        formContainer.add(recipientPanel);
        formContainer.add(Box.createVerticalStrut(15));
        
        // Package Information Panel
        JPanel packagePanel = createInfoPanel("Package Information", new String[]{
            "Weight (kg):*", "Dimensions (LxWxH cm):*", "Package Type:", "Description:"
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
            
            if (labelText.contains("Address") || labelText.contains("Description")) {
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
                String key = prefix + "_" + labelText.toLowerCase().replace("*", "").replace(" ", "_").replace(":", "");
                componentMap.put(key, scroll);
            } else if (labelText.contains("Package Type")) {
                JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                    "Documents", "Electronics", "Clothing", "Fragile Items", 
                    "Books", "Food", "Other"
                });
                typeCombo.setFont(REGULAR_FONT);
                typeCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
                gridPanel.add(label);
                gridPanel.add(typeCombo);
                
                String key = prefix + "_type";
                componentMap.put(key, typeCombo);
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
                String fieldName = labelText.toLowerCase().replace("*", "").replace(" ", "_").replace(":", "");
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
            JComboBox<String> packageTypeCombo = (JComboBox<String>) componentMap.get("package_type");
            JScrollPane descScroll = (JScrollPane) componentMap.get("package_description");
            JTextArea descArea = (JTextArea) (descScroll != null ? descScroll.getViewport().getView() : null);
            
            // Check if required fields exist
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
            
            String packageType = packageTypeCombo != null ? (String) packageTypeCombo.getSelectedItem() : "Standard";
            String description = descArea != null ? descArea.getText().trim() : "";
            
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
            
            // Validate phone number (basic)
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
            
            // Add additional notes
            StringBuilder notes = new StringBuilder();
            notes.append("Package Type: ").append(packageType);
            if (!description.isEmpty()) {
                notes.append(" Description: ").append(description);
            }
            
            // Calculate estimated cost (simple calculation)
            double baseRate = 8.0;
            double weightRate = weight * 3.5;
            double total = baseRate + weightRate;
            notes.append(" Estimated Cost: RM ").append(String.format("%.2f", total));
            
            order.notes = notes.toString();
            order.paymentStatus = "Pending";
            
            storage.addOrder(order);
            refreshTable();
            
            JOptionPane.showMessageDialog(dialog, 
                "Order created successfully!\nOrder ID: " + orderId + "\nEstimated Cost: RM " + String.format("%.2f", total), 
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
            JOptionPane.showMessageDialog(mainPanel, "Please select an order to edit", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
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
        
        String[] fields = {"Status:", "Driver ID:", "Vehicle ID:", "Payment Status:", "Notes:", "Delay/Cancel Reason:"};
        JComponent[] components = {
            createStatusCombo(order.status),
            new JTextField(order.driverId != null ? order.driverId : ""),
            new JTextField(order.vehicleId != null ? order.vehicleId : ""),
            createPaymentStatusCombo(order.paymentStatus),
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
            
            order.paymentStatus = (String) ((JComboBox<?>) components[3]).getSelectedItem();
            
            order.notes = ((JTextArea) ((JScrollPane) components[4]).getViewport().getView()).getText().trim();
            if (order.notes.isEmpty()) order.notes = null;
            
            order.reason = ((JTextField) components[5]).getText().trim();
            if (order.reason.isEmpty()) order.reason = null;
            
            storage.updateOrder(order);
            refreshTable();
            JOptionPane.showMessageDialog(dialog, "Order updated successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
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
        JComboBox<String> combo = new JComboBox<>(new String[]{
            "Pending", "In Transit", "Delayed", "Delivered", "Cancelled"
        });
        combo.setSelectedItem(currentStatus);
        combo.setFont(REGULAR_FONT);
        combo.setPreferredSize(new Dimension(200, 30));
        return combo;
    }
    
    private JComboBox<String> createPaymentStatusCombo(String currentStatus) {
        JComboBox<String> combo = new JComboBox<>(new String[]{
            "Pending", "Paid", "Failed"
        });
        combo.setSelectedItem(currentStatus != null ? currentStatus : "Pending");
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
            JOptionPane.showMessageDialog(mainPanel, "Please select an order to delete", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(mainPanel, "Order deleted successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void assignDriver() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        if (!"Pending".equals(order.status) && !"Delayed".equals(order.status)) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Only pending or delayed orders can be assigned", 
                "Invalid Status", JOptionPane.WARNING_MESSAGE);
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
        
        showDriverAndVehicleSelection(order, availableDrivers);
    }

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
        
        // Get vehicles from VehicleManagement if available
        String[] vehicleOptions;
        
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
            // Default vehicle options
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
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
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
                String vehicleId = selectedVehicle.split(" - ")[0];
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

    private void assignDriverAndVehicleToOrder(Order order, String driverId, String vehicleId, Date estimatedDate) {
        order.driverId = driverId;
        order.vehicleId = vehicleId;
        order.status = "In Transit";
        
        // Update estimated delivery
        order.estimatedDelivery = dateFormat.format(estimatedDate);
        
        storage.updateOrder(order);
        refreshTable();
        
        // Update driver status if DriverManagement is available
        if (driverManagement != null) {
            driverManagement.updateDriverStatus(driverId, "On Delivery");
        }
        
        JOptionPane.showMessageDialog(mainPanel, 
            "Driver " + driverId + " and vehicle " + vehicleId + " assigned to " + order.id + "\n" +
            "Estimated Delivery: " + dateFormat.format(estimatedDate), 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void markAsDelivered() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        if ("Delivered".equals(order.status)) {
            JOptionPane.showMessageDialog(mainPanel, "Order is already delivered", 
                "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create dialog to enter delivery details
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Complete Delivery", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int gridy = 0;
        
        JLabel titleLabel = new JLabel("Mark Order as Delivered");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = gridy++; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        JLabel orderLabel = new JLabel("Order ID:");
        orderLabel.setFont(HEADER_FONT);
        gbc.gridx = 0; gbc.gridy = gridy;
        panel.add(orderLabel, gbc);
        
        JLabel orderValue = new JLabel(order.id);
        orderValue.setFont(REGULAR_FONT);
        orderValue.setForeground(PRIMARY);
        gbc.gridx = 1; gbc.gridy = gridy++;
        panel.add(orderValue, gbc);
        
        JLabel distanceLabel = new JLabel("Distance (km):*");
        distanceLabel.setFont(HEADER_FONT);
        gbc.gridx = 0; gbc.gridy = gridy;
        panel.add(distanceLabel, gbc);
        
        JTextField distanceField = new JTextField();
        distanceField.setFont(REGULAR_FONT);
        gbc.gridx = 1; gbc.gridy = gridy++;
        panel.add(distanceField, gbc);
        
        JLabel fuelLabel = new JLabel("Fuel Used (L):*");
        fuelLabel.setFont(HEADER_FONT);
        gbc.gridx = 0; gbc.gridy = gridy;
        panel.add(fuelLabel, gbc);
        
        JTextField fuelField = new JTextField();
        fuelField.setFont(REGULAR_FONT);
        gbc.gridx = 1; gbc.gridy = gridy++;
        panel.add(fuelField, gbc);
        
        JLabel onTimeLabel = new JLabel("Delivered On Time:");
        onTimeLabel.setFont(HEADER_FONT);
        gbc.gridx = 0; gbc.gridy = gridy;
        panel.add(onTimeLabel, gbc);
        
        JCheckBox onTimeCheck = new JCheckBox("Yes");
        onTimeCheck.setSelected(true);
        onTimeCheck.setFont(REGULAR_FONT);
        onTimeCheck.setBackground(CARD_BG);
        gbc.gridx = 1; gbc.gridy = gridy++;
        panel.add(onTimeCheck, gbc);
        
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(CARD_BG);
        
        JButton confirmBtn = new JButton("Confirm Delivery");
        confirmBtn.setFont(BUTTON_FONT);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(SUCCESS);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setPreferredSize(new Dimension(140, 35));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        
        panel.add(buttonPanel, gbc);
        
        confirmBtn.addActionListener(e -> {
            try {
                String distText = distanceField.getText().trim();
                String fuelText = fuelField.getText().trim();
                
                if (distText.isEmpty() || fuelText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Please enter distance and fuel used", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                double distance = Double.parseDouble(distText);
                double fuel = Double.parseDouble(fuelText);
                boolean onTime = onTimeCheck.isSelected();
                
                // Update order
                order.status = "Delivered";
                order.actualDelivery = dateFormat.format(new Date());
                order.deliveryTime = dateTimeFormat.format(new Date());
                order.distance = distance;
                order.fuelUsed = fuel;
                order.onTime = onTime;
                
                // Update driver statistics if driver assigned
                if (order.driverId != null && driverManagement != null) {
                    Driver driver = driverManagement.getDriverById(order.driverId);
                    if (driver != null) {
                        driver.completeOrder(order.id, onTime, distance, fuel);
                        driverManagement.refreshData();
                    }
                }
                
                storage.updateOrder(order);
                refreshTable();
                
                JOptionPane.showMessageDialog(dialog, 
                    "Order marked as delivered successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numbers for distance and fuel", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void managePayment() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select an order", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Order order = storage.findOrder(id);
        if (order == null) return;
        
        String[] options = {"Mark as Paid", "Mark as Failed", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainPanel,
            "Order: " + order.id + "\n" +
            "Current Payment Status: " + (order.paymentStatus != null ? order.paymentStatus : "Pending") + "\n\n" +
            "Select new payment status:",
            "Manage Payment",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) { // Paid
            String method = JOptionPane.showInputDialog(mainPanel, 
                "Enter payment method (Credit Card, PayPal, Bank Transfer, etc.):",
                "Credit Card");
            
            if (method != null && !method.trim().isEmpty()) {
                String transactionId = "TXN" + System.currentTimeMillis();
                String paymentDate = dateTimeFormat.format(new Date());
                
                order.paymentStatus = "Paid";
                order.paymentMethod = method;
                order.transactionId = transactionId;
                order.paymentDate = paymentDate;
                
                storage.updateOrder(order);
                refreshTable();
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "Payment marked as Paid\nTransaction ID: " + transactionId, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (choice == 1) { // Failed
            order.paymentStatus = "Failed";
            order.paymentMethod = null;
            storage.updateOrder(order);
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "Payment marked as Failed", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Order o : storage.getAllOrders()) {
            String paymentStatus = o.paymentStatus != null ? o.paymentStatus : "Pending";
            
            tableModel.addRow(new Object[]{
                o.id,
                o.recipientName,
                o.status,
                o.orderDate,
                o.estimatedDelivery != null ? o.estimatedDelivery : "-",
                o.actualDelivery != null ? o.actualDelivery : "-",
                o.customerName,
                String.format("%.2f kg", o.weight),
                o.driverId != null ? o.driverId : "-",
                o.vehicleId != null ? o.vehicleId : "-",
                paymentStatus,
                o.distance > 0 ? String.format("%.1f km", o.distance) : "-",
                o.fuelUsed > 0 ? String.format("%.1f L", o.fuelUsed) : "-",
                o.status.equals("Delivered") ? (o.onTime ? "Yes" : "No") : "-"
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
        refreshTable();
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
            order.estimatedDelivery = dateFormat.format(cal.getTime());
            order.pickupTime = dateTimeFormat.format(new Date());
            
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