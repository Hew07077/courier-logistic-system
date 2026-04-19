// AdminManagementModule.java (Updated - added getDriverManagement and getVehicleManagement methods)
package admin.management;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class AdminManagementModule extends JPanel implements ManagementModule {
    
    // ==================== COMMON COLOR PALETTE ====================
    protected static final Color PRIMARY = new Color(255, 140, 0);
    protected static final Color PRIMARY_DARK = new Color(235, 120, 0);
    protected static final Color PRIMARY_LIGHT = new Color(255, 160, 40);
    protected static final Color SUCCESS = new Color(40, 167, 69);
    protected static final Color SUCCESS_DARK = new Color(30, 126, 52);
    protected static final Color WARNING = new Color(255, 193, 7);
    protected static final Color WARNING_DARK = new Color(204, 154, 6);
    protected static final Color DANGER = new Color(220, 53, 69);
    protected static final Color DANGER_DARK = new Color(176, 42, 55);
    protected static final Color INFO = new Color(23, 162, 184);
    protected static final Color INFO_DARK = new Color(17, 122, 139);
    protected static final Color PURPLE = new Color(111, 66, 193);
    protected static final Color PURPLE_DARK = new Color(88, 53, 154);
    protected static final Color ORANGE = new Color(255, 87, 34);
    
    protected static final Color BG_COLOR = new Color(248, 249, 250);
    protected static final Color CARD_BG = Color.WHITE;
    protected static final Color BORDER_COLOR = new Color(222, 226, 230);
    protected static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    protected static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    protected static final Color TEXT_MUTED = new Color(134, 142, 150);
    protected static final Color TEXT_GRAY = new Color(108, 117, 125);
    protected static final Color HOVER_COLOR = new Color(255, 245, 235);
    protected static final Color SELECTION_COLOR = new Color(255, 245, 235);
    protected static final Color ACTIVE_FILTER_BORDER = PRIMARY;
    
    // ==================== COMMON FONTS ====================
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    protected static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    protected static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    protected static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
    protected static final Font STATS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    protected static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 11);
    protected static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 11);
    
    // ==================== COMMON DATE FORMATTERS ====================
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    
    // ==================== COMMON COMPONENTS ====================
    protected JTable table;
    protected DefaultTableModel tableModel;
    protected TableRowSorter<DefaultTableModel> rowSorter;
    protected JPanel statsPanel;
    protected JLabel[] statValues;
    protected JPanel[] statCards;
    protected JPanel headerPanel;
    protected JPanel buttonPanel;
    protected JPanel statusBar;
    protected JLabel statusLabel;
    protected JLabel rightStatusLabel;
    
    // ==================== FILTER STATE ====================
    protected String currentStatusFilter = null;
    protected int currentFilterIndex = -1;
    protected boolean dataLoaded = false;
    
    // ==================== CONSTRUCTORS ====================
    
    public AdminManagementModule() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG_COLOR);
    }
    
    // ==================== ABSTRACT METHODS ====================
    
    protected abstract String getModuleName();
    protected abstract String getSubtitle();
    protected abstract void initializeUI();
    protected abstract void loadData();
    protected abstract void createTable();
    protected abstract void populateTableData();
    protected abstract void updateStats();
    protected abstract String[] getTableColumns();
    protected abstract int[] getColumnWidths();
    protected abstract void applyStatusFilter(String status, int cardIndex);
    protected abstract void clearFilters();
    public abstract int getTotalCount();
    
    // ==================== OPTIONAL OVERRIDABLE METHODS FOR INTEGRATION ====================
    
    /**
     * Override this method if the module needs to access DriverManagement
     */
    protected DriverManagement getDriverManagement() {
        return null;
    }
    
    /**
     * Override this method if the module needs to access VehicleManagement
     */
    protected VehicleManagement getVehicleManagement() {
        return null;
    }
    
    /**
     * Override this method if the module needs to access OrderManagement
     */
    protected OrderManagement getOrderManagement() {
        return null;
    }
    
    /**
     * Override this method if the module needs to access MaintenanceManagement
     */
    protected MaintenanceManagement getMaintenanceManagement() {
        return null;
    }
    
    // ==================== IMPLEMENTED METHODS FROM ManagementModule ====================
    
    @Override
    public JPanel getMainPanel() {
        if (!dataLoaded) {
            loadData();
            dataLoaded = true;
        }
        refreshData();
        return this;
    }
    
    @Override
    public JPanel getRefreshedPanel() {
        refreshData();
        return this;
    }
    
    @Override
    public void refreshData() {
        loadData();
        refreshTable();
        updateStats();
        updateStatusBar();
        revalidate();
        repaint();
    }
    
    // ==================== COMMON TABLE METHODS ====================
    
    protected void refreshTable() {
        if (tableModel != null) {
            tableModel.setRowCount(0);
            populateTableData();
        }
    }
    
    protected void configureTable() {
        table.setRowHeight(45);
        table.setFont(REGULAR_FONT);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
        
        int[] widths = getColumnWidths();
        if (widths != null && widths.length == table.getColumnCount()) {
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
    }
    
    protected int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        return table.convertRowIndexToModel(viewRow);
    }
    
    protected boolean validateRowSelection() {
        if (table.getSelectedRow() < 0) {
            showWarning("Please select a record to " + getActionName());
            return false;
        }
        return true;
    }
    
    protected String getActionName() {
        return "perform this action";
    }
    
    // ==================== COMMON STATS CARD METHODS ====================
    
    protected JPanel createStatCard(String title, String description, String initialValue, 
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
        
        JLabel valueLabel = new JLabel(initialValue);
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
        
        if (statValues == null) statValues = new JLabel[100];
        if (statCards == null) statCards = new JPanel[100];
        
        statValues[index] = valueLabel;
        statCards[index] = card;
        
        return card;
    }
    
    protected void resetCardBorders() {
        if (statCards != null) {
            for (int i = 0; i < statCards.length; i++) {
                if (statCards[i] != null) {
                    statCards[i].setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                    statCards[i].setBackground(CARD_BG);
                }
            }
        }
    }
    
    protected void highlightCard(int cardIndex, Color color) {
        if (cardIndex >= 0 && cardIndex < statCards.length && statCards[cardIndex] != null) {
            statCards[cardIndex].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACTIVE_FILTER_BORDER, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
            statCards[cardIndex].setBackground(color != null ? color.brighter() : HOVER_COLOR);
        }
    }
    
    // ==================== COMMON PANEL CREATION ====================
    
    protected JPanel createHeaderPanel() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel(getModuleName());
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel(getSubtitle());
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    protected JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setPreferredSize(new Dimension(1100, 450));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    protected JPanel createStatsPanelContainer(int columns) {
        statsPanel = new JPanel(new GridLayout(1, columns, 12, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        return statsPanel;
    }
    
    protected JPanel createButtonPanel() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        buttonPanel.setBackground(BG_COLOR);
        return buttonPanel;
    }
    
    // ==================== STATUS BAR METHODS ====================
    
    protected JPanel createStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(248, 249, 250));
        statusBar.setPreferredSize(new Dimension(getWidth(), 35));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        rightStatusLabel = new JLabel();
        rightStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightStatusLabel.setForeground(PRIMARY);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(rightStatusLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    protected void updateStatusBar() {
        if (statusLabel != null) {
            String filterInfo = "";
            if (currentStatusFilter != null) {
                filterInfo = " | Filter: " + currentStatusFilter;
            }
            statusLabel.setText("  System Status: ● Online" + filterInfo);
        }
        if (rightStatusLabel != null) {
            rightStatusLabel.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + "  ");
        }
    }
    
    // ==================== COMMON BUTTON METHODS ====================
    
    protected JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    protected JButton createStyledButton(String text, Color bgColor, int width, int height) {
        JButton btn = createStyledButton(text, bgColor);
        btn.setPreferredSize(new Dimension(width, height));
        return btn;
    }
    
    // ==================== COMMON DIALOG METHODS ====================
    
    protected JDialog createDialog(String title, int width, int height) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        return dialog;
    }
    
    protected JPanel createDialogButtonPanel(JDialog dialog, String saveText, 
                                              Color saveColor, Color hoverColor, Runnable saveAction) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(CARD_BG);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(REGULAR_FONT);
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(CARD_BG);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cancelBtn.setPreferredSize(new Dimension(80, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveBtn = new JButton(saveText);
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(saveColor);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(100, 35));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(saveColor);
            }
        });
        
        saveBtn.addActionListener(e -> saveAction.run());
        
        panel.add(cancelBtn);
        panel.add(saveBtn);
        
        return panel;
    }
    
    // ==================== COMMON STYLED COMPONENTS ====================
    
    protected JTextField createStyledTextField() {
        return createStyledTextField("");
    }
    
    protected JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(REGULAR_FONT);
        field.setPreferredSize(new Dimension(300, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    protected JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setFont(REGULAR_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return area;
    }
    
    protected JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(REGULAR_FONT);
        combo.setPreferredSize(new Dimension(300, 35));
        combo.setBackground(CARD_BG);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        return combo;
    }
    
    protected JSpinner createDatePicker(Date initialDate) {
        SpinnerDateModel dateModel = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(REGULAR_FONT);
        dateSpinner.setPreferredSize(new Dimension(200, 35));
        return dateSpinner;
    }
    
    // ==================== COMMON MESSAGE METHODS ====================
    
    protected void showSuccess(String message) {
        showNotification(message, SUCCESS);
    }
    
    protected void showWarning(String message) {
        showNotification(message, WARNING);
    }
    
    protected void showError(String message) {
        showNotification(message, DANGER);
    }
    
    protected void showInfo(String message) {
        showNotification(message, INFO);
    }
    
    protected void showNotification(String message, Color color) {
        String title;
        int messageType;
        
        if (color == SUCCESS) {
            title = "Success";
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else if (color == WARNING) {
            title = "Warning";
            messageType = JOptionPane.WARNING_MESSAGE;
        } else if (color == DANGER) {
            title = "Error";
            messageType = JOptionPane.ERROR_MESSAGE;
        } else {
            title = "Information";
            messageType = JOptionPane.INFORMATION_MESSAGE;
        }
        
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    protected boolean confirmAction(String message) {
        return confirmAction(message, "Confirm Action");
    }
    
    protected boolean confirmAction(String message, String title) {
        int result = JOptionPane.showConfirmDialog(this, message, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    // ==================== COMMON UTILITY METHODS ====================
    
    protected String formatDisplayDate(Date date) {
        if (date == null) return "-";
        return DISPLAY_DATE_FORMAT.format(date);
    }
    
    protected String formatDateTime(Date date) {
        if (date == null) return "-";
        return DATE_TIME_FORMAT.format(date);
    }
    
    protected String getCurrentDateTime() {
        return DATE_TIME_FORMAT.format(new Date());
    }
    
    protected String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }
    
    protected boolean isExpired(Date date) {
        if (date == null) return false;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTime(date);
        checkDate.set(Calendar.HOUR_OF_DAY, 0);
        checkDate.set(Calendar.MINUTE, 0);
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);
        
        return checkDate.before(today);
    }
    
    protected Color getStatusColor(String status) {
        if (status == null) return TEXT_GRAY;
        
        switch(status) {
            case "Active":
            case "APPROVED":
            case "Completed":
            case "Delivered":
                return SUCCESS;
            case "Pending":
            case "Scheduled":
                return WARNING;
            case "Maintenance":
            case "In Progress":
                return INFO;
            case "Failed":
            case "REJECTED":
            case "Cancelled":
                return DANGER;
            default:
                return TEXT_GRAY;
        }
    }
    
    // ==================== CELL RENDERERS ====================
    
    protected class StatusCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JLabel label = new JLabel();
        
        public StatusCellRenderer() {
            panel.setOpaque(true);
            label.setFont(STATUS_FONT);
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            panel.add(label);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            panel.setBackground(isSelected ? SELECTION_COLOR : 
                               (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
            
            if (value != null) {
                String status = value.toString();
                label.setText(status);
                label.setOpaque(true);
                
                Color color = getStatusColor(status);
                if (color == SUCCESS) {
                    label.setForeground(SUCCESS.darker());
                    label.setBackground(new Color(232, 245, 233));
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(SUCCESS, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else if (color == WARNING) {
                    label.setForeground(WARNING.darker());
                    label.setBackground(new Color(255, 243, 224));
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(WARNING, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else if (color == INFO) {
                    label.setForeground(INFO.darker());
                    label.setBackground(new Color(227, 242, 253));
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(INFO, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else if (color == DANGER) {
                    label.setForeground(DANGER.darker());
                    label.setBackground(new Color(255, 235, 238));
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(DANGER, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                } else {
                    label.setForeground(TEXT_SECONDARY);
                    label.setBackground(new Color(245, 245, 245));
                    label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(TEXT_SECONDARY, 1, true),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                    ));
                }
            }
            return panel;
        }
    }
    
    protected class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(REGULAR_FONT);
            
            if (value instanceof Date) {
                Date date = (Date) value;
                String text = DISPLAY_DATE_FORMAT.format(date);
                
                if (isExpired(date) && !isSelected) {
                    setForeground(DANGER);
                    setText(text + " (Overdue)");
                    setFont(getFont().deriveFont(Font.BOLD));
                    setBackground(new Color(255, 235, 238));
                } else if (isToday(date) && !isSelected) {
                    setForeground(WARNING);
                    setText(text + " (Today)");
                    setBackground(new Color(255, 243, 224));
                } else {
                    setForeground(TEXT_PRIMARY);
                    setBackground(isSelected ? SELECTION_COLOR : 
                                 (row % 2 == 0 ? new Color(252, 252, 253) : CARD_BG));
                }
            }
            
            setOpaque(true);
            return this;
        }
        
        private boolean isToday(Date date) {
            if (date == null) return false;
            Calendar today = Calendar.getInstance();
            Calendar checkDate = Calendar.getInstance();
            checkDate.setTime(date);
            return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
                   today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR);
        }
    }
    
    // ==================== CUSTOM BORDER CLASS ====================
    
    protected static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }
    
    protected static class ComboBoxPlaceholderRenderer extends DefaultListCellRenderer {
        private String placeholder;
        
        public ComboBoxPlaceholderRenderer(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (value == null || (index == -1 && value == null)) {
                setText(placeholder);
                setForeground(TEXT_SECONDARY);
            } else {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String) {
                    setText((String) value);
                }
            }
            return this;
        }
    }
}