// SenderDataManager.java
package sender;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SenderDataManager {
    private static final String ORDER_FILE = "orders.txt";
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
        System.out.println("File size: " + file.length() + " bytes");
        
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
    
    /**
     * Parse order from the full 29-field format
     */
    private SenderOrder parseOrderFromString(String line) {
        try {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 18) {
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
            
            // Field 12 = driverId (new)
            if (parts.length > 12) {
                order.setDriverId(parts[12].isEmpty() ? null : parts[12]);
            }
            
            // Field 13 = vehicleId (new)
            if (parts.length > 13) {
                order.setVehicleId(parts[13].isEmpty() ? null : parts[13]);
            }
            
            try {
                order.setWeight(Double.parseDouble(parts[14]));
            } catch (NumberFormatException e) {
                order.setWeight(0.0);
            }
            
            order.setDimensions(parts[15]);
            order.setNotes(parts[16].isEmpty() ? null : parts[16]);
            
            // Payment fields - now at positions 25-28
            if (parts.length > 25) {
                order.setPaymentStatus(parts[25].isEmpty() ? "Pending" : parts[25]);
            } else if (parts.length > 14) {
                order.setPaymentStatus(parts[14].isEmpty() ? "Pending" : parts[14]);
            } else {
                order.setPaymentStatus("Pending");
            }
            
            if (parts.length > 26) {
                order.setPaymentMethod(parts[26].isEmpty() ? null : parts[26]);
            } else if (parts.length > 15) {
                order.setPaymentMethod(parts[15].isEmpty() ? null : parts[15]);
            }
            
            if (parts.length > 27) {
                order.setTransactionId(parts[27].isEmpty() ? null : parts[27]);
            } else if (parts.length > 16) {
                order.setTransactionId(parts[16].isEmpty() ? null : parts[16]);
            }
            
            if (parts.length > 28) {
                order.setPaymentDate(parts[28].isEmpty() ? null : parts[28]);
            } else if (parts.length > 17) {
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
            "",
            safeString(order.getDriverId()),
            safeString(order.getVehicleId()),
            String.valueOf(order.getWeight()),
            safeString(order.getDimensions()),
            cleanNotes,
            "",
            "",
            "",
            "0",
            "0",
            "",
            "",
            "false",
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
            
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Create a temporary file first
            File tempFile = new File(ORDER_FILE + ".tmp");
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
                // Write header
                bw.write("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate");
                bw.newLine();
                
                // Write all orders
                for (SenderOrder order : orders) {
                    bw.write(orderToString(order));
                    bw.newLine();
                }
                
                bw.flush();
            }
            
            // Delete original file and rename temp file
            if (file.exists()) {
                file.delete();
            }
            tempFile.renameTo(file);
            
            System.out.println("Saved " + orders.size() + " orders to " + file.getAbsolutePath());
            
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addOrder(SenderOrder order) {
        if (order != null) {
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
    
    /**
     * Delete an order from the system by ID
     */
    public boolean deleteOrder(String orderId) {
        System.out.println("===== DELETE ORDER CALLED =====");
        System.out.println("Attempting to delete order: " + orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            System.out.println("Order ID is null or empty");
            return false;
        }
        
        // Find the order to delete
        SenderOrder orderToDelete = getOrderById(orderId);
        if (orderToDelete == null) {
            System.out.println("Order not found for deletion: " + orderId);
            return false;
        }
        
        System.out.println("Found order: " + orderToDelete.getId() + " - Status: " + orderToDelete.getStatus());
        
        // Check if order can be deleted (not delivered)
        if ("Delivered".equals(orderToDelete.getStatus())) {
            System.out.println("Cannot delete delivered order: " + orderId);
            return false;
        }
        
        // Store original size for verification
        int originalSize = orders.size();
        System.out.println("Original orders list size: " + originalSize);
        
        // Remove from list
        boolean removed = orders.removeIf(o -> {
            boolean match = orderId.equals(o.getId());
            if (match) {
                System.out.println("Removing order: " + o.getId());
            }
            return match;
        });
        
        System.out.println("Order removed from list: " + removed);
        System.out.println("New orders list size: " + orders.size());
        
        if (removed) {
            // Save updated orders to file
            saveOrders();
            System.out.println("Order deleted successfully: " + orderId);
            
            // Force reload to verify
            refreshData();
            
            // Verify deletion
            SenderOrder verifyDeleted = getOrderById(orderId);
            if (verifyDeleted == null) {
                System.out.println("Verification: Order successfully deleted from system");
            } else {
                System.out.println("WARNING: Order still exists after deletion!");
            }
            
            // Also try to delete from main OrderStorage system
            try {
                logistics.orders.OrderStorage mainStorage = new logistics.orders.OrderStorage();
                logistics.orders.Order mainOrder = mainStorage.findOrder(orderId);
                if (mainOrder != null) {
                    // Delete from main system if possible
                    // Note: You may need to add a deleteOrder method to OrderStorage
                    System.out.println("Found order in main system, would sync deletion");
                }
            } catch (Exception e) {
                System.err.println("Error syncing deletion to main system: " + e.getMessage());
            }
            
            return true;
        }
        
        System.out.println("Failed to delete order: " + orderId);
        return false;
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
    
    public boolean updateOrderStatus(String orderId, String newStatus) {
        SenderOrder order = getOrderById(orderId);
        
        if (order != null) {
            order.setStatus(newStatus);
            saveOrders();
            
            try {
                logistics.orders.OrderStorage mainStorage = new logistics.orders.OrderStorage();
                logistics.orders.Order mainOrder = mainStorage.findOrder(orderId);
                if (mainOrder != null) {
                    mainOrder.status = newStatus;
                    mainStorage.updateOrder(mainOrder);
                    System.out.println("Synced status update to main system for order: " + orderId);
                }
            } catch (Exception e) {
                System.err.println("Error syncing status to main system: " + e.getMessage());
            }
            
            return true;
        }
        return false;
    }
    
    public boolean cancelOrder(String orderId) {
        SenderOrder order = getOrderById(orderId);
        
        if (order != null && !"Delivered".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus())) {
            order.setStatus("Cancelled");
            saveOrders();
            
            try {
                logistics.orders.OrderStorage mainStorage = new logistics.orders.OrderStorage();
                logistics.orders.Order mainOrder = mainStorage.findOrder(orderId);
                if (mainOrder != null) {
                    mainOrder.status = "Cancelled";
                    mainStorage.updateOrder(mainOrder);
                    System.out.println("Synced cancellation to main system for order: " + orderId);
                }
            } catch (Exception e) {
                System.err.println("Error syncing cancellation to main system: " + e.getMessage());
            }
            
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
            
            try {
                logistics.orders.OrderStorage mainStorage = new logistics.orders.OrderStorage();
                logistics.orders.Order mainOrder = mainStorage.findOrder(orderId);
                if (mainOrder != null) {
                    mainOrder.paymentStatus = status;
                    mainOrder.paymentMethod = paymentMethod;
                    mainOrder.transactionId = transactionId;
                    mainOrder.paymentDate = paymentDate;
                    mainStorage.updateOrder(mainOrder);
                    System.out.println("Synced payment update to main system for order: " + orderId);
                }
            } catch (Exception e) {
                System.err.println("Error syncing payment to main system: " + e.getMessage());
            }
            
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
        List<SenderOrder> orders = getOrdersByEmail(email);
        int pendingCount = (int) orders.stream()
            .filter(o -> "Pending".equals(o.getPaymentStatus()))
            .count();
        
        System.out.println("getPendingPayments(" + email + ") = " + pendingCount);
        for (SenderOrder order : orders) {
            if ("Pending".equals(order.getPaymentStatus())) {
                System.out.println("  - Order " + order.getId() + " has pending payment");
            }
        }
        return pendingCount;
    }
    
    public double getTotalSpent(String email) {
        List<SenderOrder> orders = getOrdersByEmail(email);
        double total = orders.stream()
            .filter(o -> "Paid".equals(o.getPaymentStatus()))
            .mapToDouble(o -> o.getEstimatedCost())
            .sum();
        
        System.out.println("getTotalSpent(" + email + ") = RM " + total);
        return total;
    }
    
    private double extractCostFromNotes(SenderOrder order) {
        return order.getEstimatedCost();
    }
    
    public void refreshData() {
        System.out.println("Refreshing sender data from main orders file...");
        loadOrders();
    }
}