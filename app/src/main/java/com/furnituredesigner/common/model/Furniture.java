package com.furnituredesigner.common.model;

import java.awt.Color;

public class Furniture {
    private int id;
    private int roomId;
    private String type; // Chair, Table, Sofa, etc.
    private double xPos;
    private double yPos;
    private double zPos;
    private double width;
    private double length;
    private double height;
    private String color;
    private double rotation; // in degrees
    
    public Furniture() {
        // Default constructor
    }
    
    public Furniture(int id, int roomId, String type, double xPos, double yPos, 
                    double width, double length, double height, String color, double rotation) {
        this.id = id;
        this.roomId = roomId;
        this.type = type;
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.length = length;
        this.height = height;
        this.color = color;
        this.rotation = rotation;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getXPos() { return xPos; }
    public void setXPos(double xPos) { this.xPos = xPos; }
    
    public double getYPos() { return yPos; }
    public void setYPos(double yPos) { this.yPos = yPos; }
    
    public double getZPos() { return zPos; }
    public void setZPos(double zPos) { this.zPos = zPos; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }
    
    // Helper method to convert color string to Color object
    public Color getColorObject() {
        try {
            return Color.decode(color);
        } catch (NumberFormatException e) {
            return Color.LIGHT_GRAY; // Default color
        }
    }
    
    @Override
    public String toString() {
        return type + " at (" + xPos + ", " + yPos + ")";
    }
}