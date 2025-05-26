package com.createlittlecontraptions.utils;

import net.minecraft.world.level.Level;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import org.joml.Matrix4f;

/**
 * Helper class to store rendering context for LittleTiles rendering in contraptions.
 * Moved to utils package to avoid Mixin package restrictions.
 */
public class RenderContext {
    public final Level realLevel;
    public final VirtualRenderWorld renderLevel;
    public final Matrix4f lightTransform;
    
    public RenderContext(Level realLevel, VirtualRenderWorld renderLevel, Matrix4f lightTransform) {
        this.realLevel = realLevel;
        this.renderLevel = renderLevel;
        this.lightTransform = lightTransform;
    }
}
