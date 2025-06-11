package com.createlittlecontraptions;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.createlittlecontraptions.commands.ContraptionDebugCommand;
import com.createlittlecontraptions.commands.ContraptionDisassemblyCommand;
import com.createlittlecontraptions.commands.ContraptionAssemblyCommand;
import com.createlittlecontraptions.commands.ContraptionRenderCommand;
import com.createlittlecontraptions.events.ContraptionEventHandler;

@Mod(CreateLittleContraptions.MODID)
public class CreateLittleContraptions {
    public static final String MODID = "createlittlecontraptions";
    public static final Logger LOGGER = LogUtils.getLogger();    public CreateLittleContraptions(IEventBus modEventBus, ModContainer modContainer) {
        // Register mod lifecycle events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register for server events (like command registration)
        NeoForge.EVENT_BUS.register(this);
        
        // Register event handlers
        NeoForge.EVENT_BUS.register(ContraptionEventHandler.class);

        LOGGER.info("CreateLittleContraptions mod initializing...");
    }    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CreateLittleContraptions common setup");
        
        // Register LittleTiles MovementBehaviour for contraption rendering control
        event.enqueueWork(() -> {
            try {
                registerLittleTilesMovementBehaviour();
            } catch (Exception e) {
                LOGGER.error("Failed to register LittleTiles MovementBehaviour: {}", e.getMessage());
            }
        });    }    /**
     * Registra o MovementBehaviour para blocos LittleTiles
     */
    public static void registerLittleTilesMovementBehaviour() {
        try {
            // Note: This registration is simplified - complex compatibility was removed
            LOGGER.info("LittleTiles MovementBehaviour registration skipped - experimental code removed");
            
        } catch (Exception e) {
            LOGGER.error("Error in MovementBehaviour registration: {}", e.getMessage());
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("CreateLittleContraptions client setup");
    }    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands for CreateLittleContraptions...");
        ContraptionDebugCommand.register(event.getDispatcher());
        ContraptionRenderCommand.register(event.getDispatcher());
        ContraptionDisassemblyCommand.register(event.getDispatcher());
        ContraptionAssemblyCommand.register(event.getDispatcher());
    }
}
