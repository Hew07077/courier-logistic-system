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
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Profile Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createProfileContent() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(avatarLabel, gbc);

        addProfileField(contentPanel, "Full Name:", dashboard.getSenderName(), gbc, 1);
        addProfileField(contentPanel, "Email:", dashboard.getSenderEmail(), gbc, 2);
        addProfileField(contentPanel, "Phone:", dashboard.getSenderPhone(), gbc, 3);
        addProfileField(contentPanel, "Address:", dashboard.getSenderAddress(), gbc, 4);
        
        String userEmail = dashboard.getSenderEmail();
        if (userEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(userEmail)) {
            addProfileField(contentPanel, "Company:", "Demo Company", gbc, 5);
            addProfileField(contentPanel, "Account Type:", "Demo Account", gbc, 6);
        } else {
            addProfileField(contentPanel, "Company:", "ABC Corporation", gbc, 5);
            addProfileField(contentPanel, "Account Type:", "Premium Sender", gbc, 6);
        }
        
        addProfileField(contentPanel, "Member Since:", "January 15, 2024", gbc, 7);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(new Color(248, 249, 250));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        statsPanel.add(createStatBox("Total Orders", 
            String.valueOf(dashboard.getDeliveredOrders() + dashboard.getActiveOrders())));
        statsPanel.add(createStatBox("Delivered", 
            String.valueOf(dashboard.getDeliveredOrders())));
        statsPanel.add(createStatBox("Total Spent", 
            "RM " + String.format("%.2f", dashboard.getTotalSpent())));

        gbc.gridy = 8;
        gbc.gridwidth = 2;
        contentPanel.add(statsPanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
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

        gbc.gridy = 9;
        contentPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private void addProfileField(JPanel panel, String label, String value, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(valueComp, gbc);
    }

    private JPanel createStatBox(String label, String value) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setOpaque(false);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
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
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        JTextField nameField = addEditField(panel, "Full Name:", dashboard.getSenderName(), gbc, 0);
        JTextField emailField = addEditField(panel, "Email:", dashboard.getSenderEmail(), gbc, 1);
        JTextField phoneField = addEditField(panel, "Phone:", dashboard.getSenderPhone(), gbc, 2);
        
        JTextArea addressArea = new JTextArea(dashboard.getSenderAddress(), 3, 20);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        gbc.gridy = 3;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(addressLabel, gbc);
        gbc.gridx = 1;
        panel.add(addressScroll, gbc);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            dashboard.setSenderName(nameField.getText());
            dashboard.setSenderEmail(emailField.getText());
            dashboard.setSenderPhone(phoneField.getText());
            dashboard.setSenderAddress(addressArea.getText());
            
            dialog.dispose();
            JOptionPane.showMessageDialog(this, 
                "Profile updated successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            removeAll();
            initialize();
            revalidate();
            repaint();
        });

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(saveBtn, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private JTextField addEditField(JPanel panel, String label, String value, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        JTextField field = new JTextField(value, 20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
        
        return field;
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

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        JPasswordField currentField = addPasswordField(panel, "Current Password:", gbc, 0);
        JPasswordField newField = addPasswordField(panel, "New Password:", gbc, 1);
        JPasswordField confirmField = addPasswordField(panel, "Confirm Password:", gbc, 2);

        JButton saveBtn = new JButton("Update Password");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(0, 123, 255));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            String newPass = new String(newField.getPassword());
            String confirmPass = new String(confirmField.getPassword());
            
            if (newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Password updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "New passwords do not match!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridy = 3;
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPasswordField addPasswordField(JPanel panel, String label, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
        
        return field;
    }
}