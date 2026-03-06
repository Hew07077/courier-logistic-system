package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SupportPanel extends JPanel {
    private SenderDashboard dashboard;

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

        // Name
        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(dashboard.getSenderName());
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1;
        formPanel.add(nameField, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 2;
        formPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(dashboard.getSenderEmail());
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 3;
        formPanel.add(emailField, gbc);

        // Subject
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 4;
        formPanel.add(subjectLabel, gbc);

        JTextField subjectField = new JTextField();
        subjectField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 5;
        formPanel.add(subjectField, gbc);

        // Message
        JLabel messageLabel = new JLabel("Message:");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 6;
        formPanel.add(messageLabel, gbc);

        JTextArea messageArea = new JTextArea(5, 20);
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
        sendBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Your message has been sent. We'll respond within 24 hours.",
                "Message Sent", JOptionPane.INFORMATION_MESSAGE);
            subjectField.setText("");
            messageArea.setText("");
        });

        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(sendBtn, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }
}