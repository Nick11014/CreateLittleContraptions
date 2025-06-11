package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * Comando para fazer assembly (re-criar) de contraptions
 * Baseado na análise da timeline de disassembly
 */
public class ContraptionAssemblyCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-assembly")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionAssemblyCommand::executeStatus)
            .then(Commands.literal("create-simple")
                .executes(ContraptionAssemblyCommand::executeCreateSimple))
            .then(Commands.literal("list-available")
                .executes(ContraptionAssemblyCommand::executeListAvailable))
            .then(Commands.literal("info")
                .executes(ContraptionAssemblyCommand::executeInfo)));
    }
    
    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.literal("§eContraption Assembly Command"));
        source.sendSystemMessage(Component.literal("§7Create new contraptions from existing structures"));
        source.sendSystemMessage(Component.literal("§7Use '/contraption-assembly info' for more details"));
        return 1;
    }
    
    private static int executeCreateSimple(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            // Buscar contraptions próximas
            if (source.getLevel() instanceof ServerLevel level) {
                BlockPos playerPos = BlockPos.containing(source.getPosition());
                
                // Buscar entidades de contraption num raio de 50 blocos
                level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(50))
                    .forEach(contraption -> {
                        source.sendSystemMessage(Component.literal("§7Found contraption: " + 
                            contraption.getClass().getSimpleName() + " at " + contraption.blockPosition()));
                    });
                
                source.sendSystemMessage(Component.literal("§aScanned for contraptions in 50 block radius"));
                source.sendSystemMessage(Component.literal("§7Assembly functionality: Basic structure analysis"));
            }
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError during assembly scan: " + e.getMessage()));
            return 0;
        }
        
        return 1;
    }
    
    private static int executeListAvailable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            if (source.getLevel() instanceof ServerLevel level) {
                BlockPos playerPos = BlockPos.containing(source.getPosition());
                
                source.sendSystemMessage(Component.literal("§e=== Available Contraptions for Assembly ==="));
                
                var contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(100));
                
                if (contraptions.isEmpty()) {
                    source.sendSystemMessage(Component.literal("§7No contraptions found in 100 block radius"));
                } else {
                    for (int i = 0; i < contraptions.size(); i++) {
                        Entity contraption = contraptions.get(i);
                        source.sendSystemMessage(Component.literal(String.format("§f%d. §e%s §7at (%.1f, %.1f, %.1f)", 
                            i + 1, 
                            contraption.getClass().getSimpleName(),
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
    
    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.literal("§e=== Contraption Assembly Info ==="));
        source.sendSystemMessage(Component.literal("§7This command helps with contraption creation and analysis."));
        source.sendSystemMessage(Component.literal("§7Available subcommands:"));
        source.sendSystemMessage(Component.literal("§f- create-simple §7- Scan for existing contraptions"));
        source.sendSystemMessage(Component.literal("§f- list-available §7- List contraptions in area"));
        source.sendSystemMessage(Component.literal("§f- info §7- Show this help"));
        source.sendSystemMessage(Component.literal("§7Assembly creates new contraptions from block structures."));
        return 1;
    }
}
