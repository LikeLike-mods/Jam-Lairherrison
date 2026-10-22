package net.ussm.examplemod.init;

import net.minecraft.world.item.Items;
import net.ussm.examplemod.ExampleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MOD_ID);

    public static final Supplier<CreativeModeTab> TEST_TAB = CREATIVE_MODE_TAB.register("test_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.MILK_BUCKET))
                    .title(Component.translatable("creativetab.exemplemod.test"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModBlock.SAINT_TEST_BLOCK);
                        output.accept(ModItem.SHADOW_SABER_SWORD);
                        output.accept(ModItem.SHADOW_SABER_SWORD_VIOLET);
                        output.accept(ModItem.LIGHT_SABER_SWORD);
                    })).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
