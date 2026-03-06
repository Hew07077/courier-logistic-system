package sender;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewOrderPanel extends JPanel {
    private SenderDashboard dashboard;
    private OrderStorage orderStorage;
    
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

    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String senderAddress;

    private Map<String, Map<String, String>> malaysiaData;

    public NewOrderPanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        this.orderStorage = new OrderStorage();
        this.senderName = dashboard.getSenderName();
        this.senderEmail = dashboard.getSenderEmail();
        this.senderPhone = dashboard.getSenderPhone();
        this.senderAddress = dashboard.getSenderAddress();
        
        initializeMalaysiaData();
        initialize();
    }

    private void initializeMalaysiaData() {
        malaysiaData = new LinkedHashMap<>();
        
        // Selangor
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

        // Kuala Lumpur
        Map<String, String> klCities = new LinkedHashMap<>();
        klCities.put("Kuala Lumpur City", "50000");
        klCities.put("Bangsar", "59100");
        klCities.put("Bukit Bintang", "55100");
        klCities.put("Cheras KL", "56000");
        klCities.put("Setapak", "53300");
        klCities.put("Wangsa Maju", "53300");
        klCities.put("Kepong", "52100");
        malaysiaData.put("Kuala Lumpur", klCities);

        // Penang
        Map<String, String> penangCities = new LinkedHashMap<>();
        penangCities.put("George Town", "10000");
        penangCities.put("Bayan Lepas", "11900");
        penangCities.put("Butterworth", "12000");
        penangCities.put("Bukit Mertajam", "14000");
        penangCities.put("Nibong Tebal", "14300");
        malaysiaData.put("Penang", penangCities);

        // Johor
        Map<String, String> johorCities = new LinkedHashMap<>();
        johorCities.put("Johor Bahru", "80000");
        johorCities.put("Iskandar Puteri", "79100");
        johorCities.put("Pasir Gudang", "81700");
        johorCities.put("Batu Pahat", "83000");
        johorCities.put("Muar", "84000");
        johorCities.put("Kluang", "86000");
        malaysiaData.put("Johor", johorCities);

        // Perak
        Map<String, String> perakCities = new LinkedHashMap<>();
        perakCities.put("Ipoh", "30000");
        perakCities.put("Taiping", "34000");
        perakCities.put("Teluk Intan", "36000");
        perakCities.put("Sitiawan", "32000");
        perakCities.put("Kuala Kangsar", "33000");
        malaysiaData.put("Perak", perakCities);

        // Negeri Sembilan
        Map<String, String> nsCities = new LinkedHashMap<>();
        nsCities.put("Seremban", "70000");
        nsCities.put("Port Dickson", "71000");
        nsCities.put("Nilai", "71800");
        malaysiaData.put("Negeri Sembilan", nsCities);

        // Melaka
        Map<String, String> melakaCities = new LinkedHashMap<>();
        melakaCities.put("Melaka City", "75000");
        melakaCities.put("Ayer Keroh", "75450");
        melakaCities.put("Alor Gajah", "78000");
        malaysiaData.put("Melaka", melakaCities);

        // Sarawak
        Map<String, String> sarawakCities = new LinkedHashMap<>();
        sarawakCities.put("Kuching", "93000");
        sarawakCities.put("Miri", "98000");
        sarawakCities.put("Sibu", "96000");
        sarawakCities.put("Bintulu", "97000");
        malaysiaData.put("Sarawak", sarawakCities);

        // Sabah
        Map<String, String> sabahCities = new LinkedHashMap<>();
        sabahCities.put("Kota Kinabalu", "88000");
        sabahCities.put("Sandakan", "90000");
        sabahCities.put("Tawau", "91000");
        sabahCities.put("Lahad Datu", "91100");
        malaysiaData.put("Sabah", sabahCities);

        // Kedah
        Map<String, String> kedahCities = new LinkedHashMap<>();
        kedahCities.put("Alor Setar", "05000");
        kedahCities.put("Sungai Petani", "08000");
        kedahCities.put("Kulim", "09000");
        malaysiaData.put("Kedah", kedahCities);

        // Pahang
        Map<String, String> pahangCities = new LinkedHashMap<>();
        pahangCities.put("Kuantan", "25000");
        pahangCities.put("Bentong", "28700");
        pahangCities.put("Temerloh", "28000");
        malaysiaData.put("Pahang", pahangCities);

        // Kelantan
        Map<String, String> kelantanCities = new LinkedHashMap<>();
        kelantanCities.put("Kota Bharu", "15000");
        kelantanCities.put("Pasir Mas", "17000");
        malaysiaData.put("Kelantan", kelantanCities);

        // Terengganu
        Map<String, String> terengganuCities = new LinkedHashMap<>();
        terengganuCities.put("Kuala Terengganu", "20000");
        terengganuCities.put("Kemaman", "24000");
        malaysiaData.put("Terengganu", terengganuCities);

        // Perlis
        Map<String, String> perlisCities = new LinkedHashMap<>();
        perlisCities.put("Kangar", "01000");
        perlisCities.put("Arau", "02600");
        malaysiaData.put("Perlis", perlisCities);

        // Labuan
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

        // ===== SENDER INFORMATION BOX =====
        JPanel senderBox = createSenderInfoBox();
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(senderBox, gbc);

        // ===== RECIPIENT INFORMATION BOX =====
        JPanel recipientBox = createRecipientInfoBox();
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(recipientBox, gbc);

        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(230, 230, 230));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(separator, gbc);

        // Package Details Section
        JLabel packageLabel = new JLabel("Package Details:");
        packageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        packageLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 10, 5);
        formPanel.add(packageLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        // Package Type
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

        // Weight
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

        // Dimensions
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

        // Description
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

        // Estimate Section
        JPanel estimatePanel = new JPanel(new GridLayout(2, 2, 10, 5));
        estimatePanel.setBackground(new Color(248, 249, 250));
        estimatePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel estCostTitle = new JLabel("Estimated Cost (RM):");
        estCostTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        estimatePanel.add(estCostTitle);

        estimatedCostLabel = new JLabel("RM 0.00");
        estimatedCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        estimatedCostLabel.setForeground(new Color(0, 123, 255));
        estimatedCostLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        estimatePanel.add(estimatedCostLabel);

        JLabel deliveryTitle = new JLabel("Estimated Delivery:");
        deliveryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        estimatePanel.add(deliveryTitle);

        deliveryTimeLabel = new JLabel("3-5 business days");
        deliveryTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deliveryTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        estimatePanel.add(deliveryTimeLabel);

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(estimatePanel, gbc);

        // Buttons
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

        JButton submitBtn = new JButton("Create Order");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(new Color(0, 123, 255));
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

        // Initialize cities for default selections
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

    private JPanel createSenderInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(248, 249, 250));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255)),
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

        // Sender Name (from dashboard)
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

        // Sender Phone
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

        // Sender Email
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

        // Address Section
        JLabel addressSectionLabel = new JLabel("Pickup Address:");
        addressSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addressSectionLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        box.add(addressSectionLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        // State
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

        // City
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

        // Postcode
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

        // Address Line
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

        // Recipient Name
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

        // Recipient Phone
        JLabel recipientPhoneLabel = new JLabel("Recipient Phone:*");
        recipientPhoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 1;
        box.add(recipientPhoneLabel, gbc);

        recipientPhoneField = new JTextField(30);
        recipientPhoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        box.add(recipientPhoneField, gbc);

        // Address Section
        JLabel addressSectionLabel = new JLabel("Delivery Address:");
        addressSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addressSectionLabel.setForeground(new Color(40, 167, 69));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        box.add(addressSectionLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        // State
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

        // City
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

        // Postcode
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

        // Address Line
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
            double weight = weightField.getText().isEmpty() ? 0 : Double.parseDouble(weightField.getText());
            String packageType = (String) packageTypeCombo.getSelectedItem();
            String fromState = (String) fromStateCombo.getSelectedItem();
            String toState = (String) toStateCombo.getSelectedItem();
            
            double baseRate = 8.0;
            double weightRate = weight * 3.5;
            double distanceRate = calculateDistance(fromState, toState) * 0.15;
            double typeMultiplier = getTypeMultiplier(packageType);
            
            double total = (baseRate + weightRate + distanceRate) * typeMultiplier;
            
            estimatedCostLabel.setText(String.format("RM %.2f", total));
            
            double distance = calculateDistance(fromState, toState);
            int days;
            if (distance < 200) {
                days = 1;
            } else if (distance < 500) {
                days = 2;
            } else if (distance < 1000) {
                days = 3;
            } else {
                days = 4 + (int)(distance / 1000);
            }
            deliveryTimeLabel.setText(days + " business days");
            
        } catch (NumberFormatException e) {
            estimatedCostLabel.setText("RM 0.00");
        }
    }

    private double calculateDistance(String fromState, String toState) {
        if (fromState == null || toState == null) return 100;
        if (fromState.equals(toState)) return 50;
        
        Map<String, Map<String, Double>> distances = new HashMap<>();
        
        Map<String, Double> fromSelangor = new HashMap<>();
        fromSelangor.put("Penang", 350.0);
        fromSelangor.put("Johor", 320.0);
        fromSelangor.put("Perak", 200.0);
        fromSelangor.put("Negeri Sembilan", 70.0);
        fromSelangor.put("Melaka", 150.0);
        fromSelangor.put("Pahang", 250.0);
        fromSelangor.put("Kedah", 400.0);
        fromSelangor.put("Kelantan", 450.0);
        fromSelangor.put("Terengganu", 400.0);
        fromSelangor.put("Perlis", 450.0);
        fromSelangor.put("Sarawak", 1200.0);
        fromSelangor.put("Sabah", 1600.0);
        fromSelangor.put("Labuan", 1500.0);
        distances.put("Selangor", fromSelangor);
        distances.put("Kuala Lumpur", fromSelangor);
        
        Map<String, Double> fromPenang = new HashMap<>();
        fromPenang.put("Selangor", 350.0);
        fromPenang.put("Kuala Lumpur", 350.0);
        fromPenang.put("Johor", 650.0);
        fromPenang.put("Perak", 150.0);
        fromPenang.put("Kedah", 100.0);
        fromPenang.put("Perlis", 150.0);
        fromPenang.put("Kelantan", 300.0);
        fromPenang.put("Terengganu", 350.0);
        fromPenang.put("Pahang", 400.0);
        fromPenang.put("Sarawak", 1400.0);
        fromPenang.put("Sabah", 1800.0);
        distances.put("Penang", fromPenang);
        
        Map<String, Double> fromJohor = new HashMap<>();
        fromJohor.put("Selangor", 320.0);
        fromJohor.put("Kuala Lumpur", 320.0);
        fromJohor.put("Penang", 650.0);
        fromJohor.put("Melaka", 200.0);
        fromJohor.put("Negeri Sembilan", 250.0);
        fromJohor.put("Pahang", 300.0);
        fromJohor.put("Sarawak", 900.0);
        fromJohor.put("Sabah", 1300.0);
        distances.put("Johor", fromJohor);
        
        if (distances.containsKey(fromState) && distances.get(fromState).containsKey(toState)) {
            return distances.get(fromState).get(toState);
        }
        
        return 300.0;
    }

    private double getTypeMultiplier(String type) {
        switch(type) {
            case "Fragile Items": return 1.5;
            case "Electronics": return 1.3;
            case "Documents": return 0.8;
            case "Food": return 1.2;
            default: return 1.0;
        }
    }

    private void createOrder() {
        if (!validateForm()) {
            return;
        }

        try {
            // Generate order ID using OrderStorage
            String orderId = orderStorage.generateOrderId();
            
            // Build full addresses
            String fromAddress = fromAddressField.getText().trim() + ", " + 
                                 fromCityCombo.getSelectedItem() + ", " +
                                 fromStateCombo.getSelectedItem() + " " +
                                 fromPostcodeField.getText();
            
            String toAddress = toAddressField.getText().trim() + ", " + 
                               toCityCombo.getSelectedItem() + ", " +
                               toStateCombo.getSelectedItem() + " " +
                               toPostcodeField.getText();
            
            // Build dimensions string
            String dimensions = "";
            if (!lengthField.getText().isEmpty() && 
                !widthField.getText().isEmpty() && 
                !heightField.getText().isEmpty()) {
                dimensions = lengthField.getText() + "x" + 
                            widthField.getText() + "x" + 
                            heightField.getText();
            } else {
                dimensions = "0x0x0";
            }
            
            // Create order using the logistics.orders.Order constructor
            Order order = new Order(
                orderId,
                senderName,
                senderPhone,
                senderEmail,
                fromAddress,
                recipientNameField.getText().trim(),
                recipientPhoneField.getText().trim(),
                toAddress,
                Double.parseDouble(weightField.getText()),
                dimensions
            );
            
            // Set additional fields in notes
            String packageInfo = "Package Type: " + packageTypeCombo.getSelectedItem();
            String costInfo = "\nEstimated Cost: " + estimatedCostLabel.getText();
            String deliveryInfo = "\nEstimated Delivery: " + deliveryTimeLabel.getText();
            String descInfo = "\nDescription: " + descriptionArea.getText();
            
            order.notes = packageInfo + costInfo + deliveryInfo + descInfo;
            
            // Save order using FileDataManager
            FileDataManager.getInstance().addOrder(order);
            
            // Verify the order was saved (silently)
            Order savedOrder = FileDataManager.getInstance().getOrderById(orderId);
            if (savedOrder != null) {
                clearForm();
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "✅ Order created successfully!\n\n" +
                    "Order ID: " + orderId + "\n" +
                    "From: " + fromAddress + "\n" +
                    "To: " + toAddress + "\n\n" +
                    "You can now track this order.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Jump to tracking page
                dashboard.showPanel("TRACK");
                
                // Set the tracking number
                SwingUtilities.invokeLater(() -> {
                    findAndSetTrackingNumber(dashboard, orderId);
                });
            } else {
                JOptionPane.showMessageDialog(this, 
                    "⚠️ Order was created but may not have been saved properly.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "❌ Error creating order: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        // Validate sender information
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

        if (fromStateCombo.getSelectedItem().equals(toStateCombo.getSelectedItem()) &&
            fromCityCombo.getSelectedItem().equals(toCityCombo.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, 
                "From and To addresses cannot be the same!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double weight = Double.parseDouble(weightField.getText());
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
        
        estimatedCostLabel.setText("RM 0.00");
        deliveryTimeLabel.setText("3-5 business days");
        
        updateFromCities();
        updateToCities();
    }
}