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

    public static final Supplier<CreativeModeTab> BLOCKS = CREATIVE_MODE_TAB.register("blocks",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.PALM_LOG.get()))
                    .title(Component.translatable("creativetab.examplemod.blocks"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.BROWNSTONE);
                        output.accept(ModBlocks.PALM_LOG);
                        output.accept(ModBlocks.PALM_LEAVES);
                        output.accept(ModBlocks.DRY_GRASS);
                        output.accept(ModBlocks.SHORT_DRY_GRASS);
                        output.accept(ModBlocks.LIGHT_SAND);
                        output.accept(ModBlocks.LIGHT_SAND_WET);
                        output.accept(ModBlocks.GREY_GRANITE);
                        output.accept(ModItems.COCONUT);
                        output.accept(ModBlocks.MESSAGE_BOTTLE);
                    })).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
