package com.furnituredesigner.client.util;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * Utility class to create skyboxes with fallback options
 */
public class SkyBoxCreator {
    
    /**
     * Creates a skybox with fallback to a colored background if texture loading fails
     * 
     * @param assetManager The JME3 asset manager
     * @param rootNode The root scene node to attach the skybox to
     * @param viewPort The viewport for fallback color
     * @return true if skybox was created, false if fallback was used
     */
    public static boolean createSkyBox(AssetManager assetManager, Node rootNode, ViewPort viewPort) {
        // Try both uppercase and lowercase versions of texture paths
        String[][] possibleTextures = {
            // First try lowercase versions (common in resources folders)
            {"textures/Sky/Bright/BrightSky.dds", "textures/Sky/SimpleSky.jpg", "textures/BlueSky.png"},
            
        };
        
        // Try each texture path
        for (String[] pathVariants : possibleTextures) {
            for (String texturePath : pathVariants) {
                try {
                    System.out.println("Attempting to load skybox texture: " + texturePath);
                    Texture skyTexture = assetManager.loadTexture(texturePath);
                    if (skyTexture != null) {
                        Spatial sky = SkyFactory.createSky(assetManager, skyTexture, SkyFactory.EnvMapType.CubeMap);
                        rootNode.attachChild(sky);
                        System.out.println("Created skybox using texture: " + texturePath);
                        return true;
                    }
                } catch (Exception e) {
                    // Just try the next texture
                    System.out.println("Skybox texture not found: " + texturePath);
                }
            }
        }
        
        // If all texture loading attempts fail, create a simple colored background
        System.out.println("Using colored background as skybox fallback");
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f)); // Light blue color
        return false;
    }
}