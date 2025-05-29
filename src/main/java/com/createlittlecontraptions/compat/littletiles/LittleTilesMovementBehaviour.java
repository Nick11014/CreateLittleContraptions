package com.createlittlecontraptions.compat.littletiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * MovementBehaviour for LittleTiles blocks (littletiles:tiles) within Create contraptions.
 * This class handles the rendering and behavior of LittleTiles blocks when they are part 
 * of a moving contraption.
 * 
 * Implementation follows the Create mod's MovementBehaviour pattern but uses reflection
 * to avoid direct dependencies on Create's APIs during development.
 */
public class LittleTilesMovementBehaviour {

    /**
     * Renders the LittleTiles block within a contraption.
     * This method is called during contraption rendering to handle LittleTiles blocks specifically.
     * 
     * @param context MovementContext from Create (contains localPos, state, blockEntityData)
     * @param renderWorld VirtualRenderWorld from Create
     * @param matrices ContraptionMatrices from Create (contains modelViewStack)
     * @param buffer MultiBufferSource for rendering
     * @param partialTicks Partial tick time for smooth animation
     */
    @OnlyIn(Dist.CLIENT)
    public void renderInContraption(Object context, 
                                   Object renderWorld,
                                   Object matrices, 
                                   Object buffer, 
                                   float partialTicks) {
        
        try {
            LittleTilesAPIFacade.logDebug("LittleTiles renderInContraption called for contraption rendering");
            
            // Validate parameters
            if (context == null || renderWorld == null || matrices == null || buffer == null) {
                LittleTilesAPIFacade.logError("Null parameters passed to renderInContraption");
                return;
            }
            
            // Ensure buffer is MultiBufferSource
            if (!(buffer instanceof MultiBufferSource)) {
                LittleTilesAPIFacade.logError("Buffer parameter is not MultiBufferSource: " + buffer.getClass().getName());
                return;
            }
            
            // Call the dedicated LittleTiles contraption renderer
            LittleTilesContraptionRenderer.renderMovementBehaviourTile(
                context, 
                renderWorld, 
                matrices, 
                (MultiBufferSource) buffer, 
                partialTicks
            );
            
        } catch (Exception e) {
            LittleTilesAPIFacade.logError("Error in LittleTiles renderInContraption: " + e.getMessage());
            e.printStackTrace();
        }
    }    /**
     * Determines if this MovementBehaviour is suitable for the given block state.
     * 
     * @param state The block state to check
     * @return true if this is a LittleTiles block (littletiles:tiles)
     */
    public boolean isActive(BlockState state) {
        try {
            // Check if this is a LittleTiles block using multiple criteria
            String blockName = state.getBlock().getClass().getSimpleName();
            String fullClassName = state.getBlock().getClass().getName();
            String blockString = state.getBlock().toString();
            
            boolean isLittleTiles = blockName.equals("BlockTile") || 
                                  fullClassName.contains("littletiles") ||
                                  blockString.contains("littletiles");
            
            if (isLittleTiles) {
                LittleTilesAPIFacade.logDebug("LittleTiles block detected: " + fullClassName);
            }
            
            return isLittleTiles;
            
        } catch (Exception e) {
            LittleTilesAPIFacade.logError("Error checking if block is LittleTiles: " + e.getMessage());
            return false;
        }
    }

    /**
     * Called when the contraption starts moving.
     * Can be used for initialization if needed.
     * 
     * @param context MovementContext from Create
     */
    public void startMoving(Object context) {
        try {
            LittleTilesAPIFacade.logDebug("LittleTiles movement started");
            // No special initialization needed for LittleTiles rendering
        } catch (Exception e) {
            LittleTilesAPIFacade.logError("Error starting LittleTiles movement: " + e.getMessage());
        }
    }

    /**
     * Called when the contraption stops moving.
     * Can be used for cleanup if needed.
     * 
     * @param context MovementContext from Create
     */
    public void stopMoving(Object context) {
        try {
            LittleTilesAPIFacade.logDebug("LittleTiles movement stopped");
            // No special cleanup needed for LittleTiles rendering
        } catch (Exception e) {
            LittleTilesAPIFacade.logError("Error stopping LittleTiles movement: " + e.getMessage());
        }
    }

    /**
     * Called every tick while the contraption is moving.
     * Can be used for continuous updates if needed.
     * 
     * @param context MovementContext from Create
     */
    public void tick(Object context) {
        try {
            // No continuous updates needed for LittleTiles rendering
            // Rendering is handled per-frame in renderInContraption
        } catch (Exception e) {
            LittleTilesAPIFacade.logError("Error during LittleTiles movement tick: " + e.getMessage());
        }
    }

    /**
     * Determines if this MovementBehaviour should be active for client-side rendering.
     * 
     * @return true for LittleTiles since we need client-side rendering
     */
    @OnlyIn(Dist.CLIENT)
    public boolean hasSpecialInstancedRendering() {
        return true; // LittleTiles needs special rendering
    }
}
     */
    public void startMoving(Object context) {
        // Log the start of movement for debugging
        if (LittleTilesAPIFacade.isDebugEnabled()) {
            LittleTilesAPIFacade.logDebug("LittleTiles block started moving");
        }
    }

    /**
     * Called when the contraption stops moving.
     * Can be used for cleanup if needed.
     */
    public void stopMoving(Object context) {
        // Log the stop of movement for debugging
        if (LittleTilesAPIFacade.isDebugEnabled()) {
            LittleTilesAPIFacade.logDebug("LittleTiles block stopped moving");
        }
    }

    /**
     * Called every tick while the contraption is moving.
     * Can be used for per-tick logic if needed.
     */
    public void tick(Object context) {
        // Currently no per-tick logic needed for LittleTiles rendering
        // Future implementations could handle dynamic lighting updates here
    }
}
