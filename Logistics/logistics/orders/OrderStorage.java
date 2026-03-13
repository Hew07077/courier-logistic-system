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
            System.out.println("Order: " + o.id + " - " + o.status + " - Driver: " + (o.driverId != null ? o.driverId : "None"));
        }
    }
    
    private void loadOrders() {
        File file = new File(ORDER_FILE);
        System.out.println("Checking for orders file at: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            System.out.println("Orders file not found. Creating sample data...");
            createSampleData();
            saveOrders();
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            orders.clear();
            String line;
            int lineCount = 0;
            int orderCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineCount++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                Order o = Order.fromFileString(line);
                if (o != null) {
                    orders.add(o);
                    orderCount++;
                    updateDailyCounter(o.id);
                }
            }
            
            System.out.println("File reading complete. Processed " + lineCount + " lines, loaded " + orderCount + " orders");
            
            if (orders.isEmpty()) {
                System.out.println("No orders loaded from file. Creating sample data...");
                createSampleData();
                saveOrders();
            }
            
        } catch (IOException e) {
            System.out.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            createSampleData();
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
            // Write header
            bw.write("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|actualDelivery|driverId|vehicleId|weight|dimensions|notes|reason|pickupTime|deliveryTime|distance|fuelUsed|deliveryPhoto|recipientSignature|onTime|paymentStatus|paymentMethod|transactionId|paymentDate");
            bw.newLine();
            
            for (Order o : orders) {
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
    
    /**
     * Add an order from the sender module
     */
    public void addOrderFromSender(Object senderOrder) {
        if (senderOrder == null) return;
        
        try {
            // Use reflection to get the order ID
            Class<?> clazz = senderOrder.getClass();
            String orderId = (String) clazz.getMethod("getId").invoke(senderOrder);
            
            // Check if order already exists
            Order existing = findOrder(orderId);
            if (existing != null) {
                System.out.println("Order already exists, updating: " + orderId);
                // Update existing order with sender data
                existing.customerName = (String) clazz.getMethod("getCustomerName").invoke(senderOrder);
                existing.customerPhone = (String) clazz.getMethod("getCustomerPhone").invoke(senderOrder);
                existing.customerEmail = (String) clazz.getMethod("getCustomerEmail").invoke(senderOrder);
                existing.customerAddress = (String) clazz.getMethod("getCustomerAddress").invoke(senderOrder);
                existing.recipientName = (String) clazz.getMethod("getRecipientName").invoke(senderOrder);
                existing.recipientPhone = (String) clazz.getMethod("getRecipientPhone").invoke(senderOrder);
                existing.recipientAddress = (String) clazz.getMethod("getRecipientAddress").invoke(senderOrder);
                existing.status = (String) clazz.getMethod("getStatus").invoke(senderOrder);
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
                
                updateOrder(existing);
            } else {
                // Convert and add new order
                Order order = Order.fromSenderOrder(senderOrder);
                if (order != null) {
                    orders.add(order);
                    updateDailyCounter(order.id);
                    saveOrders();
                    System.out.println("Added order from sender: " + order.id);
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding order from sender: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get all orders for a specific sender email
     */
    public List<Order> getOrdersBySenderEmail(String email) {
        if (email == null) return new ArrayList<>();
        
        return orders.stream()
            .filter(o -> email.equalsIgnoreCase(o.customerEmail))
            .collect(Collectors.toList());
    }
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        Order order = findOrder(orderId);
        if (order != null) {
            order.status = newStatus;
            saveOrders();
            return true;
        }
        return false;
    }
    
    /**
     * Update payment status
     */
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Calendar cal = Calendar.getInstance();
            
            // Order 1 - In Transit with DRV001
            Order o1 = new Order(
                "20240301001", 
                "John Doe", "555-123-4567", "john.doe@email.com", "123 Main St, New York, NY 10001",
                "Jane Smith", "555-987-6543", "456 Oak Ave, Los Angeles, CA 90001",
                2.5, "30x20x15"
            );
            o1.status = "In Transit";
            o1.orderDate = "2024-03-01 09:30";
            o1.estimatedDelivery = "2024-03-04";
            o1.driverId = "DRV001";
            o1.vehicleId = "TRK001";
            o1.pickupTime = "2024-03-01 09:30:00";
            o1.paymentStatus = "Paid";
            o1.paymentMethod = "Credit Card";
            o1.transactionId = "TXN" + System.currentTimeMillis();
            o1.paymentDate = "2024-03-01 09:35";
            o1.notes = "Package Type: Electronics Estimated Cost: RM 85.50";
            orders.add(o1);
            
            // Order 2 - In Transit with DRV001
            Order o2 = new Order(
                "20240301002",
                "Acme Corporation", "555-234-5678", "shipping@acme.com", "789 Business Park, Chicago, IL 60601",
                "Bob Wilson", "555-876-5432", "321 Industrial Rd, Detroit, MI 48201",
                15.0, "100x80x60"
            );
            o2.status = "In Transit";
            o2.orderDate = "2024-03-01 10:15";
            o2.estimatedDelivery = "2024-03-04";
            o2.driverId = "DRV001";
            o2.vehicleId = "TRK001";
            o2.pickupTime = "2024-03-01 10:15:00";
            o2.paymentStatus = "Paid";
            o2.paymentMethod = "Bank Transfer";
            o2.notes = "Package Type: Industrial Equipment Estimated Cost: RM 450.00";
            orders.add(o2);
            
            // Order 3 - Delayed with DRV002
            Order o3 = new Order(
                "20240301003",
                "Alice Brown", "555-345-6789", "alice.brown@home.com", "555 Residential Ln, Houston, TX 77001",
                "Charlie Green", "555-765-4321", "777 Commerce St, Dallas, TX 75201",
                0.5, "20x15x10"
            );
            o3.status = "Delayed";
            o3.orderDate = "2024-03-01 08:45";
            o3.estimatedDelivery = "2024-03-02";
            o3.driverId = "DRV002";
            o3.reason = "Weather conditions - Heavy snow";
            o3.pickupTime = "2024-03-01 08:45:00";
            o3.paymentStatus = "Paid";
            o3.paymentMethod = "PayPal";
            o3.notes = "Package Type: Documents Estimated Cost: RM 25.00 Reason: Weather conditions";
            orders.add(o3);
            
            // Update daily counters
            dailyCounters.put("20240301", 3);
            dailyCounters.put("20240302", 0);
            
            System.out.println("Sample data created with " + orders.size() + " orders");
            
        } catch (Exception e) {
            System.out.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // CRUD Operations
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
    
    public void updateOrder(Order updatedOrder) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).id.equals(updatedOrder.id)) {
                orders.set(i, updatedOrder);
                saveOrders();
                return;
            }
        }
    }
    
    public void removeOrder(String id) { 
        boolean removed = orders.removeIf(o -> o.id.equals(id));
        if (removed) {
            saveOrders();
        }
    }
    
    // Driver integration methods
    public boolean assignOrderToDriver(String orderId, String driverId) {
        Order order = findOrder(orderId);
        Driver driver = driverStorage.findDriver(driverId);
        
        if (order != null && driver != null && driver.isAvailable() && order.isAssignable()) {
            order.assignDriver(driverId);
            driver.assignOrder(orderId);
            driverStorage.updateDriver(driver);
            updateOrder(order);
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
                
                // Check if delivery was on time
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
                   ("In Transit".equals(o.status) || "Delayed".equals(o.status)))
            .collect(Collectors.toList());
    }
    
    public List<Order> getCompletedOrdersByDriver(String driverId) {
        return orders.stream()
            .filter(o -> driverId.equals(o.driverId) && "Delivered".equals(o.status))
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> getDriverPerformance(String driverId) {
        Map<String, Object> performance = new HashMap<>();
        List<Order> driverOrders = getOrdersByDriver(driverId);
        List<Order> completed = driverOrders.stream()
            .filter(o -> "Delivered".equals(o.status))
            .collect(Collectors.toList());
        
        performance.put("totalOrders", driverOrders.size());
        performance.put("completedOrders", completed.size());
        performance.put("pendingOrders", driverOrders.stream().filter(o -> "Pending".equals(o.status)).count());
        performance.put("inTransitOrders", driverOrders.stream().filter(o -> "In Transit".equals(o.status)).count());
        performance.put("delayedOrders", driverOrders.stream().filter(o -> "Delayed".equals(o.status)).count());
        
        double totalDistance = completed.stream().mapToDouble(o -> o.distance).sum();
        double totalFuel = completed.stream().mapToDouble(o -> o.fuelUsed).sum();
        long onTimeCount = completed.stream().filter(o -> o.onTime).count();
        
        performance.put("totalDistance", totalDistance);
        performance.put("totalFuel", totalFuel);
        performance.put("onTimeCount", onTimeCount);
        performance.put("onTimeRate", completed.isEmpty() ? 0 : (double) onTimeCount / completed.size());
        
        return performance;
    }
    
    // Statistics
    public int getTotalCount() {
        return orders.size();
    }
    
    public int getPendingCount() {
        return (int) orders.stream().filter(o -> "Pending".equals(o.status)).count();
    }
    
    public int getInTransitCount() {
        return (int) orders.stream().filter(o -> "In Transit".equals(o.status)).count();
    }
    
    public int getDelayedCount() {
        return (int) orders.stream().filter(o -> "Delayed".equals(o.status)).count();
    }
    
    public int getDeliveredCount() {
        return (int) orders.stream().filter(o -> "Delivered".equals(o.status)).count();
    }
    
    public int getCancelledCount() {
        return (int) orders.stream().filter(o -> "Cancelled".equals(o.status)).count();
    }
    
    public List<Order> getDelayedOrders() {
        return orders.stream()
            .filter(o -> "Delayed".equals(o.status) || o.isDelayed())
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByDate(String date) {
        return orders.stream()
            .filter(o -> o.orderDate.startsWith(date))
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByStatus(String status) {
        return orders.stream()
            .filter(o -> o.status.equals(status))
            .collect(Collectors.toList());
    }
    
    public List<Order> getPendingOrders() {
        return orders.stream()
            .filter(o -> "Pending".equals(o.status))
            .collect(Collectors.toList());
    }
    
    public Map<String, Integer> getStatusStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Pending", getPendingCount());
        stats.put("In Transit", getInTransitCount());
        stats.put("Delayed", getDelayedCount());
        stats.put("Delivered", getDeliveredCount());
        stats.put("Cancelled", getCancelledCount());
        return stats;
    }
    
    public double getTotalWeight() {
        return orders.stream().mapToDouble(o -> o.weight).sum();
    }
    
    public double getAverageWeight() {
        if (orders.isEmpty()) return 0;
        return getTotalWeight() / orders.size();
    }
    
    public double getTotalDistance() {
        return orders.stream().filter(o -> "Delivered".equals(o.status))
            .mapToDouble(o -> o.distance).sum();
    }
    
    public double getTotalFuelUsed() {
        return orders.stream().filter(o -> "Delivered".equals(o.status))
            .mapToDouble(o -> o.fuelUsed).sum();
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
            
            // Read and display first few lines
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int count = 0;
                System.out.println("\nFirst few lines:");
                while ((line = br.readLine()) != null && count < 5) {
                    count++;
                    if (line.startsWith("#")) {
                        System.out.println(count + ": " + line);
                    } else if (line.length() > 0) {
                        String[] parts = line.split("\\|");
                        if (parts.length > 0) {
                            System.out.println(count + ": " + parts[0] + " - " + 
                                               (parts.length > 1 ? parts[1] : "unknown"));
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