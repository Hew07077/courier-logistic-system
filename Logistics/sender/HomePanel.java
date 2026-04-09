package sender;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomePanel extends JPanel {
    private SenderDashboard dashboard;
    private Timer clockTimer;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JScrollPane mainScrollPane;
    
    // Blue-based color scheme
    private final Color BLUE_PRIMARY = new Color(0, 123, 255);
    private final Color BLUE_DARK = new Color(0, 86, 179);
    private final Color BLUE_PALE = new Color(227, 242, 253);
    private final Color BLUE_ACCENT = new Color(25, 118, 210);
    private final Color BLUE_MEDIUM = new Color(66, 165, 245);
    private final Color BLUE_GRADIENT_START = new Color(13, 71, 161);
    private final Color BLUE_GRADIENT_END = new Color(66, 165, 245);
    private final Color BG_LIGHT = new Color(240, 242, 245);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);

    // Company statistics
    private final long TOTAL_DELIVERIES = 1250000;
    private final int ACTIVE_DRIVERS = 1250;
    private final double CUSTOMER_SATISFACTION = 98.5;
    private final int COVERAGE_CITIES = 85;

    public HomePanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initialize();
        startClock();
    }

    private void initialize() {
        setLayout(new BorderLayout(20, 20));
        add(createWelcomeHeader(), BorderLayout.NORTH);

        // Main content with GridBagLayout for flexible sections
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 20, 0);
        
        // Section 1: Our Story
        gbc.gridy = 0;
        mainContent.add(createOurStorySection(), gbc);
        
        // Section 2: Statistics Grid
        gbc.gridy = 1;
        mainContent.add(createStatisticsSection(), gbc);
        
        // Section 3: Why Choose LogiXpress
        gbc.gridy = 2;
        mainContent.add(createWhyChooseUsSection(), gbc);
        
        // Section 4: Quick Actions only
        gbc.gridy = 3;
        mainContent.add(createQuickActionsPanel(), gbc);
        
        mainScrollPane = new JScrollPane(mainContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.getViewport().setBackground(BG_LIGHT);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Scroll to top after panel is shown
        SwingUtilities.invokeLater(() -> {
            if (mainScrollPane != null && mainScrollPane.getVerticalScrollBar() != null) {
                mainScrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }
    
    // Method to scroll to top when panel is shown
    public void scrollToTop() {
        if (mainScrollPane != null && mainScrollPane.getVerticalScrollBar() != null) {
            SwingUtilities.invokeLater(() -> {
                mainScrollPane.getVerticalScrollBar().setValue(0);
            });
        }
    }

    private JPanel createWelcomeHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Blue gradient for header
                GradientPaint gradient = new GradientPaint(
                    0, 0, BLUE_GRADIENT_START,
                    getWidth(), 0, BLUE_GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        // Logo circle
        JPanel logoCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillOval(0, 0, 55, 55);
                g2d.dispose();
            }
        };
        logoCircle.setPreferredSize(new Dimension(55, 55));
        logoCircle.setLayout(new GridBagLayout());
        
        JLabel logoLabel = new JLabel("LX");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(BLUE_PRIMARY);
        logoCircle.add(logoLabel);
        leftPanel.add(logoCircle);

        JPanel titleWrapper = new JPanel(new GridLayout(2, 1, 2, 2));
        titleWrapper.setOpaque(false);

        JLabel titleLabel = new JLabel("Welcome back, " + dashboard.getSenderName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleWrapper.add(titleLabel);

        JLabel badgeLabel = new JLabel("PREMIUM SENDER ACCOUNT");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setForeground(new Color(255, 255, 255, 200));
        badgeLabel.setBackground(new Color(0, 0, 0, 30));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        titleWrapper.add(badgeLabel);

        leftPanel.add(titleWrapper);
        headerPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        timeLabel = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(timeLabel);

        dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(255, 255, 255, 200));
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(dateLabel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusPanel.setOpaque(false);
        
        // Green status circle
        JPanel statusCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SUCCESS_GREEN);
                g2d.fillOval(0, 0, 10, 10);
                g2d.dispose();
            }
        };
        statusCircle.setPreferredSize(new Dimension(10, 10));
        statusPanel.add(statusCircle);
        
        JLabel statusText = new JLabel("System Online");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusText.setForeground(new Color(255, 255, 255, 200));
        statusPanel.add(statusText);
        
        statusPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(statusPanel);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void startClock() {
        clockTimer = new Timer(1000, e -> {
            if (timeLabel != null && dateLabel != null) {
                Date now = new Date();
                timeLabel.setText(new SimpleDateFormat("HH:mm:ss").format(now));
                dateLabel.setText(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(now));
            }
        });
        clockTimer.start();
    }

    // ==================== OUR STORY SECTION ====================
    private JPanel createOurStorySection() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        // Left side - Text content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        // Title with color bar
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Our Story");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(BLUE_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Color bar under title
        JPanel colorBar = new JPanel();
        colorBar.setBackground(BLUE_PRIMARY);
        colorBar.setPreferredSize(new Dimension(60, 3));
        titlePanel.add(colorBar, BorderLayout.SOUTH);
        
        textPanel.add(titlePanel);
        textPanel.add(Box.createVerticalStrut(10));
        
        // Story text
        JTextArea storyText = new JTextArea();
        storyText.setText("Founded in 2015, LogiXpress began with a simple mission: to revolutionize last-mile delivery in Malaysia through technology and exceptional customer service. What started as a small team of 5 passionate individuals operating from a single warehouse in Shah Alam has now grown into Malaysia's fastest-growing logistics provider.\n\n" +
            "Today, we serve over 50,000+ satisfied customers across all 14 states and federal territories, handling more than 1.2 million successful deliveries annually. Our commitment to innovation has earned us the 'Best Logistics Tech Startup' award three years running.\n\n" +
            "At LogiXpress, we believe every package tells a story - whether it's a gift to a loved one, a critical business document, or an online purchase. We're honored to be part of your journey, and we're committed to delivering excellence, one package at a time.");
        storyText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        storyText.setForeground(TEXT_DARK);
        storyText.setEditable(false);
        storyText.setLineWrap(true);
        storyText.setWrapStyleWord(true);
        storyText.setBackground(CARD_BG);
        storyText.setAlignmentX(Component.LEFT_ALIGNMENT);
        storyText.setRows(8);
        
        textPanel.add(storyText);
        
        card.add(textPanel, BorderLayout.CENTER);
        
        // Right side - Timeline
        JPanel rightPanel = createTimelinePanel();
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createTimelinePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        
        // Milestone 1
        panel.add(createMilestoneItem("2015", "Company Founded", "Started operations in Shah Alam", BLUE_PRIMARY));
        panel.add(Box.createVerticalStrut(20));
        
        // Milestone 2
        panel.add(createMilestoneItem("2018", "First Expansion", "Opened hubs in Penang & Johor", BLUE_ACCENT));
        panel.add(Box.createVerticalStrut(20));
        
        // Milestone 3
        panel.add(createMilestoneItem("2021", "Tech Innovation", "Launched real-time tracking", BLUE_MEDIUM));
        panel.add(Box.createVerticalStrut(20));
        
        // Milestone 4
        panel.add(createMilestoneItem("2024", "Nationwide Coverage", "85 cities across Malaysia", BLUE_DARK));
        
        return panel;
    }
    
    private JPanel createMilestoneItem(String year, String title, String description, Color color) {
        JPanel item = new JPanel(new BorderLayout(12, 5));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(280, 70));
        
        // Year with colored background
        JPanel yearPanel = new JPanel(new BorderLayout());
        yearPanel.setBackground(color);
        yearPanel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        yearPanel.setPreferredSize(new Dimension(70, 35));
        
        JLabel yearLabel = new JLabel(year);
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        yearLabel.setForeground(Color.WHITE);
        yearPanel.add(yearLabel);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_DARK);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_GRAY);
        
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        item.add(yearPanel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        
        return item;
    }

    // ==================== STATISTICS SECTION ====================
    private JPanel createStatisticsSection() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        // Section Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("LogiXpress by the Numbers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(BLUE_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel colorBar = new JPanel();
        colorBar.setBackground(BLUE_PRIMARY);
        colorBar.setPreferredSize(new Dimension(60, 3));
        titlePanel.add(colorBar, BorderLayout.SOUTH);
        
        card.add(titlePanel, BorderLayout.NORTH);
        
        // Statistics Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        statsGrid.setOpaque(false);
        
        statsGrid.add(createStatNumberCard(formatNumber(TOTAL_DELIVERIES), "Successful Deliveries", "+25% vs last year", BLUE_PRIMARY));
        statsGrid.add(createStatNumberCard(String.valueOf(ACTIVE_DRIVERS), "Active Drivers", "Nationwide fleet", BLUE_ACCENT));
        statsGrid.add(createStatNumberCard(String.format("%.1f%%", CUSTOMER_SATISFACTION), "Customer Satisfaction", "Based on 50K+ reviews", BLUE_MEDIUM));
        statsGrid.add(createStatNumberCard(String.valueOf(COVERAGE_CITIES), "Cities Covered", "All 14 states", BLUE_DARK));
        
        card.add(statsGrid, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createStatNumberCard(String value, String label, String subtext, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BLUE_PALE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        
        // Colored top bar
        JPanel topBar = new JPanel();
        topBar.setBackground(color);
        topBar.setPreferredSize(new Dimension(50, 4));
        topBar.setMaximumSize(new Dimension(50, 4));
        topBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(topBar);
        
        card.add(Box.createVerticalStrut(15));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(valueLabel);
        
        card.add(Box.createVerticalStrut(8));
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelLabel.setForeground(TEXT_DARK);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(labelLabel);
        
        card.add(Box.createVerticalStrut(5));
        
        JLabel subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtextLabel.setForeground(TEXT_GRAY);
        subtextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtextLabel);
        
        return card;
    }
    
    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    // ==================== WHY CHOOSE US SECTION ====================
    private JPanel createWhyChooseUsSection() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        // Section Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Why Choose LogiXpress?");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(BLUE_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel colorBar = new JPanel();
        colorBar.setBackground(BLUE_PRIMARY);
        colorBar.setPreferredSize(new Dimension(60, 3));
        titlePanel.add(colorBar, BorderLayout.SOUTH);
        
        card.add(titlePanel, BorderLayout.NORTH);
        
        // Features Grid - 2x3 layout
        JPanel featuresGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        featuresGrid.setOpaque(false);
        
        featuresGrid.add(createFeatureCard("Fast Delivery", 
            "Express delivery within 1-2 business days. Real-time tracking available on all shipments.", BLUE_PRIMARY));
        featuresGrid.add(createFeatureCard("Secure Handling", 
            "Your packages are handled with care. Insurance coverage up to RM 50,000 available.", BLUE_ACCENT));
        featuresGrid.add(createFeatureCard("24/7 Support", 
            "Round-the-clock customer support via phone, email, and live chat.", BLUE_MEDIUM));
        featuresGrid.add(createFeatureCard("Real-Time Tracking", 
            "GPS-enabled tracking with SMS/Email notifications at every checkpoint.", BLUE_DARK));
        featuresGrid.add(createFeatureCard("Best Rates", 
            "Competitive pricing with volume discounts for businesses. No hidden fees.", BLUE_PRIMARY));
        featuresGrid.add(createFeatureCard("Nationwide Coverage", 
            "Serving all 85 major cities across 14 states and federal territories.", BLUE_ACCENT));
        
        card.add(featuresGrid, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createFeatureCard(String title, String description, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, color),
            BorderFactory.createEmptyBorder(18, 15, 18, 15)
        ));
        
        // Colored top accent bar
        JPanel topBar = new JPanel();
        topBar.setBackground(color);
        topBar.setPreferredSize(new Dimension(40, 4));
        topBar.setMaximumSize(new Dimension(40, 4));
        topBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(topBar);
        
        card.add(Box.createVerticalStrut(15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        card.add(Box.createVerticalStrut(12));
        
        // Colored divider line
        JPanel divider = new JPanel();
        divider.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        divider.setPreferredSize(new Dimension(30, 2));
        divider.setMaximumSize(new Dimension(30, 2));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(divider);
        
        card.add(Box.createVerticalStrut(10));
        
        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descArea.setForeground(TEXT_GRAY);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(Color.WHITE);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descArea.setColumns(20);
        descArea.setRows(3);
        
        card.add(descArea);
        
        return card;
    }

    // ==================== QUICK ACTIONS PANEL ====================  
    private JPanel createQuickActionsPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        // Section Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Quick Actions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(BLUE_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel colorBar = new JPanel();
        colorBar.setBackground(BLUE_PRIMARY);
        colorBar.setPreferredSize(new Dimension(60, 3));
        titlePanel.add(colorBar, BorderLayout.SOUTH);
        
        card.add(titlePanel, BorderLayout.NORTH);
        
        // Actions Grid - 3 cards in a row
        JPanel actionsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        actionsGrid.setOpaque(false);
        
        actionsGrid.add(createActionCard("Create New Order", 
            "Start shipping now", "NEW_ORDER", "Get instant shipping rates", BLUE_PRIMARY, "1"));
        actionsGrid.add(createActionCard("Track Order", 
            "Enter tracking number", "TRACK", "Real-time package tracking", BLUE_ACCENT, "2"));
        actionsGrid.add(createActionCard("Need Help?", 
            "Contact support 24/7", "SUPPORT", "Live chat & email support", BLUE_DARK, "3"));
        
        card.add(actionsGrid, BorderLayout.CENTER);
        
        // Delivery Progress Section
        JPanel progressPanel = createProgressPanel();
        card.add(progressPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createActionCard(String title, String subtitle, String targetCard, String hint, Color color, String number) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, color),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Colored circle with number
        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0, 0, 50, 50);
                g2d.dispose();
            }
        };
        circlePanel.setPreferredSize(new Dimension(50, 50));
        circlePanel.setMaximumSize(new Dimension(50, 50));
        circlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        numberLabel.setForeground(Color.WHITE);
        circlePanel.setLayout(new GridBagLayout());
        circlePanel.add(numberLabel);
        
        card.add(circlePanel);
        
        card.add(Box.createVerticalStrut(15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        card.add(Box.createVerticalStrut(5));
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        
        card.add(Box.createVerticalStrut(8));
        
        JPanel hintPanel = new JPanel();
        hintPanel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        hintPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        hintPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hintLabel.setForeground(color);
        hintPanel.add(hintLabel);
        
        card.add(hintPanel);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dashboard.showPanel(targetCard);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(BLUE_PALE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, color),
                    BorderFactory.createEmptyBorder(20, 15, 20, 15)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, color),
                    BorderFactory.createEmptyBorder(20, 15, 20, 15)
                ));
            }
        });
        
        return card;
    }
    
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        
        int totalOrders = dashboard.getActiveOrders() + dashboard.getDeliveredOrders();
        int deliveryProgress = totalOrders > 0 ? (dashboard.getDeliveredOrders() * 100) / totalOrders : 0;
        
        JLabel progressLabel = new JLabel("Your Delivery Progress");
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressLabel.setForeground(TEXT_DARK);
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(progressLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(deliveryProgress);
        progressBar.setStringPainted(true);
        progressBar.setForeground(SUCCESS_GREEN);
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setPreferredSize(new Dimension(400, 25));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(progressBar);
        
        panel.add(Box.createVerticalStrut(8));
        
        JLabel statsLabel = new JLabel(dashboard.getDeliveredOrders() + " delivered out of " + totalOrders + " total orders");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statsLabel.setForeground(TEXT_GRAY);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statsLabel);
        
        return panel;
    }

    public void refreshData() {
        // Refresh data from main system
        SenderOrderRepository.getInstance().refreshData();
        
        // Update dashboard stats
        String userEmail = dashboard.getSenderEmail();
        dashboard.setActiveOrders(SenderOrderRepository.getInstance().getActiveOrders(userEmail));
        dashboard.setDeliveredOrders(SenderOrderRepository.getInstance().getDeliveredOrders(userEmail));
        dashboard.setPendingPayments(SenderOrderRepository.getInstance().getPendingPayments(userEmail));
        dashboard.setTotalSpent(SenderOrderRepository.getInstance().getTotalSpent(userEmail));
        
        revalidate();
        repaint();
        
        // Scroll to top after refresh
        scrollToTop();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        // Scroll to top when panel is added/displayed
        scrollToTop();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (clockTimer != null) {
            clockTimer.stop();
        }
    }
    
    // Custom rounded border class
    class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
    }
}