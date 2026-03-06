package sender.models;

public class Payment {
    private String paymentId;
    private String orderId;
    private double amount;
    private String date;
    private String status;
    private String method;

    public Payment(String paymentId, String orderId, double amount, 
                   String date, String status, String method) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.method = method;
    }

    // Getters and setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}