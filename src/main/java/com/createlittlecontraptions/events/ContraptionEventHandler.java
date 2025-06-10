package com.createlittlecontraptions.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.resources.model.BakedModel;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.createlittlecontraptions.CreateLittleContraptions;
import com.createlittlecontraptions.rendering.baking.LittleTilesModelBaker;
import com.createlittlecontraptions.rendering.cache.ContraptionModelCache;

import java.util.Optional;

@EventBusSubscriber(modid = CreateLittleContraptions.MODID)
public class ContraptionEventHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Detecta quando uma contraption é montada (entidade criada)
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            CreateLittleContraptions.LOGGER.info("Contraption assembled: {}", contraptionEntity.getUUID());
            
            // Implement model baking for LittleTiles blocks
            Contraption contraption = contraptionEntity.getContraption();
            if (contraption != null) {
                performModelBaking(contraptionEntity.getUUID(), contraption);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        // Detecta quando uma contraption é desmontada (entidade removida)
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            CreateLittleContraptions.LOGGER.info("Contraption disassembled: {}", contraptionEntity.getUUID());
            
            // Clear cached models to free memory
            ContraptionModelCache.clearCache(contraptionEntity.getUUID());
        }
    }    /**
     * Perform model baking for all LittleTiles blocks in the contraption.
     */
    private static void performModelBaking(java.util.UUID contraptionId, Contraption contraption) {
        try {
            // Get all rendered block entities in the contraption
            var renderedBEs = contraption.getRenderedBEs();
            int littleTilesCount = 0;
            int bakedCount = 0;
            
            CreateLittleContraptions.LOGGER.info("Starting model baking for contraption {} with {} rendered block entities", 
                                                contraptionId, renderedBEs.size());
            
            // Let's also check ALL block entities in the contraption, not just rendered ones
            try {
                // Use reflection to get all block entities
                java.lang.reflect.Field presentBlockEntitiesField = contraption.getClass().getDeclaredField("presentBlockEntities");
                presentBlockEntitiesField.setAccessible(true);
                Object presentBlockEntities = presentBlockEntitiesField.get(contraption);
                
                if (presentBlockEntities instanceof java.util.Map) {
                    java.util.Map<?, ?> allBlockEntities = (java.util.Map<?, ?>) presentBlockEntities;
                    CreateLittleContraptions.LOGGER.info("Total block entities in contraption: {}", allBlockEntities.size());
                    
                    // Log the difference
                    if (allBlockEntities.size() > renderedBEs.size()) {
                        CreateLittleContraptions.LOGGER.warn("Found {} total block entities but only {} are marked as rendered!", 
                                                           allBlockEntities.size(), renderedBEs.size());
                        
                        // Let's see what block entities are NOT rendered
                        CreateLittleContraptions.LOGGER.info("=== NON-RENDERED Block Entities ===");
                        for (Object entry : allBlockEntities.entrySet()) {
                            if (entry instanceof java.util.Map.Entry) {
                                java.util.Map.Entry<?, ?> mapEntry = (java.util.Map.Entry<?, ?>) entry;
                                Object blockEntity = mapEntry.getValue();
                                if (blockEntity instanceof BlockEntity) {
                                    BlockEntity be = (BlockEntity) blockEntity;
                                    if (!renderedBEs.contains(be)) {
                                        String className = be.getClass().getName();
                                        boolean isLittleTiles = className.contains("littletiles");
                                        CreateLittleContraptions.LOGGER.info("Non-rendered BE: {} at {} (LittleTiles: {})", 
                                                                           className, be.getBlockPos(), isLittleTiles);
                                    }
                                }
                            }
                        }
                        CreateLittleContraptions.LOGGER.info("=== End Non-Rendered Block Entities ===");
                    }
                }
            } catch (Exception e) {
                CreateLittleContraptions.LOGGER.debug("Could not access presentBlockEntities field: {}", e.getMessage());
            }
            
            // Enhanced debug logging to understand what block entities we have
            if (renderedBEs.isEmpty()) {
                CreateLittleContraptions.LOGGER.warn("No rendered block entities found in contraption!");
            } else {
                CreateLittleContraptions.LOGGER.info("=== Block Entity Debug Info ===");
                for (BlockEntity blockEntity : renderedBEs) {
                    String className = blockEntity.getClass().getName();
                    CreateLittleContraptions.LOGGER.info("Found block entity: {} at {}", className, blockEntity.getBlockPos());
                    
                    // Check detection logic
                    boolean containsLittleTiles = className.contains("littletiles");
                    boolean containsBETiles = className.contains("BETiles");
                    boolean wouldBeDetected = containsLittleTiles && containsBETiles;
                    
                    CreateLittleContraptions.LOGGER.info("  - Contains 'littletiles': {}", containsLittleTiles);
                    CreateLittleContraptions.LOGGER.info("  - Contains 'BETiles': {}", containsBETiles);
                    CreateLittleContraptions.LOGGER.info("  - Would be detected as LittleTiles: {}", wouldBeDetected);
                }
                CreateLittleContraptions.LOGGER.info("=== End Block Entity Debug Info ===");
            }
            
            for (BlockEntity blockEntity : renderedBEs) {
                try {
                    String className = blockEntity.getClass().getName();
                    
                    // Check if this is a LittleTiles block (for statistics)
                    if (className.contains("littletiles")) {
                        littleTilesCount++;
                        CreateLittleContraptions.LOGGER.info("Detected LittleTiles block entity: {}", className);
                    }
                    
                    // Attempt to bake a model for this block entity
                    Optional<BakedModel> bakedModel = LittleTilesModelBaker.bake(blockEntity);
                    
                    if (bakedModel.isPresent()) {
                        // Store the baked model in cache
                        ContraptionModelCache.cacheModel(contraptionId, blockEntity.getBlockPos(), bakedModel.get());
                        bakedCount++;
                        CreateLittleContraptions.LOGGER.info("Successfully baked model for block entity {} at {}", 
                                                            className, blockEntity.getBlockPos());
                    }
                    
                } catch (Exception e) {
                    CreateLittleContraptions.LOGGER.warn("Failed to bake model for block entity at {}: {}", 
                                                       blockEntity.getBlockPos(), e.getMessage());
                }
            }
            
            CreateLittleContraptions.LOGGER.info("Model baking completed for contraption {}: {} LittleTiles blocks found, {} models baked", 
                                                contraptionId, littleTilesCount, bakedCount);
            
        } catch (Exception e) {
            CreateLittleContraptions.LOGGER.error("Error during model baking for contraption {}: {}", 
                                                 contraptionId, e.getMessage());
        }
    }
    
    /**
     * Get statistics about the model baking process for debugging.
     */
    public static void logModelBakingStatistics() {
        int totalCached = ContraptionModelCache.getTotalCachedModels();
        int contraptionCount = ContraptionModelCache.getCachedContraptionCount();
        
        CreateLittleContraptions.LOGGER.info("=== Model Baking Statistics ===");
        CreateLittleContraptions.LOGGER.info("Total cached models: {}", totalCached);
        CreateLittleContraptions.LOGGER.info("Contraptions with cached models: {}", contraptionCount);
        
        if (totalCached > 0) {
            CreateLittleContraptions.LOGGER.info("Model baking system is functioning correctly");
        } else {
            CreateLittleContraptions.LOGGER.warn("No models have been cached yet - check LittleTiles integration");
        }
    }
}
