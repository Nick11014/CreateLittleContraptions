package com.createlittlecontraptions.mixins;

import com.createlittlecontraptions.CreateLittleContraptions;
import com.createlittlecontraptions.rendering.ContraptionBlockRenderController;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ContraptionEntityRenderer.class)
public class ContraptionEntityRendererFilterMixin {
    private static final Logger LOGGER = LogManager.getLogger(CreateLittleContraptions.MODID + " ContraptionEntityRendererFilterMixin");    @Inject(
        method = "renderBlockEntities",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void onRenderBlockEntitiesFilter(Level level,
                                                   VirtualRenderWorld renderWorld,
                                                   Contraption c,
                                                   ContraptionMatrices matrices,
                                                   MultiBufferSource buffer,
                                                   CallbackInfo ci) {
        
        // Get contraption UUID from the matrices or contraption
        // For now, we'll use a method to extract or determine the UUID
        java.util.UUID contraptionUUID = getContraptionUUID(c);
        
        // Individual block entity rendering control using the centralized controller
        Collection<BlockEntity> originalBEs = c.getRenderedBEs();
        List<BlockEntity> filtered = originalBEs.stream()
            .filter(be -> shouldRenderBlockEntity(be, contraptionUUID))
            .collect(Collectors.toList());

        int hiddenCount = originalBEs.size() - filtered.size();
        if (hiddenCount > 0) {
            CreateLittleContraptions.LOGGER.info("🎮 [RENDER CONTROL] Filtered {} block entities from contraption {} rendering", 
                hiddenCount, contraptionUUID);
            
            // Debug: show what types were filtered
            originalBEs.stream()
                .filter(be -> !shouldRenderBlockEntity(be, contraptionUUID))
                .forEach(be -> CreateLittleContraptions.LOGGER.debug("  - Filtered: {} at {}", 
                    be.getClass().getSimpleName(), be.getBlockPos()));
        }

        // Log the filtered block entities for debugging
        LOGGER.debug("Filtered Block Entities: " + filtered);

        // Call original helper with filtered list
        BlockEntityRenderHelper.renderBlockEntities(
            level,
            renderWorld,
            filtered,
            matrices.getModelViewProjection(),
            matrices.getLight(),
            buffer
        );

        // Prevent the unfiltered call
        ci.cancel();
    }    /**
     * Determines if a specific block entity should be rendered in contraptions.
     * This is the central control point for individual block rendering using the centralized controller.
     * 
     * @param be The block entity to check
     * @param contraptionUUID The UUID of the contraption containing this block entity
     * @return true if should be rendered, false to hide
     */
    private static boolean shouldRenderBlockEntity(BlockEntity be, java.util.UUID contraptionUUID) {
        // Use the centralized controller for rendering decisions
        boolean shouldRender = ContraptionBlockRenderController.shouldRenderBlockEntity(be, contraptionUUID);
        
        if (!shouldRender) {
            LOGGER.debug("Controller blocked rendering for {} at {} in contraption {}", 
                be.getClass().getSimpleName(), be.getBlockPos(), contraptionUUID);
        }
        
        return shouldRender;
    }
    
    /**
     * Extract or determine the UUID of a contraption.
     * For now, we'll use a hash-based approach since direct UUID access isn't available.
     */
    private static java.util.UUID getContraptionUUID(Contraption c) {
        // Generate a consistent UUID based on contraption content/hash
        // This is a workaround since Create doesn't expose contraption UUIDs directly
        // In practice, you might need to use reflection or other methods to get the actual entity UUID
        
        try {
            // Try to create a deterministic UUID based on contraption content
            String contraptionString = c.toString() + c.hashCode();
            return java.util.UUID.nameUUIDFromBytes(contraptionString.getBytes());
        } catch (Exception e) {
            // Fallback to a random UUID that will be consistent for this render pass
            return java.util.UUID.randomUUID();
        }
    }}
