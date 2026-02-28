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

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton activeButton;
    private JLabel timeLabel;
    
    // Table Components
    private JTable deliveryTable;
    private DefaultTableModel deliveryModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> statusCombo;
    private JTextArea remarksArea;
    private JLabel selectedParcelLabel;
    private JLabel photoFileNameLabel;
    private File selectedPhotoFile;
    private JButton uploadPhotoBtn;

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

    // ================= TOP BAR (with logo from Login) =================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY_GREEN);
        bar.setPreferredSize(new Dimension(getWidth(), 80)); // Increased height to match Login
        
        // ===== Logo Panel with Image (same as Login) =====
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        logoPanel.setBackground(PRIMARY_GREEN);
        
        // Try to load logo from file - same path as Login class
        try {
            // Path to your logo - adjust as needed
            String logoPath = "C:\\Users\\User\\Documents\\LOGISTICS\\logo1.png";
            
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                ImageIcon logoIcon = new ImageIcon(logoPath);
                // Scale the image to appropriate size
                Image scaledImage = logoIcon.getImage().getScaledInstance(180, 50, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                logoPanel.add(logoLabel);
            } else {
                // Fallback to text if logo not found
                JLabel fallbackLabel = new JLabel("LogiXpress");
                fallbackLabel.setFont(new Font("Arial", Font.BOLD, 32));
                fallbackLabel.setForeground(Color.WHITE);
                logoPanel.add(fallbackLabel);
            }
        } catch (Exception e) {
            // If any error loading image, show text
            JLabel fallbackLabel = new JLabel("LogiXpress");
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 32));
            fallbackLabel.setForeground(Color.WHITE);
            logoPanel.add(fallbackLabel);
        }
        
        bar.add(logoPanel, BorderLayout.WEST);
        
        // Right panel with time and exit button (matching Login style)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setBackground(PRIMARY_GREEN);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 18)); // Match Login font
        timeLabel.setForeground(Color.WHITE);
        rightPanel.add(timeLabel);

        Timer timer = new Timer(1000, e -> timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        timer.start();
        
        // Exit button matching Login style
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(new Color(220, 20, 60)); // RED_BTN color from Login
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setPreferredSize(new Dimension(100, 45));
        exitBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit LogiXpress?", "Exit", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        rightPanel.add(exitBtn);

        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(GREEN_DARK);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));
        JLabel logo = new JLabel("LogiXpress", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logoPanel.add(logo, BorderLayout.CENTER);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(6, 1, 0, 2));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(5, 8, 10, 8));

        menu.add(createSidebarButton("👤 My Profile", "PROFILE", true));
        menu.add(createSidebarButton("📦 Deliveries & Updates", "DELIVERIES", false));
        menu.add(createSidebarButton("🚛 Vehicle Report", "VEHICLE", false));
        menu.add(createSidebarButton("📊 Stats", "STATS", false));

        sidebar.add(menu, BorderLayout.CENTER);
        return sidebar;
    }

    private JButton createSidebarButton(String text, String card, boolean selected) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        // Split the text into icon and label parts
        String icon = text.substring(0, 2);
        String label = text.substring(2);
        
        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setPreferredSize(new Dimension(30, 20));
        
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(Color.WHITE);
        
        content.add(iconLabel, BorderLayout.WEST);
        content.add(textLabel, BorderLayout.CENTER);
        
        btn.add(content, BorderLayout.CENTER);
        
        // Button styling
        btn.setBackground(selected ? SIDEBAR_SELECTED : GREEN_DARK);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) activeButton = btn;

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(GREEN_DARK);
            }
            btn.setBackground(SIDEBAR_SELECTED);
            activeButton = btn;
            cardLayout.show(contentPanel, card);
        });

        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(new Color(50, 80, 50));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(GREEN_DARK);
                }
            }
        });
        
        return btn;
    }

    // ================= CONTENT PANELS =================
    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);

        contentPanel.add(createEditableProfilePage(), "PROFILE");
        contentPanel.add(createCombinedDeliveryPage(), "DELIVERIES");
        contentPanel.add(createEnhancedVehiclePage(), "VEHICLE");
        contentPanel.add(createStatsPage(), "STATS");

        return contentPanel;
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

    /* --- COMBINED DELIVERY PAGE --- */
    private JPanel createCombinedDeliveryPage() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        // TOP SECTION: Delivery List
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BG_LIGHT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("Delivery Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchField = new JTextField(25);
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search by ID, recipient, location...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchPanel.add(searchField);
        header.add(searchPanel, BorderLayout.EAST);

        topPanel.add(header, BorderLayout.NORTH);

        String[] columns = {"Parcel ID", "Recipient", "Phone No.", "Location", "Status", "Priority", "Last Updated"};
        deliveryModel = new DefaultTableModel(new Object[][]{
            {"LX-901", "Justin Khoo", "012-3456789", "Petaling Jaya", "In Transit", "High", "10:30 AM"},
            {"LX-902", "Sarah Tan", "013-4567890", "Subang Jaya", "Pending", "Normal", "09:15 AM"},
            {"LX-903", "Ahmad Zaki", "014-5678901", "Kuala Lumpur", "Delivered", "Normal", "Yesterday"},
            {"LX-904", "Linda Chen", "015-6789012", "Shah Alam", "Pending", "Urgent", "08:45 AM"},
            {"LX-905", "Muthu Kumar", "016-7890123", "Bangsar", "In Transit", "High", "11:20 AM"},
            {"LX-906", "Emily Wong", "017-8901234", "Damansara", "Out for Delivery", "Normal", "10:05 AM"}
        }, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        deliveryTable = new JTable(deliveryModel);
        styleCombinedTable(deliveryTable);

        rowSorter = new TableRowSorter<>(deliveryModel);
        deliveryTable.setRowSorter(rowSorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        deliveryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedParcelDetails();
            }
        });

        topPanel.add(new JScrollPane(deliveryTable), BorderLayout.CENTER);

        // BOTTOM SECTION: Update Status Form
        JPanel bottomPanel = createUpdateForm();

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createUpdateForm() {
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel formTitle = new JLabel("Update Delivery Status");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(PRIMARY_GREEN);
        formPanel.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);

        // Selected Parcel
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.2;
        JLabel parcelLabel = new JLabel("Selected Parcel:");
        parcelLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(parcelLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        selectedParcelLabel = new JLabel("None selected");
        selectedParcelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectedParcelLabel.setForeground(PRIMARY_GREEN);
        fieldsPanel.add(selectedParcelLabel, gbc);

        // Status Selection
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.2;
        JLabel statusLabel = new JLabel("New Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        statusCombo = new JComboBox<>(new String[]{
            "Select Status...",
            "Out for Delivery",
            "Delivered", 
            "Failed Delivery",
            "Returning to Hub",
            "Held at Facility"
        });
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        statusCombo.setBackground(Color.WHITE);
        fieldsPanel.add(statusCombo, gbc);

        // Photo Upload
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.2;
        JLabel photoLabel = new JLabel("Photo Proof:");
        photoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(photoLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        photoPanel.setBackground(Color.WHITE);
        
        uploadPhotoBtn = new JButton("📷 Select Photo");
        uploadPhotoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        uploadPhotoBtn.setBackground(Color.WHITE);
        uploadPhotoBtn.setForeground(PRIMARY_GREEN);
        uploadPhotoBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN));
        uploadPhotoBtn.setPreferredSize(new Dimension(100, 25));
        uploadPhotoBtn.addActionListener(e -> selectPhotoFromFile());
        
        photoFileNameLabel = new JLabel("No file selected");
        photoFileNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        photoFileNameLabel.setForeground(TEXT_GRAY);
        
        JButton removePhotoBtn = new JButton("✕");
        removePhotoBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        removePhotoBtn.setBackground(Color.WHITE);
        removePhotoBtn.setForeground(Color.RED);
        removePhotoBtn.setBorder(BorderFactory.createLineBorder(Color.RED));
        removePhotoBtn.setPreferredSize(new Dimension(22, 22));
        removePhotoBtn.addActionListener(e -> removePhoto());
        
        photoPanel.add(uploadPhotoBtn);
        photoPanel.add(Box.createHorizontalStrut(5));
        photoPanel.add(photoFileNameLabel);
        photoPanel.add(Box.createHorizontalStrut(5));
        photoPanel.add(removePhotoBtn);
        
        fieldsPanel.add(photoPanel, gbc);

        // Remarks
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTH;
        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(remarksLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        remarksArea = new JTextArea(2, 30);
        remarksArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        remarksArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksScroll.setBorder(null);
        remarksScroll.setPreferredSize(new Dimension(300, 50));
        fieldsPanel.add(remarksScroll, gbc);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(TEXT_GRAY);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setPreferredSize(new Dimension(80, 28));
        clearBtn.addActionListener(e -> clearUpdateForm());

        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateBtn.setBackground(PRIMARY_GREEN);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        updateBtn.setPreferredSize(new Dimension(120, 28));
        updateBtn.addActionListener(e -> performStatusUpdate());

        buttonPanel.add(clearBtn);
        buttonPanel.add(updateBtn);

        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        return formPanel;
    }

    private void selectPhotoFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Delivery Proof Photo");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = fileChooser.getSelectedFile();
            
            String fileName = selectedPhotoFile.getName();
            if (fileName.length() > 30) {
                fileName = fileName.substring(0, 27) + "...";
            }
            photoFileNameLabel.setText(fileName);
            photoFileNameLabel.setForeground(PRIMARY_GREEN);
            uploadPhotoBtn.setText("📷 Change Photo");
        }
    }

    private void removePhoto() {
        selectedPhotoFile = null;
        photoFileNameLabel.setText("No file selected");
        photoFileNameLabel.setForeground(TEXT_GRAY);
        uploadPhotoBtn.setText("📷 Select Photo");
    }

    private void styleCombinedTable(JTable table) {
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(8, 3));
        table.setSelectionBackground(GREEN_LIGHT);
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN));

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String val = v.toString();
                
                if (val.equals("Delivered")) {
                    lbl.setForeground(new Color(46, 125, 50));
                    lbl.setText("✓ " + val);
                } else if (val.equals("Out for Delivery")) {
                    lbl.setForeground(new Color(25, 118, 210));
                    lbl.setText("🚚 " + val);
                } else if (val.equals("In Transit")) {
                    lbl.setForeground(new Color(25, 118, 210));
                    lbl.setText("⏱️ " + val);
                } else if (val.equals("Pending")) {
                    lbl.setForeground(new Color(237, 108, 2));
                    lbl.setText("⏳ " + val);
                } else if (val.equals("Failed Delivery")) {
                    lbl.setForeground(new Color(198, 40, 40));
                    lbl.setText("❌ " + val);
                }
                
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return lbl;
            }
        });

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String val = v.toString();
                
                if (val.equals("Urgent")) {
                    lbl.setForeground(new Color(198, 40, 40));
                    lbl.setText("🔴 " + val);
                } else if (val.equals("High")) {
                    lbl.setForeground(new Color(237, 108, 2));
                    lbl.setText("🟠 " + val);
                } else {
                    lbl.setForeground(new Color(46, 125, 50));
                    lbl.setText("🟢 " + val);
                }
                
                return lbl;
            }
        });
    }

    private void updateSelectedParcelDetails() {
        int row = deliveryTable.getSelectedRow();
        if (row != -1) {
            String parcelId = deliveryTable.getValueAt(row, 0).toString();
            String recipient = deliveryTable.getValueAt(row, 1).toString();
            String phoneNo = deliveryTable.getValueAt(row, 2).toString();
            String currentStatus = deliveryTable.getValueAt(row, 4).toString();
            
            selectedParcelLabel.setText(String.format("%s - %s (%s) [Current: %s]", 
                parcelId, recipient, phoneNo, currentStatus));
            
            if (currentStatus.equals("Pending")) {
                statusCombo.setSelectedItem("Out for Delivery");
            } else if (currentStatus.equals("In Transit")) {
                statusCombo.setSelectedItem("Delivered");
            } else if (currentStatus.equals("Out for Delivery")) {
                statusCombo.setSelectedItem("Delivered");
            } else {
                statusCombo.setSelectedIndex(0);
            }
            
            remarksArea.setText("Update for " + parcelId + ": ");
            removePhoto();
        }
    }

    private void performStatusUpdate() {
        int row = deliveryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a delivery from the list first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedStatus = (String) statusCombo.getSelectedItem();
        if (selectedStatus == null || selectedStatus.equals("Select Status...")) {
            JOptionPane.showMessageDialog(this,
                "Please select a new status.",
                "Invalid Status",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ((selectedStatus.equals("Delivered") || selectedStatus.equals("Failed Delivery")) && selectedPhotoFile == null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "It's recommended to attach a photo for " + selectedStatus + " status. Continue without photo?",
                "Photo Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String remarks = remarksArea.getText().trim();
        if (remarks.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "No remarks added. Continue with update?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String parcelId = deliveryTable.getValueAt(row, 0).toString();
        String oldStatus = deliveryTable.getValueAt(row, 4).toString();
        
        deliveryModel.setValueAt(selectedStatus, row, 4);
        deliveryModel.setValueAt(new SimpleDateFormat("hh:mm a").format(new Date()), row, 6);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String timestamp = sdf.format(new Date());
        
        String photoInfo = (selectedPhotoFile != null) ? 
            "\nPhoto: " + selectedPhotoFile.getName() : 
            "\nPhoto: Not attached";
        
        String message = String.format(
            "Parcel %s status updated:\n" +
            "From: %s\n" +
            "To: %s\n" +
            "Time: %s%s\n" +
            "Remarks: %s",
            parcelId, oldStatus, selectedStatus, timestamp, photoInfo,
            remarks.isEmpty() ? "No remarks" : remarks
        );
        
        JOptionPane.showMessageDialog(this,
            message,
            "Status Updated Successfully",
            JOptionPane.INFORMATION_MESSAGE);
        
        deliveryTable.clearSelection();
        clearUpdateForm();
        deliveryTable.repaint();
    }

    private void clearUpdateForm() {
        selectedParcelLabel.setText("None selected");
        statusCombo.setSelectedIndex(0);
        remarksArea.setText("");
        removePhoto();
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
        bar.setPreferredSize(new Dimension(getWidth(), 30));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        JLabel status = new JLabel("  System Status: Online | Session: Courier_88029 | " + 
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        status.setForeground(TEXT_GRAY);
        bar.add(status, BorderLayout.WEST);
        
        JLabel deliveryCount = new JLabel("Pending Deliveries: 3  ");
        deliveryCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deliveryCount.setForeground(PRIMARY_GREEN);
        bar.add(deliveryCount, BorderLayout.EAST);
        
        return bar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CourierDashboard().setVisible(true));
    }
}