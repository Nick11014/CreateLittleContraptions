package com.createlittlecontraptions.debug;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Debug commands for testing CreateLittleContraptions functionality
 * Note: The contraption-debug command is now handled by ContraptionDebugCommand.java
 */
public class DebugCommands {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Removed contraption-debug command - now handled by ContraptionDebugCommand.java
    // @SubscribeEvent
    // public static void onRegisterCommands(RegisterCommandsEvent event) {
    //     CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();    //
    //     dispatcher.register(Commands.literal("contraption-debug")
    //         .requires(source -> source.hasPermission(2))
    //         .executes(DebugCommands::executeDebug));
    // }

    // Old executeDebug method removed - now handled by ContraptionDebugCommand.java
    
    public static void testMixinSystem() {
        LOGGER.info("===== TESTING MIXIN SYSTEM =====");
        LOGGER.info("This message tests if our debug system is working");
        System.out.println("[CreateLittleContraptions] DEBUG: Manual system test triggered");
        LOGGER.info("=================================");
    }
}
