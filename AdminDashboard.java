package logistics.login.admin;

import logistics.login.admin.management.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends JFrame {

    // Refined color palette
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color ORANGE_DARK = new Color(235, 120, 0);
    private final Color ORANGE_LIGHT = new Color(255, 200, 130);
    private final Color ORANGE_PALE = new Color(255, 245, 235);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    
    // 实心按钮颜色
    private final Color BUTTON_SELECTED = new Color(255, 140, 0); // 实心橘色
    private final Color BUTTON_HOVER = new Color(235, 120, 0); // 深一点的橘色用于悬停
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0); // 透明

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // 缓存面板，避免重复添加
    private Map<String, JPanel> panelCache;
    
    // Management module references
    private OrderManagement orderManagement;
    private VehicleManagement vehicleManagement;
    private DriverManagement driverManagement;
    private MaintenanceManagement maintenanceManagement;
    private ReportManagement reportManagement;

    public AdminDashboard() {
        setTitle("LogiXpress Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        panelCache = new HashMap<>();
        
        // Initialize management modules
        initializeModules();
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
        
        // Load initial data
        refreshAllModules();
    }

    private void initializeModules() {
        orderManagement = new OrderManagement();
        vehicleManagement = new VehicleManagement();
        driverManagement = new DriverManagement();
        maintenanceManagement = new MaintenanceManagement();
        reportManagement = new ReportManagement();
        
        // 缓存面板
        panelCache.put("ORDER", orderManagement.getMainPanel());
        panelCache.put("VEHICLE", vehicleManagement.getMainPanel());
        panelCache.put("DRIVER", driverManagement.getMainPanel());
        panelCache.put("MAINTENANCE", maintenanceManagement.getMainPanel());
        panelCache.put("REPORTS", reportManagement.getMainPanel());
    }

    private void refreshAllModules() {
        // Refresh data from databases/files
        orderManagement.refreshData();
        vehicleManagement.refreshData();
        driverManagement.refreshData();
        maintenanceManagement.refreshData();
        reportManagement.refreshData();
    }

    private void initUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    // ================= TOP BAR =================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ORANGE_PRIMARY);
        bar.setPreferredSize(new Dimension(getWidth(), 80));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        // Left side with logo
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        // Load and resize logo
        ImageIcon logoIcon = loadLogo("logo.jpeg");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        }

        JLabel title = new JLabel("LogiXpress Admin Console");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JLabel badge = new JLabel("ADMIN");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ORANGE_PRIMARY);
        badge.setBackground(new Color(255, 255, 255, 200));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        leftPanel.add(badge);

        bar.add(leftPanel, BorderLayout.WEST);

        // Right side with time and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setOpaque(false);


        // Update time
        Timer timer = new Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(new Date()))
        );
        timer.start();

        rightPanel.add(timePanel);

        // Refresh button
        JButton refreshBtn = createButton("Refresh", new Color(40, 167, 69));
        refreshBtn.addActionListener(e -> {
            refreshAllModules();
            refreshCurrentView();
            JOptionPane.showMessageDialog(this, "Data refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        rightPanel.add(refreshBtn);

        // Logout button
        JButton logout = createButton("Logout", new Color(220, 53, 69));
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

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ORANGE_PRIMARY);
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
        }

        JLabel logo = new JLabel("LogiXpress", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        logoPanel.add(logo, BorderLayout.CENTER);

        JLabel version = new JLabel("Enterprise v2.0.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        version.setForeground(new Color(255, 255, 255, 180));
        logoPanel.add(version, BorderLayout.SOUTH);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menu items with notification badges
        JPanel menu = new JPanel(new GridLayout(5, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        menu.add(createNavButton("Order & Delivery", "ORDER", 
            getNotificationText(orderManagement.getPendingCount()), true));
        menu.add(createNavButton("Vehicle & Logistics", "VEHICLE", 
            getNotificationText(vehicleManagement.getActiveCount()), false));
        menu.add(createNavButton("Driver Management", "DRIVER", 
            getNotificationText(driverManagement.getOnDutyCount()), false));
        menu.add(createNavButton("Maintenance", "MAINTENANCE", 
            getNotificationText(maintenanceManagement.getScheduledCount()), false));
        menu.add(createNavButton("Reports", "REPORTS", 
            "Analytics & insights", false));

        sidebar.add(menu, BorderLayout.CENTER);

        // User profile
        sidebar.add(createUserProfile(), BorderLayout.SOUTH);

        return sidebar;
    }

    private String getNotificationText(int count) {
        return count > 0 ? "● " + count + " pending" : "All good";
    }

    private JPanel createUserProfile() {
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);

        JLabel userName = new JLabel("👤 Admin User", SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userName.setForeground(Color.WHITE);
        userInfo.add(userName);

        JLabel userRole = new JLabel("System Administrator", SwingConstants.CENTER);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(new Color(255, 255, 255, 200));
        userInfo.add(userRole);

        profile.add(userInfo, BorderLayout.CENTER);

        return profile;
    }

    // ================= BUTTONS =================
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
        
        // 悬停效果
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

    private JButton createNavButton(String text, String card, String notification, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        
        String displayText = "<html><div style='text-align: left;'>" +
                            "<b style='font-size: 13px;'>" + text + "</b><br>" +
                            "<span style='font-size: 11px; color: " + (selected ? "#FFFFFF" : "#E0E0E0") + ";'>" + 
                            notification + "</span></div></html>";
        
        JLabel contentLabel = new JLabel(displayText);
        contentLabel.setForeground(Color.WHITE);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        
        btn.add(contentLabel, BorderLayout.CENTER);
        
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        
        // 实心按钮设置
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

        // 悬停效果
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
            
            // 只切换面板，不重新添加
            cardLayout.show(contentPanel, card);
        });

        return btn;
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

        // 只添加一次面板
        for (Map.Entry<String, JPanel> entry : panelCache.entrySet()) {
            contentPanel.add(entry.getValue(), entry.getKey());
        }

        // 默认显示ORDER
        cardLayout.show(contentPanel, "ORDER");

        return contentPanel;
    }

    private void refreshCurrentView() {
        // 刷新当前视图的数据
        String currentCard = getCurrentCard();
        if (currentCard != null) {
            switch(currentCard) {
                case "ORDER":
                    orderManagement.refreshData();
                    break;
                case "VEHICLE":
                    vehicleManagement.refreshData();
                    break;
                case "DRIVER":
                    driverManagement.refreshData();
                    break;
                case "MAINTENANCE":
                    maintenanceManagement.refreshData();
                    break;
                case "REPORTS":
                    reportManagement.refreshData();
                    break;
            }
        }
    }

    private String getCurrentCard() {
        if (activeButton != null) {
            JLabel label = (JLabel) activeButton.getComponent(0);
            String text = label.getText();
            if (text.contains("Order")) return "ORDER";
            if (text.contains("Vehicle")) return "VEHICLE";
            if (text.contains("Driver")) return "DRIVER";
            if (text.contains("Maintenance")) return "MAINTENANCE";
            if (text.contains("Reports")) return "REPORTS";
        }
        return "ORDER";
    }

    // ================= STATUS BAR =================
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(248, 249, 250));
        bar.setPreferredSize(new Dimension(getWidth(), 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        // Live statistics
        JLabel status = new JLabel();
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_GRAY);
        
        // Update status every 30 seconds
        Timer statusTimer = new Timer(30000, e -> 
            status.setText(String.format("  System Status: ● Online | Orders: %d | Vehicles: %d | Drivers: %d | Last sync: Just now",
                orderManagement.getTotalCount(),
                vehicleManagement.getTotalCount(),
                driverManagement.getTotalCount()))
        );
        statusTimer.setInitialDelay(0);
        statusTimer.start();
        
        bar.add(status, BorderLayout.WEST);

        JLabel version = new JLabel("LogiXpress Enterprise v2.0.0  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_GRAY);
        bar.add(version, BorderLayout.EAST);

        return bar;
    }
}