package com.createlittlecontraptions.events;

import com.createlittlecontraptions.compat.create.MovementBehaviourRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Client-side event handler for rendering compatibility.
 * This replaces the Mixin approach with a more compatible event-based solution.
 * DISABLED: This class is temporarily disabled to avoid conflicts with CreateRuntimeIntegration
 */
// @EventBusSubscriber(modid = "createlittlecontraptions", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientRenderEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Handles rendering events to ensure LittleTiles blocks are visible in contraptions.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // This event fires during level rendering and allows us to inject
        // custom rendering logic for LittleTiles blocks in contraptions
        
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            // At this stage, we can ensure any missed LittleTiles blocks
            // in contraptions are properly rendered
            handleLittleTilesContraptionRendering(event);
        }
    }
    
    /**
     * Custom rendering logic for LittleTiles blocks in contraptions.
     */
    private static void handleLittleTilesContraptionRendering(RenderLevelStageEvent event) {
        try {
            // This is where we can add custom rendering logic if needed
            // The main solution is in the LittleTilesMovementBehaviour class
            // This event handler serves as a backup/enhancement mechanism
            
            LOGGER.debug("Handling LittleTiles contraption rendering enhancement");
            
        } catch (Exception e) {
            LOGGER.error("Error in LittleTiles contraption rendering", e);
        }
    }
}
