package courier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VehicleReport {
    
    private static final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private static final Color GREEN_DARK = new Color(27, 94, 32);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color TEXT_GRAY = new Color(108, 117, 125);
    private static final Color BG_LIGHT = new Color(245, 247, 250);
    
    private CourierData currentCourier;
    private String reportsDirectory = "vehicle_reports";
    private static final String REPORT_FILENAME = "vehicleReports_data.txt";
    
    public VehicleReport(CourierData courier) {
        this.currentCourier = courier;
        createReportsDirectory();
    }
    
    private void createReportsDirectory() {
        File directory = new File(reportsDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
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
        
        // PERSONAL INFORMATION SECTION
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
        JTextField nameField = new JTextField(currentCourier != null ? currentCourier.name : "Alex Wong");
        styleTextField(nameField, fieldSize);
        contentPanel.add(nameField, gbc);
        
        // Courier ID
        gbc.gridx = 0; gbc.gridy = row;
        JLabel courierIdLabel = new JLabel("Courier ID:");
        courierIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        courierIdLabel.setForeground(TEXT_GRAY);
        courierIdLabel.setPreferredSize(labelSize);
        contentPanel.add(courierIdLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField courierIdField = new JTextField(currentCourier != null ? currentCourier.id : "C12345");
        styleTextField(courierIdField, fieldSize);
        courierIdField.setEditable(false);
        contentPanel.add(courierIdField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setForeground(TEXT_GRAY);
        phoneLabel.setPreferredSize(labelSize);
        contentPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField phoneField = new JTextField(currentCourier != null ? currentCourier.phone : "012-3456789");
        styleTextField(phoneField, fieldSize);
        contentPanel.add(phoneField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = row;
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(TEXT_GRAY);
        emailLabel.setPreferredSize(labelSize);
        contentPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField emailField = new JTextField(currentCourier != null ? currentCourier.email : "alex.wong@logixpress.com");
        styleTextField(emailField, fieldSize);
        contentPanel.add(emailField, gbc);
        
        // Spacing
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        contentPanel.add(Box.createVerticalStrut(15), gbc);
        gbc.gridwidth = 1;
        
        // VEHICLE DETAILS SECTION
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel vehicleSectionLabel = new JLabel("VEHICLE DETAILS");
        vehicleSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vehicleSectionLabel.setForeground(PRIMARY_GREEN);
        contentPanel.add(vehicleSectionLabel, gbc);
        gbc.gridwidth = 1;
        
        // Vehicle Type
        gbc.gridx = 0; gbc.gridy = row;
        JLabel vehicleTypeLabel = new JLabel("Vehicle Type:");
        vehicleTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleTypeLabel.setForeground(TEXT_GRAY);
        vehicleTypeLabel.setPreferredSize(labelSize);
        contentPanel.add(vehicleTypeLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JComboBox<String> vehicleTypeCombo = new JComboBox<>(new String[]{
            "Select Vehicle Type...",
            "Truck", "Van", "Car", "Motorcycle", "Bicycle", "Electric Scooter"
        });
        styleComboBox(vehicleTypeCombo, comboBoxSize);
        contentPanel.add(vehicleTypeCombo, gbc);
        
        // License Plate
        gbc.gridx = 0; gbc.gridy = row;
        JLabel plateLabel = new JLabel("License Plate:");
        plateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plateLabel.setForeground(TEXT_GRAY);
        plateLabel.setPreferredSize(labelSize);
        contentPanel.add(plateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JTextField plateField = new JTextField();
        styleTextField(plateField, fieldSize);
        plateField.setToolTipText("Enter vehicle license plate number");
        contentPanel.add(plateField, gbc);
        
        // Current Mileage
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
        mileageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mileageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)));
        mileageField.setBackground(new Color(250, 250, 250));
        mileageField.setHorizontalAlignment(JTextField.RIGHT);
        mileageField.setToolTipText("Enter current mileage (numbers only)");
        
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
        
        // REPORT ISSUE SECTION
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
            "No Issues - Routine Check", "Engine Light On", "Brake Noise", 
            "Tire Pressure Low", "Battery Problem", "AC Not Working",
            "Transmission Issue", "Electrical Problem"
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
            "Low - Can continue driving, check when available",
            "Medium - Should be checked soon",
            "High - Need immediate attention",
            "Critical - Cannot continue driving"
        });
        styleComboBox(priorityCombo, comboBoxSize);
        priorityCombo.setRenderer(new PriorityListCellRenderer());
        contentPanel.add(priorityCombo, gbc);
        
        // Location
        gbc.gridx = 0; gbc.gridy = row;
        JLabel locationLabel = new JLabel("Current Location:");
        locationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        locationLabel.setForeground(TEXT_GRAY);
        locationLabel.setPreferredSize(labelSize);
        contentPanel.add(locationLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = row++;
        JComboBox<String> locationCombo = new JComboBox<>(new String[]{
            "Select Location...",
            "Kuala Lumpur - City Center", "Kuala Lumpur - North", "Kuala Lumpur - South",
            "Selangor - Petaling Jaya", "Selangor - Shah Alam", "Selangor - Klang",
            "Selangor - Subang Jaya", "Penang - George Town", "Penang - Butterworth",
            "Johor - Johor Bahru", "Johor - Skudai", "Perak - Ipoh",
            "Negeri Sembilan - Seremban", "Melaka - Melaka City", "Pahang - Kuantan",
            "Terengganu - Kuala Terengganu", "Kelantan - Kota Bharu", "Kedah - Alor Setar",
            "Perlis - Kangar", "Sabah - Kota Kinabalu", "Sarawak - Kuching",
            "Sarawak - Miri", "Other - Highway/Roadside", "Other - Customer Location",
            "Other - Warehouse/Depot"
        });
        styleComboBox(locationCombo, comboBoxSize);
        contentPanel.add(locationCombo, gbc);
        
        // Description
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
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = createCancelButton(vehicleTypeCombo, plateField, mileageField, 
                                               issueCombo, priorityCombo, locationCombo, descArea);
        JButton submitBtn = createSubmitButton(nameField, courierIdField, phoneField, emailField,
                                               vehicleTypeCombo, plateField, mileageField,
                                               issueCombo, priorityCombo, locationCombo, descArea);
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(submitBtn);
        
        // Assemble
        vehicleCard.add(headerPanel, BorderLayout.NORTH);
        vehicleCard.add(scrollPane, BorderLayout.CENTER);
        vehicleCard.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(vehicleCard, BorderLayout.CENTER);
        
        return mainPanel;
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
    
    private JButton createCancelButton(JComboBox<String> vehicleTypeCombo, JTextField plateField,
                                       JTextField mileageField, JComboBox<String> issueCombo,
                                       JComboBox<String> priorityCombo, JComboBox<String> locationCombo,
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
            vehicleTypeCombo.setSelectedIndex(0);
            plateField.setText("");
            mileageField.setText("");
            issueCombo.setSelectedIndex(0);
            priorityCombo.setSelectedIndex(0);
            locationCombo.setSelectedIndex(0);
            descArea.setText("");
        });
        
        return cancelBtn;
    }
    
    private JButton createSubmitButton(JTextField nameField, JTextField courierIdField,
                                       JTextField phoneField, JTextField emailField,
                                       JComboBox<String> vehicleTypeCombo, JTextField plateField,
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
            if (!validateInputs(nameField, courierIdField, phoneField, vehicleTypeCombo,
                                plateField, mileageField, issueCombo, priorityCombo, locationCombo)) {
                return;
            }
            
            boolean saved = saveReportToFile(
                nameField.getText().trim(),
                courierIdField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                (String) vehicleTypeCombo.getSelectedItem(),
                plateField.getText().trim(),
                mileageField.getText().trim(),
                (String) priorityCombo.getSelectedItem(),
                (String) locationCombo.getSelectedItem(),
                (String) issueCombo.getSelectedItem(),
                descArea.getText().trim()
            );
            
            if (saved) {
                showSuccessMessage("Report submitted successfully!", "Success");
                
                vehicleTypeCombo.setSelectedIndex(0);
                plateField.setText("");
                mileageField.setText("");
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
    
    private boolean validateInputs(JTextField nameField, JTextField courierIdField,
                                   JTextField phoneField, JComboBox<String> vehicleTypeCombo,
                                   JTextField plateField, JTextField mileageField,
                                   JComboBox<String> issueCombo, JComboBox<String> priorityCombo,
                                   JComboBox<String> locationCombo) {
        
        if (nameField.getText().trim().isEmpty() || 
            courierIdField.getText().trim().isEmpty() || 
            phoneField.getText().trim().isEmpty()) {
            showStyledMessage("Please fill in all personal details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (vehicleTypeCombo.getSelectedIndex() == 0) {
            showStyledMessage("Please select a vehicle type.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (plateField.getText().trim().isEmpty()) {
            showStyledMessage("Please enter the license plate number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile, true))) {
                if (!reportFile.exists() || reportFile.length() == 0) {
                    writer.println("=========================================================================================================================================================================");
                    writer.println("DATE AND TIME           | COURIER ID | COURIER NAME  | PHONE        | EMAIL                    | VEHICLE TYPE | PLATE NO  | MILEAGE (km) | ISSUE TYPE            | PRIORITY | LOCATION                    | DESCRIPTION");
                    writer.println("=========================================================================================================================================================================");
                }
                
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                
                writer.printf("%-23s | %-10s | %-13s | %-12s | %-24s | %-12s | %-9s | %-12s | %-21s | %-8s | %-27s | %s%n",
                    timestamp,
                    courierId,
                    truncate(name, 13),
                    truncate(phone, 12),
                    truncate(email, 24),
                    truncate(vehicleType, 12),
                    truncate(plateNo, 9),
                    mileage,
                    truncate(issue, 21),
                    getPriorityShort(priority),
                    truncate(location, 27),
                    truncate(description.isEmpty() ? "No description" : description, 30)
                );
            }
            return true;
        } catch (IOException e) {
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
                    label.setForeground(new Color(220, 53, 69));
                } else if (text.contains("High")) {
                    label.setForeground(new Color(255, 140, 0));
                } else if (text.contains("Medium")) {
                    label.setForeground(new Color(255, 193, 7));
                } else if (text.contains("Low")) {
                    label.setForeground(new Color(40, 167, 69));
                }
            }
            return c;
        }
    }
}