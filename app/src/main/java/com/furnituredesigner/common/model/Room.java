package com.furnituredesigner.common.model;

import java.awt.Color;

public class Room {
    private int id;
    private String name;
    private double width;
    private double length;
    private double height;
    private String color;
    private String shape; // rectangle, square, circular
    private int userId;
    private String description;
    
    public Room(int id, String name, double width, double length, double height, 
                String color, String shape, int userId, String description) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.length = length;
        this.height = height;
        this.color = color;
        this.shape = shape;
        this.userId = userId;
        this.description = description;
    }
    
    public Room() {
        // Default constructor
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getShape() { return shape; }
    public void setShape(String shape) { this.shape = shape; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // Helper method to convert color string to Color object
    public Color getColorObject() {
        try {
            return Color.decode(color);
        } catch (NumberFormatException e) {
            return Color.WHITE; // Default color
        }
    }
    
    @Override
    public String toString() {
        return name + " (" + shape + ")";
    }
}