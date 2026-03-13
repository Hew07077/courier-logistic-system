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
        
        System.out.println("Loaded " + drivers.size() + " drivers");
        for (Driver d : drivers) {
            System.out.println("Driver: " + d.id + " - " + d.name + " [" + d.approvalStatus + "] (" + d.workStatus + ") - Orders: " + d.getCurrentOrderCount());
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
            bw.write("# id|name|phone|email|licenseNumber|licenseExpiry|workStatus|approvalStatus|vehicleId|joinDate|totalDeliveries|rating|emergencyContact|emergencyPhone|address|notes|photoPath|passwordHash|icNumber|licenseType|remarks|currentOrderIds|completedOrderIds|totalDistance|totalFuelUsed|onTimeDeliveries|lateDeliveries");
            bw.newLine();
            
            for (Driver d : drivers) {
                bw.write(d.toFileString());
                bw.newLine();
            }
            
            bw.flush();
            System.out.println("Saved " + drivers.size() + " drivers to " + DRIVER_FILE);
            
        } catch (IOException e) {
            System.out.println("Error saving drivers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createSampleData() {
        drivers.clear();
        
        try {
            // Driver 1 - Approved, On Delivery with orders
            Driver d1 = new Driver("DRV001", "Ahmad Bin Abdullah", "012-3456789", 
                "ahmad.abdullah@logixpress.com", "L12345678", "2025-12-31");
            d1.workStatus = "On Delivery";
            d1.approvalStatus = "APPROVED";
            d1.vehicleId = "TRK001";
            d1.joinDate = "2023-01-15";
            d1.totalDeliveries = 1245;
            d1.rating = 4.9;
            d1.emergencyContact = "Siti Aminah";
            d1.emergencyPhone = "012-9876543";
            d1.address = "No 15, Jalan SS2/72, 47300 Petaling Jaya, Selangor";
            d1.notes = "Experienced in long-haul deliveries";
            d1.passwordHash = hashPassword("ahmad123");
            d1.icNumber = "850101-01-1234";
            d1.licenseType = "D";
            d1.currentOrderIds = Arrays.asList("ORD20240301001", "ORD20240301002");
            d1.completedOrderIds = Arrays.asList("ORD20240228004", "ORD20240228005", "ORD20240228006");
            d1.totalDistance = 45890.5;
            d1.totalFuelUsed = 5230.8;
            d1.onTimeDeliveries = 1180;
            d1.lateDeliveries = 65;
            drivers.add(d1);
            
            // Driver 2 - Approved, Available
            Driver d2 = new Driver("DRV002", "Tan Siew Ming", "013-4567890", 
                "siewming.tan@logixpress.com", "L87654321", "2025-10-15");
            d2.workStatus = "Available";
            d2.approvalStatus = "APPROVED";
            d2.vehicleId = "TRK002";
            d2.joinDate = "2023-03-20";
            d2.totalDeliveries = 892;
            d2.rating = 4.7;
            d2.emergencyContact = "Tan Siew Hong";
            d2.emergencyPhone = "013-4561237";
            d2.address = "No 8, Jalan SS15/4, 47500 Subang Jaya, Selangor";
            d2.passwordHash = hashPassword("tan123");
            d2.icNumber = "870202-02-5678";
            d2.licenseType = "D";
            d2.completedOrderIds = Arrays.asList("ORD20240301003", "ORD20240228001");
            d2.totalDistance = 32450.2;
            d2.totalFuelUsed = 3890.4;
            d2.onTimeDeliveries = 845;
            d2.lateDeliveries = 47;
            drivers.add(d2);
            
            // Driver 3 - Pending Approval
            Driver d3 = new Driver("DRV003", "Rajesh Kumar", "014-5678901", 
                "rajesh.kumar@logixpress.com", "L34567890", "2024-08-20");
            d3.workStatus = "Off Duty";
            d3.approvalStatus = "PENDING";
            d3.joinDate = "2024-02-10";
            d3.totalDeliveries = 0;
            d3.rating = 5.0;
            d3.emergencyContact = "Lakshmi Kumar";
            d3.emergencyPhone = "014-5671234";
            d3.address = "No 23, Jalan Ipoh, 51200 Kuala Lumpur";
            d3.passwordHash = hashPassword("rajesh123");
            d3.icNumber = "900303-03-9101";
            d3.licenseType = "B2";
            d3.photoPath = "pending_photo.jpg";
            drivers.add(d3);
            
            // Driver 4 - Rejected
            Driver d4 = new Driver("DRV004", "Nurul Huda", "015-6789012", 
                "nurul.huda@logixpress.com", "L45678901", "2026-02-28");
            d4.workStatus = "Off Duty";
            d4.approvalStatus = "REJECTED";
            d4.joinDate = "2024-02-15";
            d4.totalDeliveries = 0;
            d4.rating = 0;
            d4.emergencyContact = "Ahmad Faiz";
            d4.emergencyPhone = "015-6785432";
            d4.address = "No 45, Jalan Gasing, 46000 Petaling Jaya, Selangor";
            d4.passwordHash = hashPassword("nurul123");
            d4.icNumber = "910404-04-1213";
            d4.licenseType = "D";
            d4.remarks = "Invalid license document - expired";
            drivers.add(d4);
            
            // Driver 5 - Approved, Available
            Driver d5 = new Driver("DRV005", "Chong Wei Ming", "016-7890123", 
                "weiming.chong@logixpress.com", "L56789012", "2025-05-30");
            d5.workStatus = "Available";
            d5.approvalStatus = "APPROVED";
            d5.vehicleId = "VAN003";
            d5.joinDate = "2024-01-12";
            d5.totalDeliveries = 78;
            d5.rating = 5.0;
            d5.emergencyContact = "Chong Wei Ling";
            d5.emergencyPhone = "016-7894321";
            d5.address = "No 67, Jalan SS21/56, 47400 Petaling Jaya, Selangor";
            d5.passwordHash = hashPassword("chong123");
            d5.icNumber = "880505-05-1415";
            d5.licenseType = "D";
            d5.completedOrderIds = Arrays.asList("ORD20240301004", "ORD20240301005");
            d5.totalDistance = 2450.8;
            d5.totalFuelUsed = 289.5;
            d5.onTimeDeliveries = 76;
            d5.lateDeliveries = 2;
            drivers.add(d5);
            
            // Update ID counters
            idCounters.put("2024", 5);
            
            System.out.println("Sample data created with " + drivers.size() + " drivers");
            
        } catch (Exception e) {
            System.out.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }
    
    // CRUD Operations
    public List<Driver> getAllDrivers() {
        drivers.sort((a, b) -> a.id.compareTo(b.id));
        return drivers;
    }
    
    public Driver findDriver(String id) {
        for (Driver d : drivers) {
            if (d.id.equals(id)) return d;
        }
        return null;
    }
    
    public Driver findDriverByEmail(String email) {
        for (Driver d : drivers) {
            if (email != null && email.equals(d.email)) return d;
        }
        return null;
    }
    
    public Driver findDriverByVehicle(String vehicleId) {
        for (Driver d : drivers) {
            if (vehicleId != null && vehicleId.equals(d.vehicleId)) return d;
        }
        return null;
    }
    
    public List<Driver> getAvailableDrivers() {
        return drivers.stream()
            .filter(d -> "APPROVED".equals(d.approvalStatus) && "Available".equals(d.workStatus))
            .collect(Collectors.toList());
    }
    
    public List<Driver> getDriversWithCapacity() {
        return drivers.stream()
            .filter(d -> "APPROVED".equals(d.approvalStatus) && 
                   ("Available".equals(d.workStatus) || "On Delivery".equals(d.workStatus)) &&
                   d.getCurrentOrderCount() < 3)
            .collect(Collectors.toList());
    }
    
    public List<Driver> getApprovedDrivers() {
        return drivers.stream()
            .filter(d -> "APPROVED".equals(d.approvalStatus))
            .collect(Collectors.toList());
    }
    
    public List<Driver> getPendingDrivers() {
        return drivers.stream()
            .filter(d -> "PENDING".equals(d.approvalStatus))
            .collect(Collectors.toList());
    }
    
    public List<Driver> getRejectedDrivers() {
        return drivers.stream()
            .filter(d -> "REJECTED".equals(d.approvalStatus))
            .collect(Collectors.toList());
    }
    
    public List<Driver> getDriversByWorkStatus(String status) {
        return drivers.stream()
            .filter(d -> status.equals(d.workStatus))
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
    
    public void approveDriver(String id) {
        Driver d = findDriver(id);
        if (d != null) {
            d.approvalStatus = "APPROVED";
            d.workStatus = "Available";
            saveDrivers();
        }
    }
    
    public void rejectDriver(String id, String reason) {
        Driver d = findDriver(id);
        if (d != null) {
            d.approvalStatus = "REJECTED";
            d.remarks = reason;
            saveDrivers();
        }
    }
    
    // Order integration methods
    public boolean assignOrderToDriver(String driverId, String orderId) {
        Driver d = findDriver(driverId);
        if (d != null && d.isAvailable()) {
            d.assignOrder(orderId);
            saveDrivers();
            return true;
        }
        return false;
    }
    
    public boolean completeOrderForDriver(String driverId, String orderId, boolean onTime, double distance, double fuelUsed) {
        Driver d = findDriver(driverId);
        if (d != null) {
            d.completeOrder(orderId, onTime, distance, fuelUsed);
            saveDrivers();
            return true;
        }
        return false;
    }
    
    public List<Driver> getDriversWithOrder(String orderId) {
        List<Driver> result = new ArrayList<>();
        for (Driver d : drivers) {
            if (d.currentOrderIds.contains(orderId) || d.completedOrderIds.contains(orderId)) {
                result.add(d);
            }
        }
        return result;
    }
    
    public Driver getDriverByOrder(String orderId) {
        for (Driver d : drivers) {
            if (d.currentOrderIds.contains(orderId)) {
                return d;
            }
        }
        return null;
    }
    
    // Statistics
    public int getTotalCount() {
        return drivers.size();
    }
    
    public int getApprovedCount() {
        return (int) drivers.stream().filter(d -> "APPROVED".equals(d.approvalStatus)).count();
    }
    
    public int getPendingCount() {
        return (int) drivers.stream().filter(d -> "PENDING".equals(d.approvalStatus)).count();
    }
    
    public int getRejectedCount() {
        return (int) drivers.stream().filter(d -> "REJECTED".equals(d.approvalStatus)).count();
    }
    
    public int getAvailableCount() {
        return (int) drivers.stream().filter(d -> "APPROVED".equals(d.approvalStatus) && "Available".equals(d.workStatus)).count();
    }
    
    public int getOnDeliveryCount() {
        return (int) drivers.stream().filter(d -> "APPROVED".equals(d.approvalStatus) && "On Delivery".equals(d.workStatus)).count();
    }
    
    public int getOffDutyCount() {
        return (int) drivers.stream().filter(d -> "APPROVED".equals(d.approvalStatus) && 
            ("Off Duty".equals(d.workStatus) || "On Leave".equals(d.workStatus))).count();
    }
    
    public double getAverageRating() {
        if (drivers.isEmpty()) return 0;
        return drivers.stream().mapToDouble(d -> d.rating).average().orElse(0);
    }
    
    public int getTotalDeliveries() {
        return drivers.stream().mapToInt(d -> d.totalDeliveries).sum();
    }
    
    public double getTotalDistance() {
        return drivers.stream().mapToDouble(d -> d.totalDistance).sum();
    }
    
    public double getTotalFuelUsed() {
        return drivers.stream().mapToDouble(d -> d.totalFuelUsed).sum();
    }
    
    public double getAverageOnTimeRate() {
        if (drivers.isEmpty()) return 0;
        return drivers.stream().mapToDouble(Driver::getOnTimeRate).average().orElse(0);
    }
    
    public Driver getTopPerformer() {
        return drivers.stream()
            .filter(d -> d.totalDeliveries > 0)
            .max((a, b) -> Double.compare(a.rating, b.rating))
            .orElse(null);
    }
    
    public Map<String, Integer> getApprovalStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Approved", getApprovedCount());
        stats.put("Pending", getPendingCount());
        stats.put("Rejected", getRejectedCount());
        return stats;
    }
    
    public Map<String, Integer> getWorkStatusStatistics() {
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
    
    public boolean verifyPassword(String driverId, String password) {
        Driver d = findDriver(driverId);
        if (d == null || d.passwordHash == null) return false;
        return hashPassword(password).equals(d.passwordHash);
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