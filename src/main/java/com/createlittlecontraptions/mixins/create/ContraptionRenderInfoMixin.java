package com.createlittlecontraptions.mixins.create;

import com.simibubi.create.content.contraptions.render.ContraptionRenderInfo;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to inject baked LittleTiles models into Create's contraption rendering pipeline.
 * 
 * Simplified to avoid obfuscation issues with shadow fields.
 */
@Mixin(value = ContraptionRenderInfo.class, priority = 1500)
public class ContraptionRenderInfoMixin {
    
    // Simplified - removed shadow field and methods that might cause obfuscation issues
    // We'll implement model access through other means
}
