package com.createlittlecontraptions.rendering.baking;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Baker utility for converting LittleTiles BlockEntityRenderer output into static BakedModels.
 * This is the core of the Model Baking solution for CreateLittleContraptions compatibility.
 */
public class LittleTilesModelBaker {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Bake a BakedModel from a LittleTiles BlockEntity by capturing its renderer output.
     * 
     * @param blockEntity The BlockEntity to bake (should be a BETiles instance)
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
            BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
                
            if (renderer == null) {
                LOGGER.debug("No renderer found for BlockEntity {}", blockEntity.getClass().getSimpleName());
                return Optional.empty();
            }
            
            // Check if this is a BETilesRenderer
            if (!isLittleTilesRenderer(renderer)) {
                LOGGER.debug("Renderer {} is not a LittleTiles renderer", renderer.getClass().getSimpleName());
                return Optional.empty();
            }
            
            LOGGER.debug("Attempting to bake model for LittleTiles BlockEntity using renderer {}", 
                renderer.getClass().getSimpleName());
            
            // Create a custom VertexConsumer to capture geometry data
            CaptureVertexConsumer captureConsumer = new CaptureVertexConsumer();
            
            // Create a new PoseStack for rendering
            PoseStack poseStack = new PoseStack();
            
            // Standard lighting values for block entities
            int light = 15728880; // Full light (skylight=15, blocklight=15)
            int overlay = 655360; // No overlay
            
            // Render the block entity to capture its geometry
            try {
                renderer.render(blockEntity, 0.0f, poseStack, 
                    (renderType) -> captureConsumer, light, overlay);
                
                LOGGER.debug("Rendered BlockEntity, captured {} vertices", captureConsumer.getVertexCount());
                
            } catch (Exception e) {
                LOGGER.warn("Failed to render BlockEntity for baking: {}", e.getMessage());
                return Optional.empty();
            }
            
            // Convert captured vertices to BakedQuads
            List<BakedQuad> quads = captureConsumer.generateQuads();
            if (quads.isEmpty()) {
                LOGGER.debug("No quads generated from captured vertices");
                return Optional.empty();
            }
            
            LOGGER.debug("Generated {} quads from captured geometry", quads.size());
            
            // Create and return the baked model
            BakedModel bakedModel = new SimpleBakedModel(quads);
            return Optional.of(bakedModel);
            
        } catch (Exception e) {
            LOGGER.warn("Exception during model baking for {}: {}", 
                blockEntity.getClass().getSimpleName(), e.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Baking exception stack trace:", e);
            }
            return Optional.empty();
        }
    }
    
    /**
     * Create a simple placeholder model for LittleTiles blocks.
     * This is used when actual model baking fails or as a fallback.
     */
    public static Optional<BakedModel> createPlaceholderModel() {
        try {
            // Create a simple placeholder model with an empty quad list
            // In a real implementation, this might be a simple cube or other basic shape
            List<BakedQuad> placeholderQuads = new ArrayList<>();
            
            // For now, use an empty model - the presence of the model in the cache
            // is more important than its actual geometry for this initial implementation
            BakedModel placeholderModel = new SimpleBakedModel(placeholderQuads);
            
            LOGGER.debug("Created placeholder model for LittleTiles block");
            return Optional.of(placeholderModel);
            
        } catch (Exception e) {
            LOGGER.warn("Failed to create placeholder model: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Check if a BlockEntity is a LittleTiles block entity.
     */
    private static boolean isLittleTilesBlockEntity(BlockEntity blockEntity) {
        String className = blockEntity.getClass().getName();
        // Check for LittleTiles BETiles class
        return className.contains("team.creative.littletiles") && 
               className.contains("BETiles");
    }
    
    /**
     * Check if a BlockEntityRenderer is a LittleTiles renderer.
     */
    private static boolean isLittleTilesRenderer(BlockEntityRenderer<?> renderer) {
        String className = renderer.getClass().getName();
        // Check for LittleTiles BETilesRenderer class
        return className.contains("team.creative.littletiles") && 
               className.contains("BETilesRenderer");
    }
      /**
     * Custom VertexConsumer that captures vertex data instead of sending it to GPU.
     */
    private static class CaptureVertexConsumer implements VertexConsumer {
        
        private final List<VertexData> vertices = new ArrayList<>();
        private VertexData currentVertex = new VertexData();
        
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            currentVertex.x = x;
            currentVertex.y = y;
            currentVertex.z = z;
            return this;
        }
        
        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            currentVertex.r = red;
            currentVertex.g = green;
            currentVertex.b = blue;
            currentVertex.a = alpha;
            return this;
        }
        
        @Override
        public VertexConsumer setUv(float u, float v) {
            currentVertex.u = u;
            currentVertex.v = v;
            return this;
        }
        
        @Override
        public VertexConsumer setUv1(int u, int v) {
            // Store overlay coordinates if needed
            return this;
        }
        
        @Override
        public VertexConsumer setUv2(int u, int v) {
            // Store light coordinates if needed
            return this;
        }
          @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            currentVertex.nx = x;
            currentVertex.ny = y;
            currentVertex.nz = z;
            // When normal is set, we consider the vertex complete and add it to our list
            vertices.add(currentVertex.copy());
            currentVertex = new VertexData(); // Reset for next vertex
            return this;
        }
        
        public int getVertexCount() {
            return vertices.size();
        }
          /**
         * Generate BakedQuads from captured vertices.
         * Groups vertices into quads (4 vertices each).
         */
        public List<BakedQuad> generateQuads() {
            List<BakedQuad> quads = new ArrayList<>();
            
            // For this initial implementation, create a simple placeholder quad
            // In a complete implementation, you would convert the captured vertex data into proper BakedQuads
            // This is a complex process that involves vertex data packing and proper UV/normal handling
            
            if (vertices.size() >= 4) {
                // For now, we'll create a simple cube-like model
                // This is a placeholder that represents the LittleTiles block
                quads.addAll(createSimpleCube());
                
                LOGGER.debug("Generated {} placeholder quads for LittleTiles block", quads.size());
            }
            
            return quads;
        }
        
        /**
         * Create simple cube quads as a placeholder for LittleTiles blocks.
         */
        private List<BakedQuad> createSimpleCube() {
            List<BakedQuad> quads = new ArrayList<>();
            
            // This is a simplified approach - in a real implementation you would
            // convert the actual captured vertex data into proper BakedQuads
            // For now, we return an empty list as a placeholder
            
            return quads; // Placeholder - return empty for now
        }
    }
    
    /**
     * Simple data structure to hold vertex information.
     */
    private static class VertexData {
        float x, y, z;    // Position
        int r, g, b, a;   // Color
        float u, v;       // UV coordinates
        float nx, ny, nz; // Normal
        
        public VertexData copy() {
            VertexData copy = new VertexData();
            copy.x = this.x; copy.y = this.y; copy.z = this.z;
            copy.r = this.r; copy.g = this.g; copy.b = this.b; copy.a = this.a;
            copy.u = this.u; copy.v = this.v;
            copy.nx = this.nx; copy.ny = this.ny; copy.nz = this.nz;
            return copy;
        }
    }
      /**
     * Simple implementation of BakedModel that returns our captured quads.
     */
    private static class SimpleBakedModel implements BakedModel {
        
        private final List<BakedQuad> quads;
        
        public SimpleBakedModel(List<BakedQuad> quads) {
            this.quads = quads;
        }
          @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, 
                                       RandomSource random, ModelData modelData, @Nullable RenderType renderType) {
            // Return all quads for any face (simplified approach)
            return direction == null ? quads : List.of();
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            // Legacy method - delegate to the new one
            return getQuads(state, direction, random, ModelData.EMPTY, null);
        }
          @Override
        public net.neoforged.neoforge.common.util.TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
            return net.neoforged.neoforge.common.util.TriState.TRUE;
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
        public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() {
            // Return a default particle icon
            return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
        }
        
        @Override
        public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon(ModelData data) {
            return getParticleIcon();
        }
        
        @Override
        public ModelData getModelData(BlockAndTintGetter level, net.minecraft.core.BlockPos pos, 
                                     BlockState state, ModelData modelData) {
            return modelData;
        }
          @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
            return ChunkRenderTypeSet.of(RenderType.solid());
        }
        
        @Override
        public net.minecraft.client.renderer.block.model.ItemOverrides getOverrides() {
            return net.minecraft.client.renderer.block.model.ItemOverrides.EMPTY;
        }
    }
}
