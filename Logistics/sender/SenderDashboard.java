package sender;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SenderDashboard extends JFrame {
    
    private String senderName;
    private String senderEmail;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Data structure to store orders
    private List<Object[]> orders = new ArrayList<>();
    
    // Theme colors - Changed to BLUE
    private final Color BLUE_PRIMARY = new Color(0, 102, 204);      // Main blue
    private final Color BLUE_DARK = new Color(0, 82, 164);         // Darker blue
    private final Color BLUE_LIGHT = new Color(200, 220, 255);     // Light blue
    private final Color BLUE_PALE = new Color(235, 245, 255);      // Pale blue
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color WHITE_PURE = new Color(255, 255, 255);
    private final Color BUTTON_GRAY = new Color(108, 117, 125);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color WARNING_ORANGE = new Color(255, 193, 7);
    private final Color INFO_CYAN = new Color(23, 162, 184);
    
    // Table colors - HIGHLIGHT COLORS (VISIBLE) for ALL tables
    private final Color TABLE_HEADER_BG = new Color(0, 82, 164);        // Dark blue header
    private final Color TABLE_HEADER_FG = Color.WHITE;                  // White header text
    private final Color TABLE_GRID_COLOR = new Color(0, 102, 204);      // BLUE grid lines
    private final Color TABLE_ROW_HIGHLIGHT = new Color(230, 245, 255); // Light blue highlight for alternating rows
    private final Color TABLE_ROW_WHITE = Color.WHITE;                  // White for alternating rows
    private final Color TABLE_TEXT_COLOR = Color.BLACK;                 // BLACK text for maximum visibility
    private final Color TABLE_SELECTED_BG = new Color(0, 102, 204);     // Blue for selected row
    private final Color TABLE_SELECTED_FG = Color.WHITE;                // White text on selected row
    
    // Status colors - More vibrant
    private final Color STATUS_PROCESSING = new Color(0, 102, 204);     // Blue
    private final Color STATUS_TRANSIT = new Color(255, 140, 0);        // Dark Orange
    private final Color STATUS_DELIVERED = new Color(0, 128, 0);        // Dark Green
    private final Color STATUS_PICKED_UP = new Color(128, 0, 128);      // Purple
    
    // Simpler hover colors - Blue variants
    private final Color HOVER_COLOR = new Color(51, 153, 255);     // Lighter blue for hover
    private final Color SELECTED_COLOR = new Color(0, 82, 164);    // Darker blue for selected
    
    // Constructor - receives sender information
    public SenderDashboard(String senderName, String senderEmail) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        
        setTitle("LogiXpress Sender Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initUI();
    }
    
    // Overloaded constructor - if no information provided, use defaults
    public SenderDashboard() {
        this("", ""); // Empty strings instead of defaults
    }
    
    private void initUI() {
        // Remove any borders from the frame's content pane
        ((JComponent) getContentPane()).setBorder(null);
        
        add(createTopBar(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        
        // Show home panel by default
        cardLayout.show(contentPanel, "HOME");
    }
    
    // ================= TOP BAR =================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BLUE_PRIMARY);
        bar.setPreferredSize(new Dimension(getWidth(), 70));
        bar.setBorder(null);

        // Left side with logo and home icon
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(null);

        // Home Icon (using Unicode character as fallback)
        JLabel homeIcon = new JLabel("🏠");
        homeIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        homeIcon.setForeground(Color.WHITE);
        homeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeIcon.setToolTipText("Go to Dashboard Home");
        homeIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (activeButton != null) {
                    activeButton.setBackground(BLUE_PRIMARY);
                    activeButton = null;
                }
                cardLayout.show(contentPanel, "HOME");
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                homeIcon.setForeground(new Color(255, 255, 255, 200));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                homeIcon.setForeground(Color.WHITE);
            }
        });
        leftPanel.add(homeIcon);

        JLabel separator = new JLabel("|");
        separator.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        separator.setForeground(new Color(255, 255, 255, 150));
        leftPanel.add(separator);

        ImageIcon logoIcon = loadLogo("logo.jpg");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        }

        JLabel title = new JLabel("LogiXpress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JLabel badge = new JLabel("SENDER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(BLUE_PRIMARY);
        badge.setBackground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        leftPanel.add(badge);

        bar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(null);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        timePanel.setOpaque(false);
        
        JLabel clockIcon = new JLabel("🕒");
        clockIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        clockIcon.setForeground(Color.WHITE);
        timePanel.add(clockIcon);
        
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        timePanel.add(timeLabel);

        Timer timer = new Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("HH:mm:ss  dd/MM/yyyy").format(new Date()))
        );
        timer.start();

        rightPanel.add(timePanel);

        JButton logout = createRectangularButton("🚪 Logout", new Color(220, 53, 69));
        logout.setPreferredSize(new Dimension(100, 35));
        
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new logistics.login.Login().setVisible(true);
            }
        });
        
        rightPanel.add(logout);

        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }
    
    // ================= FRAMELESS SIDEBAR - NO BORDERS =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BLUE_PRIMARY);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(null);

        JPanel userPanel = new JPanel();
        userPanel.setBackground(BLUE_DARK);
        userPanel.setPreferredSize(new Dimension(250, 100));
        userPanel.setLayout(new BorderLayout());
        userPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 255, 255, 30)));

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        avatarLabel.setForeground(new Color(255, 255, 255, 180));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(60, 60));
        
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);
        avatarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        
        userPanel.add(avatarPanel, BorderLayout.WEST);

        JLabel welcomeLabel = new JLabel("Welcome,");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        welcomeLabel.setForeground(new Color(255, 255, 255, 200));
        
        String displayName = (senderName == null || senderName.trim().isEmpty()) ? "Guest" : senderName;
        String displayEmail = (senderEmail == null || senderEmail.trim().isEmpty()) ? "Not provided" : senderEmail;
        
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        
        JLabel emailLabel = new JLabel(displayEmail);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        emailLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        textPanel.setOpaque(false);
        textPanel.setBorder(null);
        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);
        textPanel.add(emailLabel);
        
        userPanel.add(textPanel, BorderLayout.CENTER);
        
        sidebar.add(userPanel, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(6, 1, 0, 2));
        menu.setBackground(BLUE_PRIMARY);
        menu.setBorder(null);

        menu.add(createRectangularNavButton("🏠 Home", "HOME", false));
        menu.add(createRectangularNavButton("📦 Create Order", "CREATE_ORDER", false));
        menu.add(createRectangularNavButton("🔍 Track Order", "ORDER_TRACKING", false));
        menu.add(createRectangularNavButton("💰 Shipping Cost", "SHIPPING_COST", false));
        menu.add(createRectangularNavButton("📍 Branches", "BRANCH_LOCATOR", false));
        menu.add(createRectangularNavButton("👤 My Profile", "PROFILE", false));

        sidebar.add(menu, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new GridLayout(1, 2));
        statsPanel.setBackground(BLUE_DARK);
        statsPanel.setPreferredSize(new Dimension(250, 50));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel ordersLabel = new JLabel("📊 " + orders.size() + " Orders");
        ordersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ordersLabel.setForeground(new Color(255, 255, 255, 200));
        ordersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel ratingLabel = new JLabel("⭐ 4.8");
        ratingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ratingLabel.setForeground(new Color(255, 255, 255, 200));
        ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        statsPanel.add(ordersLabel);
        statsPanel.add(ratingLabel);
        
        sidebar.add(statsPanel, BorderLayout.SOUTH);

        return sidebar;
    }
    
    // ===== RECTANGULAR NAVIGATION BUTTON (Squared corners) =====
    private JButton createRectangularNavButton(String text, String card, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        
        btn.setMargin(new Insets(10, 20, 10, 10));
        
        if (selected) {
            btn.setBackground(SELECTED_COLOR);
            activeButton = btn;
        } else {
            btn.setBackground(BLUE_PRIMARY);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(BLUE_PRIMARY);
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(BLUE_PRIMARY);
            }
            
            btn.setBackground(SELECTED_COLOR);
            activeButton = btn;
            
            cardLayout.show(contentPanel, card);
        });

        return btn;
    }
    
    // ===== RECTANGULAR BUTTON HELPER =====
    private JButton createRectangularButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(WHITE_PURE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);
        
        button.setOpaque(true);
        
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
    
    // ================= LOGO LOADING =================
    private ImageIcon loadLogo(String filename) {
        try {
            java.net.URL imgURL = getClass().getResource(filename);
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
    
    // ================= CONTENT PANEL =================
    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(createHomePanel(), "HOME");
        contentPanel.add(createOrderPanel(), "CREATE_ORDER");
        contentPanel.add(createOrderQueryPanel(), "ORDER_TRACKING");
        contentPanel.add(createShippingCostPanel(), "SHIPPING_COST");
        contentPanel.add(createBranchQueryPanel(), "BRANCH_LOCATOR");
        contentPanel.add(createProfilePanel(), "PROFILE");

        return contentPanel;
    }
    
    // ================= STATUS BAR =================
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(248, 249, 250));
        bar.setPreferredSize(new Dimension(getWidth(), 30));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JLabel status = new JLabel("  🔵 Status: Online | Ready to process orders");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        status.setForeground(TEXT_GRAY);
        bar.add(status, BorderLayout.WEST);

        JLabel version = new JLabel("📦 v1.0  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_GRAY);
        bar.add(version, BorderLayout.EAST);

        return bar;
    }
    
    // ================= HOME PANEL =================
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_PURE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE_PURE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel welcomeTitle = new JLabel("🏠 Dashboard Home");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeTitle.setForeground(TEXT_DARK);
        headerPanel.add(welcomeTitle, BorderLayout.WEST);
        
        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_GRAY);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(WHITE_PURE);
        
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(BLUE_PALE);
        welcomePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BLUE_LIGHT, 1),
            new EmptyBorder(25, 30, 25, 30)
        ));
        
        String displayName = (senderName == null || senderName.trim().isEmpty()) ? "Guest" : senderName;
        JLabel greetingLabel = new JLabel("Welcome back, " + displayName + "! 👋");
        greetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        greetingLabel.setForeground(BLUE_DARK);
        welcomePanel.add(greetingLabel, BorderLayout.NORTH);
        
        JLabel messageLabel = new JLabel("Manage your shipments, track orders, and more from your personalized dashboard.");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_GRAY);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        welcomePanel.add(messageLabel, BorderLayout.CENTER);
        
        centerPanel.add(welcomePanel, BorderLayout.NORTH);
        
        int activeOrders = 0;
        int inTransitOrders = 0;
        int deliveredOrders = 0;
        double totalSpent = 0;
        
        for (Object[] order : orders) {
            String status = (String) order[2];
            if (status.contains("Processing") || status.contains("Picked Up")) {
                activeOrders++;
            } else if (status.contains("In Transit") || status.contains("Out for Delivery")) {
                inTransitOrders++;
            } else if (status.contains("Delivered")) {
                deliveredOrders++;
            }
        }
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(WHITE_PURE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        statsPanel.add(createColorfulStatCard("📦", "Active Orders", String.valueOf(activeOrders), 
            new Color(0, 123, 255), new Color(0, 86, 179)));
        
        statsPanel.add(createColorfulStatCard("🚚", "In Transit", String.valueOf(inTransitOrders), 
            new Color(255, 159, 64), new Color(224, 118, 29)));
        
        statsPanel.add(createColorfulStatCard("✅", "Delivered", String.valueOf(deliveredOrders), 
            new Color(40, 167, 69), new Color(28, 117, 48)));
        
        statsPanel.add(createColorfulStatCard("💰", "Total Spent", "$" + String.format("%.2f", totalSpent), 
            new Color(111, 66, 193), new Color(78, 46, 135)));
        
        centerPanel.add(statsPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBackground(WHITE_PURE);
        
        JPanel quickActionsPanel = new JPanel(new BorderLayout());
        quickActionsPanel.setBackground(WHITE_PURE);
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel quickActionsTitle = new JLabel("⚡ Quick Actions");
        quickActionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quickActionsTitle.setForeground(TEXT_DARK);
        quickActionsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        quickActionsPanel.add(quickActionsTitle, BorderLayout.NORTH);
        
        JPanel actionsGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        actionsGrid.setBackground(WHITE_PURE);
        
        actionsGrid.add(createQuickActionButton("📦 New Order", "CREATE_ORDER", BLUE_PRIMARY));
        actionsGrid.add(createQuickActionButton("🔍 Track Order", "ORDER_TRACKING", SUCCESS_GREEN));
        actionsGrid.add(createQuickActionButton("💰 Estimate Cost", "SHIPPING_COST", WARNING_ORANGE));
        actionsGrid.add(createQuickActionButton("📍 Find Branch", "BRANCH_LOCATOR", INFO_CYAN));
        actionsGrid.add(createQuickActionButton("👤 View Profile", "PROFILE", BUTTON_GRAY));
        actionsGrid.add(createQuickActionButton("📋 Order History", "ORDER_TRACKING", BLUE_DARK));
        
        quickActionsPanel.add(actionsGrid, BorderLayout.CENTER);
        
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBackground(WHITE_PURE);
        recentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel recentTitle = new JLabel("📋 Recent Orders");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentTitle.setForeground(TEXT_DARK);
        recentTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        recentPanel.add(recentTitle, BorderLayout.NORTH);
        
        JPanel ordersList = new JPanel(new GridLayout(5, 1, 0, 8));
        ordersList.setBackground(WHITE_PURE);
        
        if (orders.isEmpty()) {
            JLabel noOrdersLabel = new JLabel("No recent orders found");
            noOrdersLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noOrdersLabel.setForeground(TEXT_GRAY);
            noOrdersLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ordersList.add(noOrdersLabel);
        } else {
            int count = Math.min(5, orders.size());
            for (int i = 0; i < count; i++) {
                Object[] order = orders.get(i);
                JPanel orderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                orderRow.setBackground(WHITE_PURE);
                
                JLabel orderLabel = new JLabel(order[0] + " - " + order[1] + " - ");
                orderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                orderLabel.setForeground(TEXT_GRAY);
                
                JLabel statusLabel = new JLabel((String) order[2]);
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                String status = (String) order[2];
                if (status.contains("Delivered")) {
                    statusLabel.setForeground(STATUS_DELIVERED);
                } else if (status.contains("In Transit") || status.contains("Out for Delivery")) {
                    statusLabel.setForeground(STATUS_TRANSIT);
                } else if (status.contains("Processing")) {
                    statusLabel.setForeground(STATUS_PROCESSING);
                } else if (status.contains("Picked Up")) {
                    statusLabel.setForeground(STATUS_PICKED_UP);
                }
                
                orderRow.add(orderLabel);
                orderRow.add(statusLabel);
                ordersList.add(orderRow);
            }
        }
        
        recentPanel.add(ordersList, BorderLayout.CENTER);
        
        JButton viewAllBtn = createStyledButton("View All Orders", BUTTON_GRAY);
        viewAllBtn.setPreferredSize(new Dimension(120, 30));
        viewAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        viewAllBtn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(BLUE_PRIMARY);
            }
            Component[] components = ((JPanel)((JPanel)getContentPane().getComponent(1)).getComponent(1)).getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if (btn.getText().contains("Track Order")) {
                        btn.setBackground(SELECTED_COLOR);
                        activeButton = btn;
                        cardLayout.show(contentPanel, "ORDER_TRACKING");
                        
                        // Force refresh the table when navigating
                        JPanel trackingPanel = (JPanel) contentPanel.getComponent(2);
                        refreshOrderTable(trackingPanel);
                        break;
                    }
                }
            }
        });
        
        JPanel viewAllPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewAllPanel.setBackground(WHITE_PURE);
        viewAllPanel.add(viewAllBtn);
        recentPanel.add(viewAllPanel, BorderLayout.SOUTH);
        
        bottomPanel.add(quickActionsPanel);
        bottomPanel.add(recentPanel);
        
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createColorfulStatCard(String icon, String label, String value, Color primaryColor, Color secondaryColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, primaryColor, w, h, secondaryColor);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(primaryColor.darker(), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel(label);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(255, 255, 255, 220));
        
        textPanel.add(valueLabel);
        textPanel.add(descLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 2),
                    new EmptyBorder(14, 14, 14, 14)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(primaryColor.darker(), 1),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
        });
        
        return card;
    }
    
    private JButton createQuickActionButton(String text, String targetCard, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(WHITE_PURE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
                btn.setForeground(WHITE_PURE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(WHITE_PURE);
                btn.setForeground(TEXT_DARK);
            }
        });
        
        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(BLUE_PRIMARY);
            }
            
            Component[] components = ((JPanel)((JPanel)getContentPane().getComponent(1)).getComponent(1)).getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton sidebarBtn = (JButton) comp;
                    if (sidebarBtn.getText().contains(text.replace("New Order", "Create Order")
                                                         .replace("Estimate Cost", "Shipping Cost")
                                                         .replace("Find Branch", "Branches")
                                                         .replace("View Profile", "My Profile")
                                                         .replace("Order History", "Track Order"))) {
                        sidebarBtn.setBackground(SELECTED_COLOR);
                        activeButton = sidebarBtn;
                        break;
                    }
                }
            }
            
            cardLayout.show(contentPanel, targetCard);
            
            // If navigating to tracking panel, refresh the table
            if (targetCard.equals("ORDER_TRACKING")) {
                JPanel trackingPanel = (JPanel) contentPanel.getComponent(2);
                refreshOrderTable(trackingPanel);
            }
        });
        
        return btn;
    }
    
    // ================= EXISTING PANEL CREATION METHODS =================
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(WHITE_PURE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));
        
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
    
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return area;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_DARK);
        return label;
    }
    
    // ================= VALIDATION METHODS =================
    
    private boolean isValidPhoneNumber(String phone) {
        // Allows formats: 1234567890, 123-456-7890, (123) 456-7890, etc.
        String phoneRegex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
        return Pattern.matches(phoneRegex, phone.trim());
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailRegex, email.trim());
    }
    
    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }
    
    private boolean isValidAddress(String address) {
        return address != null && !address.trim().isEmpty() && address.trim().length() >= 5;
    }
    
    private boolean isValidPositiveNumber(String number) {
        try {
            double value = Double.parseDouble(number.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Create Order Panel - COMPACT VERSION (NO SCROLLING)
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_PURE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE_PURE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("📦 Create New Order");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Main form panel - COMPACT with smaller spacing
        JPanel formPanel = new JPanel();
        formPanel.setBackground(WHITE_PURE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Sender Information Section
        JPanel senderSection = new JPanel(new BorderLayout());
        senderSection.setBackground(WHITE_PURE);
        senderSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BLUE_PRIMARY), 
            "📤 Sender Information", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            BLUE_PRIMARY));
        
        JPanel senderGrid = new JPanel(new GridLayout(4, 2, 10, 5));
        senderGrid.setBackground(WHITE_PURE);
        senderGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Sender Name
        senderGrid.add(createStyledLabel("Sender Name:*"));
        JTextField senderNameField = createStyledTextField(20);
        senderNameField.setText("");
        senderGrid.add(senderNameField);
        
        // Sender Email
        senderGrid.add(createStyledLabel("Sender Email:*"));
        JTextField senderEmailField = createStyledTextField(20);
        senderEmailField.setText("");
        senderGrid.add(senderEmailField);
        
        // Sender Phone
        senderGrid.add(createStyledLabel("Sender Phone:*"));
        JTextField senderPhoneField = createStyledTextField(20);
        senderPhoneField.setToolTipText("Format: 1234567890 or 123-456-7890");
        senderGrid.add(senderPhoneField);
        
        // Sender Address
        senderGrid.add(createStyledLabel("Sender Address:*"));
        JTextArea senderAddressArea = new JTextArea(2, 20);
        senderAddressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        senderAddressArea.setLineWrap(true);
        senderAddressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        JScrollPane addressScroll = new JScrollPane(senderAddressArea);
        addressScroll.setPreferredSize(new Dimension(200, 50));
        senderGrid.add(addressScroll);
        
        senderSection.add(senderGrid, BorderLayout.CENTER);
        formPanel.add(senderSection);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Recipient Information Section
        JPanel receiverSection = new JPanel(new BorderLayout());
        receiverSection.setBackground(WHITE_PURE);
        receiverSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BLUE_PRIMARY), 
            "📥 Recipient Information", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            BLUE_PRIMARY));
        
        JPanel receiverGrid = new JPanel(new GridLayout(4, 2, 10, 5));
        receiverGrid.setBackground(WHITE_PURE);
        receiverGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Recipient Name
        receiverGrid.add(createStyledLabel("Recipient Name:*"));
        JTextField receiverNameField = createStyledTextField(20);
        receiverGrid.add(receiverNameField);
        
        // Recipient Phone
        receiverGrid.add(createStyledLabel("Recipient Phone:*"));
        JTextField receiverPhoneField = createStyledTextField(20);
        receiverPhoneField.setToolTipText("Format: 1234567890 or 123-456-7890");
        receiverGrid.add(receiverPhoneField);
        
        // Recipient Address
        receiverGrid.add(createStyledLabel("Recipient Address:*"));
        JTextArea receiverAddressArea = new JTextArea(2, 20);
        receiverAddressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        receiverAddressArea.setLineWrap(true);
        receiverAddressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        JScrollPane receiverScroll = new JScrollPane(receiverAddressArea);
        receiverScroll.setPreferredSize(new Dimension(200, 50));
        receiverGrid.add(receiverScroll);
        
        // Empty cell for grid alignment
        receiverGrid.add(new JLabel(""));
        receiverGrid.add(new JLabel(""));
        
        receiverSection.add(receiverGrid, BorderLayout.CENTER);
        formPanel.add(receiverSection);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Package Information Section
        JPanel packageSection = new JPanel(new BorderLayout());
        packageSection.setBackground(WHITE_PURE);
        packageSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BLUE_PRIMARY), 
            "📦 Package Information", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            BLUE_PRIMARY));
        
        JPanel packageGrid = new JPanel(new GridLayout(2, 2, 10, 5));
        packageGrid.setBackground(WHITE_PURE);
        packageGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Weight
        packageGrid.add(createStyledLabel("Weight (kg):*"));
        JTextField weightField = createStyledTextField(20);
        weightField.setToolTipText("Enter positive number (e.g., 2.5)");
        packageGrid.add(weightField);
        
        // Dimensions
        packageGrid.add(createStyledLabel("Dimensions (cm):*"));
        JPanel dimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dimPanel.setBackground(WHITE_PURE);
        
        JTextField lengthField = new JTextField(5);
        lengthField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lengthField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        lengthField.setToolTipText("Length");
        dimPanel.add(new JLabel("L:"));
        dimPanel.add(lengthField);
        
        JTextField widthField = new JTextField(5);
        widthField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        widthField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        widthField.setToolTipText("Width");
        dimPanel.add(new JLabel("W:"));
        dimPanel.add(widthField);
        
        JTextField heightField = new JTextField(5);
        heightField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        heightField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        heightField.setToolTipText("Height");
        dimPanel.add(new JLabel("H:"));
        dimPanel.add(heightField);
        
        packageGrid.add(dimPanel);
        
        packageSection.add(packageGrid, BorderLayout.CENTER);
        formPanel.add(packageSection);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Required fields note
        JLabel requiredNote = new JLabel("* Required fields");
        requiredNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        requiredNote.setForeground(TEXT_GRAY);
        requiredNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(requiredNote);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(WHITE_PURE);
        
        JButton submitBtn = createStyledButton("✅ Submit Order", BLUE_PRIMARY);
        submitBtn.setPreferredSize(new Dimension(170, 40));
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.addActionListener(e -> {
            // Collect all validation errors
            StringBuilder errors = new StringBuilder();
            
            // Validate Sender Name
            String senderName = senderNameField.getText().trim();
            if (!isValidName(senderName)) {
                errors.append("• Sender Name must be at least 2 characters\n");
            }
            
            // Validate Sender Email
            String senderEmail = senderEmailField.getText().trim();
            if (!isValidEmail(senderEmail)) {
                errors.append("• Sender Email must be a valid email address\n");
            }
            
            // Validate Sender Phone
            String senderPhone = senderPhoneField.getText().trim();
            if (!isValidPhoneNumber(senderPhone)) {
                errors.append("• Sender Phone must be a valid phone number\n");
            }
            
            // Validate Sender Address
            String senderAddress = senderAddressArea.getText().trim();
            if (!isValidAddress(senderAddress)) {
                errors.append("• Sender Address must be at least 5 characters\n");
            }
            
            // Validate Recipient Name
            String receiverName = receiverNameField.getText().trim();
            if (!isValidName(receiverName)) {
                errors.append("• Recipient Name must be at least 2 characters\n");
            }
            
            // Validate Recipient Phone
            String receiverPhone = receiverPhoneField.getText().trim();
            if (!isValidPhoneNumber(receiverPhone)) {
                errors.append("• Recipient Phone must be a valid phone number\n");
            }
            
            // Validate Recipient Address
            String receiverAddress = receiverAddressArea.getText().trim();
            if (!isValidAddress(receiverAddress)) {
                errors.append("• Recipient Address must be at least 5 characters\n");
            }
            
            // Validate Weight
            String weightText = weightField.getText().trim();
            if (!isValidPositiveNumber(weightText)) {
                errors.append("• Weight must be a positive number\n");
            }
            
            // Validate Dimensions
            String lengthText = lengthField.getText().trim();
            String widthText = widthField.getText().trim();
            String heightText = heightField.getText().trim();
            
            if (!isValidPositiveNumber(lengthText)) {
                errors.append("• Length must be a positive number\n");
            }
            if (!isValidPositiveNumber(widthText)) {
                errors.append("• Width must be a positive number\n");
            }
            if (!isValidPositiveNumber(heightText)) {
                errors.append("• Height must be a positive number\n");
            }
            
            // Check if there are any validation errors
            if (errors.length() > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Please correct the following errors:\n\n" + errors.toString(),
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse validated numbers
            double weight = Double.parseDouble(weightText);
            double length = Double.parseDouble(lengthText);
            double width = Double.parseDouble(widthText);
            double height = Double.parseDouble(heightText);
            
            // Generate unique order ID
            String orderId = "LOG" + System.currentTimeMillis();
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000));
            
            // Create order data array
            Object[] orderData = {
                orderId,                                   // [0] Order ID
                receiverName,                              // [1] Recipient Name
                "⏳ Processing",                           // [2] Status
                currentDate,                               // [3] Created Date
                estimatedDelivery,                         // [4] Estimated Delivery
                senderName,                                // [5] Sender Name
                senderEmail,                               // [6] Sender Email
                senderPhone,                               // [7] Sender Phone
                senderAddress,                             // [8] Sender Address
                receiverPhone,                             // [9] Receiver Phone
                receiverAddress,                           // [10] Receiver Address
                weight,                                    // [11] Weight
                length + "x" + width + "x" + height       // [12] Dimensions
            };
            
            // Store the order data
            storeOrder(orderData);
            
            // Show success message with option to track
            int response = JOptionPane.showConfirmDialog(this, 
                "Order created successfully!\nOrder ID: " + orderId + "\n\nWould you like to track this order now?",
                "Success", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (response == JOptionPane.YES_OPTION) {
                navigateToTracking(orderId);
            }
            
            // Clear the form
            clearOrderForm(senderNameField, senderEmailField, senderPhoneField, senderAddressArea, 
                          receiverNameField, receiverPhoneField, receiverAddressArea, weightField, 
                          lengthField, widthField, heightField);
        });
        
        JButton clearBtn = createStyledButton("🔄 Reset", BUTTON_GRAY);
        clearBtn.setPreferredSize(new Dimension(120, 40));
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearBtn.addActionListener(e -> {
            clearOrderForm(senderNameField, senderEmailField, senderPhoneField, senderAddressArea, 
                          receiverNameField, receiverPhoneField, receiverAddressArea, weightField, 
                          lengthField, widthField, heightField);
        });
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(clearBtn);
        formPanel.add(buttonPanel);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Order Tracking Panel
    private JPanel createOrderQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_PURE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE_PURE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("🔍 Order Tracking");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(WHITE_PURE);
        searchPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        searchPanel.add(createStyledLabel("Order ID:"));
        JTextField orderIdField = createStyledTextField(20);
        searchPanel.add(orderIdField);
        
        JButton searchBtn = createStyledButton("🔎 Search", BLUE_PRIMARY);
        searchBtn.setPreferredSize(new Dimension(120, 35));
        searchBtn.addActionListener(e -> {
            String searchId = orderIdField.getText().trim();
            if (!searchId.isEmpty()) {
                Object[] order = getOrderById(searchId);
                if (order != null) {
                    String message = String.format(
                        "Order Found:\n\n" +
                        "Order ID: %s\n" +
                        "Recipient: %s\n" +
                        "Status: %s\n" +
                        "Created: %s\n" +
                        "Estimated Delivery: %s\n\n" +
                        "Sender: %s\n" +
                        "Sender Email: %s\n" +
                        "Sender Phone: %s\n" +
                        "Receiver Phone: %s\n" +
                        "Weight: %.2f kg\n" +
                        "Dimensions: %s",
                        order[0], order[1], order[2], order[3], order[4],
                        order[5], order[6], order[7], order[9], order[11], order[12]
                    );
                    JOptionPane.showMessageDialog(this, message, "Order Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Order not found!", "Search Result", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        searchPanel.add(searchBtn);
        
        JButton refreshBtn = createStyledButton("🔄 Refresh", BUTTON_GRAY);
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.addActionListener(e -> {
            refreshOrderTable(panel);
        });
        searchPanel.add(refreshBtn);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Initial table display
        refreshOrderTable(panel);
        
        return panel;
    }
    
    // Shipping Cost Estimation Panel
    private JPanel createShippingCostPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_PURE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE_PURE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("💰 Shipping Cost Estimation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE_PURE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridwidth = 1;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createStyledLabel("From City:"), gbc);
        
        gbc.gridx = 1;
        JTextField fromCityField = createStyledTextField(20);
        formPanel.add(fromCityField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(createStyledLabel("To City:"), gbc);
        
        gbc.gridx = 1;
        JTextField toCityField = createStyledTextField(20);
        formPanel.add(toCityField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createStyledLabel("Weight (kg):"), gbc);
        
        gbc.gridx = 1;
        JTextField weightField = createStyledTextField(20);
        formPanel.add(weightField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createStyledLabel("Service Type:"), gbc);
        
        gbc.gridx = 1;
        String[] services = {"⚡ Standard Express", "🚀 Priority Express", "🐢 Economy"};
        JComboBox<String> serviceBox = new JComboBox<>(services);
        serviceBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceBox.setBackground(WHITE_PURE);
        serviceBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        formPanel.add(serviceBox, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(WHITE_PURE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton calculateBtn = createStyledButton("🧮 Calculate Cost", BLUE_PRIMARY);
        calculateBtn.setPreferredSize(new Dimension(170, 45));
        calculateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calculateBtn.addActionListener(e -> {
            try {
                double weight = Double.parseDouble(weightField.getText());
                if (weight <= 0) {
                    JOptionPane.showMessageDialog(panel, 
                        "Weight must be a positive number",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double cost = weight * 10 + 15;
                JOptionPane.showMessageDialog(panel, 
                    "Estimated Cost: $" + String.format("%.2f", cost),
                    "Cost Estimation Result",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, 
                    "Please enter a valid weight",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(calculateBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Branch Locator Panel - FIXED HEADER
    private JPanel createBranchQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_PURE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE_PURE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("📍 Branch Locator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(WHITE_PURE);
        searchPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        searchPanel.add(createStyledLabel("City/Area:"));
        JTextField cityField = createStyledTextField(20);
        searchPanel.add(cityField);
        
        JButton searchBtn = createStyledButton("🔎 Search", BLUE_PRIMARY);
        searchBtn.setPreferredSize(new Dimension(120, 35));
        searchBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Searching for branches near " + cityField.getText() + "...",
                "Searching",
                JOptionPane.INFORMATION_MESSAGE);
        });
        searchPanel.add(searchBtn);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Branch data
        String[] columns = {"Branch Name", "Address", "Contact", "Business Hours"};
        Object[][] data = {
            {"LogiXpress New York Hub", "123 Main St, New York, NY 10001", "+1-212-555-1234", "8:00-20:00"},
            {"LogiXpress Los Angeles Hub", "456 Oak Ave, Los Angeles, CA 90001", "+1-213-555-5678", "8:00-20:00"},
            {"LogiXpress Chicago Hub", "789 Pine Rd, Chicago, IL 60601", "+1-312-555-9012", "8:00-20:00"},
            {"LogiXpress Houston Hub", "321 Elm St, Houston, TX 77001", "+1-713-555-3456", "8:00-20:00"},
            {"LogiXpress Phoenix Hub", "654 Cedar Ln, Phoenix, AZ 85001", "+1-602-555-7890", "8:00-20:00"}
        };
        
        // Create table with highlight colors
        JTable branchTable = new JTable(data, columns);
        branchTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        branchTable.setRowHeight(35);
        branchTable.setShowGrid(true);
        branchTable.setGridColor(TABLE_GRID_COLOR);
        branchTable.setForeground(TABLE_TEXT_COLOR);
        branchTable.setBackground(TABLE_ROW_WHITE);
        
        // Custom renderer for branch table
        branchTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                label.setForeground(TABLE_TEXT_COLOR);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        label.setBackground(TABLE_ROW_HIGHLIGHT);
                    } else {
                        label.setBackground(TABLE_ROW_WHITE);
                    }
                } else {
                    label.setBackground(TABLE_SELECTED_BG);
                    label.setForeground(TABLE_SELECTED_FG);
                }
                
                label.setHorizontalAlignment(JLabel.CENTER);
                
                return label;
            }
        });
        
        // Style the table header - FIXED
        JTableHeader header = branchTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        // Force header renderer
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(TABLE_HEADER_BG);
                label.setForeground(TABLE_HEADER_FG);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(branchTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(TABLE_GRID_COLOR, 2));
        scrollPane.getViewport().setBackground(TABLE_ROW_WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Profile Panel
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_PURE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(WHITE_PURE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("👤 My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE_PURE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridwidth = 1;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createStyledLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        JTextField nameField = createStyledTextField(30);
        nameField.setText(senderName);
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(createStyledLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        JTextField emailField = createStyledTextField(30);
        emailField.setText(senderEmail);
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createStyledLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        JTextField phoneField = createStyledTextField(30);
        formPanel.add(phoneField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createStyledLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        JTextArea addressArea = createStyledTextArea(3, 30);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        formPanel.add(addressScroll, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(WHITE_PURE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton saveBtn = createStyledButton("💾 Save Changes", BLUE_PRIMARY);
        saveBtn.setPreferredSize(new Dimension(170, 45));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Profile information saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(saveBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ================= HELPER METHODS FOR ORDER MANAGEMENT =================
    
    private void storeOrder(Object[] orderData) {
        orders.add(0, orderData);
        System.out.println("Order stored: " + orderData[0]);
        updateSidebarOrderCount();
    }
    
    private void updateSidebarOrderCount() {
        Component[] components = ((JPanel)getContentPane().getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel) {
                    JLabel label = (JLabel) panel.getComponent(0);
                    if (label.getText().startsWith("📊")) {
                        label.setText("📊 " + orders.size() + " Orders");
                        break;
                    }
                }
            }
        }
    }
    
    private void clearOrderForm(JTextField senderNameField, JTextField senderEmailField,
                               JTextField senderPhoneField, JTextArea senderAddressArea,
                               JTextField receiverNameField, JTextField receiverPhoneField,
                               JTextArea receiverAddressArea, JTextField weightField,
                               JTextField lengthField, JTextField widthField, JTextField heightField) {
        senderNameField.setText("");
        senderEmailField.setText("");
        senderPhoneField.setText("");
        senderAddressArea.setText("");
        receiverNameField.setText("");
        receiverPhoneField.setText("");
        receiverAddressArea.setText("");
        weightField.setText("");
        lengthField.setText("");
        widthField.setText("");
        heightField.setText("");
    }
    
    private void navigateToTracking(String orderId) {
        Component[] components = ((JPanel)((JPanel)getContentPane().getComponent(1)).getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText().contains("Track Order")) {
                    btn.setBackground(SELECTED_COLOR);
                    activeButton = btn;
                    cardLayout.show(contentPanel, "ORDER_TRACKING");
                    
                    JPanel trackingPanel = (JPanel) contentPanel.getComponent(2);
                    refreshOrderTable(trackingPanel);
                    
                    Component[] trackingComponents = trackingPanel.getComponents();
                    for (Component trackingComp : trackingComponents) {
                        if (trackingComp instanceof JPanel) {
                            JPanel searchPanel = (JPanel) trackingComp;
                            for (Component searchComp : searchPanel.getComponents()) {
                                if (searchComp instanceof JTextField) {
                                    JTextField searchField = (JTextField) searchComp;
                                    searchField.setText(orderId);
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
    
    // COMPLETE TABLE REFRESH METHOD
    private void refreshOrderTable(JPanel panel) {
        if (orders.isEmpty()) {
            String[] columns = {"Order ID", "Recipient", "Status", "Created Date", "Estimated Delivery"};
            Object[][] emptyData = {{"No orders found", "", "", "", ""}};
            JTable emptyTable = new JTable(emptyData, columns);
            emptyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyTable.setRowHeight(35);
            emptyTable.setForeground(TABLE_TEXT_COLOR);
            emptyTable.setBackground(TABLE_ROW_WHITE);
            emptyTable.setGridColor(TABLE_GRID_COLOR);
            emptyTable.setShowGrid(true);
            
            JScrollPane scrollPane = new JScrollPane(emptyTable);
            scrollPane.setBorder(BorderFactory.createLineBorder(TABLE_GRID_COLOR, 2));
            scrollPane.getViewport().setBackground(TABLE_ROW_WHITE);
            
            Component[] components = panel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JScrollPane) {
                    panel.remove(comp);
                    break;
                }
            }
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
            return;
        }
        
        Object[][] data = new Object[orders.size()][5];
        for (int i = 0; i < orders.size(); i++) {
            Object[] order = orders.get(i);
            data[i][0] = order[0];
            data[i][1] = order[1];
            data[i][2] = order[2];
            data[i][3] = order[3];
            data[i][4] = order[4];
        }
        
        String[] columns = {"Order ID", "Recipient", "Status", "Created Date", "Estimated Delivery"};
        
        JTable orderTable = new JTable(data, columns);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderTable.setRowHeight(35);
        orderTable.setShowGrid(true);
        orderTable.setGridColor(TABLE_GRID_COLOR);
        orderTable.setForeground(TABLE_TEXT_COLOR);
        orderTable.setBackground(TABLE_ROW_WHITE);
        
        orderTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                label.setForeground(TABLE_TEXT_COLOR);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        label.setBackground(TABLE_ROW_HIGHLIGHT);
                    } else {
                        label.setBackground(TABLE_ROW_WHITE);
                    }
                } else {
                    label.setBackground(TABLE_SELECTED_BG);
                    label.setForeground(TABLE_SELECTED_FG);
                }
                
                label.setHorizontalAlignment(JLabel.CENTER);
                
                return label;
            }
        });
        
        JTableHeader header = orderTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(TABLE_GRID_COLOR, 2));
        scrollPane.getViewport().setBackground(TABLE_ROW_WHITE);
        
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                panel.remove(comp);
                break;
            }
        }
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
    
    public List<Object[]> getAllOrders() {
        return orders;
    }
    
    public Object[] getOrderById(String orderId) {
        for (Object[] order : orders) {
            if (order[0].equals(orderId)) {
                return order;
            }
        }
        return null;
    }
}