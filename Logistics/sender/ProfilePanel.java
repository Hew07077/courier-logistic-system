package sender;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class ProfilePanel extends JPanel {
    private SenderDashboard dashboard;
    private JTabbedPane tabbedPane;
    private ProfileAvatar avatarComponent;
    private AddressPanel addressPanel;
    private NotificationPanel notificationPanel;
    private StatisticsPanel statisticsPanel;
    
    // Profile image storage
    private BufferedImage profileImage;
    private static final int AVATAR_SIZE = 120;
    
    // Malaysia location data
    private Map<String, Map<String, String>> malaysiaData;
    private List<String> stateList;
    
    public ProfilePanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
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
        
        Collections.sort(stateList);
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Create tabbed pane for better organization
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(Color.WHITE);
        
        // Initialize panels
        avatarComponent = new ProfileAvatar(AVATAR_SIZE);
        addressPanel = new AddressPanel(dashboard);
        notificationPanel = new NotificationPanel(dashboard);
        statisticsPanel = new StatisticsPanel(dashboard);
        
        // Add tabs
        tabbedPane.addTab("Personal Info", createPersonalInfoPanel());
        tabbedPane.addTab("Address Book", addressPanel);
        tabbedPane.addTab("Notifications", notificationPanel);
        tabbedPane.addTab("Statistics", statisticsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Select the Address Book tab in the profile panel
     * This is called from NewOrderPanel when no default address is found
     */
    public void selectAddressBookTab() {
        if (tabbedPane != null) {
            // Select the Address Book tab (index 1)
            tabbedPane.setSelectedIndex(1);
        }
    }
    
    //Refresh statistics when tab is selected
    public void refreshStatistics() {
        if (statisticsPanel != null) {
            statisticsPanel.refreshStats();
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)));
        
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Add profile completion badge
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JLabel completionLabel = new JLabel(getProfileCompletionText());
        completionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        completionLabel.setForeground(new Color(40, 167, 69));
        rightPanel.add(completionLabel);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createPersonalInfoPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Left side - Avatar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        
        JPanel avatarWrapper = new JPanel(new GridBagLayout());
        avatarWrapper.setBackground(Color.WHITE);
        avatarWrapper.add(avatarComponent);
        
        JButton uploadBtn = createStyledButton("Upload Photo", new Color(108, 117, 125), Color.WHITE);
        uploadBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        uploadBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        uploadBtn.setForeground(new Color(80, 80, 80));
        uploadBtn.setBackground(Color.WHITE);
        uploadBtn.addActionListener(e -> uploadProfileImage());
        avatarWrapper.add(uploadBtn);
        
        leftPanel.add(avatarWrapper, BorderLayout.NORTH);
        
        // Right side - Personal Information Form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.2;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        JTextField nameField = new JTextField(dashboard.getSenderName());
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        rightPanel.add(nameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        JTextField emailField = new JTextField(dashboard.getSenderEmail());
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        rightPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.2;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        JTextField phoneField = new JTextField(dashboard.getSenderPhone());
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        rightPanel.add(phoneField, gbc);
        
        // Email Verification Status
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel verifyLabel = new JLabel("Email Status:");
        verifyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightPanel.add(verifyLabel, gbc);
        
        gbc.gridx = 1;
        JPanel verifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        verifyPanel.setBackground(Color.WHITE);
        JLabel statusLabel = new JLabel("Unverified");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        JButton verifyBtn = createStyledButton("Verify Email", new Color(0, 123, 255), Color.WHITE);
        verifyBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        verifyBtn.setPreferredSize(new Dimension(100, 28));
        verifyBtn.addActionListener(e -> sendVerificationEmail());
        verifyPanel.add(statusLabel);
        verifyPanel.add(verifyBtn);
        rightPanel.add(verifyPanel, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveBtn = createStyledButton("Save Changes", new Color(40, 167, 69), Color.WHITE);
        JButton cancelBtn = createStyledButton("Cancel", new Color(108, 117, 125), Color.WHITE);
        
        saveBtn.addActionListener(e -> {
            if (validateInputs(nameField.getText(), emailField.getText())) {
                dashboard.setSenderName(nameField.getText().trim());
                dashboard.setSenderEmail(emailField.getText().trim());
                dashboard.setSenderPhone(phoneField.getText().trim());
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                refreshProfileDisplay();
            }
        });
        
        cancelBtn.addActionListener(e -> {
            nameField.setText(dashboard.getSenderName());
            emailField.setText(dashboard.getSenderEmail());
            phoneField.setText(dashboard.getSenderPhone());
        });
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        rightPanel.add(buttonPanel, gbc);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private void uploadProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedImage original = ImageIO.read(file);
                if (original != null) {
                    profileImage = resizeImage(original, AVATAR_SIZE, AVATAR_SIZE);
                    avatarComponent.setImage(profileImage);
                    JOptionPane.showMessageDialog(this, "Profile picture updated successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
            }
        }
    }
    
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        Image tmp = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    
    private void sendVerificationEmail() {
        JOptionPane.showMessageDialog(this, 
            "Verification email sent to " + dashboard.getSenderEmail() + 
            "\nPlease check your inbox and click the verification link.",
            "Email Sent", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private boolean validateInputs(String name, String email) {
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private String getProfileCompletionText() {
        int completed = 0;
        int total = 3;
        if (dashboard.getSenderName() != null && !dashboard.getSenderName().isEmpty()) completed++;
        if (dashboard.getSenderEmail() != null && !dashboard.getSenderEmail().isEmpty()) completed++;
        if (dashboard.getSenderPhone() != null && !dashboard.getSenderPhone().isEmpty()) completed++;
        int percentage = (completed * 100) / total;
        return "Profile " + percentage + "% Complete";
    }
    
    private void refreshProfileDisplay() {
        removeAll();
        initialize();
        revalidate();
        repaint();
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }
    
    /**
     * Get the default address from the address book
     * @return The default address, or null if none exists
     */
    public Address getDefaultAddress() {
        if (addressPanel != null) {
            return addressPanel.getDefaultAddress();
        }
        return null;
    }
    
    /**
     * Get the list of all addresses from the address book
     * @return List of addresses
     */
    public List<Address> getAllAddresses() {
        if (addressPanel != null) {
            return addressPanel.getAllAddresses();
        }
        return new ArrayList<>();
    }
    
    // Inner class for Profile Avatar
    class ProfileAvatar extends JPanel {
        private BufferedImage image;
        private int size;
        
        public ProfileAvatar(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setBackground(new Color(240, 240, 240));
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }
        
        public void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (image != null) {
                g2d.drawImage(image, 0, 0, size, size, null);
            } else {
                // Draw default avatar
                g2d.setColor(new Color(200, 200, 200));
                g2d.fillOval(0, 0, size, size);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
                String initial = dashboard.getSenderName() != null && !dashboard.getSenderName().isEmpty() 
                    ? dashboard.getSenderName().substring(0, 1).toUpperCase() 
                    : "?";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (size - fm.stringWidth(initial)) / 2;
                int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(initial, x, y);
            }
            g2d.dispose();
        }
    }
    
    // Inner class for Address data model (made public for access from NewOrderPanel)
    public class Address {
        private String name;
        private String street;
        private String city;
        private boolean isDefault;
        
        public Address(String name, String street, String city, boolean isDefault) {
            this.name = name;
            this.street = street;
            this.city = city;
            this.isDefault = isDefault;
        }
        
        public String getName() { return name; }
        public String getStreet() { return street; }
        public String getCity() { return city; }
        public boolean isDefault() { return isDefault; }
        public void setName(String name) { this.name = name; }
        public void setStreet(String street) { this.street = street; }
        public void setCity(String city) { this.city = city; }
        public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
        
        /**
         * Parse the full address into components
         * City format: "City, State Postcode"
         */
        public String getState() {
            if (city != null && city.contains(", ")) {
                String[] parts = city.split(", ");
                if (parts.length >= 2) {
                    String statePostcode = parts[1];
                    int lastSpace = statePostcode.lastIndexOf(" ");
                    if (lastSpace > 0) {
                        return statePostcode.substring(0, lastSpace);
                    }
                    return statePostcode;
                }
            }
            return "";
        }
        
        public String getCityName() {
            if (city != null && city.contains(", ")) {
                return city.split(", ")[0];
            }
            return city != null ? city : "";
        }
        
        public String getPostcode() {
            if (city != null && city.contains(", ")) {
                String[] parts = city.split(", ");
                if (parts.length >= 2) {
                    String statePostcode = parts[1];
                    int lastSpace = statePostcode.lastIndexOf(" ");
                    if (lastSpace > 0) {
                        return statePostcode.substring(lastSpace + 1);
                    }
                }
            }
            return "";
        }
        
        /**
         * Get full address string for display
         */
        public String getFullAddress() {
            return street + ", " + city;
        }
    }
    
    // Inner class for Address Management
    class AddressPanel extends JPanel {
        private SenderDashboard dashboard;
        private java.util.List<Address> addresses;
        private JPanel addressListPanel;
        
        public AddressPanel(SenderDashboard dashboard) {
            this.dashboard = dashboard;
            this.addresses = new ArrayList<>();
            loadAddressesFromStorage();
            initialize();
        }
        
        private void loadAddressesFromStorage() {
            // Load addresses from file storage
            try {
                java.io.File file = new java.io.File("sender_addresses_" + dashboard.getSenderEmail().hashCode() + ".txt");
                if (file.exists()) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split("\\|", -1);
                        if (parts.length >= 4) {
                            Address addr = new Address(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3]));
                            addresses.add(addr);
                        }
                    }
                    reader.close();
                }
            } catch (Exception e) {
                System.err.println("Error loading addresses: " + e.getMessage());
            }
        }
        
        private void saveAddressesToStorage() {
            try {
                java.io.File file = new java.io.File("sender_addresses_" + dashboard.getSenderEmail().hashCode() + ".txt");
                java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file));
                for (Address addr : addresses) {
                    writer.write(addr.getName() + "|" + addr.getStreet() + "|" + addr.getCity() + "|" + addr.isDefault());
                    writer.newLine();
                }
                writer.close();
            } catch (Exception e) {
                System.err.println("Error saving addresses: " + e.getMessage());
            }
        }
        
        // Get the default address
        public Address getDefaultAddress() {
            for (Address addr : addresses) {
                if (addr.isDefault()) {
                    return addr;
                }
            }
            return addresses.isEmpty() ? null : addresses.get(0);
        }
        
        // Get all addresses
        public java.util.List<Address> getAllAddresses() {
            return new ArrayList<>(addresses);
        }
        
        private void initialize() {
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(248, 249, 250));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // Header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(248, 249, 250));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            
            JLabel titleLabel = new JLabel("Saved Addresses");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            titleLabel.setForeground(new Color(33, 37, 41));
            
            JButton addBtn = createStyledButton("+ Add New Address", new Color(0, 123, 255), Color.WHITE);
            addBtn.addActionListener(e -> addNewAddress());
            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(addBtn, BorderLayout.EAST);
            
            add(headerPanel, BorderLayout.NORTH);
            
            // Address List Panel
            addressListPanel = new JPanel();
            addressListPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
            addressListPanel.setBackground(new Color(248, 249, 250));
            
            refreshAddressList();
            
            JScrollPane scrollPane = new JScrollPane(addressListPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getViewport().setBackground(new Color(248, 249, 250));
            add(scrollPane, BorderLayout.CENTER);
        }
        
        private void refreshAddressList() {
            addressListPanel.removeAll();
            
            if (addresses.isEmpty()) {
                JPanel emptyPanel = new JPanel(new GridBagLayout());
                emptyPanel.setBackground(new Color(248, 249, 250));
                emptyPanel.setPreferredSize(new Dimension(800, 300));
                
                JLabel emptyLabel = new JLabel("No addresses saved yet");
                emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                emptyLabel.setForeground(new Color(108, 117, 125));
                
                JLabel emptyHint = new JLabel("Click '+ Add New Address' to add your first address");
                emptyHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                emptyHint.setForeground(new Color(173, 181, 189));
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.insets = new Insets(10, 10, 10, 10);
                emptyPanel.add(emptyLabel, gbc);
                gbc.gridy = 1;
                emptyPanel.add(emptyHint, gbc);
                
                addressListPanel.add(emptyPanel);
            } else {
                java.util.List<Address> sorted = new ArrayList<>(addresses);
                sorted.sort((a1, a2) -> {
                    if (a1.isDefault()) return -1;
                    if (a2.isDefault()) return 1;
                    return a1.getName().compareToIgnoreCase(a2.getName());
                });
                
                for (Address addr : sorted) {
                    addressListPanel.add(createRichAddressCard(addr));
                }
            }
            
            addressListPanel.revalidate();
            addressListPanel.repaint();
        }
        
        private JPanel createRichAddressCard(Address address) {
            // Main card panel with shadow effect
            JPanel card = new JPanel(new BorderLayout(0, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Subtle shadow at bottom
                    g2d.setColor(new Color(0, 0, 0, 15));
                    g2d.fillRoundRect(2, getHeight() - 3, getWidth() - 4, 3, 8, 8);
                    g2d.dispose();
                }
            };
            
            card.setBackground(Color.WHITE);
            card.setPreferredSize(new Dimension(500, 260));
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Base border with rounded corners
            Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 234, 238), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            );
            
            // Apply default border
            if (address.isDefault()) {
                Border accentBorder = BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(40, 167, 69));
                card.setBorder(BorderFactory.createCompoundBorder(roundedBorder, accentBorder));
            } else {
                card.setBorder(roundedBorder);
            }
            
            // Hover effect
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    card.setBackground(new Color(255, 255, 255));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 123, 255, 120), 1),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }
                public void mouseExited(MouseEvent evt) {
                    card.setBackground(Color.WHITE);
                    if (address.isDefault()) {
                        Border accentBorder = BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(40, 167, 69));
                        card.setBorder(BorderFactory.createCompoundBorder(roundedBorder, accentBorder));
                    } else {
                        card.setBorder(roundedBorder);
                    }
                }
            });
            
            // Top section: Header with icon, name, and default badge
            JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
            headerPanel.setOpaque(false);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 10, 18));
            
            JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            leftHeader.setOpaque(false);
            
            JLabel iconLabel = new JLabel(address.isDefault() ? "🏠" : "📍");
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
            leftHeader.add(iconLabel);
            
            JLabel nameLabel = new JLabel(address.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setForeground(new Color(33, 37, 41));
            leftHeader.add(nameLabel);
            
            if (address.isDefault()) {
                JLabel defaultBadge = new JLabel(" DEFAULT ");
                defaultBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
                defaultBadge.setForeground(Color.WHITE);
                defaultBadge.setBackground(new Color(40, 167, 69));
                defaultBadge.setOpaque(true);
                defaultBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                leftHeader.add(defaultBadge);
            }
            
            headerPanel.add(leftHeader, BorderLayout.WEST);
            
            // Delivery info badge (estimated delivery time)
            JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightHeader.setOpaque(false);
            JLabel deliveryBadge = new JLabel("🚚 2-3 days");
            deliveryBadge.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            deliveryBadge.setForeground(new Color(108, 117, 125));
            deliveryBadge.setBackground(new Color(248, 249, 250));
            deliveryBadge.setOpaque(true);
            deliveryBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            rightHeader.add(deliveryBadge);
            headerPanel.add(rightHeader, BorderLayout.EAST);
            
            card.add(headerPanel, BorderLayout.NORTH);
            
            // Middle section: Address details with improved layout
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setOpaque(false);
            detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));
            
            // Street address with icon
            JPanel streetRow = new JPanel(new BorderLayout(8, 0));
            streetRow.setOpaque(false);
            streetRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel streetIcon = new JLabel("📍");
            streetIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            streetIcon.setForeground(new Color(108, 117, 125));
            JLabel streetLabel = new JLabel(address.getStreet());
            streetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            streetLabel.setForeground(new Color(73, 80, 87));
            streetRow.add(streetIcon, BorderLayout.WEST);
            streetRow.add(streetLabel, BorderLayout.CENTER);
            detailsPanel.add(streetRow);
            detailsPanel.add(Box.createVerticalStrut(8));
            
            // City/State/Postcode row
            JPanel cityRow = new JPanel(new BorderLayout(8, 0));
            cityRow.setOpaque(false);
            cityRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel cityIcon = new JLabel("🏢");
            cityIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cityIcon.setForeground(new Color(108, 117, 125));
            JLabel cityLabel = new JLabel(address.getCity());
            cityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cityLabel.setForeground(new Color(108, 117, 125));
            cityRow.add(cityIcon, BorderLayout.WEST);
            cityRow.add(cityLabel, BorderLayout.CENTER);
            detailsPanel.add(cityRow);
            
            card.add(detailsPanel, BorderLayout.CENTER);
            
            // Divider line
            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(230, 234, 238));
            card.add(separator, BorderLayout.SOUTH);
            
            // Bottom section: Action buttons with improved design
            JPanel actionPanel = new JPanel(new GridLayout(1, 3, 5, 0));
            actionPanel.setOpaque(false);
            actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 18, 12, 18));
            
            // Edit button
            JButton editBtn = createCompactIconButton("✎", "Edit", new Color(0, 123, 255));
            editBtn.addActionListener(e -> editAddress(address));
            actionPanel.add(editBtn);
            
            // Delete button
            JButton deleteBtn = createCompactIconButton("🗑", "Delete", new Color(220, 53, 69));
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(card, 
                    "Are you sure you want to delete \"" + address.getName() + "\"?", 
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    addresses.remove(address);
                    saveAddressesToStorage();
                    refreshAddressList();
                    JOptionPane.showMessageDialog(card, "Address deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            actionPanel.add(deleteBtn);
            
            // Set Default button (only if not already default)
            if (!address.isDefault()) {
                JButton defaultBtn = createCompactIconButton("✓", "Set Default", new Color(40, 167, 69));
                defaultBtn.addActionListener(e -> {
                    for (Address a : addresses) a.setDefault(false);
                    address.setDefault(true);
                    saveAddressesToStorage();
                    refreshAddressList();
                    JOptionPane.showMessageDialog(card, address.getName() + " is now your default address!", "Default Updated", JOptionPane.INFORMATION_MESSAGE);
                });
                actionPanel.add(defaultBtn);
            } else {
                // Placeholder to maintain layout
                JLabel placeholder = new JLabel("");
                placeholder.setPreferredSize(new Dimension(80, 32));
                actionPanel.add(placeholder);
            }
            
            card.add(actionPanel, BorderLayout.SOUTH);
            
            return card;
        }
        
        private JButton createCompactIconButton(String icon, String text, Color color) {
            JButton button = new JButton(icon + " " + text);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.setForeground(color);
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)
            ));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            
            // Hover effect
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    button.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 1),
                        BorderFactory.createEmptyBorder(5, 0, 5, 0)
                    ));
                }
                public void mouseExited(MouseEvent evt) {
                    button.setBackground(Color.WHITE);
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        BorderFactory.createEmptyBorder(6, 0, 6, 0)
                    ));
                }
            });
            
            return button;
        }
        
        private void addNewAddress() {
            showAddressDialog(null);
        }
        
        private void editAddress(Address address) {
            showAddressDialog(address);
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
        
        private void showAddressDialog(Address existingAddress) {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
                existingAddress == null ? "Add New Address" : "Edit Address", 
                Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(550, 520);
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);
            
            // Title
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel titleLabel = new JLabel(existingAddress == null ? "Add New Address" : "Edit Address");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setForeground(new Color(33, 37, 41));
            panel.add(titleLabel, gbc);
            
            gbc.gridy = 1;
            JSeparator separator = new JSeparator();
            panel.add(separator, gbc);
            
            gbc.gridwidth = 1;
            gbc.insets = new Insets(15, 10, 10, 10);
            
            // Address Label (Nickname)
            gbc.gridx = 0; gbc.gridy = 2;
            gbc.weightx = 0.3;
            JLabel nameLabel = new JLabel("Address Label*");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(nameLabel, gbc);
            
            gbc.gridx = 1; gbc.weightx = 0.7;
            JTextField nameField = new JTextField(existingAddress != null ? existingAddress.getName() : "");
            nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            panel.add(nameField, gbc);
            
            // Street Address
            gbc.gridx = 0; gbc.gridy = 3;
            JLabel streetLabel = new JLabel("Street Address*");
            streetLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(streetLabel, gbc);
            
            gbc.gridx = 1;
            JTextField streetField = new JTextField(existingAddress != null ? existingAddress.getStreet() : "");
            streetField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            streetField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            panel.add(streetField, gbc);
            
            // State Dropdown
            gbc.gridx = 0; gbc.gridy = 4;
            JLabel stateLabel = new JLabel("State*");
            stateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(stateLabel, gbc);
            
            gbc.gridx = 1;
            Vector<String> stateVector = new Vector<>(stateList);
            JComboBox<String> stateCombo = new JComboBox<>(stateVector);
            stateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            stateCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select State"));
            stateCombo.setSelectedIndex(-1);
            stateCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            panel.add(stateCombo, gbc);
            
            // City Dropdown
            gbc.gridx = 0; gbc.gridy = 5;
            JLabel cityLabel = new JLabel("City*");
            cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(cityLabel, gbc);
            
            gbc.gridx = 1;
            Vector<String> emptyVector = new Vector<>();
            JComboBox<String> cityCombo = new JComboBox<>(emptyVector);
            cityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
            cityCombo.setEnabled(false);
            cityCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            panel.add(cityCombo, gbc);
            
            // Postcode (Auto-filled, read-only)
            gbc.gridx = 0; gbc.gridy = 6;
            JLabel postcodeLabel = new JLabel("Postcode");
            postcodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(postcodeLabel, gbc);
            
            gbc.gridx = 1;
            JTextField postcodeField = new JTextField(10);
            postcodeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            postcodeField.setEditable(false);
            postcodeField.setBackground(new Color(248, 249, 250));
            postcodeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            panel.add(postcodeField, gbc);
            
            // State selection listener
            stateCombo.addActionListener(e -> {
                String selectedState = (String) stateCombo.getSelectedItem();
                if (selectedState != null && selectedState.length() > 0) {
                    Map<String, String> cities = malaysiaData.get(selectedState);
                    if (cities != null) {
                        List<String> sortedCities = new ArrayList<>(cities.keySet());
                        Collections.sort(sortedCities);
                        Vector<String> cityList = new Vector<>(sortedCities);
                        cityCombo.setModel(new DefaultComboBoxModel<>(cityList));
                        cityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
                        cityCombo.setEnabled(true);
                        cityCombo.setSelectedIndex(-1);
                        postcodeField.setText("");
                    }
                } else {
                    Vector<String> emptyCityVector = new Vector<>();
                    cityCombo.setModel(new DefaultComboBoxModel<>(emptyCityVector));
                    cityCombo.setEnabled(false);
                    postcodeField.setText("");
                }
            });
            
            // City selection listener
            cityCombo.addActionListener(e -> {
                String selectedState = (String) stateCombo.getSelectedItem();
                String selectedCity = (String) cityCombo.getSelectedItem();
                if (selectedState != null && selectedCity != null && selectedState.length() > 0 && selectedCity.length() > 0) {
                    String postcode = malaysiaData.get(selectedState).get(selectedCity);
                    postcodeField.setText(postcode);
                } else {
                    postcodeField.setText("");
                }
            });
            
            // If editing existing address, pre-fill the values
            if (existingAddress != null) {
                String fullAddress = existingAddress.getCity();
                String[] parts = fullAddress.split(", ");
                if (parts.length >= 2) {
                    String cityPart = parts[0];
                    String statePostcode = parts[1];
                    
                    int lastSpaceIndex = statePostcode.lastIndexOf(" ");
                    if (lastSpaceIndex > 0) {
                        String stateName = statePostcode.substring(0, lastSpaceIndex);
                        String postcode = statePostcode.substring(lastSpaceIndex + 1);
                        
                        stateCombo.setSelectedItem(stateName);
                        
                        Map<String, String> cities = malaysiaData.get(stateName);
                        if (cities != null) {
                            List<String> sortedCities = new ArrayList<>(cities.keySet());
                            Collections.sort(sortedCities);
                            Vector<String> cityList = new Vector<>(sortedCities);
                            cityCombo.setModel(new DefaultComboBoxModel<>(cityList));
                            cityCombo.setRenderer(new ComboBoxPlaceholderRenderer("Select City"));
                            cityCombo.setEnabled(true);
                            cityCombo.setSelectedItem(cityPart);
                            postcodeField.setText(postcode);
                        }
                    }
                }
            }
            
            // Default checkbox
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(20, 10, 10, 10);
            JCheckBox defaultCheck = new JCheckBox("Set as default address");
            defaultCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            defaultCheck.setSelected(existingAddress != null && existingAddress.isDefault());
            panel.add(defaultCheck, gbc);
            
            // Buttons
            gbc.gridy = 8;
            gbc.insets = new Insets(25, 10, 10, 10);
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonPanel.setBackground(Color.WHITE);
            
            JButton saveBtn = createStyledButton("Save Address", new Color(40, 167, 69), Color.WHITE);
            JButton cancelBtn = createStyledButton("Cancel", new Color(108, 117, 125), Color.WHITE);
            
            saveBtn.addActionListener(e -> {
                String name = nameField.getText().trim();
                String street = streetField.getText().trim();
                String selectedState = (String) stateCombo.getSelectedItem();
                String selectedCity = (String) cityCombo.getSelectedItem();
                String postcode = postcodeField.getText().trim();
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter an address label!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    nameField.requestFocus();
                    return;
                }
                if (street.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter street address!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    streetField.requestFocus();
                    return;
                }
                if (selectedState == null || selectedState.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select a state!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    stateCombo.requestFocus();
                    return;
                }
                if (selectedCity == null || selectedCity.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select a city!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    cityCombo.requestFocus();
                    return;
                }
                
                String fullCity = selectedCity + ", " + selectedState + " " + postcode;
                
                if (existingAddress == null) {
                    Address newAddress = new Address(name, street, fullCity, defaultCheck.isSelected());
                    if (defaultCheck.isSelected()) {
                        for (Address a : addresses) a.setDefault(false);
                    }
                    addresses.add(newAddress);
                    JOptionPane.showMessageDialog(dialog, "Address added successfully!");
                } else {
                    existingAddress.setName(name);
                    existingAddress.setStreet(street);
                    existingAddress.setCity(fullCity);
                    if (defaultCheck.isSelected()) {
                        for (Address a : addresses) a.setDefault(false);
                        existingAddress.setDefault(true);
                    } else {
                        existingAddress.setDefault(false);
                    }
                    JOptionPane.showMessageDialog(dialog, "Address updated successfully!");
                }
                
                saveAddressesToStorage();
                refreshAddressList();
                dialog.dispose();
            });
            
            cancelBtn.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);
            panel.add(buttonPanel, gbc);
            
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setBorder(null);
            dialog.add(scrollPane);
            dialog.setVisible(true);
        }
    }
    
    // Inner class for Notification Preferences
    class NotificationPanel extends JPanel {
        @SuppressWarnings("unused")
        private SenderDashboard dashboard;
        private Map<String, JCheckBox> preferences;
        
        public NotificationPanel(SenderDashboard dashboard) {
            this.dashboard = dashboard;
            this.preferences = new HashMap<>();
            initialize();
        }
        
        private void initialize() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);
            
            // Email Notifications Section
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel emailSection = new JLabel("Email Notifications");
            emailSection.setFont(new Font("Segoe UI", Font.BOLD, 14));
            emailSection.setForeground(new Color(0, 123, 255));
            mainPanel.add(emailSection, gbc);
            
            String[] emailPrefs = {
                "Order Confirmation Emails",
                "Shipping Updates",
                "Promotional Offers",
                "Newsletter"
            };
            
            gbc.gridwidth = 1;
            for (int i = 0; i < emailPrefs.length; i++) {
                gbc.gridx = 0; gbc.gridy = i + 1;
                gbc.weightx = 0.4;
                mainPanel.add(new JLabel(emailPrefs[i] + ":"), gbc);
                gbc.gridx = 1; gbc.weightx = 0.6;
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(true);
                preferences.put(emailPrefs[i], checkBox);
                mainPanel.add(checkBox, gbc);
            }
            
            // SMS Notifications Section
            gbc.gridx = 0; gbc.gridy = 6;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(20, 10, 10, 10);
            JLabel smsSection = new JLabel("SMS Notifications");
            smsSection.setFont(new Font("Segoe UI", Font.BOLD, 14));
            smsSection.setForeground(new Color(0, 123, 255));
            mainPanel.add(smsSection, gbc);
            
            String[] smsPrefs = {
                "Order Status Updates",
                "Delivery Alerts"
            };
            
            gbc.gridwidth = 1;
            for (int i = 0; i < smsPrefs.length; i++) {
                gbc.gridx = 0; gbc.gridy = i + 7;
                gbc.weightx = 0.4;
                mainPanel.add(new JLabel(smsPrefs[i] + ":"), gbc);
                gbc.gridx = 1; gbc.weightx = 0.6;
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(true);
                preferences.put(smsPrefs[i], checkBox);
                mainPanel.add(checkBox, gbc);
            }
            
            // Save Button
            gbc.gridx = 0; gbc.gridy = 10;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(30, 10, 10, 10);
            JButton saveBtn = createStyledButton("Save Preferences", new Color(40, 167, 69), Color.WHITE);
            saveBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "Notification preferences saved!");
            });
            mainPanel.add(saveBtn, gbc);
            
            add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        }
    }
    
    // Inner class for REAL Statistics - Calculates from actual order data
    class StatisticsPanel extends JPanel {
        private SenderDashboard dashboard;
        private JPanel statsGridPanel;
        private javax.swing.Timer refreshTimer;
        
        public StatisticsPanel(SenderDashboard dashboard) {
            this.dashboard = dashboard;
            initialize();
            startAutoRefresh();
        }
        
        private void initialize() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            statsGridPanel = new JPanel(new GridLayout(2, 2, 15, 15));
            statsGridPanel.setBackground(Color.WHITE);
            
            refreshStats();
            
            add(statsGridPanel, BorderLayout.CENTER);
        }
        
        /**
         * Refresh all statistics with real data from orders
         */
        public void refreshStats() {
            // Get fresh data from repository
            SenderOrderRepository.getInstance().refreshData();
            String userEmail = dashboard.getSenderEmail();
            List<SenderOrder> userOrders = SenderOrderRepository.getInstance().getOrdersByEmail(userEmail);
            
            // Calculate real statistics
            int totalOrders = userOrders.size();
            int deliveredOrders = 0;
            @SuppressWarnings("unused")
            int pendingOrders = 0;
            @SuppressWarnings("unused")
            int inTransitOrders = 0;
            double totalSpent = 0.0;
            
            for (SenderOrder order : userOrders) {
                // Count by status
                String status = order.getStatus();
                if ("Delivered".equals(status)) {
                    deliveredOrders++;
                } else if ("Pending".equals(status)) {
                    pendingOrders++;
                } else if ("In Transit".equals(status) || "Out for Delivery".equals(status)) {
                    inTransitOrders++;
                }
                
                // Calculate total spent (only from delivered orders or all? Usually total spent includes all paid orders)
                double orderCost = extractTotalCostFromOrder(order);
                totalSpent += orderCost;
            }
            
            int activeOrders = totalOrders - deliveredOrders;
            double avgOrderValue = totalOrders > 0 ? totalSpent / totalOrders : 0;
            
            // Get earliest order date for "Member Since"
            String memberSince = getMemberSinceDate(userOrders);
            
            // Update the grid
            statsGridPanel.removeAll();
            
            statsGridPanel.add(createStatCard("Total Orders", String.valueOf(totalOrders), 
                "Lifetime orders", new Color(0, 123, 255), ""));
            
            statsGridPanel.add(createStatCard("Delivered", String.valueOf(deliveredOrders),
                "Completed deliveries", new Color(40, 167, 69), ""));
            
            statsGridPanel.add(createStatCard("Active Orders", String.valueOf(activeOrders),
                "Pending & In Transit", new Color(255, 193, 7), ""));
            
            statsGridPanel.add(createStatCard("Total Spent", "RM " + String.format("%.2f", totalSpent),
                "Lifetime spending", new Color(111, 66, 193), ""));
            
            statsGridPanel.add(createStatCard("Average Order", "RM " + String.format("%.2f", avgOrderValue),
                "Per order average", new Color(23, 162, 184), ""));
            
            statsGridPanel.add(createStatCard("Member Since", memberSince,
                "Customer since", new Color(253, 126, 20), ""));
        }
        
        private double extractTotalCostFromOrder(SenderOrder order) {
            // First try from notes field
            String notes = order.getNotes();
            if (notes != null && !notes.isEmpty()) {
                // Look for Total Amount
                if (notes.contains("Total Amount: RM")) {
                    try {
                        int start = notes.indexOf("Total Amount: RM") + 15;
                        int end = notes.indexOf(";", start);
                        if (end == -1) end = notes.indexOf("|", start);
                        if (end == -1) end = notes.length();
                        String costStr = notes.substring(start, end).trim();
                        costStr = costStr.replaceAll("[^0-9.]", "");
                        if (!costStr.isEmpty()) {
                            return Double.parseDouble(costStr);
                        }
                    } catch (Exception e) {}
                }
                
                // Look for Total Cost
                if (notes.contains("Total Cost: RM")) {
                    try {
                        int start = notes.indexOf("Total Cost: RM") + 14;
                        int end = notes.indexOf(";", start);
                        if (end == -1) end = notes.indexOf("|", start);
                        if (end == -1) end = notes.length();
                        String costStr = notes.substring(start, end).trim();
                        costStr = costStr.replaceAll("[^0-9.]", "");
                        if (!costStr.isEmpty()) {
                            return Double.parseDouble(costStr);
                        }
                    } catch (Exception e) {}
                }
            }
            
            // Fallback to estimated cost from SenderOrder
            return order.getEstimatedCost();
        }
        
        private String getMemberSinceDate(List<SenderOrder> orders) {
            if (orders == null || orders.isEmpty()) {
                return "2024";
            }
            
            // Find earliest order date
            String earliestDate = null;
            for (SenderOrder order : orders) {
                String orderDate = order.getOrderDate();
                if (orderDate != null && !orderDate.isEmpty()) {
                    if (earliestDate == null || orderDate.compareTo(earliestDate) < 0) {
                        earliestDate = orderDate;
                    }
                }
            }
            
            if (earliestDate != null && earliestDate.length() >= 10) {
                // Extract year from YYYY-MM-DD format
                if (earliestDate.contains("-")) {
                    String year = earliestDate.substring(0, 4);
                    return year;
                }
                return earliestDate.substring(0, 4);
            }
            
            return "2024";
        }
        
        private JPanel createStatCard(String title, String value, String subtitle, Color color, String icon) {
            JPanel card = new JPanel(new BorderLayout(10, 5));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(Color.WHITE);
            
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            topPanel.add(iconLabel, BorderLayout.WEST);
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLabel.setForeground(new Color(108, 117, 125));
            topPanel.add(titleLabel, BorderLayout.CENTER);
            
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            valueLabel.setForeground(color);
            
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            subtitleLabel.setForeground(new Color(108, 117, 125));
            
            JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 5));
            textPanel.setBackground(Color.WHITE);
            textPanel.add(topPanel);
            textPanel.add(valueLabel);
            textPanel.add(subtitleLabel);
            
            card.add(textPanel, BorderLayout.CENTER);
            
            return card;
        }
        
        private void startAutoRefresh() {
            // Refresh statistics every 30 seconds
            refreshTimer = new javax.swing.Timer(30000, e -> {
                SwingUtilities.invokeLater(() -> refreshStats());
            });
            refreshTimer.start();
        }
        
        public void stopAutoRefresh() {
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
        }
    }
}