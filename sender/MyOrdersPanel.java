// MyOrdersPanel.java
package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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

        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        ordersTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected && value != null) {
                    String status = value.toString();
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
                return c;
            }
        });

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
                    String orderId = tableModel.getValueAt(row, 0).toString();
                    
                    if (!"No orders yet".equals(orderId)) {
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
                        
                        if (!"No orders yet".equals(orderId)) {
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
        // First, refresh data from main file
        SenderDataManager.getInstance().refreshData();
    
        tableModel.setRowCount(0);
    
        String filter = (String) statusFilter.getSelectedItem();
        String userEmail = dashboard.getSenderEmail();
        
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(userEmail);

        if (!"All Orders".equals(filter)) {
            userOrders.removeIf(order -> !filter.equals(order.getStatus()));
        }
    
        orderCountLabel.setText("(" + userOrders.size() + " orders)");
    
        for (SenderOrder order : userOrders) {
            Object[] row = {
                order.getId(),
                extractCity(order.getCustomerAddress()),
                extractCity(order.getRecipientAddress()),
                String.format("%.1f kg", order.getWeight()),
                extractPackageType(order),
                order.getOrderDate(),
                order.getStatus(),
                extractCost(order)
            };
            tableModel.addRow(row);
        }
    
        if (userOrders.isEmpty()) {
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

    private String extractCity(String address) {
        if (address != null && address.contains(",")) {
            return address.substring(0, address.indexOf(",")).trim();
        }
        return address != null ? address : "N/A";
    }

    private String extractPackageType(SenderOrder order) {
        return order.getPackageType();
    }

    private String extractCost(SenderOrder order) {
        return order.getFormattedEstimatedCost();
    }

    private void filterOrders() {
        refreshData();
    }

    private void trackSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = tableModel.getValueAt(selectedRow, 0).toString();
            
            if ("No orders yet".equals(orderId)) {
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
            
            if ("No orders yet".equals(orderId)) {
                return;
            }
            
            String userEmail = dashboard.getSenderEmail();
            if (DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
                JOptionPane.showMessageDialog(this, 
                    "Demo users cannot cancel orders. Please create a real account to cancel orders.", 
                    "Demo Account Restriction", JOptionPane.WARNING_MESSAGE);
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
                boolean cancelled = SenderDataManager.getInstance().cancelOrder(orderId);
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
            
            if ("No orders yet".equals(orderId)) {
                return;
            }
            
            SenderOrder order = SenderDataManager.getInstance().getOrderById(orderId);
            
            if (order != null) {
                showOrderDetailsDialog(order);
            }
        }
    }

    private void showOrderDetailsDialog(SenderOrder order) {
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
        addDetailRow(panel, "Order ID:", order.getId(), gbc, y++);
        addDetailRow(panel, "Date:", order.getOrderDate(), gbc, y++);
        addDetailRow(panel, "Status:", order.getStatus(), gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "From:", order.getCustomerAddress(), gbc, y++);
        addDetailRow(panel, "To:", order.getRecipientAddress(), gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Sender Name:", order.getCustomerName(), gbc, y++);
        addDetailRow(panel, "Sender Phone:", order.getCustomerPhone(), gbc, y++);
        addDetailRow(panel, "Sender Email:", order.getCustomerEmail(), gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Recipient Name:", order.getRecipientName(), gbc, y++);
        addDetailRow(panel, "Recipient Phone:", order.getRecipientPhone(), gbc, y++);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Weight:", String.format("%.2f kg", order.getWeight()), gbc, y++);
        addDetailRow(panel, "Dimensions:", order.getDimensions() + " cm", gbc, y++);
        addDetailRow(panel, "Package Type:", order.getPackageType(), gbc, y++);
        
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            String description = order.getDescription();
            if (!description.isEmpty()) {
                addDetailRow(panel, "Description:", description, gbc, y++);
            }
        }
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Payment Status:", order.getPaymentStatus(), gbc, y++);
        addDetailRow(panel, "Payment Method:", order.getPaymentMethod() != null ? order.getPaymentMethod() : "Not Selected", gbc, y++);
        
        if (order.getTransactionId() != null) {
            addDetailRow(panel, "Transaction ID:", order.getTransactionId(), gbc, y++);
        }
        
        if (order.getPaymentDate() != null) {
            addDetailRow(panel, "Payment Date:", order.getPaymentDate(), gbc, y++);
        }
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        if (order.getEstimatedDelivery() != null) {
            addDetailRow(panel, "Est. Delivery:", order.getEstimatedDelivery(), gbc, y++);
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