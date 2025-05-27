package com.createlittlecontraptions.compat.create;

import com.createlittlecontraptions.compat.littletiles.LittleTilesNBTHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.creative.littletiles.common.block.little.tile.parent.BlockParentCollection;

/**
 * MovementBehaviour for LittleTiles blocks in Create contraptions.
 * Uses direct NBT analysis to bypass VirtualRenderWorld limitations.
 * This will be registered with Create using reflection to avoid direct dependencies.
 */
public class LittleTilesMovementBehaviourNew {
    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions/LittleTilesMovementBehaviour");

    /**
     * Called by Create via reflection during contraption rendering.
     * Method signature must match: renderInContraption(MovementContext, VirtualRenderWorld, ContraptionMatrices, MultiBufferSource)
     */
    @OnlyIn(Dist.CLIENT)
    public void renderInContraption(Object context, Object renderWorld, Object matrices, MultiBufferSource buffer) {
        
        try {
            // Extract data from MovementContext using reflection
            CompoundTag blockEntityNBT = (CompoundTag) context.getClass().getField("blockEntityData").get(context);
            BlockPos localPos = (BlockPos) context.getClass().getField("localPos").get(context);
            
            if (!LittleTilesNBTHelper.hasValidLittleTilesData(blockEntityNBT)) {
                LOGGER.debug("No valid LittleTiles data for position {}", localPos);
                return;
            }

            LOGGER.debug("Attempting direct NBT rendering for LittleTiles at {}", localPos);

            // Load tiles collection directly from NBT
            Level level = (Level) renderWorld.getClass().getMethod("getLevel").invoke(renderWorld);
            BlockParentCollection tiles = LittleTilesNBTHelper.loadTilesFromNBT(blockEntityNBT, level);
            if (tiles == null) {
                LOGGER.warn("Failed to load tiles from NBT for {}", localPos);
                return;
            }

            // Get PoseStack from ContraptionMatrices using reflection
            PoseStack poseStack = (PoseStack) matrices.getClass().getMethod("getModelViewProjection").invoke(matrices);
            poseStack.pushPose();

            // Get lighting (using VirtualRenderWorld is safe for this)
            int combinedLight = LevelRenderer.getLightColor(level, localPos);

            // Render the tiles collection directly
            LittleTilesNBTHelper.renderTilesCollection(tiles, poseStack, buffer, combinedLight, 0.0f);

            poseStack.popPose();
            
            LOGGER.debug("Successfully rendered LittleTiles at {} using direct NBT method", localPos);

        } catch (Exception e) {
            LOGGER.error("Failed to render LittleTiles in contraption: ", e);
        }
    }

    /**
     * Called by Create via reflection for collision detection.
     * Method signature must match: getCollisionShapeInContraption(MovementContext, CollisionContext)
     */
    public VoxelShape getCollisionShapeInContraption(Object context, CollisionContext collisionContext) {
        try {
            // Extract NBT data using reflection
            CompoundTag blockEntityNBT = (CompoundTag) context.getClass().getField("blockEntityData").get(context);
            
            if (!LittleTilesNBTHelper.hasValidLittleTilesData(blockEntityNBT)) {
                return Shapes.empty();
            }

            // Load tiles collection from NBT for collision
            BlockParentCollection tiles = LittleTilesNBTHelper.loadTilesFromNBT(blockEntityNBT, null);
            if (tiles == null) {
                return Shapes.empty();
            }

            return LittleTilesNBTHelper.getCollisionShape(tiles, collisionContext);

        } catch (Exception e) {
            LOGGER.error("Failed to get collision shape for LittleTiles: ", e);
            return Shapes.empty();
        }
    }
}
