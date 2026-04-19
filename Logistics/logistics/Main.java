package logistics;

import javax.swing.*;
import java.io.File;

import logistics.login.Login;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== APPLICATION STARTUP ===");
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("Orders file will be at: " + new File("orders.txt").getAbsolutePath());
        System.out.println("===========================\n");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}