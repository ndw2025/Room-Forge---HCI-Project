package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.User;
import com.furnituredesigner.server.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import net.miginfocom.swing.MigLayout;

public class SuperAdminSetupPanel extends JPanel {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JButton setupButton;
    private final SetupListener setupListener;
    
    public interface SetupListener {
        void onSetupComplete(User user);
    }
    
    public SuperAdminSetupPanel(SetupListener listener) {
        this.setupListener = listener;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new MigLayout("fillx, insets 20, wrap", "[grow]", "[]20[][]20[]"));
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JLabel titleLabel = new JLabel("Create Super Admin Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        JLabel subtitleLabel = new JLabel(
            "<html>Welcome to RoomForge! To get started, please create a super admin account.<br>" +
            "This account will have full access to all features and functionalities.</html>"
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        // Form panel
        JPanel formPanel = new JPanel(new MigLayout("fillx, insets 0", "[30%]10[grow]", "[]15[]15[]15[]15[]"));
        formPanel.setOpaque(false);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Full Name field
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        fullNameField = new JTextField(20);
        fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Email field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add components to form panel
        formPanel.add(usernameLabel, "align right");
        formPanel.add(usernameField, "growx, wrap");
        
        formPanel.add(fullNameLabel, "align right");
        formPanel.add(fullNameField, "growx, wrap");
        
        formPanel.add(emailLabel, "align right");
        formPanel.add(emailField, "growx, wrap");
        
        formPanel.add(passwordLabel, "align right");
        formPanel.add(passwordField, "growx, wrap");
        
        formPanel.add(confirmPasswordLabel, "align right");
        formPanel.add(confirmPasswordField, "growx");
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        setupButton = new JButton("Create Admin Account");
        setupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        setupButton.setIcon(UIManager.getIcon("FileView.computerIcon")); // Use standard Swing icon
        setupButton.setBackground(new Color(41, 121, 255));
        setupButton.setForeground(Color.WHITE);
        setupButton.setFocusPainted(false);
        setupButton.addActionListener(this::attemptSetup);
        
        buttonPanel.add(setupButton);
        
        // Add components to main panel
        mainPanel.add(titleLabel, "center");
        mainPanel.add(subtitleLabel);
        mainPanel.add(formPanel, "growx");
        mainPanel.add(buttonPanel, "right");
        
        // Add to this panel
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void attemptSetup(ActionEvent e) {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validate inputs
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "All fields are required", 
                "Setup Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "Passwords do not match", 
                "Setup Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "Password must be at least 6 characters long", 
                "Setup Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create super admin
        try {
            UserService userService = new UserService();
            User user = userService.createSuperAdmin(username, password, fullName, email);
            
            if (user != null) {
                JOptionPane.showMessageDialog(this, 
                    "Super admin account created successfully", 
                    "Setup Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (setupListener != null) {
                    setupListener.onSetupComplete(user);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create super admin account", 
                    "Setup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("UNIQUE constraint failed")) {
                JOptionPane.showMessageDialog(this, 
                    "Username already exists. Please choose a different username.", 
                    "Setup Error", 
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + ex.getMessage(), 
                    "Setup Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}