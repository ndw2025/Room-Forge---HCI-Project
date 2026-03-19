package com.furnituredesigner.server.service;

import com.furnituredesigner.common.model.Room;
import com.furnituredesigner.server.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoomService {
    
    public Room createRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (name, width, length, height, color, shape, user_id, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, room.getName());
            stmt.setDouble(2, room.getWidth());
            stmt.setDouble(3, room.getLength());
            stmt.setDouble(4, room.getHeight());
            stmt.setString(5, room.getColor());
            stmt.setString(6, room.getShape());
            stmt.setInt(7, room.getUserId());
            stmt.setString(8, room.getDescription());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated ID
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setId(rs.getInt(1));
                        return room;
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<Room> getRoomsByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE user_id = ? ORDER BY id DESC";
        List<Room> rooms = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("width"),
                        rs.getDouble("length"),
                        rs.getDouble("height"),
                        rs.getString("color"),
                        rs.getString("shape"),
                        rs.getInt("user_id"),
                        rs.getString("description")
                    );
                    rooms.add(room);
                }
            }
        }
        
        return rooms;
    }
    
    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("width"),
                        rs.getDouble("length"),
                        rs.getDouble("height"),
                        rs.getString("color"),
                        rs.getString("shape"),
                        rs.getInt("user_id"),
                        rs.getString("description")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Delete a room by ID
     * 
     * @param roomId The ID of the room to delete
     * @return true if deletion was successful
     * @throws SQLException if there's an error accessing the database
     */
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update room details
     * 
     * @param room The room object with updated details
     * @return true if update was successful
     * @throws SQLException if there's an error accessing the database
     */
    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET name = ?, width = ?, length = ?, height = ?, " +
                     "color = ?, shape = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, room.getName());
            stmt.setDouble(2, room.getWidth());
            stmt.setDouble(3, room.getLength());
            stmt.setDouble(4, room.getHeight());
            stmt.setString(5, room.getColor());
            stmt.setString(6, room.getShape());
            stmt.setString(7, room.getDescription());
            stmt.setInt(8, room.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
}