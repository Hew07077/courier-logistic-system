package admin.management;

import logistics.driver.Driver;
import logistics.driver.DriverStorage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class DriverManagement {
    private JPanel mainPanel;
    private JTable driversTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JPanel statsPanel;
    private JLabel[] statValues;
    private JPanel[] statCards;
    
    private DriverStorage storage;
    
    // Filter state
    private String currentStatusFilter = null;
    private int currentFilterIndex = -1;
    
    // Photo related
    private static final String PHOTO_DIR = "driver_photos/";
    private String currentPhotoPath = null;
    
    // Modern color scheme
    private static final Color PRIMARY = new Color(46, 125, 50);
    private static final Color PRIMARY_DARK = new Color(27, 94, 32);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color INFO = new Color(23, 162, 184);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color BG_COLOR = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color TEXT_MUTED = new Color(134, 142, 150);
    private static final Color HOVER_COLOR = new Color(245, 247, 250);
    private static final Color SELECTION_COLOR = new Color(232, 245, 233);
    private static final Color ACTIVE_FILTER_BORDER = PRIMARY;
    
    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    
    public DriverManagement() {
        storage = new DriverStorage();
        ensurePhotoDirectoryExists();
        createMainPanel();
        refreshTable();
    }
    
    private void ensurePhotoDirectoryExists() {
        File directory = new File(PHOTO_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    private String savePhoto(File sourceFile, String driverId) {
        if (sourceFile == null) return null;
        
        ensurePhotoDirectoryExists();
        
        // Get file extension
        String fileName = sourceFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        // Create new filename with driver ID
        String newFileName = driverId + extension;
        File destFile = new File(PHOTO_DIR + newFileName);
        
        try {
            // Copy file
            java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return PHOTO_DIR + newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private ImageIcon loadDriverPhoto(String photoPath, int width, int height) {
        if (photoPath == null || photoPath.isEmpty()) {
            return createDefaultPhoto(width, height, null);
        }
        
        try {
            File photoFile = new File(photoPath);
            if (!photoFile.exists()) {
                return createDefaultPhoto(width, height, null);
            }
            
            BufferedImage img = ImageIO.read(photoFile);
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (IOException e) {
            e.printStackTrace();
            return createDefaultPhoto(width, height, null);
        }
    }
    
    private ImageIcon createDefaultPhoto(int width, int height, String initial) {
        BufferedImage defaultImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultImg.createGraphics();
        
        // Draw a colored circle
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(PRIMARY);
        g2d.fillOval(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, height / 2));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = initial != null && !initial.isEmpty() ? initial.substring(0, 1).toUpperCase() : "?";
        int x = (width - fm.stringWidth(text)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return new ImageIcon(defaultImg);
    }
    
    private JPanel createPhotoUploadPanel(String driverId, Driver existingDriver) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "Driver Photo",
            TitledBorder.LEFT, TitledBorder.TOP, HEADER_FONT, PRIMARY));
        
        // Photo display
        JLabel photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Load existing photo if available
        if (existingDriver != null && existingDriver.photoPath != null) {
            photoLabel.setIcon(loadDriverPhoto(existingDriver.photoPath, 150, 150));
            currentPhotoPath = existingDriver.photoPath;
        } else {
            String initial = existingDriver != null ? existingDriver.name : "";
            photoLabel.setIcon(createDefaultPhoto(150, 150, initial));
        }
        
        // Upload button
        JButton uploadBtn = new JButton("Upload Photo");
        uploadBtn.setFont(REGULAR_FONT);
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setBackground(PRIMARY);
        uploadBtn.setBorderPainted(false);
        uploadBtn.setFocusPainted(false);
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
            
            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String tempId = driverId != null ? driverId : "temp_" + System.currentTimeMillis();
                currentPhotoPath = savePhoto(selectedFile, tempId);
                
                // Update photo display
                ImageIcon icon = loadDriverPhoto(currentPhotoPath, 150, 150);
                photoLabel.setIcon(icon);
            }
        });
        
        JButton removeBtn = new JButton("Remove");
        removeBtn.setFont(REGULAR_FONT);
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setBackground(DANGER);
        removeBtn.setBorderPainted(false);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.addActionListener(e -> {
            currentPhotoPath = null;
            String initial = existingDriver != null ? existingDriver.name : "";
            photoLabel.setIcon(createDefaultPhoto(150, 150, initial));
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(uploadBtn);
        buttonPanel.add(removeBtn);
        
        panel.add(photoLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_COLOR);
        
        // Top container with header and stats
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createStatsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Driver Management");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Manage drivers, assignments, and performance");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] titles = {"Total Drivers", "Available", "On Delivery", "Off Duty"};
        String[] descriptions = {"All drivers", "Ready for work", "Currently delivering", "Not working"};
        Color[] colors = {PRIMARY, SUCCESS, INFO, TEXT_SECONDARY};
        Color[] bgColors = {
            new Color(232, 245, 233),
            new Color(232, 245, 233),
            new Color(227, 242, 253),
            new Color(245, 245, 245)
        };
        
        statValues = new JLabel[4];
        statCards = new JPanel[4];
        
        for (int i = 0; i < 4; i++) {
            JPanel card = createStatCard(titles[i], descriptions[i], "0", colors[i], bgColors[i], i);
            statCards[i] = card;
            statsPanel.add(card);
        }
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, String description, String value, 
                                  Color color, Color bgColor, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STATS_FONT);
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(SMALL_FONT);
        descLabel.setForeground(TEXT_MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);
        
        statValues[index] = valueLabel;
        
        // Make cards clickable (all except Total)
        if (index > 0) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final String filterStatus = title;
            final int cardIndex = index;
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(bgColor);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(color, 1, true),
                            BorderFactory.createEmptyBorder(7, 11, 7, 11)
                        ));
                    }
                }
                
                public void mouseExited(MouseEvent e) {
                    if (currentFilterIndex != cardIndex) {
                        card.setBackground(CARD_BG);
                        card.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1, true),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                        ));
                    }
                }
                
                public void mouseClicked(MouseEvent e) {
                    applyStatusFilter(filterStatus, cardIndex, color);
                }
            });
        } else {
            // Total card - clear all filters
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(HOVER_COLOR);
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(CARD_BG);
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
                statCards[i].setBackground(CARD_BG);
            }
        }
    }
    
    private void applyStatusFilter(String status, int cardIndex, Color color) {
        resetCardBorders();
        
        if (currentFilterIndex == cardIndex) {
            // Clicking the same card again - clear filter
            currentStatusFilter = null;
            currentFilterIndex = -1;
            applyFilters();
        } else {
            // Apply new filter
            currentStatusFilter = status;
            currentFilterIndex = cardIndex;
            
            // Highlight selected card
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color.brighter());
            
            applyFilters();
        }
    }
    
    private void clearAllFilters() {
        resetCardBorders();
        currentStatusFilter = null;
        currentFilterIndex = -1;
        rowSorter.setRowFilter(null);
    }
    
    private void applyFilters() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        // Add status filter
        if (currentStatusFilter != null) {
            String status = currentStatusFilter;
            if (status.equals("Available")) {
                filters.add(RowFilter.regexFilter("^Available$", 3));
            } else if (status.equals("On Delivery")) {
                filters.add(RowFilter.regexFilter("^On Delivery$", 3));
            } else if (status.equals("Off Duty")) {
                filters.add(RowFilter.regexFilter("^(Off Duty|On Leave)$", 3));
            }
        }
        
        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createTable() {
        String[] columns = {"ID", "Name", "Phone", "Status", "License No", "License Expiry", 
                            "Vehicle ID", "Deliveries", "Rating", "Join Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        driversTable = new JTable(tableModel);
        driversTable.setRowHeight(40);
        driversTable.setFont(REGULAR_FONT);
        driversTable.setSelectionBackground(SELECTION_COLOR);
        driversTable.setSelectionForeground(TEXT_PRIMARY);
        driversTable.setShowGrid(true);
        driversTable.setGridColor(BORDER_COLOR);
        driversTable.setIntercellSpacing(new Dimension(8, 3));
        driversTable.setFillsViewportHeight(true);
        
        // Table header styling
        JTableHeader header = driversTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        // Row sorter
        rowSorter = new TableRowSorter<>(tableModel);
        driversTable.setRowSorter(rowSorter);
        
        // Set column widths
        driversTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        driversTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        driversTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        driversTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        driversTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        driversTable.getColumnModel().getColumn(8).setPreferredWidth(70);
        driversTable.getColumnModel().getColumn(9).setPreferredWidth(100);
        
        // Set custom renderers
        driversTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        driversTable.getColumnModel().getColumn(8).setCellRenderer(new RatingCellRenderer());
        
        // Double-click listener
        driversTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && driversTable.getSelectedRow() != -1) {
                    showDriverDetails();
                }
            }
        });
        
        refreshTable();
        
        return driversTable;
    }
    
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected) {
                String status = value != null ? value.toString() : "";
                
                switch (status) {
                    case "Available":
                        label.setForeground(SUCCESS);
                        label.setText("● " + status);
                        break;
                    case "On Delivery":
                        label.setForeground(INFO);
                        label.setText("🚚 " + status);
                        break;
                    case "Off Duty":
                        label.setForeground(TEXT_SECONDARY);
                        label.setText("○ " + status);
                        break;
                    case "On Leave":
                        label.setForeground(WARNING);
                        label.setText("✗ " + status);
                        break;
                }
            }
            
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return label;
        }
    }
    
    private class RatingCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (value != null && !isSelected) {
                try {
                    double rating = Double.parseDouble(value.toString());
                    if (rating >= 4.5) {
                        label.setForeground(SUCCESS);
                    } else if (rating >= 3.5) {
                        label.setForeground(WARNING);
                    } else {
                        label.setForeground(DANGER);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            return label;
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.setBackground(BG_COLOR);
        
        JButton addBtn = createButton("Add Driver", SUCCESS, new Color(30, 126, 52));
        addBtn.addActionListener(e -> addDriver());
        panel.add(addBtn);
        
        JButton editBtn = createButton("Edit", WARNING, new Color(204, 154, 6));
        editBtn.addActionListener(e -> editDriver());
        panel.add(editBtn);
        
        JButton viewBtn = createButton("View Details", INFO, new Color(17, 122, 139));
        viewBtn.addActionListener(e -> showDriverDetails());
        panel.add(viewBtn);
        
        JButton deleteBtn = createButton("Delete", DANGER, new Color(176, 42, 55));
        deleteBtn.addActionListener(e -> deleteDriver());
        panel.add(deleteBtn);
        
        JButton assignBtn = createButton("Assign Vehicle", PRIMARY, PRIMARY_DARK);
        assignBtn.addActionListener(e -> assignVehicle());
        panel.add(assignBtn);
        
        JButton scheduleBtn = createButton("Schedule", new Color(111, 66, 193), new Color(81, 45, 168));
        scheduleBtn.addActionListener(e -> scheduleDriver());
        panel.add(scheduleBtn);
        
        return panel;
    }
    
    private JButton createButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void showDriverDetails() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to view", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Driver driver = storage.findDriver(id);
        if (driver == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Driver Details - " + driver.name, true);
        dialog.setSize(700, 650);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        // Header with photo
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(CARD_BG);
        
        // Photo panel
        JPanel photoPanel = new JPanel();
        photoPanel.setBackground(CARD_BG);
        photoPanel.setPreferredSize(new Dimension(100, 100));
        photoPanel.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));
        
        JLabel detailPhotoLabel = new JLabel();
        detailPhotoLabel.setPreferredSize(new Dimension(96, 96));
        
        // Load driver photo
        if (driver.photoPath != null) {
            detailPhotoLabel.setIcon(loadDriverPhoto(driver.photoPath, 96, 96));
        } else {
            detailPhotoLabel.setIcon(createDefaultPhoto(96, 96, driver.name));
        }
        
        photoPanel.add(detailPhotoLabel);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(CARD_BG);
        
        JLabel nameLabel = new JLabel(driver.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(PRIMARY);
        
        JLabel idLabel = new JLabel(driver.id + " • " + driver.status);
        idLabel.setFont(REGULAR_FONT);
        idLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(nameLabel);
        titlePanel.add(idLabel);
        
        headerPanel.add(photoPanel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(CARD_BG);
        
        // Contact Information
        detailsPanel.add(createDetailSection("Contact Information", new String[]{
            "Phone: " + driver.phone,
            "Email: " + (driver.email != null ? driver.email : "-"),
            "Emergency Contact: " + (driver.emergencyContact != null ? 
                driver.emergencyContact + " (" + driver.emergencyPhone + ")" : "-"),
            "Address: " + (driver.address != null ? driver.address : "-")
        }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // License Information
        detailsPanel.add(createDetailSection("License Information", new String[]{
            "License Number: " + driver.licenseNumber,
            "Expiry Date: " + driver.licenseExpiry,
            "Status: " + (isLicenseExpiring(driver.licenseExpiry) ? "⚠️ Expiring Soon" : "Valid")
        }));
        
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Assignment Information
        List<String> assignmentInfo = new ArrayList<>();
        assignmentInfo.add("Vehicle: " + (driver.vehicleId != null ? driver.vehicleId : "Not Assigned"));
        assignmentInfo.add("Join Date: " + driver.joinDate);
        assignmentInfo.add("Total Deliveries: " + driver.totalDeliveries);
        assignmentInfo.add("Rating: " + driver.getFormattedRating());
        
        detailsPanel.add(createDetailSection("Assignment Information", 
            assignmentInfo.toArray(new String[0])));
        
        if (driver.notes != null && !driver.notes.isEmpty()) {
            detailsPanel.add(Box.createVerticalStrut(10));
            detailsPanel.add(createDetailSection("Notes", new String[]{driver.notes}));
        }
        
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(BUTTON_FONT);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(85, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean isLicenseExpiring(String expiryDate) {
        if (expiryDate == null) return false;
        try {
            Date expiry = new SimpleDateFormat("yyyy-MM-dd").parse(expiryDate);
            Date today = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.add(Calendar.MONTH, 3);
            Date threeMonthsLater = cal.getTime();
            return expiry.before(threeMonthsLater);
        } catch (Exception e) {
            return false;
        }
    }
    
    private JPanel createDetailSection(String title, String[] lines) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        JPanel linesPanel = new JPanel(new GridLayout(lines.length, 1, 0, 3));
        linesPanel.setBackground(CARD_BG);
        linesPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        for (String line : lines) {
            JLabel lineLabel = new JLabel(line);
            lineLabel.setFont(REGULAR_FONT);
            lineLabel.setForeground(TEXT_PRIMARY);
            linesPanel.add(lineLabel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(linesPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addDriver() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Add New Driver", true);
        dialog.setSize(850, 700);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Add New Driver");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        
        // Main content panel with form and photo
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        // Form panel (left side)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(5, 10, 5, 10);
        
        String[] labels = {"Full Name:*", "Phone:*", "Email:", "License Number:*", 
                          "License Expiry:* (YYYY-MM-DD)", "Emergency Contact:", 
                          "Emergency Phone:", "Address:"};
        
        // Create arrays to store components
        JTextField[] textFields = new JTextField[7];
        JScrollPane addressScrollPane = null;
        
        for (int i = 0; i < labels.length; i++) {
            fGbc.gridx = 0;
            fGbc.gridy = i;
            fGbc.weightx = 0.3;
            JLabel label = new JLabel(labels[i]);
            label.setFont(HEADER_FONT);
            formPanel.add(label, fGbc);
            
            fGbc.gridx = 1;
            fGbc.weightx = 0.7;
            
            if (labels[i].contains("Address")) {
                JTextArea textArea = new JTextArea(3, 20);
                textArea.setFont(REGULAR_FONT);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
                addressScrollPane = new JScrollPane(textArea);
                addressScrollPane.setPreferredSize(new Dimension(250, 60));
                formPanel.add(addressScrollPane, fGbc);
            } else {
                if (i < 7) {
                    textFields[i] = new JTextField(20);
                    textFields[i].setFont(REGULAR_FONT);
                    textFields[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    formPanel.add(textFields[i], fGbc);
                }
            }
        }
        
        // Status selection
        fGbc.gridx = 0;
        fGbc.gridy = labels.length;
        fGbc.weightx = 0.3;
        JLabel statusLabel = new JLabel("Initial Status:");
        statusLabel.setFont(HEADER_FONT);
        formPanel.add(statusLabel, fGbc);
        
        fGbc.gridx = 1;
        fGbc.weightx = 0.7;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Available", "Off Duty"});
        statusCombo.setFont(REGULAR_FONT);
        statusCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        formPanel.add(statusCombo, fGbc);
        
        // Add form panel to left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        contentPanel.add(new JScrollPane(formPanel), gbc);
        
        // Photo upload panel (right side)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        
        // Generate temporary ID for photo
        String tempId = "temp_" + System.currentTimeMillis();
        JPanel photoPanel = createPhotoUploadPanel(tempId, null);
        contentPanel.add(photoPanel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Save Driver");
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(SUCCESS);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(100, 35));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> {
            currentPhotoPath = null;
            dialog.dispose();
        });
        
        final JTextField[] finalTextFields = textFields;
        final JComboBox<String> finalStatusCombo = statusCombo;
        final JScrollPane finalAddressScrollPane = addressScrollPane;
        
        saveBtn.addActionListener(e -> {
            if (validateAndSaveDriverWithPhoto(dialog, finalTextFields, finalStatusCombo, 
                                               finalAddressScrollPane, tempId)) {
                dialog.dispose();
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private boolean validateAndSaveDriverWithPhoto(JDialog dialog, JTextField[] textFields, 
                                                  JComboBox<String> statusCombo, 
                                                  JScrollPane addressScrollPane,
                                                  String tempId) {
        try {
            // Validate required fields
            if (textFields[0].getText().trim().isEmpty() || 
                textFields[1].getText().trim().isEmpty() || 
                textFields[3].getText().trim().isEmpty() || 
                textFields[4].getText().trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(dialog, 
                    "Please fill in all required fields (*)", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Get address from text area
            String address = "";
            if (addressScrollPane != null) {
                JTextArea addressArea = (JTextArea) addressScrollPane.getViewport().getView();
                address = addressArea.getText().trim();
            }
            
            // Create new driver
            String driverId = storage.generateNewId();
            Driver driver = new Driver(
                driverId,
                textFields[0].getText().trim(),
                textFields[1].getText().trim(),
                textFields[2].getText().trim(),
                textFields[3].getText().trim(),
                textFields[4].getText().trim()
            );
            
            // Set optional fields
            if (textFields.length > 5 && textFields[5] != null && !textFields[5].getText().trim().isEmpty()) {
                driver.emergencyContact = textFields[5].getText().trim();
            }
            if (textFields.length > 6 && textFields[6] != null && !textFields[6].getText().trim().isEmpty()) {
                driver.emergencyPhone = textFields[6].getText().trim();
            }
            if (!address.isEmpty()) {
                driver.address = address;
            }
            
            driver.status = (String) statusCombo.getSelectedItem();
            
            // Handle photo
            if (currentPhotoPath != null && currentPhotoPath.contains("temp_")) {
                // Rename the temp file to actual driver ID
                File tempFile = new File(currentPhotoPath);
                String extension = currentPhotoPath.substring(currentPhotoPath.lastIndexOf('.'));
                String newPath = PHOTO_DIR + driverId + extension;
                File newFile = new File(newPath);
                
                if (tempFile.renameTo(newFile)) {
                    driver.photoPath = newPath;
                }
            } else if (currentPhotoPath != null) {
                driver.photoPath = currentPhotoPath;
            }
            
            storage.addDriver(driver);
            refreshTable();
            
            JOptionPane.showMessageDialog(dialog, 
                "Driver added successfully!\nID: " + driverId, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            currentPhotoPath = null;
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, 
                "Error adding driver: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void editDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to edit", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Driver driver = storage.findDriver(id);
        if (driver == null) return;
        
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), 
                                      "Edit Driver - " + driver.name, true);
        dialog.setSize(850, 700);
        dialog.setLocationRelativeTo(mainPanel);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_BG);
        
        JLabel titleLabel = new JLabel("Edit Driver");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(5, 10, 5, 10);
        
        String[] labels = {"Name:*", "Phone:*", "Email:", "License Number:*", 
                          "License Expiry:*", "Status:", "Vehicle ID:", 
                          "Emergency Contact:", "Emergency Phone:", "Address:", "Notes:"};
        
        JTextField nameField = new JTextField(driver.name, 20);
        JTextField phoneField = new JTextField(driver.phone, 20);
        JTextField emailField = new JTextField(driver.email != null ? driver.email : "", 20);
        JTextField licenseField = new JTextField(driver.licenseNumber, 20);
        JTextField expiryField = new JTextField(driver.licenseExpiry, 20);
        
        JComboBox<String> statusCombo = new JComboBox<>(
            new String[]{"Available", "On Delivery", "Off Duty", "On Leave"});
        statusCombo.setSelectedItem(driver.status);
        
        JTextField vehicleField = new JTextField(driver.vehicleId != null ? driver.vehicleId : "", 20);
        JTextField emergencyContactField = new JTextField(
            driver.emergencyContact != null ? driver.emergencyContact : "", 20);
        JTextField emergencyPhoneField = new JTextField(
            driver.emergencyPhone != null ? driver.emergencyPhone : "", 20);
        
        JTextArea addressArea = new JTextArea(driver.address != null ? driver.address : "", 3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setPreferredSize(new Dimension(250, 60));
        
        JTextArea notesArea = new JTextArea(driver.notes != null ? driver.notes : "", 3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(250, 60));
        
        JComponent[] fields = {nameField, phoneField, emailField, licenseField, 
                               expiryField, statusCombo, vehicleField, 
                               emergencyContactField, emergencyPhoneField, 
                               addressScroll, notesScroll};
        
        for (int i = 0; i < labels.length; i++) {
            fGbc.gridx = 0;
            fGbc.gridy = i;
            fGbc.weightx = 0.3;
            JLabel label = new JLabel(labels[i]);
            label.setFont(HEADER_FONT);
            formPanel.add(label, fGbc);
            
            fGbc.gridx = 1;
            fGbc.weightx = 0.7;
            formPanel.add(fields[i], fGbc);
        }
        
        // Add form panel to left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        contentPanel.add(new JScrollPane(formPanel), gbc);
        
        // Photo upload panel
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        
        JPanel photoPanel = createPhotoUploadPanel(driver.id, driver);
        contentPanel.add(photoPanel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(WARNING);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(120, 35));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(BUTTON_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> {
            currentPhotoPath = null;
            dialog.dispose();
        });
        
        saveBtn.addActionListener(e -> {
            driver.name = nameField.getText().trim();
            driver.phone = phoneField.getText().trim();
            driver.email = emailField.getText().trim().isEmpty() ? null : emailField.getText().trim();
            driver.licenseNumber = licenseField.getText().trim();
            driver.licenseExpiry = expiryField.getText().trim();
            driver.status = (String) statusCombo.getSelectedItem();
            driver.vehicleId = vehicleField.getText().trim().isEmpty() ? null : vehicleField.getText().trim();
            driver.emergencyContact = emergencyContactField.getText().trim().isEmpty() ? null : emergencyContactField.getText().trim();
            driver.emergencyPhone = emergencyPhoneField.getText().trim().isEmpty() ? null : emergencyPhoneField.getText().trim();
            driver.address = addressArea.getText().trim().isEmpty() ? null : addressArea.getText().trim();
            driver.notes = notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim();
            driver.photoPath = currentPhotoPath;
            
            storage.updateDriver(driver);
            refreshTable();
            JOptionPane.showMessageDialog(dialog, "Driver updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            currentPhotoPath = null;
            dialog.dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to delete", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "Are you sure you want to delete driver " + name + " (" + id + ")?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Delete photo file if exists
            Driver driver = storage.findDriver(id);
            if (driver != null && driver.photoPath != null) {
                File photoFile = new File(driver.photoPath);
                if (photoFile.exists()) {
                    photoFile.delete();
                }
            }
            
            storage.removeDriver(id);
            refreshTable();
            JOptionPane.showMessageDialog(mainPanel, "Driver deleted successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void assignVehicle() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        Driver driver = storage.findDriver(id);
        if (driver == null) return;
        
        String vehicleId = JOptionPane.showInputDialog(mainPanel,
            "Enter Vehicle ID to assign to " + driver.name + ":",
            driver.vehicleId != null ? driver.vehicleId : "");
        
        if (vehicleId != null) {
            driver.vehicleId = vehicleId.trim().isEmpty() ? null : vehicleId.trim();
            storage.updateDriver(driver);
            refreshTable();
            
            if (driver.vehicleId != null) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Vehicle " + vehicleId + " assigned to " + driver.name,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void scheduleDriver() {
        int row = driversTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(mainPanel, "Please select a driver to schedule", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = driversTable.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        
        String[] options = {"Morning Shift (6AM-2PM)", "Afternoon Shift (2PM-10PM)", 
                           "Night Shift (10PM-6AM)", "Day Off", "On Leave"};
        
        String shift = (String) JOptionPane.showInputDialog(mainPanel,
            "Select schedule for " + name + ":",
            "Driver Schedule",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (shift != null) {
            JOptionPane.showMessageDialog(mainPanel,
                name + " scheduled for: " + shift,
                "Schedule Updated",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Driver d : storage.getAllDrivers()) {
            tableModel.addRow(new Object[]{
                d.id,
                d.name,
                d.phone,
                d.status,
                d.licenseNumber,
                d.licenseExpiry,
                d.vehicleId != null ? d.vehicleId : "-",
                d.totalDeliveries,
                d.rating,
                d.joinDate
            });
        }
        
        updateStats();
    }
    
    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            int total = storage.getTotalCount();
            int available = storage.getAvailableCount();
            int onDelivery = storage.getOnDeliveryCount();
            int offDuty = storage.getOffDutyCount();
            
            if (statValues[0] != null) statValues[0].setText(String.valueOf(total));
            if (statValues[1] != null) statValues[1].setText(String.valueOf(available));
            if (statValues[2] != null) statValues[2].setText(String.valueOf(onDelivery));
            if (statValues[3] != null) statValues[3].setText(String.valueOf(offDuty));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        });
    }
    
    // ==================== PUBLIC METHODS FOR ADMIN DASHBOARD ====================
    
    public JPanel getMainPanel() { 
        refreshTable();
        return mainPanel; 
    }
    
    public JPanel getRefreshedPanel() { 
        refreshTable(); 
        return mainPanel; 
    }
    
    public void refreshData() { 
        refreshTable(); 
    }
    
    // Statistics methods for dashboard
    public int getTotalCount() {
        return storage.getTotalCount();
    }
    
    public int getAvailableCount() {
        return storage.getAvailableCount();
    }
    
    public int getOnDeliveryCount() {
        return storage.getOnDeliveryCount();
    }
    
    public int getOnDutyCount() {
        // On Duty = Available + On Delivery
        return storage.getAvailableCount() + storage.getOnDeliveryCount();
    }
    
    public int getOffDutyCount() {
        return storage.getOffDutyCount();
    }
    
    public int getPendingCount() {
        // For compatibility with OrderManagement's method pattern
        // Returns drivers that are available (pending assignment)
        return storage.getAvailableCount();
    }
}