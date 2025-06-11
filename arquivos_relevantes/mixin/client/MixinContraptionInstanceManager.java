/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jozufozu.flywheel.api.MaterialManager
 *  com.jozufozu.flywheel.backend.instancing.TaskEngine
 *  com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.content.contraptions.render.ActorInstance
 *  com.simibubi.create.content.contraptions.render.ContraptionInstanceManager
 *  net.minecraft.client.Camera
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.NotNull
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package org.valkyrienskies.create_interactive.mixin.client;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.content.contraptions.render.ContraptionInstanceManager;
import java.util.ArrayList;
import net.minecraft.client.Camera;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.create_interactive.CreateInteractiveUtil;
import org.valkyrienskies.create_interactive.mixin_logic.client.MixinContraptionInstanceManagerLogic;
import org.valkyrienskies.create_interactive.mixinducks.ContraptionInstanceManagerDuck;

@Mixin(value={ContraptionInstanceManager.class})
public abstract class MixinContraptionInstanceManager
extends BlockEntityInstanceManager
implements ContraptionInstanceManagerDuck {
    @Shadow(remap=false)
    protected ArrayList<ActorInstance> actors;
    @Shadow(remap=false)
    private Contraption contraption;
    @Unique
    private boolean ci$hasRemovedBlockEntities = false;

    public MixinContraptionInstanceManager(MaterialManager materialManager) {
        super(materialManager);
    }

    @Override
    public void ci$deleteActorInstance(ActorInstance actorInstance) {
        MixinContraptionInstanceManagerLogic.INSTANCE.deleteActorInstance$create_interactive(this.actors, actorInstance);
    }

    @Overwrite
    public void beginFrame(@NotNull TaskEngine taskEngine, @NotNull Camera info) {
        if (!CreateInteractiveUtil.INSTANCE.doesContraptionHaveShipLoaded(this.contraption)) {
            super.beginFrame(taskEngine, info);
        } else if (!this.ci$hasRemovedBlockEntities) {
            for (BlockEntity be : this.contraption.maybeInstancedBlockEntities) {
                this.remove(be);
            }
            this.ci$hasRemovedBlockEntities = true;
        }
        this.actors.forEach(ActorInstance::beginFrame);
    }
}
