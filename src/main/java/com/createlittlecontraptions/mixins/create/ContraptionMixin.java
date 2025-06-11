package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions.ContraptionMixin");
    
    static {
        LOGGER.info("CLCLC: ContraptionMixin static initializer called - Mixin loaded");
    }
    
    @Unique
    private Map<BlockPos, BakedModel> bakedModelCache;    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(CallbackInfo ci) {
        int objectId = System.identityHashCode(this);
        LOGGER.info("CLCLC: ContraptionMixin applied successfully - Duck Interface ready [Object ID: {}]", objectId);
        this.bakedModelCache = new java.util.concurrent.ConcurrentHashMap<>();
    }    @Override
    public void setModelCache(Map<BlockPos, BakedModel> cache) {
        this.bakedModelCache = cache;
        int objectId = System.identityHashCode(this);
        if (cache != null && !cache.isEmpty()) {
            LOGGER.info("CLCLC: Model cache set with {} entries [Object ID: {}] [Thread: {}]", 
                       cache.size(), objectId, Thread.currentThread().getName());
        } else if (cache != null && cache.isEmpty()) {
            LOGGER.info("CLCLC: Model cache set but empty [Object ID: {}] [Thread: {}]", 
                       objectId, Thread.currentThread().getName());
        }
    }
      @Override
    public Optional<Map<BlockPos, BakedModel>> getModelCache() {
        return Optional.ofNullable(this.bakedModelCache);
    }
      @Override
    public void clearModelCache() {
        int objectId = System.identityHashCode(this);
        if (this.bakedModelCache != null && !this.bakedModelCache.isEmpty()) {
            LOGGER.info("CLCLC: Clearing model cache with {} entries [Object ID: {}]", 
                       this.bakedModelCache.size(), objectId);
        }
        this.bakedModelCache = null;
    }
}
