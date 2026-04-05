// CourierDataLoader.java
package courier;

import java.io.*;
import java.util.*;

public class CourierDataLoader {
    private static final String DRIVER_FILE = "drivers.txt";
    private List<CourierData> couriers;
    
    public CourierDataLoader() {
        couriers = new ArrayList<>();
        loadCouriers();
    }
    
    private void loadCouriers() {
        File file = new File(DRIVER_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                CourierData d = CourierData.fromFileString(line);
                if (d != null) couriers.add(d);
            }
        } catch (IOException e) {
            // Silent fail
        }
    }
    
    public CourierData findCourierById(String id) {
        for (CourierData d : couriers) {
            if (d.id.equals(id)) return d;
        }
        return null;
    }
    
    public CourierData findCourierByName(String name) {
        for (CourierData d : couriers) {
            if (d.name.equalsIgnoreCase(name)) return d;
        }
        return null;
    }
    
    public List<CourierData> getAllCouriers() {
        return couriers;
    }
    
    public int getTotalCount() {
        return couriers.size();
    }
}