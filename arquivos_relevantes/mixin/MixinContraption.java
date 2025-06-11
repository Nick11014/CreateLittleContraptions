/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.content.contraptions.StructureTransform
 *  com.simibubi.create.content.contraptions.behaviour.MovementContext
 *  com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour
 *  kotlin.Unit
 *  kotlin.jvm.functions.Function1
 *  kotlin.jvm.functions.Function2
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$StructureBlockInfo
 *  net.minecraft.world.phys.AABB
 *  org.apache.commons.lang3.tuple.MutablePair
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package org.valkyrienskies.create_interactive.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.create_interactive.mixin_logic.MixinContraptionLogic;
import org.valkyrienskies.create_interactive.mixinducks.ContraptionDuck;

@Mixin(value={Contraption.class})
public abstract class MixinContraption
implements ContraptionDuck {
    @Unique
    private Set<BlockPos> ci$changedActors;
    @Shadow(remap=false)
    public boolean disassembled;
    @Shadow(remap=false)
    public AbstractContraptionEntity entity;
    @Shadow(remap=false)
    protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;
    @Shadow(remap=false)
    public AABB bounds;
    @Shadow(remap=false)
    protected List<MutablePair<StructureTemplate.StructureBlockInfo, MovementContext>> actors;
    @Shadow(remap=false)
    protected Map<BlockPos, MovingInteractionBehaviour> interactors;

    @Shadow(remap=false)
    protected abstract void disableActorOnStart(MovementContext var1);

    @Shadow
    protected abstract CompoundTag getBlockEntityNBT(Level var1, BlockPos var2);

    @Inject(method={"<init>"}, at={@At(value="RETURN")}, remap=false)
    private void postInit(CallbackInfo ci) {
        this.ci$changedActors = new HashSet<BlockPos>();
    }

    @Inject(method={"onEntityCreated"}, at={@At(value="HEAD")}, remap=false)
    private void preOnEntityCreated(AbstractContraptionEntity entity, CallbackInfo ci) {
        MixinContraptionLogic.INSTANCE.preOnEntityCreated$create_interactive(this.blocks, entity);
    }

    @Inject(method={"addBlocksToWorld"}, at={@At(value="HEAD")})
    private void preAddBlocksToWorld(Level world, StructureTransform transform, CallbackInfo ci) {
        MixinContraptionLogic.INSTANCE.preAddBlocksToWorld$create_interactive(this.disassembled, this.entity, this.blocks, world, (Function2<? super Level, ? super BlockPos, ? extends CompoundTag>)((Function2)this::getBlockEntityNBT));
    }

    @Inject(method={"addBlocksToWorld"}, at={@At(value="RETURN")})
    private void postAddBlocksToWorld(Level world, StructureTransform transform, CallbackInfo ci) {
        MixinContraptionLogic.INSTANCE.postAddBlocksToWorld$create_interactive(this.entity, this.blocks, world, transform);
    }

    @Override
    public void ci$setBlock(@NotNull BlockPos localPos, // Could not load outer class - annotation placement on inner may be incorrect
     @NotNull StructureTemplate.StructureBlockInfo structureBlockInfo) {
        MixinContraptionLogic.INSTANCE.setBlock$create_interactive(this.blocks, this.actors, this.bounds, localPos, structureBlockInfo, (Function1<? super AABB, Unit>)((Function1)a -> {
            this.bounds = a;
            return Unit.INSTANCE;
        }), (Function1<? super MovementContext, Unit>)((Function1)a -> {
            this.disableActorOnStart((MovementContext)a);
            return Unit.INSTANCE;
        }), this.ci$changedActors, this.interactors, (Contraption)Contraption.class.cast(this));
    }

    @Override
    public boolean ci$hasActorAtPos(@NotNull BlockPos localPos) {
        return MixinContraptionLogic.INSTANCE.hasActorAtPos$create_interactive(localPos, this.actors);
    }

    @Override
    public boolean ci$hasBogeyAtPos(@NotNull BlockPos localPos) {
        return MixinContraptionLogic.INSTANCE.hasBogeyAtPos$create_interactive(this.entity, localPos);
    }

    @Override
    @Nullable
    public Pair<StructureTemplate.StructureBlockInfo, MovementContext> ci$getActorAtPos(@NotNull BlockPos localPos) {
        return MixinContraptionLogic.INSTANCE.getActorAtPos$create_interactive(localPos, this.actors);
    }

    @Override
    @NotNull
    public Collection<BlockPos> ci$getChangedActors() {
        return this.ci$changedActors;
    }

    @Override
    public void ci$clearChangedActors() {
        this.ci$changedActors.clear();
    }

    @Inject(method={"writeBlocksCompound"}, at={@At(value="HEAD")}, remap=false)
    private void preWriteBlocksCompound(CallbackInfoReturnable<CompoundTag> cir) {
        MixinContraptionLogic.INSTANCE.preWriteBlocksCompound$create_interactive((Contraption)Contraption.class.cast(this));
    }
}
