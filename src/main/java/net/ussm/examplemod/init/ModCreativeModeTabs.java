package net.ussm.examplemod.init;

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
                        output.accept(ModBlocks.PALM_SAPLING);
                        output.accept(ModBlocks.STRIPPED_PALM_LOG);
                        output.accept(ModBlocks.PALM_WOOD);
                        output.accept(ModBlocks.STRIPPED_PALM_WOOD);
                        output.accept(ModBlocks.PALM_PLANKS);
                        output.accept(ModBlocks.PALM_STAIRS);
                        output.accept(ModBlocks.PALM_SLAB);
                        output.accept(ModBlocks.PALM_FENCE);
                        output.accept(ModBlocks.PALM_FENCE_GATE);
                        output.accept(ModBlocks.PALM_DOOR);
                        output.accept(ModBlocks.PALM_TRAPDOOR);
                        output.accept(ModBlocks.PALM_PRESSURE_PLATE);
                        output.accept(ModBlocks.PALM_BUTTON);
                        output.accept(ModItems.PALM_SIGN);
                        output.accept(ModItems.PALM_HANGING_SIGN);
                        output.accept(ModItems.PALM_BOAT);
                        output.accept(ModItems.PALM_CHEST_BOAT);
                        output.accept(ModBlocks.DRY_GRASS);
                        output.accept(ModBlocks.SHORT_DRY_GRASS);
                        output.accept(ModBlocks.LIGHT_SAND);
                        output.accept(ModBlocks.LIGHT_SAND_WET);
                        output.accept(ModBlocks.GREY_GRANITE);
                        output.accept(ModBlocks.COCONUT);
                        output.accept(ModItems.COCONUT_HALF);
                        output.accept(ModItems.COCONUT_SHELL);
                        output.accept(ModItems.REDSTONE_PEPPER);
                        output.accept(ModItems.REDSTONE_PEPPER_SEEDS);
                        output.accept(ModItems.RAW_CRAB_MEAT);
                        output.accept(ModItems.COOKED_CRAB_MEAT);
                        output.accept(ModBlocks.MESSAGE_BOTTLE);
                        output.accept(ModBlocks.ABYSS_SKULL);
                        output.accept(ModItems.ABYSSAL_SCALE);
                        output.accept(ModBlocks.LIVING_ABYSS_SKULL);
                        output.accept(ModItems.DIAMOND_ABYSS_HELMET);
                        output.accept(ModItems.NETHERITE_ABYSS_HELMET);
                        output.accept(ModBlocks.HIBISCUS);
                        output.accept(ModBlocks.BIRD_OF_PARADISE);
                        output.accept(ModBlocks.FRANGIPANI);
                        output.accept(ModBlocks.SHADOW_LILY);
                        output.accept(ModBlocks.FLYTRAP);
                        output.accept(ModBlocks.PITCHER_PLANT);
                        output.accept(ModItems.NECTAR);
                        output.accept(ModItems.CRAB_CLAW);
                        ModItems.SPAWN_EGGS.forEach(output::accept);
                        output.accept(ModItems.LEVIATHAN_SPAWN_EGG);
                        output.accept(ModItems.SHADOW_SABER_SWORD);
                        output.accept(ModItems.SHADOW_SABER_SWORD_VIOLET);
                        output.accept(ModItems.LIGHT_SABER_SWORD);
                    })).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
