package com.createlittlecontraptions.compat.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Renderer for LittleTiles blocks when moved by Create contraptions.
 * This uses the Direct Structure Rendering approach to bypass VirtualRenderWorld limitations.
 */
public class LittleTilesContraptionRenderer {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTRenderer");
    private static boolean initialized = false;

    /**
     * Initialize the renderer. Called by CreateRuntimeIntegration.
     */
    public static void initialize() {
        LOGGER.info("LittleTilesContraptionRenderer initialized");
        initialized = true;
    }

    /**
     * Check if the renderer is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Legacy method for compatibility with ContraptionRendererMixin.
     * This method is called by existing mixin code and delegates to our new approach.
     */
    public static void renderLittleTileBEInContraption(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource bufferSource,
            net.minecraft.world.level.Level level,
            net.minecraft.world.level.Level contraptionLevel,
            team.creative.littletiles.common.block.entity.BETiles blockEntity,
            float partialTicks,
            boolean moved) {
        
        LOGGER.debug("Legacy renderLittleTileBEInContraption called - this should not be used with Direct Structure Rendering");
        // This method is kept for compatibility but should not be used with the new approach
        // The rendering is now handled by renderMovementBehaviourTile via MovementBehaviour
    }

    /**
     * Renders a LittleTiles block within a Create contraption using Direct Structure Rendering.
     * This method is called by LittleTilesMovementBehaviour.renderInContraption.
     * 
     * @param context MovementContext containing the block data and NBT
     * @param renderWorld VirtualRenderWorld (limited, used only for registry access)
     * @param matrices ContraptionMatrices for positioning and lighting
     * @param buffer MultiBufferSource for rendering
     * @return true if rendering was attempted, false if no data to render
     */
    public static boolean renderMovementBehaviourTile(MovementContext context, VirtualRenderWorld renderWorld,
                                                    ContraptionMatrices matrices, MultiBufferSource buffer) {
        boolean hasNBT = context.blockEntityData != null && !context.blockEntityData.isEmpty();
        LOGGER.info("🎨 [CLC Renderer] Starting renderMovementBehaviourTile for: {} with NBT (exists? {})", 
                   context.localPos, hasNBT);

        if (!hasNBT) {
            LOGGER.warn("⚠️ [CLC Renderer] No NBT data found for: {}", context.localPos);
            return false; // No data to render
        }        try {
            // Use the Direct Structure Rendering facade to parse NBT
            // For now, we'll try without the HolderLookup.Provider to test the basic approach
            LittleTilesAPIFacade.ParsedLittleTilesData parsedStructures = LittleTilesAPIFacade.parseStructuresFromNBT(
                context.blockEntityData, 
                context.state, 
                context.localPos, 
                null // TODO: Find proper way to get HolderLookup.Provider in MovementBehaviour context
            );

            if (parsedStructures == null) {
                LOGGER.warn("⚠️ [CLC Renderer] Failed to parse structures from NBT for {}. Aborting render.", context.localPos);
                return false;
            }
            LOGGER.debug("[CLC Renderer] Successfully parsed NBT structures for {}", context.localPos);

            // Prepare the PoseStack for rendering
            PoseStack poseStack = matrices.getViewProjection(); // Get the contraption's transformation matrix
            poseStack.pushPose();
            
            // Apply local translation for this specific block within the contraption
            poseStack.translate(context.localPos.getX(), context.localPos.getY(), context.localPos.getZ());            // Calculate lighting - this is complex in MovementBehaviour context
            // For now, use FULL_BRIGHT as a placeholder until we find the proper lighting method
            // TODO: Investigate ContraptionMatrices for proper lighting information
            int packedLight = LightTexture.FULL_BRIGHT; // Placeholder - NEEDS PROPER IMPLEMENTATION
            int packedOverlay = OverlayTexture.NO_OVERLAY;
            float partialTicks = 1.0f; // Placeholder - will need proper partial tick value

            LOGGER.debug("[CLC Renderer] Attempting direct render for {} with light {} at {}", 
                        context.localPos, packedLight, poseStack.last().pose());

            // Perform the direct rendering using LittleTiles' own logic
            LittleTilesAPIFacade.renderDirectly(
                parsedStructures,
                poseStack,
                buffer,
                packedLight,
                packedOverlay,
                partialTicks
            );

            poseStack.popPose();
            LOGGER.info("✅ [CLC Renderer] Direct rendering attempted for: {}", context.localPos);
            return true; // Indicate rendering was attempted

        } catch (Exception e) {
            LOGGER.error("❌ [CLC Renderer] Unexpected error in renderMovementBehaviourTile for {}: {}", 
                        context.localPos, e.getMessage(), e);
            return false;
        }
    }
}