package com.createlittlecontraptions.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Optional;

/**
 * Utility class for detecting if a BlockEntity is part of a Create contraption.
 * Inspired by create_interactive's detection logic.
 */
public class ContraptionDetector {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Find the contraption entity that contains the given block entity.
     * Returns empty if the block entity is not part of any contraption.
     */
    public static Optional<AbstractContraptionEntity> findContainingContraption(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return Optional.empty();
        }
        
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        
        try {
            // Search for contraption entities in the area
            // Contraptions are typically within a reasonable distance of their blocks
            double searchRadius = 256.0; // Reasonable search radius for contraptions
            
            return level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                new net.minecraft.world.phys.AABB(pos).inflate(searchRadius))
                .stream()
                .filter(entity -> containsBlock(entity, pos))
                .findFirst();
                
        } catch (Exception e) {
            LOGGER.debug("Error searching for contraption containing block at {}: {}", pos, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Check if a contraption entity contains a block at the given world position.
     */
    private static boolean containsBlock(AbstractContraptionEntity contraptionEntity, BlockPos worldPos) {
        try {
            if (contraptionEntity == null || contraptionEntity.getContraption() == null) {
                return false;
            }
            
            // Convert world position to contraption-relative position
            BlockPos relativePos = getRelativePosition(worldPos, contraptionEntity);
            
            // Check if the contraption has a block at this relative position
            return hasBlockAtPosition(contraptionEntity, relativePos);
            
        } catch (Exception e) {
            LOGGER.debug("Error checking if contraption contains block at {}: {}", worldPos, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the relative position of a block entity within its contraption.
     */
    public static BlockPos getRelativePosition(BlockEntity blockEntity, AbstractContraptionEntity contraptionEntity) {
        return getRelativePosition(blockEntity.getBlockPos(), contraptionEntity);
    }
    
    /**
     * Get the relative position of a world position within a contraption.
     */
    public static BlockPos getRelativePosition(BlockPos worldPos, AbstractContraptionEntity contraptionEntity) {
        try {
            // Get the contraption's anchor position (this might vary by Create version)
            BlockPos anchorPos = getContraptionAnchor(contraptionEntity);
            return worldPos.subtract(anchorPos);
            
        } catch (Exception e) {
            LOGGER.debug("Error calculating relative position for {}: {}", worldPos, e.getMessage());
            return worldPos; // Fallback to world position
        }
    }
    
    /**
     * Get the anchor position of a contraption using reflection.
     */
    private static BlockPos getContraptionAnchor(AbstractContraptionEntity contraptionEntity) {
        try {
            // Try different methods to get the contraption anchor
            // This may vary between Create versions
            
            // Method 1: Try getAnchorPos() if it exists
            try {
                var method = contraptionEntity.getClass().getMethod("getAnchorPos");
                Object result = method.invoke(contraptionEntity);
                if (result instanceof BlockPos) {
                    return (BlockPos) result;
                }
            } catch (Exception ignored) {}
            
            // Method 2: Try accessing anchor field directly
            try {
                var field = contraptionEntity.getClass().getDeclaredField("anchor");
                field.setAccessible(true);
                Object result = field.get(contraptionEntity);
                if (result instanceof BlockPos) {
                    return (BlockPos) result;
                }
            } catch (Exception ignored) {}
            
            // Method 3: Use entity position as fallback
            return contraptionEntity.blockPosition();
            
        } catch (Exception e) {
            LOGGER.debug("Error getting contraption anchor: {}", e.getMessage());
            return contraptionEntity.blockPosition();
        }
    }
    
    /**
     * Check if a contraption has a block at the given relative position.
     */
    private static boolean hasBlockAtPosition(AbstractContraptionEntity contraptionEntity, BlockPos relativePos) {
        try {
            var contraption = contraptionEntity.getContraption();
            if (contraption == null) return false;
            
            // Try to access the blocks map using reflection
            // The exact field name may vary between Create versions
            String[] possibleFields = {"blocks", "structureTemplate", "template"};
            
            for (String fieldName : possibleFields) {
                try {
                    var field = contraption.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object blocks = field.get(contraption);
                    
                    if (blocks instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<BlockPos, ?> blockMap = (java.util.Map<BlockPos, ?>) blocks;
                        return blockMap.containsKey(relativePos);
                    }
                    
                } catch (Exception ignored) {
                    // Try next field
                }
            }
            
            return false;
            
        } catch (Exception e) {
            LOGGER.debug("Error checking block at position {} in contraption: {}", relativePos, e.getMessage());
            return false;
        }
    }
}
