package com.createlittlecontraptions.compat.create.behaviour;

import com.createlittlecontraptions.compat.littletiles.LittleTilesContraptionRenderer;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LittleTilesMovementBehaviour implements MovementBehaviour {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTMovementBehaviour");    @Override
    public void startMoving(MovementContext context) {
        LOGGER.info("🚀 LittleTiles startMoving called for pos: {} with state: {}", 
            context.localPos, context.state.toString());
    }

    @Override
    public void tick(MovementContext context) {
        // Only log occasionally to avoid spam
        if (context.contraption.entity.tickCount % 100 == 0) {
            LOGGER.debug("⏰ LittleTiles tick at pos: {} (tick: {})", 
                context.localPos, context.contraption.entity.tickCount);
        }
    }

    @Override
    public void stopMoving(MovementContext context) {
        LOGGER.info("⏹️ LittleTiles stopMoving called for pos: {}", context.localPos);
    }    @Override
    public boolean disableBlockEntityRendering() {
        // We want to handle rendering ourselves
        LOGGER.info("🔧 disableBlockEntityRendering called - returning true");
        return true;
    }@Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
                                    ContraptionMatrices matrices, MultiBufferSource bufferSource) {
        LOGGER.info("🎨 renderInContraption called for pos: {}", context.localPos);
        CompoundTag nbt = context.blockEntityData; // NBT from BETiles captured by contraption

        if (nbt == null || nbt.isEmpty()) {
            LOGGER.warn("⚠️ renderInContraption: NBT data is null or empty for pos: {}. State: {}", 
                context.localPos, context.state);
            return;
        }        try {            // Get partial ticks from Minecraft's timer since MovementBehaviour interface doesn't provide it
            float partialTicks = net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            
            // Call our LittleTiles rendering system
            LittleTilesContraptionRenderer.renderMovementBehaviourTile(
                context,        // Contains BlockState, localPos, blockEntityData (NBT)
                renderWorld,    // The contraption's virtual world
                matrices,       // Contraption transformation matrices
                bufferSource,   // Buffer for drawing
                partialTicks    // Partial tick value for smooth animation
            );
            LOGGER.info("✅ renderInContraption: Successfully called custom renderer for {}", context.localPos);} catch (Exception e) {
            LOGGER.error("❌ Error rendering LittleTile in contraption at " + context.localPos, e);
        }
    }
}
