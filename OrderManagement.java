package logistics.login.admin.management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrderManagement {
    private JPanel mainPanel;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private List<Order> orders;
    
    public OrderManagement() {
        orders = new ArrayList<>();
        initializeSampleData();
        createMainPanel();
    }
    
    private void initializeSampleData() {
        // Sample data - replace with actual data loading
        orders.add(new Order("ORD001", "John Doe", "Pending", "2024-01-15"));
        orders.add(new Order("ORD002", "Jane Smith", "Delivered", "2024-01-14"));
        orders.add(new Order("ORD003", "Bob Johnson", "In Transit", "2024-01-14"));
        orders.add(new Order("ORD004", "Alice Brown", "Pending", "2024-01-13"));
        orders.add(new Order("ORD005", "Charlie Wilson", "Delivered", "2024-01-13"));
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Order Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"Order ID", "Customer", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ordersTable.setRowHeight(30);
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        ordersTable.getTableHeader().setBackground(new Color(255, 140, 0));
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        
        // Add data to table
        refreshTableData();
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Add New Order");
        addButton.setBackground(new Color(255, 140, 0));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(108, 117, 125));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshTableData() {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                order.getStatus(),
                order.getDate()
            });
        }
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JPanel getRefreshedPanel() {
        refreshData();
        return mainPanel;
    }
    
    public void refreshData() {
        // Reload data from source
        refreshTableData();
    }
    
    public int getPendingCount() {
        return (int) orders.stream()
            .filter(o -> "Pending".equals(o.getStatus()))
            .count();
    }
    
    public int getTotalCount() {
        return orders.size();
    }
    
    // Inner class for Order data
    private class Order {
        private String orderId;
        private String customerName;
        private String status;
        private String date;
        
        public Order(String orderId, String customerName, String status, String date) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.status = status;
            this.date = date;
        }
        
        public String getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }
}