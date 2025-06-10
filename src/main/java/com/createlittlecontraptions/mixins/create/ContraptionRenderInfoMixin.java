package com.createlittlecontraptions.mixins.create;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfo;
import com.simibubi.create.content.contraptions.Contraption;
import com.createlittlecontraptions.rendering.cache.ContraptionModelCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

/**
 * Mixin to inject baked LittleTiles models into Create's contraption rendering pipeline.
 * 
 * This is a simplified approach that provides a method for accessing cached models
 * without directly intercepting Create's rendering methods.
 */
@Mixin(ContraptionRenderInfo.class)
public class ContraptionRenderInfoMixin {

    /**
     * Shadow field to access the contraption from the render info.
     */
    @Shadow
    private Contraption contraption;

    /**
     * Public method to check if we have a cached model for a specific position.
     * This can be called by other parts of the system to retrieve cached models.
     * 
     * @param pos The position to check for cached models
     * @param state The block state at that position
     * @return An optional cached BakedModel if available
     */
    public Optional<BakedModel> getCachedModel(BlockPos pos, BlockState state) {
        if (contraption == null || contraption.entity == null) {
            return Optional.empty();
        }
        
        return ContraptionModelCache.getModel(contraption.entity.getUUID(), pos);
    }
}
