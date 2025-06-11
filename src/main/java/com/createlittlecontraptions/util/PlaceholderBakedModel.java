package com.createlittlecontraptions.util;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.Collections;
import java.util.List;

/**
 * Utility class for creating placeholder BakedModels to use in caches
 * instead of null values (which ConcurrentHashMap doesn't allow).
 */
public class PlaceholderBakedModel implements BakedModel {
    
    public static final PlaceholderBakedModel INSTANCE = new PlaceholderBakedModel();
    
    private PlaceholderBakedModel() {
        // Private constructor - use INSTANCE
    }
    
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource random) {
        return Collections.emptyList();
    }
    
    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }
    
    @Override
    public boolean isGui3d() {
        return false;
    }
    
    @Override
    public boolean usesBlockLight() {
        return false;
    }
    
    @Override
    public boolean isCustomRenderer() {
        return false;
    }
    
    @Override
    public TextureAtlasSprite getParticleIcon() {
        // Return a default sprite - this might need to be adjusted
        return null; // This is acceptable for particle icon
    }
    
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
    
    @Override
    public String toString() {
        return "PlaceholderBakedModel{}";
    }
}
