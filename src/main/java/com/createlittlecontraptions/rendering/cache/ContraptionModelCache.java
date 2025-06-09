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
 * Cache for storing baked models generated from LittleTiles blocks.
 * Models are cached by contraption UUID and block position.
 */
public class ContraptionModelCache {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Map<BlockPos, BakedModel>> CACHED_MODELS = new ConcurrentHashMap<>();
    
    /**
     * Cache a baked model for a specific contraption and block position.
     * 
     * @param contraptionId The UUID of the contraption entity
     * @param pos The block position within the contraption
     * @param model The baked model to cache
     */
    public static void cacheModel(UUID contraptionId, BlockPos pos, BakedModel model) {
        CACHED_MODELS.computeIfAbsent(contraptionId, k -> new ConcurrentHashMap<>()).put(pos, model);
        LOGGER.debug("Cached model for contraption {} at position {}", contraptionId, pos);
    }
    
    /**
     * Retrieve a cached model for a specific contraption and block position.
     * 
     * @param contraptionId The UUID of the contraption entity
     * @param pos The block position within the contraption
     * @return Optional containing the cached model, or empty if not found
     */
    public static Optional<BakedModel> getModel(UUID contraptionId, BlockPos pos) {
        Map<BlockPos, BakedModel> contraptionCache = CACHED_MODELS.get(contraptionId);
        if (contraptionCache != null) {
            BakedModel model = contraptionCache.get(pos);
            if (model != null) {
                LOGGER.debug("Retrieved cached model for contraption {} at position {}", contraptionId, pos);
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Clear all cached models for a specific contraption.
     * Should be called when a contraption is disassembled.
     * 
     * @param contraptionId The UUID of the contraption entity
     */
    public static void clearCache(UUID contraptionId) {
        Map<BlockPos, BakedModel> removed = CACHED_MODELS.remove(contraptionId);
        if (removed != null) {
            LOGGER.info("Cleared {} cached models for contraption {}", removed.size(), contraptionId);
        }
    }
    
    /**
     * Get the total number of cached models across all contraptions.
     * Useful for debugging and monitoring.
     */
    public static int getTotalCachedModels() {
        return CACHED_MODELS.values().stream().mapToInt(Map::size).sum();
    }
    
    /**
     * Get the number of contraptions with cached models.
     * Useful for debugging and monitoring.
     */
    public static int getCachedContraptionCount() {
        return CACHED_MODELS.size();
    }
}
