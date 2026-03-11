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
}