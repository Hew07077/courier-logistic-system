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
    public String status; // Pending, In Transit, Delivered, Delayed, Cancelled
    public String orderDate;
    public String estimatedDelivery;
    public String actualDelivery;
    public String driverId;
    public String vehicleId;
    public double weight;
    public String dimensions;
    public String notes;
    public String reason; // Delay reason or cancellation reason
    
    // New fields for delivery tracking
    public String pickupTime;
    public String deliveryTime;
    public double distance; // Distance in km
    public double fuelUsed; // Fuel used in liters
    public String deliveryPhoto;
    public String recipientSignature;
    public boolean onTime;
    
    // Payment fields from SenderOrder
    public String paymentStatus; // Pending, Paid, Failed
    public String paymentMethod; // Credit Card, PayPal, Bank Transfer, etc.
    public String transactionId;
    public String paymentDate;
    
    public Order() {
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        this.status = "Pending";
        this.paymentStatus = "Pending";
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        this.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        this.onTime = true;
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
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        this.status = "Pending";
        this.paymentStatus = "Pending";
        this.onTime = true;
        
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
        this.paymentStatus = "Pending";
        this.onTime = true;
    }

    /**
     * Create an Order from a sender.SenderOrder object
     */
    public static Order fromSenderOrder(Object senderOrder) {
        try {
            // Use reflection to access SenderOrder methods
            Class<?> clazz = senderOrder.getClass();
            
            String id = (String) clazz.getMethod("getId").invoke(senderOrder);
            String customerName = (String) clazz.getMethod("getCustomerName").invoke(senderOrder);
            String customerPhone = (String) clazz.getMethod("getCustomerPhone").invoke(senderOrder);
            String customerEmail = (String) clazz.getMethod("getCustomerEmail").invoke(senderOrder);
            String customerAddress = (String) clazz.getMethod("getCustomerAddress").invoke(senderOrder);
            String recipientName = (String) clazz.getMethod("getRecipientName").invoke(senderOrder);
            String recipientPhone = (String) clazz.getMethod("getRecipientPhone").invoke(senderOrder);
            String recipientAddress = (String) clazz.getMethod("getRecipientAddress").invoke(senderOrder);
            Double weightObj = (Double) clazz.getMethod("getWeight").invoke(senderOrder);
            double weight = weightObj != null ? weightObj : 0.0;
            String dimensions = (String) clazz.getMethod("getDimensions").invoke(senderOrder);
            String status = (String) clazz.getMethod("getStatus").invoke(senderOrder);
            String orderDate = (String) clazz.getMethod("getOrderDate").invoke(senderOrder);
            String estimatedDelivery = (String) clazz.getMethod("getEstimatedDelivery").invoke(senderOrder);
            String notes = (String) clazz.getMethod("getNotes").invoke(senderOrder);
            String paymentStatus = (String) clazz.getMethod("getPaymentStatus").invoke(senderOrder);
            String paymentMethod = (String) clazz.getMethod("getPaymentMethod").invoke(senderOrder);
            String transactionId = (String) clazz.getMethod("getTransactionId").invoke(senderOrder);
            String paymentDate = (String) clazz.getMethod("getPaymentDate").invoke(senderOrder);
            
            Order order = new Order(
                id, customerName, customerPhone, customerEmail, customerAddress,
                recipientName, recipientPhone, recipientAddress, weight, dimensions,
                status, orderDate, estimatedDelivery
            );
            
            order.notes = notes;
            order.paymentStatus = paymentStatus != null ? paymentStatus : "Pending";
            order.paymentMethod = paymentMethod;
            order.transactionId = transactionId;
            order.paymentDate = paymentDate;
            
            return order;
            
        } catch (Exception e) {
            System.err.println("Error converting sender order: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Save format: id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate
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
            safeString(actualDelivery),
            safeString(driverId),
            safeString(vehicleId),
            String.valueOf(weight),
            safeString(dimensions),
            safeString(notes),
            safeString(reason),
            safeString(pickupTime),
            safeString(deliveryTime),
            String.valueOf(distance),
            String.valueOf(fuelUsed),
            safeString(deliveryPhoto),
            safeString(recipientSignature),
            String.valueOf(onTime),
            safeString(paymentStatus),
            safeString(paymentMethod),
            safeString(transactionId),
            safeString(paymentDate)
        );
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    public static Order fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 25) {
            System.out.println("Invalid order line format (expected at least 25 parts, got " + parts.length + "): " + line);
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
            o.actualDelivery = parts[11].isEmpty() ? null : parts[11];
            o.driverId = parts[12].isEmpty() ? null : parts[12];
            o.vehicleId = parts[13].isEmpty() ? null : parts[13];
            o.weight = parts[14].isEmpty() ? 0 : Double.parseDouble(parts[14]);
            o.dimensions = parts[15];
            o.notes = parts[16].isEmpty() ? null : parts[16];
            o.reason = parts[17].isEmpty() ? null : parts[17];
            o.pickupTime = parts[18].isEmpty() ? null : parts[18];
            o.deliveryTime = parts[19].isEmpty() ? null : parts[19];
            o.distance = parts[20].isEmpty() ? 0 : Double.parseDouble(parts[20]);
            o.fuelUsed = parts[21].isEmpty() ? 0 : Double.parseDouble(parts[21]);
            o.deliveryPhoto = parts[22].isEmpty() ? null : parts[22];
            o.recipientSignature = parts[23].isEmpty() ? null : parts[23];
            o.onTime = parts[24].isEmpty() ? true : Boolean.parseBoolean(parts[24]);
            
            // Parse payment fields if available
            if (parts.length > 25) {
                o.paymentStatus = parts[25].isEmpty() ? "Pending" : parts[25];
            } else {
                o.paymentStatus = "Pending";
            }
            
            if (parts.length > 26) {
                o.paymentMethod = parts[26].isEmpty() ? null : parts[26];
            }
            
            if (parts.length > 27) {
                o.transactionId = parts[27].isEmpty() ? null : parts[27];
            }
            
            if (parts.length > 28) {
                o.paymentDate = parts[28].isEmpty() ? null : parts[28];
            }
            
            return o;
        } catch (Exception e) {
            System.out.println("Error parsing order: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void assignDriver(String driverId) {
        this.driverId = driverId;
        if (!"Delivered".equals(status) && !"Cancelled".equals(status)) {
            this.status = "In Transit";
        }
        this.pickupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        this.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    public boolean isAssignable() {
        return "Pending".equals(status) || "Delayed".equals(status);
    }
    
    public void markAsDelivered(double distance, double fuelUsed, String photoPath, String signature) {
        this.status = "Delivered";
        this.actualDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.deliveryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.distance = distance;
        this.fuelUsed = fuelUsed;
        this.deliveryPhoto = photoPath;
        this.recipientSignature = signature;
        
        // Check if delivery was on time
        try {
            Date estDate = new SimpleDateFormat("yyyy-MM-dd").parse(estimatedDelivery);
            Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(actualDelivery);
            this.onTime = !actualDate.after(estDate);
        } catch (Exception e) {
            this.onTime = true;
        }
    }
    
    public void markAsDelayed(String reason) {
        this.status = "Delayed";
        this.reason = reason;
        this.onTime = false;
    }

    public String getDriverAssignmentStatus() {
        if (driverId == null) return "Unassigned";
        if ("Delivered".equals(status)) return "Completed";
        if ("In Transit".equals(status)) return "Assigned - In Transit";
        return "Assigned";
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
        if (estimatedDelivery == null || "Delivered".equals(status) || "Cancelled".equals(status)) return false;
        
        try {
            Date today = new Date();
            Date estDate = new SimpleDateFormat("yyyy-MM-dd").parse(estimatedDelivery);
            return today.after(estDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getDeliverySummary() {
        if ("Delivered".equals(status)) {
            return String.format(
                "Delivered on %s | Distance: %.1f km | Fuel: %.1f L | %s",
                actualDelivery, distance, fuelUsed, onTime ? "ON TIME" : "LATE"
            );
        } else if ("In Transit".equals(status)) {
            return "In transit with driver " + driverId;
        } else {
            return status;
        }
    }
    
    @Override
    public String toString() {
        return id + " - " + recipientName + " (" + status + ")" + 
               (driverId != null ? " - Driver: " + driverId : "");
    }
}