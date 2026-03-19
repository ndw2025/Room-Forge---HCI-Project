package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.User;
import com.furnituredesigner.common.model.Room;
import com.furnituredesigner.common.model.Template;
import com.furnituredesigner.server.service.UserService;
import com.furnituredesigner.server.service.RoomService;
import com.furnituredesigner.server.service.TemplateService;
import java.awt.geom.Arc2D;
import com.furnituredesigner.client.ui.ProjectManagementPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

public class AdminDashboardPanel extends JPanel {
    
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private UserManagementPanel userManagementPanel;
    private ProjectManagementPanel projectManagementPanel;
    private UserService userService;
    private RoomService roomService;
    private TemplateService templateService;
    
    // Dashboard metrics
    private int totalUsers = 0;
    private int designerCount = 0;
    private int adminCount = 0;
    private int totalRooms = 0;
    private int totalTemplates = 0;
    
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color INFO_COLOR = new Color(142, 68, 173);
    private final Color WARNING_COLOR = new Color(211, 84, 0);
    private final Color LIGHT_BG = new Color(248, 249, 250);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    
    public AdminDashboardPanel(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.roomService = new RoomService();
        this.templateService = new TemplateService();
        
        setLayout(new BorderLayout());
        
        // Create card layout for the content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Create and configure the navigation panel
        NavigationPanel navPanel = new NavigationPanel(contentPanel, cardLayout);
        
        // Add menu items with simple text icons instead of emoji
        navPanel.addMenuItem("Dashboard", "dashboard", "D");
        navPanel.addMenuItem("Users", "users", "U");
        navPanel.addMenuItem("Projects", "projects", "P");
        navPanel.addMenuItem("Reports", "reports", "R");
        navPanel.addMenuItem("Settings", "settings", "S");
        
        // Load data for dashboard
        loadDashboardData();
        
        // Create content panels
        JPanel dashboardContent = createDashboardContent();
        
        // Create and add UserManagementPanel
        userManagementPanel = new UserManagementPanel(currentUser);
        
        projectManagementPanel = new ProjectManagementPanel(currentUser);
        JPanel reportsContent = createReportsContent();
        JPanel settingsContent = createSettingsContent();
        
        // Add content panels to the card layout
        contentPanel.add(dashboardContent, "dashboard");
        contentPanel.add(userManagementPanel, "users");
        contentPanel.add(projectManagementPanel, "projects");
        contentPanel.add(reportsContent, "reports");
        contentPanel.add(settingsContent, "settings");
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Add components to the main panel
        add(navPanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Show the dashboard by default
        cardLayout.show(contentPanel, "dashboard");
    }
    
    private void loadDashboardData() {
        try {
            // Get user statistics
            List<User> allUsers = userService.getAllUsers();
            totalUsers = allUsers.size();
            designerCount = 0;
            adminCount = 0;
            
            for (User user : allUsers) {
                if (user.isAdmin()) {
                    adminCount++;
                } else if (user.isDesigner()) {
                    designerCount++;
                }
            }
            
            // Get room count - assume we want all rooms
            totalRooms = 0;
            for (User user : allUsers) {
                try {
                    List<Room> userRooms = roomService.getRoomsByUserId(user.getId());
                    totalRooms += userRooms.size();
                } catch (SQLException e) {
                    // Ignore for dashboard counting
                }
            }
            
            // Get template count
            totalTemplates = 0;
            for (User user : allUsers) {
                try {
                    List<Template> userTemplates = templateService.getTemplatesByUserId(user.getId());
                    totalTemplates += userTemplates.size();
                } catch (SQLException e) {
                    // Ignore for dashboard counting
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            // Set default values if data loading fails
            totalUsers = 0;
            designerCount = 0;
            adminCount = 0;
            totalRooms = 0;
            totalTemplates = 0;
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        // Create user avatar panel
        JPanel userAvatarPanel = new JPanel(new BorderLayout(10, 0));
        userAvatarPanel.setOpaque(false);
        
        // Create avatar
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avatarLabel.setText(currentUser.getFullName().substring(0, 1).toUpperCase());
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(PRIMARY_COLOR);
        avatarLabel.setBorder(new LineBorder(new Color(41, 128, 185, 100), 2));
        
        // Make avatar round by using a custom painter
        avatarLabel.setLayout(new BorderLayout());
        
        // User info
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1));
        userInfoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel roleLabel = new JLabel(currentUser.getRole());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(120, 120, 120));
        
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(roleLabel);
        
        userAvatarPanel.add(avatarLabel, BorderLayout.WEST);
        userAvatarPanel.add(userInfoPanel, BorderLayout.CENTER);
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(241, 241, 241));
        logoutButton.setForeground(new Color(70, 70, 70));
        logoutButton.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220)),
            new EmptyBorder(5, 15, 5, 15)
        ));
        logoutButton.setFocusPainted(false);
        
        logoutButton.addActionListener(e -> {
            // Call the logout method from the root frame
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame) {
                ((MainFrame) window).logout();
            }
        });
        
        rightPanel.add(userAvatarPanel);
        rightPanel.add(logoutButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createDashboardContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(LIGHT_BG);
        
        // Create a container with padding
        JPanel containerPanel = new JPanel(new BorderLayout(0, 20));
        containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        containerPanel.setOpaque(false);
        
        // Key metrics panel - 4 cards in a row
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        
        statsPanel.add(createStatCard("Total Users", String.valueOf(totalUsers), PRIMARY_COLOR, FontAwesomeSolid.USERS));
        statsPanel.add(createStatCard("Designers", String.valueOf(designerCount), SUCCESS_COLOR, FontAwesomeSolid.PENCIL_RULER));
        statsPanel.add(createStatCard("Total Projects", String.valueOf(totalRooms), INFO_COLOR, FontAwesomeSolid.PROJECT_DIAGRAM));
        statsPanel.add(createStatCard("Templates", String.valueOf(totalTemplates), WARNING_COLOR, FontAwesomeSolid.COPY));
        
        // Main content split into two columns
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContent.setOpaque(false);
        
        // Left column - charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        chartsPanel.setOpaque(false);
        
        // Add user distribution chart
        chartsPanel.add(createUserDistributionChart());
        
        // Add project status chart
        chartsPanel.add(createProjectStatusChart());
        
        // Right column - info panels
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        infoPanel.setOpaque(false);
        
        // Recent templates panel
        infoPanel.add(createRecentTemplatesPanel());
        
        // Activity panel
        infoPanel.add(createActivityPanel());
        
        mainContent.add(chartsPanel);
        mainContent.add(infoPanel);
        
        // Add everything to container
        containerPanel.add(statsPanel, BorderLayout.NORTH);
        containerPanel.add(mainContent, BorderLayout.CENTER);
        
        // Add container to main panel
        panel.add(containerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color, FontAwesomeSolid icon) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Icon panel on the left
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setOpaque(true);
        iconPanel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        iconPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create icon
        JLabel iconLabel = new JLabel(FontIcon.of(icon, 24, color));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconPanel.add(iconLabel, BorderLayout.CENTER);
        
        // Text panel on the right
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(120, 120, 120));
        
        textPanel.add(valueLabel);
        textPanel.add(titleLabel);
        
        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUserDistributionChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("User Distribution");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create the actual pie chart
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int size = Math.min(width, height) - 40;
                int x = (width - size) / 2;
                int y = (height - size) / 2;
                
                // Draw pie slices
                double totalValue = adminCount + designerCount;
                if (totalValue == 0) totalValue = 1; // Avoid division by zero
                
                double startAngle = 0;
                
                // Admin slice
                double adminPercent = adminCount / totalValue;
                double adminAngle = 360 * adminPercent;
                g2d.setColor(PRIMARY_COLOR);
                g2d.fill(new Arc2D.Double(x, y, size, size, startAngle, adminAngle, Arc2D.PIE));
                
                // Designer slice
                startAngle += adminAngle;
                double designerPercent = designerCount / totalValue;
                double designerAngle = 360 * designerPercent;
                g2d.setColor(SUCCESS_COLOR);
                g2d.fill(new Arc2D.Double(x, y, size, size, startAngle, designerAngle, Arc2D.PIE));
                
                // Draw center circle (donut hole)
                int holeSize = size / 2;
                int holeX = x + (size - holeSize) / 2;
                int holeY = y + (size - holeSize) / 2;
                g2d.setColor(Color.WHITE);
                g2d.fill(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 200);
            }
        };
        
        // Create legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        legendPanel.setOpaque(false);
        
        // Admin legend
        JPanel adminLegend = createLegendItem("Admins", PRIMARY_COLOR, adminCount + " (" + 
                (totalUsers > 0 ? Math.round((adminCount * 100.0) / totalUsers) : 0) + "%)");
        
        // Designer legend
        JPanel designerLegend = createLegendItem("Designers", SUCCESS_COLOR, designerCount + " (" + 
                (totalUsers > 0 ? Math.round((designerCount * 100.0) / totalUsers) : 0) + "%)");
        
        legendPanel.add(adminLegend);
        legendPanel.add(designerLegend);
        
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.add(legendPanel, BorderLayout.SOUTH);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLegendItem(String label, Color color, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBackground(color);
        
        JLabel labelText = new JLabel(label + ": " + value);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(colorBox);
        panel.add(labelText);
        
        return panel;
    }
    
    private JPanel createProjectStatusChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("Project Activity");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create a bar chart for project activity
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw x and y axis
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(40, height - 40, width - 20, height - 40); // x-axis
                g2d.drawLine(40, 20, 40, height - 40); // y-axis
                
                // Draw bars
                int barCount = 7; // Days of the week
                int barWidth = (width - 60) / barCount - 10;
                int maxBarHeight = height - 60;
                
                // Sample data - you would get this from your database
                int[] data = {8, 12, 7, 15, 21, 14, 9};
                String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                Color[] colors = {
                    new Color(52, 152, 219),
                    new Color(155, 89, 182),
                    new Color(52, 152, 219),
                    new Color(155, 89, 182),
                    new Color(52, 152, 219),
                    new Color(155, 89, 182),
                    new Color(52, 152, 219)
                };
                
                int maxData = 0;
                for (int value : data) {
                    maxData = Math.max(maxData, value);
                }
                
                for (int i = 0; i < barCount; i++) {
                    int barHeight = maxData > 0 ? (int)(data[i] * 1.0 / maxData * maxBarHeight) : 0;
                    int barX = 40 + i * (barWidth + 10);
                    int barY = height - 40 - barHeight;
                    
                    // Draw bar
                    g2d.setColor(colors[i]);
                    g2d.fill(new Rectangle2D.Double(barX, barY, barWidth, barHeight));
                    
                    // Draw label
                    g2d.setColor(Color.DARK_GRAY);
                    FontMetrics fm = g2d.getFontMetrics();
                    int labelWidth = fm.stringWidth(labels[i]);
                    g2d.drawString(labels[i], barX + (barWidth - labelWidth) / 2, height - 20);
                    
                    // Draw value above bar
                    String valueStr = String.valueOf(data[i]);
                    int valueWidth = fm.stringWidth(valueStr);
                    g2d.drawString(valueStr, barX + (barWidth - valueWidth) / 2, barY - 5);
                }
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 200);
            }
        };
        
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRecentTemplatesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Recent Templates");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton viewAllButton = new JButton("View All");
        viewAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllButton.setBorderPainted(false);
        viewAllButton.setFocusPainted(false);
        viewAllButton.setContentAreaFilled(false);
        viewAllButton.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllButton, BorderLayout.EAST);
        
        // Template list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        
        // Sample template items
        listPanel.add(createTemplateItem("Modern Living Room", "Living Room", "John Designer", "2 days ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createTemplateItem("Minimalist Kitchen", "Kitchen", "Jane Designer", "3 days ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createTemplateItem("Office Space", "Office", "Mark Admin", "5 days ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createTemplateItem("Master Bedroom", "Bedroom", "Sarah Designer", "1 week ago"));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTemplateItem(String title, String type, String author, String timeAgo) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel iconLabel = new JLabel(FontIcon.of(FontAwesomeSolid.COPY, 16, new Color(120, 120, 120)));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        detailsPanel.setOpaque(false);
        
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLabel.setForeground(new Color(120, 120, 120));
        
        JLabel dotLabel = new JLabel("•");
        dotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dotLabel.setForeground(new Color(120, 120, 120));
        
        JLabel authorLabel = new JLabel(author);
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        authorLabel.setForeground(new Color(120, 120, 120));
        
        detailsPanel.add(typeLabel);
        detailsPanel.add(dotLabel);
        detailsPanel.add(authorLabel);
        
        infoPanel.add(titleLabel);
        infoPanel.add(detailsPanel);
        
        JLabel timeLabel = new JLabel(timeAgo);
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        timeLabel.setForeground(new Color(150, 150, 150));
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(timeLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("Recent Activity");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Activity list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        
        // Sample activity items
        listPanel.add(createActivityItem(FontAwesomeSolid.USER_PLUS, SUCCESS_COLOR, "New user registered", "John Smith", "2 hours ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createActivityItem(FontAwesomeSolid.PROJECT_DIAGRAM, PRIMARY_COLOR, "New project created", "Office Redesign by Jane", "Yesterday"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createActivityItem(FontAwesomeSolid.EDIT, WARNING_COLOR, "Template updated", "Modern Living Room", "3 days ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createActivityItem(FontAwesomeSolid.TRASH_ALT, new Color(220, 53, 69), "Project deleted", "Kitchen Design by Mark", "1 week ago"));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActivityItem(FontAwesomeSolid icon, Color iconColor, String action, String details, String timeAgo) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Icon with circular background
        JPanel iconContainer = new JPanel(new BorderLayout());
        iconContainer.setPreferredSize(new Dimension(32, 32));
        iconContainer.setOpaque(true);
        iconContainer.setBackground(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 30));
        
        JLabel iconLabel = new JLabel(FontIcon.of(icon, 16, iconColor));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconContainer.add(iconLabel, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel actionLabel = new JLabel(action);
        actionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsLabel.setForeground(new Color(120, 120, 120));
        
        infoPanel.add(actionLabel);
        infoPanel.add(detailsLabel);
        
        JLabel timeLabel = new JLabel(timeAgo);
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        timeLabel.setForeground(new Color(150, 150, 150));
        
        panel.add(iconContainer, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(timeLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createProjectsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Project Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create a table for projects
        String[] columns = {"ID", "Name", "Owner", "Created Date", "Status", "Actions"};
        Object[][] data = {
            {1, "Modern Living Room", "John Designer", "2023-06-15", "Active", "View Edit Delete"},
            {2, "Minimalist Kitchen", "Jane Designer", "2023-06-20", "In Review", "View Edit Delete"},
            {3, "Office Redesign", "Bob User", "2023-06-25", "Completed", "View Edit Delete"},
            {4, "Bedroom Suite", "John Designer", "2023-07-01", "Draft", "View Edit Delete"}
        };
        
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReportsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel reportsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        reportsPanel.setOpaque(false);
        
        reportsPanel.add(createReportCard("User Activity Report", "View and analyze user activity over time"));
        reportsPanel.add(createReportCard("Project Status Report", "Overview of project statuses and completion rates"));
        reportsPanel.add(createReportCard("Resource Usage Report", "Monitor system resource usage and performance"));
        reportsPanel.add(createReportCard("Audit Log", "Security and access audit trail"));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(reportsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReportCard(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton generateButton = new JButton("Generate Report");
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);
        panel.add(generateButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSettingsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("System Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel settingsForm = new JPanel(new GridLayout(5, 2, 10, 15));
        settingsForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        settingsForm.setOpaque(false);
        
        settingsForm.add(new JLabel("Database Backup Schedule:"));
        settingsForm.add(new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"}));
        
        settingsForm.add(new JLabel("Email Notifications:"));
        settingsForm.add(new JCheckBox("Enable email notifications"));
        
        settingsForm.add(new JLabel("Log Level:"));
        settingsForm.add(new JComboBox<>(new String[]{"Info", "Warning", "Error", "Debug"}));
        
        settingsForm.add(new JLabel("Session Timeout (minutes):"));
        settingsForm.add(new JTextField("30"));
        
        JButton saveButton = new JButton("Save Settings");
        JButton resetButton = new JButton("Reset to Defaults");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(settingsForm, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Helper method to refresh the dashboard data from the database
     */
    public void refreshDashboard() {
        loadDashboardData();
        
        // Replace the dashboard content with the updated data
        JPanel updatedDashboardContent = createDashboardContent();
        contentPanel.remove(0);  // Remove old dashboard
        contentPanel.add(updatedDashboardContent, "dashboard", 0);
        
        // Show the updated dashboard
        cardLayout.show(contentPanel, "dashboard");
    }
    
    /**
     * Creates a custom round border for avatar labels
     */
    private static class RoundedBorder extends LineBorder {
        private int radius;
        
        RoundedBorder(Color color, int thickness, int radius) {
            super(color, thickness);
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(lineColor);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
    }
}