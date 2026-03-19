package com.furnituredesigner.server.service;

import com.furnituredesigner.common.model.Template;
import com.furnituredesigner.server.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplateService {
    
    public Template saveTemplate(Template template) throws SQLException {
        String sql = "INSERT INTO templates (title, comments, room_id, user_id, room_type, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, template.getTitle());
            stmt.setString(2, template.getComments());
            stmt.setInt(3, template.getRoomId());
            stmt.setInt(4, template.getUserId());
            stmt.setString(5, template.getRoomType());
            
            // Use current timestamp if not provided
            Timestamp timestamp = template.getCreatedAt() != null ? 
                new Timestamp(template.getCreatedAt().getTime()) : 
                new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(6, timestamp);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        template.setId(rs.getInt(1));
                        return template;
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<Template> getTemplatesByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM templates WHERE user_id = ? ORDER BY created_at DESC";
        List<Template> templates = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Template template = new Template(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("comments"),
                        rs.getInt("room_id"),
                        rs.getInt("user_id"),
                        rs.getString("room_type"),
                        rs.getTimestamp("created_at")
                    );
                    templates.add(template);
                }
            }
        }
        
        return templates;
    }
    
    public Template getTemplateById(int templateId) throws SQLException {
        String sql = "SELECT * FROM templates WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, templateId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Template(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("comments"),
                        rs.getInt("room_id"),
                        rs.getInt("user_id"),
                        rs.getString("room_type"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        
        return null;
    }
    
    public boolean updateTemplate(Template template) throws SQLException {
        String sql = "UPDATE templates SET title = ?, comments = ?, room_type = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, template.getTitle());
            stmt.setString(2, template.getComments());
            stmt.setString(3, template.getRoomType());
            stmt.setInt(4, template.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteTemplate(int templateId) throws SQLException {
        String sql = "DELETE FROM templates WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, templateId);
            return stmt.executeUpdate() > 0;
        }
    }
}