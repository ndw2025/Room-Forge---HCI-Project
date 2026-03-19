package com.furnituredesigner.server.service;

import com.furnituredesigner.common.model.Furniture;
import com.furnituredesigner.server.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FurnitureService {
    
    public Furniture addFurniture(Furniture furniture) throws SQLException {
        String sql = "INSERT INTO furniture (room_id, type, x_pos, y_pos, width, length, height, color, rotation) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, furniture.getRoomId());
            stmt.setString(2, furniture.getType());
            stmt.setDouble(3, furniture.getXPos());
            stmt.setDouble(4, furniture.getYPos());
            stmt.setDouble(5, furniture.getWidth());
            stmt.setDouble(6, furniture.getLength());
            stmt.setDouble(7, furniture.getHeight());
            stmt.setString(8, furniture.getColor());
            stmt.setDouble(9, furniture.getRotation());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        furniture.setId(rs.getInt(1));
                        return furniture;
                    }
                }
            }
        }
        
        return null;
    }
    
    public boolean updateFurniture(Furniture furniture) throws SQLException {
        String sql = "UPDATE furniture SET x_pos = ?, y_pos = ?, width = ?, length = ?, " +
                     "height = ?, color = ?, rotation = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, furniture.getXPos());
            stmt.setDouble(2, furniture.getYPos());
            stmt.setDouble(3, furniture.getWidth());
            stmt.setDouble(4, furniture.getLength());
            stmt.setDouble(5, furniture.getHeight());
            stmt.setString(6, furniture.getColor());
            stmt.setDouble(7, furniture.getRotation());
            stmt.setInt(8, furniture.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteFurniture(int furnitureId) throws SQLException {
        String sql = "DELETE FROM furniture WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, furnitureId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Furniture> getFurnitureByRoomId(int roomId) throws SQLException {
        String sql = "SELECT * FROM furniture WHERE room_id = ?";
        List<Furniture> furnitureList = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Furniture furniture = new Furniture(
                        rs.getInt("id"),
                        rs.getInt("room_id"),
                        rs.getString("type"),
                        rs.getDouble("x_pos"),
                        rs.getDouble("y_pos"),
                        rs.getDouble("width"),
                        rs.getDouble("length"),
                        rs.getDouble("height"),
                        rs.getString("color"),
                        rs.getDouble("rotation")
                    );
                    furnitureList.add(furniture);
                }
            }
        }
        
        return furnitureList;
    }
}