package courier;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CompleteDeliveryPanel extends JPanel {
    
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color WARNING = new Color(225, 173, 1);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color INFO = new Color(23, 162, 184);
    
    private JComboBox<String> orderSelectCombo;
    private JComboBox<String> statusCombo;
    private JTextArea signatureArea;
    private JLabel photoFileNameLabel;
    private File deliveryPhotoFile;
    private JLabel orderDetailsLabel;
    private JPanel photoPanel;
    private JLabel photoLabel;
    
    private List<Order> myOrders;
    private OrderStorage orderStorage;
    private CourierDashboard parentDashboard;
    
    public CompleteDeliveryPanel(List<Order> orders, OrderStorage orderStorage, CourierDashboard parent) {
        this.myOrders = orders;
        this.orderStorage = orderStorage;
        this.parentDashboard = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        initUI();
    }
    
    public void refreshData(List<Order> updatedOrders) {
        this.myOrders = updatedOrders;
        refreshOrderSelectCombo();
    }
    
    public void clearForm() {
        if (orderSelectCombo != null) {
            orderSelectCombo.setSelectedIndex(-1); // Reset to placeholder
        }
        if (signatureArea != null) signatureArea.setText("");
        deliveryPhotoFile = null;
        if (photoFileNameLabel != null) {
            photoFileNameLabel.setText("No file selected");
            photoFileNameLabel.setForeground(TEXT_GRAY);
        }
        if (photoLabel != null && photoPanel != null) {
            photoLabel.setVisible(false);
            photoPanel.setVisible(false);
        }
        updateOrderDetailsAndStatusOptions();
    }
    
    public void selectOrder(Order order) {
        if (orderSelectCombo != null && order != null) {
            for (int i = 0; i < orderSelectCombo.getItemCount(); i++) {
                String item = orderSelectCombo.getItemAt(i);
                if (item != null && item.startsWith(order.id)) {
                    orderSelectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void initUI() {
        JPanel headerPanel = createHeader();
        JPanel formPanel = createFormPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Update Order Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_GREEN);
        
        JLabel subtitleLabel = new JLabel("Update the status of your assigned orders");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_GRAY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 30, 30, 30)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Order Selection
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel orderLabel = new JLabel("Select Order:*");
        orderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(orderLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        orderSelectCombo = new JComboBox<>();
        orderSelectCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderSelectCombo.setBackground(Color.WHITE);
        orderSelectCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        orderSelectCombo.setPreferredSize(new Dimension(400, 40));
        orderSelectCombo.addActionListener(e -> updateOrderDetailsAndStatusOptions());
        formPanel.add(orderSelectCombo, gbc);
        row++;
        
        // Spacer
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        // Order Details Display
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weighty = 0.5;
        orderDetailsLabel = new JLabel();
        orderDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderDetailsLabel.setForeground(TEXT_GRAY);
        orderDetailsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(232, 245, 233), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        orderDetailsLabel.setBackground(new Color(245, 250, 245));
        orderDetailsLabel.setOpaque(true);
        orderDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        orderDetailsLabel.setVerticalAlignment(SwingConstants.CENTER);
        orderDetailsLabel.setPreferredSize(new Dimension(500, 120));
        formPanel.add(orderDetailsLabel, gbc);
        row++;
        
        // Spacer
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        formPanel.add(separator, gbc);
        row++;
        
        // Status Selection
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.weighty = 0;
        JLabel statusLabel = new JLabel("New Status:*");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        statusCombo = new JComboBox<>();
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        statusCombo.setPreferredSize(new Dimension(400, 40));
        statusCombo.addActionListener(e -> togglePhotoPanel());
        formPanel.add(statusCombo, gbc);
        row++;
        
        // Photo Panel
        photoLabel = new JLabel("Delivery Photo:*");
        photoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(photoLabel, gbc);
        
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        photoPanel.setBackground(Color.WHITE);
        
        JButton uploadPhotoBtn = new JButton("Choose Photo");
        uploadPhotoBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadPhotoBtn.setBackground(INFO);
        uploadPhotoBtn.setForeground(Color.WHITE);
        uploadPhotoBtn.setBorderPainted(false);
        uploadPhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadPhotoBtn.addActionListener(e -> selectDeliveryPhoto());
        
        photoFileNameLabel = new JLabel("No file selected");
        photoFileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        photoFileNameLabel.setForeground(TEXT_GRAY);
        
        photoPanel.add(uploadPhotoBtn);
        photoPanel.add(photoFileNameLabel);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(photoPanel, gbc);
        row++;
        
        // Signature
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel signatureLabel = new JLabel("Signature:*");
        signatureLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(signatureLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        signatureArea = new JTextArea(3, 20);
        signatureArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signatureArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        signatureArea.setLineWrap(true);
        signatureArea.setWrapStyleWord(true);
        signatureArea.setPreferredSize(new Dimension(300, 70));
        formPanel.add(signatureArea, gbc);
        row++;
        
        // Note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel noteLabel = new JLabel("* Signature is required for all status updates. Photo is required only for 'Delivered' status.", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(TEXT_GRAY);
        formPanel.add(noteLabel, gbc);
        row++;
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 15, 10, 15);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearBtn.setForeground(TEXT_GRAY);
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setPreferredSize(new Dimension(120, 40));
        clearBtn.addActionListener(e -> clearForm());
        
        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(SUCCESS);
        updateBtn.setBorderPainted(false);
        updateBtn.setPreferredSize(new Dimension(150, 40));
        updateBtn.addActionListener(e -> processStatusUpdate());
        
        buttonPanel.add(clearBtn);
        buttonPanel.add(updateBtn);
        formPanel.add(buttonPanel, gbc);
        
        refreshOrderSelectCombo();
        photoLabel.setVisible(false);
        photoPanel.setVisible(false);
        
        return formPanel;
    }
    
    private void refreshOrderSelectCombo() {
        if (orderSelectCombo == null) return;
        
        orderSelectCombo.removeAllItems();
        
        List<Order> activeOrders = myOrders.stream()
            .filter(o -> !"Delivered".equals(o.status) && !"Failed".equals(o.status))
            .collect(Collectors.toList());
        
        if (activeOrders.isEmpty()) {
            orderSelectCombo.addItem("No active orders");
            if (statusCombo != null) statusCombo.setEnabled(false);
        } else {
            for (Order o : activeOrders) {
                orderSelectCombo.addItem(o.id + " - " + o.recipientName + " (" + o.getCourierStatus() + ")");
            }
            if (statusCombo != null) {
                statusCombo.setEnabled(false);
            }
        }
        
        // Set default to show placeholder without adding it to list
        orderSelectCombo.setSelectedIndex(-1);
        
        // Custom renderer to show placeholder text when nothing is selected
        orderSelectCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                if (index == -1 && value == null) {
                    setText("-- Select Parcel --");
                    setForeground(TEXT_GRAY);
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return this;
            }
        });
    }
    
    private void updateOrderDetailsAndStatusOptions() {
        int selectedIndex = orderSelectCombo.getSelectedIndex();
        
        // Check if nothing is selected or placeholder is shown
        if (selectedIndex == -1) {
            orderDetailsLabel.setText("<html><b>Select a parcel</b><br>Please choose an order from the dropdown above.</html>");
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            orderDetailsLabel.setText("<html><b>No active orders available</b><br>All orders have been delivered.</html>");
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        if (statusCombo != null) statusCombo.setEnabled(true);
        String orderId = selected.split(" - ")[0];
        Order order = orderStorage.findOrder(orderId);
        
        if (order != null) {
            String details = String.format("<html>" +
                "<b>Order ID:</b> %s<br>" +
                "<b>Recipient:</b> %s<br>" +
                "<b>Phone:</b> %s<br>" +
                "<b>Address:</b> %s<br>" +
                "<b>Current Status:</b> <span style='color: %s;'>%s</span></html>",
                order.id, order.recipientName, order.recipientPhone,
                order.recipientAddress.length() > 50 ? order.recipientAddress.substring(0, 47) + "..." : order.recipientAddress,
                getStatusHexColor(order.getCourierStatus()), order.getCourierStatus());
            orderDetailsLabel.setText(details);
            updateStatusOptionsForSelectedOrder();
        }
    }
    
    private String getStatusHexColor(String status) {
        switch(status) {
            case "Pending": return "#E5A100";
            case "Picked Up": return "#6F42C1";
            case "In Transit": return "#17A2B8";
            case "Delayed": return "#DC3545";
            case "Delivered": return "#28A745";
            case "Failed": return "#DC3545";
            default: return "#6C757D";
        }
    }
    
    private void updateStatusOptionsForSelectedOrder() {
        int selectedIndex = orderSelectCombo.getSelectedIndex();
        
        if (selectedIndex == -1) {
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            if (statusCombo != null) statusCombo.setEnabled(false);
            return;
        }
        
        if (statusCombo != null) {
            statusCombo.setEnabled(true);
            String orderId = selected.split(" - ")[0];
            Order order = orderStorage.findOrder(orderId);
            
            if (order != null) {
                updateStatusOptions(order.status);
            }
        }
    }
    
    private void updateStatusOptions(String currentStatus) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        switch(currentStatus) {
            case "Pending":
            case "In Transit":
                model.addElement("Picked Up");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Picked Up":
                model.addElement("In Transit");
                model.addElement("Delayed");
                model.addElement("Failed");
                break;
            case "Delayed":
                model.addElement("In Transit");
                model.addElement("Delivered");
                model.addElement("Failed");
                break;
            default:
                model.addElement("Picked Up");
                model.addElement("In Transit");
                model.addElement("Delivered");
                model.addElement("Delayed");
                model.addElement("Failed");
        }
        
        statusCombo.setModel(model);
    }
    
    private void togglePhotoPanel() {
        if (statusCombo == null) return;
        String selectedStatus = (String) statusCombo.getSelectedItem();
        boolean isDelivered = "Delivered".equals(selectedStatus);
        
        photoLabel.setVisible(isDelivered);
        photoPanel.setVisible(isDelivered);
        
        if (!isDelivered) {
            deliveryPhotoFile = null;
            photoFileNameLabel.setText("No file selected");
            photoFileNameLabel.setForeground(TEXT_GRAY);
        }
    }
    
    private void selectDeliveryPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            deliveryPhotoFile = fileChooser.getSelectedFile();
            photoFileNameLabel.setText(deliveryPhotoFile.getName());
            photoFileNameLabel.setForeground(SUCCESS);
        }
    }
    
    private void processStatusUpdate() {
        int selectedIndex = orderSelectCombo.getSelectedIndex();
        
        if (selectedIndex == -1) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("Please select an order to update", WARNING);
            }
            return;
        }
        
        String selected = (String) orderSelectCombo.getSelectedItem();
        if (selected == null || selected.equals("No active orders")) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("Please select an order to update", WARNING);
            }
            return;
        }
        
        String orderId = selected.split(" - ")[0];
        String newStatus = (String) statusCombo.getSelectedItem();
        
        if ("Failed".equals(newStatus)) {
            Order order = orderStorage.findOrder(orderId);
            if (order != null) {
                showFailedDeliveryDialog(order);
            }
            return;
        }
        
        String signature = signatureArea.getText().trim();
        
        if (signature.isEmpty()) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("Recipient signature is required", WARNING);
            }
            signatureArea.requestFocus();
            return;
        }
        
        if ("Delivered".equals(newStatus) && deliveryPhotoFile == null) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("Photo proof is required for delivery completion", WARNING);
            }
            return;
        }
        
        Order order = orderStorage.findOrder(orderId);
        if (order == null) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("Order not found", DANGER);
            }
            return;
        }
        
        if ("Delivered".equals(order.status)) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("This order has already been delivered", WARNING);
            }
            return;
        }
        
        if ("Failed".equals(order.status)) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("This order has already been marked as failed. It will be reassigned by admin.", WARNING);
            }
            return;
        }
        
        if (!isValidTransition(order.status, newStatus)) {
            if (parentDashboard != null) {
                parentDashboard.showNotification("Invalid status transition from '" + order.status + "' to '" + newStatus + "'", WARNING);
            }
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Update Order Status\n\nOrder: %s\nRecipient: %s\nCurrent Status: %s\nNew Status: %s\n\nSignature: %s",
                orderId, order.recipientName, order.status, newStatus, signature),
            "Confirm Status Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success;
            
            if ("Delivered".equals(newStatus)) {
                String notesWithSignature = (order.notes != null ? order.notes + "\n" : "") + 
                    "Signature: " + signature + " (Delivered on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + ")";
                success = orderStorage.completeOrder(orderId, 0, 0, 
                    deliveryPhotoFile.getAbsolutePath(), notesWithSignature);
            } else {
                order.status = newStatus;
                order.notes = (order.notes != null ? order.notes + "\n" : "") + 
                    "Status updated to " + newStatus + " - Signature: " + signature + 
                    " on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                orderStorage.updateOrder(order);
                success = true;
            }
            
            if (success) {
                String message = String.format("Order %s status updated to: %s\nSignature recorded", 
                    orderId, newStatus);
                if (parentDashboard != null) {
                    parentDashboard.showNotification(message, SUCCESS);
                    parentDashboard.refreshData();
                }
                clearForm();
                
                JOptionPane.showMessageDialog(this,
                    String.format("Status Updated Successfully!\n\nOrder ID: %s\nNew Status: %s\nRecipient: %s",
                        orderId, newStatus, order.recipientName),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (parentDashboard != null) {
                    parentDashboard.showNotification("Failed to update order status", DANGER);
                }
            }
        }
    }
    
    private boolean isValidTransition(String current, String next) {
        if (current.equals(next)) return false;
        
        if ("Failed".equals(next)) {
            return !"Delivered".equals(current);
        }
        
        switch(current) {
            case "Pending":
            case "In Transit":
                return next.equals("Picked Up") || next.equals("Delayed");
            case "Picked Up":
                return next.equals("In Transit") || next.equals("Delayed");
            case "Delayed":
                return next.equals("In Transit") || next.equals("Delivered");
            default:
                return false;
        }
    }
    
    private void showFailedDeliveryDialog(Order order) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Mark Delivery as Failed - " + order.id, true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Failed Delivery Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(DANGER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;
        
        int y = 0;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel orderInfoLabel = new JLabel("<html><b>Order:</b> " + order.id + "<br><b>Recipient:</b> " + order.recipientName + "<br><b>Address:</b> " + order.recipientAddress + "</html>");
        orderInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderInfoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        orderInfoLabel.setBackground(new Color(248, 249, 250));
        orderInfoLabel.setOpaque(true);
        formPanel.add(orderInfoLabel, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel reasonLabel = new JLabel("Failure Reason:*");
        reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(reasonLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> reasonCombo = new JComboBox<>(new String[]{
            "Select reason...",
            "Recipient not available",
            "Wrong address provided",
            "Recipient refused delivery",
            "Package damaged",
            "Delivery area restricted",
            "No one to receive",
            "Other"
        });
        reasonCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(reasonCombo, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y;
        JLabel descLabel = new JLabel("Details:*");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(descLabel, gbc);
        
        gbc.gridx = 1;
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        scrollPane.setPreferredSize(new Dimension(250, 80));
        formPanel.add(scrollPane, gbc);
        y++;
        
        gbc.gridx = 0; gbc.gridy = y;
        JLabel sigLabel = new JLabel("Signature:*");
        sigLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(sigLabel, gbc);
        
        gbc.gridx = 1;
        JTextArea failedSignatureArea = new JTextArea(2, 20);
        failedSignatureArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        failedSignatureArea.setLineWrap(true);
        failedSignatureArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane sigScroll = new JScrollPane(failedSignatureArea);
        sigScroll.setPreferredSize(new Dimension(250, 50));
        formPanel.add(sigScroll, gbc);
        y++;
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(TEXT_GRAY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton confirmBtn = new JButton("Mark as Failed");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(DANGER);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setPreferredSize(new Dimension(130, 35));
        confirmBtn.addActionListener(e -> {
            String selectedReason = (String) reasonCombo.getSelectedItem();
            String details = reasonArea.getText().trim();
            String signature = failedSignatureArea.getText().trim();
            
            if (selectedReason == null || selectedReason.equals("Select reason...")) {
                if (parentDashboard != null) {
                    parentDashboard.showNotification("Please select a failure reason", WARNING);
                }
                return;
            }
            
            if (details.isEmpty()) {
                if (parentDashboard != null) {
                    parentDashboard.showNotification("Please provide failure details", WARNING);
                }
                return;
            }
            
            if (signature.isEmpty()) {
                if (parentDashboard != null) {
                    parentDashboard.showNotification("Signature is required", WARNING);
                }
                return;
            }
            
            String fullReason = selectedReason + ": " + details;
            
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to mark this delivery as FAILED?\n\n" +
                "Order: " + order.id + "\n" +
                "Reason: " + fullReason + "\n\n" +
                "This order will be marked as FAILED and will appear in your history.\n" +
                "The admin will be notified for reassignment.\n" +
                "This action cannot be undone.",
                "Confirm Failed Delivery",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                order.markAsFailed(fullReason);
                order.notes = (order.notes != null ? order.notes + "\n" : "") +
                    "FAILED DELIVERY - Reason: " + fullReason + 
                    " - Signature: " + signature +
                    " on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                
                orderStorage.updateOrder(order);
                if (parentDashboard != null) {
                    parentDashboard.showNotification("Delivery marked as FAILED. Order recorded in history.", WARNING);
                    parentDashboard.refreshData();
                }
                clearForm();
                dialog.dispose();
                
                JOptionPane.showMessageDialog(this,
                    "Delivery marked as Failed\n\n" +
                    "Order ID: " + order.id + "\n" +
                    "Reason: " + fullReason + "\n\n" +
                    "This order has been recorded as FAILED in your history.\n" +
                    "Admin will reassign it to another driver.",
                    "Failed Delivery",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}