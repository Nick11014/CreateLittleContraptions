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

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTMovementBehaviour");

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
                                    ContraptionMatrices matrices, MultiBufferSource bufferSource) {
        LOGGER.debug("renderInContraption called for pos: {}", context.localPos);
        CompoundTag nbt = context.blockEntityData; // NBT from BETiles captured by contraption

        if (nbt == null || nbt.isEmpty()) {
            LOGGER.warn("renderInContraption: NBT data is null or empty for pos: {}. State: {}", context.localPos, context.state);
            return;
        }

        try {
            // Call our LittleTiles rendering system
            LittleTilesContraptionRenderer.renderMovementBehaviourTile(
                context,        // Contains BlockState, localPos, blockEntityData (NBT)
                renderWorld,    // The contraption's virtual world
                matrices,       // Contraption transformation matrices
                bufferSource    // Buffer for drawing
            );
            LOGGER.debug("renderInContraption: Successfully called custom renderer for {}", context.localPos);        } catch (Exception e) {
            LOGGER.error("Error rendering LittleTile in contraption at " + context.localPos, e);
        }
    }
}
