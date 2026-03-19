package com.furnituredesigner.common.model;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    
    public User(int id, String username, String fullName, String email, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
    
    // Compatibility constructor for existing code
    public User(int id, String username, String fullName, String email) {
        this(id, username, fullName, email, "designer"); // Default role
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isSuperAdmin() {
        return "superadmin".equalsIgnoreCase(role);
    }
    
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role) || isSuperAdmin();
    }
    
    public boolean isDesigner() {
        return "designer".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + " - " + role + ")";
    }
}