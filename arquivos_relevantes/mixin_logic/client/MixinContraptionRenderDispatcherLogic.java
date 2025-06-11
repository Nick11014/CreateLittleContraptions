/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jozufozu.flywheel.core.model.ShadeSeparatedBufferedData
 *  com.jozufozu.flywheel.core.model.WorldModelBuilder
 *  com.jozufozu.flywheel.core.virtual.VirtualRenderWorld
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.foundation.render.SuperByteBuffer
 *  kotlin.collections.CollectionsKt
 *  kotlin.jvm.internal.Intrinsics
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.level.BlockAndTintGetter
 *  org.jetbrains.annotations.NotNull
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package org.valkyrienskies.create_interactive.mixin_logic.client;

import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferedData;
import com.jozufozu.flywheel.core.model.WorldModelBuilder;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import java.util.Collection;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.create_interactive.CreateInteractiveUtil;

public final class MixinContraptionRenderDispatcherLogic {
    @NotNull
    public static final MixinContraptionRenderDispatcherLogic INSTANCE = new MixinContraptionRenderDispatcherLogic();

    private MixinContraptionRenderDispatcherLogic() {
    }

    public final void preBuildStructureBuffer$create_interactive(@NotNull VirtualRenderWorld renderWorld, @NotNull Contraption c, @NotNull RenderType layer, @NotNull CallbackInfoReturnable<SuperByteBuffer> cir) {
        Intrinsics.checkNotNullParameter((Object)renderWorld, (String)"renderWorld");
        Intrinsics.checkNotNullParameter((Object)c, (String)"c");
        Intrinsics.checkNotNullParameter((Object)layer, (String)"layer");
        Intrinsics.checkNotNullParameter(cir, (String)"cir");
        if (CreateInteractiveUtil.INSTANCE.doesContraptionHaveShipLoaded(c)) {
            ShadeSeparatedBufferedData data = new WorldModelBuilder(layer).withRenderWorld((BlockAndTintGetter)renderWorld).withBlocks((Collection)CollectionsKt.emptyList()).build();
            SuperByteBuffer sbb = new SuperByteBuffer(data);
            data.release();
            cir.setReturnValue((Object)sbb);
        }
    }

    public final void preRenderBlockEntities$create_interactive(@NotNull Contraption c, @NotNull CallbackInfo ci) {
        Intrinsics.checkNotNullParameter((Object)c, (String)"c");
        Intrinsics.checkNotNullParameter((Object)ci, (String)"ci");
        if (CreateInteractiveUtil.INSTANCE.doesContraptionHaveShipLoaded(c)) {
            ci.cancel();
        }
    }
}
