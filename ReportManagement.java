package logistics.login.admin.management;

import javax.swing.*;
import java.awt.*;

public class ReportManagement {
    private JPanel mainPanel;
    
    public ReportManagement() {
        createMainPanel();
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Reports & Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create tabs for different reports
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Summary Report
        JPanel summaryPanel = createSummaryPanel();
        tabbedPane.addTab("Summary", summaryPanel);
        
        // Orders Report
        JPanel ordersReportPanel = createOrdersReportPanel();
        tabbedPane.addTab("Orders Report", ordersReportPanel);
        
        // Vehicles Report
        JPanel vehiclesReportPanel = createVehiclesReportPanel();
        tabbedPane.addTab("Vehicles Report", vehiclesReportPanel);
        
        // Drivers Report
        JPanel driversReportPanel = createDriversReportPanel();
        tabbedPane.addTab("Drivers Report", driversReportPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Stats cards
        panel.add(createStatCard("Total Orders", "156", new Color(52, 152, 219)));
        panel.add(createStatCard("Active Vehicles", "24", new Color(46, 204, 113)));
        panel.add(createStatCard("Available Drivers", "18", new Color(155, 89, 182)));
        panel.add(createStatCard("Pending Maintenance", "7", new Color(230, 126, 34)));
        
        return panel;
    }
    
    private JPanel createOrdersReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setText(
            "ORDERS REPORT - " + new java.util.Date() + "\n" +
            "================================\n\n" +
            "Total Orders: 156\n" +
            "Pending Orders: 42\n" +
            "In Transit: 68\n" +
            "Delivered: 46\n\n" +
            "Daily Average: 12 orders/day\n" +
            "Peak Hour: 2:00 PM - 4:00 PM"
        );
        reportArea.setEditable(false);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createVehiclesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setText(
            "VEHICLES REPORT - " + new java.util.Date() + "\n" +
            "================================\n\n" +
            "Total Vehicles: 32\n" +
            "Active: 24\n" +
            "In Maintenance: 5\n" +
            "Inactive: 3\n\n" +
            "Fuel Efficiency: 8.5 km/l average\n" +
            "Total Distance: 12,450 km"
        );
        reportArea.setEditable(false);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDriversReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setText(
            "DRIVERS REPORT - " + new java.util.Date() + "\n" +
            "================================\n\n" +
            "Total Drivers: 25\n" +
            "On Duty: 18\n" +
            "Off Duty: 5\n" +
            "On Leave: 2\n\n" +
            "Average Deliveries per Driver: 6\n" +
            "Top Performer: John Smith (45 deliveries)"
        );
        reportArea.setEditable(false);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JPanel getRefreshedPanel() {
        refreshData();
        return mainPanel;
    }
    
    public void refreshData() {
        // Refresh report data
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}