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
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    
    /**
     * Extract package type from notes
     */
    public String getPackageType() {
        if (notes != null) {
            // Try to find Package Type with pipe separator first
            if (notes.contains("|Package Type:")) {
                String[] parts = notes.split("\\|");
                for (String part : parts) {
                    if (part.startsWith("Package Type:")) {
                        return part.substring("Package Type:".length()).trim();
                    }
                }
            }
            // Try with colon separator
            else if (notes.contains("Package Type:")) {
                int startIdx = notes.indexOf("Package Type:") + "Package Type:".length();
                int endIdx = notes.indexOf("|", startIdx);
                if (endIdx == -1) {
                    endIdx = notes.indexOf(" ", startIdx + 1);
                    if (endIdx == -1) {
                        endIdx = notes.length();
                    }
                }
                return notes.substring(startIdx, endIdx).trim();
            }
        }
        return "Standard";
    }
    
    /**
     * Extract estimated cost from notes
     */
    public double getEstimatedCost() {
        if (notes != null) {
            // Try to find Estimated Cost with pipe separator first
            if (notes.contains("|Estimated Cost:")) {
                String[] parts = notes.split("\\|");
                for (String part : parts) {
                    if (part.startsWith("Estimated Cost:")) {
                        String costStr = part.substring("Estimated Cost:".length()).trim();
                        costStr = costStr.replace("RM", "").replace("$", "").replace(",", "").trim();
                        try {
                            return Double.parseDouble(costStr);
                        } catch (NumberFormatException e) {
                            // Continue to fallback
                        }
                    }
                }
            }
            // Try with colon separator
            else if (notes.contains("Estimated Cost:")) {
                int startIdx = notes.indexOf("Estimated Cost:") + "Estimated Cost:".length();
                int endIdx = notes.indexOf("|", startIdx);
                if (endIdx == -1) {
                    endIdx = notes.indexOf(" ", startIdx + 1);
                    if (endIdx == -1) {
                        endIdx = notes.length();
                    }
                }
                String costStr = notes.substring(startIdx, endIdx).trim();
                costStr = costStr.replace("RM", "").replace("$", "").replace(",", "").trim();
                try {
                    return Double.parseDouble(costStr);
                } catch (NumberFormatException e) {
                    // Return 0
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
        if (notes != null) {
            if (notes.contains("|Description:")) {
                String[] parts = notes.split("\\|");
                for (String part : parts) {
                    if (part.startsWith("Description:")) {
                        return part.substring("Description:".length()).trim();
                    }
                }
            } else if (notes.contains("Description:")) {
                int startIdx = notes.indexOf("Description:") + "Description:".length();
                return notes.substring(startIdx).trim();
            }
        }
        return "";
    }
    
    /**
     * Convert this SenderOrder to a format compatible with the main Order system
     * @return A string representation for the main orders.txt file
     */
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
        sb.append("|"); // actualDelivery (empty)
        sb.append("|"); // driverId (empty)
        sb.append("|"); // vehicleId (empty)
        sb.append(weight).append("|");
        sb.append(safeString(dimensions)).append("|");
        
        // Clean notes - remove newlines
        String cleanNotes = notes != null ? notes.replace("\n", " ").replace("\r", " ") : "";
        sb.append(cleanNotes).append("|");
        sb.append("|"); // reason (empty)
        sb.append("|"); // pickupTime (empty)
        sb.append("|"); // deliveryTime (empty)
        sb.append("0|"); // distance
        sb.append("0|"); // fuelUsed
        sb.append("|"); // deliveryPhoto (empty)
        sb.append("|"); // recipientSignature (empty)
        sb.append("true|"); // onTime
        sb.append(safeString(paymentStatus)).append("|");
        sb.append(safeString(paymentMethod)).append("|");
        sb.append(safeString(transactionId)).append("|");
        sb.append(safeString(paymentDate));
        
        return sb.toString();
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    // Helper methods
    public String getFormattedWeight() {
        return String.format("%.2f kg", weight);
    }
    
    public String getSenderInfo() {
        return String.format("%s | %s | %s", customerName, customerPhone, customerEmail);
    }
    
    public String getRecipientInfo() {
        return String.format("%s | %s", recipientName, recipientPhone);
    }
    
    /**
     * Check if this order can be cancelled
     */
    public boolean isCancellable() {
        return "Pending".equals(status);
    }
    
    /**
     * Check if this order can be modified
     */
    public boolean isModifiable() {
        return "Pending".equals(status);
    }
    
    @Override
    public String toString() {
        return id + " - " + recipientName + " (" + status + ")";
    }
}