package logistics.login.admin.management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceManagement {
    private JPanel mainPanel;
    private JTable maintenanceTable;
    private DefaultTableModel tableModel;
    private List<Maintenance> maintenanceRecords;
    
    public MaintenanceManagement() {
        maintenanceRecords = new ArrayList<>();
        initializeSampleData();
        createMainPanel();
    }
    
    private void initializeSampleData() {
        maintenanceRecords.add(new Maintenance("MNT001", "VH001", "Oil Change", "Scheduled", "2024-01-20"));
        maintenanceRecords.add(new Maintenance("MNT002", "VH003", "Brake Repair", "In Progress", "2024-01-15"));
        maintenanceRecords.add(new Maintenance("MNT003", "VH002", "Tire Replacement", "Completed", "2024-01-10"));
        maintenanceRecords.add(new Maintenance("MNT004", "VH005", "Engine Check", "Scheduled", "2024-01-22"));
        maintenanceRecords.add(new Maintenance("MNT005", "VH004", "Battery Replacement", "Scheduled", "2024-01-18"));
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Maintenance Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"Maintenance ID", "Vehicle ID", "Description", "Status", "Scheduled Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        maintenanceTable = new JTable(tableModel);
        maintenanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        maintenanceTable.setRowHeight(30);
        maintenanceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceTable.getTableHeader().setBackground(new Color(255, 140, 0));
        maintenanceTable.getTableHeader().setForeground(Color.WHITE);
        
        refreshTableData();
        
        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Schedule Maintenance");
        addButton.setBackground(new Color(255, 140, 0));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(108, 117, 125));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshTableData() {
        tableModel.setRowCount(0);
        for (Maintenance record : maintenanceRecords) {
            tableModel.addRow(new Object[]{
                record.getMaintenanceId(),
                record.getVehicleId(),
                record.getDescription(),
                record.getStatus(),
                record.getScheduledDate()
            });
        }
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JPanel getRefreshedPanel() {
        refreshData();
        return mainPanel;
    }
    
    public void refreshData() {
        refreshTableData();
    }
    
    public int getScheduledCount() {
        return (int) maintenanceRecords.stream()
            .filter(m -> "Scheduled".equals(m.getStatus()))
            .count();
    }
    
    private class Maintenance {
        private String maintenanceId;
        private String vehicleId;
        private String description;
        private String status;
        private String scheduledDate;
        
        public Maintenance(String maintenanceId, String vehicleId, String description, String status, String scheduledDate) {
            this.maintenanceId = maintenanceId;
            this.vehicleId = vehicleId;
            this.description = description;
            this.status = status;
            this.scheduledDate = scheduledDate;
        }
        
        public String getMaintenanceId() { return maintenanceId; }
        public String getVehicleId() { return vehicleId; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getScheduledDate() { return scheduledDate; }
    }
}