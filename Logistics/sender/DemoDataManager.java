// DemoDataManager.java
package sender;

import java.util.ArrayList;
import java.util.List;

public class DemoDataManager {
    public static final String DEMO_EMAIL = "demo@sender.com";
    private static DemoDataManager instance;
    private List<SenderOrder> demoOrders;
    private boolean demoDataInitialized = false;
    
    private DemoDataManager() {
        demoOrders = new ArrayList<>();
        createDemoOrders(); // Create orders immediately when instance is created
    }
    
    public static DemoDataManager getInstance() {
        if (instance == null) {
            instance = new DemoDataManager();
        }
        return instance;
    }
    
    public boolean isDemoAccount(String email) {
        return email != null && DEMO_EMAIL.equalsIgnoreCase(email);
    }
    
    public void addDemoOrdersToSystem(SenderDataManager dataManager) {
        System.out.println("DemoDataManager.addDemoOrdersToSystem called - initialized: " + demoDataInitialized);
        
        if (!demoDataInitialized) {
            if (demoOrders.isEmpty()) {
                createDemoOrders();
            }
            
            int addedCount = 0;
            for (SenderOrder order : demoOrders) {
                // Check if order already exists to avoid duplicates
                SenderOrder existingOrder = dataManager.getOrderById(order.getId());
                if (existingOrder == null) {
                    dataManager.addOrder(order);
                    addedCount++;
                    System.out.println("Added demo order: " + order.getId() + " for " + order.getCustomerEmail());
                } else {
                    System.out.println("Order already exists: " + order.getId() + " - skipping");
                }
            }
            
            demoDataInitialized = true;
            System.out.println("Demo orders added to system: " + addedCount + " new orders added (total in system now: " + 
                dataManager.getOrdersByEmail(DEMO_EMAIL).size() + ")");
        } else {
            System.out.println("Demo data already initialized. Current demo orders in memory: " + demoOrders.size() + 
                ", in system: " + dataManager.getOrdersByEmail(DEMO_EMAIL).size());
            
            // Even if initialized, ensure demo orders exist in the system
            List<SenderOrder> existingOrders = dataManager.getOrdersByEmail(DEMO_EMAIL);
            if (existingOrders.isEmpty()) {
                System.out.println("No demo orders found in system despite initialization flag. Re-adding...");
                demoDataInitialized = false;
                addDemoOrdersToSystem(dataManager);
            }
        }
    }
    
    private void createDemoOrders() {
        demoOrders.clear();
        System.out.println("Creating demo orders...");
        
        // Order 1 - Delivered
        SenderOrder order1 = new SenderOrder(
            "ORD20240315001",
            "Demo Sender",
            "0123456789",
            DEMO_EMAIL,
            "123 Jalan SS2, Petaling Jaya, Selangor 47300",
            "Ahmad Abdullah",
            "0198765432",
            "45 Jalan Tebrau, Johor Bahru, Johor 80000",
            2.5,
            "30x20x15"
        );
        order1.setStatus("Delivered");
        order1.setOrderDate("2024-03-15 10:30");
        order1.setPaymentStatus("Paid");
        order1.setPaymentMethod("Credit Card");
        order1.setTransactionId("TXN1503202401");
        order1.setPaymentDate("2024-03-15 10:35");
        order1.setEstimatedDelivery("2024-03-18");
        order1.setNotes("Package Type: Electronics Estimated Cost: RM 85.50 Estimated Delivery: 3 business days Description: Smartphone and accessories");
        demoOrders.add(order1);
        
        // Order 2 - In Transit
        SenderOrder order2 = new SenderOrder(
            "ORD20240316002",
            "Demo Sender",
            "0123456789",
            DEMO_EMAIL,
            "123 Jalan SS2, Petaling Jaya, Selangor 47300",
            "Siti Nurhaliza",
            "0176543210",
            "78 Lebuh Pantai, George Town, Penang 10300",
            1.2,
            "25x18x10"
        );
        order2.setStatus("In Transit");
        order2.setOrderDate("2024-03-16 14:45");
        order2.setPaymentStatus("Paid");
        order2.setPaymentMethod("PayPal");
        order2.setTransactionId("TXN1603202401");
        order2.setPaymentDate("2024-03-16 14:50");
        order2.setEstimatedDelivery("2024-03-20");
        order2.setNotes("Package Type: Documents Estimated Cost: RM 45.00 Estimated Delivery: 3-5 business days Description: Important business documents");
        demoOrders.add(order2);
        
        // Order 3 - Pending
        SenderOrder order3 = new SenderOrder(
            "ORD20240317003",
            "Demo Sender",
            "0123456789",
            DEMO_EMAIL,
            "123 Jalan SS2, Petaling Jaya, Selangor 47300",
            "Raj Kumar",
            "0167890123",
            "15 Jalan Gasing, Ipoh, Perak 31400",
            3.8,
            "40x30x20"
        );
        order3.setStatus("Pending");
        order3.setOrderDate("2024-03-17 09:15");
        order3.setPaymentStatus("Pending");
        order3.setPaymentMethod("Not Selected");
        order3.setEstimatedDelivery("2024-03-22");
        order3.setNotes("Package Type: Fragile Items Estimated Cost: RM 120.75 Estimated Delivery: 4-5 business days Description: Glassware and ceramics");
        demoOrders.add(order3);
        
        // Order 4 - In Transit
        SenderOrder order4 = new SenderOrder(
            "ORD20240318004",
            "Demo Sender",
            "0123456789",
            DEMO_EMAIL,
            "123 Jalan SS2, Petaling Jaya, Selangor 47300",
            "Mei Ling",
            "0145678901",
            "88 Jalan Sultan Ismail, Kuala Lumpur 50250",
            0.8,
            "20x15x8"
        );
        order4.setStatus("In Transit");
        order4.setOrderDate("2024-03-18 11:20");
        order4.setPaymentStatus("Paid");
        order4.setPaymentMethod("Debit Card");
        order4.setTransactionId("TXN1803202401");
        order4.setPaymentDate("2024-03-18 11:25");
        order4.setEstimatedDelivery("2024-03-21");
        order4.setNotes("Package Type: Clothing Estimated Cost: RM 35.00 Estimated Delivery: 2-3 business days Description: Summer clothes");
        demoOrders.add(order4);
        
        // Order 5 - Delayed
        SenderOrder order5 = new SenderOrder(
            "ORD20240319005",
            "Demo Sender",
            "0123456789",
            DEMO_EMAIL,
            "123 Jalan SS2, Petaling Jaya, Selangor 47300",
            "Tan Wei Ming",
            "0187654321",
            "22 Jalan Merdeka, Kuching, Sarawak 93100",
            5.2,
            "50x40x30"
        );
        order5.setStatus("Delayed");
        order5.setOrderDate("2024-03-19 16:30");
        order5.setPaymentStatus("Pending");
        order5.setPaymentMethod("Bank Transfer");
        order5.setEstimatedDelivery("2024-03-25");
        order5.setNotes("Package Type: Other Estimated Cost: RM 210.00 Estimated Delivery: 5-7 business days Description: Household items Reason: Weather conditions");
        demoOrders.add(order5);
        
        System.out.println("Created " + demoOrders.size() + " demo orders");
    }
    
    public List<SenderOrder> getDemoOrders() {
        if (demoOrders.isEmpty()) {
            createDemoOrders();
        }
        return new ArrayList<>(demoOrders);
    }
}