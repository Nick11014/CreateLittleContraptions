package com.createlittlecontraptions.mixins.client;

import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import com.createlittlecontraptions.util.ContraptionDetector;
import com.createlittlecontraptions.util.LittleTilesDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Mixin para BlockEntityRenderDispatcher que intercepta a renderização de block entities
 * e usa modelos cached quando é um LittleTiles dentro de uma contraption.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions.BlockEntityRenderDispatcherMixin");    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void onBlockEntityRender(E blockEntity, float partialTick, 
                                                             PoseStack poseStack, MultiBufferSource bufferSource, 
                                                             CallbackInfo ci) {
        try {
            // Check if this is a LittleTiles block entity
            if (!LittleTilesDetector.isLittleTilesBlockEntity(blockEntity)) {
                return; // Not LittleTiles, allow normal rendering
            }

            // Check if this block entity is in a contraption
            Optional<AbstractContraptionEntity> contraptionOpt = ContraptionDetector.getContraptionEntity(blockEntity);
            if (contraptionOpt.isEmpty()) {
                return; // Not in contraption, allow normal rendering
            }

            AbstractContraptionEntity contraptionEntity = contraptionOpt.get();
            BlockPos relativePos = ContraptionDetector.getRelativePosition(blockEntity, contraptionEntity);
            
            LOGGER.info("CLCLC: *** INTERCEPTING LITTLETILES RENDER! Block at {} in contraption {} ***", 
                       relativePos, contraptionEntity.getId());

            // Get the contraption object and check for cached model
            Object contraption = ContraptionDetector.getContraptionFromEntity(contraptionEntity);
            if (contraption instanceof IContraptionBakedModelCache duck) {
                Optional<Map<BlockPos, BakedModel>> cacheOpt = duck.getModelCache();
                
                if (cacheOpt.isPresent()) {
                    Map<BlockPos, BakedModel> cache = cacheOpt.get();
                    BakedModel cachedModel = cache.get(relativePos);
                    
                    if (cachedModel != null) {
                        LOGGER.info("CLCLC: *** USING CACHED MODEL FOR LITTLETILES! Model: {} ***", 
                                   cachedModel.getClass().getSimpleName());
                        
                        // TODO: Render the cached model here
                        // For now, we'll cancel the original rendering to prove it's working
                        renderCachedModel(cachedModel, blockEntity, poseStack, bufferSource, partialTick);
                        ci.cancel();
                        return;
                    }
                }
            }
            
            LOGGER.info("CLCLC: No cached model found for LittleTiles at {}, allowing normal rendering", relativePos);
            
        } catch (Exception e) {
            LOGGER.error("CLCLC: Error in BlockEntityRenderDispatcher mixin: {}", e.getMessage(), e);
            // Allow normal rendering on error
        }
    }

    /**
     * Renders a cached BakedModel in place of the original BlockEntity.
     * TODO: Implement actual model rendering
     */
    private void renderCachedModel(BakedModel model, BlockEntity blockEntity, 
                                  PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        try {
            LOGGER.info("CLCLC: *** RENDERING CACHED MODEL: {} ***", model.getClass().getSimpleName());
            
            // TODO: Implement actual BakedModel rendering here
            // For now, we just log that we're using the cached model
            // The rendering will be implemented in the next step
            
        } catch (Exception e) {
            LOGGER.error("CLCLC: Error rendering cached model: {}", e.getMessage(), e);
        }
    }
}
