package courier;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;
import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.io.File;

public class CourierDashboard extends JFrame {

    // Color Palette
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_DARK = new Color(27, 94, 32);
    private final Color GREEN_LIGHT = new Color(34, 139, 34);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    
    // UI Colors
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(225, 173, 1);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color PURPLE = new Color(111, 66, 193);
    
    // Button colors
    private final Color BUTTON_SELECTED = new Color(27, 94, 32);
    private final Color BUTTON_HOVER = new Color(35, 110, 40);
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0);
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Data
    private Driver currentDriver;
    private DriverStorage driverStorage;
    private OrderStorage orderStorage;
    private List<Order> myOrders;
    private ProfilePanel profilePanel;
    private VehicleReport vehicleReport;
    
    // Separated panels
    private DeliveriesPanel deliveriesPanel;
    private CompleteDeliveryPanel completeDeliveryPanel;

    public CourierDashboard(Driver driver) {
        this.driverStorage = new DriverStorage();
        this.orderStorage = new OrderStorage();
        this.currentDriver = driverStorage.findDriver(driver.id);
        
        if (this.currentDriver == null) {
            this.currentDriver = driver;
        }
        
        loadMyOrders();
        
        setTitle("LogiXpress Courier Portal - " + currentDriver.name);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception ignored) {}

        vehicleReport = new VehicleReport(currentDriver);
        
        initUI();
        updateUserProfile();
        startAutoRefresh();
    }
    
    private void loadMyOrders() {
        orderStorage.forceReload();
        myOrders = new ArrayList<>();
        List<Order> allOrders = orderStorage.getAllOrders();
        
        for (Order order : allOrders) {
            if (order.driverId != null && order.driverId.equals(currentDriver.id)) {
                myOrders.add(order);
            } else if (order.driverId != null && order.driverId.equals(currentDriver.name)) {
                myOrders.add(order);
            }
        }
        
        if (currentDriver.currentOrderIds != null && !currentDriver.currentOrderIds.isEmpty()) {
            for (String orderId : currentDriver.currentOrderIds) {
                Order order = orderStorage.findOrder(orderId);
                if (order != null && !myOrders.contains(order)) {
                    myOrders.add(order);
                }
            }
        }
        
        myOrders.sort((a, b) -> {
            int scoreA = getStatusScore(a.status);
            int scoreB = getStatusScore(b.status);
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            return b.orderDate.compareTo(a.orderDate);
        });
    }
    
    private int getStatusScore(String status) {
        switch(status) {
            case "Assigned": return 6;
            case "Pickup": return 5;
            case "In Transit": return 4;
            case "Out for Delivery": return 3;
            case "Delayed": return 2;
            case "Pending": return 1;
            case "Delivered": return 0;
            case "Failed": return 0;
            default: return 0;
        }
    }
    
    public void refreshData() {
        orderStorage.forceReload();
        currentDriver = driverStorage.findDriver(currentDriver.id);
        if (currentDriver == null) return;
        
        loadMyOrders();
        
        if (deliveriesPanel != null) {
            deliveriesPanel.refreshData(myOrders);
        }
        if (completeDeliveryPanel != null) {
            completeDeliveryPanel.refreshData(myOrders);
            completeDeliveryPanel.clearForm();
        }
        
        updateUserProfile();
        
        if (profilePanel != null) {
            profilePanel.refreshProfile(currentDriver);
        }
        
        revalidate();
        repaint();
    }
    
    private void startAutoRefresh() {
        Timer timer = new Timer(30000, e -> SwingUtilities.invokeLater(this::refreshData));
        timer.start();
    }

    private void initUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }
    
    private void updateUserProfile() {
        Component[] components = ((JPanel)getContentPane().getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JPanel) {
                    JPanel userInfo = (JPanel) panel.getComponent(0);
                    if (userInfo.getComponentCount() >= 3) {
                        Component nameComp = userInfo.getComponent(0);
                        if (nameComp instanceof JLabel) {
                            ((JLabel) nameComp).setText(currentDriver.name);
                        }
                        Component idComp = userInfo.getComponent(1);
                        if (idComp instanceof JLabel) {
                            ((JLabel) idComp).setText("ID: " + currentDriver.id);
                        }
                        Component statusComp = userInfo.getComponent(2);
                        if (statusComp instanceof JLabel) {
                            ((JLabel) statusComp).setText("Status: " + currentDriver.workStatus);
                            ((JLabel) statusComp).setForeground(getStatusColor(currentDriver.workStatus));
                        }
                        break;
                    }
                }
            }
        }
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(GREEN_LIGHT);
        bar.setPreferredSize(new Dimension(getWidth(), 80));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        ImageIcon logoIcon = loadLogo("logo.c.png");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        }

        JLabel title = new JLabel("LogiXpress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JLabel badge = new JLabel("COURIER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(GREEN_DARK);
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

        JButton logoutBtn = createButton("Logout", new Color(220, 53, 69));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    logistics.login.Login login = new logistics.login.Login();
                    login.setVisible(true);
                });
            }
        });
        rightPanel.add(logoutBtn);

        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(PRIMARY_GREEN);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));

        ImageIcon mainLogoIcon = loadLogo("logo.c.png");
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

        JLabel version = new JLabel("Courier v2.0.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        version.setForeground(new Color(255, 255, 255, 180));
        logoPanel.add(version, BorderLayout.SOUTH);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(4, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JButton myDeliveriesBtn = createNavButton("My Deliveries", "DELIVERIES", 
            "View delivery list", false);
        menu.add(myDeliveriesBtn);
        
        JButton updateStatusBtn = createNavButton("Update Status", "UPDATE", 
            "Update order status", false);
        menu.add(updateStatusBtn);
        
        menu.add(createNavButton("Vehicle Report", "VEHICLE", 
            "Report vehicle issues", false));
        menu.add(createNavButton("My Profile", "PROFILE", 
            "Personal information", false));

        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(createUserProfile(), BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String card, String notification, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        
        String notificationColor = selected ? "#FFFFFF" : "#A0D0A0";
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
            
            if (!card.equals("VEHICLE") && !card.equals("PROFILE") && !card.equals("UPDATE")) {
                refreshData();
            }
            
            if (card.equals("UPDATE") && completeDeliveryPanel != null) {
                completeDeliveryPanel.refreshData(myOrders);
                completeDeliveryPanel.clearForm();
            }
            
            cardLayout.show(contentPanel, card);
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

    private JPanel createUserProfile() {
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(3, 1));
        userInfo.setOpaque(false);

        String displayName = currentDriver != null ? currentDriver.name : "Loading...";
        String displayId = currentDriver != null ? "ID: " + currentDriver.id : "Courier";
        String displayStatus = currentDriver != null ? "Status: " + currentDriver.workStatus : "";

        JLabel userName = new JLabel(displayName, SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userName.setForeground(Color.WHITE);
        userInfo.add(userName);

        JLabel userIdLabel = new JLabel(displayId, SwingConstants.CENTER);
        userIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userIdLabel.setForeground(new Color(255, 255, 255, 200));
        userInfo.add(userIdLabel);
        
        JLabel userStatus = new JLabel(displayStatus, SwingConstants.CENTER);
        userStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userStatus.setForeground(getStatusColor(currentDriver != null ? currentDriver.workStatus : ""));
        userInfo.add(userStatus);

        profile.add(userInfo, BorderLayout.CENTER);
        return profile;
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch(status) {
            case "Available": return SUCCESS;
            case "On Delivery": return INFO;
            case "Off Duty": return TEXT_GRAY;
            case "On Leave": return WARNING;
            default: return Color.WHITE;
        }
    }

    private ImageIcon loadLogo(String filename) {
        try {
            java.net.URL imgURL = getClass().getResource(filename);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                File file = new File(filename);
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);

        deliveriesPanel = new DeliveriesPanel(myOrders, orderStorage, this);
        completeDeliveryPanel = new CompleteDeliveryPanel(myOrders, orderStorage, this);
        
        contentPanel.add(deliveriesPanel, "DELIVERIES");
        contentPanel.add(completeDeliveryPanel, "UPDATE");
        contentPanel.add(vehicleReport.createVehicleReportPanel(), "VEHICLE");
        
        profilePanel = new ProfilePanel(currentDriver);
        contentPanel.add(profilePanel, "PROFILE");

        return contentPanel;
    }
    
    public void showEnhancedOrderDetails(Order order) {
        JDialog dialog = new JDialog(this, "Complete Order Details - " + order.id, true);
        dialog.setSize(850, 750);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Order Details: " + order.id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_GREEN);
        titlePanel.add(titleLabel);
        
        JPanel statusBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBadgePanel.setBackground(Color.WHITE);
        
        JLabel statusBadge = new JLabel("  " + order.getCourierStatus() + "  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBadge.setBackground(getStatusColorForBadge(order.getCourierStatus()));
        statusBadge.setForeground(Color.WHITE);
        statusBadgePanel.add(statusBadge);
        
        if ("Delivered".equals(order.status)) {
            JLabel onTimeBadge = new JLabel(order.onTime ? "  ON TIME  " : "  LATE  ");
            onTimeBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            onTimeBadge.setOpaque(true);
            onTimeBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            onTimeBadge.setBackground(order.onTime ? SUCCESS : DANGER);
            onTimeBadge.setForeground(Color.WHITE);
            statusBadgePanel.add(Box.createHorizontalStrut(10));
            statusBadgePanel.add(onTimeBadge);
        }
        
        titlePanel.add(statusBadgePanel);
        
        JLabel dateLabel = new JLabel("Order Date: " + order.orderDate);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_GRAY);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);
        
        tabbedPane.addTab("Order Info", createOrderInfoTab(order));
        tabbedPane.addTab("Customer & Recipient", createCustomerInfoTab(order));
        tabbedPane.addTab("Package Details", createPackageInfoTab(order));
        tabbedPane.addTab("Delivery Info", createDeliveryInfoTab(order));
        tabbedPane.addTab("Payment", createPaymentInfoTab(order));
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY_GREEN);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    public void showOrderTracking(Order order) {
        JDialog dialog = new JDialog(this, "Track Order - " + order.id, true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Order Tracking");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_GREEN);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel orderIdLabel = new JLabel("Order #" + order.id);
        orderIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderIdLabel.setForeground(TEXT_GRAY);
        headerPanel.add(orderIdLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel timelineContainer = new JPanel();
        timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        timelineContainer.setBackground(Color.WHITE);
        timelineContainer.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        List<TimelineStep> steps = buildTimelineSteps(order);
        
        for (int i = 0; i < steps.size(); i++) {
            TimelineStep step = steps.get(i);
            timelineContainer.add(createTimelineStep(step.title, step.description, step.time, step.completed));
            if (i < steps.size() - 1) {
                boolean nextCompleted = steps.get(i + 1).completed;
                timelineContainer.add(createTimelineConnectorLine(step.completed && nextCompleted));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(timelineContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(new Color(248, 249, 250));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        
        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryTitle.setForeground(PRIMARY_GREEN);
        summaryPanel.add(summaryTitle);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 10, 8));
        detailsPanel.setOpaque(false);
        
        detailsPanel.add(createSummaryDetail("Recipient:", order.recipientName != null ? order.recipientName : "-"));
        detailsPanel.add(createSummaryDetail("Status:", order.getCourierStatus()));
        detailsPanel.add(createSummaryDetail("Weight:", String.format("%.1f kg", order.weight)));
        detailsPanel.add(createSummaryDetail("Est. Cost:", extractCost(order.notes)));
        
        if (order.estimatedDelivery != null && !order.estimatedDelivery.isEmpty()) {
            detailsPanel.add(createSummaryDetail("Est. Delivery:", order.estimatedDelivery));
        }
        
        summaryPanel.add(detailsPanel);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY_GREEN);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(120, 38));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    public void showNotification(String message, Color color) {
        String title = "Notification";
        if (color == SUCCESS) title = "Success";
        else if (color == WARNING) title = "Warning";
        else if (color == DANGER) title = "Error";
        else if (color == INFO) title = "Information";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void switchToUpdatePanel(Order order) {
        cardLayout.show(contentPanel, "UPDATE");
        updateSidebarButtonHighlight("Update Status");
        
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(100, e -> {
                if (completeDeliveryPanel != null) {
                    completeDeliveryPanel.selectOrder(order);
                    showNotification("Order selected: " + order.id, SUCCESS);
                    ((Timer)e.getSource()).stop();
                }
            });
            timer.setRepeats(true);
            timer.start();
        });
    }
    
    private void updateSidebarButtonHighlight(String buttonText) {
        Component[] components = ((JPanel)getContentPane().getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel sidebar = (JPanel) comp;
                Component[] sidebarComponents = sidebar.getComponents();
                for (Component sidebarComp : sidebarComponents) {
                    if (sidebarComp instanceof JPanel) {
                        JPanel menuPanel = (JPanel) sidebarComp;
                        for (Component btnComp : menuPanel.getComponents()) {
                            if (btnComp instanceof JButton) {
                                JButton btn = (JButton) btnComp;
                                if (btn.getText().contains(buttonText)) {
                                    if (activeButton != null && activeButton != btn) {
                                        activeButton.setOpaque(false);
                                        activeButton.setBackground(BUTTON_NORMAL);
                                        activeButton.repaint();
                                    }
                                    btn.setOpaque(true);
                                    btn.setBackground(BUTTON_SELECTED);
                                    activeButton = btn;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(getWidth(), 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        String status = currentDriver != null ? "Status: " + currentDriver.workStatus : "Status: Unknown";
        JLabel leftStatus = new JLabel("  " + status + " | Session: " + currentDriver.id);
        leftStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftStatus.setForeground(TEXT_GRAY);
        bar.add(leftStatus, BorderLayout.WEST);
        
        long activeCount = myOrders.stream().filter(o -> !"Delivered".equals(o.status) && !"Failed".equals(o.status)).count();
        String orderStatus = activeCount + " active · " + myOrders.size() + " total";
        JLabel rightStatus = new JLabel(orderStatus + "  ");
        rightStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightStatus.setForeground(PRIMARY_GREEN);
        bar.add(rightStatus, BorderLayout.EAST);
        
        return bar;
    }
    
    // Helper methods for order details dialog
    private JPanel createOrderInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel basicTitle = new JLabel("BASIC INFORMATION");
        basicTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        basicTitle.setForeground(PRIMARY_GREEN);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(basicTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Order ID:", order.id, gbc, row++);
        addDetailRow(panel, "Status:", order.getCourierStatus(), gbc, row++);
        addDetailRow(panel, "Order Date:", order.orderDate, gbc, row++);
        addDetailRow(panel, "Estimated Delivery:", order.estimatedDelivery != null ? order.estimatedDelivery : "-", gbc, row++);
        
        if ("Delivered".equals(order.status)) {
            addDetailRow(panel, "Actual Delivery:", order.actualDelivery != null ? order.actualDelivery : "-", gbc, row++);
            if (order.deliveryTime != null) {
                addDetailRow(panel, "Delivery Time:", order.deliveryTime.substring(11, 16), gbc, row++);
            }
        }
        
        if ("Failed".equals(order.status)) {
            addDetailRow(panel, "Failed Date:", order.deliveryTime != null ? order.deliveryTime : order.actualDelivery, gbc, row++);
            addDetailRow(panel, "Failure Reason:", order.reason != null ? order.reason : "-", gbc, row++);
        }
        
        if (order.reason != null && !order.reason.isEmpty() && !"Failed".equals(order.status)) {
            addDetailRow(panel, "Reason:", order.reason, gbc, row++);
        }
        
        return panel;
    }
    
    private JPanel createCustomerInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel senderTitle = new JLabel("SENDER INFORMATION");
        senderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        senderTitle.setForeground(PRIMARY_GREEN);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(senderTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Name:", order.customerName, gbc, row++);
        addDetailRow(panel, "Phone:", order.customerPhone, gbc, row++);
        addDetailRow(panel, "Email:", order.customerEmail, gbc, row++);
        addDetailRow(panel, "Address:", order.customerAddress, gbc, row++);
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        JLabel recipientTitle = new JLabel("RECIPIENT INFORMATION");
        recipientTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipientTitle.setForeground(SUCCESS);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(recipientTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Name:", order.recipientName, gbc, row++);
        addDetailRow(panel, "Phone:", order.recipientPhone, gbc, row++);
        addDetailRow(panel, "Address:", order.recipientAddress, gbc, row++);
        
        return panel;
    }
    
    private JPanel createPackageInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel packageTitle = new JLabel("PACKAGE DETAILS");
        packageTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        packageTitle.setForeground(PRIMARY_GREEN);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(packageTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Weight:", String.format("%.2f kg", order.weight), gbc, row++);
        addDetailRow(panel, "Dimensions:", order.dimensions + " cm", gbc, row++);
        addDetailRow(panel, "Estimated Cost:", extractCost(order.notes), gbc, row++);
        
        if (order.notes != null && !order.notes.isEmpty()) {
            addDetailRow(panel, "Notes:", order.notes.length() > 100 ? order.notes.substring(0, 97) + "..." : order.notes, gbc, row++);
        }
        
        return panel;
    }
    
    private JPanel createDeliveryInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel deliveryTitle = new JLabel("DELIVERY INFORMATION");
        deliveryTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deliveryTitle.setForeground(INFO);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(deliveryTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Pickup Time:", order.pickupTime != null ? order.pickupTime : "-", gbc, row++);
        addDetailRow(panel, "Delivery Time:", order.deliveryTime != null ? order.deliveryTime : "-", gbc, row++);
        
        if ("Delivered".equals(order.status)) {
            addDetailRow(panel, "On Time Delivery:", order.onTime ? "Yes" : "No", gbc, row++);
        }
        
        if ("Failed".equals(order.status)) {
            addDetailRow(panel, "Delivery Status:", "FAILED", gbc, row++);
            addDetailRow(panel, "Failure Reason:", order.reason != null ? order.reason : "-", gbc, row++);
        }
        
        addDetailRow(panel, "Driver ID:", order.driverId != null ? order.driverId : "-", gbc, row++);
        addDetailRow(panel, "Vehicle ID:", order.vehicleId != null ? order.vehicleId : "-", gbc, row++);
        
        return panel;
    }
    
    private JPanel createPaymentInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel paymentTitle = new JLabel("PAYMENT INFORMATION");
        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentTitle.setForeground(PURPLE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(paymentTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Payment Status:", order.paymentStatus != null ? order.paymentStatus : "Pending", gbc, row++);
        addDetailRow(panel, "Payment Method:", order.paymentMethod != null ? order.paymentMethod : "-", gbc, row++);
        addDetailRow(panel, "Transaction ID:", order.transactionId != null ? order.transactionId : "-", gbc, row++);
        addDetailRow(panel, "Payment Date:", order.paymentDate != null ? order.paymentDate : "-", gbc, row++);
        addDetailRow(panel, "Order Cost:", extractCost(order.notes), gbc, row++);
        
        return panel;
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
    
    private String extractCost(String notes) {
        if (notes == null) return "RM --.--";
        if (notes.contains("Estimated Cost:")) {
            String[] parts = notes.split("Estimated Cost: ");
            if (parts.length > 1) {
                String[] costParts = parts[1].split(" ");
                return costParts[0].trim();
            }
        }
        return "RM --.--";
    }
    
    private Color getStatusColorForBadge(String status) {
        switch (status) {
            case "Pending": return WARNING;
            case "Picked Up": return PURPLE;
            case "Out for Delivery": return new Color(0, 123, 255);
            case "In Transit": return INFO;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Failed": return DANGER;
            case "Cancelled": return TEXT_GRAY;
            default: return PRIMARY_GREEN;
        }
    }
    
    // Timeline helper methods
    private List<TimelineStep> buildTimelineSteps(Order order) {
        List<TimelineStep> steps = new ArrayList<>();
        String currentStatus = order.getCourierStatus();
        
        steps.add(new TimelineStep("Order Placed", "Order has been placed successfully", 
            order.orderDate != null ? order.orderDate : "Date not available", true));
        
        boolean paymentCompleted = "Paid".equals(order.paymentStatus);
        steps.add(new TimelineStep("Payment Confirmed", 
            paymentCompleted ? "Payment has been confirmed" : "Awaiting payment confirmation",
            order.paymentDate != null ? order.paymentDate : (paymentCompleted ? "Confirmed" : "Pending"), 
            paymentCompleted));
        
        boolean processed = !"Pending".equals(currentStatus);
        steps.add(new TimelineStep("Order Processed", 
            processed ? "Order has been processed and prepared for pickup" : "Order is being processed",
            processed ? "Processed" : "Processing", 
            processed));
        
        boolean pickedUp = "Picked Up".equals(currentStatus) || "In Transit".equals(currentStatus) || 
                           "Out for Delivery".equals(currentStatus) || "Delivered".equals(currentStatus);
        steps.add(new TimelineStep("Picked Up by Courier", 
            pickedUp ? "Package has been picked up by courier" : "Awaiting pickup by courier",
            pickedUp ? "Picked up" : "Pending", 
            pickedUp));
        
        boolean outForDelivery = "Out for Delivery".equals(currentStatus) || "In Transit".equals(currentStatus) || "Delivered".equals(currentStatus);
        steps.add(new TimelineStep("Out for Delivery", 
            outForDelivery ? "Package is out for delivery to recipient" : "Awaiting dispatch for delivery",
            outForDelivery ? "Out for delivery" : "Pending", 
            outForDelivery));
        
        boolean inTransit = "In Transit".equals(currentStatus) || "Delivered".equals(currentStatus);
        steps.add(new TimelineStep("In Transit", 
            inTransit ? "Package is on the way to destination" : "Not yet in transit",
            inTransit ? "In transit" : "Pending", 
            inTransit));
        
        boolean delivered = "Delivered".equals(currentStatus);
        String deliveryStatus = "";
        if (delivered) {
            deliveryStatus = order.onTime ? "Successfully delivered on time" : "Delivered with delay";
        } else if ("Failed".equals(currentStatus)) {
            deliveryStatus = "Delivery failed - " + (order.reason != null ? order.reason : "Reason not specified");
        } else {
            deliveryStatus = "Delivery pending";
        }
        steps.add(new TimelineStep("Delivered", deliveryStatus, 
            delivered ? (order.actualDelivery != null ? order.actualDelivery : "Completed") : "Pending", 
            delivered));
        
        if ("Failed".equals(currentStatus)) {
            steps.add(new TimelineStep("Delivery Failed", 
                "Delivery could not be completed - " + (order.reason != null ? order.reason : "Reason not specified"),
                order.deliveryTime != null ? order.deliveryTime : "Failed", 
                true));
        }
        
        return steps;
    }
    
    private JPanel createTimelineStep(String title, String description, String time, boolean completed) {
        JPanel stepPanel = new JPanel(new BorderLayout(12, 0));
        stepPanel.setBackground(Color.WHITE);
        stepPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(30, 30));
        
        JPanel dotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (completed) {
                    g2d.setColor(SUCCESS);
                    g2d.fillOval(4, 4, 12, 12);
                } else {
                    g2d.setColor(BORDER_COLOR);
                    g2d.fillOval(4, 4, 12, 12);
                }
            }
        };
        dotPanel.setPreferredSize(new Dimension(20, 20));
        dotPanel.setBackground(Color.WHITE);
        leftPanel.add(dotPanel, BorderLayout.NORTH);
        
        stepPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(completed ? SUCCESS : TEXT_GRAY);
        contentPanel.add(titleLabel);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_GRAY);
        contentPanel.add(descLabel);
        
        stepPanel.add(contentPanel, BorderLayout.CENTER);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(TEXT_GRAY);
        stepPanel.add(timeLabel, BorderLayout.EAST);
        
        return stepPanel;
    }
    
    private JPanel createTimelineConnectorLine(boolean completed) {
        JPanel connector = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(completed ? SUCCESS : BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2));
                int x = 10;
                g2d.drawLine(x, 0, x, getHeight());
            }
        };
        connector.setPreferredSize(new Dimension(20, 25));
        connector.setBackground(Color.WHITE);
        return connector;
    }
    
    private JLabel createSummaryDetail(String label, String value) {
        JLabel lbl = new JLabel(label + " " + value);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (label.contains("Status:")) {
            String status = value;
            if ("Pending".equals(status)) {
                lbl.setForeground(WARNING);
            } else if ("Delivered".equals(status)) {
                lbl.setForeground(SUCCESS);
            } else if ("Delayed".equals(status) || "Failed".equals(status)) {
                lbl.setForeground(DANGER);
            } else if ("In Transit".equals(status)) {
                lbl.setForeground(INFO);
            } else if ("Picked Up".equals(status)) {
                lbl.setForeground(PURPLE);
            } else if ("Out for Delivery".equals(status)) {
                lbl.setForeground(new Color(0, 123, 255));
            } else {
                lbl.setForeground(PRIMARY_GREEN);
            }
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        } else {
            lbl.setForeground(TEXT_GRAY);
        }
        return lbl;
    }
    
    // Inner class for timeline steps
    private class TimelineStep {
        String title;
        String description;
        String time;
        boolean completed;
        
        TimelineStep(String title, String description, String time, boolean completed) {
            this.title = title;
            this.description = description;
            this.time = time;
            this.completed = completed;
        }
    }
}