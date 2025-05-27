package com.createlittlecontraptions.compat.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.parent.BlockParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;

/**
 * Helper class for loading and processing LittleTiles NBT data directly,
 * bypassing the normal BlockEntity lifecycle that causes issues in VirtualRenderWorld.
 */
public class LittleTilesNBTHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions/LittleTilesNBTHelper");

    /**
     * Attempts to extract and recreate a BlockParentCollection from BETiles NBT data
     * without requiring a full Level context.
     */
    public static BlockParentCollection loadTilesFromNBT(CompoundTag blockEntityNBT, Level mockLevel) {
        try {
            if (blockEntityNBT == null || blockEntityNBT.isEmpty()) {
                LOGGER.warn("Cannot load tiles from empty NBT");
                return null;
            }

            // Create a temporary BETiles for loading only
            // We'll need to create a mock/minimal level context
            BETiles tempBE = new BETiles(null, null, null);
            
            // Get the content tag that contains the tiles data
            CompoundTag contentNBT = blockEntityNBT.getCompound("content");
            if (contentNBT.isEmpty()) {
                LOGGER.warn("Content NBT is empty in BETiles data");
                return null;
            }

            // Create a new BlockParentCollection for client-side rendering
            BlockParentCollection tiles = new BlockParentCollection(tempBE, true);
            
            // Load the tiles data directly from NBT
            HolderLookup.Provider registryAccess = mockLevel != null ? mockLevel.registryAccess() : null;
            if (registryAccess == null) {
                LOGGER.error("Cannot load tiles without registry access");
                return null;
            }
            
            tiles.load(contentNBT, registryAccess);
            
            LOGGER.debug("Successfully loaded {} tiles from NBT", tiles.totalSize());
            return tiles;
            
        } catch (Exception e) {
            LOGGER.error("Failed to load tiles from NBT: ", e);
            return null;
        }
    }

    /**
     * Renders loaded LittleTiles structures directly using their renderTick method
     */
    public static void renderTilesCollection(BlockParentCollection tiles, PoseStack poseStack, 
                                           MultiBufferSource bufferSource, int combinedLight, 
                                           float partialTicks) {
        if (tiles == null) {
            return;
        }

        try {
            // Render all loaded structures that have TICK_RENDERING attribute
            for (LittleStructure structure : tiles.loadedStructures(LittleStructureAttribute.TICK_RENDERING)) {
                // Use the structure's renderTick method (same as BETilesRenderer)
                structure.renderTick(poseStack, bufferSource, null, partialTicks);
            }
            
            LOGGER.debug("Rendered {} tick-rendering structures", 
                tiles.loadedStructures(LittleStructureAttribute.TICK_RENDERING).spliterator().estimateSize());
                
        } catch (Exception e) {
            LOGGER.error("Failed to render tiles collection: ", e);
        }
    }

    /**
     * Gets collision shape from loaded tiles collection
     */
    public static VoxelShape getCollisionShape(BlockParentCollection tiles, CollisionContext context) {
        if (tiles == null) {
            return Shapes.empty();
        }

        try {
            // This is a simplified approach - in a full implementation, we'd need to
            // convert LittleBox collections to VoxelShape
            // For now, return empty shape to avoid crashes
            LOGGER.debug("Getting collision shape for {} total tiles", tiles.totalSize());
            return Shapes.empty();
            
        } catch (Exception e) {
            LOGGER.error("Failed to get collision shape: ", e);
            return Shapes.empty();
        }
    }

    /**
     * Checks if the NBT contains valid LittleTiles data
     */
    public static boolean hasValidLittleTilesData(CompoundTag blockEntityNBT) {
        if (blockEntityNBT == null || blockEntityNBT.isEmpty()) {
            return false;
        }
        
        // Check for content tag that contains the tiles
        CompoundTag contentNBT = blockEntityNBT.getCompound("content");
        if (contentNBT.isEmpty()) {
            return false;
        }
        
        // Check for tiles data in content
        CompoundTag tilesNBT = contentNBT.getCompound("tiles");
        return !tilesNBT.isEmpty();
    }
}
