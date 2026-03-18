package com.furnituredesigner.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private int progress = 0;
    private final Timer timer;
    private final SplashScreenListener listener;

    public interface SplashScreenListener {
        void onSplashScreenComplete();
    }

    public SplashScreen(SplashScreenListener listener) {
        this.listener = listener;
        setupUI();
        
        // Timer to simulate loading progress
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 1;
                progressBar.setValue(progress);
                
                if (progress <= 20) {
                    statusLabel.setText("Initializing application...");
                } else if (progress <= 50) {
                    statusLabel.setText("Loading resources...");
                } else if (progress <= 80) {
                    statusLabel.setText("Checking database...");
                } else {
                    statusLabel.setText("Starting RoomForge...");
                }
                
                if (progress >= 100) {
                    timer.stop();
                    fadeOut();
                }
            }
        });
    }
    
    private void setupUI() {
        setSize(500, 300);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(40, 44, 52));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("RoomForge");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Smart Furniture Preview");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        logoPanel.add(titleLabel, BorderLayout.CENTER);
        logoPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Progress panel
        JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
        progressPanel.setOpaque(false);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(new Color(41, 121, 255));
        progressBar.setBackground(new Color(60, 63, 65));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 5));
        
        statusLabel = new JLabel("Initializing application...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));
        
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add components to main panel
        mainPanel.add(logoPanel, BorderLayout.CENTER);
        mainPanel.add(progressPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Make the window shape rounded
        setShape(new RoundRectangle2D.Double(0, 0, 500, 300, 20, 20));
    }
    
    public void start() {
        setVisible(true);
        timer.start();
    }
    
    private void fadeOut() {
        // Using a simpler approach for fade-out effect
        Timer fadeTimer = new Timer(20, null);
        final float[] opacity = {1.0f};
        
        fadeTimer.addActionListener(e -> {
            opacity[0] -= 0.05f;
            if (opacity[0] <= 0) {
                fadeTimer.stop();
                dispose();
                if (listener != null) {
                    listener.onSplashScreenComplete();
                }
            }
            setOpacity(Math.max(0, opacity[0]));
        });
        
        fadeTimer.start();
    }
    
    public void setOpacity(float opacity) {
        if (opacity < 0.0f || opacity > 1.0f) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
        
        try {
            // Use the AWTUtilities class for setting window opacity if available
            Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
            java.lang.reflect.Method setWindowOpacityMethod = 
                awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
            setWindowOpacityMethod.invoke(null, this, opacity);
        } catch (Exception e) {
            // Fall back to a simpler approach if AWTUtilities is not available
            // Note: This won't work on all platforms
            try {
                setBackground(new Color(0, 0, 0, (int)(opacity * 255)));
            } catch (Exception ex) {
                System.err.println("Failed to set opacity: " + ex.getMessage());
            }
        }
    }
}