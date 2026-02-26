package logistics.login;

import javax.swing.*;
import javax.swing.Timer;

import logistics.login.admin.AdminDashboard;
import sender.SenderDashboard;

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
        private static final long serialVersionUID = 2L; // Updated version
        String fullName;
        String email;
        String phone;
        String licenseType;
        String icNumber;
        String licensePhotoPath;
        String icPhotoPath;
        String userId;
        String passwordHash;
        String registrationDate;
        String lastLogin;
        String status;
        
        public CourierAccount(String fullName, String email, String phone, String userId, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.userId = userId;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.lastLogin = null;
            this.status = "Pending Approval";
            // Initialize new fields
            this.licenseType = "";
            this.icNumber = "";
            this.licensePhotoPath = "";
            this.icPhotoPath = "";
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
                "Demo Courier", "courier@example.com", "0987654321", "courier", "courier123");
            courier.status = "Approved";
            courier.licenseType = "D";
            courier.icNumber = "123456-12-1234";
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
    
    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    private JTabbedPane senderTabbedPane;
    private JTabbedPane courierTabbedPane;
    
    // ========== 颜色定义 ==========
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color ORANGE_WHITE = new Color(255, 245, 235);
    private final Color WHITE_PURE = Color.WHITE;
    private final Color BLACK_TEXT = Color.BLACK;
    private final Color GREY_BUTTON = new Color(128, 128, 128);
    
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
        forgotLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                processSenderForgotPassword();
            }
        });
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
        
        // New fields for license type, IC number, and file upload buttons
        JComboBox<String> licenseTypeCombo = new JComboBox<>(new String[]{
            "A", "A1", "B", "B1", "B2", "C", "D", "DA"
        });
        JTextField icNumberField = new JTextField(15);
        JButton uploadLicenseBtn = new JButton("Upload License Photo");
        JButton uploadICBtn = new JButton("Upload IC Photo");
        JLabel licenseFileNameLabel = new JLabel("No file chosen");
        JLabel icFileNameLabel = new JLabel("No file chosen");
        
        // Style the upload buttons - GREY
        uploadLicenseBtn.setBackground(GREY_BUTTON);
        uploadLicenseBtn.setForeground(Color.WHITE);
        uploadLicenseBtn.setFocusPainted(false);
        uploadLicenseBtn.setBorderPainted(false);
        
        uploadICBtn.setBackground(GREY_BUTTON);
        uploadICBtn.setForeground(Color.WHITE);
        uploadICBtn.setFocusPainted(false);
        uploadICBtn.setBorderPainted(false);
        
        licenseFileNameLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        icFileNameLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        
        // File selection variables
        final String[] licensePhotoPath = {null};
        final String[] icPhotoPath = {null};
        
        // License photo upload action
        uploadLicenseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (jpg, png, jpeg)", "jpg", "png", "jpeg"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                licensePhotoPath[0] = selectedFile.getAbsolutePath();
                licenseFileNameLabel.setText(selectedFile.getName());
                licenseFileNameLabel.setForeground(new Color(0, 100, 0));
            }
        });
        
        // IC photo upload action
        uploadICBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (jpg, png, jpeg)", "jpg", "png", "jpeg"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                icPhotoPath[0] = selectedFile.getAbsolutePath();
                icFileNameLabel.setText(selectedFile.getName());
                icFileNameLabel.setForeground(new Color(0, 100, 0));
            }
        });
        
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
        
        // IC Number
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("IC Number:*"), gbc);
        gbc.gridx = 1;
        icNumberField.setPreferredSize(new Dimension(200, 30));
        panel.add(icNumberField, gbc);
        
        // IC Photo Upload
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Upload IC:*"), gbc);
        gbc.gridx = 1;
        JPanel icUploadPanel = new JPanel(new BorderLayout(5, 0));
        icUploadPanel.setBackground(Color.WHITE);
        icUploadPanel.add(uploadICBtn, BorderLayout.WEST);
        icUploadPanel.add(icFileNameLabel, BorderLayout.CENTER);
        panel.add(icUploadPanel, gbc);
        
        // License Type
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("License Type:*"), gbc);
        gbc.gridx = 1;
        licenseTypeCombo.setPreferredSize(new Dimension(200, 30));
        panel.add(licenseTypeCombo, gbc);
        
        // License Photo Upload
        gbc.gridy = 7; gbc.gridx = 0;
        panel.add(new JLabel("Upload License:*"), gbc);
        gbc.gridx = 1;
        JPanel licenseUploadPanel = new JPanel(new BorderLayout(5, 0));
        licenseUploadPanel.setBackground(Color.WHITE);
        licenseUploadPanel.add(uploadLicenseBtn, BorderLayout.WEST);
        licenseUploadPanel.add(licenseFileNameLabel, BorderLayout.CENTER);
        panel.add(licenseUploadPanel, gbc);
        
        // Submit button
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton submitBtn = new JButton("SUBMIT APPLICATION");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(COURIER_COLOR);
        submitBtn.setPreferredSize(new Dimension(220, 45));
        submitBtn.setBorderPainted(false);
        submitBtn.addActionListener(e -> {
            processCourierRegistration(
                licenseTypeCombo.getSelectedItem().toString(),
                icNumberField.getText().trim(),
                licensePhotoPath[0],
                icPhotoPath[0]
            );
        });
        panel.add(submitBtn, gbc);
        
        return panel;
    }

    private void processSenderForgotPassword() {
        // Create a dialog for password recovery
        JDialog forgotDialog = new JDialog(this, "Forgot Password", true);
        forgotDialog.setSize(400, 350);
        forgotDialog.setLocationRelativeTo(this);
        forgotDialog.setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Reset Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(SENDER_COLOR);
        mainPanel.add(titleLabel, gbc);
        
        // Instructions
        gbc.gridy = 1;
        JLabel instructionLabel = new JLabel("<html>Enter your User ID and registered email address.<br>We will verify your identity.</html>", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(instructionLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // User ID field
        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(new JLabel("User ID:"), gbc);
        
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(15);
        userIdField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(userIdField, gbc);
        
        // Email field
        gbc.gridy = 3; gbc.gridx = 0;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        emailField.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(emailField, gbc);
        
        // Buttons panel
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton verifyBtn = new JButton("VERIFY");
        verifyBtn.setBackground(SENDER_COLOR);
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setFont(new Font("Arial", Font.BOLD, 14));
        verifyBtn.setPreferredSize(new Dimension(100, 35));
        verifyBtn.setBorderPainted(false);
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(e -> forgotDialog.dispose());
        
        buttonPanel.add(verifyBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, gbc);
        
        forgotDialog.add(mainPanel, BorderLayout.CENTER);
        
        // Verify button action
        verifyBtn.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String email = emailField.getText().trim();
            
            if (userId.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(forgotDialog, 
                    "Please enter both User ID and Email!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if user exists
            if (senderDatabase.containsKey(userId)) {
                SenderAccount account = senderDatabase.get(userId);
                
                // Verify email matches
                if (account.email.equals(email)) {
                    forgotDialog.dispose();
                    showResetPasswordDialog(userId);
                } else {
                    JOptionPane.showMessageDialog(forgotDialog, 
                        "Email does not match our records!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(forgotDialog, 
                    "User ID not found!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        forgotDialog.setVisible(true);
    }

    // Add this method to reset password
    private void showResetPasswordDialog(String userId) {
        JDialog resetDialog = new JDialog(this, "Reset Password", true);
        resetDialog.setSize(380, 280);
        resetDialog.setLocationRelativeTo(this);
        resetDialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Set New Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(SENDER_COLOR);
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // New Password
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("New Password:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField newPwdField = new JPasswordField(15);
        newPwdField.setPreferredSize(new Dimension(180, 30));
        panel.add(newPwdField, gbc);
        
        // Confirm Password
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Confirm:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(15);
        confirmPwdField.setPreferredSize(new Dimension(180, 30));
        panel.add(confirmPwdField, gbc);
        
        // Show password checkbox
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        JCheckBox showPwdCheck = new JCheckBox("Show Password");
        showPwdCheck.setBackground(Color.WHITE);
        showPwdCheck.addActionListener(e -> {
            char echoChar = showPwdCheck.isSelected() ? (char)0 : '•';
            newPwdField.setEchoChar(echoChar);
            confirmPwdField.setEchoChar(echoChar);
        });
        panel.add(showPwdCheck, gbc);
        
        // Buttons
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton resetBtn = new JButton("RESET PASSWORD");
        resetBtn.setBackground(SENDER_COLOR);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resetBtn.setPreferredSize(new Dimension(150, 35));
        resetBtn.setBorderPainted(false);
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(e -> resetDialog.dispose());
        
        buttonPanel.add(resetBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        resetDialog.add(panel, BorderLayout.CENTER);
        
        // Reset button action
        resetBtn.addActionListener(e -> {
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());
            
            if (newPwd.isEmpty() || confirmPwd.isEmpty()) {
                JOptionPane.showMessageDialog(resetDialog, 
                    "Please enter and confirm your new password!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(resetDialog, 
                    "Passwords do not match!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPwd.length() < 6) {
                JOptionPane.showMessageDialog(resetDialog, 
                    "Password must be at least 6 characters long!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update password
            SenderAccount account = senderDatabase.get(userId);
            account.passwordHash = hashPassword(newPwd);
            saveSenderData();
            
            JOptionPane.showMessageDialog(resetDialog, 
                "Password reset successful!\nYou can now login with your new password.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            resetDialog.dispose();
            
            // Clear login fields and set user ID
            senderUserIdField.setText(userId);
            senderPasswordField.setText("");
        });
        
        resetDialog.setVisible(true);
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
    
    private void processCourierRegistration(String licenseType, String icNumber, String licensePhotoPath, String icPhotoPath) {
        String name = courierRegNameField.getText().trim();
        String email = courierRegEmailField.getText().trim();
        String phone = courierRegPhoneField.getText().trim();
        
        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || icNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate phone number (basic validation)
        if (phone.length() < 10 || phone.length() > 15) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate IC number format (basic validation)
        if (!icNumber.matches("\\d{6}-\\d{2}-\\d{4}") && !icNumber.matches("\\d{12}")) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "IC number format may be incorrect. Expected format: 123456-12-1234 or 12 digits.\nContinue anyway?", 
                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Check if files are uploaded
        if (licensePhotoPath == null || icPhotoPath == null) {
            JOptionPane.showMessageDialog(this, 
                "Please upload both License photo and IC photo!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check file sizes (optional - limit to 5MB)
        File licenseFile = new File(licensePhotoPath);
        File icFile = new File(icPhotoPath);
        long maxSize = 5 * 1024 * 1024; // 5MB
        
        if (licenseFile.length() > maxSize || icFile.length() > maxSize) {
            JOptionPane.showMessageDialog(this, 
                "File size too large! Maximum size is 5MB per file.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Generate a user ID automatically (using email prefix + timestamp)
        String emailPrefix = email.substring(0, email.indexOf('@'));
        // Remove special characters from email prefix
        emailPrefix = emailPrefix.replaceAll("[^a-zA-Z0-9]", "");
        String generatedUserId = emailPrefix + System.currentTimeMillis() % 10000;
        
        // Generate a random password for initial login
        String generatedPassword = "courier" + (int)(Math.random() * 9000 + 1000);
        
        // Create account with generated credentials
        CourierAccount newCourier = new CourierAccount(
            name, email, phone, generatedUserId, generatedPassword);
        
        // Add additional fields
        newCourier.licenseType = licenseType;
        newCourier.icNumber = icNumber;
        newCourier.licensePhotoPath = licensePhotoPath;
        newCourier.icPhotoPath = icPhotoPath;
        
        courierDatabase.put(generatedUserId, newCourier);
        saveCourierData();
        
        // Show success message with generated credentials
        String message = String.format(
            "Application submitted successfully!\n\n" +
            "Your User ID: %s\n" +
            "Your Password: %s\n\n" +
            "Please save these credentials for future login.\n" +
            "Your application is pending admin approval.",
            generatedUserId, generatedPassword);
        
        JOptionPane.showMessageDialog(this, message, 
            "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear fields
        courierRegNameField.setText("");
        courierRegEmailField.setText("");
        courierRegPhoneField.setText("");
        
        // Switch to login tab and pre-fill the generated user ID
        courierTabbedPane.setSelectedIndex(0);
        courierUserIdField.setText(generatedUserId);
        courierPasswordField.setText("");
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
                
                // Close login window and open sender dashboard
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + account.fullName + "!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Open SenderDashboard with sender information
                new SenderDashboard(account.fullName, account.email).setVisible(true);
                this.dispose(); // Close the login window
                
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
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}
