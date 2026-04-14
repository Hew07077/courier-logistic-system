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
        loadDrivers();
    }
    
    private void loadDrivers() {
        File file = new File(DRIVER_FILE);
        
        if (!file.exists()) {
            createSampleData();
            saveDrivers();
            return;
        }
        
        List<Driver> newDrivers = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                Driver d = Driver.fromFileString(line);
                if (d != null) {
                    newDrivers.add(d);
                }
            }
            
            drivers.clear();
            drivers.addAll(newDrivers);
            
            for (Driver d : drivers) {
                updateIdCounter(d.id);
            }
            
            if (drivers.isEmpty()) {
                createSampleData();
                saveDrivers();
            }
            
        } catch (IOException e) {
            if (drivers.isEmpty()) {
                createSampleData();
            }
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
                // Ignore
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
            bw.write("# id|name|phone|email|licenseNumber|licenseExpiry|workStatus|approvalStatus|vehicleId|joinDate|totalDeliveries|rating|emergencyContact|emergencyPhone|address|notes|photoPath|passwordHash|icNumber|licenseType|remarks|currentOrderIds|completedOrderIds|totalDistance|totalFuelUsed|onTimeDeliveries|lateDeliveries");
            bw.newLine();
            
            for (Driver d : drivers) {
                bw.write(d.toFileString());
                bw.newLine();
            }
            
            bw.flush();
            
        } catch (IOException e) {
            System.err.println("Error saving drivers: " + e.getMessage());
        }
    }
    
    private void createSampleData() {
        drivers.clear();
        
        Driver d1 = new Driver("DRV001", "Ahmad Bin Abdullah", "0123456789", 
            "ahmad.abdullah@logixpress.com", "L12345678", "2025-12-31");
        d1.workStatus = "Available";
        d1.approvalStatus = "APPROVED";
        d1.vehicleId = null;
        d1.joinDate = "2023-01-15";
        d1.totalDeliveries = 1245;
        d1.passwordHash = hashPassword("ahmad123");
        drivers.add(d1);
        
        Driver d2 = new Driver("DRV002", "Tan Siew Ming", "0134567890", 
            "siewming.tan@logixpress.com", "L87654321", "2025-10-15");
        d2.workStatus = "Available";
        d2.approvalStatus = "APPROVED";
        d2.vehicleId = null;
        d2.joinDate = "2023-03-20";
        d2.totalDeliveries = 892;
        d2.passwordHash = hashPassword("tan123");
        drivers.add(d2);
        
        Driver d3 = new Driver("DRV003", "Rajesh Kumar", "0145678901", 
            "rajesh.kumar@logixpress.com", "L34567890", "2024-08-20");
        d3.workStatus = "Off Duty";
        d3.approvalStatus = "PENDING";
        d3.vehicleId = null;
        d3.joinDate = "2024-02-10";
        d3.passwordHash = hashPassword("rajesh123");
        drivers.add(d3);
        
        Driver d4 = new Driver("DRV004", "Nurul Huda", "0156789012", 
            "nurul.huda@logixpress.com", "L45678901", "2026-02-28");
        d4.workStatus = "Off Duty";
        d4.approvalStatus = "REJECTED";
        d4.vehicleId = null;
        d4.joinDate = "2024-02-15";
        d4.passwordHash = hashPassword("nurul123");
        d4.remarks = "Invalid license document - expired";
        drivers.add(d4);
        
        Driver d5 = new Driver("DRV005", "Chong Wei Ming", "0167890123", 
            "weiming.chong@logixpress.com", "L56789012", "2025-05-30");
        d5.workStatus = "Available";
        d5.approvalStatus = "APPROVED";
        d5.vehicleId = null;
        d5.joinDate = "2024-01-12";
        d5.totalDeliveries = 78;
        d5.passwordHash = hashPassword("chong123");
        drivers.add(d5);
        
        idCounters.put("2024", 5);
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
    
    public boolean isIcNumberExists(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) return false;
        loadDrivers();
        for (Driver driver : drivers) {
            if (icNumber.equals(driver.icNumber)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        loadDrivers();
        for (Driver driver : drivers) {
            if (email.equalsIgnoreCase(driver.email)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        loadDrivers();
        for (Driver driver : drivers) {
            if (phone.equals(driver.phone)) {
                return true;
            }
        }
        return false;
    }
    
    public List<Driver> getAllDrivers() {
        loadDrivers();
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
    
    public void addDriver(Driver d) {
        drivers.add(d);
        updateIdCounter(d.id);
        saveDrivers();
    }
    
    public void updateDriver(Driver updatedDriver) {
        boolean found = false;
        for (int i = 0; i < drivers.size(); i++) {
            if (drivers.get(i).id.equals(updatedDriver.id)) {
                drivers.set(i, updatedDriver);
                found = true;
                break;
            }
        }
        
        if (!found) {
            drivers.add(updatedDriver);
        }
        
        saveDrivers();
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
    
    public String generateNewId() {
        return generateDriverId();
    }
    
    public boolean verifyPassword(String driverId, String password) {
        Driver d = findDriver(driverId);
        if (d == null || d.passwordHash == null) return false;
        return hashPassword(password).equals(d.passwordHash);
    }
}