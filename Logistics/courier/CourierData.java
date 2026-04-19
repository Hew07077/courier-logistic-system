// CourierData.java
package courier;

public class CourierData {
    public String id;
    public String name;
    public String phone;
    public String email;
    public String licenseNumber;
    public String licenseExpiry;
    public String status;
    public String vehicleId;
    public String joinDate;
    public int totalDeliveries;
    public double rating;
    public String emergencyContact;
    public String emergencyPhone;
    public String address;
    public String notes;
    public String photoPath;
    
    public static CourierData fromFileString(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 16) return null;
        
        try {
            CourierData d = new CourierData();
            d.id = parts[0];
            d.name = parts[1];
            d.phone = parts[2];
            d.email = parts[3];
            d.licenseNumber = parts[4];
            d.licenseExpiry = parts[5];
            d.status = parts[6];
            d.vehicleId = parts[7].isEmpty() ? null : parts[7];
            d.joinDate = parts[8];
            d.totalDeliveries = parts[9].isEmpty() ? 0 : Integer.parseInt(parts[9]);
            d.emergencyContact = parts[11].isEmpty() ? null : parts[11];
            d.emergencyPhone = parts[12].isEmpty() ? null : parts[12];
            d.address = parts[13].isEmpty() ? null : parts[13];
            d.notes = parts[14].isEmpty() ? null : parts[14];
            d.photoPath = parts[15].isEmpty() ? null : parts[15];
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}