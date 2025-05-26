package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.compat.create.behaviour.LittleTilesMovementBehaviour;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(value = AllMovementBehaviours.class, remap = false)
public abstract class AllMovementBehavioursMixin {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/AllMovementBehavioursMixin");

    // Inject at the end of the static constructor to register our LittleTiles MovementBehaviour
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clc_registerLittleTilesMovementBehaviour(CallbackInfo ci) {
        LOGGER.info("Attempting to register LittleTilesMovementBehaviour...");
        try {
            // Get the LittleTiles block instance
            // Common LittleTiles block names - trying the most common first
            String[] possibleBlockNames = {
                "littletiles:tiles",           // Most common name
                "littletiles:block_tiles",     // Alternative name
                "littletiles:tile",            // Singular version
                "littletiles:little_tiles"     // Descriptive name
            };
            
            Block littleTilesBlock = null;
            String foundBlockName = null;
            
            for (String blockName : possibleBlockNames) {
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockName));
                if (block != null && block != Blocks.AIR) {
                    littleTilesBlock = block;
                    foundBlockName = blockName;
                    break;
                }
            }
            
            if (littleTilesBlock != null) {
                // Call Create's registration method
                // Based on Create 6.0.4 API, this should be the correct method call
                // Use reflection to access the private static map in AllMovementBehaviours
                try {
                    Field behaviourMapField = AllMovementBehaviours.class.getDeclaredField("BLOCK_MOVEMENT_BEHAVIOURS");
                    behaviourMapField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<Block, MovementBehaviour> behaviourMap = (Map<Block, MovementBehaviour>) behaviourMapField.get(null);                    behaviourMap.put(littleTilesBlock, new LittleTilesMovementBehaviour());
                    LOGGER.info("Successfully registered LittleTilesMovementBehaviour for: {}", littleTilesBlock.getDescriptionId());
                } catch (Exception e) {
                    LOGGER.error("Failed to register LittleTilesMovementBehaviour via reflection", e);
                }
                
                LOGGER.info("Successfully registered LittleTilesMovementBehaviour for: {} ({})", 
                    foundBlockName, littleTilesBlock.getDescriptionId());
            } else {
                LOGGER.warn("Could not find any LittleTiles block to register movement behaviour.");
                LOGGER.debug("Tried block names: {}", String.join(", ", possibleBlockNames));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register LittleTilesMovementBehaviour", e);
        }
    }
}
