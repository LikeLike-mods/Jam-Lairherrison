package net.ussm.examplemod;

import net.ussm.examplemod.init.ModBlocks;
import net.ussm.examplemod.init.ModComponents;
import net.ussm.examplemod.init.ModCreativeModeTabs;
import net.ussm.examplemod.init.ModEntities;
import net.ussm.examplemod.init.ModItems;
import net.ussm.examplemod.init.ModBiomeSources;
import net.ussm.examplemod.init.ModChunkGenerators;
import net.ussm.examplemod.init.ModFeatures;
import net.ussm.examplemod.init.ModSounds;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();


    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeModeTabs.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModChunkGenerators.CHUNK_GENERATORS.register(modEventBus);
        ModBiomeSources.BIOME_SOURCES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModEntities.register(modEventBus);
        ModComponents.register(modEventBus);
        ModSounds.register(modEventBus);
    }
}
