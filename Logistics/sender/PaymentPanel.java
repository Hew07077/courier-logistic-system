// PaymentPanel.java
package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class PaymentPanel extends JPanel {
    private SenderDashboard dashboard;
    private JTable paymentsTable;
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;
    private JComboBox<String> methodCombo;
    private JLabel selectedMethodLabel;
    private JButton payBtn;
    private List<PaymentRecord> paymentRecords;
    
    // Modern color scheme
    private final Color BLUE_PRIMARY = new Color(0, 123, 255);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color WARNING_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(220, 53, 69);

    public PaymentPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        this.paymentRecords = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        
        refreshData();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Payments & Invoices");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(BLUE_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        balanceLabel = new JLabel("Pending Balance: RM 0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        balanceLabel.setForeground(DANGER_RED);
        headerPanel.add(balanceLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CARD_BG);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setBackground(CARD_BG);
        splitPane.setResizeWeight(0.6);

        JPanel tablePanel = createTablePanel();
        splitPane.setTopComponent(tablePanel);

        JPanel bottomPanel = createPaymentMethodPanel();
        splitPane.setBottomComponent(bottomPanel);
        
        splitPane.setDividerLocation(300);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_BG);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel tableTitle = new JLabel("Payment History");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Payment ID", "Order ID", "Amount", "Date", "Status", "Method", "Transaction ID"};
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

        paymentsTable = new JTable(tableModel);
        paymentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentsTable.setRowHeight(35);
        paymentsTable.setShowGrid(true);
        paymentsTable.setGridColor(BORDER_COLOR);
        paymentsTable.setSelectionBackground(new Color(200, 225, 255));
        paymentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        paymentsTable.getTableHeader().setBackground(new Color(248, 249, 250));
        paymentsTable.getTableHeader().setForeground(TEXT_DARK);
        paymentsTable.setRowSelectionAllowed(true);
        paymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        paymentsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected && value != null) {
                    String status = value.toString();
                    if (status.equals("Paid")) {
                        setForeground(SUCCESS_GREEN);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Pending")) {
                        setForeground(WARNING_YELLOW);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Failed")) {
                        setForeground(DANGER_RED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(TEXT_GRAY);
                    }
                }
                return c;
            }
        });

        paymentsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createPaymentMethodPanel() {
        JPanel methodPanel = new JPanel(new BorderLayout());
        methodPanel.setBackground(CARD_BG);
        methodPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        JLabel methodTitle = new JLabel("Payment Method & Actions");
        methodTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        methodTitle.setForeground(TEXT_DARK);
        methodTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        methodPanel.add(methodTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(CARD_BG);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JLabel selectMethodLabel = new JLabel("Select Payment Method:");
        selectMethodLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectMethodLabel.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        centerPanel.add(selectMethodLabel, gbc);

        String[] methods = {"Credit Card (Visa/Mastercard)", "Debit Card", "PayPal", "Bank Transfer", "Cash on Delivery"};
        methodCombo = new JComboBox<>(methods);
        methodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        methodCombo.setBackground(CARD_BG);
        methodCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        methodCombo.addActionListener(e -> updateSelectedMethod());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(methodCombo, gbc);

        selectedMethodLabel = new JLabel("Selected: Credit Card");
        selectedMethodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectedMethodLabel.setForeground(BLUE_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 5, 10, 5);
        centerPanel.add(selectedMethodLabel, gbc);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionsPanel.setBackground(CARD_BG);

        payBtn = new JButton("Pay Outstanding Balance");
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBackground(SUCCESS_GREEN);
        payBtn.setBorderPainted(false);
        payBtn.setFocusPainted(false);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.setPreferredSize(new Dimension(220, 40));
        payBtn.addActionListener(e -> payOutstanding());
        actionsPanel.add(payBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setForeground(TEXT_DARK);
        refreshBtn.setBackground(CARD_BG);
        refreshBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(90, 30));
        refreshBtn.addActionListener(e -> refreshData());
        actionsPanel.add(refreshBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 5, 0, 5);
        centerPanel.add(actionsPanel, gbc);

        methodPanel.add(centerPanel, BorderLayout.CENTER);

        return methodPanel;
    }

    private void updateSelectedMethod() {
        String selected = (String) methodCombo.getSelectedItem();
        selectedMethodLabel.setText("Selected: " + selected);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        paymentRecords.clear();
        
        String userEmail = dashboard.getSenderEmail();
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(userEmail);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        if (!userOrders.isEmpty()) {
            int paymentCounter = 1;
            for (SenderOrder order : userOrders) {
                double amount = extractCostFromOrder(order);
                String paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : "Pending";
                String transactionId = order.getTransactionId() != null ? order.getTransactionId() : 
                                      ("Paid".equals(paymentStatus) ? generateTransactionId(order) : 
                                      ("Pending".equals(paymentStatus) ? "Pending" : "-"));
                String paymentMethod = order.getPaymentMethod() != null && !"Not Selected".equals(order.getPaymentMethod()) ?
                                       order.getPaymentMethod() : (String) methodCombo.getSelectedItem();
                String paymentDate = order.getPaymentDate() != null ? order.getPaymentDate() : order.getOrderDate();
                
                PaymentRecord record = new PaymentRecord(
                    "PAY-" + String.format("%03d", paymentCounter),
                    order.getId(),
                    amount,
                    paymentDate,
                    paymentStatus,
                    paymentMethod,
                    transactionId
                );
                
                paymentRecords.add(record);
                
                String[] row = {
                    record.paymentId,
                    record.orderId,
                    String.format("RM %.2f", record.amount),
                    record.date,
                    record.status,
                    record.method,
                    record.transactionId
                };
                
                tableModel.addRow(row);
                paymentCounter++;
            }
        }
        
        if (userOrders.isEmpty()) {
            String[] emptyMessage = {
                "No payments yet",
                "-",
                "RM 0.00",
                "-",
                "-",
                "-",
                "-"
            };
            tableModel.addRow(emptyMessage);
        }

        updateBalance();
        updatePayButtonState();
    }

    private double extractCostFromOrder(SenderOrder order) {
        if (order.getNotes() != null && order.getNotes().contains("Estimated Cost:")) {
            try {
                String[] parts = order.getNotes().split("Estimated Cost: ");
                if (parts.length > 1) {
                    String[] costParts = parts[1].split("\n");
                    String costStr = costParts[0].replace("RM", "").replace("$", "").trim();
                    return Double.parseDouble(costStr);
                }
            } catch (NumberFormatException e) {
                // Return default if parsing fails
            }
        }
        return 50.0;
    }

    private String generateTransactionId(SenderOrder order) {
        return "TXN" + System.currentTimeMillis() + order.getId().substring(0, 3);
    }

    private void updateBalance() {
        double balance = 0;
        for (PaymentRecord record : paymentRecords) {
            if ("Pending".equals(record.status)) {
                balance += record.amount;
            }
        }
        balanceLabel.setText("Pending Balance: RM " + String.format("%.2f", balance));
        
        if (balance > 0) {
            balanceLabel.setForeground(DANGER_RED);
        } else {
            balanceLabel.setForeground(SUCCESS_GREEN);
        }
    }

    private void updatePayButtonState() {
        boolean hasPending = false;
        for (PaymentRecord record : paymentRecords) {
            if ("Pending".equals(record.status)) {
                hasPending = true;
                break;
            }
        }
        payBtn.setEnabled(hasPending);
        
        if (!hasPending) {
            payBtn.setText("No Outstanding Payments");
            payBtn.setBackground(TEXT_GRAY);
        } else {
            payBtn.setText("Pay Outstanding Balance");
            payBtn.setBackground(SUCCESS_GREEN);
        }
    }

    private void payOutstanding() {
        double totalPending = 0;
        List<PaymentRecord> pendingRecords = new ArrayList<>();
        
        for (PaymentRecord record : paymentRecords) {
            if ("Pending".equals(record.status)) {
                totalPending += record.amount;
                pendingRecords.add(record);
            }
        }

        if (pendingRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No outstanding payments to process", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedMethod = (String) methodCombo.getSelectedItem();
        String message = String.format(
            "Payment Summary:\n" +
            "Total Amount: RM %.2f\n" +
            "Payment Method: %s\n\n" +
            "Orders to be paid: %d\n\n" +
            "Process payment?",
            totalPending, selectedMethod, pendingRecords.size()
        );

        int confirm = JOptionPane.showConfirmDialog(this, 
            message,
            "Confirm Payment", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            processPayment(pendingRecords, selectedMethod, totalPending);
        }
    }

    private void processPayment(List<PaymentRecord> pendingRecords, String paymentMethod, double amount) {
        JDialog processingDialog = createProcessingDialog();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void done() {
                processingDialog.dispose();
                
                String transactionId = "TXN" + System.currentTimeMillis();
                String paymentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                
                for (PaymentRecord record : pendingRecords) {
                    record.status = "Paid";
                    record.method = paymentMethod;
                    record.transactionId = transactionId;
                    record.date = paymentDate;
                    
                    SenderDataManager.getInstance().updateOrderPaymentStatus(
                        record.orderId, "Paid", paymentMethod, transactionId, paymentDate
                    );
                }
                
                refreshData();
                
                JOptionPane.showMessageDialog(PaymentPanel.this,
                    String.format("Payment of RM %.2f processed successfully!\nTransaction ID: %s", 
                        amount, transactionId),
                    "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        worker.execute();
        processingDialog.setVisible(true);
    }

    private JDialog createProcessingDialog() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ancestor, "Processing Payment", true);
        
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        
        JLabel processingLabel = new JLabel("Processing payment...");
        processingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(250, 20));
        
        JLabel pleaseWaitLabel = new JLabel("Please wait...");
        pleaseWaitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pleaseWaitLabel.setForeground(TEXT_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(processingLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel.add(progressBar, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel.add(pleaseWaitLabel, gbc);
        
        dialog.add(panel);
        
        return dialog;
    }

    private class PaymentRecord {
        String paymentId;
        String orderId;
        double amount;
        String date;
        String status;
        String method;
        String transactionId;

        PaymentRecord(String paymentId, String orderId, double amount, 
                     String date, String status, String method, String transactionId) {
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.amount = amount;
            this.date = date;
            this.status = status;
            this.method = method;
            this.transactionId = transactionId;
        }
    }
}