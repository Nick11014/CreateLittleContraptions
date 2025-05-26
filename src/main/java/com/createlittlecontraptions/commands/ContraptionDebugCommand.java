package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContraptionDebugCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-debug")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionDebugCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
          source.sendSystemMessage(Component.literal("=== CONTRAPTION DEBUG REPORT ==="));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
          int contraptionCount = 0;
        int totalBlocks = 0;
        int totalLittleTilesBlocks = 0;
        
        for (Entity entity : contraptionEntities) {
            contraptionCount++;
            final int currentContraptionNumber = contraptionCount; // Make final for lambda
              source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("--- Contraption #" + currentContraptionNumber + " ---"));
            source.sendSystemMessage(Component.literal("Type: " + entity.getClass().getSimpleName()));
            source.sendSystemMessage(Component.literal("Position: " + entity.blockPosition()));
            source.sendSystemMessage(Component.literal("Entity ID: " + entity.getId()));
            
            try {
                // Get contraption data using reflection
                Object contraption = getContraptionFromEntity(entity);
                if (contraption != null) {
                    // Get blocks data
                    Object blocksData = getBlocksFromContraption(contraption);
                    Map<?, ?> blockEntitiesData = getBlockEntitiesFromContraption(contraption);
                    
                    int blockCount = getBlockCount(blocksData);
                    int beCount = blockEntitiesData != null ? blockEntitiesData.size() : 0;
                    int littleTilesInThisContraption = countLittleTilesInContraption(entity);                    source.sendSystemMessage(Component.literal("Total Blocks: " + blockCount));
                    source.sendSystemMessage(Component.literal("Total BlockEntities: " + beCount));
                    source.sendSystemMessage(Component.literal("LittleTiles Blocks: " + littleTilesInThisContraption));
                    
                    totalBlocks += blockCount;
                    totalLittleTilesBlocks += littleTilesInThisContraption;
                      // Show ALL blocks in the contraption
                    if (blocksData != null) {
                        source.sendSystemMessage(Component.literal("ALL BLOCKS IN CONTRAPTION:"));
                        showAllBlocks(source, blocksData);
                    }
                      // Show BlockEntity details
                    if (blockEntitiesData != null && !blockEntitiesData.isEmpty()) {
                        source.sendSystemMessage(Component.literal("BlockEntities:"));
                        int beShown = 0;
                        for (Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                            if (beShown >= 20) { // Show more BEs
                                final int remainingCount = beCount - 20;
                                source.sendSystemMessage(Component.literal("  ... and " + remainingCount + " more BlockEntities"));
                                break;
                            }
                            Object pos = entry.getKey();
                            Object nbtData = entry.getValue();
                            String beType = getBlockEntityType(nbtData);
                            boolean isLittleTiles = beType.contains("littletiles");
                            String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                            source.sendSystemMessage(Component.literal("  " + pos + " -> " + beType + marker));
                            beShown++;
                        }
                    }
                    
                    if (littleTilesInThisContraption > 0) {
                        final int finalLittleTilesCount = littleTilesInThisContraption;
                        source.sendSystemMessage(Component.literal("*** " + finalLittleTilesCount + " LittleTiles found in this contraption! ***"));
                    }
                }
            } catch (Exception e) {
                source.sendSystemMessage(Component.literal("Error analyzing contraption: " + e.getMessage()));
                LOGGER.warn("Error analyzing contraption {}: {}", entity.getId(), e.getMessage());
            }
        }
        
        // Make final copies for lambda usage
        final int finalContraptionCount = contraptionCount;
        final int finalTotalBlocks = totalBlocks;
        final int finalTotalLittleTilesBlocks = totalLittleTilesBlocks;
        
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== SUMMARY ==="));
        source.sendSystemMessage(Component.literal("Total Contraptions: " + finalContraptionCount));
        source.sendSystemMessage(Component.literal("Total Blocks in Contraptions: " + finalTotalBlocks));
        source.sendSystemMessage(Component.literal("Total LittleTiles in Contraptions: " + finalTotalLittleTilesBlocks));
        
        if (totalLittleTilesBlocks > 0) {
            source.sendSystemMessage(Component.literal("*** WARNING: " + finalTotalLittleTilesBlocks + " LittleTiles detected in contraptions! ***"));
        }
        
        return contraptionCount;
    }
    
    // Helper methods using reflection (based on existing dev command)
    
    private static List<Entity> findContraptionEntities(ServerLevel level) {
        List<Entity> contraptionEntities = new ArrayList<>();
        
        try {
            for (Entity entity : level.getAllEntities()) {
                if (isContraptionEntity(entity)) {
                    contraptionEntities.add(entity);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error finding contraption entities: " + e.getMessage());
        }
        
        return contraptionEntities;
    }
    
    private static boolean isContraptionEntity(Entity entity) {
        return entity instanceof AbstractContraptionEntity;
    }
    
    private static Object getContraptionFromEntity(Entity entity) {
        try {
            if (entity instanceof AbstractContraptionEntity contraptionEntity) {
                return contraptionEntity.getContraption();
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting contraption from entity: " + e.getMessage());
        }
        return null;
    }
    
    private static Object getBlocksFromContraption(Object contraption) {
        try {
            Method getBlocksMethod = contraption.getClass().getMethod("getBlocks");
            return getBlocksMethod.invoke(contraption);
        } catch (Exception e) {
            LOGGER.debug("Error getting blocks from contraption: " + e.getMessage());
        }
        return null;
    }
    
    private static Map<?, ?> getBlockEntitiesFromContraption(Object contraption) {
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
            LOGGER.debug("Error getting block entities from contraption: " + e.getMessage());
        }
        return null;
    }
    
    private static int getBlockCount(Object blocksData) {
        try {
            if (blocksData instanceof Map<?, ?> map) {
                return map.size();
            } else if (blocksData instanceof java.util.Collection<?> collection) {
                return collection.size();
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting block count: " + e.getMessage());
        }
        return 0;
    }
    
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
    
    private static int countLittleTilesInContraption(Entity contraptionEntity) {
        try {
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) return 0;
            
            Object blocksData = getBlocksFromContraption(contraption);
            if (blocksData == null) return 0;
            
            int count = 0;
            
            if (blocksData instanceof Map<?, ?> blocksMap) {
                for (Object blockData : blocksMap.values()) {
                    if (isLittleTilesBlock(blockData)) {
                        count++;
                    }
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                for (Object blockData : blocksCollection) {
                    if (isLittleTilesBlock(blockData)) {
                        count++;
                    }
                }
            }
            
            return count;
            
        } catch (Exception e) {
            LOGGER.debug("Error counting LittleTiles in contraption: " + e.getMessage());
            return 0;
        }
    }
    
    private static boolean isLittleTilesBlock(Object blockData) {
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
            LOGGER.debug("Error checking if block is LittleTiles: " + e.getMessage());
        }        
        return false;
    }
      private static void showAllBlocks(CommandSourceStack source, Object blocksData) {
        try {
            if (blocksData instanceof Map<?, ?> blocksMap) {
                int blockIndex = 0;
                for (Map.Entry<?, ?> entry : blocksMap.entrySet()) {
                    if (blockIndex >= 50) { // Limit to prevent spam
                        final int remainingBlocks = blocksMap.size() - 50;
                        source.sendSystemMessage(Component.literal("  ... and " + remainingBlocks + " more blocks"));
                        break;
                    }
                    
                    Object pos = entry.getKey();
                    Object blockData = entry.getValue();
                    String blockInfo = getBlockInfo(blockData);
                    
                    final String blockLine = "  [" + (blockIndex + 1) + "] " + pos + " -> " + blockInfo;
                    source.sendSystemMessage(Component.literal(blockLine));
                    blockIndex++;
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                int blockIndex = 0;
                for (Object blockData : blocksCollection) {
                    if (blockIndex >= 50) { // Limit to prevent spam
                        final int remainingBlocks = blocksCollection.size() - 50;
                        source.sendSystemMessage(Component.literal("  ... and " + remainingBlocks + " more blocks"));
                        break;
                    }
                    
                    String blockInfo = getBlockInfo(blockData);
                    final String blockLine = "  [" + (blockIndex + 1) + "] " + blockInfo;
                    source.sendSystemMessage(Component.literal(blockLine));
                    blockIndex++;
                }
            }
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("  Error listing blocks: " + e.getMessage()));
            LOGGER.debug("Error showing all blocks: " + e.getMessage());
        }
    }
    
    private static String getBlockInfo(Object blockData) {
        try {
            // Try to get block state information
            Object blockState = null;
            
            // Try different field/method names for block state
            String[] stateAccessors = {"state", "getState", "blockState", "getBlockState"};
            
            for (String accessor : stateAccessors) {
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
                // Get block from BlockState
                try {
                    Method getBlockMethod = blockState.getClass().getMethod("getBlock");
                    Object block = getBlockMethod.invoke(blockState);
                    
                    if (block != null) {
                        // Try to get block name
                        try {
                            Method getNameMethod = block.getClass().getMethod("getName");
                            Object nameComponent = getNameMethod.invoke(block);
                            
                            if (nameComponent != null) {
                                Method getStringMethod = nameComponent.getClass().getMethod("getString");
                                String blockName = (String) getStringMethod.invoke(nameComponent);
                                
                                // Check if it's a LittleTiles block
                                boolean isLittleTiles = block.getClass().getName().toLowerCase().contains("littletiles");
                                String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                                
                                return blockName + marker + " (" + block.getClass().getSimpleName() + ")";
                            }
                        } catch (Exception ignored) {}
                        
                        // Fallback to class name
                        boolean isLittleTiles = block.getClass().getName().toLowerCase().contains("littletiles");
                        String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                        return block.getClass().getSimpleName() + marker;
                    }
                } catch (Exception ignored) {}
            }
            
            // Final fallback
            return blockData.getClass().getSimpleName() + " (unknown block)";
            
        } catch (Exception e) {
            return "Error getting block info: " + e.getMessage();
        }
    }
}
