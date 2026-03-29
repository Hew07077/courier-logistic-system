package courier;

import logistics.driver.Driver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class VehicleReport {
    
    private static final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private static final Color GREEN_DARK = new Color(27, 94, 32);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color TEXT_GRAY = new Color(108, 117, 125);
    private static final Color BG_LIGHT = new Color(245, 247, 250);
    
    // Severity colors
    private static final Color CRITICAL_COLOR = new Color(220, 53, 69);
    private static final Color HIGH_COLOR = new Color(255, 140, 0);
    private static final Color MEDIUM_COLOR = new Color(255, 193, 7);
    private static final Color LOW_COLOR = new Color(40, 167, 69);
    
    private Driver currentDriver;
    private String reportsDirectory = "vehicle_reports";
    private String vehiclesDirectory = "vehicle_data";
    private static final String REPORT_FILENAME = "vehicleReports_data.txt";
    private static final String VEHICLES_FILENAME = "assigned_vehicles.txt";
    
    // Vehicle data storage
    private Map<String, VehicleInfo> assignedVehicles = new HashMap<>();
    
    public VehicleReport(Driver driver) {
        this.currentDriver = driver;
        createReportsDirectory();
        createVehiclesDirectory();
        loadAssignedVehicles();
    }
    
    private void createReportsDirectory() {
        File directory = new File(reportsDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    private void createVehiclesDirectory() {
        File directory = new File(vehiclesDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    private void loadAssignedVehicles() {
        String filePath = vehiclesDirectory + "/" + VEHICLES_FILENAME;
        File vehicleFile = new File(filePath);
        
        // If file doesn't exist, create with sample data for testing
        if (!vehicleFile.exists()) {
            createSampleVehicleData();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header if exists
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String courierId = parts[0].trim();
                    String vehicleType = parts[1].trim();
                    String licensePlate = parts[2].trim();
                    String vehicleModel = parts[3].trim();
                    String lastMileage = parts[4].trim();
                    
                    VehicleInfo vehicle = new VehicleInfo(vehicleType, licensePlate, vehicleModel, lastMileage);
                    assignedVehicles.put(courierId, vehicle);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createSampleVehicleData() {
        String filePath = vehiclesDirectory + "/" + VEHICLES_FILENAME;
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("COURIER_ID|VEHICLE_TYPE|LICENSE_PLATE|VEHICLE_MODEL|CURRENT_MILEAGE|LAST_SERVICE_DATE|STATUS");
            writer.println("DRV001|Van|WVS 1234|Toyota Hiace|45230|2024-01-15|Active");
            writer.println("DRV002|Motorcycle|MBR 5678|Yamaha LC135|12340|2024-02-01|Active");
            writer.println("DRV003|Car|WCD 9012|Proton Saga|67890|2023-12-10|Active");
            writer.println("DRV004|Truck|WLT 3456|Hino 300|120450|2024-01-20|Active");
            writer.println("DRV005|Bicycle|CYC 7890|Giant Escape|2450|2024-01-05|Active");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Method for admin to update vehicle assignment
    public static boolean assignVehicleToCourier(String courierId, String vehicleType, 
                                                   String licensePlate, String vehicleModel, 
                                                   String currentMileage) {
        String filePath = "vehicle_data/" + VEHICLES_FILENAME;
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        
        try {
            File file = new File(filePath);
            // Read existing data
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        
                        if (line.startsWith(courierId + "|")) {
                            // Update existing entry
                            lines.add(String.format("%s|%s|%s|%s|%s|%s|%s",
                                courierId, vehicleType, licensePlate, vehicleModel, 
                                currentMileage, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), "Active"));
                            updated = true;
                        } else {
                            lines.add(line);
                        }
                    }
                }
            }
            
            // Add new entry if not updated
            if (!updated) {
                if (lines.isEmpty()) {
                    lines.add("COURIER_ID|VEHICLE_TYPE|LICENSE_PLATE|VEHICLE_MODEL|CURRENT_MILEAGE|LAST_SERVICE_DATE|STATUS");
                }
                lines.add(String.format("%s|%s|%s|%s|%s|%s|%s",
                    courierId, vehicleType, licensePlate, vehicleModel, 
                    currentMileage, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), "Active"));
            }
            
            // Write back to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public JPanel createVehicleReportPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel vehicleCard = new JPanel(new BorderLayout(20, 20));
        vehicleCard.setBackground(Color.WHITE);
        vehicleCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 3, 3, new Color(0, 0, 0, 20)),
            new EmptyBorder(30, 30, 30, 30)));
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("Vehicle Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(PRIMARY_GREEN);
        headerPanel.add(headerLabel);
        
        // Info banner
        JPanel infoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoBanner.setBackground(new Color(255, 243, 224));
        infoBanner.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel infoLabel = new JLabel("⚠️ Report any vehicle issues to the maintenance team. Critical issues will be prioritized.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(255, 140, 0));
        infoBanner.add(infoLabel);
        
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(Color.WHITE);
        northPanel.add(headerPanel, BorderLayout.NORTH);
        northPanel.add(infoBanner, BorderLayout.SOUTH);
        
        // Content panel with scroll
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
        Dimension comboBoxSize = new Dimension(400, 35);
        Dimension labelSize = new Dimension(120, 30);
        
        int row = 0;
        
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
        contentPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField nameField = new JTextField(currentDriver != null ? currentDriver.name : "");
        styleTextField(nameField, fieldSize);
        nameField.setEditable(false);
        nameField.setBackground(new Color(240, 240, 240));
        contentPanel.add(nameField, gbc);
        
        // Courier ID
        gbc.gridx = 0; gbc.gridy = row;
        JLabel courierIdLabel = new JLabel("Courier ID:");
        courierIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        courierIdLabel.setForeground(TEXT_GRAY);
        courierIdLabel.setPreferredSize(labelSize);
        contentPanel.add(courierIdLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField courierIdField = new JTextField(currentDriver != null ? currentDriver.id : "");
        styleTextField(courierIdField, fieldSize);
        courierIdField.setEditable(false);
        courierIdField.setBackground(new Color(240, 240, 240));
        contentPanel.add(courierIdField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setForeground(TEXT_GRAY);
        phoneLabel.setPreferredSize(labelSize);
        contentPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField phoneField = new JTextField(currentDriver != null ? currentDriver.phone : "");
        styleTextField(phoneField, fieldSize);
        phoneField.setEditable(false);
        phoneField.setBackground(new Color(240, 240, 240));
        contentPanel.add(phoneField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = row;
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(TEXT_GRAY);
        emailLabel.setPreferredSize(labelSize);
        contentPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField emailField = new JTextField(currentDriver != null ? currentDriver.email : "");
        styleTextField(emailField, fieldSize);
        emailField.setEditable(false);
        emailField.setBackground(new Color(240, 240, 240));
        contentPanel.add(emailField, gbc);
        
        // Spacing
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        contentPanel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // ===== VEHICLE DETAILS SECTION =====
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel vehicleSectionLabel = new JLabel("VEHICLE DETAILS");
        vehicleSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vehicleSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(vehicleSectionLabel, gbc);
        gbc.gridwidth = 1;
        
        // Get assigned vehicle for current courier
        VehicleInfo assignedVehicle = assignedVehicles.get(currentDriver.id);
        
        // Vehicle Type - Fixed from file
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleTypeLabel = new JLabel("Vehicle Type:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleTypeLabel.setForeground(TEXT_GRAY);
        vehicleTypeLabel.setPreferredSize(labelSize);
        contentPanel.add(vehicleTypeLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField vehicleTypeField = new JTextField();
        styleTextField(vehicleTypeField, fieldSize);
        vehicleTypeField.setEditable(false);
        vehicleTypeField.setBackground(new Color(240, 240, 240));
        if (assignedVehicle != null) {
            vehicleTypeField.setText(assignedVehicle.vehicleType);
        } else {
            vehicleTypeField.setText("No vehicle assigned");
        }
        contentPanel.add(vehicleTypeField, gbc);
        
        // Vehicle Model
        gbc.gridx = 0; gbc.gridy = row;
        JLabel modelLabel = new JLabel("Vehicle Model:");
        modelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modelLabel.setForeground(TEXT_GRAY);
        modelLabel.setPreferredSize(labelSize);
        contentPanel.add(modelLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField modelField = new JTextField();
        styleTextField(modelField, fieldSize);
        modelField.setEditable(false);
        modelField.setBackground(new Color(240, 240, 240));
        if (assignedVehicle != null) {
            modelField.setText(assignedVehicle.vehicleModel);
        } else {
            modelField.setText("No vehicle assigned");
        }
        contentPanel.add(modelField, gbc);
        
        // License Plate - Fixed from file
        gbc.gridx = 0; gbc.gridy = row;
        JLabel plateLabel = new JLabel("License Plate:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plateLabel.setForeground(TEXT_GRAY);
        plateLabel.setPreferredSize(labelSize);
        contentPanel.add(plateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField plateField = new JTextField();
        styleTextField(plateField, fieldSize);
        plateField.setEditable(false);
        plateField.setBackground(new Color(240, 240, 240));
        if (assignedVehicle != null) {
            plateField.setText(assignedVehicle.licensePlate);
        } else {
            plateField.setText("No vehicle assigned");
        }
        contentPanel.add(plateField, gbc);
        
        // Current Mileage - From file but editable
        gbc.gridx = 0; gbc.gridy = row;
        JLabel mileageLabel = new JLabel("Current Mileage:");
        mileageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mileageLabel.setForeground(TEXT_GRAY);
        mileageLabel.setPreferredSize(labelSize);
        contentPanel.add(mileageLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JPanel mileagePanel = new JPanel(new BorderLayout());
        mileagePanel.setBackground(Color.WHITE);
        mileagePanel.setPreferredSize(fieldSize);
        
        JTextField mileageField = new JTextField();
        styleTextField(mileageField, fieldSize);
        mileageField.setEditable(true);
        mileageField.setBackground(Color.WHITE);
        mileageField.setHorizontalAlignment(JTextField.RIGHT);
        mileageField.setToolTipText("Enter current mileage (numbers only)");
        if (assignedVehicle != null) {
            mileageField.setText(assignedVehicle.currentMileage);
        } else {
            mileageField.setText("0");
        }
        
        JLabel kmLabel = new JLabel(" km");
        kmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        kmLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
        
        mileagePanel.add(mileageField, BorderLayout.CENTER);
        mileagePanel.add(kmLabel, BorderLayout.EAST);
        contentPanel.add(mileagePanel, gbc);
        
        // Spacing
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        contentPanel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // ===== REPORT ISSUE SECTION =====
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel reportSectionLabel = new JLabel("REPORT VEHICLE ISSUE");
        reportSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reportSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(reportSectionLabel, gbc);
        gbc.gridwidth = 1;
        
        // Issue Type
        gbc.gridx = 0; gbc.gridy = row;
        JLabel issueLabel = new JLabel("Issue Type:");
        issueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        issueLabel.setForeground(TEXT_GRAY);
        issueLabel.setPreferredSize(labelSize);
        contentPanel.add(issueLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JComboBox<String> issueCombo = new JComboBox<>(new String[]{
            "Select Issue Type...",
            "No Issues - Routine Check",
            "Engine - Starting Problem",
            "Engine - Strange Noise",
            "Engine - Loss of Power",
            "Engine - Overheating",
            "Engine - Check Engine Light",
            "Brakes - Squeaking/Grinding",
            "Brakes - Soft/Vibrating Pedal",
            "Transmission - Hard Shifting",
            "Transmission - Slipping",
            "Electrical - Battery/Charging",
            "Electrical - Lights",
            "Electrical - Dashboard Warning",
            "Tires - Low Pressure",
            "Tires - Damage/Wear",
            "Tires - Flat/Burst",
            "Steering - Difficulty/Noise",
            "Steering - Vehicle Pulling",
            "Suspension - Bumpy Ride",
            "Suspension - Noise",
            "AC/Heating - Not Working",
            "AC/Heating - Strange Smell",
            "Fuel System - Leak",
            "Fuel System - Poor Economy",
            "Exhaust - Noise/Smoke",
            "Body - Damage",
            "Body - Door Issues",
            "Wipers/Washer - Not Working",
            "Other - Please Specify"
        });
        styleComboBox(issueCombo, comboBoxSize);
        contentPanel.add(issueCombo, gbc);
        
        // Priority Level
        gbc.gridx = 0; gbc.gridy = row;
        JLabel priorityLabel = new JLabel("Priority Level:");
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priorityLabel.setForeground(TEXT_GRAY);
        priorityLabel.setPreferredSize(labelSize);
        contentPanel.add(priorityLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{
            "Select Priority...",
            "Critical - Cannot continue driving - UNSAFE",
            "High - Need immediate attention today",
            "Medium - Should be checked within 3 days",
            "Low - Can continue driving, check when available"
        });
        styleComboBox(priorityCombo, comboBoxSize);
        priorityCombo.setRenderer(new PriorityListCellRenderer());
        contentPanel.add(priorityCombo, gbc);
        
        // Current Location
        gbc.gridx = 0; gbc.gridy = row;
        JLabel locationLabel = new JLabel("Current Location:");
        locationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        locationLabel.setForeground(TEXT_GRAY);
        locationLabel.setPreferredSize(labelSize);
        contentPanel.add(locationLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JComboBox<String> locationCombo = new JComboBox<>(new String[]{
            "Select Location...",
            "Kuala Lumpur - City Center",
            "Kuala Lumpur - North",
            "Kuala Lumpur - South",
            "Selangor - Petaling Jaya",
            "Selangor - Shah Alam",
            "Selangor - Klang",
            "Selangor - Subang Jaya",
            "Penang - George Town",
            "Penang - Butterworth",
            "Johor - Johor Bahru",
            "Johor - Skudai",
            "Perak - Ipoh",
            "Negeri Sembilan - Seremban",
            "Melaka - Melaka City",
            "Pahang - Kuantan",
            "Terengganu - Kuala Terengganu",
            "Kelantan - Kota Bharu",
            "Kedah - Alor Setar",
            "Perlis - Kangar",
            "Sabah - Kota Kinabalu",
            "Sarawak - Kuching",
            "Sarawak - Miri",
            "Other - Highway/Roadside",
            "Other - Customer Location",
            "Other - Warehouse/Depot"
        });
        styleComboBox(locationCombo, comboBoxSize);
        contentPanel.add(locationCombo, gbc);
        
        // Detailed Description
        gbc.gridx = 0; gbc.gridy = row;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setPreferredSize(labelSize);
        contentPanel.add(descLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        JTextArea descArea = new JTextArea(5, 30);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 10, 10, 10)));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);
        descScroll.setPreferredSize(new Dimension(400, 100));
        contentPanel.add(descScroll, gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        
        // Severity Guide Panel
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JPanel severityGuidePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        severityGuidePanel.setBackground(Color.WHITE);
        severityGuidePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), 
            "Priority Guide",
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 11), 
            TEXT_GRAY));
        
        severityGuidePanel.add(createSeverityIndicator("Critical", CRITICAL_COLOR));
        severityGuidePanel.add(createSeverityIndicator("High", HIGH_COLOR));
        severityGuidePanel.add(createSeverityIndicator("Medium", MEDIUM_COLOR));
        severityGuidePanel.add(createSeverityIndicator("Low", LOW_COLOR));
        
        contentPanel.add(severityGuidePanel, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = createCancelButton(issueCombo, priorityCombo, locationCombo, descArea);
        JButton submitBtn = createSubmitButton(nameField, courierIdField, phoneField, emailField,
                                               vehicleTypeField, plateField, mileageField,
                                               issueCombo, priorityCombo, locationCombo, descArea);
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(submitBtn);
        
        // Assemble
        vehicleCard.add(northPanel, BorderLayout.NORTH);
        vehicleCard.add(scrollPane, BorderLayout.CENTER);
        vehicleCard.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(vehicleCard, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createSeverityIndicator(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel colorBox = new JLabel("⬤");
        colorBox.setForeground(color);
        colorBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textLabel.setForeground(TEXT_GRAY);
        
        panel.add(colorBox);
        panel.add(textLabel);
        
        return panel;
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
    
    private void styleComboBox(JComboBox<String> comboBox, Dimension size) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        comboBox.setBackground(Color.WHITE);
        comboBox.setPreferredSize(size);
        comboBox.setMinimumSize(size);
        comboBox.setMaximumSize(size);
    }
    
    private JButton createCancelButton(JComboBox<String> issueCombo,
                                       JComboBox<String> priorityCombo, 
                                       JComboBox<String> locationCombo,
                                       JTextArea descArea) {
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setForeground(TEXT_GRAY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 25, 10, 25)));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        
        cancelBtn.addActionListener(e -> {
            issueCombo.setSelectedIndex(0);
            priorityCombo.setSelectedIndex(0);
            locationCombo.setSelectedIndex(0);
            descArea.setText("");
        });
        
        return cancelBtn;
    }
    
    private JButton createSubmitButton(JTextField nameField, JTextField courierIdField,
                                       JTextField phoneField, JTextField emailField,
                                       JTextField vehicleTypeField, JTextField plateField,
                                       JTextField mileageField, JComboBox<String> issueCombo,
                                       JComboBox<String> priorityCombo, JComboBox<String> locationCombo,
                                       JTextArea descArea) {
        JButton submitBtn = new JButton("Submit Report");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(PRIMARY_GREEN);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setFocusPainted(false);
        submitBtn.setContentAreaFilled(true);
        submitBtn.setOpaque(true);
        submitBtn.setBorderPainted(false);
        submitBtn.setPreferredSize(new Dimension(150, 40));
        
        submitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submitBtn.setBackground(GREEN_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                submitBtn.setBackground(PRIMARY_GREEN);
            }
        });
        
        submitBtn.addActionListener(e -> {
            if (!validateInputs(vehicleTypeField, plateField, mileageField,
                                issueCombo, priorityCombo, locationCombo)) {
                return;
            }
            
            boolean saved = saveReportToFile(
                nameField.getText().trim(),
                courierIdField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                vehicleTypeField.getText().trim(),
                plateField.getText().trim().toUpperCase(),
                mileageField.getText().trim().replace(",", ""),
                (String) priorityCombo.getSelectedItem(),
                (String) locationCombo.getSelectedItem(),
                (String) issueCombo.getSelectedItem(),
                descArea.getText().trim()
            );
            
            if (saved) {
                showSuccessMessage("Report submitted successfully!\n\nThe maintenance team has been notified.", "Success");
                
                issueCombo.setSelectedIndex(0);
                priorityCombo.setSelectedIndex(0);
                locationCombo.setSelectedIndex(0);
                descArea.setText("");
            } else {
                showStyledMessage("Failed to save report. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return submitBtn;
    }
    
    private boolean validateInputs(JTextField vehicleTypeField, JTextField plateField,
                                   JTextField mileageField, JComboBox<String> issueCombo,
                                   JComboBox<String> priorityCombo, JComboBox<String> locationCombo) {
        
        if (vehicleTypeField.getText().trim().isEmpty() || 
            vehicleTypeField.getText().trim().equals("No vehicle assigned")) {
            showStyledMessage("No vehicle assigned to you. Please contact administrator.", 
                "Vehicle Assignment Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (plateField.getText().trim().isEmpty() || 
            plateField.getText().trim().equals("No vehicle assigned")) {
            showStyledMessage("No vehicle assigned to you. Please contact administrator.", 
                "Vehicle Assignment Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String mileage = mileageField.getText().trim();
        if (mileage.isEmpty()) {
            showStyledMessage("Please enter current mileage.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            String mileageNumeric = mileage.replace(",", "");
            Double.parseDouble(mileageNumeric);
        } catch (NumberFormatException ex) {
            showStyledMessage("Please enter a valid number for mileage.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (issueCombo.getSelectedIndex() == 0) {
            showStyledMessage("Please select an issue type.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (priorityCombo.getSelectedIndex() == 0) {
            showStyledMessage("Please select priority level.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (locationCombo.getSelectedIndex() == 0) {
            showStyledMessage("Please select current location.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private boolean saveReportToFile(String name, String courierId, String phone, String email,
                                      String vehicleType, String plateNo, String mileage,
                                      String priority, String location, String issue,
                                      String description) {
        try {
            String filePath = reportsDirectory + "/" + REPORT_FILENAME;
            File reportFile = new File(filePath);
            
            // Ensure directory exists
            createReportsDirectory();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile, true))) {
                // Write header if file is new
                if (!reportFile.exists() || reportFile.length() == 0) {
                    writer.println("======================================================================================================================================================================================================");
                    writer.println("DATE AND TIME           | COURIER ID | COURIER NAME    | PHONE        | EMAIL                    | VEHICLE TYPE | PLATE NO  | MILEAGE (km) | ISSUE TYPE                 | PRIORITY | LOCATION                    | DESCRIPTION");
                    writer.println("======================================================================================================================================================================================================");
                }
                
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                
                writer.printf("%-23s | %-10s | %-15s | %-12s | %-24s | %-12s | %-9s | %-12s | %-26s | %-8s | %-27s | %s%n",
                    timestamp,
                    courierId,
                    truncate(name, 15),
                    truncate(phone, 12),
                    truncate(email, 24),
                    truncate(vehicleType, 12),
                    truncate(plateNo, 9),
                    mileage,
                    truncate(issue, 26),
                    getPriorityShort(priority),
                    truncate(location, 27),
                    truncate(description.isEmpty() ? "No description provided" : description, 50)
                );
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String getPriorityShort(String priority) {
        if (priority.contains("Critical")) return "Critical";
        if (priority.contains("High")) return "High";
        if (priority.contains("Medium")) return "Medium";
        if (priority.contains("Low")) return "Low";
        return priority;
    }
    
    private String truncate(String text, int length) {
        if (text == null || text.isEmpty()) return "";
        if (text.length() <= length) return text;
        return text.substring(0, length - 3) + "...";
    }
    
    private void showSuccessMessage(String message, String title) {
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog(null, title);
        dialog.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        for (Component comp : optionPane.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component btn : panel.getComponents()) {
                    if (btn instanceof JButton) {
                        JButton button = (JButton) btn;
                        button.setText("OK");
                        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        button.setBackground(PRIMARY_GREEN);
                        button.setForeground(Color.WHITE);
                        button.setFocusPainted(false);
                        button.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
                        
                        button.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                button.setBackground(GREEN_DARK);
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                button.setBackground(PRIMARY_GREEN);
                            }
                        });
                    }
                }
            }
        }
        dialog.setVisible(true);
    }
    
    private void showStyledMessage(String message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        JDialog dialog = optionPane.createDialog(null, title);
        dialog.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        for (Component comp : optionPane.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component btn : panel.getComponents()) {
                    if (btn instanceof JButton) {
                        JButton button = (JButton) btn;
                        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        button.setBackground(PRIMARY_GREEN);
                        button.setForeground(Color.WHITE);
                        button.setFocusPainted(false);
                        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
                    }
                }
            }
        }
        dialog.setVisible(true);
    }
    
    // Inner class to store vehicle information
    private class VehicleInfo {
        String vehicleType;
        String licensePlate;
        String vehicleModel;
        String currentMileage;
        
        VehicleInfo(String vehicleType, String licensePlate, String vehicleModel, String currentMileage) {
            this.vehicleType = vehicleType;
            this.licensePlate = licensePlate;
            this.vehicleModel = vehicleModel;
            this.currentMileage = currentMileage;
        }
    }
    
    // Custom renderer for priority combo box
    private class PriorityListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                      boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel && value != null) {
                JLabel label = (JLabel) c;
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
                String text = value.toString();
                if (text.contains("Critical")) {
                    label.setForeground(CRITICAL_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if (text.contains("High")) {
                    label.setForeground(HIGH_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if (text.contains("Medium")) {
                    label.setForeground(MEDIUM_COLOR);
                } else if (text.contains("Low")) {
                    label.setForeground(LOW_COLOR);
                }
            }
            return c;
        }
    }
}