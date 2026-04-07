package logistics.login;

import javax.swing.*;
import javax.swing.Timer;

import sender.SenderDashboard;
import courier.CourierDashboard;
import admin.AdminDashboard;
import logistics.driver.Driver;
import logistics.driver.DriverStorage;

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
    private static final String ADMIN_DATA_FILE = "admin_data.dat";
    
    // ========== In-Memory Databases ==========
    private static Map<String, SenderAccount> senderDatabase = new HashMap<>();
    private static Map<String, AdminAccount> adminDatabase = new HashMap<>();
    private static DriverStorage driverStorage;
    
    // ========== Account Model Classes ==========
    static class AdminAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String username, email, passwordHash;
        
        AdminAccount(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.passwordHash = hashPassword(password);
        }
    }
    
    static class SenderAccount implements Serializable {
        private static final long serialVersionUID = 1L;
        String fullName, email, phone, username, passwordHash, registrationDate, status;
        
        SenderAccount(String fullName, String email, String phone, String username, String password) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.username = username;
            this.passwordHash = hashPassword(password);
            this.registrationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.status = "Active";
        }
    }
    
    // ========== Static Initialization ==========
    static {
        loadAdminData();
        loadSenderData();
        driverStorage = new DriverStorage();
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
    
    private static void addDefaultAccounts() {
        if (!adminDatabase.containsKey("admin")) {
            adminDatabase.put("admin", new AdminAccount("admin", "admin@logixpress.com", "admin123"));
            saveAdminData();
        }
        
        if (!senderDatabase.containsKey("sender")) {
            senderDatabase.put("sender", new SenderAccount(
                "Demo Sender", "sender@example.com", "0123456789", "sender", "sender123"));
            saveSenderData();
        }
    }
    
    // ========== REUSABLE VALIDATION METHODS ==========
    
    private boolean isNotEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10,11}$");
    }
    
    private boolean isValidIC(String ic) {
        return ic != null && ic.matches("^[0-9]{12}$");
    }
    
    private String getPasswordValidationMessage(String password) {
        if (password == null || password.length() < 8) return "Password must be at least 8 characters!";
        if (!password.matches(".*[A-Z].*")) return "Password must contain at least one uppercase letter!";
        if (!password.matches(".*[a-z].*")) return "Password must contain at least one lowercase letter!";
        if (!password.matches(".*[0-9].*")) return "Password must contain at least one number!";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) return "Password must contain at least one special character!";
        return null;
    }
    
    private void clearSenderRegisterFields() {
        senderRegNameField.setText("");
        senderRegEmailField.setText("");
        senderRegPhoneField.setText("");
        senderRegUsernameField.setText("");
        senderRegPasswordField.setText("");
        senderRegConfirmPwdField.setText("");
        passwordStrengthBar.setValue(0);
        passwordStrengthBar.setString("Password Strength");
        passwordStrengthBar.setForeground(Color.GRAY);
    }
    
    private void clearCourierApplyFields() {
        courierRegNameField.setText("");
        courierRegEmailField.setText("");
        courierRegPhoneField.setText("");
        courierRegIcField.setText("");
        licensePhotoPath = null;
        icPhotoPath = null;
        licenseFileNameLabel.setText("No file");
        icFileNameLabel.setText("No file");
    }
    
    // ========== LOGO LOADING METHOD ==========
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
            // Silent fail
        }
        return null;
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
    private JCheckBox senderRegShowPasswordCheckBox;
    private JCheckBox senderRegShowConfirmCheckBox;
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
    private final Color STRONG_GREEN = new Color(0, 150, 0);
    private final Color MEDIUM_ORANGE = new Color(255, 140, 0);
    private final Color WEAK_RED = new Color(220, 20, 60);
    
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
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        logoPanel.setBackground(ADMIN_COLOR);
        
        ImageIcon logoIcon = loadLogo("logo.jpeg");
        if (logoIcon != null) {
            Image img = logoIcon.getImage();
            int logoHeight = 45;
            int logoWidth = (int)((double)logoIcon.getIconWidth() / logoIcon.getIconHeight() * logoHeight);
            Image scaledImage = img.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            logoPanel.add(logoLabel);
        }
        
        JLabel companyName = new JLabel("LogiXpress");
        companyName.setFont(new Font("Arial", Font.BOLD, 24));
        companyName.setForeground(Color.WHITE);
        logoPanel.add(companyName);
        
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
        JLabel copyrightLabel = new JLabel("(c) 2024 LogiXpress Logistics Solutions. All rights reserved.");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(Color.DARK_GRAY);
        bottomBar.add(copyrightLabel);
        add(bottomBar, BorderLayout.SOUTH);
        
        // Main Center Panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(ORANGE_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 150, 15, 150));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(ORANGE_WHITE);
        
        adminBtn = createButton("ADMIN", ADMIN_COLOR);
        senderBtn = createButton("SENDER", SENDER_COLOR);
        courierBtn = createButton("COURIER", COURIER_COLOR);
        
        buttonPanel.add(adminBtn);
        buttonPanel.add(senderBtn);
        buttonPanel.add(courierBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(ORANGE_WHITE);
        mainCardPanel.setPreferredSize(new Dimension(650, 500));
        
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
        
        updateButtonColors();
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setPreferredSize(new Dimension(160, 55));
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
            btn.setBorder(BorderFactory.createLineBorder(borderColor, 3));
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
        passwordField.setEchoChar('*');
        
        showPasswordCheckBox = new JCheckBox("Show");
        showPasswordCheckBox.setFont(SMALL_FONT);
        showPasswordCheckBox.setBackground(Color.WHITE);
        showPasswordCheckBox.setPreferredSize(new Dimension(70, 35));
        showPasswordCheckBox.addActionListener(e -> 
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char)0 : '*'));
        
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
        senderPasswordField.setEchoChar('*');
        
        senderShowPasswordCheckBox = new JCheckBox("Show");
        senderShowPasswordCheckBox.setFont(SMALL_FONT);
        senderShowPasswordCheckBox.setBackground(Color.WHITE);
        senderShowPasswordCheckBox.setPreferredSize(new Dimension(70, 35));
        senderShowPasswordCheckBox.addActionListener(e -> 
            senderPasswordField.setEchoChar(senderShowPasswordCheckBox.isSelected() ? (char)0 : '*'));
        
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
        
        passwordStrengthBar = new JProgressBar(0, 100);
        passwordStrengthBar.setStringPainted(true);
        passwordStrengthBar.setString("Password Strength");
        passwordStrengthBar.setForeground(Color.GRAY);
        passwordStrengthBar.setPreferredSize(new Dimension(220, 20));
        
        JLabel passwordStrengthLabel = new JLabel("Minimum: 8+ chars, uppercase, lowercase, number, special");
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
        
        // Password field with Show checkbox
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Password:*"), gbc);
        gbc.gridx = 1;
        JPanel pwdPanel = new JPanel(new BorderLayout(5, 0));
        pwdPanel.setBackground(Color.WHITE);
        senderRegPasswordField.setPreferredSize(new Dimension(180, 30));
        senderRegPasswordField.setEchoChar('*');
        senderRegShowPasswordCheckBox = new JCheckBox("Show");
        senderRegShowPasswordCheckBox.setFont(SMALL_FONT);
        senderRegShowPasswordCheckBox.setBackground(Color.WHITE);
        senderRegShowPasswordCheckBox.addActionListener(e -> 
            senderRegPasswordField.setEchoChar(senderRegShowPasswordCheckBox.isSelected() ? (char)0 : '*'));
        pwdPanel.add(senderRegPasswordField, BorderLayout.CENTER);
        pwdPanel.add(senderRegShowPasswordCheckBox, BorderLayout.EAST);
        panel.add(pwdPanel, gbc);
        
        // Confirm Password field with Show checkbox
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("Confirm:*"), gbc);
        gbc.gridx = 1;
        JPanel confirmPanel = new JPanel(new BorderLayout(5, 0));
        confirmPanel.setBackground(Color.WHITE);
        senderRegConfirmPwdField.setPreferredSize(new Dimension(180, 30));
        senderRegConfirmPwdField.setEchoChar('*');
        senderRegShowConfirmCheckBox = new JCheckBox("Show");
        senderRegShowConfirmCheckBox.setFont(SMALL_FONT);
        senderRegShowConfirmCheckBox.setBackground(Color.WHITE);
        senderRegShowConfirmCheckBox.addActionListener(e -> 
            senderRegConfirmPwdField.setEchoChar(senderRegShowConfirmCheckBox.isSelected() ? (char)0 : '*'));
        confirmPanel.add(senderRegConfirmPwdField, BorderLayout.CENTER);
        confirmPanel.add(senderRegShowConfirmCheckBox, BorderLayout.EAST);
        panel.add(confirmPanel, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(passwordStrengthBar, gbc);
        
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(passwordStrengthLabel, gbc);
        
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
        
        // Check each requirement
        boolean hasMin8 = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        // Count how many criteria are met (excluding length 10)
        int metCount = 0;
        if (hasMin8) metCount++;
        if (hasUpper) metCount++;
        if (hasLower) metCount++;
        if (hasNumber) metCount++;
        if (hasSpecial) metCount++;
        
        // Calculate percentage (each of 5 criteria = 20%)
        int percentage = metCount * 20;
        passwordStrengthBar.setValue(percentage);
        
        String strengthText;
        Color strengthColor;
        
        if (password.isEmpty()) {
            strengthText = "Enter password";
            strengthColor = Color.GRAY;
        } else if (metCount < 3) {
            strengthText = "Weak";
            strengthColor = WEAK_RED;
        } else if (metCount < 5) {
            strengthText = "Medium";
            strengthColor = MEDIUM_ORANGE;
        } else {
            strengthText = "Strong";
            strengthColor = STRONG_GREEN;
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
        courierPasswordField.setEchoChar('*');
        
        courierShowPasswordCheckBox = new JCheckBox("Show");
        courierShowPasswordCheckBox.setFont(SMALL_FONT);
        courierShowPasswordCheckBox.setBackground(Color.WHITE);
        courierShowPasswordCheckBox.setPreferredSize(new Dimension(70, 35));
        courierShowPasswordCheckBox.addActionListener(e -> 
            courierPasswordField.setEchoChar(courierShowPasswordCheckBox.isSelected() ? (char)0 : '*'));
        
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
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton submitBtn = new JButton("SUBMIT APPLICATION");
        submitBtn.setFont(BUTTON_FONT);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(COURIER_COLOR);
        submitBtn.setPreferredSize(new Dimension(220, 45));
        submitBtn.setBorderPainted(false);
        submitBtn.addActionListener(e -> processCourierApplication());
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setPreferredSize(new Dimension(220, 45));
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(e -> {
            clearCourierApplyFields();
            JOptionPane.showMessageDialog(Login.this, "Application form has been cleared.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
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
        searchBtn.setFont(new Font("Arial", Font.BOLD, 14));
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
        
        resultPanel.add(statusLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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
            
            Driver driver = driverStorage.findDriver(id);
            
            if (driver != null) {
                switch (driver.approvalStatus) {
                    case "APPROVED":
                        statusLabel.setText("STATUS: APPROVED");
                        statusLabel.setForeground(GREEN_SUCCESS);
                        detailLabel.setText("Your application has been approved!");
                        remarkLabel.setText("");
                        break;
                    case "REJECTED":
                        statusLabel.setText("STATUS: REJECTED");
                        statusLabel.setForeground(RED_ERROR);
                        detailLabel.setText("Application rejected.");
                        remarkLabel.setText("Reason: " + (driver.remarks != null ? driver.remarks : "No reason provided"));
                        break;
                    default:
                        statusLabel.setText("STATUS: PENDING");
                        statusLabel.setForeground(YELLOW_PENDING);
                        detailLabel.setText("Under review.");
                        remarkLabel.setText("");
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "User ID not found!");
            }
        });
        
        return panel;
    }
    
    // ========== FORGOT PASSWORD METHODS ==========
    
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
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(themeColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel idJLabel = new JLabel(idLabel);
        idJLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(idJLabel, gbc);
        
        gbc.gridx = 1;
        JTextField idField = new JTextField(15);
        idField.setFont(new Font("Arial", Font.PLAIN, 13));
        idField.setPreferredSize(new Dimension(180, 30));
        formPanel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(180, 30));
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("We'll verify your identity", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        formPanel.add(infoLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
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
            } else if (type == UserType.COURIER) {
                Driver driver = driverStorage.findDriver(id);
                verified = driver != null && email.equals(driver.email);
            }
            
            if (verified) {
                dialog.dispose();
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
        JOptionPane.showMessageDialog(this, 
            "Your verification code is: " + code, 
            "Verification Code", 
            JOptionPane.INFORMATION_MESSAGE);
        
        JDialog codeDialog = new JDialog(this, "Enter Verification Code", true);
        codeDialog.setSize(340, 220);
        codeDialog.setLocationRelativeTo(this);
        codeDialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Enter Verification Code", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(themeColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
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
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Create New Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(themeColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // New Password field with show checkbox
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel newLabel = new JLabel("New:");
        newLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(newLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField pwdField = new JPasswordField(15);
        pwdField.setFont(new Font("Arial", Font.PLAIN, 13));
        pwdField.setPreferredSize(new Dimension(170, 30));
        formPanel.add(pwdField, gbc);
        
        gbc.gridx = 2;
        JCheckBox newShowCheck = new JCheckBox("Show");
        newShowCheck.setFont(new Font("Arial", Font.PLAIN, 10));
        newShowCheck.setBackground(Color.WHITE);
        newShowCheck.addActionListener(e -> pwdField.setEchoChar(newShowCheck.isSelected() ? (char)0 : '*'));
        formPanel.add(newShowCheck, gbc);
        
        // Confirm Password field with show checkbox
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        confirmField.setFont(new Font("Arial", Font.PLAIN, 13));
        confirmField.setPreferredSize(new Dimension(170, 30));
        formPanel.add(confirmField, gbc);
        
        gbc.gridx = 2;
        JCheckBox confirmShowCheck = new JCheckBox("Show");
        confirmShowCheck.setFont(new Font("Arial", Font.PLAIN, 10));
        confirmShowCheck.setBackground(Color.WHITE);
        confirmShowCheck.addActionListener(e -> confirmField.setEchoChar(confirmShowCheck.isSelected() ? (char)0 : '*'));
        formPanel.add(confirmShowCheck, gbc);
        
        // Password Strength Bar
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JProgressBar resetStrengthBar = new JProgressBar(0, 100);
        resetStrengthBar.setStringPainted(true);
        resetStrengthBar.setString("Password Strength");
        resetStrengthBar.setForeground(Color.GRAY);
        resetStrengthBar.setPreferredSize(new Dimension(300, 20));
        formPanel.add(resetStrengthBar, gbc);
        
        // Password strength label
        gbc.gridy = 3;
        JLabel strengthLabel = new JLabel("Minimum: 8+ chars, uppercase, lowercase, number, special");
        strengthLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        strengthLabel.setForeground(Color.DARK_GRAY);
        formPanel.add(strengthLabel, gbc);
        
        // Add document listener for password strength
        pwdField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                checkResetPasswordStrength(pwdField, resetStrengthBar);
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                checkResetPasswordStrength(pwdField, resetStrengthBar);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                checkResetPasswordStrength(pwdField, resetStrengthBar);
            }
        });
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
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
            
            String validationError = getPasswordValidationMessage(pwd);
            if (validationError != null) {
                JOptionPane.showMessageDialog(dialog, validationError, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (type == UserType.SENDER) {
                senderDatabase.get(userId).passwordHash = hashPassword(pwd);
                saveSenderData();
                senderUsernameField.setText(userId);
            } else {
                Driver driver = driverStorage.findDriver(userId);
                if (driver != null) {
                    driver.passwordHash = hashPassword(pwd);
                    driverStorage.saveDrivers();
                    courierUserIdField.setText(userId);
                }
            }
            
            JOptionPane.showMessageDialog(dialog, "Password reset successful!\nYou can now login.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        dialog.setVisible(true);
    }
    
    private void checkResetPasswordStrength(JPasswordField pwdField, JProgressBar strengthBar) {
        String password = new String(pwdField.getPassword());
        
        // Check each requirement
        boolean hasMin8 = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        // Count how many criteria are met
        int metCount = 0;
        if (hasMin8) metCount++;
        if (hasUpper) metCount++;
        if (hasLower) metCount++;
        if (hasNumber) metCount++;
        if (hasSpecial) metCount++;
        
        // Calculate percentage (each of 5 criteria = 20%)
        int percentage = metCount * 20;
        strengthBar.setValue(percentage);
        
        String strengthText;
        Color strengthColor;
        
        if (password.isEmpty()) {
            strengthText = "Enter password";
            strengthColor = Color.GRAY;
        } else if (metCount < 3) {
            strengthText = "Weak";
            strengthColor = WEAK_RED;
        } else if (metCount < 5) {
            strengthText = "Medium";
            strengthColor = MEDIUM_ORANGE;
        } else {
            strengthText = "Strong";
            strengthColor = STRONG_GREEN;
        }
        
        strengthBar.setString(strengthText);
        strengthBar.setForeground(strengthColor);
    }
    
    // ========== PROCESS METHODS ==========
    private void processAdminLogin() {
        String username = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (!isNotEmpty(username, password)) {
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
        
        if (!isNotEmpty(username, password)) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }
        
        if (senderDatabase.containsKey(username)) {
            SenderAccount acc = senderDatabase.get(username);
            if (verifyPassword(password, acc.passwordHash)) {
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + acc.fullName + "!");
                new SenderDashboard(acc.fullName, acc.email, acc.phone, username).setVisible(true);
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
        
        if (!isNotEmpty(userId, password)) {
            JOptionPane.showMessageDialog(this, "Please enter Courier ID and password!");
            return;
        }
        
        DriverStorage freshStorage = new DriverStorage();
        Driver driver = freshStorage.findDriver(userId);
        
        if (driver != null) {
            if (freshStorage.verifyPassword(userId, password)) {
                if ("APPROVED".equals(driver.approvalStatus)) {
                    JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + driver.name + "!");
                    setVisible(false);
                    CourierDashboard dashboard = new CourierDashboard(driver);
                    dashboard.setVisible(true);
                    dispose();
                } else if ("PENDING".equals(driver.approvalStatus)) {
                    JOptionPane.showMessageDialog(this, 
                        "Your account is pending admin approval.\nPlease check back later.",
                        "Pending Approval", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else if ("REJECTED".equals(driver.approvalStatus)) {
                    String reason = driver.remarks != null ? "\nReason: " + driver.remarks : "";
                    JOptionPane.showMessageDialog(this, 
                        "Your application was rejected." + reason,
                        "Rejected", 
                        JOptionPane.ERROR_MESSAGE);
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
        
        if (!isNotEmpty(name, email, phone, username, password)) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format!");
            return;
        }
        
        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Invalid phone number! Only digits allowed (10-11 digits)");
            return;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        String validationError = getPasswordValidationMessage(password);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (senderDatabase.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists!");
            return;
        }
        
        senderDatabase.put(username, new SenderAccount(name, email, phone, username, password));
        saveSenderData();
        
        JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
        
        clearSenderRegisterFields();
        
        senderTabbedPane.setSelectedIndex(0);
        senderUsernameField.setText(username);
    }
    
    private void processCourierApplication() {
        String name = courierRegNameField.getText().trim();
        String email = courierRegEmailField.getText().trim();
        String phone = courierRegPhoneField.getText().trim();
        String ic = courierRegIcField.getText().trim();
        String license = (String) courierRegLicenseCombo.getSelectedItem();
        
        if (!isNotEmpty(name, email, phone, ic)) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format! Example: name@domain.com");
            return;
        }
        
        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Invalid phone number! Only digits allowed (10-11 digits)");
            return;
        }
        
        if (!isValidIC(ic)) {
            JOptionPane.showMessageDialog(this, "Invalid IC number! Only digits allowed (12 digits)");
            return;
        }
        
        if (driverStorage.isIcNumberExists(ic)) {
            JOptionPane.showMessageDialog(this, "This IC number is already registered!");
            return;
        }
        
        if (driverStorage.isEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "This email is already registered!");
            return;
        }
        
        if (driverStorage.isPhoneExists(phone)) {
            JOptionPane.showMessageDialog(this, "This phone number is already registered!");
            return;
        }
        
        if (licensePhotoPath == null || icPhotoPath == null) {
            JOptionPane.showMessageDialog(this, "Please upload both IC and License photos!");
            return;
        }
        
        String userId = driverStorage.generateDriverId();
        String password = "courier" + (int)(Math.random() * 9000 + 1000);
        
        Driver driver = new Driver(userId, name, phone, email, license, "2025-12-31");
        driver.icNumber = ic;
        driver.licenseType = license;
        driver.photoPath = licensePhotoPath;
        driver.passwordHash = hashPassword(password);
        driver.approvalStatus = "PENDING";
        driver.workStatus = "Off Duty";
        
        driverStorage.addDriver(driver);
        
        String msg = String.format(
            "Application Submitted!\n\nUser ID: %s\nPassword: %s\n\nPlease save these credentials.\n\n" +
            "Your application is pending admin approval.",
            userId, password);
        
        JOptionPane.showMessageDialog(this, msg, "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
        
        clearCourierApplyFields();
        
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