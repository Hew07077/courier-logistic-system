package logistics.login.admin;

import logistics.login.admin.management.*;
// Note: If you don't have the models package, comment out or remove this import
// import logistics.models.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminDashboard extends JFrame {

    // Refined color palette - keeping only used colors
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 220);
    private final Color TRANSPARENT_ORANGE = new Color(255, 140, 0, 180);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
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
        ImageIcon logoIcon = loadLogo("logo.jpg");
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

        // Time panel with icon
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        timePanel.setOpaque(false);
        
        JLabel clockIcon = new JLabel("🕒");
        clockIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        clockIcon.setForeground(Color.WHITE);
        timePanel.add(clockIcon);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        timePanel.add(timeLabel);

        // Update time
        Timer timer = new Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(new Date()))
        );
        timer.start();

        rightPanel.add(timePanel);

        // Refresh button
        JButton refreshBtn = createTransparentButton("🔄 Refresh", new Color(40, 167, 69));
        refreshBtn.addActionListener(e -> {
            refreshAllModules();
            refreshCurrentView();
            JOptionPane.showMessageDialog(this, "Data refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        rightPanel.add(refreshBtn);

        // Logout button
        JButton logout = createTransparentButton("Logout", new Color(220, 53, 69));
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_NO_OPTION) {
                dispose();
                // Make sure this login class exists or comment it out temporarily
                // new logistics.login.Login().setVisible(true);
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

        ImageIcon mainLogoIcon = loadLogo("logo.jpg");
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

        menu.add(createNavButton("📦 Order & Delivery", "ORDER", getNotificationText(orderManagement.getPendingCount()), true));
        menu.add(createNavButton("🚛 Vehicle & Logistics", "VEHICLE", getNotificationText(vehicleManagement.getActiveCount()), false));
        menu.add(createNavButton("👨‍✈️ Driver Management", "DRIVER", getNotificationText(driverManagement.getOnDutyCount()), false));
        menu.add(createNavButton("🔧 Maintenance", "MAINTENANCE", getNotificationText(maintenanceManagement.getScheduledCount()), false));
        menu.add(createNavButton("📊 Reports", "REPORTS", "Analytics & insights", false));

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

    // ================= TRANSPARENT BUTTONS =================
    private JButton createTransparentButton(String text, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(hoverColor);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.setBackground(new Color(0, 0, 0, 0));
                btn.repaint();
            }
        });

        return btn;
    }

    private JButton createNavButton(String text, String card, String notification, boolean selected) {
        JButton btn = new JButton("<html><center>" + text + "<br><small>" + notification + "</small></center></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(selected ? Color.WHITE : TRANSPARENT_WHITE);
        btn.setBackground(selected ? TRANSPARENT_ORANGE : new Color(0, 0, 0, 0));
        btn.setOpaque(selected);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        if (selected) activeButton = btn;

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setOpaque(true);
                    btn.setBackground(new Color(255, 255, 255, 30));
                    btn.setForeground(Color.WHITE);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setOpaque(false);
                    btn.setForeground(TRANSPARENT_WHITE);
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setOpaque(false);
                activeButton.setForeground(TRANSPARENT_WHITE);
                activeButton.repaint();
            }
            
            btn.setOpaque(true);
            btn.setBackground(TRANSPARENT_ORANGE);
            btn.setForeground(Color.WHITE);
            activeButton = btn;
            
            // Show the selected management panel
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

        // Initialize all management panels
        contentPanel.add(orderManagement.getMainPanel(), "ORDER");
        contentPanel.add(vehicleManagement.getMainPanel(), "VEHICLE");
        contentPanel.add(driverManagement.getMainPanel(), "DRIVER");
        contentPanel.add(maintenanceManagement.getMainPanel(), "MAINTENANCE");
        contentPanel.add(reportManagement.getMainPanel(), "REPORTS");

        return contentPanel;
    }

    private void refreshCurrentView() {
        // Refresh the currently visible panel
        String currentCard = getCurrentCard();
        if (currentCard != null) {
            switch(currentCard) {
                case "ORDER":
                    contentPanel.add(orderManagement.getRefreshedPanel(), "ORDER");
                    break;
                case "VEHICLE":
                    contentPanel.add(vehicleManagement.getRefreshedPanel(), "VEHICLE");
                    break;
                case "DRIVER":
                    contentPanel.add(driverManagement.getRefreshedPanel(), "DRIVER");
                    break;
                case "MAINTENANCE":
                    contentPanel.add(maintenanceManagement.getRefreshedPanel(), "MAINTENANCE");
                    break;
                case "REPORTS":
                    contentPanel.add(reportManagement.getRefreshedPanel(), "REPORTS");
                    break;
            }
            cardLayout.show(contentPanel, currentCard);
        }
    }

    private String getCurrentCard() {
        if (activeButton != null) {
            String text = activeButton.getText();
            if (text.contains("Order")) return "ORDER";
            if (text.contains("Vehicle")) return "VEHICLE";
            if (text.contains("Driver")) return "DRIVER";
            if (text.contains("Maintenance")) return "MAINTENANCE";
            if (text.contains("Reports")) return "REPORTS";
        }
        return "ORDER"; // Default
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