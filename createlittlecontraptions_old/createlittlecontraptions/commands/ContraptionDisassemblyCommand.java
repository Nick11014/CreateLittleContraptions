package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Command for contraption disassembly operations
 * Provides functionality to analyze and disassemble Create contraptions
 */
public class ContraptionDisassemblyCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-disassembly")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionDisassemblyCommand::executeStatus)
            .then(Commands.literal("scan")
                .executes(ContraptionDisassemblyCommand::executeScan))
            .then(Commands.literal("list")
                .executes(ContraptionDisassemblyCommand::executeList))
            .then(Commands.literal("analyze")
                .executes(ContraptionDisassemblyCommand::executeAnalyze))
            .then(Commands.literal("info")
                .executes(ContraptionDisassemblyCommand::executeInfo)));
    }
    
    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.literal("§eContraption Disassembly Command"));
        source.sendSystemMessage(Component.literal("§7Analyze and manage Create contraptions"));
        source.sendSystemMessage(Component.literal("§7Use '/contraption-disassembly info' for more details"));
        return 1;
    }
    
    private static int executeScan(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            if (source.getLevel() instanceof ServerLevel level) {
                BlockPos playerPos = BlockPos.containing(source.getPosition());
                
                source.sendSystemMessage(Component.literal("§eScanning for contraptions..."));
                
                var contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(50));
                
                if (contraptions.isEmpty()) {
                    source.sendSystemMessage(Component.literal("§7No contraptions found in 50 block radius"));
                } else {
                    source.sendSystemMessage(Component.literal("§aFound " + contraptions.size() + " contraption(s):"));
                    for (AbstractContraptionEntity contraption : contraptions) {
                        String type = contraption.getClass().getSimpleName();
                        BlockPos pos = contraption.blockPosition();
                        source.sendSystemMessage(Component.literal(String.format("§f- %s §7at (%d, %d, %d)", 
                            type, pos.getX(), pos.getY(), pos.getZ())));
                    }
                }
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError during scan: " + e.getMessage()));
            return 0;
        }
        
        return 1;
    }
    
    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            if (source.getLevel() instanceof ServerLevel level) {
                BlockPos playerPos = BlockPos.containing(source.getPosition());
                
                source.sendSystemMessage(Component.literal("§e=== Contraption List ==="));
                
                var contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(100));
                
                if (contraptions.isEmpty()) {
                    source.sendSystemMessage(Component.literal("§7No contraptions found in 100 block radius"));
                } else {
                    source.sendSystemMessage(Component.literal("§aFound " + contraptions.size() + " contraption(s):"));
                    
                    for (int i = 0; i < contraptions.size(); i++) {
                        AbstractContraptionEntity contraption = contraptions.get(i);
                        String status = contraption.isAlive() ? "§aActive" : "§cInactive";
                        source.sendSystemMessage(Component.literal(String.format("§f%d. §e%s %s §7at (%.1f, %.1f, %.1f)", 
                            i + 1, 
                            contraption.getClass().getSimpleName(),
                            status,
                            contraption.getX(), contraption.getY(), contraption.getZ())));
                    }
                }
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError listing contraptions: " + e.getMessage()));
            return 0;
        }
        
        return 1;
    }
    
    private static int executeAnalyze(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            if (source.getLevel() instanceof ServerLevel level) {
                BlockPos playerPos = BlockPos.containing(source.getPosition());
                
                source.sendSystemMessage(Component.literal("§e=== Contraption Analysis ==="));
                
                var contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(25));
                
                if (contraptions.isEmpty()) {
                    source.sendSystemMessage(Component.literal("§7No contraptions found in 25 block radius for analysis"));
                } else {
                    for (AbstractContraptionEntity contraption : contraptions) {
                        source.sendSystemMessage(Component.literal("§f--- " + contraption.getClass().getSimpleName() + " ---"));
                        source.sendSystemMessage(Component.literal("§7Position: " + contraption.blockPosition()));
                        source.sendSystemMessage(Component.literal("§7Alive: " + (contraption.isAlive() ? "§aYes" : "§cNo")));
                        source.sendSystemMessage(Component.literal("§7UUID: " + contraption.getUUID().toString().substring(0, 8) + "..."));
                        
                        if (contraption.getContraption() != null) {
                            var contraptionData = contraption.getContraption();
                            source.sendSystemMessage(Component.literal("§7Blocks: " + contraptionData.getBlocks().size()));
                        } else {
                            source.sendSystemMessage(Component.literal("§7Blocks: §cNo data available"));
                        }
                        
                        source.sendSystemMessage(Component.literal(""));
                    }
                }
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError during analysis: " + e.getMessage()));
            return 0;
        }
        
        return 1;
    }
    
    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.literal("§e=== Contraption Disassembly Info ==="));
        source.sendSystemMessage(Component.literal("§7This command provides contraption analysis and management tools."));
        source.sendSystemMessage(Component.literal("§7Available subcommands:"));
        source.sendSystemMessage(Component.literal("§f- scan §7- Quick scan for nearby contraptions (50 blocks)"));
        source.sendSystemMessage(Component.literal("§f- list §7- List all contraptions in area (100 blocks)"));
        source.sendSystemMessage(Component.literal("§f- analyze §7- Detailed analysis of nearby contraptions (25 blocks)"));
        source.sendSystemMessage(Component.literal("§f- info §7- Show this help"));
        source.sendSystemMessage(Component.literal("§7Use these commands to debug and manage Create contraptions."));
        return 1;
    }
}