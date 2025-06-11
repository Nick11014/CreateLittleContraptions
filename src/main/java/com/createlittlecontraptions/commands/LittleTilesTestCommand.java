package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import com.createlittlecontraptions.util.ContraptionDetector;
import com.createlittlecontraptions.util.LittleTilesDetector;
import com.createlittlecontraptions.util.PlaceholderBakedModel;
import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import net.minecraft.client.resources.model.BakedModel;

/**
 * Simple test command to verify robust LittleTiles detection is working.
 * Based on proven logic from the original debug command.
 */
public class LittleTilesTestCommand {
    private static final Logger LOGGER = LogUtils.getLogger();    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("littletiles-test")
            .requires(source -> source.hasPermission(2))
            .executes(LittleTilesTestCommand::execute));
            
        dispatcher.register(Commands.literal("cache-test")
            .requires(source -> source.hasPermission(2))            .executes(LittleTilesTestCommand::testCache));
            
        dispatcher.register(Commands.literal("cache-test-prepopulate")
            .requires(source -> source.hasPermission(2))            .executes(LittleTilesTestCommand::testCacheWithPrePopulation));
    }
      private static int testCache(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== ENHANCED DUCK INTERFACE CACHE TEST ==="));
        source.sendSystemMessage(Component.literal("Thread: " + Thread.currentThread().getName()));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("Found " + contraptionEntities.size() + " contraption entities"));
        
        for (Entity entity : contraptionEntities) {
            if (entity instanceof AbstractContraptionEntity contraptionEntity) {
                source.sendSystemMessage(Component.literal(""));
                source.sendSystemMessage(Component.literal("=== Contraption Entity " + contraptionEntity.getId() + " ==="));
                source.sendSystemMessage(Component.literal("Position: " + contraptionEntity.blockPosition()));
                source.sendSystemMessage(Component.literal("Entity Class: " + contraptionEntity.getClass().getSimpleName()));
                
                Object contraption = ContraptionDetector.getContraptionFromEntity(contraptionEntity);
                if (contraption == null) {
                    source.sendSystemMessage(Component.literal("❌ No contraption data found!"));
                    continue;
                }
                
                source.sendSystemMessage(Component.literal("Contraption Class: " + contraption.getClass().getName()));
                source.sendSystemMessage(Component.literal("Contraption Object ID: " + System.identityHashCode(contraption)));
                
                // Count LittleTiles blocks
                int ltCount = ContraptionDetector.countLittleTilesInContraption(contraptionEntity);
                source.sendSystemMessage(Component.literal("LittleTiles blocks detected: " + ltCount));
                
                // Test duck interface access
                try {
                    if (contraption instanceof IContraptionBakedModelCache) {
                        IContraptionBakedModelCache cache = (IContraptionBakedModelCache) contraption;
                        source.sendSystemMessage(Component.literal("✓ Duck interface accessible!"));
                        
                        Optional<Map<BlockPos, BakedModel>> modelCacheOpt = cache.getModelCache();
                        Map<BlockPos, BakedModel> modelCache = modelCacheOpt.orElse(new HashMap<>());
                        source.sendSystemMessage(Component.literal("Model cache size: " + modelCache.size()));
                        
                        // Log to debug as well for cross-reference
                        LOGGER.info("*** CACHE TEST: Entity {} - Cache size: {} ***", 
                                   contraptionEntity.getId(), modelCache.size());
                        
                        if (!modelCache.isEmpty()) {
                            source.sendSystemMessage(Component.literal("Cache contents:"));
                            int shown = 0;
                            for (Map.Entry<BlockPos, BakedModel> entry : modelCache.entrySet()) {
                                if (shown >= 5) {
                                    source.sendSystemMessage(Component.literal("  ... and " + (modelCache.size() - 5) + " more"));
                                    break;
                                }
                                String modelInfo = entry.getValue() != null ? 
                                    (entry.getValue() instanceof PlaceholderBakedModel ? "placeholder" : entry.getValue().getClass().getSimpleName()) : 
                                    "null";
                                source.sendSystemMessage(Component.literal("  " + entry.getKey() + " -> " + modelInfo));
                                shown++;
                            }
                        } else {
                            source.sendSystemMessage(Component.literal("Cache is empty"));
                        }
                        
                        // Test cache manipulation with a unique marker
                        BlockPos testPos = new BlockPos(999, 999, 999);
                        Map<BlockPos, BakedModel> newCache = new HashMap<>(modelCache);
                        newCache.put(testPos, null);
                        cache.setModelCache(newCache);
                        source.sendSystemMessage(Component.literal("✓ Added test marker to cache"));
                        
                        // Verify the marker was set
                        Optional<Map<BlockPos, BakedModel>> verifyOpt = cache.getModelCache();
                        if (verifyOpt.isPresent() && verifyOpt.get().containsKey(testPos)) {
                            source.sendSystemMessage(Component.literal("✓ Cache manipulation verified"));
                        } else {
                            source.sendSystemMessage(Component.literal("❌ Cache manipulation failed verification"));
                        }
                        
                    } else {
                        source.sendSystemMessage(Component.literal("❌ Duck interface NOT accessible!"));
                        source.sendSystemMessage(Component.literal("Contraption interfaces:"));
                        for (Class<?> iface : contraption.getClass().getInterfaces()) {
                            source.sendSystemMessage(Component.literal("  - " + iface.getName()));
                        }
                    }
                } catch (Exception e) {
                    source.sendSystemMessage(Component.literal("❌ Error testing duck interface: " + e.getMessage()));
                    LOGGER.error("Error testing duck interface for contraption " + contraptionEntity.getId(), e);
                }
            }
        }
        
        return 1;
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== LITTLETILES DETECTION TEST ==="));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
        
        int totalContraptions = 0;
        int contraptionsWithLittleTiles = 0;
        int totalLittleTilesBlocks = 0;
        
        for (Entity entity : contraptionEntities) {
            if (entity instanceof AbstractContraptionEntity contraptionEntity) {
                totalContraptions++;
                
                source.sendSystemMessage(Component.literal(""));
                source.sendSystemMessage(Component.literal("--- Contraption #" + totalContraptions + " ---"));
                source.sendSystemMessage(Component.literal("Type: " + contraptionEntity.getClass().getSimpleName()));
                source.sendSystemMessage(Component.literal("Position: " + contraptionEntity.blockPosition()));
                source.sendSystemMessage(Component.literal("Entity ID: " + contraptionEntity.getId()));
                
                // Use our robust detection
                int littleTilesCount = ContraptionDetector.countLittleTilesInContraption(contraptionEntity);
                
                if (littleTilesCount > 0) {
                    contraptionsWithLittleTiles++;
                    totalLittleTilesBlocks += littleTilesCount;
                    
                    source.sendSystemMessage(Component.literal("*** FOUND " + littleTilesCount + " LITTLETILES BLOCKS! ***"));
                      // Get positions
                    List<BlockPos> positions = ContraptionDetector.getLittleTilesPositions(contraptionEntity);
                    source.sendSystemMessage(Component.literal("LittleTiles positions:"));
                    if (positions.isEmpty()) {
                        source.sendSystemMessage(Component.literal("  (No positions found - may need debugging)"));
                    } else {
                        int shown = 0;
                        for (BlockPos pos : positions) {
                            if (shown >= 10) {
                                source.sendSystemMessage(Component.literal("  ... and " + (positions.size() - 10) + " more"));
                                break;
                            }
                            source.sendSystemMessage(Component.literal("  " + pos));
                            shown++;
                        }
                    }
                    
                    // Additional debug info
                    source.sendSystemMessage(Component.literal("Debug Info:"));
                    Object contraption = ContraptionDetector.getContraptionFromEntity(contraptionEntity);
                    if (contraption != null) {
                        Object blocksData = ContraptionDetector.getBlocksFromContraption(contraption);
                        Map<?, ?> blockEntitiesData = ContraptionDetector.getBlockEntitiesFromContraption(contraption);
                        
                        source.sendSystemMessage(Component.literal("  - Contraption class: " + contraption.getClass().getSimpleName()));
                        source.sendSystemMessage(Component.literal("  - Blocks data: " + (blocksData != null ? blocksData.getClass().getSimpleName() : "null")));
                        source.sendSystemMessage(Component.literal("  - BlockEntities count: " + (blockEntitiesData != null ? blockEntitiesData.size() : 0)));
                        
                        if (blockEntitiesData != null && !blockEntitiesData.isEmpty()) {
                            source.sendSystemMessage(Component.literal("  - BlockEntity types:"));
                            int beShown = 0;
                            for (Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                                if (beShown >= 5) {
                                    source.sendSystemMessage(Component.literal("    ... and " + (blockEntitiesData.size() - 5) + " more"));
                                    break;
                                }
                                Object pos = entry.getKey();
                                Object nbtData = entry.getValue();
                                String beType = getBlockEntityType(nbtData);
                                boolean isLittleTiles = beType.toLowerCase().contains("littletiles");
                                String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                                source.sendSystemMessage(Component.literal("    " + pos + " -> " + beType + marker));
                                beShown++;
                            }
                        }
                    }
                } else {
                    source.sendSystemMessage(Component.literal("No LittleTiles blocks detected"));
                }
                
                // Test LittleTiles availability
                if (totalContraptions == 1) { // Only show this once
                    boolean available = LittleTilesDetector.isLittleTilesAvailable();
                    source.sendSystemMessage(Component.literal("LittleTiles mod available: " + available));
                    if (available) {
                        Class<?> ltClass = LittleTilesDetector.getLittleTilesBlockEntityClass();
                        source.sendSystemMessage(Component.literal("LittleTiles BE class: " + 
                            (ltClass != null ? ltClass.getName() : "null")));
                    }
                }
            }
        }
        
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== SUMMARY ==="));
        source.sendSystemMessage(Component.literal("Total Contraptions: " + totalContraptions));
        source.sendSystemMessage(Component.literal("Contraptions with LittleTiles: " + contraptionsWithLittleTiles));
        source.sendSystemMessage(Component.literal("Total LittleTiles Blocks: " + totalLittleTilesBlocks));
        
        if (totalLittleTilesBlocks > 0) {
            source.sendSystemMessage(Component.literal("*** SUCCESS: Robust detection is working! ***"));
        } else {
            source.sendSystemMessage(Component.literal("No LittleTiles detected - this is normal if none are present"));
        }
        
        return totalLittleTilesBlocks;
    }    private static List<Entity> findContraptionEntities(ServerLevel level) {
        List<Entity> contraptions = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof AbstractContraptionEntity) {
                contraptions.add(entity);
            }
        }
        return contraptions;
    }

    private static int testCacheWithPrePopulation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== CACHE TEST WITH PRE-POPULATION ==="));
        source.sendSystemMessage(Component.literal("Thread: " + Thread.currentThread().getName()));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("Found " + contraptionEntities.size() + " contraption entities"));
        
        for (Entity entity : contraptionEntities) {
            if (entity instanceof AbstractContraptionEntity contraptionEntity) {
                source.sendSystemMessage(Component.literal(""));
                source.sendSystemMessage(Component.literal("=== Contraption Entity " + contraptionEntity.getId() + " ==="));
                
                Object contraption = ContraptionDetector.getContraptionFromEntity(contraptionEntity);
                if (contraption == null) {
                    source.sendSystemMessage(Component.literal("❌ No contraption data found!"));
                    continue;
                }
                
                int contraptionObjectId = System.identityHashCode(contraption);
                source.sendSystemMessage(Component.literal("Contraption Object ID: " + contraptionObjectId));
                
                // STEP 1: Force cache population by triggering the detection event
                source.sendSystemMessage(Component.literal("🔄 Triggering cache population..."));
                  try {
                    // Get LittleTiles positions to trigger detection and caching
                    List<BlockPos> ltPositions = ContraptionDetector.getLittleTilesPositions(contraptionEntity);
                    source.sendSystemMessage(Component.literal("Found " + ltPositions.size() + " LittleTiles positions"));
                    
                    // Debug: Show the positions found
                    if (!ltPositions.isEmpty()) {
                        source.sendSystemMessage(Component.literal("Positions: " + ltPositions.toString()));
                    }
                    
                    // Manually trigger cache population for each position
                    if (contraption instanceof IContraptionBakedModelCache) {
                        IContraptionBakedModelCache duck = (IContraptionBakedModelCache) contraption;
                        source.sendSystemMessage(Component.literal("✓ Duck interface cast successful"));
                        
                        Optional<Map<BlockPos, BakedModel>> modelCacheOpt = duck.getModelCache();
                        source.sendSystemMessage(Component.literal("✓ Retrieved model cache optional"));
                        
                        Map<BlockPos, BakedModel> modelCache = modelCacheOpt.orElse(new HashMap<>());
                        source.sendSystemMessage(Component.literal("✓ Got model cache map, current size: " + modelCache.size()));
                        
                        // If cache is empty, populate it with placeholders
                        if (modelCache.isEmpty() && !ltPositions.isEmpty()) {
                            source.sendSystemMessage(Component.literal("Cache is empty, adding " + ltPositions.size() + " positions..."));                            for (BlockPos pos : ltPositions) {
                                source.sendSystemMessage(Component.literal("Adding position: " + pos));
                                modelCache.put(pos, PlaceholderBakedModel.INSTANCE); // Use placeholder instead of null
                            }
                            source.sendSystemMessage(Component.literal("✓ Added all positions, calling setModelCache..."));
                            duck.setModelCache(modelCache);
                            source.sendSystemMessage(Component.literal("✓ Pre-populated cache with " + ltPositions.size() + " entries"));
                        } else if (!modelCache.isEmpty()) {
                            source.sendSystemMessage(Component.literal("Cache already has " + modelCache.size() + " entries"));
                        } else {
                            source.sendSystemMessage(Component.literal("No positions to cache"));
                        }
                    } else {
                        source.sendSystemMessage(Component.literal("❌ Contraption does not implement IContraptionBakedModelCache"));
                        source.sendSystemMessage(Component.literal("Contraption class: " + contraption.getClass().getName()));
                        source.sendSystemMessage(Component.literal("Interfaces:"));
                        for (Class<?> iface : contraption.getClass().getInterfaces()) {
                            source.sendSystemMessage(Component.literal("  - " + iface.getName()));
                        }
                    }
                    
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName() + " (no message)";
                    source.sendSystemMessage(Component.literal("❌ Error during pre-population: " + errorMsg));
                    LOGGER.error("Error during cache pre-population for contraption " + contraptionEntity.getId(), e);
                }
                
                // Small delay to ensure cache operations complete
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
                
                // STEP 2: Now test the cache
                try {
                    if (contraption instanceof IContraptionBakedModelCache) {
                        IContraptionBakedModelCache cache = (IContraptionBakedModelCache) contraption;
                        source.sendSystemMessage(Component.literal("✓ Duck interface accessible!"));
                        
                        Optional<Map<BlockPos, BakedModel>> modelCacheOpt = cache.getModelCache();
                        Map<BlockPos, BakedModel> modelCache = modelCacheOpt.orElse(new HashMap<>());
                        source.sendSystemMessage(Component.literal("Model cache size AFTER pre-population: " + modelCache.size()));
                        
                        // Log to debug as well for cross-reference
                        LOGGER.info("*** CACHE TEST WITH PRE-POP: Entity {} - Cache size: {} ***", 
                                   contraptionEntity.getId(), modelCache.size());
                        
                        if (!modelCache.isEmpty()) {
                            source.sendSystemMessage(Component.literal("✓ Cache now contains data!"));
                            source.sendSystemMessage(Component.literal("Cache contents:"));
                            int shown = 0;
                            for (Map.Entry<BlockPos, BakedModel> entry : modelCache.entrySet()) {
                                if (shown >= 3) {
                                    source.sendSystemMessage(Component.literal("  ... and " + (modelCache.size() - 3) + " more"));
                                    break;
                                }
                                String modelInfo = entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null(placeholder)";
                                source.sendSystemMessage(Component.literal("  " + entry.getKey() + " -> " + modelInfo));
                                shown++;
                            }
                        } else {
                            source.sendSystemMessage(Component.literal("❌ Cache still empty after pre-population"));
                        }
                        
                    } else {
                        source.sendSystemMessage(Component.literal("❌ Duck interface NOT accessible!"));
                    }
                } catch (Exception e) {
                    source.sendSystemMessage(Component.literal("❌ Error testing cache: " + e.getMessage()));
                    LOGGER.error("Error testing cache for contraption " + contraptionEntity.getId(), e);
                }
            }
        }
        
        return 1;
    }

    /**
     * Get block entity type from NBT data (helper method).
     */
    private static String getBlockEntityType(Object nbtData) {
        try {
            if (nbtData instanceof net.minecraft.nbt.CompoundTag nbt) {
                return nbt.getString("id");
            }
            // Try reflection if CompoundTag doesn't work directly
            java.lang.reflect.Method getStringMethod = nbtData.getClass().getMethod("getString", String.class);
            Object result = getStringMethod.invoke(nbtData, "id");
            return result != null ? result.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
