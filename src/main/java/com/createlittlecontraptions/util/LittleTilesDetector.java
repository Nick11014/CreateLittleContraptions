package com.createlittlecontraptions.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Utility class for detecting LittleTiles block entities.
 * Uses reflection to maintain compatibility across versions.
 */
public class LittleTilesDetector {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Cache the class detection results for performance
    private static Boolean littleTilesAvailable = null;
    private static Class<?> littleTilesBlockEntityClass = null;
    
    /**
     * Check if LittleTiles mod is loaded and this BlockEntity is from LittleTiles.
     */
    public static boolean isLittleTilesBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null) return false;
        
        // Lazy initialization of LittleTiles detection
        if (littleTilesAvailable == null) {
            initializeLittleTilesDetection();
        }
        
        if (!littleTilesAvailable) {
            return false;
        }
        
        // Check if this block entity is a LittleTiles block entity
        return littleTilesBlockEntityClass != null && 
               littleTilesBlockEntityClass.isInstance(blockEntity);
    }
    
    /**
     * Initialize LittleTiles detection using reflection.
     */
    private static void initializeLittleTilesDetection() {
        try {
            // Try to find the main LittleTiles block entity class
            // This may vary depending on LittleTiles version
            String[] possibleClasses = {
                "team.creative.littletiles.common.block.entity.LittleTilesBlockEntity",
                "com.creativemd.littletiles.common.tile.LittleTileTE",
                "com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles"
            };
            
            for (String className : possibleClasses) {
                try {
                    littleTilesBlockEntityClass = Class.forName(className);
                    littleTilesAvailable = true;
                    LOGGER.info("Found LittleTiles BlockEntity class: {}", className);
                    return;
                } catch (ClassNotFoundException ignored) {
                    // Try next class name
                }
            }
            
            // If we get here, LittleTiles is not available
            littleTilesAvailable = false;
            LOGGER.info("LittleTiles mod not detected or no compatible BlockEntity class found");
            
        } catch (Exception e) {
            littleTilesAvailable = false;
            LOGGER.warn("Error initializing LittleTiles detection: {}", e.getMessage());
        }
    }
    
    /**
     * Check if LittleTiles mod is available.
     */
    public static boolean isLittleTilesAvailable() {
        if (littleTilesAvailable == null) {
            initializeLittleTilesDetection();
        }
        return littleTilesAvailable;
    }
    
    /**
     * Get the detected LittleTiles BlockEntity class, if any.
     */
    public static Class<?> getLittleTilesBlockEntityClass() {
        if (littleTilesAvailable == null) {
            initializeLittleTilesDetection();
        }
        return littleTilesBlockEntityClass;
    }
}
