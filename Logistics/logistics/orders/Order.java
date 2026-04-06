package logistics.orders;

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
    public String status; // 真实状态: Pending, In Transit, Picked Up, Out for Delivery, Delivered, Delayed, Failed, Cancelled
    public String orderDate;
    public String estimatedDelivery;
    public String actualDelivery;
    public String driverId;
    public String vehicleId;
    public double weight;
    public String dimensions;
    public String notes;
    public String reason;
    
    public String pickupTime;
    public String deliveryTime;
    public double distance;
    public double fuelUsed;
    public String deliveryPhoto;
    public String recipientSignature;
    public boolean onTime;
    
    public String paymentStatus;
    public String paymentMethod;
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

    public static Order fromSenderOrder(Object senderOrder) {
        try {
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
            safeString(notes != null ? notes.replace("\n", " ").replace("\r", " ") : ""),
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
        try {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 17) return null;
            
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
            o.estimatedDelivery = parts[10];
            o.actualDelivery = parts.length > 11 ? parts[11] : "";
            o.driverId = parts.length > 12 ? parts[12] : "";
            o.vehicleId = parts.length > 13 ? parts[13] : "";
            
            try {
                o.weight = parts.length > 14 && !parts[14].isEmpty() ? Double.parseDouble(parts[14]) : 0.0;
            } catch (NumberFormatException e) { o.weight = 0.0; }
            
            o.dimensions = parts.length > 15 ? parts[15] : "";
            o.notes = parts.length > 16 ? parts[16] : "";
            o.reason = parts.length > 17 ? parts[17] : "";
            o.pickupTime = parts.length > 18 ? parts[18] : "";
            o.deliveryTime = parts.length > 19 ? parts[19] : "";
            
            try {
                o.distance = parts.length > 20 && !parts[20].isEmpty() ? Double.parseDouble(parts[20]) : 0.0;
            } catch (NumberFormatException e) { o.distance = 0.0; }
            
            try {
                o.fuelUsed = parts.length > 21 && !parts[21].isEmpty() ? Double.parseDouble(parts[21]) : 0.0;
            } catch (NumberFormatException e) { o.fuelUsed = 0.0; }
            
            o.deliveryPhoto = parts.length > 22 ? parts[22] : "";
            o.recipientSignature = parts.length > 23 ? parts[23] : "";
            o.onTime = parts.length > 24 && !parts[24].isEmpty() ? Boolean.parseBoolean(parts[24]) : true;
            o.paymentStatus = parts.length > 25 && !parts[25].isEmpty() ? parts[25] : "Pending";
            o.paymentMethod = parts.length > 26 ? parts[26] : "";
            o.transactionId = parts.length > 27 ? parts[27] : "";
            o.paymentDate = parts.length > 28 ? parts[28] : "";
            
            return o;
        } catch (Exception e) {
            System.err.println("Error parsing order line: " + e.getMessage());
            return null;
        }
    }
    
    // ========== STATUS METHODS ==========
    
    /**
     * Admin sees actual system status
     */
    public String getAdminStatus() {
        return this.status;
    }
    
    /**
     * Courier sees simplified status
     * 映射规则:
     * - Pending (实际) -> Pending (Courier看到等待取件)
     * - In Transit (实际) -> Pending (Courier看到等待取件)
     * - Picked Up (实际) -> Picked Up
     * - Out for Delivery (实际) -> Out for Delivery  
     * - Delivered (实际) -> Delivered
     * - Delayed (实际) -> Delayed
     * - Failed (实际) -> Failed
     */
    public String getCourierStatus() {
        switch(this.status) {
            case "Pending":
                return "Pending";
            case "In Transit":
                return "Pending";  // 司机看到Pending，表示等待取件
            case "Picked Up":
                return "Picked Up";
            case "Out for Delivery":
                return "Out for Delivery";
            case "Delivered":
                return "Delivered";
            case "Delayed":
                return "Delayed";
            case "Failed":
                return "Failed";
            default:
                return this.status;
        }
    }
    
    /**
     * Get the actual system status that corresponds to a courier display status
     */
    public String getActualStatusFromCourierStatus(String courierStatus) {
        switch(courierStatus) {
            case "Pending":
                return "In Transit";
            case "Picked Up":
                return "Picked Up";
            case "In Transit":
                return "In Transit";
            case "Out for Delivery":
                return "Out for Delivery";
            case "Delivered":
                return "Delivered";
            case "Delayed":
                return "Delayed";
            case "Failed":
                return "Failed";
            default:
                return courierStatus;
        }
    }
    
    /**
     * Update status from courier's selection
     */
    public void updateFromCourierStatus(String courierStatus) {
        String actualStatus = getActualStatusFromCourierStatus(courierStatus);
        this.status = actualStatus;
        
        if ("Picked Up".equals(courierStatus) && this.pickupTime == null) {
            this.pickupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        } else if ("Delivered".equals(courierStatus)) {
            this.deliveryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.actualDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
    }
    
    /**
     * Get customer view status
     */
    public String getCustomerStatus() {
        return this.status;
    }
    
    public String getCustomerStatusDescription() {
        String status = getCustomerStatus();
        switch(status) {
            case "Pending":
                return "Your order is pending pickup by courier";
            case "In Transit":
                return "Your package is on the way to the recipient";
            case "Picked Up":
                return "Your package has been picked up and is with the courier";
            case "Out for Delivery":
                return "Your package is out for delivery today";
            case "Delayed":
                return "Your delivery has been delayed";
            case "Delivered":
                return "Your package has been delivered successfully";
            case "Failed":
                return "Delivery was unsuccessful. Please contact support.";
            default:
                return "";
        }
    }
    
    public String getFormattedCustomerStatus() {
        String icon;
        switch(getCustomerStatus()) {
            case "Pending": icon = "⏳"; break;
            case "Picked Up": icon = "📦"; break;
            case "In Transit": icon = "🚚"; break;
            case "Out for Delivery": icon = "🚛"; break;
            case "Delayed": icon = "⚠️"; break;
            case "Delivered": icon = "✅"; break;
            case "Failed": icon = "❌"; break;
            default: icon = "📋";
        }
        return icon + " " + getCustomerStatus();
    }
    
    public String getDriverDisplayStatus() {
        return getCourierStatus();
    }
    
    public void markAsFailed(String failureReason) {
        this.status = "Failed";
        this.reason = failureReason;
        this.onTime = false;
        this.actualDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.deliveryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
    
    public boolean isAssignable() {
        return "Pending".equals(status) || "In Transit".equals(status) || "Delayed".equals(status);
    }
    
    public void assignDriver(String driverId) {
        if (("Pending".equals(status) || "In Transit".equals(status) || "Delayed".equals(status)) && !"Delivered".equals(status)) {
            this.driverId = driverId;
            this.status = "In Transit";
            this.pickupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        this.estimatedDelivery = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    public void markAsDelivered(double distance, double fuelUsed, String photoPath, String signature) {
        this.status = "Delivered";
        this.actualDelivery = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.deliveryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.distance = distance;
        this.fuelUsed = fuelUsed;
        this.deliveryPhoto = photoPath;
        this.recipientSignature = signature;
        
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
        if ("Failed".equals(status)) return "Failed - Contact Admin";
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
        if ("Failed".equals(status)) return false;
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
        } else if ("Failed".equals(status)) {
            return "Delivery failed - " + (reason != null ? reason : "Reason not specified");
        } else {
            return status;
        }
    }
    
    @Override
    public String toString() {
        return id + " - " + recipientName + " (" + getCourierStatus() + ")" + 
               (driverId != null ? " - Driver: " + driverId : "");
    }
}