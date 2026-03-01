package logistics.driver;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DriverStorage {
    private static final String DRIVER_FILE = "drivers.txt";
    private List<Driver> drivers;
    private Map<String, Integer> idCounters;
    
    public DriverStorage() {
        drivers = new ArrayList<>();
        idCounters = new HashMap<>();
        System.out.println("DriverStorage initialized. Looking for file: " + new File(DRIVER_FILE).getAbsolutePath());
        loadDrivers();
        
        // Debug: Print loaded drivers
        System.out.println("Loaded " + drivers.size() + " drivers");
        for (Driver d : drivers) {
            System.out.println("Driver: " + d.id + " - " + d.name + " (" + d.status + ")");
        }
    }
    
    private void loadDrivers() {
        File file = new File(DRIVER_FILE);
        
        if (!file.exists()) {
            System.out.println("Drivers file not found. Creating sample data...");
            createSampleData();
            saveDrivers();
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            drivers.clear();
            String line;
            int count = 0;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                Driver d = Driver.fromFileString(line);
                if (d != null) {
                    drivers.add(d);
                    count++;
                    updateIdCounter(d.id);
                }
            }
            
            System.out.println("Loaded " + count + " drivers from file");
            
            if (drivers.isEmpty()) {
                System.out.println("No drivers loaded from file. Creating sample data...");
                createSampleData();
                saveDrivers();
            }
            
        } catch (IOException e) {
            System.out.println("Error loading drivers: " + e.getMessage());
            e.printStackTrace();
            createSampleData();
        }
    }
    
    private void updateIdCounter(String driverId) {
        // Extract number from driver ID: DRV001
        if (driverId != null && driverId.startsWith("DRV")) {
            try {
                int num = Integer.parseInt(driverId.substring(3));
                String year = new SimpleDateFormat("yyyy").format(new Date());
                Integer current = idCounters.get(year);
                if (current == null || num > current) {
                    idCounters.put(year, num);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
    }
    
    public String generateDriverId() {
        String year = new SimpleDateFormat("yyyy").format(new Date());
        int nextNum = 1;
        
        Integer lastNum = idCounters.get(year);
        if (lastNum != null) {
            nextNum = lastNum + 1;
        }
        
        String driverId = String.format("DRV%03d", nextNum);
        idCounters.put(year, nextNum);
        return driverId;
    }
    
    public void saveDrivers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DRIVER_FILE))) {
            // Write header
            bw.write("# id|name|phone|email|licenseNumber|licenseExpiry|status|vehicleId|joinDate|totalDeliveries|rating|emergencyContact|emergencyPhone|address|notes|photoPath");
            bw.newLine();
            
            for (Driver d : drivers) {
                bw.write(d.toFileString());
                bw.newLine();
            }
            
            System.out.println("Saved " + drivers.size() + " drivers to " + DRIVER_FILE);
            
        } catch (IOException e) {
            System.out.println("Error saving drivers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createSampleData() {
        drivers.clear();
        
        try {
            // Driver 1
            Driver d1 = new Driver("DRV001", "Ahmad Bin Abdullah", "012-3456789", 
                "ahmad.abdullah@logixpress.com", "L12345678", "2025-12-31");
            d1.status = "On Delivery";
            d1.vehicleId = "TRK001";
            d1.joinDate = "2023-01-15";
            d1.totalDeliveries = 1245;
            d1.rating = 4.9;
            d1.emergencyContact = "Siti Aminah";
            d1.emergencyPhone = "012-9876543";
            d1.address = "No 15, Jalan SS2/72, 47300 Petaling Jaya, Selangor";
            d1.notes = "Experienced in long-haul deliveries";
            drivers.add(d1);
            
            // Driver 2
            Driver d2 = new Driver("DRV002", "Tan Siew Ming", "013-4567890", 
                "siewming.tan@logixpress.com", "L87654321", "2025-10-15");
            d2.status = "Available";
            d2.vehicleId = "TRK002";
            d2.joinDate = "2023-03-20";
            d2.totalDeliveries = 892;
            d2.rating = 4.7;
            d2.emergencyContact = "Tan Siew Hong";
            d2.emergencyPhone = "013-4561237";
            d2.address = "No 8, Jalan SS15/4, 47500 Subang Jaya, Selangor";
            drivers.add(d2);
            
            // Driver 3
            Driver d3 = new Driver("DRV003", "Rajesh Kumar", "014-5678901", 
                "rajesh.kumar@logixpress.com", "L34567890", "2024-08-20");
            d3.status = "On Delivery";
            d3.vehicleId = "VAN001";
            d3.joinDate = "2023-06-10";
            d3.totalDeliveries = 567;
            d3.rating = 4.5;
            d3.emergencyContact = "Lakshmi Kumar";
            d3.emergencyPhone = "014-5671234";
            d3.address = "No 23, Jalan Ipoh, 51200 Kuala Lumpur";
            drivers.add(d3);
            
            // Driver 4
            Driver d4 = new Driver("DRV004", "Nurul Huda", "015-6789012", 
                "nurul.huda@logixpress.com", "L45678901", "2026-02-28");
            d4.status = "Off Duty";
            d4.vehicleId = "VAN002";
            d4.joinDate = "2023-09-05";
            d4.totalDeliveries = 234;
            d4.rating = 4.8;
            d4.emergencyContact = "Ahmad Faiz";
            d4.emergencyPhone = "015-6785432";
            d4.address = "No 45, Jalan Gasing, 46000 Petaling Jaya, Selangor";
            d4.notes = "Prefers urban deliveries";
            drivers.add(d4);
            
            // Driver 5
            Driver d5 = new Driver("DRV005", "Chong Wei Ming", "016-7890123", 
                "weiming.chong@logixpress.com", "L56789012", "2025-05-30");
            d5.status = "Available";
            d5.vehicleId = "VAN003";
            d5.joinDate = "2024-01-12";
            d5.totalDeliveries = 78;
            d5.rating = 5.0;
            d5.emergencyContact = "Chong Wei Ling";
            d5.emergencyPhone = "016-7894321";
            d5.address = "No 67, Jalan SS21/56, 47400 Petaling Jaya, Selangor";
            drivers.add(d5);
            
            // Update ID counters
            idCounters.put("2024", 5);
            
            System.out.println("Sample data created with " + drivers.size() + " drivers");
            
        } catch (Exception e) {
            System.out.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // CRUD Operations
    public List<Driver> getAllDrivers() {
        // Sort by ID
        drivers.sort((a, b) -> a.id.compareTo(b.id));
        return drivers;
    }
    
    public Driver findDriver(String id) {
        for (Driver d : drivers) {
            if (d.id.equals(id)) return d;
        }
        return null;
    }
    
    public Driver findDriverByVehicle(String vehicleId) {
        for (Driver d : drivers) {
            if (vehicleId.equals(d.vehicleId)) return d;
        }
        return null;
    }
    
    public List<Driver> getAvailableDrivers() {
        return drivers.stream()
            .filter(d -> "Available".equals(d.status))
            .collect(Collectors.toList());
    }
    
    public List<Driver> getDriversByStatus(String status) {
        return drivers.stream()
            .filter(d -> status.equals(d.status))
            .collect(Collectors.toList());
    }
    
    public void addDriver(Driver d) {
        drivers.add(d);
        updateIdCounter(d.id);
        saveDrivers();
    }
    
    public void updateDriver(Driver updatedDriver) {
        for (int i = 0; i < drivers.size(); i++) {
            if (drivers.get(i).id.equals(updatedDriver.id)) {
                drivers.set(i, updatedDriver);
                saveDrivers();
                return;
            }
        }
    }
    
    public void removeDriver(String id) {
        drivers.removeIf(d -> d.id.equals(id));
        saveDrivers();
    }
    
    // Statistics
    public int getTotalCount() {
        return drivers.size();
    }
    
    public int getAvailableCount() {
        return (int) drivers.stream().filter(d -> "Available".equals(d.status)).count();
    }
    
    public int getOnDeliveryCount() {
        return (int) drivers.stream().filter(d -> "On Delivery".equals(d.status)).count();
    }
    
    public int getOffDutyCount() {
        return (int) drivers.stream().filter(d -> "Off Duty".equals(d.status) || "On Leave".equals(d.status)).count();
    }
    
    public double getAverageRating() {
        if (drivers.isEmpty()) return 0;
        return drivers.stream().mapToDouble(d -> d.rating).average().orElse(0);
    }
    
    public int getTotalDeliveries() {
        return drivers.stream().mapToInt(d -> d.totalDeliveries).sum();
    }
    
    public Map<String, Integer> getStatusStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Available", getAvailableCount());
        stats.put("On Delivery", getOnDeliveryCount());
        stats.put("Off Duty", getOffDutyCount());
        return stats;
    }
    
    public List<Driver> getDriversWithExpiringLicense(int months) {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, months);
        Date expiryThreshold = cal.getTime();
        
        return drivers.stream()
            .filter(d -> {
                if (d.licenseExpiry == null) return false;
                try {
                    Date expiry = new SimpleDateFormat("yyyy-MM-dd").parse(d.licenseExpiry);
                    return expiry.before(expiryThreshold);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
    }
    
    public String generateNewId() {
        return generateDriverId();
    }
    
    public void checkFileStatus() {
        File file = new File(DRIVER_FILE);
        System.out.println("=== Driver File Status ===");
        System.out.println("File path: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        if (file.exists()) {
            System.out.println("File size: " + file.length() + " bytes");
            System.out.println("File can read: " + file.canRead());
            System.out.println("File can write: " + file.canWrite());
        }
        System.out.println("==========================");
    }
}