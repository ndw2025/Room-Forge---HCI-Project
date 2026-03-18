package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.User;
import com.furnituredesigner.server.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
import net.miginfocom.swing.MigLayout;

public class LoginPanel extends JPanel {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private LoginListener loginListener;
    
    // Add placeholder text constants
    private static final String USERNAME_PLACEHOLDER = "Enter username";
    private static final String PASSWORD_PLACEHOLDER = "Enter password";
    
    public interface LoginListener {
        void onLoginSuccess(User user);
    }
    
    public LoginPanel(LoginListener listener) {
        this.loginListener = listener;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create the main panel with MigLayout
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0", "[50%][50%]", "grow"));
        mainPanel.setBackground(Color.WHITE);
        
        // Left panel with logo and branding
        JPanel leftPanel = new JPanel(new MigLayout("fill, insets 0", "grow", "grow"));
        leftPanel.setBackground(new Color(41, 121, 255));
        
        JPanel brandingPanel = new JPanel(new MigLayout("fill, insets 30", "", "[]20[]"));
        brandingPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("RoomForge");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logoLabel.setForeground(Color.WHITE);
        
        JLabel taglineLabel = new JLabel("<html>Smart Furniture Preview<br>Design your space with confidence</html>");
        taglineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        taglineLabel.setForeground(new Color(220, 220, 220));
        
        brandingPanel.add(logoLabel, "wrap, center");
        brandingPanel.add(taglineLabel, "center");
        
        leftPanel.add(brandingPanel, "center");
        
        // Right panel with login form
        JPanel rightPanel = new JPanel(new MigLayout("fill, insets 40", "grow", "[]20[]20[]"));
        rightPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Login to Your Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        // Login form panel
        JPanel formPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]10[]10[]20[]"));
        formPanel.setOpaque(false);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(new Color(100, 100, 100));
        
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMargin(new Insets(5, 5, 5, 5));
        
        // Add placeholder functionality to username field
        setupPlaceholder(usernameField, USERNAME_PLACEHOLDER);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(100, 100, 100));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMargin(new Insets(5, 5, 5, 5));
        
        // Add placeholder functionality to password field
        setupPasswordPlaceholder(passwordField, PASSWORD_PLACEHOLDER);
        
        // Remember me checkbox
        JCheckBox rememberMeCheckbox = new JCheckBox("Remember me");
        rememberMeCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rememberMeCheckbox.setForeground(new Color(100, 100, 100));
        rememberMeCheckbox.setOpaque(false);
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(41, 121, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        
        // Add components to form panel
        formPanel.add(usernameLabel, "wrap");
        formPanel.add(usernameField, "growx, wrap");
        formPanel.add(passwordLabel, "wrap");
        formPanel.add(passwordField, "growx, wrap");
        formPanel.add(rememberMeCheckbox, "split 2");
        formPanel.add(new JLabel("<html><a href='#'>Forgot password?</a></html>"), "right, wrap");
        formPanel.add(loginButton, "growx");
        
        // Add components to right panel
        rightPanel.add(titleLabel, "wrap, center");
        rightPanel.add(formPanel, "growx, wrap");
        
        // Add panels to main panel
        mainPanel.add(leftPanel, "grow");
        mainPanel.add(rightPanel, "grow");
        
        // Add main panel to this panel
        add(mainPanel, BorderLayout.CENTER);
        
        // Add action listeners
        loginButton.addActionListener(this::attemptLogin);
        
        // Remove the default values that were set for testing
        // usernameField.setText("admin");
        // passwordField.setText("admin123");
    }
    
    private void setupPlaceholder(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);
        
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }
    
    private void setupPasswordPlaceholder(JPasswordField passwordField, String placeholder) {
        // For password fields we need a different approach
        passwordField.setEchoChar((char) 0); // Make the text visible initially
        passwordField.setForeground(Color.GRAY);
        passwordField.setText(placeholder);
        
        passwordField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String password = new String(passwordField.getPassword());
                if (password.equals(placeholder)) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•'); // Set echo char back to default bullet
                    passwordField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                String password = new String(passwordField.getPassword());
                if (password.isEmpty()) {
                    passwordField.setEchoChar((char) 0); // Turn off password hiding
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setText(placeholder);
                }
            }
        });
    }
    
    private void attemptLogin(ActionEvent e) {
        // Get username, checking if it's not the placeholder
        String username = usernameField.getText();
        if (username.equals(USERNAME_PLACEHOLDER)) {
            username = "";
        }
        
        // Get password, checking if it's not the placeholder
        String password = new String(passwordField.getPassword());
        if (password.equals(PASSWORD_PLACEHOLDER)) {
            password = "";
        }
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Attempt to authenticate
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            loginButton.setEnabled(false);
            
            AuthService authService = new AuthService();
            User user = authService.authenticate(username, password);
            
            if (user != null) {
                // Successful login
                if (loginListener != null) {
                    loginListener.onLoginSuccess(user);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
                loginButton.setEnabled(true);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Login error: " + ex.getMessage(), 
                "System Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            loginButton.setEnabled(true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}