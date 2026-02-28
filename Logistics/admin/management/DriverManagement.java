package admin.management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DriverManagement {
    private JPanel mainPanel;
    private JTable driversTable;
    private DefaultTableModel tableModel;
    private List<Driver> drivers;
    
    public DriverManagement() {
        drivers = new ArrayList<>();
        initializeSampleData();
        createMainPanel();
    }
    
    private void initializeSampleData() {
        drivers.add(new Driver("DRV001", "John Smith", "On Duty", "VH001"));
        drivers.add(new Driver("DRV002", "Mike Johnson", "On Duty", "VH002"));
        drivers.add(new Driver("DRV003", "Sarah Wilson", "On Duty", "VH004"));
        drivers.add(new Driver("DRV004", "David Brown", "Off Duty", null));
        drivers.add(new Driver("DRV005", "Emily Davis", "On Leave", null));
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        String[] columns = {"Driver ID", "Name", "Status", "Assigned Vehicle"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        driversTable = new JTable(tableModel);
        driversTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        driversTable.setRowHeight(30);
        driversTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        driversTable.getTableHeader().setBackground(new Color(255, 140, 0));
        driversTable.getTableHeader().setForeground(Color.WHITE);
        
        refreshTableData();
        
        JScrollPane scrollPane = new JScrollPane(driversTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Add New Driver");
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
        for (Driver driver : drivers) {
            tableModel.addRow(new Object[]{
                driver.getDriverId(),
                driver.getName(),
                driver.getStatus(),
                driver.getAssignedVehicle() != null ? driver.getAssignedVehicle() : "Unassigned"
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
    
    public int getOnDutyCount() {
        return (int) drivers.stream()
            .filter(d -> "On Duty".equals(d.getStatus()))
            .count();
    }
    
    public int getTotalCount() {
        return drivers.size();
    }
    
    private class Driver {
        private String driverId;
        private String name;
        private String status;
        private String assignedVehicle;
        
        public Driver(String driverId, String name, String status, String assignedVehicle) {
            this.driverId = driverId;
            this.name = name;
            this.status = status;
            this.assignedVehicle = assignedVehicle;
        }
        
        public String getDriverId() { return driverId; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getAssignedVehicle() { return assignedVehicle; }
    }
}