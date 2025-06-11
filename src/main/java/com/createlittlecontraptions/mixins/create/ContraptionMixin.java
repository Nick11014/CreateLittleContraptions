package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.mixins.duck.IContraptionBakedModelCache;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.Optional;

/**
 * Mixin to add baked model caching capability directly to Contraption objects.
 * This implements the Duck Interface pattern to extend the Contraption class
 * without modifying the original Create mod code.
 */
@Mixin(value = Contraption.class, remap = false)
public class ContraptionMixin implements IContraptionBakedModelCache {
    
    @Unique
    private Map<BlockPos, BakedModel> bakedModelCache;
    
    @Override
    public void setModelCache(Map<BlockPos, BakedModel> cache) {
        this.bakedModelCache = cache;
    }
    
    @Override
    public Optional<Map<BlockPos, BakedModel>> getModelCache() {
        return Optional.ofNullable(this.bakedModelCache);
    }
}
