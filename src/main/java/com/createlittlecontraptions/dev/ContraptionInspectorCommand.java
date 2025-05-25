package com.createlittlecontraptions.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Advanced contraption inspection command for debugging CreateLittleContraptions integration.
 * Lists all contraptions, their types, blocks, and specifically highlights LittleTiles blocks.
 */
public class ContraptionInspectorCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("contraption-inspect")
                .requires(source -> source.hasPermission(2))
                .executes(ContraptionInspectorCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level level = source.getLevel();
        
        try {
            source.sendSuccess(() -> Component.literal("🔍 Scanning for Create contraptions..."), false);
            
            // Find all contraptions in the level
            List<ContraptionInfo> contraptions = findAllContraptions(level);
            
            if (contraptions.isEmpty()) {
                source.sendSuccess(() -> Component.literal("❌ No contraptions found in this dimension"), false);
                return 1;
            }
            
            source.sendSuccess(() -> Component.literal("✅ Found " + contraptions.size() + " contraption(s):"), false);
            source.sendSuccess(() -> Component.literal(""), false);
            
            // Display detailed info for each contraption
            for (int i = 0; i < contraptions.size(); i++) {
                ContraptionInfo info = contraptions.get(i);
                displayContraptionInfo(source, i + 1, info);
                source.sendSuccess(() -> Component.literal(""), false);
            }
            
            // Summary statistics
            int totalBlocks = contraptions.stream().mapToInt(c -> c.totalBlocks).sum();
            int totalLittleTilesBlocks = contraptions.stream().mapToInt(c -> c.littleTilesBlocks).sum();
            
            source.sendSuccess(() -> Component.literal("📊 Summary:"), false);
            source.sendSuccess(() -> Component.literal("  Total Contraptions: " + contraptions.size()), false);
            source.sendSuccess(() -> Component.literal("  Total Blocks: " + totalBlocks), false);
            source.sendSuccess(() -> Component.literal("  LittleTiles Blocks: " + totalLittleTilesBlocks), false);
            source.sendSuccess(() -> Component.literal("  Integration Status: " + (totalLittleTilesBlocks > 0 ? "⚠️ Issues Detected" : "✅ No Issues")), false);
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("❌ Error during contraption inspection: " + e.getMessage()));
            LOGGER.error("Error in contraption inspection command", e);
        }
        
        return 1;
    }
    
    private static void displayContraptionInfo(CommandSourceStack source, int index, ContraptionInfo info) {
        // Header
        source.sendSuccess(() -> Component.literal("🎪 [" + index + "] " + info.entityType + 
            " at (" + String.format("%.1f", info.x) + ", " + String.format("%.1f", info.y) + 
            ", " + String.format("%.1f", info.z) + ")"), false);
        
        source.sendSuccess(() -> Component.literal("  📦 Total Blocks: " + info.totalBlocks), false);
        
        if (info.littleTilesBlocks > 0) {
            source.sendSuccess(() -> Component.literal("  🧱 LittleTiles Blocks: §c" + info.littleTilesBlocks + "§r"), false);
        } else {
            source.sendSuccess(() -> Component.literal("  🧱 LittleTiles Blocks: §a0§r"), false);
        }
        
        // Block type breakdown
        source.sendSuccess(() -> Component.literal("  📋 Block Types:"), false);
        for (Map.Entry<String, Integer> entry : info.blockTypes.entrySet()) {
            String blockName = entry.getKey();
            int count = entry.getValue();
            boolean isLittleTiles = isLittleTilesBlock(blockName);
            
            String color = isLittleTiles ? "§e" : "§7";
            String highlight = isLittleTiles ? " §c[LittleTiles]§r" : "";
            
            source.sendSuccess(() -> Component.literal("    " + color + blockName + "§r: " + count + highlight), false);
        }
        
        // NBT info for LittleTiles blocks
        if (info.littleTilesBlocks > 0) {
            source.sendSuccess(() -> Component.literal("  🔧 LittleTiles NBT Status:"), false);
            for (LittleTilesBlockInfo ltInfo : info.littleTilesBlocksInfo) {
                String nbtStatus = ltInfo.hasNBT ? "§a✓ Has NBT§r" : "§c✗ No NBT§r";
                source.sendSuccess(() -> Component.literal("    " + ltInfo.blockName + " " + nbtStatus + 
                    " (Size: " + ltInfo.nbtSize + " bytes)"), false);
            }
        }
    }
    
    private static List<ContraptionInfo> findAllContraptions(Level level) {
        List<ContraptionInfo> contraptions = new ArrayList<>();        try {
            // Find all entities that might be contraptions
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    String entityClassName = entity.getClass().getName().toLowerCase();
                
                    // Check if this entity is a Create contraption
                    if (entityClassName.contains("contraption") || 
                        entityClassName.contains("create") ||
                        entityClassName.contains("superglue")) {
                        
                        ContraptionInfo info = analyzeContraption(entity);
                        if (info != null) {
                            contraptions.add(info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error finding contraptions", e);
        }
        
        return contraptions;
    }
    
    private static ContraptionInfo analyzeContraption(Entity entity) {
        try {
            ContraptionInfo info = new ContraptionInfo();
            info.entityType = entity.getClass().getSimpleName();
            info.x = entity.getX();
            info.y = entity.getY();
            info.z = entity.getZ();
            info.blockTypes = new HashMap<>();
            info.littleTilesBlocksInfo = new ArrayList<>();
            
            // Try to get the contraption data using reflection
            Object contraption = getContraptionFromEntity(entity);
            if (contraption == null) {
                LOGGER.debug("Could not extract contraption from entity: " + entity.getClass().getName());
                return null;
            }
            
            // Get blocks from contraption
            Map<BlockPos, Object> blocks = getBlocksFromContraption(contraption);
            if (blocks == null) {
                LOGGER.debug("Could not extract blocks from contraption");
                return null;
            }
            
            info.totalBlocks = blocks.size();
            
            // Analyze each block
            for (Map.Entry<BlockPos, Object> entry : blocks.entrySet()) {
                BlockPos pos = entry.getKey();
                Object blockInfo = entry.getValue();
                
                BlockState blockState = getBlockStateFromBlockInfo(blockInfo);
                CompoundTag nbt = getNBTFromBlockInfo(blockInfo);
                
                if (blockState != null) {
                    String blockName = blockState.getBlock().getClass().getSimpleName();
                    info.blockTypes.merge(blockName, 1, Integer::sum);
                    
                    // Check if this is a LittleTiles block
                    if (isLittleTilesBlock(blockState)) {
                        info.littleTilesBlocks++;
                        
                        LittleTilesBlockInfo ltInfo = new LittleTilesBlockInfo();
                        ltInfo.blockName = blockName;
                        ltInfo.position = pos;
                        ltInfo.hasNBT = (nbt != null && !nbt.isEmpty());
                        ltInfo.nbtSize = nbt != null ? nbt.toString().length() : 0;
                        
                        info.littleTilesBlocksInfo.add(ltInfo);
                    }
                }
            }
            
            return info;
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing contraption: " + entity.getClass().getName(), e);
            return null;
        }
    }
    
    // Reflection helpers to extract contraption data
    private static Object getContraptionFromEntity(Entity entity) {
        try {
            // Try common method names for getting contraption
            String[] methodNames = {"getContraption", "contraption", "getStructure"};
            
            for (String methodName : methodNames) {
                try {
                    Method method = entity.getClass().getMethod(methodName);
                    return method.invoke(entity);
                } catch (NoSuchMethodException ignored) {
                    // Try next method
                }
            }
            
            // Try to access contraption field directly
            try {
                var field = entity.getClass().getDeclaredField("contraption");
                field.setAccessible(true);
                return field.get(entity);
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            LOGGER.debug("Could not get contraption from entity", e);
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static Map<BlockPos, Object> getBlocksFromContraption(Object contraption) {
        try {
            // Try common field/method names for getting blocks
            String[] fieldNames = {"blocks", "blockMap", "structure"};
            String[] methodNames = {"getBlocks", "getAllBlocks", "getBlockMap"};
            
            // Try methods first
            for (String methodName : methodNames) {
                try {
                    Method method = contraption.getClass().getMethod(methodName);
                    Object result = method.invoke(contraption);
                    if (result instanceof Map) {
                        return (Map<BlockPos, Object>) result;
                    }
                } catch (NoSuchMethodException ignored) {}
            }
            
            // Try fields
            for (String fieldName : fieldNames) {
                try {
                    var field = contraption.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object result = field.get(contraption);
                    if (result instanceof Map) {
                        return (Map<BlockPos, Object>) result;
                    }
                } catch (Exception ignored) {}
            }
            
        } catch (Exception e) {
            LOGGER.debug("Could not get blocks from contraption", e);
        }
        
        return null;
    }
    
    private static BlockState getBlockStateFromBlockInfo(Object blockInfo) {
        try {
            // Try common method names
            String[] methodNames = {"state", "getState", "getBlockState"};
            
            for (String methodName : methodNames) {
                try {
                    Method method = blockInfo.getClass().getMethod(methodName);
                    Object result = method.invoke(blockInfo);
                    if (result instanceof BlockState) {
                        return (BlockState) result;
                    }
                } catch (NoSuchMethodException ignored) {}
            }
            
        } catch (Exception e) {
            LOGGER.debug("Could not get BlockState from blockInfo", e);
        }
        
        return null;
    }
    
    private static CompoundTag getNBTFromBlockInfo(Object blockInfo) {
        try {
            // Try common method names
            String[] methodNames = {"nbt", "getNbt", "getTileEntity", "getTag"};
            
            for (String methodName : methodNames) {
                try {
                    Method method = blockInfo.getClass().getMethod(methodName);
                    Object result = method.invoke(blockInfo);
                    if (result instanceof CompoundTag) {
                        return (CompoundTag) result;
                    }
                } catch (NoSuchMethodException ignored) {}
            }
            
        } catch (Exception e) {
            LOGGER.debug("Could not get NBT from blockInfo", e);
        }
        
        return null;
    }
    
    private static boolean isLittleTilesBlock(BlockState blockState) {
        if (blockState == null) return false;
        return isLittleTilesBlock(blockState.getBlock().getClass().getName());
    }
    
    private static boolean isLittleTilesBlock(String blockClassName) {
        String lowerName = blockClassName.toLowerCase();
        return lowerName.contains("team.creative.littletiles") ||
               lowerName.contains("littletiles") ||
               lowerName.contains("littletile") ||
               lowerName.contains("little");
    }
    
    // Data classes
    private static class ContraptionInfo {
        String entityType;
        double x, y, z;
        int totalBlocks;
        int littleTilesBlocks;
        Map<String, Integer> blockTypes;
        List<LittleTilesBlockInfo> littleTilesBlocksInfo;
    }
    
    private static class LittleTilesBlockInfo {
        String blockName;
        BlockPos position;
        boolean hasNBT;
        int nbtSize;
    }
}
