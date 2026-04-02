package courier;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;
import logistics.orders.Order;
import logistics.orders.OrderStorage;

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
    private static final Color ORANGE = new Color(255, 87, 34);
    
    // Button colors
    private final Color BUTTON_SELECTED = new Color(27, 94, 32);
    private final Color BUTTON_HOVER = new Color(35, 110, 40);
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0);
    private static final Color ACTIVE_FILTER_BORDER = new Color(46, 125, 50);
    
    // Delivery Management components
    private JTable deliveriesTable;
    private DefaultTableModel deliveriesTableModel;
    private TableRowSorter<DefaultTableModel> deliveriesRowSorter;
    private JPanel[] statCards = new JPanel[6];
    private JLabel[] statValues = new JLabel[6];
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Update Status Components
    private JComboBox<String> orderSelectCombo;
    private JComboBox<String> statusCombo;
    private JTextArea signatureArea;
    private JLabel photoFileNameLabel;
    private File deliveryPhotoFile;
    private JLabel orderDetailsLabel;
    private JPanel photoPanel;
    private JLabel photoLabel;
    
    // Data
    private Driver currentDriver;
    private DriverStorage driverStorage;
    private OrderStorage orderStorage;
    private List<Order> myOrders;
    private ProfilePanel profilePanel;
    private VehicleReport vehicleReport;

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
        myOrders = orderStorage.getOrdersByDriver(currentDriver.id);
        // Keep failed orders in the list for record
        myOrders.sort((a, b) -> {
            int scoreA = getStatusScore(a.status);
            int scoreB = getStatusScore(b.status);
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            return b.orderDate.compareTo(a.orderDate);
        });
    }
    
    private int getStatusScore(String status) {
        switch(status) {
            case "In Transit": return 5;
            case "Delayed": return 4;
            case "Pending": return 3;
            case "Picked Up": return 3;
            case "Delivered": return 2;
            case "Failed": return 1;
            default: return 0;
        }
    }
    
    private void refreshData() {
        currentDriver = driverStorage.findDriver(currentDriver.id);
        if (currentDriver == null) return;
        
        loadMyOrders();
        refreshDeliveriesTable();
        refreshOrderSelectCombo();
        updateUserProfile();
        
        if (profilePanel != null) {
            profilePanel.refreshProfile(currentDriver);
        }
    }
    
    private void refreshOrderSelectCombo() {
        if (orderSelectCombo != null) {
            orderSelectCombo.removeAllItems();
            // Exclude delivered and failed from being updated
            List<Order> activeOrders = myOrders.stream()
                .filter(o -> !"Delivered".equals(o.status) && !"Failed".equals(o.status))
                .collect(Collectors.toList());
            
            if (activeOrders.isEmpty()) {
                orderSelectCombo.addItem("No active orders");
                if (statusCombo != null) statusCombo.setEnabled(false);
            } else {
                for (Order o : activeOrders) {
                    orderSelectCombo.addItem(o.id + " - " + o.recipientName + " (" + o.getCourierStatus() + ")");
                }
                if (statusCombo != null) {
                    statusCombo.setEnabled(true);
                    updateStatusOptionsForSelectedOrder();
                }
            }
        }
    }
    
    private void updateStatusOptionsForSelectedOrder() {
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        if (statusCombo != null) {
            statusCombo.setEnabled(true);
            String orderId = selected.split(" - ")[0];
            Order order = orderStorage.findOrder(orderId);
            
            if (order != null) {
                updateStatusOptions(order.status);
            }
        }
    }
    
    private void updateStatusOptions(String currentStatus) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        switch(currentStatus) {
            case "Pending":
            case "In Transit":
                model.addElement("Picked Up");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Picked Up":
                model.addElement("In Transit");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Delayed":
                model.addElement("In Transit");
                model.addElement("Delivered");
                model.addElement("Failed");
                break;
            default:
                model.addElement("Picked Up");
                model.addElement("In Transit");
                model.addElement("Delivered");
                model.addElement("Delayed");
                model.addElement("Failed");
        }
        
        statusCombo.setModel(model);
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

        JPanel menu = new JPanel(new GridLayout(4, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JButton myDeliveriesBtn = createNavButton("My Deliveries", "DELIVERIES", 
            currentDriver.getCurrentOrderCount() + " active orders", true);
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
            
            if (card.equals("UPDATE")) {
                refreshOrderSelectCombo();
                clearUpdateStatusForm();
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
        } catch (Exception e) {
            // Silently fail
        }
        return null;
    }

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);

        contentPanel.add(createDeliveriesPanel(), "DELIVERIES");
        contentPanel.add(createUpdateStatusPanel(), "UPDATE");
        contentPanel.add(vehicleReport.createVehicleReportPanel(), "VEHICLE");
        
        profilePanel = new ProfilePanel(currentDriver);
        contentPanel.add(profilePanel, "PROFILE");

        return contentPanel;
    }
    
    private JPanel createUpdateStatusPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Update Order Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_GREEN);
        
        JLabel subtitleLabel = new JLabel("Update the status of your assigned orders");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_GRAY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
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
        orderSelectCombo.addActionListener(e -> updateOrderDetailsAndStatusOptions());
        formPanel.add(orderSelectCombo, gbc);
        row++;
        
        // Add some vertical space
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        // Order Details Display
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weighty = 0.5;
        orderDetailsLabel = new JLabel();
        orderDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderDetailsLabel.setForeground(TEXT_GRAY);
        orderDetailsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(232, 245, 233), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        orderDetailsLabel.setBackground(new Color(245, 250, 245));
        orderDetailsLabel.setOpaque(true);
        orderDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderDetailsLabel.setVerticalAlignment(SwingConstants.CENTER);
        orderDetailsLabel.setPreferredSize(new Dimension(500, 120));
        formPanel.add(orderDetailsLabel, gbc);
        row++;
        
        // Add some vertical space
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        formPanel.add(separator, gbc);
        row++;
        
        // Status Selection
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.weighty = 0;
        JLabel statusLabel = new JLabel("New Status:*");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        statusCombo = new JComboBox<>();
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        statusCombo.setPreferredSize(new Dimension(400, 40));
        statusCombo.addActionListener(e -> togglePhotoPanel());
        formPanel.add(statusCombo, gbc);
        row++;
        
        // Photo Panel
        photoLabel = new JLabel("Delivery Photo:*");
        photoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(photoLabel, gbc);
        
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        photoPanel.setBackground(Color.WHITE);
        
        JButton uploadPhotoBtn = new JButton("Choose Photo");
        uploadPhotoBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadPhotoBtn.setBackground(INFO);
        uploadPhotoBtn.setForeground(Color.WHITE);
        uploadPhotoBtn.setBorderPainted(false);
        uploadPhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadPhotoBtn.addActionListener(e -> selectDeliveryPhoto());
        
        photoFileNameLabel = new JLabel("No file selected");
        photoFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        photoFileNameLabel.setForeground(TEXT_GRAY);
        
        photoPanel.add(uploadPhotoBtn);
        photoPanel.add(photoFileNameLabel);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(photoPanel, gbc);
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
        
        // Note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel noteLabel = new JLabel("* Signature is required for all status updates. Photo is required only for 'Delivered' status.", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(TEXT_GRAY);
        formPanel.add(noteLabel, gbc);
        row++;
        
        // Buttons
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
        clearBtn.addActionListener(e -> clearUpdateStatusForm());
        
        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(SUCCESS);
        updateBtn.setBorderPainted(false);
        updateBtn.setPreferredSize(new Dimension(150, 40));
        updateBtn.addActionListener(e -> processStatusUpdate());
        
        buttonPanel.add(clearBtn);
        buttonPanel.add(updateBtn);
        formPanel.add(buttonPanel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        photoLabel.setVisible(false);
        photoPanel.setVisible(false);
        
        return mainPanel;
    }
    
    private void updateOrderDetailsAndStatusOptions() {
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            orderDetailsLabel.setText("<html><b>No active orders available</b><br>All orders have been delivered.</html>");
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        if (statusCombo != null) statusCombo.setEnabled(true);
        String orderId = selected.split(" - ")[0];
        Order order = orderStorage.findOrder(orderId);
        
        if (order != null) {
            String details = String.format("<html>" +
                "<b>Order ID:</b> %s<br>" +
                "<b>Recipient:</b> %s<br>" +
                "<b>Phone:</b> %s<br>" +
                "<b>Address:</b> %s<br>" +
                "<b>Current Status:</b> <span style='color: %s;'>%s</span></html>",
                order.id, order.recipientName, order.recipientPhone,
                order.recipientAddress.length() > 50 ? order.recipientAddress.substring(0, 47) + "..." : order.recipientAddress,
                getStatusHexColor(order.getCourierStatus()), order.getCourierStatus());
            orderDetailsLabel.setText(details);
            updateStatusOptions(order.status);
        }
    }
    
    private String getStatusHexColor(String status) {
        switch(status) {
            case "Pending": return "#E5A100";
            case "Picked Up": return "#6F42C1";
            case "In Transit": return "#17A2B8";
            case "Delayed": return "#DC3545";
            case "Delivered": return "#28A745";
            case "Failed": return "#DC3545";
            default: return "#6C757D";
        }
    }
    
    private void togglePhotoPanel() {
        if (statusCombo == null) return;
        String selectedStatus = (String) statusCombo.getSelectedItem();
        boolean isDelivered = "Delivered".equals(selectedStatus);
        
        photoLabel.setVisible(isDelivered);
        photoPanel.setVisible(isDelivered);
        
        if (!isDelivered) {
            deliveryPhotoFile = null;
            photoFileNameLabel.setText("No file selected");
            photoFileNameLabel.setForeground(TEXT_GRAY);
        }
    }
    
    private void selectDeliveryPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            deliveryPhotoFile = fileChooser.getSelectedFile();
            photoFileNameLabel.setText(deliveryPhotoFile.getName());
            photoFileNameLabel.setForeground(SUCCESS);
        }
    }
    
    private void clearUpdateStatusForm() {
        if (orderSelectCombo != null) orderSelectCombo.setSelectedIndex(0);
        if (signatureArea != null) signatureArea.setText("");
        deliveryPhotoFile = null;
        photoFileNameLabel.setText("No file selected");
        photoFileNameLabel.setForeground(TEXT_GRAY);
        photoLabel.setVisible(false);
        photoPanel.setVisible(false);
        updateOrderDetailsAndStatusOptions();
    }
    
    private void showFailedDeliveryDialog(Order order) {
        JDialog dialog = new JDialog(this, "Mark Delivery as Failed - " + order.id, true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Failed Delivery Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(DANGER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int y = 0;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel orderInfoLabel = new JLabel("<html><b>Order:</b> " + order.id + "<br><b>Recipient:</b> " + order.recipientName + "<br><b>Address:</b> " + order.recipientAddress + "</html>");
        orderInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderInfoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        orderInfoLabel.setBackground(new Color(248, 249, 250));
        orderInfoLabel.setOpaque(true);
        formPanel.add(orderInfoLabel, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel reasonLabel = new JLabel("Failure Reason:*");
        reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(reasonLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> reasonCombo = new JComboBox<>(new String[]{
            "Select reason...",
            "Recipient not available",
            "Wrong address provided",
            "Recipient refused delivery",
            "Package damaged",
            "Delivery area restricted",
            "No one to receive",
            "Other"
        });
        reasonCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(reasonCombo, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y;
        JLabel descLabel = new JLabel("Details:*");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(descLabel, gbc);
        
        gbc.gridx = 1;
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        scrollPane.setPreferredSize(new Dimension(250, 80));
        formPanel.add(scrollPane, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y;
        JLabel sigLabel = new JLabel("Signature:*");
        sigLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(sigLabel, gbc);
        
        gbc.gridx = 1;
        JTextArea failedSignatureArea = new JTextArea(2, 20);
        failedSignatureArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        failedSignatureArea.setLineWrap(true);
        failedSignatureArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane sigScroll = new JScrollPane(failedSignatureArea);
        sigScroll.setPreferredSize(new Dimension(250, 50));
        formPanel.add(sigScroll, gbc);
        y++;
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(TEXT_GRAY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton confirmBtn = new JButton("Mark as Failed");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(DANGER);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setPreferredSize(new Dimension(130, 35));
        confirmBtn.addActionListener(e -> {
            String selectedReason = (String) reasonCombo.getSelectedItem();
            String details = reasonArea.getText().trim();
            String signature = failedSignatureArea.getText().trim();
            
            if (selectedReason == null || selectedReason.equals("Select reason...")) {
                showNotification("Please select a failure reason", WARNING);
                return;
            }
            
            if (details.isEmpty()) {
                showNotification("Please provide failure details", WARNING);
                return;
            }
            
            if (signature.isEmpty()) {
                showNotification("Signature is required", WARNING);
                return;
            }
            
            String fullReason = selectedReason + ": " + details;
            
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to mark this delivery as FAILED?\n\n" +
                "Order: " + order.id + "\n" +
                "Reason: " + fullReason + "\n\n" +
                "This order will be marked as FAILED and will appear in your history.\n" +
                "The admin will be notified for reassignment.\n" +
                "This action cannot be undone.",
                "Confirm Failed Delivery",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                order.markAsFailed(fullReason);
                order.notes = (order.notes != null ? order.notes + "\n" : "") +
                    "FAILED DELIVERY - Reason: " + fullReason + 
                    " - Signature: " + signature +
                    " on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                
                orderStorage.updateOrder(order);
                showNotification("Delivery marked as FAILED. Order recorded in history.", WARNING);
                refreshData();
                clearUpdateStatusForm();
                dialog.dispose();
                
                JOptionPane.showMessageDialog(this,
                    "Delivery marked as Failed\n\n" +
                    "Order ID: " + order.id + "\n" +
                    "Reason: " + fullReason + "\n\n" +
                    "This order has been recorded as FAILED in your history.\n" +
                    "Admin will reassign it to another driver.",
                    "Failed Delivery",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void processStatusUpdate() {
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            showNotification("Please select an order to update", WARNING);
            return;
        }
        
        String orderId = selected.split(" - ")[0];
        String newStatus = (String) statusCombo.getSelectedItem();
        
        if ("Failed".equals(newStatus)) {
            Order order = orderStorage.findOrder(orderId);
            if (order != null) {
                showFailedDeliveryDialog(order);
            }
            return;
        }
        
        String signature = signatureArea.getText().trim();
        
        if (signature.isEmpty()) {
            showNotification("Recipient signature is required", WARNING);
            signatureArea.requestFocus();
            return;
        }
        
        if ("Delivered".equals(newStatus) && deliveryPhotoFile == null) {
            showNotification("Photo proof is required for delivery completion", WARNING);
            return;
        }
        
        Order order = orderStorage.findOrder(orderId);
        if (order == null) {
            showNotification("Order not found", DANGER);
            return;
        }
        
        if ("Delivered".equals(order.status)) {
            showNotification("This order has already been delivered", WARNING);
            return;
        }
        
        if ("Failed".equals(order.status)) {
            showNotification("This order has already been marked as failed. It will be reassigned by admin.", WARNING);
            return;
        }
        
        if (!isValidTransition(order.status, newStatus)) {
            showNotification("Invalid status transition from '" + order.status + "' to '" + newStatus + "'", WARNING);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Update Order Status\n\nOrder: %s\nRecipient: %s\nCurrent Status: %s\nNew Status: %s\n\nSignature: %s",
                orderId, order.recipientName, order.status, newStatus, signature),
            "Confirm Status Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success;
            
            if ("Delivered".equals(newStatus)) {
                String notesWithSignature = (order.notes != null ? order.notes + "\n" : "") + 
                    "Signature: " + signature + " (Delivered on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + ")";
                success = orderStorage.completeOrder(orderId, 0, 0, 
                    deliveryPhotoFile.getAbsolutePath(), notesWithSignature);
            } else {
                order.status = newStatus;
                order.notes = (order.notes != null ? order.notes + "\n" : "") + 
                    "Status updated to " + newStatus + " - Signature: " + signature + 
                    " on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                orderStorage.updateOrder(order);
                success = true;
            }
            
            if (success) {
                String message = String.format("Order %s status updated to: %s\nSignature recorded", 
                    orderId, newStatus);
                showNotification(message, SUCCESS);
                refreshData();
                clearUpdateStatusForm();
                
                JOptionPane.showMessageDialog(this,
                    String.format("Status Updated Successfully!\n\nOrder ID: %s\nNew Status: %s\nRecipient: %s",
                        orderId, newStatus, order.recipientName),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showNotification("Failed to update order status", DANGER);
            }
        }
    }
    
    private boolean isValidTransition(String current, String next) {
        if (current.equals(next)) return false;
        
        if ("Failed".equals(next)) {
            return !"Delivered".equals(current);
        }
        
        switch(current) {
            case "Pending":
            case "In Transit":
                return next.equals("Picked Up") || next.equals("Delayed");
            case "Picked Up":
                return next.equals("In Transit") || next.equals("Delayed");
            case "Delayed":
                return next.equals("In Transit") || next.equals("Delivered");
            default:
                return false;
        }
    }
    
    private JPanel createDeliveriesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
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
        JPanel statsPanel = new JPanel(new GridLayout(1, 6, 12, 0));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Total Orders", "In Transit", "Pending", "Delivered", "Delayed", "Failed"};
        String[] descriptions = {"All assigned", "Currently out", "Awaiting pickup", "Completed", "Late", "Failed"};
        Color[] colors = {PRIMARY_GREEN, INFO, WARNING, SUCCESS, DANGER, DANGER};
        Color[] bgColors = {new Color(245, 250, 245), new Color(227, 242, 253), 
            new Color(255, 243, 224), new Color(232, 245, 233), new Color(255, 235, 238), new Color(255, 220, 220)};
        
        statCards = new JPanel[6];
        statValues = new JLabel[6];
        
        for (int i = 0; i < 6; i++) {
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
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        
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
        
        if (index >= 1 && index <= 5) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final String filterStatus = title;
            final int cardIndex = index;
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(bgColor);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(color, 1, true),
                            BorderFactory.createEmptyBorder(7, 11, 7, 11)));
                    }
                }
                
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(Color.WHITE);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                    }
                }
                
                public void mouseClicked(MouseEvent e) {
                    applyDeliveriesStatusFilter(filterStatus, cardIndex, color);
                }
            });
        } else if (index == 0) {
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
        for (int i = 0; i < statCards.length; i++) {
            statCards[i].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            statCards[i].setBackground(Color.WHITE);
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
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            statCards[cardIndex].setBackground(Color.WHITE);
        } else {
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            statCards[cardIndex].setBackground(color.brighter());
            deliveriesRowSorter.setRowFilter(RowFilter.regexFilter("^" + status + "$", 3));
        }
    }
    
    private void clearDeliveriesFilters() {
        resetDeliveriesCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        deliveriesRowSorter.setRowFilter(null);
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
        
        JScrollPane scrollPane = new JScrollPane(createDeliveriesTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(1000, 450));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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
                    comp.setBackground(row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE);
                } else {
                    comp.setBackground(new Color(232, 245, 233));
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        };
        
        deliveriesTable.setRowHeight(48);
        deliveriesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deliveriesTable.setSelectionBackground(new Color(232, 245, 233));
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
                    case "Pending":
                        label.setForeground(new Color(150, 100, 0));
                        label.setBackground(new Color(255, 243, 224));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(225, 173, 1), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "In Transit":
                        label.setForeground(new Color(13, 110, 130));
                        label.setBackground(new Color(227, 242, 253));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(23, 162, 184), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Delivered":
                        label.setForeground(new Color(0, 100, 0));
                        label.setBackground(new Color(232, 245, 233));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(40, 167, 69), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Delayed":
                        label.setForeground(new Color(150, 20, 30));
                        label.setBackground(new Color(255, 235, 238));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(220, 53, 69), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Picked Up":
                        label.setForeground(new Color(70, 40, 120));
                        label.setBackground(new Color(240, 235, 255));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(111, 66, 193), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    case "Failed":
                        label.setForeground(new Color(150, 0, 0));
                        label.setBackground(new Color(255, 220, 220));
                        label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(220, 53, 69), 1, true),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                        break;
                    default:
                        label.setForeground(Color.BLACK);
                        label.setBackground(Color.WHITE);
                        label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
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
            
            // Use courier-specific status
            deliveriesTableModel.addRow(new Object[]{
                o.id, 
                o.recipientName != null ? o.recipientName : "-",
                o.recipientPhone != null ? o.recipientPhone : "-",
                o.getCourierStatus(),
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
            int failed = (int) myOrders.stream().filter(o -> "Failed".equals(o.status)).count();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(total));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(inTransit));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(pending));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(delivered));
            if (statValues[4] != null) statValues[4].setText(String.valueOf(delayed));
            if (statValues[5] != null) statValues[5].setText(String.valueOf(failed));
        });
    }
    
    private JPanel createDeliveriesButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        panel.setBackground(BG_LIGHT);
        
        JButton viewDetailsBtn = createActionButton("View Details", INFO, this::viewSelectedOrderDetails);
        JButton updateStatusBtn = createActionButton("Update Status", ORANGE, this::updateSelectedOrderStatus);
        JButton trackBtn = createActionButton("Track Order", PURPLE, this::trackSelectedOrder);
        
        panel.add(viewDetailsBtn);
        panel.add(updateStatusBtn);
        panel.add(trackBtn);
        
        return panel;
    }
    
    private void updateSelectedOrderStatus() {
        int row = deliveriesTable.getSelectedRow();
        if (row == -1) {
            showNotification("Please select an order to update", WARNING);
            return;
        }
        
        int modelRow = deliveriesTable.convertRowIndexToModel(row);
        if (modelRow >= 0 && modelRow < myOrders.size()) {
            Order selectedOrder = myOrders.get(modelRow);
            
            if ("Delivered".equals(selectedOrder.status)) {
                showNotification("This order has already been delivered. Cannot update status.", WARNING);
                return;
            }
            
            if ("Failed".equals(selectedOrder.status)) {
                showNotification("This order has failed. Admin will reassign it to another driver.", WARNING);
                return;
            }
            
            cardLayout.show(contentPanel, "UPDATE");
            updateSidebarButtonHighlight("Update Status");
            
            SwingUtilities.invokeLater(() -> {
                Timer timer = new Timer(100, e -> {
                    if (orderSelectCombo != null && orderSelectCombo.getItemCount() > 0) {
                        for (int i = 0; i < orderSelectCombo.getItemCount(); i++) {
                            String item = orderSelectCombo.getItemAt(i);
                            if (item != null && item.startsWith(selectedOrder.id)) {
                                orderSelectCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                        showNotification("Order selected: " + selectedOrder.id, SUCCESS);
                        ((Timer)e.getSource()).stop();
                    }
                });
                timer.setRepeats(true);
                timer.start();
            });
        }
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
        dialog.setSize(500, 550);
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
        
        List<TimelineStep> steps = new ArrayList<>();
        
        steps.add(new TimelineStep("Order Placed", "Order has been placed successfully", order.orderDate != null ? order.orderDate : "Date not available", true));
        
        boolean paymentCompleted = "Paid".equals(order.paymentStatus);
        steps.add(new TimelineStep("Payment Confirmed", paymentCompleted ? "Payment has been confirmed" : "Awaiting payment confirmation",
            order.paymentDate != null ? order.paymentDate : (paymentCompleted ? "Confirmed" : "Pending"), paymentCompleted));
        
        boolean processed = !"Pending".equals(order.status);
        steps.add(new TimelineStep("Order Processed", processed ? "Order has been processed and prepared" : "Order is being processed",
            processed ? "Processed" : "Processing", processed));
        
        boolean pickedUp = "Picked Up".equals(order.status) || "In Transit".equals(order.status) || "Delivered".equals(order.status);
        steps.add(new TimelineStep("Picked Up by Courier", pickedUp ? "Package has been picked up by courier" : "Awaiting pickup",
            pickedUp ? "Picked up" : "Pending", pickedUp));
        
        boolean inTransit = "In Transit".equals(order.status) || "Delivered".equals(order.status);
        steps.add(new TimelineStep("In Transit", inTransit ? "Package is on the way to destination" : "Awaiting dispatch",
            inTransit ? "In transit" : "Not yet", inTransit));
        
        boolean delivered = "Delivered".equals(order.status);
        String deliveryStatus = delivered ? (order.onTime ? "Successfully delivered on time" : "Delivered with delay") : "Delivery pending";
        steps.add(new TimelineStep("Delivered", deliveryStatus, delivered ? "Completed" : "Pending", delivered));
        
        for (int i = 0; i < steps.size(); i++) {
            TimelineStep step = steps.get(i);
            timelineContainer.add(createTimelineStep(step.title, step.description, step.time, step.completed));
            if (i < steps.size() - 1) {
                timelineContainer.add(createTimelineConnectorLine(step.completed && steps.get(i + 1).completed));
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
            } else {
                lbl.setForeground(PRIMARY_GREEN);
            }
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        } else {
            lbl.setForeground(TEXT_GRAY);
        }
        return lbl;
    }
    
    private void showNotification(String message, Color color) {
        String title = "Notification";
        if (color == SUCCESS) {
            title = "Success";
        } else if (color == WARNING) {
            title = "Warning";
        } else if (color == DANGER) {
            title = "Error";
        } else if (color == INFO) {
            title = "Information";
        }
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
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
            case "In Transit": return INFO;
            case "Delayed": return DANGER;
            case "Delivered": return SUCCESS;
            case "Failed": return DANGER;
            case "Cancelled": return TEXT_GRAY;
            default: return PRIMARY_GREEN;
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

        if (currentStatusFilter != null) {
            orderStatus += " · Filtered: " + currentStatusFilter;
        }
        
        return bar;
    }
}