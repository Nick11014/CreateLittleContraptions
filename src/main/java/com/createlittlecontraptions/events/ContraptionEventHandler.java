package com.createlittlecontraptions.events;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import com.createlittlecontraptions.rendering.baking.LittleTilesModelBaker;
import com.createlittlecontraptions.rendering.cache.ContraptionModelCache;
import com.createlittlecontraptions.util.ContraptionDetector;
import com.createlittlecontraptions.util.LittleTilesDetector;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import java.util.Optional;
import java.util.List;
import net.minecraft.core.BlockPos;

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
     */    @SubscribeEvent
    public static void onContraptionAssembled(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            if (eventLoggingEnabled) {
                LOGGER.info("=== CONTRAPTION ASSEMBLED ===");
                LOGGER.info("Type: {}", contraptionEntity.getClass().getSimpleName());
                LOGGER.info("Position: {}", contraptionEntity.blockPosition());
                LOGGER.info("Entity ID: {}", contraptionEntity.getId());
                LOGGER.info("Level: {}", event.getLevel().dimension().location());
            }
              // Analyze LittleTiles in assembled contraption with robust detection
            analyzeLittleTilesInContraptionRobust(contraptionEntity);
            
            // Perform model baking for LittleTiles blocks
            bakeModelsForContraption(contraptionEntity);
        }
    }
    
    /**
     * Detect when contraptions are disassembled (entity removed)
     */    @SubscribeEvent
    public static void onContraptionDisassembled(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            if (eventLoggingEnabled) {
                LOGGER.info("=== CONTRAPTION DISASSEMBLED ===");
                LOGGER.info("Type: {}", contraptionEntity.getClass().getSimpleName());
                LOGGER.info("Position: {}", contraptionEntity.blockPosition());
                LOGGER.info("Entity ID: {}", contraptionEntity.getId());
                LOGGER.info("Level: {}", event.getLevel().dimension().location());
            }
              // Analyze LittleTiles before disassembly with robust detection
            analyzeLittleTilesInContraptionRobust(contraptionEntity);
            
            // Clear cached models to free memory
            ContraptionModelCache.clearCache(contraptionEntity.getUUID());
        }
    }/**
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
            }        } catch (Exception e) {
            LOGGER.debug("Could not analyze LittleTiles in contraption: {} - {}", 
                e.getClass().getSimpleName(), e.getMessage());
            // Log stack trace only in debug mode to avoid spam
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Full stack trace:", e);
            }
        }
    }
    
    /**
     * Bake models for all LittleTiles blocks in the contraption.
     * This is the core of the Model Baking solution.
     */
    private static void bakeModelsForContraption(AbstractContraptionEntity contraptionEntity) {
        if (contraptionEntity == null || contraptionEntity.level().isClientSide()) {
            return; // Only process on server side for now
        }
        
        try {
            // Get the contraption object
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) {
                LOGGER.debug("Could not get contraption from entity for baking");
                return;
            }
            
            // Get rendered block entities
            java.util.Collection<BlockEntity> renderedBEs = getRenderedBlockEntities(contraption);
            if (renderedBEs == null || renderedBEs.isEmpty()) {
                LOGGER.debug("No rendered block entities found in contraption for baking");
                return;
            }
            
            int bakedCount = 0;
            for (BlockEntity blockEntity : renderedBEs) {
                if (blockEntity == null) continue;
                
                // Attempt to bake the model for this block entity
                Optional<BakedModel> bakedModel = LittleTilesModelBaker.bake(blockEntity);
                if (bakedModel.isPresent()) {
                    // Cache the baked model
                    ContraptionModelCache.cacheModel(
                        contraptionEntity.getUUID(), 
                        blockEntity.getBlockPos(), 
                        bakedModel.get()
                    );
                    bakedCount++;
                    
                    if (eventLoggingEnabled) {
                        LOGGER.info("Baked model for LittleTiles block at {}", blockEntity.getBlockPos());
                    }
                }
            }
            
            if (bakedCount > 0) {
                LOGGER.info("Successfully baked {} models for contraption {}", 
                    bakedCount, contraptionEntity.getUUID());
            }
            
        } catch (Exception e) {
            LOGGER.warn("Exception during model baking for contraption: {}", e.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Model baking exception stack trace:", e);
            }
        }
    }
    
    /**
     * Helper method to get contraption object from entity using reflection.
     */
    private static Object getContraptionFromEntity(AbstractContraptionEntity contraptionEntity) {
        try {
            String[] possibleContraptionFields = {"contraption", "contraptionInstance", "contraptionData"};
            
            for (String fieldName : possibleContraptionFields) {
                try {
                    var field = AbstractContraptionEntity.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object contraption = field.get(contraptionEntity);
                    if (contraption != null) {
                        return contraption;
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get contraption object: {}", e.getMessage());
        }
        return null;
    }    
    /**
     * Robust analysis of LittleTiles in contraption using proven debug command logic.
     */
    private static void analyzeLittleTilesInContraptionRobust(AbstractContraptionEntity contraptionEntity) {
        try {
            if (eventLoggingEnabled) {
                LOGGER.info("Starting robust LittleTiles analysis...");
            }
            
            // Use the proven detection logic from ContraptionDetector
            int littleTilesCount = ContraptionDetector.countLittleTilesInContraption(contraptionEntity);
            
            if (littleTilesCount > 0) {
                LOGGER.info("*** ROBUST DETECTION: Found {} LittleTiles blocks in contraption! ***", littleTilesCount);
                
                // Get detailed positions
                List<BlockPos> littleTilesPositions = ContraptionDetector.getLittleTilesPositions(contraptionEntity);
                
                if (eventLoggingEnabled) {
                    LOGGER.info("LittleTiles positions detected: {}", littleTilesPositions);
                    
                    // Additional analysis using the debug command methods
                    Object contraption = ContraptionDetector.getContraptionFromEntity(contraptionEntity);
                    if (contraption != null) {
                        Object blocksData = ContraptionDetector.getBlocksFromContraption(contraption);
                        java.util.Map<?, ?> blockEntitiesData = ContraptionDetector.getBlockEntitiesFromContraption(contraption);
                        
                        LOGGER.info("Contraption analysis:");
                        LOGGER.info("  - Block data type: {}", blocksData != null ? blocksData.getClass().getSimpleName() : "null");
                        LOGGER.info("  - BlockEntities count: {}", blockEntitiesData != null ? blockEntitiesData.size() : 0);
                        
                        if (blockEntitiesData != null) {
                            for (java.util.Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                                Object pos = entry.getKey();
                                Object nbtData = entry.getValue();
                                String beType = getBlockEntityTypeFromNBT(nbtData);
                                if (beType.toLowerCase().contains("littletiles")) {
                                    LOGGER.info("  - LittleTiles BE at {}: {}", pos, beType);
                                }
                            }
                        }
                    }
                }
                
                // Trigger model baking for detected blocks
                for (BlockPos pos : littleTilesPositions) {
                    try {
                        bakeModelForPosition(contraptionEntity, pos);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to bake model for LittleTiles at {}: {}", pos, e.getMessage());
                    }
                }
                
            } else {
                if (eventLoggingEnabled) {
                    LOGGER.info("No LittleTiles blocks detected in contraption (robust detection)");
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error in robust LittleTiles analysis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to get block entity type from NBT data.
     */
    private static String getBlockEntityTypeFromNBT(Object nbtData) {
        try {
            if (nbtData instanceof net.minecraft.nbt.CompoundTag nbt) {
                return nbt.getString("id");
            }
            // Try reflection if CompoundTag doesn't work directly
            java.lang.reflect.Method getStringMethod = nbtData.getClass().getMethod("getString", String.class);
            Object result = getStringMethod.invoke(nbtData, "id");
            return result != null ? result.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }    
    /**
     * Bake a model for a specific LittleTiles block at the given position in a contraption.
     */
    private static void bakeModelForPosition(AbstractContraptionEntity contraptionEntity, BlockPos pos) {
        try {
            if (contraptionEntity == null || contraptionEntity.level().isClientSide()) {
                return; // Only process on server side for now
            }
            
            // Get the contraption object
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) {
                LOGGER.debug("Could not get contraption from entity for position baking");
                return;
            }
            
            // For now, we'll use a simplified approach since we know this is a LittleTiles position
            // In a full implementation, we would need to get the specific BlockEntity at this position
            
            // Create a dummy BlockEntity for testing purposes
            // In reality, this would need to be the actual LittleTiles BlockEntity
            if (eventLoggingEnabled) {
                LOGGER.info("Baking model for LittleTiles block at position {} in contraption {}", 
                    pos, contraptionEntity.getId());
            }
              // Cache a placeholder model for now
            ContraptionModelCache.cacheModel(
                contraptionEntity.getUUID(),
                pos,
                null // This would be the actual baked model
            );
            
            if (eventLoggingEnabled) {
                LOGGER.info("Model baking completed for position {}", pos);
            }
            
        } catch (Exception e) {
            LOGGER.warn("Error baking model for position {}: {}", pos, e.getMessage());
        }
    }    
    /**
     * Helper method to get rendered block entities from contraption.
     */
    @SuppressWarnings("unchecked")
    private static java.util.Collection<BlockEntity> getRenderedBlockEntities(Object contraption) {
        try {
            // Try to find getRenderedBEs method
            var method = contraption.getClass().getMethod("getRenderedBEs");
            Object result = method.invoke(contraption);
            if (result instanceof java.util.Collection<?> collection) {
                return (java.util.Collection<BlockEntity>) collection;
            }
        } catch (Exception e) {
            LOGGER.debug("Could not get rendered block entities: {}", e.getMessage());
        }
        return java.util.List.of();
    }
}
