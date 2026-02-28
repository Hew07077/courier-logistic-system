package admin.management;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class OrderManagement {
    private JPanel mainPanel;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel totalLabel, pendingLabel, transitLabel, delayedLabel;
    
    private OrderStorage storage;
    
    public OrderManagement() {
        storage = new OrderStorage();
        createMainPanel();
        refreshTable();
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // 顶部
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(createStatsPanel(), BorderLayout.NORTH);
        topPanel.add(createSearchPanel(), BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 表格
        createTable();
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 按钮
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        totalLabel = createStatCard("Total Orders", "0", new Color(52, 152, 219));
        pendingLabel = createStatCard("Pending", "0", new Color(241, 196, 15));
        transitLabel = createStatCard("In Transit", "0", new Color(46, 204, 113));
        delayedLabel = createStatCard("Delayed", "0", new Color(231, 76, 60));
        
        panel.add(totalLabel);
        panel.add(pendingLabel);
        panel.add(transitLabel);
        panel.add(delayedLabel);
        
        return panel;
    }
    
    private JLabel createStatCard(String title, String value, Color color) {
        JLabel label = new JLabel("<html><div style='text-align:center;'>" +
            "<span style='font-size:12px;color:#7F8C8D;'>" + title + "</span><br>" +
            "<span style='font-size:24px;color:" + toHex(color) + ";font-weight:bold;'>" + value + 
            "</span></div></html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        return label;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(Color.WHITE);
        
        panel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTable(); }
        });
        panel.add(searchField);
        
        panel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "In Transit", "Delivered", "Delayed"});
        statusFilter.addActionListener(e -> filterTable());
        panel.add(statusFilter);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(108, 117, 125));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> {
            storage.saveOrders();
            refreshTable();
        });
        panel.add(refreshBtn);
        
        return panel;
    }
    
    private void createTable() {
        String[] columns = {"ID", "Customer", "Status", "Date", "Driver", "Est. Delivery"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ordersTable.setRowHeight(35);
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        ordersTable.getTableHeader().setBackground(new Color(255, 140, 0));
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        
        ordersTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showOrderDialog();
            }
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(Color.WHITE);
        
        panel.add(createButton("View Details", new Color(52, 152, 219), e -> showOrderDialog()));
        panel.add(createButton("Assign Driver", new Color(255, 140, 0), e -> assignOrder()));
        panel.add(createButton("Reassign Delayed", new Color(231, 76, 60), e -> reassignDelayed()));
        panel.add(createButton("Add Order", new Color(46, 204, 113), e -> addOrder()));
        panel.add(createButton("Delete", new Color(149, 165, 166), e -> deleteOrder()));
        
        return panel;
    }
    
    private JButton createButton(String text, Color bg, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
    
    // ========== 订单详情对话框 ==========
    private void showOrderDialog() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请选择订单");
            return;
        }
        
        String id = (String) tableModel.getValueAt(row, 0);
        Order o = storage.findOrder(id);
        if (o == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "订单详情", true);
        dialog.setSize(450, 550);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;
        
        // 订单号 (只读)
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("订单号:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField idField = new JTextField(o.id);
        idField.setEditable(false);
        panel.add(idField, gbc);
        y++;
        
        // 客户名
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("客户:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField nameField = new JTextField(o.customer);
        panel.add(nameField, gbc);
        y++;
        
        // 电话
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("电话:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField phoneField = new JTextField(o.phone);
        panel.add(phoneField, gbc);
        y++;
        
        // 地址
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("地址:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField addressField = new JTextField(o.address);
        panel.add(addressField, gbc);
        y++;
        
        // 状态
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending", "In Transit", "Delivered", "Delayed"});
        statusBox.setSelectedItem(o.status);
        panel.add(statusBox, gbc);
        y++;
        
        // 司机
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("司机:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField driverField = new JTextField(o.driver != null ? o.driver : "");
        panel.add(driverField, gbc);
        y++;
        
        // 车辆
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("车辆:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField vehicleField = new JTextField(o.vehicle != null ? o.vehicle : "");
        panel.add(vehicleField, gbc);
        y++;
        
        // 预计送达
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("预计送达:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField estField = new JTextField(o.estDate != null ? o.estDate : "");
        panel.add(estField, gbc);
        y++;
        
        // 订单日期 (只读)
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel("下单日期:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField dateField = new JTextField(o.date);
        dateField.setEditable(false);
        panel.add(dateField, gbc);
        y++;
        
        // 如果是延误订单，显示延误原因
        if ("Delayed".equals(o.status)) {
            gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
            panel.add(new JLabel("延误原因:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 2;
            JTextField reasonField = new JTextField(o.reason != null ? o.reason : "");
            panel.add(reasonField, gbc);
        }
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveBtn = new JButton("保存");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            // 更新订单信息
            o.customer = nameField.getText().trim();
            o.phone = phoneField.getText().trim();
            o.address = addressField.getText().trim();
            o.status = (String) statusBox.getSelectedItem();
            o.driver = driverField.getText().trim().isEmpty() ? null : driverField.getText().trim();
            o.vehicle = vehicleField.getText().trim().isEmpty() ? null : vehicleField.getText().trim();
            o.estDate = estField.getText().trim().isEmpty() ? null : estField.getText().trim();
            
            storage.saveOrders();
            refreshTable();
            dialog.dispose();
            JOptionPane.showMessageDialog(mainPanel, "订单已保存");
        });
        
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    // ========== 分配订单 ==========
    private void assignOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请选择订单");
            return;
        }
        
        String id = (String) tableModel.getValueAt(row, 0);
        Order o = storage.findOrder(id);
        if (o == null) return;
        
        if (!"Pending".equals(o.status)) {
            JOptionPane.showMessageDialog(mainPanel, "只能分配待处理的订单");
            return;
        }
        
        String driver = JOptionPane.showInputDialog(mainPanel, "输入司机ID (如: DRV001):");
        if (driver != null && !driver.trim().isEmpty()) {
            o.driver = driver.trim();
            // 根据司机自动分配车辆
            if (driver.equals("DRV001")) o.vehicle = "VH001";
            else if (driver.equals("DRV002")) o.vehicle = "VH002";
            else if (driver.equals("DRV003")) o.vehicle = "VH003";
            else o.vehicle = "VH004";
            
            o.status = "In Transit";
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 3);
            o.estDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            
            storage.saveOrders();
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "订单已分配给 " + driver);
        }
    }
    
    // ========== 重新分配延误订单 ==========
    private void reassignDelayed() {
        java.util.List<Order> delayed = storage.getDelayedOrders();
        if (delayed.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "没有延误订单");
            return;
        }
        
        String[] options = new String[delayed.size()];
        for (int i = 0; i < delayed.size(); i++) {
            options[i] = delayed.get(i).id + " - " + delayed.get(i).customer;
        }
        
        String selected = (String) JOptionPane.showInputDialog(mainPanel,
            "选择要重新分配的延误订单:", "重新分配",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
        if (selected != null) {
            String id = selected.split(" - ")[0];
            Order o = storage.findOrder(id);
            
            String newDriver = JOptionPane.showInputDialog(mainPanel, "输入新司机ID (DRV004 或 DRV005):");
            if (newDriver != null && !newDriver.trim().isEmpty()) {
                o.driver = newDriver.trim();
                if (newDriver.equals("DRV004")) o.vehicle = "VH004";
                else if (newDriver.equals("DRV005")) o.vehicle = "VH005";
                
                o.status = "In Transit";
                o.reason = null;
                
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, 2);
                o.estDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                
                storage.saveOrders();
                refreshTable();
                JOptionPane.showMessageDialog(mainPanel, "订单已重新分配");
            }
        }
    }
    
    // ========== 添加新订单 ==========
    private void addOrder() {
        String newId = storage.generateNewId();
        
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addrField = new JTextField();
        
        Object[] fields = {
            "客户姓名:", nameField,
            "电话:", phoneField,
            "地址:", addrField
        };
        
        int result = JOptionPane.showConfirmDialog(mainPanel, fields, 
            "新订单 - " + newId, JOptionPane.OK_CANCEL_OPTION);
            
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addrField.getText().trim();
            
            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "请填写所有字段");
                return;
            }
            
            Order o = new Order(newId, name, phone, address);
            storage.addOrder(o);
            storage.saveOrders();
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "订单添加成功");
        }
    }
    
    // ========== 删除订单 ==========
    private void deleteOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "请选择要删除的订单");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "确定删除这个订单?", "确认删除",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String id = (String) tableModel.getValueAt(row, 0);
            storage.removeOrder(id);
            storage.saveOrders();
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "订单已删除");
        }
    }
    
    // ========== 刷新表格 ==========
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Order o : storage.getAllOrders()) {
            tableModel.addRow(new Object[]{
                o.id, 
                o.customer, 
                o.status, 
                o.date,
                o.driver != null ? o.driver : "-",
                o.estDate != null ? o.estDate : "-"
            });
        }
        
        // 更新统计卡片
        updateStat(totalLabel, "Total Orders", String.valueOf(storage.getAllOrders().size()), new Color(52, 152, 219));
        updateStat(pendingLabel, "Pending", String.valueOf(storage.getPendingCount()), new Color(241, 196, 15));
        updateStat(transitLabel, "In Transit", String.valueOf(storage.getInTransitCount()), new Color(46, 204, 113));
        updateStat(delayedLabel, "Delayed", String.valueOf(storage.getDelayedCount()), new Color(231, 76, 60));
    }
    
    private void updateStat(JLabel label, String title, String value, Color color) {
        label.setText("<html><div style='text-align:center;'>" +
            "<span style='font-size:12px;color:#7F8C8D;'>" + title + "</span><br>" +
            "<span style='font-size:24px;color:" + toHex(color) + ";font-weight:bold;'>" + value + 
            "</span></div></html>");
    }
    
    // ========== 过滤表格 ==========
    private void filterTable() {
        String search = searchField.getText().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        
        tableModel.setRowCount(0);
        for (Order o : storage.getAllOrders()) {
            boolean match = true;
            
            // 搜索过滤
            if (!search.isEmpty()) {
                if (!o.id.toLowerCase().contains(search) && 
                    !o.customer.toLowerCase().contains(search) &&
                    !(o.driver != null && o.driver.toLowerCase().contains(search))) {
                    match = false;
                }
            }
            
            // 状态过滤
            if (!"All".equals(status) && !o.status.equals(status)) {
                match = false;
            }
            
            if (match) {
                tableModel.addRow(new Object[]{
                    o.id, o.customer, o.status, o.date,
                    o.driver != null ? o.driver : "-",
                    o.estDate != null ? o.estDate : "-"
                });
            }
        }
    }
    
    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
    
    // ========== 公共方法 ==========
    public JPanel getMainPanel() { 
        return mainPanel; 
    }
    
    public JPanel getRefreshedPanel() { 
        refreshTable(); 
        return mainPanel; 
    }
    
    public void refreshData() { 
        refreshTable(); 
    }
    
    public int getPendingCount() { 
        return storage.getPendingCount(); 
    }
    
    public int getTotalCount() { 
        return storage.getAllOrders().size(); 
    }
}