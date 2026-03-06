package sender;

import logistics.orders.Order;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class HomePanel extends JPanel {
    private SenderDashboard dashboard;
    private JPanel statsPanel;
    private JPanel recentOrdersPanel;
    
    // Modern color scheme
    private final Color GRADIENT_START = new Color(0, 123, 255);
    private final Color GRADIENT_END = new Color(0, 210, 255);
    private final Color CARD_SHADOW = new Color(0, 0, 0, 20);
    private final Color HOVER_COLOR = new Color(245, 247, 250);

    public HomePanel(SenderDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 20, 0);

        // Welcome Header with Gradient
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(createWelcomeHeader(), gbc);

        // Stats Panel
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 25, 0);
        add(createStatsPanel(), gbc);

        // Main Content Area (Two columns)
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 10);
        
        JPanel leftColumn = createQuickActionsPanel();
        leftColumn.setPreferredSize(new Dimension(400, 350));
        add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0, 0);
        JPanel rightColumn = createRecentOrdersPanel();
        rightColumn.setPreferredSize(new Dimension(400, 350));
        add(rightColumn, gbc);
    }

    private JPanel createWelcomeHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, w, 0, GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        headerPanel.setPreferredSize(new Dimension(getWidth(), 120));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Left side with welcome message
        JPanel welcomeWrapper = new JPanel(new GridLayout(2, 1, 5, 5));
        welcomeWrapper.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + dashboard.getSenderName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeWrapper.add(welcomeLabel);

        JLabel subLabel = new JLabel("Here's what's happening with your shipments today");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subLabel.setForeground(new Color(255, 255, 255, 230));
        welcomeWrapper.add(subLabel);

        headerPanel.add(welcomeWrapper, BorderLayout.WEST);

        // Right side with date
        JPanel datePanel = new JPanel();
        datePanel.setOpaque(false);
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));

        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        datePanel.add(dateLabel);

        JLabel timeLabel = new JLabel(new SimpleDateFormat("hh:mm a").format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        timeLabel.setForeground(new Color(255, 255, 255, 200));
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        datePanel.add(timeLabel);

        headerPanel.add(datePanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panel.add(createStatCard("Active Orders", 
            String.valueOf(dashboard.getActiveOrders()), new Color(0, 123, 255), 
            "Currently in progress"));
        panel.add(createStatCard("Delivered", 
            String.valueOf(dashboard.getDeliveredOrders()), new Color(40, 167, 69), 
            "Successfully delivered"));
        panel.add(createStatCard("Pending Payments", 
            String.valueOf(dashboard.getPendingPayments()), new Color(255, 193, 7), 
            "Awaiting payment"));
        panel.add(createStatCard("Total Spent", 
            "RM " + String.format("%.2f", dashboard.getTotalSpent()), new Color(111, 66, 193), 
            "Lifetime spending"));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Add shadow effect
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 30)),
                card.getBorder()
            )
        ));

        // Left side with title
        JPanel leftPanel = new JPanel(new BorderLayout(10, 5));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(150, 150, 150));
        leftPanel.add(subtitleLabel, BorderLayout.SOUTH);

        card.add(leftPanel, BorderLayout.WEST);

        // Right side with value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(valueLabel, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(250, 250, 250));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.repaint();
            }
        });

        return card;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Quick Actions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(33, 37, 41));
        titlePanel.add(title);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Action buttons grid
        JPanel actionsGrid = new JPanel(new GridLayout(4, 1, 0, 15));
        actionsGrid.setOpaque(false);
        actionsGrid.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        actionsGrid.add(createEnhancedActionButton("Create New Order", 
            "Start shipping now", "Get your package on its way", new Color(0, 123, 255), "NEW_ORDER"));
        actionsGrid.add(createEnhancedActionButton("Track Order", 
            "Enter tracking number", "Real-time package location", new Color(23, 162, 184), "TRACK"));
        actionsGrid.add(createEnhancedActionButton("Make Payment", 
            dashboard.getPendingPayments() + " pending payments", 
            "Clear outstanding balance", new Color(40, 167, 69), "PAYMENT"));
        actionsGrid.add(createEnhancedActionButton("Need Help?", 
            "Contact support", "24/7 customer service", new Color(108, 117, 125), "SUPPORT"));

        panel.add(actionsGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnhancedActionButton(String text, String subtitle, String tooltip, Color color, String targetCard) {
        JPanel panel = new JPanel(new BorderLayout(15, 5));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setToolTipText(tooltip);

        // Left side with text
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setOpaque(false);

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        textLabel.setForeground(color);
        leftPanel.add(textLabel);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(subtitleLabel);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right arrow
        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        arrowLabel.setForeground(color);
        arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.add(arrowLabel, BorderLayout.EAST);

        // Hover effect
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dashboard.showPanel(targetCard);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(240, 242, 245));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(new Color(248, 249, 250));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
        });

        return panel;
    }

    private JPanel createRecentOrdersPanel() {
        recentOrdersPanel = new JPanel(new BorderLayout());
        recentOrdersPanel.setBackground(Color.WHITE);
        recentOrdersPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Recent Orders");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(33, 37, 41));
        titlePanel.add(title);

        recentOrdersPanel.add(titlePanel, BorderLayout.NORTH);

        refreshRecentOrders();

        return recentOrdersPanel;
    }

    private void refreshRecentOrders() {
        JPanel ordersList = new JPanel();
        ordersList.setLayout(new BoxLayout(ordersList, BoxLayout.Y_AXIS));
        ordersList.setOpaque(false);
        ordersList.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // Get actual orders from FileDataManager for this user
        String userEmail = dashboard.getSenderEmail();
        List<Order> allOrders = FileDataManager.getInstance().getAllOrders();
        List<Order> userOrders = new ArrayList<>();
        
        // Filter orders for this user
        for (Order order : allOrders) {
            if (order.customerEmail != null && userEmail != null) {
                if (order.customerEmail.trim().equals(userEmail.trim())) {
                    userOrders.add(order);
                }
            }
        }
        
        // Check if this is demo sender
        boolean isDemoSender = "demo@sender.com".equals(userEmail);
        
        // Show only the 5 most recent orders
        if (!userOrders.isEmpty()) {
            int startIndex = Math.max(0, userOrders.size() - 5);
            for (int i = startIndex; i < userOrders.size(); i++) {
                Order order = userOrders.get(i);
                ordersList.add(createEnhancedOrderRow(
                    order.id,
                    extractCity(order.customerAddress) + " → " + extractCity(order.recipientAddress),
                    order.status,
                    order.orderDate,
                    getStatusColor(order.status),
                    false // not demo
                ));
                ordersList.add(Box.createVerticalStrut(10));
            }
        } else if (isDemoSender) {
            // Show demo orders ONLY for demo sender
            ordersList.add(createDemoOrderRow("DEMO-ORD-001", "Kuala Lumpur → Penang", "In Transit (DEMO)", "2024-01-15", new Color(0, 123, 255)));
            ordersList.add(Box.createVerticalStrut(10));
            ordersList.add(createDemoOrderRow("DEMO-ORD-002", "Johor Bahru → Melaka", "Delivered (DEMO)", "2024-01-14", new Color(40, 167, 69)));
            ordersList.add(Box.createVerticalStrut(10));
            ordersList.add(createDemoOrderRow("DEMO-ORD-003", "Ipoh → Kuala Lumpur", "Pending (DEMO)", "2024-01-13", new Color(255, 193, 7)));
            ordersList.add(Box.createVerticalStrut(10));
            ordersList.add(createDemoOrderRow("DEMO-ORD-004", "Kota Kinabalu → Sandakan", "Delayed (DEMO)", "2024-01-12", new Color(255, 87, 34)));
            ordersList.add(Box.createVerticalStrut(10));
            ordersList.add(createDemoOrderRow("DEMO-ORD-005", "Kuching → Sibu", "In Transit (DEMO)", "2024-01-11", new Color(0, 123, 255)));
            ordersList.add(Box.createVerticalStrut(10));
        } else {
            // Empty state for real users with no orders
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            
            JLabel emptyIcon = new JLabel("📦");
            emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(emptyIcon);
            
            JLabel emptyLabel = new JLabel("No orders yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            emptyLabel.setForeground(new Color(33, 37, 41));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(emptyLabel);
            
            JLabel emptySubLabel = new JLabel("Create your first order to get started");
            emptySubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptySubLabel.setForeground(new Color(108, 117, 125));
            emptySubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyPanel.add(emptySubLabel);
            
            emptyPanel.add(Box.createVerticalStrut(15));
            
            // Create Order Button
            JButton createOrderBtn = new JButton("+ Create New Order");
            createOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            createOrderBtn.setForeground(Color.WHITE);
            createOrderBtn.setBackground(new Color(0, 123, 255));
            createOrderBtn.setBorderPainted(false);
            createOrderBtn.setFocusPainted(false);
            createOrderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            createOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            createOrderBtn.setMaximumSize(new Dimension(200, 40));
            createOrderBtn.addActionListener(e -> dashboard.showPanel("NEW_ORDER"));
            
            // Hover effect for button
            createOrderBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    createOrderBtn.setBackground(new Color(0, 105, 217));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    createOrderBtn.setBackground(new Color(0, 123, 255));
                }
            });
            
            emptyPanel.add(createOrderBtn);
            ordersList.add(emptyPanel);
        }

        JScrollPane scrollPane = new JScrollPane(ordersList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Remove existing scroll pane if any
        Component[] components = recentOrdersPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                recentOrdersPanel.remove(comp);
            }
        }
        
        recentOrdersPanel.add(scrollPane, BorderLayout.CENTER);

        // View all button (only show if there are orders or for demo)
        boolean hasOrders = !userOrders.isEmpty() || isDemoSender;
        
        // Remove existing button panel if any
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0 && 
                ((JPanel) comp).getComponent(0) instanceof JButton) {
                recentOrdersPanel.remove(comp);
            }
        }
        
        if (hasOrders) {
            JButton viewAllBtn = new JButton("View All Orders");
            viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            viewAllBtn.setForeground(new Color(0, 123, 255));
            viewAllBtn.setBackground(Color.WHITE);
            viewAllBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
            ));
            viewAllBtn.setFocusPainted(false);
            viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewAllBtn.addActionListener(e -> dashboard.showPanel("MY_ORDERS"));

            // Hover effect
            viewAllBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    viewAllBtn.setBackground(new Color(0, 123, 255));
                    viewAllBtn.setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    viewAllBtn.setBackground(Color.WHITE);
                    viewAllBtn.setForeground(new Color(0, 123, 255));
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setOpaque(false);
            buttonPanel.add(viewAllBtn);
            
            recentOrdersPanel.add(buttonPanel, BorderLayout.SOUTH);
        }
        
        recentOrdersPanel.revalidate();
        recentOrdersPanel.repaint();
    }

    private String extractCity(String address) {
        if (address != null && address.contains(",")) {
            return address.substring(0, address.indexOf(",")).trim();
        }
        return address != null ? address : "N/A";
    }

    private Color getStatusColor(String status) {
        // Remove (DEMO) tag for color determination
        String cleanStatus = status.replace(" (DEMO)", "");
        switch(cleanStatus) {
            case "Delivered": return new Color(40, 167, 69);
            case "In Transit": return new Color(0, 123, 255);
            case "Pending": return new Color(255, 193, 7);
            case "Cancelled": return new Color(220, 53, 69);
            case "Delayed": return new Color(255, 87, 34);
            default: return new Color(108, 117, 125);
        }
    }

    private JPanel createEnhancedOrderRow(String orderId, String route, String status, String date, Color statusColor, boolean isDemo) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        // Left side with order info
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setOpaque(false);

        JLabel idLabel = new JLabel(orderId);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        idLabel.setForeground(new Color(33, 37, 41));
        leftPanel.add(idLabel);

        JLabel routeLabel = new JLabel(route);
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        routeLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(routeLabel);

        row.add(leftPanel, BorderLayout.WEST);

        // Right side with status and date
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        rightPanel.setOpaque(false);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(statusLabel);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(150, 150, 150));
        datePanel.add(dateLabel);
        
        rightPanel.add(datePanel);

        row.add(rightPanel, BorderLayout.EAST);

        // Hover effect
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
                dashboard.showPanel("MY_ORDERS");
            }
        });

        return row;
    }

    private JPanel createDemoOrderRow(String orderId, String route, String status, String date, Color statusColor) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        // Left side with order info
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        leftPanel.setOpaque(false);

        JLabel idLabel = new JLabel(orderId);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        idLabel.setForeground(new Color(33, 37, 41));
        leftPanel.add(idLabel);

        JLabel routeLabel = new JLabel(route);
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        routeLabel.setForeground(new Color(108, 117, 125));
        leftPanel.add(routeLabel);

        row.add(leftPanel, BorderLayout.WEST);

        // Right side with status and date
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        rightPanel.setOpaque(false);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(statusLabel);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        badgePanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(150, 150, 150));
        badgePanel.add(dateLabel);
        
        JLabel demoBadge = new JLabel("DEMO");
        demoBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        demoBadge.setForeground(Color.WHITE);
        demoBadge.setBackground(new Color(108, 117, 125));
        demoBadge.setOpaque(true);
        demoBadge.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        badgePanel.add(demoBadge);
        
        rightPanel.add(badgePanel);

        row.add(rightPanel, BorderLayout.EAST);

        // Hover effect
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
                JOptionPane.showMessageDialog(dashboard, 
                    "This is a demo order. Create your own order to get started!", 
                    "Demo Order", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return row;
    }

    public void refreshData() {
        removeAll();
        initialize();
        revalidate();
        repaint();
    }
}