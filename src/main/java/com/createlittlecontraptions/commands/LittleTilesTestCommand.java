package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.createlittlecontraptions.util.ContraptionDetector;
import com.createlittlecontraptions.util.LittleTilesDetector;

/**
 * Simple test command to verify robust LittleTiles detection is working.
 * Based on proven logic from the original debug command.
 */
public class LittleTilesTestCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("littletiles-test")
            .requires(source -> source.hasPermission(2))
            .executes(LittleTilesTestCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== LITTLETILES DETECTION TEST ==="));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
        
        int totalContraptions = 0;
        int contraptionsWithLittleTiles = 0;
        int totalLittleTilesBlocks = 0;
        
        for (Entity entity : contraptionEntities) {
            if (entity instanceof AbstractContraptionEntity contraptionEntity) {
                totalContraptions++;
                
                source.sendSystemMessage(Component.literal(""));
                source.sendSystemMessage(Component.literal("--- Contraption #" + totalContraptions + " ---"));
                source.sendSystemMessage(Component.literal("Type: " + contraptionEntity.getClass().getSimpleName()));
                source.sendSystemMessage(Component.literal("Position: " + contraptionEntity.blockPosition()));
                source.sendSystemMessage(Component.literal("Entity ID: " + contraptionEntity.getId()));
                
                // Use our robust detection
                int littleTilesCount = ContraptionDetector.countLittleTilesInContraption(contraptionEntity);
                
                if (littleTilesCount > 0) {
                    contraptionsWithLittleTiles++;
                    totalLittleTilesBlocks += littleTilesCount;
                    
                    source.sendSystemMessage(Component.literal("*** FOUND " + littleTilesCount + " LITTLETILES BLOCKS! ***"));
                      // Get positions
                    List<BlockPos> positions = ContraptionDetector.getLittleTilesPositions(contraptionEntity);
                    source.sendSystemMessage(Component.literal("LittleTiles positions:"));
                    if (positions.isEmpty()) {
                        source.sendSystemMessage(Component.literal("  (No positions found - may need debugging)"));
                    } else {
                        int shown = 0;
                        for (BlockPos pos : positions) {
                            if (shown >= 10) {
                                source.sendSystemMessage(Component.literal("  ... and " + (positions.size() - 10) + " more"));
                                break;
                            }
                            source.sendSystemMessage(Component.literal("  " + pos));
                            shown++;
                        }
                    }
                    
                    // Additional debug info
                    source.sendSystemMessage(Component.literal("Debug Info:"));
                    Object contraption = ContraptionDetector.getContraptionFromEntity(contraptionEntity);
                    if (contraption != null) {
                        Object blocksData = ContraptionDetector.getBlocksFromContraption(contraption);
                        Map<?, ?> blockEntitiesData = ContraptionDetector.getBlockEntitiesFromContraption(contraption);
                        
                        source.sendSystemMessage(Component.literal("  - Contraption class: " + contraption.getClass().getSimpleName()));
                        source.sendSystemMessage(Component.literal("  - Blocks data: " + (blocksData != null ? blocksData.getClass().getSimpleName() : "null")));
                        source.sendSystemMessage(Component.literal("  - BlockEntities count: " + (blockEntitiesData != null ? blockEntitiesData.size() : 0)));
                        
                        if (blockEntitiesData != null && !blockEntitiesData.isEmpty()) {
                            source.sendSystemMessage(Component.literal("  - BlockEntity types:"));
                            int beShown = 0;
                            for (Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                                if (beShown >= 5) {
                                    source.sendSystemMessage(Component.literal("    ... and " + (blockEntitiesData.size() - 5) + " more"));
                                    break;
                                }
                                Object pos = entry.getKey();
                                Object nbtData = entry.getValue();
                                String beType = getBlockEntityType(nbtData);
                                boolean isLittleTiles = beType.toLowerCase().contains("littletiles");
                                String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                                source.sendSystemMessage(Component.literal("    " + pos + " -> " + beType + marker));
                                beShown++;
                            }
                        }
                    }
                } else {
                    source.sendSystemMessage(Component.literal("No LittleTiles blocks detected"));
                }
                
                // Test LittleTiles availability
                if (totalContraptions == 1) { // Only show this once
                    boolean available = LittleTilesDetector.isLittleTilesAvailable();
                    source.sendSystemMessage(Component.literal("LittleTiles mod available: " + available));
                    if (available) {
                        Class<?> ltClass = LittleTilesDetector.getLittleTilesBlockEntityClass();
                        source.sendSystemMessage(Component.literal("LittleTiles BE class: " + 
                            (ltClass != null ? ltClass.getName() : "null")));
                    }
                }
            }
        }
        
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== SUMMARY ==="));
        source.sendSystemMessage(Component.literal("Total Contraptions: " + totalContraptions));
        source.sendSystemMessage(Component.literal("Contraptions with LittleTiles: " + contraptionsWithLittleTiles));
        source.sendSystemMessage(Component.literal("Total LittleTiles Blocks: " + totalLittleTilesBlocks));
        
        if (totalLittleTilesBlocks > 0) {
            source.sendSystemMessage(Component.literal("*** SUCCESS: Robust detection is working! ***"));
        } else {
            source.sendSystemMessage(Component.literal("No LittleTiles detected - this is normal if none are present"));
        }
        
        return totalLittleTilesBlocks;
    }    private static List<Entity> findContraptionEntities(ServerLevel level) {
        List<Entity> contraptions = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof AbstractContraptionEntity) {
                contraptions.add(entity);
            }
        }
        return contraptions;
    }

    /**
     * Get block entity type from NBT data (helper method).
     */
    private static String getBlockEntityType(Object nbtData) {
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
}
