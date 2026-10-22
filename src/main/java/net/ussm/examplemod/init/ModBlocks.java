package net.ussm.examplemod.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExampleMod.MOD_ID);

    public static final DeferredBlock<Block> BROWNSTONE = registerBlock("brownstone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.5f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));
    public static final DeferredBlock<Block> PALM_LOG = registerBlock("palm_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(2f, 3f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> LIGHT_SAND = registerBlock("light_sand",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.5f, 0.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.SAND)));
    public static final DeferredBlock<Block> LIGHT_SAND_WET = registerBlock("light_sand_wet",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.5f, 0.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.SAND)));
    public static final DeferredBlock<Block> PALM_LEAVES = registerBlock("palm_leaves",
            () -> new TransparentBlock(BlockBehaviour.Properties.of()
                    .strength(0.2f, 0.2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AZALEA_LEAVES)
                    .noOcclusion()));

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
