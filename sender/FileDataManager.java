package sender;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileDataManager {
    private static FileDataManager instance;
    private List<Order> orders;
    private OrderStorage orderStorage;
    
    private FileDataManager() {
        orderStorage = new OrderStorage();
        orders = orderStorage.getAllOrders();
    }
    
    public static FileDataManager getInstance() {
        if (instance == null) {
            instance = new FileDataManager();
        }
        return instance;
    }
    
    public void addOrder(Order order) {
        orderStorage.addOrder(order);
        orders = orderStorage.getAllOrders();
    }
    
    public List<Order> getAllOrders() {
        return orderStorage.getAllOrders();
    }
    
    public List<Order> getOrdersByStatus(String status) {
        if (status.equals("All Orders")) {
            return getAllOrders();
        }
        return orderStorage.getOrdersByStatus(status);
    }
    
    public Order getOrderById(String orderId) {
        return orderStorage.findOrder(orderId);
    }
    
    public boolean cancelOrder(String orderId) {
        Order order = getOrderById(orderId);
        if (order != null && !order.status.equals("Delivered")) {
            order.status = "Cancelled";
            orderStorage.updateOrder(order);
            return true;
        }
        return false;
    }
    
    public boolean updateOrderStatus(String orderId, String newStatus) {
        Order order = getOrderById(orderId);
        if (order != null) {
            order.status = newStatus;
            orderStorage.updateOrder(order);
            return true;
        }
        return false;
    }
    
    public int getActiveOrders() {
        return orderStorage.getInTransitCount();
    }
    
    public int getPendingPayments() {
        return orderStorage.getPendingCount();
    }
}