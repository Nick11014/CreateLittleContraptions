package com.createlittlecontraptions.registry;

import com.createlittlecontraptions.CreateLittleContraptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateLittleContraptions.MODID);

    public static final Supplier<CreativeModeTab> CREATE_LITTLE_CONTRAPTIONS_TAB = CREATIVE_MODE_TABS.register(
            "create_little_contraptions_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createlittlecontraptions"))
                    .icon(() -> new ItemStack(ModItems.MINI_MECHANICAL_CRAFTER.get()))
                    .displayItems((parameters, output) -> {
                        // Add all our mini contraptions to the creative tab
                        output.accept(ModItems.MINI_MECHANICAL_CRAFTER.get());
                        output.accept(ModItems.COMPACT_MIXER.get());
                        output.accept(ModItems.TINY_FUNNEL.get());
                        output.accept(ModItems.MINI_PRESS.get());
                        output.accept(ModItems.COMPACT_DEPLOYER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
