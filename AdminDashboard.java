import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard(String username) {
        setTitle("logiXpress - Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        /* ================= HEADER ================= */
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBackground(new Color(30, 136, 229)); // Blue

        JLabel companyLabel = new JLabel("  logiXpress");
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        companyLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Admin: " + username + "   ");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setForeground(Color.WHITE);

        headerPanel.add(companyLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        /* ================= SIDEBAR ================= */
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBackground(new Color(255, 152, 0)); // Orange
        sidebarPanel.setLayout(new GridLayout(6, 1, 0, 10));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnDashboard = createSidebarButton("Dashboard");
        JButton btnOrders = createSidebarButton("Orders");
        JButton btnWarehouse = createSidebarButton("Warehouse");
        JButton btnDelivery = createSidebarButton("Delivery");
        JButton btnReports = createSidebarButton("Reports");
        JButton btnLogout = createSidebarButton("Logout");

        btnLogout.addActionListener(e -> {
            dispose();
            new AdminLogin();
        });

        sidebarPanel.add(btnDashboard);
        sidebarPanel.add(btnOrders);
        sidebarPanel.add(btnWarehouse);
        sidebarPanel.add(btnDelivery);
        sidebarPanel.add(btnReports);
        sidebarPanel.add(btnLogout);

        /* ================= CONTENT ================= */
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new GridLayout(2, 2, 20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        contentPanel.add(createCard("Total Orders", "1,250"));
        contentPanel.add(createCard("Active Deliveries", "320"));
        contentPanel.add(createCard("Warehouses", "8"));
        contentPanel.add(createCard("Pending Issues", "14"));

        /* ================= ADD TO FRAME ================= */
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    /* ========== Sidebar Button Style ========== */
    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(30, 136, 229)); // Blue
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    /* ========== Dashboard Card ========== */
    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(245, 245, 245));
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(new Color(255, 152, 0)); // Orange

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }
}
