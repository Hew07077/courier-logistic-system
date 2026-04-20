package admin;

import admin.management.*;
import logistics.login.Login;

import javax.swing.*;
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
    private final Color ORANGE_TOP_BAR = new Color(255, 160, 40);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    
    // Button colors
    private final Color BUTTON_SELECTED = new Color(215, 115, 0);
    private final Color BUTTON_HOVER = new Color(235, 120, 0);
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    private Map<String, JPanel> panelCache;
    
    // Management module references
    private OrderManagement orderManagement;
    private VehicleManagement vehicleManagement;
    private DriverManagement driverManagement;
    private MaintenanceManagement maintenanceManagement;

    public AdminDashboard() {
        setTitle("LogiXpress Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        panelCache = new HashMap<>();
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 先初始化模块（同步执行，确保面板可用）
        initializeModules();
        
        initUI();
        
        // 刷新数据
        refreshAllModules();
        refreshCurrentView();
    }

    private void initializeModules() {
        System.out.println("Initializing modules...");
        
        try {
            // 首先创建 DriverManagement
            driverManagement = new DriverManagement();
            System.out.println("DriverManagement initialized");
            
            // 创建 VehicleManagement
            vehicleManagement = new VehicleManagement();
            System.out.println("VehicleManagement initialized");
            
            // 创建 OrderManagement
            orderManagement = new OrderManagement(driverManagement, vehicleManagement);
            System.out.println("OrderManagement initialized");
            
            // 创建 MaintenanceManagement
            maintenanceManagement = new MaintenanceManagement();
            System.out.println("MaintenanceManagement initialized");
            
            // 设置交叉引用
            driverManagement.setVehicleManagement(vehicleManagement);
            driverManagement.setOrderManagement(orderManagement);
            
            vehicleManagement.setDriverManagement(driverManagement);
            vehicleManagement.setOrderManagement(orderManagement);
            vehicleManagement.setMaintenanceManagement(maintenanceManagement);
            
            if (maintenanceManagement != null) {
                maintenanceManagement.setVehicleManagement(vehicleManagement);
            }
            
            // 缓存面板
            panelCache.put("ORDER", orderManagement.getMainPanel());
            panelCache.put("VEHICLE", vehicleManagement.getMainPanel());
            panelCache.put("DRIVER", driverManagement.getMainPanel());
            panelCache.put("MAINTENANCE", maintenanceManagement.getMainPanel());
            
            System.out.println("All modules initialized successfully. Panel cache size: " + panelCache.size());
            
        } catch (Exception e) {
            System.err.println("Error initializing modules: " + e.getMessage());
            e.printStackTrace();
            
            // 如果出错，创建空面板作为后备
            JPanel errorPanel = createErrorPanel("Error loading module: " + e.getMessage());
            panelCache.put("ORDER", errorPanel);
            panelCache.put("VEHICLE", errorPanel);
            panelCache.put("DRIVER", errorPanel);
            panelCache.put("MAINTENANCE", errorPanel);
        }
    }
    
    private JPanel createErrorPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_LIGHT);
        JLabel errorLabel = new JLabel(message);
        errorLabel.setForeground(Color.RED);
        panel.add(errorLabel);
        return panel;
    }

    private void refreshAllModules() {
        System.out.println("Refreshing all modules...");
        try {
            if (orderManagement != null) orderManagement.refreshData();
            if (vehicleManagement != null) vehicleManagement.refreshData();
            if (driverManagement != null) driverManagement.refreshData();
            if (maintenanceManagement != null) maintenanceManagement.refreshData();
            System.out.println("All modules refreshed");
        } catch (Exception e) {
            System.err.println("Error refreshing modules: " + e.getMessage());
            e.printStackTrace();
        }
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
        bar.setBackground(ORANGE_TOP_BAR);
        bar.setPreferredSize(new Dimension(getWidth(), 80));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        ImageIcon logoIcon = loadLogo("logo.at.png");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        } else {
            JLabel logoLabel = new JLabel("LX");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
            logoLabel.setForeground(Color.WHITE);
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

        JButton refreshBtn = createButton("Refresh", new Color(40, 167, 69));
        refreshBtn.addActionListener(e -> {
            refreshAllModules();
            refreshCurrentView();
            JOptionPane.showMessageDialog(this, "Data refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        rightPanel.add(refreshBtn);

        JButton logout = createButton("Logout", new Color(220, 53, 69));
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    Login login = new Login();
                    login.setVisible(true);
                });
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

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));

        ImageIcon mainLogoIcon = loadLogo("logo.a.jpeg");
        if (mainLogoIcon != null) {
            Image img = mainLogoIcon.getImage();
            Image resizedImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoImage = new JLabel(new ImageIcon(resizedImg));
            logoImage.setHorizontalAlignment(SwingConstants.CENTER);
            logoPanel.add(logoImage, BorderLayout.NORTH);
        } else {
            JLabel logoText = new JLabel("LX", SwingConstants.CENTER);
            logoText.setFont(new Font("Segoe UI", Font.BOLD, 48));
            logoText.setForeground(Color.WHITE);
            logoPanel.add(logoText, BorderLayout.NORTH);
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

        JPanel menu = new JPanel(new GridLayout(4, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        int pendingCount = orderManagement != null ? orderManagement.getPendingCount() : 0;
        int activeCount = vehicleManagement != null ? vehicleManagement.getActiveCount() : 0;
        int onDutyCount = driverManagement != null ? driverManagement.getOnDutyCount() : 0;
        int scheduledCount = maintenanceManagement != null ? maintenanceManagement.getScheduledCount() : 0;

        JButton orderBtn = createNavButton("Order & Delivery", "ORDER", 
            pendingCount > 0 ? "● " + pendingCount + " pending" : "All good", true);
        JButton vehicleBtn = createNavButton("Vehicle & Logistics", "VEHICLE", 
            activeCount > 0 ? "● " + activeCount + " active" : "All good", false);
        JButton driverBtn = createNavButton("Driver Management", "DRIVER", 
            onDutyCount > 0 ? "● " + onDutyCount + " on duty" : "All good", false);
        JButton maintenanceBtn = createNavButton("Maintenance", "MAINTENANCE", 
            scheduledCount > 0 ? "● " + scheduledCount + " pending" : "All good", false);
        
        menu.add(orderBtn);
        menu.add(vehicleBtn);
        menu.add(driverBtn);
        menu.add(maintenanceBtn);

        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(createUserProfile(), BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createUserProfile() {
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);

        JLabel userName = new JLabel("Admin User", SwingConstants.CENTER);
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
            
            JPanel targetPanel = panelCache.get(card);
            if (targetPanel != null) {
                cardLayout.show(contentPanel, card);
                refreshCurrentView();
            } else {
                System.err.println("Panel not found for card: " + card);
                JOptionPane.showMessageDialog(AdminDashboard.this, 
                    "Module not loaded properly. Please restart the application.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return btn;
    }

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

        if (panelCache.isEmpty()) {
            System.err.println("ERROR: panelCache is empty! Creating fallback panels.");
            JPanel errorPanel = createErrorPanel("Modules failed to load. Please check console for errors.");
            contentPanel.add(errorPanel, "ORDER");
            contentPanel.add(errorPanel, "VEHICLE");
            contentPanel.add(errorPanel, "DRIVER");
            contentPanel.add(errorPanel, "MAINTENANCE");
        } else {
            for (Map.Entry<String, JPanel> entry : panelCache.entrySet()) {
                contentPanel.add(entry.getValue(), entry.getKey());
                System.out.println("Added panel for: " + entry.getKey());
            }
        }

        if (panelCache.containsKey("ORDER")) {
            cardLayout.show(contentPanel, "ORDER");
        } else {
            cardLayout.show(contentPanel, "ORDER");
        }

        return contentPanel;
    }

    private void refreshCurrentView() {
        String currentCard = getCurrentCard();
        System.out.println("Refreshing view: " + currentCard);
        
        if (currentCard != null) {
            try {
                switch(currentCard) {
                    case "ORDER":
                        if (orderManagement != null) {
                            orderManagement.refreshData();
                            System.out.println("OrderManagement refreshed");
                        }
                        break;
                    case "VEHICLE":
                        if (vehicleManagement != null) {
                            vehicleManagement.refreshData();
                            System.out.println("VehicleManagement refreshed");
                        }
                        break;
                    case "DRIVER":
                        if (driverManagement != null) {
                            driverManagement.refreshData();
                            System.out.println("DriverManagement refreshed");
                        }
                        break;
                    case "MAINTENANCE":
                        if (maintenanceManagement != null) {
                            maintenanceManagement.refreshData();
                            System.out.println("MaintenanceManagement refreshed");
                        }
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error refreshing view: " + e.getMessage());
                e.printStackTrace();
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
        }
        return "ORDER";
    }

    // ================= STATUS BAR =================
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(248, 249, 250));
        bar.setPreferredSize(new Dimension(getWidth(), 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JLabel status = new JLabel();
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_GRAY);
        
        Timer statusTimer = new Timer(30000, e -> {
            try {
                int orders = orderManagement != null ? orderManagement.getTotalCount() : 0;
                int vehicles = vehicleManagement != null ? vehicleManagement.getTotalCount() : 0;
                int drivers = driverManagement != null ? driverManagement.getTotalCount() : 0;
                status.setText(String.format("  System Status: ● Online | Orders: %d | Vehicles: %d | Drivers: %d | Last sync: Just now",
                    orders, vehicles, drivers));
            } catch (Exception ex) {
                status.setText("  System Status: ● Online | Loading stats...");
            }
        });
        statusTimer.setInitialDelay(0);
        statusTimer.start();
        
        bar.add(status, BorderLayout.WEST);

        JLabel version = new JLabel("LogiXpress Enterprise v2.0.0  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_GRAY);
        bar.add(version, BorderLayout.EAST);

        return bar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminDashboard().setVisible(true);
        });
    }
}