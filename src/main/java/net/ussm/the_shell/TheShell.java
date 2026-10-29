package net.ussm.the_shell;

import net.ussm.the_shell.init.ModArmorMaterials;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.init.ModBlockEntities;
import net.ussm.the_shell.init.ModBlocks;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModCreativeModeTabs;
import net.ussm.the_shell.init.ModEntities;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.init.ModBiomeSources;
import net.ussm.the_shell.init.ModChunkGenerators;
import net.ussm.the_shell.init.ModFeatures;
import net.ussm.the_shell.init.ModSounds;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(TheShell.MOD_ID)
public class TheShell {
    public static final String MOD_ID = "the_shell";
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
