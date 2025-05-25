package com.createlittlecontraptions.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tiny Funnel - A smaller version of Create's Funnel.
 * Perfect for tight spaces while maintaining item transfer capabilities.
 */
public class TinyFunnelBlock extends Block {
    
    public TinyFunnelBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Tiny Funnel (Functionality coming soon!)"));
        }
        return InteractionResult.SUCCESS;
    }
    
    // TODO: Add block entity for item transfer logic
    // TODO: Add proper funnel behavior integration with Create
    // TODO: Add directional states and properties
}
