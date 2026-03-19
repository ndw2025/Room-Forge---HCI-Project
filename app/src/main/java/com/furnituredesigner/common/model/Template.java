package com.furnituredesigner.common.model;

import java.util.Date;

public class Template {
    private int id;
    private String title;
    private String comments;
    private int roomId;
    private int userId;
    private String roomType;
    private Date createdAt;
    
    public Template() {
        // Default constructor
    }
    
    public Template(int id, String title, String comments, int roomId, int userId, String roomType, Date createdAt) {
        this.id = id;
        this.title = title;
        this.comments = comments;
        this.roomId = roomId;
        this.userId = userId;
        this.roomType = roomType;
        this.createdAt = createdAt;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return title + " (" + roomType + ")";
    }
}