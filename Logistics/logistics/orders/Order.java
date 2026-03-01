package logistics.orders;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Order {
    public String id;
    public String customerName;
    public String customerPhone;
    public String customerEmail;
    public String customerAddress;
    public String recipientName;
    public String recipientPhone;
    public String recipientAddress;
    public String status;
    public String orderDate;
    public String estimatedDelivery;
    public String driverId;
    public String vehicleId;
    public double weight;
    public String dimensions;
    public String notes;
    public String reason;
    
    public Order() {
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.status = "Pending";
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        this.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    public Order(String id, String customerName, String customerPhone, 
                 String customerEmail, String customerAddress,
                 String recipientName, String recipientPhone, String recipientAddress,
                 double weight, String dimensions) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.weight = weight;
        this.dimensions = dimensions;
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.status = "Pending";
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        this.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    public Order(String id, String customerName, String customerPhone, 
                 String customerEmail, String customerAddress,
                 String recipientName, String recipientPhone, String recipientAddress,
                 double weight, String dimensions, String status, 
                 String orderDate, String estimatedDelivery) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.weight = weight;
        this.dimensions = dimensions;
        this.status = status;
        this.orderDate = orderDate;
        this.estimatedDelivery = estimatedDelivery;
    }

    // Add these methods to your Order class

/**
 * Assign a driver to this order
 */
    public void assignDriver(String driverId) {
        this.driverId = driverId;
        if (!"Delivered".equals(status) && !"Delayed".equals(status)) {
            this.status = "In Transit";
        }
    
    // Update estimated delivery
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        this.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

/**
 * Check if order can be assigned to a driver
 */
    public boolean isAssignable() {
        return "Pending".equals(status) || "Delayed".equals(status);
    }

/**
 * Get driver assignment status
 */
    public String getDriverAssignmentStatus() {
        if (driverId == null) return "Unassigned";
        if ("Delivered".equals(status)) return "Completed";
        if ("In Transit".equals(status)) return "Assigned - In Transit";
        return "Assigned";
    }
    
    // Save format: id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|driverId|vehicleId|weight|dimensions|notes|reason
    public String toFileString() {
        return String.join("|", 
            safeString(id),
            safeString(customerName),
            safeString(customerPhone),
            safeString(customerEmail),
            safeString(customerAddress),
            safeString(recipientName),
            safeString(recipientPhone),
            safeString(recipientAddress),
            safeString(status),
            safeString(orderDate),
            safeString(estimatedDelivery),
            safeString(driverId),
            safeString(vehicleId),
            String.valueOf(weight),
            safeString(dimensions),
            safeString(notes),
            safeString(reason)
        );
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    public static Order fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 17) {
            System.out.println("Invalid line format (expected 17 parts, got " + parts.length + "): " + line);
            return null;
        }
        
        try {
            Order o = new Order();
            o.id = parts[0];
            o.customerName = parts[1];
            o.customerPhone = parts[2];
            o.customerEmail = parts[3];
            o.customerAddress = parts[4];
            o.recipientName = parts[5];
            o.recipientPhone = parts[6];
            o.recipientAddress = parts[7];
            o.status = parts[8];
            o.orderDate = parts[9];
            o.estimatedDelivery = parts[10].isEmpty() ? null : parts[10];
            o.driverId = parts[11].isEmpty() ? null : parts[11];
            o.vehicleId = parts[12].isEmpty() ? null : parts[12];
            o.weight = parts[13].isEmpty() ? 0 : Double.parseDouble(parts[13]);
            o.dimensions = parts[14];
            o.notes = parts[15].isEmpty() ? null : parts[15];
            o.reason = parts[16].isEmpty() ? null : parts[16];
            return o;
        } catch (Exception e) {
            System.out.println("Error parsing order: " + e.getMessage());
            return null;
        }
    }
    
    public String getFormattedWeight() {
        return String.format("%.2f kg", weight);
    }
    
    public String getSenderInfo() {
        return String.format("%s | %s | %s", 
            customerName, customerPhone, customerEmail);
    }
    
    public String getRecipientInfo() {
        return String.format("%s | %s", recipientName, recipientPhone);
    }
    
    public boolean isDelayed() {
        if ("Delayed".equals(status)) return true;
        if (estimatedDelivery == null) return false;
        
        try {
            Date today = new Date();
            Date estDate = new SimpleDateFormat("yyyy-MM-dd").parse(estimatedDelivery);
            return today.after(estDate) && !"Delivered".equals(status);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return id + " - " + recipientName + " (" + status + ")";
    }
}