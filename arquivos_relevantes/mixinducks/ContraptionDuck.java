/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.behaviour.MovementContext
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$StructureBlockInfo
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package org.valkyrienskies.create_interactive.mixinducks;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContraptionDuck {
    public void ci$setBlock(@NotNull BlockPos var1, @NotNull StructureTemplate.StructureBlockInfo var2);

    public boolean ci$hasActorAtPos(@NotNull BlockPos var1);

    public boolean ci$hasBogeyAtPos(@NotNull BlockPos var1);

    @Nullable
    public Pair<StructureTemplate.StructureBlockInfo, MovementContext> ci$getActorAtPos(@NotNull BlockPos var1);

    @NotNull
    public Collection<BlockPos> ci$getChangedActors();

    public void ci$clearChangedActors();
}
