package com.createlittlecontraptions.duck;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import java.util.Map;
import java.util.Optional;

/**
 * Duck Interface for Contraption to add per-contraption BakedModel caching.
 * This interface is implemented by Contraption via mixin.
 */
public interface IContraptionBakedModelCache {
    
    /**
     * Get the model cache for this contraption.
     * @return Optional containing the model cache map, or empty if no cache exists
     */
    Optional<Map<BlockPos, BakedModel>> getModelCache();
    
    /**
     * Set the model cache for this contraption.
     * @param cache The model cache to store
     */
    void setModelCache(Map<BlockPos, BakedModel> cache);
    
    /**
     * Clear the model cache for this contraption.
     */
    void clearModelCache();
}
