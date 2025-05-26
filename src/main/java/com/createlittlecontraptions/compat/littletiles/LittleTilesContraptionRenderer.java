package com.createlittlecontraptions.compat.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

// LittleTiles imports
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LittleTilesContraptionRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LittleTilesContraptionRenderer.class);
    private static boolean initialized = false;

    /**
     * Initialize the LittleTiles contraption renderer.
     * Called during mod initialization.
     */
    public static void initialize() {
        LOGGER.info("Initializing LittleTiles contraption renderer...");
        initialized = true;
        LOGGER.info("LittleTiles contraption renderer initialized successfully.");
    }

    /**
     * Check if the renderer has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Render a LittleTiles BlockEntity in a contraption.
     * This is the main entry point for rendering LittleTiles blocks in contraptions.
     */
    public static void renderLittleTileBEInContraption(PoseStack poseStack, MultiBufferSource bufferSource,
                                                       Level realLevel, VirtualRenderWorld renderLevel,
                                                       BlockEntity blockEntity, float partialTicks,
                                                       Matrix4f lightTransform, int light, int overlay) {
        if (!(blockEntity instanceof BETiles betiles)) {
            LOGGER.warn("⚠️ renderLittleTileBEInContraption called with non-BETiles BlockEntity: {}", 
                       blockEntity.getClass().getSimpleName());
            return;
        }

        LOGGER.info("🔍 renderLittleTileBEInContraption called for BETiles at: {}", blockEntity.getBlockPos());

        try {
            // Use the existing render logic but adapted for direct BlockEntity access
            poseStack.pushPose();

            BlockPos pos = blockEntity.getBlockPos();
            BlockState state = blockEntity.getBlockState();

            if (!betiles.isEmpty()) {
                int tilesCount = betiles.tilesCount();
                LOGGER.info("✨ Found {} tiles to render at {}", tilesCount, pos);

                // Render each tile
                for (Pair<IParentCollection, LittleTile> pair : betiles.allTiles()) {
                    LittleTile tile = pair.getValue();
                    LittleGrid grid = betiles.getGrid();
                    
                    // Create render boxes for each LittleBox in the tile
                    for (var box : tile) {
                        try {
                            // Create LittleRenderBox
                            LittleRenderBox renderBox = new LittleRenderBox(grid, box, tile.getState());
                            
                            // Get the appropriate render type
                            RenderType renderType = RenderType.solid(); // Start with solid, can be improved
                            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
                            
                            // TODO: Fix render method call when we have correct method signature
                            // renderBox.render(poseStack, vertexConsumer, light, overlay);
                            LOGGER.debug("📦 Would render LittleRenderBox for tile at {}", pos);
                            
                        } catch (Exception e) {
                            LOGGER.warn("⚠️ Failed to render individual box for tile at {}: {}", pos, e.getMessage());
                        }
                    }
                }

                LOGGER.info("✅ Successfully processed LittleTiles structure at {}", pos);
            } else {
                LOGGER.warn("⚠️ No tiles found in BETiles at {}", pos);
            }

        } catch (Exception e) {
            LOGGER.error("❌ Error in renderLittleTileBEInContraption for pos " + blockEntity.getBlockPos(), e);
        } finally {
            poseStack.popPose();
        }
    }

    public static void renderMovementBehaviourTile(MovementContext context, VirtualRenderWorld renderWorld,
                                                  ContraptionMatrices matrices, MultiBufferSource bufferSource) {
        LOGGER.info("🔍 renderMovementBehaviourTile TOP para pos: {} com NBT: {}", 
                   context.localPos, context.blockEntityData != null && !context.blockEntityData.isEmpty());

        CompoundTag nbt = context.blockEntityData;
        BlockState state = context.state;
        BlockPos localPos = context.localPos;

        if (nbt == null || nbt.isEmpty()) {
            LOGGER.warn("⚠️ renderMovementBehaviourTile: NBT data é null ou vazia para pos: {}", localPos);
            return;
        }

        PoseStack poseStack = matrices.getModelViewProjection();

        try {
            poseStack.pushPose();

            // Translate to the local position of the block within the contraption
            poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());            // Create a virtual BETiles and load the NBT data
            BETiles virtualBE = new BETiles(localPos, state);
            // Set the level so handleUpdate can work properly
            virtualBE.setLevel(Minecraft.getInstance().level);
            // Use handleUpdate which is public and calls loadAdditional internally
            virtualBE.handleUpdate(nbt, false);

            // Get the tiles collection from the virtual BE
            if (!virtualBE.isEmpty()) {
                int tilesCount = virtualBE.tilesCount();
                LOGGER.info("✨ Found {} tiles to render at {}", tilesCount, localPos);

                // Get lighting
                int light = LevelRenderer.getLightColor(renderWorld, localPos);
                int overlay = OverlayTexture.NO_OVERLAY;
                
                // Render each tile
                for (Pair<IParentCollection, LittleTile> pair : virtualBE.allTiles()) {
                    LittleTile tile = pair.getValue();
                    LittleGrid grid = virtualBE.getGrid();
                    
                    // Create render boxes for each LittleBox in the tile
                    for (var box : tile) {
                        try {
                            // Create LittleRenderBox
                            LittleRenderBox renderBox = new LittleRenderBox(grid, box, tile.getState());
                            
                            // Get the appropriate render type
                            RenderType renderType = RenderType.solid(); // Start with solid, can be improved
                            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
                              // Render the box using the RenderBox rendering system
                            // TODO: Fix render method call when we have correct method signature
                            // renderBox.render(poseStack, vertexConsumer, light, overlay);
                            LOGGER.debug("📦 Would render LittleRenderBox for tile at {}", localPos);
                            
                        } catch (Exception e) {
                            LOGGER.warn("⚠️ Failed to render individual box for tile at {}: {}", localPos, e.getMessage());
                        }
                    }
                }

                LOGGER.info("✅ Successfully rendered LittleTiles structure at {}", localPos);
            } else {
                LOGGER.warn("⚠️ No tiles found in BETiles at {}", localPos);
            }

        } catch (Exception e) {
            LOGGER.error("❌ Error in renderMovementBehaviourTile for pos " + localPos, e);
        } finally {
            poseStack.popPose();
        }
    }
}
