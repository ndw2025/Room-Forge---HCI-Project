package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.Room;
import com.furnituredesigner.common.model.User;
import com.furnituredesigner.server.service.RoomService;
import com.furnituredesigner.server.service.UserService;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectManagementPanel extends JPanel {
    
    private User currentUser;
    private RoomService roomService;
    private UserService userService;
    private JTable projectsTable;
    private DefaultTableModel tableModel;
    private JPanel detailsPanel;
    private CardLayout detailsCardLayout;
    private Map<Integer, User> userCache;
    
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Color LIGHT_BG = new Color(248, 249, 250);
    
    public ProjectManagementPanel(User user) {
        this.currentUser = user;
        this.roomService = new RoomService();
        this.userService = new UserService();
        this.userCache = new HashMap<>();
        
        setLayout(new BorderLayout(0, 15));
        setBackground(LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        
        // Load initial data
        loadProjects();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Project Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.setIcon(FontIcon.of(FontAwesomeSolid.SYNC, 14));
        refreshButton.addActionListener(e -> loadProjects());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        
        // Create project list panel
        JPanel projectsPanel = createProjectsPanel();
        
        // Create details panel with card layout
        detailsCardLayout = new CardLayout();
        detailsPanel = new JPanel(detailsCardLayout);
        detailsPanel.setPreferredSize(new Dimension(350, 0));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new LineBorder(BORDER_COLOR));
        
        // Add placeholder for detail panel
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setBackground(Color.WHITE);
        
        JLabel placeholderLabel = new JLabel("Select a project to view details", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholderLabel.setForeground(new Color(120, 120, 120));
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);
        
        detailsPanel.add(placeholderPanel, "placeholder");
        detailsCardLayout.show(detailsPanel, "placeholder");
        
        // Add components to main panel
        panel.add(projectsPanel, BorderLayout.CENTER);
        panel.add(detailsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createProjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(BORDER_COLOR));
        
        // Create table model with non-editable cells
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Add columns
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Owner");
        tableModel.addColumn("Type");
        tableModel.addColumn("Dimensions");
        tableModel.addColumn("Actions");
        
        // Create table
        projectsTable = new JTable(tableModel);
        projectsTable.setRowHeight(40);
        projectsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectsTable.getTableHeader().setReorderingAllowed(false);
        projectsTable.setShowVerticalLines(false);
        projectsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style header
        projectsTable.getTableHeader().setBackground(new Color(245, 245, 245));
        projectsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Center align the ID column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        projectsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        // Set column widths
        projectsTable.getColumnModel().getColumn(0).setPreferredWidth(50);    // ID
        projectsTable.getColumnModel().getColumn(1).setPreferredWidth(200);   // Name
        projectsTable.getColumnModel().getColumn(2).setPreferredWidth(150);   // Owner
        projectsTable.getColumnModel().getColumn(3).setPreferredWidth(100);   // Type
        projectsTable.getColumnModel().getColumn(4).setPreferredWidth(100);   // Dimensions
        projectsTable.getColumnModel().getColumn(5).setPreferredWidth(120);   // Actions
        
        // Custom renderer for action buttons
        projectsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        
        // Handle action button clicks
        projectsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = projectsTable.rowAtPoint(e.getPoint());
                int col = projectsTable.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 5) {
                    // Get room ID from the first column
                    int roomId = (int) projectsTable.getValueAt(row, 0);
                    
                    // Calculate button positions (total width of each button including spacing is about 40px)
                    int buttonWidth = 40;
                    int x = e.getX() - projectsTable.getCellRect(row, col, false).x;
                    
                    // View button clicked (first button)
                    if (x < buttonWidth) {
                        showProjectDetails(roomId);
                    }
                    // Edit button clicked (second button)
                    else if (x < buttonWidth * 2) {
                        try {
                            Room room = roomService.getRoomById(roomId);
                            if (room != null) {
                                editProject(room);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(ProjectManagementPanel.this,
                                "Error loading project details: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    // Delete button clicked (third button)
                    else if (x < buttonWidth * 3) {
                        deleteProject(roomId);
                    }
                }
                // Row selection - show details
                else if (row >= 0) {
                    int roomId = (int) projectsTable.getValueAt(row, 0);
                    showProjectDetails(roomId);
                }
            }
        });
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(projectsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Add search panel at the top
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        
        JButton searchButton = new JButton("Search");
        searchButton.setIcon(FontIcon.of(FontAwesomeSolid.SEARCH, 14));
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        // Add components to panel
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadProjects() {
        try {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Fetch all users to build a cache for quicker lookups
            List<User> allUsers = userService.getAllUsers();
            userCache.clear();
            for (User user : allUsers) {
                userCache.put(user.getId(), user);
            }
            
            // Fetch all rooms/projects from all users for admin view
            for (User user : allUsers) {
                List<Room> rooms = roomService.getRoomsByUserId(user.getId());
                
                for (Room room : rooms) {
                    User owner = userCache.get(room.getUserId());
                    String ownerName = owner != null ? owner.getFullName() : "Unknown";
                    
                    // Format dimensions
                    String dimensions = String.format("%.1f x %.1f", room.getWidth(), room.getLength());
                    
                    // Add row to table
                    tableModel.addRow(new Object[]{
                        room.getId(),
                        room.getName(),
                        ownerName,
                        room.getShape() != null ? room.getShape() : "Rectangle",
                        dimensions,
                        "Actions" // This is a placeholder for the action buttons
                    });
                }
            }
            
            // If no projects found, show message
            if (tableModel.getRowCount() == 0) {
                tableModel.addRow(new Object[]{"No projects found", "", "", "", "", ""});
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading projects: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void showProjectDetails(int roomId) {
        try {
            Room room = roomService.getRoomById(roomId);
            
            if (room != null) {
                // Create or update details panel
                JPanel detailPanel = createDetailPanel(room);
                
                // Add to card layout with room ID as key
                String cardName = "room_" + roomId;
                
                // Check if card already exists
                boolean cardExists = false;
                for (Component comp : detailsPanel.getComponents()) {
                    if (comp.getName() != null && comp.getName().equals(cardName)) {
                        cardExists = true;
                        break;
                    }
                }
                
                if (!cardExists) {
                    detailsPanel.add(detailPanel, cardName);
                    detailPanel.setName(cardName);
                }
                
                // Show the details
                detailsCardLayout.show(detailsPanel, cardName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading project details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private JPanel createDetailPanel(Room room) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with close button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel titleLabel = new JLabel("Project Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton closeButton = new JButton(FontIcon.of(FontAwesomeSolid.TIMES, 16));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> detailsCardLayout.show(detailsPanel, "placeholder"));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        // Project preview (placeholder)
        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Draw room based on shape
                if ("circular".equalsIgnoreCase(room.getShape())) {
                    g2d.setColor(room.getColorObject());
                    int diameter = Math.min(getWidth() - 40, getHeight() - 40);
                    g2d.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
                } else {
                    // Default to rectangle
                    g2d.setColor(room.getColorObject());
                    
                    // Calculate aspect ratio
                    double aspectRatio = room.getWidth() / room.getLength();
                    int width, height;
                    
                    if (aspectRatio > 1) {
                        width = Math.min(getWidth() - 40, getHeight() - 40);
                        height = (int) (width / aspectRatio);
                    } else {
                        height = Math.min(getWidth() - 40, getHeight() - 40);
                        width = (int) (height * aspectRatio);
                    }
                    
                    g2d.fillRect((getWidth() - width) / 2, (getHeight() - height) / 2, width, height);
                }
            }
        };
        
        previewPanel.setPreferredSize(new Dimension(0, 200));
        previewPanel.setBackground(new Color(245, 245, 245));
        previewPanel.setBorder(new LineBorder(BORDER_COLOR));
        
        // Project details in a form layout
        JPanel detailsFormPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsFormPanel.setOpaque(false);
        
        addDetailField(detailsFormPanel, "ID:", String.valueOf(room.getId()));
        addDetailField(detailsFormPanel, "Name:", room.getName());
        addDetailField(detailsFormPanel, "Shape:", room.getShape() != null ? room.getShape() : "Rectangle");
        addDetailField(detailsFormPanel, "Width:", String.format("%.2f m", room.getWidth()));
        addDetailField(detailsFormPanel, "Length:", String.format("%.2f m", room.getLength()));
        addDetailField(detailsFormPanel, "Height:", String.format("%.2f m", room.getHeight()));
        
        // Get owner name
        User owner = userCache.get(room.getUserId());
        addDetailField(detailsFormPanel, "Owner:", owner != null ? owner.getFullName() : "Unknown");
        
        // Description with scroll if needed
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setOpaque(false);
        
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JTextArea descArea = new JTextArea(room.getDescription() != null ? room.getDescription() : "No description available");
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setBackground(new Color(245, 245, 245));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);
        descScroll.setPreferredSize(new Dimension(0, 100));
        
        descriptionPanel.add(descLabel, BorderLayout.NORTH);
        descriptionPanel.add(descScroll, BorderLayout.CENTER);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        JButton editButton = new JButton("Edit Project");
        editButton.setIcon(FontIcon.of(FontAwesomeSolid.EDIT, 14));
        editButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editButton.addActionListener(e -> editProject(room));
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.setIcon(FontIcon.of(FontAwesomeSolid.TRASH_ALT, 14));
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deleteButton.setForeground(new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deleteProject(room.getId()));
        
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        // Add all components
        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(previewPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(detailsFormPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(descriptionPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(actionPanel);
        
        return panel;
    }
    
    private void addDetailField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
    
    private void editProject(Room room) {
        // Create a dialog for editing
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Project", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create form fields
        JTextField nameField = new JTextField(room.getName());
        JComboBox<String> shapeCombo = new JComboBox<>(new String[]{"rectangle", "square", "circular"});
        shapeCombo.setSelectedItem(room.getShape() != null ? room.getShape() : "rectangle");
        
        JTextField widthField = new JTextField(String.valueOf(room.getWidth()));
        JTextField lengthField = new JTextField(String.valueOf(room.getLength()));
        JTextField heightField = new JTextField(String.valueOf(room.getHeight()));
        
        JTextArea descArea = new JTextArea(room.getDescription());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 100));
        
        // Add fields to form
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Shape:"));
        formPanel.add(shapeCombo);
        
        formPanel.add(new JLabel("Width (m):"));
        formPanel.add(widthField);
        
        formPanel.add(new JLabel("Length (m):"));
        formPanel.add(lengthField);
        
        formPanel.add(new JLabel("Height (m):"));
        formPanel.add(heightField);
        
        // Description panel
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        descPanel.add(descScroll, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                // Update room object
                room.setName(nameField.getText());
                room.setShape((String) shapeCombo.getSelectedItem());
                room.setWidth(Double.parseDouble(widthField.getText()));
                room.setLength(Double.parseDouble(lengthField.getText()));
                room.setHeight(Double.parseDouble(heightField.getText()));
                room.setDescription(descArea.getText());
                
                // Save to database
                boolean success = roomService.updateRoom(room);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Project updated successfully", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                    // Refresh data
                    loadProjects();
                    showProjectDetails(room.getId());
                
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Failed to update project", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numbers for dimensions", 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Database error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(descPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void deleteProject(int roomId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this project? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = roomService.deleteRoom(roomId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Project deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                
                    // Refresh the projects list
                    loadProjects();
                
                    // Reset detail panel to placeholder
                    detailsCardLayout.show(detailsPanel, "placeholder");
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to delete project",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting project: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    // Custom renderer for action buttons
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton viewButton;
        private JButton editButton;
        private JButton deleteButton;
        
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
            setOpaque(true);
            
            // Create buttons with icons instead of text to save space
            viewButton = new JButton();
            viewButton.setIcon(FontIcon.of(FontAwesomeSolid.EYE, 14, new Color(41, 128, 185)));
            viewButton.setToolTipText("View Details");
            viewButton.setPreferredSize(new Dimension(32, 28));
            viewButton.setMargin(new Insets(2, 2, 2, 2));
            
            editButton = new JButton();
            editButton.setIcon(FontIcon.of(FontAwesomeSolid.EDIT, 14, new Color(39, 174, 96)));
            editButton.setToolTipText("Edit Project");
            editButton.setPreferredSize(new Dimension(32, 28));
            editButton.setMargin(new Insets(2, 2, 2, 2));
            
            deleteButton = new JButton();
            deleteButton.setIcon(FontIcon.of(FontAwesomeSolid.TRASH_ALT, 14, new Color(220, 53, 69)));
            deleteButton.setToolTipText("Delete Project");
            deleteButton.setPreferredSize(new Dimension(32, 28));
            deleteButton.setMargin(new Insets(2, 2, 2, 2));
            
            add(viewButton);
            add(editButton);
            add(deleteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            
            return this;
        }
    }
}