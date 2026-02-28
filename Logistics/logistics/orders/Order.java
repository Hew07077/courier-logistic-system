package logistics.orders;

import java.io.*;
import java.util.*;

public class Order {
    public String id;
    public String customer;
    public String phone;
    public String address;
    public String status;
    public String date;
    public String estDate;
    public String driver;
    public String vehicle;
    public String reason;
    
    public Order(String id, String customer, String phone, String address) {
        this.id = id;
        this.customer = customer;
        this.phone = phone;
        this.address = address;
        this.status = "Pending";
        this.date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
    
    // 保存格式：id|customer|phone|address|status|date|estDate|driver|vehicle|reason
    public String toFileString() {
        return String.join("|", 
            id, 
            customer, 
            phone, 
            address, 
            status, 
            date,
            estDate != null ? estDate : "",
            driver != null ? driver : "",
            vehicle != null ? vehicle : "",
            reason != null ? reason : ""
        );
    }
    
    public static Order fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 10) return null;
        
        Order o = new Order(parts[0], parts[1], parts[2], parts[3]);
        o.status = parts[4];
        o.date = parts[5];
        o.estDate = parts[6].isEmpty() ? null : parts[6];
        o.driver = parts[7].isEmpty() ? null : parts[7];
        o.vehicle = parts[8].isEmpty() ? null : parts[8];
        o.reason = parts[9].isEmpty() ? null : parts[9];
        return o;
    }
}