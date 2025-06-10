package com.createlittlecontraptions.mixins.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to track when contraption entities are being rendered
 * so we can set up the rendering context for our model substitution.
 * 
 * Temporarily disabled to avoid mixin injection issues.
 */
@Mixin(AbstractContraptionEntity.class)
public class AbstractContraptionEntityMixin {
    
    // TODO: Re-implement tick injection once mixin obfuscation issues are resolved
    // For now, we'll handle contraption tracking via events in ContraptionEventHandler
    
    /*
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            AbstractContraptionEntity entity = (AbstractContraptionEntity) (Object) this;
            if (entity.level().isClientSide) {
                // Only set context on client side when contraption is active
                ContraptionRenderingContext.setContext(entity.getUUID());
            }
        } catch (Exception e) {
            LOGGER.warn("Error setting up contraption rendering context during tick: {}", e.getMessage());
        }
    }
    */
}
