// ReceiverTracker.java (Fixed - No conflicts)
package receiver;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class ReceiverTracker extends JFrame {
    
    // Modern color scheme
    private final Color PRIMARY_BLUE = new Color(0, 123, 255);
    private final Color PRIMARY_DARK = new Color(0, 86, 179);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color WARNING_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(220, 53, 69);
    private final Color INFO_BLUE = new Color(23, 162, 184);
    private final Color PURPLE = new Color(111, 66, 193);
    private final Color ORANGE = new Color(253, 126, 20);
    private final Color BG_LIGHT = new Color(248, 249, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color TEXT_MUTED = new Color(134, 142, 150);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Color TIMELINE_COMPLETED = SUCCESS_GREEN;
    private final Color TIMELINE_CURRENT = PRIMARY_BLUE;
    private final Color TIMELINE_PENDING = new Color(206, 212, 218);
    
    // Components
    private JTextField trackingField;
    private JPanel resultPanel;
    private JScrollPane resultScrollPane;
    private CardLayout cardLayout;
    private JPanel mainCardPanel;
    private JLabel timeLabel;
    
    // Timer for auto-refresh (using javax.swing.Timer explicitly)
    private javax.swing.Timer refreshTimer;
    private String lastTrackedOrderId = null;
    
    public ReceiverTracker() {
        setTitle("LogiXpress - Track Your Package");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initUI();
        startAutoRefresh();
    }
    
    private void initUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }
    
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PRIMARY_BLUE);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
        
        // Left side - Logo
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);
        
        // Load logo from logo.rec.png (Receiver logo)
        ImageIcon logoIcon = loadLogo("logo.rec.png");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        } else {
            // Fallback text if logo not found
            JLabel logoLabel = new JLabel("LX");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
            logoLabel.setForeground(Color.WHITE);
            leftPanel.add(logoLabel);
        }
        
        JLabel title = new JLabel("LogiXpress Tracking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);
        
        JLabel badge = new JLabel("RECEIVER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(PRIMARY_DARK);
        badge.setBackground(new Color(255, 255, 255, 200));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        leftPanel.add(badge);
        
        topBar.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Time and info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setOpaque(false);
        
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(new Date()))
        );
        clockTimer.start();
        rightPanel.add(timeLabel);
        
        topBar.add(rightPanel, BorderLayout.EAST);
        
        return topBar;
    }
    
    // Add this method to load the logo
    private ImageIcon loadLogo(String filename) {
        try {
            // Try loading from classpath
            java.net.URL imgURL = getClass().getResource(filename);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            }
            // Try loading from file system
            java.io.File file = new java.io.File(filename);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
            // Try loading from resources folder
            file = new java.io.File("resources/" + filename);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
            // Try loading from parent directory
            file = new java.io.File("../" + filename);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        return null;
    }
    
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        // Search Section
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Results Section with CardLayout
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BG_LIGHT);
        
        // Welcome panel
        mainCardPanel.add(createWelcomePanel(), "WELCOME");
        
        // Results panel
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(BG_LIGHT);
        
        resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setBorder(null);
        resultScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        resultScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultScrollPane.getViewport().setBackground(BG_LIGHT);
        
        mainCardPanel.add(resultScrollPane, "RESULTS");
        
        mainPanel.add(mainCardPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(16, BORDER_COLOR),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel titleLabel = new JLabel("Track Your Package");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_BLUE);
        panel.add(titleLabel, gbc);
        
        // Subtitle
        gbc.gridy = 1;
        JLabel subtitleLabel = new JLabel("Enter your tracking number to get real-time delivery updates");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_GRAY);
        panel.add(subtitleLabel, gbc);
        
        // Tracking input
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        
        JLabel trackingLabel = new JLabel("Tracking Number:");
        trackingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(trackingLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        trackingField = new JTextField(20);
        trackingField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        trackingField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        trackingField.addActionListener(e -> trackOrder());
        panel.add(trackingField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        JButton trackBtn = createStyledButton("Track Order", PRIMARY_BLUE, Color.WHITE);
        trackBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        trackBtn.setPreferredSize(new Dimension(140, 45));
        trackBtn.addActionListener(e -> trackOrder());
        panel.add(trackBtn, gbc);
        
        // Example hint
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 10, 5, 10);
        JLabel hintLabel = new JLabel("Example: 20240301001 (Format: YYYYMMDD + 3-digit sequence)");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(TEXT_MUTED);
        panel.add(hintLabel, gbc);
        
        return panel;
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_LIGHT);
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(16, BORDER_COLOR),
            BorderFactory.createEmptyBorder(50, 60, 50, 60)
        ));
        
        
        
        card.add(Box.createVerticalStrut(20));
        
        JLabel titleLabel = new JLabel("Enter Your Tracking Number");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        card.add(Box.createVerticalStrut(10));
        
        JLabel descLabel = new JLabel("Enter the tracking number provided by the sender to track your package");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(descLabel);
        
        card.add(Box.createVerticalStrut(15));
        
        JLabel exampleLabel = new JLabel("Tracking number format: YYYYMMDD + 3 digits (e.g., 20240301001)");
        exampleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        exampleLabel.setForeground(TEXT_MUTED);
        exampleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(exampleLabel);
        
        panel.add(card);
        
        return panel;
    }
    
    private void trackOrder() {
        String trackingNumber = trackingField.getText().trim();
        
        if (trackingNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a tracking number", 
                "Input Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Store for auto-refresh
        lastTrackedOrderId = trackingNumber;
        
        // Show loading
        showLoading();
        
        // Search for order
        SwingWorker<ReceiverOrderData, Void> worker = new SwingWorker<ReceiverOrderData, Void>() {
            @Override
            protected ReceiverOrderData doInBackground() throws Exception {
                Thread.sleep(500); // Simulate loading
                return findOrderByTrackingNumber(trackingNumber);
            }
            
            @Override
            protected void done() {
                try {
                    ReceiverOrderData order = get();
                    if (order != null) {
                        displayTrackingResult(order);
                    } else {
                        showOrderNotFound(trackingNumber);
                    }
                } catch (Exception e) {
                    showError("Error searching for order: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private ReceiverOrderData findOrderByTrackingNumber(String trackingNumber) {
        File file = new File("orders.txt");
        if (!file.exists()) {
            return null;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("\\|", -1);
                if (parts.length > 0 && parts[0].equals(trackingNumber)) {
                    return parseOrderData(parts);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private ReceiverOrderData parseOrderData(String[] parts) {
        ReceiverOrderData order = new ReceiverOrderData();
        
        // Basic info
        order.orderId = safeString(parts, 0);
        order.customerAddress = safeString(parts, 4);
        order.recipientName = safeString(parts, 5);
        order.recipientPhone = safeString(parts, 6);
        order.recipientAddress = safeString(parts, 7);
        order.status = safeString(parts, 8);
        order.orderDate = safeString(parts, 9);
        order.estimatedDelivery = safeString(parts, 10);
        order.driverId = safeString(parts, 12);
        order.vehicleId = safeString(parts, 13);
        order.notes = safeString(parts, 16);
        order.reason = safeString(parts, 17);
        
        // Extract cost from notes
        order.estimatedCost = extractCostFromNotes(order.notes);
        
        return order;
    }
    
    private double extractCostFromNotes(String notes) {
        if (notes == null) return 0;
        
        if (notes.contains("Total Amount: RM")) {
            try {
                int start = notes.indexOf("Total Amount: RM") + 15;
                int end = notes.indexOf(";", start);
                if (end == -1) end = notes.length();
                String costStr = notes.substring(start, end).trim();
                costStr = costStr.replaceAll("[^0-9.]", "");
                if (!costStr.isEmpty()) {
                    return Double.parseDouble(costStr);
                }
            } catch (Exception e) {}
        }
        return 0;
    }
    
    private String safeString(String[] parts, int index) {
        return (parts.length > index && parts[index] != null) ? parts[index] : "";
    }
    
    private void showLoading() {
        resultPanel.removeAll();
        
        JPanel loadingPanel = new JPanel(new GridBagLayout());
        loadingPanel.setBackground(CARD_BG);
        loadingPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 20));
        
        JLabel loadingLabel = new JLabel("Searching for your package...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setForeground(TEXT_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        loadingPanel.add(progressBar, gbc);
        gbc.gridy = 1;
        loadingPanel.add(loadingLabel, gbc);
        
        resultPanel.add(loadingPanel);
        resultPanel.revalidate();
        resultPanel.repaint();
        cardLayout.show(mainCardPanel, "RESULTS");
    }
    
    private void showOrderNotFound(String trackingNumber) {
        resultPanel.removeAll();
        
        JPanel notFoundPanel = new JPanel(new GridBagLayout());
        notFoundPanel.setBackground(CARD_BG);
        notFoundPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        JLabel iconLabel = new JLabel("🔍");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        
        JLabel titleLabel = new JLabel("Order Not Found");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(DANGER_RED);
        
        JLabel messageLabel = new JLabel("We couldn't find an order with tracking number: " + trackingNumber);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_GRAY);
        
        JLabel hintLabel = new JLabel("Please check the tracking number and try again");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(TEXT_MUTED);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridy = 0;
        notFoundPanel.add(iconLabel, gbc);
        gbc.gridy = 1;
        notFoundPanel.add(titleLabel, gbc);
        gbc.gridy = 2;
        notFoundPanel.add(messageLabel, gbc);
        gbc.gridy = 3;
        notFoundPanel.add(hintLabel, gbc);
        
        resultPanel.add(notFoundPanel);
        resultPanel.revalidate();
        resultPanel.repaint();
        cardLayout.show(mainCardPanel, "RESULTS");
    }
    
    private void showError(String message) {
        resultPanel.removeAll();
        
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setBackground(CARD_BG);
        errorPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        JLabel iconLabel = new JLabel("❌");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        
        JLabel titleLabel = new JLabel("Error");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(DANGER_RED);
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridy = 0;
        errorPanel.add(iconLabel, gbc);
        gbc.gridy = 1;
        errorPanel.add(titleLabel, gbc);
        gbc.gridy = 2;
        errorPanel.add(messageLabel, gbc);
        
        resultPanel.add(errorPanel);
        resultPanel.revalidate();
        resultPanel.repaint();
        cardLayout.show(mainCardPanel, "RESULTS");
    }
    
    private void displayTrackingResult(ReceiverOrderData order) {
        resultPanel.removeAll();
        
        // Create centered wrapper
        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(BG_LIGHT);
        centerWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Status Header
        JPanel statusHeader = createStatusHeader(order);
        statusHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(statusHeader);
        centerWrapper.add(Box.createVerticalStrut(20));
        
        // Visual Timeline
        JPanel timelinePanel = createTimelinePanel(order);
        timelinePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(timelinePanel);
        centerWrapper.add(Box.createVerticalStrut(20));
        
        // Delivery Information
        JPanel deliveryInfo = createDeliveryInfoPanel(order);
        deliveryInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(deliveryInfo);
        centerWrapper.add(Box.createVerticalStrut(15));
        
        // Address Information
        JPanel addressPanel = createAddressPanel(order);
        addressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(addressPanel);
        
        resultPanel.add(centerWrapper);
        resultPanel.revalidate();
        resultPanel.repaint();
        cardLayout.show(mainCardPanel, "RESULTS");
        
        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            if (resultScrollPane != null && resultScrollPane.getVerticalScrollBar() != null) {
                resultScrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }
    
    private JPanel createStatusHeader(ReceiverOrderData order) {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(new Color(240, 248, 255));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, PRIMARY_BLUE),
            BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));
        headerPanel.setMaximumSize(new Dimension(700, 110));
        headerPanel.setPreferredSize(new Dimension(650, 110));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        infoPanel.setOpaque(false);
        
        JLabel orderIdLabel = new JLabel("Order: " + order.orderId);
        orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        orderIdLabel.setForeground(TEXT_DARK);
        infoPanel.add(orderIdLabel);
        
        JLabel statusLabel = new JLabel(getStatusDisplay(order.status));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(getStatusColor(order.status));
        infoPanel.add(statusLabel);
        
        leftPanel.add(infoPanel);
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        rightPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Ordered: " + formatDate(order.orderDate));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_GRAY);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(dateLabel);
        
        String costDisplay = order.estimatedCost > 0 ? String.format("RM %.2f", order.estimatedCost) : "RM --.--";
        JLabel costLabel = new JLabel(costDisplay);
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        costLabel.setForeground(SUCCESS_GREEN);
        costLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(costLabel);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createTimelinePanel(ReceiverOrderData order) {
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(CARD_BG);
        timelinePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Delivery Status Timeline",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_BLUE
        ));
        timelinePanel.setMaximumSize(new Dimension(700, 420));
        timelinePanel.setPreferredSize(new Dimension(650, 400));
        
        String[] stepTitles = {"Order Placed", "Processing", "Picked Up", "In Transit", "Out for Delivery", "Delivered"};
        String[] stepDescriptions = {
            "Your order has been received and confirmed",
            "Your order is being prepared for shipping",
            "Your package has been picked up by the courier",
            "Your package is on its way to the destination",
            "Your package is out for delivery today",
            "Your package has been successfully delivered"
        };
        
        int currentStep = getStepIndex(order.status);
        
        for (int i = 0; i < stepTitles.length; i++) {
            boolean isCompleted = i < currentStep;
            boolean isCurrent = i == currentStep;
            String timestamp = getTimestampForStep(order, i);
            
            timelinePanel.add(createTimelineStep(
                stepTitles[i],
                stepDescriptions[i],
                timestamp,
                isCompleted,
                isCurrent,
                i,
                stepTitles.length - 1
            ));
        }
        
        if ("Delayed".equals(order.status)) {
            timelinePanel.add(createTimelineStep(
                "Delayed",
                "Delivery is experiencing delays: " + (order.reason != null && !order.reason.isEmpty() ? order.reason : "Please contact support"),
                getCurrentDateTime(),
                false,
                true,
                5,
                5
            ));
        } else if ("Failed".equals(order.status)) {
            timelinePanel.add(createTimelineStep(
                "Delivery Failed",
                "Delivery was unsuccessful: " + (order.reason != null && !order.reason.isEmpty() ? order.reason : "Please contact support"),
                getCurrentDateTime(),
                false,
                true,
                5,
                5
            ));
        } else if ("Cancelled".equals(order.status)) {
            timelinePanel.add(createTimelineStep(
                "Cancelled",
                "This order has been cancelled",
                getCurrentDateTime(),
                false,
                true,
                5,
                5
            ));
        }
        
        return timelinePanel;
    }
    
    private JPanel createTimelineStep(String title, String description, String timestamp,
                                       boolean completed, boolean current, int step, int totalSteps) {
        JPanel stepPanel = new JPanel(new BorderLayout(15, 5));
        stepPanel.setOpaque(false);
        stepPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        stepPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        stepPanel.setPreferredSize(new Dimension(650, 65));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(50, 50));
        
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(false);
        
        String icon;
        Color iconColor;
        if (completed) {
            icon = "●";
            iconColor = TIMELINE_COMPLETED;
        } else if (current) {
            icon = "●";
            iconColor = TIMELINE_CURRENT;
        } else {
            icon = "○";
            iconColor = TIMELINE_PENDING;
        }
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconLabel.setForeground(iconColor);
        iconPanel.add(iconLabel);
        leftPanel.add(iconPanel, BorderLayout.NORTH);
        
        if (step < totalSteps) {
            JLabel connectorLabel = new JLabel("|");
            connectorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            connectorLabel.setForeground(completed ? TIMELINE_COMPLETED : TIMELINE_PENDING);
            connectorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            leftPanel.add(connectorLabel, BorderLayout.CENTER);
        }
        
        stepPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        centerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", (completed || current) ? Font.BOLD : Font.PLAIN, 14));
        titleLabel.setForeground(completed ? TIMELINE_COMPLETED : (current ? TIMELINE_CURRENT : TEXT_GRAY));
        centerPanel.add(titleLabel);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_GRAY);
        centerPanel.add(descLabel);
        
        stepPanel.add(centerPanel, BorderLayout.CENTER);
        
        JLabel timeLabel = new JLabel(timestamp != null && !timestamp.isEmpty() && !"Pending".equals(timestamp) ? timestamp : "-");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(completed ? TIMELINE_COMPLETED : (current ? TIMELINE_CURRENT : TEXT_GRAY));
        stepPanel.add(timeLabel, BorderLayout.EAST);
        
        return stepPanel;
    }
    
    private JPanel createDeliveryInfoPanel(ReceiverOrderData order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Delivery Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            INFO_BLUE
        ));
        panel.setMaximumSize(new Dimension(700, 160));
        panel.setPreferredSize(new Dimension(650, 150));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 15, 6, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        addDetailRow(panel, "Recipient:", order.recipientName, gbc, row++);
        addDetailRow(panel, "Contact:", order.recipientPhone, gbc, row++);
        
        if (order.estimatedDelivery != null && !order.estimatedDelivery.isEmpty()) {
            addDetailRow(panel, "Estimated Delivery:", order.estimatedDelivery, gbc, row++);
        }
        
        if (order.driverId != null && !order.driverId.isEmpty()) {
            addDetailRow(panel, "Courier ID:", order.driverId, gbc, row++);
        }
        
        if (order.vehicleId != null && !order.vehicleId.isEmpty()) {
            addDetailRow(panel, "Vehicle ID:", order.vehicleId, gbc, row++);
        }
        
        return panel;
    }
    
    private JPanel createAddressPanel(ReceiverOrderData order) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Address Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            SUCCESS_GREEN
        ));
        panel.setMaximumSize(new Dimension(700, 160));
        panel.setPreferredSize(new Dimension(650, 150));
        
        JPanel fromPanel = new JPanel(new BorderLayout(8, 8));
        fromPanel.setOpaque(false);
        fromPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel fromLabel = new JLabel("SENDER");
        fromLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fromLabel.setForeground(PRIMARY_BLUE);
        fromPanel.add(fromLabel, BorderLayout.NORTH);
        
        JLabel fromAddress = new JLabel("<html><div style='width: 100%; font-family: Segoe UI; font-size: 11px;'>" + 
            order.customerAddress + "</div></html>");
        fromAddress.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fromPanel.add(fromAddress, BorderLayout.CENTER);
        panel.add(fromPanel);
        
        JPanel toPanel = new JPanel(new BorderLayout(8, 8));
        toPanel.setOpaque(false);
        toPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel toLabel = new JLabel("RECIPIENT");
        toLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toLabel.setForeground(SUCCESS_GREEN);
        toPanel.add(toLabel, BorderLayout.NORTH);
        
        JLabel toAddress = new JLabel("<html><div style='width: 100%; font-family: Segoe UI; font-size: 11px;'>" + 
            order.recipientAddress + "</div></html>");
        toAddress.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toPanel.add(toAddress, BorderLayout.CENTER);
        panel.add(toPanel);
        
        return panel;
    }
    
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 11));
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value != null ? value : "-");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(valueComp, gbc);
    }
    
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(248, 249, 250));
        bar.setPreferredSize(new Dimension(getWidth(), 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        JLabel status = new JLabel("  System Status: Connected | Real-time tracking available");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_GRAY);
        bar.add(status, BorderLayout.WEST);
        
        JLabel rights = new JLabel("LogiXpress Tracking System v1.0  ");
        rights.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rights.setForeground(TEXT_MUTED);
        bar.add(rights, BorderLayout.EAST);
        
        return bar;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private int getStepIndex(String status) {
        switch(status) {
            case "Delivered": return 5;
            case "Out for Delivery": return 4;
            case "In Transit": return 3;
            case "Picked Up": return 2;
            case "Processing": return 1;
            case "Assigned": return 1;
            case "Pending": return 0;
            default: return 0;
        }
    }
    
    private String getStatusDisplay(String status) {
        switch(status) {
            case "Pending": return "Order Pending";
            case "Assigned": return "Assigned to Courier";
            case "Picked Up": return "Picked Up";
            case "In Transit": return "In Transit";
            case "Out for Delivery": return "Out for Delivery";
            case "Delivered": return "Delivered";
            case "Delayed": return "Delayed";
            case "Failed": return "Delivery Failed";
            case "Cancelled": return "✗ Cancelled";
            default: return status;
        }
    }
    
    private Color getStatusColor(String status) {
        switch(status) {
            case "Delivered": return SUCCESS_GREEN;
            case "In Transit": return PRIMARY_BLUE;
            case "Out for Delivery": return ORANGE;
            case "Picked Up": return PURPLE;
            case "Assigned": return INFO_BLUE;
            case "Pending": return WARNING_YELLOW;
            case "Cancelled": return DANGER_RED;
            case "Delayed": return DANGER_RED;
            case "Failed": return DANGER_RED;
            default: return TEXT_GRAY;
        }
    }
    
    private String getTimestampForStep(ReceiverOrderData order, int step) {
        // This would ideally come from actual timestamps in the order data
        // For now, use order date as base and add days
        if (step == 0) return order.orderDate;
        
        // For demo purposes, generate approximate dates
        if ("Delivered".equals(order.status) && step <= 5) {
            // Use order date + days
            return getApproximateDate(order.orderDate, step);
        }
        
        if (step <= getStepIndex(order.status)) {
            return getApproximateDate(order.orderDate, step);
        }
        
        return "Pending";
    }
    
    private String getApproximateDate(String baseDate, int daysToAdd) {
        if (baseDate == null || baseDate.isEmpty()) return "Pending";
        try {
            String dateStr = baseDate.split(" ")[0];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return "Pending";
        }
    }
    
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date());
    }
    
    private String formatDate(String date) {
        if (date == null || date.isEmpty()) return "-";
        if (date.contains(" ")) {
            return date.substring(0, 10);
        }
        return date;
    }
    
    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(30000, e -> {
            // Auto-refresh currently displayed order if any
            if (lastTrackedOrderId != null && !lastTrackedOrderId.isEmpty()) {
                SwingUtilities.invokeLater(() -> trackOrder());
            }
        });
        refreshTimer.start();
    }
    
    private static class ReceiverOrderData {
        String orderId;
        String customerAddress;
        String recipientName;
        String recipientPhone;
        String recipientAddress;
        String status;
        String orderDate;
        String estimatedDelivery;
        String driverId;
        String vehicleId;
        String notes;
        String reason;
        double estimatedCost;
    }
    
    // Custom rounded border class
    class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new ReceiverTracker().setVisible(true);
        });
    }
}