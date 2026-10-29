package net.ussm.theshell.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.ussm.theshell.TheShell;
import net.ussm.theshell.init.ModBlocks;

// la dalle d'herbe prend la couleur de l'herbe du biome, comme le bloc d'herbe
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class GrassSlabColors {

    @SubscribeEvent
    public static void blocks(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor(),
                ModBlocks.GRASS_SLAB.get());
    }

    @SubscribeEvent
    public static void items(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> GrassColor.get(0.5, 1.0), ModBlocks.GRASS_SLAB.get());
    }
}
