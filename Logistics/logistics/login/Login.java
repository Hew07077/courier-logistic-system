package logistics.login;

import javax.swing.*;
import javax.swing.Timer;

import sender.SenderDashboard;
import courier.CourierDashboard;
import admin.AdminDashboard;

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
        String username, email, passwordHash;
        
        public AdminAccount(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.passwordHash = hashPassword(password);
        }
    }
    
    static class SenderAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String fullName, email, phone, username, passwordHash, registrationDate, status;
        
        public SenderAccount(String fullName, String email, String phone, String username, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.username = username;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.status = "Active";
        }
    }
    
    static class CourierAccount implements Serializable {
        private static final long serialVersionUID = 2L;
        String fullName, email, phone, licenseType, icNumber, licensePhotoPath, icPhotoPath;
        String userId, passwordHash, registrationDate, status, remarks;
        
        public CourierAccount(String fullName, String email, String phone, String userId, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.userId = userId;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
    
    @SuppressWarnings("unchecked")
    private static void loadAdminData() {
        File file = new File(ADMIN_DATA_FILE);
        if (!file.exists()) {
            adminDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof HashMap<?, ?>) {
                adminDatabase = (HashMap<String, AdminAccount>) obj;
            }
        } catch (Exception e) {
            adminDatabase = new HashMap<>();
        }
    }
    
    private static void saveAdminData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ADMIN_DATA_FILE))) {
            oos.writeObject(adminDatabase);
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadSenderData() {
        File file = new File(SENDER_DATA_FILE);
        if (!file.exists()) {
            senderDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof HashMap<?, ?>) {
                senderDatabase = (HashMap<String, SenderAccount>) obj;
            }
        } catch (Exception e) {
            senderDatabase = new HashMap<>();
        }
    }
    
    private static void saveSenderData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SENDER_DATA_FILE))) {
            oos.writeObject(senderDatabase);
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadCourierData() {
        File file = new File(COURIER_DATA_FILE);
        if (!file.exists()) {
            courierDatabase = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof HashMap<?, ?>) {
                courierDatabase = (HashMap<String, CourierAccount>) obj;
            }
        } catch (Exception e) {
            courierDatabase = new HashMap<>();
        }
    }
    
    private static void saveCourierData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(COURIER_DATA_FILE))) {
            oos.writeObject(courierDatabase);
        } catch (Exception e) {
            // Silent fail
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
            courier.licensePhotoPath = "default_license.jpg";
            courier.icPhotoPath = "default_ic.jpg";
            courierDatabase.put("courier", courier);
            saveCourierData();
        }
        
        // Add pending courier
        if (!courierDatabase.containsKey("pending")) {
            CourierAccount pending = new CourierAccount(
                "Pending User", "pending@example.com", "0123456789", "pending", "pending123");
            pending.status = "PENDING";
            pending.licenseType = "B";
            pending.icNumber = "987654-12-5678";
            pending.licensePhotoPath = "pending_license.jpg";
            pending.icPhotoPath = "pending_ic.jpg";
            courierDatabase.put("pending", pending);
            saveCourierData();
        }
        
        // Add rejected courier
        if (!courierDatabase.containsKey("rejected")) {
            CourierAccount rejected = new CourierAccount(
                "Rejected User", "rejected@example.com", "0112233445", "rejected", "rejected123");
            rejected.status = "REJECTED";
            rejected.remarks = "Invalid license document";
            rejected.licenseType = "C";
            rejected.icNumber = "555555-12-8888";
            rejected.licensePhotoPath = "rejected_license.jpg";
            rejected.icPhotoPath = "rejected_ic.jpg";
            courierDatabase.put("rejected", rejected);
            saveCourierData();
        }
    }
    
    // ========== UI Components ==========
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    
    private JTextField senderUsernameField;
    private JPasswordField senderPasswordField;
    private JCheckBox senderShowPasswordCheckBox;
    
    private JTextField senderRegNameField;
    private JTextField senderRegEmailField;
    private JTextField senderRegPhoneField;
    private JTextField senderRegUsernameField;
    private JPasswordField senderRegPasswordField;
    private JPasswordField senderRegConfirmPwdField;
    private JLabel passwordStrengthLabel;
    private JProgressBar passwordStrengthBar;
    
    private JTextField courierUserIdField;
    private JPasswordField courierPasswordField;
    private JCheckBox courierShowPasswordCheckBox;
    
    private JTextField courierRegNameField;
    private JTextField courierRegEmailField;
    private JTextField courierRegPhoneField;
    private JTextField courierRegIcField;
    private JComboBox<String> courierRegLicenseCombo;
    private JLabel licenseFileNameLabel, icFileNameLabel;
    private String licensePhotoPath, icPhotoPath;
    
    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    private JTabbedPane senderTabbedPane;
    private JTabbedPane courierTabbedPane;
    
    private JButton adminBtn, senderBtn, courierBtn;
    private String currentRole = "ADMIN";
    
    // ========== Color Definitions ==========
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color ORANGE_WHITE = new Color(255, 245, 235);
    private final Color ADMIN_COLOR = ORANGE_PRIMARY;
    private final Color SENDER_COLOR = new Color(52, 152, 219);
    private final Color COURIER_COLOR = new Color(46, 204, 113);
    private final Color RED_BTN = new Color(220, 20, 60);
    private final Color GREEN_SUCCESS = new Color(46, 204, 113);
    private final Color RED_ERROR = new Color(231, 76, 60);
    private final Color YELLOW_PENDING = new Color(241, 196, 15);
    
    // ========== Font Settings ==========
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 30);
    private final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 15);
    private final Font FIELD_FONT = new Font("Arial", Font.PLAIN, 15);
    private final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);
    private final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 13);
    private final Font TAB_FONT = new Font("Arial", Font.BOLD, 16);
    
    // ========== User Type Enum ==========
    private enum UserType { SENDER, COURIER }
    
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
        
        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ADMIN_COLOR);
        topBar.setPreferredSize(new Dimension(getWidth(), 70));
        
        // Logo Panel - Using image instead of text
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        logoPanel.setBackground(ADMIN_COLOR);
        
        // Try to load logo from file
        try {
            String logoPath = "logo.png"; // Change this to your logo file name
            File logoFile = new File(logoPath);
            
            if (logoFile.exists()) {
                ImageIcon logoIcon = new ImageIcon(logoPath);
                // Scale logo to appropriate height while maintaining aspect ratio
                int logoHeight = 50;
                int logoWidth = (int)((double)logoIcon.getIconWidth() / logoIcon.getIconHeight() * logoHeight);
                Image scaledImage = logoIcon.getImage().getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                logoPanel.add(logoLabel);
            } else {
                // Fallback to text if logo not found
                JLabel fallbackLabel = new JLabel("LogiXpress");
                fallbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
                fallbackLabel.setForeground(Color.WHITE);
                logoPanel.add(fallbackLabel);
                System.out.println("Logo file not found at: " + new File(logoPath).getAbsolutePath());
            }
        } catch (Exception e) {
            // Fallback to text on error
            JLabel fallbackLabel = new JLabel("LogiXpress");
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
            fallbackLabel.setForeground(Color.WHITE);
            logoPanel.add(fallbackLabel);
            e.printStackTrace();
        }
        
        topBar.add(logoPanel, BorderLayout.WEST);
        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        topRightPanel.setBackground(ADMIN_COLOR);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        topRightPanel.add(timeLabel);
        new Timer(1000, e -> timeLabel.setText(
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))).start();
        
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(RED_BTN);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setPreferredSize(new Dimension(70, 35));
        exitBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit LogiXpress?", "Exit", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        topRightPanel.add(exitBtn);
        topBar.add(topRightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        
        // Bottom Bar
        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(ORANGE_WHITE);
        bottomBar.setPreferredSize(new Dimension(getWidth(), 25));
        JLabel copyrightLabel = new JLabel("© 2024 LogiXpress Logistics Solutions. All rights reserved.");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(Color.DARK_GRAY);
        bottomBar.add(copyrightLabel);
        add(bottomBar, BorderLayout.SOUTH);
        
        // Main Center Panel - Made narrower with reduced side margins
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(ORANGE_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 150, 15, 150)); // Reduced from 250 to 150
        
        // Role Buttons - Made larger and closer
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(ORANGE_WHITE);
        
        adminBtn = createButton("ADMIN", ADMIN_COLOR);
        senderBtn = createButton("SENDER", SENDER_COLOR);
        courierBtn = createButton("COURIER", COURIER_COLOR);
        
        buttonPanel.add(adminBtn);
        buttonPanel.add(senderBtn);
        buttonPanel.add(courierBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Card Panel - Made slightly larger
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(ORANGE_WHITE);
        mainCardPanel.setPreferredSize(new Dimension(650, 500)); // Increased from 550x450
        
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
            currentRole = "ADMIN";
            updateButtonColors();
        });
        
        senderBtn.addActionListener(e -> {
            cardLayout.show(mainCardPanel, "SENDER");
            currentRole = "SENDER";
            updateButtonColors();
        });
        
        courierBtn.addActionListener(e -> {
            cardLayout.show(mainCardPanel, "COURIER");
            currentRole = "COURIER";
            updateButtonColors();
        });
        
        // Set initial colors
        updateButtonColors();
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 22)); // Increased font size
        btn.setPreferredSize(new Dimension(160, 55)); // Increased size from 120x40 to 160x55
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("color", color);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        return btn;
    }
    
    private void updateButtonColors() {
        resetButtonStyle(adminBtn, ADMIN_COLOR, "ADMIN");
        resetButtonStyle(senderBtn, SENDER_COLOR, "SENDER");
        resetButtonStyle(courierBtn, COURIER_COLOR, "COURIER");
    }
    
    private void resetButtonStyle(JButton btn, Color borderColor, String role) {
        if (role.equals(currentRole)) {
            btn.setBackground(borderColor);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
        } else {
            btn.setBackground(ORANGE_WHITE);
            btn.setForeground(Color.BLACK);
            btn.setBorder(BorderFactory.createLineBorder(borderColor, 3)); // Thicker border
            btn.setBorderPainted(true);
        }
        btn.repaint();
    }
    
    // ========== ADMIN PANEL ==========
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ADMIN_COLOR, 3),
            BorderFactory.createEmptyBorder(25, 40, 25, 40)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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
        userIdField.setPreferredSize(new Dimension(220, 35));
        panel.add(userIdField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        passwordField = new JPasswordField(15);
        passwordField.setFont(FIELD_FONT);
        passwordField.setPreferredSize(new Dimension(180, 35));
        passwordField.setEchoChar('•');
        
        showPasswordCheckBox = new JCheckBox("Show");
        showPasswordCheckBox.setFont(SMALL_FONT);
        showPasswordCheckBox.setBackground(Color.WHITE);
        showPasswordCheckBox.setPreferredSize(new Dimension(70, 35));
        showPasswordCheckBox.addActionListener(e -> 
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(passwordField, BorderLayout.CENTER);
        pwdPanel.add(showPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(BUTTON_FONT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(ADMIN_COLOR);
        loginBtn.setPreferredSize(new Dimension(220, 45));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processAdminLogin());
        panel.add(loginBtn, gbc);
        
        return panel;
    }
    
    // ========== SENDER PANEL ==========
    private JPanel createSenderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(SENDER_COLOR, 3));
        
        senderTabbedPane = new JTabbedPane();
        senderTabbedPane.setFont(TAB_FONT);
        
        senderTabbedPane.addTab("LOGIN", createSenderLoginPanel());
        senderTabbedPane.addTab("REGISTER", createSenderRegisterPanel());
        
        panel.add(senderTabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSenderLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("SENDER LOGIN", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(SENDER_COLOR);
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(LABEL_FONT);
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        senderUsernameField = new JTextField(15);
        senderUsernameField.setFont(FIELD_FONT);
        senderUsernameField.setPreferredSize(new Dimension(220, 35));
        panel.add(senderUsernameField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        senderPasswordField = new JPasswordField(15);
        senderPasswordField.setFont(FIELD_FONT);
        senderPasswordField.setPreferredSize(new Dimension(180, 35));
        senderPasswordField.setEchoChar('•');
        
        senderShowPasswordCheckBox = new JCheckBox("Show");
        senderShowPasswordCheckBox.setFont(SMALL_FONT);
        senderShowPasswordCheckBox.setBackground(Color.WHITE);
        senderShowPasswordCheckBox.setPreferredSize(new Dimension(70, 35));
        senderShowPasswordCheckBox.addActionListener(e -> 
            senderPasswordField.setEchoChar(senderShowPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(senderPasswordField, BorderLayout.CENTER);
        pwdPanel.add(senderShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(BUTTON_FONT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(SENDER_COLOR);
        loginBtn.setPreferredSize(new Dimension(220, 45));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processSenderLogin());
        panel.add(loginBtn, gbc);
        
        gbc.gridy = 4; gbc.insets = new Insets(5, 10, 5, 10);
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
        
        gbc.gridy = 5; gbc.insets = new Insets(5, 10, 10, 10);
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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
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
        senderRegUsernameField = new JTextField(15);
        senderRegPasswordField = new JPasswordField(15);
        senderRegConfirmPwdField = new JPasswordField(15);
        
        // Password strength meter
        passwordStrengthBar = new JProgressBar(0, 100);
        passwordStrengthBar.setStringPainted(true);
        passwordStrengthBar.setString("Password Strength");
        passwordStrengthBar.setForeground(Color.GRAY);
        passwordStrengthBar.setPreferredSize(new Dimension(220, 20));
        
        passwordStrengthLabel = new JLabel("Minimum: 8+ chars, uppercase, lowercase, number, special");
        passwordStrengthLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        passwordStrengthLabel.setForeground(Color.DARK_GRAY);
        
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        senderRegNameField.setPreferredSize(new Dimension(220, 30));
        panel.add(senderRegNameField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        senderRegEmailField.setPreferredSize(new Dimension(220, 30));
        panel.add(senderRegEmailField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Phone:*"), gbc);
        gbc.gridx = 1;
        senderRegPhoneField.setPreferredSize(new Dimension(220, 30));
        panel.add(senderRegPhoneField, gbc);
        
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("Username:*"), gbc);
        gbc.gridx = 1;
        senderRegUsernameField.setPreferredSize(new Dimension(220, 30));
        panel.add(senderRegUsernameField, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Password:*"), gbc);
        gbc.gridx = 1;
        senderRegPasswordField.setPreferredSize(new Dimension(220, 30));
        panel.add(senderRegPasswordField, gbc);
        
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("Confirm:*"), gbc);
        gbc.gridx = 1;
        senderRegConfirmPwdField.setPreferredSize(new Dimension(220, 30));
        panel.add(senderRegConfirmPwdField, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(passwordStrengthBar, gbc);
        
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(passwordStrengthLabel, gbc);
        
        // Add password strength listener
        senderRegPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkPasswordStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkPasswordStrength(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkPasswordStrength(); }
        });
        
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JButton regBtn = new JButton("REGISTER");
        regBtn.setFont(BUTTON_FONT);
        regBtn.setForeground(Color.WHITE);
        regBtn.setBackground(SENDER_COLOR);
        regBtn.setPreferredSize(new Dimension(220, 45));
        regBtn.setBorderPainted(false);
        regBtn.addActionListener(e -> processSenderRegistration());
        panel.add(regBtn, gbc);
        
        return panel;
    }
    
    private void checkPasswordStrength() {
        String password = new String(senderRegPasswordField.getPassword());
        int strength = 0;
        String strengthText = "";
        Color strengthColor = Color.GRAY;
        
        if (password.length() >= 8) strength++;
        if (password.length() >= 10) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;
        
        int percentage = Math.min(strength * 17, 100);
        passwordStrengthBar.setValue(percentage);
        
        if (password.isEmpty()) {
            strengthText = "Enter password";
            strengthColor = Color.GRAY;
        } else if (strength < 3) {
            strengthText = "Weak";
            strengthColor = Color.RED;
        } else if (strength < 5) {
            strengthText = "Medium";
            strengthColor = Color.ORANGE;
        } else if (strength < 7) {
            strengthText = "Strong";
            strengthColor = new Color(0, 150, 0);
        } else {
            strengthText = "Very Strong";
            strengthColor = new Color(0, 100, 0);
        }
        
        passwordStrengthBar.setString(strengthText);
        passwordStrengthBar.setForeground(strengthColor);
    }
    
    // ========== COURIER PANEL ==========
    private JPanel createCourierPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(COURIER_COLOR, 3));
        
        courierTabbedPane = new JTabbedPane();
        courierTabbedPane.setFont(TAB_FONT);
        
        courierTabbedPane.addTab("LOGIN", createCourierLoginPanel());
        courierTabbedPane.addTab("APPLY", createCourierApplyPanel());
        courierTabbedPane.addTab("CHECK STATUS", createCourierStatusPanel());
        
        panel.add(courierTabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCourierLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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
        courierUserIdField.setPreferredSize(new Dimension(220, 35));
        panel.add(courierUserIdField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        
        courierPasswordField = new JPasswordField(15);
        courierPasswordField.setFont(FIELD_FONT);
        courierPasswordField.setPreferredSize(new Dimension(180, 35));
        courierPasswordField.setEchoChar('•');
        
        courierShowPasswordCheckBox = new JCheckBox("Show");
        courierShowPasswordCheckBox.setFont(SMALL_FONT);
        courierShowPasswordCheckBox.setBackground(Color.WHITE);
        courierShowPasswordCheckBox.setPreferredSize(new Dimension(70, 35));
        courierShowPasswordCheckBox.addActionListener(e -> 
            courierPasswordField.setEchoChar(courierShowPasswordCheckBox.isSelected() ? (char)0 : '•'));
        
        pwdPanel.add(courierPasswordField, BorderLayout.CENTER);
        pwdPanel.add(courierShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(BUTTON_FONT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(COURIER_COLOR);
        loginBtn.setPreferredSize(new Dimension(220, 45));
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> processCourierLogin());
        panel.add(loginBtn, gbc);
        
        gbc.gridy = 4; gbc.insets = new Insets(5, 10, 5, 10);
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
        
        gbc.gridy = 5; gbc.insets = new Insets(5, 10, 10, 10);
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
    
    private JPanel createCourierApplyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
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
        courierRegIcField = new JTextField(15);
        
        courierRegLicenseCombo = new JComboBox<>(new String[]{
            "A", "A1", "B", "B1", "B2", "C", "D", "DA", "E", "G"
        });
        courierRegLicenseCombo.setFont(FIELD_FONT);
        courierRegLicenseCombo.setPreferredSize(new Dimension(220, 30));
        
        JButton uploadLicenseBtn = new JButton("Upload");
        JButton uploadICBtn = new JButton("Upload");
        
        uploadLicenseBtn.setFont(SMALL_FONT);
        uploadLicenseBtn.setBackground(new Color(100, 100, 100));
        uploadLicenseBtn.setForeground(Color.WHITE);
        uploadLicenseBtn.setFocusPainted(false);
        uploadLicenseBtn.setBorderPainted(false);
        uploadLicenseBtn.setPreferredSize(new Dimension(90, 30));
        
        uploadICBtn.setFont(SMALL_FONT);
        uploadICBtn.setBackground(new Color(100, 100, 100));
        uploadICBtn.setForeground(Color.WHITE);
        uploadICBtn.setFocusPainted(false);
        uploadICBtn.setBorderPainted(false);
        uploadICBtn.setPreferredSize(new Dimension(90, 30));
        
        licenseFileNameLabel = new JLabel("No file");
        icFileNameLabel = new JLabel("No file");
        licenseFileNameLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        icFileNameLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        
        uploadLicenseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                licensePhotoPath = fc.getSelectedFile().getAbsolutePath();
                licenseFileNameLabel.setText(fc.getSelectedFile().getName());
            }
        });
        
        uploadICBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                icPhotoPath = fc.getSelectedFile().getAbsolutePath();
                icFileNameLabel.setText(fc.getSelectedFile().getName());
            }
        });
        
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        courierRegNameField.setPreferredSize(new Dimension(220, 30));
        panel.add(courierRegNameField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        courierRegEmailField.setPreferredSize(new Dimension(220, 30));
        panel.add(courierRegEmailField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Phone:*"), gbc);
        gbc.gridx = 1;
        courierRegPhoneField.setPreferredSize(new Dimension(220, 30));
        panel.add(courierRegPhoneField, gbc);
        
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("IC Number:*"), gbc);
        gbc.gridx = 1;
        courierRegIcField.setPreferredSize(new Dimension(220, 30));
        panel.add(courierRegIcField, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Upload IC:*"), gbc);
        gbc.gridx = 1;
        JPanel icPanel = new JPanel(new BorderLayout(5, 0));
        icPanel.setBackground(Color.WHITE);
        icPanel.add(uploadICBtn, BorderLayout.WEST);
        icPanel.add(icFileNameLabel, BorderLayout.CENTER);
        panel.add(icPanel, gbc);
        
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("License Type:*"), gbc);
        gbc.gridx = 1;
        panel.add(courierRegLicenseCombo, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0;
        panel.add(new JLabel("Upload License:*"), gbc);
        gbc.gridx = 1;
        JPanel licensePanel = new JPanel(new BorderLayout(5, 0));
        licensePanel.setBackground(Color.WHITE);
        licensePanel.add(uploadLicenseBtn, BorderLayout.WEST);
        licensePanel.add(licenseFileNameLabel, BorderLayout.CENTER);
        panel.add(licensePanel, gbc);
        
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 4, 4, 4);
        
        JButton submitBtn = new JButton("SUBMIT APPLICATION");
        submitBtn.setFont(BUTTON_FONT);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(COURIER_COLOR);
        submitBtn.setPreferredSize(new Dimension(220, 45));
        submitBtn.setBorderPainted(false);
        submitBtn.addActionListener(e -> processCourierApplication());
        panel.add(submitBtn, gbc);
        
        return panel;
    }
    
    private JPanel createCourierStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        JLabel title = new JLabel("CHECK STATUS", SwingConstants.CENTER);
        title.setFont(SUBTITLE_FONT);
        title.setForeground(COURIER_COLOR);
        panel.add(title, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel searchLabel = new JLabel("User ID:");
        searchLabel.setFont(LABEL_FONT);
        centerPanel.add(searchLabel, gbc);
        
        gbc.gridx = 1;
        JTextField searchField = new JTextField(15);
        searchField.setFont(FIELD_FONT);
        searchField.setPreferredSize(new Dimension(180, 30));
        centerPanel.add(searchField, gbc);
        
        gbc.gridx = 2;
        JButton searchBtn = new JButton("SEARCH");
        searchBtn.setFont(BUTTON_FONT);
        searchBtn.setBackground(COURIER_COLOR);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setPreferredSize(new Dimension(110, 35));
        centerPanel.add(searchBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(250, 250, 250));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 45));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel statusLabel = new JLabel("Enter ID", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel detailLabel = new JLabel("", SwingConstants.CENTER);
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel remarkLabel = new JLabel("", SwingConstants.CENTER);
        remarkLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        remarkLabel.setForeground(Color.RED);
        remarkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resultPanel.add(iconLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(statusLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        resultPanel.add(detailLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        resultPanel.add(remarkLabel);
        
        centerPanel.add(resultPanel, gbc);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        searchBtn.addActionListener(e -> {
            String id = searchField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter User ID!");
                return;
            }
            
            if (courierDatabase.containsKey(id)) {
                CourierAccount acc = courierDatabase.get(id);
                
                switch (acc.status) {
                    case "APPROVED":
                        iconLabel.setText("✓");
                        iconLabel.setForeground(GREEN_SUCCESS);
                        statusLabel.setText("STATUS: APPROVED");
                        statusLabel.setForeground(GREEN_SUCCESS);
                        detailLabel.setText("Application approved!");
                        detailLabel.setForeground(GREEN_SUCCESS);
                        remarkLabel.setText("");
                        break;
                    case "REJECTED":
                        iconLabel.setText("✗");
                        iconLabel.setForeground(RED_ERROR);
                        statusLabel.setText("STATUS: REJECTED");
                        statusLabel.setForeground(RED_ERROR);
                        detailLabel.setText("Application rejected.");
                        detailLabel.setForeground(RED_ERROR);
                        remarkLabel.setText("Reason: " + acc.remarks);
                        break;
                    default:
                        iconLabel.setText("⏳");
                        iconLabel.setForeground(YELLOW_PENDING);
                        statusLabel.setText("STATUS: PENDING");
                        statusLabel.setForeground(YELLOW_PENDING);
                        detailLabel.setText("Under review.");
                        detailLabel.setForeground(YELLOW_PENDING);
                        remarkLabel.setText("");
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "User ID not found!");
            }
        });
        
        return panel;
    }
    
    // ========== SIMPLIFIED FORGOT PASSWORD - DIRECT CODE POPUP ==========
    
    private void showForgotPasswordDialog(UserType type) {
        String title = (type == UserType.SENDER) ? "Reset Sender Password" : "Reset Courier Password";
        String idLabel = (type == UserType.SENDER) ? "Username:" : "Courier ID:";
        Color themeColor = (type == UserType.SENDER) ? SENDER_COLOR : COURIER_COLOR;
        
        JDialog dialog = new JDialog(this, "Forgot Password", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(themeColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username/ID field
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel idJLabel = new JLabel(idLabel);
        idJLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(idJLabel, gbc);
        
        gbc.gridx = 1;
        JTextField idField = new JTextField(15);
        idField.setFont(new Font("Arial", Font.PLAIN, 13));
        idField.setPreferredSize(new Dimension(180, 30));
        formPanel.add(idField, gbc);
        
        // Email field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(180, 30));
        formPanel.add(emailField, gbc);
        
        // Info text
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("We'll verify your identity", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        formPanel.add(infoLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton verifyBtn = new JButton("VERIFY");
        verifyBtn.setFont(new Font("Arial", Font.BOLD, 12));
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setBackground(themeColor);
        verifyBtn.setPreferredSize(new Dimension(90, 32));
        verifyBtn.setBorderPainted(false);
        verifyBtn.setFocusPainted(false);
        verifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setPreferredSize(new Dimension(90, 32));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(verifyBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.getRootPane().setDefaultButton(verifyBtn);
        
        verifyBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String email = emailField.getText().trim();
            
            if (id.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            boolean verified = false;
            if (type == UserType.SENDER && senderDatabase.containsKey(id)) {
                verified = senderDatabase.get(id).email.equals(email);
            } else if (type == UserType.COURIER && courierDatabase.containsKey(id)) {
                verified = courierDatabase.get(id).email.equals(email);
            }
            
            if (verified) {
                dialog.dispose();
                // Generate and show verification code directly
                String verificationCode = String.format("%06d", new Random().nextInt(999999));
                showVerificationCodePopup(verificationCode, id, type, themeColor);
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid " + idLabel.toLowerCase() + " or email!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.setVisible(true);
    }
    
    private void showVerificationCodePopup(String code, String userId, UserType type, Color themeColor) {
        // Show code in a dialog first
        JOptionPane.showMessageDialog(this, 
            "Your verification code is: " + code, 
            "Verification Code", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // Then show code entry dialog
        JDialog codeDialog = new JDialog(this, "Enter Verification Code", true);
        codeDialog.setSize(340, 220);
        codeDialog.setLocationRelativeTo(this);
        codeDialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Enter Verification Code", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(themeColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Code input field (single field for 6-digit code)
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(Color.WHITE);
        
        JTextField codeField = new JTextField(6);
        codeField.setFont(new Font("Arial", Font.BOLD, 22));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setPreferredSize(new Dimension(140, 45));
        codeField.setDocument(new javax.swing.text.PlainDocument() {
            public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= 6) {
                    super.insertString(offset, str, attr);
                }
            }
        });
        
        inputPanel.add(codeField);
        centerPanel.add(inputPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton verifyBtn = new JButton("VERIFY");
        verifyBtn.setFont(new Font("Arial", Font.BOLD, 12));
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setBackground(themeColor);
        verifyBtn.setPreferredSize(new Dimension(90, 32));
        verifyBtn.setBorderPainted(false);
        verifyBtn.setFocusPainted(false);
        verifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setPreferredSize(new Dimension(90, 32));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> codeDialog.dispose());
        
        buttonPanel.add(verifyBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        codeDialog.add(mainPanel);
        codeDialog.getRootPane().setDefaultButton(verifyBtn);
        
        verifyBtn.addActionListener(e -> {
            String enteredCode = codeField.getText().trim();
            
            if (enteredCode.length() != 6) {
                JOptionPane.showMessageDialog(codeDialog, "Please enter 6-digit code!", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (enteredCode.equals(code)) {
                codeDialog.dispose();
                showResetPasswordDialog(userId, type);
            } else {
                JOptionPane.showMessageDialog(codeDialog, "Invalid verification code!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                codeField.setText("");
                codeField.requestFocus();
            }
        });
        
        codeDialog.setVisible(true);
    }
    
    private void showResetPasswordDialog(String userId, UserType type) {
        Color themeColor = (type == UserType.SENDER) ? SENDER_COLOR : COURIER_COLOR;
        
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(340, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Create New Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(themeColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel newLabel = new JLabel("New:");
        newLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(newLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField pwdField = new JPasswordField(15);
        pwdField.setFont(new Font("Arial", Font.PLAIN, 13));
        pwdField.setPreferredSize(new Dimension(170, 30));
        formPanel.add(pwdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        confirmField.setFont(new Font("Arial", Font.PLAIN, 13));
        confirmField.setPreferredSize(new Dimension(170, 30));
        formPanel.add(confirmField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JCheckBox showCheck = new JCheckBox("Show Password");
        showCheck.setFont(new Font("Arial", Font.PLAIN, 11));
        showCheck.setBackground(Color.WHITE);
        showCheck.addActionListener(e -> {
            char c = showCheck.isSelected() ? (char)0 : '•';
            pwdField.setEchoChar(c);
            confirmField.setEchoChar(c);
        });
        formPanel.add(showCheck, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton resetBtn = new JButton("RESET");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 12));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(themeColor);
        resetBtn.setPreferredSize(new Dimension(90, 32));
        resetBtn.setBorderPainted(false);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setPreferredSize(new Dimension(90, 32));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(resetBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.getRootPane().setDefaultButton(resetBtn);
        
        resetBtn.addActionListener(e -> {
            String pwd = new String(pwdField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (pwd.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!pwd.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Stronger password requirements
            if (pwd.length() < 8) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 8 characters!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!pwd.matches(".*[A-Z].*")) {
                JOptionPane.showMessageDialog(dialog, "Password must contain at least one uppercase letter!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!pwd.matches(".*[a-z].*")) {
                JOptionPane.showMessageDialog(dialog, "Password must contain at least one lowercase letter!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!pwd.matches(".*[0-9].*")) {
                JOptionPane.showMessageDialog(dialog, "Password must contain at least one number!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                JOptionPane.showMessageDialog(dialog, "Password must contain at least one special character!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (type == UserType.SENDER) {
                senderDatabase.get(userId).passwordHash = hashPassword(pwd);
                saveSenderData();
                senderUsernameField.setText(userId);
            } else {
                courierDatabase.get(userId).passwordHash = hashPassword(pwd);
                saveCourierData();
                courierUserIdField.setText(userId);
            }
            
            JOptionPane.showMessageDialog(dialog, "Password reset successful!\nYou can now login.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        dialog.setVisible(true);
    }
    
    // ========== PROCESS METHODS ==========
    private void processAdminLogin() {
        String username = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }
        
        if (adminDatabase.containsKey(username) && 
            verifyPassword(password, adminDatabase.get(username).passwordHash)) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome, Administrator.");
            new AdminDashboard().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!");
            passwordField.setText("");
        }
    }
    
    private void processSenderLogin() {
        String username = senderUsernameField.getText().trim();
        String password = new String(senderPasswordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }
        
        if (senderDatabase.containsKey(username)) {
            SenderAccount acc = senderDatabase.get(username);
            if (verifyPassword(password, acc.passwordHash)) {
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + acc.fullName + "!");
                new SenderDashboard(acc.fullName, acc.email).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password!");
                senderPasswordField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Username not found!");
            senderPasswordField.setText("");
        }
    }
    
    private void processCourierLogin() {
        String userId = courierUserIdField.getText().trim();
        String password = new String(courierPasswordField.getPassword());
        
        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Courier ID and password!");
            return;
        }
        
        if (courierDatabase.containsKey(userId)) {
            CourierAccount acc = courierDatabase.get(userId);
            if (verifyPassword(password, acc.passwordHash)) {
                if ("APPROVED".equals(acc.status)) {
                    JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + acc.fullName + "!");
                    new CourierDashboard().setVisible(true);
                    dispose();
                } else if ("PENDING".equals(acc.status)) {
                    JOptionPane.showMessageDialog(this, "Your account is pending approval.");
                } else {
                    JOptionPane.showMessageDialog(this, "Your application was rejected.\nReason: " + acc.remarks);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password!");
                courierPasswordField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Courier ID not found!");
            courierPasswordField.setText("");
        }
    }
    
    private void processSenderRegistration() {
        String name = senderRegNameField.getText().trim();
        String email = senderRegEmailField.getText().trim();
        String phone = senderRegPhoneField.getText().trim();
        String username = senderRegUsernameField.getText().trim();
        String password = new String(senderRegPasswordField.getPassword());
        String confirm = new String(senderRegConfirmPwdField.getPassword());
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        // Stronger password requirements for registration
        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.matches(".*[A-Z].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one uppercase letter!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.matches(".*[a-z].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one lowercase letter!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.matches(".*[0-9].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one number!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one special character!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (senderDatabase.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists!");
            return;
        }
        
        senderDatabase.put(username, new SenderAccount(name, email, phone, username, password));
        saveSenderData();
        
        JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
        
        senderRegNameField.setText("");
        senderRegEmailField.setText("");
        senderRegPhoneField.setText("");
        senderRegUsernameField.setText("");
        senderRegPasswordField.setText("");
        senderRegConfirmPwdField.setText("");
        passwordStrengthBar.setValue(0);
        passwordStrengthBar.setString("Password Strength");
        passwordStrengthBar.setForeground(Color.GRAY);
        
        senderTabbedPane.setSelectedIndex(0);
        senderUsernameField.setText(username);
    }
    
    private void processCourierApplication() {
        String name = courierRegNameField.getText().trim();
        String email = courierRegEmailField.getText().trim();
        String phone = courierRegPhoneField.getText().trim();
        String ic = courierRegIcField.getText().trim();
        String license = (String) courierRegLicenseCombo.getSelectedItem();
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || ic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        if (licensePhotoPath == null || icPhotoPath == null) {
            JOptionPane.showMessageDialog(this, "Please upload both IC and License photos!");
            return;
        }
        
        String userId = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "") + 
                       System.currentTimeMillis() % 10000;
        String password = "courier" + (int)(Math.random() * 9000 + 1000);
        
        CourierAccount courier = new CourierAccount(name, email, phone, userId, password);
        courier.icNumber = ic;
        courier.licenseType = license;
        courier.licensePhotoPath = licensePhotoPath;
        courier.icPhotoPath = icPhotoPath;
        
        courierDatabase.put(userId, courier);
        saveCourierData();
        
        String msg = String.format(
            "Application Submitted!\n\nUser ID: %s\nPassword: %s\n\nPlease save these credentials.",
            userId, password);
        
        JOptionPane.showMessageDialog(this, msg, "Application Successful", JOptionPane.INFORMATION_MESSAGE);
        
        courierRegNameField.setText("");
        courierRegEmailField.setText("");
        courierRegPhoneField.setText("");
        courierRegIcField.setText("");
        licensePhotoPath = null;
        icPhotoPath = null;
        licenseFileNameLabel.setText("No file");
        icFileNameLabel.setText("No file");
        
        courierTabbedPane.setSelectedIndex(0);
        courierUserIdField.setText(userId);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore
        }
        
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}