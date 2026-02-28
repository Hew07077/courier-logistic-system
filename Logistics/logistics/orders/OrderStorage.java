package logistics.orders;

import java.io.*;
import java.util.*;

public class OrderStorage {
    private static final String ORDER_FILE = "orders.txt";
    private List<Order> orders;
    private int lastOrderNumber = 0;
    
    public OrderStorage() {
        orders = new ArrayList<>();
        loadOrders();
    }
    
    private void loadOrders() {
        File file = new File(ORDER_FILE);
        if (!file.exists()) {
            createSampleData();
            saveOrders();
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            orders.clear();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Order o = Order.fromFileString(line);
                if (o != null) {
                    orders.add(o);
                    // 提取订单号数字
                    try {
                        int num = Integer.parseInt(o.id.replace("ORD", ""));
                        if (num > lastOrderNumber) lastOrderNumber = num;
                    } catch (NumberFormatException e) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            createSampleData();
        }
    }
    
    public void saveOrders() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ORDER_FILE))) {
            bw.write("# id|customer|phone|address|status|date|estDate|driver|vehicle|reason\n");
            for (Order o : orders) {
                bw.write(o.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createSampleData() {
        orders.clear();
        
        Order o1 = new Order("ORD001", "John Doe", "123-456-7890", "123 Main St, NY");
        o1.status = "Pending";
        
        Order o2 = new Order("ORD002", "Jane Smith", "234-567-8901", "456 Oak Ave, LA");
        o2.status = "In Transit";
        o2.estDate = "2024-03-01";
        o2.driver = "DRV001";
        o2.vehicle = "VH001";
        
        Order o3 = new Order("ORD003", "Bob Johnson", "345-678-9012", "789 Pine Rd, CHI");
        o3.status = "Delayed";
        o3.reason = "Weather";
        o3.driver = "DRV002";
        
        Order o4 = new Order("ORD004", "Alice Brown", "456-789-0123", "321 Elm St, HOU");
        o4.status = "Delivered";
        o4.estDate = "2024-02-28";
        
        orders.add(o1);
        orders.add(o2);
        orders.add(o3);
        orders.add(o4);
        
        lastOrderNumber = 4;
    }
    
    public String generateNewId() {
        lastOrderNumber++;
        return String.format("ORD%03d", lastOrderNumber);
    }
    
    public List<Order> getAllOrders() { return orders; }
    public Order findOrder(String id) {
        for (Order o : orders) if (o.id.equals(id)) return o;
        return null;
    }
    public void addOrder(Order o) { orders.add(o); }
    public void removeOrder(String id) { orders.removeIf(o -> o.id.equals(id)); }
    public int getPendingCount() {
        return (int) orders.stream().filter(o -> "Pending".equals(o.status)).count();
    }
    public int getInTransitCount() {
        return (int) orders.stream().filter(o -> "In Transit".equals(o.status)).count();
    }
    public int getDelayedCount() {
        return (int) orders.stream().filter(o -> "Delayed".equals(o.status)).count();
    }
    public List<Order> getDelayedOrders() {
        List<Order> list = new ArrayList<>();
        for (Order o : orders) if ("Delayed".equals(o.status)) list.add(o);
        return list;
    }
}