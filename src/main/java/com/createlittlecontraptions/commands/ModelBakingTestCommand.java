package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.createlittlecontraptions.rendering.cache.ContraptionModelCache;

/**
 * Command for testing and debugging the model baking system.
 */
public class ModelBakingTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("modelbaking-test")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("status")
                .executes(ModelBakingTestCommand::showStatus))
            .then(Commands.literal("clear-cache")
                .executes(ModelBakingTestCommand::clearCache))
            .then(Commands.literal("list-contraptions")
                .executes(ModelBakingTestCommand::listContraptions))
        );
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        int totalModels = ContraptionModelCache.getTotalCachedModels();
        int contraptionCount = ContraptionModelCache.getCachedContraptionCount();
        
        source.sendSystemMessage(Component.literal("§6=== Model Baking System Status ==="));
        source.sendSystemMessage(Component.literal("§aCached Models: " + totalModels));
        source.sendSystemMessage(Component.literal("§aCached Contraptions: " + contraptionCount));
        
        if (totalModels > 0) {
            source.sendSystemMessage(Component.literal("§2✓ Model baking system is active"));
        } else {
            source.sendSystemMessage(Component.literal("§eNo cached models found. Try assembling a contraption with LittleTiles blocks."));
        }
        
        return 1;
    }

    private static int clearCache(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        // Note: We can't clear all cache at once without access to UUIDs
        // This would need to be implemented in ContraptionModelCache
        source.sendSystemMessage(Component.literal("§eCache clearing is handled automatically when contraptions are disassembled."));
        source.sendSystemMessage(Component.literal("§eFor manual clearing, use /contraption-render commands."));
        
        return 1;
    }

    private static int listContraptions(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        
        source.sendSystemMessage(Component.literal("§6=== Active Contraptions ==="));
        
        int count = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof AbstractContraptionEntity) {
                count++;
                source.sendSystemMessage(Component.literal(String.format(
                    "§a%d. %s at (%.1f, %.1f, %.1f) - UUID: %s",
                    count,
                    entity.getType().getDescription().getString(),
                    entity.getX(),
                    entity.getY(), 
                    entity.getZ(),
                    entity.getUUID().toString().substring(0, 8)
                )));
            }
        }
        
        if (count == 0) {
            source.sendSystemMessage(Component.literal("§eNo contraptions found in the current level."));
        } else {
            source.sendSystemMessage(Component.literal("§aFound " + count + " contraption(s)."));
        }
        
        return count;
    }
}
