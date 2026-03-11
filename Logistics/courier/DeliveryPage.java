package courier;

import logistics.orders.Order;
import logistics.orders.OrderStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.File;

public class DeliveryPage extends JPanel {
    
    // --- Color Palette ---
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);
    private final Color GREEN_LIGHT = new Color(220, 245, 220);
    private final Color BG_LIGHT = new Color(245, 247, 250);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Color TEXT_GRAY = new Color(108, 117, 125);
    
    // Modern UI Colors
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color DANGER = new Color(220, 53, 69);
    
    // Status colors
    private static final Color STATUS_DELIVERED = new Color(40, 167, 69);
    private static final Color STATUS_TRANSIT = new Color(23, 162, 184);
    private static final Color STATUS_PENDING = new Color(255, 193, 7);
    private static final Color STATUS_DELAYED = new Color(220, 53, 69);
    private static final Color STATUS_FAILED = new Color(220, 53, 69);
    
    // Status background colors
    private static final Color STATUS_BG_DELIVERED = new Color(232, 245, 233);
    private static final Color STATUS_BG_TRANSIT = new Color(227, 242, 253);
    private static final Color STATUS_BG_PENDING = new Color(255, 243, 224);
    private static final Color STATUS_BG_DELAYED = new Color(255, 235, 238);
    private static final Color STATUS_BG_FAILED = new Color(255, 235, 238);
    
    // Row highlight color for delivered parcels
    private static final Color DELIVERED_ROW_COLOR = new Color(232, 245, 233); // Light green
    private static final Color DELIVERED_ROW_COLOR_ALT = new Color(220, 240, 220); // Slightly darker for alternating
    
    // Priority colors
    private static final Color PRIORITY_URGENT = DANGER;
    private static final Color PRIORITY_HIGH = new Color(237, 108, 2);
    private static final Color PRIORITY_NORMAL = SUCCESS;
    
    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    
    // Table Components
    private JTable deliveryTable;
    private DefaultTableModel deliveryModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
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
    private JPanel statsPanel;
    private JLabel[] statValues = new JLabel[4];
    private JPanel[] statCards = new JPanel[4];
    
    // Data Storage
    private OrderStorage orderStorage;
    private List<Order> orders;
    
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;
    private static final Color ACTIVE_FILTER_BORDER = new Color(46, 125, 50);

    public DeliveryPage() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Initialize order storage and load data
        orderStorage = new OrderStorage();
        orders = orderStorage.getAllOrders();
        
        initUI();
    }
    
    private void initUI() {
        JPanel headerPanel = createHeaderPanel();
        JPanel statsPanel = createStatsPanel();
        JSplitPane splitPane = createSplitPane();
        
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
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(33, 37, 41));
        
        JLabel subtitleLabel = new JLabel("Track parcels and update delivery status in real-time");
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(TEXT_GRAY);
        
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(titleContainer, BorderLayout.WEST);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        filterPanel.setBackground(BG_LIGHT);
        
        JLabel filterLabel = new JLabel("Search by:");
        filterLabel.setFont(HEADER_FONT);
        
        String[] columns = {"Parcel ID", "Recipient", "Phone No.", "Location", "Status", "Priority", "Last Updated"};
        filterColumnCombo = new JComboBox<>(columns);
        filterColumnCombo.setPreferredSize(new Dimension(120, 35));
        filterColumnCombo.setBackground(Color.WHITE);
        filterColumnCombo.setFont(REGULAR_FONT);
        
        searchTextField = new JTextField(15);
        searchTextField.setPreferredSize(new Dimension(150, 35));
        searchTextField.setFont(REGULAR_FONT);
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
        clearFilterBtn.setFont(REGULAR_FONT);
        clearFilterBtn.setBackground(Color.WHITE);
        clearFilterBtn.setForeground(TEXT_GRAY);
        clearFilterBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearFilterBtn.setPreferredSize(new Dimension(80, 35));
        clearFilterBtn.addActionListener(e -> {
            searchTextField.setText("");
            rowSorter.setRowFilter(null);
            filterColumnCombo.setSelectedIndex(0);
            resetCardBorders();
            currentStatusFilter = null;
            currentFilterIndex = -1;
        });
        
        filterPanel.add(filterLabel);
        filterPanel.add(filterColumnCombo);
        filterPanel.add(searchTextField);
        filterPanel.add(clearFilterBtn);
        
        headerPanel.add(filterPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BG_LIGHT);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        int pendingCount = orderStorage.getPendingCount();
        int inTransitCount = orderStorage.getInTransitCount();
        int delayedCount = orderStorage.getDelayedCount();
        int totalCount = orders.size();
        
        // Define colors for stat cards
        Color[] colors = {WARNING, INFO, DANGER, PRIMARY_GREEN};
        Color[] bgColors = {
            new Color(255, 243, 224),  // Light orange for pending
            new Color(227, 242, 253),  // Light blue for transit
            new Color(255, 235, 238),  // Light red for delayed
            new Color(232, 245, 233)   // Light green for total
        };
        
        String[] values = {String.valueOf(pendingCount), String.valueOf(inTransitCount), 
                          String.valueOf(delayedCount), String.valueOf(totalCount)};
        String[] titles = {"Pending", "In Transit", "Delayed", "Total Tasks"};
        String[] descriptions = {"Awaiting pickup", "Currently out", "Delivery issues", "All deliveries"};
        
        for (int i = 0; i < 4; i++) {
            JPanel card = createStatCard(titles[i], descriptions[i], values[i], colors[i], bgColors[i], i);
            statCards[i] = card;
            statsPanel.add(card);
        }
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, String description, String value, 
                                  Color color, Color bgColor, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STATS_FONT);
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(SMALL_FONT);
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);
        
        statValues[index] = valueLabel;
        
        // Add click handler for filtering (except for total tasks)
        if (index < 3) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final String filterStatus = title;
            final int cardIndex = index;
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(bgColor);
                    }
                }
                
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(Color.WHITE);
                    }
                }
                
                public void mouseClicked(MouseEvent e) {
                    applyStatusFilter(filterStatus, cardIndex, color);
                }
            });
        } else {
            // Total tasks card - clear filter
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(245, 245, 245));
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(Color.WHITE);
                }
                public void mouseClicked(MouseEvent e) {
                    clearAllFilters();
                }
            });
        }
        
        return card;
    }
    
    private void resetCardBorders() {
        if (statCards != null) {
            for (int i = 0; i < statCards.length; i++) {
                statCards[i].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                statCards[i].setBackground(Color.WHITE);
            }
        }
    }
    
    private void applyStatusFilter(String status, int cardIndex, Color color) {
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            currentStatusFilter = null;
            currentFilterIndex = -1;
            rowSorter.setRowFilter(null);
        } else {
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            rowSorter.setRowFilter(RowFilter.regexFilter("^" + status + "$", 4));
        }
    }
    
    private void clearAllFilters() {
        resetCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        rowSorter.setRowFilter(null);
        filterColumnCombo.setSelectedIndex(0);
        searchTextField.setText("");
    }
    
    private JSplitPane createSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        String[] columns = {"Parcel ID", "Recipient", "Phone No.", "Location", "Status", "Priority", "Last Updated"};
        
        // Load data from orders
        Object[][] data = loadDeliveryData();
        
        deliveryModel = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        rowSorter = new TableRowSorter<>(deliveryModel);
        
        deliveryTable = new JTable(deliveryModel);
        deliveryTable.setRowSorter(rowSorter);
        styleDeliveryTable(deliveryTable);
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
    
    private void styleDeliveryTable(JTable table) {
        table.setRowHeight(45);
        table.setIntercellSpacing(new Dimension(8, 3));
        table.setSelectionBackground(GREEN_LIGHT);
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(REGULAR_FONT);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        
        // Custom renderer for row highlighting based on status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                // Get the status for this row
                int modelRow = table.convertRowIndexToModel(row);
                String status = table.getModel().getValueAt(modelRow, 4).toString();
                
                // Apply background color based on status
                if (!isSelected) {
                    if ("Delivered".equals(status)) {
                        // Light green for delivered rows
                        if (row % 2 == 0) {
                            c.setBackground(DELIVERED_ROW_COLOR);
                        } else {
                            c.setBackground(DELIVERED_ROW_COLOR_ALT);
                        }
                    } else {
                        // Normal alternating rows for other statuses
                        if (row % 2 == 0) {
                            c.setBackground(new Color(252, 252, 253));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    }
                } else {
                    c.setBackground(GREEN_LIGHT);
                }
                
                // Set horizontal alignment for specific columns
                if (column == 4 || column == 5) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                return c;
            }
        });
        
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(new Color(33, 37, 41));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        // Status column renderer with pill-shaped frames
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            private final JLabel label = new JLabel();
            
            {
                panel.setOpaque(true);
                label.setFont(STATUS_FONT);
                label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                panel.add(label);
            }
            
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                // Set panel background based on selection and row status
                int modelRow = t.convertRowIndexToModel(row);
                String status = t.getModel().getValueAt(modelRow, 4).toString();
                
                if (!isSelected) {
                    if ("Delivered".equals(status)) {
                        if (row % 2 == 0) {
                            panel.setBackground(DELIVERED_ROW_COLOR);
                        } else {
                            panel.setBackground(DELIVERED_ROW_COLOR_ALT);
                        }
                    } else {
                        if (row % 2 == 0) {
                            panel.setBackground(new Color(252, 252, 253));
                        } else {
                            panel.setBackground(Color.WHITE);
                        }
                    }
                } else {
                    panel.setBackground(GREEN_LIGHT);
                }
                
                if (v != null) {
                    String statusText = v.toString();
                    label.setText(statusText);
                    label.setOpaque(true);
                    
                    switch (statusText) {
                        case "Delivered":
                            label.setForeground(STATUS_DELIVERED.darker());
                            label.setBackground(STATUS_BG_DELIVERED);
                            label.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(STATUS_DELIVERED, 1, true),
                                BorderFactory.createEmptyBorder(4, 12, 4, 12)
                            ));
                            break;
                        case "In Transit":
                        case "Out for Delivery":
                            label.setForeground(STATUS_TRANSIT.darker());
                            label.setBackground(STATUS_BG_TRANSIT);
                            label.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(STATUS_TRANSIT, 1, true),
                                BorderFactory.createEmptyBorder(4, 12, 4, 12)
                            ));
                            break;
                        case "Pending":
                            label.setForeground(STATUS_PENDING.darker());
                            label.setBackground(STATUS_BG_PENDING);
                            label.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(STATUS_PENDING, 1, true),
                                BorderFactory.createEmptyBorder(4, 12, 4, 12)
                            ));
                            break;
                        case "Delayed":
                            label.setForeground(STATUS_DELAYED.darker());
                            label.setBackground(STATUS_BG_DELAYED);
                            label.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(STATUS_DELAYED, 1, true),
                                BorderFactory.createEmptyBorder(4, 12, 4, 12)
                            ));
                            break;
                        case "Failed Delivery":
                            label.setForeground(STATUS_FAILED.darker());
                            label.setBackground(STATUS_BG_FAILED);
                            label.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(STATUS_FAILED, 1, true),
                                BorderFactory.createEmptyBorder(4, 12, 4, 12)
                            ));
                            break;
                        default:
                            label.setForeground(TEXT_GRAY);
                            label.setBackground(new Color(245, 245, 245));
                            label.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(TEXT_GRAY, 1, true),
                                BorderFactory.createEmptyBorder(4, 12, 4, 12)
                            ));
                            break;
                    }
                }
                
                return panel;
            }
        });
        
        // Priority column renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, v, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(STATUS_FONT);
                
                if (v != null) {
                    String val = v.toString();
                    
                    if (val.equals("Urgent")) {
                        setForeground(PRIORITY_URGENT);
                    } else if (val.equals("High")) {
                        setForeground(PRIORITY_HIGH);
                    } else {
                        setForeground(PRIORITY_NORMAL);
                    }
                }
                
                // Set background based on selection and row status
                int modelRow = t.convertRowIndexToModel(row);
                String status = t.getModel().getValueAt(modelRow, 4).toString();
                
                if (!isSelected) {
                    if ("Delivered".equals(status)) {
                        if (row % 2 == 0) {
                            setBackground(DELIVERED_ROW_COLOR);
                        } else {
                            setBackground(DELIVERED_ROW_COLOR_ALT);
                        }
                    } else {
                        if (row % 2 == 0) {
                            setBackground(new Color(252, 252, 253));
                        } else {
                            setBackground(Color.WHITE);
                        }
                    }
                } else {
                    setBackground(GREEN_LIGHT);
                }
                
                return this;
            }
        });
    }
    
    private Object[][] loadDeliveryData() {
        Object[][] data = new Object[orders.size()][7];
        
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            data[i][0] = order.id;
            data[i][1] = order.customer;
            data[i][2] = order.phone;
            data[i][3] = order.address;
            data[i][4] = order.status;
            data[i][5] = determinePriority(order);
            data[i][6] = formatLastUpdated(order.date);
        }
        
        return data;
    }
    
    private String determinePriority(Order order) {
        if ("Delayed".equals(order.status)) {
            return "Urgent";
        } else if (order.estDate != null) {
            return "High";
        } else {
            return "Normal";
        }
    }
    
    private String formatLastUpdated(String date) {
        if (date == null || date.isEmpty()) {
            return "Not set";
        }
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date orderDate = dbFormat.parse(date);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");
            return displayFormat.format(orderDate);
        } catch (Exception e) {
            return date;
        }
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
        parcelLabel.setFont(HEADER_FONT);
        fieldsPanel.add(parcelLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        selectedParcelLabel = new JLabel("None selected");
        selectedParcelLabel.setFont(REGULAR_FONT);
        selectedParcelLabel.setForeground(PRIMARY_GREEN);
        fieldsPanel.add(selectedParcelLabel, gbc);
        
        // Status Selection
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.2;
        JLabel statusLabel = new JLabel("New Status:");
        statusLabel.setFont(HEADER_FONT);
        fieldsPanel.add(statusLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        statusCombo = new JComboBox<>(new String[]{
            "Select Status...",
            "Out for Delivery",
            "Delivered", 
            "Failed Delivery",
            "Returning to Hub",
            "Held at Facility",
            "Delayed"
        });
        statusCombo.setFont(REGULAR_FONT);
        statusCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        statusCombo.setBackground(Color.WHITE);
        statusCombo.addActionListener(e -> togglePhotoVisibility());
        fieldsPanel.add(statusCombo, gbc);
        
        // Photo Upload
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.2;
        photoLabel = new JLabel("Photo Proof:");
        photoLabel.setFont(HEADER_FONT);
        fieldsPanel.add(photoLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        
        photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        photoPanel.setBackground(Color.WHITE);
        
        uploadPhotoBtn = new JButton("Select Photo");
        uploadPhotoBtn.setFont(SMALL_FONT);
        uploadPhotoBtn.setBackground(Color.WHITE);
        uploadPhotoBtn.setForeground(PRIMARY_GREEN);
        uploadPhotoBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN));
        uploadPhotoBtn.setPreferredSize(new Dimension(100, 25));
        uploadPhotoBtn.addActionListener(e -> selectPhotoFromFile());
        
        photoFileNameLabel = new JLabel("No file selected");
        photoFileNameLabel.setFont(SMALL_FONT);
        photoFileNameLabel.setForeground(TEXT_GRAY);
        
        JButton removePhotoBtn = new JButton("Remove");
        removePhotoBtn.setFont(SMALL_FONT);
        removePhotoBtn.setBackground(Color.WHITE);
        removePhotoBtn.setForeground(DANGER);
        removePhotoBtn.setBorder(BorderFactory.createLineBorder(DANGER));
        removePhotoBtn.setPreferredSize(new Dimension(70, 22));
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
        remarksLabel.setFont(HEADER_FONT);
        fieldsPanel.add(remarksLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        remarksArea = new JTextArea(2, 30);
        remarksArea.setFont(REGULAR_FONT);
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
        clearBtn.setFont(BUTTON_FONT);
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(TEXT_GRAY);
        clearBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        clearBtn.setPreferredSize(new Dimension(80, 28));
        clearBtn.addActionListener(e -> clearUpdateForm());
        
        JButton updateBtn = new JButton("Update Status");
        updateBtn.setFont(BUTTON_FONT);
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
    
    private void applyFilter() {
        String text = searchTextField.getText().trim();
        int columnIndex = filterColumnCombo.getSelectedIndex();
        
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
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
            uploadPhotoBtn.setText("Change Photo");
        }
    }
    
    private void removePhoto() {
        selectedPhotoFile = null;
        photoFileNameLabel.setText("No file selected");
        photoFileNameLabel.setForeground(TEXT_GRAY);
        uploadPhotoBtn.setText("Select Photo");
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
            
            // Suggest next status based on current status
            if (currentStatus.equals("Pending")) {
                statusCombo.setSelectedItem("Out for Delivery");
            } else if (currentStatus.equals("In Transit") || currentStatus.equals("Out for Delivery")) {
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
        
        // Update the table
        deliveryModel.setValueAt(selectedStatus, modelRow, 4);
        deliveryModel.setValueAt(new SimpleDateFormat("hh:mm a").format(new Date()), modelRow, 6);
        
        // Update the Order object and save to file
        Order updatedOrder = orderStorage.findOrder(parcelId);
        if (updatedOrder != null) {
            updatedOrder.status = selectedStatus;
            if (!remarks.isEmpty()) {
                updatedOrder.reason = remarks;
            }
            orderStorage.saveOrders();
        }
        
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
        
        // Refresh stats
        refreshStatsPanel();
        
        deliveryTable.clearSelection();
        clearUpdateForm();
        deliveryTable.repaint();
    }
    
    private void refreshStatsPanel() {
        // Update stat values
        int pendingCount = orderStorage.getPendingCount();
        int inTransitCount = orderStorage.getInTransitCount();
        int delayedCount = orderStorage.getDelayedCount();
        int totalCount = orders.size();
        
        if (statValues[0] != null) statValues[0].setText(String.valueOf(pendingCount));
        if (statValues[1] != null) statValues[1].setText(String.valueOf(inTransitCount));
        if (statValues[2] != null) statValues[2].setText(String.valueOf(delayedCount));
        if (statValues[3] != null) statValues[3].setText(String.valueOf(totalCount));
        
        Component topContainer = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if (topContainer instanceof JPanel) {
            JPanel container = (JPanel) topContainer;
            container.revalidate();
            container.repaint();
        }
    }
    
    private void clearUpdateForm() {
        selectedParcelLabel.setText("None selected");
        statusCombo.setSelectedIndex(0);
        remarksArea.setText("");
        removePhoto();
        togglePhotoVisibility();
    }
    
    public void refreshData() {
        // Reload orders from storage
        orderStorage = new OrderStorage();
        orders = orderStorage.getAllOrders();
        
        // Update table data
        Object[][] newData = loadDeliveryData();
        deliveryModel.setDataVector(newData, new String[]{"Parcel ID", "Recipient", "Phone No.", "Location", "Status", "Priority", "Last Updated"});
        
        // Refresh stats panel
        refreshStatsPanel();
        
        deliveryTable.repaint();
    }
}
