package courier;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CompleteDeliveryPanel extends JPanel {
    
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color WARNING = new Color(225, 173, 1);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color PURPLE = new Color(111, 66, 193);
    private static final Color OUT_FOR_DELIVERY_COLOR = new Color(0, 123, 255);
    
    private JComboBox<String> orderSelectCombo;
    private JComboBox<String> statusCombo;
    private JTextArea signatureArea;
    private JLabel photoFileNameLabel;
    private File deliveryPhotoFile;
    private JPanel orderDetailsPanel;
    
    private List<Order> myOrders;
    private OrderStorage orderStorage;
    private CourierDashboard parentDashboard;
    
    public CompleteDeliveryPanel(List<Order> orders, OrderStorage orderStorage, CourierDashboard parent) {
        this.myOrders = orders;
        this.orderStorage = orderStorage;
        this.parentDashboard = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }
    
    public void refreshData(List<Order> updatedOrders) {
        this.myOrders = updatedOrders;
        refreshOrderSelectCombo();
    }
    
    public void clearForm() {
        if (orderSelectCombo != null) orderSelectCombo.setSelectedIndex(-1);
        if (statusCombo != null && statusCombo.getModel().getSize() > 0) statusCombo.setSelectedIndex(0);
        if (signatureArea != null) signatureArea.setText("");
        deliveryPhotoFile = null;
        updateOrderDetailsAndStatusOptions();
    }
    
    public void selectOrder(Order order) {
        if (orderSelectCombo != null && order != null) {
            for (int i = 0; i < orderSelectCombo.getItemCount(); i++) {
                String item = orderSelectCombo.getItemAt(i);
                if (item != null && item.startsWith(order.id)) {
                    orderSelectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void initUI() {
        JPanel headerPanel = createHeader();
        JPanel formPanel = createFormPanel();
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Update Order Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_GREEN);
        
        JLabel subtitleLabel = new JLabel("Update the status of your assigned orders");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_GRAY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 30, 30, 30)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Order Selection
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel orderLabel = new JLabel("Select Order:*");
        orderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(orderLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        orderSelectCombo = new JComboBox<>();
        orderSelectCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderSelectCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        orderSelectCombo.setPreferredSize(new Dimension(400, 40));
        orderSelectCombo.addActionListener(e -> updateOrderDetailsAndStatusOptions());
        formPanel.add(orderSelectCombo, gbc);
        row++;
        
        // Spacer
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        // Order Details Panel - 显示时间轴
        orderDetailsPanel = new JPanel();
        orderDetailsPanel.setLayout(new BoxLayout(orderDetailsPanel, BoxLayout.Y_AXIS));
        orderDetailsPanel.setBackground(new Color(248, 249, 250));
        orderDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        JScrollPane detailsScroll = new JScrollPane(orderDetailsPanel);
        detailsScroll.setBorder(null);
        detailsScroll.setPreferredSize(new Dimension(500, 280));
        formPanel.add(detailsScroll, gbc);
        row++;
        
        // Spacer
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        formPanel.add(separator, gbc);
        row++;
        
        // Status Selection
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.insets = new Insets(12, 15, 12, 15);
        JLabel statusLabel = new JLabel("New Status:*");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        statusCombo = new JComboBox<>();
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        statusCombo.setPreferredSize(new Dimension(400, 40));
        statusCombo.addActionListener(e -> {});
        formPanel.add(statusCombo, gbc);
        row++;
        
        // Signature
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel signatureLabel = new JLabel("Signature:*");
        signatureLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(signatureLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        signatureArea = new JTextArea(3, 20);
        signatureArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signatureArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);
        signatureArea.setPreferredSize(new Dimension(300, 70));
        formPanel.add(signatureArea, gbc);
        row++;
        
        // Note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel noteLabel = new JLabel("* Signature is required for all status updates.", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(TEXT_GRAY);
        formPanel.add(noteLabel, gbc);
        row++;
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 15, 10, 15);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearBtn.setForeground(TEXT_GRAY);
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setPreferredSize(new Dimension(120, 40));
        clearBtn.addActionListener(e -> clearForm());
        
        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(SUCCESS);
        updateBtn.setBorderPainted(false);
        updateBtn.setPreferredSize(new Dimension(150, 40));
        updateBtn.addActionListener(e -> processStatusUpdate());
        
        buttonPanel.add(clearBtn);
        buttonPanel.add(updateBtn);
        formPanel.add(buttonPanel, gbc);
        
        refreshOrderSelectCombo();
        return formPanel;
    }
    
    private void refreshOrderSelectCombo() {
        if (orderSelectCombo == null) return;
        orderSelectCombo.removeAllItems();
        
        List<Order> activeOrders = myOrders.stream()
            .filter(o -> !"Delivered".equals(o.status) && !"Failed".equals(o.status))
            .collect(Collectors.toList());
        
        if (activeOrders.isEmpty()) {
            orderSelectCombo.addItem("No active orders");
            if (statusCombo != null) statusCombo.setEnabled(false);
        } else {
            for (Order o : activeOrders) {
                String courierStatus = o.getCourierStatus();
                orderSelectCombo.addItem(o.id + " - " + o.recipientName + " (" + courierStatus + ")");
            }
            if (statusCombo != null) statusCombo.setEnabled(true);
        }
        orderSelectCombo.setSelectedIndex(-1);
    }
    
    private void updateOrderDetailsAndStatusOptions() {
        int selectedIndex = orderSelectCombo.getSelectedIndex();
        
        if (selectedIndex == -1) {
            orderDetailsPanel.removeAll();
            JLabel emptyLabel = new JLabel("<html><center>Select a parcel<br>Please choose an order from the dropdown above.</center></html>");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            orderDetailsPanel.add(emptyLabel);
            orderDetailsPanel.revalidate();
            orderDetailsPanel.repaint();
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        if (statusCombo != null) statusCombo.setEnabled(true);
        String orderId = selected.split(" - ")[0];
        Order order = orderStorage.findOrder(orderId);
        
        if (order != null) {
            updateOrderDetailsPanel(order);
            updateStatusOptions(order);
        }
    }
    
    private void updateOrderDetailsPanel(Order order) {
        orderDetailsPanel.removeAll();
        
        // Order ID and Status
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel orderIdLabel = new JLabel("Order: " + order.id);
        orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        orderIdLabel.setForeground(PRIMARY_GREEN);
        headerPanel.add(orderIdLabel, BorderLayout.WEST);
        
        String courierStatus = order.getCourierStatus();
        Color statusColor = getStatusColorForBadge(courierStatus);
        JLabel statusLabel = new JLabel("  " + courierStatus + "  ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusColor);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        orderDetailsPanel.add(headerPanel);
        
        // Recipient Info
        JPanel recipientPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        recipientPanel.setOpaque(false);
        recipientPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel recipientLabel = new JLabel("📦 " + order.recipientName);
        recipientLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        recipientPanel.add(recipientLabel);
        
        JLabel phoneLabel = new JLabel("📞 " + order.recipientPhone);
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneLabel.setForeground(TEXT_GRAY);
        recipientPanel.add(phoneLabel);
        
        orderDetailsPanel.add(recipientPanel);
        orderDetailsPanel.add(Box.createVerticalStrut(5));
        
        // Price
        double estimatedCost = order.getEstimatedCost();
        JPanel pricePanel = new JPanel(new BorderLayout());
        pricePanel.setOpaque(false);
        pricePanel.setBackground(new Color(232, 245, 233));
        pricePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        
        JLabel priceTitleLabel = new JLabel("💰 Amount:");
        priceTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pricePanel.add(priceTitleLabel, BorderLayout.WEST);
        
        JLabel priceValueLabel = new JLabel(String.format("RM %.2f", estimatedCost));
        priceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceValueLabel.setForeground(SUCCESS);
        pricePanel.add(priceValueLabel, BorderLayout.EAST);
        
        orderDetailsPanel.add(pricePanel);
        orderDetailsPanel.add(Box.createVerticalStrut(10));
        
        // Timeline Section
        JLabel timelineLabel = new JLabel("📅 Delivery Timeline");
        timelineLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timelineLabel.setForeground(PRIMARY_GREEN);
        orderDetailsPanel.add(timelineLabel);
        orderDetailsPanel.add(Box.createVerticalStrut(8));
        
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setOpaque(false);
        
        // Order Created
        timelinePanel.add(createTimelineRow("Order Created", order.orderDate, true));
        timelinePanel.add(createTimelineConnectorLine(order.pickupTime != null && !order.pickupTime.isEmpty()));
        
        // Picked Up
        boolean pickedUpCompleted = order.pickupTime != null && !order.pickupTime.isEmpty();
        timelinePanel.add(createTimelineRow("Picked Up", 
            pickedUpCompleted ? order.getPickupTimeFormatted() : "Not yet", 
            pickedUpCompleted));
        timelinePanel.add(createTimelineConnectorLine(order.inTransitTime != null && !order.inTransitTime.isEmpty()));
        
        // In Transit
        boolean inTransitCompleted = order.inTransitTime != null && !order.inTransitTime.isEmpty();
        timelinePanel.add(createTimelineRow("In Transit", 
            inTransitCompleted ? order.getInTransitTimeFormatted() : "Not yet", 
            inTransitCompleted));
        timelinePanel.add(createTimelineConnectorLine(order.outForDeliveryTime != null && !order.outForDeliveryTime.isEmpty()));
        
        // Out for Delivery
        boolean outForDeliveryCompleted = order.outForDeliveryTime != null && !order.outForDeliveryTime.isEmpty();
        timelinePanel.add(createTimelineRow("Out for Delivery", 
            outForDeliveryCompleted ? order.getOutForDeliveryTimeFormatted() : "Not yet", 
            outForDeliveryCompleted));
        timelinePanel.add(createTimelineConnectorLine("Delivered".equals(order.status)));
        
        // Delivered
        boolean deliveredCompleted = "Delivered".equals(order.status);
        timelinePanel.add(createTimelineRow("Delivered", 
            deliveredCompleted ? order.getDeliveryTimeFormatted() : "Not yet", 
            deliveredCompleted));
        
        orderDetailsPanel.add(timelinePanel);
        
        orderDetailsPanel.revalidate();
        orderDetailsPanel.repaint();
    }
    
    private JPanel createTimelineRow(String title, String time, boolean completed) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        String icon = completed ? "✅ " : "⏳ ";
        JLabel titleLabel = new JLabel(icon + title);
        titleLabel.setFont(new Font("Segoe UI", completed ? Font.BOLD : Font.PLAIN, 12));
        titleLabel.setForeground(completed ? SUCCESS : TEXT_GRAY);
        row.add(titleLabel, BorderLayout.WEST);
        
        String displayTime = (time != null && !time.isEmpty() && !"Not yet".equals(time)) ? time : "-";
        JLabel timeLabel = new JLabel(displayTime);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(completed ? SUCCESS : TEXT_GRAY);
        row.add(timeLabel, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createTimelineConnectorLine(boolean completed) {
        JPanel connector = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(completed ? SUCCESS : new Color(206, 212, 218));
                g2d.setStroke(new BasicStroke(2));
                int x = 12;
                g2d.drawLine(x, 0, x, getHeight());
            }
        };
        connector.setPreferredSize(new Dimension(25, 20));
        connector.setBackground(new Color(248, 249, 250));
        return connector;
    }
    
    private Color getStatusColorForBadge(String status) {
        switch(status) {
            case "Pending": return WARNING;
            case "Picked Up": return PURPLE;
            case "In Transit": return INFO;
            case "Out for Delivery": return OUT_FOR_DELIVERY_COLOR;
            case "Delivered": return SUCCESS;
            case "Delayed": return DANGER;
            case "Failed": return DANGER;
            default: return PRIMARY_GREEN;
        }
    }
    
    private void updateStatusOptions(Order order) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        String currentCourierStatus = order.getCourierStatus();
        
        model.addElement("-- Select Status --");
        
        switch(currentCourierStatus) {
            case "Pending":
                model.addElement("Picked Up");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Picked Up":
                model.addElement("In Transit");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "In Transit":
                model.addElement("Out for Delivery");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Out for Delivery":
                model.addElement("Delivered");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Delayed":
                model.addElement("Picked Up");
                model.addElement("In Transit");
                model.addElement("Out for Delivery");
                model.addElement("Delivered");
                model.addElement("Failed");
                break;
            default:
                model.addElement("Picked Up");
                model.addElement("In Transit");
                model.addElement("Out for Delivery");
                model.addElement("Delivered");
                model.addElement("Delayed");
                model.addElement("Failed");
        }
        
        statusCombo.setModel(model);
        statusCombo.setSelectedIndex(0);
    }
    
    private boolean isValidCourierTransition(String current, String next) {
        if (current.equals(next)) return false;
        switch(current) {
            case "Pending":
                return next.equals("Picked Up") || next.equals("Delayed") || next.equals("Failed");
            case "Picked Up":
                return next.equals("In Transit") || next.equals("Delayed") || next.equals("Failed");
            case "In Transit":
                return next.equals("Out for Delivery") || next.equals("Delayed") || next.equals("Failed");
            case "Out for Delivery":
                return next.equals("Delivered") || next.equals("Delayed") || next.equals("Failed");
            case "Delayed":
                return next.equals("Picked Up") || next.equals("In Transit") || 
                       next.equals("Out for Delivery") || next.equals("Delivered") || next.equals("Failed");
            default:
                return false;
        }
    }
    
    // ========== 修复: 处理状态更新并记录时间戳 ==========
    
    private void processStatusUpdate() {
        int selectedIndex = orderSelectCombo.getSelectedIndex();
        if (selectedIndex == -1) {
            showNotification("Please select an order to update", WARNING);
            return;
        }
        
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            showNotification("Please select an order to update", WARNING);
            return;
        }
        
        String orderId = selected.split(" - ")[0];
        
        Object statusSelected = statusCombo.getSelectedItem();
        if (statusSelected == null || statusSelected.toString().equals("-- Select Status --")) {
            showNotification("Please select a new status", WARNING);
            return;
        }
        
        String newCourierStatus = statusSelected.toString();
        
        if ("Failed".equals(newCourierStatus)) {
            Order order = orderStorage.findOrder(orderId);
            if (order != null) showFailedDeliveryDialog(order);
            return;
        }
        
        String signature = signatureArea.getText().trim();
        if (signature.isEmpty()) {
            showNotification("Recipient signature is required", WARNING);
            signatureArea.requestFocus();
            return;
        }
        
        Order order = orderStorage.findOrder(orderId);
        if (order == null) {
            showNotification("Order not found", DANGER);
            return;
        }
        
        if ("Delivered".equals(order.status)) {
            showNotification("This order has already been delivered", WARNING);
            return;
        }
        
        if ("Failed".equals(order.status)) {
            showNotification("This order has already been marked as failed.", WARNING);
            return;
        }
        
        String currentCourierStatus = order.getCourierStatus();
        
        if (!isValidCourierTransition(currentCourierStatus, newCourierStatus)) {
            showNotification("Invalid status transition from '" + currentCourierStatus + "' to '" + newCourierStatus + "'", WARNING);
            return;
        }
        
        // 确认对话框显示将要设置的状态
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Update Order Status\n\nOrder: %s\nRecipient: %s\nCurrent Status: %s\nNew Status: %s\n\nSignature: %s\n\nAmount: %s",
                orderId, order.recipientName, currentCourierStatus, newCourierStatus, signature, order.getFormattedEstimatedCost()),
            "Confirm Status Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        System.out.println("=== Updating Order Status ===");
        System.out.println("Order ID: " + order.id);
        System.out.println("New Status: " + newCourierStatus);
        System.out.println("Current time: " + now);
        
        // 根据新状态设置对应的时间戳
        switch(newCourierStatus) {
            case "Picked Up":
                if (order.pickupTime == null || order.pickupTime.isEmpty()) {
                    order.pickupTime = now;
                    System.out.println("Set pickupTime: " + order.pickupTime);
                    
                    // 重要：司机取件后，将司机状态改为 On Delivery
                    updateDriverStatusToOnDelivery(order.driverId);
                }
                break;
                case "In Transit":
                    if (order.inTransitTime == null || order.inTransitTime.isEmpty()) {
                        order.inTransitTime = now;
                        System.out.println("Set inTransitTime: " + order.inTransitTime);
                    }
                    break;
                case "Out for Delivery":
                    if (order.outForDeliveryTime == null || order.outForDeliveryTime.isEmpty()) {
                        order.outForDeliveryTime = now;
                        System.out.println("Set outForDeliveryTime: " + order.outForDeliveryTime);
                    }
                    break;
                case "Delivered":
                    if (order.deliveryTime == null || order.deliveryTime.isEmpty()) {
                        order.deliveryTime = now;
                        System.out.println("Set deliveryTime: " + order.deliveryTime);
                    }
                    if (order.actualDelivery == null || order.actualDelivery.isEmpty()) {
                        order.actualDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        System.out.println("Set actualDelivery: " + order.actualDelivery);
                    }
                    break;
                case "Delayed":
                    order.reason = "Delayed by courier: " + signature;
                    break;
            }
            
            // 更新系统状态
            order.updateFromCourierStatus(newCourierStatus);
            
            // 添加备注
            order.notes = (order.notes != null ? order.notes + "\n" : "") + 
                "Status updated to " + newCourierStatus + " - Signature: " + signature + 
                " on " + now;
            
            // 保存订单
            orderStorage.updateOrder(order);
            orderStorage.forceReload();
            
            // 验证保存是否成功
            Order savedOrder = orderStorage.findOrder(orderId);
            if (savedOrder != null) {
                System.out.println("Order saved successfully - New status: " + savedOrder.status);
                System.out.println("Pickup time: " + savedOrder.pickupTime);
                System.out.println("In transit time: " + savedOrder.inTransitTime);
                System.out.println("Out for delivery time: " + savedOrder.outForDeliveryTime);
                System.out.println("Delivery time: " + savedOrder.deliveryTime);
            }
            
            showNotification("Order status updated to: " + newCourierStatus, SUCCESS);
            if (parentDashboard != null) parentDashboard.refreshData();
            clearForm();
            
            JOptionPane.showMessageDialog(this,
                String.format("Status Updated Successfully!\n\nOrder ID: %s\nNew Status: %s\nRecipient: %s\nTime: %s\nAmount: %s",
                    orderId, newCourierStatus, order.recipientName, now, order.getFormattedEstimatedCost()),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateDriverStatusToOnDelivery(String driverId) {
    if (driverId == null || driverId.isEmpty()) return;
    
    try {
        logistics.driver.DriverStorage driverStorage = new logistics.driver.DriverStorage();
        logistics.driver.Driver driver = driverStorage.findDriver(driverId);
        if (driver != null && !"On Delivery".equals(driver.workStatus)) {
            driver.workStatus = "On Delivery";
            driverStorage.updateDriver(driver);
            System.out.println("Driver " + driverId + " status updated to On Delivery");
        }
    } catch (Exception e) {
        System.err.println("Error updating driver status: " + e.getMessage());
    }
}
    
    private void showFailedDeliveryDialog(Order order) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Mark Delivery as Failed - " + order.id, true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Failed Delivery Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(DANGER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int y = 0;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel orderInfoLabel = new JLabel("<html><b>Order:</b> " + order.id + "<br><b>Recipient:</b> " + order.recipientName + 
            "<br><b>Address:</b> " + order.recipientAddress + "<br><b>Amount:</b> " + order.getFormattedEstimatedCost() + "</html>");
        orderInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderInfoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        orderInfoLabel.setBackground(new Color(248, 249, 250));
        orderInfoLabel.setOpaque(true);
        formPanel.add(orderInfoLabel, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel reasonLabel = new JLabel("Failure Reason:*");
        reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(reasonLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> reasonCombo = new JComboBox<>(new String[]{
            "Select reason...",
            "Recipient not available",
            "Wrong address provided",
            "Recipient refused delivery",
            "Package damaged",
            "Delivery area restricted",
            "No one to receive",
            "Other"
        });
        reasonCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(reasonCombo, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y;
        JLabel descLabel = new JLabel("Details:*");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(descLabel, gbc);
        
        gbc.gridx = 1;
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        scrollPane.setPreferredSize(new Dimension(250, 80));
        formPanel.add(scrollPane, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y;
        JLabel sigLabel = new JLabel("Signature:*");
        sigLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(sigLabel, gbc);
        
        gbc.gridx = 1;
        JTextArea failedSignatureArea = new JTextArea(2, 20);
        failedSignatureArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        failedSignatureArea.setLineWrap(true);
        failedSignatureArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane sigScroll = new JScrollPane(failedSignatureArea);
        sigScroll.setPreferredSize(new Dimension(250, 50));
        formPanel.add(sigScroll, gbc);
        y++;
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(TEXT_GRAY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton confirmBtn = new JButton("Mark as Failed");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(DANGER);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setPreferredSize(new Dimension(130, 35));
        confirmBtn.addActionListener(e -> {
            String selectedReason = (String) reasonCombo.getSelectedItem();
            String details = reasonArea.getText().trim();
            String signature = failedSignatureArea.getText().trim();
            
            if (selectedReason == null || selectedReason.equals("Select reason...")) {
                showNotification("Please select a failure reason", WARNING);
                return;
            }
            if (details.isEmpty()) {
                showNotification("Please provide failure details", WARNING);
                return;
            }
            if (signature.isEmpty()) {
                showNotification("Signature is required", WARNING);
                return;
            }
            
            String fullReason = selectedReason + ": " + details;
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to mark this delivery as FAILED?\n\n" +
                "Order: " + order.id + "\nReason: " + fullReason + "\nAmount: " + order.getFormattedEstimatedCost(),
                "Confirm Failed Delivery", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                order.markAsFailed(fullReason);
                order.deliveryTime = now;
                order.notes = (order.notes != null ? order.notes + "\n" : "") +
                    "FAILED DELIVERY - Reason: " + fullReason + " - Signature: " + signature + " on " + now;
                
                orderStorage.updateOrder(order);
                orderStorage.forceReload();
                
                if (parentDashboard != null) parentDashboard.refreshData();
                clearForm();
                dialog.dispose();
                
                JOptionPane.showMessageDialog(this,
                    "Delivery marked as Failed\n\nOrder ID: " + order.id + "\nReason: " + fullReason,
                    "Failed Delivery", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showNotification(String message, Color color) {
        if (parentDashboard != null) {
            parentDashboard.showNotification(message, color);
        } else {
            String title = color == SUCCESS ? "Success" : (color == WARNING ? "Warning" : "Error");
            JOptionPane.showMessageDialog(this, message, title, 
                color == SUCCESS ? JOptionPane.INFORMATION_MESSAGE : 
                (color == WARNING ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE));
        }
    }
}