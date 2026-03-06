package sender;

import logistics.orders.Order;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class PaymentPanel extends JPanel {
    private SenderDashboard dashboard;
    private JTable paymentsTable;
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;

    public PaymentPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Payments & Invoices");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        balanceLabel = new JLabel("Pending Balance: RM 0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        balanceLabel.setForeground(new Color(220, 53, 69));
        headerPanel.add(balanceLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setBackground(Color.WHITE);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Payment History");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Payment ID", "Order ID", "Amount", "Date", "Status", "Method"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentsTable = new JTable(tableModel);
        paymentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentsTable.setRowHeight(30);
        paymentsTable.setShowGrid(true);
        paymentsTable.setGridColor(new Color(230, 230, 230));
        paymentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        paymentsTable.getTableHeader().setBackground(new Color(248, 249, 250));

        // Custom renderer for status
        paymentsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected) {
                    if (status.equals("Paid")) {
                        setForeground(new Color(40, 167, 69));
                    } else if (status.equals("Pending")) {
                        setForeground(new Color(255, 193, 7));
                    } else {
                        setForeground(new Color(108, 117, 125));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(tablePanel);

        // Payment method panel
        JPanel methodPanel = new JPanel(new GridBagLayout());
        methodPanel.setBackground(Color.WHITE);
        methodPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel methodTitle = new JLabel("Payment Method");
        methodTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        methodPanel.add(methodTitle, gbc);

        String[] methods = {"Credit Card", "Debit Card", "PayPal", "Bank Transfer"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        methodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1;
        methodPanel.add(methodCombo, gbc);

        JButton payBtn = new JButton("Pay Outstanding");
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBackground(new Color(40, 167, 69));
        payBtn.setBorderPainted(false);
        payBtn.setFocusPainted(false);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.addActionListener(e -> payOutstanding());
        gbc.gridx = 1;
        methodPanel.add(payBtn, gbc);

        splitPane.setBottomComponent(methodPanel);
        splitPane.setDividerLocation(300);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        refreshData();

        return mainPanel;
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        
        // Get actual orders with payment information
        String userEmail = dashboard.getSenderEmail();
        List<Order> allOrders = FileDataManager.getInstance().getAllOrders();
        
        // Check if this is demo sender
        boolean isDemoSender = "demo@sender.com".equals(userEmail);
        
        // Filter orders for this user
        List<Order> userOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.customerEmail != null && userEmail != null) {
                if (order.customerEmail.trim().equals(userEmail.trim())) {
                    userOrders.add(order);
                }
            }
        }
        
        // If no real orders and this is demo sender, show demo payment records
        if (userOrders.isEmpty() && isDemoSender) {
            addDemoPayments();
            updateBalance();
            return;
        }
        
        // Create payment records from real orders
        int paymentCounter = 1;
        for (Order order : userOrders) {
            // Extract cost from order notes
            String cost = extractCost(order.notes);
            double amount = 0.0;
            try {
                String cleanCost = cost.replace("RM", "").replace("$", "").trim();
                amount = Double.parseDouble(cleanCost);
            } catch (NumberFormatException e) {
                amount = 0.0;
            }
            
            // Determine payment status based on order status
            String paymentStatus = "Pending";
            if ("Delivered".equals(order.status)) {
                paymentStatus = "Paid";
            } else if ("Cancelled".equals(order.status)) {
                paymentStatus = "Cancelled";
            }
            
            String[] payment = {
                "PAY-" + String.format("%03d", paymentCounter++),
                order.id,
                String.format("RM %.2f", amount),
                order.orderDate,
                paymentStatus,
                "Credit Card"
            };
            
            tableModel.addRow(payment);
        }
        
        // Show empty state for real users with no orders
        if (userOrders.isEmpty() && !isDemoSender) {
            String[] emptyMessage = {
                "No payments yet",
                "-",
                "RM 0.00",
                "-",
                "-",
                "-"
            };
            tableModel.addRow(emptyMessage);
        }

        updateBalance();
    }

    private void addDemoPayments() {
        // Demo Payment 1
        String[] payment1 = {
            "PAY-001",
            "DEMO-ORD-001",
            "RM 45.50",
            "2024-01-15",
            "Pending",
            "Credit Card"
        };
        tableModel.addRow(payment1);
        
        // Demo Payment 2
        String[] payment2 = {
            "PAY-002",
            "DEMO-ORD-002",
            "RM 28.90",
            "2024-01-14",
            "Paid",
            "PayPal"
        };
        tableModel.addRow(payment2);
        
        // Demo Payment 3
        String[] payment3 = {
            "PAY-003",
            "DEMO-ORD-003",
            "RM 67.20",
            "2024-01-13",
            "Pending",
            "Bank Transfer"
        };
        tableModel.addRow(payment3);
        
        // Demo Payment 4
        String[] payment4 = {
            "PAY-004",
            "DEMO-ORD-004",
            "RM 89.50",
            "2024-01-12",
            "Pending",
            "Credit Card"
        };
        tableModel.addRow(payment4);
        
        // Demo Payment 5
        String[] payment5 = {
            "PAY-005",
            "DEMO-ORD-005",
            "RM 35.80",
            "2024-01-11",
            "Paid",
            "Debit Card"
        };
        tableModel.addRow(payment5);
    }

    private String extractCost(String notes) {
        if (notes != null && notes.contains("Estimated Cost:")) {
            String[] parts = notes.split("Estimated Cost: ");
            if (parts.length > 1) {
                String[] costParts = parts[1].split("\n");
                return costParts[0].trim();
            }
        }
        return "RM 0.00";
    }

    private void updateBalance() {
        double balance = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            if (statusObj != null && statusObj.toString().equals("Pending")) {
                String amount = tableModel.getValueAt(i, 2).toString().replace("RM", "").trim();
                try {
                    balance += Double.parseDouble(amount);
                } catch (NumberFormatException e) {
                    // Skip if amount can't be parsed
                }
            }
        }
        balanceLabel.setText("Pending Balance: RM " + String.format("%.2f", balance));
    }

    private void payOutstanding() {
        String userEmail = dashboard.getSenderEmail();
        boolean isDemoSender = "demo@sender.com".equals(userEmail);
        
        // Check if it's demo and has demo payments
        if (isDemoSender && tableModel.getRowCount() > 0) {
            Object firstOrderId = tableModel.getValueAt(0, 1);
            if (firstOrderId != null && firstOrderId.toString().contains("DEMO-")) {
                JOptionPane.showMessageDialog(this,
                    "This is a demo account. Create real orders to make payments.",
                    "Demo Account",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        double balance = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            if (statusObj != null && statusObj.toString().equals("Pending")) {
                String amount = tableModel.getValueAt(i, 2).toString().replace("RM", "").trim();
                try {
                    balance += Double.parseDouble(amount);
                } catch (NumberFormatException e) {
                    // Skip if amount can't be parsed
                }
            }
        }

        if (balance == 0) {
            JOptionPane.showMessageDialog(this, 
                "No outstanding payments", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Process payment of RM " + String.format("%.2f", balance) + "?",
            "Confirm Payment", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Update all pending payments to paid
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object statusObj = tableModel.getValueAt(i, 4);
                if (statusObj != null && statusObj.toString().equals("Pending")) {
                    tableModel.setValueAt("Paid", i, 4);
                }
            }
            
            updateBalance();
            
            JOptionPane.showMessageDialog(this, 
                "Payment successful!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}