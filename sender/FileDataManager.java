package sender;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import java.util.ArrayList;
import java.util.List;

public class FileDataManager {
    private static FileDataManager instance;
    private OrderStorage orderStorage;
    private boolean demoDataInitialized = false;
    
    private FileDataManager() {
        orderStorage = new OrderStorage();
    }
    
    public static FileDataManager getInstance() {
        if (instance == null) {
            instance = new FileDataManager();
            return instance;
        } else {
            return instance;
        }
    }
    
    public void ensureDemoDataForEmail(String email) {
        if (email != null && DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(email)) {
            if (!demoDataInitialized) {
                DemoDataManager.getInstance().addDemoOrdersToSystem(this);
                demoDataInitialized = true;
                System.out.println("Demo data initialized for: " + email);
            } else {
                System.out.println("Demo data already initialized for: " + email);
            }
        } else {
            System.out.println("Regular user (not demo): " + email);
        }
    }
    
    public void addOrder(Order order) {
        if (order != null) {
            orderStorage.addOrder(order);
            System.out.println("Order added: " + order.id);
        } else {
            System.out.println("Cannot add null order");
        }
    }
    
    public List<Order> getAllOrders() {
        return orderStorage.getAllOrders();
    }
    
    public List<Order> getOrdersByStatus(String status) {
        if (status == null || "All Orders".equals(status)) {
            return getAllOrders();
        } else {
            return orderStorage.getOrdersByStatus(status);
        }
    }
    
    public Order getOrderById(String orderId) {
        if (orderId == null) {
            return null;
        } else {
            return orderStorage.findOrder(orderId);
        }
    }
    
    public boolean cancelOrder(String orderId) {
        if (orderId == null) {
            return false;
        }
        
        Order order = getOrderById(orderId);
        
        if (order != null) {
            if (!"Delivered".equals(order.status) && !"Cancelled".equals(order.status)) {
                order.status = "Cancelled";
                orderStorage.updateOrder(order);
                return true;
            } else {
                System.out.println("Cannot cancel order with status: " + order.status);
                return false;
            }
        } else {
            System.out.println("Order not found: " + orderId);
            return false;
        }
    }
    
    public boolean updateOrderStatus(String orderId, String newStatus) {
        if (orderId == null || newStatus == null) {
            return false;
        }
        
        Order order = getOrderById(orderId);
        
        if (order != null) {
            order.status = newStatus;
            orderStorage.updateOrder(order);
            return true;
        } else {
            System.out.println("Order not found: " + orderId);
            return false;
        }
    }
    
    public void saveOrders(List<Order> updatedOrders) {
        if (updatedOrders == null) {
            System.out.println("Cannot save null orders list");
            return;
        }
        
        for (Order order : updatedOrders) {
            orderStorage.updateOrder(order);
        }
        System.out.println("Saved " + updatedOrders.size() + " orders");
    }
    
    public List<Order> getOrdersByPaymentStatus(String paymentStatus) {
        if (paymentStatus == null) {
            return new ArrayList<>();
        } else {
            return orderStorage.getOrdersByPaymentStatus(paymentStatus);
        }
    }
    
    public List<Order> getOrdersByEmail(String email) {
        if (email == null) {
            return new ArrayList<>();
        }
        
        if (DemoDataManager.DEMO_EMAIL.equalsIgnoreCase(email)) {
            System.out.println("Getting demo orders for: " + email);
            return DemoDataManager.getInstance().getDemoOrders();
        } else {
            System.out.println("Getting regular orders for: " + email);
            return orderStorage.getOrdersByEmail(email);
        }
    }
    
    public int getActiveOrders(String email) {
        if (email == null) {
            return 0;
        }
        
        List<Order> userOrders = getOrdersByEmail(email);
        int count = 0;
        
        for (Order order : userOrders) {
            if (!"Delivered".equals(order.status) && !"Cancelled".equals(order.status)) {
                count++;
            }
        }
        return count;
    }
    
    public int getDeliveredOrders(String email) {
        if (email == null) {
            return 0;
        }
        
        List<Order> userOrders = getOrdersByEmail(email);
        int count = 0;
        
        for (Order order : userOrders) {
            if ("Delivered".equals(order.status)) {
                count++;
            }
        }
        return count;
    }
    
    public int getPendingPayments(String email) {
        if (email == null) {
            return 0;
        }
        
        List<Order> userOrders = getOrdersByEmail(email);
        int count = 0;
        
        for (Order order : userOrders) {
            if ("Pending".equals(order.paymentStatus)) {
                count++;
            }
        }
        return count;
    }
    
    public double getTotalSpent(String email) {
        if (email == null) {
            return 0.0;
        }
        
        List<Order> userOrders = getOrdersByEmail(email);
        double total = 0.0;
        
        for (Order order : userOrders) {
            if ("Paid".equals(order.paymentStatus)) {
                double amount = extractCostFromNotes(order.notes);
                total += amount;
            }
        }
        return total;
    }
    
    private double extractCostFromNotes(String notes) {
        if (notes == null) {
            return 0.0;
        }
        
        try {
            if (notes.contains("Estimated Cost:")) {
                String[] parts = notes.split("Estimated Cost: ");
                if (parts.length > 1) {
                    String[] costParts = parts[1].split("\n");
                    String costStr = costParts[0].replace("RM", "").replace("$", "").trim();
                    return Double.parseDouble(costStr);
                } else {
                    return 0.0;
                }
            } else {
                return 0.0;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing cost from notes: " + e.getMessage());
            return 0.0;
        }
    }
}