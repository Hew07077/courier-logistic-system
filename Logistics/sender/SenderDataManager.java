// SenderDataManager.java
package sender;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SenderDataManager {
    private static final String ORDER_FILE = "orders.txt"; // Changed from sender_orders.txt to orders.txt
    private static SenderDataManager instance;
    private List<SenderOrder> orders;
    
    private SenderDataManager() {
        orders = new ArrayList<>();
        loadOrders();
        System.out.println("SenderDataManager initialized. Loaded " + orders.size() + " orders from orders.txt");
    }
    
    public static SenderDataManager getInstance() {
        if (instance == null) {
            instance = new SenderDataManager();
        }
        return instance;
    }
    
    private void loadOrders() {
        File file = new File(ORDER_FILE);
        orders.clear();
        
        if (!file.exists()) {
            System.out.println("Orders file not found at: " + file.getAbsolutePath());
            System.out.println("Will create orders.txt when first order is placed.");
            return;
        }
        
        System.out.println("Loading orders from: " + file.getAbsolutePath());
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            int orderCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineCount++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                SenderOrder order = parseOrderFromString(line);
                if (order != null) {
                    orders.add(order);
                    orderCount++;
                }
            }
            
            System.out.println("Loaded " + orderCount + " orders from " + lineCount + " lines");
            
        } catch (IOException e) {
            System.out.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private SenderOrder parseOrderFromString(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 15) {
                System.out.println("Invalid order format (too few fields): " + parts.length);
                return null;
            }
            
            SenderOrder order = new SenderOrder();
            order.setId(parts[0]);
            order.setCustomerName(parts[1]);
            order.setCustomerPhone(parts[2]);
            order.setCustomerEmail(parts[3]);
            order.setCustomerAddress(parts[4]);
            order.setRecipientName(parts[5]);
            order.setRecipientPhone(parts[6]);
            order.setRecipientAddress(parts[7]);
            order.setStatus(parts[8]);
            order.setOrderDate(parts[9]);
            order.setEstimatedDelivery(parts[10].isEmpty() ? null : parts[10]);
            
            try {
                order.setWeight(Double.parseDouble(parts[11]));
            } catch (NumberFormatException e) {
                order.setWeight(0.0);
            }
            
            order.setDimensions(parts[12]);
            order.setNotes(parts[13].isEmpty() ? null : parts[13]);
            
            // Payment fields
            if (parts.length > 14) {
                order.setPaymentStatus(parts[14].isEmpty() ? "Pending" : parts[14]);
            } else {
                order.setPaymentStatus("Pending");
            }
            
            if (parts.length > 15) {
                order.setPaymentMethod(parts[15].isEmpty() ? null : parts[15]);
            }
            
            if (parts.length > 16) {
                order.setTransactionId(parts[16].isEmpty() ? null : parts[16]);
            }
            
            if (parts.length > 17) {
                order.setPaymentDate(parts[17].isEmpty() ? null : parts[17]);
            }
            
            return order;
        } catch (Exception e) {
            System.out.println("Error parsing order: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String orderToString(SenderOrder order) {
        // Clean notes - remove any newlines that might break the pipe format
        String cleanNotes = order.getNotes() != null ? 
            order.getNotes().replace("\n", " ").replace("\r", " ") : "";
        
        return String.join("|",
            safeString(order.getId()),
            safeString(order.getCustomerName()),
            safeString(order.getCustomerPhone()),
            safeString(order.getCustomerEmail()),
            safeString(order.getCustomerAddress()),
            safeString(order.getRecipientName()),
            safeString(order.getRecipientPhone()),
            safeString(order.getRecipientAddress()),
            safeString(order.getStatus()),
            safeString(order.getOrderDate()),
            safeString(order.getEstimatedDelivery()),
            String.valueOf(order.getWeight()),
            safeString(order.getDimensions()),
            cleanNotes,  // Use cleaned notes without newlines
            safeString(order.getPaymentStatus()),
            safeString(order.getPaymentMethod()),
            safeString(order.getTransactionId()),
            safeString(order.getPaymentDate())
        );
    }
    
    private String safeString(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }
    
    public void saveOrders() {
        try {
            File file = new File(ORDER_FILE);
            System.out.println("Saving orders to: " + file.getAbsolutePath());
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                // Write header
                bw.write("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|weight|dimensions|notes|paymentStatus|paymentMethod|transactionId|paymentDate");
                bw.newLine();
                
                for (SenderOrder order : orders) {
                    bw.write(orderToString(order));
                    bw.newLine();
                }
                
                System.out.println("Saved " + orders.size() + " orders to " + file.getAbsolutePath());
                
            }
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addOrder(SenderOrder order) {
        if (order != null) {
            // Check if order already exists
            SenderOrder existing = getOrderById(order.getId());
            if (existing == null) {
                orders.add(order);
                saveOrders();
                System.out.println("Order added: " + order.getId() + " for " + order.getCustomerEmail());
            } else {
                System.out.println("Order already exists, not adding duplicate: " + order.getId());
            }
        }
    }
    
    public List<SenderOrder> getAllOrders() {
        return new ArrayList<>(orders);
    }
    
    public List<SenderOrder> getOrdersByEmail(String email) {
        if (email == null) return new ArrayList<>();
        
        List<SenderOrder> result = orders.stream()
            .filter(o -> email.equalsIgnoreCase(o.getCustomerEmail()))
            .collect(Collectors.toList());
        
        System.out.println("getOrdersByEmail(" + email + ") found " + result.size() + " orders");
        return result;
    }
    
    public SenderOrder getOrderById(String orderId) {
        if (orderId == null) return null;
        
        return orders.stream()
            .filter(o -> orderId.equals(o.getId()))
            .findFirst()
            .orElse(null);
    }
    
    public List<SenderOrder> getOrdersByStatus(String status) {
        if (status == null || "All Orders".equals(status)) {
            return getAllOrders();
        }
        
        return orders.stream()
            .filter(o -> status.equals(o.getStatus()))
            .collect(Collectors.toList());
    }
    
    public List<SenderOrder> getOrdersByPaymentStatus(String paymentStatus) {
        if (paymentStatus == null) return new ArrayList<>();
        
        return orders.stream()
            .filter(o -> paymentStatus.equals(o.getPaymentStatus()))
            .collect(Collectors.toList());
    }
    
    public boolean cancelOrder(String orderId) {
        SenderOrder order = getOrderById(orderId);
        
        if (order != null && !"Delivered".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus())) {
            order.setStatus("Cancelled");
            saveOrders();
            return true;
        }
        return false;
    }
    
    public boolean updateOrderPaymentStatus(String orderId, String status, String paymentMethod, 
                                           String transactionId, String paymentDate) {
        SenderOrder order = getOrderById(orderId);
        
        if (order != null) {
            order.setPaymentStatus(status);
            order.setPaymentMethod(paymentMethod);
            order.setTransactionId(transactionId);
            order.setPaymentDate(paymentDate);
            saveOrders();
            return true;
        }
        return false;
    }
    
    public int getActiveOrders(String email) {
        return (int) getOrdersByEmail(email).stream()
            .filter(o -> !"Delivered".equals(o.getStatus()) && !"Cancelled".equals(o.getStatus()))
            .count();
    }
    
    public int getDeliveredOrders(String email) {
        return (int) getOrdersByEmail(email).stream()
            .filter(o -> "Delivered".equals(o.getStatus()))
            .count();
    }
    
    public int getPendingPayments(String email) {
        return (int) getOrdersByEmail(email).stream()
            .filter(o -> "Pending".equals(o.getPaymentStatus()))
            .count();
    }
    
    public double getTotalSpent(String email) {
        return getOrdersByEmail(email).stream()
            .filter(o -> "Paid".equals(o.getPaymentStatus()))
            .mapToDouble(o -> extractCostFromNotes(o.getNotes()))
            .sum();
    }
    
    private double extractCostFromNotes(String notes) {
        if (notes == null) return 0.0;
        
        try {
            if (notes.contains("Estimated Cost:")) {
                String[] parts = notes.split("Estimated Cost: ");
                if (parts.length > 1) {
                    String costPart = parts[1];
                    // Extract just the RM value (e.g., "RM 85.50" from "RM 85.50 Estimated Delivery...")
                    String[] costParts = costPart.split(" ");
                    if (costParts.length > 0) {
                        String costStr = costParts[0].replace("RM", "").replace("$", "").replace(",", "").trim();
                        return Double.parseDouble(costStr);
                    }
                }
            }
            return 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}