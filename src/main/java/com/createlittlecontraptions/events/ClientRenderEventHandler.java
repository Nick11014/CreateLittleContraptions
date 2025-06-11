package com.createlittlecontraptions.events;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

import com.createlittlecontraptions.rendering.LittleTilesRenderingLogic;
import com.createlittlecontraptions.util.ContraptionDetector;
import com.createlittlecontraptions.util.LittleTilesDetector;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import java.util.List;

/**
 * Client-side event handler for rendering-related events.
 * This provides an alternative to mixins for intercepting rendering.
 * Enhanced with robust LittleTiles detection from proven debug command logic.
 */
@EventBusSubscriber(modid = "createlittlecontraptions", value = Dist.CLIENT)
public class ClientRenderEventHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Timing for periodic contraption scanning
    private static long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 5000; // 5 seconds
    
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
    
    /**
     * Periodic scanning during level rendering to detect LittleTiles in contraptions.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Only scan periodically to avoid performance issues
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime < SCAN_INTERVAL_MS) {
            return;
        }
        lastScanTime = currentTime;
        
        // Only scan during AFTER_ENTITIES stage to avoid redundant scans
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        
        // Scan for contraptions with LittleTiles using robust detection
        scanContraptionsForLittleTiles(mc.level);
    }
      /**
     * Scan all contraptions in the level for LittleTiles blocks using robust detection.
     */
    private static void scanContraptionsForLittleTiles(Level level) {
        try {
            // Use a reasonable bounding box instead of getBounds()
            net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(-1000, -64, -1000, 1000, 320, 1000);
            List<AbstractContraptionEntity> contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, searchBox);
            
            for (AbstractContraptionEntity contraptionEntity : contraptions) {
                // Use the robust detection logic
                int littleTilesCount = ContraptionDetector.countLittleTilesInContraption(contraptionEntity);
                
                if (littleTilesCount > 0 && LittleTilesRenderingLogic.isDebugModeEnabled()) {
                    LOGGER.debug("Periodic scan: Found {} LittleTiles blocks in contraption {} at {}", 
                        littleTilesCount, 
                        contraptionEntity.getClass().getSimpleName(),
                        contraptionEntity.blockPosition());
                    
                    // Get positions for potential model updates
                    List<net.minecraft.core.BlockPos> positions = ContraptionDetector.getLittleTilesPositions(contraptionEntity);
                    LOGGER.debug("  LittleTiles positions: {}", positions);
                }
            }
            
        } catch (Exception e) {
            if (LittleTilesRenderingLogic.isDebugModeEnabled()) {
                LOGGER.debug("Error during periodic contraption scan: {}", e.getMessage());
            }
        }
    }
}
