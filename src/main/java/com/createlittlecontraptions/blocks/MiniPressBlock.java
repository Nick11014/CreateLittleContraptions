package com.createlittlecontraptions.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Mini Press - A smaller version of Create's Mechanical Press.
 * Requires less rotational force and takes up less vertical space.
 */
public class MiniPressBlock extends Block {
    
    public MiniPressBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Mini Press (Functionality coming soon!)"));
        }
        return InteractionResult.SUCCESS;
    }
    
    // TODO: Add block entity for pressing logic
    // TODO: Add kinetic behavior integration with Create
    // TODO: Add proper rendering and animation
}
