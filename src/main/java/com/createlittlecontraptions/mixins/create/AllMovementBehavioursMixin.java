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

@Mixin(value = AllMovementBehaviours.class, remap = false)
public abstract class AllMovementBehavioursMixin {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/AllMovementBehavioursMixin");    // Inject at the end of registerDefaults to register our LittleTiles MovementBehaviour
    @Inject(method = "registerDefaults", at = @At("TAIL"))
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
                ResourceLocation blockId = ResourceLocation.parse(blockName);
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                if (block != null && block != Blocks.AIR) {
                    littleTilesBlock = block;
                    foundBlockName = blockName;
                    LOGGER.info("Found LittleTiles block: {} -> {}", blockName, block.getDescriptionId());
                    break;
                }
            }
            
            if (littleTilesBlock != null) {
                // Use the public API as seen in AllMovementBehaviours.registerDefaults()
                MovementBehaviour.REGISTRY.register(littleTilesBlock, new LittleTilesMovementBehaviour());
                LOGGER.info("✅ Successfully registered LittleTilesMovementBehaviour for: {} ({})", 
                    foundBlockName, littleTilesBlock.getDescriptionId());
            } else {
                LOGGER.warn("⚠️ Could not find any LittleTiles block to register movement behaviour.");
                LOGGER.info("Available blocks starting with 'littletiles:': ");
                
                // Debug: List available littletiles blocks
                BuiltInRegistries.BLOCK.entrySet().stream()
                    .filter(entry -> entry.getKey().location().getNamespace().equals("littletiles"))
                    .limit(10)
                    .forEach(entry -> LOGGER.info("  - {}", entry.getKey().location()));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register LittleTilesMovementBehaviour", e);
        }
    }
}
