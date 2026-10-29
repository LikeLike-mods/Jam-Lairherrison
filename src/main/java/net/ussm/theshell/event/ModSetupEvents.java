package net.ussm.theshell.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.ussm.theshell.TheShell;
import net.ussm.theshell.entity.init.SpawnRules;
import net.ussm.theshell.init.ModBlocks;
import net.ussm.theshell.init.ModEntities;
import net.ussm.theshell.init.ModItems;

import java.util.List;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class ModSetupEvents {
    // pancartes en palmier = block entities vanilla
    @SubscribeEvent
    public static void addSignBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, ModBlocks.PALM_SIGN.get(), ModBlocks.PALM_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN, ModBlocks.PALM_HANGING_SIGN.get(), ModBlocks.PALM_WALL_HANGING_SIGN.get());
    }

    // regles de spawn
    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnRules::onSandOrGrass, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (EntityType<? extends Mob> type : List.of(ModEntities.CRAB.get(), ModEntities.HERMIT_CRAB.get(), ModEntities.SEAGULL.get())) {
            event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnRules::onSand, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (EntityType<? extends Mob> type : List.of(ModEntities.GECKO.get(), ModEntities.POISON_FROG.get(), ModEntities.TOUCAN.get())) {
            event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnRules::onGrass, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(ModEntities.JELLYFISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnRules::inLagoon, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (EntityType<? extends Mob> type : List.of(ModEntities.ABYSSAL_ANGLER.get(), ModEntities.GULPER_EEL.get(),
                ModEntities.DRAGONFISH.get(), ModEntities.GIANT_ISOPOD.get(), ModEntities.VAMPIRE_SQUID.get())) {
            event.register(type, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnRules::inDeepWater, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    @SubscribeEvent
    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) ModItems.SPAWN_EGGS.forEach(event::accept);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModSetupEvents::registerFlammables);
        event.enqueueWork(() -> ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.PALM_SAPLING.getId(), ModBlocks.POTTED_PALM_SAPLING));
    }

    // ce qui brule
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
