package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackOrderPanel extends JPanel {
    private SenderDashboard dashboard;
    
    // Tracking components
    private JTextField trackOrderField;
    private JPanel trackingResultPanel;
    private SenderOrder currentOrder;
    private JButton myOrdersBtn;
    
    // Cache for driver details
    private Map<String, DriverInfo> driverCache;
    
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
    private final Color INFO_BLUE = new Color(0, 123, 255);

    // Inner class to hold driver information
    private class DriverInfo {
        String driverId;
        String phone;
        
        DriverInfo(String driverId, String phone) {
            this.driverId = driverId;
            this.phone = phone;
        }
    }

    public TrackOrderPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        this.driverCache = new HashMap<>();
        initialize();
        loadDriverCache();
    }

    private void loadDriverCache() {
        // Load driver information from drivers.txt
        FileReader fr = null;
        BufferedReader br = null;
        try {
            java.io.File file = new java.io.File("drivers.txt");
            if (!file.exists()) {
                System.out.println("drivers.txt not found");
                return;
            }
            
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    String driverId = parts[0].trim();
                    String phone = parts[2].trim();
                    
                    driverCache.put(driverId, new DriverInfo(driverId, phone));
                }
            }
            
            System.out.println("Loaded " + driverCache.size() + " drivers into cache");
            
        } catch (IOException e) {
            System.err.println("Error loading drivers.txt: " + e.getMessage());
        } finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private DriverInfo getDriverInfo(String driverId) {
        if (driverId == null || driverId.isEmpty()) {
            return null;
        }
        
        // If not in cache, try to reload
        if (!driverCache.containsKey(driverId)) {
            loadDriverCache();
        }
        
        return driverCache.get(driverId);
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Track Your Package");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(BLUE_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        myOrdersBtn = new JButton("My Recent Orders");
        myOrdersBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        myOrdersBtn.setForeground(Color.WHITE);
        myOrdersBtn.setBackground(INFO_BLUE);
        myOrdersBtn.setBorderPainted(false);
        myOrdersBtn.setFocusPainted(false);
        myOrdersBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        myOrdersBtn.addActionListener(e -> showMyRecentOrders());
        rightPanel.add(myOrdersBtn);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CARD_BG);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        JLabel trackLabel = new JLabel("Enter Tracking Number:");
        trackLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(trackLabel);

        trackOrderField = new JTextField(25);
        trackOrderField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        trackOrderField.setPreferredSize(new Dimension(250, 40));
        trackOrderField.addActionListener(e -> trackOrder());
        searchPanel.add(trackOrderField);

        JButton trackBtn = new JButton("Track Package");
        trackBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        trackBtn.setForeground(Color.WHITE);
        trackBtn.setBackground(INFO_BLUE);
        trackBtn.setBorderPainted(false);
        trackBtn.setFocusPainted(false);
        trackBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trackBtn.addActionListener(e -> trackOrder());
        searchPanel.add(trackBtn);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Tracking result panel
        trackingResultPanel = new JPanel();
        trackingResultPanel.setLayout(new BoxLayout(trackingResultPanel, BoxLayout.Y_AXIS));
        trackingResultPanel.setBackground(CARD_BG);
        trackingResultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        showWelcomeMessage();

        JScrollPane scrollPane = new JScrollPane(trackingResultPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private void showWelcomeMessage() {
        trackingResultPanel.removeAll();
        
        JLabel welcomeLabel = new JLabel("Track Your Package");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(INFO_BLUE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(welcomeLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        JLabel instructionLabel = new JLabel("Enter your tracking number above to get real-time delivery updates");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(TEXT_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(instructionLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(30));
        
        showRecentOrdersList();
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private void showRecentOrdersList() {
        String userEmail = dashboard.getSenderEmail();
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(userEmail);
        
        if (!userOrders.isEmpty()) {
            JLabel recentLabel = new JLabel("Your Recent Orders:");
            recentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            recentLabel.setForeground(TEXT_DARK);
            recentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(recentLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(10));
            
            int startIndex = Math.max(0, userOrders.size() - 5);
            for (int i = startIndex; i < userOrders.size(); i++) {
                SenderOrder order = userOrders.get(i);
                JPanel orderCard = createOrderCard(order);
                trackingResultPanel.add(orderCard);
                trackingResultPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel emptyLabel = new JLabel("You haven't created any orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(emptyLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(15));
            
            JButton newOrderBtn = new JButton("Create Your First Order");
            newOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            newOrderBtn.setForeground(Color.WHITE);
            newOrderBtn.setBackground(SUCCESS_GREEN);
            newOrderBtn.setBorderPainted(false);
            newOrderBtn.setFocusPainted(false);
            newOrderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            newOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            newOrderBtn.setMaximumSize(new Dimension(200, 40));
            newOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));
            trackingResultPanel.add(newOrderBtn);
        }
    }

    private JPanel createOrderCard(SenderOrder order) {
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
        
        JLabel idLabel = new JLabel(order.getId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftPanel.add(idLabel);
        
        String fromCity = extractCity(order.getCustomerAddress());
        String toCity = extractCity(order.getRecipientAddress());
        JLabel routeLabel = new JLabel(fromCity + " -> " + toCity);
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        routeLabel.setForeground(TEXT_GRAY);
        leftPanel.add(routeLabel);
        
        card.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        rightPanel.setOpaque(false);
        
        // Use customer status (same as courier - "Pending" instead of "In Transit")
        JLabel statusLabel = new JLabel(getCustomerStatus(order.getStatus()));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setForeground(getCustomerStatusColor(order.getStatus()));
        rightPanel.add(statusLabel);
        
        JLabel costLabel = new JLabel(order.getFormattedEstimatedCost());
        costLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        costLabel.setForeground(SUCCESS_GREEN);
        costLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(costLabel);
        
        // Add payment method if available
        String paymentMethod = order.getPaymentMethod();
        if (paymentMethod != null && !paymentMethod.isEmpty() && !"Not Selected".equals(paymentMethod)) {
            JLabel methodLabel = new JLabel(paymentMethod.length() > 15 ? 
                paymentMethod.substring(0, 12) + "..." : paymentMethod);
            methodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            methodLabel.setForeground(INFO_BLUE);
            methodLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            rightPanel.add(methodLabel);
        }
        
        card.add(rightPanel, BorderLayout.EAST);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                trackOrderField.setText(order.getId());
                trackOrder();
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
            String[] parts = address.split(",");
            if (parts.length >= 2) {
                return parts[1].trim();
            }
            return parts[0].trim();
        }
        return address != null ? address : "N/A";
    }

    public void refreshOrders() {
        SenderDataManager.getInstance().refreshData();
        
        if (currentOrder != null) {
            String orderId = currentOrder.getId();
            SenderOrder refreshedOrder = SenderDataManager.getInstance().getOrderById(orderId);
            if (refreshedOrder != null) {
                currentOrder = refreshedOrder;
                displayTrackingResult(orderId);
            } else {
                currentOrder = null;
                showWelcomeMessage();
            }
        } else {
            showWelcomeMessage();
        }
        
        dashboard.refreshStats();
    }

    private void showMyRecentOrders() {
        trackingResultPanel.removeAll();
        
        String userEmail = dashboard.getSenderEmail();
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(userEmail);
        
        JLabel titleLabel = new JLabel("My Recent Orders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(INFO_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(titleLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        if (!userOrders.isEmpty()) {
            for (SenderOrder order : userOrders) {
                JPanel orderCard = createOrderCard(order);
                trackingResultPanel.add(orderCard);
                trackingResultPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel emptyLabel = new JLabel("You haven't created any orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            trackingResultPanel.add(emptyLabel);
            
            trackingResultPanel.add(Box.createVerticalStrut(15));
            
            JButton newOrderBtn = new JButton("Create New Order");
            newOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            newOrderBtn.setForeground(Color.WHITE);
            newOrderBtn.setBackground(SUCCESS_GREEN);
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
        JButton backBtn = new JButton("Back to Search");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setForeground(TEXT_GRAY);
        backBtn.setBackground(CARD_BG);
        backBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
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

    private void displayTrackingResult(String trackingNumber) {
        trackingResultPanel.removeAll();
        
        // Force refresh data from the main system
        SenderDataManager.getInstance().refreshData();
        currentOrder = SenderDataManager.getInstance().getOrderById(trackingNumber);
        String userEmail = dashboard.getSenderEmail();

        if (currentOrder == null) {
            showOrderNotFound(trackingNumber);
        } else {
            boolean isAuthorized = currentOrder.getCustomerEmail() != null && 
                                   userEmail != null && 
                                   currentOrder.getCustomerEmail().trim().equalsIgnoreCase(userEmail.trim());
            
            if (!isAuthorized && userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
                List<SenderOrder> demoOrders = DemoDataManager.getInstance().getDemoOrders();
                for (SenderOrder demoOrder : demoOrders) {
                    if (demoOrder.getId().equals(trackingNumber)) {
                        isAuthorized = true;
                        currentOrder = demoOrder;
                        break;
                    }
                }
            }
            
            if (isAuthorized) {
                // Debug output to verify payment details
                System.out.println("Displaying tracking for order: " + currentOrder.getId());
                System.out.println("  - Payment Method: " + currentOrder.getPaymentMethod());
                System.out.println("  - Transaction ID: " + currentOrder.getTransactionId());
                System.out.println("  - Payment Date: " + currentOrder.getPaymentDate());
                System.out.println("  - Estimated Cost: " + currentOrder.getFormattedEstimatedCost());
                System.out.println("  - Driver ID: " + currentOrder.getDriverId());
                
                displayOrderTrackingDetails(currentOrder);
            } else {
                showUnauthorizedMessage();
            }
        }
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private void showOrderNotFound(String trackingNumber) {
        JLabel notFoundLabel = new JLabel("Order not found: " + trackingNumber);
        notFoundLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        notFoundLabel.setForeground(DANGER_RED);
        notFoundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(notFoundLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(20));
        
        JLabel suggestionLabel = new JLabel("Please check your tracking number and try again.");
        suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        suggestionLabel.setForeground(TEXT_GRAY);
        suggestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(suggestionLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        addBackButton();
    }

    private void showUnauthorizedMessage() {
        JLabel notAuthorizedLabel = new JLabel("You are not authorized to view this order");
        notAuthorizedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        notAuthorizedLabel.setForeground(WARNING_YELLOW);
        notAuthorizedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trackingResultPanel.add(notAuthorizedLabel);
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        addBackButton();
    }

    // ========== CUSTOMER STATUS METHODS ==========
    
    /**
     * Get customer-friendly status (same as courier view)
     * "In Transit" from system shows as "Pending" to customer
     */
    private String getCustomerStatus(String systemStatus) {
        if ("In Transit".equals(systemStatus)) {
            return "Pending";
        }
        if ("Failed".equals(systemStatus)) {
            return "Failed";
        }
        return systemStatus;
    }
    
    /**
     * Get customer-friendly status color
     */
    private Color getCustomerStatusColor(String systemStatus) {
        String customerStatus = getCustomerStatus(systemStatus);
        switch(customerStatus) {
            case "Delivered": return SUCCESS_GREEN;
            case "Pending": return WARNING_YELLOW;
            case "Cancelled": return DANGER_RED;
            case "Delayed": return new Color(255, 87, 34);
            case "Failed": return DANGER_RED;
            default: return TEXT_GRAY;
        }
    }
    
    private void displayOrderTrackingDetails(SenderOrder order) {
        JPanel statusHeader = createStatusHeader(order);
        trackingResultPanel.add(statusHeader);
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        trackingResultPanel.add(createStatusTimeline(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        trackingResultPanel.add(createAddressPanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        trackingResultPanel.add(createPackageDetailsPanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        // Add payment details panel
        trackingResultPanel.add(createPaymentDetailsPanel(order));
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        // Add driver details panel (with driver ID and phone number ONLY)
        if (order.getDriverId() != null && !order.getDriverId().isEmpty()) {
            trackingResultPanel.add(createDriverDetailsPanel(order));
            trackingResultPanel.add(Box.createVerticalStrut(15));
        }
        
        JPanel buttonPanel = createOrderActionButtons(order);
        trackingResultPanel.add(buttonPanel);
        trackingResultPanel.add(Box.createVerticalStrut(10));
        
        addBackButton();
    }

    private JPanel createPaymentDetailsPanel(SenderOrder order) {
        JPanel paymentPanel = new JPanel(new GridLayout(3, 2, 15, 8));
        paymentPanel.setBackground(new Color(248, 249, 250));
        paymentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Payment Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        paymentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Add payment method
        addDetailRow(paymentPanel, "Payment Method:", 
            order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty() ? 
            order.getPaymentMethod() : "Not Selected");
        
        // Add payment status
        addDetailRow(paymentPanel, "Payment Status:", 
            order.getPaymentStatus() != null ? order.getPaymentStatus() : "Pending");
        
        // Add transaction ID if available
        String transactionId = order.getTransactionId();
        if (transactionId != null && !transactionId.isEmpty()) {
            paymentPanel.setLayout(new GridLayout(4, 2, 15, 8));
            addDetailRow(paymentPanel, "Transaction ID:", transactionId);
            
            // Add payment date if available
            if (order.getPaymentDate() != null && !order.getPaymentDate().isEmpty()) {
                paymentPanel.setLayout(new GridLayout(5, 2, 15, 8));
                addDetailRow(paymentPanel, "Payment Date:", order.getPaymentDate());
            }
        } else {
            // If no transaction ID, show placeholder
            addDetailRow(paymentPanel, "Transaction ID:", "Processing...");
        }

        return paymentPanel;
    }

    private JPanel createStatusHeader(SenderOrder order) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        
        JLabel orderIdLabel = new JLabel("Order #" + order.getId());
        orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        orderIdLabel.setForeground(TEXT_DARK);
        infoPanel.add(orderIdLabel);
        
        // Use customer status
        String customerStatus = getCustomerStatus(order.getStatus());
        JLabel statusLabel = new JLabel(customerStatus);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(getCustomerStatusColor(order.getStatus()));
        infoPanel.add(statusLabel);
        
        leftPanel.add(infoPanel);
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        rightPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Ordered: " + formatDate(order.getOrderDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_GRAY);
        rightPanel.add(dateLabel);
        
        JLabel costLabel = new JLabel(order.getFormattedEstimatedCost());
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        costLabel.setForeground(SUCCESS_GREEN);
        rightPanel.add(costLabel);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createStatusTimeline(SenderOrder order) {
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(CARD_BG);
        timelinePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Order Status Timeline",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        timelinePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        timelinePanel.add(createTimelineEvent("Order Placed", 
            "Your order has been received and confirmed", 
            order.getOrderDate(), 
            true));
        
        timelinePanel.add(createTimelineEvent("Processing", 
            "Your order is being prepared for shipping", 
            getProcessingTime(order.getOrderDate()), 
            !"Pending".equals(order.getStatus())));
        
        String customerStatus = getCustomerStatus(order.getStatus());
        
        if ("Pending".equals(customerStatus) && !"Delivered".equals(order.getStatus()) && !"Failed".equals(order.getStatus())) {
            timelinePanel.add(createTimelineEvent("Awaiting Pickup", 
                "Your order is waiting for courier pickup", 
                "Pending", 
                false));
        } else if ("Picked Up".equals(order.getStatus())) {
            timelinePanel.add(createTimelineEvent("Picked Up", 
                "Your package has been picked up by the courier", 
                "Picked up", 
                true));
        } else if ("Delayed".equals(order.getStatus())) {
            String reason = extractReason(order.getNotes());
            timelinePanel.add(createTimelineEvent("Delayed", 
                "Your shipment is experiencing a delay - " + (reason != null ? reason : "Please check back for updates"), 
                order.getEstimatedDelivery() != null ? "Est: " + order.getEstimatedDelivery() : "Check back later", 
                false));
        }
        
        if ("Delivered".equals(order.getStatus())) {
            timelinePanel.add(createTimelineEvent("Delivered", 
                "Your package has been successfully delivered", 
                order.getEstimatedDelivery() != null ? order.getEstimatedDelivery() : "Completed", 
                true));
        }
        
        if ("Failed".equals(order.getStatus())) {
            timelinePanel.add(createTimelineEvent("Failed", 
                "Delivery was unsuccessful. Please contact support.", 
                order.getEstimatedDelivery() != null ? order.getEstimatedDelivery() : "Failed", 
                false));
        }
        
        if ("Cancelled".equals(order.getStatus())) {
            timelinePanel.add(createTimelineEvent("Cancelled", 
                "This order has been cancelled", 
                order.getOrderDate(), 
                false));
        }

        return timelinePanel;
    }

    private String getProcessingTime(String orderDate) {
        if (orderDate != null && !orderDate.isEmpty()) {
            return "1-2 business days after order placement";
        }
        return "Processing in progress";
    }

    private JPanel createTimelineEvent(String status, String location, String time, boolean completed) {
        JPanel eventPanel = new JPanel(new BorderLayout());
        eventPanel.setOpaque(false);
        eventPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setOpaque(false);

        String statusText = completed ? "- " + status : "  " + status;
        
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Segoe UI", completed ? Font.BOLD : Font.PLAIN, 13));
        statusLabel.setForeground(completed ? SUCCESS_GREEN : TEXT_GRAY);
        leftPanel.add(statusLabel);

        JLabel locationLabel = new JLabel(location);
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locationLabel.setForeground(TEXT_GRAY);
        leftPanel.add(locationLabel);

        eventPanel.add(leftPanel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_GRAY);
        eventPanel.add(timeLabel, BorderLayout.EAST);

        return eventPanel;
    }

    private JPanel createAddressPanel(SenderOrder order) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel fromPanel = new JPanel(new BorderLayout(5, 5));
        fromPanel.setOpaque(false);
        
        JLabel fromLabel = new JLabel("FROM:");
        fromLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fromLabel.setForeground(TEXT_GRAY);
        fromPanel.add(fromLabel, BorderLayout.NORTH);
        
        JLabel fromAddress = new JLabel("<html><div style='width: 220px;'>" + 
            order.getCustomerAddress() + "</div></html>");
        fromAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fromPanel.add(fromAddress, BorderLayout.CENTER);
        panel.add(fromPanel);

        JPanel toPanel = new JPanel(new BorderLayout(5, 5));
        toPanel.setOpaque(false);
        
        JLabel toLabel = new JLabel("TO:");
        toLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toLabel.setForeground(TEXT_GRAY);
        toPanel.add(toLabel, BorderLayout.NORTH);
        
        JLabel toAddress = new JLabel("<html><div style='width: 220px;'>" + 
            order.getRecipientAddress() + "</div></html>");
        toAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toPanel.add(toAddress, BorderLayout.CENTER);
        panel.add(toPanel);

        return panel;
    }

    private JPanel createPackageDetailsPanel(SenderOrder order) {
        JPanel packagePanel = new JPanel(new GridLayout(4, 2, 15, 8));
        packagePanel.setBackground(new Color(248, 249, 250));
        packagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Package Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        packagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        addDetailRow(packagePanel, "Weight:", String.format("%.2f kg", order.getWeight()));
        addDetailRow(packagePanel, "Dimensions:", order.getDimensions() + " cm");
        addDetailRow(packagePanel, "Package Type:", order.getPackageType());
        addDetailRow(packagePanel, "Cost:", order.getFormattedEstimatedCost());

        String description = order.getDescription();
        if (!description.isEmpty()) {
            packagePanel.setLayout(new GridLayout(5, 2, 15, 8));
            addDetailRow(packagePanel, "Description:", description);
        }

        return packagePanel;
    }

    /**
     * Create driver details panel - shows ONLY Driver ID and Phone Number
     */
    private JPanel createDriverDetailsPanel(SenderOrder order) {
        JPanel driverPanel = new JPanel(new GridBagLayout());
        driverPanel.setBackground(new Color(248, 249, 250));
        driverPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Driver Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        driverPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1.0;
        
        String driverId = order.getDriverId();
        DriverInfo driverInfo = getDriverInfo(driverId);
        
        int row = 0;
        
        // Driver ID
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel idLabel = new JLabel("Driver ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        driverPanel.add(idLabel, gbc);
        
        gbc.gridx = 1;
        String driverIdText = (driverInfo != null && driverInfo.driverId != null) ? driverInfo.driverId : (driverId != null ? driverId : "Not Assigned");
        JLabel idValue = new JLabel(driverIdText);
        idValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idValue.setForeground(INFO_BLUE);
        driverPanel.add(idValue, gbc);
        row++;
        
        // Driver Phone Number
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel phoneLabel = new JLabel("Driver Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        driverPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        String driverPhone = (driverInfo != null && driverInfo.phone != null) ? driverInfo.phone : "Not Available";
        JLabel phoneValue = new JLabel(driverPhone);
        phoneValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        driverPanel.add(phoneValue, gbc);
        
        return driverPanel;
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(labelComp);

        JLabel valueComp = new JLabel(value != null ? value : "-");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(valueComp);
    }

    private JPanel createOrderActionButtons(SenderOrder order) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        
        JButton detailsBtn = new JButton("View Full Details");
        detailsBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        detailsBtn.setForeground(Color.WHITE);
        detailsBtn.setBackground(INFO_BLUE);
        detailsBtn.setBorderPainted(false);
        detailsBtn.setFocusPainted(false);
        detailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsBtn.addActionListener(e -> showFullOrderDetails(order));
        
        buttonPanel.add(detailsBtn);
        
        // NEW: Delete Button (Red)
        JButton deleteBtn = new JButton("Delete Order");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(DANGER_RED);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> deleteOrder(order));
        
        buttonPanel.add(deleteBtn);
        
        return buttonPanel;
    }
    
    /**
     * Delete order from the system
     */
    private void deleteOrder(SenderOrder order) {
        String userEmail = dashboard.getSenderEmail();
        
        // Check if it's a demo account
        if (DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
            JOptionPane.showMessageDialog(this, 
                "Demo users cannot delete orders. Please create a real account to delete orders.", 
                "Demo Account Restriction", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if order is delivered or cancelled - can't delete delivered orders
        if ("Delivered".equals(order.getStatus())) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete a delivered order. Only pending or cancelled orders can be deleted.", 
                "Cannot Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to permanently delete order " + order.getId() + "?\n\n" +
            "This action cannot be undone!", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = SenderDataManager.getInstance().deleteOrder(order.getId());
            
            if (deleted) {
                JOptionPane.showMessageDialog(this, 
                    "Order " + order.getId() + " has been successfully deleted.", 
                    "Order Deleted", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear current order
                currentOrder = null;
                
                // Refresh data
                SenderDataManager.getInstance().refreshData();
                dashboard.refreshStats();
                
                // Go back to welcome screen
                showWelcomeMessage();
                
                // Clear the tracking field
                trackOrderField.setText("");
                
                // Refresh the dashboard to update counts
                dashboard.refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete order. Please try again.", 
                    "Delete Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showFullOrderDetails(SenderOrder order) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            "Order Details - " + order.getId(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(550, 750);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        int y = 0;
        
        JPanel statusHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusHeader.setBackground(CARD_BG);
        
        String customerStatus = getCustomerStatus(order.getStatus());
        JLabel statusLabel = new JLabel(customerStatus);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(getCustomerStatusColor(order.getStatus()));
        statusHeader.add(statusLabel);
        
        gbc.gridy = y++;
        panel.add(statusHeader, gbc);
        
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        addDetailRow(panel, "Order ID:", order.getId(), gbc, y++);
        addDetailRow(panel, "Order Date:", order.getOrderDate(), gbc, y++);
        addDetailRow(panel, "Status:", customerStatus, gbc, y++);
        
        // Payment section header
        gbc.gridy = y++;
        panel.add(new JSeparator(), gbc);
        
        JLabel paymentHeader = new JLabel("Payment Details");
        paymentHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        paymentHeader.setForeground(SUCCESS_GREEN);
        gbc.gridy = y++;
        panel.add(paymentHeader, gbc);
        
        addDetailRow(panel, "Payment Method:", order.getPaymentMethod() != null ? order.getPaymentMethod() : "Not Selected", gbc, y++);
        addDetailRow(panel, "Payment Status:", order.getPaymentStatus() != null ? order.getPaymentStatus() : "Pending", gbc, y++);
        addDetailRow(panel, "Amount Paid:", order.getFormattedEstimatedCost(), gbc, y++);
        
        if (order.getTransactionId() != null && !order.getTransactionId().isEmpty()) {
            addDetailRow(panel, "Transaction ID:", order.getTransactionId(), gbc, y++);
        }
        
        if (order.getPaymentDate() != null && !order.getPaymentDate().isEmpty()) {
            addDetailRow(panel, "Payment Date:", order.getPaymentDate(), gbc, y++);
        }
        
        // Driver information section (ONLY driver ID and phone number)
        if (order.getDriverId() != null && !order.getDriverId().isEmpty()) {
            gbc.gridy = y++;
            panel.add(new JSeparator(), gbc);
            
            JLabel driverHeader = new JLabel("Driver Information");
            driverHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
            driverHeader.setForeground(INFO_BLUE);
            gbc.gridy = y++;
            panel.add(driverHeader, gbc);
            
            addDetailRow(panel, "Driver ID:", order.getDriverId(), gbc, y++);
            
            // Get and display driver phone number
            DriverInfo driverInfo = getDriverInfo(order.getDriverId());
            if (driverInfo != null && driverInfo.phone != null) {
                addDetailRow(panel, "Driver Phone:", driverInfo.phone, gbc, y++);
            } else {
                addDetailRow(panel, "Driver Phone:", "Not Available", gbc, y++);
            }
        }
        
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
        
        String description = order.getDescription();
        if (!description.isEmpty()) {
            addDetailRow(panel, "Description:", description, gbc, y++);
        }
        
        if (order.getEstimatedDelivery() != null) {
            gbc.gridy = y++;
            panel.add(new JSeparator(), gbc);
            addDetailRow(panel, "Est. Delivery:", order.getEstimatedDelivery(), gbc, y++);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(INFO_BLUE);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        // Add delete button to dialog as well
        if (!"Delivered".equals(order.getStatus())) {
            JButton deleteBtnDialog = new JButton("Delete Order");
            deleteBtnDialog.setFont(new Font("Segoe UI", Font.BOLD, 13));
            deleteBtnDialog.setForeground(Color.WHITE);
            deleteBtnDialog.setBackground(DANGER_RED);
            deleteBtnDialog.setBorderPainted(false);
            deleteBtnDialog.setFocusPainted(false);
            deleteBtnDialog.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtnDialog.addActionListener(e -> {
                dialog.dispose();
                deleteOrder(order);
            });
            buttonPanel.add(deleteBtnDialog);
        }

        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

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

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) return "-";
        if (date.contains(" ")) {
            return date.substring(0, 10);
        }
        return date;
    }

    private String extractReason(String notes) {
        if (notes != null && !notes.isEmpty()) {
            // Try with semicolon separator (new format)
            if (notes.contains("Reason:")) {
                String[] parts = notes.split(";");
                for (String part : parts) {
                    String trimmedPart = part.trim();
                    if (trimmedPart.startsWith("Reason:")) {
                        return trimmedPart.substring("Reason:".length()).trim();
                    }
                }
            }
            
            // Fallback for old pipe-delimited format
            if (notes.contains("Reason:")) {
                String[] parts = notes.split("Reason:");
                if (parts.length > 1) {
                    String reason = parts[1].trim();
                    int pipeIndex = reason.indexOf("|");
                    if (pipeIndex > 0) {
                        reason = reason.substring(0, pipeIndex);
                    }
                    int semiIndex = reason.indexOf(";");
                    if (semiIndex > 0) {
                        reason = reason.substring(0, semiIndex);
                    }
                    return reason;
                }
            }
        }
        return null;
    }
}