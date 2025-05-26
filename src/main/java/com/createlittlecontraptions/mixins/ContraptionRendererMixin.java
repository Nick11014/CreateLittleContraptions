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
 * Based on Gemini AI analysis - targeting ContraptionKineticRenderer.renderBlockEntity for proper integration.
 * This implements the refined approach suggested by Gemini to target the specific method that
 * handles individual BlockEntity rendering within contraptions.
 */
@Mixin(targets = "com.simibubi.create.content.contraptions.render.ContraptionKineticRenderer", remap = false)
public class ContraptionRendererMixin {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Hook into Create's contraption rendering to intercept LittleTiles blocks.
     * Updated to target ContraptionKineticRenderer.renderBlockEntity based on Gemini's analysis.
     * 
     * This method signature is based on Gemini's analysis of Create 6.0.4 structure.
     * If the exact signature differs, it will need to be adjusted based on the actual Create source.
     */
    @Inject(
        method = "renderBlockEntity(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILcom/simibubi/create/content/contraptions/Contraption$BlockInfo;Lcom/simibubi/create/content/contraptions/Contraption;F)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void onRenderContraptionBlockEntity(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int light,
        int overlay,
        Object blockInfo, // Using Object since we can't import Create classes directly
        Object contraption,
        float partialTicks,
        CallbackInfo ci
    ) {
        try {
            // Access BlockInfo fields using reflection since we can't import Create classes
            java.lang.reflect.Field stateField = blockInfo.getClass().getField("state");
            net.minecraft.world.level.block.state.BlockState blockState = 
                (net.minecraft.world.level.block.state.BlockState) stateField.get(blockInfo);
            
            if (isLittleTilesBlock(blockState)) {
                LOGGER.info("🎯 Intercepted LittleTiles block entity rendering in contraption: {}", 
                    blockState.getBlock().getClass().getName());
                
                // Get NBT data from BlockInfo
                java.lang.reflect.Field nbtField = blockInfo.getClass().getField("nbt");
                CompoundTag tileNBT = (CompoundTag) nbtField.get(blockInfo);
                
                if (tileNBT != null && !tileNBT.isEmpty()) {
                    // Get Level from contraption or fallback to client level
                    net.minecraft.world.level.Level world = getWorldFromContraption(contraption);
                    if (world == null) {
                        world = net.minecraft.client.Minecraft.getInstance().level;
                    }
                    
                    if (world != null) {
                        // Use our custom renderer
                        LittleTilesContraptionRenderer.renderLittleTileInContraption(
                            poseStack, bufferSource, light, overlay, blockState, tileNBT, world
                        );
                        
                        // Cancel Create's default rendering for this BlockEntity
                        ci.cancel();
                        LOGGER.debug("✅ Successfully rendered LittleTiles block and cancelled Create's rendering");
                    } else {
                        LOGGER.warn("Could not obtain world context for LittleTiles rendering in contraption");
                    }
                } else {
                    LOGGER.warn("LittleTiles block found but no NBT data available");
                }
            }
            
        } catch (Exception e) {
            // Don't log errors too frequently to avoid spam
            if (System.currentTimeMillis() % 5000 < 100) { // Log roughly every 5 seconds
                LOGGER.error("Error in ContraptionRendererMixin (targeting ContraptionKineticRenderer): ", e);
            }
        }
    }
    
    /**
     * Fallback method targeting with broader signature in case the specific method signature differs
     */
    @Inject(method = "*", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onAnyRenderMethod(CallbackInfo ci) {
        // This is a broad fallback that will catch any method call
        // Used for debugging if the specific method signature doesn't match
        // Will be removed once the correct signature is confirmed
    }
    
    /**
     * Helper method to get Level from contraption object using reflection
     */
    private static net.minecraft.world.level.Level getWorldFromContraption(Object contraption) {
        try {
            if (contraption == null) return null;
            
            // Try common method names to get Level from contraption
            String[] possibleMethods = {"getLevel", "getWorld", "level", "world"};
            
            for (String methodName : possibleMethods) {
                try {
                    java.lang.reflect.Method getMethod = contraption.getClass().getMethod(methodName);
                    Object result = getMethod.invoke(contraption);
                    if (result instanceof net.minecraft.world.level.Level) {
                        return (net.minecraft.world.level.Level) result;
                    }
                } catch (Exception e) {
                    // Try next method
                }
            }
            
            // Try field access
            String[] possibleFields = {"level", "world"};
            for (String fieldName : possibleFields) {
                try {
                    java.lang.reflect.Field field = contraption.getClass().getField(fieldName);
                    Object result = field.get(contraption);
                    if (result instanceof net.minecraft.world.level.Level) {
                        return (net.minecraft.world.level.Level) result;
                    }
                } catch (Exception e) {
                    // Try next field
                }
            }
            
        } catch (Exception e) {
            // Silent fail - will use fallback
        }
        
        return null;
    }    
    /**
     * Utility method to check if a BlockState represents a LittleTiles block.
     * Updated to use the correct team.creative.littletiles package names.
     */
    private static boolean isLittleTilesBlock(BlockState blockState) {
        if (blockState == null || blockState.getBlock() == null) {
            return false;
        }
        
        String blockName = blockState.getBlock().getClass().getName().toLowerCase();
        return blockName.contains("team.creative.littletiles") ||
               blockName.contains("littletile") ||
               blockName.contains("littleblock");
    }
}
