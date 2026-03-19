package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.User;
import com.furnituredesigner.common.model.Room;
import com.furnituredesigner.server.service.RoomService;
import java.text.SimpleDateFormat;
import com.furnituredesigner.common.model.Template;
import com.furnituredesigner.server.service.TemplateService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import com.furnituredesigner.client.ui.WrapLayout;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class DesignerDashboardPanel extends JPanel {
    
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Constructor
    public DesignerDashboardPanel(User user) {
        this.currentUser = user;
        
        setLayout(new BorderLayout());
        
        // Create card layout for the content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Create and configure the navigation panel
        NavigationPanel navPanel = new NavigationPanel(contentPanel, cardLayout);
        
        // Add menu items with simple text icons instead of emoji
        // navPanel.addMenuItem("Projects", "projects", "P");
        navPanel.addMenuItem("New Design", "new-design", "+");
        navPanel.addMenuItem("2D View", "2d-view", "2D");
        navPanel.addMenuItem("3D View", "3d-preview", "3D");  // Add 3D View navigation item
        navPanel.addMenuItem("Materials", "materials", "M");
        navPanel.addMenuItem("Templates", "templates", "T");
        navPanel.addMenuItem("Settings", "settings", "S");
        
        // Create content panels
        JPanel projectsContent = createProjectsContent();
        JPanel newDesignContent = createNewDesignContent();
        JPanel viewContent = create2DViewContent();
        // We'll create the 3D view panel dynamically when needed
        JPanel materialsContent = createMaterialsContent();
        JPanel templatesContent = createTemplatesContent();
        JPanel settingsContent = createSettingsContent();
        
        // Add content panels to the card layout
        contentPanel.add(projectsContent, "projects");
        contentPanel.add(newDesignContent, "new-design");
        contentPanel.add(viewContent, "2d-view");
        // 3D view will be added when a room is opened
        contentPanel.add(materialsContent, "materials");
        contentPanel.add(templatesContent, "templates");
        contentPanel.add(settingsContent, "settings");
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Add components to the main panel
        add(navPanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Show the projects by default
        cardLayout.show(contentPanel, "projects");
    }

    /**
 * Public method to refresh the templates panel
 * This can be called from other components when templates are created or changed
 */
/**
 * Public method to refresh the templates panel
 * This can be called from other components when templates are created or changed
 */
public void refreshTemplatesPanel() {
    // First get access to our templates panel components
    Component templatesComponent = null;
    for (Component comp : contentPanel.getComponents()) {
        if ("templates".equals(comp.getName())) {
            templatesComponent = comp;
            break;
        }
    }
    
    if (templatesComponent == null) {
        System.out.println("Templates panel not found in content panel");
        return;
    }
    
    // Try to find the grid panel and list panel inside the templates component
    JPanel gridPanel = null;
    JPanel listPanel = null;
    
    if (templatesComponent instanceof JPanel) {
        JPanel templatesPanel = (JPanel) templatesComponent;
        // Look for view panels that might contain our grid and list panels
        for (Component comp : templatesPanel.getComponents()) {
            if (comp instanceof JPanel && "viewsPanel".equals(comp.getName())) {
                JPanel viewsPanel = (JPanel) comp;
                
                // Check components in the views panel
                for (Component viewComp : viewsPanel.getComponents()) {
                    if (viewComp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) viewComp;
                        Component viewComponent = scrollPane.getViewport().getView();
                        
                        // Check if this is the grid or list panel
                        if (viewComponent instanceof JPanel) {
                            JPanel panel = (JPanel) viewComponent;  // Cast to JPanel first
                            if (panel.getLayout() instanceof WrapLayout) {
                                gridPanel = panel;
                            } else if (panel.getLayout() instanceof BoxLayout) {
                                listPanel = panel;
                            }
                        }
                    }
                }
            }
        }
    }
    
    // If we found grid or list panels, refresh their content
    if (gridPanel != null) {
        loadUserTemplates(gridPanel, "All", true, "", "Newest First");
        System.out.println("Refreshed grid panel for templates");
    }
    
    if (listPanel != null) {
        loadUserTemplates(listPanel, "All", false, "", "Newest First");
        System.out.println("Refreshed list panel for templates");
    }
    
    // If we couldn't find the existing panels through the hierarchy,
    // recreate the templates panel entirely
    if (gridPanel == null && listPanel == null) {
        System.out.println("Could not find template grid/list panels, recreating templates panel");
        JPanel newTemplatesPanel = createTemplatesContent();
        newTemplatesPanel.setName("templates");
        
        // Remove old templates panel and add the new one
        contentPanel.remove(templatesComponent);
        contentPanel.add(newTemplatesPanel, "templates");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}


/**
 * Public method to switch to the templates panel
 */
public void showTemplatesPanel() {
    cardLayout.show(contentPanel, "templates");
}

    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        JLabel titleLabel = new JLabel("Designer Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel(currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            // Call the logout method from the root frame
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame) {
                ((MainFrame) window).logout();
            }
        });
        
        rightPanel.add(userLabel);
        rightPanel.add(logoutButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createProjectsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("My Projects");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel projectsGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        projectsGrid.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        projectsGrid.setOpaque(false);
        
        // Add project cards
        projectsGrid.add(createProjectCard("Modern Living Room", "Last edited: 3 days ago"));
        projectsGrid.add(createProjectCard("Kitchen Redesign", "Last edited: 1 week ago"));
        projectsGrid.add(createProjectCard("Office Space", "Last edited: 2 weeks ago"));
        projectsGrid.add(createProjectCard("Master Bedroom", "Last edited: 1 month ago"));
        projectsGrid.add(createNewProjectCard());
        
        JScrollPane scrollPane = new JScrollPane(projectsGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProjectCard(String title, String lastEdited) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        panel.setBackground(Color.WHITE);
        
        // Project thumbnail (placeholder)
        JPanel thumbnailPanel = new JPanel();
        thumbnailPanel.setPreferredSize(new Dimension(0, 150));
        thumbnailPanel.setBackground(new Color(240, 240, 240));
        thumbnailPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        JLabel thumbnailLabel = new JLabel("Project Preview");
        thumbnailLabel.setHorizontalAlignment(JLabel.CENTER);
        thumbnailPanel.add(thumbnailLabel);
        
        // Project info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel dateLabel = new JLabel(lastEdited);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(120, 120, 120));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(dateLabel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        
        JButton editButton = new JButton("Open");
        JButton deleteButton = new JButton("Delete");
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        panel.add(thumbnailPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createNewProjectCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        panel.setBackground(new Color(248, 248, 248));
        
        JButton newButton = new JButton("+ Create New Project");
        newButton.setFont(new Font("Arial", Font.BOLD, 14));
        newButton.setFocusPainted(false);
        newButton.setBackground(new Color(240, 240, 240));
        
        panel.add(newButton, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 220)); // Match height of project cards
        
        return panel;
    }
    
    private JPanel createNewDesignContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Create New Design");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Project Name field
        formPanel.add(new JLabel("Project Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Room Type field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Room Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<String> roomTypeCombo = new JComboBox<>(new String[] {
            "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Dining Room", "Other"
        });
        formPanel.add(roomTypeCombo, gbc);
        
        // Room Shape field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Room Shape:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> shapeCombo = new JComboBox<>(new String[] {
            "Rectangle", "Square", "Circular"
        });
        formPanel.add(shapeCombo, gbc);
        
        // Room Dimensions panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Room Dimensions:"), gbc);
        
        gbc.gridx = 1;
        JPanel dimensionsPanel = new JPanel(new CardLayout());
        dimensionsPanel.setOpaque(false);
        
        // Rectangle dimensions panel
        JPanel rectDimensionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rectDimensionsPanel.setOpaque(false);
        JTextField rectWidthField = new JTextField(5);
        JTextField rectLengthField = new JTextField(5);
        JTextField rectHeightField = new JTextField(5);
        rectDimensionsPanel.add(new JLabel("Width (m):"));
        rectDimensionsPanel.add(rectWidthField);
        rectDimensionsPanel.add(new JLabel("Length (m):"));
        rectDimensionsPanel.add(rectLengthField);
        rectDimensionsPanel.add(new JLabel("Height (m):"));
        rectDimensionsPanel.add(rectHeightField);
        
        // Square dimensions panel
        JPanel squareDimensionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        squareDimensionsPanel.setOpaque(false);
        JTextField squareSideField = new JTextField(5);
        JTextField squareHeightField = new JTextField(5);
        squareDimensionsPanel.add(new JLabel("Side Length (m):"));
        squareDimensionsPanel.add(squareSideField);
        squareDimensionsPanel.add(new JLabel("Height (m):"));
        squareDimensionsPanel.add(squareHeightField);
        
        // Circular dimensions panel
        JPanel circularDimensionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        circularDimensionsPanel.setOpaque(false);
        JTextField diameterField = new JTextField(5);
        JTextField circularHeightField = new JTextField(5);
        circularDimensionsPanel.add(new JLabel("Diameter (m):"));
        circularDimensionsPanel.add(diameterField);
        circularDimensionsPanel.add(new JLabel("Height (m):"));
        circularDimensionsPanel.add(circularHeightField);
        
        // Add all dimension panels to card layout
        dimensionsPanel.add(rectDimensionsPanel, "Rectangle");
        dimensionsPanel.add(squareDimensionsPanel, "Square");
        dimensionsPanel.add(circularDimensionsPanel, "Circular");
        
        // Add listener to shape combo box to switch dimension panels
        shapeCombo.addActionListener(e -> {
            String selectedShape = (String) shapeCombo.getSelectedItem();
            CardLayout cl = (CardLayout) dimensionsPanel.getLayout();
            cl.show(dimensionsPanel, selectedShape);
        });
        
        formPanel.add(dimensionsPanel, gbc);
        
        // Room Color field
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Room Color:"), gbc);
        
        gbc.gridx = 1;
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.setOpaque(false);
        
        JPanel colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(30, 30));
        colorPreview.setBackground(Color.WHITE);
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JButton colorButton = new JButton("Choose Color");
        colorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(panel, "Choose Room Color", colorPreview.getBackground());
            if (selectedColor != null) {
                colorPreview.setBackground(selectedColor);
            }
        });
        
        colorPanel.add(colorPreview);
        colorPanel.add(colorButton);
        formPanel.add(colorPanel, gbc);
        
        // Description field
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        JTextArea descArea = new JTextArea(4, 20);
        JScrollPane scrollPane = new JScrollPane(descArea);
        formPanel.add(scrollPane, gbc);
        
        // Create Design button
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        JButton createButton = new JButton("Create Design");
        createButton.addActionListener(e -> {
            try {
                // Get form values
                String projectName = nameField.getText().trim();
                String roomType = (String) roomTypeCombo.getSelectedItem();
                String shape = (String) shapeCombo.getSelectedItem();
                String description = descArea.getText().trim();
                
                // Validate project name
                if (projectName.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please enter a project name", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get dimensions based on selected shape
                double width = 0, length = 0, height = 0;
                
                try {
                    if ("Rectangle".equals(shape)) {
                        width = Double.parseDouble(rectWidthField.getText().trim());
                        length = Double.parseDouble(rectLengthField.getText().trim());
                        height = Double.parseDouble(rectHeightField.getText().trim());
                    } else if ("Square".equals(shape)) {
                        width = Double.parseDouble(squareSideField.getText().trim());
                        length = width; // For a square, width = length
                        height = Double.parseDouble(squareHeightField.getText().trim());
                    } else if ("Circular".equals(shape)) {
                        width = Double.parseDouble(diameterField.getText().trim());
                        length = width; // For a circle, diameter is used for both width and length
                        height = Double.parseDouble(circularHeightField.getText().trim());
                    }
                    
                    // Validate dimensions
                    if (width <= 0 || length <= 0 || height <= 0) {
                        JOptionPane.showMessageDialog(panel, "Dimensions must be positive numbers", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Please enter valid numbers for dimensions", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get color as hex string
                Color roomColor = colorPreview.getBackground();
                String colorHex = String.format("#%02x%02x%02x", 
                    roomColor.getRed(), roomColor.getGreen(), roomColor.getBlue());
                
                // Create room object
                Room room = new Room();
                room.setName(projectName);
                room.setWidth(width);
                room.setLength(length);
                room.setHeight(height);
                room.setColor(colorHex);
                room.setShape(shape);
                room.setUserId(currentUser.getId());
                room.setDescription(description);
                
                // Save room to database
                RoomService roomService = new RoomService();
                Room savedRoom = roomService.createRoom(room);
                
                if (savedRoom != null) {
                    JOptionPane.showMessageDialog(panel, "Room design created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Create and add the 2D view panel if it doesn't exist
                    TwoDViewPanel twoDViewPanel = new TwoDViewPanel(savedRoom);
                    contentPanel.add(twoDViewPanel, "2d-view");
                    
                    // Switch to 2D view
                    cardLayout.show(contentPanel, "2d-view");
                    
                    // Clear form fields
                    nameField.setText("");
                    roomTypeCombo.setSelectedIndex(0);
                    shapeCombo.setSelectedIndex(0);
                    rectWidthField.setText("");
                    rectLengthField.setText("");
                    rectHeightField.setText("");
                    squareSideField.setText("");
                    squareHeightField.setText("");
                    diameterField.setText("");
                    circularHeightField.setText("");
                    colorPreview.setBackground(Color.WHITE);
                    descArea.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Error creating room design", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        formPanel.add(createButton, gbc);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel create2DViewContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("2D Room Design View");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Placeholder panel - actual content will be created when a design is selected
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setBackground(new Color(245, 245, 245));
        
        JLabel placeholderText = new JLabel("No room selected. Create a new design or select a project to view.");
        placeholderText.setHorizontalAlignment(JLabel.CENTER);
        placeholderText.setFont(new Font("Arial", Font.PLAIN, 16));
        placeholderPanel.add(placeholderText, BorderLayout.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(placeholderPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMaterialsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Materials Library");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel materialsGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        materialsGrid.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        materialsGrid.setOpaque(false);
        
        // Add material swatches
        String[] materialNames = {
            "Wood - Oak", "Wood - Walnut", "Wood - Pine", 
            "Fabric - Cotton", "Fabric - Leather", "Fabric - Linen",
            "Metal - Steel", "Metal - Aluminum", "Metal - Brass",
            "Glass - Clear", "Glass - Frosted", "Glass - Tinted",
            "Stone - Marble", "Stone - Granite", "Stone - Concrete"
        };
        
        Color[] materialColors = {
            new Color(194, 150, 90), new Color(113, 76, 50), new Color(222, 184, 135),
            new Color(230, 230, 230), new Color(150, 111, 51), new Color(230, 225, 207),
            new Color(192, 192, 192), new Color(211, 211, 211), new Color(181, 166, 66),
            new Color(200, 200, 255, 150), new Color(220, 220, 255, 180), new Color(130, 180, 255, 150),
            new Color(225, 225, 225), new Color(190, 190, 190), new Color(180, 180, 180)
        };
        
        for (int i = 0; i < materialNames.length; i++) {
            materialsGrid.add(createMaterialSwatch(materialNames[i], materialColors[i]));
        }
        
        JScrollPane scrollPane = new JScrollPane(materialsGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMaterialSwatch(String name, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.setBackground(Color.WHITE);
        
        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(color);
        colorPanel.setPreferredSize(new Dimension(0, 100));
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(colorPanel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTemplatesContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setName("templates");
        
        // Title panel at the top
        JLabel titleLabel = new JLabel("Design Templates");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create filter panel with more options
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        filterPanel.setOpaque(false);
        
        // Top row: Search and filters
        JPanel topFilterRow = new JPanel(new BorderLayout());
        topFilterRow.setOpaque(false);
        
        // Left side: Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search by template name");
        
        JButton searchButton = new JButton("Search");
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Right side: View controls
        JPanel viewControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewControlPanel.setOpaque(false);
        
        JToggleButton gridViewButton = new JToggleButton("Grid View");
        JToggleButton listViewButton = new JToggleButton("List View");
        
        ButtonGroup viewButtonGroup = new ButtonGroup();
        viewButtonGroup.add(gridViewButton);
        viewButtonGroup.add(listViewButton);
        gridViewButton.setSelected(true);
        
        viewControlPanel.add(gridViewButton);
        viewControlPanel.add(listViewButton);
        
        topFilterRow.add(searchPanel, BorderLayout.WEST);
        topFilterRow.add(viewControlPanel, BorderLayout.EAST);
        
        // Bottom row: Category filters and sorting
        JPanel bottomFilterRow = new JPanel(new BorderLayout());
        bottomFilterRow.setOpaque(false);
        
        // Left side: Room type filter
        JPanel typeFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeFilterPanel.setOpaque(false);
        
        typeFilterPanel.add(new JLabel("Room Type:"));
        JComboBox<String> roomTypeFilter = new JComboBox<>(new String[] {
            "All", "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Dining Room", "Other"
        });
        typeFilterPanel.add(roomTypeFilter);
        
        // Right side: Sort options
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sortPanel.setOpaque(false);
        
        sortPanel.add(new JLabel("Sort By:"));
        JComboBox<String> sortOptions = new JComboBox<>(new String[] {
            "Newest First", "Oldest First", "A-Z", "Z-A"
        });
        sortPanel.add(sortOptions);
        
        JButton refreshButton = new JButton("Refresh");
        sortPanel.add(refreshButton);
        
        bottomFilterRow.add(typeFilterPanel, BorderLayout.WEST);
        bottomFilterRow.add(sortPanel, BorderLayout.EAST);
        
        // Add filter rows to the filter panel
        filterPanel.add(topFilterRow, BorderLayout.NORTH);
        filterPanel.add(bottomFilterRow, BorderLayout.SOUTH);
        
        // Content panel with card layout for different views
        JPanel viewsPanel = new JPanel(new CardLayout());
        viewsPanel.setName("viewsPanel");
        
        // Create grid view panel with WrapLayout
        JPanel gridPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 15, 15));
        gridPanel.setOpaque(false);
        gridPanel.setName("gridPanel");
        
        // Create list view panel with BoxLayout
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setName("listPanel");
        
        // Create scroll panes for both views
        JScrollPane gridScrollPane = new JScrollPane(gridPanel);
        gridScrollPane.setBorder(null);
        gridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gridScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JScrollPane listScrollPane = new JScrollPane(listPanel);
        listScrollPane.setBorder(null);
        listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Add scroll panes to the card layout panel
        viewsPanel.add(gridScrollPane, "grid");
        viewsPanel.add(listScrollPane, "list");
        CardLayout viewsCardLayout = (CardLayout) viewsPanel.getLayout();
        
        // Load templates for the default view (grid)
        loadUserTemplates(gridPanel, "All", true, null, "Newest First");
        
        // Add logic for view switching
        gridViewButton.addActionListener(e -> {
            viewsCardLayout.show(viewsPanel, "grid");
            // Only reload if not already loaded
            if (gridPanel.getComponentCount() == 0) {
                loadUserTemplates(gridPanel, 
                    (String) roomTypeFilter.getSelectedItem(), 
                    true, 
                    searchField.getText(), 
                    (String) sortOptions.getSelectedItem());
            }
        });
        
        listViewButton.addActionListener(e -> {
            viewsCardLayout.show(viewsPanel, "list");
            // Only reload if not already loaded
            if (listPanel.getComponentCount() == 0) {
                loadUserTemplates(listPanel, 
                    (String) roomTypeFilter.getSelectedItem(), 
                    false, 
                    searchField.getText(), 
                    (String) sortOptions.getSelectedItem());
            }
        });
        
        // Add refresh button logic
        refreshButton.addActionListener(e -> {
            String filterType = (String) roomTypeFilter.getSelectedItem();
            String searchQuery = searchField.getText().trim();
            String sortOrder = (String) sortOptions.getSelectedItem();
            JPanel activePanel = gridViewButton.isSelected() ? gridPanel : listPanel;
            boolean isGridView = gridViewButton.isSelected();
            
            // Show loading indicator
            activePanel.removeAll();
            JLabel loadingLabel = new JLabel("Loading templates...");
            loadingLabel.setHorizontalAlignment(JLabel.CENTER);
            activePanel.add(loadingLabel);
            activePanel.revalidate();
            activePanel.repaint();
            
            // Use SwingWorker to reload in background
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        Thread.sleep(200); // Small delay for UI update
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    loadUserTemplates(activePanel, filterType, isGridView, searchQuery, sortOrder);
                    activePanel.revalidate();
                    activePanel.repaint();
                }
            };
            worker.execute();
        });
        
        // Add search button logic
        searchButton.addActionListener(e -> {
            String filterType = (String) roomTypeFilter.getSelectedItem();
            String searchQuery = searchField.getText().trim();
            String sortOrder = (String) sortOptions.getSelectedItem();
            JPanel activePanel = gridViewButton.isSelected() ? gridPanel : listPanel;
            boolean isGridView = gridViewButton.isSelected();
            
            activePanel.removeAll();
            loadUserTemplates(activePanel, filterType, isGridView, searchQuery, sortOrder);
            activePanel.revalidate();
            activePanel.repaint();
        });
        
        // Enter key in search field triggers search
        searchField.addActionListener(e -> searchButton.doClick());
        
        // Add room type filter logic
        roomTypeFilter.addActionListener(e -> {
            String filterType = (String) roomTypeFilter.getSelectedItem();
            String searchQuery = searchField.getText().trim();
            String sortOrder = (String) sortOptions.getSelectedItem();
            JPanel activePanel = gridViewButton.isSelected() ? gridPanel : listPanel;
            boolean isGridView = gridViewButton.isSelected();
            
            activePanel.removeAll();
            loadUserTemplates(activePanel, filterType, isGridView, searchQuery, sortOrder);
            activePanel.revalidate();
            activePanel.repaint();
        });
        
        // Add sort options logic
        sortOptions.addActionListener(e -> {
            String filterType = (String) roomTypeFilter.getSelectedItem();
            String searchQuery = searchField.getText().trim();
            String sortOrder = (String) sortOptions.getSelectedItem();
            JPanel activePanel = gridViewButton.isSelected() ? gridPanel : listPanel;
            boolean isGridView = gridViewButton.isSelected();
            
            activePanel.removeAll();
            loadUserTemplates(activePanel, filterType, isGridView, searchQuery, sortOrder);
            activePanel.revalidate();
            activePanel.repaint();
        });
        
        // Create a main content panel to hold both the filter panel and views panel
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(filterPanel, BorderLayout.NORTH);
        mainContentPanel.add(viewsPanel, BorderLayout.CENTER);
        
        // Add title to NORTH and main content to CENTER
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(mainContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    
private void loadUserTemplates(JPanel targetPanel, String filterType, boolean isGridView, 
                              String searchQuery, String sortOrder) {
    targetPanel.removeAll();
    
    if (isGridView) {
        // For grid view, ensure WrapLayout is used
        if (!(targetPanel.getLayout() instanceof WrapLayout)) {
            targetPanel.setLayout(new WrapLayout(WrapLayout.LEFT, 15, 15));
        }
    } else {
        // For list view, ensure BoxLayout is used
        if (!(targetPanel.getLayout() instanceof BoxLayout)) {
            targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.Y_AXIS));
        }
    }
    
    try {
        TemplateService templateService = new TemplateService();
        List<Template> templates = templateService.getTemplatesByUserId(currentUser.getId());
        
        // Apply filters and sort
        List<Template> filteredTemplates = new ArrayList<>();
        
        for (Template template : templates) {
            // Apply room type filter if not "All"
            if (!"All".equals(filterType) && !template.getRoomType().equals(filterType)) {
                continue;
            }
            
            // Apply search filter if provided
            if (searchQuery != null && !searchQuery.isEmpty()) {
                if (!template.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                    continue;
                }
            }
            
            filteredTemplates.add(template);
        }
        
        // Apply sorting
        if (sortOrder != null) {
            switch (sortOrder) {
                case "Newest First":
                    filteredTemplates.sort((t1, t2) -> 
                        t2.getCreatedAt().compareTo(t1.getCreatedAt()));
                    break;
                case "Oldest First":
                    filteredTemplates.sort((t1, t2) -> 
                        t1.getCreatedAt().compareTo(t2.getCreatedAt()));
                    break;
                case "A-Z":
                    filteredTemplates.sort((t1, t2) -> 
                        t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                    break;
                case "Z-A":
                    filteredTemplates.sort((t1, t2) -> 
                        t2.getTitle().compareToIgnoreCase(t1.getTitle()));
                    break;
            }
        }
        
        boolean hasTemplates = false;
        
        for (Template template : filteredTemplates) {
            if (isGridView) {
                targetPanel.add(createTemplateCard(template));
            } else {
                targetPanel.add(createTemplateListItem(template));
                // Add separator between list items except for the last one
                if (filteredTemplates.indexOf(template) < filteredTemplates.size() - 1) {
                    JSeparator separator = new JSeparator();
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    targetPanel.add(separator);
                }
            }
            hasTemplates = true;
        }
        
        if (!hasTemplates) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(new Color(245, 245, 245));
            
            JLabel emptyIcon = new JLabel("📁"); // Folder emoji as a simple icon
            emptyIcon.setFont(new Font("Dialog", Font.PLAIN, 48));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            String messageText = "No templates found";
            if (searchQuery != null && !searchQuery.isEmpty()) {
                messageText = "No templates match your search";
            }
            
            JLabel emptyLabel = new JLabel(messageText);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel instructionLabel = new JLabel("Try different search terms or create a new template");
            instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JButton newDesignButton = new JButton("Create New Design");
            newDesignButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            newDesignButton.addActionListener(e -> cardLayout.show(contentPanel, "new-design"));
            
            JButton clearFiltersButton = new JButton("Clear Filters");
            clearFiltersButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            clearFiltersButton.addActionListener(e -> {
                // This will be handled by the parent's refresh mechanism
                JPanel parentPanel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, targetPanel);
                if (parentPanel != null) {
                    for (Component comp : parentPanel.getComponents()) {
                        if (comp instanceof JComboBox) {
                            ((JComboBox<?>) comp).setSelectedIndex(0);
                        } else if (comp instanceof JTextField) {
                            ((JTextField) comp).setText("");
                        }
                    }
                }
                
                loadUserTemplates(targetPanel, "All", isGridView, "", "Newest First");
                targetPanel.revalidate();
                targetPanel.repaint();
            });
            
            emptyPanel.add(Box.createVerticalGlue());
            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(10));
            emptyPanel.add(emptyLabel);
            emptyPanel.add(Box.createVerticalStrut(5));
            emptyPanel.add(instructionLabel);
            emptyPanel.add(Box.createVerticalStrut(20));
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel.add(newDesignButton);
            
            if (searchQuery != null && !searchQuery.isEmpty() || !"All".equals(filterType)) {
                buttonPanel.add(clearFiltersButton);
            }
            
            emptyPanel.add(buttonPanel);
            emptyPanel.add(Box.createVerticalGlue());
            
            targetPanel.add(emptyPanel);
        }
        
        // Add filter summary if filters are applied
        boolean hasFilters = (searchQuery != null && !searchQuery.isEmpty()) || !"All".equals(filterType);
        if (hasFilters && hasTemplates) {
            JPanel filterSummaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filterSummaryPanel.setOpaque(false);
            filterSummaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            StringBuilder summaryText = new StringBuilder("Showing templates for: ");
            if (!"All".equals(filterType)) {
                summaryText.append(filterType);
            } else {
                summaryText.append("All room types");
            }
            
            if (searchQuery != null && !searchQuery.isEmpty()) {
                summaryText.append(" containing \"").append(searchQuery).append("\"");
            }
            
            JLabel summaryLabel = new JLabel(summaryText.toString());
            summaryLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            summaryLabel.setForeground(new Color(100, 100, 100));
            
            JButton clearButton = new JButton("Clear Filters");
            clearButton.setFont(new Font("Arial", Font.PLAIN, 10));
            clearButton.setMargin(new Insets(0, 5, 0, 5));
            clearButton.addActionListener(e -> {
                // This will be handled by the parent's refresh mechanism
                JPanel parentPanel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, targetPanel);
                if (parentPanel != null) {
                    Component[] components = ((Container) parentPanel.getParent()).getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JComboBox) {
                            ((JComboBox<?>) comp).setSelectedIndex(0);
                        } else if (comp instanceof JTextField) {
                            ((JTextField) comp).setText("");
                        }
                    }
                }
                
                loadUserTemplates(targetPanel, "All", isGridView, "", "Newest First");
                targetPanel.revalidate();
                targetPanel.repaint();
            });
            
            filterSummaryPanel.add(summaryLabel);
            filterSummaryPanel.add(clearButton);
            
            // Add at the beginning
            if (targetPanel.getComponentCount() > 0) {
                targetPanel.add(filterSummaryPanel, 0);
            } else {
                targetPanel.add(filterSummaryPanel);
            }
        }
        
        // Debug info
        System.out.println("Loaded " + filteredTemplates.size() + " templates of " + 
            templates.size() + " total for user " + currentUser.getId());
        
    } catch (SQLException e) {
        e.printStackTrace();
        
        // Show error message in panel
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(new Color(255, 235, 235));
        JLabel errorLabel = new JLabel("Error loading templates: " + e.getMessage());
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        targetPanel.add(errorPanel);
    }
    
    // Add extra space at bottom for better scrolling
    targetPanel.add(Box.createVerticalStrut(20));
    
    // Ensure layout updates
    targetPanel.revalidate();
    targetPanel.repaint();
    
    // Update JScrollPane if needed
    Container parent = targetPanel.getParent();
    if (parent instanceof JViewport) {
        parent = parent.getParent();
        if (parent instanceof JScrollPane) {
            ((JScrollPane) parent).revalidate();
            ((JScrollPane) parent).repaint();
        }
    }
}
    
    // New method to create list view items
    private JPanel createTemplateListItem(Template template) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(8, 0, 8, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            )
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        try {
            RoomService roomService = new RoomService();
            Room room = roomService.getRoomById(template.getRoomId());
            
            // Left panel with template info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            
            JLabel titleLabel = new JLabel(template.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            detailsPanel.setOpaque(false);
            
            JLabel typeLabel = new JLabel("Type: " + template.getRoomType());
            typeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            // Format date to a readable format
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            String dateString = sdf.format(template.getCreatedAt());
            JLabel dateLabel = new JLabel("Created: " + dateString);
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            detailsPanel.add(typeLabel);
            detailsPanel.add(new JSeparator(JSeparator.VERTICAL));
            detailsPanel.add(dateLabel);
            
            infoPanel.add(titleLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(detailsPanel);
            
            if (template.getComments() != null && !template.getComments().isEmpty()) {
                JLabel commentsLabel = new JLabel(template.getComments());
                commentsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                commentsLabel.setForeground(Color.GRAY);
                commentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoPanel.add(Box.createVerticalStrut(5));
                infoPanel.add(commentsLabel);
            }
            
            // Right panel with action buttons
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actionPanel.setOpaque(false);
            
            JButton viewButton = new JButton("View");
            JButton editButton = new JButton("Edit");
            JButton deleteButton = new JButton("Delete");
            
            // Make buttons more compact for list view
            viewButton.setMargin(new Insets(2, 8, 2, 8));
            editButton.setMargin(new Insets(2, 8, 2, 8));
            deleteButton.setMargin(new Insets(2, 8, 2, 8));
            
            // Add actions to buttons (same as in card view)
            // View button action - load the template in 2D view
            viewButton.addActionListener(e -> {
                try {
                    // Get room from the template
                    RoomService roomService2 = new RoomService();
                    Room room2 = roomService2.getRoomById(template.getRoomId());
                    
                    if (room2 != null) {
                        // Create 2D view panel with the room
                        TwoDViewPanel twoDViewPanel = new TwoDViewPanel(room2);
                        
                        // Add to content panel and show it
                        contentPanel.add(twoDViewPanel, "2d-view");
                        cardLayout.show(contentPanel, "2d-view");
                    } else {
                        JOptionPane.showMessageDialog(panel, 
                            "Room not found for this template", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, 
                        "Error loading template: " + ex.getMessage(), 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
            
            // Edit button action
            editButton.addActionListener(e -> {
                showTemplateEditDialog(template, panel);
            });
            
            // Delete button action
            deleteButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    "Are you sure you want to delete this template?", 
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        TemplateService templateService = new TemplateService();
                        boolean deleted = templateService.deleteTemplate(template.getId());
                        
                        if (deleted) {
                            JOptionPane.showMessageDialog(panel, 
                                "Template deleted successfully", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // CHANGE: Instead of switching panels, directly refresh the current panel
                            refreshTemplatesPanel();
                        } else {
                            JOptionPane.showMessageDialog(panel, 
                                "Failed to delete template", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(panel, 
                            "Database error: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });
            
            actionPanel.add(viewButton);
            actionPanel.add(editButton);
            actionPanel.add(deleteButton);
            
            // Add a color swatch representing the room color
            JPanel colorPanel = new JPanel();
            colorPanel.setPreferredSize(new Dimension(24, 24));
            if (room != null) {
                colorPanel.setBackground(room.getColorObject());
                colorPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            } else {
                colorPanel.setBackground(Color.LIGHT_GRAY);
            }
            
            // Use a small square panel on the left to show room color
            JPanel leftDecoration = new JPanel(new FlowLayout(FlowLayout.LEFT));
            leftDecoration.setOpaque(false);
            leftDecoration.add(colorPanel);
            
            // Add everything to the main panel
            panel.add(leftDecoration, BorderLayout.WEST);
            panel.add(infoPanel, BorderLayout.CENTER);
            panel.add(actionPanel, BorderLayout.EAST);
        } catch (SQLException e) {
            // Handle exception gracefully
            JLabel errorLabel = new JLabel("Error loading template details");
            panel.add(errorLabel, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    // Enhanced createTemplateCard method for grid view
    private JPanel createTemplateCard(Template template) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(new Color(200, 200, 200))
        ));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(280, 300));
        
        // Add room preview (could be a colored panel to represent the room)
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(240, 240, 240));
        previewPanel.setPreferredSize(new Dimension(0, 180));
        
        try {
            RoomService roomService = new RoomService();
            Room room = roomService.getRoomById(template.getRoomId());
            
            if (room != null) {
                // Create a simplified room preview
                JPanel roomPreview = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        int width = getWidth();
                        int height = getHeight();
                        int padding = 20;
                        
                        // Set background
                        g2d.setColor(room.getColorObject());
                        
                        // Draw room shape based on type
                        if ("Circular".equals(room.getShape())) {
                            g2d.fillOval(padding, padding, width - 2*padding, height - 2*padding);
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.drawOval(padding, padding, width - 2*padding, height - 2*padding);
                        } else {
                            g2d.fillRect(padding, padding, width - 2*padding, height - 2*padding);
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.drawRect(padding, padding, width - 2*padding, height - 2*padding);
                        }
                        
                        // Add dimensions text
                        g2d.setColor(Color.WHITE);
                        String dimensionsText = String.format("%.1fm × %.1fm", room.getWidth(), room.getLength());
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(dimensionsText);
                        g2d.drawString(dimensionsText, (width - textWidth) / 2, height / 2 + 5);
                    }
                };
                roomPreview.setBackground(new Color(240, 240, 240));
                previewPanel.add(roomPreview, BorderLayout.CENTER);
            }
        } catch (SQLException e) {
            // Just use default preview if we can't load the room
        }
        
        // Info panel with title, date, etc.
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(template.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel typeLabel = new JLabel("Room Type: " + template.getRoomType());
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Format date to a readable format
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String dateString = sdf.format(template.getCreatedAt());
        JLabel dateLabel = new JLabel("Created: " + dateString);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel commentsLabel = new JLabel("<html>" + (template.getComments() != null && !template.getComments().isEmpty() ? 
            template.getComments() : "<i>No description</i>") + "</html>");
        commentsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        commentsLabel.setForeground(new Color(100, 100, 100));
        commentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add components to info panel
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(typeLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(dateLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(commentsLabel);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JButton viewButton = new JButton("View Layout");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        
        // View button action - load the template in 2D view
        viewButton.addActionListener(e -> {
            try {
                // Get room from the template
                RoomService roomService = new RoomService();
                Room room = roomService.getRoomById(template.getRoomId());
                
                if (room != null) {
                    // Create 2D view panel with the room
                    TwoDViewPanel twoDViewPanel = new TwoDViewPanel(room);
                    
                    // Add to content panel and show it
                    contentPanel.add(twoDViewPanel, "2d-view");
                    cardLayout.show(contentPanel, "2d-view");
                } else {
                    JOptionPane.showMessageDialog(panel, 
                        "Room not found for this template", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, 
                    "Error loading template: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        // Edit button action
        editButton.addActionListener(e -> {
            showTemplateEditDialog(template, panel);
        });
        
        // Delete button action
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(panel, 
                "Are you sure you want to delete this template?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    TemplateService templateService = new TemplateService();
                    boolean deleted = templateService.deleteTemplate(template.getId());
                    
                    if (deleted) {
                        JOptionPane.showMessageDialog(panel, 
                            "Template deleted successfully", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Refresh the templates panel
                        cardLayout.show(contentPanel, "other-panel"); // Switch to any other panel temporarily
                        cardLayout.show(contentPanel, "templates"); // Switch back to refresh
                    } else {
                        JOptionPane.showMessageDialog(panel, 
                            "Failed to delete template", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Add everything to the main panel
        panel.add(previewPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showTemplateEditDialog(Template template, JPanel parentPanel) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, "Edit Template", true);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, "Edit Template", true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Edit Template");
            dialog.setModal(true);
        }
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Template title
        panel.add(new JLabel("Template Title:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField titleField = new JTextField(20);
        titleField.setText(template.getTitle());
        panel.add(titleField, gbc);
        
        // Room type field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Room Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> roomTypeCombo = new JComboBox<>(new String[] {
            "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Dining Room", "Other"
        });
        roomTypeCombo.setSelectedItem(template.getRoomType());
        panel.add(roomTypeCombo, gbc);
        
        // Comments
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Comments:"), gbc);
        
        gbc.gridx = 1;
        JTextArea commentsArea = new JTextArea(5, 20);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setText(template.getComments());
        JScrollPane scrollPane = new JScrollPane(commentsArea);
        panel.add(scrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save Changes");
        
        cancelButton.addActionListener(evt -> dialog.dispose());
        
        saveButton.addActionListener(evt -> {
            String title = titleField.getText().trim();
            String roomType = (String) roomTypeCombo.getSelectedItem();
            String comments = commentsArea.getText().trim();
            
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a template title", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Update template object
                template.setTitle(title);
                template.setComments(comments);
                template.setRoomType(roomType);
                
                // Save changes
                TemplateService templateService = new TemplateService();
                boolean updated = templateService.updateTemplate(template);
                
                if (updated) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Template updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    
                    // Refresh the templates panel
                    cardLayout.show(contentPanel, "other-panel"); // Switch to any other panel temporarily
                    cardLayout.show(contentPanel, "templates"); // Switch back to refresh
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Failed to update template", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
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
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createSettingsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Designer Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Profile settings
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        profilePanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(currentUser.getFullName(), 20);
        profilePanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        profilePanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(currentUser.getEmail(), 20);
        profilePanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        profilePanel.add(new JLabel("Change Password:"), gbc);
        
        gbc.gridx = 1;
        JButton changePasswordButton = new JButton("Change Password");
        profilePanel.add(changePasswordButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveProfileButton = new JButton("Save Profile");
        profilePanel.add(saveProfileButton, gbc);
        
        // Preferences settings
        JPanel preferencesPanel = new JPanel(new GridBagLayout());
        preferencesPanel.setBackground(Color.WHITE);
        preferencesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        preferencesPanel.add(new JLabel("Default Units:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<String> unitsCombo = new JComboBox<>(new String[] {"Metric (cm)", "Imperial (inches)"});
        preferencesPanel.add(unitsCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        preferencesPanel.add(new JLabel("Default View:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> viewCombo = new JComboBox<>(new String[] {"2D Top View", "3D Perspective"});
        preferencesPanel.add(viewCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        preferencesPanel.add(new JLabel("Autosave Interval:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> autosaveCombo = new JComboBox<>(new String[] {"1 minute", "5 minutes", "10 minutes", "Never"});
        preferencesPanel.add(autosaveCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton savePrefsButton = new JButton("Save Preferences");
        preferencesPanel.add(savePrefsButton, gbc);
        
        tabbedPane.addTab("Profile", profilePanel);
        tabbedPane.addTab("Preferences", preferencesPanel);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Add a placeholder 3D preview panel that will prompt the user to first create or load a room
    private JPanel create3DPlaceholderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("3D Room Design View");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(new Color(245, 245, 245));
        
        JLabel messageLabel = new JLabel("Please create a new design or open an existing project to view in 3D");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JButton createNewButton = new JButton("Create New Design");
        createNewButton.addActionListener(e -> cardLayout.show(contentPanel, "new-design"));
        
        JButton openProjectButton = new JButton("Open Projects");
        openProjectButton.addActionListener(e -> cardLayout.show(contentPanel, "projects"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        messagePanel.add(messageLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        messagePanel.add(createNewButton, gbc);
        
        gbc.gridx = 1;
        messagePanel.add(openProjectButton, gbc);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(messagePanel, BorderLayout.CENTER);
        
        return panel;
    }
}