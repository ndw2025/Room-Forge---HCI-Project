package com.furnituredesigner.server.service;

import com.furnituredesigner.server.db.DatabaseManager;
import com.furnituredesigner.common.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, full_name, email, role FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                
                // Check password with BCrypt
                if (storedHash.startsWith("$2a$") && BCrypt.checkpw(password, storedHash)) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                }
            }
        }
        
        return null; // Authentication failed
    }
    
    public boolean checkSuperAdminExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'superadmin'";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }
}