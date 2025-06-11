/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity$ContraptionRotationState
 *  com.simibubi.create.content.contraptions.BlockMovementChecks
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.content.contraptions.StructureTransform
 *  com.simibubi.create.content.contraptions.TranslatingContraption
 *  com.simibubi.create.content.contraptions.bearing.BearingContraption
 *  com.simibubi.create.content.contraptions.bearing.ClockworkContraption
 *  com.simibubi.create.content.contraptions.behaviour.MovementContext
 *  com.simibubi.create.content.contraptions.mounted.MountedContraption
 *  com.simibubi.create.content.trains.entity.Carriage
 *  com.simibubi.create.content.trains.entity.Carriage$DimensionalCarriageEntity
 *  com.simibubi.create.content.trains.entity.CarriageBogey
 *  com.simibubi.create.content.trains.entity.CarriageContraption
 *  com.simibubi.create.content.trains.entity.CarriageContraptionEntity
 *  com.simibubi.create.content.trains.entity.Train
 *  com.simibubi.create.content.trains.entity.TrainRelocator
 *  com.simibubi.create.content.trains.entity.TravellingPoint
 *  com.simibubi.create.content.trains.graph.TrackNode
 *  com.simibubi.create.content.trains.graph.TrackNodeLocation
 *  com.simibubi.create.content.trains.track.ITrackBlock
 *  com.simibubi.create.content.trains.track.TrackBlock
 *  com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer
 *  kotlin.NoWhenBranchMatchedException
 *  kotlin.collections.CollectionsKt
 *  kotlin.jvm.internal.DefaultConstructorMarker
 *  kotlin.jvm.internal.Intrinsics
 *  kotlin.math.MathKt
 *  kotlin.text.StringsKt
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$StructureBlockInfo
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4dc
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.joml.Vector3i
 *  org.joml.Vector3ic
 *  org.joml.primitives.AABBd
 *  org.joml.primitives.AABBdc
 *  org.valkyrienskies.core.api.ships.ClientShip
 *  org.valkyrienskies.core.api.ships.ServerShip
 *  org.valkyrienskies.core.api.ships.ServerShipTransformProvider
 *  org.valkyrienskies.core.api.ships.ServerShipTransformProvider$NextTransformAndVelocityData
 *  org.valkyrienskies.core.api.ships.Ship
 *  org.valkyrienskies.core.api.ships.properties.ShipTransform
 *  org.valkyrienskies.core.apigame.ShipTeleportData
 *  org.valkyrienskies.core.impl.game.ships.ShipTransformImpl
 *  org.valkyrienskies.core.util.AABBdUtilKt
 *  org.valkyrienskies.mod.common.VSGameUtilsKt
 *  org.valkyrienskies.mod.common.util.ShipSettingsKt
 *  org.valkyrienskies.mod.common.util.VectorConversionsMCKt
 */
package org.valkyrienskies.create_interactive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.BlockMovementChecks;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.TranslatingContraption;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.ClockworkContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import kotlin.NoWhenBranchMatchedException;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.math.MathKt;
import kotlin.text.StringsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ServerShipTransformProvider;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.apigame.ShipTeleportData;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.util.AABBdUtilKt;
import org.valkyrienskies.create_interactive.CreateInteractiveContraptionRotationState;
import org.valkyrienskies.create_interactive.GameContent;
import org.valkyrienskies.create_interactive.config.CreateInteractiveConfigs;
import org.valkyrienskies.create_interactive.config.InteractiveHandling;
import org.valkyrienskies.create_interactive.mixin.CarriageBogeyAccessor;
import org.valkyrienskies.create_interactive.mixin.DimensionalCarriageEntityAccessor;
import org.valkyrienskies.create_interactive.mixin.TrainAccessor;
import org.valkyrienskies.create_interactive.mixin_logic.MixinTrainLogic;
import org.valkyrienskies.create_interactive.mixinducks.AbstractContraptionEntityDuck;
import org.valkyrienskies.create_interactive.mixinducks.ContraptionDuck;
import org.valkyrienskies.create_interactive.mixinducks.ContraptionRotationStateDuck;
import org.valkyrienskies.create_interactive.mixinducks.OrientedContraptionEntityDuck;
import org.valkyrienskies.create_interactive.services.NoOptimize;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.ShipSettingsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public final class CreateInteractiveUtil {
    @NotNull
    public static final CreateInteractiveUtil INSTANCE = new CreateInteractiveUtil();
    @NotNull
    private static final Map<Long, WeakReference<AbstractContraptionEntity>> shipIdToContraptionEntityClientInternal = new HashMap();
    @NotNull
    private static final Map<Long, WeakReference<AbstractContraptionEntity>> shipIdToContraptionEntityServerInternal = new HashMap();

    private CreateInteractiveUtil() {
    }

    public final boolean hasInteractMeNotSticker(@NotNull Iterable<? extends Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo>> blocks) {
        boolean bl;
        block3: {
            Intrinsics.checkNotNullParameter(blocks, (String)"blocks");
            Iterable<? extends Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo>> $this$any$iv = blocks;
            boolean $i$f$any = false;
            if ($this$any$iv instanceof Collection && ((Collection)$this$any$iv).isEmpty()) {
                bl = false;
            } else {
                Iterator<? extends Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo>> iterator = $this$any$iv.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo> element$iv;
                    Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo> it = element$iv = iterator.next();
                    boolean bl2 = false;
                    if (!it.getValue().f_74676_().m_60713_((Block)GameContent.INTERACT_ME_NOT.get())) continue;
                    bl = true;
                    break block3;
                }
                bl = false;
            }
        }
        return bl;
    }

    public final boolean hasInteractMeSticker(@NotNull Iterable<? extends Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo>> blocks) {
        boolean bl;
        block3: {
            Intrinsics.checkNotNullParameter(blocks, (String)"blocks");
            Iterable<? extends Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo>> $this$any$iv = blocks;
            boolean $i$f$any = false;
            if ($this$any$iv instanceof Collection && ((Collection)$this$any$iv).isEmpty()) {
                bl = false;
            } else {
                Iterator<? extends Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo>> iterator = $this$any$iv.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo> element$iv;
                    Map.Entry<? extends BlockPos, StructureTemplate.StructureBlockInfo> it = element$iv = iterator.next();
                    boolean bl2 = false;
                    if (!it.getValue().f_74676_().m_60713_((Block)GameContent.INTERACT_ME.get())) continue;
                    bl = true;
                    break block3;
                }
                bl = false;
            }
        }
        return bl;
    }

    @NotNull
    public final InteractiveHandling shouldBeInteractive(@NotNull Contraption contraption) {
        Intrinsics.checkNotNullParameter((Object)contraption, (String)"contraption");
        if (contraption instanceof CarriageContraption) {
            Object object = CreateInteractiveConfigs.server().trainHandling.get();
            Intrinsics.checkNotNullExpressionValue((Object)object, (String)"get(...)");
            return (InteractiveHandling)((Object)object);
        }
        if (contraption instanceof BearingContraption) {
            Object object = CreateInteractiveConfigs.server().bearingHandling.get();
            Intrinsics.checkNotNullExpressionValue((Object)object, (String)"get(...)");
            return (InteractiveHandling)((Object)object);
        }
        if (contraption instanceof ClockworkContraption) {
            Object object = CreateInteractiveConfigs.server().clockworkHandling.get();
            Intrinsics.checkNotNullExpressionValue((Object)object, (String)"get(...)");
            return (InteractiveHandling)((Object)object);
        }
        if (contraption instanceof TranslatingContraption) {
            Object object = CreateInteractiveConfigs.server().translatingHandling.get();
            Intrinsics.checkNotNullExpressionValue((Object)object, (String)"get(...)");
            return (InteractiveHandling)((Object)object);
        }
        if (contraption instanceof MountedContraption) {
            Object object = CreateInteractiveConfigs.server().mountedHandling.get();
            Intrinsics.checkNotNullExpressionValue((Object)object, (String)"get(...)");
            return (InteractiveHandling)((Object)object);
        }
        Object object = CreateInteractiveConfigs.server().otherHandling.get();
        Intrinsics.checkNotNullExpressionValue((Object)object, (String)"get(...)");
        return (InteractiveHandling)((Object)object);
    }

    /*
     * WARNING - void declaration
     */
    @Nullable
    public final Long createShipForContraption(@NotNull ServerLevel level, @NotNull Contraption contraption, @NotNull BlockPos blockPos, @NotNull Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks) {
        void $this$filterTo$iv$iv;
        void $this$filterTo$iv$iv2;
        Intrinsics.checkNotNullParameter((Object)level, (String)"level");
        Intrinsics.checkNotNullParameter((Object)contraption, (String)"contraption");
        Intrinsics.checkNotNullParameter((Object)blockPos, (String)"blockPos");
        Intrinsics.checkNotNullParameter(blocks, (String)"blocks");
        String string = contraption.getClass().getPackageName();
        Intrinsics.checkNotNullExpressionValue((Object)string, (String)"getPackageName(...)");
        if (StringsKt.contains$default((CharSequence)string, (CharSequence)"createbigcannons", (boolean)false, (int)2, null)) {
            return null;
        }
        Iterable $this$filter$iv = blocks.entrySet();
        boolean $i$f$filter = false;
        Iterable iterable = $this$filter$iv;
        Iterable destination$iv$iv = new ArrayList();
        boolean $i$f$filterTo = false;
        for (Object element$iv$iv : $this$filterTo$iv$iv2) {
            Map.Entry it = (Map.Entry)element$iv$iv;
            boolean bl = false;
            if (!(!BlockMovementChecks.isBrittle((BlockState)((StructureTemplate.StructureBlockInfo)it.getValue()).f_74676_()))) continue;
            destination$iv$iv.add(element$iv$iv);
        }
        List nonBrittleBlocks = (List)destination$iv$iv;
        Iterable $this$filter$iv2 = blocks.entrySet();
        boolean $i$f$filter2 = false;
        destination$iv$iv = $this$filter$iv2;
        Collection destination$iv$iv2 = new ArrayList();
        boolean $i$f$filterTo2 = false;
        for (Object element$iv$iv : $this$filterTo$iv$iv) {
            Map.Entry it = (Map.Entry)element$iv$iv;
            boolean bl = false;
            if (!BlockMovementChecks.isBrittle((BlockState)((StructureTemplate.StructureBlockInfo)it.getValue()).f_74676_())) continue;
            destination$iv$iv2.add(element$iv$iv);
        }
        List brittleBlocks = (List)destination$iv$iv2;
        List blocksOrderedCorrectly = CollectionsKt.plus((Collection)nonBrittleBlocks, (Iterable)brittleBlocks);
        InteractiveHandling handling = this.shouldBeInteractive(contraption);
        boolean shouldBeInteractive = switch (WhenMappings.$EnumSwitchMapping$0[handling.ordinal()]) {
            case 1 -> true;
            case 2 -> {
                if (!this.hasInteractMeNotSticker(blocksOrderedCorrectly)) {
                    yield true;
                }
                yield false;
            }
            case 3 -> this.hasInteractMeSticker(blocksOrderedCorrectly);
            case 4 -> false;
            default -> throw new NoWhenBranchMatchedException();
        };
        if (!shouldBeInteractive) {
            return null;
        }
        ServerShip serverShip = VSGameUtilsKt.getShipObjectWorld((ServerLevel)level).createNewShipAtBlock((Vector3ic)VectorConversionsMCKt.toJOML((Vec3i)((Vec3i)blockPos)), false, 1.0, VSGameUtilsKt.getDimensionId((Level)((Level)level)));
        Vector3ic shipCenter = this.getChunkClaimCenterPos((Ship)serverShip, (Level)level);
        for (Map.Entry entry : blocksOrderedCorrectly) {
            CompoundTag tag;
            BlockPos pos = (BlockPos)entry.getKey();
            StructureTemplate.StructureBlockInfo structureInfo = (StructureTemplate.StructureBlockInfo)entry.getValue();
            BlockPos newPos = pos.m_7918_(shipCenter.x(), shipCenter.y(), shipCenter.z());
            int flags = 67;
            level.m_7731_(newPos, structureInfo.f_74676_(), flags);
            BlockEntity newBlockEntity = level.m_7702_(newPos);
            if (newBlockEntity == null || (tag = structureInfo.f_74677_()) == null) continue;
            tag.m_128405_("x", newPos.m_123341_());
            tag.m_128405_("y", newPos.m_123342_());
            tag.m_128405_("z", newPos.m_123343_());
            if (newBlockEntity instanceof IMultiBlockEntityContainer && tag.m_128441_("LastKnownPos")) {
                tag.m_128365_("LastKnownPos", (Tag)NbtUtils.m_129224_((BlockPos)BlockPos.f_121853_.m_6625_(0x7FFFFFFE)));
            }
            newBlockEntity.m_142466_(tag);
            level.m_151523_(newBlockEntity);
        }
        BlockPos blockPos2 = contraption.anchor;
        Intrinsics.checkNotNullExpressionValue((Object)blockPos2, (String)"anchor");
        CreateInteractiveUtil.attemptTrainRelocation$create_interactive$default(this, level, blockPos2, blocks, shipCenter, null, 16, null);
        serverShip.setStatic(true);
        return serverShip.getId();
    }

    public static /* synthetic */ Long createShipForContraption$default(CreateInteractiveUtil createInteractiveUtil, ServerLevel serverLevel, Contraption contraption, BlockPos blockPos, Map map, int n, Object object) {
        if ((n & 8) != 0) {
            Map map2 = contraption.getBlocks();
            Intrinsics.checkNotNullExpressionValue((Object)map2, (String)"getBlocks(...)");
            map = map2;
        }
        return createInteractiveUtil.createShipForContraption(serverLevel, contraption, blockPos, map);
    }

    private final AABBdc createTrackAABB(ServerLevel level, BlockPos offsetPos, Map<BlockPos, StructureTemplate.StructureBlockInfo> localBlocks, Vector3ic shipCenter) {
        Vector3i minPosNotRelative = null;
        Vector3i maxPosNotRelative = null;
        Vector3i posAsJOML = new Vector3i();
        RandomSource random = RandomSource.m_216327_();
        for (Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry : localBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            StructureTemplate.StructureBlockInfo structureInfo = entry.getValue();
            if (!(structureInfo.f_74676_().m_60734_() instanceof ITrackBlock)) continue;
            BlockPos posInWorld = pos.m_121955_((Vec3i)offsetPos);
            BlockPos posInShip = pos.m_121955_((Vec3i)VectorConversionsMCKt.toBlockPos((Vector3ic)shipCenter));
            BlockState stateInWorld = level.m_8055_(posInShip);
            Block block = stateInWorld.m_60734_();
            if (block instanceof TrackBlock) {
                block.m_213897_(stateInWorld, level, posInShip, random);
            }
            Intrinsics.checkNotNull((Object)posInWorld);
            VectorConversionsMCKt.set((Vector3i)posAsJOML, (Vec3i)((Vec3i)posInWorld));
            if (minPosNotRelative == null) {
                minPosNotRelative = new Vector3i((Vector3ic)posAsJOML);
            } else {
                minPosNotRelative.min((Vector3ic)posAsJOML);
            }
            if (maxPosNotRelative == null) {
                maxPosNotRelative = new Vector3i((Vector3ic)posAsJOML);
                continue;
            }
            maxPosNotRelative.max((Vector3ic)posAsJOML);
        }
        if (minPosNotRelative == null || maxPosNotRelative == null) {
            return null;
        }
        return (AABBdc)AABBdUtilKt.expand((AABBd)new AABBd((double)minPosNotRelative.x(), (double)minPosNotRelative.y(), (double)minPosNotRelative.z(), (double)maxPosNotRelative.x() + 1.0, (double)maxPosNotRelative.y() + 1.0, (double)maxPosNotRelative.z() + 1.0), (double)1.0);
    }

    /*
     * WARNING - void declaration
     */
    public final void attemptTrainRelocation$create_interactive(@NotNull ServerLevel level, @NotNull BlockPos offsetPos, @NotNull Map<BlockPos, StructureTemplate.StructureBlockInfo> localBlocks, @NotNull Vector3ic shipCenter, @Nullable StructureTransform transform) {
        void $this$forEach$iv;
        void $this$filterTo$iv$iv;
        Intrinsics.checkNotNullParameter((Object)level, (String)"level");
        Intrinsics.checkNotNullParameter((Object)offsetPos, (String)"offsetPos");
        Intrinsics.checkNotNullParameter(localBlocks, (String)"localBlocks");
        Intrinsics.checkNotNullParameter((Object)shipCenter, (String)"shipCenter");
        if (localBlocks.isEmpty()) {
            return;
        }
        AABBdc aABBdc = this.createTrackAABB(level, offsetPos, localBlocks, shipCenter);
        if (aABBdc == null) {
            return;
        }
        AABBdc searchAABB = aABBdc;
        AABB searchAABBmc = VectorConversionsMCKt.toMinecraft((AABBdc)searchAABB);
        List trainCars = level.m_45976_(CarriageContraptionEntity.class, searchAABBmc);
        Intrinsics.checkNotNull((Object)trainCars);
        Iterable $this$filter$iv = trainCars;
        boolean $i$f$filter = false;
        Iterable iterable = $this$filter$iv;
        Collection destination$iv$iv = new ArrayList();
        boolean $i$f$filterTo = false;
        for (Object element$iv$iv : $this$filterTo$iv$iv) {
            CarriageContraptionEntity it = (CarriageContraptionEntity)element$iv$iv;
            boolean bl = false;
            if (!(!it.getCarriage().train.derailed && it.carriageIndex == it.getCarriage().train.carriages.size() - 1 && it.m_20191_().m_82381_(searchAABBmc))) continue;
            destination$iv$iv.add(element$iv$iv);
        }
        $this$filter$iv = (List)destination$iv$iv;
        boolean $i$f$forEach = false;
        for (Object element$iv : $this$forEach$iv) {
            Vector3d it;
            BlockPos blockPos;
            Vec3 vec3;
            TrackNodeLocation trackNodeLocation;
            TrackNodeLocation bl;
            Object element$iv$iv;
            CarriageContraptionEntity carriageEntity = (CarriageContraptionEntity)element$iv;
            boolean bl2 = false;
            Object object = element$iv$iv = carriageEntity.getCarriage().getLeadingPoint();
            if (object == null) continue;
            Intrinsics.checkNotNull(object);
            Object leadingPoint = element$iv$iv;
            TrackNode trackNode = ((TravellingPoint)leadingPoint).node1;
            if (trackNode == null || (trackNode = (bl = trackNode.getLocation())) == null) continue;
            Intrinsics.checkNotNull((Object)trackNode);
            trackNode = MixinTrainLogic.INSTANCE.getLocationVec3i$create_interactive(bl);
            if (trackNode == null) {
                continue;
            }
            Vector3ic node1Location = (Vector3ic)trackNode;
            TrackNode trackNode2 = ((TravellingPoint)leadingPoint).node2;
            if (trackNode2 == null || (trackNode2 = (trackNodeLocation = trackNode2.getLocation())) == null) continue;
            Intrinsics.checkNotNull((Object)trackNode2);
            trackNode2 = MixinTrainLogic.INSTANCE.getLocationVec3i$create_interactive(trackNodeLocation);
            if (trackNode2 == null) {
                continue;
            }
            Vector3ic node2Location = (Vector3ic)trackNode2;
            Vector3d vector3d = new Vector3d((Vector3ic)node1Location.sub(node2Location, new Vector3i())).mul(-1.0).normalize();
            Intrinsics.checkNotNullExpressionValue((Object)vector3d, (String)"normalize(...)");
            Vector3dc normalLocal = (Vector3dc)vector3d;
            if (transform == null) {
                Vector3d vector3d2 = new Vector3d((Vector3ic)node1Location.sub(node2Location, new Vector3i())).mul(-1.0).normalize();
                Intrinsics.checkNotNullExpressionValue((Object)vector3d2, (String)"normalize(...)");
                vec3 = VectorConversionsMCKt.toMinecraft((Vector3dc)((Vector3dc)vector3d2));
            } else {
                Vector3d diff = new Vector3d((Vector3ic)node1Location.sub(node2Location, new Vector3i()));
                Vector3d vector3d3 = diff.mul(-1.0);
                Intrinsics.checkNotNullExpressionValue((Object)vector3d3, (String)"mul(...)");
                Vec3 vec32 = transform.applyWithoutOffsetUncentered(VectorConversionsMCKt.toMinecraft((Vector3dc)((Vector3dc)vector3d3))).m_82541_();
                Intrinsics.checkNotNull((Object)vec32);
                vec3 = vec32;
            }
            Vec3 normal = vec3;
            CarriageBogey bogey = carriageEntity.getCarriage().trailingBogey();
            Intrinsics.checkNotNull((Object)bogey, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixin.CarriageBogeyAccessor");
            if (((CarriageBogeyAccessor)bogey).getIsLeading()) {
                blockPos = BlockPos.f_121853_;
            } else {
                it = new Vector3d(normalLocal).mul(-((double)carriageEntity.getCarriage().bogeySpacing));
                boolean bl3 = false;
                blockPos = new BlockPos(MathKt.roundToInt((double)it.x), MathKt.roundToInt((double)it.y), MathKt.roundToInt((double)it.z));
            }
            BlockPos bogeyRelPos = blockPos;
            Vec3 vec33 = carriageEntity.getAnchorVec();
            Intrinsics.checkNotNullExpressionValue((Object)vec33, (String)"getAnchorVec(...)");
            it = VectorConversionsMCKt.toJOML((Vec3)vec33).add((double)bogeyRelPos.m_123341_(), (double)bogeyRelPos.m_123342_(), (double)bogeyRelPos.m_123343_()).sub(0.0, 1.0, 0.0);
            Intrinsics.checkNotNullExpressionValue((Object)it, (String)"sub(...)");
            Vector3dc leadingBogeyPosInLocal = (Vector3dc)it;
            BlockPos closestBlockPosRelative = new BlockPos(MathKt.roundToInt((double)leadingBogeyPosInLocal.x()), MathKt.roundToInt((double)leadingBogeyPosInLocal.y()), MathKt.roundToInt((double)leadingBogeyPosInLocal.z())).m_121996_((Vec3i)offsetPos);
            boolean success = false;
            StructureTemplate.StructureBlockInfo structureBlockInfo = localBlocks.get(closestBlockPosRelative);
            if ((structureBlockInfo != null && (structureBlockInfo = structureBlockInfo.f_74676_()) != null ? structureBlockInfo.m_60734_() : null) instanceof ITrackBlock) {
                BlockPos relocatePos;
                BlockPos defaultOne = closestBlockPosRelative.m_7918_(shipCenter.x(), shipCenter.y(), shipCenter.z());
                StructureTransform structureTransform = transform;
                Object withTransformPos = structureTransform != null ? structureTransform.apply(closestBlockPosRelative) : null;
                BlockPos blockPos2 = withTransformPos;
                if (blockPos2 == null) {
                    blockPos2 = defaultOne;
                }
                if (success = TrainRelocator.relocate((Train)carriageEntity.getCarriage().train, (Level)((Level)level), (BlockPos)(relocatePos = blockPos2.m_7918_(-MathKt.roundToInt((double)normal.m_7096_()), -MathKt.roundToInt((double)normal.m_7098_()), -MathKt.roundToInt((double)normal.m_7094_()))), null, (boolean)false, (Vec3)normal, (boolean)false)) {
                    carriageEntity.m_20219_(carriageEntity.getCarriage().getDimensional((Level)((Level)level)).positionAnchor);
                }
            }
            if (success) continue;
            Train train = carriageEntity.getCarriage().train;
            Intrinsics.checkNotNull((Object)train, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixin.TrainAccessor");
            ((TrainAccessor)train).getMigratingPoints().clear();
            train.navigation.cancelNavigation();
            ((TrainAccessor)train).setGraph(null);
            ((TrainAccessor)train).setDerailed(true);
            train.status.highStress();
        }
    }

    public static /* synthetic */ void attemptTrainRelocation$create_interactive$default(CreateInteractiveUtil createInteractiveUtil, ServerLevel serverLevel, BlockPos blockPos, Map map, Vector3ic vector3ic, StructureTransform structureTransform, int n, Object object) {
        if ((n & 0x10) != 0) {
            structureTransform = null;
        }
        createInteractiveUtil.attemptTrainRelocation$create_interactive(serverLevel, blockPos, map, vector3ic, structureTransform);
    }

    public final boolean doesContraptionHaveShipLoaded(@NotNull Contraption contraption) {
        Intrinsics.checkNotNullParameter((Object)contraption, (String)"contraption");
        AbstractContraptionEntity abstractContraptionEntity = contraption.entity;
        if (abstractContraptionEntity == null) {
            return false;
        }
        AbstractContraptionEntity contraptionEntity = abstractContraptionEntity;
        Long l = ((AbstractContraptionEntityDuck)contraptionEntity).ci$getShadowShipId();
        if (l == null) {
            return false;
        }
        long shipId = ((Number)l).longValue();
        return VSGameUtilsKt.getShipObjectWorld((Level)contraptionEntity.m_9236_()).getLoadedShips().getById(shipId) != null;
    }

    private final ShipTransform posRotToShipTransform(ContraptionPosRot contraptionPosRot, ServerShip serverShip, ServerLevel level) {
        Vector3dc contraptionPos = contraptionPosRot.component1();
        Quaterniondc contraptionRot = contraptionPosRot.component2();
        Vector3dc cmInShip = serverShip.getInertiaData().getCenterOfMassInShip();
        Vector3ic shipCenter = this.getChunkClaimCenterPos((Ship)serverShip, (Level)level);
        Vector3d offset = cmInShip.sub((double)shipCenter.x(), (double)shipCenter.y(), (double)shipCenter.z(), new Vector3d());
        contraptionRot.transform(offset);
        Vector3d vector3d = contraptionPos.add((Vector3dc)offset, new Vector3d());
        Intrinsics.checkNotNullExpressionValue((Object)vector3d, (String)"add(...)");
        Vector3dc newPos = (Vector3dc)vector3d;
        double newScale = contraptionPosRot.getScale();
        Vector3d vector3d2 = cmInShip.add(0.5, 0.5, 0.5, new Vector3d());
        Intrinsics.checkNotNullExpressionValue((Object)vector3d2, (String)"add(...)");
        Vector3dc posInShip = (Vector3dc)vector3d2;
        return (ShipTransform)new ShipTransformImpl(newPos, posInShip, contraptionPosRot.getRot(), (Vector3dc)new Vector3d(newScale));
    }

    public final void teleportShipToPosRot(@NotNull ContraptionPosRot contraptionPosRot, @NotNull ServerShip serverShip, @NotNull ServerLevel level) {
        Intrinsics.checkNotNullParameter((Object)contraptionPosRot, (String)"contraptionPosRot");
        Intrinsics.checkNotNullParameter((Object)serverShip, (String)"serverShip");
        Intrinsics.checkNotNullParameter((Object)level, (String)"level");
        ShipTransform shipTransform = this.posRotToShipTransform(contraptionPosRot, serverShip, level);
        Vector3dc newVel = (Vector3dc)new Vector3d();
        Vector3dc newOmega = (Vector3dc)new Vector3d();
        String newDimension = VSGameUtilsKt.getDimensionId((Level)((Level)level));
        ShipTeleportData shipTeleportData = new ShipTeleportDataImplFixed(shipTransform.getPositionInWorld(), shipTransform.getPositionInShip(), shipTransform.getShipToWorldRotation(), newVel, newOmega, newDimension, shipTransform.getShipToWorldScaling().x());
        VSGameUtilsKt.getShipObjectWorld((ServerLevel)level).teleportShip(serverShip, shipTeleportData);
    }

    @NotNull
    public final ShipTransform updateShipShadow(@NotNull AbstractContraptionEntity entity, @NotNull ServerShip serverShip, @NotNull ContraptionPosRot posRot) {
        Intrinsics.checkNotNullParameter((Object)entity, (String)"entity");
        Intrinsics.checkNotNullParameter((Object)serverShip, (String)"serverShip");
        Intrinsics.checkNotNullParameter((Object)posRot, (String)"posRot");
        Level level = entity.m_9236_();
        Intrinsics.checkNotNull((Object)level, (String)"null cannot be cast to non-null type net.minecraft.server.level.ServerLevel");
        ShipTransform transform = this.posRotToShipTransform(posRot, serverShip, (ServerLevel)level);
        serverShip.setTransformProvider(new ServerShipTransformProvider(entity, transform){
            final /* synthetic */ AbstractContraptionEntity $entity;
            final /* synthetic */ ShipTransform $transform;
            {
                this.$entity = $entity;
                this.$transform = $transform;
            }

            @NoOptimize
            @Nullable
            public ServerShipTransformProvider.NextTransformAndVelocityData provideNextTransformAndVelocity(@NotNull ShipTransform prevShipTransform, @NotNull ShipTransform shipTransform) {
                Vector3d vector3d;
                Intrinsics.checkNotNullParameter((Object)prevShipTransform, (String)"prevShipTransform");
                Intrinsics.checkNotNullParameter((Object)shipTransform, (String)"shipTransform");
                if (this.$entity instanceof CarriageContraptionEntity && CreateInteractiveUtil.INSTANCE.isTrainDerailed((CarriageContraptionEntity)this.$entity)) {
                    return null;
                }
                Vector3d prevPos = prevShipTransform.getShipToWorld().transformPosition(this.$transform.getPositionInShip(), new Vector3d());
                Vector3d vector3d2 = this.$transform.getPositionInWorld().sub((Vector3dc)prevPos, new Vector3d()).mul(20.0);
                Intrinsics.checkNotNullExpressionValue((Object)vector3d2, (String)"mul(...)");
                Vector3dc velocityAtContraptionPos = (Vector3dc)vector3d2;
                Quaterniond quaterniond = this.$transform.getShipToWorldRotation().difference(prevShipTransform.getShipToWorldRotation(), new Quaterniond()).normalize();
                Intrinsics.checkNotNullExpressionValue((Object)quaterniond, (String)"normalize(...)");
                Quaterniondc rotDiff = (Quaterniondc)quaterniond;
                Vector3d $this$provideNextTransformAndVelocity_u24lambda_u240 = vector3d = new Vector3d(rotDiff.x() * 2.0, rotDiff.y() * 2.0, rotDiff.z() * 2.0);
                boolean bl = false;
                if (rotDiff.w() > 0.0) {
                    $this$provideNextTransformAndVelocity_u24lambda_u240.mul(-1.0);
                }
                Vector3d vector3d3 = vector3d.mul(20.0);
                Intrinsics.checkNotNullExpressionValue((Object)vector3d3, (String)"mul(...)");
                Vector3dc omega = (Vector3dc)vector3d3;
                return new ServerShipTransformProvider.NextTransformAndVelocityData(this.$transform, velocityAtContraptionPos, omega);
            }
        });
        Level level2 = entity.m_9236_();
        Intrinsics.checkNotNullExpressionValue((Object)level2, (String)"level(...)");
        if (!Intrinsics.areEqual((Object)VSGameUtilsKt.getDimensionId((Level)level2), (Object)serverShip.getChunkClaimDimension())) {
            Level level3 = entity.m_9236_();
            Intrinsics.checkNotNull((Object)level3, (String)"null cannot be cast to non-null type net.minecraft.server.level.ServerLevel");
            this.teleportShipToPosRot(posRot, serverShip, (ServerLevel)level3);
        }
        serverShip.setStatic(true);
        serverShip.setEnableKinematicVelocity(true);
        ShipSettingsKt.getSettings((ServerShip)serverShip).setChangeDimensionOnTouchPortals(false);
        return transform;
    }

    public final void moveContraptionToTransform(@NotNull CarriageContraptionEntity entity, @NotNull Ship ship) {
        Intrinsics.checkNotNullParameter((Object)entity, (String)"entity");
        Intrinsics.checkNotNullParameter((Object)ship, (String)"ship");
        ShipTransform shipTransform = ship.getTransform();
        CreateInteractiveContraptionRotationState rotState = new CreateInteractiveContraptionRotationState(shipTransform.getShipToWorldRotation());
        ((OrientedContraptionEntityDuck)entity).ci$setForcedRotation(rotState);
        Level level = entity.m_9236_();
        Intrinsics.checkNotNullExpressionValue((Object)level, (String)"level(...)");
        Vector3ic shipCenter = this.getChunkClaimCenterPos(ship, level);
        Vector3d vector3d = shipTransform.getShipToWorld().transformPosition(new Vector3d(shipCenter).add(0.5, 0.5, 0.5));
        Intrinsics.checkNotNullExpressionValue((Object)vector3d, (String)"transformPosition(...)");
        Vector3dc newPos = (Vector3dc)vector3d;
        entity.m_6034_(newPos.x(), newPos.y() - 0.5, newPos.z());
        Carriage.DimensionalCarriageEntity dimensionalCarriageEntity = entity.getCarriage().getDimensional(entity.m_9236_());
        Intrinsics.checkNotNull((Object)dimensionalCarriageEntity, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixin.DimensionalCarriageEntityAccessor");
        ((DimensionalCarriageEntityAccessor)dimensionalCarriageEntity).setPositionAnchor(new Vec3(newPos.x(), newPos.y() - 0.5, newPos.z()));
        AABB box = entity.getContraption().bounds;
        if (box != null) {
            AABBd aABBd = VectorConversionsMCKt.toJOML((AABB)box).translate((double)shipCenter.x() + 0.5, (double)shipCenter.y() + 0.5, (double)shipCenter.z() + 0.5);
            Intrinsics.checkNotNullExpressionValue((Object)aABBd, (String)"translate(...)");
            AABBdc boxInLocal = (AABBdc)aABBd;
            AABBd aABBd2 = boxInLocal.transform(shipTransform.getShipToWorld(), new AABBd());
            Intrinsics.checkNotNullExpressionValue((Object)aABBd2, (String)"transform(...)");
            AABBdc boxInGlobal = (AABBdc)aABBd2;
            entity.m_20011_(VectorConversionsMCKt.toMinecraft((AABBdc)boxInGlobal));
        }
    }

    @NotNull
    public final ContraptionPosRot getContraptionPosRot(@NotNull AbstractContraptionEntity entity) {
        Intrinsics.checkNotNullParameter((Object)entity, (String)"entity");
        AbstractContraptionEntity.ContraptionRotationState rotationStateOriginal = ((AbstractContraptionEntity)AbstractContraptionEntity.class.cast(entity)).getRotationState();
        Intrinsics.checkNotNull((Object)rotationStateOriginal, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionRotationStateDuck");
        Quaterniond newRot = ((ContraptionRotationStateDuck)rotationStateOriginal).ci$getRotationQuaternion(new Quaterniond());
        Vec3 vec3 = entity.getAnchorVec();
        Intrinsics.checkNotNullExpressionValue((Object)vec3, (String)"getAnchorVec(...)");
        Vector3d vector3d = VectorConversionsMCKt.toJOML((Vec3)vec3).add(0.5, 0.5, 0.5);
        Intrinsics.checkNotNullExpressionValue((Object)vector3d, (String)"add(...)");
        Vector3dc contraptionPos = (Vector3dc)vector3d;
        Level level = entity.m_9236_();
        Vec3 vec32 = entity.m_20182_();
        Intrinsics.checkNotNullExpressionValue((Object)vec32, (String)"position(...)");
        Ship parentShip = VSGameUtilsKt.getShipManagingPos((Level)level, (Position)((Position)vec32));
        if (parentShip != null) {
            Vector3d newNewPos = parentShip.getTransform().getShipToWorld().transformPosition(contraptionPos, new Vector3d());
            Quaterniond newNewRot = parentShip.getTransform().getShipToWorldRotation().mul((Quaterniondc)newRot, new Quaterniond());
            Intrinsics.checkNotNull((Object)newNewPos);
            Vector3dc vector3dc = (Vector3dc)newNewPos;
            Intrinsics.checkNotNull((Object)newNewRot);
            return new ContraptionPosRot(vector3dc, (Quaterniondc)newNewRot, parentShip.getTransform().getShipToWorldScaling().x());
        }
        Intrinsics.checkNotNull((Object)newRot);
        return new ContraptionPosRot(contraptionPos, (Quaterniondc)newRot, 1.0);
    }

    @NotNull
    public final ContraptionPosRot getContraptionPosRotForRender(@NotNull AbstractContraptionEntity entity, double partialTick) {
        Intrinsics.checkNotNullParameter((Object)entity, (String)"entity");
        AbstractContraptionEntity.ContraptionRotationState contraptionRotationState = ((AbstractContraptionEntityDuck)entity).ci$getPrevTickRotationState();
        Intrinsics.checkNotNull((Object)contraptionRotationState, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionRotationStateDuck");
        Quaterniond quaterniond = ((ContraptionRotationStateDuck)contraptionRotationState).ci$getRotationQuaternion(new Quaterniond());
        Intrinsics.checkNotNullExpressionValue((Object)quaterniond, (String)"ci$getRotationQuaternion(...)");
        Quaterniondc prevRot = (Quaterniondc)quaterniond;
        AbstractContraptionEntity.ContraptionRotationState contraptionRotationState2 = ((AbstractContraptionEntity)AbstractContraptionEntity.class.cast(entity)).getRotationState();
        Intrinsics.checkNotNull((Object)contraptionRotationState2, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionRotationStateDuck");
        Quaterniond quaterniond2 = ((ContraptionRotationStateDuck)contraptionRotationState2).ci$getRotationQuaternion(new Quaterniond());
        Intrinsics.checkNotNullExpressionValue((Object)quaterniond2, (String)"ci$getRotationQuaternion(...)");
        Quaterniondc curRot = (Quaterniondc)quaterniond2;
        Quaterniond newRot = prevRot.slerp(curRot, partialTick, new Quaterniond()).normalize();
        Vector3d vector3d = new Vector3d(entity.getAnchorVec().f_82479_ * partialTick + entity.getPrevAnchorVec().f_82479_ * (1.0 - partialTick), entity.getAnchorVec().f_82480_ * partialTick + entity.getPrevAnchorVec().f_82480_ * (1.0 - partialTick), entity.getAnchorVec().f_82481_ * partialTick + entity.getPrevAnchorVec().f_82481_ * (1.0 - partialTick)).add(0.5, 0.5, 0.5);
        Intrinsics.checkNotNullExpressionValue((Object)vector3d, (String)"add(...)");
        Vector3dc contraptionPos = (Vector3dc)vector3d;
        Level level = entity.m_9236_();
        Vec3 vec3 = entity.m_20182_();
        Intrinsics.checkNotNullExpressionValue((Object)vec3, (String)"position(...)");
        ClientShip parentShip = (ClientShip)VSGameUtilsKt.getShipManagingPos((Level)level, (Position)((Position)vec3));
        if (parentShip != null) {
            Vector3d newNewPos = parentShip.getRenderTransform().getShipToWorld().transformPosition(contraptionPos, new Vector3d());
            Quaterniond newNewRot = parentShip.getRenderTransform().getShipToWorldRotation().mul((Quaterniondc)newRot, new Quaterniond()).normalize();
            Intrinsics.checkNotNull((Object)newNewPos);
            Vector3dc vector3dc = (Vector3dc)newNewPos;
            Intrinsics.checkNotNull((Object)newNewRot);
            return new ContraptionPosRot(vector3dc, (Quaterniondc)newNewRot, parentShip.getRenderTransform().getShipToWorldScaling().x());
        }
        Intrinsics.checkNotNull((Object)newRot);
        return new ContraptionPosRot(contraptionPos, (Quaterniondc)newRot, 1.0);
    }

    @NotNull
    public final ContraptionPosRot getContraptionPosRot(@NotNull AbstractContraptionEntity entity, @Nullable ShipTransform parentTransform) {
        Intrinsics.checkNotNullParameter((Object)entity, (String)"entity");
        AbstractContraptionEntity.ContraptionRotationState rotationStateOriginal = ((AbstractContraptionEntity)AbstractContraptionEntity.class.cast(entity)).getRotationState();
        Intrinsics.checkNotNull((Object)rotationStateOriginal, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionRotationStateDuck");
        Quaterniond newRot = ((ContraptionRotationStateDuck)rotationStateOriginal).ci$getRotationQuaternion(new Quaterniond()).normalize();
        if (parentTransform != null) {
            Matrix4dc matrix4dc = parentTransform.getShipToWorld();
            Vec3 vec3 = entity.getAnchorVec();
            Intrinsics.checkNotNullExpressionValue((Object)vec3, (String)"getAnchorVec(...)");
            Vector3d newNewPos = matrix4dc.transformPosition((Vector3dc)VectorConversionsMCKt.toJOML((Vec3)vec3).add(0.5, 0.5, 0.5), new Vector3d());
            Quaterniond newNewRot = parentTransform.getShipToWorldRotation().mul((Quaterniondc)newRot, new Quaterniond()).normalize();
            Intrinsics.checkNotNull((Object)newNewPos);
            Vector3dc vector3dc = (Vector3dc)newNewPos;
            Intrinsics.checkNotNull((Object)newNewRot);
            return new ContraptionPosRot(vector3dc, (Quaterniondc)newNewRot, parentTransform.getShipToWorldScaling().x());
        }
        Vec3 vec3 = entity.getAnchorVec();
        Intrinsics.checkNotNullExpressionValue((Object)vec3, (String)"getAnchorVec(...)");
        Vector3d vector3d = VectorConversionsMCKt.toJOML((Vec3)vec3).add(0.5, 0.5, 0.5);
        Intrinsics.checkNotNullExpressionValue((Object)vector3d, (String)"add(...)");
        Vector3dc vector3dc = (Vector3dc)vector3d;
        Intrinsics.checkNotNull((Object)newRot);
        return new ContraptionPosRot(vector3dc, (Quaterniondc)newRot, 1.0);
    }

    @Nullable
    public final Ship getShipForMovementContext(@NotNull MovementContext context) {
        Intrinsics.checkNotNullParameter((Object)context, (String)"context");
        Contraption contraption = context.contraption;
        Intrinsics.checkNotNullExpressionValue((Object)contraption, (String)"contraption");
        return this.getShipForContraption$create_interactive(contraption);
    }

    @Nullable
    public final Ship getShipForContraption$create_interactive(@NotNull Contraption contraption) {
        Intrinsics.checkNotNullParameter((Object)contraption, (String)"contraption");
        AbstractContraptionEntity abstractContraptionEntity = contraption.entity;
        if (abstractContraptionEntity == null) {
            return null;
        }
        AbstractContraptionEntity contraptionEntity = abstractContraptionEntity;
        Long l = ((AbstractContraptionEntityDuck)contraptionEntity).ci$getShadowShipId();
        if (l == null) {
            return null;
        }
        long shadowShipId = ((Number)l).longValue();
        return VSGameUtilsKt.getShipObjectWorld((Level)contraptionEntity.m_9236_()).getAllShips().getById(shadowShipId);
    }

    @Nullable
    public final Pair<StructureTemplate.StructureBlockInfo, MovementContext> getActorAtPos(@NotNull Level level, @NotNull BlockPos pos) {
        Intrinsics.checkNotNullParameter((Object)level, (String)"level");
        Intrinsics.checkNotNullParameter((Object)pos, (String)"pos");
        Ship ship = VSGameUtilsKt.getShipManagingPos((Level)level, (BlockPos)pos);
        if (ship == null) {
            return null;
        }
        Ship ship2 = ship;
        WeakReference<AbstractContraptionEntity> weakReference = this.getShipIdToContraptionEntityClient().get(ship2.getId());
        if (weakReference == null) {
            return null;
        }
        WeakReference<AbstractContraptionEntity> contraptionEntityWeakReference = weakReference;
        AbstractContraptionEntity abstractContraptionEntity = (AbstractContraptionEntity)contraptionEntityWeakReference.get();
        if (abstractContraptionEntity == null) {
            return null;
        }
        AbstractContraptionEntity contraptionEntity = abstractContraptionEntity;
        Vector3ic shipCenter = this.getChunkClaimCenterPos(ship2, level);
        BlockPos relativePos = pos.m_121996_((Vec3i)VectorConversionsMCKt.toBlockPos((Vector3ic)shipCenter));
        Contraption contraption = contraptionEntity.getContraption();
        Intrinsics.checkNotNull((Object)contraption, (String)"null cannot be cast to non-null type org.valkyrienskies.create_interactive.mixinducks.ContraptionDuck");
        ContraptionDuck contraptionDuck = (ContraptionDuck)contraption;
        Intrinsics.checkNotNull((Object)relativePos);
        return contraptionDuck.ci$getActorAtPos(relativePos);
    }

    @NotNull
    public final Vector3ic getChunkClaimCenterPos(@NotNull Ship $this$getChunkClaimCenterPos, @NotNull Level level) {
        Intrinsics.checkNotNullParameter((Object)$this$getChunkClaimCenterPos, (String)"<this>");
        Intrinsics.checkNotNullParameter((Object)level, (String)"level");
        return (Vector3ic)$this$getChunkClaimCenterPos.getChunkClaim().getCenterBlockCoordinates(VSGameUtilsKt.getYRange((Level)level), new Vector3i());
    }

    @NotNull
    public final Map<Long, WeakReference<AbstractContraptionEntity>> getShipIdToContraptionEntityClient() {
        return shipIdToContraptionEntityClientInternal;
    }

    @NotNull
    public final Map<Long, WeakReference<AbstractContraptionEntity>> getShipIdToContraptionEntityServer() {
        return shipIdToContraptionEntityServerInternal;
    }

    @Nullable
    public final AbstractContraptionEntity getContraptionEntityForShip(long shipId, boolean clientSide) {
        Object object;
        if (clientSide) {
            WeakReference<AbstractContraptionEntity> weakReference = this.getShipIdToContraptionEntityClient().get(shipId);
            object = weakReference != null ? (AbstractContraptionEntity)weakReference.get() : null;
        } else {
            WeakReference<AbstractContraptionEntity> weakReference = this.getShipIdToContraptionEntityServer().get(shipId);
            object = weakReference != null ? (AbstractContraptionEntity)weakReference.get() : null;
        }
        return object;
    }

    public final void linkShipToContraption(long shipId, @NotNull AbstractContraptionEntity contraptionEntity) {
        Intrinsics.checkNotNullParameter((Object)contraptionEntity, (String)"contraptionEntity");
        if (contraptionEntity.m_9236_().f_46443_) {
            Long l = shipId;
            shipIdToContraptionEntityClientInternal.put(l, new WeakReference<AbstractContraptionEntity>(contraptionEntity));
        } else {
            Long l = shipId;
            shipIdToContraptionEntityServerInternal.put(l, new WeakReference<AbstractContraptionEntity>(contraptionEntity));
        }
    }

    public final void unlinkShipToContraption(long shipId, @NotNull AbstractContraptionEntity contraptionEntity) {
        Intrinsics.checkNotNullParameter((Object)contraptionEntity, (String)"contraptionEntity");
        if (contraptionEntity.m_9236_().f_46443_) {
            AbstractContraptionEntity prevVal;
            WeakReference<AbstractContraptionEntity> weakReference = shipIdToContraptionEntityClientInternal.get(shipId);
            Object object = prevVal = weakReference != null ? (AbstractContraptionEntity)weakReference.get() : null;
            if (prevVal != null && Intrinsics.areEqual((Object)prevVal, (Object)contraptionEntity)) {
                shipIdToContraptionEntityClientInternal.remove(shipId);
            }
        } else {
            AbstractContraptionEntity prevVal;
            WeakReference<AbstractContraptionEntity> weakReference = shipIdToContraptionEntityServerInternal.get(shipId);
            Object object = prevVal = weakReference != null ? (AbstractContraptionEntity)weakReference.get() : null;
            if (prevVal != null && Intrinsics.areEqual((Object)prevVal, (Object)contraptionEntity)) {
                shipIdToContraptionEntityServerInternal.remove(shipId);
            }
        }
    }

    public final void onShipUnloadEventClient$create_interactive(@NotNull ClientShip clientShip) {
        Intrinsics.checkNotNullParameter((Object)clientShip, (String)"clientShip");
        shipIdToContraptionEntityClientInternal.remove(clientShip.getId());
    }

    @Nullable
    public final BlockEntity getBlockEntity(@NotNull MovementContext context) {
        Intrinsics.checkNotNullParameter((Object)context, (String)"context");
        Ship ship = this.getShipForMovementContext(context);
        if (ship == null) {
            return null;
        }
        Ship ship2 = ship;
        Level level = context.world;
        Intrinsics.checkNotNullExpressionValue((Object)level, (String)"world");
        Vector3ic shipCenter = this.getChunkClaimCenterPos(ship2, level);
        BlockPos blockPos = context.localPos.m_7918_(shipCenter.x(), shipCenter.y(), shipCenter.z());
        return context.world.m_7702_(blockPos);
    }

    public final boolean isTrainDerailed(@NotNull CarriageContraptionEntity carriageEntity) {
        Intrinsics.checkNotNullParameter((Object)carriageEntity, (String)"carriageEntity");
        Carriage carriage = carriageEntity.getCarriage();
        return carriage != null && (carriage = carriage.train) != null ? carriage.derailed : false;
    }

    public static final class ContraptionPosRot {
        @NotNull
        private final Vector3dc pos;
        @NotNull
        private final Quaterniondc rot;
        private final double scale;

        public ContraptionPosRot(@NotNull Vector3dc pos, @NotNull Quaterniondc rot, double scale) {
            Intrinsics.checkNotNullParameter((Object)pos, (String)"pos");
            Intrinsics.checkNotNullParameter((Object)rot, (String)"rot");
            this.pos = pos;
            this.rot = rot;
            this.scale = scale;
        }

        @NotNull
        public final Vector3dc getPos() {
            return this.pos;
        }

        @NotNull
        public final Quaterniondc getRot() {
            return this.rot;
        }

        public final double getScale() {
            return this.scale;
        }

        @NotNull
        public final Vector3dc component1() {
            return this.pos;
        }

        @NotNull
        public final Quaterniondc component2() {
            return this.rot;
        }

        public final double component3() {
            return this.scale;
        }

        @NotNull
        public final ContraptionPosRot copy(@NotNull Vector3dc pos, @NotNull Quaterniondc rot, double scale) {
            Intrinsics.checkNotNullParameter((Object)pos, (String)"pos");
            Intrinsics.checkNotNullParameter((Object)rot, (String)"rot");
            return new ContraptionPosRot(pos, rot, scale);
        }

        public static /* synthetic */ ContraptionPosRot copy$default(ContraptionPosRot contraptionPosRot, Vector3dc vector3dc, Quaterniondc quaterniondc, double d, int n, Object object) {
            if ((n & 1) != 0) {
                vector3dc = contraptionPosRot.pos;
            }
            if ((n & 2) != 0) {
                quaterniondc = contraptionPosRot.rot;
            }
            if ((n & 4) != 0) {
                d = contraptionPosRot.scale;
            }
            return contraptionPosRot.copy(vector3dc, quaterniondc, d);
        }

        @NotNull
        public String toString() {
            return "ContraptionPosRot(pos=" + this.pos + ", rot=" + this.rot + ", scale=" + this.scale + ")";
        }

        public int hashCode() {
            int result = this.pos.hashCode();
            result = result * 31 + this.rot.hashCode();
            result = result * 31 + Double.hashCode(this.scale);
            return result;
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ContraptionPosRot)) {
                return false;
            }
            ContraptionPosRot contraptionPosRot = (ContraptionPosRot)other;
            if (!Intrinsics.areEqual((Object)this.pos, (Object)contraptionPosRot.pos)) {
                return false;
            }
            if (!Intrinsics.areEqual((Object)this.rot, (Object)contraptionPosRot.rot)) {
                return false;
            }
            return Double.compare(this.scale, contraptionPosRot.scale) == 0;
        }
    }

    public static final class ShipTeleportDataImplFixed
    implements ShipTeleportData {
        @NotNull
        private final Vector3dc newPos;
        @NotNull
        private final Vector3dc newPosInShip;
        @NotNull
        private final Quaterniondc newRot;
        @NotNull
        private final Vector3dc newVel;
        @NotNull
        private final Vector3dc newOmega;
        @Nullable
        private final String newDimension;
        @Nullable
        private final Double newScale;

        public ShipTeleportDataImplFixed(@NotNull Vector3dc newPos, @NotNull Vector3dc newPosInShip, @NotNull Quaterniondc newRot, @NotNull Vector3dc newVel, @NotNull Vector3dc newOmega, @Nullable String newDimension, @Nullable Double newScale) {
            Intrinsics.checkNotNullParameter((Object)newPos, (String)"newPos");
            Intrinsics.checkNotNullParameter((Object)newPosInShip, (String)"newPosInShip");
            Intrinsics.checkNotNullParameter((Object)newRot, (String)"newRot");
            Intrinsics.checkNotNullParameter((Object)newVel, (String)"newVel");
            Intrinsics.checkNotNullParameter((Object)newOmega, (String)"newOmega");
            this.newPos = newPos;
            this.newPosInShip = newPosInShip;
            this.newRot = newRot;
            this.newVel = newVel;
            this.newOmega = newOmega;
            this.newDimension = newDimension;
            this.newScale = newScale;
        }

        public /* synthetic */ ShipTeleportDataImplFixed(Vector3dc vector3dc, Vector3dc vector3dc2, Quaterniondc quaterniondc, Vector3dc vector3dc3, Vector3dc vector3dc4, String string, Double d, int n, DefaultConstructorMarker defaultConstructorMarker) {
            if ((n & 1) != 0) {
                vector3dc = (Vector3dc)new Vector3d();
            }
            if ((n & 2) != 0) {
                vector3dc2 = (Vector3dc)new Vector3d();
            }
            if ((n & 4) != 0) {
                quaterniondc = (Quaterniondc)new Quaterniond();
            }
            if ((n & 8) != 0) {
                vector3dc3 = (Vector3dc)new Vector3d();
            }
            if ((n & 0x10) != 0) {
                vector3dc4 = (Vector3dc)new Vector3d();
            }
            if ((n & 0x20) != 0) {
                string = null;
            }
            if ((n & 0x40) != 0) {
                d = null;
            }
            this(vector3dc, vector3dc2, quaterniondc, vector3dc3, vector3dc4, string, d);
        }

        @NotNull
        public Vector3dc getNewPos() {
            return this.newPos;
        }

        @NotNull
        public final Vector3dc getNewPosInShip() {
            return this.newPosInShip;
        }

        @NotNull
        public Quaterniondc getNewRot() {
            return this.newRot;
        }

        @NotNull
        public Vector3dc getNewVel() {
            return this.newVel;
        }

        @NotNull
        public Vector3dc getNewOmega() {
            return this.newOmega;
        }

        @Nullable
        public String getNewDimension() {
            return this.newDimension;
        }

        @Nullable
        public Double getNewScale() {
            return this.newScale;
        }

        /*
         * WARNING - void declaration
         */
        @NoOptimize
        @NotNull
        public ShipTransform createNewShipTransform(@NotNull ShipTransform oldShipTransform) {
            Vector3dc vector3dc;
            Intrinsics.checkNotNullParameter((Object)oldShipTransform, (String)"oldShipTransform");
            Vector3dc vector3dc2 = this.getNewPos();
            Vector3dc vector3dc3 = this.newPosInShip;
            Quaterniondc quaterniondc = this.getNewRot();
            Double d = this.getNewScale();
            if (d != null) {
                void it;
                double d2 = ((Number)d).doubleValue();
                Quaterniondc quaterniondc2 = quaterniondc;
                Vector3dc vector3dc4 = vector3dc3;
                Vector3dc vector3dc5 = vector3dc2;
                boolean bl = false;
                Vector3d vector3d = new Vector3d((double)it);
                vector3dc2 = vector3dc5;
                vector3dc3 = vector3dc4;
                quaterniondc = quaterniondc2;
                vector3dc = (Vector3dc)vector3d;
            } else {
                vector3dc = oldShipTransform.getShipToWorldScaling();
            }
            Vector3dc vector3dc6 = vector3dc;
            Quaterniondc quaterniondc3 = quaterniondc;
            Vector3dc vector3dc7 = vector3dc3;
            Vector3dc vector3dc8 = vector3dc2;
            return (ShipTransform)new ShipTransformImpl(vector3dc8, vector3dc7, quaterniondc3, vector3dc6);
        }

        @NotNull
        public final Vector3dc component1() {
            return this.newPos;
        }

        @NotNull
        public final Vector3dc component2() {
            return this.newPosInShip;
        }

        @NotNull
        public final Quaterniondc component3() {
            return this.newRot;
        }

        @NotNull
        public final Vector3dc component4() {
            return this.newVel;
        }

        @NotNull
        public final Vector3dc component5() {
            return this.newOmega;
        }

        @Nullable
        public final String component6() {
            return this.newDimension;
        }

        @Nullable
        public final Double component7() {
            return this.newScale;
        }

        @NotNull
        public final ShipTeleportDataImplFixed copy(@NotNull Vector3dc newPos, @NotNull Vector3dc newPosInShip, @NotNull Quaterniondc newRot, @NotNull Vector3dc newVel, @NotNull Vector3dc newOmega, @Nullable String newDimension, @Nullable Double newScale) {
            Intrinsics.checkNotNullParameter((Object)newPos, (String)"newPos");
            Intrinsics.checkNotNullParameter((Object)newPosInShip, (String)"newPosInShip");
            Intrinsics.checkNotNullParameter((Object)newRot, (String)"newRot");
            Intrinsics.checkNotNullParameter((Object)newVel, (String)"newVel");
            Intrinsics.checkNotNullParameter((Object)newOmega, (String)"newOmega");
            return new ShipTeleportDataImplFixed(newPos, newPosInShip, newRot, newVel, newOmega, newDimension, newScale);
        }

        public static /* synthetic */ ShipTeleportDataImplFixed copy$default(ShipTeleportDataImplFixed shipTeleportDataImplFixed, Vector3dc vector3dc, Vector3dc vector3dc2, Quaterniondc quaterniondc, Vector3dc vector3dc3, Vector3dc vector3dc4, String string, Double d, int n, Object object) {
            if ((n & 1) != 0) {
                vector3dc = shipTeleportDataImplFixed.newPos;
            }
            if ((n & 2) != 0) {
                vector3dc2 = shipTeleportDataImplFixed.newPosInShip;
            }
            if ((n & 4) != 0) {
                quaterniondc = shipTeleportDataImplFixed.newRot;
            }
            if ((n & 8) != 0) {
                vector3dc3 = shipTeleportDataImplFixed.newVel;
            }
            if ((n & 0x10) != 0) {
                vector3dc4 = shipTeleportDataImplFixed.newOmega;
            }
            if ((n & 0x20) != 0) {
                string = shipTeleportDataImplFixed.newDimension;
            }
            if ((n & 0x40) != 0) {
                d = shipTeleportDataImplFixed.newScale;
            }
            return shipTeleportDataImplFixed.copy(vector3dc, vector3dc2, quaterniondc, vector3dc3, vector3dc4, string, d);
        }

        @NotNull
        public String toString() {
            return "ShipTeleportDataImplFixed(newPos=" + this.newPos + ", newPosInShip=" + this.newPosInShip + ", newRot=" + this.newRot + ", newVel=" + this.newVel + ", newOmega=" + this.newOmega + ", newDimension=" + this.newDimension + ", newScale=" + this.newScale + ")";
        }

        public int hashCode() {
            int result = this.newPos.hashCode();
            result = result * 31 + this.newPosInShip.hashCode();
            result = result * 31 + this.newRot.hashCode();
            result = result * 31 + this.newVel.hashCode();
            result = result * 31 + this.newOmega.hashCode();
            result = result * 31 + (this.newDimension == null ? 0 : this.newDimension.hashCode());
            result = result * 31 + (this.newScale == null ? 0 : ((Object)this.newScale).hashCode());
            return result;
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ShipTeleportDataImplFixed)) {
                return false;
            }
            ShipTeleportDataImplFixed shipTeleportDataImplFixed = (ShipTeleportDataImplFixed)other;
            if (!Intrinsics.areEqual((Object)this.newPos, (Object)shipTeleportDataImplFixed.newPos)) {
                return false;
            }
            if (!Intrinsics.areEqual((Object)this.newPosInShip, (Object)shipTeleportDataImplFixed.newPosInShip)) {
                return false;
            }
            if (!Intrinsics.areEqual((Object)this.newRot, (Object)shipTeleportDataImplFixed.newRot)) {
                return false;
            }
            if (!Intrinsics.areEqual((Object)this.newVel, (Object)shipTeleportDataImplFixed.newVel)) {
                return false;
            }
            if (!Intrinsics.areEqual((Object)this.newOmega, (Object)shipTeleportDataImplFixed.newOmega)) {
                return false;
            }
            if (!Intrinsics.areEqual((Object)this.newDimension, (Object)shipTeleportDataImplFixed.newDimension)) {
                return false;
            }
            return Intrinsics.areEqual((Object)this.newScale, (Object)shipTeleportDataImplFixed.newScale);
        }

        public ShipTeleportDataImplFixed() {
            this(null, null, null, null, null, null, null, 127, null);
        }
    }

    public final class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] nArray = new int[InteractiveHandling.values().length];
            try {
                nArray[InteractiveHandling.ALWAYS.ordinal()] = 1;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                nArray[InteractiveHandling.WITHOUT_STICKER.ordinal()] = 2;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                nArray[InteractiveHandling.WITH_STICKER.ordinal()] = 3;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                nArray[InteractiveHandling.NEVER.ordinal()] = 4;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            $EnumSwitchMapping$0 = nArray;
        }
    }
}
