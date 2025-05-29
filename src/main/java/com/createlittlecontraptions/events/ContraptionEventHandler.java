package com.createlittlecontraptions.events;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

/**
 * Event handler for detecting Create contraption assembly/disassembly events.
 * Part of Step 2 implementation for CreateLittleContraptions mod.
 */
@EventBusSubscriber(modid = "createlittlecontraptions")
public class ContraptionEventHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean eventLoggingEnabled = false;
    
    /**
     * Toggle event logging on/off
     */
    public static void setEventLogging(boolean enabled) {
        eventLoggingEnabled = enabled;
        LOGGER.info("Contraption event logging " + (enabled ? "ENABLED" : "DISABLED"));
    }
    
    public static boolean isEventLoggingEnabled() {
        return eventLoggingEnabled;
    }
    
    /**
     * Detect when contraptions are assembled (entity spawned)
     */
    @SubscribeEvent
    public static void onContraptionAssembled(EntityJoinLevelEvent event) {
        if (!eventLoggingEnabled) return;
        
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            LOGGER.info("=== CONTRAPTION ASSEMBLED ===");
            LOGGER.info("Type: {}", contraptionEntity.getClass().getSimpleName());
            LOGGER.info("Position: {}", contraptionEntity.blockPosition());
            LOGGER.info("Entity ID: {}", contraptionEntity.getId());
            LOGGER.info("Level: {}", event.getLevel().dimension().location());
            
            // Analyze LittleTiles in assembled contraption
            analyzeLittleTilesInContraption(contraptionEntity);
            
            // Notify nearby players
            notifyNearbyPlayers(contraptionEntity, "Contraption assembled with LittleTiles!", true);
        }
    }
    
    /**
     * Detect when contraptions are disassembled (entity removed)
     */
    @SubscribeEvent
    public static void onContraptionDisassembled(EntityLeaveLevelEvent event) {
        if (!eventLoggingEnabled) return;
        
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            LOGGER.info("=== CONTRAPTION DISASSEMBLED ===");
            LOGGER.info("Type: {}", contraptionEntity.getClass().getSimpleName());
            LOGGER.info("Position: {}", contraptionEntity.blockPosition());
            LOGGER.info("Entity ID: {}", contraptionEntity.getId());
            LOGGER.info("Level: {}", event.getLevel().dimension().location());
            
            // Analyze LittleTiles before disassembly
            analyzeLittleTilesInContraption(contraptionEntity);
            
            // Notify nearby players
            notifyNearbyPlayers(contraptionEntity, "Contraption disassembled with LittleTiles!", false);
        }
    }
      /**
     * Analyze LittleTiles blocks in the contraption
     */
    private static void analyzeLittleTilesInContraption(AbstractContraptionEntity contraptionEntity) {
        if (contraptionEntity == null) {
            LOGGER.warn("ContraptionEntity is null, cannot analyze LittleTiles");
            return;
        }
        
        try {
            // Use reflection to access contraption data (similar to debug command)
            var contraptionField = AbstractContraptionEntity.class.getDeclaredField("contraption");
            contraptionField.setAccessible(true);
            var contraption = contraptionField.get(contraptionEntity);
            
            if (contraption == null) {
                LOGGER.warn("Contraption is null in entity: {}", contraptionEntity.getClass().getSimpleName());
                return;
            }
            
            // Try 'blocks' field first, if not found try 'structureTemplate.blocks'
            java.util.Map<?, ?> blocks = null;
            try {
                var blocksField = contraption.getClass().getDeclaredField("blocks");
                blocksField.setAccessible(true);
                blocks = (java.util.Map<?, ?>) blocksField.get(contraption);
            } catch (NoSuchFieldException e) {
                // Try alternative field structure
                try {
                    var structureTemplateField = contraption.getClass().getDeclaredField("structureTemplate");
                    structureTemplateField.setAccessible(true);
                    var structureTemplate = structureTemplateField.get(contraption);
                    if (structureTemplate != null) {
                        var blocksField = structureTemplate.getClass().getDeclaredField("blocks");
                        blocksField.setAccessible(true);
                        blocks = (java.util.Map<?, ?>) blocksField.get(structureTemplate);
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Could not access blocks data: {}", ex.getMessage());
                    return;
                }
            }
            
            if (blocks == null || blocks.isEmpty()) {
                LOGGER.debug("No blocks found in contraption");
                return;
            }
            
            int littleTilesCount = 0;
            for (var entry : blocks.entrySet()) {
                var blockInfo = entry.getValue();
                if (blockInfo == null) continue;
                
                try {
                    var stateField = blockInfo.getClass().getDeclaredField("state");
                    stateField.setAccessible(true);
                    var blockState = stateField.get(blockInfo);
                    
                    if (blockState != null) {
                        String blockName = blockState.toString();
                        if (blockName.contains("littletiles")) {
                            littleTilesCount++;
                        }
                    }
                } catch (Exception fieldEx) {
                    // Skip this block if we can't access its state
                    continue;
                }
            }
            
            if (littleTilesCount > 0) {
                LOGGER.info("*** {} LittleTiles blocks detected in contraption! ***", littleTilesCount);
            } else {
                LOGGER.debug("No LittleTiles blocks found in contraption with {} total blocks", blocks.size());
            }
            
        } catch (Exception e) {
            LOGGER.warn("Failed to analyze LittleTiles in contraption: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * Notify nearby players about contraption events
     */
    private static void notifyNearbyPlayers(AbstractContraptionEntity contraptionEntity, String message, boolean isAssembly) {
        if (contraptionEntity.level().isClientSide()) return;
        
        var serverLevel = (net.minecraft.server.level.ServerLevel) contraptionEntity.level();
        var players = serverLevel.getPlayers(player -> 
            player.distanceToSqr(contraptionEntity) < 64 * 64 // 64 block radius
        );
        
        String prefix = isAssembly ? "§a[ASSEMBLY]" : "§c[DISASSEMBLY]";
        Component notificationMessage = Component.literal(prefix + " " + message);
        
        for (ServerPlayer player : players) {
            player.sendSystemMessage(notificationMessage);
        }
    }
}
