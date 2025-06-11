package com.createlittlecontraptions.rendering;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.createlittlecontraptions.rendering.cache.ContraptionModelCache;
import com.createlittlecontraptions.util.LittleTilesDetector;
import com.createlittlecontraptions.util.ContraptionDetector;

/**
 * Main logic class for handling LittleTiles rendering in contraptions.
 * Separates mixin concerns from business logic following create_interactive pattern.
 */
public class LittleTilesRenderingLogic {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean debugMode = false;
      public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }
    
    public static boolean isDebugModeEnabled() {
        return debugMode;
    }
    
    /**
     * Main entry point called by the mixin to handle BlockEntity rendering.
     * Decides whether to use cached BakedModel or allow normal rendering.
     */
    public static <E extends BlockEntity> void handleBlockEntityRender(
            E blockEntity, 
            float partialTick, 
            PoseStack poseStack, 
            MultiBufferSource bufferSource, 
            CallbackInfo ci) {
        
        try {
            // Step 1: Check if this is a LittleTiles block entity
            if (!LittleTilesDetector.isLittleTilesBlockEntity(blockEntity)) {
                // Not a LittleTiles block - allow normal rendering
                return;
            }
            
            // Step 2: Check if this block entity is part of a contraption
            var contraptionInfo = ContraptionDetector.findContainingContraption(blockEntity);
            if (contraptionInfo.isEmpty()) {
                // Not in a contraption - allow normal rendering
                if (debugMode) {
                    LOGGER.debug("LittleTiles block {} not in contraption, using normal rendering", 
                        blockEntity.getBlockPos());
                }
                return;
            }
            
            var contraptionEntity = contraptionInfo.get();
            BlockPos relativePos = ContraptionDetector.getRelativePosition(blockEntity, contraptionEntity);
              // Step 3: Check if we have a cached BakedModel for this position
            var cachedModelOpt = ContraptionModelCache.getModel(
                contraptionEntity.getUUID(), relativePos);
            
            if (cachedModelOpt.isPresent()) {
                BakedModel cachedModel = cachedModelOpt.get();
                
                // Step 4: Use our cached BakedModel instead of normal rendering
                if (debugMode) {
                    LOGGER.debug("Using cached BakedModel for LittleTiles block at {} in contraption {}", 
                        relativePos, contraptionEntity.getUUID());
                }
                
                // Render the cached model
                renderCachedModel(cachedModel, blockEntity, poseStack, bufferSource, partialTick);
                
                // Cancel the original rendering
                ci.cancel();
            } else {
                // No cached model available - allow normal rendering but log it
                if (debugMode) {
                    LOGGER.debug("No cached model for LittleTiles block at {} in contraption {}", 
                        relativePos, contraptionEntity.getUUID());
                }
            }
            
        } catch (Exception e) {
            // If anything goes wrong, allow normal rendering and log the error
            LOGGER.warn("Error in LittleTiles rendering logic for block at {}: {}", 
                blockEntity.getBlockPos(), e.getMessage());
            if (debugMode) {
                LOGGER.debug("Full error trace:", e);
            }
        }
    }
    
    /**
     * Render a cached BakedModel in place of the original BlockEntity.
     */
    private static void renderCachedModel(
            BakedModel model, 
            BlockEntity blockEntity, 
            PoseStack poseStack, 
            MultiBufferSource bufferSource, 
            float partialTick) {
        
        try {
            // TODO: Implement actual BakedModel rendering
            // This is where we would use Minecraft's model rendering system
            // to draw our cached BakedModel at the correct position
            
            if (debugMode) {
                LOGGER.debug("Rendering cached BakedModel for block at {}", blockEntity.getBlockPos());
            }
            
            // For now, we just cancel the original rendering
            // The actual BakedModel rendering will be implemented in the next step
            
        } catch (Exception e) {
            LOGGER.warn("Error rendering cached BakedModel for block at {}: {}", 
                blockEntity.getBlockPos(), e.getMessage());
        }
    }
}
