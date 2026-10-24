package net.ussm.examplemod.event;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModBlocks;

@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class ModSetupEvents {
    // Les pancartes en palmier utilisent les block entities des pancartes vanilla (texte, rendu, édition)
    @SubscribeEvent
    public static void addSignBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, ModBlocks.PALM_SIGN.get(), ModBlocks.PALM_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN, ModBlocks.PALM_HANGING_SIGN.get(), ModBlocks.PALM_WALL_HANGING_SIGN.get());
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModSetupEvents::registerFlammables);
    }

    // Ce qui brûle et se propage au feu (mêmes valeurs que les bois, feuilles et fleurs vanilla)
    private static void registerFlammables() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(ModBlocks.PALM_LOG.get(), 5, 5);
        fire.setFlammable(ModBlocks.STRIPPED_PALM_LOG.get(), 5, 5);
        fire.setFlammable(ModBlocks.PALM_WOOD.get(), 5, 5);
        fire.setFlammable(ModBlocks.STRIPPED_PALM_WOOD.get(), 5, 5);
        fire.setFlammable(ModBlocks.PALM_PLANKS.get(), 5, 20);
        fire.setFlammable(ModBlocks.PALM_STAIRS.get(), 5, 20);
        fire.setFlammable(ModBlocks.PALM_SLAB.get(), 5, 20);
        fire.setFlammable(ModBlocks.PALM_FENCE.get(), 5, 20);
        fire.setFlammable(ModBlocks.PALM_FENCE_GATE.get(), 5, 20);
        fire.setFlammable(ModBlocks.PALM_LEAVES.get(), 30, 60);
        fire.setFlammable(ModBlocks.DRY_GRASS.get(), 60, 100);
        fire.setFlammable(ModBlocks.SHORT_DRY_GRASS.get(), 60, 100);
        fire.setFlammable(ModBlocks.HIBISCUS.get(), 60, 100);
        fire.setFlammable(ModBlocks.BIRD_OF_PARADISE.get(), 60, 100);
        fire.setFlammable(ModBlocks.FRANGIPANI.get(), 60, 100);
        fire.setFlammable(ModBlocks.SHADOW_LILY.get(), 60, 100);
        fire.setFlammable(ModBlocks.FLYTRAP.get(), 60, 100);
        fire.setFlammable(ModBlocks.PITCHER_PLANT.get(), 60, 100);
    }
}
