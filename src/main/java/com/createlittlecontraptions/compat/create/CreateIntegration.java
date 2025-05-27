package com.createlittlecontraptions.compat.create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.creative.littletiles.LittleTilesRegistry;

import java.lang.reflect.Method;

/**
 * Handles registration of LittleTiles MovementBehaviour with Create using reflection.
 * This avoids compile-time dependencies on Create classes.
 */
public class CreateIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions/CreateIntegration");
    
    private static boolean registrationAttempted = false;
    private static boolean registrationSuccessful = false;

    /**
     * Attempts to register our custom MovementBehaviour for LittleTiles blocks with Create.
     * Uses reflection to avoid compile-time dependencies.
     */
    public static void registerLittleTilesMovementBehaviour() {
        if (registrationAttempted) {
            return;
        }
        registrationAttempted = true;

        try {
            LOGGER.info("Attempting to register LittleTiles MovementBehaviour with Create...");

            // Try to find Create's AllMovementBehaviours class
            Class<?> allMovementBehavioursClass = Class.forName("com.simibubi.create.AllMovementBehaviours");
            
            // Find the registration method (usually something like "register" or "of")
            // We'll need to check what methods are available
            Method[] methods = allMovementBehavioursClass.getDeclaredMethods();
            LOGGER.debug("Available methods in AllMovementBehaviours:");
            for (Method method : methods) {
                LOGGER.debug("  - {}", method.getName());
            }

            // Try to find a method that takes a Block and returns a MovementBehaviour
            Method registerMethod = null;
            for (Method method : methods) {
                if (method.getName().equals("of") || method.getName().equals("register")) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 2) {
                        // Likely: of(Block, MovementBehaviour) or similar
                        registerMethod = method;
                        break;
                    }
                }
            }

            if (registerMethod != null) {
                // Create an instance of our MovementBehaviour
                LittleTilesMovementBehaviourNew behaviour = new LittleTilesMovementBehaviourNew();
                
                // Register it for all LittleTiles block variants
                registerMethod.invoke(null, LittleTilesRegistry.BLOCK_TILES.value(), behaviour);
                registerMethod.invoke(null, LittleTilesRegistry.BLOCK_TILES_TICKING.value(), behaviour);
                registerMethod.invoke(null, LittleTilesRegistry.BLOCK_TILES_RENDERED.value(), behaviour);
                registerMethod.invoke(null, LittleTilesRegistry.BLOCK_TILES_TICKING_RENDERED.value(), behaviour);
                
                registrationSuccessful = true;
                LOGGER.info("Successfully registered LittleTiles MovementBehaviour with Create for all block variants!");
                
            } else {
                LOGGER.warn("Could not find suitable registration method in AllMovementBehaviours");
            }

        } catch (ClassNotFoundException e) {
            LOGGER.warn("Create mod not found - LittleTiles MovementBehaviour not registered");
        } catch (Exception e) {
            LOGGER.error("Failed to register LittleTiles MovementBehaviour with Create: ", e);
        }
    }

    /**
     * Checks if Create integration is available and working.
     */
    public static boolean isCreateIntegrationWorking() {
        return registrationSuccessful;
    }

    /**
     * Logs debug information about Create integration status.
     */
    public static void logIntegrationStatus() {
        if (!registrationAttempted) {
            LOGGER.info("Create integration not yet attempted");
        } else if (registrationSuccessful) {
            LOGGER.info("Create integration successful - LittleTiles MovementBehaviour registered");
        } else {
            LOGGER.warn("Create integration failed - LittleTiles blocks may not render in contraptions");
        }
    }
}
