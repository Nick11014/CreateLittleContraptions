package com.createlittlecontraptions;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.createlittlecontraptions.registry.ModBlocks;
import com.createlittlecontraptions.registry.ModItems;
import com.createlittlecontraptions.registry.ModCreativeTabs;
import com.createlittlecontraptions.dev.ContraptionInspectorCommand;
import com.createlittlecontraptions.commands.ContraptionDebugCommand;

@Mod(CreateLittleContraptions.MODID)
public class CreateLittleContraptions {
    public static final String MODID = "createlittlecontraptions";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateLittleContraptions(IEventBus modEventBus, ModContainer modContainer) {
        // Register our mod content
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register for server events (like command registration)
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("CreateLittleContraptions mod initializing...");
    }    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands for CreateLittleContraptions...");
        ContraptionInspectorCommand.register(event.getDispatcher());
        ContraptionDebugCommand.register(event.getDispatcher());
    }
}
