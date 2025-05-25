package com.createlittlecontraptions.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.createlittlecontraptions.compat.littletiles.LittleTilesContraptionRenderer;

/**
 * Mixin to hook into Create's contraption rendering to intercept LittleTiles blocks
 * when they're being rendered as part of Create contraptions.
 * 
 * Based on Gemini AI analysis - targeting ContraptionRenderDispatcher for proper integration.
 * This implements refined Mixin targeting for specific rendering methods.
 */
@Mixin(targets = "com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher", remap = false)
public class ContraptionRendererMixin {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Hook into Create's contraption rendering to intercept LittleTiles blocks.
     * Updated to target a more specific method signature based on Gemini's analysis.
     * We're looking for methods that handle individual BlockInfo rendering.
     */
    @Inject(method = "*", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRenderContraptionBlock(CallbackInfo ci) {
        try {
            // Broad targeting for initial debugging - will be refined to specific method
            // Once we identify the correct method signature, we'll add proper parameters:
            // PoseStack poseStack, MultiBufferSource bufferSource, 
            // com.simibubi.create.content.contraptions.Contraption.BlockInfo blockInfo,
            // int light, int overlay
            
            LOGGER.debug("ContraptionRendererMixin: Intercepted Create rendering call");
            
        } catch (Exception e) {
            LOGGER.error("Error in ContraptionRendererMixin hook: ", e);
        }
    }
    
    /**
     * Utility method to check if a BlockState represents a LittleTiles block.
     * Updated to use the correct team.creative.littletiles package names.
     */
    private boolean isLittleTilesBlock(BlockState blockState) {
        if (blockState == null || blockState.getBlock() == null) {
            return false;
        }
        
        String blockName = blockState.getBlock().getClass().getName().toLowerCase();
        return blockName.contains("team.creative.littletiles") ||
               blockName.contains("littletile") ||
               blockName.contains("littleblock");
    }
    
    /**
     * Helper method to process LittleTiles blocks in contraptions.
     * This will be called once we have the proper method parameters.
     */
    private void processLittleTilesBlock(PoseStack poseStack, MultiBufferSource bufferSource,
                                       BlockState blockState, CompoundTag tileNbt,
                                       int light, int overlay, CallbackInfo ci) {
        try {
            LOGGER.info("Processing LittleTiles block in contraption: {}", blockState.getBlock().getClass().getName());
            
            if (tileNbt != null && !tileNbt.isEmpty()) {
                LOGGER.debug("LittleTiles NBT data available, size: {} tags", tileNbt.size());
                
                // Call the LittleTiles contraption renderer
                LittleTilesContraptionRenderer.renderLittleTileInContraption(
                    poseStack, bufferSource, light, overlay, blockState, tileNbt, 
                    net.minecraft.client.Minecraft.getInstance().level
                );
                
                // Cancel the original rendering to prevent double-rendering
                ci.cancel();
                LOGGER.debug("Cancelled original Create rendering for LittleTiles block");
            } else {
                LOGGER.warn("LittleTiles block found but no NBT data available");
            }
            
        } catch (Exception e) {
            LOGGER.error("Error processing LittleTiles block in contraption: ", e);
        }
    }
}
