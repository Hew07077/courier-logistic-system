// SupportPanel.java
package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.regex.Pattern;

public class SupportPanel extends JPanel {
    private SenderDashboard dashboard;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField subjectField;
    private JTextArea messageArea;

    public SupportPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Support Center");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(new Color(250, 250, 250));

        contentPanel.add(createFAQPanel());
        contentPanel.add(createContactPanel());

        return contentPanel;
    }

    private JPanel createFAQPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Frequently Asked Questions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JPanel faqList = new JPanel();
        faqList.setLayout(new BoxLayout(faqList, BoxLayout.Y_AXIS));
        faqList.setOpaque(false);
        faqList.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        addFAQItem(faqList, "How do I track my order?", 
            "You can track your order using the tracking number provided in the confirmation email.");
        addFAQItem(faqList, "What are the delivery times?", 
            "Standard delivery takes 3-5 business days. Express delivery takes 1-2 business days.");
        addFAQItem(faqList, "How do I cancel an order?", 
            "Orders can be cancelled within 1 hour of placing them from the My Orders section.");
        addFAQItem(faqList, "What payment methods are accepted?", 
            "We accept all major credit cards, PayPal, and bank transfers.");
        addFAQItem(faqList, "How do I file a claim?", 
            "For damaged or lost items, please contact support within 48 hours of delivery.");

        JScrollPane scrollPane = new JScrollPane(faqList);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addFAQItem(JPanel panel, String question, String answer) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel questionLabel = new JLabel("• " + question);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        questionLabel.setForeground(new Color(0, 123, 255));
        itemPanel.add(questionLabel, BorderLayout.NORTH);

        JLabel answerLabel = new JLabel("<html><div style='width: 300px; padding: 5px 0 5px 15px;'>" + answer + "</div></html>");
        answerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        answerLabel.setForeground(new Color(108, 117, 125));
        itemPanel.add(answerLabel, BorderLayout.CENTER);

        panel.add(itemPanel);
        panel.add(Box.createVerticalStrut(5));
    }

    private JPanel createContactPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Contact Support");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;

        // Name field
        JLabel nameLabel = new JLabel("Your Name:*");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);

        nameField = new JTextField(dashboard.getSenderName());
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1;
        formPanel.add(nameField, gbc);

        // Email field
        JLabel emailLabel = new JLabel("Email:*");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 2;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField(dashboard.getSenderEmail());
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 3;
        formPanel.add(emailField, gbc);

        // Subject field
        JLabel subjectLabel = new JLabel("Subject:*");
        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 4;
        formPanel.add(subjectLabel, gbc);

        subjectField = new JTextField();
        subjectField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 5;
        formPanel.add(subjectField, gbc);

        // Message field
        JLabel messageLabel = new JLabel("Message:*");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 6;
        formPanel.add(messageLabel, gbc);

        messageArea = new JTextArea(5, 20);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        gbc.gridy = 7;
        formPanel.add(messageScroll, gbc);

        // Send button
        JButton sendBtn = new JButton("Send Message");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setBackground(new Color(0, 123, 255));
        sendBtn.setBorderPainted(false);
        sendBtn.setFocusPainted(false);
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> validateAndSend());

        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(sendBtn, gbc);

        // Add note about required fields
        JLabel noteLabel = new JLabel("* Required fields");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        noteLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 9;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(noteLabel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void validateAndSend() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String subject = subjectField.getText().trim();
        String message = messageArea.getText().trim();

        // Validate Name
        if (name.isEmpty()) {
            showErrorDialog("Name cannot be empty. Please enter your name.");
            nameField.requestFocus();
            return;
        }

        if (name.length() < 2) {
            showErrorDialog("Name must be at least 2 characters long.");
            nameField.requestFocus();
            return;
        }

        if (name.length() > 100) {
            showErrorDialog("Name cannot exceed 100 characters.");
            nameField.requestFocus();
            return;
        }

        // Validate Email
        if (email.isEmpty()) {
            showErrorDialog("Email cannot be empty. Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showErrorDialog("Please enter a valid email address (e.g., name@example.com).");
            emailField.requestFocus();
            return;
        }

        // Validate Subject
        if (subject.isEmpty()) {
            showErrorDialog("Subject cannot be empty. Please enter a subject.");
            subjectField.requestFocus();
            return;
        }

        if (subject.length() < 3) {
            showErrorDialog("Subject must be at least 3 characters long.");
            subjectField.requestFocus();
            return;
        }

        if (subject.length() > 200) {
            showErrorDialog("Subject cannot exceed 200 characters.");
            subjectField.requestFocus();
            return;
        }

        // Validate Message
        if (message.isEmpty()) {
            showErrorDialog("Message cannot be empty. Please enter your message.");
            messageArea.requestFocus();
            return;
        }

        if (message.length() < 10) {
            showErrorDialog("Message must be at least 10 characters long.");
            messageArea.requestFocus();
            return;
        }

        if (message.length() > 5000) {
            showErrorDialog("Message cannot exceed 5000 characters.");
            messageArea.requestFocus();
            return;
        }

        // Check for potentially harmful content (basic)
        if (containsHarmfulContent(message) || containsHarmfulContent(subject)) {
            showErrorDialog("Your message contains inappropriate content. Please review and try again.");
            return;
        }

        // All validations passed
        JOptionPane.showMessageDialog(this, 
            "Your message has been sent successfully!\n\n" +
            "We'll respond within 24 hours to: " + email + "\n\n" +
            "Reference: Support request from " + name,
            "Message Sent", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear form
        subjectField.setText("");
        messageArea.setText("");
        
        // Optional: Keep name and email fields populated
        // nameField.setText(name);  // Uncomment if you want to keep
        // emailField.setText(email); // Uncomment if you want to keep
    }

    private boolean isValidEmail(String email) {
        // RFC 5322 compliant email regex pattern
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Additional checks
        if (email.length() > 254) {
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        if (parts[0].length() > 64) {
            return false;
        }
        
        if (parts[1].length() > 255) {
            return false;
        }
        
        return pattern.matcher(email).matches();
    }

    private boolean containsHarmfulContent(String text) {
        String lowerText = text.toLowerCase();
        
        // List of harmful/inappropriate keywords (customize as needed)
        String[] harmfulKeywords = {
            "spam", "hack", "virus", "malware", "phishing",
            "scam", "fraud", "abuse", "threat", "harass"
        };
        
        for (String keyword : harmfulKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        
        // Check for excessive URLs (potential spam)
        int urlCount = lowerText.split("http").length - 1;
        if (urlCount > 3) {
            return true;
        }
        
        return false;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, 
            message,
            "Validation Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}