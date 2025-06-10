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
    }
    
    /**
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
            
            for (BlockEntity blockEntity : renderedBEs) {
                try {
                    // Attempt to bake a model for this block entity
                    Optional<BakedModel> bakedModel = LittleTilesModelBaker.bake(blockEntity);
                    
                    if (bakedModel.isPresent()) {
                        // Store the baked model in cache
                        ContraptionModelCache.cacheModel(contraptionId, blockEntity.getBlockPos(), bakedModel.get());
                        bakedCount++;
                        CreateLittleContraptions.LOGGER.debug("Successfully baked model for LittleTiles block at {}", 
                                                            blockEntity.getBlockPos());
                    }
                    
                    // Check if this was a LittleTiles block (for statistics)
                    String className = blockEntity.getClass().getName();
                    if (className.contains("littletiles")) {
                        littleTilesCount++;
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
}
