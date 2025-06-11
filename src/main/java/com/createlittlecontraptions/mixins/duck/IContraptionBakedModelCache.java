package com.createlittlecontraptions.mixins.duck;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.Optional;

/**
 * Duck Interface for Contraption to add baked model caching capability.
 * This interface is implemented via mixin to add caching directly to Contraption objects.
 */
public interface IContraptionBakedModelCache {
    
    /**
     * Set the baked model cache for this contraption.
     * @param cache Map of BlockPos to BakedModel
     */
    void setModelCache(Map<BlockPos, BakedModel> cache);
    
    /**
     * Get the baked model cache for this contraption.
     * @return Optional containing the cache map, empty if no cache is set
     */
    Optional<Map<BlockPos, BakedModel>> getModelCache();
}
