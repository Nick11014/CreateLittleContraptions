package com.createlittlecontraptions.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Utility class for detecting LittleTiles block entities.
 * Uses multiple detection strategies including reflection and NBT inspection
 * for maximum compatibility across versions.
 * 
 * Based on proven detection logic from the original debug command.
 */
public class LittleTilesDetector {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Cache the class detection results for performance
    private static Boolean littleTilesAvailable = null;
    private static Class<?> littleTilesBlockEntityClass = null;
      /**
     * Check if LittleTiles mod is loaded and this BlockEntity is from LittleTiles.
     * Uses multiple detection strategies for maximum reliability.
     */
    public static boolean isLittleTilesBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null) return false;
        
        // Strategy 1: Check NBT data for LittleTiles ID
        if (isLittleTilesByNBT(blockEntity)) {
            return true;
        }
        
        // Strategy 2: Check block class name
        if (isLittleTilesByBlockClass(blockEntity)) {
            return true;
        }
        
        // Strategy 3: Check BlockEntity class (original method)
        if (isLittleTilesByEntityClass(blockEntity)) {
            return true;
        }
        
        return false;
    }
      /**
     * Strategy 1: Check BlockEntity NBT data for LittleTiles ID.
     * This is based on the proven logic from ContraptionDebugCommand.
     */
    private static boolean isLittleTilesByNBT(BlockEntity blockEntity) {
        try {
            CompoundTag nbt = blockEntity.saveWithId(blockEntity.getLevel().registryAccess());
            if (nbt != null) {
                String id = nbt.getString("id");
                return id != null && id.toLowerCase().contains("littletiles");
            }
        } catch (Exception e) {
            LOGGER.debug("Error checking LittleTiles by NBT: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Strategy 2: Check if the BlockEntity's block class contains "littletiles".
     * This matches the logic from the debug command's isLittleTilesBlock method.
     */
    private static boolean isLittleTilesByBlockClass(BlockEntity blockEntity) {
        try {
            BlockState blockState = blockEntity.getBlockState();
            if (blockState != null) {
                Block block = blockState.getBlock();
                if (block != null) {
                    String blockClassName = block.getClass().getName().toLowerCase();
                    return blockClassName.contains("littletiles");
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error checking LittleTiles by block class: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Strategy 3: Check BlockEntity class (original implementation).
     */
    private static boolean isLittleTilesByEntityClass(BlockEntity blockEntity) {
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
     * Check if block data from contraption contains LittleTiles.
     * This method works with the block data structure from contraptions,
     * similar to the debug command's logic.
     */
    public static boolean isLittleTilesBlockData(Object blockData) {
        try {
            // Try to get the block state from the block data
            Object blockState = null;
            
            // Try different field/method names that might contain the BlockState
            String[] accessors = {"state", "getState", "blockState", "getBlockState"};
            
            for (String accessor : accessors) {
                try {
                    if (accessor.startsWith("get")) {
                        Method method = blockData.getClass().getMethod(accessor);
                        blockState = method.invoke(blockData);
                    } else {
                        Field field = blockData.getClass().getDeclaredField(accessor);
                        field.setAccessible(true);
                        blockState = field.get(blockData);
                    }
                    
                    if (blockState != null) break;
                } catch (Exception ignored) {
                    // Try next accessor
                }
            }
            
            if (blockState != null) {
                // Try to get the block from the BlockState
                Object block = null;
                try {
                    Method getBlockMethod = blockState.getClass().getMethod("getBlock");
                    block = getBlockMethod.invoke(blockState);
                } catch (Exception ignored) {}
                
                if (block != null) {
                    String blockName = block.getClass().getName().toLowerCase();
                    return blockName.contains("littletiles");
                }
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error checking if block data is LittleTiles: {}", e.getMessage());
        }        
        return false;
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
                "team.creative.littletiles.common.blockentity.LittleTilesBlockEntity", 
                "team.creative.littletiles.common.be.LittleTilesBlockEntity",
                "team.creative.littletiles.LittleTilesBlockEntity",
                "com.creativemd.littletiles.common.tile.LittleTileTE",
                "com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles",
                "com.creativemd.littletiles.common.blockentity.LittleTilesBlockEntity"
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
            
            // Alternative strategy: search for any class containing "littletiles" in its package name
            // and implementing BlockEntity interface
            try {
                // This is a more general approach - try to find any class that:
                // 1. Has "littletiles" in its package name
                // 2. Implements BlockEntity
                LOGGER.debug("Trying alternative LittleTiles detection strategy...");
                
                // Since we can't easily enumerate all classes at runtime, 
                // we'll rely on the NBT and block class strategies instead
                littleTilesAvailable = true; // Allow other strategies to work
                LOGGER.info("LittleTiles detection enabled using alternative strategies (NBT and block class checking)");
                return;
                
            } catch (Exception altEx) {
                LOGGER.debug("Alternative detection strategy failed: {}", altEx.getMessage());
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
