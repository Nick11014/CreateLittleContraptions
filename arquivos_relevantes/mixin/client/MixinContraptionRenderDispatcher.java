/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jozufozu.flywheel.core.virtual.VirtualRenderWorld
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.content.contraptions.render.ContraptionMatrices
 *  com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher
 *  com.simibubi.create.foundation.render.SuperByteBuffer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package org.valkyrienskies.create_interactive.mixin.client;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.create_interactive.mixin_logic.client.MixinContraptionRenderDispatcherLogic;

@Mixin(value={ContraptionRenderDispatcher.class})
public class MixinContraptionRenderDispatcher {
    @Inject(method={"buildStructureBuffer"}, at={@At(value="HEAD")}, cancellable=true)
    private static void preBuildStructureBuffer(VirtualRenderWorld renderWorld, Contraption c, RenderType layer, CallbackInfoReturnable<SuperByteBuffer> cir) {
        MixinContraptionRenderDispatcherLogic.INSTANCE.preBuildStructureBuffer$create_interactive(renderWorld, c, layer, cir);
    }

    @Inject(method={"renderBlockEntities"}, at={@At(value="HEAD")}, cancellable=true)
    private static void preRenderBlockEntities(Level world, VirtualRenderWorld renderWorld, Contraption c, ContraptionMatrices matrices, MultiBufferSource buffer, CallbackInfo ci) {
        MixinContraptionRenderDispatcherLogic.INSTANCE.preRenderBlockEntities$create_interactive(c, ci);
    }
}
