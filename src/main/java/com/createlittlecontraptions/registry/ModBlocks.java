package com.createlittlecontraptions.registry;

import com.createlittlecontraptions.CreateLittleContraptions;
import com.createlittlecontraptions.blocks.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateLittleContraptions.MODID);
    
    // Mini Mechanical Crafter - smaller version of Create's mechanical crafter
    public static final DeferredBlock<Block> MINI_MECHANICAL_CRAFTER = BLOCKS.register("mini_mechanical_crafter",
            () -> new MiniMechanicalCrafterBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));
    
    // Compact Mixer - smaller version of Create's mechanical mixer
    public static final DeferredBlock<Block> COMPACT_MIXER = BLOCKS.register("compact_mixer",
            () -> new CompactMixerBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 8.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    
    // Tiny Funnel - smaller version of Create's funnel
    public static final DeferredBlock<Block> TINY_FUNNEL = BLOCKS.register("tiny_funnel",
            () -> new TinyFunnelBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 4.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    
    // Mini Press - smaller version of Create's mechanical press
    public static final DeferredBlock<Block> MINI_PRESS = BLOCKS.register("mini_press",
            () -> new MiniPressBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 8.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    
    // Compact Deployer - smaller version of Create's deployer
    public static final DeferredBlock<Block> COMPACT_DEPLOYER = BLOCKS.register("compact_deployer",
            () -> new CompactDeployerBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
