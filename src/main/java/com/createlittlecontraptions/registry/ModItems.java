package com.createlittlecontraptions.registry;

import com.createlittlecontraptions.CreateLittleContraptions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateLittleContraptions.MODID);
    
    // Block Items for all our mini contraptions
    public static final DeferredItem<BlockItem> MINI_MECHANICAL_CRAFTER = ITEMS.register("mini_mechanical_crafter",
            () -> new BlockItem(ModBlocks.MINI_MECHANICAL_CRAFTER.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> COMPACT_MIXER = ITEMS.register("compact_mixer",
            () -> new BlockItem(ModBlocks.COMPACT_MIXER.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> TINY_FUNNEL = ITEMS.register("tiny_funnel",
            () -> new BlockItem(ModBlocks.TINY_FUNNEL.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> MINI_PRESS = ITEMS.register("mini_press",
            () -> new BlockItem(ModBlocks.MINI_PRESS.get(), new Item.Properties()));
    
    public static final DeferredItem<BlockItem> COMPACT_DEPLOYER = ITEMS.register("compact_deployer",
            () -> new BlockItem(ModBlocks.COMPACT_DEPLOYER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
