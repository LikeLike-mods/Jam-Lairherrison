package net.ussm.theshell;

import net.ussm.theshell.init.ModArmorMaterials;
import net.ussm.theshell.init.ModAttachments;
import net.ussm.theshell.init.ModBlockEntities;
import net.ussm.theshell.init.ModBlocks;
import net.ussm.theshell.init.ModComponents;
import net.ussm.theshell.init.ModCreativeModeTabs;
import net.ussm.theshell.init.ModEntities;
import net.ussm.theshell.init.ModItems;
import net.ussm.theshell.init.ModBiomeSources;
import net.ussm.theshell.init.ModChunkGenerators;
import net.ussm.theshell.init.ModFeatures;
import net.ussm.theshell.init.ModSounds;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(TheShell.MOD_ID)
public class TheShell {
    public static final String MOD_ID = "theshell";
    public static final Logger LOGGER = LogUtils.getLogger();


    public TheShell(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeModeTabs.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModChunkGenerators.CHUNK_GENERATORS.register(modEventBus);
        ModBiomeSources.BIOME_SOURCES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModEntities.register(modEventBus);
        ModComponents.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModSounds.register(modEventBus);
    }
}
