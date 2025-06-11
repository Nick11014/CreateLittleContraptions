package com.createlittlecontraptions.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.createlittlecontraptions.rendering.LittleTilesRenderingLogic;

/**
 * Mixin to intercept BlockEntity rendering and apply LittleTiles compatibility.
 * Inspired by create_interactive mod's approach to contraption rendering.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class LittleTilesContraptionRenderMixin {
      /**
     * Intercept BlockEntity rendering to handle LittleTiles blocks in contraptions.
     * Uses the full method signature for better compatibility with obfuscated mappings.
     */
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", 
            at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void onBlockEntityRender(
            E blockEntity, 
            float partialTick, 
            PoseStack poseStack, 
            MultiBufferSource bufferSource, 
            CallbackInfo ci) {
        
        // Delegate to our logic class
        LittleTilesRenderingLogic.handleBlockEntityRender(
            blockEntity, partialTick, poseStack, bufferSource, ci);
    }
}
