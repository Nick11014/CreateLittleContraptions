package com.createlittlecontraptions.rendering;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.createlittlecontraptions.CreateLittleContraptions;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central control system for individual block entity rendering in contraptions.
 * This allows granular control over which blocks are visible during contraption movement.
 */
public class ContraptionBlockRenderController {
    
    // Global render control - affects all contraptions
    private static boolean globalRenderingEnabled = true;
    
    // Per-contraption render control
    private static final Map<UUID, Boolean> contraptionRenderStates = new ConcurrentHashMap<>();
    
    // Per-block render control within contraptions
    private static final Map<UUID, Map<BlockPos, Boolean>> blockRenderStates = new ConcurrentHashMap<>();
    
    // Block type render control (e.g., hide all LittleTiles)
    private static final Map<String, Boolean> blockTypeRenderStates = new ConcurrentHashMap<>();
    
    /**
     * Check if a specific block entity should be rendered in a contraption
     */
    public static boolean shouldRenderBlockEntity(BlockEntity blockEntity, UUID contraptionUUID) {
        // Global disable override
        if (!globalRenderingEnabled) {
            return false;
        }
        
        // Per-contraption control
        if (contraptionRenderStates.containsKey(contraptionUUID)) {
            boolean contraptionEnabled = contraptionRenderStates.get(contraptionUUID);
            if (!contraptionEnabled) {
                return false;
            }
        }
        
        // Per-block control within this contraption
        Map<BlockPos, Boolean> contraptionBlocks = blockRenderStates.get(contraptionUUID);
        if (contraptionBlocks != null && contraptionBlocks.containsKey(blockEntity.getBlockPos())) {
            return contraptionBlocks.get(blockEntity.getBlockPos());
        }
        
        // Block type control
        String blockTypeName = blockEntity.getType().toString();
        String className = blockEntity.getClass().getSimpleName();
        
        // Check exact type matches
        if (blockTypeRenderStates.containsKey(blockTypeName)) {
            return blockTypeRenderStates.get(blockTypeName);
        }
        
        // Check partial matches for LittleTiles, etc.
        for (Map.Entry<String, Boolean> entry : blockTypeRenderStates.entrySet()) {
            if (blockTypeName.contains(entry.getKey()) || className.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Default: render everything
        return true;
    }
      /**
     * Set global rendering state for all contraptions
     */
    public static void setGlobalRendering(boolean enabled) {
        globalRenderingEnabled = enabled;
        CreateLittleContraptions.LOGGER.info("Global contraption rendering: {}", enabled ? "ENABLED" : "DISABLED");
    }
    
    /**
     * Control rendering for a specific contraption
     */
    public static void setContraptionRendering(UUID contraptionUUID, boolean enabled) {
        contraptionRenderStates.put(contraptionUUID, enabled);
        CreateLittleContraptions.LOGGER.info("Contraption {} rendering: {}", 
            contraptionUUID, enabled ? "ENABLED" : "DISABLED");
    }
    
    /**
     * Control rendering for a specific block within a contraption
     */
    public static void setBlockRendering(UUID contraptionUUID, BlockPos blockPos, boolean enabled) {
        blockRenderStates.computeIfAbsent(contraptionUUID, k -> new ConcurrentHashMap<>())
            .put(blockPos, enabled);
        CreateLittleContraptions.LOGGER.info("Block {} in contraption {} rendering: {}", 
            blockPos, contraptionUUID, enabled ? "ENABLED" : "DISABLED");
    }
      /**
     * Control rendering for all blocks of a specific type
     */
    public static void setBlockTypeRendering(String blockTypePattern, boolean enabled) {
        blockTypeRenderStates.put(blockTypePattern, enabled);
        CreateLittleContraptions.LOGGER.info("Block type '{}' rendering: {}", 
            blockTypePattern, enabled ? "ENABLED" : "DISABLED");
    }
    
    /**
     * Clear block-level rendering overrides for a specific contraption
     */
    public static void clearBlockSettings(UUID contraptionUUID) {
        blockRenderStates.remove(contraptionUUID);
        CreateLittleContraptions.LOGGER.info("Cleared block settings for contraption {}", contraptionUUID);
    }
    
    /**
     * Get current render state for debugging
     */
    public static String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Contraption Render Controller State ===\n");
        sb.append("Global: ").append(globalRenderingEnabled).append("\n");
        sb.append("Contraptions: ").append(contraptionRenderStates.size()).append(" controlled\n");
        sb.append("Block overrides: ").append(blockRenderStates.size()).append(" contraptions with individual blocks\n");
        sb.append("Type filters: ").append(blockTypeRenderStates).append("\n");
        return sb.toString();
    }
    
    /**
     * Clear all rendering overrides
     */
    public static void clearAll() {
        globalRenderingEnabled = true;
        contraptionRenderStates.clear();
        blockRenderStates.clear();
        blockTypeRenderStates.clear();
        CreateLittleContraptions.LOGGER.info("Cleared all rendering overrides");
    }
}
