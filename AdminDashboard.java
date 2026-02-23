package LogisticAdmin.gui.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminDashboard extends JFrame {
    
    private JTabbedPane mainTabbedPane;
    
    // 主题颜色
    private final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private final Color ORANGE_LIGHT = new Color(255, 180, 80);
    private final Color ORANGE_PALE = new Color(255, 220, 180);
    private final Color WHITE_PURE = new Color(255, 255, 255);
    private final Color BLACK_TEXT = new Color(0, 0, 0);
    private final Color GRAY_BG = new Color(245, 245, 245);
    
    public AdminDashboard() {
        setTitle("LogiXpress 管理员控制台");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        
        // ========== 顶部导航栏 ==========
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ORANGE_PRIMARY);
        topBar.setPreferredSize(new Dimension(getWidth(), 70));
        
        JLabel titleLabel = new JLabel("LogiXpress 管理员控制台");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(WHITE_PURE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 0));
        topBar.add(titleLabel, BorderLayout.WEST);
        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        topRightPanel.setBackground(ORANGE_PRIMARY);
        
        JLabel adminLabel = new JLabel("管理员: Admin");
        adminLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        adminLabel.setForeground(WHITE_PURE);
        topRightPanel.add(adminLabel);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        timeLabel.setForeground(WHITE_PURE);
        topRightPanel.add(timeLabel);
        
        Timer timer = new Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
        );
        timer.start();
        
        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        logoutBtn.setForeground(WHITE_PURE);
        logoutBtn.setBackground(new Color(220, 20, 60));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(Color.BLACK);
            }
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(new Color(220, 20, 60));
            }
        });
        
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要退出登录吗？",
                "退出确认",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LogisticAdmin.gui.Login().setVisible(true);
            }
        });
        
        topRightPanel.add(logoutBtn);
        topBar.add(topRightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        
        // ========== 主选项卡面板 - 只有菜单框架 ==========
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 18));
        mainTabbedPane.setBackground(WHITE_PURE);
        mainTabbedPane.setForeground(BLACK_TEXT);
        
        // 添加四个主要模块 - 只有占位内容，没有任何功能
        mainTabbedPane.addTab("📦 订单与派送管理", createPlaceholderPanel("订单与派送管理", "此功能开发中..."));
        mainTabbedPane.addTab("🚛 车辆与物流管理", createPlaceholderPanel("车辆与物流管理", "此功能开发中..."));
        mainTabbedPane.addTab("👨‍✈️ 司机管理", createPlaceholderPanel("司机管理", "此功能开发中..."));
        mainTabbedPane.addTab("💰 财务管理", createPlaceholderPanel("财务管理", "此功能开发中..."));
        
        add(mainTabbedPane, BorderLayout.CENTER);
        
        // ========== 底部状态栏 ==========
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.setBackground(GRAY_BG);
        bottomBar.setPreferredSize(new Dimension(getWidth(), 35));
        
        JLabel statusLabel = new JLabel("系统状态: 正常运行  |  欢迎使用LogiXpress管理员系统");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setForeground(Color.DARK_GRAY);
        bottomBar.add(statusLabel);
        
        add(bottomBar, BorderLayout.SOUTH);
    }
    
    /**
     * 创建占位面板 - 只有文字，没有任何功能按钮
     */
    private JPanel createPlaceholderPanel(String title, String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE_PURE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(ORANGE_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);
        
        panel.add(Box.createVerticalStrut(50), gbc);
        
        // 消息
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        messageLabel.setForeground(Color.GRAY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, gbc);
        
        panel.add(Box.createVerticalStrut(30), gbc);
        
        // 开发中图标
        JLabel devLabel = new JLabel("⚙️");
        devLabel.setFont(new Font("微软雅黑", Font.PLAIN, 60));
        devLabel.setForeground(ORANGE_LIGHT);
        devLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(devLabel, gbc);
        
        return panel;
    }
}