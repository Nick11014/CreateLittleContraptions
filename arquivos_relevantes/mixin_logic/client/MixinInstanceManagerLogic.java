/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jozufozu.flywheel.backend.instancing.AbstractInstance
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity
 *  com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity
 *  com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity
 *  com.simibubi.create.content.trains.bogey.AbstractBogeyBlockEntity
 *  kotlin.jvm.internal.Intrinsics
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Vector3ic
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.valkyrienskies.core.api.ships.Ship
 *  org.valkyrienskies.mod.common.VSGameUtilsKt
 *  org.valkyrienskies.mod.common.util.VectorConversionsMCKt
 */
package org.valkyrienskies.create_interactive.mixin_logic.client;

import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlockEntity;
import java.lang.ref.WeakReference;
import kotlin.jvm.internal.Intrinsics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.create_interactive.CreateInteractiveUtil;
import org.valkyrienskies.create_interactive.mixinducks.ContraptionDuck;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public final class MixinInstanceManagerLogic {
    @NotNull
    public static final MixinInstanceManagerLogic INSTANCE = new MixinInstanceManagerLogic();

    private MixinInstanceManagerLogic() {
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public final boolean shouldRemoveBlockEntityInShip$create_interactive(@NotNull BlockEntity blockEntity) {
        Intrinsics.checkNotNullParameter((Object)blockEntity, (String)"blockEntity");
        Level level = blockEntity.m_58904_();
        BlockPos pos = blockEntity.m_58899_();
        Intrinsics.checkNotNull((Object)pos);
        Ship ship = VSGameUtilsKt.getShipManagingPos((Level)level, (BlockPos)pos);
        if (ship == null) {
            return false;
        }
        Ship ship2 = ship;
        WeakReference<AbstractContraptionEntity> weakReference = CreateInteractiveUtil.INSTANCE.getShipIdToContraptionEntityClient().get(ship2.getId());
        if (weakReference == null) {
            return false;
        }
        WeakReference<AbstractContraptionEntity> contraptionEntityWeakReference = weakReference;
        AbstractContraptionEntity abstractContraptionEntity = (AbstractContraptionEntity)contraptionEntityWeakReference.get();
        if (abstractContraptionEntity == null) {
            return false;
        }
        AbstractContraptionEntity contraptionEntity = abstractContraptionEntity;
        Level level2 = level;
        Intrinsics.checkNotNull((Object)level2);
        Vector3ic shipCenter = CreateInteractiveUtil.INSTANCE.getChunkClaimCenterPos(ship2, level2);
        BlockPos relativePos = pos.m_121996_((Vec3i)VectorConversionsMCKt.toBlockPos((Vector3ic)shipCenter));
        if (blockEntity instanceof AbstractBogeyBlockEntity) {
            Contraption contraption = contraptionEntity.getContraption();
            Intrinsics.checkNotNull((Object)contraption, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionDuck");
            ContraptionDuck contraptionDuck = (ContraptionDuck)contraption;
            Intrinsics.checkNotNull((Object)relativePos);
            return contraptionDuck.ci$hasBogeyAtPos(relativePos);
        }
        if (blockEntity instanceof DeployerBlockEntity) return false;
        if (blockEntity instanceof SlidingDoorBlockEntity) return false;
        if (blockEntity instanceof MechanicalBearingBlockEntity) return false;
        Contraption contraption = contraptionEntity.getContraption();
        Intrinsics.checkNotNull((Object)contraption, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionDuck");
        ContraptionDuck contraptionDuck = (ContraptionDuck)contraption;
        Intrinsics.checkNotNull((Object)relativePos);
        if (!contraptionDuck.ci$hasActorAtPos(relativePos)) return false;
        return true;
    }

    public final void preCreateInternal$create_interactive(@NotNull Object obj, @NotNull CallbackInfoReturnable<AbstractInstance> cir) {
        Intrinsics.checkNotNullParameter((Object)obj, (String)"obj");
        Intrinsics.checkNotNullParameter(cir, (String)"cir");
        if (obj instanceof BlockEntity && this.shouldRemoveBlockEntityInShip$create_interactive((BlockEntity)obj)) {
            cir.setReturnValue(null);
        }
    }
}
