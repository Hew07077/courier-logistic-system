import javax.swing.*;
import java.awt.*;

public class AdminLogin extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLogin() {
        setTitle("logiXpress | Admin Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridLayout(1, 2));

        /* ============== LEFT BRAND PANEL ============== */
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(new Color(30, 136, 229)); // Blue
        brandPanel.setLayout(new GridBagLayout());

        JLabel brandName = new JLabel("logiXpress");
        brandName.setForeground(Color.WHITE);
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 42));

        JLabel slogan = new JLabel("Fast • Smart • Reliable Logistics");
        slogan.setForeground(Color.WHITE);
        slogan.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JPanel brandBox = new JPanel(new GridLayout(2, 1, 0, 10));
        brandBox.setBackground(new Color(30, 136, 229));
        brandBox.add(brandName);
        brandBox.add(slogan);

        brandPanel.add(brandBox);

        /* ============== RIGHT LOGIN PANEL ============== */
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(420, 420));
        card.setBackground(Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 30, 12, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(title, gbc);

        // Username
        gbc.gridy++;
        card.add(new JLabel("Username"), gbc);

        gbc.gridy++;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(300, 40));
        card.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        card.add(new JLabel("Password"), gbc);

        gbc.gridy++;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 40));
        card.add(passwordField, gbc);

        // Login button
        gbc.gridy++;
        JButton loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(255, 152, 0)); // Orange
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(300, 45));

        loginButton.addActionListener(e -> login());

        card.add(loginButton, gbc);

        loginPanel.add(card);

        /* ============== ADD TO FRAME ============== */
        root.add(brandPanel);
        root.add(loginPanel);

        add(root);
        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.equals("admin") && password.equals("admin123")) {
            JOptionPane.showMessageDialog(this, "Welcome to logiXpress!");
            dispose();
            new AdminDashboard(username);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Invalid username or password",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminLogin::new);
    }
}
