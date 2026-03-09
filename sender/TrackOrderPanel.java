package sender;

import logistics.orders.Order;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class TrackOrderPanel extends JPanel {
    private SenderDashboard dashboard;
    private JTextField trackOrderField;
    private JPanel trackingResultPanel;
    private Order currentOrder;
    private JButton refreshBtn;
    private JButton myOrdersBtn;

    public TrackOrderPanel(SenderDashboard dashboard) {
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

        JLabel titleLabel = new JLabel("Track Your Order");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(new Color(40, 167, 69));
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshOrders());
        rightPanel.add(refreshBtn);

        myOrdersBtn = new JButton("📋 My Orders");
        myOrdersBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        myOrdersBtn.setForeground(Color.WHITE);
        myOrdersBtn.setBackground(new Color(0, 123, 255));
        myOrdersBtn.setBorderPainted(false);
        myOrdersBtn.setFocusPainted(false);
        myOrdersBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        myOrdersBtn.addActionListener(e -> showMyOrders());
        rightPanel.add(myOrdersBtn);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void refreshOrders() {
        FileDataManager.getInstance().getAllOrders();
        showWelcomeMessage();
        JOptionPane.showMessageDialog(this, 
            "Orders refreshed!", 
            "Refreshed", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        JLabel trackLabel = new JLabel("Enter Tracking Number:");
        trackLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(trackLabel);

        trackOrderField = new JTextField(20);
        trackOrderField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        trackOrderField.setPreferredSize(new Dimension(200, 35));
        trackOrderField.addActionListener(e -> trackOrder());
        searchPanel.add(trackOrderField);

        JButton trackBtn = new JButton("Track Package");
        trackBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        trackBtn.setForeground(Color.WHITE);
        trackBtn.setBackground(new Color(0, 123, 255));
        trackBtn.setBorderPainted(false);
        trackBtn.setFocusPainted(false);
        trackBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trackBtn.addActionListener(e -> trackOrder());
        searchPanel.add(trackBtn);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Tracking result panel
        trackingResultPanel = new JPanel();
        trackingResultPanel.setLayout(new BoxLayout(trackingResultPanel, BoxLayout.Y_AXIS));
        trackingResultPanel.setBackground(Color.WHITE);
        trackingResultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        showWelcomeMessage();

        JScrollPane scrollPane = new JScrollPane(trackingResultPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    // MODIFIED: showWelcomeMessage method
    private void showWelcomeMessage() {
        trackingResultPanel.removeAll();
        
        JLabel welcomeLabel = new JLabel("📦 Track Your Packages");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(0, 123, 255));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(welcomeLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(20));
        
        // Show user's recent orders
        String userEmail = dashboard.getSenderEmail();
        
        List<Order> userOrders = new ArrayList<>();
        
        // Use if-else to check if the sender is a demo user
        if (userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
            // If it's the demo sender, get demo orders
            userOrders = DemoDataManager.getInstance().getDemoOrders();
            System.out.println("TrackOrderPanel: Loading " + userOrders.size() + " demo orders");
        } else {
            // Otherwise, get regular orders from the system
            List<Order> allOrders = FileDataManager.getInstance().getAllOrders();
            
            for (Order order : allOrders) {
                if (order.customerEmail != null && userEmail != null) {
                    if (order.customerEmail.trim().equals(userEmail.trim())) {
                        userOrders.add(order);
                    }
                }
            }
        }
        
        if (!userOrders.isEmpty()) {
            JLabel recentLabel = new JLabel("Your Recent Orders:");
            recentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            recentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(recentLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(10));
            
            // Show only the 5 most recent orders
            int startIndex = Math.max(0, userOrders.size() - 5);
            for (int i = startIndex; i < userOrders.size(); i++) {
                Order order = userOrders.get(i);
                JPanel orderCard = createOrderCard(order);
                trackingResultPanel.add(orderCard);
                trackingResultPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel emptyLabel = new JLabel("You haven't created any orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(108, 117, 125));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(emptyLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(15));
            
            JButton newOrderBtn = new JButton("+ Create Your First Order");
            newOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            newOrderBtn.setForeground(Color.WHITE);
            newOrderBtn.setBackground(new Color(40, 167, 69));
            newOrderBtn.setBorderPainted(false);
            newOrderBtn.setFocusPainted(false);
            newOrderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            newOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            newOrderBtn.setMaximumSize(new Dimension(200, 40));
            newOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));
            trackingResultPanel.add(newOrderBtn);
        }
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private JPanel createOrderCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setOpaque(false);
        
        JLabel idLabel = new JLabel("📦 " + order.id);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftPanel.add(idLabel);
        
        String fromCity = extractCity(order.customerAddress);
        String toCity = extractCity(order.recipientAddress);
        JLabel routeLabel = new JLabel(fromCity + " → " + toCity);
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        routeLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(routeLabel);
        
        card.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        rightPanel.setOpaque(false);
        
        JLabel statusLabel = new JLabel(order.status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setForeground(getStatusColor(order.status));
        rightPanel.add(statusLabel);
        
        JLabel dateLabel = new JLabel(order.orderDate);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(108, 117, 125));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(dateLabel);
        
        card.add(rightPanel, BorderLayout.EAST);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                trackOrderField.setText(order.id);
                displayTrackingResult(order.id);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(230, 240, 255));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
            }
        });
        
        return card;
    }

    private String extractCity(String address) {
        if (address != null && address.contains(",")) {
            return address.substring(0, address.indexOf(",")).trim();
        }
        return address != null ? address : "N/A";
    }

    private Color getStatusColor(String status) {
        switch(status) {
            case "Delivered": return new Color(40, 167, 69);
            case "In Transit": return new Color(0, 123, 255);
            case "Pending": return new Color(255, 193, 7);
            case "Cancelled": return new Color(220, 53, 69);
            case "Delayed": return new Color(255, 87, 34);
            default: return new Color(108, 117, 125);
        }
    }

    // MODIFIED: showMyOrders method
    private void showMyOrders() {
        trackingResultPanel.removeAll();
        
        String userEmail = dashboard.getSenderEmail();
        
        List<Order> userOrders = new ArrayList<>();
        
        // Use if-else to check if the sender is a demo user
        if (userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
            // If it's the demo sender, get demo orders
            userOrders = DemoDataManager.getInstance().getDemoOrders();
        } else {
            // Otherwise, get regular orders from the system
            List<Order> allOrders = FileDataManager.getInstance().getAllOrders();
            
            for (Order order : allOrders) {
                if (order.customerEmail != null && userEmail != null) {
                    if (order.customerEmail.trim().equals(userEmail.trim())) {
                        userOrders.add(order);
                    }
                }
            }
        }
        
        JLabel titleLabel = new JLabel("My Orders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 123, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(titleLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        if (!userOrders.isEmpty()) {
            for (Order o : userOrders) {
                JPanel orderCard = createOrderCard(o);
                trackingResultPanel.add(orderCard);
                trackingResultPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel emptyLabel = new JLabel("You haven't created any orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(108, 117, 125));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(emptyLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(10));
            
            JButton newOrderBtn = new JButton("+ Create New Order");
            newOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            newOrderBtn.setForeground(Color.WHITE);
            newOrderBtn.setBackground(new Color(40, 167, 69));
            newOrderBtn.setBorderPainted(false);
            newOrderBtn.setFocusPainted(false);
            newOrderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            newOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            newOrderBtn.setMaximumSize(new Dimension(150, 35));
            newOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));
            trackingResultPanel.add(newOrderBtn);
        }
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        addBackButton();
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private void addBackButton() {
        JButton backBtn = new JButton("← Back to Search");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setForeground(new Color(108, 117, 125));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(120, 35));
        backBtn.addActionListener(e -> showWelcomeMessage());
        trackingResultPanel.add(backBtn);
    }

    public void setTrackingNumber(String trackingNumber) {
        trackOrderField.setText(trackingNumber);
        trackOrder();
    }

    private void trackOrder() {
        String trackingNumber = trackOrderField.getText().trim();
        if (trackingNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a tracking number", 
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        displayTrackingResult(trackingNumber);
    }

    // MODIFIED: displayTrackingResult method
    private void displayTrackingResult(String trackingNumber) {
        trackingResultPanel.removeAll();
        
        currentOrder = FileDataManager.getInstance().getOrderById(trackingNumber);
        String userEmail = dashboard.getSenderEmail();

        if (currentOrder == null) {
            JLabel notFoundLabel = new JLabel("❌ Order not found: " + trackingNumber);
            notFoundLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            notFoundLabel.setForeground(new Color(220, 53, 69));
            notFoundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(notFoundLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(20));
            
            JLabel suggestionLabel = new JLabel("Please check your order number and try again.");
            suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            suggestionLabel.setForeground(new Color(108, 117, 125));
            suggestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(suggestionLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(15));
            addBackButton();
        } else {
            // Check if this order belongs to the logged-in user
            boolean isAuthorized = false;
            
            if (currentOrder.customerEmail != null && userEmail != null) {
                if (currentOrder.customerEmail.trim().equalsIgnoreCase(userEmail.trim())) {
                    isAuthorized = true;
                }
            }
            
            // Also check if it's a demo user with demo orders
            if (userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
                List<Order> demoOrders = DemoDataManager.getInstance().getDemoOrders();
                for (Order demoOrder : demoOrders) {
                    if (demoOrder.id.equals(trackingNumber)) {
                        isAuthorized = true;
                        currentOrder = demoOrder;
                        break;
                    }
                }
            }
            
            if (isAuthorized) {
                displayOrderDetails(currentOrder);
            } else {
                JLabel notAuthorizedLabel = new JLabel("⚠️ You are not authorized to view this order");
                notAuthorizedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                notAuthorizedLabel.setForeground(new Color(255, 193, 7));
                notAuthorizedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                trackingResultPanel.add(notAuthorizedLabel);
                
                trackingResultPanel.add(Box.createVerticalStrut(15));
                addBackButton();
            }
        }
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private void displayOrderDetails(Order order) {
        // Header with clickable area
        JPanel headerPanel = createClickableHeader(order);
        trackingResultPanel.add(headerPanel);
        trackingResultPanel.add(Box.createVerticalStrut(15));

        // Status Panel
        trackingResultPanel.add(createStatusPanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));

        // Address Panel
        trackingResultPanel.add(createAddressPanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));

        // Timeline Panel
        trackingResultPanel.add(createTimelinePanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));

        // Package Details Panel
        trackingResultPanel.add(createPackagePanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(20));

        // View Details Button
        JButton detailsBtn = createDetailsButton(order);
        trackingResultPanel.add(detailsBtn);
        
        trackingResultPanel.add(Box.createVerticalStrut(10));
        addBackButton();
    }

    private JPanel createClickableHeader(Order order) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel headerLabel = new JLabel("📦 Order " + order.id);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JLabel clickIcon = new JLabel("🔍 Click for full details");
        clickIcon.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        clickIcon.setForeground(new Color(108, 117, 125));
        headerPanel.add(clickIcon, BorderLayout.EAST);
        
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showFullOrderDetails(order);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                headerPanel.setBackground(new Color(230, 240, 255));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                headerPanel.setBackground(new Color(248, 249, 250));
            }
        });
        
        return headerPanel;
    }

    private JPanel createStatusPanel(Order order) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel statusIcon = new JLabel(getStatusIcon(order.status));
        statusIcon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        panel.add(statusIcon, BorderLayout.WEST);

        JPanel statusInfo = new JPanel(new GridLayout(2, 1));
        statusInfo.setOpaque(false);
        
        JLabel statusLabel = new JLabel("Current Status: " + order.status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(getStatusColor(order.status));
        statusInfo.add(statusLabel);
        
        JLabel updateLabel = new JLabel("Last updated: " + order.orderDate);
        updateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        updateLabel.setForeground(new Color(108, 117, 125));
        statusInfo.add(updateLabel);
        
        panel.add(statusInfo, BorderLayout.CENTER);

        return panel;
    }

    private String getStatusIcon(String status) {
        switch(status) {
            case "Delivered": return "✅";
            case "In Transit": return "🚚";
            case "Pending": return "⏳";
            case "Cancelled": return "❌";
            case "Delayed": return "⚠️";
            default: return "📦";
        }
    }

    private JPanel createAddressPanel(Order order) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // From address
        JPanel fromPanel = new JPanel(new BorderLayout());
        fromPanel.setOpaque(false);
        JLabel fromLabel = new JLabel("📤 FROM:");
        fromLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        fromLabel.setForeground(new Color(108, 117, 125));
        fromPanel.add(fromLabel, BorderLayout.NORTH);
        
        JLabel fromAddress = new JLabel("<html>" + order.customerAddress + "</html>");
        fromAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fromPanel.add(fromAddress, BorderLayout.CENTER);
        panel.add(fromPanel);

        // To address
        JPanel toPanel = new JPanel(new BorderLayout());
        toPanel.setOpaque(false);
        JLabel toLabel = new JLabel("📥 TO:");
        toLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        toLabel.setForeground(new Color(108, 117, 125));
        toPanel.add(toLabel, BorderLayout.NORTH);
        
        JLabel toAddress = new JLabel("<html>" + order.recipientAddress + "</html>");
        toAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toPanel.add(toAddress, BorderLayout.CENTER);
        panel.add(toPanel);

        return panel;
    }

    private JPanel createTimelinePanel(Order order) {
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(Color.WHITE);
        timelinePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            "Tracking Timeline",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        timelinePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        timelinePanel.add(createTimelineEvent("Order Placed", order.customerName, order.orderDate, true));
        
        if ("Pending".equals(order.status)) {
            timelinePanel.add(createTimelineEvent("Awaiting Processing", "Sorting Facility", 
                "Waiting for driver assignment", false));
        }
        
        if (order.driverId != null && !order.driverId.isEmpty()) {
            timelinePanel.add(createTimelineEvent("Driver Assigned", "Driver: " + order.driverId, 
                order.orderDate, true));
        }
        
        if ("In Transit".equals(order.status) || "Delayed".equals(order.status)) {
            String status = "In Transit";
            if ("Delayed".equals(order.status)) {
                status = "Delayed - " + (order.reason != null ? order.reason : "Unknown reason");
            }
            timelinePanel.add(createTimelineEvent(status, "On the way to destination", 
                order.estimatedDelivery != null ? "Est: " + order.estimatedDelivery : "In progress", 
                "In Transit".equals(order.status)));
        }
        
        if ("Delivered".equals(order.status)) {
            timelinePanel.add(createTimelineEvent("Delivered", "Package delivered", 
                order.estimatedDelivery != null ? order.estimatedDelivery : "Completed", true));
        }

        return timelinePanel;
    }

    private JPanel createTimelineEvent(String status, String location, String time, boolean completed) {
        JPanel eventPanel = new JPanel(new BorderLayout());
        eventPanel.setOpaque(false);
        eventPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setOpaque(false);

        String statusText = completed ? "✓ " + status : "○ " + status;
        
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Segoe UI", completed ? Font.BOLD : Font.PLAIN, 13));
        statusLabel.setForeground(completed ? new Color(40, 167, 69) : new Color(108, 117, 125));
        leftPanel.add(statusLabel);

        JLabel locationLabel = new JLabel(location);
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locationLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(locationLabel);

        eventPanel.add(leftPanel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(108, 117, 125));
        eventPanel.add(timeLabel, BorderLayout.EAST);

        return eventPanel;
    }

    private JPanel createPackagePanel(Order order) {
        JPanel packagePanel = new JPanel(new GridLayout(4, 2, 10, 5));
        packagePanel.setBackground(new Color(248, 249, 250));
        packagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            "Package Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        packagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        addInfoRow(packagePanel, "Weight:", String.format("%.2f kg", order.weight));
        addInfoRow(packagePanel, "Dimensions:", order.dimensions + " cm");
        
        String packageType = extractPackageType(order.notes);
        addInfoRow(packagePanel, "Package Type:", packageType);
        
        String cost = extractCost(order.notes);
        addInfoRow(packagePanel, "Cost:", cost);

        return packagePanel;
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

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(labelComp);

        JLabel valueComp = new JLabel(value != null ? value : "-");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(valueComp);
    }

    private JButton createDetailsButton(Order order) {
        JButton detailsBtn = new JButton("View Full Order Details");
        detailsBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsBtn.setForeground(Color.WHITE);
        detailsBtn.setBackground(new Color(0, 123, 255));
        detailsBtn.setBorderPainted(false);
        detailsBtn.setFocusPainted(false);
        detailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailsBtn.setMaximumSize(new Dimension(200, 40));
        detailsBtn.addActionListener(e -> showFullOrderDetails(order));
        return detailsBtn;
    }

    private void showFullOrderDetails(Order order) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            "Order Details - " + order.id, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        int y = 0;
        
        // Status header
        JPanel statusHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusHeader.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(getStatusIcon(order.status) + " ");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        statusHeader.add(iconLabel);
        
        JLabel statusLabel = new JLabel(order.status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(getStatusColor(order.status));
        statusHeader.add(statusLabel);
        
        gbc.gridy = y++;
        panel.add(statusHeader, gbc);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Order ID:", order.id, gbc, y++);
        addDetailRow(panel, "Order Date:", order.orderDate, gbc, y++);
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