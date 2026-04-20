package admin.management;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import logistics.orders.OrderStorage;
import sender.SenderOrder;
import sender.SenderOrderRepository;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CreateOrderPanel extends JPanel {
    private OrderManagement parentOrderManagement;
    private OrderStorage orderStorage;

    // Sender fields (editable by admin)
    private JTextField senderNameField;
    private JTextField senderEmailField;
    private JTextField senderPhoneField;
    
    // Address fields
    private JTextField fromAddressField;
    private JComboBox<String> fromStateCombo;
    private JComboBox<String> fromCityCombo;
    private JTextField fromPostcodeField;
    
    private JTextField toAddressField;
    private JComboBox<String> toStateCombo;
    private JComboBox<String> toCityCombo;
    private JTextField toPostcodeField;
    
    private JTextField recipientNameField;
    private JTextField recipientPhoneField;
    
    private JComboBox<String> packageTypeCombo;
    private String customPackageType = null;
    private JTextField weightField;
    private JTextField lengthField;
    private JTextField widthField;
    private JTextField heightField;
    private JTextArea descriptionArea;
    private JLabel estimatedCostLabel;
    private JLabel deliveryTimeLabel;
    
    // Shipping speed - Radio buttons
    private JRadioButton standardSpeedRadio;
    private JRadioButton expressSpeedRadio;
    private ButtonGroup speedButtonGroup;
    private JLabel speedDescriptionLabel;
    
    private JCheckBox insuranceCheckBox;
    private JLabel insuranceCostLabel;
    private JTextField declaredValueField;
    private JPanel insurancePanel;
    private JLabel shippingCostDisplayLabel;
    
    private JLabel distanceLabel;
    
    private JComboBox<String> paymentMethodCombo;
    private JPanel paymentDetailsPanel;
    private CardLayout paymentDetailsLayout;

    private Map<String, Map<String, String>> malaysiaData;
    private List<String> stateList;
    
    private static final String ORDERS_FILE = "orders.txt";
    
    private static final double INSURANCE_RATE = 0.015;
    private static final double MIN_INSURANCE = 5.0;
    private static final double RATE_PER_KG = 4.50;
    private static final double RATE_PER_KM = 0.25;
    private static final double BASE_RATE = 5.00;
    
    // Malaysian Banks list sorted alphabetically
    private static final String[] MALAYSIAN_BANKS = {
        "Affin Bank", "Alliance Bank", "AmBank", "Bank Islam", 
        "Bank Muamalat", "Bank Rakyat", "CIMB Bank", "Hong Leong Bank (HLB)", 
        "HSBC Bank", "Maybank", "OCBC Bank", "Public Bank (PBB)", 
        "RHB Bank", "UOB Malaysia"
    };
    
    // Package types sorted alphabetically
    private static final String[] PACKAGE_TYPES = {
        "Books", "Clothing", "Documents", "Electronics", "Food", "Fragile Items", "Other"
    };
    
    // Payment methods sorted alphabetically
    private static final String[] PAYMENT_METHODS = {
        "Bank Transfer", "Credit Card (Visa/Mastercard)", "Debit Card", "GrabPay", "PayPal", "Touch 'n Go"
    };
    
    // Months for expiry date
    private static final String[] MONTHS = {
        "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
    };

    // CONSTRUCTOR
    public CreateOrderPanel(OrderManagement parentOrderManagement) {
        this.parentOrderManagement = parentOrderManagement;
        this.orderStorage = new OrderStorage();
        
        initializeMalaysiaData();
        initialize();
    }

    private void initializeMalaysiaData() {
        malaysiaData = new LinkedHashMap<>();
        stateList = new ArrayList<>();
        
        Map<String, String> selangorCities = new LinkedHashMap<>();
        selangorCities.put("Ampang", "68000");
        selangorCities.put("Cheras", "43200");
        selangorCities.put("Kajang", "43000");
        selangorCities.put("Klang", "41000");
        selangorCities.put("Petaling Jaya", "46100");
        selangorCities.put("Rawang", "48000");
        selangorCities.put("Shah Alam", "40000");
        selangorCities.put("Subang Jaya", "47500");
        malaysiaData.put("Selangor", selangorCities);
        stateList.add("Selangor");

        Map<String, String> klCities = new LinkedHashMap<>();
        klCities.put("Bangsar", "59100");
        klCities.put("Bukit Bintang", "55100");
        klCities.put("Cheras KL", "56000");
        klCities.put("Kepong", "52100");
        klCities.put("Kuala Lumpur City", "50000");
        klCities.put("Setapak", "53300");
        klCities.put("Wangsa Maju", "53300");
        malaysiaData.put("Kuala Lumpur", klCities);
        stateList.add("Kuala Lumpur");

        Map<String, String> penangCities = new LinkedHashMap<>();
        penangCities.put("Bayan Lepas", "11900");
        penangCities.put("Bukit Mertajam", "14000");
        penangCities.put("Butterworth", "12000");
        penangCities.put("George Town", "10000");
        penangCities.put("Nibong Tebal", "14300");
        malaysiaData.put("Penang", penangCities);
        stateList.add("Penang");

        Map<String, String> johorCities = new LinkedHashMap<>();
        johorCities.put("Batu Pahat", "83000");
        johorCities.put("Iskandar Puteri", "79100");
        johorCities.put("Johor Bahru", "80000");
        johorCities.put("Kluang", "86000");
        johorCities.put("Muar", "84000");
        johorCities.put("Pasir Gudang", "81700");
        malaysiaData.put("Johor", johorCities);
        stateList.add("Johor");

        Map<String, String> perakCities = new LinkedHashMap<>();
        perakCities.put("Ipoh", "30000");
        perakCities.put("Kuala Kangsar", "33000");
        perakCities.put("Sitiawan", "32000");
        perakCities.put("Taiping", "34000");
        perakCities.put("Teluk Intan", "36000");
        malaysiaData.put("Perak", perakCities);
        stateList.add("Perak");

        Map<String, String> nsCities = new LinkedHashMap<>();
        nsCities.put("Nilai", "71800");
        nsCities.put("Port Dickson", "71000");
        nsCities.put("Seremban", "70000");
        malaysiaData.put("Negeri Sembilan", nsCities);
        stateList.add("Negeri Sembilan");

        Map<String, String> melakaCities = new LinkedHashMap<>();
        melakaCities.put("Alor Gajah", "78000");
        melakaCities.put("Ayer Keroh", "75450");
        melakaCities.put("Melaka City", "75000");
        malaysiaData.put("Melaka", melakaCities);
        stateList.add("Melaka");

        Map<String, String> sarawakCities = new LinkedHashMap<>();
        sarawakCities.put("Bintulu", "97000");
        sarawakCities.put("Kuching", "93000");
        sarawakCities.put("Miri", "98000");
        sarawakCities.put("Sibu", "96000");
        malaysiaData.put("Sarawak", sarawakCities);
        stateList.add("Sarawak");

        Map<String, String> sabahCities = new LinkedHashMap<>();
        sabahCities.put("Kota Kinabalu", "88000");
        sabahCities.put("Lahad Datu", "91100");
        sabahCities.put("Sandakan", "90000");
        sabahCities.put("Tawau", "91000");
        malaysiaData.put("Sabah", sabahCities);
        stateList.add("Sabah");

        Map<String, String> kedahCities = new LinkedHashMap<>();
        kedahCities.put("Alor Setar", "05000");
        kedahCities.put("Kulim", "09000");
        kedahCities.put("Sungai Petani", "08000");
        malaysiaData.put("Kedah", kedahCities);
        stateList.add("Kedah");

        Map<String, String> pahangCities = new LinkedHashMap<>();
        pahangCities.put("Bentong", "28700");
        pahangCities.put("Kuantan", "25000");
        pahangCities.put("Temerloh", "28000");
        malaysiaData.put("Pahang", pahangCities);
        stateList.add("Pahang");

        Map<String, String> kelantanCities = new LinkedHashMap<>();
        kelantanCities.put("Kota Bharu", "15000");
        kelantanCities.put("Pasir Mas", "17000");
        malaysiaData.put("Kelantan", kelantanCities);
        stateList.add("Kelantan");

        Map<String, String> terengganuCities = new LinkedHashMap<>();
        terengganuCities.put("Kemaman", "24000");
        terengganuCities.put("Kuala Terengganu", "20000");
        malaysiaData.put("Terengganu", terengganuCities);
        stateList.add("Terengganu");

        Map<String, String> perlisCities = new LinkedHashMap<>();
        perlisCities.put("Arau", "02600");
        perlisCities.put("Kangar", "01000");
        malaysiaData.put("Perlis", perlisCities);
        stateList.add("Perlis");

        Map<String, String> labuanCities = new LinkedHashMap<>();
        labuanCities.put("Victoria", "87000");
        malaysiaData.put("Labuan", labuanCities);
        stateList.add("Labuan");
        
        // Sort states alphabetically
        Collections.sort(stateList);
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
        estimatedCostLabel = new JLabel("RM 0.00");
        deliveryTimeLabel = new JLabel("--");
        insuranceCostLabel = new JLabel("RM 0.00");
        shippingCostDisplayLabel = new JLabel("RM 0.00");
        distanceLabel = new JLabel("-- km");
        
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

        JLabel typeLabel = new JLabel("Package Type:*");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 1;
        formPanel.add(typeLabel, gbc);

        packageTypeCombo = new JComboBox<>(PACKAGE_TYPES);
        packageTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        packageTypeCombo.setSelectedIndex(-1);
        packageTypeCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select Package Type"));
        packageTypeCombo.addActionListener(e -> {
            String selected = (String) packageTypeCombo.getSelectedItem();
            if ("Other".equals(selected)) {
                showCustomPackageTypeDialog();
            }
            calculateEstimate();
        });
        gbc.gridx = 1;
        formPanel.add(packageTypeCombo, gbc);

        JLabel weightLabel = new JLabel("Weight (kg):*");
        weightLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        formPanel.add(weightLabel, gbc);

        JPanel weightPanel = new JPanel(new BorderLayout(5, 0));
        weightPanel.setOpaque(false);
        weightField = new JTextField(10);
        weightField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        weightField.setForeground(new Color(108, 117, 125));
        weightField.setText("Enter weight in kg");
        weightField.addFocusListener(new PlaceholderFocusListener(weightField, "Enter weight in kg"));
        weightField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        weightPanel.add(weightField, BorderLayout.CENTER);
        
        JLabel perKgRateLabel = new JLabel("  (RM " + RATE_PER_KG + " per kg)");
        perKgRateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        perKgRateLabel.setForeground(new Color(108, 117, 125));
        weightPanel.add(perKgRateLabel, BorderLayout.EAST);
        
        gbc.gridx = 1;
        formPanel.add(weightPanel, gbc);

        JLabel dimLabel = new JLabel("Dimensions (cm):*");
        dimLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        formPanel.add(dimLabel, gbc);

        JPanel dimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dimPanel.setOpaque(false);

        dimPanel.add(new JLabel("L:"));
        lengthField = new JTextField(5);
        lengthField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lengthField.setForeground(new Color(108, 117, 125));
        lengthField.setText("cm");
        lengthField.setPreferredSize(new Dimension(60, 25));
        lengthField.addFocusListener(new PlaceholderFocusListener(lengthField, "cm"));
        lengthField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        dimPanel.add(lengthField);

        dimPanel.add(new JLabel("W:"));
        widthField = new JTextField(5);
        widthField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        widthField.setForeground(new Color(108, 117, 125));
        widthField.setText("cm");
        widthField.setPreferredSize(new Dimension(60, 25));
        widthField.addFocusListener(new PlaceholderFocusListener(widthField, "cm"));
        widthField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        dimPanel.add(widthField);

        dimPanel.add(new JLabel("H:"));
        heightField = new JTextField(5);
        heightField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heightField.setForeground(new Color(108, 117, 125));
        heightField.setText("cm");
        heightField.setPreferredSize(new Dimension(60, 25));
        heightField.addFocusListener(new PlaceholderFocusListener(heightField, "cm"));
        heightField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        dimPanel.add(heightField);
        
        dimPanel.add(new JLabel("cm"));

        gbc.gridx = 1;
        formPanel.add(dimPanel, gbc);

        JLabel descLabel = new JLabel("Description:*");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        formPanel.add(descLabel, gbc);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setForeground(new Color(108, 117, 125));
        descriptionArea.setText("Describe the package contents...");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (descriptionArea.getText().equals("Describe the package contents...")) {
                    descriptionArea.setText("");
                    descriptionArea.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (descriptionArea.getText().isEmpty()) {
                    descriptionArea.setText("Describe the package contents...");
                    descriptionArea.setForeground(new Color(108, 117, 125));
                }
            }
        });
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        gbc.gridx = 1;
        formPanel.add(descScroll, gbc);

        // ========== SHIPPING SPEED SECTION - RADIO BUTTONS ==========
        JLabel shippingLabel = new JLabel("Shipping Speed:*");
        shippingLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        shippingLabel.setForeground(new Color(255, 193, 7));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 10, 5);
        formPanel.add(shippingLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        speedPanel.setOpaque(false);

        standardSpeedRadio = new JRadioButton("Standard (3-5 business days)", true);
        standardSpeedRadio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        standardSpeedRadio.addActionListener(e -> {
            calculateEstimate();
            updateSpeedDescription();
        });

        expressSpeedRadio = new JRadioButton("Express (1-2 business days)", false);
        expressSpeedRadio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        expressSpeedRadio.addActionListener(e -> {
            calculateEstimate();
            updateSpeedDescription();
        });

        speedButtonGroup = new ButtonGroup();
        speedButtonGroup.add(standardSpeedRadio);
        speedButtonGroup.add(expressSpeedRadio);

        speedPanel.add(standardSpeedRadio);
        speedPanel.add(expressSpeedRadio);

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(speedPanel, gbc);

        speedDescriptionLabel = new JLabel("Standard: 3-5 business days delivery (Economy shipping)");
        speedDescriptionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        speedDescriptionLabel.setForeground(new Color(108, 117, 125));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        formPanel.add(speedDescriptionLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel insuranceHeaderLabel = new JLabel("Package Insurance (Optional):");
        insuranceHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        insuranceHeaderLabel.setForeground(new Color(40, 167, 69));
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 10, 5);
        formPanel.add(insuranceHeaderLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        insuranceCheckBox = new JCheckBox("Add Insurance for High-Value Items (Covers loss/damage)");
        insuranceCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        insuranceCheckBox.addActionListener(e -> {
            toggleInsurancePanel();
            calculateEstimate();
        });
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(insuranceCheckBox, gbc);

        insurancePanel = new JPanel(new GridBagLayout());
        insurancePanel.setBackground(new Color(255, 248, 225));
        insurancePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        insurancePanel.setVisible(false);
        
        GridBagConstraints insGbc = new GridBagConstraints();
        insGbc.fill = GridBagConstraints.HORIZONTAL;
        insGbc.insets = new Insets(5, 5, 5, 5);
        insGbc.weightx = 1.0;
        
        insGbc.gridx = 0;
        insGbc.gridy = 0;
        insurancePanel.add(new JLabel("Declared Value (RM):*"), insGbc);
        
        declaredValueField = new JTextField(15);
        declaredValueField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        declaredValueField.setForeground(new Color(108, 117, 125));
        declaredValueField.setText("Enter declared value");
        declaredValueField.addFocusListener(new PlaceholderFocusListener(declaredValueField, "Enter declared value"));
        declaredValueField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        insGbc.gridx = 1;
        insurancePanel.add(declaredValueField, insGbc);
        
        JLabel insuranceInfoLabel = new JLabel("Insurance covers loss/damage at 1.5% of declared value (min RM 5)");
        insuranceInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        insuranceInfoLabel.setForeground(new Color(108, 117, 125));
        insGbc.gridx = 0;
        insGbc.gridy = 1;
        insGbc.gridwidth = 2;
        insurancePanel.add(insuranceInfoLabel, insGbc);
        
        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        formPanel.add(insurancePanel, gbc);

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

        paymentMethodCombo = new JComboBox<>(PAYMENT_METHODS);
        paymentMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentMethodCombo.setSelectedIndex(-1);
        paymentMethodCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select Payment Method"));
        paymentMethodCombo.addActionListener(e -> updatePaymentDetails());
        gbc.gridx = 1;
        formPanel.add(paymentMethodCombo, gbc);

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
        JPanel estimatePanel = new JPanel(new GridBagLayout());
        estimatePanel.setBackground(new Color(248, 249, 250));
        estimatePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints estGbc = new GridBagConstraints();
        estGbc.fill = GridBagConstraints.HORIZONTAL;
        estGbc.insets = new Insets(5, 10, 5, 10);
        estGbc.weightx = 1.0;
        
        estGbc.gridx = 0;
        estGbc.gridy = 0;
        estGbc.gridwidth = 1;
        JLabel distanceTitle = new JLabel("Distance:");
        distanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estimatePanel.add(distanceTitle, estGbc);
        
        estGbc.gridx = 1;
        distanceLabel = new JLabel("-- km");
        distanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        distanceLabel.setForeground(new Color(0, 123, 255));
        estimatePanel.add(distanceLabel, estGbc);
        
        estGbc.gridx = 0;
        estGbc.gridy = 1;
        JLabel shippingCostTitle = new JLabel("Shipping Cost:");
        shippingCostTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estimatePanel.add(shippingCostTitle, estGbc);
        
        estGbc.gridx = 1;
        shippingCostDisplayLabel = new JLabel("RM 0.00");
        shippingCostDisplayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estimatePanel.add(shippingCostDisplayLabel, estGbc);
        
        estGbc.gridx = 0;
        estGbc.gridy = 2;
        JLabel insuranceTitle = new JLabel("Insurance Cost:");
        insuranceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estimatePanel.add(insuranceTitle, estGbc);
        
        estGbc.gridx = 1;
        insuranceCostLabel = new JLabel("RM 0.00");
        insuranceCostLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estimatePanel.add(insuranceCostLabel, estGbc);
        
        estGbc.gridx = 0;
        estGbc.gridy = 3;
        estGbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        estimatePanel.add(sep, estGbc);
        
        estGbc.gridx = 0;
        estGbc.gridy = 4;
        estGbc.gridwidth = 1;
        JLabel totalTitle = new JLabel("Total Amount:");
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        estimatePanel.add(totalTitle, estGbc);
        
        estGbc.gridx = 1;
        estimatedCostLabel = new JLabel("RM 0.00");
        estimatedCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        estimatedCostLabel.setForeground(new Color(40, 167, 69));
        estimatePanel.add(estimatedCostLabel, estGbc);
        
        estGbc.gridx = 0;
        estGbc.gridy = 5;
        JLabel deliveryTitle = new JLabel("Estimated Delivery:");
        deliveryTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estimatePanel.add(deliveryTitle, estGbc);
        
        estGbc.gridx = 1;
        deliveryTimeLabel = new JLabel("3-5 business days (Standard)");
        deliveryTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deliveryTimeLabel.setForeground(new Color(0, 123, 255));
        estimatePanel.add(deliveryTimeLabel, estGbc);

        gbc.gridx = 0;
        gbc.gridy = gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(estimatePanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color( 220, 53, 69));
        cancelBtn.setBorderPainted(false);
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

        initializeCombosWithPlaceholders();
        updateFromCities();
        updateToCities();
        updateSpeedDescription();

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    // Placeholder Focus Listener inner class
    class PlaceholderFocusListener extends FocusAdapter {
        private JTextField textField;
        private String placeholder;
        
        public PlaceholderFocusListener(JTextField textField, String placeholder) {
            this.textField = textField;
            this.placeholder = placeholder;
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            if (textField.getText().equals(placeholder)) {
                textField.setText("");
                textField.setForeground(Color.BLACK);
            }
        }
        
        @Override
        public void focusLost(FocusEvent e) {
            if (textField.getText().isEmpty()) {
                textField.setText(placeholder);
                textField.setForeground(new Color(108, 117, 125));
            }
        }
    }

    private JPanel createSenderInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(248, 249, 250));
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
            "Sender Information (Enter Customer Details)", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(0, 123, 255));
        box.setBorder(titledBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Name field
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("Sender Name:*");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        senderNameField = new JTextField(30);
        senderNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        senderNameField.setForeground(new Color(108, 117, 125));
        senderNameField.setText("Enter customer name");
        senderNameField.addFocusListener(new PlaceholderFocusListener(senderNameField, "Enter customer name"));
        box.add(senderNameField, gbc);

        // Phone field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel phoneLabel = new JLabel("Sender Phone:*");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        senderPhoneField = new JTextField(30);
        senderPhoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        senderPhoneField.setForeground(new Color(108, 117, 125));
        senderPhoneField.setText("0123456789");
        senderPhoneField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (senderPhoneField.getText().equals("0123456789")) {
                    senderPhoneField.setText("");
                    senderPhoneField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                String text = senderPhoneField.getText().trim();
                if (text.isEmpty()) {
                    senderPhoneField.setText("0123456789");
                    senderPhoneField.setForeground(new Color(108, 117, 125));
                } else if (isValidMalaysianPhoneNumber(text)) {
                    String formatted = formatPhoneNumber(text);
                    senderPhoneField.setText(formatted);
                    senderPhoneField.setForeground(Color.BLACK);
                }
            }
        });
        box.add(senderPhoneField, gbc);

        // Email field
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Sender Email:*");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        senderEmailField = new JTextField(30);
        senderEmailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        senderEmailField.setForeground(new Color(108, 117, 125));
        senderEmailField.setText("customer@example.com");
        senderEmailField.addFocusListener(new PlaceholderFocusListener(senderEmailField, "customer@example.com"));
        box.add(senderEmailField, gbc);

        // Address section header
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        JLabel addressHeader = new JLabel("Pickup Address:");
        addressHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.add(addressHeader, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        // Address line
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        box.add(new JLabel("Address Line:*"), gbc);
        fromAddressField = new JTextField(30);
        fromAddressField.setForeground(new Color(108, 117, 125));
        fromAddressField.setText("Enter street address");
        fromAddressField.addFocusListener(new PlaceholderFocusListener(fromAddressField, "Enter street address"));
        fromAddressField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        gbc.gridx = 1;
        box.add(fromAddressField, gbc);

        // State
        gbc.gridx = 0; gbc.gridy = 5;
        box.add(new JLabel("State:*"), gbc);
        fromStateCombo = new JComboBox<>();
        fromStateCombo.addActionListener(e -> { updateFromCities(); calculateEstimate(); });
        gbc.gridx = 1;
        box.add(fromStateCombo, gbc);

        // City
        gbc.gridx = 0; gbc.gridy = 6;
        box.add(new JLabel("City:*"), gbc);
        fromCityCombo = new JComboBox<>();
        fromCityCombo.addActionListener(e -> { updateFromPostcode(); calculateEstimate(); });
        gbc.gridx = 1;
        box.add(fromCityCombo, gbc);

        // Postcode
        gbc.gridx = 0; gbc.gridy = 7;
        box.add(new JLabel("Postcode:"), gbc);
        fromPostcodeField = new JTextField(10);
        fromPostcodeField.setEditable(false);
        gbc.gridx = 1;
        box.add(fromPostcodeField, gbc);

        return box;
    }

    private JPanel createRecipientInfoBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(248, 249, 250));
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 167, 69), 1),
            "Recipient Information", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(40, 167, 69));
        box.setBorder(titledBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        box.add(new JLabel("Recipient Name:*"), gbc);
        recipientNameField = new JTextField(30);
        recipientNameField.setForeground(new Color(108, 117, 125));
        recipientNameField.setText("Full name");
        recipientNameField.addFocusListener(new PlaceholderFocusListener(recipientNameField, "Full name"));
        gbc.gridx = 1;
        box.add(recipientNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        box.add(new JLabel("Recipient Phone:*"), gbc);
        recipientPhoneField = new JTextField(30);
        recipientPhoneField.setForeground(new Color(108, 117, 125));
        recipientPhoneField.setText("0123456789");
        recipientPhoneField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (recipientPhoneField.getText().equals("0123456789")) {
                    recipientPhoneField.setText("");
                    recipientPhoneField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                String text = recipientPhoneField.getText().trim();
                if (text.isEmpty()) {
                    recipientPhoneField.setText("0123456789");
                    recipientPhoneField.setForeground(new Color(108, 117, 125));
                } else if (isValidMalaysianPhoneNumber(text)) {
                    String formatted = formatPhoneNumber(text);
                    recipientPhoneField.setText(formatted);
                    recipientPhoneField.setForeground(Color.BLACK);
                }
            }
        });
        gbc.gridx = 1;
        box.add(recipientPhoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        box.add(new JLabel("Delivery Address:"), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        box.add(new JLabel("Address Line:*"), gbc);
        toAddressField = new JTextField(30);
        toAddressField.setForeground(new Color(108, 117, 125));
        toAddressField.setText("Enter street address");
        toAddressField.addFocusListener(new PlaceholderFocusListener(toAddressField, "Enter street address"));
        toAddressField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { calculateEstimate(); }
        });
        gbc.gridx = 1;
        box.add(toAddressField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        box.add(new JLabel("State:*"), gbc);
        toStateCombo = new JComboBox<>();
        toStateCombo.addActionListener(e -> { updateToCities(); calculateEstimate(); });
        gbc.gridx = 1;
        box.add(toStateCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        box.add(new JLabel("City:*"), gbc);
        toCityCombo = new JComboBox<>();
        toCityCombo.addActionListener(e -> { updateToPostcode(); calculateEstimate(); });
        gbc.gridx = 1;
        box.add(toCityCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        box.add(new JLabel("Postcode:"), gbc);
        toPostcodeField = new JTextField(10);
        toPostcodeField.setEditable(false);
        gbc.gridx = 1;
        box.add(toPostcodeField, gbc);

        return box;
    }

    private void showCustomPackageTypeDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Specify Package Type", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel instructionLabel = new JLabel("Please specify the package type:");
        instructionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(instructionLabel, BorderLayout.NORTH);
        
        JTextField typeField = new JTextField();
        typeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        if (customPackageType != null && !customPackageType.isEmpty()) {
            typeField.setText(customPackageType);
        }
        
        panel.add(typeField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okBtn.setForeground(Color.WHITE);
        okBtn.setBackground(new Color(40, 167, 69));
        okBtn.setBorderPainted(false);
        okBtn.setFocusPainted(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        okBtn.addActionListener(e -> {
            String customType = typeField.getText().trim();
            if (customType.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a package type", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            customPackageType = customType;
            dialog.dispose();
            calculateEstimate();
        });
        
        cancelBtn.addActionListener(e -> {
            packageTypeCombo.setSelectedIndex(-1);
            dialog.dispose();
        });
        
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void initializeCombosWithPlaceholders() {
        Vector<String> stateVector = new Vector<>(stateList);
        fromStateCombo.setModel(new DefaultComboBoxModel<>(stateVector));
        toStateCombo.setModel(new DefaultComboBoxModel<>(stateVector));
        
        fromStateCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select State"));
        toStateCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select State"));
        
        fromStateCombo.setSelectedIndex(-1);
        toStateCombo.setSelectedIndex(-1);
        
        Vector<String> emptyVector = new Vector<>();
        fromCityCombo.setModel(new DefaultComboBoxModel<>(emptyVector));
        toCityCombo.setModel(new DefaultComboBoxModel<>(emptyVector));
        fromCityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
        toCityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
        
        fromPostcodeField.setText("");
        toPostcodeField.setText("");
    }
    
    class ComboBoxPlaceholderRenderer extends DefaultListCellRenderer {
        private String placeholder;
        
        public ComboBoxPlaceholderRenderer(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value == null || (index == -1 && value == null)) {
                setText(placeholder);
                setForeground(new Color(108, 117, 125));
            } else {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String) {
                    setText((String) value);
                }
            }
            return this;
        }
    }

    private void updateSpeedDescription() {
        if (expressSpeedRadio.isSelected()) {
            speedDescriptionLabel.setText("Express: 1-2 business days delivery (Priority handling, 50% premium)");
            deliveryTimeLabel.setText("1-2 business days (Express)");
        } else {
            speedDescriptionLabel.setText("Standard: 3-5 business days delivery (Economy shipping)");
            deliveryTimeLabel.setText("3-5 business days (Standard)");
        }
    }

    private void toggleInsurancePanel() {
        insurancePanel.setVisible(insuranceCheckBox.isSelected());
        if (!insuranceCheckBox.isSelected()) {
            declaredValueField.setText("Enter declared value");
            declaredValueField.setForeground(new Color(108, 117, 125));
        }
        revalidate();
        repaint();
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
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel bankLabel = new JLabel("Issuing Bank:*");
        bankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(bankLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> bankCombo = new JComboBox<>(MALAYSIAN_BANKS);
        bankCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bankCombo.setSelectedIndex(-1);
        bankCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select Bank"));
        panel.add(bankCombo, gbc);
        
        addPaymentField(panel, "Card Number:*", "4111111111111111", gbc, y++);
        addPaymentField(panel, "Cardholder Name:*", "John Doe", gbc, y++);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel expiryLabel = new JLabel("Expiry Date:*");
        expiryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(expiryLabel, gbc);
        
        gbc.gridx = 1;
        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        expiryPanel.setOpaque(false);
        
        JComboBox<String> monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthCombo.setPreferredSize(new Dimension(65, 25));
        expiryPanel.add(monthCombo);
        
        expiryPanel.add(new JLabel("/"));
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[11];
        for (int i = 0; i <= 10; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        JComboBox<String> yearCombo = new JComboBox<>(years);
        yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearCombo.setPreferredSize(new Dimension(75, 25));
        expiryPanel.add(yearCombo);
        
        panel.add(expiryPanel, gbc);
        
        addPaymentField(panel, "CVV:*", "123", gbc, y++);
        
        return panel;
    }

    private JPanel createDebitCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        int y = 0;
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel bankLabel = new JLabel("Issuing Bank:*");
        bankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(bankLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> bankCombo = new JComboBox<>(MALAYSIAN_BANKS);
        bankCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bankCombo.setSelectedIndex(-1);
        bankCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select Bank"));
        panel.add(bankCombo, gbc);
        
        addPaymentField(panel, "Debit Card Number:*", "4111111111111111", gbc, y++);
        addPaymentField(panel, "Cardholder Name:*", "John Doe", gbc, y++);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel expiryLabel = new JLabel("Expiry Date:*");
        expiryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(expiryLabel, gbc);
        
        gbc.gridx = 1;
        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        expiryPanel.setOpaque(false);
        
        JComboBox<String> monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthCombo.setPreferredSize(new Dimension(65, 25));
        expiryPanel.add(monthCombo);
        
        expiryPanel.add(new JLabel("/"));
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[11];
        for (int i = 0; i <= 10; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        JComboBox<String> yearCombo = new JComboBox<>(years);
        yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearCombo.setPreferredSize(new Dimension(75, 25));
        expiryPanel.add(yearCombo);
        
        panel.add(expiryPanel, gbc);
        
        addPaymentField(panel, "CVV:*", "123", gbc, y++);
        
        return panel;
    }

    private JPanel createPayPalPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        addPaymentField(panel, "PayPal Email:*", "user@example.com", gbc, 0);
        
        JLabel noteLabel = new JLabel("You will be redirected to PayPal to complete payment");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
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
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel merchantLabel = new JLabel("Transfer to LogiXpress (Maybank: 1234567890123456)");
        merchantLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        merchantLabel.setForeground(new Color(0, 102, 204));
        panel.add(merchantLabel, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel userBankLabel = new JLabel("Your Bank:*");
        userBankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(userBankLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> userBankCombo = new JComboBox<>(MALAYSIAN_BANKS);
        userBankCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userBankCombo.setSelectedIndex(-1);
        userBankCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select Your Bank"));
        panel.add(userBankCombo, gbc);
        
        addPaymentField(panel, "Reference No:*", "e.g., ORD-XXXX", gbc, y++);
        
        JLabel noteLabel = new JLabel("Please use your Order ID as reference for faster verification");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        noteLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(noteLabel, gbc);
        
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
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel walletLabel = new JLabel(walletName + " Payment");
        walletLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(walletLabel, gbc);
        
        addPaymentField(panel, walletName + " Number:*", "0123456789", gbc, y++);
        
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
        if (selected == null) {
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

    // ========== PHONE VALIDATION METHODS ==========
    
    private boolean isValidMalaysianPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        String cleaned = phone.trim().replaceAll("\\s+", "").replaceAll("-", "");
        
        boolean valid = false;
        
        if (cleaned.matches("^01[0-46-9][0-9]{7,8}$")) {
            valid = true;
        } else if (cleaned.matches("^601[0-46-9][0-9]{7,8}$")) {
            valid = true;
        } else if (cleaned.matches("^\\+601[0-46-9][0-9]{7,8}$")) {
            valid = true;
        }
        
        return valid;
    }
    
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        
        String cleaned = phone.trim().replaceAll("\\s+", "").replaceAll("-", "");
        
        if (cleaned.startsWith("+60")) {
            cleaned = "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("60")) {
            cleaned = "0" + cleaned.substring(2);
        }
        
        if (cleaned.length() >= 10 && cleaned.length() <= 11 && cleaned.startsWith("01")) {
            return cleaned.substring(0, 3) + "-" + cleaned.substring(3);
        }
        
        return cleaned;
    }
    
    private boolean validateSenderInfo() {
        String name = senderNameField.getText().trim();
        if (name.isEmpty() || name.equals("Enter customer name")) {
            JOptionPane.showMessageDialog(this, "Please enter sender name", "Validation Error", JOptionPane.ERROR_MESSAGE);
            senderNameField.requestFocus();
            return false;
        }
        
        String phone = senderPhoneField.getText().trim();
        if (phone.isEmpty() || phone.equals("0123456789")) {
            JOptionPane.showMessageDialog(this, "Please enter sender phone number", "Validation Error", JOptionPane.ERROR_MESSAGE);
            senderPhoneField.requestFocus();
            return false;
        }
        
        if (!isValidMalaysianPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid Malaysian phone number\n\n" +
                "Valid formats:\n" +
                "• 0123456789\n" +
                "• 60123456789\n" +
                "• +60123456789\n\n" +
                "Mobile prefixes: 010, 011, 012, 013, 014, 015, 016, 017, 018, 019", 
                "Invalid Phone Number", 
                JOptionPane.ERROR_MESSAGE);
            senderPhoneField.requestFocus();
            return false;
        }
        
        String email = senderEmailField.getText().trim();
        if (email.isEmpty() || email.equals("customer@example.com")) {
            JOptionPane.showMessageDialog(this, "Please enter sender email", "Validation Error", JOptionPane.ERROR_MESSAGE);
            senderEmailField.requestFocus();
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address", "Validation Error", JOptionPane.ERROR_MESSAGE);
            senderEmailField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean validateRecipientPhone() {
        String phone = recipientPhoneField.getText().trim();
        
        if (phone.isEmpty() || phone.equals("0123456789")) {
            JOptionPane.showMessageDialog(this, 
                "Please enter recipient phone number", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            recipientPhoneField.requestFocus();
            return false;
        }
        
        if (!isValidMalaysianPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid Malaysian phone number\n\n" +
                "Valid formats:\n" +
                "• 0123456789\n" +
                "• 60123456789\n" +
                "• +60123456789\n\n" +
                "Mobile prefixes: 010, 011, 012, 013, 014, 015, 016, 017, 018, 019", 
                "Invalid Phone Number", 
                JOptionPane.ERROR_MESSAGE);
            recipientPhoneField.requestFocus();
            return false;
        }
        
        String formatted = formatPhoneNumber(phone);
        recipientPhoneField.setText(formatted);
        recipientPhoneField.setForeground(Color.BLACK);
        
        return true;
    }

    private void updateFromCities() {
        String selectedState = (String) fromStateCombo.getSelectedItem();
        if (selectedState != null && selectedState.length() > 0) {
            Map<String, String> cities = malaysiaData.get(selectedState);
            if (cities != null) {
                List<String> sortedCities = new ArrayList<>(cities.keySet());
                Collections.sort(sortedCities);
                Vector<String> cityList = new Vector<>(sortedCities);
                fromCityCombo.setModel(new DefaultComboBoxModel<>(cityList));
                fromCityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
                fromCityCombo.setSelectedIndex(-1);
                fromPostcodeField.setText("");
            }
        } else {
            Vector<String> emptyVector = new Vector<>();
            fromCityCombo.setModel(new DefaultComboBoxModel<>(emptyVector));
            fromCityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
            fromPostcodeField.setText("");
        }
        calculateEstimate();
    }

    private void updateToCities() {
        String selectedState = (String) toStateCombo.getSelectedItem();
        if (selectedState != null && selectedState.length() > 0) {
            Map<String, String> cities = malaysiaData.get(selectedState);
            if (cities != null) {
                List<String> sortedCities = new ArrayList<>(cities.keySet());
                Collections.sort(sortedCities);
                Vector<String> cityList = new Vector<>(sortedCities);
                toCityCombo.setModel(new DefaultComboBoxModel<>(cityList));
                toCityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
                toCityCombo.setSelectedIndex(-1);
                toPostcodeField.setText("");
            }
        } else {
            Vector<String> emptyVector = new Vector<>();
            toCityCombo.setModel(new DefaultComboBoxModel<>(emptyVector));
            toCityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
            toPostcodeField.setText("");
        }
        calculateEstimate();
    }

    private void updateFromPostcode() {
        String selectedState = (String) fromStateCombo.getSelectedItem();
        String selectedCity = (String) fromCityCombo.getSelectedItem();
        if (selectedState != null && selectedCity != null && selectedState.length() > 0 && selectedCity.length() > 0) {
            fromPostcodeField.setText(malaysiaData.get(selectedState).get(selectedCity));
        } else {
            fromPostcodeField.setText("");
        }
        calculateEstimate();
    }

    private void updateToPostcode() {
        String selectedState = (String) toStateCombo.getSelectedItem();
        String selectedCity = (String) toCityCombo.getSelectedItem();
        if (selectedState != null && selectedCity != null && selectedState.length() > 0 && selectedCity.length() > 0) {
            toPostcodeField.setText(malaysiaData.get(selectedState).get(selectedCity));
        } else {
            toPostcodeField.setText("");
        }
        calculateEstimate();
    }

    private double calculateDistance(String fromState, String toState) {
        if (fromState == null || toState == null || fromState.isEmpty() || toState.isEmpty()) return 0;
        if (fromState.equals(toState)) return 50.0;
        
        List<String> eastMalaysia = Arrays.asList("Sabah", "Sarawak", "Labuan");
        List<String> westMalaysia = Arrays.asList("Selangor", "Kuala Lumpur", "Penang", "Johor", "Perak", 
                "Negeri Sembilan", "Melaka", "Pahang", "Kedah", "Kelantan", "Terengganu", "Perlis");
        
        boolean fromEast = eastMalaysia.contains(fromState);
        boolean toEast = eastMalaysia.contains(toState);
        
        if (fromEast != toEast) return 1500.0;
        
        if (westMalaysia.contains(fromState) && westMalaysia.contains(toState)) {
            Map<String, Double> distances = new HashMap<>();
            distances.put("Penang", 350.0); distances.put("Johor", 320.0); distances.put("Perak", 200.0);
            distances.put("Negeri Sembilan", 70.0); distances.put("Melaka", 150.0); distances.put("Pahang", 250.0);
            distances.put("Kedah", 400.0); distances.put("Kelantan", 450.0); distances.put("Terengganu", 400.0);
            distances.put("Perlis", 450.0);
            if (distances.containsKey(fromState)) return distances.get(fromState);
            if (distances.containsKey(toState)) return distances.get(toState);
        }
        return 300.0;
    }

    private double getTypeMultiplier(String type) {
        if (type == null) return 1.0;
        if (type.equals("Other") || (customPackageType != null && !customPackageType.isEmpty())) return 1.0;
        switch(type) {
            case "Fragile Items": return 1.5;
            case "Electronics": return 1.3;
            case "Documents": return 0.8;
            case "Food": return 1.2;
            default: return 1.0;
        }
    }
    
    private String getSelectedPackageType() {
        String selected = (String) packageTypeCombo.getSelectedItem();
        if (selected == null) return null;
        if ("Other".equals(selected) && customPackageType != null && !customPackageType.isEmpty()) {
            return customPackageType;
        }
        return selected;
    }

    private double calculateShippingCost() {
        try {
            String weightText = weightField.getText().trim();
            if (weightText.isEmpty() || weightText.equals("Enter weight in kg")) return 0;
            
            double weight = Double.parseDouble(weightText);
            String packageType = getSelectedPackageType();
            String fromState = (String) fromStateCombo.getSelectedItem();
            String toState = (String) toStateCombo.getSelectedItem();
            
            if (fromState == null || toState == null || fromState.isEmpty() || toState.isEmpty() || packageType == null) {
                distanceLabel.setText("-- km");
                return 0;
            }
            
            double distance = calculateDistance(fromState, toState);
            distanceLabel.setText(distance > 0 ? String.format("%.0f km", distance) : "-- km");
            
            double total = BASE_RATE + (weight * RATE_PER_KG) + (distance * RATE_PER_KM);
            total *= getTypeMultiplier(packageType);
            
            if (expressSpeedRadio.isSelected()) {
                total *= 1.5;
            }
            
            return total;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double calculateInsuranceCost() {
        if (!insuranceCheckBox.isSelected()) return 0;
        try {
            String declaredStr = declaredValueField.getText().trim();
            if (declaredStr.isEmpty() || declaredStr.equals("Enter declared value")) return 0;
            double declaredValue = Double.parseDouble(declaredStr);
            return declaredValue <= 0 ? 0 : Math.max(declaredValue * INSURANCE_RATE, MIN_INSURANCE);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void calculateEstimate() {
        double shippingCost = calculateShippingCost();
        double insuranceCost = calculateInsuranceCost();
        shippingCostDisplayLabel.setText(String.format("RM %.2f", shippingCost));
        insuranceCostLabel.setText(String.format("RM %.2f", insuranceCost));
        estimatedCostLabel.setText(String.format("RM %.2f", shippingCost + insuranceCost));
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
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\|", -1);
                    if (parts.length > 0 && parts[0] != null && parts[0].startsWith(datePrefix)) {
                        try {
                            String seqStr = parts[0].substring(datePrefix.length()).replaceAll("[^0-9]", "");
                            if (!seqStr.isEmpty()) maxSequence = Math.max(maxSequence, Integer.parseInt(seqStr));
                        } catch (Exception e) {}
                    }
                }
            } catch (IOException e) {}
        }
        return datePrefix + String.format("%03d", maxSequence + 1);
    }

    // ========== FIXED: saveOrderToFile 方法 - 保留所有现有订单 ==========
    private void saveOrderToFile(SenderOrder order) {
        if (order == null) {
            System.err.println("Cannot save null order");
            return;
        }
        
        try {
            File file = new File(ORDERS_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            List<String> existingLines = new ArrayList<>();
            boolean orderExists = false;
            int orderLineIndex = -1;
            
            // 读取现有文件中的所有行
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    int lineIndex = 0;
                    while ((line = br.readLine()) != null) {
                        existingLines.add(line);
                        // 检查订单是否已存在（跳过注释行）
                        if (!line.startsWith("#") && line.contains(order.getId())) {
                            orderExists = true;
                            orderLineIndex = lineIndex;
                            System.out.println("Order already exists at line " + lineIndex + ", will update: " + order.getId());
                        }
                        lineIndex++;
                    }
                } catch (IOException e) {
                    System.err.println("Error reading existing orders: " + e.getMessage());
                }
            }
            
            System.out.println("Existing lines count: " + existingLines.size());
            System.out.println("Order exists: " + orderExists);
            
            // 写入文件
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // 写入 header（如果文件是新的或没有header）
                boolean hasHeader = false;
                for (String line : existingLines) {
                    if (line.startsWith("#")) {
                        hasHeader = true;
                        break;
                    }
                }
                
                if (!hasHeader || existingLines.isEmpty()) {
                    writer.println("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate");
                }
                
                // 写入所有现有的行，但排除要更新的订单（如果存在）
                for (int i = 0; i < existingLines.size(); i++) {
                    String line = existingLines.get(i);
                    // 保留注释行
                    if (line.startsWith("#")) {
                        writer.println(line);
                    } 
                    // 如果不是当前订单，保留
                    else if (!orderExists || i != orderLineIndex) {
                        writer.println(line);
                    }
                    // 如果是当前订单的行，跳过（稍后写入新版本）
                    else {
                        System.out.println("Skipping old version of order: " + order.getId());
                    }
                }
                
                // 写入新订单
                writer.println(buildOrderLine(order));
                writer.flush();
            }
            
            System.out.println("Order saved successfully: " + order.getId());
            
        } catch (IOException e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error saving order: " + e.getMessage() + "\n\nPlease check file permissions.",
                "File Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // CreateOrderPanel.java - buildOrderLine 方法
private String buildOrderLine(SenderOrder order) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    String currentDateTime = sdf.format(new Date());
    String estimatedDeliveryDate = calculateEstimatedDeliveryDate();
    String cleanNotes = order.getNotes() != null ? order.getNotes().replace("|", ";").replace("\n", " ").replace("\r", " ") : "";
    
    // 字段索引 20 对应 outForDeliveryTime，设置为空字符串
    // 字段索引 21 对应 deliveryTime，设置为空字符串
    return String.join("|",
        safeString(order.getId()),           // 0
        safeString(order.getCustomerName()), // 1
        safeString(order.getCustomerPhone()),// 2
        safeString(order.getCustomerEmail()),// 3
        safeString(order.getCustomerAddress()),// 4
        safeString(order.getRecipientName()), // 5
        safeString(order.getRecipientPhone()),// 6
        safeString(order.getRecipientAddress()),// 7
        "Pending",                           // 8 - status
        currentDateTime,                     // 9 - orderDate
        estimatedDeliveryDate,               // 10 - estimatedDelivery
        "",                                  // 11 - actualDelivery (空)
        "",                                  // 12 - driverId (空)
        "",                                  // 13 - vehicleId (空)
        String.valueOf(order.getWeight()),   // 14 - weight
        safeString(order.getDimensions()),   // 15 - dimensions
        cleanNotes,                          // 16 - notes
        "",                                  // 17 - reason (空)
        "",                                  // 18 - pickupTime (空)
        "",                                  // 19 - inTransitTime (空)
        "",                                  // 20 - outForDeliveryTime (空) ← 关键
        "",                                  // 21 - deliveryTime (空)
        "0",                                 // 22 - distance (0)
        "0",                                 // 23 - fuelUsed (0)
        "",                                  // 24 - deliveryPhoto (空)
        "",                                  // 25 - recipientSignature (空)
        "false",                             // 26 - onTime (false)
        "Paid",                              // 27 - paymentStatus
        safeString(order.getPaymentMethod()),// 28 - paymentMethod
        safeString(order.getTransactionId()),// 29 - transactionId
        safeString(order.getPaymentDate() != null ? order.getPaymentDate() : currentDateTime) // 30 - paymentDate
    );
}
    
    private String calculateEstimatedDeliveryDate() {
        int days = standardSpeedRadio.isSelected() ? 3 : 1;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    private String safeString(String s) { 
        return s != null && !s.isEmpty() ? s : ""; 
    }

    private boolean validateAddressesNotSame() {
        String fromAddressLine = fromAddressField.getText().trim();
        if (fromAddressLine.equals("Enter street address")) fromAddressLine = "";
        
        String fromState = (String) fromStateCombo.getSelectedItem();
        String fromCity = (String) fromCityCombo.getSelectedItem();
        String fromPostcode = fromPostcodeField.getText().trim();
        
        String toAddressLine = toAddressField.getText().trim();
        if (toAddressLine.equals("Enter street address")) toAddressLine = "";
        
        String toState = (String) toStateCombo.getSelectedItem();
        String toCity = (String) toCityCombo.getSelectedItem();
        String toPostcode = toPostcodeField.getText().trim();
        
        String fromFullAddress = buildFullAddress(fromAddressLine, fromCity, fromState, fromPostcode);
        String toFullAddress = buildFullAddress(toAddressLine, toCity, toState, toPostcode);
        
        boolean isSame = fromFullAddress.equalsIgnoreCase(toFullAddress);
        
        if (isSame) {
            JOptionPane.showMessageDialog(this,
                "Sender and recipient addresses cannot be the same!\n\n" +
                "Please ensure that the pickup address and delivery address are different.\n" +
                "If you need to ship to the same address, please contact customer support.",
                "Invalid Address",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private String buildFullAddress(String addressLine, String city, String state, String postcode) {
        StringBuilder sb = new StringBuilder();
        if (addressLine != null && !addressLine.isEmpty()) {
            sb.append(addressLine.trim().toLowerCase());
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city.trim().toLowerCase());
        }
        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state.trim().toLowerCase());
        }
        if (postcode != null && !postcode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postcode.trim());
        }
        return sb.toString().trim();
    }

    // ========== CREATE ORDER METHOD ==========
    
    private void createOrder() {
        if (!validateForm()) return;

        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        if (paymentMethod == null) {
            JOptionPane.showMessageDialog(this, "Please select a payment method", "Payment Required", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double shippingCost = calculateShippingCost();
        double insuranceCost = calculateInsuranceCost();
        double totalCost = shippingCost + insuranceCost;
        
        if (totalCost <= 0) {
            JOptionPane.showMessageDialog(this, "Invalid order amount. Please check weight and addresses.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String orderId = generateCustomOrderId();
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            String transactionId = "TXN" + System.currentTimeMillis() + (orderId.length() > 3 ? orderId.substring(0, 3) : orderId);
            String paymentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            // Get sender info from editable fields
            String senderName = senderNameField.getText().trim();
            if (senderName.equals("Enter customer name")) senderName = "";
            
            String senderPhoneVal = senderPhoneField.getText().trim();
            if (senderPhoneVal.equals("0123456789")) senderPhoneVal = "";
            senderPhoneVal = formatPhoneNumber(senderPhoneVal);
            
            String senderEmailVal = senderEmailField.getText().trim();
            if (senderEmailVal.equals("customer@example.com")) senderEmailVal = "";
            
            String fromState = (String) fromStateCombo.getSelectedItem();
            String fromCity = (String) fromCityCombo.getSelectedItem();
            String fromPostcode = fromPostcodeField.getText();
            String fromAddressLine = fromAddressField.getText().trim();
            if (fromAddressLine.equals("Enter street address")) fromAddressLine = "";
            
            String toState = (String) toStateCombo.getSelectedItem();
            String toCity = (String) toCityCombo.getSelectedItem();
            String toPostcode = toPostcodeField.getText();
            String toAddressLine = toAddressField.getText().trim();
            if (toAddressLine.equals("Enter street address")) toAddressLine = "";
            
            // Build full addresses
            String fromAddress = buildAddressString(fromAddressLine, fromCity, fromState, fromPostcode);
            String toAddress = buildAddressString(toAddressLine, toCity, toState, toPostcode);
            
            String dimensions = buildDimensionsString();
            
            String weightText = weightField.getText().trim();
            if (weightText.equals("Enter weight in kg")) weightText = "0";
            double weight = Double.parseDouble(weightText);
            
            String recipientNameVal = recipientNameField.getText().trim();
            if (recipientNameVal.equals("Full name")) recipientNameVal = "";
            
            String recipientPhoneVal = recipientPhoneField.getText().trim();
            if (recipientPhoneVal.equals("0123456789")) recipientPhoneVal = "";
            recipientPhoneVal = formatPhoneNumber(recipientPhoneVal);
            
            // Validate required fields
            if (senderName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sender name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (senderPhoneVal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sender phone is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (senderEmailVal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sender email is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (recipientNameVal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Recipient name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (recipientPhoneVal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Recipient phone is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            System.out.println("Creating order with ID: " + orderId);
            
            // Create the order
            SenderOrder order = new SenderOrder(orderId, senderName, senderPhoneVal, senderEmailVal, fromAddress,
                recipientNameVal, recipientPhoneVal, toAddress, weight, dimensions);
            
            order.setOrderDate(currentDateTime);
            order.setPaymentStatus("Paid");
            order.setPaymentMethod(paymentMethod);
            order.setTransactionId(transactionId);
            order.setPaymentDate(paymentDate);
            
            String selectedSpeed = expressSpeedRadio.isSelected() ? "Express" : "Standard";
            order.setEstimatedDelivery(expressSpeedRadio.isSelected() ? "Express: 1-2 business days" : "Standard: 3-5 business days");
            
            double distance = calculateDistance(fromState, toState);
            String packageType = getSelectedPackageType();
            if (packageType == null) packageType = "Standard";
            
            StringBuilder notes = new StringBuilder();
            notes.append("Package Type: ").append(packageType);
            notes.append("; Shipping Speed: ").append(selectedSpeed);
            notes.append("; Distance: ").append(String.format("%.0f", distance)).append(" km");
            notes.append("; Rate per kg: RM ").append(RATE_PER_KG);
            notes.append("; Rate per km: RM ").append(RATE_PER_KM);
            notes.append("; Base Fee: RM ").append(BASE_RATE);
            notes.append("; Weight: ").append(weight).append(" kg");
            notes.append("; Weight Cost: RM ").append(String.format("%.2f", weight * RATE_PER_KG));
            notes.append("; Distance Cost: RM ").append(String.format("%.2f", distance * RATE_PER_KM));
            notes.append("; Shipping Cost: RM ").append(String.format("%.2f", shippingCost));
            notes.append("; Payment Method: ").append(paymentMethod);
            
            if (insuranceCheckBox.isSelected()) {
                String declaredVal = declaredValueField.getText().trim();
                if (declaredVal.equals("Enter declared value")) declaredVal = "";
                notes.append("; Insurance: YES (Declared Value: RM ").append(declaredVal);
                notes.append(", Insurance Cost: RM ").append(String.format("%.2f", insuranceCost)).append(")");
            } else {
                notes.append("; Insurance: NO");
            }
            notes.append("; Total Amount: RM ").append(String.format("%.2f", totalCost));
            
            String description = descriptionArea.getText().trim();
            if (description.equals("Describe the package contents...")) description = "";
            notes.append("; Description: ").append(description);
            order.setNotes(notes.toString());
            
            // Save the order - this now preserves existing orders
            saveOrderToFile(order);
            
            // Add to repository
            SenderOrderRepository.getInstance().addOrder(order);
            SenderOrderRepository.getInstance().refreshData();
            
            if (parentOrderManagement != null) {
                parentOrderManagement.refreshData();
            }
            
            clearForm();
            
            JOptionPane.showMessageDialog(this,
                "✅ Order Created & Payment Successful!\n\nOrder ID: " + orderId + "\nTotal: RM " + String.format("%.2f", totalCost),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JDialog) {
                window.dispose();
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating order: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private String buildAddressString(String addressLine, String city, String state, String postcode) {
        StringBuilder sb = new StringBuilder();
        if (addressLine != null && !addressLine.isEmpty()) {
            sb.append(addressLine);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state);
        }
        if (postcode != null && !postcode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postcode);
        }
        return sb.length() > 0 ? sb.toString() : "Address not specified";
    }
    
    private String buildDimensionsString() {
        String length = lengthField.getText().trim();
        String width = widthField.getText().trim();
        String height = heightField.getText().trim();
        
        if (length.equals("cm")) length = "";
        if (width.equals("cm")) width = "";
        if (height.equals("cm")) height = "";
        
        if (length.isEmpty() && width.isEmpty() && height.isEmpty()) {
            return "0x0x0";
        }
        
        return (length.isEmpty() ? "0" : length) + "x" + 
               (width.isEmpty() ? "0" : width) + "x" + 
               (height.isEmpty() ? "0" : height);
    }


    private boolean validateForm() {
        // Validate sender information first
        if (!validateSenderInfo()) {
            return false;
        }
        
        String fromAddress = fromAddressField.getText().trim();
        if (fromAddress.isEmpty() || fromAddress.equals("Enter street address")) {
            JOptionPane.showMessageDialog(this, "Please enter sender address line", "Validation Error", JOptionPane.ERROR_MESSAGE);
            fromAddressField.requestFocus();
            return false;
        }
        
        String fromState = (String) fromStateCombo.getSelectedItem();
        if (fromState == null || fromState.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select sender state", "Validation Error", JOptionPane.ERROR_MESSAGE);
            fromStateCombo.requestFocus();
            return false;
        }
        
        String fromCity = (String) fromCityCombo.getSelectedItem();
        if (fromCity == null || fromCity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select sender city", "Validation Error", JOptionPane.ERROR_MESSAGE);
            fromCityCombo.requestFocus();
            return false;
        }
        
        String toAddress = toAddressField.getText().trim();
        if (toAddress.isEmpty() || toAddress.equals("Enter street address")) {
            JOptionPane.showMessageDialog(this, "Please enter recipient address line", "Validation Error", JOptionPane.ERROR_MESSAGE);
            toAddressField.requestFocus();
            return false;
        }
        
        String toState = (String) toStateCombo.getSelectedItem();
        if (toState == null || toState.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select recipient state", "Validation Error", JOptionPane.ERROR_MESSAGE);
            toStateCombo.requestFocus();
            return false;
        }
        
        String toCity = (String) toCityCombo.getSelectedItem();
        if (toCity == null || toCity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select recipient city", "Validation Error", JOptionPane.ERROR_MESSAGE);
            toCityCombo.requestFocus();
            return false;
        }
        
        if (!validateAddressesNotSame()) {
            return false;
        }
        
        String recipientName = recipientNameField.getText().trim();
        if (recipientName.isEmpty() || recipientName.equals("Full name")) {
            JOptionPane.showMessageDialog(this, "Please enter recipient name", "Validation Error", JOptionPane.ERROR_MESSAGE);
            recipientNameField.requestFocus();
            return false;
        }
        
        if (!validateRecipientPhone()) {
            return false;
        }
        
        String packageType = (String) packageTypeCombo.getSelectedItem();
        if (packageType == null) {
            JOptionPane.showMessageDialog(this, "Please select a package type", "Validation Error", JOptionPane.ERROR_MESSAGE);
            packageTypeCombo.requestFocus();
            return false;
        }
        
        String weightText = weightField.getText().trim();
        if (weightText.isEmpty() || weightText.equals("Enter weight in kg")) {
            JOptionPane.showMessageDialog(this, "Please enter weight", "Validation Error", JOptionPane.ERROR_MESSAGE);
            weightField.requestFocus();
            return false;
        }
        
        try {
            double weight = Double.parseDouble(weightText);
            if (weight <= 0) {
                JOptionPane.showMessageDialog(this, "Weight must be greater than 0 kg", "Validation Error", JOptionPane.ERROR_MESSAGE);
                weightField.requestFocus();
                return false;
            }
            if (weight > 100) {
                JOptionPane.showMessageDialog(this, "Weight cannot exceed 100 kg", "Validation Error", JOptionPane.ERROR_MESSAGE);
                weightField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid weight", "Validation Error", JOptionPane.ERROR_MESSAGE);
            weightField.requestFocus();
            return false;
        }
        
        String lengthText = lengthField.getText().trim();
        String widthText = widthField.getText().trim();
        String heightText = heightField.getText().trim();
        
        if (lengthText.isEmpty() || lengthText.equals("cm") || 
            widthText.isEmpty() || widthText.equals("cm") || 
            heightText.isEmpty() || heightText.equals("cm")) {
            JOptionPane.showMessageDialog(this, "Please enter all dimensions", "Validation Error", JOptionPane.ERROR_MESSAGE);
            lengthField.requestFocus();
            return false;
        }
        
        try {
            double length = Double.parseDouble(lengthText);
            double width = Double.parseDouble(widthText);
            double height = Double.parseDouble(heightText);
            
            if (length <= 0 || width <= 0 || height <= 0) {
                JOptionPane.showMessageDialog(this, "All dimensions must be greater than 0 cm", "Validation Error", JOptionPane.ERROR_MESSAGE);
                lengthField.requestFocus();
                return false;
            }
            if (length > 200 || width > 200 || height > 200) {
                JOptionPane.showMessageDialog(this, "Dimensions cannot exceed 200 cm", "Validation Error", JOptionPane.ERROR_MESSAGE);
                lengthField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for all dimensions", "Validation Error", JOptionPane.ERROR_MESSAGE);
            lengthField.requestFocus();
            return false;
        }
        
        String description = descriptionArea.getText().trim();
        if (description.isEmpty() || description.equals("Describe the package contents...")) {
            JOptionPane.showMessageDialog(this, "Please enter a package description", "Validation Error", JOptionPane.ERROR_MESSAGE);
            descriptionArea.requestFocus();
            return false;
        }
        
        if (insuranceCheckBox.isSelected()) {
            String declaredStr = declaredValueField.getText().trim();
            if (declaredStr.isEmpty() || declaredStr.equals("Enter declared value")) {
                JOptionPane.showMessageDialog(this, "Please enter declared value for insurance", "Validation Error", JOptionPane.ERROR_MESSAGE);
                declaredValueField.requestFocus();
                return false;
            }
            try {
                double declaredValue = Double.parseDouble(declaredStr);
                if (declaredValue <= 0) {
                    JOptionPane.showMessageDialog(this, "Declared value must be greater than 0", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    declaredValueField.requestFocus();
                    return false;
                }
                if (declaredValue > 50000) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Declared value is RM " + String.format("%.2f", declaredValue) + 
                        ". Insurance premium will be RM " + String.format("%.2f", Math.max(declaredValue * INSURANCE_RATE, MIN_INSURANCE)) +
                        ". Do you want to continue?",
                        "High Value Item",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for declared value", "Validation Error", JOptionPane.ERROR_MESSAGE);
                declaredValueField.requestFocus();
                return false;
            }
        }
        
        return true;
    }

    private void clearForm() {
        // Clear sender fields
        senderNameField.setText("Enter customer name");
        senderNameField.setForeground(new Color(108, 117, 125));
        senderPhoneField.setText("0123456789");
        senderPhoneField.setForeground(new Color(108, 117, 125));
        senderEmailField.setText("customer@example.com");
        senderEmailField.setForeground(new Color(108, 117, 125));
        
        // Clear address fields
        fromAddressField.setText("Enter street address");
        fromAddressField.setForeground(new Color(108, 117, 125));
        fromStateCombo.setSelectedIndex(-1);
        fromCityCombo.setSelectedIndex(-1);
        fromPostcodeField.setText("");
        toAddressField.setText("Enter street address");
        toAddressField.setForeground(new Color(108, 117, 125));
        toStateCombo.setSelectedIndex(-1);
        toCityCombo.setSelectedIndex(-1);
        toPostcodeField.setText("");
        
        // Clear recipient fields
        recipientNameField.setText("Full name");
        recipientNameField.setForeground(new Color(108, 117, 125));
        recipientPhoneField.setText("0123456789");
        recipientPhoneField.setForeground(new Color(108, 117, 125));
        
        // Clear package fields
        packageTypeCombo.setSelectedIndex(-1);
        customPackageType = null;
        weightField.setText("Enter weight in kg");
        weightField.setForeground(new Color(108, 117, 125));
        lengthField.setText("cm");
        lengthField.setForeground(new Color(108, 117, 125));
        widthField.setText("cm");
        widthField.setForeground(new Color(108, 117, 125));
        heightField.setText("cm");
        heightField.setForeground(new Color(108, 117, 125));
        descriptionArea.setText("Describe the package contents...");
        descriptionArea.setForeground(new Color(108, 117, 125));
        
        // Reset shipping speed
        standardSpeedRadio.setSelected(true);
        
        // Reset insurance
        insuranceCheckBox.setSelected(false);
        declaredValueField.setText("Enter declared value");
        declaredValueField.setForeground(new Color(108, 117, 125));
        insurancePanel.setVisible(false);
        
        // Reset payment
        paymentMethodCombo.setSelectedIndex(-1);
        paymentDetailsLayout.show(paymentDetailsPanel, "EMPTY");
        
        // Reset labels
        estimatedCostLabel.setText("RM 0.00");
        deliveryTimeLabel.setText("3-5 business days (Standard)");
        shippingCostDisplayLabel.setText("RM 0.00");
        insuranceCostLabel.setText("RM 0.00");
        distanceLabel.setText("-- km");
        
        // Update UI
        updateFromCities();
        updateToCities();
        calculateEstimate();
    }
}