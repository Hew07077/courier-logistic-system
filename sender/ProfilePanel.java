// ProfilePanel.java
package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private SenderDashboard dashboard;

    public ProfilePanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createProfileContent(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createProfileContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // ========== PERSONAL INFORMATION SECTION ==========
        JPanel personalSection = new JPanel(new BorderLayout());
        personalSection.setBackground(Color.WHITE);
        personalSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel sectionTitle = new JLabel("PERSONAL INFORMATION");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(0, 123, 255));
        sectionTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        personalSection.add(sectionTitle, BorderLayout.NORTH);
        
        // Create info panel with GridBagLayout for proper alignment
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.insets = new Insets(10, 0, 10, 20);
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(new Color(85, 85, 85));
        infoPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        JLabel nameValue = new JLabel(dashboard.getSenderName() != null && !dashboard.getSenderName().isEmpty() ? dashboard.getSenderName() : "—");
        nameValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameValue.setForeground(new Color(51, 51, 51));
        infoPanel.add(nameValue, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailLabel.setForeground(new Color(85, 85, 85));
        infoPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        JLabel emailValue = new JLabel(dashboard.getSenderEmail() != null && !dashboard.getSenderEmail().isEmpty() ? dashboard.getSenderEmail() : "—");
        emailValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailValue.setForeground(new Color(51, 51, 51));
        infoPanel.add(emailValue, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        phoneLabel.setForeground(new Color(85, 85, 85));
        infoPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        JLabel phoneValue = new JLabel(dashboard.getSenderPhone() != null && !dashboard.getSenderPhone().isEmpty() ? dashboard.getSenderPhone() : "—");
        phoneValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneValue.setForeground(new Color(51, 51, 51));
        infoPanel.add(phoneValue, gbc);
        
        personalSection.add(infoPanel, BorderLayout.CENTER);
        contentPanel.add(personalSection);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(new Color(248, 249, 250));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        int totalOrders = dashboard.getDeliveredOrders() + dashboard.getActiveOrders();
        statsPanel.add(createStatBox("Total Orders", String.valueOf(totalOrders)));
        statsPanel.add(createStatBox("Delivered", String.valueOf(dashboard.getDeliveredOrders())));
        statsPanel.add(createStatBox("Total Spent", "RM " + String.format("%.2f", dashboard.getTotalSpent())));
        
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton editBtn = new JButton("Edit Profile");
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editBtn.setForeground(Color.WHITE);
        editBtn.setBackground(new Color(0, 123, 255));
        editBtn.setBorderPainted(false);
        editBtn.setFocusPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> editProfile());

        JButton changePwdBtn = new JButton("Change Password");
        changePwdBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        changePwdBtn.setForeground(Color.WHITE);
        changePwdBtn.setBackground(new Color(23, 162, 184));
        changePwdBtn.setBorderPainted(false);
        changePwdBtn.setFocusPainted(false);
        changePwdBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePwdBtn.addActionListener(e -> changePassword());

        buttonPanel.add(editBtn);
        buttonPanel.add(changePwdBtn);
        
        contentPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private JPanel createStatBox(String label, String value) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));
        panel.setOpaque(false);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(0, 123, 255));
        panel.add(valueLabel);

        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(new Color(108, 117, 125));
        panel.add(labelLabel);

        return panel;
    }

    private void editProfile() {
        String userEmail = dashboard.getSenderEmail();
        if (userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
            JOptionPane.showMessageDialog(this, 
                "Demo users cannot edit profile. Please create a real account to edit your profile.", 
                "Demo Account Restriction", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Profile", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Edit Personal Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 123, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Full Name field
        JPanel namePanel = new JPanel(new BorderLayout(10, 0));
        namePanel.setBackground(Color.WHITE);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setPreferredSize(new Dimension(80, 25));
        JTextField nameField = new JTextField(dashboard.getSenderName());
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Email field
        JPanel emailPanel = new JPanel(new BorderLayout(10, 0));
        emailPanel.setBackground(Color.WHITE);
        emailPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailLabel.setPreferredSize(new Dimension(80, 25));
        JTextField emailField = new JTextField(dashboard.getSenderEmail());
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailPanel.add(emailLabel, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);
        panel.add(emailPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Phone field
        JPanel phonePanel = new JPanel(new BorderLayout(10, 0));
        phonePanel.setBackground(Color.WHITE);
        phonePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        phoneLabel.setPreferredSize(new Dimension(80, 25));
        JTextField phoneField = new JTextField(dashboard.getSenderPhone());
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phonePanel.add(phoneLabel, BorderLayout.WEST);
        phonePanel.add(phoneField, BorderLayout.CENTER);
        panel.add(phonePanel);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();
            
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Full name cannot be empty!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Email cannot be empty!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!isValidEmail(newEmail)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a valid email address!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            dashboard.setSenderName(newName);
            dashboard.setSenderEmail(newEmail);
            dashboard.setSenderPhone(newPhone);
            
            dialog.dispose();
            JOptionPane.showMessageDialog(this, 
                "Profile updated successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            removeAll();
            initialize();
            revalidate();
            repaint();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void changePassword() {
        String userEmail = dashboard.getSenderEmail();
        if (userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
            JOptionPane.showMessageDialog(this, 
                "Demo users cannot change password. Please create a real account to change your password.", 
                "Demo Account Restriction", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Change Password", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 123, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Current Password
        JPanel currentPanel = new JPanel(new BorderLayout(10, 0));
        currentPanel.setBackground(Color.WHITE);
        currentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel currentLabel = new JLabel("Current Password:");
        currentLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currentLabel.setPreferredSize(new Dimension(120, 25));
        JPasswordField currentField = new JPasswordField();
        currentField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        currentPanel.add(currentLabel, BorderLayout.WEST);
        currentPanel.add(currentField, BorderLayout.CENTER);
        panel.add(currentPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // New Password
        JPanel newPanel = new JPanel(new BorderLayout(10, 0));
        newPanel.setBackground(Color.WHITE);
        newPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        newLabel.setPreferredSize(new Dimension(120, 25));
        JPasswordField newField = new JPasswordField();
        newField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newPanel.add(newLabel, BorderLayout.WEST);
        newPanel.add(newField, BorderLayout.CENTER);
        panel.add(newPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Confirm Password
        JPanel confirmPanel = new JPanel(new BorderLayout(10, 0));
        confirmPanel.setBackground(Color.WHITE);
        confirmPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmLabel.setPreferredSize(new Dimension(120, 25));
        JPasswordField confirmField = new JPasswordField();
        confirmField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmPanel.add(confirmLabel, BorderLayout.WEST);
        confirmPanel.add(confirmField, BorderLayout.CENTER);
        panel.add(confirmPanel);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton updateBtn = new JButton("Update Password");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(new Color(0, 123, 255));
        updateBtn.setBorderPainted(false);
        updateBtn.setFocusPainted(false);
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        updateBtn.addActionListener(e -> {
            String currentPass = new String(currentField.getPassword());
            String newPass = new String(newField.getPassword());
            String confirmPass = new String(confirmField.getPassword());
            
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a new password.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPass.length() < 8) {
                JOptionPane.showMessageDialog(dialog, 
                    "Password must be at least 8 characters long!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, 
                    "New passwords do not match!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JOptionPane.showMessageDialog(dialog, 
                "Password updated successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(updateBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
}