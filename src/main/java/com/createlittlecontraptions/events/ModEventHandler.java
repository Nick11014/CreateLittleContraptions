package com.createlittlecontraptions.events;

import com.createlittlecontraptions.compat.create.CreateCompatHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Event handler for mod lifecycle events.
 * Ensures proper initialization of compatibility features.
 */
@EventBusSubscriber(modid = "createlittlecontraptions", bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
      @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("CreateLittleContraptions common setup starting...");
        
        event.enqueueWork(() -> {
            // Initialize Create compatibility
            CreateCompatHandler.initialize();
            
            // Add integration status logging
            logIntegrationStatus();
        });
        
        LOGGER.info("CreateLittleContraptions common setup complete!");
    }    private static void logIntegrationStatus() {
        LOGGER.info("=== CreateLittleContraptions Integration Status ===");
        try {
            Class.forName("com.simibubi.create.Create");
            LOGGER.info("✓ Create mod detected!");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("✗ Create mod NOT found!");
        }
        
        // Use ModList for LittleTiles detection
        boolean littleTilesDetected = false;
        try {
            Class<?> modListClass = Class.forName("net.neoforged.fml.ModList");
            Object modListInstance = modListClass.getMethod("get").invoke(null);
            littleTilesDetected = (Boolean) modListClass.getMethod("isLoaded", String.class).invoke(modListInstance, "littletiles");        } catch (Exception e) {
            // Fallback to class detection (team.creative package only)
            try {
                Class.forName("team.creative.littletiles.LittleTiles");
                littleTilesDetected = true;
            } catch (ClassNotFoundException ex) {
                // Not found
            }
        }
        
        if (littleTilesDetected) {
            LOGGER.info("✓ LittleTiles mod detected!");
        } else {
            LOGGER.warn("✗ LittleTiles mod NOT found!");
        }
          // Additional compatibility checks (team.creative package only)
        try {
            Class.forName("team.creative.creativecore.CreativeCore");
            LOGGER.info("✓ CreativeCore detected!");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("✗ CreativeCore NOT found!");
        }
        
        LOGGER.info("===============================================");
    }
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("CreateLittleContraptions client setup starting...");
        
        event.enqueueWork(() -> {
            // Any client-specific initialization can go here
            LOGGER.info("Client-side rendering compatibility initialized");
        });        
        LOGGER.info("CreateLittleContraptions client setup complete!");
    }
}
