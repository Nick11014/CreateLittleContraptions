package com.createlittlecontraptions.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Compact Mixer - A smaller version of Create's Mechanical Mixer.
 * Uses less space and rotational force while maintaining mixing capabilities.
 */
public class CompactMixerBlock extends Block {
    
    public CompactMixerBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Compact Mixer (GUI coming soon!)"));
        }
        return InteractionResult.SUCCESS;
    }
    
    // TODO: Add block entity for mixing logic
    // TODO: Add kinetic behavior integration with Create
    // TODO: Add proper rendering and animation
}
