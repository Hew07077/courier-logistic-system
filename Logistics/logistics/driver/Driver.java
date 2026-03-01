package logistics.driver;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Driver {
    public String id;
    public String name;
    public String phone;
    public String email;
    public String licenseNumber;
    public String licenseExpiry;
    public String status; // Available, On Delivery, Off Duty, On Leave
    public String vehicleId; // Assigned vehicle
    public String joinDate;
    public int totalDeliveries;
    public double rating;
    public String emergencyContact;
    public String emergencyPhone;
    public String address;
    public String notes;
    public String photoPath;
    
    public Driver() {
        this.joinDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.status = "Available";
        this.totalDeliveries = 0;
        this.rating = 5.0;
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
        this.status = "Available";
        this.totalDeliveries = 0;
        this.rating = 5.0;
    }
    
    // Save format: id|name|phone|email|licenseNumber|licenseExpiry|status|vehicleId|joinDate|totalDeliveries|rating|emergencyContact|emergencyPhone|address|notes|photoPath
    public String toFileString() {
        return String.join("|", 
            safeString(id),
            safeString(name),
            safeString(phone),
            safeString(email),
            safeString(licenseNumber),
            safeString(licenseExpiry),
            safeString(status),
            safeString(vehicleId),
            safeString(joinDate),
            String.valueOf(totalDeliveries),
            String.valueOf(rating),
            safeString(emergencyContact),
            safeString(emergencyPhone),
            safeString(address),
            safeString(notes),
            safeString(photoPath)
        );
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    public static Driver fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 16) {
            System.out.println("Invalid driver line format (expected 16 parts, got " + parts.length + ")");
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
            d.status = parts[6];
            d.vehicleId = parts[7].isEmpty() ? null : parts[7];
            d.joinDate = parts[8];
            d.totalDeliveries = parts[9].isEmpty() ? 0 : Integer.parseInt(parts[9]);
            d.rating = parts[10].isEmpty() ? 5.0 : Double.parseDouble(parts[10]);
            d.emergencyContact = parts[11].isEmpty() ? null : parts[11];
            d.emergencyPhone = parts[12].isEmpty() ? null : parts[12];
            d.address = parts[13].isEmpty() ? null : parts[13];
            d.notes = parts[14].isEmpty() ? null : parts[14];
            d.photoPath = parts[15].isEmpty() ? null : parts[15];
            return d;
        } catch (Exception e) {
            System.out.println("Error parsing driver: " + e.getMessage());
            return null;
        }
    }
    
    public String getFormattedRating() {
        return String.format("%.1f ⭐", rating);
    }
    
    public boolean isAvailable() {
        return "Available".equals(status);
    }
    
    public boolean isOnDelivery() {
        return "On Delivery".equals(status);
    }
    
    @Override
    public String toString() {
        return id + " - " + name + " (" + status + ")";
    }
}