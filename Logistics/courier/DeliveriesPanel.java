package courier;

import logistics.orders.Order;
import logistics.orders.OrderStorage;
import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveriesPanel extends JPanel {
    
    // Color Palette (matching original)
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(225, 173, 1);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color PURPLE = new Color(111, 66, 193);
    private static final Color ACTIVE_FILTER_BORDER = new Color(46, 125, 50);
    private static final Color ORANGE = new Color(255, 152, 0);
    private static final Color ASSIGNED_COLOR = new Color(111, 66, 193);
    private static final Color PICKUP_COLOR = new Color(0, 150, 136);
    private static final Color OUT_FOR_DELIVERY_COLOR = new Color(0, 123, 255);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color SELECTION_COLOR = new Color(232, 245, 233);
    
    // Components
    private JTable deliveriesTable;
    private DefaultTableModel deliveriesTableModel;
    private TableRowSorter<DefaultTableModel> deliveriesRowSorter;
    private JPanel[] statCards = new JPanel[8];
    private JLabel[] statValues = new JLabel[8];
    private int currentFilterIndex = -1;
    
    private List<Order> myOrders;
    private CourierDashboard parentDashboard;
    private DriverStorage driverStorage;
    
    public DeliveriesPanel(List<Order> orders, OrderStorage orderStorage, CourierDashboard parent) {
        this.myOrders = orders;
        this.parentDashboard = parent;
        this.driverStorage = new DriverStorage();
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }
    
    public void refreshData(List<Order> updatedOrders) {
        this.myOrders = updatedOrders;
        refreshDeliveriesTable();
    }
    
    private void initUI() {
        add(createDeliveriesHeaderPanel(), BorderLayout.NORTH);
        add(createDeliveriesStatsPanel(), BorderLayout.CENTER);
        add(createDeliveriesCenterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createDeliveriesHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("My Deliveries");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY_GREEN);
        
        JLabel subtitle = new JLabel("Track and manage your assigned deliveries");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createDeliveriesStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 8, 12, 0));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Total Orders", "In Transit", "Pending", "Picked Up", "Out for Delivery", "Delivered", "Delayed", "Failed"};
        Color[] colors = {PRIMARY_GREEN, INFO, WARNING, PURPLE, OUT_FOR_DELIVERY_COLOR, SUCCESS, DANGER, DANGER};
        Color[] bgColors = {new Color(245, 250, 245), new Color(227, 242, 253), 
            new Color(255, 243, 224), new Color(240, 235, 255), new Color(220, 240, 255),
            new Color(232, 245, 233), new Color(255, 235, 238), new Color(255, 220, 220)};
        
        for (int i = 0; i < 8; i++) {
            JPanel card = createClickableStatCard(titles[i], "0", colors[i], bgColors[i], i);
            statCards[i] = card;
            statsPanel.add(card);
        }
        
        return statsPanel;
    }
    
    private JPanel createClickableStatCard(String title, String value, 
                                            Color color, Color bgColor, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        
        statValues[index] = valueLabel;
        
        if (index >= 1 && index <= 7) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final String filterStatus = title;
            final int cardIndex = index;
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(bgColor);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(color, 1, true),
                            BorderFactory.createEmptyBorder(7, 11, 7, 11)));
                    }
                }
                
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(Color.WHITE);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                    }
                }
                
                public void mouseClicked(MouseEvent e) {
                    applyDeliveriesStatusFilter(filterStatus, cardIndex, color);
                }
            });
        } else if (index == 0) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != index) {
                        card.setBackground(new Color(245, 250, 245));
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != index) {
                        card.setBackground(Color.WHITE);
                    }
                }
                public void mouseClicked(MouseEvent e) {
                    clearDeliveriesFilters();
                }
            });
        }
        
        return card;
    }
    
    private void resetDeliveriesCardBorders() {
        for (int i = 0; i < statCards.length; i++) {
            statCards[i].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            statCards[i].setBackground(Color.WHITE);
        }
    }
    
    private void applyDeliveriesStatusFilter(String status, int cardIndex, Color color) {
        resetDeliveriesCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentFilterIndex = -1;
            deliveriesRowSorter.setRowFilter(null);
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            statCards[cardIndex].setBackground(Color.WHITE);
        } else {
            currentFilterIndex = cardIndex;
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            statCards[cardIndex].setBackground(color.brighter());
            
            String filterValue;
            switch (status) {
                case "In Transit": filterValue = "In Transit"; break;
                case "Pending": filterValue = "Pending"; break;
                case "Picked Up": filterValue = "Picked Up"; break;
                case "Out for Delivery": filterValue = "Out for Delivery"; break;
                case "Delivered": filterValue = "Delivered"; break;
                case "Delayed": filterValue = "Delayed"; break;
                case "Failed": filterValue = "Failed"; break;
                default: filterValue = status; break;
            }
            deliveriesRowSorter.setRowFilter(RowFilter.regexFilter("^" + filterValue + "$", 3));
        }
    }
    
    private void clearDeliveriesFilters() {
        resetDeliveriesCardBorders();
        currentFilterIndex = -1;
        deliveriesRowSorter.setRowFilter(null);
    }
    
    private JPanel createDeliveriesCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_LIGHT);
        panel.add(createDeliveriesTablePanel(), BorderLayout.CENTER);
        panel.add(createDeliveriesButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    
    private JPanel createDeliveriesTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JScrollPane scrollPane = new JScrollPane(createDeliveriesTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(1000, 450));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createDeliveriesTable() {
        String[] columns = {"Order ID", "Recipient", "Phone", "Status", "Pickup Time", "Est. Delivery", "Address", "Weight"};
        deliveriesTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        deliveriesTable = new JTable(deliveriesTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE);
                } else {
                    comp.setBackground(SELECTION_COLOR);
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        };
        
        deliveriesTable.setRowHeight(48);
        deliveriesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deliveriesTable.setSelectionBackground(SELECTION_COLOR);
        deliveriesTable.setShowGrid(true);
        deliveriesTable.setGridColor(BORDER_COLOR);
        deliveriesTable.setIntercellSpacing(new Dimension(10, 5));
        deliveriesTable.setFillsViewportHeight(true);
        deliveriesTable.setAutoCreateRowSorter(false);
        
        JTableHeader header = deliveriesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(new Color(108, 117, 125));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN));
        header.setReorderingAllowed(false);
        
        deliveriesRowSorter = new TableRowSorter<>(deliveriesTableModel);
        deliveriesTable.setRowSorter(deliveriesRowSorter);
        
        deliveriesTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        deliveriesTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        deliveriesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        deliveriesTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        deliveriesTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        deliveriesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        deliveriesTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        deliveriesTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        
        deliveriesTable.getColumnModel().getColumn(3).setCellRenderer(new DeliveriesStatusCellRenderer());
        deliveriesTable.getColumnModel().getColumn(7).setCellRenderer(new WeightCellRenderer());
        
        deliveriesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && deliveriesTable.getSelectedRow() != -1) {
                    int row = deliveriesTable.convertRowIndexToModel(deliveriesTable.getSelectedRow());
                    if (row >= 0 && row < myOrders.size() && parentDashboard != null) {
                        showEnhancedOrderDetails(myOrders.get(row));
                    }
                }
            }
        });
        
        refreshDeliveriesTable();
        return deliveriesTable;
    }
    
    // ==================== ENHANCED ORDER DETAILS DIALOG (Like Admin) ====================
    
    private void showEnhancedOrderDetails(Order order) {
        Driver driver = order.driverId != null ? driverStorage.findDriver(order.driverId) : null;
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                      "Complete Order Details - " + order.id, true);
        dialog.setSize(900, 800);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(CARD_BG);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Order Details: " + order.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_GREEN);
        titlePanel.add(titleLabel);
        
        JPanel statusBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBadgePanel.setBackground(CARD_BG);
        
        JLabel statusBadge = new JLabel("  " + order.getCourierStatus() + "  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBadge.setBackground(getStatusColorForBadge(order.getCourierStatus()));
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
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(CARD_BG);
        
        tabbedPane.addTab("Order Info", createOrderInfoTab(order));
        tabbedPane.addTab("Customer & Recipient", createCustomerInfoTab(order));
        tabbedPane.addTab("Package & Timeline", createPackageAndTimelineTab(order));
        tabbedPane.addTab("Payment", createPaymentInfoTab(order));
        tabbedPane.addTab("Driver & Vehicle", createDriverInfoTab(order, driver));
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BG);
        
        if (!"Delivered".equals(order.status) && !"Failed".equals(order.status)) {
            JButton updateBtn = new JButton("Update Status");
            updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            updateBtn.setForeground(Color.WHITE);
            updateBtn.setBackground(SUCCESS);
            updateBtn.setBorderPainted(false);
            updateBtn.setPreferredSize(new Dimension(120, 35));
            updateBtn.addActionListener(e -> {
                dialog.dispose();
                if (parentDashboard != null) {
                    parentDashboard.switchToUpdatePanel(order);
                }
            });
            buttonPanel.add(updateBtn);
        }
        
        if ("Failed".equals(order.status)) {
            JButton reassignBtn = new JButton("Reassign Driver");
            reassignBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            reassignBtn.setForeground(Color.WHITE);
            reassignBtn.setBackground(new Color(255, 87, 34));
            reassignBtn.setBorderPainted(false);
            reassignBtn.setPreferredSize(new Dimension(130, 35));
            reassignBtn.addActionListener(e -> {
                dialog.dispose();
                // Notify admin or handle reassign
                JOptionPane.showMessageDialog(dialog, 
                    "Please contact admin to reassign this order.\nOrder ID: " + order.id,
                    "Reassign Required", JOptionPane.INFORMATION_MESSAGE);
            });
            buttonPanel.add(reassignBtn);
        }
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY_GREEN);
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
        
        JLabel basicTitle = new JLabel("BASIC INFORMATION");
        basicTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        basicTitle.setForeground(PRIMARY_GREEN);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(basicTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Order ID:", order.id, gbc, row++);
        addDetailRow(panel, "Status:", order.getCourierStatus(), gbc, row++, getStatusColor(order.getCourierStatus()));
        addDetailRow(panel, "Order Date:", order.orderDate, gbc, row++);
        addDetailRow(panel, "Estimated Delivery:", order.estimatedDelivery != null ? order.estimatedDelivery : "-", gbc, row++);
        addDetailRow(panel, "Order Amount:", order.getFormattedEstimatedCost(), gbc, row++, SUCCESS);
        
        if ("Delivered".equals(order.status) && order.actualDelivery != null) {
            addDetailRow(panel, "Actual Delivery:", order.actualDelivery, gbc, row++, SUCCESS);
            if (order.deliveryTime != null) {
                addDetailRow(panel, "Delivery Time:", order.deliveryTime.substring(11, 16), gbc, row++);
            }
        }
        
        if ("Failed".equals(order.status)) {
            addDetailRow(panel, "Failed Date:", order.deliveryTime != null ? order.deliveryTime : order.actualDelivery, gbc, row++, DANGER);
            addDetailRow(panel, "Failure Reason:", order.reason != null ? order.reason : "-", gbc, row++, DANGER);
        }
        
        if (order.reason != null && !order.reason.isEmpty() && !"Failed".equals(order.status)) {
            addDetailRow(panel, "Reason:", order.reason, gbc, row++, WARNING);
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
        senderTitle.setForeground(PRIMARY_GREEN);
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
        
        // Package Details Panel
        JPanel packagePanel = new JPanel(new GridBagLayout());
        packagePanel.setBackground(CARD_BG);
        packagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ORANGE), "Package Details",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), ORANGE));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        addDetailRow(packagePanel, "Weight:", order.getFormattedWeight(), gbc, row++);
        addDetailRow(packagePanel, "Dimensions:", order.dimensions + " cm", gbc, row++);
        addDetailRow(packagePanel, "Package Type:", extractPackageType(order.notes), gbc, row++);
        
        panel.add(packagePanel, BorderLayout.NORTH);
        
        // Timeline Panel
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(CARD_BG);
        timelinePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(INFO), "Delivery Timeline",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), INFO));
        
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
        boolean outForDeliveryCompleted = hasValidTime(order.outForDeliveryTime);
        timelinePanel.add(createTimelineEvent("Out for Delivery", "Package out for delivery", 
            outForDeliveryCompleted ? formatDateTime(order.outForDeliveryTime) : "Not yet", 
            outForDeliveryCompleted));
        timelinePanel.add(createTimelineConnector("Delivered".equals(order.status)));
        
        // Delivered
        boolean deliveredCompleted = "Delivered".equals(order.status);
        timelinePanel.add(createTimelineEvent("Delivered", "Package delivered successfully", 
            deliveredCompleted && hasValidTime(order.deliveryTime) ? formatDateTime(order.deliveryTime) : "Not yet", 
            deliveredCompleted));
        
        JScrollPane timelineScroll = new JScrollPane(timelinePanel);
        timelineScroll.setBorder(null);
        timelineScroll.getVerticalScrollBar().setUnitIncrement(16);
        timelineScroll.setPreferredSize(new Dimension(500, 300));
        
        panel.add(timelineScroll, BorderLayout.CENTER);
        
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
        
        JLabel paymentTitle = new JLabel("PAYMENT INFORMATION");
        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentTitle.setForeground(PURPLE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(paymentTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Payment Status:", order.paymentStatus != null ? order.paymentStatus : "Pending", gbc, row++, 
                     "Paid".equals(order.paymentStatus) ? SUCCESS : WARNING);
        addDetailRow(panel, "Payment Method:", order.paymentMethod != null ? order.paymentMethod : "-", gbc, row++);
        addDetailRow(panel, "Order Amount:", order.getFormattedEstimatedCost(), gbc, row++, SUCCESS);
        
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
            addDetailRow(panel, "Driver Phone:", driver.phone != null ? driver.phone : "-", gbc, row++);
            addDetailRow(panel, "Driver Status:", driver.workStatus != null ? driver.workStatus : "-", gbc, row++, 
                         "On Delivery".equals(driver.workStatus) ? INFO : TEXT_SECONDARY);
        } else if (order.driverId != null && !order.driverId.isEmpty()) {
            addDetailRow(panel, "Driver ID:", order.driverId, gbc, row++);
            addDetailRow(panel, "Driver Name:", "Not Found", gbc, row++, TEXT_SECONDARY);
        } else {
            addDetailRow(panel, "Driver:", "Not assigned", gbc, row++, TEXT_SECONDARY);
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
            if (driver != null && driver.vehicleId != null && driver.vehicleId.equals(order.vehicleId)) {
                addDetailRow(panel, "Vehicle:", "Assigned to driver", gbc, row++, SUCCESS);
            } else {
                addDetailRow(panel, "Vehicle:", "Assigned", gbc, row++);
            }
        } else {
            addDetailRow(panel, "Vehicle:", "Not assigned", gbc, row++, TEXT_SECONDARY);
        }
        
        return panel;
    }
    
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int y) {
        addDetailRow(panel, label, value, gbc, y, null);
    }
    
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int y, Color valueColor) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setForeground(TEXT_SECONDARY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value != null ? value : "-");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (valueColor != null) {
            valueComp.setForeground(valueColor);
        } else {
            valueComp.setForeground(TEXT_PRIMARY);
        }
        panel.add(valueComp, gbc);
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
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_SECONDARY);
        leftPanel.add(descLabel);
        
        eventPanel.add(leftPanel, BorderLayout.WEST);
        
        String displayTime = (time != null && !time.isEmpty() && !"Not yet".equals(time)) ? time : "-";
        JLabel timeLabel = new JLabel(displayTime);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
    
    private Color getStatusColor(String status) {
        switch(status) {
            case "Pending": return WARNING;
            case "Assigned": return ASSIGNED_COLOR;
            case "Picked Up": return PICKUP_COLOR;
            case "In Transit": return INFO;
            case "Out for Delivery": return OUT_FOR_DELIVERY_COLOR;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Failed": return DANGER;
            default: return PRIMARY_GREEN;
        }
    }
    
    private Color getStatusColorForBadge(String status) {
        switch(status) {
            case "Pending": return WARNING;
            case "Picked Up": return PURPLE;
            case "Out for Delivery": return OUT_FOR_DELIVERY_COLOR;
            case "In Transit": return INFO;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Failed": return DANGER;
            case "Cancelled": return TEXT_GRAY;
            default: return PRIMARY_GREEN;
        }
    }
    
    private class DeliveriesStatusCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JLabel label = new JLabel();
        
        public DeliveriesStatusCellRenderer() {
            panel.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            panel.add(label);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            panel.setBackground(isSelected ? SELECTION_COLOR : 
                               (row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE));
            
            if (value != null) {
                String status = value.toString();
                label.setText(status);
                label.setOpaque(true);
                
                switch (status) {
                    case "Pending":
                        label.setForeground(new Color(150, 100, 0));
                        label.setBackground(new Color(255, 243, 224));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(WARNING, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "In Transit":
                        label.setForeground(new Color(13, 110, 130));
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(INFO, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Delivered":
                        label.setForeground(new Color(0, 100, 0));
                        label.setBackground(new Color(232, 245, 233));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(SUCCESS, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Delayed":
                        label.setForeground(new Color(150, 20, 30));
                        label.setBackground(new Color(255, 235, 238));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DANGER, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Picked Up":
                        label.setForeground(new Color(70, 40, 120));
                        label.setBackground(new Color(240, 235, 255));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(PURPLE, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Out for Delivery":
                        label.setForeground(new Color(0, 80, 150));
                        label.setBackground(new Color(220, 240, 255));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(OUT_FOR_DELIVERY_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Failed":
                        label.setForeground(new Color(150, 0, 0));
                        label.setBackground(new Color(255, 220, 220));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DANGER, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    default:
                        label.setForeground(Color.BLACK);
                        label.setBackground(Color.WHITE);
                        label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                        break;
                }
            }
            return panel;
        }
    }
    
    private class WeightCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setBackground(isSelected ? SELECTION_COLOR : 
                         (row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE));
            return this;
        }
    }
    
    private void refreshDeliveriesTable() {
        deliveriesTableModel.setRowCount(0);
    
        for (Order o : myOrders) {
            String pickupTime = "-";
            if (o.pickupTime != null && o.pickupTime.length() >= 16) {
                pickupTime = o.pickupTime.substring(11, 16);
            } else if (o.pickupTime != null) {
                pickupTime = o.pickupTime;
            }
        
            String estDelivery = "-";
            if (o.estimatedDelivery != null && o.estimatedDelivery.length() >= 16) {
                estDelivery = o.estimatedDelivery.substring(11, 16);
            } else if (o.estimatedDelivery != null) {
                estDelivery = o.estimatedDelivery;
            }
        
            String address = o.recipientAddress;
            if (address != null && address.length() > 25) {
                address = address.substring(0, 22) + "...";
            } else if (address == null) {
                address = "-";
            }
        
            String courierStatus = o.getCourierStatus();
        
            deliveriesTableModel.addRow(new Object[]{
                o.id, 
                o.recipientName != null ? o.recipientName : "-",
                o.recipientPhone != null ? o.recipientPhone : "-",
                courierStatus,
                pickupTime, 
                estDelivery, 
                address,
                String.format("%.1f kg", o.weight)
            });
        }
    
        if (myOrders.isEmpty()) {
            deliveriesTableModel.addRow(new Object[]{
                "No orders assigned", "-", "-", "-", "-", "-", "-", "-"
            });
        }
    
        updateDeliveriesStats();
    }
    
    private void updateDeliveriesStats() {
        SwingUtilities.invokeLater(() -> {
            int total = myOrders.size();
            int inTransit = (int) myOrders.stream().filter(o -> "In Transit".equals(o.status)).count();
            int pending = (int) myOrders.stream().filter(o -> "Pending".equals(o.status)).count();
            int pickedUp = (int) myOrders.stream().filter(o -> "Picked Up".equals(o.status)).count();
            int outForDelivery = (int) myOrders.stream().filter(o -> "Out for Delivery".equals(o.status)).count();
            int delivered = (int) myOrders.stream().filter(o -> "Delivered".equals(o.status)).count();
            int delayed = (int) myOrders.stream().filter(o -> "Delayed".equals(o.status)).count();
            int failed = (int) myOrders.stream().filter(o -> "Failed".equals(o.status)).count();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(total));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(inTransit));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(pending));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(pickedUp));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(outForDelivery));
            if (statValues[5] != null) statValues[5].setText(String.valueOf(delivered));
            if (statValues[6] != null) statValues[6].setText(String.valueOf(delayed));
            if (statValues[7] != null) statValues[7].setText(String.valueOf(failed));
        });
    }
    
    private JPanel createDeliveriesButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        panel.setBackground(BG_LIGHT);
        
        JButton viewDetailsBtn = createActionButton("View Details", INFO);
        JButton updateStatusBtn = createActionButton("Update Status", new Color(255, 87, 34));
        JButton trackBtn = createActionButton("Track Order", PURPLE);
        
        panel.add(viewDetailsBtn);
        panel.add(updateStatusBtn);
        panel.add(trackBtn);
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        btn.addActionListener(e -> {
            int row = deliveriesTable.getSelectedRow();
            if (row == -1) {
                if (parentDashboard != null) {
                    parentDashboard.showNotification("Please select an order", WARNING);
                }
                return;
            }
            
            int modelRow = deliveriesTable.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < myOrders.size()) {
                Order selectedOrder = myOrders.get(modelRow);
                
                if (text.equals("View Details")) {
                    showEnhancedOrderDetails(selectedOrder);
                } else if (text.equals("Update Status")) {
                    if ("Delivered".equals(selectedOrder.status)) {
                        parentDashboard.showNotification("This order has already been delivered. Cannot update status.", WARNING);
                        return;
                    }
                    if ("Failed".equals(selectedOrder.status)) {
                        parentDashboard.showNotification("This order has failed. Admin will reassign it to another driver.", WARNING);
                        return;
                    }
                    parentDashboard.switchToUpdatePanel(selectedOrder);
                } else if (text.equals("Track Order")) {
                    parentDashboard.showOrderTracking(selectedOrder);
                }
            }
        });
        
        return btn;
    }
}