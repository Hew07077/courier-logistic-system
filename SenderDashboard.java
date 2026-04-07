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
    private final Color BLUE_DARK = new Color(0, 86, 179);
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
        
        initializeStats();
        fixPendingPayments(); // Fix any incorrect payment statuses
        initialize();
        startAutoRefresh();
    }
    
    // Temporary fix - call this from constructor after initializeStats()
    private void fixPendingPayments() {
        List<SenderOrder> userOrders = SenderOrderRepository.getInstance().getOrdersByEmail(senderEmail);
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
            SenderOrderRepository.getInstance().saveOrders();
            System.out.println("Fixed pending payments for orders");
        }
    }

    private void initializeStats() {
        List<SenderOrder> userOrders = SenderOrderRepository.getInstance().getOrdersByEmail(senderEmail);
        
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
        supportPanel = new SupportPanel();
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
        ImageIcon logoIcon = loadLogo("logos.png");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            JLabel logoImage = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoImage);
        } else {
            // Fallback text if logo not found
            JLabel logoLabel = new JLabel("LX");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            logoLabel.setForeground(Color.WHITE);
            leftPanel.add(logoLabel);
        }

        JLabel title = new JLabel("LogiXpress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JLabel badge = new JLabel("SENDER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(BLUE_DARK);
        badge.setBackground(new Color(255, 255, 255, 200));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        leftPanel.add(badge);

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
        ImageIcon logoIcon = loadLogo("logos.png");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoImage = new JLabel(new ImageIcon(resizedImg));
            logoImage.setHorizontalAlignment(SwingConstants.CENTER);
            logoPanel.add(logoImage, BorderLayout.NORTH);
        } else {
            // Fallback text if logo not found
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

        // Menu items
        JPanel menu = new JPanel(new GridLayout(5, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JButton homeBtn = createNavButton("Home", "HOME", 
            "Dashboard overview", true);
        menu.add(homeBtn);
        
        JButton newOrderBtn = createNavButton("New Order", "NEW_ORDER", 
            "Create new shipment", false);
        menu.add(newOrderBtn);
        
        JButton trackBtn = createNavButton("Track Order", "TRACK", 
            "Track your packages", false);
        menu.add(trackBtn);
        
        JButton supportBtn = createNavButton("Support", "SUPPORT", 
            "Get help and support", false);
        menu.add(supportBtn);
        
        JButton profileBtn = createNavButton("My Profile", "PROFILE", 
            "Personal information", false);
        menu.add(profileBtn);

        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(createUserProfile(), BorderLayout.SOUTH);
    }

    private JButton createNavButton(String text, String card, String notification, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        
        String notificationColor = selected ? "#FFFFFF" : "#A0C0FF";
        String displayText = "<html><div style='text-align: left;'>" +
                            "<b style='font-size: 13px;'>" + text + "</b><br>" +
                            "<span style='font-size: 11px; color: " + notificationColor + ";'>" + 
                            notification + "</span></div></html>";
        
        JLabel contentLabel = new JLabel(displayText);
        contentLabel.setForeground(Color.WHITE);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.add(contentLabel, BorderLayout.CENTER);
        
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
        btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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

    private JPanel createUserProfile() {
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(3, 1));
        userInfo.setOpaque(false);

        String displayName = senderName != null ? senderName : "Loading...";
        String displayId = "Premium Sender";
        String displayOrders = activeOrders + " active orders";

        JLabel userName = new JLabel(displayName, SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userName.setForeground(Color.WHITE);
        userInfo.add(userName);

        JLabel userIdLabel = new JLabel(displayId, SwingConstants.CENTER);
        userIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userIdLabel.setForeground(new Color(255, 255, 255, 200));
        userInfo.add(userIdLabel);
        
        JLabel userOrders = new JLabel(displayOrders, SwingConstants.CENTER);
        userOrders.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userOrders.setForeground(new Color(255, 255, 255, 180));
        userInfo.add(userOrders);

        profile.add(userInfo, BorderLayout.CENTER);
        return profile;
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

        String orderStats = activeOrders + " active · " + deliveredOrders + " delivered";
        JLabel rightStats = new JLabel(orderStats + "  ");
        rightStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightStats.setForeground(BLUE_PRIMARY);
        statusBar.add(rightStats, BorderLayout.EAST);
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        
        switch(panelName) {
            case "HOME":
                if (homePanel != null) homePanel.refreshData();
                break;
            case "TRACK":
                if (trackOrderPanel != null) {
                    trackOrderPanel.resetToWelcomeState();  // Reset to welcome screen
                }
                break;
            case "PROFILE":
                // No refresh needed
                break;
            case "NEW_ORDER":
                // No refresh needed
                break;
            case "SUPPORT":
                // No refresh needed
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
        SenderOrderRepository.getInstance().refreshData();
        
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
                    if (trackOrderPanel != null) {
                        // Don't auto-refresh track panel - just reset if needed
                        // trackOrderPanel.refreshOrders() is disabled
                    }
                    break;
            }
        }
        
        // Update sidebar user info
        updateSidebarUserInfo();
        
        // Update status bar
        updateStatusBar();
    }

    /**
     * Refresh statistics from current data - FIXED VERSION
     */
    public void refreshStats() {
        List<SenderOrder> userOrders = SenderOrderRepository.getInstance().getOrdersByEmail(senderEmail);
        
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

    private void updateSidebarUserInfo() {
        // Find and update user info in sidebar
        Component[] components = sidebar.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0) {
                JPanel panel = (JPanel) comp;
                Component innerComp = panel.getComponent(0);
                if (innerComp instanceof JPanel) {
                    JPanel userInfo = (JPanel) innerComp;
                    if (userInfo.getComponentCount() >= 3) {
                        Component nameComp = userInfo.getComponent(0);
                        Component ordersComp = userInfo.getComponent(2);
                        if (nameComp instanceof JLabel) {
                            ((JLabel) nameComp).setText(senderName);
                        }
                        if (ordersComp instanceof JLabel) {
                            ((JLabel) ordersComp).setText(activeOrders + " active orders");
                        }
                    }
                }
            }
        }
    }

    private void updateStatusBar() {
        Component[] components = statusBar.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && ((JLabel) comp).getHorizontalAlignment() == SwingConstants.RIGHT) {
                ((JLabel) comp).setText(activeOrders + " active · " + deliveredOrders + " delivered  ");
                break;
            }
        }
    }

    /**
     * Get current card being displayed
     */
    private String getCurrentCard() {
        if (activeButton != null) {
            JLabel contentLabel = (JLabel) ((JButton) activeButton).getComponent(0);
            String text = contentLabel.getText();
            if (text.contains("Home")) return "HOME";
            if (text.contains("New Order")) return "NEW_ORDER";
            if (text.contains("Track Order")) return "TRACK";
            if (text.contains("Support")) return "SUPPORT";
            if (text.contains("My Profile")) return "PROFILE";
        }
        return "HOME";
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // For testing - replace with actual login data
            new SenderDashboard("Test User", "test@example.com", "0123456789", "testuser").setVisible(true);
        });
    }
}