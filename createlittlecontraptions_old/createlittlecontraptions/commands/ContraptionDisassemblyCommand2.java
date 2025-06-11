package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Comando para fazer disassembly de contraptions - versão simplificada
 */
public class ContraptionDisassemblyCommand2 {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-disassembly")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionDisassemblyCommand2::executeStatus)
            .then(Commands.literal("execute")
                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                    .executes(ContraptionDisassemblyCommand2::executeDisassembly)))
            .then(Commands.literal("list")
                .executes(ContraptionDisassemblyCommand2::executeList))
            .then(Commands.literal("info")
                .executes(ContraptionDisassemblyCommand2::executeInfo)));
    }
    
    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.literal("§eContraption Disassembly Command"));
        source.sendSystemMessage(Component.literal("§7This command allows safe disassembly of Create contraptions."));
        source.sendSystemMessage(Component.literal("§7Use '/contraption-disassembly list' to see available contraptions"));
        return 1;
    }
    
    private static int executeDisassembly(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int contraptionId = IntegerArgumentType.getInteger(context, "id");
        
        try {
            if (source.getLevel() instanceof ServerLevel level) {
                Vec3 pos = source.getPosition();
                BlockPos playerPos = BlockPos.containing(pos);
                
                var contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(100));
                
                if (contraptionId >= contraptions.size() || contraptionId < 0) {
                    source.sendFailure(Component.literal("§cInvalid contraption ID. Use /contraption-disassembly list"));
                    return 0;
                }
                
                AbstractContraptionEntity contraptionEntity = contraptions.get(contraptionId);
                
                source.sendSystemMessage(Component.literal("§e=== STARTING CONTRAPTION DISASSEMBLY ==="));
                source.sendSystemMessage(Component.literal("§7Contraption: " + contraptionEntity.getClass().getSimpleName()));
                source.sendSystemMessage(Component.literal("§7Position: " + contraptionEntity.blockPosition()));
                source.sendSystemMessage(Component.literal("§7UUID: " + contraptionEntity.getUUID()));
                
                try {
                    // Análise da contraption antes do disassembly
                    if (contraptionEntity.getContraption() != null) {
                        source.sendSystemMessage(Component.literal("§7Contraption blocks: " + 
                            contraptionEntity.getContraption().getBlocks().size()));
                    }
                    
                    source.sendSystemMessage(Component.literal("§aExecuting disassembly..."));
                    
                    // Executar o disassembly
                    contraptionEntity.disassemble();
                    
                    source.sendSystemMessage(Component.literal("§a=== DISASSEMBLY COMPLETED ==="));
                    
                } catch (Exception e) {
                    source.sendFailure(Component.literal("§cError during disassembly: " + e.getMessage()));
                    return 0;
                }
            }
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
        
        return 1;
    }
    
    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            if (source.getLevel() instanceof ServerLevel level) {
                Vec3 pos = source.getPosition();
                BlockPos playerPos = BlockPos.containing(pos);
                
                var contraptions = level.getEntitiesOfClass(AbstractContraptionEntity.class, 
                    new net.minecraft.world.phys.AABB(playerPos).inflate(100));
                
                source.sendSystemMessage(Component.literal("§e=== Available Contraptions for Disassembly ==="));
                
                if (contraptions.isEmpty()) {
                    source.sendSystemMessage(Component.literal("§7No contraptions found in 100 block radius"));
                } else {
                    for (int i = 0; i < contraptions.size(); i++) {
                        AbstractContraptionEntity contraption = contraptions.get(i);
                        source.sendSystemMessage(Component.literal(String.format("§f%d. §e%s §7at %s", 
                            i, 
                            contraption.getClass().getSimpleName(),
                            contraption.blockPosition())));
                    }
                    source.sendSystemMessage(Component.literal("§7Use '/contraption-disassembly execute <id>' to disassemble"));
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
        source.sendSystemMessage(Component.literal("§e=== Contraption Disassembly Info ==="));
        source.sendSystemMessage(Component.literal("§7This command analyzes and disassembles Create contraptions."));
        source.sendSystemMessage(Component.literal("§7Available subcommands:"));
        source.sendSystemMessage(Component.literal("§f- list §7- List available contraptions"));
        source.sendSystemMessage(Component.literal("§f- execute <id> §7- Disassemble contraption by ID"));
        source.sendSystemMessage(Component.literal("§f- info §7- Show this help"));
        return 1;
    }
}
