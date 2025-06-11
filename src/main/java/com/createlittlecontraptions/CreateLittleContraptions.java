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
import com.createlittlecontraptions.commands.LittleTilesTestCommand;
import com.createlittlecontraptions.events.ContraptionEventHandler;
import com.createlittlecontraptions.rendering.LittleTilesRuntimeHook;

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
    }private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CreateLittleContraptions common setup");
        // Future integration with Create and LittleTiles will be implemented here
    }    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("CreateLittleContraptions client setup");
        
        // Initialize the LittleTiles runtime hook system
        event.enqueueWork(() -> {
            LittleTilesRuntimeHook.initialize();
            LOGGER.info("LittleTiles Runtime Hook system initialized");
        });
    }    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands for CreateLittleContraptions...");
        ContraptionDebugCommand.register(event.getDispatcher());
        LittleTilesTestCommand.register(event.getDispatcher());
        LOGGER.info("Registered commands: contraption-debug, littletiles-test, cache-test");
    }
}
