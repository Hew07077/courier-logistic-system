// SenderOrder.java
package sender;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SenderOrder {
    private String id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String status;
    private String orderDate;
    private String estimatedDelivery;
    private double weight;
    private String dimensions;
    private String notes;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionId;
    private String paymentDate;
    
    // New fields for tracking
    private String driverId;
    private String vehicleId;
    
    public SenderOrder() {
        this.orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        this.status = "Pending";
        this.paymentStatus = "Pending";
    }
    
    public SenderOrder(String id, String customerName, String customerPhone, 
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
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    
    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getPaymentStatus() { 
        return paymentStatus; 
    }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    
    /**
     * Extract package type from notes
     */
    public String getPackageType() {
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Package Type:")) {
                String[] parts = notes.split(";");
                for (String part : parts) {
                    String trimmedPart = part.trim();
                    if (trimmedPart.startsWith("Package Type:")) {
                        String result = trimmedPart.substring("Package Type:".length()).trim();
                        int pipeIndex = result.indexOf("|");
                        if (pipeIndex > 0) {
                            result = result.substring(0, pipeIndex);
                        }
                        int semiIndex = result.indexOf(";");
                        if (semiIndex > 0) {
                            result = result.substring(0, semiIndex);
                        }
                        return result;
                    }
                }
            }
        }
        return "Standard";
    }
    
    /**
     * Extract estimated cost from notes
     */
    public double getEstimatedCost() {
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Estimated Cost:")) {
                String[] parts = notes.split(";");
                for (String part : parts) {
                    String trimmedPart = part.trim();
                    if (trimmedPart.startsWith("Estimated Cost:")) {
                        String costStr = trimmedPart.substring("Estimated Cost:".length()).trim();
                        costStr = costStr.replace("RM", "").replace("$", "").replace(",", "").trim();
                        try {
                            return Double.parseDouble(costStr);
                        } catch (NumberFormatException e) {
                            // Continue
                        }
                    }
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Get formatted estimated cost
     */
    public String getFormattedEstimatedCost() {
        double cost = getEstimatedCost();
        if (cost > 0) {
            return String.format("RM %.2f", cost);
        }
        return "RM --.--";
    }
    
    /**
     * Get description from notes
     */
    public String getDescription() {
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Description:")) {
                String[] parts = notes.split(";");
                for (String part : parts) {
                    String trimmedPart = part.trim();
                    if (trimmedPart.startsWith("Description:")) {
                        String result = trimmedPart.substring("Description:".length()).trim();
                        int pipeIndex = result.indexOf("|");
                        if (pipeIndex > 0) {
                            result = result.substring(0, pipeIndex);
                        }
                        int semiIndex = result.indexOf(";");
                        if (semiIndex > 0) {
                            result = result.substring(0, semiIndex);
                        }
                        return result;
                    }
                }
            }
        }
        return "";
    }

    public String getReason() {
        if (notes != null && !notes.isEmpty()) {
            if (notes.contains("Reason:")) {
                String[] parts = notes.split(";");
                for (String part : parts) {
                    String trimmedPart = part.trim();
                    if (trimmedPart.startsWith("Reason:")) {
                        return trimmedPart.substring("Reason:".length()).trim();
                    }
                }
            }
        }
        return null;
    }
    
    public String toMainSystemFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(safeString(id)).append("|");
        sb.append(safeString(customerName)).append("|");
        sb.append(safeString(customerPhone)).append("|");
        sb.append(safeString(customerEmail)).append("|");
        sb.append(safeString(customerAddress)).append("|");
        sb.append(safeString(recipientName)).append("|");
        sb.append(safeString(recipientPhone)).append("|");
        sb.append(safeString(recipientAddress)).append("|");
        sb.append(safeString(status)).append("|");
        sb.append(safeString(orderDate)).append("|");
        sb.append(safeString(estimatedDelivery)).append("|");
        sb.append("|");
        sb.append(safeString(driverId)).append("|");
        sb.append(safeString(vehicleId)).append("|");
        sb.append(weight).append("|");
        sb.append(safeString(dimensions)).append("|");
        
        String cleanNotes = notes != null ? notes.replace("\n", " ").replace("\r", " ").replace("|", ";") : "";
        sb.append(cleanNotes).append("|");
        sb.append("|");
        sb.append("|");
        sb.append("|");
        sb.append("0|");
        sb.append("0|");
        sb.append("|");
        sb.append("|");
        sb.append("false|");
        sb.append(safeString(paymentStatus)).append("|");
        sb.append(safeString(paymentMethod)).append("|");
        sb.append(safeString(transactionId)).append("|");
        sb.append(safeString(paymentDate));
        
        return sb.toString();
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    public String getFormattedWeight() {
        return String.format("%.2f kg", weight);
    }
    
    public String getSenderInfo() {
        return String.format("%s | %s | %s", customerName, customerPhone, customerEmail);
    }
    
    public String getRecipientInfo() {
        return String.format("%s | %s", recipientName, recipientPhone);
    }
    
    public boolean isCancellable() {
        return "Pending".equals(status);
    }
    
    public boolean isModifiable() {
        return "Pending".equals(status);
    }
    
    public boolean isDeletable() {
        return "Pending".equals(status);
    }

    public boolean isDemoOrder() {
        return customerEmail != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(customerEmail);
    }
    
    @Override
    public String toString() {
        return id + " - " + recipientName + " (" + status + ")";
    }
}