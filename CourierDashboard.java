package courier;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;
import logistics.orders.Order;
import logistics.orders.OrderStorage;
import courier.ProfilePanel;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CourierDashboard extends JFrame {

    // --- Refined Color Palette ---
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_DARK = new Color(27, 94, 32);
    private final Color GREEN_LIGHT = new Color(34, 139, 34);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    
    // Modern UI Colors
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(225, 173, 1);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color PURPLE = new Color(111, 66, 193);
    private static final Color ORANGE = new Color(255, 87, 34);
    
    // Status colors
    private static final Color STATUS_PENDING = new Color(255, 193, 7);
    private static final Color STATUS_TRANSIT = new Color(23, 162, 184);
    private static final Color STATUS_DELIVERED = new Color(40, 167, 69);
    private static final Color STATUS_DELAYED = new Color(220, 53, 69);
    
    // Button colors
    private final Color BUTTON_SELECTED = new Color(27, 94, 32);
    private final Color BUTTON_HOVER = new Color(35, 110, 40);
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0);
    
    // Delivery Management components
    private JTable deliveriesTable;
    private DefaultTableModel deliveriesTableModel;
    private TableRowSorter<DefaultTableModel> deliveriesRowSorter;
    private JPanel[] statCards = new JPanel[5];
    private JLabel[] statValues = new JLabel[5];
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;
    private JTextField searchField;
    private JComboBox<String> searchColumnCombo;
    private static final Color ACTIVE_FILTER_BORDER = new Color(46, 125, 50);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Complete Delivery Components
    private JComboBox<String> orderSelectCombo;
    private JTextField distanceField;
    private JTextField fuelField;
    private JTextArea signatureArea;
    private JCheckBox onTimeCheck;
    private JLabel photoFileNameLabel;
    private File deliveryPhotoFile;
    private JLabel orderDetailsLabel;
    
    // Data
    private Driver currentDriver;
    private DriverStorage driverStorage;
    private OrderStorage orderStorage;
    private List<Order> myOrders;
    
    // Profile Panel
    private ProfilePanel profilePanel;
    
    // Vehicle Report
    private VehicleReport vehicleReport;
    
    // Date formatters
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public CourierDashboard(Driver driver) {
        // Reload latest driver data from file
        this.driverStorage = new DriverStorage();
        this.orderStorage = new OrderStorage();
        this.currentDriver = driverStorage.findDriver(driver.id);
        
        // If not found (maybe deleted), use the passed object
        if (this.currentDriver == null) {
            this.currentDriver = driver;
        }
        
        // Load my orders
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
        myOrders = orderStorage.getOrdersByDriver(currentDriver.id);
        // Sort by status: In Transit > Delayed > Pending > Delivered
        myOrders.sort((a, b) -> {
            int scoreA = getStatusScore(a.status);
            int scoreB = getStatusScore(b.status);
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            return b.orderDate.compareTo(a.orderDate);
        });
    }
    
    private int getStatusScore(String status) {
        switch(status) {
            case "In Transit": return 4;
            case "Delayed": return 3;
            case "Pending": return 2;
            case "Delivered": return 1;
            default: return 0;
        }
    }
    
    private void refreshData() {
        // Reload driver data
        currentDriver = driverStorage.findDriver(currentDriver.id);
        if (currentDriver == null) return;
        
        // Reload orders
        loadMyOrders();
        
        // Refresh delivery table
        refreshDeliveriesTable();
        
        // Refresh complete delivery order list
        refreshOrderSelectCombo();
        
        // Update sidebar
        updateUserProfile();
        
        // Update profile panel if visible
        if (profilePanel != null) {
            profilePanel.refreshProfile(currentDriver);
        }
    }
    
    private void refreshOrderSelectCombo() {
        if (orderSelectCombo != null) {
            orderSelectCombo.removeAllItems();
            List<Order> inTransitOrders = myOrders.stream()
                .filter(o -> "In Transit".equals(o.status))
                .collect(Collectors.toList());
            
            if (inTransitOrders.isEmpty()) {
                orderSelectCombo.addItem("No orders available");
            } else {
                for (Order o : inTransitOrders) {
                    orderSelectCombo.addItem(o.id + " - " + o.recipientName);
                }
            }
        }
    }
    
    private void startAutoRefresh() {
        Timer timer = new Timer(30000, e -> {
            SwingUtilities.invokeLater(this::refreshData);
        });
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
                    if (userInfo.getComponentCount() >= 2) {
                        Component comp1 = userInfo.getComponent(0);
                        Component comp2 = userInfo.getComponent(1);
                        if (comp1 instanceof JLabel && comp2 instanceof JLabel) {
                            JLabel nameLabel = (JLabel) comp1;
                            JLabel roleLabel = (JLabel) comp2;
                            
                            nameLabel.setText(currentDriver.name);
                            roleLabel.setText("Courier #" + currentDriver.id + " | " + currentDriver.getCurrentOrderCount() + " active orders");
                            break;
                        }
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

        JPanel menu = new JPanel(new GridLayout(5, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Create buttons
        JButton myDeliveriesBtn = createNavButton("📦 My Deliveries", "DELIVERIES", 
            currentDriver.getCurrentOrderCount() + " active orders", true);
        menu.add(myDeliveriesBtn);
        
        JButton completeDeliveryBtn = createNavButton("✅ Complete Delivery", "COMPLETE", 
            "Mark order as delivered", false);
        menu.add(completeDeliveryBtn);
        
        menu.add(createNavButton("🚗 Vehicle Report", "VEHICLE", 
            "Report vehicle issues", false));
        menu.add(createNavButton("📊 My Statistics", "STATS", 
            "Performance metrics", false));
        menu.add(createNavButton("👤 My Profile", "PROFILE", 
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
            
            if (!card.equals("VEHICLE") && !card.equals("PROFILE") && !card.equals("COMPLETE")) {
                refreshData();
            }
            
            // Refresh order list when opening complete delivery page
            if (card.equals("COMPLETE")) {
                refreshOrderSelectCombo();
                clearCompleteDeliveryForm();
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

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);

        contentPanel.add(createDeliveriesPanel(), "DELIVERIES");
        contentPanel.add(createCompleteDeliveryPanel(), "COMPLETE");
        contentPanel.add(vehicleReport.createVehicleReportPanel(), "VEHICLE");
        contentPanel.add(createStatsPanel(), "STATS");
        
        // Create profile panel
        profilePanel = new ProfilePanel(currentDriver);
        contentPanel.add(profilePanel, "PROFILE");

        return contentPanel;
    }
    
    // ==================== COMPLETE DELIVERY PANEL ====================
    
    private JPanel createCompleteDeliveryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("✅ Complete Delivery");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_GREEN);
        
        JLabel subtitleLabel = new JLabel("Mark an order as delivered and record delivery details");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_GRAY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 30, 30, 30)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Order Selection
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel orderLabel = new JLabel("Select Order:*");
        orderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(orderLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        orderSelectCombo = new JComboBox<>();
        orderSelectCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderSelectCombo.setBackground(Color.WHITE);
        orderSelectCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        orderSelectCombo.setPreferredSize(new Dimension(400, 40));
        orderSelectCombo.addActionListener(e -> updateOrderDetails());
        formPanel.add(orderSelectCombo, gbc);
        
        row++;
        
        // Order Details Display
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        orderDetailsLabel = new JLabel();
        orderDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderDetailsLabel.setForeground(TEXT_GRAY);
        orderDetailsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(232, 245, 233), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        orderDetailsLabel.setBackground(new Color(245, 250, 245));
        orderDetailsLabel.setOpaque(true);
        formPanel.add(orderDetailsLabel, gbc);
        
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        formPanel.add(separator, gbc);
        row++;
        
        // Distance
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel distanceLabel = new JLabel("Distance (km):*");
        distanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(distanceLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        distanceField = new JTextField();
        distanceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        distanceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        distanceField.setToolTipText("Enter the distance traveled in kilometers");
        formPanel.add(distanceField, gbc);
        
        row++;
        
        // Fuel Used
        gbc.gridx = 0; gbc.gridy = row;
        JLabel fuelLabel = new JLabel("Fuel Used (L):*");
        fuelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(fuelLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        fuelField = new JTextField();
        fuelField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fuelField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        fuelField.setToolTipText("Enter the fuel used in liters");
        formPanel.add(fuelField, gbc);
        
        row++;
        
        // On Time Delivery
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        onTimeCheck = new JCheckBox("Delivered on time");
        onTimeCheck.setSelected(true);
        onTimeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        onTimeCheck.setBackground(Color.WHITE);
        formPanel.add(onTimeCheck, gbc);
        
        row++;
        
        // Delivery Photo
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel photoLabel = new JLabel("Delivery Photo:*");
        photoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(photoLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 1;
        JButton uploadPhotoBtn = new JButton("📷 Choose Photo");
        uploadPhotoBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadPhotoBtn.setBackground(INFO);
        uploadPhotoBtn.setForeground(Color.WHITE);
        uploadPhotoBtn.setBorderPainted(false);
        uploadPhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadPhotoBtn.addActionListener(e -> selectDeliveryPhoto());
        formPanel.add(uploadPhotoBtn, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1;
        photoFileNameLabel = new JLabel("No file selected");
        photoFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        photoFileNameLabel.setForeground(TEXT_GRAY);
        formPanel.add(photoFileNameLabel, gbc);
        
        row++;
        
        // Signature
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel signatureLabel = new JLabel("Signature:*");
        signatureLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(signatureLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        signatureArea = new JTextArea(3, 20);
        signatureArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signatureArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);
        
        JScrollPane sigScroll = new JScrollPane(signatureArea);
        sigScroll.setBorder(null);
        sigScroll.setPreferredSize(new Dimension(300, 70));
        formPanel.add(sigScroll, gbc);
        
        row++;
        
        // Required fields note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel noteLabel = new JLabel("* Required fields", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(TEXT_GRAY);
        formPanel.add(noteLabel, gbc);
        
        row++;
        
        // Button Panel
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 15, 10, 15);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearBtn.setForeground(TEXT_GRAY);
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setPreferredSize(new Dimension(120, 40));
        clearBtn.addActionListener(e -> clearCompleteDeliveryForm());
        
        JButton completeBtn = new JButton("Complete Delivery");
        completeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        completeBtn.setForeground(Color.WHITE);
        completeBtn.setBackground(SUCCESS);
        completeBtn.setBorderPainted(false);
        completeBtn.setPreferredSize(new Dimension(150, 40));
        completeBtn.addActionListener(e -> completeDelivery());
        
        buttonPanel.add(clearBtn);
        buttonPanel.add(completeBtn);
        
        formPanel.add(buttonPanel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private void updateOrderDetails() {
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No orders available")) {
            orderDetailsLabel.setText("<html><b>No orders available for delivery</b><br>All orders are either delivered or pending pickup.</html>");
            return;
        }
        
        String orderId = selected.split(" - ")[0];
        Order order = orderStorage.findOrder(orderId);
        
        if (order != null) {
            String details = String.format("<html>" +
                "<b>Order ID:</b> %s<br>" +
                "<b>Recipient:</b> %s<br>" +
                "<b>Phone:</b> %s<br>" +
                "<b>Address:</b> %s<br>" +
                "<b>Weight:</b> %.1f kg<br>" +
                "<b>Est. Delivery:</b> %s</html>",
                order.id,
                order.recipientName,
                order.recipientPhone,
                order.recipientAddress.length() > 50 ? order.recipientAddress.substring(0, 47) + "..." : order.recipientAddress,
                order.weight,
                order.estimatedDelivery != null ? order.estimatedDelivery : "Not set"
            );
            orderDetailsLabel.setText(details);
        }
    }
    
    private void selectDeliveryPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Delivery Photo");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (JPG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            deliveryPhotoFile = fileChooser.getSelectedFile();
            photoFileNameLabel.setText(deliveryPhotoFile.getName());
        }
    }
    
    private void clearCompleteDeliveryForm() {
        orderSelectCombo.setSelectedIndex(0);
        distanceField.setText("");
        fuelField.setText("");
        onTimeCheck.setSelected(true);
        signatureArea.setText("");
        photoFileNameLabel.setText("No file selected");
        deliveryPhotoFile = null;
        updateOrderDetails();
    }
    
    private void completeDelivery() {
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No orders available")) {
            showNotification("Please select an order to complete", WARNING);
            return;
        }
        
        String orderId = selected.split(" - ")[0];
        String distanceText = distanceField.getText().trim();
        String fuelText = fuelField.getText().trim();
        String signature = signatureArea.getText().trim();
        boolean onTime = onTimeCheck.isSelected();
        
        // Validation
        if (distanceText.isEmpty()) {
            showNotification("Please enter distance traveled", WARNING);
            distanceField.requestFocus();
            return;
        }
        
        if (fuelText.isEmpty()) {
            showNotification("Please enter fuel used", WARNING);
            fuelField.requestFocus();
            return;
        }
        
        if (signature.isEmpty()) {
            showNotification("Please enter recipient signature", WARNING);
            signatureArea.requestFocus();
            return;
        }
        
        if (deliveryPhotoFile == null) {
            showNotification("Please upload a delivery photo", WARNING);
            return;
        }
        
        try {
            double distance = Double.parseDouble(distanceText);
            double fuel = Double.parseDouble(fuelText);
            
            if (distance <= 0) {
                showNotification("Distance must be greater than 0", WARNING);
                return;
            }
            
            if (fuel <= 0) {
                showNotification("Fuel used must be greater than 0", WARNING);
                return;
            }
            
            Order order = orderStorage.findOrder(orderId);
            if (order == null) {
                showNotification("Order not found", DANGER);
                return;
            }
            
            if (!"In Transit".equals(order.status)) {
                showNotification("Order status is '" + order.status + "'. Only 'In Transit' orders can be completed.", WARNING);
                return;
            }
            
            if (!currentDriver.id.equals(order.driverId)) {
                showNotification("This order is assigned to another driver", DANGER);
                return;
            }
            
            // Confirm completion
            int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Complete delivery for order %s?\n\n" +
                    "📦 Recipient: %s\n" +
                    "📍 Address: %s\n" +
                    "🚚 Distance: %.1f km\n" +
                    "⛽ Fuel: %.1f L\n" +
                    "✅ On Time: %s\n\n" +
                    "Signature: %s",
                    orderId, order.recipientName, 
                    order.recipientAddress.length() > 50 ? order.recipientAddress.substring(0, 47) + "..." : order.recipientAddress,
                    distance, fuel, onTime ? "Yes" : "No", signature),
                "Confirm Completion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Complete the order
                boolean success = orderStorage.completeOrder(
                    orderId, distance, fuel, 
                    deliveryPhotoFile.getAbsolutePath(), 
                    signature);
                
                if (success) {
                    // Update onTime flag
                    order.onTime = onTime;
                    orderStorage.updateOrder(order);
                    
                    // Update driver stats
                    currentDriver.completeOrder(orderId, onTime, distance, fuel);
                    driverStorage.updateDriver(currentDriver);
                    
                    showNotification("✓ Delivery completed successfully!", SUCCESS);
                    
                    // Clear the form
                    clearCompleteDeliveryForm();
                    
                    // Refresh data
                    refreshData();
                    
                    // Show success message with details
                    JOptionPane.showMessageDialog(this,
                        String.format("✅ Delivery Completed Successfully!\n\n" +
                            "Order ID: %s\n" +
                            "Recipient: %s\n" +
                            "Distance: %.1f km\n" +
                            "Fuel: %.1f L\n" +
                            "Efficiency: %.2f km/L\n" +
                            "On Time: %s\n\n" +
                            "Your performance statistics have been updated.",
                            orderId, order.recipientName, distance, fuel, 
                            distance / fuel, onTime ? "Yes" : "No"),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                } else {
                    showNotification("Failed to complete delivery. Please try again.", DANGER);
                }
            }
            
        } catch (NumberFormatException e) {
            showNotification("Please enter valid numbers for distance and fuel", WARNING);
        }
    }
    
    // ==================== DELIVERIES MANAGEMENT PANEL ====================
    
    private JPanel createDeliveriesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Top container with header and stats cards
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_LIGHT);
        topContainer.add(createDeliveriesHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createDeliveriesStatsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(createDeliveriesCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createDeliveriesButtonPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel createDeliveriesHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("My Deliveries");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY_GREEN);
        
        JLabel subtitle = new JLabel("Track and manage your assigned deliveries");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createDeliveriesStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Total Orders", "In Transit", "Pending", "Delivered", "Delayed"};
        String[] descriptions = {"All assigned", "Currently out", "Awaiting pickup", "Completed", "Late"};
        Color[] colors = {PRIMARY_GREEN, INFO, WARNING, SUCCESS, DANGER};
        Color[] bgColors = {
            new Color(245, 250, 245),
            new Color(227, 242, 253),
            new Color(255, 243, 224),
            new Color(232, 245, 233),
            new Color(255, 235, 238)
        };
        
        for (int i = 0; i < 5; i++) {
            JPanel card = createClickableStatCard(titles[i], descriptions[i], "0", colors[i], bgColors[i], i);
            statCards[i] = card;
            statsPanel.add(card);
        }
        
        return statsPanel;
    }
    
    private JPanel createClickableStatCard(String title, String description, String value, 
                                            Color color, Color bgColor, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(new Color(134, 142, 150));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);
        
        statValues[index] = valueLabel;
        
        if (index >= 1 && index <= 4) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final String filterStatus = title;
            final int cardIndex = index;
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(bgColor);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(color, 1, true),
                            BorderFactory.createEmptyBorder(7, 11, 7, 11)
                        ));
                    }
                }
                
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(Color.WHITE);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                        ));
                    }
                }
                
                public void mouseClicked(MouseEvent e) {
                    applyDeliveriesStatusFilter(filterStatus, cardIndex, color);
                }
            });
        } 
        else if (index == 0) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != index) {
                        card.setBackground(new Color(245, 250, 245));
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != index) {
                        card.setBackground(Color.WHITE);
                    }
                }
                public void mouseClicked(MouseEvent e) {
                    clearDeliveriesFilters();
                }
            });
        }
        
        return card;
    }
    
    private void resetDeliveriesCardBorders() {
        if (statCards != null) {
            for (int i = 0; i < statCards.length; i++) {
                statCards[i].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                statCards[i].setBackground(Color.WHITE);
            }
        }
    }
    
    private void applyDeliveriesStatusFilter(String status, int cardIndex, Color color) {
        resetDeliveriesCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentStatusFilter = null;
            currentFilterIndex = -1;
            deliveriesRowSorter.setRowFilter(null);
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            statCards[cardIndex].setBackground(Color.WHITE);
        } else {
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            deliveriesRowSorter.setRowFilter(RowFilter.regexFilter("^" + status + "$", 3));
        }
    }
    
    private void clearDeliveriesFilters() {
        resetDeliveriesCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        deliveriesRowSorter.setRowFilter(null);
        if (searchField != null) {
            searchField.setText("");
        }
    }
    
    private JPanel createDeliveriesCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_LIGHT);
        panel.add(createDeliveriesTablePanel(), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDeliveriesTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JPanel searchPanel = createDeliveriesSearchPanel();
        panel.add(searchPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(createDeliveriesTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(1000, 450));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDeliveriesSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        
        JLabel searchLabel = new JLabel("Search by:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        String[] columns = {"Order ID", "Recipient", "Phone", "Address"};
        searchColumnCombo = new JComboBox<>(columns);
        searchColumnCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchColumnCombo.setBackground(Color.WHITE);
        searchColumnCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        searchColumnCombo.setPreferredSize(new Dimension(120, 32));
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton clearSearchBtn = new JButton("Clear");
        clearSearchBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        clearSearchBtn.setBackground(Color.WHITE);
        clearSearchBtn.setForeground(TEXT_GRAY);
        clearSearchBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearSearchBtn.setPreferredSize(new Dimension(80, 32));
        clearSearchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText().trim();
                int columnIndex = searchColumnCombo.getSelectedIndex();
                if (text.isEmpty()) {
                    deliveriesRowSorter.setRowFilter(null);
                } else {
                    deliveriesRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
                }
            }
        });
        
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            deliveriesRowSorter.setRowFilter(null);
        });
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchColumnCombo);
        searchPanel.add(searchField);
        searchPanel.add(clearSearchBtn);
        
        return searchPanel;
    }
    
    private JTable createDeliveriesTable() {
        String[] columns = {"Order ID", "Recipient", "Phone", "Status", "Pickup Time", "Est. Delivery", "Address", "Weight"};
        deliveriesTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        deliveriesTable = new JTable(deliveriesTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        comp.setBackground(new Color(252, 252, 253));
                    } else {
                        comp.setBackground(Color.WHITE);
                    }
                } else {
                    comp.setBackground(new Color(232, 245, 233));
                }
                
                return comp;
            }
        };
        
        deliveriesTable.setRowHeight(48);
        deliveriesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deliveriesTable.setSelectionBackground(new Color(232, 245, 233));
        deliveriesTable.setSelectionForeground(new Color(33, 37, 41));
        deliveriesTable.setShowGrid(true);
        deliveriesTable.setGridColor(BORDER_COLOR);
        deliveriesTable.setIntercellSpacing(new Dimension(10, 5));
        deliveriesTable.setFillsViewportHeight(true);
        deliveriesTable.setAutoCreateRowSorter(true);
        
        JTableHeader header = deliveriesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(new Color(108, 117, 125));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN));
        
        deliveriesRowSorter = new TableRowSorter<>(deliveriesTableModel);
        deliveriesTable.setRowSorter(deliveriesRowSorter);
        
        deliveriesTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        deliveriesTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        deliveriesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        deliveriesTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        deliveriesTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        deliveriesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        deliveriesTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        deliveriesTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        
        deliveriesTable.getColumnModel().getColumn(3).setCellRenderer(new DeliveriesStatusCellRenderer());
        deliveriesTable.getColumnModel().getColumn(7).setCellRenderer(new WeightCellRenderer());
        
        deliveriesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && deliveriesTable.getSelectedRow() != -1) {
                    int row = deliveriesTable.convertRowIndexToModel(deliveriesTable.getSelectedRow());
                    if (row >= 0 && row < myOrders.size()) {
                        showEnhancedOrderDetails(myOrders.get(row));
                    }
                }
            }
        });
        
        refreshDeliveriesTable();
        return deliveriesTable;
    }
    
    private class DeliveriesStatusCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JLabel label = new JLabel();
        
        public DeliveriesStatusCellRenderer() {
            panel.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            panel.add(label);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            panel.setBackground(isSelected ? new Color(232, 245, 233) : 
                               (row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE));
            
            if (value != null) {
                String status = value.toString();
                label.setText(status);
                label.setOpaque(true);
                
                switch (status) {
                    case "In Transit":
                        label.setForeground(INFO.darker());
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(INFO, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Pending":
                        label.setForeground(WARNING.darker());
                        label.setBackground(new Color(255, 243, 224));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(WARNING, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Delivered":
                        label.setForeground(SUCCESS.darker());
                        label.setBackground(new Color(232, 245, 233));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(SUCCESS, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                    case "Delayed":
                        label.setForeground(DANGER.darker());
                        label.setBackground(new Color(255, 235, 238));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DANGER, 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)
                        ));
                        break;
                }
            }
            
            return panel;
        }
    }
    
    private class WeightCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            
            setOpaque(true);
            setBackground(isSelected ? new Color(232, 245, 233) : 
                         (row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE));
            
            return this;
        }
    }
    
    private void refreshDeliveriesTable() {
        deliveriesTableModel.setRowCount(0);
        
        for (Order o : myOrders) {
            String pickupTime = "-";
            if (o.pickupTime != null && o.pickupTime.length() >= 16) {
                pickupTime = o.pickupTime.substring(11, 16);
            } else if (o.pickupTime != null) {
                pickupTime = o.pickupTime;
            }
            
            String estDelivery = "-";
            if (o.estimatedDelivery != null && o.estimatedDelivery.length() >= 16) {
                estDelivery = o.estimatedDelivery.substring(11, 16);
            } else if (o.estimatedDelivery != null) {
                estDelivery = o.estimatedDelivery;
            }
            
            String address = o.recipientAddress;
            if (address != null && address.length() > 25) {
                address = address.substring(0, 22) + "...";
            } else if (address == null) {
                address = "-";
            }
            
            deliveriesTableModel.addRow(new Object[]{
                o.id,
                o.recipientName != null ? o.recipientName : "-",
                o.recipientPhone != null ? o.recipientPhone : "-",
                o.status != null ? o.status : "-",
                pickupTime,
                estDelivery,
                address,
                String.format("%.1f kg", o.weight)
            });
        }
        
        updateDeliveriesStats();
    }
    
    private void updateDeliveriesStats() {
        SwingUtilities.invokeLater(() -> {
            int total = myOrders.size();
            int inTransit = (int) myOrders.stream().filter(o -> "In Transit".equals(o.status)).count();
            int pending = (int) myOrders.stream().filter(o -> "Pending".equals(o.status)).count();
            int delivered = (int) myOrders.stream().filter(o -> "Delivered".equals(o.status)).count();
            int delayed = (int) myOrders.stream().filter(o -> "Delayed".equals(o.status)).count();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(total));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(inTransit));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(pending));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(delivered));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(delayed));
        });
    }
    
    private JPanel createDeliveriesButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_LIGHT);
        
        JButton viewDetailsBtn = createActionButton("View Details", INFO, this::viewSelectedOrderDetails);
        JButton trackBtn = createActionButton("Track Order", PURPLE, this::trackSelectedOrder);
        
        panel.add(viewDetailsBtn);
        panel.add(trackBtn);
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color bgColor, Runnable action) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        btn.addActionListener(e -> action.run());
        
        return btn;
    }
    
    private void viewSelectedOrderDetails() {
        int row = deliveriesTable.getSelectedRow();
        if (row == -1) {
            showNotification("Please select an order", WARNING);
            return;
        }
        
        int modelRow = deliveriesTable.convertRowIndexToModel(row);
        if (modelRow >= 0 && modelRow < myOrders.size()) {
            showEnhancedOrderDetails(myOrders.get(modelRow));
        }
    }
    
    private void trackSelectedOrder() {
        int row = deliveriesTable.getSelectedRow();
        if (row == -1) {
            showNotification("Please select an order", WARNING);
            return;
        }
        
        int modelRow = deliveriesTable.convertRowIndexToModel(row);
        if (modelRow >= 0 && modelRow < myOrders.size()) {
            showOrderTracking(myOrders.get(modelRow));
        }
    }
    
    private void showOrderTracking(Order order) {
        JDialog dialog = new JDialog(this, "Track Order - " + order.id, true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Tracking Timeline");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_GREEN);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(Color.WHITE);
        
        timelinePanel.add(createTimelineEvent("Order Created", 
            "Order placed by " + order.customerName, order.orderDate, true));
        timelinePanel.add(Box.createVerticalStrut(5));
        
        boolean paymentCompleted = "Paid".equals(order.paymentStatus);
        timelinePanel.add(createTimelineEvent("Payment", 
            paymentCompleted ? "Payment completed" : "Payment pending",
            order.paymentDate != null ? order.paymentDate : "Not paid", paymentCompleted));
        timelinePanel.add(Box.createVerticalStrut(5));
        
        boolean pickedUp = order.pickupTime != null;
        timelinePanel.add(createTimelineEvent("Pickup", 
            pickedUp ? "Picked up by courier" : "Awaiting pickup",
            pickedUp ? order.pickupTime : "Not picked up", pickedUp));
        timelinePanel.add(Box.createVerticalStrut(5));
        
        boolean inTransit = "In Transit".equals(order.status) || "Delayed".equals(order.status) || "Delivered".equals(order.status);
        timelinePanel.add(createTimelineEvent("In Transit", 
            inTransit ? "Package is on the way" : "Not in transit",
            order.estimatedDelivery != null ? "Est: " + order.estimatedDelivery : "-", inTransit));
        timelinePanel.add(Box.createVerticalStrut(5));
        
        boolean delivered = "Delivered".equals(order.status);
        String deliveryInfo = delivered ? (order.onTime ? "Delivered on time" : "Delivered late") : "Not delivered";
        timelinePanel.add(createTimelineEvent("Delivered", deliveryInfo,
            order.actualDelivery != null ? order.actualDelivery : "-", delivered));
        
        JScrollPane scrollPane = new JScrollPane(timelinePanel);
        scrollPane.setBorder(null);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY_GREEN);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(closeBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JPanel createTimelineEvent(String event, String description, String time, boolean completed) {
        JPanel eventPanel = new JPanel(new BorderLayout(10, 0));
        eventPanel.setBackground(Color.WHITE);
        eventPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setBackground(Color.WHITE);
        
        String icon = completed ? "✅ " : "⏳ ";
        JLabel eventLabel = new JLabel(icon + event);
        eventLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eventLabel.setForeground(completed ? SUCCESS : TEXT_GRAY);
        leftPanel.add(eventLabel);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_GRAY);
        leftPanel.add(descLabel);
        
        eventPanel.add(leftPanel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(TEXT_GRAY);
        eventPanel.add(timeLabel, BorderLayout.EAST);
        
        return eventPanel;
    }
    
    private void showNotification(String message, Color color) {
        JWindow notification = new JWindow();
        notification.setAlwaysOnTop(true);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        
        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        
        notification.getContentPane().add(panel);
        notification.pack();
        
        Point p = getLocationOnScreen();
        notification.setLocation(
            p.x + getWidth() - notification.getWidth() - 24,
            p.y + getHeight() - notification.getHeight() - 24
        );
        
        notification.setVisible(true);
        
        Timer timer = new Timer(3000, e -> notification.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    // ==================== ENHANCED ORDER DETAILS ====================
    
    private void showEnhancedOrderDetails(Order order) {
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
        
        JLabel statusBadge = new JLabel("  " + order.status + "  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        Color statusColor = getStatusColorForBadge(order.status);
        statusBadge.setBackground(statusColor);
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
        
        JPanel orderTab = createOrderInfoTab(order);
        tabbedPane.addTab("📋 Order Info", orderTab);
        
        JPanel customerTab = createCustomerInfoTab(order);
        tabbedPane.addTab("👥 Customer & Recipient", customerTab);
        
        JPanel packageTab = createPackageInfoTab(order);
        tabbedPane.addTab("📦 Package Details", packageTab);
        
        JPanel deliveryTab = createDeliveryInfoTab(order);
        tabbedPane.addTab("🚚 Delivery Info", deliveryTab);
        
        JPanel paymentTab = createPaymentInfoTab(order);
        tabbedPane.addTab("💰 Payment", paymentTab);
        
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
    
    private JPanel createOrderInfoTab(Order order) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        JLabel basicTitle = new JLabel("📋 BASIC INFORMATION");
        basicTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        basicTitle.setForeground(PRIMARY_GREEN);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(basicTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Order ID:", order.id, gbc, row++);
        addDetailRow(panel, "Status:", order.status, gbc, row++);
        addDetailRow(panel, "Order Date:", order.orderDate, gbc, row++);
        addDetailRow(panel, "Estimated Delivery:", order.estimatedDelivery != null ? order.estimatedDelivery : "-", gbc, row++);
        
        if ("Delivered".equals(order.status)) {
            addDetailRow(panel, "Actual Delivery:", order.actualDelivery != null ? order.actualDelivery : "-", gbc, row++);
            addDetailRow(panel, "Delivery Time:", order.deliveryTime != null ? order.deliveryTime.substring(11, 16) : "-", gbc, row++);
        }
        
        if (order.reason != null && !order.reason.isEmpty()) {
            addDetailRow(panel, "Delay/Cancel Reason:", order.reason, gbc, row++);
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
        
        JLabel senderTitle = new JLabel("📤 SENDER INFORMATION");
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
        
        JLabel recipientTitle = new JLabel("📥 RECIPIENT INFORMATION");
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
        
        JLabel packageTitle = new JLabel("📦 PACKAGE DETAILS");
        packageTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        packageTitle.setForeground(PRIMARY_GREEN);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(packageTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Weight:", String.format("%.2f kg", order.weight), gbc, row++);
        addDetailRow(panel, "Dimensions:", order.dimensions + " cm", gbc, row++);
        
        String cost = extractCost(order.notes);
        addDetailRow(panel, "Estimated Cost:", cost, gbc, row++);
        
        if (order.notes != null && !order.notes.isEmpty()) {
            addDetailRow(panel, "Full Notes:", order.notes, gbc, row++);
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
        
        JLabel deliveryTitle = new JLabel("🚚 DELIVERY INFORMATION");
        deliveryTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deliveryTitle.setForeground(INFO);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(deliveryTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Pickup Time:", order.pickupTime != null ? order.pickupTime : "-", gbc, row++);
        addDetailRow(panel, "Delivery Time:", order.deliveryTime != null ? order.deliveryTime : "-", gbc, row++);
        
        if ("Delivered".equals(order.status)) {
            addDetailRow(panel, "Distance Traveled:", String.format("%.1f km", order.distance), gbc, row++);
            addDetailRow(panel, "Fuel Used:", String.format("%.1f L", order.fuelUsed), gbc, row++);
            addDetailRow(panel, "Fuel Efficiency:", order.fuelUsed > 0 ? 
                        String.format("%.2f km/L", order.distance / order.fuelUsed) : "-", gbc, row++);
            addDetailRow(panel, "On Time Delivery:", order.onTime ? "✅ Yes" : "❌ No", gbc, row++);
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
        
        JLabel paymentTitle = new JLabel("💰 PAYMENT INFORMATION");
        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentTitle.setForeground(PURPLE);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(paymentTitle, gbc);
        
        gbc.gridwidth = 1;
        addDetailRow(panel, "Payment Status:", order.paymentStatus != null ? order.paymentStatus : "Pending", gbc, row++);
        addDetailRow(panel, "Payment Method:", order.paymentMethod != null ? order.paymentMethod : "-", gbc, row++);
        addDetailRow(panel, "Transaction ID:", order.transactionId != null ? order.transactionId : "-", gbc, row++);
        addDetailRow(panel, "Payment Date:", order.paymentDate != null ? order.paymentDate : "-", gbc, row++);
        
        String cost = extractCost(order.notes);
        addDetailRow(panel, "Order Cost:", cost, gbc, row++);
        
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
            case "In Transit": return INFO;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Cancelled": return TEXT_GRAY;
            default: return PRIMARY_GREEN;
        }
    }
    
    // ==================== STATS PANEL ====================
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel titleLabel = new JLabel("📊 Performance Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_GREEN);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel statsContainer = new JPanel(new GridBagLayout());
        statsContainer.setBackground(BG_LIGHT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        summaryPanel.setBackground(BG_LIGHT);
        
        summaryPanel.add(createDetailedStatCard("Total Deliveries", String.valueOf(currentDriver.totalDeliveries), "All time", PRIMARY_GREEN));
        summaryPanel.add(createDetailedStatCard("Rating", String.format("%.1f ⭐", currentDriver.rating), "Average", new Color(255, 193, 7)));
        summaryPanel.add(createDetailedStatCard("On-Time", String.format("%.0f%%", currentDriver.getOnTimeRate() * 100), "Rate", SUCCESS));
        summaryPanel.add(createDetailedStatCard("Late", String.valueOf(currentDriver.lateDeliveries), "Deliveries", DANGER));
        summaryPanel.add(createDetailedStatCard("Distance", String.format("%.1f km", currentDriver.totalDistance), "Total driven", INFO));
        summaryPanel.add(createDetailedStatCard("Fuel", String.format("%.1f L", currentDriver.totalFuelUsed), "Total used", PURPLE));
        summaryPanel.add(createDetailedStatCard("Efficiency", String.format("%.2f km/L", 
            currentDriver.totalFuelUsed > 0 ? currentDriver.totalDistance / currentDriver.totalFuelUsed : 0), "Average", new Color(0, 150, 136)));
        summaryPanel.add(createDetailedStatCard("Active Orders", String.valueOf(currentDriver.getCurrentOrderCount()), "Current", ORANGE));
        
        statsContainer.add(summaryPanel, gbc);
        
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)));
        
        JLabel statusTitle = new JLabel("Order Status Breakdown");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusTitle.setForeground(PRIMARY_GREEN);
        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.add(statusTitle);
        statusPanel.add(Box.createVerticalStrut(15));
        
        int inTransit = (int) myOrders.stream().filter(o -> "In Transit".equals(o.status)).count();
        int pending = (int) myOrders.stream().filter(o -> "Pending".equals(o.status)).count();
        int delayed = (int) myOrders.stream().filter(o -> "Delayed".equals(o.status)).count();
        int delivered = (int) myOrders.stream().filter(o -> "Delivered".equals(o.status)).count();
        
        statusPanel.add(createStatusBar("In Transit", inTransit, INFO));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusBar("Pending", pending, WARNING));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusBar("Delayed", delayed, DANGER));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(createStatusBar("Delivered", delivered, SUCCESS));
        
        statsContainer.add(statusPanel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(statsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailedStatCard(String title, String value, String subtitle, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(15, 15, 15, 15)));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLabel.setForeground(TEXT_GRAY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(subLabel);
        
        return card;
    }
    
    private JPanel createStatusBar(String label, int count, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel labelComp = new JLabel(label + ":");
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelComp.setForeground(TEXT_GRAY);
        labelComp.setPreferredSize(new Dimension(100, 25));
        
        JLabel countComp = new JLabel(String.valueOf(count));
        countComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        countComp.setForeground(color);
        countComp.setPreferredSize(new Dimension(50, 25));
        
        panel.add(labelComp, BorderLayout.WEST);
        panel.add(countComp, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(getWidth(), 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        String status = currentDriver != null ? 
            "Status: " + currentDriver.workStatus : "Status: Unknown";
        
        JLabel leftStatus = new JLabel("  " + status + " | Session: " + currentDriver.id);
        leftStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftStatus.setForeground(TEXT_GRAY);
        bar.add(leftStatus, BorderLayout.WEST);
        
        String orderStatus = currentDriver.getCurrentOrderCount() + " active · " + 
            myOrders.size() + " total";
        
        JLabel rightStatus = new JLabel(orderStatus + "  ");
        rightStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightStatus.setForeground(PRIMARY_GREEN);
        bar.add(rightStatus, BorderLayout.EAST);
        
        return bar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            logistics.driver.DriverStorage storage = new logistics.driver.DriverStorage();
            Driver testDriver = storage.findDriver("DRV001");
            if (testDriver != null) {
                new CourierDashboard(testDriver).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, 
                    "No test driver found. Please run the application normally.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}