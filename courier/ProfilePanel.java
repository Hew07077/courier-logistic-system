// ProfilePanel.java
package courier;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ProfilePanel extends JPanel {
    
    private Driver currentDriver;
    private DriverStorage driverStorage;
    private Map<String, VehicleData> vehicleDataMap;
    
    // Profile Components
    private JLabel profilePhotoLabel;
    private File profilePhotoFile;
    private JTextField nameField;
    private JTextField staffIdField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField vehicleModelField;
    private JTextField plateNoField;
    private JTextField vehicleStatusField;
    private JTextField vehicleTypeField;
    private JTextField roadTaxExpiryField;
    private JTextField fuelTypeField;
    private JTextField vehicleIdField;
    
    // Colors
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_DARK = new Color(27, 94, 32);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color WARNING_ORANGE = new Color(255, 152, 0);
    
    // Vehicle Data Class
    private static class VehicleData {
        String vehicleId;
        String vehicleType;
        String licensePlate;
        String vehicleModel;
        long roadTaxExpiry;
        String status;
        String assignedTo;
        String fuelType;
        
        VehicleData(String vehicleId, String vehicleType, String licensePlate, 
                   String vehicleModel, long roadTaxExpiry, String status, 
                   String assignedTo, String fuelType) {
            this.vehicleId = vehicleId;
            this.vehicleType = vehicleType;
            this.licensePlate = licensePlate;
            this.vehicleModel = vehicleModel;
            this.roadTaxExpiry = roadTaxExpiry;
            this.status = status;
            this.assignedTo = assignedTo;
            this.fuelType = fuelType;
        }
        
        String getFormattedExpiryDate() {
            if (roadTaxExpiry <= 0) return "N/A";
            Date date = new Date(roadTaxExpiry);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(date);
        }
        
        boolean isExpired() {
            if (roadTaxExpiry <= 0) return false;
            return System.currentTimeMillis() > roadTaxExpiry;
        }
    }
    
    public ProfilePanel(Driver driver) {
        this.currentDriver = driver;
        this.driverStorage = new DriverStorage();
        this.vehicleDataMap = new HashMap<>();
        
        loadVehicleData();
        initUI();
    }
    
    private void loadVehicleData() {
        vehicleDataMap.clear();
        File vehicleFile = new File("vehicles.txt");
        
        if (!vehicleFile.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(vehicleFile))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                if (isFirstLine && line.contains("VEHICLE_ID")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                String[] parts = line.split("\\|");
                
                if (parts.length >= 8) {
                    long roadTaxExpiry = 0;
                    try {
                        roadTaxExpiry = Long.parseLong(parts[4].trim());
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                    
                    VehicleData vehicle = new VehicleData(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        roadTaxExpiry,
                        parts[5].trim(),
                        parts[6].trim(),
                        parts[7].trim()
                    );
                    vehicleDataMap.put(vehicle.vehicleId, vehicle);
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private VehicleData getAssignedVehicle() {
        if (currentDriver == null) return null;
        
        for (VehicleData vehicle : vehicleDataMap.values()) {
            if (vehicle.assignedTo != null && !vehicle.assignedTo.equals("Unassigned")) {
                if (vehicle.assignedTo.equalsIgnoreCase(currentDriver.name)) {
                    return vehicle;
                }
            }
        }
        
        if (currentDriver.vehicleId != null && !currentDriver.vehicleId.isEmpty()) {
            return vehicleDataMap.get(currentDriver.vehicleId);
        }
        
        return null;
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
        
        // Photo Section
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
        
        // Personal Information Section
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
        nameField.setEditable(false);
        nameField.setBackground(new Color(240, 240, 240));
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
        emailField.setEditable(false);
        emailField.setBackground(new Color(240, 240, 240));
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
        phoneField.setEditable(false);
        phoneField.setBackground(new Color(240, 240, 240));
        contentPanel.add(phoneField, gbc);
        
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
        
        // Vehicle Information Section
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel vehicleSectionLabel = new JLabel("VEHICLE INFORMATION");
        vehicleSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vehicleSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(vehicleSectionLabel, gbc);
        
        gbc.gridwidth = 1;
        
        VehicleData assignedVehicle = getAssignedVehicle();
        
        if (assignedVehicle == null) {
            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
            JLabel noVehicleLabel = new JLabel("No vehicle assigned yet. Please contact administrator.");
            noVehicleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            noVehicleLabel.setForeground(WARNING_ORANGE);
            contentPanel.add(noVehicleLabel, gbc);
            gbc.gridwidth = 1;
        } else {
            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
            JLabel assignedLabel = new JLabel("Vehicle successfully assigned to you");
            assignedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            assignedLabel.setForeground(PRIMARY_GREEN);
            contentPanel.add(assignedLabel, gbc);
            gbc.gridwidth = 1;
        }
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleIdLabel = new JLabel("Vehicle ID:");
        vehicleIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleIdLabel.setForeground(TEXT_GRAY);
        vehicleIdLabel.setPreferredSize(labelSize);
        vehicleIdLabel.setMinimumSize(labelSize);
        contentPanel.add(vehicleIdLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        vehicleIdField = new JTextField(assignedVehicle != null ? assignedVehicle.vehicleId : "Not Assigned");
        styleTextField(vehicleIdField, fieldSize);
        vehicleIdField.setEditable(false);
        vehicleIdField.setBackground(new Color(240, 240, 240));
        contentPanel.add(vehicleIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleModelLabel = new JLabel("Vehicle Model:");
        vehicleModelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleModelLabel.setForeground(TEXT_GRAY);
        vehicleModelLabel.setPreferredSize(labelSize);
        vehicleModelLabel.setMinimumSize(labelSize);
        contentPanel.add(vehicleModelLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        vehicleModelField = new JTextField(assignedVehicle != null ? assignedVehicle.vehicleModel : "Not Assigned");
        styleTextField(vehicleModelField, fieldSize);
        vehicleModelField.setEditable(false);
        vehicleModelField.setBackground(new Color(240, 240, 240));
        contentPanel.add(vehicleModelField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel plateLabel = new JLabel("License Plate:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plateLabel.setForeground(TEXT_GRAY);
        plateLabel.setPreferredSize(labelSize);
        plateLabel.setMinimumSize(labelSize);
        contentPanel.add(plateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        plateNoField = new JTextField(assignedVehicle != null ? assignedVehicle.licensePlate : "N/A");
        styleTextField(plateNoField, fieldSize);
        plateNoField.setEditable(false);
        plateNoField.setBackground(new Color(240, 240, 240));
        contentPanel.add(plateNoField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleTypeLabel = new JLabel("Vehicle Type:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleTypeLabel.setForeground(TEXT_GRAY);
        vehicleTypeLabel.setPreferredSize(labelSize);
        vehicleTypeLabel.setMinimumSize(labelSize);
        contentPanel.add(vehicleTypeLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        vehicleTypeField = new JTextField(assignedVehicle != null ? assignedVehicle.vehicleType : "N/A");
        styleTextField(vehicleTypeField, fieldSize);
        vehicleTypeField.setEditable(false);
        vehicleTypeField.setBackground(new Color(240, 240, 240));
        contentPanel.add(vehicleTypeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel fuelLabel = new JLabel("Fuel Type:");
        fuelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fuelLabel.setForeground(TEXT_GRAY);
        fuelLabel.setPreferredSize(labelSize);
        fuelLabel.setMinimumSize(labelSize);
        contentPanel.add(fuelLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        fuelTypeField = new JTextField(assignedVehicle != null ? assignedVehicle.fuelType : "N/A");
        styleTextField(fuelTypeField, fieldSize);
        fuelTypeField.setEditable(false);
        fuelTypeField.setBackground(new Color(240, 240, 240));
        contentPanel.add(fuelTypeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel roadTaxLabel = new JLabel("Road Tax Expiry:");
        roadTaxLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roadTaxLabel.setForeground(TEXT_GRAY);
        roadTaxLabel.setPreferredSize(labelSize);
        roadTaxLabel.setMinimumSize(labelSize);
        contentPanel.add(roadTaxLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        String expiryText = assignedVehicle != null ? assignedVehicle.getFormattedExpiryDate() : "N/A";
        roadTaxExpiryField = new JTextField(expiryText);
        styleTextField(roadTaxExpiryField, fieldSize);
        roadTaxExpiryField.setEditable(false);
        roadTaxExpiryField.setBackground(new Color(240, 240, 240));
        
        if (assignedVehicle != null && assignedVehicle.isExpired()) {
            roadTaxExpiryField.setForeground(Color.RED);
            roadTaxExpiryField.setText("EXPIRED - " + expiryText);
        } else if (assignedVehicle != null) {
            roadTaxExpiryField.setForeground(PRIMARY_GREEN);
        }
        contentPanel.add(roadTaxExpiryField, gbc);
        
        gbc.gridx = 0; gbc.gridy = row;
        JLabel statusLabel = new JLabel("Vehicle Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(TEXT_GRAY);
        statusLabel.setPreferredSize(labelSize);
        statusLabel.setMinimumSize(labelSize);
        contentPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        vehicleStatusField = new JTextField(assignedVehicle != null ? assignedVehicle.status : "No Vehicle Assigned");
        styleTextField(vehicleStatusField, fieldSize);
        vehicleStatusField.setEditable(false);
        vehicleStatusField.setBackground(new Color(240, 240, 240));
        
        if (assignedVehicle != null) {
            if (assignedVehicle.status.equalsIgnoreCase("Maintenance")) {
                vehicleStatusField.setForeground(WARNING_ORANGE);
                vehicleStatusField.setText("Under Maintenance");
            } else if (assignedVehicle.status.equalsIgnoreCase("Active")) {
                vehicleStatusField.setForeground(PRIMARY_GREEN);
                vehicleStatusField.setText("Active");
            } else if (assignedVehicle.status.equalsIgnoreCase("Inactive")) {
                vehicleStatusField.setForeground(Color.RED);
                vehicleStatusField.setText("Inactive");
            }
        }
        contentPanel.add(vehicleStatusField, gbc);
        
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
            // Silent fail
        }
    }
    
    private void uploadProfilePhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Photo");
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            profilePhotoFile = fileChooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(profilePhotoFile.getPath());
            Image image = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            profilePhotoLabel.setIcon(new ImageIcon(image));
            profilePhotoLabel.setText("");
        }
    }
    
    private void saveProfileChanges() {
        if (profilePhotoFile != null) {
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
                driverStorage.updateDriver(currentDriver);
            } catch (Exception e) {
                // Silent fail
            }
        }
        
        VehicleData assignedVehicle = getAssignedVehicle();
        
        String message = "Profile Information\n\n" +
            "Name: " + currentDriver.name + "\n" +
            "Staff ID: " + currentDriver.id + "\n" +
            "Email: " + currentDriver.email + "\n" +
            "Phone: " + currentDriver.phone;
        
        if (profilePhotoFile != null) {
            message += "\n\nProfile photo updated successfully!";
        }
        
        if (assignedVehicle != null) {
            message += "\n\nAssigned Vehicle:\n" +
                "ID: " + assignedVehicle.vehicleId + "\n" +
                "Model: " + assignedVehicle.vehicleModel + "\n" +
                "Type: " + assignedVehicle.vehicleType + "\n" +
                "License Plate: " + assignedVehicle.licensePlate + "\n" +
                "Fuel Type: " + assignedVehicle.fuelType + "\n" +
                "Road Tax Expiry: " + assignedVehicle.getFormattedExpiryDate() + "\n" +
                "Status: " + assignedVehicle.status;
            if (assignedVehicle.isExpired()) {
                message += "\n ROAD TAX EXPIRED! Please renew immediately.";
            }
        } else {
            message += "\n\n No vehicle assigned yet.";
        }
        
        JOptionPane.showMessageDialog(this,
            message,
            "Profile Information",
            JOptionPane.INFORMATION_MESSAGE);
        
        profilePhotoFile = null;
    }
    
    public void refreshProfile(Driver updatedDriver) {
        this.currentDriver = updatedDriver;
        loadVehicleData();
        VehicleData assignedVehicle = getAssignedVehicle();
        
        nameField.setText(currentDriver.name);
        emailField.setText(currentDriver.email);
        phoneField.setText(currentDriver.phone);
        
        if (assignedVehicle != null) {
            vehicleIdField.setText(assignedVehicle.vehicleId);
            vehicleModelField.setText(assignedVehicle.vehicleModel);
            plateNoField.setText(assignedVehicle.licensePlate);
            vehicleTypeField.setText(assignedVehicle.vehicleType);
            fuelTypeField.setText(assignedVehicle.fuelType);
            
            String expiryText = assignedVehicle.getFormattedExpiryDate();
            roadTaxExpiryField.setText(expiryText);
            if (assignedVehicle.isExpired()) {
                roadTaxExpiryField.setForeground(Color.RED);
                roadTaxExpiryField.setText("EXPIRED - " + expiryText);
            } else {
                roadTaxExpiryField.setForeground(PRIMARY_GREEN);
            }
            
            if (assignedVehicle.status.equalsIgnoreCase("Maintenance")) {
                vehicleStatusField.setForeground(WARNING_ORANGE);
                vehicleStatusField.setText("Under Maintenance");
            } else if (assignedVehicle.status.equalsIgnoreCase("Active")) {
                vehicleStatusField.setForeground(PRIMARY_GREEN);
                vehicleStatusField.setText("Active");
            } else if (assignedVehicle.status.equalsIgnoreCase("Inactive")) {
                vehicleStatusField.setForeground(Color.RED);
                vehicleStatusField.setText("Inactive");
            }
        }
        
        revalidate();
        repaint();
    }
    
    public Driver getUpdatedDriver() {
        return currentDriver;
    }
}