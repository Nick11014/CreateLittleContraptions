package com.createlittlecontraptions.utils;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

// Direct import for LittleTiles BETiles class (compileOnly dependency)
import team.creative.littletiles.common.block.entity.BETiles;

/**
 * Utility class for detecting and working with LittleTiles block entities
 */
public class LittleTilesHelper {
    private static final Logger LOGGER = LogUtils.getLogger();    /**
     * Check if a BlockEntity is from LittleTiles mod using type-safe instanceof check
     * Updated to use direct instanceof as recommended by Gemini AI
     * @param be The BlockEntity to check
     * @return true if it's a LittleTiles block entity
     */
    public static boolean isLittleTilesBlockEntity(BlockEntity be) {
        if (be == null) return false;
        
        // Type-safe instanceof check using compileOnly dependency
        boolean isLittleTilesBE = be instanceof BETiles;
        
        if (isLittleTilesBE) {
            LOGGER.debug("Detected LittleTiles BlockEntity (BETiles): {} at {}", 
                be.getClass().getSimpleName(), be.getBlockPos());
        }
        
        return isLittleTilesBE;
    }
    
    /**
     * Get a detailed description of a BlockEntity for debugging
     * @param be The BlockEntity to describe
     * @return A descriptive string
     */
    public static String describeBlockEntity(BlockEntity be) {
        if (be == null) return "null";
        
        return String.format("%s at %s (type: %s)", 
            be.getClass().getSimpleName(), 
            be.getBlockPos(), 
            be.getType().toString());
    }
}
