package sender;

import logistics.login.Login;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SenderDashboard extends JFrame {

    // Refined color palette
    private final Color BLUE_PRIMARY = new Color(0, 123, 255);
    private final Color BLUE_DARK = new Color(0, 86, 179);
    private final Color BLUE_LIGHT = new Color(200, 225, 255);
    private final Color BLUE_PALE = new Color(240, 248, 255);
    private final Color BLUE_TOP_BAR = new Color(25, 135, 255);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color WARNING_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(220, 53, 69);
    private final Color INFO_CYAN = new Color(23, 162, 184);
    private final Color PURPLE = new Color(111, 66, 193);
    
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
    private MyOrdersPanel myOrdersPanel;
    private TrackOrderPanel trackOrderPanel;
    private PaymentPanel paymentPanel;
    private SupportPanel supportPanel;
    private ProfilePanel profilePanel;
    
    // User data
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String senderAddress = "PV9, Kuala Lumpur";
    private String username;
    private int activeOrders = 3;
    private int deliveredOrders = 12;
    private int pendingPayments = 2;
    private double totalSpent = 1245.50;

    // Constructor with user data
    public SenderDashboard(String name, String email, String phone, String username) {
        this.senderName = name;
        this.senderEmail = email;
        this.senderPhone = phone;
        this.username = username;
        
        initialize();
    }

    private void initialize() {
        setTitle("LogiXpress Sender Portal - Welcome " + senderName);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        panelCache = new HashMap<>();
        
        // Initialize all panels
        initializePanels();
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize UI with all design
        initUI();
    }

    private void initializePanels() {
        homePanel = new HomePanel(this);
        newOrderPanel = new NewOrderPanel(this);
        myOrdersPanel = new MyOrdersPanel(this);
        trackOrderPanel = new TrackOrderPanel(this);
        paymentPanel = new PaymentPanel(this);
        supportPanel = new SupportPanel(this);
        profilePanel = new ProfilePanel(this);
        
        panelCache.put("HOME", homePanel);
        panelCache.put("NEW_ORDER", newOrderPanel);
        panelCache.put("MY_ORDERS", myOrdersPanel);
        panelCache.put("TRACK", trackOrderPanel);
        panelCache.put("PAYMENT", paymentPanel);
        panelCache.put("SUPPORT", supportPanel);
        panelCache.put("PROFILE", profilePanel);
    }

    // ================= ALL DESIGN IN ONE PLACE =================
    private void initUI() {
        // Create all design components
        createTopBar();
        createSidebar();
        createContentPanel();
        createStatusBar();
        
        // Add to frame
        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    // ================= TOP BAR DESIGN =================
    private void createTopBar() {
        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BLUE_TOP_BAR);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        // Left panel with logo
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        // Load logo
        ImageIcon logoIcon = loadLogo("logo.jpeg");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        } else {
            // Placeholder if logo not found
            JLabel placeholderLogo = new JLabel("📦");
            placeholderLogo.setFont(new Font("Segoe UI", Font.PLAIN, 40));
            placeholderLogo.setForeground(Color.WHITE);
            leftPanel.add(placeholderLogo);
        }

        JLabel title = new JLabel("LogiXpress Sender Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JLabel badge = new JLabel("SENDER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(BLUE_PRIMARY);
        badge.setBackground(new Color(255, 255, 255, 200));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        leftPanel.add(badge);

        topBar.add(leftPanel, BorderLayout.WEST);

        // Right panel with time and buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setOpaque(false);

        // Time panel
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

        // Welcome user
        JLabel welcomeUser = new JLabel("👤 " + senderName);
        welcomeUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        welcomeUser.setForeground(Color.WHITE);
        rightPanel.add(welcomeUser);

        // Notification button
        JButton notificationBtn = createButton("🔔", new Color(0, 123, 180));
        notificationBtn.setToolTipText("Notifications");
        notificationBtn.addActionListener(e -> showNotifications());
        rightPanel.add(notificationBtn);

        // Logout button
        JButton logout = createButton("Logout", DANGER_RED);
        logout.addActionListener(e -> logout());
        rightPanel.add(logout);

        topBar.add(rightPanel, BorderLayout.EAST);
    }

    // ================= SIDEBAR DESIGN =================
    private void createSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BLUE_PRIMARY);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));

        // Logo area
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));

        ImageIcon mainLogoIcon = loadLogo("logo.jpeg");
        if (mainLogoIcon != null) {
            Image img = mainLogoIcon.getImage();
            Image resizedImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoImage = new JLabel(new ImageIcon(resizedImg));
            logoImage.setHorizontalAlignment(SwingConstants.CENTER);
            logoPanel.add(logoImage, BorderLayout.NORTH);
        } else {
            JLabel placeholderLogo = new JLabel("📦", SwingConstants.CENTER);
            placeholderLogo.setFont(new Font("Segoe UI", Font.PLAIN, 60));
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
        JPanel menu = new JPanel(new GridLayout(7, 1, 0, 5));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        menu.add(createNavButtonWithTooltip("🏠 Home", "HOME", "Dashboard overview with statistics and quick actions", true));
        menu.add(createNavButtonWithTooltip("📦 New Order", "NEW_ORDER", "Create a new shipment order", false));
        menu.add(createNavButtonWithTooltip("📋 My Orders", "MY_ORDERS", "View and manage your orders (" + activeOrders + " active)", false));
        menu.add(createNavButtonWithTooltip("🔍 Track", "TRACK", "Track your package in real-time", false));
        menu.add(createNavButtonWithTooltip("💰 Payments", "PAYMENT", "Manage payments and invoices (" + pendingPayments + " pending)", false));
        menu.add(createNavButtonWithTooltip("❓ Support", "SUPPORT", "Get help and contact support", false));
        menu.add(createNavButtonWithTooltip("👤 Profile", "PROFILE", "Manage your account settings", false));

        sidebar.add(menu, BorderLayout.CENTER);

        // User profile
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);

        JLabel userName = new JLabel("👤 " + senderName, SwingConstants.CENTER);
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

    // ================= NAVIGATION BUTTON WITH TOOLTIP =================
    private JButton createNavButtonWithTooltip(String text, String card, String tooltip, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        
        // Create panel with icon and text
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        contentPanel.setOpaque(false);
        
        // Extract icon from text (first character or emoji)
        String icon = text.substring(0, text.indexOf(' ') + 1);
        String title = text.substring(text.indexOf(' ') + 1);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setPreferredSize(new Dimension(30, 30));
        contentPanel.add(iconLabel);
        
        JLabel textLabel = new JLabel(title);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        textLabel.setForeground(Color.WHITE);
        contentPanel.add(textLabel);
        
        btn.add(contentPanel, BorderLayout.CENTER);
        
        // Set tooltip for details on hover
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

        // Hover effect
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

        // Action listener
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

    // ================= BUTTON DESIGN =================
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

    // ================= CONTENT PANEL DESIGN =================
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

    // ================= STATUS BAR DESIGN =================
    private void createStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(248, 249, 250));
        statusBar.setPreferredSize(new Dimension(getWidth(), 35));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JLabel status = new JLabel("  System Status: ● Connected | Last updated: " + 
            new SimpleDateFormat("HH:mm:ss").format(new Date()));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_GRAY);
        
        Timer statusTimer = new Timer(1000, e -> 
            status.setText("  System Status: ● Connected | Last updated: " + 
                new SimpleDateFormat("HH:mm:ss").format(new Date()))
        );
        statusTimer.start();
        
        statusBar.add(status, BorderLayout.WEST);

        JLabel version = new JLabel("LogiXpress Sender Portal v1.0.0  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_GRAY);
        statusBar.add(version, BorderLayout.EAST);
    }

    // ================= UTILITY METHODS =================
    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        
        // Refresh panel data if needed
        switch(panelName) {
            case "HOME":
                if (homePanel != null) homePanel.refreshData();
                break;
            case "MY_ORDERS":
                if (myOrdersPanel != null) myOrdersPanel.refreshData();
                break;
            case "PAYMENT":
                if (paymentPanel != null) paymentPanel.refreshData();
                break;
        }
    }

    private void showNotifications() {
        StringBuilder message = new StringBuilder();
        message.append("📋 Notifications for ").append(senderName).append(":\n\n");
        
        if (activeOrders > 0) {
            message.append("• ").append(activeOrders).append(" active orders\n");
        }
        if (pendingPayments > 0) {
            message.append("• ").append(pendingPayments).append(" pending payments\n");
        }
        if (deliveredOrders > 0) {
            message.append("• ").append(deliveredOrders).append(" orders delivered\n");
        }
        
        JOptionPane.showMessageDialog(this, 
            message.toString(), 
            "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                Login login = new Login();
                login.setVisible(true);
            });
        }
    }

    private ImageIcon loadLogo(String filename) {
        try {
            java.net.URL imgURL = getClass().getResource("/" + filename);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                java.io.File file = new java.io.File(filename);
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + filename);
        }
        return null;
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