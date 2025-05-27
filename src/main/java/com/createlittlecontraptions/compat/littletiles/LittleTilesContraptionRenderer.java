package com.createlittlecontraptions.compat.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Renderer for LittleTiles blocks when moved by Create contraptions.
 * This uses the Direct Structure Rendering approach to bypass VirtualRenderWorld limitations.
 */
public class LittleTilesContraptionRenderer {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTRenderer");
    private static boolean initialized = false;      // ULTRA AGGRESSIVE throttling for logging - only log once every 5 minutes
    private static long lastLogTime = 0;
    private static final long LOG_INTERVAL_MS = 300000; // 5 minutes
    
    /**
     * Check if enough time has passed to allow logging (throttling)
     */
    private static boolean shouldLog() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime >= LOG_INTERVAL_MS) {
            lastLogTime = currentTime;
            return true;
        }
        return false;
    }

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
    }    /**
     * Renders a LittleTiles block within a Create contraption using Direct Structure Rendering.
     * This method is called by LittleTilesMovementBehaviour.renderInContraption.
     * 
     * @param context MovementContext containing the block data and NBT
     * @param renderWorld VirtualRenderWorld (limited, used only for registry access)
     * @param matrices ContraptionMatrices for positioning and lighting
     * @param buffer MultiBufferSource for rendering
     * @param partialTicks Partial tick value for smooth animation
     * @return true if rendering was attempted, false if no data to render
     */    /**
     * NEW DIRECT APPROACH: Render LittleTiles during contraption movement.
     * This bypasses all LittleTiles internal systems (including BERenderManager) 
     * and uses only Minecraft's core rendering system.
     */    public static boolean renderMovementBehaviourTile(MovementContext context, VirtualRenderWorld renderWorld,
                                                    ContraptionMatrices matrices, MultiBufferSource bufferSource, float partialTicks) {
        if (shouldLog()) {
            LOGGER.info("🎨 [CLC Renderer] NEW DIRECT APPROACH for LittleTiles at {}", context.localPos);
        }

        // Check if we have NBT data
        CompoundTag nbt = context.blockEntityData;
        if (nbt == null || nbt.isEmpty()) {
            if (shouldLog()) {
                LOGGER.warn("⚠️ [CLC Renderer] No NBT data for {}", context.localPos);
            }
            return false;
        }

        try {            // NEW APPROACH: Render using only Minecraft systems, no LittleTiles internals
            return renderWithMinecraftOnly(context, matrices, bufferSource, nbt, partialTicks);

        } catch (Exception e) {
            LOGGER.error("❌ [CLC Renderer] Error in NEW DIRECT APPROACH for " + context.localPos, e);
            return false;
        }
    }

    /**
     * Render using ONLY Minecraft's rendering system.
     * No LittleTiles APIs, no BERenderManager, no complex parsing.
     */    private static boolean renderWithMinecraftOnly(MovementContext context, ContraptionMatrices matrices, 
                                                  MultiBufferSource bufferSource, CompoundTag nbt, float partialTicks) {
        try {
            if (shouldLog()) {
                LOGGER.info("🔧 [CLC Renderer] Starting Minecraft-only rendering for {}", context.localPos);
                LOGGER.info("📄 [CLC Renderer] NBT content: {}", nbt.toString());
            }

            // Get transformation matrix
            PoseStack poseStack = matrices.getViewProjection();
            poseStack.pushPose();
            
            // Position within contraption
            poseStack.translate(context.localPos.getX(), context.localPos.getY(), context.localPos.getZ());

            // Basic lighting and overlay
            int packedLight = LightTexture.FULL_BRIGHT;
            int packedOverlay = OverlayTexture.NO_OVERLAY;            // STEP 1: Try to render via NBT data directly
            if (nbt.contains("grid") && nbt.contains("tiles")) {
                // We have LittleTiles NBT data - attempt basic visualization
                if (shouldLog()) {
                    LOGGER.info("🎯 [CLC Renderer] Found LittleTiles NBT data, creating basic visualization");
                }
                
                // Create a simple colored block representation
                var buffer = bufferSource.getBuffer(RenderType.solid());
                // TODO: Implement LittleTiles-specific rendering here
                // For now, just render as a colored indicator
                
            } else {
                if (shouldLog()) {
                    LOGGER.info("🔄 [CLC Renderer] No LittleTiles data, rendering basic block placeholder");
                }
                
                // Render a simple placeholder block
                var buffer = bufferSource.getBuffer(RenderType.solid());
                // TODO: Implement basic block rendering here
            }

            poseStack.popPose();

            if (shouldLog()) {
                LOGGER.info("✅ [CLC Renderer] Minecraft-only rendering completed for {}", context.localPos);
            }
            
            return true;

        } catch (Exception e) {
            LOGGER.error("❌ [CLC Renderer] Error in Minecraft-only rendering for " + context.localPos, e);
            return false;
        }
    }
}