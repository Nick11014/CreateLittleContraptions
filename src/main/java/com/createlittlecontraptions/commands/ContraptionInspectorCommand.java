package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced command to inspect all Create contraptions and highlight LittleTiles blocks.
 * This command provides comprehensive analysis of contraptions in the world.
 */
public class ContraptionInspectorCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-debug")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionInspectorCommand::executeInspection));
    }

    private static int executeInspection(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level level = source.getLevel();

        if (!(level instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("❌ This command can only be run on the server side"));
            return 0;
        }

        try {
            source.sendSuccess(() -> Component.literal("🔍 Starting Create contraption inspection..."), false);
            
            List<Object> contraptions = findCreateContraptions(serverLevel);
            
            if (contraptions.isEmpty()) {
                source.sendSuccess(() -> Component.literal("📋 No Create contraptions found in this dimension"), false);
                return 1;
            }

            int totalBlocks = 0;
            int totalLittleTilesBlocks = 0;

            source.sendSuccess(() -> Component.literal(String.format(
                "📊 Found %d contraption(s). Analyzing...", contraptions.size())), false);            for (int i = 0; i < contraptions.size(); i++) {
                Object contraption = contraptions.get(i);
                final int contraptionNumber = i + 1; // Create final variable for lambda
                
                source.sendSuccess(() -> Component.literal(String.format(
                    "\n🎪 Contraption #%d:", contraptionNumber)), false);

                // Get contraption position if possible
                BlockPos contraptionPos = getContraptionPosition(contraption);
                if (contraptionPos != null) {
                    source.sendSuccess(() -> Component.literal(String.format(
                        "  📍 Position: %s", contraptionPos.toString())), false);
                }

                // Analyze blocks in this contraption
                Map<String, Integer> blockCounts = analyzeContraptionBlocks(contraption);
                int contraptionBlocks = blockCounts.values().stream().mapToInt(Integer::intValue).sum();
                int contraptionLittleTilesBlocks = blockCounts.entrySet().stream()
                    .filter(entry -> isLittleTilesBlock(entry.getKey()))
                    .mapToInt(Map.Entry::getValue)
                    .sum();

                totalBlocks += contraptionBlocks;
                totalLittleTilesBlocks += contraptionLittleTilesBlocks;

                source.sendSuccess(() -> Component.literal(String.format(
                    "  📦 Total blocks: %d", contraptionBlocks)), false);
                
                if (contraptionLittleTilesBlocks > 0) {
                    source.sendSuccess(() -> Component.literal(String.format(
                        "  🌟 LittleTiles blocks: %d (%.1f%%)", 
                        contraptionLittleTilesBlocks, 
                        contraptionBlocks > 0 ? (contraptionLittleTilesBlocks * 100.0 / contraptionBlocks) : 0)), false);
                } else {
                    source.sendSuccess(() -> Component.literal("  ⚪ No LittleTiles blocks found"), false);
                }

                // Show top block types
                if (!blockCounts.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("  📋 Block types:"), false);
                    blockCounts.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                        .limit(5)
                        .forEach(entry -> {
                            String blockName = getSimpleBlockName(entry.getKey());
                            boolean isLittleTiles = isLittleTilesBlock(entry.getKey());
                            String prefix = isLittleTiles ? "    🌟 " : "    📄 ";
                            source.sendSuccess(() -> Component.literal(String.format(
                                "%s%s: %d", prefix, blockName, entry.getValue())), false);
                        });
                }
            }

            // Create final variables for use in lambdas
            final int finalTotalBlocks = totalBlocks;
            final int finalTotalLittleTilesBlocks = totalLittleTilesBlocks;
            
            // Summary
            source.sendSuccess(() -> Component.literal(
                "\n📈 Total Summary:"), false);
            source.sendSuccess(() -> Component.literal(String.format(
                "  🎪 Contraptions: %d", contraptions.size())), false);
            source.sendSuccess(() -> Component.literal(String.format(
                "  📦 Total blocks: %d", finalTotalBlocks)), false);
            source.sendSuccess(() -> Component.literal(String.format(
                "  🌟 LittleTiles blocks: %d (%.1f%%)", 
                finalTotalLittleTilesBlocks, 
                finalTotalBlocks > 0 ? (finalTotalLittleTilesBlocks * 100.0 / finalTotalBlocks) : 0)), false);

            return 1;

        } catch (Exception e) {
            LOGGER.error("Error during contraption inspection: ", e);
            source.sendFailure(Component.literal("❌ Error during inspection: " + e.getMessage()));
            return 0;
        }
    }

    private static List<Object> findCreateContraptions(ServerLevel level) {
        List<Object> contraptions = new ArrayList<>();
        
        try {
            // Try to find Create contraptions through reflection
            // This will vary based on Create's internal structure
            
            // Look for contraption entities
            for (Entity entity : level.getAllEntities()) {
                String className = entity.getClass().getName();
                if (className.contains("contraption") || className.contains("Contraption")) {
                    LOGGER.debug("Found potential contraption entity: {}", className);
                    
                    // Try to get the actual contraption object
                    Object contraption = getContraptionFromEntity(entity);
                    if (contraption != null) {
                        contraptions.add(contraption);
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error finding contraptions: ", e);
        }
        
        return contraptions;
    }

    private static Object getContraptionFromEntity(Entity entity) {
        try {
            // Use reflection to get the contraption from the entity
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null && value.getClass().getName().toLowerCase().contains("contraption")) {
                    return value;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not extract contraption from entity: {}", e.getMessage());
        }
        return null;
    }

    private static BlockPos getContraptionPosition(Object contraption) {
        try {
            // Use reflection to get position information
            Method[] methods = contraption.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().toLowerCase().contains("pos") || 
                    method.getName().toLowerCase().contains("anchor")) {
                    method.setAccessible(true);
                    Object result = method.invoke(contraption);
                    if (result instanceof BlockPos) {
                        return (BlockPos) result;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not get contraption position: {}", e.getMessage());
        }
        return null;
    }

    private static Map<String, Integer> analyzeContraptionBlocks(Object contraption) {
        Map<String, Integer> blockCounts = new HashMap<>();
        
        try {
            // Use reflection to analyze blocks in the contraption
            Field[] fields = contraption.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(contraption);
                  if (value instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getValue() != null) {
                            String className = entry.getValue().getClass().getName();
                            if (className.toLowerCase().contains("blockinfo") || 
                                className.toLowerCase().contains("blockstate")) {
                                
                                BlockState blockState = extractBlockState(entry.getValue());
                                if (blockState != null) {
                                    String blockName = getBlockRegistryName(blockState);
                                    blockCounts.merge(blockName, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.debug("Could not analyze contraption blocks: {}", e.getMessage());
        }
        
        return blockCounts;
    }

    private static BlockState extractBlockState(Object blockInfo) {
        try {
            // Try to extract BlockState from BlockInfo object
            Field[] fields = blockInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(blockInfo);
                if (value instanceof BlockState) {
                    return (BlockState) value;
                }
            }
            
            // Try methods
            Method[] methods = blockInfo.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().toLowerCase().contains("state") && 
                    method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    Object result = method.invoke(blockInfo);
                    if (result instanceof BlockState) {
                        return (BlockState) result;
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.debug("Could not extract BlockState: {}", e.getMessage());
        }
        return null;
    }

    private static String getBlockRegistryName(BlockState blockState) {
        try {
            Block block = blockState.getBlock();
            ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
            return location.toString();
        } catch (Exception e) {
            return blockState.getBlock().getClass().getName();
        }
    }

    private static boolean isLittleTilesBlock(String blockName) {
        return blockName.toLowerCase().contains("littletiles") ||
               blockName.toLowerCase().contains("littletile") ||
               blockName.toLowerCase().contains("team.creative.littletiles");
    }

    private static String getSimpleBlockName(String fullName) {
        if (fullName.contains(":")) {
            String[] parts = fullName.split(":");
            return parts.length > 1 ? parts[1] : fullName;
        }
        if (fullName.contains(".")) {
            String[] parts = fullName.split("\\.");
            return parts[parts.length - 1];
        }
        return fullName;
    }
}
