package net.ussm.the_shell.init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.sounds.SoundEvents;
import net.ussm.the_shell.blocks.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.crops.RedstonePepperBlock;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheShell.MOD_ID);

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


    public static final DeferredBlock<Block> LIGHT_SAND_WET = registerBlock("light_sand_wet",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(0.6f, 0.6f)
                    .sound(SoundType.SAND)));


    public static final DeferredBlock<Block> LIGHT_SAND = registerBlock("light_sand",
            () -> new ConcretePowderBlock(LIGHT_SAND_WET.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.5f, 0.5f)
                    .sound(SoundType.SAND)));

    public static final DeferredBlock<Block> ABYSSAL_STONE = registerBlock("abyssal_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> ABYSSAL_SILT = registerBlock("abyssal_silt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.6f)
                    .sound(SoundType.MUD)));

    public static final DeferredBlock<Block> ABYSSAL_BASALT = registerBlock("abyssal_basalt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.BASALT)));

    public static final DeferredBlock<ChimneyBlock> HYDROTHERMAL_CHIMNEY = registerBlock("hydrothermal_chimney",
            () -> new ChimneyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.BASALT)
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(ChimneyBlock.TOP) ? 6 : 0)));

    public static final DeferredBlock<AbyssPlantBlock> TUBE_WORMS = registerBlock("tube_worms",
            () -> new AbyssPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS)
                    .lightLevel(s -> 3), false));

    public static final DeferredBlock<AbyssPlantBlock> GLOWING_ANEMONE = registerBlock("glowing_anemone",
            () -> new AbyssPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS)
                    .lightLevel(s -> 10), true));

    public static final DeferredBlock<DecorBlock> MANGANESE_NODULE = registerBlock("manganese_nodule",
            () -> new DecorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.0f)
                    .sound(SoundType.TUFF)
                    .noOcclusion(),
                    Block.box(4, 0, 4, 12, 5, 12)));

    public static final DeferredBlock<Block> AIR_BUBBLE = registerBlock("air_bubble",
            () -> new AirBubbleBlock(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 6)));
    public static final DeferredBlock<Block> SUBMARINE_WINDOW = registerBlock("submarine_window",
            () -> new SubmarineWindowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).lightLevel(s -> 5)));

    // les minerais du jeu, version abysses
    public static final DeferredBlock<Block> ABYSSAL_COAL_ORE = registerBlock("abyssal_coal_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(0, 2), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COAL_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_IRON_ORE = registerBlock("abyssal_iron_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_COPPER_ORE = registerBlock("abyssal_copper_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_GOLD_ORE = registerBlock("abyssal_gold_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_REDSTONE_ORE = registerBlock("abyssal_redstone_ore",
            () -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_REDSTONE_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_LAPIS_ORE = registerBlock("abyssal_lapis_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(2, 5), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_LAPIS_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_DIAMOND_ORE = registerBlock("abyssal_diamond_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(3, 7), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)));
    public static final DeferredBlock<Block> ABYSSAL_EMERALD_ORE = registerBlock("abyssal_emerald_ore",
            () -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(3, 7), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));

    public static final DeferredBlock<Block> KYBER_ORE = registerBlock("kyber_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(4.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 7)));

    public static final DeferredBlock<Block> SULFUR_BLOCK = registerBlock("sulfur_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.TUFF)));

    public static final DeferredBlock<Block> GLOWING_CORAL_BLOCK = registerBlock("glowing_coral_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.5f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.CORAL_BLOCK)
                    .lightLevel(s -> 8)));


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
                    .instabreak().lightLevel(state -> 15).pushReaction(PushReaction.DESTROY)));
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

    public static final DeferredBlock<TropicalFlowerBlock> TROPICAL_ORCHID = registerBlock("tropical_orchid",
            () -> new TropicalFlowerBlock(MobEffects.ABSORPTION, 5.0f, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));

    public static final DeferredBlock<TropicalFlowerBlock> JADE_FLOWER = registerBlock("jade_flower",
            () -> new TropicalFlowerBlock(MobEffects.WATER_BREATHING, 8.0f, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));

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

    // bananier
    public static final DeferredBlock<Block> BANANA_STEM = registerBlock("banana_stem",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.5f).ignitedByLava().sound(SoundType.BAMBOO_WOOD)));
    public static final DeferredBlock<Block> BANANA_LEAVES = registerBlock("banana_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES).sound(SoundType.AZALEA_LEAVES)));
    public static final DeferredBlock<PalmSaplingBlock> BANANA_SAPLING = registerBlock("banana_sapling",
            () -> new PalmSaplingBlock(PalmSaplingBlock.BANANA_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SAPLING)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_BANANA_SAPLING = BLOCKS.register("potted_banana_sapling",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, BANANA_SAPLING, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_JUNGLE_SAPLING)));
    public static final DeferredBlock<net.ussm.the_shell.blocks.HangingFruitBlock> BANANA_BUNCH = BLOCKS.register("banana_bunch",
            () -> new net.ussm.the_shell.blocks.HangingFruitBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.2f)
                    .sound(SoundType.GRASS).noOcclusion().noCollission().pushReaction(PushReaction.DESTROY)));

    // palmier dattier (ses buches donnent des planches de palmier)
    public static final DeferredBlock<Block> DATE_PALM_LOG = registerBlock("date_palm_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2f, 3f).ignitedByLava().sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> DATE_PALM_LEAVES = registerBlock("date_palm_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES).sound(SoundType.AZALEA_LEAVES)));
    public static final DeferredBlock<PalmSaplingBlock> DATE_PALM_SAPLING = registerBlock("date_palm_sapling",
            () -> new PalmSaplingBlock(PalmSaplingBlock.DATE_PALM_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SAPLING)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_DATE_PALM_SAPLING = BLOCKS.register("potted_date_palm_sapling",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, DATE_PALM_SAPLING, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_JUNGLE_SAPLING)));
    public static final DeferredBlock<net.ussm.the_shell.blocks.HangingFruitBlock> DATE_CLUSTER = BLOCKS.register("date_cluster",
            () -> new net.ussm.the_shell.blocks.HangingFruitBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.2f)
                    .sound(SoundType.GRASS).noOcclusion().noCollission().pushReaction(PushReaction.DESTROY)));

    // tout en haut des abysses a la place de la bedrock : du noir
    public static final DeferredBlock<Block> ABYSSAL_VOID = registerBlock("abyssal_void",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).mapColor(MapColor.COLOR_BLACK)));

    public static final DeferredBlock<CoconutBlock> COCONUT = registerBlock("coconut",
            () -> new CoconutBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.4f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)));


    public static final DeferredBlock<RedstonePepperBlock> REDSTONE_PEPPER_CROP = registerBlock("redstone_pepper_crop",
            () -> new RedstonePepperBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> METAL_PLATING = registerBlock("metal_plating",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> RUSTY_METAL_PLATING = registerBlock("rusty_metal_plating",
            () -> new Block(metal()));

    public static final DeferredBlock<StairBlock> METAL_PLATING_STAIRS = registerBlock("metal_plating_stairs",
            () -> new StairBlock(METAL_PLATING.get().defaultBlockState(), metal()));

    public static final DeferredBlock<SlabBlock> METAL_PLATING_SLAB = registerBlock("metal_plating_slab",
            () -> new SlabBlock(metal()));

    public static final DeferredBlock<StairBlock> RUSTY_METAL_PLATING_STAIRS = registerBlock("rusty_metal_plating_stairs",
            () -> new StairBlock(RUSTY_METAL_PLATING.get().defaultBlockState(), metal()));

    public static final DeferredBlock<SlabBlock> RUSTY_METAL_PLATING_SLAB = registerBlock("rusty_metal_plating_slab",
            () -> new SlabBlock(metal()));

    public static final DeferredBlock<WaterloggedTransparentBlock> METAL_GRATE = registerBlock("metal_grate",
            () -> new WaterloggedTransparentBlock(seeThrough(metal().sound(SoundType.COPPER_GRATE))));

    public static final DeferredBlock<WaterloggedTransparentBlock> RUSTY_METAL_GRATE = registerBlock("rusty_metal_grate",
            () -> new WaterloggedTransparentBlock(seeThrough(metal().sound(SoundType.COPPER_GRATE))));

    // verre blinde : resiste aux explosions comme l'obsidienne
    public static final DeferredBlock<TransparentBlock> REINFORCED_GLASS = registerBlock("reinforced_glass",
            () -> new TransparentBlock(seeThrough(metal()
                    .strength(5f, 1200f)
                    .sound(SoundType.GLASS))));

    public static final DeferredBlock<IndustrialLampBlock> INDUSTRIAL_LAMP = registerBlock("industrial_lamp",
            () -> new IndustrialLampBlock(metal()
                    .strength(3f, 6f)
                    .lightLevel(s -> s.getValue(IndustrialLampBlock.LIT) ? 15 : 0)
                    .noOcclusion()));

    public static final DeferredBlock<MetalPipeBlock> PIPE = registerBlock("pipe",
            () -> new MetalPipeBlock(metal()
                    .noOcclusion()));

    public static final DeferredBlock<ValveBlock> VALVE = registerBlock("valve",
            () -> new ValveBlock(metal()
                    .strength(2f, 6f)
                    .noOcclusion()
                    .noCollission()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<SlidingDoorBlock> SLIDING_DOOR = registerBlock("sliding_door",
            () -> new SlidingDoorBlock(metal()
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK), false));

    public static final DeferredBlock<SlidingDoorBlock> BROKEN_SLIDING_DOOR = registerBlock("broken_sliding_door",
            () -> new SlidingDoorBlock(metal()
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK), true));

    public static final BlockSetType STEEL_SET = BlockSetType.register(new BlockSetType(TheShell.MOD_ID + ":steel",
            true,
            true,
            false,
            BlockSetType.PressurePlateSensitivity.MOBS,
            SoundType.METAL,
            SoundEvents.IRON_DOOR_CLOSE,
            SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE,
            SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundEvents.STONE_BUTTON_CLICK_ON));

    public static final DeferredBlock<Block> CHECKER_PLATE = registerBlock("checker_plate",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> RUSTY_CHECKER_PLATE = registerBlock("rusty_checker_plate",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> CORRUGATED_METAL = registerBlock("corrugated_metal",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> RUSTY_CORRUGATED_METAL = registerBlock("rusty_corrugated_metal",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> RIVETED_PANEL = registerBlock("riveted_panel",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> RUSTY_RIVETED_PANEL = registerBlock("rusty_riveted_panel",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> HAZARD_BLOCK = registerBlock("hazard_block",
            () -> new Block(metal()));

    public static final DeferredBlock<Block> STATION_CONCRETE = registerBlock("station_concrete",
            () -> new Block(metal()
                    .strength(3f, 6f)
                    .sound(SoundType.STONE)));

    public static final DeferredBlock<AirVentBlock> AIR_VENT = registerBlock("air_vent",
            () -> new AirVentBlock(metal()));

    public static final DeferredBlock<StorageBlock> METAL_CRATE = registerBlock("metal_crate",
            () -> new StorageBlock(metal(), Shapes.block()));

    public static final DeferredBlock<TransparentBlock> PORTHOLE = registerBlock("porthole",
            () -> new TransparentBlock(seeThrough(metal())));

    public static final DeferredBlock<RotatedPillarBlock> STEEL_BEAM = registerBlock("steel_beam",
            () -> new RotatedPillarBlock(metal()));

    public static final DeferredBlock<RotatedPillarBlock> RUSTY_STEEL_BEAM = registerBlock("rusty_steel_beam",
            () -> new RotatedPillarBlock(metal()));

    public static final DeferredBlock<WallBlock> METAL_PLATING_WALL = registerBlock("metal_plating_wall",
            () -> new WallBlock(metal()
                    .forceSolidOn()));

    public static final DeferredBlock<WallBlock> RUSTY_METAL_PLATING_WALL = registerBlock("rusty_metal_plating_wall",
            () -> new WallBlock(metal().forceSolidOn()));

    public static final DeferredBlock<StairBlock> METAL_GRATE_STAIRS = registerBlock("metal_grate_stairs",
            () -> new StairBlock(METAL_GRATE.get()
                    .defaultBlockState(),
                    seeThrough(metal().sound(SoundType.COPPER_GRATE))));

    public static final DeferredBlock<SlabBlock> METAL_GRATE_SLAB = registerBlock("metal_grate_slab",
            () -> new SlabBlock(seeThrough(metal().sound(SoundType.COPPER_GRATE))));

    public static final DeferredBlock<StairBlock> RUSTY_METAL_GRATE_STAIRS = registerBlock("rusty_metal_grate_stairs",
            () -> new StairBlock(RUSTY_METAL_GRATE.get().defaultBlockState(), seeThrough(metal().sound(SoundType.COPPER_GRATE))));

    public static final DeferredBlock<SlabBlock> RUSTY_METAL_GRATE_SLAB = registerBlock("rusty_metal_grate_slab",
            () -> new SlabBlock(seeThrough(metal().sound(SoundType.COPPER_GRATE))));

    public static final DeferredBlock<TrapDoorBlock> STEEL_TRAPDOOR = registerBlock("steel_trapdoor",
            () -> new TrapDoorBlock(STEEL_SET, metal()
                    .noOcclusion()
                    .isValidSpawn(Blocks::never)));

    public static final DeferredBlock<TrapDoorBlock> RUSTY_STEEL_TRAPDOOR = registerBlock("rusty_steel_trapdoor",
            () -> new TrapDoorBlock(STEEL_SET, metal()
                    .noOcclusion()
                    .isValidSpawn(Blocks::never)));

    public static final DeferredBlock<LadderBlock> METAL_LADDER = registerBlock("metal_ladder",
            () -> new LadderBlock(metal()
                    .strength(2f)
                    .noOcclusion()));

    public static final DeferredBlock<IronBarsBlock> REINFORCED_GLASS_PANE = registerBlock("reinforced_glass_pane",
            () -> new IronBarsBlock(seeThrough(metal()
                    .strength(5f, 1200f)
                    .sound(SoundType.GLASS))));

    public static final DeferredBlock<ConsoleBlock> CONTROL_CONSOLE = registerBlock("control_console",
            () -> new ConsoleBlock(metal()
                    .lightLevel(s -> s.getValue(ConsoleBlock.POWERED) ? 7 : 3)));

    public static final DeferredBlock<MachineBlock> MACHINE = registerBlock("machine",
            () -> new MachineBlock(metal().lightLevel(s -> s.getValue(MachineBlock.POWERED) ? 6 : 0)));

    public static final DeferredBlock<TankBlock> TANK = registerBlock("tank",
            () -> new TankBlock(metal()));

    public static final DeferredBlock<StorageBlock> RUSTY_BARREL = registerBlock("rusty_barrel",
            () -> new StorageBlock(metal(), Block.box(2, 0, 2, 14, 14, 14)));

    public static final DeferredBlock<CableBlock> HANGING_CABLES = registerBlock("hanging_cables",
            () -> new CableBlock(metal()
                    .strength(0.5f)
                    .sound(SoundType.CHAIN)
                    .noOcclusion()));
    public static final DeferredBlock<DecorBlock> DANGER_SIGN = registerBlock("danger_sign",
            () -> new DecorBlock(metal()
                    .strength(1f)
                    .noOcclusion(),
                    Block.box(1, 2, 15, 15, 14, 16), false));

    public static final DeferredBlock<LightDecorBlock> LIGHT_STRIP = registerBlock("light_strip",
            () -> new LightDecorBlock(metal()
                    .strength(1f)
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(LightDecorBlock.LIT) ? 12 : 0),
                    Block.box(0, 6, 14, 16, 10, 16), false));

    public static final DeferredBlock<FloodlightBlock> FLOODLIGHT = registerBlock("floodlight",
            () -> new FloodlightBlock(metal()
                    .strength(2f)
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(LightDecorBlock.LIT) ? 15 : 0)));

    public static final DeferredBlock<FloodlightBeamBlock> FLOODLIGHT_BEAM = BLOCKS.register("floodlight_beam",
            () -> new FloodlightBeamBlock(BlockBehaviour.Properties.of()
                    .replaceable()
                    .noCollission()
                    .noLootTable()
                    .noOcclusion()
                    .instabreak()
                    .lightLevel(state -> 14).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<EmergencyLightBlock> EMERGENCY_LIGHT = registerBlock("emergency_light",
            () -> new EmergencyLightBlock(metal()
                    .strength(3f, 6f)
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(EmergencyLightBlock.ALARM) && s.getValue(EmergencyLightBlock.FLASH) ? 12 : 0)));
    public static final DeferredBlock<IndustrialLampBlock> BROKEN_INDUSTRIAL_LAMP = registerBlock("broken_industrial_lamp",
            () -> new IndustrialLampBlock(metal()
                    .strength(3f, 6f)
                    .noOcclusion(),
                    false));

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(5f, 6f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties seeThrough(BlockBehaviour.Properties properties) {
        return properties
                .noOcclusion()
                .isValidSpawn(Blocks::never)
                .isRedstoneConductor((s, l, p) -> false)
                .isSuffocating((s, l, p) -> false)
                .isViewBlocking((s, l, p) -> false);
    }

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
