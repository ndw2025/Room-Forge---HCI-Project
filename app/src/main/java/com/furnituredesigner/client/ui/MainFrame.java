package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.User;
import com.furnituredesigner.App;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private AdminDashboardPanel adminDashboardPanel;
    private DesignerDashboardPanel designerDashboardPanel;
    
    public MainFrame(User user) {
        this.currentUser = user;
        
        // Configure the frame
        setTitle("RoomForge - Welcome " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        
        // Create UI components
        setupContentPanel();
        
        // Maximize the window
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private Image createAppIcon() {
        // Get the icon from UIManager without casting directly to ImageIcon
        Icon icon = UIManager.getIcon("FileView.computerIcon");
        if (icon != null) {
            // If it's an ImageIcon, get its image
            if (icon instanceof ImageIcon) {
                return ((ImageIcon) icon).getImage();
            }
            
            // If it's a different type of icon, create a BufferedImage from it
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            icon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();
            return image;
        }
        
        // Fallback to creating a simple colored icon if UIManager icon is not available
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(41, 121, 255));
        g2d.fillRect(0, 0, 32, 32);
        g2d.dispose();
        
        return image;
    }
    
    private void setupContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Create dashboard panels without navigation action listeners
        adminDashboardPanel = new AdminDashboardPanel(currentUser);
        designerDashboardPanel = new DesignerDashboardPanel(currentUser);
        
        // Add dashboard panels to content panel
        contentPanel.add(adminDashboardPanel, "admin");
        contentPanel.add(designerDashboardPanel, "designer");
        
        // Set default dashboard based on user role
        if (currentUser.isSuperAdmin() || currentUser.isAdmin()) {
            cardLayout.show(contentPanel, "admin");
        } else {
            cardLayout.show(contentPanel, "designer");
        }
        
        // Add content panel to the frame
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void switchToDashboard(String dashboard) {
        cardLayout.show(contentPanel, dashboard);
        
        // Update title based on current dashboard
        if (dashboard.equals("admin")) {
            setTitle("RoomForge - Admin Dashboard - " + currentUser.getFullName());
        } else {
            setTitle("RoomForge - Designer Dashboard - " + currentUser.getFullName());
        }
    }
    
    // Add logout method
    public void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // Close this frame
            
            // Show login screen again
            SwingUtilities.invokeLater(() -> {
                App.showLoginScreen();
            });
        }
    }
}