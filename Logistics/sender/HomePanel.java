// HomePanel.java
package sender;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomePanel extends JPanel {
    private SenderDashboard dashboard;
    private Timer clockTimer;
    private JLabel timeLabel;
    private JLabel dateLabel;
    
    // Modern color scheme
    private final Color BLUE_PRIMARY = new Color(0, 123, 255);
    private final Color BG_LIGHT = new Color(250, 250, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color WARNING_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(220, 53, 69);
    private final Color PURPLE = new Color(111, 66, 193);

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

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 20, 0);

        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(createStatsPanel(), gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 10);
        
        JPanel leftColumn = createQuickActionsPanel();
        leftColumn.setPreferredSize(new Dimension(400, 350));
        centerPanel.add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0, 0);
        JPanel rightColumn = createRecentOrdersPanel();
        rightColumn.setPreferredSize(new Dimension(400, 350));
        centerPanel.add(rightColumn, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createWelcomeHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, Color.WHITE,
                    getWidth(), 0, new Color(230, 242, 255)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("LX");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setBackground(BLUE_PRIMARY);
        logoLabel.setOpaque(true);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setPreferredSize(new Dimension(45, 45));
        logoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        leftPanel.add(logoLabel);

        JPanel titleWrapper = new JPanel(new GridLayout(2, 1, 2, 2));
        titleWrapper.setOpaque(false);

        JLabel titleLabel = new JLabel("LogiXpress Sender Portal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_DARK);
        titleWrapper.add(titleLabel);

        JLabel badgeLabel = new JLabel("SENDER PORTAL v1.0.0");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setForeground(BLUE_PRIMARY);
        badgeLabel.setBackground(new Color(200, 225, 255));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        titleWrapper.add(badgeLabel);

        leftPanel.add(titleWrapper);
        headerPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        timeLabel = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timeLabel.setForeground(BLUE_PRIMARY);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(timeLabel);

        dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(TEXT_GRAY);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(dateLabel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusPanel.setOpaque(false);
        
        JLabel statusDot = new JLabel("");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusDot.setForeground(SUCCESS_GREEN);
        statusPanel.add(statusDot);
        
        JLabel statusText = new JLabel("Connected");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusText.setForeground(TEXT_GRAY);
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

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panel.add(createModernStatCard("Active Orders", 
            String.valueOf(dashboard.getActiveOrders()), BLUE_PRIMARY, 
            "Currently in progress"));
        panel.add(createModernStatCard("Delivered", 
            String.valueOf(dashboard.getDeliveredOrders()), SUCCESS_GREEN, 
            "Successfully delivered"));
        panel.add(createModernStatCard("Pending", 
            String.valueOf(dashboard.getPendingPayments()), WARNING_YELLOW, 
            "Awaiting payment"));
        panel.add(createModernStatCard("Total Spent", 
            "RM " + String.format("%.2f", dashboard.getTotalSpent()), PURPLE, 
            "Lifetime spending"));

        return panel;
    }

    private JPanel createModernStatCard(String title, String value, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        bottomPanel.add(titleLabel, BorderLayout.WEST);

        JLabel infoLabel = new JLabel("i");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_GRAY);
        infoLabel.setToolTipText(subtitle);
        bottomPanel.add(infoLabel, BorderLayout.EAST);

        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel actionsGrid = new JPanel(new GridLayout(3, 1, 0, 12));
        actionsGrid.setOpaque(false);

        actionsGrid.add(createModernActionButton("Create New Order", 
            "Start shipping now", BLUE_PRIMARY, "NEW_ORDER"));
        actionsGrid.add(createModernActionButton("Track Order", 
            "Enter tracking number", SUCCESS_GREEN, "TRACK"));
        actionsGrid.add(createModernActionButton("Need Help?", 
            "Contact support 24/7", TEXT_GRAY, "SUPPORT"));

        panel.add(actionsGrid, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createModernActionButton(String text, String subtitle, Color color, String targetCard) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        textLabel.setForeground(TEXT_DARK);
        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(textLabel);

        leftPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(subtitleLabel);

        panel.add(leftPanel, BorderLayout.CENTER);

        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrowLabel.setForeground(color);
        arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        panel.add(arrowLabel, BorderLayout.EAST);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dashboard.showPanel(targetCard);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(248, 249, 250));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(CARD_BG);
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
        });

        return panel;
    }

    private JPanel createRecentOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Recent Orders");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(title, BorderLayout.NORTH);

        refreshRecentOrders(panel);

        return panel;
    }

    private void refreshRecentOrders(JPanel parentPanel) {
        JPanel ordersList = new JPanel();
        ordersList.setLayout(new BoxLayout(ordersList, BoxLayout.Y_AXIS));
        ordersList.setOpaque(false);

        // Refresh data from main system
        SenderDataManager.getInstance().refreshData();
        
        String userEmail = dashboard.getSenderEmail();
        List<SenderOrder> userOrders = SenderDataManager.getInstance().getOrdersByEmail(userEmail);
        
        if (!userOrders.isEmpty()) {
            int startIndex = Math.max(0, userOrders.size() - 3);
            for (int i = startIndex; i < userOrders.size(); i++) {
                SenderOrder order = userOrders.get(i);
                ordersList.add(createModernOrderRow(order));
                if (i < userOrders.size() - 1) {
                    ordersList.add(Box.createVerticalStrut(8));
                }
            }
        } else {
            JLabel emptyLabel = new JLabel("No orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            ordersList.add(emptyLabel);
            
            ordersList.add(Box.createVerticalStrut(15));
            
            JLabel createText = new JLabel("Click 'Create New Order' to start");
            createText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            createText.setForeground(TEXT_GRAY);
            createText.setAlignmentX(Component.CENTER_ALIGNMENT);
            ordersList.add(createText);
        }

        JScrollPane scrollPane = new JScrollPane(ordersList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        Component[] components = parentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                parentPanel.remove(comp);
            }
        }
        
        parentPanel.add(scrollPane, BorderLayout.CENTER);

        if (!userOrders.isEmpty()) {
            JButton viewAllBtn = new JButton("View All Orders →");
            viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            viewAllBtn.setForeground(BLUE_PRIMARY);
            viewAllBtn.setBackground(CARD_BG);
            viewAllBtn.setBorderPainted(false);
            viewAllBtn.setFocusPainted(false);
            viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewAllBtn.setHorizontalAlignment(SwingConstants.RIGHT);
            viewAllBtn.addActionListener(e -> dashboard.showPanel("TRACK"));
            
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    parentPanel.remove(comp);
                }
            }
            
            parentPanel.add(viewAllBtn, BorderLayout.SOUTH);
        }
        
        parentPanel.revalidate();
        parentPanel.repaint();
    }

    private JPanel createModernOrderRow(SenderOrder order) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JLabel idLabel = new JLabel(order.getId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        idLabel.setForeground(TEXT_DARK);
        row.add(idLabel, BorderLayout.WEST);

        JLabel statusLabel = new JLabel(order.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(getStatusColor(order.getStatus()));
        row.add(statusLabel, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(248, 249, 250));
                row.setOpaque(true);
                row.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setOpaque(false);
                row.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                dashboard.showPanel("TRACK");
                SwingUtilities.invokeLater(() -> {
                    findAndSetTrackingNumber(dashboard, order.getId());
                });
            }
        });

        return row;
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

    private Color getStatusColor(String status) {
        switch(status) {
            case "Delivered": return SUCCESS_GREEN;
            case "In Transit": return BLUE_PRIMARY;
            case "Pending": return WARNING_YELLOW;
            case "Cancelled": return DANGER_RED;
            case "Delayed": return new Color(255, 87, 34);
            default: return TEXT_GRAY;
        }
    }

    public void refreshData() {
        // Refresh data from main system
        SenderDataManager.getInstance().refreshData();
        
        // Update dashboard stats
        String userEmail = dashboard.getSenderEmail();
        dashboard.setActiveOrders(SenderDataManager.getInstance().getActiveOrders(userEmail));
        dashboard.setDeliveredOrders(SenderDataManager.getInstance().getDeliveredOrders(userEmail));
        dashboard.setPendingPayments(SenderDataManager.getInstance().getPendingPayments(userEmail));
        dashboard.setTotalSpent(SenderDataManager.getInstance().getTotalSpent(userEmail));
        
        // Refresh UI
        removeAll();
        initialize();
        revalidate();
        repaint();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (clockTimer != null) {
            clockTimer.stop();
        }
    }
}