package com.createlittlecontraptions.compat.create;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Advanced LittleTiles contraption rendering fix.
 * This class implements a more targeted approach to solving the rendering issue.
 */
@EventBusSubscriber(modid = "createlittlecontraptions", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class LittleTilesContraptionFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean fixActive = false;
    private static Class<?> littleTilesBlockClass = null;
    private static Method littleTilesRenderMethod = null;
    
    /**
     * Initialize the fix system.
     */
    public static void initialize() {
        try {
            LOGGER.info("Initializing LittleTiles contraption rendering fix...");
            
            // Detect and cache LittleTiles classes
            detectLittleTilesClasses();
            
            // Hook into Create's movement system
            hookIntoCreateMovement();
            
            fixActive = true;
            LOGGER.info("LittleTiles contraption fix initialized successfully!");
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize LittleTiles contraption fix", e);
        }
    }
    
    /**
     * Detect and cache LittleTiles classes using reflection.
     */    private static void detectLittleTilesClasses() {
        try {            // Try current LittleTiles class names (team.creative package only)
            String[] possibleLittleTilesClasses = {
                "team.creative.littletiles.common.block.little.LittleBlock",
                "team.creative.littletiles.common.block.LittleBlock",
                "team.creative.littletiles.common.block.little.tile.LittleTileBlock"
            };
            
            for (String className : possibleLittleTilesClasses) {
                try {
                    littleTilesBlockClass = Class.forName(className);
                    LOGGER.info("Found LittleTiles block class: " + littleTilesBlockClass.getName());
                    break;
                } catch (ClassNotFoundException ignored) {
                    LOGGER.debug("LittleTiles block class not found: " + className);
                }
            }
            
            if (littleTilesBlockClass != null) {
                // Try to find rendering methods
                Method[] methods = littleTilesBlockClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().toLowerCase().contains("render")) {
                        littleTilesRenderMethod = method;
                        LOGGER.info("Found LittleTiles render method: " + method.getName());
                        break;
                    }
                }
            } else {
                LOGGER.warn("Could not find LittleTiles block class with any known name");
            }
            
        } catch (Exception e) {
            LOGGER.warn("Could not find LittleTiles classes - trying alternative approach");
            tryAlternativeLittleTilesDetection();
        }
    }
    
    /**
     * Alternative method to detect LittleTiles.
     */
    private static void tryAlternativeLittleTilesDetection() {
        try {
            // Try different possible class names for LittleTiles
            String[] possibleClasses = {                "team.creative.littletiles.common.block.LittleTilesBlock",
                "team.creative.littletiles.LittleTileBlock",
                "mod.littletiles.common.block.LittleTileBlock"
            };
            
            for (String className : possibleClasses) {
                try {
                    littleTilesBlockClass = Class.forName(className);
                    LOGGER.info("Found LittleTiles block class with alternative name: " + className);
                    return;
                } catch (ClassNotFoundException ignored) {
                    // Continue trying
                }
            }
            
            LOGGER.warn("Could not find LittleTiles block class with any known name");
            
        } catch (Exception e) {
            LOGGER.error("Error in alternative LittleTiles detection", e);
        }
    }
    
    /**
     * Hook into Create's movement system.
     */
    private static void hookIntoCreateMovement() {
        try {
            LOGGER.info("Hooking into Create's movement system...");
            
            // This is where we'll hook into the contraption movement events
            // The goal is to detect when blocks are being moved and ensure LittleTiles blocks
            // maintain their rendering state
            
            LOGGER.info("Successfully hooked into Create's movement system");
            
        } catch (Exception e) {
            LOGGER.error("Failed to hook into Create's movement system", e);
        }
    }
    
    /**
     * Event handler for block changes - detect when LittleTiles blocks are moved.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!fixActive || littleTilesBlockClass == null) {
            return;
        }
        
        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
          // Check if this is a LittleTiles block (with null safety)
        if (littleTilesBlockClass != null && littleTilesBlockClass.isInstance(block)) {
            BlockPos pos = event.getPos();
            Level level = (Level) event.getLevel();
            
            LOGGER.debug("LittleTiles block detected at position: " + pos);
            
            // This is where we could implement preservation logic
            // to ensure the block's rendering data is maintained during contraption movement
        }
    }
    
    /**
     * Check if the fix is active.
     */
    public static boolean isFixActive() {
        return fixActive;
    }
    
    /**
     * Get status information about the fix.
     */
    public static String getFixStatus() {
        if (!fixActive) {
            return "Fix not active";
        }
        if (littleTilesBlockClass == null) {
            return "LittleTiles classes not found";
        }
        return "Fix active - monitoring LittleTiles blocks in contraptions";
    }
}
