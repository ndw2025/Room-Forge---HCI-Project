package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.User;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;

public class UserFormDialog extends JDialog {
    
    private User user;
    private String password = "";
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JComboBox<String> roleComboBox;
    
    private boolean isEditMode;
    
    public UserFormDialog(Window owner, User userToEdit) {
        super(owner, userToEdit == null ? "Add New User" : "Edit User", ModalityType.APPLICATION_MODAL);
        
        this.isEditMode = userToEdit != null;
        if (isEditMode) {
            this.user = userToEdit;
        }
        
        setupUI();
        
        if (isEditMode) {
            populateFields();
        }
        
        setSize(500, 450);
        setLocationRelativeTo(owner);
    }
    
    private void setupUI() {
        JPanel mainPanel = new JPanel(new MigLayout("fillx, insets 20", "[30%]10[grow]", "[]15[]15[]15[]15[]15[]25[]"));
        mainPanel.setBackground(Color.WHITE);
        
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
        JLabel passwordLabel = new JLabel(isEditMode ? "New Password:" : "Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Role field
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        roleComboBox = new JComboBox<>(new String[]{"designer", "admin"});
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Password note for edit mode
        JLabel passwordNoteLabel = new JLabel("<html><i>Leave blank to keep current password</i></html>");
        passwordNoteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        passwordNoteLabel.setForeground(new Color(100, 100, 100));
        
        // Buttons
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.addActionListener(e -> saveUser());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.addActionListener(e -> dispose());
        
        // Add components to panel
        mainPanel.add(usernameLabel, "align right");
        mainPanel.add(usernameField, "growx, wrap");
        
        mainPanel.add(fullNameLabel, "align right");
        mainPanel.add(fullNameField, "growx, wrap");
        
        mainPanel.add(emailLabel, "align right");
        mainPanel.add(emailField, "growx, wrap");
        
        mainPanel.add(passwordLabel, "align right");
        mainPanel.add(passwordField, "growx, wrap");
        
        if (isEditMode) {
            mainPanel.add(new JLabel(""), "align right");
            mainPanel.add(passwordNoteLabel, "growx, wrap");
        }
        
        mainPanel.add(confirmPasswordLabel, "align right");
        mainPanel.add(confirmPasswordField, "growx, wrap");
        
        mainPanel.add(roleLabel, "align right");
        mainPanel.add(roleComboBox, "growx, wrap");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        mainPanel.add(buttonPanel, "span, align right");
        
        // If it's an edit for a superadmin, disable role changing
        if (isEditMode && user.isSuperAdmin()) {
            roleComboBox.setEnabled(false);
        }
        
        // Add main panel to dialog
        add(mainPanel);
    }
    
    private void populateFields() {
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        roleComboBox.setSelectedItem(user.getRole());
        
        // Username shouldn't be editable in edit mode
        usernameField.setEnabled(false);
    }
    
    private void saveUser() {
        // Validate fields
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();
        
        // Basic validation
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username, full name, and email are required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // If not in edit mode or password is provided, validate it
        if (!isEditMode || !password.isEmpty()) {
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Password is required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                    "Passwords do not match",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters long",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Create or update user object
        if (isEditMode) {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
        } else {
            user = new User(0, username, fullName, email, role);
        }
        
        // Close dialog
        setVisible(false);
    }
    
    public User getUser() {
        return user;
    }
    
    public String getPassword() {
        return password;
    }
}