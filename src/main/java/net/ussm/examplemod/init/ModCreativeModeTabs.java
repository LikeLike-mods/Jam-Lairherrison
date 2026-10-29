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
                        output.accept(ModBlocks.DIRT_SLAB);
                        output.accept(ModBlocks.GRASS_SLAB);
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
                        output.accept(ModItems.DIVING_HELMET);
                        output.accept(ModItems.DIVING_CHESTPLATE);
                        output.accept(ModItems.DIVING_LEGGINGS);
                        output.accept(ModItems.DIVING_BOOTS);
                        output.accept(ModItems.DIVING_ABYSS_HELMET);
                        output.accept(ModItems.SUBMARINE);
                        output.accept(ModBlocks.HIBISCUS);
                        output.accept(ModBlocks.BIRD_OF_PARADISE);
                        output.accept(ModBlocks.FRANGIPANI);
                        output.accept(ModBlocks.SHADOW_LILY);
                        output.accept(ModBlocks.FLYTRAP);
                        output.accept(ModBlocks.PITCHER_PLANT);
                        output.accept(ModItems.NECTAR);
                        output.accept(ModItems.CRAB_CLAW);
                        output.accept(ModItems.CONCH);
                        output.accept(ModBlocks.METAL_PLATING);
                        output.accept(ModBlocks.METAL_PLATING_STAIRS);
                        output.accept(ModBlocks.METAL_PLATING_SLAB);
                        output.accept(ModBlocks.RUSTY_METAL_PLATING);
                        output.accept(ModBlocks.RUSTY_METAL_PLATING_STAIRS);
                        output.accept(ModBlocks.RUSTY_METAL_PLATING_SLAB);
                        output.accept(ModBlocks.METAL_GRATE);
                        output.accept(ModBlocks.RUSTY_METAL_GRATE);
                        output.accept(ModBlocks.REINFORCED_GLASS);
                        output.accept(ModBlocks.INDUSTRIAL_LAMP);
                        output.accept(ModBlocks.PIPE);
                        output.accept(ModBlocks.VALVE);
                        output.accept(ModBlocks.SLIDING_DOOR);
                        output.accept(ModBlocks.BROKEN_SLIDING_DOOR);
                        output.accept(ModBlocks.CHECKER_PLATE);
                        output.accept(ModBlocks.RUSTY_CHECKER_PLATE);
                        output.accept(ModBlocks.CORRUGATED_METAL);
                        output.accept(ModBlocks.RUSTY_CORRUGATED_METAL);
                        output.accept(ModBlocks.RIVETED_PANEL);
                        output.accept(ModBlocks.RUSTY_RIVETED_PANEL);
                        output.accept(ModBlocks.HAZARD_BLOCK);
                        output.accept(ModBlocks.STATION_CONCRETE);
                        output.accept(ModBlocks.AIR_VENT);
                        output.accept(ModBlocks.METAL_CRATE);
                        output.accept(ModBlocks.PORTHOLE);
                        output.accept(ModBlocks.STEEL_BEAM);
                        output.accept(ModBlocks.RUSTY_STEEL_BEAM);
                        output.accept(ModBlocks.METAL_PLATING_WALL);
                        output.accept(ModBlocks.RUSTY_METAL_PLATING_WALL);
                        output.accept(ModBlocks.METAL_GRATE_STAIRS);
                        output.accept(ModBlocks.METAL_GRATE_SLAB);
                        output.accept(ModBlocks.RUSTY_METAL_GRATE_STAIRS);
                        output.accept(ModBlocks.RUSTY_METAL_GRATE_SLAB);
                        output.accept(ModBlocks.STEEL_TRAPDOOR);
                        output.accept(ModBlocks.RUSTY_STEEL_TRAPDOOR);
                        output.accept(ModBlocks.METAL_LADDER);
                        output.accept(ModBlocks.REINFORCED_GLASS_PANE);
                        output.accept(ModBlocks.CONTROL_CONSOLE);
                        output.accept(ModBlocks.MACHINE);
                        output.accept(ModBlocks.TANK);
                        output.accept(ModBlocks.RUSTY_BARREL);
                        output.accept(ModBlocks.HANGING_CABLES);
                        output.accept(ModBlocks.DANGER_SIGN);
                        output.accept(ModBlocks.LIGHT_STRIP);
                        output.accept(ModBlocks.FLOODLIGHT);
                        output.accept(ModBlocks.EMERGENCY_LIGHT);
                        output.accept(ModBlocks.BROKEN_INDUSTRIAL_LAMP);
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
