package com.createlittlecontraptions.events;

import com.createlittlecontraptions.dev.ContraptionDebugCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Event handler for game events (not mod lifecycle events).
 */
@EventBusSubscriber(modid = "createlittlecontraptions", bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Register debug commands when the server starts.
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering CreateLittleContraptions debug commands...");
        ContraptionDebugCommand.register(event.getDispatcher());
        LOGGER.info("Debug commands registered successfully!");
    }
}
