package com.createlittlecontraptions.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.createlittlecontraptions.utils.LittleTilesHelper;
import com.createlittlecontraptions.utils.RenderContext;
import com.createlittlecontraptions.compat.littletiles.LittleTilesContraptionRenderer;
import team.creative.littletiles.common.block.entity.BETiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;

/**
 * Mixin to intercept BlockEntity rendering in Create contraptions and provide custom rendering for LittleTiles.
 * This targets the BlockEntityRenderHelper.renderBlockEntities method in Create mod.
 * 
 * Updated approach based on Gemini's detailed analysis:
 * - Uses @Inject with LocalCapture to intercept BEFORE renderer.render() is called
 * - Captures local variables to access the proper context for each BlockEntity
 * - Cancels vanilla rendering for LittleTiles while allowing others to render normally
 */
@Mixin(com.simibubi.create.foundation.render.BlockEntityRenderHelper.class)
public class ContraptionRendererMixin {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/Mixin");

    private static final String RENDER_BLOCK_ENTITIES_METHOD_SIGNATURE =        "(Lnet/minecraft/world/level/Level;" +
        "Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;" +
        "Ljava/lang/Iterable;" +
        "Lcom/mojang/blaze3d/vertex/PoseStack;" +
        "Lorg/joml/Matrix4f;" +
        "Lnet/minecraft/client/renderer/MultiBufferSource;" +
        "F)V";    // Inject at the HEAD to see the incoming BlockEntities (debugging) and capture context
    @Inject(method = "renderBlockEntities" + RENDER_BLOCK_ENTITIES_METHOD_SIGNATURE, at = @At("HEAD"))
    private static void clc_onRenderBlockEntitiesHead(
        Level realLevel, 
        @Nullable VirtualRenderWorld renderLevel,
        Iterable<BlockEntity> customRenderBEs, 
        PoseStack ms, 
        @Nullable Matrix4f lightTransform, 
        MultiBufferSource buffer,
        float pt, 
        CallbackInfo ci) {        // Capture context for later use (no logging for performance)
        currentContext.set(new RenderContext(realLevel, renderLevel, lightTransform));

        // Only log when we find LittleTiles BlockEntities (important events only)
        for (BlockEntity be : customRenderBEs) {
            if (be != null && LittleTilesHelper.isLittleTilesBlockEntity(be)) {
                LOGGER.info("[CLC Mixin HEAD] Found LittleTiles BlockEntity: {} at {}", 
                    be.getClass().getSimpleName(), be.getBlockPos().toString());
            }
        }}    // ThreadLocal for context passing
    private static final ThreadLocal<RenderContext> currentContext = new ThreadLocal<>();

    // Main injection - RIGHT BEFORE renderer.render() call
    @Inject(
        method = "renderBlockEntities" + RENDER_BLOCK_ENTITIES_METHOD_SIGNATURE,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void clc_beforeRenderBlockEntityVanilla(
        Level realLevel, @Nullable VirtualRenderWorld renderLevel,
        Iterable<BlockEntity> customRenderBEs, PoseStack msOuter,
        @Nullable Matrix4f lightTransform, MultiBufferSource bufferOuter,
        float ptOuter,
        CallbackInfo ci,
        // Captured locals - types and order must match bytecode exactly based on error message
        java.util.Set<?> visibleBlockEntities,
        java.util.Iterator<?> iterator,
        BlockEntity blockEntity,
        BlockEntityRenderer<BlockEntity> renderer,
        net.minecraft.core.BlockPos pos,
        int light1,
        int light2    ) {
        // Only process LittleTiles BlockEntities (silent check for performance)
        if (LittleTilesHelper.isLittleTilesBlockEntity(blockEntity)) {
            LOGGER.info("[CLC Mixin] Intercepting LittleTiles BE: {} at {}", 
                blockEntity.getClass().getSimpleName(), blockEntity.getBlockPos());
            
            // Retrieve context
            RenderContext capturedContext = currentContext.get(); 
            Level effectiveRealLevel = realLevel;
            VirtualRenderWorld effectiveRenderLevel = renderLevel;
            Matrix4f effectiveLightTransform = lightTransform;

            if (capturedContext != null) {
                effectiveRealLevel = capturedContext.realLevel;
                effectiveRenderLevel = capturedContext.renderLevel;
                effectiveLightTransform = capturedContext.lightTransform;
            } else {
                LOGGER.warn("[CLC Mixin PRE-RENDER VANILLA] RenderContext was null! Using direct parameters.");
            }            try {
                // Cast BlockEntity to BETiles if possible
                BETiles betiles = null;
                if (blockEntity instanceof BETiles) {
                    betiles = (BETiles) blockEntity;
                } else {
                    LOGGER.warn("[CLC Mixin PRE-RENDER VANILLA] BlockEntity is not BETiles: {}", blockEntity.getClass().getSimpleName());
                    return; // Can't render non-BETiles as LittleTiles
                }
                  // Convert Matrix4f to boolean (lightTransform parameter)
                boolean useLightTransform = (effectiveLightTransform != null);
                
                // Cast VirtualRenderWorld to Level for the method call
                Level renderLevelAsLevel = effectiveRenderLevel; // VirtualRenderWorld extends Level
                
                LittleTilesContraptionRenderer.renderLittleTileBEInContraption(
                    msOuter,                // PoseStack from method parameters (outer matrix)
                    bufferOuter,            // MultiBufferSource from method parameters
                    effectiveRealLevel,     // Level (real level)
                    renderLevelAsLevel,     // Level (render level cast from VirtualRenderWorld)
                    betiles,                // The BETiles instance
                    ptOuter,                // Partial ticks from method parameters
                    useLightTransform       // Light transform flag
                );
                ci.cancel(); // Prevent the original renderer.render() call
                // Success - rendering completed silently
            } catch (Exception e) {
                LOGGER.error("[CLC Mixin PRE-RENDER VANILLA] Error during custom LittleTiles rendering: ", e);
                // Do not cancel, let original try to render to avoid cascade failure
            }
        }
    }

    // Simplified approach - remove LocalCapture that's causing crashes
    // TODO: Re-implement with proper LocalCapture signatures once we identify the exact structure

    // Keep the @Redirect for comparison and fallback (should not be hit for LittleTiles if @Inject works)
    @Redirect(
        method = "renderBlockEntities" + RENDER_BLOCK_ENTITIES_METHOD_SIGNATURE, 
        at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
        )
    )
    private static void clc_redirectRenderBlockEntity(
        BlockEntityRenderer<BlockEntity> instance, 
        BlockEntity blockEntity,                   
        float partialTicks, 
        PoseStack poseStack, 
        MultiBufferSource bufferSource, 
        int combinedLight, 
        int combinedOverlay) {        // Silent redirect - only log important events
        if (LittleTilesHelper.isLittleTilesBlockEntity(blockEntity)) {
            // This path should ideally not be taken if the @Inject above successfully cancels for LittleTiles
            LOGGER.warn("[CLC Mixin REDIRECT] Reached redirect for LittleTiles BE, meaning @Inject before it didn't fully handle/cancel it. BE: {}", blockEntity.getBlockPos());
            
            // Fallback: Call original for safety if this path is unexpectedly hit for LittleTiles
            instance.render(blockEntity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        } else {
            // Normal path for non-LittleTiles BEs
            instance.render(blockEntity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);        }
    }

    // Cleanup context at the end of the method
    @Inject(method = "renderBlockEntities" + RENDER_BLOCK_ENTITIES_METHOD_SIGNATURE, at = @At("TAIL"))
    private static void clc_cleanupContextAtTail(
        Level realLevel, @Nullable VirtualRenderWorld renderLevel,
        Iterable<BlockEntity> customRenderBEs, PoseStack ms, 
        @Nullable Matrix4f lightTransform, MultiBufferSource buffer,
        float pt,
        CallbackInfo ci) {
          // Clean up context silently for performance
        currentContext.remove();
    }
}
