package net.ussm.examplemod.client;

import net.minecraft.client.renderer.Sheets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModEntities;
import net.ussm.examplemod.init.ModWoodTypes;

@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // textures des pancartes en palmier pour le rendu vanilla des pancartes
        event.enqueueWork(() -> Sheets.addWoodType(ModWoodTypes.PALM));
    }

    // (nom, ombre, la tête suit le regard, parties lumineuses, translucide) ; le léviathan a son propre rendu
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LEVIATHAN.get(), LeviathanRenderer::new);
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), c -> new AtollMobRenderer<>(c, "butterfly", 0.2F, false, false, false));
        event.registerEntityRenderer(ModEntities.CRAB.get(), c -> new AtollMobRenderer<>(c, "crab", 0.4F, false, false, false));
        event.registerEntityRenderer(ModEntities.GECKO.get(), c -> new AtollMobRenderer<>(c, "gecko", 0.25F, true, false, false));
        event.registerEntityRenderer(ModEntities.HERMIT_CRAB.get(), c -> new AtollMobRenderer<>(c, "hermit_crab", 0.3F, false, false, false));
        event.registerEntityRenderer(ModEntities.JELLYFISH.get(), c -> new AtollMobRenderer<>(c, "jellyfish", 0.0F, false, false, true));
        event.registerEntityRenderer(ModEntities.POISON_FROG.get(), c -> new AtollMobRenderer<>(c, "poison_frog", 0.2F, true, false, false));
        event.registerEntityRenderer(ModEntities.SEAGULL.get(), c -> new AtollMobRenderer<>(c, "seagull", 0.3F, true, false, false));
        event.registerEntityRenderer(ModEntities.TOUCAN.get(), c -> new AtollMobRenderer<>(c, "toucan", 0.3F, true, false, false));
        event.registerEntityRenderer(ModEntities.ABYSSAL_ANGLER.get(), c -> new AtollMobRenderer<>(c, "abyssal_angler", 0.0F, false, true, false));
        event.registerEntityRenderer(ModEntities.GULPER_EEL.get(), c -> new AtollMobRenderer<>(c, "gulper_eel", 0.0F, false, true, false));
        event.registerEntityRenderer(ModEntities.DRAGONFISH.get(), c -> new AtollMobRenderer<>(c, "dragonfish", 0.0F, false, true, false));
        event.registerEntityRenderer(ModEntities.GIANT_ISOPOD.get(), c -> new AtollMobRenderer<>(c, "giant_isopod", 0.6F, false, true, false));
        event.registerEntityRenderer(ModEntities.VAMPIRE_SQUID.get(), c -> new AtollMobRenderer<>(c, "vampire_squid", 0.0F, false, true, false));
    }
}
