package sender;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class TrackOrderPanel extends JPanel {
    private SenderDashboard dashboard;
    
    // Tracking components
    private JPanel trackingResultPanel;
    private JButton myOrdersBtn;
    private JPanel searchPanel;
    
    // Cache for driver details
    private Map<String, DriverInfo> driverCache;

    
    // Modern color scheme - Professional Logistics Theme
    private final Color PRIMARY_BLUE = new Color(0, 86, 179);
    private final Color PRIMARY_LIGHT = new Color(0, 123, 255);
    private final Color BG_LIGHT = new Color(248, 249, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color TEXT_MUTED = new Color(134, 142, 150);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color WARNING_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(220, 53, 69);
    private final Color INFO_BLUE = new Color(23, 162, 184);
    private final Color ORANGE = new Color(253, 126, 20);
    private final Color PURPLE = new Color(111, 66, 193);
    
    // Timeline colors
    private final Color TIMELINE_COMPLETED = SUCCESS_GREEN;
    private final Color TIMELINE_CURRENT = PRIMARY_LIGHT;
    private final Color TIMELINE_PENDING = new Color(206, 212, 218);

    private class DriverInfo {
        String driverId;
        String name;
        String phone;
        
        DriverInfo(String driverId, String name, String phone) {
            this.driverId = driverId;
            this.name = name;
            this.phone = phone;
        }
    }

    public TrackOrderPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        this.driverCache = new HashMap<>();
        initialize();
        loadDriverCache();
        startAutoRefresh();
    }

    private void loadDriverCache() {
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
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    String driverId = parts[0].trim();
                    String name = parts[1].trim();
                    String phone = parts[2].trim();
                    
                    driverCache.put(driverId, new DriverInfo(driverId, name, phone));
                } else if (parts.length >= 3) {
                    String driverId = parts[0].trim();
                    String phone = parts[2].trim();
                    driverCache.put(driverId, new DriverInfo(driverId, "Driver", phone));
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
        
        if (!driverCache.containsKey(driverId)) {
            loadDriverCache();
        }
        
        return driverCache.get(driverId);
    }

    private void initialize() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(18, 25, 18, 25)
        ));

        // Left side - Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Track Your Package");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        leftPanel.add(titleLabel);
        
        JLabel badgeLabel = new JLabel("REAL-TIME");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLabel.setForeground(SUCCESS_GREEN);
        badgeLabel.setBackground(new Color(212, 237, 218));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        leftPanel.add(badgeLabel);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);

        // Right side - My Orders button
        myOrdersBtn = createModernButton("My Recent Orders", PRIMARY_LIGHT, 12);
        myOrdersBtn.addActionListener(e -> showMyRecentOrders());
        headerPanel.add(myOrdersBtn, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CARD_BG);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Search Panel
        searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Results Panel
        trackingResultPanel = new JPanel();
        trackingResultPanel.setLayout(new BoxLayout(trackingResultPanel, BoxLayout.Y_AXIS));
        trackingResultPanel.setBackground(CARD_BG);
        trackingResultPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Start with welcome message (fresh state)
        showWelcomeMessage();

        JScrollPane scrollPane = new JScrollPane(trackingResultPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        
        JLabel infoLabel = new JLabel("Click on any order below to track it");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(TEXT_GRAY);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    private JButton createModernButton(String text, Color bgColor, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    /**
     * Reset the panel to welcome state - called when navigating to this panel
     */
    public void resetToWelcomeState() {
        // Show welcome message
        showWelcomeMessage();
        // Refresh the UI
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private void showWelcomeMessage() {
        trackingResultPanel.removeAll();
        
        // Welcome card
        JPanel welcomeCard = createGlassCard();
        welcomeCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeCard.setMaximumSize(new Dimension(500, 240));
        
        JLabel welcomeLabel = new JLabel("Track Your Package");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(PRIMARY_BLUE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeCard.add(welcomeLabel);
        
        welcomeCard.add(Box.createVerticalStrut(15));
        
        JLabel instructionLabel = new JLabel("Click on any order below to view real-time delivery updates");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instructionLabel.setForeground(TEXT_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeCard.add(instructionLabel);
        
        welcomeCard.add(Box.createVerticalStrut(8));
        
        JLabel exampleLabel = new JLabel("Your orders are automatically synced from the system");
        exampleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        exampleLabel.setForeground(TEXT_MUTED);
        exampleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeCard.add(exampleLabel);
        
        trackingResultPanel.add(welcomeCard);
        trackingResultPanel.add(Box.createVerticalStrut(30));
        
        // Recent orders section
        showRecentOrdersList();
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private JPanel createGlassCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(16, BORDER_COLOR),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        return card;
    }

    private void showRecentOrdersList() {
        String userEmail = dashboard.getSenderEmail();
        List<SenderOrder> userOrders = SenderOrderRepository.getInstance().getOrdersByEmail(userEmail);
        
        // Section header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.setMaximumSize(new Dimension(600, 40));
        
        JLabel recentLabel = new JLabel("Your Recent Orders");
        recentLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentLabel.setForeground(TEXT_DARK);
        headerPanel.add(recentLabel, BorderLayout.WEST);
        
        JLabel countLabel = new JLabel("(" + userOrders.size() + " orders)");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);
        headerPanel.add(countLabel, BorderLayout.EAST);
        
        trackingResultPanel.add(headerPanel);
        trackingResultPanel.add(Box.createVerticalStrut(10));
        
        if (!userOrders.isEmpty()) {
            // Show last 5 orders
            int startIndex = Math.max(0, userOrders.size() - 5);
            for (int i = startIndex; i < userOrders.size(); i++) {
                SenderOrder order = userOrders.get(i);
                JPanel orderCard = createOrderCard(order);
                orderCard.setAlignmentX(Component.CENTER_ALIGNMENT);
                trackingResultPanel.add(orderCard);
                trackingResultPanel.add(Box.createVerticalStrut(8));
            }
            
            // View all button
            JButton viewAllBtn = new JButton("View All Orders");
            viewAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            viewAllBtn.setForeground(PRIMARY_LIGHT);
            viewAllBtn.setBackground(CARD_BG);
            viewAllBtn.setBorderPainted(false);
            viewAllBtn.setFocusPainted(false);
            viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewAllBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            viewAllBtn.addActionListener(e -> showMyRecentOrders());
            trackingResultPanel.add(viewAllBtn);
            
        } else {
            JPanel emptyCard = new JPanel();
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
            emptyCard.setBackground(new Color(248, 249, 250));
            emptyCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, BORDER_COLOR),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));
            emptyCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.setMaximumSize(new Dimension(400, 140));
            
            JLabel emptyLabel = new JLabel("You haven't created any orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.add(emptyLabel);
            
            emptyCard.add(Box.createVerticalStrut(15));
            
            JButton newOrderBtn = createModernButton("Create Your First Order", SUCCESS_GREEN, 12);
            newOrderBtn.setMaximumSize(new Dimension(200, 38));
            newOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            newOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));
            emptyCard.add(newOrderBtn);
            
            trackingResultPanel.add(emptyCard);
        }
    }

    private JPanel createOrderCard(SenderOrder order) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(600, 85));
        card.setPreferredSize(new Dimension(550, 85));
        
        // Left section - Order ID and route
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        leftPanel.setOpaque(false);
        
        JLabel idLabel = new JLabel(order.getId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        idLabel.setForeground(PRIMARY_BLUE);
        leftPanel.add(idLabel);
        
        String fromCity = extractCity(order.getCustomerAddress());
        String toCity = extractCity(order.getRecipientAddress());
        JLabel routeLabel = new JLabel(fromCity + " -> " + toCity);
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        routeLabel.setForeground(TEXT_GRAY);
        leftPanel.add(routeLabel);
        
        card.add(leftPanel, BorderLayout.WEST);
        
        // Center section - Status and cost
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel statusLabel = new JLabel(order.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(getStatusColor(order.getStatus()));
        centerPanel.add(statusLabel);
        
        double totalCost = extractTotalCostFromNotes(order);
        JLabel costLabel = new JLabel(String.format("RM %.2f", totalCost));
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        costLabel.setForeground(SUCCESS_GREEN);
        costLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(costLabel);
        
        card.add(centerPanel, BorderLayout.CENTER);
        
        // Right section - Speed and date
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        rightPanel.setOpaque(false);
        
        String shippingSpeed = extractShippingSpeed(order.getNotes());
        if (shippingSpeed != null) {
            JLabel speedLabel = new JLabel(shippingSpeed.equals("Express") ? "Express" : "Standard");
            speedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            speedLabel.setForeground(shippingSpeed.equals("Express") ? ORANGE : TEXT_GRAY);
            speedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            rightPanel.add(speedLabel);
        } else {
            JLabel spacerLabel = new JLabel(" ");
            rightPanel.add(spacerLabel);
        }
        
        String orderDate = formatDateShort(order.getOrderDate());
        JLabel dateLabel = new JLabel(orderDate);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLabel.setForeground(TEXT_GRAY);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(dateLabel);
        
        card.add(rightPanel, BorderLayout.EAST);
        
        // Hover effect - tracks the order in the same panel
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Track the order directly
                displayTrackingResult(order.getId());
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 248, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, PRIMARY_LIGHT),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)
                ));
            }
        });
        
        return card;
    }

    public void refreshOrders() {
        // Do nothing - we don't want to auto-refresh and show old orders
        // This method is kept for compatibility but does nothing
    }

    private void showMyRecentOrders() {
        trackingResultPanel.removeAll();
        
        String userEmail = dashboard.getSenderEmail();
        List<SenderOrder> userOrders = SenderOrderRepository.getInstance().getOrdersByEmail(userEmail);
        
        // Header
        JPanel headerCard = new JPanel();
        headerCard.setLayout(new BoxLayout(headerCard, BoxLayout.Y_AXIS));
        headerCard.setBackground(CARD_BG);
        headerCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("My Recent Orders");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerCard.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Click on any order to track it");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerCard.add(subtitleLabel);
        
        trackingResultPanel.add(headerCard);
        trackingResultPanel.add(Box.createVerticalStrut(15));
        
        if (!userOrders.isEmpty()) {
            List<SenderOrder> sortedOrders = new ArrayList<>(userOrders);
            sortedOrders.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));
            
            for (SenderOrder order : sortedOrders) {
                JPanel orderCard = createOrderCard(order);
                orderCard.setAlignmentX(Component.CENTER_ALIGNMENT);
                trackingResultPanel.add(orderCard);
                trackingResultPanel.add(Box.createVerticalStrut(8));
            }
        } else {
            JPanel emptyCard = new JPanel();
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
            emptyCard.setBackground(new Color(248, 249, 250));
            emptyCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, BORDER_COLOR),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
            ));
            emptyCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.setMaximumSize(new Dimension(400, 160));
            
            JLabel emptyLabel = new JLabel("You haven't created any orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.add(emptyLabel);
            
            emptyCard.add(Box.createVerticalStrut(15));
            
            JButton newOrderBtn = createModernButton("Create New Order", SUCCESS_GREEN, 12);
            newOrderBtn.setMaximumSize(new Dimension(180, 38));
            newOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            newOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));
            emptyCard.add(newOrderBtn);
            
            trackingResultPanel.add(emptyCard);
        }
        
        trackingResultPanel.add(Box.createVerticalStrut(15));
        addBackButton();
        
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
    }

    private void addBackButton() {
        JButton backBtn = new JButton("Back to Search");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setForeground(PRIMARY_LIGHT);
        backBtn.setBackground(CARD_BG);
        backBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_LIGHT));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(140, 35));
        backBtn.addActionListener(e -> {
            resetToWelcomeState();
        });
        trackingResultPanel.add(backBtn);
    }

    /**
     * Set tracking number externally (called from HomePanel)
     */
    public void setTrackingNumber(String trackingNumber) {
        displayTrackingResult(trackingNumber);
    }

    private void displayTrackingResult(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No tracking number provided", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        trackingResultPanel.removeAll();
        
        // Show loading indicator
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 20));
        loadingPanel.add(progressBar);
        JLabel loadingLabel = new JLabel("Fetching order details...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loadingLabel.setForeground(TEXT_GRAY);
        loadingPanel.add(loadingLabel);
        trackingResultPanel.add(loadingPanel);
        trackingResultPanel.revalidate();
        trackingResultPanel.repaint();
        
        // Simulate loading
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    trackingResultPanel.removeAll();
                    
                    SenderOrderRepository.getInstance().refreshData();
                    SenderOrder currentOrder = SenderOrderRepository.getInstance().getOrderById(trackingNumber);
                    String userEmail = dashboard.getSenderEmail();

                    if (currentOrder == null) {
                        showOrderNotFound(trackingNumber);
                    } else {
                        boolean isAuthorized = currentOrder.getCustomerEmail() != null && 
                                               userEmail != null && 
                                               currentOrder.getCustomerEmail().trim().equalsIgnoreCase(userEmail.trim());
                        
                        
                        if (isAuthorized) {
                            displayOrderTrackingDetails(currentOrder);
                        } else {
                            showUnauthorizedMessage();
                        }
                    }
                    
                    trackingResultPanel.revalidate();
                    trackingResultPanel.repaint();
                });
                timer.cancel();
            }
        }, 500);
    }

    private void showOrderNotFound(String trackingNumber) {
        JPanel notFoundCard = createGlassCard();
        notFoundCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        notFoundCard.setMaximumSize(new Dimension(450, 180));
        notFoundCard.setBackground(new Color(255, 245, 245));
        
        JLabel notFoundLabel = new JLabel("Order not found");
        notFoundLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        notFoundLabel.setForeground(DANGER_RED);
        notFoundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        notFoundCard.add(notFoundLabel);
        
        notFoundCard.add(Box.createVerticalStrut(10));
        
        JLabel suggestionLabel = new JLabel("We couldn't find order: " + trackingNumber);
        suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionLabel.setForeground(TEXT_GRAY);
        suggestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        notFoundCard.add(suggestionLabel);
        
        notFoundCard.add(Box.createVerticalStrut(8));
        
        JLabel hintLabel = new JLabel("Please check the tracking number and try again");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        notFoundCard.add(hintLabel);
        
        trackingResultPanel.add(notFoundCard);
        trackingResultPanel.add(Box.createVerticalStrut(20));
        addBackButton();
    }

    private void showUnauthorizedMessage() {
        JPanel unauthorizedCard = createGlassCard();
        unauthorizedCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        unauthorizedCard.setMaximumSize(new Dimension(450, 160));
        unauthorizedCard.setBackground(new Color(255, 248, 225));
        
        JLabel notAuthorizedLabel = new JLabel("Access Denied");
        notAuthorizedLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        notAuthorizedLabel.setForeground(WARNING_YELLOW);
        notAuthorizedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        unauthorizedCard.add(notAuthorizedLabel);
        
        unauthorizedCard.add(Box.createVerticalStrut(10));
        
        JLabel suggestionLabel = new JLabel("You are not authorized to view this order");
        suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionLabel.setForeground(TEXT_GRAY);
        suggestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        unauthorizedCard.add(suggestionLabel);
        
        trackingResultPanel.add(unauthorizedCard);
        trackingResultPanel.add(Box.createVerticalStrut(20));
        addBackButton();
    }

    private void displayOrderTrackingDetails(SenderOrder order) {
        // Create a centered wrapper panel
        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setBackground(CARD_BG);
        centerWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Status Header
        JPanel statusHeader = createEnhancedStatusHeader(order);
        statusHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(statusHeader);
        centerWrapper.add(Box.createVerticalStrut(20));
        
        // Visual Timeline - TALLER HEIGHT to show all text
        JPanel timelinePanel = createVisualTimeline(order);
        timelinePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(timelinePanel);
        centerWrapper.add(Box.createVerticalStrut(20));
        
        // Address Information - Full width
        JPanel addressPanel = createAddressPanel(order);
        addressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(addressPanel);
        centerWrapper.add(Box.createVerticalStrut(15));
        
        // Package Details - Full width
        JPanel packagePanel = createPackageDetailsPanel(order);
        packagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(packagePanel);
        centerWrapper.add(Box.createVerticalStrut(15));
        
        // Cost Breakdown - Full width
        JPanel costPanel = createCostBreakdownPanel(order);
        costPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(costPanel);
        centerWrapper.add(Box.createVerticalStrut(15));
        
        // Payment Details - Full width
        JPanel paymentPanel = createPaymentDetailsPanel(order);
        paymentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(paymentPanel);
        centerWrapper.add(Box.createVerticalStrut(15));
        
        // Shipping Information - Full width
        JPanel shippingPanel = createShippingInfoPanel(order);
        shippingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(shippingPanel);
        
        if (order.getDriverId() != null && !order.getDriverId().isEmpty()) {
            centerWrapper.add(Box.createVerticalStrut(15));
            JPanel driverPanel = createDriverDetailsPanel(order);
            driverPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerWrapper.add(driverPanel);
        }
        
        centerWrapper.add(Box.createVerticalStrut(20));
        
        // Action buttons
        JPanel buttonPanel = createOrderActionButtons(order);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(buttonPanel);
        centerWrapper.add(Box.createVerticalStrut(15));
        
        addBackButton();
        
        // Add the centered wrapper to the main panel
        trackingResultPanel.add(centerWrapper);
    }
    
    private JPanel createEnhancedStatusHeader(SenderOrder order) {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(new Color(240, 248, 255));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, PRIMARY_LIGHT),
            BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));
        headerPanel.setMaximumSize(new Dimension(700, 110));
        headerPanel.setPreferredSize(new Dimension(650, 110));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        infoPanel.setOpaque(false);
        
        JLabel orderIdLabel = new JLabel(order.getId());
        orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        orderIdLabel.setForeground(TEXT_DARK);
        infoPanel.add(orderIdLabel);
        
        JLabel statusLabel = new JLabel(order.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(getStatusColor(order.getStatus()));
        infoPanel.add(statusLabel);
        
        leftPanel.add(infoPanel);
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        rightPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Ordered: " + formatDate(order.getOrderDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_GRAY);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(dateLabel);
        
        double totalCost = extractTotalCostFromNotes(order);
        JLabel costLabel = new JLabel(String.format("RM %.2f", totalCost));
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        costLabel.setForeground(SUCCESS_GREEN);
        costLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(costLabel);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createVisualTimeline(SenderOrder order) {
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(CARD_BG);
        timelinePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Order Status Timeline",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_BLUE
        ));
        // INCREASED HEIGHT to show all text properly
        timelinePanel.setMaximumSize(new Dimension(700, 420));
        timelinePanel.setPreferredSize(new Dimension(650, 400));
        
        String[] stepTitles = {"Order Placed", "Processing", "In Transit", "Out for Delivery", "Delivered"};
        String[] stepDescriptions = {
            "Your order has been received and confirmed",
            "Your order is being prepared for shipping",
            "Your package is on its way to the destination",
            "Your package is out for delivery today",
            "Your package has been successfully delivered"
        };
        
        String currentStatus = order.getStatus();
        int currentStep = getStepIndex(currentStatus);
        
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
        
        if ("Delayed".equals(currentStatus)) {
            String reason = extractReason(order.getNotes());
            timelinePanel.add(createTimelineStep(
                "Delayed",
                reason != null ? "Delivery delayed: " + reason : "Delivery is experiencing delays",
                order.getOrderDate(),
                false,
                true,
                5,
                5
            ));
        } else if ("Cancelled".equals(currentStatus)) {
            timelinePanel.add(createTimelineStep(
                "Cancelled",
                "Order has been cancelled",
                order.getOrderDate(),
                false,
                true,
                5,
                5
            ));
        }
        
        return timelinePanel;
    }
    
    private int getStepIndex(String status) {
        switch(status) {
            case "Delivered": return 4;
            case "Out for Delivery": return 3;
            case "In Transit": return 2;
            case "Processing": return 1;
            case "Pending": return 0;
            default: return 0;
        }
    }
    
    private String getTimestampForStep(SenderOrder order, int step) {
        switch(step) {
            case 0: return order.getOrderDate();
            case 1: return addDaysToDate(order.getOrderDate(), 1);
            case 2: return addDaysToDate(order.getOrderDate(), 2);
            case 3: return addDaysToDate(order.getOrderDate(), 3);
            case 4: return order.getEstimatedDelivery() != null ? order.getEstimatedDelivery() : addDaysToDate(order.getOrderDate(), 5);
            default: return "Pending";
        }
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
        
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(completed ? TIMELINE_COMPLETED : TEXT_GRAY);
        stepPanel.add(timeLabel, BorderLayout.EAST);
        
        return stepPanel;
    }
    
    private JPanel createCostBreakdownPanel(SenderOrder order) {
        double shippingCost = extractShippingCostFromNotes(order);
        double insuranceCost = extractInsuranceCostFromNotes(order);
        double totalCost = extractTotalCostFromNotes(order);
        
        JPanel costPanel = new JPanel(new GridBagLayout());
        costPanel.setBackground(new Color(248, 249, 250));
        costPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SUCCESS_GREEN, 2),
            "Cost Breakdown",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            SUCCESS_GREEN
        ));
        costPanel.setMaximumSize(new Dimension(700, 180));
        costPanel.setPreferredSize(new Dimension(650, 160));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 15, 6, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel shippingLabel = new JLabel("Shipping Cost:");
        shippingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        costPanel.add(shippingLabel, gbc);
        
        gbc.gridx = 1;
        JLabel shippingValue = new JLabel(String.format("RM %.2f", shippingCost));
        shippingValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        costPanel.add(shippingValue, gbc);
        row++;
        
        if (insuranceCost > 0) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel insuranceLabel = new JLabel("Insurance Cost:");
            insuranceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            costPanel.add(insuranceLabel, gbc);
            
            gbc.gridx = 1;
            JLabel insuranceValue = new JLabel(String.format("RM %.2f", insuranceCost));
            insuranceValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            insuranceValue.setForeground(ORANGE);
            costPanel.add(insuranceValue, gbc);
            row++;
        }
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(SUCCESS_GREEN);
        costPanel.add(sep, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel totalLabel = new JLabel("TOTAL:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(TEXT_DARK);
        costPanel.add(totalLabel, gbc);
        
        gbc.gridx = 1;
        JLabel totalValue = new JLabel(String.format("RM %.2f", totalCost));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalValue.setForeground(SUCCESS_GREEN);
        costPanel.add(totalValue, gbc);
        
        return costPanel;
    }
    
    private JPanel createPaymentDetailsPanel(SenderOrder order) {
        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setBackground(new Color(248, 249, 250));
        paymentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Payment Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            PURPLE
        ));
        paymentPanel.setMaximumSize(new Dimension(700, 160));
        paymentPanel.setPreferredSize(new Dimension(650, 150));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 15, 6, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Payment Method
        String paymentMethod = order.getPaymentMethod();
        String displayPaymentMethod = "Not Selected";
        
        if (paymentMethod != null && !paymentMethod.isEmpty() && !"Not Selected".equals(paymentMethod)) {
            displayPaymentMethod = paymentMethod;
        } else {
            String notes = order.getNotes();
            if (notes != null && notes.contains("Payment Method:")) {
                try {
                    int start = notes.indexOf("Payment Method:") + 15;
                    int end = notes.indexOf(";", start);
                    if (end == -1) end = notes.indexOf("|", start);
                    if (end == -1) end = notes.length();
                    String extractedMethod = notes.substring(start, end).trim();
                    if (!extractedMethod.isEmpty()) {
                        displayPaymentMethod = extractedMethod;
                    }
                } catch (Exception e) {}
            }
        }
        
        addDetailRow(paymentPanel, "Payment Method:", displayPaymentMethod, gbc, row++);
        addDetailRow(paymentPanel, "Payment Status:", 
            order.getPaymentStatus() != null ? order.getPaymentStatus() : "Pending", gbc, row++);
        
        String transactionId = order.getTransactionId();
        if (transactionId != null && !transactionId.isEmpty()) {
            addDetailRow(paymentPanel, "Transaction ID:", transactionId, gbc, row++);
            if (order.getPaymentDate() != null && !order.getPaymentDate().isEmpty()) {
                addDetailRow(paymentPanel, "Payment Date:", order.getPaymentDate(), gbc, row++);
            }
        } else {
            addDetailRow(paymentPanel, "Transaction ID:", "Processing...", gbc, row++);
        }
        
        return paymentPanel;
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
    
    private JPanel createShippingInfoPanel(SenderOrder order) {
        JPanel shippingPanel = new JPanel(new GridBagLayout());
        shippingPanel.setBackground(new Color(248, 249, 250));
        shippingPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Shipping Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            INFO_BLUE
        ));
        shippingPanel.setMaximumSize(new Dimension(700, 100));
        shippingPanel.setPreferredSize(new Dimension(650, 95));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 15, 6, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        String shippingSpeed = "Standard";
        String notes = order.getNotes();
        if (notes != null) {
            if (notes.contains("Express")) {
                shippingSpeed = "Express (Priority)";
            } else if (notes.contains("Standard")) {
                shippingSpeed = "Standard (Economy)";
            }
        }
        
        String insuranceInfo = "Not Insured";
        if (notes != null && notes.contains("Insurance: YES")) {
            insuranceInfo = "Insured";
        }
        
        addDetailRow(shippingPanel, "Shipping Speed:", shippingSpeed, gbc, row++);
        addDetailRow(shippingPanel, "Insurance:", insuranceInfo, gbc, row++);
        
        return shippingPanel;
    }

    private JPanel createAddressPanel(SenderOrder order) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Address Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            PRIMARY_BLUE
        ));
        panel.setMaximumSize(new Dimension(700, 160));
        panel.setPreferredSize(new Dimension(650, 150));

        JPanel fromPanel = new JPanel(new BorderLayout(8, 8));
        fromPanel.setOpaque(false);
        fromPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel fromLabel = new JLabel("FROM");
        fromLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fromLabel.setForeground(PRIMARY_LIGHT);
        fromPanel.add(fromLabel, BorderLayout.NORTH);
        
        JLabel fromAddress = new JLabel("<html><div style='width: 100%; font-size: 12px;'>" + 
            order.getCustomerAddress() + "</div></html>");
        fromPanel.add(fromAddress, BorderLayout.CENTER);
        panel.add(fromPanel);

        JPanel toPanel = new JPanel(new BorderLayout(8, 8));
        toPanel.setOpaque(false);
        toPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel toLabel = new JLabel("TO");
        toLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toLabel.setForeground(SUCCESS_GREEN);
        toPanel.add(toLabel, BorderLayout.NORTH);
        
        JLabel toAddress = new JLabel("<html><div style='width: 100%; font-size: 12px;'>" + 
            order.getRecipientAddress() + "</div></html>");
        toPanel.add(toAddress, BorderLayout.CENTER);
        panel.add(toPanel);

        return panel;
    }

    private JPanel createPackageDetailsPanel(SenderOrder order) {
        JPanel packagePanel = new JPanel(new GridBagLayout());
        packagePanel.setBackground(new Color(248, 249, 250));
        packagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Package Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            ORANGE
        ));
        packagePanel.setMaximumSize(new Dimension(700, 160));
        packagePanel.setPreferredSize(new Dimension(650, 150));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 15, 4, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel weightLabel = new JLabel("Weight:");
        weightLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        packagePanel.add(weightLabel, gbc);
        
        gbc.gridx = 1;
        JLabel weightValue = new JLabel(String.format("%.2f kg", order.getWeight()));
        weightValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        packagePanel.add(weightValue, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel dimLabel = new JLabel("Dimensions:");
        dimLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        packagePanel.add(dimLabel, gbc);
        
        gbc.gridx = 1;
        JLabel dimValue = new JLabel(order.getDimensions() + " cm");
        dimValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        packagePanel.add(dimValue, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel typeLabel = new JLabel("Package Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        packagePanel.add(typeLabel, gbc);
        
        gbc.gridx = 1;
        JLabel typeValue = new JLabel(order.getPackageType());
        typeValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        packagePanel.add(typeValue, gbc);
        row++;
        
        String description = order.getDescription();
        if (description != null && !description.isEmpty()) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel descLabel = new JLabel("Description:");
            descLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            packagePanel.add(descLabel, gbc);
            
            gbc.gridx = 1;
            JLabel descValue = new JLabel(description.length() > 50 ? description.substring(0, 50) + "..." : description);
            descValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            packagePanel.add(descValue, gbc);
        }
        
        return packagePanel;
    }

    private JPanel createDriverDetailsPanel(SenderOrder order) {
        JPanel driverPanel = new JPanel(new GridBagLayout());
        driverPanel.setBackground(new Color(248, 249, 250));
        driverPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Driver Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            INFO_BLUE
        ));
        driverPanel.setMaximumSize(new Dimension(700, 130));
        driverPanel.setPreferredSize(new Dimension(650, 120));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.weightx = 1.0;
        
        String driverId = order.getDriverId();
        DriverInfo driverInfo = getDriverInfo(driverId);
        
        int row = 0;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel idLabel = new JLabel("Driver ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        driverPanel.add(idLabel, gbc);
        
        gbc.gridx = 1;
        String driverIdText = (driverInfo != null && driverInfo.driverId != null) ? driverInfo.driverId : (driverId != null ? driverId : "Not Assigned");
        JLabel idValue = new JLabel(driverIdText);
        idValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        idValue.setForeground(PRIMARY_LIGHT);
        driverPanel.add(idValue, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel nameLabel = new JLabel("Driver Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        driverPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        String driverName = (driverInfo != null && driverInfo.name != null) ? driverInfo.name : "Not Available";
        JLabel nameValue = new JLabel(driverName);
        nameValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        driverPanel.add(nameValue, gbc);
        row++;
        
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel phoneLabel = new JLabel("Driver Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        driverPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        String driverPhone = (driverInfo != null && driverInfo.phone != null) ? driverInfo.phone : "Not Available";
        JLabel phoneValue = new JLabel(driverPhone);
        phoneValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        driverPanel.add(phoneValue, gbc);
        
        return driverPanel;
    }

    private JPanel createOrderActionButtons(SenderOrder order) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setOpaque(false);
        
        if (!"Delivered".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus())) {
            JButton deleteBtn = new JButton("Delete Order");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBackground(DANGER_RED);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            deleteBtn.addActionListener(e -> deleteOrder(order));
            buttonPanel.add(deleteBtn);
        }
        
        return buttonPanel;
    }
    
    private void deleteOrder(SenderOrder order) {
        if ("Delivered".equals(order.getStatus())) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete a delivered order. Only pending or cancelled orders can be deleted.", 
                "Cannot Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to permanently delete order " + order.getId() + "?\n\n" +
            "This action cannot be undone!", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = SenderOrderRepository.getInstance().deleteOrder(order.getId());
            
            if (deleted) {
                JOptionPane.showMessageDialog(this, 
                    "Order " + order.getId() + " has been successfully deleted.", 
                    "Order Deleted", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                resetToWelcomeState();
                dashboard.refreshStats();
                dashboard.refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete order. Please try again.", 
                    "Delete Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) return "-";
        if (date.contains(" ")) {
            return date.substring(0, 10);
        }
        return date;
    }
    
    private String formatDateShort(String date) {
        if (date == null || date.isEmpty()) return "-";
        try {
            if (date.contains(" ")) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date d = inputFormat.parse(date);
                return outputFormat.format(d);
            }
        } catch (Exception e) {}
        return date.length() > 10 ? date.substring(0, 10) : date;
    }

    private Color getStatusColor(String status) {
        switch(status) {
            case "Delivered": return SUCCESS_GREEN;
            case "In Transit": return PRIMARY_LIGHT;
            case "Out for Delivery": return INFO_BLUE;
            case "Pending": return WARNING_YELLOW;
            case "Cancelled": return DANGER_RED;
            case "Delayed": return ORANGE;
            default: return TEXT_GRAY;
        }
    }
    
    private String addDaysToDate(String dateStr, int days) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, days);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return "Estimated: +" + days + " days";
        }
    }

    private String extractReason(String notes) {
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Reason:")) {
                String[] parts = notes.split(";");
                for (String part : parts) {
                    String trimmedPart = part.trim();
                    if (trimmedPart.startsWith("Reason:")) {
                        return trimmedPart.substring("Reason:".length()).trim();
                    }
                }
            }
            
            if (notes.contains("Reason:")) {
                String[] parts = notes.split("Reason:");
                if (parts.length > 1) {
                    String reason = parts[1].trim();
                    int pipeIndex = reason.indexOf("|");
                    if (pipeIndex > 0) reason = reason.substring(0, pipeIndex);
                    int semiIndex = reason.indexOf(";");
                    if (semiIndex > 0) reason = reason.substring(0, semiIndex);
                    return reason;
                }
            }
        }
        return null;
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
    
    private String extractShippingSpeed(String notes) {
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Shipping Speed: Express")) {
                return "Express";
            } else if (notes.contains("Shipping Speed: Standard")) {
                return "Standard";
            }
        }
        return null;
    }
    
    private double extractTotalCostFromNotes(SenderOrder order) {
        String notes = order.getNotes();
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Total Amount: RM")) {
                try {
                    int start = notes.indexOf("Total Amount: RM") + 15;
                    int end = notes.indexOf(";", start);
                    if (end == -1) end = notes.indexOf("|", start);
                    if (end == -1) end = notes.indexOf(" ", start + 10);
                    if (end == -1) end = notes.length();
                    String costStr = notes.substring(start, end).trim();
                    costStr = costStr.replaceAll("[^0-9.]", "");
                    if (!costStr.isEmpty()) {
                        return Double.parseDouble(costStr);
                    }
                } catch (Exception e) {}
            }
        }
        return order.getEstimatedCost();
    }
    
    private double extractShippingCostFromNotes(SenderOrder order) {
        String notes = order.getNotes();
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Shipping Cost: RM")) {
                try {
                    int start = notes.indexOf("Shipping Cost: RM") + 16;
                    int end = notes.indexOf(";", start);
                    if (end == -1) end = notes.indexOf("|", start);
                    if (end == -1) end = notes.indexOf(" ", start + 10);
                    if (end == -1) end = notes.length();
                    String costStr = notes.substring(start, end).trim();
                    costStr = costStr.replaceAll("[^0-9.]", "");
                    if (!costStr.isEmpty()) {
                        return Double.parseDouble(costStr);
                    }
                } catch (Exception e) {}
            }
        }
        return order.getEstimatedCost();
    }
    
    private double extractInsuranceCostFromNotes(SenderOrder order) {
        String notes = order.getNotes();
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Insurance Cost: RM")) {
                try {
                    int start = notes.indexOf("Insurance Cost: RM") + 17;
                    int end = notes.indexOf(")", start);
                    if (end == -1) end = notes.indexOf(";", start);
                    if (end == -1) end = notes.indexOf("|", start);
                    if (end == -1) end = notes.length();
                    String costStr = notes.substring(start, end).trim();
                    costStr = costStr.replaceAll("[^0-9.]", "");
                    if (!costStr.isEmpty()) {
                        return Double.parseDouble(costStr);
                    }
                } catch (Exception e) {}
            }
        }
        return 0.0;
    }
    
    private void startAutoRefresh() {
        javax.swing.Timer timer = new javax.swing.Timer(30000, e -> {
            // Do nothing - auto-refresh is disabled to prevent unwanted behavior
        });
        timer.start();
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
}