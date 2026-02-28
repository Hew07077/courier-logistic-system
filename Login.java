package logistics.login;

import javax.swing.*;
import javax.swing.Timer;
import logistics.login.admin.AdminDashboard;
import sender.SenderDashboard;
import courier.CourierDashboard;
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
    private static final String ADMIN_DATA_FILE = "admin_data.dat";
    
    // ========== In-Memory Databases ==========
    private static Map<String, SenderAccount> senderDatabase = new HashMap<>();
    private static Map<String, CourierAccount> courierDatabase = new HashMap<>();
    private static Map<String, AdminAccount> adminDatabase = new HashMap<>();
    
    // ========== Account Model Classes ==========
    static class AdminAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String username, email,passwordHash,lastLogin;
        
        public AdminAccount(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.passwordHash = hashPassword(password);
            this.lastLogin = null;
        }
    }
    
    static class SenderAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String fullName,email,phone,userId,passwordHash,registrationDate,lastLogin,address,companyName,status;
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
        private static final long serialVersionUID = 2L;
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
        String remarks;
        
        public CourierAccount(String fullName, String email, String phone, String userId, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.userId = userId;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.lastLogin = null;
            this.status = "PENDING";
            this.remarks = "";
            this.licenseType = "";
            this.icNumber = "";
            this.licensePhotoPath = "";
            this.icPhotoPath = "";
        }
    }
    
    // ========== Static Initialization ==========
    static {
        loadAdminData();
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
    
    private static void loadAdminData() {
        File file = new File(ADMIN_DATA_FILE);
        if (!file.exists()) {
            adminDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            adminDatabase = (HashMap<String, AdminAccount>) ois.readObject();
        } catch (Exception e) {
            adminDatabase = new HashMap<>();
        }
    }
    
    private static void saveAdminData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ADMIN_DATA_FILE))) {
            oos.writeObject(adminDatabase);
        } catch (Exception e) {
            // Silent fail in production
        }
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
            // Silent fail in production
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
            // Silent fail in production
        }
    }
    
    private static void addDefaultAccounts() {
        // Add default admin
        if (!adminDatabase.containsKey("admin")) {
            adminDatabase.put("admin", new AdminAccount("admin", "admin@logixpress.com", "admin123"));
            saveAdminData();
        }
        
        // Add default sender
        if (!senderDatabase.containsKey("sender")) {
            senderDatabase.put("sender", new SenderAccount(
                "Demo Sender", "sender@example.com", "1234567890", "sender", "sender123"));
            saveSenderData();
        }
        
        // Add default courier
        if (!courierDatabase.containsKey("courier")) {
            CourierAccount courier = new CourierAccount(
                "Demo Courier", "courier@example.com", "0987654321", "courier", "courier123");
            courier.status = "APPROVED";
            courier.licenseType = "D";
            courier.icNumber = "123456-12-1234";
            courierDatabase.put("courier", courier);
            saveCourierData();
        }
        
        // Add pending courier
        if (!courierDatabase.containsKey("pending_courier")) {
            CourierAccount pending = new CourierAccount(
                "Pending User", "pending@example.com", "0123456789", "pending_courier", "pending123");
            pending.status = "PENDING";
            pending.licenseType = "B";
            pending.icNumber = "987654-12-5678";
            courierDatabase.put("pending_courier", pending);
            saveCourierData();
        }
        
        // Add rejected courier
        if (!courierDatabase.containsKey("rejected_courier")) {
            CourierAccount rejected = new CourierAccount(
                "Rejected User", "rejected@example.com", "0112233445", "rejected_courier", "rejected123");
            rejected.status = "REJECTED";
            rejected.remarks = "Invalid license document";
            rejected.licenseType = "C";
            rejected.icNumber = "555555-12-8888";
            courierDatabase.put("rejected_courier", rejected);
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
    
    // ========== Color Definitions ==========
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color ORANGE_WHITE = new Color(255, 245, 235);
    private final Color WHITE_PURE = Color.WHITE;
    private final Color BLACK_TEXT = Color.BLACK;
    private final Color GREY_BUTTON = new Color(128, 128, 128);
    private final Color GREEN_SUCCESS = new Color(46, 204, 113);
    private final Color RED_ERROR = new Color(231, 76, 60);
    private final Color YELLOW_PENDING = new Color(241, 196, 15);
    
    private final Color ADMIN_COLOR = ORANGE_PRIMARY;
    private final Color SENDER_COLOR = new Color(52, 152, 219);
    private final Color COURIER_COLOR = new Color(46, 204, 113);
    private final Color RED_BTN = new Color(220, 20, 60);
    
    // ========== Font Settings ==========
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 32);
    private final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 16);
    private final Font FIELD_FONT = new Font("Arial", Font.PLAIN, 16);
    private final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);
    private final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 14);
    private final Font TAB_FONT = new Font("Arial", Font.BOLD, 16);
    
    // ========== User Type Enum for Forgot Password ==========
    private enum UserType {
        ADMIN, SENDER, COURIER
    }
    
    public Login() {
        setTitle("LogiXpress - Logistics Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        getContentPane().setBackground(ORANGE_WHITE);
        
        initUI();
        
        getRootPane().registerKeyboardAction(e -> System.exit(0),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // ========== Top Navigation Bar ==========
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ADMIN_COLOR);
        topBar.setPreferredSize(new Dimension(getWidth(), 80));
        
        // ===== Logo Panel =====
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        logoPanel.setBackground(ADMIN_COLOR);
        
        // Try to load logo from file
        try {
            String logoPath = "C:\\Users\\User\\Documents\\LOGISTICS\\logo1.png";
            
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                ImageIcon logoIcon = new ImageIcon(logoPath);
                Image scaledImage = logoIcon.getImage().getScaledInstance(180, 50, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                logoPanel.add(logoLabel);
            } else {
                JLabel fallbackLabel = new JLabel("LogiXpress");
                fallbackLabel.setFont(new Font("Arial", Font.BOLD, 32));
                fallbackLabel.setForeground(Color.WHITE);
                logoPanel.add(fallbackLabel);
            }
        } catch (Exception e) {
            JLabel fallbackLabel = new JLabel("LogiXpress");
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 32));
            fallbackLabel.setForeground(Color.WHITE);
            logoPanel.add(fallbackLabel);
        }
        
        topBar.add(logoPanel, BorderLayout.WEST);
        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        topRightPanel.setBackground(ADMIN_COLOR);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        timeLabel.setForeground(Color.WHITE);
        topRightPanel.add(timeLabel);
        new Timer(1000, e -> timeLabel.setText(
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))).start();
        
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(RED_BTN);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setPreferredSize(new Dimension(100, 45));
        exitBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit LogiXpress?", "Exit", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        topRightPanel.add(exitBtn);
        topBar.add(topRightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        
        // ========== Bottom Copyright Bar ==========
        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(ORANGE_WHITE);
        bottomBar.setPreferredSize(new Dimension(getWidth(), 40));
        JLabel copyrightLabel = new JLabel("© 2024 LogiXpress Logistics Solutions. All rights reserved.");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        copyrightLabel.setForeground(Color.DARK_GRAY);
        bottomBar.add(copyrightLabel);
        add(bottomBar, BorderLayout.SOUTH);
        
        // ========== Main Center Panel ==========
        JPanel mainPanel = new JPanel(new BorderLayout(0, 30));
        mainPanel.setBackground(ORANGE_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));
        
        // ========== Role Buttons ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonPanel.setBackground(ORANGE_WHITE);
        
        JButton adminBtn = createButton("ADMIN", ADMIN_COLOR);
        JButton senderBtn = createButton("SENDER", SENDER_COLOR);
        JButton courierBtn = createButton("COURIER", COURIER_COLOR);
        
        buttonPanel.add(adminBtn);
        buttonPanel.add(senderBtn);
        buttonPanel.add(courierBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // ========== Card Panel ==========
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(ORANGE_WHITE);
        mainCardPanel.setPreferredSize(new Dimension(700, 600));
        
        mainCardPanel.add(createAdminPanel(), "ADMIN");
        mainCardPanel.add(createSenderPanel(), "SENDER");
        mainCardPanel.add(createCourierPanel(), "COURIER");
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(ORANGE_WHITE);
        centerPanel.add(mainCardPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
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
        
        updateButtonColors(adminBtn, senderBtn, courierBtn, ADMIN_COLOR);
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setPreferredSize(new Dimension(180, 60));
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
        btn1.setBorder(BorderFactory.createLineBorder((Color)btn1.getClientProperty("color"), 3));
        
        btn2.setBackground(ORANGE_WHITE);
        btn2.setForeground(Color.BLACK);
        btn2.setBorder(BorderFactory.createLineBorder((Color)btn2.getClientProperty("color"), 3));
    }
    
    // ========== ADMIN PANEL ==========
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ADMIN_COLOR, 3),
            BorderFactory.createEmptyBorder(50, 60, 50, 60)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(ADMIN_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(LABEL_FONT);
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        userIdField = new JTextField(15);
        userIdField.setFont(FIELD_FONT);
        userIdField.setPreferredSize(new Dimension(250, 40));
        panel.add(userIdField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(10, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        passwordField = new JPasswordField(15);
        passwordField.setFont(FIELD_FONT);
        passwordField.setPreferredSize(new Dimension(180, 40));
        passwordField.setEchoChar('•');
        
        showPasswordCheckBox = new JCheckBox("Show");
        showPasswordCheckBox.setFont(SMALL_FONT);
        showPasswordCheckBox.setBackground(Color.WHITE);
        showPasswordCheckBox.addActionListener(e -> 
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(passwordField, BorderLayout.CENTER);
        pwdPanel.add(showPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 15, 15);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(BUTTON_FONT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(ADMIN_COLOR);
        loginBtn.setPreferredSize(new Dimension(250, 50));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processAdminLogin());
        panel.add(loginBtn, gbc);
        
        gbc.gridy = 4; gbc.insets = new Insets(10, 15, 15, 15);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(SMALL_FONT);
        forgotLabel.setForeground(ADMIN_COLOR);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog(UserType.ADMIN);
            }
        });
        panel.add(forgotLabel, gbc);
        
        return panel;
    }
    
    // ========== SENDER PANEL ==========
    private JPanel createSenderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(SENDER_COLOR, 3));
        
        senderTabbedPane = new JTabbedPane();
        senderTabbedPane.setFont(TAB_FONT);
        
        JPanel loginPanel = createSenderLoginPanel();
        senderTabbedPane.addTab("LOGIN", loginPanel);
        
        JScrollPane registerScroll = new JScrollPane(createSenderRegisterPanel());
        registerScroll.setBorder(BorderFactory.createEmptyBorder());
        senderTabbedPane.addTab("REGISTER", registerScroll);
        
        panel.add(senderTabbedPane, BorderLayout.CENTER);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        JLabel statsLabel = new JLabel("Registered Senders: " + senderDatabase.size());
        statsLabel.setFont(SMALL_FONT);
        statsPanel.add(statsLabel);
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSenderLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("SENDER LOGIN", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(SENDER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("User ID:");
        userLabel.setFont(LABEL_FONT);
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        senderUserIdField = new JTextField(15);
        senderUserIdField.setFont(FIELD_FONT);
        senderUserIdField.setPreferredSize(new Dimension(250, 40));
        panel.add(senderUserIdField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(10, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        senderPasswordField = new JPasswordField(15);
        senderPasswordField.setFont(FIELD_FONT);
        senderPasswordField.setPreferredSize(new Dimension(180, 40));
        senderPasswordField.setEchoChar('•');
        
        senderShowPasswordCheckBox = new JCheckBox("Show");
        senderShowPasswordCheckBox.setFont(SMALL_FONT);
        senderShowPasswordCheckBox.setBackground(Color.WHITE);
        senderShowPasswordCheckBox.addActionListener(e -> 
            senderPasswordField.setEchoChar(senderShowPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(senderPasswordField, BorderLayout.CENTER);
        pwdPanel.add(senderShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 15, 15);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(BUTTON_FONT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(SENDER_COLOR);
        loginBtn.setPreferredSize(new Dimension(250, 50));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processSenderLogin());
        panel.add(loginBtn, gbc);
        
        gbc.gridy = 4; gbc.insets = new Insets(10, 15, 5, 15);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(SMALL_FONT);
        forgotLabel.setForeground(SENDER_COLOR);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog(UserType.SENDER);
            }
        });
        panel.add(forgotLabel, gbc);
        
        gbc.gridy = 5; gbc.insets = new Insets(5, 15, 15, 15);
        JLabel registerLabel = new JLabel("New User? Register Here");
        registerLabel.setFont(SMALL_FONT);
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
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("CREATE ACCOUNT", SwingConstants.CENTER);
        title.setFont(SUBTITLE_FONT);
        title.setForeground(SENDER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        senderRegNameField = new JTextField(15);
        senderRegEmailField = new JTextField(15);
        senderRegPhoneField = new JTextField(15);
        senderRegUserIdField = new JTextField(15);
        senderRegPasswordField = new JPasswordField(15);
        senderRegConfirmPwdField = new JPasswordField(15);
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel nameLabel = new JLabel("Full Name:*");
        nameLabel.setFont(LABEL_FONT);
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        senderRegNameField.setFont(FIELD_FONT);
        senderRegNameField.setPreferredSize(new Dimension(250, 35));
        panel.add(senderRegNameField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:*");
        emailLabel.setFont(LABEL_FONT);
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        senderRegEmailField.setFont(FIELD_FONT);
        senderRegEmailField.setPreferredSize(new Dimension(250, 35));
        panel.add(senderRegEmailField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel phoneLabel = new JLabel("Phone:*");
        phoneLabel.setFont(LABEL_FONT);
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        senderRegPhoneField.setFont(FIELD_FONT);
        senderRegPhoneField.setPreferredSize(new Dimension(250, 35));
        panel.add(senderRegPhoneField, gbc);
        
        gbc.gridy = 4; gbc.gridx = 0;
        JLabel userIdLabel = new JLabel("User ID:*");
        userIdLabel.setFont(LABEL_FONT);
        panel.add(userIdLabel, gbc);
        gbc.gridx = 1;
        senderRegUserIdField.setFont(FIELD_FONT);
        senderRegUserIdField.setPreferredSize(new Dimension(250, 35));
        panel.add(senderRegUserIdField, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:*");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        gbc.gridx = 1;
        senderRegPasswordField.setFont(FIELD_FONT);
        senderRegPasswordField.setPreferredSize(new Dimension(250, 35));
        senderRegPasswordField.setEchoChar('•');
        panel.add(senderRegPasswordField, gbc);
        
        gbc.gridy = 6; gbc.gridx = 0;
        JLabel confirmLabel = new JLabel("Confirm:*");
        confirmLabel.setFont(LABEL_FONT);
        panel.add(confirmLabel, gbc);
        gbc.gridx = 1;
        senderRegConfirmPwdField.setFont(FIELD_FONT);
        senderRegConfirmPwdField.setPreferredSize(new Dimension(250, 35));
        senderRegConfirmPwdField.setEchoChar('•');
        panel.add(senderRegConfirmPwdField, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 8, 8, 8);
        
        JButton regBtn = new JButton("REGISTER");
        regBtn.setFont(BUTTON_FONT);
        regBtn.setForeground(Color.WHITE);
        regBtn.setBackground(SENDER_COLOR);
        regBtn.setPreferredSize(new Dimension(250, 50));
        regBtn.setBorderPainted(false);
        regBtn.addActionListener(e -> processSenderRegistration());
        panel.add(regBtn, gbc);
        
        return panel;
    }
    
    // ========== COURIER PANEL ==========
    private JPanel createCourierPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(COURIER_COLOR, 3));
        
        courierTabbedPane = new JTabbedPane();
        courierTabbedPane.setFont(TAB_FONT);
        
        JPanel loginPanel = createCourierLoginPanel();
        courierTabbedPane.addTab("LOGIN", loginPanel);
        
        JScrollPane registerScroll = new JScrollPane(createCourierRegisterPanel());
        registerScroll.setBorder(BorderFactory.createEmptyBorder());
        courierTabbedPane.addTab("APPLY", registerScroll);
        
        JPanel statusPanel = createCourierStatusPanel();
        courierTabbedPane.addTab("CHECK STATUS", statusPanel);
        
        panel.add(courierTabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCourierLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("COURIER LOGIN", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(COURIER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Courier ID:");
        userLabel.setFont(LABEL_FONT);
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        courierUserIdField = new JTextField(15);
        courierUserIdField.setFont(FIELD_FONT);
        courierUserIdField.setPreferredSize(new Dimension(250, 40));
        panel.add(courierUserIdField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(10, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        courierPasswordField = new JPasswordField(15);
        courierPasswordField.setFont(FIELD_FONT);
        courierPasswordField.setPreferredSize(new Dimension(180, 40));
        courierPasswordField.setEchoChar('•');
        
        courierShowPasswordCheckBox = new JCheckBox("Show");
        courierShowPasswordCheckBox.setFont(SMALL_FONT);
        courierShowPasswordCheckBox.setBackground(Color.WHITE);
        courierShowPasswordCheckBox.addActionListener(e -> 
            courierPasswordField.setEchoChar(courierShowPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(courierPasswordField, BorderLayout.CENTER);
        pwdPanel.add(courierShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 15, 15);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(BUTTON_FONT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(COURIER_COLOR);
        loginBtn.setPreferredSize(new Dimension(250, 50));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processCourierLogin());
        panel.add(loginBtn, gbc);
        
        gbc.gridy = 4; gbc.insets = new Insets(10, 15, 5, 15);
        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(SMALL_FONT);
        forgotLabel.setForeground(COURIER_COLOR);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog(UserType.COURIER);
            }
        });
        panel.add(forgotLabel, gbc);
        
        gbc.gridy = 5; gbc.insets = new Insets(5, 15, 15, 15);
        JLabel applyLabel = new JLabel("Apply as Courier");
        applyLabel.setFont(SMALL_FONT);
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
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("APPLY AS COURIER", SwingConstants.CENTER);
        title.setFont(SUBTITLE_FONT);
        title.setForeground(COURIER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        courierRegNameField = new JTextField(15);
        courierRegEmailField = new JTextField(15);
        courierRegPhoneField = new JTextField(15);
        
        JComboBox<String> licenseTypeCombo = new JComboBox<>(new String[]{
            "A", "A1", "B", "B1", "B2", "C", "D", "DA"
        });
        licenseTypeCombo.setFont(FIELD_FONT);
        
        JTextField icNumberField = new JTextField(15);
        icNumberField.setFont(FIELD_FONT);
        
        JButton uploadLicenseBtn = new JButton("Upload License Photo");
        JButton uploadICBtn = new JButton("Upload IC Photo");
        JLabel licenseFileNameLabel = new JLabel("No file chosen");
        JLabel icFileNameLabel = new JLabel("No file chosen");
        
        uploadLicenseBtn.setFont(SMALL_FONT);
        uploadLicenseBtn.setBackground(GREY_BUTTON);
        uploadLicenseBtn.setForeground(Color.WHITE);
        uploadLicenseBtn.setFocusPainted(false);
        uploadLicenseBtn.setBorderPainted(false);
        uploadLicenseBtn.setPreferredSize(new Dimension(180, 35));
        
        uploadICBtn.setFont(SMALL_FONT);
        uploadICBtn.setBackground(GREY_BUTTON);
        uploadICBtn.setForeground(Color.WHITE);
        uploadICBtn.setFocusPainted(false);
        uploadICBtn.setBorderPainted(false);
        uploadICBtn.setPreferredSize(new Dimension(180, 35));
        
        licenseFileNameLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        icFileNameLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        
        final String[] licensePhotoPath = {null};
        final String[] icPhotoPath = {null};
        
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
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel nameLabel = new JLabel("Full Name:*");
        nameLabel.setFont(LABEL_FONT);
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        courierRegNameField.setFont(FIELD_FONT);
        courierRegNameField.setPreferredSize(new Dimension(250, 35));
        panel.add(courierRegNameField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:*");
        emailLabel.setFont(LABEL_FONT);
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        courierRegEmailField.setFont(FIELD_FONT);
        courierRegEmailField.setPreferredSize(new Dimension(250, 35));
        panel.add(courierRegEmailField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel phoneLabel = new JLabel("Phone:*");
        phoneLabel.setFont(LABEL_FONT);
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        courierRegPhoneField.setFont(FIELD_FONT);
        courierRegPhoneField.setPreferredSize(new Dimension(250, 35));
        panel.add(courierRegPhoneField, gbc);
        
        gbc.gridy = 4; gbc.gridx = 0;
        JLabel icLabel = new JLabel("IC Number:*");
        icLabel.setFont(LABEL_FONT);
        panel.add(icLabel, gbc);
        gbc.gridx = 1;
        icNumberField.setPreferredSize(new Dimension(250, 35));
        panel.add(icNumberField, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0;
        JLabel icUploadLabel = new JLabel("Upload IC:*");
        icUploadLabel.setFont(LABEL_FONT);
        panel.add(icUploadLabel, gbc);
        gbc.gridx = 1;
        JPanel icUploadPanel = new JPanel(new BorderLayout(10, 0));
        icUploadPanel.setBackground(Color.WHITE);
        icUploadPanel.add(uploadICBtn, BorderLayout.WEST);
        icUploadPanel.add(icFileNameLabel, BorderLayout.CENTER);
        panel.add(icUploadPanel, gbc);
        
        gbc.gridy = 6; gbc.gridx = 0;
        JLabel licenseTypeLabel = new JLabel("License Type:*");
        licenseTypeLabel.setFont(LABEL_FONT);
        panel.add(licenseTypeLabel, gbc);
        gbc.gridx = 1;
        licenseTypeCombo.setPreferredSize(new Dimension(250, 35));
        panel.add(licenseTypeCombo, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0;
        JLabel licenseUploadLabel = new JLabel("Upload License:*");
        licenseUploadLabel.setFont(LABEL_FONT);
        panel.add(licenseUploadLabel, gbc);
        gbc.gridx = 1;
        JPanel licenseUploadPanel = new JPanel(new BorderLayout(10, 0));
        licenseUploadPanel.setBackground(Color.WHITE);
        licenseUploadPanel.add(uploadLicenseBtn, BorderLayout.WEST);
        licenseUploadPanel.add(licenseFileNameLabel, BorderLayout.CENTER);
        panel.add(licenseUploadPanel, gbc);
        
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 8, 8, 8);
        
        JButton submitBtn = new JButton("SUBMIT APPLICATION");
        submitBtn.setFont(BUTTON_FONT);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(COURIER_COLOR);
        submitBtn.setPreferredSize(new Dimension(280, 50));
        submitBtn.setBorderPainted(false);
        submitBtn.addActionListener(e -> {
            processCourierRegistration(
                licenseTypeCombo.getSelectedItem().toString(),
                icNumberField.getText().trim(),
                licensePhotoPath[0],
                icPhotoPath[0]
            );
            
            licensePhotoPath[0] = null;
            icPhotoPath[0] = null;
            licenseFileNameLabel.setText("No file chosen");
            icFileNameLabel.setText("No file chosen");
            licenseFileNameLabel.setForeground(Color.BLACK);
            icFileNameLabel.setForeground(Color.BLACK);
        });
        panel.add(submitBtn, gbc);
        
        return panel;
    }
    
    private JPanel createCourierStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        JLabel titleLabel = new JLabel("CHECK APPLICATION STATUS", SwingConstants.CENTER);
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setForeground(COURIER_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Search row
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel searchLabel = new JLabel("Enter User ID:");
        searchLabel.setFont(LABEL_FONT);
        centerPanel.add(searchLabel, gbc);
        
        gbc.gridx = 1;
        JTextField searchIdField = new JTextField(15);
        searchIdField.setFont(FIELD_FONT);
        searchIdField.setPreferredSize(new Dimension(200, 40));
        centerPanel.add(searchIdField, gbc);
        
        gbc.gridx = 2;
        JButton searchBtn = new JButton("SEARCH");
        searchBtn.setFont(BUTTON_FONT);
        searchBtn.setBackground(COURIER_COLOR);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setPreferredSize(new Dimension(160, 45));
        centerPanel.add(searchBtn, gbc);
        
        // Status display panel
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.insets = new Insets(30, 15, 15, 15);
        
        JPanel statusDisplayPanel = new JPanel();
        statusDisplayPanel.setLayout(new BoxLayout(statusDisplayPanel, BoxLayout.Y_AXIS));
        statusDisplayPanel.setBackground(new Color(245, 245, 245));
        statusDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        
        JLabel statusIconLabel = new JLabel("", SwingConstants.CENTER);
        statusIconLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        statusIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel statusLabel = new JLabel("Enter User ID to check status", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel detailsLabel = new JLabel("", SwingConstants.CENTER);
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        detailsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel remarksLabel = new JLabel("", SwingConstants.CENTER);
        remarksLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        remarksLabel.setForeground(Color.RED);
        remarksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusDisplayPanel.add(statusIconLabel);
        statusDisplayPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statusDisplayPanel.add(statusLabel);
        statusDisplayPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statusDisplayPanel.add(detailsLabel);
        statusDisplayPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statusDisplayPanel.add(remarksLabel);
        
        centerPanel.add(statusDisplayPanel, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        searchBtn.addActionListener(e -> {
            String searchId = searchIdField.getText().trim();
            
            if (searchId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter User ID!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (courierDatabase.containsKey(searchId)) {
                CourierAccount account = courierDatabase.get(searchId);
                
                switch (account.status) {
                    case "APPROVED":
                        statusIconLabel.setText("✓");
                        statusIconLabel.setForeground(GREEN_SUCCESS);
                        statusLabel.setText("STATUS: APPROVED");
                        statusLabel.setForeground(GREEN_SUCCESS);
                        detailsLabel.setText("Your application has been approved!");
                        detailsLabel.setForeground(GREEN_SUCCESS);
                        remarksLabel.setText("");
                        break;
                        
                    case "REJECTED":
                        statusIconLabel.setText("✗");
                        statusIconLabel.setForeground(RED_ERROR);
                        statusLabel.setText("STATUS: REJECTED");
                        statusLabel.setForeground(RED_ERROR);
                        detailsLabel.setText("Your application has been rejected.");
                        detailsLabel.setForeground(RED_ERROR);
                        if (account.remarks != null && !account.remarks.isEmpty()) {
                            remarksLabel.setText("Reason: " + account.remarks);
                        } else {
                            remarksLabel.setText("");
                        }
                        break;
                        
                    case "PENDING":
                    default:
                        statusIconLabel.setText("⏳");
                        statusIconLabel.setForeground(YELLOW_PENDING);
                        statusLabel.setText("STATUS: PENDING APPROVAL");
                        statusLabel.setForeground(YELLOW_PENDING);
                        detailsLabel.setText("Your application is waiting for admin review.");
                        detailsLabel.setForeground(YELLOW_PENDING);
                        remarksLabel.setText("");
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "User ID not found! Please check and try again.", 
                    "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        return panel;
    }

    // ========== CONSOLIDATED FORGOT PASSWORD METHODS ==========
    
    /**
     * Unified forgot password dialog for all user types
     */
    private void showForgotPasswordDialog(UserType userType) {
        String title;
        String idLabel;
        Color themeColor;
        
        switch (userType) {
            case ADMIN:
                title = "Reset Admin Password";
                idLabel = "Username:";
                themeColor = ADMIN_COLOR;
                break;
            case SENDER:
                title = "Reset Sender Password";
                idLabel = "User ID:";
                themeColor = SENDER_COLOR;
                break;
            case COURIER:
                title = "Reset Courier Password";
                idLabel = "Courier ID:";
                themeColor = COURIER_COLOR;
                break;
            default:
                return;
        }
        
        JDialog forgotDialog = new JDialog(this, "Forgot Password", true);
        forgotDialog.setSize(500, 400);
        forgotDialog.setLocationRelativeTo(this);
        forgotDialog.setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setForeground(themeColor);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        JLabel instructionLabel = new JLabel(
            "<html><center>Enter your " + idLabel.toLowerCase() + 
            " and registered email address.<br>We will verify your identity.</center></html>", 
            SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        mainPanel.add(instructionLabel, gbc);
        
        gbc.gridwidth = 1;
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel userLabel = new JLabel(idLabel);
        userLabel.setFont(LABEL_FONT);
        mainPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        JTextField idField = new JTextField(15);
        idField.setFont(FIELD_FONT);
        idField.setPreferredSize(new Dimension(250, 40));
        mainPanel.add(idField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(LABEL_FONT);
        mainPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        emailField.setFont(FIELD_FONT);
        emailField.setPreferredSize(new Dimension(250, 40));
        mainPanel.add(emailField, gbc);
        
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 15, 15);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton verifyBtn = new JButton("VERIFY");
        verifyBtn.setFont(BUTTON_FONT);
        verifyBtn.setBackground(themeColor);
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setPreferredSize(new Dimension(130, 45));
        verifyBtn.setBorderPainted(false);
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(130, 45));
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(e -> forgotDialog.dispose());
        
        buttonPanel.add(verifyBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, gbc);
        
        forgotDialog.add(mainPanel, BorderLayout.CENTER);
        
        verifyBtn.addActionListener(e -> {
            String userId = idField.getText().trim();
            String email = emailField.getText().trim();
            
            if (userId.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(forgotDialog, 
                    "Please enter both " + idLabel + " and Email!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verify user based on type
            boolean verified = false;
            
            switch (userType) {
                case ADMIN:
                    if (adminDatabase.containsKey(userId)) {
                        AdminAccount account = adminDatabase.get(userId);
                        if (account.email.equals(email)) {
                            verified = true;
                            forgotDialog.dispose();
                            showResetPasswordDialog(userId, userType);
                        }
                    }
                    break;
                    
                case SENDER:
                    if (senderDatabase.containsKey(userId)) {
                        SenderAccount account = senderDatabase.get(userId);
                        if (account.email.equals(email)) {
                            verified = true;
                            forgotDialog.dispose();
                            showResetPasswordDialog(userId, userType);
                        }
                    }
                    break;
                    
                case COURIER:
                    if (courierDatabase.containsKey(userId)) {
                        CourierAccount account = courierDatabase.get(userId);
                        if (account.email.equals(email)) {
                            verified = true;
                            forgotDialog.dispose();
                            showResetPasswordDialog(userId, userType);
                        }
                    }
                    break;
            }
            
            if (!verified) {
                JOptionPane.showMessageDialog(forgotDialog, 
                    "Invalid credentials! Please check your " + idLabel.toLowerCase() + " and email.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        forgotDialog.setVisible(true);
    }

    /**
     * Unified reset password dialog for all user types
     */
    private void showResetPasswordDialog(String userId, UserType userType) {
        String title;
        Color themeColor;
        
        switch (userType) {
            case ADMIN:
                title = "Reset Admin Password";
                themeColor = ADMIN_COLOR;
                break;
            case SENDER:
                title = "Reset Sender Password";
                themeColor = SENDER_COLOR;
                break;
            case COURIER:
                title = "Reset Courier Password";
                themeColor = COURIER_COLOR;
                break;
            default:
                return;
        }
        
        JDialog resetDialog = new JDialog(this, "Reset Password", true);
        resetDialog.setSize(450, 350);
        resetDialog.setLocationRelativeTo(this);
        resetDialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Set New Password", SwingConstants.CENTER);
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setForeground(themeColor);
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(LABEL_FONT);
        panel.add(newLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField newPwdField = new JPasswordField(15);
        newPwdField.setFont(FIELD_FONT);
        newPwdField.setPreferredSize(new Dimension(220, 40));
        panel.add(newPwdField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setFont(LABEL_FONT);
        panel.add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(15);
        confirmPwdField.setFont(FIELD_FONT);
        confirmPwdField.setPreferredSize(new Dimension(220, 40));
        panel.add(confirmPwdField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        JCheckBox showPwdCheck = new JCheckBox("Show Password");
        showPwdCheck.setFont(SMALL_FONT);
        showPwdCheck.setBackground(Color.WHITE);
        showPwdCheck.addActionListener(e -> {
            char echoChar = showPwdCheck.isSelected() ? (char)0 : '•';
            newPwdField.setEchoChar(echoChar);
            confirmPwdField.setEchoChar(echoChar);
        });
        panel.add(showPwdCheck, gbc);
        
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 15, 15, 15);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton resetBtn = new JButton("RESET PASSWORD");
        resetBtn.setFont(BUTTON_FONT);
        resetBtn.setBackground(themeColor);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setPreferredSize(new Dimension(200, 45));
        resetBtn.setBorderPainted(false);
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(130, 45));
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(e -> resetDialog.dispose());
        
        buttonPanel.add(resetBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        resetDialog.add(panel, BorderLayout.CENTER);
        
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
            
            // Update password based on user type
            switch (userType) {
                case ADMIN:
                    AdminAccount adminAccount = adminDatabase.get(userId);
                    adminAccount.passwordHash = hashPassword(newPwd);
                    saveAdminData();
                    break;
                    
                case SENDER:
                    SenderAccount senderAccount = senderDatabase.get(userId);
                    senderAccount.passwordHash = hashPassword(newPwd);
                    saveSenderData();
                    break;
                    
                case COURIER:
                    CourierAccount courierAccount = courierDatabase.get(userId);
                    courierAccount.passwordHash = hashPassword(newPwd);
                    saveCourierData();
                    break;
            }
            
            JOptionPane.showMessageDialog(resetDialog, 
                "Password reset successful!\nYou can now login with your new password.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            resetDialog.dispose();
            
            // Auto-fill the login field
            switch (userType) {
                case ADMIN:
                    userIdField.setText(userId);
                    passwordField.setText("");
                    break;
                case SENDER:
                    senderUserIdField.setText(userId);
                    senderPasswordField.setText("");
                    break;
                case COURIER:
                    courierUserIdField.setText(userId);
                    courierPasswordField.setText("");
                    break;
            }
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
        
        senderRegNameField.setText("");
        senderRegEmailField.setText("");
        senderRegPhoneField.setText("");
        senderRegUserIdField.setText("");
        senderRegPasswordField.setText("");
        senderRegConfirmPwdField.setText("");
        
        senderTabbedPane.setSelectedIndex(0);
        senderUserIdField.setText(userId);
    }
    
    private void processCourierRegistration(String licenseType, String icNumber, String licensePhotoPath, String icPhotoPath) {
        String name = courierRegNameField.getText().trim();
        String email = courierRegEmailField.getText().trim();
        String phone = courierRegPhoneField.getText().trim();
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || icNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (phone.length() < 10 || phone.length() > 15) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!icNumber.matches("\\d{6}-\\d{2}-\\d{4}") && !icNumber.matches("\\d{12}")) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "IC number format may be incorrect. Expected format: 123456-12-1234 or 12 digits.\nContinue anyway?", 
                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        if (licensePhotoPath == null || icPhotoPath == null) {
            JOptionPane.showMessageDialog(this, 
                "Please upload both License photo and IC photo!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File licenseFile = new File(licensePhotoPath);
        File icFile = new File(icPhotoPath);
        long maxSize = 5 * 1024 * 1024;
        
        if (licenseFile.length() > maxSize || icFile.length() > maxSize) {
            JOptionPane.showMessageDialog(this, 
                "File size too large! Maximum size is 5MB per file.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String emailPrefix = email.substring(0, email.indexOf('@'));
        emailPrefix = emailPrefix.replaceAll("[^a-zA-Z0-9]", "");
        String generatedUserId = emailPrefix + System.currentTimeMillis() % 10000;
        
        String generatedPassword = "courier" + (int)(Math.random() * 9000 + 1000);
        
        CourierAccount newCourier = new CourierAccount(
            name, email, phone, generatedUserId, generatedPassword);
        
        newCourier.licenseType = licenseType;
        newCourier.icNumber = icNumber;
        newCourier.licensePhotoPath = licensePhotoPath;
        newCourier.icPhotoPath = icPhotoPath;
        newCourier.status = "PENDING";
        
        courierDatabase.put(generatedUserId, newCourier);
        saveCourierData();
        
        String successMessage = String.format(
            "✓ APPLICATION SUBMITTED SUCCESSFULLY!\n\n" +
            "Dear %s,\n\n" +
            "Your courier application has been received.\n" +
            "Your User ID: %s\n" +
            "Status: PENDING APPROVAL\n\n" +
            "⏳ Please wait for admin approval.\n" +
            "You can check your application status in the 'CHECK STATUS' tab.\n\n" +
            "Thank you for joining LogiXpress!",
            name, generatedUserId);
        
        JOptionPane.showMessageDialog(this, 
            successMessage, 
            "Application Submitted - Pending Approval", 
            JOptionPane.INFORMATION_MESSAGE);
        
        String credentialsMessage = String.format(
            "Please save your login credentials:\n\n" +
            "User ID: %s\n" +
            "Password: %s\n\n" +
            "You will need these to login after approval.\n" +
            "You can also use your User ID to check application status.",
            generatedUserId, generatedPassword);
        
        JOptionPane.showMessageDialog(this, 
            credentialsMessage, 
            "Save Your Credentials", 
            JOptionPane.WARNING_MESSAGE);
        
        courierRegNameField.setText("");
        courierRegEmailField.setText("");
        courierRegPhoneField.setText("");
        
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
                
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + account.fullName + "!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                new SenderDashboard(account.fullName, account.email).setVisible(true);
                this.dispose();
                
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
        
        if (courierDatabase.containsKey(userId)) {
            CourierAccount account = courierDatabase.get(userId);
            if (verifyPassword(password, account.passwordHash)) {
                if ("APPROVED".equals(account.status)) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    new CourierDashboard().setVisible(true);
                    dispose();
                } else {
                    String message = account.status.equals("PENDING") ? 
                        "Your account is pending approval. Please wait for admin verification." : 
                        "Your application has been rejected. Reason: " + account.remarks;
                    JOptionPane.showMessageDialog(this, message);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Courier not found!");
        }
    }
    
    private void processAdminLogin() {
        String username = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (adminDatabase.containsKey(username) && verifyPassword(password, adminDatabase.get(username).passwordHash)) {
            adminDatabase.get(username).lastLogin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            saveAdminData();
            
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
            // Silent fail in production
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}