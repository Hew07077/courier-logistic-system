// SenderDashboard.java
package sender;

import logistics.login.Login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SenderDashboard extends JFrame {

    // Refined color palette
    private final Color BLUE_PRIMARY = new Color(0, 123, 255);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color DANGER_RED = new Color(220, 53, 69);
    
    // Button colors
    private final Color BUTTON_SELECTED = new Color(0, 86, 179);
    private final Color BUTTON_HOVER = new Color(0, 105, 217);
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0);

    // Main components
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    private JPanel sidebar;
    private JPanel topBar;
    private JPanel statusBar;
    
    // Panel cache
    private Map<String, JPanel> panelCache;
    
    // Panel instances
    private HomePanel homePanel;
    private NewOrderPanel newOrderPanel;
    private SupportPanel supportPanel;
    private ProfilePanel profilePanel;
    private TrackOrderPanel trackOrderPanel;
    
    // User data
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String senderAddress;
    private String username;
    private int activeOrders = 0;
    private int deliveredOrders = 0;
    private int pendingPayments = 0;
    private double totalSpent = 0.0;
    
    // Timer for auto-refresh
    private Timer refreshTimer;

    // Constructor with user data
    public SenderDashboard(String name, String email, String phone, String username) {
        this.senderName = name;
        this.senderEmail = email;
        this.senderPhone = phone;
        this.username = username;
        
        // Handle demo vs regular users
        if (email != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(email)) {
            System.out.println("======================================");
            System.out.println("DEMO USER DETECTED: " + email);
            System.out.println("======================================");
            
            // Get the data manager instance
            SenderDataManager dataManager = SenderDataManager.getInstance();
            
            // Check if demo data already exists for this user
            List<SenderOrder> existingOrders = dataManager.getOrdersByEmail(email);
            System.out.println("Existing orders for demo user: " + existingOrders.size());
            
            if (existingOrders.isEmpty()) {
                System.out.println("No existing demo orders found. Creating demo data...");
                // Add demo orders to the system
                DemoDataManager.getInstance().addDemoOrdersToSystem(dataManager);
                
                // Force save to ensure data is written to file
                dataManager.saveOrders();
                
                // Verify orders were added
                List<SenderOrder> updatedOrders = dataManager.getOrdersByEmail(email);
                System.out.println("After adding: " + updatedOrders.size() + " demo orders");
            } else {
                System.out.println("Demo orders already exist: " + existingOrders.size() + " orders");
            }
            
            this.senderAddress = "123 Jalan SS2, Petaling Jaya, Selangor 47300";
            
            // Print all demo orders for debugging
            List<SenderOrder> finalOrders = dataManager.getOrdersByEmail(email);
            System.out.println("Final demo orders count: " + finalOrders.size());
            for (SenderOrder order : finalOrders) {
                System.out.println("  - Order: " + order.getId() + ", Status: " + order.getStatus() + ", Payment: " + order.getPaymentStatus());
            }
            System.out.println("======================================");
        } else {
            System.out.println("REGULAR USER LOGGED IN: " + email);
        }
        
        initializeStats();
        fixPendingPayments(); // Fix any incorrect payment statuses
        initialize();
        startAutoRefresh();
    }
    
    // Temporary fix - call this from constructor after initializeStats()
    private void fixPendingPayments() {
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(senderEmail);
        boolean fixed = false;
        
        for (SenderOrder order : userOrders) {
            // If order is not pending and not cancelled, but payment is pending, fix it
            if (!"Pending".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus()) 
                && "Pending".equals(order.getPaymentStatus())) {
                System.out.println("Fixing payment status for order: " + order.getId());
                order.setPaymentStatus("Paid");
                // Generate a transaction ID if not present
                if (order.getTransactionId() == null || order.getTransactionId().isEmpty()) {
                    order.setTransactionId("TXN" + System.currentTimeMillis() + order.getId().substring(0, 3));
                }
                if (order.getPaymentDate() == null || order.getPaymentDate().isEmpty()) {
                    order.setPaymentDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                }
                fixed = true;
            }
        }
        
        if (fixed) {
            SenderDataManager.getInstance().saveOrders();
            System.out.println("Fixed pending payments for orders");
        }
    }

    private void initializeStats() {
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(senderEmail);
        
        activeOrders = 0;
        deliveredOrders = 0;
        pendingPayments = 0;
        totalSpent = 0.0;
        
        System.out.println("Initializing stats for " + senderEmail + " with " + userOrders.size() + " orders");
        
        for (SenderOrder order : userOrders) {
            // Count active orders (not delivered and not cancelled)
            if (!"Delivered".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus())) {
                activeOrders++;
                System.out.println("  Active order: " + order.getId() + " - Status: " + order.getStatus());
            }
            
            // Count delivered orders
            if ("Delivered".equals(order.getStatus())) {
                deliveredOrders++;
                System.out.println("  Delivered order: " + order.getId());
            }
            
            // Count pending payments - FIX: Check payment status correctly
            if ("Pending".equals(order.getPaymentStatus())) {
                pendingPayments++;
                System.out.println("  Pending payment order: " + order.getId() + " - Payment Status: " + order.getPaymentStatus());
            } else {
                System.out.println("  Order " + order.getId() + " payment status: " + order.getPaymentStatus());
            }
            
            // Calculate total spent from paid orders only
            if ("Paid".equals(order.getPaymentStatus())) {
                double cost = order.getEstimatedCost();
                totalSpent += cost;
                System.out.println("  Paid order amount: RM " + cost);
            }
        }
        
        System.out.println("Stats - Active: " + activeOrders + 
                           ", Delivered: " + deliveredOrders + 
                           ", Pending Payments: " + pendingPayments + 
                           ", Total Spent: RM " + String.format("%.2f", totalSpent));
    }

    private String extractCost(String notes) {
        if (notes != null && notes.contains("Estimated Cost:")) {
            String[] parts = notes.split("Estimated Cost: ");
            if (parts.length > 1) {
                String[] costParts = parts[1].split(" ");
                return costParts[0].trim();
            }
        }
        return "RM 0.00";
    }

    private void initialize() {
        setTitle("LogiXpress Sender Portal - Welcome " + senderName);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        panelCache = new HashMap<>();
        
        initializePanels();
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
    }

    private void initializePanels() {
        homePanel = new HomePanel(this);
        newOrderPanel = new NewOrderPanel(this);
        supportPanel = new SupportPanel(this);
        profilePanel = new ProfilePanel(this);
        trackOrderPanel = new TrackOrderPanel(this);
        
        panelCache.put("HOME", homePanel);
        panelCache.put("NEW_ORDER", newOrderPanel);
        panelCache.put("SUPPORT", supportPanel);
        panelCache.put("PROFILE", profilePanel);
        panelCache.put("TRACK", trackOrderPanel);
    }

    private void initUI() {
        createTopBar();
        createSidebar();
        createContentPanel();
        createStatusBar();
        
        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void createTopBar() {
        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(25, 135, 255));
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        // Load and display logo in top bar
        ImageIcon logoIcon = loadLogo("logo.jpeg");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            JLabel logoImage = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoImage);
        } else {
            // Fallback text if logo not found (no emoji)
            JLabel logoLabel = new JLabel("LX");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            logoLabel.setForeground(Color.WHITE);
            leftPanel.add(logoLabel);
        }

        JLabel title = new JLabel("LogiXpress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        topBar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setOpaque(false);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        timePanel.setOpaque(false);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        timePanel.add(timeLabel);

        Timer timer = new Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(new Date()))
        );
        timer.start();

        rightPanel.add(timePanel);

        JLabel welcomeUser = new JLabel(senderName);
        welcomeUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        welcomeUser.setForeground(Color.WHITE);
        rightPanel.add(welcomeUser);

        JButton logout = createButton("Logout", DANGER_RED);
        logout.addActionListener(e -> logout());
        rightPanel.add(logout);

        topBar.add(rightPanel, BorderLayout.EAST);
    }

    private void createSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BLUE_PRIMARY);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));

        // Logo area
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));

        // Load and display logo in sidebar
        ImageIcon logoIcon = loadLogo("logo.jpeg");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoImage = new JLabel(new ImageIcon(resizedImg));
            logoImage.setHorizontalAlignment(SwingConstants.CENTER);
            logoPanel.add(logoImage, BorderLayout.NORTH);
        } else {
            // Fallback text if logo not found (no emoji)
            JLabel placeholderLogo = new JLabel("LX", SwingConstants.CENTER);
            placeholderLogo.setFont(new Font("Segoe UI", Font.BOLD, 48));
            placeholderLogo.setForeground(Color.WHITE);
            logoPanel.add(placeholderLogo, BorderLayout.NORTH);
        }

        JLabel logo = new JLabel("LogiXpress", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        logoPanel.add(logo, BorderLayout.CENTER);

        JLabel version = new JLabel("Sender Portal v1.0.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        version.setForeground(new Color(255, 255, 255, 180));
        logoPanel.add(version, BorderLayout.SOUTH);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menu items - 5 items (no emojis)
        JPanel menu = new JPanel(new GridLayout(5, 1, 0, 5));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        menu.add(createNavButtonWithTooltip("Home", "HOME", "Dashboard overview with statistics and quick actions", true));
        menu.add(createNavButtonWithTooltip("New Order", "NEW_ORDER", "Create a new shipment order", false));
        menu.add(createNavButtonWithTooltip("Track Order", "TRACK", "Track your package by tracking number", false));
        menu.add(createNavButtonWithTooltip("Support", "SUPPORT", "Get help and contact support", false));
        menu.add(createNavButtonWithTooltip("Profile", "PROFILE", "Manage your account settings", false));

        sidebar.add(menu, BorderLayout.CENTER);

        // User profile (no emoji)
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);

        JLabel userName = new JLabel(senderName, SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userName.setForeground(Color.WHITE);
        userInfo.add(userName);

        JLabel userRole = new JLabel("Premium Sender", SwingConstants.CENTER);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(new Color(255, 255, 255, 200));
        userInfo.add(userRole);

        profile.add(userInfo, BorderLayout.CENTER);
        sidebar.add(profile, BorderLayout.SOUTH);
    }

    /**
     * Loads an image from file
     * @param path The path to the image file
     * @return ImageIcon if successful, null if failed
     */
    private ImageIcon loadLogo(String path) {
        try {
            // Try loading from classpath (same package)
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                // Try loading from file system
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
                
                // Try loading from parent directory
                file = new java.io.File("../" + path);
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
                
                // Try loading from resources folder
                file = new java.io.File("resources/" + path);
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        return null;
    }

    private JButton createNavButtonWithTooltip(String text, String card, String tooltip, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        contentPanel.setOpaque(false);
        
        // No icon, just text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        textLabel.setForeground(Color.WHITE);
        contentPanel.add(textLabel);
        
        btn.add(contentPanel, BorderLayout.CENTER);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        
        if (selected) {
            btn.setBackground(BUTTON_SELECTED);
            btn.setOpaque(true);
        } else {
            btn.setBackground(BUTTON_NORMAL);
            btn.setOpaque(false);
        }
        
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) activeButton = btn;

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setOpaque(true);
                    btn.setBackground(BUTTON_HOVER);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setOpaque(false);
                    btn.setBackground(BUTTON_NORMAL);
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null && activeButton != btn) {
                activeButton.setOpaque(false);
                activeButton.setBackground(BUTTON_NORMAL);
                activeButton.repaint();
            }
            
            btn.setOpaque(true);
            btn.setBackground(BUTTON_SELECTED);
            activeButton = btn;
            
            showPanel(card);
        });

        return btn;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (Map.Entry<String, JPanel> entry : panelCache.entrySet()) {
            contentPanel.add(entry.getValue(), entry.getKey());
        }

        showPanel("HOME");
    }

    private void createStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(248, 249, 250));
        statusBar.setPreferredSize(new Dimension(getWidth(), 35));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JLabel status = new JLabel("  System Status: Connected | Last updated: " + 
            new SimpleDateFormat("HH:mm:ss").format(new Date()));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_GRAY);
        
        Timer statusTimer = new Timer(1000, e -> 
            status.setText("  System Status: Connected | Last updated: " + 
                new SimpleDateFormat("HH:mm:ss").format(new Date()))
        );
        statusTimer.start();
        
        statusBar.add(status, BorderLayout.WEST);

        JLabel version = new JLabel("LogiXpress Sender Portal v1.0.0  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_GRAY);
        statusBar.add(version, BorderLayout.EAST);
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        
        switch(panelName) {
            case "HOME":
                if (homePanel != null) homePanel.refreshData();
                break;
            case "TRACK":
                if (trackOrderPanel != null) trackOrderPanel.refreshOrders();
                break;
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            // Stop auto-refresh timer
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            
            dispose();
            SwingUtilities.invokeLater(() -> {
                Login login = new Login();
                login.setVisible(true);
            });
        }
    }

    /**
     * Start auto-refresh timer to keep data in sync
     */
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> {
            SwingUtilities.invokeLater(() -> {
                refreshAllData();
            });
        });
        refreshTimer.start();
    }

    /**
     * Refresh all data from main system
     */
    public void refreshAllData() {
        System.out.println("Refreshing all sender data from main system...");
        
        // Refresh data manager
        SenderDataManager.getInstance().refreshData();
        
        // Update stats
        refreshStats();
        
        // Refresh current panel
        String currentCard = getCurrentCard();
        if (currentCard != null) {
            switch(currentCard) {
                case "HOME":
                    if (homePanel != null) homePanel.refreshData();
                    break;
                case "TRACK":
                    if (trackOrderPanel != null) trackOrderPanel.refreshOrders();
                    break;
            }
        }
        
        // Update sidebar notification counts
        updateSidebarNotifications();
    }

    /**
     * Refresh statistics from current data - FIXED VERSION
     */
    public void refreshStats() {
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(senderEmail);
        
        activeOrders = 0;
        deliveredOrders = 0;
        pendingPayments = 0;
        totalSpent = 0.0;
        
        System.out.println("Refreshing stats for " + senderEmail + " - Total orders: " + userOrders.size());
        
        for (SenderOrder order : userOrders) {
            // Count active orders (not delivered and not cancelled)
            if (!"Delivered".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus())) {
                activeOrders++;
                System.out.println("  Active order: " + order.getId() + " - Status: " + order.getStatus());
            }
            
            // Count delivered orders
            if ("Delivered".equals(order.getStatus())) {
                deliveredOrders++;
                System.out.println("  Delivered order: " + order.getId());
            }
            
            // Count pending payments - FIX: Check payment status correctly
            if ("Pending".equals(order.getPaymentStatus())) {
                pendingPayments++;
                System.out.println("  Pending payment order: " + order.getId() + " - Payment Status: " + order.getPaymentStatus());
            } else {
                System.out.println("  Order " + order.getId() + " payment status: " + order.getPaymentStatus());
            }
            
            // Calculate total spent from paid orders only
            if ("Paid".equals(order.getPaymentStatus())) {
                double cost = order.getEstimatedCost();
                totalSpent += cost;
                System.out.println("  Paid order amount: RM " + cost);
            }
        }
        
        System.out.println("Stats refreshed - Active: " + activeOrders + 
                           ", Delivered: " + deliveredOrders + 
                           ", Pending Payments: " + pendingPayments + 
                           ", Total Spent: RM " + String.format("%.2f", totalSpent));
    }

    /**
     * Get current card being displayed
     */
    private String getCurrentCard() {
        if (activeButton != null) {
            JPanel contentPanel = (JPanel) activeButton.getComponent(0);
            for (Component comp : contentPanel.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    String text = label.getText();
                    if (text.contains("Home")) return "HOME";
                    if (text.contains("New Order")) return "NEW_ORDER";
                    if (text.contains("Track Order")) return "TRACK";
                    if (text.contains("Support")) return "SUPPORT";
                    if (text.contains("Profile")) return "PROFILE";
                }
            }
        }
        return "HOME";
    }

    /**
     * Update notification counts in sidebar
     */
    private void updateSidebarNotifications() {
        // Notifications removed - this method is kept for compatibility
    }

    // ================= GETTERS AND SETTERS =================
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderPhone() { return senderPhone; }
    public String getSenderAddress() { return senderAddress; }
    public String getUsername() { return username; }
    public int getActiveOrders() { return activeOrders; }
    public int getDeliveredOrders() { return deliveredOrders; }
    public int getPendingPayments() { return pendingPayments; }
    public double getTotalSpent() { return totalSpent; }
    
    public void setSenderName(String name) { this.senderName = name; }
    public void setSenderEmail(String email) { this.senderEmail = email; }
    public void setSenderPhone(String phone) { this.senderPhone = phone; }
    public void setSenderAddress(String address) { this.senderAddress = address; }
    public void setActiveOrders(int count) { this.activeOrders = count; }
    public void setDeliveredOrders(int count) { this.deliveredOrders = count; }
    public void setPendingPayments(int count) { this.pendingPayments = count; }
    public void setTotalSpent(double amount) { this.totalSpent = amount; }
}