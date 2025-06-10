package com.createlittlecontraptions.rendering;

import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.UUID;

/**
 * Context tracking for contraption rendering to help mixins identify
 * when we're rendering contraption blocks vs. regular world blocks.
 */
public class ContraptionRenderingContext {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<ContraptionContext> currentContext = new ThreadLocal<>();
    
    /**
     * Set the current contraption rendering context.
     * This should be called when starting to render a contraption.
     */
    public static void setContext(UUID contraptionId) {
        currentContext.set(new ContraptionContext(contraptionId));
        LOGGER.debug("Set contraption rendering context for {}", contraptionId);
    }
    
    /**
     * Set the current position being rendered within the contraption.
     */
    public static void setCurrentPosition(BlockPos pos) {
        ContraptionContext context = currentContext.get();
        if (context != null) {
            context.setCurrentPosition(pos);
        }
    }
    
    /**
     * Get the current contraption rendering context.
     */
    public static ContraptionContext getCurrentContext() {
        return currentContext.get();
    }
    
    /**
     * Clear the current contraption rendering context.
     * This should be called when finishing contraption rendering.
     */
    public static void clearContext() {
        ContraptionContext context = currentContext.get();
        if (context != null) {
            LOGGER.debug("Cleared contraption rendering context for {}", context.getContraptionId());
        }
        currentContext.remove();
    }
    
    /**
     * Check if we're currently in a contraption rendering context.
     */
    public static boolean isInContraptionContext() {
        return currentContext.get() != null;
    }
    
    /**
     * Context data for contraption rendering.
     */
    public static class ContraptionContext {
        private final UUID contraptionId;
        private BlockPos currentPosition;
        
        public ContraptionContext(UUID contraptionId) {
            this.contraptionId = contraptionId;
        }
        
        public UUID getContraptionId() {
            return contraptionId;
        }
        
        public BlockPos getCurrentPosition() {
            return currentPosition;
        }
        
        public void setCurrentPosition(BlockPos currentPosition) {
            this.currentPosition = currentPosition;
        }
    }
}
