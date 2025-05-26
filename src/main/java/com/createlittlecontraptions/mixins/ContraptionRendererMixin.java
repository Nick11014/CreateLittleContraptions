package com.createlittlecontraptions.mixins;

import java.util.Collection;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.createlittlecontraptions.compat.create.CreateRuntimeIntegration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Mixin to intercept Create's contraption block entity rendering.
 * 
 * This targets the correct Create 6.0.4 method: BlockEntityRenderHelper.renderBlockEntities
 * which is called by ContraptionEntityRenderer to render all block entities in contraptions.
 * 
 * Based on GitHub analysis of Create 6.0.4 source code.
 */
@Mixin(targets = "com.simibubi.create.foundation.render.BlockEntityRenderHelper", remap = false)
public class ContraptionRendererMixin {

    @Inject(method = "renderBlockEntities(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;Ljava/lang/Iterable;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;F)V",
            at = @At("HEAD"))
    private static void onRenderBlockEntities(Level realLevel, @Nullable VirtualRenderWorld renderLevel,
                                              Iterable<BlockEntity> customRenderBEs, PoseStack ms, 
                                              @Nullable Matrix4f lightTransform, MultiBufferSource buffer,
                                              float pt, CallbackInfo ci) {
        try {
            System.out.println("[CreateLittleContraptions] ==========================================");
            System.out.println("[CreateLittleContraptions] MIXIN INTERCEPTED! renderBlockEntities called!");
            System.out.println("[CreateLittleContraptions] Real level: " + realLevel);
            System.out.println("[CreateLittleContraptions] Render level: " + renderLevel);
            
            if (customRenderBEs instanceof Collection<?> collection) {
                System.out.println("[CreateLittleContraptions] Block entities count: " + collection.size());
            }
            
            // Process each block entity
            int count = 0;
            for (BlockEntity be : customRenderBEs) {
                if (be != null) {
                    count++;
                    BlockPos pos = be.getBlockPos();                    System.out.println("[CreateLittleContraptions] BE #" + count + " at " + pos + ": " + be.getClass().getSimpleName());
                    
                    // Check if this is a LittleTiles block entity
                    CreateRuntimeIntegration.handleLittleTilesBERendering(realLevel, pos, be);
                }
            }
            System.out.println("[CreateLittleContraptions] ==========================================");
        } catch (Exception e) {
            System.err.println("[CreateLittleContraptions] Error in ContraptionRendererMixin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
