package logistics.driver;

import java.text.SimpleDateFormat;
import java.util.*;

public class Driver {
    public String id;
    public String name;
    public String phone;
    public String email;
    public String licenseNumber;
    public String licenseExpiry;
    public String workStatus;      // Available, On Delivery, Off Duty, On Leave
    public String approvalStatus;   // PENDING, APPROVED, REJECTED
    public String vehicleId;
    public String joinDate;
    public int totalDeliveries;
    public String emergencyContact;
    public String emergencyPhone;
    public String address;
    public String notes;
    public String photoPath;        // IC photo path
    public String passwordHash;
    public String icNumber;
    public String licenseType;      //B,B1,B2,D,DA,E,E1,E2
    public String remarks;
    
    // Fields for order integration
    public List<String> currentOrderIds;
    public List<String> completedOrderIds;
    public double totalDistance;
    public double totalFuelUsed;
    public int onTimeDeliveries;
    public int lateDeliveries;
    
    public Driver() {
        this.joinDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.workStatus = "Off Duty";
        this.approvalStatus = "PENDING";
        this.totalDeliveries = 0;
        this.currentOrderIds = new ArrayList<>();
        this.completedOrderIds = new ArrayList<>();
        this.totalDistance = 0;
        this.totalFuelUsed = 0;
        this.onTimeDeliveries = 0;
        this.lateDeliveries = 0;
    }
    
    public Driver(String id, String name, String phone, String email, 
                  String licenseNumber, String licenseExpiry) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.licenseExpiry = licenseExpiry;
        this.joinDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.workStatus = "Off Duty";
        this.approvalStatus = "PENDING";
        this.totalDeliveries = 0;
        this.currentOrderIds = new ArrayList<>();
        this.completedOrderIds = new ArrayList<>();
        this.totalDistance = 0;
        this.totalFuelUsed = 0;
        this.onTimeDeliveries = 0;
        this.lateDeliveries = 0;
    }
    
    // Save format
    public String toFileString() {
        return String.join("|", 
            safeString(id),
            safeString(name),
            safeString(phone),
            safeString(email),
            safeString(licenseNumber),
            safeString(licenseExpiry),
            safeString(workStatus),
            safeString(approvalStatus),
            safeString(vehicleId),
            safeString(joinDate),
            String.valueOf(totalDeliveries),
            "", 
            safeString(emergencyContact),
            safeString(emergencyPhone),
            safeString(address),
            safeString(notes),
            safeString(photoPath),
            safeString(passwordHash),
            safeString(icNumber),
            safeString(licenseType),
            safeString(remarks),
            serializeList(currentOrderIds),
            serializeList(completedOrderIds),
            String.valueOf(totalDistance),
            String.valueOf(totalFuelUsed),
            String.valueOf(onTimeDeliveries),
            String.valueOf(lateDeliveries)
        );
    }
    
    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }
    
    private List<String> deserializeList(String str) {
        if (str == null || str.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(str.split(",")));
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    public static Driver fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 27) {
            return null;
        }
        
        try {
            Driver d = new Driver();
            d.id = parts[0];
            d.name = parts[1];
            d.phone = parts[2];
            d.email = parts[3];
            d.licenseNumber = parts[4];
            d.licenseExpiry = parts[5];
            d.workStatus = parts[6];
            d.approvalStatus = parts[7];
            d.vehicleId = parts[8].isEmpty() ? null : parts[8];
            d.joinDate = parts[9];
            d.totalDeliveries = parts[10].isEmpty() ? 0 : Integer.parseInt(parts[10]);
            d.emergencyContact = parts[12].isEmpty() ? null : parts[12];
            d.emergencyPhone = parts[13].isEmpty() ? null : parts[13];
            d.address = parts[14].isEmpty() ? null : parts[14];
            d.notes = parts[15].isEmpty() ? null : parts[15];
            d.photoPath = parts[16].isEmpty() ? null : parts[16];
            d.passwordHash = parts[17].isEmpty() ? null : parts[17];
            d.icNumber = parts[18].isEmpty() ? null : parts[18];
            d.licenseType = parts[19].isEmpty() ? null : parts[19];
            d.remarks = parts[20].isEmpty() ? null : parts[20];
            d.currentOrderIds = d.deserializeList(parts[21]);
            d.completedOrderIds = d.deserializeList(parts[22]);
            d.totalDistance = parts[23].isEmpty() ? 0 : Double.parseDouble(parts[23]);
            d.totalFuelUsed = parts[24].isEmpty() ? 0 : Double.parseDouble(parts[24]);
            d.onTimeDeliveries = parts[25].isEmpty() ? 0 : Integer.parseInt(parts[25]);
            d.lateDeliveries = parts[26].isEmpty() ? 0 : Integer.parseInt(parts[26]);
            return d;
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isAvailable() {
        return "APPROVED".equals(approvalStatus) && "Available".equals(workStatus);
    }
    
    public boolean isApproved() {
        return "APPROVED".equals(approvalStatus);
    }
    
    public boolean canLogin() {
        return "APPROVED".equals(approvalStatus);
    }
    
    public boolean canTakeNewOrder() {
        return isAvailable() && currentOrderIds.size() < 3;
    }
    
    public void assignOrder(String orderId) {
        if (orderId != null && !orderId.isEmpty() && !currentOrderIds.contains(orderId)) {
            currentOrderIds.add(orderId);
            workStatus = "On Delivery";
        }
    }
    
    public void completeOrder(String orderId, boolean onTime, double distance, double fuelUsed) {
        if (currentOrderIds.remove(orderId)) {
            completedOrderIds.add(orderId);
            totalDeliveries++;
            totalDistance += distance;
            totalFuelUsed += fuelUsed;
            
            if (onTime) {
                onTimeDeliveries++;
            } else {
                lateDeliveries++;
            }
            
            if (currentOrderIds.isEmpty()) {
                workStatus = "Available";
            }
        }
    }
    
    public int getCurrentOrderCount() {
        return currentOrderIds.size();
    }
    
    public List<String> getCurrentOrderIds() {
        return new ArrayList<>(currentOrderIds);
    }
    
    public double getOnTimeRate() {
        if (totalDeliveries == 0) return 1.0;
        return (double) onTimeDeliveries / totalDeliveries;
    }
    
    public String getPerformanceSummary() {
        return String.format(
            "Deliveries: %d | On-Time: %.1f%% | Distance: %.1f km | Fuel: %.1f L",
            totalDeliveries,
            getOnTimeRate() * 100,
            totalDistance,
            totalFuelUsed
        );
    }
    
    @Override
    public String toString() {
        return id + " - " + name + " [" + approvalStatus + "] (" + workStatus + ")";
    }
}