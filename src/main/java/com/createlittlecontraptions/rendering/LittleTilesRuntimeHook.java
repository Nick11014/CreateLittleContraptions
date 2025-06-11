package com.createlittlecontraptions.rendering;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.api.distmarker.Dist;

import com.createlittlecontraptions.util.LittleTilesDetector;
import com.createlittlecontraptions.util.ContraptionDetector;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime hook system that monitors LittleTiles blocks in contraptions.
 * This provides an alternative to mixins for detecting and managing LittleTiles rendering.
 */
@EventBusSubscriber(modid = "createlittlecontraptions", value = Dist.CLIENT)
public class LittleTilesRuntimeHook {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Track known LittleTiles blocks in contraptions
    private static final Set<BlockPos> trackedLittleTilesBlocks = ConcurrentHashMap.newKeySet();
    
    // Track contraptions we've analyzed
    private static final Set<AbstractContraptionEntity> analyzedContraptions = ConcurrentHashMap.newKeySet();
    
    private static boolean initialized = false;
    private static int tickCounter = 0;
    
    /**
     * Initialize the runtime hook system.
     */
    public static void initialize() {
        if (!initialized) {
            LOGGER.info("Initializing LittleTiles Runtime Hook system");
            initialized = true;
        }
    }
    
    /**
     * Periodic scan for LittleTiles blocks in contraptions.
     * This runs every few ticks to avoid performance impact.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!initialized || !event.getLevel().isClientSide()) {
            return;
        }
        
        // Only run every 20 ticks (once per second) to minimize performance impact
        if (++tickCounter % 20 != 0) {
            return;
        }
        
        try {
            Level level = event.getLevel();
            scanForLittleTilesInContraptions(level);
            
        } catch (Exception e) {
            LOGGER.debug("Error during LittleTiles contraption scan: {}", e.getMessage());
        }
    }
    
    /**
     * Scan the current level for contraptions containing LittleTiles blocks.
     */
    private static void scanForLittleTilesInContraptions(Level level) {
        if (!LittleTilesDetector.isLittleTilesAvailable()) {
            return;
        }
          // Find all contraption entities in the level
        // Create a large AABB to search for contraptions
        var searchArea = new net.minecraft.world.phys.AABB(
            -1000, -100, -1000, 1000, 400, 1000); // Large search area
        
        level.getEntitiesOfClass(AbstractContraptionEntity.class, searchArea)
            .stream()
            .filter(entity -> !analyzedContraptions.contains(entity))
            .forEach(LittleTilesRuntimeHook::analyzeContraptionForLittleTiles);
    }
    
    /**
     * Analyze a contraption entity for LittleTiles blocks.
     */
    private static void analyzeContraptionForLittleTiles(AbstractContraptionEntity contraptionEntity) {
        try {
            // Mark as analyzed to avoid repeated processing
            analyzedContraptions.add(contraptionEntity);
            
            if (contraptionEntity.getContraption() == null) {
                return;
            }
            
            // Get contraption center for position calculations
            BlockPos center = contraptionEntity.blockPosition();
            
            // Scan area around the contraption for LittleTiles blocks
            scanAreaForLittleTiles(contraptionEntity.level(), center, contraptionEntity);
            
            if (LittleTilesRenderingLogic.isDebugModeEnabled()) {
                LOGGER.debug("Analyzed contraption {} at {} for LittleTiles blocks", 
                    contraptionEntity.getUUID(), center);
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error analyzing contraption {}: {}", 
                contraptionEntity.getUUID(), e.getMessage());
        }
    }
    
    /**
     * Scan an area around a contraption for LittleTiles blocks.
     */
    private static void scanAreaForLittleTiles(Level level, BlockPos center, AbstractContraptionEntity contraption) {
        // Scan a reasonable area around the contraption
        int radius = 32; // Adjust based on typical contraption sizes
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    
                    try {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity != null && LittleTilesDetector.isLittleTilesBlockEntity(blockEntity)) {
                            
                            // Check if this block is actually part of the contraption
                            if (ContraptionDetector.findContainingContraption(blockEntity)
                                    .map(entity -> entity.equals(contraption))
                                    .orElse(false)) {
                                
                                trackedLittleTilesBlocks.add(pos);
                                
                                if (LittleTilesRenderingLogic.isDebugModeEnabled()) {
                                    LOGGER.debug("Found LittleTiles block at {} in contraption {}", 
                                        pos, contraption.getUUID());
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Skip this position if there's an error
                    }
                }
            }
        }
    }
    
    /**
     * Check if a block position is a tracked LittleTiles block.
     */
    public static boolean isTrackedLittleTilesBlock(BlockPos pos) {
        return trackedLittleTilesBlocks.contains(pos);
    }
    
    /**
     * Remove a block from tracking (e.g., when contraption is disassembled).
     */
    public static void removeTrackedBlock(BlockPos pos) {
        trackedLittleTilesBlocks.remove(pos);
    }
    
    /**
     * Clear all tracking data (e.g., when leaving a world).
     */
    public static void clearAllTracking() {
        trackedLittleTilesBlocks.clear();
        analyzedContraptions.clear();
        tickCounter = 0;
        
        if (LittleTilesRenderingLogic.isDebugModeEnabled()) {
            LOGGER.debug("Cleared all LittleTiles tracking data");
        }
    }
    
    /**
     * Get statistics about tracked blocks.
     */
    public static int getTrackedBlockCount() {
        return trackedLittleTilesBlocks.size();
    }
}
