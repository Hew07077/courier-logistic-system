package sender;

import logistics.orders.Order;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class MyOrdersPanel extends JPanel {
    private SenderDashboard dashboard;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JButton refreshBtn;
    private JLabel orderCountLabel;

    public MyOrdersPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Orders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        titlePanel.add(titleLabel);
        
        orderCountLabel = new JLabel();
        orderCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderCountLabel.setForeground(new Color(108, 117, 125));
        titlePanel.add(orderCountLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        statusFilter = new JComboBox<>(new String[]{
            "All Orders", "Pending", "In Transit", "Delivered", "Cancelled", "Delayed"
        });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.addActionListener(e -> filterOrders());
        filterPanel.add(statusFilter);

        refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(new Color(0, 123, 255));
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());
        filterPanel.add(refreshBtn);

        headerPanel.add(filterPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create table model
        String[] columns = {"Order ID", "From", "To", "Weight", "Type", "Date", "Status", "Cost"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        ordersTable = new JTable(tableModel);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ordersTable.setRowHeight(35);
        ordersTable.setShowGrid(true);
        ordersTable.setGridColor(new Color(230, 230, 230));
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        ordersTable.getTableHeader().setBackground(new Color(248, 249, 250));
        ordersTable.getTableHeader().setForeground(new Color(33, 37, 41));
        ordersTable.setSelectionBackground(new Color(240, 248, 255));

        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        // Custom renderer for status column
        ordersTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected && value != null) {
                    String status = value.toString();
                    // Check if it's a demo order
                    if (status.contains("(DEMO)")) {
                        setForeground(new Color(108, 117, 125)); // Gray color for demo
                    } else {
                        switch(status) {
                            case "Delivered":
                                setForeground(new Color(40, 167, 69));
                                break;
                            case "In Transit":
                                setForeground(new Color(0, 123, 255));
                                break;
                            case "Pending":
                                setForeground(new Color(255, 193, 7));
                                break;
                            case "Cancelled":
                                setForeground(new Color(220, 53, 69));
                                break;
                            case "Delayed":
                                setForeground(new Color(255, 87, 34));
                                break;
                            default:
                                setForeground(new Color(33, 37, 41));
                        }
                    }
                }
                return c;
            }
        });

        // Add right-click menu
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem trackItem = new JMenuItem("Track Order");
        trackItem.addActionListener(e -> trackSelectedOrder());
        
        JMenuItem cancelItem = new JMenuItem("Cancel Order");
        cancelItem.addActionListener(e -> cancelSelectedOrder());
        
        JMenuItem detailsItem = new JMenuItem("View Details");
        detailsItem.addActionListener(e -> viewOrderDetails());
        
        popupMenu.add(trackItem);
        popupMenu.add(cancelItem);
        popupMenu.add(detailsItem);

        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            
            private void showPopup(MouseEvent e) {
                int row = ordersTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    ordersTable.setRowSelectionInterval(row, row);
                    // Check if it's a demo order
                    String orderId = tableModel.getValueAt(row, 0).toString();
                    String status = tableModel.getValueAt(row, 6).toString();
                    
                    if (orderId.contains("DEMO-") || status.contains("(DEMO)")) {
                        JOptionPane.showMessageDialog(MyOrdersPanel.this,
                            "This is a demo order. Create your own order to perform actions.",
                            "Demo Order",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        popupMenu.show(ordersTable, e.getX(), e.getY());
                    }
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ordersTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String orderId = tableModel.getValueAt(row, 0).toString();
                        String status = tableModel.getValueAt(row, 6).toString();
                        
                        if (orderId.contains("DEMO-") || status.contains("(DEMO)")) {
                            JOptionPane.showMessageDialog(MyOrdersPanel.this,
                                "This is a demo order. Create your own order to view details.",
                                "Demo Order",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            viewOrderDetails();
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshData();

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JButton trackBtn = new JButton("Track Selected");
        trackBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trackBtn.setForeground(Color.WHITE);
        trackBtn.setBackground(new Color(0, 123, 255));
        trackBtn.setBorderPainted(false);
        trackBtn.setFocusPainted(false);
        trackBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trackBtn.addActionListener(e -> trackSelectedOrder());

        JButton newOrderBtn = new JButton("+ New Order");
        newOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        newOrderBtn.setForeground(Color.WHITE);
        newOrderBtn.setBackground(new Color(40, 167, 69));
        newOrderBtn.setBorderPainted(false);
        newOrderBtn.setFocusPainted(false);
        newOrderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));

        bottomPanel.add(trackBtn);
        bottomPanel.add(newOrderBtn);

        return bottomPanel;
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        
        String filter = (String) statusFilter.getSelectedItem();
        String userEmail = dashboard.getSenderEmail();
        
        // Check if this is a demo sender
        boolean isDemoSender = "demo@sender.com".equals(userEmail);
        
        // Get all orders from FileDataManager
        List<Order> allOrders = FileDataManager.getInstance().getAllOrders();
        
        // Filter orders for this user
        List<Order> userOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.customerEmail != null && userEmail != null) {
                String orderEmail = order.customerEmail.trim();
                String currentEmail = userEmail.trim();
                
                if (orderEmail.equals(currentEmail)) {
                    userOrders.add(order);
                }
            }
        }
        
        // If this is demo sender AND no real orders, show demo orders
        if (isDemoSender && userOrders.isEmpty()) {
            userOrders = createDemoOrders();
        }
        
        // Apply status filter
        if (!"All Orders".equals(filter)) {
            List<Order> filteredOrders = new ArrayList<>();
            for (Order order : userOrders) {
                String statusToCompare = order.status;
                // Remove (DEMO) tag for comparison if present
                if (statusToCompare.contains(" (DEMO)")) {
                    statusToCompare = statusToCompare.replace(" (DEMO)", "");
                }
                if (filter.equals(statusToCompare)) {
                    filteredOrders.add(order);
                }
            }
            userOrders = filteredOrders;
        }
        
        // Update count label
        orderCountLabel.setText("(" + userOrders.size() + " orders)");
        
        // Add rows to table
        for (Order order : userOrders) {
            Object[] row = {
                order.id,
                extractCity(order.customerAddress),
                extractCity(order.recipientAddress),
                String.format("%.1f kg", order.weight),
                extractPackageType(order.notes),
                order.orderDate,
                order.status,
                extractCost(order.notes)
            };
            tableModel.addRow(row);
        }
        
        // If no orders at all and not demo sender, show empty state message in table
        if (userOrders.isEmpty() && !isDemoSender) {
            Object[] emptyRow = {
                "No orders yet",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            };
            tableModel.addRow(emptyRow);
        }
    }

    private List<Order> createDemoOrders() {
        List<Order> demoOrders = new ArrayList<>();
        
        // Demo Order 1
        Order order1 = new Order();
        order1.id = "DEMO-ORD-001";
        order1.customerName = "Demo User";
        order1.customerEmail = "demo@sender.com";
        order1.customerPhone = "012-3456789";
        order1.customerAddress = "Kuala Lumpur, Malaysia";
        order1.recipientName = "John Doe";
        order1.recipientPhone = "011-22334455";
        order1.recipientAddress = "Penang, Malaysia";
        order1.weight = 2.5;
        order1.dimensions = "30x20x15";
        order1.orderDate = "2024-01-15";
        order1.status = "In Transit (DEMO)";
        order1.notes = "Package Type: Electronics\nEstimated Cost: RM 45.50\nFragile: Yes";
        order1.estimatedDelivery = "2024-01-18";
        order1.driverId = "DRV-001";
        demoOrders.add(order1);
        
        // Demo Order 2
        Order order2 = new Order();
        order2.id = "DEMO-ORD-002";
        order2.customerName = "Demo User";
        order2.customerEmail = "demo@sender.com";
        order2.customerPhone = "012-3456789";
        order2.customerAddress = "Johor Bahru, Malaysia";
        order2.recipientName = "Jane Smith";
        order2.recipientPhone = "013-55667788";
        order2.recipientAddress = "Melaka, Malaysia";
        order2.weight = 1.8;
        order2.dimensions = "25x15x10";
        order2.orderDate = "2024-01-14";
        order2.status = "Delivered (DEMO)";
        order2.notes = "Package Type: Documents\nEstimated Cost: RM 28.90\nInsurance: No";
        order2.estimatedDelivery = "2024-01-16";
        order2.driverId = "DRV-002";
        demoOrders.add(order2);
        
        // Demo Order 3
        Order order3 = new Order();
        order3.id = "DEMO-ORD-003";
        order3.customerName = "Demo User";
        order3.customerEmail = "demo@sender.com";
        order3.customerPhone = "012-3456789";
        order3.customerAddress = "Ipoh, Malaysia";
        order3.recipientName = "Ahmad Abdullah";
        order3.recipientPhone = "014-77889900";
        order3.recipientAddress = "Kuala Lumpur, Malaysia";
        order3.weight = 5.0;
        order3.dimensions = "40x30x25";
        order3.orderDate = "2024-01-13";
        order3.status = "Pending (DEMO)";
        order3.notes = "Package Type: Clothing\nEstimated Cost: RM 67.20\nExpress Delivery: Yes";
        order3.estimatedDelivery = "2024-01-17";
        order3.driverId = "";
        demoOrders.add(order3);
        
        // Demo Order 4
        Order order4 = new Order();
        order4.id = "DEMO-ORD-004";
        order4.customerName = "Demo User";
        order4.customerEmail = "demo@sender.com";
        order4.customerPhone = "012-3456789";
        order4.customerAddress = "Kota Kinabalu, Malaysia";
        order4.recipientName = "Sarah Lim";
        order4.recipientPhone = "016-11223344";
        order4.recipientAddress = "Sandakan, Malaysia";
        order4.weight = 3.2;
        order4.dimensions = "35x25x20";
        order4.orderDate = "2024-01-12";
        order4.status = "Delayed (DEMO)";
        order4.notes = "Package Type: Perishable\nEstimated Cost: RM 89.50\nKeep Refrigerated: Yes";
        order4.estimatedDelivery = "2024-01-19";
        order4.driverId = "DRV-003";
        demoOrders.add(order4);
        
        // Demo Order 5
        Order order5 = new Order();
        order5.id = "DEMO-ORD-005";
        order5.customerName = "Demo User";
        order5.customerEmail = "demo@sender.com";
        order5.customerPhone = "012-3456789";
        order5.customerAddress = "Kuching, Malaysia";
        order5.recipientName = "Tan Wei Ming";
        order5.recipientPhone = "017-99887766";
        order5.recipientAddress = "Sibu, Malaysia";
        order5.weight = 1.2;
        order5.dimensions = "20x15x10";
        order5.orderDate = "2024-01-11";
        order5.status = "In Transit (DEMO)";
        order5.notes = "Package Type: Electronics\nEstimated Cost: RM 35.80\nBattery Included: No";
        order5.estimatedDelivery = "2024-01-14";
        order5.driverId = "DRV-004";
        demoOrders.add(order5);
        
        return demoOrders;
    }

    private String extractCity(String address) {
        if (address != null && address.contains(",")) {
            return address.substring(0, address.indexOf(",")).trim();
        }
        return address != null ? address : "N/A";
    }

    private String extractPackageType(String notes) {
        if (notes != null && notes.contains("Package Type:")) {
            String[] parts = notes.split("Package Type: ");
            if (parts.length > 1) {
                String[] typeParts = parts[1].split("\n");
                return typeParts[0].trim();
            }
        }
        return "Standard";
    }

    private String extractCost(String notes) {
        if (notes != null && notes.contains("Estimated Cost:")) {
            String[] parts = notes.split("Estimated Cost: ");
            if (parts.length > 1) {
                String[] costParts = parts[1].split("\n");
                return costParts[0].trim();
            }
        }
        return "RM --.--";
    }

    private void filterOrders() {
        refreshData();
    }

    private void trackSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = tableModel.getValueAt(selectedRow, 0).toString();
            String status = tableModel.getValueAt(selectedRow, 6).toString();
            
            // Check if it's a demo order
            if (orderId.contains("DEMO-") || status.contains("(DEMO)")) {
                JOptionPane.showMessageDialog(this,
                    "This is a demo order. Create your own order to track it.",
                    "Demo Order",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            dashboard.showPanel("TRACK");
            
            SwingUtilities.invokeLater(() -> {
                findAndSetTrackingNumber(dashboard, orderId);
            });
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an order to track", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void findAndSetTrackingNumber(Container container, String orderId) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof TrackOrderPanel) {
                ((TrackOrderPanel) comp).setTrackingNumber(orderId);
                return;
            } else if (comp instanceof Container) {
                findAndSetTrackingNumber((Container) comp, orderId);
            }
        }
    }

    private void cancelSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = tableModel.getValueAt(selectedRow, 0).toString();
            String status = tableModel.getValueAt(selectedRow, 6).toString();
            
            // Check if it's a demo order
            if (orderId.contains("DEMO-") || status.contains("(DEMO)")) {
                JOptionPane.showMessageDialog(this,
                    "This is a demo order. Create your own order to cancel it.",
                    "Demo Order",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            if (status.equals("Delivered") || status.equals("Cancelled")) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot cancel this order (Status: " + status + ")", 
                    "Invalid Action", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel order " + orderId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean cancelled = FileDataManager.getInstance().cancelOrder(orderId);
                if (cancelled) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, 
                        "Order cancelled successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an order to cancel", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = tableModel.getValueAt(selectedRow, 0).toString();
            String status = tableModel.getValueAt(selectedRow, 6).toString();
            
            // Check if it's a demo order
            if (orderId.contains("DEMO-") || status.contains("(DEMO)")) {
                showDemoOrderDetailsDialog(orderId);
                return;
            }
            
            Order order = FileDataManager.getInstance().getOrderById(orderId);
            if (order != null) {
                showOrderDetailsDialog(order);
            }
        }
    }

    private void showDemoOrderDetailsDialog(String orderId) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Demo Order Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        int y = 0;
        
        // Demo badge
        JPanel demoBadgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        demoBadgePanel.setOpaque(false);
        JLabel demoBadge = new JLabel("DEMO ORDER");
        demoBadge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        demoBadge.setForeground(Color.WHITE);
        demoBadge.setBackground(new Color(108, 117, 125));
        demoBadge.setOpaque(true);
        demoBadge.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        demoBadgePanel.add(demoBadge);
        
        gbc.gridy = y++;
        panel.add(demoBadgePanel, gbc);
        
        // Find which demo order
        Order demoOrder = null;
        for (Order order : createDemoOrders()) {
            if (order.id.equals(orderId)) {
                demoOrder = order;
                break;
            }
        }
        
        if (demoOrder != null) {
            addDetailRow(panel, "Order ID:", demoOrder.id, gbc, y++);
            addDetailRow(panel, "Date:", demoOrder.orderDate, gbc, y++);
            addDetailRow(panel, "Status:", demoOrder.status, gbc, y++);
            
            gbc.gridy = y++;
            panel.add(new JSeparator(), gbc);
            
            addDetailRow(panel, "From:", demoOrder.customerAddress, gbc, y++);
            addDetailRow(panel, "To:", demoOrder.recipientAddress, gbc, y++);
            
            gbc.gridy = y++;
            panel.add(new JSeparator(), gbc);
            
            addDetailRow(panel, "Weight:", String.format("%.2f kg", demoOrder.weight), gbc, y++);
            addDetailRow(panel, "Dimensions:", demoOrder.dimensions + " cm", gbc, y++);
            
            if (demoOrder.notes != null && !demoOrder.notes.isEmpty()) {
                String[] noteLines = demoOrder.notes.split("\n");
                for (String line : noteLines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            addDetailRow(panel, parts[0] + ":", parts[1].trim(), gbc, y++);
                        }
                    }
                }
            }
        }

        gbc.gridy = y;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(0, 123, 255));
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(closeBtn, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void showOrderDetailsDialog(Order order) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Order Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        int y = 0;
        addDetailRow(panel, "Order ID:", order.id, gbc, y++);
        addDetailRow(panel, "Date:", order.orderDate, gbc, y++);
        addDetailRow(panel, "Status:", order.status, gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "From:", order.customerAddress, gbc, y++);
        addDetailRow(panel, "To:", order.recipientAddress, gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Sender Name:", order.customerName, gbc, y++);
        addDetailRow(panel, "Sender Phone:", order.customerPhone, gbc, y++);
        addDetailRow(panel, "Sender Email:", order.customerEmail, gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Recipient Name:", order.recipientName, gbc, y++);
        addDetailRow(panel, "Recipient Phone:", order.recipientPhone, gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Weight:", String.format("%.2f kg", order.weight), gbc, y++);
        addDetailRow(panel, "Dimensions:", order.dimensions + " cm", gbc, y++);
        
        if (order.notes != null && !order.notes.isEmpty()) {
            String[] noteLines = order.notes.split("\n");
            for (String line : noteLines) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        addDetailRow(panel, parts[0] + ":", parts[1].trim(), gbc, y++);
                    }
                }
            }
        }
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        if (order.estimatedDelivery != null) {
            addDetailRow(panel, "Est. Delivery:", order.estimatedDelivery, gbc, y++);
        }
        if (order.driverId != null && !order.driverId.isEmpty()) {
            addDetailRow(panel, "Driver ID:", order.driverId, gbc, y++);
        }

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(0, 123, 255));
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());

        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(closeBtn, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value != null ? value : "-");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(valueComp, gbc);
    }
}