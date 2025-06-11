package com.createlittlecontraptions.events;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.api.distmarker.Dist;

import com.createlittlecontraptions.rendering.LittleTilesRenderingLogic;

/**
 * Client-side event handler for rendering-related events.
 * This provides an alternative to mixins for intercepting rendering.
 */
@EventBusSubscriber(modid = "createlittlecontraptions", value = Dist.CLIENT)
public class ClientRenderEventHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * This event is fired before block screen effects are rendered.
     * We can use this as a hook to detect and manage LittleTiles rendering.
     */
    @SubscribeEvent
    public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        // This event is useful for detecting when blocks are being rendered
        // We can use this information to track LittleTiles blocks
        if (LittleTilesRenderingLogic.isDebugModeEnabled()) {
            LOGGER.debug("Block screen effect render event: {}", event.getBlockState().getBlock());
        }
    }
}
