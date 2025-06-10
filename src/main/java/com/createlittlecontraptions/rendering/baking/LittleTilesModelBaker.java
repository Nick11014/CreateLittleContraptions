package com.createlittlecontraptions.rendering.baking;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.*;

/**
 * LittleTilesModelBaker is responsible for creating static BakedModels from LittleTiles
 * block entities that can be used by Create's contraption rendering system.
 * 
 * Instead of trying to capture rendering directly, this approach focuses on
 * creating BakedModels that contain BakedQuads representing the geometry.
 */
public class LittleTilesModelBaker {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Attempt to create a BakedModel from a LittleTiles block entity.
     * This creates a cached BakedModel that contains BakedQuads representing
     * the LittleTiles structure's geometry.
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

            LOGGER.info("Creating baked model for LittleTiles block entity at {}", blockEntity.getBlockPos());
            
            // For now, create a simple placeholder model
            // In the future, this could be enhanced to extract actual geometry from LittleTiles
            List<BakedQuad> quads = createLittleTilesQuads(blockEntity);
            
            LOGGER.info("Created baked model with {} quads for LittleTiles block entity", quads.size());
            return Optional.of(new LittleTilesBakedModel(quads));
            
        } catch (Exception e) {
            LOGGER.error("Error during LittleTiles model baking", e);
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
     * Create BakedQuads representing the LittleTiles structure.
     * This is a placeholder implementation that could be enhanced to
     * extract actual geometry from the LittleTiles structure.
     */
    private static List<BakedQuad> createLittleTilesQuads(BlockEntity blockEntity) {
        List<BakedQuad> quads = new ArrayList<>();
        
        try {
            // Get the default texture sprite
            TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/stone"));
            
            // Create a simple cube as placeholder
            // In the future, this could access LittleTiles' internal structure
            // to create accurate geometry
            quads.addAll(createCubeQuads(sprite));
            
        } catch (Exception e) {
            LOGGER.warn("Error creating LittleTiles quads: {}", e.getMessage());
        }
        
        return quads;
    }
    
    /**
     * Create a simple cube made of BakedQuads as a placeholder.
     */
    private static List<BakedQuad> createCubeQuads(TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        
        // Create quads for all 6 faces of a cube
        for (Direction direction : Direction.values()) {
            BakedQuad quad = createFaceQuad(direction, sprite);
            if (quad != null) {
                quads.add(quad);
            }
        }
        
        return quads;
    }
    
    /**
     * Create a BakedQuad for a single face of a cube.
     */
    private static BakedQuad createFaceQuad(Direction face, TextureAtlasSprite sprite) {
        try {
            VertexFormat format = DefaultVertexFormat.BLOCK;
            int vertexSize = format.getVertexSize() / 4; // Size in ints
            int[] data = new int[vertexSize * 4]; // 4 vertices
            
            // Define vertices for this face
            float[][] vertices = getFaceVertices(face);
            
            // Pack each vertex
            for (int i = 0; i < 4; i++) {
                packVertex(data, i * vertexSize, vertices[i], sprite);
            }
            
            return new BakedQuad(data, -1, face, sprite, false);
            
        } catch (Exception e) {
            LOGGER.warn("Error creating face quad for {}: {}", face, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get the vertices for a face of a unit cube.
     */
    private static float[][] getFaceVertices(Direction face) {
        switch (face) {
            case DOWN:
                return new float[][] {
                    {0, 0, 0, 0, 1}, {1, 0, 0, 1, 1}, {1, 0, 1, 1, 0}, {0, 0, 1, 0, 0}
                };
            case UP:
                return new float[][] {
                    {0, 1, 1, 0, 0}, {1, 1, 1, 1, 0}, {1, 1, 0, 1, 1}, {0, 1, 0, 0, 1}
                };
            case NORTH:
                return new float[][] {
                    {1, 0, 0, 0, 1}, {0, 0, 0, 1, 1}, {0, 1, 0, 1, 0}, {1, 1, 0, 0, 0}
                };
            case SOUTH:
                return new float[][] {
                    {0, 0, 1, 0, 1}, {1, 0, 1, 1, 1}, {1, 1, 1, 1, 0}, {0, 1, 1, 0, 0}
                };
            case WEST:
                return new float[][] {
                    {0, 0, 0, 0, 1}, {0, 0, 1, 1, 1}, {0, 1, 1, 1, 0}, {0, 1, 0, 0, 0}
                };
            case EAST:
                return new float[][] {
                    {1, 0, 1, 0, 1}, {1, 0, 0, 1, 1}, {1, 1, 0, 1, 0}, {1, 1, 1, 0, 0}
                };
            default:
                return new float[][] {{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}};
        }
    }
    
    /**
     * Pack vertex data into the int array following the BLOCK vertex format.
     */
    private static void packVertex(int[] data, int offset, float[] vertex, TextureAtlasSprite sprite) {
        int index = offset;
        
        // Position (x, y, z) - 3 floats
        data[index++] = Float.floatToRawIntBits(vertex[0]);
        data[index++] = Float.floatToRawIntBits(vertex[1]);
        data[index++] = Float.floatToRawIntBits(vertex[2]);
        
        // Color (RGBA) - packed into 1 int (white)
        data[index++] = 0xFFFFFFFF;
        
        // Texture coordinates (u, v) - 2 floats
        data[index++] = Float.floatToRawIntBits(sprite.getU(vertex[3] * 16));
        data[index++] = Float.floatToRawIntBits(sprite.getV(vertex[4] * 16));
        
        // Light map coordinates - 1 int (full bright)
        data[index++] = 0x00F000F0;
        
        // Normal - 1 int (pointing up)
        data[index] = 0x7F7F0000;
    }
    
    /**
     * Simple BakedModel implementation that holds our LittleTiles quads.
     */
    private static class LittleTilesBakedModel implements BakedModel {
        
        private final List<BakedQuad> quads;
        private final Map<Direction, List<BakedQuad>> faceQuads;
        private final TextureAtlasSprite particleSprite;
        
        public LittleTilesBakedModel(List<BakedQuad> quads) {
            this.quads = new ArrayList<>(quads);
            this.faceQuads = new EnumMap<>(Direction.class);
            
            // Organize quads by face for more efficient rendering
            for (Direction direction : Direction.values()) {
                faceQuads.put(direction, new ArrayList<>());
            }
            
            // Separate quads by their faces
            for (BakedQuad quad : quads) {
                Direction face = quad.getDirection();
                if (face != null) {
                    faceQuads.get(face).add(quad);
                }
            }
            
            // Get particle sprite
            this.particleSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/stone"));
        }
        
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            if (side == null) {
                // Return all quads for null side (general quads)
                return quads;
            } else {
                // Return quads for specific face
                return faceQuads.getOrDefault(side, Collections.emptyList());
            }
        }
        
        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }
        
        @Override
        public boolean isGui3d() {
            return true;
        }
        
        @Override
        public boolean usesBlockLight() {
            return true;
        }
        
        @Override
        public boolean isCustomRenderer() {
            return false;
        }
        
        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particleSprite;
        }
        
        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
        
        // NeoForge-specific methods
        @Override
        public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
            return modelData;
        }
        
        @Override
        public TextureAtlasSprite getParticleIcon(ModelData data) {
            return getParticleIcon();
        }
        
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
            return getQuads(state, side, rand);
        }
          @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
            return ChunkRenderTypeSet.of(RenderType.solid());
        }
    }
}
