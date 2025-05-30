package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContraptionRenderCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Estado global para contraptions selecionadas por jogador
    private static final Map<String, SelectedContraption> playerSelections = new ConcurrentHashMap<>();
    
    // Mapa para controle de renderização de blocos específicos
    private static final Map<String, Set<BlockPos>> disabledBlocks = new ConcurrentHashMap<>();
    
    // Conjunto para rastrear contraptions com renderização desabilitada
    private static final Set<UUID> disabledContraptions = ConcurrentHashMap.newKeySet();
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-render")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionRenderCommand::executeStatus)
            .then(Commands.literal("list")
                .executes(ContraptionRenderCommand::executeList))
            .then(Commands.literal("select")
                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                    .executes(ContraptionRenderCommand::executeSelect)))
            .then(Commands.literal("info")
                .executes(ContraptionRenderCommand::executeInfo))
            .then(Commands.literal("disable")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(ContraptionRenderCommand::executeDisableBlock)))
            .then(Commands.literal("enable")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(ContraptionRenderCommand::executeEnableBlock)))
            .then(Commands.literal("disable-all")
                .executes(ContraptionRenderCommand::executeDisableAll))
            .then(Commands.literal("enable-all")
                .executes(ContraptionRenderCommand::executeEnableAll))
            .then(Commands.literal("status")
                .executes(ContraptionRenderCommand::executeRenderStatus)));
    }
    
    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        
        source.sendSystemMessage(Component.literal("§e=== CONTRAPTION RENDER CONTROL ==="));
        source.sendSystemMessage(Component.literal("§7Available commands:"));
        source.sendSystemMessage(Component.literal("§f/contraption-render list §7- List all contraptions"));
        source.sendSystemMessage(Component.literal("§f/contraption-render select <id> §7- Select contraption by ID"));
        source.sendSystemMessage(Component.literal("§f/contraption-render info §7- Show selected contraption info"));
        source.sendSystemMessage(Component.literal("§f/contraption-render disable <pos> §7- Disable rendering of block at position"));
        source.sendSystemMessage(Component.literal("§f/contraption-render enable <pos> §7- Enable rendering of block at position"));
        source.sendSystemMessage(Component.literal("§f/contraption-render disable-all §7- Disable rendering of all blocks"));
        source.sendSystemMessage(Component.literal("§f/contraption-render enable-all §7- Enable rendering of all blocks"));
        source.sendSystemMessage(Component.literal("§f/contraption-render status §7- Show current render status"));
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected != null) {
            source.sendSystemMessage(Component.literal("§aCurrently selected: Contraption #" + selected.id + " at " + selected.position));
        } else {
            source.sendSystemMessage(Component.literal("§cNo contraption selected"));
        }
        
        return 1;
    }
    
    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("§e=== CONTRAPTIONS LIST ==="));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("§cNo contraptions found in the world"));
            return 0;
        }
        
        for (int i = 0; i < contraptionEntities.size(); i++) {
            Entity entity = contraptionEntities.get(i);
            String type = entity.getClass().getSimpleName();
            String position = entity.blockPosition().toShortString();
            source.sendSystemMessage(Component.literal("§f" + i + ": §a" + type + " §7at " + position));
        }
        
        source.sendSystemMessage(Component.literal("§7Use /contraption-render select <id> to select one"));
        
        return 1;
    }
    
    private static int executeSelect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        int id = IntegerArgumentType.getInteger(context, "id");
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (id >= contraptionEntities.size()) {
            source.sendFailure(Component.literal("§cInvalid ID. Use /contraption-render list to see available contraptions"));
            return 0;
        }
        
        Entity entity = contraptionEntities.get(id);
        SelectedContraption selected = new SelectedContraption(
            id,
            entity.getUUID(),
            entity.getClass().getSimpleName(),
            entity.blockPosition()
        );
        
        playerSelections.put(playerName, selected);
        
        source.sendSystemMessage(Component.literal("§aSelected contraption #" + id + " (" + selected.type + ") at " + selected.position.toShortString()));
        
        // Inicializar mapa de blocos desabilitados para esta contraption se não existir
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        disabledBlocks.putIfAbsent(contraptionKey, new HashSet<>());
        
        return 1;
    }
    
    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected == null) {
            source.sendFailure(Component.literal("§cNo contraption selected. Use /contraption-render select <id> first"));
            return 0;
        }
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        // Encontrar a contraption atual
        AbstractContraptionEntity contraptionEntity = findContraptionByUUID(serverLevel, selected.uuid);
        if (contraptionEntity == null) {
            source.sendFailure(Component.literal("§cSelected contraption no longer exists"));
            playerSelections.remove(playerName);
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("§e=== SELECTED CONTRAPTION INFO ==="));
        source.sendSystemMessage(Component.literal("§fID: §a" + selected.id));
        source.sendSystemMessage(Component.literal("§fType: §a" + selected.type));
        source.sendSystemMessage(Component.literal("§fPosition: §a" + contraptionEntity.blockPosition().toShortString()));
        source.sendSystemMessage(Component.literal("§fUUID: §7" + selected.uuid.toString()));
        
        int totalBlocks = getBlockCount(contraptionEntity);
        int littleTilesCount = countLittleTiles(contraptionEntity);
        
        source.sendSystemMessage(Component.literal("§fTotal Blocks: §b" + totalBlocks));
        source.sendSystemMessage(Component.literal("§fLittleTiles Blocks: §e" + littleTilesCount));
        
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        Set<BlockPos> disabled = disabledBlocks.getOrDefault(contraptionKey, new HashSet<>());
        
        source.sendSystemMessage(Component.literal("§fDisabled Blocks: §c" + disabled.size()));
        if (!disabled.isEmpty()) {
            source.sendSystemMessage(Component.literal("§7Disabled positions:"));
            disabled.forEach(pos -> 
                source.sendSystemMessage(Component.literal("  §c- " + pos.toShortString()))
            );
        }
        
        return 1;
    }
    
    private static int executeDisableBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected == null) {
            source.sendFailure(Component.literal("§cNo contraption selected. Use /contraption-render select <id> first"));
            return 0;
        }
        
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        Set<BlockPos> disabled = disabledBlocks.computeIfAbsent(contraptionKey, k -> new HashSet<>());
        
        if (disabled.add(pos)) {
            source.sendSystemMessage(Component.literal("§aDisabled rendering for block at " + pos.toShortString()));
            LOGGER.info("Player {} disabled rendering for block at {} in contraption {}", playerName, pos, selected.uuid);
        } else {
            source.sendSystemMessage(Component.literal("§7Block at " + pos.toShortString() + " is already disabled"));
        }
        
        return 1;
    }
    
    private static int executeEnableBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected == null) {
            source.sendFailure(Component.literal("§cNo contraption selected. Use /contraption-render select <id> first"));
            return 0;
        }
        
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        Set<BlockPos> disabled = disabledBlocks.get(contraptionKey);
        
        if (disabled != null && disabled.remove(pos)) {
            source.sendSystemMessage(Component.literal("§aEnabled rendering for block at " + pos.toShortString()));
            LOGGER.info("Player {} enabled rendering for block at {} in contraption {}", playerName, pos, selected.uuid);
        } else {
            source.sendSystemMessage(Component.literal("§7Block at " + pos.toShortString() + " was not disabled"));
        }
        
        return 1;
    }
    
    private static int executeDisableAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected == null) {
            source.sendFailure(Component.literal("§cNo contraption selected. Use /contraption-render select <id> first"));
            return 0;
        }
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        AbstractContraptionEntity contraptionEntity = findContraptionByUUID(serverLevel, selected.uuid);
        if (contraptionEntity == null) {
            source.sendFailure(Component.literal("§cSelected contraption no longer exists"));
            playerSelections.remove(playerName);
            return 0;
        }
        
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        Set<BlockPos> disabled = disabledBlocks.computeIfAbsent(contraptionKey, k -> new HashSet<>());
        
        // Adicionar todas as posições de blocos da contraption
        Set<BlockPos> allBlocks = getAllBlockPositions(contraptionEntity);
        int addedCount = 0;
        for (BlockPos pos : allBlocks) {
            if (disabled.add(pos)) {
                addedCount++;
            }
        }
        
        source.sendSystemMessage(Component.literal("§aDisabled rendering for all " + addedCount + " blocks in contraption"));
        LOGGER.info("Player {} disabled rendering for all blocks in contraption {}", playerName, selected.uuid);
        
        return 1;
    }
    
    private static int executeEnableAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected == null) {
            source.sendFailure(Component.literal("§cNo contraption selected. Use /contraption-render select <id> first"));
            return 0;
        }
        
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        Set<BlockPos> disabled = disabledBlocks.get(contraptionKey);
        
        if (disabled != null && !disabled.isEmpty()) {
            int count = disabled.size();
            disabled.clear();
            source.sendSystemMessage(Component.literal("§aEnabled rendering for all " + count + " blocks in contraption"));
            LOGGER.info("Player {} enabled rendering for all blocks in contraption {}", playerName, selected.uuid);
        } else {
            source.sendSystemMessage(Component.literal("§7No blocks were disabled in this contraption"));
        }
        
        return 1;
    }
    
    private static int executeRenderStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = getPlayerName(source);
        
        SelectedContraption selected = playerSelections.get(playerName);
        if (selected == null) {
            source.sendFailure(Component.literal("§cNo contraption selected. Use /contraption-render select <id> first"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("§e=== RENDER STATUS ==="));
        source.sendSystemMessage(Component.literal("§fSelected: §a" + selected.type + " #" + selected.id));
        
        String contraptionKey = getContraptionKey(playerName, selected.uuid);
        Set<BlockPos> disabled = disabledBlocks.getOrDefault(contraptionKey, new HashSet<>());
        
        if (disabled.isEmpty()) {
            source.sendSystemMessage(Component.literal("§aAll blocks are visible"));
        } else {
            source.sendSystemMessage(Component.literal("§c" + disabled.size() + " blocks are hidden:"));
            disabled.forEach(pos -> 
                source.sendSystemMessage(Component.literal("  §7- " + pos.toShortString()))
            );
            source.sendSystemMessage(Component.literal("§7Use /contraption-render enable-all to show all blocks"));
            source.sendSystemMessage(Component.literal("§7Use /contraption-render enable <pos> to show specific blocks"));
        }
        
        return 1;
    }
    
    // ============= UTILITY METHODS =============
    
    private static String getPlayerName(CommandSourceStack source) {
        try {
            return source.getPlayerOrException().getName().getString();
        } catch (CommandSyntaxException e) {
            return "Server";
        }
    }
    
    private static String getContraptionKey(String playerName, UUID contraptionUUID) {
        return playerName + ":" + contraptionUUID.toString();
    }
    
    private static List<Entity> findContraptionEntities(ServerLevel level) {
        List<Entity> contraptions = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof AbstractContraptionEntity) {
                contraptions.add(entity);
            }
        }
        return contraptions;
    }
    
    private static AbstractContraptionEntity findContraptionByUUID(ServerLevel level, UUID uuid) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof AbstractContraptionEntity && entity.getUUID().equals(uuid)) {
                return (AbstractContraptionEntity) entity;
            }
        }
        return null;
    }
    
    private static int getBlockCount(AbstractContraptionEntity entity) {
        try {
            Field contraptionField = AbstractContraptionEntity.class.getDeclaredField("contraption");
            contraptionField.setAccessible(true);
            Object contraption = contraptionField.get(entity);
            
            Method getBlocksMethod = contraption.getClass().getMethod("getBlocks");
            Object blocksMap = getBlocksMethod.invoke(contraption);
            
            if (blocksMap instanceof Map) {
                return ((Map<?, ?>) blocksMap).size();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get block count: {}", e.getMessage());
        }
        return 0;
    }
    
    private static int countLittleTiles(AbstractContraptionEntity entity) {
        // Placeholder implementation
        return 0;
    }
    
    private static Set<BlockPos> getAllBlockPositions(AbstractContraptionEntity entity) {
        Set<BlockPos> positions = new HashSet<>();
        try {
            Field contraptionField = AbstractContraptionEntity.class.getDeclaredField("contraption");
            contraptionField.setAccessible(true);
            Object contraption = contraptionField.get(entity);
            
            Method getBlocksMethod = contraption.getClass().getMethod("getBlocks");
            Object blocksMap = getBlocksMethod.invoke(contraption);
            
            if (blocksMap instanceof Map) {
                for (Object key : ((Map<?, ?>) blocksMap).keySet()) {
                    if (key instanceof BlockPos) {
                        positions.add((BlockPos) key);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get block positions: {}", e.getMessage());
        }
        return positions;
    }
    
    // ============= PUBLIC API FOR INTEGRATION =============
    
    public static boolean isContraptionRenderingDisabled(UUID contraptionUUID) {
        return disabledContraptions.contains(contraptionUUID);
    }
    
    public static void setContraptionRenderingDisabled(UUID contraptionUUID, boolean disabled) {
        if (disabled) {
            disabledContraptions.add(contraptionUUID);
        } else {
            disabledContraptions.remove(contraptionUUID);
        }
    }
    
    public static boolean isBlockRenderingDisabled(UUID contraptionUUID, BlockPos blockPos) {
        // Check for all players if any disabled this block
        for (Map.Entry<String, SelectedContraption> entry : playerSelections.entrySet()) {
            String playerName = entry.getKey();
            SelectedContraption selected = entry.getValue();
            
            if (selected.uuid.equals(contraptionUUID)) {
                String contraptionKey = getContraptionKey(playerName, contraptionUUID);
                Set<BlockPos> disabled = disabledBlocks.get(contraptionKey);
                
                if (disabled != null && disabled.contains(blockPos)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // ============= DATA CLASSES =============
    
    public static class SelectedContraption {
        public final int id;
        public final UUID uuid;
        public final String type;
        public final BlockPos position;
        
        public SelectedContraption(int id, UUID uuid, String type, BlockPos position) {
            this.id = id;
            this.uuid = uuid;
            this.type = type;
            this.position = position;
        }
    }
}
