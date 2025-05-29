package com.createlittlecontraptions.compat.create;

import com.createlittlecontraptions.compat.littletiles.LittleTilesMovementBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Handles registration of custom MovementBehaviours for Create integration.
 * This class is responsible for registering our LittleTilesMovementBehaviour
 * with the Create mod's movement system.
 */
public class CreateMovementRegistry {
    
    /**
     * Registers custom MovementBehaviours during FML common setup.
     * This should be called from the main mod's FMLCommonSetupEvent handler.
     * 
     * @param event The FML common setup event
     */
    public static void registerMovementBehaviours(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerLittleTilesMovementBehaviour();
        });
    }
    
    /**
     * Registers the LittleTilesMovementBehaviour for littletiles:tiles blocks.
     */    
    private static void registerLittleTilesMovementBehaviour() {
        try {            
            // Find the LittleTiles block using BuiltInRegistries
            Block littleTilesBlock = BuiltInRegistries.BLOCK.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("littletiles", "tiles")
            );
              
            if (littleTilesBlock != null && !littleTilesBlock.equals(net.minecraft.world.level.block.Blocks.AIR)) {
                // TODO: Implement movement behaviour registration once Create APIs are available
                // For now, just log that we found the block
                com.createlittlecontraptions.CreateLittleContraptions.LOGGER.info(
                    "Found LittleTiles block for movement behaviour: " + littleTilesBlock +
                    " (Registration deferred until Create APIs are accessible)"
                );
                
                // Store reference for later use
                registerMovementBehaviourReflection(littleTilesBlock);
                
            } else {
                com.createlittlecontraptions.CreateLittleContraptions.LOGGER.warn(
                    "Failed to find littletiles:tiles block for MovementBehaviour registration"
                );
            }
            
        } catch (Exception e) {
            com.createlittlecontraptions.CreateLittleContraptions.LOGGER.error(
                "Failed to register LittleTilesMovementBehaviour: " + e.getMessage(), e
            );
        }
    }
    
    /**
     * Attempts to register movement behaviour using reflection as fallback.
     * This method will try to access Create's AllMovementBehaviours via reflection.
     */
    private static void registerMovementBehaviourReflection(Block littleTilesBlock) {
        try {
            // Try to find Create's AllMovementBehaviours class
            Class<?> allMovementBehavioursClass = Class.forName("com.simibubi.create.AllMovementBehaviours");
            
            // Try to find the registerBehaviour method
            java.lang.reflect.Method registerMethod = allMovementBehavioursClass.getDeclaredMethod(
                "registerBehaviour", Block.class, Object.class
            );
            
            // Create instance of our movement behaviour
            LittleTilesMovementBehaviour behaviour = new LittleTilesMovementBehaviour();
            
            // Invoke the registration method
            registerMethod.invoke(null, littleTilesBlock, behaviour);
            
            com.createlittlecontraptions.CreateLittleContraptions.LOGGER.info(
                "Successfully registered LittleTilesMovementBehaviour via reflection for block: " + littleTilesBlock
            );
            
        } catch (ClassNotFoundException e) {
            com.createlittlecontraptions.CreateLittleContraptions.LOGGER.warn(
                "Create mod's AllMovementBehaviours class not found - registration skipped"
            );
        } catch (NoSuchMethodException e) {
            com.createlittlecontraptions.CreateLittleContraptions.LOGGER.warn(
                "Create mod's registerBehaviour method not found - registration skipped"
            );
        } catch (Exception e) {
            com.createlittlecontraptions.CreateLittleContraptions.LOGGER.error(
                "Failed to register LittleTilesMovementBehaviour via reflection: " + e.getMessage(), e
            );
        }
    }
    
    /**
     * Checks if the Create mod's movement system is available.
     * 
     * @return true if Create's AllMovementBehaviours is available
     */
    public static boolean isCreateMovementSystemAvailable() {
        try {
            Class.forName("com.simibubi.create.AllMovementBehaviours");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
