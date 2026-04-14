package courier;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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
    
    // Components
    private JTable deliveriesTable;
    private DefaultTableModel deliveriesTableModel;
    private TableRowSorter<DefaultTableModel> deliveriesRowSorter;
    private JPanel[] statCards = new JPanel[8];
    private JLabel[] statValues = new JLabel[8];
    private int currentFilterIndex = -1;
    
    private List<Order> myOrders;
    private CourierDashboard parentDashboard;
    
    public DeliveriesPanel(List<Order> orders, OrderStorage orderStorage, CourierDashboard parent) {
        this.myOrders = orders;
        this.parentDashboard = parent;
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
        Color[] colors = {PRIMARY_GREEN, INFO, WARNING, PURPLE, new Color(0, 123, 255), SUCCESS, DANGER, DANGER};
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
                    comp.setBackground(new Color(232, 245, 233));
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        };
        
        deliveriesTable.setRowHeight(48);
        deliveriesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deliveriesTable.setSelectionBackground(new Color(232, 245, 233));
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
                        parentDashboard.showEnhancedOrderDetails(myOrders.get(row));
                    }
                }
            }
        });
        
        refreshDeliveriesTable();
        return deliveriesTable;
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
            
            panel.setBackground(isSelected ? new Color(232, 245, 233) : 
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
                            new LineBorder(new Color(225, 173, 1), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "In Transit":
                        label.setForeground(new Color(13, 110, 130));
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(23, 162, 184), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Delivered":
                        label.setForeground(new Color(0, 100, 0));
                        label.setBackground(new Color(232, 245, 233));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(40, 167, 69), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Delayed":
                        label.setForeground(new Color(150, 20, 30));
                        label.setBackground(new Color(255, 235, 238));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(220, 53, 69), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Picked Up":
                        label.setForeground(new Color(70, 40, 120));
                        label.setBackground(new Color(240, 235, 255));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(111, 66, 193), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Out for Delivery":
                        label.setForeground(new Color(0, 80, 150));
                        label.setBackground(new Color(220, 240, 255));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(0, 123, 255), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Failed":
                        label.setForeground(new Color(150, 0, 0));
                        label.setBackground(new Color(255, 220, 220));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(220, 53, 69), 1, true),
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
            setBackground(isSelected ? new Color(232, 245, 233) : 
                         (row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE));
            return this;
        }
    }
    
    // DeliveriesPanel.java - 修改 refreshDeliveriesTable 方法中的状态获取

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
        
            // 使用 getCourierStatus() 获取司机看到的状态
            String courierStatus = o.getCourierStatus();
        
            deliveriesTableModel.addRow(new Object[]{
                o.id, 
                o.recipientName != null ? o.recipientName : "-",
                o.recipientPhone != null ? o.recipientPhone : "-",
                courierStatus,  // 使用司机状态
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
                    parentDashboard.showEnhancedOrderDetails(selectedOrder);
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