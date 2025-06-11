package com.createlittlecontraptions.mixins;

import com.createlittlecontraptions.CreateLittleContraptions;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para interceptar a renderização de contraptions do Create
 * Simplificado após remoção do código experimental
 * 
 * Este Mixin intercepta o método shouldRender das contraptions para debugging
 */
@Mixin(AbstractContraptionEntity.class)
public class ContraptionRendererMixin {
      private static long lastLogTime = 0;
    private static int accessCount = 0;
    private static final long LOG_INTERVAL_MS = 5000; // Log only once every 5 seconds
    
    /**
     * Intercepts the isReadyForRender() method for debugging
     */
    @Inject(method = "isReadyForRender", at = @At("HEAD"), cancellable = true, remap = false)
    public void onIsReadyForRender(CallbackInfoReturnable<Boolean> cir) {
        // Note: Complex rendering control removed - mixin simplified
        // This mixin is now passive for debugging only
        long currentTime = System.currentTimeMillis();
        accessCount++;
        
        // Only log once every 5 seconds
        if (currentTime - lastLogTime > LOG_INTERVAL_MS) {
            CreateLittleContraptions.LOGGER.debug("[ContraptionRendererMixin] Render access - {} calls in last {}ms", 
                accessCount, currentTime - lastLogTime);
            lastLogTime = currentTime;
            accessCount = 0;
        }
    }
}
