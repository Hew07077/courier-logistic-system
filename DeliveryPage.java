package courier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileNotFoundException;

public class DeliveryPage extends JPanel {
    
    // --- Color Palette (从CourierDashboard复制) ---
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_DARK = new Color(27, 94, 32);
    private final Color GREEN_LIGHT = new Color(220, 245, 220);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    
    // Modern UI Colors
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(255, 193, 7);
    
    // Table Components
    private JTable deliveryTable;
    private DefaultTableModel deliveryModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> filterColumnCombo;
    private JTextField searchTextField;
    private JComboBox<String> statusCombo;
    private JTextArea remarksArea;
    private JLabel selectedParcelLabel;
    private JLabel photoFileNameLabel;
    private File selectedPhotoFile;
    private JButton uploadPhotoBtn;
    private JPanel photoPanel;
    private JLabel photoLabel;

    // 私有构造器，防止外部直接创建实例
    public DeliveryPage() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        
        initUI();
    }
    
    private void initUI() {
        // --- 1. Header Section ---
        JPanel headerPanel = createHeaderPanel();
        
        // --- 2. Stats Cards Section ---
        JPanel statsPanel = createStatsPanel();
        
        // --- 3. Table and Form Section ---
        JSplitPane splitPane = createSplitPane();
        
        // Assemble top parts
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(BG_LIGHT);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.CENTER);
        
        add(topContainer, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_LIGHT);
        
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setBackground(BG_LIGHT);
        
        JLabel titleLabel = new JLabel("Deliveries & Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel subtitleLabel = new JLabel("Track parcels and update delivery status in real-time");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_GRAY);
        
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(titleContainer, BorderLayout.WEST);
        
        // 高级筛选面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        filterPanel.setBackground(BG_LIGHT);
        
        JLabel filterLabel = new JLabel("Search by:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        String[] columns = {"Parcel ID", "Recipient", "Phone No.", "Location", "Status", "Priority", "Last Updated"};
        filterColumnCombo = new JComboBox<>(columns);
        filterColumnCombo.setPreferredSize(new Dimension(120, 35));
        filterColumnCombo.setBackground(Color.WHITE);
        filterColumnCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        searchTextField = new JTextField(15);
        searchTextField.setPreferredSize(new Dimension(150, 35));
        searchTextField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTextField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        
        JButton clearFilterBtn = new JButton("Clear");
        clearFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearFilterBtn.setBackground(Color.WHITE);
        clearFilterBtn.setForeground(TEXT_GRAY);
        clearFilterBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearFilterBtn.setPreferredSize(new Dimension(80, 35));
        clearFilterBtn.addActionListener(e -> {
            searchTextField.setText("");
            rowSorter.setRowFilter(null);
            filterColumnCombo.setSelectedIndex(0);
        });
        
        filterPanel.add(filterLabel);
        filterPanel.add(filterColumnCombo);
        filterPanel.add(searchTextField);
        filterPanel.add(clearFilterBtn);
        
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JPanel pendingCard = createStatCard("Pending", "To be delivered", "3", WARNING);
        pendingCard.addMouseListener(new StatCardClickListener("Pending"));
        
        JPanel transitCard = createStatCard("In Transit", "Currently out", "2", INFO);
        transitCard.addMouseListener(new StatCardClickListener("In Transit"));
        
        JPanel deliveredCard = createStatCard("Delivered", "Completed today", "1", SUCCESS);
        deliveredCard.addMouseListener(new StatCardClickListener("Delivered"));
        
        JPanel totalCard = createStatCard("Total Tasks", "Assigned parcels", "6", PRIMARY_GREEN);
        totalCard.addMouseListener(new StatCardClickListener(""));
        
        statsPanel.add(pendingCard);
        statsPanel.add(transitCard);
        statsPanel.add(deliveredCard);
        statsPanel.add(totalCard);
        
        return statsPanel;
    }
    
    private JSplitPane createSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        
        // Table Container
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        // Initialize Table Model
        String[] columns = {"Parcel ID", "Recipient", "Phone No.", "Location", "Status", "Priority", "Last Updated"};
        deliveryModel = new DefaultTableModel(new Object[][]{
            {"LX-901", "Justin Khoo", "012-3456789", "Petaling Jaya", "In Transit", "High", "10:30 AM"},
            {"LX-902", "Sarah Tan", "013-4567890", "Subang Jaya", "Pending", "Normal", "09:15 AM"},
            {"LX-903", "Ahmad Zaki", "014-5678901", "Kuala Lumpur", "Delivered", "Normal", "Yesterday"},
            {"LX-904", "Linda Chen", "015-6789012", "Shah Alam", "Pending", "Urgent", "08:45 AM"},
            {"LX-905", "Muthu Kumar", "016-7890123", "Bangsar", "In Transit", "High", "11:20 AM"},
            {"LX-906", "Emily Wong", "017-8901234", "Damansara", "Out for Delivery", "Normal", "10:05 AM"}
        }, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        rowSorter = new TableRowSorter<>(deliveryModel);
        
        deliveryTable = new JTable(deliveryModel);
        deliveryTable.setRowSorter(rowSorter);
        styleCombinedTable(deliveryTable);
        deliveryTable.setRowHeight(50);
        deliveryTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        deliveryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateSelectedParcelDetails();
        });
        
        JScrollPane scrollPane = new JScrollPane(deliveryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        splitPane.setTopComponent(tableContainer);
        splitPane.setBottomComponent(createUpdateForm());
        
        return splitPane;
    }
    
    private JPanel createUpdateForm() {
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel formTitle = new JLabel("Update Delivery Status");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(PRIMARY_GREEN);
        formPanel.add(formTitle, BorderLayout.NORTH);
        
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);
        
        // Selected Parcel
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.2;
        JLabel parcelLabel = new JLabel("Selected Parcel:");
        parcelLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(parcelLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        selectedParcelLabel = new JLabel("None selected");
        selectedParcelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectedParcelLabel.setForeground(PRIMARY_GREEN);
        fieldsPanel.add(selectedParcelLabel, gbc);
        
        // Status Selection
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.2;
        JLabel statusLabel = new JLabel("New Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        statusCombo = new JComboBox<>(new String[]{
            "Select Status...",
            "Out for Delivery",
            "Delivered", 
            "Failed Delivery",
            "Returning to Hub",
            "Held at Facility"
        });
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        statusCombo.setBackground(Color.WHITE);
        statusCombo.addActionListener(e -> togglePhotoVisibility());
        fieldsPanel.add(statusCombo, gbc);
        
        // Photo Upload
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.2;
        photoLabel = new JLabel("Photo Proof:");
        photoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(photoLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        photoPanel.setBackground(Color.WHITE);
        
        uploadPhotoBtn = new JButton("📷 Select Photo");
        uploadPhotoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        uploadPhotoBtn.setBackground(Color.WHITE);
        uploadPhotoBtn.setForeground(PRIMARY_GREEN);
        uploadPhotoBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN));
        uploadPhotoBtn.setPreferredSize(new Dimension(100, 25));
        uploadPhotoBtn.addActionListener(e -> selectPhotoFromFile());
        
        photoFileNameLabel = new JLabel("No file selected");
        photoFileNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        photoFileNameLabel.setForeground(TEXT_GRAY);
        
        JButton removePhotoBtn = new JButton("✕");
        removePhotoBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        removePhotoBtn.setBackground(Color.WHITE);
        removePhotoBtn.setForeground(Color.RED);
        removePhotoBtn.setBorder(BorderFactory.createLineBorder(Color.RED));
        removePhotoBtn.setPreferredSize(new Dimension(22, 22));
        removePhotoBtn.addActionListener(e -> removePhoto());
        
        photoPanel.add(uploadPhotoBtn);
        photoPanel.add(Box.createHorizontalStrut(5));
        photoPanel.add(photoFileNameLabel);
        photoPanel.add(Box.createHorizontalStrut(5));
        photoPanel.add(removePhotoBtn);
        
        fieldsPanel.add(photoPanel, gbc);
        
        // Remarks
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTH;
        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(remarksLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        remarksArea = new JTextArea(2, 30);
        remarksArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        remarksArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksScroll.setBorder(null);
        remarksScroll.setPreferredSize(new Dimension(300, 50));
        fieldsPanel.add(remarksScroll, gbc);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(TEXT_GRAY);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setPreferredSize(new Dimension(80, 28));
        clearBtn.addActionListener(e -> clearUpdateForm());
        
        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateBtn.setBackground(PRIMARY_GREEN);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setContentAreaFilled(true);
        updateBtn.setOpaque(true);
        updateBtn.setBorderPainted(false);
        updateBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        updateBtn.setPreferredSize(new Dimension(120, 28));
        updateBtn.addActionListener(e -> performStatusUpdate());
        
        buttonPanel.add(clearBtn);
        buttonPanel.add(updateBtn);
        
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        togglePhotoVisibility();
        
        return formPanel;
    }
    
    // Helper method to create stat cards
    private JPanel createStatCard(String title, String desc, String val, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true), 
            BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        
        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tLabel.setForeground(TEXT_GRAY);
        
        JLabel vLabel = new JLabel(val);
        vLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        vLabel.setForeground(color);
        
        JLabel dLabel = new JLabel(desc);
        dLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dLabel.setForeground(TEXT_GRAY);
        
        card.add(tLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(vLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(dLabel);
        
        return card;
    }
    
    private void styleCombinedTable(JTable table) {
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(8, 3));
        table.setSelectionBackground(GREEN_LIGHT);
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        // Status column renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String val = v.toString();
                
                if (val.equals("Delivered")) {
                    lbl.setForeground(new Color(46, 125, 50));
                    lbl.setText("✓ " + val);
                } else if (val.equals("Out for Delivery")) {
                    lbl.setForeground(new Color(25, 118, 210));
                    lbl.setText("🚚 " + val);
                } else if (val.equals("In Transit")) {
                    lbl.setForeground(new Color(25, 118, 210));
                    lbl.setText("⏱️ " + val);
                } else if (val.equals("Pending")) {
                    lbl.setForeground(new Color(237, 108, 2));
                    lbl.setText("⏳ " + val);
                } else if (val.equals("Failed Delivery")) {
                    lbl.setForeground(new Color(198, 40, 40));
                    lbl.setText("❌ " + val);
                }
                
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return lbl;
            }
        });
        
        // Priority column renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String val = v.toString();
                
                if (val.equals("Urgent")) {
                    lbl.setForeground(new Color(198, 40, 40));
                    lbl.setText("🔴 " + val);
                } else if (val.equals("High")) {
                    lbl.setForeground(new Color(237, 108, 2));
                    lbl.setText("🟠 " + val);
                } else {
                    lbl.setForeground(new Color(46, 125, 50));
                    lbl.setText("🟢 " + val);
                }
                
                return lbl;
            }
        });
    }
    
    private void applyFilter() {
        String text = searchTextField.getText().trim();
        int columnIndex = filterColumnCombo.getSelectedIndex();
        
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
        }
    }
    
    private class StatCardClickListener extends MouseAdapter {
        private String filterStatus;
        public StatCardClickListener(String status) { this.filterStatus = status; }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (filterStatus.isEmpty()) {
                rowSorter.setRowFilter(null);
                filterColumnCombo.setSelectedIndex(4);
                searchTextField.setText("");
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter("^" + filterStatus + "$", 4));
                filterColumnCombo.setSelectedIndex(4);
                searchTextField.setText(filterStatus);
            }
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
            JPanel source = (JPanel)e.getSource();
            source.setCursor(new Cursor(Cursor.HAND_CURSOR));
            source.setBackground(new Color(252, 252, 252));
            source.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_GREEN, 1), 
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            JPanel source = (JPanel)e.getSource();
            source.setBackground(Color.WHITE);
            source.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true), 
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        }
    }
    
    private void togglePhotoVisibility() {
        String selectedStatus = (String) statusCombo.getSelectedItem();
        if (selectedStatus != null && selectedStatus.equals("Delivered")) {
            photoLabel.setVisible(true);
            photoPanel.setVisible(true);
        } else {
            photoLabel.setVisible(false);
            photoPanel.setVisible(false);
            if (selectedPhotoFile != null) {
                removePhoto();
            }
        }
    }
    
    private void selectPhotoFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Delivery Proof Photo");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = fileChooser.getSelectedFile();
            
            String fileName = selectedPhotoFile.getName();
            if (fileName.length() > 30) {
                fileName = fileName.substring(0, 27) + "...";
            }
            photoFileNameLabel.setText(fileName);
            photoFileNameLabel.setForeground(PRIMARY_GREEN);
            uploadPhotoBtn.setText("📷 Change Photo");
        }
    }
    
    private void removePhoto() {
        selectedPhotoFile = null;
        photoFileNameLabel.setText("No file selected");
        photoFileNameLabel.setForeground(TEXT_GRAY);
        uploadPhotoBtn.setText("📷 Select Photo");
    }
    
    private void updateSelectedParcelDetails() {
        int row = deliveryTable.getSelectedRow();
        if (row != -1) {
            int modelRow = deliveryTable.convertRowIndexToModel(row);
            String parcelId = deliveryModel.getValueAt(modelRow, 0).toString();
            String recipient = deliveryModel.getValueAt(modelRow, 1).toString();
            String phoneNo = deliveryModel.getValueAt(modelRow, 2).toString();
            String currentStatus = deliveryModel.getValueAt(modelRow, 4).toString();
            
            selectedParcelLabel.setText(String.format("%s - %s (%s) [Current: %s]", 
                parcelId, recipient, phoneNo, currentStatus));
            
            if (currentStatus.equals("Pending")) {
                statusCombo.setSelectedItem("Out for Delivery");
            } else if (currentStatus.equals("In Transit")) {
                statusCombo.setSelectedItem("Delivered");
            } else if (currentStatus.equals("Out for Delivery")) {
                statusCombo.setSelectedItem("Delivered");
            } else {
                statusCombo.setSelectedIndex(0);
            }
            
            remarksArea.setText("Update for " + parcelId + ": ");
            removePhoto();
            togglePhotoVisibility();
        }
    }
    
    private void performStatusUpdate() {
        int viewRow = deliveryTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a delivery from the list first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = deliveryTable.convertRowIndexToModel(viewRow);
        
        String selectedStatus = (String) statusCombo.getSelectedItem();
        if (selectedStatus == null || selectedStatus.equals("Select Status...")) {
            JOptionPane.showMessageDialog(this,
                "Please select a new status.",
                "Invalid Status",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedStatus.equals("Delivered") && selectedPhotoFile == null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "It's recommended to attach a photo for Delivered status. Continue without photo?",
                "Photo Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        String remarks = remarksArea.getText().trim();
        if (remarks.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "No remarks added. Continue with update?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        String parcelId = deliveryModel.getValueAt(modelRow, 0).toString();
        String oldStatus = deliveryModel.getValueAt(modelRow, 4).toString();
        
        deliveryModel.setValueAt(selectedStatus, modelRow, 4);
        deliveryModel.setValueAt(new SimpleDateFormat("hh:mm a").format(new Date()), modelRow, 6);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String timestamp = sdf.format(new Date());
        
        String photoInfo = (selectedPhotoFile != null && selectedStatus.equals("Delivered")) ? 
            "\nPhoto: " + selectedPhotoFile.getName() : 
            "\nPhoto: Not attached";
        
        String message = String.format(
            "Parcel %s status updated:\n" +
            "From: %s\n" +
            "To: %s\n" +
            "Time: %s%s\n" +
            "Remarks: %s",
            parcelId, oldStatus, selectedStatus, timestamp, photoInfo,
            remarks.isEmpty() ? "No remarks" : remarks
        );
        
        JOptionPane.showMessageDialog(this,
            message,
            "Status Updated Successfully",
            JOptionPane.INFORMATION_MESSAGE);
        
        deliveryTable.clearSelection();
        clearUpdateForm();
        deliveryTable.repaint();
    }
    
    private void clearUpdateForm() {
        selectedParcelLabel.setText("None selected");
        statusCombo.setSelectedIndex(0);
        remarksArea.setText("");
        removePhoto();
        togglePhotoVisibility();
    }
    
    // Public method to refresh data (if needed)
    public void refreshData() {
        // Add any refresh logic here if needed
        deliveryTable.repaint();
    }
}