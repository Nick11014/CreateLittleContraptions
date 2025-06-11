/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  kotlin.jvm.internal.Intrinsics
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.NotNull
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package org.valkyrienskies.create_interactive.mixin_logic.client;

import kotlin.jvm.internal.Intrinsics;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.create_interactive.mixin_logic.client.MixinInstanceManagerLogic;

public final class MixinBlockEntityRenderDispatcherLogic {
    @NotNull
    public static final MixinBlockEntityRenderDispatcherLogic INSTANCE = new MixinBlockEntityRenderDispatcherLogic();

    private MixinBlockEntityRenderDispatcherLogic() {
    }

    public final <E extends BlockEntity> void preRender$create_interactive(@NotNull E blockEntity, @NotNull CallbackInfo ci) {
        Intrinsics.checkNotNullParameter(blockEntity, (String)"blockEntity");
        Intrinsics.checkNotNullParameter((Object)ci, (String)"ci");
        if (MixinInstanceManagerLogic.INSTANCE.shouldRemoveBlockEntityInShip$create_interactive(blockEntity)) {
            ci.cancel();
        }
    }
}
