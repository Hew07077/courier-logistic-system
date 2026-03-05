package logistics.login;

import java.io.*;

public class CourierStorage {
    private static final String COURIER_FILE = "courier_data.txt";

    public void saveCourier(Login.CourierAccount courier) {
        if (courier == null) {
            System.err.println("Courier is null");
            return;
        }
        
        try (PrintWriter out = new PrintWriter(new FileWriter(COURIER_FILE, true))) {
            // Format: ID|Name|Email|Phone|IC|LicenseType|Status|RegistrationDate|PasswordHash|LicensePhoto|ICPhoto
            String line = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                courier.userId != null ? courier.userId : "",
                courier.fullName != null ? courier.fullName : "",
                courier.email != null ? courier.email : "",
                courier.phone != null ? courier.phone : "",
                courier.icNumber != null ? courier.icNumber : "",
                courier.licenseType != null ? courier.licenseType : "",
                courier.status != null ? courier.status : "PENDING",
                courier.registrationDate != null ? courier.registrationDate : "",
                courier.passwordHash != null ? courier.passwordHash : "",
                courier.licensePhotoPath != null ? courier.licensePhotoPath : "",
                courier.icPhotoPath != null ? courier.icPhotoPath : ""
            );
            out.println(line);
            System.out.println("Courier saved to " + COURIER_FILE + ": " + line);
        } catch (IOException e) {
            System.err.println("Error saving courier: " + e.getMessage());
            e.printStackTrace();
        }
    }
}