package com.createlittlecontraptions.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
      /**
     * Get contraption data from entity using reflection.
     * Based on proven logic from ContraptionDebugCommand.
     */
    public static Object getContraptionFromEntity(AbstractContraptionEntity entity) {
        try {
            // First try the standard method
            Object contraption = entity.getContraption();
            if (contraption != null) {
                return contraption;
            }
            
            // If that fails, try reflection to access the field directly
            String[] possibleContraptionFields = {"contraption", "contraptionInstance", "contraptionData"};
            
            for (String fieldName : possibleContraptionFields) {
                try {
                    var field = AbstractContraptionEntity.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object contraptionFromField = field.get(entity);
                    if (contraptionFromField != null) {
                        LOGGER.debug("Successfully accessed contraption via reflection field: {}", fieldName);
                        return contraptionFromField;
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                }
            }
            
            LOGGER.debug("Could not access contraption from entity via any method");
            return null;
            
        } catch (Exception e) {
            LOGGER.debug("Error getting contraption from entity: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Get blocks data from contraption using reflection.
     * Based on proven logic from ContraptionDebugCommand.
     */
    public static Object getBlocksFromContraption(Object contraption) {
        try {
            Method getBlocksMethod = contraption.getClass().getMethod("getBlocks");
            return getBlocksMethod.invoke(contraption);
        } catch (Exception e) {
            LOGGER.debug("Error getting blocks from contraption: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Get block entities data from contraption using reflection.
     * Based on proven logic from ContraptionDebugCommand.
     */
    public static Map<?, ?> getBlockEntitiesFromContraption(Object contraption) {
        try {
            // Try different method names that might exist
            String[] methodNames = {"getBlockEntities", "getStoredBlockData", "getBlockEntityData"};
            
            for (String methodName : methodNames) {
                try {
                    Method method = contraption.getClass().getMethod(methodName);
                    Object result = method.invoke(contraption);
                    if (result instanceof Map<?, ?>) {
                        return (Map<?, ?>) result;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try next method name
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting block entities from contraption: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Count LittleTiles blocks in a contraption using robust detection.
     * Based on proven logic from ContraptionDebugCommand.
     */
    public static int countLittleTilesInContraption(AbstractContraptionEntity contraptionEntity) {
        try {
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) return 0;
            
            // Count from block data
            int blockCount = countLittleTilesFromBlocks(contraption);
            
            // Count from block entities
            int beCount = countLittleTilesFromBlockEntities(contraption);
            
            // Return the higher count (they should be the same, but this ensures we don't miss any)
            return Math.max(blockCount, beCount);
            
        } catch (Exception e) {
            LOGGER.debug("Error counting LittleTiles in contraption: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Count LittleTiles blocks from block data in contraption.
     */
    private static int countLittleTilesFromBlocks(Object contraption) {
        try {
            Object blocksData = getBlocksFromContraption(contraption);
            if (blocksData == null) return 0;
            
            int count = 0;
            
            if (blocksData instanceof Map<?, ?> blocksMap) {
                for (Object blockData : blocksMap.values()) {
                    if (LittleTilesDetector.isLittleTilesBlockData(blockData)) {
                        count++;
                    }
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                for (Object blockData : blocksCollection) {
                    if (LittleTilesDetector.isLittleTilesBlockData(blockData)) {
                        count++;
                    }
                }
            }
            
            return count;
            
        } catch (Exception e) {
            LOGGER.debug("Error counting LittleTiles from blocks: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Count LittleTiles blocks from block entities in contraption.
     */
    private static int countLittleTilesFromBlockEntities(Object contraption) {
        try {
            Map<?, ?> blockEntitiesData = getBlockEntitiesFromContraption(contraption);
            if (blockEntitiesData == null) return 0;
            
            int count = 0;
            
            for (Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                Object nbtData = entry.getValue();
                String beType = getBlockEntityType(nbtData);
                if (beType.toLowerCase().contains("littletiles")) {
                    count++;
                }
            }
            
            return count;
            
        } catch (Exception e) {
            LOGGER.debug("Error counting LittleTiles from block entities: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get block entity type from NBT data.
     * Based on proven logic from ContraptionDebugCommand.
     */
    private static String getBlockEntityType(Object nbtData) {
        try {
            if (nbtData instanceof CompoundTag nbt) {
                return nbt.getString("id");
            }
            // Try reflection if CompoundTag doesn't work directly
            Method getStringMethod = nbtData.getClass().getMethod("getString", String.class);
            Object result = getStringMethod.invoke(nbtData, "id");
            return result != null ? result.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
      /**
     * Get all LittleTiles positions in a contraption.
     * Returns a list of BlockPos for LittleTiles blocks.
     * Updated to match the logic used in countLittleTilesInContraption().
     */
    public static List<BlockPos> getLittleTilesPositions(AbstractContraptionEntity contraptionEntity) {
        List<BlockPos> positions = new ArrayList<>();
        
        try {
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) return positions;
            
            // Check both blocks AND block entities (same logic as countLittleTilesInContraption)
            
            // 1. Get positions from block data
            List<BlockPos> blockPositions = getLittleTilesPositionsFromBlocks(contraption);
            positions.addAll(blockPositions);
            
            // 2. Get positions from block entities
            List<BlockPos> bePositions = getLittleTilesPositionsFromBlockEntities(contraption);
            positions.addAll(bePositions);
              // Remove duplicates (in case a position appears in both lists)
            positions = positions.stream().distinct().collect(Collectors.toList());
            
        } catch (Exception e) {
            LOGGER.debug("Error getting LittleTiles positions: {}", e.getMessage());
        }
        
        return positions;
    }
    
    /**
     * Get LittleTiles positions from block data in contraption.
     */
    private static List<BlockPos> getLittleTilesPositionsFromBlocks(Object contraption) {
        List<BlockPos> positions = new ArrayList<>();
        
        try {
            Object blocksData = getBlocksFromContraption(contraption);
            if (blocksData == null) return positions;
            
            if (blocksData instanceof Map<?, ?> blocksMap) {
                for (Map.Entry<?, ?> entry : blocksMap.entrySet()) {
                    Object blockData = entry.getValue();
                    if (LittleTilesDetector.isLittleTilesBlockData(blockData)) {
                        Object pos = entry.getKey();
                        if (pos instanceof BlockPos blockPos) {
                            positions.add(blockPos);
                        }
                    }
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                // For collections, we don't have direct position mapping
                // This case is less common, but we handle it for completeness
                LOGGER.debug("Block data is a collection - positions may not be available");
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error getting LittleTiles positions from blocks: {}", e.getMessage());
        }
        
        return positions;
    }
    
    /**
     * Get LittleTiles positions from block entities in contraption.
     */
    private static List<BlockPos> getLittleTilesPositionsFromBlockEntities(Object contraption) {
        List<BlockPos> positions = new ArrayList<>();
        
        try {
            Map<?, ?> blockEntitiesData = getBlockEntitiesFromContraption(contraption);
            if (blockEntitiesData != null) {
                for (Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                    Object nbtData = entry.getValue();
                    String beType = getBlockEntityType(nbtData);
                    if (beType.toLowerCase().contains("littletiles")) {
                        Object pos = entry.getKey();
                        if (pos instanceof BlockPos blockPos) {
                            positions.add(blockPos);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error getting LittleTiles positions from block entities: {}", e.getMessage());
        }
        
        return positions;
    }
}
