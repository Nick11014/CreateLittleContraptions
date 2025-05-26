package com.createlittlecontraptions.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Debug commands for testing CreateLittleContraptions functionality
 */
@EventBusSubscriber(modid = "createlittlecontraptions")
public class DebugCommands {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("contraption-debug")
            .requires(source -> source.hasPermission(2))
            .executes(DebugCommands::executeDebug));
    }

    private static int executeDebug(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            // Force log some debug information
            LOGGER.info("===== CONTRAPTION DEBUG COMMAND EXECUTED =====");
            LOGGER.info("Player: {}", source.getDisplayName().getString());
            LOGGER.info("Position: {}", source.getPosition());
            
            // Test our integration status
            source.sendSuccess(() -> Component.literal("§a[CreateLittleContraptions] Debug command executed!"), false);
            source.sendSuccess(() -> Component.literal("§7Check the logs for detailed integration status."), false);
            
            // Force trigger some of our systems
            testMixinSystem();
            
            return 1;
            
        } catch (Exception e) {
            LOGGER.error("Error executing contraption debug command: {}", e.getMessage());
            context.getSource().sendFailure(Component.literal("§c[CreateLittleContraptions] Debug command failed: " + e.getMessage()));
            return 0;
        }
    }
    
    private static void testMixinSystem() {
        LOGGER.info("===== TESTING MIXIN SYSTEM =====");
        LOGGER.info("This message tests if our debug system is working");
        System.out.println("[CreateLittleContraptions] DEBUG: Manual system test triggered");
        LOGGER.info("=================================");
    }
}
