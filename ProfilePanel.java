package courier;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ProfilePanel extends JPanel {
    
    private Driver currentDriver;
    private DriverStorage driverStorage;
    
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
    
    // Colors
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_DARK = new Color(27, 94, 32);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private static final Color SUCCESS = new Color(40, 167, 69);
    
    public ProfilePanel(Driver driver) {
        this.currentDriver = driver;
        this.driverStorage = new DriverStorage();
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel profileCard = new JPanel(new BorderLayout(20, 20));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 3, 3, new Color(0, 0, 0, 20)),
            new EmptyBorder(30, 30, 30, 30)));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("My Profile");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(PRIMARY_GREEN);
        headerPanel.add(headerLabel);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.weightx = 1.0;
        
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
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_GRAY);
        nameLabel.setPreferredSize(labelSize);
        nameLabel.setMinimumSize(labelSize);
        contentPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        nameField = new JTextField(currentDriver != null ? currentDriver.name : "");
        styleTextField(nameField, fieldSize);
        contentPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel idLabel = new JLabel("Staff ID:");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(TEXT_GRAY);
        idLabel.setPreferredSize(labelSize);
        idLabel.setMinimumSize(labelSize);
        contentPanel.add(idLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        staffIdField = new JTextField(currentDriver != null ? currentDriver.id : "");
        styleTextField(staffIdField, fieldSize);
        staffIdField.setEditable(false);
        staffIdField.setBackground(new Color(240, 240, 240));
        contentPanel.add(staffIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(TEXT_GRAY);
        emailLabel.setPreferredSize(labelSize);
        emailLabel.setMinimumSize(labelSize);
        contentPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        emailField = new JTextField(currentDriver != null ? currentDriver.email : "");
        styleTextField(emailField, fieldSize);
        contentPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setForeground(TEXT_GRAY);
        phoneLabel.setPreferredSize(labelSize);
        phoneLabel.setMinimumSize(labelSize);
        contentPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        phoneField = new JTextField(currentDriver != null ? currentDriver.phone : "");
        styleTextField(phoneField, fieldSize);
        contentPanel.add(phoneField, gbc);
        
        // License Info
        gbc.gridx = 0; gbc.gridy = row;
        JLabel licenseLabel = new JLabel("License:");
        licenseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        licenseLabel.setForeground(TEXT_GRAY);
        licenseLabel.setPreferredSize(labelSize);
        licenseLabel.setMinimumSize(labelSize);
        contentPanel.add(licenseLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField licenseField = new JTextField(currentDriver != null ? 
            currentDriver.licenseNumber + " (" + currentDriver.licenseType + ")" : "");
        styleTextField(licenseField, fieldSize);
        licenseField.setEditable(false);
        licenseField.setBackground(new Color(240, 240, 240));
        contentPanel.add(licenseField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel icLabel = new JLabel("IC Number:");
        icLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        icLabel.setForeground(TEXT_GRAY);
        icLabel.setPreferredSize(labelSize);
        icLabel.setMinimumSize(labelSize);
        contentPanel.add(icLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField icField = new JTextField(currentDriver != null ? currentDriver.icNumber : "");
        styleTextField(icField, fieldSize);
        icField.setEditable(false);
        icField.setBackground(new Color(240, 240, 240));
        contentPanel.add(icField, gbc);
        
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
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleTypeLabel = new JLabel("Vehicle:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleTypeLabel.setForeground(TEXT_GRAY);
        vehicleTypeLabel.setPreferredSize(labelSize);
        vehicleTypeLabel.setMinimumSize(labelSize);
        contentPanel.add(vehicleTypeLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        vehicleModelField = new JTextField(currentDriver != null && currentDriver.vehicleId != null ? 
            currentDriver.vehicleId : "Not Assigned");
        styleTextField(vehicleModelField, fieldSize);
        vehicleModelField.setEditable(false);
        vehicleModelField.setBackground(new Color(240, 240, 240));
        contentPanel.add(vehicleModelField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel plateLabel = new JLabel("Plate No:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plateLabel.setForeground(TEXT_GRAY);
        plateLabel.setPreferredSize(labelSize);
        plateLabel.setMinimumSize(labelSize);
        contentPanel.add(plateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        plateNoField = new JTextField("VAF 1234");
        styleTextField(plateNoField, fieldSize);
        plateNoField.setEditable(false);
        plateNoField.setBackground(new Color(240, 240, 240));
        contentPanel.add(plateNoField, gbc);
        
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
        
        mileageField = new JTextField(String.format("%,.0f km", currentDriver.totalDistance));
        mileageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mileageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        mileageField.setBackground(new Color(240, 240, 240));
        mileageField.setEditable(false);
        mileageField.setHorizontalAlignment(JTextField.RIGHT);
        
        mileagePanel.add(mileageField, BorderLayout.CENTER);
        
        contentPanel.add(mileagePanel, gbc);
        
        // Load existing photo if any
        if (currentDriver != null && currentDriver.photoPath != null && !currentDriver.photoPath.isEmpty()) {
            loadDriverPhoto(currentDriver.photoPath);
        }
        
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
        
        profileCard.add(headerPanel, BorderLayout.NORTH);
        profileCard.add(scrollPane, BorderLayout.CENTER);
        profileCard.add(buttonPanel, BorderLayout.SOUTH);
        
        add(profileCard, BorderLayout.CENTER);
    }
    
    private void styleTextField(JTextField field, Dimension size) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        field.setBackground(new Color(250, 250, 250));
        field.setPreferredSize(size);
        field.setMinimumSize(size);
        field.setMaximumSize(size);
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
    
    private void uploadProfilePhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Photo");
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            profilePhotoFile = fileChooser.getSelectedFile();
            
            ImageIcon icon = new ImageIcon(profilePhotoFile.getPath());
            Image image = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            profilePhotoLabel.setIcon(new ImageIcon(image));
            profilePhotoLabel.setText("");
        }
    }
    
    private void saveProfileChanges() {
        // Update current driver object
        currentDriver.name = nameField.getText().trim();
        currentDriver.email = emailField.getText().trim();
        currentDriver.phone = phoneField.getText().trim();
        
        // If new photo
        if (profilePhotoFile != null) {
            // Save photo to driver photos directory
            String photoDir = "driver_photos/";
            File dir = new File(photoDir);
            if (!dir.exists()) dir.mkdirs();
            
            String extension = profilePhotoFile.getName().substring(profilePhotoFile.getName().lastIndexOf('.'));
            String newPhotoPath = photoDir + currentDriver.id + extension;
            
            try {
                java.nio.file.Files.copy(profilePhotoFile.toPath(), 
                    new File(newPhotoPath).toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                currentDriver.photoPath = newPhotoPath;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Save to file
        driverStorage.updateDriver(currentDriver);
        
        String message = "Profile updated successfully!\n\n" +
            "Name: " + currentDriver.name + "\n" +
            "Staff ID: " + currentDriver.id + "\n" +
            "Email: " + currentDriver.email + "\n" +
            "Phone: " + currentDriver.phone;
        
        JOptionPane.showMessageDialog(this,
            message,
            "Profile Saved",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Clear photo file reference
        profilePhotoFile = null;
    }
    
    // Method to refresh profile data when driver is updated externally
    public void refreshProfile(Driver updatedDriver) {
        this.currentDriver = updatedDriver;
        nameField.setText(currentDriver.name);
        emailField.setText(currentDriver.email);
        phoneField.setText(currentDriver.phone);
        mileageField.setText(String.format("%,.0f km", currentDriver.totalDistance));
        
        if (currentDriver.photoPath != null && !currentDriver.photoPath.isEmpty()) {
            loadDriverPhoto(currentDriver.photoPath);
        }
    }
    
    // Get updated driver
    public Driver getUpdatedDriver() {
        return currentDriver;
    }
}