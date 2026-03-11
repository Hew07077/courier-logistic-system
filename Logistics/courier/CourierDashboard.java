package courier;

import courier.CourierData;
import courier.CourierDataLoader;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

public class CourierDashboard extends JFrame {

    // --- Refined Color Palette ---
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_DARK = new Color(27, 94, 32);
    private final Color GREEN_LIGHT = new Color(220, 245, 220);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    
    // Modern UI Colors
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(255, 193, 7);
    
    // 实心按钮颜色
    private final Color BUTTON_SELECTED = new Color(46, 125, 50);
    private final Color BUTTON_HOVER = new Color(27, 94, 32);
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Profile Components
    private JLabel profilePhotoLabel;
    private File profilePhotoFile;
    private JTextField nameField;
    private JTextField staffIdField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField vehicleModelField;
    private JTextField plateNoField;
    private JTextField mileageField;
    
    // Data Loading
    private CourierDataLoader dataLoader;
    private CourierData currentCourier;
    private String loggedInCourierName;
    
    // Vehicle Report
    private VehicleReport vehicleReport;

    public CourierDashboard() {
        this("Alex Wong"); // Default name if none provided
    }
    
    public CourierDashboard(String courierName) {
        this.loggedInCourierName = courierName;
        
        setTitle("logiXpress Courier Portal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception ignored) {}

        // Load courier data first
        dataLoader = new CourierDataLoader();
        loadCourierData(loggedInCourierName);
        
        // Initialize vehicle report
        vehicleReport = new VehicleReport(currentCourier);
        
        initUI();
    }

    private void initUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }
    
    private void loadCourierData(String courierName) {
        currentCourier = dataLoader.findCourierByName(courierName);
        if (currentCourier != null) {
            // Update profile fields will be done when creating the profile page
            loggedInCourierName = currentCourier.name;
        }
    }
    
    private void loadDriverPhoto(String photoPath) {
        try {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                ImageIcon icon = new ImageIcon(photoPath);
                Image image = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                profilePhotoLabel.setIcon(new ImageIcon(image));
                profilePhotoLabel.setText("");
                profilePhotoFile = photoFile;
            }
        } catch (Exception e) {
            System.err.println("Error loading photo: " + e.getMessage());
        }
    }
    
    private void updateUserProfile(CourierData courier) {
        // Find the user profile panel in sidebar and update it
        Component[] components = ((JPanel)getContentPane().getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Look for the user profile panel at the bottom
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JPanel) {
                    JPanel userInfo = (JPanel) panel.getComponent(0);
                    if (userInfo.getComponentCount() >= 2) {
                        Component comp1 = userInfo.getComponent(0);
                        Component comp2 = userInfo.getComponent(1);
                        if (comp1 instanceof JLabel && comp2 instanceof JLabel) {
                            JLabel nameLabel = (JLabel) comp1;
                            JLabel roleLabel = (JLabel) comp2;
                            
                            nameLabel.setText(courier.name);
                            roleLabel.setText("Courier #" + courier.id);
                            break;
                        }
                    }
                }
            }
        }
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(GREEN_DARK);
        bar.setPreferredSize(new Dimension(getWidth(), 80));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        // Load and resize logo (保留Logo)
        ImageIcon logoIcon = loadLogo("logo.ct.png");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            leftPanel.add(logoLabel);
        }

        JLabel title = new JLabel("LogiXpress Courier Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JLabel badge = new JLabel("COURIER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(PRIMARY_GREEN);
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
                this.dispose(); // Close the courier dashboard
                // Open the login page
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

        // 使用与AdminDashboard完全相同的方法加载logo (保留Logo)
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

        JPanel menu = new JPanel(new GridLayout(6, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        menu.add(createNavButton("My Profile", "PROFILE", 
            "Personal information", true));
        menu.add(createNavButton("Deliveries & Updates", "DELIVERIES", 
            "Track & manage parcels", false));
        menu.add(createNavButton("Vehicle Report", "VEHICLE", 
            "Report vehicle issues", false));
        menu.add(createNavButton("Statistics", "STATS", 
            "Performance metrics", false));

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
                    JLabel label = (JLabel) btn.getComponent(0);
                    String currentText = label.getText();
                    String updatedText = currentText.replace(
                        "color: #A0D0A0;", 
                        "color: #FFFFFF;"
                    );
                    label.setText(updatedText);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setOpaque(false);
                    btn.setBackground(BUTTON_NORMAL);
                    JLabel label = (JLabel) btn.getComponent(0);
                    String currentText = label.getText();
                    String updatedText = currentText.replace(
                        "color: #FFFFFF;", 
                        "color: #A0D0A0;"
                    );
                    label.setText(updatedText);
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null && activeButton != btn) {
                activeButton.setOpaque(false);
                activeButton.setBackground(BUTTON_NORMAL);
                JLabel oldLabel = (JLabel) activeButton.getComponent(0);
                String oldText = oldLabel.getText();
                String updatedOldText = oldText.replace(
                    "color: #FFFFFF;", 
                    "color: #A0D0A0;"
                );
                oldLabel.setText(updatedOldText);
                activeButton.repaint();
            }
            
            btn.setOpaque(true);
            btn.setBackground(BUTTON_SELECTED);
            JLabel newLabel = (JLabel) btn.getComponent(0);
            String newText = newLabel.getText();
            String updatedNewText = newText.replace(
                "color: #A0D0A0;", 
                "color: #FFFFFF;"
            );
            newLabel.setText(updatedNewText);
            
            activeButton = btn;
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

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);

        String displayName = (currentCourier != null) ? currentCourier.name : "Loading...";
        String displayId = (currentCourier != null) ? "Courier #" + currentCourier.id : "Courier";

        JLabel userName = new JLabel(displayName, SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userName.setForeground(Color.WHITE);
        userInfo.add(userName);

        JLabel userRole = new JLabel(displayId, SwingConstants.CENTER);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(new Color(255, 255, 255, 200));
        userInfo.add(userRole);

        profile.add(userInfo, BorderLayout.CENTER);

        return profile;
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
                
                String[] possiblePaths = {
                    "C:\\Users\\User\\Documents\\LOGISTICS\\logo1.png",
                    "logo1.png",
                    "images/logo.jpeg",
                    "images/logo1.png",
                    "../logo.jpeg",
                    "src/logo.jpeg"
                };
                
                for (String path : possiblePaths) {
                    file = new java.io.File(path);
                    if (file.exists()) {
                        return new ImageIcon(file.getAbsolutePath());
                    }
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

        contentPanel.add(createEnhancedProfilePage(), "PROFILE");
        contentPanel.add(DeliveryPage(), "DELIVERIES");
        contentPanel.add(vehicleReport.createVehicleReportPanel(), "VEHICLE");
        contentPanel.add(createStatsPage(), "STATS");

        return contentPanel;
    }

    private JPanel DeliveryPage() {
        return new DeliveryPage();
    }

    private JPanel createEnhancedProfilePage() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Create a modern card with subtle shadow effect (same as vehicle page)
        JPanel profileCard = new JPanel(new BorderLayout(20, 20));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 3, 3, new Color(0, 0, 0, 20)),
            new EmptyBorder(30, 30, 30, 30)));
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("My Profile");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(PRIMARY_GREEN);
        
        headerPanel.add(headerLabel);
        
        // Create a scroll pane for the content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Wrap content panel in a scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        
        // Remove the border from the viewport
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.weightx = 1.0;
        
        // Set consistent sizes for all input fields
        Dimension fieldSize = new Dimension(400, 35);
        Dimension labelSize = new Dimension(120, 30);
        
        int row = 0;
        
        // ===== PHOTO SECTION =====
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel photoSectionLabel = new JLabel("PROFILE PHOTO");
        photoSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        photoSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(photoSectionLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Photo Panel
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel photoPanel = new JPanel();
        photoPanel.setLayout(new BoxLayout(photoPanel, BoxLayout.Y_AXIS));
        photoPanel.setBackground(Color.WHITE);
        
        profilePhotoLabel = new JLabel("", SwingConstants.CENTER);
        profilePhotoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        profilePhotoLabel.setPreferredSize(new Dimension(150, 150));
        profilePhotoLabel.setMaximumSize(new Dimension(150, 150));
        profilePhotoLabel.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN, 3));
        
        JButton uploadProfilePhotoBtn = new JButton("Upload Photo");
        uploadProfilePhotoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadProfilePhotoBtn.setBackground(Color.WHITE);
        uploadProfilePhotoBtn.setForeground(PRIMARY_GREEN);
        uploadProfilePhotoBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN));
        uploadProfilePhotoBtn.setMaximumSize(new Dimension(120, 30));
        uploadProfilePhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadProfilePhotoBtn.addActionListener(e -> uploadProfilePhoto());
        
        // Add hover effect
        uploadProfilePhotoBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                uploadProfilePhotoBtn.setBackground(PRIMARY_GREEN);
                uploadProfilePhotoBtn.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                uploadProfilePhotoBtn.setBackground(Color.WHITE);
                uploadProfilePhotoBtn.setForeground(PRIMARY_GREEN);
            }
        });
        
        photoPanel.add(profilePhotoLabel);
        photoPanel.add(Box.createVerticalStrut(10));
        photoPanel.add(uploadProfilePhotoBtn);
        
        contentPanel.add(photoPanel, gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        row++;
        
        // Add some spacing
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        contentPanel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // ===== PERSONAL INFORMATION SECTION =====
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel personalSectionLabel = new JLabel("PERSONAL INFORMATION");
        personalSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        personalSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(personalSectionLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_GRAY);
        nameLabel.setPreferredSize(labelSize);
        nameLabel.setMinimumSize(labelSize);
        contentPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        nameField = new JTextField(currentCourier != null ? currentCourier.name : "");
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        nameField.setBackground(new Color(250, 250, 250));
        nameField.setPreferredSize(fieldSize);
        nameField.setMinimumSize(fieldSize);
        nameField.setMaximumSize(fieldSize);
        contentPanel.add(nameField, gbc);
        
        // Staff ID
        gbc.gridx = 0; gbc.gridy = row;
        JLabel idLabel = new JLabel("Staff ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(TEXT_GRAY);
        idLabel.setPreferredSize(labelSize);
        idLabel.setMinimumSize(labelSize);
        contentPanel.add(idLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        staffIdField = new JTextField(currentCourier != null ? currentCourier.id : "");
        staffIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        staffIdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        staffIdField.setBackground(new Color(250, 250, 250));
        staffIdField.setEditable(false);
        staffIdField.setPreferredSize(fieldSize);
        staffIdField.setMinimumSize(fieldSize);
        staffIdField.setMaximumSize(fieldSize);
        contentPanel.add(staffIdField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = row;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(TEXT_GRAY);
        emailLabel.setPreferredSize(labelSize);
        emailLabel.setMinimumSize(labelSize);
        contentPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        emailField = new JTextField(currentCourier != null ? currentCourier.email : "");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        emailField.setBackground(new Color(250, 250, 250));
        emailField.setPreferredSize(fieldSize);
        emailField.setMinimumSize(fieldSize);
        emailField.setMaximumSize(fieldSize);
        contentPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setForeground(TEXT_GRAY);
        phoneLabel.setPreferredSize(labelSize);
        phoneLabel.setMinimumSize(labelSize);
        contentPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        phoneField = new JTextField(currentCourier != null ? currentCourier.phone : "");
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        phoneField.setBackground(new Color(250, 250, 250));
        phoneField.setPreferredSize(fieldSize);
        phoneField.setMinimumSize(fieldSize);
        phoneField.setMaximumSize(fieldSize);
        contentPanel.add(phoneField, gbc);
        
        // Add some spacing
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        contentPanel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // ===== VEHICLE INFORMATION SECTION =====
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel vehicleSectionLabel = new JLabel("VEHICLE INFORMATION");
        vehicleSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vehicleSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(vehicleSectionLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Vehicle
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleTypeLabel = new JLabel("Vehicle:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleTypeLabel.setForeground(TEXT_GRAY);
        vehicleTypeLabel.setPreferredSize(labelSize);
        vehicleTypeLabel.setMinimumSize(labelSize);
        contentPanel.add(vehicleTypeLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        vehicleModelField = new JTextField(currentCourier != null && currentCourier.vehicleId != null ? 
            currentCourier.vehicleId : "Not Assigned");
        vehicleModelField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vehicleModelField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        vehicleModelField.setBackground(new Color(250, 250, 250));
        vehicleModelField.setPreferredSize(fieldSize);
        vehicleModelField.setMinimumSize(fieldSize);
        vehicleModelField.setMaximumSize(fieldSize);
        contentPanel.add(vehicleModelField, gbc);
        
        // Plate No
        gbc.gridx = 0; gbc.gridy = row;
        JLabel plateLabel = new JLabel("Plate No:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plateLabel.setForeground(TEXT_GRAY);
        plateLabel.setPreferredSize(labelSize);
        plateLabel.setMinimumSize(labelSize);
        contentPanel.add(plateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        plateNoField = new JTextField("VAF 1234");
        plateNoField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        plateNoField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        plateNoField.setBackground(new Color(250, 250, 250));
        plateNoField.setPreferredSize(fieldSize);
        plateNoField.setMinimumSize(fieldSize);
        plateNoField.setMaximumSize(fieldSize);
        contentPanel.add(plateNoField, gbc);
        
        // Current Mileage
        gbc.gridx = 0; gbc.gridy = row;
        JLabel mileageLabel = new JLabel("Current Mileage:");
        mileageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mileageLabel.setForeground(TEXT_GRAY);
        mileageLabel.setPreferredSize(labelSize);
        mileageLabel.setMinimumSize(labelSize);
        contentPanel.add(mileageLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JPanel mileagePanel = new JPanel(new BorderLayout());
        mileagePanel.setBackground(Color.WHITE);
        mileagePanel.setPreferredSize(fieldSize);
        mileagePanel.setMinimumSize(fieldSize);
        mileagePanel.setMaximumSize(fieldSize);
        
        mileageField = new JTextField("45,892");
        mileageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mileageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        mileageField.setBackground(new Color(250, 250, 250));
        mileageField.setHorizontalAlignment(JTextField.RIGHT);
        
        JLabel kmLabel = new JLabel(" km");
        kmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        kmLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
        
        mileagePanel.add(mileageField, BorderLayout.CENTER);
        mileagePanel.add(kmLabel, BorderLayout.EAST);
        contentPanel.add(mileagePanel, gbc);
        
        // Load photo if exists
        if (currentCourier != null && currentCourier.photoPath != null && !currentCourier.photoPath.isEmpty()) {
            loadDriverPhoto(currentCourier.photoPath);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(PRIMARY_GREEN);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setFocusPainted(false);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setOpaque(true);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(150, 40));
        
        // Add hover effect
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(GREEN_DARK);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(PRIMARY_GREEN);
            }
        });
        
        saveBtn.addActionListener(e -> saveProfileChanges());
        
        buttonPanel.add(saveBtn);
        
        // Assemble the card
        profileCard.add(headerPanel, BorderLayout.NORTH);
        profileCard.add(scrollPane, BorderLayout.CENTER);
        profileCard.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(profileCard, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private void uploadProfilePhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Photo");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            profilePhotoFile = fileChooser.getSelectedFile();
            
            ImageIcon icon = new ImageIcon(profilePhotoFile.getPath());
            Image image = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            profilePhotoLabel.setIcon(new ImageIcon(image));
            profilePhotoLabel.setText("");
            
            JOptionPane.showMessageDialog(this,
                "Profile photo uploaded: " + profilePhotoFile.getName(),
                "Upload Successful",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveProfileChanges() {
        String message = "Profile updated successfully!\n\n" +
            "Name: " + nameField.getText() + "\n" +
            "Staff ID: " + staffIdField.getText() + "\n" +
            "Email: " + emailField.getText() + "\n" +
            "Phone: " + phoneField.getText() + "\n" +
            "Vehicle: " + vehicleModelField.getText() + "\n" +
            "Plate No: " + plateNoField.getText() + "\n" +
            "Mileage: " + mileageField.getText() + "\n" +
            (profilePhotoFile != null ? "Photo: " + profilePhotoFile.getName() : "Photo: Not changed");
        
        JOptionPane.showMessageDialog(this,
            message,
            "Profile Saved",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createStatsPage() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_LIGHT);
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        if (currentCourier != null) {
            statsPanel.add(createStatBox("Total Deliveries", String.valueOf(currentCourier.totalDeliveries), PRIMARY_GREEN));
            statsPanel.add(createStatBox("Rating", String.format("%.1f ⭐", currentCourier.rating), new Color(255, 160, 0)));
            statsPanel.add(createStatBox("Status", currentCourier.status, new Color(46, 125, 50)));
            statsPanel.add(createStatBox("Join Date", currentCourier.joinDate, new Color(25, 118, 210)));
            statsPanel.add(createStatBox("License", currentCourier.licenseNumber, new Color(156, 39, 176)));
            statsPanel.add(createStatBox("Expiry", currentCourier.licenseExpiry, new Color(100, 100, 100)));
        } else {
            statsPanel.add(createStatBox("Deliveries Today", "12", PRIMARY_GREEN));
            statsPanel.add(createStatBox("Weekly Total", "84", new Color(25, 118, 210)));
            statsPanel.add(createStatBox("On-Time Rate", "96%", new Color(46, 125, 50)));
            statsPanel.add(createStatBox("Rating", "4.9", new Color(255, 160, 0)));
            statsPanel.add(createStatBox("Distance", "342 km", new Color(156, 39, 176)));
            statsPanel.add(createStatBox("Fuel Used", "45 L", new Color(100, 100, 100)));
        }
        
        p.add(statsPanel);
        return p;
    }

    private JPanel createStatBox(String title, String val, Color c) {
        JPanel box = new JPanel(new BorderLayout(5, 5));
        box.setPreferredSize(new Dimension(180, 120));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_GRAY);
        
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 28)); 
        v.setForeground(c);
        
        box.add(t, BorderLayout.NORTH); 
        box.add(v, BorderLayout.CENTER);
        
        return box;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(getWidth(), 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        String sessionInfo = (currentCourier != null) ? 
            "Session: " + currentCourier.id : "Session: Courier_88029";
        
        JLabel status = new JLabel("  System Status: Online | " + sessionInfo + " | " + 
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(TEXT_GRAY);
        bar.add(status, BorderLayout.WEST);
        
        JLabel deliveryCount = new JLabel("Pending Deliveries: 3  ");
        deliveryCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deliveryCount.setForeground(PRIMARY_GREEN);
        bar.add(deliveryCount, BorderLayout.EAST);
        
        return bar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CourierDashboard().setVisible(true));
    }
}