package net.ussm.examplemod.client;

import net.minecraft.client.renderer.Sheets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModWoodTypes;

@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // textures des pancartes en palmier pour le rendu vanilla des pancartes
        event.enqueueWork(() -> Sheets.addWoodType(ModWoodTypes.PALM));
    }
}
