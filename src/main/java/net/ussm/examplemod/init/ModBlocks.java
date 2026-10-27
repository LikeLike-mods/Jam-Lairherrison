package net.ussm.examplemod.init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.ussm.examplemod.blocks.CoconutBlock;
import net.ussm.examplemod.blocks.DryGrassBlock;
import net.ussm.examplemod.blocks.FlytrapBlock;
import net.ussm.examplemod.blocks.PitcherPlantBlock;
import net.ussm.examplemod.blocks.AbyssLightBlock;
import net.ussm.examplemod.blocks.AbyssSkullBlock;
import net.ussm.examplemod.blocks.MessageBottleBlock;
import net.ussm.examplemod.blocks.PalmSaplingBlock;
import net.ussm.examplemod.blocks.TropicalFlowerBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.blocks.ShortDryGrassBlock;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExampleMod.MOD_ID);

    public static final DeferredBlock<Block> BROWNSTONE = registerBlock("brownstone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(1.5f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final DeferredBlock<Block> GREY_GRANITE = registerBlock("grey_granite",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(1.8f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> PALM_LOG = registerBlock("palm_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(2f, 3f)
                    .ignitedByLava()
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> LIGHT_SAND = registerBlock("light_sand",
            () -> new ColoredFallingBlock(new ColorRGBA(0xE6D8BC), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.5f, 0.5f)
                    .sound(SoundType.SAND)));

    public static final DeferredBlock<Block> LIGHT_SAND_WET = registerBlock("light_sand_wet",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.6f, 0.6f)
                    .sound(SoundType.SAND)));

    public static final DeferredBlock<Block> PALM_LEAVES = registerBlock("palm_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES)
                    .sound(SoundType.AZALEA_LEAVES)));

    public static final DeferredBlock<PalmSaplingBlock> PALM_SAPLING = registerBlock("palm_sapling",
            () -> new PalmSaplingBlock(PalmSaplingBlock.PALM_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SAPLING)));

    public static final DeferredBlock<FlowerPotBlock> POTTED_PALM_SAPLING = BLOCKS.register("potted_palm_sapling",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, PALM_SAPLING, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_JUNGLE_SAPLING)));

    public static final DeferredBlock<Block> STRIPPED_PALM_LOG = registerBlock("stripped_palm_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_JUNGLE_LOG).mapColor(MapColor.SAND)));

    public static final DeferredBlock<Block> PALM_WOOD = registerBlock("palm_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN)));

    public static final DeferredBlock<Block> STRIPPED_PALM_WOOD = registerBlock("stripped_palm_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_JUNGLE_WOOD).mapColor(MapColor.SAND)));

    public static final DeferredBlock<Block> PALM_PLANKS = registerBlock("palm_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PLANKS).mapColor(MapColor.SAND)));

    public static final DeferredBlock<StairBlock> PALM_STAIRS = registerBlock("palm_stairs",
            () -> new StairBlock(PALM_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(PALM_PLANKS.get())));

    public static final DeferredBlock<SlabBlock> PALM_SLAB = registerBlock("palm_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(PALM_PLANKS.get())));
    public static final DeferredBlock<FenceBlock> PALM_FENCE = registerBlock("palm_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_FENCE).mapColor(MapColor.SAND)));
    public static final DeferredBlock<FenceGateBlock> PALM_FENCE_GATE = registerBlock("palm_fence_gate",
            () -> new FenceGateBlock(ModWoodTypes.PALM, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_FENCE_GATE).mapColor(MapColor.SAND)));
    public static final DeferredBlock<DoorBlock> PALM_DOOR = registerBlock("palm_door",
            () -> new DoorBlock(ModWoodTypes.PALM_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_DOOR).mapColor(MapColor.SAND)));
    public static final DeferredBlock<TrapDoorBlock> PALM_TRAPDOOR = registerBlock("palm_trapdoor",
            () -> new TrapDoorBlock(ModWoodTypes.PALM_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_TRAPDOOR).mapColor(MapColor.SAND)));
    public static final DeferredBlock<PressurePlateBlock> PALM_PRESSURE_PLATE = registerBlock("palm_pressure_plate",
            () -> new PressurePlateBlock(ModWoodTypes.PALM_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PRESSURE_PLATE).mapColor(MapColor.SAND)));
    public static final DeferredBlock<ButtonBlock> PALM_BUTTON = registerBlock("palm_button",
            () -> new ButtonBlock(ModWoodTypes.PALM_SET, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<StandingSignBlock> PALM_SIGN = BLOCKS.register("palm_sign",
            () -> new StandingSignBlock(ModWoodTypes.PALM, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SIGN).mapColor(MapColor.SAND)));
    public static final DeferredBlock<WallSignBlock> PALM_WALL_SIGN = BLOCKS.register("palm_wall_sign",
            () -> new WallSignBlock(ModWoodTypes.PALM, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_SIGN).mapColor(MapColor.SAND).lootFrom(PALM_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> PALM_HANGING_SIGN = BLOCKS.register("palm_hanging_sign",
            () -> new CeilingHangingSignBlock(ModWoodTypes.PALM, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_HANGING_SIGN).mapColor(MapColor.SAND)));
    public static final DeferredBlock<WallHangingSignBlock> PALM_WALL_HANGING_SIGN = BLOCKS.register("palm_wall_hanging_sign",
            () -> new WallHangingSignBlock(ModWoodTypes.PALM, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_HANGING_SIGN).mapColor(MapColor.SAND).lootFrom(PALM_HANGING_SIGN)));
    public static final DeferredBlock<MessageBottleBlock> MESSAGE_BOTTLE = registerBlock("message_bottle",
            () -> new MessageBottleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .instabreak()
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<AbyssSkullBlock> ABYSS_SKULL = registerBlock("abyss_skull",
            () -> new AbyssSkullBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.0f)
                    .sound(SoundType.BONE_BLOCK).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<AbyssSkullBlock> LIVING_ABYSS_SKULL = registerBlock("living_abyss_skull",
            () -> new AbyssSkullBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(1.0f)
                    .sound(SoundType.MUD).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<AbyssLightBlock> ABYSS_LIGHT = BLOCKS.register("abyss_light",
            () -> new AbyssLightBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().noOcclusion()
                    .instabreak().lightLevel(state -> 14).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<DryGrassBlock> DRY_GRASS = registerBlock("dry_grass",
            () -> new DryGrassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .replaceable()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<ShortDryGrassBlock> SHORT_DRY_GRASS = registerBlock("short_dry_grass",
            () -> new ShortDryGrassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .replaceable()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<TropicalFlowerBlock> HIBISCUS = registerBlock("hibiscus",
            () -> new TropicalFlowerBlock(MobEffects.REGENERATION, 8.0f, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<TropicalFlowerBlock> BIRD_OF_PARADISE = registerBlock("bird_of_paradise",
            () -> new TropicalFlowerBlock(MobEffects.MOVEMENT_SPEED, 6.0f, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<TropicalFlowerBlock> FRANGIPANI = registerBlock("frangipani",
            () -> new TropicalFlowerBlock(MobEffects.SATURATION, 0.35f, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<TropicalFlowerBlock> SHADOW_LILY = registerBlock("shadow_lily",
            () -> new TropicalFlowerBlock(MobEffects.NIGHT_VISION, 5.0f, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)
                    .lightLevel(state -> 6)));

    public static final DeferredBlock<FlytrapBlock> FLYTRAP = registerBlock("flytrap",
            () -> new FlytrapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<PitcherPlantBlock> PITCHER_PLANT = registerBlock("pitcher_plant",
            () -> new PitcherPlantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<CoconutBlock> COCONUT = BLOCKS.register("coconut",
            () -> new CoconutBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.4f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block ) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name,DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
