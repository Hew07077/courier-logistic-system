// NewOrderPanel.java
package sender;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class NewOrderPanel extends JPanel {
    private SenderDashboard dashboard;
    
    private JComboBox<String> fromStateCombo;
    private JComboBox<String> fromCityCombo;
    private JTextField fromPostcodeField;
    private JTextField fromAddressField;
    
    private JComboBox<String> toStateCombo;
    private JComboBox<String> toCityCombo;
    private JTextField toPostcodeField;
    private JTextField toAddressField;
    
    private JTextField recipientNameField;
    private JTextField recipientPhoneField;
    
    private JComboBox<String> packageTypeCombo;
    private JTextField weightField;
    private JTextField lengthField;
    private JTextField widthField;
    private JTextField heightField;
    private JTextArea descriptionArea;
    private JLabel estimatedCostLabel;
    private JLabel deliveryTimeLabel;
    
    // Payment components
    private JComboBox<String> paymentMethodCombo;
    private JPanel paymentDetailsPanel;
    private CardLayout paymentDetailsLayout;

    private String senderName;
    private String senderEmail;
    private String senderPhone;

    private Map<String, Map<String, String>> malaysiaData;
    
    // File to save orders
    private static final String ORDERS_FILE = "orders.txt";

    public NewOrderPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        this.senderName = dashboard.getSenderName();
        this.senderEmail = dashboard.getSenderEmail();
        this.senderPhone = dashboard.getSenderPhone();
        
        initializeMalaysiaData();
        initialize();
    }

    private void initializeMalaysiaData() {
        malaysiaData = new LinkedHashMap<>();
        
        Map<String, String> selangorCities = new LinkedHashMap<>();
        selangorCities.put("Petaling Jaya", "46100");
        selangorCities.put("Shah Alam", "40000");
        selangorCities.put("Klang", "41000");
        selangorCities.put("Subang Jaya", "47500");
        selangorCities.put("Cheras", "43200");
        selangorCities.put("Ampang", "68000");
        selangorCities.put("Kajang", "43000");
        selangorCities.put("Rawang", "48000");
        malaysiaData.put("Selangor", selangorCities);

        Map<String, String> klCities = new LinkedHashMap<>();
        klCities.put("Kuala Lumpur City", "50000");
        klCities.put("Bangsar", "59100");
        klCities.put("Bukit Bintang", "55100");
        klCities.put("Cheras KL", "56000");
        klCities.put("Setapak", "53300");
        klCities.put("Wangsa Maju", "53300");
        klCities.put("Kepong", "52100");
        malaysiaData.put("Kuala Lumpur", klCities);

        Map<String, String> penangCities = new LinkedHashMap<>();
        penangCities.put("George Town", "10000");
        penangCities.put("Bayan Lepas", "11900");
        penangCities.put("Butterworth", "12000");
        penangCities.put("Bukit Mertajam", "14000");
        penangCities.put("Nibong Tebal", "14300");
        malaysiaData.put("Penang", penangCities);

        Map<String, String> johorCities = new LinkedHashMap<>();
        johorCities.put("Johor Bahru", "80000");
        johorCities.put("Iskandar Puteri", "79100");
        johorCities.put("Pasir Gudang", "81700");
        johorCities.put("Batu Pahat", "83000");
        johorCities.put("Muar", "84000");
        johorCities.put("Kluang", "86000");
        malaysiaData.put("Johor", johorCities);

        Map<String, String> perakCities = new LinkedHashMap<>();
        perakCities.put("Ipoh", "30000");
        perakCities.put("Taiping", "34000");
        perakCities.put("Teluk Intan", "36000");
        perakCities.put("Sitiawan", "32000");
        perakCities.put("Kuala Kangsar", "33000");
        malaysiaData.put("Perak", perakCities);

        Map<String, String> nsCities = new LinkedHashMap<>();
        nsCities.put("Seremban", "70000");
        nsCities.put("Port Dickson", "71000");
        nsCities.put("Nilai", "71800");
        malaysiaData.put("Negeri Sembilan", nsCities);

        Map<String, String> melakaCities = new LinkedHashMap<>();
        melakaCities.put("Melaka City", "75000");
        melakaCities.put("Ayer Keroh", "75450");
        melakaCities.put("Alor Gajah", "78000");
        malaysiaData.put("Melaka", melakaCities);

        Map<String, String> sarawakCities = new LinkedHashMap<>();
        sarawakCities.put("Kuching", "93000");
        sarawakCities.put("Miri", "98000");
        sarawakCities.put("Sibu", "96000");
        sarawakCities.put("Bintulu", "97000");
        malaysiaData.put("Sarawak", sarawakCities);

        Map<String, String> sabahCities = new LinkedHashMap<>();
        sabahCities.put("Kota Kinabalu", "88000");
        sabahCities.put("Sandakan", "90000");
        sabahCities.put("Tawau", "91000");
        sabahCities.put("Lahad Datu", "91100");
        malaysiaData.put("Sabah", sabahCities);

        Map<String, String> kedahCities = new LinkedHashMap<>();
        kedahCities.put("Alor Setar", "05000");
        kedahCities.put("Sungai Petani", "08000");
        kedahCities.put("Kulim", "09000");
        malaysiaData.put("Kedah", kedahCities);

        Map<String, String> pahangCities = new LinkedHashMap<>();
        pahangCities.put("Kuantan", "25000");
        pahangCities.put("Bentong", "28700");
        pahangCities.put("Temerloh", "28000");
        malaysiaData.put("Pahang", pahangCities);

        Map<String, String> kelantanCities = new LinkedHashMap<>();
        kelantanCities.put("Kota Bharu", "15000");
        kelantanCities.put("Pasir Mas", "17000");
        malaysiaData.put("Kelantan", kelantanCities);

        Map<String, String> terengganuCities = new LinkedHashMap<>();
        terengganuCities.put("Kuala Terengganu", "20000");
        terengganuCities.put("Kemaman", "24000");
        malaysiaData.put("Terengganu", terengganuCities);

        Map<String, String> perlisCities = new LinkedHashMap<>();
        perlisCities.put("Kangar", "01000");
        perlisCities.put("Arau", "02600");
        malaysiaData.put("Perlis", perlisCities);

        Map<String, String> labuanCities = new LinkedHashMap<>();
        labuanCities.put("Victoria", "87000");
        malaysiaData.put("Labuan", labuanCities);
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Create New Shipment Order (Malaysia)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        // Initialize labels
        estimatedCostLabel = new JLabel("RM 0.00");
        deliveryTimeLabel = new JLabel("3-5 business days");
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        int gridy = 0;

        JPanel senderBox = createSenderInfoBox();
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(senderBox, gbc);

        JPanel recipientBox = createRecipientInfoBox();
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(recipientBox, gbc);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(230, 230, 230));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(separator, gbc);

        JLabel packageLabel = new JLabel("Package Details:");
        packageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        packageLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 10, 5);
        formPanel.add(packageLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel typeLabel = new JLabel("Package Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 1;
        formPanel.add(typeLabel, gbc);

        String[] packageTypes = {"Documents", "Electronics", "Clothing", "Fragile Items", 
                                "Books", "Food", "Other"};
        packageTypeCombo = new JComboBox<>(packageTypes);
        packageTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        packageTypeCombo.addActionListener(e -> calculateEstimate());
        gbc.gridx = 1;
        formPanel.add(packageTypeCombo, gbc);

        JLabel weightLabel = new JLabel("Weight (kg):*");
        weightLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        formPanel.add(weightLabel, gbc);

        weightField = new JTextField(10);
        weightField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        weightField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateEstimate();
            }
        });
        gbc.gridx = 1;
        formPanel.add(weightField, gbc);

        JLabel dimLabel = new JLabel("Dimensions (cm):");
        dimLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        formPanel.add(dimLabel, gbc);

        JPanel dimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dimPanel.setOpaque(false);

        lengthField = new JTextField(5);
        lengthField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lengthField.setPreferredSize(new Dimension(60, 25));
        dimPanel.add(lengthField);
        dimPanel.add(new JLabel("L"));

        widthField = new JTextField(5);
        widthField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        widthField.setPreferredSize(new Dimension(60, 25));
        dimPanel.add(widthField);
        dimPanel.add(new JLabel("W"));

        heightField = new JTextField(5);
        heightField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heightField.setPreferredSize(new Dimension(60, 25));
        dimPanel.add(heightField);
        dimPanel.add(new JLabel("H"));

        gbc.gridx = 1;
        formPanel.add(dimPanel, gbc);

        JLabel descLabel = new JLabel("Description:*");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        formPanel.add(descLabel, gbc);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        gbc.gridx = 1;
        formPanel.add(descScroll, gbc);

        // Payment Section
        JLabel paymentLabel = new JLabel("Payment Details:");
        paymentLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentLabel.setForeground(new Color(40, 167, 69));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 10, 5);
        formPanel.add(paymentLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel paymentMethodLabel = new JLabel("Payment Method:*");
        paymentMethodLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 1;
        formPanel.add(paymentMethodLabel, gbc);

        String[] paymentMethods = {"Select Payment Method", "Credit Card (Visa/Mastercard)", "Debit Card", "PayPal", "Bank Transfer", "Touch 'n Go", "GrabPay"};
        paymentMethodCombo = new JComboBox<>(paymentMethods);
        paymentMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentMethodCombo.addActionListener(e -> updatePaymentDetails());
        gbc.gridx = 1;
        formPanel.add(paymentMethodCombo, gbc);

        // Payment details panel with card layout
        paymentDetailsLayout = new CardLayout();
        paymentDetailsPanel = new JPanel(paymentDetailsLayout);
        paymentDetailsPanel.setBackground(new Color(248, 249, 250));
        paymentDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        paymentDetailsPanel.add(createEmptyPaymentPanel(), "EMPTY");
        paymentDetailsPanel.add(createCreditCardPanel(), "CREDIT_CARD");
        paymentDetailsPanel.add(createDebitCardPanel(), "DEBIT_CARD");
        paymentDetailsPanel.add(createPayPalPanel(), "PAYPAL");
        paymentDetailsPanel.add(createBankTransferPanel(), "BANK_TRANSFER");
        paymentDetailsPanel.add(createEWalletPanel("Touch 'n Go"), "TOUCH_N_GO");
        paymentDetailsPanel.add(createEWalletPanel("GrabPay"), "GRABPAY");
        
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(paymentDetailsPanel, gbc);

        // Estimate Panel
        JPanel estimatePanel = new JPanel(new GridLayout(2, 2, 10, 5));
        estimatePanel.setBackground(new Color(248, 249, 250));
        estimatePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel estCostTitle = new JLabel("Total Amount (RM):");
        estCostTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        estimatePanel.add(estCostTitle);
        estimatePanel.add(estimatedCostLabel);

        JLabel deliveryTitle = new JLabel("Estimated Delivery:");
        deliveryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        estimatePanel.add(deliveryTitle);
        estimatePanel.add(deliveryTimeLabel);

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(estimatePanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(new Color(108, 117, 125));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> clearForm());

        JButton submitBtn = new JButton("Place Order & Pay");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(new Color(40, 167, 69));
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> createOrder());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(submitBtn);

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(buttonPanel, gbc);

        updateFromCities();
        updateToCities();

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private JPanel createEmptyPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        JLabel label = new JLabel("Please select a payment method", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(108, 117, 125));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCreditCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int y = 0;
        addPaymentField(panel, "Card Number:", "4111 1111 1111 1111", gbc, y++);
        addPaymentField(panel, "Cardholder Name:", "John Doe", gbc, y++);
        
        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        expiryPanel.setOpaque(false);
        JTextField monthField = new JTextField(3);
        monthField.setText("MM");
        JTextField yearField = new JTextField(3);
        yearField.setText("YY");
        expiryPanel.add(monthField);
        expiryPanel.add(new JLabel("/"));
        expiryPanel.add(yearField);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel expiryLabel = new JLabel("Expiry Date:");
        expiryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(expiryLabel, gbc);
        gbc.gridx = 1;
        panel.add(expiryPanel, gbc);
        
        addPaymentField(panel, "CVV:", "123", gbc, y++);
        
        return panel;
    }

    private JPanel createDebitCardPanel() {
        return createCreditCardPanel();
    }

    private JPanel createPayPalPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int y = 0;
        addPaymentField(panel, "PayPal Email:", "user@example.com", gbc, y++);
        
        JLabel noteLabel = new JLabel("You will be redirected to PayPal to complete payment");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(noteLabel, gbc);
        
        return panel;
    }

    private JPanel createBankTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int y = 0;
        JLabel bankLabel = new JLabel("Bank Account Details:");
        bankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(bankLabel, gbc);
        
        JLabel accountLabel = new JLabel("Account: LogiXpress Sdn Bhd");
        accountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridy = y++;
        panel.add(accountLabel, gbc);
        
        JLabel numberLabel = new JLabel("Account No: 1234-5678-9012-3456");
        numberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridy = y++;
        panel.add(numberLabel, gbc);
        
        JLabel bankNameLabel = new JLabel("Bank: Maybank");
        bankNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridy = y++;
        panel.add(bankNameLabel, gbc);
        
        addPaymentField(panel, "Reference No:", "e.g., ORD-XXXX", gbc, y++);
        
        return panel;
    }

    private JPanel createEWalletPanel(String walletName) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int y = 0;
        JLabel titleLabel = new JLabel(walletName + " Payment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        addPaymentField(panel, walletName + " Number:", "012-3456789", gbc, y++);
        
        JLabel noteLabel = new JLabel("You will be redirected to " + walletName + " to complete payment");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = y++;
        panel.add(noteLabel, gbc);
        
        return panel;
    }

    private void addPaymentField(JPanel panel, String label, String placeholder, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        JTextField field = new JTextField(placeholder, 20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(new Color(108, 117, 125));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(108, 117, 125));
                }
            }
        });
        panel.add(field, gbc);
    }

    private void updatePaymentDetails() {
        String selected = (String) paymentMethodCombo.getSelectedItem();
        if (selected == null || selected.equals("Select Payment Method")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "EMPTY");
        } else if (selected.contains("Credit Card")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "CREDIT_CARD");
        } else if (selected.contains("Debit Card")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "DEBIT_CARD");
        } else if (selected.contains("PayPal")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "PAYPAL");
        } else if (selected.contains("Bank Transfer")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "BANK_TRANSFER");
        } else if (selected.contains("Touch 'n Go")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "TOUCH_N_GO");
        } else if (selected.contains("GrabPay")) {
            paymentDetailsLayout.show(paymentDetailsPanel, "GRABPAY");
        }
    }

    private JPanel createSenderInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(248, 249, 250));
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
            "Sender Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 123, 255)
        );
        box.setBorder(titledBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        box.add(nameLabel, gbc);

        JLabel nameValue = new JLabel(senderName != null ? senderName : "Not set");
        nameValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameValue.setForeground(new Color(0, 123, 255));
        gbc.gridx = 1;
        box.add(nameValue, gbc);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 1;
        box.add(phoneLabel, gbc);

        JLabel phoneValue = new JLabel(senderPhone != null ? senderPhone : "Not set");
        phoneValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneValue.setForeground(new Color(0, 123, 255));
        gbc.gridx = 1;
        box.add(phoneValue, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 2;
        box.add(emailLabel, gbc);

        JLabel emailValue = new JLabel(senderEmail != null ? senderEmail : "Not set");
        emailValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailValue.setForeground(new Color(0, 123, 255));
        gbc.gridx = 1;
        box.add(emailValue, gbc);

        JLabel addressSectionLabel = new JLabel("Pickup Address:");
        addressSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addressSectionLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        box.add(addressSectionLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel fromStateLabel = new JLabel("State:");
        fromStateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        box.add(fromStateLabel, gbc);

        fromStateCombo = new JComboBox<>(malaysiaData.keySet().toArray(new String[0]));
        fromStateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fromStateCombo.addActionListener(e -> updateFromCities());
        gbc.gridx = 1;
        box.add(fromStateCombo, gbc);

        JLabel fromCityLabel = new JLabel("City:");
        fromCityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 5;
        box.add(fromCityLabel, gbc);

        fromCityCombo = new JComboBox<>();
        fromCityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fromCityCombo.addActionListener(e -> updateFromPostcode());
        gbc.gridx = 1;
        box.add(fromCityCombo, gbc);

        JLabel fromPostcodeLabel = new JLabel("Postcode:");
        fromPostcodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 6;
        box.add(fromPostcodeLabel, gbc);

        fromPostcodeField = new JTextField(10);
        fromPostcodeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fromPostcodeField.setEditable(false);
        gbc.gridx = 1;
        box.add(fromPostcodeField, gbc);

        JLabel fromAddressLabel = new JLabel("Address Line:");
        fromAddressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 7;
        box.add(fromAddressLabel, gbc);

        fromAddressField = new JTextField(30);
        fromAddressField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        box.add(fromAddressField, gbc);

        return box;
    }

    private JPanel createRecipientInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(248, 249, 250));
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 167, 69), 1),
            "Recipient Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(40, 167, 69)
        );
        box.setBorder(titledBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JLabel recipientNameLabel = new JLabel("Recipient Name:*");
        recipientNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        box.add(recipientNameLabel, gbc);

        recipientNameField = new JTextField(30);
        recipientNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        box.add(recipientNameField, gbc);

        JLabel recipientPhoneLabel = new JLabel("Recipient Phone:*");
        recipientPhoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 1;
        box.add(recipientPhoneLabel, gbc);

        recipientPhoneField = new JTextField(30);
        recipientPhoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        box.add(recipientPhoneField, gbc);

        JLabel addressSectionLabel = new JLabel("Delivery Address:");
        addressSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addressSectionLabel.setForeground(new Color(40, 167, 69));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        box.add(addressSectionLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel toStateLabel = new JLabel("State:");
        toStateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        box.add(toStateLabel, gbc);

        toStateCombo = new JComboBox<>(malaysiaData.keySet().toArray(new String[0]));
        toStateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toStateCombo.addActionListener(e -> updateToCities());
        gbc.gridx = 1;
        box.add(toStateCombo, gbc);

        JLabel toCityLabel = new JLabel("City:");
        toCityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 4;
        box.add(toCityLabel, gbc);

        toCityCombo = new JComboBox<>();
        toCityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toCityCombo.addActionListener(e -> updateToPostcode());
        gbc.gridx = 1;
        box.add(toCityCombo, gbc);

        JLabel toPostcodeLabel = new JLabel("Postcode:");
        toPostcodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 5;
        box.add(toPostcodeLabel, gbc);

        toPostcodeField = new JTextField(10);
        toPostcodeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toPostcodeField.setEditable(false);
        gbc.gridx = 1;
        box.add(toPostcodeField, gbc);

        JLabel toAddressLabel = new JLabel("Address Line:");
        toAddressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 6;
        box.add(toAddressLabel, gbc);

        toAddressField = new JTextField(30);
        toAddressField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        box.add(toAddressField, gbc);

        return box;
    }

    private void updateFromCities() {
        String selectedState = (String) fromStateCombo.getSelectedItem();
        if (selectedState != null) {
            Map<String, String> cities = malaysiaData.get(selectedState);
            fromCityCombo.setModel(new DefaultComboBoxModel<>(cities.keySet().toArray(new String[0])));
            updateFromPostcode();
        }
    }

    private void updateToCities() {
        String selectedState = (String) toStateCombo.getSelectedItem();
        if (selectedState != null) {
            Map<String, String> cities = malaysiaData.get(selectedState);
            toCityCombo.setModel(new DefaultComboBoxModel<>(cities.keySet().toArray(new String[0])));
            updateToPostcode();
        }
    }

    private void updateFromPostcode() {
        String selectedState = (String) fromStateCombo.getSelectedItem();
        String selectedCity = (String) fromCityCombo.getSelectedItem();
        if (selectedState != null && selectedCity != null) {
            String postcode = malaysiaData.get(selectedState).get(selectedCity);
            fromPostcodeField.setText(postcode);
        }
        calculateEstimate();
    }

    private void updateToPostcode() {
        String selectedState = (String) toStateCombo.getSelectedItem();
        String selectedCity = (String) toCityCombo.getSelectedItem();
        if (selectedState != null && selectedCity != null) {
            String postcode = malaysiaData.get(selectedState).get(selectedCity);
            toPostcodeField.setText(postcode);
        }
        calculateEstimate();
    }

    private void calculateEstimate() {
        try {
            String weightText = weightField.getText().trim();
            if (weightText.isEmpty()) {
                estimatedCostLabel.setText("RM 0.00");
                deliveryTimeLabel.setText("3-5 business days");
                return;
            }
            
            double weight = Double.parseDouble(weightText);
            String packageType = (String) packageTypeCombo.getSelectedItem();
            String fromState = (String) fromStateCombo.getSelectedItem();
            String toState = (String) toStateCombo.getSelectedItem();
            
            if (fromState == null || toState == null || packageType == null) {
                estimatedCostLabel.setText("RM 0.00");
                return;
            }
            
            double baseRate = 8.0;
            double weightRate = weight * 3.5;
            double distance = calculateDistance(fromState, toState);
            double distanceRate = distance * 0.15;
            double typeMultiplier = getTypeMultiplier(packageType);
            
            double total = (baseRate + weightRate + distanceRate) * typeMultiplier;
            
            estimatedCostLabel.setText(String.format("RM %.2f", total));
            
            int days;
            if (distance < 200) {
                days = 1;
            } else if (distance < 500) {
                days = 2;
            } else if (distance < 1000) {
                days = 3;
            } else {
                days = 5;
            }
            deliveryTimeLabel.setText(days + " business days");
            
        } catch (NumberFormatException e) {
            estimatedCostLabel.setText("RM 0.00");
            deliveryTimeLabel.setText("3-5 business days");
        } catch (Exception e) {
            estimatedCostLabel.setText("RM 0.00");
            deliveryTimeLabel.setText("3-5 business days");
        }
    }

    private double calculateDistance(String fromState, String toState) {
        if (fromState == null || toState == null) return 300.0;
        if (fromState.equals(toState)) return 50.0;
        
        List<String> eastMalaysia = Arrays.asList("Sabah", "Sarawak", "Labuan");
        List<String> westMalaysia = Arrays.asList("Selangor", "Kuala Lumpur", "Penang", "Johor", "Perak", 
                                                   "Negeri Sembilan", "Melaka", "Pahang", "Kedah", 
                                                   "Kelantan", "Terengganu", "Perlis");
        
        boolean fromEast = eastMalaysia.contains(fromState);
        boolean toEast = eastMalaysia.contains(toState);
        
        if (fromEast != toEast) {
            return 1500.0;
        }
        
        if (westMalaysia.contains(fromState) && westMalaysia.contains(toState)) {
            Map<String, Double> distancesFromKL = new HashMap<>();
            distancesFromKL.put("Penang", 350.0);
            distancesFromKL.put("Johor", 320.0);
            distancesFromKL.put("Perak", 200.0);
            distancesFromKL.put("Negeri Sembilan", 70.0);
            distancesFromKL.put("Melaka", 150.0);
            distancesFromKL.put("Pahang", 250.0);
            distancesFromKL.put("Kedah", 400.0);
            distancesFromKL.put("Kelantan", 450.0);
            distancesFromKL.put("Terengganu", 400.0);
            distancesFromKL.put("Perlis", 450.0);
            
            if (distancesFromKL.containsKey(fromState)) {
                return distancesFromKL.get(fromState);
            }
            if (distancesFromKL.containsKey(toState)) {
                return distancesFromKL.get(toState);
            }
        }
        
        return 300.0;
    }

    private double getTypeMultiplier(String type) {
        if (type == null) return 1.0;
        switch(type) {
            case "Fragile Items": return 1.5;
            case "Electronics": return 1.3;
            case "Documents": return 0.8;
            case "Food": return 1.2;
            default: return 1.0;
        }
    }

    private String generateCustomOrderId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String datePrefix = dateFormat.format(new Date());
        
        int maxSequence = 0;
        File file = new File(ORDERS_FILE);
        
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    String[] parts = line.split("\\|", -1);
                    if (parts.length > 0) {
                        String orderId = parts[0];
                        if (orderId != null && orderId.startsWith(datePrefix)) {
                            try {
                                String seqStr = orderId.substring(datePrefix.length());
                                seqStr = seqStr.replaceAll("[^0-9]", "");
                                if (!seqStr.isEmpty()) {
                                    int seq = Integer.parseInt(seqStr);
                                    if (seq > maxSequence) {
                                        maxSequence = seq;
                                    }
                                }
                            } catch (Exception e) {
                                // Skip
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading orders.txt: " + e.getMessage());
            }
        }
        
        int nextSequence = maxSequence + 1;
        String sequenceStr = String.format("%03d", nextSequence);
        
        return datePrefix + sequenceStr;
    }

    private void saveOrderToFile(SenderOrder order) {
        PrintWriter writer = null;
        BufferedReader reader = null;
        
        try {
            File file = new File(ORDERS_FILE);
            System.out.println("\n=== SAVING ORDER TO FILE ===");
            System.out.println("File path: " + file.getAbsolutePath());
            System.out.println("Order ID: " + order.getId());
            
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            List<String> existingLines = new ArrayList<>();
            boolean orderExists = false;
            
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    existingLines.add(line);
                    if (!line.startsWith("#") && line.contains(order.getId())) {
                        orderExists = true;
                        System.out.println("Order already exists in file, will update");
                    }
                }
                reader.close();
                reader = null;
            }
            
            // Build the order line with ALL 29 fields matching the header
            String orderLine = buildOrderLine(order);
            
            writer = new PrintWriter(new FileWriter(file));
            
            // Write header if file is empty
            if (!file.exists() || file.length() == 0) {
                writer.println("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate");
                System.out.println("Created new file with header");
            } else {
                // Write all existing lines except the one being updated
                for (String line : existingLines) {
                    if (orderExists && !line.startsWith("#") && line.contains(order.getId())) {
                        continue;
                    }
                    writer.println(line);
                }
            }
            
            writer.println(orderLine);
            writer.flush();
            writer.close();
            writer = null;
            
            System.out.println("✓ Order successfully saved to file");
            System.out.println("Order line length: " + orderLine.split("\\|").length + " fields");
            
        } catch (IOException e) {
            System.err.println("❌ Error saving order: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "❌ Error saving order: " + e.getMessage(),
                "File Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    private String buildOrderLine(SenderOrder order) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        String currentDateTime = dateTimeFormat.format(new Date());
        
        // Calculate estimated delivery date
        String estimatedDeliveryDate = calculateEstimatedDeliveryDate();
        
        // Clean notes - remove pipes and newlines to prevent field shifting
        String cleanNotes = order.getNotes() != null ? 
            order.getNotes().replace("|", ";").replace("\n", " ").replace("\r", " ") : "";
        
        return String.join("|",
            safeString(order.getId()),                    // 0: id
            safeString(order.getCustomerName()),          // 1: customerName
            safeString(order.getCustomerPhone()),         // 2: customerPhone
            safeString(order.getCustomerEmail()),         // 3: customerEmail
            safeString(order.getCustomerAddress()),       // 4: customerAddress
            safeString(order.getRecipientName()),         // 5: recipientName
            safeString(order.getRecipientPhone()),        // 6: recipientPhone
            safeString(order.getRecipientAddress()),      // 7: recipientAddress
            "Pending",                                     // 8: status
            currentDateTime,                               // 9: orderDate
            estimatedDeliveryDate,                         // 10: estimatedDelivery
            "",                                            // 11: actualDelivery
            "",                                            // 12: driverId
            "",                                            // 13: vehicleId
            String.valueOf(order.getWeight()),             // 14: weight
            safeString(order.getDimensions()),             // 15: dimensions
            cleanNotes,                                    // 16: notes
            "",                                            // 17: reason
            "",                                            // 18: pickupTime
            "",                                            // 19: deliveryTime
            "0",                                           // 20: distance
            "0",                                           // 21: fuelUsed
            "",                                            // 22: deliveryPhoto
            "",                                            // 23: recipientSignature
            "false",                                       // 24: onTime
            "Paid",                                        // 25: paymentStatus
            safeString(order.getPaymentMethod()),          // 26: paymentMethod
            safeString(order.getTransactionId()),          // 27: transactionId
            safeString(order.getPaymentDate() != null ? order.getPaymentDate() : currentDateTime)  // 28: paymentDate
        );
    }
    
    private String calculateEstimatedDeliveryDate() {
        try {
            String deliveryText = deliveryTimeLabel.getText();
            int days = 3; // default
            
            if (deliveryText.contains("business days")) {
                String[] parts = deliveryText.split(" ");
                if (parts.length > 0) {
                    try {
                        days = Integer.parseInt(parts[0]);
                    } catch (NumberFormatException e) {
                        days = 3;
                    }
                }
            }
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, days);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(cal.getTime());
            
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 3);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(cal.getTime());
        }
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }

    private void createOrder() {
        if (!validateForm()) {
            return;
        }

        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        if (paymentMethod == null || paymentMethod.equals("Select Payment Method")) {
            JOptionPane.showMessageDialog(this,
                "Please select a payment method",
                "Payment Required",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String costText = estimatedCostLabel.getText().replace("RM", "").trim();
        double estimatedCost;
        try {
            estimatedCost = Double.parseDouble(costText);
            if (estimatedCost <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Invalid order amount. Please check weight and addresses.",
                    "Invalid Amount",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Unable to calculate order cost. Please check your inputs.",
                "Calculation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String orderId = generateCustomOrderId();
            System.out.println("Generated Order ID: " + orderId);
            
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            String transactionId = "TXN" + System.currentTimeMillis() + orderId.substring(0, 3);
            String paymentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            String fromState = (String) fromStateCombo.getSelectedItem();
            String fromCity = (String) fromCityCombo.getSelectedItem();
            String fromPostcode = fromPostcodeField.getText();
            String fromAddressLine = fromAddressField.getText().trim();
            
            String toState = (String) toStateCombo.getSelectedItem();
            String toCity = (String) toCityCombo.getSelectedItem();
            String toPostcode = toPostcodeField.getText();
            String toAddressLine = toAddressField.getText().trim();
            
            String fromAddress = fromAddressLine + ", " + fromCity + ", " + fromState + " " + fromPostcode;
            String toAddress = toAddressLine + ", " + toCity + ", " + toState + " " + toPostcode;
            
            String dimensions;
            String lengthStr = lengthField.getText().trim();
            String widthStr = widthField.getText().trim();
            String heightStr = heightField.getText().trim();

            try {
                double length = Double.parseDouble(lengthStr);
                double width = Double.parseDouble(widthStr);
                double height = Double.parseDouble(heightStr);
                dimensions = lengthStr + "x" + widthStr + "x" + heightStr;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for dimensions",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double weight = Double.parseDouble(weightField.getText());
            String recipientName = recipientNameField.getText().trim();
            String recipientPhone = recipientPhoneField.getText().trim();
            
            SenderOrder order = new SenderOrder(
                orderId,
                senderName,
                senderPhone,
                senderEmail,
                fromAddress,
                recipientName,
                recipientPhone,
                toAddress,
                weight,
                dimensions
            );
            
            order.setStatus("Pending");
            order.setOrderDate(currentDateTime);
            order.setPaymentStatus("Paid");
            order.setPaymentMethod(paymentMethod);
            order.setTransactionId(transactionId);
            order.setPaymentDate(paymentDate);
            order.setEstimatedDelivery(deliveryTimeLabel.getText());
            
            String packageType = (String) packageTypeCombo.getSelectedItem();
            String description = descriptionArea.getText().trim();
            
            // Clean notes - remove newlines and format properly
            StringBuilder notesBuilder = new StringBuilder();
            notesBuilder.append("Package Type: ").append(packageType);
            notesBuilder.append("; Estimated Cost: RM ").append(String.format("%.2f", estimatedCost));
            notesBuilder.append("; Estimated Delivery: ").append(deliveryTimeLabel.getText());
            notesBuilder.append("; Description: ").append(description);
            
            order.setNotes(notesBuilder.toString());
            
            JDialog processingDialog = createPaymentProcessingDialog(estimatedCost, paymentMethod);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Thread.sleep(2000);
                    return null;
                }

                @Override
                protected void done() {
                    processingDialog.dispose();
                    
                    // Save to orders.txt in correct format
                    saveOrderToFile(order);
                    
                    // Also add to SenderDataManager
                    SenderDataManager dataManager = SenderDataManager.getInstance();
                    dataManager.addOrder(order);
                    
                    // Force refresh to ensure data is loaded
                    dataManager.refreshData();
                    
                    // Add to main OrderStorage system
                    try {
                        logistics.orders.OrderStorage mainOrderStorage = new logistics.orders.OrderStorage();
                        mainOrderStorage.addOrderFromSender(order);
                        System.out.println("Order also added to main OrderStorage system");
                    } catch (Exception e) {
                        System.err.println("Warning: Could not add to main OrderStorage: " + e.getMessage());
                    }
                    
                    // Verify order was saved correctly
                    SenderOrder savedOrder = dataManager.getOrderById(orderId);
                    
                    if (savedOrder != null) {
                        System.out.println("Order successfully saved and verified!");
                        System.out.println("  - Order ID: " + savedOrder.getId());
                        System.out.println("  - Payment Method: " + savedOrder.getPaymentMethod());
                        System.out.println("  - Transaction ID: " + savedOrder.getTransactionId());
                        System.out.println("  - Payment Date: " + savedOrder.getPaymentDate());
                        System.out.println("  - Estimated Cost: RM " + savedOrder.getEstimatedCost());
                        
                        SenderDataManager.getInstance().refreshData();
                        dashboard.refreshStats();
                        
                        clearForm();
                        
                        JOptionPane.showMessageDialog(NewOrderPanel.this,
                            "✅ Order Created & Payment Successful!\n\n" +
                            "Order ID: " + orderId + "\n" +
                            "Date: " + currentDateTime + "\n" +
                            "From: " + fromCity + ", " + fromState + "\n" +
                            "To: " + toCity + ", " + toState + "\n" +
                            "Weight: " + weight + " kg\n" +
                            "Dimensions: " + dimensions + " cm\n" +
                            "Package Type: " + packageType + "\n" +
                            "Amount Paid: RM " + String.format("%.2f", estimatedCost) + "\n" +
                            "Payment Method: " + paymentMethod + "\n" +
                            "Transaction ID: " + transactionId + "\n\n" +
                            "Your order has been saved and payment confirmed.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Switch to tracking panel
                        dashboard.showPanel("TRACK");
                        
                        // Use invokeLater to ensure the panel is fully loaded before setting tracking number
                        SwingUtilities.invokeLater(() -> {
                            // Find and set tracking number with a small delay to ensure panel is ready
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ex) {
                                    // Ignore
                                }
                                SwingUtilities.invokeLater(() -> {
                                    findAndSetTrackingNumber(dashboard, orderId);
                                });
                            }).start();
                        });
                        
                    } else {
                        System.err.println("Order was not found after saving!");
                        JOptionPane.showMessageDialog(NewOrderPanel.this,
                            "⚠️ Order was created but could not be verified.\n" +
                            "Please check your orders list to confirm.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            processingDialog.setVisible(true);
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "❌ Invalid number format: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "❌ Error creating order: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JDialog createPaymentProcessingDialog(double amount, String paymentMethod) {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ancestor, "Processing Payment", true);
        
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(350, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JLabel amountLabel = new JLabel("Amount: RM " + String.format("%.2f", amount));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        amountLabel.setForeground(new Color(0, 123, 255));
        panel.add(amountLabel, gbc);
        
        gbc.gridy = 1;
        JLabel methodLabel = new JLabel("Payment Method: " + paymentMethod);
        methodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(methodLabel, gbc);
        
        gbc.gridy = 2;
        JLabel processingLabel = new JLabel("Processing payment...");
        processingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(processingLabel, gbc);
        
        gbc.gridy = 3;
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(250, 15));
        panel.add(progressBar, gbc);
        
        gbc.gridy = 4;
        JLabel pleaseWaitLabel = new JLabel("Please wait while we process your payment...");
        pleaseWaitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pleaseWaitLabel.setForeground(new Color(108, 117, 125));
        panel.add(pleaseWaitLabel, gbc);
        
        dialog.add(panel);
        
        return dialog;
    }

    private void findAndSetTrackingNumber(Container container, String orderId) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof TrackOrderPanel) {
                ((TrackOrderPanel) comp).setTrackingNumber(orderId);
                return;
            } else if (comp instanceof Container) {
                findAndSetTrackingNumber((Container) comp, orderId);
            }
        }
    }

    private boolean validateForm() {
        if (senderName == null || senderName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Sender name is required. Please check your profile.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (senderPhone == null || senderPhone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Sender phone is required. Please check your profile.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Sender email is required. Please check your profile.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (fromAddressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter the sender's address line", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (toAddressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter the recipient's address line", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (recipientNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter recipient name", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (recipientPhoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter recipient phone", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String fromState = (String) fromStateCombo.getSelectedItem();
        String fromCity = (String) fromCityCombo.getSelectedItem();
        String toState = (String) toStateCombo.getSelectedItem();
        String toCity = (String) toCityCombo.getSelectedItem();
        
        if (fromState != null && toState != null && fromCity != null && toCity != null) {
            if (fromState.equals(toState) && fromCity.equals(toCity)) {
                JOptionPane.showMessageDialog(this, 
                    "From and To addresses cannot be the same city!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        try {
            double weight = Double.parseDouble(weightField.getText().trim());
            if (weight <= 0 || weight > 100) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid weight (0.1 - 100 kg)", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid weight", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // NEW: Validate dimensions - all three fields required
        String lengthStr = lengthField.getText().trim();
        String widthStr = widthField.getText().trim();
        String heightStr = heightField.getText().trim();

        if (lengthStr.isEmpty() || widthStr.isEmpty() || heightStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter all dimensions (Length, Width, Height)", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double length = Double.parseDouble(lengthStr);
            double width = Double.parseDouble(widthStr);
            double height = Double.parseDouble(heightStr);
            
            if (length <= 0 || width <= 0 || height <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "All dimensions must be greater than 0", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (length > 200 || width > 200 || height > 200) {
                JOptionPane.showMessageDialog(this, 
                    "Dimensions should not exceed 200 cm", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for dimensions", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a package description", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void clearForm() {
        fromStateCombo.setSelectedIndex(0);
        fromAddressField.setText("");
        
        toStateCombo.setSelectedIndex(1);
        toAddressField.setText("");
        
        recipientNameField.setText("");
        recipientPhoneField.setText("");
        
        packageTypeCombo.setSelectedIndex(0);
        weightField.setText("");
        lengthField.setText("");
        widthField.setText("");
        heightField.setText("");
        descriptionArea.setText("");
        
        paymentMethodCombo.setSelectedIndex(0);
        paymentDetailsLayout.show(paymentDetailsPanel, "EMPTY");
        
        if (estimatedCostLabel != null) {
            estimatedCostLabel.setText("RM 0.00");
        }
        if (deliveryTimeLabel != null) {
            deliveryTimeLabel.setText("3-5 business days");
        }
        
        updateFromCities();
        updateToCities();
    }
}