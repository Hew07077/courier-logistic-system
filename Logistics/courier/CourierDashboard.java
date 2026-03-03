package courier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
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
    private final Color SIDEBAR_SELECTED = new Color(60, 90, 60);
    // Modern UI Colors (from VehicleManagement)
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    
    // 实心按钮颜色 (匹配绿色主题)
    private final Color BUTTON_SELECTED = new Color(46, 125, 50); // 实心绿色
    private final Color BUTTON_HOVER = new Color(27, 94, 32); // 深一点的绿色用于悬停
    private final Color BUTTON_NORMAL = new Color(0, 0, 0, 0); // 透明
    
    // 通知文本颜色 - 改为绿色系
    private final Color NOTIFICATION_COLOR = new Color(200, 230, 200); // 浅绿色
    private final Color NOTIFICATION_SELECTED_COLOR = Color.WHITE; // 选中时白色

    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Table Components - 移到DeliveryPage类中
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

    public CourierDashboard() {
        setTitle("logiXpress Courier Portal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        initUI();
    }

    private void initUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    // ================= 更新后的TOP BAR (完全匹配AdminDashboard风格) =================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(GREEN_DARK);
        bar.setPreferredSize(new Dimension(getWidth(), 80));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        // Left side with logo (完全匹配AdminDashboard)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftPanel.setOpaque(false);

        // Load and resize logo (使用与AdminDashboard完全相同的方法)
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

        // Right side with time and exit (完全匹配AdminDashboard)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setOpaque(false);

        // Time panel with icon
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        timePanel.setOpaque(false);

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

        // Exit button (完全匹配AdminDashboard的logout按钮)
        JButton exitBtn = createButton("Exit", new Color(220, 53, 69));
        exitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to exit?", "Confirm Exit", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        rightPanel.add(exitBtn);

        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    // ================= 更新后的SIDEBAR (完全匹配AdminDashboard风格，颜色改为绿色) =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(PRIMARY_GREEN);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));

        // Logo area (完全匹配AdminDashboard)
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));

        // 使用与AdminDashboard完全相同的方法加载logo
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

        // Menu items with notification badges (完全匹配AdminDashboard风格，颜色改为绿色)
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

        // User Profile (完全匹配AdminDashboard)
        sidebar.add(createUserProfile(), BorderLayout.SOUTH);

        return sidebar;
    }

    // ================= 新增的Nav Button (完全匹配AdminDashboard风格，颜色改为绿色) =================
    private JButton createNavButton(String text, String card, String notification, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        
        // 根据选中状态设置通知文本颜色
        String notificationColor = selected ? "#FFFFFF" : "#A0D0A0"; // 选中时白色，未选中时浅绿色
        
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
                    // 更新悬停时的通知文本颜色
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
                    // 恢复未选中时的通知文本颜色
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
                // 恢复之前选中按钮的通知文本颜色
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
            // 更新新选中按钮的通知文本颜色
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

    // ================= 新增的Button创建方法 (完全匹配AdminDashboard) =================
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

    // ================= 新增的用户Profile (完全匹配AdminDashboard) =================
    private JPanel createUserProfile() {
        JPanel profile = new JPanel(new BorderLayout());
        profile.setBackground(new Color(0, 0, 0, 30));
        profile.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);

        JLabel userName = new JLabel("👤 Alex Wong", SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userName.setForeground(Color.WHITE);
        userInfo.add(userName);

        JLabel userRole = new JLabel("Courier #LX-88029", SwingConstants.CENTER);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(new Color(255, 255, 255, 200));
        userInfo.add(userRole);

        profile.add(userInfo, BorderLayout.CENTER);

        return profile;
    }

    // ================= Logo加载方法 (完全复制AdminDashboard的loadLogo方法) =================
    private ImageIcon loadLogo(String filename) {
        try {
            // 首先尝试从资源加载
            java.net.URL imgURL = getClass().getResource(filename);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                // 如果资源不存在，尝试从文件系统加载
                java.io.File file = new java.io.File(filename);
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
                
                // 尝试其他可能的路径
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

    // ================= 以下所有方法保持原样，只修改了名称 =================
    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);

        contentPanel.add(createEditableProfilePage(), "PROFILE");
        contentPanel.add(DeliveryPage(), "DELIVERIES");  // 使用独立的DeliveryPage
        contentPanel.add(createEnhancedVehiclePage(), "VEHICLE");
        contentPanel.add(createStatsPage(), "STATS");

        return contentPanel;
    }

    /* --- DELIVERY PAGE - 使用独立的DeliveryPage类 --- */
    private JPanel DeliveryPage() {
        return new DeliveryPage();  // DeliveryPage只能通过这里被创建
    }

    /* --- EDITABLE PROFILE PAGE WITH PHOTO UPLOAD --- */
    private JPanel createEditableProfilePage() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_LIGHT);
        
        JPanel profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), 
            new EmptyBorder(30, 50, 30, 50)));
        profileCard.setPreferredSize(new Dimension(500, 650));

        // Profile Header
        JLabel header = new JLabel("My Profile", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(PRIMARY_GREEN);
        header.setAlignmentX(0.5f);
        
        // Profile Photo with upload button
        JPanel photoPanel = new JPanel();
        photoPanel.setLayout(new BoxLayout(photoPanel, BoxLayout.Y_AXIS));
        photoPanel.setBackground(Color.WHITE);
        photoPanel.setAlignmentX(0.5f);
        
        profilePhotoLabel = new JLabel("👤", SwingConstants.CENTER);
        profilePhotoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        profilePhotoLabel.setPreferredSize(new Dimension(150, 150));
        profilePhotoLabel.setMaximumSize(new Dimension(150, 150));
        profilePhotoLabel.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN, 3));
        profilePhotoLabel.setAlignmentX(0.5f);
        
        JButton uploadProfilePhotoBtn = new JButton("📷 Upload Photo");
        uploadProfilePhotoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadProfilePhotoBtn.setBackground(Color.WHITE);
        uploadProfilePhotoBtn.setForeground(PRIMARY_GREEN);
        uploadProfilePhotoBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN));
        uploadProfilePhotoBtn.setAlignmentX(0.5f);
        uploadProfilePhotoBtn.setMaximumSize(new Dimension(120, 30));
        uploadProfilePhotoBtn.addActionListener(e -> uploadProfilePhoto());
        
        photoPanel.add(profilePhotoLabel);
        photoPanel.add(Box.createVerticalStrut(10));
        photoPanel.add(uploadProfilePhotoBtn);

        // Personal Details Section - Editable
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Personal Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_GREEN
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField("Alex Wong");
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        detailsPanel.add(nameField, gbc);
        
        // Staff ID
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel idLabel = new JLabel("Staff ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsPanel.add(idLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        staffIdField = new JTextField("LX-88029");
        staffIdField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        staffIdField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        detailsPanel.add(staffIdField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField("alex.wong@logixpress.com");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        detailsPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        phoneField = new JTextField("012-3456789");
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        detailsPanel.add(phoneField, gbc);
        
        // Vehicle Section - Editable
        JPanel vehiclePanel = new JPanel(new GridBagLayout());
        vehiclePanel.setBackground(Color.WHITE);
        vehiclePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Assigned Vehicle",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_GREEN
        ));
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel vehicleTypeLabel = new JLabel("Vehicle:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vehiclePanel.add(vehicleTypeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        vehicleModelField = new JTextField("Toyota Hiace");
        vehicleModelField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vehicleModelField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        vehiclePanel.add(vehicleModelField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel plateLabel = new JLabel("Plate No:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vehiclePanel.add(plateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        plateNoField = new JTextField("VAF 1234");
        plateNoField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        plateNoField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        vehiclePanel.add(plateNoField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel mileageLabel = new JLabel("Current Mileage:");
        mileageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vehiclePanel.add(mileageLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        mileageField = new JTextField("45,892 km");
        mileageField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mileageField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        vehiclePanel.add(mileageField, gbc);

        // Save Button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(PRIMARY_GREEN);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setOpaque(true);
        saveBtn.setBorderPainted(false);
        saveBtn.setAlignmentX(0.5f);
        saveBtn.setMaximumSize(new Dimension(150, 40));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        saveBtn.addActionListener(e -> saveProfileChanges());

        // Assemble profile card
        profileCard.add(header);
        profileCard.add(Box.createVerticalStrut(20));
        profileCard.add(photoPanel);
        profileCard.add(Box.createVerticalStrut(20));
        profileCard.add(detailsPanel);
        profileCard.add(Box.createVerticalStrut(15));
        profileCard.add(vehiclePanel);
        profileCard.add(Box.createVerticalStrut(20));
        profileCard.add(saveBtn);
        
        mainPanel.add(profileCard);
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
            
            // Load and display the image
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

    /* --- ENHANCED VEHICLE PAGE WITH PERSONAL DETAILS --- */
    private JPanel createEnhancedVehiclePage() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_LIGHT);
        
        JPanel vehicleCard = new JPanel();
        vehicleCard.setLayout(new BoxLayout(vehicleCard, BoxLayout.Y_AXIS));
        vehicleCard.setBackground(Color.WHITE);
        vehicleCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(30, 40, 30, 40)));
        vehicleCard.setPreferredSize(new Dimension(550, 700));

        // Header
        JLabel header = new JLabel("Vehicle Management", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(PRIMARY_GREEN);
        header.setAlignmentX(0.5f);

        // Personal Details Section
        JPanel personalDetailsPanel = new JPanel(new GridBagLayout());
        personalDetailsPanel.setBackground(Color.WHITE);
        personalDetailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Personal Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_GREEN
        ));
        personalDetailsPanel.setAlignmentX(0.5f);
        personalDetailsPanel.setMaximumSize(new Dimension(500, 150));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Driver Name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel driverNameLabel = new JLabel("Driver Name:");
        driverNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        personalDetailsPanel.add(driverNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField driverNameField = new JTextField("Alex Wong");
        driverNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        driverNameField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        personalDetailsPanel.add(driverNameField, gbc);

        // License Number
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel licenseLabel = new JLabel("License No:");
        licenseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        personalDetailsPanel.add(licenseLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField licenseField = new JTextField("D12345678");
        licenseField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        licenseField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        personalDetailsPanel.add(licenseField, gbc);

        // Phone Number
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel phoneLabel = new JLabel("Phone No:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        personalDetailsPanel.add(phoneLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField phoneField = new JTextField("012-3456789");
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        personalDetailsPanel.add(phoneField, gbc);

        // Vehicle Details Section
        JPanel vehicleDetailsPanel = new JPanel(new GridBagLayout());
        vehicleDetailsPanel.setBackground(Color.WHITE);
        vehicleDetailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Vehicle Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_GREEN
        ));
        vehicleDetailsPanel.setAlignmentX(0.5f);
        vehicleDetailsPanel.setMaximumSize(new Dimension(500, 120));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Vehicle Model
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel modelLabel = new JLabel("Vehicle Model:");
        modelLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        vehicleDetailsPanel.add(modelLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField modelField = new JTextField("Toyota Hiace 2023");
        modelField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        vehicleDetailsPanel.add(modelField, gbc);

        // License Plate
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel plateLabel = new JLabel("License Plate:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        vehicleDetailsPanel.add(plateLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField plateField = new JTextField("VAF 1234");
        plateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        plateField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        vehicleDetailsPanel.add(plateField, gbc);

        // Report Issue Form
        JPanel reportPanel = new JPanel();
        reportPanel.setLayout(new BoxLayout(reportPanel, BoxLayout.Y_AXIS));
        reportPanel.setBackground(Color.WHITE);
        reportPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Report Vehicle Issue",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_GREEN
        ));
        reportPanel.setAlignmentX(0.5f);
        reportPanel.setMaximumSize(new Dimension(500, 180));

        // Mileage Input
        JPanel mileageInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mileageInputPanel.setBackground(Color.WHITE);
        JLabel mileageInputLabel = new JLabel("Current Mileage (km):");
        mileageInputLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField mileageField = new JTextField(15);
        mileageField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mileageField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        mileageInputPanel.add(mileageInputLabel);
        mileageInputPanel.add(mileageField);
        
        // Issue Type Combo
        JPanel issuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        issuePanel.setBackground(Color.WHITE);
        JLabel issueLabel = new JLabel("Issue Type:");
        issueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JComboBox<String> issueCombo = new JComboBox<>(new String[]{
            "Select Issue Type...",
            "No Issues - Routine Check",
            "Engine Light On",
            "Brake Noise", 
            "Tire Pressure Low",
            "Battery Problem",
            "AC Not Working",
            "Transmission Issue",
            "Electrical Problem"
        });
        issueCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        issueCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        issueCombo.setBackground(Color.WHITE);
        issueCombo.setPreferredSize(new Dimension(200, 30));
        issuePanel.add(issueLabel);
        issuePanel.add(issueCombo);
        
        // Description Area
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(Color.WHITE);
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextArea descArea = new JTextArea(3, 30);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);
        descScroll.setPreferredSize(new Dimension(450, 60));
        
        descPanel.add(descLabel, BorderLayout.NORTH);
        descPanel.add(descScroll, BorderLayout.CENTER);
        
        reportPanel.add(mileageInputPanel);
        reportPanel.add(Box.createVerticalStrut(5));
        reportPanel.add(issuePanel);
        reportPanel.add(Box.createVerticalStrut(5));
        reportPanel.add(descPanel);

        // Submit Button
        JButton submitBtn = new JButton("Submit Report");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setBackground(PRIMARY_GREEN);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setContentAreaFilled(true);
        submitBtn.setOpaque(true);
        submitBtn.setBorderPainted(false);
        submitBtn.setAlignmentX(0.5f);
        submitBtn.setMaximumSize(new Dimension(200, 40));
        submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        submitBtn.addActionListener(e -> {
            if (driverNameField.getText().trim().isEmpty() || 
                licenseField.getText().trim().isEmpty() || 
                phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill in all personal details.", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (issueCombo.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "Please select an issue type.", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String mileage = mileageField.getText().trim();
            if (mileage.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter current mileage.", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            JOptionPane.showMessageDialog(this, 
                "Vehicle report submitted successfully!\n\n" +
                "Driver: " + driverNameField.getText() + "\n" +
                "License: " + licenseField.getText() + "\n" +
                "Phone: " + phoneField.getText() + "\n" +
                "Vehicle: " + modelField.getText() + " (" + plateField.getText() + ")\n" +
                "Mileage: " + mileage + " km\n" +
                "Issue: " + issueCombo.getSelectedItem() + "\n" +
                "Description: " + (descArea.getText().trim().isEmpty() ? "No description" : descArea.getText().trim()),
                "Report Submitted",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Clear form
            mileageField.setText("");
            issueCombo.setSelectedIndex(0);
            descArea.setText("");
        });

        // Assemble vehicle card
        vehicleCard.add(header);
        vehicleCard.add(Box.createVerticalStrut(15));
        vehicleCard.add(personalDetailsPanel);
        vehicleCard.add(Box.createVerticalStrut(15));
        vehicleCard.add(vehicleDetailsPanel);
        vehicleCard.add(Box.createVerticalStrut(15));
        vehicleCard.add(reportPanel);
        vehicleCard.add(Box.createVerticalStrut(20));
        vehicleCard.add(submitBtn);
        
        mainPanel.add(vehicleCard);
        return mainPanel;
    }

    /* --- STATS PAGE --- */
    private JPanel createStatsPage() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_LIGHT);
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        statsPanel.add(createStatBox("Deliveries Today", "12", PRIMARY_GREEN));
        statsPanel.add(createStatBox("Weekly Total", "84", new Color(25, 118, 210)));
        statsPanel.add(createStatBox("On-Time Rate", "96%", new Color(46, 125, 50)));
        statsPanel.add(createStatBox("Rating", "4.9 ⭐", new Color(255, 160, 0)));
        statsPanel.add(createStatBox("Distance", "342 km", new Color(156, 39, 176)));
        statsPanel.add(createStatBox("Fuel Used", "45 L", new Color(100, 100, 100)));
        
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
        
        JLabel status = new JLabel("  System Status: Online | Session: Courier_88029 | " + 
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