package logistics.orders;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class OrderStorage {
    private static final String ORDER_FILE = "orders.txt";
    private List<Order> orders;
    private Map<String, Integer> dailyCounters;
    private DriverStorage driverStorage;
    
    public OrderStorage() {
        orders = new ArrayList<>();
        dailyCounters = new HashMap<>();
        driverStorage = new DriverStorage();
        System.out.println("OrderStorage initialized. Looking for file: " + new File(ORDER_FILE).getAbsolutePath());
        loadOrders();
        
        System.out.println("Loaded " + orders.size() + " orders");
        for (Order o : orders) {
            System.out.println("Order: " + o.id + " - " + o.status + " - driverId: '" + o.driverId + "'");
        }
    }
    
    public void loadOrders() {
        File file = new File(ORDER_FILE);
        System.out.println("Loading orders from: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            System.out.println("Orders file not found. Creating sample data...");
            createSampleData();
            saveOrders();
            return;
        }
        
        List<Order> newOrders = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int orderCount = 0;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                Order o = Order.fromFileString(line);
                if (o != null) {
                    newOrders.add(o);
                    orderCount++;
                }
            }
            
            System.out.println("Loaded " + orderCount + " orders from file");
            
            orders.clear();
            orders.addAll(newOrders);
            
            dailyCounters.clear();
            for (Order o : orders) {
                updateDailyCounter(o.id);
            }
            
        } catch (IOException e) {
            System.out.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void forceReload() {
        System.out.println("Force reloading orders from file...");
        loadOrders();
        System.out.println("Reloaded " + orders.size() + " orders");
        for (Order o : orders) {
            System.out.println("  - " + o.id + ": " + o.status + " | driverId: '" + o.driverId + "'");
        }
    }
    
    private void updateDailyCounter(String orderId) {
        Pattern pattern = Pattern.compile("(\\d{8})(\\d{3})$");
        Matcher matcher = pattern.matcher(orderId);
        if (matcher.find()) {
            String date = matcher.group(1);
            int sequence = Integer.parseInt(matcher.group(2));
            Integer current = dailyCounters.get(date);
            if (current == null || sequence > current) {
                dailyCounters.put(date, sequence);
            }
        }
    }
    
    public String generateOrderId() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int nextSequence = 1;
        
        Integer lastSequence = dailyCounters.get(today);
        if (lastSequence != null) {
            nextSequence = lastSequence + 1;
        }
        
        String orderId = String.format("%s%03d", today, nextSequence);
        dailyCounters.put(today, nextSequence);
        return orderId;
    }
    
    public void saveOrders() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ORDER_FILE))) {
            bw.write("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|inTransitTime|outForDeliveryTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate");
            bw.newLine();
            
            for (Order o : orders) {
                System.out.println("Saving order " + o.id + " - driverId: '" + o.driverId + "', status: '" + o.status + "'");
                bw.write(o.toFileString());
                bw.newLine();
            }
            
            bw.flush();
            System.out.println("Saved " + orders.size() + " orders to " + ORDER_FILE);
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addOrderFromSender(Object senderOrder) {
        if (senderOrder == null) return;
        
        try {
            Class<?> clazz = senderOrder.getClass();
            String orderId = (String) clazz.getMethod("getId").invoke(senderOrder);
            String status = (String) clazz.getMethod("getStatus").invoke(senderOrder);
            
            Order existing = findOrder(orderId);
            if (existing != null) {
                System.out.println("Order already exists, updating: " + orderId);
                existing.customerName = (String) clazz.getMethod("getCustomerName").invoke(senderOrder);
                existing.customerPhone = (String) clazz.getMethod("getCustomerPhone").invoke(senderOrder);
                existing.customerEmail = (String) clazz.getMethod("getCustomerEmail").invoke(senderOrder);
                existing.customerAddress = (String) clazz.getMethod("getCustomerAddress").invoke(senderOrder);
                existing.recipientName = (String) clazz.getMethod("getRecipientName").invoke(senderOrder);
                existing.recipientPhone = (String) clazz.getMethod("getRecipientPhone").invoke(senderOrder);
                existing.recipientAddress = (String) clazz.getMethod("getRecipientAddress").invoke(senderOrder);
                existing.status = status;
                existing.orderDate = (String) clazz.getMethod("getOrderDate").invoke(senderOrder);
                existing.estimatedDelivery = (String) clazz.getMethod("getEstimatedDelivery").invoke(senderOrder);
                
                Object weightObj = clazz.getMethod("getWeight").invoke(senderOrder);
                if (weightObj != null) {
                    existing.weight = (Double) weightObj;
                }
                
                existing.dimensions = (String) clazz.getMethod("getDimensions").invoke(senderOrder);
                existing.notes = (String) clazz.getMethod("getNotes").invoke(senderOrder);
                existing.paymentStatus = (String) clazz.getMethod("getPaymentStatus").invoke(senderOrder);
                existing.paymentMethod = (String) clazz.getMethod("getPaymentMethod").invoke(senderOrder);
                existing.transactionId = (String) clazz.getMethod("getTransactionId").invoke(senderOrder);
                existing.paymentDate = (String) clazz.getMethod("getPaymentDate").invoke(senderOrder);
                
                if ("Pending".equals(status)) {
                    existing.pickupTime = null;
                    existing.inTransitTime = null;
                    existing.outForDeliveryTime = null;
                    existing.deliveryTime = null;
                    existing.actualDelivery = null;
                    existing.reason = null;
                    existing.driverId = null;
                    existing.vehicleId = null;
                    existing.distance = 0.0;
                    existing.fuelUsed = 0.0;
                    existing.deliveryPhoto = null;
                    existing.recipientSignature = null;
                    existing.onTime = true;
                    
                    System.out.println("  Cleared all status timestamps for existing order: " + orderId);
                }
                
                updateOrder(existing);
            } else {
                Order order = Order.fromSenderOrder(senderOrder);
                if (order != null) {
                    orders.add(order);
                    updateDailyCounter(order.id);
                    saveOrders();
                    System.out.println("Added new order from sender: " + order.id);
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding order from sender: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Order> getOrdersBySenderEmail(String email) {
        if (email == null) return new ArrayList<>();
        
        return orders.stream()
            .filter(o -> email.equalsIgnoreCase(o.customerEmail))
            .collect(Collectors.toList());
    }
    
    public boolean updateOrderStatus(String orderId, String newStatus) {
        Order order = findOrder(orderId);
        if (order != null) {
            order.status = newStatus;
            saveOrders();
            return true;
        }
        return false;
    }
    
    public boolean updatePaymentStatus(String orderId, String paymentStatus, 
                                      String paymentMethod, String transactionId, 
                                      String paymentDate) {
        Order order = findOrder(orderId);
        if (order != null) {
            order.paymentStatus = paymentStatus;
            order.paymentMethod = paymentMethod;
            order.transactionId = transactionId;
            order.paymentDate = paymentDate;
            saveOrders();
            return true;
        }
        return false;
    }
    
    private void createSampleData() {
        orders.clear();
        
        try {
            // Order 1 - Pending
            Order o1 = new Order(
                "20240301001", 
                "John Doe", "012-3456789", "john.doe@email.com", "123 Main St, Petaling Jaya, Selangor 46100",
                "Jane Smith", "012-9876543", "456 Oak Ave, George Town, Penang 10000",
                2.5, "30x20x15"
            );
            o1.status = "Pending";
            o1.orderDate = "2024-03-01 09:30";
            o1.estimatedDelivery = "2024-03-04";
            o1.paymentStatus = "Paid";
            o1.paymentMethod = "Credit Card";
            o1.transactionId = "TXN001";
            o1.paymentDate = "2024-03-01 09:35";
            o1.notes = "Package Type: Electronics; Estimated Cost: RM 85.50; Description: Laptop; Total Amount: RM 85.50";
            orders.add(o1);
            
            // Order 2 - Assigned
            Order o2 = new Order(
                "20240301002",
                "Acme Corporation", "013-4567890", "shipping@acme.com", "789 Business Park, Shah Alam, Selangor 40000",
                "Bob Wilson", "013-7654321", "321 Industrial Rd, Johor Bahru, Johor 80000",
                15.0, "100x80x60"
            );
            o2.status = "Assigned";
            o2.orderDate = "2024-03-01 10:15";
            o2.estimatedDelivery = "2024-03-04";
            o2.driverId = "DRV001";
            o2.vehicleId = "TRK001";
            o2.paymentStatus = "Paid";
            o2.paymentMethod = "Bank Transfer";
            o2.transactionId = "TXN002";
            o2.paymentDate = "2024-03-01 10:20";
            o2.notes = "Package Type: Industrial Equipment; Estimated Cost: RM 450.00; Description: Machinery parts; Total Amount: RM 450.00";
            orders.add(o2);
            
            // Order 3 - Picked Up
            Order o3 = new Order(
                "20240301003",
                "Alice Brown", "014-5678901", "alice.brown@home.com", "555 Residential Ln, Kuala Lumpur 50000",
                "Charlie Green", "014-6543210", "777 Commerce St, Ipoh, Perak 30000",
                0.5, "20x15x10"
            );
            o3.status = "Picked Up";
            o3.orderDate = "2024-03-01 08:45";
            o3.estimatedDelivery = "2024-03-04";
            o3.driverId = "DRV002";
            o3.vehicleId = "VAN001";
            o3.pickupTime = "2024-03-01 08:45:00";
            o3.paymentStatus = "Paid";
            o3.paymentMethod = "PayPal";
            o3.transactionId = "TXN003";
            o3.paymentDate = "2024-03-01 08:50";
            o3.notes = "Package Type: Documents; Estimated Cost: RM 25.00; Description: Legal documents; Total Amount: RM 25.00";
            orders.add(o3);
            
            // Order 4 - In Transit
            Order o4 = new Order(
                "20240301004",
                "David Tan", "015-6789012", "david.tan@email.com", "123 Jalan SS2, Petaling Jaya, Selangor 47300",
                "Elena Wong", "015-7890123", "456 Jalan Ipoh, Kuala Lumpur 51200",
                3.2, "40x30x20"
            );
            o4.status = "In Transit";
            o4.orderDate = "2024-03-01 11:00";
            o4.estimatedDelivery = "2024-03-04";
            o4.driverId = "DRV003";
            o4.vehicleId = "CAR001";
            o4.pickupTime = "2024-03-01 11:30:00";
            o4.inTransitTime = "2024-03-01 13:00:00";
            o4.paymentStatus = "Paid";
            o4.paymentMethod = "Credit Card";
            o4.transactionId = "TXN004";
            o4.paymentDate = "2024-03-01 11:05";
            o4.notes = "Package Type: Fragile Items; Estimated Cost: RM 75.00; Description: Glassware; Total Amount: RM 75.00";
            orders.add(o4);
            
            // Order 5 - Out for Delivery
            Order o5 = new Order(
                "20240301005",
                "Fiona Lim", "016-7890123", "fiona.lim@email.com", "789 Jalan Gasing, Petaling Jaya, Selangor 46000",
                "George Khoo", "016-8901234", "321 Jalan Meru, Klang, Selangor 41000",
                1.8, "25x20x15"
            );
            o5.status = "Out for Delivery";
            o5.orderDate = "2024-03-01 13:00";
            o5.estimatedDelivery = "2024-03-03";
            o5.driverId = "DRV004";
            o5.vehicleId = "MTC001";
            o5.pickupTime = "2024-03-01 13:30:00";
            o5.inTransitTime = "2024-03-01 14:00:00";
            o5.outForDeliveryTime = "2024-03-02 09:00:00";
            o5.paymentStatus = "Paid";
            o5.paymentMethod = "Touch 'n Go";
            o5.transactionId = "TXN005";
            o5.paymentDate = "2024-03-01 13:10";
            o5.notes = "Package Type: Clothing; Estimated Cost: RM 45.00; Description: Fashion items; Total Amount: RM 45.00";
            orders.add(o5);
            
            // Order 6 - Delivered
            Order o6 = new Order(
                "20240228001",
                "Henry Ng", "017-8901234", "henry.ng@email.com", "456 Jalan PJS, Subang Jaya, Selangor 47500",
                "Irene Chang", "017-9012345", "789 Jalan SS15, Subang Jaya, Selangor 47500",
                0.8, "15x15x10"
            );
            o6.status = "Delivered";
            o6.orderDate = "2024-02-28 14:00";
            o6.estimatedDelivery = "2024-03-02";
            o6.actualDelivery = "2024-03-01";
            o6.driverId = "DRV005";
            o6.vehicleId = "VAN002";
            o6.pickupTime = "2024-02-28 14:30:00";
            o6.inTransitTime = "2024-02-28 15:00:00";
            o6.outForDeliveryTime = "2024-03-01 08:30:00";
            o6.deliveryTime = "2024-03-01 14:30:00";
            o6.distance = 15.5;
            o6.fuelUsed = 2.3;
            o6.onTime = true;
            o6.paymentStatus = "Paid";
            o6.paymentMethod = "GrabPay";
            o6.transactionId = "TXN006";
            o6.paymentDate = "2024-02-28 14:10";
            o6.notes = "Package Type: Documents; Estimated Cost: RM 20.00; Description: Contracts; Total Amount: RM 20.00";
            orders.add(o6);
            
            // Order 7 - Failed
            Order o7 = new Order(
                "20240301006",
                "Julia Tan", "018-9012345", "julia.tan@email.com", "111 Jalan Ampang, Kuala Lumpur 50450",
                "Kevin Lee", "018-0123456", "222 Jalan Bukit Bintang, Kuala Lumpur 55100",
                1.2, "20x15x15"
            );
            o7.status = "Failed";
            o7.orderDate = "2024-03-01 09:00";
            o7.estimatedDelivery = "2024-03-02";
            o7.driverId = "DRV001";
            o7.vehicleId = "TRK001";
            o7.pickupTime = "2024-03-01 10:00:00";
            o7.deliveryTime = "2024-03-01 15:30:00";
            o7.reason = "Recipient not available - Attempted delivery at 3pm, no one home";
            o7.paymentStatus = "Paid";
            o7.paymentMethod = "Credit Card";
            o7.transactionId = "TXN007";
            o7.paymentDate = "2024-03-01 09:15";
            o7.notes = "Package Type: Electronics; Estimated Cost: RM 55.00; Description: Phone charger; Total Amount: RM 55.00";
            orders.add(o7);
            
            dailyCounters.put("20240301", 6);
            dailyCounters.put("20240228", 1);
            
            System.out.println("Sample data created with " + orders.size() + " orders");
            
        } catch (Exception e) {
            System.out.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== CRUD Operations ====================
    
    public List<Order> getAllOrders() { 
        orders.sort((a, b) -> b.orderDate.compareTo(a.orderDate));
        return orders; 
    }
    
    public Order findOrder(String id) {
        for (Order o : orders) {
            if (o.id.equals(id)) return o;
        }
        return null;
    }
    
    public void addOrder(Order o) { 
        orders.add(o); 
        updateDailyCounter(o.id);
        saveOrders();
    }
    

    public synchronized void updateOrder(Order updatedOrder) {
        loadOrders();
        
        System.out.println("===== updateOrder called =====");
        System.out.println("Order ID: " + updatedOrder.id);
        System.out.println("  - driverId: '" + updatedOrder.driverId + "'");
        System.out.println("  - vehicleId: '" + updatedOrder.vehicleId + "'");
        System.out.println("  - status: '" + updatedOrder.status + "'");
        
        boolean found = false;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).id.equals(updatedOrder.id)) {
                System.out.println("Updating existing order in memory at index " + i);
                orders.set(i, updatedOrder);
                found = true;
                break;
            }
        }
        
        if (!found) {
            System.out.println("Order not found in memory, adding as new order");
            orders.add(updatedOrder);
            updateDailyCounter(updatedOrder.id);
        }
        

        saveOrders();
        

        Order verifyOrder = findOrder(updatedOrder.id);
        if (verifyOrder != null) {
            System.out.println("Verification - driverId: '" + verifyOrder.driverId + "', status: '" + verifyOrder.status + "'");
        }
        
        System.out.println("Order " + updatedOrder.id + " updated and saved successfully");
    }
    
    public void removeOrder(String id) { 
        boolean removed = orders.removeIf(o -> o.id.equals(id));
        if (removed) {
            saveOrders();
        }
    }
    

    public boolean assignOrderToDriver(String orderId, String driverId) {
        System.out.println("===== assignOrderToDriver called =====");
        System.out.println("Order ID: " + orderId);
        System.out.println("Driver ID: " + driverId);
        
        Order order = findOrder(orderId);
        if (order == null) {
            System.err.println("ERROR: Order not found: " + orderId);
            return false;
        }
        
        System.out.println("Order found - Status: " + order.status);
        System.out.println("Order current driverId: " + order.driverId);
        System.out.println("Order isAssignable: " + order.isAssignable());
        
        Driver driver = driverStorage.findDriver(driverId);
        if (driver == null) {
            System.err.println("ERROR: Driver not found: " + driverId);
            return false;
        }
        
        System.out.println("Driver found - Name: " + driver.name);
        System.out.println("Driver isAvailable: " + driver.isAvailable());
        
        if (!driver.isAvailable()) {
            System.err.println("ERROR: Driver is not available. Status: " + driver.workStatus);
            return false;
        }
        

        if (!order.isAssignable()) {
            System.err.println("ERROR: Order is not assignable. Status: " + order.status);
            return false;
        }
 
        System.out.println("Assigning driver " + driverId + " to order " + orderId);
        
        order.driverId = driverId;
        

        if (!"Assigned".equals(order.status) && !"Picked Up".equals(order.status) 
            && !"In Transit".equals(order.status) && !"Out for Delivery".equals(order.status)) {
            order.status = "Assigned";
        }
        
        System.out.println("After assignment - Order status: " + order.status);
        System.out.println("After assignment - Order driverId: " + order.driverId);
        

        driver.assignOrder(orderId);
        driverStorage.updateDriver(driver);
        
        updateOrder(order);
        
        System.out.println("Verifying save...");
        Order verifyOrder = findOrder(orderId);
        if (verifyOrder != null && driverId.equals(verifyOrder.driverId)) {
            System.out.println("SUCCESS: Order " + orderId + " assigned to driver " + driverId);
            System.out.println("  - Verified driverId: " + verifyOrder.driverId);
            System.out.println("  - Verified status: " + verifyOrder.status);
            return true;
        } else {
            System.err.println("FAILED: Verification failed - Order driverId is " + 
                              (verifyOrder != null ? verifyOrder.driverId : "null"));
            return false;
        }
    }

    public boolean assignOrderToDriver(String orderId, String driverId, String vehicleId) {
        System.out.println("===== assignOrderToDriver (with vehicle) called =====");
        System.out.println("Order ID: " + orderId);
        System.out.println("Driver ID: " + driverId);
        System.out.println("Vehicle ID: " + vehicleId);
        
        Order order = findOrder(orderId);
        if (order == null) {
            System.err.println("ERROR: Order not found: " + orderId);
            return false;
        }
        
        Driver driver = driverStorage.findDriver(driverId);
        if (driver == null) {
            System.err.println("ERROR: Driver not found: " + driverId);
            return false;
        }
        
        if (!driver.isAvailable()) {
            System.err.println("ERROR: Driver is not available");
            return false;
        }
        
        if (!order.isAssignable()) {
            System.err.println("ERROR: Order is not assignable. Status: " + order.status);
            return false;
        }
        
        // 直接设置字段
        order.driverId = driverId;
        order.vehicleId = vehicleId;
        
        if (!"Assigned".equals(order.status) && !"Picked Up".equals(order.status) 
            && !"In Transit".equals(order.status) && !"Out for Delivery".equals(order.status)) {
            order.status = "Assigned";
        }
        
        System.out.println("After assignment - Order status: " + order.status);
        System.out.println("After assignment - Order driverId: " + order.driverId);
        System.out.println("After assignment - Order vehicleId: " + order.vehicleId);
        
        driver.assignOrder(orderId);
        driverStorage.updateDriver(driver);
        updateOrder(order);
        
        Order verifyOrder = findOrder(orderId);
        if (verifyOrder != null && driverId.equals(verifyOrder.driverId)) {
            System.out.println("SUCCESS: Order " + orderId + " assigned with vehicle " + vehicleId);
            return true;
        }
        return false;
    }
    
    public boolean completeOrder(String orderId, double distance, double fuelUsed, String photoPath, String signature) {
        Order order = findOrder(orderId);
        if (order != null && order.driverId != null) {
            Driver driver = driverStorage.findDriver(order.driverId);
            
            if (driver != null) {
                order.markAsDelivered(distance, fuelUsed, photoPath, signature);
                
                boolean onTime = order.onTime;
                driver.completeOrder(orderId, onTime, distance, fuelUsed);
                
                driverStorage.updateDriver(driver);
                updateOrder(order);
                return true;
            }
        }
        return false;
    }
    
    public boolean delayOrder(String orderId, String reason) {
        Order order = findOrder(orderId);
        if (order != null) {
            order.markAsDelayed(reason);
            updateOrder(order);
            return true;
        }
        return false;
    }
    
    public List<Order> getOrdersByDriver(String driverId) {
        return orders.stream()
            .filter(o -> driverId.equals(o.driverId))
            .collect(Collectors.toList());
    }
    
    public List<Order> getActiveOrdersByDriver(String driverId) {
        return orders.stream()
            .filter(o -> driverId.equals(o.driverId) && 
                   ("In Transit".equals(o.status) || "Delayed".equals(o.status) || "Assigned".equals(o.status) || "Picked Up".equals(o.status) || "Out for Delivery".equals(o.status)))
            .collect(Collectors.toList());
    }
    
    public List<Order> getCompletedOrdersByDriver(String driverId) {
        return orders.stream()
            .filter(o -> driverId.equals(o.driverId) && "Delivered".equals(o.status))
            .collect(Collectors.toList());
    }
    
    // ==================== Statistics Methods ====================
    
    public int getTotalCount() { return orders.size(); }
    public int getPendingCount() { return (int) orders.stream().filter(o -> "Pending".equals(o.status)).count(); }
    public int getAssignedCount() { return (int) orders.stream().filter(o -> "Assigned".equals(o.status)).count(); }
    public int getPickupCount() { return (int) orders.stream().filter(o -> "Picked Up".equals(o.status)).count(); }
    public int getInTransitCount() { return (int) orders.stream().filter(o -> "In Transit".equals(o.status)).count(); }
    public int getOutForDeliveryCount() { return (int) orders.stream().filter(o -> "Out for Delivery".equals(o.status)).count(); }
    public int getDelayedCount() { return (int) orders.stream().filter(o -> "Delayed".equals(o.status)).count(); }
    public int getDeliveredCount() { return (int) orders.stream().filter(o -> "Delivered".equals(o.status)).count(); }
    public int getCancelledCount() { return (int) orders.stream().filter(o -> "Cancelled".equals(o.status)).count(); }
    public int getFailedCount() { return (int) orders.stream().filter(o -> "Failed".equals(o.status)).count(); }
    
    // ==================== Filter Methods ====================
    
    public List<Order> getAssignedOrders() { 
        return orders.stream().filter(o -> "Assigned".equals(o.status)).collect(Collectors.toList()); 
    }
    
    public List<Order> getPickupOrders() { 
        return orders.stream().filter(o -> "Picked Up".equals(o.status)).collect(Collectors.toList()); 
    }
    
    public List<Order> getOutForDeliveryOrders() { 
        return orders.stream().filter(o -> "Out for Delivery".equals(o.status)).collect(Collectors.toList()); 
    }
    
    public List<Order> getInTransitOrders() { 
        return orders.stream().filter(o -> "In Transit".equals(o.status)).collect(Collectors.toList()); 
    }
    
    public List<Order> getDelayedOrders() { 
        return orders.stream().filter(o -> "Delayed".equals(o.status) || o.isDelayed()).collect(Collectors.toList()); 
    }
    
    public List<Order> getOrdersByDate(String date) { 
        return orders.stream().filter(o -> o.orderDate.startsWith(date)).collect(Collectors.toList()); 
    }
    
    public List<Order> getOrdersByStatus(String status) { 
        return orders.stream().filter(o -> o.status.equals(status)).collect(Collectors.toList()); 
    }
    
    public List<Order> getPendingOrders() { 
        return orders.stream().filter(o -> "Pending".equals(o.status)).collect(Collectors.toList()); 
    }
    
    public Map<String, Integer> getStatusStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Pending", getPendingCount());
        stats.put("Assigned", getAssignedCount());
        stats.put("Picked Up", getPickupCount());
        stats.put("In Transit", getInTransitCount());
        stats.put("Out for Delivery", getOutForDeliveryCount());
        stats.put("Delayed", getDelayedCount());
        stats.put("Delivered", getDeliveredCount());
        stats.put("Cancelled", getCancelledCount());
        stats.put("Failed", getFailedCount());
        return stats;
    }
    
    public double getTotalWeight() { 
        return orders.stream().mapToDouble(o -> o.weight).sum(); 
    }
    
    public double getAverageWeight() { 
        return orders.isEmpty() ? 0 : getTotalWeight() / orders.size(); 
    }
    
    public double getTotalDistance() { 
        return orders.stream().filter(o -> "Delivered".equals(o.status)).mapToDouble(o -> o.distance).sum(); 
    }
    
    public double getTotalFuelUsed() { 
        return orders.stream().filter(o -> "Delivered".equals(o.status)).mapToDouble(o -> o.fuelUsed).sum(); 
    }
    
    public String generateNewId() { 
        return generateOrderId(); 
    }
    
    public void checkFileStatus() {
        File file = new File(ORDER_FILE);
        System.out.println("=== File Status ===");
        System.out.println("File path: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        if (file.exists()) {
            System.out.println("File size: " + file.length() + " bytes");
            System.out.println("File can read: " + file.canRead());
            System.out.println("File can write: " + file.canWrite());
            
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int count = 0;
                System.out.println("\nFirst few lines:");
                while ((line = br.readLine()) != null && count < 10) {
                    count++;
                    if (line.startsWith("#")) {
                        System.out.println(count + ": " + line.substring(0, Math.min(100, line.length())) + "...");
                    } else if (line.length() > 0) {
                        String[] parts = line.split("\\|");
                        if (parts.length > 0) {
                            System.out.println(count + ": " + parts[0] + " - driverId: " + 
                                       (parts.length > 12 ? "'" + parts[12] + "'" : "N/A"));
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
        System.out.println("==================");
    }
}