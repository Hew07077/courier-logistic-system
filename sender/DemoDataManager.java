package sender;

import logistics.orders.Order;
import java.util.ArrayList;
import java.util.List;

public class DemoDataManager {
    public static final String DEMO_EMAIL = "demo@sender.com";
    private static DemoDataManager instance;
    private List<Order> demoOrders;
    private boolean demoDataInitialized = false;
    
    private DemoDataManager() {
        demoOrders = new ArrayList<>();
    }
    
    public static DemoDataManager getInstance() {
        if (instance == null) {
            instance = new DemoDataManager();
        }
        return instance;
    }
    
    public boolean isDemoAccount(String email) {
        if (email != null && DEMO_EMAIL.equalsIgnoreCase(email)) {
            return true;
        } else {
            return false;
        }
    }
    
    public void addDemoOrdersToSystem(FileDataManager fileDataManager) {
        if (!demoDataInitialized) {
            createDemoOrders();
            for (Order order : demoOrders) {
                fileDataManager.addOrder(order);
            }
            demoDataInitialized = true;
            System.out.println("Demo orders added to system: " + demoOrders.size() + " orders");
        } else {
            System.out.println("Demo data already initialized, skipping...");
        }
    }
    
    private void createDemoOrders() {
        demoOrders.clear();
        
        Order order1 = new Order(
            "20240315001",
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
        order1.status = "Delivered";
        order1.orderDate = "2024-03-15 10:30";
        order1.paymentStatus = "Paid";
        order1.paymentMethod = "Credit Card";
        order1.transactionId = "TXN1503202401";
        order1.paymentDate = "2024-03-15 10:35";
        order1.estimatedDelivery = "2024-03-18";
        order1.notes = "Package Type: Electronics\nEstimated Cost: RM 85.50\nEstimated Delivery: 3 business days\nDescription: Smartphone and accessories";
        demoOrders.add(order1);
        
        Order order2 = new Order(
            "20240316002",
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
        order2.status = "In Transit";
        order2.orderDate = "2024-03-16 14:45";
        order2.paymentStatus = "Paid";
        order2.paymentMethod = "PayPal";
        order2.transactionId = "TXN1603202401";
        order2.paymentDate = "2024-03-16 14:50";
        order2.estimatedDelivery = "2024-03-20";
        order2.driverId = "DRV-789";
        order2.notes = "Package Type: Documents\nEstimated Cost: RM 45.00\nEstimated Delivery: 3-5 business days\nDescription: Important business documents";
        demoOrders.add(order2);
        
        Order order3 = new Order(
            "20240317003",
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
        order3.status = "Pending";
        order3.orderDate = "2024-03-17 09:15";
        order3.paymentStatus = "Pending";
        order3.paymentMethod = "Not Selected";
        order3.estimatedDelivery = "2024-03-22";
        order3.notes = "Package Type: Fragile Items\nEstimated Cost: RM 120.75\nEstimated Delivery: 4-5 business days\nDescription: Glassware and ceramics";
        demoOrders.add(order3);
        
        Order order4 = new Order(
            "20240318004",
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
        order4.status = "In Transit";
        order4.orderDate = "2024-03-18 11:20";
        order4.paymentStatus = "Paid";
        order4.paymentMethod = "Debit Card";
        order4.transactionId = "TXN1803202401";
        order4.paymentDate = "2024-03-18 11:25";
        order4.estimatedDelivery = "2024-03-21";
        order4.driverId = "DRV-456";
        order4.notes = "Package Type: Clothing\nEstimated Cost: RM 35.00\nEstimated Delivery: 2-3 business days\nDescription: Summer clothes";
        demoOrders.add(order4);
        
        Order order5 = new Order(
            "20240319005",
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
        order5.status = "Delayed";
        order5.orderDate = "2024-03-19 16:30";
        order5.paymentStatus = "Pending";
        order5.paymentMethod = "Bank Transfer";
        order5.estimatedDelivery = "2024-03-25";
        order5.reason = "Weather conditions";
        order5.notes = "Package Type: Other\nEstimated Cost: RM 210.00\nEstimated Delivery: 5-7 business days\nDescription: Household items";
        demoOrders.add(order5);
    }
    
    public List<Order> getDemoOrders() {
        if (demoOrders.isEmpty()) {
            createDemoOrders();
            return demoOrders;
        } else {
            return demoOrders;
        }
    }
}
