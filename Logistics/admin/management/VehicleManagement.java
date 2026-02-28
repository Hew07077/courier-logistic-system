package admin.management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleManagement {
    private JPanel mainPanel;
    private JTable vehiclesTable;
    private DefaultTableModel tableModel;
    private List<Vehicle> vehicles;
    
    public VehicleManagement() {
        vehicles = new ArrayList<>();
        initializeSampleData();
        createMainPanel();
    }
    
    private void initializeSampleData() {
        vehicles.add(new Vehicle("VH001", "Truck A", "Active", "John Smith"));
        vehicles.add(new Vehicle("VH002", "Van B", "Active", "Mike Johnson"));
        vehicles.add(new Vehicle("VH003", "Truck C", "Maintenance", null));
        vehicles.add(new Vehicle("VH004", "Van D", "Active", "Sarah Wilson"));
        vehicles.add(new Vehicle("VH005", "Truck E", "Inactive", null));
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        String[] columns = {"Vehicle ID", "Model", "Status", "Driver"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        vehiclesTable = new JTable(tableModel);
        vehiclesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vehiclesTable.setRowHeight(30);
        vehiclesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehiclesTable.getTableHeader().setBackground(new Color(255, 140, 0));
        vehiclesTable.getTableHeader().setForeground(Color.WHITE);
        
        refreshTableData();
        
        JScrollPane scrollPane = new JScrollPane(vehiclesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Add New Vehicle");
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
        for (Vehicle vehicle : vehicles) {
            tableModel.addRow(new Object[]{
                vehicle.getVehicleId(),
                vehicle.getModel(),
                vehicle.getStatus(),
                vehicle.getDriverName() != null ? vehicle.getDriverName() : "Unassigned"
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
    
    public int getActiveCount() {
        return (int) vehicles.stream()
            .filter(v -> "Active".equals(v.getStatus()))
            .count();
    }
    
    public int getTotalCount() {
        return vehicles.size();
    }
    
    private class Vehicle {
        private String vehicleId;
        private String model;
        private String status;
        private String driverName;
        
        public Vehicle(String vehicleId, String model, String status, String driverName) {
            this.vehicleId = vehicleId;
            this.model = model;
            this.status = status;
            this.driverName = driverName;
        }
        
        public String getVehicleId() { return vehicleId; }
        public String getModel() { return model; }
        public String getStatus() { return status; }
        public String getDriverName() { return driverName; }
    }
}