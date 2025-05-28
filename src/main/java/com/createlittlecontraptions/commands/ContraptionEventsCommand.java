package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import com.createlittlecontraptions.events.ContraptionEventHandler;

/**
 * Command to toggle contraption event logging on/off.
 * Part of Step 2 implementation for CreateLittleContraptions mod.
 */
public class ContraptionEventsCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-events")
            .requires(source -> source.hasPermission(2)) // Requires OP permission
            .then(Commands.argument("enabled", BoolArgumentType.bool())
                .executes(ContraptionEventsCommand::setEventLogging))
            .executes(ContraptionEventsCommand::getEventLoggingStatus));
    }
    
    /**
     * Set event logging enabled/disabled
     */
    private static int setEventLogging(CommandContext<CommandSourceStack> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        ContraptionEventHandler.setEventLogging(enabled);
        
        String status = enabled ? "§aENABLED" : "§cDISABLED";
        context.getSource().sendSuccess(() -> 
            Component.literal("Contraption event logging " + status), true);
        
        if (enabled) {
            context.getSource().sendSuccess(() -> 
                Component.literal("§7Events will be logged and nearby players notified during assembly/disassembly"), false);
        }
        
        return 1;
    }
    
    /**
     * Get current event logging status
     */
    private static int getEventLoggingStatus(CommandContext<CommandSourceStack> context) {
        boolean enabled = ContraptionEventHandler.isEventLoggingEnabled();
        String status = enabled ? "§aENABLED" : "§cDISABLED";
        
        context.getSource().sendSuccess(() -> 
            Component.literal("Contraption event logging is currently " + status), false);
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Use '/contraption-events <true|false>' to toggle"), false);
        
        return 1;
    }
}
