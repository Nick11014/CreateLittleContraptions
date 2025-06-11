package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import com.createlittlecontraptions.util.ContraptionDetector;
import com.createlittlecontraptions.util.PlaceholderBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mixin para ContraptionEntityRenderer.
 * Intercepta o método render() para detectar quando contraptions com modelos customizados
 * estão sendo renderizadas, permitindo validar se nosso sistema de cache está funcionando.
 */
@Mixin(value = ContraptionEntityRenderer.class, remap = false)
public abstract class ContraptionEntityRendererMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions.ContraptionEntityRendererMixin");
    
    static {
        LOGGER.info("CLCLC: ContraptionEntityRendererMixin static initializer called - Mixin loaded");
    }    /**
     * Hook no método principal render() para detectar renderização de contraptions
     * com modelos customizados em nosso cache.
     */
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void onRender(AbstractContraptionEntity entity, float yaw, float partialTicks, 
                         PoseStack poseStack, MultiBufferSource buffers, int overlay, CallbackInfo ci) {
        try {
            LOGGER.info("CLCLC: *** CONTRAPTION ENTITY RENDER() CALLED! Entity ID: {} ***", entity.getId());
            
            // Tenta acessar o cache da contraption
            if (entity.getContraption() != null) {
                IContraptionBakedModelCache duck = (IContraptionBakedModelCache) entity.getContraption();
                Optional<Map<BlockPos, BakedModel>> cacheOpt = duck.getModelCache();
                
                int contraptionId = System.identityHashCode(entity.getContraption());
                LOGGER.info("CLCLC: Contraption Object ID: {} | Cache present: {}", 
                           contraptionId, cacheOpt.isPresent());
                  if (cacheOpt.isPresent()) {
                    Map<BlockPos, BakedModel> cache = cacheOpt.get();
                    LOGGER.info("CLCLC: *** CACHE SIZE: {} ***", cache.size());
                      // Auto-populate cache on client side if it's empty
                    if (cache.isEmpty()) {
                        LOGGER.info("CLCLC: *** CACHE IS EMPTY - AUTO-POPULATING ON CLIENT SIDE ***");
                        
                        // Get LittleTiles positions
                        List<BlockPos> ltPositions = ContraptionDetector.getLittleTilesPositions(entity);
                        LOGGER.info("CLCLC: Found {} LittleTiles positions for auto-population", ltPositions.size());
                        
                        if (!ltPositions.isEmpty()) {
                            // Populate cache with placeholder models
                            for (BlockPos pos : ltPositions) {
                                cache.put(pos, PlaceholderBakedModel.INSTANCE);
                            }
                            
                            // Update the cache in the duck interface
                            IContraptionBakedModelCache cacheDuck = (IContraptionBakedModelCache) entity.getContraption();
                            cacheDuck.setModelCache(cache);
                            
                            LOGGER.info("CLCLC: *** AUTO-POPULATION COMPLETE - NEW CACHE SIZE: {} ***", cache.size());
                        } else {
                            LOGGER.info("CLCLC: No LittleTiles positions found, cache remains empty");
                        }
                    }
                    
                    if (!cache.isEmpty()) {
                        LOGGER.info("CLCLC: *** CONTRAPTION ENTITY RENDERING WITH CACHED MODELS! ***");
                        
                        // Count non-placeholder models
                        long customModels = cache.values().stream()
                            .filter(model -> !model.getClass().getSimpleName().equals("PlaceholderBakedModel"))
                            .count();
                          LOGGER.info("CLCLC: *** {} CUSTOM LITTLETILES MODELS IN RENDERING PIPELINE! ***", customModels);
                        
                        // Log cache details - proof that LittleTiles are being detected and cached
                        cache.forEach((pos, model) -> {
                            String modelType = model.getClass().getSimpleName();
                            LOGGER.info("CLCLC: *** CACHED LITTLETILES *** Position {} -> {}", pos, modelType);
                        });
                    } else {
                        LOGGER.info("CLCLC: Cache remains empty after auto-population attempt");
                    }
                } else {
                    LOGGER.info("CLCLC: No cache present");
                }
            } else {
                LOGGER.info("CLCLC: Entity has no contraption");
            }        } catch (Exception e) {
            LOGGER.error("CLCLC: Error in ContraptionEntityRenderer hook: {}", e.getMessage(), e);
        }
    }
}
