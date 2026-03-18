package com.furnituredesigner.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class NavigationPanel extends JPanel {
    
    private JPanel menuPanel;
    private JButton collapseButton;
    private boolean collapsed = false;
    private int expandedWidth = 250;
    private int collapsedWidth = 60;
    private List<JPanel> menuItems = new ArrayList<>();
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    
    public NavigationPanel(JPanel contentPanel, CardLayout contentCardLayout) {
        this.contentPanel = contentPanel;
        this.contentCardLayout = contentCardLayout;
        
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));
        setPreferredSize(new Dimension(expandedWidth, getHeight()));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(70, 70, 70)));
        
        // Header panel with app title and collapse button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setPreferredSize(new Dimension(expandedWidth, 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 70, 70)));
        
        JLabel titleLabel = new JLabel("RoomForge");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        collapseButton = new JButton("◀");  // Using a simple unicode character
        collapseButton.setFocusPainted(false);
        collapseButton.setContentAreaFilled(false);
        collapseButton.setBorderPainted(false);
        collapseButton.setForeground(Color.WHITE);
        collapseButton.addActionListener(e -> toggleCollapse());
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(collapseButton, BorderLayout.EAST);
        
        // Menu panel
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(50, 50, 50));
        
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void addMenuItem(String title, String cardName, String iconText) {
        JPanel menuItem = new JPanel(new BorderLayout());
        menuItem.setMaximumSize(new Dimension(expandedWidth, 50));
        menuItem.setBackground(new Color(50, 50, 50));
        
        // Use text as simple icon for now
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setPreferredSize(new Dimension(50, 50));
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        menuItem.add(iconLabel, BorderLayout.WEST);
        menuItem.add(titleLabel, BorderLayout.CENTER);
        
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(new Color(70, 70, 70));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(new Color(50, 50, 50));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                contentCardLayout.show(contentPanel, cardName);
                for (JPanel item : menuItems) {
                    item.setBackground(new Color(50, 50, 50));
                }
                menuItem.setBackground(new Color(70, 70, 70));
            }
        });
        
        menuItems.add(menuItem);
        menuPanel.add(menuItem);
        menuPanel.add(Box.createVerticalStrut(1)); // Separator
    }
    
    public void addNavButton(String title, String iconText, ActionListener action) {
        JPanel menuItem = new JPanel(new BorderLayout());
        menuItem.setMaximumSize(new Dimension(expandedWidth, 50));
        menuItem.setBackground(new Color(50, 50, 50));
        
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setPreferredSize(new Dimension(50, 50));
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        menuItem.add(iconLabel, BorderLayout.WEST);
        menuItem.add(titleLabel, BorderLayout.CENTER);
        
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(new Color(70, 70, 70));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(new Color(50, 50, 50));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
        });
        
        menuItems.add(menuItem);
        menuPanel.add(menuItem);
        menuPanel.add(Box.createVerticalStrut(1)); // Separator
    }
    
    public void addSeparator() {
        JPanel separator = new JPanel();
        separator.setMaximumSize(new Dimension(expandedWidth, 1));
        separator.setBackground(new Color(70, 70, 70));
        menuPanel.add(separator);
        menuPanel.add(Box.createVerticalStrut(10));
    }
    
    private void toggleCollapse() {
        collapsed = !collapsed;
        
        if (collapsed) {
            setPreferredSize(new Dimension(collapsedWidth, getHeight()));
            collapseButton.setText("▶");
            
            // Hide text for all menu items
            for (JPanel menuItem : menuItems) {
                Component[] components = menuItem.getComponents();
                for (Component component : components) {
                    if (component instanceof JLabel && !((JLabel) component).getText().matches(".")) {
                        component.setVisible(false);
                    }
                }
            }
        } else {
            setPreferredSize(new Dimension(expandedWidth, getHeight()));
            collapseButton.setText("◀");
            
            // Show text for all menu items
            for (JPanel menuItem : menuItems) {
                Component[] components = menuItem.getComponents();
                for (Component component : components) {
                    component.setVisible(true);
                }
            }
        }
        
        revalidate();
        repaint();
    }
}