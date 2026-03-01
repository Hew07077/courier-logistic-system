package logistics.orders;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class OrderStorage {
    private static final String ORDER_FILE = "orders.txt";
    private List<Order> orders;
    private Map<String, Integer> dailyCounters;
    
    public OrderStorage() {
        orders = new ArrayList<>();
        dailyCounters = new HashMap<>();
        System.out.println("OrderStorage initialized. Looking for file: " + new File(ORDER_FILE).getAbsolutePath());
        loadOrders();
        
        // Debug: Print loaded orders
        System.out.println("Loaded " + orders.size() + " orders");
        for (Order o : orders) {
            System.out.println("Order: " + o.id + " - " + o.status);
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
        
        System.out.println("Orders file found. Loading orders...");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            orders.clear();
            String line;
            int lineCount = 0;
            int orderCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineCount++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    System.out.println("Skipping line " + lineCount + ": " + (line.isEmpty() ? "empty" : "comment"));
                    continue;
                }
                
                System.out.println("Parsing line " + lineCount + ": " + line.substring(0, Math.min(50, line.length())) + "...");
                Order o = Order.fromFileString(line);
                if (o != null) {
                    orders.add(o);
                    orderCount++;
                    updateDailyCounter(o.id);
                    System.out.println("Successfully loaded order: " + o.id);
                } else {
                    System.out.println("Failed to parse order from line: " + line);
                }
            }
            
            System.out.println("File reading complete. Processed " + lineCount + " lines, loaded " + orderCount + " orders");
            
            // If no orders were loaded, create sample data
            if (orders.isEmpty()) {
                System.out.println("No orders loaded from file. Creating sample data...");
                createSampleData();
                saveOrders();
            }
            
        } catch (IOException e) {
            System.out.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Creating sample data due to error...");
            createSampleData();
        }
    }
    
    private void updateDailyCounter(String orderId) {
        // Extract date from order ID: ORD20240301001
        Pattern pattern = Pattern.compile("ORD(\\d{8})(\\d{3})");
        Matcher matcher = pattern.matcher(orderId);
        if (matcher.matches()) {
            String date = matcher.group(1);
            int sequence = Integer.parseInt(matcher.group(2));
            Integer current = dailyCounters.get(date);
            if (current == null || sequence > current) {
                dailyCounters.put(date, sequence);
                System.out.println("Updated daily counter for " + date + " to " + sequence);
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
        
        String orderId = String.format("ORD%s%03d", today, nextSequence);
        dailyCounters.put(today, nextSequence);
        System.out.println("Generated new order ID: " + orderId);
        return orderId;
    }
    
    public void saveOrders() {
        System.out.println("Saving " + orders.size() + " orders to " + ORDER_FILE);
        System.out.println("File path: " + new File(ORDER_FILE).getAbsolutePath());
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ORDER_FILE))) {
            // Write header
            bw.write("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|driverId|vehicleId|weight|dimensions|notes|reason");
            bw.newLine();
            
            // Write orders
            for (Order o : orders) {
                String orderLine = o.toFileString();
                bw.write(orderLine);
                bw.newLine();
                System.out.println("Saved order: " + o.id);
            }
            
            System.out.println("Orders saved successfully to: " + new File(ORDER_FILE).getAbsolutePath());
            
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
            e.printStackTrace();
            
            // Try to save in user's home directory as fallback
            try {
                String homePath = System.getProperty("user.home") + File.separator + ORDER_FILE;
                System.out.println("Attempting to save to home directory: " + homePath);
                
                try (BufferedWriter bw2 = new BufferedWriter(new FileWriter(homePath))) {
                    bw2.write("# id|customerName|customerPhone|customerEmail|customerAddress|recipientName|recipientPhone|recipientAddress|status|orderDate|estimatedDelivery|driverId|vehicleId|weight|dimensions|notes|reason");
                    bw2.newLine();
                    
                    for (Order o : orders) {
                        bw2.write(o.toFileString());
                        bw2.newLine();
                    }
                    
                    System.out.println("Orders saved successfully to home directory");
                    
                    // Update the file path for future operations
                    // Note: You might want to make ORDER_FILE configurable
                }
            } catch (IOException e2) {
                System.out.println("Failed to save to home directory as well: " + e2.getMessage());
            }
        }
    }
    
    private void createSampleData() {
        System.out.println("Creating sample data...");
        orders.clear();
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            
            // Order 1 - Pending
            Order o1 = new Order(
                "ORD20240301001", 
                "John Doe", "555-123-4567", "john.doe@email.com", "123 Main St, New York, NY 10001",
                "Jane Smith", "555-987-6543", "456 Oak Ave, Los Angeles, CA 90001",
                2.5, "30x20x15"
            );
            o1.status = "Pending";
            o1.orderDate = "2024-03-01";
            o1.estimatedDelivery = "2024-03-04";
            orders.add(o1);
            System.out.println("Added sample order 1: " + o1.id);
            
            // Order 2 - In Transit
            Order o2 = new Order(
                "ORD20240301002",
                "Acme Corporation", "555-234-5678", "shipping@acme.com", "789 Business Park, Chicago, IL 60601",
                "Bob Wilson", "555-876-5432", "321 Industrial Rd, Detroit, MI 48201",
                15.0, "100x80x60"
            );
            o2.status = "In Transit";
            o2.orderDate = "2024-03-01";
            o2.estimatedDelivery = "2024-03-04";
            o2.driverId = "DRV001";
            o2.vehicleId = "TRK001";
            orders.add(o2);
            System.out.println("Added sample order 2: " + o2.id);
            
            // Order 3 - Delayed
            Order o3 = new Order(
                "ORD20240301003",
                "Alice Brown", "555-345-6789", "alice.brown@home.com", "555 Residential Ln, Houston, TX 77001",
                "Charlie Green", "555-765-4321", "777 Commerce St, Dallas, TX 75201",
                0.5, "20x15x10"
            );
            o3.status = "Delayed";
            o3.orderDate = "2024-03-01";
            o3.estimatedDelivery = "2024-03-02";
            o3.driverId = "DRV002";
            o3.reason = "Weather conditions - Heavy snow";
            orders.add(o3);
            System.out.println("Added sample order 3: " + o3.id);
            
            // Order 4 - Delivered
            Order o4 = new Order(
                "ORD20240228004",
                "Tech Solutions Inc", "555-456-7890", "orders@techsolutions.com", "123 Tech Park, San Francisco, CA 94105",
                "David Lee", "555-654-3210", "999 Silicon Valley Blvd, San Jose, CA 95110",
                8.2, "60x40x30"
            );
            o4.status = "Delivered";
            o4.orderDate = "2024-02-28";
            o4.estimatedDelivery = "2024-03-01";
            o4.driverId = "DRV003";
            o4.vehicleId = "VAN002";
            orders.add(o4);
            System.out.println("Added sample order 4: " + o4.id);
            
            // Order 5 - In Transit
            Order o5 = new Order(
                "ORD20240228005",
                "Global Imports", "555-567-8901", "logistics@global.com", "456 Harbor Dr, Miami, FL 33101",
                "Maria Garcia", "555-543-2109", "789 Beach Blvd, Tampa, FL 33601",
                22.5, "120x80x80"
            );
            o5.status = "In Transit";
            o5.orderDate = "2024-02-28";
            o5.estimatedDelivery = "2024-03-02";
            o5.driverId = "DRV004";
            o5.vehicleId = "TRK002";
            orders.add(o5);
            System.out.println("Added sample order 5: " + o5.id);
            
            // Order 6 - Pending
            Order o6 = new Order(
                "ORD20240228006",
                "Sarah Johnson", "555-678-9012", "sarah.j@email.com", "321 Pine St, Seattle, WA 98101",
                "Mike Thompson", "555-432-1098", "654 Mountain Rd, Portland, OR 97201",
                3.2, "40x30x20"
            );
            o6.status = "Pending";
            o6.orderDate = "2024-02-28";
            o6.estimatedDelivery = "2024-03-02";
            orders.add(o6);
            System.out.println("Added sample order 6: " + o6.id);
            
            // Update daily counters
            dailyCounters.put("20240301", 3);
            dailyCounters.put("20240228", 6);
            
            System.out.println("Sample data creation complete. Total orders: " + orders.size());
            
        } catch (Exception e) {
            System.out.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // CRUD Operations
    public List<Order> getAllOrders() { 
        // Sort by date descending (newest first)
        orders.sort((a, b) -> b.orderDate.compareTo(a.orderDate));
        System.out.println("getAllOrders() returning " + orders.size() + " orders");
        return orders; 
    }
    
    public Order findOrder(String id) {
        for (Order o : orders) {
            if (o.id.equals(id)) {
                System.out.println("Found order: " + id);
                return o;
            }
        }
        System.out.println("Order not found: " + id);
        return null;
    }
    
    public void addOrder(Order o) { 
        orders.add(o); 
        updateDailyCounter(o.id);
        System.out.println("Added new order: " + o.id);
        saveOrders();
    }
    
    public void addOrderFromSender(Object[] orderData) {
        try {
            String orderId = (String) orderData[0];
            String recipientName = (String) orderData[1];
            String status = (String) orderData[2];
            String orderDate = (String) orderData[3];
            String estimatedDelivery = (String) orderData[4];
            String senderName = (String) orderData[5];
            String senderEmail = (String) orderData[6];
            String senderPhone = (String) orderData[7];
            String senderAddress = (String) orderData[8];
            String recipientPhone = (String) orderData[9];
            String recipientAddress = (String) orderData[10];
            double weight = (Double) orderData[11];
            String dimensions = (String) orderData[12];
            
            Order order = new Order(
                orderId, senderName, senderPhone, senderEmail, senderAddress,
                recipientName, recipientPhone, recipientAddress, weight, dimensions,
                status, orderDate, estimatedDelivery
            );
            
            addOrder(order);
            System.out.println("Added order from sender data: " + orderId);
        } catch (Exception e) {
            System.out.println("Error adding order from sender: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void updateOrder(Order updatedOrder) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).id.equals(updatedOrder.id)) {
                orders.set(i, updatedOrder);
                System.out.println("Updated order: " + updatedOrder.id);
                saveOrders();
                return;
            }
        }
        System.out.println("Order not found for update: " + updatedOrder.id);
    }
    
    public void removeOrder(String id) { 
        boolean removed = orders.removeIf(o -> o.id.equals(id));
        if (removed) {
            System.out.println("Removed order: " + id);
            saveOrders();
        } else {
            System.out.println("Order not found for removal: " + id);
        }
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
    
    public List<Order> getDelayedOrders() {
        return orders.stream()
            .filter(o -> "Delayed".equals(o.status))
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByDate(String date) {
        return orders.stream()
            .filter(o -> o.orderDate.equals(date))
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByStatus(String status) {
        return orders.stream()
            .filter(o -> o.status.equals(status))
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByDriver(String driverId) {
        return orders.stream()
            .filter(o -> driverId.equals(o.driverId))
            .collect(Collectors.toList());
    }
    
    public Map<String, Integer> getStatusStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Pending", getPendingCount());
        stats.put("In Transit", getInTransitCount());
        stats.put("Delayed", getDelayedCount());
        stats.put("Delivered", getDeliveredCount());
        return stats;
    }
    
    public double getTotalWeight() {
        return orders.stream().mapToDouble(o -> o.weight).sum();
    }
    
    public double getAverageWeight() {
        if (orders.isEmpty()) return 0;
        return getTotalWeight() / orders.size();
    }
    
    public String generateNewId() {
        return generateOrderId();
    }
    
    // Debug method to check file status
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
                System.out.println("File content preview:");
                String line;
                int lineCount = 0;
                while ((line = br.readLine()) != null && lineCount < 5) {
                    System.out.println("  " + line);
                    lineCount++;
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
        System.out.println("==================");
    }
}