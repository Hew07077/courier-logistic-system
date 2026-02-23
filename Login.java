package LogisticAdmin.gui;

import LogisticAdmin.gui.admin.AdminDashboard;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Login extends JFrame {
    
    // ========== Data Storage Files ==========
    private static final String SENDER_DATA_FILE = "sender_data.dat";
    private static final String COURIER_DATA_FILE = "courier_data.dat";
    
    // ========== In-Memory Databases ==========
    private static Map<String, SenderAccount> senderDatabase = new HashMap<>();
    private static Map<String, CourierAccount> courierDatabase = new HashMap<>();
    
    // ========== Account Model Classes ==========
    static class SenderAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String fullName;
        String email;
        String phone;
        String userId;
        String passwordHash;
        String registrationDate;
        String lastLogin;
        String address;
        String companyName;
        String status;
        
        public SenderAccount(String fullName, String email, String phone, String userId, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.userId = userId;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.lastLogin = null;
            this.address = "";
            this.companyName = "";
            this.status = "Active";
        }
    }
    
    static class CourierAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String fullName;
        String email;
        String phone;
        String driverLicense;
        String vehicleType;
        String vehiclePlate;
        String userId;
        String passwordHash;
        String registrationDate;
        String lastLogin;
        String status;
        
        public CourierAccount(String fullName, String email, String phone, String driverLicense,
                            String vehicleType, String vehiclePlate, String userId, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.driverLicense = driverLicense;
            this.vehicleType = vehicleType;
            this.vehiclePlate = vehiclePlate;
            this.userId = userId;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.lastLogin = null;
            this.status = "Pending Approval";
        }
    }
    
    // ========== Static Initialization ==========
    static {
        loadSenderData();
        loadCourierData();
        addDefaultAccounts();
    }
    
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
    
    private static boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }
    
    private static void loadSenderData() {
        File file = new File(SENDER_DATA_FILE);
        if (!file.exists()) {
            senderDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            senderDatabase = (HashMap<String, SenderAccount>) ois.readObject();
        } catch (Exception e) {
            senderDatabase = new HashMap<>();
        }
    }
    
    private static void saveSenderData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SENDER_DATA_FILE))) {
            oos.writeObject(senderDatabase);
        } catch (Exception e) {
            System.err.println("Error saving sender data: " + e.getMessage());
        }
    }
    
    private static void loadCourierData() {
        File file = new File(COURIER_DATA_FILE);
        if (!file.exists()) {
            courierDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            courierDatabase = (HashMap<String, CourierAccount>) ois.readObject();
        } catch (Exception e) {
            courierDatabase = new HashMap<>();
        }
    }
    
    private static void saveCourierData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(COURIER_DATA_FILE))) {
            oos.writeObject(courierDatabase);
        } catch (Exception e) {
            System.err.println("Error saving courier data: " + e.getMessage());
        }
    }
    
    private static void addDefaultAccounts() {
        if (!senderDatabase.containsKey("sender")) {
            senderDatabase.put("sender", new SenderAccount(
                "Demo Sender", "sender@example.com", "1234567890", "sender", "sender123"));
            saveSenderData();
        }
        if (!courierDatabase.containsKey("courier")) {
            CourierAccount courier = new CourierAccount(
                "Demo Courier", "courier@example.com", "0987654321",
                "DL123456", "Motorcycle", "ABC-123", "courier", "courier123");
            courier.status = "Approved";
            courierDatabase.put("courier", courier);
            saveCourierData();
        }
    }
    
    // ========== UI Components ==========
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    
    private JTextField senderUserIdField;
    private JPasswordField senderPasswordField;
    private JCheckBox senderShowPasswordCheckBox;
    
    private JTextField senderRegNameField;
    private JTextField senderRegEmailField;
    private JTextField senderRegPhoneField;
    private JTextField senderRegUserIdField;
    private JPasswordField senderRegPasswordField;
    private JPasswordField senderRegConfirmPwdField;
    
    private JTextField courierUserIdField;
    private JPasswordField courierPasswordField;
    private JCheckBox courierShowPasswordCheckBox;
    
    private JTextField courierRegNameField;
    private JTextField courierRegEmailField;
    private JTextField courierRegPhoneField;
    private JTextField courierRegLicenseField;
    private JTextField courierRegVehicleTypeField;
    private JTextField courierRegVehiclePlateField;
    private JTextField courierRegUserIdField;
    private JPasswordField courierRegPasswordField;
    private JPasswordField courierRegConfirmPwdField;
    
    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    private JTabbedPane senderTabbedPane;
    private JTabbedPane courierTabbedPane;
    
    // ========== 颜色定义 ==========
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color ORANGE_WHITE = new Color(255, 245, 235);
    private final Color WHITE_PURE = Color.WHITE;
    private final Color BLACK_TEXT = Color.BLACK;
    
    private final Color ADMIN_COLOR = ORANGE_PRIMARY;
    private final Color SENDER_COLOR = new Color(52, 152, 219);
    private final Color COURIER_COLOR = new Color(46, 204, 113);
    private final Color RED_BTN = new Color(220, 20, 60);
    
    public Login() {
        setTitle("LogiXpress - Logistics Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        getContentPane().setBackground(ORANGE_WHITE);
        
        // 直接调用初始化方法
        initUI();
        
        getRootPane().registerKeyboardAction(e -> System.exit(0),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // ========== 顶部导航栏 ==========
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ADMIN_COLOR);
        topBar.setPreferredSize(new Dimension(getWidth(), 70));
        
        JLabel logoLabel = new JLabel("LogiXpress");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 0));
        topBar.add(logoLabel, BorderLayout.WEST);
        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        topRightPanel.setBackground(ADMIN_COLOR);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timeLabel.setForeground(Color.WHITE);
        topRightPanel.add(timeLabel);
        new Timer(1000, e -> timeLabel.setText(
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))).start();
        
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(RED_BTN);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setPreferredSize(new Dimension(80, 35));
        exitBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit LogiXpress?", "Exit", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        topRightPanel.add(exitBtn);
        topBar.add(topRightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        
        // ========== 底部版权栏 ==========
        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(ORANGE_WHITE);
        bottomBar.setPreferredSize(new Dimension(getWidth(), 30));
        JLabel copyrightLabel = new JLabel("© 2024 LogiXpress Logistics Solutions. All rights reserved.");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        copyrightLabel.setForeground(Color.DARK_GRAY);
        bottomBar.add(copyrightLabel);
        add(bottomBar, BorderLayout.SOUTH);
        
        // ========== 中央主面板 ==========
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(ORANGE_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        // ========== 角色按钮 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(ORANGE_WHITE);
        
        JButton adminBtn = createButton("ADMIN", ADMIN_COLOR);
        JButton senderBtn = createButton("SENDER", SENDER_COLOR);
        JButton courierBtn = createButton("COURIER", COURIER_COLOR);
        
        buttonPanel.add(adminBtn);
        buttonPanel.add(senderBtn);
        buttonPanel.add(courierBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // ========== 卡片面板 ==========
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(ORANGE_WHITE);
        mainCardPanel.setPreferredSize(new Dimension(600, 500));
        
        // 创建登录面板
        mainCardPanel.add(createAdminPanel(), "ADMIN");
        mainCardPanel.add(createSenderPanel(), "SENDER");
        mainCardPanel.add(createCourierPanel(), "COURIER");
        
        // 居中显示
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(ORANGE_WHITE);
        centerPanel.add(mainCardPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // ========== 按钮事件 ==========
        adminBtn.addActionListener(e -> {
            cardLayout.show(mainCardPanel, "ADMIN");
            updateButtonColors(adminBtn, senderBtn, courierBtn, ADMIN_COLOR);
        });
        
        senderBtn.addActionListener(e -> {
            cardLayout.show(mainCardPanel, "SENDER");
            updateButtonColors(senderBtn, adminBtn, courierBtn, SENDER_COLOR);
        });
        
        courierBtn.addActionListener(e -> {
            cardLayout.show(mainCardPanel, "COURIER");
            updateButtonColors(courierBtn, adminBtn, senderBtn, COURIER_COLOR);
        });
        
        // 默认选中ADMIN
        updateButtonColors(adminBtn, senderBtn, courierBtn, ADMIN_COLOR);
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(150, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("color", color);
        return btn;
    }
    
    private void updateButtonColors(JButton selected, JButton btn1, JButton btn2, Color color) {
        selected.setBackground(color);
        selected.setForeground(Color.WHITE);
        selected.setBorderPainted(false);
        
        btn1.setBackground(ORANGE_WHITE);
        btn1.setForeground(Color.BLACK);
        btn1.setBorder(BorderFactory.createLineBorder((Color)btn1.getClientProperty("color"), 2));
        
        btn2.setBackground(ORANGE_WHITE);
        btn2.setForeground(Color.BLACK);
        btn2.setBorder(BorderFactory.createLineBorder((Color)btn2.getClientProperty("color"), 2));
    }
    
    // ========== ADMIN PANEL ==========
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ADMIN_COLOR, 2),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(ADMIN_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        // Username
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        userIdField = new JTextField(15);
        userIdField.setPreferredSize(new Dimension(200, 35));
        panel.add(userIdField, gbc);
        
        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        passwordField = new JPasswordField(15);
        passwordField.setPreferredSize(new Dimension(150, 35));
        passwordField.setEchoChar('•');
        
        showPasswordCheckBox = new JCheckBox("Show");
        showPasswordCheckBox.setBackground(Color.WHITE);
        showPasswordCheckBox.addActionListener(e -> 
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(passwordField, BorderLayout.CENTER);
        pwdPanel.add(showPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        // Login button
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 18));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(ADMIN_COLOR);
        loginBtn.setPreferredSize(new Dimension(200, 45));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processAdminLogin());
        panel.add(loginBtn, gbc);
        
        // Forgot password
        gbc.gridy = 4; gbc.insets = new Insets(5, 10, 10, 10);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        forgotLabel.setForeground(ADMIN_COLOR);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(forgotLabel, gbc);
        
        return panel;
    }
    
    // ========== SENDER PANEL ==========
    private JPanel createSenderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(SENDER_COLOR, 2));
        
        senderTabbedPane = new JTabbedPane();
        senderTabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Login Tab
        JPanel loginPanel = createSenderLoginPanel();
        senderTabbedPane.addTab("LOGIN", loginPanel);
        
        // Register Tab
        JScrollPane registerScroll = new JScrollPane(createSenderRegisterPanel());
        registerScroll.setBorder(BorderFactory.createEmptyBorder());
        senderTabbedPane.addTab("REGISTER", registerScroll);
        
        panel.add(senderTabbedPane, BorderLayout.CENTER);
        
        // Stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(new JLabel("Registered Senders: " + senderDatabase.size()));
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSenderLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("SENDER LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(SENDER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        // User ID
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("User ID:"), gbc);
        
        gbc.gridx = 1;
        senderUserIdField = new JTextField(15);
        senderUserIdField.setPreferredSize(new Dimension(200, 35));
        panel.add(senderUserIdField, gbc);
        
        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        senderPasswordField = new JPasswordField(15);
        senderPasswordField.setPreferredSize(new Dimension(150, 35));
        senderPasswordField.setEchoChar('•');
        
        senderShowPasswordCheckBox = new JCheckBox("Show");
        senderShowPasswordCheckBox.setBackground(Color.WHITE);
        senderShowPasswordCheckBox.addActionListener(e -> 
            senderPasswordField.setEchoChar(senderShowPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(senderPasswordField, BorderLayout.CENTER);
        pwdPanel.add(senderShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        // Login button
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 18));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(SENDER_COLOR);
        loginBtn.setPreferredSize(new Dimension(200, 45));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processSenderLogin());
        panel.add(loginBtn, gbc);
        
        // Forgot password
        gbc.gridy = 4; gbc.insets = new Insets(5, 10, 2, 10);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        forgotLabel.setForeground(SENDER_COLOR);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(forgotLabel, gbc);
        
        // Register link
        gbc.gridy = 5; gbc.insets = new Insets(2, 10, 10, 10);
        JLabel registerLabel = new JLabel("New User? Register Here");
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerLabel.setForeground(SENDER_COLOR);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                senderTabbedPane.setSelectedIndex(1);
            }
        });
        panel.add(registerLabel, gbc);
        
        return panel;
    }
    
    private JPanel createSenderRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("CREATE ACCOUNT", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(SENDER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        // Initialize fields
        senderRegNameField = new JTextField(15);
        senderRegEmailField = new JTextField(15);
        senderRegPhoneField = new JTextField(15);
        senderRegUserIdField = new JTextField(15);
        senderRegPasswordField = new JPasswordField(15);
        senderRegConfirmPwdField = new JPasswordField(15);
        
        // Full Name
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        senderRegNameField.setPreferredSize(new Dimension(200, 30));
        panel.add(senderRegNameField, gbc);
        
        // Email
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        senderRegEmailField.setPreferredSize(new Dimension(200, 30));
        panel.add(senderRegEmailField, gbc);
        
        // Phone
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Phone:*"), gbc);
        gbc.gridx = 1;
        senderRegPhoneField.setPreferredSize(new Dimension(200, 30));
        panel.add(senderRegPhoneField, gbc);
        
        // User ID
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("User ID:*"), gbc);
        gbc.gridx = 1;
        senderRegUserIdField.setPreferredSize(new Dimension(200, 30));
        panel.add(senderRegUserIdField, gbc);
        
        // Password
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Password:*"), gbc);
        gbc.gridx = 1;
        senderRegPasswordField.setPreferredSize(new Dimension(200, 30));
        senderRegPasswordField.setEchoChar('•');
        panel.add(senderRegPasswordField, gbc);
        
        // Confirm Password
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("Confirm:*"), gbc);
        gbc.gridx = 1;
        senderRegConfirmPwdField.setPreferredSize(new Dimension(200, 30));
        senderRegConfirmPwdField.setEchoChar('•');
        panel.add(senderRegConfirmPwdField, gbc);
        
        // Register button
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton regBtn = new JButton("REGISTER");
        regBtn.setFont(new Font("Arial", Font.BOLD, 18));
        regBtn.setForeground(Color.WHITE);
        regBtn.setBackground(SENDER_COLOR);
        regBtn.setPreferredSize(new Dimension(200, 45));
        regBtn.setBorderPainted(false);
        regBtn.addActionListener(e -> processSenderRegistration());
        panel.add(regBtn, gbc);
        
        return panel;
    }
    
    // ========== COURIER PANEL ==========
    private JPanel createCourierPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(COURIER_COLOR, 2));
        
        courierTabbedPane = new JTabbedPane();
        courierTabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Login Tab
        JPanel loginPanel = createCourierLoginPanel();
        courierTabbedPane.addTab("LOGIN", loginPanel);
        
        // Register Tab
        JScrollPane registerScroll = new JScrollPane(createCourierRegisterPanel());
        registerScroll.setBorder(BorderFactory.createEmptyBorder());
        courierTabbedPane.addTab("APPLY", registerScroll);
        
        panel.add(courierTabbedPane, BorderLayout.CENTER);
        
        // Stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(new JLabel("Registered Couriers: " + courierDatabase.size()));
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCourierLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("COURIER LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(COURIER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        // Courier ID
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Courier ID:"), gbc);
        
        gbc.gridx = 1;
        courierUserIdField = new JTextField(15);
        courierUserIdField.setPreferredSize(new Dimension(200, 35));
        panel.add(courierUserIdField, gbc);
        
        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        courierPasswordField = new JPasswordField(15);
        courierPasswordField.setPreferredSize(new Dimension(150, 35));
        courierPasswordField.setEchoChar('•');
        
        courierShowPasswordCheckBox = new JCheckBox("Show");
        courierShowPasswordCheckBox.setBackground(Color.WHITE);
        courierShowPasswordCheckBox.addActionListener(e -> 
            courierPasswordField.setEchoChar(courierShowPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(courierPasswordField, BorderLayout.CENTER);
        pwdPanel.add(courierShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        // Login button
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 18));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(COURIER_COLOR);
        loginBtn.setPreferredSize(new Dimension(200, 45));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processCourierLogin());
        panel.add(loginBtn, gbc);
        
        // Forgot password
        gbc.gridy = 4; gbc.insets = new Insets(5, 10, 2, 10);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        forgotLabel.setForeground(COURIER_COLOR);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(forgotLabel, gbc);
        
        // Apply link
        gbc.gridy = 5; gbc.insets = new Insets(2, 10, 10, 10);
        JLabel applyLabel = new JLabel("Apply as Courier");
        applyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        applyLabel.setForeground(COURIER_COLOR);
        applyLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                courierTabbedPane.setSelectedIndex(1);
            }
        });
        panel.add(applyLabel, gbc);
        
        return panel;
    }
    
    private JPanel createCourierRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("APPLY AS COURIER", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(COURIER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        // Initialize fields
        courierRegNameField = new JTextField(15);
        courierRegEmailField = new JTextField(15);
        courierRegPhoneField = new JTextField(15);
        courierRegLicenseField = new JTextField(15);
        courierRegVehicleTypeField = new JTextField(15);
        courierRegVehiclePlateField = new JTextField(15);
        courierRegUserIdField = new JTextField(15);
        courierRegPasswordField = new JPasswordField(15);
        courierRegConfirmPwdField = new JPasswordField(15);
        
        // Full Name
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        courierRegNameField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegNameField, gbc);
        
        // Email
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        courierRegEmailField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegEmailField, gbc);
        
        // Phone
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Phone:*"), gbc);
        gbc.gridx = 1;
        courierRegPhoneField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegPhoneField, gbc);
        
        // License
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("License:*"), gbc);
        gbc.gridx = 1;
        courierRegLicenseField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegLicenseField, gbc);
        
        // Vehicle Type
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Vehicle Type:*"), gbc);
        gbc.gridx = 1;
        courierRegVehicleTypeField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegVehicleTypeField, gbc);
        
        // Vehicle Plate
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("Vehicle Plate:*"), gbc);
        gbc.gridx = 1;
        courierRegVehiclePlateField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegVehiclePlateField, gbc);
        
        // User ID
        gbc.gridy = 7; gbc.gridx = 0;
        panel.add(new JLabel("User ID:*"), gbc);
        gbc.gridx = 1;
        courierRegUserIdField.setPreferredSize(new Dimension(200, 30));
        panel.add(courierRegUserIdField, gbc);
        
        // Password
        gbc.gridy = 8; gbc.gridx = 0;
        panel.add(new JLabel("Password:*"), gbc);
        gbc.gridx = 1;
        courierRegPasswordField.setPreferredSize(new Dimension(200, 30));
        courierRegPasswordField.setEchoChar('•');
        panel.add(courierRegPasswordField, gbc);
        
        // Confirm
        gbc.gridy = 9; gbc.gridx = 0;
        panel.add(new JLabel("Confirm:*"), gbc);
        gbc.gridx = 1;
        courierRegConfirmPwdField.setPreferredSize(new Dimension(200, 30));
        courierRegConfirmPwdField.setEchoChar('•');
        panel.add(courierRegConfirmPwdField, gbc);
        
        // Submit button
        gbc.gridy = 10; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton submitBtn = new JButton("SUBMIT APPLICATION");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(COURIER_COLOR);
        submitBtn.setPreferredSize(new Dimension(220, 45));
        submitBtn.setBorderPainted(false);
        submitBtn.addActionListener(e -> processCourierRegistration());
        panel.add(submitBtn, gbc);
        
        return panel;
    }
    
    // ========== PROCESS METHODS ==========
    private void processSenderRegistration() {
        String name = senderRegNameField.getText().trim();
        String email = senderRegEmailField.getText().trim();
        String phone = senderRegPhoneField.getText().trim();
        String userId = senderRegUserIdField.getText().trim();
        String password = new String(senderRegPasswordField.getPassword());
        String confirmPwd = new String(senderRegConfirmPwdField.getPassword());
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPwd)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (senderDatabase.containsKey(userId)) {
            JOptionPane.showMessageDialog(this, "User ID already exists!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SenderAccount newSender = new SenderAccount(name, email, phone, userId, password);
        senderDatabase.put(userId, newSender);
        saveSenderData();
        
        JOptionPane.showMessageDialog(this, "Registration successful! You can now login.", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear fields
        senderRegNameField.setText("");
        senderRegEmailField.setText("");
        senderRegPhoneField.setText("");
        senderRegUserIdField.setText("");
        senderRegPasswordField.setText("");
        senderRegConfirmPwdField.setText("");
        
        // Switch to login tab
        senderTabbedPane.setSelectedIndex(0);
        senderUserIdField.setText(userId);
    }
    
    private void processCourierRegistration() {
        String name = courierRegNameField.getText().trim();
        String email = courierRegEmailField.getText().trim();
        String phone = courierRegPhoneField.getText().trim();
        String license = courierRegLicenseField.getText().trim();
        String vehicleType = courierRegVehicleTypeField.getText().trim();
        String vehiclePlate = courierRegVehiclePlateField.getText().trim();
        String userId = courierRegUserIdField.getText().trim();
        String password = new String(courierRegPasswordField.getPassword());
        String confirmPwd = new String(courierRegConfirmPwdField.getPassword());
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || license.isEmpty() || 
            vehicleType.isEmpty() || vehiclePlate.isEmpty() || userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPwd)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (courierDatabase.containsKey(userId)) {
            JOptionPane.showMessageDialog(this, "User ID already exists!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        CourierAccount newCourier = new CourierAccount(name, email, phone, license, vehicleType, vehiclePlate, userId, password);
        courierDatabase.put(userId, newCourier);
        saveCourierData();
        
        JOptionPane.showMessageDialog(this, 
            "Application submitted! Pending admin approval.", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear fields
        courierRegNameField.setText("");
        courierRegEmailField.setText("");
        courierRegPhoneField.setText("");
        courierRegLicenseField.setText("");
        courierRegVehicleTypeField.setText("");
        courierRegVehiclePlateField.setText("");
        courierRegUserIdField.setText("");
        courierRegPasswordField.setText("");
        courierRegConfirmPwdField.setText("");
        
        // Switch to login tab
        courierTabbedPane.setSelectedIndex(0);
    }
    
    private void processSenderLogin() {
        String userId = senderUserIdField.getText().trim();
        String password = new String(senderPasswordField.getPassword());
        
        if (userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter User ID!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (senderDatabase.containsKey(userId)) {
            SenderAccount account = senderDatabase.get(userId);
            if (verifyPassword(password, account.passwordHash)) {
                account.lastLogin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                saveSenderData();
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + account.fullName + "!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                senderUserIdField.setText("");
                senderPasswordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                senderPasswordField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "User ID not found!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            senderPasswordField.setText("");
        }
    }
    
    private void processCourierLogin() {
        String userId = courierUserIdField.getText().trim();
        String password = new String(courierPasswordField.getPassword());
        
        if (userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Courier ID!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (courierDatabase.containsKey(userId)) {
            CourierAccount account = courierDatabase.get(userId);
            if (verifyPassword(password, account.passwordHash)) {
                if (!"Approved".equals(account.status)) {
                    JOptionPane.showMessageDialog(this, "Application pending approval!", 
                        "Error", JOptionPane.WARNING_MESSAGE);
                    courierPasswordField.setText("");
                    return;
                }
                account.lastLogin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                saveCourierData();
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + account.fullName + "!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                courierUserIdField.setText("");
                courierPasswordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                courierPasswordField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Courier ID not found!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            courierPasswordField.setText("");
        }
    }
    
    private void processAdminLogin() {
        String username = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if ("admin".equals(username) && "admin123".equals(password)) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome, Administrator.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            new AdminDashboard().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 确保在事件调度线程中创建GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}