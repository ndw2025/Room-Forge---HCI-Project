package com.furnituredesigner.client.ui;

import com.furnituredesigner.common.model.Room;
import com.furnituredesigner.common.model.Furniture;
import com.furnituredesigner.server.service.FurnitureService;
import com.furnituredesigner.server.service.TemplateService;
import com.furnituredesigner.common.model.Template;
import java.util.Date;
import java.util.Date;
import com.furnituredesigner.common.model.Template;
import com.furnituredesigner.server.service.TemplateService;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwoDViewPanel extends JPanel {
    
    private Room room;
    private JPanel drawingPanel;
    private double scale = 50.0; // 50 pixels per meter
    private Point dragStart;
    private Point viewOffset = new Point(0, 0);
    
    // Furniture management
    private List<Furniture> furnitureList = new ArrayList<>();
    private Furniture selectedFurniture = null;
    private Furniture draggedFurniture = null;
    private Point dragOffset = new Point(0, 0);
    private boolean isResizing = false;
    private boolean isRotating = false;
    private boolean isDraggingView = false;
    private Point lastMousePos = null;
    
    // Predefined furniture types
    private Map<String, Dimension> furniturePresets = new HashMap<>();
    private JPanel furnitureToolbar;
    private String selectedFurnitureType = null;
    
    public TwoDViewPanel(Room room) {
        this.room = room;
        setupFurniturePresets();
        setLayout(new BorderLayout());
        
        // Load existing furniture for this room
        loadFurniture();
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create drawing panel
        drawingPanel = createDrawingPanel();
        
        // Create toolbar for furniture selection
        furnitureToolbar = createFurnitureToolbar();
        
        // Create info panel
        JPanel infoPanel = createInfoPanel();
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(furnitureToolbar, BorderLayout.WEST);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupFurniturePresets() {
        // Define standard sizes for furniture in meters (width, length)
        furniturePresets.put("Chair", new Dimension(50, 50));
        furniturePresets.put("Dining Table", new Dimension(120, 80));
        furniturePresets.put("Side Table", new Dimension(45, 45));
        furniturePresets.put("Sofa", new Dimension(200, 85));
        furniturePresets.put("Bed", new Dimension(160, 200));
        furniturePresets.put("Cupboard", new Dimension(100, 50));
        furniturePresets.put("TV Stand", new Dimension(120, 45));
        furniturePresets.put("Bookshelf", new Dimension(90, 30));
    }
    
    private void loadFurniture() {
        try {
            FurnitureService furnitureService = new FurnitureService();
            furnitureList = furnitureService.getFurnitureByRoomId(room.getId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load furniture: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("2D Room View: " + room.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);
        
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        JButton resetViewButton = new JButton("Reset View");
        JButton saveAsTemplateButton = new JButton("Save as Template");  // Changed from saveFurnitureButton
        JButton threeDViewButton = new JButton("3D Preview");
        
        // Button action listeners
        zoomInButton.addActionListener(e -> {
            scale *= 1.2;
            drawingPanel.repaint();
        });
        
        zoomOutButton.addActionListener(e -> {
            scale /= 1.2;
            drawingPanel.repaint();
        });
        
        resetViewButton.addActionListener(e -> {
            scale = 50.0;
            viewOffset = new Point(0, 0);
            drawingPanel.repaint();
        });
        
        // Update this listener to call the saveAsTemplate method
        saveAsTemplateButton.addActionListener(e -> {
            saveAsTemplate();
        });
        
        threeDViewButton.addActionListener(e -> {
            showThreeDPreview();
        });
        
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(resetViewButton);
        controlPanel.add(saveAsTemplateButton);
        controlPanel.add(threeDViewButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createDrawingPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Enable anti-aliasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw room
                drawRoom(g2d);
                
                // Draw furniture
                drawFurniture(g2d);
                
                // Draw selection indicator if a furniture is selected
                if (selectedFurniture != null) {
                    drawSelectionHandles(g2d, selectedFurniture);
                }
            }
        };
        
        panel.setBackground(Color.WHITE);
        
        // Mouse listeners for furniture manipulation
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                
                // Check if we're clicking on a selection handle
                if (selectedFurniture != null) {
                    Rectangle2D bounds = getFurnitureBounds(selectedFurniture);
                    
                    // Check if clicking on resize handle (bottom-right corner)
                    Rectangle resizeHandle = new Rectangle(
                        (int)(bounds.getMaxX() - 10), 
                        (int)(bounds.getMaxY() - 10), 
                        20, 20);
                    
                    // Check if clicking on rotation handle (top-right corner)
                    Rectangle rotateHandle = new Rectangle(
                        (int)(bounds.getMaxX() - 10), 
                        (int)(bounds.getMinY() - 10), 
                        20, 20);
                    
                    if (resizeHandle.contains(e.getPoint())) {
                        isResizing = true;
                        return;
                    } else if (rotateHandle.contains(e.getPoint())) {
                        isRotating = true;
                        return;
                    }
                }
                
                // Check if we're clicking on existing furniture
                Furniture clickedFurniture = getFurnitureAt(e.getPoint());
                if (clickedFurniture != null) {
                    selectedFurniture = clickedFurniture;
                    draggedFurniture = clickedFurniture;
                    
                    // Calculate offset for precise dragging
                    Point2D furnitureCenter = getFurnitureCenter(clickedFurniture);
                    dragOffset.x = (int)(e.getX() - furnitureCenter.getX());
                    dragOffset.y = (int)(e.getY() - furnitureCenter.getY());
                } else if (selectedFurnitureType != null) {
                    // Add new furniture at click point
                    addNewFurniture(e.getPoint());
                } else {
                    // Start panning the view
                    dragStart = e.getPoint();
                    isDraggingView = true;
                    selectedFurniture = null;
                }
                
                panel.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                draggedFurniture = null;
                isResizing = false;
                isRotating = false;
                isDraggingView = false;
                lastMousePos = null;
                panel.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selectedFurniture != null) {
                    // Double-click to edit furniture properties
                    showFurniturePropertyDialog(selectedFurniture);
                    panel.repaint();
                }
            }
        });
        
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos == null) {
                    lastMousePos = e.getPoint();
                    return;
                }
                
                if (isResizing && selectedFurniture != null) {
                    // Calculate new size based on drag distance
                    double dx = (e.getX() - lastMousePos.x) / scale;
                    double dy = (e.getY() - lastMousePos.y) / scale;
                    
                    selectedFurniture.setWidth(Math.max(0.3, selectedFurniture.getWidth() + dx));
                    selectedFurniture.setLength(Math.max(0.3, selectedFurniture.getLength() + dy));
                    
                    lastMousePos = e.getPoint();
                    panel.repaint();
                } else if (isRotating && selectedFurniture != null) {
                    // Calculate rotation angle
                    Point2D center = getFurnitureCenter(selectedFurniture);
                    double angle1 = Math.atan2(lastMousePos.y - center.getY(), lastMousePos.x - center.getX());
                    double angle2 = Math.atan2(e.getY() - center.getY(), e.getX() - center.getX());
                    double deltaAngle = Math.toDegrees(angle2 - angle1);
                    
                    selectedFurniture.setRotation((selectedFurniture.getRotation() + deltaAngle) % 360);
                    
                    lastMousePos = e.getPoint();
                    panel.repaint();
                } else if (draggedFurniture != null) {
                    // Move furniture
                    double roomCenterX = panel.getWidth() / 2 + viewOffset.x;
                    double roomCenterY = panel.getHeight() / 2 + viewOffset.y;
                    
                    double newXPos = (e.getX() - dragOffset.x - roomCenterX) / scale;
                    double newYPos = (e.getY() - dragOffset.y - roomCenterY) / scale;
                    
                    // Constrain to room boundaries
                    if (isWithinRoom(newXPos, newYPos, draggedFurniture.getWidth(), draggedFurniture.getLength())) {
                        draggedFurniture.setXPos(newXPos);
                        draggedFurniture.setYPos(newYPos);
                    }
                    
                    lastMousePos = e.getPoint();
                    panel.repaint();
                } else if (isDraggingView) {
                    // Pan the view
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    viewOffset.x += dx;
                    viewOffset.y += dy;
                    lastMousePos = e.getPoint();
                    panel.repaint();
                }
            }
        });
        
        // Add mouse wheel support for zooming
        panel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                // Zoom in
                scale *= 1.1;
            } else {
                // Zoom out
                scale /= 1.1;
            }
            panel.repaint();
        });
        
        // Add context menu for furniture
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem rotateItem = new JMenuItem("Rotate 90°");
        JMenuItem colorItem = new JMenuItem("Change Color");
        
        deleteItem.addActionListener(e -> {
            if (selectedFurniture != null) {
                try {
                    FurnitureService service = new FurnitureService();
                    if (selectedFurniture.getId() > 0 && service.deleteFurniture(selectedFurniture.getId())) {
                        furnitureList.remove(selectedFurniture);
                        JOptionPane.showMessageDialog(panel, "Furniture deleted");
                    } else if (selectedFurniture.getId() == 0) {
                        // For unsaved furniture
                        furnitureList.remove(selectedFurniture);
                    }
                    selectedFurniture = null;
                    panel.repaint();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, 
                        "Failed to delete furniture: " + ex.getMessage(), 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        rotateItem.addActionListener(e -> {
            if (selectedFurniture != null) {
                selectedFurniture.setRotation((selectedFurniture.getRotation() + 90) % 360);
                panel.repaint();
            }
        });
        
        colorItem.addActionListener(e -> {
            if (selectedFurniture != null) {
                Color currentColor = selectedFurniture.getColorObject();
                Color newColor = JColorChooser.showDialog(panel, "Choose Furniture Color", currentColor);
                if (newColor != null) {
                    String colorHex = String.format("#%02x%02x%02x", 
                        newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                    selectedFurniture.setColor(colorHex);
                    panel.repaint();
                }
            }
        });
        
        popupMenu.add(rotateItem);
        popupMenu.add(colorItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);
        
        panel.setComponentPopupMenu(popupMenu);
        
        return panel;
    }
    
    private JPanel createFurnitureToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBorder(BorderFactory.createTitledBorder("Furniture"));
        toolbar.setPreferredSize(new Dimension(120, 100));
        
        JLabel instructionLabel = new JLabel("<html>Select and click<br>to place:</html>");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbar.add(instructionLabel);
        toolbar.add(Box.createVerticalStrut(10));
        
        ButtonGroup group = new ButtonGroup();
        
        // Add buttons for each furniture type
        for (String type : furniturePresets.keySet()) {
            JToggleButton button = new JToggleButton(type);
            button.setMaximumSize(new Dimension(110, 30));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            button.addActionListener(e -> {
                selectedFurnitureType = type;
                selectedFurniture = null; // Deselect current furniture when selecting from toolbar
            });
            
            group.add(button);
            toolbar.add(button);
            toolbar.add(Box.createVerticalStrut(5));
        }
        
        // Add button to cancel selection
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(110, 30));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> {
            selectedFurnitureType = null;
            group.clearSelection();
        });
        
        toolbar.add(Box.createVerticalStrut(10));
        toolbar.add(cancelButton);
        
        toolbar.add(Box.createVerticalGlue());
        
        return toolbar;
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        infoPanel.setBackground(new Color(245, 245, 245));
        
        infoPanel.add(new JLabel("Shape: " + room.getShape()));
        if ("Rectangle".equals(room.getShape()) || "Square".equals(room.getShape())) {
            infoPanel.add(new JLabel(String.format("Width: %.2f m", room.getWidth())));
            infoPanel.add(new JLabel(String.format("Length: %.2f m", room.getLength())));
        } else {
            infoPanel.add(new JLabel(String.format("Diameter: %.2f m", room.getWidth())));
        }
        infoPanel.add(new JLabel(String.format("Height: %.2f m", room.getHeight())));
        
        return infoPanel;
    }
    
    private void drawRoom(Graphics2D g2d) {
        // Calculate center of panel
        int centerX = drawingPanel.getWidth() / 2 + viewOffset.x;
        int centerY = drawingPanel.getHeight() / 2 + viewOffset.y;
        
        // Set color based on room color
        Color roomColor = room.getColorObject();
        Color wallColor = roomColor.darker();
        
        // Draw room based on shape
        if ("Circular".equals(room.getShape())) {
            // Draw circular room
            int radius = (int) (room.getWidth() * scale / 2);
            
            // Draw floor
            g2d.setColor(roomColor);
            g2d.fill(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
            
            // Draw walls (outline)
            g2d.setColor(wallColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
            
        } else {
            // Draw rectangular or square room
            int width = (int) (room.getWidth() * scale);
            int length = (int) (room.getLength() * scale);
            
            // Draw floor
            g2d.setColor(roomColor);
            g2d.fill(new Rectangle2D.Double(centerX - width / 2, centerY - length / 2, width, length));
            
            // Draw walls (outline)
            g2d.setColor(wallColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new Rectangle2D.Double(centerX - width / 2, centerY - length / 2, width, length));
        }
        
        // Draw coordinate system
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        
        // Draw horizontal gridlines every meter
        for (int i = -20; i <= 20; i++) {
            int y = centerY + (int)(i * scale);
            g2d.drawLine(0, y, drawingPanel.getWidth(), y);
        }
        
        // Draw vertical gridlines every meter
        for (int i = -20; i <= 20; i++) {
            int x = centerX + (int)(i * scale);
            g2d.drawLine(x, 0, x, drawingPanel.getHeight());
        }
        
        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(centerX, 0, centerX, drawingPanel.getHeight()); // Y axis
        g2d.drawLine(0, centerY, drawingPanel.getWidth(), centerY); // X axis
        
        // Draw legend
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Scale: 1m = " + (int)scale + "px", 20, 20);
    }
    
    private void drawFurniture(Graphics2D g2d) {
        for (Furniture f : furnitureList) {
            drawSingleFurniture(g2d, f);
        }
    }
    
    private void drawSingleFurniture(Graphics2D g2d, Furniture furniture) {
        // Calculate center of panel
        int centerX = drawingPanel.getWidth() / 2 + viewOffset.x;
        int centerY = drawingPanel.getHeight() / 2 + viewOffset.y;
        
        // Calculate furniture position
        int x = centerX + (int)(furniture.getXPos() * scale);
        int y = centerY + (int)(furniture.getYPos() * scale);
        int width = (int)(furniture.getWidth() * scale);
        int length = (int)(furniture.getLength() * scale);
        
        // Save the original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Apply translation and rotation
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(furniture.getRotation()));
        
        // Get furniture color
        Color furnitureColor = furniture.getColorObject();
        
        // Draw furniture based on type
        g2d.setColor(furnitureColor);
        
        switch (furniture.getType()) {
            case "Chair":
                drawChair(g2d, width, length, furnitureColor);
                break;
            case "Dining Table":
                drawDiningTable(g2d, width, length, furnitureColor);
                break;
            case "Side Table":
                drawSideTable(g2d, width, length, furnitureColor);
                break;
            case "Sofa":
                drawSofa(g2d, width, length, furnitureColor);
                break;
            case "Bed":
                drawBed(g2d, width, length, furnitureColor);
                break;
            case "Cupboard":
                drawCupboard(g2d, width, length, furnitureColor);
                break;
            case "TV Stand":
                drawTVStand(g2d, width, length, furnitureColor);
                break;
            case "Bookshelf":
                drawBookshelf(g2d, width, length, furnitureColor);
                break;
            default:
                // Draw a generic rectangle for unknown types
                g2d.fillRect(-width / 2, -length / 2, width, length);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(-width / 2, -length / 2, width, length);
        }
        
        // Draw type label
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String label = furniture.getType();
        int textWidth = fm.stringWidth(label);
        g2d.drawString(label, -textWidth / 2, 0);
        
        // Restore the original transform
        g2d.setTransform(originalTransform);
    }
    
    private void drawChair(Graphics2D g2d, int width, int length, Color color) {
        // Draw seat
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw backrest
        g2d.setColor(color.darker());
        g2d.fillRect(-width / 2, -length / 2 - length/3, width, length/3);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2 - length/3, width, length/3);
    }
    
    private void drawDiningTable(Graphics2D g2d, int width, int length, Color color) {
        // Draw table top
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw table legs (small rectangles in corners)
        int legSize = Math.min(width, length) / 10;
        g2d.setColor(color.darker());
        g2d.fillRect(-width / 2, -length / 2, legSize, legSize); // Top-left
        g2d.fillRect(width / 2 - legSize, -length / 2, legSize, legSize); // Top-right
        g2d.fillRect(-width / 2, length / 2 - legSize, legSize, legSize); // Bottom-left
        g2d.fillRect(width / 2 - legSize, length / 2 - legSize, legSize, legSize); // Bottom-right
    }
    
    private void drawSideTable(Graphics2D g2d, int width, int length, Color color) {
        // Similar to dining table but smaller
        drawDiningTable(g2d, width, length, color);
    }
    
    private void drawSofa(Graphics2D g2d, int width, int length, Color color) {
        // Draw base
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw backrest
        int backThickness = length / 3;
        g2d.setColor(color.darker());
        g2d.fillRect(-width / 2, -length / 2 - backThickness, width, backThickness);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2 - backThickness, width, backThickness);
        
        // Draw armrests
        int armWidth = width / 10;
        g2d.setColor(color.darker());
        g2d.fillRect(-width / 2 - armWidth, -length / 2, armWidth, length); // Left arm
        g2d.fillRect(width / 2, -length / 2, armWidth, length); // Right arm
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2 - armWidth, -length / 2, armWidth, length); // Left arm
        g2d.drawRect(width / 2, -length / 2, armWidth, length); // Right arm
    }
    
    private void drawBed(Graphics2D g2d, int width, int length, Color color) {
        // Draw mattress
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw headboard
        int headboardHeight = length / 6;
        g2d.setColor(color.darker());
        g2d.fillRect(-width / 2, -length / 2 - headboardHeight, width, headboardHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2 - headboardHeight, width, headboardHeight);
    }
    
    private void drawCupboard(Graphics2D g2d, int width, int length, Color color) {
        // Draw main body
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw doors (divide into two vertically)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(0, -length / 2, 0, length / 2); // Divider
        
        // Draw handles
        g2d.setColor(Color.DARK_GRAY);
        int handleSize = Math.min(width, length) / 15;
        g2d.fillRect(-width / 4, 0 - handleSize/2, handleSize, handleSize); // Left door handle
        g2d.fillRect(width / 4 - handleSize, 0 - handleSize/2, handleSize, handleSize); // Right door handle
    }
    
    private void drawTVStand(Graphics2D g2d, int width, int length, Color color) {
        // Draw main body
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw shelves/divisions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(-width / 2, 0, width / 2, 0); // Middle shelf
    }
    
    private void drawBookshelf(Graphics2D g2d, int width, int length, Color color) {
        // Draw main body
        g2d.fillRect(-width / 2, -length / 2, width, length);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-width / 2, -length / 2, width, length);
        
        // Draw shelves
        g2d.setColor(Color.LIGHT_GRAY);
        int numShelves = 4;
        int shelfSpacing = length / (numShelves + 1);
        for (int i = 1; i <= numShelves; i++) {
            int y = -length / 2 + i * shelfSpacing;
            g2d.drawLine(-width / 2, y, width / 2, y);
        }
    }
    
    private void drawSelectionHandles(Graphics2D g2d, Furniture furniture) {
        Rectangle2D bounds = getFurnitureBounds(furniture);
        
        // Draw selection rectangle
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2d.draw(bounds);
        
        // Draw resize handle (bottom-right corner)
        g2d.setColor(Color.RED);
        g2d.fillRect((int)bounds.getMaxX() - 10, (int)bounds.getMaxY() - 10, 20, 20);
        
        // Draw rotation handle (top-right corner)
        g2d.setColor(Color.GREEN);
        g2d.fillRect((int)bounds.getMaxX() - 10, (int)bounds.getMinY() - 10, 20, 20);
    }
    
    private Rectangle2D getFurnitureBounds(Furniture furniture) {
        // Calculate center of panel
        int centerX = drawingPanel.getWidth() / 2 + viewOffset.x;
        int centerY = drawingPanel.getHeight() / 2 + viewOffset.y;
        
        // Calculate furniture position and size
        int x = centerX + (int)(furniture.getXPos() * scale);
        int y = centerY + (int)(furniture.getYPos() * scale);
        int width = (int)(furniture.getWidth() * scale);
        int length = (int)(furniture.getLength() * scale);
        
        // Create bounds (needs to account for rotation)
        // For simplicity, we'll use a larger bounding box that encompasses the rotated furniture
        double diagonal = Math.sqrt(width * width + length * length);
        return new Rectangle2D.Double(x - diagonal/2, y - diagonal/2, diagonal, diagonal);
    }
    
    private Point2D getFurnitureCenter(Furniture furniture) {
        // Calculate center of panel
        int centerX = drawingPanel.getWidth() / 2 + viewOffset.x;
        int centerY = drawingPanel.getHeight() / 2 + viewOffset.y;
        
        // Calculate furniture position
        int x = centerX + (int)(furniture.getXPos() * scale);
        int y = centerY + (int)(furniture.getYPos() * scale);
        
        return new Point2D.Double(x, y);
    }
    
    private Furniture getFurnitureAt(Point point) {
        // Check if point is within any furniture
        for (int i = furnitureList.size() - 1; i >= 0; i--) {
            Furniture furniture = furnitureList.get(i);
            
            // Calculate center of panel
            int centerX = drawingPanel.getWidth() / 2 + viewOffset.x;
            int centerY = drawingPanel.getHeight() / 2 + viewOffset.y;
            
            // Calculate furniture position and size
            int x = centerX + (int)(furniture.getXPos() * scale);
            int y = centerY + (int)(furniture.getYPos() * scale);
            int width = (int)(furniture.getWidth() * scale);
            int length = (int)(furniture.getLength() * scale);
            
            // Create a transform to check if the point is inside the rotated furniture
            AffineTransform transform = new AffineTransform();
            transform.translate(x, y);
            transform.rotate(Math.toRadians(furniture.getRotation()));
            
            // Create shape for the furniture
            Rectangle2D rect = new Rectangle2D.Double(-width/2, -length/2, width, length);
            Shape shape = transform.createTransformedShape(rect);
            
            if (shape.contains(point)) {
                return furniture;
            }
        }
        
        return null;
    }
    
    private boolean isWithinRoom(double x, double y, double width, double length) {
        // Check if furniture is within room boundaries
        double halfWidth = width / 2;
        double halfLength = length / 2;
        
        if ("Circular".equals(room.getShape())) {
            // For circular room, check if the furniture's corners are within the circle
            double roomRadius = room.getWidth() / 2;
            double distanceSquared = x * x + y * y;
            double cornerDistanceSquared = Math.max(
                Math.sqrt((x + halfWidth) * (x + halfWidth) + (y + halfLength) * (y + halfLength)),
                Math.max(
                    Math.sqrt((x + halfWidth) * (x + halfWidth) + (y - halfLength) * (y - halfLength)),
                    Math.max(
                        Math.sqrt((x - halfWidth) * (x - halfWidth) + (y + halfLength) * (y + halfLength)),
                        Math.sqrt((x - halfWidth) * (x - halfWidth) + (y - halfLength) * (y - halfLength))
                    )
                )
            );
            
            return cornerDistanceSquared <= roomRadius;
        } else {
            // For rectangular room, check if furniture is within the rectangle
            double roomHalfWidth = room.getWidth() / 2;
            double roomHalfLength = room.getLength() / 2;
            
            return (x - halfWidth >= -roomHalfWidth && 
                    x + halfWidth <= roomHalfWidth && 
                    y - halfLength >= -roomHalfLength && 
                    y + halfLength <= roomHalfLength);
        }
    }
    
    private void addNewFurniture(Point clickPoint) {
        // Calculate center of panel
        int centerX = drawingPanel.getWidth() / 2 + viewOffset.x;
        int centerY = drawingPanel.getHeight() / 2 + viewOffset.y;
        
        // Calculate position in room coordinates
        double xPos = (clickPoint.x - centerX) / scale;
        double yPos = (clickPoint.y - centerY) / scale;
        
        // Get default size for selected furniture type
        Dimension preset = furniturePresets.get(selectedFurnitureType);
        double width = preset.width / 100.0;  // Convert from cm to meters
        double length = preset.height / 100.0; // Convert from cm to meters
        
        // Create new furniture
        Furniture newFurniture = new Furniture();
        newFurniture.setRoomId(room.getId());
        newFurniture.setType(selectedFurnitureType);
        newFurniture.setXPos(xPos);
        newFurniture.setYPos(yPos);
        newFurniture.setWidth(width);
        newFurniture.setLength(length);
        newFurniture.setHeight(0.75); // Default height
        newFurniture.setColor("#CCCCCC"); // Default color
        newFurniture.setRotation(0);
        
        // Check if furniture is within room
        if (isWithinRoom(xPos, yPos, width, length)) {
            try {
                // Add to database
                FurnitureService service = new FurnitureService();
                Furniture savedFurniture = service.addFurniture(newFurniture);
                
                if (savedFurniture != null) {
                    furnitureList.add(savedFurniture);
                    selectedFurniture = savedFurniture;
                    
                    // Clear selection type to prevent adding multiple furniture with one click
                    selectedFurnitureType = null;
                    
                    // If the furniture toolbar has toggle buttons, clear the selection
                    Component[] components = furnitureToolbar.getComponents();
                    for (Component c : components) {
                        if (c instanceof JToggleButton) {
                            ((JToggleButton) c).setSelected(false);
                        }
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(drawingPanel, 
                    "Failed to add furniture: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(drawingPanel, 
                "Cannot place furniture outside room boundaries", 
                "Placement Error", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showFurniturePropertyDialog(Furniture furniture) {
        // Create a dialog to edit furniture properties
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, "Edit Furniture", true);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, "Edit Furniture", true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Edit Furniture");
            dialog.setModal(true);
        }
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Type (non-editable)
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField typeField = new JTextField(furniture.getType());
        typeField.setEditable(false);
        panel.add(typeField, gbc);
        
        // Width
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Width (m):"), gbc);
        gbc.gridx = 1;
        JTextField widthField = new JTextField(String.valueOf(furniture.getWidth()));
        panel.add(widthField, gbc);
        
        // Length
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Length (m):"), gbc);
        gbc.gridx = 1;
        JTextField lengthField = new JTextField(String.valueOf(furniture.getLength()));
        panel.add(lengthField, gbc);
        
        // Height
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Height (m):"), gbc);
        gbc.gridx = 1;
        JTextField heightField = new JTextField(String.valueOf(furniture.getHeight()));
        panel.add(heightField, gbc);
        
        // Rotation
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Rotation (°):"), gbc);
        gbc.gridx = 1;
        JTextField rotationField = new JTextField(String.valueOf(furniture.getRotation()));
        panel.add(rotationField, gbc);
        
        // Color
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        
        JPanel colorPanel = new JPanel(new BorderLayout());
        JPanel colorPreview = new JPanel();
        colorPreview.setBackground(furniture.getColorObject());
        colorPreview.setPreferredSize(new Dimension(30, 30));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JButton colorButton = new JButton("Choose");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(dialog, "Choose Color", colorPreview.getBackground());
            if (newColor != null) {
                colorPreview.setBackground(newColor);
            }
        });
        
        colorPanel.add(colorPreview, BorderLayout.WEST);
        colorPanel.add(colorButton, BorderLayout.CENTER);
        panel.add(colorPanel, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        saveButton.addActionListener(e -> {
            try {
                // Update furniture properties
                furniture.setWidth(Double.parseDouble(widthField.getText()));
                furniture.setLength(Double.parseDouble(lengthField.getText()));
                furniture.setHeight(Double.parseDouble(heightField.getText()));
                furniture.setRotation(Double.parseDouble(rotationField.getText()));
                
                // Update color
                Color newColor = colorPreview.getBackground();
                String colorHex = String.format("#%02x%02x%02x", 
                    newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                furniture.setColor(colorHex);
                
                // Save to database if furniture has an ID
                if (furniture.getId() > 0) {
                    FurnitureService service = new FurnitureService();
                    service.updateFurniture(furniture);
                }
                
                dialog.dispose();
                drawingPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numbers for dimensions", 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Failed to update furniture: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        panel.add(buttonPanel, gbc);
        dialog.add(panel, BorderLayout.CENTER);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void saveFurnitureLayout() {
        try {
            FurnitureService service = new FurnitureService();
            
            // Save each furniture item
            for (Furniture furniture : furnitureList) {
                if (furniture.getId() > 0) {
                    service.updateFurniture(furniture);
                } else {
                    Furniture saved = service.addFurniture(furniture);
                    if (saved != null) {
                        furniture.setId(saved.getId());
                    }
                }
            }
            
            JOptionPane.showMessageDialog(this, 
                "Furniture layout saved successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Failed to save layout: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    public void setRoom(Room room) {
        this.room = room;
        loadFurniture();
        repaint();
    }
    
    private void showThreeDPreview() {
        try {
            // Save furniture layout before switching to 3D
            saveFurnitureLayout();
            
            // Get parent container (should be the content panel in DesignerDashboardPanel)
            Container parent = getParent();
            if (parent != null && parent.getLayout() instanceof CardLayout) {
                CardLayout layout = (CardLayout) parent.getLayout();
                
                // Create or update 3D panel
                ThreeDViewPanel threeDPanel = new ThreeDViewPanel(room, furnitureList);
                parent.add(threeDPanel, "3d-preview");
                
                // Show 3D view
                layout.show(parent, "3d-preview");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Unable to switch to 3D view", 
                    "Navigation Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error showing 3D preview: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void saveAsTemplate() {
        try {
            // First save the furniture layout
            saveFurnitureLayout();
            
            // Show dialog to enter template details
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dialog;
            if (owner instanceof Frame) {
                dialog = new JDialog((Frame) owner, "Save as Template", true);
            } else if (owner instanceof Dialog) {
                dialog = new JDialog((Dialog) owner, "Save as Template", true);
            } else {
                dialog = new JDialog();
                dialog.setTitle("Save as Template");
                dialog.setModal(true);
            }
            dialog.setLayout(new BorderLayout());
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Template title
            panel.add(new JLabel("Template Title:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            JTextField titleField = new JTextField(20);
            titleField.setText(room.getName());
            panel.add(titleField, gbc);
            
            // Room type field
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            panel.add(new JLabel("Room Type:"), gbc);
            
            gbc.gridx = 1;
            JComboBox<String> roomTypeCombo = new JComboBox<>(new String[] {
                "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Dining Room", "Other"
            });
            panel.add(roomTypeCombo, gbc);
            
            // Comments
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Comments:"), gbc);
            
            gbc.gridx = 1;
            JTextArea commentsArea = new JTextArea(5, 20);
            commentsArea.setLineWrap(true);
            commentsArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(commentsArea);
            panel.add(scrollPane, gbc);
            
            // Buttons
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelButton = new JButton("Cancel");
            JButton saveButton = new JButton("Save Template");
            
            cancelButton.addActionListener(evt -> dialog.dispose());
            
            saveButton.addActionListener(evt -> {
                String title = titleField.getText().trim();
                String roomType = (String) roomTypeCombo.getSelectedItem();
                String comments = commentsArea.getText().trim();
                
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Please enter a template title", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    // Create template object
                    Template template = new Template();
                    template.setTitle(title);
                    template.setComments(comments);
                    template.setRoomId(room.getId());
                    template.setUserId(room.getUserId());
                    template.setRoomType(roomType);
                    template.setCreatedAt(new Date());
                    
                    // Save template
                    TemplateService templateService = new TemplateService();
                    Template savedTemplate = templateService.saveTemplate(template);
                    
                    if (savedTemplate != null) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Template saved successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        
                        // NEW CODE: Find the parent DesignerDashboardPanel and refresh templates
                        refreshTemplatesPanel();
                    } else {
                        JOptionPane.showMessageDialog(dialog, 
                            "Failed to save template", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
            
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(buttonPanel, gbc);
            
            dialog.add(panel, BorderLayout.CENTER);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error saving template: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    // Add this new method to refresh the templates panel
    private void refreshTemplatesPanel() {
        // Find the parent DesignerDashboardPanel in the component hierarchy
        Container parent = getParent();
        while (parent != null && !(parent instanceof DesignerDashboardPanel)) {
            parent = parent.getParent();
        }
        
        // If we found the DesignerDashboardPanel, refresh the templates
        if (parent instanceof DesignerDashboardPanel) {
            DesignerDashboardPanel dashboard = (DesignerDashboardPanel) parent;
            dashboard.refreshTemplatesPanel();
            
            // Switch to templates panel to show the newly created template
            dashboard.showTemplatesPanel();
        } else {
            System.out.println("Could not find parent DesignerDashboardPanel to refresh templates");
        }
    }
}