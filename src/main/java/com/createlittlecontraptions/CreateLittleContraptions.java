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

import com.createlittlecontraptions.registry.ModBlocks;
import com.createlittlecontraptions.registry.ModItems;
import com.createlittlecontraptions.registry.ModCreativeTabs;
import com.createlittlecontraptions.dev.ContraptionInspectorCommand;
import com.createlittlecontraptions.commands.ContraptionDebugCommand;
import com.createlittlecontraptions.compat.create.CreateIntegration;

@Mod(CreateLittleContraptions.MODID)
public class CreateLittleContraptions {
    public static final String MODID = "createlittlecontraptions";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateLittleContraptions(IEventBus modEventBus, ModContainer modContainer) {
        // Register our mod content
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register mod lifecycle events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register for server events (like command registration)
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("CreateLittleContraptions mod initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CreateLittleContraptions common setup");
        
        // Register Create integration during common setup
        // This ensures both Create and LittleTiles are loaded
        event.enqueueWork(() -> {
            CreateIntegration.registerLittleTilesMovementBehaviour();
            CreateIntegration.logIntegrationStatus();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("CreateLittleContraptions client setup");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands for CreateLittleContraptions...");
        ContraptionInspectorCommand.register(event.getDispatcher());
        ContraptionDebugCommand.register(event.getDispatcher());
    }
}
