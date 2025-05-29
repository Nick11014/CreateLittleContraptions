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
    }    /**
     * Analyze LittleTiles blocks in the contraption using robust reflection approach
     */
    private static void analyzeLittleTilesInContraption(AbstractContraptionEntity contraptionEntity) {
        if (contraptionEntity == null) {
            LOGGER.debug("ContraptionEntity is null, skipping LittleTiles analysis");
            return;
        }
        
        try {
            // First, try to get contraption using reflection with multiple possible field names
            Object contraption = null;
            String[] possibleContraptionFields = {"contraption", "contraptionInstance", "contraptionData"};
            
            for (String fieldName : possibleContraptionFields) {
                try {
                    var field = AbstractContraptionEntity.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    contraption = field.get(contraptionEntity);
                    if (contraption != null) {
                        LOGGER.debug("Found contraption using field: {}", fieldName);
                        break;
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                }
            }
            
            if (contraption == null) {
                LOGGER.debug("Contraption is null in entity: {} - might be during assembly/disassembly transition", 
                    contraptionEntity.getClass().getSimpleName());
                return;
            }
            
            // Now try to find blocks using various possible field structures
            java.util.Map<?, ?> blocks = null;
            String blocksSource = "unknown";
            
            // Strategy 1: Direct 'blocks' field
            try {
                var blocksField = contraption.getClass().getDeclaredField("blocks");
                blocksField.setAccessible(true);
                blocks = (java.util.Map<?, ?>) blocksField.get(contraption);
                blocksSource = "contraption.blocks";
            } catch (Exception e1) {
                
                // Strategy 2: 'structureTemplate.blocks'
                try {
                    var structureTemplateField = contraption.getClass().getDeclaredField("structureTemplate");
                    structureTemplateField.setAccessible(true);
                    var structureTemplate = structureTemplateField.get(contraption);
                    if (structureTemplate != null) {
                        var blocksField = structureTemplate.getClass().getDeclaredField("blocks");
                        blocksField.setAccessible(true);
                        blocks = (java.util.Map<?, ?>) blocksField.get(structureTemplate);
                        blocksSource = "contraption.structureTemplate.blocks";
                    }
                } catch (Exception e2) {
                    
                    // Strategy 3: 'template.blocks'
                    try {
                        var templateField = contraption.getClass().getDeclaredField("template");
                        templateField.setAccessible(true);
                        var template = templateField.get(contraption);
                        if (template != null) {
                            var blocksField = template.getClass().getDeclaredField("blocks");
                            blocksField.setAccessible(true);
                            blocks = (java.util.Map<?, ?>) blocksField.get(template);
                            blocksSource = "contraption.template.blocks";
                        }
                    } catch (Exception e3) {
                        
                        // Strategy 4: Look for any field containing "blocks"
                        for (var field : contraption.getClass().getDeclaredFields()) {
                            if (field.getName().toLowerCase().contains("blocks") || 
                                field.getType().getName().contains("Map")) {
                                try {
                                    field.setAccessible(true);
                                    var potentialBlocks = field.get(contraption);
                                    if (potentialBlocks instanceof java.util.Map<?, ?> map && !map.isEmpty()) {
                                        blocks = map;
                                        blocksSource = "contraption." + field.getName();
                                        break;
                                    }
                                } catch (Exception ignored) {
                                    // Continue searching
                                }
                            }
                        }
                    }
                }
            }
            
            if (blocks == null || blocks.isEmpty()) {
                LOGGER.debug("No blocks data found in contraption of type: {}", contraption.getClass().getSimpleName());
                return;
            }
            
            // Analyze blocks for LittleTiles
            int littleTilesCount = 0;
            int totalBlocks = blocks.size();
            
            for (var entry : blocks.entrySet()) {
                var blockInfo = entry.getValue();
                if (blockInfo == null) continue;
                
                try {
                    // Try multiple ways to get block state
                    Object blockState = null;
                    
                    // Method 1: Direct 'state' field
                    try {
                        var stateField = blockInfo.getClass().getDeclaredField("state");
                        stateField.setAccessible(true);
                        blockState = stateField.get(blockInfo);
                    } catch (NoSuchFieldException e) {
                        // Method 2: 'blockState' field
                        try {
                            var stateField = blockInfo.getClass().getDeclaredField("blockState");
                            stateField.setAccessible(true);
                            blockState = stateField.get(blockInfo);
                        } catch (NoSuchFieldException e2) {
                            // Method 3: Get any field that contains "state"
                            for (var field : blockInfo.getClass().getDeclaredFields()) {
                                if (field.getName().toLowerCase().contains("state")) {
                                    try {
                                        field.setAccessible(true);
                                        blockState = field.get(blockInfo);
                                        if (blockState != null) break;
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                    
                    if (blockState != null) {
                        String blockName = blockState.toString();
                        if (blockName.contains("littletiles")) {
                            littleTilesCount++;
                            LOGGER.debug("Found LittleTiles block: {}", blockName);
                        }
                    }
                } catch (Exception fieldEx) {
                    // Skip this block if we can't access its state
                    LOGGER.debug("Could not analyze block info of type: {}", blockInfo.getClass().getSimpleName());
                }
            }
            
            if (littleTilesCount > 0) {
                LOGGER.info("*** {} LittleTiles blocks detected in contraption! *** (from {}, total blocks: {})", 
                    littleTilesCount, blocksSource, totalBlocks);
            } else {
                LOGGER.debug("No LittleTiles blocks found in contraption with {} total blocks (from {})", 
                    totalBlocks, blocksSource);
            }
            
        } catch (Exception e) {
            LOGGER.debug("Could not analyze LittleTiles in contraption: {} - {}", 
                e.getClass().getSimpleName(), e.getMessage());
            // Log stack trace only in debug mode to avoid spam
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Full stack trace:", e);
            }
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
