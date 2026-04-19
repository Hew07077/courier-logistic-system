package courier;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
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
    
    private static final String ORDERS_FILE = "orders.txt";
    
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
        String currentSelectedOrderId = null;
        if (orderSelectCombo != null && orderSelectCombo.getSelectedIndex() != -1) {
            String selected = (String) orderSelectCombo.getSelectedItem();
            if (selected != null && !selected.equals("No active orders")) {
                currentSelectedOrderId = selected.split(" - ")[0];
            }
        }
        
        refreshOrderSelectCombo();
        
        if (currentSelectedOrderId != null) {
            for (int i = 0; i < orderSelectCombo.getItemCount(); i++) {
                String item = orderSelectCombo.getItemAt(i);
                if (item != null && item.startsWith(currentSelectedOrderId)) {
                    orderSelectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
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
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
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
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        formPanel.add(separator, gbc);
        row++;
        
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
        formPanel.add(statusCombo, gbc);
        row++;
        
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
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel noteLabel = new JLabel("* Signature is required for all status updates.", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(TEXT_GRAY);
        formPanel.add(noteLabel, gbc);
        row++;
        
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
        
        String previouslySelected = null;
        if (orderSelectCombo.getSelectedIndex() != -1) {
            String selected = (String) orderSelectCombo.getSelectedItem();
            if (selected != null && !selected.equals("No active orders")) {
                previouslySelected = selected.split(" - ")[0];
            }
        }
        
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
        
        if (previouslySelected != null) {
            for (int i = 0; i < orderSelectCombo.getItemCount(); i++) {
                String item = orderSelectCombo.getItemAt(i);
                if (item != null && item.startsWith(previouslySelected)) {
                    orderSelectCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            orderSelectCombo.setSelectedIndex(-1);
        }
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
        
        JLabel timelineLabel = new JLabel("📅 Delivery Timeline");
        timelineLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timelineLabel.setForeground(PRIMARY_GREEN);
        orderDetailsPanel.add(timelineLabel);
        orderDetailsPanel.add(Box.createVerticalStrut(8));
        
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setOpaque(false);
        
        // Helper to check if time is valid (not null, not empty, not "0")
        java.util.function.Function<String, Boolean> isValidTime = (timeValue) -> {
            return timeValue != null && !timeValue.isEmpty() && !"0".equals(timeValue) && !"null".equals(timeValue);
        };
        
        timelinePanel.add(createTimelineRow("Order Created", order.orderDate, true));
        timelinePanel.add(createTimelineConnectorLine(isValidTime.apply(order.pickupTime)));
        
        boolean pickedUpCompleted = isValidTime.apply(order.pickupTime);
        timelinePanel.add(createTimelineRow("Picked Up", 
            pickedUpCompleted ? formatTime(order.pickupTime) : "Not yet", 
            pickedUpCompleted));
        timelinePanel.add(createTimelineConnectorLine(isValidTime.apply(order.inTransitTime)));
        
        boolean inTransitCompleted = isValidTime.apply(order.inTransitTime);
        timelinePanel.add(createTimelineRow("In Transit", 
            inTransitCompleted ? formatTime(order.inTransitTime) : "Not yet", 
            inTransitCompleted));
        timelinePanel.add(createTimelineConnectorLine(isValidTime.apply(order.outForDeliveryTime)));
        
        boolean outForDeliveryCompleted = isValidTime.apply(order.outForDeliveryTime);
        timelinePanel.add(createTimelineRow("Out for Delivery", 
            outForDeliveryCompleted ? formatTime(order.outForDeliveryTime) : "Not yet", 
            outForDeliveryCompleted));
        timelinePanel.add(createTimelineConnectorLine(isValidTime.apply(order.deliveryTime)));
        
        boolean deliveredCompleted = isValidTime.apply(order.deliveryTime);
        timelinePanel.add(createTimelineRow("Delivered", 
            deliveredCompleted ? formatTime(order.deliveryTime) : "Not yet", 
            deliveredCompleted));
        
        orderDetailsPanel.add(timelinePanel);
        
        orderDetailsPanel.revalidate();
        orderDetailsPanel.repaint();
    }
    
    private String formatTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty() || "0".equals(dateTime) || "null".equals(dateTime)) {
            return "Not yet";
        }
        try {
            if (dateTime.length() >= 16) {
                return dateTime.substring(11, 16);
            }
            return dateTime;
        } catch (Exception e) {
            return "Not yet";
        }
    }
    
    private JPanel createTimelineRow(String title, String time, boolean completed) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Check if time is valid (not null, not empty, not "0", not "Not yet")
        boolean hasValidTime = time != null && !time.isEmpty() && !"0".equals(time) && !"Not yet".equals(time);
        boolean isActuallyCompleted = completed && hasValidTime;
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", isActuallyCompleted ? Font.BOLD : Font.PLAIN, 12));
        titleLabel.setForeground(isActuallyCompleted ? SUCCESS : TEXT_GRAY);
        row.add(titleLabel, BorderLayout.WEST);
        
        String displayTime = hasValidTime ? time : "-";
        JLabel timeLabel = new JLabel(displayTime);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(isActuallyCompleted ? SUCCESS : TEXT_GRAY);
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
    
    // ========== PROCESS STATUS UPDATE ==========
    
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
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Update Order Status\n\nOrder: %s\nRecipient: %s\nCurrent Status: %s\nNew Status: %s\n\nSignature: %s\n\nAmount: %s",
                orderId, order.recipientName, currentCourierStatus, newCourierStatus, signature, order.getFormattedEstimatedCost()),
            "Confirm Status Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            System.out.println("=== UPDATING ORDER: " + order.id + " ===");
            System.out.println("New Status: " + newCourierStatus);
            System.out.println("Timestamp: " + now);
            
            // Update the order fields directly
            switch(newCourierStatus) {
                case "Picked Up":
                    order.pickupTime = now;
                    System.out.println("Set pickupTime: '" + order.pickupTime + "'");
                    updateDriverStatusToOnDelivery(order.driverId);
                    order.status = "Picked Up";
                    break;
                case "In Transit":
                    order.inTransitTime = now;
                    System.out.println("Set inTransitTime: '" + order.inTransitTime + "'");
                    order.status = "In Transit";
                    break;
                case "Out for Delivery":
                    order.outForDeliveryTime = now;
                    System.out.println("Set outForDeliveryTime: '" + order.outForDeliveryTime + "'");
                    order.status = "Out for Delivery";
                    break;
                case "Delivered":
                    order.deliveryTime = now;
                    System.out.println("Set deliveryTime: '" + order.deliveryTime + "'");
                    
                    if (order.actualDelivery == null || order.actualDelivery.isEmpty()) {
                        order.actualDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        System.out.println("Set actualDelivery: '" + order.actualDelivery + "'");
                    }
                    order.status = "Delivered";
                    break;
                case "Delayed":
                    order.reason = "Delayed by courier: " + signature;
                    order.status = "Delayed";
                    break;
            }
            
            // Add notes
            String newNote = "Status updated to " + newCourierStatus + " - Signature: " + signature + " on " + now;
            if (order.notes != null && !order.notes.isEmpty()) {
                order.notes = order.notes + "\n" + newNote;
            } else {
                order.notes = newNote;
            }
            
            System.out.println("=== BEFORE SAVE - FINAL CHECK ===");
            System.out.println("outForDeliveryTime: '" + order.outForDeliveryTime + "'");
            System.out.println("deliveryTime: '" + order.deliveryTime + "'");
            System.out.println("status: '" + order.status + "'");
            
            // Save directly to file
            boolean saved = saveOrderDirectlyToFile(order);
            
            if (saved) {
                System.out.println("Order saved successfully!");
                orderStorage.updateOrder(order);
                
                showNotification("Order status updated to: " + newCourierStatus, SUCCESS);
                if (parentDashboard != null) parentDashboard.refreshData();
                
                // Only clear the signature area
                signatureArea.setText("");
                
                // Refresh the details panel to show updated status
                updateOrderDetailsAndStatusOptions();
                
                JOptionPane.showMessageDialog(this,
                    String.format("Status Updated Successfully!\n\nOrder ID: %s\nNew Status: %s\nRecipient: %s\nTime: %s\nAmount: %s",
                        orderId, newCourierStatus, order.recipientName, now, order.getFormattedEstimatedCost()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showNotification("Error saving order. Please try again.", DANGER);
            }
        }
    }
    
    /**
     * DIRECT FILE SAVE - writes the order directly to orders.txt
     */
    private boolean saveOrderDirectlyToFile(Order order) {
        BufferedReader reader = null;
        PrintWriter writer = null;
        
        try {
            File file = new File(ORDERS_FILE);
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            boolean orderFound = false;
            int orderLineIndex = -1;
            
            System.out.println("=== DIRECT FILE SAVE ===");
            System.out.println("Order ID: " + order.id);
            System.out.println("outForDeliveryTime: '" + order.outForDeliveryTime + "'");
            System.out.println("deliveryTime: '" + order.deliveryTime + "'");
            System.out.println("status: '" + order.status + "'");
            
            // Read existing file
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                String line;
                int lineIndex = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#") || line.trim().isEmpty()) {
                        lines.add(line);
                    } else if (line.contains(order.id) && !line.startsWith("#")) {
                        orderFound = true;
                        orderLineIndex = lineIndex;
                        System.out.println("Found existing order at line " + lineIndex);
                    } else {
                        lines.add(line);
                    }
                    lineIndex++;
                }
                reader.close();
            }
            
            // Build the order line
            String orderLine = buildOrderFileString(order);
            
            if (orderFound) {
                lines.add(orderLineIndex, orderLine);
                System.out.println("Updated existing order at line " + orderLineIndex);
            } else {
                // Check if header exists
                boolean hasHeader = false;
                for (String line : lines) {
                    if (line.startsWith("#")) {
                        hasHeader = true;
                        break;
                    }
                }
                if (!hasHeader) {
                    lines.add(0, "# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|inTransitTime|outForDeliveryTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate");
                }
                lines.add(orderLine);
                System.out.println("Added new order");
            }
            
            // Write back to file
            writer = new PrintWriter(new FileWriter(file));
            for (String line : lines) {
                writer.println(line);
            }
            writer.flush();
            
            System.out.println("File written successfully");
            
            // Verify
            verifySavedOrder(order.id);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String buildOrderFileString(Order order) {
        String[] fields = new String[31];
        fields[0] = safeString(order.id);
        fields[1] = safeString(order.customerName);
        fields[2] = safeString(order.customerPhone);
        fields[3] = safeString(order.customerEmail);
        fields[4] = safeString(order.customerAddress);
        fields[5] = safeString(order.recipientName);
        fields[6] = safeString(order.recipientPhone);
        fields[7] = safeString(order.recipientAddress);
        fields[8] = safeString(order.status);
        fields[9] = safeString(order.orderDate);
        fields[10] = safeString(order.estimatedDelivery);
        fields[11] = safeString(order.actualDelivery);
        fields[12] = safeString(order.driverId);
        fields[13] = safeString(order.vehicleId);
        fields[14] = String.valueOf(order.weight);
        fields[15] = safeString(order.dimensions);
        fields[16] = safeString(order.notes != null ? order.notes.replace("\n", " ").replace("\r", " ") : "");
        fields[17] = safeString(order.reason);
        fields[18] = safeString(order.pickupTime);
        fields[19] = safeString(order.inTransitTime);
        fields[20] = safeString(order.outForDeliveryTime);
        fields[21] = safeString(order.deliveryTime);
        fields[22] = String.valueOf(order.distance);
        fields[23] = String.valueOf(order.fuelUsed);
        fields[24] = safeString(order.deliveryPhoto);
        fields[25] = safeString(order.recipientSignature);
        fields[26] = String.valueOf(order.onTime);
        fields[27] = safeString(order.paymentStatus);
        fields[28] = safeString(order.paymentMethod);
        fields[29] = safeString(order.transactionId);
        fields[30] = safeString(order.paymentDate);
        
        System.out.println("=== BUILD ORDER STRING ===");
        System.out.println("outForDeliveryTime at index 20: '" + fields[20] + "'");
        System.out.println("deliveryTime at index 21: '" + fields[21] + "'");
        
        return String.join("|", fields);
    }
    
    private void verifySavedOrder(String orderId) {
        try {
            File file = new File(ORDERS_FILE);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(orderId) && !line.startsWith("#")) {
                        String[] parts = line.split("\\|", -1);
                        System.out.println("=== VERIFICATION ===");
                        System.out.println("Order ID from file: " + (parts.length > 0 ? parts[0] : "N/A"));
                        System.out.println("outForDeliveryTime from file (index 20): " + (parts.length > 20 ? parts[20] : "N/A"));
                        System.out.println("deliveryTime from file (index 21): " + (parts.length > 21 ? parts[21] : "N/A"));
                        System.out.println("status from file (index 8): " + (parts.length > 8 ? parts[8] : "N/A"));
                        break;
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            System.err.println("Verification error: " + e.getMessage());
        }
    }
    
    private String safeString(String s) {
        if (s == null || s.isEmpty() || "0".equals(s) || "null".equals(s)) {
            return "";
        }
        return s;
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
                order.status = "Failed";
                order.reason = fullReason;
                order.deliveryTime = now;
                order.notes = (order.notes != null ? order.notes + "\n" : "") +
                    "FAILED DELIVERY - Reason: " + fullReason + " - Signature: " + signature + " on " + now;
                
                saveOrderDirectlyToFile(order);
                orderStorage.updateOrder(order);
                
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