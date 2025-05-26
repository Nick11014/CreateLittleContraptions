package com.createlittlecontraptions.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Import necessary classes (explicit types as recommended by Gemini)
import net.minecraft.world.level.Level;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

// Other imports for our logic
import com.createlittlecontraptions.compat.littletiles.LittleTilesContraptionRenderer;
import com.createlittlecontraptions.utils.LittleTilesHelper;
import com.createlittlecontraptions.utils.RenderContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mixin to intercept Create's contraption block entity rendering.
 * 
 * Updated approach based on Gemini's analysis:
 * - Using @Redirect to selectively replace only LittleTiles BE rendering
 * - Leaving other block entities to render normally through Create's pipeline
 * - More precise and efficient than canceling the entire method
 */
@Mixin(value = com.simibubi.create.foundation.render.BlockEntityRenderHelper.class, remap = false)
public class ContraptionRendererMixin {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/Mixin");

    // Store context for the current rendering operation
    private static ThreadLocal<RenderContext> currentContext = new ThreadLocal<>();
    
    /**
     * Inject at the start of renderBlockEntities to capture the context
     */
    @Inject(
        method = "renderBlockEntities(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;Ljava/lang/Iterable;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;F)V",
        at = @At(value = "HEAD")
    )
    private static void captureContext(
        Level level,
        VirtualRenderWorld virtualRenderWorld,
        Iterable<BlockEntity> blockEntities,
        PoseStack poseStack,
        Matrix4f matrix4f,
        MultiBufferSource multiBufferSource,
        float f,
        CallbackInfo ci
    ) {
        currentContext.set(new RenderContext(level, virtualRenderWorld, matrix4f));
    }
    
    /**
     * Clear the context at the end of renderBlockEntities
     */
    @Inject(
        method = "renderBlockEntities(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;Ljava/lang/Iterable;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;F)V",
        at = @At("RETURN")
    )
    private static void clearRenderContext(CallbackInfo ci) {
        // Clean up the context to prevent memory leaks
        currentContext.remove();
    }
    
    /**
     * Redirect the rendering of block entities
     */
    @Redirect(
        method = "renderBlockEntities(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;Ljava/lang/Iterable;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")
    )
    private static void redirectRenderBlockEntity(
        BlockEntityRenderer<BlockEntity> renderer,
        BlockEntity blockEntity, 
        float partialTicks, 
        PoseStack poseStack, 
        MultiBufferSource bufferSource, 
        int combinedLight, 
        int combinedOverlay
    ) {
        try {
            // Check if this is a LittleTiles block entity
            if (LittleTilesHelper.isLittleTilesBlockEntity(blockEntity)) {
                LOGGER.info("[CLC Mixin Redirect] Rendering LittleTiles BE: {} at {}", 
                    blockEntity.getClass().getSimpleName(), blockEntity.getBlockPos());
                
                // Get the current context (if we can access it)
                RenderContext context = currentContext.get();
                  // Use our custom LittleTiles renderer
                if (context != null) {
                    LittleTilesContraptionRenderer.renderLittleTileBEInContraption(
                        poseStack, 
                        bufferSource, 
                        context.realLevel, 
                        context.renderLevel, 
                        blockEntity, 
                        partialTicks, 
                        context.lightTransform,
                        combinedLight,    // Pass the Create-calculated light
                        combinedOverlay   // Pass the Create-calculated overlay
                    );
                } else {
                    // Fallback: use simplified rendering without full context
                    LOGGER.warn("[CLC Mixin Redirect] No context available, using fallback rendering");
                    LittleTilesContraptionRenderer.renderLittleTileBEInContraption(
                        poseStack, 
                        bufferSource, 
                        null, // realLevel
                        null, // renderLevel
                        blockEntity, 
                        partialTicks, 
                        null,  // lightTransform
                        combinedLight,    // Pass the Create-calculated light
                        combinedOverlay   // Pass the Create-calculated overlay
                    );
                }
                
                // Don't call the original renderer - our custom renderer handles it
                return;
            } else {
                // For non-LittleTiles block entities, use Create's normal rendering
                renderer.render(blockEntity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
            
        } catch (Exception e) {
            LOGGER.error("[CLC Mixin Redirect] Error in redirectRenderBlockEntity: ", e);
            // Fallback to original rendering on error
            try {
                renderer.render(blockEntity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
            } catch (Exception fallbackError) {
                LOGGER.error("[CLC Mixin Redirect] Fallback rendering also failed: ", fallbackError);
            }
        }    }
}
