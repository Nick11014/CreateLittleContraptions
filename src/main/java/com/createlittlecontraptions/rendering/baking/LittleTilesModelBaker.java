package com.createlittlecontraptions.rendering.baking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.*;

/**
 * Utility class for "baking" LittleTiles block entities into static BakedModels.
 * This allows LittleTiles blocks to be rendered in Create contraptions.
 */
public class LittleTilesModelBaker {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Attempt to bake a BakedModel from a LittleTiles block entity.
     * 
     * @param blockEntity The block entity to bake (should be a BETiles)
     * @return Optional containing the baked model, or empty if baking failed
     */
    public static Optional<BakedModel> bake(BlockEntity blockEntity) {
        if (blockEntity == null) {
            LOGGER.debug("BlockEntity is null, cannot bake model");
            return Optional.empty();
        }
        
        try {
            // Check if this is a LittleTiles block entity
            if (!isLittleTilesBlockEntity(blockEntity)) {
                LOGGER.debug("BlockEntity {} is not a LittleTiles entity", blockEntity.getClass().getSimpleName());
                return Optional.empty();
            }
            
            // Get the renderer for this block entity
            var renderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            var renderer = renderDispatcher.getRenderer(blockEntity);
            
            if (renderer == null) {
                LOGGER.debug("No renderer found for LittleTiles block entity");
                return Optional.empty();
            }
            
            // Check if this is the expected BETilesRenderer
            if (!isBETilesRenderer(renderer)) {
                LOGGER.debug("Renderer {} is not a BETilesRenderer", renderer.getClass().getSimpleName());
                return Optional.empty();
            }
            
            LOGGER.debug("Attempting to bake model for LittleTiles block entity using renderer {}", 
                renderer.getClass().getSimpleName());
            
            // For now, create a placeholder model
            // In a full implementation, we would capture geometry from the renderer
            List<BakedQuad> quads = createPlaceholderQuads();
            
            LOGGER.info("Created placeholder baked model with {} quads for LittleTiles block entity", quads.size());
            
            // Create and return the baked model
            return Optional.of(new SimpleBakedModel(quads));
            
        } catch (Exception e) {
            LOGGER.error("Error during model baking: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Check if the given block entity is a LittleTiles block entity.
     */
    private static boolean isLittleTilesBlockEntity(BlockEntity blockEntity) {
        String className = blockEntity.getClass().getName();
        return className.contains("littletiles") && className.contains("BETiles");
    }
    
    /**
     * Check if the given renderer is a BETilesRenderer.
     */
    private static boolean isBETilesRenderer(Object renderer) {
        String className = renderer.getClass().getName();
        return className.contains("littletiles") && className.contains("BETilesRenderer");
    }
    
    /**
     * Create placeholder quads for testing.
     * In a full implementation, these would be captured from the actual renderer.
     */
    private static List<BakedQuad> createPlaceholderQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        
        // Create a simple placeholder quad (representing a basic cube)
        try {
            TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/stone"));
            
            // Create vertex data array for a simple quad
            int[] vertexData = new int[32]; // 8 ints per vertex * 4 vertices
            // Simplified vertex data - in practice this would be properly packed
            
            BakedQuad quad = new BakedQuad(vertexData, -1, Direction.UP, sprite, false);
            quads.add(quad);
            
        } catch (Exception e) {
            LOGGER.debug("Failed to create placeholder quad: {}", e.getMessage());
        }
        
        return quads;
    }
    
    /**
     * Simple BakedModel implementation that holds our captured quads.
     */
    private static class SimpleBakedModel implements BakedModel {
        
        private final List<BakedQuad> quads;
        private final Map<Direction, List<BakedQuad>> faceQuads;
        
        public SimpleBakedModel(List<BakedQuad> quads) {
            this.quads = new ArrayList<>(quads);
            this.faceQuads = new EnumMap<>(Direction.class);
            
            // Organize quads by face for more efficient rendering
            for (Direction direction : Direction.values()) {
                faceQuads.put(direction, new ArrayList<>());
            }
            
            // For now, put all quads in general list and UP face
            faceQuads.get(Direction.UP).addAll(quads);
        }
        
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            if (side == null) {
                return quads; // Return all quads for general rendering
            } else {
                return faceQuads.getOrDefault(side, Collections.emptyList());
            }
        }
        
        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }
        
        @Override
        public boolean isGui3d() {
            return false; // Not used in GUI
        }
        
        @Override
        public boolean usesBlockLight() {
            return true;
        }
        
        @Override
        public boolean isCustomRenderer() {
            return false; // We're providing a standard model
        }
          @Override
        public TextureAtlasSprite getParticleIcon() {
            // Return a default texture for particles
            return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/stone"));
        }
        
        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
        
        // NeoForge-specific methods
        @Override
        public net.neoforged.neoforge.client.model.data.ModelData getModelData(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos, BlockState state, net.neoforged.neoforge.client.model.data.ModelData modelData) {
            return modelData;
        }
        
        @Override
        public TextureAtlasSprite getParticleIcon(net.neoforged.neoforge.client.model.data.ModelData data) {
            return getParticleIcon();
        }
        
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, net.neoforged.neoforge.client.model.data.ModelData data, RenderType renderType) {
            return getQuads(state, side, rand);
        }
    }
}
