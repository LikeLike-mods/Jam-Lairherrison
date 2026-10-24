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

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "examplemod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    // Les événements (installation, pancartes, apparitions des mobs, rendus...) sont dans les classes event/ et client/.
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
