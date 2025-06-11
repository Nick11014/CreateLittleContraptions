package com.createlittlecontraptions.rendering.cache;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache system for storing baked models of LittleTiles blocks in contraptions.
 * Part of the Model Baking solution for CreateLittleContraptions compatibility.
 */
public class ContraptionModelCache {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Cache storage: UUID (contraption entity ID) -> BlockPos -> BakedModel
     */
    private static final Map<UUID, Map<BlockPos, BakedModel>> CACHED_MODELS = new ConcurrentHashMap<>();
    
    /**
     * Cache a baked model for a specific block position in a contraption.
     * 
     * @param contraptionId The UUID of the contraption entity
     * @param pos The block position within the contraption
     * @param model The baked model to cache
     */
    public static void cacheModel(UUID contraptionId, BlockPos pos, BakedModel model) {
        if (contraptionId == null || pos == null || model == null) {
            LOGGER.warn("Attempted to cache model with null parameters: contraptionId={}, pos={}, model={}", 
                contraptionId, pos, model);
            return;
        }
        
        CACHED_MODELS.computeIfAbsent(contraptionId, k -> new ConcurrentHashMap<>()).put(pos, model);
        LOGGER.debug("Cached model for contraption {} at position {}", contraptionId, pos);
    }
    
    /**
     * Retrieve a cached baked model for a specific block position in a contraption.
     * 
     * @param contraptionId The UUID of the contraption entity
     * @param pos The block position within the contraption
     * @return Optional containing the cached model, or empty if not found
     */
    public static Optional<BakedModel> getModel(UUID contraptionId, BlockPos pos) {
        if (contraptionId == null || pos == null) {
            return Optional.empty();
        }
        
        Map<BlockPos, BakedModel> contraptionCache = CACHED_MODELS.get(contraptionId);
        if (contraptionCache == null) {
            return Optional.empty();
        }
        
        BakedModel model = contraptionCache.get(pos);
        if (model != null) {
            LOGGER.debug("Retrieved cached model for contraption {} at position {}", contraptionId, pos);
        }
        
        return Optional.ofNullable(model);
    }
    
    /**
     * Clear all cached models for a specific contraption.
     * Should be called when a contraption is disassembled to free memory.
     * 
     * @param contraptionId The UUID of the contraption entity
     */
    public static void clearCache(UUID contraptionId) {
        if (contraptionId == null) {
            return;
        }
        
        Map<BlockPos, BakedModel> removed = CACHED_MODELS.remove(contraptionId);
        if (removed != null) {
            LOGGER.debug("Cleared cache for contraption {} ({} models)", contraptionId, removed.size());
        }
    }
    
    /**
     * Get the total number of cached contraptions.
     * 
     * @return Number of contraptions with cached models
     */
    public static int getCachedContraptionCount() {
        return CACHED_MODELS.size();
    }
    
    /**
     * Get the total number of cached models across all contraptions.
     * 
     * @return Total number of cached models
     */
    public static int getTotalCachedModelCount() {
        return CACHED_MODELS.values().stream()
            .mapToInt(Map::size)
            .sum();
    }
    
    /**
     * Clear all cached models. Should be used sparingly, mainly for debugging.
     */
    public static void clearAllCaches() {
        int contraptionCount = CACHED_MODELS.size();
        int modelCount = getTotalCachedModelCount();
        CACHED_MODELS.clear();
        LOGGER.info("Cleared all caches: {} contraptions, {} models", contraptionCount, modelCount);
    }
}
